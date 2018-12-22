package com.android.contacts.common.prize;

/**
 * Created by prize on 2017/12/8.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.util.TypedValue;
import android.util.Log;
import com.android.contacts.common.R;

public class PrizeCirclePercentView extends View {

    private float mRadius;

    private float mStripeWidth;
    private int mHeight;
    private int mWidth;

    private float mCurPercent;

    private int mPercent;
    private float x;
    private float y;

    private float mEndAngle;

    private int mProgressColor;
    private int mBackgroundColor;
    private int mForegroundColor;

    private float mCenterTextSize;
    
    private String TAG = "PrizeCirclePercentView";

    public PrizeCirclePercentView(Context context) {
        this(context, null);
    }

    public PrizeCirclePercentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrizeCirclePercentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CirclePercentView, defStyleAttr, 0);
        mStripeWidth = a.getDimension(R.styleable.CirclePercentView_stripeWidth, 5);
        mCurPercent = a.getInteger(R.styleable.CirclePercentView_percent, 0);
        mProgressColor = a.getColor(R.styleable.CirclePercentView_progressColor,R.color.prize_theme_color);
        mBackgroundColor = a.getColor(R.styleable.CirclePercentView_backgroundColor,R.color.prize_custom_progressbar_background_color);
        mForegroundColor = a.getColor(R.styleable.CirclePercentView_foregroundColor,R.color.prize_custom_progressbar_foreground_color);
        mRadius = a.getDimension(R.styleable.CirclePercentView_radius,100);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            mRadius = widthSize / 2;
            x = widthSize / 2;
            y = heightSize / 2;
            mWidth = widthSize;
            mHeight = heightSize;
        }

        if(widthMode == MeasureSpec.AT_MOST&&heightMode ==MeasureSpec.AT_MOST){
            mWidth = (int) (mRadius*2);
            mHeight = (int) (mRadius*2);
            x = mRadius;
            y = mRadius;

        }

        setMeasuredDimension(mWidth,mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mEndAngle = mCurPercent * 360;
        Paint bigCirclePaint = new Paint();
        bigCirclePaint.setAntiAlias(true);
        bigCirclePaint.setColor(mBackgroundColor);
        bigCirclePaint.setAntiAlias(true);
        canvas.drawCircle(x, y, mRadius, bigCirclePaint);


        Paint sectorPaint = new Paint();
        sectorPaint.setColor(mProgressColor);
        sectorPaint.setAntiAlias(true);
        RectF rect = new RectF(0, 0, mWidth, mHeight);

        canvas.drawArc(rect, 270, mEndAngle, true, sectorPaint);


        Paint smallCirclePaint = new Paint();
        smallCirclePaint.setAntiAlias(true);
        smallCirclePaint.setColor(mForegroundColor);
        canvas.drawCircle(x, y, mRadius - mStripeWidth, smallCirclePaint);
    }

    public void setPercent(float percent) {
        if (percent > 1) {
        	Log.d(TAG,"[setPercent]  percent > 1");
        	mCurPercent = 1;
        }
        mCurPercent = percent;
    	invalidate();
    	if((int)percent == 1){
        	mOnFinishListener.onFinish();
        }
    }
    
    public int dpToPx(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    private OnFinishListener mOnFinishListener;
	public interface OnFinishListener{
		void onFinish();
	}
	
	public void setOnFinishListener(OnFinishListener onFinishListener){
		mOnFinishListener = onFinishListener;
	}
    
    
}