package com.anish.jarvis

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Keeps the last few notifications IN MEMORY so Jarvis can read them aloud when you ask.
 * Nothing is written to disk or sent anywhere.
 */
class JarvisNotificationListener : NotificationListenerService() {

    data class Item(val app: String, val title: String, val text: String)

    companion object {
        @Volatile var instance: JarvisNotificationListener? = null
        private val recent = ArrayDeque<Item>()

        /** Newest first. */
        fun latest(n: Int): List<Item> = synchronized(recent) { recent.reversed().take(n) }
        fun clear() = synchronized(recent) { recent.clear() }
    }

    override fun onListenerConnected() { instance = this }
    override fun onListenerDisconnected() { instance = null }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.isOngoing || sbn.packageName == packageName) return
        val n = sbn.notification ?: return
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val ex = n.extras ?: return
        val title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (ex.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: ex.getCharSequence(Notification.EXTRA_TEXT))
            ?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return
        val app = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(sbn.packageName, 0)).toString()
        } catch (e: Exception) { sbn.packageName }
        val item = Item(app, title, text.take(200))
        synchronized(recent) {
            recent.remove(item)
            recent.addLast(item)
            while (recent.size > 30) recent.removeFirst()
        }
    }
}
