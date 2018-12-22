package com.prize.music.ui.widgets;

import com.prize.music.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.Button;

public class PrizeCircleButton extends Button {
	
	private Resources mResources = null;
	
	private float mWidth = 0;
	private float mHeight = 0;
	private float mRadius = 0;
	private int mLineColor = 0;
	private int mPaneNormalColor = 0;
	private int mPanePressedColor = 0;
	private String mText;
	
	private Paint mPaint = new Paint();;
	RectF mArcRect = new RectF();
	

	public PrizeCircleButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    public PrizeCircleButton(Context context, AttributeSet attrs) {
    	super(context, attrs);  
    	
        mContext = context;
        mResources = mContext.getResources();
        
        TypedArray typedarray = context.obtainStyledAttributes(attrs, R.styleable.PrizeCircleButton);
        mRadius = typedarray.getDimension(R.styleable.PrizeCircleButton_radius, 0);        
        mLineColor = typedarray.getColor(R.styleable.PrizeCircleButton_line_color, 0);
        mPaneNormalColor = typedarray.getColor(R.styleable.PrizeCircleButton_pane_normal_color, 0);
        mPanePressedColor = typedarray.getColor(R.styleable.PrizeCircleButton_pane_pressed_color, 0);
        
        mWidth = getWidth();
        mHeight = getHeight();
        
        mRadius = (float) (mHeight/2 -1);
                
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);       

    }

    public PrizeCircleButton(Context context, AttributeSet attrs,int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float xCenter = getWidth() / 2;
        float yCenter = getHeight() / 2;
        drawLine(canvas, xCenter, yCenter);
        
    }
    
    /**
     * @see 画边框
     * @param canvas
     * @param xCenter
     * @param yCenter
     */
    private void drawLine(Canvas canvas, float xCenter, float yCenter){
    	float xLeftCircleCenter = mRadius + 1;
    	float yLeftCircleCenter = yCenter;
    	
    	float xRightCircleCenter = 2 * xCenter - mRadius -1;
    	float yRightCircleCenter = yCenter;
    	
    	float topLineLeftXVertex = mRadius + 1;
    	float topLineLeftYVertex = 1;
    	
    	float topLineRightXVertex = 2 * xCenter - mRadius -1;
    	float topLineRightYVertex = 1;
    	
    	float bottomLineLeftXVertex = mRadius + 1;
    	float bottomLineLeftYVertex = 2 * yCenter -1;

    	float bottomLineRightXVertex = 2 * xCenter - mRadius -1;
    	float bottomLineRightYVertex = 2 * yCenter -1;

    	mPaint.setColor(Color.BLACK);
    	mPaint.setStyle(Paint.Style.STROKE);
    	mPaint.setStrokeWidth(1);
    	canvas.drawLine(topLineLeftXVertex, topLineLeftYVertex, topLineRightXVertex, topLineRightYVertex, mPaint);
    	canvas.drawLine(bottomLineLeftXVertex, bottomLineLeftYVertex, bottomLineRightXVertex, bottomLineRightYVertex, mPaint);
    	
    	
    	mPaint.setStrokeWidth(3);
    	mArcRect.top = 1;
    	mArcRect.bottom = getHeight() - 1;
    	mArcRect.left = 1;
    	mArcRect.right = 2 * mRadius + 1;    	
    	canvas.drawArc(mArcRect, 0, +180, false, mPaint);
    	
    	mArcRect.top = 1;
    	mArcRect.bottom = getHeight() - 1;
    	mArcRect.left = getWidth() - 2 * mRadius - 1;
    	mArcRect.right = getWidth() - 1;    	
    	canvas.drawArc(mArcRect, 90, -180, false, mPaint);

    	
    }
    
    
}
