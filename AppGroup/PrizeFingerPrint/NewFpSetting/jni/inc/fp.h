/****************************************************
 ************goodix.com******************

 Edit history:
 zhaoyi add 20140520 draft
 *****************************************************
 **/

#ifndef ANDROID_HARDWARE_FP_H
#define ANDROID_HARDWARE_FP_H

#include <utils/Timers.h>
#include "IFpClient.h"


namespace android {

struct FpInfo {
    /**
     * The direction that the fp faces to. It should be CAMERA_FACING_BACK
     * or CAMERA_FACING_FRONT.
     */
    int facing;

    /**
     * The orientation of the fp image. The value is the angle that the
     * fp image needs to be rotated clockwise so it shows correctly on the
     * display in its natural orientation. It should be 0, 90, 180, or 270.
     *
     * For example, suppose a device has a naturally tall screen. The
     * back-facing fp sensor is mounted in landscape. You are looking at
     * the screen. If the top side of the fp sensor is aligned with the
     * right edge of the screen in natural orientation, the value should be
     * 90. If the top side of a front-facing fp sensor is aligned with the
     * right of the screen, the value should be 270.
     */
    int orientation;
    int mode;
};

class IFpService;
class IFp;
class Mutex;
class String8;

// ref-counted object for callbacks
class FpListener: virtual public RefBase
{
public:
    virtual void notify(int32_t msgType, int32_t ext1, int32_t ext2) = 0;
    virtual void notifyData(int32_t msgType, int32_t length,
                                char *data) = 0;
};

class Fp:public BnFpClient ,public IBinder::DeathRecipient
{
public:
            // construct a fp client from an existing remote
    static  sp<Fp>  create(const sp<IFp>& fp);
    static  int32_t     getNumberOfFps();
    static  status_t    getFpInfo(int fpId,   struct FpInfo* fpInfo);
    static  sp<Fp>  connect(int fpId);
            virtual     ~Fp();
            void        init();
status_t gx_Register();
status_t gx_Match();
const char* gx_GetInfo();
status_t  gx_query();
status_t gx_EngTest(int cmd);
status_t gx_SendCmd(int cmd,const char *arg1,const char *arg2, void *rsp);
status_t gx_UnRegister(int index);
status_t gx_ResetRegister();
status_t gx_getPermission(const char *pwd);
status_t gx_sendScreenState(int state);//zhaoyi add 20140731, get screen state for set chip work mode
status_t   gx_CheckPWD(const char *pwd);
status_t   gx_SetPWD(const char *pwd,const char *newPWD);
status_t   gx_RegisterCancel();
status_t   gx_MatchCancel();
status_t   gx_RegisterRollback();
status_t   gx_RegisterSave(int index);

            status_t    reconnect();
            void        disconnect();
            status_t    lock();
            status_t    unlock();

            status_t    getStatus() { return mStatus; }



            void        setListener(const sp<FpListener>& listener);
    // IFpClient interface
    virtual void        notifyCallback(int32_t msgType, int32_t ext, int32_t ext2);
    virtual void dataCallback(int msgType,
    int length, char *pdata);
    sp<IFp>         remote();

private:
                        Fp();
                        Fp(const Fp&);
                        Fp& operator=(const Fp);
                        virtual void binderDied(const wp<IBinder>& who);

            class DeathNotifier: public IBinder::DeathRecipient
            {
            public:
                DeathNotifier() {
                }

                virtual void binderDied(const wp<IBinder>& who);
            };

            static sp<DeathNotifier> mDeathNotifier;

            // helper function to obtain fp service handle
            static const sp<IFpService>& getFpService();

            sp<IFp>         mFp;
            status_t        mStatus;
            sp<FpListener>  mListener;

            friend class DeathNotifier;

            static  Mutex               mLock;
            static  sp<IFpService>  mFpService;
};

}; // namespace android

#endif

