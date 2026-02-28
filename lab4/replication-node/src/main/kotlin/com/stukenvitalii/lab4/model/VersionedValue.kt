package com.stukenvitalii.lab4.model

/**
 * A versioned value — a pair of (value, vector clock).
 *
 * @property value    The stored value.
 * @property clock    Vector clock at the time of the write.
 * @property authorId Identifier of the node that performed the write.
 * @property wallTime Monotonic wall-clock time of the write (System.currentTimeMillis).
 *                    Used ONLY as a tie-breaker in the Last-Write-Wins strategy for
 *                    concurrent versions. Not used as the sole ordering criterion.
 */
data class VersionedValue(
    val value: String,
    val clock: VectorClock,
    val authorId: String,
    val wallTime: Long = System.currentTimeMillis()
) {
    override fun toString(): String =
        "VersionedValue(value='$value', clock=$clock, author='$authorId')"
}
