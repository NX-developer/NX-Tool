#include "Esp.hpp"
#include "../../Sdk/Minecraft.hpp"

void Esp::onTick() {
    auto client = ClientInstance::get();
    if (!client) return;
    auto player = client->getLocalPlayer();
    if (!player) return;
}
