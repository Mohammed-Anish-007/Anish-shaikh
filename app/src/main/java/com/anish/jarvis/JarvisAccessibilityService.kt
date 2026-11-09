package com.anish.jarvis

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Lets Jarvis act on the screen: type into the focused text box, press "Send",
 * go back / home, scroll. It only acts when YOU give a verified voice command,
 * and it does not read or store screen content otherwise.
 */
class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile var instance: JarvisAccessibilityService? = null
        @Volatile private var pendingSendPkg: String? = null
        @Volatile private var pendingSendUntil = 0L

        /** After opening a WhatsApp chat with pre-filled text, press Send automatically. */
        fun armAutoSend(pkg: String) {
            pendingSendPkg = pkg
            pendingSendUntil = SystemClock.elapsedRealtime() + 15_000
        }

        private val SEND_IDS = listOf(
            "com.whatsapp:id/send", "com.whatsapp.w4b:id/send",
            "com.google.android.apps.messaging:id/send_message_button_icon",
            "com.google.android.apps.messaging:id/send_message_button_container",
            "com.samsung.android.messaging:id/send_button1",
            "org.telegram.messenger:id/send_button"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        JarvisLog.add("Accessibility connected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = pendingSendPkg ?: return
        if (SystemClock.elapsedRealtime() > pendingSendUntil) { pendingSendPkg = null; return }
        if (event?.packageName?.toString() != pkg) return
        if (clickSend()) {
            pendingSendPkg = null
            JarvisLog.add("Pressed Send in $pkg")
        }
    }

    fun clickSend(): Boolean {
        val root = rootInActiveWindow ?: return false
        for (id in SEND_IDS) {
            root.findAccessibilityNodeInfosByViewId(id)?.forEach { if (clickNode(it)) return true }
        }
        val node = findNode(root) { n ->
            val d = n.contentDescription?.toString()?.lowercase().orEmpty()
            val t = n.text?.toString()?.lowercase().orEmpty()
            n.isVisibleToUser && (d == "send" || d.startsWith("send ") || d == "भेजें" || t == "send" || t == "भेजें")
        } ?: return false
        return clickNode(node)
    }

    fun typeText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val node = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)?.takeIf { it.isEditable }
            ?: findNode(root) { it.isEditable && it.isVisibleToUser }
            ?: return false
        val existing = if (node.isShowingHintText) "" else node.text?.toString().orEmpty()
        val newText = if (existing.isBlank()) text else "$existing $text"
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    fun scroll(forward: Boolean): Boolean {
        val root = rootInActiveWindow ?: return false
        val node = findNode(root) { it.isScrollable && it.isVisibleToUser } ?: return false
        return node.performAction(
            if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        )
    }

    fun global(action: Int) = performGlobalAction(action)

    private fun clickNode(n: AccessibilityNodeInfo): Boolean {
        var c: AccessibilityNodeInfo? = n
        var depth = 0
        while (c != null && depth < 6) {
            if (c.isClickable && c.isEnabled) return c.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            c = c.parent
            depth++
        }
        return false
    }

    private fun findNode(root: AccessibilityNodeInfo, pred: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var seen = 0
        while (queue.isNotEmpty() && seen < 3000) {
            val n = queue.removeFirst()
            seen++
            if (pred(n)) return n
            for (i in 0 until n.childCount) n.getChild(i)?.let { queue.add(it) }
        }
        return null
    }
}
