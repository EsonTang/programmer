package com.prize.boot;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ActionBar.LayoutParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.boot.R;
import com.prize.boot.appadapter.OneKeyInsallAdapter;
import com.prize.boot.util.ClientInfo;
import com.prize.boot.util.OneKeyInstallData.DataEntity.AppsEntity;
import com.prize.boot.util.OneKeyInstallServiceInterface;

public class OneKeyDialog extends Dialog implements android.view.View.OnClickListener {

	public OneKeyDialog(Context context) {
		super(context);
	}

	private ImageView delDialogBtn;
	private TextView installAll;
	private TextView wifiConnected;
	private OneKeyInsallAdapter onKeyInsallAdapter;

	// private ArrayList<AppsItemBean> mDatas;

	private Context mContext;
	private NetStateReceiver mNetstateReceiver;
	// private PromptDialogFragment df;

	/*
	 * public OneKeyDialog(Context context, ArrayList<AppsItemBean> dates) {
	 * super(context); this.mDatas = dates; mContext = context; }
	 */

	/*
	 * public OneKeyDialog(Context context, int theme, ArrayList<AppsItemBean>
	 * dates) { super(context, theme); this.mDatas = dates; mContext = context;
	 * // TODO Auto-generated constructor stub }
	 */

	private List<AppsEntity> mAppsEntities;

	public OneKeyDialog(Context context, int theme, List<AppsEntity> mAppsEntities) {
		super(context, theme);
		mContext = context;
		this.mAppsEntities = mAppsEntities;
		// TODO Auto-generated constructor stub
	}

	class NetStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			// 判断网络状态
			ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			State wifi = (info != null) ? info.getState() : State.DISCONNECTED;
			info = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (wifi == State.CONNECTED) {
				if (wifiConnected != null) {
					wifiConnected.setVisibility(View.VISIBLE);
				}
			} else {
				wifiConnected.setVisibility(View.INVISIBLE);
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mNetstateReceiver = new NetStateReceiver();
		mContext.registerReceiver(mNetstateReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		Window window = getWindow();
		window.setGravity(Gravity.BOTTOM);
		window.setWindowAnimations(R.style.popwindow_anim_style);
		setContentView(R.layout.dialog_one_key_install);
		window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		delDialogBtn = (ImageView) findViewById(R.id.btn_delete_dialog);
		installAll = (TextView) findViewById(R.id.install_all);
		/*
		 * installAll.setText(installAll.getContext().getString(
		 * R.string.dialog_down_comment, "0", "0"));
		 */
		onKeyInsallAdapter = new OneKeyInsallAdapter(this.getContext(), mAppsEntities);
		wifiConnected = (TextView) findViewById(R.id.wifi_conntected);
		boolean visible = ClientInfo.networkType == ClientInfo.WIFI;
		wifiConnected.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		delDialogBtn.setOnClickListener(this);
		installAll.setOnClickListener(this);
		GridView dialog_gridview = (GridView) findViewById(R.id.dialog_gridview);
		dialog_gridview.setAdapter(onKeyInsallAdapter);

		// 2015-12-24
		/*
		 * df =
		 * PromptDialogFragment.newInstance(mContext.getString(R.string.tip),
		 * mContext.getString(R.string.toast_tip_download_only_wifi),
		 * mContext.getString(R.string.now_download),
		 * mContext.getString(R.string.download_after), mDeletePromptListener);
		 */

		/*
		 * df.setDismissCallBack(new DismissCallBack() {
		 * 
		 * @Override public void oneKeyDialogDismiss() {
		 * OneKeyDialog.this.dismiss(); } });
		 */
	}

	/**
	 * 数据流量下载对话框
	 */
	private View.OnClickListener mDeletePromptListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// df.dismissAllowingStateLoss();
			oneKeyDown();
		}
	};

	@Override
	public void dismiss() {
		mContext.unregisterReceiver(mNetstateReceiver);
		// MainActivity m = (MainActivity) mContext;
		// m.showUpdateDialog();
		super.dismiss();
	}

	private void oneKeyDown() {
		if (onKeyInsallAdapter != null && onKeyInsallAdapter.getCount() > 0) {
			List<AppsEntity> items = onKeyInsallAdapter.getChecks();
			for (int i = 0; i < items.size(); i++) {
				OneKeyInstallServiceInterface.startBackgoundDownload(items.get(i));
				Log.i("pengcancan", "startappDownloading..." + items.get(i));
			}
		}
		this.dismiss();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_delete_dialog:
			this.dismiss();
			break;
		case R.id.install_all:
			if (ClientInfo.networkType != ClientInfo.WIFI && ClientInfo.networkType != ClientInfo.NONET) {
			} else {
				oneKeyDown();
				getContext().getSharedPreferences("onekeyinstall", Activity.MODE_PRIVATE).edit().putBoolean("unInstalled", true).commit();
				Log.i("pengcancan", "---->unInstalled:"+getContext().getSharedPreferences("onekeyinstall", Activity.MODE_PRIVATE).getBoolean("unInstalled", false));
			}
			break;
		default:
			this.dismiss();
			break;
		}
	}
}
