package com.android.settings.wallpaper;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class CropImageBorderView extends View {
	
	protected static final String TAG = CropImageBorderView.class.getSimpleName();
	private int mCropWidth;

	private int mCropHeight;

	private int mShaderColor;

	private int mBorderColor;

	private float mBorderWidth;

	private int mCropShape;

	private Bitmap mIntermediateBitmap;
	
	public CropImageBorderView(Context context) {
		this(context, null);
	}

	public CropImageBorderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CropImageBorderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		int horizontalPadding = (w - mCropWidth) / 2;
		int verticalPadding = (h - mCropHeight) / 2;
		
		mIntermediateBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas intermediateCanvas = new Canvas(mIntermediateBitmap);
		Paint muskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		muskPaint.setColor(mShaderColor);
		muskPaint.setStyle(Style.FILL);
		intermediateCanvas.drawRect(0, 0, intermediateCanvas.getWidth(),
				intermediateCanvas.getHeight(), muskPaint);
		
		RectF rect = new RectF(horizontalPadding, verticalPadding, getWidth()
				- horizontalPadding, getHeight() - verticalPadding);
		Paint transparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		transparentPaint.setColor(Color.TRANSPARENT);
		transparentPaint.setXfermode(new PorterDuffXfermode(
				PorterDuff.Mode.CLEAR));
		switch(mCropShape){
		case 0:
			intermediateCanvas.drawRect(rect, transparentPaint);
			break;
		case 1:
			intermediateCanvas.drawOval(rect, transparentPaint);
			break;
		}
		
		Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		borderPaint.setColor(mBorderColor);
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setStrokeWidth(mBorderWidth);
		switch(mCropShape){
		case 0:
			intermediateCanvas.drawRect(rect, borderPaint);
			break;
		case 1:
			intermediateCanvas.drawOval(rect, borderPaint);
			break;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(mIntermediateBitmap, 0, 0, null);
	}
	
	public void setCropSize(int width, int height) {
		mCropWidth = width;
		mCropHeight = height;
		// invalidate();
	}
	
	public void setBorderColor(int color){
		mBorderColor = color;
	}
	
	public void setBorderWidth(float width){
		mBorderWidth = width;
	}
	
	public void setShaderColor(int color){
		mShaderColor = color;
	}
	
	public void setCropShape(int cropShape){
		mCropShape = cropShape;
	}
	
	public void destroy() {
		if (mIntermediateBitmap != null)
			mIntermediateBitmap.recycle();
	}

}
