package com.stukenvitalii.lab1.p2p

import mu.KotlinLogging

object PeerChat {
    private val logger = KotlinLogging.logger { }
    @JvmStatic
    fun main(args: Array<String>) {
        logger.info { "=== P2P chat without a dedicated server ===" }
        logger.info { "Enter display name:" }
        val name = readlnOrNull()?.takeIf { it.isNotBlank() } ?: "Peer"
        logger.info { "Listening port (e.g. 9000):" }
        val listenPort = readlnOrNull()?.toIntOrNull() ?: 9000
        val node = PeerNode(name, listenPort)
        node.start()
        logger.info { "Commands: /connect host port — connect to another peer, /quit — exit." }
        logger.info { "Type any text to broadcast it to all active connections." }
        while (true) {
            val input = readlnOrNull() ?: break
            when {
                input.equals("/quit", ignoreCase = true) -> {
                    node.shutdown()
                    return
                }
                input.startsWith("/connect", ignoreCase = true) -> handleConnect(input, node)
                input.isBlank() -> continue
                else -> node.broadcast(input)
            }
        }
    }

    private fun handleConnect(command: String, node: PeerNode) {
        val parts = command.split(" ")
        if (parts.size < 3) {
            logger.warn { "Usage: /connect host port" }
            return
        }
        val host = parts[1]
        val port = parts[2].toIntOrNull()
        if (port == null) {
            logger.warn { "Port must be a number" }
            return
        }
        node.connect(host, port)
    }
}
