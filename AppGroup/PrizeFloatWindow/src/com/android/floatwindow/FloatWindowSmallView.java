package com.android.floatwindow;

import java.lang.reflect.Field;

import com.android.floatwindow.R;

import android.R.integer;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FloatWindowSmallView extends LinearLayout {


	public static final int STATE_NORMAL = 1;
	public static final int STATE_PRESSED = 2;
	
	public static int viewWidth ;
	public static int viewHeight ;
	
	private static int mStatusBarHeight;
	
	private TextView tv_start_cm;
	
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;
	
	private float xCurrentInScreen;
	private float yCurrentInScreen;
	
	private float xDownInScreen;
	private float yDownInScreen;
	
	private float xInView;
	private float yInView;
	
//	private Vibrator mVibrator;
//	private boolean isLongPress = false;


	public void setLayoutParams(WindowManager.LayoutParams layoutParams){
		this.mLayoutParams = layoutParams;
	}
	
	public WindowManager.LayoutParams getLayoutParams(){
		return mLayoutParams;
	}

	public FloatWindowSmallView(Context context) {
		super(context);

		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater.from(context).inflate(R.layout.float_window_view_small, this);

		viewWidth = getResources().getDimensionPixelOffset(R.dimen.small_width);
		viewHeight = getResources().getDimensionPixelOffset(R.dimen.small_height);

		tv_start_cm = (TextView) findViewById(R.id.tv_start_cm);
//		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}

//	private Handler mHandler = new Handler();
//
//	private Runnable mLongClickRunnable = new Runnable(){
//
//		public void run() {
//			isLongPress = true;
//			mVibrator.vibrate(50);
//		};
//	};

//	@Override
//	public boolean dispatchTouchEvent(MotionEvent event) {
//		switch (event.getAction()) {
//		case MotionEvent.ACTION_DOWN:
//			setBackgroundState(STATE_PRESSED);
//			xInView = event.getX();
//			yInView = event.getY();
//
//			xDownInScreen = event.getRawX();
//			yDownInScreen = event.getRawY() - getStatusBarHeight();
//
//
//			xCurrentInScreen = event.getRawX();
//			yCurrentInScreen = event.getRawY() - getStatusBarHeight();
//
//
//			mHandler.postDelayed(mLongClickRunnable, 700);
//
//
//			break;
//
//		case MotionEvent.ACTION_MOVE:
//			xCurrentInScreen = event.getRawX();
//			yCurrentInScreen = event.getRawY() - getStatusBarHeight();
//
//
//			if(Math.abs(xDownInScreen-xCurrentInScreen)>20 || Math.abs(yDownInScreen-yCurrentInScreen)>20){
//				mHandler.removeCallbacks(mLongClickRunnable);
//			}
//
//			break;
//
//		case MotionEvent.ACTION_UP:
//			setBackgroundState(STATE_NORMAL);
//
//			mHandler.removeCallbacks(mLongClickRunnable);
//			if(Math.abs(xDownInScreen-xCurrentInScreen)<9 && Math.abs(yDownInScreen-yCurrentInScreen)<9 && isLongPress == false){
//
//				FloatWindowController.onBackkeyDown(getContext());
//
//			}
//			updateViewWithY();
//			isLongPress = false;
//
//			break;
//
//		default:
//			break;
//		}
//
//		return super.dispatchTouchEvent(event);
//	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		if(isLongPress == true){
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			FloatWindowController.setFloatWindowAlpha(getContext());
			FloatWindowController.mHandler.removeCallbacksAndMessages(null);
			setBackgroundState(STATE_PRESSED);
			
			xInView = event.getX();
			yInView = event.getY();

			xDownInScreen = event.getRawX();
			yDownInScreen = event.getRawY() - getStatusBarHeight();

			xCurrentInScreen = event.getRawX();
			yCurrentInScreen = event.getRawY() - getStatusBarHeight();

			break;

		case MotionEvent.ACTION_MOVE:
			xCurrentInScreen = event.getRawX();
			yCurrentInScreen = event.getRawY() - getStatusBarHeight();
			updateViewPosition();
			break;
			
		case MotionEvent.ACTION_UP:
			setBackgroundState(STATE_NORMAL);

			if(Math.abs(xDownInScreen-xCurrentInScreen)<9 && Math.abs(yDownInScreen-yCurrentInScreen)<9){

				FloatWindowController.onBackkeyDown(getContext());

			}else{
			updateViewWithY();
			}
			FloatWindowController.mHandler.postDelayed(setTranslucent, 5000);
			break;
		default:
			break;
		}

		return true;
		}
//		return super.onTouchEvent(event);
//	}


	private void setBackgroundState(int state) {
		if(null != tv_start_cm){
			switch (state) {
			case STATE_NORMAL:
				tv_start_cm.setBackgroundResource(R.drawable.ic_launcher);
				break;
			case STATE_PRESSED:
				tv_start_cm.setBackgroundResource(R.drawable.btn_float_pressed);
				break;
			
			default:
				break;
			}
		}
		
	}
	
	/**
	 * No matter how the fingers move, in the end, small floating window on the Y axis the border.
	 */
	private void updateViewWithY() {
		WindowManager wm = FloatWindowController.getWindowManager(getContext());
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		int screenWidth = outMetrics.widthPixels;
		mLayoutParams.x = (mLayoutParams.x)>((screenWidth - getWidth())/2)?screenWidth:0;
		mWindowManager.updateViewLayout(this, mLayoutParams);
		
	}

	private void updateViewPosition() {
		mLayoutParams.x = (int) (xCurrentInScreen-xInView);
		mLayoutParams.y = (int) (yCurrentInScreen-yInView);
		mWindowManager.updateViewLayout(this, mLayoutParams);
		
	}

	/**
	 * Get the height of the status bar.
	 * @return Returns the status bar height pixel values.
	 */
	private float getStatusBarHeight() {
		if(mStatusBarHeight == 0){
			try{
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field f = c.getField("status_bar_height");
				int x = (Integer) f.get(o);
				mStatusBarHeight = getResources().getDimensionPixelSize(x);
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return mStatusBarHeight;
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateViewWithY();
		
	}
	
	Runnable setTranslucent = new Runnable() {
		
		@Override
		public void run() {
			FloatWindowController.setFloatWindowTranslucent(getContext());
		}
	};
	
	
	
}
