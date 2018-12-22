/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.android.camera.CameraActivity;
import com.android.camera.Log;
import com.android.camera.Util;
import com.mediatek.camera.platform.ICameraAppUi.ViewState;
import com.mediatek.camera.setting.preference.IconListPreference;
import com.mediatek.camera.setting.preference.ListPreference;
import com.android.camera.R;

/**
 * A view for switching the front/back camera.
 */
public class PickerButton extends RotateImageView implements View.OnClickListener {
    private static final String TAG = "PickerButton";
    private boolean mForceEnable = false;
    private boolean mForceDisableEnable = false;
    private Animation mFadeIn;
    private Animation mFadeOut;
    private boolean mShowAnimationEnabled = true;
    private boolean mHideAnimationEnabled = true;
    public interface Listener {
        boolean onPicked(PickerButton button, ListPreference preference, String newValue);
    }

    protected Listener mListener;
    protected IconListPreference mPreference;

    public PickerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void initialize(IconListPreference pref) {
        Log.d(TAG, "initialize(" + pref + ")");
        mPreference = pref;
    }

    /**
     * Force to show this button on UI.
     */
    public void forceEnable() {
        mForceEnable = true;
    }

    /**
     * Do not force to show this button on UI.
     */
    public void cancelForcedEnable() {
        mForceEnable = false;
    }

    /**
     * Force to show this button on UI.
     */
    public void forceDisableEnable() {
    	mForceDisableEnable = true;
    }

    /**
     * Do not force to show this button on UI.
     */
    public void cancelforceDisableEnable() {
    	mForceDisableEnable = false;
    }
    /**
     * Refresh the buttons status.
     */
    public void refresh() {
        Log.d(TAG, "refresh() " + mPreference);
        if (!isVisible()) {
        	/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
            setVisibility(View.GONE);
        	//hide();
            /*prize-xuchunming-20161104-add bgfuzzy switch-end*/
        } else if(mPreference != null){
            setOnClickListener(null);
            String value = mPreference.getOverrideValue();
            if (value == null) {
                value = mPreference.getValue();
            }
            int index = mPreference.findIndexOfValue(value);
            int[] icons = mPreference.getIconIds();
            if (icons != null) {
                if (index >= 0 && index < icons.length) {
                    setImageResource(icons[index]);
                } else {
                    index = getValidIndexIfNotFind(value);
                    setImageResource(icons[index]);
                }
            }
            setOnClickListener(this);
            //setVisibility(View.VISIBLE);
            show();
        }
    }

    @Override
    public void onClick(View v) {
    	Log.i(TAG, "onClick,start,mPreference: "+mPreference);
        if (mPreference == null) {
            Log.w(TAG, "onClick() why mPreference is null?", new Throwable());
            return;
        } else {
            if (!mPreference.isEnabled() && !mForceEnable) {
                Log.i(TAG, "onClick() mPreference's enable = false ,return this click event");
                return;
            }
            /*prize-modify-bugid:54944 invalid click on camera interface-xiaoping-20180412-start*/
            if (((CameraActivity)getContext()).getCameraAppUI().getViewState() == ViewState.VIEW_STATE_FOCUSING
            		|| ((CameraActivity)getContext()).getCameraAppUI().getViewState() == ViewState.VIEW_STATE_MVFOCUSING) {
            	 Log.i(TAG, "onClick() current status is focusing  ,return this click event");
                 return;
			}
            /*prize-modify-bugid:54944 invalid click on camera interface-xiaoping-20180412-end*/
            
        }
        // Find the index of next camera.
        String value = mPreference.getOverrideValue();
        if (value == null) {
            value = mPreference.getValue();
        }
        int index = mPreference.findIndexOfValue(value);
        if (index < 0) { // make 0 as default
            index = getValidIndexIfNotFind(value);
        }
        CharSequence[] values = mPreference.getEntryValues();
        // do {
        index = (index + 1) % values.length;
        // } while (!mPreference.isEnabled(index));
        String next = values[index].toString();
        mPreference.setOverrideValue(null, false);
        if (mListener != null && mListener.onPicked(this, mPreference, next)) {
            // clear override value after user changed it
            //mPreference.setOverrideValue(null, false);
            mPreference.setValueIndex(index); // should be checked
            refresh();
        }
    }

    public void setValue(String value) {
        Log.v(TAG, "setValue(" + value + ") mPreference=" + mPreference);
        if (mPreference != null && value != null && !value.endsWith(mPreference.getValue())) {
            mPreference.setValue(value);
            refresh();
        }
    }

    protected int getValidIndexIfNotFind(String value) {
        return 0;
    }

    private boolean isVisible() {
        if (mForceEnable) {
            Log.i(TAG, "[isVisible], force enable is true, return true");
            return true;
        }
        if (mForceDisableEnable) {
            Log.i(TAG, "[isVisible], force enable is false, return false");
            return false;
        }
        if (mPreference == null) {
            Log.i(TAG, "[isVisible], mPreference is null, return false");
            return false;
        }
        if (mPreference.getEntries() == null || mPreference.getEntries().length <= 1) {
            Log.i(TAG, "[isVisible], entries is null or entries size less than one, return false");
            return false;
        }
        if (!mPreference.isEnabled()) {
            Log.i(TAG, "[isVisible], preference enable is false, return false");
            return false;
        }
        if (mPreference.isShowInSetting()) {
            Log.i(TAG, "[isVisible], this icon should be show in setting, return false");
            return false;
        }
        return true;
    }
    
    public void show(){
    	if(getVisibility() != View.VISIBLE){
    		fadeIn();
        	setVisibility(View.VISIBLE);
        	setEnabled(true);
    	}
    }
    
    public void hide(){
    	if(getVisibility() == View.VISIBLE){
    		setEnabled(false);
        	if(getVisibility() == View.VISIBLE){
        		fadeOn();
            	setVisibility(View.INVISIBLE);
        	}
    	}
    }
    
    protected void fadeIn() {
        if (mHideAnimationEnabled) {
        	 if (mFadeIn == null) {
                 mFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.setting_popup_grow_fade_in);
             }
             if (mFadeIn != null) {
                 startAnimation(mFadeIn);
             } else {
                 Util.fadeIn(this);
             }
        }
        
    }
    
    protected void fadeOn() {
        if (mShowAnimationEnabled) {
        	 if (mFadeOut == null) {
                 mFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.setting_popup_shrink_fade_out);
             }
             if (mFadeOut != null) {
                 startAnimation(mFadeOut);
             } else {
                 Util.fadeOut(this);
             }
        }
    }
    
    public IconListPreference getIconListPreference(){
    	return mPreference;
    }
    public void setAnimationEnabled(boolean showAnimationEnabled, boolean hideAnimationEnabled) {
        this.mShowAnimationEnabled = showAnimationEnabled;
        this.mHideAnimationEnabled = hideAnimationEnabled;
    }

}
