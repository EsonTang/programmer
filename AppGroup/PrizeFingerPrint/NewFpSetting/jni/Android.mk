#
# Copyright 2009 Cedric Priscal
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License. 
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_CXXFLAGS := -DHAVE_PTHREADS
#TARGET_PLATFORM := android-3
#LOCAL_CFLAGS := -mfpu=neon -mfloat-abi=softfp -ftree-vectorize -ffast-math -O2
LOCAL_MODULE    :=  libFp
LOCAL_MULTILIB  := 32
LOCAL_SRC_FILES :=  android_hardware_fpdevice.cpp\

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../inc/native/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../inc/native/include/utils
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../inc/system/core/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../inc/android_runtime
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../inc/android_runtime/nativehelper
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../inc/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/inc/native/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/inc/native/include/utils
LOCAL_C_INCLUDES += $(LOCAL_PATH)/inc/system/core/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/inc/android_runtime
LOCAL_C_INCLUDES += $(LOCAL_PATH)/inc/android_runtime/nativehelper
LOCAL_C_INCLUDES += $(LOCAL_PATH)/inc

LOCAL_LDFLAGS := $(LOCAL_PATH)/libbinder.so
LOCAL_LDFLAGS += $(LOCAL_PATH)/libcutils.so
LOCAL_LDFLAGS += $(LOCAL_PATH)/libhardware.so
LOCAL_LDFLAGS += $(LOCAL_PATH)/libutils.so
LOCAL_LDFLAGS += $(LOCAL_PATH)/libandroid_runtime.so
LOCAL_LDFLAGS += $(LOCAL_PATH)/libfp_client.so
LOCAL_LDFLAGS += $(LOCAL_PATH)/libnativehelper.so

LOCAL_LDLIBS    := -lm -llog
#LOCAL_LDFLAGS := $(LOCAL_PATH)/libbinder.so
include $(BUILD_SHARED_LIBRARY)

