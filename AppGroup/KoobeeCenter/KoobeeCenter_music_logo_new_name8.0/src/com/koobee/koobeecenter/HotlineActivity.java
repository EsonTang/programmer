package com.koobee.koobeecenter;

import java.lang.reflect.Field;
import java.net.URISyntaxException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.widget.Toast;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.koobee.koobeecenter.utils.WindowMangerUtils;
import com.koobee.koobeecenter02.R;

public class HotlineActivity extends Activity {

	@SuppressLint("ResourceAsColor")
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
		setContentView(R.layout.activity_hotline);
		WindowMangerUtils.changeStatus(getWindow());
		initUI();
		initStatusBar();
	}

	private void initStatusBar() {
		Window window = getWindow();
		window.setStatusBarColor(getResources().getColor(R.color.color_fafafa));

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		try {
			Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
			Field grayField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_GRAY");
			Object gray = grayField.get(statusBarManagerClazz);
			Class windowManagerLpClazz = lp.getClass();
			Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
			statusBarInverseField.set(lp,gray);
			getWindow().setAttributes(lp);
		} catch (Exception e) {
		}
	}
	
	private void initUI() {
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.hotline);
	}

	public void back_clk(View v) {
		finish();
	}

	// prize-add-by-yanghao-20150914-start
	public void toMap(View v) {
		String mapGeo = getString(R.string.map_geo);
		String mapZoom = getString(R.string.map_zoom);
		String mapAddress = getString(R.string.map_address);

		String uriStr = "geo:" + mapGeo + "?z=" + mapZoom + "?q=" + mapAddress;

		Uri mUri = Uri.parse(uriStr);
		Intent it = new Intent(Intent.ACTION_VIEW, mUri);

		try {
			startActivity(it);
		} catch (Exception e) {
			// TODO: handle exception

			display(R.string.map_activity_not_found);
		}
	}

	// prize-add-by-yanghao-20150914-end

	// prize-add-by-yanghao-20150911-start

	public void toBaiduMap(View v) {
		if (isAppInstalled("com.baidu.BaiduMap")) {
			Intent intent;
			try {

				String address = getString(R.string.company_address);
				String intentStr = "intent://map/geocoder?address="
						+ address
						+ "&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end";
				intent = Intent.getIntent(intentStr);

				startActivity(intent); // 启动调用

			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
			}
		} else {
			display(R.string.bdmap_not_found);
		}

	}

	private boolean isAppInstalled(String packagename) {
		PackageInfo packageInfo = null;

		try {
			packageInfo = getPackageManager().getPackageInfo(packagename, 0);
		} catch (NameNotFoundException e) {

			packageInfo = null;

		}

		if (packageInfo != null)
			return true;

		return false;
	}

	private void display(int resId) {
		Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
	}

	// prize-add-by-yanghao-20150911-end

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}
}
