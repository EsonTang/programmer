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

package com.prize.lockscreen.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.prizelockscreen.R;

public class NumPadKey extends ViewGroup {
    // list of "ABC", etc per digit, starting with '0'
    static String sKlondike[];

    private int mDigit = -1;
    private int mTextViewResId;
    private NewPasswordTextView mTextView;
    private TextView mDigitText;
    private TextView mKlondikeText;
    private boolean mEnableHaptics;
    
    private ImageView mBgImg;

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View thisView) {
            if (mTextView == null && mTextViewResId > 0) {
                final View v = NumPadKey.this.getRootView().findViewById(mTextViewResId);
                if (v != null && v instanceof NewPasswordTextView) {
                    mTextView = (NewPasswordTextView) v;
                }
            }
            if (mTextView != null && mTextView.isEnabled()) {
                mTextView.append(Character.forDigit(mDigit, 10));
            }
            doHapticKeyClick();
        }
    };

    public NumPadKey(Context context) {
        this(context, null);
    }

    public NumPadKey(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumPadKey(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumPadKey);

        try {
            mDigit = a.getInt(R.styleable.NumPadKey_digit, mDigit);
            mTextViewResId = a.getResourceId(R.styleable.NumPadKey_textView, 0);
        } finally {
            a.recycle();
        }

        setOnClickListener(mListener);
        setOnHoverListener(new LiftToActivateListener(context));
        // setAccessibilityDelegate(new ObscureSpeechDelegate(context));

        mEnableHaptics = false;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.keyguard_num_pad_key, this, true);

        mDigitText = (TextView) findViewById(R.id.digit_text);
        mDigitText.setText(Integer.toString(mDigit));
        mKlondikeText = (TextView) findViewById(R.id.klondike_text);
        mBgImg = (ImageView) findViewById(R.id.img_bg);

        if (mDigit >= 0) {
            if (sKlondike == null) {
                sKlondike = getResources().getStringArray(R.array.lockscreen_num_pad_klondike);
            }
            if (sKlondike != null && sKlondike.length > mDigit) {
                String klondike = sKlondike[mDigit];
                final int len = klondike.length();
                if (len > 0) {
                    mKlondikeText.setText(klondike);
                } else {
                    mKlondikeText.setVisibility(View.INVISIBLE);
                }
            }
        }
        
        //mRipple = (RippleDrawable)context.getDrawable(R.drawable.ripple_drawable);
        //setBackground(mRipple);
        mBgImg.setBackground(context.getDrawable(R.drawable.num_key_selector));
        setContentDescription(mDigitText.getText().toString());
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Reset the "announced headset" flag when detached.
        //ObscureSpeechDelegate.sAnnouncedHeadset = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int digitHeight = mDigitText.getMeasuredHeight();
        int klondikeHeight = mKlondikeText.getMeasuredHeight();
        int totalHeight = digitHeight + klondikeHeight;
        
        int h = getHeight();
        int w = getWidth();
        
        layoutBgImg(w, h);
        
        int top = h / 2 - totalHeight / 2;
        int centerX = w / 2;
        int left = centerX - mDigitText.getMeasuredWidth() / 2;
        int bottom = top + digitHeight;
        mDigitText.layout(left, top, left + mDigitText.getMeasuredWidth(), bottom);
        top = (int) (bottom - klondikeHeight * 0.35f);
        //top = bottom - 40;
        bottom = top + klondikeHeight;

        left = centerX - mKlondikeText.getMeasuredWidth() / 2;
        mKlondikeText.layout(left, top, left + mKlondikeText.getMeasuredWidth(), bottom);
        
        //setRippleBounds(l, t, r, b);
    }
    /***
     * 设置圆圈的范围
     */
    private void layoutBgImg(int w, int h) {
    	if (w > h) {
    		int left = (w-h)/2;
    		mBgImg.layout(left, 0, left + h, h);
    	}
    	else {
    		int left = (h-w)/2;
    		mBgImg.layout(left, 0, left + w, w);
    	}
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    // Cause a VIRTUAL_KEY vibration
    public void doHapticKeyClick() {
        if (mEnableHaptics) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                    | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }
}
