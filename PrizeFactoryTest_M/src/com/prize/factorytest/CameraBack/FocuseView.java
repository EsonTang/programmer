package com.prize.factorytest.CameraBack;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.prize.factorytest.R;

public class FocuseView extends View {
	public static final int STATE_UNFOCUSE = 0;
	public static final int STATE_FOCUSING = 1;
	private static final int SCALING_DOWN_TIME = 200;
	private static final int DISAPPEAR_TIMEOUT = 200;

	private int mState = STATE_UNFOCUSE;
	private Runnable mDisappear = new Disappear();
	private Runnable mEndAction = new EndAction();

	public FocuseView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FocuseView(Context context) {
		super(context);
	}

	public void moveView(float x, float y) {
		setX(x);
		setY(y);
	}

	public void showStart() {
		if (mState == STATE_UNFOCUSE) {
			setAlpha(1.0f);
			setBackgroundResource(R.drawable.ic_focus_focusing);
			animate().setDuration(SCALING_DOWN_TIME).scaleX(1.3f).scaleY(1.3f)
					.start();
			mState = STATE_FOCUSING;
		}
	}

	public void showSuccess(boolean timeout) {
		if (mState == STATE_FOCUSING) {
			setBackgroundResource(R.drawable.ic_focus_focused);
			animate().setDuration(SCALING_DOWN_TIME).scaleX(1f).scaleY(1f)
					.withEndAction(timeout ? mEndAction : null);
		}
	}

	public int getFocuseState() {
		return mState;
	}

	private class EndAction implements Runnable {
		@Override
		public void run() {
			postDelayed(mDisappear, DISAPPEAR_TIMEOUT);
		}
	}

	private class Disappear implements Runnable {
		@Override
		public void run() {
			setAlpha(0.0f);
			mState = STATE_UNFOCUSE;
		}
	}
}
