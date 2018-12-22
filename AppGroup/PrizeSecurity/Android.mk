ifeq ($(PRIZE_PURE_BACKGROUND),yes)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)


LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := PrizeSecurity
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED := custom
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_DEX_PREOPT := false

include $(BUILD_PACKAGE)

endif
