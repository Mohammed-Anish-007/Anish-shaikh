package com.anish.jarvis

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.sqrt

/**
 * The owner's voice-print: the average Vosk x-vector of the enrollment sentences.
 * Stored as numbers in private app storage. No audio is ever saved.
 */
object VoiceProfile {
    /** Speaker vectors from very short utterances are unreliable. */
    const val MIN_FRAMES = 40

    private var cache: FloatArray? = null

    private fun file(ctx: Context) = File(ctx.filesDir, "voiceprint.json")

    fun isEnrolled(ctx: Context) = file(ctx).exists()

    fun clear(ctx: Context) {
        file(ctx).delete(); cache = null
    }

    /** Saves the profile and returns a suggested threshold. */
    fun save(ctx: Context, samples: List<FloatArray>): Float {
        val c = centroid(samples)
        // leave-one-out similarity tells us how consistent the owner's voice is
        val loo = samples.indices.map { i ->
            val others = samples.filterIndexed { j, _ -> j != i }
            cosine(samples[i], centroid(others))
        }
        val suggested = ((loo.minOrNull() ?: 0.65f) - 0.12f).coerceIn(0.35f, 0.75f)
        val o = JSONObject()
        o.put("centroid", JSONArray().apply { c.forEach { put(it.toDouble()) } })
        o.put("samples", samples.size)
        file(ctx).writeText(o.toString())
        cache = c
        return suggested
    }

    private fun load(ctx: Context): FloatArray? {
        cache?.let { return it }
        val f = file(ctx)
        if (!f.exists()) return null
        val arr = JSONObject(f.readText()).getJSONArray("centroid")
        return FloatArray(arr.length()) { arr.getDouble(it).toFloat() }.also { cache = it }
    }

    /** Cosine similarity between the speaker vector and the owner's voice-print (-1..1). */
    fun score(ctx: Context, v: FloatArray): Float {
        val c = load(ctx) ?: return -1f
        if (c.size != v.size) return -1f
        return cosine(v, c)
    }

    private fun normalized(v: FloatArray): FloatArray {
        var n = 0.0
        for (x in v) n += x * x
        val d = sqrt(n).toFloat().takeIf { it > 0 } ?: 1f
        return FloatArray(v.size) { v[it] / d }
    }

    private fun centroid(samples: List<FloatArray>): FloatArray {
        val dim = samples.first().size
        val sum = FloatArray(dim)
        for (s in samples) {
            val n = normalized(s)
            for (i in 0 until dim) sum[i] += n[i]
        }
        return normalized(sum)
    }

    fun cosine(a: FloatArray, b: FloatArray): Float {
        var dot = 0.0; var na = 0.0; var nb = 0.0
        for (i in a.indices) { dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i] }
        if (na == 0.0 || nb == 0.0) return 0f
        return (dot / (sqrt(na) * sqrt(nb))).toFloat()
    }
}
