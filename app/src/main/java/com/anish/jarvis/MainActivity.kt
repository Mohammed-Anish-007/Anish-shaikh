package com.anish.jarvis

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {

    companion object {
        const val EXTRA_AUTOSTART = "autostart"
        private const val REQ_PERMS = 1
        private const val REQ_AUTH = 2
        private const val AUTH_WINDOW_MS = 5 * 60_000L
    }

    private lateinit var orb: ReactorView
    private lateinit var tvState: TextView
    private lateinit var tvHint: TextView
    private lateinit var tvBadge: TextView
    private lateinit var tvSetupCount: TextView
    private lateinit var pbSetup: ProgressBar
    private lateinit var setupList: LinearLayout
    private lateinit var cardSetup: View
    private lateinit var chatList: LinearLayout
    private lateinit var etTest: EditText
    private lateinit var tvThreshold: TextView
    private lateinit var sbThreshold: SeekBar
    private lateinit var cbWake: CheckBox
    private lateinit var etWake: EditText
    private lateinit var etCountry: EditText
    private lateinit var rbEn: RadioButton
    private lateinit var rbHi: RadioButton

    private val main = Handler(Looper.getMainLooper())
    private val logListener: () -> Unit = { renderChat() }
    private var downloading = false
    private var downloadMsg = ""

    // "Set up everything" wizard
    private var wizard = false
    private val wizardTried = mutableSetOf<String>()
    private var lastStepKey: String? = null

    // owner check (phone lock) before settings / typed commands
    private var authUntil = 0L
    private var pendingAuth: (() -> Unit)? = null
    private var lastRunning: Boolean? = null

    private val poll = object : Runnable {
        override fun run() {
            if (lastRunning != JarvisService.isRunning) refresh()
            main.postDelayed(this, 1000)
        }
    }

    private val testExecutor by lazy {
        CommandExecutor(this, { msg ->
            JarvisLog.add("Jarvis: $msg")
        })
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun granted(p: String) = ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide Jarvis's screen from screenshots, screen recorders and the recent-apps preview.
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_main)

        orb = findViewById(R.id.orb)
        tvState = findViewById(R.id.tvState)
        tvHint = findViewById(R.id.tvHint)
        tvBadge = findViewById(R.id.tvBadge)
        tvSetupCount = findViewById(R.id.tvSetupCount)
        pbSetup = findViewById(R.id.pbSetup)
        setupList = findViewById(R.id.setupList)
        cardSetup = findViewById(R.id.cardSetup)
        chatList = findViewById(R.id.chatList)
        etTest = findViewById(R.id.etTest)
        tvThreshold = findViewById(R.id.tvThreshold)
        sbThreshold = findViewById(R.id.sbThreshold)
        cbWake = findViewById(R.id.cbWake)
        etWake = findViewById(R.id.etWake)
        etCountry = findViewById(R.id.etCountry)
        rbEn = findViewById(R.id.rbEn)
        rbHi = findViewById(R.id.rbHi)

        orb.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            toggleService()
        }

        buildChips()
        loadSettings()

        rbEn.setOnClickListener { Prefs.setLanguage(this, "en"); refresh(); prepareModels() }
        rbHi.setOnClickListener { Prefs.setLanguage(this, "hi"); refresh(); prepareModels() }
        sbThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) = showThreshold(p / 100f)
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        val body = findViewById<View>(R.id.settingsBody)
        val arrow = findViewById<TextView>(R.id.tvSettingsArrow)
        findViewById<View>(R.id.settingsHeader).setOnClickListener {
            val open = body.visibility != View.VISIBLE
            val toggle = {
                body.visibility = if (open) View.VISIBLE else View.GONE
                arrow.text = if (open) "▴" else "▾"
            }
            if (open) requireOwner(toggle) else toggle()
        }
        findViewById<Button>(R.id.btnSave).setOnClickListener { requireOwner { saveSettings(); toast("Saved ✓") } }
        findViewById<View>(R.id.btnReEnroll).setOnClickListener { requireOwner { openEnroll() } }
        findViewById<View>(R.id.btnWizard).setOnClickListener { startWizard() }

        if (intent?.getBooleanExtra(EXTRA_AUTOSTART, false) == true) main.postDelayed({ startIfReady() }, 400)

        findViewById<View>(R.id.btnTest).setOnClickListener { runTest(etTest.text.toString()) }
        etTest.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEND) { runTest(etTest.text.toString()); true } else false
        }
    }

    override fun onResume() {
        super.onResume()
        JarvisLog.listen(logListener)
        renderChat()
        refresh()
        main.post(poll)
        if (!ModelManager.allReady(this)) prepareModels()
        var paused = false
        if (lastStepKey == "a11y_info") {
            // back from "Allow restricted settings" → straight to Accessibility again
            lastStepKey = "a11y"
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            paused = true
        } else if (lastStepKey == "a11y" && !a11yEnabled()) {
            paused = showRestrictedSettingsHelp()
        }
        if (wizard && !paused) main.postDelayed({ nextWizardStep() }, 700)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra(EXTRA_AUTOSTART, false) == true) startIfReady()
    }

    private fun startIfReady() {
        if (!JarvisService.isRunning) toggleService()
    }

    override fun onPause() {
        JarvisLog.unlisten(logListener)
        main.removeCallbacks(poll)
        super.onPause()
    }

    // ------------------------------------------------------------------ hero / status

    private fun refresh() {
        val running = JarvisService.isRunning
        lastRunning = running
        orb.active = running
        val ready = ModelManager.allReady(this) && VoiceProfile.isEnrolled(this) && granted(Manifest.permission.RECORD_AUDIO)
        val wake = Prefs.wakeWords(this).firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Jarvis"
        when {
            running -> {
                tvState.text = "Listening…"
                tvHint.text = "Say \"$wake, open YouTube\" · tap the core to stop"
                tvBadge.text = "●  ONLINE"
                tvBadge.setTextColor(ContextCompat.getColor(this, R.color.ok))
            }
            ready -> {
                tvState.text = "Tap the core to wake me"
                tvHint.text = "Everything is set up. I only respond to your voice."
                tvBadge.text = "○  OFFLINE"
                tvBadge.setTextColor(ContextCompat.getColor(this, R.color.muted))
            }
            else -> {
                tvState.text = "Let's set me up"
                tvHint.text = "Finish the steps below. It takes about 2 minutes."
                tvBadge.text = "SETUP"
                tvBadge.setTextColor(ContextCompat.getColor(this, R.color.warn))
            }
        }
        renderSetup()
    }

    private fun buildChips() {
        val chips = findViewById<LinearLayout>(R.id.chips)
        val examples = listOf(
            "Open YouTube", "Wi-Fi on karo", "Battery kitni hai", "Torch on",
            "What time is it", "Brightness half", "Next reel", "Notifications padho"
        )
        for (e in examples) {
            val tv = TextView(this).apply {
                text = e
                setTextColor(ContextCompat.getColor(context, R.color.text))
                textSize = 13f
                background = ContextCompat.getDrawable(context, R.drawable.chip)
                setPadding(dp(14), dp(8), dp(14), dp(8))
                setOnClickListener { runTest(e) }
            }
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.marginEnd = dp(8)
            chips.addView(tv, lp)
        }
    }

    // ------------------------------------------------------------------ setup checklist

    private class Step(
        val key: String,
        val title: String,
        val subtitle: String,
        val done: Boolean,
        val optional: Boolean,
        val action: () -> Unit
    )

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

    private fun a11yEnabled(): Boolean {
        if (JarvisAccessibilityService.instance != null) return true
        val me = ComponentName(this, JarvisAccessibilityService::class.java).flattenToString()
        val s = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return s.split(':').any { it.equals(me, ignoreCase = true) }
    }

    private fun steps(): List<Step> {
        val lang = if (Prefs.language(this) == "hi") "Hindi" else "English / Hinglish"
        val list = mutableListOf(
            Step(
                "models", "Voice brain",
                when {
                    downloading -> downloadMsg.ifEmpty { "Preparing…" }
                    ModelManager.allReady(this) -> "$lang · built in · works offline"
                    else -> "Preparing on first launch…"
                },
                ModelManager.allReady(this), false
            ) { prepareModels() },
            Step(
                "perms", "Allow permissions", "Microphone, contacts, calls, SMS, notifications",
                requiredPerms().all { granted(it) }, false
            ) { requestPerms() },
            Step(
                "a11y", "Turn on phone control", "Accessibility → Jarvis → On",
                a11yEnabled(), false
            ) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                toast("Tap \"Jarvis\" (it may be under \"Installed apps\") and turn it ON")
            },
            Step(
                "voice", "Record my voice", "Voice lock · only you can command Jarvis",
                VoiceProfile.isEnrolled(this), false
            ) { openEnroll() },
            Step(
                "battery", "Keep running in background", "Battery → Unrestricted (one tap)",
                PhoneSetup.ignoresBatteryOptimizations(this), false
            ) {
                try { startActivity(PhoneSetup.batteryIntent(this)) }
                catch (e: Exception) { startActivity(PhoneSetup.appInfoIntent(this)) }
            }
        )
        if (PhoneSetup.needsAutostart()) list += Step(
            "autostart", "Allow Autostart", "${Build.MANUFACTURER} phones stop apps without this",
            Prefs.flag(this, "autostart_done"), false
        ) {
            Prefs.setFlag(this, "autostart_done", true)
            PhoneSetup.openAutostart(this)
            toast("Turn Autostart ON for Jarvis")
        }
        // No "Display over other apps" step: Jarvis doesn't need it. Once Accessibility is on,
        // Android already lets it open apps from the background.
        list += listOf(
            Step(
                "brightness", "Brightness control", "Modify system settings",
                Settings.System.canWrite(this), true
            ) { startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))) },
            Step(
                "notif", "Read notifications aloud", "Notification access",
                NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName), true
            ) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                toast("Find \"Jarvis\" and allow it")
            },
            Step(
                "dnd", "Silent mode control", "Do Not Disturb access",
                PhoneSetup.hasDndAccess(this), true
            ) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                toast("Find \"Jarvis\" and allow it")
            }
        )
        return list
    }

    // ------------------------------------------------------------------ one-tap wizard

    private fun startWizard() {
        wizard = true
        wizardTried.clear()
        toast("I'll open each setting one by one. Allow it and press Back.")
        nextWizardStep()
    }

    private var lastAdvance = 0L

    private fun nextWizardStep() {
        if (!wizard || isFinishing || pendingAuth != null) return
        val now = System.currentTimeMillis()
        if (now - lastAdvance < 1500) return   // permission popups trigger resume + result; advance once
        lastAdvance = now
        val next = steps().firstOrNull { !it.done && it.key != "models" && it.key !in wizardTried }
        if (next == null) {
            wizard = false
            refresh()
            val missing = steps().count { !it.done && !it.optional }
            toast(if (missing == 0) "All set ✓  Tap the core to start Jarvis" else "Almost done – $missing step(s) still need you")
            return
        }
        wizardTried += next.key
        runStep(next)
    }

    private fun runStep(st: Step) {
        lastStepKey = st.key
        st.action()
    }

    /** Android 13+ blocks Accessibility for apps installed from an APK until you allow it. */
    private fun showRestrictedSettingsHelp(): Boolean {
        lastStepKey = null
        if (Build.VERSION.SDK_INT < 33) return false
        AlertDialog.Builder(this)
            .setTitle("Switch is greyed out?")
            .setMessage(
                "Android protects Accessibility for apps installed from an APK.\n\n" +
                    "1. Tap \"Open App info\" below\n" +
                    "2. Tap ⋮ (top-right corner)\n" +
                    "3. Tap \"Allow restricted settings\" and confirm with your PIN\n" +
                    "4. Come back and turn on Jarvis in Accessibility"
            )
            .setPositiveButton("Open App info") { _, _ ->
                lastStepKey = "a11y_info"
                startActivity(PhoneSetup.appInfoIntent(this))
            }
            .setNegativeButton("Skip") { _, _ -> if (wizard) nextWizardStep() }
            .setCancelable(false)
            .show()
        return true
    }

    private fun renderSetup() {
        val all = steps()
        val doneCount = all.count { it.done }
        tvSetupCount.text = "$doneCount of ${all.size} done"
        pbSetup.progress = doneCount * 100 / all.size
        setupList.removeAllViews()
        all.forEachIndexed { i, s -> setupList.addView(stepRow(i + 1, s)) }
    }

    private fun stepRow(n: Int, s: Step): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(12))
            val attrs = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
            background = attrs.getDrawable(0)
            attrs.recycle()
            setOnClickListener { if (!s.done) runStep(s) }
        }
        val badge = TextView(this).apply {
            text = if (s.done) "✓" else n.toString()
            gravity = Gravity.CENTER
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ContextCompat.getColor(context, if (s.done) R.color.ok else R.color.accent))
            background = ContextCompat.getDrawable(context, if (s.done) R.drawable.circle_ok else R.drawable.circle_todo)
        }
        row.addView(badge, LinearLayout.LayoutParams(dp(34), dp(34)))

        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        texts.addView(TextView(this).apply {
            text = s.title + if (s.optional) "  ·  optional" else ""
            textSize = 15f
            setTextColor(ContextCompat.getColor(context, if (s.done) R.color.muted else R.color.text))
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })
        texts.addView(TextView(this).apply {
            text = s.subtitle
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.muted))
        })
        val tlp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        tlp.marginStart = dp(14)
        row.addView(texts, tlp)

        if (!s.done) {
            row.addView(TextView(this).apply {
                text = "›"
                textSize = 24f
                setTextColor(ContextCompat.getColor(context, R.color.accent))
            })
        }
        return row
    }

    // ------------------------------------------------------------------ conversation

    private fun renderChat() {
        val entries = JarvisLog.recent(14)
        chatList.removeAllViews()
        if (entries.isEmpty()) {
            chatList.addView(TextView(this).apply {
                text = "Nothing yet. Tap a suggestion above or start Jarvis and speak."
                textSize = 13f
                setTextColor(ContextCompat.getColor(context, R.color.muted))
                setPadding(0, dp(8), 0, dp(4))
            })
            return
        }
        for (e in entries) {
            val m = e.msg
            when {
                m.startsWith("Jarvis: ") -> chatList.addView(bubble(m.removePrefix("Jarvis: "), e.time, mine = false))
                m.startsWith("Heard: ") -> chatList.addView(bubble(m.removePrefix("Heard: "), e.time, mine = true))
                m.startsWith("Test: ") -> chatList.addView(bubble(m.removePrefix("Test: ").substringBefore(" → "), e.time, mine = true))
                m.startsWith("→ ") -> {}
                else -> chatList.addView(systemLine(m))
            }
        }
    }

    private fun bubble(msg: String, time: String, mine: Boolean): View {
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (mine) Gravity.END else Gravity.START
            setPadding(0, dp(4), 0, dp(4))
        }
        val tv = TextView(this).apply {
            text = msg.trim('"')
            textSize = 14f
            setTextColor(Color.WHITE)
            maxWidth = (resources.displayMetrics.widthPixels * 0.72).toInt()
            background = ContextCompat.getDrawable(context, if (mine) R.drawable.bubble_me else R.drawable.bubble_jarvis)
            setPadding(dp(14), dp(9), dp(14), dp(9))
        }
        wrap.addView(tv, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        wrap.addView(TextView(this).apply {
            text = if (mine) "You · $time" else "Jarvis · $time"
            textSize = 10f
            setTextColor(ContextCompat.getColor(context, R.color.muted))
            setPadding(dp(6), dp(2), dp(6), 0)
        })
        return wrap
    }

    private fun systemLine(msg: String) = TextView(this).apply {
        text = msg
        textSize = 11f
        gravity = Gravity.CENTER
        setTextColor(ContextCompat.getColor(context, R.color.muted))
        setPadding(dp(8), dp(4), dp(8), dp(4))
    }

    private fun runTest(raw: String) {
        val t = raw.trim()
        if (t.isEmpty()) return
        // Typed commands skip the voice lock, so they need your phone lock instead.
        requireOwner { runTestNow(t) }
    }

    private fun runTestNow(t: String) {
        etTest.setText("")
        val cmd = CommandParser.parse(WakeWord.split(t, Prefs.wakeWords(this)).rest)
        JarvisLog.add("Test: $t → $cmd")
        testExecutor.execute(cmd)
    }

    // ------------------------------------------------------------------ settings

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
        tvThreshold.text = "Voice-lock strictness: %.2f  (higher = stricter)".format(v)
    }

    private fun saveSettings() {
        Prefs.setRequireWakeWord(this, cbWake.isChecked)
        Prefs.setWakeWords(this, etWake.text.toString().ifBlank { Prefs.DEFAULT_WAKE })
        Prefs.setCountryCode(this, etCountry.text.toString().filter { it.isDigit() }.ifBlank { "91" })
        Prefs.setThreshold(this, (sbThreshold.progress / 100f).coerceIn(0.2f, 0.95f))
    }

    // ------------------------------------------------------------------ actions

    private fun requestPerms() {
        val missing = requiredPerms().filter { !granted(it) }
        if (missing.isEmpty()) { toast("All permissions already granted ✓"); return }
        if (Prefs.flag(this, "perms_asked") && missing.none { shouldShowRequestPermissionRationale(it) }) {
            // Android won't show the popup again ("Don't ask again"), so open App info → Permissions.
            toast("Open Permissions and allow everything for Jarvis")
            startActivity(PhoneSetup.appInfoIntent(this))
            return
        }
        Prefs.setFlag(this, "perms_asked", true)
        requestPermissions(missing.toTypedArray(), REQ_PERMS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refresh()
        if (wizard) main.postDelayed({ nextWizardStep() }, 400)
    }

    // ------------------------------------------------------------------ owner check

    /** Runs [action] only after the phone's own PIN / pattern / fingerprint confirms it's you. */
    private fun requireOwner(action: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now < authUntil) { action(); return }
        val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (!km.isDeviceSecure) {
            toast("Tip: set a screen lock on your phone so nobody else can change Jarvis")
            action(); return
        }
        @Suppress("DEPRECATION")
        val i = km.createConfirmDeviceCredentialIntent("Jarvis", "Confirm it's you")
        if (i == null) { action(); return }
        pendingAuth = action
        startActivityForResult(i, REQ_AUTH)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ_AUTH) return
        val action = pendingAuth
        pendingAuth = null
        if (resultCode == RESULT_OK) {
            authUntil = System.currentTimeMillis() + AUTH_WINDOW_MS
            action?.invoke()
        } else {
            toast("Not confirmed")
        }
    }

    private fun openEnroll() {
        when {
            !ModelManager.allReady(this) -> toast("Voice brain is still being prepared, please wait a moment")
            !granted(Manifest.permission.RECORD_AUDIO) -> requestPerms()
            else -> startActivity(Intent(this, EnrollActivity::class.java))
        }
    }

    private fun prepareModels() {
        if (downloading || ModelManager.allReady(this)) return
        downloading = true
        downloadMsg = "Preparing voice brain…"
        refresh()
        Thread {
            try {
                ModelManager.prepareAll(this) { msg ->
                    downloadMsg = msg
                    runOnUiThread { renderSetup() }
                }
                JarvisLog.add("Voice brain ready. Everything runs offline on this phone.")
            } catch (e: Exception) {
                JarvisLog.add("Could not prepare the voice brain: ${e.message}")
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
                !ModelManager.allReady(this) -> { toast("Voice brain is still being prepared, please wait a moment"); return }
                !granted(Manifest.permission.RECORD_AUDIO) -> { requestPerms(); return }
                !VoiceProfile.isEnrolled(this) -> { toast("Record your voice first (step 4). Jarvis only obeys you."); return }
            }
            if (!a11yEnabled()) toast("Tip: turn on phone control (step 3) for typing, taps and Wi-Fi")
            ContextCompat.startForegroundService(this, Intent(this, JarvisService::class.java))
        }
        main.postDelayed({ refresh() }, 600)
    }
}
