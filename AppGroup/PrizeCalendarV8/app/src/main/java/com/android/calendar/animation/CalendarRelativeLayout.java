package com.android.calendar.animation;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.R;
import com.android.calendar.agenda.AgendaListView;
import com.android.calendar.horday.DayHeaderView;
import com.android.calendar.hormonth.CalendarViewPagerLisenter;

/**
 * Created by hky on 2017/8/17.
 */

public class CalendarRelativeLayout extends LinearLayout {
    private boolean scrollable;

    public CalendarRelativeLayout(Context context) {
        super(context);
    }

    public CalendarRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CalendarRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    float fromY,fromX;
    float secondaryPaneY;
    float titlePaneY;
    long startTime111;
    AgendaListView mAgendaListView;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
//        super.dispatchTouchEvent(event);
        if (!isScrollable()) return false;
//        secondaryPaneY = this.getChildAt(2).getHeight()+this.getChildAt(3).findViewById(R.id.secondary_layout).getY();

//        secondaryPaneY = this.getChildAt(0).getHeight() + this.getChildAt(1).findViewById(R.id.secondary_layout).getY();
        titlePaneY = this.getChildAt(0).getHeight();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                fromY = event.getY();
                fromX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float fromY2 = getY();
                float dy = fromY2 - fromY;
                float dx = event.getX() - fromX;
                if (Math.abs(dy) > 10 && (Math.abs(dx)<10)) {
                    startTime111 = System.currentTimeMillis();
//                    Log.d("hekeyi", "[CalendarRelativeLayout]-dispatchTouchEvent ACTION_MOVE  startTime111 = " + startTime111);
                    mAgendaListView = (AgendaListView) getRootView().findViewById(R.id.agenda_events_list);
                    boolean isFirstItem = (mAgendaListView.getFirstVisiblePosition() == 0 && mAgendaListView.getChildAt(0).getTop() >= 0);
                    float disY = event.getY() - fromY;
//                if(fromY<secondaryPaneY){
                    if ((disY > 5) && (mOnLayoutMoveListener != null) && !CalendarViewPagerLisenter.isScrolling /*&& !DayHeaderView.isScrolling */&& isFirstItem && AllInOneActivity.viewTypeFlag== CalendarController.ViewType.WEEK) {
                        requestDisallowInterceptTouchEvent(true);
                        mOnLayoutMoveListener.onMoveDown();
                    } else if ((disY < -5) && (mOnLayoutMoveListener != null) && !CalendarViewPagerLisenter.isScrolling && AllInOneActivity.viewTypeFlag== CalendarController.ViewType.MONTH) {
                        requestDisallowInterceptTouchEvent(true);
                        mOnLayoutMoveListener.onMoveUp();
                    }
//                }

                }
                break;
            case MotionEvent.ACTION_UP:
                requestDisallowInterceptTouchEvent(false);
                break;
           /* case MotionEvent.ACTION_UP:
                startTime111 = System.currentTimeMillis();
                Log.d("hekeyi","[CalendarRelativeLayout]-dispatchTouchEvent ACTION_UP  startTime111 = "+startTime111);
                mAgendaListView = (AgendaListView)getRootView().findViewById(R.id.agenda_events_list);
                boolean isFirstItem = (mAgendaListView.getFirstVisiblePosition()==0 && mAgendaListView.getChildAt(0).getTop()>=0);
//                Log.d("hekeyi","[CalendarRelativeLayout]  isFirstItem  = "+isFirstItem );
//                Log.d("hekeyi","[CalendarRelativeLayout]  !CalendarViewPagerLisenter.isScrolling  = "+!CalendarViewPagerLisenter.isScrolling );
//                Log.d("hekeyi","[CalendarRelativeLayout]  !DayHeaderView.isScrolling  = "+!DayHeaderView.isScrolling );
                float disY = event.getY() - fromY;
//                if(fromY<secondaryPaneY){
                    if ((disY > 5) && (mOnLayoutMoveListener != null) && !CalendarViewPagerLisenter.isScrolling && !DayHeaderView.isScrolling && isFirstItem) {
                        mOnLayoutMoveListener.onMoveDown();
                    } else if ((disY < -5) && (mOnLayoutMoveListener != null) && !CalendarViewPagerLisenter.isScrolling) {
                        mOnLayoutMoveListener.onMoveUp();
                    }
//                }
                *//*if(Math.abs(fromX)<titlePaneY){
                    return true;
                }else {
                    return super.dispatchTouchEvent(event);
                }*//*
                break;*/
        }
        return super.dispatchTouchEvent(event);
//        return true;
    }

    public OnLayoutMoveListener mOnLayoutMoveListener;

    public interface OnLayoutMoveListener {
        void onMoveDown();

        void onMoveUp();
    }

    public void setOnLayoutMoveListener(OnLayoutMoveListener onLayoutMoveListener) {
        this.mOnLayoutMoveListener = onLayoutMoveListener;
    }

    private OnNavigationBarChangeListener mOnNavigationBarChangeListener;

    public void setOnNavigationBarChangeListener(OnNavigationBarChangeListener onNavigationBarChangeListener) {
        this.mOnNavigationBarChangeListener = onNavigationBarChangeListener;
    }

    public interface OnNavigationBarChangeListener {
        void onChange();
    }
}
