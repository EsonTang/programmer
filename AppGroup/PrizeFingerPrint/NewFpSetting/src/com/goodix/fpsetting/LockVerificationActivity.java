package com.goodix.fpsetting;

import java.util.ArrayList;

import com.goodix.aidl.IVerifyCallback;
import com.goodix.application.FpApplication;
import com.goodix.application.FpApplication.ServiceConnectCallback;
import com.goodix.database.FpDbOperarionImpl;
import com.goodix.device.MessageType;
import com.goodix.model.AppLockInfo;
import com.goodix.service.FingerprintManager;
import com.goodix.service.FingerprintManager.VerifySession;
import com.goodix.service.LockScreenService;
import com.goodix.util.ConstantUtil;
import com.goodix.util.DataUtil;
import com.goodix.util.L;
import com.goodix.util.ToastUtil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LockVerificationActivity extends BaseActivity implements ServiceConnectCallback{
	public static final String CHECK_PASSWORD        = "check.password";
	public static final String CHECK_PASSWORD_RESULT = "check.password.result";
	public static final String CHANGE_PASSWORD       = "change.password";
	public static final String CHECK_RETURN          = "check.return";
	public static final String START_TYPE_KEY        = "start_type";

	private String START_TYPE;
	private InputState mInputState = InputState.INPUT_NO_INPUT;

	private RelativeLayout mFPUnlockRl;
	private static TextView mFpPromptFirstPromptView;
	private static TextView mFpPromptSecondPromptView;
	private TextView mUsePasswordUnlockButton;
	private RelativeLayout mPasswordUnlockRl;
	private TextView mPasswordErrorView;
	private TextView mFirstPasswordImageView;
	private TextView mSecondPasswordImageView;
	private TextView mThirdPasswordImageView;
	private TextView mFourPasswordImageView;
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
	private static ImageView mFingerImageView;
	private ArrayList<Integer> mPasswordList = new ArrayList<Integer>();

	private static final int MSG_VERIFY_SUCCESS = 1;

	private static final int MSG_VERIFY_FAILED = 2;

	private static final int MSG_VERIFY_NO_ENROLL = 3;

	public static final int SHOW_DELAY = 200;

	public static final int SHOW_DELAY_ONE_SECOND = 1000;

	private int mPswErrorCount = 0;

	private VerifySession mVerifySession;
	private MyHandler mHandler = new MyHandler(this);
	private static String mAlreadyLockedAppPkg;
	private Vibrator vibrator;
	private long[] pattern = {50,100};

	private FpDbOperarionImpl mDao;
	private int mLockInfoCount;
	private AppLockInfo mAppLockInfo;
	private boolean mIsHasLockInfo;

	public enum InputState{
		INPUT_NO_INPUT,
		INPUT_NORMAL_INPUT, 
		INPUT_OLD_PASSWORD, 
		INPUT_NEW_PASSWORD_PRE, 
		INPUT_NEW_PASSWORD_AG
	}

	private OnClickListener keyBoardClickListener = new  OnClickListener (){
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.digit_9:
				mPasswordList.add(9);
				break;
			case R.id.digit_8:
				mPasswordList.add(8);
				break;
			case R.id.digit_7:
				mPasswordList.add(7);
				break;
			case R.id.digit_6:
				mPasswordList.add(6);
				break;
			case R.id.digit_5:
				mPasswordList.add(5);
				break;
			case R.id.digit_4:
				mPasswordList.add(4);
				break;
			case R.id.digit_3:
				mPasswordList.add(3);
				break;
			case R.id.digit_2:
				mPasswordList.add(2);
				break;
			case R.id.digit_1:
				mPasswordList.add(1);
				break;
			case R.id.digit_0:
				mPasswordList.add(0);
				break;
			case R.id.digit_del:
				int lastIndex = mPasswordList.size()-1;
				if(!mPasswordList.isEmpty()){
					mPasswordList.remove(lastIndex);
				}else{
					setPasswordImageReduction();
					mPasswordErrorView.setText(getResources().getString(R.string.please_input_password));
				}
				break;
			}
			setPasswordImage();
			int passwordLength = mPasswordList.size();
			if(passwordLength == 4){
				capturePassword();
			}else if(passwordLength == 1){
				mPasswordErrorView.setText(getResources().getString(R.string.please_input_password));
			}
		}
	};

	private IVerifyCallback mVerifyCallback = new IVerifyCallback.Stub() {
		@Override
		public boolean handleMessage(int msg, int arg0, int arg1, byte[] data)
				throws RemoteException {
			Log.d("FpSetting", "PasswordActivity IVerifyCallback msg = " + MessageType.getString(msg));
			if (msg == MessageType.MSG_TYPE_RECONGNIZE_SUCCESS) {
				vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(pattern,-1);
				mHandler.sendMessage(mHandler.obtainMessage(MSG_VERIFY_SUCCESS,msg, arg0, arg1));
			} else if (msg == MessageType.MSG_TYPE_RECONGNIZE_FAILED) {
				vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(pattern,-1);
				mHandler.sendMessage(mHandler.obtainMessage(MSG_VERIFY_FAILED,msg, arg0, arg1));
			} else if (msg == MessageType.MSG_TYPE_RECONGNIZE_NO_REGISTER_DATA) {
				mHandler.sendMessage(mHandler.obtainMessage(MSG_VERIFY_NO_ENROLL, msg, arg0, arg1));
			}
			return false;
		}
	};
	private FpApplication mApplication;
	private FingerprintManager mManager;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setSubContentView(R.layout.activity_lock_verification);
		displayBackButton();
		setTitleHeaderText(getResources().getString(R.string.app_lock));

		initData();
		loadView();
		initActivity();     

		mApplication = FpApplication.getInstance();

//		Intent intent2 = new Intent(LockVerificationActivity.this,LockScreenService.class);
//		startService(intent2);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(mApplication.isFpServiceManagerEmpty()){
			mApplication.setCallback(this);
			mManager = mApplication.getFpServiceManager();
		}else{
			mManager = mApplication.getFpServiceManager();
			if (null == mVerifySession) {
				mVerifySession = mManager.newVerifySession(mVerifyCallback);
			}
			mVerifySession.enter();
		}
	}

	private void initData() {
		mDao = FpDbOperarionImpl.getInstance(this);
		Cursor cursor = mDao.query(ConstantUtil.APP_LOCK_INFO_TB_NAME, null, null, null, null);
		if(null != cursor){
			mLockInfoCount = cursor.getCount();
		}
		if(null != cursor && cursor.moveToNext()){
			int id = cursor.getInt(cursor.getColumnIndex(AppLockInfo.ID));
			String info = cursor.getString(cursor.getColumnIndex(AppLockInfo.INFO));
			mAppLockInfo = new AppLockInfo(id, info);
		}
		if(null != cursor){
			cursor.close();
		}
		mIsHasLockInfo = mLockInfoCount > 0?true:false;
	}

	private static class MyHandler extends Handler {

		private Context mContext;

		public MyHandler(Context context) {
			mContext = context;
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_VERIFY_SUCCESS:
				if (msg.arg2 > 0) {
					if(null != mAlreadyLockedAppPkg && mAlreadyLockedAppPkg.length()>0){
						DataUtil dataUtil = new DataUtil(mContext);
						dataUtil.setAppAlreadyUnLocked(mAlreadyLockedAppPkg);
					}
					((LockVerificationActivity)mContext).finish();
				}
				break;
			case MSG_VERIFY_FAILED:
				mFingerImageView.setBackgroundResource(R.drawable.b_20);
				mFpPromptSecondPromptView.setText(R.string.fp_verify_fail);

				postDelayed(new Runnable() {
					public void run() { 
						mFingerImageView.setBackgroundResource(R.drawable.b_00);
						mFpPromptFirstPromptView.setText(R.string.has_set_fp);
						mFpPromptSecondPromptView.setText(R.string.use_fp_to_correction);
					}
				}, SHOW_DELAY_ONE_SECOND);
				break;
			case MSG_VERIFY_NO_ENROLL:

				break;
			default:
				break;
			}
		}
	}

	@Override
	protected void onDestroy() {
		//		if (null != mVerifySession && VerifySession.isEntry) {
		//			mVerifySession.exit();
		//		}
		if (null != mVerifySession) {
			mVerifySession.exit();
		}
		super.onDestroy();
	}

	private void initActivity() {
		Intent intent = getIntent();
		mAlreadyLockedAppPkg = intent.getStringExtra("Already_Locked_App_Pkg");
		START_TYPE = intent.getStringExtra(START_TYPE_KEY);
		if (null == START_TYPE) {
			START_TYPE = CHECK_PASSWORD;
			mInputState = InputState.INPUT_NORMAL_INPUT;
		}else{
			finish();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void loadView() {

		mFPUnlockRl = (RelativeLayout) findViewById(R.id.fingerprint_unlock_rl);
		mFpPromptFirstPromptView =  (TextView) findViewById(R.id.fp_finger_prompt_first);
		mFpPromptSecondPromptView =  (TextView) findViewById(R.id.fp_finger_prompt_second);
		mUsePasswordUnlockButton = (TextView) findViewById(R.id.password_unlock_button);

		mPasswordUnlockRl = (RelativeLayout) findViewById(R.id.password_unlock_rl);
		mPasswordErrorView =  (TextView) findViewById(R.id.password_error_prompt);

		mFirstPasswordImageView =  (TextView) findViewById(R.id.first_password);
		mSecondPasswordImageView =  (TextView) findViewById(R.id.second_password);
		mThirdPasswordImageView =  (TextView) findViewById(R.id.third_password);
		mFourPasswordImageView =  (TextView) findViewById(R.id.four_password);
		mFingerImageView = (ImageView) findViewById(R.id.fingerprint_image);

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

		mUsePasswordUnlockButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHandler.postDelayed(new Runnable() {
					public void run() { 
						if(!mIsHasLockInfo){
							ToastUtil.showToast(LockVerificationActivity.this, getResources().getString(R.string.not_set_app_lock_psw));
							return;
						}
						if (null != mVerifySession) {
							mVerifySession.exit();
						}
						mFPUnlockRl.setVisibility(View.GONE);
						mPasswordUnlockRl.setVisibility(View.VISIBLE);
						Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(LockVerificationActivity.this, 
								R.anim.move_right_in);
						mPasswordUnlockRl.startAnimation(hyperspaceJumpAnimation);
					}
				}, SHOW_DELAY);
			}
		});

	}

	private void setPasswordImage(){
		int passwordLength = mPasswordList.size();
		switch (passwordLength) {
		case 0:
			mFirstPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
			mSecondPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
			mThirdPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
			mFourPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
			break;
		case 1:
			mFirstPasswordImageView.setBackgroundResource(R.drawable.password_has_input);
			mSecondPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
			mThirdPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
			mFourPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
			break;
		case 2:
			mFirstPasswordImageView.setBackgroundResource(R.drawable.password_has_input);
			mSecondPasswordImageView.setBackgroundResource(R.drawable.password_has_input);
			mThirdPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
			mFourPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
			break;
		case 3:
			mFirstPasswordImageView.setBackgroundResource(R.drawable.password_has_input);
			mSecondPasswordImageView.setBackgroundResource(R.drawable.password_has_input);
			mThirdPasswordImageView.setBackgroundResource(R.drawable.password_has_input);
			mFourPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
			break;
		case 4:
			mFirstPasswordImageView.setBackgroundResource(R.drawable.password_has_input);
			mSecondPasswordImageView.setBackgroundResource(R.drawable.password_has_input);
			mThirdPasswordImageView.setBackgroundResource(R.drawable.password_has_input);
			mFourPasswordImageView.setBackgroundResource(R.drawable.password_has_input);
			break;
		}
	}

	private void setPasswordImageReduction(){
		mFirstPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
		mSecondPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
		mThirdPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
		mFourPasswordImageView.setBackgroundResource(R.drawable.password_has_not_input);
	}

	private boolean checkPassword(ArrayList<Integer> passwordList){
		int length = passwordList.size();
		boolean result = false;
		if (length > 0){
			String pwd = "";
			for (int i = 0; i < length; i++){
				pwd += passwordList.get(i);
			}

			L.v("checkPassword: password = " + pwd);
			result = pwd.equals(mAppLockInfo.getInfo());

		}
		return result;
	}

	private void capturePassword(){
		switch (mInputState){
		case INPUT_NORMAL_INPUT:
			if (checkPassword(mPasswordList)){
				mPswErrorCount=0;
				if(null != mAlreadyLockedAppPkg && mAlreadyLockedAppPkg.length()>0){
					DataUtil dataUtil = new DataUtil(this);
					dataUtil.setAppAlreadyUnLocked(mAlreadyLockedAppPkg);
					finish();
				}
			}else{
				mPswErrorCount++;
				String errorPrompt = getResources().getString(R.string.password_is_error)
						+mPswErrorCount+getResources().getString(R.string.error_time);
				mPasswordErrorView.setText(errorPrompt);
				mHandler.postDelayed(new Runnable() {
					public void run() { 
						mPasswordList.clear();
						setPasswordImageReduction();
						mPasswordErrorView.setText(getResources().getString(R.string.please_input_password));
					}
				}, SHOW_DELAY_ONE_SECOND);
			}
			break;
		default:
			break;
		}
	}

	private void backToHome() {
		Intent home = new Intent(Intent.ACTION_MAIN);
		home.addCategory(Intent.CATEGORY_HOME);
		startActivity(home);
		try {
			Thread.sleep(300);
		} catch (Exception e) {
		} finally {
			finish();
		}
	}

	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
		if (keyCode == KeyEvent.KEYCODE_BACK) {  
			backToHome();
			return true;  
		}  
		return super.onKeyDown(keyCode, event);  
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.left_click_area:
			backToHome();
			break;
		default:
			super.onClick(view);
			break;
		}
	}

	@Override
	public void serviceConnect() {
		mManager = FpApplication.getInstance().getFpServiceManager();
		if (null == mVerifySession) {
			mVerifySession = mManager.newVerifySession(mVerifyCallback);
		}
		mVerifySession.enter();
	}
}
