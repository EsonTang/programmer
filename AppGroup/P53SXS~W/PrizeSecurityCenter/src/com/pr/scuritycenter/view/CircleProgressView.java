package com.pr.scuritycenter.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.pr.scuritycenter.R;

public class CircleProgressView extends View {

	private Paint mPaintBackground; 
	private Paint mPaintProgress; 
	private Paint mPaintText; 
	private int bgColor = Color.WHITE; 
	private int textColor = Color.WHITE; 
	private int progressColor = Color.CYAN; 
	private float mStrokeWidth = 10;
	private float mRadius = 60; 
	private RectF rectPro;
	private int mProgress = 0; 
	private int mMaxProgress = 100;
	private int mWidth, mHeight;
	private onProgressListener mOnProgressListener;
	
	


	public void setOnProgressListener(onProgressListener mOnProgressListener) {
		this.mOnProgressListener = mOnProgressListener;
	}

	
	public  interface onProgressListener{
	
		public void onEnd(); 
	
	}
	
	public CircleProgressView(Context context) {
		this(context, null);
	}

	public CircleProgressView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleProgressView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		if(attrs!=null){
			TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.CircleProgress);
			int count = ta.getIndexCount();
			for (int i = 0; i < count; i++) {
				int attr = ta.getIndex(i);
				switch (attr) {
				case R.styleable.CircleProgress_radius:
					mRadius = ta.getDimension(R.styleable.CircleProgress_radius, mRadius);
					break;
				case R.styleable.CircleProgress_strokeWidth:
					mStrokeWidth = ta.getDimension(R.styleable.CircleProgress_strokeWidth, mStrokeWidth);
					break;
				case R.styleable.CircleProgress_bgColor:
					bgColor = ta.getColor(R.styleable.CircleProgress_bgColor, bgColor);
					break;
				case R.styleable.CircleProgress_progressColor:
					progressColor = ta.getColor(R.styleable.CircleProgress_progressColor, progressColor);
					break;
				case R.styleable.CircleProgress_android_textColor:
					textColor = ta.getColor(R.styleable.CircleProgress_android_textColor, textColor);
					break;
				}
			}
			ta.recycle();
		}
		
		initPaint();
	}

	private void initPaint() {
		mPaintBackground = new Paint();
		mPaintBackground.setColor(bgColor);
		
		mPaintBackground.setAntiAlias(true);
		
		mPaintBackground.setDither(true);
		
		mPaintBackground.setStyle(Style.STROKE);
		
		mPaintBackground.setStrokeWidth(mStrokeWidth);

		mPaintProgress = new Paint();
		mPaintProgress.setColor(progressColor);
		
		mPaintProgress.setAntiAlias(true);
		
		mPaintProgress.setDither(true);

		mPaintProgress.setStyle(Style.STROKE);
		
		mPaintProgress.setStrokeWidth(mStrokeWidth);

		mPaintText = new Paint();
		mPaintText.setColor(textColor);
		
		mPaintText.setAntiAlias(true);
		mPaintText.setTextAlign(Align.CENTER);
		mPaintText.setTextSize(40);
		

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		mWidth = getRealSize(widthMeasureSpec);
		mHeight = getRealSize(heightMeasureSpec);
		setMeasuredDimension(mWidth, mHeight);
	}

	private void initRect() {
		if (rectPro == null) {
			rectPro = new RectF();
			int viewSize = (int) (mRadius * 2);
			int left = (mWidth - viewSize) / 2;
			int top = (mHeight - viewSize) / 2;
			int right = left + viewSize;
			int bottom = top + viewSize;
			rectPro.set(left, top, right, bottom);
		}
	}

	private int getRealSize(int measureSpec) {
		int result = -1;
		int mode = MeasureSpec.getMode(measureSpec);
		int size = MeasureSpec.getSize(measureSpec);
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED) { // ������ģʽ��Ҫ�Լ�����
			result = (int) (mRadius * 2 + mStrokeWidth*2);
		} else {
			result = size;
		}
		return result;
	}

	public void setProgress(int progress){
		this.mProgress =progress;
		invalidate();
	}
	
	public int getProgress(){
		return mProgress;
	}
	@Override
	protected void onDraw(Canvas canvas) {
		float angle = mProgress / (mMaxProgress * 1.0f) * 360; // Բ���Ƕ�
		Log.d("circle","mProgress = "+mProgress);
		initRect();
	
		canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius,
				mPaintBackground);
		
		canvas.drawArc(rectPro, 90, angle, false, mPaintProgress);

		if(mOnProgressListener != null){
		if(mProgress == mMaxProgress){
				mOnProgressListener.onEnd();
			}
		}
		
	}

}
