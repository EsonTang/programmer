ifeq ($(strip $(MTK_YIQI_FONTS_FRAMEWORK_SUPPORT)),yes)
PRODUCT_COPY_FILES += frameworks/base/lovelyfontsframework/clib/lib/libFonts.so:system/lib/libFonts.so
ifneq ($(strip $(TARGET_CPU_ABI_LIST_64_BIT)),"")
PRODUCT_COPY_FILES += frameworks/base/lovelyfontsframework/clib/lib64/libFonts.so:system/lib64/libFonts.so
endif
PRODUCT_COPY_FILES += frameworks/base/lovelyfontsframework/init.lovelyfonts.rc:root/init.lovelyfonts.rc
BOARD_SEPOLICY_DIRS += frameworks/base/lovelyfontsframework/sepolicy
endif

