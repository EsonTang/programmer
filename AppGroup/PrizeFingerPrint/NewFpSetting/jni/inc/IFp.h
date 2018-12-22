/****************************************************
 ************goodix.com******************

 Edit history:
 zhaoyi add 20140520 draft
 *****************************************************
 **/

#ifndef GOODIX_HARDWARE_IFINGERPRINT_H
#define GOODIX_HARDWARE_IFINGERPRINT_H

#include <utils/RefBase.h>
#include <binder/IInterface.h>
#include <binder/Parcel.h>
#include <binder/IMemory.h>
#include <utils/String8.h>
#include "fp.h"
namespace android {

class IFpClient;

class IFp: public IInterface
{
public:
    DECLARE_META_INTERFACE(Fp);



    // connect new client with existing camera remote
    virtual status_t        connect(const sp<IFpClient>& client) = 0;

virtual       status_t gx_Register() = 0;
virtual       status_t gx_Match() = 0;
virtual       const char* gx_GetInfo() = 0;
virtual       status_t gx_query() = 0;
virtual       status_t gx_EngTest(int cmd) = 0;
virtual       status_t gx_SendCmd(int cmd, const char *arg1,const char *arg2) = 0;
virtual       status_t gx_UnRegister(int index) = 0;
virtual		  status_t gx_ResetRegister() = 0;
virtual status_t   gx_getPermission(const char *pwd) = 0;
virtual status_t   gx_sendScreenState(int state) = 0;//zhaoyi add 20140731, get screen state for set chip work mode
virtual status_t   gx_CheckPWD(const char *pwd) = 0;
virtual status_t   gx_SetPWD(const char *pwd,const char *newPWD) = 0;
virtual status_t   gx_RegisterCancel() = 0;
virtual status_t   gx_MatchCancel() = 0;
virtual status_t   gx_RegisterRollback() = 0;
virtual status_t   gx_RegisterSave(int index) = 0;
//[wangbo add 20150413, for GF818 Heart beat demo
virtual status_t   gx_enableHbRetrieve() = 0;
virtual status_t   gx_disableHbRetrieve() = 0;
//]wangbo add 20150413, for GF818 Heart beat demo
    // tell the camera hal to store meta data or real YUV data in video buffers.
};

// ----------------------------------------------------------------------------

class BnFp: public BnInterface<IFp>
{
public:
    virtual status_t    onTransact( uint32_t code,
                                    const Parcel& data,
                                    Parcel* reply,
                                    uint32_t flags = 0);
};

}; // namespace android

#endif

