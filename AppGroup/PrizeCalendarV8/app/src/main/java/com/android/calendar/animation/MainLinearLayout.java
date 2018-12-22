package com.android.calendar.animation;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.R;

/**
 * Created by hky on 2017/8/8.
 */

public class MainLinearLayout extends LinearLayout {
    public MainLinearLayout(Context context) {
        super(context);
    }

    public MainLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    private int animationTypeFlag;

    public int getAnimationTypeFlag() {
        return animationTypeFlag;
    }

    public void setAnimationTypeFlag(int animationTypeFlag) {
        this.animationTypeFlag = animationTypeFlag;
    }

    private OnMainChangeListener mMainChangeListener;

    public interface OnMainChangeListener {
        void onChange();
    }

    public void setOnMainChangeListener(OnMainChangeListener onOnMainChangeListener) {
        this.mMainChangeListener = onOnMainChangeListener;
    }


    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
//        Log.d("hekeyi","monthToDayAnimation-addView  =  "+System.currentTimeMillis());
        if(mMainChangeListener == null) return;
        if(child.findViewById(R.id.viewpager)!=null){
            if(getAnimationTypeFlag()== CalendarController.ViewType.WEEK){
                mMainChangeListener.onChange();
            }
        }
    }



}
