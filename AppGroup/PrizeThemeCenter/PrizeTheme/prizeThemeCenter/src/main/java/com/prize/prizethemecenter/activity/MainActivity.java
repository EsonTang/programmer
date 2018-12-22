package com.prize.prizethemecenter.activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.iapppay.sdk.main.IAppPay;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.HttpUtils;
import com.prize.app.util.HttpUtils.RequestPIDCallBack;
import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.app.util.PreferencesUtils;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.AppsItemBean;
import com.prize.prizethemecenter.ui.actionbar.ActionBarTabActivity;
import com.prize.prizethemecenter.ui.page.BasePage;
import com.prize.prizethemecenter.ui.page.HomePage;
import com.prize.prizethemecenter.ui.page.HomePage.CallBack;
import com.prize.prizethemecenter.ui.page.MinePage;
import com.prize.prizethemecenter.ui.page.WallpaperFontPage;
import com.prize.prizethemecenter.ui.utils.MTAUtil;
import com.prize.prizethemecenter.ui.utils.PayConfig;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UpdateDataUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.UpdateSelfDialog;
import com.prize.prizethemecenter.ui.widget.indicator.IconTextPagerAdapter;
import com.prize.prizethemecenter.ui.widget.indicator.TabTextImagePageIndicator;
import com.prize.prizethemecenter.ui.widget.view.CustomViewPager;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatReportStrategy;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 * 主界面MainActivity
 * @author pengyang
 * @version V1.0
 */
public class MainActivity extends ActionBarTabActivity {
	public static MainActivity thisActivity = null;
	private static final int[] ICONS = { R.string.app_theme, R.string.app_wallpaper,
			R.string.app_font, R.string.app_my };
	private static final int[] ICONSRES = { R.drawable.tab_home_selector,
			R.drawable.tab_wallpaper_selector, R.drawable.tab_font_selector,
			R.drawable.tab_mine_selector };

	/*** pageID */
	private static final int HOME_PAGER_ID = 0;
	private static final int WALLPAPER_PAGER_ID = 1;
	private static final int FONT_PAGE_ID = 2;
	private static final int MINE_PAGER_ID = 3;

	private HomePage homePage;
	private WallpaperFontPage wallPaperPage;
	private MinePage minePage;
	private WallpaperFontPage fontPage;

	private CustomViewPager viewPager;
	private TabTextImagePageIndicator mIndicator;
	private RelativeLayout download_queue_Rlyt;
	private int currentPage;
	private AppsItemBean bean;
	private BasePage[] pagers = new BasePage[ICONS.length];
	private Callback.Cancelable reqHandler;
	public List<AppsItemBean> data;
	private DownloadManager downloadManager;
	private Handler mHander;
	private final String TAG = "MainActivity";
	public LinearLayout mTitleLlyt;
	public TextView searchKey_Tv;
	public ImageView searchkey_logo;
	private int currentAlpha;
	public View main_line;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		WindowMangerUtils.initStateBar(getWindow(), this);
		thisActivity = this;
		setContentView(R.layout.activity_main);
		findViewById();
		init();
		setOnClicListener();
		sendPushCast();
		checkNewVersion();
		requestUUID();
		initPay();
		initMTA();
		// 接收推送默认开启
		String push_notification = DataStoreUtils
				.readLocalInfo(DataStoreUtils.RECEIVE_NOTIFICATION);
		if (!DataStoreUtils.CHECK_OFF.equals(push_notification)) {
			initPush();
		}
	}

	/**支付SDK初始化*/
	private void initPay() {
		/** IAppPay.PORTRAIT  竖屏
		    IAppPay.LANDSCAPE  横屏
		    IAppPay.SENSOR_LANDSCAPE  横屏自动切换
		*/
		IAppPay.init(MainActivity.this, IAppPay.PORTRAIT, PayConfig.appid);//接入时！不要使用Demo中的appid
	}

	/**
	 * 初始化MTA统计平台
	 */
	private void initMTA() {
		// androidManifest.xml指定本activity最先启动
		// 因此，MTA的初始化工作需要在本onCreate中进行
		// 在startStatService之前调用StatConfig配置类接口，使得MTA配置及时生效
		// initMTAConfig(true);
		String appkey = "AS6XXYP716SK";
		// 初始化并启动MTA
		// 第三方SDK必须按以下代码初始化MTA，其中appkey为规定的格式或MTA分配的代码。
		// 其它普通的app可自行选择是否调用
		try {
			// 第三个参数必须为：com.tencent.stat.common.StatConstants.VERSION
			StatConfig.setStatSendStrategy(StatReportStrategy.INSTANT);
			StatService.startStatService(this, appkey,
					com.tencent.stat.common.StatConstants.VERSION);
//			JLog.i("hu","MTA success");
		} catch (MtaSDkException e) {
			e.printStackTrace();
//			JLog.i("hu","MTA failed");
		}
	}

//	private void initPay() {
//		SFCommonSDKInterface.onInit(this);
//		SFCommonSDKInterface.onInit(this,new SFOfflineInitListener() {
//			@Override
//			public void onResponse(String tag, String value) {
//				if(tag.equalsIgnoreCase("success")){
//                       //初始化成功的回调
//					ToastUtils.showToast("success");
//				}else if(tag.equalsIgnoreCase("fail")){
//                   //初始化失败的回调，value：如果SDK返回了失败的原因，会给value赋值
//					ToastUtils.showToast("fail");
//				}
//			}});
//	}

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
  
	private void requestUUID() {
		if (TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
			HttpUtils.getPidFromServer(new RequestPIDCallBack() {

				@Override
				public void requestOk(String pid) {
					HttpUtils.getUuidFromServer(pid);
				}
			});
		}
	}

	private void init() {
		mIndicator.setViewPager(viewPager);
		mIndicator.setVisibility(View.VISIBLE);

		int page = getIntent().getIntExtra("page",0);
		if (page==0) {
			mIndicator.setCurrentItem(HOME_PAGER_ID);
			currentPage = HOME_PAGER_ID;
		}else if (page==1){
			mIndicator.setCurrentItem(WALLPAPER_PAGER_ID);
			currentPage = WALLPAPER_PAGER_ID;
		}
		if (ClientInfo.networkType != ClientInfo.NONET
				&& ClientInfo.networkType != ClientInfo.WIFI) {
//			ToastUtils.showToast(R.string.tip_moblie_net);
		}
	}

	private void initTabs() {
		MainAdapter adapter = new MainAdapter();
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(4);
	}

	private void findViewById() {
		mTitleLlyt = (LinearLayout) findViewById(R.id.linearLayout);
		searchKey_Tv = (TextView) findViewById(R.id.searchKey_Tv);
		searchkey_logo = (ImageView)findViewById(R.id.searchkey_logo);
		main_line =  findViewById(R.id.main_line);

		viewPager = (CustomViewPager) findViewById(R.id.pager);
		mIndicator = (TabTextImagePageIndicator) findViewById(R.id.indicator);
		viewPager.setScanScroll(false);
		initTabs();
	}


	/**
	 * 检测新版本
	 * @return void
	 */
	public void checkNewVersion() {
		String mUrl =  Constants.SYSTEM_UPGRADE_URL;
		RequestParams params = new RequestParams (mUrl);

		reqHandler = x.http().post(params, new Callback.CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				try {
					JSONObject obj = new JSONObject(result);
					if (obj.getInt("code") == 00000) {
						JSONObject o = new JSONObject(obj.getString("data"));
						bean = new Gson().fromJson(o.getString("app"),
								AppsItemBean.class);
						if (appIsNeedUpate(bean.packageName,
								bean.versionCode)) {
							JLog.i("hu","appIsNeedUpate");
							PreferencesUtils.putString(MainActivity.this,
									Constants.APP_MD5, bean.apkMd5);
							if (downloadManager == null) {
								downloadManager = (DownloadManager) MainActivity.this
										.getSystemService(Context.DOWNLOAD_SERVICE);
							}
							if ((new File(Constants.APKFILEPATH)).exists()) {
								queryDownloadStatus();
							} else {
								if (ClientInfo.networkType != ClientInfo.NONET) {
									String wifiSettingString = DataStoreUtils
											.readLocalInfo(DataStoreUtils.DOWNLOAD_WIFI_ONLY);
									if (ClientInfo.networkType != ClientInfo.WIFI) {
										if (wifiSettingString
												.equals(DataStoreUtils.DOWNLOAD_WIFI_ONLY_ENABLE)) {
											return;
										}
									}
									if (mHander == null) {
										mHander = new Handler();
									}
									mHander.postDelayed(new Runnable() {
										public void run() {
											UpdateDataUtils.downloadApk(
													downloadManager, bean,
													MainActivity.this);
										}
									}, 3000);
								}
							}
						} else {
							File file = new File(Constants.APKFILEPATH);
							if (file.exists()) {
								file.delete();
							}
							PreferencesUtils.putString(MainActivity.this,
									Constants.APP_MD5, "");
							getContentResolver().delete(
									MediaStore.Files.getContentUri("external"),
									"_DATA=?",
									new String[] { Constants.APKFILEPATH });
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
			}

			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});
	}
		private void setOnClicListener(){
			homePage.setmCallBack(new CallBack() {

				@Override
				public void onScrollStates(int alpha) {
					if (mTitleLlyt == null || currentPage != 0) {
						return;
					}
					currentAlpha = alpha;

					if (currentAlpha > 245 & currentAlpha <= 255) {
						mTitleLlyt.getBackground().setAlpha(255);
						WindowMangerUtils.changeStatus(getWindow());
					} else {
						if (mTitleLlyt.getBackground() != null) {
							mTitleLlyt.getBackground().setAlpha(alpha);
							WindowMangerUtils.changeStatus2White(getWindow());
						}
					}
				}
			});

			searchKey_Tv.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this,
									SearchActivity.class);
					intent.putExtra(SearchActivity.STR, ((TextView) v).getText()
									.toString());
					intent.putExtra(SearchActivity.CURRENTPOSITION, currentPage+"");
					startActivity(intent);
				}
					});

			searchkey_logo.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					//bug 25712 modify by pengy
					if(!TextUtils.isEmpty(searchKey_Tv.getText())){
						MTAUtil.onSearchHotKey(searchKey_Tv.getText()
								.toString());
					}
					Intent intent = new Intent(MainActivity.this,
							SearchActivity.class);
					intent.putExtra(SearchActivity.STR_SEARCH, searchKey_Tv.getText()
							.toString());
					intent.putExtra(SearchActivity.CURRENTPOSITION, currentPage+"");
					startActivity(intent);

				}
			});

			// 指示器
			mIndicator.setOnPageChangeListener(new OnPageChangeListener() {

				@Override
				public void onPageSelected(int position) {
					// TODO Auto-generated method stub
//					PriorityRunnable.decreaseBase();
					if (currentPage != position) {
						pagers[currentPage].onPause(); // 前一页pause
						pagers[position].onResume();
					}
					homePage.setAutoScroll(position == HOME_PAGER_ID);
					fontPage.setAutoScroll(position == FONT_PAGE_ID);
					wallPaperPage.setAutoScroll(position == WALLPAPER_PAGER_ID);
					pagers[position].loadData();
					responseChangeTab(position);
					currentPage = position;
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {

				}

				@Override
				public void onPageScrollStateChanged(int arg0) {

				}
			});

			mIndicator
			.setOnTabReselectedListener(new TabTextImagePageIndicator.OnTabReselectedListener() {

				@Override
				public void onTabReselected(int position) {
					if (currentPage == position) {
						pagers[currentPage].scrollToTop();
					}
				}
			});
		}

		/**
		 * 判断是否要更新版本(getPackageArchiveInfo)，根据versionCode来判断
		 * @param packageName
		 * @param versionCode
		 * @return
		 */
		public static boolean appIsNeedUpate(String packageName, int versionCode) {
			try {
				ApplicationInfo applicationInfo = BaseApplication.curContext
						.getPackageManager().getApplicationInfo(packageName, 0);

				if (!isNewMethod()) {
					return applicationInfo.versionCode < versionCode;
				}
				PackageInfo packageInfo = BaseApplication.curContext
						.getPackageManager().getPackageArchiveInfo(
								applicationInfo.publicSourceDir, 0);
				JLog.i("hu","versioncode="+packageInfo.versionName + packageInfo.versionCode);
				if (packageInfo != null) {
					return packageInfo.versionCode < versionCode;
				}
			} catch (NameNotFoundException e) {
				return false;
			}
			return false;
		}

		public static boolean isNewMethod(){
			return "1".equals(SystemProperties.get("ro.prize_app_update_appcenter", "0"));
		}

	/**
	 * 响应tab切换时title栏的状态
	 * @param position
	 * @return void
	 * @see
	 */
	private void responseChangeTab(int position) {
		if (mTitleLlyt == null) {
			return;
		}
		if (position == 0) {
			if (homePage != null && homePage.gridview != null
					&& homePage.gridview.getFirstVisiblePosition() == 0) {
				if (mTitleLlyt.getBackground() != null) {
					mTitleLlyt.getBackground().setAlpha(currentAlpha);
					WindowMangerUtils.changeStatus2White(getWindow());
				}
			} else {
				if (mTitleLlyt.getBackground() != null) {
					mTitleLlyt.getBackground().setAlpha(currentAlpha);
					WindowMangerUtils.changeStatus(getWindow());
				}
			}
			main_line.setVisibility(View.INVISIBLE);
		} else {
			if (mTitleLlyt.getBackground() != null) {
				mTitleLlyt.getBackground().setAlpha(255);
				WindowMangerUtils.changeStatus(getWindow());
			}
			main_line.setVisibility(View.VISIBLE);
		}
		setStrs(pagers[0].getWords());
	}

	@Override
	protected void onResume() {
		pagers[currentPage].onResume();
//add by zhouerlong select wallpaper or theme
		if(viewPager!=null) {
			PagerAdapter adapter = viewPager.getAdapter();
			if(adapter!=null) {
				MainAdapter p = (MainAdapter) adapter;
				p.onResume();
			}

		}
//add by zhouerlong select wallpaper or theme
		pagers[currentPage].setAutoScroll(true);
		isAutoScroll = true;
//		SFCommonSDKInterface.onResume(this);
		super.onResume();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);// 必须要调用这句
	}
	@Override
	protected void onPause() {
		pagers[currentPage].onPause();
		pagers[currentPage].setAutoScroll(false);
		isAutoScroll = false;
//		SFCommonSDKInterface.onPause(this);
		super.onPause();
	}

	class MainAdapter extends PagerAdapter implements IconTextPagerAdapter {
		int page = -1;
		public MainAdapter() {
			// 首页
			page = getIntent().getIntExtra("page",0);
			homePage = new HomePage(MainActivity.this);
			homePage.getView(); // 需要初始化，原因：page 可以跳转
			pagers[HOME_PAGER_ID] = homePage;
//			if (page == 0) {
				homePage.loadData();
//			}

			// 壁纸
			wallPaperPage = new WallpaperFontPage(MainActivity.this, false);
			pagers[WALLPAPER_PAGER_ID] = wallPaperPage;
			pagers[WALLPAPER_PAGER_ID].getView();
			if(page==1){
				wallPaperPage.loadData();
			}
			// 字体
			fontPage = new WallpaperFontPage (MainActivity.this, true);
			pagers[FONT_PAGE_ID] = fontPage;
			pagers[FONT_PAGE_ID].getView();

			// 我的
			minePage = new MinePage(MainActivity.this);
			pagers[MINE_PAGER_ID] = minePage;
			pagers[MINE_PAGER_ID].getView();

		}
//add by zhouerlong select wallpaper or theme
		public void onResume() {

			page = getIntent().getIntExtra("page",0);
			//add by zhouerlong bug id 30260
			boolean fromMore = getIntent().getBooleanExtra("from_more",false);
			if(homePage!=null) {
				if (fromMore&&page == 0) {
//					homePage.loadData();
					viewPager.setCurrentItem(0);
				}
			}

			if(wallPaperPage!=null) {
				if(fromMore&&page ==1) {
//					wallPaperPage.loadData();
					viewPager.setCurrentItem(1);
				}
			}
			//add by zhouerlong bug id 30260
		}
//add by zhouerlong select wallpaper or theme

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(pagers[position].getView());
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			container.addView(pagers[position].getView());
			return pagers[position].getView();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			int id = ICONS[position % ICONS.length];
			return getString(id);
		}

		@Override
		public int getCount() {
			return ICONS.length;
		}

		@Override
		public int getIconResId(int index) {
			return ICONSRES[index];
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	private static final long PRESS_BACK_TIME = 2 * 1000;
	/** 上次按返回的时间 */
	private long lastPressBackTime = 0;

	@Override
	/**
	 *  实现按两次Back键退出
	 */
	public void onBackPressed() {
		long currentPressBackTime = System.currentTimeMillis();
		if (currentPressBackTime - lastPressBackTime < PRESS_BACK_TIME) {
			finish();
//			SFCommonSDKInterface.onExit(this, new SFGameExitListener() {
//				@Override
//				public void onGameExit(boolean flag) {
//					if (flag) {
//                       //SDK已经退出，此处可处理游戏的退出代码
//						MainActivity.this.finish();
//						JLog.i("hu","onbackPressed");
//					}
//				}
//			});
		} else {
			ToastUtils.showToast(R.string.toast_exit, Gravity.BOTTOM);
		}
		lastPressBackTime = currentPressBackTime;

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (reqHandler != null) {
			reqHandler.cancel();
		}
		pagers[MINE_PAGER_ID].onDestroy();
		pagers[WALLPAPER_PAGER_ID].onDestroy();
		pagers[FONT_PAGE_ID].onDestroy();
		pagers[HOME_PAGER_ID].onDestroy();
		if (flowHandler != null) {
			flowHandler.removeCallbacksAndMessages(null);
		}
		HttpUtils.uploadCarClickDataInfo();
//		SFCommonSDKInterface.onDestroy(this);
	}

	@Override
	public void finish() {
		thisActivity = null;
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
		super.finish();
		RootActivity.exitActivity();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("position", currentPage);
		if (pagers[currentPage] != null) {
			pagers[currentPage].onSaveInstanceState(outState);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		currentPage = savedInstanceState.getInt("position", 0);
		if (pagers[currentPage] != null) {
			pagers[currentPage].onRestoreInstanceState(savedInstanceState);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public String getActivityName() {
		return null; // 返回null ，因为需要统计page友盟统计
	}

	@Override
	protected void initActionBar() {
		enableSlideLayout(false);
	}

	/**
	 * 发送取消提醒数字广播
	 *
	 * @return void
	 * @see
	 */
	private void sendPushCast() {
		// 软件更新提示设置
//		String updateNotifySetting = DataStoreUtils
//				.readLocalInfo(DataStoreUtils.GAME_UPDATES_REMINDER);
//		if (!DataStoreUtils.CHECK_OFF.equals(updateNotifySetting)) {
//			Intent intent = new Intent();
//			intent.setAction(Constants.ACTION_UNREAD_CHANGED);
//			intent.putExtra("package", getApplicationInfo().packageName);
//			intent.putExtra(Constants.EXTRA_UNREAD_NUMBER, 0);
//			intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//			sendStickyBroadcast(intent);
//		}
	}

	/** 系统版打开，第三方版注销方法内容 */
	@SuppressLint("NewApi")
	protected void initStatusBar() {
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.status_color));
		}
	}

	private void queryDownloadStatus() {
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(PreferencesUtils.getLong(this,
				Constants.KEY_NAME_DOWNLOAD_ID));
		JLog.i("hu","downloadid == "+PreferencesUtils.getLong(this,
				Constants.KEY_NAME_DOWNLOAD_ID));
		Cursor c = downloadManager.query(query);
		if (c.moveToFirst()) {
			int status = c.getInt(c
					.getColumnIndex(DownloadManager.COLUMN_STATUS));
			switch (status) {
			case DownloadManager.STATUS_PAUSED:
			case DownloadManager.STATUS_PENDING:
			case DownloadManager.STATUS_RUNNING:
				// 正在下载，不做任何事情
				break;
			case DownloadManager.STATUS_SUCCESSFUL:
				JLog.i("hu","STATUS_SUCCESSFUL");
				// 完成
				displayDialog();
				break;
			case DownloadManager.STATUS_FAILED:
				// 清除已下载的内容，重新下载
				JLog.i("hu","STATUS_FAILED");
				downloadManager.remove(PreferencesUtils.getLong(this,
						Constants.KEY_NAME_DOWNLOAD_ID));
				PreferencesUtils.putLong(this, Constants.KEY_NAME_DOWNLOAD_ID,
						-1);
				break;
			}
		}
	}

	private static final int SCROLL = 100;
	/** 自动滚动 */
	private boolean isAutoScroll = true;
	/** 图片滚动间隔 */
	private final long delayMillis = 2 * 1000;
	/** 图片滚动任务 */
	private ArrayList<String> strs;
	private Handler flowHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SCROLL: {
				if (isAutoScroll && (null != searchKey_Tv)) {
					refresh();
					flowHandler.removeMessages(SCROLL);
				}
				flowHandler.sendEmptyMessageDelayed(SCROLL, delayMillis);
			}
				break;
			}
		}
	};

	public void setStrs(ArrayList<String> strs) {
		this.strs = strs;
		refresh();
		if (!flowHandler.hasMessages(SCROLL)) {
			flowHandler.sendEmptyMessageDelayed(SCROLL, 100);
		}
	}

	public void refresh() {
		if (searchKey_Tv != null && strs != null && strs.size() > 0) {
			searchKey_Tv.setText(strs.get((int) (Math.random() * strs.size())));
		}
	}

	private UpdateSelfDialog mUpdateSelfDialog;
	private boolean isShow = true;

	private void displayDialog() {
//		JLog.i("hu","bean=="+bean.downloadUrlCdn + "--isResumed()=="+isResumed()+ "--isShow()=="+isShow);
		if (bean == null|| !isResumed() || !isShow)
			return;
		try {
			if (appIsNeedUpate(bean.packageName,
					bean.versionCode)) {
				File file = new File(Constants.APKFILEPATH);
				if (file.exists()) {
					PackageInfo localPackageInfo = getPackageManager()
							.getPackageArchiveInfo(Constants.APKFILEPATH, PackageManager.GET_ACTIVITIES);
					if ((localPackageInfo != null)
							&& (bean.packageName
									.equals(localPackageInfo.packageName))
							&& (this.bean.versionCode == localPackageInfo.versionCode)
							&& (MD5Util.Md5Check(Constants.APKFILEPATH,
									bean.apkMd5))) {
						if (mUpdateSelfDialog == null) {
							mUpdateSelfDialog = new UpdateSelfDialog(
									MainActivity.this, R.style.add_dialog,
									ClientInfo.getInstance().appVersionCode,
									getResources().getString(
											R.string.new_version_name,
											bean.versionName), bean.updateInfo);
							mUpdateSelfDialog.setBean(bean);
						}
						if (mUpdateSelfDialog != null
								&& !mUpdateSelfDialog.isShowing()) {
							mUpdateSelfDialog.show();
							isShow = false;
						}
					} else {
						file.delete();
					}
				}
			} else {
				isShow = false;
			}
		} catch (Resources.NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
