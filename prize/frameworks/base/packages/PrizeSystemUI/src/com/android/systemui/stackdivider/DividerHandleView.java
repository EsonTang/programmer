/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.systemui.stackdivider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.Nullable;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;

import com.android.systemui.Interpolators;
import com.android.systemui.R;

import com.mediatek.common.prizeoption.PrizeOption;
import android.util.Log;
import android.content.res.Configuration;
import static android.app.ActivityManager.StackId.DOCKED_STACK_ID;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * View for the handle in the docked stack divider.
 */
public class DividerHandleView extends View {

    private final static Property<DividerHandleView, Integer> WIDTH_PROPERTY
            = new Property<DividerHandleView, Integer>(Integer.class, "width") {

        @Override
        public Integer get(DividerHandleView object) {
            return object.mCurrentWidth;
        }

        @Override
        public void set(DividerHandleView object, Integer value) {
            object.mCurrentWidth = value;
            object.invalidate();
        }
    };

    private final static Property<DividerHandleView, Integer> HEIGHT_PROPERTY
            = new Property<DividerHandleView, Integer>(Integer.class, "height") {

        @Override
        public Integer get(DividerHandleView object) {
            return object.mCurrentHeight;
        }

        @Override
        public void set(DividerHandleView object, Integer value) {
            object.mCurrentHeight = value;
            object.invalidate();
        }
    };

    private final Paint mPaint = new Paint();
    private final int mWidth;
    private final int mHeight;
    private final int mCircleDiameter;
    private int mCurrentWidth;
    private int mCurrentHeight;
    private AnimatorSet mAnimator;
    private boolean mTouching;
    
    private int mhandleColor; // prize-add-split screen-liyongli-20170708
    private boolean mLandscape;  // prize-add-split screen-liyongli-20170714
    private float prizeDpi; // prize-add-split screen-liyongli-20180207

    public DividerHandleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);        
        mPaint.setColor(getResources().getColor(R.color.docked_divider_handle, null));
        mPaint.setAntiAlias(true);
        mWidth = getResources().getDimensionPixelSize(R.dimen.docked_divider_handle_width);
        mHeight = getResources().getDimensionPixelSize(R.dimen.docked_divider_handle_height);
        mCurrentWidth = mWidth;
        mCurrentHeight = mHeight;
        mCircleDiameter = (mWidth + mHeight) / 3;
        
        mLandscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
                
        /* prize-modify-split screen-liyongli-20180207-start */
        if( PrizeOption.PRIZE_SPLIT_SCREEN_DRAGVIEW ){
            mhandleColor = getResources().getColor(R.color.docked_divider_handle, null); // prize-add-split screen-liyongli-20170708
            prizeDpi =  getResources().getDisplayMetrics().density;
        }/* prize-modify-split screen-liyongli-20180207-end */
    }

    public void setTouching(boolean touching, boolean animate) {
        if (touching == mTouching) {
            return;
        }
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        if (!animate) {
            if (touching) {
                mCurrentWidth = mCircleDiameter;
                mCurrentHeight = mCircleDiameter;
            } else {
                mCurrentWidth = mWidth;
                mCurrentHeight = mHeight;
            }
            invalidate();
        } else {
            animateToTarget(touching ? mCircleDiameter : mWidth,
                    touching ? mCircleDiameter : mHeight, touching);
        }
        mTouching = touching;
    }

    private void animateToTarget(int targetWidth, int targetHeight, boolean touching) {
        ObjectAnimator widthAnimator = ObjectAnimator.ofInt(this, WIDTH_PROPERTY,
                mCurrentWidth, targetWidth);
        ObjectAnimator heightAnimator = ObjectAnimator.ofInt(this, HEIGHT_PROPERTY,
                mCurrentHeight, targetHeight);
        mAnimator = new AnimatorSet();
        mAnimator.playTogether(widthAnimator, heightAnimator);
        mAnimator.setDuration(touching
                ? DividerView.TOUCH_ANIMATION_DURATION
                : DividerView.TOUCH_RELEASE_ANIMATION_DURATION);
        mAnimator.setInterpolator(touching
                ? Interpolators.TOUCH_RESPONSE
                : Interpolators.FAST_OUT_SLOW_IN);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator = null;
            }
        });
        mAnimator.start();
    }
    
    /* prize-add-split screen-liyongli-20170708-start */
    private boolean prizeDockedStackFocus = false;
    public void prizeSetDockedStackFocus(boolean dockedStackFocus) {
        if(!PrizeOption.PRIZE_SPLIT_SCREEN_FOCUS) {
    	        return;
        }
        if (prizeDockedStackFocus == dockedStackFocus) {
            return;
        }
        prizeDockedStackFocus = dockedStackFocus;
        invalidate();
    }
    public void prizeExchangePositionChangeFocus() {
        if(!PrizeOption.PRIZE_SPLIT_SCREEN_FOCUS) {
    	        return;
        }
        prizeDockedStackFocus = !prizeDockedStackFocus;
        invalidate();
    }
    public void prizeChangeDockedStackFocus(boolean dockedFocus) {
        if(!PrizeOption.PRIZE_SPLIT_SCREEN_FOCUS) {
    	        return;
        }
        prizeDockedStackFocus = false;
    }
    public int prizeGetFocusStack() {
    	if(prizeDockedStackFocus){
    		return DOCKED_STACK_ID;
    	}
    	return 1;
    }
    
    public boolean prizeDockedStackIsFocus() {
    	return prizeDockedStackFocus;
    }
    /* prize-add-split screen-liyongli-20170708-end */
    
    

    @Override
    protected void onDraw(Canvas canvas) {
        /* prize-modify-split screen-liyongli-20170608-start */
        if( PrizeOption.PRIZE_SPLIT_SCREEN_DRAGVIEW ){
            
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        float radius = 11*prizeDpi;//22;//30;//mCircleDiameter;
       
        if( mCurrentWidth==mCurrentHeight){//touch down
            mPaint.setColor(0xFF33CBCC);
            radius = 13*prizeDpi;
        }else{
        mPaint.setColor(0xFFE1E1E1);
        canvas.drawCircle(centerX, centerY, radius+2, mPaint);
        mPaint.setColor(mhandleColor);
        }
        //mPaint.setAlpha(128);
        canvas.drawCircle(centerX, centerY, radius, mPaint);
        //mPaint.setAlpha(255);
        if(false && PrizeOption.PRIZE_SPLIT_SCREEN_FOCUS) {
            radius = 10;
            mPaint.setColor(0xFFFF0000);
            if( mLandscape ){
            	if( prizeDockedStackFocus ){
            		centerX -= 15;
            	}else{
            		centerX+=15;
            	}
            }else{
            	if( prizeDockedStackFocus ){
            		centerY -= 15;
            	}else{
            		centerY+=15;
            	}
            }
            canvas.drawCircle(centerX, centerY, radius, mPaint);
        }
        return;
        }//end PrizeOption.PRIZE_SPLIT_SCREEN_DRAGVIEW
        /* prize-modify-split screen-liyongli-20170608-end */

        int left = getWidth() / 2 - mCurrentWidth / 2;
        int top = getHeight() / 2 - mCurrentHeight / 2;
        int radius = Math.min(mCurrentWidth, mCurrentHeight) / 2;
        canvas.drawRoundRect(left, top, left + mCurrentWidth, top + mCurrentHeight,
                radius, radius, mPaint);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}
