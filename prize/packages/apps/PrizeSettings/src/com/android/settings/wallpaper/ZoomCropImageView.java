package com.android.settings.wallpaper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public class ZoomCropImageView extends ImageView implements
             OnScaleGestureListener, OnTouchListener,
             ViewTreeObserver.OnGlobalLayoutListener{
	
	protected static final String TAG = ZoomCropImageView.class.getSimpleName();

	public float mScaleMax;

	public static float mScaleMin;


	private int mCropShape;

	private float initScale = 1.0f;
	private boolean once = true;

	private final float[] matrixValues = new float[9];

	private ScaleGestureDetector mScaleGestureDetector = null;
	
	private final Matrix mScaleMatrix = new Matrix();
	
	private final Matrix originMatrix;

	private int mTouchSlop;

	private float mLastX;
	private float mLastY;

	private boolean isCanDrag;
	private int lastPointerCount;
	private boolean canScale = false;
	private static boolean isWidthChart = false;
	private static boolean isLongChart = false;
	public ZoomCropImageView(Context context) {
		this(context, null);
	}

	public ZoomCropImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressLint("ClickableViewAccessibility")
	public ZoomCropImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setScaleType(ScaleType.MATRIX);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		this.setOnTouchListener(this);
		originMatrix = getImageMatrix();
	}
	
	private class AutoScaleRunnable implements Runnable {
		static final float BIGGER = 1.07f;
		static final float SMALLER = 0.93f;
		private float mTargetScale;
		private float tmpScale;
		private float x;
		private float y;
		
		public AutoScaleRunnable(float targetScale, float x, float y) {
			this.mTargetScale = targetScale;
			this.x = x;
			this.y = y;
			if (getScale() < mTargetScale) {
				tmpScale = BIGGER;
			} else {
				tmpScale = SMALLER;
			}
		
	}

		@Override
		public void run() {
			mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
			checkBorder();
			setImageMatrix(mScaleMatrix);

			final float currentScale = getScale();
			if (((tmpScale > 1f) && (currentScale < mTargetScale))
					|| ((tmpScale < 1f) && (mTargetScale < currentScale))) {
				ZoomCropImageView.this.postDelayed(this, 16);
			} else
			{
				final float deltaScale = mTargetScale / currentScale;
				mScaleMatrix.postScale(deltaScale, deltaScale, x, y);
				checkBorder();
				setImageMatrix(mScaleMatrix);
			}
		}
	}
	
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getScale();
		float scaleFactor = detector.getScaleFactor();

		if (getDrawable() == null)
			return true;
		if ((scale < mScaleMax && scaleFactor > 1.0f)
				|| (scale > mScaleMin && scaleFactor < 1.0f)) {
			if (scaleFactor * scale < mScaleMin) {
				scaleFactor = mScaleMin / scale;
			}
			if (scaleFactor * scale > mScaleMax) {
				scaleFactor = mScaleMax / scale;
			}
			mScaleMatrix.postScale(scaleFactor, scaleFactor,
					detector.getFocusX(), detector.getFocusY());
			checkBorder();
			setImageMatrix(mScaleMatrix);
		}
		return true;
	}
	
	private RectF getMatrixRectF() {
		Matrix matrix = mScaleMatrix;
		RectF rect = new RectF();
		Drawable d = getDrawable();
		if (null != d) {
			rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			matrix.mapRect(rect);
		}
		return rect;
	}
	
	@Override
	public boolean onScaleBegin(ScaleGestureDetector arg0) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public void onScaleEnd(ScaleGestureDetector arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		mScaleGestureDetector.onTouchEvent(event);
		float x = 0, y = 0;
		final int pointerCount = event.getPointerCount();
		for (int i = 0; i < pointerCount; i++) {
			x += event.getX(i);
			y += event.getY(i);
		}
		x = x / pointerCount;
		y = y / pointerCount;
		
		if (pointerCount != lastPointerCount) {
			isCanDrag = false;
			mLastX = x;
			mLastY = y;
		}
		
		lastPointerCount = pointerCount;
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			float dx = x - mLastX;
			float dy = y - mLastY;
			
			if (!isCanDrag) {
				isCanDrag = isCanDrag(dx, dy);
			}
			if (isCanDrag) {
				if (getDrawable() != null) {

					RectF rectF = getMatrixRectF();
					if (rectF.width() <= mCropWidth || isLongChart) {
						dx = 0;
					}
					if (rectF.height() <= mCropHeight || isWidthChart) {
						dy = 0;
					}
					mScaleMatrix.postTranslate(dx, dy);
					checkBorder();
					setImageMatrix(mScaleMatrix);
				}
			}
			mLastX = x;
			mLastY = y;
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			lastPointerCount = 0;
			break;
		}

		return true;
	}
	
	public final float getScale() {
		mScaleMatrix.getValues(matrixValues);
		return matrixValues[Matrix.MSCALE_X];
	}
	
	public final void resetMatrix() {
		originMatrix.getValues(matrixValues);
		
		mScaleMatrix.setValues(matrixValues);
		
		setImageMatrix(mScaleMatrix);
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}
	
	private int mCropWidth;
	
	private int mCropHeight;
	
	@Override
	public void onGlobalLayout() {
		if (once) {
			Drawable d = getDrawable();
			if (d == null)
				return;
			
			int width = getWidth();
			int height = getHeight();
			int dw = d.getIntrinsicWidth();
			int dh = d.getIntrinsicHeight();
			float scale = 1.0f;
			
			if (dw < mCropWidth && dh > mCropHeight) {
				scale = mCropWidth * 1.0f / dw;
			}

			if (dh < mCropHeight && dw > mCropWidth) {
				scale = mCropHeight * 1.0f / dh;
			}
			
			if (dw < mCropWidth && dh < mCropHeight) {
				float scaleW = mCropWidth * 1.0f / dw;
				float scaleH = mCropHeight * 1.0f / dh;
				scale = Math.max(scaleW, scaleH);
			}

			initScale = scale;
			
			float scaleW = mCropWidth * 1.0f / dw;
			float scaleH = mCropHeight * 1.0f / dh;
			mScaleMin = Math.max(Math.max(scaleW, scaleH), mScaleMin);
			mScaleMax = Math.max(mScaleMax, mScaleMin);
			mScaleMatrix.postTranslate((width - dw) / 2, (height - dh) / 2);
			mScaleMatrix.postScale(scale, scale, getWidth() / 2,
					getHeight() / 2);
			setImageMatrix(mScaleMatrix);
			once = false;
		}
	}
	
	public Bitmap crop(int outputWidth, int outputHeight) {
		int width = getWidth();
		int height = getHeight();
		int horizontalPadding = (width - mCropWidth) / 2;
		int verticalPadding = (height - mCropHeight) / 2;
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
// 		bitmap.setHasAlpha(true);
		Canvas canvas = new Canvas(bitmap);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
				Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG));
		Path clipPath = new Path();
		RectF rect = new RectF(horizontalPadding, verticalPadding, width
				- horizontalPadding, height - verticalPadding);
		switch(mCropShape){
		case 0:
			clipPath.addRect(rect, Direction.CW);
			break;
		case 1:
			clipPath.addOval(rect, Direction.CW);
			break;
		}
		canvas.clipPath(clipPath);

		draw(canvas);

		if(mCropWidth <= 0){
			Log.i("setting", "mCropWidth <= 0 = " + mCropWidth);
			mCropWidth = 720;
		}
		if(mCropHeight <= 0){
			mCropHeight = 1280;
		}
		Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, horizontalPadding,
				verticalPadding, mCropWidth, mCropHeight);
		Bitmap resultBp = null;
		if (croppedBitmap.getWidth()==outputWidth&&croppedBitmap.getHeight()==outputHeight){
			resultBp = croppedBitmap;
		}else {
			resultBp = Bitmap.createScaledBitmap(croppedBitmap, outputWidth, outputHeight, true);
		}
		if (bitmap != null)
			bitmap.recycle();
		if (croppedBitmap != null && croppedBitmap != resultBp)
			croppedBitmap.recycle();
		return resultBp;
		
	}
	
	private void checkBorder() {

		RectF rect = getMatrixRectF();
		float deltaX = 0;
		float deltaY = 0;
		
		int width = getWidth();
		int height = getHeight();

		int horizontalPadding = (width - mCropWidth) / 2;
		int verticalPadding = (height - mCropHeight) / 2;
		
		if (rect.width() + 0.01 >= mCropWidth) {
			if (rect.left > horizontalPadding) {
				deltaX = -rect.left + horizontalPadding;
			}
			if (rect.right < width - horizontalPadding) {
				deltaX = width - horizontalPadding - rect.right;
			}
		}
		
		if (rect.height() + 0.01 >= mCropHeight) {
			if (rect.top > verticalPadding) {
				deltaY = -rect.top + verticalPadding;
			}
			if (rect.bottom < height - verticalPadding) {
				deltaY = height - verticalPadding - rect.bottom;
			}
		}
		mScaleMatrix.postTranslate(deltaX, deltaY);
		
	}
	
	private boolean isCanDrag(float dx, float dy) {
		return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
	}
	
	public void setCropSize(int cropWidth, int cropHeight) {
		boolean b = false;
		if (mCropWidth != cropWidth) {
			mCropWidth = cropWidth;
			b = true;
		}
		if (mCropHeight != cropHeight) {
			mCropHeight = cropHeight;
			b = true;
		}
		if (b) getRawScale();
	}
	

	public void setCropShape(int cropShape){
		mCropShape = cropShape;
	}

	public void setScaleSize(float scaleMin, float scaleMax){
		// integrity check
		if(scaleMin > scaleMax){
			throw new IllegalArgumentException(
					"scaleMin must not be greater than scaleMax");
		}
		mScaleMin = Math.max(scaleMin, mScaleMin);
		mScaleMax = Math.max(scaleMax, mScaleMin);
	}
	
	public void setCanScale(boolean b) {
		canScale = b;
	}
	
	private void getRawScale() {
		Drawable d = getDrawable();
		if (d == null)
			return;
		int dw = d.getIntrinsicWidth();
		int dh = d.getIntrinsicHeight();
		float scale = 1.0f;
		if (dw < mCropWidth && dh > mCropHeight) {
			scale = mCropWidth * 1.0f / dw;
		}
		
		if (dh < mCropHeight && dw > mCropWidth) {
			scale = mCropHeight * 1.0f / dh;
		}

		if (dw < mCropWidth && dh < mCropHeight) {
			float scaleW = mCropWidth * 1.0f / dw;
			float scaleH = mCropHeight * 1.0f / dh;
			scale = Math.max(scaleW, scaleH);
		}
		
		initScale = scale;
		float scaleW = mCropWidth * 1.0f / dw;
		float scaleH = mCropHeight * 1.0f / dh;
		mScaleMin = Math.max(Math.max(scaleW, scaleH), mScaleMin);
		mScaleMax = Math.max(mScaleMax, mScaleMin);
	}
	public static void setScaleMin(float scaleMin, boolean isLong, boolean isWidth){
		mScaleMin = scaleMin;
		isLongChart = isLong;
		isWidthChart = isWidth;
	}
}
