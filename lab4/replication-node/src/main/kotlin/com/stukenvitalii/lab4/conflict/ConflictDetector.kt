package com.stukenvitalii.lab4.conflict

import com.stukenvitalii.lab4.model.Ordering
import com.stukenvitalii.lab4.model.VersionedValue

/**
 * Conflict detector.
 *
 * Compares two values by their vector clocks to determine whether a conflict exists.
 */
object ConflictDetector {

    /**
     * Determines whether two values are conflicting (concurrent).
     *
     * A conflict occurs when neither value strictly causally precedes the other —
     * i.e. they were written independently.
     */
    fun isConflict(a: VersionedValue, b: VersionedValue): Boolean {
        val ordering = a.clock.compareTo(b.clock)
        return ordering == Ordering.CONCURRENT
    }

    /**
     * From a list of versions selects the "current" set:
     * discards versions that are strictly dominated by another,
     * and returns the remainder (possibly more than one — if there are conflicts).
     *
     * @return List of current (non-dominated) versions.
     */
    fun findConflicts(versions: List<VersionedValue>): List<VersionedValue> {
        if (versions.size <= 1) return versions

        val result = mutableListOf<VersionedValue>()
        for (candidate in versions) {
            val dominated = versions.any { other ->
                other !== candidate &&
                        candidate.clock.compareTo(other.clock) == Ordering.BEFORE
            }
            if (!dominated) result.add(candidate)
        }
        return result
    }
}
