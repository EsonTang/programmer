LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_JAVA_LIBRARIES := framework
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := src/com/prize/music/IApolloService.aidl
LOCAL_SRC_FILES += $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-query \
	androidslidinguppanel \
	v4 \
	library

#LOCAL_PACKAGE_NAME := ApolloMod

LOCAL_CERTIFICATE := platform
LOCAL_MODULE_PATH := $(TARGET_OUT)/priv-app

LOCAL_PACKAGE_NAME := PrizeMusic

LOCAL_SDK_VERSION := current
LOCAL_DEX_PREOPT := false
LOCAL_PROGUARD_FLAG_FILES := proguard.cfg

include $(BUILD_PACKAGE)

##################################################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := android-query:libs/android-query-0.21.7.jar \
	androidslidinguppanel:libs/androidslidinguppanel-master.jar \
	v4:libs/android-support-v4.jar \
	library:libs/library.jar

include $(BUILD_MULTI_PREBUILT)

