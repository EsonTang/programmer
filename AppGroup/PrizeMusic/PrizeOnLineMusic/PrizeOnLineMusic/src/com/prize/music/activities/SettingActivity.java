package com.prize.music.activities;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.cloud.util.Utils;
import com.prize.custmerxutils.XExtends;
import com.prize.music.IApolloService;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.PreferencesUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.dialog.UpdateSelfDialog;
import com.prize.music.ui.fragments.CleanDataFragment;
import com.prize.music.ui.widgets.PrizeHorizontalTimeView;
import com.prize.music.ui.widgets.PrizeHorizontalTimeView.ViewChangeListener;
import com.prize.music.R;

/**
 * 设置界面
 * 
 * @author pengyang
 *
 */
public class SettingActivity extends FragmentActivity implements
		OnClickListener, ViewChangeListener ,ServiceConnection{

	private ImageButton action_bar_back;
	private TextView action_bar_title;
	private TextView cache_size;
	private TextView clock_time_tv;

	/** 当前版本号 */
	private TextView current_version_iv;
	/** 边听边下载开关 **/
	private Switch download_set_sw;
	/** WiFi自动下载开关 **/
	private Switch wify_set_sw;
	/** 流量下载放歌弹框开关 **/
	private Switch download_hint_set_sw;
	/** 定时停止开关 **/
	private Switch time_stop_sw;
	private RelativeLayout clock_rlt;
	/** 意见反馈 **/
	private RelativeLayout feedback_RL;
	/** 清除缓存 **/
	private RelativeLayout clear_data_RL;
	/** WIFI **/
	private RelativeLayout wify_set_RL;
	/** 版本更新 **/
	private RelativeLayout update_version_RL;
	/** 对话框 */
	private CleanDataFragment df;
	/** 定时器 */
	private PrizeHorizontalTimeView timeView;
	private Handler mHandler;
	TextView login_Btn;
	private boolean isNeedTip = false;
	private boolean isRequestOk = false;
	private CleanDataFragment sureDialog;
	TextView wify_set_tv;
	
	private ServiceToken mToken;
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		StateBarUtils.initStateBar(this,
				getResources().getColor(R.color.statusbar_color));
		setContentView(R.layout.activity_setting);
		StateBarUtils.changeStatus(getWindow());
		initView();
		setListener();
		init();
		mHandler = new Handler(Looper.getMainLooper(), callback);
		mHandler.post(mBackgroundRunnable);// 将线程post到Handler中
		// startTime();
		checkNewVersion();
	}

	private void initView() {
		login_Btn = (TextView) findViewById(R.id.login_Btn);
		action_bar_back = (ImageButton) findViewById(R.id.action_bar_back);
		action_bar_title = (TextView) findViewById(R.id.action_bar_title);
		wify_set_tv = (TextView) findViewById(R.id.wify_set_tv);

		download_set_sw = (Switch) findViewById(R.id.download_set_sw);
		wify_set_sw = (Switch) findViewById(R.id.wify_set_sw);
		download_hint_set_sw = (Switch) findViewById(R.id.download_hint_set_sw);
		time_stop_sw = (Switch) findViewById(R.id.time_stop_sw);

		clock_rlt = (RelativeLayout) findViewById(R.id.clock_rlt);
		feedback_RL = (RelativeLayout) findViewById(R.id.feedback_RL);
		clear_data_RL = (RelativeLayout) findViewById(R.id.clear_data_RL);
		wify_set_RL = (RelativeLayout) findViewById(R.id.wify_set_RL);
		update_version_RL = (RelativeLayout) findViewById(R.id.update_version_RL);
		current_version_iv = (TextView) findViewById(R.id.current_version_iv);
		current_version_iv.setText(this.getString(R.string.version_num_is,
				ClientInfo.getInstance().appVersionCode));

		cache_size = (TextView) findViewById(R.id.cache_size);
		clock_time_tv = (TextView) findViewById(R.id.clock_time_tv);

		df = CleanDataFragment.newInstance(this.getString(R.string.clear_data),
				this.getString(R.string.is_clear_cache_now),
				this.getString(R.string.clear),
				this.getString(R.string.cancel), mDeletePromptListener);

		timeView = (PrizeHorizontalTimeView) findViewById(R.id.timeview);
		timeView.setViewChangeListener(this);
		if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
			login_Btn.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void onViewChange(int time) {
		// TODO 滑动过程
		// minute = time;
		// second = 0;
	}

	@Override
	public void viewChanged(int time) {
		// TODO 滑动完成
		minute = time;
		second = 0;

	}

	private void setListener() {
		action_bar_back.setOnClickListener(this);
		action_bar_title.setOnClickListener(this);
		feedback_RL.setOnClickListener(this);
		clear_data_RL.setOnClickListener(this);
		update_version_RL.setOnClickListener(this);
		login_Btn.setOnClickListener(this);

		download_set_sw.setOnCheckedChangeListener(settingClickListener);
		wify_set_sw.setOnCheckedChangeListener(settingClickListener);
		download_hint_set_sw.setOnCheckedChangeListener(settingClickListener);
		time_stop_sw.setOnCheckedChangeListener(settingClickListener);
	}

	private void init() {
		// 边听边下载 默认开启
		String listenAutoDownload = DataStoreUtils
				.readLocalInfo(DataStoreUtils.SWITCH_LISTEN_DOWNLOAD);
		if (DataStoreUtils.CHECK_OFF.equals(listenAutoDownload)) {
			download_set_sw.setChecked(false);
			wify_set_sw.setEnabled(false);
			wify_set_tv.setEnabled(false);
		} else {
			download_set_sw.setChecked(true);
			wify_set_sw.setEnabled(true);
			wify_set_tv.setEnabled(true);
		}

		// wifi自动下载 默认关闭
		String wifiDownload = DataStoreUtils
				.readLocalInfo(DataStoreUtils.SWITCH_WIFI_DOWNLOAD);
		if (!TextUtils.isEmpty(wifiDownload)
				&& DataStoreUtils.CHECK_OFF.equals(wifiDownload)) {
			wify_set_sw.setChecked(false);
		} else {
			wify_set_sw.setChecked(true);
		}
		// 弹框提示 默认开启
		String hintShow = DataStoreUtils
				.readLocalInfo(DataStoreUtils.SWITCH_HINT_SHOW);
		if (DataStoreUtils.CHECK_OFF.equals(hintShow)) {
			download_hint_set_sw.setChecked(false);
		} else {
			download_hint_set_sw.setChecked(true);
		}
		// 定时关闭 默认关闭
		String timeToStop = DataStoreUtils
				.readLocalInfo(DataStoreUtils.SWITCH_TIME_STOP);
		if (!TextUtils.isEmpty(timeToStop)
				&& DataStoreUtils.CHECK_ON.equals(wifiDownload)) {
			time_stop_sw.setChecked(true);
			clock_rlt.setVisibility(View.VISIBLE);
			clock_time_tv.setVisibility(View.VISIBLE);
			timeView.setVisibility(View.VISIBLE);
			minute = timeView.getMTime();
			second = 0;
			startTime();
		} else {
			time_stop_sw.setChecked(false);
			clock_rlt.setVisibility(View.GONE);
			clock_time_tv.setVisibility(View.INVISIBLE);
			timeView.setVisibility(View.INVISIBLE);
			stopTimer();
		}

	}

	private OnCheckedChangeListener settingClickListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			int viewId = buttonView.getId();
			switch (viewId) {
			case R.id.download_set_sw: // 边听边下载
				ListenAutoDownload(isChecked);
				break;
			case R.id.wify_set_sw:// WiFi自动下载
				WifiAutoDownload(isChecked);
				break;
			case R.id.download_hint_set_sw:// 流量下播放歌弹框提示
				TrifficPlayHint(isChecked);
				break;
			case R.id.time_stop_sw:// 定时停止
				TimeToStop(isChecked);
				break;
			}
		}
	};

	/**
	 * 删除提示对话框
	 */
	private View.OnClickListener mDeletePromptListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			new AsyncLoader_GuessInfo().execute();
		}
	};
	/**
	 * 退出账号提示对话框
	 */
	private View.OnClickListener logoutPromptListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
//			df.dismissAllowingStateLoss();
			Utils.logout(SettingActivity.this);
			login_Btn.setVisibility(View.GONE);
		}
	};

	protected void TimeToStop(boolean isChecked) {
		if (!isChecked) {// 默认OFF
			DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_TIME_STOP,
					DataStoreUtils.CHECK_OFF);
			clock_rlt.setVisibility(View.GONE);
			clock_time_tv.setVisibility(View.INVISIBLE);
			timeView.setVisibility(View.INVISIBLE);
			stopTimer();
		} else {
			DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_TIME_STOP,
					DataStoreUtils.CHECK_ON);
			clock_rlt.setVisibility(View.VISIBLE);
			clock_time_tv.setVisibility(View.VISIBLE);
			timeView.setVisibility(View.VISIBLE);
			minute = timeView.getMTime();
			second = 0;
			startTime();
		}
	}

	protected void TrifficPlayHint(boolean isChecked) {
		if (!isChecked) {// 默认ON，故判断OFF
			DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_HINT_SHOW,
					DataStoreUtils.CHECK_OFF);
		} else {
			DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_HINT_SHOW,
					DataStoreUtils.CHECK_ON);
		}
	}

	protected void WifiAutoDownload(boolean isChecked) {
		if (!isChecked) {// 默认OFF
			DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_WIFI_DOWNLOAD,
					DataStoreUtils.CHECK_OFF);
			WiFiDownloadSongs(true);
		} else {
			DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_WIFI_DOWNLOAD,
					DataStoreUtils.CHECK_ON);
			WiFiDownloadSongs(false);
		}
	}

	protected void ListenAutoDownload(boolean isChecked) {
		if (!isChecked) {// 默认ON，故判断OFF
			DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_LISTEN_DOWNLOAD,
					DataStoreUtils.CHECK_OFF);
			download_set_sw.setChecked(false);
			wify_set_sw.setEnabled(false);
			wify_set_tv.setEnabled(false);
//			DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_WIFI_DOWNLOAD,
//					DataStoreUtils.CHECK_OFF);
		} else {
			download_set_sw.setChecked(true);
			wify_set_sw.setEnabled(true);
			wify_set_tv.setEnabled(true);
			DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_LISTEN_DOWNLOAD,
					DataStoreUtils.CHECK_ON);
		}
	}

	/**
	 * true wifi下自动下载
	 * 
	 * @param isOnlyWifi
	 */
	private void WiFiDownloadSongs(boolean isOnlyWifi) {
		int netType = ClientInfo.networkType;

		if (isOnlyWifi && (netType != ClientInfo.WIFI)) {
			// download

		} else if (!isOnlyWifi
				&& ((netType == ClientInfo.MOBILE_2G) || (netType == ClientInfo.MOBILE_3G))) {
			// do nothings
		}
	}

	class AsyncLoader_GuessInfo extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			File appCacheDir = StorageUtils
					.getCacheDirectory(getApplicationContext());
			if (!appCacheDir.exists()) {
				return false;
			}
			return FileUtils.recursionDeleteFile(appCacheDir);
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				changeState();
				// ToastUtils.showToast(R.string.clear_data_success);
			}
		}
	}

	Callback callback = new Callback() {

		@Override
		public boolean handleMessage(Message msg) {

			if (msg != null && msg.obj != null && msg.what == 1) {
				String size = (String) msg.obj.toString();
				cache_size.setText(size);
				return true;
			} else {
				changeState();
			}
			return false;
		}

	};
	Runnable mBackgroundRunnable = new Runnable() {

		@Override
		public void run() {

			File appCacheDir = StorageUtils
					.getCacheDirectory(getApplicationContext());
			double size = 0;
			if (appCacheDir.exists()) {
				size = FileUtils.getDirSize(appCacheDir);
			}
			Message msg = Message.obtain();
			if (size == 0.0) {
				msg.what = 0;
				mHandler.sendMessage(msg);
				return;
			}
			StringBuilder content = new StringBuilder("(");
			content.append(String.format("%1$.2f", size) + "M)");

			msg.obj = content;
			msg.what = 1;
			mHandler.sendMessage(msg);
		}
	};

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.login_Btn:
			if(sureDialog==null||!sureDialog.isAdded()){
				sureDialog = CleanDataFragment.newInstance(this.getString(R.string.alert_button_yes),
						this.getString(R.string.is_logoutnow),
						this.getString(R.string.logout),
						this.getString(R.string.cancel), logoutPromptListener);
				sureDialog.show(getSupportFragmentManager(), "logout");
			}
			
			break;
		case R.id.action_bar_back:
			onBackPressed();
			break;
		case R.id.action_bar_title:
			onBackPressed();
			break;
		case R.id.feedback_RL: // 意见反馈
			Intent intent = new Intent(this, FeedbackExActivity.class);
			startActivity(intent);
			break;
		case R.id.clear_data_RL: // 清理缓存
			if (df == null || !df.isAdded()) {
				df = CleanDataFragment.newInstance(null, null, null, null,
						mDeletePromptListener);
			}
			if (df != null && !df.isAdded()) {
				df.show(getSupportFragmentManager(), "cleanDialog");
			}
			break;
		case R.id.update_version_RL:
			isNeedTip = true;
			if (isRequestOk && mAppsItemBean != null) {
				if (AppManagerCenter.appIsNeedUpate(mAppsItemBean.packageName,
						mAppsItemBean.versionCode)) {
					displayDialog();
				} else {
					ToastUtils.showToast(R.string.already_newest_version);
				}
				return;
			}
			checkNewVersion();
			break;
		}
	}

	protected void changeState() {
		cache_size.setVisibility(View.GONE);
		/*
		 * ((TextView) findViewById(R.id.clear_data_tv))
		 * .setTextColor(getResources() .getColor(R.color.text_color_737373));
		 */
		clear_data_RL.setEnabled(false);
		clear_data_RL.setClickable(false);
	}

	private void stopTimer() {

		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
		clock_time_tv.setText("00:00");
	}

	public void startTime() {
		timerTask = new TimerTask() {

			@Override
			public void run() {
				Message msg = new Message();
				msg.what = 0;
				handler.sendMessage(msg);
			}
		};
		timer = new Timer();
		timer.schedule(timerTask, 0, 1000);
	}

	static int minute = -1;
	static int second = -1;
	Timer timer;
	TimerTask timerTask;
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (minute == 0) {
				if (second == 0) {
					clock_time_tv.setText("0" + minute + ":0" + second);
					if (timer != null) {
						timer.cancel();
						timer = null;
					}
					if (timerTask != null) {
						timerTask = null;
					}
				} else {
					second--;
					if (second >= 10) {
						clock_time_tv.setText("0" + minute + ":" + second);
					} else {
						clock_time_tv.setText("0" + minute + ":0" + second);
					}
				}
			} else {
				if (second == 0) {
					second = 59;
					minute--;
					if (minute >= 10) {
						clock_time_tv.setText(minute + ":" + second);
					} else {
						clock_time_tv.setText("0" + minute + ":" + second);
					}
				} else {
					second--;
					if (second >= 10) {
						if (minute >= 10) {
							clock_time_tv.setText(minute + ":" + second);
						} else {
							clock_time_tv.setText("0" + minute + ":" + second);
						}
					} else {
						if (minute >= 10) {
							clock_time_tv.setText(minute + ":0" + second);
						} else {
							clock_time_tv.setText("0" + minute + ":0" + second);
						}
					}
				}
			}
		};
	};

	@Override
	protected void onDestroy() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (timerTask != null) {
			timerTask = null;
		}
		minute = -1;
		second = -1;
		super.onDestroy();
		if (reqHandler != null) {
			reqHandler.cancel();
		}
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
	}

	private Cancelable reqHandler;

	// private DownloadManager downloadManager;
	// private Handler mHander;

	private void checkNewVersion() {
		String mUrl = Constants.GIS_URL + "/upgrade/check";
		RequestParams params = new RequestParams(mUrl);

		reqHandler = XExtends.http().post(params, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {

				try {
					JSONObject obj = new JSONObject(result);
					if (obj.getInt("code") == 0) {
						isRequestOk = true;
						JSONObject o = new JSONObject(obj.getString("data"));
						mAppsItemBean = new Gson().fromJson(o.getString("app"),
								AppsItemBean.class);
						if (AppManagerCenter.appIsNeedUpate(mAppsItemBean.packageName,
								mAppsItemBean.versionCode)) {
							PreferencesUtils.putString(SettingActivity.this,
									Constants.APP_MD5, mAppsItemBean.apkMd5);
							current_version_iv.setText(getString(
									R.string.new_version, mAppsItemBean.versionName));
							if (isNeedTip && SettingActivity.this.isResumed()) {
								displayDialog();
							}

						} else {
							if (isNeedTip) {
								ToastUtils
										.showToast(R.string.already_newest_version);
							}
						}

					} else {
						try {
							String msg = obj.getString("msg");
							if (!TextUtils.isEmpty(msg)) {
								ToastUtils.showToast(msg);
							}

						} catch (Exception e) {
						}
					}
				} catch (JSONException e) {

					e.printStackTrace();

				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

				// TODO Auto-generated method stub

			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});

	}

	private AppsItemBean mAppsItemBean;
	private UpdateSelfDialog mUpdateSelfDialog;

	private void displayDialog() {
		if (mAppsItemBean == null)
			return;
		if (mUpdateSelfDialog == null) {
			mUpdateSelfDialog = new UpdateSelfDialog(SettingActivity.this,
					R.style.add_dialog,
					ClientInfo.getInstance().appVersionCode, getResources()
							.getString(R.string.for_new_version,
									mAppsItemBean.versionName),
					mAppsItemBean.updateInfo);
			mUpdateSelfDialog.setBean(mAppsItemBean);
		}
		if (mUpdateSelfDialog != null && !mUpdateSelfDialog.isShowing()) {
			mUpdateSelfDialog.show();

		}
	}

	@Override
	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		MusicUtils.mService = IApolloService.Stub.asInterface(arg1);
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		MusicUtils.mService = null;
	}
	
	@Override
	protected void onStart() {
		mToken = MusicUtils.bindToService(this, this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onStop();
	}
	

}