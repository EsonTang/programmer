LOCAL_PATH := $(call my-dir)

IFAA_JNI_PATH := src/jni

include $(CLEAR_VARS)

LOCAL_MODULE := libifaa_jni

LOCAL_CFLAGS := -Wall -Wextra -Werror -Wunused
LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE)

LOCAL_SRC_FILES := \
	$(IFAA_JNI_PATH)/org_ifaa_android_manager_IFAAManager.cpp

LOCAL_SHARED_LIBRARIES := \
	liblog \
	libcutils \
	libutils \
	libc \
	libbinder \
	libifaa_daemon

include $(BUILD_SHARED_LIBRARY)



############################ build ifaa_fw ############################
 
 
 include $(CLEAR_VARS)

 LOCAL_MODULE := ifaa_fw
 LOCAL_SRC_FILES := $(call all-java-files-under, $(IFAA_JAVA_PATH))
 LOCAL_SHARED_LIBRARIES := libifaa_jni
 LOCAL_MODULE_TAGS := optional

 include $(BUILD_JAVA_LIBRARY)
