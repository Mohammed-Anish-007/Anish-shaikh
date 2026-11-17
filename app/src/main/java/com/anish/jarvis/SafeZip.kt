package com.anish.jarvis

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream

/** Unzips while refusing "zip-slip" entries (e.g. ../../evil) and oversized archives. */
object SafeZip {
    /** Hard cap so a malformed archive can't fill the phone's storage. */
    const val MAX_TOTAL_BYTES = 600L * 1024 * 1024

    fun unzip(input: InputStream, dest: File, maxBytes: Long = MAX_TOTAL_BYTES) {
        dest.mkdirs()
        val destPath = dest.canonicalPath + File.separator
        var total = 0L
        ZipInputStream(input.buffered()).use { zis ->
            var e = zis.nextEntry
            while (e != null) {
                val f = File(dest, e.name)
                if (!f.canonicalPath.startsWith(destPath)) throw IOException("Blocked unsafe zip entry: ${e.name}")
                if (e.isDirectory) {
                    f.mkdirs()
                } else {
                    f.parentFile?.mkdirs()
                    FileOutputStream(f).use { out ->
                        val buf = ByteArray(64 * 1024)
                        while (true) {
                            val n = zis.read(buf)
                            if (n < 0) break
                            total += n
                            if (total > maxBytes) throw IOException("Archive too large")
                            out.write(buf, 0, n)
                        }
                    }
                }
                zis.closeEntry()
                e = zis.nextEntry
            }
        }
    }
}
