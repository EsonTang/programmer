package com.koobee.koobeecenter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.koobee.koobeecenter.base.BaseActivity;
import com.koobee.koobeecenter02.R;

/**
 * 选择问题反馈类别界面
 * 
 * @author longbaoxiu
 *
 */
public class FeedBackActivity extends BaseActivity implements
		View.OnClickListener {
	private TextView power_and_heat_TV;
	private TextView signal_and_communicate_TV;
	private TextView shutdow_and_restart_TV;
	private TextView bluetooth_TV;
	private TextView camara_TV;
	private TextView wlan_TV;
	private TextView gps_TV;
	private TextView advice_TV;
	private TextView application_TV;

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
			window.setStatusBarColor(getResources().getColor(
					R.color.feedback_status_color));
			// window.setNavigationBarColor(Color.TRANSPARENT);
		}
		setContentView(R.layout.activity_feedback_layout);
		findViewById();
		setListener();
	}

	@Override
	protected void findViewById() {
		wlan_TV = (TextView) findViewById(R.id.wlan_TV);
		power_and_heat_TV = (TextView) findViewById(R.id.power_and_heat_TV);
		signal_and_communicate_TV = (TextView) findViewById(R.id.signal_and_communicate_TV);
		shutdow_and_restart_TV = (TextView) findViewById(R.id.shutdow_and_restart_TV);
		bluetooth_TV = (TextView) findViewById(R.id.bluetooth_TV);
		camara_TV = (TextView) findViewById(R.id.camara_TV);
		gps_TV = (TextView) findViewById(R.id.gps_TV);
		advice_TV = (TextView) findViewById(R.id.advice_TV);
		application_TV = (TextView) findViewById(R.id.application_TV);

	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(FeedBackActivity.this,
				FeedBackRequestActivity.class);
		String qtype = "";
		switch (v.getId()) {
		case R.id.application_TV:
			qtype = "9";
			break;
		case R.id.advice_TV:
			qtype = "8";
			break;
		case R.id.gps_TV:
			qtype = "7"; 
			break;
		case R.id.wlan_TV:
			qtype = "6";
			break;
		case R.id.camara_TV:
			qtype = "5";
			break;
		case R.id.bluetooth_TV:
			qtype = "4";   
			break;
		case R.id.shutdow_and_restart_TV:
			qtype = "3";
			break;
		case R.id.signal_and_communicate_TV:
			qtype = "2";
			break;
		case R.id.power_and_heat_TV:
			qtype = "1";
			break;
		}
		intent.putExtra("qtype", qtype);
		startActivity(intent);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

	}

	@Override
	protected void init() {

	}

	@Override
	protected void setListener() {
		application_TV.setOnClickListener(this);
		advice_TV.setOnClickListener(this);
		gps_TV.setOnClickListener(this);
		wlan_TV.setOnClickListener(this);
		camara_TV.setOnClickListener(this);
		bluetooth_TV.setOnClickListener(this);
		shutdow_and_restart_TV.setOnClickListener(this);
		signal_and_communicate_TV.setOnClickListener(this);
		power_and_heat_TV.setOnClickListener(this);

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}
}
