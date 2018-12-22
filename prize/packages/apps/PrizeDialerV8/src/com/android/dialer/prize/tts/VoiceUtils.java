package com.android.dialer.util;

import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

/**
 * @author Administrator
 *	语音 开关类0 表示关  1表示 开
 */
public class VoiceUtils {
	/**
	 * 桌面语音开关
	 */
	public static String PRIZE_VOICE_LAUNCHER_KEY = "prize_voice_launcher_key";  
	/**
	 * 通话语音开关
	 */
	public static String PRIZE_VOICE_CALL_KEY = "prize_voice_call_key";
	/**
	 * 短信语音开关
	 */
	public static String PRIZE_VOICE_MMS_KEY = "prize_voice_mms_key";
	/**
	 * 拨号语音开关
	 */
	public static String PRIZE_VOICE_DIALER_KEY = "prize_voice_dialer_key";
	/**
	 * 语音总开关
	 */
	public static String PRIZE_VOICE_KEY = "prize_voice_key";

	/**
	 * @param key 
	 * @param c
	 * @return  0 表示 关  1 表示开
	 */
	public static int getKey(String key, Context c) {
		int result = 0;
		try {
			result = Settings.System.getInt(c.getContentResolver(), key);
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	
}
