package com.goodix.fpsetting;

import com.goodix.database.FpDbOperarionImpl;
import com.goodix.model.AppLockInfo;
import com.goodix.util.ConstantUtil;
import com.goodix.util.CustomEditText;
import com.goodix.util.KeyBoardLayoutPort;
import com.goodix.util.ToastUtil;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AppLockPswSettingActivity extends BaseActivity {

	public static final String TAG = "PasswordSettingActivity";

	private KeyBoardLayoutPort mKeyBoardLayoutPort;
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
	private CustomEditText mInPutEditText;
	private TextView mEntryPrompt;

	private String mInitialPwd = null;
	private String mConfirmPwd = null;

	private static final int SHOW_DELAY_ONE_SECOND = 1000;
	private boolean mIsShowDelay = false;
	private boolean mIsHasLockInfo;
	private FpDbOperarionImpl mDao;
	private int mLockInfoCount=0;
	private AppLockInfo mAppLockInfo = null;
	private boolean mPswCheckCorrect = false;

	private Handler mPromptHandler = new Handler(){
		public void handleMessage(Message msg) {
			mInPutEditText.getText().clear();
			if(mPswCheckCorrect){
				mEntryPrompt.setText(getResources().getString(R.string.please_set_password));
			}else{
				mEntryPrompt.setText(getResources().getString(R.string.confirm_password));
			}
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

		setSubContentView(R.layout.activity_password_setting);
		displayBackButton();
		hideRightClickArea();
		setTitleHeaderText(getResources().getString(R.string.fp_password_setting));
		mDao = FpDbOperarionImpl.getInstance(this);
		initData();
		initView();
	}
	
	@Override
	protected void onPause() {
		ToastUtil.cancelToast();
		super.onPause();
	}

	private void initData() {
		Cursor cursor = mDao.query(ConstantUtil.APP_LOCK_INFO_TB_NAME, null, null, null, null);
		if(null != cursor){
			mLockInfoCount = cursor.getCount();
		}
		if(null != cursor && cursor.moveToNext()){
			int id = cursor.getInt(cursor.getColumnIndex(AppLockInfo.ID));
			String info = cursor.getString(cursor.getColumnIndex(AppLockInfo.INFO));
			mAppLockInfo = new AppLockInfo(id, info);
		}
		cursor.close();
		mIsHasLockInfo = mLockInfoCount > 0?true:false;
	}

	private void initView() {
		mEntryPrompt = (TextView) findViewById(R.id.entry_prompt);

		if(mIsHasLockInfo){
			mEntryPrompt.setText(getResources().getString(R.string.please_enter_original_password));
		}else{
			mEntryPrompt.setText(getResources().getString(R.string.please_set_password));
		}

		mInPutEditText = (CustomEditText) findViewById(R.id.input_edittext);

		mKeyBoardLayoutPort =  (KeyBoardLayoutPort) findViewById(R.id.key_board_layout_port);

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
		String content = null;
		if(length >= 4 && mInitialPwd == null){
			content = mInPutEditText.getText().toString();
			if(mIsHasLockInfo){
				if (null == mAppLockInfo || !content.equals(mAppLockInfo.getInfo())) {
					ToastUtil.showToast(this, getResources().getString(R.string.wrong_password));
					finish();
					return;
				}
				if(null != mAppLockInfo && content.equals(mAppLockInfo.getInfo())){
					mPswCheckCorrect = true;
					mInitialPwd = content;
					mPromptHandler.postDelayed(mPromptRunnable, SHOW_DELAY_ONE_SECOND);
					mIsShowDelay = true;
				}
			}else{
				mInitialPwd = content;
				mPromptHandler.postDelayed(mPromptRunnable, SHOW_DELAY_ONE_SECOND);
				mIsShowDelay = true;
			}
		}else if(length >= 4 && mInitialPwd != null && mConfirmPwd == null){
			content = mInPutEditText.getText().toString();
			mConfirmPwd = content.trim();
			ContentValues values = null;
			if(mPswCheckCorrect){
				String selection = AppLockInfo.ID+"=?";
				String[] selectionArgs = new String[]{String.valueOf(mAppLockInfo.getId())};
				values = new ContentValues();
				values.put(AppLockInfo.INFO, mConfirmPwd);
				mDao.update(ConstantUtil.APP_LOCK_INFO_TB_NAME, selection, selectionArgs, values);
				ToastUtil.showToast(this, getResources().getString(R.string.is_consistent));
				finish();
			}else if(mInitialPwd.equals(mConfirmPwd)){
				values = new ContentValues();
				values.put(AppLockInfo.INFO, mConfirmPwd);
				mDao.insert(ConstantUtil.APP_LOCK_INFO_TB_NAME, values);
				ToastUtil.showToast(this, getResources().getString(R.string.is_consistent));
				finish();
				return;
			}else{
				mInitialPwd = null;
				mConfirmPwd = null;
				mInPutEditText.getText().clear();
				ToastUtil.showToast(this, getResources().getString(R.string.is_not_consistent));
				finish();
			}
		}
	}

	private void delelePassword() {
		int length = mInPutEditText.length();
		if(length > 0){
			String content = mInPutEditText.getText().toString();
			Log.d(TAG, content);
			mInPutEditText.getText().delete(length-1, length);
		}	
	}

}
