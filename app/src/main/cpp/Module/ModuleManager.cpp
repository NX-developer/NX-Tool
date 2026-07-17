#include "ModuleManager.hpp"
#include "../Utils/Logger.hpp"
#include "Modules/KillAura.hpp"
#include "Modules/Fly.hpp"
#include "Modules/Speed.hpp"
#include "Modules/NoFall.hpp"
#include "Modules/Esp.hpp"

static std::vector<std::unique_ptr<Module>> g_modules;

namespace ModuleManager {

    void Init() {
        g_modules.push_back(std::make_unique<KillAura>());
        g_modules.push_back(std::make_unique<Fly>());
        g_modules.push_back(std::make_unique<Speed>());
        g_modules.push_back(std::make_unique<NoFall>());
        g_modules.push_back(std::make_unique<Esp>());
        NX_LOGI("Loaded %zu modules", g_modules.size());
    }

    void Tick() {
        for (auto& m : g_modules) if (m->isEnabled()) m->onTick();
    }

    const std::vector<std::unique_ptr<Module>>& GetModules() { return g_modules; }
}
