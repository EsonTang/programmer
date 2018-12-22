ifeq ($(strip $(PRIZE_COMPASS_APP)), yes)
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := PrizeCompass
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := ./PrizeCompass.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_PREBUILT_JNI_LIBS:= \
@lib/armeabi/liblocSDK6a.so
LOCAL_MULTILIB := 32
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)
endif



