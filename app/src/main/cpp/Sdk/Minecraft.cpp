#include "Minecraft.hpp"
#include "GameSymbols.hpp"
#include "../Utils/Logger.hpp"

namespace GameData {
    static uintptr_t g_base = 0;
    uintptr_t Base() {
        if (g_base == 0) g_base = Memory::GetLibraryBase(NX_GAME_LIB);
        return g_base;
    }
}

Vec3* Entity::getPosition() {
    if (!g_syms.Entity_getPosition) return nullptr;
    return reinterpret_cast<Vec3* (*)(Entity*)>(g_syms.Entity_getPosition)(this);
}

const char* Entity::getNameTag() {
    if (!g_syms.Entity_getNameTag) return "";
    return reinterpret_cast<const char* (*)(Entity*)>(g_syms.Entity_getNameTag)(this);
}

bool Entity::isValid() { return this != nullptr; }

void Player::setPosition(const Vec3& pos) {
    if (!g_syms.Player_setPos) return;
    reinterpret_cast<void (*)(Player*, const Vec3&)>(g_syms.Player_setPos)(this, pos);
}

void* Level::getRuntimeActorList() {
    if (!g_syms.Level_getRuntimeActorList) return nullptr;
    return reinterpret_cast<void* (*)(Level*)>(g_syms.Level_getRuntimeActorList)(this);
}

ClientInstance* ClientInstance::get() {
    if (!Offsets::ClientInstance) return nullptr;
    return *reinterpret_cast<ClientInstance**>(GameData::Base() + Offsets::ClientInstance);
}

Player* ClientInstance::getLocalPlayer() {
    if (!g_syms.ClientInstance_getLocalPlayer) return nullptr;
    return reinterpret_cast<Player* (*)(ClientInstance*)>(g_syms.ClientInstance_getLocalPlayer)(this);
}

Level* ClientInstance::getLevel() {
    auto player = getLocalPlayer();
    if (!player || !g_syms.Player_getLevel) return nullptr;
    return reinterpret_cast<Level* (*)(Player*)>(g_syms.Player_getLevel)(player);
}
