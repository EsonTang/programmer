ifeq ($(strip $(MTK_YIQI_FONTS_FRAMEWORK_SUPPORT)),yes)
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
$(shell mkdir -p $(PRODUCT_OUT)/system/fonts/free)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := LovelyFontContainerService
ifeq ($(findstring 7.,$(PLATFORM_VERSION)),7.)
LOCAL_SRC_FILES := ./70/LovelyFontContainerService.apk
endif
LOCAL_PACKAGE_NAME := LovelyFontContainerService
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_CLASS := APPS
LOCAL_PRIVILEGED_MODULE := true
include $(BUILD_PREBUILT)
include $(call all-makefiles-under, $(LOCAL_PATH))
endif
