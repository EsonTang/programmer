#add by HEKEYI 20170617
LOCAL_PATH :=$(TOPDIR)packages/apps/PrebuildApps/SougouBrowser

		ifeq ($(L_PBAPK_PRIZE_SOUGOU_BROWSER),yes)
		    PRODUCT_PACKAGES += SougouBrowser
		endif
		ifeq ($(L_PBAPK_PRIZE_SOUGOU_BROWSER_SYS),yes)
		    PRODUCT_PACKAGES += SougouBrowser
		endif
#end...