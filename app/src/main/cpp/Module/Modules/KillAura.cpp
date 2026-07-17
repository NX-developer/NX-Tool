#include "KillAura.hpp"
#include "../../Sdk/Minecraft.hpp"

void KillAura::onTick() {
    auto client = ClientInstance::get();
    if (!client) return;
    auto player = client->getLocalPlayer();
    if (!player) return;
}
