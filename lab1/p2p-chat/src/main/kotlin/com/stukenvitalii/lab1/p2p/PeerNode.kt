package com.stukenvitalii.lab1.p2p

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import mu.KotlinLogging

class PeerNode(
    private val nodeName: String,
    private val listenPort: Int
) {
    private val logger = KotlinLogging.logger { }
    private val running = AtomicBoolean(false)
    private val connections = CopyOnWriteArrayList<PeerConnection>()
    private var serverSocket: ServerSocket? = null

    fun start() {
        if (!running.compareAndSet(false, true)) return
        serverSocket = ServerSocket(listenPort)
        thread(name = "peer-accept-$listenPort") { acceptLoop() }
        logger.info { "Node '$nodeName' listens on port $listenPort" }
    }

    fun connect(host: String, port: Int) {
        if (!running.get()) {
            logger.warn { "Start the node before connecting" }
            return
        }
        try {
            val socket = Socket(host, port)
            attachConnection(socket, inbound = false)
        } catch (ex: IOException) {
            logger.error(ex) { "Failed to connect to $host:$port" }
        }
    }

    fun broadcast(message: String) {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) return
        val stale = mutableListOf<PeerConnection>()
        connections.forEach { connection ->
            try {
                connection.writer.println("$nodeName: $trimmed")
            } catch (ex: Exception) {
                stale += connection
                logger.error(ex) { "Failed to send to ${connection.remoteName}" }
            }
        }
        if (stale.isNotEmpty()) {
            connections.removeAll(stale.toSet())
        }
        logger.info { "(local) $nodeName: $trimmed" }
    }

    fun shutdown() {
        if (!running.compareAndSet(true, false)) return
        try {
            serverSocket?.close()
        } catch (ignored: IOException) {
        }
        connections.forEach { it.close() }
        connections.clear()
        logger.info { "Node '$nodeName' stopped" }
    }

    private fun acceptLoop() {
        while (running.get()) {
            try {
                val socket = serverSocket?.accept() ?: break
                attachConnection(socket, inbound = true)
            } catch (_: IOException) {
                if (running.get()) logger.warn { "Failed to accept incoming connection" }
            }
        }
    }

    private fun attachConnection(socket: Socket, inbound: Boolean) {
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val writer = PrintWriter(socket.getOutputStream(), true)
        val remoteName = try {
            if (inbound) {
                val incoming = reader.readLine()?.ifBlank { "peer" } ?: "peer"
                writer.println(nodeName)
                incoming
            } else {
                writer.println(nodeName)
                reader.readLine()?.ifBlank { "peer" } ?: "peer"
            }
        } catch (ex: IOException) {
            logger.error(ex) { "Handshake failed" }
            socket.close()
            return
        }
        val connection = PeerConnection(socket, writer, remoteName)
        connections += connection
        logger.info { "Connected to $remoteName (${socket.inetAddress.hostAddress}:${socket.port})" }
        thread(name = "peer-recv-${socket.port}") {
            listenOnConnection(connection, reader)
        }
    }

    private fun listenOnConnection(connection: PeerConnection, reader: BufferedReader) {
        try {
            while (running.get()) {
                val incoming = reader.readLine() ?: break
                logger.info { incoming }
            }
        } catch (_: IOException) {
            logger.warn { "Connection to ${connection.remoteName} lost" }
        } finally {
            connections -= connection
            connection.close()
            logger.info { "Peer ${connection.remoteName} disconnected" }
        }
    }

    private data class PeerConnection(
        val socket: Socket,
        val writer: PrintWriter,
        val remoteName: String
    ) {
        fun close() {
            try {
                socket.close()
            } catch (_: IOException) {
            }
        }
    }
}
