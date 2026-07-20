package com.nxteam.nxtool

enum class FeatureCategory(val label: String) {
    RELAY("Relay"),
    CHAT("Chat"),
    TOOLS("Tools"),
    COMFORT("Comfort")
}

data class Feature(
    val name: String,
    val description: String,
    val category: FeatureCategory,
    var enabled: Boolean = false
)

object FeatureRegistry {

    val features: List<Feature> = listOf(
        Feature("PacketCapture", "Records packets flowing through the relay", FeatureCategory.RELAY, true),
        Feature("ConnectionStats", "Tracks ping, packet rate and bandwidth", FeatureCategory.RELAY, true),
        Feature("AutoReconnect", "Reconnects after an unexpected disconnect", FeatureCategory.RELAY),
        Feature("SessionRecorder", "Writes the packet log to a file", FeatureCategory.RELAY),

        Feature("ChatLogger", "Keeps a timestamped history of chat", FeatureCategory.CHAT, true),
        Feature("ChatTimestamps", "Shows the arrival time next to messages", FeatureCategory.CHAT),
        Feature("ChatFilter", "Hides repeated or spam messages", FeatureCategory.CHAT),

        Feature("Waypoints", "Saves your own coordinates as named points", FeatureCategory.TOOLS),
        Feature("DeathMarker", "Remembers where you last died", FeatureCategory.TOOLS),
        Feature("ServerList", "Keeps your favourite servers", FeatureCategory.TOOLS, true),

        Feature("AutoSprint", "Keeps sprinting while you move forward", FeatureCategory.COMFORT),
        Feature("KeepAlivePing", "Shows the latency reported by the server", FeatureCategory.COMFORT)
    )

    fun byCategory(category: FeatureCategory): List<Feature> =
        features.filter { it.category == category }

    fun enabledCount(): Int = features.count { it.enabled }

    fun isEnabled(name: String): Boolean =
        features.firstOrNull { it.name == name }?.enabled ?: false
}
