package com.anish.jarvis

/**
 * Brute-force protection for the voice lock: after [maxFails] rejected voices within
 * [windowMs], Jarvis ignores everyone (including you) for [lockMs].
 */
class VoiceGuard(
    private val maxFails: Int = 3,
    private val windowMs: Long = 60_000,
    private val lockMs: Long = 120_000,
    private val clock: () -> Long
) {
    private val fails = ArrayDeque<Long>()
    private var lockedUntil = 0L

    fun isLocked(): Boolean = clock() < lockedUntil

    fun secondsLeft(): Long = ((lockedUntil - clock()).coerceAtLeast(0) + 999) / 1000

    /** Returns true if this rejection just triggered the lock-out. */
    fun onReject(): Boolean {
        val now = clock()
        fails.addLast(now)
        while (fails.isNotEmpty() && now - fails.first() > windowMs) fails.removeFirst()
        if (fails.size >= maxFails) {
            lockedUntil = now + lockMs
            fails.clear()
            return true
        }
        return false
    }

    fun onAccept() = fails.clear()
}
