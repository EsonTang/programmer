package com.prize.appcenter.ui.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.prize.appcenter.R;

/**
 **
 * 下载悬浮状态按钮
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class ProgressView extends TextView {

	public ProgressView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		// super(context, attrs,defStyleAttr,defStyleRes);
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	private Drawable mDownIcon;
	private Drawable mBg;
	private Drawable mPauseIcon;
	private Drawable mFinishIcon;
	private int pauseW;
	private int pauseH;
	private int fW;
	private int fH;
	private int downW;
	private int bgh;
	private int bgw;
	private int downH;

	public static enum DOWN_STATE {
		NORMAL, DOWNING, PAUSE, FINISH
	};

	public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public ProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDownIcon = context.getResources().getDrawable(R.drawable.xiazai);
		mPauseIcon = context.getResources().getDrawable(R.drawable.zanting);
		mFinishIcon = context.getResources().getDrawable(R.drawable.wancheng);
		mBg = context.getResources().getDrawable(R.drawable.beijing);
		// TODO Auto-generated constructor stub
	}

	public ProgressView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();
		bgw = mBg.getIntrinsicWidth();
		bgh = mBg.getIntrinsicHeight();
		downW = mDownIcon.getIntrinsicWidth();
		pauseH = mPauseIcon.getIntrinsicHeight();
		pauseW = mPauseIcon.getIntrinsicWidth();
		fH = mFinishIcon.getIntrinsicHeight();
		fW = mFinishIcon.getIntrinsicWidth();
		downH = mDownIcon.getIntrinsicHeight();
		mBg.setBounds(0, 0, mBg.getIntrinsicWidth(), mBg.getIntrinsicHeight());
		mBg.draw(canvas);
		drawDownState(mState, canvas);

	}

	private DOWN_STATE mState = DOWN_STATE.NORMAL;

	public DOWN_STATE getState() {
		return mState;
	}

	public void setState(DOWN_STATE mState) {
		this.mState = mState;
	}

	private void drawPause(Canvas canvas) {

		canvas.save();
		canvas.translate((bgw - pauseW) / 2, (bgh - pauseH) / 2);
		mPauseIcon.setBounds(0, 0, (int) (pauseW * mDownPercent),
				mPauseIcon.getIntrinsicHeight());
		mPauseIcon.draw(canvas);
		canvas.restore();

	}

	private void drawFinish(Canvas canvas) {

		canvas.save();
		canvas.translate((bgw - fW) / 2, (bgh - fH) / 2);
		mFinishIcon.setBounds(0, 0, (int) (fW * mDownPercent),
				mFinishIcon.getIntrinsicHeight());
		mFinishIcon.draw(canvas);
		canvas.restore();

	}

	private void drawNormal(Canvas canvas) {

		canvas.save();
		canvas.translate((bgw - downW) / 2, (bgh - downH) / 2);
		mPauseIcon.setBounds(0, 0, mPauseIcon.getIntrinsicWidth(),
				mPauseIcon.getIntrinsicHeight());
		mDownIcon.draw(canvas);
		canvas.restore();

	}

	private void drawDowning(Canvas canvas) {
		canvas.save();
		mDownIcon.setBounds(0, 0, mDownIcon.getIntrinsicWidth(),
				mDownIcon.getIntrinsicHeight());
		int centerX = (bgw - downW) / 2;
		canvas.translate(centerX, mDownPercent * (bgh) - downH);

		if (mDownPercent >= 0.8f) {
			float alphaValue = 1f - mDownPercent;
			mDownIcon.setAlpha((int) (255 * alphaValue * 1 / 0.8f));
		}
		if (mDownPercent <= 0.5f) {
			float alphaValue = mDownPercent;
			mDownIcon.setAlpha((int) (255 * alphaValue * 1 / 0.5f));
		}
		mDownIcon.draw(canvas);
		canvas.restore();
	}

	private float mDownPercent;

	ValueAnimator downing = ObjectAnimator.ofFloat(0f, 1f);

	/**
	 * 状态动画启动入口
	 * 
	 * @param state
	 *            分为三种 下载中，暂停，完成
	 */
	public void setupAnimationState(final DOWN_STATE state) {
		mState = state;
		downing.setDuration(2500);
		downing.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mDownPercent = animation.getAnimatedFraction();
				invalidate();
			}
		});
		if (mState == DOWN_STATE.DOWNING) {
			downing.setRepeatCount(ValueAnimator.INFINITE);
		}
		if (state == DOWN_STATE.PAUSE) {
			downing.setRepeatCount(0);
		}
		if (state == DOWN_STATE.FINISH) {
			downing.setRepeatCount(0);

		}
		downing.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {

				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {

				if (mState == DOWN_STATE.FINISH) {
					setupProgressViewAnimation(false);

				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {

				// TODO Auto-generated method stub

			}
		});
		if (!downing.isRunning())
			downing.start();

	}

	private void drawDownState(DOWN_STATE state, Canvas canvas) {
		if (state == DOWN_STATE.NORMAL) {
			drawNormal(canvas);
		} else if (state == DOWN_STATE.PAUSE) {

			if (mDownPercent >= 0.7f) {
				drawPause(canvas);
			}
		} else if (state == DOWN_STATE.FINISH) {
			if (mDownPercent >= 0.7f) {
				drawFinish(canvas);
			}
		}
		if (state == DOWN_STATE.DOWNING) {
			drawDowning(canvas);
		}
	}

	/**
	 * fad in or fad out 动画
	 */
	public void setupProgressViewAnimation(final boolean fadInOrOut) {
		if (fadInOrOut) {
			mState = DOWN_STATE.NORMAL;
		} else {

		}
		AnimatorSet anim = new AnimatorSet();
		anim.setDuration(1500);
		anim.setInterpolator(new AccelerateDecelerateInterpolator());

		float from = fadInOrOut ? 0f : 1f;
		float to = 1f - from;

		float toR = fadInOrOut ? 0f : 720f;
		float fromR = fadInOrOut ? 720f : 0f;

		int fromT = fadInOrOut ? this.getHeight() : 0;
		int toT = fadInOrOut ? 0 : this.getHeight();
		ObjectAnimator scaleX = ObjectAnimator
				.ofFloat(this, "scaleX", from, to);
		ObjectAnimator scaleY = ObjectAnimator
				.ofFloat(this, "scaleY", from, to);
		ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha",
				fadInOrOut ? 1f : 0f);
		ObjectAnimator rotation = ObjectAnimator.ofFloat(this, "rotation",
				fromR, toR);
		ObjectAnimator translationY = ObjectAnimator.ofFloat(this,
				"translationY", fromT, toT);
		anim.play(scaleY).with(scaleX).with(translationY).with(rotation)
				.with(alpha);
		anim.start();
		anim.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {

				// setVisibility(fadInOrOut ? View.VISIBLE : View.GONE);

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (!fadInOrOut && mState == DOWN_STATE.FINISH) {
					setVisibility(View.GONE);
				}
				if (fadInOrOut) {
					setRotation(720f);
				} else {
					setRotation(0f);
				}

			}

			@Override
			public void onAnimationCancel(Animator animation) {

				// TODO Auto-generated method stub

			}
		});
		this.requestLayout();

	}

}
