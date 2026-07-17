cmake_minimum_required(VERSION 3.22.1)
project(nxtool)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include(FetchContent)

set(DOBBY_DEBUG OFF CACHE BOOL "" FORCE)
set(DOBBY_GENERATE_SHARED OFF CACHE BOOL "" FORCE)
FetchContent_Declare(dobby GIT_REPOSITORY https://github.com/jmpews/Dobby.git GIT_TAG master)
FetchContent_MakeAvailable(dobby)

FetchContent_Declare(imgui GIT_REPOSITORY https://github.com/ocornut/imgui.git GIT_TAG v1.91.5)
FetchContent_MakeAvailable(imgui)
set(IMGUI_DIR ${imgui_SOURCE_DIR})

add_library(nxtool SHARED
    Main.cpp
    Utils/Memory.cpp
    Hook/HookManager.cpp
    Hook/EglHook.cpp
    Hook/InputHook.cpp
    Gui/Gui.cpp
    Sdk/Minecraft.cpp
    Sdk/GameSymbols.cpp
    Module/ModuleManager.cpp
    Module/Modules/KillAura.cpp
    Module/Modules/Fly.cpp
    Module/Modules/Speed.cpp
    Module/Modules/NoFall.cpp
    Module/Modules/Esp.cpp
    ${IMGUI_DIR}/imgui.cpp
    ${IMGUI_DIR}/imgui_draw.cpp
    ${IMGUI_DIR}/imgui_tables.cpp
    ${IMGUI_DIR}/imgui_widgets.cpp
    ${IMGUI_DIR}/backends/imgui_impl_opengl3.cpp
)

target_include_directories(nxtool PRIVATE
    ${IMGUI_DIR}
    ${IMGUI_DIR}/backends
    ${dobby_SOURCE_DIR}/include
)

target_compile_definitions(nxtool PRIVATE IMGUI_IMPL_OPENGL_ES3 IMGUI_IMPL_OPENGL_LOADER_CUSTOM)

find_library(log-lib log)
find_library(android-lib android)
find_library(egl-lib EGL)
find_library(gles-lib GLESv3)

target_link_libraries(nxtool dobby ${log-lib} ${android-lib} ${egl-lib} ${gles-lib})
