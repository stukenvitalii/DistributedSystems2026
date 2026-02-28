package com.stukenvitalii.lab4.model

/**
 * Vector Clock.
 *
 * Stores a counter per node. Allows determining causal ordering between
 * events without globally synchronised clocks.
 *
 * @property counters Map<nodeId, counter> — per-node event counters.
 */
data class VectorClock(
    val counters: Map<String, Long> = emptyMap()
) {
    /**
     * Increments the counter for the given node.
     */
    fun increment(nodeId: String): VectorClock {
        val updated = counters.toMutableMap()
        updated[nodeId] = (updated[nodeId] ?: 0L) + 1L
        return VectorClock(updated)
    }

    /**
     * Merges two vector clocks — takes the maximum counter per node.
     */
    fun merge(other: VectorClock): VectorClock {
        val merged = counters.toMutableMap()
        for ((nodeId, counter) in other.counters) {
            merged[nodeId] = maxOf(merged[nodeId] ?: 0L, counter)
        }
        return VectorClock(merged)
    }

    /**
     * Compares two vector clocks.
     * @return Ordering: BEFORE, AFTER, CONCURRENT, or EQUAL.
     */
    fun compareTo(other: VectorClock): Ordering {
        val allKeys = counters.keys + other.counters.keys
        var thisLeads = false
        var otherLeads = false

        for (key in allKeys) {
            val a = counters[key] ?: 0L
            val b = other.counters[key] ?: 0L
            if (a > b) thisLeads = true
            if (b > a) otherLeads = true
        }

        return when {
            !thisLeads && !otherLeads -> Ordering.EQUAL
            thisLeads && !otherLeads -> Ordering.AFTER      // this happened after other
            !thisLeads && otherLeads -> Ordering.BEFORE     // this happened before other
            else -> Ordering.CONCURRENT // conflict
        }
    }

    override fun toString(): String {
        val sorted = counters.entries.sortedBy { it.key }
            .joinToString(", ") { "${it.key}=${it.value}" }
        return "{$sorted}"
    }
}

/**
 * Result of comparing two vector clocks.
 */
enum class Ordering {
    /** This event happened BEFORE the other (causally). */
    BEFORE,

    /** This event happened AFTER the other. */
    AFTER,

    /** Events are independent — conflict. */
    CONCURRENT,

    /** Identical clocks. */
    EQUAL
}
