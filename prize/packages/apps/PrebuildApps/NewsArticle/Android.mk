LOCAL_PATH:= $(call my-dir)

#add NewsArticle start
ifeq ($(strip $(L_PBAPK_NEWSARTICLE)), yes)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := NewsArticle
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/operator/app
ifeq ($(strip $(PRIZE_CUSTOMER_NAME)), koobee)
LOCAL_SRC_FILES := ./koobee/NewsArticle.apk
else
LOCAL_SRC_FILES := ./pcba/NewsArticle.apk
endif
LOCAL_MULTILIB := 32
include $(BUILD_PREBUILT)
endif

ifeq ($(strip $(L_PBAPK_NEWSARTICLE_SYS)), yes)
ifeq ($(strip $(PRIZE_CUSTOMER_NAME)), koobee)
include $(CLEAR_VARS)
# Module name should match apk name to be installed
LOCAL_MODULE := NewsArticle
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := ./koobee/NewsArticle.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_PATH := $(TARGET_OUT)/prebuilt-app
LOCAL_PREBUILT_JNI_LIBS:= \
	@lib/armeabi/libbitmaps.so \
	@lib/armeabi/libcocklogic.so \
	@lib/armeabi/libgif.so \
	@lib/armeabi/libgifimage.so \
	@lib/armeabi/libimagepipeline.so \
	@lib/armeabi/liblocSDK5.so \
	@lib/armeabi/libmemchunk.so \
	@lib/armeabi/libsupervisor.so \
	@lib/armeabi/libtnet-2.0.17.2-agoo.so \
	@lib/armeabi/libwebp.so \
	@lib/armeabi/libwebpimage.so \
	@lib/armeabi/libweibosdkcore.so 
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_MULTILIB := 32
include $(BUILD_PREBUILT)
else
include $(CLEAR_VARS)
# Module name should match apk name to be installed
LOCAL_MODULE := NewsArticle
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := ./pcba/NewsArticle.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_PATH := $(TARGET_OUT)/prebuilt-app
LOCAL_PREBUILT_JNI_LIBS:= \
	@lib/armeabi/libbitmaps.so \
	@lib/armeabi/libcocklogic.so \
	@lib/armeabi/libgif.so \
	@lib/armeabi/libgifimage.so \
	@lib/armeabi/libimagepipeline.so \
	@lib/armeabi/liblocSDK5.so \
	@lib/armeabi/libmemchunk.so \
	@lib/armeabi/libsupervisor.so \
	@lib/armeabi/libtnet-2.0.17.2-agoo.so \
	@lib/armeabi/libwebp.so \
	@lib/armeabi/libwebpimage.so \
	@lib/armeabi/libweibosdkcore.so 
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_MULTILIB := 32
include $(BUILD_PREBUILT)
endif

endif

