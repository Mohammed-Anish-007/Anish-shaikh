package com.anish.jarvis

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

/** Finds contacts on the phone by spoken name (works for English and Devanagari speech). */
object ContactFinder {
    data class Contact(val name: String, val number: String)

    const val MIN_SCORE = 0.72

    private var cache: List<Contact> = emptyList()
    private var cacheAt = 0L

    fun hasPermission(ctx: Context) =
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    fun all(ctx: Context): List<Contact> {
        if (!hasPermission(ctx)) return emptyList()
        val now = SystemClock.elapsedRealtime()
        if (cache.isNotEmpty() && now - cacheAt < 60_000) return cache
        val list = ArrayList<Contact>()
        val proj = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        ctx.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, proj, null, null, null)?.use { c ->
            val ni = c.getColumnIndex(proj[0])
            val pi = c.getColumnIndex(proj[1])
            while (c.moveToNext()) {
                val n = c.getString(ni) ?: continue
                val p = c.getString(pi) ?: continue
                list += Contact(n, p)
            }
        }
        cache = list.distinctBy { it.name.lowercase() }
        cacheAt = now
        return cache
    }

    /** Best contact for the spoken name with its score, or null if nothing is close enough. */
    fun find(ctx: Context, spoken: String): Pair<Contact, Double>? {
        val list = all(ctx)
        val r = TextUtil.bestMatch(list.map { it.name }, spoken) ?: return null
        return if (r.second >= MIN_SCORE) list[r.first] to r.second else null
    }
}

/** Finds installed apps by spoken name. */
object AppFinder {
    data class App(val label: String, val pkg: String)

    const val MIN_SCORE = 0.7

    private var cache: List<App> = emptyList()
    private var cacheAt = 0L

    fun all(ctx: Context): List<App> {
        val now = SystemClock.elapsedRealtime()
        if (cache.isNotEmpty() && now - cacheAt < 120_000) return cache
        val pm = ctx.packageManager
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        cache = pm.queryIntentActivities(i, 0).map {
            App(it.loadLabel(pm).toString(), it.activityInfo.packageName)
        }.filter { it.pkg != ctx.packageName }.distinctBy { it.pkg }
        cacheAt = now
        return cache
    }

    private val aliases = mapOf(
        "insta" to "instagram", "fb" to "facebook", "yt" to "youtube", "gpay" to "google pay",
        "maps" to "maps", "map" to "maps", "photos" to "photos", "gallery" to "gallery",
        "dialer" to "phone", "contacts" to "contacts", "browser" to "chrome", "mail" to "gmail",
        "play store" to "play store", "playstore" to "play store", "calculator" to "calculator"
    )

    fun find(ctx: Context, spoken: String): App? {
        val list = all(ctx)
        val key = aliases[TextUtil.normalize(spoken)] ?: spoken
        val r = TextUtil.bestMatch(list.map { it.label }, key) ?: return null
        return if (r.second >= MIN_SCORE) list[r.first] else null
    }
}
