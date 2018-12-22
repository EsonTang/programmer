package com.prize.prizethemecenter.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by Administrator on 2016/9/8.
 */
public class ImgForHorizontalScrollview extends HorizontalScrollView{

    private int currentX;

    public ImgForHorizontalScrollview(Context context) {
        super(context);
    }

    public ImgForHorizontalScrollview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImgForHorizontalScrollview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int scrollX = getScrollX();
        int x = (int) ev.getX();
        if(ev.getAction() == MotionEvent.ACTION_MOVE){
            if(currentX == 0)
                currentX = x;
        }
        if(currentX > x){
            if(getWidth() + scrollX >= computeHorizontalScrollRange()){
                currentX = 0;
                return false;
            }else if(currentX < x){
                currentX = x;
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
}
