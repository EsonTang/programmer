ifeq ($(PRIZE_KOOBEE_CENTER),yes)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PACKAGE_NAME := KoobeeCenter
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_JAVA_LIBRARIES += org.apache.http.legacy
include $(BUILD_PACKAGE)

endif



