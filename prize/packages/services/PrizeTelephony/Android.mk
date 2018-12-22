ifeq ($(strip $(PRIZE_TELE)),yes)

LOCAL_PATH:= $(call my-dir)

# Build the Phone app which includes the emergency dialer. See Contacts
# for the 'other' dialer.
include $(CLEAR_VARS)

#PRIZE-Change-DialerV8-wangzhong-2017_7_19-start
#phone_common_dir := ../../apps/PhoneCommon
phone_common_dir := ../../apps/PrizePhoneCommonV8
#PRIZE-Change-DialerV8-wangzhong-2017_7_19-end

src_dirs := src $(phone_common_dir)/src sip/src
res_dirs := res res_ext $(phone_common_dir)/res sip/res

LOCAL_JAVA_LIBRARIES := telephony-common voip-common ims-common

# Add for Plug-in, include the plug-in framework
LOCAL_JAVA_LIBRARIES += mediatek-framework
# Add for IMS provisioning
LOCAL_JAVA_LIBRARIES += ims-config

LOCAL_STATIC_JAVA_LIBRARIES := \
        org.apache.http.legacy \
        guava \
        volley \
        com.mediatek.phone.ext

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_SRC_FILES += \
        src/com/android/phone/EventLogTags.logtags \
        src/com/android/phone/INetworkQueryService.aidl \
        src/com/android/phone/INetworkQueryServiceCallback.aidl
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.android.phone.common \
    --extra-packages com.android.services.telephony.sip

LOCAL_PACKAGE_NAME := TeleService

LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags sip/proguard.flags

include frameworks/base/packages/SettingsLib/common.mk

include $(BUILD_PACKAGE)

# Build the test package
include $(call all-makefiles-under,$(LOCAL_PATH))

endif
