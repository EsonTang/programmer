ifeq ($(strip $(PRIZE_CONTACTS)),V8)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

incallui_dir := InCallUI
#contacts_common_dir := ../ContactsCommon
#phone_common_dir := ../PhoneCommon
contacts_common_dir := ../PrizeContactsCommonV8.0
phone_common_dir := ../PrizePhoneCommonV8

ifeq ($(TARGET_BUILD_APPS),)
support_library_root_dir := frameworks/support
else
support_library_root_dir := prebuilts/sdk/current/support
endif

src_dirs := src \
    $(incallui_dir)/src \
    $(contacts_common_dir)/src \
    $(phone_common_dir)/src

res_dirs := res \
    $(incallui_dir)/res \
    $(contacts_common_dir)/res \
    $(contacts_common_dir)/icons/res \
    $(phone_common_dir)/res

src_dirs += \
    src-N \
    $(incallui_dir)/src-N \
    $(contacts_common_dir)/src-N \
    $(phone_common_dir)/src-N

# M: Add ContactsCommon ext
src_dirs += $(contacts_common_dir)/ext

# M: Add ext resources
res_dirs += res_ext

# M: Add ContactsCommon ext resources
res_dirs += $(contacts_common_dir)/res_ext

# M: Vilte project not support multi-window @{
$(info Vilte $(MTK_VILTE_SUPPORT), 3GVT $(MTK_VT3G324M_SUPPORT))
ifeq (yes, $(filter yes, $(strip $(MTK_VILTE_SUPPORT)) $(strip $(MTK_VT3G324M_SUPPORT))))
res_dirs += $(incallui_dir)/vt_config
$(info disable multi-window for InCallUi $(res_dirs))
endif
# @}

# M: [InCallUI]additional res
res_dirs += $(incallui_dir)/res_ext
# M: [InCallUI]needed by AddMemberEditView who extends MTKRecipientEditTextView
# M: [InCallUI]FIXME: should replace this with google default RecipientEditTextView
res_dirs += ../../../frameworks/ex/chips/res

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs)) \
    $(support_library_root_dir)/v7/cardview/res \
    $(support_library_root_dir)/v7/recyclerview/res \
    $(support_library_root_dir)/v7/appcompat/res \
    $(support_library_root_dir)/design/res
#PRIZE Add CooTek SDK zhoushuanghua 2018_06_21 start
LOCAL_SRC_FILES += /src/com/android/dialer/service/ILocationService.aidl
#PRIZE Add CooTek SDK zhoushuanghua 2018_06_21 end

# M: [InCallUI]added com.android.mtkex.chips for MTKRecipientEditTextView
# M: [InCallUI]FIXME: should replace this with google default RecipientEditTextView
LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages android.support.v7.appcompat \
    --extra-packages android.support.v7.cardview \
    --extra-packages android.support.v7.recyclerview \
    --extra-packages android.support.design \
    --extra-packages com.android.incallui \
    --extra-packages com.android.contacts.common \
    --extra-packages com.android.phone.common \
    --extra-packages com.android.mtkex.chips

LOCAL_JAVA_LIBRARIES := telephony-common ims-common

# M: [InCallUI]additional libraries
LOCAL_JAVA_LIBRARIES += mediatek-framework
# M: Add for ContactsCommon
LOCAL_JAVA_LIBRARIES += voip-common

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-common \
    android-support-v13 \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-cardview \
    android-support-v7-recyclerview \
    android-support-design \
    com.android.vcard \
    guava \
    libphonenumber \
    CooTekPhoneService

#PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-start
LOCAL_STATIC_JAVA_LIBRARIES += tms
#PRIZE-Add-TMSDK_Call_Mark and CooTek SDK zhoushuanghua 2018_06_21-start
LOCAL_JNI_SHARED_LIBRARIES += libTmsdk-2.0.10-mfr \
				 libijkffmpeg \
				 libijksdl \
				 libijkplayer \
				 liblocSDK6a \
				 libsmartdialer_oem_module
#PRIZE-Add-TMSDK_Call_Mark and CooTek SDK zhoushuanghua 2018_06_21-end

#PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-end
#--PRIZE-Add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25-start
#ifeq ($(PRIZE_TTS_SUPPORT),yes)
    LOCAL_STATIC_JAVA_LIBRARIES += SPEAKER
#endif
#--PRIZE-Add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25-end

# M: add mtk-ex
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.dialer.ext

# M: add for WFC support
LOCAL_STATIC_JAVA_LIBRARIES += wfo-common

# M: add for mtk-tatf case
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.tatf.common

# M: [InCallUI]ext library
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.incallui.ext
# M: [InCallUI]added for MTKRecipientEditTextView
# M: [InCallUI]FIXME: should replace this with google default RecipientEditTextView
LOCAL_STATIC_JAVA_LIBRARIES += android-common-chips

LOCAL_PACKAGE_NAME := Dialer
LOCAL_CERTIFICATE := shared
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags $(incallui_dir)/proguard.flags

# Uncomment the following line to build against the current SDK
# This is required for building an unbundled app.
# M: disable it for mediatek's internal function call.
#LOCAL_SDK_VERSION := current

include $(BUILD_PACKAGE)

#PRIZE-Add-TMSDK_Call_Mark and CooTek SDK zhoushuanghua 2018_06_21-start
include $(CLEAR_VARS)
ifeq ($(strip $(TARGET_ARCH)),arm64)
LOCAL_PREBUILT_LIBS :=libTmsdk-2.0.10-mfr:libs/arm64-v8a/libTmsdk-2.0.10-mfr.so
LOCAL_PREBUILT_LIBS +=libijksdl:libs/arm64-v8a/libijksdl.so
LOCAL_PREBUILT_LIBS +=libijkffmpeg:libs/arm64-v8a/libijkffmpeg.so
LOCAL_PREBUILT_LIBS +=libijkplayer:libs/arm64-v8a/libijkplayer.so
LOCAL_PREBUILT_LIBS +=liblocSDK6a:libs/arm64-v8a/liblocSDK6a.so
LOCAL_PREBUILT_LIBS +=libsmartdialer_oem_module:libs/arm64-v8a/libsmartdialer_oem_module.so
else
LOCAL_PREBUILT_LIBS :=libTmsdk-2.0.10-mfr:libs/armeabi/libTmsdk-2.0.10-mfr.so
LOCAL_PREBUILT_LIBS +=libijksdl:libs/armeabi/libijksdl.so
LOCAL_PREBUILT_LIBS +=libijkffmpeg:libs/armeabi/libijkffmpeg.so
LOCAL_PREBUILT_LIBS +=libijkplayer:libs/armeabi/libijkplayer.so
LOCAL_PREBUILT_LIBS +=liblocSDK6a:libs/armeabi/liblocSDK6a.so
LOCAL_PREBUILT_LIBS +=libsmartdialer_oem_module:libs/armeabi/libsmartdialer_oem_module.so
endif
include $(BUILD_MULTI_PREBUILT)
#PRIZE-Add-TMSDK_Call_Mark and CooTek SDK zhoushuanghua 2018_06_21-end

#-- PRIZE-Add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25-start
#ifeq ($(PRIZE_TTS_SUPPORT),yes)
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := SPEAKER:libs/speaker.jar
include $(BUILD_MULTI_PREBUILT)
#endif
#--PRIZE-Add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25-end

#PRIZE Add CooTek SDK zhoushuanghua 2018_06_21 start
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := CooTekPhoneService:libs/CooTekPhoneService.jar
include $(BUILD_MULTI_PREBUILT)
#PRIZE Add CooTek SDK zhoushuanghua 2018_06_21 end

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))

endif
