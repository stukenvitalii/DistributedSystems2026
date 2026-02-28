package com.stukenvitalii.lab4.conflict

import com.stukenvitalii.lab4.model.VectorClock
import com.stukenvitalii.lab4.model.VersionedValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConflictDetectorTest {

    private fun v(value: String, clock: Map<String, Long>, author: String, wallTime: Long = 0L) =
        VersionedValue(value = value, clock = VectorClock(clock), authorId = author, wallTime = wallTime)

    @Test
    fun `no conflict when one version dominates another`() {
        val old = v("old", mapOf("A" to 1L), "A")
        val new = v("new", mapOf("A" to 2L), "A")
        assertFalse(ConflictDetector.isConflict(old, new))
        assertFalse(ConflictDetector.isConflict(new, old))
    }

    @Test
    fun `conflict detected for concurrent versions`() {
        val fromA = v("from-A", mapOf("A" to 1L, "B" to 0L), "A")
        val fromB = v("from-B", mapOf("A" to 0L, "B" to 1L), "B")
        assertTrue(ConflictDetector.isConflict(fromA, fromB))
    }

    @Test
    fun `findConflicts removes dominated versions`() {
        val old = v("old", mapOf("A" to 1L), "A")
        val newer = v("newer", mapOf("A" to 2L), "A")
        val newest = v("newest", mapOf("A" to 3L), "A")
        val result = ConflictDetector.findConflicts(listOf(old, newer, newest))
        assertEquals(1, result.size)
        assertEquals("newest", result.first().value)
    }

    @Test
    fun `findConflicts keeps concurrent versions`() {
        val fromA = v("from-A", mapOf("A" to 1L, "B" to 0L), "A")
        val fromB = v("from-B", mapOf("A" to 0L, "B" to 1L), "B")
        val result = ConflictDetector.findConflicts(listOf(fromA, fromB))
        assertEquals(2, result.size)
    }

    @Test
    fun `findConflicts with mix of dominated and concurrent`() {
        // old is dominated by both A and B — should be removed
        val old = v("old", mapOf("A" to 0L, "B" to 0L), "X")
        val fromA = v("from-A", mapOf("A" to 1L, "B" to 0L), "A")
        val fromB = v("from-B", mapOf("A" to 0L, "B" to 1L), "B")
        val result = ConflictDetector.findConflicts(listOf(old, fromA, fromB))
        assertEquals(2, result.size)
        assertTrue(result.none { it.value == "old" })
    }
}

