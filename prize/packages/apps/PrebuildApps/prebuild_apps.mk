## add 3rdparty app to system.image prize-wangyunhe 20150417
LOCAL_PATH :=$(TOPDIR)packages/apps/PrebuildApps

ifeq ($(L_PBAPK_PRIZE_GAMEPLAY),yes)
    PRODUCT_PACKAGES += GamePlay
endif
ifeq ($(MTK_HOTKNOT_SUPPORT),yes)
		PRODUCT_PACKAGES += PrizeHotKnot
endif
ifeq ($(L_PBAPK_KOOBEE_CENTER),yes)
		PRODUCT_PACKAGES += KoobeeCenter
endif
ifeq ($(L_PBAPK_KUSAI_CENTER),yes)
		PRODUCT_PACKAGES += KusaiCenter
endif
ifeq ($(L_PBAPK_PRIZE_APP_CENTER),yes)
		PRODUCT_PACKAGES += PrizeAppCenter
endif  
ifeq ($(L_PBAPK_PRIZE_CLOUD),yes)
		PRODUCT_PACKAGES += PrizeCloud
endif  

ifeq ($(PRIZE_WEATHER),yes)
    PRODUCT_PACKAGES += PrizeWeather
#    PRODUCT_COPY_FILES +=$(LOCAL_PATH)/PrizeWeather/PrizeWeatherlib/liblocSDK3.so:system/lib/liblocSDK3.so
endif
ifeq ($(PRIZE_VIDEO),yes)
    PRODUCT_PACKAGES += PrizeVideo
endif
ifeq ($(PRIZE_VIDEO_V63),yes)
    PRODUCT_PACKAGES += PrizeVideo
endif
ifeq ($(PRIZE_MUSIC_V63),yes)
    PRODUCT_PACKAGES += PrizeMusic
endif
ifeq ($(PRIZE_MUSIC),yes)
    PRODUCT_PACKAGES += PrizeMusic
endif

ifeq ($(L_PBAPK_SHANCHUAN_VENDOR),yes)
		PRODUCT_PACKAGES += xender
endif
ifeq ($(strip $(L_PBAPK_PRIZE_YIDIANZIXUN_SYS)), yes)
		PRODUCT_PACKAGES += yidianzixun
endif

ifeq ($(strip $(L_PBAPK_PRIZE_YIDIANZIXUN)), yes)
		PRODUCT_PACKAGES += yidianzixun
endif

ifeq ($(strip $(L_PBAPK_PRIZE_JOKEESSAY)), yes)
		PRODUCT_PACKAGES += JokeEssay
endif
		
ifeq ($(strip $(L_PBAPK_PRIZE_JOKEESSAY_SYS)), yes)
		PRODUCT_PACKAGES += JokeEssay
endif
ifeq ($(strip $(L_PBAPK_PRIZE_YOUKU)), yes)
		PRODUCT_PACKAGES += Youku
endif
		
ifeq ($(strip $(L_PBAPK_PRIZE_YOUKU_SYS)), yes)
		PRODUCT_PACKAGES += Youku
endif
ifeq ($(strip $(L_PBAPK_PRIZE_CHINESECALENDAR)), yes)
		PRODUCT_PACKAGES += ChineseCalendar
endif
		
ifeq ($(strip $(L_PBAPK_PRIZE_CHINESECALENDAR_SYS)), yes)
		PRODUCT_PACKAGES += ChineseCalendar
endif
		
ifeq ($(strip $(L_PBAPK_SOHU_NEWS)), yes)
		PRODUCT_PACKAGES += SohuNewsClient
endif
		
ifeq ($(strip $(L_PBAPK_SOHU_NEWS_SYS)), yes)
		PRODUCT_PACKAGES += SohuNewsClient
endif

ifeq ($(strip $(L_PBAPK_AMAP)), yes)
		PRODUCT_PACKAGES += amap
endif
		
ifeq ($(strip $(L_PBAPK_AMAP_SYS)), yes)
		PRODUCT_PACKAGES += amap
endif			

ifeq ($(strip $(L_PBAPK_SOGOU_INPUT)), yes)
		PRODUCT_PACKAGES += SogouInput
endif
		
ifeq ($(strip $(L_PBAPK_SOGOU_INPUT_SYS)), yes)
		PRODUCT_PACKAGES += SogouInput
endif

ifeq ($(strip $(L_PBAPK_PRIZE_HAOSOU)), yes)
		PRODUCT_PACKAGES += haosou
endif
		
ifeq ($(strip $(L_PBAPK_PRIZE_HAOSOU_SYS)), yes)
		PRODUCT_PACKAGES += haosou
endif

ifeq ($(strip $(L_PBAPK_PRIZE_QQSTOCK)), yes)
		PRODUCT_PACKAGES += QQStock
endif
		
ifeq ($(strip $(L_PBAPK_PRIZE_QQSTOCK_SYS)), yes)
		PRODUCT_PACKAGES += QQStock
endif

ifeq ($(strip $(L_PBAPK_PRIZE_QQbrowser)), yes)
		PRODUCT_PACKAGES += QQbrowser
endif
		
ifeq ($(strip $(L_PBAPK_PRIZE_QQbrowser_SYS)), yes)
		PRODUCT_PACKAGES += QQbrowser
endif

ifeq ($(strip $(L_PBAPK_PRIZE_TENCENT_NEWS)), yes)
		PRODUCT_PACKAGES += TencentNews
endif
		
ifeq ($(strip $(L_PBAPK_PRIZE_TENCENT_NEWS_SYS)), yes)
		PRODUCT_PACKAGES += TencentNews
endif

ifeq ($(strip $(L_PBAPK_CTRIP)), yes)
		PRODUCT_PACKAGES += ctrip
endif
		
ifeq ($(strip $(L_PBAPK_CTRIP_SYS)), yes)
		PRODUCT_PACKAGES += ctrip
endif

ifeq ($(strip $(L_PBAPK_TENCENT_VIDEO)), yes)
		PRODUCT_PACKAGES += TencentVideo
endif
		
ifeq ($(strip $(L_PBAPK_TENCENT_VIDEO_SYS)), yes)
		PRODUCT_PACKAGES += TencentVideo
endif

ifeq ($(PRIZE_SECURITY_CENTER),yes)
    PRODUCT_PACKAGES += PrizeSecurityCenter
	#PRODUCT_COPY_FILES +=$(LOCAL_PATH)/PrizeSecurityCenter/lib/libams-1.2.8-mfr.so:system/lib/libams-1.2.8-mfr.so \
	#					 $(LOCAL_PATH)/PrizeSecurityCenter/lib/libbuffalo-1.0.0-mfr.so:system/lib/libbuffalo-1.0.0-mfr.so \
	#					 $(LOCAL_PATH)/PrizeSecurityCenter/lib/libbumblebee-1.0.4-mfr.so:system/lib/libbumblebee-1.0.4-mfr.so \
	##					 $(LOCAL_PATH)/PrizeSecurityCenter/lib/libTmsdk-2.0.10-mfr.so:system/lib/libTmsdk-2.0.10-mfr.so \
	#					 $(LOCAL_PATH)/PrizeSecurityCenter/lib/libdce-1.1.17-mfr.so:system/lib/libdce-1.1.17-mfr.so
						 
		PRODUCT_COPY_FILES +=$(LOCAL_PATH)/PrizeSecurityCenter/lib64/libams-1.2.8-mfr.so:system/lib64/libams-1.2.8-mfr.so \
						 $(LOCAL_PATH)/PrizeSecurityCenter/lib64/libbuffalo-1.0.0-mfr.so:system/lib64/libbuffalo-1.0.0-mfr.so \
						 $(LOCAL_PATH)/PrizeSecurityCenter/lib64/libbumblebee-1.0.4-mfr.so:system/lib64/libbumblebee-1.0.4-mfr.so \
						 $(LOCAL_PATH)/PrizeSecurityCenter/lib64/libTmsdk-2.0.10-mfr.so:system/lib64/libTmsdk-2.0.10-mfr.so \
						 $(LOCAL_PATH)/PrizeSecurityCenter/lib64/libdce-1.1.17-mfr.so:system/lib64/libdce-1.1.17-mfr.so					 
endif

#add for obtain liuhai screen--liufan-2018-04-19-start
ifeq ($(strip $(PRIZE_NOTCH_SCREEN)),yes)
    PRODUCT_COPY_FILES += \
        frameworks/native/data/etc/com.prize.notch.screen.xml:system/etc/permissions/com.prize.notch.screen.xml
endif
#add for obtain liuhai screen--liufan-2018-04-19-end

ifeq ($(strip $(PRIZE_FILE_EXPLORER)),BUILT_IN)
	PRODUCT_PACKAGES += PrizeFileManager
endif

ifeq ($(L_PBAPK_NEWSARTICLE),yes)
	PRODUCT_PACKAGES += NewsArticle
endif
ifeq ($(L_PBAPK_NEWSARTICLE_SYS),yes)
	PRODUCT_PACKAGES += NewsArticle
endif
ifeq ($(L_PBAPK_PRIZE_QQREADER),yes)
	PRODUCT_PACKAGES += QQReader
endif
ifeq ($(L_PBAPK_PRIZE_QQREADER_SYS),yes)
	PRODUCT_PACKAGES += QQReader
endif
ifeq ($(L_PBAPK_DISCOUNTEIGHTHUNDRED),yes)
	PRODUCT_PACKAGES += DiscountEightHundred
endif
ifeq ($(L_PBAPK_DISCOUNTEIGHTHUNDRED_SYS),yes)
	PRODUCT_PACKAGES += DiscountEightHundred
endif
ifeq ($(L_PBAPK_QY_VIDEO),yes)
	PRODUCT_PACKAGES += iQIYI
endif
ifeq ($(L_PBAPK_QY_VIDEO_SYS),yes)
	PRODUCT_PACKAGES += iQIYI
endif

#--prize--add by lihuangyuan for rootcheck support-2017-04-17-start
ifeq ($(PRIZE_ROOTCHECK_SUPPORT),yes)
	PRODUCT_PACKAGES += PrizeRootCheck
endif
#--prize--add by lihuangyuan for rootcheck support-2017-04-17-end

ifeq ($(L_PBAPK_IFLYINPUT),yes)
	PRODUCT_PACKAGES += iFlyInput
endif

ifeq ($(L_PBAPK_IFLYINPUT_SYS),yes)
	PRODUCT_PACKAGES += iFlyInput
endif
ifeq ($(L_PBAPK_SOGOUSEARCH),yes)
	PRODUCT_PACKAGES += Sogousearch
endif

ifeq ($(L_PBAPK_SOGOUSEARCH_SYS),yes)
	PRODUCT_PACKAGES += Sogousearch
endif

ifeq ($(L_PBAPK_PRIZE_TIANTIANKUAIBAO),yes)
	PRODUCT_PACKAGES += TencentReading
endif

ifeq ($(L_PBAPK_PRIZE_TIANTIANKUAIBAO_SYS),yes)
	PRODUCT_PACKAGES += TencentReading
endif

ifeq ($(L_PBAPK_PINGAN_FEATURE),yes)
	PRODUCT_PACKAGES += PinganFeature
endif

ifeq ($(L_PBAPK_PINGAN_FEATURE_SYS),yes)
	PRODUCT_PACKAGES += PinganFeature
endif

#add vipshop app
    $(call inherit-product, packages/apps/PrebuildApps/VipShop/add_vipshop.mk)
#add jd app
    $(call inherit-product, packages/apps/PrebuildApps/jd/add_jd.mk)
#add sinablog app
    $(call inherit-product, packages/apps/PrebuildApps/SinaBlog/add_sinablog.mk)
#add sougou browser app
    $(call inherit-product, packages/apps/PrebuildApps/SougouBrowser/add_sougoubrowser.mk)
#add migu migu app
    $(call inherit-product, packages/apps/PrebuildApps/MiguReader/add_migureader.mk)
#add miaopai migu app
    $(call inherit-product, packages/apps/PrebuildApps/Miaopai/add_miaopai.mk)

#haokanscreen iteration one--liufan-2016-06-23-start	
ifeq ($(PRIZE_HAOKAN_SCREENVIEW),yes)
PRODUCT_PACKAGES += haoKan
PRODUCT_COPY_FILES +=$(LOCAL_PATH)/haoKan/lib64/libweibosdkcore.so:system/lib64/libweibosdkcore.so

PRODUCT_COPY_FILES +=$(LOCAL_PATH)/haoKan/lib32/libweibosdkcore.so:system/lib/libweibosdkcore.so
endif
#haokanscreen iteration one--liufan-2016-06-23-end
##### app multi instances feature. prize-linkh-20160119
ifeq (yes, $(strip $(PRIZE_SUPPORT_APP_MULTI_INSTANCES)))
ifeq (yes, $(strip $(PRIZE_CREATE_SHORCUT_FOR_APP_MULTI_INST_TEST)))
	PRODUCT_PACKAGES += PrizeCreateAppInstShortcut
endif
ifeq (yes, $(strip $(PRIZE_CONTROL_APP_MULTI_INST_TEST)))
	PRODUCT_PACKAGES += PrizeDisableAppInst
endif
endif
######## end

##### Smart Killer App. prize-linkh-20160119
ifeq (yes, $(strip $(PRIZE_SUPPORT_SMART_KILLER)))
PRODUCT_PACKAGES += PrizeSysResMon
PRODUCT_COPY_FILES += $(LOCAL_PATH)/PrizeSysResMon/pre_sk_list.xml:system/etc/pre_sk_list.xml
else ifeq (yes, $(strip $(PRIZE_SUPPORT_SMART_KILLER_WITH_DEBUG_UI)))
PRODUCT_PACKAGES += PrizeSysResMon_DebugUI
PRODUCT_COPY_FILES += $(LOCAL_PATH)/PrizeSysResMon/pre_sk_list.xml:system/etc/pre_sk_list.xml
endif
######## end

##### Prize Debug Tool. prize-linkh-20160919
PRODUCT_PACKAGES += PrizeDebugTool

####cooee  doudizhu
    ifeq ($(L_PBAKP_KU_FIGHT_AGAINST_LANDLORDS),yes)
		PRODUCT_PACKAGES += kuyu_fight_against_landlords            
    endif
		
	ifeq ($(L_PBAKP_KU_FIGHT_AGAINST_LANDLORDS_SYS),yes)
		PRODUCT_PACKAGES += kuyu_fight_against_landlords
		            
	endif

ifeq ($(L_PBAPK_PRIZE_LAUNCHER_SUPPORT),yes)
    $(call inherit-product, packages/apps/PrebuildApps/PrizeLauncher/add_prizeapps.mk)
	PRODUCT_COPY_FILES +=$(LOCAL_PATH)/PrizeLauncher/lockscreen.png:system/lockscreen.png
else
		PRODUCT_PACKAGES += Launcher3
endif

#prize-add-by-yangming-20170920-start
ifeq ($(strip $(PRIZE_XIAOKU)), yes)
    PRODUCT_PACKAGES += PrizeXiaoKu
endif
#prize-add-by-yangming-20170920-end

#prize add zhaojian 8.0 2017725 start
ifeq ($(strip $(PRIZE_HONGBAO_AUTO_HELPER)), yes)
    PRODUCT_PACKAGES += PrizeHBhelper
endif
#prize add zhaojian 8.0 2017725 end
#not use so noted it 
#	PRODUCT_PACKAGES += Prize_LockScreen

# prize doze white list app. prize-linkh-20171228
ifeq ($(strip $(PRIZE_DOZE_WHITE_LIST_APP)),yes)
	PRODUCT_PACKAGES += PrizeDozeWhiteList
endif

ifeq ($(L_PBAPK_KB_COMMUNITY),yes)
 	     PRODUCT_PACKAGES += koobeecommunity
endif
ifeq ($(L_PBAPK_KB_COMMUNITY_SYS),yes)
	 PRODUCT_PACKAGES += koobeecommunity
endif
#--prize--add by lihuangyuan for tts support-2017-03-15-start
ifeq ($(PRIZE_TTS_SUPPORT),yes)
	PRODUCT_PACKAGES += TTSServerApp
endif
#--prize--add by lihuangyuan for tts support-2017-03-15-end
#ifeq ($(strip $(L_PBAPK_PINGAN_FEATURE)),yes)
#PRODUCT_PACKAGES += Pinan
#endif
#add meituan app
    $(call inherit-product, packages/apps/PrebuildApps/meituan/add_meituan.mk)
#add baidu app
    $(call inherit-product, packages/apps/PrebuildApps/baidu/add_baiduapps.mk)
#add tencent app
    $(call inherit-product, packages/apps/PrebuildApps/tencent/add_tencentapps.mk)
#add pptv app
    $(call inherit-product, packages/apps/PrebuildApps/PPTV/add_pptv.mk)
#add uc_browser app
    $(call inherit-product, packages/apps/PrebuildApps/Android_UCBrowser/add_ucbrowser.mk)
#add cloud_music app
    $(call inherit-product, packages/apps/PrebuildApps/CloudMusic/add_cloudmusic.mk)
#add iReader app
    $(call inherit-product, packages/apps/PrebuildApps/iReader/add_ireader.mk)
#add IcontrolResistant app
    $(call inherit-product, packages/apps/PrebuildApps/IcontrolResistant/add_icontrolresistant.mk)
#add ifengnews app
    $(call inherit-product, packages/apps/PrebuildApps/IfengNews/add_ifengnews.mk)
#add 360 app
    $(call inherit-product, packages/apps/PrebuildApps/360/add_360apps.mk)
#add wangyi email app
    $(call inherit-product, packages/apps/PrebuildApps/wangyiEmail/add_email.mk)
#add wangyi 2345browser app
    $(call inherit-product, packages/apps/PrebuildApps/2345browser/add_2345browser.mk)
#add yumai apk
    $(call inherit-product, packages/apps/PrebuildApps/APKYumai/add_yumai.mk)
#PRIZE-add-longbaoxiu-2017-04-13-start
    $(call inherit-product, packages/apps/PrebuildApps/PrizeAppCenter/add_appcenter.mk)
#PRIZE-add-longbaoxiu-2017-04-13-end
#add kuai video apk
    $(call inherit-product, packages/apps/PrebuildApps/CPI_app/kuaiVideo/add_kuaivideo.mk)
    $(call inherit-product, packages/apps/PrebuildApps/BoBoVideo/add_bobovideo.mk)
    $(call inherit-product, packages/apps/PrebuildApps/kuaikanxiaoshuo/add_kuaikanxiaoshuo.mk)
########################################################## end

#add prizecalendarv8 apk
    $(call inherit-product, packages/apps/PrebuildApps/PrizeCalendarV8/add_prizecalendar.mk)
#add cpi apk
    $(call inherit-product, packages/apps/PrebuildApps/CPI_app/TencentNewsCPI/add_tencentnews_cpi.mk)
    $(call inherit-product, packages/apps/PrebuildApps/CPI_app/TencentVideoCPI/add_tencentvideo_cpi.mk)
    $(call inherit-product, packages/apps/PrebuildApps/CPI_app/MiaoPaiCPI/add_miaopai_cpi.mk)
# add sougou browser
    $(call inherit-product, packages/apps/PrebuildApps/CPI_app/SougouBrowserCPI/add_sougoubrowser_cpi.mk)
#add  konka3d
    $(call inherit-product, packages/apps/PrebuildApps/Konka3D/prebuild3D.mk)
	
ifeq ($(strip $(OPTR_SPEC_SEG_DEF)), OP01_SPEC0200_SEGC)
    $(call inherit-product, packages/apps/PrebuildApps/cmccnew/prebuild_cmcc.mk)
endif
#PRIZE-add-longbaoxiu-2017-04-13-start
    $(call inherit-product, packages/apps/PrebuildApps/PrizeAppCenter/add_appcenter.mk)
#PRIZE-add-longbaoxiu-2017-04-13-end
########################################################## end
