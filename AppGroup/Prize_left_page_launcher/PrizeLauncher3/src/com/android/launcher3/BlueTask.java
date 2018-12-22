package com.android.launcher3;

import com.android.gallery3d.util.LogUtils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

public class BlueTask extends AsyncTask<Bitmap, Void, Drawable> {

	private View mWallpaperView;

	public BlueTask(Activity mLauncher, View wall,Drawable dbk) {
		super();
		this.mLauncher = mLauncher;
		this.wall = dbk;
		mWallpaperView=wall;
	}

	private long time;
	private Activity mLauncher;
	private Drawable wall;

	@Override
	protected void onPostExecute(Drawable result) {

		super.onPostExecute(result);
		blurBackground(result);
		LogUtils.i("zhouerlong", "耗时:" + (SystemClock.uptimeMillis() - time));
	}

	public void blurBackground(Drawable bac_launcher) {
		bac_launcher.setAlpha(220);
		mWallpaperView.setBackground(bac_launcher);
		wall=null;
	}

	@Override
	protected Drawable doInBackground(Bitmap... params) {
		Drawable bac_launcher1 = null;
		if (mWallpaperView != null) {
			int w, h;
			android.app.WallpaperManager wallpaperManager = android.app.WallpaperManager
					.getInstance(mLauncher);
			Drawable walls = wallpaperManager.getFastDrawable();
			w = walls.getIntrinsicWidth();
			h = walls.getIntrinsicHeight();
			Bitmap blurScreen1 = BlurPic.blurScale(ImageUtils
					.drawableToBitmap1(walls));
			blurScreen1 =ImageUtils.resize(blurScreen1, w, h);
			blurScreen1=	ImageUtils.createMaskImage(blurScreen1, ImageUtils.drawableToBitmap1(wall));

			bac_launcher1 = new BitmapDrawable(blurScreen1);
		}
		return bac_launcher1;
	}

}