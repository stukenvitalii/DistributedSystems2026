package com.stukenvitalii.lab4.conflict

import com.stukenvitalii.lab4.model.VectorClock
import com.stukenvitalii.lab4.model.VersionedValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConflictResolverTest {

    private fun v(value: String, clock: Map<String, Long>, author: String, wallTime: Long) =
        VersionedValue(value = value, clock = VectorClock(clock), authorId = author, wallTime = wallTime)

    @Test
    fun `lww picks latest wallTime`() {
        val resolver = ConflictResolver(ResolutionStrategy.LAST_WRITE_WINS)
        val old = v("old-value", mapOf("A" to 1L), "A", wallTime = 1000L)
        val new = v("new-value", mapOf("B" to 1L), "B", wallTime = 2000L)
        val result = resolver.resolve(listOf(old, new))
        assertEquals("new-value", result.value)
    }

    @Test
    fun `vector merge merges clocks and picks latest wallTime`() {
        val resolver = ConflictResolver(ResolutionStrategy.VECTOR_CLOCK_MERGE)
        val fromA = v("from-A", mapOf("A" to 1L, "B" to 0L), "A", wallTime = 1000L)
        val fromB = v("from-B", mapOf("A" to 0L, "B" to 1L), "B", wallTime = 2000L)
        val result = resolver.resolve(listOf(fromA, fromB))
        assertEquals("from-B", result.value)
        // Merged clocks must have both A=1 and B=1
        assertEquals(1L, result.clock.counters["A"])
        assertEquals(1L, result.clock.counters["B"])
    }

    @Test
    fun `single version returned as-is`() {
        val resolver = ConflictResolver()
        val single = v("only", mapOf("A" to 1L), "A", wallTime = 0L)
        assertEquals(single, resolver.resolve(listOf(single)))
    }

    @Test
    fun `merged clock dominates all input clocks`() {
        val resolver = ConflictResolver(ResolutionStrategy.VECTOR_CLOCK_MERGE)
        val a = v("a", mapOf("A" to 3L, "B" to 1L), "A", wallTime = 100L)
        val b = v("b", mapOf("A" to 1L, "B" to 4L), "B", wallTime = 200L)
        val result = resolver.resolve(listOf(a, b))
        // merged: A=3, B=4
        assertTrue(result.clock.counters["A"]!! >= 3L)
        assertTrue(result.clock.counters["B"]!! >= 4L)
    }
}

