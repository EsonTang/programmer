ifeq ($(strip $(PRIZE_CONTACTS)),V8)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

contacts_common_dir := ../PrizeContactsCommonV8.0
phone_common_dir := ../PrizePhoneCommonV8
contacts_ext_dir := ../PrizeContactsCommonV8.0/ext

#ifeq ($(TARGET_BUILD_APPS),)
#support_library_root_dir := frameworks/support
#else
support_library_root_dir := prebuilts/sdk/current/support
#endif

src_dirs := src $(contacts_common_dir)/src $(phone_common_dir)/src $(contacts_ext_dir)/src
res_dirs := res res-aosp $(contacts_common_dir)/res $(contacts_common_dir)/icons/res $(phone_common_dir)/res $(contacts_common_dir)/res_ext
asset_dirs := $(contacts_common_dir)/assets

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_ASSET_DIR := $(addprefix $(LOCAL_PATH)/, $(asset_dirs))
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs)) \
    $(support_library_root_dir)/v7/cardview/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res_ext

#PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -start
LOCAL_SRC_FILES += /src/com/android/dialer/service/ILocationService.aidl
#PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -end

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.android.contacts.common \
    --extra-packages com.android.phone.common \
    --extra-packages android.support.v7.cardview

LOCAL_JAVA_LIBRARIES := telephony-common voip-common
LOCAL_JAVA_LIBRARIES += framework
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_JAVA_LIBRARIES += mediatek-common

LOCAL_STATIC_JAVA_LIBRARIES := \
    com.android.vcard \
    android-common \
    guava \
    android-support-v13 \
    android-support-v7-cardview \
    android-support-v7-palette \
    android-support-v4 \
    libphonenumber \
    libzxing

LOCAL_PACKAGE_NAME := Contacts
LOCAL_CERTIFICATE := shared
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags


#LOCAL_SDK_VERSION := current
LOCAL_MIN_SDK_VERSION := 21

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
endif