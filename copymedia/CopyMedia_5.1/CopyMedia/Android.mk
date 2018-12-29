LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional


LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) 

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res

LOCAL_JNI_SHARED_LIBRARIES := libjni_copymedia


LOCAL_PACKAGE_NAME := CopyMedia
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := false

LOCAL_PROGUARD_ENABLED := obfuscation
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
