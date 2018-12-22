##########################add by lyt-liup 20130924#############################
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_STATIC_JAVA_LIBRARIES := zxing 
LOCAL_PACKAGE_NAME := PrizeFactoryTest
LOCAL_CERTIFICATE := platform
LOCAL_JAVA_LIBRARIES :=telephony-common
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_PROGUARD_ENABLED := custom

LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
#################################################################################
