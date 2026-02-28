package com.stukenvitalii.lab4.model

import kotlin.test.Test
import kotlin.test.assertEquals

class VectorClockTest {

    @Test
    fun `increment increases counter for specified node`() {
        val clock = VectorClock()
        val updated = clock.increment("A").increment("A").increment("B")
        assertEquals(2L, updated.counters["A"])
        assertEquals(1L, updated.counters["B"])
    }

    @Test
    fun `merge takes maximum counters`() {
        val a = VectorClock(mapOf("A" to 3L, "B" to 1L))
        val b = VectorClock(mapOf("A" to 1L, "B" to 5L, "C" to 2L))
        val merged = a.merge(b)
        assertEquals(3L, merged.counters["A"])
        assertEquals(5L, merged.counters["B"])
        assertEquals(2L, merged.counters["C"])
    }

    @Test
    fun `equal clocks return EQUAL ordering`() {
        val a = VectorClock(mapOf("A" to 1L))
        val b = VectorClock(mapOf("A" to 1L))
        assertEquals(Ordering.EQUAL, a.compareTo(b))
    }

    @Test
    fun `strictly greater clock returns AFTER`() {
        val before = VectorClock(mapOf("A" to 1L))
        val after = VectorClock(mapOf("A" to 2L))
        assertEquals(Ordering.AFTER, after.compareTo(before))
        assertEquals(Ordering.BEFORE, before.compareTo(after))
    }

    @Test
    fun `independent clocks return CONCURRENT`() {
        val a = VectorClock(mapOf("A" to 2L, "B" to 0L))
        val b = VectorClock(mapOf("A" to 0L, "B" to 2L))
        assertEquals(Ordering.CONCURRENT, a.compareTo(b))
        assertEquals(Ordering.CONCURRENT, b.compareTo(a))
    }

    @Test
    fun `clock missing keys treated as zero`() {
        val a = VectorClock(mapOf("A" to 1L))
        val b = VectorClock(mapOf("A" to 1L, "B" to 0L))
        // B=0 is same as missing B
        assertEquals(Ordering.EQUAL, a.compareTo(b))
    }
}

