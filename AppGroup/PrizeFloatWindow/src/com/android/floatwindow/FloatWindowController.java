package com.android.floatwindow;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.media.AudioManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class FloatWindowController {
	
	private static FloatWindowSmallView smallView;
	
	private static LayoutParams smallParams;
	
	private static WindowManager mWindowManager;
	public static Handler mHandler = new Handler();
	private static boolean changeAlpha = true;
	private static AudioManager audioManager;
	public static void startFloatWindowService(final Context context){
		Intent iFWService = new Intent(context,FloatWindowService.class);
		context.startService(iFWService);
		Log.e("test", "creat:"+iFWService);
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				setFloatWindowTranslucent(context);
			}
		}, 5000);
	}
	
	public static void stopFloatWindowService(Context context){
		Intent iFWService = new Intent(context,FloatWindowService.class);
		context.stopService(iFWService);
		removeSmallWindow(context);
		mHandler.removeCallbacksAndMessages(null);
		
	}
	
	/**
	 * Get the WindowManager.
	 * @param context
	 * @return
	 */
	public static WindowManager getWindowManager(Context context){
		if(mWindowManager == null){
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		}
		
		return mWindowManager;
		
	}
	
	public static boolean isWindowShowing(){
		
		return smallView != null;
		
	}
	
//	private static int getWindowWidth(Context context){
//		WindowManager wm = getWindowManager(context);
//		DisplayMetrics outMetrics = new DisplayMetrics();
//		wm.getDefaultDisplay().getMetrics(outMetrics);
//		int screenWidth = outMetrics.widthPixels;
//		return screenWidth;
//		
//	}
	
	/**
	 * Creat a small floating window,the initial position to the upper right corner of the screen. 
	 * @param context
	 */
	public static void creatSmallWindow(Context context){
		creatSmallWindow(context,0,0);
	}
	
	public static void creatSmallWindow(Context context, int x, int y) {
		WindowManager wm = getWindowManager(context);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		int screenWidth = outMetrics.widthPixels;
		int screenHeight = outMetrics.heightPixels;
		if (null == smallView){
			smallView = new FloatWindowSmallView(context);
		   if(null == smallParams){
			smallParams = new LayoutParams();
//			smallParams.type = LayoutParams.TYPE_PHONE;
			smallParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
			smallParams.format = PixelFormat.RGBA_8888;
//			smallParams.flags =  LayoutParams.FLAG_NOT_FOCUSABLE;
			smallParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
			smallParams.gravity = Gravity.LEFT | Gravity.TOP;
			smallParams.width = FloatWindowSmallView.viewWidth;
			smallParams.height = FloatWindowSmallView.viewHeight;
		}
		if (0 ==x && 0 ==y){
			smallParams.x = screenWidth;
			smallParams.y = screenHeight / 20;
		}else {
			smallParams.x = x;
			smallParams.y = y > (smallParams.height/2)? y-smallParams.height/2 : 0;
		}
		smallParams.alpha = 20;
		
		smallView.setLayoutParams(smallParams);
		wm.addView(smallView, smallParams);
		}
	}
	/**
	 *  
	 * @param context
	 */
	public static void removeSmallWindow(Context context) {
		if(null != smallView ){
			WindowManager wm = getWindowManager(context);
			wm.removeView(smallView);
			smallView= null;
		}
	} 

	
	public static void onBackkeyDown(Context context){
		
		if(audioManager == null){
			audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		}
//		try {
//			Runtime runtime = Runtime.getRuntime();
//			runtime.exec("input keyevent"+KeyEvent.KEYCODE_BACK);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		new Thread(){
			public void run() {
				try {
					Log.d("snailRuntime", "------------------onTouchEvent----------ACTION_Up----sendKeyDownUpSync-----");
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
					if (audioManager != null) {
						audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
					} 
				} catch (Exception e) {
					Log.e("Exception when doBack", e.toString());
				}
			}
		}.start();
		
//		Intent intent = new Intent("android.intent.action.ICONKEY_CHANGED");
//		intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
//		intent.putExtra("keycode", KeyEvent.KEYCODE_BACK);
//		if(intent != null){
//			getContext().sendBroadcast(intent);
//	}
//		
		}
	public static void setFloatWindowTranslucent(Context context){
		if(smallView != null  ){
			Log.i("float", "setFloatWindowTranslucent");
			smallParams.alpha = 150;
			WindowManager wm = getWindowManager(context);
			wm.updateViewLayout(smallView, smallParams);
			changeAlpha = false;
		}
	}
	
	public static void setFloatWindowAlpha(Context context){
		if(smallView != null && !changeAlpha){
			Log.i("float", "setFloatWindowAlpha");
			smallParams.alpha = 20;
			WindowManager wm = getWindowManager(context);
			wm.updateViewLayout(smallView, smallParams);
			changeAlpha = true;
		}
	}
		
	}
	
	
	
	
	
	


