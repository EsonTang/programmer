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

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import com.android.camera.R;

import com.mediatek.camera.AdditionManager;
import com.mediatek.camera.ICameraAddition;
import com.mediatek.camera.ICameraContext;
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
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import com.mediatek.camera.platform.ICameraDeviceManager;
import com.mediatek.camera.platform.ICameraDeviceManager.ICameraDevice;
import com.android.camera.CameraActivity;
import com.mediatek.camera.mode.gyfacebeauty.GyBeautyHelper;

public class GyBeautyMode extends CameraMode implements ICameraAddition.Listener,
        IFocusManager.FocusListener {
    private static final String TAG = "gyBeautyMode";
    
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
    private static final int MSG_RESET_BEAUTY = 11;
    private static final int MSG_SET_BEAUTY = 12;
    
    
    private CfbCallback mCfbCallback = new CfbCallback();
    private Handler mHandler;
//    private FaceBeautyPreviewSize mFaceBeautyPreviewSize;
   // private FaceBeautyParametersHelper mFaceBeautyParametersHelper;
  //  private ParameterListener mParameterListener;
    
    private ICameraView mICameraView;
    private ArrayList<Integer> mVfbFacesPoint = new ArrayList<Integer>();
    private AdditionManager mAdditionManager;
    
    private boolean mIsAutoFocusCallback = false;
  //  private Activity mContext;
    private CameraActivity mContext;
    private GyBeautyHelper gyBeautyHelper;
    public GyBeautyMode(ICameraContext cameraContext) {
        super(cameraContext);
        Log.i(TAG, "[GyBeautyMode]constructor...");
        // first check the camera device

      //  mFaceBeautyParametersHelper = new FaceBeautyParametersHelper(cameraContext);
      //  mParameterListener = mFaceBeautyParametersHelper.getListener();

        if (mIFeatureConfig.isVfbEnable()) {
            // just Vfb have View,so if current project is only support cFB,so
            // not have view
         //   mICameraView = mICameraAppUi.getCameraView(SpecViewType.MODE_FACE_BEAUTY);
          //  mICameraView.init(mActivity, mICameraAppUi, mIModuleCtrl);
      //      mICameraView.setListener(mParameterListener);
           // mICameraAppUi.changeBackToVFBModeStatues(false);
        }
		
       mContext = (CameraActivity)cameraContext.getActivity();

        gyBeautyHelper = new GyBeautyHelper((CameraActivity)mContext);
        mHandler = new MainHandler(mActivity.getMainLooper());
        mAdditionManager = cameraContext.getAdditionManager();
        if (mIFeatureConfig.isVfbEnable()) {
           // setVFBPreviewSizeRule();
        }
    }

    private android.widget.SeekBar mSeekBar;
    private android.widget.SeekBar mSeekBar2;
    private static int mSmoothValue = -1;
    private static int mWhiteningValue = 0;
    private int  mWhitening = 6;
    private boolean isInPopupSetting = false;


private void startGYBeauty(int isEnable, int smooth, int whitening){
    Log.i(TAG, "gangyun1 startGYBeauty  a");
    if(mContext.getCameraDevice() == null){
      return;
   }
	Log.i(TAG, "gangyun1 startGYBeauty isEnable = " +isEnable + " smooth ="+smooth + " whitening = "+whitening);
//	startGYBeautyEnable(isEnable);
	//setGYBeautySmooth(smooth);
//	setGYBeautyWhitening(whitening);
	 
}
   private void initBeautifySkinLevelBar(){
    
            if (true) {
               Log.i(TAG, "initBeautifySkinLevelBar ");
            }

         int camerId = mICameraDeviceManager.getCurrentCameraId();
        Log.i(TAG, "[updateDevice] camerId = " + camerId);
		
   	 if (mContext.findViewById(R.id.gy_rl_leveltip) != null ){
		 ((com.android.camera.ui.Rotatable)mContext.findViewById(R.id.gy_rl_leveltip)).setOrientation(90, true);
 	 }
	 if (mContext.findViewById(R.id.gy_rl_levelseek) != null){
	 	((com.android.camera.ui.Rotatable)mContext.findViewById(R.id.gy_rl_levelseek)).setOrientation(180, true);
   	 }

        mSeekBar = (android.widget.SeekBar)mContext.findViewById(R.id.gy_beatifySeekBar);
        
        int seekbarmax = 180;
        if (mSmoothValue == -1){
       	    mSmoothValue = seekbarmax/2;
	          mWhiteningValue = mSmoothValue / mWhitening;
        }

        mSeekBar.setMax(seekbarmax);
	//mSeekBar.setEnabled(true);
	//mSeekBar.setClickable(true);
//	mSeekBar.
	if (mSeekBar instanceof com.gangyun.camera.FilterSeekBar)
	{
    //	mSeekBar.setProgress(mSmoothValue);
 		 ((com.gangyun.camera.FilterSeekBar)mSeekBar).setProgressAndThumb(mSmoothValue);
	}
	else{
		mSeekBar.setProgress(mSmoothValue);
	}
	
        mSeekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
			// TODO Auto-generated method stub
			//if ( null != mICameraContext.getCameraDevice())
			{
			       //	mICameraContext.getCameraDevice().setGYBeautySmooth(progress);
				mSmoothValue = progress;
				
				//mICameraContext.getCameraDevice().setGYBeautyWhitening(progress/mWhitening);
				mWhiteningValue = progress/mWhitening;
				 Log.i(TAG, "gangyun11 setOnSeekBarChangeListener ");
				startGYBeauty(1,mSmoothValue,mWhiteningValue);
			}
		}
	
		@Override
		public void onStartTrackingTouch(android.widget.SeekBar arg0) {
			// TODO Auto-generated method stub
			 Log.i(TAG, "gangyun11 onStartTrackingTouch ");
		
		}

		@Override
		public void onStopTrackingTouch(android.widget.SeekBar arg0) {
			// TODO Auto-generated method stub
			Log.i(TAG, "gangyun11 onStopTrackingTouch ");
			
	}});
        
         if(!isInPopupSetting){
	    mSeekBar.setVisibility(android.view.View.VISIBLE);
	    mSeekBar.postInvalidate();
        } 

	 startGYBeauty(1, mSmoothValue, mWhiteningValue);
    }
   
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
        if (mIModuleCtrl.getNextMode() != null) {
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
        mAdditionManager.close(true);

	 startGYBeauty(0,0,0);
	// mContext.findViewById(R.id.gy_beatifySeekBar).setVisibility(android.view.View.INVISIBLE);
     //   mContext.findViewById(R.id.gy_exposureTextView).setVisibility(android.view.View.GONE);  
			 
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
            break;

        case ACTION_ON_CAMERA_CLOSE:
            setModeState(ModeState.STATE_CLOSED);
            if (mIFeatureConfig.isVfbEnable()) {
                //mICameraView.update(ON_CAMERA_CLOSED);
            }
	     Log.i(TAG, "[execute]set CameraModeType.ACTION_ON_CAMERA_CLOSE");
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
            mHandler.sendEmptyMessage(ON_CAMERA_PARAMETERS_READY);

            mAdditionManager.onCameraParameterReady(true);
           mHandler.sendEmptyMessageDelayed(MSG_RESET_BEAUTY,  500);
	     
			
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
	     mHandler.sendEmptyMessageDelayed(MSG_SET_BEAUTY,  100);
            break;

        case ACTION_ON_FULL_SCREEN_CHANGED:
	     gyBeautyHelper.startGYBeauty(1,0,0);
            if (mICameraView != null) {
                mICameraView.update(ON_FULL_SCREEN_CHANGED, (Boolean) arg[0]);
            }
            break;
            
        case ACTION_SHUTTER_BUTTON_FOCUS:
            //when focus is detected, not need to do AF
            break;
            
        case ACTION_PHOTO_SHUTTER_BUTTON_CLICK:
	     	     Log.i(TAG, "[execute]set CameraModeType.ACTION_PHOTO_SHUTTER_BUTTON_CLICK");			
   //mAdditionManager = mICameraContext.getAdditionManager();
            //first need hide the effects item if the effects have expanded
            if (mICameraView != null) {
                mICameraView.update(HIDE_EFFECTS_ITEM);
            }
            if (mIFocusManager != null) {
                mIFocusManager.focusAndCapture();
            }
            break;

        case ACTION_SHUTTER_BUTTON_LONG_PRESS:
	     Log.i(TAG, "[execute]set CameraModeType.ACTION_SHUTTER_BUTTON_LONG_PRESS");				
            mICameraAppUi.showInfo(mActivity.getString(R.string.pref_camera_capturemode_enrty_fb)
                    + mActivity.getString(R.string.camera_continuous_not_supported));
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
            break;
        
        case ACTION_ON_CONFIGURATION_CHANGED:
            if (mHandler != null) {
                mHandler.sendEmptyMessage(ON_CONFIGURATION_CHANGED);
            }
            break;
            
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
       // setModeState(ModeState.STATE_SAVING);
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
        Log.i(TAG, "[setFocusParameters]mIsAutoFocusCallback = " + mIsAutoFocusCallback);
        mIModuleCtrl.applyFocusParameters(!mIsAutoFocusCallback);
        mIsAutoFocusCallback = false;
    }

    @Override
    public void playSound(int soundId) {
        mCameraSound.play(soundId);
    }
    //<------IFocusManager.FocusListener
    
    private void removeAllMsg() {
        if (mHandler != null) {
            mHandler.removeMessages(ON_CAMERA_PARAMETERS_READY);
            mHandler.removeMessages(INFO_FACE_DETECTED);
            mHandler.removeMessages(ORIENTATION_CHANGED);
	        mHandler.removeMessages(ORIENTATION_CHANGED);
	        mHandler.removeMessages(MSG_SET_BEAUTY);

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
                if (mICameraView != null) {
                    mICameraView.update(ON_CAMERA_PARAMETERS_READY);
                    mICameraView.show();
                }
                // have clear the face view
                // so you need initialize yourself
                mIModuleCtrl.initializeFrameView(false);
                setVFBParameters();
                //after set the parameters to native ,enable face beauty frame
               // changeFaceBeautyStatues(true);
                
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
				
	       case MSG_RESET_BEAUTY:
                  //    initBeautifySkinLevelBar();
                break;
		case MSG_SET_BEAUTY:
			  startGYBeauty(1, mSmoothValue, mWhiteningValue);				
                 break;
            default:
                break;
            }
        }
    }

    private void setVFBParameters() {
        if (mICameraDevice != null && mIModuleCtrl.isNonePickIntent()
                && mIFeatureConfig.isVfbEnable()) {
            // if face-beauty value is false,means current need to start VFB
            Log.d(TAG, "face-beauty will set to :ture");
            if (isOnlyMultiFaceBeautySupported()) {
              //  mICameraDevice.setParameter(Util.KEY_FB_EXTREME_BEAUTY, "false");
            }
         //   setVFBPs(true);
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

    private boolean isOnlyMultiFaceBeautySupported() {
        // fb-extreme-beauty-supported is false means just supported Multi face
        // mode,
        // so need remove the single face mode in settings
        //boolean isOnlySupported = "false".equals(mICameraDevice.getParameters().get(
        //        Util.KEY_FB_EXTEME_BEAUTY_SUPPORTED));
        boolean isOnlySupported = "false".equals(mICameraDevice
                .getParameter(Util.KEY_FB_EXTEME_BEAUTY_SUPPORTED));
        Log.i(TAG, "isOnlyMultiFaceBeautySupported = " + isOnlySupported);
        return isOnlySupported;
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
        mICameraAppUi.showInfo(mActivity.getString(R.string.pref_camera_capturemode_enrty_fb)
                + mActivity.getString(R.string.camera_continuous_not_supported));
    }

    private void onSinlgeTapUp(View view, int x, int y) {
        Log.i(TAG, "[onSingleTapUp] mIFocusManager = " + mIFocusManager);
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

        mICameraDevice.setcFBOrignalCallback(mCfbCallback);
        if (mIFeatureConfig.isVfbEnable()) {
            // because native not know the face beauty location when take
            // picture,
            // so need set the beauty location
           // setvFBFacePoints();
        }
        setModeState(ModeState.STATE_CAPTURING);
        mICameraDevice.takePicture(mShutterCallback, null, null,
                mJpegPictureCallback);

        return true;
    }

    private ShutterCallback mShutterCallback = new ShutterCallback() {

        @Override
        public void onShutter() {
            Log.d(TAG, "[mShutterCallback], time = " + System.currentTimeMillis());

        }
    };

    private PictureCallback mRawPictureCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "[mRawPictureCallback], time = " + System.currentTimeMillis());

        }
    };

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
            
            if (data != null) {
                // prepare the save request
                mIFileSaver.init(FILE_TYPE.JPEG, 0, null, -1);
                long time = System.currentTimeMillis();
                mIFileSaver.savePhotoFile(data, null, time,  mIModuleCtrl.getLocation(), 0,
                        mFileSavedListener);
            }
            
            // Ensure focus indicator
            mIFocusManager.updateFocusUI();
            // Need Restart preview ,synchronize Normal photo
            // ZSD don't call stop preview when Capture done.
            boolean needRestartPreivew = !"on".equals(mISettingCtrl
                    .getSettingValue(SettingConstants.KEY_CAMERA_ZSD));
            startPreview(needRestartPreivew);
            mIModuleCtrl.startFaceDetection();
            mICameraAppUi.setSwipeEnabled(true);
            mICameraAppUi.restoreViewState();
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
            if (!mIFeatureConfig.isVfbEnable()) {
                mIFileSaver.init(FILE_TYPE.JPEG, 0, null, -1);
                long time = System.currentTimeMillis();
                mIFileSaver.savePhotoFile(data, null, time,  mIModuleCtrl.getLocation(), 0,
                        mFileSavedListener);
            }
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
            Log.i(TAG, "[onAutoFocusMoving]moving = " + start);
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
        mICameraAppUi.restoreViewState();
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
	mICameraDevice.cancelAutoFocus(); // Reset the focus.
	mICameraDevice.stopPreview();
	if (mIFocusManager != null) {
		mActivity.runOnUiThread(new Runnable() {
		public void run() {
			mIFocusManager.onPreviewStopped();			
		}
	});
    }
  }
}
