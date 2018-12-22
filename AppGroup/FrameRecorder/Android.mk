LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under,src)

LOCAL_PACKAGE_NAME := FrameRecorder

LOCAL_CERTIFICATE := platform
LOCAL_DEX_PREOPT := false
#PROGUARD start
LOCAL_PROGUARD_ENABLED := custom
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#PROGUARD end

#prize-add fix bug[46808]-hpf-2018-1-12-start
LOCAL_STATIC_JAVA_LIBRARIES := \
	android-support-v4 
#prize-add fix bug[46808]-hpf-2018-1-12-end

include $(BUILD_PACKAGE)
# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))

