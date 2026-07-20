# Changes relative to upstream

This project is a derivative work of Lumina Client (GPL-3.0).

## NX Tool

- Rebuilt project structure and build system under the NX Tool name
- Replaced Dobby with ShadowHook for the native hook layer
- Added GitHub Actions workflows for library builds and APK patching
- Added a signature based runtime symbol resolver instead of hardcoded offsets
- Added reverse engineering notes for Minecraft Bedrock 1.26.33.1

- Vendored the relay stack from Lumina Client: Lunaris (relay core),
  CloudburstMC Protocol and Network modules, and minecraft-msftauth
- Aligned the toolchain to AGP 8.10.1, Kotlin 2.1.10, compileSdk 35
- Kept the original com.project.lumina.relay package names so that the
  vendored JNI entry points and upstream attribution remain intact

- Replaced the module set with relay tooling and quality of life features:
  packet capture, connection stats, chat logging, waypoints, auto sprint
- Added a relay screen and a live packet inspector

Further changes are listed as they are made.
