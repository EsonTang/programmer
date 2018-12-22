package com.prize.prizethemecenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.prize.app.util.JLog;
import com.prize.prizethemecenter.R;

/**
 *  created by pengy  启动页
 */
public class InitLogoActivity extends RootActivity {

	private ImageView frontCover;
	private boolean isToMainFrame = false;
	private String TAG = "FrontCoverActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_bg);
		frontCover = (ImageView) findViewById(R.id.loading_img);
		isToMainFrame = false;
		frontCover.setScaleType(ScaleType.FIT_XY);
		gotoMainActivity(1000);
	}
	private void gotoMainActivity(long delay) {
		JLog.i(TAG, "gotoMainActivity");
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (isToMainFrame) {
					return;
				}
				isToMainFrame = true;
				fadeToMainActivity();
				finish();
			}
		}, delay);
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		isToMainFrame = false;
		JLog.i(TAG, "onActivityResult");
		gotoMainActivity(0);
		this.finish();
		super.onActivityResult(arg0, arg1, arg2);
	}

	private void fadeToMainActivity() {
		JLog.i(TAG, "fadeToMainActivity");
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		finish();
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public String getActivityName() {
		return "InitLogoActivity";
	}
}