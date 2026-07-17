#include "Memory.hpp"
#include "Logger.hpp"
#include <cstdio>
#include <cstring>
#include <cstdlib>
#include <string>
#include <vector>
#include <unistd.h>
#include <sys/mman.h>

namespace Memory {

    static bool ParseMaps(const char* name, uintptr_t* outStart, uintptr_t* outEnd) {
        FILE* fp = fopen("/proc/self/maps", "r");
        if (!fp) return false;
        char line[512];
        uintptr_t start = 0, end = 0;
        bool found = false;
        while (fgets(line, sizeof(line), fp)) {
            if (strstr(line, name)) {
                uintptr_t s, e;
                if (sscanf(line, "%lx-%lx", &s, &e) == 2) {
                    if (!found) { start = s; found = true; }
                    end = e;
                }
            } else if (found) {
                break;
            }
        }
        fclose(fp);
        if (found) { if (outStart) *outStart = start; if (outEnd) *outEnd = end; }
        return found;
    }

    uintptr_t GetLibraryBase(const char* name) {
        uintptr_t start = 0;
        if (ParseMaps(name, &start, nullptr)) return start;
        return 0;
    }

    uintptr_t GetLibrarySize(const char* name) {
        uintptr_t start = 0, end = 0;
        if (ParseMaps(name, &start, &end)) return end - start;
        return 0;
    }

    bool WaitForLibrary(const char* name, int timeoutSeconds) {
        for (int i = 0; i < timeoutSeconds * 10; i++) {
            if (GetLibraryBase(name) != 0) return true;
            usleep(100000);
        }
        return false;
    }

    static bool MatchPattern(const uint8_t* data, const std::vector<uint8_t>& pattern, const std::vector<uint8_t>& mask) {
        for (size_t i = 0; i < pattern.size(); i++) {
            if (mask[i] && data[i] != pattern[i]) return false;
        }
        return true;
    }

    uintptr_t FindSignature(uintptr_t base, size_t size, const char* pattern) {
        std::vector<uint8_t> bytes;
        std::vector<uint8_t> mask;
        const char* p = pattern;
        while (*p) {
            if (*p == ' ') { p++; continue; }
            if (*p == '?') {
                bytes.push_back(0);
                mask.push_back(0);
                p++;
                if (*p == '?') p++;
            } else {
                bytes.push_back((uint8_t) strtol(std::string(p, 2).c_str(), nullptr, 16));
                mask.push_back(1);
                p += 2;
            }
        }
        if (bytes.empty()) return 0;
        const uint8_t* mem = reinterpret_cast<const uint8_t*>(base);
        for (size_t i = 0; i + bytes.size() <= size; i++) {
            if (MatchPattern(mem + i, bytes, mask)) return base + i;
        }
        return 0;
    }

    void PatchBytes(void* address, const void* data, size_t size) {
        uintptr_t page = reinterpret_cast<uintptr_t>(address) & ~(getpagesize() - 1);
        size_t span = (reinterpret_cast<uintptr_t>(address) + size) - page;
        mprotect(reinterpret_cast<void*>(page), span, PROT_READ | PROT_WRITE | PROT_EXEC);
        memcpy(address, data, size);
        mprotect(reinterpret_cast<void*>(page), span, PROT_READ | PROT_EXEC);
    }
}
