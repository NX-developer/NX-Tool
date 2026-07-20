package com.nxteam.nxtool

import android.util.Log
import com.project.lumina.relay.LuminaRelay
import com.project.lumina.relay.address.LuminaAddress
import com.project.lumina.relay.listener.LuminaRelayPacketListener
import com.project.lumina.relay.util.captureLuminaRelay
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

data class ChatLine(val timestamp: Long, val source: String, val message: String)

object RelayManager {

    private const val TAG = "NXTool"
    const val LOCAL_PORT = 19132

    @Volatile private var relay: LuminaRelay? = null

    @Volatile var remoteHost: String = ""
        private set
    @Volatile var remotePort: Int = 19132
        private set
    @Volatile var lastError: String? = null
        private set
    @Volatile var connected: Boolean = false
        private set

    private val chat = ArrayDeque<ChatLine>()

    val running: Boolean get() = relay != null

    val statusText: String
        get() = when {
            !running -> "Not started"
            connected -> "Connected to $remoteHost:$remotePort"
            else -> "Listening on 127.0.0.1:$LOCAL_PORT"
        }

    fun start(host: String, port: Int): Boolean {
        if (running) return true
        remoteHost = host
        remotePort = port
        lastError = null
        return try {
            relay = captureLuminaRelay(
                localAddress = LuminaAddress("0.0.0.0", LOCAL_PORT),
                remoteAddress = LuminaAddress(host, port)
            ) {
                listeners.add(NxPacketListener())
            }
            Log.i(TAG, "Relay started for $host:$port")
            true
        } catch (e: Throwable) {
            lastError = e.message ?: e.javaClass.simpleName
            Log.e(TAG, "Relay failed to start", e)
            relay = null
            false
        }
    }

    fun stop() {
        try {
            relay?.disconnect()
        } catch (e: Throwable) {
            Log.e(TAG, "Relay stop failed", e)
        }
        relay = null
        connected = false
    }

    @Synchronized
    fun chatHistory(): List<ChatLine> = chat.toList().asReversed()

    @Synchronized
    private fun addChat(line: ChatLine) {
        chat.addLast(line)
        while (chat.size > 300) chat.removeFirst()
    }

    @Synchronized
    fun clearChat() = chat.clear()

    private class NxPacketListener : LuminaRelayPacketListener {

        override fun beforeClientBound(packet: BedrockPacket): Boolean {
            connected = true
            record(PacketDirectionTag.TO_CLIENT, packet)
            return false
        }

        override fun beforeServerBound(packet: BedrockPacket): Boolean {
            record(PacketDirectionTag.TO_SERVER, packet)
            return false
        }

        override fun onDisconnect(reason: String) {
            connected = false
            Log.i(TAG, "Relay disconnected: $reason")
            PacketLog.add(PacketDirectionTag.TO_CLIENT, "Disconnect", reason)
        }

        private fun record(direction: PacketDirectionTag, packet: BedrockPacket) {
            if (!FeatureRegistry.isEnabled("PacketCapture")) return
            val type = packet.javaClass.simpleName
            PacketLog.add(direction, type, summarise(packet))
            if (packet is TextPacket && FeatureRegistry.isEnabled("ChatLogger")) {
                val source = packet.sourceName.ifBlank { packet.type?.name ?: "server" }
                addChat(ChatLine(System.currentTimeMillis(), source, packet.message ?: ""))
            }
        }

        private fun summarise(packet: BedrockPacket): String = when (packet) {
            is TextPacket -> "${packet.sourceName}: ${packet.message}".take(120)
            else -> ""
        }
    }
}
