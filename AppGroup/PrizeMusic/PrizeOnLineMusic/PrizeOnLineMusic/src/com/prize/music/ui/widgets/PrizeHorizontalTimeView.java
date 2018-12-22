package com.prize.music.ui.widgets;


import com.prize.music.R;

import android.R.interpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PrizeHorizontalTimeView extends View {
	private Context mContext;
	private Paint mPaint;
	
	private float mCircleRadiu = 10;
	private float mLineWidth = 5 ;
	private int mBottomColor = 0xffe4e4e4;
	private int mActiveColor = 0xff33ccff; 
	private int mTextColor = Color.BLACK;
	private float mTextSize = 10;
	
	private String[] mTexts = {"10min","20min","30min","45min","60min","90min"};
	private int[] mTimes = {10,20,30,45,60,90};
	private int mTime = 10;
	private ViewChangeListener mListener;
	
	int mWidth , mHeight;

	public PrizeHorizontalTimeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		
		TypedArray typedarray = context.obtainStyledAttributes(attrs, R.styleable.PrizeHorizontalTimeView);
		mCircleRadiu = typedarray.getDimension(R.styleable.PrizeHorizontalTimeView_circle_radius, 8);        
        mLineWidth = typedarray.getDimension(R.styleable.PrizeHorizontalTimeView_line_width, 3);
        mTextSize = typedarray.getDimension(R.styleable.PrizeHorizontalTimeView_text_size, 18);
        mBottomColor = typedarray.getColor(R.styleable.PrizeHorizontalTimeView_bottom_color, 0xffe4e4e4);
        mActiveColor = typedarray.getColor(R.styleable.PrizeHorizontalTimeView_active_color, 0xff33ccff);
        mTextColor = typedarray.getColor(R.styleable.PrizeHorizontalTimeView_text_color, R.color.text_color_969696);
		
		mWidth = getWidth();
		mHeight = getHeight();
		
		mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        
        typedarray.recycle();
		
	}
	
	

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		float startX ;
		float startY;
		switch(arg0.getAction()){
			case MotionEvent.ACTION_DOWN:
				startX = arg0.getX();
				startY = arg0.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				float moveX = arg0.getX();
				float moveY = arg0.getY();
				snapUI(moveX,moveY);			
				break;
			case MotionEvent.ACTION_UP:
				float upX = arg0.getX();
				float upY = arg0.getY();
				snapUI(upX,upY);
				mListener.viewChanged(mTime);
				break;
			case MotionEvent.ACTION_CANCEL:
				break;
		}
		
		
		return /*super.onTouchEvent(arg0)*/true;
	}



	private void snapUI(float moveX, float moveY) {
		// TODO Auto-generated method stub
		float f =  moveX / (getWidth() / 7);
		int i = Math.round(f);
		switch(i){
		case 0:
			mTime = 0;
			break;
		case 1:
			mTime = 10;
			break;
		case 2:
			mTime = 20;
			break;
		case 3:
			mTime = 30;
			break;
		case 4:
			mTime = 45;
			break;
		case 5:
			mTime = 60;
			break;
		case 6:
			if(f <= 6)
				mTime = 60;
			else if(f > 6)
				mTime = 90;
			break;
		case 7:
			mTime = 90;
			break;
		}
		invalidate();
		
		
	}



	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		drawBottomLine(canvas);
		drawBottomCircle(canvas);
		drawText(canvas);
		
		drawActiveLineAndCircle(canvas);
	}



	
	
	private void drawActiveLineAndCircle(Canvas canvas) {
		// TODO Auto-generated method stub
		int i = 0;
		switch(mTime){
		case 0:
			i = 0;
			break;
		case 10:
			i = 1;
			break;
		case 20:
			i = 2;
			break;
		case 30:
			i = 3;
			break;
		case 45:
			i = 4;
			break;
		case 60:
			i = 5;
			break;
		case 90:
			i = 7;
			break;
			
		}
		drawActiveLine(canvas, i);
		drawActiveCircle(canvas,i);
	}

	/**
	 * @see 滑动上层已滑动的圆点
	 * @param canvas
	 * @param length
	 */
	private void drawActiveCircle(Canvas canvas, int length) {
		// TODO Auto-generated method stub
		if(length == 0)
			return;
		mPaint.setColor(mActiveColor);
    	mPaint.setStyle(Paint.Style.FILL);
    	
    	float xCircleCenter = getWidth() / 7;
    	float yCircleCenter = getHeight() / 4;
    	
    	for(int i=0; i<length; i++){
    		if(i == 5 ){
    			continue;
    		}
    		xCircleCenter =  getWidth() / 7 * (i + 1);    		
    		if(i == 6){
    			xCircleCenter = xCircleCenter - mCircleRadiu / 2;
    		}    		
    		canvas.drawCircle(xCircleCenter, yCircleCenter, mCircleRadiu, mPaint);
    	}
	}

	/**
	 * @see 画上层表示已滑动的线
	 * @param canvas
	 * @param length
	 */
	private void drawActiveLine(Canvas canvas, int length){
		if(length == 0)
			return;
		mPaint.setColor(mActiveColor);
    	mPaint.setStyle(Paint.Style.STROKE);
    	mPaint.setStrokeWidth(mLineWidth);
    	
    	float xStart = 0 ;
    	float yStart = getHeight() / 4;
    	
    	float xEnd = getWidth() / 7 * length;
    	float yEnd = yStart;
    	
    	canvas.drawLine(xStart, yStart, xEnd, yEnd, mPaint);
	} 
	
	
	
	
	
	/**
	 * @see 画底部的字体
	 * @param canvas
	 */
	private void drawText(Canvas canvas) {
		// TODO Auto-generated method stub		
		mPaint.setColor(mTextColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(0);
		mPaint.setTextSize(mTextSize);
		
		float xStart = getWidth() / 7; 
		
		for(int i = 0 ;i<7;i++){
			xStart = getWidth() / 7 * (i + 1) - mPaint.measureText(mTexts[0])/2; 
			if( i == 5){
				continue;
			}
			
    		String text = "";
    		if(i == 6){
    			text = mTexts[5];
    			xStart = getWidth() / 7 * (i + 1) - mPaint.measureText(mTexts[0]); 
    		}else{   			
    			text = mTexts[i];
    		}
    		canvas.drawText(text, xStart, getHeight() * 5 / 7, mPaint);
 
		}
	}

	/**
	 * @see 画底层的圆点
	 * @param canvas
	 */
	private void drawBottomCircle(Canvas canvas) {
		// TODO Auto-generated method stub
		mPaint.setColor(mBottomColor);
    	mPaint.setStyle(Paint.Style.FILL);
    	
    	float xCircleCenter = getWidth() / 7;
    	float yCircleCenter = getHeight() / 4;
    	
    	for(int i=0; i<7; i++){
    		if(i == 5 ){
    			continue;
    		}
    		xCircleCenter =  getWidth() / 7 * (i + 1);    		
    		if(i == 6){
    			xCircleCenter = xCircleCenter - mCircleRadiu / 2;
    		}    		
    		canvas.drawCircle(xCircleCenter, yCircleCenter, mCircleRadiu, mPaint);
    		
    	}
    	
	}

	/**
	 * @see 画底层的线
	 * @param canvas
	 */
	private void drawBottomLine(Canvas canvas) {
		// TODO Auto-generated method stub
		mPaint.setColor(mBottomColor);
    	mPaint.setStyle(Paint.Style.STROKE);
    	mPaint.setStrokeWidth(mLineWidth);
    	
    	float xStart = 0 ;
    	float yStart = getHeight() / 4;
    	
    	float xEnd = getWidth();
    	float yEnd = yStart;
    	
    	canvas.drawLine(xStart, yStart, xEnd, yEnd, mPaint);
	}

	public void setMTime(int time){
		this.mTime = time;
	}
	
	public int getMTime(){
		return mTime;
	}
	
	/**
	 * @see 外部调用时间，根据时间更新UI，传入的time为分钟数如:10,30,90.传入的reSetAlarm表示是否重新调用监听事件
	 * 		在setViewChangeListener()后调用
	 * @author lixing
	 * @param time reSetAlarm
	 */
	public void setTime(int time,Boolean reSetAlarm){
		mTime = time;
		invalidate();
		if(mListener != null && reSetAlarm ){   
			mListener.viewChanged(mTime);
		}
	}
	
	/**
	 * @see 外部调用设置回调监听实例,在setTime之前调用
	 * @author lixing
	 * @param listener
	 */
	public void setViewChangeListener(ViewChangeListener listener){
		mListener = listener;
	}
	
	/**
	 * @see 回调监听接口
	 * @author lixing
	 *
	 */
	public interface ViewChangeListener{
		public void onViewChange(int time); //正在滑动过程中
		public void viewChanged(int time);  //已经滑动完成
	}

}
