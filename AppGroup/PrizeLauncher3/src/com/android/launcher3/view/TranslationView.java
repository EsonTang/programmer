package com.android.launcher3.view;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class TranslationView extends View {

	private Drawable mLauncherBg = null;
	private Drawable mLauncherOrgBg = null;
	private float mProgress;

	public void setLauncherBg(Drawable mLauncherBg,Drawable org) {
		this.mLauncherBg = mLauncherBg;
		this.mLauncherOrgBg = org;
	}

	public TranslationView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public TranslationView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public TranslationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public void setprogress(float p) {
		mProgress = p;
	}

	public TranslationView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();
		if (mLauncherBg == null) {
			return;
		}
		int w = getMeasuredWidth();
		int h = getMeasuredHeight();
		mLauncherBg.setAlpha((int)mProgress*255);
		mLauncherBg.setBounds(0, 0, w, h);
		mLauncherBg.draw(canvas);
		canvas.restore();

		canvas.save();
		if (mLauncherOrgBg == null) {
			return;
		}
		mLauncherOrgBg.setAlpha((int) ((1f-mProgress)*255));
		mLauncherOrgBg.setBounds(0, 0, w, h);
		mLauncherOrgBg.draw(canvas);
		canvas.restore();
		
		
	}

}
