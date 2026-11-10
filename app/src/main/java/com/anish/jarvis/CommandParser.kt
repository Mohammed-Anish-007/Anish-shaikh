package com.anish.jarvis

import com.anish.jarvis.Command.Channel

/**
 * Turns recognised speech (English, Hinglish or Hindi/Devanagari) into a [Command].
 * Pure Kotlin – see CommandParserTest.
 */
object CommandParser {

    private fun set(vararg w: String): Set<String> = w.map { TextUtil.normalize(it) }.toSet()

    private val POLITE = set("please", "plz", "pls", "kripya", "कृपया", "jara", "zara", "जरा", "jarvis")

    private val STOP = set(
        "stop listening", "go to sleep", "sleep", "so jao", "सो जाओ", "band ho jao", "बंद हो जाओ",
        "stop jarvis", "shut down", "goodbye", "bye"
    )
    private val TORCH = set("torch", "flashlight", "flash light", "flash", "टॉर्च", "टार्च")
    private val OFF = set("off", "band", "बंद", "bujhao", "बुझाओ", "bujha do", "बुझा दो")
    private val ALARM = set("alarm", "अलार्म", "wake me up", "wake me", "jagana", "jaga dena", "जगा देना", "उठा देना")
    private val TIMER = set("timer", "टाइमर")
    private val TIME_Q = set(
        "what time", "what is the time", "whats the time", "tell me the time", "time kya", "kitne baje",
        "time batao", "टाइम क्या", "समय क्या", "कितने बजे", "टाइम बताओ", "समय बताओ", "current time"
    )
    private val VOLUME = set("volume", "वॉल्यूम", "आवाज", "awaaz", "awaz", "sound")
    private val DOWN = set("down", "kam", "कम", "low", "lower", "ghatao", "घटाओ", "decrease", "reduce", "dheere", "धीरे")

    private val SEND = set(
        "send", "send it", "send karo", "send kar do", "send message", "bhej do", "bhejo",
        "भेज दो", "भेजो", "सेंड करो", "सेंड कर दो", "सेंड", "press send", "hit send"
    )
    private val BACK = set("back", "go back", "wapas", "wapas jao", "peeche", "piche", "पीछे", "वापस", "वापस जाओ", "पीछे जाओ", "back jao", "बैक")
    private val HOME = set("home", "go home", "home screen", "home jao", "होम", "होम जाओ", "होम स्क्रीन")
    private val RECENTS = set("recent apps", "recents", "recent", "show recent apps", "रीसेंट")
    private val NOTIFS = set("notifications", "notification", "show notifications", "open notifications", "नोटिफिकेशन")
    private val SCROLL_DOWN = set("scroll down", "scroll", "neeche", "niche", "neeche karo", "नीचे", "नीचे करो", "scroll karo")
    private val SCROLL_UP = set("scroll up", "upar", "upar karo", "ऊपर", "ऊपर करो")

    private val WHATSAPP = set("whatsapp", "whats app", "व्हाट्सएप", "व्हाट्सऐप", "वॉट्सऐप", "वाट्सएप")
    private val MESSAGE_WORDS = set("message", "messages", "msg", "sms", "text", "मैसेज", "संदेश", "मेसेज", "ko bolo", "को बोलो")
    private val OPEN_WORDS = set(
        "open", "launch", "start", "run", "kholo", "khol", "khol do", "chalao", "chala do", "chalu karo",
        "ओपन", "खोलो", "खोल", "खोल दो", "चलाओ", "चला दो", "चालू करो"
    )
    private val CALL_WORDS = set("call", "dial", "phone", "ring", "कॉल", "काल", "फोन")
    private val TYPE_START = set("type", "write", "likho", "लिखो", "टाइप")

    private val SEPARATORS = set("saying", "that", "ki", "kee", "bolo", "कि", "की", "बोलो")
    private val MSG_FILLERS = set(
        "send", "a", "an", "the", "message", "messages", "msg", "sms", "text", "on", "par", "pe", "via",
        "bhejo", "bhej", "karo", "kar", "do", "dena", "my", "mera", "meri", "mere", "to", "ko", "bolo",
        "मैसेज", "मेसेज", "संदेश", "पर", "पे", "भेजो", "भेज", "करो", "कर", "दो", "देना", "मेरा", "मेरी", "मेरे", "को", "बोलो"
    ) + WHATSAPP + OPEN_WORDS
    private val TEXT_LEAD = set(
        "message", "msg", "sms", "text", "on", "par", "pe", "bhejo", "bhej", "send", "karo", "kar",
        "ki", "kee", "that", "saying", "bolo", "मैसेज", "पर", "पे", "भेजो", "भेज", "करो", "कर", "कि", "की", "बोलो"
    ) + WHATSAPP
    private val TEXT_TRAIL = set(
        "bhejo", "bhej", "do", "send", "karo", "kar", "dena", "de", "please",
        "भेजो", "भेज", "दो", "करो", "कर", "देना", "दे"
    )
    private val CALL_FILLERS = set(
        "call", "dial", "phone", "ring", "make", "a", "an", "the", "to", "up", "my", "mera", "meri", "mere",
        "karo", "kar", "do", "lagao", "laga", "milao", "ko", "par", "pe",
        "कॉल", "काल", "फोन", "करो", "कर", "दो", "लगाओ", "लगा", "मिलाओ", "को", "पर", "मेरा", "मेरी", "मेरे"
    )
    private val OPEN_FILLERS = OPEN_WORDS + set(
        "the", "app", "application", "karo", "kar", "do", "please", "up", "my",
        "एप", "ऐप", "करो", "कर", "दो", "मेरा"
    )

    // ------------------------------------------------------------- "full control" vocabulary
    private val ANSWER = set(
        "answer", "answer call", "answer the call", "pick up", "pick up the call", "receive call",
        "call uthao", "call utha lo", "phone uthao", "phone utha lo", "call receive karo",
        "कॉल उठाओ", "फोन उठाओ", "कॉल उठा लो", "फोन उठा लो"
    )
    private val END_CALL = set(
        "reject", "reject call", "reject the call", "end call", "end the call", "cut call", "cut the call",
        "hang up", "disconnect", "call cut karo", "call kaato", "call kato", "call kaat do", "phone kaato",
        "phone kaat do", "phone rakho", "call band karo", "कॉल काटो", "फोन काटो", "कॉल काट दो", "फोन काट दो"
    )
    private val NOTIF_WORDS = set("notification", "notifications", "नोटिफिकेशन", "message", "messages", "मैसेज")
    private val READ_WORDS = set("read", "padho", "padh do", "sunao", "suna do", "batao", "पढो", "सुनाओ", "बताओ", "kya aaya", "क्या आया")
    private val CLEAR_WORDS = set("clear", "saaf", "hatao", "delete", "हटाओ", "साफ", "क्लियर")
    private val SCREENSHOT = set("screenshot", "screen shot", "स्क्रीनशॉट")
    private val LOCK = set(
        "lock", "lock phone", "lock the phone", "phone lock", "lock screen", "screen lock", "screen off",
        "screen band", "लॉक", "स्क्रीन बंद"
    )
    private val POWER = set("power menu", "power off", "switch off", "restart", "reboot", "phone off", "पावर")
    private val MUTE = set("mute", "mute karo", "mute kar do", "awaaz band karo", "awaz band karo", "sound off", "म्यूट", "आवाज बंद करो")
    private val UNMUTE = set("unmute", "unmute karo", "sound on", "awaaz on karo", "awaz on karo", "अनम्यूट")
    private val SILENT = set("silent", "silent mode", "साइलेंट")
    private val VIBRATE = set("vibrate", "vibrate mode", "vibration mode", "वाइब्रेट")
    private val RING = set("ring mode", "ringer mode", "normal mode", "general mode", "ringer on", "रिंग मोड")
    private val BRIGHT = set("brightness", "ब्राइटनेस", "roshni", "रोशनी", "screen light")
    private val FULL = set("full", "max", "maximum", "poori", "puri", "पूरी", "फुल", "highest")
    private val MIN = set("minimum", "min", "lowest", "sabse kam", "सबसे कम")
    private val HALF_WORD = set("half", "aadhi", "आधी")
    private val BATTERY = set("battery", "बैटरी", "charging", "charge")
    private val SETTINGS_WORDS = set("settings", "setting", "सेटिंग", "सेटिंग्स")
    private val TOGGLE = set("toggle", "switch", "badlo", "बदलो")
    private val ON = set("on", "chalu", "चालू", "enable", "turn on", "start", "jalao", "जलाओ", "ऑन")
    private val FEATURES: List<Pair<Command.Feature, Set<String>>> = listOf(
        Command.Feature.AIRPLANE to set("airplane mode", "aeroplane mode", "airplane", "aeroplane", "flight mode", "hawai jahaz", "हवाई जहाज", "एयरप्लेन"),
        Command.Feature.BATTERY_SAVER to set("battery saver", "power saving", "power saver", "बैटरी सेवर"),
        Command.Feature.WIFI to set("wifi", "wi fi", "वाईफाई", "वाई फाई"),
        Command.Feature.BLUETOOTH to set("bluetooth", "ब्लूटूथ"),
        Command.Feature.HOTSPOT to set("hotspot", "hot spot", "हॉटस्पॉट"),
        Command.Feature.LOCATION to set("location", "gps", "लोकेशन"),
        Command.Feature.DND to set("do not disturb", "dnd"),
        Command.Feature.AUTO_ROTATE to set("auto rotate", "auto rotation", "rotation", "rotate"),
        Command.Feature.MOBILE_DATA to set("mobile data", "data", "internet", "net", "मोबाइल डेटा", "डेटा", "इंटरनेट")
    )
    private val PLAY = set(
        "play", "play music", "resume", "resume music", "music chalao", "gaana chalao", "gana chalao", "song chalao",
        "music play karo", "गाना चलाओ", "म्यूजिक चलाओ"
    )
    private val PAUSE = set(
        "pause", "pause music", "stop music", "music band karo", "gaana roko", "gana roko", "gaana band karo",
        "gana band karo", "music roko", "गाना रोको", "गाना बंद करो"
    )
    private val NEXT = set("next", "next song", "next track", "skip", "skip song", "agla gaana", "agla gana", "अगला गाना")
    private val PREV = set("previous", "previous song", "previous track", "pichla gaana", "pichla gana", "पिछला गाना")
    private val SWIPE_UP = set("swipe up", "next reel", "next video", "next short", "agla video", "agli reel", "upar swipe karo", "अगला वीडियो", "अगली रील")
    private val SWIPE_DOWN = set("swipe down", "previous reel", "previous video", "pichla video", "pichli reel", "neeche swipe karo", "पिछला वीडियो", "पिछली रील")
    private val SWIPE_LEFT = set("swipe left", "left swipe karo", "next photo", "agli photo", "अगली फोटो")
    private val SWIPE_RIGHT = set("swipe right", "right swipe karo", "previous photo", "pichli photo", "पिछली फोटो")
    private val CLICK_START = set("click", "tap", "press", "select", "choose", "क्लिक", "टैप")
    private val CLICK_END = set("dabao", "daba do", "दबाओ", "दबा दो")
    private val CLICK_FILLERS = set("on", "the", "button", "pe", "par", "per", "पर", "पे", "ko", "को", "option", "बटन")
    private val YOUTUBE = set("youtube", "you tube", "यूट्यूब")
    private val SEARCHY = set(
        "search", "dhundo", "dhoondo", "khojo", "सर्च", "खोजो", "ढूंढो",
        "play", "chalao", "lagao", "dikhao", "bajao", "sunao", "चलाओ", "लगाओ", "दिखाओ", "बजाओ", "सुनाओ"
    )
    private val WEB_SEARCH = set("search", "google", "सर्च", "गूगल", "dhundo", "dhoondo", "khojo", "खोजो", "ढूंढो")
    private val SONG = set("song", "songs", "gaana", "gana", "gaane", "gane", "गाना", "गाने")
    private val SEARCH_FILLERS = YOUTUBE + SEARCHY + WEB_SEARCH + OPEN_WORDS + set(
        "on", "pe", "par", "per", "me", "mein", "पर", "पे", "में", "karo", "kar", "do", "करो", "कर", "दो",
        "for", "the", "a", "video", "videos", "वीडियो", "about", "ke", "baare", "बारे", "के", "and", "aur"
    )

    private fun has(t: String, phrases: Set<String>): Boolean {
        val p = " $t "
        return phrases.any { p.contains(" $it ") }
    }

    fun parse(input: String): Command {
        val toksAll = TextUtil.tokens(input).filter { it !in POLITE }
        val t = toksAll.joinToString(" ")
        if (t.isBlank()) return Command.Unknown(input)
        val n = toksAll.size

        if (t in STOP) return Command.StopListening
        if (t in ANSWER) return Command.AnswerCall
        if (t in END_CALL) return Command.EndCall
        if (has(t, TORCH)) return if (has(t, OFF)) Command.TorchOff else Command.TorchOn
        if (has(t, TIMER)) {
            val s = parseDuration(toksAll) ?: return Command.Unknown(input, "timer length not understood")
            return Command.Timer(s)
        }
        if (has(t, ALARM)) {
            val hm = parseTime(toksAll) ?: return Command.Unknown(input, "alarm time not understood")
            return Command.Alarm(hm.first, hm.second)
        }
        if (n <= 5 && has(t, NOTIF_WORDS)) {
            if (has(t, CLEAR_WORDS)) return Command.ClearNotifications
            if (has(t, READ_WORDS)) return Command.ReadNotifications
        }
        if (has(t, MUTE)) return Command.Mute
        if (has(t, UNMUTE)) return Command.Unmute
        if (n <= 5 && has(t, SILENT)) return Command.SilentMode
        if (n <= 5 && has(t, VIBRATE)) return Command.VibrateMode
        if (has(t, RING)) return Command.RingMode
        if (has(t, BRIGHT)) return parseBrightness(toksAll, t)

        val isMsg = has(t, MESSAGE_WORDS)
        val feature = FEATURES.firstOrNull { has(t, it.second) }?.first
        if (feature != null && n <= 7 && !isMsg) {
            if (has(t, SETTINGS_WORDS)) return Command.OpenSettings(feature)
            // Only switch something when the user clearly says on / off / toggle.
            when {
                has(t, OFF) -> return Command.Toggle(feature, false)
                has(t, ON) -> return Command.Toggle(feature, true)
                has(t, TOGGLE) -> return Command.Toggle(feature, null)
            }
        }
        if (n <= 6 && has(t, BATTERY)) return Command.BatteryLevel
        if (has(t, SCREENSHOT)) return Command.Screenshot
        if (n <= 5 && has(t, LOCK)) return Command.LockScreen
        if (n <= 5 && has(t, POWER)) return Command.PowerMenu

        if (has(t, TIME_Q)) return Command.TellTime
        if (has(t, VOLUME)) return if (has(t, DOWN)) Command.VolumeDown else Command.VolumeUp

        when (t) {
            in SEND -> return Command.Send
            in BACK -> return Command.Back
            in HOME -> return Command.Home
            in RECENTS -> return Command.Recents
            in NOTIFS -> return Command.Notifications
            in SCROLL_DOWN -> return Command.ScrollDown
            in SCROLL_UP -> return Command.ScrollUp
            in PLAY -> return Command.MediaPlay
            in PAUSE -> return Command.MediaPause
            in NEXT -> return Command.MediaNext
            in PREV -> return Command.MediaPrevious
            in SWIPE_UP -> return Command.Swipe(Command.Dir.UP)
            in SWIPE_DOWN -> return Command.Swipe(Command.Dir.DOWN)
            in SWIPE_LEFT -> return Command.Swipe(Command.Dir.LEFT)
            in SWIPE_RIGHT -> return Command.Swipe(Command.Dir.RIGHT)
        }

        parseType(toksAll)?.let { return it }
        parseClick(toksAll)?.let { return it }
        if (!isMsg) parseSearch(toksAll, t)?.let { return it }

        val isWa = has(t, WHATSAPP)
        val isOpen = has(t, OPEN_WORDS)

        if (isMsg || (isWa && !isOpen)) parseMessage(toksAll, isWa)?.let { return it }
        if (isOpen) parseOpen(toksAll)?.let { return it }
        if (has(t, CALL_WORDS)) parseCall(toksAll)?.let { return it }
        return Command.Unknown(input)
    }

    private fun parseBrightness(toks: List<String>, t: String): Command {
        val num = NumberWords.toDigits(toks).firstNotNullOfOrNull { it.toIntOrNull() }
        return when {
            num != null -> Command.Brightness(num.coerceIn(0, 100))
            has(t, FULL) -> Command.Brightness(100)
            has(t, MIN) -> Command.Brightness(5)
            has(t, HALF_WORD) -> Command.Brightness(50)
            has(t, DOWN) -> Command.Brightness(null, -25)
            else -> Command.Brightness(null, 25)
        }
    }

    private fun parseClick(toks: List<String>): Command? {
        if (toks.size < 2) return null
        var long = false
        var body: List<String>? = null
        if (toks[0] == "long" && toks[1] == "press") {
            long = true; body = toks.drop(2)
        } else if (toks[0] in CLICK_START) {
            body = toks.drop(1)
        } else if (toks.last() in CLICK_END) {
            body = toks.dropLast(1)
        } else if (toks.size >= 3 && toks[toks.size - 2] in CLICK_START && toks.last() in set("karo", "करो", "kar", "do")) {
            body = toks.dropLast(2)
        } else if (toks.size >= 4 && toks[toks.size - 3] in CLICK_START && toks.takeLast(2) == listOf("kar", "do")) {
            body = toks.dropLast(3)
        }
        val target = body?.filter { it !in CLICK_FILLERS }.orEmpty()
        if (target.isEmpty()) return null
        return Command.Click(target.joinToString(" "), long)
    }

    private fun parseSearch(toks: List<String>, t: String): Command? {
        val q = toks.filter { it !in SEARCH_FILLERS }
        val isYt = has(t, YOUTUBE)
        val searchy = has(t, SEARCHY)
        if (isYt && searchy) return if (q.isEmpty()) null else Command.YoutubeSearch(q.joinToString(" "))
        if (has(t, SONG) && searchy && q.isNotEmpty()) return Command.YoutubeSearch(q.joinToString(" "))
        if (toks.first() in set("play", "bajao", "chalao") && q.isNotEmpty()) return Command.YoutubeSearch(q.joinToString(" "))
        if (has(t, WEB_SEARCH) && q.isNotEmpty()) return Command.WebSearch(q.joinToString(" "))
        return null
    }

    private fun parseType(toks: List<String>): Command? {
        if (toks.size < 2) return null
        val first = toks.first()
        val last = toks.last()
        var body: List<String>? = null
        if (first in TYPE_START) {
            body = toks.drop(1)
            if (body.firstOrNull() in set("karo", "करो", "kar", "कर")) body = body.drop(1)
        } else if (last in set("likho", "लिखो", "type")) {
            body = toks.dropLast(1)
        } else if (toks.size >= 3 && toks[toks.size - 2] in set("type", "टाइप") && last in set("karo", "करो", "kar do")) {
            body = toks.dropLast(2)
        }
        if (body.isNullOrEmpty()) return null
        return Command.Type(body.joinToString(" "))
    }

    private fun parseMessage(toks: List<String>, wa: Boolean): Command? {
        val sepIdx = toks.indexOfFirst { it in SEPARATORS }
        val head = if (sepIdx >= 0) toks.subList(0, sepIdx) else toks
        var text: List<String> = if (sepIdx >= 0) toks.subList(sepIdx + 1, toks.size) else emptyList()
        // "rahul ko bolo ki ..." – "bolo" is the separator but "ko" is before it
        val koIdx = head.indexOfFirst { it == "ko" || it == "को" }
        val toIdx = head.indexOf("to")
        var who: List<String>
        var split = false
        when {
            koIdx > 0 -> {
                who = head.subList(0, koIdx)
                if (text.isEmpty()) text = head.subList(koIdx + 1, head.size)
            }
            toIdx >= 0 -> {
                who = head.subList(toIdx + 1, head.size)
                if (text.isEmpty()) split = true
            }
            else -> {
                who = head
                if (text.isEmpty()) split = true
            }
        }
        who = who.filter { it !in MSG_FILLERS }
        text = trimText(text)
        if (who.isEmpty()) return null
        return Command.Message(
            if (wa) Channel.WHATSAPP else Channel.SMS,
            who.joinToString(" "),
            text.joinToString(" "),
            split
        )
    }

    private fun trimText(t: List<String>): List<String> {
        var s = 0
        var e = t.size
        while (s < e && t[s] in TEXT_LEAD) s++
        while (e > s && t[e - 1] in TEXT_TRAIL) e--
        return t.subList(s, e)
    }

    private fun parseCall(toks: List<String>): Command? {
        val who = toks.filter { it !in CALL_FILLERS }
        if (who.isEmpty()) return null
        return Command.Call(who.joinToString(" "))
    }

    private fun parseOpen(toks: List<String>): Command? {
        val name = toks.filter { it !in OPEN_FILLERS }
        if (name.isEmpty()) return null
        return Command.OpenApp(name.joinToString(" "))
    }

    // ---------------------------------------------------------------- time parsing

    private val PM = set("pm", "evening", "night", "tonight", "afternoon", "sham", "shaam", "raat", "dopahar", "शाम", "रात", "दोपहर")
    private val NIGHT = set("night", "tonight", "raat", "रात")
    private val AM = set("am", "morning", "subah", "savere", "सुबह", "सवेरे")
    private val HALF = set("saade", "sade", "साढे")
    private val QUARTER_PAST = set("sawa", "सवा")
    private val QUARTER_TO = set("paune", "पौने")

    fun parseTime(rawToks: List<String>): Pair<Int, Int>? {
        val joined = " " + rawToks.joinToString(" ") + " "
        val isPm = PM.any { joined.contains(" $it ") } || joined.contains(" p m ")
        val isAm = AM.any { joined.contains(" $it ") } || joined.contains(" a m ")
        val isNight = NIGHT.any { joined.contains(" $it ") }

        val toks = NumberWords.toDigits(rawToks.filter { it != "oh" && it != "o" })
        val nums = ArrayList<Int>()
        for (tk in toks) {
            val c = Regex("^(\\d{1,2}):(\\d{2})$").find(tk)
            if (c != null) { nums += c.groupValues[1].toInt(); nums += c.groupValues[2].toInt(); continue }
            tk.toIntOrNull()?.let { nums += it }
        }
        val j = " " + toks.joinToString(" ") + " "
        var h: Int
        var m = 0
        when {
            nums.isNotEmpty() -> {
                h = nums[0]
                if (nums.size > 1 && nums[1] in 0..59) m = nums[1]
            }
            j.contains(" dedh ") || j.contains(" डेढ ") -> { h = 1; m = 30 }
            j.contains(" dhai ") || j.contains(" ढाई ") -> { h = 2; m = 30 }
            else -> return null
        }
        if (j.contains(" half past ") || HALF.any { j.contains(" $it ") }) m = 30
        if (j.contains(" quarter past ") || QUARTER_PAST.any { j.contains(" $it ") }) m = 15
        if (j.contains(" quarter to ") || QUARTER_TO.any { j.contains(" $it ") }) { h -= 1; m = 45; if (h < 0) h = 23 }
        if (h !in 0..23) return null

        if (isPm) {
            if (isNight && h == 12) h = 0
            else if (isNight && h <= 4) { /* raat 2 baje = 2 AM */ }
            else if (h in 1..11) h += 12
        } else if (isAm && h == 12) {
            h = 0
        }
        return h to m
    }

    fun parseDuration(rawToks: List<String>): Int? {
        val toks = NumberWords.toDigits(rawToks)
        val j = " " + toks.joinToString(" ") + " "
        var total = 0
        var found = false
        for (i in toks.indices) {
            val n = toks[i].toIntOrNull() ?: continue
            val unit = toks.getOrNull(i + 1) ?: ""
            val mult = when {
                unit.startsWith("sec") || unit == "सेकंड" || unit == "सेकेंड" -> 1
                unit.startsWith("hour") || unit.startsWith("ghant") || unit.startsWith("घंट") -> 3600
                else -> 60
            }
            total += n * mult
            found = true
        }
        if (!found && (j.contains(" half an hour ") || j.contains(" aadha ghanta ") || j.contains(" आधा घंटा "))) return 1800
        return if (found && total > 0) total else null
    }
}
