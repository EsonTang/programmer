#add by lyt-lan 20130422
LOCAL_PATH :=$(TOPDIR)packages/apps/PrebuildApps/PrizeLauncher

ifeq ($(L_PBAPK_PRIZE_LAUNCHER_SUPPORT),yes)
	PRODUCT_PACKAGES += ThemeStore
	PRODUCT_PACKAGES += InfoStream
	PRODUCT_PACKAGES += PrizeAppOutAd
	PRODUCT_PACKAGES += PrizeGlobalData
	PRODUCT_PACKAGES += PrizeGameCenter
	PRODUCT_PACKAGES += PrizeBrowser
	PRODUCT_PACKAGES += PrizeTips
	ifeq ($(L_PBAPK_PRIZE_EASYLAUNCHER),yes)
	PRODUCT_PACKAGES += PrizeEasyLauncher
	else
	PRODUCT_PACKAGES += PrizeLauncher
	endif
								
    PRODUCT_COPY_FILES += $(LOCAL_PATH)/lib/lib/liblqtheme.so:system/lib/liblqtheme.so
    PRODUCT_COPY_FILES += $(LOCAL_PATH)/lib/lib/liblqthemecore.so:system/lib/liblqthemecore.so
    PRODUCT_COPY_FILES += $(LOCAL_PATH)/lib/lib64/liblqtheme.so:system/lib64/liblqtheme.so
    PRODUCT_COPY_FILES += $(LOCAL_PATH)/lib/lib64/liblqthemecore.so:system/lib64/liblqthemecore.so
    PRODUCT_COPY_FILES += $(LOCAL_PATH)/lib/lib64/liblocSDK6a.so:system/lib64/liblocSDK6a.so
	  

	ifeq ($(strip $(LCM_WIDTH)),1080)
		wallpapersPath=wallpaper_1080
	else
		wallpapersPath=wallpaper
	endif

	ifeq ($(strip $(L_SW_TOTAL_COPIED_PRIZE_WALLPAPERS)),)
		L_SW_TOTAL_COPIED_PRIZE_WALLPAPERS=14
	endif
	#filter strings.
	filter_wp_strings := $(LOCAL_PATH)/$(wallpapersPath)/wallpaper%
	#filter out the exist wallpaper
	PRODUCT_COPY_FILES := $(filter-out $(filter_wp_strings), $(PRODUCT_COPY_FILES))
	# assign new values.
	COPY_OUR_WALLPAPERS := $(foreach num, $(shell $(LOCAL_PATH)/genSerialNumbers.sh $(L_SW_TOTAL_COPIED_PRIZE_WALLPAPERS)), \
		$(eval PRODUCT_COPY_FILES += \
			$(LOCAL_PATH)/$(wallpapersPath)/wallpaper$(num)/wallpaper$(num).jpg:system/media/config/wallpaper/wallpaper$(num)/wallpaper$(num).jpg \
			$(LOCAL_PATH)/$(wallpapersPath)/wallpaper$(num)/wallpaper$(num)_icon.png:system/media/config/wallpaper/wallpaper$(num)/wallpaper$(num)_icon.png \
			$(LOCAL_PATH)/$(wallpapersPath)/wallpaper$(num)/wallpaper$(num)_preview.jpg:system/media/config/wallpaper/wallpaper$(num)/wallpaper$(num)_preview.jpg))

	PRODUCT_COPY_FILES +=$(LOCAL_PATH)/$(wallpapersPath)/config.xml:system/media/config/wallpaper/config.xml



	
	ifeq ($(strip $(LCM_WIDTH)),1080)
		themesFolder=theme_1080
	else
		themesFolder=theme
	endif
	ifeq ($(strip $(PRIZE_THEMES)),)
		ifeq ($(strip $(LCM_WIDTH)),1080)
		PRIZE_THEMES := shensuizhanlan mengguaidangan fuguangliujin meihuoyaohong xingjizhiguang yuanzhicaiyun qingyechizi
		else
		PRIZE_THEMES := jinsenianhua mengguaidangan shuiguopaidui xingguangcuican xingjizhiguang yuanzhicaiyun zhiruochujian
		endif 
	endif
	COPY_OUR_THEME := $(foreach themeFolder, $(PRIZE_THEMES), \
				$(eval PRODUCT_COPY_FILES += \
					$(LOCAL_PATH)/$(themesFolder)/$(themeFolder)/$(themeFolder)_preview0.png:system/media/config/theme/$(themeFolder)/$(themeFolder)_preview0.png\
							 $(LOCAL_PATH)/$(themesFolder)/$(themeFolder)/$(themeFolder)_preview1.png:system/media/config/theme/$(themeFolder)/$(themeFolder)_preview1.png\
							 $(LOCAL_PATH)/$(themesFolder)/$(themeFolder)/$(themeFolder)_icon.png:system/media/config/theme/$(themeFolder)/$(themeFolder)_icon.png\
							 $(LOCAL_PATH)/$(themesFolder)/$(themeFolder)/$(themeFolder).jar:system/media/config/theme/$(themeFolder)/$(themeFolder).jar))
	PRODUCT_COPY_FILES +=$(LOCAL_PATH)/$(themesFolder)/config.xml:system/media/config/theme/config.xml						 
	
	ifeq ($(strip $(PRIZE_DEFAULT_THEME)),)
		PRIZE_DEFAULT_THEME=default
	endif

	PRODUCT_COPY_FILES += \
		$(LOCAL_PATH)/$(themesFolder)/$(PRIZE_DEFAULT_THEME)/$(PRIZE_DEFAULT_THEME)_preview0.png:system/media/config/theme/default/default_preview0.png\
		$(LOCAL_PATH)/$(themesFolder)/$(PRIZE_DEFAULT_THEME)/$(PRIZE_DEFAULT_THEME)_preview1.png:system/media/config/theme/default/default_preview1.png\
		$(LOCAL_PATH)/$(themesFolder)/$(PRIZE_DEFAULT_THEME)/$(PRIZE_DEFAULT_THEME)_icon.png:system/media/config/theme/default/default_icon.png\
		$(LOCAL_PATH)/$(themesFolder)/$(PRIZE_DEFAULT_THEME)/$(PRIZE_DEFAULT_THEME).jar:system/media/config/theme/default/default.jar
		

	PRODUCT_COPY_FILES +=$(LOCAL_PATH)/default_config/default_config.xml:system/media/config/default_config/default_config.xml		
	PRODUCT_COPY_FILES +=$(LOCAL_PATH)/default_config/default_workspace_koobee.xml:system/media/config/default_config/default_workspace_koobee.xml	
	PRODUCT_COPY_FILES +=$(LOCAL_PATH)/default_config/default_workspace_koosai.xml:system/media/config/default_config/default_workspace_koosai.xml	


	PRODUCT_COPY_FILES +=$(LOCAL_PATH)/default_config/overlay_icon_koosai.xml:system/media/config/default_config/overlay_icon_koosai.xml	
	PRODUCT_COPY_FILES +=$(LOCAL_PATH)/default_config/overlay_icon_koobee.xml:system/media/config/default_config/overlay_icon_koobee.xml		
						 
endif



#end....wangyh


