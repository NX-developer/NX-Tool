#pragma once
#include <cstdint>
#include <cstddef>

namespace Memory {
    uintptr_t GetLibraryBase(const char* name);
    uintptr_t GetLibrarySize(const char* name);
    bool WaitForLibrary(const char* name, int timeoutSeconds);
    uintptr_t FindSignature(uintptr_t base, size_t size, const char* pattern);
    void PatchBytes(void* address, const void* data, size_t size);
}
