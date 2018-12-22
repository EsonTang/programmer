/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.keyguard.CarrierText;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;

import java.text.NumberFormat;
//import package liyao 20150629
import android.provider.Settings;
import com.mediatek.common.prizeoption.PrizeOption;
//Add for backgroud_reflect-luyingying-2017-09-09-Start*/
import com.android.systemui.BatteryMeterViewDefinedNew;
import com.android.systemui.statusbar.AlphaOptimizedImageView;
import com.android.systemui.statusbar.SignalClusterView;
import android.content.res.ColorStateList;
import android.view.ViewGroup;
import android.app.StatusBarManager;
//Add end

/*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
import android.widget.LinearLayout;
/*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

/**
 * The header group on Keyguard.
 */
public class KeyguardStatusBarView extends RelativeLayout
        implements BatteryController.BatteryStateChangeCallback {

    private static final String TAG = "KeyguardStatusBarView";
    private boolean mBatteryCharging;
    private boolean mKeyguardUserSwitcherShowing;
    private boolean mBatteryListening;

    // private TextView mCarrierLabel;
    private CarrierText mCarrierLabel;
    private View mSystemIconsSuperContainer;
    private MultiUserSwitch mMultiUserSwitch;
    private ImageView mMultiUserAvatar;
    private TextView mBatteryLevel;

    //prize add by xiarui for Bug#43623 2017-12-14 @{
    private TextView mNetworkSpeedTxt;
    //@}

    private BatteryController mBatteryController;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private UserSwitcherController mUserSwitcherController;

    private int mSystemIconsSwitcherHiddenExpandedMargin;

    private int mSystemIconsBaseMargin;
    private View mSystemIconsContainer;

    /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
    private LinearLayout mLiuHaiKeyguardArea;
    /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

    /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
    private LinearLayout mLiuHaiKeyguardArea2;
    /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/

    public KeyguardStatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSystemIconsSuperContainer = findViewById(R.id.system_icons_super_container);
        mSystemIconsContainer = findViewById(R.id.system_icons_container);
        mMultiUserSwitch = (MultiUserSwitch) findViewById(R.id.multi_user_switch);
        mMultiUserAvatar = (ImageView) findViewById(R.id.multi_user_avatar);
        /*PRIZE-dismiss MultiUserSwitch- liufan-2015-05-28-start*/
        mMultiUserSwitch.setVisibility(View.GONE);
        /*PRIZE-dismiss MultiUserSwitch- liufan-2015-05-28-end*/
        mBatteryLevel = (TextView) findViewById(R.id.battery_level);
        // mCarrierLabel = (TextView) findViewById(R.id.keyguard_carrier_text);
        mCarrierLabel = (CarrierText) findViewById(R.id.keyguard_carrier_text);
        // Respect font size setting.
        mCarrierLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimensionPixelSize(
                        //com.android.internal.R.dimen.text_size_small_material));
                        R.dimen.status_bar_text_size_14));//update by liufan -2018-03-02

        //prize add by xiarui for Bug#43623 2017-12-14 @{
        mNetworkSpeedTxt = (TextView) findViewById(R.id.network_speed_prize);
        //@}

        
        loadDimens();
        updateUserSwitcher();
        /*PRIZE-battery and battery_level show- liufan-2015-10-30-start*/
        if(PrizeOption.PRIZE_SYSTEMUI_BATTERY_METER){
            boolean showLevel =  Settings.System.getInt(mContext.getContentResolver(),
                    "battery_percentage_enabled", 0) == 1;
            mBatteryLevel.setVisibility(showLevel ? View.VISIBLE : View.GONE);
            (findViewById(R.id.battery_new)).setVisibility( View.VISIBLE );
            (findViewById(R.id.battery)).setVisibility(View.GONE);
        }else{
            mBatteryLevel.setVisibility(View.GONE);
            (findViewById(R.id.battery_new)).setVisibility(View.GONE);
            (findViewById(R.id.battery)).setVisibility(View.VISIBLE);
        }
        /*PRIZE-battery and battery_level show- liufan-2015-10-30-end*/

        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        mLiuHaiKeyguardArea = (LinearLayout) findViewById(R.id.prize_liuhai_area);
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            mCarrierLabel.setVisibility(View.GONE);
            mSystemIconsSuperContainer.setVisibility(View.GONE);
            mLiuHaiKeyguardArea.setVisibility(View.VISIBLE);

            TextView clock = (TextView)mLiuHaiKeyguardArea.findViewById(R.id.clock_liuhai);
            if(clock != null) clock.setVisibility(View.GONE);

            MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
            lp.height =  getResources().getDimensionPixelSize(
                    com.android.internal.R.dimen.status_bar_height);
            lp.setMargins(45, 0, 45, 0);
            PhoneStatusBar.debugLiuHai("keyguard status bar height = " + lp.height);
        } else {
            mLiuHaiKeyguardArea.setVisibility(View.GONE);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        mLiuHaiKeyguardArea2 = (LinearLayout) findViewById(R.id.prize_liuhai_area2);
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            mCarrierLabel.setVisibility(View.GONE);
            mSystemIconsSuperContainer.setVisibility(View.GONE);
            mLiuHaiKeyguardArea2.setVisibility(View.VISIBLE);

            TextView clock = (TextView)mLiuHaiKeyguardArea2.findViewById(R.id.clock_liuhai);
            if(clock != null) clock.setVisibility(View.GONE);

            MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
            lp.height =  getResources().getDimensionPixelSize(
                    com.android.internal.R.dimen.status_bar_height);
            lp.setMargins(18, 0, 18, 0);
            setLayoutParams(lp);
            PhoneStatusBar.debugLiuHai("keyguard status bar height = " + lp.height);
        } else {
            mLiuHaiKeyguardArea2.setVisibility(View.GONE);
        }
        if(!PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            mCarrierLabel.setVisibility(View.VISIBLE);
            mSystemIconsSuperContainer.setVisibility(View.VISIBLE);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        MarginLayoutParams lp = (MarginLayoutParams) mMultiUserAvatar.getLayoutParams();
        lp.width = lp.height = getResources().getDimensionPixelSize(
                R.dimen.multi_user_avatar_keyguard_size);
        mMultiUserAvatar.setLayoutParams(lp);

        lp = (MarginLayoutParams) mMultiUserSwitch.getLayoutParams();
        lp.width = getResources().getDimensionPixelSize(
                R.dimen.multi_user_switch_width_keyguard);
        lp.setMarginEnd(getResources().getDimensionPixelSize(
                R.dimen.multi_user_switch_keyguard_margin));
        mMultiUserSwitch.setLayoutParams(lp);

        lp = (MarginLayoutParams) mSystemIconsSuperContainer.getLayoutParams();
        lp.height = getResources().getDimensionPixelSize(
                R.dimen.status_bar_header_height);
        lp.setMarginStart(getResources().getDimensionPixelSize(
                R.dimen.system_icons_super_container_margin_start));
        mSystemIconsSuperContainer.setLayoutParams(lp);
        mSystemIconsSuperContainer.setPaddingRelative(mSystemIconsSuperContainer.getPaddingStart(),
                mSystemIconsSuperContainer.getPaddingTop(),
                getResources().getDimensionPixelSize(R.dimen.system_icons_keyguard_padding_end),
                mSystemIconsSuperContainer.getPaddingBottom());

        lp = (MarginLayoutParams) mSystemIconsContainer.getLayoutParams();
        lp.height = getResources().getDimensionPixelSize(
                R.dimen.status_bar_height);
        mSystemIconsContainer.setLayoutParams(lp);

        lp = (MarginLayoutParams) mBatteryLevel.getLayoutParams();
        /*PRIZE-add for bugid:51725- liufan-2018-03-05-start*/
        /*lp.setMarginStart(
                getResources().getDimensionPixelSize(R.dimen.header_battery_margin_keyguard));*/
        /*PRIZE-add for bugid:51725- liufan-2018-03-05-end*/
        mBatteryLevel.setLayoutParams(lp);
        mBatteryLevel.setPaddingRelative(mBatteryLevel.getPaddingStart(),
                mBatteryLevel.getPaddingTop(),
                getResources().getDimensionPixelSize(R.dimen.battery_level_padding_end),
                mBatteryLevel.getPaddingBottom());
        mBatteryLevel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimensionPixelSize(R.dimen.battery_level_text_size));

        lp = (MarginLayoutParams) mCarrierLabel.getLayoutParams();
        lp.setMarginStart(
                getResources().getDimensionPixelSize(R.dimen.keyguard_carrier_text_margin));
        mCarrierLabel.setLayoutParams(lp);

        lp = (MarginLayoutParams) getLayoutParams();
        /*PRIZE-update for liuhai screen-liufan-2018-04-09-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN && !PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            lp.height =  getResources().getDimensionPixelSize(
                    com.android.internal.R.dimen.status_bar_height);
            lp.setMargins(45, 0, 45, 0);
            PhoneStatusBar.debugLiuHai("keyguard status bar height = " + lp.height);
        } else {
            lp.height =  getResources().getDimensionPixelSize(
                    R.dimen.status_bar_header_height_keyguard);
        }
        /*PRIZE-update for liuhai screen-liufan-2018-04-09-end*/

        /*PRIZE-update for liuhai screen-liufan-2018-06-25-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            lp = (MarginLayoutParams) getLayoutParams();
            lp.height =  getResources().getDimensionPixelSize(
                    com.android.internal.R.dimen.status_bar_height);
            lp.setMargins(18, 0, 18, 0);
            PhoneStatusBar.debugLiuHai("keyguard status bar height = " + lp.height);
            setLayoutParams(lp);
        }
        /*PRIZE-update for liuhai screen-liufan-2018-06-25-end*/
        setLayoutParams(lp);
        /**PRIZE update the percent of battery liyao 2015-07-01 start */
        if(!PrizeOption.PRIZE_SYSTEMUI_BATTERY_METER){
            mBatteryLevel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimensionPixelSize(R.dimen.battery_level_text_size));
        } else{
            mBatteryLevel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimensionPixelSize(R.dimen.status_bar_clock_size));
        }
        /**PRIZE update the percent of battery liyao 2015-07-01 end */
    }

    private void loadDimens() {
        Resources res = getResources();
        mSystemIconsSwitcherHiddenExpandedMargin = res.getDimensionPixelSize(
                R.dimen.system_icons_switcher_hidden_expanded_margin);
        mSystemIconsBaseMargin = res.getDimensionPixelSize(
                R.dimen.system_icons_super_container_avatarless_margin_end);
    }

    private void updateVisibilities() {
        if (mMultiUserSwitch.getParent() != this && !mKeyguardUserSwitcherShowing) {
            if (mMultiUserSwitch.getParent() != null) {
                getOverlay().remove(mMultiUserSwitch);
            }
            addView(mMultiUserSwitch, 0);
        } else if (mMultiUserSwitch.getParent() == this && mKeyguardUserSwitcherShowing) {
            removeView(mMultiUserSwitch);
        }
        if (mKeyguardUserSwitcher == null) {
            // If we have no keyguard switcher, the screen width is under 600dp. In this case,
            // we don't show the multi-user avatar unless there is more than 1 user on the device.
            if (mUserSwitcherController != null
                    && mUserSwitcherController.getSwitchableUserCount() > 1) {
                mMultiUserSwitch.setVisibility(View.VISIBLE);
            } else {
                mMultiUserSwitch.setVisibility(View.GONE);
            }
        }
        /**PRIZE-update the percent of battery-liufan-2015-10-30-start */
        //if(!PrizeOption.PRIZE_SYSTEMUI_BATTERY_METER){ mBatteryLevel.setVisibility(mBatteryCharging ? View.VISIBLE : View.GONE);}
        /**PRIZE-update the percent of battery-liufan-2015-10-30-end */
        
    }

    private void updateSystemIconsLayoutParams() {
        RelativeLayout.LayoutParams lp =
                (LayoutParams) mSystemIconsSuperContainer.getLayoutParams();
        // If the avatar icon is gone, we need to have some end margin to display the system icons
        // correctly.
        int baseMarginEnd = mMultiUserSwitch.getVisibility() == View.GONE
                ? mSystemIconsBaseMargin
                : 0;
        int marginEnd = mKeyguardUserSwitcherShowing ? mSystemIconsSwitcherHiddenExpandedMargin :
                baseMarginEnd;
        if (marginEnd != lp.getMarginEnd()) {
            lp.setMarginEnd(marginEnd);
            mSystemIconsSuperContainer.setLayoutParams(lp);
        }
    }

    public void setListening(boolean listening) {
        if (listening == mBatteryListening) {
            return;
        }
        mBatteryListening = listening;
        if (mBatteryListening) {
            mBatteryController.addStateChangedCallback(this);
        } else {
            mBatteryController.removeStateChangedCallback(this);
        }
    }

    private void updateUserSwitcher() {
        boolean keyguardSwitcherAvailable = mKeyguardUserSwitcher != null;
        mMultiUserSwitch.setClickable(keyguardSwitcherAvailable);
        mMultiUserSwitch.setFocusable(keyguardSwitcherAvailable);
        mMultiUserSwitch.setKeyguardMode(keyguardSwitcherAvailable);
    }

    public void setBatteryController(BatteryController batteryController) {
        mBatteryController = batteryController;

        /**PRIZE update the battery icon liyao 2015-06-25 start */
        if(!PrizeOption.PRIZE_SYSTEMUI_BATTERY_METER){
            ((BatteryMeterView) findViewById(R.id.battery)).setBatteryController(batteryController);
        }
        /**PRIZE update the battery icon liyao 2015-06-25 end */

    }

    public void setUserSwitcherController(UserSwitcherController controller) {
        mUserSwitcherController = controller;
        mMultiUserSwitch.setUserSwitcherController(controller);
    }

    public void setUserInfoController(UserInfoController userInfoController) {
        userInfoController.addListener(new UserInfoController.OnUserInfoChangedListener() {
            @Override
            public void onUserInfoChanged(String name, Drawable picture) {
                Log.d(TAG,"onUserInfoChanged and set new profile icon");
                mMultiUserAvatar.setImageDrawable(picture);
            }
        });
    }

    public void setQSPanel(QSPanel qsp) {
        mMultiUserSwitch.setQsPanel(qsp);
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        String percentage = NumberFormat.getPercentInstance().format((double) level / 100.0);
        mBatteryLevel.setText(percentage);
        boolean changed = mBatteryCharging != charging;
        mBatteryCharging = charging;
        if (changed) {
            updateVisibilities();
        }
        /**PRIZE-update the percent of battery-liufan-2015-10-30-start */
        if(PrizeOption.PRIZE_SYSTEMUI_BATTERY_METER){
            boolean showLevel =  Settings.System.getInt(mContext.getContentResolver(),
                    "battery_percentage_enabled", 0) == 1;
            mBatteryLevel.setVisibility(showLevel ? View.VISIBLE : View.GONE);
            (findViewById(R.id.battery_new)).setVisibility( View.VISIBLE );
            (findViewById(R.id.battery)).setVisibility(View.GONE);
        }
        /**PRIZE-update the percent of battery-liufan-2015-10-30-end */
    }

    @Override
    public void onPowerSaveChanged(boolean isPowerSave) {
        // could not care less
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        mKeyguardUserSwitcher = keyguardUserSwitcher;
        mMultiUserSwitch.setKeyguardUserSwitcher(keyguardUserSwitcher);
        updateUserSwitcher();
    }

    public void setKeyguardUserSwitcherShowing(boolean showing, boolean animate) {
        mKeyguardUserSwitcherShowing = showing;
        if (animate) {
            animateNextLayoutChange();
        }
        updateVisibilities();
        updateSystemIconsLayoutParams();
    }

    private void animateNextLayoutChange() {
        final int systemIconsCurrentX = mSystemIconsSuperContainer.getLeft();
        final boolean userSwitcherVisible = mMultiUserSwitch.getParent() == this;
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                boolean userSwitcherHiding = userSwitcherVisible
                        && mMultiUserSwitch.getParent() != KeyguardStatusBarView.this;
                mSystemIconsSuperContainer.setX(systemIconsCurrentX);
                mSystemIconsSuperContainer.animate()
                        .translationX(0)
                        .setDuration(400)
                        .setStartDelay(userSwitcherHiding ? 300 : 0)
                        .setInterpolator(Interpolators.FAST_OUT_SLOW_IN)
                        .start();
                if (userSwitcherHiding) {
                    getOverlay().add(mMultiUserSwitch);
                    mMultiUserSwitch.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .setStartDelay(0)
                            .setInterpolator(Interpolators.ALPHA_OUT)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    mMultiUserSwitch.setAlpha(1f);
                                    getOverlay().remove(mMultiUserSwitch);
                                }
                            })
                            .start();

                } else {
                    mMultiUserSwitch.setAlpha(0f);
                    mMultiUserSwitch.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .setStartDelay(200)
                            .setInterpolator(Interpolators.ALPHA_IN);
                }
                return true;
            }
        });

    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != View.VISIBLE) {
            mSystemIconsSuperContainer.animate().cancel();
            mSystemIconsSuperContainer.setTranslationX(0);
            mMultiUserSwitch.animate().cancel();
            mMultiUserSwitch.setAlpha(1f);
        } else {
            updateVisibilities();
            updateSystemIconsLayoutParams();
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
    
    /*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-Start*/
    public static int textColor = 0;
    public void setTextColor(int color){
        if(!PrizeOption.PRIZE_LOCKSCREEN_INVERSE){
            return ;
        }
        textColor = color;
        if (textColor == 0){
            mBatteryLevel.setTextColor(ColorStateList.valueOf(PhoneStatusBar.LOCK_DARK_COLOR));
            mCarrierLabel.setTextColor(PhoneStatusBar.LOCK_DARK_COLOR);
            mNetworkSpeedTxt.setTextColor(PhoneStatusBar.LOCK_DARK_COLOR);
        } else {
            mBatteryLevel.setTextColor(ColorStateList.valueOf(PhoneStatusBar.LOCK_LIGHT_COLOR));
            mCarrierLabel.setTextColor(PhoneStatusBar.LOCK_LIGHT_COLOR);
            mNetworkSpeedTxt.setTextColor(PhoneStatusBar.LOCK_LIGHT_COLOR);
        }
    }
    /*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-end*/


    /*prize add by xiarui for Bug#43623 2017-12-14 start*/

    public void updateNetworkSpeed(int visibility, CharSequence text) {
        if (mNetworkSpeedTxt != null) {
            mNetworkSpeedTxt.setVisibility(visibility);
            mNetworkSpeedTxt.setText(text);
        }
    }
    /*prize add by xiarui for Bug#43623 2017-12-14 end*/
}
