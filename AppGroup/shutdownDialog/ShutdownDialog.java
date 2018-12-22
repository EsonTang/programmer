package com.android.server.policy;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import java.util.zip.Inflater;

import com.android.internal.R;

import android.view.WindowManager;
import android.graphics.drawable.Drawable;
import android.view.SurfaceControl;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.animation.AccelerateInterpolator;

import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.content.Intent;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;
import android.view.MotionEvent;
import android.os.Handler;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.Matrix;
import android.widget.RelativeLayout;
/**
 * Created by Administrator on 2017/7/29.
 */

public class ShutdownDialog extends Dialog {
	
	private static final String SHUTDOWN = "windowManagerFuncsShutdown";
	private static final String REBOOT = "windowManagerFuncsReboot";
	

    private static ShutdownDialog myDialog;

	private static Context myContext;
	
	private static LinearLayout shutdownLinearLayout;
    private static LinearLayout rebootLinearLayout;
    private static ImageView shutdown;
    private static ImageView reboot;
	private static TextView shutdownText;
	private static TextView rebootText;
	private static TextView shutdownSummary;
	private static TextView rebootSummary;
    private static boolean onlyShutdown = false;
    private static boolean onlyReboot = false;
    private static View layout;
    private static int shutdownLeft;
    private static int shutdownWidth;
    private static int shutdownTop;
    private static int shutdownHeight;
    private static int rebootLeft;
    private static int rebootWidth;
    private static int rebootTop;
    private static int rebootHeight;
	private static View myLayout;
	
	private static boolean isBigShutdownReturn = false;
 	private static boolean isBigRebootReturn = false;
	private static boolean isTouchBigView = false;
	
	private static LinearLayout cancelLinearLayout;
    private static ImageView cancel;
	private static TextView cancelText;
	private static boolean orientationInLand = false;
	
	private static ImageView imageAnim;
	
    public ShutdownDialog(Context context) {
        super(context);
		this.myContext = context;
    }

    public ShutdownDialog(Context context, int themeResId) {
        super(context, themeResId);
		this.myContext = context;
        onlyShutdown = false;
        onlyReboot = false;

    }

    protected ShutdownDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
		this.myContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);			
    }

    public static class Builder implements View.OnClickListener{
		
        private ShutdownDialog dialog;
        private Context mContext;
        private static int SCALE_VAL = 8;

        public Builder(Context context){ 
			mContext = context;
			
        }

        public ShutdownDialog create(){
            dialog = new ShutdownDialog(mContext, com.prize.internal.R.style.blurDialog); 
			dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_BOOT_PROGRESS);

            LayoutInflater inflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			//layout = inflater.inflate(com.prize.internal.R.layout.prize_shutdown_reboot_dialog, null);
            if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                layout = inflater.inflate(com.prize.internal.R.layout.prize_shutdown_reboot_dialog, null);
				orientationInLand = false;
            }else{
                layout = inflater.inflate(com.prize.internal.R.layout.prize_shutdown_reboot_dialog_land, null);
				orientationInLand = true;
            }
            shutdownLinearLayout = (LinearLayout) layout.findViewById(com.prize.internal.R.id.shutdownLinear);			
            rebootLinearLayout = (LinearLayout) layout.findViewById(com.prize.internal.R.id.rebootLinear);
            shutdown = (ImageView) layout.findViewById(com.prize.internal.R.id.shutdownImage);
            reboot = (ImageView) layout.findViewById(com.prize.internal.R.id.rebootImage);			
            shutdownText = (TextView) layout.findViewById(com.prize.internal.R.id.shutdown_textView);
            rebootText = (TextView) layout.findViewById(com.prize.internal.R.id.reboot_textView);
            shutdownSummary = (TextView) layout.findViewById(com.prize.internal.R.id.shutdown_summary);
            rebootSummary= (TextView) layout.findViewById(com.prize.internal.R.id.reboot_summary);			
			shutdown.setOnClickListener(this);
            reboot.setOnClickListener(this);
			cancelLinearLayout = (LinearLayout) layout.findViewById(com.prize.internal.R.id.cancelLinear);
            cancel = (ImageView) layout.findViewById(com.prize.internal.R.id.cancelImage);
			cancelText = (TextView) layout.findViewById(com.prize.internal.R.id.cancel_textView);
			cancel.setOnClickListener(this);
			imageAnim = (ImageView) layout.findViewById(com.prize.internal.R.id.big_image_anim);
			
			if(GlobalActions.mBitmapPixels > 200){
				shutdownText.setTextColor(mContext.getResources().getColor(com.prize.internal.R.color.prize_black));
				rebootText.setTextColor(mContext.getResources().getColor(com.prize.internal.R.color.prize_black));
				shutdownSummary.setTextColor(mContext.getResources().getColor(com.prize.internal.R.color.prize_black));
				rebootSummary.setTextColor(mContext.getResources().getColor(com.prize.internal.R.color.prize_black));
				cancelText.setTextColor(mContext.getResources().getColor(com.prize.internal.R.color.prize_black));
				cancel.setBackgroundResource(com.prize.internal.R.drawable.prize_cancel_background_black);
			}else{
				shutdownText.setTextColor(mContext.getResources().getColor(com.prize.internal.R.color.prize_white));
				rebootText.setTextColor(mContext.getResources().getColor(com.prize.internal.R.color.prize_white));
                shutdownSummary.setTextColor(mContext.getResources().getColor(com.prize.internal.R.color.prize_white));
				rebootSummary.setTextColor(mContext.getResources().getColor(com.prize.internal.R.color.prize_white));
				cancelText.setTextColor(mContext.getResources().getColor(com.prize.internal.R.color.prize_white));
				cancel.setBackgroundResource(com.prize.internal.R.drawable.prize_cancel_background_white);
			}

			
			dialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            dialog.addContentView(layout, new ViewGroup.
                   LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
	
			
			int systemBrightness = 0;
            try {
                systemBrightness = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
			WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            //lp.screenBrightness = Float.valueOf(systemBrightness / 2 - 30) * (1f / 255f);
            lp.screenBrightness = Float.valueOf(systemBrightness / 6) * (1f / 255f);
            dialog.getWindow().setAttributes(lp);
			
            dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
			
			myDialog = dialog;
			isTouchBigView = false;
            return dialog;
        }

        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case com.prize.internal.R.id.shutdownImage:
				    if(!onlyShutdown && rebootLinearLayout.getVisibility() == View.VISIBLE){
                        onlyShutdown = true;
						shutdownLinearLayout.setVisibility(View.INVISIBLE);
						shutdownText.setTextSize(16f);
						shutdown.setBackgroundResource(com.prize.internal.R.drawable.prize_big_shutdown_background);
                        rebootLinearLayout.clearAnimation();
                        rebootLinearLayout.setVisibility(View.INVISIBLE);
                        shutdownLinearLayout.setVisibility(View.VISIBLE);
			            shutdownText.setVisibility(View.INVISIBLE);

			            TranslateAnimation translateAnimation = new TranslateAnimation(0f, 0f, -42, 225);
                        if(orientationInLand){
							translateAnimation = new TranslateAnimation(0f, 225, -15, 0f);
						}
						ScaleAnimation scaleAnimation = new ScaleAnimation(0.8f, 1f, 0.8f, 1f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
						
                        AnimationSet animationSet = new AnimationSet(true);
                        animationSet.addAnimation(translateAnimation);
                        animationSet.addAnimation(scaleAnimation);
                        animationSet.setDuration(300);
                        animationSet.setFillAfter(true);
                        shutdownLinearLayout.startAnimation(animationSet);
                        animationSet.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                shutdownSummary.setVisibility(View.VISIBLE);
								shutdownText.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
					}
				
                    break;
                case com.prize.internal.R.id.rebootImage:
				    if(!onlyReboot && shutdownLinearLayout.getVisibility() == View.VISIBLE){
                        onlyReboot = true;
						rebootLinearLayout.setVisibility(View.INVISIBLE);
						rebootText.setTextSize(16f);
						reboot.setBackgroundResource(com.prize.internal.R.drawable.prize_big_reboot_background);
                        shutdownLinearLayout.clearAnimation();
                        shutdownLinearLayout.setVisibility(View.INVISIBLE);
                        rebootLinearLayout.setVisibility(View.VISIBLE);
						rebootText.setVisibility(View.INVISIBLE);
						TranslateAnimation translateAnimation2 = new TranslateAnimation(0f, 0f, -38, -228);
						if(orientationInLand){
							translateAnimation2 = new TranslateAnimation(0f, -228, -15, 0f);
						}
                        ScaleAnimation scaleAnimation2 = new ScaleAnimation(0.8f, 1f, 0.8f, 1f,
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        AnimationSet animationSet2 = new AnimationSet(true);
                        animationSet2.addAnimation(translateAnimation2);
                        animationSet2.addAnimation(scaleAnimation2);
                        animationSet2.setDuration(300);
                        animationSet2.setFillAfter(true);
                        rebootLinearLayout.startAnimation(animationSet2);
						animationSet2.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                rebootSummary.setVisibility(View.VISIBLE);
								rebootText.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }
                    break;
					
					case com.prize.internal.R.id.cancelImage:
				        if(onlyShutdown){
							shutdownViewReturn();
						}else if(onlyReboot){
							rebootViewReturn();
						}else{
							if(null != myDialog ){
                                myDialog.dismiss();
					            myDialog = null;
				            }
						}
					break;
            }
        }

    }

	
	
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
		if(isTouchBigView){
			Log.i("dialog","dispatchTouchEvent isTouchBigView");
			return super.dispatchTouchEvent(ev);
		}
        if (isTouchPointInView(shutdown, x, y) || isTouchPointInView(reboot, x, y)) {
			/*if(onlyShutdown && rebootLinearLayout.getVisibility() == View.INVISIBLE){
				if(!isBigShutdownReturn){
 					shutdownViewReturn();
 				}
            }else if(onlyReboot && shutdownLinearLayout.getVisibility() == View.INVISIBLE){
				if(!isBigRebootReturn){
 					rebootViewReturn();
 				}
            }*/
            return super.dispatchTouchEvent(ev);
        }else{
            if(shutdownLinearLayout.getVisibility() == View.VISIBLE && rebootLinearLayout.getVisibility() == View.VISIBLE){
				/*if(null != myDialog ){
                    myDialog.dismiss();
					myDialog = null;
				}*/
            }
	        else if(shutdownLinearLayout.getVisibility() == View.VISIBLE && rebootLinearLayout.getVisibility() != View.VISIBLE){
				
                if(isTouchPointInView(shutdown, x, y - 200)){
		            Log.i("dialog", "ShutdownDialog big_shutdown");
					if(ev.getAction() == MotionEvent.ACTION_DOWN){
						//shutdown.setBackgroundResource(com.prize.internal.R.drawable.prize_shutdown_anim_background);
                    }else if(ev.getAction() == MotionEvent.ACTION_UP){
						//shutdown.setBackgroundResource(com.prize.internal.R.drawable.prize_big_shutdown_background);
						Intent shutdownIntent = new Intent(SHUTDOWN);
                        myContext.sendBroadcast(shutdownIntent);
						isTouchBigView = true;
						//myDialog.dismiss();
					    //myDialog = null;
						startShutdownAnim(true);
					}
                    return super.dispatchTouchEvent(ev);
                }
				if(orientationInLand && isTouchPointInView(shutdown, x - 200, y) ){
					Log.i("dialog", " orientationInLand ShutdownDialog big_shutdown");
					if(ev.getAction() == MotionEvent.ACTION_DOWN){
						//shutdown.setBackgroundResource(com.prize.internal.R.drawable.prize_big_shutdow_pressed);
                    }else if(ev.getAction() == MotionEvent.ACTION_UP){
						//shutdown.setBackgroundResource(com.prize.internal.R.drawable.prize_big_shutdown_background);
						Intent shutdownIntent = new Intent(SHUTDOWN);
                        myContext.sendBroadcast(shutdownIntent);
						isTouchBigView = true;
						//myDialog.dismiss();
					    //myDialog = null;
						startShutdownAnim(false);
					}
                    return super.dispatchTouchEvent(ev);
				}
            }
	        else{
		        if(isTouchPointInView(reboot, x, y + 200)){
		            Log.i("dialog", "ShutdownDialog big_reboot");
					if(ev.getAction() == MotionEvent.ACTION_DOWN){
						//reboot.setBackgroundResource(com.prize.internal.R.drawable.prize_big_reboot_pressed);
                    }else if(ev.getAction() == MotionEvent.ACTION_UP){
						//reboot.setBackgroundResource(com.prize.internal.R.drawable.prize_big_reboot_background);
						Intent rebootIntent = new Intent(REBOOT);
                        myContext.sendBroadcast(rebootIntent);
						isTouchBigView = true;
						//myDialog.dismiss();
					    //myDialog = null;
						startRebootAnim(true);
					}
                    return super.dispatchTouchEvent(ev);
                }
				if(orientationInLand && isTouchPointInView(reboot, x + 200, y)){
					Log.i("dialog", "isTouchPointInView ShutdownDialog big_reboot");
					if(ev.getAction() == MotionEvent.ACTION_DOWN){
						//reboot.setBackgroundResource(com.prize.internal.R.drawable.prize_big_reboot_pressed);
                    }else if(ev.getAction() == MotionEvent.ACTION_UP){
						//reboot.setBackgroundResource(com.prize.internal.R.drawable.prize_big_reboot_background);
						Intent rebootIntent = new Intent(REBOOT);
                        myContext.sendBroadcast(rebootIntent);
						isTouchBigView = true;
						//myDialog.dismiss();
					    //myDialog = null;
						startRebootAnim(false);
					}
                    return super.dispatchTouchEvent(ev);
				}
            }
            return super.dispatchTouchEvent(ev);
		}
    }

	private static void startRebootAnim(boolean isPort){
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(imageAnim.getLayoutParams()); 
        if(isPort){
            lp.setMargins(428, 817, 0, 0);
		}else{
			lp.setMargins(806, 357, 0, 0);
		}
        imageAnim.setLayoutParams(lp);
		imageAnim.setBackgroundResource(com.prize.internal.R.drawable.prize_reboot_anim_background);
		rebootLinearLayout.clearAnimation();
		rebootLinearLayout.setVisibility(View.INVISIBLE);//GONE
		imageAnim.setVisibility(View.VISIBLE);
		AnimationDrawable mAnimationDrawable = (AnimationDrawable) imageAnim.getBackground();
		mAnimationDrawable.start();
	}

	private static void startShutdownAnim(boolean isPort){
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(imageAnim.getLayoutParams()); 
        if(isPort){
            lp.setMargins(428, 765, 0, 0);
		}else{
			lp.setMargins(786, 357, 0, 0);
		}
        imageAnim.setLayoutParams(lp);
		imageAnim.setBackgroundResource(com.prize.internal.R.drawable.prize_shutdown_anim_background);
		shutdownLinearLayout.clearAnimation();
		shutdownLinearLayout.setVisibility(View.INVISIBLE);//GONE
		imageAnim.setVisibility(View.VISIBLE);
		AnimationDrawable mAnimationDrawable = (AnimationDrawable) imageAnim.getBackground();
		mAnimationDrawable.start();
	}

    private boolean isTouchPointInView(View view, int x, int y) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
	    if(onlyShutdown || onlyReboot){
            left = left - 32;
            top = top - 32;
            right = right + 32;
            bottom = bottom + 32;
        }
        if (y >= top && y <= bottom && x >= left
                && x <= right) {
            return true;
        }
        return false;
    }
	
    private static void shutdownViewReturn(){
		isBigShutdownReturn = true;
        onlyShutdown = false;
        shutdownSummary.clearAnimation();
        shutdownSummary.setVisibility(View.INVISIBLE);
        shutdownLinearLayout.setVisibility(View.INVISIBLE);
	    shutdownText.setTextSize(14f);
        shutdown.setBackgroundResource(com.prize.internal.R.drawable.prize_small_shutdown_background);
        shutdownLinearLayout.setVisibility(View.VISIBLE);
	    shutdownText.setVisibility(View.INVISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(0f, 0f, 210, 0f);
		if(orientationInLand){
			translateAnimation = new TranslateAnimation(185, 0f, 10, 0f);
		}
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.2f, 1f, 1.2f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setDuration(300);
        animationSet.setFillAfter(true);
        shutdownLinearLayout.startAnimation(animationSet);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rebootLinearLayout.setVisibility(View.VISIBLE);
                shutdownText.setVisibility(View.VISIBLE);
				isBigShutdownReturn = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private static void rebootViewReturn(){
		isBigRebootReturn = true;
        onlyReboot = false;
        rebootSummary.clearAnimation();
        rebootSummary.setVisibility(View.INVISIBLE);
        rebootLinearLayout.setVisibility(View.INVISIBLE);
		rebootText.setTextSize(14f);
        reboot.setBackgroundResource(com.prize.internal.R.drawable.prize_small_reboot_background);
        rebootLinearLayout.setVisibility(View.VISIBLE);
		rebootText.setVisibility(View.INVISIBLE);
        TranslateAnimation translateAnimation2 = new TranslateAnimation(0f, 0f, -160, 0f);
		if(orientationInLand){
		    translateAnimation2 = new TranslateAnimation(-190, 0f, 10, 0f);
		}
        ScaleAnimation scaleAnimation2 = new ScaleAnimation(1.2f, 1f, 1.2f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AnimationSet animationSet2 = new AnimationSet(true);
        animationSet2.addAnimation(translateAnimation2);
        animationSet2.addAnimation(scaleAnimation2);
        animationSet2.setDuration(300);
        animationSet2.setFillAfter(true);
        rebootLinearLayout.startAnimation(animationSet2);
        animationSet2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                shutdownLinearLayout.setVisibility(View.VISIBLE);
				rebootText.setVisibility(View.VISIBLE);
				isBigRebootReturn = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }	
	
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(null != myDialog){
			Log.i("dialog", "onBackPressed myDialog dismiss");
            myDialog.dismiss();
			myDialog = null;
			onlyShutdown = false;
			onlyReboot = false;
		}
    }

	public static boolean dialogIsShowing(){
		if(myDialog == null){
			return false;
		}
		return true;
	}

	public static void setDialogDismiss(){
		if(myDialog != null){
			myDialog.dismiss();
			myDialog = null;
			isBigShutdownReturn = false;
 		    isBigRebootReturn = false;
			isTouchBigView = false;
			Log.i("globalactions","ShutdownDialog setDialogDismiss");
		}
	}
}
