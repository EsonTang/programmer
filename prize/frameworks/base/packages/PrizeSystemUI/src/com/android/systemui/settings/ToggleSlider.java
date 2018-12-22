/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.systemui.settings;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBarPrize;
import android.widget.SeekBarPrize.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;

/*PRIZE-import package- liufan-2015-05-14-start*/
import com.android.systemui.statusbar.phone.FeatureOption;
import android.graphics.drawable.Drawable;
/*PRIZE-import package- liufan-2015-05-14-end*/
public class ToggleSlider extends RelativeLayout {
    public interface Listener {
        public void onInit(ToggleSlider v);
        public void onChanged(ToggleSlider v, boolean tracking, boolean checked, int value,
                boolean stopTracking, boolean fromUser);
    }

    private Listener mListener;
    private boolean mTracking;

    private CompoundButton mToggle;
    private SeekBarPrize mSlider;
    private TextView mLabel;

    private ToggleSlider mMirror;
    private BrightnessMirrorController mMirrorController;

    public ToggleSlider(Context context) {
        this(context, null);
    }

    public ToggleSlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToggleSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        View.inflate(context, R.layout.status_bar_toggle_slider, this);

        final Resources res = context.getResources();
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ToggleSlider, defStyle, 0);

        mToggle = (CompoundButton) findViewById(R.id.toggle);
        mToggle.setOnCheckedChangeListener(mCheckListener);

        mSlider = (SeekBarPrize) findViewById(R.id.slider);
        mSlider.setOnSeekBarChangeListener(mSeekListener);
        /*PRIZE-update the UI of mSlider- liufan-2015-05-14-start*/
        /*if(FeatureOption.PRIZE_QS_SORT){
            Drawable thumb = res.getDrawable(R.drawable.ic_brightness_thumb_prize);
            mSlider.setThumb(thumb);
            Drawable d = res.getDrawable(R.drawable.qs_brightness_seekbar_bg_define);
            mSlider.setProgressDrawable(d);
        }*/
        /*PRIZE-update the UI of mSlider- liufan-2015-05-14-end*/

        mLabel = (TextView) findViewById(R.id.label);
        mLabel.setText(a.getString(R.styleable.ToggleSlider_text));

        //mSlider.setAccessibilityLabel(getContentDescription().toString());

        a.recycle();
    }

    public void setMirror(ToggleSlider toggleSlider) {
        mMirror = toggleSlider;
        if (mMirror != null) {
            mMirror.setChecked(mToggle.isChecked());
            mMirror.setMax(mSlider.getMax());
            mMirror.setValue(mSlider.getProgress());
        }
    }

    public void setMirrorController(BrightnessMirrorController c) {
        mMirrorController = c;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mListener != null) {
            mListener.onInit(this);
        }
    }

    public void setOnChangedListener(Listener l) {
        mListener = l;
    }

    public void setChecked(boolean checked) {
        mToggle.setChecked(checked);
    }

    public boolean isChecked() {
        return mToggle.isChecked();
    }

    public void setMax(int max) {
        mSlider.setMax(max);
        if (mMirror != null) {
            mMirror.setMax(max);
        }
    }

    public void setValue(int value) {
        mSlider.setProgress(value);
        if (mMirror != null) {
            mMirror.setValue(value);
        }
    }

    /*-add for bugid:51807-liufan-2018-03-08-start-*/
    public int getProgress(){
        return mSlider.getProgress();
    }

    public int getMax(){
        return mSlider.getMax();
    }
    /*-add for bugid:51807-liufan-2018-03-08-end-*/

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mMirror != null) {
            MotionEvent copy = ev.copy();
            mMirror.dispatchTouchEvent(copy);
            copy.recycle();
        }
        return super.dispatchTouchEvent(ev);
    }

    private final OnCheckedChangeListener mCheckListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton toggle, boolean checked) {
            mSlider.setEnabled(!checked);

            if (mListener != null) {
                mListener.onChanged(
                        ToggleSlider.this, mTracking, checked, mSlider.getProgress(), false, false);
            }

            if (mMirror != null) {
                mMirror.mToggle.setChecked(checked);
            }
        }
    };

    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBarPrize seekBar, int progress, boolean fromUser) {
            if (mListener != null) {
                mListener.onChanged(
                        ToggleSlider.this, mTracking, mToggle.isChecked(), progress, false, fromUser);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBarPrize seekBar) {
            mTracking = true;

            if (mListener != null) {
                mListener.onChanged(ToggleSlider.this, mTracking, mToggle.isChecked(),
                        mSlider.getProgress(), false, false);
            }

            mToggle.setChecked(false);

            if (mMirrorController != null) {
                /*PRIZE-update for brightness controller- liufan-2016-06-29-start*/
                mMirrorController.setLocation((View) getParent());
                mMirrorController.showMirror();
                /*PRIZE-update for brightness controller- liufan-2016-06-29-end*/
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBarPrize seekBar) {
            mTracking = false;

            if (mListener != null) {
                mListener.onChanged(ToggleSlider.this, mTracking, mToggle.isChecked(),
                        mSlider.getProgress(), true, false);
            }

            if (mMirrorController != null) {
                mMirrorController.hideMirror();
            }
        }
    };
}

