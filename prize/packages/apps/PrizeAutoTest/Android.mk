LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := PrizeAutoTest
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_PATH := $(TARGET_OUT)/priv-app
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_CERTIFICATE := platform
LOCAL_DEX_PREOPT := false

include $(BUILD_PREBUILT)
include $(call all-makefiles-under, $(LOCAL_PATH))
