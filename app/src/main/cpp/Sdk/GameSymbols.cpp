#include "GameSymbols.hpp"
#include "Signatures.hpp"
#include "Offsets.hpp"
#include "../Utils/Memory.hpp"
#include "../Utils/Logger.hpp"

GameSymbols g_syms;

namespace Resolver {

    static int g_resolved = 0;

    static uintptr_t Resolve(uintptr_t base, size_t size, const char* sig) {
        if (sig == nullptr || sig[0] == '\0') return 0;
        uintptr_t addr = Memory::FindSignature(base, size, sig);
        if (addr) g_resolved++;
        return addr;
    }

    void ResolveAll() {
        uintptr_t base = Memory::GetLibraryBase(NX_GAME_LIB);
        size_t size = Memory::GetLibrarySize(NX_GAME_LIB);
        if (!base) {
            NX_LOGE("Cannot resolve, %s not mapped", NX_GAME_LIB);
            return;
        }
        g_resolved = 0;
        g_syms.ClientInstance_getLocalPlayer  = Resolve(base, size, Sig::ClientInstance_getLocalPlayer);
        g_syms.ClientInstance_getMinecraftGame = Resolve(base, size, Sig::ClientInstance_getMinecraftGame);
        g_syms.Player_getPosition   = Resolve(base, size, Sig::Player_getPosition);
        g_syms.Player_setPos        = Resolve(base, size, Sig::Player_setPos);
        g_syms.Player_getLevel      = Resolve(base, size, Sig::Player_getLevel);
        g_syms.Level_getRuntimeActorList = Resolve(base, size, Sig::Level_getRuntimeActorList);
        g_syms.Entity_getPosition   = Resolve(base, size, Sig::Entity_getPosition);
        g_syms.Entity_getNameTag    = Resolve(base, size, Sig::Entity_getNameTag);
        g_syms.GameMode_attack      = Resolve(base, size, Sig::GameMode_attack);
        NX_LOGI("Resolver: %d symbols resolved", g_resolved);
    }

    int ResolvedCount() { return g_resolved; }
}
