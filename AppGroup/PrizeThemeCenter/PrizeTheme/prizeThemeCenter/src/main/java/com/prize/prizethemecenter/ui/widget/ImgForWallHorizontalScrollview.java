package com.prize.prizethemecenter.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by Administrator on 2016/9/8.
 */
public class ImgForWallHorizontalScrollview extends HorizontalScrollView{

    private static final String TAG ="hu" ;
    private int currentX;

    public ImgForWallHorizontalScrollview(Context context) {
        super(context);
        init();
    }

    public ImgForWallHorizontalScrollview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImgForWallHorizontalScrollview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        int to = (computeHorizontalScrollRange()-getWidth())/2;
        scrollTo(to,0);
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

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }
}
