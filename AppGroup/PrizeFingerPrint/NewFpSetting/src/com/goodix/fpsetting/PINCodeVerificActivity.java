package com.goodix.fpsetting;

import com.android.internal.widget.LockPatternUtils;
import com.goodix.util.ConstantUtil;
import com.goodix.util.CustomEditText;
import com.goodix.util.KeyBoardLayoutPort;
import com.goodix.util.ToastUtil;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class PINCodeVerificActivity extends BaseActivity {
	private LockPatternUtils mLockPatternUtils;
	private TextView mEntryPrompt;
	private CustomEditText mInPutEditText;
	private TextView mDigitNineView;
	private TextView mDigitEightView;
	private TextView mDigitSevenView;
	private TextView mDigitSixView;
	private TextView mDigitFiveView;
	private TextView mDigitFourView;
	private TextView mDigitThirdView;
	private TextView mDigitSecondView;
	private TextView mDigitFirstView;
	private TextView mDigitZeroView;
	private TextView mDigitDelView;

	private String mInitialPwd = null;
	private String mConfirmPwd = null;

	private static final int SHOW_DELAY_ONE_SECOND = 1000;
	private boolean mIsShowDelay = false;
	private boolean mIsConfirm = false;

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

	private OnClickListener keyBoardClickListener = new  OnClickListener (){
		@Override
		public void onClick(View v) {
			if(mIsShowDelay){
				return;
			}
			switch (v.getId()) {
			case R.id.digit_9:
				inPutPassword(9);
				break;
			case R.id.digit_8:
				inPutPassword(8);
				break;
			case R.id.digit_7:
				inPutPassword(7);
				break;
			case R.id.digit_6:
				inPutPassword(6);
				break;
			case R.id.digit_5:
				inPutPassword(5);
				break;
			case R.id.digit_4:
				inPutPassword(4);
				break;
			case R.id.digit_3:
				inPutPassword(3);
				break;
			case R.id.digit_2:
				inPutPassword(2);
				break;
			case R.id.digit_1:
				inPutPassword(1);
				break;
			case R.id.digit_0:
				inPutPassword(0);
				break;
			case R.id.digit_del:
				delelePassword();
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSubContentView(R.layout.activity_pin_code_verific);
		displayBackButton();
		hideRightClickArea();

		Intent intent = getIntent();
		int mSetOrConfirmCode = intent.getIntExtra(ConstantUtil.SETTING_CONFIRM_STYLE, 0);
		switch (mSetOrConfirmCode) {
		case ConstantUtil.PIN_CIPHER_SETTING:
			setTitleHeaderText(getResources().getString(R.string.setting_cipher));
			mIsConfirm = false;
			break;
		case ConstantUtil.PIN_CIPHER_CONFIRM:
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

		mInPutEditText = (CustomEditText) findViewById(R.id.input_edittext);

		mDigitNineView   =  (TextView) findViewById(R.id.digit_9);
		mDigitEightView  =  (TextView) findViewById(R.id.digit_8);
		mDigitSevenView  =  (TextView) findViewById(R.id.digit_7);
		mDigitSixView    =  (TextView) findViewById(R.id.digit_6);
		mDigitFiveView   =  (TextView) findViewById(R.id.digit_5);
		mDigitFourView   =  (TextView) findViewById(R.id.digit_4);
		mDigitThirdView  =  (TextView) findViewById(R.id.digit_3);
		mDigitSecondView =  (TextView) findViewById(R.id.digit_2);
		mDigitFirstView  =  (TextView) findViewById(R.id.digit_1);
		mDigitZeroView   =  (TextView) findViewById(R.id.digit_0);
		mDigitDelView    =  (TextView) findViewById(R.id.digit_del);

		mDigitNineView.setOnClickListener(keyBoardClickListener);
		mDigitEightView.setOnClickListener(keyBoardClickListener); 
		mDigitSevenView.setOnClickListener(keyBoardClickListener); 
		mDigitSixView.setOnClickListener(keyBoardClickListener);   
		mDigitFiveView.setOnClickListener(keyBoardClickListener);  
		mDigitFourView.setOnClickListener(keyBoardClickListener);  
		mDigitThirdView.setOnClickListener(keyBoardClickListener); 
		mDigitSecondView.setOnClickListener(keyBoardClickListener);
		mDigitFirstView.setOnClickListener(keyBoardClickListener); 
		mDigitZeroView.setOnClickListener(keyBoardClickListener);  
		mDigitDelView.setOnClickListener(keyBoardClickListener);
	}

	private void inPutPassword(int number){
		mInPutEditText.append(String.valueOf(number));
		int length = mInPutEditText.length();
		if(length >= 4 && mInitialPwd == null){
			String content = mInPutEditText.getText().toString();
			if(mIsConfirm){
				if (mLockPatternUtils.checkPassword(content)) {
					setResult(ConstantUtil.PIN_VERIFIC_OK_CODE);
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
				mLockPatternUtils.saveLockPassword(mConfirmPwd, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
				setResult(ConstantUtil.PIN_VERIFIC_OK_CODE);
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

	private void delelePassword() {
		int length = mInPutEditText.length();
		if(length > 0){
			mInPutEditText.getText().delete(length-1, length);
		}	
	}
}
