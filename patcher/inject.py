import argparse
import os
import shutil
import subprocess
import sys
import tempfile
import zipfile

INJECT_LIB = "libnxtool.so"
GAME_LIB = "libminecraftpe.so"
ABI = "arm64-v8a"


def run(cmd):
    print("+ " + " ".join(cmd))
    subprocess.run(cmd, check=True)


def patch_needed(game_lib_path):
    import lief
    binary = lief.parse(game_lib_path)
    for entry in binary.dynamic_entries:
        if entry.tag == lief.ELF.DynamicEntry.TAG.NEEDED and entry.name == INJECT_LIB:
            print("Already injected")
            return
    binary.add_library(INJECT_LIB)
    binary.write(game_lib_path)
    print("Added NEEDED " + INJECT_LIB)


def main():
    parser = argparse.ArgumentParser(description="Inject libnxtool.so into a Minecraft Bedrock APK")
    parser.add_argument("apk", help="Path to the base Minecraft APK")
    parser.add_argument("lib", help="Path to libnxtool.so built for " + ABI)
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
        print("ERROR: " + GAME_LIB + " not found for " + ABI, file=sys.stderr)
        sys.exit(1)

    shutil.copy(args.lib, os.path.join(lib_dir, INJECT_LIB))
    patch_needed(game_lib_path)

    unsigned = os.path.join(workdir, "unsigned.apk")
    base_dir = os.getcwd()
    os.chdir(extract_dir)
    with zipfile.ZipFile(unsigned, "w", zipfile.ZIP_DEFLATED) as zf:
        for root, _, files in os.walk("."):
            for name in files:
                full = os.path.join(root, name)
                zf.write(full, os.path.relpath(full, "."))
    os.chdir(base_dir)

    run(["zipalign", "-f", "4", unsigned, args.output])

    if args.keystore:
        run(["apksigner", "sign", "--ks", args.keystore,
             "--ks-pass", "pass:" + (args.ks_pass or ""),
             "--ks-key-alias", args.ks_alias, args.output])
        print("Signed: " + args.output)
    else:
        print("Unsigned (use --keystore to sign): " + args.output)


if __name__ == "__main__":
    main()
