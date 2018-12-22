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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.android.gallery3d.R;
import com.android.gallery3d.util.LogUtil;
import com.prize.util.GloblePrizeUtil;

import java.util.HashMap;
import java.util.Map;

public class PhotoPageBottomControls implements OnClickListener {
    public interface Delegate {
        public boolean canDisplayBottomControls();
        public boolean canDisplayBottomControl(int control);
        public void onBottomControlClicked(int control);
        public void refreshBottomControlsWhenReady();
    }

    protected static final String TAG = "PhotoPageBottomControls";
    
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
    
    private boolean mHasNavigationBar;
    private int mNavigationBarHeight;
    
    private static Animation getControlAnimForVisibility(boolean visible) {
        Animation anim = visible ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);
        anim.setDuration(CONTROL_ANIM_DURATION_MS);
        return anim;
    }

    /// M: [BUG.MODIFY] @{
    /*public PhotoPageBottomControls(Delegate delegate, Context context, RelativeLayout layout) {*/
    public PhotoPageBottomControls(Delegate delegate, Context context, ViewGroup layout, boolean hasNavigationBar,
                                   int navigationBarHeight) {
        /// @}
        mDelegate = delegate;
        mParentLayout = layout;
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        /*PRIZE-gallery modify PhotoPage layout UI-wanzhijuan-2015-6-4-start*/
 
        mContainer = (ViewGroup) inflater
                .inflate(R.layout.prize_photopage_bottom_controls, mParentLayout, false);
        
        mParentLayout.addView(mContainer);
       
        ViewGroup subContainer = (ViewGroup) mContainer.findViewById(R.id.photopage_buttomd);
        mNavigationBarHeight = navigationBarHeight;
        /*PRIZE-gallery modify PhotoPage layout UI-wanzhijuan-2015-6-4-end*/
        for (int i = subContainer.getChildCount() - 1; i >= 0; i--) {
            View child = subContainer.getChildAt(i);
            child.setOnClickListener(this);
            mControlsEnable.put(child, true);
        }
       
        mContainerAnimIn.setDuration(CONTAINER_ANIM_DURATION_MS);
        mContainerAnimOut.setDuration(CONTAINER_ANIM_DURATION_MS);

        mDelegate.refreshBottomControlsWhenReady();
        mHasNavigationBar = hasNavigationBar;
    }
    
    public void updateNavigationBar(){
    	
    	if(mHasNavigationBar && GloblePrizeUtil.isShowNavigationBar(mContext) 
    			&& mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mContainer.setPadding(0, 0, 0, mNavigationBarHeight);
    	}else {
            mContainer.setPadding(0, 0, 0, 0);
    	}
    }

    private void hide(boolean isForceUnVisibility) {
        mContainer.clearAnimation();
        if (!isForceUnVisibility) {
            mContainerAnimOut.reset();
            mContainer.startAnimation(mContainerAnimOut);
        }
        mContainer.setVisibility(View.INVISIBLE);
    }

    private void show() {
    	updateNavigationBar();
        mContainer.clearAnimation();
        mContainerAnimIn.reset();
        mContainer.startAnimation(mContainerAnimIn);
        mContainer.setVisibility(View.VISIBLE);
    }
    
    public boolean getBottomVisible(){
    	return this.mContainerVisible;
    }
    
    public void refresh(boolean isForceUnVisibility) {
        boolean visible = mDelegate.canDisplayBottomControls() && !isForceUnVisibility;
        boolean containerVisibilityChanged = (visible != mContainerVisible);
        if (containerVisibilityChanged) {
            if (visible) {
                show();
            } else {
                hide(isForceUnVisibility);
            }
            mContainerVisible = visible;
        }
        if (!mContainerVisible) {
            return;
        }
        for (View control : mControlsEnable.keySet()) {
            Boolean prevEnable = mControlsEnable.get(control);
            boolean curEnable = mDelegate.canDisplayBottomControl(control.getId());
            if (prevEnable.booleanValue() != curEnable) {
                if (!containerVisibilityChanged) {
                    control.clearAnimation();
                    control.startAnimation(getControlAnimForVisibility(curEnable));
                }
                //control.setVisibility(curVisibility ? View.VISIBLE : View.INVISIBLE);

                control.setEnabled(curEnable);
                if (control instanceof ViewGroup) {
                	ViewGroup parent = (ViewGroup) control;
                	for (int i = parent.getChildCount() - 1; i >= 0; i--) {
                        View child = parent.getChildAt(i);
                        child.setEnabled(curEnable);
                    }
                }
                mControlsEnable.put(control, curEnable);
            }
        }
        // Force a layout change
        mContainer.requestLayout(); // Kick framework to draw the control.
    }

    public void cleanup() {
        mParentLayout.removeView(mContainer);
        mControlsEnable.clear();
    }

    @Override
    public void onClick(View view) {
        Boolean controlVisible = mControlsEnable.get(view);
        if (mContainerVisible && controlVisible != null && controlVisible.booleanValue()) {
            mDelegate.onBottomControlClicked(view.getId());
        }
    }

    /// M: [FEATURE.ADD] get photopage_bottom_control_edit visibility @{
    /**
     * Get view visibility, which controller other layer view visibility.
     * @return true if this view is visible.
     */
    public boolean getContainerVisibility() {
        boolean visiable = mContainerVisible;
        for (View control : mControlsEnable.keySet()) {
            if (control.getId() == R.id.photopage_bottom_control_edit) {
                Boolean editViewVisibility = mControlsEnable.get(control);
                visiable &= editViewVisibility;
            }
        }
        return visiable;
    }
    // In case of hide container without animation.
    public void hideContainer() {
        mContainer.clearAnimation();
        mContainer.setVisibility(View.INVISIBLE);
        mContainerVisible = false;
    }
    /// @}
}
