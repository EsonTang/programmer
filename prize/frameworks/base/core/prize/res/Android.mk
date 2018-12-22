LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

#include $(LOCAL_PATH)/base_rules.mk
# include apicheck.mk later, we need the build pass to prepare the first version
#include $(LOCAL_PATH)/apicheck.mk

LOCAL_PACKAGE_NAME := prize-framework-res
LOCAL_CERTIFICATE := platform

LOCAL_AAPT_FLAGS := -x3

# Tell aapt to build resource in utf16(the ROM will be enlarged),
# in order to save RAM size for string cache table
ifeq (yes,strip$(MTK_GMO_RAM_OPTIMIZE))
LOCAL_AAPT_FLAGS += --utf16
endif

LOCAL_NO_MTKCUSTOMERRES := true

LOCAL_MODULE_TAGS := optional

# Install this alongside the libraries.
LOCAL_MODULE_PATH := $(TARGET_OUT_JAVA_LIBRARIES)
# Create package-export.apk, which other packages can use to get
# PRODUCT-agnostic resource data like IDs and type definitions.
LOCAL_EXPORT_PACKAGE_RESOURCES := true

include $(BUILD_PACKAGE)

# define a global intermediate target that other module may depend on.
.PHONY: prize-framework-res-package-target
prize-framework-res-package-target: $(LOCAL_BUILT_MODULE)
