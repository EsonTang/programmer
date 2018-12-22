package com.prize.applock.fingerprintapplock;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.VectorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.provider.Settings;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import com.prize.applock.fingerprintapplock.view.IndicatorDots;
import com.prize.applock.fingerprintapplock.view.PinLockListener;
import com.prize.applock.fingerprintapplock.view.PinLockView;

import android.app.Activity;

import android.net.Uri;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.ContentResolver;
import android.content.ContentValues;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.app.Instrumentation;
import android.content.ClipData;

import android.content.pm.PackageManager;
import android.app.IActivityManager;
import android.app.ActivityManagerNative;

import android.view.Window;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.graphics.Color;
import android.provider.Settings;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView.OnEditorActionListener;
import android.text.TextWatcher;
import android.widget.EditText;
import android.text.Editable;
import android.view.inputmethod.InputMethodManager;
import android.text.InputType;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockPatternView.Cell;
import static com.android.internal.widget.LockPatternView.DisplayMode;
import java.util.List;

public class LockUI extends Activity 
	implements FingerprintCallback.Callback, OnClickListener ,OnEditorActionListener,TextWatcher{

    static final String TAG = "PrizeAppLock";
	
    Intent mIntent, intent;
    Bundle mBundle;
    private String pkgName;
    private String className;
    private String appName;
	private int type;

    Button bt;
    FingerprintManager.CryptoObject mCryptoObject;
    FingerprintCallback mFingerprintCallback;
    FingerprintManager mFingerprintManager;
    private static final String KEY_NAME = "my_key";
    ComponentName mComponentName;

    ContentResolver mResolver;

    ////wangzhong
    private static final int MSG_NOTICE_COMPLETE = 189;
    private static final int MSG_NOTICE_ERROE = 190;
    private static final int MSG_NOTICE_ERROE_TO_FINGERPRINT = 200;

    private final String FINGERPTINT_APP_LOCK = "prize.fingerprint.applock";
 
    private InputMethodManager mImm;
    //head
    private String mDisplayTitle;
    private TextView tv_header;

    //complex input view
    private EditText mComplexEdit;
    private ImageView mComplexLine;

    //num input view
    private View mNumParent;
    private IndicatorDots indicator_dots;
    private PinLockView pin_lock_view;
    private Button btn_delete;

    //pattern input view
    private LockPatternView mLockPatternView;

    //cancel button
    private TextView tv_cancle;
    //fingerprint view
    private ImageView fingerprint_status;
    //bkground
    private LinearLayout blur_bg;    

    private boolean mIsUerFinger = false;
    private String mApplockCipher;
    private int      mAppLockPwdType = -1;
    //the type define in PrizeAppLockCipherMetaData
    public static final int CIPHER_TYPE_NUM = 0;
    public static final int CIPHER_TYPE_COMPLEX = 1;
    public static final int CIPHER_TYPE_PATTERN = 2;
	
    private String whereApplockCipher = "cipher_status" + " =?";//effective.
    private String[] selectionArgsApplockCipher = new String[]{"1"};
    private Uri uriApplockCipher = Uri.parse("content://com.android.settings.provider.fpdata.share/lock_app_cipher_data");


    
    private AnimatedVectorDrawable animation_fingerprint_draw_on;
    private Drawable drawable_fingerprint_draw_on;
    private AnimatedVectorDrawable animation_fingerprint_draw_off;
    private Drawable drawable_fingerprint_draw_off;

    private AnimatedVectorDrawable animation_fingerprint_fp_to_error;
    private Drawable drawable_fingerprint_fp_to_error;
    private AnimatedVectorDrawable animation_fingerprint_error_to_fp;
    private Drawable drawable_fingerprint_error_to_fp;

    private void initStatusBar() {
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
				|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED /*| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON*/);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	 initStatusBar();

        setContentView(R.layout.activity_lock_ui);

        mFingerprintManager = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintCallback = new FingerprintCallback(mFingerprintManager, getApplicationContext(), this);
        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
        mIntent = getIntent();
        mBundle = mIntent.getExtras();
        
        pkgName = (String) mIntent.getStringExtra("block_pkgname");
	 mResultCode = mIntent.getIntExtra("block_resultcode",-1);
	 Log.d("fingerprintlock","pkgName:"+pkgName+",mResultCode:"+mResultCode);
	 try{
		appName = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkgName, 0)).toString();
	 }catch(Exception e){
		Log.e("FingerprintAppLock", "appName can't be resolve!!");
	 }
	 mDisplayTitle = "\""+appName + "\"" + getString(R.string.header_text);  


        //get password & type
        mResolver = getContentResolver();        
        if (null != mResolver) {
            Cursor cursorApplockCipher = null;
            try {
                cursorApplockCipher = mResolver.query(uriApplockCipher, null, whereApplockCipher, selectionArgsApplockCipher, null);
                if (null != cursorApplockCipher && cursorApplockCipher.getCount() > 0) {
                    while (cursorApplockCipher.moveToNext()) {
                        mApplockCipher = cursorApplockCipher.getString(cursorApplockCipher.getColumnIndex("cipher_1"));
			    mAppLockPwdType = cursorApplockCipher.getInt(cursorApplockCipher.getColumnIndex("cipher_type"));
                        Log.d(TAG, "mApplockCipher : " + mApplockCipher+",mAppLockPwdType:"+mAppLockPwdType);
                    }
                }
		  else
		  {
		  	Log.e(TAG,"get cipher :"+mApplockCipher);
		  }
            } catch (Exception e) {

            } finally{
                if (null != cursorApplockCipher) {
                    cursorApplockCipher.close();
                }
            }
        }
	 //get usefingerprint flag
	 mIsUerFinger = Settings.System.getInt(getContentResolver(),"applock_usefinger",0) == 1?true:false;
	 boolean ishasfingerprint = mFingerprintManager.hasEnrolledFingerprints();
	 if(!ishasfingerprint)mIsUerFinger = false;
	 
	 initDrawableAndAnimation();
        initView();
    }

    @Override
	protected void onResume(){
		super.onResume();
              Log.d(TAG,"onResume...");
		new Thread(new Runnable() {
            @Override
            public void run() {
                /*try {
                    Thread.sleep(250);
					mFingerprintCallback.startListening();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                mCancelTimes = 0;
                if(mIsUerFinger)
		  {
			mFingerprintCallback.startListening();
                }
            }
        }).start();
        startAnimationFPDrawOn();
	 if(mAppLockPwdType == CIPHER_TYPE_COMPLEX)
	 {
	 	int currentType = mComplexEdit.getInputType();
              mComplexEdit.setInputType(mAppLockPwdType == CIPHER_TYPE_COMPLEX ? currentType
                    : (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD));
	 }
    }
    /*@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
	 if(mAppLockPwdType == CIPHER_TYPE_COMPLEX && hasFocus)
	 {
	 	mComplexEdit.post(new Runnable() {
                @Override
                public void run() {                    
                    mImm.showSoftInput(mComplexEdit, InputMethodManager.SHOW_IMPLICIT);
                }
            });
	 }
    }*/
    private void startAnimationFPDrawOn() {
        if (null != fingerprint_status && null != animation_fingerprint_draw_on && null != drawable_fingerprint_draw_on) {
            //fingerprint_status.setImageResource(R.drawable.status_fingerprint);
            fingerprint_status.setImageDrawable(drawable_fingerprint_draw_on);
            animation_fingerprint_draw_on.start();
        }
    }

    private void startAnimationFPDrawOff() {
        if (null != fingerprint_status && null != animation_fingerprint_draw_off && null != drawable_fingerprint_draw_off) {
            fingerprint_status.setImageDrawable(drawable_fingerprint_draw_off);
            animation_fingerprint_draw_off.start();
        }
    }

    private void startAnimationFP2Error() {
        if (null != fingerprint_status && null != animation_fingerprint_fp_to_error && null != drawable_fingerprint_fp_to_error) {
            //fingerprint_status.setImageResource(R.drawable.status_fingerprint_error);
            fingerprint_status.setImageDrawable(drawable_fingerprint_fp_to_error);
            animation_fingerprint_fp_to_error.start();

            Message msg = mHandler.obtainMessage(MSG_NOTICE_ERROE_TO_FINGERPRINT);
            mHandler.removeMessages(MSG_NOTICE_ERROE_TO_FINGERPRINT);
            mHandler.sendMessageDelayed(msg, 1000);
        }
    }

    private void startAnimationError2FP() {
        if (null != fingerprint_status && null != animation_fingerprint_error_to_fp && null != drawable_fingerprint_error_to_fp) {
            fingerprint_status.setImageDrawable(drawable_fingerprint_error_to_fp);
            animation_fingerprint_error_to_fp.start();
        }
    }

    /**
     * init the drawable and animation.
     */
    private void initDrawableAndAnimation() {
        drawable_fingerprint_draw_on = getDrawable(R.drawable.applock_fingerprint_draw_on_animation);
        animation_fingerprint_draw_on = drawable_fingerprint_draw_on instanceof AnimatedVectorDrawable ?
                (AnimatedVectorDrawable) drawable_fingerprint_draw_on : null;
        drawable_fingerprint_draw_off = getDrawable(R.drawable.applock_fingerprint_draw_off_animation);
        animation_fingerprint_draw_off = drawable_fingerprint_draw_off instanceof AnimatedVectorDrawable ?
                (AnimatedVectorDrawable) drawable_fingerprint_draw_off : null;

        drawable_fingerprint_fp_to_error = getDrawable(R.drawable.applock_fingerprint_fp_to_error_state_animation);
        animation_fingerprint_fp_to_error = drawable_fingerprint_fp_to_error instanceof AnimatedVectorDrawable ?
                (AnimatedVectorDrawable) drawable_fingerprint_fp_to_error : null;
        drawable_fingerprint_error_to_fp = getDrawable(R.drawable.applock_fingerprint_error_state_to_fp_animation);
        animation_fingerprint_error_to_fp = drawable_fingerprint_error_to_fp instanceof AnimatedVectorDrawable ?
                (AnimatedVectorDrawable) drawable_fingerprint_error_to_fp : null;
    }

    private void initView() {
       //num mode
	 mNumParent = findViewById(R.id.applock_num_parent);
	 pin_lock_view = (PinLockView) findViewById(R.id.pin_lock_view);
	 indicator_dots = (IndicatorDots) findViewById(R.id.indicator_dots);        
        btn_delete = (Button) findViewById(R.id.btn_delete);
        pin_lock_view.attachIndicatorDots(indicator_dots);
        pin_lock_view.attachDeleteButton(btn_delete);
        pin_lock_view.setPinLockListener(mPinLockListener);

	 //complex
	 mComplexEdit = (EditText)findViewById(R.id.applock_complex);
	 mComplexLine = (ImageView)findViewById(R.id.applock_complex_line);
	 mComplexEdit.setOnEditorActionListener(this);
        mComplexEdit.addTextChangedListener(this);

	 //pattern
	 mLockPatternView = (LockPatternView) findViewById(R.id.lockPattern); 
	 mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
		 
	 if(mAppLockPwdType == CIPHER_TYPE_NUM)
	 {
		mNumParent.setVisibility(View.VISIBLE);
		pin_lock_view.setVisibility(View.VISIBLE);

		mComplexEdit.setVisibility(View.GONE);
		mComplexLine.setVisibility(View.GONE);
		
		mLockPatternView.setVisibility(View.GONE);
	 }
	 else if(mAppLockPwdType == CIPHER_TYPE_COMPLEX)
	 {
	 	mComplexEdit.setVisibility(View.VISIBLE);
		mComplexLine.setVisibility(View.VISIBLE);
		
		mNumParent.setVisibility(View.GONE);
		pin_lock_view.setVisibility(View.GONE);

		mLockPatternView.setVisibility(View.GONE);
	 }
	 else if(mAppLockPwdType == CIPHER_TYPE_PATTERN)
	 {
	 	mLockPatternView.setVisibility(View.VISIBLE);
		
	 	mComplexEdit.setVisibility(View.GONE);
		mComplexLine.setVisibility(View.GONE);
		
		mNumParent.setVisibility(View.GONE);
		pin_lock_view.setVisibility(View.GONE);		
	 }

	 //header	 
        tv_header = (TextView) findViewById(R.id.tv_header);
	  tv_header.setText(mDisplayTitle);
	  
        //bkground
        blur_bg = (LinearLayout) findViewById(R.id.blur_bg);		

	 //cancel and fingerprint
	 tv_cancle = (TextView) findViewById(R.id.tv_cancle);
	 tv_cancle.setOnClickListener(this);
	 
        fingerprint_status = (ImageView) findViewById(R.id.fingerprint_status);	 
        fingerprint_status.setOnClickListener(this); 
	 if(!mIsUerFinger)
	 {
	 	fingerprint_status.setVisibility(View.GONE);
	 }
    }
    
    /*prize-add by lihuangyuan,for fingerapplock-2017-07-04-start*/
    private void gointoApplication()
    {        	 	 
        //hide input method
        if(mAppLockPwdType == CIPHER_TYPE_COMPLEX)
        {
            mImm.hideSoftInputFromWindow(mComplexEdit.getWindowToken(),0);
        }
	 int startMode = mIntent.getIntExtra("startMode",0);
	 Log.i("fingerprintlock","gointoApplication mResultCode:"+mResultCode+",startMode:"+startMode);	 
	 if(startMode == 1)//for android-N start from systemui recenttask
	 {
	 	int taskId = mIntent.getIntExtra("taskId",-1);
		if(mBundle == null)
		{
			Log.i("fingerprintlock","startMode:"+startMode+",taskId:"+taskId+",mBundle is null1");
			//ActivityOptions options = ActivityOptions.makeBasic();
			mBundle = new Bundle(); 
		}
		mBundle.putShort("fromSource",(short)1);
		mIam = ActivityManagerNative.getDefault();
		try {	            
	            mIam.startActivityFromRecents(taskId, mBundle);
	        } catch (Exception e) {
	            Log.e("fingerprintlock", " Failed to dock task: " + taskId );
	        }
		return;
	 }
	 if(mResultCode > 0)//for android6.0 start from systemui recenttask
	 {
	 	setResult(mResultCode, mIntent);
	 	finish();
	 }
	 else
	 {
	 	Intent new_intent = (Intent)mBundle.getParcelable("srcintent");
	 	startActivity(new_intent);	
		finish();
	 }
    }    
    private int mResultCode;    
    IActivityManager mIam;
    

    private void toLauncher() {
        Intent iHome = new Intent();
        iHome.setComponent(new ComponentName("com.android.launcher3", "com.android.launcher3.Launcher"));
        iHome.addCategory(Intent.CATEGORY_HOME);
        iHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(iHome);
        finish();
    }

    private void updatePinLockViewDelayed(int what, String pin, int delay) {
        Message msg = mHandler.obtainMessage(what, pin);
        mHandler.removeMessages(what);
        mHandler.sendMessageDelayed(msg, delay);
    }

    private PinLockListener mPinLockListener = new PinLockListener() {

        @Override
        public void onComplete(String pin) {
            Log.d(TAG, "Pin complete:  pin : " + pin);
	     checkPassword(pin,true); 
        }

        @Override
        public void onEmpty() {
            //Log.d("john", "Pin empty");
        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {
            //Log.d("john", "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
        }
    };

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_NOTICE_COMPLETE) {
                gointoApplication();
            } else if (msg.what == MSG_NOTICE_ERROE) {
            
            } else if (msg.what == MSG_NOTICE_ERROE_TO_FINGERPRINT) {
                tv_header.setText(mDisplayTitle);
                startAnimationError2FP();
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancle) {		
		finish();   
        }else if (v.getId() == R.id.fingerprint_status) {
            startAnimationFPDrawOn();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause...");
        mCancelTimes = 2;
        mFingerprintCallback.stopListening();
	 //hide input method
        if(mAppLockPwdType == CIPHER_TYPE_COMPLEX)
        {
            mImm.hideSoftInputFromWindow(mComplexEdit.getWindowToken(),0);
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            mFingerprintCallback.stopListening();
	     //hide input method
            if(mAppLockPwdType == CIPHER_TYPE_COMPLEX)
            {
                mImm.hideSoftInputFromWindow(mComplexEdit.getWindowToken(),0);
            }
            finish();
        }
	 else
	 {
 	 }
        return super.onKeyDown(keyCode, event);
    }

    ///////////////////////////////////////////////////////
    @Override
    public void onAuthenticated() {
        startAnimationFPDrawOff();
        gointoApplication();
    }

    @Override
    public void onFailed() {
        Log.d(TAG, "onFailed");
        tv_header.setText(R.string.fingerprint_error_status_unknown);
        startAnimationFP2Error();
    }
    private int mCancelTimes = 0;
    @Override
    public void onError(int errorCode,CharSequence errString) {
        Log.d(TAG, "onError  " + errString + ",errorcode:"+errorCode);
	 if(errorCode == FingerprintManager.FINGERPRINT_ERROR_CANCELED
	     && mFingerprintCallback.isListening())
	 {	 	
		if(mCancelTimes < 2)
		{
	 		Log.i(TAG,"onError FINGERPRINT_ERROR_CANCELED ,restartListening");
			mHandler.post(new Runnable() {			
				@Override
				public void run() {				
					mFingerprintCallback.startListening();
				}
			});
                    mCancelTimes ++;
			return;
		}		
		mCancelTimes ++;
	 }
        else if(errorCode == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT 
            && mFingerprintCallback.isListening())
        {
            mHandler.post(new Runnable() {			
				@Override
				public void run() {				
					mFingerprintCallback.resetErrorTimes();
				}
			});
            mHandler.postDelayed(new Runnable() {			
				@Override
				public void run() {
                                   mFingerprintCallback.startListening();
				}
			},100);
        }
        startAnimationFP2Error();
    }
    //////////////////////////////////////////////////////
	
	public  void sendKeyCode(final int keyCode){
        new Thread () {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    Log.e("Exception when sendPointerSync", e.toString());
                }
            }
        }.start();
    }
    //////////////////////////////////////////////////////////    
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
	 {
            // Check if this was the result of hitting the enter or "done" key
            if (actionId == EditorInfo.IME_NULL
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT) 
           {
                checkPassword(mComplexEdit.getText().toString(),true);		   
                return true;
            }
            return false;
        }
    public void onTextChanged(CharSequence s, int start, int before, int count){}
    public void beforeTextChanged(CharSequence s, int start, int count, int after){}
    public void afterTextChanged(Editable s) 
    {    
		if (mComplexEdit.getText().length() >= 4) 
		{
			final String strpassword = mComplexEdit.getText().toString();
			checkPassword(strpassword,false);
		}
     }
     /////////////////////////////////////////////////////////////////     
     protected LockPatternView.OnPatternListener mChooseNewLockPatternListener =
                new LockPatternView.OnPatternListener() {

                public void onPatternStart() {
                }

                public void onPatternCleared() {
                }
                @Override
                public void onPatternDetected(List<LockPatternView.Cell> pattern) {                     
		      {
		      		String strpattern = LockPatternUtils.patternToString(pattern);
				checkPassword(strpattern,true);				
                    }
                }
                @Override
                public void onPatternCellAdded(List<Cell> pattern) {

                }
               
         };	 
	 /////////////////////////////////////////////////////////////
	 private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 1000;
	 private final Runnable mResetErrorRunnable = new Runnable() 
	    {
		        @Override
		        public void run() 
		        {
		            if(mAppLockPwdType == CIPHER_TYPE_NUM)
			     {
		            		tv_header.setText(mDisplayTitle);
	                   		pin_lock_view.resetPinLockView();
		            }
			      else if(mAppLockPwdType == CIPHER_TYPE_COMPLEX)
			      {
			    	   	tv_header.setText(mDisplayTitle);
				   	mComplexEdit.setText("");
			      }
			      else if(mAppLockPwdType == CIPHER_TYPE_PATTERN)
			      	{			      	       
			      		mLockPatternView.clearPattern();
					mLockPatternView.setDisplayMode(DisplayMode.Correct);
					mLockPatternView.enableInput();
					tv_header.setText(mDisplayTitle);
			      	}
		        }
	    };
	    public void checkPassword(String password,boolean isShowError)
		 {	     
			 Log.d(TAG,"checkPassword password:"+password+",saved:"+password);
			 if(password.equals(mApplockCipher))
			 {
			 	 //show correct
			 	 if(mAppLockPwdType == CIPHER_TYPE_NUM)
			 	{		 	    
			 	    	tv_header.setText(R.string.cipher_input_status_correct);
			 	}
				else if(mAppLockPwdType == CIPHER_TYPE_COMPLEX)
				{
					tv_header.setText(R.string.cipher_input_status_correct);
				}
				else if(mAppLockPwdType == CIPHER_TYPE_PATTERN)
				{
				      tv_header.setText(R.string.lockpattern_need_to_unlock_right);
				}
				mHandler.sendEmptyMessageDelayed(MSG_NOTICE_COMPLETE, 100);
			 }
			 else
			 {
			 	    if(!isShowError)
					 return;
	                         //show error
				    if(mAppLockPwdType == CIPHER_TYPE_NUM)
			 	    {		 	    
			 	    	tv_header.setText(R.string.cipher_input_status_error);
					pin_lock_view.updatePinLockViewError();
			 	    }
				    else if(mAppLockPwdType == CIPHER_TYPE_COMPLEX)
				    {
				    	 tv_header.setText(R.string.cipher_input_status_error);
				    }
				    else if(mAppLockPwdType == CIPHER_TYPE_PATTERN)
				    {
				    	 tv_header.setText(R.string.lockpattern_need_to_unlock_wrong);
					 mLockPatternView.setDisplayMode(DisplayMode.Wrong);
				    }

				    //reset input state
				    mLockPatternView.disableInput();
				    mHandler.removeCallbacks(mResetErrorRunnable);
				    mHandler.postDelayed(mResetErrorRunnable, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
			 }
		 }

}
