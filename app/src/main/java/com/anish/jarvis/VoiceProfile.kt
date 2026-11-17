package com.anish.jarvis

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * The owner's voice-print: the average Vosk x-vector of the enrollment sentences.
 * Stored ENCRYPTED (Android Keystore, AES-256-GCM) in private app storage. No audio is ever saved.
 */
object VoiceProfile {
    /** Speaker vectors from very short utterances are unreliable. */
    const val MIN_FRAMES = 40

    private var cache: FloatArray? = null

    private fun file(ctx: Context) = File(ctx.filesDir, "voiceprint.bin")
    private fun legacyFile(ctx: Context) = File(ctx.filesDir, "voiceprint.json")

    fun isEnrolled(ctx: Context) = file(ctx).exists() || legacyFile(ctx).exists()

    fun clear(ctx: Context) {
        file(ctx).delete(); legacyFile(ctx).delete(); cache = null
        SecureStore.deleteKey()
    }

    /** Saves the profile (encrypted) and returns a suggested threshold. */
    fun save(ctx: Context, samples: List<FloatArray>): Float {
        val c = VoiceMath.centroid(samples)
        write(ctx, c, samples.size)
        return VoiceMath.suggestThreshold(samples)
    }

    private fun write(ctx: Context, c: FloatArray, count: Int) {
        val o = JSONObject()
        o.put("centroid", JSONArray().apply { c.forEach { put(it.toDouble()) } })
        o.put("samples", count)
        file(ctx).writeBytes(SecureStore.encrypt(o.toString().toByteArray()))
        legacyFile(ctx).delete()
        cache = c
    }

    private fun parse(json: String): FloatArray {
        val arr = JSONObject(json).getJSONArray("centroid")
        return FloatArray(arr.length()) { arr.getDouble(it).toFloat() }
    }

    fun load(ctx: Context): FloatArray? {
        cache?.let { return it }
        return try {
            when {
                file(ctx).exists() -> parse(String(SecureStore.decrypt(file(ctx).readBytes())))
                legacyFile(ctx).exists() -> {
                    // migrate the old plain-text profile to the encrypted one
                    val c = parse(legacyFile(ctx).readText())
                    write(ctx, c, 0)
                    c
                }
                else -> null
            }?.also { cache = it }
        } catch (e: Exception) {
            JarvisLog.add("Voice-print could not be read (${e.javaClass.simpleName}). Please record your voice again.")
            null
        }
    }

    /** Cosine similarity between the speaker vector and the owner's voice-print (-1..1). */
    fun score(ctx: Context, v: FloatArray): Float {
        val c = load(ctx) ?: return -1f
        return VoiceMath.cosine(v, c)
    }
}
