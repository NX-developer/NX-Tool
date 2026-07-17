#pragma once
#include <cstdint>
#include "Offsets.hpp"
#include "../Utils/Memory.hpp"

struct Vec3 { float x, y, z; };

class Entity {
public:
    Vec3* getPosition();
    const char* getNameTag();
    bool isValid();
};

class Player : public Entity {
public:
    void setPosition(const Vec3& pos);
};

class Level {
public:
    void* getRuntimeActorList();
};

class ClientInstance {
public:
    static ClientInstance* get();
    Player* getLocalPlayer();
    Level* getLevel();
};

namespace GameData {
    uintptr_t Base();
}
