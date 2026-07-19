package com.nxteam.nxtool

enum class ModuleCategory(val label: String) {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    WORLD("World"),
    MISC("Misc")
}

data class ModuleInfo(
    val name: String,
    val description: String,
    val category: ModuleCategory,
    var enabled: Boolean = false
)

object ModuleRegistry {

    val modules: List<ModuleInfo> = listOf(
        ModuleInfo("KillAura", "Attacks nearby targets automatically", ModuleCategory.COMBAT),
        ModuleInfo("Reach", "Extends the attack distance", ModuleCategory.COMBAT),
        ModuleInfo("TriggerBot", "Attacks the entity you look at", ModuleCategory.COMBAT),
        ModuleInfo("Velocity", "Reduces incoming knockback", ModuleCategory.COMBAT),
        ModuleInfo("AutoTotem", "Keeps a totem in the offhand slot", ModuleCategory.COMBAT),

        ModuleInfo("Fly", "Free movement in the air", ModuleCategory.MOVEMENT),
        ModuleInfo("Speed", "Increases movement speed", ModuleCategory.MOVEMENT),
        ModuleInfo("HighJump", "Increases jump height", ModuleCategory.MOVEMENT),
        ModuleInfo("Spider", "Allows climbing walls", ModuleCategory.MOVEMENT),
        ModuleInfo("NoClip", "Passes through blocks", ModuleCategory.MOVEMENT),

        ModuleInfo("NoFall", "Cancels fall damage", ModuleCategory.PLAYER),
        ModuleInfo("AutoSprint", "Always sprints while moving", ModuleCategory.PLAYER),
        ModuleInfo("FastBreak", "Speeds up block breaking", ModuleCategory.PLAYER),
        ModuleInfo("AntiKick", "Filters packets that cause kicks", ModuleCategory.PLAYER),

        ModuleInfo("ESP", "Highlights entities through blocks", ModuleCategory.RENDER),
        ModuleInfo("Tracers", "Draws lines toward entities", ModuleCategory.RENDER),
        ModuleInfo("FullBright", "Removes darkness", ModuleCategory.RENDER),
        ModuleInfo("NameTags", "Enlarges player name tags", ModuleCategory.RENDER),

        ModuleInfo("Nuker", "Breaks blocks around you", ModuleCategory.WORLD),
        ModuleInfo("Scaffold", "Places blocks beneath you", ModuleCategory.WORLD),
        ModuleInfo("ChestStealer", "Empties containers quickly", ModuleCategory.WORLD),

        ModuleInfo("SessionInfo", "Shows relay session details", ModuleCategory.MISC),
        ModuleInfo("PingSpoof", "Alters the reported latency", ModuleCategory.MISC),
        ModuleInfo("CoordLogger", "Records visited coordinates", ModuleCategory.MISC)
    )

    fun byCategory(category: ModuleCategory): List<ModuleInfo> =
        modules.filter { it.category == category }

    fun enabledCount(): Int = modules.count { it.enabled }
}
