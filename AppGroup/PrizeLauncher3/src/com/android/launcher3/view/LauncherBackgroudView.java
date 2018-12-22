package com.android.launcher3.view;

import com.android.gallery3d.util.LogUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.View;

public class LauncherBackgroudView extends View {

	private Drawable mLauncherBg = null;
	private Drawable mLauncherOrgBg = null;
	private float mProgress = 1f;

	public void setLauncherBg(Drawable mLauncherBg, Drawable org) {
		this.mLauncherBg = mLauncherBg;
		this.mLauncherOrgBg = org;
	}

	@Override
	@RemotableViewMethod
	public void setVisibility(int visibility) {
		// TODO Auto-generated method stub
		super.setVisibility(visibility);
	}

	public LauncherBackgroudView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public LauncherBackgroudView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public LauncherBackgroudView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public void setprogress(float p) {
		mProgress = p;
		invalidate();
	}

	public LauncherBackgroudView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();

		int w = getMeasuredWidth();
		int h = getMeasuredHeight();
		if (mLauncherOrgBg != null) {
			canvas.save();
			
			int alpha = (int) ((1f - mProgress) * 255);
			LogUtils.i("zhouerlong", "alpha:---pro" + alpha);
			mLauncherOrgBg.setAlpha(alpha);
			mLauncherOrgBg.setBounds(0, 0, w, h);
			mLauncherOrgBg.draw(canvas);
			canvas.restore();
		}
		if (mLauncherBg != null) {
			canvas.save();
			mLauncherBg.setAlpha((int) (mProgress * 255f));
			mLauncherBg.setBounds(0, 0, w, h);
			mLauncherBg.draw(canvas);
			canvas.restore();
		}

	}

}
