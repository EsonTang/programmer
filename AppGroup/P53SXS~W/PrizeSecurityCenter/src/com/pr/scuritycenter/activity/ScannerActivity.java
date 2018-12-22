package com.pr.scuritycenter.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tmsdk.common.module.qscanner.QScanAdPluginEntity;
import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.qscanner.QScanListenerV2;
import tmsdk.fg.module.qscanner.QScannerManagerV2;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pr.scuritycenter.R;
import com.pr.scuritycenter.adapter.HideSoftListAdapter;
import com.pr.scuritycenter.base.BaseActivity;
import com.pr.scuritycenter.utils.ListViewUtils;
import com.pr.scuritycenter.view.CustomDialog;

public class ScannerActivity extends BaseActivity implements OnClickListener {
	private LinearLayout virusScanContent;
	private LinearLayout hideSoftLayout;
	private ListView hideSoftList;
	private HideSoftListAdapter hideSoftListAdapter;
	private boolean isHideSoftListVisible = false;
	private QScannerManagerV2 mQScannerMananger;// 病毒扫描功能接口
	private Thread mScanThread;// 扫描线程对象

	int mMulwareCount = 0;// 病毒数
	int mCount = 0;// 软件数
	long mTimeValue = 0;// 扫描开始时间
	private Button sdcard_scan;
	private ProgressBar progress_bar;
	private TextView progress_bar_value;
	private Button sdcard_scan_stop;
	private ImageButton return_main;
	private LinearLayout viruses_scan_security;
	private LinearLayout viruses_scan_content;
	private ImageView malicious_software;
	private ImageView progress_malicious;
	private ImageView hidden_software;
	private ImageView progress_software;
	private ImageView hack_risk;
	private ImageView progress_hack;
	private ImageView ad_plugin;
	private ImageView progress_plugin;
	private Animation animation;
	private ImageButton iv_update;
	private AnimationDrawable background;
	private Intent intent;
	private ImageView iv_progress;
	private ImageView iv_result_des;

	private static final int MSG_ENABLE_ALLBTN = 101;
	private static final int MSG_RESET_PAUSE = 102;
	private static final int ONSCANFINISHED = 103;
	int btStatus = -1; // 1 开始扫描 ， 2 扫描结束
	private Handler mHandle2 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				// mScanResultStateView.setText((String) msg.obj);
				break;
			case MSG_ENABLE_ALLBTN:

				// setHandleBtnEnable(true);
				progress_bar_value.setVisibility(View.VISIBLE);
				viruses_scan_security.setVisibility(View.GONE);

				malicious_software.setVisibility(View.GONE);
				hidden_software.setVisibility(View.GONE);
				hack_risk.setVisibility(View.GONE);
				ad_plugin.setVisibility(View.GONE);

				startAinimation();
				break;
			case MSG_RESET_PAUSE:
				// mPauseScan.setText("暂停扫描");
				break;
			}
		}

		private void startAinimation() {
			progress_malicious.setVisibility(View.VISIBLE);
			progress_software.setVisibility(View.VISIBLE);
			progress_hack.setVisibility(View.VISIBLE);
			progress_plugin.setVisibility(View.VISIBLE);
			animation = new RotateAnimation(0f, 360f,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(1000);
			animation.setRepeatCount(-1);
			progress_malicious.setAnimation(animation);
			progress_software.setAnimation(animation);
			progress_hack.setAnimation(animation);
			progress_plugin.setAnimation(animation);

			progress_malicious.startAnimation(animation);
			progress_software.startAnimation(animation);
			progress_hack.startAnimation(animation);
			progress_plugin.startAnimation(animation);
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
					alphaAnimation.setDuration(1000);
					alphaAnimation.setFillAfter(true);

					ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0,
							1, Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					scaleAnimation.setDuration(1000);
					scaleAnimation.setFillAfter(true);

					AnimationSet animationSet = new AnimationSet(false);
					animationSet.addAnimation(alphaAnimation);
					animationSet.addAnimation(scaleAnimation);

					iv_result_des.startAnimation(animationSet);

				}
			});

			iv_progress.setBackgroundResource(R.drawable.scanner_background);
			background = (AnimationDrawable) iv_progress.getBackground();
			background.start();
		}

	};

	private Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.arg1 > 0) {
				Log.v("demo", "progress_bar" + msg.obj);
				progress_bar.setProgress(msg.arg1);
				progress_bar_value.setText(msg.arg1 + "%");

				if (msg.arg1 == 100) {
					stopAnimation();

					btStatus = 2;
					iv_progress.setVisibility(View.GONE);
					sdcard_scan.setText(R.string.viruse_scan_completed);
					sdcard_scan.setTextColor(Color.WHITE);
					sdcard_scan.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
					// sdcard_scan.setBackgroundDrawable(R.drawable.viruse_scan_pause_bt_completed);
					sdcard_scan
							.setBackgroundResource(R.drawable.viruse_scan_pause_bt_completed_selector);
					sdcard_scan.setVisibility(View.VISIBLE);
					sdcard_scan_stop.setVisibility(View.GONE);
					viruses_scan_security.setVisibility(View.VISIBLE);
					viruses_scan_content.setVisibility(View.GONE);
					progress_bar_value.setVisibility(View.GONE);

					malicious_software.setVisibility(View.VISIBLE);
					hidden_software.setVisibility(View.VISIBLE);
					hack_risk.setVisibility(View.VISIBLE);
					ad_plugin.setVisibility(View.VISIBLE);
				}
			} else if (msg.what == ONSCANFINISHED) {
				/*
				 * stopAnimation();
				 * sdcard_scan.setText(R.string.viruse_scan_again);
				 * sdcard_scan.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				 * sdcard_scan.setVisibility(View.VISIBLE);
				 * sdcard_scan_stop.setVisibility(View.GONE);
				 * viruses_scan_security.setVisibility(View.VISIBLE);
				 * viruses_scan_content.setVisibility(View.GONE);
				 * progress_bar_value.setVisibility(View.GONE);
				 * 
				 * malicious_software.setVisibility(View.VISIBLE);
				 * hidden_software.setVisibility(View.VISIBLE);
				 * hack_risk.setVisibility(View.VISIBLE);
				 * ad_plugin.setVisibility(View.VISIBLE);
				 */
			}
		}

		private void stopAnimation() {
			if (animation != null) {
				animation.cancel();
			}
			if (background != null) {
				background.stop();
				background = null;
			}
			progress_malicious.setVisibility(View.GONE);
			progress_software.setVisibility(View.GONE);
			progress_hack.setVisibility(View.GONE);
			progress_plugin.setVisibility(View.GONE);
		}
	};

	@Override
	protected void setContentView() {
		setContentView(R.layout.qscanner_activity);
	}

	@Override
	protected void findViewById() {

		hideSoftLayout = (LinearLayout) findViewById(R.id.hide_soft_layout);
		hideSoftLayout.setOnClickListener(layoutClickListener);

		return_main = (ImageButton) findViewById(R.id.return_main);
		hideSoftList = (ListView) findViewById(R.id.hide_soft_list);
		sdcard_scan = (Button) findViewById(R.id.sdcard_scan);
		sdcard_scan_stop = (Button) findViewById(R.id.sdcard_scan_stop);
		progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
		progress_bar_value = (TextView) findViewById(R.id.progress_bar_value);
		viruses_scan_security = (LinearLayout) findViewById(R.id.viruses_scan_security);
		viruses_scan_content = (LinearLayout) findViewById(R.id.viruses_scan_content);
		malicious_software = (ImageView) findViewById(R.id.malicious_software);
		progress_malicious = (ImageView) findViewById(R.id.progress_malicious);
		hidden_software = (ImageView) findViewById(R.id.hidden_software);
		progress_software = (ImageView) findViewById(R.id.progress_software);
		hack_risk = (ImageView) findViewById(R.id.hack_risk);
		progress_hack = (ImageView) findViewById(R.id.progress_hack);
		ad_plugin = (ImageView) findViewById(R.id.ad_plugin);
		progress_plugin = (ImageView) findViewById(R.id.progress_plugin);
		iv_update = (ImageButton) findViewById(R.id.iv_update);
		iv_progress = (ImageView) findViewById(R.id.iv_progress);
		iv_result_des = (ImageView) findViewById(R.id.iv_result_des);
	}

	@Override
	protected void controll() {
		initView();

	}

	private void initView() {
		hideSoftListAdapter = new HideSoftListAdapter(this);
		hideSoftList.setAdapter(hideSoftListAdapter);
		ListViewUtils.setHeight(hideSoftList, hideSoftList.getCount());

		btStatus = 1;
		mQScannerMananger = ManagerCreatorF.getManager(QScannerManagerV2.class);
		sdcard_scan.setOnClickListener(this);
		sdcard_scan_stop.setOnClickListener(this);
		return_main.setOnClickListener(this);
		iv_update.setOnClickListener(this);
		mHandle2.sendEmptyMessage(MSG_ENABLE_ALLBTN);
		if (mScanThread == null) {
			Log.v("demo", "mScanThread=" + "start");
			mScanThread = new Thread() {
				@Override
				public void run() {
					countTime();
					mQScannerMananger.scanInstalledPackages(
							new MyQScanListener(), true);
					countTime();
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			};
			mScanThread.start();
			sdcard_scan.setVisibility(View.GONE);
			sdcard_scan_stop.setVisibility(View.VISIBLE);
			iv_progress.setVisibility(View.VISIBLE);

		}

	}

	// 计时器
	void countTime() {
		if (mTimeValue == 0) {
			mMulwareCount = 0;
			mTimeValue = System.currentTimeMillis();
		} else {
			long end = System.currentTimeMillis();

			Message msg = mHandle2.obtainMessage();
			String msgValue = "用时：" + String.valueOf(end - mTimeValue)
					+ "毫秒 扫描软件:" + mCount + "个 病毒：" + mMulwareCount + "个";
			msg.obj = msgValue.toString();
			msg.sendToTarget();

			mTimeValue = 0;
		}
	}

	OnClickListener layoutClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			/*
			 * Log.d("secure",
			 * "onclick.................. view.equals(hideSoftLayout)" +
			 * view.equals(hideSoftLayout));
			 */
			if (view.equals(hideSoftLayout)) {
				if (isHideSoftListVisible) {
					hideSoftList.setVisibility(View.GONE);
				} else {
					hideSoftList.setVisibility(View.VISIBLE);
				}
				isHideSoftListVisible = !isHideSoftListVisible;
			}
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sdcard_scan:
			if (btStatus == 1) {
				btStatus = 1;
				mHandle2.sendEmptyMessage(MSG_ENABLE_ALLBTN);
				if (mScanThread == null) {
					Log.v("demo", "mScanThread=" + "start");
					mScanThread = new Thread() {
						@Override
						public void run() {
							countTime();
							mQScannerMananger.scanInstalledPackages(
									new MyQScanListener(), true);
							countTime();
							try {
								Thread.sleep(1500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

					};
					mScanThread.start();
					sdcard_scan.setVisibility(View.GONE);
					sdcard_scan_stop.setVisibility(View.VISIBLE);
					iv_progress.setVisibility(View.VISIBLE);
				}
			} else if (btStatus == 2) {
				finish();
			}

			break;

		case R.id.sdcard_scan_stop:
			CustomDialog.Builder builder= new CustomDialog.Builder(ScannerActivity.this);
			builder.setTitle(R.string.scan_cutomdialog_title)
				.setMessage(R.string.scan_cutomdialog_message)
				.setNegativeButton(R.string.scan_cutomdialog_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//
					}
				})
				.setPositiveButton(R.string.scan_cutomdialog_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
			CustomDialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
			break;
		case R.id.return_main:
			this.finish();
			break;
		case R.id.iv_update:
			intent = new Intent();
			intent.setClass(getApplicationContext(), UpdateActivity.class);
			startActivity(intent);
			break;
		}
	}

	private class MyQScanListener extends QScanListenerV2 {
		private static final String TAG = "bian";

		private LinkedList<String> mSb = new LinkedList<String>();

		@Override
		public void onScanStarted(int scanType) {
			android.util.Log.v(TAG, "onScanStarted:[" + scanType + "]");
			updateTip("扫描开始：", -1);
			updateTip("扫描类型：" + getScanTypeString(scanType), -1);
		}
		/**
		 * 安装包扫描进度回调
		 * 
		 * @param scanType
		 *            扫描类型，具体参考{@link QScanConstants#SCAN_INSTALLEDPKGS} ~
		 *            {@link QScanConstants#SCAN_SPECIALS}
		 * @param progress
		 *            扫描进度 像未安装apk扫描，progress无法计算，这里会返回-1的值，标识未知
		 * @param result
		 *            扫描项信息
		 */
		@Override
		public void onScanProgress(int scanType, int progress,
				QScanResultEntity result) {
			updateTip(result, progress);
		}

		/**
		 * 搜索到不扫描的文件的回调
		 */
		@Override
		public void onFoundElseFile(int scanType, File file) {

			android.util.Log.v(TAG, "onFoundElseFile:[" + scanType + "]");
		}

		/**
		 * 云扫描出现网络错误
		 * 
		 * @param scanType
		 *            扫描类型，具体参考{@link QScanConstants#SCAN_INSTALLEDPKGS} ~
		 *            {@link QScanConstants#SCAN_SPECIALS}
		 * @param errCode
		 *            错误码
		 */
		@Override
		public void onScanError(int scanType, int errCode) {
			android.util.Log.v(TAG, "onScanError--scanType[" + scanType
					+ "]errCode[" + errCode + "]");

			updateTip("查杀出错，出错码：" + errCode + " " + "查杀类型-"
					+ getScanTypeString(scanType), 0);
			mHandle2.sendEmptyMessage(MSG_RESET_PAUSE);
		}

		/**
		 * 扫描被暂停时回调
		 */
		@Override
		public void onScanPaused(int scanType) {
			android.util.Log.v(TAG, "onScanPaused--scanType[" + scanType + "]");
			updateTip("暂停扫描：查杀类型-" + getScanTypeString(scanType), -1);
		}

		/**
		 * 扫描继续时回调
		 */
		@Override
		public void onScanContinue(int scanType) {
			android.util.Log.v(TAG, "onScanContinue--scanType[" + scanType
					+ "]");

			updateTip("继续扫描：查杀类型-" + getScanTypeString(scanType), -1);
		}

		/**
		 * 扫描被取消时回调
		 */
		@Override
		public void onScanCanceled(int scanType) {
			android.util.Log.v(TAG, "onScanCanceled--scanType[" + scanType
					+ "]");

			updateTip("扫描已取消：查杀类型-" + getScanTypeString(scanType), -1);
			mScanThread = null;
		}

		/**
		 * 扫描结束
		 * 
		 * @param scanType
		 *            扫描类型，具体参考{@link QScanConstants#SCAN_INSTALLEDPKGS} ~
		 *            {@link QScanConstants#SCAN_SPECIALS}
		 * @param results
		 *            扫描的所有结果
		 */
		@Override
		public void onScanFinished(int scanType, List<QScanResultEntity> results) {
			android.util.Log.v(TAG, "onScanFinished--scanType[" + scanType
					+ "]results.size()[" + results.size() + "]");

			mMulwareCount = results.size();
			for (QScanResultEntity entity : results) {
				Log.v(TAG, "[onScanFinished]" + "softName[" + entity.softName
						+ "]packageName[" + entity.packageName + "]path["
						+ entity.path + "]name[" + entity.name + "]");

				Log.v(TAG, "[onScanFinished]" + "discription["
						+ entity.discription + "]url[" + entity.url);
			}
			mScanThread = null;
			Message msg = mHandle.obtainMessage();
			msg.what = ONSCANFINISHED;
			mHandle.sendMessage(msg);
			// updateTip("扫描结束:查杀类型-" + getScanTypeString(scanType), -1);
			if (results != null) {
				new DisplayResult().displayResult(results);
			}

		}

		// 更新提示
		private void updateTip(String text, int progress) {
			if (mSb.size() == 20) {
				mSb.remove(0);
			}

			mSb.add(text + "\n");
			StringBuffer tmp = new StringBuffer();
			for (String line : mSb) {
				tmp.append(line);
			}

			Message msg = mHandle.obtainMessage();
			msg.obj = tmp.toString();
			msg.arg1 = progress;
			Log.i("andysinguan", "progress -> " + progress);
			msg.sendToTarget();
		}

		// 判断应用安全
		private String getEntityDes(QScanResultEntity result) {
			StringBuilder content = new StringBuilder();
			String message = result.softName;
			if (message == null || message.length() == 0) {
				message = result.path;
			}
			message = message + "[" + result.discription + "]";

			switch (result.type) {
			case QScanConstants.TYPE_OK:
				content.append(message + " 正常");
				mCount++;
				break;

			case QScanConstants.TYPE_RISK:
				android.util.Log.v("demo", result.softName + " is TYPE_RISK ");
				content.append(message + "  风险");
				mMulwareCount++;
				mCount++;
				break;

			case QScanConstants.TYPE_VIRUS:
				android.util.Log.v("demo", result.packageName
						+ " is TYPE_VIRUS ");
				content.append(message + " " + result.name + " 病毒");
				mMulwareCount++;
				mCount++;
				break;

			case QScanConstants.TYPE_SYSTEM_FLAW:
				android.util.Log.v("demo", result.packageName
						+ " is TYPE_SYSTEM_FLAW ");
				content.append(message + " " + result.name + " 系统漏洞");
				mMulwareCount++;
				mCount++;
				break;

			case QScanConstants.TYPE_TROJAN:
				android.util.Log.v("demo", result.packageName
						+ " is TYPE_TROJAN ");
				content.append(message + " " + result.name + " 专杀木马");
				mMulwareCount++;
				mCount++;
				break;

			case QScanConstants.TYPE_UNKNOWN:
				content.append(message + "  未知");
				mCount++;
				break;

			default:
				android.util.Log.v("demo", result.softName + " is others! ");
				content.append(message + "  未知");
				break;
			}
			log_adinfo(result);
			return content.append(getEntityAdvice(result)).toString();
		}

		// ad block info
		private void log_adinfo(QScanResultEntity result) {
			if (result.plugins != null) {
				ArrayList<QScanAdPluginEntity> plugins = result.plugins;
				if (plugins.size() > 0) {
					android.util.Log.v("demo", result.softName + " has ad : "
							+ plugins.size());
					int i = 1;
					for (QScanAdPluginEntity n : plugins) {
						android.util.Log.v("demo", "" + i + ". ");
						android.util.Log.v("demo",
								"  "
										+ n.id
										+ ":"
										+ n.type
										+ ":"
										+ (n.name == null ? "nonName" : n.name)
										+ ":"
										+ (n.banIps == null ? "nonbanIps"
												: n.banIps)
										+ ":"
										+ (n.banUrls == null ? "nonbanUrls"
												: n.banUrls));
						i++;
					}
				}
			}
		}

		// 应用安全建议
		private String getEntityAdvice(QScanResultEntity result) {
			StringBuilder content = new StringBuilder();
			content.append("[");
			switch (result.advice) {
			case QScanConstants.ADVICE_NONE:
				content.append("无建议");
				break;

			case QScanConstants.ADVICE_CLEAR:
				content.append("建议清除");
				break;

			case QScanConstants.ADVICE_UPDATE:
				content.append("建议升级");
				break;

			case QScanConstants.ADVICE_CLEAR_UPDATE:
				content.append("建议清除或升级");
				break;

			case QScanConstants.ADVICE_CHECK_PAGE:
				content.append("建议查看清除方法");
				break;

			case QScanConstants.ADVICE_CHECK_PAGE_UPDATE:
				content.append("建议查看清除方法或者升级");
				break;

			case QScanConstants.ADVICE_DOWN_TOOL:
				content.append("建议下载专杀清除");
				break;

			case QScanConstants.ADVICE_DOWN_TOOL_UPDATE:
				content.append("建议下载专杀清除或者升级");
				break;

			default:
				content.append("无建议");
				break;
			}
			content.append("]");
			return content.toString();
		}

		private void updateTip(QScanResultEntity result, int progress) {
			// //正常的不显示
			// if(result.type == QScanConstants.TYPE_OK || result.type ==
			// QScanConstants.TYPE_UNKNOWN){
			// return;
			// }
			//

			if (mSb.size() == 20) {
				mSb.remove(0);
			}

			mSb.add(getEntityDes(result) + "\n");
			StringBuffer tmp = new StringBuffer();
			for (String line : mSb) {
				tmp.append(line);
			}

			Message msg = mHandle.obtainMessage();
			msg.obj = tmp.toString();
			msg.arg1 = progress;
			Log.i("andysinguan", "progress -> " + progress);
			msg.sendToTarget();
		}
	}

	private String getScanTypeString(int type) {
		switch (type) {
		case QScanConstants.SCAN_INSTALLEDPKGS:

			return "已安装软件扫描";
		case QScanConstants.SCAN_UNINSTALLEDAPKS:
			return "未安装的APK扫描";
		case QScanConstants.SCAN_CLOUD:
			return "云查杀";
		default:
			return String.valueOf(type);
		}
	}

	private class DisplayResult {
		private int mCount;
		private int mMulwareCount;

		public void displayResult(List<QScanResultEntity> results) {
			LinkedList<String> mSb = new LinkedList<String>();
			mCount = 0;
			mMulwareCount = 0;
			String des = null;
			mSb.add("Results:\n");
			for (QScanResultEntity re : results) {
				des = getEntityDes(re);
				if (des != null) {
					mSb.add(des + "\n");
				}
			}

			Message msg = mHandle.obtainMessage();
			msg.obj = mSb.toString();
			msg.arg1 = 100;
			msg.sendToTarget();

			Message msg1 = mHandle2.obtainMessage();
			String msgValue = "扫描软件:" + mCount + "个 病毒：" + mMulwareCount + "个";
			msg1.obj = msgValue.toString();
			msg1.sendToTarget();

		}

		private String getEntityDes(QScanResultEntity result) {
			StringBuilder content = new StringBuilder();
			content.append("应用程序扫描：");
			String message = result.softName;
			if (message == null || message.length() == 0) {
				message = result.path;
			}
			boolean isNormal = false;
			switch (result.type) {
			case QScanConstants.TYPE_OK:
				content.append(message + " 正常");
				mCount++;
				isNormal = true;
				break;

			case QScanConstants.TYPE_RISK:
				android.util.Log.v("bian", result.softName + " is TYPE_RISK ");
				content.append(message + "  风险");
				mMulwareCount++;
				mCount++;
				break;

			case QScanConstants.TYPE_VIRUS:
				android.util.Log.v("bian", result.packageName
						+ " is TYPE_VIRUS ");
				content.append(message + " " + result.name + " 病毒");
				mMulwareCount++;
				mCount++;
				break;

			case QScanConstants.TYPE_SYSTEM_FLAW:
				android.util.Log.v("bian", result.packageName
						+ " is TYPE_SYSTEM_FLAW ");
				content.append(message + " " + result.name + " 系统漏洞");
				mMulwareCount++;
				mCount++;
				break;

			case QScanConstants.TYPE_TROJAN:
				android.util.Log.v("bian", result.packageName
						+ " is TYPE_TROJAN ");
				content.append(message + " " + result.name + " 专杀木马");
				mMulwareCount++;
				mCount++;
				break;

			case QScanConstants.TYPE_NOT_OFFICIAL:
				android.util.Log.v("bian", result.packageName
						+ " is TYPE_NOT_OFFICIAL ");
				content.append(message + " " + result.name + " 非官方证书");
				mMulwareCount++;
				mCount++;
				break;

			case QScanConstants.TYPE_RISK_PAY:
				android.util.Log.v("bian", result.packageName
						+ " is TYPE_RISK_PAY ");
				content.append(message + " " + result.name + " 支付风险");
				mMulwareCount++;
				mCount++;
				break;

			case QScanConstants.TYPE_RISK_STEALACCOUNT:
				android.util.Log.v("bian", result.packageName
						+ " is TYPE_RISK_STEALACCOUNT ");
				content.append(message + " " + result.name + " 账号风险");
				mMulwareCount++;
				mCount++;
				break;

			case QScanConstants.TYPE_UNKNOWN:
				content.append(message + "  未知");
				mCount++;
				isNormal = true;
				break;

			default:
				android.util.Log.v("bian", result.softName + " is others! ");
				content.append(message + "  未知");
				mCount++;
				break;
			}
			if (isNormal) {
				return null;
			}
			return content.append(getEntityAdvice(result)).toString();
		}

		private String getEntityAdvice(QScanResultEntity result) {
			StringBuilder content = new StringBuilder();
			content.append("[");
			switch (result.advice) {
			case QScanConstants.ADVICE_NONE:
				content.append("无建议");
				break;

			case QScanConstants.ADVICE_CLEAR:
				content.append("建议清除");
				break;

			case QScanConstants.ADVICE_UPDATE:
				content.append("建议升级");
				break;

			case QScanConstants.ADVICE_CLEAR_UPDATE:
				content.append("建议清除或升级");
				break;

			case QScanConstants.ADVICE_CHECK_PAGE:
				content.append("建议查看清除方法");
				break;

			case QScanConstants.ADVICE_CHECK_PAGE_UPDATE:
				content.append("建议查看清除方法或者升级");
				break;

			case QScanConstants.ADVICE_DOWN_TOOL:
				content.append("建议下载专杀清除");
				break;

			case QScanConstants.ADVICE_DOWN_TOOL_UPDATE:
				content.append("建议下载专杀清除或者升级");
				break;

			default:
				content.append("无建议");
				break;
			}
			content.append("]");
			return content.toString();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		background = null;
	}

}
