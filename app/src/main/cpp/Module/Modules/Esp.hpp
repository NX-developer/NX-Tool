#pragma once
#include "../Module.hpp"

class Esp : public Module {
public:
    Esp() : Module("ESP", Category::Render) {}
    void onTick() override;
};
