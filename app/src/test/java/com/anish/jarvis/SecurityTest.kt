package com.anish.jarvis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.random.Random

/** Security tests: voice lock, brute-force guard, safe unzip, and command-injection style inputs. */
class SecurityTest {

    // ---------------------------------------------------------------- voice lock

    private fun randVec(seed: Int, dim: Int = 128): FloatArray {
        val r = Random(seed)
        return FloatArray(dim) { r.nextFloat() * 2 - 1 }
    }

    /** owner voice + a little noise */
    private fun noisy(base: FloatArray, seed: Int, amount: Float = 0.25f): FloatArray {
        val r = Random(seed)
        return FloatArray(base.size) { base[it] + (r.nextFloat() * 2 - 1) * amount }
    }

    private val owner = randVec(1)
    private val profile = VoiceMath.centroid((1..5).map { noisy(owner, 100 + it) })
    private val threshold = 0.55f

    @Test fun ownerVoiceIsAccepted() {
        assertTrue(VoiceMath.isOwner(noisy(owner, 7), 200, profile, threshold))
    }

    @Test fun strangerVoiceIsRejected() {
        for (s in 2..60) assertFalse("stranger $s accepted", VoiceMath.isOwner(randVec(s), 200, profile, threshold))
    }

    @Test fun tooShortAudioIsRejectedEvenForOwner() {
        assertFalse(VoiceMath.isOwner(noisy(owner, 7), VoiceProfile.MIN_FRAMES - 1, profile, threshold))
    }

    @Test fun missingOrMalformedVectorsAreRejected() {
        assertFalse(VoiceMath.isOwner(null, 200, profile, threshold))
        assertFalse(VoiceMath.isOwner(owner, 200, null, threshold))
        assertFalse(VoiceMath.isOwner(FloatArray(64), 200, profile, threshold))            // wrong size
        assertFalse(VoiceMath.isOwner(FloatArray(128), 200, profile, threshold))           // all zeros
        assertFalse(VoiceMath.isOwner(FloatArray(128) { Float.NaN }, 200, profile, threshold))
        assertFalse(VoiceMath.isOwner(FloatArray(128) { Float.POSITIVE_INFINITY }, 200, profile, threshold))
    }

    @Test fun suggestedThresholdStaysInSafeRange() {
        val t = VoiceMath.suggestThreshold((1..5).map { noisy(owner, 200 + it) })
        assertTrue(t in 0.35f..0.75f)
        // even wildly inconsistent samples can't push the threshold below the safe floor
        val bad = VoiceMath.suggestThreshold((1..5).map { randVec(300 + it) })
        assertTrue(bad >= 0.35f)
    }

    // ---------------------------------------------------------------- brute-force guard

    @Test fun lockOutAfterThreeWrongVoices() {
        var now = 0L
        val g = VoiceGuard(clock = { now })
        assertFalse(g.onReject()); now += 5_000
        assertFalse(g.onReject()); now += 5_000
        assertTrue(g.onReject())            // third strike
        assertTrue(g.isLocked())
        now += 119_000
        assertTrue(g.isLocked())
        now += 2_000
        assertFalse(g.isLocked())           // unlocks after 2 minutes
    }

    @Test fun slowFailuresDoNotLock() {
        var now = 0L
        val g = VoiceGuard(clock = { now })
        repeat(10) { assertFalse(g.onReject()); now += 61_000 }
        assertFalse(g.isLocked())
    }

    @Test fun ownerSuccessResetsCounter() {
        var now = 0L
        val g = VoiceGuard(clock = { now })
        g.onReject(); g.onReject()
        g.onAccept()
        assertFalse(g.onReject())
        assertFalse(g.isLocked())
    }

    // ---------------------------------------------------------------- safe unzip

    private fun zipOf(vararg entries: Pair<String, ByteArray>): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { z ->
            for ((name, data) in entries) {
                z.putNextEntry(ZipEntry(name)); z.write(data); z.closeEntry()
            }
        }
        return bos.toByteArray()
    }

    @Test fun normalZipExtracts() {
        val dir = Files.createTempDirectory("jarvis").toFile()
        SafeZip.unzip(ByteArrayInputStream(zipOf("model/conf/a.txt" to "hello".toByteArray())), dir)
        assertEquals("hello", File(dir, "model/conf/a.txt").readText())
    }

    @Test fun zipSlipIsBlocked() {
        val parent = Files.createTempDirectory("jarvis").toFile()
        val dir = File(parent, "inside").apply { mkdirs() }
        try {
            SafeZip.unzip(ByteArrayInputStream(zipOf("../evil.txt" to "pwned".toByteArray())), dir)
            fail("zip-slip entry was extracted")
        } catch (e: IOException) {
            assertFalse(File(parent, "evil.txt").exists())
        }
    }

    @Test fun absolutePathEntryIsContained() {
        val dir = Files.createTempDirectory("jarvis").toFile()
        try {
            SafeZip.unzip(ByteArrayInputStream(zipOf("/tmp/jarvis-abs-evil.txt" to "x".toByteArray())), dir)
        } catch (_: IOException) {}
        // either blocked, or written INSIDE the target folder – never to /tmp
        assertFalse(File("/tmp/jarvis-abs-evil.txt").exists() && !File(dir, "tmp/jarvis-abs-evil.txt").exists())
    }

    @Test fun zipBombIsStopped() {
        val dir = Files.createTempDirectory("jarvis").toFile()
        val big = ByteArray(2 * 1024 * 1024)
        try {
            SafeZip.unzip(ByteArrayInputStream(zipOf("big.bin" to big)), dir, maxBytes = 1024 * 1024)
            fail("oversized archive was fully extracted")
        } catch (_: IOException) {}
    }

    // ---------------------------------------------------------------- wake word & parser robustness

    @Test fun wakeWordAloneNeverRunsACommand() {
        val w = WakeWord.split("jarvis", listOf("jarvis"))
        assertTrue(w.found)
        assertEquals("", w.rest)
    }

    @Test fun backgroundChatterDoesNotBecomeADangerousCommand() {
        val chatter = listOf(
            "and that's the weather for today",
            "breaking news the market closed higher",
            "i think we should go out tonight",
            "tell me more about the movie",
            "kal mausam accha rahega",
            "aaj bahut garmi hai"
        )
        for (c in chatter) {
            val cmd = CommandParser.parse(c)
            assertFalse("'$c' became $cmd", cmd is Command.Call || cmd is Command.Message ||
                cmd is Command.Toggle || cmd is Command.Click || cmd is Command.EndCall || cmd is Command.AnswerCall)
        }
    }

    @Test fun hostileInputsNeverCrashTheParser() {
        val inputs = listOf(
            "", " ", "\u0000", "%s%s%s%n", "'; DROP TABLE contacts; --", "../../../../etc/passwd",
            "a".repeat(10_000), "जार्विस ".repeat(500), "😀🔥💀", "‮evil", "call " + "9".repeat(500),
            "set alarm for 99 99", "timer for -5 minutes", "brightness 9999 percent"
        )
        for (i in inputs) CommandParser.parse(i)  // must not throw
        assertEquals(Command.Brightness(100), CommandParser.parse("brightness 9999 percent"))   // clamped
    }

    @Test fun invalidAlarmTimesAreRefused() {
        assertTrue(CommandParser.parse("set alarm for 99 99") is Command.Unknown)
    }
}
