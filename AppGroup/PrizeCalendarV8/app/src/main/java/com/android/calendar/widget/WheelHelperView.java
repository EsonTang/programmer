/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *文件名称：WheelHelperView.java
 *内容摘要：
 *当前版本：v 1.0
 *作	者：刘栋
 *完成日期：

 *修改记录：
 *修改日期：2015-5-27 下午4:09:23
 *版 本 号：v  1.0
 *修 改 人：刘栋
 *修改内容：
 ********************************************/
package com.android.calendar.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class WheelHelperView extends View {

	private final int COUNT_SIZE = 5;

	private Paint mPaint;

	public WheelHelperView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		  // 消除锯齿
		mPaint.setAntiAlias(true);
		mPaint.setColor(0xce000000);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		mPaint.setColor(0xFF146dc9);
		RectF rect = new RectF(0, 0, getWidth(), getHeight());
		
		canvas.drawOval(rect, mPaint);
	
		mPaint.setColor(0xce000000);
		RectF rects = new RectF(0, getHeight() / 2 - getHeight() / 5,
				getWidth(), getHeight() / 2 + getHeight() / 5);
		canvas.drawOval(rect, mPaint);
		super.onDraw(canvas);
	}

}
