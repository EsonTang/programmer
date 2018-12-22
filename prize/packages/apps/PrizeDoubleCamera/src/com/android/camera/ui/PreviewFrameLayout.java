/*
 * Copyright (C) 2009 The Android Open Source Project
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
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.camera.Log;
import com.android.camera.R;
import com.android.camera.Util;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingUtils;
import com.prize.setting.NavigationBarUtils;

/**
 * A layout which handles the preview aspect ratio.
 */
public class PreviewFrameLayout extends RelativeLayout {
    /** A callback to be invoked when the preview frame's size changes. */
    public interface OnSizeChangedListener {
        void onSizeChanged(int width, int height);
    }

    private static final String TAG = "PreviewFrameLayout";
    private double mAspectRatio;
    private View mBorder;
    private OnSizeChangedListener mListener;
    
    private int mScreenPixHeight;
	private int mScreenPixWidth;

    public PreviewFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        getScreenPix();
    }

    @Override
    protected void onFinishInflate() {
        mBorder = findViewById(R.id.preview_border);
    }

    public void setAspectRatio(double ratio) {
        Log.d(TAG, "setAspectRatio(" + ratio + ") mAspectRatio=" + mAspectRatio + ", " + this);
        if (ratio <= 0.0) {
            throw new IllegalArgumentException();
        }

        if (mAspectRatio != ratio) {
            mAspectRatio = ratio;
            requestLayout();
        }
    }

    public void showBorder(boolean enabled) {
        mBorder.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    }

    public void fadeOutBorder() {
        Util.fadeOut(mBorder);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int previewWidth = MeasureSpec.getSize(widthSpec);
        int previewHeight = MeasureSpec.getSize(heightSpec);

        // Get the padding of the border background.
        int hPadding = getPaddingLeft() + getPaddingRight();
        int vPadding = getPaddingTop() + getPaddingBottom();

        // Resize the preview frame with correct aspect ratio.
        previewWidth -= hPadding;
        previewHeight -= vPadding;

        boolean widthLonger = previewWidth > previewHeight;
        int longSide = (widthLonger ? previewWidth : previewHeight);
        int shortSide = (widthLonger ? previewHeight : previewWidth);
        // here aspect ratio is standard aspect ratio, no need to add navigation
        // bar's size
        if (longSide > shortSide * mAspectRatio) {
            longSide = Math.round((float) (shortSide * mAspectRatio) / 2) * 2;
        } else {
            shortSide = Math.round((float) (longSide / mAspectRatio) / 2) * 2;
        }
        Log.i(TAG, "mAspectRatio: "+mAspectRatio);
        /*prize-modify-bugid:57556 camera interface display abnormal on videomode-xiaoping-20180511-start*/
        if (Math.abs((mAspectRatio - SettingUtils.getFullScreenRatio())) < Util.ASPECT_TOLERANCE) {
        	adapter199display();
        }else if(Math.abs((mAspectRatio - SettingUtils.getFullScreenRatio())) < Util.ASPECT_TOLERANCE_16_9){
        	adapter169display();
        } else {
        	adapter43display(mScreenPixHeight, longSide);
		}
        /*prize-modify-bugid:57556 camera interface display abnormal on videomode-xiaoping-20180511-end*/
        if (widthLonger) {
            previewWidth = longSide;
            previewHeight = shortSide;
        } else {
            previewWidth = shortSide;
            previewHeight = longSide;
        }

        // Add the padding of the border.
        previewWidth += hPadding;
        previewHeight += vPadding;

        // Ask children to follow the new preview dimension.
        super.onMeasure(MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY));
        Log.d(TAG, "onMeasure() width = " + previewWidth + " height = " + previewHeight + ", "
                + this);
    }

    public void setOnSizeChangedListener(OnSizeChangedListener listener) {
        mListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged(" + w + ", " + h + ", " + oldw + ", " + oldh + ") " + this);
        if (mListener != null) {
            mListener.onSizeChanged(w, h);
        }
    }
    
    public void getScreenPix() {
		DisplayMetrics metric = new DisplayMetrics();
		WindowManager wm = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getRealMetrics(metric);

		mScreenPixWidth = metric.widthPixels;
		mScreenPixHeight = metric.heightPixels;
	}
    
    public void adapter43display(int ScreenHeight, int displayHeight){
    	Log.d("xucm", "ScreenHeight:"+ScreenHeight+",displayHeight:"+displayHeight);
    	FrameLayout.LayoutParams p = (FrameLayout.LayoutParams)getLayoutParams();
    	p.bottomMargin = (int)getContext().getResources().getDimension(R.dimen.shutter_group_height) - (ScreenHeight - displayHeight) / 2;
    	if(p.bottomMargin < 0){
    		p.bottomMargin = 0;
    	}
    	p.topMargin = 0;
    	setLayoutParams(p);
    }
    
    public void adapter169display(){
    	FrameLayout.LayoutParams p = (FrameLayout.LayoutParams)getLayoutParams();
    	p.bottomMargin = 0;
    	p.topMargin = (int)getResources().getDimension(R.dimen.preview_surfaceview_margin);
    	setLayoutParams(p);
    }
    
    /*prize-modify-bugid:57556 camera interface display abnormal on videomode-xiaoping-20180511-start*/
    public void adapter199display(){
    	FrameLayout.LayoutParams p = (FrameLayout.LayoutParams)getLayoutParams();
    	p.bottomMargin = 0;
    	p.topMargin = 0;
    	setLayoutParams(p);
    }
    /*prize-modify-bugid:57556 camera interface display abnormal on videomode-xiaoping-20180511-end*/
}
