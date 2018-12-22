package com.prize.factorytest.FingerPrint;

import java.io.FileInputStream;  
import java.io.IOException;  
import java.util.List;  
import java.util.Timer;  
import java.util.TimerTask;  
  
import android.R.color;  
import android.app.Activity;  
import android.app.AlertDialog;  
import android.content.Context;  
import android.content.DialogInterface;  
import android.content.Intent;  
import android.graphics.Color;  
import android.os.Bundle;  
import android.os.CancellationSignal;  
import android.os.Handler;  
import android.os.Message;  
import android.text.Spannable;  
import android.text.style.ForegroundColorSpan;  
import android.util.Log;  
import android.view.KeyEvent;  
import android.view.View;  
import android.view.View.OnClickListener;  
import android.widget.Button;  
import android.widget.ImageView;  
import android.widget.ProgressBar;  
import android.widget.TableRow;  
import android.widget.TextView;  
import android.widget.Toast;  
  
import com.prize.factorytest.R;
import android.hardware.fingerprint.Fingerprint;  
import android.hardware.fingerprint.FingerprintManager;  
import android.os.CancellationSignal;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.RelativeLayout;
import android.os.SystemProperties;
import android.os.UserHandle;

import android.provider.Settings;
import android.util.Base64;

  
public class FingerPrintActivity extends BaseActivity{  
	private static final String TAG = "FactoryTest-FingerprintActivity";

	private static final long CANCEL_TIME_INTERVAL = 30000;
	private static final long RELEASE_TIME_INTERVAL = 100;

	private ImageView mPhoneImage;
	private TextView mTitleNoticeTxt;
	private TextView mSubInfoTxt;
	private TextView mSubInfoTxtOutside;

	private final int[] printImages = new int[] { R.drawable.b_20,
							R.drawable.b_10,
							R.drawable.b_01
			};
	private ImageView mGuideAnimationView;
	private RelativeLayout mGuideRl;
	private RelativeLayout mRegisterLl;
	private AnimationDrawable mGuideAnim;
   
    private FingerprintManager mFingerprintManager;
	private CancellationSignal mEnrollmentCancel;
	private byte[] mToken = new byte[69];
	private int mEnrollmentSteps = -1;  
    private int mEnrollmentRemaining = 0;
	private boolean mEnrolling;
	private boolean mDone;
	private int mEnrollSteps = 0;
	private int mUserId;
	
	private FingerprintManager.EnrollmentCallback mEnrollmentCallback  
            = new FingerprintManager.EnrollmentCallback() {  
  
        @Override  
        public void onEnrollmentProgress(int remaining) {  
			if(View.VISIBLE == mGuideRl.getVisibility()){
					mGuideRl.setVisibility(View.GONE);
					mGuideAnim.stop();
					mRegisterLl.setVisibility(View.VISIBLE);
				}
            if (mEnrollmentSteps == -1) {  
                mEnrollmentSteps = remaining;  
            }
			mEnrollSteps++;
			if(mEnrollSteps<1 || mEnrollSteps>3){
				return;
			}
			mPhoneImage.setBackgroundResource(printImages[3-mEnrollSteps]);			
			if(mEnrollSteps == 3){
				mEnrollmentCancel.cancel();
				mEnrollmentSteps = -1;
				SystemProperties.set("persist.sys.prize_fp_enable", "1");
				finish();				
			}

        }  
  
        @Override  
        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {  

        }  
  
        @Override  
        public void onEnrollmentError(int errMsgId, CharSequence errString) {

        }  
    };  
      
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        		super.onCreate(savedInstanceState);

		setSubContentView(R.layout.activity_register);
		displayBackButton();
		setTitleHeaderText(getResources().getString(R.string.register_title));

		initView();
		mUserId = getIntent().getIntExtra(Intent.EXTRA_USER_ID, UserHandle.myUserId());
    }  
        
    @Override  
    protected void onStart() {  
        super.onStart();  
		//Log.d(TAG, " onStart  mToken length="+mToken.length);  
		mFingerprintManager = (FingerprintManager) this.getSystemService(  
                Context.FINGERPRINT_SERVICE);
				
		mEnrollmentCancel = new CancellationSignal();		
        if(mFingerprintManager != null){
            long challenge = mFingerprintManager.preEnroll();
            
            Log.i(TAG, "mToken challenge="+Long.toHexString(challenge));
            for (int i = 0; i < mToken.length; i++) {
    		    mToken[i] = 0;
    	    }
            mToken[33] = 2;
            int challenge_h = (int) (challenge >> 32);
            int challenge_l = (int) (challenge & 0xFFFFFFFF);
            Log.i(TAG, "mToken challenge_h="+ Integer.toHexString(challenge_h));
            Log.i(TAG, "mToken challenge_l="+ Integer.toHexString(challenge_l));
            
            mToken[8] = (byte) (challenge_h >> 24);
            mToken[7] = (byte) ((challenge_h & 0xFF0000) >> 16);
            mToken[6] = (byte) ((challenge_h & 0xFF00) >> 8);
            mToken[5] = (byte) (challenge_h & 0xFF);
            
            mToken[4] = (byte) (challenge_l >> 24);
            mToken[3] = (byte) ((challenge_l & 0xFF0000) >> 16);
            mToken[2] = (byte) ((challenge_l & 0xFF00) >> 8);
            mToken[1] = (byte) (challenge_l & 0xFF); 
			mFingerprintManager.enroll(mToken, mEnrollmentCancel, 0 /* flags */, mUserId, mEnrollmentCallback);
        }  		

    }  
      
    @Override  
    protected void onStop() {  
        super.onStop();  
		mEnrollmentCancel.cancel();
		mEnrollSteps = 0;
		mEnrollmentSteps = -1;
		finish();
    }  
  
    @Override  
    protected void onResume() {  
        super.onResume();  
    }  
      
    @Override  
    protected void onPause() {  
        super.onPause(); 
    }  
	
	private void initView() {
		mGuideRl = (RelativeLayout) findViewById(R.id.guide_animation_rl);
		mRegisterLl = (RelativeLayout) findViewById(R.id.register_rl);

		mGuideAnimationView = (ImageView)findViewById(R.id.guide_animation_view);
		mGuideAnimationView.setBackgroundResource(R.drawable.guide_animation);
		mGuideAnim = (AnimationDrawable) mGuideAnimationView.getBackground();
		mGuideAnim.start();

		mPhoneImage = (ImageView) findViewById(R.id.register_phone);
		mTitleNoticeTxt = (TextView) findViewById(R.id.title_notice_text);

		mSubInfoTxt = (TextView) findViewById(R.id.register_sub_info);

		mSubInfoTxt.setText(getString(R.string.guide_notice));

		mSubInfoTxtOutside = (TextView) findViewById(R.id.register_sub_info_outside);
	}
	/*PRIZE-liyu-for masking the back key-2017-01-20-start*/
	@Override
    public void onBackPressed() {
        //super.onBackPressed();
		Log.i(TAG, "in FingerPrintActivity back press!");
    }
  	/*PRIZE-liyu-for masking the back key-2017-01-20-end*/
}
