package com.stukenvitalii.lab4.replication

import com.stukenvitalii.lab4.model.VersionedValue

/**
 * Messages exchanged between nodes via the message bus.
 */
sealed class Message {

    /**
     * Replication update — node [sourceNodeId] sends a new version of the value
     * for [key] to all other nodes.
     */
    data class ReplicationUpdate(
        val sourceNodeId: String,
        val key: String,
        val value: VersionedValue
    ) : Message()
}
