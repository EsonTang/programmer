LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES += $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-query

LOCAL_PACKAGE_NAME := PrizeCloud

LOCAL_CERTIFICATE := platform

LOCAL_OVERRIDES_PACKAGES := PrizeCloud

LOCAL_SDK_VERSION := current

LOCAL_PROGUARD_FLAG_FILES := proguard.cfg

include $(BUILD_PACKAGE)


