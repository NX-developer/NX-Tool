#include "Fly.hpp"
#include "../../Sdk/Minecraft.hpp"

void Fly::onTick() {
    auto client = ClientInstance::get();
    if (!client) return;
    auto player = client->getLocalPlayer();
    if (!player) return;
}
