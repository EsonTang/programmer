ifeq ($(strip $(LOVELYFONTS_SUPPORT)),yes)
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := LovelyFonts
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := ./yiqifont_mediatek_noicon_cta.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := PRESIGNED
include $(BUILD_PREBUILT)
include $(call all-makefiles-under, $(LOCAL_PATH))
endif
