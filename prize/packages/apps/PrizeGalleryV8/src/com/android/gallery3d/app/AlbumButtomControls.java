/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.app;

import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.ui.Log;
import com.prize.util.GloblePrizeUtil;

import java.util.HashMap;
import java.util.Map;

public class AlbumButtomControls implements OnClickListener {
    public interface Delegate {
        public boolean canDisplayBottomControls();
        public boolean canDisplayBottomControl(int control);
        public void onBottomControlClicked(int control);
        public void refreshBottomControlsWhenReady();
        public boolean getBottomIsFile();
    }

    protected static final String TAG = "AlbumButtomControls";
    
    private Context mContext;
    private Delegate mDelegate;
    private ViewGroup mParentLayout;
    private ViewGroup mContainer;

    private boolean mContainerVisible = false;
    private Map<View, Boolean> mControlsEnable = new HashMap<View, Boolean>();

    private Animation mContainerAnimIn = new AlphaAnimation(0f, 1f);
    private Animation mContainerAnimOut = new AlphaAnimation(1f, 0f);
    private static final int CONTAINER_ANIM_DURATION_MS = 200;

    private static final int CONTROL_ANIM_DURATION_MS = 150;
    
    private boolean isContainer =  false;

    private boolean mHasNavigationBar;
    
    private static Animation getControlAnimForVisibility(boolean visible) {
        Animation anim = visible ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);
        anim.setDuration(CONTROL_ANIM_DURATION_MS);
        return anim;
    }
    
    /// M: [BUG.MODIFY] @{
    /*public PhotoPageBottomControls(Delegate delegate, Context context, RelativeLayout layout) {*/
    public AlbumButtomControls(Delegate delegate, Context context, ViewGroup layout) {
        /// @}
        mDelegate = delegate;
        mParentLayout = layout;
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        /*PRIZE-相册修改PhotoPage界面UI-wanzhijuan-2015-6-4-start*/
        // 布局修改，功能View相对于父布局也不一样
        mContainer = (ViewGroup) inflater
                .inflate(R.layout.prize_albumall_bottom_controls, mParentLayout, false);
        mParentLayout.addView(mContainer);
        ViewGroup subContainer = (ViewGroup) mContainer.findViewById(R.id.linear_buttom_view);
        /*PRIZE-相册修改PhotoPage界面UI-wanzhijuan-2015-6-4-end*/
        for (int i = subContainer.getChildCount() - 1; i >= 0; i--) {
            View child = subContainer.getChildAt(i);
            child.setOnClickListener(this);
            mControlsEnable.put(child, true);
        }

        mContainerAnimIn.setDuration(CONTAINER_ANIM_DURATION_MS);
        mContainerAnimOut.setDuration(CONTAINER_ANIM_DURATION_MS);

        mDelegate.refreshBottomControlsWhenReady();
        this.isContainer = false;
 	    mHasNavigationBar = GloblePrizeUtil.checkContianNavigationBar(mContext);
    }
    
    public AlbumButtomControls(Delegate delegate, Context context, ViewGroup layout,boolean isContainer) {
        /// @}
        mDelegate = delegate;
        mParentLayout = layout;
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        /*PRIZE-相册修改PhotoPage界面UI-wanzhijuan-2015-6-4-start*/
        // 布局修改，功能View相对于父布局也不一样
        mContainer = (ViewGroup) inflater
                .inflate(R.layout.prize_albumall_bottom_controls, mParentLayout, false);
        mParentLayout.addView(mContainer);
        ViewGroup subContainer = (ViewGroup) mContainer.findViewById(R.id.linear_buttom_view);
        /*PRIZE-相册修改PhotoPage界面UI-wanzhijuan-2015-6-4-end*/
        for (int i = subContainer.getChildCount() - 1; i >= 0; i--) {
            View child = subContainer.getChildAt(i);
            child.setOnClickListener(this);
            mControlsEnable.put(child, true);
        }

        mContainerAnimIn.setDuration(CONTAINER_ANIM_DURATION_MS);
        mContainerAnimOut.setDuration(CONTAINER_ANIM_DURATION_MS);

        mDelegate.refreshBottomControlsWhenReady();
        this.isContainer = isContainer;
    }
    
    public void OnResume() {
 	}
    
    public void onPause(){
    }
    

    private void hide() {
        mContainer.clearAnimation();
        mContainerAnimOut.reset();
        mContainer.startAnimation(mContainerAnimOut);
        mContainer.setVisibility(View.INVISIBLE);
    }

    private void show() {
        mContainer.clearAnimation();
        mContainerAnimIn.reset();
        mContainer.startAnimation(mContainerAnimIn);
        mContainer.setVisibility(View.VISIBLE);
    }

    public void refresh() {
    	
        boolean visible = mDelegate.canDisplayBottomControls();
        boolean containerVisibilityChanged = (visible != mContainerVisible);
        if (containerVisibilityChanged) {
            if (visible) {
                show();
            } else {
                hide();
            }
            mContainerVisible = visible;
        }
        if (!mContainerVisible) {
            return;
        }
        for (View control : mControlsEnable.keySet()) {
        	if(control.getVisibility() == View.GONE){
        		continue;
        	}
        	
            Boolean prevEnable = mControlsEnable.get(control);
            boolean curEnable = mDelegate.canDisplayBottomControl(control.getId());
            if (prevEnable.booleanValue() != curEnable) {
                if (!containerVisibilityChanged) {
                    //control.clearAnimation();
                    //control.startAnimation(getControlAnimForVisibility(curEnable));
                }
                /*PRIZE-相册修改PhotoPage界面UI-wanzhijuan-2015-6-4-start*/
                // 编辑按钮只有是不是可用
                control.setEnabled(curEnable);
                if (control instanceof ViewGroup) {
                	ViewGroup parent = (ViewGroup) control;
                	for (int i = parent.getChildCount() - 1; i >= 0; i--) {
                        View child = parent.getChildAt(i);
                        child.setEnabled(curEnable);
                    }
                }
                /*PRIZE-相册修改PhotoPage界面UI-wanzhijuan-2015-6-4-end*/
                mControlsEnable.put(control, curEnable);
            }
        }
        // Force a layout change
        mContainer.requestLayout(); // Kick framework to draw the control.
    }
    
    public void updateSupportedOperation(boolean isSetFile){
    	
    	if(isSetFile){
    		mContainer.findViewById(R.id.view_delete_left).setVisibility(View.GONE);
            mContainer.findViewById(R.id.view_share).setEnabled(false);
    		mContainer.findViewById(R.id.view_share).setVisibility(View.GONE);
    		mContainer.findViewById(R.id.view_delete).setVisibility(View.VISIBLE);
    		mContainer.findViewById(R.id.view_set_to).setEnabled(false);
    		mContainer.findViewById(R.id.view_set_to).setVisibility(View.GONE);
            mContainer.findViewById(R.id.view_details).setEnabled(false);
    		mContainer.findViewById(R.id.view_details).setVisibility(View.GONE);
    		mContainer.findViewById(R.id.view_delete_right).setVisibility(View.GONE);
    	}else {
    		updateSupportedOperation();
    	}
    }
    
    
    public void updateSupportedOperation(){
    	mContainer.findViewById(R.id.view_delete_left).setVisibility(View.GONE);
    	mContainer.findViewById(R.id.view_share).setVisibility(View.VISIBLE);
        mContainer.findViewById(R.id.view_share).setEnabled(true);
		mContainer.findViewById(R.id.view_delete).setVisibility(View.VISIBLE);
		mContainer.findViewById(R.id.view_set_to).setVisibility(View.VISIBLE);
		mContainer.findViewById(R.id.view_set_to).setEnabled(true); //prize-public-bug:15974 set to doesn't work-pengcancan-20160511
		mContainer.findViewById(R.id.view_details).setVisibility(View.VISIBLE);
        mContainer.findViewById(R.id.view_details).setEnabled(true);
		mContainer.findViewById(R.id.view_delete_right).setVisibility(View.GONE);
    }
    
    public void updateEnableOperation(boolean isDouble){
    	mContainer.findViewById(R.id.view_share).setEnabled(true);
    	mContainer.findViewById(R.id.view_delete).setEnabled(true);
    	mContainer.findViewById(R.id.view_set_to).setEnabled(isDouble);
    	mContainer.findViewById(R.id.view_details).setEnabled(isDouble);
    }
    
    public void updateEnableOperation(int operation){
    	if(!((operation & MediaObject.SUPPORT_SHARE)!= 0)){
			mContainer.findViewById(R.id.view_share).setEnabled(false);
		}else {
			mContainer.findViewById(R.id.view_share).setEnabled(true);
		}
		
		if(!((operation & MediaObject.SUPPORT_DELETE)!= 0)){
			mContainer.findViewById(R.id.view_delete).setEnabled(false);
		}else {
			mContainer.findViewById(R.id.view_delete).setEnabled(true);
		}
		
		if(!((operation & MediaObject.SUPPORT_SETAS)!= 0)){
			mContainer.findViewById(R.id.view_set_to).setEnabled(false);
		}else {
			mContainer.findViewById(R.id.view_set_to).setEnabled(true);
		}
		
		if(!((operation & MediaObject.SUPPORT_INFO)!= 0)){
			mContainer.findViewById(R.id.view_details).setEnabled(false);
		}else {
			mContainer.findViewById(R.id.view_details).setEnabled(true);
		}
    }

    public void cleanup() {
        mParentLayout.removeView(mContainer);
        mControlsEnable.clear();
    }
    
    public boolean getIsVisable(){
    	return mContainerVisible;
    }

    @Override
    public void onClick(View view) {
        Boolean controlVisible = mControlsEnable.get(view);
        if (mContainerVisible && controlVisible != null && controlVisible.booleanValue()) {
            mDelegate.onBottomControlClicked(view.getId());
        }
    }
}
