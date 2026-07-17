# NX Tool

In-game injection mod menu for Minecraft Bedrock (Android).

- Target: Minecraft Bedrock 1.26.33.1 (arm64-v8a)
- Injection: LIEF DT_NEEDED patch into libminecraftpe.so, resign APK
- Hook engine: Dobby
- Overlay: ImGui via eglSwapBuffers hook, touch via AInputQueue
- Modules: Module / ModuleManager framework, runtime signature resolver

## Build
Push to GitHub. Actions builds libnxtool.so and the loader APK.

## Inject
```
python patcher/inject.py minecraft.apk out/lib/arm64-v8a/libnxtool.so \
  -o minecraft-nxtool.apk --keystore nx.keystore --ks-pass yourpass --ks-alias nxkey
```
Install minecraft-nxtool.apk, launch, tap the NX button to open the menu.

## Status
Framework is complete and version-agnostic. Cheat modules need real signatures
for 1.26.33.1 in app/src/main/cpp/Sdk/Signatures.hpp (see REVERSE_ENGINEERING.md).
