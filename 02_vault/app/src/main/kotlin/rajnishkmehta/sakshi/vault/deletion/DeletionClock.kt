/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import android.content.Context
import android.os.SystemClock
import android.provider.Settings

/**
 * A consistent snapshot of the clocks used to schedule and evaluate media deletion.
 *
 * @property wallTimeMillis Wall-clock time in epoch milliseconds. Can be changed by the user or network time.
 * @property elapsedRealtimeMillis Monotonic time since boot, including deep sleep. Resets on reboot.
 * @property bootCount Number of device boots, or `null` if unavailable. Identifies whether
 * [elapsedRealtimeMillis] values from different snapshots are comparable.
 */
data class DeletionClockReading(
    val wallTimeMillis: Long,
    val elapsedRealtimeMillis: Long,
    val bootCount: Int?
)

/**
 * Source of [DeletionClockReading]s. Abstracted so eligibility and scheduling can be tested
 * with deterministic time.
 */
fun interface DeletionClock {
    /** Returns the current clock snapshot. */
    fun now(): DeletionClockReading
}

/**
 * [DeletionClock] backed by the Android system clocks and the global boot counter.
 */
class SystemDeletionClock(context: Context) : DeletionClock {

    private val contentResolver = context.applicationContext.contentResolver

    override fun now(): DeletionClockReading = DeletionClockReading(
        wallTimeMillis = System.currentTimeMillis(),
        elapsedRealtimeMillis = SystemClock.elapsedRealtime(),
        bootCount = readBootCount()
    )

    private fun readBootCount(): Int? = try {
        Settings.Global.getInt(contentResolver, Settings.Global.BOOT_COUNT, UNKNOWN_BOOT_COUNT)
            .takeIf { it != UNKNOWN_BOOT_COUNT }
    } catch (_: SecurityException) {
        null
    }

    private companion object {
        const val UNKNOWN_BOOT_COUNT = -1
    }
}
