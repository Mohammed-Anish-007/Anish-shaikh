package com.anish.jarvis

import com.anish.jarvis.Command.Channel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandParserTest {
    private fun p(s: String) = CommandParser.parse(s)

    @Test fun openApps() {
        assertEquals(Command.OpenApp("whatsapp"), p("open whatsapp"))
        assertEquals(Command.OpenApp("whatsapp"), p("WhatsApp kholo"))
        assertEquals(Command.OpenApp("youtube"), p("please open the youtube app"))
        assertEquals(Command.OpenApp("यूट्यूब"), p("यूट्यूब खोलो"))
        assertEquals(Command.OpenApp("messages"), p("open messages"))
        assertEquals(Command.OpenApp("phone"), p("open phone"))
    }

    @Test fun calls() {
        assertEquals(Command.Call("mummy"), p("call mummy"))
        assertEquals(Command.Call("rahul"), p("rahul ko call karo"))
        assertEquals(Command.Call("rahul"), p("Rahul ko phone lagao"))
        assertEquals(Command.Call("राहुल"), p("राहुल को कॉल करो"))
    }

    @Test fun messages() {
        assertEquals(Command.Message(Channel.SMS, "rahul", "i am coming"), p("send a message to rahul saying i am coming"))
        assertEquals(Command.Message(Channel.SMS, "rahul", "main aa raha hoon"), p("rahul ko message bhejo ki main aa raha hoon"))
        assertEquals(Command.Message(Channel.SMS, "rahul", "main aa raha hoon"), p("rahul ko bolo ki main aa raha hoon"))
        assertEquals(Command.Message(Channel.WHATSAPP, "mummy", "dinner ready"), p("send whatsapp to mummy that dinner ready"))
        assertEquals(Command.Message(Channel.WHATSAPP, "mummy", "", true), p("whatsapp message to mummy"))
        assertEquals(Command.Message(Channel.SMS, "rahul hello there", "", true), p("message rahul hello there"))
    }

    @Test fun alarms() {
        assertEquals(Command.Alarm(7, 30), p("set alarm for seven thirty am"))
        assertEquals(Command.Alarm(18, 45), p("alarm at six forty five pm"))
        assertEquals(Command.Alarm(7, 0), p("subah saat baje ka alarm lagao"))
        assertEquals(Command.Alarm(6, 30), p("सुबह साढ़े छह बजे अलार्म लगाओ"))
        assertEquals(Command.Alarm(21, 0), p("raat nau baje alarm laga do"))
        assertEquals(Command.Alarm(14, 0), p("dopahar do baje alarm"))
        assertEquals(Command.Alarm(5, 15), p("wake me up at 5:15"))
    }

    @Test fun timers() {
        assertEquals(Command.Timer(300), p("set a timer for five minutes"))
        assertEquals(Command.Timer(5400), p("timer 1 hour 30 minutes"))
        assertEquals(Command.Timer(120), p("do minute ka timer"))
    }

    @Test fun uiActions() {
        assertEquals(Command.Type("hello how are you"), p("type hello how are you"))
        assertEquals(Command.Type("main aa raha hoon"), p("main aa raha hoon likho"))
        assertEquals(Command.Send, p("send"))
        assertEquals(Command.Send, p("bhej do"))
        assertEquals(Command.Back, p("go back"))
        assertEquals(Command.Home, p("home"))
        assertEquals(Command.ScrollDown, p("scroll down"))
        assertEquals(Command.TorchOn, p("torch on"))
        assertEquals(Command.TorchOff, p("flashlight band karo"))
        assertEquals(Command.TellTime, p("what time is it"))
        assertEquals(Command.TellTime, p("kitne baje hain"))
        assertEquals(Command.VolumeDown, p("volume kam karo"))
        assertEquals(Command.StopListening, p("go to sleep"))
    }

    @Test fun wakeWord() {
        val w = listOf("jarvis", "जार्विस")
        assertEquals(WakeWord.Result(true, "open youtube"), WakeWord.split("Hey Jarvis, open YouTube", w))
        assertEquals(WakeWord.Result(true, ""), WakeWord.split("jarvis", w))
        assertEquals(WakeWord.Result(false, "open youtube"), WakeWord.split("open youtube", w))
        assertEquals(WakeWord.Result(true, "व्हाट्सएप खोलो"), WakeWord.split("जार्विस व्हाट्सएप खोलो", w))
    }

    @Test fun phoneticMatching() {
        assertEquals(TextUtil.phonetic("Rahul"), TextUtil.phonetic("राहुल"))
        assertTrue(TextUtil.nameScore("व्हाट्सएप", "WhatsApp") >= AppFinder.MIN_SCORE)
        assertTrue(TextUtil.nameScore("यूट्यूब", "YouTube") >= AppFinder.MIN_SCORE)
        assertTrue(TextUtil.nameScore("rahul", "Rahul Sharma") >= ContactFinder.MIN_SCORE)
        assertTrue(TextUtil.nameScore("rahul", "Priya") < ContactFinder.MIN_SCORE)
        assertEquals(0, TextUtil.bestMatch(listOf("Mummy", "Papa", "Rahul"), "mummy")!!.first)
    }

    @Test fun fullControlToggles() {
        assertEquals(Command.Toggle(Command.Feature.WIFI, true), p("wifi on karo"))
        assertEquals(Command.Toggle(Command.Feature.WIFI, false), p("turn off wi-fi"))
        assertEquals(Command.Toggle(Command.Feature.BLUETOOTH, false), p("bluetooth band karo"))
        assertEquals(Command.Toggle(Command.Feature.MOBILE_DATA, true), p("mobile data on kar do"))
        assertEquals(Command.Toggle(Command.Feature.MOBILE_DATA, false), p("internet band karo"))
        assertEquals(Command.Toggle(Command.Feature.AIRPLANE, true), p("airplane mode on"))
        assertEquals(Command.Toggle(Command.Feature.HOTSPOT, true), p("hotspot chalu karo"))
        assertEquals(Command.Toggle(Command.Feature.LOCATION, false), p("location off"))
        assertEquals(Command.Toggle(Command.Feature.BATTERY_SAVER, true), p("battery saver on karo"))
        assertEquals(Command.Toggle(Command.Feature.WIFI, false), p("वाई-फाई बंद करो"))
        assertEquals(Command.OpenSettings(Command.Feature.BLUETOOTH), p("bluetooth settings kholo"))
        // "data" inside a normal sentence must NOT switch mobile data
        assertTrue(p("rahul ko data bhejo") !is Command.Toggle)
    }

    @Test fun fullControlSystem() {
        assertEquals(Command.Brightness(100), p("brightness full karo"))
        assertEquals(Command.Brightness(50), p("brightness fifty percent"))
        assertEquals(Command.Brightness(null, -25), p("brightness kam karo"))
        assertEquals(Command.Brightness(null, 25), p("brightness badhao"))
        assertEquals(Command.Screenshot, p("screenshot lo"))
        assertEquals(Command.LockScreen, p("phone lock karo"))
        assertEquals(Command.PowerMenu, p("power menu"))
        assertEquals(Command.BatteryLevel, p("battery kitni hai"))
        assertEquals(Command.Mute, p("mute karo"))
        assertEquals(Command.SilentMode, p("phone silent karo"))
        assertEquals(Command.VibrateMode, p("vibrate mode"))
        assertEquals(Command.RingMode, p("normal mode"))
    }

    @Test fun fullControlCallsMediaNotifications() {
        assertEquals(Command.AnswerCall, p("call uthao"))
        assertEquals(Command.EndCall, p("call kaato"))
        assertEquals(Command.EndCall, p("reject call"))
        assertEquals(Command.MediaPlay, p("gaana chalao"))
        assertEquals(Command.MediaPause, p("music band karo"))
        assertEquals(Command.MediaNext, p("agla gaana"))
        assertEquals(Command.MediaPrevious, p("previous song"))
        assertEquals(Command.ReadNotifications, p("notifications padho"))
        assertEquals(Command.ReadNotifications, p("read my messages"))
        assertEquals(Command.ClearNotifications, p("clear notifications"))
        assertEquals(Command.Notifications, p("notifications"))
    }

    @Test fun fullControlScreen() {
        assertEquals(Command.Click("subscribe"), p("click on subscribe button"))
        assertEquals(Command.Click("like"), p("like button dabao"))
        assertEquals(Command.Click("settings"), p("settings pe click karo"))
        assertEquals(Command.Click("photo", long = true), p("long press photo"))
        assertEquals(Command.Swipe(Command.Dir.UP), p("next reel"))
        assertEquals(Command.Swipe(Command.Dir.LEFT), p("swipe left"))
        assertEquals(Command.YoutubeSearch("arijit singh songs"), p("youtube pe arijit singh songs chalao"))
        assertEquals(Command.YoutubeSearch("despacito"), p("play despacito"))
        assertEquals(Command.WebSearch("weather in mumbai"), p("google pe weather in mumbai search karo"))
        // plain "open youtube" / "youtube chalao" still just opens the app
        assertEquals(Command.OpenApp("youtube"), p("youtube chalao"))
        assertEquals(Command.OpenApp("google"), p("google kholo"))
    }

    @Test fun numbers() {
        assertEquals("9876543210", NumberWords.phoneNumber("nine eight seven six five four three two one zero"))
        assertEquals(null, NumberWords.phoneNumber("rahul"))
    }
}
