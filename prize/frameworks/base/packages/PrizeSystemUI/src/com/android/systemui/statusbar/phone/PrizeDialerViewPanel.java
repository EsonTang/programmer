package com.android.systemui.statusbar.phone;

import android.app.Instrumentation;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.R;

/**
 * Created by prize-xiarui on 2018/4/11.
 */

public class PrizeDialerViewPanel extends PrizeBaseTicker implements View.OnClickListener  {

    private static final boolean DEBUG = true;
    private static final String TAG = PrizeBaseTicker.TAG + "_Dialer";
    private Context mContext;
    public View mTickerTopView;
    public ImageButton mDesclineBtn;
    public ImageButton mAcceptBtn;
    public TextView mIncomingCallNumber;
    public TextView mIncomingCallLocation;
    public LinearLayout mTickerContentLayout;
    public LinearLayout mTickerInternalContent;
    private PendingIntent mContentIntent;

    public PrizeDialerViewPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        //mScreenWidth = getResources().getDisplayMetrics().widthPixels;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d(TAG, "PrizeDialerViewPanel onFinishInflate");
        mTickerContentLayout = (LinearLayout) findViewById(R.id.ticker_content);
        mTickerInternalContent = (LinearLayout) findViewById(R.id.dialer_internal_content);
        mTickerTopView = findViewById(R.id.ticker_top_view);
        mDesclineBtn = (ImageButton) findViewById(R.id.dialer_descline_button);
        mAcceptBtn = (ImageButton) findViewById(R.id.dialer_accept_button);
        mIncomingCallNumber = (TextView) findViewById(R.id.dialer_number);
        mIncomingCallLocation = (TextView) findViewById(R.id.dialer_location);
        mDesclineBtn.setOnClickListener(this);
        mAcceptBtn.setOnClickListener(this);

        int orientation = getResources().getConfiguration().orientation;
        updateConfigurationChanged(orientation);
    }

    public void setIncomingCallNumberLocation(String number, String location) {
        mIncomingCallNumber.setText(number);
        if (location == null) {
            location = mContext.getResources().getString(com.android.internal.R.string.unknownName);
        }
        mIncomingCallLocation.setText(location);
    }

    public void setContentIntent(PendingIntent contentIntent) {
        this.mContentIntent = contentIntent;
    }

    public static void simulateKeystroke(final int KeyCode) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    Log.d(TAG, "simulateKeystroke, KeyCode = " + KeyCode);
                    inst.sendKeyDownUpSync(KeyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialer_descline_button:
                Log.d(TAG, "-----click call end-----");
                simulateKeystroke(KeyEvent.KEYCODE_ENDCALL);
                if (mTickerController != null) {
                    mTickerController.animateTickerCollapse();
                }
                break;
            case R.id.dialer_accept_button:
                Log.d(TAG, "-----click accept call-----");
                simulateKeystroke(KeyEvent.KEYCODE_CALL);
                if (mContentIntent != null) {
                    try {
                        Log.i(TAG, "KEYCODE_CALL Sending contentIntent success");
                        mContentIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        Log.i(TAG, "KEYCODE_CALL Sending contentIntent failed");
                    }
                }
                if (mTickerController != null) {
                    mTickerController.animateTickerCollapse();
                }
                break;
        }
    }

    private float mOrigDownY;
    private float mOrigDownX;
    private float mCurDownX;
    private float mCurDownY;
    private float mOrigY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (mDownOutside) {
            restoreDownOutsideFlag(action);
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                //Log.d(TAG, "ACTION_DOWN");
                mOrigY = getY();
                mOrigDownX = mCurDownX = event.getRawX();
                mOrigDownY = mCurDownY = event.getRawY();
            }
                break;
            case MotionEvent.ACTION_MOVE: {
                //Log.d(TAG, "ACTION_MOVE");
                float gapX = event.getRawX() - mCurDownX;
                float gapY = event.getRawY() - mCurDownY;
                int xDiff = (int) (event.getRawX() - mOrigDownX);
                int yDiff = (int) (event.getRawY() - mOrigDownY);
                if (yDiff < 0 && Math.abs(yDiff) > /*this.getMeasuredHeight() / 2*/mTouchSlop) {
                    if (mTickerController != null) {
                        mTickerController.animateTickerCollapse();
                    }
                }
                if (Math.abs(gapY) > 0 && Math.abs(gapY) > Math.abs(gapX)) {
                    mCurDownX = event.getRawX();
                    mCurDownY = event.getRawY();
                    updateViewY(gapY, mOrigY);
                }
            }
                break;
            case MotionEvent.ACTION_UP: {
                //Log.d(TAG, "ACTION_UP");
                int xDiff = (int) (event.getRawX() - mOrigDownX);
                int yDiff = (int) (event.getRawY() - mOrigDownY);
                if (Math.abs(yDiff) <= mTouchSlop && Math.hypot((double) Math.abs(yDiff), (double) Math.abs(xDiff)) < ((double) mTouchSlop)) {
                    if (mContentIntent != null) {
                        try {
                            Log.i(TAG, "Sending contentIntent success");
                            mContentIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            Log.i(TAG, "Sending contentIntent failed");
                        }
                    }
                    if (mTickerController != null) {
                        mTickerController.animateTickerCollapse();
                    }
                } else if (yDiff < 0 && Math.abs(yDiff) > /*this.getMeasuredHeight() / 2*/mTouchSlop) {
                    if (mTickerController != null) {
                        mTickerController.animateTickerCollapse();
                    }
                } else {
                    resetViewY();
                }
            }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        checkDownOutside(ev);
        int action = ev.getActionMasked();
        if (mDownOutside) {
            restoreDownOutsideFlag(action);
            return false;
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                //Log.d(TAG, "onInterceptTouchEvent ACTION_DOWN");
                mTickerController.removeCollapseMessage();
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                //Log.d(TAG, "onInterceptTouchEvent ACTION_UP");
                break;
        }
        return true;
    }

    @Override
    public boolean checkDownOutside(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            int rawX = (int) ev.getRawX();
            int raxY = (int) ev.getRawY();
            //if (rawX < 0 || rawX > mScreenWidth || raxY < mTickerTopView.getMeasuredHeight() ||
            //        isTouchPointInView(mAcceptBtn, rawX, raxY) || isTouchPointInView(mDesclineBtn, rawX, raxY)) {
            if (!isTouchPointInView(mTickerInternalContent, rawX, raxY) || isTouchPointInView(mAcceptBtn, rawX, raxY) || isTouchPointInView(mDesclineBtn, rawX, raxY)) {
                mDownOutside = true;
            } else {
                mDownOutside = false;
            }
        }
        Log.d(TAG, "checkDownOutside mDownOutside = " + mDownOutside);
        return mDownOutside;
    }

    public void setTickerController(PrizeTickerController controller) {
        mTickerController = controller;
    }

    @Override
    public WindowManager.LayoutParams getPanelLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                mScreenWidth, getPanelViewHeight(),
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL, WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        lp.gravity = Gravity.TOP|Gravity.CENTER;
        lp.windowAnimations = R.style.PrizeTickerWindowAnimation;
        lp.setTitle("TickerPanel");
        return lp;
    }

    @Override
    public int getPanelViewHeight() {
        LinearLayout content = (LinearLayout) findViewById(R.id.ticker_content);
        content.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
        int viewHeight = content.getMeasuredHeight();
        Log.d(TAG, "dialer view height = " + viewHeight);
        return viewHeight;
    }

    @Override
    public void makeTickerVisible(boolean visible) {
        if (DEBUG) {
            Log.i(TAG, "makeDialerVisible  visible = " + visible + "  mDialerViewVisible = " + getVisibility());
        }
        if (visible) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
            setNotification(null);
        }
    }

    @Override
    public void updateConfigurationChanged(int orientation) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mTickerTopView != null) mTickerTopView.setVisibility(View.VISIBLE);
        } else {
            if (mTickerTopView != null) mTickerTopView.setVisibility(View.GONE);
        }
    }
}
