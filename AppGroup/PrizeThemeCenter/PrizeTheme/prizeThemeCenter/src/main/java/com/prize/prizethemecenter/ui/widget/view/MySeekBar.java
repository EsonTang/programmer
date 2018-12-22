package com.prize.prizethemecenter.ui.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * Created by Administrator on 2017/3/9.
 */
public class MySeekBar extends SeekBar {
    public MySeekBar(Context context) {
        super(context);
    }

    public MySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
