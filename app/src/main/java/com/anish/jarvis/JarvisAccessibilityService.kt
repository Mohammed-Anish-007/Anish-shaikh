package com.anish.jarvis

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    // ------------------------------------------------------------------ tap anything by name

    private fun label(n: AccessibilityNodeInfo): String =
        TextUtil.normalize((n.contentDescription ?: n.text ?: "").toString())

    /** Taps (or long-presses) the visible element whose text / description best matches [target]. */
    fun clickByText(target: String, long: Boolean): String? {
        val root = rootInActiveWindow ?: return null
        val want = TextUtil.normalize(target)
        var best: AccessibilityNodeInfo? = null
        var bestScore = 0.0
        var bestLabel = ""
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var seen = 0
        while (queue.isNotEmpty() && seen < 3000) {
            val n = queue.removeFirst()
            seen++
            for (i in 0 until n.childCount) n.getChild(i)?.let { queue.add(it) }
            if (!n.isVisibleToUser) continue
            val l = label(n)
            if (l.isEmpty() || l.length > 80) continue
            var score = TextUtil.nameScore(want, l)
            if (l == want) score = 1.0
            else if (" $l ".contains(" $want ")) score = maxOf(score, 0.9 - 0.002 * l.length)
            if (score > bestScore) { bestScore = score; best = n; bestLabel = l }
        }
        val node = best ?: return null
        if (bestScore < 0.7) return null
        val ok = if (long) longClickNode(node) else clickNode(node)
        return if (ok) bestLabel else null
    }

    private fun longClickNode(n: AccessibilityNodeInfo): Boolean {
        var c: AccessibilityNodeInfo? = n
        var depth = 0
        while (c != null && depth < 6) {
            if (c.isLongClickable && c.isEnabled) return c.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
            c = c.parent
            depth++
        }
        return false
    }

    // ------------------------------------------------------------------ gestures

    fun swipe(dir: Command.Dir): Boolean {
        val dm = resources.displayMetrics
        val w = dm.widthPixels.toFloat()
        val h = dm.heightPixels.toFloat()
        val pts = when (dir) {
            Command.Dir.UP -> floatArrayOf(w / 2, h * 0.75f, w / 2, h * 0.25f)
            Command.Dir.DOWN -> floatArrayOf(w / 2, h * 0.25f, w / 2, h * 0.75f)
            Command.Dir.LEFT -> floatArrayOf(w * 0.85f, h / 2, w * 0.15f, h / 2)
            Command.Dir.RIGHT -> floatArrayOf(w * 0.15f, h / 2, w * 0.85f, h / 2)
        }
        val path = Path().apply { moveTo(pts[0], pts[1]); lineTo(pts[2], pts[3]) }
        val g = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()
        return dispatchGesture(g, null, null)
    }

    // ------------------------------------------------------------------ quick settings toggles

    enum class TileResult { DONE, ALREADY, NOT_FOUND }

    private val handler = Handler(Looper.getMainLooper())

    private fun systemRoots(): List<AccessibilityNodeInfo> {
        val roots = try { windows.mapNotNull { it.root } } catch (e: Exception) { emptyList() }
        val sys = roots.filter { it.packageName?.toString() == "com.android.systemui" }
        return sys.ifEmpty { roots.ifEmpty { listOfNotNull(rootInActiveWindow) } }
    }

    private fun findTile(names: List<String>): Pair<AccessibilityNodeInfo, String>? {
        val roots = systemRoots()
        for (name in names) {
            for (r in roots) {
                val n = findNode(r) { node ->
                    if (!node.isVisibleToUser) return@findNode false
                    val l = label(node)
                    l == name || l.startsWith("$name ") || l.startsWith(name)
                }
                if (n != null) return n to name
            }
        }
        return null
    }

    /** true = on, false = off, null = unknown */
    private fun tileState(n: AccessibilityNodeInfo): Boolean? {
        var c: AccessibilityNodeInfo? = n
        var d = 0
        while (c != null && d < 3) {
            if (c.isCheckable) return c.isChecked
            if (Build.VERSION.SDK_INT >= 30) {
                val st = c.stateDescription?.toString()?.lowercase()
                if (st != null) {
                    if (st.startsWith("off") || st.contains("बंद")) return false
                    if (st.startsWith("on") || st.contains("चालू")) return true
                }
            }
            val l = " " + label(c) + " "
            if (l.contains(" off ") || l.contains(" बंद ")) return false
            if (l.contains(" on ") || l.contains(" चालू ")) return true
            c = c.parent
            d++
        }
        return null
    }

    private fun findSwitchNear(text: String): AccessibilityNodeInfo? {
        for (r in systemRoots()) {
            val labelNode = findNode(r) { it.isVisibleToUser && label(it).contains(text) } ?: continue
            if (labelNode.isCheckable) return labelNode
            var p: AccessibilityNodeInfo? = labelNode.parent
            var d = 0
            while (p != null && d < 4) {
                findNode(p) { it.isCheckable }?.let { return it }
                p = p.parent
                d++
            }
        }
        return null
    }

    private fun closeShade() {
        if (Build.VERSION.SDK_INT >= 31) performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
        else performGlobalAction(GLOBAL_ACTION_BACK)
    }

    /**
     * Opens Quick Settings, finds the tile called one of [names] and switches it.
     * [dialogLabel] handles Android 12+'s combined "Internet" tile, which opens a dialog with switches.
     */
    fun toggleQuickSetting(names: List<String>, dialogLabel: String?, on: Boolean?, cb: (TileResult) -> Unit) {
        performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        handler.postDelayed({ tileStep(names, dialogLabel, on, cb, 0) }, 900)
    }

    private fun tileStep(names: List<String>, dialogLabel: String?, on: Boolean?, cb: (TileResult) -> Unit, attempt: Int) {
        val found = findTile(names)
        if (found == null) {
            if (attempt == 0) {
                // the tile may be on the next Quick Settings page
                systemRoots().forEach { r ->
                    findNode(r) { it.isScrollable }?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                }
                handler.postDelayed({ tileStep(names, dialogLabel, on, cb, 1) }, 700)
            } else {
                closeShade(); cb(TileResult.NOT_FOUND)
            }
            return
        }
        val (tile, matched) = found
        val isInternetTile = matched == "internet" && dialogLabel != null
        if (!isInternetTile && on != null && tileState(tile) == on) {
            closeShade(); cb(TileResult.ALREADY); return
        }
        clickNode(tile)
        handler.postDelayed({
            var dialog = false
            if (isInternetTile) {
                val sw = findSwitchNear(dialogLabel!!)
                if (sw != null) {
                    dialog = true
                    if (on == null || sw.isChecked != on) clickNode(sw)
                }
            }
            handler.postDelayed({
                if (dialog) performGlobalAction(GLOBAL_ACTION_BACK)
                handler.postDelayed({ closeShade() }, 300)
                cb(TileResult.DONE)
            }, 600)
        }, 900)
    }

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
