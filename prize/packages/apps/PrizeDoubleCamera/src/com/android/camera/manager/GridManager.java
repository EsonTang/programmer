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
import android.content.res.TypedArray;
import android.hardware.Camera.Parameters;
import android.view.View;

import com.android.camera.CameraActivity;
import com.android.camera.Log;
import com.android.camera.ParametersHelper;
import com.android.camera.R;
import com.android.camera.SettingUtils;
import com.android.camera.ui.RotateImageView;
import com.mediatek.camera.platform.ICameraAppUi.ViewState;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingDataBase;
import com.mediatek.camera.setting.preference.IconListPreference;
import com.mediatek.camera.setting.preference.ListPreference;
import com.prize.setting.GridLineView;

import java.util.List;
/**
 * 
 **
 * Grid line interface management
 * @author wanzhijuan
 * @version V1.0
 */
public class GridManager extends ViewManager implements
        CameraActivity.OnParametersReadyListener, CameraActivity.OnPreferenceReadyListener {
    private static final String TAG = "GridManager";
    
    private boolean mPreferenceReady;
    private GridLineView mView;
    private boolean mVisible;
    private boolean mIsFirst = true;
    private int mPreWidth;
    private int mPreHeight;
    private boolean mIsVideo;
    
    public GridManager(CameraActivity context) {
        super(context, ViewManager.VIEW_LAYER_GRID);
        context.addOnParametersReadyListener(this);
        context.addOnPreferenceReadyListener(this);
        // disable animation for cross with remaining.
        setAnimationEnabled(true, false);
    }
    
    @Override
    protected View getView() {
    	Log.i(TAG, "getView()");
    	View view = inflate(R.layout.grid_frame);
    	mView = (GridLineView) view.findViewById(R.id.view_grid);
    	/*prize-xuchunming-20171201-bugid:43220-start*/
    	if (mView != null) {
			mView.setPreviewSize(mPreWidth, mPreHeight);
		} 
    	/*prize-xuchunming-20171201-bugid:43220-end*/
        return view;
    }
    
    public void onPreferenceReady() {
    	Log.i(TAG, "onPreferenceReady()");
        mPreferenceReady = true;
    }
    
    public void onCameraParameterReady() {
    	Log.i(TAG, "onCameraParameterReady()");
        refreshView(true);
        refresh();
    }
    
    @Override
    public synchronized void onRefresh() { 
    	Log.i(TAG, "onRefresh() mPreferenceReady=" + mPreferenceReady);
        if (!mPreferenceReady) {
            return;
        }
        refreshView(false);
        //onSizeChanged(mPreWidth, mPreHeight);
        int visiable = mVisible ? View.VISIBLE : View.GONE;
        
        Log.i(TAG, "onRefresh() visiable=" + (  mVisible ? "View.VISIBLE" : "View.GONE"));
        

        if(getContext().isActivityOnpause()){
        	visiable = View.GONE;
        	Log.i(TAG, "onRefresh() context isOnPause  visiable= View.GONE" );
        }
        
        
		mView.setVisibility(visiable);
    }
    
    public synchronized void refreshView(boolean force) {
    	
    	if (force || mIsFirst) {
    		String value = getContext().getISettingCtrl().getSettingValue(SettingConstants.KEY_GRIDE);
        	boolean visible = "on".equals(value) ? true : false;
        	if (visible != mVisible) {
        		mVisible = visible;
        	}
        	mIsFirst = false;
    	}
    }
    
    @Override
	protected void onShow() {
		
	}

	private int getPictureSizeType() {
		String radio = getContext().getISettingCtrl().getSettingValue(SettingConstants.KEY_PICTURE_RATIO);
    	boolean is43 = radio.equals(SettingDataBase.PICTURE_RATIO_4_3);
    	Log.i(TAG, "getPictureSizeType() radio=" + radio + " is43=" + is43);
    	return is43 ? GridLineView.PICTURE43 : GridLineView.PICTURE169;
    }

	/*PRIZE-12383-wanzhijuan-2016-03-02-start*/
	public void onSizeChanged(int width, int height) {
		Log.i(TAG, "onSizeChanged() width=" + width + " height=" + height + " " + mView);
		if (mView != null) {
			mView.setPreviewSize(width, height);
		} 
		mPreWidth = width;
		mPreHeight = height;
	}
	/*PRIZE-12383-wanzhijuan-2016-03-02-end*/

	/*prize Video status does not show grid lines wanzhijuan 2016-5-31 start*/
	public void onViewStateChanged(boolean isVideo) {
		mIsVideo = isVideo;
		onRefresh();
	}
	/*prize Video status does not show grid lines wanzhijuan 2016-5-31 end*/
}
