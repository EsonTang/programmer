package com.koobee.koobeecenter;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.ex.HttpException;
import org.xutils.http.RequestParams;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.koobee.koobeecenter.base.BaseActivity;
import com.koobee.koobeecenter.utils.ToastUtils;
import com.koobee.koobeecenter.utils.UpdateDataUtils;
import com.koobee.koobeecenter02.R;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.PreferencesUtils;
import com.prize.custmerxutils.XExtends;

public class MainActivity extends BaseActivity implements View.OnClickListener {
	private RelativeLayout mFeedBackRealtive;
	private RelativeLayout system_update_Rlyt;
	private RelativeLayout after_servicee_Rlyt;
	private RelativeLayout tmall_Rlyt;
	private RelativeLayout koobee_Rlyt;
	private Cancelable reqHandler;
	private AppsItemBean bean;
	private DownloadManager downloadManager;
	private Handler mHander;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			// | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			// window.setStatusBarColor(Color.TRANSPARENT);
			// window.setNavigationBarColor(Color.TRANSPARENT);
		}
		setContentView(R.layout.activity_main);
		findViewById();
		setListener();
		checkNewVersion();
	}

	public void checkNewVersion() {
//	    String mUrl = "http://192.168.1.148:8080/apis/upgrade/kbcenter";
	    String mUrl = "http://api.szprize.cn/apis/upgrade/kbcenter";
	    
		RequestParams params = new RequestParams(mUrl);
		JLog.i("hu", "Mainactivity murl=="+mUrl);

		reqHandler = XExtends.http().post(params, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				JLog.i("hu", "onSuccess");
				try {
					JSONObject obj = new JSONObject(result);
					JLog.i("hu", "onSuccess result=="+result);
					JLog.i("hu", "onSuccess obj=="+obj.toString());
					JLog.i("hu", "onSuccess code=="+obj.getInt("code"));
					if (obj.getInt("code") == 0) {
						JSONObject o = new JSONObject(obj.getString("data"));
						bean = new Gson().fromJson(o.getString("app"),
								AppsItemBean.class);
						JLog.i("hu", "onSuccess bean=="+bean.toString()+"bean.packageName=="+bean.packageName+"bean.versionCode=="+bean.versionCode);
						if (appIsNeedUpate(bean.packageName,
								bean.versionCode)) {
							JLog.i("hu", "onSuccess appIsNeedUpate true");
							PreferencesUtils.putString(MainActivity.this,
									Constants.APP_MD5, bean.apkMd5);
							if (downloadManager == null) {
								downloadManager = (DownloadManager) MainActivity.this
										.getSystemService(Context.DOWNLOAD_SERVICE);
							}
							if ((new File(Constants.APKFILEPATH)).exists()) {
								queryDownloadStatus();
								JLog.i("hu", "onSuccess queryDownloadStatus");
							} else {
								if (ClientInfo.networkType != ClientInfo.NONET) {
									JLog.i("hu", "onSuccess networkType=="+ClientInfo.networkType);
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
//							if(new File(Constants.APKINSTALL).exists()){
//							    int installFlag = PackageUtils.install(MainApplication.curContext,Constants.APKINSTALL);
//							    JLog.i("hu", "main---installFlag--"+installFlag);
//						     }
							JLog.i("hu", "onSuccess  --else");
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
					JLog.i("hu", "onSuccess  JSONException=="+e.getMessage());
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				// TODO Auto-generated method stub
                  if (ex instanceof HttpException) { // 网络错误  
                      HttpException httpEx = (HttpException) ex;  
                      int responseCode = httpEx.getCode();  
                      String responseMsg = httpEx.getMessage();  
                      String errorResult = httpEx.getResult();  
                      JLog.i("hu", "responseCode"+responseCode+"---"+"responseMsg"+responseMsg+"---"+"errorResult"+errorResult+"---");
                  } else { // 其他错误  
                	  JLog.i("hu", "onError"+ex+"---"+isOnCallback);
                	  ex.toString();
                  }  
				
			}

			@Override
			public void onCancelled(CancelledException cex) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
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
//			JLog.i("hu", "appIsNeedUpate  isNewMethod()=="+isNewMethod());
			JLog.i("hu", "appIsNeedUpate  applicationInfo.versionCode=="+applicationInfo.versionCode+"versionCode=="+versionCode);
//			if (!isNewMethod()) {
			if (applicationInfo!=null) {
				return applicationInfo.versionCode < versionCode;
			}
			PackageInfo packageInfo = BaseApplication.curContext
					.getPackageManager().getPackageArchiveInfo(
							applicationInfo.publicSourceDir, 0);
			JLog.i("hu", "appIsNeedUpate  packageInfo.versionCode=="+packageInfo.versionCode);
			if (packageInfo != null) {
				return packageInfo.versionCode < versionCode;
			}
		} catch (NameNotFoundException e) {
			return false;
		}
		return false;
	}
	
//	public static boolean isNewMethod(){
//		return "1".equals(SystemProperties.get("ro.prize_app_update_appcenter", "0"));
//	}
	
	private void queryDownloadStatus() {
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(PreferencesUtils.getLong(this,
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
				// 完成
//				displayDialog();
				JLog.i("hu", "下载完成");
				break;
			case DownloadManager.STATUS_FAILED:
				// 清除已下载的内容，重新下载
				downloadManager.remove(PreferencesUtils.getLong(this,
						Constants.KEY_NAME_DOWNLOAD_ID));
				PreferencesUtils.putLong(this, Constants.KEY_NAME_DOWNLOAD_ID,
						-1);
				break;
			}
		}
	}
	
	@Override
	protected void findViewById() {
		mFeedBackRealtive = (RelativeLayout) findViewById(R.id.feedback_relative);
		system_update_Rlyt = (RelativeLayout) findViewById(R.id.system_update_Rlyt);
		after_servicee_Rlyt = (RelativeLayout) findViewById(R.id.after_servicee_Rlyt);
		koobee_Rlyt = (RelativeLayout) findViewById(R.id.koobee_Rlyt);
		tmall_Rlyt = (RelativeLayout) findViewById(R.id.tmall_Rlyt);

	}

	@Override
	protected void setListener() {
		mFeedBackRealtive.setOnClickListener(this);
		system_update_Rlyt.setOnClickListener(this);
		tmall_Rlyt.setOnClickListener(this);
		koobee_Rlyt.setOnClickListener(this);
		after_servicee_Rlyt.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		
		switch (v.getId()) {
		case R.id.feedback_relative:
			intent = new Intent(MainActivity.this, AdviceActivity.class);
			// intent = new Intent(MainActivity.this, FeedBackActivity.class);
			break;
		case R.id.system_update_Rlyt:
			// 调用系统升级服务
			intent = new Intent();
			intent.setComponent(new ComponentName("com.qiku.android.ota",
			"com.qiku.android.ota.ui.MainActivity"));
			if (getPackageManager().resolveActivity(intent, 0) == null) {
				ToastUtils.showOnceToast(getApplicationContext(), "系统中没有此功能");
				return;
				// 说明系统中不存在这个activity
			}
			break;
		case R.id.after_servicee_Rlyt:// AftersalesActivity.class));
			intent = new Intent(MainActivity.this, AftersalesActivity.class);
			break;
		case R.id.tmall_Rlyt:

			intent = new Intent();
			intent.setClassName("com.freeme.operationManual",
					"com.freeme.operationManual.ui.MainOperationManualActivity");

			// intent = new Intent("android.intent.action.VIEW",
			// Uri.parse(getString(R.string.koobee_tmall_website)));
			break;
		case R.id.koobee_Rlyt:
			intent = new Intent("android.intent.action.VIEW",
					Uri.parse(getString(R.string.koobee_official_website)));
			break;
		}
		if (intent == null) {
			return;
		}
		if (getPackageManager().resolveActivity(intent, 0) == null) {
			return;
		}

		MainActivity.this.startActivity(intent);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (reqHandler != null) {
			reqHandler.cancel();
		}
		if (mHander != null) {
			mHander.removeCallbacksAndMessages(null);
		}
		super.onDestroy();
	}
    
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		checkNewVersion();
		super.onResume();
	}
	@Override
	protected void init() {

	}

}
