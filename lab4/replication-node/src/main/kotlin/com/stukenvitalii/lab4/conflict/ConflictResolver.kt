package com.stukenvitalii.lab4.conflict

import com.stukenvitalii.lab4.model.VersionedValue

/**
 * Conflict resolution strategies.
 */
enum class ResolutionStrategy {
    /**
     * Last-Write-Wins: on conflict, the version with the highest wallTime wins.
     * Simple strategy — may lead to data loss.
     */
    LAST_WRITE_WINS,

    /**
     * Vector-Clock Merge: builds a merged clock and keeps the value from the
     * version with the latest wallTime, but replaces its clock with the merged one.
     * Used to demonstrate correct clock merging.
     */
    VECTOR_CLOCK_MERGE
}

/**
 * Conflict resolver — applies the chosen strategy to a set of conflicting versions.
 */
class ConflictResolver(
    private val strategy: ResolutionStrategy = ResolutionStrategy.VECTOR_CLOCK_MERGE
) {
    /**
     * Resolves a list of versions (possibly conflicting) into a single winner.
     *
     * @param versions List of versions for one key on one node.
     * @return The single winning version.
     */
    fun resolve(versions: List<VersionedValue>): VersionedValue {
        require(versions.isNotEmpty()) { "Version list must not be empty" }
        if (versions.size == 1) return versions.first()

        return when (strategy) {
            ResolutionStrategy.LAST_WRITE_WINS -> resolveLww(versions)
            ResolutionStrategy.VECTOR_CLOCK_MERGE -> resolveVectorMerge(versions)
        }
    }

    // -------------------------------------------------------------------------
    // Strategy 1: Last-Write-Wins
    // -------------------------------------------------------------------------

    private fun resolveLww(versions: List<VersionedValue>): VersionedValue {
        // Pick the version with the highest wallTime
        return versions.maxBy { it.wallTime }
    }

    // -------------------------------------------------------------------------
    // Strategy 2: Vector Clock Merge
    // -------------------------------------------------------------------------

    private fun resolveVectorMerge(versions: List<VersionedValue>): VersionedValue {
        // Pick the winner by wallTime among concurrent versions
        val winner = versions.maxBy { it.wallTime }

        // Merge all vector clocks
        val mergedClock = versions.fold(winner.clock) { acc, v -> acc.merge(v.clock) }

        return winner.copy(clock = mergedClock)
    }
}
