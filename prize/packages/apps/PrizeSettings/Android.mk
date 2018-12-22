ifeq ($(strip $(PRIZE_SETTINGS)),V8)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
        $(call all-logtags-files-under, src)

LOCAL_MODULE := settings-logtags

include $(BUILD_STATIC_JAVA_LIBRARY)
include $(CLEAR_VARS)

ifeq ($(strip $(MTK_CLEARMOTION_SUPPORT)),no)
# if not support clearmotion, load a small video for clearmotion
LOCAL_ASSET_DIR := $(LOCAL_PATH)/assets_no_clearmotion
else
LOCAL_ASSET_DIR := $(LOCAL_PATH)/assets_clearmotion
endif

LOCAL_JAVA_LIBRARIES := bouncycastle core-oj telephony-common ims-common \
                        mediatek-framework


LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v4 \
    android-support-v13 \
    android-support-v7-recyclerview \
    android-support-v7-preference \
    android-support-v7-appcompat \
    android-support-v14-preference \
    jsr305 \
    settings-logtags \
    com.mediatek.lbs.em2.utils \
    com.mediatek.settings.ext \
    paFaceSDK1.0.0-offLine \
	android-support-v7-cardview
#prize-add-v8.0_wallpaper-yangming-2017_7_20-start
LOCAL_STATIC_JAVA_LIBRARIES += image_loder
#prize-add-v8.0_wallpaper-yangming-2017_7_20-end

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \
        src/com/android/settings/EventLogTags.logtags

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res \
    frameworks/support/v7/preference/res \
    frameworks/support/v14/preference/res \
    frameworks/support/v7/appcompat/res \
    frameworks/support/v7/recyclerview/res \
	frameworks/support/v7/cardview/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res_ext

# Nav bar color customized feature. prize-linkh-2017.07.13
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res_prize

LOCAL_PACKAGE_NAME := Settings
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages android.support.v7.preference:android.support.v14.preference:android.support.v7.appcompat:android.support.v7.recyclerview:android.support.v7.cardview

ifneq ($(INCREMENTAL_BUILDS),)
    LOCAL_PROGUARD_ENABLED := disabled
    LOCAL_JACK_ENABLED := incremental
    LOCAL_DX_FLAGS := --multi-dex
    LOCAL_JACK_FLAGS := --multi-dex native
endif

include frameworks/opt/setupwizard/library/common-full-support.mk
include frameworks/base/packages/SettingsLib/common.mk

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := paFaceSDK1.0.0-offLine:libs/paFaceSDK1.0.0-offLine.jar
include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our test apk.
ifeq (,$(ONE_SHOT_MAKEFILE))
include $(call all-makefiles-under,$(LOCAL_PATH))
endif
endif
