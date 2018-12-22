LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := PrizeAutoTest

LOCAL_CERTIFICATE := platform

LOCAL_DEX_PREOPT := false

#LOCAL_JNI_SHARED_LIBRARIES +=ormlite-android-4.48

#LOCAL_MULTILIB := 32

LOCAL_PROGUARD_ENABLED := custom

LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)

include $(call all-makefiles-under, $(LOCAL_PATH))