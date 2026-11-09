package com.anish.jarvis

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.telephony.SmsManager
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
