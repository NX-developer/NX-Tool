import argparse
import os
import shutil
import subprocess
import sys
import tempfile
import zipfile

GAME_LIB = "libminecraftpe.so"
INJECT_MAIN = "libnxtool.so"
ABI = "arm64-v8a"
SKIP_LIBS = {"libc++_shared.so"}


def run(cmd):
    print("+ " + " ".join(cmd))
    subprocess.run(cmd, check=True)


def patch_needed(game_lib_path):
    import lief
    binary = lief.parse(game_lib_path)
    for entry in binary.dynamic_entries:
        if entry.tag == lief.ELF.DynamicEntry.TAG.NEEDED and entry.name == INJECT_MAIN:
            print("NEEDED already present")
            return
    binary.add_library(INJECT_MAIN)
    binary.write(game_lib_path)
    print("Added NEEDED " + INJECT_MAIN + " to " + GAME_LIB)


def repack_aligned(src_dir, out_apk):
    unsigned = out_apk + ".unsigned"
    if os.path.exists(unsigned):
        os.remove(unsigned)
    with zipfile.ZipFile(unsigned, "w") as zf:
        for root, _, files in os.walk(src_dir):
            for name in files:
                full = os.path.join(root, name)
                rel = os.path.relpath(full, src_dir)
                if rel.endswith(".so"):
                    zf.write(full, rel, compress_type=zipfile.ZIP_STORED)
                else:
                    zf.write(full, rel, compress_type=zipfile.ZIP_DEFLATED)
    run(["zipalign", "-p", "-f", "4", unsigned, out_apk])
    os.remove(unsigned)


def main():
    parser = argparse.ArgumentParser(
        description="Inject NX Tool libs into a Minecraft Bedrock APK (merged universal or arm64 split)")
    parser.add_argument("apk", help="APK that contains libminecraftpe.so (merged universal or arm64 config split)")
    parser.add_argument("--libs-dir", required=True, help="Folder holding libnxtool.so and libshadowhook.so")
    parser.add_argument("-o", "--output", default="minecraft-nxtool.apk")
    parser.add_argument("--keystore")
    parser.add_argument("--ks-pass")
    parser.add_argument("--ks-alias", default="nxkey")
    args = parser.parse_args()

    workdir = tempfile.mkdtemp(prefix="nxtool_")
    extract_dir = os.path.join(workdir, "apk")
    os.makedirs(extract_dir)

    with zipfile.ZipFile(args.apk, "r") as zf:
        zf.extractall(extract_dir)

    lib_dir = os.path.join(extract_dir, "lib", ABI)
    game_lib_path = os.path.join(lib_dir, GAME_LIB)
    if not os.path.isfile(game_lib_path):
        print("ERROR: " + GAME_LIB + " not found in lib/" + ABI + ".", file=sys.stderr)
        print("Give the merged universal APK or the split_config.arm64_v8a APK.", file=sys.stderr)
        sys.exit(1)

    injected = []
    for name in os.listdir(args.libs_dir):
        if not name.endswith(".so"):
            continue
        if name in SKIP_LIBS or name == GAME_LIB:
            continue
        shutil.copy(os.path.join(args.libs_dir, name), os.path.join(lib_dir, name))
        injected.append(name)
    if INJECT_MAIN not in injected:
        print("ERROR: " + INJECT_MAIN + " not found in --libs-dir", file=sys.stderr)
        sys.exit(1)
    print("Injected: " + ", ".join(sorted(injected)))

    patch_needed(game_lib_path)

    repack_aligned(extract_dir, args.output)

    if args.keystore:
        run(["apksigner", "sign", "--ks", args.keystore,
             "--ks-pass", "pass:" + (args.ks_pass or ""),
             "--ks-key-alias", args.ks_alias, args.output])
        print("Signed: " + args.output)
    else:
        print("Unsigned (pass --keystore to sign): " + args.output)


if __name__ == "__main__":
    main()
