#pragma once

namespace Gui {
    void Init();
    void SetDisplaySize(float width, float height);
    void OnTouch(float x, float y, bool down);
    void Render(bool menuOpen);
}
