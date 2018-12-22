package com.android.prize;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.Assert;

import com.android.camera.CameraActivity;
import com.android.camera.FeatureSwitcher;
import com.android.camera.Util;
import com.android.camera.CameraActivity.OnSingleTapUpListener;
import com.android.camera.R;

import android.R.integer;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import com.android.camera.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.RelativeLayout.LayoutParams;

import com.android.camera.ui.PickerButton;
import com.android.camera.ui.PickerButton.Listener;
import com.android.camera.ui.PreviewFrameLayout;
import com.android.camera.ui.PreviewSurfaceView;
import com.android.prize.DoubleCameraFocusIndicator.OnSeekBarChangeListener;
import com.mediatek.camera.ICameraContext;
import com.mediatek.camera.mode.PhotoMode;
import com.mediatek.camera.mode.facebeauty.FaceBeautyPreviewSize;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingDataBase;
import com.mediatek.camera.setting.preference.IconListPreference;
import com.mediatek.camera.setting.preference.ListPreference;
import com.mediatek.camera.util.CaptureSound;
import com.android.camera.bridge.CameraDeviceCtrl.PreviewCallbackListen;
public class DoubleCameraMode extends PhotoMode {

	private static final String TAG = "DoubleCameraMode";
	private Camera mSecondaryCamera = null;
	private static final int MSG_OPENCAMERA = 0;
	private static final int MSG_STARTPREVIEW = 1;

	String proc = "0";
	private Timer mTimer = null;
	private TimerTask mTimerTask = null;

	private int mScreenWidth = 0;// 屏幕分辨率
	private int mScreenHeight = 0;

	private static final int GYFUZZY_LEVEL_MAX = 100;
	private static final int GYFUZZY_LEVEL_DEFAULT = 50;
	private static final int GYFUZZY_RADIUS_DEFAULT = 60;
	private static final int NOdEVALUE_MAX = 450;
	private static final int PREVIEW_SIZE_MAIN_WIGHT = 640;
	private static final int PREVIEW_SIZE_MAIN_HEIGHT = 480;
	private static final int PREVIEW_SIZE_MAIN_WIGHT_16_9 = 800;
	private static final int PREVIEW_SIZE_MAIN_HEIGHT_16_9 = 450;
	/*prize-xuchunming-20160919-add double camera activity boot-start*/
	private static final int MSG_ATTACH_SURFACE= 1;
	private static final int MSG_PARAMETERS_READY= 2;
	/*prize-xuchunming-20160919-add double camera activity boot-end*/
	private boolean isBuzzyBackgroud = true;
	private HandlerThread mHandlerThread;
	private boolean mRunning;
	RelativeLayout mFoucsLayout;
	DoubleCameraFocusIndicator mDoubleCameraFocusIndicator;
	private Handler myHandler;
	
    private DoubleCaptureSound mDoubleCaptureSound;
	
    /*prize-xuchunming-20161104-add bgfuzzy switch-start*/
    private PickerButton mBgFuzzySwitch = null;
    private FrameLayout mBgFuzzySwitchLayout = null;
    private boolean isBgFuzzySwitchOn = false;
    /*prize-xuchunming-20161104-add bgfuzzy switch-end*/
	
	private IBuzzyStrategy mBuzzyStrategy;
    
	private DoubleCameraPrieviewSizeRule mDoubleCameraPrieviewSizeRule;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (myHandler != null && msg.what == 1) {
				myHandler.post(mRunnable);
			}
		}
	};

	/*prize-xuchunming-20160919-add double camera activity boot-start*/
	private Handler mUiHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_ATTACH_SURFACE:
				if(mBuzzyStrategy != null && isBgFuzzySwitchOn == true){
					mBuzzyStrategy.attachSurfaceViewLayout();
					addFocusView();
				}
				break;
			case MSG_PARAMETERS_READY:
				addBgFuzzySwitch();
				break;
			default:
				break;
			}
		}
	};
	/*prize-xuchunming-20160919-add double camera activity boot-end*/
	
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mBuzzyStrategy == null) {
				return;
			}
			long starttime = System.currentTimeMillis();
			if (mBuzzyStrategy.isOcclusion()) {
				if (isBuzzyBackgroud) {
					showReminder();
					setBgFuzzyLevel(0);
					isBuzzyBackgroud = false;
				}
			} else {
				/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
				if (!isBuzzyBackgroud && isBgFuzzySwitchOn == true) {
				/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
					String tempValue = ((CameraActivity)mActivity).getListPreference(SettingConstants.KEY_APERTURE).getValue();
					if(tempValue!=null){
						int mApertureValue = Integer.valueOf(tempValue);
						setBgFuzzyLevel(mApertureValue);
					}else{
						setBgFuzzyLevel(GYFUZZY_LEVEL_DEFAULT);
					}
					isBuzzyBackgroud = true;
				}
			}
			Log.i(TAG, "handleMessage()-------------------->>> time = " + (System.currentTimeMillis() - starttime));
		}
	};

	public DoubleCameraMode(ICameraContext cameraContext) {
		super(cameraContext);
		getScreenWH();
		mDoubleCaptureSound = new DoubleCaptureSound(mActivity);
		if (FeatureSwitcher.isRearCameraSub()) {
			mBuzzyStrategy = new DoubleBuzzyStrategy(mActivity);
		} else if (FeatureSwitcher.isRearCameraSubAls()) {
			mBuzzyStrategy = new LightBuzzyStrategy();
		}
		setDoubleCameraPreviewSizeRule();
	}

	@Override
	protected boolean executeAction(ActionType type, Object... arg) {
		// TODO Auto-generated method stub
		switch (type) {
		/*prize-xuchunming-20160919-add double camera activity boot-start*/
		case ACTION_ON_CAMERA_FIRST_OPEN_DONE:
			Log.d(TAG, "ACTION_ON_CAMERA_FIRST_OPEN_DONE");
			mUiHandler.removeMessages(MSG_ATTACH_SURFACE);
			mUiHandler.sendEmptyMessage(MSG_ATTACH_SURFACE);
			startCamera();
			break;
		case ACTION_MODE_BLUE_DONE:
			Log.d(TAG, "ACTION_MODE_BLUE_DONE");
			mUiHandler.removeMessages(MSG_ATTACH_SURFACE);
			mUiHandler.sendEmptyMessage(MSG_ATTACH_SURFACE);
			startCamera();
			break;
		/*prize-xuchunming-20160919-add double camera activity boot-end*/
		case ACTION_ACTIVITY_ONPAUSE:
			Log.d(TAG, "ACTION_ON_CAMERA_OPEN_DONE");
			stopCamera();
			break;
		case ACTION_ON_CAMERA_PARAMETERS_READY:
			/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
			mUiHandler.removeMessages(MSG_PARAMETERS_READY);
			mUiHandler.sendEmptyMessage(MSG_PARAMETERS_READY);
			/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
			
			break;
		case ACTION_ON_SETTING_BUTTON_CLICK:
			Log.d(TAG, "ACTION_ON_SETTING_BUTTON_CLICK :" + (Boolean) arg[0]);
			if ((Boolean) arg[0] == true) {
				stopCamera();
			} else {
				if(((CameraActivity)mActivity).isActivityOnpause() == false){
					((CameraActivity) mActivity).setPreviewCallback();
					startCamera();
				}
			}
			break;
		case ACTION_ON_SINGLE_TAP_UP:
			Assert.assertTrue(arg.length == 3);
			/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
			if(mDoubleCameraFocusIndicator != null){
				mDoubleCameraFocusIndicator.onSinlgeTapUp((View) arg[0], (Integer) arg[1], (Integer) arg[2]);
				singleTapUp((View) arg[0], (Integer) arg[1], (Integer) arg[2]);
			}
			/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
			break;
		case ACTION_AUTO_FOCUSING:
			onAutoFocusing();
			break;
		case ACTION_AUTO_FOCUSED:
			onAutoFocused();
			break;
		case ACTION_MV_FOCUSING:
			onMvFocusing();
			break;
		case ACTION_MV_FOCUSEND:
			onMvFocused();
			break;
		case ACTION_SHUTTER_BUTTON_LONG_PRESS:
	         mICameraAppUi.showInfo(mActivity.getString(R.string.double_camera_title)
	                    + mActivity.getString(R.string.camera_continuous_not_supported));
	         break;

		}
		return super.executeAction(type, arg);
	}

	private void onAutoFocusing() {
		// TODO Auto-generated method stub
		if(mDoubleCameraFocusIndicator != null){
			mDoubleCameraFocusIndicator.onAutoFocusing();
		}
	}

	private void onAutoFocused() {
		// TODO Auto-generated method stub
		if(mDoubleCameraFocusIndicator != null){
			mDoubleCameraFocusIndicator.onAutoFocused();
		}

	}

	private void onMvFocused() {
		// TODO Auto-generated method stub
		if(mDoubleCameraFocusIndicator != null){
			mDoubleCameraFocusIndicator.onMvFocused();
		}
	}

	private void onMvFocusing() {
		// TODO Auto-generated method stub
		if(mDoubleCameraFocusIndicator != null){
			mDoubleCameraFocusIndicator.onMvFocusing();
		}
	}

	private void setPropery(String value){
		/*try{
			Log.v(TAG, "[DoubleCamera]setPropery  start...");
			android.os.SystemProperties.set("sys.prize.dualcamera",value);
			Log.v(TAG, "[DoubleCamera]setPropery  end...");
		} catch (Exception e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();  
			Log.v(TAG, "[DoubleCamera]setPropery  Exception...");
		}*/  
		Log.v(TAG, "[DoubleCamera] set propery start!");
		try {
			IActivityManager am = ActivityManagerNative.getDefault();
			//am.setPropertyForExternal("sys.prize.dualcamera",value);
			Log.v(TAG, "[DoubleCamera] set propery end!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startCamera() {
		/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
		if(isBgFuzzySwitchOn == true){
			setPropery("1");
			openCamera();
			startPreview();
			openBgFuuzzy();
			// initPickerAreaPointOfSecond(BACK_CAMERA_SECOND);
			// initPickerAreaPointOfMain(BACK_CAMERA_MAIN);
			startTimer();
			startHandle();
		}
		/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
	}

	public void stopCamera() {
		// TODO Auto-generated method stub
		stopTimer();
		stopHandle();
	
		if (mBuzzyStrategy != null) {
			mBuzzyStrategy.closeCamera();
		}
	}

	@Override
	public void takePictureStart() {
		// TODO Auto-generated method stub
		stopCamera();
		if (mDoubleCaptureSound.isCameraUnMute()) {
			mISettingCtrl.onSettingChanged(SettingConstants.KEY_CAMERA_MUTE, "on");
			((CameraActivity)mActivity).applyParametersToServer();
			mDoubleCaptureSound.play();
        }
	}

	@Override
	public void takePictureEnd() {
		// TODO Auto-generated method stub
		startCamera();
		Log.d(TAG, "takePictureEnd");
		((CameraActivity)mActivity).setPreviewCallback();
	}

	public void openCamera() {
		if (mBuzzyStrategy != null) {
			mBuzzyStrategy.openCamera();
		}
	}

	@Override
	public boolean close() {
		// TODO Auto-generated method stub
		/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
		setPropery("0");
		stopCamera();
		closeBgFuuzzy();
		mBuzzyStrategy.detachSurfaceViewLayout();
		removeFocusView();
		if (mDoubleCaptureSound.isCameraUnMute()) {
			mDoubleCaptureSound.release();
			mISettingCtrl.onSettingChanged(SettingConstants.KEY_CAMERA_MUTE, "off");
			((CameraActivity)mActivity).applyParametersToServer();
        }
		removeBgFuzzySwitch();
		resetSettingItem();
		((CameraActivity)mActivity).removePreviewCallback(mPreviewCallback);
		/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
		return super.close();
	}
    
	public void startPreview() {
		Log.d(TAG, "mSecondaryCamera.startPreview()");
		if (mBuzzyStrategy != null) {
			mBuzzyStrategy.startPreview();
		}
	}

	/* prize-xuchunming-20160829-add previewCallback interface-start */
	private PreviewCallbackListen mPreviewCallback = new PreviewCallbackListen() {
		public void onPreviewFrame(byte[] data, final Camera camera) {
			// Log.d(TAG, "[Main mPreviewCallback.onPreviewFrame]
			// data.length:"+data.length);
			if (mBuzzyStrategy != null) {
				mBuzzyStrategy.saveMainBmp(data);
			}
		}
	};
	/* prize-xuchunming-20160829-add previewCallback interface-end */


	private void showReminder() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast toast = new Toast(mActivity);
				View view = LayoutInflater.from(mActivity).inflate(R.layout.double_camera_toast, null);
				toast.setView(view);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setGravity(Gravity.TOP, 0, 90);
				toast.show();
			}
		});
	}

	public void setBgFuuzzy(String key, int value) {
		CameraActivity mCameraActivity = (CameraActivity) mActivity;
		if (mCameraActivity.getParameters() != null) {
			mCameraActivity.getParameters().set(key, value);
			mCameraActivity.applyParametersToServer();
		}
	}

	public void openBgFuuzzy() {
		if (mActivity instanceof CameraActivity) {
			CameraActivity mCameraActivity = (CameraActivity) mActivity;
			Size size = mCameraActivity.getParameters().getPreviewSize();
			Log.i(TAG, "openBgFuuzzy ");
			if (mCameraActivity.getParameters() != null) {
				Log.i(TAG, "[openBgFuuzzy()] Preview W = " + size.width + ", H = " + size.height);
				mCameraActivity.getParameters().set("gy-bokeh-enable", "1");
				mCameraActivity.getParameters().set("gy-bokeh-x", size.height / 2);
				mCameraActivity.getParameters().set("gy-bokeh-y", size.width / 2);
				/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
				int progress = Integer.valueOf(((CameraActivity)mActivity).getListPreference(SettingConstants.KEY_APERTURE).getValue());
				mCameraActivity.getParameters().set("gy-bokeh-level", progressMapping(progress));
				progress = 100 - progress;
				if (progress < 30) {
					progress = 30;
				}
				mCameraActivity.getParameters().set("gy-bokeh-radius", progress);
				/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
				mCameraActivity.getParameters().set("gy-bokeh-bklevel", 50);
				mCameraActivity.applyParametersToServer();
				isBuzzyBackgroud = true;
			} else {
				Log.i(TAG, "setBgFuuzzy error ");
			}
		}
	}

	public void closeBgFuuzzy() {
		if (mActivity instanceof CameraActivity) {
			CameraActivity mCameraActivity = (CameraActivity) mActivity;
			if (mCameraActivity.getParameters() != null) {
				Log.i(TAG, "closeBgFuuzzy ");
				mCameraActivity.getParameters().set("gy-bokeh-enable", 0);
				mCameraActivity.getParameters().set("gy-bokeh-x", 0);
				mCameraActivity.getParameters().set("gy-bokeh-y", 0);
				mCameraActivity.getParameters().set("gy-bokeh-radius", 0);
				mCameraActivity.getParameters().set("gy-bokeh-level", 0);
				mCameraActivity.getParameters().set("gy-bokeh-bklevel", 0);
				mCameraActivity.applyParametersToServer();
				isBuzzyBackgroud = false;
			} else {
				Log.i(TAG, "closeBgFuuzzy error ");
			}
		}
	}

	private void startTimer() {
		if (mTimer == null) {
			mTimer = new Timer();
		}

		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					Message msg = new Message();
					msg.what = 1;
					mHandler.sendMessage(msg);
				}
			};
			int time = mBuzzyStrategy != null ? mBuzzyStrategy.getCheckTime() : 1000;
			mTimer.schedule(mTimerTask, 0, time);
		}
	}

	private void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}

	/**
	 * 获取屏幕分辨率
	 */
	private void getScreenWH() {
		WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metric = new DisplayMetrics();
		display.getRealMetrics(metric);
		mScreenWidth = metric.widthPixels;;
		mScreenHeight = metric.heightPixels;
	}

	/**
	 * 设置虚化位置
	 * 
	 * @param x
	 * @param y
	 */
	private void setBgFuzzyLocation(float x, float y) {
		CameraActivity mCameraActivity = (CameraActivity) mActivity;
		if (mCameraActivity.getParameters() != null) {
			Log.i(TAG, "setBgFuuzzy x:" + x + ",y:" + y);
			mCameraActivity.getParameters().set("gy-bokeh-x", (int) x);
			mCameraActivity.getParameters().set("gy-bokeh-y", (int) y);
			mCameraActivity.applyParametersToServer();
		} else {
			Log.i(TAG, "setBgFuuzzy error ");
		}
	}

	/**
	 * 设置虚化程度
	 * 
	 * @param x
	 * @param y
	 */
	private void setBgFuzzyLevel(int level) {
		CameraActivity mCameraActivity = (CameraActivity) mActivity;
		if (mCameraActivity.getParameters() != null) {
			Log.i(TAG, "setBgFuuzzy level:" + level);
			mCameraActivity.getParameters().set("gy-bokeh-level", level);
			mCameraActivity.applyParametersToServer();
		} else {
			Log.i(TAG, "setBgFuuzzy error ");
		}
	}

	protected void singleTapUp(View view, int x, int y) {
		Log.v(TAG, "onSingleTapUp x:" + x + ",y:" + y);
		float screenClickX = x;
		float screenClickY = y;
		CameraActivity mCameraActivity = (CameraActivity) mActivity;
		Size previewSize = mCameraActivity.getParameters().getPreviewSize();
		int prewight = previewSize.width;
		int preheight = previewSize.height;
		Log.v(TAG, "onSingleTapUp previewSize W:" + prewight + ", H:" + preheight + ", ScreenSize: W = " + mScreenWidth
				+ ", H = " + mScreenHeight);
		// ganyun The origin of the coordinate is on the right and top. 
		if ((Math.abs(1.3333 - (float) prewight / preheight)) < 0.02) {
			setBgFuzzyLocation((float) (screenClickX) * ((float) preheight / mScreenWidth),
					((float) (screenClickY)) * ((float) preheight / mScreenWidth));
		} else {
			setBgFuzzyLocation((float) (screenClickX) * ((float) preheight / mScreenWidth),
					((float) (screenClickY)) * ((float) prewight / mScreenHeight));
		}
	}

	/* prize-xuchunming-20160829-add previewCallback interface-start */
	public PreviewCallbackListen getPreviewCallback() {
		Log.v(TAG, "return mPreviewCallback");
		return mPreviewCallback;
	}
	/* prize-xuchunming-20160829-add previewCallback interface-end */
	
	public void addFocusView() {
		if(mFoucsLayout == null){
			((CameraActivity) mActivity).getFocusManager().getFocusLayout().setAlpha(0.0f);
			PreviewFrameLayout mPreviewFrameLayout = (PreviewFrameLayout) mActivity.findViewById(R.id.frame);
			mFoucsLayout = (RelativeLayout) mActivity.getLayoutInflater().inflate(R.layout.double_camera_view, null);
			mPreviewFrameLayout.addView(mFoucsLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			mDoubleCameraFocusIndicator = (DoubleCameraFocusIndicator) mFoucsLayout
					.findViewById(R.id.double_camera_rotate_layout);
			mDoubleCameraFocusIndicator.initUi();
			mDoubleCameraFocusIndicator.setOrientation(90, false);
			mDoubleCameraFocusIndicator.startShow();
			mDoubleCameraFocusIndicator.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
				}

				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					// TODO Auto-generated method stub
					if (mActivity instanceof CameraActivity) {
						CameraActivity mCameraActivity = (CameraActivity) mActivity;
						if (mCameraActivity.getParameters() != null && isBuzzyBackgroud) {
							Log.i(TAG, "set gy-bokeh-radius: " + progress);
							mCameraActivity.getParameters().set("gy-bokeh-level", progressMapping(progress));
							progress = 100 - progress;
							if (progress < 30) {
								progress = 30;
							}
							mCameraActivity.getParameters().set("gy-bokeh-radius", progress);
							mCameraActivity.applyParametersToServer();
						} else {
							Log.i(TAG, "set gy-bokeh-radius error ");
						}
					}
				}
			});
		}
	}
	
	private int progressMapping(int index){
		if(index > 0){
			if(index < 10){
				return index*4;
			}else{
				return (int) (40 + (index-10)/1.5);
			}
		}
		return 0;
	}

	public void removeFocusView() {
		PreviewFrameLayout mPreviewFrameLayout = (PreviewFrameLayout) mActivity.findViewById(R.id.frame);
		/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
		if(mFoucsLayout != null){
		/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
			mPreviewFrameLayout.removeView(mFoucsLayout);
			mFoucsLayout = null;
			mDoubleCameraFocusIndicator = null;
			((CameraActivity) mActivity).getFocusManager().getFocusLayout().setAlpha(1.0f);
		}
		
	}

	/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
	public void addBgFuzzySwitch(){
		if (mBgFuzzySwitchLayout == null) {
			FrameLayout mViewLayerTop = (FrameLayout) mActivity.findViewById(R.id.view_layer_top);
			mBgFuzzySwitchLayout = (FrameLayout) mActivity.getLayoutInflater().inflate(R.layout.bg_fuzzy_switch, null);
			mBgFuzzySwitch = (PickerButton) mBgFuzzySwitchLayout.findViewById(R.id.bg_fuzzy_switch);
			IconListPreference pref = (IconListPreference) ((CameraActivity)mActivity).getListPreference(SettingConstants.KEY_BG_FUZZY);
			pref.showInSetting(false);
			mBgFuzzySwitch.initialize(pref);
			mBgFuzzySwitch.refresh();
			isBgFuzzySwitchOn = pref.getValue().equals("on") ? true:false;
			mBgFuzzySwitch.setListener(new Listener() {
				
				@Override
				public boolean onPicked(PickerButton button, ListPreference preference,
						String newValue) {
					// TODO Auto-generated method stub
					if(newValue.equals("on")){
						openBlur();
					}else{
						closeBlur();
					}
					return true;
				}

			});
			
			mViewLayerTop.addView(mBgFuzzySwitchLayout);
			
			if(isBgFuzzySwitchOn == true){
				((CameraActivity)mActivity).getParameters().set("gy-bokeh-enable", "1");
				((CameraActivity)mActivity).applyParametersToServer();
			}
		}
		
	}
	
	public void removeBgFuzzySwitch(){
		if (mBgFuzzySwitchLayout != null) {
			FrameLayout mViewLayerTop = (FrameLayout) mActivity.findViewById(R.id.view_layer_top);
			mViewLayerTop.removeView(mBgFuzzySwitchLayout);
			mBgFuzzySwitchLayout = null;
		}
	}
	
	public void closeBlur() {
		// TODO Auto-generated method stub
		Log.d(TAG, "closeBlur-onlycloseBgFuuzzy");
		isBgFuzzySwitchOn = false;
		stopTimer();
		stopHandle();
		closeBgFuuzzy();
		removeFocusView();
	}

	public void openBlur() {
		// TODO Auto-generated method stub
		Log.d(TAG, "openBlur");
		isBgFuzzySwitchOn = true;
		openBgFuuzzy();
		startTimer();
		startHandle();
		addFocusView();
	}
	
	public void resetSettingItem(){
		
		IconListPreference pref = (IconListPreference) ((CameraActivity)mActivity).getListPreference(SettingConstants.KEY_BG_FUZZY);
		if(pref != null){
			pref.setValue("on");
		}
	
	}
	
	public void stopHandle(){
		if(myHandler != null){
			myHandler.removeCallbacks(mRunnable);
			mHandlerThread.quitSafely();
			myHandler = null;
			mHandlerThread = null;
		}
	}
	
	public void startHandle(){
		if(myHandler == null){
			mHandlerThread = new HandlerThread("DoubleCamera", 1);
			mHandlerThread.start();
			myHandler = new Handler(mHandlerThread.getLooper());
		}
	}
	/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
	
	 private void setDoubleCameraPreviewSizeRule() {
	        mDoubleCameraPrieviewSizeRule = new DoubleCameraPrieviewSizeRule(mICameraContext);
	        mISettingCtrl.addRule(SettingConstants.KEY_DOUBLE_CAMERA, SettingConstants.KEY_PICTURE_RATIO,
	        		mDoubleCameraPrieviewSizeRule);
	        mDoubleCameraPrieviewSizeRule.addLimitation("on", null, null);
	}
}
