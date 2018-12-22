/************************************************************************
* 版权所有 (C)2015, 深圳市铂睿智恒科技有限公司
* 文件名称：PowerExtendModeDataManager.java
* 文件标识：应急省电模式数据备份管理类
* 内容摘要：
* 其它说明：
* 当前版本：1.0
* 作    者：wangxianzhen
* 完成日期: 2015-04-14
************************************************************************/
package com.android.server.power;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.service.wallpaper.WallpaperService;

import com.android.internal.widget.LockPatternUtils;
//import com.mediatek.audioprofile.AudioProfileManager;
import com.mediatek.common.prizeoption.PrizeOption;

public class PowerExtendModeDataManager {
	private static final String TAG = "PowerExtendModeDataManager";

	private final static String REBOOT_POWER_EXTEND_PROPERTY = "persist.sys.power_extend_mode";
	private static final int def_pw_ext_mode_scr_brightness = 15;
	private static final int def_pw_ext_mode_scr_off_timeout = 6*1000;

    //静态壁纸相关参数
    //system wallpaper file name
    private static final String WALLPAPER_FILE_NAME = "wallpaper";
	//backup wallpaper file name
	private static final String WALLPAPER_POWEREXTEND_MODE_FILE_NAME = "wallpaper_powerextend_mode";
    public static File getSystemWallpaperDirectory() {
        int userId = 0;
        return new File(new File(Environment.getSystemSecureDirectory(), "users"),  Integer.toString(userId));
    }
    private static final File sDataDir = Environment.getDataDirectory();
	//动态壁纸相关参数
	private static final String IS_LIVE_WALLPAPER = "kk_pw_ext_is_live_wallpaper";
	private static final String LIVE_WALLPAPER_PKG_NAME = "kk_pw_ext_live_wallpaper_pkg_name";
	private static final String LIVE_WALLPAPER_SERVICE_NAME = "kk_pw_ext_live_wallpaper_service_name";
    
	//backup Appwidgets file name
	private static final String APPWIDGETS_POWEREXTEND_MODE_FILE_NAME = "appwidgets_powerextend_mode.xml";
	private static final String APPWIDGETS_FILE_NAME = "appwidgets.xml";
	
	private static final String MMS_APP_PACKAGE = "com.android.mms";
	public static final String GOOGLE_TALK_APP_PACKAGE = "com.google.android.talk";

    /**
    * set flag for super saver mode
    */
    static void setSuperSaverMode(boolean enable) {
        if (enable){
            SystemProperties.set(REBOOT_POWER_EXTEND_PROPERTY, "true");
        }else{
            SystemProperties.set(REBOOT_POWER_EXTEND_PROPERTY, "false");
        }
    }

    /**
    * Enter into power extends mode before rebooting,it would backup
    * the state for data connection.
    * @param context
    */
	static void storeDataConnectionState(Context context){
		String lLocation;
		ContentResolver lResolver = context.getContentResolver();
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final WifiManager mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		//Nfc off
//		if (true == FeatureOption.MTK_NFC_SUPPORT){
//			NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
//			NfcAdapter lNfcAdpt = manager.getDefaultAdapter();
//			if (lNfcAdpt!=null && lNfcAdpt.isEnabled()){
//				lNfcAdpt.disable();
//				Settings.System.putInt(lResolver, Settings.System.POWER_EXTEND_BK_NFC_ON, 1);
//			}else{
//				Settings.System.putInt(lResolver, Settings.System.POWER_EXTEND_BK_NFC_ON, 0);
//			}
//		}
		//Gprs off
		/*if (cm.getMobileDataEnabled()){
			//cm.setMobileDataEnabled(false);
			Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_GPRS_ON, 1, UserHandle.USER_CURRENT);
			//GPRS connection off
			//Settings.System.putInt(lResolver, Settings.System.GPRS_CONNECTION_SETTING, 0);
			int soltId = Settings.System.getIntForUser(lResolver,
					Settings.System.GPRS_CONNECTION_SETTING, Settings.System.GPRS_CONNECTION_SETTING_DEFAULT, UserHandle.USER_CURRENT);
			Long simID = Settings.System.getLongForUser(lResolver,
					Settings.System.GPRS_CONNECTION_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET, UserHandle.USER_CURRENT);
			Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_GPRS_CONNECTION_SETTING, soltId, UserHandle.USER_CURRENT);
			Settings.System.putLongForUser(lResolver, Settings.System.POWER_EXTEND_BK_GPRS_CONNECTION_SIM_SETTING, simID, UserHandle.USER_CURRENT);
		}else{
			Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_GPRS_ON, 0, UserHandle.USER_CURRENT);
		}*/
		//WiFi off
        if (mWifiManager != null) {
            if (mWifiManager.getWifiState()== WifiManager.WIFI_STATE_ENABLING || mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... args) {
                        mWifiManager.setWifiEnabled(false);
                        return null;
                    }
                }.execute();
                Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_WIFI_ON, 1, UserHandle.USER_CURRENT);
            }else{
                Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_WIFI_ON, 0, UserHandle.USER_CURRENT);
            }
        }
		//Bluetooth off
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON || bluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... args) {
                        bluetoothAdapter.disable();
                        return null;
                    }
                }.execute();
                Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_BLUETOOTH, 1, UserHandle.USER_CURRENT);
            }else{
			    Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_BLUETOOTH, 0, UserHandle.USER_CURRENT);
            }
        }
		//DATA SYNC status on
		/*if (ContentResolver.getMasterSyncAutomatically()){
            ContentResolver.setMasterSyncAutomatically(false);
			Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_SYNC_STATUS, 1, UserHandle.USER_CURRENT);
		}else{
			Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_SYNC_STATUS, 0, UserHandle.USER_CURRENT);
		}*/
		//GPS off
		lLocation = Settings.Secure.getStringForUser(lResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED, UserHandle.USER_CURRENT);
		if (lLocation != null){
			Settings.System.putStringForUser(lResolver, Settings.System.POWER_EXTEND_BK_LOCATION_PROVIDERS_ALLOWED, lLocation, UserHandle.USER_CURRENT);
		}
		Settings.Secure.putStringForUser(lResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED, "", UserHandle.USER_CURRENT);
		//widgets enable
		/*LockPatternUtils lLockPatternUtils = new LockPatternUtils(context); 
		if (lLockPatternUtils.getWidgetsEnabled()){
			lLockPatternUtils.setWidgetsEnabled(false);
			Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_WIDGETS_ENABLE, 1, UserHandle.USER_CURRENT);
		}else{
			Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_WIDGETS_ENABLE, 0, UserHandle.USER_CURRENT);
		}

		String smsDefaultApp = Settings.Secure.getStringForUser(lResolver, Settings.Secure.SMS_DEFAULT_APPLICATION, UserHandle.USER_CURRENT);
		if(smsDefaultApp != null){
			if (GOOGLE_TALK_APP_PACKAGE.equals(smsDefaultApp)) {
		        Settings.Secure.putStringForUser(lResolver, Settings.Secure.POWER_EXTEND_BK_SMS_DEFAULT_APPLICATION, GOOGLE_TALK_APP_PACKAGE, UserHandle.USER_CURRENT);
			}else{
				Settings.Secure.putStringForUser(lResolver, Settings.Secure.POWER_EXTEND_BK_SMS_DEFAULT_APPLICATION, MMS_APP_PACKAGE, UserHandle.USER_CURRENT);
			}
		}*/
		//ReadingMode off
        boolean prizeReadingMode = Settings.System.getIntForUser(lResolver, Settings.System.PRIZE_READING_MODE, 0, UserHandle.USER_CURRENT) == 1;
        if (prizeReadingMode) {
            Settings.System.putIntForUser(lResolver, Settings.System.PRIZE_READING_MODE, 0, UserHandle.USER_CURRENT);
        }
        //GameMode off
        boolean prizeGameMode = Settings.System.getIntForUser(lResolver, Settings.System.PRIZE_GAME_MODE, 0, UserHandle.USER_CURRENT) == 1;
        if (prizeGameMode) {
            Settings.System.putIntForUser(lResolver, Settings.System.PRIZE_GAME_MODE, 0, UserHandle.USER_CURRENT);
        }
        
        //BarrageWindow off
        boolean prizeBarrageWindow = Settings.System.getIntForUser(lResolver, Settings.System.PRIZE_BARRAGE_WINDOW, 0, UserHandle.USER_CURRENT) == 1;
        if (prizeBarrageWindow) {
            Settings.System.putIntForUser(lResolver, Settings.System.PRIZE_BARRAGE_WINDOW, 0, UserHandle.USER_CURRENT);
            Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_PRIZE_BARRAGE_ENABLE, 1, UserHandle.USER_CURRENT);

            Intent intent = new Intent();
            intent.setAction("android.intent.action.PRIZE_BARRAGE_WINDOW");
            intent.setPackage("com.prize.barragewindow");
            context.sendBroadcast(intent);

        } else {
            Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_PRIZE_BARRAGE_ENABLE, 0, UserHandle.USER_CURRENT);
        }
	}

    /**
	 * Enter into power extends mode before rebooting,it would restore 
	 * the state for data connection.
	 * @param context
	 */
	static void restoreDataConnectionState(Context context){
		ConnectivityManager cm;
		ContentResolver lResolver = context.getContentResolver();
		final WifiManager mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		//Nfc on		
		/*if (true == FeatureOption.MTK_NFC_SUPPORT){
			if (1 == Settings.System.getInt(lResolver, Settings.System.POWER_EXTEND_BK_NFC_ON, 0)){
				NfcManager manager = (NfcManager)context.getSystemService(Context.NFC_SERVICE); 
				NfcAdapter lNfcAdpt = manager.getDefaultAdapter();
				if (lNfcAdpt != null){
					lNfcAdpt.enable();
				}
			}
		}*/
		//Gprs on
		/*if (1 == Settings.System.getIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_GPRS_ON, 0, UserHandle.USER_CURRENT)){
			cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			//cm.setMobileDataEnabled(true);
			int soltId = Settings.System.getIntForUser(lResolver,
					Settings.System.POWER_EXTEND_BK_GPRS_CONNECTION_SETTING, Settings.System.GPRS_CONNECTION_SETTING_DEFAULT, UserHandle.USER_CURRENT);
			Long simID = Settings.System.getLongForUser(lResolver,
					Settings.System.GPRS_CONNECTION_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET, UserHandle.USER_CURRENT);
			Settings.System.putIntForUser(lResolver, Settings.System.GPRS_CONNECTION_SETTING, soltId, UserHandle.USER_CURRENT);
			Settings.System.putLongForUser(lResolver, Settings.System.GPRS_CONNECTION_SIM_SETTING, simID, UserHandle.USER_CURRENT);
			//cm.setMobileDataEnabledGemini(soltId-1);
		}*/
		//WiFi on
		if (1 == Settings.System.getIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_WIFI_ON, 0, UserHandle.USER_CURRENT)){
        	if (mWifiManager != null) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... args) {
                        mWifiManager.setWifiEnabled(true);
                        return null;
                    }
                }.execute();
            }
		}
		//Bluetooth on
		if (1 == Settings.System.getIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_BLUETOOTH, 0, UserHandle.USER_CURRENT)){
        	if (bluetoothAdapter != null) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... args) {
                        bluetoothAdapter.enable();
                        return null;
                    }
                }.execute();
            }
		}
		//DATA SYNC status on
		/*if (1 == Settings.System.getIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_SYNC_STATUS, 0, UserHandle.USER_CURRENT)){
			ContentResolver.setMasterSyncAutomatically(true);
		}*/
		//GPS on
		String lLocation = Settings.System.getStringForUser(lResolver, Settings.System.POWER_EXTEND_BK_LOCATION_PROVIDERS_ALLOWED, UserHandle.USER_CURRENT);
		if (null != lLocation){
			Settings.Secure.putStringForUser(lResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED, lLocation, UserHandle.USER_CURRENT);
		}
		//widgets enable
		/*if (1 == Settings.System.getIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_WIDGETS_ENABLE, 0, UserHandle.USER_CURRENT)){
			LockPatternUtils lLockPatternUtils = new LockPatternUtils(context); 
			lLockPatternUtils.setWidgetsEnabled(true);			
		}
		String bkSmsDefaultApp = Settings.Secure.getStringForUser(lResolver, Settings.Secure.POWER_EXTEND_BK_SMS_DEFAULT_APPLICATION, UserHandle.USER_CURRENT);
		if(bkSmsDefaultApp != null){
			Settings.Secure.putStringForUser(lResolver, Settings.Secure.SMS_DEFAULT_APPLICATION, bkSmsDefaultApp, UserHandle.USER_CURRENT);
		}*/

        //BarrageWindow on
        if (Settings.System.getIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_PRIZE_BARRAGE_ENABLE, 0, UserHandle.USER_CURRENT) == 1) {
            Settings.System.putIntForUser(lResolver, Settings.System.PRIZE_BARRAGE_WINDOW, 1, UserHandle.USER_CURRENT);

            Intent intent = new Intent();
            intent.setAction("android.intent.action.PRIZE_BARRAGE_WINDOW");
            intent.setPackage("com.prize.barragewindow");
            context.sendBroadcast(intent);
        }
	}
	
	/**
	 * Enter into power extends mode before rebooting,it would backup the screen state.
	 * @param lResolver
	 */
	static void storeScreenState(ContentResolver lResolver){
		int lVal;
		
		//Screen automatic brightness mode		
		lVal = Settings.System.getIntForUser(lResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0, UserHandle.USER_CURRENT);
		Settings.System.putIntForUser(lResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0, UserHandle.USER_CURRENT);
		Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_SCREEN_BRIGHTNESS_MODE, lVal, UserHandle.USER_CURRENT);
		Log.i(TAG,"storeScreenState SCREEN_BRIGHTNESS_MODE " + lVal);
		//Screen brightness
		lVal = Settings.System.getIntForUser(lResolver, Settings.System.SCREEN_BRIGHTNESS, def_pw_ext_mode_scr_brightness, UserHandle.USER_CURRENT);
		Settings.System.putIntForUser(lResolver, Settings.System.SCREEN_BRIGHTNESS, def_pw_ext_mode_scr_brightness, UserHandle.USER_CURRENT);
		Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_SCREEN_BRIGHTNESS, lVal, UserHandle.USER_CURRENT);
		Log.i(TAG,"storeScreenState SCREEN_BRIGHTNESS " + lVal);
		//Screen off timeout 
		lVal = Settings.System.getIntForUser(lResolver, Settings.System.SCREEN_OFF_TIMEOUT, def_pw_ext_mode_scr_off_timeout, UserHandle.USER_CURRENT);
                Settings.System.putIntForUser(lResolver, Settings.System.SCREEN_OFF_TIMEOUT, def_pw_ext_mode_scr_off_timeout,UserHandle.USER_CURRENT);
		Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_SCREEN_OFF_TIMEOUT, lVal, UserHandle.USER_CURRENT);
		Log.i(TAG,"storeScreenState SCREEN_OFF_TIMEOUT " + lVal);
		//Accelerometer rotation off
		lVal = Settings.System.getIntForUser(lResolver, Settings.System.ACCELEROMETER_ROTATION, 0, UserHandle.USER_CURRENT);
		Settings.System.putIntForUser(lResolver, Settings.System.ACCELEROMETER_ROTATION, 0, UserHandle.USER_CURRENT);
		Settings.System.putIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_ACCELEROMETER_ROTATION, lVal, UserHandle.USER_CURRENT);
		Log.i(TAG,"storeScreenState ACCELEROMETER_ROTATION " + lVal);
	}
	
	/**
	 * Enter into power extends mode before rebooting,it would restore the screen state.
	 * @param lResolver
	 */
	static void restoreScreenState(ContentResolver lResolver){
		int lVal;
		
		//Screen automatic brightness mode		
		lVal = Settings.System.getIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_SCREEN_BRIGHTNESS_MODE, 0, UserHandle.USER_CURRENT);
		Log.i(TAG,"restoreScreenState SCREEN_BRIGHTNESS_MODE " + lVal);
		Settings.System.putIntForUser(lResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, lVal, UserHandle.USER_CURRENT);
		//Screen brightness
		lVal = Settings.System.getIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_SCREEN_BRIGHTNESS, def_pw_ext_mode_scr_brightness, UserHandle.USER_CURRENT);
		Log.i(TAG,"restoreScreenState SCREEN_BRIGHTNESS " + lVal);
		Settings.System.putIntForUser(lResolver, Settings.System.SCREEN_BRIGHTNESS, lVal, UserHandle.USER_CURRENT);
		//Screen off timeout
		lVal = Settings.System.getIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_SCREEN_OFF_TIMEOUT, def_pw_ext_mode_scr_off_timeout, UserHandle.USER_CURRENT);
		Log.i(TAG,"restoreScreenState SCREEN_OFF_TIMEOUT " + lVal);
		Settings.System.putIntForUser(lResolver, Settings.System.SCREEN_OFF_TIMEOUT, lVal, UserHandle.USER_CURRENT);
		//Accelerometer rotation
		lVal = Settings.System.getIntForUser(lResolver, Settings.System.POWER_EXTEND_BK_ACCELEROMETER_ROTATION, 0, UserHandle.USER_CURRENT);
		Log.i(TAG,"restoreScreenState ACCELEROMETER_ROTATION " + lVal);
		Settings.System.putIntForUser(lResolver, Settings.System.ACCELEROMETER_ROTATION, lVal, UserHandle.USER_CURRENT);
	}
	/**
	 * 进入极限省电模式后还原情景模式状态信息
	 * @param lResolver
	 */
	static void storeProfile(Context lCtx, ContentResolver lResolver){
		//AudioProfileManager lAudProfileManager = (AudioProfileManager)lCtx.getSystemService(Context.AUDIOPROFILE_SERVICE);
		//feedback vibrate
		if (1== Settings.System.getInt(lResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 0)){
			Settings.System.putInt(lResolver, Settings.System.POWER_EXTEND_BK_HAPTIC_FEEDBACK_ENABLED, 1);
			//lAudProfileManager.setHapticFeedbackEnabled(lAudProfileManager.getActiveProfileKey(), false);
		}else{
			Settings.System.putInt(lResolver, Settings.System.POWER_EXTEND_BK_HAPTIC_FEEDBACK_ENABLED, 0);
		}
	}
	/**
	 * 退出极限省电模式后还原情景模式状态信息
	 * @param lResolver
	 */
	static void restoreProfile(Context lCtx, ContentResolver lResolver){
		//AudioProfileManager lAudProfileManager = (AudioProfileManager)lCtx.getSystemService(Context.AUDIOPROFILE_SERVICE);
		
		if (1 == Settings.System.getInt(lResolver, Settings.System.POWER_EXTEND_BK_HAPTIC_FEEDBACK_ENABLED, 0)){
			//lAudProfileManager.setHapticFeedbackEnabled(lAudProfileManager.getActiveProfileKey(), true);
			Log.w(
				TAG
				, "restoreProfile(): HAPTIC_FEEDBACK_ENABLED= " + Settings.System.getInt(lResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 0)
			);
		}
	}
	
	/**
	 * Enter into power extends mode before rebooting, it would backup wallpaper
	 * information.
	 */
	static void storeWallpaperInfo(Context context) {
		ContentResolver lResolver = context.getContentResolver();
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
		WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
		//如果是动态壁纸
		if (wallpaperInfo != null) {
			Log.d(TAG, "this is live wallpaper!!!");
			String packageName = wallpaperInfo.getPackageName();
			String serviceName = wallpaperInfo.getServiceName();
			Settings.System.putString(lResolver, IS_LIVE_WALLPAPER, "true");
			Settings.System.putString(lResolver, LIVE_WALLPAPER_PKG_NAME, packageName);
			Settings.System.putString(lResolver, LIVE_WALLPAPER_SERVICE_NAME, serviceName);
			//发现packagename是对的，但是cls不对
			Log.d(TAG, "package = " + packageName + serviceName + " wallpaperInfo = " + wallpaperInfo);
			
			PackageManager packageManager = context.getPackageManager();
			List<ResolveInfo> list = packageManager.queryIntentServices(new Intent(WallpaperService.SERVICE_INTERFACE), PackageManager.GET_META_DATA);
			int len = list.size();
			ResolveInfo inf;
			for (int i = 0; i < len; i++) {
				inf = list.get(i);
				if ((inf.serviceInfo != null)
						&& (inf.serviceInfo.packageName.equals(packageName))) {
					Log.d(TAG, "live wallpaper is " + inf.serviceInfo.name);
				}
			}
			return;
		}
		
		//如果是静态壁纸
		Settings.System.putString(lResolver, IS_LIVE_WALLPAPER, "false");
		InputStream defIs = null;
		File sysDir = getSystemWallpaperDirectory();	
		if(!sysDir.exists()){
			sysDir.mkdir();
			FileUtils.setPermissions(sysDir.getPath(), FileUtils.S_IRWXU
					| FileUtils.S_IRWXG | FileUtils.S_IXOTH, -1, -1);
		}

		//create the system wallpaper file
		File sysWpfile = new File(sysDir, WALLPAPER_FILE_NAME);
		
		//create the backup wallpaper file
		File bkWpfile = new File(sysDir, WALLPAPER_POWEREXTEND_MODE_FILE_NAME);
		if(!bkWpfile.exists()){
			Log.i(TAG, "storeWallpaperInfo:->the backup wallpaper file is not exists!!!");			
			try {
				bkWpfile.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, "storeWallpaperInfo:->Can't create the backup wallpaper file!!!");
			}
			FileUtils.setPermissions(bkWpfile.getPath(), FileUtils.S_IRWXU
					| FileUtils.S_IRWXG | FileUtils.S_IXOTH, -1, -1);
		}

		if (sysWpfile.exists()){
			//Backup wallpaper is starting.
			FileUtils.copyFile(sysWpfile, bkWpfile);
		}else{
			//Backup wallpaper is default.
			defIs = context.getResources().openRawResource(com.android.internal.R.drawable.default_wallpaper);
			FileUtils.copyToFile(defIs, bkWpfile);
		}
	}
	
	/**
	 * Quit power extends mode before rebooting,
	 * it would restore wallpaper information.
	 */
	static void restoreWallpaperInfo(Context context){
		ContentResolver lResolver = context.getContentResolver();
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
		//如果当前壁纸就是动态壁纸，不需要恢复了
		WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
		if (wallpaperInfo != null) {		    
			return;
		}
		
		String isPreLiveWallpaper = Settings.System.getString(lResolver, IS_LIVE_WALLPAPER);
		if ("true".equals(isPreLiveWallpaper)) {
			String pkgName = Settings.System.getString(lResolver, LIVE_WALLPAPER_PKG_NAME);
			String serviceName = Settings.System.getString(lResolver, LIVE_WALLPAPER_SERVICE_NAME);
			Log.d(TAG, "[PowerExtendModeDataManager]restoreWallpaperInfo:pkgName = " + pkgName + ", serviceName = " + serviceName);
			try {
				wallpaperManager.getIWallpaperManager().setWallpaperComponent(
						new ComponentName(pkgName, serviceName));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			wallpaperManager.setWallpaperOffsetSteps(0.5f, 0.0f);
			return;
		}
		
		//如果切换到应急省电模式之前是静态壁纸，那么恢复静态壁纸
		InputStream defIs = null;
		File sysDir = getSystemWallpaperDirectory();
		File sysWpfile = new File(sysDir, WALLPAPER_FILE_NAME);		
		File bkWpfile = new File(sysDir, WALLPAPER_POWEREXTEND_MODE_FILE_NAME);	
		
		if(!sysWpfile.exists() || !bkWpfile.exists() ){			
			try {
				sysWpfile.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, "restoreWallpaperInfo: Can't create the system wallpaper file!!!");
			}
			FileUtils.setPermissions(sysWpfile.getPath(), FileUtils.S_IRWXU
					| FileUtils.S_IRWXG | FileUtils.S_IXOTH, -1, -1);
			defIs = context.getResources().openRawResource(com.android.internal.R.drawable.default_wallpaper);
			FileUtils.copyToFile(defIs, sysWpfile);
			return;
		}else{
			FileUtils.setPermissions(sysWpfile.getPath(), FileUtils.S_IRWXU
					| FileUtils.S_IRWXG | FileUtils.S_IXOTH, -1, -1);
			FileUtils.setPermissions(bkWpfile.getPath(), FileUtils.S_IRWXU
					| FileUtils.S_IRWXG | FileUtils.S_IXOTH, -1, -1);
		}
		
		//restore wallpaper
		FileUtils.copyFile(bkWpfile, sysWpfile);
 	}
	
	/**
	 * Enter into extends mode before rebooting,
	 * it would backup data/system/users/0/appwidgets.xml information.
	 */
	static void storeAppWidgetsXmlInfo(Context context) {
		final File sysDir = getSystemWallpaperDirectory();	
		final File sysAppWidgetsXmlFile = new File(sysDir, APPWIDGETS_FILE_NAME);
		final File bkAppWidgetsXmlFile = new File(sysDir, APPWIDGETS_POWEREXTEND_MODE_FILE_NAME);
	    
        if(!bkAppWidgetsXmlFile.exists()){
			try {
	        	//创建备份文件
				bkAppWidgetsXmlFile.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, "storePackageXmlInfo: Can't create appwidgets_powerextend_mode.xml!!!");
				return;
			}
        }
		FileUtils.setPermissions(sysDir.getPath(), FileUtils.S_IRWXU
				| FileUtils.S_IRWXG | FileUtils.S_IXOTH, -1, -1);
        FileUtils.copyFile(sysAppWidgetsXmlFile, bkAppWidgetsXmlFile);
		Log.w(TAG, "PowerExtendModeDebug:-->>PowerExtendModeDataManager:storeAppWidgetsXmlInfo is run.");
	}
	
	/**
	 * Quit power extends mode before rebooting,
	 * it would restore data/system/users/0/appwidgets.xml information.
	 */
	static void restoreAppWidgetsXmlInfo(Context context){
		final File sysDir = getSystemWallpaperDirectory();
		final File sysAppWidgetsXmlFile = new File(sysDir, APPWIDGETS_FILE_NAME);
		final File bkAppWidgetsXmlFile = new File(sysDir, APPWIDGETS_POWEREXTEND_MODE_FILE_NAME);
		
	    if(bkAppWidgetsXmlFile.exists() && sysAppWidgetsXmlFile.exists()){
			FileUtils.setPermissions(sysDir.getPath(), FileUtils.S_IRWXU
					| FileUtils.S_IRWXG | FileUtils.S_IXOTH, -1, -1);	    	
			FileUtils.copyFile(bkAppWidgetsXmlFile, sysAppWidgetsXmlFile);
			Log.w(TAG, "PowerExtendModeDebug:-->>PowerExtendModeDataManager:restoreAppWidgetsXmlInfo is run.");
        }else{
	    	Log.e(TAG, "restoreAppWidgetsXmlInfo: file is not exist!!!");
	    }	    
 	}
}
