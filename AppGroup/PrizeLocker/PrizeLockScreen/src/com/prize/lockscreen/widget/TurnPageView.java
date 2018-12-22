package com.prize.lockscreen.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

/**
 * 可以滑动的surfaceView
 * @author fanjunchewn
 * 
 */
public class TurnPageView extends SurfaceView {

	/**暂不用**/
	// private GestureDetector mGestureDetector; // 手势
	
	private final int leafNum = 16;
	
	private boolean isPause = true;
	
	private SurfaceHolder holder;
	/**同步锁*/
	private Object mObject = new Object();
	/**背景图片*/
	private Bitmap mBitmap = null;

	public TurnPageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TurnPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TurnPageView(Context context) {
		super(context);
		init();
	}

	private void init() {
		try {
			if (android.os.Build.VERSION.SDK_INT >= 11) {
				setLayerType(LAYER_TYPE_SOFTWARE, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		holder = this.getHolder();
		// setZOrderOnTop(true);
		holder.addCallback(callBack);
		holder.setFormat(PixelFormat.TRANSPARENT);
		setFocusableInTouchMode(true);
		// mGestureDetector = new GestureDetector(mSimpleOnGestureListener);
	}

	public void setBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
		if (mBitmap != null && !mBitmap.isRecycled()) {
			Canvas canvas = holder.lockCanvas(null);
			if (null == canvas) return;
			canvas.drawBitmap(mBitmap, 0, 0, null);
			holder.unlockCanvasAndPost(canvas);
		}
	}

	private SurfaceHolder.Callback callBack = new Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// Log.v("surfaceDestroyed ", "------------");
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// Log.v("surfaceCreated ", "------------");
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// Log.v("surfaceChanged: format:"+format,
			// "width："+width+"height: "+height);
			if (mBitmap != null && !mBitmap.isRecycled()) {
				Canvas canvas = holder.lockCanvas(null);
				canvas.drawBitmap(mBitmap, 0, 0, null);
				holder.unlockCanvasAndPost(canvas);
			}
		}
	};

	/*@Override
	public boolean onTouchEvent(MotionEvent event) {
		// mGestureDetector.onTouchEvent(event); // 通知手势识别方法
		return true;
	}*/

	/*private SimpleOnGestureListener mSimpleOnGestureListener = new SimpleOnGestureListener() {
		
		final int MIN_DISTANCE = 100;
		final int MIN_VELOCITY = 200;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			boolean isHor = (Math.abs(e1.getX() - e2.getX()) > Math.abs(e1
					.getY() - e2.getY()));
			if (isHor) {
				if (e1.getX() - e2.getX() > MIN_DISTANCE
						&& Math.abs(velocityX) > MIN_VELOCITY) {
					Log.d("SimpleOnGestureListener", "Fling left");

					if (null != mFillingListener) {
						mFillingListener.onFlingLeft();
					}
					notifyTurnPage();
				} else if (e2.getX() - e1.getX() > MIN_DISTANCE
						&& Math.abs(velocityX) > MIN_VELOCITY) {
					Log.d("SimpleOnGestureListener", "Fling right");

					if (null != mFillingListener) {
						mFillingListener.onFlingRight();
					}
					notifyTurnPage();
				}
			} else {
				if (e1.getY() - e2.getY() > MIN_DISTANCE
						&& Math.abs(velocityX) > MIN_VELOCITY) {
					Log.d("SimpleOnGestureListener", "Fling up");
					if (null != mFillingListener) {
						mFillingListener.onFlingUp();
					}
					notifyTurnPage();
				} else if (e2.getY() - e1.getY() > MIN_DISTANCE
						&& Math.abs(velocityX) > MIN_VELOCITY) {
					Log.d("SimpleOnGestureListener", "Fling down");
					if (null != mFillingListener) {
						mFillingListener.onFlingDown();
					}
					notifyTurnPage();
				}
			}
			return true;
		}
	};*/

	// let's be nice with the cpu
	@Override
	public void setVisibility(int visibility) {
		if (getVisibility() != visibility) {
			super.setVisibility(visibility);
			isPause = (visibility == GONE || visibility == INVISIBLE);
		}
	}

	// let's be nice with the cpu
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		isPause = (visibility == GONE || visibility == INVISIBLE);
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		isPause = (visibility == GONE || visibility == INVISIBLE);
	}

	public void clearDraw() {
		Canvas canvas = holder.lockCanvas(null);
		canvas.drawColor(Color.BLACK);// 清除画布
		holder.unlockCanvasAndPost(canvas);
	}
	
	private PaintFlagsDrawFilter pdf = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
	
	private Rect src = new Rect();
	/***
	 * 根据移动的距离来展示模拟百页窗
	 * @param distance
	 */
	public void shutter(int distance) {
		
		if (null == mBitmap)
			return;
		
		int dx = (getWidth() - mBitmap.getWidth())/2;
		int dy = (getHeight() - mBitmap.getHeight())/2;
		int perHeight = getHeight()/leafNum;
		
		Canvas canvas = null;
//		while(isRunning)
//		{
//			isRunning = ((runMills = (System.currentTimeMillis() - start)) < duration);
//			if(!isRunning)
//			{
//				runMills = duration;
//			}
			
			try {
					canvas = holder.lockCanvas(null);
					
					canvas.setDrawFilter(pdf);
					canvas.drawColor(Color.BLACK);// 清除画布
					
					canvas.save();
					canvas.translate(dx, dy);
					for(int j=0; j < leafNum; j++)
					{
						src.set(0, (int)((j+1) * perHeight - (1 - distance / (float)getHeight()) * perHeight), getWidth(), (j+1) * perHeight);
						canvas.drawBitmap(mBitmap, src, src, null);
					}
					canvas.rotate(60);
					canvas.restore();
					
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(null != canvas)
				{
					holder.unlockCanvasAndPost(canvas);
				}
			}
//		}
	}
}
