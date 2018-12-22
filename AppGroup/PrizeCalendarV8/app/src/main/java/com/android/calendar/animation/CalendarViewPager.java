package com.android.calendar.animation;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.android.calendar.R;


/**
 * Created by hky on 2017/8/22.
 */

public class CalendarViewPager extends ViewPager {

    private boolean noScroll = false;
    private Time selectedDay;

    public CalendarViewPager(Context context) {
        super(context);
    }

    public CalendarViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public boolean isNoScroll() {
        return noScroll;
    }

    public void setNoScroll(boolean noScroll) {
        this.noScroll = noScroll;
    }

    public Time getSelectedDay() {
        return selectedDay;
    }

    public void setSelectedDay(Time selectedDay) {
        this.selectedDay = selectedDay;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (isNoScroll()){
            return false;
        }else{
            return super.onInterceptTouchEvent(arg0);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
