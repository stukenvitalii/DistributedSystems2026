package com.stukenvitalii.lab4

import com.stukenvitalii.lab4.conflict.ConflictDetector
import com.stukenvitalii.lab4.conflict.ConflictResolver
import com.stukenvitalii.lab4.conflict.ResolutionStrategy
import com.stukenvitalii.lab4.node.ReplicaNode
import com.stukenvitalii.lab4.replication.AsyncMessageBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

/**
 * Entry point — demonstration scenario for Lab 4.
 *
 * Modeled system:
 *  - 3 nodes: Node-A, Node-B, Node-C
 *  - Leaderless replication: writes can go to any node
 *  - Asynchronous replication with simulated network delays (50–500 ms)
 *  - Conflict resolution algorithm: Vector Clock Merge
 *
 * Scenarios:
 *  1. Sequential writes — causally ordered updates (no conflicts)
 *  2. Concurrent writes to different nodes for the same key — conflict arises
 *  3. Conflict resolution
 *  4. Three-way concurrent conflict
 */
fun main() = runBlocking {
    printBanner()

    // ==========================================================================
    // Initialisation
    // ==========================================================================

    val bus = AsyncMessageBus(minDelayMs = 50, maxDelayMs = 500)
    val resolver = ConflictResolver(strategy = ResolutionStrategy.VECTOR_CLOCK_MERGE)

    val nodeA = ReplicaNode(id = "Node-A", bus = bus, resolver = resolver)
    val nodeB = ReplicaNode(id = "Node-B", bus = bus, resolver = resolver)
    val nodeC = ReplicaNode(id = "Node-C", bus = bus, resolver = resolver)

    val allNodes = listOf(nodeA, nodeB, nodeC)
    val peersOf = { node: ReplicaNode -> allNodes.filter { it.id != node.id }.map { it.id } }

    println()
    println("=".repeat(70))
    println("  SCENARIO 1: Sequential writes (no conflicts)")
    println("=".repeat(70))

    // ==========================================================================
    // Scenario 1: Causally ordered writes
    // ==========================================================================

    // Node-A writes "user:1" = "Alice"
    println("\n>> Node-A: write user:1 = 'Alice'")
    nodeA.write(key = "user:1", value = "Alice", peerIds = peersOf(nodeA))

    // Wait for full propagation (maxDelay=500ms) before B writes,
    // so B already knows about A's write — causal order, no conflict
    delay(700.milliseconds)

    // Node-B updates the same value (already aware of A's write via replication)
    println("\n>> Node-B: write user:1 = 'Alice Updated' (learned about A's write via replication)")
    nodeB.write(key = "user:1", value = "Alice Updated", peerIds = peersOf(nodeB))

    // Wait for full propagation
    delay(700.milliseconds)

    println("\n--- Node states after scenario 1 ---")
    printAllStates(allNodes, key = "user:1")

    // ==========================================================================
    // Scenario 2: Concurrent writes — conflict
    // ==========================================================================

    println()
    println("=".repeat(70))
    println("  SCENARIO 2: Concurrent writes — conflict arises")
    println("=".repeat(70))

    // Node-A and Node-C simultaneously write different values for "product:42"
    // They are unaware of each other's writes (replication has not happened yet)
    println("\n>> Node-A: write product:42 = 'Laptop (v1 by A)' [concurrent with Node-C]")
    nodeA.write(key = "product:42", value = "Laptop (v1 by A)", peerIds = peersOf(nodeA))

    println(">> Node-C: write product:42 = 'Laptop (v1 by C)' [concurrent with Node-A]")
    nodeC.write(key = "product:42", value = "Laptop (v1 by C)", peerIds = peersOf(nodeC))

    // Wait for full propagation — both updates spread to all nodes
    delay(800.milliseconds)

    println("\n--- Node states after concurrent writes (before resolution) ---")
    printAllStates(allNodes, key = "product:42")

    // Check for conflicts
    println("\n--- Conflict analysis ---")
    for (node in allNodes) {
        val versions = node.read("product:42")
        val conflicts = ConflictDetector.findConflicts(versions)
        if (conflicts.size > 1) {
            println("[${node.id}] WARNING: CONFLICT detected! Concurrent versions: ${conflicts.size}")
            conflicts.forEachIndexed { i, v ->
                println("       version ${i + 1}: value='${v.value}' clock=${v.clock} author=${v.authorId}")
            }
        } else {
            println("[${node.id}] OK: No conflict, versions: ${versions.size}")
        }
    }

    // ==========================================================================
    // Scenario 3: Conflict resolution
    // ==========================================================================

    println()
    println("=".repeat(70))
    println("  SCENARIO 3: Conflict resolution (Vector Clock Merge)")
    println("=".repeat(70))

    println("\n>> Applying conflict resolution on all nodes for key 'product:42'...")
    for (node in allNodes) {
        val resolved = node.resolveConflicts("product:42")
        if (resolved != null) {
            println("[${node.id}] Resolved -> value='${resolved.value}' clock=${resolved.clock}")
        }
    }

    println("\n--- Node states after conflict resolution ---")
    printAllStates(allNodes, key = "product:42")

    // ==========================================================================
    // Scenario 4: Three-way concurrent conflict
    // ==========================================================================

    println()
    println("=".repeat(70))
    println("  SCENARIO 4: Concurrent writes from three nodes — three-way conflict")
    println("=".repeat(70))

    // Writes happen "simultaneously" (before replication has propagated),
    // but with a small wallTime offset so the tie-breaker is deterministic
    println("\n>> Node-A writes 'config:timeout' = '30s (from A)' ...")
    nodeA.write(key = "config:timeout", value = "30s (from A)", peerIds = peersOf(nodeA))
    delay(20.milliseconds) // small pause to differentiate wallTime
    println(">> Node-B writes 'config:timeout' = '60s (from B)' ...")
    nodeB.write(key = "config:timeout", value = "60s (from B)", peerIds = peersOf(nodeB))
    delay(20.milliseconds)
    println(">> Node-C writes 'config:timeout' = '45s (from C)' ...")
    nodeC.write(key = "config:timeout", value = "45s (from C)", peerIds = peersOf(nodeC))

    delay(800.milliseconds)

    println("\n--- State before resolution ---")
    printAllStates(allNodes, key = "config:timeout")

    println("\n--- Conflict analysis ---")
    for (node in allNodes) {
        val versions = node.read("config:timeout")
        val conflicts = ConflictDetector.findConflicts(versions)
        println("[${node.id}] versions=${versions.size}, concurrent=${conflicts.size}")
        conflicts.forEach { v ->
            println("       -> value='${v.value}' clock=${v.clock}")
        }
    }

    println("\n>> Resolving conflicts...")
    for (node in allNodes) {
        val resolved = node.resolveConflicts("config:timeout")
        println("[${node.id}] Resolved -> '${resolved?.value}' clock=${resolved?.clock}")
    }

    // ==========================================================================
    // Shutdown
    // ==========================================================================

    delay(200.milliseconds)
    bus.shutdown()

    println()
    println("=".repeat(70))
    println("  CONCLUSIONS")
    println("=".repeat(70))
    println(
        """
  1. In a leaderless replication system with eventual consistency,
     concurrent writes to different nodes inevitably cause conflicts.

  2. Vector clocks allow precise determination of causal ordering
     without global synchronised clocks.
     If clock(A) < clock(B) — A happened before B.
     If neither dominates the other — conflict (CONCURRENT).

  3. Vector Clock Merge preserves causal semantics:
     the merged clock guarantees that future writes will be strictly
     "after" all previous concurrent versions.

  4. Last-Write-Wins (used as a tie-breaker here) is simple but unsafe
     — data loss is possible under contention.
     In production, application-level merge or returning all versions
     to the client (as in Amazon Dynamo) is preferred.
    """.trimIndent()
    )
    println("=".repeat(70))
}

// =============================================================================
// Utilities
// =============================================================================

private fun printBanner() {
    println()
    println("=".repeat(70))
    println("  Lab 4 - Data Replication in Distributed Systems")
    println("  Leaderless Replication + Vector Clocks + Conflict Resolution")
    println("  Nodes: Node-A, Node-B, Node-C  |  Eventual Consistency")
    println("=".repeat(70))
}

private fun printAllStates(nodes: List<ReplicaNode>, key: String) {
    for (node in nodes) {
        val versions = node.read(key)
        if (versions.isEmpty()) {
            println("[${node.id}] key='$key' -> (empty)")
        } else {
            versions.forEachIndexed { i, v ->
                val marker = if (versions.size > 1) " [version ${i + 1}/${versions.size} CONFLICT]" else ""
                println("[${node.id}]$marker key='$key' value='${v.value}' clock=${v.clock}")
            }
        }
    }
}
