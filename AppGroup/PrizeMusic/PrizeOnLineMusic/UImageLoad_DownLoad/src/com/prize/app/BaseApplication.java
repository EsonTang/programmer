package com.prize.app;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
//import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.database.PrizeDatabaseHelper;
import com.prize.app.download.DownloadTaskMgr;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;
import com.prize.custmerxutils.XExtends;
import com.xiami.sdk.utils.UTUtil;

public class BaseApplication extends Application {
	// 鐠佹澘缍嶈ぐ鎾冲 Context
	public static boolean SWITCH_UNSUPPORT = true;
	public static Context curContext;

	public static Handler handler = new Handler();
	
	private static String NEW_SIGN="new";
	private static String OLD_SIGN="old";
	private static String NO_SIGN="no";
	
	/**新、旧、无签名判断  liukun 2017.7.10*/
	//0：无签名；1：旧签名；2-∞：新签名
//	private static boolean isOldSign= false;	
	private static int apkSign=2;
	/**新、旧、无签名判断  liukun 2017.7.10*/
	public static boolean isDebug= true;

	public static String getSign(){
		
		if (apkSign==0) 
			return NO_SIGN;			
		else if(apkSign==1)
			return OLD_SIGN;
		else
			return NEW_SIGN;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		curContext = this;
//		UTUtil.init(this);
//		initBaseApp();
//		XExtends.Ext.init(this);
//		initImageLoader(this);
		
		
		
		
//		String value = SystemProperties.get(Constants.SWITCH_KEY);
		ContentResolver resolver = curContext.getContentResolver();
		String value = Settings.System.getString(resolver, Constants.SWITCH_KEY);
		
		if (!TextUtils.isEmpty(value)
				&& value.equalsIgnoreCase(Constants.SWITCH_VALUE_ON)) {
			SWITCH_UNSUPPORT = true;
			JLog.i("hu", "SWITCH_UNSUPPORT=="+value);
		}
	}

	public void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024)
				// 50 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				// .writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);

	}

	/**
	 * 閸掓繂顫愰崠鏍у弿闁劍澧嶉棁锟介惃鍕殶閹诡喖绨�
	 * 
	 * @return void
	 */
	public static void initBaseApp() {
		ClientInfo.initClientInfo();
		PrizeDatabaseHelper.initPrizeSQLiteDatabase();
		DownloadTaskMgr.getInstance();
	}

	public static boolean isDownloadWIFIOnly() {
		String wifiSettingString = DataStoreUtils
				.readLocalInfo(DataStoreUtils.SWITCH_WIFI_DOWNLOAD);
		if (TextUtils.isEmpty(wifiSettingString)) {
			return true;
		}
		if (null != wifiSettingString) {
			return wifiSettingString
					.equals(DataStoreUtils.CHECK_ON);
		}
		return false;
	}

	// /** 閼惧嘲褰囨稉鑽ゅ殠缁嬪┉D */
	// public static int getMainThreadId() {
	// return mMainThreadId;
	// }
	//
	// /** 閼惧嘲褰囨稉鑽ゅ殠缁嬪娈慼andler */
	// public static Handler getMainThreadHandler() {
	// return mMainThreadHandler;
	// }
	//
	// /** 閼惧嘲褰囨稉鑽ゅ殠缁嬪娈憀ooper */
	// public static Looper getMainThreadLooper() {
	// return mMainLooper;
	// }

}