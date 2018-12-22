package com.android.launcher3;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class RecommdDragView extends View {

	public RecommdDragView(Context arg0, AttributeSet arg1, int arg2, int arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

	public RecommdDragView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public RecommdDragView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public RecommdDragView(Context arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	private Drawable mbg;

	private float mSrcX, mSrcY, mFinalX, mFinalY;

	private ValueAnimator mAnim;

	private DragLayer mDragLayer;

	private CellLayout mCell;

	public void show() {

		DragLayer.LayoutParams lp = new DragLayer.LayoutParams(0,0);

		// Start the pick-up animation
		lp.width = mbg.getMinimumWidth();
		lp.height = mbg.getMinimumHeight();
		this.setVisibility(View.INVISIBLE);
		lp.customPosition = true;
		setLayoutParams(lp);
		if (mDragLayer.indexOfChild(this) == -1) {
			mDragLayer.addView(this,lp);
			this.setLayerType(LAYER_TYPE_HARDWARE, null);
		}
		post(new Runnable() {
			public void run() {
				mAnim.start();
			}
		});

	}

	void remove() {

		if (getParent() != null) {
			mDragLayer.removeView(this);
		}
	}

	public RecommdDragView(Context c, float srcX, float srcY, float finalX,
			float finalY, Drawable d,CellLayout layout,final Runnable finish) {
		super(c);
		mbg = d;
		this.mSrcX = srcX;
		this.mSrcY = srcY;
		this.mFinalX = finalX;
		this.mFinalY = finalY;
		this.mCell  = layout;
		mDragLayer = ((Launcher) c).getDragLayer();

		mAnim = LauncherAnimUtils.ofFloat(this, 0f, 1f);
		mAnim.setDuration(1000);
		mAnim.setInterpolator(new DecelerateInterpolator());
		mAnim.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
				setVisibility(View.VISIBLE);

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				
				/*try {
					 int[] cel = new int[2];
					mCell.getLastPosition(cel);
					View child =mCell.getChildAt(cel[0], cel[1]);
					child.setVisibility(View.VISIBLE);
				} catch (Exception e) {
					// TODO: handle exception
				}*/
				if(finish!=null)
				{
					finish.run();
				}
				remove();

			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub

			}
		});
		mAnim.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				final float value = (Float) animation.getAnimatedValue();
				float upX = value * (mFinalX - mSrcX);
				float upY = value * (mFinalY - mSrcY);
				setTranslationX(upX + mSrcX);
				setTranslationY(upY + mSrcY);
				float finalScaX = 1 / Launcher.recommdScale;
				float p = (Launcher.recommdScale) + value
						* (1f - Launcher.recommdScale);
				setScaleX(p);
				setScaleY(p);

			}
		});
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mbg.setBounds(0, 0, mbg.getMinimumWidth(), mbg.getMinimumHeight());

		mbg.setColorFilter(Color.GRAY,PorterDuff.Mode.MULTIPLY);
		mbg.draw(canvas);
		
		super.onDraw(canvas);
	}

}
