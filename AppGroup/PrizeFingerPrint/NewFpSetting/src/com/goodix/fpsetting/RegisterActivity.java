package com.goodix.fpsetting;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goodix.aidl.IEnrollCallback;
import com.goodix.application.FpApplication;
import com.goodix.application.FpApplication.ServiceConnectCallback;
import com.goodix.device.MessageType;
import com.goodix.service.FingerprintManager;
import com.goodix.service.FingerprintManager.EnrollSession;
import com.goodix.util.Fingerprint;
import com.goodix.util.L;
import com.goodix.util.ToastUtil;
import com.goodix.aidl.IUpdateBaseCallback;

public class RegisterActivity extends BaseActivity implements ServiceConnectCallback{
	private static final long CANCEL_TIME_INTERVAL = 30000;
	private static final long RELEASE_TIME_INTERVAL = 100;
	private static final String TAG = "RegisterActivity";
	private ImageView mPhoneImage;
	private TextView mTitleNoticeTxt;
	private TextView mSubInfoTxt;
	private TextView mSubInfoTxtOutside;
	private Handler mCancelHandler;
	private Runnable mCancelRunable;
	private Handler mReleaseFingerHandler;
	private ReleaseTouchRunnable mReleaseFingerRunable;
	private int mPercent = 0;
	private EnrollSession mSession;

	private ArrayList<Fingerprint> mDataList = null;

	private PowerManager mPowerManager;

	private final int[] printImages = new int[] { R.drawable.b_01, R.drawable.b_02,
			R.drawable.b_03, R.drawable.b_04, R.drawable.b_05,
			R.drawable.b_06, R.drawable.b_07, R.drawable.b_08,
			R.drawable.b_09, R.drawable.b_10, R.drawable.b_11,
			R.drawable.b_12, R.drawable.b_13, R.drawable.b_14,
			R.drawable.b_15, R.drawable.b_16, R.drawable.b_17,
			R.drawable.b_17, R.drawable.b_18, R.drawable.b_19,
			R.drawable.b_20};
	private WakeLock mTimeoutWakeLock;
	private ImageView mGuideAnimationView;
	private RelativeLayout mGuideRl;
	private RelativeLayout mRegisterLl;
	private AnimationDrawable mGuideAnim;
	private RegisterHandler mHandler = new RegisterHandler();
	private FpApplication mApplication;
	private FingerprintManager mManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSubContentView(R.layout.activity_register);
		displayBackButton();
		setTitleHeaderText(getResources().getString(R.string.register_title));

		initView();
		startCancelTimer();
		mDataList = getIntent().getParcelableArrayListExtra(TouchIDActivity.FRINGERPRINT_INDEX);

		mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
		mTimeoutWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK 
				| PowerManager.ON_AFTER_RELEASE, TAG);

		mApplication = FpApplication.getInstance();
	}

	private int getFingerViewIndex(int index) {
		if (mDataList == null) {
			return index;
		}
		for (int i = 0; i < this.mDataList.size(); i++) {
			if (Integer.parseInt(this.mDataList.get(i).getUri()) == index) {
				return i + 1;
			}
		}
		return index;
	}

	@Override
	public void finish() {
		Log.v(TAG, "finish");
		super.finish();
	}

	@Override
	protected void onPause() {
		Log.v(TAG, "onPause");
		super.onPause();
		ToastUtil.cancelToast();
		mTimeoutWakeLock.release();
		finish();
	}

	@Override
	protected void onRestart() {
		Log.v(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume");
		super.onResume();
		mTimeoutWakeLock.acquire();
		//		initRegister();
		if(mApplication.isFpServiceManagerEmpty()){
			mApplication.setCallback(this);
			mManager = mApplication.getFpServiceManager();
		}else{
			mManager = mApplication.getFpServiceManager();
			if (null == mSession) {
				mSession = mManager.newEnrollSession(mEnrollCallback);
			}
			mSession.enter();
		}
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy");
		if (null != mSession) {
			mSession.exit();
		}

		FpApplication.getInstance().getFpServiceManager().setUpdateBaseCallback(null);

		cancelCancelTimer();
		super.onDestroy();
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

		mSubInfoTxt.setText(getStyle(String.format(getString(R.string.capture_notice_put_on_screen),getString(R.string.center_area)),getString(R.string.center_area)));

		mSubInfoTxtOutside = (TextView) findViewById(R.id.register_sub_info_outside);

		mCancelHandler = new Handler();
		mCancelRunable = new CancelRunnable();

		mReleaseFingerHandler = new Handler();
		mReleaseFingerRunable = new ReleaseTouchRunnable();
	}

	private void showTextTranslateAnim(View v, int animID, int visible) {
		Animation animation = AnimationUtils.loadAnimation(this, animID);
		animation.setAnimationListener(new TitleExitAnimListener(v, visible));
		v.startAnimation(animation);
	}

	private void startCancelTimer() {
		if (null != mCancelHandler && null != mCancelRunable) {
			mCancelHandler.postDelayed(mCancelRunable, CANCEL_TIME_INTERVAL);
		}
	}

	private void cancelCancelTimer() {
		Log.v(TAG, "cancelCancelTimer");
		if (null != mCancelHandler && null != mCancelRunable) {
			mCancelHandler.removeCallbacks(mCancelRunable);
			mCancelRunable = null;
			mCancelHandler = null;
		}
	}

	private void resetCancelTimer() {
		Log.v(TAG, "resetCancelTimer");
		if (null != mCancelHandler && null != mCancelRunable) {
			mCancelHandler.removeCallbacks(mCancelRunable);
			mCancelHandler.postDelayed(mCancelRunable, CANCEL_TIME_INTERVAL);
		}
	}

	private void startReleaseFingerTimer() {
		Log.v(TAG, "startReleaseFingerTimer");
		if (null != mReleaseFingerHandler && null != mReleaseFingerRunable) {
			mReleaseFingerHandler.postDelayed(mReleaseFingerRunable, RELEASE_TIME_INTERVAL);
		}
	}

	private void cancelReleaseFingerTimer() {
		Log.v(TAG, "cancelReleaseFingerTimer");
		if (null != mReleaseFingerHandler && null != mReleaseFingerRunable) {
			if (true == mReleaseFingerRunable.bWarning) {
				Log.v(TAG, "Cancel Warning!");
				mReleaseFingerRunable.bWarning = false;
			}
			mReleaseFingerHandler.removeCallbacks(mReleaseFingerRunable);
		}

	}

	private void startWarning(int textID) {
		mTitleNoticeTxt.setText(textID);
	}

	private SpannableStringBuilder getStyle(String text,String keyTex) {
		int length  = 0;
		int index = 0;
		if (TextUtils.isEmpty(text) || TextUtils.isEmpty(keyTex)) {
			return null;
		}
		index = text.indexOf(keyTex);
		length = keyTex.length();
		SpannableStringBuilder style = new SpannableStringBuilder(text);
		style.setSpan(new ForegroundColorSpan(Color.RED), index, index + length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE); 
		return style;
	}

	private void startSubWarning(String text,String keyText) {
		mSubInfoTxtOutside.setVisibility(View.VISIBLE);
		mSubInfoTxtOutside.setText(getStyle(text,keyText));
		showTextTranslateAnim(mSubInfoTxtOutside, R.anim.register_title_text_enter, View.VISIBLE);
		showTextTranslateAnim(mSubInfoTxt, R.anim.register_title_text_exit, View.INVISIBLE);
	}

	private void CaptureResult(String uri) {
		if (null != mSession) {
			mSession.exit();
			mSession = null;
		}

		cancelCancelTimer();

		Intent intent = new Intent(RegisterActivity.this, TouchIDActivity.class);
		intent.putExtra(TouchIDActivity.FRINGERPRINT_URI, uri);
		setResult(RESULT_OK, intent);
		finish();
	}

	private class CancelRunnable implements Runnable {
		@Override
		public void run() {
			try {
				if (null != mSession) {
					mSession.exit();
					if(View.VISIBLE == mGuideRl.getVisibility()){
						mGuideRl.setVisibility(View.GONE);
						mGuideAnim.stop();
						mRegisterLl.setVisibility(View.VISIBLE);
					}
					mSubInfoTxt.setText(R.string.entry_timeout);
					mSession = null;
				}
				startWarning(R.string.register_register_failed);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class ReleaseTouchRunnable implements Runnable {
		public boolean bWarning = false;
		@Override
		public void run() {
			Log.v(TAG, "ReleaseTouchRunnable:Run...");
			bWarning = true;
			startWarning(R.string.register_notice_the_hand);
		}
	}

	private class TitleExitAnimListener implements AnimationListener {
		int visible;
		private WeakReference<View> mViewReference;

		public TitleExitAnimListener(View v, int visible) {
			this.visible = visible;
			mViewReference = new WeakReference<View>(v);
		}

		@Override
		public void onAnimationEnd(Animation arg0) {
			TextView textView = (TextView) mViewReference.get();
			if (null != textView) {
				textView.setVisibility(visible);
			}
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
		}

		@Override
		public void onAnimationStart(Animation arg0) {
		}
	}

	private IEnrollCallback mEnrollCallback = new IEnrollCallback.Stub() {
		@Override
		public boolean handleMessage(int msg, int arg0, int arg1, byte[] data) throws RemoteException {
			Log.v(TAG, String.format("msg = %d , arg0 = %d ,arg1 = %d", msg, arg0, arg1));
			mHandler.sendMessage(mHandler.obtainMessage(msg, arg1, arg0, data));
			return false;
		}
	};

	private IUpdateBaseCallback mUpdateaBaseCallback = new IUpdateBaseCallback.Stub() {
		@Override
		public void updated() {
			Log.d(TAG, "base updated!!!!");
			if (mSession != null) {
				mSession.enter();
			}
		}
	};
	public   void setDialogProgress(int progress) {
		if (progress >= 0 && progress <= 100) {
			mPhoneImage.setBackgroundResource(printImages[progress/5]);
		}
	}

	private class RegisterHandler extends Handler {
		private final static int MAX_ACTION_ERROR = 4;
		private int mNoPieceTime = 0;

		private boolean bHasShowNoPieceDialog = false;

		private boolean bHasShowAni = false;

		private boolean bToutch = false;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageType.MSG_TYPE_COMMON_NOTIFY_INFO :
				Object obj = msg.obj;
				if (obj != null) {
					byte[] loginfo = (byte[]) obj;
					String str = new String(loginfo);
					int index = str.indexOf("=", str.indexOf("=") + 1);

					if (-1 != index) {
						String fileName = null;
						if (index != -1) {
							fileName = str.substring(index + 1, str.length() - 1);
							File file = new File(fileName);
							try {
								InputStream in = new FileInputStream(file);
								Bitmap map = BitmapFactory.decodeStream(in);

								in.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				break;
			case MessageType.MSG_TYPE_REGISTER_DUPLICATE_REG :
				L.d("index == " + getFingerViewIndex(msg.arg1));
				ToastUtil.showToast(RegisterActivity.this, R.string.register_duplicate);
				break;
			case MessageType.MSG_TYPE_REGISTER_PIECE :
			case MessageType.MSG_TYPE_REGISTER_NO_PIECE :
			case MessageType.MSG_TYPE_REGISTER_NO_EXTRAINFO :
			case MessageType.MSG_TYPE_REGISTER_LOW_COVER :
			case MessageType.MSG_TYPE_REGISTER_GET_DATA_FAILED :
			case MessageType.MSG_TYPE_REGISTER_BAD_IMAGE :
				Log.v(TAG, "RegisterHandler: Result");
				if(View.VISIBLE == mGuideRl.getVisibility()){
					mGuideRl.setVisibility(View.GONE);
					mGuideAnim.stop();
					mSubInfoTxt.setText(getStyle(String.format(getString(R.string.capture_notice_put_on_screen),getString(R.string.center_area)),getString(R.string.center_area)));
					mRegisterLl.setVisibility(View.VISIBLE);
				}

				Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
				vib.vibrate(100);
				if (msg.what == MessageType.MSG_TYPE_REGISTER_PIECE || msg.what == MessageType.MSG_TYPE_REGISTER_NO_PIECE) {
					mPercent = msg.arg2;
				}
				if (msg.what == MessageType.MSG_TYPE_REGISTER_BAD_IMAGE || msg.what == MessageType.MSG_TYPE_REGISTER_GET_DATA_FAILED) {
					if (bToutch == true) {
						ToastUtil.showToast(RegisterActivity.this, R.string.register_bad_images);
					} else {
						ToastUtil.showToast(RegisterActivity.this, R.string.register_notice_steady);
					}
				} else if (msg.what != MessageType.MSG_TYPE_REGISTER_BAD_IMAGE && msg.what != MessageType.MSG_TYPE_REGISTER_GET_DATA_FAILED && msg.what != MessageType.MSG_TYPE_COMMON_TOUCH
						&& msg.what != MessageType.MSG_TYPE_COMMON_UNTOUCH) {
				}

				if (msg.what == MessageType.MSG_TYPE_REGISTER_NO_EXTRAINFO) {
					ToastUtil.showToast(RegisterActivity.this, R.string.register_no_move);
				} else if (msg.what != MessageType.MSG_TYPE_REGISTER_NO_EXTRAINFO && msg.what != MessageType.MSG_TYPE_COMMON_TOUCH && msg.what != MessageType.MSG_TYPE_COMMON_UNTOUCH) {
				}
				if (msg.what == MessageType.MSG_TYPE_REGISTER_NO_PIECE) {
					mNoPieceTime++;
					if (mNoPieceTime >= MAX_ACTION_ERROR && bHasShowNoPieceDialog == false) {
						ToastUtil.showToast(RegisterActivity.this, R.string.register_no_piece_together);
						bHasShowNoPieceDialog = true;
						mNoPieceTime = 0;
					}
				} else if (msg.what != MessageType.MSG_TYPE_REGISTER_NO_PIECE && msg.what != MessageType.MSG_TYPE_COMMON_TOUCH && msg.what != MessageType.MSG_TYPE_COMMON_UNTOUCH) {
					mNoPieceTime = 0;
				}
				int index = msg.arg1;
				setDialogProgress(mPercent);

				Log.v(TAG, "RegisterHandler: mPercent" + mPercent);
				if (mPercent >= 100) {
					try {
						mSession.save(index);
					} catch (Exception e) {
						e.printStackTrace();
					}
					cancelReleaseFingerTimer();
					cancelCancelTimer();
					mSubInfoTxt.setText(R.string.register_complete_infomation);
					CaptureResult(Integer.toString(index));
				} else {
					if (mPercent >= 70 && bHasShowAni == false) {
						bHasShowAni = true;
						startSubWarning(String.format(getResources().getString(R.string.capture_notice_put_on_screen_outside),
								getResources().getString(R.string.outside_area)),getResources().getString(R.string.outside_area));
					}
					if (bToutch == true) {
						startReleaseFingerTimer();
					}
				}
				break;
			case MessageType.MSG_TYPE_COMMON_TOUCH :
				Log.v(TAG, "RegisterHandler:MSG_TYPE_COMMON_TOUCH");
				bToutch = true;
				resetCancelTimer();

				break;
			case MessageType.MSG_TYPE_COMMON_UNTOUCH :
				bToutch = false;
				Log.v(TAG, "RegisterHandler:MSG_TYPE_COMMON_UNTOUCH");
				cancelReleaseFingerTimer();
				break;

			default :
				break;
			}
		}
	}

	@Override
	public void serviceConnect() {
		mManager = FpApplication.getInstance().getFpServiceManager();
		if (null == mSession) {
			mSession = mManager.newEnrollSession(mEnrollCallback);
		}
		mSession.enter();
	}
}
