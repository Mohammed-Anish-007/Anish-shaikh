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

    @Test fun numbers() {
        assertEquals("9876543210", NumberWords.phoneNumber("nine eight seven six five four three two one zero"))
        assertEquals(null, NumberWords.phoneNumber("rahul"))
    }
}
