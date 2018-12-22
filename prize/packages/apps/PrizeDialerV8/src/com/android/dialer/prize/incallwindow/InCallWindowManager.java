package com.android.dialer.prize.incallwindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dialer.R;
import com.android.incallui.InCallPresenter;

/**
 * Created by wangzhong on 2017/8/19.
 */
public class InCallWindowManager {

    public static final String TAG = "InCallWindow";

    private static WindowManager mWindowManager = null;
    private static TextView mInCallStatusTextView = null;
    private static LayoutParams mInCallStatusLayoutParams = null;

    //private static String mTime = "00:00";

    public static void updateInCallWindow(Context context, long duration) {
        String callTimeElapsed = DateUtils.formatElapsedTime(duration / 1000);
        if (!isInCallWindowVisible()) {
            //createNotificationWindow(context);
            return;
        }
        mInCallStatusTextView.setText(context.getResources().getString(R.string.prize_incallwindow_incall) + callTimeElapsed);
    }

    public static boolean isInCallWindowVisible() {
        return mInCallStatusTextView != null;
    }

    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    public static void createNotificationWindow(final Context context) {
        getWindowManager(context);

        DisplayMetrics outMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(outMetrics);
        int screenWidth = outMetrics.widthPixels;
        //int screenHeight = outMetrics.heightPixels;
        if (null == mInCallStatusTextView) {
            mInCallStatusTextView = new TextView(context);
            mInCallStatusTextView.setText(context.getResources().getString(R.string.prize_incallwindow_outgoing));
            mInCallStatusTextView.setTextColor(Color.parseColor("#FFFFFF"));
            mInCallStatusTextView.setTextSize(context.getResources().getDimensionPixelOffset(R.dimen.prize_incallwindow_text_size));
            mInCallStatusTextView.setBackgroundResource(R.color.prize_incallwindow_bg_color);
            mInCallStatusTextView.setGravity(Gravity.CENTER);
            mInCallStatusTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    goToInCallUI(context);
                }
            });
            mInCallStatusTextView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    InCallWindowPresenter.getInstance().setStatusBarBackground(context);
                    if (null != mInCallStatusTextView && null != mInCallStatusTextView.getViewTreeObserver()) {
                        mInCallStatusTextView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
            if (null == mInCallStatusLayoutParams) {
                mInCallStatusLayoutParams = new LayoutParams();
                mInCallStatusLayoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
                mInCallStatusLayoutParams.format = PixelFormat.RGBA_8888;
                mInCallStatusLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
                mInCallStatusLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                mInCallStatusLayoutParams.width = screenWidth;
                mInCallStatusLayoutParams.height = context.getResources().getDimensionPixelOffset(R.dimen.prize_incallwindow_height);
            }
            mInCallStatusTextView.setLayoutParams(mInCallStatusLayoutParams);
            mWindowManager.addView(mInCallStatusTextView, mInCallStatusLayoutParams);
        }
    }

    public static void removeNotificationWindow(Context context) {
        getWindowManager(context);

        if (null != mInCallStatusTextView) {
            mWindowManager.removeView(mInCallStatusTextView);
            mInCallStatusTextView = null;
        }
    }

    /**
     * To Intent.
     * @param context
     */
    private static void goToInCallUI(Context context) {
        //Toast.makeText(context, "0000", Toast.LENGTH_SHORT).show();
        /*Intent intent = InCallPresenter.getInstance().getInCallIntent(false *//* showDialpad *//*, false *//* newOutgoingCall *//*);
        context.startActivity(intent);*/
        InCallPresenter.getInstance().showInCall(false, false);
    }

}
