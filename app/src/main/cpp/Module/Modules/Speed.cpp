#include "Speed.hpp"
#include "../../Sdk/Minecraft.hpp"

void Speed::onTick() {
    auto client = ClientInstance::get();
    if (!client) return;
    auto player = client->getLocalPlayer();
    if (!player) return;
}
