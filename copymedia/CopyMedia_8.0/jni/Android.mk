LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := com_roco_copymedia_CopyJni.cpp

LOCAL_LDFLAGS	:= -llog 

LOCAL_SHARED_LIBRARIES := \
	libcutils 

 


LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := libjni_copymedia

include $(BUILD_SHARED_LIBRARY)

