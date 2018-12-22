package com.android.settings.dashboard;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.graphics.Paint.Align;
import android.graphics.Rect;
public class PrizeNotificationImageView extends ImageView {
	private Paint mPaint;
	private Paint mTextPaint;
	private Rect mRect;
	public PrizeNotificationImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
	}

	public PrizeNotificationImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initPaint();
	}

	public PrizeNotificationImageView(Context context) {
		super(context);
		initPaint();
	}
@Override
protected void onDraw(Canvas canvas) {
	
	super.onDraw(canvas);
	int width = getWidth()/2;
	int heigth = getHeight()/2;
	//canvas.drawCircle(width, heigth, width,mPaint);
	canvas.drawText("1", width, heigth+mRect.height()/2.0f, mTextPaint);
}
	public void initPaint(){
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.RED);
		mTextPaint = new Paint();
		
		mTextPaint.setAntiAlias(true);
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setStrokeWidth(4);
		mTextPaint.setTextSize(30);
		mTextPaint.setTextAlign(Align.CENTER);
		mRect = new Rect();
		mTextPaint.getTextBounds("1", 0, 1, mRect);
	}
}
