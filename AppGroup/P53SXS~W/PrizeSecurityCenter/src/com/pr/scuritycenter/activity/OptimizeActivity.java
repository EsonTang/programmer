package com.pr.scuritycenter.activity;

import java.util.ArrayList;
import java.util.List;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.optimize.IAutoBootHelper;
import tmsdk.common.module.optimize.IMemoryHelper;
import tmsdk.common.module.optimize.OptimizeManager;
import tmsdk.common.module.optimize.OptimizeManager.RunRootCmd;
import tmsdk.common.module.optimize.RunningProcessEntity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.pr.scuritycenter.R;

public final class OptimizeActivity extends Activity implements OnClickListener {
	private OptimizeManager mOptimizeManager;
	 private IAutoBootHelper mAutoBootHelper;
	private IMemoryHelper mMemoryHelper;

	private TextView mInfo;
	private Button mClearAllCacheBtn;
	private Button mSpeedBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.optimize_activity);
		mOptimizeManager = ManagerCreatorC.getManager(OptimizeManager.class);
		mAutoBootHelper = mOptimizeManager.getAutoBootHelper();
		mMemoryHelper = mOptimizeManager.getMemoryHelper();

		mInfo = (TextView) findViewById(R.id.device_info);
		mClearAllCacheBtn = (Button) findViewById(R.id.clear_all_cache);
		mSpeedBtn = (Button) findViewById(R.id.call_speed);
		mSpeedBtn.setOnClickListener(this);

		meminfoDis();

		// 关闭某些软件的开机启动
		mAutoBootHelper.setAutoBootEnable("com.tencent.mytt", true);

		// 清空应用的缓存文件夹cache
		boolean r = mMemoryHelper.clearAllCacheData();

		// 获取内存信息
		tmsdk.common.module.optimize.IMemoryHelper.MemoryInfo[] infos = mMemoryHelper
				.getRamSize(new String[] { "com.tencent.mytt" });

		mClearAllCacheBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ProgressDialogDisplay("请稍等", "正在清除缓存");
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						new Thread() {
							public void run() {
								mMemoryHelper.clearAllCacheData();
								ProgressDialogCancel();
							};
						}.start();
					}
				});

			}
		});

	}

	private void meminfoDis() {
		StringBuffer sb = new StringBuffer();
		sb.append("总内存： " + mMemoryHelper.getTotalMemery() + "KB\n");
		sb.append("可用内存: " + mMemoryHelper.getFreeMemery() + "KB\n");
		myHandler.obtainMessage(MSG_MEM_DIS, sb.toString()).sendToTarget();
		// mInfo.setText(sb.toString());
	}

	/**
	 * 示例：手机加速接口使用--获取所有运行应用
	 */
	private void getAllRunningProcess() {

		new Thread() {
			public void run() {
				meminfoDis();
				List<RunningProcessEntity> list = mOptimizeManager
						.getRunningProcessList(true, true);
				Log.v("demo", "手机加速接口使用--获取所有运行应用");

				if (list != null && list.size() > 0) {
					ArrayList<String> Pkglist = new ArrayList<String>();
					for (RunningProcessEntity entity : list) {
						Log.v("demo", "RunningappPkg:"
								+ entity.mProcessEntity.mPackageName);
						if (!OptimizeActivity.this.getApplication()
								.getPackageName()
								.equals(entity.mProcessEntity.mPackageName)) {
							Pkglist.add(entity.mProcessEntity.mPackageName);
						}
					}
					/**
					 * 用户设置的白名单，需要在这里从应用列表中去除！ // Pkglist.remove(object);
					 */
					if (Pkglist.size() > 0) {
						killRunningTask(Pkglist);
					}
				}
				meminfoDis();
				myHandler.sendEmptyMessage(MSG_SPEED_END);

				myHandler.sendEmptyMessageDelayed(MSG_DELAY_MEM_DIS, 2000);// 内存反应慢需等待
			}
		}.start();
	}

	/**
	 * 示例：手机加速接口使用--kill运行应用
	 * 
	 * @param arg0
	 */
	private void killRunningTask(List<String> arg0) {
		Log.v("demo", "手机加速接口使用--kill运行应用size = " + arg0.size());
		mOptimizeManager.killTasks(arg0, true, null);
		// mOptimizeManager.killTasks(arg0, true, nHanCmd);
	}

	private RunRootCmd nHanCmd = new RunRootCmd() {
		@Override
		public void OnRunRootCmd(String cmd) {
			Log.v("demo", "activity OnRunRootCmd");
		}

	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.call_speed:
			mSpeedBtn.setEnabled(false);
			ProgressDialogDisplay("请等待", "正在进行一键加速...");
			getAllRunningProcess();
			break;
		}

	}

	/**
	 * 异步处理用handler
	 */
	private final static int MSG_SPEED_END = 101;
	private final static int MSG_MEM_DIS = 102;
	private final static int MSG_DELAY_MEM_DIS = 103;
	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SPEED_END:
				mSpeedBtn.setEnabled(true);
				ProgressDialogCancel();
				break;
			case MSG_MEM_DIS:
				mInfo.setText((String) msg.obj);
				break;
			case MSG_DELAY_MEM_DIS:
				meminfoDis();
				break;
			}
		}

	};

	ProgressDialog mDialogProgress = null;
	Object mDialogSyncObj = new Object();

	/**
	 * 对话框显示
	 * 
	 * @param title
	 * @param message
	 */
	void ProgressDialogDisplay(String title, String message) {
		synchronized (mDialogSyncObj) {
			if (mDialogProgress == null) {
				mDialogProgress = new ProgressDialog(OptimizeActivity.this);
				mDialogProgress.setCancelable(false);
			}
			if (mDialogProgress.isShowing()) {
				return;
			}
		}
		mDialogProgress.setTitle(title);
		mDialogProgress.setMessage(message);

		mDialogProgress.show();
	}

	/**
	 * 对话框cancel
	 */
	void ProgressDialogCancel() {
		if (mDialogProgress != null) {
			if (mDialogProgress.isShowing()) {
				mDialogProgress.cancel();
			}
		}
	}
}
