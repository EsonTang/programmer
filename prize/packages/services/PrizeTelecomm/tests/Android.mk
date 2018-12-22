#
# Copyright (C) 2013 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

ifeq ($(strip $(PRIZE_TELE)),yes)

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_STATIC_JAVA_LIBRARIES := \
        android-ex-camera2 \
        android-support-v4 \
        guava \
        mockito-target \
        platform-test-annotations \
        com.mediatek.telecom.ext

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \
        $(call all-Iaidl-files-under, src) \
        $(call all-java-files-under, ../src) \
        $(call all-proto-files-under, ../proto)

LOCAL_PROTOC_OPTIMIZE_TYPE := nano
LOCAL_PROTOC_FLAGS := --proto_path=$(LOCAL_PATH)/../proto/
LOCAL_PROTO_JAVA_OUTPUT_PARAMS := optional_field_style=accessors

#PRIZE-Change-DialerV8-wangzhong-2017_7_19-start
#phone_common_dir := $(LOCAL_PATH)/../../../apps/PhoneCommon
phone_common_dir := $(LOCAL_PATH)/../../../apps/PrizePhoneCommonV8
#PRIZE-Change-DialerV8-wangzhong-2017_7_19-end



LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/res \
    $(LOCAL_PATH)/../res \
    $(LOCAL_PATH)/../res_ext \
    $(phone_common_dir)/res

LOCAL_JAVA_LIBRARIES := \
        android.test.runner \
        telephony-common

# Add for access VoIP API,ex.SipManager
LOCAL_JAVA_LIBRARIES += voip-common

# Add for MMI, include the account widget framework
LOCAL_JAVA_LIBRARIES += mediatek-framework

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.android.phone.common \
    --extra-packages com.android.server.telecom

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_PACKAGE_NAME := TelecomUnitTests
LOCAL_CERTIFICATE := platform

LOCAL_MODULE_TAGS := tests

LOCAL_JACK_COVERAGE_INCLUDE_FILTER := com.android.server.telecom.*
LOCAL_JACK_COVERAGE_EXCLUDE_FILTER := com.android.server.telecom.tests.*

include frameworks/base/packages/SettingsLib/common.mk

include $(BUILD_PACKAGE)

endif
