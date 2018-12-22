package com.prize.prizethemecenter;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.prize.app.BaseApplication;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.PreferencesUtils;
import com.prize.cloud.bean.Person;
import com.prize.custmerxutils.XExtends;
import com.prize.prizethemecenter.bean.table.AdTable;
import com.prize.prizethemecenter.bean.table.DownloadedTable;
import com.prize.prizethemecenter.bean.table.LocalFontTable;
import com.prize.prizethemecenter.bean.table.SearchHistoryTable;
import com.prize.prizethemecenter.bean.table.ThemeDetailTable;
import com.prize.prizethemecenter.bean.table.ThemeItemTable;
import com.prize.prizethemecenter.bean.table.TipsTable;
import com.prize.prizethemecenter.bean.table.WallDetailTable;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.receiver.NetStateReceiver;
import com.prize.prizethemecenter.receiver.ScreenListener;
import com.prize.prizethemecenter.receiver.ScreenListener.ScreenStateListener;
import com.prize.prizethemecenter.service.DownloadService;
import com.prize.prizethemecenter.ui.utils.FontModel;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;

import org.xutils.DbManager;
import org.xutils.db.DbManagerImpl;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.util.List;

import me.myfont.fontsdk.FounderFont;

/**
 * @author pengyang
 */
public class MainApplication extends BaseApplication {
	private static NetStateReceiver netstateReceiver;
	private static boolean hasInit = false;
	public static int commentCount;
	public static int commentCount1;
	protected String TAG = "MainApplication";
	private LoginDataCallBack loginDataCallBack;
	private Person person;
	private static MainApplication instance;
	private static Handler	mHandler;
	private static Thread	mMainThread;

	/** 单例数据库管理类 */
	private static DbManager xDbManager = null;
	private static DownloadTaskMgr downloadManager;


	//Demo appKey,请更换为在平台申请的appKey
	public static String APP_KEY = "f79be623-faac-44ce-bbae-2d3f561186f5";

	public static MainApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		FileUtils.initAppPath();

		//初始化字体sdk
		FounderFont.init(this, APP_KEY);

		initApp();
		XExtends.Ext.init(this);
		XExtends.Ext.setDebug(false);
		initImageLoader(this);
		xDbManager = x.getDb(daoConfig);
		downloadManager = DownloadService.getDownloadManager(this);
		initDB();
		// 接收推送默认开启
		String push_notification = DataStoreUtils
				.readLocalInfo(DataStoreUtils.RECEIVE_NOTIFICATION);
		if (DataStoreUtils.CHECK_OFF.equals(push_notification)) {
			XGPushManager.unregisterPush(getApplicationContext());
		}else {
			initPush();
		}
		// MTA统计  true 为自动捕获异常
		StatConfig.setAutoExceptionCaught(true);
		StatService.trackCustomEvent(this, "onCreate", "");

		// 主线程
		mMainThread = Thread.currentThread();
		mHandler = new Handler();
		String processName = getProcessName(this, mMainThreadId);
		JLog.i(TAG, "processName=" + processName);
		if (processName != null) {
			boolean defaultProcess = processName.equals(getPackageName());
			if (defaultProcess) {
				isOnCreate = true;
				if (person == null) {
					person = queryUserId();
				}
				registerObserver();
			}

		}
		registerScreenLister();
		FontModel fontModel = new FontModel(this);
		fontModel.loadLocalFont("font/");

	}

	private void initPush() {
		XGPushConfig.enableDebug(this, true);

		// 注册接口
		XGPushManager.registerPush(getApplicationContext(),
				new XGIOperateCallback() {
					@Override
					public void onSuccess(Object data, int flag) {
						Log.w(com.tencent.android.tpush.common.Constants.LogTag,
								"+++ register push sucess. token:" + data);
					}

					@Override
					public void onFail(Object data, int errCode, String msg) {
						Log.w(com.tencent.android.tpush.common.Constants.LogTag,
								"+++ register push fail. token:" + data
										+ ", errCode:" + errCode + ",msg:"
										+ msg);
					}
				});
	}
	/**是否初始化过DB基本数据**/
	private final String INIT_KEY = "initOk";

    /**数据库初始化*/
	private void initDB() {
		if (!PreferencesUtils.getBoolean(instance,INIT_KEY)){
			try {
				xDbManager.getDatabase().beginTransaction();
				xDbManager.delete(AdTable.class);
				xDbManager.delete(ThemeItemTable.class);
				xDbManager.getDatabase().setTransactionSuccessful();
				PreferencesUtils.putBoolean(instance, INIT_KEY, true);
			} catch (DbException e) {
				e.printStackTrace();
			}
			xDbManager.getDatabase().endTransaction();
		}
         createdTable();
	}

	private void createdTable() {
		DbManagerImpl impl = (DbManagerImpl)xDbManager;
		try {
			TableEntity<AdTable> adTable = TableEntity.get(xDbManager,AdTable.class);
			impl.createTableIfNotExist(adTable);
			adTable = null;

			TableEntity<ThemeItemTable> themeTable = TableEntity.get(xDbManager,ThemeItemTable.class);
			impl.createTableIfNotExist(themeTable);
			themeTable = null;

			TableEntity<SearchHistoryTable> historyTable = TableEntity.get(xDbManager,SearchHistoryTable.class);
			impl.createTableIfNotExist(historyTable);
			historyTable = null;

			TableEntity<TipsTable> tipsTable = TableEntity.get(xDbManager,TipsTable.class);
			impl.createTableIfNotExist(tipsTable);
			tipsTable = null;

			TableEntity<WallDetailTable> wallDetailTable = TableEntity.get(xDbManager,WallDetailTable.class);
			impl.createTableIfNotExist(wallDetailTable);
			wallDetailTable = null;

			TableEntity<ThemeDetailTable> downLoadManageTable = TableEntity.get(xDbManager, ThemeDetailTable.class);
			impl.createTableIfNotExist(downLoadManageTable);
			downLoadManageTable = null;

			TableEntity<LocalFontTable> fontTableEntity = TableEntity.get(xDbManager,LocalFontTable.class);
			impl.createTableIfNotExist(fontTableEntity);
			fontTableEntity = null;

			TableEntity<DownloadedTable> downLoadedTable = TableEntity.get(xDbManager, DownloadedTable.class);
			impl.createTableIfNotExist(downLoadedTable);
			downLoadedTable = null;

		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	/**数据库的创建*/
	private final int DB_VERSION = 1;
	private DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
			.setDbName("prizeThemeDB.db")
			.setDbVersion(DB_VERSION)
			.setDbUpgradeListener(new DbManager.DbUpgradeListener() {
				@Override
				public void onUpgrade(DbManager db, int oldVersion,
									  int newVersion) {
					// TODO: upgrade
				}
			});
	public static DbManager getDbManager() {
		return xDbManager;
	}

	private void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024)
//				.discCacheFileCount(100) //缓存的文件数量
				// 50 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				// .writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	/**
	 * 
	 * @return void
	 */
	public void initApp() {
		File crashFile = new File(FileUtils.CFG_APP_CRASH_FILE);
		if (crashFile.exists()) {
			// 发生异常了,啥都不初始化了
			crashFile.delete();
		} else {
			if (hasInit) {
				return;
			}
		}
		hasInit = true;
		BaseApplication.initBaseApp();
		// 启动下载服务
		// startDownloadService();
		// 网络切换监听
		netstateReceiver = new NetStateReceiver();
		this.registerReceiver(netstateReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
	}

	/**
	 * 方法描述：注册锁屏监听者
	 */
	private void registerScreenLister() {
		ScreenListener screenListener = new ScreenListener(this);
		screenListener.begin(new ScreenStateListener() {

			@Override
			public void onUserPresent() {

			}

			@Override
			public void onScreenOn() {

			}

			// 锁屏并且电量高于30%时才会调用
			@Override
			public void onScreenOff() {
			}

			@Override
			public void onScreenOffNoRLLevel() {
			}
		});
	}

	/**
	 * 方法描述：查询是否登录云账号
	 * 
	 * @return void
	 * @see /完整类名/完整类名#方法名
	 */
	private Person queryUserId() {
		ContentResolver resolver = this.getContentResolver();
		Uri uri = null;
		uri = Uri
				.parse("content://com.prize.appcenter.provider.appstore/table_person");
		Person person = new Person();
		String userId = null;
		String realName = null;
		String imgPath = null;
		try {
			Cursor cs = resolver.query(uri, null, null, null, null);
			if (cs != null && cs.moveToFirst()) {
				userId = cs.getString(cs.getColumnIndex("userId"));
				realName = cs.getString(cs.getColumnIndex("realName"));
				imgPath = cs.getString(cs.getColumnIndex("avatar"));
			}
			if (cs != null) {
				cs.close();
			}
			if (TextUtils.isEmpty(userId)) {
				return null;
			} else {
				if (!TextUtils.isEmpty(imgPath)) {
					person.setAvatar(imgPath);
				} else {
					person.setAvatar("");
				}
				if (!TextUtils.isEmpty(realName)) {
					person.setRealName(realName);
				} else {
					person.setRealName("");
				}
				person.setUserId(userId);
				return person;
			}
		} catch (Exception e) {
		}
		return person;
	}

	public void queryPerson() {
		person = queryUserId();
		JLog.i(TAG, "queryPerson-loginDataCallBack=" + loginDataCallBack
				+ "----person=" + person);
		if (loginDataCallBack != null) {
			loginDataCallBack.setPerson(person);
		}
	}

	/**
	 * 类描述：云账号是否登录监听接口，用于实时给个人中心页和详情页传递登录信息
	 * 
	 * @author 作者
	 * @version 版本
	 */
	public interface LoginDataCallBack {
		/**
		 * 方法描述：实时改变登录状态
		 * 
		 * @param
		 * @see /完整类名/完整类名#方法名
		 */
		void setPerson(Person person);
	}

	public void setLoginCallBack(LoginDataCallBack loginDataCallBack) {
		this.loginDataCallBack = loginDataCallBack;
	}

	public Person getPerson() {
		return person;
	}

	/**
	 * 方法描述：注册数据库监听器，已便实时刷新数据
	 */
	public void registerObserver() {
		PersonObserver personResolver = new PersonObserver(this, handler);
		Uri personUri = Uri
				.parse("content://com.prize.appcenter.provider.appstore/table_person");
		this.getContentResolver().registerContentObserver(personUri, true,
				personResolver);
	}

	private class PersonObserver extends ContentObserver {

		public PersonObserver(Context context, Handler handler) {
			super(handler);
		}

		/**
		 * 当所监听的Uri发生改变时，就会回调此方法
		 * 
		 * @param selfChange
		 *            此值意义不大 一般情况下该回调值false
		 */
		@Override
		public void onChange(boolean selfChange) {
			queryPerson();
		}

	}

	/**
	 * @return null may be returned if the specified process not found
	 */
	public static String getProcessName(Context cxt, int pid) {
		ActivityManager am = (ActivityManager) cxt
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
		if (runningApps == null) {
			return null;
		}
		for (RunningAppProcessInfo procInfo : runningApps) {
			if (procInfo.pid == pid) {
				return procInfo.processName;
			}
		}
		return null;
	}

	@Override
	public void onTerminate() {
		// 程序终止的时候执行
		JLog.i(TAG, "onTerminate");
		XGPushManager.unregisterPush(getApplicationContext());
		super.onTerminate();
	}

	public static Thread getMainThread()
	{
		return mMainThread;
	}

	public static Handler getHandler()
	{
		return mHandler;
	}

	public static DownloadTaskMgr getDownloadManager(){return downloadManager;}

}
