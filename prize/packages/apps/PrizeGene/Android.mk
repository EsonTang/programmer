LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := mediatek-framework

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_LIBRARIES += telephony-common
LOCAL_PACKAGE_NAME := PrizeGene
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := custom
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(call all-makefiles-under, $(LOCAL_PATH))

