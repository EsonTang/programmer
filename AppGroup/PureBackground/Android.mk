LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := mediatek-common

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PACKAGE_NAME := PureBackground
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_CERTIFICATE := platform
#PROGUARD start
LOCAL_PROGUARD_ENABLED := custom
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#PROGUARD end

include $(BUILD_PACKAGE)
##################################################
