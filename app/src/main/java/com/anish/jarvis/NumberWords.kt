package com.anish.jarvis

/** Converts spoken numbers (English, Hinglish, Hindi) to digits. */
object NumberWords {
    private val englishUnits = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5, "six" to 6,
        "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10, "eleven" to 11, "twelve" to 12,
        "thirteen" to 13, "fourteen" to 14, "fifteen" to 15, "sixteen" to 16, "seventeen" to 17,
        "eighteen" to 18, "nineteen" to 19
    )
    private val englishTens = mapOf(
        "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50,
        "sixty" to 60, "seventy" to 70, "eighty" to 80, "ninety" to 90
    )
    private val hindi: Map<String, Int> = listOf(
        "ek" to 1, "teen" to 3, "tin" to 3, "char" to 4, "chaar" to 4, "paanch" to 5, "panch" to 5,
        "chhe" to 6, "chhah" to 6, "che" to 6, "saat" to 7, "aath" to 8, "ath" to 8, "nau" to 9,
        "das" to 10, "gyarah" to 11, "gyara" to 11, "barah" to 12, "bara" to 12,
        "pandrah" to 15, "bees" to 20, "pachis" to 25, "pachchis" to 25, "tees" to 30,
        "paintis" to 35, "chalis" to 40, "chaalis" to 40, "paintalis" to 45, "pachas" to 50,
        "pachaas" to 50, "pachpan" to 55,
        "एक" to 1, "तीन" to 3, "चार" to 4, "पांच" to 5, "छह" to 6, "छः" to 6, "छे" to 6,
        "सात" to 7, "आठ" to 8, "नौ" to 9, "दस" to 10, "ग्यारह" to 11, "बारह" to 12,
        "पंद्रह" to 15, "पन्द्रह" to 15, "बीस" to 20, "पच्चीस" to 25, "तीस" to 30, "पैंतीस" to 35,
        "चालीस" to 40, "पैंतालीस" to 45, "पचास" to 50, "पचपन" to 55
    ).associate { TextUtil.normalize(it.first) to it.second }

    /** "do"/"दो" also means "give" — only treat it as 2 next to a time/duration word. */
    private val twoWords = setOf("do", "दो")
    private val twoContext = setOf(
        "baje", "बजे", "minute", "minutes", "मिनट", "ghante", "ghanta", "घंटे", "घंटा",
        "second", "seconds", "सेकंड", "saade", "sade", "साढे", "sawa", "सवा", "paune", "पौने"
    )

    private fun value(tok: String, prev: String?, next: String?): Int? {
        tok.toIntOrNull()?.let { return it }
        englishUnits[tok]?.let { return it }
        englishTens[tok]?.let { return it }
        hindi[tok]?.let { return it }
        if (tok in twoWords && (next in twoContext || prev in twoContext)) return 2
        return null
    }

    /** Replace number words in a token list with digit strings ("seven thirty" -> "7", "30"). */
    fun toDigits(tokens: List<String>): List<String> {
        val out = ArrayList<String>()
        var i = 0
        while (i < tokens.size) {
            val t = tokens[i]
            val v = value(t, tokens.getOrNull(i - 1), tokens.getOrNull(i + 1))
            if (v == null) { out += t; i++; continue }
            if (englishTens.containsKey(t)) {
                val unit = tokens.getOrNull(i + 1)?.let { englishUnits[it] }
                if (unit != null && unit in 1..9) { out += (v + unit).toString(); i += 2; continue }
            }
            out += v.toString(); i++
        }
        return out
    }

    /** "nine eight seven six ..." or "98765..." -> "98765..." ; null if it is not a phone number. */
    fun phoneNumber(text: String): String? {
        val toks = toDigits(TextUtil.tokens(text).filter { it != "plus" })
        if (toks.isEmpty() || !toks.all { it.all(Char::isDigit) }) return null
        val n = toks.joinToString("")
        return if (n.length >= 3) n else null
    }
}
