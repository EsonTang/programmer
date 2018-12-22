/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：批量整理动画 多个跟随移动特效类
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-9-2
 *********************************************/
package com.android.launcher3;

import com.android.gallery3d.util.LogUtils;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * @author Administrator
 *批量整理动画 
 *多个跟随移动
 */
public class AnimationRingForDragview implements AnimatorUpdateListener,
		AnimatorListener {
	DragView mDragView;

	private int mLastPointX = 0;
	private int mLastPointY = 0;

	private int touchX = 0;
	private final AccelerateDecelerateInterpolator mZoomInInterpolator = new AccelerateDecelerateInterpolator();

	private int touchY = 0;

	public AnimationRingForDragview(DragView mDragView, int x, int y) {
		super();
		this.mDragView = mDragView;
		mLastPointX = x;
		mLastPointY = y;
		touchX = x;
		touchY = y;
	}

	public void cancel() {
		value.cancel();
	}

	public void setup() {

		value.addUpdateListener(this);
		value.addListener(this);
		value.setDuration(100);
		value.setInterpolator(mZoomInInterpolator);
	}

	ValueAnimator value = ObjectAnimator.ofFloat(0f, 1f);

	private int targetX;

	private int targetY;

	public void move(int startDelay) {
		value = ObjectAnimator.ofFloat(0f, 1f);
		value.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator v) {
				float r = (float) v.getAnimatedValue();
				float deltax = mLastPointX - touchX;
				float deltay = mLastPointY - touchY;
				targetX = (int) ((1 - r) * mLastPointX + r * touchX);
				targetY = (int) ((1 - r) * mLastPointY + r * touchY);
				/*Log.i("zhouerlong", "onAnimationUpdate--touchXY:" + "["
						+ touchX + "," + touchY + " ]" + "mLastPointXY:" + "["
						+ mLastPointX + "," + mLastPointY + " ]" + "targetXY:"
						+ "[" + targetX + "," + targetY + " ]" + "delta" + "["
						+ deltax + "," + deltay + "]");*/
				mDragView.move(targetX, targetY,null);
//				if (Math.round(deltax) > 1f || Math.round(deltay) > 1f) {

					mLastPointX = targetX;
					mLastPointY = targetY;
//				}
			}
		});
		value.addListener(this);
		value.setDuration(100);
		value.setInterpolator(mZoomInInterpolator);
		value.setStartDelay(startDelay);
		value.start();
	}

	public void setPoint(int x, int y, MotionEvent e) {
		if (e == null) {

			mLastPointX = x;
			mLastPointY = y;
		}
		touchX = x;
		touchY = y;
	}

	@Override
	public void onAnimationUpdate(ValueAnimator v) {
		// mLastPointX = targetX;
		// mLastPointY = targetY;

	}

	@Override
	public void onAnimationCancel(Animator arg0) {

		mLastPointX = targetX;
		mLastPointY = targetY;
	/*	Log.i("zhouerlong", "onAnimationUpdate--touchXY:" + "[" + touchX + ","
				+ touchY + " ]" + "mLastPointXY:" + "[" + mLastPointX + ","
				+ mLastPointY + " ]" + "targetXY:" + "[" + targetX + ","
				+ targetY + " ]");*/

	}

	@Override
	public void onAnimationEnd(Animator anim) {
		// mLastPointX = touchX;
		// mLastPointY = touchY;

	}

	@Override
	public void onAnimationRepeat(Animator v) {
		LogUtils.i("zhouerlong", "onAnimationRepeat");
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animator arg0) {
	}

}