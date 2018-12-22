package com.android.HorCali.sensor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.android.HorCali.R;
import android.util.Log;

public class HorizontalCalibrationView extends View {

	boolean isCalibrating = false;
	float bubbleX;
	float bubbleY;
	float bubbleX0;
	float bubbleY0;
	float radius;
	private Paint paint;
	Bitmap background;
	Bitmap bubble;
	Bitmap bubble_green;
	public int mWidth;
	public int mHeight;
	boolean isBubbleInit = true;
	private int mBollWidth;
	private int mBollHeight;

	public HorizontalCalibrationView(Context context, AttributeSet attrs) {
		super(context, attrs);

		background = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_calibration_background);
		mWidth = background.getWidth();
		mHeight = background.getHeight();
		bubble = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_calibration_bubble);
		bubble_green = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_calibration_bubble_green);
		
		mBollWidth = bubble.getWidth();
		mBollHeight = bubble.getHeight();
		
		radius = (float)(mWidth * 0.9/ 2);
		paint = new Paint();
		paint.setColor(Color.GREEN);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3); 
		paint.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		bubbleX0 =  (canvas.getWidth() - bubble.getWidth())/2;
		bubbleY0 =  (canvas.getHeight() - bubble.getHeight())/2;
		canvas.drawBitmap(background, 0, 0, null);
		if (!isCalibrating) {
			if (!isBubbleInit) {
			canvas.drawBitmap(bubble, bubbleX, bubbleY, null);
			}
		} else {
			canvas.drawBitmap(bubble_green, (mWidth - mBollWidth) / 2, (mHeight - mBollHeight) / 2, null);
			canvas.drawCircle(mWidth / 2, mHeight / 2, radius, paint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(mWidth, mHeight);
	}
}
