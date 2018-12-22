package com.prize.prizethemecenter.ui.widget;

import android.app.ActionBar.LayoutParams;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.app.util.PackageUtils;
import com.prize.app.util.PreferencesUtils;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.AppsItemBean;
import com.prize.prizethemecenter.receiver.CompleteReceiver;
import com.prize.prizethemecenter.ui.utils.DownloadManagerPro;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UpdateDataUtils;
import com.prize.prizethemecenter.ui.widget.view.XRTextView;

import java.io.File;
import java.text.DecimalFormat;

/**
 **
 * 自升级的弹出提示dialog
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class UpdateSelfDialog extends Dialog implements
		View.OnClickListener, CompleteReceiver.DownFinish {

	public UpdateSelfDialog(Context context) {
		super(context);
	}

	private DownloadChangeObserver downloadObserver;
	private DownloadManager downloadManager;
	private DownloadManagerPro downloadManagerPro;
	private ProgressBar downloadProgress;
	private long downloadId = 0;
	private Context mContext;
	private String title;
	private String subtitle;
	private String content;
	private MyHandler handler;
	private Button add_neg;
	private Button sureBtn;
	private AppsItemBean bean;
	private TextView downloadSize;
	private TextView downloadPrecent;
	private TextView download_finish;
	private LinearLayout down_percenter_ll;
	private TextView caution_tv;
	private RelativeLayout middle_Rlyt;

	public void setBean(AppsItemBean bean) {
		this.bean = bean;
	}

	public UpdateSelfDialog(Context context, int theme, String title,
			String subtitle, String content) {
		super(context, theme);
		mContext = context;
		this.title = title;
		this.subtitle = subtitle;
		this.content = content;
		downloadManager = (DownloadManager) mContext
				.getSystemService(Context.DOWNLOAD_SERVICE);
		downloadManagerPro = new DownloadManagerPro(downloadManager);
		handler = new MyHandler();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.setGravity(Gravity.CENTER);
		window.addFlags(Window.FEATURE_NO_TITLE);
		window.setWindowAnimations(R.style.popwindow_anim_style);
		setContentView(R.layout.fragment_updateself_dialog);
		window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		downloadProgress = (ProgressBar) findViewById(R.id.download_progress);
		// TextView content_tv = (TextView) findViewById(R.id.content_tv);
		XRTextView content_tv = (XRTextView) findViewById(R.id.content_tv);
		TextView title_tv = (TextView) findViewById(R.id.title_tv);
		TextView size_tv = (TextView) findViewById(R.id.size_tv);
		caution_tv = (TextView) findViewById(R.id.caution_tv);

		down_percenter_ll = (LinearLayout) findViewById(R.id.down_percenter_ll);
		download_finish = (TextView) findViewById(R.id.download_finish);
		downloadSize = (TextView) findViewById(R.id.download_size);
		downloadPrecent = (TextView) findViewById(R.id.download_precent);
		add_neg = (Button) findViewById(R.id.add_neg);
		sureBtn = (Button) findViewById(R.id.sure_Btn);
		if (!TextUtils.isEmpty(subtitle)) {
			size_tv.setText(subtitle);
		}

		middle_Rlyt = (RelativeLayout)findViewById(R.id.middle_Rlyt);
		middle_Rlyt.setBackgroundColor(mContext.getResources().getColor(R.color.white));
//		else{
//			size_tv.setText("1.1test");
//		}
		if (!TextUtils.isEmpty(content)) {
			// content_tv.setText(Html.fromHtml(content.trim()));
			content_tv.setText(content.trim());
		}
//		else{
//			String test="1 详情页全新设计，每一次升级，总有一处新颜等君来\n"
//					+ "2 牵手「最美应用」团队，由专业设计师团队测评筛选，每日为您推荐「最美」的手机应用\n"
//					+ "3 首页添加「精选应用」和「最美应用」，好应用一一呈现\n"
//					+ "4 下载按钮重新设计，现在在任何地方按钮都可以暂停啦\n"
//					+ "5 更新界面全新设计，升级市场如沐春风\n"
//					+ "6 用户反馈更好用啦，不爽的地方一定要告诉我们哦\n"
//					+ "7 一键装机升级优化，更好用更好看";
//			content_tv.setText(test);
//			
//		}
		if (!TextUtils.isEmpty(title)) {
			title_tv.setText(title);
		}
//		else{
//			title_tv.setText("1.2.test");
//		}
		downloadObserver = new DownloadChangeObserver();
		mContext.getContentResolver().registerContentObserver(
				DownloadManagerPro.CONTENT_URI, true, downloadObserver);
		add_neg.setOnClickListener(this);
		sureBtn.setOnClickListener(this);
	}

	@Override
	public void dismiss() {
		super.dismiss();
		mContext.getContentResolver().unregisterContentObserver(
				downloadObserver);
		if (handler != null) {
			handler.removeCallbacksAndMessages(null);

		}
	}

	@Override
	public void show() {
		JLog.i("bao", "show()" + "---mContext=" + mContext);
		if (handler == null) {
			handler = new MyHandler();

		}
		if (downloadObserver == null) {
			downloadObserver = new DownloadChangeObserver();
		}
		if (mContext != null) {
			mContext.getContentResolver().registerContentObserver(
					DownloadManagerPro.CONTENT_URI, true, downloadObserver);

		}
		updateView();
		super.show();
	}

	@Override
	public void onAttachedToWindow() {
		JLog.i("bao", "onAttachedToWindow");
		super.onAttachedToWindow();
	}
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.add_neg:
			this.dismiss();
			break;
		case R.id.sure_Btn:// 下载安装

			downloadId = PreferencesUtils.getLong(mContext,
					Constants.KEY_NAME_DOWNLOAD_ID);
			int[] bytesAndStatus = downloadManagerPro
					.getBytesAndStatus(downloadId);

			if (bytesAndStatus[2] == DownloadManager.STATUS_RUNNING) return;
			if (!TextUtils.isEmpty(Constants.APKFILEPATH)) {
				File file = new File(Constants.APKFILEPATH);
				if (file.exists()) {
					PackageInfo packageInfo = mContext.getPackageManager()
							.getPackageArchiveInfo(Constants.APKFILEPATH,
									PackageManager.GET_ACTIVITIES);
					if (packageInfo != null && bean != null) {
						if (bean.packageName.equals(packageInfo.packageName) && bean.versionCode == packageInfo.versionCode) {
							if (MD5Util.Md5Check(Constants.APKFILEPATH,
									bean.apkMd5)) {
								PackageUtils.installNormal(
										mContext.getApplicationContext(),
										Constants.APKFILEPATH);
								return;
							} else {
								file.delete();
							}
						} else {
							file.delete();
						}
					}
				}
			}
			if (ClientInfo.networkType == ClientInfo.NONET) {
				ToastUtils.showToast(R.string.net_error);
				return;
			}
			//暂停所有下载
//			AppManagerCenter.pauseAllDownload();
			CompleteReceiver.isNeedInStall = true;
			UpdateDataUtils.downloadApk(downloadManager, bean, mContext);
			break;
		default:
			this.dismiss();
			break;
		}
	}

	/**
	 * MyHandler
	 * 
	 * @author
	 */
	private class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case 0:
				int status = (Integer) msg.obj;
				JLog.i("bao", "1.4.2=downing---8:success--16:faile--status="
						+ status);
				if (isDownloading(status)) {
					download_finish.setVisibility(View.GONE);
					down_percenter_ll.setVisibility(View.VISIBLE);
					downloadProgress.setMax(0);
					downloadProgress.setProgress(0);
					if (msg.arg2 < 0) {
						// downloadPrecent.setText("0%");
						// downloadSize.setText("0M/0M");
					} else {
						downloadProgress.setMax(msg.arg2);
						JLog.i("bao", "1	downloadProgress.setMax(msg.arg2)");
						downloadProgress.setProgress(msg.arg1);
						downloadPrecent.setText(getNotiPercent(msg.arg1,
								msg.arg2));
						downloadSize.setText(getAppSize(msg.arg1) + "/"
								+ getAppSize(msg.arg2));
					}
				} else {
					down_percenter_ll.setVisibility(View.GONE);
					downloadProgress.setMax(0);
					downloadProgress.setProgress(0);

					if (status == DownloadManager.STATUS_FAILED) {
						sureBtn.setText(R.string.now_update);
						PreferencesUtils.putLong(mContext,
								Constants.KEY_NAME_DOWNLOAD_ID, 0);
					} else if (status == DownloadManager.STATUS_SUCCESSFUL) {
						down_percenter_ll.setVisibility(View.GONE);
						if (!TextUtils.isEmpty(Constants.APKFILEPATH)) {
							File file = new File(Constants.APKFILEPATH);
							if (file.exists()) {
								download_finish.setVisibility(View.VISIBLE);
							} else {
								PreferencesUtils.putLong(mContext,
										Constants.KEY_NAME_DOWNLOAD_ID, -1);
							}
						}
					} else {
						File file = new File(Constants.APKFILEPATH);
						if (file.exists()) {
							download_finish.setVisibility(View.VISIBLE);
						} else {
							download_finish.setVisibility(View.GONE);
							PreferencesUtils.putLong(mContext,
									Constants.KEY_NAME_DOWNLOAD_ID, -1);
							if (ClientInfo.networkType != ClientInfo.WIFI
									&& ClientInfo.networkType != ClientInfo.NONET) {
								caution_tv.setVisibility(View.VISIBLE);
							} else {
								caution_tv.setVisibility(View.GONE);
							}
						}

					}
				}
				break;
			}
		}
	}

	public static final int MB_2_BYTE = 1024 * 1024;
	public static final int KB_2_BYTE = 1024;
	static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");

	/**
	 * @param size
	 * @return
	 */
	public static CharSequence getAppSize(long size) {
		if (size <= 0) {
			return "0M";
		}

		if (size >= MB_2_BYTE) {
			return new StringBuilder(16).append(
					DOUBLE_DECIMAL_FORMAT.format((double) size / MB_2_BYTE))
					.append("M");
		} else if (size >= KB_2_BYTE) {
			return new StringBuilder(16).append(
					DOUBLE_DECIMAL_FORMAT.format((double) size / KB_2_BYTE))
					.append("K");
		} else {
			return size + "B";
		}
	}

	public static String getNotiPercent(long progress, long max) {
		int rate = 0;
		if (progress <= 0 || max <= 0) {
			rate = 0;
		} else if (progress > max) {
			rate = 100;
		} else {
			rate = (int) ((double) progress / max * 100);
		}
		return new StringBuilder(16).append(rate).append("%").toString();
	}

	public static boolean isDownloading(int downloadManagerStatus) {
		return downloadManagerStatus == DownloadManager.STATUS_RUNNING
				|| downloadManagerStatus == DownloadManager.STATUS_PAUSED
				|| downloadManagerStatus == DownloadManager.STATUS_PENDING;
	}

	class DownloadChangeObserver extends ContentObserver {

		public DownloadChangeObserver() {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			JLog.i("bao", "onChange");
			updateView();
		}

	}

	public void updateView() {
		downloadId = PreferencesUtils.getLong(mContext,
				Constants.KEY_NAME_DOWNLOAD_ID);
		if (mContext == null) {
			return;
		}
		if (downloadManager == null) {
			downloadManager = (DownloadManager) mContext
					.getSystemService(Context.DOWNLOAD_SERVICE);
		}
		if (downloadManagerPro == null) {
			downloadManagerPro = new DownloadManagerPro(downloadManager);
		}
		int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadId);
		handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0],
				bytesAndStatus[1], bytesAndStatus[2]));
	}

	@Override
	public void onDownFinish(Intent intent) {
		long completeDownloadId = intent.getLongExtra(
				DownloadManager.EXTRA_DOWNLOAD_ID, -1);
		if (completeDownloadId == downloadId) {
			updateView();
			dismiss();
		}
	}

	// 全角转化为半角的方法
	public String ToDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (isChinese(c[i])) {
				if (c[i] == 12288) {
					c[i] = (char) 32;
					continue;
				}
				if (c[i] > 65280 && c[i] < 65375)
					c[i] = (char) (c[i] - 65248);
			}
		}
		return new String(c);
	}

	private boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

}
