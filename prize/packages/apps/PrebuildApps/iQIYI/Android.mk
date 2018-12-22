LOCAL_PATH:= $(call my-dir)

#add iQIYI start
ifeq ($(strip $(L_PBAPK_QY_VIDEO)), yes)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := iQIYI
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/operator/app
ifeq ($(strip $(PRIZE_CUSTOMER_NAME)), koobee)
LOCAL_SRC_FILES := ./koobee/iQIYI.apk
else
LOCAL_SRC_FILES := ./pcba/iQIYI.apk
endif
LOCAL_MULTILIB := 32
include $(BUILD_PREBUILT)
endif

ifeq ($(strip $(L_PBAPK_QY_VIDEO_SYS)), yes)
include $(CLEAR_VARS)
# Module name should match apk name to be installed
LOCAL_MODULE := iQIYI
LOCAL_MODULE_TAGS := optional

ifeq ($(strip $(PRIZE_CUSTOMER_NAME)), koobee)
LOCAL_SRC_FILES := ./koobee/iQIYI.apk
else
LOCAL_SRC_FILES := ./pcba/iQIYI.apk
endif
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_PATH := $(TARGET_OUT)/prebuilt-app
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_EXTRACT_JNI_LIBS_PRIZE := yes
LOCAL_MULTILIB := 32
include $(BUILD_PREBUILT)
endif
