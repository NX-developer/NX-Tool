#pragma once

namespace HookManager {
    bool Hook(void* target, void* replacement, void** original);
    void Init();
}
