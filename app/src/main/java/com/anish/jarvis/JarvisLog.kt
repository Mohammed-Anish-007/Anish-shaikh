package com.anish.jarvis

import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/** In-memory log only (never written to disk, never sent anywhere). */
object JarvisLog {
    data class Entry(val time: String, val msg: String)

    private val lines = ArrayDeque<String>()
    private val entries = ArrayDeque<Entry>()
    private val listeners = CopyOnWriteArrayList<() -> Unit>()
    private val main = Handler(Looper.getMainLooper())
    private val fmt = SimpleDateFormat("HH:mm:ss", Locale.US)

    fun add(msg: String) {
        synchronized(lines) {
            val time = fmt.format(Date())
            lines.addLast("$time  $msg")
            entries.addLast(Entry(time.substring(0, 5), msg))
            while (lines.size > 200) lines.removeFirst()
            while (entries.size > 200) entries.removeFirst()
        }
        main.post { listeners.forEach { it() } }
    }

    fun text(): String = synchronized(lines) { lines.reversed().joinToString("\n") }

    /** Oldest first, last [n] entries. */
    fun recent(n: Int): List<Entry> = synchronized(lines) { entries.toList().takeLast(n) }

    fun listen(l: () -> Unit) { listeners += l }
    fun unlisten(l: () -> Unit) { listeners -= l }
}
