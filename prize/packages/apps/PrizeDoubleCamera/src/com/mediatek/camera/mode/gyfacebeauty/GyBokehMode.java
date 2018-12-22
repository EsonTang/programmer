/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2014. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera.mode.gyfacebeauty;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.android.camera.FeatureSwitcher;
import com.android.camera.MyRunable;
import com.android.camera.R;

import com.android.camera.Storage;
import com.mediatek.camera.AdditionManager;
import com.mediatek.camera.ICameraAddition;
import com.mediatek.camera.ICameraContext;
import com.mediatek.camera.ICameraAddition.AdditionActionType;
import com.mediatek.camera.ICameraMode.ActionType;
import com.mediatek.camera.ICameraMode.ModeState;
import com.mediatek.camera.mode.CameraMode;
//import com.mediatek.camera.mode.facebeauty.FaceBeautyParametersHelper.ParameterListener;
import com.mediatek.camera.platform.ICameraAppUi.SpecViewType;
import com.mediatek.camera.platform.ICameraAppUi.ViewState;
import com.mediatek.camera.platform.ICameraDeviceManager.ICameraDevice.AutoFocusMvCallback;
import com.mediatek.camera.platform.ICameraDeviceManager.ICameraDevice.cFbOriginalCallback;
import com.mediatek.camera.platform.ICameraView;
import com.mediatek.camera.platform.IFocusManager;
import com.mediatek.camera.platform.IFileSaver.FILE_TYPE;
import com.mediatek.camera.platform.IFileSaver.OnFileSavedListener;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.util.Log;
import com.mediatek.camera.util.Util;
import android.content.Context;
import android.app.Activity;
import junit.framework.Assert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.mediatek.camera.platform.ICameraDeviceManager;
import com.mediatek.camera.platform.ICameraDeviceManager.ICameraDevice;
import com.android.camera.CameraActivity;
import com.android.prize.BokehCameraPrieviewSizeRule;
import com.android.prize.DoubleBuzzyStrategy;
import com.android.prize.DoubleCameraPrieviewSizeRule;
import com.android.prize.IBuzzyStrategy;
import com.android.prize.LightBuzzyStrategy;
import com.android.prize.YuvBackBuzzyStrategy;
import com.mediatek.camera.mode.gyfacebeauty.GyBokehHelper;
import com.mediatek.camera.mode.gyfacebeauty.GyBokehHelper.BgFuzzySwitchListeren;

import com.gangyun.camera.GYSurfaceView;
import com.gangyun.camera.LibVGestureDetect;

import org.json.JSONException;
import org.json.JSONObject;


public class GyBokehMode extends CameraMode implements ICameraAddition.Listener,
        IFocusManager.FocusListener {
    private static final String TAG = "GYLog GyBokehMode";
    
    protected static final int SUPPORTED_FB_PROPERTIES_MAX_NUMBER = 4;
    
    protected static final int ON_CAMERA_PARAMETERS_READY = 1;
    protected static final int INFO_FACE_DETECTED = 2;
    protected static final int ORIENTATION_CHANGED = 3;
    protected static final int ON_BACK_PRESSED = 4;
    protected static final int HIDE_EFFECTS_ITEM = 5;
    protected static final int ON_FULL_SCREEN_CHANGED = 6;
    protected static final int ON_CAMERA_CLOSED = 7;
    protected static final int ON_SETTING_BUTTON_CLICK = 8;
    protected static final int ON_LEAVE_FACE_BEAUTY_MODE = 9;
    private static final int ON_CONFIGURATION_CHANGED = 10;
    private static final int MSG_ON_SHOW_BLOCK_TOAST = 11;

    private int size[] ={0,0,0,0,0,0,0,0,0,0,0};
    
    
    private CfbCallback mCfbCallback = new CfbCallback();
    private Handler mHandler;
//    private FaceBeautyPreviewSize mFaceBeautyPreviewSize;
   // private FaceBeautyParametersHelper mFaceBeautyParametersHelper;
  //  private ParameterListener mParameterListener;
    
    private ICameraView mICameraView;
    private ArrayList<Integer> mVfbFacesPoint = new ArrayList<Integer>();
    private AdditionManager mAdditionManager;
    
    private boolean mIsAutoFocusCallback = false;
    private CameraActivity mContext;
   	private GyBokehHelper mGyBokehHelper;

	private Parameters mParameters;
	
	/*prize-xuchunming-20161202-start*/
	private BokehCameraPrieviewSizeRule mBokehCameraPrieviewSizeRule;
	private IBuzzyStrategy mBuzzyStrategy;
	private Timer mTimer = null;	
	private TimerTask mTimerTask = null;
	private Handler myHandler;
	private Handler myThreadHandler;
	private HandlerThread mHandlerThread;
	private boolean isBuzzyBackgroud = true;
	private static final int GYFUZZY_LEVEL_DEFAULT = 50;
	private static final int MSG_READ_SURFACE_DATA = 104;
	private static final int MSG_ON_CAMERA_OPEN_DONE = 105;
	
	private ConditionVariable mConditionVariable = new ConditionVariable();
	private BgFuzzySwitchListeren mBgFuzzySwitchListeren;
	
	protected boolean mCameraClosed = false;

    private Executor mBitmapExecutor = null;
	
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
					mGyBokehHelper.onGyProgressChanged(0);
					mGyBokehHelper.setIsCoverSecondaryCamera(true);
					isBuzzyBackgroud = false;
				}
			} else {
				/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
				if (!isBuzzyBackgroud && mGyBokehHelper.isBgFuzzySwitchOn() == true) {
				/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
					String tempValue = ((CameraActivity)mActivity).getListPreference(SettingConstants.KEY_APERTURE).getValue();
					if(tempValue!=null){
						int mApertureValue = Integer.valueOf(tempValue);
						mGyBokehHelper.onGyProgressChanged(mApertureValue);
					}else{
						mGyBokehHelper.onGyProgressChanged(GYFUZZY_LEVEL_DEFAULT);
					}
					mGyBokehHelper.setIsCoverSecondaryCamera(false);
					isBuzzyBackgroud = true;
				}
			}

			Log.i(TAG, "handleMessage()-------------------->>> time = " + (System.currentTimeMillis() - starttime));
		}
	};
	/*prize-xuchunming-20161202-end*/
		
    public GyBokehMode(ICameraContext cameraContext) {
        super(cameraContext);
        Log.i(TAG, "[GyBeautyMode]constructor...");
        // first check the camera device
		
        mContext = (CameraActivity)cameraContext.getActivity();       
        mHandler = new MainHandler(mActivity.getMainLooper());
        mAdditionManager = cameraContext.getAdditionManager();
		mGyBokehHelper = GyBokehHelper.getInstance(mContext);
		mBgFuzzySwitchListeren = new BgFuzzySwitchListeren() {
			
			@Override
			public void onBgFuzzySwitch(boolean isOpen) {
				// TODO Auto-generated method stub
				if(isOpen == true){
					startTimer();
				}else{
					stopTimer();
				}
			}
		};
		mGyBokehHelper.setBgFuzzySwitchListeren(mBgFuzzySwitchListeren);
		setBotkehCameraPreviewSizeRule();
		if (FeatureSwitcher.isRearCameraSub()) {
			mBuzzyStrategy = new DoubleBuzzyStrategy(mActivity);
			mGyBokehHelper.setmBuzzyStrategy(mBuzzyStrategy);
		} else if (FeatureSwitcher.isRearCameraSubAls()) {
			mBuzzyStrategy = new LightBuzzyStrategy();
		}else if(FeatureSwitcher.isYuvRearCameraSub()){
		     mBuzzyStrategy = new YuvBackBuzzyStrategy();
		}
    }

    private android.widget.SeekBar mSeekBar;
    private android.widget.SeekBar mSeekBar2;
    private static int mSmoothValue = -1;
    private static int mWhiteningValue = 0;
    private int  mWhitening = 6;
    private boolean isInPopupSetting = false;

  
    @Override
    public void pause() {
        super.pause();
        Log.i(TAG, "[pause()] mICameraView = " + mICameraView);
        // Need hide the view when activity is onPause
        if (mICameraView != null) {
            mICameraView.hide();
        }
    }

    @Override
    public boolean close() {
        Log.i(TAG, "[closeMode]NextMode = " + mIModuleCtrl.getNextMode());
        /*prize-xuchunming-20180504-bugid:56566-start*/
        super.close();
        /*prize-xuchunming-20180504-bugid:56566-end*/
        if (mIModuleCtrl.getNextMode() != null) {
         /*gangyun tech add  SLR continuous shooting probability there is no blur effect begin*/	
		 mParameters =mContext.getCameraDeviceCtrl().getCurCameraDevice().getParameters();
	     mParameters.set("gy-bokeh-enable",  0);
		 mParameters.set("gy-bokeh-x", 0);
		 mParameters.set("gy-bokeh-y", 0);
		 mParameters.set("gy-bokeh-radius",  0);
		 mParameters.set("gy-bokeh-level",  0);
		 mParameters.set("gy-bokeh-transition",  0); 
		
         //mContext.getCameraDevice().getCamera().setParameters(mParameters);
         mContext.getCameraDeviceCtrl().getCurCameraDevice().applyParametersToServer();
         /*gangyun tech add  SLR continuous shooting probability there is no blur effect end*/
            // VFB photo mode to Video VR,not need set the face-beauty= false
            // 1:because video not know before mode is VFB;
            // 2:if set face-beauty= false,will found the video former parts not
            // have the face beauty effects
            if (mIFeatureConfig.isVfbEnable()) {
                if (CameraModeType.EXT_MODE_VIDEO == mIModuleCtrl.getNextMode()) {
                    //first need update the effects item if current effects is expanded
                   // mICameraView.update(HIDE_EFFECTS_ITEM);
                } else {
                  //  setVFBPs(false);
                }
            }
            
            if (mHandler != null) {
                mHandler.sendEmptyMessage(ON_LEAVE_FACE_BEAUTY_MODE);
            }
            
            // when close the mode ,need remove all the Msg when not execute
            removeAllMsg();
            // when leave out face beauty mode,need change the face view beauty
            // tag; because into this mode ,you have set true;
       //     changeFaceBeautyStatues(false);

			 
        }
        mGyBokehHelper.gyBokehclose(true);
        mGyBokehHelper.gyBokehrelease();
        mAdditionManager.close(true);
        mGyBokehHelper.setIsCoverSecondaryCamera(true);
        isBuzzyBackgroud = false;
    	stopTimer();
		stopHandle();
    	closeSecondaryCamera();		 
        /*prize-xuchunming-20160215-bugid:28649-start*/
    	//deleteBotkehCameraPreviewSizeRule();
    	/*prize-xuchunming-20160215-bugid:28649-end*/
        return true;
    }

    @Override
    public boolean execute(ActionType type, Object... arg) {
        boolean returnValue = true;
        switch (type) {
        case ACTION_ON_CAMERA_OPEN:
	     Log.i(TAG, "[execute]set CameraModeType.ACTION_ON_CAMERA_OPEN");
            super.updateDevice();
            mAdditionManager.execute(type, true); 
            mCameraClosed = false;
            break;
        case ACTION_ON_CAMERA_CLOSE:				
	        Log.i(TAG, "[execute]set CameraModeType.ACTION_ON_CAMERA_CLOSE");
	        onCameraClose();
            break;
        case ACTION_ACTIVITY_ONPAUSE:				
	        resumeThread();
            break;   
        case ACTION_ON_START_PREVIEW:
            Assert.assertTrue(arg.length == 1);
            startPreview((Boolean) arg[0]);
	        Log.i(TAG, "[execute]set CameraModeType.ACTION_ON_START_PREVIEW");	  	
            break;
            
        case ACTION_ON_CAMERA_PARAMETERS_READY:
            super.updateDevice();
            super.updateFocusManager();
            if (mIFocusManager != null) {
                mIFocusManager.setListener(this);
            }
	        Log.i(TAG, "[execute]set CameraModeType.ACTION_ON_CAMERA_PARAMETERS_READY");			
            setModeState(ModeState.STATE_IDLE);
            
		
            // when user change the face beauty value to :Off need back to
            // PhotoMode

            // just Vfb need stopFD,because when native will get the preview
            // buffer itself will
            // get the FD Client,if we not stopFD,means the FD client is catched
            // by us;
            if (mIFeatureConfig.isVfbEnable()) {
              //  mIModuleCtrl.stopFaceDetection();
            }
           // mFaceBeautyParametersHelper.updateParameters(mICameraDevice);
            if(mContext.isActivityOnpause() == false){  // no pause 
            	 if (mContext.isCameraOpening() == true) { //is not ui thread
                 	//load surface view in Ui thread
                     //delay 5ms, be sure pause thread before resume thread
                 	Log.i(TAG, "[execute] CAMERA_PARAMETERS_READY Camera is Opening , wait 5s for load surface view");
                 	mHandler.sendEmptyMessageDelayed(ON_CAMERA_PARAMETERS_READY, 5);
                 	pauseThread();
                 }else{  //is ui thread
                 	 mIModuleCtrl.initializeFrameView(false);
      				 mGyBokehHelper.gyBokehShow();
                 }
            }
           
            mAdditionManager.onCameraParameterReady(true);       
	     
			
            break;

        case ACTION_FACE_DETECTED:
            Face[] faces = (Face[]) arg;
            if (mIFeatureConfig.isVfbEnable() && faces != null && mICameraView != null) {
                mICameraView.update(INFO_FACE_DETECTED, faces.length);
                // update the face beauty point
              //  storeFaceBeautyLocation(faces);
                // PhotoActor/3rd party not set the face, because you have call
                // stopFD
            //    mIModuleCtrl.setFaces(faces);
            }
            break;

        case ACTION_ON_COMPENSATION_CHANGED:
            if (mICameraView != null) {
                mICameraView.update(ORIENTATION_CHANGED, mIModuleCtrl.getOrientationCompensation());
            }
	    
            break;

        case ACTION_ON_FULL_SCREEN_CHANGED:
	    
            if (mICameraView != null) {
                mICameraView.update(ON_FULL_SCREEN_CHANGED, (Boolean) arg[0]);
            }
            break;
            
        case ACTION_SHUTTER_BUTTON_FOCUS:
            //when focus is detected, not need to do AF
            break;
            
        case ACTION_PHOTO_SHUTTER_BUTTON_CLICK:
        	onShutterButtonClick(); 
            break;

        case ACTION_SHUTTER_BUTTON_LONG_PRESS:
	        Log.i(TAG, "[execute]set CameraModeType.ACTION_SHUTTER_BUTTON_LONG_PRESS");				
            mICameraAppUi.showInfo(mActivity.getString(R.string.gy_accessibility_switch_to_bokeh)
                    + mActivity.getString(R.string.camera_continuous_not_supported),5 * 1000,(int)mActivity.getResources().getDimension(R.dimen.info_bottom));
            break;

        case ACTION_ON_SINGLE_TAP_UP:
            Assert.assertTrue(arg.length == 3);
            onSinlgeTapUp((View) arg[0], (Integer) arg[1], (Integer) arg[2]);
            break;

        case ACTION_ON_BACK_KEY_PRESS:
            // need callback ,if true means not need activity action on-back
            // pressed
            // when just supported Cfb, so need the supper onbackpressed
            if (mICameraView != null && !mICameraView.update(ON_BACK_PRESSED)
                    || !mIFeatureConfig.isVfbEnable()) {
                // returnValue is false means need super action the
                // onBackPressed
                returnValue = false;
            }
 	    returnValue = false;
            break;
        
        case ACTION_ON_SETTING_BUTTON_CLICK:
            // when user go to setting turn off the face beauty,before change
            // the setting we have hide the VFB UI, when the setting have
            // changed,this time not need show the UI
            //if (mICameraView != null && !isVfbOff()) {
             //   mICameraView.update(ON_SETTING_BUTTON_CLICK, arg[0]);
         //   }
        	Log.d(TAG, "ACTION_ON_SETTING_BUTTON_CLICK :" + (Boolean) arg[0]);
			if ((Boolean) arg[0] == true) {
				stopTimer();
			} else {
				startTimer();
			}
            break;
        
        case ACTION_ON_CONFIGURATION_CHANGED:
            if (mHandler != null) {
                mHandler.sendEmptyMessage(ON_CONFIGURATION_CHANGED);
            }
            break;
        /*prize-xuchunming-20161202-start*/
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
		case ACTION_MODE_BLUE_DONE:
			if(mContext.isActivityOnpause() == false){
				isBuzzyBackgroud = true;
				mGyBokehHelper.setIsCoverSecondaryCamera(false);
				if(mGyBokehHelper.isBgFuzzySwitchOn() == true){
					startTimer();
				}
				startHandle();
	    		openSecondaryCamera();
			}
			break;
		case ACTION_ON_CAMERA_OPEN_DONE:
			mGyBokehHelper.gyBokehSetPreviewCallback();
			mHandler.removeMessages(MSG_ON_CAMERA_OPEN_DONE);
			mHandler.sendEmptyMessage(MSG_ON_CAMERA_OPEN_DONE);
			break;
		case ACTION_ONESHOT_PREVIEW_CALLBACK:
			mGyBokehHelper.gyBokehSetPreviewCallback();
			break;
		/*prize-xuchunming-20161202-end*/
		/*prize-add-do not show the message-xiaoping-20171128-start*/
            case ACTION_MODE_CHANGE_DONE:
                if(mContext.isActivityOnpause() == false){
                    isBuzzyBackgroud = true;
                    mGyBokehHelper.setIsCoverSecondaryCamera(false);
                    if(mGyBokehHelper.isBgFuzzySwitchOn() == true){
                        startTimer();
                    }
                    startHandle();
                    openSecondaryCamera();
                }
                break;
        /*prize-add-do not show the message-xiaoping-20171128-start*/
        default:
            return false;
        }

        if (ActionType.ACTION_FACE_DETECTED != type) {
            Log.i(TAG, "[execute]type =" + type + ",returnValue = " + returnValue);
        }
        return returnValue;
    }

    @Override
    public boolean open() {
        mAdditionManager.setListener(this);
        mAdditionManager.open(true);
        super.open();
        return true;
    }

    //------> ICameraAddition.Listener
    @Override
    public boolean capture() {
        Log.i(TAG, "[capture]...");
        startCapture();
        return true;
    }

    @Override
    public boolean restartPreview(boolean needStop) {
      Log.i(TAG, "[restartPreview]...");
        return false;
    }
	
	@Override
    public void onFileSaveing() {
        setModeState(ModeState.STATE_SAVING);
    }
    
	
    //<------ICameraAddition.Listener

    //------> IFocusManager.FocusListener
    @Override
    public void autoFocus() {
        Log.i(TAG, "[autoFocus]...");
        mICameraDevice.autoFocus(mAutoFocusCallback);
        mICameraAppUi.setViewState(ViewState.VIEW_STATE_FOCUSING);
        setModeState(ModeState.STATE_FOCUSING);
    }

    @Override
    public void cancelAutoFocus() {
        Log.i(TAG, "[cancelAutoFocus]...");
        mICameraDevice.cancelAutoFocus();
        setFocusParameters();
        if (ModeState.STATE_CAPTURING != getModeState()) {
            mICameraAppUi.restoreViewState();
            setModeState(ModeState.STATE_IDLE);
        }
    }

    @Override
    public void startFaceDetection() {
        Log.i(TAG, "[startFaceDetection]...");
         mIModuleCtrl.startFaceDetection();
      //   mContext.getCameraDevice().startFaceDetection();
    }

    @Override
    public void stopFaceDetection() {
        Log.i(TAG, "[stopFaceDetection]...");
        mIModuleCtrl.stopFaceDetection();
    }

    @Override
    public void setFocusParameters() {
      //  Log.i(TAG, "[setFocusParameters]mIsAutoFocusCallback = " + mIsAutoFocusCallback);
        mIModuleCtrl.applyFocusParameters(!mIsAutoFocusCallback);
        mIsAutoFocusCallback = false;
    }

    @Override
    public void playSound(int soundId) {
    	/*prize-modify-bugid:60629 focus still sounds at cameraUnMute open-xiaoping-20180606-start*/
        if (mCameraSound != null && isCameraUnMute()) {
            mCameraSound.play(soundId);
        }
        /*prize-modify-bugid:60629 focus still sounds at cameraUnMute open-xiaoping-20180606-end*/
    }
    //<------IFocusManager.FocusListener
    
    private void removeAllMsg() {
        if (mHandler != null) {
            mHandler.removeMessages(ON_CAMERA_PARAMETERS_READY);
            mHandler.removeMessages(INFO_FACE_DETECTED);
            mHandler.removeMessages(ORIENTATION_CHANGED);
	        mHandler.removeMessages(ORIENTATION_CHANGED);
	        mHandler.removeMessages(MSG_ON_SHOW_BLOCK_TOAST);
	     
        }
    }

    private class MainHandler extends Handler {

        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "[handleMessage],msg = " + msg.what);

            switch (msg.what) {
            // this msg just used for VFB,so if you want use cFB,please be
            // careful
            case ON_CAMERA_PARAMETERS_READY:
            	if(mCameraClosed == true){
            		return;
            	}
                if (mICameraView != null) {
                    mICameraView.update(ON_CAMERA_PARAMETERS_READY);
                    mICameraView.show();
                }
                // have clear the face view
                // so you need initialize yourself
                mIModuleCtrl.initializeFrameView(false);
				mGyBokehHelper.gyBokehShow();
				
				if (mContext.isCameraOpening() == true) {
	            	resumeThread();
	            }
                break;

            case ON_LEAVE_FACE_BEAUTY_MODE:
                if (mICameraView != null) {
                    mICameraView.update(ON_LEAVE_FACE_BEAUTY_MODE);
                }
                break;
                
            case ON_CONFIGURATION_CHANGED:
                // because configuration change,so need re-inflate the view
                // layout
                if (mICameraView != null) {
                    mICameraView.uninit();
                    mICameraView.init(mActivity, mICameraAppUi, mIModuleCtrl);
                }
                break;
            /*prize-xuchunming-20161202-start*/
            case MSG_READ_SURFACE_DATA:
            	if (myThreadHandler != null) {
            		myThreadHandler.post(mRunnable);
    			}
            	break;
            case MSG_ON_CAMERA_OPEN_DONE:
            	isBuzzyBackgroud = true;
            	mGyBokehHelper.setIsCoverSecondaryCamera(false);
            	if(mGyBokehHelper.isBgFuzzySwitchOn() == true){
	            	startTimer();
            	}
            	startHandle();
        		openSecondaryCamera();
            	break;
            /*prize-xuchunming-20161202-end*/
            /*prize-modify-bugid:52930 prompt language appears on the desktop -xiaoping-20180326-start*/ 	
            case MSG_ON_SHOW_BLOCK_TOAST:
	            mICameraAppUi.showInfo(mActivity.getString(R.string.gy_accessibility_switch_to_bokeh)
	                    + mActivity.getString(R.string.double_camera_reminder_toast),2 * 1000);
				break;
			/*prize-modify-bugid:52930 prompt language appears on the desktop -xiaoping-20180326-start*/ 	
            default:
                break;
            }
        }
    }



    private void changeFaceBeautyStatues(boolean enable) {
        /**
         * true : means will set face beauty view to true, so the UI will show a
         * face beauty frame false: means will not show the full color face
         * frame,just is white color
         */
        Log.i(TAG, "[changeFaceBeautyStatues] enable = " + enable);
        mIModuleCtrl.setFaceBeautyEnalbe(enable);
    }

  

    private void setVFBPs(boolean isStart) {
        String value = isStart ? Util.VIDEO_FACE_BEAUTY_ENABLE : Util.VIDEO_FACE_BEAUTY_DISABLE;
        Log.i(TAG, "[setVFBPs] isStart = " + isStart + ",value = " + value);
        mICameraDevice.setParameter(Util.KEY_VIDEO_FACE_BEAUTY, value);
        // before set the ps,need update the ps
        mICameraDevice.applyParameters();
    }

    private void setVFBPreviewSizeRule() {
		/*
        mFaceBeautyPreviewSize = new FaceBeautyPreviewSize(mICameraContext);
        mISettingCtrl.addRule(SettingConstants.KEY_FACE_BEAUTY, SettingConstants.KEY_PICTURE_RATIO,
                mFaceBeautyPreviewSize);
        mFaceBeautyPreviewSize.addLimitation("on", null, null);*/
    }

    private void onShutterButtonLongPress() {
        mICameraAppUi.showInfo(mActivity.getString(R.string.gy_accessibility_switch_to_bokeh)
                + mActivity.getString(R.string.camera_continuous_not_supported));
    }

    private void onSinlgeTapUp(View view, int x, int y) {
        Log.i(TAG, "[onSingleTapUp]mCameraClosed:" + mCameraClosed + ",mCurrentState = "
                + getModeState() + ",mIFocusManager = " + mIFocusManager);
        if (mIFocusManager == null || mCameraClosed || ModeState.STATE_IDLE != getModeState()) {
            return;
        }
        if (ModeState.STATE_IDLE != getModeState()) {
            Log.i(TAG, "[onSingleTapUp] current state is = " + getModeState() + " ,so returen");
            return;
        }
        String focusMode = null;
        if (mIFocusManager != null) {
            focusMode = mIFocusManager.getFocusMode();
            Log.i(TAG, "[onSingleTapUp] current focusMode = " + focusMode);
        }
        if (mICameraDevice == null || focusMode == null
                || (Parameters.FOCUS_MODE_INFINITY.equals(focusMode))) {
            Log.i(TAG, "[onSinlgeTapUp]mICameraDevice = " + mICameraDevice + ",focusMode = "
                    + focusMode);
            return;
        }
        if (!mIFocusManager.getFocusAreaSupported()) {
            Log.i(TAG, "[onSinlgeTapUp]this project not supported Touch AF");
            return;
        }
        if (mICameraView != null) {
            mICameraView.update(HIDE_EFFECTS_ITEM);
        }
        mIFocusManager.onSingleTapUp(x, y);
        /*prize-xuchunming-20161202-start*/
        mGyBokehHelper.onSingleTapUp(view, x, y);
        /*prize-xuchunming-20161202-end*/
        Log.i(TAG, "[onSingleTapUp] end ");
    }

    private boolean startCapture() {
        if ((ModeState.STATE_IDLE != getModeState()) || !isEnoughSpace()) {
            Log.i(TAG, "[startCapture],invalid state, return!");
            return false;
        }
        Log.i(TAG, "[startCapture]...");
        // need set FB original capture callback and here
        mICameraAppUi.setSwipeEnabled(false);
        mICameraAppUi.setViewState(ViewState.VIEW_STATE_CAPTURE);

        // mtk add begin++
        /*prize-xuchunming-20170523-bugid:gallery2 can edit the picture of unbokeh takepicture-start*/
        if(isBuzzying() == true){
        /*prize-xuchunming-20170523-bugid:gallery2 can edit the picture of unbokeh takepicture-end*/
            Camera.Size curPictureSize = mICameraDevice.getParameters().getPictureSize();
            byte []uRawDataBuffer = new byte [curPictureSize.width*curPictureSize.height*2];
            mICameraDevice.addRawImageCallbackBuffer(uRawDataBuffer); // ????????????????raw_image
        }
        // mtk add end--

        //mICameraDevice.setcFBOrignalCallback(mCfbCallback);
        if (mIFeatureConfig.isVfbEnable()) {
            // because native not know the face beauty location when take
            // picture,
            // so need set the beauty location
           // setvFBFacePoints();
        }
        //os-add-for bokeh after process-20170428-pengcancan-start
        mRotation = Util.getRecordingRotation(mIModuleCtrl.getOrientation(),
                mICameraDeviceManager.getCurrentCameraId(),
                mICameraDeviceManager.getCameraInfo(mICameraDeviceManager.getCurrentCameraId()));
        Log.i("pengcc","[startCapture] rotate : " + mRotation);
        //os-add-for bokeh after process-20170428-pengcancan-end
        setModeState(ModeState.STATE_CAPTURING);
        closeSecondaryCamera();
        /*prize-xuchunming-20170523-bugid:gallery2 can edit the picture of unbokeh takepicture-start*/
        mICameraDevice.takePicture(mShutterCallback, isBuzzying() == true ? mRawPictureCallback : null, null,
                mJpegPictureCallback);
        /*prize-xuchunming-20170523-bugid:gallery2 can edit the picture of unbokeh takepicture-end*/
        return true;
    }

    private ShutterCallback mShutterCallback = new ShutterCallback() {

        @Override
        public void onShutter() {
            Log.d(TAG, "[mShutterCallback], time = " + System.currentTimeMillis());

        }
    };

    //os-add-for bokeh after process-20170428-pengcancan-start
    private long mPicTakenTime ;
    private int mRotation ;
    private long mBokehFileCreateTime = 0;
    private PictureCallback mRawPictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, final Camera camera) {
            mPicTakenTime = System.currentTimeMillis();
            if(data == null) {
            	Log.w(TAG, "mRawPictureCallback onPictureTaken data == null");
            	return;
            }
            
            //saveImageToSDCard((Storage.getBokehFileDirectory()+"/" + mPicTakenTime + ".yuv"),data);
            if (mBitmapExecutor == null){
                mBitmapExecutor = Executors.newFixedThreadPool(2);
            }
            mBokehFileCreateTime = mPicTakenTime;
            mBitmapExecutor.execute(new MyRunable(mBokehFileCreateTime) {
                long mDate = mBokehFileCreateTime;
                @Override
                public void run() {
                    long date = mBokehFileCreateTime;
                    Log.d(TAG,"[mRawPictureCallback,run], execute"+",start,currentTimeMillis(): "+System.currentTimeMillis()+"mPicTakenTime: "+mPicTakenTime+"mDate: "+mDate+",date: "+date+",mRotation: "+mRotation);
                    Camera.Size size = camera.getParameters().getPictureSize();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    YuvImage yuvImage = new YuvImage(data, ImageFormat.YUY2, size.width, size.height, null);
                    yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 90, out);
                    byte[] imageBytes = out.toByteArray();
                    if (mRotation != 0) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(mRotation);
                        Bitmap src = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        Bitmap mTemp = Bitmap.createBitmap(src, 0, 0, size.width, size.height, matrix, true);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        mTemp.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        imageBytes = stream.toByteArray();
                        if (src != null && !src.isRecycled()) {
                            src.recycle();
                        }
                        if (mTemp != null && !mTemp.isRecycled()) {
                            mTemp.recycle();
                        } 
                    }
                    mIFileSaver.init(FILE_TYPE.BOKEH, 0, null, -1);
                    mIFileSaver.savePhotoFile(imageBytes, null, date, mIModuleCtrl.getLocation(), 0,
                            mFileSavedListener);
                }
            });
        }
    };

    private void saveImageToSDCard(String filePath, byte[] data) {
        FileOutputStream out = null;
        try {
            // Write to a temporary file and rename it to the final name.
            // This
            // avoids other apps reading incomplete data.
            com.android.camera.Log.d(TAG, "[saveImageToSDCard]begin add the data to SD Card");
            out = new FileOutputStream(filePath);
            out.write(data);
            out.close();
        } catch (IOException e) {
            com.android.camera.Log.e(TAG, "[saveImageToSDCard]Failed to write image,ex:", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    com.android.camera.Log.e(TAG, "[saveImageToSDCard]IOException:", e);
                }
            }
        }
        com.android.camera.Log.i(TAG, "[saveImageToSDCard]end of add the data to SD Card");
    }
    //os-add-for bokeh after process-20170428-pengcancan-end

    private PictureCallback mPostViewPictureCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "[mPostViewPictureCallback], time = " + System.currentTimeMillis());
        }
    };

    private PictureCallback mJpegPictureCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "VFBCallback[mJpegPictureCallback], time = " + System.currentTimeMillis()
                    + ",data = " + data);
            
            if (ModeState.STATE_CLOSED == getModeState()) {
                Log.i(TAG, "[onPictureTaken] Camera is Closed");
                mICameraAppUi.restoreViewState();
                mICameraAppUi.setSwipeEnabled(true);
                return;
            }
            
            if (mCameraClosed) {
                Log.i(TAG, "[onPictureTaken] mCameraClosed:" + mCameraClosed);
                return;
            }
            
            if (data != null) {
                // prepare the save request
                //os-add-for bokeh after process-20170428-pengcancan-start
            	/*prize-xuchunming-20170523-bugid:gallery2 can edit the picture of unbokeh takepicture-start*/
                if (isBuzzying() == true) {
                /*prize-xuchunming-20170523-bugid:gallery2 can edit the picture of unbokeh takepicture-end*/
                    float times = 1f;
                    int x = size[2];
                    int y = size[3];
                    if (mParameters != null) {
                        Camera.Size previewSize = mParameters.getPreviewSize();
                        Camera.Size picSize = mParameters.getPictureSize();
                        if (previewSize.width > previewSize.height && picSize.width > picSize.height) {
                            times = picSize.width * times / previewSize.width;
                        } else {
                            times = picSize.width * times / previewSize.height;
                        }
                        switch (mRotation) {
                            case 0:
                                x = size[2];
                                y = size[3];
                                break;
                            case 90:
                                x = previewSize.height - size[3];
                                y = size[2];
                                break;
                            case 180:
                                x = previewSize.width - size[2];
                                y = previewSize.height - size[3];
                                break;
                            case 270:
                                x = size[3];
                                y = previewSize.width - size[2];
                                break;
                        }
                    }
                    JSONObject desc = new JSONObject();
                    try {
                        desc.put("x", x * times);
                        desc.put("y", y * times);
                        desc.put("radius", size[4]);
                        desc.put("level", size[5]);
                        desc.put("scope", size[6]);
                        desc.put("path", Storage.generateBokehFilepath(mContext, mPicTakenTime));
                        //Log.i(TAG, "[mJpegPictureCallback] desc : " + desc.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mIFileSaver.init(FILE_TYPE.BOKEH, 0, desc.toString(), -1);
                    mIFileSaver.savePhotoFile(data, null, mPicTakenTime, mIModuleCtrl.getLocation(), 0,
                            mFileSavedListener);
                    //Log.d(TAG,"[mJpegPictureCallback],mPicTakenTime: "+mPicTakenTime);
                } else {
                    mIFileSaver.init(FILE_TYPE.JPEG, 0, null, -1);
                    long time = System.currentTimeMillis();
                    mIFileSaver.savePhotoFile(data, null, time,  mIModuleCtrl.getLocation(), 0,
                            mFileSavedListener);
                }
            }
            //os-add-for bokeh after process-20170428-pengcancan-end
            
            // Ensure focus indicator
            mIFocusManager.updateFocusUI();
            // Need Restart preview ,synchronize Normal photo
            // ZSD don't call stop preview when Capture done.
            boolean needRestartPreivew = !"on".equals(mISettingCtrl
                    .getSettingValue(SettingConstants.KEY_CAMERA_ZSD));
            startPreview(false);
            mIModuleCtrl.startFaceDetection();
            mICameraAppUi.setSwipeEnabled(true);
            mICameraAppUi.restoreViewState();
            openSecondaryCamera();
        }
    };

    private void storeFaceBeautyLocation(Face[] faces) {
        int index = 0;
        // First :clear last time stored values
        if (mVfbFacesPoint != null && mVfbFacesPoint.size() != 0) {
            mVfbFacesPoint.clear();
        }
        // Seconded: store the new values to the list
        if (faces != null) {
            for (int i = 0; i < faces.length; i++) {
                if (100 == faces[i].score) {
                    int x = faces[i].rect.left + (faces[i].rect.right - faces[i].rect.left) / 2;
                    int y = faces[i].rect.top + (faces[i].rect.bottom - faces[i].rect.top) / 2;
                    mVfbFacesPoint.add(index++, x);
                    mVfbFacesPoint.add(index, y);
                }
            }
        }
    }

    private void setvFBFacePoints() {
        if (mVfbFacesPoint == null) {
            Log.i(TAG, "[vFB] mVfbFacesPoint,current points is null,return");
            return;
        }
        String value = setFacePose();
        if (value != null) {
          //  mICameraDevice.setParameter(FaceBeautyParametersHelper.KEY_VIDED_FACE_BEAUTY_FACE,value);
          //  mICameraDevice.applyParameters();
        }
    }

    private String setFacePose() {
        String value = "";
        for (int i = 0; i < mVfbFacesPoint.size(); i++) {
            value += mVfbFacesPoint.get(i);
            // why need (i +1) != mvFBFacesPoint.size() ?
            // because at the end of value,not need any symbol
            // the value format is: xxx:yyy,x1:y1
            if ((i + 1) != mVfbFacesPoint.size()) {
                if (i % 2 != 0) {
                    value += ",";
                } else {
                    value += ":";
                }
            }
        }
        Log.i(TAG, "[vFB] setFacePose,vaue = " + value);
        return value;
    }

    private OnFileSavedListener mFileSavedListener = new OnFileSavedListener() {
        @Override
        public void onFileSaved(Uri uri) {
            Log.i(TAG, "[onFileSaved]uri = " + uri);
        }
    };

    // CFB
    public class CfbCallback implements cFbOriginalCallback {

        @Override
        public void onOriginalCallback(byte[] data) {
            Log.d(TAG, "cFBCallback,[onOriginalCallback],data.length = " + data.length);
            /*if (!mIFeatureConfig.isVfbEnable()) {
                mIFileSaver.init(FILE_TYPE.JPEG, 0, null, -1);
                long time = System.currentTimeMillis();
                mIFileSaver.savePhotoFile(data, null, time,  mIModuleCtrl.getLocation(), 0,
                        mFileSavedListener);
            }*/
        }
    }
    


    private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {
        
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            Log.i(TAG, "[onAutoFocus] success = " + success +",current state = " + getModeState());
            if (ModeState.STATE_CLOSED == getModeState()) {
                Log.i(TAG, "[onAutoFocus]camera is closed,so return");
                return;
            }
            if (ModeState.STATE_FOCUSING == getModeState()) {
                mICameraAppUi.restoreViewState();
                setModeState(ModeState.STATE_IDLE);
            }
            mIFocusManager.onAutoFocus(success);
            mIsAutoFocusCallback = true;
        }
    };
    
    private final AutoFocusMvCallback mAutoFocusMvCallback = new AutoFocusMvCallback() {

        @Override
        public void onAutoFocusMoving(boolean start, Camera camera) {
        //    Log.i(TAG, "[onAutoFocusMoving]moving = " + start);
            mIFocusManager.onAutoFocusMoving(start);
        }
        
    };
    
    private void startPreview(boolean needStop) {
        Log.i(TAG, "[startPreview]needStop = " + needStop);
        
        mIsAutoFocusCallback = false;
        
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mIFocusManager.resetTouchFocus();
            }
        });
        if (needStop) {
            stopPreview();
        }
        mIFocusManager.setAeLock(false); // Unlock AE and AWB.
        mIFocusManager.setAwbLock(false);
        
        mIModuleCtrl.applyFocusParameters(false);
        Log.i(TAG, "set setFocusParameters normal");
        
        mICameraDevice.startPreview();
        mICameraDevice.setAutoFocusMoveCallback(mAutoFocusMvCallback);
        mIFocusManager.onPreviewStarted();
        setModeState(ModeState.STATE_IDLE);
    }
    
    private void stopPreview() {
        Log.i(TAG, "[stopPreview]mCurrentState = " + getModeState());
        if (ModeState.STATE_CLOSED == getModeState()) {
            Log.i(TAG, "[stopPreview]Preview is stopped.");
            return;
        }
        if (mICameraDevice == null) {
            updateDevice();
        }
        if (mICameraDevice.getFaceDetectionStatus()) {
            stopFaceDetection();
        }
       
        mAdditionManager.execute(AdditionActionType.ACTION_ON_STOP_PREVIEW);
        mICameraDevice.cancelAutoFocus(); // Reset the focus.
        mICameraDevice.setAutoFocusMoveCallback(null);
		mICameraDevice.stopPreview();
		if (mIFocusManager != null) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					mIFocusManager.onPreviewStopped();			
				}
			});
	    }
    }
    /*prize-xuchunming-20161202-start*/
    private void setBotkehCameraPreviewSizeRule() {
    	mBokehCameraPrieviewSizeRule = new BokehCameraPrieviewSizeRule(mICameraContext);
    	mISettingCtrl.addRule(SettingConstants.KEY_GYBOKEH_MODE, SettingConstants.KEY_PICTURE_RATIO,mBokehCameraPrieviewSizeRule);
    	mISettingCtrl.addRule(SettingConstants.KEY_GYBOKEH_MODE, SettingConstants.KEY_PICTURE_SIZE,null);
    	mBokehCameraPrieviewSizeRule.addLimitation("on", null, null);
    }
    
    private void onMvFocused() {
  		// TODO Auto-generated method stub
  		mGyBokehHelper.onMvFocused();
  	}

  	private void onMvFocusing() {
  		// TODO Auto-generated method stub
  		mGyBokehHelper.onMvFocusing();
  	}

  	private void onAutoFocused() {
  		// TODO Auto-generated method stub
  		mGyBokehHelper.onAutoFocused();
  	}

  	private void onAutoFocusing() {
  		// TODO Auto-generated method stub
  		mGyBokehHelper.onAutoFocusing();
  	}
  	
	/*private PreviewCallbackListen mPreviewCallback = new PreviewCallbackListen() {
		public void onPreviewFrame(byte[] data, final Camera camera) {
			mGyBokehHelper.doPreviewCallBack(data, camera);
			if (mBuzzyStrategy != null) {
				mBuzzyStrategy.saveMainBmp(data);
			}
		}
	};*/
	
	public void closeSecondaryCamera() {
		// TODO Auto-generated method stub
		if (mBuzzyStrategy != null) {
			mBuzzyStrategy.detachSurfaceViewLayout();
			mBuzzyStrategy.closeCamera();
		}
	}
	
	public void openSecondaryCamera() {
		if (mBuzzyStrategy != null) {
			mBuzzyStrategy.attachSurfaceViewLayout();
			if (myThreadHandler != null) {
        		myThreadHandler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(mContext.isCameraOpening() == false){
							mBuzzyStrategy.openCamera();
							mBuzzyStrategy.startPreview();
						}
					}
				});
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
					if(mHandler != null){
						mHandler.sendEmptyMessage(MSG_READ_SURFACE_DATA);
					}
				}
			};
			int time = mBuzzyStrategy != null ? mBuzzyStrategy.getCheckTime() : 700;
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
	
	public void stopHandle(){
		if(myThreadHandler != null){
			myThreadHandler.removeCallbacks(mRunnable);
			mHandlerThread.quitSafely();
			myThreadHandler = null;
			mHandlerThread = null;
		}
	}
	
	public void startHandle(){
		if(myThreadHandler == null){
			mHandlerThread = new HandlerThread(TAG, 1);
			mHandlerThread.start();
			myThreadHandler = new Handler(mHandlerThread.getLooper());
		}
	}
	
	private void showReminder() {
		/*prize-modify-bugid:52930 prompt language appears on the desktop -xiaoping-20180326-start*/ 
		mHandler.sendEmptyMessage(MSG_ON_SHOW_BLOCK_TOAST);
/*		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast toast = new Toast(mActivity);
				View view = LayoutInflater.from(mActivity).inflate(R.layout.double_camera_toast, null);
				toast.setView(view);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP, 0, (int) mContext.getResources().getDimension(R.dimen.setting_diaglog_btn_height));
				toast.show();
			}
		});*/
		/*prize-modify-bugid:52930 prompt language appears on the desktop -xiaoping-20180326-end*/ 
	}
	
	public GYSurfaceView getGlSurfaceView(){
		return mGyBokehHelper.getGlSurfaceView();
	}
	
	 public void pauseThread() {
         Log.d(TAG, "pause CameraStartUpThread");
         mConditionVariable.close();
         mConditionVariable.block();
     }
     
     public void resumeThread() {
         Log.d(TAG, "resume CameraStartUpThread");
         mConditionVariable.open();
     }
     
     
     private void onCameraClose() {
         Log.d(TAG, "[onCameraClose]");
         mAdditionManager.close(true);
         mCameraClosed = true;
         mGyBokehHelper.gyBokehclose(false);
	     mGyBokehHelper.gyBokehrelease();
	     stopTimer();
		 stopHandle();
	     closeSecondaryCamera();		 
	     
         ModeState state = getModeState();
         Log.d(TAG, "[onCameraClose]ModeState=" + state);
         if (ModeState.STATE_FOCUSING == state) {
             mICameraAppUi.restoreViewState();
         } else if (ModeState.STATE_CAPTURING == state) {
             mICameraAppUi.restoreViewState();
             mICameraAppUi.setSwipeEnabled(true);
         }
         setModeState(ModeState.STATE_CLOSED);
     }

     private void onShutterButtonClick() {
         boolean isEnoughSpace = mIFileSaver.isEnoughSpace();
         Log.i(TAG, "[onShutterButtonClick]isEnoughSpace = " + isEnoughSpace + ",mCameraClosed = "
                 + mCameraClosed + ",mCurrentState = " + getModeState());
         // Do not take the picture if there is not enough storage or camera is not available.
         if (!isEnoughSpace || isCameraNotAvailable()) {
             Log.w(TAG, "[onShutterButtonClick]return.");
             return;
         }
         Log.i(TAG,
                 "[CMCC Performance test][Camera][Camera] camera capture start ["
                         + System.currentTimeMillis() + "]");
         
         Log.i(TAG, "[execute]set CameraModeType.ACTION_PHOTO_SHUTTER_BUTTON_CLICK");
         LibVGestureDetect.CurState(0,size);
         /*gangyun tech modify  SLR continuous shooting probability there is no blur effect begin*/
		 mParameters =mContext.getCameraDeviceCtrl().getCurCameraDevice().getParameters();
		 mParameters.set("gy-bokeh-enable",  1);
		 mParameters.set("gy-bokeh-x",  size[2]);
		 mParameters.set("gy-bokeh-y", size[3]);
		 mParameters.set("gy-bokeh-radius",  size[4]);
		 mParameters.set("gy-bokeh-level",  size[5]);
		 mParameters.set("gy-bokeh-transition",  size[6]); 
		 Log.i(TAG, "gycapture x:"+size[2] + " y:"+size[3]+" radius:"+size[4]+" level:"+size[5]+" trasition:"+size[6]);	
         //mContext.getCameraDevice().getCamera().setParameters(mParameters);
         mContext.getCameraDeviceCtrl().getCurCameraDevice().applyParametersToServer();
         /*gangyun tech modify  SLR continuous shooting probability there is no blur effect end*/	
         
	     //first need hide the effects item if the effects have expanded
	     if (mICameraView != null) {
	         mICameraView.update(HIDE_EFFECTS_ITEM);
	     }
	     if (mIFocusManager != null) {
	         mIFocusManager.focusAndCapture();
	     }
     }

     private boolean isCameraNotAvailable() {
         ModeState modeState = getModeState();
         Log.d(TAG, "isCameraNotAvailable modeState " + modeState);
         return (ModeState.STATE_CAPTURING == modeState || ModeState.STATE_SAVING == modeState ||
                 ModeState.STATE_CLOSED == modeState) ? true : false;
     }

  	/*prize-xuchunming-20161202-end*/
     
     /*prize-xuchunming-20160215-bugid:28649-start*/
     public void deleteBotkehCameraPreviewSizeRule(){
    	 mISettingCtrl.addRule(SettingConstants.KEY_GYBOKEH_MODE, SettingConstants.KEY_PICTURE_RATIO,null);
     }
     /*prize-xuchunming-20160215-bugid:28649-end*/
	 
	/*prize-xuchunming-20170523-bugid:gallery2 can edit the picture of unbokeh takepicture-start*/
     public boolean isBuzzying(){
    	 if(FeatureSwitcher.isGyBokehEmptinessShow() == true && mGyBokehHelper.isBgFuzzySwitchOn() == true && isBuzzyBackgroud == true){
    		 return true;
    	 }else{
    		 return false;
    	 }
     }
     /*prize-xuchunming-20170523-bugid:gallery2 can edit the picture of unbokeh takepicture-end*/
}

