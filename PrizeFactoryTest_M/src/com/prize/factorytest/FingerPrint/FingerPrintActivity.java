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
		
		String test = Settings.System.getString(getContentResolver(),
						Settings.System.PRIZE_FINGERPRINT_TOKEN);
		if(test != null){
			mToken = Base64.decode(test.getBytes(), Base64.DEFAULT);
		}	
		mEnrollmentCancel = new CancellationSignal();
		mFingerprintManager = (FingerprintManager) this.getSystemService(  
                Context.FINGERPRINT_SERVICE);	
        mFingerprintManager.enroll(mToken, mEnrollmentCancel, 0, mEnrollmentCallback);    
    }  
        
    @Override  
    protected void onStart() {  
        super.onStart();  
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