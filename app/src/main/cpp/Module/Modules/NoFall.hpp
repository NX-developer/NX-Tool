#pragma once
#include "../Module.hpp"

class NoFall : public Module {
public:
    NoFall() : Module("NoFall", Category::Player) {}
    void onTick() override;
};
