#include "Utils/Logger.hpp"
#include "Utils/Memory.hpp"
#include "Hook/HookManager.hpp"
#include "Hook/EglHook.hpp"
#include "Hook/InputHook.hpp"
#include "Module/ModuleManager.hpp"
#include "Sdk/Offsets.hpp"
#include "Sdk/GameSymbols.hpp"

#include <jni.h>
#include <pthread.h>
#include <unistd.h>

static void* InitThread(void*) {
    NX_LOGI("NX Tool starting, target %s (%s)", NX_TARGET_VERSION, NX_TARGET_ABI);

    if (!Memory::WaitForLibrary(NX_GAME_LIB, 60)) {
        NX_LOGE("%s not found, aborting", NX_GAME_LIB);
        return nullptr;
    }
    NX_LOGI("%s base %p", NX_GAME_LIB, (void*) Memory::GetLibraryBase(NX_GAME_LIB));

    Resolver::ResolveAll();
    HookManager::Init();
    ModuleManager::Init();
    EglHook::Install();
    InputHook::Install();

    NX_LOGI("NX Tool ready");
    return nullptr;
}

static void StartInit() {
    pthread_t thread;
    pthread_create(&thread, nullptr, InitThread, nullptr);
    pthread_detach(thread);
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM*, void*) {
    StartInit();
    return JNI_VERSION_1_6;
}

__attribute__((constructor))
static void OnDlOpen() {
    StartInit();
}
