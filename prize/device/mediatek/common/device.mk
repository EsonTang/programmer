# this is platform common device config
# you should migrate turnkey alps/build/target/product/common.mk to this file in correct way

# TARGET_PREBUILT_KERNEL should be assigned by central building system
#ifeq ($(TARGET_PREBUILT_KERNEL),)
#LOCAL_KERNEL := device/mediatek/common/kernel
#else
#LOCAL_KERNEL := $(TARGET_PREBUILT_KERNEL)
#endif

#PRODUCT_COPY_FILES += $(LOCAL_KERNEL):kernel

# MediaTek framework base modules
PRODUCT_PACKAGES += \
    mediatek-common \
    mediatek-framework \
    CustomPropInterface \
    mediatek-telephony-common

# yv12 implementation of wallpaper
PRODUCT_PACKAGES += libyv12util

ifeq ($(strip $(OPTR_SPEC_SEG_DEF)),NONE)
PRODUCT_PACKAGES += \
    FwkPlugin
endif


ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
# Override the PRODUCT_BOOT_JARS to include the MediaTek system base modules for global access
PRODUCT_BOOT_JARS += \
    mediatek-common \
    mediatek-framework \

ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
PRODUCT_BOOT_JARS += \
    mediatek-telephony-common
endif

endif

# Telephony
PRODUCT_COPY_FILES += device/mediatek/common/apns-conf.xml:system/etc/apns-conf.xml:mtk
PRODUCT_COPY_FILES += device/mediatek/common/spn-conf.xml:system/etc/spn-conf.xml:mtk

# Audio
ifeq ($(findstring maxim, $(MTK_AUDIO_SPEAKER_PATH)), maxim)
    PRODUCT_PACKAGES += libdsm
    PRODUCT_PACKAGES += libdsmconfigparser
    PRODUCT_PACKAGES += libdsm_interface
else ifeq ($(strip $(MTK_AUDIO_SPEAKER_PATH)),smartpa_nxp_tfa9887)
    PRODUCT_PACKAGES += libtfa9887_interface
else ifeq ($(strip $(MTK_AUDIO_SPEAKER_PATH)),smartpa_nxp_tfa9890)
    PRODUCT_PACKAGES += libtfa9890_interface
else ifeq ($(strip $(MTK_AUDIO_SPEAKER_PATH)),smartpa_richtek_rt5509)
    PRODUCT_PACKAGES += librt_extamp_intf

    ifeq ($(MTK_AUDIO_NUMBER_OF_SPEAKER),)
        PRODUCT_COPY_FILES += \
            vendor/mediatek/proprietary/hardware/smartpa/richtek/rt_mono_device.xml:$(TARGET_COPY_OUT_VENDOR)/etc/smartpa_param/rt_device.xml:mtk
    else ifeq ($(MTK_AUDIO_NUMBER_OF_SPEAKER),$(filter $(MTK_AUDIO_NUMBER_OF_SPEAKER),1))
        PRODUCT_COPY_FILES += \
            vendor/mediatek/proprietary/hardware/smartpa/richtek/rt_mono_device.xml:$(TARGET_COPY_OUT_VENDOR)/etc/smartpa_param/rt_device.xml:mtk
    else ifeq ($(MTK_AUDIO_NUMBER_OF_SPEAKER),$(filter $(MTK_AUDIO_NUMBER_OF_SPEAKER),2))
        PRODUCT_COPY_FILES += \
            vendor/mediatek/proprietary/hardware/smartpa/richtek/rt_multi_device.xml:$(TARGET_COPY_OUT_VENDOR)/etc/smartpa_param/rt_device.xml:mtk
    endif
    PRODUCT_COPY_FILES += \
        $(call add-to-product-copy-files-if-exists, $(MTK_TARGET_PROJECT_FOLDER)/rt_device.xml:$(TARGET_COPY_OUT_VENDOR)/etc/smartpa_param/rt_device.xml:mtk)

    PRODUCT_COPY_FILES += \
        vendor/mediatek/proprietary/hardware/smartpa/richtek/rt5509_calibration:$(TARGET_COPY_OUT_VENDOR)/bin/rt5509_calibration:mtk
    PRODUCT_COPY_FILES += \
        device/mediatek/$(shell echo $(MTK_PLATFORM) | tr '[A-Z]' '[a-z]')/smartpa_param/rt5509_param:$(TARGET_COPY_OUT_VENDOR)/etc/smartpa_param/rt5509_param:mtk
    PRODUCT_COPY_FILES += \
        $(call add-to-product-copy-files-if-exists, $(MTK_TARGET_PROJECT_FOLDER)/rt5509_param:$(TARGET_COPY_OUT_VENDOR)/etc/smartpa_param/rt5509_param:mtk)
endif

PRODUCT_COPY_FILES += device/mediatek/common/audio_em.xml:$(TARGET_COPY_OUT_VENDOR)/etc/audio_em.xml:mtk

RAT_CONFIG = $(strip $(MTK_PROTOCOL1_RAT_CONFIG))
ifneq (,$(RAT_CONFIG))
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_protocol1_rat_config=$(RAT_CONFIG)
  ifneq (,$(findstring C,$(RAT_CONFIG)))
    # C2K is supported
    RAT_CONFIG_C2K_SUPPORT=yes
  endif
  ifneq (,$(findstring L,$(RAT_CONFIG)))
    # LTE is supported
    RAT_CONFIG_LTE_SUPPORT=yes
  endif
endif

# For C2K CDMA feature file
ifeq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
PRODUCT_COPY_FILES += frameworks/native/data/etc/android.hardware.telephony.cdma.xml:system/etc/permissions/android.hardware.telephony.cdma.xml
endif

ifeq ($(strip $(MTK_TELEPHONY_FEATURE_SWITCH_DYNAMICALLY)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_telephony_switch=1
endif

ifeq ($(strip $(MTK_MP2_PLAYBACK_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_support_mp2_playback=1
endif

ifeq ($(strip $(MTK_AUDIO_ALAC_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_audio_alac_support=1
endif

#MTB
PRODUCT_PACKAGES += mtk_setprop

#MMS
ifeq ($(strip $(MTK_BASIC_PACKAGE)), yes)
    ifndef MTK_TB_WIFI_3G_MODE
        PRODUCT_PACKAGES += messaging
    else
        ifeq ($(strip $(MTK_TB_WIFI_3G_MODE)), 3GDATA_SMS)
            PRODUCT_PACKAGES += messaging
        endif
    endif
endif

ifeq ($(strip $(MTK_BSP_PACKAGE)), yes)
    ifndef MTK_TB_WIFI_3G_MODE
        PRODUCT_PACKAGES += messaging
    else
        ifeq ($(strip $(MTK_TB_WIFI_3G_MODE)), 3GDATA_SMS)
            PRODUCT_PACKAGES += messaging
        endif
    endif
endif

ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
    ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
        ifneq ($(strip $(MTK_A1_FEATURE)), yes)
            ifndef MTK_TB_WIFI_3G_MODE
                PRODUCT_PACKAGES += MtkMms
            else
                ifeq ($(strip $(MTK_TB_WIFI_3G_MODE)), 3GDATA_SMS)
                    PRODUCT_PACKAGES += MtkMms
                endif
            endif
        endif
    endif
endif

ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
    ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
        PRODUCT_PACKAGES += MtkCalendar
#       PRODUCT_PACKAGES += MtkBrowser
#       PRODUCT_PACKAGES += MtkQuickSearchBox
#       PRODUCT_PACKAGES += MtkWebView
    endif
endif

# Telephony begin
PRODUCT_PACKAGES += muxreport
PRODUCT_PACKAGES += mtkrild
PRODUCT_PACKAGES += mtk-ril
PRODUCT_PACKAGES += libutilrilmtk
PRODUCT_PACKAGES += gsm0710muxd
PRODUCT_PACKAGES += mtkrildmd2
PRODUCT_PACKAGES += mtk-rilmd2
PRODUCT_PACKAGES += librilmtkmd2
PRODUCT_PACKAGES += gsm0710muxdmd2
PRODUCT_PACKAGES += KoobeeUsermanual
ifeq ($(strip $(MTK_RIL_MODE)), c6m_1rild)
    PRODUCT_PACKAGES += libmtk-ril
    PRODUCT_PACKAGES += mtkfusionrild
    PRODUCT_PACKAGES += librilfusion
    PRODUCT_PACKAGES += riltest
    ifeq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
        PRODUCT_PACKAGES += libvia-ril
    endif
endif

PRODUCT_PACKAGES += md_minilog_util
PRODUCT_PACKAGES += SimRecoveryTestTool
PRODUCT_PACKAGES += ppl_agent
PRODUCT_PACKAGES += libratconfig
# External SIM support
ifeq ($(strip $(MTK_EXTERNAL_SIM_SUPPORT)), yes)
    PRODUCT_PACKAGES += libvsim-adaptor-client
endif

# Remote SIM unlock
ifeq ($(strip $(SIM_ME_LOCK_MODE)), 1)
    PRODUCT_PACKAGES += libsimmelock
endif

ifeq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
#For C2K RIL
PRODUCT_PACKAGES += \
          viarild \
          libc2kril \
          libviatelecom-withuim-ril \
          viaradiooptions \
          librpcril \
          ctclient

#Set CT6M_SUPPORT
ifeq ($(strip $(CT6M_SUPPORT)), yes)
PRODUCT_PACKAGES += CdmaSystemInfo
PRODUCT_PROPERTY_OVERRIDES += ro.ct6m_support=1
  ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
    ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
      PRODUCT_COPY_FILES += vendor/mediatek/proprietary/frameworks/base/telephony/etc/spn-conf-op09.xml:$(TARGET_COPY_OUT_VENDOR)/etc/spn-conf-op09.xml:mtk
    endif
  endif
endif

#For PPPD
PRODUCT_PACKAGES += \
          ip-up-cdma \
          ipv6-up-cdma \
          link-down-cdma \
          pppd_via

#For C2K control modules
PRODUCT_PACKAGES += \
          libc2kutils \
          flashlessd \
          statusd
endif

ifeq ($(MTK_RIL_MODE), c6m_1rild)
# IMSM(93)
PRODUCT_PACKAGES += volte_imsm_93
else
# MAL shared library
PRODUCT_PACKAGES += libmdfx
PRODUCT_PACKAGES += libmal_mdmngr
PRODUCT_PACKAGES += libmal_nwmngr
PRODUCT_PACKAGES += libmal_rilproxy
PRODUCT_PACKAGES += libmal_simmngr
PRODUCT_PACKAGES += libmal_datamngr
PRODUCT_PACKAGES += libmal_rds
PRODUCT_PACKAGES += libmal_epdga
PRODUCT_PACKAGES += libmal_imsmngr
PRODUCT_PACKAGES += libmal

PRODUCT_PACKAGES += volte_imsm
PRODUCT_PACKAGES += volte_imspa

# MAL-Dongle shared library
PRODUCT_PACKAGES += libmd_mdmngr
PRODUCT_PACKAGES += libmd_rilproxy
PRODUCT_PACKAGES += libmd_simmngr
PRODUCT_PACKAGES += libmd_datamngr
PRODUCT_PACKAGES += libmd_nwmngr
PRODUCT_PACKAGES += libmd

# # Volte IMS shared library
PRODUCT_PACKAGES += volte_imspa_md

# Add for (VzW) chipset test
ifneq ($(strip $(MTK_VZW_CHIPTEST_MODE_SUPPORT)), 0)
PRODUCT_PACKAGES += libatch
PRODUCT_PACKAGES += libatcputil
PRODUCT_PACKAGES += atcp
PRODUCT_PACKAGES += libswext_plugin
PRODUCT_PACKAGES += libnetmngr_plugin

PRODUCT_PACKAGES += liblannetmngr_core
PRODUCT_PACKAGES += liblannetmngr_api
PRODUCT_PACKAGES += lannetmngrd
PRODUCT_PACKAGES += lannetmngr_test
endif

endif

# VoLTE Process
ifeq ($(strip $(MTK_IMS_SUPPORT)),yes)
PRODUCT_PACKAGES += Gba
PRODUCT_PACKAGES += libipsec_ims_shr

ifneq ($(MTK_RIL_MODE), c6m_1rild)
# MAL Process
PRODUCT_PACKAGES += mtkmal

# # Volte IMS Dongle Process
PRODUCT_PACKAGES += volte_imsm_md
endif


else
    ifeq ($(strip $(MTK_EPDG_SUPPORT)),yes) # EPDG without IMS

    ifneq ($(MTK_RIL_MODE), c6m_1rild)
    # MAL Process
    PRODUCT_PACKAGES += mtkmal

    # # Volte IMS Dongle Process
#    PRODUCT_PACKAGES += volte_imsm_md
    endif

    endif
endif

# include init.volte.rc
ifeq ($(MTK_IMS_SUPPORT),yes)
    ifneq ($(wildcard $(MTK_TARGET_PROJECT_FOLDER)/init.volte.rc),)
        PRODUCT_COPY_FILES += $(MTK_TARGET_PROJECT_FOLDER)/init.volte.rc:root/init.volte.rc
    else
        ifneq ($(wildcard $(MTK_PROJECT_FOLDER)/init.volte.rc),)
            PRODUCT_COPY_FILES += $(MTK_PROJECT_FOLDER)/init.volte.rc:root/init.volte.rc
        else
            PRODUCT_COPY_FILES += device/mediatek/common/init.volte.rc:root/init.volte.rc
        endif
    endif
endif

#include multi_init.rc in meta mode and factory mode.
PRODUCT_COPY_FILES += device/mediatek/common/multi_init.rc:root/multi_init.rc

PRODUCT_PACKAGES += llibmtk_vt_swip
PRODUCT_PACKAGES += libmtk_vt_utils
PRODUCT_PACKAGES += libmtk_vt_wrapper
PRODUCT_PACKAGES += libmtk_vt_service
PRODUCT_PACKAGES += vtservice

# WFCA Process
ifeq ($(strip $(MTK_WFC_SUPPORT)),yes)
  PRODUCT_PACKAGES += wfca
endif


# Hwui program binary service
PRODUCT_PACKAGES += program_binary_service
PRODUCT_PACKAGES += program_binary_builder

ifeq ($(strip $(MTK_RCS_SUPPORT)),yes)
PRODUCT_PACKAGES += Gba
endif

ifeq ($(strip $(MTK_PRIVACY_PROTECTION_LOCK)),yes)
  PRODUCT_PACKAGES += PrivacyProtectionLock
  PRODUCT_PROPERTY_OVERRIDES += persist.sys.prize_fp_password = 6
  #add by lijimeng 20161205 persist.sys.prize_fp_password bugid:25907
endif

ifeq ($(strip $(MTK_USB_CBA_SUPPORT)),yes)
  PRODUCT_PACKAGES += UsbChecker
endif

ifeq ($(strip $(GOOGLE_RELEASE_RIL)), yes)
    PRODUCT_PACKAGES += libril
else
    PRODUCT_PACKAGES += librilmtk
endif

ifeq ($(strip $(PRIZE_BACK_CAMERA_SUB_YUV)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_rear_camera_sub_yuv=1
endif
ifeq ($(strip $(PRIZE_FRONT_CAMERA_SUB_YUV)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_front_camera_sub_yuv=1
endif
# Telephony end

#prize-Add for gangyun tech beauty-pengcancan-20170112-start
ifeq ($(strip $(PRIZE_CAMERA_APP)),DC)
    PRODUCT_PACKAGES += PrizeDoubleCamera
else
ifeq ($(strip $(PRIZE_CAMERA_APP)),V7)
    PRODUCT_PACKAGES += PrizeCameraV7
else
ifeq ($(strip $(PRIZE_CAMERA_APP)),yes)
    PRODUCT_PACKAGES += PrizeCamera
else
    # For MTK Camera
    PRODUCT_PACKAGES += Camera
endif
endif
endif
ifeq ($(strip $(GANGYUN_CAMERA_BEAUTY)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.gangyun_camera_beauty=1
  PRODUCT_PACKAGES += GYBeautySnap_V1.2
endif
ifeq ($(strip $(GANGYUN_BOKEH_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.gangyun_camera_bokeh=1
  PRODUCT_PACKAGES += GYBeautySnap_V1.2
  #PRODUCT_PACKAGES +=gyBeautySnap
endif
ifeq ($(strip $(GANGYUN_BOKEH_SUPPORT)), yes) 
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_gangyun_bokeh=true
    ifeq ($(strip $(GANGYUN_BOKEH_BLUR_SUPPORT)), yes) 
    PRODUCT_PROPERTY_OVERRIDES += ro.gangyun_camera_blur_show=1
endif
endif
#prize-Add for gangyun tech beauty-pengcancan-20170112-end

#PRIZE-xuchunming-20171123-arcsoft switch-start
 ifeq ($(strip $(PRIZE_SUPERZOOM_FROM_ARCSOFT)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize.superzoom.arcsoft=1
endif
 
ifeq ($(strip $(PRIZE_NIGHTSHOT_FROM_ARCSOFT)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize.nightshot.arcsoft=1
endif
 
ifeq ($(strip $(PRIZE_HDR_FROM_ARCSOFT)),yes)
     PRODUCT_PROPERTY_OVERRIDES += ro.prize.hdr.arcsoft=1
endif
 
ifeq ($(strip $(PRIZE_SELFIE_FROM_ARCSOFT)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize.selfie.arcsoft=1
endif
#PRIZE-xuchunming-20171123-arcsoft switch-end
 
#PRIZE-xuchunming-20171123-Brand watermark switch-start
 ifeq ($(strip $(PRIZE_BRAND_WATERMARK)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize.brand.watermark=1
endif
#PRIZE-xuchunming-20171123-Brand watermark switch-end

#PRIZE-xiaoping-20180413-arc_fcam_superzoom switch-start
 ifeq ($(strip $(PRIZE_ARC_FCAM_SUPERZOOM)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize.arc.fcam.superzoom=1
endif
#PRIZE-xiaoping-20180413-arc_fcam_superzoom switch-end

ifeq ($(strip $(PRIZE_SHORT_VIDEO)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize.short.video.mode=1
endif

#PRIZE-xiaoping-20180428-portrait mode switch-start
ifeq ($(strip $(PRIZE_PORTRAIT_SUPPORT)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize.portrait.mode=1
endif
#PRIZE-xiaoping-20180428-portrait mode switch-end
# PRODUCT_PACKAGES += Panorama
# PRODUCT_PACKAGES += NativePip
# PRODUCT_PACKAGES += SlowMotion
# PRODUCT_PACKAGES += CameraRoot

PRODUCT_DEFAULT_PROPERTY_OVERRIDES += camera.disable_zsl_mode=1

PRODUCT_PACKAGES += libBnMtkCodec
PRODUCT_PACKAGES += MtkCodecService
PRODUCT_PACKAGES += autokd
RODUCT_PACKAGES += \
    dhcp6c \
    dhcp6ctl \
    dhcp6c.conf \
    dhcp6cDNS.conf \
    dhcp6s \
    dhcp6s.conf \
    dhcp6c.script \
    dhcp6cctlkey \
    libifaddrs

# meta tool
ifeq ($(MTK_INTERNAL),yes)
ifneq ($(wildcard vendor/mediatek/proprietary/buildinfo/label.ini),)
  include vendor/mediatek/proprietary/buildinfo/label.ini
  ifeq ($(MTK_BUILD_VERNO),ALPS.W10.24.p0)
    MTK_BUILD_VERNO := $(MTK_INTERNAL_BUILD_VERNO)
  endif
  ifeq ($(MTK_WEEK_NO),W10.24)
    MTK_WEEK_NO := $(MTK_INTERNAL_WEEK_NO)
  endif
endif
endif
$(call inherit-product-if-exists, vendor/mediatek/proprietary/buildinfo/branch.mk)
PRODUCT_PROPERTY_OVERRIDES += ro.mediatek.version.release=$(strip $(MTK_BUILD_VERNO))
PRODUCT_PROPERTY_OVERRIDES += ro.mediatek.version.sdk=4

# To specify customer's releasekey
ifeq ($(MTK_INTERNAL),yes)
  PRODUCT_DEFAULT_DEV_CERTIFICATE := device/mediatek/common/security/releasekey
else
  ifeq ($(MTK_SIGNATURE_CUSTOMIZATION),yes)
    ifeq ($(wildcard device/mediatek/common/security/$(strip $(MTK_TARGET_PROJECT))),)
      $(error Please create device/mediatek/common/security/$(strip $(MTK_TARGET_PROJECT))/ and put your releasekey there!!)
    else
      PRODUCT_DEFAULT_DEV_CERTIFICATE := device/mediatek/common/security/$(strip $(MTK_TARGET_PROJECT))/releasekey
    endif
  else
#   Not specify PRODUCT_DEFAULT_DEV_CERTIFICATE and the default testkey will be used.
  endif
endif

# Handheld core hardware
PRODUCT_COPY_FILES += frameworks/native/data/etc/handheld_core_hardware.xml:system/etc/permissions/handheld_core_hardware.xml

# Bluetooth Low Energy Capability
PRODUCT_COPY_FILES += frameworks/native/data/etc/android.hardware.bluetooth_le.xml:system/etc/permissions/android.hardware.bluetooth_le.xml

# Bluetooth DUN profile
ifeq ($(MTK_BT_BLUEDROID_DUN_GW_12),yes)
PRODUCT_PROPERTY_OVERRIDES += bt.profiles.dun.enabled=1
PRODUCT_PACKAGES += pppd_btdun libpppbtdun.so
endif

# Bluetooth BIP profile cover art feature
ifeq ($(MTK_BT_BLUEDROID_AVRCP_TG_16),yes)
  PRODUCT_PROPERTY_OVERRIDES += bt.profiles.bip.coverart.enable=1
endif

# Customer configurations
ifneq ($(wildcard $(MTK_TARGET_PROJECT_FOLDER)/custom.conf),)
PRODUCT_COPY_FILES += $(MTK_TARGET_PROJECT_FOLDER)/custom.conf:$(TARGET_COPY_OUT_VENDOR)/etc/custom.conf:mtk
else
ifdef OPTR_SPEC_SEG_DEF
    ifneq ($(strip $(OPTR_SPEC_SEG_DEF)),NONE)
        OPTR := $(word 1,$(subst _,$(space),$(OPTR_SPEC_SEG_DEF)))
        SPEC := $(word 2,$(subst _,$(space),$(OPTR_SPEC_SEG_DEF)))
        SEG  := $(word 3,$(subst _,$(space),$(OPTR_SPEC_SEG_DEF)))
        ifneq ($(wildcard vendor/mediatek/proprietary/operator/$(OPTR)/$(SPEC)/$(SEG)/custom.conf),)
        PRODUCT_COPY_FILES += vendor/mediatek/proprietary/operator/$(OPTR)/$(SPEC)/$(SEG)/custom.conf:$(TARGET_COPY_OUT_VENDOR)/etc/custom.conf:mtk
        else
        PRODUCT_COPY_FILES += device/mediatek/common/custom.conf:$(TARGET_COPY_OUT_VENDOR)/etc/custom.conf:mtk
        endif
    else
        PRODUCT_COPY_FILES += device/mediatek/common/custom.conf:$(TARGET_COPY_OUT_VENDOR)/etc/custom.conf:mtk
    endif
else
    PRODUCT_COPY_FILES += device/mediatek/common/custom.conf:$(TARGET_COPY_OUT_VENDOR)/etc/custom.conf:mtk
endif
endif

# Recovery
PRODUCT_COPY_FILES += $(MTK_PROJECT_FOLDER)/recovery.fstab:$(TARGET_COPY_OUT_VENDOR)/etc/recovery.fstab:mtk

ifndef MTK_PLATFORM_DIR
  ifneq ($(wildcard device/mediatek/$(MTK_PLATFORM)),)
    MTK_PLATFORM_DIR = $(MTK_PLATFORM)
  else
    MTK_PLATFORM_DIR = $(shell echo $(MTK_PLATFORM) | tr '[A-Z]' '[a-z]')
  endif
endif

ifeq ($(wildcard device/mediatek/$(MTK_PLATFORM_DIR)),)
  $(error the platform dir changed, expected: device/mediatek/$(MTK_PLATFORM_DIR), please check manually)
endif

# GMS interface
ifdef BUILD_GMS
ifeq ($(strip $(BUILD_GMS)), yes)
$(call inherit-product-if-exists, vendor/google/products/gms.mk)

PRODUCT_PROPERTY_OVERRIDES += \
      ro.com.google.clientidbase=alps-$(TARGET_PRODUCT)-{country} \
      ro.com.google.clientidbase.ms=alps-$(TARGET_PRODUCT)-{country} \
      ro.com.google.clientidbase.yt=alps-$(TARGET_PRODUCT)-{country} \
      ro.com.google.clientidbase.am=alps-$(TARGET_PRODUCT)-{country} \
      ro.com.google.clientidbase.gmm=alps-$(TARGET_PRODUCT)-{country}
endif
endif

# prebuilt interface
$(call inherit-product-if-exists, vendor/mediatek/common/device-vendor.mk)

# Prize aw87329 bin file hjian 20180309
PRODUCT_COPY_FILES += device/mediatek/common/audio_param_awpa/aw87339_kspk.bin:system/etc/firmware/aw87339_kspk.bin:mtk
PRODUCT_COPY_FILES += device/mediatek/common/audio_param_awpa/aw87339_drcv.bin:system/etc/firmware/aw87339_drcv.bin:mtk
PRODUCT_COPY_FILES += device/mediatek/common/audio_param_awpa/aw87339_abrcv.bin:system/etc/firmware/aw87339_abrcv.bin:mtk
PRODUCT_COPY_FILES += device/mediatek/common/audio_param_awpa/aw87339_rcvspk.bin:system/etc/firmware/aw87339_rcvspk.bin:mtk

#M:chenteng-20180515-Add AudioParamVersion Info File
PRODUCT_COPY_FILES += device/mediatek/common/audio_param/AudioParamVersionInfo.txt:system/vendor/etc/audio_param/AudioParamVersionInfo.txt:mtk

# mtklog config
ifeq ($(strip $(MTK_BASIC_PACKAGE)), yes)
ifeq ($(TARGET_BUILD_VARIANT),eng)
PRODUCT_COPY_FILES += device/mediatek/common/mtklog/mtklog-config-basic-eng.prop:$(TARGET_COPY_OUT_VENDOR)/etc/mtklog-config.prop:mtk
else
PRODUCT_COPY_FILES += device/mediatek/common/mtklog/mtklog-config-basic-user.prop:$(TARGET_COPY_OUT_VENDOR)/etc/mtklog-config.prop:mtk
endif
else
ifeq ($(TARGET_BUILD_VARIANT),eng)
PRODUCT_COPY_FILES += device/mediatek/common/mtklog/mtklog-config-bsp-eng.prop:$(TARGET_COPY_OUT_VENDOR)/etc/mtklog-config.prop:mtk
else
PRODUCT_COPY_FILES += device/mediatek/common/mtklog/mtklog-config-bsp-user.prop:$(TARGET_COPY_OUT_VENDOR)/etc/mtklog-config.prop:mtk
endif
endif

# ECC List Customization
$(call inherit-product-if-exists, vendor/mediatek/proprietary/external/EccList/EccList.mk)

#fonts
$(call inherit-product-if-exists, frameworks/base/data/fonts/fonts.mk)
$(call inherit-product-if-exists, external/naver-fonts/fonts.mk)
$(call inherit-product-if-exists, external/noto-fonts/fonts.mk)
$(call inherit-product-if-exists, external/roboto-fonts/fonts.mk)
$(call inherit-product-if-exists, frameworks/base/data/fonts/openfont/fonts.mk)

#3Dwidget
$(call inherit-product-if-exists, vendor/mediatek/proprietary/frameworks/base/3dwidget/appwidget.mk)

# AAPT Config
$(call inherit-product-if-exists, device/mediatek/common/aapt/aapt_config.mk)

#
# MediaTek Operator features configuration
#

ifdef OPTR_SPEC_SEG_DEF
  ifneq ($(strip $(OPTR_SPEC_SEG_DEF)),NONE)
    OPTR := $(word 1,$(subst _,$(space),$(OPTR_SPEC_SEG_DEF)))
    SPEC := $(word 2,$(subst _,$(space),$(OPTR_SPEC_SEG_DEF)))
    SEG  := $(word 3,$(subst _,$(space),$(OPTR_SPEC_SEG_DEF)))
    $(call inherit-product-if-exists, vendor/mediatek/proprietary/operator/$(OPTR)/$(SPEC)/$(SEG)/optr_apk_config.mk)

    PRODUCT_PROPERTY_OVERRIDES += \
      persist.operator.optr=$(OPTR) \
      persist.operator.spec=$(SPEC) \
      persist.operator.seg=$(SEG)
  endif
endif

# Here we initializes variable MTK_REGIONAL_OP_PACK based on Carrier express pack
ifdef MTK_CARRIEREXPRESS_PACK
  ifeq ($(strip $(MTK_CARRIEREXPRESS_PACK)),la)
    MTK_REGIONAL_OP_PACK = OP112_SPEC0200_SEGDEFAULT OP120_SPEC0100_SEGDEFAULT OP15_SPEC0200_SEGDEFAULT
  else ifeq ($(strip $(MTK_CARRIEREXPRESS_PACK)),na)
    MTK_REGIONAL_OP_PACK = OP07_SPEC0407_SEGDEFAULT OP08_SPEC0200_SEGDEFAULT
  else ifeq ($(strip $(MTK_CARRIEREXPRESS_PACK)),eu)
    MTK_REGIONAL_OP_PACK = OP03_SPEC0200_SEGDEFAULT OP05_SPEC0200_SEGDEFAULT OP06_SPEC0106_SEGDEFAULT OP11_SPEC0200_SEGDEFAULT OP15_SPEC0200_SEGDEFAULT OP16_SPEC0200_SEGDEFAULT
  else ifeq ($(strip $(MTK_CARRIEREXPRESS_PACK)),ind)
    MTK_REGIONAL_OP_PACK = OP18_SPEC0100_SEGDEFAULT
  else ifeq ($(strip $(MTK_CARRIEREXPRESS_PACK)),jpn)
    MTK_REGIONAL_OP_PACK = OP17_SPEC0200_SEGDEFAULT
  else ifeq ($(strip $(MTK_CARRIEREXPRESS_PACK)),mea)
    MTK_REGIONAL_OP_PACK = OP126_SPEC0100_SEGDEFAULT
  else ifeq ($(strip $(MTK_CARRIEREXPRESS_PACK)),au)
    MTK_REGIONAL_OP_PACK = OP19_SPEC0200_SEGDEFAULT
  else ifeq ($(strip $(MTK_CARRIEREXPRESS_PACK)),rus)
    MTK_REGIONAL_OP_PACK = OP127_SPEC0200_SEGDEFAULT OP113_SPEC0200_SEGDEFAULT
  else ifneq ($(strip $(MTK_CARRIEREXPRESS_PACK)),no)
    $(error "MTK_CARRIEREXPRESS_PACK: $(MTK_CARRIEREXPRESS_PACK) not supported")
  endif
endif

ifdef MTK_CARRIEREXPRESS_PACK
  ifneq ($(strip $(MTK_CARRIEREXPRESS_PACK)),no)
      PRODUCT_PROPERTY_OVERRIDES += ro.mtk_carrierexpress_pack=$(strip $(MTK_CARRIEREXPRESS_PACK))
      ifeq ($(strip $(MTK_CARRIEREXPRESS_APK_INSTALL_SUPPORT)),yes)
        PRODUCT_PROPERTY_OVERRIDES += ro.mtk_carrierexpress_inst_sup=1
      endif
      ifeq ($(strip $(MTK_CARRIEREXPRESS_SWITCH_MODE)),2)
        PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cxp_switch_mode=2
      endif
      PRODUCT_PACKAGES += usp_service
      PRODUCT_PACKAGES += libusp_native
      temp_optr := $(OPTR_SPEC_SEG_DEF)
      $(foreach OP_SPEC, $(MTK_REGIONAL_OP_PACK), \
        $(eval OPTR_SPEC_SEG_DEF := $(OP_SPEC)) \
        $(eval OPTR     := $(word 1, $(subst _,$(space),$(OP_SPEC)))) \
        $(eval SPEC     := $(word 2, $(subst _,$(space),$(OP_SPEC)))) \
        $(eval SEG      := $(word 3, $(subst _,$(space),$(OP_SPEC)))) \
        $(eval include vendor/mediatek/proprietary/operator/$(OPTR)/$(SPEC)/$(SEG)/optr_apk_config.mk))
      OPTR_SPEC_SEG_DEF := $(temp_optr)
  endif
endif

PRODUCT_PACKAGES += DataTransfer

# add for OMA DM, common module used by MediatekDM & red bend DM
PRODUCT_PACKAGES += dm_agent_binder

# red bend DM config files & lib
ifeq ($(strip $(MTK_DM_APP)),yes)
    PRODUCT_PACKAGES += reminder.xml
    PRODUCT_PACKAGES += tree.xml
    PRODUCT_PACKAGES += DmApnInfo.xml
    PRODUCT_PACKAGES += vdmconfig.xml
    PRODUCT_PACKAGES += libvdmengine.so
    PRODUCT_PACKAGES += libvdmfumo.so
    PRODUCT_PACKAGES += libvdmlawmo.so
    PRODUCT_PACKAGES += libvdmscinv.so
    PRODUCT_PACKAGES += libvdmscomo.so
    PRODUCT_PACKAGES += dm
endif

# MediatekDM package & lib
ifeq ($(strip $(MTK_MDM_APP)),yes)
    PRODUCT_PACKAGES += MediatekDM
endif

# CTM
ifeq ($(strip $(MTK_CTM_SUPPORT)),yes)
PRODUCT_PACKAGES += ctm
PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ctm_flag=0
endif
# SmsReg package
ifeq ($(strip $(MTK_SMSREG_APP)),yes)
    PRODUCT_PACKAGES += SmsReg
endif

ifeq ($(strip $(MTK_CMCC_FT_PRECHECK_SUPPORT)),yes)
  PRODUCT_PACKAGES += FTPreCheck
endif

ifeq ($(strip $(OPTR_SPEC_SEG_DEF)),OP09_SPEC0212_SEGDEFAULT)
    PRODUCT_PACKAGES += ConfigureCheck
else
    ifeq ($(strip $(OPTR_SPEC_SEG_DEF)), OP09_SPEC0212_SEGC)
        PRODUCT_PACKAGES += ConfigureCheck
    endif
endif

$(call inherit-product-if-exists, vendor/mediatek/proprietary/frameworks/base/voicecommand/cfg/voicecommand.mk)

ifeq ($(strip $(MTK_VOICE_UNLOCK_SUPPORT)),yes)
    PRODUCT_PACKAGES += VoiceCommand
else
    ifeq ($(strip $(MTK_VOICE_UI_SUPPORT)),yes)
        PRODUCT_PACKAGES += VoiceCommand
    else
            ifeq ($(strip $(MTK_VOW_SUPPORT)),yes)
                PRODUCT_PACKAGES += VoiceCommand
            endif
    endif
endif

ifeq ($(strip $(MTK_VOICE_UNLOCK_SUPPORT)),yes)
    PRODUCT_PACKAGES += VoiceUnlock
else
    ifeq ($(strip $(MTK_VOW_SUPPORT)),yes)
        PRODUCT_PACKAGES += VoiceUnlock
        PRODUCT_PACKAGES += MtkVoiceWakeupInteraction
    endif
endif

ifeq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
    PRODUCT_PACKAGES += c2k-ril-prop
    PRODUCT_PACKAGES += via-plugin
endif

ifeq ($(strip $(MTK_USB_CBA_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_usb_cba_support=1
endif

ifeq ($(strip $(MTK_FOTA_SUPPORT)), yes)
   PRODUCT_PACKAGES += fota1
endif

ifeq ($(strip $(MTK_NUM_MODEM_PROTOCOL)), 1)
  PRODUCT_PROPERTY_OVERRIDES += ro.num_md_protocol=1
endif
ifeq ($(strip $(MTK_NUM_MODEM_PROTOCOL)), 2)
  PRODUCT_PROPERTY_OVERRIDES += ro.num_md_protocol=2
endif
ifeq ($(strip $(MTK_NUM_MODEM_PROTOCOL)), 3)
  PRODUCT_PROPERTY_OVERRIDES += ro.num_md_protocol=3
endif
ifeq ($(strip $(MTK_NUM_MODEM_PROTOCOL)), 4)
  PRODUCT_PROPERTY_OVERRIDES += ro.num_md_protocol=4
endif

ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), ss)
  PRODUCT_PROPERTY_OVERRIDES += persist.radio.multisim.config=ss
endif
ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), dsds)
  PRODUCT_PROPERTY_OVERRIDES += persist.radio.multisim.config=dsds
endif
ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), dsda)
  PRODUCT_PROPERTY_OVERRIDES += persist.radio.multisim.config=dsda
endif
ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), tsts)
  PRODUCT_PROPERTY_OVERRIDES += persist.radio.multisim.config=tsts
endif
ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), qsqs)
  PRODUCT_PROPERTY_OVERRIDES += persist.radio.multisim.config=qsqs
endif

ifeq ($(strip $(MTK_AUDIO_PROFILES)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_audio_profiles=1
endif

ifeq ($(strip $(MTK_AUDENH_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_audenh_support=1
endif

# MTK_LOSSLESS_BT
ifeq ($(strip $(MTK_LOSSLESS_BT_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_lossless_bt_audio=1
endif

# MTK_LOUNDNESS
ifeq ($(strip $(MTK_BESLOUDNESS_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_besloudness_support=1
endif

# MTK_BESSURROUND
ifeq ($(strip $(MTK_BESSURROUND_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_bessurround_support=1
endif

# MTK_ANC
ifeq ($(strip $(MTK_HEADSET_ACTIVE_NOISE_CANCELLATION)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_active_noise_cancel=1
endif

ifeq ($(strip $(MTK_MEMORY_COMPRESSION_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_mem_comp_support=1
endif

ifeq ($(strip $(MTK_WAPI_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_wapi_support=1
endif

ifeq ($(strip $(MTK_BT_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_bt_support=1
endif

ifeq ($(strip $(MTK_WAPPUSH_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_wappush_support=1
endif

ifeq ($(strip $(MTK_AGPS_APP)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_agps_app=1
endif

ifeq ($(strip $(MTK_FM_TX_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_fm_tx_support=1
endif

ifeq ($(strip $(MTK_VT3G324M_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_vt3g324m_support=1
endif

ifeq ($(strip $(MTK_VOICE_UI_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_voice_ui_support=1
endif

ifeq ($(strip $(MTK_VOICE_UNLOCK_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_voice_unlock_support=1
endif



ifneq ($(MTK_AUDIO_TUNING_TOOL_VERSION),)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_audio_tuning_tool_ver=$(strip $(MTK_AUDIO_TUNING_TOOL_VERSION))
else
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_audio_tuning_tool_ver=V1
endif

ifeq ($(strip $(MTK_DM_APP)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_dm_app=1
endif

ifeq ($(strip $(MTK_MATV_ANALOG_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_matv_analog_support=1
endif

ifeq ($(strip $(MTK_WLAN_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_wlan_support=1
  PRODUCT_PACKAGES += halutil
endif

ifeq ($(strip $(MTK_IPO_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ipo_support=1
endif

ifeq ($(strip $(MTK_GPS_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_gps_support=1
endif

ifeq ($(strip $(MTK_OMACP_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_omacp_support=1
endif

ifeq ($(strip $(HAVE_MATV_FEATURE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.have_matv_feature=1
endif

ifeq ($(strip $(MTK_BT_FM_OVER_BT_VIA_CONTROLLER)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_bt_fm_over_bt=1
endif

ifeq ($(strip $(MTK_SEARCH_DB_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_search_db_support=1
endif

ifeq ($(strip $(MTK_DIALER_SEARCH_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_dialer_search_support=1
endif

ifeq ($(strip $(MTK_DHCPV6C_WIFI)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_dhcpv6c_wifi=1
endif

ifeq ($(strip $(MTK_FM_SHORT_ANTENNA_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_fm_short_antenna_support=1
endif

ifeq ($(strip $(HAVE_AACENCODE_FEATURE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.have_aacencode_feature=1
endif

ifeq ($(strip $(MTK_CTA_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cta_support=1
endif

ifeq ($(strip $(MTK_CLEARMOTION_SUPPORT)),yes)
  PRODUCT_PACKAGES += libMJCjni
  PRODUCT_PROPERTY_OVERRIDES += \
    persist.sys.display.clearMotion=0
  PRODUCT_PROPERTY_OVERRIDES += \
    persist.clearMotion.fblevel.nrm=255
  PRODUCT_PROPERTY_OVERRIDES += \
    persist.clearMotion.fblevel.bdr=255
endif

ifeq ($(strip $(MTK_PHONE_VT_VOICE_ANSWER)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_phone_vt_voice_answer=1
endif

ifeq ($(strip $(MTK_PHONE_VOICE_RECORDING)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_phone_voice_recording=1
endif

ifeq ($(strip $(MTK_POWER_SAVING_SWITCH_UI_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_pwr_save_switch=1
endif

ifeq ($(strip $(MTK_FD_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_fd_support=1
endif

ifeq ($(strip $(MTK_CC33_SUPPORT)), yes)
# Only support the format: 0: turn off / 1: turn on
    PRODUCT_PROPERTY_OVERRIDES += persist.data.cc33.support=1
endif

#DRM part
ifeq ($(strip $(MTK_DRM_APP)), yes)
  #OMA DRM
  ifeq ($(strip $(MTK_OMADRM_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_oma_drm_support=1
  endif
  #CTA DRM
  ifeq ($(strip $(MTK_CTA_SET)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cta_drm_support=1
  endif
endif

#Widevine DRM
ifeq ($(strip $(MTK_WVDRM_SUPPORT)), yes)
  #PRODUCT_PROPERTY_OVERRIDES += ro.mtk_widevine_drm_support=1
  ifeq ($(strip $(MTK_WVDRM_L1_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_widevine_drm_l1_support=1
  else
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_widevine_drm_l3_support=1
  endif
endif

#Playready DRM
ifeq ($(strip $(MTK_PLAYREADY_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_playready_drm_support=1
endif

########
ifeq ($(strip $(MTK_DISABLE_CAPABILITY_SWITCH)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_disable_cap_switch=1
endif

ifeq ($(strip $(MTK_EAP_SIM_AKA)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_eap_sim_aka=1
endif

ifeq ($(strip $(MTK_LOG2SERVER_APP)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_log2server_app=1
endif

ifeq ($(strip $(MTK_FM_RECORDING_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_fm_recording_support=1
endif

ifeq ($(strip $(MTK_AUDIO_APE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_audio_ape_support=1
endif

ifeq ($(strip $(MTK_FLV_PLAYBACK_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_flv_playback_support=1
endif

ifeq ($(strip $(MTK_FD_FORCE_REL_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_fd_force_rel_support=1
endif

ifeq ($(strip $(MTK_BRAZIL_CUSTOMIZATION_CLARO)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.brazil_cust_claro=1
endif

ifeq ($(strip $(MTK_BRAZIL_CUSTOMIZATION_VIVO)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.brazil_cust_vivo=1
endif

ifeq ($(strip $(MTK_WMV_PLAYBACK_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_wmv_playback_support=1
endif

ifeq ($(strip $(MTK_HDMI_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_hdmi_support=1
endif

ifeq ($(strip $(MTK_FOTA_ENTRY)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_fota_entry=1
endif

ifeq ($(strip $(MTK_SCOMO_ENTRY)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_scomo_entry=1
endif

ifeq ($(strip $(MTK_MTKPS_PLAYBACK_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_mtkps_playback_support=1
endif

ifeq ($(strip $(MTK_SEND_RR_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_send_rr_support=1
endif

ifeq ($(strip $(MTK_RAT_WCDMA_PREFERRED)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_rat_wcdma_preferred=1
endif

ifeq ($(strip $(OPTR_SPEC_SEG_DEF)),OP09_SPEC0212_SEGDEFAULT)
  PRODUCT_PACKAGES += DeviceRegister
  PRODUCT_PACKAGES += SelfRegister
else

  ifeq ($(strip $(MTK_DEVREG_APP)),yes)
    PRODUCT_PACKAGES += DeviceRegister
  endif

  ifeq ($(strip $(MTK_CT4GREG_APP)),yes)
    PRODUCT_PACKAGES += SelfRegister
  endif
endif

ifeq ($(strip $(MTK_ESN_TRACK_APP)),yes)
  PRODUCT_PACKAGES += EsnTrack
endif

ifeq ($(strip $(MTK_ESN_TRACK_APP)), yes)
  PRODUCT_PROPERTY_OVERRIDES += persist.sys.esn_track_switch=0
endif

ifeq ($(strip $(MTK_SMSREG_APP)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_smsreg_app=1
endif

ifeq ($(strip $(MTK_DEFAULT_DATA_OFF)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_default_data_off=1
endif

ifeq ($(strip $(MTK_TB_APP_CALL_FORCE_SPEAKER_ON)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_tb_call_speaker_on=1
endif

ifeq ($(strip $(MTK_EMMC_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_emmc_support=1
endif

ifeq ($(strip $(MTK_UFS_BOOTING)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ufs_booting=1
endif

ifeq ($(strip $(MTK_FM_50KHZ_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_fm_50khz_support=1
endif

ifeq ($(strip $(MTK_BSP_PACKAGE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_bsp_package=1
endif

ifeq ($(strip $(MTK_TETHERINGIPV6_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_tetheringipv6_support=1
endif

ifeq ($(strip $(MTK_PHONE_NUMBER_GEODESCRIPTION)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_phone_number_geo=1
endif

ifeq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_c2k_support=1
  PRODUCT_PROPERTY_OVERRIDES += persist.radio.flashless.fsm=0
  PRODUCT_PROPERTY_OVERRIDES += persist.radio.flashless.fsm_cst=0
  PRODUCT_PROPERTY_OVERRIDES += persist.radio.flashless.fsm_rw=0

  PRODUCT_PROPERTY_OVERRIDES += ro.cdma.cfu.enable=*72
  PRODUCT_PROPERTY_OVERRIDES += ro.cdma.cfu.disable=*720
  PRODUCT_PROPERTY_OVERRIDES += ro.cdma.cfb.enable=*90
  PRODUCT_PROPERTY_OVERRIDES += ro.cdma.cfb.disable=*900
  PRODUCT_PROPERTY_OVERRIDES += ro.cdma.cfnr.enable=*92
  PRODUCT_PROPERTY_OVERRIDES += ro.cdma.cfnr.disable=*920
  PRODUCT_PROPERTY_OVERRIDES += ro.cdma.cfdf.enable=*68
  PRODUCT_PROPERTY_OVERRIDES += ro.cdma.cfdf.disable=*680
  PRODUCT_PROPERTY_OVERRIDES += ro.cdma.cfall.disable=*730

  # callWaiting
  PRODUCT_PROPERTY_OVERRIDES += ro.cdma.cw.enable=*74
  PRODUCT_PROPERTY_OVERRIDES += ro.cdma.cw.disable=*740

  # network property
   ifeq ($(strip $(RAT_CONFIG_LTE_SUPPORT)),yes)
      # NETWORK_MODE_LTE_CDMA_EVDO_GSM_WCDMA (10)
      PRODUCT_PROPERTY_OVERRIDES += telephony.lteOnCdmaDevice=1
      PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=10,10
   else
      # NETWORK_MODE_GLOBAL(7)
      PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=7,7
   endif
endif

ifneq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
    ifeq ($(strip $(RAT_CONFIG_LTE_SUPPORT)),yes)
        # NETWORK_MODE_LTE_GSM_WCDMA (9)
        ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), ss)
            PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=9
        else ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), dsds)
            PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=9,9
        else ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), dsda)
            PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=9,9
        else ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), tsts)
            PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=9,9,9
        else ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), qsqs)
            PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=9,9,9,9
        else
            PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=9
        endif
    else
        # NETWORK_MODE_WCDMA_PREF(0)
        ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), ss)
            PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=0
        else ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), dsds)
            PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=0,0
        else ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), dsda)
            PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=0,0
        else ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), tsts)
            PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=0,0,0
        else ifeq ($(strip $(MTK_MULTI_SIM_SUPPORT)), qsqs)
            PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=0,0,0,0
        else
            PRODUCT_PROPERTY_OVERRIDES += ro.telephony.default_network=0
        endif
    endif
endif

ifeq ($(strip $(EVDO_DT_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.evdo_dt_support=1
endif

ifeq ($(strip $(EVDO_DT_VIA_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.evdo_dt_via_support=1
endif

PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ril_mode=$(strip $(MTK_RIL_MODE))
ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
  ifeq ($(strip $(MTK_RIL_MODE)), c6m_1rild)
    PRODUCT_COPY_FILES += device/mediatek/common/init.mtkfusionril.rc:root/init.ril.rc
  else
    PRODUCT_PACKAGES += rilproxy
    PRODUCT_PACKAGES += mtk-rilproxy
    PRODUCT_PACKAGES += lib-rilproxy
    PRODUCT_COPY_FILES += device/mediatek/common/init.rilproxy.rc:root/init.rilproxy.rc
  endif
endif

ifeq ($(strip $(MTK_SHARED_SDCARD)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_shared_sdcard=1
endif

ifeq ($(strip $(MTK_2SDCARD_SWAP)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_2sdcard_swap=1
endif

ifeq ($(strip $(MTK_RAT_BALANCING)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_rat_balancing=1
endif

ifeq ($(strip $(WIFI_WEP_KEY_ID_SET)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.wifi_wep_key_id_set=1
endif

ifeq ($(strip $(OP01_COMPATIBLE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.op01_compatible=1
endif

ifeq ($(strip $(MTK_ENABLE_MD1)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_enable_md1=1
endif

ifeq ($(strip $(MTK_ENABLE_MD2)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_enable_md2=1
endif

ifeq ($(strip $(MTK_ENABLE_MD3)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_enable_md3=1
endif

ifeq ($(strip $(MTK_ANDROID_FOR_WORK_SUPPORT)), yes)
    PRODUCT_COPY_FILES += frameworks/native/data/etc/android.software.device_admin.xml:system/etc/permissions/android.software.device_admin.xml
    PRODUCT_COPY_FILES += frameworks/native/data/etc/android.software.managed_users.xml:system/etc/permissions/android.software.managed_users.xml
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_afw_support=1
endif

#For SOTER
ifeq ($(strip $(MTK_SOTER_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_soter_support=1
endif

ifeq ($(strip $(MTK_NETWORK_TYPE_ALWAYS_ON)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_network_type_always_on=1
endif

ifeq ($(strip $(MTK_NFC_ADDON_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_nfc_addon_support=1
endif

ifeq ($(strip $(MTK_BENCHMARK_BOOST_TP)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_benchmark_boost_tp=1
endif

ifeq ($(strip $(MTK_FLIGHT_MODE_POWER_OFF_MD)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_flight_mode_power_off_md=1
endif

ifeq ($(strip $(MTK_BT_BLE_MANAGER_SUPPORT)), yes)
  PRODUCT_PACKAGES += BluetoothLe \
                      BLEManager
endif

#For GattProfile
PRODUCT_PACKAGES += GattProfile

#For BtAutoTest
PRODUCT_PACKAGES += BtAutoTest

ifeq ($(strip $(MTK_AAL_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_aal_support=1
endif

ifeq ($(strip $(MTK_ULTRA_DIMMING_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ultra_dimming_support=1
endif

ifeq ($(strip $(MTK_DRE30_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_dre30_support=1
endif

ifneq ($(strip $(MTK_PQ_SUPPORT)), no)
    ifeq ($(strip $(MTK_PQ_SUPPORT)), PQ_HW_VER_2)
      PRODUCT_PROPERTY_OVERRIDES += ro.mtk_pq_support=2
    else
        ifeq ($(strip $(MTK_PQ_SUPPORT)), PQ_HW_VER_3)
          PRODUCT_PROPERTY_OVERRIDES += ro.mtk_pq_support=3
        endif
    endif
endif

# pq color mode, default mode is 1 (DISP)
ifeq ($(strip $(MTK_PQ_COLOR_MODE)), OFF)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_pq_color_mode=0
else
  ifeq ($(strip $(MTK_PQ_COLOR_MODE)), MDP)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_pq_color_mode=2
  else
    ifeq ($(strip $(MTK_PQ_COLOR_MODE)), DISP_MDP)
        PRODUCT_PROPERTY_OVERRIDES += ro.mtk_pq_color_mode=3
    else
        PRODUCT_PROPERTY_OVERRIDES += ro.mtk_pq_color_mode=1
    endif
  endif
endif

ifeq ($(strip $(MTK_MIRAVISION_SETTING_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_miravision_support=1
endif

ifeq ($(strip $(MTK_MIRAVISION_SETTING_SUPPORT)), yes)
  PRODUCT_PACKAGES += MiraVision
endif

ifeq ($(strip $(MTK_MIRAVISION_IMAGE_DC_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_miravision_image_dc=1
endif

ifeq ($(strip $(MTK_BLULIGHT_DEFENDER_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_blulight_def_support=1
endif

ifeq ($(strip $(MTK_CHAMELEON_DISPLAY_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_chameleon_support=1
endif

ifeq ($(strip $(MTK_TETHERING_EEM_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_tethering_eem_support=1
endif

ifeq ($(strip $(MTK_WFD_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_wfd_support=1
endif

ifeq ($(strip $(MTK_WFD_SINK_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_wfd_sink_support=1
endif

ifeq ($(strip $(MTK_WFD_SINK_UIBC_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_wfd_sink_uibc_support=1
endif

ifeq ($(strip $(MTK_WIFI_MCC_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_wifi_mcc_support=1
endif

ifeq ($(strip $(MTK_CROSSMOUNT_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_crossmount_support=1
endif

ifeq ($(strip $(MTK_MULTIPLE_TDLS_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_multiple_tdls_support=1
endif

ifeq ($(strip $(MTK_MT8193_HDMI_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_mt8193_hdmi_support=1
endif

ifeq ($(strip $(MTK_SYSTEM_UPDATE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_system_update_support=1
endif

ifeq ($(strip $(MTK_SIM_HOT_SWAP)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_sim_hot_swap=1
endif

ifeq ($(strip $(MTK_RADIOOFF_POWER_OFF_MD)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_radiooff_power_off_md=1
endif

ifeq ($(strip $(MTK_BIP_SCWS)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_bip_scws=1
endif

ifeq (OP09_SPEC0212_SEGDEFAULT,$(OPTR_SPEC_SEG_DEF))
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ctpppoe_support=1
endif

ifeq ($(strip $(MTK_IPV6_TETHER_PD_MODE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ipv6_tether_pd_mode=1
endif

ifeq ($(strip $(MTK_CACHE_MERGE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cache_merge_support=1
endif

ifeq ($(strip $(MTK_FAT_ON_NAND)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_fat_on_nand=1
endif

ifeq ($(strip $(MTK_GMO_RAM_OPTIMIZE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_gmo_ram_optimize=1
endif

ifeq ($(strip $(MTK_GMO_ROM_OPTIMIZE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_gmo_rom_optimize=1
endif

ifeq ($(strip $(MTK_CMCC_FT_PRECHECK_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cmcc_ft_precheck_support=1
endif

ifeq ($(strip $(MTK_MDM_APP)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_mdm_app=1
endif

ifeq ($(strip $(MTK_MDM_LAWMO)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_mdm_lawmo=1
endif

ifeq ($(strip $(MTK_MDM_FUMO)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_mdm_fumo=1
endif

ifeq ($(strip $(MTK_MDM_SCOMO)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_mdm_scomo=1
endif

ifeq ($(strip $(MTK_MULTISIM_RINGTONE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_multisim_ringtone=1
endif

ifeq ($(strip $(MTK_MT8193_HDCP_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_mt8193_hdcp_support=1
endif

ifeq ($(strip $(PURE_AP_USE_EXTERNAL_MODEM)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.pure_ap_use_external_modem=1
endif

ifeq ($(strip $(MTK_WFD_HDCP_TX_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_wfd_hdcp_tx_support=1
endif

ifeq ($(strip $(MTK_WFD_HDCP_RX_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_wfd_hdcp_rx_support=1
endif

ifeq ($(strip $(CMCC_LIGHT_CUST_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.cmcc_light_cust_support=1
endif

ifeq ($(strip $(MTK_WORLD_PHONE_POLICY)), 1)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_world_phone_policy=1
else
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_world_phone_policy=0
endif

ifeq ($(strip $(MTK_ECCCI_C2K)),yes)
  PRODUCT_PROPERTY_OVERRIDES +=ro.mtk_md_world_mode_support=1
endif

ifeq ($(strip $(MTK_PERFSERVICE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_perfservice_support=1
endif

ifeq ($(strip $(MTK_AUDIO_CHANGE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_audio_change_support=1
endif

ifeq ($(strip $(MTK_HDMI_HDCP_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_hdmi_hdcp_support=1
endif

ifeq ($(strip $(MTK_INTERNAL_HDMI_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_internal_hdmi_support=1
endif

ifeq ($(strip $(MTK_INTERNAL_MHL_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_internal_mhl_support=1
endif

ifeq ($(strip $(MTK_OWNER_SDCARD_ONLY_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_owner_sdcard_support=1
endif

ifeq ($(strip $(MTK_ONLY_OWNER_SIM_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_owner_sim_support=1
endif

ifeq ($(strip $(MTK_SIM_HOT_SWAP_COMMON_SLOT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_sim_hot_swap_common_slot=1
endif

ifeq ($(strip $(MTK_CTA_SET)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cta_set=1
endif

ifeq ($(strip $(MTK_CTSC_MTBF_INTERNAL_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ctsc_mtbf_intersup=1
endif

ifeq ($(strip $(MTK_3GDONGLE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_3gdongle_support=1
endif

ifeq ($(strip $(OPTR_SPEC_SEG_DEF)),OP09_SPEC0212_SEGDEFAULT)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_devreg_app=1
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ct4greg_app=1
else

  ifeq ($(strip $(MTK_DEVREG_APP)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_devreg_app=1
  endif

  ifeq ($(strip $(MTK_CT4GREG_APP)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ct4greg_app=1
  endif
endif

ifeq ($(strip $(EVDO_IR_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.evdo_ir_support=1
endif

ifeq ($(strip $(MTK_MULTI_PARTITION_MOUNT_ONLY_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_multi_patition=1
endif

ifeq ($(strip $(MTK_WIFI_CALLING_RIL_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_wifi_calling_ril_support=1
endif

ifeq ($(strip $(MTK_DRM_KEY_MNG_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_key_manager_support=1
endif

ifeq ($(strip $(MTK_DOLBY_DAP_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_dolby_dap_support=1
endif

ifeq ($(strip $(MTK_MOBILE_MANAGEMENT)), yes)
  ifdef BUILD_GMS
    ifeq ($(strip $(BUILD_GMS)), yes)
      PRODUCT_PROPERTY_OVERRIDES += ro.mtk_mobile_management=0
    else
      PRODUCT_PROPERTY_OVERRIDES += ro.mtk_mobile_management=1
    endif
  else
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_mobile_management=1
  endif
endif

ifeq ($(strip $(MTK_RUNTIME_PERMISSION_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_runtime_permission=1
endif

# enable zsd+hdr
ifeq ($(strip $(MTK_CAM_ZSDHDR_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_zsdhdr_support=1
endif

# default MFLL support level, [0~4]= off, mfll, ais, both, debug
ifeq ($(strip $(MTK_CAM_MFB_SUPPORT)), 0)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_mfb_support=0
endif
ifeq ($(strip $(MTK_CAM_MFB_SUPPORT)), 1)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_mfb_support=1
endif
ifeq ($(strip $(MTK_CAM_MFB_SUPPORT)), 2)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_mfb_support=2
endif
ifeq ($(strip $(MTK_CAM_MFB_SUPPORT)), 3)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_mfb_support=3
endif
ifeq ($(strip $(MTK_CAM_MFB_SUPPORT)), 4)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_mfb_support=4
endif

ifeq ($(strip $(MTK_CAM_DUAL_ZOOM_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_dualzoom_support=1
endif

ifeq ($(strip $(MTK_CAM_STEREO_DENOISE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_dualdenoise_support=1
endif

ifeq ($(strip $(MTK_CLEARMOTION_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_clearmotion_support=1
endif

ifeq ($(strip $(MTK_DISPLAY_120HZ_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_display_120hz_support=1
endif

ifeq ($(strip $(MTK_SLOW_MOTION_VIDEO_SUPPORT)), yes)
	PRODUCT_PROPERTY_OVERRIDES += ro.mtk_slow_motion_support=1
	PRODUCT_PACKAGES += libMtkVideoSpeedEffect
	PRODUCT_PACKAGES += libjni_slow_motion
endif

ifeq ($(strip $(MTK_CAM_NATIVE_PIP_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_native_pip_support=1
endif

ifeq ($(strip $(MTK_CAM_LOMO_SUPPORT)), yes)
  PRODUCT_PACKAGES += libcam.jni.lomohaljni
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_lomo_support=1
endif

ifeq ($(strip $(MTK_CAM_IMAGE_REFOCUS_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_img_refocus_support=1
endif

ifeq ($(strip $(MTK_LTE_DC_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_lte_dc_support=1
endif

ifeq ($(strip $(RAT_CONFIG_LTE_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_lte_support=1
endif

ifeq ($(strip $(MTK_ENABLE_MD5)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_enable_md5=1
endif

ifeq ($(strip $(MTK_FEMTO_CELL_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_femto_cell_support=1
endif

ifeq ($(strip $(MTK_SAFEMEDIA_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_safemedia_support=1
endif

ifeq ($(strip $(MTK_UMTS_TDD128_MODE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_umts_tdd128_mode=1
endif

ifeq ($(strip $(MTK_SINGLE_IMEI)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_single_imei=1
endif

ifeq ($(strip $(MTK_SINGLE_3DSHOT_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_single_3Dshot_support=1
endif

ifeq ($(strip $(MTK_CAM_MAV_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_mav_support=1
endif

ifeq ($(strip $(MTK_CAM_FACEBEAUTY_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_cfb=1
endif

ifeq ($(strip $(MTK_CAM_VIDEO_FACEBEAUTY_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_cam_vfb=1
endif

ifeq ($(strip $(MTK_RILD_READ_IMSI)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_rild_read_imsi=1
endif

ifeq ($(strip $(SIM_REFRESH_RESET_BY_MODEM)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.sim_refresh_reset_by_modem=1
endif

ifeq ($(strip $(MTK_EXTERNAL_SIM_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_external_sim_support=1
endif

ifeq ($(strip $(MTK_DISABLE_PERSIST_VSIM)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_persist_vsim_disabled=1
endif

ifneq ($(strip $(MTK_EXTERNAL_SIM_ONLY_SLOTS)),)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_external_sim_only_slots=$(strip $(MTK_EXTERNAL_SIM_ONLY_SLOTS))
else
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_external_sim_only_slots=0
endif

ifeq ($(strip $(MTK_SUBTITLE_SUPPORT)), yes)
  PRODUCT_PACKAGES += libvobsub_jni
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_subtitle_support=1
endif

ifeq ($(strip $(MTK_DFO_RESOLUTION_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_dfo_resolution_support=1
endif

ifeq ($(strip $(MTK_SMARTBOOK_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_smartbook_support=1
endif

ifeq ($(strip $(MTK_DX_HDCP_SUPPORT)), yes)
  PRODUCT_PACKAGES += ffffffff000000000000000000000003.tlbin libDxHdcp DxHDCP.cfg
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_dx_hdcp_support=1
endif

ifeq ($(strip $(MTK_LIVE_PHOTO_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_live_photo_support=1
endif

ifeq ($(strip $(MTK_MOTION_TRACK_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_motion_track_support=1
endif

ifeq ($(strip $(MTK_HOTKNOT_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_hotknot_support=1
endif

ifeq ($(strip $(MTK_PASSPOINT_R2_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_passpoint_r2_support=1
endif

ifeq ($(strip $(MTK_PRIVACY_PROTECTION_LOCK)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_privacy_protection_lock=1
endif

ifeq ($(strip $(MTK_BG_POWER_SAVING_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_bg_power_saving_support=1
endif

ifeq ($(strip $(MTK_BG_POWER_SAVING_UI_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_bg_power_saving_ui=1
endif

ifeq ($(strip $(MTK_WIFIWPSP2P_NFC_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_wifiwpsp2p_nfc_support=1
endif

ifeq ($(strip $(MTK_TC1_FEATURE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_tc1_feature=1
endif

ifeq ($(strip $(MTK_A1_FEATURE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_a1_feature=1
endif

ifneq ($(strip $(SIM_ME_LOCK_MODE)),)
  PRODUCT_PROPERTY_OVERRIDES += ro.sim_me_lock_mode=$(strip $(SIM_ME_LOCK_MODE))
else
  PRODUCT_PROPERTY_OVERRIDES += ro.sim_me_lock_mode=0
endif

ifneq ($(strip $(MTK_AP_INFO_COLLECT)),)
  PRODUCT_PROPERTY_OVERRIDES += ro.ap_info_monitor=$(strip $(MTK_AP_INFO_COLLECT))
else
  PRODUCT_PROPERTY_OVERRIDES += ro.ap_info_monitor=0
endif

ifeq ($(strip $(MTK_DUAL_MIC_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_dual_mic_support=1
else
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_dual_mic_support=0
endif

ifeq ($(strip $(MTK_VOICE_UNLOCK_USE_TAB_LIB)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_is_tablet=1
else
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_is_tablet=0
endif

ifeq ($(strip $(MTK_EXTERNAL_MODEM_SLOT)), 1)
  PRODUCT_PROPERTY_OVERRIDES += ril.external.md=1
endif
ifeq ($(strip $(MTK_EXTERNAL_MODEM_SLOT)), 2)
  PRODUCT_PROPERTY_OVERRIDES += ril.external.md=2
endif

ifeq ($(strip $(MTK_POWER_PERFORMANCE_STRATEGY_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_pow_perf_support=1
endif

# serial port open or not
ifeq ($(strip $(MTK_SERIAL_PORT_DEFAULT_ON)),yes)
ADDITIONAL_DEFAULT_PROPERTIES += persist.service.acm.enable=1
else
ADDITIONAL_DEFAULT_PROPERTIES += persist.service.acm.enable=0
endif

# for pppoe
ifeq (OP09_SPEC0212_SEGDEFAULT,$(OPTR_SPEC_SEG_DEF))
  PRODUCT_PACKAGES += ip-up \
                      ip-down \
                      pppoe \
                      pppoe-server \
                      launchpppoe
  PRODUCT_PROPERTY_OVERRIDES += ro.config.pppoe_enable=1
endif
# for 3rd party app
ifeq ($(strip $(OPTR_SPEC_SEG_DEF)),NONE)
  ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
    ifneq ($(strip $(MTK_A1_FEATURE)), yes)
      PRODUCT_PACKAGES += TouchPal
      #PRODUCT_PACKAGES += YahooNewsWidget
    endif
  endif
endif

#For 3rd party NLP provider
PRODUCT_PACKAGES += Baidu_Location
PRODUCT_PACKAGES += liblocSDK6c
PRODUCT_PACKAGES += libnetworklocation
ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
  ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
    PRODUCT_PROPERTY_OVERRIDES += persist.mtk_nlp_switch_support=1
  endif
endif

# open TouchPal in OP02
ifeq (OP02,$(word 1,$(subst _, ,$(OPTR_SPEC_SEG_DEF))))
  ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
     PRODUCT_PACKAGES += TouchPal
  endif
endif
# open TouchPal in OP09A
ifeq ($(strip $(OPTR_SPEC_SEG_DEF)),OP09_SPEC0212_SEGDEFAULT)
  ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
     PRODUCT_PACKAGES += TouchPal
  endif
endif

# open TouchPal in OP09C
ifeq ($(strip $(OPTR_SPEC_SEG_DEF)),OP09_SPEC0212_SEGC)
  ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
     PRODUCT_PACKAGES += TouchPal
  endif
endif

# default IME
ifeq (OP01,$(word 1,$(subst _, ,$(OPTR_SPEC_SEG_DEF))))
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_default_ime =com.iflytek.inputmethod.FlyIME
endif

# Data usage overview
ifeq ($(strip $(MTK_DATAUSAGE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_datausage_support=1
endif

# wifi offload service common library
PRODUCT_PACKAGES += wfo-common
PRODUCT_PACKAGES += WfoService libwfo_jni

#Define MD has the capability to setup IMS PDN
ifeq ($(strip $(MTK_RIL_MODE)), c6m_1rild)
  PRODUCT_PROPERTY_OVERRIDES += ro.md_auto_setup_ims=1
endif

# IMS and VoLTE feature
ifeq ($(strip $(MTK_IMS_SUPPORT)), yes)
    ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
        ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
            PRODUCT_PACKAGES += ImsService
        endif
    endif
  PRODUCT_PROPERTY_OVERRIDES += persist.mtk_ims_support=1
  ifneq ($(filter $(strip $(MTK_MULTIPLE_IMS_SUPPORT)),2 3 4),)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_multiple_ims_support=$(strip $(MTK_MULTIPLE_IMS_SUPPORT))
  else
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_multiple_ims_support=1
  endif
##prize-add-mms_voice_information-yangming-2016_11_14-start
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ims_support=1
##prize-add-mms_voice_information-yangming-2016_11_14-end  
endif

#WFC feature
ifeq ($(strip $(MTK_WFC_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += persist.mtk_wfc_support=1
endif

##prize-add-mms_voice_information-yangming-2016_11_14-start
ifeq ($(strip $(MTK_VOLTE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_volte_support=1
  PRODUCT_PROPERTY_OVERRIDES += persist.mtk.volte.enable=1
endif
##prize-add-mms_voice_information-yangming-2016_11_14-end

ifeq ($(strip $(MTK_VOLTE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += persist.mtk_volte_support=1
  PRODUCT_PROPERTY_OVERRIDES += persist.mtk.volte.enable=1
endif

ifeq ($(strip $(MTK_VILTE_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += persist.mtk_vilte_support=1
  ifeq ($(strip $(MTK_VIWIFI_SUPPORT)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ims_video_call_support=vilte_viwifi
    PRODUCT_PROPERTY_OVERRIDES += persist.mtk_viwifi_support=1
  else
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ims_video_call_support=vilte
    PRODUCT_PROPERTY_OVERRIDES += persist.mtk_viwifi_support=0
  endif
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_vilte_ut_support=0
endif

ifeq ($(strip $(MTK_VILTE_SUPPORT)),no)
  PRODUCT_PROPERTY_OVERRIDES += persist.mtk_vilte_support=0
  ifeq ($(strip $(MTK_VIWIFI_SUPPORT)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ims_video_call_support=viwifi
    PRODUCT_PROPERTY_OVERRIDES += persist.mtk_viwifi_support=1
  else
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_ims_video_call_support=none
    PRODUCT_PROPERTY_OVERRIDES += persist.mtk_viwifi_support=0
  endif
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_vilte_ut_support=0
endif

ifeq ($(strip $(MTK_USSI_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += persist.mtk_ussi_support=1
endif

# DTAG DUAL APN
ifeq ($(strip $(MTK_DTAG_DUAL_APN_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_dtag_dual_apn_support=1
endif

# sbc security
ifeq ($(strip $(MTK_SECURITY_SW_SUPPORT)), yes)
  PRODUCT_PACKAGES += libsec
  PRODUCT_PACKAGES += sbchk
  PRODUCT_PACKAGES += S_ANDRO_SFL.ini
  PRODUCT_PACKAGES += S_SECRO_SFL.ini
  PRODUCT_PACKAGES += sec_chk.sh
  PRODUCT_PACKAGES += AC_REGION
endif

ifeq ($(strip $(MTK_USER_ROOT_SWITCH)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_user_root_switch=1
endif

ifeq ($(strip $(MTK_DOLBY_DAP_SUPPORT)), yes)
PRODUCT_COPY_FILES += frameworks/av/media/libeffects/data/audio_effects_dolby.conf:system/etc/audio_effects.conf
PRODUCT_COPY_FILES += $(MTK_PROJECT_FOLDER)/dolby/ds1-default.xml:$(TARGET_COPY_OUT_VENDOR)/etc/ds1-default.xml:mtk
else
PRODUCT_COPY_FILES += frameworks/av/media/libeffects/data/audio_effects.conf:system/etc/audio_effects.conf
endif
ifeq ($(strip $(HAVE_SRSAUDIOEFFECT_FEATURE)),yes)
  PRODUCT_COPY_FILES += vendor/mediatek/proprietary/external/SRS_AudioEffect/srs_processing/license/dts.lic:$(TARGET_COPY_OUT_VENDOR)/data/dts.lic:mtk
  PRODUCT_COPY_FILES += vendor/mediatek/proprietary/external/SRS_AudioEffect/srs_processing/srs_processing.cfg:$(TARGET_COPY_OUT_VENDOR)/data/srs_processing.cfg:mtk
endif

ifeq ($(strip $(MTK_PERMISSION_CONTROL)), yes)
  PRODUCT_PACKAGES += PermissionControl
endif

ifeq ($(strip $(MTK_NFC_SUPPORT)), yes)
    ifdef MTK_NFC_PACKAGE
        ifneq ($(wildcard vendor/mediatek/proprietary/hardware/nfc/mtknfc.mk),)
            $(call inherit-product-if-exists, vendor/mediatek/proprietary/hardware/nfc/mtknfc.mk)
        else
            $(call inherit-product-if-exists, vendor/mediatek/proprietary/external/mtknfc/mtknfc.mk)
        endif
    else
        PRODUCT_PACKAGES += nfcstackp
        PRODUCT_PACKAGES += DeviceTestApp
        PRODUCT_PACKAGES += libdta_mt6605_jni
        PRODUCT_PACKAGES += libmtknfc_dynamic_load_jni
        PRODUCT_PACKAGES += libnfc_mt6605_jni
        $(call inherit-product-if-exists, vendor/mediatek/proprietary/packages/apps/DeviceTestApp/DeviceTestApp.mk)
        $(call inherit-product-if-exists, vendor/mediatek/proprietary/external/mtknfc/mtknfc.mk)
    endif
endif

ifeq ($(strip $(MTK_NFC_SUPPORT)), yes)
    ifeq ($(wildcard $(MTK_TARGET_PROJECT_FOLDER)/nfcse.cfg),)
        ifeq ($(strip $(MTK_BSP_PACKAGE)), yes)
            PRODUCT_COPY_FILES += packages/apps/Nfc/mtk-nfc/nfcsebsp.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/nfcse.cfg:mtk
        else
            PRODUCT_COPY_FILES += packages/apps/Nfc/mtk-nfc/nfcsetk.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/nfcse.cfg:mtk
        endif
    else
        PRODUCT_COPY_FILES += $(MTK_TARGET_PROJECT_FOLDER)/nfcse.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/nfcse.cfg:mtk
    endif
endif

ifeq (yes,$(strip $(MTK_NFC_SUPPORT)))

  PRODUCT_COPY_FILES += $(call add-to-product-copy-files-if-exists,frameworks/native/data/etc/android.hardware.nfc.xml:system/etc/permissions/android.hardware.nfc.xml)

  ifneq ($(MTK_BSP_PACKAGE), yes)
    PRODUCT_COPY_FILES +=$(call add-to-product-copy-files-if-exists,frameworks/base/nfc-extras/com.android.nfc_extras.xml:system/etc/permissions/com.android.nfc_extras.xml)
    PRODUCT_COPY_FILES +=$(call add-to-product-copy-files-if-exists,packages/apps/Nfc/etc/nfcee_access.xml:system/etc/nfcee_access.xml)
    ifeq ($(MTK_NFC_GSMA_SUPPORT), yes)
        PRODUCT_PACKAGES += com.mediatek.nfcgsma_extras
        PRODUCT_PACKAGES += com.gsma.services.nfc
        PRODUCT_COPY_FILES +=$(call add-to-product-copy-files-if-exists,packages/apps/Nfc/gsma/jar/com.gsma.services.nfc.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/com.gsma.services.nfc.xml:mtk)
        PRODUCT_COPY_FILES +=$(call add-to-product-copy-files-if-exists,packages/apps/Nfc/gsma/jar/com.gsma.services.nfc.jar:$(TARGET_COPY_OUT_VENDOR)/framework/com.gsma.services.nfc.jar:mtk)

        ifeq ($(wildcard $(MTK_TARGET_PROJECT_FOLDER)/gsma.cfg),)
            PRODUCT_COPY_FILES += packages/apps/Nfc/gsma/gsma.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gsma.cfg:mtk
        endif

        PRODUCT_PROPERTY_OVERRIDES += ro.mtk_nfc_gsma_support=1
    endif
  endif

  PRODUCT_PACKAGES += Nfc
  PRODUCT_PACKAGES += Tag
  PRODUCT_PACKAGES += nfcc.default
  PRODUCT_PROPERTY_OVERRIDES +=  ro.nfc.port=I2C

  ifeq (yes,$(strip $(MTK_NFC_HCE_SUPPORT)))
    PRODUCT_COPY_FILES += $(call add-to-product-copy-files-if-exists,frameworks/native/data/etc/android.hardware.nfc.hce.xml:system/etc/permissions/android.hardware.nfc.hce.xml)
  endif

endif


ifeq ($(strip $(MTK_NFC_OMAAC_SUPPORT)),yes)
  PRODUCT_PACKAGES += SmartcardService
  PRODUCT_PACKAGES += org.simalliance.openmobileapi.jar
  PRODUCT_PACKAGES += org.simalliance.openmobileapi.xml
  PRODUCT_PACKAGES += eSETerminal
  PRODUCT_PACKAGES += Uicc1Terminal
  PRODUCT_PACKAGES += Uicc2Terminal
endif

# IR-Learning Core
ifeq ($(strip $(MTK_IR_LEARNING_SUPPORT)),yes)
  PRODUCT_PACKAGES += ConsumerIrExtraService
  PRODUCT_PACKAGES += com.mediatek.consumerir
  PRODUCT_PACKAGES += com.mediatek.consumerirextra.xml
  PRODUCT_PACKAGES += libconsumerir
  PRODUCT_PACKAGES += libconsumerir_vendor
endif

# IR-Learning Test Package
ifeq ($(strip $(MTK_IR_LEARNING_SUPPORT)),yes)
  PRODUCT_PACKAGES += ConsumerIrValidator
  PRODUCT_PACKAGES += ConsumerIrPermissionValidator
endif

# IRTX HAL CORE
ifeq (yes,$(strip $(MTK_IRTX_SUPPORT)))
    PRODUCT_PACKAGES += libconsumerir_core
else
ifeq (yes,$(strip $(MTK_IRTX_PWM_SUPPORT)))
    PRODUCT_PACKAGES += libconsumerir_core
endif
endif

ifeq ($(strip $(MTK_HOTKNOT_SUPPORT)), yes)
  PRODUCT_PACKAGES += libhotknot_GT1XX
  PRODUCT_PACKAGES += libhotknot_GT9XX
  PRODUCT_PROPERTY_OVERRIDES += ro.mediatek.hotknot.module=$(CUSTOM_KERNEL_TOUCHPANEL)
endif
ifeq ($(strip $(MTK_HOTKNOT_SUPPORT)), yes)
  PRODUCT_PACKAGES += HotKnot
  PRODUCT_PACKAGES += HotKnotBeam
  PRODUCT_PACKAGES += HotKnotCommonUI
  PRODUCT_PACKAGES += HotKnotConnectivity
  PRODUCT_PACKAGES += hotknot_native_service
  PRODUCT_PACKAGES += libhotknot_dev

    ifeq ($(wildcard $(MTK_TARGET_PROJECT_FOLDER)/hotknot.cfg),)
        PRODUCT_COPY_FILES += vendor/mediatek/proprietary/packages/apps/HotKnot/hotknot.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/hotknot.cfg:mtk
    else
        PRODUCT_COPY_FILES += $(MTK_TARGET_PROJECT_FOLDER)/hotknot.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/hotknot.cfg:mtk
    endif
endif

ifeq ($(strip $(MTK_CROSSMOUNT_SUPPORT)),yes)
  PRODUCT_PACKAGES += com.mediatek.crossmount.discovery
  PRODUCT_PACKAGES += com.mediatek.crossmount.discovery.xml
  PRODUCT_PACKAGES += CrossMount
  PRODUCT_PACKAGES += com.mediatek.crossmount.adapter
  PRODUCT_PACKAGES += com.mediatek.crossmount.adapter.xml
  PRODUCT_PACKAGES += CrossMountSettings
  PRODUCT_PACKAGES += CrossMountSourceCamera
  PRODUCT_PACKAGES += CrossMountStereoSound
  PRODUCT_PACKAGES += libcrossmount
  PRODUCT_PACKAGES += libcrossmount_jni
  PRODUCT_PACKAGES += sensors.virtual
  PRODUCT_PACKAGES += SWMountViewer
endif

$(call inherit-product-if-exists, frameworks/base/data/videos/FrameworkResource.mk)
ifeq ($(strip $(MTK_LIVE_PHOTO_SUPPORT)), yes)
  PRODUCT_PACKAGES += com.mediatek.effect
  PRODUCT_PACKAGES += com.mediatek.effect.xml
endif

ifeq ($(strip $(MTK_MULTICORE_OBSERVER_APP)), yes)
  PRODUCT_PACKAGES += MultiCoreObserver
endif

# for Search, ApplicationsProvider provides apps search
PRODUCT_PACKAGES += ApplicationsProvider

# Live wallpaper configurations
# #workaround: disable it directly since device.mk can't get the value of TARGET_BUILD_PDK
PRODUCT_COPY_FILES += packages/wallpapers/LivePicker/android.software.live_wallpaper.xml:system/etc/permissions/android.software.live_wallpaper.xml

# for JPE
PRODUCT_PACKAGES += jpe_tool

# for Camera EffectFactory
PRODUCT_PACKAGES += libfeatureio.featurefactory

ifneq ($(strip $(MTK_PLATFORM)),)
  PRODUCT_PACKAGES += libnativecheck-jni
endif

# for mediatek-res
PRODUCT_PACKAGES += mediatek-res
# for prize-framework-res
PRODUCT_PACKAGES += prize-framework-res

# for TER service
PRODUCT_PACKAGES += terservice
PRODUCT_PACKAGES += tertestclient
ifeq ($(strip $(MTK_TER_SERVICE)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ter.service.enable=1
endif

PRODUCT_PROPERTY_OVERRIDES += wfd.dummy.enable=1
PRODUCT_PROPERTY_OVERRIDES += wfd.iframesize.level=0

ifeq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
   PRODUCT_PACKAGES += Utk
endif

ifeq ($(strip $(EVDO_IR_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += \
    ril.evdo.irsupport=1
endif

ifeq ($(strip $(EVDO_DT_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += \
    ril.evdo.dtsupport=1
endif

# for libudf
ifeq ($(strip $(MTK_USER_SPACE_DEBUG_FW)),yes)
PRODUCT_PACKAGES += libudf
endif

PRODUCT_COPY_FILES += $(MTK_TARGET_PROJECT_FOLDER)/ProjectConfig.mk:$(TARGET_COPY_OUT_VENDOR)/data/misc/ProjectConfig.mk:mtk

ifeq ($(strip $(MTK_BICR_SUPPORT)), yes)
PRODUCT_COPY_FILES += device/mediatek/common/iAmCdRom.iso:$(TARGET_COPY_OUT_VENDOR)/etc/iAmCdRom.iso:mtk
endif

ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
  ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
    PRODUCT_COPY_FILES += $(call add-to-product-copy-files-if-exists,vendor/mediatek/proprietary/frameworks/base/telephony/etc/virtual-spn-conf-by-efgid1.xml:$(TARGET_COPY_OUT_VENDOR)/etc/virtual-spn-conf-by-efgid1.xml:mtk)
    PRODUCT_COPY_FILES += $(call add-to-product-copy-files-if-exists,vendor/mediatek/proprietary/frameworks/base/telephony/etc/virtual-spn-conf-by-efpnn.xml:$(TARGET_COPY_OUT_VENDOR)/etc/virtual-spn-conf-by-efpnn.xml:mtk)
    PRODUCT_COPY_FILES += $(call add-to-product-copy-files-if-exists,vendor/mediatek/proprietary/frameworks/base/telephony/etc/virtual-spn-conf-by-efspn.xml:$(TARGET_COPY_OUT_VENDOR)/etc/virtual-spn-conf-by-efspn.xml:mtk)
    PRODUCT_COPY_FILES += $(call add-to-product-copy-files-if-exists,vendor/mediatek/proprietary/frameworks/base/telephony/etc/virtual-spn-conf-by-imsi.xml:$(TARGET_COPY_OUT_VENDOR)/etc/virtual-spn-conf-by-imsi.xml:mtk)

    ifeq ($(strip $(OPTR_SPEC_SEG_DEF)),OP09_SPEC0212_SEGDEFAULT)
      PRODUCT_COPY_FILES += vendor/mediatek/proprietary/frameworks/base/telephony/etc/spn-conf-op09.xml:$(TARGET_COPY_OUT_VENDOR)/etc/spn-conf-op09.xml:mtk
    endif

    ifeq ($(strip $(OPTR_SPEC_SEG_DEF)),OP09_SPEC0212_SEGC)
      PRODUCT_COPY_FILES += vendor/mediatek/proprietary/frameworks/base/telephony/etc/spn-conf-op09.xml:$(TARGET_COPY_OUT_VENDOR)/etc/spn-conf-op09.xml:mtk
    endif
  endif
endif

ifeq ($(strip $(MTK_AUDIO_ALAC_SUPPORT)), yes)
  PRODUCT_PACKAGES += libMtkOmxAlacDec
endif

ifeq ($(strip $(TRUSTONIC_TEE_SUPPORT)), yes)
  PRODUCT_PACKAGES += RootPA
  PRODUCT_PACKAGES += libMcClient
  PRODUCT_PACKAGES += libTeeClient
  PRODUCT_PACKAGES += libMcRegistry
  PRODUCT_PACKAGES += mcDriverDaemon
  PRODUCT_PACKAGES += libMcTeeKeymaster
  PRODUCT_PACKAGES += libMcGatekeeper
  PRODUCT_PACKAGES += libsec_mem
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_trustonic_tee_support=1
  PRODUCT_COPY_FILES += \
      device/mediatek/common/public.libraries.vendor.txt:$(TARGET_COPY_OUT_VENDOR)/etc/public.libraries.txt:mtk
  ifeq ($(strip $(MTK_TEE_GP_SUPPORT)),yes)
    PRODUCT_PACKAGES += libTEECommon
  endif
  ifeq ($(strip $(MTK_SEC_VIDEO_PATH_SUPPORT)), yes)
    PRODUCT_PACKAGES += libMtkH264SecVencTLCLib
    PRODUCT_PACKAGES += libMtkH264SecVdecTLCLib
    PRODUCT_PACKAGES += libMtkH265SecVdecTLCLib
    PRODUCT_PACKAGES += libMtkVP9SecVdecTLCLib
    PRODUCT_PACKAGES += libtlcWidevineModularDrm
    PRODUCT_PACKAGES += libtlcWidevineClassicDrm
    PRODUCT_PACKAGES += libtplay
    ifeq ($(strip $(MTK_DRM_KEY_MNG_SUPPORT)), yes)
      PRODUCT_PACKAGES += liburee_meta_drmkeyinstall
    endif
  endif
  ifeq ($(strip $(MTK_TEE_TRUSTED_UI_SUPPORT)), yes)
    PRODUCT_PACKAGES += libTui
    PRODUCT_PACKAGES += TuiService
    PRODUCT_PACKAGES += SamplePinpad
    PRODUCT_PACKAGES += libTlcPinpad
  endif
endif

ifeq ($(strip $(MTK_GOOGLE_TRUSTY_SUPPORT)), yes)
  PRODUCT_PACKAGES += gatekeeper.trusty
  PRODUCT_PACKAGES += keystore.trusty
  PRODUCT_PACKAGES += libtrusty
endif

ifeq ($(strip $(MICROTRUST_TEE_SUPPORT)), yes)
  PRODUCT_PACKAGES += teei_daemon
  PRODUCT_PACKAGES += init_thh
#  PRODUCT_PACKAGES += libteei_fp
#  PRODUCT_PACKAGES += libfingerprint_tac
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_microtrust_tee_support=1
ifeq ($(strip $(MICROTRUST_IFAA_SUPPORT)), yes)
  PRODUCT_PACKAGES += libmtee
  PRODUCT_PACKAGES += libteeclientjni
  PRODUCT_PACKAGES += libifaa_daemon
  PRODUCT_PACKAGES += ifaad
  PRODUCT_PACKAGES += libifaa_jni
endif
endif

ifeq ($(strip $(MTK_TEE_GP_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_tee_gp_support=1
endif

ifeq ($(strip $(MTK_SEC_VIDEO_PATH_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_sec_video_path_support=1
  ifeq ($(filter $(MTK_IN_HOUSE_TEE_SUPPORT) $(MTK_GOOGLE_TRUSTY_SUPPORT),yes),yes)
  PRODUCT_PACKAGES += lib_uree_mtk_video_secure_al
  endif
endif

ifeq ($(strip $(MTK_COMBO_SUPPORT)), yes)
  $(call inherit-product-if-exists, device/mediatek/common/connectivity/product_package/product_package.mk)
endif

ifneq (yes,$(strip $(SPM_FW_USE_PARTITION)))
  $(call inherit-product-if-exists,vendor/mediatek/proprietary/hardware/spm/product_package.mk)
else
  PRODUCT_PACKAGES += spmfw.bin
endif

ifeq ($(strip $(MTK_SENSOR_HUB_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_sensorhub_support=1
  PRODUCT_PACKAGES += libhwsensorhub \
                      libsensorhub \
                      libsensorhub_jni \
                      sensorhubservice \
                      libsensorhubservice
endif

ifeq ($(strip $(MTK_TC7_FEATURE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_tc7_feature=1
endif

PRODUCT_PACKAGES += Launcher3

#Add for CCCI Lib
PRODUCT_PACKAGES += libccci_util

ifeq ($(strip $(MTK_WMA_PLAYBACK_SUPPORT)), yes)
  PRODUCT_PACKAGES += libMtkOmxWmaDec
endif

ifeq ($(strip $(MTK_WMA_PLAYBACK_SUPPORT))_$(strip $(MTK_SWIP_WMAPRO)), yes_yes)
  PRODUCT_PACKAGES += libMtkOmxWmaProDec
endif


# IKEv2
ifeq ($(strip $(MTK_EPDG_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += persist.mtk_epdg_support=1
  PRODUCT_PACKAGES += ipsec_mon

  PRODUCT_COPY_FILES += device/mediatek/common/init.epdg.rc:root/init.epdg.rc
endif

#Set mobiledata to false only in operator package
ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
  ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
    ifdef OPTR
      ifneq ($(strip $(OPTR)), NONE)
        ifeq (OP01,$(word 1,$(subst _, ,$(OPTR_SPEC_SEG_DEF))))
          PRODUCT_PROPERTY_OVERRIDES += ro.com.android.mobiledata=false
        else ifeq ($(strip $(OPTR_SPEC_SEG_DEF)),OP09_SPEC0212_SEGDEFAULT)
          PRODUCT_PROPERTY_OVERRIDES += ro.com.android.mobiledata=false
        else
          PRODUCT_PROPERTY_OVERRIDES += ro.com.android.mobiledata=true
        endif
      else
        PRODUCT_PROPERTY_OVERRIDES += ro.com.android.mobiledata=true
      endif
    else
      PRODUCT_PROPERTY_OVERRIDES += ro.com.android.mobiledata=true
    endif
  else
    PRODUCT_PROPERTY_OVERRIDES += ro.com.android.mobiledata=true
  endif
else
  PRODUCT_PROPERTY_OVERRIDES += ro.com.android.mobiledata=true
endif

PRODUCT_PROPERTY_OVERRIDES += persist.radio.mobile.data=0,0
#for meta mode dump data
PRODUCT_PROPERTY_OVERRIDES += persist.meta.dumpdata=0

ifneq ($(MTK_AUDIO_TUNING_TOOL_VERSION),)
  ifneq ($(strip $(MTK_AUDIO_TUNING_TOOL_VERSION)),V1)
    PRODUCT_PACKAGES += libaudio_param_parser
    AUDIO_PARAM_OPTIONS_LIST += 5_POLE_HS_SUPPORT=$(MTK_HEADSET_ACTIVE_NOISE_CANCELLATION)
    MTK_AUDIO_PARAM_DIR_LIST += device/mediatek/common/audio_param
    MTK_AUDIO_PARAM_DIR_LIST_1 += device/mediatek/common/audio_param_1
    #MTK_AUDIO_PARAM_FILE_LIST += SOME_ZIP_FILE
    ifeq ($(strip $(MTK_USB_PHONECALL)),AP)
      AUDIO_PARAM_OPTIONS_LIST += MTK_USB_PHONECALL=yes
    endif

    # speaker path customization for gain table
    ifeq ($(strip $(MTK_AUDIO_SPEAKER_PATH)),int_spk_amp)
      AUDIO_PARAM_OPTIONS_LIST += SPK_PATH_INT=yes
    else ifeq ($(strip $(MTK_AUDIO_SPEAKER_PATH)),2_in_1_spk)
      AUDIO_PARAM_OPTIONS_LIST += SPK_PATH_INT=yes
    else ifeq ($(strip $(MTK_AUDIO_SPEAKER_PATH)),3_in_1_spk)
      AUDIO_PARAM_OPTIONS_LIST += SPK_PATH_INT=yes
    else ifeq ($(strip $(MTK_AUDIO_SPEAKER_PATH)),int_lo_buf)
      AUDIO_PARAM_OPTIONS_LIST += SPK_PATH_LO=yes
    else ifeq ($(strip $(MTK_AUDIO_SPEAKER_PATH)),int_hp_buf)
      AUDIO_PARAM_OPTIONS_LIST += SPK_PATH_HP=yes
    else
      AUDIO_PARAM_OPTIONS_LIST += SPK_PATH_NO_ANA=yes
    endif

    # receiver path customization for gain table
    ifeq ($(strip $(MTK_AUDIO_SPEAKER_PATH)),2_in_1_spk)
      AUDIO_PARAM_OPTIONS_LIST += RCV_PATH_2_IN_1_SPK=yes
    else ifeq ($(strip $(MTK_AUDIO_SPEAKER_PATH)),3_in_1_spk)
      AUDIO_PARAM_OPTIONS_LIST += RCV_PATH_3_IN_1_SPK=yes
    else ifeq ($(findstring smartpa,$(MTK_AUDIO_SPEAKER_PATH)),smartpa)
      ifeq ($(MTK_2IN1_SPK_SUPPORT),yes)
        AUDIO_PARAM_OPTIONS_LIST += RCV_PATH_NO_ANA=yes
     else
        AUDIO_PARAM_OPTIONS_LIST += RCV_PATH_INT=yes
      endif
    else
      AUDIO_PARAM_OPTIONS_LIST += RCV_PATH_INT=yes
    endif

    # Speech Parameter Tuning
    # SPH_PARAM_VERSION: 0 support single network(MD ability related)
    # SPH_PARAM_VERSION: 1.0 support multiple networks(MD ability related)
    # SPH_PARAM_VERSION: 2.0 support IIR and fix WBFIR(MD ability related)
    AUDIO_PARAM_OPTIONS_LIST += SPH_PARAM_VERSION=2.0
    AUDIO_PARAM_OPTIONS_LIST += SPH_PARAM_TTY=yes
    AUDIO_PARAM_OPTIONS_LIST += FIX_WB_ENH=yes
    AUDIO_PARAM_OPTIONS_LIST += MTK_IIR_ENH_SUPPORT=yes
    AUDIO_PARAM_OPTIONS_LIST += MTK_IIR_MIC_SUPPORT=no
    AUDIO_PARAM_OPTIONS_LIST += MTK_FIR_IIR_ENH_SUPPORT=no

    # Speech Loopback Tunning
    ifeq ($(MTK_TC1_FEATURE),yes)
      AUDIO_PARAM_OPTIONS_LIST += MTK_AUDIO_SPH_LPBK_PARAM=yes
    else ifeq ($(MTK_TC10_FEATURE),yes)
      AUDIO_PARAM_OPTIONS_LIST += MTK_AUDIO_SPH_LPBK_PARAM=yes
    else ifeq ($(MTK_AUDIO_SPH_LPBK_PARAM),yes)
      AUDIO_PARAM_OPTIONS_LIST += MTK_AUDIO_SPH_LPBK_PARAM=yes
    endif
    # Super Volume Parameter
    AUDIO_PARAM_OPTIONS_LIST += SPH_PARAM_SV=no

  endif
endif

ifeq ($(strip $(MTK_SPEECH_ENCRYPTION_SUPPORT)), yes)
  PRODUCT_PACKAGES += libaudiocustencrypt
endif

# aurisys framework
ifneq ($(MTK_BASIC_PACKAGE), yes)
  ifeq ($(MTK_AURISYS_FRAMEWORK_SUPPORT),yes)
    # configurations
    PRODUCT_COPY_FILES += vendor/mediatek/proprietary/external/aurisys/aurisys_config.xml:$(TARGET_COPY_OUT_VENDOR)/etc/aurisys_config.xml:mtk

    # OpenDSP
    PRODUCT_COPY_FILES += vendor/mediatek/proprietary/external/aurisys/libfvaudio/FV-SAM-MTK2.dat:$(TARGET_COPY_OUT_VENDOR)/etc/aurisys_param/FV-SAM-MTK2.dat:mtk
    PRODUCT_PACKAGES   += libfvaudio

    # mediatek IIR
    PRODUCT_PACKAGES   += lib_iir

    # mediatek BESSOUND
    PRODUCT_PACKAGES   += libaudioloudc
    PRODUCT_PACKAGES   += libaudiocompensationfilterc

    # mediatek record/VoIP
    PRODUCT_COPY_FILES += device/mediatek/common/audio_param/Speech_AudioParam.xml:$(TARGET_COPY_OUT_VENDOR)/etc/aurisys_param/Speech_AudioParam.xml:mtk
    PRODUCT_PACKAGES   += lib_speech_enh

    # BliSRC & Bit Converter
    PRODUCT_PACKAGES += libaudiocomponentenginec

    # tuning tool
    PRODUCT_PACKAGES += AudioSetParam
  endif
endif

# Audio speech enhancement library
PRODUCT_PACKAGES += libspeech_enh_lib

ifeq ($(findstring smartpa, $(MTK_AUDIO_SPEAKER_PATH)), smartpa)
    PRODUCT_PACKAGES += libaudio_param_parser
    MTK_AUDIO_PARAM_DIR_LIST += device/mediatek/common/audio_param_smartpa
endif

# Audio BTCVSD Lib
PRODUCT_PACKAGES += libcvsd_mtk
PRODUCT_PACKAGES += libmsbc_mtk

# Audio Compensation Lib
PRODUCT_PACKAGES += libaudiocompensationfilter

ifeq ($(strip $(MTK_NTFS_OPENSOURCE_SUPPORT)), yes)
  PRODUCT_PACKAGES += ntfs-3g
  PRODUCT_PACKAGES += ntfsfix
endif

# Add for HetComm feature
ifeq ($(strip $(MTK_HETCOMM_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_hetcomm_support=1
  PRODUCT_PACKAGES += HetComm
endif

ifeq ($(strip $(MTK_DEINTERLACE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_deinterlace_support=1
endif

ifeq ($(strip $(MTK_DOLBY_DAP_SUPPORT)),yes)

DOLBY_DAX_VERSION            := 1
DOLBY_DAP                    := true
DOLBY_DAP2                   := false
DOLBY_DAP_SW                 := true
DOLBY_DAP_HW                 := false
DOLBY_DAP_PREGAIN            := true
DOLBY_DAP_HW_QDSP_HAL_API    := false
DOLBY_DAP_MOVE_EFFECT        := true
DOLBY_DAP_BYPASS_SOUND_TYPES := false
DOLBY_CONSUMER_APP           := true
DOLBY_UDC                    := true
DOLBY_UDC_VIRTUALIZE_AUDIO   := false
DOLBY_MONO_SPEAKER           := true

include vendor/dolby/ds/dolby-buildspec.mk
$(call inherit-product, vendor/dolby/ds/dolby-product.mk)

PRODUCT_COPY_FILES := \
    vendor/dolby/device/mediatek_sw/audio_effects.conf:system/vendor/etc/audio_effects.conf:dolby \
    vendor/dolby/device/mediatek_sw/ds1-default.xml:system/vendor/etc/dolby/ds1-default.xml:dolby \
    $(PRODUCT_COPY_FILES)

PRODUCT_RESTRICT_VENDOR_FILES := false
endif

ifeq ($(strip $(MTK_MD_DIRECT_TETHERING_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_md_direct_tethering=1
    PRODUCT_PROPERTY_OVERRIDES += ro.tethering.bridge.interface=mdbr0
    PRODUCT_PROPERTY_OVERRIDES += sys.mtk_md_direct_tether_enable=true
    PRODUCT_PACKAGES += brctl
endif

#Fix me: specific enable for build error workaround
SKIP_BOOT_JARS_CHECK := true

ifeq ($(strip $(MTK_SWITCH_ANTENNA_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_switch_antenna=1
endif

ifeq ($(strip $(MTK_TDD_DATA_ONLY_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_tdd_data_only_support=1
endif

ifneq ($(strip $(MTK_MD_SBP_CUSTOM_VALUE)),)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_md_sbp_custom_value=$(strip $(MTK_MD_SBP_CUSTOM_VALUE))
endif

# Add for Automatic Setting for heapgrowthlimit & heapsize
RESOLUTION_HXW=$(shell expr $(LCM_HEIGHT) \* $(LCM_WIDTH))

ifeq ($(shell test $(RESOLUTION_HXW) -ge 0 && test $(RESOLUTION_HXW) -lt 1000000 && echo true), true)
PRODUCT_PROPERTY_OVERRIDES += dalvik.vm.heapgrowthlimit=128m
PRODUCT_PROPERTY_OVERRIDES += dalvik.vm.heapsize=256m
endif

ifeq ($(shell test $(RESOLUTION_HXW) -ge 1000000 && test $(RESOLUTION_HXW) -lt 3500000 && echo true), true)
PRODUCT_PROPERTY_OVERRIDES += dalvik.vm.heapgrowthlimit=192m
PRODUCT_PROPERTY_OVERRIDES += dalvik.vm.heapsize=384m
endif

ifeq ($(shell test $(RESOLUTION_HXW) -ge 3500000 && echo true), true)
PRODUCT_PROPERTY_OVERRIDES += dalvik.vm.heapgrowthlimit=384m
PRODUCT_PROPERTY_OVERRIDES += dalvik.vm.heapsize=768m
endif

PRODUCT_PACKAGES += CarrierConfig

# Add for SensorHub
PRODUCT_PACKAGES += SensorHub

# Add for common service initialization
PRODUCT_COPY_FILES += device/mediatek/common/init.common_svc.rc:root/init.common_svc.rc

# M: GMO Zygote64 on demand @{
ifeq ($(strip $(MTK_GMO_ZYGOTE_ON_DEMAND)), yes)
    # 1 = support secondary zygote on demand, 0 = not support
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_gmo_zygote_on_demand=1

    # 1 = full preload, 0 = no preload
    PRODUCT_PROPERTY_OVERRIDES += persist.sys.mtk_zygote_preload=0

    # the time to stop secondary zygote after the last forked process has been killed, in seconds
    PRODUCT_PROPERTY_OVERRIDES += persist.sys.mtk_zygote_timeout=60

    # 1 = enable debug, 0 = disable debug
    PRODUCT_PROPERTY_OVERRIDES += persist.sys.mtk_zygote_debug=0

    # include init.zygote_on_demand.rc
    PRODUCT_COPY_FILES += device/mediatek/common/init.zygote_on_demand.rc:root/init.zygote_on_demand.rc
endif
# M: GMO Zygote64 on demand @}

# Add for (VzW) chipset test
ifneq ($(MTK_VZW_CHIPTEST_MODE_SUPPORT),)
  #we dont support yes, only support 0,1,2
  ifeq (, $(filter 0 no,$(MTK_VZW_CHIPTEST_MODE_SUPPORT)))
    PRODUCT_COPY_FILES += device/mediatek/common/init.chiptest.rc:root/init.chiptest.rc
    ifeq ($(strip $(MTK_VZW_CHIPTEST_MODE_SUPPORT)), 1)
      PRODUCT_PROPERTY_OVERRIDES += persist.chiptest.enable=1
    endif
  endif
endif

# Add for Dynamic-SBP
ifeq ($(strip $(MTK_DYNAMIC_SBP_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += persist.radio.mtk_dsbp_support=1
endif

# Add for Dynamic IMS switch support
ifeq ($(strip $(MTK_DYNAMIC_IMS_SWITCH_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += persist.mtk_dynamic_ims_switch=1
endif

# Add for MTK_CT_VOLTE_SUPPORT
ifeq ($(strip $(MTK_CT_VOLTE_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += persist.mtk_ct_volte_support=1
endif

ifeq ($(strip $(PRIZE_FINGER_PRINT)),afs083t)
	PRODUCT_COPY_FILES += vendor/fingerprint/afs083t/libfprint-x64.so:system/lib64/libfprint-x64.so
	PRODUCT_COPY_FILES += vendor/fingerprint/afs083t/libma-fpservice.so:system/lib64/libma-fpservice.so
	PRODUCT_COPY_FILES += vendor/fingerprint/afs083t/fingerprint.default.so:system/lib64/hw/fingerprint.default.so
	PRODUCT_PACKAGES += fingerprintd
	PRODUCT_PACKAGES += FingerprintApplock
	PRODUCT_PROPERTY_OVERRIDES += ro.prize_fingerprint=1
	PRODUCT_COPY_FILES += frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml
endif

#  add by hushilun
ifeq ($(strip $(PRIZE_FINGER_PRINT)),sw533)
	include vendor/fingerprint/sunwavecorp_sw533/sw_cfg.mk
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw533/bin/fpserver:system/bin/fpserver
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw533/bin/FpServiceTest:system/bin/FpServiceTest
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw533/bin/sunwave_test:system/bin/sunwave_test
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw533/lib/libfpService_sw.so:system/lib/libfpService_sw.so
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw533/lib/libfpClient_sw.so:system/lib/libfpClient_sw.so
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw533/lib/libfpjni_sw.so:system/lib/libfpjni_sw.so
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw533/lib/hw/fingerprint.default.so:system/lib/hw/fingerprint.default.so
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw533/lib/hw/sw_sensor.default.so:system/lib/hw/sw_sensor.default.so
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw533/lib64/hw/sw_sensor.default.so:system/lib64/hw/sw_sensor.default.so
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw533/lib64/libfpClient_sw.so:system/lib64/libfpClient_sw.so
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw533/lib64/libfpjni_sw.so:system/lib64/libfpjni_sw.so
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw533/lib64/hw/fingerprint.default.so:system/lib64/hw/fingerprint.default.so
	PRODUCT_PACKAGES += fingerprintd
	PRODUCT_PACKAGES += FingerprintApplock
	PRODUCT_PROPERTY_OVERRIDES += ro.prize_fingerprint=1
	PRODUCT_COPY_FILES += frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml
endif

#--------------------prize-liuqingwen-add for tee 20180129 start--------------------#
ifeq ($(strip $(PRIZE_FINGER_PRINT)),sw533_tee)
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwave_sw533_tee/tee:data/thh/tee_05/tee
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwave_sw533_tee/fingerprint.default.so:system/vendor/lib/hw/fingerprint.default.so
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwave_sw533_tee/lib64/fingerprint.default.so:system/vendor/lib64/hw/fingerprint.default.so
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwave_sw533_tee/fp_server:system/vendor/thh/fp_server
	PRODUCT_COPY_FILES += vendor/fingerprint/sunwave_sw533_tee/sw_config.xml:system/etc/sw_config.xml
	PRODUCT_PACKAGES += fingerprintd
	PRODUCT_PACKAGES += FingerprintApplock
	PRODUCT_PROPERTY_OVERRIDES += ro.prize_fingerprint=1
	PRODUCT_COPY_FILES += frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml
endif
#--------------------prize-liuqingwen-add for tee 20180129 end--------------------#

# Add for BRM superset
ifeq ($(strip $(RELEASE_BRM)), yes)
  include device/mediatek/common/releaseBRM.mk
endif

# Add for Contacts AAS and SNE
ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
    ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
        PRODUCT_PACKAGES += AasSne
    endif
endif

# Add for Contacts SimProcessor
ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
    ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
        PRODUCT_PACKAGES += SimProcessor
    endif
endif

# Add for Modem protocol2 capability setting
ifneq ($(strip $(MTK_PROTOCOL2_RAT_CONFIG)),)
  PRODUCT_PROPERTY_OVERRIDES += persist.radio.mtk_ps2_rat=$(strip $(MTK_PROTOCOL2_RAT_CONFIG))
endif

# Add for Modem protocol3 capability setting
ifneq ($(strip $(MTK_PROTOCOL3_RAT_CONFIG)),)
  PRODUCT_PROPERTY_OVERRIDES += persist.radio.mtk_ps3_rat=$(strip $(MTK_PROTOCOL3_RAT_CONFIG))
endif


ifeq ($(strip $(MTK_VIBSPK_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_vibspk_support=1
endif

# Add for opt_c2k_lte_mode
ifeq ($(strip $(MTK_C2K_LTE_MODE)), 2)
  PRODUCT_PROPERTY_OVERRIDES +=ro.boot.opt_c2k_lte_mode=2
else
  ifeq ($(strip $(MTK_C2K_LTE_MODE)), 1)
    PRODUCT_PROPERTY_OVERRIDES +=ro.boot.opt_c2k_lte_mode=1
  else
    ifeq ($(strip $(MTK_C2K_LTE_MODE)), 0)
      PRODUCT_PROPERTY_OVERRIDES +=ro.boot.opt_c2k_lte_mode=0
    endif
  endif
endif

# Add for opt_md1_support
ifneq ($(strip $(MTK_MD1_SUPPORT)),)
ifneq ($(strip $(MTK_MD1_SUPPORT)), 0)
  PRODUCT_PROPERTY_OVERRIDES += ro.boot.opt_md1_support=$(strip $(MTK_MD1_SUPPORT))
endif
endif

# Add for opt_md3_support
ifneq ($(strip $(MTK_MD3_SUPPORT)),)
ifneq ($(strip $(MTK_MD3_SUPPORT)), 0)
  PRODUCT_PROPERTY_OVERRIDES += ro.boot.opt_md3_support=$(strip $(MTK_MD3_SUPPORT))
endif
endif

# Add for opt_lte_support
ifneq ($(strip $(MTK_PROTOCOL1_RAT_CONFIG)),)
ifneq ($(findstring L,$(strip $(MTK_PROTOCOL1_RAT_CONFIG))),)
  PRODUCT_PROPERTY_OVERRIDES += ro.boot.opt_lte_support=1
endif
endif

# Add for opt_irat_support
ifeq ($(strip $(MTK_IRAT_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.boot.opt_irat_support=1
endif

PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RILMUXD=I

# Telephony RIL log configurations
ifeq ($(strip $(MTK_TELEPHONY_CONN_LOG_CTRL_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.tel_log_ctrl=1
ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.AT=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RILC-MTK=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RILC=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxMainThread=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxRoot=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxRilAdapter=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxController=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RILC-RP=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxCloneMgr=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxHandlerMgr=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxIdToStr=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxMclMessenger=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RilClient=I
endif
ifneq ($(strip $(TARGET_BUILD_VARIANT)),eng)
  # user/userdebug load
  # V/D/(I/W/E)
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.DCT=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RIL-DATA=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.C2K_RIL-DATA=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.GsmCdmaPhone=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.SSDecisonMaker=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.GsmMmiCode=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RpSsController=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RIL-SS=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RILMD2-SS=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.DataSubSelector=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.DcFcMgr=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.DC-1=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.DC-2=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RetryManager=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.IccProvider=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.IccPhoneBookIM=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.AdnRecordCache=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.AdnRecordLoader=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.AdnRecord=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RIL-PHB=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RpPhbController=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RmcPhbReq=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RmcPhbUrc=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RtcPhb=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RIL-SMS=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.DupSmsFilterExt=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ConSmsFwkExt=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.DataOnlySmsFwk=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.VT=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsVTProvider=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.IccCardProxy=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.IsimFileHandler=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.IsimRecords=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.SIMRecords=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.SpnOverride=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.UiccCard=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.UiccController=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RIL-SIM=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.MtkAgpsHelper=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.CountryDetector=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.NetworkStats=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.NetworkPolicy=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.DataDispatcher=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsService=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.IMS_RILA=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.IMSRILRequest=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsManager=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsApp=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsBaseCommands=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsCall=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsPhone=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsPhoneCall=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsPhoneBase=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsCallSession=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsCallProfile=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsEcbm=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsEcbmProxy=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.OperatorUtils=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.WfoApp=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.GbaApp=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.GbaBsfProcedure=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.GbaBsfResponse=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.GbaDebugParam=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.GbaService=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.SresResponse=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsUtService=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.SimservType=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.SimservsTest=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ImsUt=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.SSDecisonMaker=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.SuppSrvConfig=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ECCCallHelper=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.GsmConnection=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.TelephonyConf=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.TeleConfCtrler=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.TelephonyConn=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.TeleConnService=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ECCRetryHandler=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ECCNumUtils=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ECCRuleHandler=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.SuppMsgMgr=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ECCSwitchPhone=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.GsmCdmaConn=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.GsmCdmaPhone=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.Phone=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RIL-CC=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RpCallControl=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RpAudioControl=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RadioManager=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RIL_Mux=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RIL-OEM=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RIL=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RIL_UIM_SOCKET=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RILD=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RIL-RP=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxMessage=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxDebugInfo=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxTimer=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxObject=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.SlotQueueEntry=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxAction=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RFX=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RpRadioMessage=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RpModemMessage=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.PhoneFactory=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.ProxyController=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.SpnOverride=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.SmsPlusCode=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.AutoRegSmsFwk=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.AirplaneHandler=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxDefDestUtils=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxSM=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxSocketSM=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RfxDT=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RpCdmaOemCtrl=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RpRadioCtrl=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RpMDCtrl=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RpCdmaRadioCtrl=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RpFOUtils=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.C2K_RIL-SIM=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.VDO_LOG=I
else
  # eng load
  # V/(D/I/W/E)
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.DCT=D
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.RIL-DATA=D
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.C2K_RIL-DATA=D
endif
# endif for TARGET_BUILD_VARIANT
else
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.DCT=D
endif
# endif for MTK_TELEPHONY_CONN_LOG_CTRL_SUPPORT

# Add for opt_eccci_c2k
ifeq ($(strip $(MTK_ECCCI_C2K)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.boot.opt_eccci_c2k=1
endif

# Add for opt_using_default, always set to 1
PRODUCT_PROPERTY_OVERRIDES += ro.boot.opt_using_default=1

# Add for opt_c2k_support
ifneq ($(strip $(MTK_PROTOCOL1_RAT_CONFIG)),)
ifneq ($(findstring C,$(strip $(MTK_PROTOCOL1_RAT_CONFIG))),)
  PRODUCT_PROPERTY_OVERRIDES += ro.boot.opt_c2k_support=1
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.C2K_AT=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.C2K_RILC=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.C2K_ATConfig=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.LIBC2K_RIL=I
endif
endif

# Add for Multi Ps Attach
ifeq ($(strip $(MTK_MULTI_PS_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_data_config=1
endif

# Add for multi-window
ifeq ($(strip $(MTK_MULTI_WINDOW_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_multiwindow=1
  PRODUCT_COPY_FILES += $(call add-to-product-copy-files-if-exists,frameworks/native/data/etc/android.software.freeform_window_management.xml:system/etc/permissions/android.software.freeform_window_management.xml)
endif

# Audio policy config
ifeq ($(strip $(USE_XML_AUDIO_POLICY_CONF)), 1)
AUDIO_POLICY_PROJECT_CONFIGS := \
  $(strip \
    $(notdir $(wildcard $(MTK_TARGET_PROJECT_FOLDER)/audio_policy_config/*.xml)\
    ) \
  )
AUDIO_POLICY_BASE_PROJECT_CONFIGS := \
  $(strip \
    $(filter-out $(AUDIO_POLICY_PROJECT_CONFIGS), \
      $(notdir $(wildcard device/mediatek/$(MTK_BASE_PROJECT)/audio_policy_config/*.xml)) \
    ) \
  )
AUDIO_POLICY_PLATFORM_CONFIGS := \
  $(strip \
    $(filter-out $(AUDIO_POLICY_PROJECT_CONFIGS) $(AUDIO_POLICY_BASE_PROJECT_CONFIGS), \
      $(notdir $(wildcard device/mediatek/$(MTK_PLATFORM_DIR)/audio_policy_config/*.xml)) \
    ) \
  )
AUDIO_POLICY_COMMON_CONFIGS := \
  $(strip \
    $(filter-out $(AUDIO_POLICY_PROJECT_CONFIGS) $(AUDIO_POLICY_BASE_PROJECT_CONFIGS) $(AUDIO_POLICY_PLATFORM_CONFIGS), \
      $(notdir $(wildcard device/mediatek/common/audio_policy_config/*.xml)) \
    ) \
  )

$(foreach x,$(AUDIO_POLICY_PROJECT_CONFIGS), \
  $(eval PRODUCT_COPY_FILES += $(MTK_TARGET_PROJECT_FOLDER)/audio_policy_config/$(x):system/etc/$(x)) \
)

$(foreach x,$(AUDIO_POLICY_BASE_PROJECT_CONFIGS), \
  $(eval PRODUCT_COPY_FILES += device/mediatek/$(MTK_BASE_PROJECT)/audio_policy_config/$(x):system/etc/$(x)) \
)

$(foreach x,$(AUDIO_POLICY_PLATFORM_CONFIGS), \
  $(eval PRODUCT_COPY_FILES += device/mediatek/$(MTK_PLATFORM_DIR)/audio_policy_config/$(x):system/etc/$(x)) \
)

$(foreach x,$(AUDIO_POLICY_COMMON_CONFIGS), \
  $(eval PRODUCT_COPY_FILES += device/mediatek/common/audio_policy_config/$(x):system/etc/$(x)) \
)
endif

#add guoxiong  for wlan ssdp
PRODUCT_PACKAGES += wifi_fw.cfg
#Add for video codec customization
PRODUCT_PROPERTY_OVERRIDES += mtk.vdec.waitkeyframeforplay=1

ifeq ($(strip $(MTK_EMBMS_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_embms_support=1
    PRODUCT_PACKAGES += \
        EweMBMSServer \
        com.verizon.embms.jar \
        com.verizon.embms.xml
endif

# Add for sdcardfs
PRODUCT_PROPERTY_OVERRIDES += ro.sys.sdcardfs=1

# Add for CMCC Light Customization Support
ifeq ($(strip $(CMCC_LIGHT_CUST_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.cmcc_light_cust_support=1
endif

# Add for USB MIDI
PRODUCT_COPY_FILES += \
frameworks/native/data/etc/android.software.midi.xml:system/etc/permissions/android.software.midi.xml

# workaround for dex_preopt
$(call add-product-dex-preopt-module-config,Settings,disable)
$(call add-product-dex-preopt-module-config,DataProtection,disable)
$(call add-product-dex-preopt-module-config,PermissionControl,disable)
$(call add-product-dex-preopt-module-config,PrivacyProtectionLock,disable)

ifeq (yes,$(strip $(MTK_BG_POWER_SAVING_SUPPORT)))
    ifeq (yes,$(strip $(MTK_ALARM_AWARE_UPLINK_SUPPORT)))
        PRODUCT_PROPERTY_OVERRIDES += persist.mtk.datashaping.support=1
        PRODUCT_PROPERTY_OVERRIDES += persist.datashaping.alarmgroup=1
    endif
endif

# Add for Running Booster Feature
ifeq (yes,$(strip $(MTK_RUNNING_BOOSTER_SUPPORT)))
    PRODUCT_PROPERTY_OVERRIDES += persist.runningbooster.support=1
    PRODUCT_PACKAGES += DuraSpeed
    PRODUCT_PACKAGES += mediatek-feature-runningbooster
    PRODUCT_COPY_FILES += frameworks/base/core/java/com/mediatek/runningbooster/com.mediatek.runningbooster.xml:system/etc/permissions/com.mediatek.runningbooster.xml
    PRODUCT_COPY_FILES += frameworks/base/core/java/com/mediatek/runningbooster/platform_list.txt:system/etc/runningbooster/platform_list.txt
    ifeq (yes,$(strip $(MTK_RUNNING_BOOSTER_UPGRADE)))
        PRODUCT_PROPERTY_OVERRIDES += persist.runningbooster.upgrade=1
    endif
    ifeq (yes,$(strip $(MTK_RUNNING_BOOSTER_DEFAULT_ON)))
        PRODUCT_PROPERTY_OVERRIDES += persist.runningbooster.app.on=1
    endif
endif

# Add for Resolution Switch feature
ifeq ($(strip $(MTK_RESOLUTION_SWITCH_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_res_switch=1
endif

# Add for Display HDR feature
ifeq ($(strip $(MTK_HDR_VIDEO_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_hdr_video_support=1
endif

# Add for App Working Set feature
ifeq ($(strip $(MTK_AWS_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_aws_support=1
endif

ifeq ($(strip $(MTK_MLC_NAND_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_mlc_nand_support=1
endif
ifeq ($(strip $(MTK_TLC_NAND_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_tlc_nand_support=1
endif

# for sensor multi-hal
ifeq ($(USE_SENSOR_MULTI_HAL), true)
  PRODUCT_COPY_FILES += $(MTK_PROJECT_FOLDER)/hals.conf:system/etc/sensors/hals.conf:mtk
  PRODUCT_PROPERTY_OVERRIDES += ro.hardware.sensors=$(MTK_BASE_PROJECT)
  PRODUCT_PACKAGES += sensors.$(MTK_BASE_PROJECT)
endif

# for hifi sensors feature
ifeq ($(strip $(CUSTOM_HIFI_SENSORS)), yes)
  PRODUCT_COPY_FILES += frameworks/native/data/etc/android.hardware.sensor.hifi_sensors.xml:system/etc/permissions/android.hardware.sensor.hifi_sensors.xml
endif

# for VR high performane feature
ifeq ($(MTK_VR_HIGH_PERFORMANCE_SUPPORT),yes)
  PRODUCT_COPY_FILES += frameworks/native/data/etc/android.hardware.vr.high_performance.xml:system/etc/permissions/android.hardware.vr.high_performance.xml
  PRODUCT_PACKAGES += vr.$(shell echo $(MTK_PLATFORM) | tr '[A-Z]' '[a-z]')
endif


# Add EmergencyInfo apk to TK load
ifneq ($(strip $(MTK_BASIC_PACKAGE)), yes)
    ifneq ($(strip $(MTK_BSP_PACKAGE)), yes)
        PRODUCT_PACKAGES += EmergencyInfo
    endif
endif

# Add for SKT customization
ifeq ($(strip $(MTK_KOR_CUSTOMIZATION_SKT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_kor_customization_skt=1
  PRODUCT_PROPERTY_OVERRIDES += persist.ril.sim.regmode=0
endif

# for KSC5601 decoding to write phonebook
ifeq ($(strip $(KSC5601_WRITE)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.ksc5601_write=1
else
  PRODUCT_PROPERTY_OVERRIDES += ro.ksc5601_write=0
endif

# for email field ucs2 decoding
ifeq ($(strip $(EMAIL_SUPPORT_UCS2)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.email_support_ucs2=1
else
  PRODUCT_PROPERTY_OVERRIDES += ro.email_support_ucs2=0
endif

# for decoding USSD of KSC5601 for KOR operator
ifeq ($(strip $(USSD_KSC5601)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.ussd_ksc5601=1
else
  PRODUCT_PROPERTY_OVERRIDES += ro.ussd_ksc5601=0
endif

#======================lovelyfonts start ==============================
ifeq ($(strip $(LOVELYFONTS_SUPPORT)), yes)
  PRODUCT_PACKAGES += LovelyFonts
endif

ifeq ($(strip $(MTK_YIQI_FONTS_FRAMEWORK_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.lovelyfonts_support=1
  PRODUCT_COPY_FILES += frameworks/base/lovelyfontsframework/clib/lib/libFonts.so:system/lib/libFonts.so
  ifneq ($(strip $(TARGET_CPU_ABI_LIST_64_BIT)),"")
    PRODUCT_COPY_FILES += frameworks/base/lovelyfontsframework/clib/lib64/libFonts.so:system/lib64/libFonts.so
  endif
  PRODUCT_COPY_FILES += frameworks/base/lovelyfontsframework/init.lovelyfonts.rc:root/init.lovelyfonts.rc
  PRODUCT_PACKAGES += LovelyFontContainerService
endif
#======================lovelyfonts end  =======================

# Log control for SMS
ifneq ($(strip $(TARGET_BUILD_VARIANT)),eng)
  # user/userdebug load
  # V/D/(I/W/E)
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.CdmaMoSms=I
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.CdmaMtSms=I
else
  # eng load
  # V/(D/I/W/E)
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.CdmaMoSms=D
  PRODUCT_PROPERTY_OVERRIDES += persist.log.tag.CdmaMtSms=D
endif

# Add for LWA feature support
ifeq ($(strip $(MTK_LWA_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_lwa_support=1
endif

# Add for LWI feature support
ifeq ($(strip $(MTK_LWI_SUPPORT)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_lwi_support=1
endif

# Add for FTL feature support
ifeq ($(strip $(MTK_NAND_MTK_FTL_SUPPORT)), yes)
   PRODUCT_PROPERTY_OVERRIDES += ro.mtk_nand_ftl_support=1
endif

# Add for MNTL feature support
ifeq ($(strip $(MNTL_SUPPORT)), yes)
   PRODUCT_PROPERTY_OVERRIDES += ro.mntl_support=1
endif

# MTK internal load or customer eng/userdebug load will buildin log daemon.
# Customer user load default not have MTK log daemon.
# If customer user load want buildin the log daemon, You need set
# MTK_LOG_CUSTOMER_SUPPORT_ALL to yes, it will buildin log daemon as internal load.
# Each log daemon is decided by its feature option.
# Case: MTK internal load
ifeq ($(strip $(MTK_INTERNAL)),yes)
  ifeq ($(strip $(MTK_LOG_SUPPORT_GPS)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_log_hide_gps=0
  else
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_log_hide_gps=1
  endif
  ifeq ($(strip $(MTK_MTKLOGGER_SUPPORT)),yes)
    PRODUCT_PACKAGES += MTKLogger
    ifeq ($(strip $(MTK_BTLOGGER_SUPPORT)),yes)
      PRODUCT_PACKAGES += BtTool
    endif
  endif
  ifeq ($(strip $(MTK_NETWORK_LOG_SUPPORT)),yes)
    PRODUCT_PACKAGES += netdiag
  endif
  PRODUCT_PACKAGES += tcpdump
  ifeq ($(strip $(MTK_LOG_SUPPORT_MOBILE_LOG)),yes)
    PRODUCT_PACKAGES += mobile_log_d
  endif
# Add for ModemMonitor(MDM) framework
  ifeq ($(strip $(MTK_MODEM_MONITOR_SUPPORT)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_modem_monitor_support=1
    PRODUCT_PACKAGES += \
      md_monitor \
      md_monitor_ctrl \
      MDMLSample \
      MDMConfig
  endif
  ifeq ($(strip $(MTK_AEE_SUPPORT)),yes)
    HAVE_AEE_FEATURE = yes
  else
    HAVE_AEE_FEATURE = no
  endif
# Case: Customer eng/userdebug load
else ifneq ($(strip $(TARGET_BUILD_VARIANT)),user)
  ifeq ($(strip $(MTK_LOG_SUPPORT_GPS)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_log_hide_gps=0
  else
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_log_hide_gps=1
  endif
  ifeq ($(strip $(MTK_MTKLOGGER_SUPPORT)),yes)
    PRODUCT_PACKAGES += MTKLogger
    ifeq ($(strip $(MTK_BTLOGGER_SUPPORT)),yes)
      PRODUCT_PACKAGES += BtTool
    endif
  endif
  ifeq ($(strip $(MTK_NETWORK_LOG_SUPPORT)),yes)
    PRODUCT_PACKAGES += netdiag
  endif
  PRODUCT_PACKAGES += tcpdump
  ifeq ($(strip $(MTK_LOG_SUPPORT_MOBILE_LOG)),yes)
    PRODUCT_PACKAGES += mobile_log_d
  endif
# Add for ModemMonitor(MDM) framework
  ifeq ($(strip $(MTK_MODEM_MONITOR_SUPPORT)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_modem_monitor_support=1
    PRODUCT_PACKAGES += \
      md_monitor \
      md_monitor_ctrl \
      MDMLSample \
      MDMConfig
  endif
  ifeq ($(strip $(MTK_AEE_SUPPORT)),yes)
    HAVE_AEE_FEATURE = yes
  else
    HAVE_AEE_FEATURE = no
  endif
# Case: Customer user load and MTK_LOG_CUSTOMER_SUPPORT = yes
else ifeq ($(strip $(MTK_LOG_CUSTOMER_SUPPORT)),yes)
  ifeq ($(strip $(MTK_LOG_SUPPORT_GPS)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_log_hide_gps=0
  else
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_log_hide_gps=1
  endif
  ifeq ($(strip $(MTK_MTKLOGGER_SUPPORT)),yes)
    PRODUCT_PACKAGES += MTKLogger
    ifeq ($(strip $(MTK_BTLOGGER_SUPPORT)),yes)
      PRODUCT_PACKAGES += BtTool
    endif
  endif
  ifeq ($(strip $(MTK_NETWORK_LOG_SUPPORT)),yes)
    PRODUCT_PACKAGES += netdiag
    PRODUCT_PACKAGES += tcpdump
  endif
  ifeq ($(strip $(MTK_LOG_SUPPORT_MOBILE_LOG)),yes)
    PRODUCT_PACKAGES += mobile_log_d
  endif
# Add for ModemMonitor(MDM) framework
  ifeq ($(strip $(MTK_MODEM_MONITOR_SUPPORT)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.mtk_modem_monitor_support=1
    PRODUCT_PACKAGES += \
      md_monitor \
      md_monitor_ctrl \
      MDMLSample \
      MDMConfig
  endif
  ifeq ($(strip $(MTK_AEE_SUPPORT)),yes)
    HAVE_AEE_FEATURE = yes
  else
    HAVE_AEE_FEATURE = no
  endif
# Other Case: Customer user load and MTK_LOG_CUSTOMER_SUPPORT = no
else
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_log_hide_gps=1
  HAVE_AEE_FEATURE = no
endif

ifeq ($(strip $(PRIZE_HIDING_NAVBAR)),yes)
PRODUCT_PROPERTY_OVERRIDES += ro.support_hiding_navbar=1
endif

ifeq ($(strip $(PRIZE_FP_SUNWAVE_SW531)),yes)
PRODUCT_COPY_FILES += vendor/prize/fp/sunwave_sw531/bin/fpserver:system/bin/fpserver

PRODUCT_COPY_FILES += vendor/prize/fp/sunwave_sw531/lib/libfpClient_sw.so:system/lib/libfpClient_sw.so
PRODUCT_COPY_FILES += vendor/prize/fp/sunwave_sw531/lib/libfpService_sw.so:system/lib/libfpService_sw.so
PRODUCT_COPY_FILES += vendor/prize/fp/sunwave_sw531/lib/libfpjni_sw.so:system/lib/libfpjni_sw.so
PRODUCT_COPY_FILES += vendor/prize/fp/sunwave_sw531/lib/hw/fingerprint.default.so:system/lib/hw/fingerprint.default.so
PRODUCT_COPY_FILES += vendor/prize/fp/sunwave_sw531/lib/hw/sw_sensor.default.so:system/lib/hw/sw_sensor.default.so

PRODUCT_COPY_FILES += vendor/prize/fp/sunwave_sw531/lib64/libfpClient_sw.so:system/lib64/libfpClient_sw.so
PRODUCT_COPY_FILES += vendor/prize/fp/sunwave_sw531/lib64/libfpjni_sw.so:system/lib64/libfpjni_sw.so
PRODUCT_COPY_FILES += vendor/prize/fp/sunwave_sw531/lib64/hw/fingerprint.default.so:system/lib64/hw/fingerprint.default.so
PRODUCT_COPY_FILES += vendor/prize/fp/sunwave_sw531/lib64/hw/sw_sensor.default.so:system/lib64/hw/sw_sensor.default.so

# PRODUCT_PROPERTY_OVERRIDES += ro.prize_fingerprint=1

# endif
PRODUCT_PACKAGES += fingerprintd
  PRODUCT_COPY_FILES += \
frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml 
endif

# AEE Config
ifeq ($(HAVE_AEE_FEATURE),yes)
  ifneq ($(MTK_CHIPTEST_INT),yes)
    ifneq ($(wildcard vendor/mediatek/proprietary/external/aee_config_internal/init.aee.mtk.rc),)
$(call inherit-product-if-exists, vendor/mediatek/proprietary/external/aee_config_internal/aee.mk)
    else
$(call inherit-product-if-exists, vendor/mediatek/proprietary/external/aee/config_external/aee.mk)
    endif
  else  # MTK_CHEPTEST_INT
$(call inherit-product-if-exists, vendor/mediatek/proprietary/external/aee/config_external/aee.mk)
  endif  # MTK_CHEPTEST_INT
PRODUCT_COPY_FILES += vendor/mediatek/proprietary/external/aee/binary/aee-config:$(TARGET_COPY_OUT_VENDOR)/etc/aee-config:mtk

PRODUCT_PROPERTY_OVERRIDES += ro.have_aee_feature=1
PRODUCT_COPY_FILES += vendor/mediatek/proprietary/external/aee/binary/bin/debuggerd:system/bin/debuggerd:mtk
  ifeq ($(MTK_K64_SUPPORT), yes)
    PRODUCT_COPY_FILES += vendor/mediatek/proprietary/external/aee/binary/bin/debuggerd64:system/bin/debuggerd64:mtk
  endif
# MRDUMP
PRODUCT_PACKAGES += \
    libmrdump \
    mrdump_tool \
    ksyms-query
endif

# Modem log daemon built in according to feature option flow.
ifeq ($(strip $(MTK_INTERNAL)),yes)
  ifeq ($(strip $(MTK_MDLOGGER_SUPPORT)),yes)
    PRODUCT_PACKAGES += \
      libmdloggerrecycle \
      libmemoryDumpEncoder \
      mdlogger
    ifneq ($(strip $(MTK_MD1_SUPPORT)),)
      ifneq ($(strip $(MTK_MD1_SUPPORT)),0)
        PRODUCT_PACKAGES += emdlogger1
      endif
    endif
    ifneq ($(strip $(MTK_MD2_SUPPORT)),)
      ifneq ($(strip $(MTK_MD2_SUPPORT)),0)
        PRODUCT_PACKAGES += emdlogger2
      endif
    endif
    ifneq ($(strip $(MTK_MD5_SUPPORT)),)
      ifneq ($(strip $(MTK_MD5_SUPPORT)),0)
        PRODUCT_PACKAGES += emdlogger5
      endif
    endif
    #  $(call inherit-product-if-exists, vendor/mediatek/proprietary/protect-app/external/emdlogger/usb_port.mk)
    ifneq ($(wildcard device/mediatek/$(shell echo $(MTK_PLATFORM) | tr '[A-Z]' '[a-z]')/emdlogger_usb_config.prop),)
      PRODUCT_COPY_FILES += device/mediatek/$(shell echo $(MTK_PLATFORM) | tr '[A-Z]' '[a-z]')/emdlogger_usb_config.prop:$(TARGET_COPY_OUT_VENDOR)/etc/emdlogger_usb_config.prop:mtk
    endif
  endif
  ifneq ($(strip $(MTK_MDLOGGER_SUPPORT)),yes)
    ifeq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
      PRODUCT_PACKAGES += libmdloggerrecycle
    endif
  endif
  ifeq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
    PRODUCT_PACKAGES += emdlogger3
  endif
else ifneq ($(strip $(TARGET_BUILD_VARIANT)),user)
  ifeq ($(strip $(MTK_MDLOGGER_SUPPORT)),yes)
    PRODUCT_PACKAGES += \
      libmdloggerrecycle \
      libmemoryDumpEncoder \
      mdlogger
    ifneq ($(strip $(MTK_MD1_SUPPORT)),)
      ifneq ($(strip $(MTK_MD1_SUPPORT)),0)
        PRODUCT_PACKAGES += emdlogger1
      endif
    endif
    ifneq ($(strip $(MTK_MD2_SUPPORT)),)
      ifneq ($(strip $(MTK_MD2_SUPPORT)),0)
        PRODUCT_PACKAGES += emdlogger2
      endif
    endif
    ifneq ($(strip $(MTK_MD5_SUPPORT)),)
      ifneq ($(strip $(MTK_MD5_SUPPORT)),0)
        PRODUCT_PACKAGES += emdlogger5
      endif
    endif
    #  $(call inherit-product-if-exists, vendor/mediatek/proprietary/protect-app/external/emdlogger/usb_port.mk)
    ifneq ($(wildcard device/mediatek/$(shell echo $(MTK_PLATFORM) | tr '[A-Z]' '[a-z]')/emdlogger_usb_config.prop),)
      PRODUCT_COPY_FILES += device/mediatek/$(shell echo $(MTK_PLATFORM) | tr '[A-Z]' '[a-z]')/emdlogger_usb_config.prop:$(TARGET_COPY_OUT_VENDOR)/etc/emdlogger_usb_config.prop:mtk
    endif
  endif
  ifneq ($(strip $(MTK_MDLOGGER_SUPPORT)),yes)
    ifeq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
      PRODUCT_PACKAGES += libmdloggerrecycle
    endif
  endif
  ifeq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
    PRODUCT_PACKAGES += emdlogger3
  endif
else ifeq ($(strip $(MTK_LOG_CUSTOMER_SUPPORT)),yes)
  ifeq ($(strip $(MTK_MDLOGGER_SUPPORT)),yes)
    PRODUCT_PACKAGES += \
      libmdloggerrecycle \
      libmemoryDumpEncoder \
      mdlogger
    ifneq ($(strip $(MTK_MD1_SUPPORT)),)
      ifneq ($(strip $(MTK_MD1_SUPPORT)),0)
        PRODUCT_PACKAGES += emdlogger1
      endif
    endif
    ifneq ($(strip $(MTK_MD2_SUPPORT)),)
      ifneq ($(strip $(MTK_MD2_SUPPORT)),0)
        PRODUCT_PACKAGES += emdlogger2
      endif
    endif
    ifneq ($(strip $(MTK_MD5_SUPPORT)),)
      ifneq ($(strip $(MTK_MD5_SUPPORT)),0)
        PRODUCT_PACKAGES += emdlogger5
      endif
    endif
    #  $(call inherit-product-if-exists, vendor/mediatek/proprietary/protect-app/external/emdlogger/usb_port.mk)
    ifneq ($(wildcard device/mediatek/$(shell echo $(MTK_PLATFORM) | tr '[A-Z]' '[a-z]')/emdlogger_usb_config.prop),)
      PRODUCT_COPY_FILES += device/mediatek/$(shell echo $(MTK_PLATFORM) | tr '[A-Z]' '[a-z]')/emdlogger_usb_config.prop:$(TARGET_COPY_OUT_VENDOR)/etc/emdlogger_usb_config.prop:mtk
    endif
  endif
  ifneq ($(strip $(MTK_MDLOGGER_SUPPORT)),yes)
    ifeq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
      PRODUCT_PACKAGES += libmdloggerrecycle
    endif
  endif
  ifeq ($(strip $(RAT_CONFIG_C2K_SUPPORT)),yes)
    PRODUCT_PACKAGES += emdlogger3
  endif
endif

ifeq ($(strip $(CUSTOM_KERNEL_BIOMETRIC_SENSOR)),yes)
  PRODUCT_PACKAGES += biosensord_nvram
  PRODUCT_PACKAGES += libbiosensor
endif

ifeq ($(strip $(NFC_CHIP_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.nfc.vendor=STMicro
  PRODUCT_PROPERTY_OVERRIDES += ro.nfc.chipset=st21nfc
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_nfc_addon_support=1
  PRODUCT_BOOT_JARS += com.st.android.nfc_extensions
  PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.nfc.xml:system/etc/permissions/android.hardware.nfc.xml \
    frameworks/native/data/etc/android.hardware.nfc.hce.xml:system/etc/permissions/android.hardware.nfc.hce.xml \
    frameworks/native/data/etc/android.hardware.nfc.hcef.xml:system/etc/permissions/android.hardware.nfc.hcef.xml \
    frameworks/native/data/etc/com.android.nfc_extras.xml:system/etc/permissions/com.android.nfc_extras.xml \
    vendor/mediatek/proprietary/frameworks/opt/ST-Extension/nfc/nfcee_access.xml:system/etc/nfcee_access.xml:mtk \
    vendor/mediatek/proprietary/frameworks/opt/ST-Extension/frameworks/NfcExtensions/com.st.android.nfc_extensions.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/com.st.android.nfc_extensions.xml:mtk

  ifeq ($(strip $(TARGET_BUILD_VARIANT)),eng)
    PRODUCT_COPY_FILES += \
      vendor/mediatek/proprietary/frameworks/opt/ST-Extension/nfc/libnfc-st.conf:system/vendor/etc/libnfc-st.conf
  else
    PRODUCT_COPY_FILES += \
      vendor/mediatek/proprietary/frameworks/opt/ST-Extension/nfc/libnfc-st.conf-user:system/vendor/etc/libnfc-st.conf
  endif

  ifeq ($(strip $(ST_NFC_FM_SE_CONFIG)), 3)
    ifneq ($(wildcard $(MTK_PROJECT_FOLDER)/script_DB10mtk_2UICC.txt),)
      ST_NFC_SCRIPT_PATH = $(MTK_PROJECT_FOLDER)/script_DB10mtk_2UICC.txt
    else
      ST_NFC_SCRIPT_PATH = device/mediatek/$(MTK_PLATFORM_DIR)/script_DB10mtk_2UICC.txt
    endif
    PRODUCT_COPY_FILES += \
      $(ST_NFC_SCRIPT_PATH):$(TARGET_COPY_OUT_VENDOR)/etc/script_DB10mtk.txt:mtk
  endif
  ifeq ($(strip $(ST_NFC_FM_SE_CONFIG)), 5)
    ifneq ($(wildcard $(MTK_PROJECT_FOLDER)/script_DB10mtk_UICC_eSE.txt),)
      ST_NFC_SCRIPT_PATH = $(MTK_PROJECT_FOLDER)/script_DB10mtk_UICC_eSE.txt
    else
      ST_NFC_SCRIPT_PATH = device/mediatek/$(MTK_PLATFORM_DIR)/script_DB10mtk_UICC_eSE.txt
    endif
    PRODUCT_COPY_FILES += \
      $(ST_NFC_SCRIPT_PATH):$(TARGET_COPY_OUT_VENDOR)/etc/script_DB10mtk.txt:mtk
  endif

  PRODUCT_PACKAGES += \
    nfc_nci.st21nfc.default \
    libstnfc-nci \
    libnfc_st_jni \
    Nfc_st \
    Tag \
    com.android.nfc_extras \
    com.st.android.nfc_extensions \
    STFlashTool \
    nfcstackp \
    stDTAservice \
    libdtaNfaProvider \
    libstDTA \
    STNFCDta \
    ixitdata.xml
endif

# Add for log much detect
ifeq ($(strip $(MTK_INTERNAL)), yes)
   PRODUCT_PROPERTY_OVERRIDES += ro.mtk_internal=1
endif

# Add WallpaperPicker
PRODUCT_PACKAGES += WallpaperPicker

ifdef CUSTOM_MODEM
  ifeq ($(strip $(TARGET_BUILD_VARIANT)),eng)
    MTK_MODEM_MODULE_MAKEFILES := $(foreach item,$(CUSTOM_MODEM),$(firstword $(wildcard vendor/mediatek/proprietary/modem/$(patsubst %_prod,%,$(item))/Android.mk vendor/mediatek/proprietary/modem/$(item)/Android.mk)))
  else
    MTK_MODEM_MODULE_MAKEFILES := $(foreach item,$(CUSTOM_MODEM),$(firstword $(wildcard vendor/mediatek/proprietary/modem/$(patsubst %_prod,%,$(item))_prod/Android.mk vendor/mediatek/proprietary/modem/$(item)/Android.mk)))
  endif
  MTK_MODEM_APPS_MAKEFILES :=
  $(foreach f,$(MTK_MODEM_MODULE_MAKEFILES),\
    $(if $(strip $(MTK_MODEM_APPS_MAKEFILES)),,\
      $(eval MTK_MODEM_APPS_MAKEFILES := $(wildcard $(patsubst %/Android.mk,%/makefile/product_*.mk,$(f))))\
    )\
  )
  $(foreach f,$(MTK_MODEM_APPS_MAKEFILES),\
    $(eval $(call inherit-product-if-exists,$(f)))\
  )
endif

ifeq ($(strip $(MTK_SINGLE_BIN_MODEM_SUPPORT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_single_bin_modem_support=1
endif
# add front fingerprint-20170321-tangan
ifeq ($(strip $(PRIZE_FRONT_FINGERPRINT)),yes)
       PRODUCT_PROPERTY_OVERRIDES += ro.prize_fingerprint_front=1
    DEVICE_PACKAGE_OVERLAYS += \
        vendor/prize/front_fingerprint_res_overlay
endif
#prize-factorytest-lanwm-20171103-end
ifeq ($(strip $(MTK_FULLSCREEN_SWITCH_SUPPORT)), yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.mtk_fullscreen_switch=1
  PRODUCT_PACKAGES += FullscreenSwitchService
  PRODUCT_PACKAGES += FullscreenMode
  PRODUCT_PACKAGES += FullscreenSwitchProvider
endif

####################################################################################################
############################========prize add start==============###################################
####################################################################################################
#--------------------------------one----------------------------------------------------------------

#add PRIZE_CUSTOMER_NAME
ifneq ($(strip $(PRIZE_CUSTOMER_NAME)),)
PRODUCT_PROPERTY_OVERRIDES += ro.prize_customer=$(strip $(PRIZE_CUSTOMER_NAME))
endif

#add PRIZE_BOARD_NETWORK_TYPE
ifneq ($(strip $(PRIZE_BOARD_NETWORK_TYPE)),)
PRODUCT_PROPERTY_OVERRIDES += ro.prize_board_network_type=$(strip $(PRIZE_BOARD_NETWORK_TYPE))
endif

#add DEFAULT_INPUT_METHOD
ifneq (, $(strip $(DEFAULT_INPUT_METHOD)))
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_default_ime =$(DEFAULT_INPUT_METHOD)
endif
$(call inherit-product-if-exists, packages/apps/PrebuildApps/prebuild_apps.mk)

#PRIZE-PrizeFactoryTest-2017-12-06-start
PRODUCT_PACKAGES += PrizeFactoryTest
#PRIZE-PrizeFactoryTest-2017-12-06-end

#add PrizeImei
PRODUCT_PACKAGES += PrizeImei

#PRIZE-HwInfo-2017-12-06-start
PRODUCT_PACKAGES += PrizeHwInfo
PRODUCT_PROPERTY_OVERRIDES += ro.prize_hw_info=1
#PRIZE-HwInfo-2017-12-06-end

#PRIZE-PRIZE_LED-2017-12-06-start
ifeq ($(strip $(PRIZE_LED)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_led=1
endif
ifeq ($(strip $(PRIZE_LED_YC)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_led_yc=1
endif
#PRIZE-PRIZE_LED-2017-12-06-end

#add PRIZE_TORCH_FRONT
ifeq ($(strip $(PRIZE_TORCH_FRONT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.torch_front=1
endif

#add PRIZE_HALL
ifeq ($(strip $(PRIZE_HALL)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_hall=1
endif

#add PRIZE_OTG_TEST
ifeq ($(strip $(PRIZE_OTG_TEST)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_otg=1
endif

#PRIZE-L_PBAPK_PRIZE_ICONTROLRESISTANT-2017-12-06-start
ifeq ($(strip $(L_PBAPK_PRIZE_ICONTROLRESISTANT)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_infrared=1
endif
ifeq ($(strip $(L_PBAPK_PRIZE_ICONTROLRESISTANT_SYS)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_infrared=1
endif
#PRIZE-L_PBAPK_PRIZE_ICONTROLRESISTANT-2017-12-06-end

#prize-add-by-zhongweilin-Auto test-start
ifeq ($(strip $(PRIZE_AutoTest_APP)),yes)
    PRODUCT_PACKAGES += PrizeAutoTest
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_auto_test=1
    PRODUCT_PACKAGES += autotest_client
endif


#PRIZE-PRIZE_FACTORY_TEST_REAR_CAMERA_SUB-2017-12-06-start
ifeq ($(strip $(PRIZE_FACTORY_TEST_REAR_CAMERA_SUB)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_rear_camera_sub=1
endif
ifeq ($(strip $(PRIZE_FACTORY_TEST_REAR_CAMERA_SUB_ALS)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_rear_camera_sub_als=1
endif
#PRIZE-PRIZE_FACTORY_TEST_REAR_CAMERA_SUB-2017-12-06-end

#add COULOMBMETER VOLTAGE
ifeq ($(strip $(FTM_COULOMBMETER_CW2015)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.ftm_coulombmeter=1
endif

#--------------------------------two----------------------------------------------------------

#add PRIZE_COMPASS_APP
ifeq ($(strip $(PRIZE_COMPASS_APP)), yes) 
	PRODUCT_PACKAGES += PrizeCompass
endif

#add PRIZE_SALES_STATIS
PRODUCT_PROPERTY_OVERRIDES += ro.prize.project.ver=$(PRIZE_SALES_STATIS)

#add PRIZE_GENE_APP
ifeq ($(strip $(PRIZE_GENE_APP)), yes)
    PRODUCT_PACKAGES += PrizeGene
endif

#add PRIZE_FLASH_APP
ifeq ($(strip $(PRIZE_FLASH_APP)), yes)
    PRODUCT_PACKAGES += PrizeFlash
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_flash_app=1
endif

#add PRIZE_WELCOME_APP
ifeq ($(strip $(PRIZE_WELCOME_APP)),yes)
   PRODUCT_PACKAGES += PrizeWelcome
   PRODUCT_PACKAGES += PrizeHtml
endif

#add PRIZE_BARRAGE_WINDOW
ifeq ($(strip $(PRIZE_BARRAGE_WINDOW)),yes)
PRODUCT_PACKAGES += PrizeBarrageWindow
PRODUCT_PROPERTY_OVERRIDES += ro.prize_barrage_window=1
endif

#---------------------------------three------------------------------------------------------------

#add PRIZE_FINGERPRINT
ifeq ($(strip $(PRIZE_FINGERPRINT_APPLOCK)), yes)
	PRODUCT_PACKAGES += FingerprintApplock
	PRODUCT_PROPERTY_OVERRIDES += ro.prize_fingerprint_applock=1
	PRODUCT_PROPERTY_OVERRIDES += ro.prize_fingerprint=1
endif
ifeq ($(strip $(PRIZE_FINGER_PRINT)),afs083t)
PRODUCT_COPY_FILES += vendor/fingerprint/afs083t/libfprint-x64.so:system/lib64/libfprint-x64.so
PRODUCT_COPY_FILES += vendor/fingerprint/afs083t/libma-fpservice.so:system/lib64/libma-fpservice.so
PRODUCT_COPY_FILES += vendor/fingerprint/afs083t/fingerprint.default.so:system/lib64/hw/fingerprint.default.so
PRODUCT_PACKAGES += fingerprintd
PRODUCT_PACKAGES += FingerprintApplock
endif

#  add by hushilun
ifeq ($(strip $(PRIZE_FINGER_PRINT)),sw9362)
include vendor/fingerprint/sunwavecorp_sw531/sw_cfg.mk
PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw531/bin/fpserver:system/bin/fpserver
PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw531/bin/FpServiceTest:system/bin/FpServiceTest
PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw531/bin/sunwave_test:system/bin/sunwave_test
PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw531/lib/libfpService_sw.so:system/lib/libfpService_sw.so
PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw531/lib/libfpClient_sw.so:system/lib/libfpClient_sw.so
PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw531/lib/libfpjni_sw.so:system/lib/libfpjni_sw.so
PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw531/lib/hw/fingerprint.default.so:system/lib/hw/fingerprint.default.so
PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw531/lib/hw/sw_sensor.default.so:system/lib/hw/sw_sensor.default.so
PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw531/lib64/hw/sw_sensor.default.so:system/lib64/hw/sw_sensor.default.so
PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw531/lib64/libfpClient_sw.so:system/lib64/libfpClient_sw.so
PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw531/lib64/libfpjni_sw.so:system/lib64/libfpjni_sw.so
PRODUCT_COPY_FILES += vendor/fingerprint/sunwavecorp_sw531/lib64/hw/fingerprint.default.so:system/lib64/hw/fingerprint.default.so
PRODUCT_PACKAGES += fingerprintd
PRODUCT_PACKAGES += FingerprintApplock
#PRODUCT_COPY_FILES += frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml
endif
ifneq ($(strip $(PRIZE_FINGER_PRINT)),)
PRODUCT_COPY_FILES += frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml
PRODUCT_PROPERTY_OVERRIDES += ro.prize_fingerprint=1
endif

#-------------------------------four----------------------------------------------------------------

#add PRIZE-FACEBEAUTY
ifeq ($(strip $(FN_FACEBEAUTY_SUPPORT)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_camera_fn_facebeauty = 1
endif
#prize-xuchunming-201804026-add spotlight-start
PRODUCT_COPY_FILES += vendor/mediatek/proprietary/hardware/mtkcam/fotonation/VSFB/VfbTuningInterface.xml:system/VfbTuningInterface.xml
#prize-xuchunming-201804026-add spotlight-end
ifeq ($(strip $(PRIZE_CAMERA_FACEBEAUTY)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_cam_vfb=1
endif
ifeq ($(strip $(GANGYUN_CAMERA_BEAUTY)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.gangyun_camera_beauty=1
  PRODUCT_PACKAGES += GYBeautySnap_V1.2
endif

#-----------------------------------five--------------------------------------------------
#prize add by xiarui for smart cleaner
ifeq ($(strip $(PRIZE_SMART_CLEANER)), yes)
	PRODUCT_PACKAGES += PrizeSmartCleaner
	PRODUCT_PROPERTY_OVERRIDES += ro.prize_smart_cleaner = 1
endif

#prize add by xiarui for vibrate
ifeq ($(strip $(PRIZE_VIBRATE_CONTROL)), yes)
	PRODUCT_PROPERTY_OVERRIDES += ro.prize_vibrate_control = 1
endif

#prize add by xiarui for led blink when incoming call
ifeq ($(strip $(PRIZE_LED_BLINK_WHEN_INCOMING)), yes)
	PRODUCT_PROPERTY_OVERRIDES += ro.prize_led_blink = 1
endif

#add PRIZE_NOTEPAD
ifeq ($(strip $(PRIZE_NOTEPAD)), V8)
	PRODUCT_PACKAGES += PrizeNotePad
endif
ifeq ($(strip $(PRIZE_FILE_EXPLORER)), V8)
	PRODUCT_PACKAGES += PrizeFileManager
endif
ifeq ($(strip $(PRIZE_HOR_CALI)),yes)
	PRODUCT_PACKAGES += HorCali
endif
ifeq ($(strip $(PRIZE_USER_MANUAL)),yes)
    PRODUCT_PACKAGES += KoobeeUsermanual
endif
ifeq ($(strip $(PRIZE_FLOAT_WINDOW)),yes)
  PRODUCT_PACKAGES += FloatWindow
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_float_window=1
endif
ifeq ($(strip $(PRIZE_NET_CONTROL)), yes)
	PRODUCT_PACKAGES += PrizeNetControl
endif
	PRODUCT_PACKAGES += LongShotScreen \
						FrameRecorder				
ifeq ($(strip $(ADUPS_FOTA_SUPPORT)), yes)
	PRODUCT_PACKAGES += AdupsFota \
                    AdupsFotaReboot
endif						
ifeq ($(strip $(PRIZE_HONGBAO_AUTO_HELPER)), yes)
    PRODUCT_PROPERTY_OVERRIDES += persist.hongbao.auto.helper=1
endif
ifeq ($(strip $(PRIZE_XIAOKU)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_xiao_ku = 1
endif
ifeq ($(strip $(PRIZE_DIALER_TMSDK_CALLMARK_SUPPORT)), yes)
	PRODUCT_PROPERTY_OVERRIDES += ro.prize_callmark_support=1
endif					
ifeq ($(strip $(PRIZE_OLD_LAUNCHER)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_old_launcher=1
endif
ifeq ($(strip $(PRIZE_SHUTMENU)),yes)
	PRODUCT_PROPERTY_OVERRIDES += ro.prize_shut_menu=1
endif
$(call inherit-product-if-exists, external/local-wallpapers/koobee/wallpapers.mk)
ifeq ($(strip $(PRIZE_CHANGED_WALLPAPER)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_changed_wallpaper=1
    ifeq ($(strip $(PRIZE_CUSTOMER_NAME)),coosea)
        $(call inherit-product-if-exists, external/keyguard-wallpapers/coosea/keyguardwallpapers.mk)
    else
        $(call inherit-product-if-exists, external/keyguard-wallpapers/koobee/keyguardwallpapers.mk)
    endif
endif
#PRIZE-prize-public-standard:Changed lock screen-liuweiquan-20151210-start
ifneq ($(strip $(PRIZE_HAOKAN_SCREENVIEW)),yes)
ifeq ($(strip $(PRIZE_CHANGED_WALLPAPER)),yes)
    ifeq ($(strip $(PRIZE_CUSTOMER_NAME)),coosea)
        $(call inherit-product-if-exists, external/keyguard-wallpapers/coosea/keyguardwallpapers.mk)
    else
        $(call inherit-product-if-exists, external/keyguard-wallpapers/koobee/keyguardwallpapers.mk)
    endif
endif
endif
#PRIZE-prize-public-standard:Changed lock screen-liuweiquan-20151210-end

#PRIZE-ChangedWallpaper-liuweiquan-2015-12-12-start
ifneq ($(strip $(PRIZE_HAOKAN_SCREENVIEW)),yes)
ifeq ($(strip $(PRIZE_CHANGED_WALLPAPER)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_changed_wallpaper=1
endif
endif
#PRIZE-ChangedWallpaper-liuweiquan-2015-12-12-end
ifeq ($(strip $(PRIZE_HAOKAN_SCREENVIEW)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_haokan_screenview = 1
endif

ifeq ($(strip $(PRIZE_LOCKSCREEN_INVERSE)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_lockscreen_inverse=1
endif

ifeq ($(strip $(PRIZE_NOTCH_SCREEN)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_notch_screen=1
endif

ifeq ($(strip $(PRIZE_NOTCH_SCREEN2)),yes)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_notch_screen2=1
endif

PRODUCT_PACKAGES += SoundRecorder
#---------------------------------six-----------------------------------------------------

ifeq ($(strip $(PRIZE_POWER_EXTEND_MODE)),yes)
    PRODUCT_PACKAGES += PrizeSuperPowerSave
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_power_extend_mode=1
endif
ifeq ($(strip $(PRIZE_PURE_BACKGROUND)),yes)
	PRODUCT_PACKAGES += PureBackground
	PRODUCT_PACKAGES += PrizeSecurity
	PRODUCT_PROPERTY_OVERRIDES += ro.prize_pure_background=1
endif
#PRIZE-xuchunming-20170712-add circle window at screen four corner-start
ifeq ($(strip $(PRIZE_SCREEN_CIRCLECORNER)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize.screen.circlecorner=1
endif
#PRIZE-xuchunming-20170712-add circle window at screen four corner-end
ifeq ($(strip $(PRIZE_CLOUD_LIST)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.cloudlist = 1
	PRODUCT_PACKAGES += PrizeCloudList
	PRODUCT_PACKAGES += PrizeBlacklistProvider
endif 
ifeq ($(strip $(PRIZE_TTS_SUPPORT)), yes)
	PRODUCT_PROPERTY_OVERRIDES += ro.prize_tts_support=1
	PRODUCT_COPY_FILES += vendor/prize/baidutts/bd_etts_speech_female.dat:system/etc/baidutts/bd_etts_speech_female.dat
	PRODUCT_COPY_FILES += vendor/prize/baidutts/bd_etts_speech_female_en.dat:system/etc/baidutts/bd_etts_speech_female_en.dat
	PRODUCT_COPY_FILES += vendor/prize/baidutts/bd_etts_speech_male.dat:system/etc/baidutts/bd_etts_speech_male.dat
	PRODUCT_COPY_FILES += vendor/prize/baidutts/bd_etts_speech_male_en.dat:system/etc/baidutts/bd_etts_speech_male_en.dat
	PRODUCT_COPY_FILES += vendor/prize/baidutts/bd_etts_text.dat:system/etc/baidutts/bd_etts_text.dat
	PRODUCT_COPY_FILES += vendor/prize/baidutts/bd_etts_text_en.dat:system/etc/baidutts/bd_etts_text_en.dat
	PRODUCT_COPY_FILES += vendor/prize/baidutts/prize_license:system/etc/baidutts/prize_license
endif
$(call prize_add_sys_prop,PRIZE_SUPPORT_APP_MULTI_INSTANCES,ro.prize_app_multi_inst)
$(call prize_add_sys_prop,PRIZE_CONTROL_APPS_NETWORK_FOR_SLEEPING,ro.p_ctl_app_net_for_sleeping)
$(call prize_add_sys_prop,PRIZE_SUPPORT_HIDING_NAVBAR,ro.support_hiding_navbar)
$(call prize_add_sys_prop_inv,PRIZE_SUPPORT_HIDING_NAVBAR,qemu.hw.mainkeys)
$(call prize_add_sys_prop,PRIZE_LOW_MEMORY_OPTIMIZE,ro.prize_low_memory_optimize)
$(call prize_add_sys_prop,PRIZE_NAV_BAR_COLOR_CUST,ro.p_nav_bar_color_cust)
$(call prize_add_sys_prop,PRIZE_SUPPORT_SMART_KILLER,ro.prize_support_sys_res_mon)
$(call prize_add_sys_prop,PRIZE_SUPPORT_SMART_KILLER_WITH_DEBUG_UI,ro.prize_support_sys_res_mon)

ifeq (yes, $(strip $(PRIZE_FINGERPRINT_MBACK)))
$(call prize_add_sys_prop_full,PRIZE_SUPPORT_NAV_BAR_FOR_MBACK_DEVICE,ro.p_support_nav_bar_for_mback)
endif

# Prize Process Manager. prize-linkh-20180319 @{
ifneq ($(strip $(PRIZE_SUPPORT_PROC_MANAGER)), no)
TMP_PRIZE_PROC_MGR_FLAG := 1
TMP_PRIZE_GEN_BIT_NUM_SHELL_SCRIPT := build/tools/genBitNum.sh
# Those values are from PrizeOption.java. See that file for details.
# activity switch. (1<<1)
ifneq ($(strip $(PRIZE_SUPPORT_PROC_MANAGER_ACT)), no)
TMP_PRIZE_PROC_MGR_FLAG := $(shell $(TMP_PRIZE_GEN_BIT_NUM_SHELL_SCRIPT) $(TMP_PRIZE_PROC_MGR_FLAG) or \
								$(shell $(TMP_PRIZE_GEN_BIT_NUM_SHELL_SCRIPT) 1 ls 1))
endif
# broadcast switch. (1<<2)
ifneq ($(strip $(PRIZE_SUPPORT_PROC_MANAGER_BR)), no)
TMP_PRIZE_PROC_MGR_FLAG := $(shell $(TMP_PRIZE_GEN_BIT_NUM_SHELL_SCRIPT) $(TMP_PRIZE_PROC_MGR_FLAG) or \
								$(shell $(TMP_PRIZE_GEN_BIT_NUM_SHELL_SCRIPT) 1 ls 2))
endif
# service switch. (1<<3)
ifneq ($(strip $(PRIZE_SUPPORT_PROC_MANAGER_SR)), no)
TMP_PRIZE_PROC_MGR_FLAG := $(shell $(TMP_PRIZE_GEN_BIT_NUM_SHELL_SCRIPT) $(TMP_PRIZE_PROC_MGR_FLAG) or \
								$(shell $(TMP_PRIZE_GEN_BIT_NUM_SHELL_SCRIPT) 1 ls 3))
endif
# provider switch. (1<<4)
ifneq ($(strip $(PRIZE_SUPPORT_PROC_MANAGER_PR)), no)
TMP_PRIZE_PROC_MGR_FLAG := $(shell $(TMP_PRIZE_GEN_BIT_NUM_SHELL_SCRIPT) $(TMP_PRIZE_PROC_MGR_FLAG) or \
								$(shell $(TMP_PRIZE_GEN_BIT_NUM_SHELL_SCRIPT) 1 ls 4))
endif

ifndef TMP_PRIZE_PROC_MGR_FLAG
$(error Undefined TMP_PRIZE_PROC_MGR_FLAG value!)
else
#$(warning ====> TMP_PRIZE_PROC_MGR_FLAG=$(TMP_PRIZE_PROC_MGR_FLAG))
PRODUCT_PROPERTY_OVERRIDES += ro.prize_proc_mgr_flg=$(TMP_PRIZE_PROC_MGR_FLAG)
endif

TMP_PRIZE_PROC_MGR_FLAG :=
endif
# END @}

#prize-add-by-tangzeming-20171115-start
ifeq ($(strip $(PRIZE_FACE_ID)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_faceid = 1
endif
ifeq ($(strip $(PRIZE_FACE_ID_KOOBEE)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_faceidkoobee = 1
endif
#prize-add-by-tangzeming-20171115-end

#prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar start
ifeq ($(strip $(PRIZE_SUPPORT_NAV_BAR_FOR_MBACK_DEVICE)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.p_support_nav_bar_for_mback = 1
endif

ifeq ($(strip $(PRIZE_SWIPE_UP_GESTURE_NAVIGATION)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_gesture_navigation = 1
endif
#prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar end

# prize-add lihuangyuan-pingan face detected-2017-10-24-start

PRODUCT_COPY_FILES += packages/apps/PrizeSettings/jni/armeabi-v7a/libcaffe-jni.so:system/lib/libcaffe-jni.so \
                         packages/apps/PrizeSettings/jni/armeabi-v7a/libface_detect.so:system/lib/libface_detect.so

PRODUCT_COPY_FILES += packages/apps/PrizeSettings/jni/arm64-v8a/libcaffe-jni.so:system/lib64/libcaffe-jni.so \
                         packages/apps/PrizeSettings/jni/arm64-v8a/libface_detect.so:system/lib64/libface_detect.so

# prize-add lihuangyuan-pingan face detected-2017-10-24-end
#----------------------------------other---------------------------------------

ifneq ($(strip $(PRIZE_AUTO_GEN_WIFI_ADDRESS)), no)
PRODUCT_PACKAGES += wifimacaddr
endif
PRODUCT_COPY_FILES += device/mediatek/common/prizemusic/prizemusic.sh:system/etc/prizemusic.sh
PRODUCT_PROPERTY_OVERRIDES += persist.prize.music=1
PRODUCT_COPY_FILES += device/mediatek/common/prizemusic/audiosource/Alice.mp3:system/media/audio/music/Alice.mp3
PRODUCT_COPY_FILES += device/mediatek/common/prizemusic/audiosource/VanillaGarden.mp3:system/media/audio/music/VanillaGarden.mp3
PRODUCT_COPY_FILES += device/mediatek/common/prizevideo/prizevideo.sh:system/etc/prizevideo.sh
PRODUCT_PROPERTY_OVERRIDES += persist.prize.video=1
ifeq ($(strip $(LCM_WIDTH)),540)
PRODUCT_COPY_FILES += device/mediatek/common/prizevideo/videosource/brand/960_540.mp4:system/media/koobeevideo/960_540.mp4
endif
ifeq ($(strip $(LCM_WIDTH)),720)
PRODUCT_COPY_FILES += device/mediatek/common/prizevideo/videosource/brand/1280_720.mp4:system/media/koobeevideo/1280_720.mp4
endif
ifeq ($(strip $(LCM_WIDTH)),1080)
PRODUCT_COPY_FILES += device/mediatek/common/prizevideo/videosource/brand/1920_1080.mp4:system/media/koobeevideo/1920_1080.mp4
endif

#prize-add-by-zhongweilin-Old_test-start
ifeq ($(strip $(PRIZE_RUNOLDTEST_APP)),yes)
    PRODUCT_PACKAGES += PrizeRunOldTest
    #DDR test tools
    PRODUCT_PACKAGES += memtester
endif
#prize-add-by-zhongweilin-Old_test-end

#prize-xuchunming-20180405-add faceid-start
ifeq ($(strip $(PRIZE_FACE_ID)),yes)
  PRODUCT_PACKAGES += PrizeFaceUnlock
endif
#prize-xuchunming-20180405-add faceid-end
    PRODUCT_PACKAGES += PrizePermission

#prize-xuchunming-201804026-add spotlight-start
ifeq ($(strip $(PRIZE_SPOTLIGHT)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize.spotlight=1
endif
#prize-xuchunming-201804026-add spotlight-end

#prize-taoyingyou-20180514-add  4G+_switch 57339-start
ifeq ($(strip $(PRIZE_CMCC_SWITCH)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize.cmcc_switch=1
endif
#prize-taoyingyou-20180514-add 4G+_switch -end

#-prize-add by lihuangyuan,for insertsim reboot-2018-05-22-start
ifeq ($(strip $(PRIZE_INSERTSIM_REBOOT)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize.insertsim.reboot=1
endif
#-prize-add by lihuangyuan,for insertsim reboot-2018-05-22-end

#-prize-add by huangpengfei,for incomming call game modle -2018-05-28-start
ifneq ($(strip $(PRIZE_CALL_GAME_MODLE)),no)
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_call_game_modle=1
endif
#-prize-add by huangpengfei,for incomming call game modle -2018-05-28-end
ifeq ($(strip $(PRIZE_FORBID_ERROR_DIALOG)),yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_forbid_error_dialog = 1
endif
ifeq ($(strip $(PRIZE_ADB_MANAGER)),yes)
    PRODUCT_PROPERTY_OVERRIDES += persist.prize.adb.manager = 1
endif

#prize add by zenghui-20180530 for smart wifi switch start
ifeq ($(strip $(PRIZE_SMART_WIFI_SWITCH)), yes)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize_smart_wifi_switch = 1
endif
#prize add by zenghui-20180530 for smart wifi switch end
#prize add by mafei-20180703 for new floatwindow start
ifeq ($(strip $(PRIZE_NEW_FLOAT_WINDOW)),yes)
  PRODUCT_PACKAGES += PrizeNewFloatWindow
  PRODUCT_PROPERTY_OVERRIDES += ro.prize_new_float_window=1
endif
#prize add by mafei-20180703 for new floatwindow end

#prize-modify-cootek sdk- longzhongping-2018.06.21-start
ifneq ($(strip $(PRIZE_COOTEK_SDK)), no)
    PRODUCT_PROPERTY_OVERRIDES += ro.prize.cootek.location = 1
endif
#prize-modify-cootek sdk- longzhongping-2018.06.21-end
####################################################################################################
############################========prize add end==============#####################################
####################################################################################################
