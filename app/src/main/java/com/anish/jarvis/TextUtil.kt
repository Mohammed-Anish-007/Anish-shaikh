package com.anish.jarvis

import java.text.Normalizer
import java.util.Locale

/** Text helpers shared by the parser and the contact / app matchers. Pure Kotlin, unit-tested. */
object TextUtil {

    private val NON_WORD = Regex("[^\\p{L}\\p{M}\\p{N}:\\s]")
    private val SPACES = Regex("\\s+")
    private val REPEAT = Regex("(.)\\1+")

    /** Lowercase, drop punctuation, unify Devanagari nukta / chandrabindu, convert Devanagari digits. */
    fun normalize(s: String): String {
        var t = Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace("़", "")          // nukta (फ़ -> फ)
            .replace('ँ', 'ं')     // chandrabindu -> anusvara (पाँच -> पांच)
        t = Normalizer.normalize(t, Normalizer.Form.NFC)
        val sb = StringBuilder(t.length)
        for (c in t) sb.append(if (c in '०'..'९') ('0' + (c - '०')) else c)
        t = sb.toString().lowercase(Locale.ROOT)
            .replace("'", "").replace("’", "")
        t = NON_WORD.replace(t, " ")
        return SPACES.replace(t, " ").trim()
    }

    fun tokens(s: String): List<String> = normalize(s).split(" ").filter { it.isNotEmpty() }

    /**
     * A rough "sounds-like" key so that "राहुल" matches "Rahul" and "व्हाट्सएप" matches "WhatsApp".
     */
    fun phonetic(s: String): String {
        var t = Translit.toLatin(normalize(s)).replace(Regex("[^a-z0-9 ]"), "")
        t = t.split(" ").filter { it.isNotEmpty() }.joinToString("") { w ->
            var x = w.replace("ph", "f").replace("w", "v").replace("q", "k").replace("z", "j")
                .replace("ck", "k").replace("ee", "i").replace("oo", "u").replace("ou", "u")
            if (x.length > 3 && x.endsWith("e")) x = x.dropLast(1)  // silent English e: phone -> phon
            x
        }
        return REPEAT.replace(t, "\$1")
    }

    fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var prev = IntArray(b.length + 1) { it }
        var cur = IntArray(b.length + 1)
        for (i in 1..a.length) {
            cur[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                cur[j] = minOf(cur[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
            }
            val tmp = prev; prev = cur; cur = tmp
        }
        return prev[b.length]
    }

    fun similarity(a: String, b: String): Double {
        if (a.isEmpty() && b.isEmpty()) return 1.0
        val m = maxOf(a.length, b.length)
        return 1.0 - levenshtein(a, b).toDouble() / m
    }

    /** Score how well a spoken phrase matches a name (full name or its first word). */
    fun nameScore(spoken: String, name: String): Double {
        val key = phonetic(spoken)
        if (key.isEmpty()) return 0.0
        val full = phonetic(name)
        if (full.isEmpty()) return 0.0
        var best = similarity(key, full)
        if (full.contains(key) && key.length >= 3) best = maxOf(best, 0.8 + 0.2 * key.length / full.length)
        val words = normalize(name).split(" ").filter { it.isNotEmpty() }
        if (words.size > 1) {
            best = maxOf(best, similarity(key, phonetic(words[0])) * 0.97)
            best = maxOf(best, similarity(key, phonetic(words.take(2).joinToString(" "))) * 0.98)
        }
        return best
    }

    /** Best match among [names]; returns (index, score) or null if the list is empty. */
    fun bestMatch(names: List<String>, spoken: String): Pair<Int, Double>? {
        var bestI = -1
        var bestS = -1.0
        names.forEachIndexed { i, n ->
            val s = nameScore(spoken, n)
            if (s > bestS) { bestS = s; bestI = i }
        }
        return if (bestI < 0) null else bestI to bestS
    }
}
