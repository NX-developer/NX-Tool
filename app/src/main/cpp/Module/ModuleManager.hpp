#pragma once
#include "Module.hpp"
#include <vector>
#include <memory>

namespace ModuleManager {
    void Init();
    void Tick();
    const std::vector<std::unique_ptr<Module>>& GetModules();
}
