package com.pr.scuritycenter.activity;

import java.util.Calendar;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.CheckResult;
import tmsdk.common.module.update.ICheckListener;
import tmsdk.common.module.update.IUpdateListener;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateInfo;
import tmsdk.common.module.update.UpdateManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.pr.scuritycenter.R;
import com.pr.scuritycenter.utils.SharedPreferencesUtil;
import com.pr.scuritycenter.utils.StateBarUtils;

/**
 * 更新模块DEMO，涉及所有功能模块的更新逻辑
 * 
 * @author boyliang
 */
public final class UpdateActivity extends Activity {
	private TextView mContextShower;
	private Button mCheckButton;
	private Button mUpdateButton;

	private UpdateManager mUpdateManager;
	private CheckResult mCheckResults;
	private ProgressDialog mProgressDialog;
	private RelativeLayout rl_update_result;
	private ScrollView sl_update_result;

	private TextView tv_update_state;
	private TextView tv_data_version;
	private String data_version; // 数据版本号

	int updateBtStatus = -1;// 0为检查更新，1为正在检查更新，2更新数据，3为更新完成,

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 更新模块窗口
		StateBarUtils.initSateBar(this);
		setContentView(R.layout.update_activity);
		ViewUtils.inject(this);
		// findViewById(R.id.update_title_content).setPadding(0,DeviceUtils.getStatusBarHeight(this),
		// 0, 0);
		// update_return.setOnClickListener(this);

		mUpdateManager = ManagerCreatorC.getManager(UpdateManager.class);
		// 更新模块显示
		mContextShower = (TextView) findViewById(R.id.content_shower);
		// 检查按钮
		mCheckButton = (Button) findViewById(R.id.check_btn);
		// 更新按钮
		mUpdateButton = (Button) findViewById(R.id.update_btn);

		rl_update_result = (RelativeLayout) findViewById(R.id.rl_update_result);
		sl_update_result = (ScrollView) findViewById(R.id.sl_update_result);

		tv_update_state = (TextView) findViewById(R.id.tv_update_state);
		tv_data_version = (TextView) findViewById(R.id.tv_data_version);

		// mProgressDialog = new ProgressDialog(this);
		mProgressDialog = new ProgressDialog(this,
				com.android.internal.R.style.Theme_Material_Light_Dialog_Alert);
		// mProgressDialog.setCancelable(false);
		mProgressDialog.setCanceledOnTouchOutside(false);

		// 进去就检查更新
		rl_update_result.setVisibility(View.GONE);
		sl_update_result.setVisibility(View.VISIBLE);

		mProgressDialog.setMessage(getResources().getString(R.string.update_dialog_message));
		mProgressDialog.setIndeterminate(true);
		showDialog(0);
		mContextShower.setText(getResources().getString(R.string.update_dialog_message));
		new Thread(new Runnable() {
			@Override
			public void run() {
				/*long flags = UpdateConfig.UPDATA_FLAG_NUM_MARK// 号码标记模块
						| UpdateConfig.UPDATE_FLAG_BLACKLIST_PROCESS // 优化模块的加速功能
						| UpdateConfig.UPDATE_FLAG_NOTKILLLIST_KILL_PROCESSES // 优化模块的加速功能
						| UpdateConfig.UPDATE_FLAG_SYSTEM_SCAN_CONFIG// 病毒扫描模块
						| UpdateConfig.UPDATE_FLAG_ADB_DES_LIST// 病毒扫描模块
						| UpdateConfig.UPDATE_FLAG_VIRUS_BASE// 病毒扫描模块
						| UpdateConfig.UPDATE_FLAG_STEAL_ACCOUNT_LIST// 病毒扫描模块
						| UpdateConfig.UPDATE_FLAG_PAY_LIST// 病毒扫描模块
						| UpdateConfig.UPDATE_FLAG_TRAFFIC_MONITOR_CONFIG// 流量监控
						| UpdateConfig.UPDATE_FLAG_LOCATION// 归属地模块
						| UpdateConfig.UPDATE_FLAG_SMS_CHECKER;// 智能拦截
*/				long flags =  UpdateConfig.UPDATE_FLAG_SYSTEM_SCAN_CONFIG// 病毒扫描模块
						| UpdateConfig.UPDATE_FLAG_ADB_DES_LIST// 病毒扫描模块
						| UpdateConfig.UPDATE_FLAG_VIRUS_BASE// 病毒扫描模块
						| UpdateConfig.UPDATE_FLAG_STEAL_ACCOUNT_LIST// 病毒扫描模块
						| UpdateConfig.UPDATE_FLAG_PAY_LIST;// 病毒扫描模块
				mUpdateManager.check(flags, new ICheckListener() {
					@Override
					// 检查网络，如果网络失败则回调
					public void onCheckEvent(int arg0) {
						// 检查网络状态，如果网络失败则不能更新
						Message msg = Message.obtain(mHandler,
								MSG_NETWORK_ERROR);
						msg.arg1 = arg0;
						msg.sendToTarget();
					}

					@Override
					public void onCheckStarted() {
						Log.v("bian", "started");
					}

					@Override
					public void onCheckCanceled() {
						Log.v("bian", "canceled");
					}

					@Override
					public void onCheckFinished(CheckResult result) {
						Log.v("bian", "finished");
						mCheckResults = result;

						// 修改数据显示时机，在数据check出后，再发送通知进行显示
						mHandler.sendEmptyMessage(MSG_HIDE_CHECK_PROGRESS);
					}
				});
				// mHandler.sendEmptyMessage(MSG_HIDE_CHECK_PROGRESS);
			}
		}).start();

		// 检查更新
		mCheckButton.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				if (updateBtStatus == 1) { // 检查更新
					mProgressDialog.setMessage(getResources().getString(R.string.update_dialog_message));
					mProgressDialog.setIndeterminate(true);
					showDialog(0);
					mContextShower.setText(getResources().getString(R.string.update_dialog_message));
					new Thread(new Runnable() {
						@Override
						public void run() {
							long flags =  UpdateConfig.UPDATE_FLAG_SYSTEM_SCAN_CONFIG// 病毒扫描模块
									| UpdateConfig.UPDATE_FLAG_ADB_DES_LIST// 病毒扫描模块
									| UpdateConfig.UPDATE_FLAG_VIRUS_BASE// 病毒扫描模块
									| UpdateConfig.UPDATE_FLAG_STEAL_ACCOUNT_LIST// 病毒扫描模块
									| UpdateConfig.UPDATE_FLAG_PAY_LIST;// 病毒扫描模块
							mUpdateManager.check(flags, new ICheckListener() {
								@Override
								// 检查网络，如果网络失败则回调
								public void onCheckEvent(int arg0) {
									// 检查网络状态，如果网络失败则不能更新
									Message msg = Message.obtain(mHandler,
											MSG_NETWORK_ERROR);
									msg.arg1 = arg0;
									msg.sendToTarget();
								}

								@Override
								public void onCheckStarted() {
								}

								@Override
								public void onCheckCanceled() {
								}

								@Override
								public void onCheckFinished(CheckResult result) {
									mCheckResults = result;
									// 修改数据显示时机，在数据check出后，再发送通知进行显示
									mHandler.sendEmptyMessage(MSG_HIDE_CHECK_PROGRESS);
								}
							});
						}
					}).start();
				} else if (updateBtStatus == 2) { // 更新数据

					if (mCheckResults != null
							&& mCheckResults.mUpdateInfoList != null
							&& mCheckResults.mUpdateInfoList.size() > 0) {
						mProgressDialog.setMessage(getResources().getString(R.string.update_dialog_message));
						mProgressDialog.setIndeterminate(false);
						showDialog(0);
						new Thread(new Runnable() {
							@Override
							public void run() {
								if (null == mCheckResults)
									return;
								mUpdateManager.update(
										mCheckResults.mUpdateInfoList,
										new IUpdateListener() {
											@Override
											// 更新
											public void onProgressChanged(
													UpdateInfo arg0, int arg1) {
												Message msg = Message.obtain(
														mHandler,
														MSG_UPDATE_PROGRESS);
												msg.obj = arg0;
												msg.arg1 = arg1;
												msg.sendToTarget();
											}

											@Override
											// 更新中检查网络
											public void onUpdateEvent(
													UpdateInfo arg0, int arg1) {
												Message msg = Message.obtain(
														mHandler,
														MSG_NETWORK_ERROR);
												msg.arg1 = arg1;
												msg.sendToTarget();
											}

											@Override
											public void onUpdateFinished() {
												Message msg = Message.obtain(
														mHandler,
														MSG_UPDATE_FINISHED);
												msg.sendToTarget();
											}

											@Override
											public void onUpdateStarted() {

											}

											public void onUpdateCanceled() {

											}
										});
								// 发出通知更新
								mHandler.sendEmptyMessage(MSG_HIDE_UPDATE_PROGRESS);
							}
						}).start();
					}

				} else if (updateBtStatus == 3) {
					finish();
				}

			}
		});
	}

	private static final int MSG_HIDE_CHECK_PROGRESS = 0;// 发出检查通知
	private static final int MSG_HIDE_UPDATE_PROGRESS = 1;// 发出更新通知
	private static final int MSG_UPDATE_PROGRESS = 2;// 更新
	private static final int MSG_NETWORK_ERROR = 3;// 网络失败
	private static final int MSG_UPDATE_FINISHED = 4;//

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_HIDE_CHECK_PROGRESS:
				mCheckButton.setText(R.string.update_data);
				updateBtStatus = 2;
				mProgressDialog.dismiss();
				//dismissDialog(0);
				showCheckResult();
				break;
			case MSG_HIDE_UPDATE_PROGRESS:
				mProgressDialog.dismiss();
			//	dismissDialog(0);
				break;
			case MSG_UPDATE_PROGRESS:
				UpdateInfo updateInfo = (UpdateInfo) msg.obj;
				mProgressDialog.setProgress(msg.arg1);
				mProgressDialog.setMessage(updateInfo.fileName);
				addUpdateResult(updateInfo.fileName);
				mProgressDialog.setMessage("正在更新...");
				
				break;
			case MSG_NETWORK_ERROR:
				data_version = SharedPreferencesUtil
						.getString(getApplicationContext(), "DATAVERSION",
								"< 2015-8-10 >");
				updateBtStatus = 0;
				tv_update_state.setText("当前数据版本");
				rl_update_result.setVisibility(View.VISIBLE);
				sl_update_result.setVisibility(View.GONE);
				mCheckButton.setText(R.string.update_check);
				tv_data_version.setText(data_version);
				Toast.makeText(UpdateActivity.this, "网络错误，请检查网络的连通性！",
						Toast.LENGTH_LONG).show();
				break;
			case MSG_UPDATE_FINISHED:
				saveCurrentTime();
				updateBtStatus = 3;
				tv_update_state.setText("当前数据版本");
				rl_update_result.setVisibility(View.VISIBLE);
				sl_update_result.setVisibility(View.GONE);
				tv_data_version.setText(getCuurentTime());
				mCheckButton.setText(R.string.update_checked);
				mCheckButton
						.setBackgroundResource(R.drawable.update_bt_completed_bg);
				break;
			}
		}
	};

	@SuppressLint("Override")
	protected Dialog onCreateDialog(int id, Bundle args) {
		return mProgressDialog;
	}

	// 检查结果显示
	private void showCheckResult() {
		StringBuilder sb = new StringBuilder();

		if (null == mCheckResults || null == mCheckResults.mUpdateInfoList) {
			data_version = SharedPreferencesUtil.getString(
					getApplicationContext(), "DATAVERSION", "< 2015-11-10 >");
			tv_update_state.setText(getResources().getString(R.string.update_result));
			tv_data_version.setText(data_version);
			rl_update_result.setVisibility(View.VISIBLE);
			sl_update_result.setVisibility(View.GONE);
			mCheckButton.setText(R.string.update_checked);
			updateBtStatus = 3;
			return;
		}
		/*for (UpdateInfo info : mCheckResults.mUpdateInfoList) {
			sb.append("check result: ").append("\n");
			sb.append("need to update: ").append(info.fileName).append("\n");
		}
		mContextShower.setText(sb.toString());*/
		mContextShower.setText("当前有新版本:  "+getCuurentTime());
	}

	// 更新结果显示
	private void addUpdateResult(String fileName) {
		StringBuilder sb = new StringBuilder(mContextShower.getText());
		sb.append("update file -> ").append(fileName).append("\n");
		//mContextShower.setText(sb.toString());
		mContextShower.setText("正在更新，请稍后...");
	}

	// 把当前时间作为数据版本号
	private void saveCurrentTime() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DATE);

		SharedPreferencesUtil.saveString(getApplicationContext(),
				"DATAVERSION", "< " + year + "-" + month + "-" + date + " >");
	}
	
	private String getCuurentTime(){
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DATE);
		
		return "< " + year + "-" + month + "-" + date + " >";
		
	}

}
