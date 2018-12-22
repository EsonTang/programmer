ifeq ($(strip $(PRIZE_CAMERA_APP)),DC)
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.camera.ext
LOCAL_STATIC_JAVA_LIBRARIES += mp4parser
LOCAL_STATIC_JAVA_LIBRARIES += xmp_toolkit
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v8-renderscript
#LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-recyclerview
LOCAL_STATIC_JAVA_LIBRARIES += android-ex-camera2
LOCAL_STATIC_JAVA_LIBRARIES += BD_SDK
LOCAL_STATIC_JAVA_LIBRARIES += SUPPORT_V7
LOCAL_STATIC_JAVA_LIBRARIES += libaritygangyunsdk

LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_JAVA_LIBRARIES += telephony-common

LOCAL_RENDERSCRIPT_TARGET_API := 18
LOCAL_RENDERSCRIPT_COMPATIBILITY := 18
LOCAL_RENDERSCRIPT_FLAGS := -rs-package-name=android.support.v8.renderscript

# Keep track of previously compiled RS files too (from bundled GalleryGoogle).
prev_compiled_rs_files := $(call all-renderscript-files-under, src)

# We already have these files from GalleryGoogle, so don't install them.
LOCAL_RENDERSCRIPT_SKIP_INSTALL := $(prev_compiled_rs_files)

LOCAL_SRC_FILES := $(call all-java-files-under, src) $(prev_compiled_rs_files)
#make plugin
LOCAL_SRC_FILES += $(call all-java-files-under, ext/src)
LOCAL_SRC_FILES += ../PrizeDoubleCamera/src/com/mediatek/camera/addition/remotecamera/service/ICameraClientCallback.aidl
LOCAL_SRC_FILES += ../PrizeDoubleCamera/src/com/mediatek/camera/addition/remotecamera/service/IMtkCameraService.aidl
LOCAL_AIDL_INCLUDES += $(LOCAL_PATH)/src

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res $(LOCAL_PATH)/res_ext $(LOCAL_PATH)/res_v2

#LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS := --auto-add-overlay --extra-packages com.android.camera


LOCAL_JNI_SHARED_LIBRARIES := libjni_jpegdecoder


LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#LOCAL_DEX_PREOPT := false
LOCAL_PACKAGE_NAME := PrizeDoubleCamera
LOCAL_OVERRIDES_PACKAGES := Camera Camera2 PrizeCameraV7

LOCAL_JNI_SHARED_LIBRARIES += libgangyun_face_jni libimagealg libpocketsphinx_jni libVGestureDetect liblocSDK6a
LOCAL_MULTILIB := 32
include $(BUILD_PACKAGE)
#####################xucm#########################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := BD_SDK:libs/BaiduLBS-Android.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += SUPPORT_V7:libs/android-support-v7-recyclerview.jar
# gangyun tech add  @{
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += libaritygangyunsdk:libs/gangyunsdk-1.5-20160430.jar
include $(BUILD_MULTI_PREBUILT)
include $(CLEAR_VARS)
LOCAL_MODULE := liblocSDK6a
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_SRC_FILES_arm := libs/armeabi/liblocSDK6a.so
LOCAL_MODULE_SUFFIX := .so
LOCAL_MULTILIB := 32
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := libgangyun_face_jni
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_SRC_FILES_arm := libs/armeabi/libgangyun_face_jni.so
LOCAL_MODULE_SUFFIX := .so
LOCAL_MULTILIB := 32
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := libimagealg
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_SRC_FILES_arm := libs/armeabi/libimagealg.so
LOCAL_MODULE_SUFFIX := .so
LOCAL_MULTILIB := 32
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := libpocketsphinx_jni
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_SRC_FILES_arm := libs/armeabi/libpocketsphinx_jni.so
LOCAL_MODULE_SUFFIX := .so
LOCAL_MULTILIB := 32
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := libVGestureDetect
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_SRC_FILES_arm := libs/armeabi/libVGestureDetect.so
LOCAL_MODULE_SUFFIX := .so
LOCAL_MULTILIB := 32
include $(BUILD_PREBUILT)
# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
#####################xucm#########################
include $(call all-makefiles-under, jni)
include $(call all-makefiles-under, packages/apps/PrizeDoubleCamera/jni_jpegdecoder)
ifeq ($(strip $(LOCAL_PACKAGE_OVERRIDES)),)

# Use the following include to make gallery test apk
include $(call all-makefiles-under, $(LOCAL_PATH))

endif
endif
