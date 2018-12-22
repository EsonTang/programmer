package com.koobee.koobeecenter;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.koobee.koobeecenter.base.BaseActivity;
import com.koobee.koobeecenter.utils.ToastUtils;
import com.koobee.koobeecenter02.R;

public class MainActivity extends BaseActivity implements View.OnClickListener {
	private RelativeLayout mFeedBackRealtive;
	private RelativeLayout system_update_Rlyt;
	private RelativeLayout after_servicee_Rlyt;
	private RelativeLayout tmall_Rlyt;
	private RelativeLayout koobee_Rlyt;

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
			intent.setComponent(new ComponentName("com.adups.fota",
					"com.adups.fota.GoogleOtaClient"));
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
	protected void init() {

	}

}
