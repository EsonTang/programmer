package com.android.systemui.statusbar.phone;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.service.notification.StatusBarNotification;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.RemoteInputController;

/**
 * Created by prize-xiarui on 2018/4/11.
 */

public class PrizeTickerController implements RemoteInputController.Callback {

    private static final String TAG = "PrizeTickerController";
    private static final boolean DEBUG = true;
    private static final int COLLAPSE = 1;
    private static final int DELAY = 5000;
    private Context mContext;
    private PhoneStatusBar mStatusBar;
    private WindowManager mWindowManager;
    private TelephonyManager mTelephonyManager;
    public PrizeTickerViewPanel mTickerView;
    public PrizeDialerViewPanel mDialerView;

    private static final String KEY_CONTACT_TITLE = "contact_title";
    private static final String KEY_CONTACT_LOCATION = "contact_location";

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case COLLAPSE:
                    animateTickerCollapse();
                    break;
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.v(TAG, "onReceive: " + intent);
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                makeTickerVisible(false);
            }
        }
    };

    public PrizeTickerController(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void registerBroadcastReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);//set the priority high
        context.registerReceiverAsUser(mBroadcastReceiver, UserHandle.ALL, filter, null, null);
    }

    public void unRegisterBroadcastReceiver(Context context) {
        context.unregisterReceiver(mBroadcastReceiver);
    }

    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String number) {
            super.onCallStateChanged(state, number);
            Log.d(TAG, "state = " + state + "   number = " + number);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "hang up");
                    animateTickerCollapse();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "call ringing, number = "+ number);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "answer the phone");
                    animateTickerCollapse();
                    break;
                default:
                    break;
            }
        };
    };

    public boolean removeTickerViewIfNeed(String key) {
        StatusBarNotification notification = null;
        //prize add by xiarui for bug63242\bug65189 2018-08-11 @{
        if (mTickerView != null) {
            notification = mTickerView.getNotification();
        } else if (mDialerView != null) {
            notification = mDialerView.getNotification();
        }
        if (notification != null && notification.getKey().equals(key)) {
            Log.d(TAG, "removeTickerViewIfNeed true");
            return true;
        }
        //@}
        Log.d(TAG, "removeTickerViewIfNeed false");
        return false;
    }

    public void setStatusBar(PhoneStatusBar statusBar) {
        mStatusBar = statusBar;
    }

    public void createTickerWindow() {
        if (mTickerView == null) {
            mTickerView = (PrizeTickerViewPanel) View.inflate(mContext, R.layout.prize_msg_ticker, null);
            mTickerView.setTickerController(this);
            LayoutParams lp = mTickerView.getPanelLayoutParams();
            mWindowManager.addView(mTickerView, lp);
        }
    }

    public void createDialerWindow() {
        if (mDialerView == null) {
            mDialerView = (PrizeDialerViewPanel) View.inflate(mContext, R.layout.prize_dialer_ticker, null);
            mDialerView.setTickerController(this);
            LayoutParams lp = mDialerView.getPanelLayoutParams();
            mWindowManager.addView(mDialerView, lp);
        }
    }

    public void updateTickerWindow(boolean hasRemoteInput) {
        LayoutParams lp = mTickerView.getPanelLayoutParams();
        if (hasRemoteInput) {
            lp.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            lp.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        } else {
            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            lp.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        }
        mWindowManager.updateViewLayout(mTickerView, lp);
    }

    public void refreshTickerWindow() {
        if (DEBUG) {
            Log.d(TAG, "refreshTickerWindow_mTickerVisible = " + mTickerView.getTickerVisible());
        }
        if (mTickerView.getTickerVisible()) {
            animateTickerCollapse();
        }
        if (mTickerView != null) {
            mWindowManager.removeView(mTickerView);
            mTickerView = null;
        }
        mTickerView = (PrizeTickerViewPanel) View.inflate(mContext, R.layout.prize_msg_ticker, null);
        LayoutParams lp = mTickerView.getPanelLayoutParams();
        mTickerView.setVisibility(View.GONE);
        mWindowManager.addView(mTickerView, lp);
        mTickerView.setTickerController(this);
    }

    public void updateTickerWidth() {
        if (mTickerView != null) {
            Log.d(TAG, "updateTickerWidth() called ticker");
            mWindowManager.updateViewLayout(mTickerView, mTickerView.getPanelLayoutParams());
        }
        if (mDialerView != null) {
            Log.d(TAG, "updateTickerWidth() called dialer");
            mWindowManager.updateViewLayout(mDialerView, mDialerView.getPanelLayoutParams());
        }
    }

    public void animateTickerCollapse() {
        Log.d(TAG, "animateTickerCollapse_TickerView = " + mTickerView);
        if (hasCollapseMessage()) {
            removeCollapseMessage();
        }
        if (mTickerView != null) {
            removeTickerView();
        }
        if (mDialerView != null) {
            removeDialerView();
        }
    }

    public void makeTickerVisible(boolean visible) {

        if (mTickerView != null) {
            mTickerView.makeTickerVisible(visible);
        } else {
            Log.w(TAG, "ticker view is not init");
        }

        if (mDialerView != null) {
            mDialerView.makeTickerVisible(visible);
        } else {
            Log.w(TAG, "dialer view is not init");
        }
    }

    public void removeCollapseMessage() {
        mHandler.removeMessages(COLLAPSE);
    }

    public boolean hasCollapseMessage() {
        if (mHandler == null || !mHandler.hasMessages(COLLAPSE)) {
            return false;
        }
        if (!DEBUG) {
            return true;
        }
        Log.d(TAG, "has COLLAPSE Message!");
        return true;
    }

    public void collapseMessage() {
        removeCollapseMessage();
        mHandler.sendEmptyMessageDelayed(COLLAPSE, DELAY);
    }

    public OnClickListener makeClicker(PendingIntent contentIntent, String key) {
        if (mStatusBar != null) {
            return mStatusBar.makeClicker(contentIntent, key);
        }
        return null;
    }

    public void showTickerView() {
        if (mTickerView == null) {
            createTickerWindow();
        }
        mTickerView.setVisibility(View.VISIBLE);
    }

    public void removeTickerView() {
        if (mTickerView != null) {
            mWindowManager.removeView(mTickerView);
            mTickerView = null;
        }
    }

    public void showDialerView() {
        if (mDialerView == null) {
            createDialerWindow();
        }
        mDialerView.setVisibility(View.VISIBLE);
    }

    public void removeDialerView() {
        if (mDialerView != null) {
            mWindowManager.removeView(mDialerView);
            mDialerView = null;
        }
    }

    public void showTickerView(Entry entry, RemoteInputController r) {
        if (TelephonyManager.getDefault().getCallState() == TelephonyManager.CALL_STATE_RINGING
                && "com.android.dialer".equals(entry.notification.getPackageName())) {
            if (hasCollapseMessage()) {
                removeCollapseMessage();
            }
            removeTickerView();
            showDialerView();
            makeTickerVisible(true);
            PendingIntent contentIntent = entry.notification.getNotification().contentIntent;
            String contactTitle = entry.notification.getNotification().extras.getString(KEY_CONTACT_TITLE);
            String contactLocation = entry.notification.getNotification().extras.getString(KEY_CONTACT_LOCATION);
            Log.d(TAG, "contactTitle = " + contactTitle + " , contactLocation = " + contactLocation);
            if (mDialerView != null) {
                //prize add by xiarui for bug63242\bug65189 2018-08-11 @{
                mDialerView.setNotification(entry.notification);
                //@}
                mDialerView.setContentIntent(contentIntent);
                mDialerView.setIncomingCallNumberLocation(contactTitle, contactLocation);
            }
            Log.d(TAG, "show incoming call view");
        } else {
            if (mDialerView == null) {
                if (hasCollapseMessage()) {
                    removeCollapseMessage();
                }
                //removeDialerView();
                showTickerView();
                makeTickerVisible(true);
                mTickerView.setTickerInfo(entry, r);
                Log.d(TAG, "show ticker view");
            } else {
                Log.d(TAG, "dialer view is showing, interrupt ticker view show");
            }
        }
    }

    @Override
    public void onRemoteInputActive(boolean active) {
        if (DEBUG) {
            Log.i(TAG, "onRemoteInputActive active " + active);
        }
        if (active) {
            if (mTickerView != null) {
                mTickerView.updateActionVisibleForRemoteInputClick(true);
            }
            removeCollapseMessage();
            return;
        }
        if (mTickerView != null) {
            mTickerView.updateActionVisibleForRemoteInputClick(false);
        }
        collapseMessage();
    }

    @Override
    public void onRemoteInputSent(Entry entry) {
        if (DEBUG) {
            Log.i(TAG, "onRemoteInputSent --- ");
        }
        animateTickerCollapse();
    }
}
