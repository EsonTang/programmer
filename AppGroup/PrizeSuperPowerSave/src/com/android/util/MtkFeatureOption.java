package com.android.util;

import android.os.Build;
import java.lang.reflect.Field;

public class MtkFeatureOption {

	public static boolean getUnreadSupport() {
		int SDK_version = Build.VERSION.SDK_INT;
		boolean unreadfeatureOption = false;
		String mtkFeatureOptionString = "com.mediatek.featureoption.FeatureOption";
		if (SDK_version > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			mtkFeatureOptionString = "com.mediatek.common.prizeoption.PrizeOption";
		}

		try {
			Class<?> mtkFeatureOption = Class.forName(mtkFeatureOptionString);
			Field unreadSupport = mtkFeatureOption.getField("MTK_LAUNCHER_UNREAD_SUPPORT");
			unreadfeatureOption = (Boolean) unreadSupport.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return unreadfeatureOption;
	}

	public static boolean getSimGeminiSupport() {
		int SDK_version = Build.VERSION.SDK_INT;
		boolean geminifeatureOption = false;
		String mtkFeatureOptionString = "com.mediatek.featureoption.FeatureOption";
		if (SDK_version > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			mtkFeatureOptionString = "com.mediatek.common.prizeoption.PrizeOption";
		}

		try {
			Class<?> mtkFeatureOption = Class.forName(mtkFeatureOptionString);
			Field geminiSupport = mtkFeatureOption.getField("MTK_GEMINI_SUPPORT");
			geminifeatureOption = (Boolean) geminiSupport.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return geminifeatureOption;
	}

}
