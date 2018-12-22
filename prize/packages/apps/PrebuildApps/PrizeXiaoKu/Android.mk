LOCAL_PATH:= $(call my-dir)

#hotknot
ifeq ($(strip $(PRIZE_XIAOKU)), yes)
#add hotknot start
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := PrizeXiaoKu
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := PRESIGNED
#LOCAL_CERTIFICATE := platform
LOCAL_MODULE_PATH := $(TARGET_OUT)/priv-app
LOCAL_EXTRACT_JNI_LIBS_PRIZE := yes
#LOCAL_MULTILIB := 32
LOCAL_MULTILIB := both
LOCAL_SRC_FILES := PrizeXiaoKu.apk
include $(BUILD_PREBUILT)
endif


