LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_LIBRARIES += mediatek-framework telephony-common
LOCAL_PACKAGE_NAME := PrizeImei

LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)
