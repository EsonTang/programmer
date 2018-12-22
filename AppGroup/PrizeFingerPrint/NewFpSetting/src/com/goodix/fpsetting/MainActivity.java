package com.goodix.fpsetting;

import java.util.ArrayList;
import java.util.List;

import com.android.internal.widget.LockPatternUtils;
import com.goodix.model.FpFunctionDescription;
import com.goodix.model.FpFunctionItem;
import com.goodix.util.ConstantUtil;
import com.goodix.util.ToastUtil;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends BaseActivity {
	private static String TAG = "MainActivity";
	private final List<FpFunctionDescription> mFpFunctionNameList = new ArrayList<FpFunctionDescription>();
	private ListView mFpFunctionListView;
	private FpFunctionListViewAdapter mFpFunctionListViewAdapter;
	private ListView mFunctionListView;
	private List<FpFunctionDescription> mFunctionNameList = new ArrayList<FpFunctionDescription>();

	private FpManagerListViewAdapter mFunctionListViewAdapter;
	private LockPatternUtils mLockPatternUtils;
	private int mUserId;
	private int mLockPatternMode;

	private static final int FP_MANAGER_INDEX = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setSubContentView(R.layout.activity_main);
		hideBackButton();
		hideRightClickArea();
		setMidTitleText(getResources().getString(R.string.fingerprint));

		initData();

		mFunctionListView = (ListView) findViewById(R.id.list_view);
		mFunctionListViewAdapter = new FpManagerListViewAdapter(this, mFunctionNameList);
		mFunctionListView.setAdapter(mFunctionListViewAdapter);
		mFunctionListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Intent intent = null;
				String className = null;
				Log.d(TAG,"onItemClick() mLockPatternMode =" + mLockPatternMode + ",position = " + position);
				switch (position) {
				case FP_MANAGER_INDEX:
					switch (mLockPatternMode) {
					case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
					case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX:
						intent = new Intent(MainActivity.this,PINCodeVerificActivity.class);
						intent.putExtra(ConstantUtil.SETTING_CONFIRM_STYLE, ConstantUtil.PIN_CIPHER_CONFIRM);
						startActivityForResult(intent,ConstantUtil.PIN_REQUEST_CODE);
						break;
					case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
					case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
					case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
						intent = new Intent(MainActivity.this,ComplexCodeVerificActivity.class);
						intent.putExtra(ConstantUtil.SETTING_CONFIRM_STYLE, ConstantUtil.COMPLEX_CIPHER_CONFIRM);
						startActivityForResult(intent,ConstantUtil.COMPLEX_REQUEST_CODE);
						break;
					case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
						className = "com.android.settings.ConfirmLockPattern";
						ConfirmLockPattern(DevicePolicyManager.PASSWORD_QUALITY_SOMETHING,className);
						break;
					case 0:
						intent = new Intent(MainActivity.this,EntryManageVerificActivity.class);
						startActivity(intent);
						break;
					}
					break;
				}
			}
		});

		mFpFunctionListView = (ListView) findViewById(R.id.fp_function_list_view);
		mFpFunctionListViewAdapter = new FpFunctionListViewAdapter(this, mFpFunctionNameList);
		mFpFunctionListView.setAdapter(mFpFunctionListViewAdapter);
	}
	
	@Override
	protected void onPause() {
		ToastUtil.cancelToast();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mLockPatternUtils = new LockPatternUtils(this);
		mUserId = mLockPatternUtils.getCurrentUser();
		mLockPatternMode = mLockPatternUtils.getKeyguardStoredPasswordQuality(mUserId);
		super.onResume();
	}

	private void initData() {
		String managerName = getResources().getString(R.string.fingerprint_manager);
		String managerInstruction = getResources().getString(R.string.fingerprint_manager_instruction);
		String managerDbColumnName = null;
		FpFunctionDescription managerDescription = new FpFunctionDescription(managerName, managerInstruction, managerDbColumnName);
		mFunctionNameList.add(managerDescription);

		String photoName = getResources().getString(R.string.fingerprint_photograph);
		String photoInstruction = getResources().getString(R.string.photograph_instruction);
		String photoDbColumnName = FpFunctionItem.CAMERA;
		FpFunctionDescription photoDescription = new FpFunctionDescription(photoName, photoInstruction, photoDbColumnName);

		String answerName = getResources().getString(R.string.fingerprint_answer);
		String answerInstruction = getResources().getString(R.string.answer_instruction);
		String answerDbColumnName = FpFunctionItem.ANSWER;
		FpFunctionDescription answerDescription = new FpFunctionDescription(answerName, answerInstruction, answerDbColumnName);

		mFpFunctionNameList.add(photoDescription);
		mFpFunctionNameList.add(answerDescription);
	}

	private void ConfirmLockPattern(int requestCode,String className){
		Intent patternIntent = new Intent();
		patternIntent.setClassName("com.android.settings", className);
		startActivityForResult(patternIntent,requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Intent intent = new Intent(MainActivity.this,RelatedManagerActivity.class);
		switch (requestCode) {
		case ConstantUtil.PIN_REQUEST_CODE:
		case ConstantUtil.COMPLEX_REQUEST_CODE:
			if(resultCode == ConstantUtil.PIN_VERIFIC_OK_CODE||resultCode == ConstantUtil.COMPLEX_VERIFIC_OK_CODE){
				startActivity(intent);
			}else{
				ToastUtil.showToast(this, getResources().getString(R.string.validation_failure));
			}
		case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
			if(resultCode == -1){
				startActivity(intent);
			}
			break;
		}
	}
}