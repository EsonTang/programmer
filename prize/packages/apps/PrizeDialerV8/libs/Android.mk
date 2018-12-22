ifeq ($(strip $(PRIZE_CONTACTS)),V8)

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := tms.jar

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under, $(LOCAL_PATH))

endif
