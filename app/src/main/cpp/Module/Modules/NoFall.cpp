#include "NoFall.hpp"
#include "../../Sdk/Minecraft.hpp"

void NoFall::onTick() {
    auto client = ClientInstance::get();
    if (!client) return;
    auto player = client->getLocalPlayer();
    if (!player) return;
}
