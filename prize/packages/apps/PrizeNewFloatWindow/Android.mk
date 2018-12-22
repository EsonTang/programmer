LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
                              
LOCAL_PACKAGE_NAME := PrizeNewFloatWindow
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_CERTIFICATE := platform
#PROGUARD start
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#PROGUARD end
LOCAL_JAVA_LIBRARIES := mediatek-framework
include frameworks/base/packages/SettingsLib/common.mk


include $(BUILD_PACKAGE)
##################################################
