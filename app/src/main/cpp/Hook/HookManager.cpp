#include "HookManager.hpp"
#include "../Utils/Logger.hpp"
#include <dobby.h>

namespace HookManager {

    bool Hook(void* target, void* replacement, void** original) {
        if (target == nullptr) {
            NX_LOGE("Hook target is null");
            return false;
        }
        int result = DobbyHook(target, replacement, original);
        if (result != 0) {
            NX_LOGE("DobbyHook failed at %p (code %d)", target, result);
            return false;
        }
        NX_LOGI("Hooked %p", target);
        return true;
    }

    void Init() {
        NX_LOGI("HookManager ready");
    }
}
