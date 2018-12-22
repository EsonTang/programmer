package com.goodix.fpsetting;

import java.util.Timer;
import java.util.TimerTask;

import com.android.internal.widget.LockPatternUtils;
import com.goodix.util.ConstantUtil;
import com.goodix.util.CustomEditText;
import com.goodix.util.ToastUtil;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

public class ComplexCodeVerificActivity extends BaseActivity {
	private LockPatternUtils mLockPatternUtils;
	private TextView mEntryPrompt;
	private CustomEditText mInPutEditText;

	private String mInitialPwd = null;
	private String mConfirmPwd = null;

	private static final int SHOW_DELAY_ONE_SECOND = 1000;
	private boolean mIsShowDelay = false;
	private boolean mIsConfirm = false;
	private Timer mTimer = new Timer();
	private TextView mInPutCancelBt;
	private TextView mInPutConfirmBt;

	private Handler mPromptHandler = new Handler(){
		public void handleMessage(Message msg) {
			mInPutEditText.getText().clear();
			mEntryPrompt.setText(getResources().getString(R.string.confirm_password));
			mIsShowDelay = false;
		};
	};

	private Runnable mPromptRunnable = new Runnable(){
		public void run() {
			mPromptHandler.sendEmptyMessage(0);
		};
	};

	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mIsShowDelay){
				return;
			}
			switch (v.getId()) {
			case R.id.input_confirm_button:
				inPutConfirm();
				break;
			case R.id.input_cancel_button:
				finish();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSubContentView(R.layout.activity_complex_code_verific);
		displayBackButton();
		hideRightClickArea();

		Intent intent = getIntent();
		int mSetOrConfirmCode = intent.getIntExtra(ConstantUtil.SETTING_CONFIRM_STYLE, 0);
		switch (mSetOrConfirmCode) {
		case ConstantUtil.COMPLEX_CIPHER_SETTING:
			setTitleHeaderText(getResources().getString(R.string.setting_cipher));
			mIsConfirm = false;
			break;
		case ConstantUtil.COMPLEX_CIPHER_CONFIRM:
			mIsConfirm = true;
			setTitleHeaderText(getResources().getString(R.string.confirm_cipher));
			break;
		default:
			finish();
			break;
		}
		mLockPatternUtils = new LockPatternUtils(this);
		initView();
	}
	
	@Override
	protected void onPause() {
		ToastUtil.cancelToast();
		super.onPause();
	}

	private void initView() {
		mEntryPrompt = (TextView) findViewById(R.id.entry_prompt);
		if(mIsConfirm){
			mEntryPrompt.setText(getResources().getString(R.string.please_enter_password));
		}else{
			mEntryPrompt.setText(getResources().getString(R.string.please_set_password));
		}

		mInPutCancelBt = (TextView) findViewById(R.id.input_cancel_button);
		mInPutConfirmBt = (TextView) findViewById(R.id.input_confirm_button);
		mInPutCancelBt.setOnClickListener(mClickListener);
		mInPutConfirmBt.setOnClickListener(mClickListener);

		mInPutEditText = (CustomEditText) findViewById(R.id.input_edittext);
		mTimer.schedule(new TimerTask(){
			public void run(){
				InputMethodManager inputManager =
						(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(mInPutEditText, 0);
			}
		},0);
	}

	private void inPutConfirm(){
		int length = mInPutEditText.length();
		if(length >= 4 && mInitialPwd == null){
			String content = mInPutEditText.getText().toString();
			if(mIsConfirm){
				if (mLockPatternUtils.checkPassword(content)) {
					setResult(ConstantUtil.COMPLEX_VERIFIC_OK_CODE);
					finish();
					return;
				} 
			}else{
				mInitialPwd = content;
				mPromptHandler.postDelayed(mPromptRunnable, SHOW_DELAY_ONE_SECOND);
				mIsShowDelay = true;
			}
		}else if(length >= 4 && mInitialPwd != null && mConfirmPwd == null){
			String content = mInPutEditText.getText().toString();
			mConfirmPwd = content;
			if(mInitialPwd.equals(mConfirmPwd)){
				mLockPatternUtils.saveLockPassword(mConfirmPwd, DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC);
				setResult(ConstantUtil.COMPLEX_VERIFIC_OK_CODE);
				ToastUtil.showToast(this, getResources().getString(R.string.is_consistent));
				finish();
				return;
			}else{
				mInitialPwd = null;
				mConfirmPwd = null;
				mInPutEditText.getText().clear();
				ToastUtil.showToast(this, getResources().getString(R.string.is_not_consistent));
			}
		}
	}
}
