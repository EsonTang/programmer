package com.goodix.fpsetting;

import java.util.ArrayList;
import java.util.List;

import com.goodix.util.ConstantUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class RelatedManagerActivity extends BaseActivity {

	private ListView mOperationListView;
	private OperationListViewAdapter mOperationListViewAdapter;
	private final List<String> mOperationNameList = new ArrayList<String>();
	private LinearLayout mDeleteSetLockLl;

	private static final int APP_MANAGER_INDEX = 0;
	private static final int FP_SETTING_INDEX = 1;
	private static final String APPS_MANAGER_ACTION = "com.goodix.fp.action.VIEW";
	private static final String APPS_MANAGER_CATEGORY = "com.goodix.fp.category.appsmanager";
	private static final String FP_SETTIONG_ACTION = "com.goodix.fp.setting.Settings.ACTION";

	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(RelatedManagerActivity.this,EntryManageVerificActivity.class);
			intent.putExtra(ConstantUtil.INTENT_TYPE, ConstantUtil.DEVICE_LOCK_DELETE_TYPE);
			startActivity(intent);
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSubContentView(R.layout.activity_related_manager);
		displayBackButton();
		hideRightClickArea();
		setTitleHeaderText(getResources().getString(R.string.fingerprint));

		initData();
		initView();
	}

	private void initData() {
		String managerName = getResources().getString(R.string.app_lock);
		String settingName = getResources().getString(R.string.fingerprint_manager);
		mOperationNameList.add(managerName);
		mOperationNameList.add(settingName);
	}

	private void initView() {
		mOperationListView = (ListView) findViewById(R.id.list_view);
		mOperationListViewAdapter = new OperationListViewAdapter(this, mOperationNameList);
		mOperationListView.setAdapter(mOperationListViewAdapter);
		mOperationListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case APP_MANAGER_INDEX:
					Intent intent_manger = new Intent(APPS_MANAGER_ACTION);
					intent_manger.addCategory(APPS_MANAGER_CATEGORY);
					startActivity(intent_manger);
					break;
				case FP_SETTING_INDEX:
					Intent settingIntent = new Intent(FP_SETTIONG_ACTION);
					startActivity(settingIntent);
					break;
				}
			}
		});
		mDeleteSetLockLl = (LinearLayout) findViewById(R.id.delete_all_setting_lock_ll);
		mDeleteSetLockLl.setOnClickListener(mClickListener);
	}
}
