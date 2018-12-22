package com.prize.autotest.mmi;

import com.prize.autotest.AutoConstant;
import com.prize.autotest.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.hardware.fingerprint.FingerprintManager;
import android.graphics.drawable.AnimationDrawable;
import android.widget.RelativeLayout;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;

public class AutoFingerTestActivity extends AutoFingerBaseActivity {
	private static final String TAG = "FactoryTest-FingerprintActivity";

	private ImageView mPhoneImage;
	private TextView mSubInfoTxt;

	private final int[] printImages = new int[] { R.drawable.b_20,
			R.drawable.b_10, R.drawable.b_01 };
	private ImageView mGuideAnimationView;
	private RelativeLayout mGuideRl;
	private RelativeLayout mRegisterLl;
	private AnimationDrawable mGuideAnim;

	private FingerprintManager mFingerprintManager;
	private CancellationSignal mEnrollmentCancel;
	private byte[] mToken = new byte[69];
	private int mEnrollmentSteps = -1;
	private int mEnrollSteps = 0;

	private String cmdOrder = null;
	private BroadcastReceiver mBroadcast = null;
	boolean isFingerResult = false;
	
	private FingerprintManager.EnrollmentCallback mEnrollmentCallback = new FingerprintManager.EnrollmentCallback() {

		@Override
		public void onEnrollmentProgress(int remaining) {
			if (View.VISIBLE == mGuideRl.getVisibility()) {
				mGuideRl.setVisibility(View.GONE);
				mGuideAnim.stop();
				mRegisterLl.setVisibility(View.VISIBLE);
			}
			if (mEnrollmentSteps == -1) {
				mEnrollmentSteps = remaining;
			}
			mEnrollSteps++;
			if (mEnrollSteps < 1 || mEnrollSteps > 3) {
				return;
			}
			mPhoneImage.setBackgroundResource(printImages[3 - mEnrollSteps]);
			if (mEnrollSteps == 3) {
				mEnrollmentCancel.cancel();
				mEnrollmentSteps = -1;
				SystemProperties.set("persist.sys.prize_fp_enable", "1");
				isFingerResult = true;
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
		if (test != null) {
			mToken = Base64.decode(test.getBytes(), Base64.DEFAULT);
		}
		mEnrollmentCancel = new CancellationSignal();
		mFingerprintManager = (FingerprintManager) this
				.getSystemService(Context.FINGERPRINT_SERVICE);
		mFingerprintManager.enroll(mToken, mEnrollmentCancel, 0,
				mEnrollmentCallback);
		
		mBroadcast = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AutoConstant.ACTION_UI);
		registerReceiver(mBroadcast, filter);

		Intent intent = getIntent();
		cmdOrder = intent.getStringExtra("back");
		if (cmdOrder != null) {
			new Handler().post(new Runnable() {
				public void run() {
					runCmdOrder();
				}
			});
		}		
		
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				runCmdOrder();
			}
		}
	}
	private void runCmdOrder() {
		if (cmdOrder == null || cmdOrder.length() < 1) {
			return;
		}

		String temp = cmdOrder.substring(1);
		
		if (temp.startsWith(AutoConstant.CMD_MMI_FINGER_RESULT)) {
			if(isFingerResult){
				AutoConstant.writeFile("Finger : PASS" + "\n");
				AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
						AutoFingerTestActivity.this);
			}else{
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL, this);
				AutoConstant.writeFile("Finger : FAIL" + "\n");
			}
			finish();
		}
		AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
				this);

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

		mGuideAnimationView = (ImageView) findViewById(R.id.guide_animation_view);
		mGuideAnimationView.setBackgroundResource(R.drawable.guide_animation);
		mGuideAnim = (AnimationDrawable) mGuideAnimationView.getBackground();
		mGuideAnim.start();

		mPhoneImage = (ImageView) findViewById(R.id.register_phone);
		mSubInfoTxt = (TextView) findViewById(R.id.register_sub_info);

		mSubInfoTxt.setText(getString(R.string.guide_notice));
	}

	/* PRIZE-liyu-for masking the back key-2017-01-20-start */
	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		Log.i(TAG, "in FingerPrintActivity back press!");
	}
	/* PRIZE-liyu-for masking the back key-2017-01-20-end */
}