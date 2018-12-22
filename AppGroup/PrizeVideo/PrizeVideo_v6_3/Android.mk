LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_PACKAGE_NAME := PrizeVideo
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := custom
LOCAL_PROGUARD_FLAG_FILES := proguard.cfg
LOCAL_PRIVILEGED_MODULE := true
LOCAL_DEX_PREOPT := false
include $(BUILD_PACKAGE)
