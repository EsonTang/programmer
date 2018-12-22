package com.pr.scuritycenter.activity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.deepclean.DeepcleanManager;
import tmsdk.fg.module.deepclean.RubbishEntity;
import tmsdk.fg.module.deepclean.RubbishEntityManager;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.deepclean.ScanProcessListener;
import tmsdk.fg.module.deepclean.UpdateRubbishDataCallback;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.pr.scuritycenter.R;
import com.pr.scuritycenter.base.BaseActivity;
import com.pr.scuritycenter.utils.DeviceUtils;
import com.pr.scuritycenter.utils.SharedPreferencesUtil;
import com.pr.scuritycenter.view.CircleProgressView;

public class RubbishCleanActivity extends BaseActivity implements
		OnClickListener, UpdateRubbishDataCallback {

	protected static final int MSG_SDSCANNER_END = 0x19;

	protected static final int INDEX_APK = 0x01;

	protected static final int INDEX_SOFT_RUNTIMG_RUBBISH = 0x02;

	protected static final int INDEX_SOFTWARE_CACHE = 0x03;

	protected static final int INDEX_UNINSTALL_RETAIL = 0x04;

	protected static final int MSG_SCAN_PROCESSCHAGE = 0x07;

	/** 缓存头部进程 */
	private final int MSG_REFRESH_HEAD_PROGRESS = 0x05;

	protected static final int MSG_SDCLEAN_END = 0x06;

	@ViewInject(R.id.ib_return_last)
	private ImageView ib_return_last;

	@ViewInject(R.id.pb_scan)
	private CircleProgressView pb_scan;

	@ViewInject(R.id.garbage_scan_percent_txt)
	private TextView garbage_scan_percent_txt;

	@ViewInject(R.id.bt_garbage_clean)
	private Button bt_garbage_clean;

	@ViewInject(R.id.garbage_status_layout)
	private LinearLayout garbage_status_layout;

	@ViewInject(R.id.garbage_status_title)
	private TextView garbage_status_title;

	@ViewInject(R.id.garbage_status_size)
	private TextView garbage_status_size;

	@ViewInject(R.id.garbage_cache_size)
	private TextView garbage_cache_size;

	@ViewInject(R.id.garbage_system_size)
	private TextView garbage_system_size;

	@ViewInject(R.id.garbage_install_size)
	private TextView garbage_install_size;

	@ViewInject(R.id.garbage_uninstall_size)
	private TextView garbage_uninstall_size;

	@ViewInject(R.id.garbage_cache_process)
	private ProgressBar garbage_cache_process;

	@ViewInject(R.id.garbage_cache_result)
	private ImageView garbage_cache_result;

	@ViewInject(R.id.garbage_system_result)
	private ImageView garbage_system_result;

	@ViewInject(R.id.garbage_install_result)
	private ImageView garbage_install_result;

	@ViewInject(R.id.garbage_uninstall_result)
	private ImageView garbage_uninstall_result;
	
	@ViewInject(R.id.garbage_status_size_unit)
	private TextView garbage_status_size_unit;

	/** 深度清理 */
	private DeepcleanManager mDeepcleanManager;
	private int rubbishSize = 0;
	private int systemFileSize;
	private int uninstallFileSize;
	private int installFileSize;

	List<RubbishEntity> cacheFileList = new ArrayList<RubbishEntity>();
	List<RubbishEntity> systemFileList = new ArrayList<RubbishEntity>();
	List<RubbishEntity> installFileList = new ArrayList<RubbishEntity>();
	List<RubbishEntity> uninstallFileList = new ArrayList<RubbishEntity>();

	int cleanBtStatus = -1; // 0为暂停扫描，1为一键清理，2为重新扫描

	@Override
	protected void setContentView() {
		setContentView(R.layout.garbage_remove);
	}

	@Override
	protected void findViewById() {
		findViewById(R.id.garbage_scan_content).setPadding(0,
				DeviceUtils.getStatusBarHeight(this), 0, 0);
		ViewUtils.inject(this);
		mDeepcleanManager = ManagerCreatorF.getManager(DeepcleanManager.class);
		Log.d("scan", "mDeepcleanManager  = " + mDeepcleanManager);

		ScanProcessListener listener = initProcessListener();
		mDeepcleanManager.init(listener);
		mDeepcleanManager.startScan(RubbishType.SCAN_FLAG_ALL);
		// handler.sendEmptyMessage(INDEX_SOFTWARE_CACHE);

		bt_garbage_clean.setOnClickListener(this);
	}

	// 任务进程开启
	private ScanProcessListener initProcessListener() {
		return new ScanProcessListener() {
			private Message msg;
			private int cacheFileListSize = 0;

			@Override
			public void onScanStarted() {
				Log.d("scan", "begin scan rabbish.............");
				cleanBtStatus = 0;
			}

			@Override
			public void onScanProcessChange(int nowPercent) {
				Message msg = handler.obtainMessage(MSG_SCAN_PROCESSCHAGE);
				msg.arg1 = nowPercent;
				msg.sendToTarget();

			}

			@Override
			public void onScanFinished() {
				Log.d("scan", "finish scan rabbish ...........");
				handler.sendEmptyMessage(MSG_SDSCANNER_END);

				int _currentRubbishType = -1;
				List<RubbishEntity> _Rubbishes = mDeepcleanManager
						.getmRubbishEntityManager().getRubbishes();
				for (RubbishEntity aRubbish : _Rubbishes) {

					StringBuffer sbtips = new StringBuffer();
					if (_currentRubbishType != aRubbish.getRubbishType()) {
						sbtips.append("【垃圾类型】 ");
						switch (aRubbish.getRubbishType()) {
						case RubbishType.INDEX_APK:
							//aRubbish.setStatus(RubbishType.MODEL_TYPE_SELECTED);
							sbtips.append("——————————————————————【APK】——————————————————————\n");
							break;
						case RubbishType.INDEX_SOFT_RUNTIMG_RUBBISH:
							aRubbish.setStatus(RubbishType.MODEL_TYPE_SELECTED);
							sbtips.append("——————————————————————【系统垃圾】——————————————————————\n ");
							break;
						case RubbishType.INDEX_SOFTWARE_CACHE:
							aRubbish.setStatus(RubbishType.MODEL_TYPE_SELECTED);
							sbtips.append("——————————————————————【软件缓存】——————————————————————\n ");
							break;
						case RubbishType.INDEX_UNINSTALL_RETAIL:
							aRubbish.setStatus(RubbishType.MODEL_TYPE_SELECTED);
							sbtips.append("——————————————————————【卸载残余】 ——————————————————————\n ");
							break;
						}

						_currentRubbishType = aRubbish.getRubbishType();
					}

				}

				handler.sendEmptyMessage(MSG_SDSCANNER_END);
			}

			@Override
			public void onScanError(int error) {
				Log.d("scan", "onScanError ..............");
				if (DeepcleanManager.ERROR_CODE_SCAN_LOAD_ERROR == error)
					Log.e("onScanError", "load  do error!!   report in ui ...");
				else if (DeepcleanManager.ERROR_CODE_PROCESS_ERROR == error)
					Log.e("onScanError", "service error!!   report in ui ...");
				Log.v("bian", "onScanError : ");
			}

			@Override
			public void onScanCanceled() {

			}

			@Override
			public void onRubbishFound(RubbishEntity aRubbish) {
				Log.d("scan", "onRubbishFound...............");
				msg = Message.obtain();
				Log.v("scan", "found rabbish.......");
				switch (aRubbish.getRubbishType()) {
				case RubbishType.INDEX_SOFTWARE_CACHE: // 0
					cacheFileList.add(aRubbish);
					break;
				case RubbishType.INDEX_SOFT_RUNTIMG_RUBBISH: // 1
					systemFileList.add(aRubbish);
					break;
				case RubbishType.INDEX_APK: // 2
					installFileList.add(aRubbish);
					break;
				case RubbishType.INDEX_UNINSTALL_RETAIL: // 4
					uninstallFileList.add(aRubbish);
					break;
				}

			}

			@Override
			public void onCleanStart() {

				Log.i("scan", "begin clean..............");
			}

			@Override
			public void onCleanProcessChange(long currenCleanSize,
					int nowPercent) {
				Message msg = handler.obtainMessage(MSG_REFRESH_HEAD_PROGRESS);
				msg.obj = currenCleanSize;
				msg.arg1 = nowPercent;
				msg.sendToTarget();

			}

			@Override
			public void onCleanFinish() {

				// RubbishCleanActivity.this.finish();
				handler.sendEmptyMessage(MSG_SDCLEAN_END);

			}

			@Override
			public void onCleanError(int arg0) {
			}

			@Override
			public void onCleanCancel() {

			}

			/*
			 * @Override public void onScanProcessChange(int nowPercent, String
			 * scanPath) { // TODO Auto-generated method stub Message msg =
			 * handler.obtainMessage(MSG_SCAN_PROCESSCHAGE); msg.arg1 =
			 * nowPercent; msg.sendToTarget(); }
			 */
		};

	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// case INDEX_SOFTWARE_CACHE:
			// mDeepcleanManager.startScan(RubbishType.INDEX_SOFTWARE_CACHE);
			// garbage_cache_process.setVisibility(View.VISIBLE);
			// break;

			case MSG_SCAN_PROCESSCHAGE:
				int nowPercent = msg.arg1;
				pb_scan.setProgress(nowPercent);
				garbage_scan_percent_txt.setText(nowPercent + "%");
				break;
			case MSG_SDSCANNER_END:
				Log.d("clean",
						"allsize = " + mDeepcleanManager.getAllRubbishSize());
				Log.d("clean", "totalsize = " + cacheFileList.size()
						+ systemFileList.size() + installFileList.size()
						+ uninstallFileList.size());

				bt_garbage_clean.setText(getResources().getString(
						R.string.garbage_clean_fast_bt));

				cleanBtStatus = 1;
				pb_scan.setProgress(100);
				garbage_scan_percent_txt.setVisibility(View.GONE);
				garbage_status_layout.setVisibility(View.VISIBLE);
				garbage_status_title.setText(getResources().getString(
						R.string.garbage_status_title1));
				garbage_status_size.setText(transformShortType(
						getTotalRabbish(), true));

				// 缓冲文件 rubbishSize
				if (cacheFileList.size() > 0) {
					rubbishSize = 0;
					for (RubbishEntity rubbishEntity : cacheFileList) {
						rubbishSize += rubbishEntity.getSize();
					}
				}
				garbage_cache_size.setVisibility(View.VISIBLE);
				garbage_cache_size
						.setText(transformShortType(rubbishSize, true));

				systemFileSize = 0;
				if (systemFileList.size() > 0) {
					systemFileSize = 0;
					for (RubbishEntity rubbishEntity : systemFileList) {
						systemFileSize += rubbishEntity.getSize();
					}
				}
				garbage_system_size.setVisibility(View.VISIBLE);
				garbage_system_size.setText(transformShortType(systemFileSize,
						true));

				// 多余安装包

				installFileSize = 0;
				if (installFileList.size() > 0) {
					installFileSize = 0;
					for (RubbishEntity rubbishEntity : installFileList) {
						Log.v("bian", "haha"+rubbishEntity.getAppName());
						//暂时 安装的apk全部不删除
						rubbishEntity.setStatus(RubbishType.MODEL_TYPE_UNSELECTED);
						installFileSize += rubbishEntity.getSize();
					}
				}
				garbage_install_size.setVisibility(View.VISIBLE);
				garbage_install_size.setText(transformShortType(
						installFileSize, true));

				uninstallFileSize = 0;
				if (uninstallFileList.size() > 0) {
					uninstallFileSize = 0;
					for (RubbishEntity rubbishEntity : uninstallFileList) {
						uninstallFileSize += rubbishEntity.getSize();
					}
				}
				garbage_uninstall_size.setVisibility(View.VISIBLE);
				garbage_uninstall_size.setText(transformShortType(
						uninstallFileSize, true));
				break;

			case MSG_REFRESH_HEAD_PROGRESS:
				garbage_status_title.setText(getResources().getString(
						R.string.garbage_status_title2));
				garbage_status_size.setText(transformShortType(msg.arg1, true));
				break;
			case MSG_SDCLEAN_END:
				garbage_status_title.setText(getResources().getString(
						R.string.garbage_status_title2));
				bt_garbage_clean
						.setBackgroundResource(R.drawable.garbage_clean_start_seclector);
				bt_garbage_clean.setText(getResources().getString(
						R.string.garbage_clean_repeat_bt));
				bt_garbage_clean.setTextColor(Color.WHITE);
				garbage_status_size.setText(transformShortType(
						getTotalRabbish(), true));
				pb_scan.setProgress(0);
				cleanBtStatus = 2;
				garbage_cache_size.setVisibility(View.GONE);
				garbage_system_size.setVisibility(View.GONE);
				garbage_install_size.setVisibility(View.GONE);
				garbage_uninstall_size.setVisibility(View.GONE);
				garbage_cache_result.setVisibility(View.VISIBLE);
				garbage_system_result.setVisibility(View.VISIBLE);
				garbage_install_result.setVisibility(View.VISIBLE);
				garbage_uninstall_result.setVisibility(View.VISIBLE);
				break;
			}
		}
	};

	@Override
	public void updateFinished() {

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ib_return_last:
			this.finish();
			break;
		case R.id.bt_garbage_clean:
			if (cleanBtStatus == 0) {
				mDeepcleanManager.cancelScan();
			} else if (cleanBtStatus == 1) {
				_rubbishManager = mDeepcleanManager.getmRubbishEntityManager();
				_rubbish = _rubbishManager.getRubbishes();
				for (RubbishEntity _aRubbish : _rubbish) {
					if ((!TextUtils.isEmpty(_aRubbish.getPackageName()))
							&& _aRubbish.getPackageName().equals(
									"com.tencent.qq")) {
						// 将当前垃圾，设定为选择清除。 在后面的清理过程中，该垃圾会被删除。
						_aRubbish.setStatus(RubbishType.MODEL_TYPE_SELECTED);
					}

				}
				mDeepcleanManager.startClean();

				SharedPreferencesUtil.saveLong(getApplicationContext(),
						"RUNTIME", System.currentTimeMillis());

			} else if (cleanBtStatus == 2) {
				// SharedPreferencesUtil.saveLong(getApplicationContext(),
				// "CURENTTIME", System.currentTimeMillis());
				/*
				 * garbage_cache_result.setVisibility(View.GONE);
				 * garbage_system_result.setVisibility(View.GONE);
				 * garbage_install_result.setVisibility(View.GONE);
				 * garbage_uninstall_result.setVisibility(View.GONE);
				 * mDeepcleanManager.startScan(RubbishType.SCAN_FLAG_ALL);
				 */
				finish();
			}
			break;
		}
	}

	@Override
	protected void controll() {
		ib_return_last.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDeepcleanManager != null) {
			mDeepcleanManager.onDestory();
		}
	}

	private long getTotalRabbish() {
		return rubbishSize + systemFileSize + uninstallFileSize
				+ installFileSize;
	}

	public static final long ONE_KB = 1024L;
	public static final long ONE_MB = ONE_KB * 1024L;
	public static final long ONE_GB = ONE_MB * 1024L;
	public static final long ONE_TB = ONE_GB * 1024L;

	private long runtime;

	private RubbishEntityManager _rubbishManager;

	private List<RubbishEntity> _rubbish;

	public String transformShortType(long bytes, boolean isShortType) {
		long currenUnit = ONE_KB;
		int unitLevel = 0;
		boolean isNegative = false;
		if (bytes < 0) {
			isNegative = true;
			bytes = (-1) * bytes;
		}

		while ((bytes / currenUnit) > 0) {
			unitLevel++;
			currenUnit *= ONE_KB;
		}

		String result_text = null;
		double currenResult = 0;
		// int skipLevel = 1000;//如果大于等于1000就用更大一级单位显示
		switch (unitLevel) {
		case 0:
			currenResult = bytes;
			result_text = getFloatValue(currenResult, 1) + "B";
			break;
		case 1:
			currenResult = bytes / ONE_KB;
			result_text = getFloatValue(currenResult, 1) + "KB";
			break;
		case 2:
			currenResult = bytes * 1.0 / ONE_MB;
			result_text = getFloatValue(currenResult, 1) + "MB";
			break;
		case 3:
			currenResult = bytes * 1.0 / ONE_GB;
			result_text = getFloatValue(currenResult, 2) + "GB";
			break;
		case 4:
			result_text = getFloatValue(bytes * 1.0 / ONE_TB, 2) + "TB";
		}

		if (isNegative) {
			result_text = "-" + result_text;
		}
		return result_text;
	}

	private String getFloatValue(double oldValue, int decimalCount) {
		if (oldValue >= 1000) {// 大于四位整数 不出现小数部分
			decimalCount = 0;
		} else if (oldValue >= 100) {
			decimalCount = 1;
		}

		BigDecimal b = new BigDecimal(oldValue);
		try {
			if (decimalCount <= 0) {
				oldValue = b.setScale(0, BigDecimal.ROUND_DOWN).floatValue(); // ROUND_DOWN
																				// 表示舍弃末尾
			} else {
				oldValue = b.setScale(decimalCount, BigDecimal.ROUND_DOWN)
						.floatValue(); // ROUND_DOWN 表示舍弃末尾,decimalCount 位小数保留
			}
		} catch (ArithmeticException e) {
			Log.w("Unit.getFloatValue", e.getMessage());
		}
		String decimalStr = "";
		if (decimalCount <= 0) {
			decimalStr = "#";
		} else {
			for (int i = 0; i < decimalCount; i++) {
				decimalStr += "#";
			}
		}
		// decimalCount 位小数保留
		DecimalFormat format = new DecimalFormat("###." + decimalStr);
		return format.format(oldValue);
	}

}
