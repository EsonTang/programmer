package com.android.launcher3;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.view.LauncherBackgroudView;

public class BlueTaskWall extends AsyncTask<Bitmap, Void, Drawable[]> {

	private LauncherBackgroudView mWallpaperView;

	public BlueTaskWall(Activity mLauncher, LauncherBackgroudView mWallpaperView) {
		super();
		this.mLauncher = mLauncher;
		this.mWallpaperView = mWallpaperView;
	}

	private long time;
	private Activity mLauncher;
	private Drawable wall;

	@Override
	protected void onPostExecute(Drawable[] result) {

		super.onPostExecute(result);
		blurBackground(result);
		LogUtils.i("zhouerlong", "耗时:" + (SystemClock.uptimeMillis() - time));
	}

	public void blurBackground(Drawable[] bac_launcher) {
		// final Drawable bac_launcher = new
		// BitmapDrawable(blurScale(getScreenView()));
		mWallpaperView.setLauncherBg(bac_launcher[0], bac_launcher[1]);

		wall=null;
		mWallpaperView.invalidate();
		mWallpaperView=null;
		mLauncher=null;
		
//		mWallpaperView.setVisibility(View.VISIBLE);
		// add by zhouerlong
		/*ObjectAnimator launcherBackground = ObjectAnimator.ofFloat(
				mWallpaperView, "alpha", 0f, 1f);
		
		 * ObjectAnimator launcherBackground =
		 * LauncherAnimUtils.ofPropertyValuesHolder(mLauncherBackground, alpha);
		 
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
		});*/
	}

	@Override
	protected Drawable[] doInBackground(Bitmap... params) {
		Drawable bac_launcher1 = null;
		Drawable[] bc = new Drawable[2];
		if (mWallpaperView != null) {
			android.app.WallpaperManager wallpaperManager = android.app.WallpaperManager
					.getInstance(mLauncher);
			 wall = wallpaperManager.getDrawable();
			Bitmap blurScreen1 = BlurPic.blurScale(ImageUtils
					.drawableToBitmap1(wall));
			Bitmap blurScreen2= Bitmap.createBitmap(blurScreen1);
			 System.gc();
				BitmapDrawable bac_launcher2 = new BitmapDrawable(blurScreen2);
			 
			if(blurScreen1!=null){
                Canvas canvas = new Canvas(blurScreen1);
                canvas.drawColor(0x4c000000);
                canvas.release();
            }

			bac_launcher1 = new BitmapDrawable(blurScreen1);
			bc[0]=bac_launcher1;
			bc[1]=bac_launcher2;

            System.gc();
		}
		return bc;
	}

}