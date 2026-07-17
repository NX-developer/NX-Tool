#include "Gui.hpp"
#include "../Utils/Logger.hpp"
#include "../Module/ModuleManager.hpp"
#include "../Hook/EglHook.hpp"

#include "imgui.h"
#include "backends/imgui_impl_opengl3.h"

#include <GLES3/gl3.h>
#include <chrono>

static bool g_contextReady = false;
static bool g_backendReady = false;
static float g_width = 1280.0f;
static float g_height = 720.0f;
static float g_touchX = 0.0f;
static float g_touchY = 0.0f;
static bool g_touchDown = false;
static std::chrono::steady_clock::time_point g_lastFrame;

static const char* CategoryName(Category c) {
    switch (c) {
        case Category::Combat: return "Combat";
        case Category::Movement: return "Movement";
        case Category::Player: return "Player";
        case Category::Render: return "Render";
        case Category::World: return "World";
        default: return "Misc";
    }
}

namespace Gui {

    void Init() {
        if (g_contextReady) return;
        IMGUI_CHECKVERSION();
        ImGui::CreateContext();
        ImGuiIO& io = ImGui::GetIO();
        io.IniFilename = nullptr;
        io.MouseDrawCursor = true;
        ImGui::StyleColorsDark();
        ImGuiStyle& style = ImGui::GetStyle();
        style.WindowRounding = 8.0f;
        style.FrameRounding = 6.0f;
        style.ScaleAllSizes(2.5f);
        g_lastFrame = std::chrono::steady_clock::now();
        g_contextReady = true;
    }

    void SetDisplaySize(float width, float height) {
        if (width > 0 && height > 0) { g_width = width; g_height = height; }
    }

    void OnTouch(float x, float y, bool down) {
        g_touchX = x; g_touchY = y; g_touchDown = down;
    }

    static void RenderToggle() {
        ImGui::SetNextWindowPos(ImVec2(24, 24), ImGuiCond_FirstUseEver);
        ImGui::SetNextWindowBgAlpha(0.55f);
        ImGui::Begin("##nxtoggle", nullptr,
                     ImGuiWindowFlags_NoTitleBar | ImGuiWindowFlags_NoResize |
                     ImGuiWindowFlags_AlwaysAutoResize | ImGuiWindowFlags_NoScrollbar |
                     ImGuiWindowFlags_NoNav);
        if (ImGui::Button("NX", ImVec2(130, 90))) {
            EglHook::ToggleMenu();
        }
        ImGui::End();
    }

    static void RenderMenu() {
        ImGui::SetNextWindowSize(ImVec2(g_width * 0.35f, g_height * 0.6f), ImGuiCond_FirstUseEver);
        ImGui::SetNextWindowPos(ImVec2(g_width * 0.05f, g_height * 0.15f), ImGuiCond_FirstUseEver);
        ImGui::Begin("NX Tool");
        ImGui::TextDisabled("Target 1.26.33.1");
        ImGui::Separator();

        Category order[] = { Category::Combat, Category::Movement, Category::Player,
                             Category::Render, Category::World, Category::Misc };
        for (Category cat : order) {
            bool header = false;
            for (auto& m : ModuleManager::GetModules()) {
                if (m->getCategory() != cat) continue;
                if (!header) {
                    if (!ImGui::CollapsingHeader(CategoryName(cat), ImGuiTreeNodeFlags_DefaultOpen)) break;
                    header = true;
                }
                bool enabled = m->isEnabled();
                if (ImGui::Checkbox(m->getName().c_str(), &enabled)) {
                    m->toggle();
                }
            }
        }
        ImGui::End();
    }

    void Render(bool menuOpen) {
        if (!g_contextReady) return;
        if (!g_backendReady) {
            if (!ImGui_ImplOpenGL3_Init("#version 300 es")) {
                NX_LOGE("ImGui GL backend init failed");
                return;
            }
            g_backendReady = true;
            NX_LOGI("ImGui GL backend ready");
        }

        ImGuiIO& io = ImGui::GetIO();
        io.DisplaySize = ImVec2(g_width, g_height);
        auto now = std::chrono::steady_clock::now();
        float delta = std::chrono::duration<float>(now - g_lastFrame).count();
        g_lastFrame = now;
        io.DeltaTime = delta > 0.0f ? delta : 1.0f / 60.0f;
        io.AddMousePosEvent(g_touchX, g_touchY);
        io.AddMouseButtonEvent(0, g_touchDown);

        ImGui_ImplOpenGL3_NewFrame();
        ImGui::NewFrame();

        RenderToggle();
        if (menuOpen) RenderMenu();

        ImGui::Render();
        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
    }
}
