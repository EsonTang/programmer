LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

contacts_common_dir1 := ../../ContactsCommon/ext/src/com/mediatek/contacts/ext/IOp01Extension.java
contacts_common_dir2 := ../../ContactsCommon/ext/src/com/mediatek/contacts/ext/DefaultOp01Extension.java

src_dirs := src $(contacts_common_dir1) $(contacts_common_dir2)
LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))

LOCAL_PACKAGE_NAME := SimProcessor
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_JAVA_LIBRARIES += telephony-common

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)
