package com.prize.lockscreen.widget;

import com.prize.prizelockscreen.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.TextView;
/***
 * 像apple一样的文字光照扫过样式
 * @author fanjunchen
 *
 */
public class LightTextView extends TextView {

	private LinearGradient mLinearGradient;
	private Matrix mGradientMatrix;
	private Paint mPaint;
	private int mViewWidth = 0;
	private int mTranslate = 0;
	
	private int mRefreshTime = 60;

	private boolean mAnimating = true;

	public LightTextView(Context context) {
		this(context, null, 0, 0);
	}

	public LightTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0, 0);
	}

	public LightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}
	
	public LightTextView(Context ctx, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(ctx, attrs, defStyleAttr, defStyleRes);
		
		TypedArray arr = ctx.obtainStyledAttributes(attrs, R.styleable.LightTextView);
		mRefreshTime = arr.getInt(R.styleable.LightTextView_refreshTime, 60);
		
		arr.recycle();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mViewWidth == 0) {
			mViewWidth = getMeasuredWidth();
			if (mViewWidth > 0) {
				mPaint = getPaint();
				// 定义颜色渐变
				mLinearGradient = new LinearGradient(-mViewWidth, 0, 0, 0,
						new int[] { 0x33ffffff, 0xffffffff, 0x33ffffff },
						new float[] { 0, 0.5f, 1 }, Shader.TileMode.CLAMP);
				mPaint.setShader(mLinearGradient);
				mGradientMatrix = new Matrix();
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mAnimating && mGradientMatrix != null) {
			mTranslate += mViewWidth / 10;
			if (mTranslate > 2 * mViewWidth) {
				mTranslate = -mViewWidth;
			}
			mGradientMatrix.setTranslate(mTranslate, 0);
			mLinearGradient.setLocalMatrix(mGradientMatrix);
			postInvalidateDelayed(mRefreshTime);
		}
	}

}
