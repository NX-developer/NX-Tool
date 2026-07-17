#pragma once
#include <android/log.h>

#define NX_TAG "NXTool"
#define NX_LOGI(...) __android_log_print(ANDROID_LOG_INFO, NX_TAG, __VA_ARGS__)
#define NX_LOGW(...) __android_log_print(ANDROID_LOG_WARN, NX_TAG, __VA_ARGS__)
#define NX_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, NX_TAG, __VA_ARGS__)
