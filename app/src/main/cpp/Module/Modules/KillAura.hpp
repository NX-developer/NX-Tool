#pragma once
#include "../Module.hpp"

class KillAura : public Module {
public:
    KillAura() : Module("KillAura", Category::Combat) {}
    void onTick() override;
};
