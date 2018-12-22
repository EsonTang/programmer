package com.example.longshotscreen.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.longshotscreen.R;
import com.example.longshotscreen.manager.SuperShotFloatViewManager;
import com.example.longshotscreen.utils.SuperShotUtils;

public class SavingAnimation
{
	private static SavingAnimation mSavingAnimation;
	private Animation mAnimationFirst;
	private Animation mAnimationSecond;
	private Context mContext;
	private SuperShotFloatViewManager mFloatViewManager;
	private FrameLayout mFrameLayout;
	private Handler mHandler = new Handler();
	private ImageView mImageView;
	private WindowManager.LayoutParams mLayoutParams;
	private Bitmap mSavingBitmap;
	private int mScreenHeight;
	private int mScreenWidth;
	private WindowManager mWindowManager;

	private SavingAnimation(Context context)
	{
		this.mContext = context;
		this.mWindowManager = ((WindowManager)context.getApplicationContext().getSystemService("window"));
		initParams();
		setSavingLayoutParams();
	}

	public static SavingAnimation getInstance(Context context)
	{
		if (mSavingAnimation == null){
			mSavingAnimation = new SavingAnimation(context);
		}
		return mSavingAnimation;
	}

	private void initParams()
	{
		mFloatViewManager = SuperShotFloatViewManager.getInstance(mContext);
		Point mPoint = mFloatViewManager.getScreenSize();
		mScreenWidth = mPoint.x;
		mScreenHeight = mPoint.y;
		mAnimationFirst = AnimationUtils.loadAnimation(mContext, R.anim.anim);
		mAnimationSecond = AnimationUtils.loadAnimation(mContext, R.anim.anim_out);
		mAnimationFirst.setFillAfter(true);
		mAnimationSecond.setFillAfter(true);

		mAnimationFirst.setAnimationListener(new Animation.AnimationListener() {

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				mHandler.postDelayed(new Runnable() {

					public void run() {
						mImageView.startAnimation(mAnimationSecond);
					}
				}, 800);
			}
		});
		mAnimationSecond.setAnimationListener(new Animation.AnimationListener() {

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				mHandler.postDelayed(new Runnable() {

					public void run() {
						if(SuperShotUtils.mIsSaveComplete) {
							 SavingAnimation.this.recycleBitmap();//syc add
							return;
						}
						mHandler.postDelayed(this, 50);
					}
				}, 100);
			}
		});
	}

	private void recycleBitmap()
	{
		if (mSavingBitmap != null)
		{
			mSavingBitmap.recycle();
			mSavingBitmap = null;
		}
		dismiss();
	}

	public void dismiss()
	{
		mWindowManager.removeView(mFrameLayout);
	}

	public void setSavingLayoutParams()
	{
		mLayoutParams = mFloatViewManager.getLayoutParams();
		mLayoutParams.flags = 1288;
		mLayoutParams.width = mScreenWidth;
		mLayoutParams.height = mScreenHeight;
	}

	public void show(Bitmap bitmap)
	{
		mSavingBitmap = bitmap;
		initParams();
		setSavingLayoutParams();
		mFrameLayout = ((FrameLayout)LayoutInflater.from(mContext).inflate(R.layout.saving_animation, null));
		mImageView = ((ImageView)mFrameLayout.findViewById(R.id.saving_animation));
		mImageView.setImageBitmap(bitmap);
		mWindowManager.addView(mFrameLayout, mLayoutParams);
		mImageView.startAnimation(mAnimationFirst);
	}
}
