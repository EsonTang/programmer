#define LOG_TAG "FpsPolicyService"

#include <stdio.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/ioctl.h>

#include <utils/misc.h>
#include <utils/Log.h>
#include <utils/StrongPointer.h>
#include <utils/Vector.h>

#ifdef MTK_DYNAMIC_FPS_FRAMEWORK_SUPPORT
#include <dfps/FpsInfo.h>
#endif

#include "jni.h"
#include "JNIHelp.h"

#include "com_android_server_input_InputWindowHandle.h"

#include <linux/mtkfb_info.h>

namespace android
{
#ifdef MTK_DYNAMIC_FPS_FRAMEWORK_SUPPORT
static int inited = false;
static sp<FpsInfo> fpsInfo = NULL;
static Mutex lock;

static void init()
{
    inited = true;
    fpsInfo = new FpsInfo();
}
#endif

static void nativeSetInputWindows(JNIEnv* env, jclass /* clazz */,
        jobjectArray windowHandleObjArray) {
#ifdef MTK_DYNAMIC_FPS_FRAMEWORK_SUPPORT
    Vector<sp<InputWindowHandle> > windowHandles;
    Mutex::Autolock l(lock);
    if (!inited) {
        init();
    }

    if (windowHandleObjArray) {
        jsize length = env->GetArrayLength(windowHandleObjArray);
        for (jsize i = 0; i < length; i++) {
            jobject windowHandleObj = env->GetObjectArrayElement(windowHandleObjArray, i);
            if (!windowHandleObj) {
                break; // found null element indicating end of used portion of the array
            }

            sp<InputWindowHandle> windowHandle =
                    android_server_InputWindowHandle_getHandle(env, windowHandleObj);
            if (windowHandle != NULL) {
                windowHandles.push(windowHandle);
            }
            env->DeleteLocalRef(windowHandleObj);
        }
    }

    for (size_t i = 0; i < windowHandles.size(); i++) {
        const InputWindowInfo* info = windowHandles[i]->getInfo();
        if (info != NULL && info->hasFocus) {
            SimpleInputWindowInfo data;
            data.ownerPid = info->ownerPid;
            data.hasFocus = info->hasFocus;
            fpsInfo->setInputWindows(info->name, data);
        }
    }
#endif
}

static void nativeSetWindowFlag(JNIEnv *env, jobject thiz, jint flag, jint mask)
{
#ifdef MTK_DYNAMIC_FPS_FRAMEWORK_SUPPORT
    Mutex::Autolock l(lock);
    if (!inited) {
        init();
    }

    fpsInfo->setWindowFlag(flag, mask);
#endif
}

static JNINativeMethod gMethods[] = {
     { "nativeSetInputWindows", "([Lcom/android/server/input/InputWindowHandle;)V",
             (void*) nativeSetInputWindows },
     { "nativeSetWindowFlag", "(II)V",
             (void*) nativeSetWindowFlag },
};

int register_com_mediatek_fpspolicy_FpsInfo(JNIEnv* env)
{
    return jniRegisterNativeMethods(env, "com/mediatek/fpspolicy/FpsInfo",
            gMethods, NELEM(gMethods));
}

};
