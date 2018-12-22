package com.android.launcher3;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;

import com.android.launcher3.view.LauncherBackgroudView;

public class BlueTaskWall extends AsyncTask<Bitmap, Void, Drawable> {

	private LauncherBackgroudView mWallpaperView;

	public BlueTaskWall(Launcher mLauncher, LauncherBackgroudView mWallpaperView) {
		super();
		this.mLauncher = mLauncher;
		this.mWallpaperView = mWallpaperView;
	}

	private long time;
	private Launcher mLauncher;

	@Override
	protected void onPostExecute(Drawable result) {

		super.onPostExecute(result);
		blurBackground(result);
		Log.i("zhouerlong", "耗时:" + (SystemClock.uptimeMillis() - time));
	}

	public void blurBackground(Drawable bac_launcher) {
		// final Drawable bac_launcher = new
		// BitmapDrawable(blurScale(getScreenView()));
		mWallpaperView.setLauncherBg(bac_launcher, null);
		mWallpaperView.invalidate();
		mWallpaperView.setVisibility(View.VISIBLE);
		// add by zhouerlong
		ObjectAnimator launcherBackground = ObjectAnimator.ofFloat(
				mWallpaperView, "alpha", 0f, 1f);
		/*
		 * ObjectAnimator launcherBackground =
		 * LauncherAnimUtils.ofPropertyValuesHolder(mLauncherBackground, alpha);
		 */
		launcherBackground.setDuration(500);
		launcherBackground.start();
		launcherBackground.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				mWallpaperView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub

				mWallpaperView.setLayerType(View.LAYER_TYPE_NONE, null);
				mWallpaperView.setAlpha(1f);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	protected Drawable doInBackground(Bitmap... params) {
		Drawable bac_launcher1 = null;
		if (mWallpaperView != null) {
			int w, h;
			Drawable wall = mLauncher.getWallpaper();
			w = wall.getIntrinsicWidth();
			h = wall.getIntrinsicHeight();
			Bitmap blurScreen1 = BlurPic.blurScale(ImageUtils
					.drawableToBitmap1(wall));

			bac_launcher1 = new BitmapDrawable(blurScreen1);
		}
		return bac_launcher1;
	}

}