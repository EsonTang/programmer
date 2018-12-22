/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "FBC"
#include "utils/Log.h"
#include <dlfcn.h>

#include <stdio.h>

//#include <fcntl.h>      /* open */
//#include <unistd.h>     /* exit */
//#include <sys/ioctl.h>  /* ioctl */

#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"


namespace android
{
#if defined(MTK_PERFSERVICE_SUPPORT)
static int inited = false;

static int (*fbcNotifyIntendedVsync)() = NULL;
static int (*fbcNotifyNoRender)() = NULL;
#endif

typedef int (*notify_intended_vsync)();
typedef int (*notify_no_render)();

#define LIB_FULL_NAME "libfbc.so"

#if defined(MTK_PERFSERVICE_SUPPORT)
static void init()
{
    void *handle, *func;

    // only enter once
    inited = true;

    handle = dlopen(LIB_FULL_NAME, RTLD_NOW);
    if (handle == NULL) {
        ALOGE("Can't load library: %s", dlerror());
        return;
    }

    func = dlsym(handle, "fbcNotifyIntendedVsync");
    fbcNotifyIntendedVsync = reinterpret_cast<notify_intended_vsync>(func);

    if (fbcNotifyIntendedVsync == NULL) {
        ALOGE("fbcNotifyIntendedVsync error: %s", dlerror());
        fbcNotifyIntendedVsync = NULL;
        dlclose(handle);
        return;
    }

    func = dlsym(handle, "fbcNotifyNoRender");
    fbcNotifyNoRender = reinterpret_cast<notify_no_render>(func);

    if (fbcNotifyNoRender == NULL) {
        ALOGE("fbcNotifyNoRender error: %s", dlerror());
        fbcNotifyNoRender = NULL;
        dlclose(handle);
        return;
    }
}
#endif

static int
android_mark_IntendedVsync(JNIEnv *env, jobject thiz)
{
#if defined(MTK_PERFSERVICE_SUPPORT)
    //ALOGE("fbc jni intend vsync: ");
    if (!inited)
        init();

    if (fbcNotifyIntendedVsync) {
        return fbcNotifyIntendedVsync();
    }
    //else {
    //    ALOGE("fbcNotifyIntendedVsync load error");
    //}

#endif
    return -1;
}

static int
android_mark_NoRender(JNIEnv *env, jobject thiz)
{
#if defined(MTK_PERFSERVICE_SUPPORT)
    //ALOGE("fbc jni no render: ");
    if (!inited)
        init();

    if (fbcNotifyNoRender) {
        return fbcNotifyNoRender();
    }
    //else {
    //    ALOGE("fbcNotifyNoRender load error");
    //}

#endif
    return -1;
}

static JNINativeMethod sMethods[] = {
    {"nativeMarkIntendedVsync",   "()I",   (int *)android_mark_IntendedVsync},
    {"nativeMarkNoRender",   "()I",   (int *)android_mark_NoRender},
};

int register_android_view_PerfFrameInfo(JNIEnv* env)
{
    jclass clazz = env->FindClass("android/view/PerfFrameInfo");

    if (clazz == NULL) {
        ALOGE("Can't find android/view/PerfFrameInfo");
        return -1;
    }

    return android::AndroidRuntime::registerNativeMethods(env, "android/view/PerfFrameInfo", sMethods, NELEM(sMethods));
}

}
