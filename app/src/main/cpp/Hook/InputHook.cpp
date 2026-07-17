#include "InputHook.hpp"
#include "HookManager.hpp"
#include "EglHook.hpp"
#include "../Utils/Logger.hpp"
#include "../Gui/Gui.hpp"

#include <android/input.h>

typedef int32_t (*AInputQueue_getEvent_t)(AInputQueue*, AInputEvent**);
static AInputQueue_getEvent_t o_getEvent = nullptr;

typedef int32_t (*AInputQueue_preDispatchEvent_t)(AInputQueue*, AInputEvent*);
static AInputQueue_preDispatchEvent_t o_preDispatch = nullptr;

static void FeedGui(AInputEvent* event) {
    if (AInputEvent_getType(event) != AINPUT_EVENT_TYPE_MOTION) return;
    int32_t action = AMotionEvent_getAction(event) & AMOTION_EVENT_ACTION_MASK;
    float x = AMotionEvent_getX(event, 0);
    float y = AMotionEvent_getY(event, 0);
    bool down = (action == AMOTION_EVENT_ACTION_DOWN || action == AMOTION_EVENT_ACTION_MOVE ||
                 action == AMOTION_EVENT_ACTION_POINTER_DOWN);
    Gui::OnTouch(x, y, down);
}

static int32_t h_getEvent(AInputQueue* queue, AInputEvent** outEvent) {
    int32_t res = o_getEvent(queue, outEvent);
    if (res >= 0 && outEvent && *outEvent) {
        FeedGui(*outEvent);
    }
    return res;
}

static int32_t h_preDispatch(AInputQueue* queue, AInputEvent* event) {
    if (EglHook::IsMenuOpen() && AInputEvent_getType(event) == AINPUT_EVENT_TYPE_MOTION) {
        return 1;
    }
    return o_preDispatch(queue, event);
}

namespace InputHook {
    void Install() {
        HookManager::Hook((void*) AInputQueue_getEvent, (void*) h_getEvent, (void**) &o_getEvent);
        HookManager::Hook((void*) AInputQueue_preDispatchEvent, (void*) h_preDispatch, (void**) &o_preDispatch);
    }
}
