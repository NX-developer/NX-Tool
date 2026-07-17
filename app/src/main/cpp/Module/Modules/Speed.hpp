#pragma once
#include "../Module.hpp"

class Speed : public Module {
public:
    Speed() : Module("Speed", Category::Movement) {}
    void onTick() override;
};
