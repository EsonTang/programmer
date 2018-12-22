package com.koobee.koobeecenter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.koobee.koobeecenter.utils.WindowMangerUtils;
import com.koobee.koobeecenter02.R;

public class IntroActivity extends Activity {

	@SuppressLint({ "NewApi", "ResourceAsColor" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Window window = getWindow();
		// window.requestFeature(Window.FEATURE_NO_TITLE);
		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		// window = getWindow();
		// window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
		// | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		// window.getDecorView().setSystemUiVisibility(
		// View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		// | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		// // | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		// window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		// window.setStatusBarColor(R.color.color_ffffff);
		// window.setNavigationBarColor(R.color.color_ffffff);
		// }
		getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
		WindowMangerUtils.initStateBar(getWindow(), this);
		setContentView(R.layout.activity_intro);
		WindowMangerUtils.changeStatus(getWindow());
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.aftersales_instroduction);
	}

	public void back_clk(View v) {
		finish();
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}
}
