package com.android.soundrecorder;

import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

/**
 * Created by tangzeming on 2017/8/30.
 */

public class FloatWindowManager {

    /*private static WindowManager mWindowManager = null;
    private static TextView mFloatTextView = null;
    private static LayoutParams mLayoutParams = null;*/
    private static StatusBarManager mStatusBarManager = null;

   /* private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }*/

    private static StatusBarManager getStatusBarManager(Context context) {
        if (null == mStatusBarManager) {
            mStatusBarManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
        }
        return mStatusBarManager;
    }

    public static void createFloatWindow(Context context) {
        /*DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(outMetrics);
        int screenWidth = outMetrics.widthPixels;
        if (null == mFloatTextView) {
            mFloatTextView = new TextView(context);
            mFloatTextView.setTextColor(Color.parseColor("#FFFFFF"));
            mFloatTextView.setTextSize(context.getResources().getDimensionPixelOffset(R.dimen.float_text_size));
            mFloatTextView.setBackgroundResource(R.color.float_bg_color);
            mFloatTextView.setGravity(Gravity.CENTER|Gravity.BOTTOM);
            mFloatTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent("com.android.soundrecorder.SoundRecorder");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
            if (null == mLayoutParams) {
                mLayoutParams = new LayoutParams();
                mLayoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mLayoutParams.format = PixelFormat.RGBA_8888;
                mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP; 
                mLayoutParams.width = screenWidth;
                mLayoutParams.height = context.getResources().getDimensionPixelOffset(R.dimen.float_window_height);
            }
            mFloatTextView.setLayoutParams(mLayoutParams);
            mWindowManager.addView(mFloatTextView, mLayoutParams);
            mFloatTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (null != mFloatTextView){
                    getStatusBarManager(context).setStatusBarBackgroundColor(context.getResources().getColor(R.color.float_bg_color));
                        Intent intent = new Intent("com.android.soundrecorder.SoundRecorder");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getStatusBarManager(context).setStatusBarBackgroundClickEvent(intent);
                    Log.i("tzm","setStatusbarcolor");
                        mFloatTextView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }*/
        getStatusBarManager(context).setStatusBarBackgroundColor(context.getResources().getColor(R.color.float_bg_color));
        Intent intent = new Intent("com.android.soundrecorder.SoundRecorder");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getStatusBarManager(context).setStatusBarBackgroundClickEvent(intent);
    }

    public static void updateFloatWindow(String str ,Context context) {
        /*if (mFloatTextView != null) {
            mFloatTextView.setText(str);
        }*/
        getStatusBarManager(context).setStatusBarText(str);
    }

    public static void removeFloatWindow(Context context) {
        getStatusBarManager(context).clearStatusBarBackgroundColor();
        /*Log.i("tzm","rmwm");
        if (null != mFloatTextView) {
            getWindowManager(context).removeView(mFloatTextView);
            mFloatTextView = null;
        }*/
    }

}
