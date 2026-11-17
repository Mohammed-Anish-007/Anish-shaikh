package com.anish.jarvis

import android.content.Context
import java.io.File
import java.io.IOException

/**
 * The Vosk offline models ship INSIDE the APK (app/src/main/assets/models/*.zip) and are
 * unpacked once into private app storage. The app has no INTERNET permission at all.
 * The zips are checksum-verified at build time (models.sha256) and protected by the APK signature.
 */
object ModelManager {
    data class ModelInfo(val dirName: String, val label: String)

    val EN_IN = ModelInfo("vosk-model-small-en-in-0.4", "English / Hinglish voice brain")
    val HI = ModelInfo("vosk-model-small-hi-0.22", "Hindi voice brain")
    val SPEAKER = ModelInfo("vosk-model-spk-0.4", "Voice-lock model")

    fun speechModel(ctx: Context) = if (Prefs.language(ctx) == "hi") HI else EN_IN

    private fun root(ctx: Context) = File(ctx.filesDir, "models")
    fun dir(ctx: Context, m: ModelInfo) = File(root(ctx), m.dirName)
    fun isReady(ctx: Context, m: ModelInfo) = File(dir(ctx, m), ".ready").exists()
    fun allReady(ctx: Context) = isReady(ctx, speechModel(ctx)) && isReady(ctx, SPEAKER)
    fun missing(ctx: Context) = listOf(speechModel(ctx), SPEAKER).filter { !isReady(ctx, it) }

    /** Unpacks a bundled model. Blocking – call from a background thread. */
    fun prepare(ctx: Context, info: ModelInfo, progress: (String) -> Unit) {
        val root = root(ctx).apply { mkdirs() }
        progress("${info.label}: unpacking…")
        val tmp = File(root, info.dirName + ".tmp")
        tmp.deleteRecursively()
        try {
            ctx.assets.open("models/${info.dirName}.zip").use { SafeZip.unzip(it, tmp) }
        } catch (e: IOException) {
            tmp.deleteRecursively()
            throw IOException("${info.label} is missing from this build (${e.message})")
        }
        val children = tmp.listFiles()?.toList().orEmpty()
        val src = if (children.size == 1 && children[0].isDirectory) children[0] else tmp
        val target = dir(ctx, info)
        target.deleteRecursively()
        if (!src.renameTo(target)) throw IOException("Could not move model into place")
        tmp.deleteRecursively()
        File(target, ".ready").writeText("ok")
        progress("${info.label}: ready ✓")
    }

    /** Unpacks every model the current language needs. */
    fun prepareAll(ctx: Context, progress: (String) -> Unit) {
        for (m in missing(ctx)) prepare(ctx, m, progress)
        // free space: remove the other language's unpacked model
        val other = if (speechModel(ctx) == EN_IN) HI else EN_IN
        dir(ctx, other).deleteRecursively()
    }
}
