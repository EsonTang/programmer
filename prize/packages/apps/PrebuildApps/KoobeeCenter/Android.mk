LOCAL_PATH:= $(call my-dir)

#hotknot

#add hotknot start
ifeq ($(L_PBAPK_KOOBEE_CENTER),yes)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := KoobeeCenter
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_MODULE_PATH := $(TARGET_OUT)/priv-app
LOCAL_SRC_FILES :=KoobeeCenter.apk

 ###########################################################system/vendor/operator/app############################
 vendor_operator_app_apks := $(notdir $(shell find $(LOCAL_PATH)/vendor_operator_app -name *.apk))
 $(warning "the value of vendor_operator_app_apks is $(vendor_operator_app_apks)")
 vendor_operator_app_apks_name :=$(patsubst %.apk,%,$(vendor_operator_app_apks))
 
 #vendor_operator_app_apks_notneed_odex_name=
 
 
 ifneq (,$(strip $(vendor_operator_app_apks)))
 $(foreach t,$(vendor_operator_app_apks_name), \
 $(shell mkdir -p $(TARGET_OUT)/vendor/operator/app/$(t)) \
 $(shell cp $(LOCAL_PATH)/vendor_operator_app/$(t).apk $(TARGET_OUT)/vendor/operator/app/$(t)) \
 )
 endif
###########################################################system/vendor/operator/app##########################


include $(BUILD_PREBUILT)
endif

