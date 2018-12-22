/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui.statusbar.phone;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;

import java.io.PrintWriter;
import java.util.ArrayList;
//add for statusbar inverse. prize-linkh-20150903
import com.mediatek.common.prizeoption.PrizeOption;
import com.android.systemui.statusbar.phone.PrizeStatusBarStyleListener;
import android.app.StatusBarManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Icon;
import com.android.systemui.power.PowerNotificationWarnings;
import android.util.Log;
/* app multi instances feature. prize-linkh-20151228 */

/*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
import com.android.systemui.statusbar.LiuHaiStatusBarIconController;
import com.android.systemui.statusbar.LiuHaiStatusBarIconController.StatusIconsListener;
import com.android.systemui.LiuHaiBatteryMeterViewDefinedNew;
/*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
/*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
import com.android.systemui.LiuHaiBatteryMeterViewDefinedNew2;
/*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/

/**
 * Controls everything regarding the icons in the status bar and on Keyguard, including, but not
 * limited to: notification icons, signal cluster, additional status icons, and clock in the status
 * bar.
 */
public class StatusBarIconController extends StatusBarIconList implements Tunable, PrizeStatusBarStyleListener  {

    public static final long DEFAULT_TINT_ANIMATION_DURATION = 120;
    public static final String ICON_BLACKLIST = "icon_blacklist";
    public static final int DEFAULT_ICON_TINT = Color.WHITE;
	//add for statusbar inverse. prize-linkh-20150903
    private static final String TAG = "StatusBarIconController";
	//end...

    private Context mContext;
    private PhoneStatusBar mPhoneStatusBar;
    private DemoStatusIcons mDemoStatusIcons;

    private LinearLayout mSystemIconArea;
    private LinearLayout mStatusIcons;
    private SignalClusterView mSignalCluster;
    private LinearLayout mStatusIconsKeyguard;

    private NotificationIconAreaController mNotificationIconAreaController;
    private View mNotificationIconAreaInner;

    private BatteryMeterView mBatteryMeterView;
    private BatteryMeterView mBatteryMeterViewKeyguard;
    private TextView mClock;

    private int mIconSize;
    private int mIconHPadding;

    private int mIconTint = DEFAULT_ICON_TINT;
    private float mDarkIntensity;
    private final Rect mTintArea = new Rect();
    private static final Rect sTmpRect = new Rect();
    private static final int[] sTmpInt2 = new int[2];

    private boolean mTransitionPending;
    private boolean mTintChangePending;
    private float mPendingDarkIntensity;
    private ValueAnimator mTintAnimator;

    private int mDarkModeIconColorSingleTone;
    private int mLightModeIconColorSingleTone;

    private final Handler mHandler;
    private boolean mTransitionDeferring;
    private long mTransitionDeferringStartTime;
    private long mTransitionDeferringDuration;

    private final ArraySet<String> mIconBlacklist = new ArraySet<>();

    private final Runnable mTransitionDeferringDoneRunnable = new Runnable() {
        @Override
        public void run() {
            mTransitionDeferring = false;
        }
    };

    /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
    private LinearLayout mLiuHaiArea;
    private LinearLayout mKeyguardLiuHaiArea;
    private TextView mLiuHaiClock;
    private LiuHaiBatteryMeterViewDefinedNew mLiuHaiBatteryView;
    private ArrayList<LiuHaiStatusBarIconController> mLiuHaiIconControllerList;
    /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

    /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
    private LinearLayout mLiuHaiArea2;
    private ViewGroup mWifiGroup;
    private ImageView mWifi, mWifiInOut, mAirplane;
    private LiuHaiIconGroupPrize2 mStatusBarLeftGroup;
    private LiuHaiIconGroupPrize2 mStatusBarRightGroup;

    private LinearLayout mKeyguardLiuHaiArea2;
    private ViewGroup mKeyguardWifiGroup;
    private ImageView mKeyguardWifi, mKeyguardWifiInOut, mKeyguardAirplane;
    private LiuHaiIconGroupPrize2 mKeyguardLeftGroup;
    private LiuHaiIconGroupPrize2 mKeyguardRightGroup;

    private LinearLayout mHeaderLiuHaiArea;
    private TextView mHeaderLiuHaiClock;
    private ViewGroup mHeaderWifiGroup;
    private ImageView mHeaderWifi, mHeaderWifiInOut, mHeaderAirplane;
    private LiuHaiIconGroupPrize2 mHeaderLeftGroup;
    private LiuHaiIconGroupPrize2 mHeaderRightGroup;
    /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/

    public StatusBarIconController(Context context, View statusBar, View keyguardStatusBar,
            PhoneStatusBar phoneStatusBar) {
        super(context.getResources().getStringArray(
                com.android.internal.R.array.config_statusBarIcons));
        mContext = context;
        mPhoneStatusBar = phoneStatusBar;
        mSystemIconArea = (LinearLayout) statusBar.findViewById(R.id.system_icon_area);
        mStatusIcons = (LinearLayout) statusBar.findViewById(R.id.statusIcons);
        mSignalCluster = (SignalClusterView) statusBar.findViewById(R.id.signal_cluster);

        mNotificationIconAreaController = SystemUIFactory.getInstance()
                .createNotificationIconAreaController(context, phoneStatusBar);
        mNotificationIconAreaInner =
                mNotificationIconAreaController.getNotificationInnerAreaView();

        ViewGroup notificationIconArea =
                (ViewGroup) statusBar.findViewById(R.id.notification_icon_area);
        notificationIconArea.addView(mNotificationIconAreaInner);

        mStatusIconsKeyguard = (LinearLayout) keyguardStatusBar.findViewById(R.id.statusIcons);

        mBatteryMeterView = (BatteryMeterView) statusBar.findViewById(R.id.battery);
        mBatteryMeterViewKeyguard = (BatteryMeterView) keyguardStatusBar.findViewById(R.id.battery);
        scaleBatteryMeterViews(context);

        mClock = (TextView) statusBar.findViewById(R.id.clock);
        mDarkModeIconColorSingleTone = context.getColor(R.color.dark_mode_icon_color_single_tone);
        mLightModeIconColorSingleTone = context.getColor(R.color.light_mode_icon_color_single_tone);
        mHandler = new Handler();
        loadDimens();

        TunerService.get(mContext).addTunable(this, ICON_BLACKLIST);

        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        mLiuHaiArea = (LinearLayout) statusBar.findViewById(R.id.prize_liuhai_area);
        mKeyguardLiuHaiArea = (LinearLayout) keyguardStatusBar.findViewById(R.id.prize_liuhai_area);
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            notificationIconArea.setVisibility(View.GONE);
            mSystemIconArea.setVisibility(View.GONE);
            mLiuHaiArea.setVisibility(View.VISIBLE);
            mLiuHaiClock = (TextView) statusBar.findViewById(R.id.clock_liuhai);
            mLiuHaiBatteryView = (LiuHaiBatteryMeterViewDefinedNew) statusBar.findViewById(R.id.liuhai_battery_new);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams)mLiuHaiBatteryView.getLayoutParams();
            p.setMargins(0, 0, 20, 0);
            mLiuHaiBatteryView.setLayoutParams(p);

            if(mPhoneStatusBar != null){
                mLiuHaiIconControllerList = mPhoneStatusBar.getLiuHaiIconControllerList();
                LinearLayout linear = (LinearLayout)mLiuHaiArea.findViewById(R.id.liuhai_statusIcons);
                LinearLayout keyguardLinear = (LinearLayout)mKeyguardLiuHaiArea.findViewById(R.id.liuhai_statusIcons);
                if(mLiuHaiIconControllerList != null) {
                    mLiuHaiIconControllerList.get(0).setStatusIconParent(linear);
                    mLiuHaiIconControllerList.get(1).setStatusIconParent(keyguardLinear);
                    for(LiuHaiStatusBarIconController controller : mLiuHaiIconControllerList){
                        controller.setStatusBarIconController(this);
                    }
                }
            }
        } else {
            mLiuHaiArea.setVisibility(View.GONE);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        mLiuHaiArea2 = (LinearLayout) statusBar.findViewById(R.id.prize_liuhai_area2);
        mKeyguardLiuHaiArea2 = (LinearLayout) keyguardStatusBar.findViewById(R.id.prize_liuhai_area2);
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            notificationIconArea.setVisibility(View.GONE);
            mSystemIconArea.setVisibility(View.GONE);
            mLiuHaiArea2.setVisibility(View.VISIBLE);
            /*mLiuHaiBatteryView = (LiuHaiBatteryMeterViewDefinedNew) statusBar.findViewById(R.id.liuhai_battery_new);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams)mLiuHaiBatteryView.getLayoutParams();
            p.setMargins(0, 0, 0, 0);
            mLiuHaiBatteryView.setLayoutParams(p);*/
            int statusbar_height = mPhoneStatusBar.getStatusBarHeight();
            PhoneStatusBar.debugLiuHai("statusbar_height = " + statusbar_height);
            mWifiGroup = (ViewGroup)mLiuHaiArea2.findViewById(R.id.liuhai_wifi_combo);
            mWifi = (ImageView)mLiuHaiArea2.findViewById(R.id.liuhai_wifi_signal);
            mWifiInOut = (ImageView)mLiuHaiArea2.findViewById(R.id.liuhai_wifi_inout);
            mAirplane = (ImageView)mLiuHaiArea2.findViewById(R.id.liuhai_airplane);
            mStatusBarLeftGroup = (LiuHaiIconGroupPrize2)mLiuHaiArea2.findViewById(R.id.liuhai_left_group);
            mStatusBarRightGroup = (LiuHaiIconGroupPrize2)mLiuHaiArea2.findViewById(R.id.liuhai_right_group);

            //mKeyguardLiuHaiClock = (TextView) mKeyguardLiuHaiArea.findViewById(R.id.clock_liuhai);
            //mKeyguardLiuHaiClock.setVisibility(View.GONE);
            mKeyguardWifiGroup = (ViewGroup)mKeyguardLiuHaiArea2.findViewById(R.id.liuhai_wifi_combo);
            mKeyguardWifi = (ImageView)mKeyguardLiuHaiArea2.findViewById(R.id.liuhai_wifi_signal);
            mKeyguardWifiInOut = (ImageView)mKeyguardLiuHaiArea2.findViewById(R.id.liuhai_wifi_inout);
            mKeyguardAirplane = (ImageView)mKeyguardLiuHaiArea2.findViewById(R.id.liuhai_airplane);
            mKeyguardLeftGroup = (LiuHaiIconGroupPrize2)mKeyguardLiuHaiArea2.findViewById(R.id.liuhai_left_group);
            mKeyguardRightGroup = (LiuHaiIconGroupPrize2)mKeyguardLiuHaiArea2.findViewById(R.id.liuhai_right_group);

        } else {
            mLiuHaiArea2.setVisibility(View.GONE);
        }
        if(!PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            notificationIconArea.setVisibility(View.VISIBLE);
            mSystemIconArea.setVisibility(View.VISIBLE);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
    }

    /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
    private BaseStatusBarHeader mHeader;
    public void setStatusBarHeaderView(BaseStatusBarHeader view){
        mHeader = view;
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2 && mHeader != null){
            mHeaderLiuHaiArea = (LinearLayout) mHeader.findViewById(R.id.prize_liuhai_area2);

            mHeaderWifiGroup = (ViewGroup)mHeaderLiuHaiArea.findViewById(R.id.liuhai_wifi_combo);
            mHeaderWifi = (ImageView)mHeaderLiuHaiArea.findViewById(R.id.liuhai_wifi_signal);
            mHeaderWifiInOut = (ImageView)mHeaderLiuHaiArea.findViewById(R.id.liuhai_wifi_inout);
            mHeaderAirplane = (ImageView)mHeaderLiuHaiArea.findViewById(R.id.liuhai_airplane);
            mHeaderLeftGroup = (LiuHaiIconGroupPrize2)mHeaderLiuHaiArea.findViewById(R.id.liuhai_left_group);
            mHeaderRightGroup = (LiuHaiIconGroupPrize2)mHeaderLiuHaiArea.findViewById(R.id.liuhai_right_group);
        }
    }

    private LiuHaiStatusBarIconController2 mLiuHaiStatusBarIconController;
    public void initLiuHaiStatusBarIconListener(LiuHaiStatusBarIconController2 controller){
        if(controller == null){
            PhoneStatusBar.debugLiuHai2("setLiuHaiStatusBarIconListener is null");
            return ;
        }
        PhoneStatusBar.debugLiuHai2("StatusBarIconController initLiuHaiStatusBarIconListener");
        mLiuHaiStatusBarIconController = controller;
        mLiuHaiStatusBarIconController.setStatusBarIconController(this);
        LiuHaiStatusBarIconController2.InverseController inverseController
            = mLiuHaiStatusBarIconController.getInverseController();
        inverseController.initData(mLiuHaiArea2);
        mStatusIconImpI = controller.getStatusIconImpI();
        mStatusIconImpI.initValue(mStatusBarLeftGroup, mStatusBarRightGroup);
        mStatusIconImpI.initLiuHaiStatusBarIconData(R.dimen.liuhai_status_bar_space2,
            R.array.config_liuhai_statusBarIcons_direction2,
            R.array.config_liuhai_statusBarIcons_width2,
            R.array.config_liuhai_statusBarIcons2);
        mStatusIconImpI.setRightIconController(controller.getRightIconController());
        mStatusIconImpI.setLimitShow(true);

        LiuHaiStatusBarIconController2.ShowOnLeftListenerImpI showOnLeftListenerImpI = controller.getShowOnLeftListenerImpI();
        showOnLeftListenerImpI.initValue(mWifiGroup, mWifi, mWifiInOut, mAirplane);
    }

    private LiuHaiStatusBarIconController2 mLiuHaiKeyguardIconController;
    public void initLiuHaiKeyguardIconListener(LiuHaiStatusBarIconController2 controller){
        if(controller == null){
            PhoneStatusBar.debugLiuHai2("setLiuHaiKeyguardIconListener is null");
            return ;
        }
        mLiuHaiKeyguardIconController = controller;
        mKeguardIconImpI = controller.getStatusIconImpI();
        mKeguardIconImpI.initValue(mKeyguardLeftGroup, mKeyguardRightGroup);
        mKeguardIconImpI.initLiuHaiStatusBarIconData(R.dimen.liuhai_status_bar_space2,
            R.array.config_liuhai_statusBarIcons_direction2,
            R.array.config_liuhai_statusBarIcons_width2,
            R.array.config_liuhai_statusBarIcons2);
        mKeguardIconImpI.setRightIconController(controller.getRightIconController());
        mKeguardIconImpI.setLimitShow(true);

        LiuHaiStatusBarIconController2.ShowOnLeftListenerImpI showOnLeftListenerImpI = controller.getShowOnLeftListenerImpI();
        showOnLeftListenerImpI.initValue(mKeyguardWifiGroup, mKeyguardWifi, mKeyguardWifiInOut, mKeyguardAirplane);
    }

    private LiuHaiStatusBarIconController2 mLiuHaiHeaderIconController;
    public void initLiuHaiHeaderIconListener(LiuHaiStatusBarIconController2 controller){
        if(controller == null){
            PhoneStatusBar.debugLiuHai2("setLiuHaiKeyguardIconListener is null");
            return ;
        }
        mLiuHaiHeaderIconController = controller;
        mHeaderIconImpI = controller.getStatusIconImpI();
        mHeaderIconImpI.initValue(mHeaderLeftGroup, mHeaderRightGroup);
        mHeaderIconImpI.initLiuHaiStatusBarIconData(R.dimen.liuhai_status_bar_space2,
            R.array.config_liuhai_headerIcons_direction2,
            R.array.config_liuhai_headerIcons_width2,
            R.array.config_liuhai_headerIcons2);

        LiuHaiStatusBarIconController2.ShowOnLeftListenerImpI showOnLeftListenerImpI = controller.getShowOnLeftListenerImpI();
        showOnLeftListenerImpI.initValue(mHeaderWifiGroup, mHeaderWifi, mHeaderWifiInOut, mHeaderAirplane);
    }

    private LiuHaiStatusBarIconController2.StatusIconImpI mStatusIconImpI;
    public LiuHaiStatusBarIconController2.StatusIconImpI getStatusIconImpI(){
        return mStatusIconImpI;
    }

    private LiuHaiStatusBarIconController2.StatusIconImpI mKeguardIconImpI;
    public LiuHaiStatusBarIconController2.StatusIconImpI getKeguardIconImpI(){
        return mKeguardIconImpI;
    }

    private LiuHaiStatusBarIconController2.StatusIconImpI mHeaderIconImpI;
    public LiuHaiStatusBarIconController2.StatusIconImpI getHeaderIconImpI(){
        return mHeaderIconImpI;
    }

    public void toShowInLiuHaiStatusBar2(StatusBarIcon icon, String slot, int index, boolean visibility){
        if(!PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            return ;
        }
        if(mStatusIconImpI != null){
            mStatusIconImpI.refreshStatusIcon(icon, slot, index, visibility);
        }
        if(mKeguardIconImpI != null){
            mKeguardIconImpI.refreshStatusIcon(icon, slot, index, visibility);
        }
        if(mHeaderIconImpI != null){
            mHeaderIconImpI.refreshStatusIcon(icon, slot, index, visibility);
        }
    }
    /*PRIZE-add for liuhai screen-liufan-2018-05-17-end*/

    /*PRIZE-add for clock view- liufan-2016-11-07-start*/
	public View getNotificationIconLayout(){
	    return mNotificationIconAreaInner;
	}
    /*PRIZE-add for clock view- liufan-2016-11-07-end*/

    public void setSignalCluster(SignalClusterView signalCluster) {
        mSignalCluster = signalCluster;
    }

    /**
     * Looks up the scale factor for status bar icons and scales the battery view by that amount.
     */
    private void scaleBatteryMeterViews(Context context) {
        Resources res = context.getResources();
        TypedValue typedValue = new TypedValue();

        res.getValue(R.dimen.status_bar_icon_scale_factor, typedValue, true);
        float iconScaleFactor = typedValue.getFloat();

        int batteryHeight = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_height);
        int batteryWidth = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_width);
        int marginBottom = res.getDimensionPixelSize(R.dimen.battery_margin_bottom);

        LinearLayout.LayoutParams scaledLayoutParams = new LinearLayout.LayoutParams(
                (int) (batteryWidth * iconScaleFactor), (int) (batteryHeight * iconScaleFactor));
        scaledLayoutParams.setMarginsRelative(0, 0, 0, marginBottom);

        mBatteryMeterView.setLayoutParams(scaledLayoutParams);
        mBatteryMeterViewKeyguard.setLayoutParams(scaledLayoutParams);
    }

    @Override
    public void onTuningChanged(String key, String newValue) {
        if (!ICON_BLACKLIST.equals(key)) {
            return;
        }
        mIconBlacklist.clear();
        mIconBlacklist.addAll(getIconBlacklist(newValue));
        ArrayList<StatusBarIconView> views = new ArrayList<StatusBarIconView>();
        // Get all the current views.
        for (int i = 0; i < mStatusIcons.getChildCount(); i++) {
            views.add((StatusBarIconView) mStatusIcons.getChildAt(i));
        }
        // Remove all the icons.
        for (int i = views.size() - 1; i >= 0; i--) {
            removeIcon(views.get(i).getSlot());
        }
        // Add them all back
        for (int i = 0; i < views.size(); i++) {
            setIcon(views.get(i).getSlot(), views.get(i).getStatusBarIcon());
        }
    }
    private void loadDimens() {
        mIconSize = mContext.getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.status_bar_icon_size);
        mIconHPadding = mContext.getResources().getDimensionPixelSize(
                R.dimen.status_bar_icon_padding);
    }

    private void addSystemIcon(int index, StatusBarIcon icon) {
        String slot = getSlot(index);
        int viewIndex = getViewIndex(index);
        boolean blocked = mIconBlacklist.contains(slot);
        StatusBarIconView view = new StatusBarIconView(mContext, slot, null, blocked);
        view.set(icon);
        //add for statusbar inverse. prize-linkh-20150903
        if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && mCurStatusBarStyle != 0) {
            setStatusIconForStyle(view, true, true);
        } //end...
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, mIconSize);
        lp.setMargins(mIconHPadding, 0, mIconHPadding, 0);
        mStatusIcons.addView(view, viewIndex, lp);

        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        toShowInLiuHaiStatusBar(icon, slot, index, view.getVisibility() == View.VISIBLE ? true : false);
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        toShowInLiuHaiStatusBar2(icon, slot, index, view.getVisibility() == View.VISIBLE ? true : false);
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/

        view = new StatusBarIconView(mContext, slot, null, blocked);
        view.set(icon);

        if(PrizeOption.PRIZE_LOCKSCREEN_INVERSE && textColor != 1) {
            setKeyguardStatusIconForStyle(view, true, true);
        }
        mStatusIconsKeyguard.addView(view, viewIndex, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, mIconSize));
        applyIconTint();
    }

    public void setIcon(String slot, int resourceId, CharSequence contentDescription) {
        int index = getSlotIndex(slot);
        StatusBarIcon icon = getIcon(index);
        if (icon == null) {
            icon = new StatusBarIcon(UserHandle.SYSTEM, mContext.getPackageName(),
                    Icon.createWithResource(mContext, resourceId), 0, 0, contentDescription);
            setIcon(slot, icon);
        } else {
            icon.icon = Icon.createWithResource(mContext, resourceId);
            icon.contentDescription = contentDescription;
            handleSet(index, icon);
        }
    }

    public void setExternalIcon(String slot) {
        int viewIndex = getViewIndex(getSlotIndex(slot));
        int height = mContext.getResources().getDimensionPixelSize(
                R.dimen.status_bar_icon_drawing_size);
        ImageView imageView = (ImageView) mStatusIcons.getChildAt(viewIndex);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        setHeightAndCenter(imageView, height);
        imageView = (ImageView) mStatusIconsKeyguard.getChildAt(viewIndex);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        setHeightAndCenter(imageView, height);
    }

    private void setHeightAndCenter(ImageView imageView, int height) {
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.height = height;
        if (params instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) params).gravity = Gravity.CENTER_VERTICAL;
        }
        imageView.setLayoutParams(params);
    }

    public void setIcon(String slot, StatusBarIcon icon) {
        setIcon(getSlotIndex(slot), icon);
    }

    public void removeIcon(String slot) {
        int index = getSlotIndex(slot);
        removeIcon(index);
    }

    public void setIconVisibility(String slot, boolean visibility) {
        int index = getSlotIndex(slot);
        StatusBarIcon icon = getIcon(index);
        if (icon == null || icon.visible == visibility) {
            return;
        }
        icon.visible = visibility;
        handleSet(index, icon);
    }

    /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
    public void toShowInLiuHaiStatusBar(StatusBarIcon icon, String slot, int index, boolean visibility){
        if(!PhoneStatusBar.OPEN_LIUHAI_SCREEN || PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            return ;
        }
        for(LiuHaiStatusBarIconController controller : mLiuHaiIconControllerList){
            if(controller != null){
                LiuHaiStatusBarIconController.StatusIconsListener listener
                    = controller.getStatusIconsListener();
                if(listener != null){
                    listener.refreshStatusIcon(icon, slot, index, visibility);
                }
            }
        }
    }
    /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

    @Override
    public void removeIcon(int index) {
        if (getIcon(index) == null) {
            return;
        }
        super.removeIcon(index);
        int viewIndex = getViewIndex(index);
        mStatusIcons.removeViewAt(viewIndex);
        mStatusIconsKeyguard.removeViewAt(viewIndex);

        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        StatusBarIconView view = (StatusBarIconView) mStatusIcons.getChildAt(viewIndex);
        toShowInLiuHaiStatusBar(getIcon(index), getSlot(index), index, false);
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        toShowInLiuHaiStatusBar2(getIcon(index), getSlot(index), index, false);
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
    }

    @Override
    public void setIcon(int index, StatusBarIcon icon) {
        if (icon == null) {
            removeIcon(index);
            return;
        }

        /*prize add by xiarui for Bug#43533 2017-12-18 start*/
        if ("com.sohu.inputmethod.sogou".equals(icon.pkg)) {
            removeIcon(index);
            return;
        }
        /*prize add by xiarui for Bug#43533 2017-12-18 end*/

        boolean isNew = getIcon(index) == null;
        super.setIcon(index, icon);
        if (isNew) {
            addSystemIcon(index, icon);
        } else {
            handleSet(index, icon);
        }
    }

    private void handleSet(int index, StatusBarIcon icon) {
        int viewIndex = getViewIndex(index);
        StatusBarIconView view = (StatusBarIconView) mStatusIcons.getChildAt(viewIndex);
        view.set(icon);
        //add for statusbar inverse. prize-linkh-20150903
        if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && mCurStatusBarStyle != 0) {
            setStatusIconForStyle(view, true, true);
        } //end...

        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        toShowInLiuHaiStatusBar(icon, getSlot(index), index, view.getVisibility() == View.VISIBLE ? true : false);
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        toShowInLiuHaiStatusBar2(icon, getSlot(index), index, view.getVisibility() == View.VISIBLE ? true : false);
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/

        view = (StatusBarIconView) mStatusIconsKeyguard.getChildAt(viewIndex);
        view.set(icon);
        if(PrizeOption.PRIZE_LOCKSCREEN_INVERSE && textColor != 1) {
            setKeyguardStatusIconForStyle(view, true, true);
        }
        applyIconTint();
    }

    public void updateNotificationIcons(NotificationData notificationData) {
        mNotificationIconAreaController.updateNotificationIcons(notificationData);
    }
	
	/*PRIZE-updateViewsForStatusBarStyleChanged-liufan-2016-04-20-start*/
    public void updateViewsForStatusBarStyleChanged() {
        PrizeStatusBarStyle statusBarStyle = PrizeStatusBarStyle.getInstance(mContext);
        //More icon
		ImageView mMoreIcon = mNotificationIconAreaController.getMoreIcon();
        if(mMoreIcon != null) {
            StatusBarIconView moreIconView = (StatusBarIconView)mMoreIcon;
            int iconId = statusBarStyle.getMoreIcon(mCurStatusBarStyle);
            if(iconId > 0) {
                Log.d(TAG, "update more icon view...");
                moreIconView.setImageResource(iconId); 
            }
        }
		
        //Status Icon area
        if(mStatusIcons != null) {
            int count = mStatusIcons.getChildCount();
            Log.d(TAG, "mStatusIcons.count=" + count);
                
            for(int i = 0; i < count; ++i) {
                StatusBarIconView child = (StatusBarIconView)mStatusIcons.getChildAt(i);
                setStatusIconForStyle(child, true, false);
            }
        }
	
	}
	/*PRIZE-updateViewsForStatusBarStyleChanged-liufan-2016-04-20-end*/

    public void hideSystemIconArea(boolean animate) {
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            //mSystemIconArea.setVisibility(View.GONE);
            animateHide(mLiuHaiArea, animate);
            return ;
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            //mSystemIconArea.setVisibility(View.GONE);
            animateHide(mLiuHaiArea2, animate);
            return ;
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        animateHide(mSystemIconArea, animate);
    }

    public void showSystemIconArea(boolean animate) {
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            //mSystemIconArea.setVisibility(View.GONE);
            animateShow(mLiuHaiArea, animate);
            return ;
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            //mSystemIconArea.setVisibility(View.GONE);
            animateShow(mLiuHaiArea2, animate);
            return ;
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        animateShow(mSystemIconArea, animate);
    }

    public void hideNotificationIconArea(boolean animate) {
        animateHide(mNotificationIconAreaInner, animate);
    }

    public void showNotificationIconArea(boolean animate) {
        animateShow(mNotificationIconAreaInner, animate);
    }

    public void setClockVisibility(boolean visible) {
        mClock.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void dump(PrintWriter pw) {
        int N = mStatusIcons.getChildCount();
        pw.println("  icon views: " + N);
        for (int i=0; i<N; i++) {
            StatusBarIconView ic = (StatusBarIconView) mStatusIcons.getChildAt(i);
            pw.println("    [" + i + "] icon=" + ic);
        }
        super.dump(pw);
    }

    public void dispatchDemoCommand(String command, Bundle args) {
        if (mDemoStatusIcons == null) {
            mDemoStatusIcons = new DemoStatusIcons(mStatusIcons, mIconSize);
        }
        mDemoStatusIcons.dispatchDemoCommand(command, args);
    }

    /**
     * Hides a view.
     */
    private void animateHide(final View v, boolean animate) {
        v.animate().cancel();
        if (!animate) {
            v.setAlpha(0f);
            v.setVisibility(View.INVISIBLE);
            return;
        }
        v.animate()
                .alpha(0f)
                .setDuration(160)
                .setStartDelay(0)
                .setInterpolator(Interpolators.ALPHA_OUT)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        v.setVisibility(View.INVISIBLE);
                    }
                });
    }

    /**
     * Shows a view, and synchronizes the animation with Keyguard exit animations, if applicable.
     */
    private void animateShow(View v, boolean animate) {
        v.animate().cancel();
        v.setVisibility(View.VISIBLE);
        if (!animate) {
            v.setAlpha(1f);
            return;
        }
        v.animate()
                .alpha(1f)
                .setDuration(320)
                .setInterpolator(Interpolators.ALPHA_IN)
                .setStartDelay(50)

                // We need to clean up any pending end action from animateHide if we call
                // both hide and show in the same frame before the animation actually gets started.
                // cancel() doesn't really remove the end action.
                .withEndAction(null);

        // Synchronize the motion with the Keyguard fading if necessary.
        if (mPhoneStatusBar.isKeyguardFadingAway()) {
            v.animate()
                    .setDuration(mPhoneStatusBar.getKeyguardFadingAwayDuration())
                    .setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN)
                    .setStartDelay(mPhoneStatusBar.getKeyguardFadingAwayDelay())
                    .start();
        }
    }

    /**
     * Sets the dark area so {@link #setIconsDark} only affects the icons in the specified area.
     *
     * @param darkArea the area in which icons should change it's tint, in logical screen
     *                 coordinates
     */
    public void setIconsDarkArea(Rect darkArea) {
        if (darkArea == null && mTintArea.isEmpty()) {
            return;
        }
        if (darkArea == null) {
            mTintArea.setEmpty();
        } else {
            mTintArea.set(darkArea);
        }
        applyIconTint();
        mNotificationIconAreaController.setTintArea(darkArea);
    }

    public void setIconsDark(boolean dark, boolean animate) {
        if (!animate) {
            setIconTintInternal(dark ? 1.0f : 0.0f);
        } else if (mTransitionPending) {
            deferIconTintChange(dark ? 1.0f : 0.0f);
        } else if (mTransitionDeferring) {
            animateIconTint(dark ? 1.0f : 0.0f,
                    Math.max(0, mTransitionDeferringStartTime - SystemClock.uptimeMillis()),
                    mTransitionDeferringDuration);
        } else {
            animateIconTint(dark ? 1.0f : 0.0f, 0 /* delay */, DEFAULT_TINT_ANIMATION_DURATION);
        }
    }

    private void animateIconTint(float targetDarkIntensity, long delay,
            long duration) {
        if (mTintAnimator != null) {
            mTintAnimator.cancel();
        }
        if (mDarkIntensity == targetDarkIntensity) {
            return;
        }
        mTintAnimator = ValueAnimator.ofFloat(mDarkIntensity, targetDarkIntensity);
        mTintAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setIconTintInternal((Float) animation.getAnimatedValue());
            }
        });
        mTintAnimator.setDuration(duration);
        mTintAnimator.setStartDelay(delay);
        mTintAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        mTintAnimator.start();
    }

    private void setIconTintInternal(float darkIntensity) {
        mDarkIntensity = darkIntensity;
        mIconTint = (int) ArgbEvaluator.getInstance().evaluate(darkIntensity,
                mLightModeIconColorSingleTone, mDarkModeIconColorSingleTone);
        mNotificationIconAreaController.setIconTint(mIconTint);
        applyIconTint();
    }

    private void deferIconTintChange(float darkIntensity) {
        if (mTintChangePending && darkIntensity == mPendingDarkIntensity) {
            return;
        }
        mTintChangePending = true;
        mPendingDarkIntensity = darkIntensity;
    }

    /**
     * @return the tint to apply to {@param view} depending on the desired tint {@param color} and
     *         the screen {@param tintArea} in which to apply that tint
     */
    public static int getTint(Rect tintArea, View view, int color) {
        if (isInArea(tintArea, view)) {
            return color;
        } else {
            return DEFAULT_ICON_TINT;
        }
    }

    /**
     * @return the dark intensity to apply to {@param view} depending on the desired dark
     *         {@param intensity} and the screen {@param tintArea} in which to apply that intensity
     */
    public static float getDarkIntensity(Rect tintArea, View view, float intensity) {
        if (isInArea(tintArea, view)) {
            return intensity;
        } else {
            return 0f;
        }
    }

    /**
     * @return true if more than half of the {@param view} area are in {@param area}, false
     *         otherwise
     */
    private static boolean isInArea(Rect area, View view) {
        if (area.isEmpty()) {
            return true;
        }
        sTmpRect.set(area);
        view.getLocationOnScreen(sTmpInt2);
        int left = sTmpInt2[0];

        int intersectStart = Math.max(left, area.left);
        int intersectEnd = Math.min(left + view.getWidth(), area.right);
        int intersectAmount = Math.max(0, intersectEnd - intersectStart);

        boolean coversFullStatusBar = area.top <= 0;
        boolean majorityOfWidth = 2 * intersectAmount > view.getWidth();
        return majorityOfWidth && coversFullStatusBar;
    }

    private void applyIconTint() {
		/*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-Start*/
		//applyNotificationIconsTint();
		/*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-end*/
		/*PRIZE-cancel the colorTint-liufan-2016-04-26-start*/
        /*for (int i = 0; i < mStatusIcons.getChildCount(); i++) {
            StatusBarIconView v = (StatusBarIconView) mStatusIcons.getChildAt(i);
            v.setImageTintList(ColorStateList.valueOf(getTint(mTintArea, v, mIconTint)));
        }
        mSignalCluster.setIconTint(mIconTint, mDarkIntensity, mTintArea);
        mBatteryMeterView.setDarkIntensity(
                isInArea(mTintArea, mBatteryMeterView) ? mDarkIntensity : 0);
        mClock.setTextColor(getTint(mTintArea, mClock, mIconTint));*/
		/*PRIZE-cancel the colorTint-liufan-2016-04-26-end*/
    }

    private void applyNotificationIconsTint() {
		/*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-Start
		for (int i = 0; i < mStatusIconsKeyguard.getChildCount(); i++) {
            StatusBarIconView v = (StatusBarIconView) mStatusIconsKeyguard.getChildAt(i);
            if(colorFlag){
				if(textColor == 0){
					v.setImageTintList(ColorStateList.valueOf(PhoneStatusBar.LOCK_DARK_COLOR));
				}else{
					v.setImageTintList(ColorStateList.valueOf(PhoneStatusBar.LOCK_LIGHT_COLOR));
				}
			}
        }
		/*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-end*/
		
		/*PRIZE-cancel the colorTint-liufan-2016-04-26-start*/
        /*for (int i = 0; i < mNotificationIcons.getChildCount(); i++) {
            StatusBarIconView v = (StatusBarIconView) mNotificationIcons.getChildAt(i);
            boolean isPreL = Boolean.TRUE.equals(v.getTag(R.id.icon_is_pre_L));
            boolean colorize = !isPreL || isGrayscale(v);
            if (colorize) {
                v.setImageTintList(ColorStateList.valueOf(mIconTint));
            }
        }*/
		/*PRIZE-cancel the colorTint-liufan-2016-04-26-end*/
    }

    public void appTransitionPending() {
        mTransitionPending = true;
    }

    public void appTransitionCancelled() {
        if (mTransitionPending && mTintChangePending) {
            mTintChangePending = false;
            animateIconTint(mPendingDarkIntensity, 0 /* delay */, DEFAULT_TINT_ANIMATION_DURATION);
        }
        mTransitionPending = false;
    }

    public void appTransitionStarting(long startTime, long duration) {
        if (mTransitionPending && mTintChangePending) {
            mTintChangePending = false;
            animateIconTint(mPendingDarkIntensity,
                    Math.max(0, startTime - SystemClock.uptimeMillis()),
                    duration);

        } else if (mTransitionPending) {

            // If we don't have a pending tint change yet, the change might come in the future until
            // startTime is reached.
            mTransitionDeferring = true;
            mTransitionDeferringStartTime = startTime;
            mTransitionDeferringDuration = duration;
            mHandler.removeCallbacks(mTransitionDeferringDoneRunnable);
            mHandler.postAtTime(mTransitionDeferringDoneRunnable, startTime);
        }
        mTransitionPending = false;
    }

    public static ArraySet<String> getIconBlacklist(String blackListStr) {
        ArraySet<String> ret = new ArraySet<String>();
        if (blackListStr == null) {
            /// M: enable headset icon.
            //blackListStr = "rotate,headset";
            blackListStr = "rotate,";
        }
        String[] blacklist = blackListStr.split(",");
        for (String slot : blacklist) {
            if (!TextUtils.isEmpty(slot)) {
                ret.add(slot);
            }
        }
        return ret;
    }

    public void onDensityOrFontScaleChanged() {
        loadDimens();
        mNotificationIconAreaController.onDensityOrFontScaleChanged(mContext);
        updateClock();
        for (int i = 0; i < mStatusIcons.getChildCount(); i++) {
            View child = mStatusIcons.getChildAt(i);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, mIconSize);
            lp.setMargins(mIconHPadding, 0, mIconHPadding, 0);
            child.setLayoutParams(lp);
        }
        for (int i = 0; i < mStatusIconsKeyguard.getChildCount(); i++) {
            View child = mStatusIconsKeyguard.getChildAt(i);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, mIconSize);
            child.setLayoutParams(lp);
        }
        scaleBatteryMeterViews(mContext);
    }

    private void updateClock() {
        FontSizeUtils.updateFontSize(mClock, R.dimen.status_bar_clock_size);
        mClock.setPaddingRelative(
                mContext.getResources().getDimensionPixelSize(
                        R.dimen.status_bar_clock_starting_padding),
                0,
                mContext.getResources().getDimensionPixelSize(
                        R.dimen.status_bar_clock_end_padding),
                0);
    }
	
	
	//add for statusbar inverse. prize-linkh-20150903
    public void setStatusIconForStyle(StatusBarIconView view, boolean update, boolean force) {        
        Log.d(TAG, "setStatusIconForStyle() view " + view + ", update=" + update + ", force=" + force);        
        if(view == null) {
            return;
        }

        StatusBarIcon sbIcon = view.getStatusBarIcon();
        if(sbIcon == null) {
            return;
        }

        if(!force && sbIcon.visible != true) {
            return;
        }

         String slot = view.getSlot();
         if(slot == null) {
             return;
         }

         if(false) {
            view.setColorFilter(0xff686868, PorterDuff.Mode.MULTIPLY);
         } else {
             Icon defaultIcon = view.mDefaultIcon;
             int iconId = PrizeStatusBarStyle.getInstance(mContext).getStatusIcon(mCurStatusBarStyle, slot, defaultIcon.getResId());
             Log.d(TAG, "mCurStatusBarStyle=" + mCurStatusBarStyle + ", slot=" +  slot);
             Log.d(TAG, "new iconId=" + Integer.toHexString(iconId) + ", defaultIconId=" +  Integer.toHexString(defaultIcon.getResId()));
             if(iconId > 0) {
                 sbIcon.icon = Icon.createWithResource(mContext, iconId);
                 view.isSystemUIIcon = true;
                 
                 if(update) {
                     Log.d(TAG, "update status icon view..");        
                     view.updateDrawable();
                 }
             } else {
                 view.isSystemUIIcon = false;
             }
        }
    }

    public void setKeyguardStatusIconForStyle(StatusBarIconView view, boolean update, boolean force) {
        Log.d(TAG, "setKeyguardStatusIconForStyle() view " + view + ", update=" + update + ", force=" + force);
        if(!PrizeOption.PRIZE_LOCKSCREEN_INVERSE){
            return ;
        }
        if(view == null) {
            return;
        }

        StatusBarIcon sbIcon = view.getStatusBarIcon();
        if(sbIcon == null) {
            return;
        }

        if(!force && sbIcon.visible != true) {
            return;
        }

         String slot = view.getSlot();
         if(slot == null) {
             return;
         }

         if(false) {
            view.setColorFilter(0xff686868, PorterDuff.Mode.MULTIPLY);
         } else {
             Icon defaultIcon = view.mDefaultIcon;
             int iconId = PrizeStatusBarStyle.getInstance(mContext).getStatusIcon(textColor == 0 ? 1 : 0, slot, defaultIcon.getResId());
             if(iconId > 0) {
                 sbIcon.icon = Icon.createWithResource(mContext, iconId);
                 view.isSystemUIIcon = true;
                 if(update) {
                     Log.d(TAG, "update status icon view..");
                     view.updateDrawable();
                 }
             } else {
                 view.isSystemUIIcon = false;
             }
        }
    }

    public void updateViewsForKeyguardStatusBarStyleChanged() {
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            if(mLiuHaiIconControllerList != null){
                mLiuHaiIconControllerList.get(1).onStatusBarStyleChanged(textColor == 0 ? 1 : 0);
            }
            return ;
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
        if(mStatusIconsKeyguard != null) {
            int count = mStatusIconsKeyguard.getChildCount();
            Log.d(TAG, "mStatusIconsKeyguard.count=" + count);
            for(int i = 0; i < count; ++i) {
                StatusBarIconView child = (StatusBarIconView)mStatusIconsKeyguard.getChildAt(i);
                setKeyguardStatusIconForStyle(child, true, false);
            }
        }
	
	}

    private int mCurStatusBarStyle = StatusBarManager.STATUS_BAR_INVERSE_DEFALUT;
    @Override
    public void onStatusBarStyleChanged(int style) {
        Log.d(TAG, "onStatusBarStyleChanged(). curStyle=" + mCurStatusBarStyle + ", newStyle=" + style);
        if(mCurStatusBarStyle != style) {
            mCurStatusBarStyle = style;
			
            /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
            if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
                if(mLiuHaiIconControllerList != null){
                    mLiuHaiIconControllerList.get(0).onStatusBarStyleChanged(style);
                }

                int color = StatusBarManager.STATUS_BAR_COLOR_WHITE;
                if(mCurStatusBarStyle != 0){
                    color = StatusBarManager.STATUS_BAR_COLOR_GRAY;
                }
                if(mLiuHaiClock != null){
                    mLiuHaiClock.setTextColor(color);
                }

                if(mLiuHaiBatteryView != null){
                    mLiuHaiBatteryView.onStatusBarStyleChanged(style);
                }
            }
            /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
			
            /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
            if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
                if(mLiuHaiStatusBarIconController != null && mLiuHaiStatusBarIconController.isAllowInverse()){
                    LiuHaiStatusBarIconController2.InverseController inverseController
                        = mLiuHaiStatusBarIconController.getInverseController();
                    inverseController.inverse(mCurStatusBarStyle);
                }
            }
            /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        }
    }
	//end...
	
	/*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-Start*/
	public int textColor = 0;
	public boolean colorFlag = false;
	public void setTextColor(int color, boolean flag){
	    if(!PrizeOption.PRIZE_LOCKSCREEN_INVERSE){
		    return ;
		}
		textColor = color;
		colorFlag = flag;
		//applyNotificationIconsTint();
        updateViewsForKeyguardStatusBarStyleChanged();
	}
	/*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-end*/
}
