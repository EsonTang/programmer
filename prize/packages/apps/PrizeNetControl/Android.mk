LOCAL_PATH:=$(call my-dir)
include $(CLEAR_VARS)
LOCAL_JAVA_LIBRARIES := bouncycastle \
                        conscrypt \
                        telephony-common \
                        ims-common \
                        mediatek-framework 

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 \
                               android-support-v13 \
                               jsr305 \
                               com.mediatek.lbs.em2.utils \
                               com.mediatek.settings.ext \
                               com.mediatek.keyguard.ext \
							   
LOCAL_MODULE_TAGS:= optional

LOCAL_SRC_FILES:= $(call all-subdir-java-files)
#prize add by lihuangyuan,for whitelist -2017-11-07-start
LOCAL_CERTIFICATE := platform
#prize add by lihuangyuan,for whitelist -2017-11-07-end
LOCAL_PACKAGE_NAME:= PrizeNetControl

include $(BUILD_PACKAGE)