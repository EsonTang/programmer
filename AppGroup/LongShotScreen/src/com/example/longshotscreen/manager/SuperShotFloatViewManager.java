package com.example.longshotscreen.manager;

import java.lang.reflect.Method;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import com.example.longshotscreen.utils.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.example.longshotscreen.ui.FunnyShotView;
import com.example.longshotscreen.ui.ScrollShotView;

public class SuperShotFloatViewManager
{
	private static SuperShotFloatViewManager mShotFloatViewManager;
	private Context mContext;
	private Display mDisplay;
	private Matrix mDisplayMatrix;
	private DisplayMetrics mDisplayMetrics;
	private FunnyShotView mFunnyShotView;
	private WindowManager.LayoutParams mLayoutParams;
	private ScrollShotView mScrollShotView;
	private int heightPixels;
	private SuperShotFloatViewManager(Context context)
	{
		mContext = context;
		initDisplayParams();
		initLayoutParams();
	}

	public static SuperShotFloatViewManager getInstance(Context context) {
		if(mShotFloatViewManager == null) {
			mShotFloatViewManager = new SuperShotFloatViewManager(context);
		}
		return mShotFloatViewManager;
	}

	private void initDisplayParams()
	{
	   
		WindowManager windowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
		mDisplayMatrix = new Matrix();
		mDisplay = windowManager.getDefaultDisplay();
		mDisplayMetrics = new DisplayMetrics();
		mDisplay.getMetrics(mDisplayMetrics);
		
		heightPixels = mDisplayMetrics.heightPixels;
		 Log.i("xxx", "---heightPixels--0 = " + heightPixels);
		 if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)  
		        try {  
		            heightPixels = (Integer) Display.class  
		                    .getMethod("getRawHeight").invoke(mDisplay);  
		            
		            Log.i("xxx", "---heightPixels--1 = " + heightPixels);
		        } catch (Exception ignored) {  
		        }
		 else if (Build.VERSION.SDK_INT >= 17)  
		        try {  
		            android.graphics.Point realSize = new android.graphics.Point();  
		            Display.class.getMethod("getRealSize",  
		                    android.graphics.Point.class).invoke(mDisplay, realSize);  
		            heightPixels = realSize.y;  
		            Log.i("xxx", "---heightPixels--2 = " + heightPixels);
		        } catch (Exception ignored) {  
		        }  
		 
		 			Log.i("xxx", "---heightPixels--3 = " + heightPixels);
	}

	public void initLayoutParams()
	{
		  mLayoutParams = new WindowManager.LayoutParams();
	        mLayoutParams.type = 2024;
	        /*prize-add | mLayoutParams.FLAG_NOT_FOCUSABLE-fix bug[31724]-huangpengfei-2017-4-6-start*/
	        mLayoutParams.flags = 0x20520 | mLayoutParams.FLAG_NOT_FOCUSABLE;
	        /*prize-add | mLayoutParams.FLAG_NOT_FOCUSABLE-fix bug[31724]-huangpengfei-2017-4-6-end*/
	        mLayoutParams.gravity = Gravity.BOTTOM;
	        mLayoutParams.x = 0x0;
	        mLayoutParams.y = 0x0;
	        mLayoutParams.width = mDisplayMetrics.widthPixels;
	        mLayoutParams.height = heightPixels;//mDisplayMetrics.heightPixels;
	        mLayoutParams.format = 0x1;
	        mLayoutParams.windowAnimations = 0x0;
	}

	private void initFunnyShotView()
	{
		mFunnyShotView = new FunnyShotView(mContext);
		mFunnyShotView.setScreenSize(mDisplayMetrics.widthPixels, heightPixels);
		//Log.i("xxx", "mDisplayMetrics.widthPixels = " + mDisplayMetrics.widthPixels);
		//Log.i("xxx", "mDisplayMetrics.heightPixels = " + mDisplayMetrics.heightPixels);
	}

	public WindowManager.LayoutParams getLayoutParams()
	{
		return mLayoutParams;
	}

	public Point getScreenSize()
	{
		return new Point(mDisplayMetrics.widthPixels,heightPixels);
	}

	private float getDegreesForRotation(int value)
	{
		switch (value)
		{
		case Surface.ROTATION_90:  
			return 270.0f;

		case Surface.ROTATION_180:
			return 180.0f;

		case Surface.ROTATION_270:
			return 90.0f;

		}
		return 0.0f;
	}

	public Bitmap takeScreenShot()
	{
		Bitmap bmp = null;
		mDisplay.getMetrics(mDisplayMetrics);
		float[] dims = {(float)mDisplayMetrics.widthPixels,(float)heightPixels};
		float degrees = getDegreesForRotation(mDisplay.getRotation());
		boolean requiresRotation = degrees > 0;
		if (requiresRotation){
			mDisplayMatrix.reset();
			mDisplayMatrix.preRotate(-degrees);
			mDisplayMatrix.mapPoints(dims);
			dims[0] = Math.abs(dims[0]);
			dims[1] = Math.abs(dims[1]);

		}
		try
		{
			Class<?> demo = Class.forName("android.view.SurfaceControl");
			Method method = demo.getMethod("screenshot", new Class[] {Integer.TYPE, Integer.TYPE});
			bmp = (Bitmap)method.invoke(demo, new Object[] {Integer.valueOf((int)dims[0]), Integer.valueOf((int)dims[1])});
			if(bmp == null) {
				return null;
			}
			if(requiresRotation) {
				Bitmap ss = Bitmap.createBitmap(mDisplayMetrics.widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(ss);
				c.translate((float)(ss.getWidth() / 2), (float)(ss.getHeight() / 2));
				c.rotate(degrees);
				c.translate((-dims[0] / 2), (-dims[1] / 2));
				c.drawBitmap(bmp, 0, 0, null);
				c.setBitmap(null);
				bmp.recycle();
				bmp = ss;
			}
			if (bmp == null) {

				return null;
			}
			bmp.setHasAlpha(false);
			bmp.prepareToDraw();
			return bmp;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return bmp;
		}

		//return degrees [cmp] 0x0;
	}

	public void startFunnyShot()
	{
		if (mFunnyShotView == null){
			initFunnyShotView();
			mFunnyShotView.setParams(this, mLayoutParams);
			mFunnyShotView.showFunnyShotView();
		}
	}

	public void exitFunnyShot()
	{
		mFunnyShotView.exitFunnyShot();
		mShotFloatViewManager = null; //syc add
	}

	private void initScrollShotView()
	{
		mScrollShotView = new ScrollShotView(mContext);
		mScrollShotView.setScreenSize(mDisplayMetrics.widthPixels, heightPixels);
	}

	public void startScrollShot()
	{
		if (mScrollShotView == null){
			initScrollShotView();
		}
		mScrollShotView.setParams(this, mLayoutParams);
		mScrollShotView.showScrollShotView();
	}

	public void exitScrollShot()
	{
		mScrollShotView.exitScrollShot();
		mScrollShotView = null;
		mShotFloatViewManager = null; //syc fix bug 15051
	}
}