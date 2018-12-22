ifeq ($(PRIZE_GALLERY2_APP),V8)
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13
LOCAL_STATIC_JAVA_LIBRARIES += com.android.gallery3d.common2
LOCAL_STATIC_JAVA_LIBRARIES += mp4parser
LOCAL_STATIC_JAVA_LIBRARIES += xmp

# glide gif decoder @{
#LOCAL_STATIC_JAVA_LIBRARIES += glide
# @}

LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_JAVA_LIBRARIES += telephony-common

LOCAL_STATIC_JAVA_LIBRARIES += GLIDE
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-recyclerview

LOCAL_SRC_FILES := \
    $(call all-java-files-under, src) \
    $(call all-renderscript-files-under, src)
LOCAL_SRC_FILES += $(call all-java-files-under, src_pd)

# make plugin @{
LOCAL_SRC_FILES += $(call all-java-files-under, ext/src)
# @}

ifeq ($(strip $(MTK_CAM_IMAGE_REFOCUS_SUPPORT)),yes)
    LOCAL_ASSET_DIR += $(LOCAL_PATH)/assets
endif

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res

LOCAL_AAPT_FLAGS := --auto-add-overlay

LOCAL_PACKAGE_NAME := Gallery2

LOCAL_OVERRIDES_PACKAGES := Gallery Gallery3D GalleryNew3D

#LOCAL_SDK_VERSION := current

# If this is an unbundled build (to install seprately) then include
# the libraries in the APK, otherwise just put them in /system/lib and
# leave them out of the APK
ifneq (,$(TARGET_BUILD_APPS))
  LOCAL_JNI_SHARED_LIBRARIES := libjni_eglfence libjni_filtershow_filters libjni_jpegstream
  ifeq ($(strip $(MTK_CAM_IMAGE_REFOCUS_SUPPORT)),yes)
      LOCAL_JNI_SHARED_LIBRARIES += libjni_image_refocus libjni_fv3d libjni_segment libjni_fancycolor
  endif
else
  LOCAL_REQUIRED_MODULES := libjni_eglfence libjni_filtershow_filters libjni_jpegstream
  ifeq ($(strip $(MTK_CAM_IMAGE_REFOCUS_SUPPORT)),yes)
      LOCAL_JNI_SHARED_LIBRARIES += libjni_image_refocus libjni_fv3d libjni_segment libjni_fancycolor
  endif
endif

#REOFUCIMAGE @{
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.ngin3d-static
ifneq ($(strip $(MTK_PLATFORM)),)
LOCAL_JNI_SHARED_LIBRARIES += libja3m 
endif
# @}
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#LOCAL_DEX_PREOPT := false

# mtkgallery @{
LOCAL_SRC_FILES += $(call all-java-files-under, mtkgallery/src)
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/mtkgallery/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/mtkgallery/res_stereo
# @}

LOCAL_JAVA_LIBRARIES += org.apache.http.legacy

LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.omadrm.common

include $(BUILD_PACKAGE)
#####################xucm#########################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := GLIDE:libs/glide-3.7.0.jar
include $(BUILD_MULTI_PREBUILT)
#####################xucm#########################
include $(call all-makefiles-under, jni)

ifeq ($(strip $(MTK_CAM_IMAGE_REFOCUS_SUPPORT)),yes)
    include $(call all-makefiles-under, $(LOCAL_PATH)/jni_stereo)
endif

ifeq ($(strip $(LOCAL_PACKAGE_OVERRIDES)),)

# Use the following include to make gallery test apk
include $(call all-makefiles-under, $(LOCAL_PATH))

endif

endif
