package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * ScrollView中使用GridView,使用此widget
 * 
 * @author prize
 * 
 */
public class ScrollGridView extends GridView {
	public ScrollGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollGridView(Context context) {
		super(context);
	}

	public ScrollGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
	
	@Override  
	public boolean dispatchTouchEvent(MotionEvent ev) {  
	    // TODO Auto-generated method stub  
	    if (ev.getAction() == MotionEvent.ACTION_MOVE) {  
	        return true;  
	    }  
	    return super.dispatchTouchEvent(ev);  
	}  

}