package com.goodix.fpsetting;

import java.util.ArrayList;

import com.goodix.model.AppLockEvent;
import com.goodix.model.Appinfo;
import com.goodix.util.ConstantUtil;
import com.goodix.util.DataUtil;
import com.goodix.util.FpModel;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AppLockManagerActivity extends BaseActivity {

	private ListView mListView;
	private ArrayList<Appinfo> mApps = new ArrayList<Appinfo>();
	private AppsAdapter mAdapter;

	private DataUtil mDateManager;
	private TextView mAppLockPswSetButton;
	private RelativeLayout mAppLockPswSetRl;
	
	private OnClickListener mAllLockClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.app_lock_psw_setting_rl:
				mAppLockPswSetButton.setPressed(true);
				Intent intent = new Intent(AppLockManagerActivity.this,AppLockPswManagerActivity.class);
				startActivity(intent);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setSubContentView(R.layout.activity_app_lock_manager);
		displayBackButton();
		setTitleHeaderText(getResources().getString(R.string.app_lock));

		initData();
		initView();
	}
	
	@Override
	protected void onResume() {
		mAppLockPswSetButton.setPressed(false);
		super.onResume();
	}

	private void initData() {
		FpModel mfp = new FpModel(this);
		mApps = mfp.getAppsList();
	}

	private void initView() {
		mAppLockPswSetRl = (RelativeLayout) findViewById(R.id.app_lock_psw_setting_rl);
		mAppLockPswSetButton = (TextView) findViewById(R.id.app_lock_psw_setting);
		mAppLockPswSetRl.setOnClickListener(mAllLockClickListener);

		mDateManager = new DataUtil(this);
		mListView = (ListView) findViewById(R.id.apps_view);
		mAdapter = new AppsAdapter(this, mApps, mDateManager);
		mListView.setAdapter(mAdapter);
	}
}
