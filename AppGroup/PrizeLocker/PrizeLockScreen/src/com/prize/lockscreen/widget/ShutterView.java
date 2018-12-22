package com.prize.lockscreen.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
/***
 * 百叶窗小图片
 * @author fanjunchen
 *
 */
public class ShutterView extends View {
	
	/**背景图片*/
	private Bitmap mBitmap;
	
	private Rect dst = new Rect();
	
	private Rect src;
	
	private final int DURATION = 350;
	
	private boolean isGone = false;
	/**是否正在动画中**/
	private boolean isAniming = false;
	
	public ShutterView(Context context) {
		super(context);
	}

	public ShutterView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ShutterView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public ShutterView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	
	/***
	 * 调用这个之前确保已经调用过 {@link #setSrc(Rect)} 和  {@link #setBitmap(Bitmap)}
	 */
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (null == mBitmap)
			return;
		dst.set(0, 0, getWidth(), getHeight());
		canvas.drawBitmap(mBitmap, src, dst, null);
	}
	
	
	public void setSrc(Rect src) {
		this.src = src;
	}
	
	
	public Rect getSrc() {
		return src;
	}
	
	public void setBitmap(Bitmap m) {
		mBitmap = m;
	}
	
	/****
	 * 动画到消失
	 */
	public void animToEnd() {
		
		if (isAniming || getVisibility() == View.INVISIBLE)
			return;
		isAniming = true;
		isGone = true;
		// setVisibility(View.VISIBLE);
		ObjectAnimator a = ObjectAnimator.ofFloat(this, "rotationX", 0.0F, 90.0F)  
         	.setDuration(DURATION); 
		a.addListener(mListener);
        a.start(); 
	}
	
	/****
	 * 动画恢复到原始状态
	 */
	public void animToStart() {
		if (isAniming || getVisibility() == View.VISIBLE)
			return;
		isAniming = true;
		isGone = false;
		ObjectAnimator a = ObjectAnimator.ofFloat(this, "rotationX", 90.0F, 0.0F)  
         	.setDuration(DURATION); 
		a.addListener(mListener);
        a.start(); 
	}
	
	private AnimatorListener mListener = new AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animation) {
			// TODO Auto-generated method stub
			if (!isGone)
				ShutterView.this.setVisibility(View.VISIBLE);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (isGone)
				ShutterView.this.setVisibility(View.INVISIBLE);
			isAniming = false;
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			isAniming = false;
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			
		}
		
	};
}
