package com.example.longshotscreen.ui;

import com.example.longshotscreen.R;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import com.example.longshotscreen.utils.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class LongShotView extends SurfaceView implements SurfaceHolder.Callback
{
	private float RADIUS;
	private float TOUCH_DISTANCE;
	private int mBgColor;
	private Paint mBgPaint = new Paint();
	private Rect mClearStatusBarDrawRect;
	private Point mDownPoint = new Point();
	private int mDownRectBottom;
	private int mDownRectTop;
	private Handler mHandler;
	private Point mMovePoint = new Point();
	private Paint mPointPaint = new Paint();
	private Point[] mPoints = new Point[2];
	private int mPressPoint = -1;
	private int mRectMinHeight = 200;
	private Paint mRectPaint = new Paint();
	private int mScreenHeight;
	private ScrollShotView mScrollShotView;
	private int mStatusBarHeight;
	private float mStrokeWidth = 0;
	private SurfaceHolder mSurfaceHolder;

	public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1, int paramInt2, int paramInt3)
	{
	}

	public void surfaceCreated(SurfaceHolder paramSurfaceHolder)
	{
		drawRect();
	}

	public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder)
	{
	}

	public LongShotView(Context context, Handler handler, ScrollShotView ScrollShotView, int statusbarHeight, int width, int height)
	{
		super(context);
		Resources localResources = context.getResources();
		mScrollShotView = ScrollShotView;
		mStatusBarHeight = statusbarHeight;
		mScreenHeight = height;
		mClearStatusBarDrawRect = new Rect(0, 0, width, mStatusBarHeight);
		RADIUS = 0;
		TOUCH_DISTANCE = 0;
		mHandler = handler;
		mBgColor = localResources.getColor(R.color.translucent);
		mBgPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		mRectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		mRectPaint.setColor(localResources.getColor(R.color.rect_line_color));
		mRectPaint.setStyle(Paint.Style.STROKE);
		mRectPaint.setStrokeWidth(5);
		mPointPaint.setColor(localResources.getColor(R.color.rect_line_color));
		mPointPaint.setStyle(Paint.Style.FILL);
		mPointPaint.setAntiAlias(true);
		mSurfaceHolder = getHolder();
		setZOrderOnTop(true);
		mSurfaceHolder.setFormat(-3);
		mSurfaceHolder.addCallback(this);
	}

	private void drawRect()
	{
		Canvas mCanvas = mSurfaceHolder.lockCanvas();
		mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
		mCanvas.drawPaint(mBgPaint);
		mCanvas.drawColor(mBgColor);
		mCanvas.drawRect(mScrollShotView.mRect, mBgPaint);
		mCanvas.drawRect(mScrollShotView.mRect, mRectPaint);
		if (mScrollShotView.getIsFirstPressNextPageButton()){
			mCanvas.drawCircle((float)((mScrollShotView.mRect.left + mScrollShotView.mRect.right) / 2), (float)mScrollShotView.mRect.top, RADIUS, mPointPaint);
		}else
		{
			mCanvas.drawCircle((float)((mScrollShotView.mRect.left + mScrollShotView.mRect.right) / 2), (float)mScrollShotView.mRect.bottom, RADIUS, mPointPaint);

		}
		mCanvas.drawRect(mClearStatusBarDrawRect, mBgPaint);
		mSurfaceHolder.unlockCanvasAndPost(mCanvas);
	}

	private static int getDistence(Point start, Point end)
	{
		return (int)Math.sqrt(Math.pow(start.x - end.x, 2.0D) + Math.pow(start.y - end.y, 2.0D));
	}

	private int getPosition(Point point)
	{
		int pos = -1;
		if ((mPressPoint == 1) || (mPressPoint == 2))
		{
			pos = mPressPoint;
			
		}
		setPointsValue();
		for (int i = 1; i < mPoints.length; i = i + 1)
		{
			 if((float)getDistence(mPoints[i], point) < TOUCH_DISTANCE) {
	                pos = i + 1;
	            }
		}
		return pos;
	}

	private void setPointsValue()
	{
		mPoints[0] = new Point((mScrollShotView.mRect.left + mScrollShotView.mRect.right) / 2, mScrollShotView.mRect.top);
		mPoints[1] = new Point((mScrollShotView.mRect.left + mScrollShotView.mRect.right) / 2, mScrollShotView.mRect.bottom);
	}

	protected void onConfigurationChanged(Configuration configuration)
	{
		mHandler.sendEmptyMessage(2);
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		mMovePoint.x = (int)event.getX();
		mMovePoint.y = (int)event.getY();
		switch (event.getAction())
		{
		
		case MotionEvent.ACTION_DOWN:
			 mDownPoint.x = (int)event.getX();
             mDownPoint.y = (int)event.getY();
             mDownRectTop = mScrollShotView.mRect.top;
             mDownRectBottom = mScrollShotView.mRect.bottom;
             Log.d("LongShotView", "enter mDownPoint = " + mDownPoint);
             break;
		case MotionEvent.ACTION_MOVE:
			 Log.d("LongShotView", "enter event.ACTION_MOVE...");
             switch(getPosition(mMovePoint)) {
                 case 1:
                 {
                     if(mScrollShotView.getIsFirstPressNextPageButton()) {
                         mScrollShotView.mRect.top = mMovePoint.y;
                         if((mDownRectBottom - mMovePoint.y) < mRectMinHeight) {
                             mScrollShotView.mRect.top = (mDownRectBottom - mRectMinHeight);
                         }
                         if(mScrollShotView.mRect.top < mStatusBarHeight) {
                             mScrollShotView.mRect.top = mStatusBarHeight;
                         }
                         if(mScrollShotView.mRect.top > ((mScreenHeight / 2) - mStatusBarHeight)) {
                             mScrollShotView.mRect.top = ((mScreenHeight / 2) - mStatusBarHeight);
                         }
                         drawRect();
                         mPressPoint = 1;
                         return true;
                     }
                 }
                     case 2:
                     {
                         if(!mScrollShotView.getIsFirstPressNextPageButton()) {
                             mScrollShotView.mRect.bottom = mMovePoint.y;
                             if((mMovePoint.y - mDownRectTop) < mRectMinHeight) {
                                 mScrollShotView.mRect.bottom = (mDownRectTop + mRectMinHeight);
                             }
                             if(mScrollShotView.mRect.bottom > (mScreenHeight - 1)) {
                                 mScrollShotView.mRect.bottom = (mScreenHeight - 1);
                             }
                             if(mScrollShotView.mRect.bottom < (mStatusBarHeight + (mScreenHeight / 2))) {
                                 mScrollShotView.mRect.bottom = (mStatusBarHeight + (mScreenHeight / 2));
                             }
                             mHandler.sendEmptyMessage(1);
                             drawRect();
                             mPressPoint = 2;
                             return true;
                         }
                         {
                             mPressPoint = -1;
                             if(!mScrollShotView.getIsFirstPressNextPageButton()) {
                                 mScrollShotView.setNextPageTextViewEnabled(false);
                                 break;
                             }
                         }
                     }
                 }
		case MotionEvent.ACTION_UP:
		}
		return true;
		
	}



}
