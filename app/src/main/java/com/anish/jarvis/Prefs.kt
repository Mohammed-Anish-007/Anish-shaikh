package com.anish.jarvis

import android.content.Context

/** All settings live in private app storage on the phone. */
object Prefs {
    const val DEFAULT_WAKE = "jarvis, जार्विस, जारविस"
    const val DEFAULT_THRESHOLD = 0.55f

    private fun sp(ctx: Context) = ctx.getSharedPreferences("jarvis_prefs", Context.MODE_PRIVATE)

    fun language(ctx: Context): String = sp(ctx).getString("language", "en") ?: "en"
    fun setLanguage(ctx: Context, v: String) = sp(ctx).edit().putString("language", v).apply()

    fun requireWakeWord(ctx: Context) = sp(ctx).getBoolean("require_wake", true)
    fun setRequireWakeWord(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean("require_wake", v).apply()

    fun wakeWordsRaw(ctx: Context): String = sp(ctx).getString("wake_words", DEFAULT_WAKE) ?: DEFAULT_WAKE
    fun wakeWords(ctx: Context): List<String> =
        wakeWordsRaw(ctx).split(",").map { it.trim() }.filter { it.isNotEmpty() }
    fun setWakeWords(ctx: Context, v: String) = sp(ctx).edit().putString("wake_words", v).apply()

    fun threshold(ctx: Context) = sp(ctx).getFloat("threshold", DEFAULT_THRESHOLD)
    fun setThreshold(ctx: Context, v: Float) = sp(ctx).edit().putFloat("threshold", v).apply()

    fun countryCode(ctx: Context): String = sp(ctx).getString("country_code", "91") ?: "91"
    fun setCountryCode(ctx: Context, v: String) = sp(ctx).edit().putString("country_code", v).apply()
}
