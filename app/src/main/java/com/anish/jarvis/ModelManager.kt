package com.anish.jarvis

import android.content.Context
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

/**
 * Downloads the Vosk offline models ONCE into private app storage.
 * After that, recognition and speaker verification run 100% on the phone.
 */
object ModelManager {
    data class ModelInfo(val url: String, val dirName: String, val label: String)

    val EN_IN = ModelInfo(
        "https://alphacephei.com/vosk/models/vosk-model-small-en-in-0.4.zip",
        "vosk-model-small-en-in-0.4", "English (India) speech model"
    )
    val HI = ModelInfo(
        "https://alphacephei.com/vosk/models/vosk-model-small-hi-0.22.zip",
        "vosk-model-small-hi-0.22", "Hindi speech model"
    )
    val SPEAKER = ModelInfo(
        "https://alphacephei.com/vosk/models/vosk-model-spk-0.4.zip",
        "vosk-model-spk-0.4", "Speaker (voice lock) model"
    )

    fun speechModel(ctx: Context) = if (Prefs.language(ctx) == "hi") HI else EN_IN

    private fun root(ctx: Context) = File(ctx.filesDir, "models")
    fun dir(ctx: Context, m: ModelInfo) = File(root(ctx), m.dirName)
    fun isReady(ctx: Context, m: ModelInfo) = File(dir(ctx, m), ".ready").exists()
    fun allReady(ctx: Context) = isReady(ctx, speechModel(ctx)) && isReady(ctx, SPEAKER)

    /** Blocking – call from a background thread. */
    fun download(ctx: Context, info: ModelInfo, progress: (String) -> Unit) {
        val root = root(ctx).apply { mkdirs() }
        val zip = File(ctx.cacheDir, info.dirName + ".zip")
        progress("${info.label}: connecting…")
        val conn = URL(info.url).openConnection() as HttpURLConnection
        conn.connectTimeout = 20_000
        conn.readTimeout = 60_000
        conn.instanceFollowRedirects = true
        try {
            if (conn.responseCode !in 200..299) throw IOException("HTTP ${conn.responseCode}")
            val total = conn.contentLengthLong
            conn.inputStream.use { input ->
                FileOutputStream(zip).use { out ->
                    val buf = ByteArray(64 * 1024)
                    var done = 0L
                    var last = -1
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        done += n
                        if (total > 0) {
                            val pct = (done * 100 / total).toInt()
                            if (pct / 10 != last) { last = pct / 10; progress("${info.label}: $pct%") }
                        }
                    }
                }
            }
        } finally {
            conn.disconnect()
        }

        progress("${info.label}: unpacking…")
        val tmp = File(root, info.dirName + ".tmp")
        tmp.deleteRecursively(); tmp.mkdirs()
        unzip(zip, tmp)
        zip.delete()
        val children = tmp.listFiles()?.toList().orEmpty()
        val src = if (children.size == 1 && children[0].isDirectory) children[0] else tmp
        val target = dir(ctx, info)
        target.deleteRecursively()
        if (!src.renameTo(target)) throw IOException("Could not move model into place")
        tmp.deleteRecursively()
        File(target, ".ready").writeText("ok")
        progress("${info.label}: ready ✓")
    }

    private fun unzip(zip: File, dest: File) {
        val destPath = dest.canonicalPath + File.separator
        ZipInputStream(BufferedInputStream(FileInputStream(zip))).use { zis ->
            var e = zis.nextEntry
            while (e != null) {
                val f = File(dest, e.name)
                if (!f.canonicalPath.startsWith(destPath)) throw IOException("Bad zip entry ${e.name}")
                if (e.isDirectory) f.mkdirs() else {
                    f.parentFile?.mkdirs()
                    FileOutputStream(f).use { zis.copyTo(it) }
                }
                zis.closeEntry()
                e = zis.nextEntry
            }
        }
    }
}
