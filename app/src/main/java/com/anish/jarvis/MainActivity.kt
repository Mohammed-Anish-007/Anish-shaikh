package com.anish.jarvis

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvLog: TextView
    private lateinit var tvThreshold: TextView
    private lateinit var sbThreshold: SeekBar
    private lateinit var cbWake: CheckBox
    private lateinit var etWake: EditText
    private lateinit var etCountry: EditText
    private lateinit var etTest: EditText
    private lateinit var rbEn: RadioButton
    private lateinit var rbHi: RadioButton
    private lateinit var btnToggle: Button
    private lateinit var btnDownload: Button

    private val main = Handler(Looper.getMainLooper())
    private val logListener: () -> Unit = { tvLog.text = JarvisLog.text() }
    private var downloading = false

    private val testExecutor by lazy {
        CommandExecutor(this, { msg ->
            JarvisLog.add("Jarvis: $msg")
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvLog = findViewById(R.id.tvLog)
        tvThreshold = findViewById(R.id.tvThreshold)
        sbThreshold = findViewById(R.id.sbThreshold)
        cbWake = findViewById(R.id.cbWake)
        etWake = findViewById(R.id.etWake)
        etCountry = findViewById(R.id.etCountry)
        etTest = findViewById(R.id.etTest)
        rbEn = findViewById(R.id.rbEn)
        rbHi = findViewById(R.id.rbHi)
        btnToggle = findViewById(R.id.btnToggle)
        btnDownload = findViewById(R.id.btnDownload)

        loadSettings()

        rbEn.setOnClickListener { Prefs.setLanguage(this, "en"); refresh() }
        rbHi.setOnClickListener { Prefs.setLanguage(this, "hi"); refresh() }

        sbThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) = showThreshold(p / 100f)
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        btnDownload.setOnClickListener { downloadModels() }
        findViewById<Button>(R.id.btnPerms).setOnClickListener { requestPerms() }
        findViewById<Button>(R.id.btnA11y).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Find \"Jarvis\" and turn it ON", Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        findViewById<Button>(R.id.btnEnroll).setOnClickListener {
            when {
                !ModelManager.allReady(this) -> toast("Download the offline models first (step ①)")
                !granted(Manifest.permission.RECORD_AUDIO) -> requestPerms()
                else -> startActivity(Intent(this, EnrollActivity::class.java))
            }
        }
        findViewById<Button>(R.id.btnWriteSettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
        }
        findViewById<Button>(R.id.btnNotifAccess).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            Toast.makeText(this, "Find \"Jarvis\" and allow it", Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.btnSave).setOnClickListener { saveSettings(); toast("Saved") }
        btnToggle.setOnClickListener { toggleService() }
        findViewById<Button>(R.id.btnTest).setOnClickListener {
            val t = etTest.text.toString()
            if (t.isBlank()) return@setOnClickListener
            val cmd = CommandParser.parse(WakeWord.split(t, Prefs.wakeWords(this)).rest)
            JarvisLog.add("Test: \"$t\" → $cmd")
            testExecutor.execute(cmd)
        }
    }

    override fun onResume() {
        super.onResume()
        JarvisLog.listen(logListener)
        tvLog.text = JarvisLog.text()
        refresh()
    }

    override fun onPause() {
        JarvisLog.unlisten(logListener)
        super.onPause()
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    private fun granted(p: String) = ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED

    private fun loadSettings() {
        if (Prefs.language(this) == "hi") rbHi.isChecked = true else rbEn.isChecked = true
        cbWake.isChecked = Prefs.requireWakeWord(this)
        etWake.setText(Prefs.wakeWordsRaw(this))
        etCountry.setText(Prefs.countryCode(this))
        val th = Prefs.threshold(this)
        sbThreshold.progress = (th * 100).toInt()
        showThreshold(th)
    }

    private fun showThreshold(v: Float) {
        tvThreshold.text = "Voice-lock strictness: %.2f  (higher = stricter; check \"voice=\" scores in the log)".format(v)
    }

    private fun saveSettings() {
        Prefs.setRequireWakeWord(this, cbWake.isChecked)
        Prefs.setWakeWords(this, etWake.text.toString().ifBlank { Prefs.DEFAULT_WAKE })
        Prefs.setCountryCode(this, etCountry.text.toString().filter { it.isDigit() }.ifBlank { "91" })
        Prefs.setThreshold(this, (sbThreshold.progress / 100f).coerceIn(0.2f, 0.95f))
    }

    private fun requiredPerms(): List<String> {
        val p = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ANSWER_PHONE_CALLS
        )
        if (Build.VERSION.SDK_INT >= 33) p += Manifest.permission.POST_NOTIFICATIONS
        return p
    }

    private fun requestPerms() {
        val missing = requiredPerms().filter { !granted(it) }
        if (missing.isEmpty()) toast("All permissions already granted ✓")
        else requestPermissions(missing.toTypedArray(), 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refresh()
    }

    private fun a11yEnabled(): Boolean {
        if (JarvisAccessibilityService.instance != null) return true
        val me = ComponentName(this, JarvisAccessibilityService::class.java).flattenToString()
        val s = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return s.split(':').any { it.equals(me, ignoreCase = true) }
    }

    private fun refresh() {
        fun mark(b: Boolean) = if (b) "✓" else "✗"
        val speechModel = ModelManager.speechModel(this)
        val perms = requiredPerms().all { granted(it) }
        tvStatus.text = buildString {
            appendLine("${mark(ModelManager.isReady(this@MainActivity, speechModel))} ${speechModel.label}")
            appendLine("${mark(ModelManager.isReady(this@MainActivity, ModelManager.SPEAKER))} ${ModelManager.SPEAKER.label}")
            appendLine("${mark(perms)} Permissions")
            appendLine("${mark(a11yEnabled())} Accessibility (typing / Send / back)")
            appendLine("${mark(Settings.canDrawOverlays(this@MainActivity))} Display over other apps (optional)")
            appendLine("${mark(VoiceProfile.isEnrolled(this@MainActivity))} Owner voice recorded")
            appendLine("${mark(Settings.System.canWrite(this@MainActivity))} Brightness control (optional)")
            appendLine("${mark(NotificationManagerCompat.getEnabledListenerPackages(this@MainActivity).contains(packageName))} Read notifications (optional)")
            append(if (JarvisService.isRunning) "● Jarvis is LISTENING" else "○ Jarvis is stopped")
        }
        btnToggle.text = if (JarvisService.isRunning) "■  Stop Jarvis" else "▶  Start Jarvis"
        btnDownload.isEnabled = !downloading
    }

    private fun downloadModels() {
        val todo = listOf(ModelManager.speechModel(this), ModelManager.SPEAKER).filter { !ModelManager.isReady(this, it) }
        if (todo.isEmpty()) { toast("Models already downloaded ✓"); return }
        downloading = true
        refresh()
        Thread {
            try {
                for (m in todo) ModelManager.download(this, m) { msg -> JarvisLog.add(msg) }
                JarvisLog.add("All models ready. From now on Jarvis works without internet.")
            } catch (e: Exception) {
                JarvisLog.add("Download failed: ${e.message}. Check internet and try again.")
            } finally {
                runOnUiThread { downloading = false; refresh() }
            }
        }.start()
    }

    private fun toggleService() {
        if (JarvisService.isRunning) {
            startService(Intent(this, JarvisService::class.java).setAction(JarvisService.ACTION_STOP))
        } else {
            saveSettings()
            when {
                !ModelManager.allReady(this) -> { toast("Download the offline models first (step ①)"); return }
                !granted(Manifest.permission.RECORD_AUDIO) -> { requestPerms(); return }
                !VoiceProfile.isEnrolled(this) -> { toast("Record your voice first (step ⑤) — Jarvis only obeys you"); return }
            }
            if (!a11yEnabled()) toast("Tip: enable Accessibility (step ③) for typing, Send and opening apps")
            ContextCompat.startForegroundService(this, Intent(this, JarvisService::class.java))
        }
        main.postDelayed({ refresh() }, 600)
        main.postDelayed({ refresh() }, 2500)
    }
}
