/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：默认页面指示图标
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-9-2
 *********************************************/
package com.android.launcher3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class HomeIndicatorView extends ImageView {

	private Drawable mNormalDrawable;
	private Drawable mPressDrawable;
	boolean isHomeScreen = false;
	private long workspaceWaitTime;

	public boolean isHomeScreen() {
		return isHomeScreen;
	}

	public void setHomeScreen(boolean isHomeScreen) {

          workspaceWaitTime =  SystemClock.uptimeMillis() ;
		this.isHomeScreen = isHomeScreen;
		requestLayout();
	}

	public HomeIndicatorView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public HomeIndicatorView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public HomeIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public HomeIndicatorView(Context context) {
		super(context);
		mNormalDrawable = context.getDrawable(R.drawable.edit_home_press);
		mPressDrawable = context.getDrawable(R.drawable.edit_home_normal);
		int left = mPressDrawable.getIntrinsicWidth()/2;
		int top = mPressDrawable.getIntrinsicHeight()/2;
		int right = mPressDrawable.getIntrinsicWidth();
		int bottom=mPressDrawable.getIntrinsicHeight();
		Rect r = new Rect(left, top, right,
				bottom);
		mPressDrawable.setBounds(r);
		mNormalDrawable.setBounds(r);
	}

	/*@Override
	protected void onDraw(Canvas canvas) {
		
		super.onDraw(canvas);
		if (this.isHomeScreen) {
			long time = workspaceWaitTime - SystemClock.uptimeMillis();
			mPressDrawable.draw(canvas);
			Log.i("zhouerlong", "HomeIndicatorView:::::ondraw Time:"+time);
		} else {
			mNormalDrawable.draw(canvas);
		}
	}*/

}
