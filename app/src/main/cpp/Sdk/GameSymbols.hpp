#pragma once
#include <cstdint>

struct GameSymbols {
    uintptr_t ClientInstance_getLocalPlayer = 0;
    uintptr_t ClientInstance_getMinecraftGame = 0;
    uintptr_t Player_getPosition = 0;
    uintptr_t Player_setPos = 0;
    uintptr_t Player_getLevel = 0;
    uintptr_t Level_getRuntimeActorList = 0;
    uintptr_t Entity_getPosition = 0;
    uintptr_t Entity_getNameTag = 0;
    uintptr_t GameMode_attack = 0;
};

extern GameSymbols g_syms;

namespace Resolver {
    void ResolveAll();
    int ResolvedCount();
}
