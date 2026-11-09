package com.anish.jarvis

/** Very small Devanagari -> Latin transliterator (enough for matching names and app labels). */
object Translit {
    private val consonants = mapOf(
        'क' to "k", 'ख' to "kh", 'ग' to "g", 'घ' to "gh", 'ङ' to "n",
        'च' to "ch", 'छ' to "chh", 'ज' to "j", 'झ' to "jh", 'ञ' to "n",
        'ट' to "t", 'ठ' to "th", 'ड' to "d", 'ढ' to "dh", 'ण' to "n",
        'त' to "t", 'थ' to "th", 'द' to "d", 'ध' to "dh", 'न' to "n",
        'प' to "p", 'फ' to "ph", 'ब' to "b", 'भ' to "bh", 'म' to "m",
        'य' to "y", 'र' to "r", 'ल' to "l", 'ळ' to "l", 'व' to "v",
        'श' to "sh", 'ष' to "sh", 'स' to "s", 'ह' to "h"
    )
    private val vowels = mapOf(
        'अ' to "a", 'आ' to "aa", 'इ' to "i", 'ई' to "ee", 'उ' to "u", 'ऊ' to "oo",
        'ऋ' to "ri", 'ए' to "e", 'ऐ' to "ai", 'ओ' to "o", 'औ' to "au", 'ऑ' to "o", 'ऍ' to "e"
    )
    private val matras = mapOf(
        'ा' to "aa", 'ि' to "i", 'ी' to "ee", 'ु' to "u", 'ू' to "oo", 'ृ' to "ri",
        'े' to "e", 'ै' to "ai", 'ो' to "o", 'ौ' to "au", 'ॉ' to "o", 'ॅ' to "e"
    )
    private const val VIRAMA = '्'
    private const val NUKTA = '़'
    private const val ANUSVARA = 'ं'
    private const val CHANDRA = 'ँ'
    private const val VISARGA = 'ः'

    fun toLatin(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            val cons = consonants[c]
            if (cons != null) {
                sb.append(cons)
                var j = i + 1
                while (j < s.length && s[j] == NUKTA) j++
                val next = if (j < s.length) s[j] else null
                if (next == VIRAMA) {
                    j++
                } else if (next != null && matras.containsKey(next)) {
                    sb.append(matras[next]); j++
                } else if (next != null && (consonants.containsKey(next) || next == ANUSVARA ||
                            next == CHANDRA || next == VISARGA)) {
                    sb.append('a') // inherent vowel inside a word; dropped at word end (schwa deletion)
                }
                i = j
                continue
            }
            val v = vowels[c]
            val m = matras[c]
            when {
                v != null -> sb.append(v)
                m != null -> sb.append(m)
                c == ANUSVARA || c == CHANDRA -> sb.append('n')
                c == VISARGA -> sb.append('h')
                c == NUKTA || c == VIRAMA -> {}
                else -> sb.append(c)
            }
            i++
        }
        return sb.toString()
    }
}
