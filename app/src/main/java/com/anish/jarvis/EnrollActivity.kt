package com.anish.jarvis

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.json.JSONObject
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService

/** Records 5 sentences and stores the owner's voice-print (numbers only, no audio). */
class EnrollActivity : Activity(), RecognitionListener {

    private val phrases = listOf(
        "Jarvis, open WhatsApp and call my mother.",
        "Hello Jarvis, set an alarm for seven thirty in the morning.",
        "Jarvis, yeh meri awaaz hai, sirf meri baat maanna.",
        "Jarvis, send a message to my friend that I am coming home.",
        "Jarvis, aaj mausam bahut accha hai, music chalao aur volume badhao."
    )

    private val samples = ArrayList<FloatArray>()
    private var recognizer: Recognizer? = null
    private var speech: SpeechService? = null
    private var done = false

    private lateinit var tvProgress: TextView
    private lateinit var tvPhrase: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enroll)
        tvProgress = findViewById(R.id.tvProgress)
        tvPhrase = findViewById(R.id.tvPhrase)
        tvStatus = findViewById(R.id.tvEnrollStatus)
        btn = findViewById(R.id.btnEnrollStart)

        // The microphone can only be used by one recogniser at a time.
        if (JarvisService.isRunning) stopService(Intent(this, JarvisService::class.java))

        showPhrase()
        btn.setOnClickListener {
            if (done) finish() else start()
        }
    }

    private fun showPhrase() {
        tvProgress.text = "Sentence ${samples.size + 1} of ${phrases.size}"
        tvPhrase.text = phrases[samples.size]
    }

    private fun start() {
        btn.isEnabled = false
        tvStatus.text = "Loading offline models…"
        Thread {
            try {
                val rec = Recognizer(VoskEngine.model(this), VoskEngine.SAMPLE_RATE, VoskEngine.speaker(this))
                runOnUiThread {
                    if (isFinishing) { rec.close(); return@runOnUiThread }
                    recognizer = rec
                    speech = SpeechService(rec, VoskEngine.SAMPLE_RATE).also { it.startListening(this) }
                    tvStatus.text = "🎙  Listening… read the sentence above clearly."
                }
            } catch (e: Exception) {
                runOnUiThread {
                    tvStatus.text = "Error: ${e.message}"
                    btn.isEnabled = true
                }
            }
        }.start()
    }

    override fun onResult(hypothesis: String?) = handle(hypothesis)
    override fun onFinalResult(hypothesis: String?) = handle(hypothesis)
    override fun onPartialResult(hypothesis: String?) {}
    override fun onError(exception: Exception?) { tvStatus.text = "Error: ${exception?.message}" }
    override fun onTimeout() {}

    private fun handle(json: String?) {
        if (done || json.isNullOrBlank()) return
        val o = try { JSONObject(json) } catch (e: Exception) { return }
        val text = o.optString("text").trim()
        if (text.isEmpty()) return
        val arr = o.optJSONArray("spk")
        val frames = o.optInt("spk_frames", 0)
        if (arr == null || frames < 120) {
            tvStatus.text = "Heard: \"$text\"\nToo short — please read the whole sentence again."
            return
        }
        samples += FloatArray(arr.length()) { arr.optDouble(it).toFloat() }
        if (samples.size >= phrases.size) {
            finishEnrollment()
        } else {
            tvStatus.text = "✓ Got it (\"$text\"). Now the next one."
            showPhrase()
        }
    }

    private fun finishEnrollment() {
        done = true
        try { speech?.stop() } catch (_: Exception) {}
        val suggested = VoiceProfile.save(this, samples)
        Prefs.setThreshold(this, suggested)
        tvProgress.text = "Done"
        tvPhrase.text = "Voice lock is ON 🔒"
        tvStatus.text = "Your voice-print is saved on this phone only.\n" +
            "Voice-match threshold set to %.2f (you can change it in Settings).".format(suggested)
        btn.text = "Finish"
        btn.isEnabled = true
        JarvisLog.add("Voice enrolled (${samples.size} samples), threshold %.2f".format(suggested))
    }

    override fun onDestroy() {
        try { speech?.stop(); speech?.shutdown() } catch (_: Exception) {}
        try { recognizer?.close() } catch (_: Exception) {}
        super.onDestroy()
    }
}
