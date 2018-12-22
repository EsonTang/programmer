/****************************************************
 ************goodix.com******************

 Edit history:
 zhaoyi add 20140520 draft
 *****************************************************
 **/

#ifndef ANDROID_HARDWARE_IFP_APP_H
#define ANDROID_HARDWARE_IFP_APP_H

#include <utils/RefBase.h>
#include <binder/IInterface.h>
#include <binder/Parcel.h>
#include <binder/IMemory.h>
#include <utils/Timers.h>


namespace android {

class IFpClient: public IInterface
{
public:
    DECLARE_META_INTERFACE(FpClient);

    virtual void            notifyCallback(int32_t msgType, int32_t ext1, int32_t ext2) = 0;
};

// ----------------------------------------------------------------------------

class BnFpClient: public BnInterface<IFpClient>
{
public:
    virtual status_t    onTransact( uint32_t code,
                                    const Parcel& data,
                                    Parcel* reply,
                                    uint32_t flags = 0);
};

}; // namespace android

#endif
