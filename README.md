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
Target must contain libminecraftpe.so (merged universal APK, or split_config.arm64_v8a).
libs-dir is the lib/arm64-v8a folder from the CI artifact (libnxtool.so + libshadowhook.so).
```
python patcher/inject.py minecraft-universal.apk --libs-dir ./out/lib/arm64-v8a \
  -o minecraft-nxtool.apk --keystore nx.keystore --ks-pass yourpass --ks-alias nxkey
```
Install minecraft-nxtool.apk, launch, tap the NX button to open the menu.

## Status
Framework is complete and version-agnostic. Cheat modules need real signatures
for 1.26.33.1 in app/src/main/cpp/Sdk/Signatures.hpp (see REVERSE_ENGINEERING.md).

## License

NX Tool is licensed under the GNU General Public License v3.0. See LICENSE.

This project is a derivative work of Lumina Client
(https://github.com/TheProjectLumina/LuminaClient), also GPL-3.0.
See NOTICE for attribution and CHANGES.md for modifications.
