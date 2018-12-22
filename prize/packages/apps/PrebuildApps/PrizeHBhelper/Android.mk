LOCAL_PATH:= $(call my-dir)

#hotknot
ifeq ($(strip $(PRIZE_HONGBAO_AUTO_HELPER)), yes)
#add hotknot start
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := PrizeHBhelper
LOCAL_MODULE_CLASS := APPS
LOCAL_DEX_PREOPT := false
LOCAL_CERTIFICATE := PRESIGNED
#LOCAL_CERTIFICATE := platform
LOCAL_MODULE_PATH := $(TARGET_OUT)/priv-app
LOCAL_SRC_FILES :=PrizeHBhelper.apk
LOCAL_DEX_PREOPT := false
include $(BUILD_PREBUILT)
endif


