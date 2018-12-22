package com.prize.prizeappoutad.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.location.LocationClient;
import com.google.gson.Gson;
import com.prize.prizeappoutad.bean.AdDetailsInfo;
import com.prize.prizeappoutad.bean.AppSelfUpdateBean;
import com.prize.prizeappoutad.bean.ClientInfo;
import com.prize.prizeappoutad.bean.NetCommonInfo;
import com.prize.prizeappoutad.bean.OutAdInfo;
import com.prize.prizeappoutad.bean.PackageNamesInfo;
import com.prize.prizeappoutad.constants.Constants;
import com.prize.prizeappoutad.listener.MyLocationListener;
import com.prize.prizeappoutad.listener.MyLocationListener.GetAdressSuccess;
import com.prize.prizeappoutad.manager.ClockManager;
import com.prize.prizeappoutad.manager.InitUtilsManager;
import com.prize.prizeappoutad.manager.SelfUpdataUtilsManager;
import com.prize.prizeappoutad.manager.XExtends;
import com.prize.prizeappoutad.receiver.ScreenListener;
import com.prize.prizeappoutad.receiver.ScreenListener.ScreenStateListener;
import com.prize.prizeappoutad.utils.DownloadManagerPro;
import com.prize.prizeappoutad.utils.FileUtils;
import com.prize.prizeappoutad.utils.JLog;
import com.prize.prizeappoutad.utils.MD5Util;
import com.prize.prizeappoutad.utils.MTAUtils;
import com.prize.prizeappoutad.utils.PackageUtils;
import com.prize.prizeappoutad.utils.PreferencesUtils;
import com.prize.prizeappoutad.view.BannerAdView;
import com.prize.prizeappoutad.view.SlotAdView;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;
import com.zhy.http.okhttp.OkHttpUtils;

/**
 * 三方广告后台服务
 * 
 * @category 用于自升级和自启动
 * @author huangchangguo 2016.10.14
 * 
 */
public class ThirdAdService extends Service {

	private Cancelable reqHandler;
	private Cancelable reqFilterHandler;
	// 安坐自带下载-现在暂时未使用
	private DownloadManager downloadManager;
	private DownloadManagerPro downloadManagerPro;
	private Boolean isDownloaded;
	// 是否需要更新的开关
	private Boolean isNeedUpdate = false;
	// 包名json字符串
	private String packageNameInfo;
	// 本地得到的包名
	private String mNativePackageName;
	// handler-what
	private static final int GETPACKAGENAME = 1;
	private static final int NEEDTOUPDATE = GETPACKAGENAME + 1;
	private static final int GETPACKAGENAME2 = NEEDTOUPDATE + 1;
	private static final int TIMEDELAYTASK = GETPACKAGENAME2 + 1;
	private static final int SCREENPRECENT = TIMEDELAYTASK + 1;
	private static final int TIMEUPDATETASK = SCREENPRECENT + 1;
	private static final int SHOWRSPLASH = TIMEUPDATETASK + 1;
	// 如果返回的json为null，重新走一边加载的逻辑
	private int reFilter = -1;
	// 网络获取的包名
	ArrayList<String> netPackageNames = new ArrayList<>();
	private AppSelfUpdateBean bean;
	// 初始化文件夹
	private static boolean hasInit = false;
	// 是否需要检测自更新
	private boolean isCheackVersion = false;
	// 计时器开关，是否允许请求包名
	private static boolean timerDo = false;
	// 计时器间隔 5min,请求网络
	private static final int TIMEDELAY = 5 * 60 * 1000;
	// 计时器间隔 半个小时检测升级
	// private static final int TIMEDELAYUPDATE = 30 * 60 * 1000;
	// test
	private static final int TIMEDELAYUPDATE = 2 * 60 * 1000;
	// test
	// private static final int TIMEDELAY = 20 * 1000;
	// 是否已经得到了包名
	private static boolean isGetAdress = false;
	// 是否执行了oncreate方法
	private static boolean isOnCreateService = false;
	// 百度定位
	private MyLocationListener mLBSListener;
	private LocationClient mLocationClient;
	private static final String TAG = "huang-ThirdAdService";
	Timer scheduleTimer = new Timer();
	Timer scheduleTimerUpdate;
	// 计时器，5分钟发一次消息
	TimerTask task = new TimerTask() {
		@Override
		public void run() {
			Message msg = Message.obtain();
			msg.what = TIMEDELAYTASK;
			handler.handleMessage(msg);
		}

	};
	// // 检测升级的计时器
	// TimerTask taskUpdate = new TimerTask() {
	// @Override
	// public void run() {
	// Message msg = Message.obtain();
	// msg.what = TIMEUPDATETASK;
	// handler.handleMessage(msg);
	// }
	//
	// };

	private BannerAdView bannerAdView;
	private SlotAdView slotAdView;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// re-get-pkgname
			case GETPACKAGENAME:
				if (reFilter == 1) {
					filterAndGoTo(mNativePackageName);
				}
				break;
			// self-update
			case NEEDTOUPDATE:

				bean = (AppSelfUpdateBean) msg.obj;
				Boolean isNeedToUpdate = SelfUpdataUtilsManager
						.appIsNeedUpate(bean.packageName, bean.versionCode,
								ThirdAdService.this);
				JLog.i(TAG, "checkNewVersion-onSuccess-isNeedToUpdate："
						+ isNeedToUpdate + "  bean.versionCode:"
						+ bean.versionCode + " bean.packageName:"
						+ bean.packageName);

				if (isNeedToUpdate) {
					// TODO 保存MD5值,校验
					if (bean != null && bean.isDownloadApk == 0) {
						// PreferencesUtils.putString(ThirdAdService.this,
						// Constants.APP_MD5, bean.apkMd5);

						SelfUpdataUtilsManager
								.downloadSelf(
										ThirdAdService.this,
										bean,
										new SelfUpdataUtilsManager.setOnDownloadedLinstener() {

											@Override
											public void downloadedLinstener(
													String path) {
												if (path != null) {
													MTAUtils.onUpdateTimes(appContext);
													installSilence(path);
													// } else {
													// 下载失败了重新执行下载逻辑会造成重复下载死循环
													// isCheackVersion = false;
												}
											}
										});
					}
				} else {
					// 不需要升级的逻辑
					File file = new File(Constants.APKFILEPATH);
					if (file.exists()) {
						file.delete();
					}
					// 保存MD5值
					// PreferencesUtils.putString(ThirdAdService.this,
					// Constants.APP_MD5, "");

					getContentResolver().delete(
							MediaStore.Files.getContentUri("external"),
							"_DATA=?", new String[] { Constants.APKFILEPATH });

				}
				break;
			// baidu lbs success
			case GETPACKAGENAME2:
				isGetAdress = true;
				break;
			// time due to getpkgname
			case TIMEDELAYTASK:
				timerDo = false;
				break;
			// screen-ad
			case SCREENPRECENT:
				// 广告二期，插屏广告
				onDisplayAds();
				break;
			// Rsplash
			case SHOWRSPLASH:
				// 广告二期，插屏广告

				break;
			default:
				break;
			}

		};
	};
	private ScreenListener mScreenListener;
	private Context appContext;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		appContext = getApplicationContext();
		if (intent == null) {
			return super.onStartCommand(intent, flags, startId);
		}

		String action = intent.getAction();
		if (action != null && action.equals(Constants.ACTION_PRIZE_ALARM)) {
			// 闹钟检测升级逻辑
			String baiduKey = PreferencesUtils.getString(appContext,
					Constants.AD_SOURCE_BAIDU);
			// 闹钟log打开
			Log.i(TAG, "Alarm-checkNewVersion! baiduKey:" + baiduKey);
			if (baiduKey != null && !baiduKey.equals(Constants.BD_INIT_SUCESS)) {
				// 百度初始化失败则重新初始化
				initBaidu();
			}
			checkNewVersion();
			if (Constants.JLog) {
				Toast.makeText(appContext, "Alarm-update-start!",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			// action==null等的情况
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mNativePackageName = extras.getString("packagename", null);
			}
			// MTA
			if (isOnCreateService) {
				isOnCreateService = false;
			} else {
				MTAUtils.onServiceCommand(appContext);
			}
			JLog.i(TAG, "------onStartCommand-mNativePackageName: "
					+ mNativePackageName + " isCheackVersion:"
					+ isCheackVersion);

			// mNativePackageName = intent.getStringExtra("packagename");
			// if (isGetAdress) {
			// 获得需要添加广告的包名，存入SP
			if (!timerDo) {
				timerDo = true;
				getTargetPackageName();
			}
			// }
			// if (isNeedUpdate) {
			if (!isCheackVersion) {
				isCheackVersion = true;
				checkNewVersion();
			}
			// }
			// 过滤包名
			filterAndGoTo(mNativePackageName);
			// return super.onStartCommand(intent, flags, startId);
		}
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "---ThirdAdService_onCreate!");
		// 创建文件路径
		FileUtils.initAppPath();
		initCrashFile();
		// 打开计时器
		scheduleTimer.schedule(task, 1000, TIMEDELAY);
		isOnCreateService = true;
		initMTA();
		initRequest();
		initBaidu();
		// 屏幕监听
		initScreenListener();
		MTAUtils.onServiceCreate(appContext);
	}

	private void initMTA() {
		// 初始化MTA统计
		StatConfig.setAutoExceptionCaught(true);
		StatService.trackCustomEvent(this, "onCreate", "");
		// 服务创建后检测自更新
		InitUtilsManager.initTengXunAccount(this);
	}

	private void initRequest() {
		XExtends.Ext.init(getApplication());
		XExtends.Ext.setDebug(false);
		OkHttpClient okHttpClient = new OkHttpClient.Builder()
				// .addInterceptor(new LoggerInterceptor("TAG"))
				.connectTimeout(10000L, TimeUnit.MILLISECONDS)
				.readTimeout(10000L, TimeUnit.MILLISECONDS).build();
		OkHttpUtils.initClient(okHttpClient);
	}

	private void initBaidu() {
		// 百度定位lbs
		mLocationClient = new LocationClient(getApplicationContext());
		mLBSListener = new MyLocationListener(ThirdAdService.this,
				mLocationClient, new GetAdressSuccess() {
					// 地址返回成功
					@Override
					public void getAdressSuccess() {

						Message msg = Message.obtain();
						msg.what = GETPACKAGENAME2;
						handler.handleMessage(msg);
					}
				});

		mLocationClient.registerLocationListener(mLBSListener); // 注册监听函数
		InitUtilsManager.initLocation(mLocationClient);
		// 定位
		Log.i(TAG, "---baidu LBS start location！");
		mLocationClient.start();
		// 百度推送
		PushManager
				.startWork(getApplicationContext(),
						PushConstants.LOGIN_TYPE_API_KEY,
						Constants.PUSH_API_KEY.trim());
	}

	private void initScreenListener() {
		mScreenListener = new ScreenListener(getApplicationContext());
		mScreenListener.begin(new ScreenStateListener() {

			@Override
			public void onUserPresent() {

				JLog.i(TAG, "onUserPresent");
				Message msg = Message.obtain();
				msg.what = SCREENPRECENT;
				handler.sendMessageDelayed(msg, 1000);
				// ClockManager.stopAlam(ThirdAdService.this);
			}

			@Override
			public void onScreenOn() {
				JLog.i(TAG, "onScreenOn");
				// scheduleTimerUpdate.cancel();
			}

			@Override
			public void onScreenOff() {
				JLog.i(TAG, "onScreenOff");
				// PreferencesUtils.putOutAdString(appContext,
				// Constants.OUTADINFO, Constants.TEST_AD_STRING);
				// 灭屏幕30min钟检测自升级
				// scheduleTimerUpdate.schedule(taskUpdate, 1000,
				// TIMEDELAYUPDATE);
				// scheduleTimerUpdate = new Timer();
				// scheduleTimerUpdate.schedule(taskUpdate, TIMEDELAYUPDATE);
				// ClockManager.startAlam(ThirdAdService.this);

			}

			@Override
			public void onScreenOffNoRLLevel() {
				JLog.i(TAG, "onScreenOffNoRLLevel");
			}

		});
		// 启动闹钟，检测升级
		ClockManager.startAlam(this);
	}

	/**
	 * 选择展示广告
	 */
	private void onDisplayAds() {

		String getJSONString = PreferencesUtils.getOutAdString(appContext,
				Constants.OUTADINFO);
		if (getJSONString == null) {
			Log.i(TAG, "onDisplayAds.getJSONString= " + getJSONString);
		} else {
			Log.i(TAG, "onDisplayAds.getJSONString!=null");
		}
		// 没网或则没数据，不展示广告
		if (getJSONString == null || ClientInfo.networkType == ClientInfo.NONET) {
			return;
		}
		OutAdInfo outAdInfo = null;
		String bannerUrl = null;
		AdDetailsInfo adDetailsInfo = null;
		try {
			outAdInfo = new Gson().fromJson(getJSONString, OutAdInfo.class);
			if (outAdInfo.adBanner != null && outAdInfo.adDetails != null) {
				bannerUrl = outAdInfo.adBanner;
				adDetailsInfo = outAdInfo.adDetails;
			} else {
				PreferencesUtils.clearOutAd(appContext);
				return;
			}
		} catch (Exception e) {
			PreferencesUtils.clearOutAd(appContext);
			e.printStackTrace();
			return;
		}
		int typeKey = outAdInfo.typeId;
		JLog.i(TAG, "typeKey:" + typeKey);
		switch (typeKey) {
		// 横幅广告
		case 1:
			if (slotAdView != null) {
				slotAdView.hide();
			}
			if (bannerAdView == null) {
				bannerAdView = new BannerAdView(ThirdAdService.this);
			}
			bannerAdView.show(bannerUrl, adDetailsInfo);
			// 清除记录移动到View里面执行
			// PreferencesUtils.clearOutAd(appContext);
			break;
		// 插屏广告
		case 2:
			if (bannerAdView != null) {
				bannerAdView.hide();
			}
			if (slotAdView == null) {
				slotAdView = new SlotAdView(ThirdAdService.this);
			}
			slotAdView.show(bannerUrl, adDetailsInfo);
			// 清除记录移动到View里面执行
			// PreferencesUtils.clearOutAd(appContext);
			break;
		default:
			break;
		}

	}

	private void filterAndGoTo(String packageName) {
		JLog.i(TAG, "packageName:" + packageName);

		String result = null;
		// if (packageName == null || ClientInfo.networkType ==
		// ClientInfo.NONET) {
		if (packageName == null) {
			return;
		}
		try {
			if (packageNameInfo == null) {
				// 从sp中获取需要广告的包名
				result = PreferencesUtils.getString(this,
						Constants.PACKAGENAMES, null);
			} else {
				result = packageNameInfo;
			}
		} catch (Exception e) {
			PreferencesUtils.clear(this);
			e.printStackTrace();

		}
		// JLog.i(TAG, "filterAppPcgName-result:" + result);
		// 为了防止两个数据为空
		if (result == null) {
			reFilter = 1;
			return;
		}

		if (netPackageNames == null || netPackageNames.size() <= 0) {
			try {
				JSONObject obj = new JSONObject(result);

				if (obj.getInt("code") == 0 && obj.getString("data") != null) {

					NetCommonInfo packageNamesInfo = new Gson().fromJson(
							result, NetCommonInfo.class);
					if (packageNamesInfo != null) {
						netPackageNames = packageNamesInfo.data.packageName;
						isNeedUpdate = packageNamesInfo.data.needUpdate;
					}
				} else {
					PreferencesUtils.clear(this);
					return;
				}
			} catch (JSONException e) {
				PreferencesUtils.clear(this);
				e.printStackTrace();
			}
		}

		JLog.i(TAG, "  nativePcgName:" + packageName + " netPcgNames:"
				+ netPackageNames.toString());
		// changed by huangchangguo jump to activity
		if (netPackageNames != null && netPackageNames.contains(packageName)) {
			Log.i(TAG, "startAdSplashActivity!");
			// changed by huangchangguo 防止过快启动，广告页被应用顶掉
			// try {
			// Thread.sleep(500);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// handler.sendMessageDelayed(msg, 500);
			times = 6;
			if (startSplashTimer != null) {
				startSplashTimer.cancel();
				startSplashTimer = null;
			}
			if (startSplashTask != null) {
				startSplashTask.cancel();
				startSplashTask = null;
			}
			startSplashTimer = new Timer();
			startSplashTask = new TimerTask() {
				@Override
				public void run() {
					try {
						JLog.i(TAG, "startSplashView-times:" + times);
						if (times > 0) {
							startSplashView();
						} else {
							if (startSplashTask != null) {
								startSplashTask.cancel();
							}
							if (startSplashTimer != null) {
								startSplashTimer.purge();
								startSplashTimer.cancel();
							}
						}
						if (times <= 0) {
							if (startSplashTask != null) {
								startSplashTask.cancel();
							}
							startSplashTask = null;
							if (startSplashTimer != null) {
								startSplashTimer.cancel();
							}
							startSplashTimer = null;
							JLog.i(TAG, "startSplashView-times <= 0:" + times);
						}
						times--;
					} catch (Exception e) {
						startSplashTask.scheduledExecutionTime();
						e.printStackTrace();
					}
				}
			};
			// 循环启动开屏广告
			if (startSplashTimer != null && startSplashTask != null) {
				startSplashTimer.scheduleAtFixedRate(startSplashTask, 100, 500);
			}
		}
		// 换成View启动（作废）
		// if (netPackageNames != null && netPackageNames.contains(packageName))
		// {
		// Log.i(TAG, "startAdSplashActivity!");
		// SplashAdView splash = new SplashAdView(ThirdAdService.this);
		// splash.show();
		// }

	}

	private int times;
	private Timer startSplashTimer = null;
	private TimerTask startSplashTask = null;

	// 跳转到开屏界面
	private void startSplashView() {
		Intent it = new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_LAUNCHER);
		it.setClassName("com.prize.prizeappoutad",
				"com.prize.prizeappoutad.activity.RSplashActivity");
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		// 防止重复实例的生成 X
		// | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		// 关闭自动回launcher X
		// | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
		// 重置Activity X
		// it.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
		// 启动无动画效果
		// | Intent.FLAG_ACTIVITY_NO_ANIMATION);
		this.startActivity(it);
	}

	private void getTargetPackageName() {

		String mFilterUrl = null;
		mFilterUrl = Constants.AD_FILTER_URL;

		RequestParams params = new RequestParams(mFilterUrl);
		reqFilterHandler = XExtends.http(this).post(params,
				new CommonCallback<String>() {

					@Override
					public void onSuccess(String result) {
						JLog.i(TAG, "getTargetPackageName-onSuccess:" + result);
						try {
							JSONObject obj = new JSONObject(result);
							if (obj.getInt("code") == 0
									&& obj.getString("data") != null) {

								packageNameInfo = result;

								NetCommonInfo packageNamesInfo = new Gson()
										.fromJson(result, NetCommonInfo.class);
								if (packageNamesInfo != null
										&& packageNamesInfo.data != null) {
									netPackageNames = null;
									netPackageNames = packageNamesInfo.data.packageName;
									isNeedUpdate = packageNamesInfo.data.needUpdate;
									PreferencesUtils.putString(
											ThirdAdService.this,
											Constants.PACKAGENAMES, result);
									Message msg = Message.obtain();
									msg.what = GETPACKAGENAME;
									handler.handleMessage(msg);
								}
							} else {
								// 计时关闭
								timerDo = false;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}

					@Override
					public void onError(Throwable ex, boolean isOnCallback) {
						JLog.i(TAG,
								"filterPackageName-onError:" + ex.toString());
						// 计时关闭
						timerDo = false;
					}

					@Override
					public void onCancelled(CancelledException cex) {
					}

					@Override
					public void onFinished() {
					}
				});

	}

	public void initCrashFile() {
		File crashFile = new File(FileUtils.CFG_APP_CRASH_FILE);
		if (crashFile.exists()) {
			// 发生异常了,啥都不初始化
			crashFile.delete();
		} else {
			if (hasInit) {
				return;
			}
		}
		hasInit = true;
	}

	/** 检测是否有新版本 */
	public void checkNewVersion() {

		String mUrl = null;
		mUrl = Constants.AD_SOURCE_URL;

		RequestParams params = new RequestParams(mUrl);

		reqHandler = XExtends.http().post(params, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				JLog.i(TAG, "checkNewVersion-onSuccess:" + result.toString());
				try {
					JSONObject obj = new JSONObject(result);
					int code = obj.getInt("code");
					if (code == 0) {

						JSONObject o = obj.getJSONObject("data");

						PackageNamesInfo info = new Gson().fromJson(
								obj.getString("data"), PackageNamesInfo.class);

						if (info == null || info.app == null)
							return;
						AppSelfUpdateBean bean = info.app;
						Message msg = Message.obtain();
						msg.obj = bean;
						msg.what = NEEDTOUPDATE;
						handler.handleMessage(msg);
					} else if (code == 1) {
						// 没有更新，不再检查
						// isCheackVersion = false;
					} else {
						// 数据得到失败
						isCheackVersion = false;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				JLog.i(TAG, "checkNewVersion-onError:" + ex.toString());
				isCheackVersion = false;
			}

			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});

	}

	private void installSilence(String ApkPath) {
		try {
			if (bean != null && bean.packageName != null && bean.apkMd5 != null) {

				File file = new File(ApkPath);
				if (file.exists()) {
					PackageInfo localPackageInfo = getPackageManager()
							.getPackageArchiveInfo(ApkPath,
									PackageManager.GET_ACTIVITIES);
					JLog.i(TAG,
							"installApk-Md5Check:"
									+ MD5Util.Md5Check(Constants.APKFILEPATH,
											bean.apkMd5));
					if (localPackageInfo == null) {
						JLog.i(TAG, "installApk-localPackageInfo:"
								+ localPackageInfo);
						return;
					}
					if ((localPackageInfo != null)
							&& (bean.packageName
									.equals(localPackageInfo.packageName))
							&& (this.bean.versionCode == localPackageInfo.versionCode)
							&& (MD5Util.Md5Check(ApkPath, bean.apkMd5))) {
						int returnCode = PackageUtils.install(this, ApkPath);
						// PackageUtils.installSilentFRSys(this, ApkPath.trim(),
						// ThirdAdService.this.getPackageName(),
						// new InstallResultCallBack() {
						//
						// @Override
						// public void back(int returnCode) {
						// // TODO Auto-generated method stub
						// // 如果安装成功，则删除安装包
						JLog.i(TAG, "OkHttpUtils-installApk-installTag:"
								+ returnCode + " thisPackageName:"
								+ ThirdAdService.this.getPackageName());
						//
						// }
						// });

						// if (returnCode == 1) {
						file.delete();
						// } else {
						// file.delete();
						// }

					} else {
						JLog.i(TAG, "installApk-Uninstall"
								+ "localPackageInfo.packageName:"
								+ localPackageInfo.packageName
								+ "localPackageInfo.versionCode:"
								+ localPackageInfo.versionCode);

						file.delete();
					}
				}
			}
		} catch (NotFoundException e) {
			JLog.i(TAG, "installSilence-catch:" + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		ClockManager.stopAlam(this);
		if (reqHandler != null)
			reqHandler.cancel();
		if (reqFilterHandler != null)
			reqFilterHandler.cancel();
		if (mScreenListener != null)
			mScreenListener.unregisterListener();
		mLocationClient.unRegisterLocationListener(mLBSListener);
		PushManager.stopWork(getApplicationContext());
		super.onDestroy();

	}

}
