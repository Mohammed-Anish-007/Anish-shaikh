package com.anish.jarvis

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.SmsManager
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Carries out a parsed [Command] on the phone. Everything happens locally. */
class CommandExecutor(
    private val ctx: Context,
    private val say: (String) -> Unit,
    private val onStop: () -> Unit = {}
) {
    private val a11y get() = JarvisAccessibilityService.instance

    fun execute(cmd: Command) {
        try {
            run(cmd)
        } catch (e: Exception) {
            JarvisLog.add("Error: ${e.javaClass.simpleName}: ${e.message}")
            say("Sorry, that did not work")
        }
    }

    private fun granted(p: String) = ContextCompat.checkSelfPermission(ctx, p) == PackageManager.PERMISSION_GRANTED

    /** Activity starts from the background are allowed because our accessibility service is bound. */
    private fun launch(i: Intent) {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        (a11y ?: ctx).startActivity(i)
    }

    private fun needA11y(): Boolean {
        if (a11y == null) { say("Please enable Jarvis in Accessibility settings first"); return true }
        return false
    }

    private fun run(cmd: Command) {
        when (cmd) {
            is Command.OpenApp -> openApp(cmd.name)
            is Command.Call -> call(cmd.who)
            is Command.Message -> message(cmd)
            is Command.Alarm -> {
                launch(Intent(AlarmClock.ACTION_SET_ALARM)
                    .putExtra(AlarmClock.EXTRA_HOUR, cmd.hour)
                    .putExtra(AlarmClock.EXTRA_MINUTES, cmd.minute)
                    .putExtra(AlarmClock.EXTRA_MESSAGE, "Jarvis")
                    .putExtra(AlarmClock.EXTRA_SKIP_UI, true))
                say("Alarm set for ${timeText(cmd.hour, cmd.minute)}")
            }
            is Command.Timer -> {
                launch(Intent(AlarmClock.ACTION_SET_TIMER)
                    .putExtra(AlarmClock.EXTRA_LENGTH, cmd.seconds)
                    .putExtra(AlarmClock.EXTRA_MESSAGE, "Jarvis")
                    .putExtra(AlarmClock.EXTRA_SKIP_UI, true))
                say("Timer started for ${durationText(cmd.seconds)}")
            }
            is Command.Type -> {
                if (needA11y()) return
                if (a11y?.typeText(cmd.text) == true) say("Typed") else say("I couldn't find a text box")
            }
            Command.Send -> {
                if (needA11y()) return
                if (a11y?.clickSend() == true) say("Sent") else say("I couldn't find the send button")
            }
            Command.Back -> if (!needA11y()) a11y?.global(AccessibilityService.GLOBAL_ACTION_BACK)
            Command.Home -> if (!needA11y()) a11y?.global(AccessibilityService.GLOBAL_ACTION_HOME)
            Command.Recents -> if (!needA11y()) a11y?.global(AccessibilityService.GLOBAL_ACTION_RECENTS)
            Command.Notifications -> if (!needA11y()) a11y?.global(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
            Command.ScrollDown -> if (!needA11y()) a11y?.scroll(true)
            Command.ScrollUp -> if (!needA11y()) a11y?.scroll(false)
            Command.TellTime -> say("It's " + SimpleDateFormat("h:mm a", Locale.ENGLISH).format(Date()))
            Command.TorchOn -> torch(true)
            Command.TorchOff -> torch(false)
            Command.VolumeUp -> volume(AudioManager.ADJUST_RAISE)
            Command.VolumeDown -> volume(AudioManager.ADJUST_LOWER)
            Command.StopListening -> { say("Going to sleep"); onStop() }
            is Command.Toggle -> toggle(cmd.feature, cmd.on)
            is Command.OpenSettings -> { launch(Intent(settingsAction(cmd.feature))); say("Opening ${cmd.feature.spoken} settings") }
            is Command.Brightness -> brightness(cmd)
            is Command.Click -> {
                if (needA11y()) return
                val hit = a11y?.clickByText(cmd.target, cmd.long)
                if (hit == null) say("I couldn't find ${cmd.target} on the screen")
            }
            is Command.Swipe -> if (!needA11y()) a11y?.swipe(cmd.dir)
            is Command.YoutubeSearch -> youtube(cmd.query)
            is Command.WebSearch -> webSearch(cmd.query)
            Command.ReadNotifications -> readNotifications()
            Command.ClearNotifications -> {
                JarvisNotificationListener.instance?.cancelAllNotifications()
                JarvisNotificationListener.clear()
                say("Notifications cleared")
            }
            Command.BatteryLevel -> {
                val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                say("Battery is at $pct percent" + if (bm.isCharging) ", charging" else "")
            }
            Command.Mute -> {
                audio().adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
            }
            Command.Unmute -> {
                audio().adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI)
                say("Sound on")
            }
            Command.VibrateMode -> ringer(AudioManager.RINGER_MODE_VIBRATE, "Vibrate mode on")
            Command.SilentMode -> ringer(AudioManager.RINGER_MODE_SILENT, "Silent mode on")
            Command.RingMode -> ringer(AudioManager.RINGER_MODE_NORMAL, "Ring mode on")
            Command.MediaPlay -> media(KeyEvent.KEYCODE_MEDIA_PLAY)
            Command.MediaPause -> media(KeyEvent.KEYCODE_MEDIA_PAUSE)
            Command.MediaNext -> media(KeyEvent.KEYCODE_MEDIA_NEXT)
            Command.MediaPrevious -> media(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            Command.AnswerCall -> answerCall()
            Command.EndCall -> endCall()
            Command.Screenshot -> globalP(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
            Command.LockScreen -> globalP(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
            Command.PowerMenu -> if (!needA11y()) a11y?.global(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
            is Command.Unknown -> say(if (cmd.reason.isNotEmpty()) "Sorry, ${cmd.reason}" else "Sorry, I didn't understand")
        }
    }

    private fun openApp(name: String) {
        val n = TextUtil.normalize(name)
        if (n == "camera" || n == "कैमरा") {
            launch(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)); say("Opening camera"); return
        }
        val app = AppFinder.find(ctx, name)
        if (app == null) { say("I couldn't find an app called $name"); return }
        val i = ctx.packageManager.getLaunchIntentForPackage(app.pkg)
        if (i == null) { say("I can't open ${app.label}"); return }
        launch(i)
        say("Opening ${app.label}")
    }

    private fun call(who: String) {
        if (!granted(Manifest.permission.CALL_PHONE)) { say("Please allow the phone permission"); return }
        val number = NumberWords.phoneNumber(who)
        val (target, label) = if (number != null) number to number else {
            val c = ContactFinder.find(ctx, who)?.first
            if (c == null) {
                say(if (ContactFinder.hasPermission(ctx)) "I couldn't find $who in your contacts" else "Please allow the contacts permission")
                return
            }
            c.number to c.name
        }
        launch(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", target, null)))
        say("Calling $label")
    }

    private fun message(cmd: Command.Message) {
        var contact: ContactFinder.Contact? = null
        var text = cmd.text
        val number = NumberWords.phoneNumber(cmd.who)
        if (number != null) {
            contact = ContactFinder.Contact(number, number)
        } else if (cmd.splitNeeded) {
            // "message rahul hello how are you" – find the longest prefix that is a contact
            val words = cmd.who.split(" ")
            val list = ContactFinder.all(ctx)
            var bestScore = 0.0
            var bestN = 0
            for (n in 1..minOf(3, words.size)) {
                val r = TextUtil.bestMatch(list.map { it.name }, words.take(n).joinToString(" ")) ?: continue
                if (r.second >= ContactFinder.MIN_SCORE && r.second >= bestScore) {
                    bestScore = r.second; bestN = n; contact = list[r.first]
                }
            }
            if (contact != null) text = (words.drop(bestN).joinToString(" ") + " " + text).trim()
        } else {
            contact = ContactFinder.find(ctx, cmd.who)?.first
        }
        if (contact == null) {
            say(if (ContactFinder.hasPermission(ctx)) "I couldn't find that contact" else "Please allow the contacts permission")
            return
        }

        when (cmd.channel) {
            Command.Channel.SMS -> sendSms(contact, text)
            Command.Channel.WHATSAPP -> sendWhatsApp(contact, text)
        }
    }

    private fun sendSms(c: ContactFinder.Contact, text: String) {
        if (text.isBlank()) {
            launch(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(c.number))))
            say("What should I write to ${c.name}? Say type, then your message")
            return
        }
        if (!granted(Manifest.permission.SEND_SMS)) { say("Please allow the SMS permission"); return }
        val sms: SmsManager = if (Build.VERSION.SDK_INT >= 31) {
            ctx.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION") SmsManager.getDefault()
        }
        val parts = sms.divideMessage(text)
        sms.sendMultipartTextMessage(c.number, null, parts, null, null)
        JarvisLog.add("SMS to ${c.name}: $text")
        say("Message sent to ${c.name}")
    }

    private fun installed(pkg: String) = try {
        ctx.packageManager.getPackageInfo(pkg, 0); true
    } catch (e: PackageManager.NameNotFoundException) { false }

    private fun sendWhatsApp(c: ContactFinder.Contact, text: String) {
        val pkg = listOf("com.whatsapp", "com.whatsapp.w4b").firstOrNull { installed(it) }
        if (pkg == null) { say("WhatsApp is not installed"); return }
        var digits = c.number.filter { it.isDigit() }
        if (c.number.trim().startsWith("+")) {
            // already international
        } else {
            digits = digits.trimStart('0')
            if (digits.length <= 10) digits = Prefs.countryCode(ctx) + digits
        }
        val url = "https://api.whatsapp.com/send?phone=$digits" +
            if (text.isNotBlank()) "&text=" + URLEncoder.encode(text, "UTF-8") else ""
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage(pkg)
        if (text.isNotBlank() && a11y != null) JarvisAccessibilityService.armAutoSend(pkg)
        launch(i)
        when {
            text.isBlank() -> say("Opening WhatsApp chat with ${c.name}")
            a11y != null -> say("Sending WhatsApp message to ${c.name}")
            else -> say("Message ready for ${c.name}. Tap send")
        }
    }

    private fun torch(on: Boolean) {
        val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val id = cm.cameraIdList.firstOrNull {
            cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
        if (id == null) { say("No flashlight found"); return }
        cm.setTorchMode(id, on)
        say(if (on) "Torch on" else "Torch off")
    }

    private fun volume(dir: Int) {
        val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        repeat(2) { am.adjustStreamVolume(AudioManager.STREAM_MUSIC, dir, AudioManager.FLAG_SHOW_UI) }
    }

    // ------------------------------------------------------------------ full-control helpers

    private fun audio() = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /** Screenshot / lock need Android 9+. */
    private fun globalP(action: Int) {
        if (Build.VERSION.SDK_INT < 28) { say("This needs Android 9 or newer"); return }
        if (needA11y()) return
        a11y?.global(action)
    }

    private fun settingsAction(f: Command.Feature): String = when (f) {
        Command.Feature.WIFI -> Settings.ACTION_WIFI_SETTINGS
        Command.Feature.BLUETOOTH -> Settings.ACTION_BLUETOOTH_SETTINGS
        Command.Feature.MOBILE_DATA -> Settings.ACTION_DATA_ROAMING_SETTINGS
        Command.Feature.AIRPLANE -> Settings.ACTION_AIRPLANE_MODE_SETTINGS
        Command.Feature.HOTSPOT -> Settings.ACTION_WIRELESS_SETTINGS
        Command.Feature.LOCATION -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
        Command.Feature.DND -> Settings.ACTION_SOUND_SETTINGS
        Command.Feature.AUTO_ROTATE -> Settings.ACTION_DISPLAY_SETTINGS
        Command.Feature.BATTERY_SAVER -> Settings.ACTION_BATTERY_SAVER_SETTINGS
    }

    /** Quick-Settings tile labels (English + Hindi phones), and the switch label inside Android 12+'s Internet dialog. */
    private fun tileNames(f: Command.Feature): Pair<List<String>, String?> {
        val n: List<String> = when (f) {
            Command.Feature.WIFI -> listOf("wi fi", "wifi", "wlan", "वाई फाई", "वाईफाई", "internet", "इंटरनेट")
            Command.Feature.BLUETOOTH -> listOf("bluetooth", "ब्लूटूथ")
            Command.Feature.MOBILE_DATA -> listOf("mobile data", "mobile", "cellular", "मोबाइल डेटा", "internet", "इंटरनेट")
            Command.Feature.AIRPLANE -> listOf("airplane", "aeroplane", "flight", "हवाई जहाज")
            Command.Feature.HOTSPOT -> listOf("hotspot", "mobile hotspot", "हॉटस्पॉट")
            Command.Feature.LOCATION -> listOf("location", "लोकेशन", "स्थान")
            Command.Feature.DND -> listOf("do not disturb", "dnd", "परेशान न करें")
            Command.Feature.AUTO_ROTATE -> listOf("auto rotate", "auto rotation", "rotate", "portrait", "ऑटो रोटेट")
            Command.Feature.BATTERY_SAVER -> listOf("battery saver", "power saving", "power saver", "बैटरी सेवर")
        }
        val dialog = when (f) {
            Command.Feature.WIFI -> "wi fi"
            Command.Feature.MOBILE_DATA -> "mobile data"
            else -> null
        }
        return n.map { TextUtil.normalize(it) } to dialog
    }

    private fun toggle(f: Command.Feature, on: Boolean?) {
        val a = a11y
        if (a == null) {
            // Without accessibility we can only open the right settings page.
            launch(Intent(settingsAction(f)))
            say("Opening ${f.spoken} settings. Enable Jarvis accessibility to switch it by voice")
            return
        }
        val (names, dialog) = tileNames(f)
        val what = when (on) { true -> "on"; false -> "off"; null -> "switched" }
        a.toggleQuickSetting(names, dialog, on) { r ->
            when (r) {
                JarvisAccessibilityService.TileResult.DONE ->
                    say(if (on == null) "${f.spoken} switched" else "${f.spoken} $what")
                JarvisAccessibilityService.TileResult.ALREADY -> say("${f.spoken} is already $what")
                JarvisAccessibilityService.TileResult.NOT_FOUND -> {
                    launch(Intent(settingsAction(f)))
                    say("I couldn't find the ${f.spoken} button, opening settings")
                }
            }
        }
    }

    private fun brightness(c: Command.Brightness) {
        if (!Settings.System.canWrite(ctx)) {
            launch(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + ctx.packageName)))
            say("Please allow Jarvis to modify system settings, then ask again")
            return
        }
        val cr = ctx.contentResolver
        Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
        val cur = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS, 128)
        val target = if (c.percent != null) c.percent * 255 / 100 else cur + c.delta * 255 / 100
        val v = target.coerceIn(3, 255)
        Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, v)
        say("Brightness ${v * 100 / 255} percent")
    }

    private fun media(code: Int) {
        val am = audio()
        am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
        am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
    }

    private fun ringer(mode: Int, msg: String) {
        val am = audio()
        try {
            am.ringerMode = mode
            say(msg)
        } catch (e: SecurityException) {
            // Silent mode needs Do-Not-Disturb access on newer Android
            am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            say("Silent mode needs Do Not Disturb access, I set vibrate instead")
        }
    }

    private fun answerCall() {
        if (!granted(Manifest.permission.ANSWER_PHONE_CALLS)) { say("Please allow the answer calls permission"); return }
        val tm = ctx.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        @Suppress("DEPRECATION", "MissingPermission")
        tm.acceptRingingCall()
    }

    private fun endCall() {
        if (Build.VERSION.SDK_INT < 28) { say("This needs Android 9 or newer"); return }
        if (!granted(Manifest.permission.ANSWER_PHONE_CALLS)) { say("Please allow the answer calls permission"); return }
        val tm = ctx.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        @Suppress("DEPRECATION", "MissingPermission")
        tm.endCall()
    }

    private fun youtube(q: String) {
        val i = Intent(Intent.ACTION_SEARCH).setPackage("com.google.android.youtube").putExtra(SearchManager.QUERY, q)
        try {
            launch(i)
        } catch (e: Exception) {
            launch(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(q))))
        }
        say("Searching YouTube for $q")
    }

    private fun webSearch(q: String) {
        try {
            launch(Intent(Intent.ACTION_WEB_SEARCH).putExtra(SearchManager.QUERY, q))
        } catch (e: Exception) {
            launch(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(q))))
        }
        say("Searching for $q")
    }

    private fun readNotifications() {
        if (JarvisNotificationListener.instance == null) {
            launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            say("Please allow notification access for Jarvis")
            return
        }
        val items = JarvisNotificationListener.latest(5)
        if (items.isEmpty()) { say("You have no new notifications"); return }
        val sb = StringBuilder("You have ${items.size} notifications. ")
        for (it in items) {
            sb.append(it.app).append(". ")
            if (it.title.isNotBlank()) sb.append(it.title).append(": ")
            sb.append(it.text).append(". ")
        }
        say(sb.toString())
    }

    private fun timeText(h: Int, m: Int): String {
        val hh = if (h % 12 == 0) 12 else h % 12
        return "%d:%02d %s".format(hh, m, if (h < 12) "AM" else "PM")
    }

    private fun durationText(s: Int): String = when {
        s % 3600 == 0 -> "${s / 3600} hour" + if (s / 3600 > 1) "s" else ""
        s % 60 == 0 -> "${s / 60} minute" + if (s / 60 > 1) "s" else ""
        else -> "$s seconds"
    }
}
