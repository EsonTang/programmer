package com.android.settings.powermaster;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.SystemProperties;
import com.android.internal.view.RotationPolicy;
/*import com.mediatek.audioprofile.AudioProfileManager;*/
import com.android.settings.R;
import com.android.settings.Utils;

public class CPUModeUtils {

	private static final String TAG = "CPUModeUtils";

	public static final int FAST_FUNCTION_MODE_TYPE = 0;
	public static final int NORMAL_MODE_TYPE = 1;
	public static final int NOOPSYCHE_SAVE_MODE_TYPE = 2;
	public static final int SUPER_POWER_SAVE_MODE_TYPE = 3;

	private static final String PROPERTY_POWER_MODE= "persist.sys.power.mode";

	public static final String CPU_MODE_TYPE = "cpu_mode_type";

	public static final String ORIGNAL_BLUETOOTH_STATUS = "orignal_bluetooth";
	public static final String ORIGNAL_WIFI_STATUS = "orignal_wifi";
	public static final String ORIGNAL_WIFI_HOT_POINT_STATUS = "orignal_wifi_hot_point";
	public static final String ORIGNAL_GPS_STATUS = "orignal_gps";
	public static final String ORIGNAL_DATA_FLOW_STATUS = "orignal_data_flow";
	public static final String ORIGNAL_SCREEN_BRIGHTNESS_MODE = "orignal_screen_brightness_mode";
	public static final String ORIGNAL_SCREEN_BRIGHTNESS_STATUS = "orignal_screen_brightness";
	public static final String ORIGNAL_SCREEN_OFF_TIMEOUT_STATUS = "orignal_screen_off_timeout";
	public static final String ORIGNAL_ROTATION_POLICY_STATUS = "orignal_rotation_policy";
	public static final String ORIGNAL_TACTILE_FEEDBACK = "orignal_tactile_feedback";

	private static final int HUNDRED = 100;
	private static final int NORMAL_BRIGHTNESS = 20;
	private static final int MAX_BRIGHTNESS = 255;

	public static void setCpuModeType(Context context,int type){
		ContentResolver mContentResolver = context.getContentResolver();
		Settings.System.putInt(mContentResolver, CPU_MODE_TYPE,type);
	}

	public static int getCpuModeType(Context context){
		ContentResolver mContentResolver = context.getContentResolver();
		return Settings.System.getInt(mContentResolver, CPU_MODE_TYPE,1);
	}

	public static void setNoopsycheSaveMode(Context context){
		intoNoopsycheSaveMode(context);
	}

	public static void intoNoopsycheSaveMode(Context context){
		ContentResolver mContentResolver = context.getContentResolver();
		int modeType = getModeType(context);

		// Bluetooth
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
			if(modeType != NOOPSYCHE_SAVE_MODE_TYPE){
				// Bluetooth
				if(mBluetoothAdapter.isEnabled()){
					Settings.System.putInt(mContentResolver, ORIGNAL_BLUETOOTH_STATUS,1);
				}else if(mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()){
					Settings.System.putInt(mContentResolver, ORIGNAL_BLUETOOTH_STATUS,0);
				}
			}
			if(mBluetoothAdapter.isEnabled()){
				mBluetoothAdapter.disable();
			}
			mBluetoothAdapter = null;
		}

		// Wifi
		WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (mWifiManager != null){
			if(modeType != NOOPSYCHE_SAVE_MODE_TYPE){
				if (!mWifiManager.isWifiEnabled() && mWifiManager.isWifiApEnabled()){
					Settings.System.putInt(mContentResolver, ORIGNAL_WIFI_STATUS, 0);
					Settings.System.putInt(mContentResolver, ORIGNAL_WIFI_HOT_POINT_STATUS,1);
				}else if (mWifiManager.isWifiEnabled() && !mWifiManager.isWifiApEnabled()){
					Settings.System.putInt(mContentResolver, ORIGNAL_WIFI_STATUS, 1);
					Settings.System.putInt(mContentResolver, ORIGNAL_WIFI_HOT_POINT_STATUS,0);
				}else if (!mWifiManager.isWifiEnabled() && !mWifiManager.isWifiApEnabled()){
					Settings.System.putInt(mContentResolver, ORIGNAL_WIFI_STATUS, 0);
					Settings.System.putInt(mContentResolver, ORIGNAL_WIFI_HOT_POINT_STATUS,0);
				}
			}
			if(mWifiManager.isWifiApEnabled()){
				mWifiManager.setWifiApEnabled(null, false);
			}
			Log.d(TAG, "Wifi isEnabled: "+mWifiManager.isWifiEnabled());
			if(!mWifiManager.isWifiEnabled()){
				mWifiManager.setWifiEnabled(true);
			}
			mWifiManager = null;
		}

		// GPS
		if(modeType != NOOPSYCHE_SAVE_MODE_TYPE){
			int mode = Settings.Secure.getIntForUser(mContentResolver, Settings.Secure.LOCATION_MODE,
					Settings.Secure.LOCATION_MODE_OFF, UserHandle.USER_CURRENT);
			if(mode != Settings.Secure.LOCATION_MODE_OFF){
				Settings.System.putInt(mContentResolver, ORIGNAL_GPS_STATUS,1);
			}else {
				Settings.System.putInt(mContentResolver, ORIGNAL_GPS_STATUS,0);
			}
		}

		Settings.Secure.putIntForUser(mContentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF, 
				UserHandle.USER_CURRENT);

		//		// shujuliangjie
		//		TelephonyManager mTelephonyManager = TelephonyManager.from(context);
		//		if(mTelephonyManager != null){
		//			if(modeType != NOOPSYCHE_SAVE_MODE_TYPE){
		//				if(mTelephonyManager.getDataEnabled()){
		//					Settings.System.putInt(mContentResolver, ORIGNAL_DATA_FLOW_STATUS,1);
		//				}else{
		//					Settings.System.putInt(mContentResolver, ORIGNAL_DATA_FLOW_STATUS,0);
		//				}
		//			}
		//			
		//			if(mTelephonyManager.getDataEnabled()){
		//				mTelephonyManager.setDataEnabled(false);
		//			}
		//			mTelephonyManager = null;
		//		}

		// zidongsuoping
		Uri uri = Settings.System.getUriFor("screen_brightness");
		if(modeType != NOOPSYCHE_SAVE_MODE_TYPE){
			int timeout = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 15000);
			Settings.System.putInt(mContentResolver, ORIGNAL_SCREEN_OFF_TIMEOUT_STATUS,timeout);
		}
		Settings.System.putInt(mContentResolver, Settings.System.SCREEN_OFF_TIMEOUT,15000);

		// pingmuliangdu
		if(modeType != NOOPSYCHE_SAVE_MODE_TYPE){
			int automatic = Settings.System.getIntForUser(mContentResolver,Settings.System.SCREEN_BRIGHTNESS_MODE, 
					Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC,UserHandle.USER_CURRENT);
			int mIsAutoMode = automatic != Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL ? 1:0;
			Settings.System.putInt(mContentResolver, ORIGNAL_SCREEN_BRIGHTNESS_MODE,mIsAutoMode);

			int brightness = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, NORMAL_BRIGHTNESS);
			Settings.System.putInt(mContentResolver, ORIGNAL_SCREEN_BRIGHTNESS_STATUS,brightness);
		}
		Settings.System.putIntForUser(mContentResolver,Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL ,UserHandle.USER_CURRENT);
		Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, NORMAL_BRIGHTNESS);
		mContentResolver.notifyChange(uri, null);

		// shupingsuoding
		boolean isRotationLocked = RotationPolicy.isRotationLocked(context);
		if(modeType != NOOPSYCHE_SAVE_MODE_TYPE){
			if(isRotationLocked){
				Settings.System.putInt(mContentResolver, ORIGNAL_ROTATION_POLICY_STATUS,1);
			}else {
				Settings.System.putInt(mContentResolver, ORIGNAL_ROTATION_POLICY_STATUS,0);
			}
		}
		//		if(!isRotationLocked){
		//			RotationPolicy.setRotationLock(context, true);
		//		}

		// chuganfankui
		/*
		AudioProfileManager mProfileManager = (AudioProfileManager) context.getSystemService(Context.AUDIO_PROFILE_SERVICE);
		boolean isVibrateOnTouch = mProfileManager.isVibrateOnTouchEnabled("mtk_audioprofile_general");
		if(modeType != NOOPSYCHE_SAVE_MODE_TYPE){
			if (isVibrateOnTouch) {
				Settings.System.putInt(mContentResolver, ORIGNAL_TACTILE_FEEDBACK,1);
			}else {
				Settings.System.putInt(mContentResolver, ORIGNAL_TACTILE_FEEDBACK,0);
			}
		}
		if (isVibrateOnTouch) {
			mProfileManager.setVibrateOnTouchEnabled("mtk_audioprofile_general", false);
			mProfileManager.setVibrateOnTouchEnabled("mtk_audioprofile_silent", false);
			mProfileManager.setVibrateOnTouchEnabled("mtk_audioprofile_meeting", false);
			mProfileManager.setVibrateOnTouchEnabled("mtk_audioprofile_outdoor", false);
		}
		mProfileManager = null;*/
		Log.d("NoopsycheSave", "NoopsycheSave Setting End");
	}  

	public static void resumeOrignalState(Context context){
		ContentResolver mContentResolver = context.getContentResolver();
		int modeType = getModeType(context);

		if(modeType != NOOPSYCHE_SAVE_MODE_TYPE){
			// Bluetooth
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter != null) {
				int mode = Settings.System.getInt(mContentResolver, ORIGNAL_BLUETOOTH_STATUS,0);
				Log.d(TAG, "Bluetooth isEnabled: "+mBluetoothAdapter.isEnabled());
				if(mode == 1 && !mBluetoothAdapter.isEnabled()){
					mBluetoothAdapter.enable();
					Settings.System.putInt(mContentResolver, ORIGNAL_BLUETOOTH_STATUS,1);
				}
			}

			// Wifi
			WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			if (mWifiManager != null){
				int wifiMode = Settings.System.getInt(mContentResolver, ORIGNAL_WIFI_STATUS,0);
				int wifiHotPointMode = Settings.System.getInt(mContentResolver, ORIGNAL_WIFI_HOT_POINT_STATUS,0);
				int wifiState = mWifiManager.getWifiState();
				if (wifiMode !=1 && wifiHotPointMode == 1 && !mWifiManager.isWifiApEnabled()) {
					mWifiManager.setWifiEnabled(false);
					mWifiManager.setWifiApEnabled(null, true);
					Settings.Global.putInt(mContentResolver, Settings.Global.WIFI_SAVED_STATE, 1);
				} else if(wifiHotPointMode != 1 && wifiMode == 1 && !mWifiManager.isWifiEnabled()) {
					mWifiManager.setWifiEnabled(true);
				}

				Log.d(TAG, "Wifi isEnabled: "+mWifiManager.isWifiEnabled());
				Log.d(TAG, "Wifi HotPoint isEnabled: "+mWifiManager.isWifiApEnabled());
			}

			// GPS
			int gpsMode = Settings.System.getInt(mContentResolver, ORIGNAL_GPS_STATUS,0);
			int mode = Settings.Secure.getIntForUser(mContentResolver, Settings.Secure.LOCATION_MODE,
					Settings.Secure.LOCATION_MODE_OFF, UserHandle.USER_CURRENT);
			if(gpsMode == 1 && mode == Settings.Secure.LOCATION_MODE_OFF){
				Settings.Secure.putIntForUser(mContentResolver, Settings.Secure.LOCATION_MODE, 
						Settings.Secure.LOCATION_MODE_HIGH_ACCURACY, UserHandle.USER_CURRENT);
			}

			//			// shujuliangjie
			//			TelephonyManager mTelephonyManager = TelephonyManager.from(context);
			//			int dataFlowMode = Settings.System.getInt(mContentResolver, ORIGNAL_DATA_FLOW_STATUS,0);
			//			Log.d(TAG, "Data_Flow isEnabled: "+mTelephonyManager.getDataEnabled());
			//			if(dataFlowMode == 1 && !mTelephonyManager.getDataEnabled()){
			//				mTelephonyManager.setDataEnabled(true);
			//			}

			// zidongsuoping
			Uri uri = Settings.System.getUriFor("screen_brightness");
			int timeout = Settings.System.getInt(mContentResolver, ORIGNAL_SCREEN_OFF_TIMEOUT_STATUS,
					Settings.System.getInt(mContentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 15*1000));
			Settings.System.putInt(mContentResolver, Settings.System.SCREEN_OFF_TIMEOUT,timeout);

			// pingmuliangdu
			int mIsAutoMode = Settings.System.getInt(mContentResolver, ORIGNAL_SCREEN_BRIGHTNESS_MODE,0);
			if(mIsAutoMode == 1){
				Settings.System.putIntForUser(mContentResolver,Settings.System.SCREEN_BRIGHTNESS_MODE,
						Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC,UserHandle.USER_CURRENT);
			}else{
				int brightness = Settings.System.getInt(mContentResolver, ORIGNAL_SCREEN_BRIGHTNESS_STATUS,
						Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, NORMAL_BRIGHTNESS));
				Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
			}
			mContentResolver.notifyChange(uri, null);

			// shupingsuoding
			int rotationPolicyMode = Settings.System.getInt(mContentResolver, ORIGNAL_ROTATION_POLICY_STATUS,0);
			if(rotationPolicyMode == 1 && !RotationPolicy.isRotationLocked(context)){
				RotationPolicy.setRotationLock(context, true);
			}else if(rotationPolicyMode == 0 && RotationPolicy.isRotationLocked(context)){
				RotationPolicy.setRotationLock(context, false);
			}

			// chuganfankui
			/*AudioProfileManager mProfileManager = (AudioProfileManager) context.getSystemService(Context.AUDIO_PROFILE_SERVICE);
			boolean isVibrateOnTouch = mProfileManager.isVibrateOnTouchEnabled("mtk_audioprofile_general");
			int feedbackMode = Settings.System.getInt(mContentResolver, ORIGNAL_TACTILE_FEEDBACK,0);
			if(feedbackMode == 1 && !isVibrateOnTouch){
				mProfileManager.setVibrateOnTouchEnabled("mtk_audioprofile_general", true);
				mProfileManager.setVibrateOnTouchEnabled("mtk_audioprofile_silent", true);
				mProfileManager.setVibrateOnTouchEnabled("mtk_audioprofile_meeting", true);
				mProfileManager.setVibrateOnTouchEnabled("mtk_audioprofile_outdoor", true);
			}*/
		}
	} 

	public static void saveOrignalState(Context context){
		ContentResolver mContentResolver = context.getContentResolver();
		int modeType = getModeType(context);

		if(modeType != NOOPSYCHE_SAVE_MODE_TYPE){
			// Bluetooth
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()){
				Settings.System.putInt(mContentResolver, ORIGNAL_BLUETOOTH_STATUS,1);
			}else if(mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()){
				Settings.System.putInt(mContentResolver, ORIGNAL_BLUETOOTH_STATUS,0);
			}

			// Wifi
			WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			if (mWifiManager != null && !mWifiManager.isWifiEnabled() && mWifiManager.isWifiApEnabled()){
				Settings.System.putInt(mContentResolver, ORIGNAL_WIFI_STATUS, 0);
				Settings.System.putInt(mContentResolver, ORIGNAL_WIFI_HOT_POINT_STATUS,1);
			}else if (mWifiManager != null && mWifiManager.isWifiEnabled() && !mWifiManager.isWifiApEnabled()){
				Settings.System.putInt(mContentResolver, ORIGNAL_WIFI_STATUS, 1);
				Settings.System.putInt(mContentResolver, ORIGNAL_WIFI_HOT_POINT_STATUS,0);
			}else if (mWifiManager != null && !mWifiManager.isWifiEnabled() && !mWifiManager.isWifiApEnabled()){
				Settings.System.putInt(mContentResolver, ORIGNAL_WIFI_STATUS, 0);
				Settings.System.putInt(mContentResolver, ORIGNAL_WIFI_HOT_POINT_STATUS,0);
			}
			Log.d(TAG, "Wifi isEnabled: "+mWifiManager.isWifiEnabled());
			Log.d(TAG, "Wifi HotPoint isEnabled: "+mWifiManager.isWifiApEnabled());

			// GPS
			int mode = Settings.Secure.getIntForUser(mContentResolver, Settings.Secure.LOCATION_MODE,
					Settings.Secure.LOCATION_MODE_OFF, UserHandle.USER_CURRENT);
			if(mode != Settings.Secure.LOCATION_MODE_OFF){
				Settings.System.putInt(mContentResolver, ORIGNAL_GPS_STATUS,1);
			}else {
				Settings.System.putInt(mContentResolver, ORIGNAL_GPS_STATUS,0);
			}

			//			// shujuliangjie
			//			TelephonyManager mTelephonyManager = TelephonyManager.from(context);
			//			int dataFlowMode = Settings.System.getInt(mContentResolver, ORIGNAL_DATA_FLOW_STATUS,0);
			//			Log.d(TAG, "Data_Flow isEnabled: "+mTelephonyManager.getDataEnabled());
			//			if(mTelephonyManager.getDataEnabled()){
			//				Settings.System.putInt(mContentResolver, ORIGNAL_DATA_FLOW_STATUS,1);
			//			}else{
			//				Settings.System.putInt(mContentResolver, ORIGNAL_DATA_FLOW_STATUS,0);
			//			}

			// zidongsuoping
			int timeout = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 15*1000);
			Settings.System.putInt(mContentResolver, ORIGNAL_SCREEN_OFF_TIMEOUT_STATUS,timeout);

			// pingmuliangdu
			int automatic = Settings.System.getIntForUser(mContentResolver,Settings.System.SCREEN_BRIGHTNESS_MODE, 
					Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC,UserHandle.USER_CURRENT);
			int mIsAutoMode = automatic != Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL ? 1:0;
			Settings.System.putInt(mContentResolver, ORIGNAL_SCREEN_BRIGHTNESS_MODE,mIsAutoMode);

			int brightness = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, NORMAL_BRIGHTNESS);
			Settings.System.putInt(mContentResolver, ORIGNAL_SCREEN_BRIGHTNESS_STATUS,brightness);

			// shupingsuoding
			if(RotationPolicy.isRotationLocked(context)){
				Settings.System.putInt(mContentResolver, ORIGNAL_ROTATION_POLICY_STATUS,1);
			}else {
				Settings.System.putInt(mContentResolver, ORIGNAL_ROTATION_POLICY_STATUS,0);
			}

			// chuganfankui
			/*AudioProfileManager mProfileManager = (AudioProfileManager) context.getSystemService(Context.AUDIO_PROFILE_SERVICE);
			boolean isVibrateOnTouch = mProfileManager.isVibrateOnTouchEnabled("mtk_audioprofile_general");
			if (isVibrateOnTouch) {
				Settings.System.putInt(mContentResolver, ORIGNAL_TACTILE_FEEDBACK,1);
			}else {
				Settings.System.putInt(mContentResolver, ORIGNAL_TACTILE_FEEDBACK,0);
			}*/
		}
	} 

	public static String[] getModeState(Context context, int modeType){
		ContentResolver mContentResolver = context.getContentResolver();
		Resources res = context.getResources();

		String[] statusArr = new String[10];
		String opend = res.getString(R.string.opened);
		String closed = res.getString(R.string.closed);
		if(modeType != NOOPSYCHE_SAVE_MODE_TYPE){
			// cpu mode
			if(modeType == FAST_FUNCTION_MODE_TYPE){
				statusArr[0] = res.getString(R.string.turbo_mode);
			}else if(modeType == NORMAL_MODE_TYPE){
				statusArr[0] = res.getString(R.string.normal_mode);
			}

			// pingmuliangdu
			int automatic = Settings.System.getIntForUser(mContentResolver,Settings.System.SCREEN_BRIGHTNESS_MODE, 
					Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC,UserHandle.USER_CURRENT);
			int mIsAutoMode = automatic != Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL ? 1:0;
			if(mIsAutoMode == 1){
				statusArr[1] = res.getString(R.string.automatic);
			} else {
				int brightness = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, NORMAL_BRIGHTNESS);
				int percentage = brightness*HUNDRED/MAX_BRIGHTNESS;
				statusArr[1] = Utils.formatPercentage(percentage);
			}

			// zidongsuoping
			int mTimeOut = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 15*1000)/1000;
			/* prize-modify-by-lijimeng-for bugid 47941-20180110-start*/
			if(mTimeOut == 2147483){
				statusArr[2] = res.getString(R.string.zen_mode_when_never);
			}else{
			statusArr[2] = res.getString(R.string.stand_by_second,mTimeOut);
			}
			//statusArr[2] = res.getString(R.string.stand_by_second,mTimeOut);
			/* prize-modify-by-lijimeng-for bugid 47941-20180110-end*/
			// shujuliangjie
			TelephonyManager mTelephonyManager = TelephonyManager.from(context);
			boolean mDataFlowStatus = false;
			if(mTelephonyManager != null){
				mDataFlowStatus = mTelephonyManager.getDataEnabled();
			}
			statusArr[3] = mDataFlowStatus?opend:closed;

			// Wifi
			WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			boolean mWifiStatus = false;
			boolean mWifiApStatus = false;
			if (mWifiManager != null){
				mWifiStatus = mWifiManager.isWifiEnabled();
				mWifiApStatus = mWifiManager.isWifiApEnabled();
			}
			statusArr[4] = mWifiStatus?opend:closed;
			statusArr[5] = mWifiApStatus?opend:closed;

			// Bluetooth
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			boolean mBluetoothStatus = false;
			if (mBluetoothAdapter != null) {
				mBluetoothStatus = mBluetoothAdapter.isEnabled();
			}
			statusArr[6] = mBluetoothStatus?opend:closed;

			// GPS
			int mGpsMode = Settings.Secure.getIntForUser(mContentResolver, Settings.Secure.LOCATION_MODE,
					Settings.Secure.LOCATION_MODE_OFF, UserHandle.USER_CURRENT);
			boolean mGpsStatus = false;
			if(mGpsMode != Settings.Secure.LOCATION_MODE_OFF){
				mGpsStatus = true;
			}
			statusArr[7] = mGpsStatus?opend:closed;

			// chuganfankui
			/*AudioProfileManager mProfileManager = (AudioProfileManager) context.getSystemService(Context.AUDIO_PROFILE_SERVICE);
			boolean isVibrateOnTouch = mProfileManager.isVibrateOnTouchEnabled("mtk_audioprofile_general");
			statusArr[8] = isVibrateOnTouch?opend:closed;*/

			int hapticFeedBack = Settings.System.getInt(context.getContentResolver(),Settings.System.HAPTIC_FEEDBACK_ENABLED,1);
			statusArr[8] = hapticFeedBack == 1 ? opend:closed;
			/* prize-add-by-lijimeng-for bugid 45094-20180110-start*/
			// shupingsuoding
			boolean mRotationStatus = false;
			mRotationStatus = RotationPolicy.isRotationLocked(context);
			statusArr[9] = mRotationStatus?opend:closed;
		}else {
			// CPU mode
			statusArr[0] = res.getString(R.string.balance_mode);
			// pingmuliangdu
			int brightness = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, NORMAL_BRIGHTNESS);
			int percentage = brightness*HUNDRED/MAX_BRIGHTNESS;
			statusArr[1] = Utils.formatPercentage(percentage);
			// zidongsuoping
			statusArr[2] = res.getString(R.string.stand_by_second,15);
			// shujuliangjie
			TelephonyManager mTelephonyManager = TelephonyManager.from(context);
			boolean mDataFlowStatus = false;
			if(mTelephonyManager != null){
				mDataFlowStatus = mTelephonyManager.getDataEnabled();
			}
			statusArr[3] = mDataFlowStatus?opend:closed;
			// Wifi
			statusArr[4] = opend;
			// Wifi HotPoint
			statusArr[5] = closed;
			// Bluetooth
			statusArr[6] = closed;
			// GPS
			statusArr[7] = closed;
			// chuganfankui
			statusArr[8] = closed;
			// shupingsuoding
			boolean isRotationLocked = RotationPolicy.isRotationLocked(context);
			statusArr[9] = isRotationLocked? opend : closed;
		}
		return statusArr;
	}

	public static int getModeType(Context context){
		String mode = SystemProperties.get(PROPERTY_POWER_MODE);
		int modeType = 1;
		if(mode == null || mode.length() == 0){
			modeType = 1;
		}else {
			modeType = Integer.parseInt(mode);
			int mSaveModeType = getCpuModeType(context);
			if(modeType == 1 && mSaveModeType == 2){
				modeType = 2;
			}
		}
		return modeType;
	} 
}
