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
package com.android.camera.manager;

import android.view.View;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.Log;
import com.android.camera.R;
import com.mediatek.camera.mode.gyfacebeauty.GyBeautyHelper;

public class GyBeautyManager extends ViewManager {
    private static final String TAG = "GyBeautyManager";
    
    private android.widget.SeekBar mSeekBar;
    private static int mSmoothValue = -1;
    private static int mWhiteningValue = 0;
    private int  mWhitening = 5;
    private boolean isInPopupSetting = false;
    private CameraActivity mContext;
    private GyBeautyHelper gyBeautyHelper;
    private com.android.camera.ui.RotateLayout mRotateLayout;
    public GyBeautyManager(CameraActivity context) {
        super(context);
	 mContext = context;
	  gyBeautyHelper = new GyBeautyHelper((CameraActivity)mContext);

    }
    
    @Override
    protected View getView() {
        View view = inflate(R.layout.gy_levelseekbar);
     //   mInfoView = (TextView) view.findViewById(R.id.info_view);

	  	 if (view.findViewById(R.id.gy_rl_leveltip) != null ){
		 ((com.android.camera.ui.Rotatable)view.findViewById(R.id.gy_rl_leveltip)).setOrientation(90, true);
 	 }
         mRotateLayout = (com.android.camera.ui.RotateLayout)view.findViewById(R.id.gy_rl_levelseek);
	 if (mRotateLayout != null){
	 	((com.android.camera.ui.Rotatable)mRotateLayout).setOrientation(180, true);
   	 }

        mSeekBar = (android.widget.SeekBar)view.findViewById(R.id.gy_beatifySeekBar);
        
        int seekbarmax = 150;
        if (mSmoothValue == -1){
       	   mSmoothValue = seekbarmax/2;
	          mWhiteningValue = mSmoothValue / mWhitening;
        }

        mSeekBar.setMax(seekbarmax);
	if (mSeekBar instanceof com.gangyun.camera.FilterSeekBar)
	{
         	mSeekBar.setProgress(mSmoothValue);
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
				 Log.i(TAG, "gangyun11 setOnSeekBarChangeListener  aa");
				 gyBeautyHelper.startGYBeauty(1,mSmoothValue,mWhiteningValue);

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

        return view;
    }
    
    
    @Override
    protected void onRefresh() {
        Log.d(TAG, "gangyun tech onRefresh" );
      	 if (mRotateLayout != null){
	 	((com.android.camera.ui.Rotatable)mRotateLayout).setOrientation(180, true);
   	 }

    }

@Override
public void show() {
    super.show();
	gyBeautyHelper.startGYBeauty(1,mSmoothValue,mWhiteningValue);
}


@Override
public void hide() {
    super.hide();
    gyBeautyHelper.startGYBeauty(0,0,0);

}

@Override
public void onOrientationChanged(int orientation) {
 Log.d(TAG, "gangyun onOrientationChanged orientation ="+orientation );

      	 if (mRotateLayout != null){
	 	((com.android.camera.ui.Rotatable)mRotateLayout).setOrientation(180, true);
   	 }

}

/*
    public void show() {
        Log.d(TAG, "show() " + this);
      if(mSeekBar !=null) {
           mSeekBar.setVisibility(android.view.View.VISIBLE);
	 }
    }
    
    public void hide() {
        Log.d(TAG, "hide() " + this);
      if(mSeekBar !=null) {
           mSeekBar.setVisibility(android.view.View.INVISIBLE);
	 }
    }
	*/
}
