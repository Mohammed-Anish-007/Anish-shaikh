package com.anish.jarvis

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import org.json.JSONObject
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.util.Locale

/**
 * Always-listening foreground service. Pipeline (all on-device):
 * microphone -> Vosk speech-to-text + speaker x-vector -> voice-lock check -> parser -> executor.
 */
class JarvisService : Service(), RecognitionListener, TextToSpeech.OnInitListener {

    companion object {
        const val ACTION_STOP = "com.anish.jarvis.STOP"
        @Volatile var isRunning = false
        private const val CHANNEL = "jarvis"
        private const val NOTIF_ID = 7
        private const val FOLLOW_UP_MS = 8_000L
    }

    private val main = Handler(Looper.getMainLooper())
    private var recognizer: Recognizer? = null
    private var speech: SpeechService? = null
    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private var awaitUntil = 0L
    private lateinit var executor: CommandExecutor
    @Volatile private var destroyed = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createChannel()
        ServiceCompat.startForeground(
            this, NOTIF_ID, buildNotification("Starting…"),
            if (Build.VERSION.SDK_INT >= 30) ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0
        )
        tts = TextToSpeech(this, this)
        executor = CommandExecutor(this, ::say) { main.postDelayed({ stopSelf() }, 1500) }
        Thread { startEngine() }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) stopSelf()
        return START_NOT_STICKY
    }

    private fun startEngine() {
        try {
            val rec = Recognizer(VoskEngine.model(this), VoskEngine.SAMPLE_RATE, VoskEngine.speaker(this))
            if (destroyed) { rec.close(); return }
            recognizer = rec
            main.post {
                if (destroyed) return@post
                try {
                    speech = SpeechService(rec, VoskEngine.SAMPLE_RATE).also { it.startListening(this) }
                    updateNotification("Listening — say \"${Prefs.wakeWords(this).firstOrNull() ?: "Jarvis"}…\"")
                    JarvisLog.add("Jarvis is listening (offline)")
                    say("Jarvis online")
                } catch (e: Exception) {
                    JarvisLog.add("Mic error: ${e.message}")
                    stopSelf()
                }
            }
        } catch (e: Exception) {
            JarvisLog.add("Engine error: ${e.message}")
            main.post { stopSelf() }
        }
    }

    // ------------------------------------------------------------------ recognition

    override fun onPartialResult(hypothesis: String?) {}
    override fun onResult(hypothesis: String?) = handle(hypothesis)
    override fun onFinalResult(hypothesis: String?) = handle(hypothesis)

    override fun onError(exception: Exception?) {
        JarvisLog.add("Recognition error: ${exception?.message}")
        restartListening()
    }

    override fun onTimeout() = restartListening()

    private fun restartListening() {
        main.postDelayed({
            if (destroyed) return@postDelayed
            try { speech?.stop(); speech?.startListening(this) } catch (_: Exception) {}
        }, 1000)
    }

    private fun handle(json: String?) {
        if (json.isNullOrBlank() || destroyed) return
        val o = try { JSONObject(json) } catch (e: Exception) { return }
        val text = o.optString("text").trim()
        if (text.isEmpty()) return

        val arr = o.optJSONArray("spk")
        val frames = o.optInt("spk_frames", 0)
        val vec = arr?.let { a -> FloatArray(a.length()) { a.optDouble(it).toFloat() } }

        val wake = WakeWord.split(text, Prefs.wakeWords(this))
        val now = SystemClock.elapsedRealtime()
        val awaiting = now < awaitUntil

        if (Prefs.requireWakeWord(this) && !wake.found && !awaiting) {
            JarvisLog.add("(ignored) $text")
            return
        }
        if (wake.found && wake.rest.isBlank()) {
            awaitUntil = now + FOLLOW_UP_MS
            say("Yes?")
            return
        }
        if (!VoiceProfile.isEnrolled(this)) {
            say("Please record your voice in the Jarvis app first")
            return
        }

        val score = if (vec != null && frames >= VoiceProfile.MIN_FRAMES) VoiceProfile.score(this, vec) else -1f
        val ok = score >= Prefs.threshold(this)
        JarvisLog.add("Heard: \"$text\"  voice=" + "%.2f".format(score) + if (ok) " ✓ owner" else " ✗ rejected")
        val addressed = wake.found || awaiting
        awaitUntil = 0
        if (!ok) {
            if (addressed) say("Sorry, I only follow my owner's voice")
            return
        }

        wakeScreen()
        val cmd = CommandParser.parse(if (wake.found) wake.rest else text)
        JarvisLog.add("→ $cmd")
        executor.execute(cmd)
    }

    /** Turn the screen on so the result of the command is visible (it cannot bypass your lock screen). */
    private fun wakeScreen() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (pm.isInteractive) return
        @Suppress("DEPRECATION")
        val wl = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "jarvis:wake"
        )
        wl.acquire(5_000)
    }

    // ------------------------------------------------------------------ speech output

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val lang = if (Prefs.language(this) == "hi") Locale("hi", "IN") else Locale("en", "IN")
        if (tts.setLanguage(lang) < TextToSpeech.LANG_AVAILABLE) tts.setLanguage(Locale.ENGLISH)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { main.postDelayed({ speech?.setPause(false) }, 250) }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { main.post { speech?.setPause(false) } }
        })
        ttsReady = true
    }

    private var utt = 0

    private fun say(msg: String) {
        JarvisLog.add("Jarvis: $msg")
        main.post {
            if (!ttsReady || destroyed) return@post
            speech?.setPause(true) // don't listen to ourselves
            tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "u${utt++}")
        }
    }

    // ------------------------------------------------------------------ notification

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(CHANNEL, "Jarvis listening", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(text: String): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, JarvisService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_jarvis)
            .setContentTitle("Jarvis (offline)")
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(0, "Stop", stop)
            .build()
    }

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID, buildNotification(text))
    }

    override fun onDestroy() {
        destroyed = true
        isRunning = false
        main.removeCallbacksAndMessages(null)
        try { speech?.stop(); speech?.shutdown() } catch (_: Exception) {}
        speech = null
        try { recognizer?.close() } catch (_: Exception) {}
        recognizer = null
        tts.stop(); tts.shutdown()
        JarvisLog.add("Jarvis stopped")
        super.onDestroy()
    }
}
