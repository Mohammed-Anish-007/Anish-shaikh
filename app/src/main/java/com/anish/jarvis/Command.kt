package com.anish.jarvis

sealed class Command {
    enum class Channel { SMS, WHATSAPP }

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

    data object Send : Command()
    data object Back : Command()
    data object Home : Command()
    data object Recents : Command()
    data object Notifications : Command()
    data object ScrollDown : Command()
    data object ScrollUp : Command()
    data object TellTime : Command()
    data object TorchOn : Command()
    data object TorchOff : Command()
    data object VolumeUp : Command()
    data object VolumeDown : Command()
    data object StopListening : Command()

    data class Unknown(val text: String, val reason: String = "") : Command()
}
