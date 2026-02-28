package com.stukenvitalii.lab4.node

import com.stukenvitalii.lab4.conflict.ConflictDetector
import com.stukenvitalii.lab4.conflict.ConflictResolver
import com.stukenvitalii.lab4.model.Ordering
import com.stukenvitalii.lab4.model.VectorClock
import com.stukenvitalii.lab4.model.VersionedValue
import com.stukenvitalii.lab4.replication.AsyncMessageBus
import com.stukenvitalii.lab4.replication.Message
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

/**
 * A node in the distributed data store.
 *
 * Each node holds its own local replica. Read and write operations are
 * performed locally, after which the update is broadcast to peers via the bus.
 *
 * The node does not use globally synchronised clocks — only vector clocks
 * to track causal relationships between events.
 *
 * @param id       Unique node identifier.
 * @param bus      Message bus for replica exchange.
 * @param resolver Conflict resolution strategy.
 */
class ReplicaNode(
    val id: String,
    private val bus: AsyncMessageBus,
    private val resolver: ConflictResolver = ConflictResolver()
) {
    // Local store: key -> list of versions (may contain >1 version during a conflict)
    private val store: MutableMap<String, MutableList<VersionedValue>> = mutableMapOf()

    // Local vector clock of this node
    @Volatile
    private var clock: VectorClock = VectorClock()

    init {
        // Register in the bus — provide the incoming message handler
        bus.register(id) { message -> handleMessage(message) }
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Writes [value] under [key] on this node and initiates async replication to peers.
     *
     * @param key     Record key.
     * @param value   New value.
     * @param peerIds List of peer node identifiers to replicate to.
     */
    @Synchronized
    fun write(key: String, value: String, peerIds: List<String>) {
        // Increment our vector clock
        clock = clock.increment(id)

        val vv = VersionedValue(value = value, clock = clock, authorId = id)
        applyLocally(key, vv)
        log.info { "[$id] WRITE key='$key' value='$value' clock=$clock" }

        // Asynchronously broadcast the update to peers
        for (peerId in peerIds) {
            bus.send(peerId, Message.ReplicationUpdate(sourceNodeId = id, key = key, value = vv))
        }
    }

    /**
     * Reads the value(s) for [key].
     *
     * If the node holds multiple concurrent versions (unresolved conflict),
     * all are returned. The caller can choose a strategy or wait for replication.
     *
     * @return List of versions for the key (empty if the key does not exist).
     */
    @Synchronized
    fun read(key: String): List<VersionedValue> {
        val versions = store[key] ?: return emptyList()
        return versions.toList()
    }

    /**
     * Resolves conflicts for [key] using the configured resolver and stores
     * the single winning version back.
     *
     * @return The winning version, or null if the key is not found.
     */
    @Synchronized
    fun resolveConflicts(key: String): VersionedValue? {
        val versions = store[key] ?: return null
        if (versions.size <= 1) return versions.firstOrNull()

        val conflicts = ConflictDetector.findConflicts(versions)
        return if (conflicts.size <= 1) {
            val resolved = conflicts.firstOrNull() ?: versions.first()
            store[key] = mutableListOf(resolved)
            resolved
        } else {
            val resolved = resolver.resolve(conflicts)
            store[key] = mutableListOf(resolved)
            log.info { "[$id] RESOLVED conflicts for key='$key' -> $resolved" }
            resolved
        }
    }

    /**
     * Returns the current state of the local store (for logging/debugging).
     */
    @Synchronized
    fun dumpState(): Map<String, List<VersionedValue>> = store.mapValues { it.value.toList() }

    // =========================================================================
    // Internal
    // =========================================================================

    /** Dispatches an incoming bus message to the appropriate handler. */
    private fun handleMessage(message: Message) {
        when (message) {
            is Message.ReplicationUpdate -> receiveReplication(message)
        }
    }

    /** Applies an incoming replication update from another node. */
    @Synchronized
    private fun receiveReplication(msg: Message.ReplicationUpdate) {
        // Merge clocks — no self-increment needed, just learn about remote events
        clock = clock.merge(msg.value.clock)
        applyLocally(msg.key, msg.value)
        log.info { "[$id] RECV replication from [${msg.sourceNodeId}] key='${msg.key}' -> ${msg.value}" }
    }

    /**
     * Applies a new version to the local store.
     *
     * Rules:
     * - store is empty  -> just add
     * - incoming strictly dominates all existing -> replace all
     * - any existing strictly dominates incoming -> discard incoming
     * - otherwise -> keep both (conflict to be resolved later)
     */
    private fun applyLocally(key: String, incoming: VersionedValue) {
        val current = store.getOrPut(key) { mutableListOf() }

        if (current.isEmpty()) {
            current.add(incoming)
            return
        }

        val toKeep = mutableListOf<VersionedValue>()
        var dominated = false  // is incoming dominated by any existing version?

        for (existing in current) {
            val order = incoming.clock.compareTo(existing.clock)
            when (order) {
                Ordering.AFTER -> {
                    // Incoming is newer — existing is stale, do not keep it
                }

                Ordering.BEFORE, Ordering.EQUAL -> {
                    // Incoming is older or equal — keep existing, discard incoming
                    toKeep.add(existing)
                    dominated = true
                }

                Ordering.CONCURRENT -> {
                    // Conflict — keep existing
                    toKeep.add(existing)
                }
            }
        }

        if (!dominated) {
            toKeep.add(incoming)
        }

        store[key] = toKeep
    }
}

