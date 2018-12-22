package com.prize.left.page.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.prize.simple.model.DataDeals;
import com.android.prize.simple.ui.SimpleFrame;
import com.prize.left.page.util.ToastUtils;

public class TestActivity extends Activity {

	DataDeals dataDeal = null;
	
	SimpleFrame mFrame;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_page_lay);
		
		initView();
	}
	
	private void initView() {/*
		
		mFrame = (SimpleFrame)findViewById(R.id.simple_frame);
		
		dataDeal = new DataDeals(this);
		
		
		mFrame.setActivity(this);
		
		
		com.android.prize.simple.ui.SimplePageView pagedView = (com.android.prize.simple.ui.SimplePageView)findViewById(R.id.paged_view);
		pagedView.syncPages();
	*/}
	
	public void onClick(View v) {/*
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_add:
			dataDeal.loadFavorites(R.xml.simple_default_data);
			ToastUtils.showToast(this, "加载完成");
			break;
		case R.id.btn_del:
			mFrame.setVisibility(View.GONE);
			break;
		case R.id.btn_refresh:
			mFrame.setVisibility(View.VISIBLE);
			ToastUtils.showToast(this, "数据size:" + dataDeal.queryAllDatas().size());
			break;
		}
	*/}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == Launcher.REQ_CONTACT_CODE && resultCode == RESULT_OK) {
			mFrame.onActivityResult(requestCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	@Override
	protected void onDestroy() {
		//mFrame.destroy();
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		// mFrame.onMenu();
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		mFrame.onMenu();
		return super.onPrepareOptionsMenu(menu);
	}
}
