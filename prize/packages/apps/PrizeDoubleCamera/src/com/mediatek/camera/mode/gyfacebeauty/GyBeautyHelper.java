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

public class GyBeautyHelper  {
    private static final String TAG = "GyBeautyHelper";
    
    public GyBeautyHelper(CameraActivity context) {
        mContext = context;
    }
    
    private android.widget.SeekBar mSeekBar;
    private static int mSmoothValue = 0;
    private static int mWhiteningValue = 0;
    private int  mWhitening = 3;
    private boolean isInPopupSetting = false;
    private CameraActivity mContext;
    private Parameters mParameters;


public void startGYBeauty(int isEnable, int smooth, int whitening){
    Log.i(TAG, "gangyun1 startGYBeauty  a");
    if(mContext.getCameraDevice() == null){
      return;
   }

	Log.i(TAG, "gangyun1 startGYBeauty isEnable = " +isEnable + " smooth ="+smooth + " whitening = "+whitening);
	mParameters =mContext.getCameraDevice().getCamera().getParameters();
	if(isEnable == 1 && smooth == 0 && whitening == 0){
		mParameters.set("gyBeautyEnable",  isEnable);
		mParameters.set("gyBeautySmooth",  mSmoothValue);
		mParameters.set("gyBeautyWhitening",  mWhiteningValue);
	}
	else{
		mParameters.set("gyBeautyEnable",  isEnable);
		mParameters.set("gyBeautySmooth",  smooth);
		mParameters.set("gyBeautyWhitening",  whitening);
		mSmoothValue = smooth;
		mWhiteningValue = whitening;

	}
   mContext.getCameraDevice().getCamera().setParameters(mParameters);
	 
}
 
}
