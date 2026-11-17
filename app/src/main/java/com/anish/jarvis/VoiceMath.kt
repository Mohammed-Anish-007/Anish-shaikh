package com.anish.jarvis

import kotlin.math.sqrt

/** Pure maths behind the voice lock (unit-tested). */
object VoiceMath {

    fun cosine(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size || a.isEmpty()) return -1f
        var dot = 0.0; var na = 0.0; var nb = 0.0
        for (i in a.indices) { dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i] }
        if (na == 0.0 || nb == 0.0) return -1f
        return (dot / (sqrt(na) * sqrt(nb))).toFloat()
    }

    fun normalized(v: FloatArray): FloatArray {
        var n = 0.0
        for (x in v) n += x * x
        val d = sqrt(n).toFloat().takeIf { it > 0 } ?: 1f
        return FloatArray(v.size) { v[it] / d }
    }

    fun centroid(samples: List<FloatArray>): FloatArray {
        val dim = samples.first().size
        val sum = FloatArray(dim)
        for (s in samples) {
            val n = normalized(s)
            for (i in 0 until dim) sum[i] += n[i]
        }
        return normalized(sum)
    }

    /** Threshold suggestion from leave-one-out consistency of the owner's own samples. */
    fun suggestThreshold(samples: List<FloatArray>): Float {
        if (samples.size < 2) return Prefs.DEFAULT_THRESHOLD
        val loo = samples.indices.map { i ->
            cosine(samples[i], centroid(samples.filterIndexed { j, _ -> j != i }))
        }
        return ((loo.minOrNull() ?: 0.65f) - 0.12f).coerceIn(0.35f, 0.75f)
    }

    /**
     * The single place that decides whether a voice is the owner's.
     * Too-short audio, missing vectors or a wrong-size vector are always rejected.
     */
    fun isOwner(vector: FloatArray?, frames: Int, profile: FloatArray?, threshold: Float): Boolean {
        if (vector == null || profile == null) return false
        if (frames < VoiceProfile.MIN_FRAMES) return false
        if (vector.size != profile.size) return false
        if (vector.any { it.isNaN() || it.isInfinite() }) return false
        return cosine(vector, profile) >= threshold
    }
}
