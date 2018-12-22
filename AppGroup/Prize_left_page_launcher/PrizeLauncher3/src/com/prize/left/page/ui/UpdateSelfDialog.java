package com.prize.left.page.ui;

import java.io.File;

import org.xutils.common.util.LogUtil;

import com.android.launcher3.R;
import com.prize.left.page.bean.AppInfoBean;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.DownloadManagerPro;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.PackageUtils;
import com.prize.left.page.util.PreferencesUtils;
import com.prize.left.page.util.ToastUtils;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 自升级的弹出提示dialog
 * @author prize appcenter
 */
public class UpdateSelfDialog extends Dialog implements
		android.view.View.OnClickListener {

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
	
	private TextView downloadSize;
	
	private TextView downloadPrecent;
	
	private TextView download_finish;
	
	private LinearLayout down_percenter_ll;
	
	private RelativeLayout middle_Rlyt;
	
	private RelativeLayout middle_update;
	
	private TextView caution_tv;
	
	private AppInfoBean bean;
	/**延迟刷新的时间间隔 2小时*/
	private final long BETWEEN_TIME = 1000 * 60 * 60 * 24;

	private static UpdateSelfDialog mUpdateSelfDialog;
	public UpdateSelfDialog(Context context) {
		super(context);
	}
	
	private UpdateSelfDialog(Context context, int theme, String title,
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

	public static UpdateSelfDialog getInstance(Context context, int theme, String title,
			String subtitle, String content){
		if(mUpdateSelfDialog == null){
			mUpdateSelfDialog = new  UpdateSelfDialog(context,theme,title,subtitle,content);
		}
		return mUpdateSelfDialog;
	}
	public void setBean(AppInfoBean b) {
		bean = b;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.setGravity(Gravity.CENTER);
		window.addFlags(Window.FEATURE_NO_TITLE);
		window.setWindowAnimations(R.style.popwindow_anim_style);
		setContentView(R.layout.self_update_dialog);
		window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
		
		initView();
		
	}
	
	private void initView() {
		
		downloadProgress = (ProgressBar) findViewById(R.id.download_progress);
		XRTextView content_tv = (XRTextView) findViewById(R.id.content_tv);
		TextView title_tv = (TextView) findViewById(R.id.title_tv);
		TextView size_tv = (TextView) findViewById(R.id.size_tv);
		caution_tv = (TextView) findViewById(R.id.caution_tv);

		middle_Rlyt = (RelativeLayout) findViewById(R.id.middle_Rlyt);
		middle_update = (RelativeLayout) findViewById(R.id.middle_update);
		down_percenter_ll = (LinearLayout) findViewById(R.id.down_percenter_ll);
		download_finish = (TextView) findViewById(R.id.download_finish);
		downloadSize = (TextView) findViewById(R.id.download_size);
		downloadPrecent = (TextView) findViewById(R.id.download_precent);
		add_neg = (Button) findViewById(R.id.add_neg);
		sureBtn = (Button) findViewById(R.id.sure_Btn);
		middle_Rlyt.setVisibility(View.GONE);
		middle_update.setVisibility(View.VISIBLE);
		
		if (!TextUtils.isEmpty(subtitle)) {
			size_tv.setText(subtitle);
		}
		if (!TextUtils.isEmpty(content)) {
			content_tv.setText(content.trim());
		}
		if (!TextUtils.isEmpty(title)) {
			title_tv.setText(title);
		}
		/*mContext.getContentResolver().registerContentObserver(
				DownloadManagerPro.CONTENT_URI, true, downloadObserver);*/
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
		if(downloadObserver ==null) {
			downloadObserver = new DownloadChangeObserver();
		}
		mContext.getContentResolver().registerContentObserver(
				DownloadManagerPro.CONTENT_URI, true, downloadObserver);
		updateView();
		super.show();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.add_neg:
			this.dismiss();
			PreferencesUtils.putLong(mContext, IConstants.KEY_DELAY_CHECK_TIME, System.currentTimeMillis()+BETWEEN_TIME);
			break;
		case R.id.sure_Btn:// 下载安装
			middle_Rlyt.setVisibility(View.VISIBLE);
			middle_update.setVisibility(View.GONE);
			downloadId = PreferencesUtils.getLong(mContext,
					IConstants.KEY_NAME_DOWNLOAD_ID);
			int[] bytesAndStatus = downloadManagerPro
					.getBytesAndStatus(downloadId);
			if (bytesAndStatus[2] == DownloadManager.STATUS_SUCCESSFUL) {
				int tCode = PreferencesUtils.getInt(mContext,
						IConstants.KEY_DOWNLOAD_CODE);
				if (!TextUtils.isEmpty(IConstants.APK_FILE_PATH)) {
					File file = new File(IConstants.APK_FILE_PATH);
					if (file.exists()) {
						PackageUtils.installNormal(
								mContext.getApplicationContext(),
								IConstants.APK_FILE_PATH);
						return;
					}
				}
			}
			if (ClientInfo.networkType == ClientInfo.NONET) {
				ToastUtils.showToast(mContext, R.string.str_no_update);
				return;
			}
			CommonUtils.downloadApk(downloadManager, bean.downloadurlcdn, mContext, bean.versioncode);
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
				
				if (CommonUtils.isDownloading(status)) {
					download_finish.setVisibility(View.GONE);
					down_percenter_ll.setVisibility(View.VISIBLE);
					downloadProgress.setMax(0);
					downloadProgress.setProgress(0);
					if (msg.arg2 < 0) {
						// downloadPrecent.setText("0%");
						// downloadSize.setText("0M/0M");
					} else {
						downloadProgress.setMax(msg.arg2);
						downloadProgress.setProgress(msg.arg1);
						downloadPrecent.setText(CommonUtils.getNotiPercent(msg.arg1,
								msg.arg2));
						downloadSize.setText(CommonUtils.getAppSize(msg.arg1) + "/"
								+ CommonUtils.getAppSize(msg.arg2));
					}
				} else {
					down_percenter_ll.setVisibility(View.GONE);
					downloadProgress.setMax(0);
					downloadProgress.setProgress(0);

					if (status == DownloadManager.STATUS_FAILED) {
						sureBtn.setText(R.string.now_update);
						PreferencesUtils.putLong(mContext,
								IConstants.KEY_NAME_DOWNLOAD_ID, 0);
					} else if (status == DownloadManager.STATUS_SUCCESSFUL) {
						int tCode = PreferencesUtils.getInt(mContext,
								IConstants.KEY_DOWNLOAD_CODE);
						down_percenter_ll.setVisibility(View.GONE);
						if (!TextUtils.isEmpty(IConstants.APK_FILE_PATH)) {
							File file = new File(IConstants.APK_FILE_PATH);
							if (file.exists()) {
								download_finish.setVisibility(View.VISIBLE);
							} else {
								PreferencesUtils.putLong(mContext,
										IConstants.KEY_NAME_DOWNLOAD_ID, -1);
							}
						}
					} else {
						int tCode = PreferencesUtils.getInt(mContext,
								IConstants.KEY_DOWNLOAD_CODE);
						if (!TextUtils.isEmpty(IConstants.APK_FILE_PATH)) {
							File file = new File(IConstants.APK_FILE_PATH);
							if (file.exists()) {
								download_finish.setVisibility(View.VISIBLE);
							}
						} else {
							PreferencesUtils.putLong(mContext,
									IConstants.KEY_NAME_DOWNLOAD_ID, -1);
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

	class DownloadChangeObserver extends ContentObserver {

		public DownloadChangeObserver() {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			updateView();
		}

	}

	public void updateView() {
		downloadId = PreferencesUtils.getLong(mContext,
				IConstants.KEY_NAME_DOWNLOAD_ID);
		LogUtil.i("===downloadId=" + downloadId);
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
		int tCode = PreferencesUtils.getInt(mContext,
				IConstants.KEY_DOWNLOAD_CODE);
		if (bean != null && tCode != bean.versioncode) {
			downloadId = -1;
		}
		int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadId);
		handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0],
				bytesAndStatus[1], bytesAndStatus[2]));
	}
}
