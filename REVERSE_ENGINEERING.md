# NX Tool - Reverse Engineering Notes (Minecraft Bedrock 1.26.33.1, arm64-v8a)

## Binary facts
- libminecraftpe.so: 349 MB, ELF64 AArch64, .symtab stripped
- .dynsym has ~78,700 FUNC symbols but they are third-party middleware only
  (v8, cohtml/Gameface, webrtc, Xal, renoir, leveldb, absl). No core game symbols.
- Core game classes are hidden-visibility. dlsym resolution is NOT possible.
- Architecture is ECS-based: LocalPlayerComponent, ActorRotationComponent,
  ActorOwnerComponent, CameraAimAssistComponent, EntityFactory, IStrictTickingSystem.
  Actor access goes through components, not a classic getEntityList.

## RTTI typeinfo anchors (vaddr in the .so)
Jump to these in Ghidra, follow xrefs to the type_info, then to the vtable,
then read virtual function pointers. Cross-reference with a BDS symbol dump
for the closest version to name them.

    14ClientInstance        0x00297d25f
    19ClientInstanceModel   0x002a7e636
    11LocalPlayer           0x00277bb23
    13MinecraftGame         0x0002989bc
    8GameMode               0x002fda5ba
    9HitResult              0x00029d8b1b
    15PlayerInventory       0x0030207cc

## Workflow to make a module functional
1. In Ghidra: Search > For Strings, locate the RTTI name above.
2. Find the type_info referencing the name, then the vtable referencing it.
3. Identify the virtual method you need (getPosition, setPos, attack, etc.),
   cross-checked against a BDS symbol map.
4. Copy a stable byte pattern (AOB) from the function prologue.
5. Paste it into Sdk/Signatures.hpp. The runtime resolver scans it on launch.

## Why signatures over offsets
Raw offsets break on every game update. A prologue signature usually survives
minor updates, so modules keep working without re-dumping every time.
