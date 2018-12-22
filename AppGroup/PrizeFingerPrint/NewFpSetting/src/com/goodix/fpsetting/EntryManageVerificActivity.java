package com.goodix.fpsetting;

import java.util.ArrayList;
import java.util.List;

import com.android.internal.widget.LockPatternUtils;
import com.goodix.util.ConstantUtil;
import com.goodix.util.ToastUtil;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class EntryManageVerificActivity extends BaseActivity {

	private final List<String> mOperationNameList = new ArrayList<String>();
	private ListView mOperationListView;
	private OperationListViewAdapter mOperationListViewAdapter;
	private LockPatternUtils mLockPatternUtils;
	private int mIntentType = 0;
	private static final int ENTRY_PIN_CIPHER_INDEX = 0;
	private static final int ENTRY_MIXED_CIPHER_INDEX = 1;
	private static final int ENTRY_GRAPHICS_CIPHER_INDEX = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSubContentView(R.layout.activity_entry_manage_verific);
		displayBackButton();
		hideRightClickArea();
		setTitleHeaderText(getResources().getString(R.string.choose_entry_verific_mode));

		initData();
		initView();
	}
	
	@Override
	protected void onPause() {
		ToastUtil.cancelToast();
		super.onPause();
	}

	private void initData() {
		Intent intent = getIntent();
		mIntentType  = intent.getIntExtra(ConstantUtil.INTENT_TYPE, 0);
		mLockPatternUtils = new LockPatternUtils(this);
		String pinCodeName = getResources().getString(R.string.pin_cipher);
		String mixedCipherName = getResources().getString(R.string.mixed_cipher);
		String graphicsCipherName = getResources().getString(R.string.graphics_cipher);
		mOperationNameList.add(pinCodeName);
		mOperationNameList.add(mixedCipherName);
		mOperationNameList.add(graphicsCipherName);
	}

	private void initView() {
		mOperationListView = (ListView) findViewById(R.id.list_view);
		mOperationListViewAdapter = new OperationListViewAdapter(this, mOperationNameList);
		mOperationListView.setAdapter(mOperationListViewAdapter);
		mOperationListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Intent intent = null;
				String className = null;
				switch (position) {
				case ENTRY_PIN_CIPHER_INDEX:
					isNeedDelete();
					intent = new Intent(EntryManageVerificActivity.this,PINCodeVerificActivity.class);
					intent.putExtra(ConstantUtil.SETTING_CONFIRM_STYLE, ConstantUtil.PIN_CIPHER_SETTING);
					startActivityForResult(intent, ConstantUtil.PIN_REQUEST_CODE);
					break;
				case ENTRY_MIXED_CIPHER_INDEX:
					isNeedDelete();
					intent = new Intent(EntryManageVerificActivity.this,ComplexCodeVerificActivity.class);
					intent.putExtra(ConstantUtil.SETTING_CONFIRM_STYLE, ConstantUtil.COMPLEX_CIPHER_SETTING);
					startActivityForResult(intent,ConstantUtil.COMPLEX_REQUEST_CODE);
					break;
				case ENTRY_GRAPHICS_CIPHER_INDEX:
					isNeedDelete();
					className = "com.android.settings.ChooseLockPattern";
					ChooseLockPattern(DevicePolicyManager.PASSWORD_QUALITY_SOMETHING,className);
					break;
				}
			}
		});
	}
	
	private void ChooseLockPattern(int requestCode,String className){
		Intent patternIntent = new Intent();
		patternIntent.setClassName("com.android.settings", className);
		startActivityForResult(patternIntent,requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Intent intent = new Intent(EntryManageVerificActivity.this,RelatedManagerActivity.class);
		switch (requestCode) {
		case ConstantUtil.PIN_REQUEST_CODE:
		case ConstantUtil.COMPLEX_REQUEST_CODE:
			if(resultCode == ConstantUtil.PIN_VERIFIC_OK_CODE||resultCode == ConstantUtil.COMPLEX_VERIFIC_OK_CODE){
				startActivity(intent);
			}else{
				ToastUtil.showToast(this, getResources().getString(R.string.validation_failure));
			}
			break;
		case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
			if(resultCode == -1){
				startActivity(intent);
			}
			break;
		}
		finish();
	}
	
	private void isNeedDelete(){
		if(mIntentType == ConstantUtil.DEVICE_LOCK_DELETE_TYPE){
			deleteDeviceLockPsw();
		}
	}
	
	private void deleteDeviceLockPsw(){
		mLockPatternUtils.clearLock(false);
		mLockPatternUtils.setLockScreenDisabled(true);
	}
}
