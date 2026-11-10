package com.anish.jarvis

sealed class Command {
    enum class Channel { SMS, WHATSAPP }

    /** Phone features that are switched from Quick Settings. */
    enum class Feature(val spoken: String) {
        WIFI("Wi-Fi"), BLUETOOTH("Bluetooth"), MOBILE_DATA("Mobile data"), AIRPLANE("Airplane mode"),
        HOTSPOT("Hotspot"), LOCATION("Location"), DND("Do not disturb"), AUTO_ROTATE("Auto rotate"),
        BATTERY_SAVER("Battery saver")
    }

    enum class Dir { UP, DOWN, LEFT, RIGHT }

    data class OpenApp(val name: String) : Command()
    data class Call(val who: String) : Command()

    /** [splitNeeded] = recipient and text were not separated; the executor finds the contact prefix. */
    data class Message(
        val channel: Channel,
        val who: String,
        val text: String,
        val splitNeeded: Boolean = false
    ) : Command()

    data class Alarm(val hour: Int, val minute: Int) : Command()
    data class Timer(val seconds: Int) : Command()
    data class Type(val text: String) : Command()

    /** on = null means "flip it". */
    data class Toggle(val feature: Feature, val on: Boolean?) : Command()
    data class OpenSettings(val feature: Feature) : Command()

    /** Either an absolute [percent] or a relative [delta] in percent. */
    data class Brightness(val percent: Int?, val delta: Int = 0) : Command()

    /** Tap (or long-press) any button / text visible on screen. */
    data class Click(val target: String, val long: Boolean = false) : Command()
    data class Swipe(val dir: Dir) : Command()
    data class YoutubeSearch(val query: String) : Command()
    data class WebSearch(val query: String) : Command()

    data object Send : Command()
    data object Back : Command()
    data object Home : Command()
    data object Recents : Command()
    data object Notifications : Command()
    data object ReadNotifications : Command()
    data object ClearNotifications : Command()
    data object ScrollDown : Command()
    data object ScrollUp : Command()
    data object TellTime : Command()
    data object BatteryLevel : Command()
    data object TorchOn : Command()
    data object TorchOff : Command()
    data object VolumeUp : Command()
    data object VolumeDown : Command()
    data object Mute : Command()
    data object Unmute : Command()
    data object VibrateMode : Command()
    data object SilentMode : Command()
    data object RingMode : Command()
    data object MediaPlay : Command()
    data object MediaPause : Command()
    data object MediaNext : Command()
    data object MediaPrevious : Command()
    data object AnswerCall : Command()
    data object EndCall : Command()
    data object Screenshot : Command()
    data object LockScreen : Command()
    data object PowerMenu : Command()
    data object StopListening : Command()

    data class Unknown(val text: String, val reason: String = "") : Command()
}
