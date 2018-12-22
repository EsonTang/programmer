package com.example.longshotscreen.manager;

import android.content.Context;
import android.util.DisplayMetrics;
import com.example.longshotscreen.utils.Log;
import android.view.WindowManager;
import android.view.Gravity;
import android.graphics.PixelFormat;
import com.example.longshotscreen.ui.MainFloatMenu;
import com.example.longshotscreen.utils.SharedPreferenceUtils;

public class SuperShotMenuManager
{
	private static SuperShotMenuManager mShotMenuManager;
	private Context mContext;
	private WindowManager.LayoutParams mLayoutParams;
	private MainFloatMenu mMainFloatMenu;
	private WindowManager mWindowManager;

	private SuperShotMenuManager(Context paramContext)
	{
		this.mContext = paramContext;
		this.mWindowManager = ((WindowManager)paramContext.getSystemService("window"));
		DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		this.mWindowManager.getDefaultDisplay().getMetrics(localDisplayMetrics);
		initMainFloatMenu();
		initLayoutParams();
	}

	public static SuperShotMenuManager getInstance(Context paramContext)
	{
		if (mShotMenuManager == null)
			mShotMenuManager = new SuperShotMenuManager(paramContext);
		return mShotMenuManager;
	}

	private void initLayoutParams()
	{
		this.mLayoutParams = new WindowManager.LayoutParams();
		this.mLayoutParams.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		this.mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL  
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		this.mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		this.mLayoutParams.x = 0;
		this.mLayoutParams.y = 0;
		this.mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		this.mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		this.mLayoutParams.format = PixelFormat.RGBA_8888;
		this.mLayoutParams.windowAnimations = android.R.style.Animation;
	}

	private void initMainFloatMenu()
	{
		this.mMainFloatMenu = new MainFloatMenu(this.mContext);
	}

	public void addView()
	{
		Log.i("SuperShotMenuManager", "addView");
		Log.i("SuperShotApp", "addView");
		if (this.mMainFloatMenu == null)
			initMainFloatMenu();
		this.mMainFloatMenu.setLayoutParams(this.mLayoutParams);
		this.mWindowManager.addView(this.mMainFloatMenu, this.mLayoutParams);
		String str = SharedPreferenceUtils.getString(this.mContext, "main_menu_coord", "").trim();
		if ("".equals(str)){
			return;
		}
		String[] arrayOfString = str.split(",");
		this.mLayoutParams.x = Integer.valueOf(arrayOfString[0]).intValue();
		this.mLayoutParams.y = Integer.valueOf(arrayOfString[1]).intValue();
		this.mWindowManager.updateViewLayout(this.mMainFloatMenu, this.mLayoutParams);
	}

	public void removeView()
	{
		Log.i("SuperShotMenuManager", "removeView");
		Log.i("SuperShotApp", "removeView");
		if (this.mMainFloatMenu != null)
		{
			this.mWindowManager.removeView(this.mMainFloatMenu);
			this.mMainFloatMenu = null;
		}
	}
}
