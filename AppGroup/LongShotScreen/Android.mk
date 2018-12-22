LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under,src)

LOCAL_PACKAGE_NAME := LongShotScreen

LOCAL_CERTIFICATE := platform
LOCAL_DEX_PREOPT := false
#PROGUARD start
LOCAL_PROGUARD_ENABLED := custom
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#PROGUARD end

include $(BUILD_PACKAGE)
# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))