#include "HookManager.hpp"
#include "../Utils/Logger.hpp"
#include "shadowhook.h"

namespace HookManager {

    bool Hook(void* target, void* replacement, void** original) {
        if (target == nullptr) {
            NX_LOGE("Hook target is null");
            return false;
        }
        void* stub = shadowhook_hook_func_addr(target, replacement, original);
        if (stub == nullptr) {
            int err = shadowhook_get_errno();
            NX_LOGE("shadowhook failed at %p errno=%d (%s)", target, err, shadowhook_to_errmsg(err));
            return false;
        }
        NX_LOGI("Hooked %p", target);
        return true;
    }

    void Init() {
        int r = shadowhook_init(SHADOWHOOK_MODE_UNIQUE, false);
        NX_LOGI("shadowhook_init returned %d", r);
    }
}
