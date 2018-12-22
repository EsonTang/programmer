/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.common.util.LogUtil;
import org.xutils.db.DbManagerImpl;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;
import org.xutils.image.ImageOptions;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.lq.DefaultConfig;
import com.android.launcher3.lq.FindDefaultResoures;
import com.baidu.location.Address;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
//import com.inveno.se.NContext;
import com.lqsoft.LqServiceUpdater.LqService;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.mediatek.launcher3.ext.LauncherLog;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.prize.cloud.bean.Person;
import com.prize.left.page.AppConfig;
import com.prize.left.page.bean.table.AccountTable;
import com.prize.left.page.bean.table.BigCardType;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.HotBoxTable;
import com.prize.left.page.bean.table.HotTipsTable;
import com.prize.left.page.bean.table.HotWordTable;
import com.prize.left.page.bean.table.NetNaviTable;
import com.prize.left.page.bean.table.NormalAddrTable;
import com.prize.left.page.bean.table.PersonTable;
import com.prize.left.page.bean.table.PushTable;
import com.prize.left.page.bean.table.SelCardType;
import com.prize.left.page.bean.table.SubCardType;
import com.prize.left.page.model.IOnLocChange;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.PreferencesUtils;

/**
 * @author Administrator
 *
 */
public class LauncherApplication extends Application {
    private static final String TAG = "LauncherApplication";

    /// M: flag for starting Launcher from application
    private boolean mTotallyStart = false;
    private boolean isRecordLog = false;  //是否打开记录错误日记
    /// M: added for unread feature.
    private MTKUnreadLoader mUnreadLoader;
    
//    public static Context curText;

	private LinkedHashMap<String, String> mDefault_config;
	public LinkedHashMap<ComponentName,String> mDefault_language;
    public LinkedHashMap<String, String> getDefault_config() {
		return mDefault_config;
	}


	/// M: flag for multi window support    
    //public static final boolean FLOAT_WINDOW_SUPPORT = FeatureOption.MTK_MULTI_WINDOW_SUPPORT;
    @Override
    public void onCreate() {
    	if(isRecordLog){
    		  CrashHandler catchHandler = CrashHandler.getInstance();
    	      catchHandler.init(getApplicationContext());
    	}
        super.onCreate();
//        
//        curText = this;
        
        appInstance = this;

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "LauncherApplication onCreate");
        }

        LauncherAppState.setApplicationContext(this);
//        initImageLoader(this);
        LauncherAppState.getInstance().setLauncehrApplication(this);
        
        /**M: register unread broadcast.@{**/
        if (getResources().getBoolean(R.bool.config_unreadSupport)) {
            mUnreadLoader = new MTKUnreadLoader(getApplicationContext());
            // Register unread change broadcast.
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_UNREAD_CHANGED);
            registerReceiver(mUnreadLoader, filter);
        }
        /**@}**/


		SharedPreferences sps = getSharedPreferences(
				"load_default_res", Context.MODE_PRIVATE);
		finddefaultConfig();
        //begin add by ouyangjin for lqtheme
		if (LqShredPreferences.isLqtheme(this)) {
//			Lqservice.getInstance().linkedSystemLibFiles();
        LqService.getInstance().initSync(this);
        LqService.getInstance().setThemeOverlayIcon(this.getPackageName());
        LqShredPreferences.init(this);
		boolean isloaded = sps.getBoolean("load_default_theme_wall", false);
        	String lqThmePath =LqShredPreferences.getLqThemePath();
        	if(!lqThmePath.equals("")){
        		try {
            		boolean result = LqService.getInstance().notifyLqThemeChanged(lqThmePath);
            		if(!isloaded) {
            		 LqService.getInstance().applyWallpaper(true);

//            			LqThemeParser.applyWallpaper(this, lqThmePath);
            		}
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        }

		sps.edit().putBoolean("load_default_theme_wall", true).commit();
        //end add by ouyangjin for lqtheme
        
        mCtx = getBaseContext();
		initXUtils();
        if(Utilities.supportleftScreen()) {
//        NContext.getInstance().initConfig(mCtx);
        

		initDb();
		// 注释掉下面这行, 就是去掉定位服务 20160216
		initLoc();
		
		registerObserver();
        }
    }
    
    
    /**
     * 设置默认配置
     */
    public void finddefaultConfig() {

		File configFile = new File(DefaultConfig.test_config
				+ "default_config.xml");
		
		LogUtils.i("zhouerlong", "configFile:::"+configFile.getAbsolutePath()+"  :configFile.exists(): " +configFile.exists());
		if (configFile.exists()) {
			mDefault_config = DefaultConfig
					.findCustomConfig(DefaultConfig.test_config
							+ "default_config.xml");
			com.android.gallery3d.util.LogUtils.i("zhouerlong",
					"test_config:/storage/sdcard0/config/default_config/");
		}else {
			mDefault_config = DefaultConfig
					.findCustomConfig(DefaultConfig.default_config
							+ "default_config.xml");
			com.android.gallery3d.util.LogUtils.i("zhouerlong",
					"default_config:/system/media/config/default_config/");
		}
		if(mDefault_config !=null) {
			DefaultConfig.config_theme_wallpaper_path = mDefault_config
					.get(DefaultConfig.default_bulit_in_path);

			if (!FileUtils
					.isexists(DefaultConfig.config_theme_wallpaper_path)) {
				DefaultConfig.config_theme_wallpaper_path = DefaultConfig.default_config_theme_wallpaper_path;
			}
			String themeName = mDefault_config
					.get(DefaultConfig.default_bulit_in_theme_name);
			LqShredPreferences.DEFAULT_THME = DefaultConfig.config_theme_wallpaper_path
					+ "theme/"
					+ themeName
					+ "/"
					+ mDefault_config
							.get(DefaultConfig.default_bulit_in_theme_name)
					+ ".jar";
			String koobee=mDefault_config.get(DefaultConfig.is_koobee);
			boolean isKoobee =Boolean.valueOf(koobee);
			String default_workspace = isKoobee?mDefault_config
					.get(DefaultConfig.default_workspace_name_koobee):mDefault_config
					.get(DefaultConfig.default_workspace_name_koosai);
					
					

					String default_overlay_icon = isKoobee?mDefault_config
							.get(DefaultConfig.overlay_icon_koobee):mDefault_config
							.get(DefaultConfig.overlay_icon_koosai);
			DefaultConfig.default_workspace_path = DefaultConfig.config_theme_wallpaper_path+"default_config/"
			+default_workspace;
			

			DefaultConfig.default_overlay_icon_path = DefaultConfig.config_theme_wallpaper_path+"default_config/"
			+default_overlay_icon;
			
			
			mDefault_language = DefaultConfig
					.findDefaultXml(DefaultConfig.default_workspace_path);
			

			DefaultConfig
					.findOverIcons(DefaultConfig.default_overlay_icon_path);
			
			
		}else {
			if (!FileUtils
					.isexists(DefaultConfig.config_theme_wallpaper_path)) {
				DefaultConfig.config_theme_wallpaper_path = DefaultConfig.default_config_theme_wallpaper_path;
			}
			LqShredPreferences.DEFAULT_THME = DefaultConfig.config_theme_wallpaper_path+"theme/jinshu/jinshu.jar";
			com.android.gallery3d.util.LogUtils.i("zhouerlong",
					"default config file not exits  "
							+ DefaultConfig.default_config + "  defualt theme:"
							+ LqShredPreferences.DEFAULT_THME);
		}
		FindDefaultResoures.DEFALUT_THEME_PATH = DefaultConfig.config_theme_wallpaper_path
				+ "theme/";
		FindDefaultResoures.DEFALUT_WALLPAPER_PATH = DefaultConfig.config_theme_wallpaper_path
				+ "wallpaper/";
    }
    
    private void initImageLoader(Context context) {
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
    @Override
    public void onTerminate() {
        super.onTerminate();
        /**M: added for unread feature, unregister unread receiver.@{**/
        if (getResources().getBoolean(R.bool.config_unreadSupport)) {
            unregisterReceiver(mUnreadLoader);
        }
        /**@}**/
        LauncherAppState.getInstance().onTerminate();
    }

    /// M: LauncherApplication start flag @{
    public void setTotalStartFlag() {
        mTotallyStart = true;
    }

    public void resetTotalStartFlag() {
        mTotallyStart = false;
    }

    public boolean isTotalStart() {
        return mTotallyStart;
    }
    /// M: }@
    /**M: Added for unread message feature.@{**/
    /**
     * M: Get unread loader, added for unread feature.
     */
    public MTKUnreadLoader getUnreadLoader() {
        return mUnreadLoader;
    }
    /**@}**/
    
    /**@prize left page start fanjunchen {*/
    /** 数据库配置信息类 */
    
    private final int DB_VERSION = 2;
    
	private DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
			.setDbName("leftPage.db")
			// .setDbDir(new File("/sdcard"))
			.setDbVersion(DB_VERSION)
			.setDbUpgradeListener(new DbManager.DbUpgradeListener() {
				@Override
				public void onUpgrade(DbManager db, int oldVersion,
						int newVersion) {
					// TODO: upgrade
					Log.v("LK", "oldVersion=="+oldVersion+"newVersion=="+newVersion);
					if (newVersion>oldVersion) {
						try {
							db.dropDb();
							db.getDaoConfig();
						} catch (DbException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}
			});
	/** 单例数据库管理类 */
	private static DbManager xDbManager = null;

	public static ImageOptions IMG_OPT;

	/** 定位client */
	private LocationClient mLocationClient;
	/**位置信息*/
	private BDLocation mLoc = null;
	
	private Object mLock = new Object();
	
	private List<IOnLocChange> mLocListeners = new ArrayList<IOnLocChange>();

	private Context mCtx;
	
	private PersonTable mPerson = null;
	
	private IPersonChange iPersonChange = null;
	
	private static LauncherApplication appInstance = null;
	
	private PersonContentObserver mPersonObserver = null;
	
	private String mCityId = null;
	
	private Address address=null;
	
	public static LauncherApplication getInstance() {
		return appInstance;
	}
	
	public static DbManager getDbManager() {
		return xDbManager;
	}
	
	public void setPersonChange(IPersonChange p) {
		iPersonChange = p;
	}
	
	private void registerObserver() {
		mPersonObserver = new PersonContentObserver();  
	    getContentResolver().registerContentObserver(com.prize.cloud.util.Utils.PERSON_URI,  
	               true, mPersonObserver);
	}
	/***
	 * 注销内容观察者
	 */
	private void unregisterObserver() {
		getContentResolver().unregisterContentObserver(mPersonObserver);
	}

	private void initXUtils() {
		x.Ext.init(this);
		//x.Ext.setDebug(true);
		initImageLoader(this);
		xDbManager = x.getDb(daoConfig);
		x.Ext.setDebug(AppConfig.ISDEBUG);

		IMG_OPT = new ImageOptions.Builder().setRadius(4)
				.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE).build();
	}
	
	/**是否初始化过DB基本数据**/
	private final String INIT_KEY = "initOk";
	/***
	 * 初始化数据库
	 */
	private void initDb() {
		if (!PreferencesUtils.getBoolean(mCtx, INIT_KEY)) {
			try {
				xDbManager.getDatabase().beginTransaction();
				xDbManager.delete(CardType.class);
				String types = getBaseContext().getString(R.string.cardTypes);
				LogUtil.i(types);
				List<CardType> listType = CommonUtils.getObjects(types,
						CardType[].class);
				for (CardType c : listType)
//					xDbManager.save(c);
				
				xDbManager.delete(SelCardType.class);
				types = getBaseContext().getString(R.string.selCardTypes);
				
				List<SelCardType> selListType = CommonUtils.getObjects(types,
						SelCardType[].class);
				for (SelCardType d : selListType)
//					xDbManager.save(d);
				
				xDbManager.delete(SubCardType.class);
				types = getBaseContext().getString(R.string.one_news_channel);
				List<SubCardType> subTypes = CommonUtils.getObjects(types,
						SubCardType[].class);
				for (SubCardType d : subTypes)
					xDbManager.save(d);
				// xDbManager.save(selListType);
				
				types = getBaseContext().getString(R.string.big_card_type);
				List<BigCardType> bigTypes = CommonUtils.getObjects(types,
						BigCardType[].class);
				xDbManager.save(bigTypes);
				
				xDbManager.getDatabase().setTransactionSuccessful();
				PreferencesUtils.putBoolean(mCtx, INIT_KEY, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			xDbManager.getDatabase().endTransaction();
		}
		
		createTableIsNeed();
	}
	/***
	 * 需要检查是否要创建这些表
	 */
	private void createTableIsNeed() {
		DbManagerImpl impl = (DbManagerImpl)xDbManager;
		try {
			TableEntity<AccountTable> table = TableEntity.get(xDbManager, AccountTable.class);
			impl.createTableIfNotExist(table);
			table = null;
			
			
			
			
			TableEntity<PersonTable> table2 = TableEntity.get(xDbManager, PersonTable.class);
			impl.createTableIfNotExist(table2);
			table2 = null;
			
			TableEntity<HotWordTable> table3 = TableEntity.get(xDbManager, HotWordTable.class);
			impl.createTableIfNotExist(table3);
			table3 = null;
			
			TableEntity<NetNaviTable> table4 = TableEntity.get(xDbManager, NetNaviTable.class);
			impl.createTableIfNotExist(table4);
			table4 = null;
			
			
			
			
			TableEntity<NormalAddrTable> table5 = TableEntity.get(xDbManager, NormalAddrTable.class);
			impl.createTableIfNotExist(table5);
			table5 = null;
			

			
			TableEntity<PushTable> table6 = TableEntity.get(xDbManager, PushTable.class);
			impl.createTableIfNotExist(table6);
			table6 = null;
			
			TableEntity<HotBoxTable> table7 = TableEntity.get(xDbManager, HotBoxTable.class);
			impl.createTableIfNotExist(table7);
			table7 = null;
			
			TableEntity<HotTipsTable> table8 = TableEntity.get(xDbManager, HotTipsTable.class);
			impl.createTableIfNotExist(table8);
			table8 = null;
			
			
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 实现实时位置回调监听
	 */
	class LeftLocListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation loc) {
			// Receive Location
			if (loc != null)
				mLoc = loc;
			StringBuffer sb = new StringBuffer(256);
			sb.append("====>latitude : ");
			sb.append(loc.getLatitude());
			sb.append(";lontitude : ");
			sb.append(loc.getLongitude());
			
			switch (loc.getLocType()) {
				case BDLocation.TypeGpsLocation:
				case BDLocation.TypeNetWorkLocation:
				case BDLocation.TypeOffLineLocation:
					sb.append(";describe : ");
					sb.append("gps定位成功");
					if (null == mCityId)
						mCityId = loc.getCityCode();
						address=loc.getAddress();
					synchronized(mLock) {
						int sz = mLocListeners.size();
						boolean hasNet = ClientInfo.getAPNType(mCtx) != 0;
						for (int i=0; i<sz; i++) {
							IOnLocChange o = mLocListeners.get(i);
							if (o != null) {
								o.onLocChange(loc, hasNet ? 0 : 1);
							}
						}
					}
					LogUtil.i(sb.toString());
					return;
				case BDLocation.TypeServerError:
					sb.append("\ndescribe : ");
					sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
					break;
				case BDLocation.TypeNetWorkException:
					sb.append("\ndescribe : ");
					sb.append("网络不同导致定位失败，请检查网络是否通畅");
					break;
				case BDLocation.TypeCriteriaException:
					sb.append("\ndescribe : ");
					sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
					break;
			}
			// 定位失败
			synchronized(mLock) {
				int sz = mLocListeners.size();
				for (int i=0; i<sz; i++) {
					IOnLocChange o = mLocListeners.get(i);
					if (o != null) {
						o.onLocChange(null, -1);
					}
				}
			}
			
			LogUtil.i(sb.toString());
		}

	}

	/***
	 * 初始化定位相关东东
	 */
	private void initLoc() {
		mLocationClient = new LocationClient(getApplicationContext());
		mLocationClient.registerLocationListener(new LeftLocListener());

		initLocation();
	}

	/***
	 * 开始定位
	 */
	public void startLoc() {
		if (mLocationClient != null && !mLocationClient.isStarted())
			mLocationClient.start();
		
		if (mLocationClient == null)
			initLoc();
	}

	/***
	 * 结束定位
	 */
	public void stopLoc() {
		if (mLocationClient != null)
			mLocationClient.stop();
	}
	/***
	 * 获取定位信息
	 * @return
	 */
	public BDLocation getLoc() {
		return mLoc;
	}
	/***
	 * 获取定到位的城市ID
	 * @return
	 */
	public String getCityId() {
		if (mLoc != null) {
			String code = mLoc.getCityCode();
			if (!TextUtils.isEmpty(code))
				mCityId = code;
		}
		return mCityId;
	}
	/**
	 * 
	 * @return
	 */
	public Address getAddress() {
		if (mLoc != null) {
			Address add = mLoc.getAddress();
			if (add!=null)
				address = add;
		}
		return address;
	}
	/***
	 * 位置信息注册监听
	 * @param o
	 */
	public void registerLocListener(IOnLocChange o) {
		synchronized(mLock) {
			if (!mLocListeners.contains(o))
				mLocListeners.add(o);
		}
	}
	
	/***
	 * 移除位置信息注册监听
	 * @param o
	 */
	public void unregisterLocListener(IOnLocChange o) {
		synchronized(mLock) {
			if (mLocListeners.contains(o))
				mLocListeners.remove(o);
		}
	}
	/***
	 * 初始化定位的基本要素
	 */
	private void initLocation() {
		LocationClientOption option = new LocationClientOption();
		// 可选，默认高精度，设置定位模式，高精度(LocationMode.Hight_Accuracy)，低功耗LocationMode.Battery_Saving，仅设备LocationMode.Device_Sensors
		option.setLocationMode(LocationMode.Battery_Saving);
		// 可选，默认gcj02，设置返回的定位结果坐标系 "gcj02","bd09ll","bd09";
		option.setCoorType("gcj02");
		int span = 120000;
		// 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
		option.setScanSpan(span);
		// 可选，设置是否需要地址信息，默认不需要
		option.setIsNeedAddress(true);
		// 可选，默认false,设置是否使用gps
		option.setOpenGps(false);

		// option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
		// 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
		option.setIgnoreKillProcess(true);
		mLocationClient.setLocOption(option);
	}

	public SQLiteDatabase getDatabase() {
		return xDbManager.getDatabase();
	}
	/***
	 * 获取登录信息
	 * @return
	 */
	public PersonTable getLoginPerson() {
		if (null == mPerson)
			try {
				// 暂时注释掉, 因没有东东
				// mPerson = xDbManager.findFirst(PersonTable.class);
				if (null == mPerson) {
					toPersonTable(com.prize.cloud.util.Utils.getPersonalInfo(getBaseContext()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		return mPerson;
	}
	
	private class PersonContentObserver extends ContentObserver {  
		   
        public PersonContentObserver() {  
            super(new Handler());
        }  
        /** 
         * Receives notification when the data in the observed content 
         * provider changes. 
         */  
        public void onChange(final boolean selfChange) {  
            onChange(selfChange, null);
        }
        
        @Override
        public void onChange(boolean selfChange, Uri uri) {
        	//super.onChange(selfChange, uri);
        	
        	if (!selfChange) {
        		Person p = com.prize.cloud.util.Utils.getPersonalInfo(getBaseContext());
        		
        		toPersonTable(p);
        		if (iPersonChange != null)
        			iPersonChange.onPersonChange();
        	}
        }
    }
	
	private PersonTable toPersonTable(Person p) {
		try {
			if (null == p) {
	    		xDbManager.delete(PersonTable.class);
	    		mPerson = null;
	    		return null;
	    	}
			
	    	if (null == mPerson)
	    		mPerson = new PersonTable();
	    	mPerson.email = p.getEmail();
	    	mPerson.avatar = p.getAvatar();
	    	mPerson.phone = p.getPhone();
	    	mPerson.userId = p.getUserId();
	    	mPerson.realName = p.getRealName();
	    	mPerson.sex = p.getSex();
    	
			xDbManager.saveOrUpdate(mPerson);
		} catch (DbException e) {
			e.printStackTrace();
		}
    	return mPerson;
    }
	
	public interface IPersonChange {
		void onPersonChange();
	}
	/**@przie end }**/
}