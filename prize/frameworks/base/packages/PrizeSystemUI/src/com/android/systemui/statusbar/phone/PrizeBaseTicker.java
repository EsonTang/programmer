package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;


/**
 * Created by prize-xiarui on 2018/4/23.
 */

public abstract class PrizeBaseTicker extends FrameLayout{

    public static final String TAG = "PrizeBaseTicker";
    public int mTouchSlop;
    public int mScreenWidth;
    public boolean mDownOutside;
    public StatusBarNotification mNotification;
    public PrizeTickerController mTickerController;

    public PrizeBaseTicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        } else {
            mScreenWidth = mContext.getResources().getDisplayMetrics().heightPixels;
        }
        mTouchSlop = ViewConfiguration.get(context).getScaledWindowTouchSlop();//.getScaledTouchSlop();
        Log.d(TAG, "PrizeBaseTicker orientation = " + orientation + " , mScreenWidth = " + mScreenWidth);
    }

    public void setTickerController(PrizeTickerController controller) {
        mTickerController = controller;
    }

    public void setNotification(StatusBarNotification n) {
        mNotification = n;
    }

    public StatusBarNotification getNotification() {
        return mNotification;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        } else {
            mScreenWidth = mContext.getResources().getDisplayMetrics().heightPixels;
        }
        updateConfigurationChanged(newConfig.orientation);
        Log.d(TAG, "onConfigurationChanged orientation = " + newConfig.orientation + " , mScreenWidth = " + mScreenWidth);
        if (mTickerController != null) {
            mTickerController.updateTickerWidth();
        }
    }

    public int getPanelViewWidth() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        } else {
            mScreenWidth = getResources().getDisplayMetrics().heightPixels;
        }
        Log.d(TAG, "orientation = " + orientation + " , mScreenWidth = " + mScreenWidth);
        return mScreenWidth;
    }

    public boolean isTouchPointInView(View view, int x, int y) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (y >= top && y <= bottom && x >= left && x <= right) {
            return true;
        }
        return false;
    }

    public void updateViewY(float gapY, float origY) {
        float curY = getY();
        float newY = curY + gapY;
        if (newY > origY) {
            newY = origY;
        }
        setY(newY);
    }

    public void resetViewY() {
        setY(0);
    }

    public void restoreDownOutsideFlag(int action) {
        if (MotionEvent.ACTION_UP == action || MotionEvent.ACTION_CANCEL == action) {
            mDownOutside = false;
        }
    }

    public abstract void makeTickerVisible(boolean visible);
    public abstract int getPanelViewHeight();
    public abstract WindowManager.LayoutParams getPanelLayoutParams();
    public abstract boolean checkDownOutside(MotionEvent ev);
    public abstract void updateConfigurationChanged(int orientation);
}
