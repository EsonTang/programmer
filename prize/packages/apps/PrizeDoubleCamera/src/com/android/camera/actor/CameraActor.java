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
package com.android.camera.actor;

import android.content.Intent;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.FaceDetectionListener;
import android.provider.MediaStore;
import com.android.camera.Log;
import android.view.KeyEvent;
import android.view.View.OnClickListener;

import com.android.camera.CameraActivity;
import com.android.camera.CameraActivity.OnLongPressListener;
import com.android.camera.CameraActivity.OnSingleTapUpListener;
import com.android.camera.FocusManager;
import com.android.camera.FocusManager.Listener;
import com.android.camera.bridge.CameraDeviceCtrl.PreviewCallbackListen;
import com.android.camera.manager.ModePicker;
import com.android.camera.ui.ShutterButton.OnShutterButtonListener;
import com.mediatek.camera.ICameraMode.CameraModeType;
import com.mediatek.camera.ISettingCtrl;
import com.mediatek.camera.setting.SettingConstants;

//just control capture flow, don't set parameters
public abstract class CameraActor {
    private static final String TAG = "CameraActor";

    protected final CameraActivity mContext;
    protected FocusManager mFocusManager;

    public CameraActor(final CameraActivity context) {
        mContext = context;
    }

    public CameraActivity getContext() {
        return mContext;
    }

    public AutoFocusMoveCallback getAutoFocusMoveCallback() {
        return null;
    }

    public ErrorCallback getErrorCallback() {
        return null;
    }

    public FaceDetectionListener getFaceDetectionListener() {
        return null;
    }

    // user action
    public boolean onUserInteraction() {
        return false;
    }

    public boolean onBackPressed() {
        return false;
    }

    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        return false;
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    }

    public void onMediaEject() {
    }

    public void onRestoreSettings() {
    }
    
    public void onModeBlueChange() {
    }
    
    public void onModeBlueDone() {
    }

    public void onNavigatChange(boolean isShow) {
    }
    // public void onConfigurationChanged(Configuration newConfig){}
    // public void onCameraSwitched(int newCameraId){}//not recommended

    // shutter button callback
    public OnShutterButtonListener getVideoShutterButtonListener() {
        return null;
    }

    public OnShutterButtonListener getPhotoShutterButtonListener() {
        return null;
    }

    public OnSingleTapUpListener getonSingleTapUpListener() {
        return null;
    }

    public OnLongPressListener getonLongPressListener() {
        return null;
    }

    public OnClickListener getPlayListener() {
        return null;
    }

    public OnClickListener getRetakeListener() {
        return null;
    }

    public OnClickListener getOkListener() {
        return null;
    }

    public OnClickListener getCancelListener() {
        return null;
    }

    public Listener getFocusManagerListener() {
        return null;
    }

    // camera life cycle
    public void onCameraOpenDone() {
    }// called in opening thread

    public void onCameraOpenFailed() {
    }

    public void onCameraDisabled() {
    }

    public void onCameraParameterReady(boolean startPreview) {
    }// may be called in opening thread

    public void stopPreview() {

    }

    public void onCameraClose() {
    }

    public boolean handleFocus() {
        return false;
    }

    public void release() {
    }

    public void onOrientationChanged(int orientation) {
    }

    public abstract int getMode();

    protected boolean isFromInternal() {
        final Intent intent = mContext.getIntent();
        final String action = intent.getAction();
        Log.i(TAG, "Check action = " + action);
        // menu helper ?
        return (MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA.equals(action));
    }

    public void onDisplayRotate() {
    }

    public void onLongPress(int x, int y) {
    }

    public void setSurfaceTextureReady(boolean ready) {
    }

    public void onCameraDeviceSwitch() {

    }

    public void startFaceDetection() {

    }

    public void stopFaceDetection() {

    }

    public void onSettingChange(String key, String value) {
    }
    
    public CameraModeType getCameraModeType(int newMode) {
        CameraModeType mode = null;
        switch (newMode) {
        case ModePicker.MODE_PANORAMA:
            mode = CameraModeType.EXT_MODE_PANORAMA;
            break;

        case ModePicker.MODE_PHOTO:
            mode = CameraModeType.EXT_MODE_PHOTO;
            break;

        case ModePicker.MODE_PHOTO_PIP:
            mode = CameraModeType.EXT_MODE_PHOTO_PIP;
            break;

        case ModePicker.MODE_STEREO_CAMERA:
            mode = CameraModeType.EXT_MODE_STEREO_CAMERA;
            break;

        case ModePicker.MODE_VIDEO:
            mode = CameraModeType.EXT_MODE_VIDEO;
            break;

        case ModePicker.MODE_FACE_BEAUTY:
            mode = CameraModeType.EXT_MODE_FACE_BEAUTY;
            break;

        case ModePicker.MODE_VIDEO_PIP:
            mode = CameraModeType.EXT_MODE_VIDEO_PIP;
            break;
        case ModePicker.MODE_WATERMARK:
        	mode = CameraModeType.EXT_MODE_WATERMARK;
        	break;
        case ModePicker.MODE_PREVIDEO:
        	mode = CameraModeType.EXT_MODE_PREVIDEO;
        	break;
        case ModePicker.MODE_DOUBLECAMERA:
        	mode = CameraModeType.EXT_MODE_DOUBLECAMERA;
        	break;
			
	    //gangyun tech add begin
	    case ModePicker.MODE_FACEART:
            mode = CameraModeType.EXT_MODE_GY_BEAUTY;
            break;
        case ModePicker.MODE_BOKEH:
            mode = CameraModeType.EXT_MODE_GY_BOKEH;
            break;
        //gangyun tech add end   
       
        /*arc add start*/
	    case ModePicker.MODE_PICTURE_ZOOM:
            mode = CameraModeType.EXT_MODE_PICTURE_ZOOM;
            break;
        case ModePicker.MODE_LOWLIGHT_SHOT:
            mode = CameraModeType.EXT_MODE_LOWLIGHT_SHOOT;
            break;
        case ModePicker.MODE_SHORT_PREVIDEO:
            mode = CameraModeType.EXT_MODE_SHORT_PREVIDEO;
            break;
        //add portrait mode
        case ModePicker.MODE_PORTRAIT:
            mode = CameraModeType.EXT_MODE_PORTRAIT;
            break;
        /*arc add end*/ 

        default:
            break;
        }
        return mode;
    }
    
    /*prize Camera mute wanzhijuan 2016-5-30 start*/
    protected boolean isCameraUnMute() {
    	ISettingCtrl settingCtrl = getContext().getISettingCtrl();
		String value = settingCtrl.getSettingValue(SettingConstants.KEY_CAMERA_MUTE);
		Log.i(TAG, "isCameraUnMute() value=" + value);
		boolean isUnMute = "on".equals(value) ? false : true;
		return isUnMute;
	}
    /*prize Camera mute wanzhijuan 2016-5-30 end*/
    
    public void onShowInfo() {
    }

    public void onHideInfo() {
    }

    public void openFrontCaemra() {
    }

	public void onPause() {
		// TODO Auto-generated method stub
	}
	
	/*prize-xuchunming-20160829-add previewCallback interface-start*/
	public PreviewCallbackListen getPreviewCallbackListen(){
		return null;
	}
	/*prize-xuchunming-20160829-add previewCallback interface-end*/
	public void onAutoFocusing() {
		// TODO Auto-generated method stub
	}
	
	public void onAutoFocused() {
		// TODO Auto-generated method stub
	}
	
	public void onMvFocusing() {
		// TODO Auto-generated method stub
	}
	
	public void onMvFocused() {
		// TODO Auto-generated method stub
	}
}
