/****************************************************
 ************goodix.com******************

 Edit history:
 zhaoyi add 20140520 draft
 *****************************************************
 **/

#ifndef ANDROID_HARDWARE_IFPSERVICE_H
#define ANDROID_HARDWARE_IFPSERVICE_H

#include <utils/RefBase.h>
#include <binder/IInterface.h>
#include <binder/Parcel.h>

#include "IFpClient.h"
#include "IFp.h"

namespace android {

class IFpService : public IInterface
{
public:
    enum {
        GET_VERSION = IBinder::FIRST_CALL_TRANSACTION,
        CHECK_WORKS,
        GET_FP_STATUS,
        CONNECT
    };

public:
    DECLARE_META_INTERFACE(FpService);

    virtual status_t        check_works(int fp) = 0;
    virtual sp<IFp>     connect(const sp<IFpClient>& cameraClient,
                                    int cameraId) = 0;
};

// ----------------------------------------------------------------------------

class BnFpService: public BnInterface<IFpService>
{
public:
    virtual status_t    onTransact( uint32_t code,
                                    const Parcel& data,
                                    Parcel* reply,
                                    uint32_t flags = 0);
};

}; // namespace android

#endif

