package com.prize.music.ui.widgets;

import com.prize.music.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class TransitionColorView extends View{
    private int mColor;
    int last24bit;
    int front8bit;
    private Paint mPaint;
	public TransitionColorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		TypedArray typedarray = context.obtainStyledAttributes(attrs, R.styleable.TransitionColorView);
		mColor = typedarray.getColor(R.styleable.TransitionColorView_max_color, 0x00ffffff);
		
		mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(2);
        
        front8bit = mColor >> 24;
        last24bit = mColor & 0x00ffffff;
        typedarray.recycle();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		if(front8bit == 0){
			return;
		}
		
		for(int i = 0; i< getHeight(); i++){
			int startX = 0;
			int startY = i;
			int endX = getWidth();
			int endY = i;
//			Log.d("LIXING","last24bit = " + Integer.toHexString(last24bit));
//			Log.d("LIXING","i = " + i + " ,getHeight() = " + getHeight());
			int trans = (int)(i * 1.0f /getHeight() * 255) << 24;
//			Log.d("LIXING","trans = " + Integer.toHexString(trans));
			mPaint.setColor(last24bit + trans);
//			Log.d("LIXING","color = " + Integer.toHexString(last24bit + trans));
			canvas.drawLine(startX, startY, endX, endY, mPaint);
		}
		
	}

	public void setColor(int color){
		mColor = color;
		front8bit = mColor >> 24;
		last24bit = mColor & 0x00ffffff;
		
		invalidate();
	}
	

}
