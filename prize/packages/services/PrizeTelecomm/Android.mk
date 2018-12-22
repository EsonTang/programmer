ifeq ($(strip $(PRIZE_TELE)),yes)

LOCAL_PATH:= $(call my-dir)

# Build the Telecom service.
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := telephony-common
# Add for access VoIP API,ex.SipManager
LOCAL_JAVA_LIBRARIES += voip-common
# Add for MMI, include the account widget framework
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_STATIC_JAVA_LIBRARIES := \
        guava \
        com.mediatek.telecom.ext \

#PRIZE-Change-DialerV8-wangzhong-2017_7_19-start
#phone_common_dir := ../../apps/PhoneCommon
phone_common_dir := ../../apps/PrizePhoneCommonV8
#PRIZE-Change-DialerV8-wangzhong-2017_7_19-end

res_dirs := res \
            res_ext \
            $(phone_common_dir)/res

LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.android.phone.common

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \
        $(call all-Iaidl-files-under, src) \
        $(call all-proto-files-under, proto)

LOCAL_PROTOC_OPTIMIZE_TYPE := nano
LOCAL_PROTOC_FLAGS := --proto_path=$(LOCAL_PATH)/proto/
LOCAL_PROTO_JAVA_OUTPUT_PARAMS := optional_field_style=accessors

LOCAL_PACKAGE_NAME := Telecom

LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include frameworks/base/packages/SettingsLib/common.mk

include $(BUILD_PACKAGE)

# Build the test package.
include $(call all-makefiles-under,$(LOCAL_PATH))

endif
