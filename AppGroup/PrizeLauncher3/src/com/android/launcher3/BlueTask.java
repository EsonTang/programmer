package com.android.launcher3;

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

public class BlueTask extends AsyncTask<Bitmap, Void, Drawable[]> {

	private LauncherBackgroudView mWallpaperView;

	public BlueTask(Launcher mLauncher,
			LauncherBackgroudView mLauncherBackground,
			LauncherBackgroudView mWallpaperView) {
		super();
		this.mLauncher = mLauncher;
		this.mLauncherBackground = mLauncherBackground;
		this.mWallpaperView = mWallpaperView;
	}

	private long time;
	private Launcher mLauncher;

	@Override
	protected void onPostExecute(Drawable[] result) {

		super.onPostExecute(result);
		blurBackground(result);
		Log.i("zhouerlong", "耗时:" + (SystemClock.uptimeMillis() - time));
	}

	private Drawable mlauncherOrg = null;
	private Drawable mWallpaperOrg = null;

	/**
	 * 第二种方法的延伸
	 * 
	 * @param bm
	 * @param view
	 * @return void
	 * @author Doraemon
	 * @time 2014年7月7日下午4:56:53
	 */
	private Bitmap rsBlur2(Bitmap bm, View view,int w,int h) {
		Bitmap outputBitmap = Bitmap.createBitmap(
				w,
				h, Bitmap.Config.ARGB_8888);

		RenderScript rs = RenderScript.create(mLauncher);
		ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs,
				Element.U8_4(rs));
		Allocation tmpIn = Allocation.createFromBitmap(rs, bm);
		Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
		theIntrinsic.setRadius(25.f);
		theIntrinsic.setInput(tmpIn);
		theIntrinsic.forEach(tmpOut);
		tmpOut.copyTo(outputBitmap);
		rs.destroy();
		return outputBitmap;

	}

	private LauncherBackgroudView mLauncherBackground;// M by xxf

	public void blurBackground(Drawable bac_launcher[]) {
		// final Drawable bac_launcher = new
		// BitmapDrawable(blurScale(getScreenView()));
		if(bac_launcher[0] !=null) {
			if(mLauncherBackground!=null) {
				mLauncherBackground.setLauncherBg(bac_launcher[0], mlauncherOrg);
				mLauncherBackground.invalidate();
			}
		}
		if(mWallpaperView !=null) {
			mWallpaperView.setLauncherBg(bac_launcher[1], null);
			mWallpaperView.invalidate();
		}
		// mLauncherBackground.setAlpha(0f);
	}

	@Override
	protected Drawable[] doInBackground(Bitmap... params) {
		
		Drawable[] d = new Drawable[2];
		if (mLauncherBackground!=null&& params.length>0) {

			Bitmap screen = params[0];
			time = SystemClock.uptimeMillis();
			mlauncherOrg = ImageUtils.bitmapToDrawable(screen);
			screen = ImageUtils.scaleBitmap(screen);
			int w=screen.getWidth();
			int h =screen.getHeight();
			long time = System.currentTimeMillis();
			Bitmap blurScreen = rsBlur2(screen, mLauncherBackground,w,h);
			 blurScreen = rsBlur2(blurScreen, mLauncherBackground,w,h);
			 long end = System.currentTimeMillis();
			 Log.i("zhouerlong", "time:"+(time-end));
				screen = ImageUtils.scaleBitmap(screen, w, h);
			final Drawable bac_launcher = new BitmapDrawable(blurScreen);
			d[0] = bac_launcher;
		}
		if(mWallpaperView !=null) {
			int w,h;
			Drawable wall = mLauncher.getWallpaper();
			w= wall.getIntrinsicWidth();
			h = wall.getIntrinsicHeight();
			Bitmap blurScreen1 = BlurPic.blurScale(ImageUtils.drawableToBitmap1(wall));
			
			final Drawable bac_launcher1 = new BitmapDrawable(blurScreen1);
			d[1] = bac_launcher1;
		}
		return d;
	}

}