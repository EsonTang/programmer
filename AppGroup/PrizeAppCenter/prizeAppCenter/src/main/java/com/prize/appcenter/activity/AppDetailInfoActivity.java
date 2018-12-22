package com.prize.appcenter.activity;

import android.os.Bundle;

import com.prize.app.net.datasource.base.DetailApp;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.widget.AppDetailView;
import com.tencent.stat.StatService;

/**
 * 
 * @author huanglingjun
 * 
 */
public class AppDetailInfoActivity extends ActionBarNoTabActivity {
	private AppDetailView mAppDetailView;
	private DetailApp mData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_info);
		WindowMangerUtils.changeStatus(getWindow());
		mData = (DetailApp) getIntent().getSerializableExtra("AppDetailData");
		mAppDetailView = (AppDetailView) findViewById(R.id.appDetailView);
		if (mData != null) {
			super.setTitle(mData.name);
		}
		mAppDetailView.setData(mData, false);
	}

	@Override
	public String getActivityName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StatService.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		StatService.onPause(this);
	}

}
