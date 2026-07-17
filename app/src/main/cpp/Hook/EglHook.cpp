#include "EglHook.hpp"
#include "HookManager.hpp"
#include "../Utils/Logger.hpp"
#include "../Gui/Gui.hpp"

#include <EGL/egl.h>
#include <GLES3/gl3.h>
#include <atomic>

static std::atomic<bool> g_menuOpen{false};
static bool g_guiInit = false;

typedef EGLBoolean (*eglSwapBuffers_t)(EGLDisplay, EGLSurface);
static eglSwapBuffers_t o_eglSwapBuffers = nullptr;

static void UpdateSurfaceSize(EGLDisplay display, EGLSurface surface) {
    EGLint width = 0, height = 0;
    eglQuerySurface(display, surface, EGL_WIDTH, &width);
    eglQuerySurface(display, surface, EGL_HEIGHT, &height);
    Gui::SetDisplaySize((float) width, (float) height);
}

static EGLBoolean h_eglSwapBuffers(EGLDisplay display, EGLSurface surface) {
    if (!g_guiInit) {
        Gui::Init();
        g_guiInit = true;
        NX_LOGI("GUI initialized inside render thread");
    }
    UpdateSurfaceSize(display, surface);
    Gui::Render(g_menuOpen.load());
    return o_eglSwapBuffers(display, surface);
}

namespace EglHook {
    void Install() {
        HookManager::Hook((void*) eglSwapBuffers, (void*) h_eglSwapBuffers, (void**) &o_eglSwapBuffers);
    }
    bool IsMenuOpen() { return g_menuOpen.load(); }
    void ToggleMenu() { g_menuOpen.store(!g_menuOpen.load()); }
}
