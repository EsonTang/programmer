ifeq ($(strip $(PRIZE_SOUNDRECORDER_APP)),V7)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_SRC_FILES := $(call all-java-files-under, src)

# for mediatek sdk MediaRecorderEx and StorageManagerEx
LOCAL_JAVA_LIBRARIES += mediatek-framework

LOCAL_PACKAGE_NAME := SoundRecorder
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED := disabled
#LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_DEX_PREOPT := false

include $(BUILD_PACKAGE)

include $(call all-makefiles-under, jni)
include $(call all-makefiles-under,$(LOCAL_PATH))

endif
