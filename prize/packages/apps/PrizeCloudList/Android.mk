ifeq ($(PRIZE_CLOUD_LIST),yes)
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := httpclient-4.5.1:libs/httpclient-4.5.1.jar httpcore-4.4.3:libs/httpcore-4.4.3.jar 
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := httpclient-4.5.1 httpcore-4.4.3  gson_2.2.4

LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := PrizeCloudList
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED := custom
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_DEX_PREOPT := false

include $(BUILD_PACKAGE)

endif
