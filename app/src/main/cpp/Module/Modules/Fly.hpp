#pragma once
#include "../Module.hpp"

class Fly : public Module {
public:
    Fly() : Module("Fly", Category::Movement) {}
    void onTick() override;
};
