/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/
package com.prize.appcenter.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.AppSyncInfo;
import com.prize.appcenter.receiver.AppLoginAndRegistBroadcast;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.util.CountUpdateUtils;
import com.prize.appcenter.ui.util.CountUpdateUtils.LastUpoadTimeLinstener;
import com.prize.appcenter.ui.util.CountUpdateUtils.RestoreDataLinstener;
import com.prize.appcenter.ui.util.CountUpdateUtils.SyncUplaodLinstener;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.cloud.activity.LoginActivityNew;
import com.prize.cloud.util.CloudIntent;
import com.prize.cloud.util.Utils;
import com.tencent.stat.StatService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AppSyncAssistantActivity extends ActionBarNoTabActivity implements
		OnClickListener {

	private static final String TAG = "AppSyncAssistantActivity";
	private TextView mUpDateTime, mUpDateTimeDesc, mUpDateTimeShow;
	private TextView mBackupApp, mAppSynchroning;
	private TextView mRestoreApp;
	private static final int SYNCAPP = 1;
	private static final int RESTOREAPP = 2;
	protected static final int BROADCAST = 3;
	protected static final int RESTOREDATAS = 4;
	protected static final int RESTOREDATASFAILED = 5;
	private static final int SYNCAPPFAILED = 6;

	private String mUserId;
	private Context mContext;
	private ArrayList<AppsItemBean> mFilterRestoreApp;
	// private AppSyncAssistantActivity mActivity;

	private BroadcastReceiver mReceiver = new AppLoginAndRegistBroadcast() {
		public void onReceive(Context context, Intent intent) {
			if (intent != null
					&& !TextUtils.isEmpty(intent.getAction())
					&& CloudIntent.ACTION_PASSPORT_GET.equals(intent
							.getAction())) {

				Message msg = Message.obtain();
				msg.what = BROADCAST;
				handler.sendMessage(msg);
			}
		};
	};

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case BROADCAST:
				updateData();
				break;
			case SYNCAPP:
				/*显示更新时间 */
				isUsable(true);

				// 获取本地时间
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy年MM月dd日  HH:mm:ss");
				Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
				String str = formatter.format(curDate);

				mUpDateTimeShow.setText(mContext
						.getString(R.string.sync_assistant_last_time) + str);

				break;
			case SYNCAPPFAILED:
				// 延迟1秒，防止太快
				/* 设置一系列按钮和文字的状态 */
				isUsable(true);

				break;
			case RESTOREDATASFAILED:
				// 延迟1秒，防止太快
				/* 设置一系列按钮和文字的状态 */
				isUsable(true);

				break;
			case RESTOREDATAS:

				@SuppressWarnings("unchecked")
				ArrayList<AppsItemBean> restoreApps = (ArrayList<AppsItemBean>) msg.obj;

				if (restoreApps != null) {

					mFilterRestoreApp = CountUpdateUtils.filterRestoreApp(
							mContext, restoreApps);
					if (mFilterRestoreApp.size() > 0) {
						gotoRestoreAppDetail();

					} else {
						ToastUtils.showToast(mContext
								.getString(R.string.sync_restore_no_app));
					}

					isUsable(true);

				}

				break;

			default:
				break;
			}

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置启动加载页
		setNeedAddWaitingView(true);
		setContentView(R.layout.activity_app_sync_assistant);

		mContext = getApplicationContext();

		WindowMangerUtils.changeStatus(getWindow());

		initView();

		initListener();

		// initData();
		updateData();

		/*
		 * 监听账号登录或者注册信息
		 */
		initReceiver();

	}

	private void initReceiver() {
		// 注册移除监听
		IntentFilter filterRemove = new IntentFilter();
		filterRemove.addAction(CloudIntent.ACTION_PASSPORT_GET);
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
				filterRemove);

	}

	private void initListener() {
		// 备份到云端
		mBackupApp.setOnClickListener(this);
		// 恢复到本地
		mRestoreApp.setOnClickListener(this);
	}

	private void initView() {

		mAppSynchroning = (TextView) findViewById(R.id.sync_assistant_go_on_tv);
		mUpDateTimeDesc = (TextView) findViewById(R.id.sync_assistant_desc_tv);
		mUpDateTime = (TextView) findViewById(R.id.sync_assistant_last_update_time_tv);
		mUpDateTimeShow = (TextView) findViewById(R.id.sync_assistant_update_time_show_tv);

		mBackupApp = (TextView) findViewById(R.id.sync_assistant_backup_app_btn);
		mRestoreApp = (TextView) findViewById(R.id.sync_assistant_restore_app_btn);

	}


	/**
	 * 检查账号，请求更新
	 */
	private void updateData() {

		mUserId = CommonUtils.queryUserId();

		if (TextUtils.isEmpty(mUserId) || mUserId.contains("unknown")) {

			mUpDateTime.setVisibility(View.VISIBLE);
			mUpDateTimeDesc.setVisibility(View.VISIBLE);
			mUpDateTime.setText(mContext.getResources().getString(
					R.string.login_koobee));
			hideWaiting();
		} else {

			mUpDateTime.setVisibility(View.GONE);
			mUpDateTimeDesc.setVisibility(View.GONE);
			// 显示上次跟新时间
			requestData();

		}

	}

	private void requestData() {

		// 请求网络更新时间
		CountUpdateUtils.requestData(mUserId, new LastUpoadTimeLinstener() {

			@Override
			public void getLastUpoadTime(String lastUploadTime, int type) {
				JLog.d(TAG, "这里是发送更新时间的请求,-----lastUploadTime:---"
						+ lastUploadTime);

				/* 显示更新时间 */
				isUsable(true);

				if (lastUploadTime != null) {

					// desc： 上次备份：2016年7月21日20:23:32

					mUpDateTimeShow.setText(mContext
							.getString(R.string.sync_assistant_last_time)
							+ lastUploadTime);
				} else {
					mUpDateTimeShow.setVisibility(View.GONE);
					mUpDateTimeDesc.setVisibility(View.VISIBLE);
					mUpDateTimeDesc.setText(mContext
							.getString(R.string.sync_assistant_desc));

				}
				hideWaiting();

			}

		});

	}

	@Override
	public void onClick(View v) {

		boolean isfastClick = Utils.isFastDoubleClick();

		if (isfastClick) {
			return;
		}

		int clickId = v.getId();

		switch (clickId) {
		// 备份到云端
		case R.id.sync_assistant_backup_app_btn:
			// 1表示备份，2表示恢复APP

			queryWhetherLogin(SYNCAPP);

			break;
		// 恢复到本地
		case R.id.sync_assistant_restore_app_btn:

			queryWhetherLogin(RESTOREAPP);
			break;

		}

	}

	/**
	 * 查询是否登录帐号
	 * 
	 * @param status
	 * 
	 *            COPYAPP=1：表示备份 ; RESTOREAPP=2：表示恢复APP
	 * 
	 */
	private void queryWhetherLogin(int status) {
		if (TextUtils.isEmpty(mUserId) || mUserId.contains("unknown")) {
			// 跳转到登录页
			UIUtils.gotoActivity(LoginActivityNew.class,AppSyncAssistantActivity.this);
			this.overridePendingTransition(R.anim.slide_in_right,
					R.anim.fade_out);
		} else {

			String userId = mUserId;
			/* 设置一系列按钮和文字的状态 */
			isUsable(false);

			switch (status) {

			case SYNCAPP:
				mAppSynchroning.setText(mContext
						.getString(R.string.sync_assistant_go_on));
				syncApp(userId);

				break;
			case RESTOREAPP:
				mAppSynchroning.setText(mContext
						.getString(R.string.sync_assistant_restore));
				restoreApp(userId);
				break;
			}
		}
	}

	/**
	 * 同步应用
	 * 
	 * 1:扫描手机，获得三方应用 2：获得应用信息存入bean类 3：转换成json对象 4:上传
	 * 
	 */
	private void syncApp(String userId) {

		synchronized (AppSyncAssistantActivity.class) {

			// 1-2:扫描手机，获得三方应用 ;获得应用信息存入bean类
			ArrayList<AppSyncInfo> usersInfos = CountUpdateUtils
					.getLocalAppData(mContext);

			// 如果扫描出来没有数据，再扫描一次,等待子线程扫描完成，防止第一次返回过快，值为空
			if (usersInfos.size() <= 0) {
				SystemClock.sleep(300);
				usersInfos = CountUpdateUtils.getLocalAppData(mContext);
			}

			if (userId == null || usersInfos == null) {
				return;
			} else {
				// 3：转换成json对象
				Gson gson = new Gson();

				String localSyncAppJson = gson.toJson(usersInfos);
				/* 联网上传同步信息 */

				CountUpdateUtils.requestSyncupload(userId, localSyncAppJson,
						new SyncUplaodLinstener() {

							@Override
							public void getSyncUplaodData(int type) {

								if (type == Constants.SYNC_SUCESS) {

									ToastUtils.showToast("同步成功");

									Message msg = Message.obtain();
									msg.what = SYNCAPP;
									handler.sendMessage(msg);

								} else {

									Message msg = Message.obtain();
									msg.what = SYNCAPPFAILED;
									handler.sendMessageDelayed(msg, 1500);
								}

							}
						});
			}

			// syncDialogShow();
		}

	}

	/**
	 * btn是否可用，是否显示时间
	 */

	private void isUsable(Boolean isVisible) {

		if (isVisible) {

			// mUpDateTime.setVisibility(View.GONE);
			// mUpDateTimeDesc.setVisibility(View.GONE);
			mUpDateTimeShow.setVisibility(View.VISIBLE);
			mBackupApp.setEnabled(true);
			mRestoreApp.setEnabled(true);
			mAppSynchroning.setVisibility(View.GONE);

		} else {

			mUpDateTime.setVisibility(View.GONE);
			mUpDateTimeDesc.setVisibility(View.GONE);
			mUpDateTimeShow.setVisibility(View.GONE);
			mBackupApp.setEnabled(false);
			mRestoreApp.setEnabled(false);
			mAppSynchroning.setVisibility(View.VISIBLE);

		}

	}

	// /**
	// * 弹出提示框
	// */
	// private void syncDialogShow() {
	//
	// // 信息不一样，弹出提示框
	// CommonDialog syncDialog = new CommonDialog(mActivity);
	// syncDialog.setConfirmLoginOut(new ConfirmLoginOutCallBack() {
	//
	// @Override
	// public void confirmLoginOut() {
	// return;
	// // TODO
	//
	// }
	// });
	// syncDialog.show();
	//
	// syncDialog.setDialogContent(mActivity.getResources().getString(
	// R.string.sync_app_dialog_tips));
	// syncDialog.setDialogConfirm(mActivity.getResources().getString(
	// R.string.sync_app_dialog_go_on));
	// }

	/**
	 * 恢复应用到本地：1，联网请求返回上次备份的Apps；2，过滤本地现有的Apps；
	 */
	private void restoreApp(String userId) {

		if (userId == null) {
			return;
		}

		/*
		 * final ProDialog proDialog = new ProDialog(mContext,
		 * ProgressDialog.THEME_HOLO_LIGHT, mContext.getResources()
		 * .getString(R.string.sync_progressing));
		 */
		// 1，返回上次备份的Apps；
		CountUpdateUtils.requestSyncdownload(userId,
				new RestoreDataLinstener() {

					@Override
					public void getRestoreData(
							ArrayList<AppsItemBean> restoreData, int type) {

						if (type == Constants.SYNC_SUCESS) {

							Message msg = Message.obtain();
							msg.obj = restoreData;
							msg.what = RESTOREDATAS;

							handler.sendMessageDelayed(msg, 1000);
						} else {

							Message msg = Message.obtain();
							msg.obj = restoreData;
							msg.what = RESTOREDATASFAILED;

							handler.sendMessageDelayed(msg, 1500);
						}

						/*
						 * if (proDialog != null && proDialog.isShowing()) {
						 * proDialog.dismiss(); }
						 */

					}
				});

		// proDialog.show();

	}

	/**
	 * 跳转到恢复APP页面
	 */
	private void gotoRestoreAppDetail() {

		Intent intent = new Intent(this, AppSyncAssistantrestoreActivity.class);
		intent.putExtra("userId", mUserId);
		intent.putParcelableArrayListExtra("bean", mFilterRestoreApp);
		startActivity(intent);

	}

	// -----------------------------------------------通用类，无需修改--------------------------------------------------//

	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
	}

	@Override
	protected void initActionBar() {

		enableSlideLayout(false);
		findViewById(R.id.action_bar_tab).setVisibility(View.VISIBLE);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				switch (id) {
				// 返回
				case R.id.back_IBtn:
					finish();
					break;
				case R.id.bar_title:
					onBackPressed();
					break;
				case R.id.action_bar_search:
					UIUtils.goSearchActivity(AppSyncAssistantActivity.this);
					break;
				case R.id.action_go_downQueen:
					UIUtils.gotoActivity(AppDownLoadQueenActivity.class,
							AppSyncAssistantActivity.this);
					break;
				}
			}
		};

		// 增加点击返回的灵敏度
		findViewById(R.id.action_go_downQueen).setOnClickListener(
				onClickListener);
		findViewById(R.id.back_IBtn).setOnClickListener(onClickListener);
		findViewById(R.id.action_bar_search)
				.setOnClickListener(onClickListener);
		TextView title = (TextView) findViewById(R.id.bar_title);
		title.setText(R.string.app_sync_assistant);
	}

	@Override
	public String getActivityName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onDestroy() {
		if (mReceiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					mReceiver);
			mReceiver = null;
		}
		super.onDestroy();
	}
}
