package com.prize.weather.menu;

import android.content.Intent;

import com.prize.weather.R;
import com.prize.weather.WeatherHomeActivity;
import com.prize.weather.framework.BaseActivity;

/**
 * 
 * @author wangzhong
 *
 */
public class WeatherMenuActivity extends BaseActivity {

	@Override
	public void initInfo() {
		
	}

	@Override
	public void initView() {
		setContentView(R.layout.weather_menu_home);
		
	}

	@Override
	public void initData() {
		
	}

	@Override
	public void onBDLocationFinishedListener() {
		
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent iHome = new Intent(this, WeatherHomeActivity.class);
		startActivity(iHome);
        WeatherMenuActivity.this.finish(); 
	}


}
