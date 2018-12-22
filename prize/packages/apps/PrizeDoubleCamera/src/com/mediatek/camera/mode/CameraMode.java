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
package com.mediatek.camera.mode;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaActionSound;
import android.widget.ImageView;

import com.mediatek.camera.ICameraContext;
import com.mediatek.camera.ICameraMode;
import com.mediatek.camera.ISettingCtrl;
import com.mediatek.camera.platform.ICameraAppUi;
import com.mediatek.camera.platform.ICameraDeviceManager;
import com.mediatek.camera.platform.ICameraDeviceManager.ICameraDevice;
import com.mediatek.camera.platform.IFeatureConfig;
import com.mediatek.camera.platform.IFileSaver;
import com.mediatek.camera.platform.IFocusManager;
import com.mediatek.camera.platform.IModuleCtrl;
import com.mediatek.camera.platform.ISelfTimeManager;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.util.Log;
import com.prize.setting.SettingTool;

import junit.framework.Assert;

public abstract class CameraMode implements ICameraMode {
    private static final String TAG = "CameraMode";

    private ModeState mCurrentModeState = ModeState.STATE_UNKNOWN;
    protected Activity mActivity;
    protected MediaActionSound mCameraSound;
    protected IModuleCtrl mIModuleCtrl;
    protected ISettingCtrl mISettingCtrl;
    protected ICameraAppUi mICameraAppUi;
    protected ICameraDeviceManager mICameraDeviceManager;
    protected IFeatureConfig mIFeatureConfig;
    protected IFileSaver mIFileSaver;
    protected ISelfTimeManager mISelfTimeManager;

    protected IFocusManager mIFocusManager;
    protected ICameraDevice mICameraDevice;

    protected ICameraContext mICameraContext;
    
    private final static int CACHE_BUFFER_NUM = 3;
    private byte[][] mPreviewCallbackBuffers = new byte[CACHE_BUFFER_NUM][];

    public CameraMode(ICameraContext cameraContext) {
        Assert.assertNotNull(cameraContext);

        mActivity = cameraContext.getActivity();
        mCameraSound = new MediaActionSound();

        mICameraContext = cameraContext;

        mIModuleCtrl = cameraContext.getModuleController();
        mISettingCtrl = cameraContext.getSettingController();
        mICameraAppUi = cameraContext.getCameraAppUi();
        mICameraDeviceManager = cameraContext.getCameraDeviceManager();
        mIFeatureConfig = cameraContext.getFeatureConfig();
        mIFileSaver = cameraContext.getFileSaver();
        mISelfTimeManager = cameraContext.getSelfTimeManager();

        Assert.assertNotNull(mIModuleCtrl);
        Assert.assertNotNull(mISettingCtrl);
        Assert.assertNotNull(mICameraAppUi);
        Assert.assertNotNull(mICameraDeviceManager);
        Assert.assertNotNull(mIFeatureConfig);
        Assert.assertNotNull(mIFileSaver);
        Assert.assertNotNull(mISelfTimeManager);
    }

    // when receive ACTION_ON_CAMERA_OPEN, then mIFocusManager and
    // mICameraDevice could be assigned value
    protected void updateDevice() {
        int camerId = mICameraDeviceManager.getCurrentCameraId();
        Log.i(TAG, "[updateDevice] camerId = " + camerId);
        mICameraDevice = mICameraDeviceManager.getCameraDevice(camerId);
        Assert.assertNotNull(mICameraDevice);
    }

    // when receive ACTION_ON_CAMERA_PARAMETERS_READY, then mIFocusManager and
    // mICameraDevice could be assigned value
    protected void updateFocusManager() {
        Log.i(TAG, "[updateFocusManager]...");
        mIFocusManager = mICameraContext.getFocusManager();
        Assert.assertNotNull(mIFocusManager);
    }

    @Override
    public ModeState getModeState() {
        return mCurrentModeState;
    }

    @Override
    public void setModeState(ModeState state) {
        mCurrentModeState = state;
        Log.i(TAG, "[setCameraState] state = " + state);
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destory() {
    }

    @Override
    public boolean open() {
        return false;
    }

    @Override
    public boolean close() {
        Log.i(TAG, "[close]...");
        mCameraSound.release();
        mCameraSound = null;
        return true;
    }

    @Override
    public boolean isDisplayUseSurfaceView() {
        return true;
    }

    @Override
    public boolean isDeviceUseSurfaceView() {
        return true;
    }

    @Override
    public SurfaceTexture getBottomSurfaceTexture() {
        return null;
    }

    @Override
    public SurfaceTexture getTopSurfaceTexture() {
        return null;
    }

    @Override
    public boolean execute(ActionType type, Object... arg) {
        return false;
    }

    @Override
    public boolean isRestartCamera() {
        return false;
    }

    @Override
    public boolean isNeedDualCamera() {
        return false;
    }

    protected boolean isEnoughSpace() {
        return mIFileSaver.isEnoughSpace();
    }
    
    /*prize Front camera touch screen camera wanzhijuan 2016-5-30 start*/
    protected void checkFrontTouchShutter() {
    	if (SettingTool.isTouchShutter(mISettingCtrl)) {
    		ImageView shutterIm = mICameraAppUi.getPhotoShutter();
    		Log.d(TAG, "checkFrontTouchShutter() shutterIm=" + shutterIm);
    		if (shutterIm != null && shutterIm.isEnabled()) {
    			shutterIm.performClick();
    		}
    	}
    }
    /*prize Front camera touch screen camera wanzhijuan 2016-5-30 end*/
    
    /*prize Camera mute wanzhijuan 2016-5-30 start*/
    protected boolean isCameraUnMute() {
		String value = mISettingCtrl.getSettingValue(SettingConstants.KEY_CAMERA_MUTE);
		Log.i(TAG, "isCameraUnMute() value=" + value);
		boolean isUnMute = "on".equals(value) ? false : true;
		return isUnMute;
	}
    /*prize Camera mute wanzhijuan 2016-5-30 end*/
    
}
