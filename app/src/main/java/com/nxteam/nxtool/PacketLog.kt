package com.nxteam.nxtool

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PacketDirectionTag(val label: String) {
    TO_CLIENT("S -> C"),
    TO_SERVER("C -> S")
}

data class PacketEntry(
    val timestamp: Long,
    val direction: PacketDirectionTag,
    val type: String,
    val summary: String
) {
    fun clockTime(): String =
        SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(timestamp))
}

object PacketLog {

    private const val CAPACITY = 600

    private val entries = ArrayDeque<PacketEntry>()
    private val counts = HashMap<String, Int>()

    @Volatile var toClientCount: Long = 0; private set
    @Volatile var toServerCount: Long = 0; private set

    @Synchronized
    fun add(direction: PacketDirectionTag, type: String, summary: String) {
        entries.addLast(PacketEntry(System.currentTimeMillis(), direction, type, summary))
        while (entries.size > CAPACITY) entries.removeFirst()
        counts[type] = (counts[type] ?: 0) + 1
        if (direction == PacketDirectionTag.TO_CLIENT) toClientCount++ else toServerCount++
    }

    @Synchronized
    fun snapshot(filter: String = ""): List<PacketEntry> {
        val list = entries.toList().asReversed()
        if (filter.isBlank()) return list
        return list.filter { it.type.contains(filter, ignoreCase = true) }
    }

    @Synchronized
    fun topTypes(limit: Int = 8): List<Pair<String, Int>> =
        counts.entries.sortedByDescending { it.value }.take(limit).map { it.key to it.value }

    @Synchronized
    fun clear() {
        entries.clear()
        counts.clear()
        toClientCount = 0
        toServerCount = 0
    }

    @Synchronized
    fun exportText(): String = buildString {
        appendLine("NX Tool packet log")
        appendLine("entries=${entries.size} toClient=$toClientCount toServer=$toServerCount")
        appendLine()
        entries.forEach {
            appendLine("${it.clockTime()}  ${it.direction.label}  ${it.type}  ${it.summary}")
        }
    }
}
