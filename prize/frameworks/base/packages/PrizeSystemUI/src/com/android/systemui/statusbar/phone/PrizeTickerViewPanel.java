package com.android.systemui.statusbar.phone;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DateTimeView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.PrizeNotificaionIconUtil;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.policy.RemoteInputView;

/**
 * Created by prize-xiarui on 2018/4/11.
 */

public class PrizeTickerViewPanel extends PrizeBaseTicker {

    private static final boolean DEBUG = true;
    private static final String TAG = PrizeBaseTicker.TAG + "_Ticker";
    private Context mContext;
    private TextView mAdditional;
    private View mAdditionalDivider;
    public TextView mTickerAction1;
    public TextView mTickerAction2;
    public TextView mTickerAction3;
    public LinearLayout mTickerTail;
    public FrameLayout mTickerTailContainer;
    public View mTickerTopView;
    public ImageView mTickerAppIcon;
    public TextView mTickerAppName;
    public TextView mTickerContent;
    public LinearLayout mTickerContentLayout;
    private LinearLayout mTickerHeaderAndContent;
    public DateTimeView mTickerDate;
    public ImageView mTickerRightIcon;
    public TextView mTickerTitle;
    private boolean mDownOutside;
    private RemoteInputView mRemoteInputView = null;
    private static final int MAX_ACTIONS = 3;

    public PrizeTickerViewPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d(TAG, "PrizeTickerViewPanel onFinishInflate");
        mTickerHeaderAndContent = (LinearLayout) findViewById(R.id.ticker_header_and_content);
        mTickerContentLayout = (LinearLayout) findViewById(R.id.ticker_content);
        mTickerTopView = findViewById(R.id.ticker_top_view);
        mTickerAppIcon = (ImageView) findViewById(R.id.ticker_app_icon);
        mTickerAppName = (TextView) findViewById(R.id.ticker_app_name);
        mTickerDate = (DateTimeView) findViewById(R.id.ticker_ticker_date);
        mTickerTitle = (TextView) findViewById(R.id.ticker_title);
        mTickerContent = (TextView) findViewById(R.id.ticker_summary);
        mTickerRightIcon = (ImageView) findViewById(R.id.ticker_large_icon);
        mTickerTailContainer = (FrameLayout) findViewById(R.id.ticker_tail_container);
        mTickerTail = (LinearLayout) findViewById(R.id.ticker_tail);
        mTickerAction1 = (TextView) findViewById(R.id.ticker_action1);
        mTickerAction2 = (TextView) findViewById(R.id.ticker_action2);
        mTickerAction3 = (TextView) findViewById(R.id.ticker_action3);
        mAdditionalDivider = findViewById(R.id.additional_divider);
        mAdditional = (TextView) findViewById(R.id.additional);

        int orientation = getResources().getConfiguration().orientation;
        updateConfigurationChanged(orientation);
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
                    try {
                        Notification.Action[] actions = mNotification.getNotification().actions;
                        if (actions != null) {
                            mNotification.getNotification().actions[0].actionIntent.send();
                        }
                    } catch (PendingIntent.CanceledException e) {
                        Log.d(TAG, "send intent failed");
                    }
                    //mTickerController.removeCollapseMessage();
                    mTickerController.animateTickerCollapse();
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
                    performClick();
                    //mTickerController.removeCollapseMessage();
                    mTickerController.animateTickerCollapse();
                } else if (yDiff < 0 && Math.abs(yDiff) > /*this.getMeasuredHeight() / 2*/mTouchSlop) {
                    try {
                        Notification.Action[] actions = mNotification.getNotification().actions;
                        if (actions != null) {
                            mNotification.getNotification().actions[0].actionIntent.send();
                        }
                    } catch (PendingIntent.CanceledException e) {

                    }
                    //mTickerController.removeCollapseMessage();
                    mTickerController.animateTickerCollapse();
                } else {
                    resetViewY();
                    mTickerController.collapseMessage();
                }
            }
            break;
            default:
                break;
        }
        return true;
    }

//    private float mFirstDownY;
//    private float mFirstDownX;
//    private float mOrigTranslationY;
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getActionMasked()) {
//            case MotionEvent.ACTION_DOWN:
//                Log.d(TAG, "ACTION_DOWN");
//                mFirstDownY = event.getRawY();
//                mFirstDownX = event.getRawX();
//                mOrigTranslationY = this.getTranslationY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                Log.d(TAG, "ACTION_MOVE");
//                float gapY = event.getRawY() - mFirstDownY;
//                float gapX = event.getRawX() - mFirstDownX;
//                if (gapY < 0 && Math.abs(gapY) > Math.abs(gapX) && Math.abs(gapY) > mTouchSlop * 3) {
//                    try {
//                        Notification.Action[] actions = mNotification.getNotification().actions;
//                        if (actions != null) {
//                            mNotification.getNotification().actions[0].actionIntent.send();
//                        }
//                    } catch (PendingIntent.CanceledException e) {
//
//                    }
//                    //mTickerController.removeCollapseMessage();
//                    mTickerController.animateTickerCollapse();
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                Log.d(TAG, "ACTION_UP");
//                int yDiff = (int) Math.abs(event.getRawY() - mFirstDownY);
//                int xDiff = (int) Math.abs(event.getRawX() - mFirstDownX);
//                if (yDiff <= mTouchSlop && Math.hypot((double) yDiff, (double) xDiff) < ((double) mTouchSlop)) {
//                    performClick();
//                    //mTickerController.removeCollapseMessage();
//                    mTickerController.animateTickerCollapse();
//                }
//                break;
//            default:
//                break;
//        }
//        return true;
//    }

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
                Log.d(TAG, "onInterceptTouchEvent ACTION_DOWN");
                mTickerController.removeCollapseMessage();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onInterceptTouchEvent ACTION_UP");
                break;
        }
        return true;
    }

    public boolean getTickerVisible() {
        return getVisibility() == View.VISIBLE;
    }

    @Override
    public boolean checkDownOutside(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            int rawX = (int) ev.getRawX();
            int raxY = (int) ev.getRawY();
            Log.d(TAG, "rawX = " + rawX + " , raxY = " + raxY + " , topH = " + mTickerTopView.getMeasuredHeight());
            //if (rawX < 0 || rawX > mScreenWidth || raxY < mTickerTopView.getMeasuredHeight() || isTouchPointInView(mTickerTailContainer, rawX, raxY)) {
            if (!isTouchPointInView(mTickerHeaderAndContent, rawX, raxY)) {
                mDownOutside = true;
            } else {
                mDownOutside = false;
            }
        }
        Log.d(TAG, "checkDownOutside mDownOutside = " + mDownOutside);
        return mDownOutside;
    }

    public void setTickerContentText(String text) {
        mTickerContent.setText(text);
    }

    public boolean updateAdditionalButton() {
        if (showCopyVerifyCode()) {
            Log.i(TAG, "show VCode");
            mAdditionalDivider.setVisibility(View.VISIBLE);
            mAdditional.setVisibility(View.VISIBLE);
            mAdditional.setText(R.string.copy_verify_code);
            mAdditional.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    copyVerifyCodeToClip();
                }
            });
            mTickerRightIcon.setVisibility(View.GONE);
        } else {
            if (DEBUG) {
                Log.i(TAG, "normal");
            }
            mAdditionalDivider.setVisibility(View.GONE);
            mAdditional.setVisibility(View.GONE);
            mTickerRightIcon.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    private void copyVerifyCodeToClip() {
        String vCode = "123456";
        Log.w(TAG, "copy VCode:" + vCode + " to clipboard");
        ((ClipboardManager) mContext.getSystemService(NavigationBarInflaterView.CLIPBOARD)).setPrimaryClip(ClipData.newPlainText("Verify Code", vCode));
    }

    private boolean showCopyVerifyCode() {
        return false;
    }

    public void setTickerInfo(NotificationData.Entry entry, RemoteInputController r) {

        if (mRemoteInputView != null) {
            mRemoteInputView.onNotificationUpdateOrReset();
            mRemoteInputView.setVisibility(View.GONE);
        }
        StatusBarNotification n = entry.notification;
        String pkg = n.getPackageName();
        Notification notification = n.getNotification();
        if (notification == null) {
            Log.d(TAG, "notification is null, so return!");
            return;
        }
        String appName = getApplicationName(pkg);
        setNotification(n);
        boolean hasAdditionalButton = updateAdditionalButton();

        if (DEBUG) {
            Log.d(TAG, "setTickerContent_pkg = " + pkg + ", appName = " + appName);
        }
        Drawable smallIcon = notification.getSmallIcon().loadDrawable(mContext);
        if (DEBUG) {
            Log.d(TAG, "setTickerContent_smallIconId = " + smallIcon);
        }
        CharSequence tickerText = notification.tickerText;
        if (smallIcon == null) {
            smallIcon = getIconBitmap(mContext, new StatusBarIcon(n.getPackageName(), n.getUser(), notification.icon, notification.iconLevel, 0, tickerText));
            if (DEBUG) {
                Log.d(TAG, "setTickerContent_largeIcon is null, and smallIcon = " + smallIcon);
            }
        }
        long when = notification.when;
        Notification.Action[] actions = notification.actions;
        int length = actions != null ? actions.length : 0;
        if (length > MAX_ACTIONS) {
            length = MAX_ACTIONS;
        }
        mTickerAppIcon.setImageDrawable(smallIcon);

        PrizeNotificaionIconUtil.replaceNotificationIcon(mContext, entry, this, R.id.ticker_app_icon, true);

        mTickerAppName.setText(appName);
        if (notification.showsTime()) {
            mTickerDate.setTime(when);
            mTickerDate.setVisibility(View.VISIBLE);
        } else {
            mTickerDate.setVisibility(View.GONE);
        }
        boolean needEncrypt = needEncrypt(pkg);
        if (!hasAdditionalButton) {
            populateLargeIcon(notification, needEncrypt);
        }
        setTickerTitle(pkg, notification, needEncrypt);
        setTickerSummary(notification, tickerText, needEncrypt);
        populateActions(entry, r, actions, length, needEncrypt);
        mTickerController.updateTickerWindow(false);
        PendingIntent contentIntent = notification.contentIntent;
        if (contentIntent != null) {
            OnClickListener listener = mTickerController.makeClicker(contentIntent, n.getKey());
            setClickable(true);
            setOnClickListener(listener);
        } else {
            setOnClickListener(null);
            setClickable(false);
        }
        if (DEBUG) {
            Log.i(TAG, "setTickerContent mTickerVisible = " + getTickerVisible());
        }

        if (noAutoCollapse(notification) && !needAutoCollapse(pkg)) {
            Log.i(TAG, "noAutoCollapse");
            mTickerController.removeCollapseMessage();
        } else {
            Log.i(TAG, "AutoCollapse");
            mTickerController.collapseMessage();
        }
        if (getScrollX() != 0) {
            scrollTo(0, 0);
        }
    }

    private void populateActions(NotificationData.Entry entry, RemoteInputController r, Notification.Action[] actions, int length, boolean needEncrypt) {
        boolean showActionButton;
        TextView[] textViews = new TextView[]{mTickerAction1, mTickerAction2, mTickerAction3};
        Log.d(TAG, "setTickerContent actions length:" + length + ", needEncrypt:" + needEncrypt);
        if (length <= 0 || needEncrypt) {
            showActionButton = false;
        } else {
            showActionButton = true;
        }
        if (showActionButton) {
            mTickerTailContainer.setVisibility(View.VISIBLE);
            mTickerTail.setVisibility(View.VISIBLE);
            updateActionViewVisibility(length, textViews);
            applyActionsBackground(length, textViews);
            if (hasRemoteInput(actions)) {
                mRemoteInputView = applyRemoteInput(mTickerTailContainer, entry, true, r);
                mRemoteInputView.setVisibility(View.INVISIBLE);
            } else {
                resetRemoteInputView();
            }
            for (int index = 0; index < length; index++) {
                updateActionButton(textViews[index], actions[index]);
            }
        } else {
            mTickerTailContainer.setVisibility(View.GONE);
            resetRemoteInputView();
        }
    }

    private void resetRemoteInputView() {
        if (mRemoteInputView != null) {
            mTickerTailContainer.removeView(mRemoteInputView);
            mRemoteInputView = null;
        }
    }

    private void applyActionsBackground(int length, TextView[] textViews) {
        if (length > 0) {
            switch (length) {
                case 1:
                    textViews[0].setBackgroundResource(R.drawable.prize_ticker_only_one_action_selector);
                    break;
                case 2:
                    textViews[0].setBackgroundResource(R.drawable.prize_ticker_more_action_left_selector);
                    textViews[1].setBackgroundResource(R.drawable.prize_ticker_more_action_right_selector);
                    break;
                case 3:
                    textViews[0].setBackgroundResource(R.drawable.prize_ticker_more_action_left_selector);
                    textViews[1].setBackgroundResource(R.drawable.prize_ticker_more_action_middle_selector);
                    textViews[2].setBackgroundResource(R.drawable.prize_ticker_more_action_right_selector);
                    break;
            }
        }
    }

    private void updateActionViewVisibility(int length, TextView[] textViews) {
        int index = 0;
        while (index < textViews.length) {
            textViews[index].setVisibility(index < length ? View.VISIBLE : View.GONE);
            index++;
        }
    }

    private boolean hasRemoteInput(Notification.Action[] actions) {
        if (actions == null) {
            return false;
        }
        for (Notification.Action a : actions) {
            if (a.getRemoteInputs() != null) {
                for (RemoteInput ri : a.getRemoteInputs()) {
                    if (ri.getAllowFreeFormInput()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void updateActionButton(TextView view, final Notification.Action action) {
        if (view == null) {
            Log.w(TAG, "updateActionButton, action view is null");
        } else if (!checkRemoteInputAction(action, false)) {
            setActionOnClickListener(view, action.title, action.actionIntent);
        } else if (mRemoteInputView == null) {
            Log.w(TAG, "updateActionButton, mRemoteInputView is null");
        } else {
            view.setText(action.title);
            view.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == 0) {
                        mTickerController.updateTickerWindow(true);
                    }
                    return false;
                }
            });
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    checkRemoteInputAction(action, true);
                    mRemoteInputView.focus();
                    updateActionVisibleForRemoteInputClick(true);
                }
            });
        }
    }

    public void updateActionVisibleForRemoteInputClick(boolean showingInput) {
        for (TextView view : new TextView[]{mTickerAction1, mTickerAction2, mTickerAction3}) {
            if (showingInput && view.getVisibility() == View.VISIBLE) {
                view.setVisibility(View.INVISIBLE);
            }
            if (!showingInput && View.INVISIBLE == view.getVisibility()) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean checkRemoteInputAction(Notification.Action action, boolean performAction) {
        RemoteInput[] inputs = action.getRemoteInputs();
        if (inputs == null) {
            return false;
        }
        RemoteInput input = null;
        for (RemoteInput i : inputs) {
            if (i.getAllowFreeFormInput()) {
                input = i;
            }
        }
        if (input == null) {
            return false;
        }
        if (performAction) {
            mRemoteInputView.setRemoteInput(inputs, input);
            mRemoteInputView.setPendingIntent(action.actionIntent);
        }
        return true;
    }

    private boolean noAutoCollapse(Notification notification) {
        return notification.fullScreenIntent != null;
    }

    private boolean needAutoCollapse(String pkg) {
        boolean needAutoCollapse = false;
        if (pkg != null && pkg.equals("com.android.mms")) {
            needAutoCollapse = true;
        }
        return needAutoCollapse;
    }

    private void populateLargeIcon(Notification notification, boolean needEncrypt) {
        if (needEncrypt) {
            if (DEBUG) {
                Log.d(TAG, "Ticker is encrypt");
            }
            mTickerRightIcon.setVisibility(8);
            return;
        }
        Icon largeIcon = notification.getLargeIcon();
        if (largeIcon == null) {
            largeIcon = getIconFromExtra(notification);
        } else if (DEBUG) {
            Log.d(TAG, "Ticker larger icon get from getMethod :" + largeIcon);
        }
        if (largeIcon != null) {
            mTickerRightIcon.setVisibility(View.VISIBLE);
            mTickerRightIcon.setImageDrawable(largeIcon.loadDrawable(mContext));
            return;
        }
        mTickerRightIcon.setVisibility(View.GONE);
    }

    private Icon getIconFromExtra(Notification notification) {
        Icon icon = null;
        Parcelable tempIcon = notification.extras.getParcelable(Notification.EXTRA_LARGE_ICON);
        if (tempIcon instanceof Icon) {
            icon = (Icon) tempIcon;
        } else if (tempIcon instanceof Bitmap) {
            icon = Icon.createWithBitmap((Bitmap) tempIcon);
        }
        if (DEBUG) {
            Log.d(TAG, "Ticker larger icon get from extras :" + tempIcon);
        }
        return icon;
    }

    private boolean needEncrypt(String pkg) {
        return false;
    }

    private void setTickerSummary(Notification notification, CharSequence tickerText, boolean needEncrypt) {
        if (needEncrypt) {
            mTickerContent.setVisibility(View.GONE);
            return;
        }
        CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
        Log.w(TAG, "setTickerSummary notification EXTRA_TEXT = " + (text != null ? text.toString() : "null"));
        Log.w(TAG, "setTickerSummary notification tickerText = " + (tickerText != null ? tickerText.toString() : "null"));
        String summary = text != null ? text.toString() : tickerText != null ? tickerText.toString() : null;
        if (TextUtils.isEmpty(summary)) {
            mTickerContent.setVisibility(View.GONE);
            return;
        }
        mTickerContent.setVisibility(View.VISIBLE);
        setTickerContentText(summary);
    }

    private void setTickerTitle(String pkg, Notification notification, boolean needEncrypt) {
        String tickerTitle = null;
        if (needEncrypt) {
            mTickerTitle.setText(com.android.internal.R.string.notification_hidden_text);
            return;
        }
        CharSequence tile = notification.extras.getCharSequence(Notification.EXTRA_TITLE, null);
        if (tile != null) {
            tickerTitle = tile.toString();
        }
        if (TextUtils.isEmpty(tickerTitle)) {
            Log.w(TAG, pkg + " notification EXTRA_TITLE is empty");
            tickerTitle = getLabelFromPkg(pkg);
        }
        if (DEBUG) {
            Log.d(TAG, "tickerTitle = " + tickerTitle);
        }
        mTickerTitle.setText(tickerTitle);
    }

    public void setActionOnClickListener(TextView view, final CharSequence actiontitle, final PendingIntent actionintent) {
        view.setText(actiontitle);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (actionintent != null) {
                        actionintent.send();
                    }
                    Log.d(TAG, "onClick_v.getText = " + ((TextView) v).getText() + ", actiontitle = " + actiontitle + ", actionintent = " + actionintent);
                } catch (PendingIntent.CanceledException e) {
                    Log.d(TAG, "actions[0]_e = " + e);
                }
                mTickerController.animateTickerCollapse();
            }
        });
    }

    public void updateTypeFace() {
        if (DEBUG) {
            Log.d(TAG, "updateTypeFace");
        }
        if (mTickerContent != null) {
            mTickerContent.setTypeface(Typeface.DEFAULT);
        }
        if (mTickerTitle != null) {
            mTickerTitle.setTypeface(Typeface.DEFAULT);
        }
        if (mTickerDate != null) {
            mTickerDate.setTypeface(Typeface.DEFAULT);
        }
        if (mTickerAppName != null) {
            mTickerAppName.setTypeface(Typeface.DEFAULT);
        }
    }

    public String getLabelFromPkg(String pkg) {
        PackageManager pManager = mContext.getPackageManager();
        try {
            return (String) pManager.getApplicationInfo(pkg, PackageManager.GET_META_DATA).loadLabel(pManager);
        } catch (Exception e) {
            return null;
        }
    }

    public Drawable getIconBitmap(Context context, StatusBarIcon icon) {
        if (DEBUG) {
            Log.d(TAG, "getIconBitmap_icon = " + icon);
        }
        Drawable iconDrawable = StatusBarIconView.getIcon(context, icon);
        if (DEBUG) {
            Log.d(TAG, "getIconBitmap_iconDrawable = " + iconDrawable);
        }
        return iconDrawable;
    }

    public String getApplicationName(String pkgName) {
        ApplicationInfo applicationInfo;
        PackageManager packageManager = null;
        try {
            packageManager = mContext.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        if (packageManager != null) {
            return (String) packageManager.getApplicationLabel(applicationInfo);
        }
        return null;
    }

    private RemoteInputView applyRemoteInput(ViewGroup view, NotificationData.Entry entry, boolean hasRemoteInput, RemoteInputController r) {
        if (!hasRemoteInput) {
            return null;
        }
        RemoteInputView existing = (RemoteInputView) view.findViewWithTag(RemoteInputView.VIEW_TAG);
        if (existing != null) {
            existing.onNotificationUpdateOrReset();
        }
        if (existing == null && hasRemoteInput) {
            RemoteInputView riv = RemoteInputView.inflate(mContext, view, entry, r);
            riv.setVisibility(View.INVISIBLE);
            view.addView(riv, new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
            existing = riv;
        }
        Notification.Action[] actions = entry.notification.getNotification().actions;
        if (existing != null) {
            existing.updatePendingIntentFromActions(actions);
        }
        if (!hasRemoteInput || entry.notification.getNotification().color != 0) {
            return existing;
        }
        mContext.getColor(R.color.default_remote_input_background);
        return existing;
    }

    public void onNotificationUpdateOrReset() {
        if (mRemoteInputView != null) {
            mRemoteInputView.onNotificationUpdateOrReset();
        }
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
        Log.d(TAG, "ticker view height = " + viewHeight);
        return viewHeight;
    }

    @Override
    public void makeTickerVisible(boolean visible) {
        if (DEBUG) {
            Log.i(TAG, "makeTickerVisible  visible = " + visible + "  mTickerViewVisible = " + getTickerVisible());
        }
        if (visible) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
            setNotification(null);
        }
        onNotificationUpdateOrReset();
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
