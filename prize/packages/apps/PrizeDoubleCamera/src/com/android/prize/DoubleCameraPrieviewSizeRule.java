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
package com.android.prize;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera.Size;

import com.android.camera.CameraActivity;
import com.android.camera.bridge.CameraAppUiImpl;
import com.mediatek.camera.ICameraContext;
import com.mediatek.camera.ISettingCtrl;
import com.mediatek.camera.ISettingRule;
import com.mediatek.camera.platform.ICameraDeviceManager;
import com.mediatek.camera.platform.ICameraDeviceManager.ICameraDevice;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingDataBase;
import com.mediatek.camera.setting.SettingUtils;
import com.mediatek.camera.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DoubleCameraPrieviewSizeRule implements ISettingRule {
   private static final String TAG = "DoubleCameraPrieviewSizeRule";

   private static final int UNKNOWN_INDEX = -1;

   private ICameraContext mICameraContext;
   private ICameraDeviceManager mICameraDeviceManager;
   private ICameraDevice mICameraDevice;
   private ISettingCtrl mISettingCtrl;

   private Activity mActivity;
   private int mCameraId;
   private Size mCurrentPreviewSize = null;
   private List<String> mConditions = new ArrayList<String>();

   public static final int DOUBLE_CAMERA_MAX_WIDTH = 640;
   public static final int DOUBLE_CAMERA_MAX_HEIGHT = 480;
   
   public DoubleCameraPrieviewSizeRule(ICameraContext ct) {
       mICameraContext = ct;
       mActivity = mICameraContext.getActivity();
       mISettingCtrl = mICameraContext.getSettingController();
       Log.i(TAG, "[DoubleCameraPrieviewSizeRule]");
   }

   @Override
   public void execute() {
       String value = mISettingCtrl.getSettingValue(SettingConstants.KEY_DOUBLE_CAMERA);
       int index = mConditions.indexOf(value);
       Log.i(TAG, "[execute],index = " + index);

       initizeParameters();

       if (UNKNOWN_INDEX == index) {
    	   restoreCameraPreviewSize();
       } else {
           seDoubleCameraPreviewSize();
       }
   }

   @Override
   public void addLimitation(String condition, List<String> result, MappingFinder mappingFinder) {
       mConditions.add(condition);
   }

   private void initizeParameters() {
       if (mICameraDeviceManager == null) {
           mICameraDeviceManager = mICameraContext.getCameraDeviceManager();
       }
       mCameraId = mICameraDeviceManager.getCurrentCameraId();
       mICameraDevice = mICameraDeviceManager.getCameraDevice(mCameraId);
       if (mICameraDevice == null) {
           Log.e(TAG, "[initizeParameters] current mICameraDevice is null");
       } else {
           mCurrentPreviewSize = mICameraDevice.getPreviewSize();
           Log.i(TAG, "[initizeParameters] mCurrentPreviewSize : " + mCurrentPreviewSize.width
                   + " X " + ",width = " + mCurrentPreviewSize.height);
       }
   }

   private void setPictureSize(String previewRation , String pictureSize){
	   if(pictureSize != null && previewRation != null && ((CameraActivity)mActivity).getParameters() != null){
		   List<String> supportedSizes = SettingUtils.buildSupportedPictureSizeByRatio(mICameraDevice.getParameters(),previewRation);
		   int index = pictureSize.indexOf('x');
	       int width = Integer.parseInt(pictureSize.substring(0, index));
	       int height = Integer.parseInt(pictureSize.substring(index + 1));
	       Log.d(TAG, "[seDoubleCameraPictureSize] will set picture width = " + width + ",height = " +height);
	       mICameraDevice.setPictureSize(width, height);
	   }
   }
   
   private void seDoubleCameraPreviewSize() {
	   if(((CameraActivity)mActivity).getParameters() != null){
		   String previewRatio = SettingDataBase.PICTURE_RATIO_4_3;
	       Size optiomSize = SettingUtils.getOptimalPreviewSize((Context)mActivity,mICameraDevice.getParameters(),previewRatio,DOUBLE_CAMERA_MAX_WIDTH,DOUBLE_CAMERA_MAX_HEIGHT);
	       List<String> supportedSizes = SettingUtils.buildSupportedPictureSizeByRatio(mICameraDevice.getParameters(),previewRatio);
	       setPictureSize(previewRatio, supportedSizes.get(supportedSizes.size() -1));
	       Log.d(TAG, "[seDoubleCameraPreviewSize] will set preview width = " + optiomSize.width+ ",height = " + optiomSize.height);
	       mICameraDevice.setPreviewSize(optiomSize.width, optiomSize.height);
	   }
   }

   private void restoreCameraPreviewSize(){
	   String pictureRatio = mISettingCtrl.getSettingValue(SettingConstants.KEY_PICTURE_RATIO);
	   String pictureSize = mISettingCtrl.getSettingValue(SettingConstants.KEY_PICTURE_SIZE);
	   setPictureSize(pictureRatio, pictureSize);
       SettingUtils.setPreviewSize(mActivity, mICameraDevice.getParameters(), pictureRatio);
   }
   
}
