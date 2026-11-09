package com.anish.jarvis

object WakeWord {
    data class Result(val found: Boolean, val rest: String)

    private val greetings = setOf("hey", "hi", "ok", "okay", "hello", "हे", "ओके", "हेलो")

    fun split(text: String, wakeWords: List<String>): Result {
        val toks = TextUtil.tokens(text)
        val wakes = wakeWords.map { TextUtil.tokens(it) }.filter { it.isNotEmpty() }
        val out = ArrayList<String>()
        var found = false
        var i = 0
        while (i < toks.size) {
            val w = wakes.firstOrNull { i + it.size <= toks.size && toks.subList(i, i + it.size) == it }
            if (w != null) {
                found = true
                if (out.isNotEmpty() && out.last() in greetings) out.removeAt(out.size - 1)
                i += w.size
                continue
            }
            out += toks[i]
            i++
        }
        return Result(found, out.joinToString(" "))
    }
}
