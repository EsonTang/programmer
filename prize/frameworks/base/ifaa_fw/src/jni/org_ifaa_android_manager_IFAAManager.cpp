/**
 * @author yangzh6
 *
 * Reference:
 *         [frameworks/base/services/core/jni/onload.cpp]
 */

#define LOG_NDEBUG 0
#define LOG_TAG "IFAAManager-JNI"

#include <binder/IServiceManager.h>
#include <JNIHelp.h>
#include <jni.h>
#include <utils/Log.h>
#include <utils/misc.h>
#include <utils/threads.h>

#include "IIfaaDaemon.h"

using namespace android;

static const char* JNI_REG_CLASS_NAME = "org/ifaa/android/manager/IFAAManager";
static const char* IIfaaDaemonDescriptor = "android.hardware.ifaa.IIfaaDaemon";

class IfaaDaemonClient; // Forward declaration.
Mutex gLock;
sp<IIfaaDaemon> gIfaaDaemon;
sp<IfaaDaemonClient> gIfaaDaemonClient;

// -----------------------------------------------------------------------------------------
class IfaaDaemonClient : public IBinder::DeathRecipient
{
public:
    // DeathRecipient
    virtual void binderDied(const wp<IBinder>& who);
};

void IfaaDaemonClient::binderDied(const wp<IBinder>& who __unused)
{
    Mutex::Autolock _l(gLock);
    gIfaaDaemon.clear();
    ALOGW("IfaaDaemon server died!");
}

// -----------------------------------------------------------------------------------------
static jbyteArray newByteArray(JNIEnv* env, uint8_t* array, uint32_t size_array)
{
    jbyteArray jarray = env->NewByteArray(size_array);
    if(NULL == jarray) {
        ALOGE("NewByteArray(%u) returned NULL", size_array);
        return NULL;
    }

    env->SetByteArrayRegion(jarray, 0, size_array, (jbyte*) array);

    if (env->ExceptionCheck()) {
        return NULL;
    }

    return jarray;
}

const sp<IIfaaDaemon> getIfaaDaemon()
{
    sp<IIfaaDaemon> daemon;
    {
        Mutex::Autolock _l(gLock);
        if (gIfaaDaemon == 0) {
            sp<IServiceManager> sm = defaultServiceManager();
            sp<IBinder> binder;
            do {
                binder = sm->getService(String16(IIfaaDaemonDescriptor));
                if (binder != 0) {
                    break;
                } else {
                    ALOGW("IIfaaDaemon not published, waiting...");
                    usleep(500000); // 0.5s
                }
            } while (true);
            if (gIfaaDaemonClient == NULL) {
                gIfaaDaemonClient = new IfaaDaemonClient();
            }
            binder->linkToDeath(gIfaaDaemonClient);
            gIfaaDaemon = interface_cast<IIfaaDaemon>(binder);
            LOG_ALWAYS_FATAL_IF(gIfaaDaemon == 0);
        }
        daemon = gIfaaDaemon;
    }
    return daemon;
}

static int daemonProcessCmd(const uint8_t* param, size_t size_param, uint8_t** result, size_t* size_result)
{
    int err = 0;

    const sp<IIfaaDaemon>& daemon = getIfaaDaemon();
    if (daemon != 0) {
        err = daemon->processCmd(param, size_param, result, size_result);
    }

    return err;
}

static jbyteArray processCmd(JNIEnv* env, jobject /* clazz */,
        jobject /* jcontext */, jbyteArray jparam)
{
    /** result related */
    int result = 0;
    uint8_t* result_blob = NULL;
    size_t size_result_blob = 0;
    jbyteArray jresult_blob = NULL;

    /** convert param */
    uint32_t size_param = env->GetArrayLength(jparam);
    jbyte* param = (jbyte*) env->GetByteArrayElements(jparam, NULL);

    if (NULL == param) {
        ALOGE("%s GetByteArrayElements returned NULL", __func__);
        goto out;
    }

    /**
     * invoke daemon method.
     *      int processCmd(const uint8_t *param, size_t paramLength, uint8_t **resp, size_t *respLength);
     */
    result = daemonProcessCmd((uint8_t*)param, size_param, &result_blob, &size_result_blob);
    if (result || (NULL == result_blob)) {
        ALOGE("processCmd, result = %d, result_blob = %p", result, result_blob);
        goto out;
    }

    jresult_blob = newByteArray(env, result_blob, (uint32_t)size_result_blob);

out:
    //ALOGE("zyang---, jparam = %s, param = %s, result_blob = %s, jresult_blob = %s", (char*)jparam, (char*)param, (char*)result_blob, (char*)jresult_blob);
    if (result_blob) {
        free(result_blob);
    }
    if (param) {
        env->ReleaseByteArrayElements(jparam, param, 0);
    }

    return jresult_blob;
}

class ScopedLocalRef {
    public:
        ScopedLocalRef(JNIEnv* env, jobject ref) :
            ref_(ref), env_(env) {}

        ~ScopedLocalRef() {
            if (ref_)
                env_->DeleteLocalRef(ref_);
        }

        jobject ref() const {
            return ref_;
        }

    private:
        jobject ref_;
        JNIEnv* env_;
};

static int registerMethods(JNIEnv* env, const char* className, const JNINativeMethod* methodList, int length)
{
    ScopedLocalRef clazz(env, env->FindClass(className));
    if (!clazz.ref()) {
        ALOGE("Can't find class %s", className);
        return -1;
    }

    jint result = env->RegisterNatives((jclass) clazz.ref(), methodList, length);
    if (result != JNI_OK) {
        ALOGE("registerNatives failed, className: %s", className);
        return -1;
    }

    return 0;
}

static JNINativeMethod method_table[] = {
        { "processCmd", "(Landroid/content/Context;[B)[B", (void*) processCmd },
};

extern "C" jint JNI_OnLoad(JavaVM* vm, void* /* reserved */) {
    JNIEnv* env = NULL;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("GetEnv failed!");
        return -1;
    }
    ALOG_ASSERT(env, "Could not retrieve the env!");

    //jniRegisterNativeMethods(env, JNI_REG_CLASS_NAME, method_table,
    //        NELEM(method_table));
    if (registerMethods(env, JNI_REG_CLASS_NAME, method_table, NELEM(method_table))) {
        ALOGE("JNI_OnLoad, registerMethods failed!");
    }

    return JNI_VERSION_1_4;
}
