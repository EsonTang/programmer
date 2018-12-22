package com.goodix.fpsetting;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
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

public class FingerFactoryTestActivity extends BaseActivity implements ServiceConnectCallback{
	private static final long CANCEL_TIME_INTERVAL = 30000;
	private static final long RELEASE_TIME_INTERVAL = 100;
	private static final int FORECAST_PERCENT = 7;
	private static final String TAG = "RegisterActivity";
	private ViewGroup mRootGroup;
	private ViewGroup mGuideGroup;
	private ViewGroup mRegisterGroup;
	private ImageView mPhoneImage;
	private TextView mTitleNoticeTxt;
	private TextView mSubInfoTxt;
	private TextView mSubInfoTxtOutside;
	private static Handler mCancelHandler;
	private static Runnable mCancelRunable;
	private Handler mReleaseFingerHandler;
	private ReleaseTouchRunnable mReleaseFingerRunable;
	private int mPercent = 0;
	private EnrollSession mSession;

	private AlertDialog mDialog;

	public static FingerFactoryTestActivity instance;

	private ArrayList<Fingerprint> mDataList = null;

	private int mInputFrequencyCount = 0; 

	private final int[] printImages = new int[] {R.drawable.b_01,R.drawable.b_10,R.drawable.b_20};
	private FpApplication mApplication;
	private FingerprintManager mManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSubContentView(R.layout.activity_register);
		displayBackButton();
		setTitleHeaderText(getResources().getString(R.string.register_title));

		SystemProperties.set("persist.sys.prize_fp_enable", "0");

		instance = this;
		initView();
		startCancelTimer();
		mDataList = getIntent().getParcelableArrayListExtra(TouchIDActivity.FRINGERPRINT_INDEX);

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
		ToastUtil.cancelToast();
		super.onPause();
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
		instance = null;
		if (null != mSession) {
			mSession.exit();
		}
		cancelCancelTimer();
		super.onDestroy();
	}
	private void initView() {
		mRootGroup = (ViewGroup) findViewById(R.id.register_root_group);
		//		mRootGroup.setOnClickListener(new RootGroupClickListener());
		mGuideGroup = (ViewGroup) findViewById(R.id.guide_animation_rl);
		mRegisterGroup = (ViewGroup) findViewById(R.id.register_rl);
		mGuideGroup.setVisibility(View.GONE);
		mRegisterGroup.setVisibility(View.VISIBLE);
		mPhoneImage = (ImageView) findViewById(R.id.register_phone);

		mTitleNoticeTxt = (TextView) findViewById(R.id.title_notice_text);

		mSubInfoTxt = (TextView) findViewById(R.id.register_sub_info);

		mSubInfoTxt.setText(getStyle(String.format(getString(R.string.capture_notice_put_on_screen),getString(R.string.center_area)),getString(R.string.center_area)));

		mSubInfoTxtOutside = (TextView) findViewById(R.id.register_sub_info_outside);

		mCancelHandler = new Handler();
		mCancelRunable = new CancelRunnable(this);

		mReleaseFingerHandler = new Handler();
		mReleaseFingerRunable = new ReleaseTouchRunnable(this);
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

	private class CancelRunnable implements Runnable {
		private WeakReference<FingerFactoryTestActivity> mActivityReference;

		public CancelRunnable(FingerFactoryTestActivity activity) {
			mActivityReference = new WeakReference<FingerFactoryTestActivity>(activity);
		}

		@Override
		public void run() {
			FingerFactoryTestActivity activity = (FingerFactoryTestActivity) mActivityReference.get();
			if (null != activity) {

				try {
					if (null != activity.mSession) {
						activity.mSession.exit();
					}
					startWarning(R.string.register_register_failed);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

	private class ReleaseTouchRunnable implements Runnable {
		private WeakReference<FingerFactoryTestActivity> mActivityReference;
		public boolean bWarning = false;

		public ReleaseTouchRunnable(FingerFactoryTestActivity activity) {
			mActivityReference = new WeakReference<FingerFactoryTestActivity>(activity);
		}

		@Override
		public void run() {
			Log.v(TAG, "ReleaseTouchRunnable:Run...");
			FingerFactoryTestActivity activity = (FingerFactoryTestActivity) mActivityReference.get();
			if (null != activity) {
				bWarning = true;
				startWarning(R.string.register_notice_the_hand);
			}
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

	public void setDialogProgress(int progress) {
		if (progress >= 0 && progress <= 100) {
			if(mInputFrequencyCount < 2){
				mPhoneImage.setBackgroundResource(printImages[mInputFrequencyCount]);
				mInputFrequencyCount ++;
			}
		}
	}

	private RegisterHandler mHandler = new RegisterHandler(this);

	private  class RegisterHandler extends Handler {
		private final static int MAX_ACTION_ERROR = 4;
		private int mBadImageCount = 0;
		private int mNoPieceTime = 0;
		private int mNoMoveTime = 0;

		private boolean bHasShowBadDialog = false;
		private boolean bHasShowNoPieceDialog = false;
		private boolean bHasShowMoveDialog = false;

		private boolean bHasShowAni = false;

		private boolean bToutch = false;
		private Context mContext = null;

		public RegisterHandler(Context context) {
			mContext = context;
		}

		@Override
		public void handleMessage(Message msg) {
			if (null == mContext)
				return;
			final FingerFactoryTestActivity activity = (FingerFactoryTestActivity)mContext;
			if (null == activity) {
				return;
			}

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
				L.d("index == " + activity.getFingerViewIndex(msg.arg1));
				ToastUtil.showToast(FingerFactoryTestActivity.this, R.string.register_duplicate);
				break;
			case MessageType.MSG_TYPE_REGISTER_PIECE :
			case MessageType.MSG_TYPE_REGISTER_NO_PIECE :
			case MessageType.MSG_TYPE_REGISTER_NO_EXTRAINFO :
			case MessageType.MSG_TYPE_REGISTER_LOW_COVER :
			case MessageType.MSG_TYPE_REGISTER_GET_DATA_FAILED :
			case MessageType.MSG_TYPE_REGISTER_BAD_IMAGE :
				Log.v(TAG, "RegisterHandler: Result");

				Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
				vib.vibrate(100);
				if (msg.what == MessageType.MSG_TYPE_REGISTER_PIECE || msg.what == MessageType.MSG_TYPE_REGISTER_NO_PIECE) {
					activity.mPercent = msg.arg2;
				}
				if (msg.what == MessageType.MSG_TYPE_REGISTER_BAD_IMAGE || msg.what == MessageType.MSG_TYPE_REGISTER_GET_DATA_FAILED) {
					mBadImageCount++;
					if (bToutch == true) {
						ToastUtil.showToast(FingerFactoryTestActivity.this, R.string.register_bad_images);
						bHasShowBadDialog = true;
						mBadImageCount = 0;
					} else {
						ToastUtil.showToast(FingerFactoryTestActivity.this, R.string.register_notice_steady);
					}
				} else if (msg.what != MessageType.MSG_TYPE_REGISTER_BAD_IMAGE && msg.what != MessageType.MSG_TYPE_REGISTER_GET_DATA_FAILED && msg.what != MessageType.MSG_TYPE_COMMON_TOUCH
						&& msg.what != MessageType.MSG_TYPE_COMMON_UNTOUCH) {
					mBadImageCount = 0;
				}

				if (msg.what == MessageType.MSG_TYPE_REGISTER_NO_EXTRAINFO) {
					mNoMoveTime++;
					ToastUtil.showToast(FingerFactoryTestActivity.this, R.string.register_no_move);
					bHasShowMoveDialog = true;
					mNoMoveTime = 0;
				} else if (msg.what != MessageType.MSG_TYPE_REGISTER_NO_EXTRAINFO && msg.what != MessageType.MSG_TYPE_COMMON_TOUCH && msg.what != MessageType.MSG_TYPE_COMMON_UNTOUCH) {
					mNoMoveTime = 0;
				}
				if (msg.what == MessageType.MSG_TYPE_REGISTER_NO_PIECE) {
					mNoPieceTime++;
					if (mNoPieceTime >= MAX_ACTION_ERROR && bHasShowNoPieceDialog == false) {
						ToastUtil.showToast(FingerFactoryTestActivity.this, R.string.register_no_piece_together);
						bHasShowNoPieceDialog = true;
						mNoPieceTime = 0;
					}
				} else if (msg.what != MessageType.MSG_TYPE_REGISTER_NO_PIECE && msg.what != MessageType.MSG_TYPE_COMMON_TOUCH && msg.what != MessageType.MSG_TYPE_COMMON_UNTOUCH) {
					mNoPieceTime = 0;
				}
				int index = msg.arg1;
				setDialogProgress(activity.mPercent);

				Log.v(TAG, "RegisterHandler: mPercent" + activity.mPercent);
				if (mInputFrequencyCount >= 2) {
					try {
						SystemProperties.set("persist.sys.prize_fp_enable", "1");
						//						activity.mSession.save(index);
					} catch (Exception e) {
						e.printStackTrace();
					}
					activity.cancelReleaseFingerTimer();
					activity.cancelCancelTimer();
					activity.mSubInfoTxt.setText(R.string.register_complete_infomation);
					activity.mRootGroup.setOnClickListener(null);
					ToastUtil.showToast(FingerFactoryTestActivity.this, R.string.enter_successfully);
					finish();
				} else {
					if (activity.mPercent >= 70 && bHasShowAni == false) {
						bHasShowAni = true;
						activity.startSubWarning(
								String.format(activity.getResources().getString(R.string.capture_notice_put_on_screen_outside),
										activity.getResources().getString(R.string.outside_area)),activity.getResources().getString(R.string.outside_area));
					}
					if (bToutch == true) {
						activity.startReleaseFingerTimer();
					}
				}
				break;
			case MessageType.MSG_TYPE_COMMON_TOUCH :
				Log.v(TAG, "RegisterHandler:MSG_TYPE_COMMON_TOUCH");
				bToutch = true;
				if (activity.mDialog != null && activity.mDialog.isShowing()) {
					activity.mDialog.dismiss();
				}
				activity.resetCancelTimer();

				break;
			case MessageType.MSG_TYPE_COMMON_UNTOUCH :
				bToutch = false;
				Log.v(TAG, "RegisterHandler:MSG_TYPE_COMMON_UNTOUCH");
				activity.cancelReleaseFingerTimer();
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
