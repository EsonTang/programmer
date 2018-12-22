LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v13
LOCAL_PACKAGE_NAME := PrizePermission
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED := custom
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)
include $(call all-makefiles-under,$(LOCAL_PATH))
