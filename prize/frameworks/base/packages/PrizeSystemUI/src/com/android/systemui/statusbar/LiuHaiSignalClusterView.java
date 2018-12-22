/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.systemui.statusbar;

import android.annotation.DrawableRes;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.NetworkController.IconState;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;

import com.mediatek.systemui.ext.ISystemUIStatusBarExt;
import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.statusbar.util.FeatureOptions;

import java.util.ArrayList;
import java.util.List;
//导包 liyao 20150629
import com.android.systemui.statusbar.phone.FeatureOption;
import android.widget.TextView;
import com.android.systemui.FontSizeUtils;
import android.content.res.Configuration;
import com.mediatek.systemui.statusbar.networktype.NetworkTypeUtils;
import android.telephony.SubscriptionManager;
//add for statusbar inverse. prize-linkh-20150903
import com.android.systemui.statusbar.phone.PrizeStatusBarStyleListener;
import android.app.StatusBarManager;
import com.android.systemui.statusbar.phone.PrizeStatusBarStyle;
import com.mediatek.common.prizeoption.PrizeOption;
import android.telephony.TelephonyManager;
import com.android.systemui.statusbar.policy.TelephonyIcons;
import com.android.systemui.statusbar.policy.WifiIcons;
//end...
/*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
import com.android.systemui.statusbar.phone.PhoneStatusBar;
/*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

// Intimately tied to the design of res/layout/signal_cluster_view.xml
public class LiuHaiSignalClusterView
        extends SignalClusterView
        implements NetworkControllerImpl.SignalCallback,
        SecurityController.SecurityControllerCallback, Tunable {

    static final String TAG = "LiuHaiSignalClusterView";
    static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private static final String SLOT_AIRPLANE = "airplane";
    private static final String SLOT_MOBILE = "mobile";
    private static final String SLOT_WIFI = "wifi";
    private static final String SLOT_ETHERNET = "ethernet";

    NetworkControllerImpl mNC;
    SecurityController mSC;

    private boolean mNoSimsVisible = false;
    private boolean mVpnVisible = false;
    private int mVpnIconId = 0;
    private int mLastVpnIconId = -1;
    private boolean mEthernetVisible = false;
    private int mEthernetIconId = 0;
    private int mLastEthernetIconId = -1;
    private boolean mWifiVisible = false;
    private int mWifiStrengthId = 0;
    private int mLastWifiStrengthId = -1;
    private boolean mIsAirplaneMode = false;
    private int mAirplaneIconId = 0;
    private int mLastAirplaneIconId = -1;
    private String mAirplaneContentDescription;
    private String mWifiDescription;
    private String mEthernetDescription;
    private ArrayList<PhoneState> mPhoneStates = new ArrayList<PhoneState>();
    private int mIconTint = Color.WHITE;
    private float mDarkIntensity;
    private final Rect mTintArea = new Rect();

    ViewGroup mEthernetGroup, mWifiGroup;
    View mNoSimsCombo;
    ImageView /*mVpn,*/ mEthernet, mWifi, mAirplane, mNoSims, mEthernetDark, mWifiDark, mNoSimsDark;
    //View mWifiAirplaneSpacer;
    //View mWifiSignalSpacer;
    LinearLayout mMobileSignalGroup;

    private final int mMobileSignalGroupEndPadding;
    private final int mMobileDataIconStartPadding;
    private final int mWideTypeIconStartPadding;
    private final int mSecondaryTelephonyPadding;
    private final int mEndPadding;
    private final int mEndPaddingNothingVisible;
    private final float mIconScaleFactor;

    private boolean mBlockAirplane;
    private boolean mBlockMobile;
    private boolean mBlockWifi;
    private boolean mBlockEthernet;



    /// M: Add for Plugin feature @ {
    private ISystemUIStatusBarExt mStatusBarExt;
    /*PRIZE 修改状态栏wifi图标(信号格数和数据交互) liyao 20150615 start*/
    private int  mWifiActivityId = 0;
    ImageView mWifiActivity;
    /*PRIZE 修改状态栏wifi图标(信号格数和数据交互) liyao 20150615 end*/
    /*PRIZE 无SIM卡定制显示 liyao 20150629 start*/
    ImageView mNoSimsPrize;
    /*PRIZE 无SIM卡定制显示 liyao 20150629 end*/
    /// @ }

    /// M: for vowifi
    boolean mIsWfcEnable;

    public LiuHaiSignalClusterView(Context context) {
        this(context, null);
    }

    public LiuHaiSignalClusterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiuHaiSignalClusterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Resources res = getResources();
        mMobileSignalGroupEndPadding =
                res.getDimensionPixelSize(R.dimen.mobile_signal_group_end_padding);
        mMobileDataIconStartPadding =
                res.getDimensionPixelSize(R.dimen.mobile_data_icon_start_padding);
        mWideTypeIconStartPadding = res.getDimensionPixelSize(R.dimen.wide_type_icon_start_padding);
        mSecondaryTelephonyPadding = res.getDimensionPixelSize(R.dimen.secondary_telephony_padding);
        mEndPadding = res.getDimensionPixelSize(R.dimen.signal_cluster_battery_padding);
        mEndPaddingNothingVisible = res.getDimensionPixelSize(
                R.dimen.no_signal_cluster_battery_padding);

        TypedValue typedValue = new TypedValue();
        res.getValue(R.dimen.status_bar_icon_scale_factor, typedValue, true);
        mIconScaleFactor = typedValue.getFloat();

        /// M: Add for Plugin feature @ {
        mStatusBarExt = PluginManager.getSystemUIStatusBarExt(context);
        /// @ }
        mIsWfcEnable = SystemProperties.get("persist.mtk_wfc_support").equals("1");
    }

    @Override
    public void onTuningChanged(String key, String newValue) {
        if (!StatusBarIconController.ICON_BLACKLIST.equals(key)) {
            return;
        }
        ArraySet<String> blockList = StatusBarIconController.getIconBlacklist(newValue);
        boolean blockAirplane = blockList.contains(SLOT_AIRPLANE);
        boolean blockMobile = blockList.contains(SLOT_MOBILE);
        boolean blockWifi = blockList.contains(SLOT_WIFI);
        boolean blockEthernet = blockList.contains(SLOT_ETHERNET);

        if (blockAirplane != mBlockAirplane || blockMobile != mBlockMobile
                || blockEthernet != mBlockEthernet || blockWifi != mBlockWifi) {
            mBlockAirplane = blockAirplane;
            mBlockMobile = blockMobile;
            mBlockEthernet = blockEthernet;
            mBlockWifi = blockWifi;
            // Re-register to get new callbacks.
            mNC.removeSignalCallback(this);
            mNC.addSignalCallback(this);
        }
    }

    public void setNetworkController(NetworkControllerImpl nc) {
        if (DEBUG) Log.d(TAG, "NetworkController=" + nc);
        mNC = nc;
    }

    public void setSecurityController(SecurityController sc) {
        if (DEBUG) Log.d(TAG, "SecurityController=" + sc);
        mSC = sc;
        mSC.addCallback(this);
        mVpnVisible = mSC.isVpnEnabled();
        mVpnIconId = currentVpnIconId(mSC.isVpnBranded());
    }

    @Override
    protected void onFinishInflate() {
        //super.onFinishInflate();

        //mVpn            = (ImageView) findViewById(R.id.vpn);
        mEthernetGroup  = (ViewGroup) findViewById(R.id.ethernet_combo);
        mEthernet       = (ImageView) findViewById(R.id.ethernet);
        mEthernetDark   = (ImageView) findViewById(R.id.ethernet_dark);
        mWifiGroup      = (ViewGroup) findViewById(R.id.wifi_combo);
        mWifi           = (ImageView) findViewById(R.id.wifi_signal);
        mWifiDark       = (ImageView) findViewById(R.id.wifi_signal_dark);
        mAirplane       = (ImageView) findViewById(R.id.airplane);
        mNoSims         = (ImageView) findViewById(R.id.no_sims);
        mNoSimsDark     = (ImageView) findViewById(R.id.no_sims_dark);
        mNoSimsCombo    =             findViewById(R.id.no_sims_combo);
        //mWifiAirplaneSpacer =         findViewById(R.id.wifi_airplane_spacer);
        //mWifiSignalSpacer =           findViewById(R.id.wifi_signal_spacer);
        mMobileSignalGroup = (LinearLayout) findViewById(R.id.mobile_signal_group);

        maybeScaleVpnAndNoSimsIcons();
		
        /*PRIZE uodate wifi icon liyao 20150615 start*/
        mWifiActivity   = (ImageView) findViewById(R.id.wifi_inout);
        /*PRIZE uodate wifi icon  liyao 20150615 end*/
        /*PRIZE no sim card textview liyao 20150629 start*/
        mNoSimsPrize = (ImageView) findViewById(R.id.no_sims_prize);
        /*PRIZE no sim card textview liyao 20150629 end*/
    }

    /**
     * Extracts the icon off of the VPN and no sims views and maybe scale them by
     * {@link #mIconScaleFactor}. Note that the other icons are not scaled here because they are
     * dynamic. As such, they need to be scaled each time the icon changes in {@link #apply()}.
     */
    private void maybeScaleVpnAndNoSimsIcons() {
        if (mIconScaleFactor == 1.f) {
            return;
        }

        //mVpn.setImageDrawable(new ScalingDrawableWrapper(mVpn.getDrawable(), mIconScaleFactor));

        mNoSims.setImageDrawable(
                new ScalingDrawableWrapper(mNoSims.getDrawable(), mIconScaleFactor));
        mNoSimsDark.setImageDrawable(
                new ScalingDrawableWrapper(mNoSimsDark.getDrawable(), mIconScaleFactor));
    }

    @Override
    protected void onAttachedToWindow() {
        //super.onAttachedToWindow();

        if(!PhoneStatusBar.OPEN_LIUHAI_SCREEN){
            return ;
        }
        for (PhoneState state : mPhoneStates) {
            mMobileSignalGroup.addView(state.mMobileGroup);
        }

        int endPadding = mMobileSignalGroup.getChildCount() > 0 ? mMobileSignalGroupEndPadding : 0;
        mMobileSignalGroup.setPaddingRelative(0, 0, endPadding, 0);

        TunerService.get(mContext).addTunable(this, StatusBarIconController.ICON_BLACKLIST);

        /// M: Add for Plugin feature @ {
        mStatusBarExt.setCustomizedNoSimView(mNoSims);
        mStatusBarExt.setCustomizedNoSimView(mNoSimsDark);
        mStatusBarExt.addSignalClusterCustomizedView(mContext, this,
                indexOfChild(findViewById(R.id.mobile_signal_group)));
        /// @ }

        apply();
        applyIconTint();
        if(mNC != null) mNC.addSignalCallback(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        mMobileSignalGroup.removeAllViews();
        TunerService.get(mContext).removeTunable(this);
        if(mSC != null) mSC.removeCallback(this);
        if(mNC != null) mNC.removeSignalCallback(this);

        //super.onDetachedFromWindow();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        // Re-run all checks against the tint area for all icons
        applyIconTint();
    }

    // From SecurityController.
    @Override
    public void onStateChanged() {
        post(new Runnable() {
            @Override
            public void run() {
                mVpnVisible = mSC.isVpnEnabled();
                mVpnIconId = currentVpnIconId(mSC.isVpnBranded());
                apply();
            }
        });
    }

    /*PRIZE-update wifi-mobile data icons-liyao 20150615 start*/
    @Override
    public void setWifiIndicators(boolean enabled, IconState statusIcon, IconState statusIconGray, IconState qsIcon,
            int activityIcon, boolean activityIn, boolean activityOut, String description) {
        mWifiVisible = statusIcon.visible && !mBlockWifi;
        Log.d(TAG, "setWifiIndicators, enabled=" + enabled
            + ", mIgnoreStatusBarStyleChanged=" + mIgnoreStatusBarStyleChanged
            + ", mCurStatusBarStyle=" + mCurStatusBarStyle);
		if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && !mIgnoreStatusBarStyleChanged){
			if(mCurStatusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_WHITE){
	        	mWifiStrengthId = statusIcon.icon;
			} else if(mCurStatusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_GRAY){
	        	mWifiStrengthId = statusIconGray.icon;
			}
        	mWifiActivityId = WifiIcons.getWifiInOutIcon(mCurStatusBarStyle, activityIcon);
		}else{
        	mWifiStrengthId = statusIcon.icon;
        	//mWifiActivityId = activityIcon;
        	mWifiActivityId = WifiIcons.getWifiInOutIcon(StatusBarManager.STATUS_BAR_INVERSE_WHITE, activityIcon);
		}
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN){
            mWifiActivityId = exchangeWifiInOutIconForLiuHai(mWifiActivityId);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
        mWifiDescription = statusIcon.contentDescription;

        apply();
    }

    @Override
    public void setWifiIndicatorsForInverse(boolean enabled, IconState statusIcon, IconState statusIconGray, IconState qsIcon,
            int activityIcon, boolean activityIn, boolean activityOut, String description) {
    }

    ///M: Support[Network Type and volte on StatusBar]. Add more parameter networkType and volte .
    @Override
    public void setMobileDataIndicators(IconState statusIcon, IconState statusIconGray, IconState qsIcon, int statusType,
                int networkType, int volteIcon, int qsType, boolean activityIn, boolean activityOut,
                String typeContentDescription, String description, boolean isWide, int subId) {
        PhoneState state = getState(subId);
        if (state == null) {
            return;
        }
        state.mMobileVisible = statusIcon.visible && !mBlockMobile;
		if(PluginManager.isDefaultSystemUIStatusBarExt() && PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && !mIgnoreStatusBarStyleChanged){
			if(mCurStatusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_WHITE){
	        	state.mMobileStrengthId = statusIcon.icon;
                state.mVolteIcon = volteIcon;
			} else if(mCurStatusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_GRAY){
	        	state.mMobileStrengthId = statusIconGray.icon;
                state.mVolteIcon = getGrayVolteIcon(volteIcon);
			}
		}else{
        	state.mMobileStrengthId = statusIcon.icon;
            state.mVolteIcon = volteIcon;
		}
        state.mMobileTypeId = statusType;
        state.mMobileDescription = statusIcon.contentDescription;
        state.mMobileTypeDescription = typeContentDescription;
        state.mIsMobileTypeIconWide = statusType != 0 && isWide;
        state.mNetworkIcon = networkType;
        //state.mVolteIcon = volteIcon;

        /// M: Add for plugin features. @ {
        state.mDataActivityIn = activityIn;
        state.mDataActivityOut = activityOut;
        /// @ }

        apply();
    }

    @Override
    public void setMobileDataIndicatorsForInverse(IconState statusIcon, IconState statusIconGray, IconState qsIcon, int statusType,
                int networkType, int volteIcon, int qsType, boolean activityIn, boolean activityOut,
                String typeContentDescription, String description, boolean isWide, int subId) {
    }

    private int getGrayVolteIcon(int volteIcon){
        if(volteIcon == NetworkTypeUtils.VOLTE_ICON){
            return NetworkTypeUtils.VOLTE_ICON_GRAY;
        }
        return volteIcon;
    }

    @Override
    public void setEthernetIndicators(IconState state) {
        mEthernetVisible = state.visible && !mBlockEthernet;
        mEthernetIconId = state.icon;
        mEthernetDescription = state.contentDescription;

        apply();
    }

    @Override
    public void setNoSims(boolean show) {
        mNoSimsVisible = show && !mBlockMobile;
        // M: Bug fix ALPS02302143, in case UI need to be refreshed.
        // MR1 also add this patch
        apply();
    }

    @Override
    public void setSubs(List<SubscriptionInfo> subs) {
        if (hasCorrectSubs(subs)) {
            return;
        }
        // Clear out all old subIds.
        for (PhoneState state : mPhoneStates) {
            if (state.mMobile != null) {
                state.maybeStopAnimatableDrawable(state.mMobile);
            }
            if (state.mMobileDark != null) {
                state.maybeStopAnimatableDrawable(state.mMobileDark);
            }
        }
        mPhoneStates.clear();
        if (mMobileSignalGroup != null) {
            mMobileSignalGroup.removeAllViews();
        }

        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN){
            LiuHaiStatusBarIconController.ShowOnRightListener listener = getShowOnRightListener();
            if(listener != null) {
                listener.hideAllVolteIcon();
            }
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

        final int n = subs.size();
        for (int i = 0; i < n; i++) {
            inflatePhoneState(subs.get(i).getSubscriptionId());
        }
        if (isAttachedToWindow()) {
            applyIconTint();
        }
    }

    private boolean hasCorrectSubs(List<SubscriptionInfo> subs) {
        final int N = subs.size();
        if (N != mPhoneStates.size()) {
            return false;
        }
        for (int i = 0; i < N; i++) {
            if (mPhoneStates.get(i).mSubId != subs.get(i).getSubscriptionId()) {
                return false;
            }
            ///M: fix 2968114, if sim swap but subId has not changed, need to inflate PhoneState
            /// for op views. @ {
            if (mStatusBarExt.checkIfSlotIdChanged(subs.get(i).getSubscriptionId(),
                            subs.get(i).getSimSlotIndex())) {
                return false;
            }
            /// @ }
        }
        return true;
    }

    private PhoneState getOrInflateState(int subId) {
        PhoneState s = null;
        for (PhoneState state : mPhoneStates) {
            if (state.mSubId == subId) {
                return state;
            }
        }
        return inflatePhoneState(subId);
    }

    private PhoneState getState(int subId) {
        for (PhoneState state : mPhoneStates) {
            if (state.mSubId == subId) {
                return state;
            }
        }
        Log.e(TAG, "Unexpected subscription " + subId);
        return null;
    }

    private PhoneState inflatePhoneState(int subId) {
        PhoneState state = new PhoneState(subId, mContext);
        if (mMobileSignalGroup != null) {
            mMobileSignalGroup.addView(state.mMobileGroup);
        }
        mPhoneStates.add(state);
        return state;
    }

    @Override
    public void setIsAirplaneMode(IconState icon) {
        mIsAirplaneMode = icon.visible && !mBlockAirplane;
        mAirplaneIconId = icon.icon;
        mAirplaneContentDescription = icon.contentDescription;

        apply();
    }

    @Override
    public void setMobileDataEnabled(boolean enabled) {
        // Don't care.
    }

    @Override
    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        // Standard group layout onPopulateAccessibilityEvent() implementations
        // ignore content description, so populate manually
        if (mEthernetVisible && mEthernetGroup != null &&
                mEthernetGroup.getContentDescription() != null)
            event.getText().add(mEthernetGroup.getContentDescription());
        if (mWifiVisible && mWifiGroup != null && mWifiGroup.getContentDescription() != null)
            event.getText().add(mWifiGroup.getContentDescription());
        for (PhoneState state : mPhoneStates) {
            state.populateAccessibilityEvent(event);
        }
        return super.dispatchPopulateAccessibilityEventInternal(event);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        //super.onRtlPropertiesChanged(layoutDirection);

        if (mEthernet != null) {
            mEthernet.setImageDrawable(null);
            mEthernetDark.setImageDrawable(null);
            mLastEthernetIconId = -1;
        }

        if (mWifi != null) {
            mWifi.setImageDrawable(null);
            mWifiDark.setImageDrawable(null);
            mLastWifiStrengthId = -1;
        }
        /*PRIZE-refresh the textsize of no sim card textview- liyao-2015-07-01 start*/
        //if(PluginManager.isDefaultSystemUIStatusBarExt()) FontSizeUtils.updateFontSize(mNoSimsPrize, R.dimen.status_bar_clock_size);
        /*PRIZE-refresh the textsize of no sim card textview- liyao-2015-07-01 end*/

        for (PhoneState state : mPhoneStates) {
            if (state.mMobile != null) {
                state.maybeStopAnimatableDrawable(state.mMobile);
                state.mMobile.setImageDrawable(null);
                state.mLastMobileStrengthId = -1;
            }
            if (state.mMobileDark != null) {
                state.maybeStopAnimatableDrawable(state.mMobileDark);
                state.mMobileDark.setImageDrawable(null);
                state.mLastMobileStrengthId = -1;
            }
            if (state.mMobileType != null) {
                state.mMobileType.setImageDrawable(null);
                state.mLastMobileTypeId = -1;
            }
            /*PRIZE*refresh the data activity null(bug 699) 2015-05-25 start*/
            if (PluginManager.isDefaultSystemUIStatusBarExt() && state.mMobileActivity != null) {
                state.mMobileActivity.setImageDrawable(null);
            }
            /*PRIZE*refresh the data activity null(bug 699) 2015-05-25 end*/
        }

        if (mAirplane != null) {
            mAirplane.setImageDrawable(null);
            mLastAirplaneIconId = -1;
        }


        /*PRIZE-refresh ths status of wifi icon-liyao 20150615 start*/
        if (PluginManager.isDefaultSystemUIStatusBarExt() && mWifi != null) {
            if(mWifiActivity!=null) mWifiActivity.setImageDrawable(null);
        }
        /*PRIZE-refresh ths status of wifi icon-liyao 20150615 end*/
        /*PRIZE-reacquire the text, after exchange language-liyao-2015-06-30 start*/
        if(PluginManager.isDefaultSystemUIStatusBarExt() && mNoSimsPrize != null){
            //mNoSimsPrize.setText(R.string.stat_no_sims);
            mNoSimsPrize.setImageResource(R.drawable.liuhai_no_sims);
        }
        /*PRIZE-reacquire the text, after exchange language-liyao-2015-06-30 end*/
        apply();
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    //add for statusbar inverse. prize-linkh-20150903
    private int mCurStatusBarStyle = StatusBarManager.STATUS_BAR_INVERSE_DEFALUT;
    public boolean mIgnoreStatusBarStyleChanged = true;
    private int mCurrentVolteId = -1;
    
    @Override
    public void onStatusBarStyleChanged(int style) {
        if(inverseFlag){
            return ;
        }
        Log.d(TAG, "onStatusBarStyleChanged(). curStyle=" + mCurStatusBarStyle + ", newStyle=" + style);
        if(!mIgnoreStatusBarStyleChanged && mCurStatusBarStyle != style) {
            mCurStatusBarStyle = style;
            apply();
        }
    }
    
    @Override
    public void setIgnoreStatusBarStyleChanged(boolean ignore) {
        mIgnoreStatusBarStyleChanged = ignore;
    }

    @Override    
    public boolean shouldIgnoreStatusBarStyleChanged() {
        return mIgnoreStatusBarStyleChanged;
    }
    @Override
    public int getStatusBarStyle() {
        return mCurStatusBarStyle;
    }
    //end........
    
    // Run after each indicator change.
    private void apply() {
        if (mWifiGroup == null) return;

        /*mVpn.setVisibility(mVpnVisible ? View.VISIBLE : View.GONE);
        if (mVpnVisible) {
            if (mLastVpnIconId != mVpnIconId) {
                setIconForView(mVpn, mVpnIconId);
                mLastVpnIconId = mVpnIconId;
            }
            mVpn.setVisibility(View.VISIBLE);
        } else {
            mVpn.setVisibility(View.GONE);
        }
        if (DEBUG) Log.d(TAG, String.format("vpn: %s", mVpnVisible ? "VISIBLE" : "GONE"));*/

        if (mEthernetVisible) {
            if (mLastEthernetIconId != mEthernetIconId) {
                setIconForView(mEthernet, mEthernetIconId);
                setIconForView(mEthernetDark, mEthernetIconId);
                mLastEthernetIconId = mEthernetIconId;
            }
            mEthernetGroup.setContentDescription(mEthernetDescription);
            mEthernetGroup.setVisibility(View.VISIBLE);
        } else {
            mEthernetGroup.setVisibility(View.GONE);
        }

        if (DEBUG) Log.d(TAG,
                String.format("ethernet: %s",
                    (mEthernetVisible ? "VISIBLE" : "GONE")));

        if (mWifiVisible) {
            if (mWifiStrengthId != mLastWifiStrengthId) {
                setIconForView(mWifi, mWifiStrengthId);
                setIconForView(mWifiDark, mWifiStrengthId);
                mLastWifiStrengthId = mWifiStrengthId;
            }
            mWifiGroup.setContentDescription(mWifiDescription);
            mWifiGroup.setVisibility(View.VISIBLE);
            /*PRIZE-update wifi icon-liyao 20150615 start*/
            if(PluginManager.isDefaultSystemUIStatusBarExt()) mWifiActivity.setImageResource(mWifiActivityId);
            /*PRIZE-update wifi icon-liyao 20150615 end*/
        } else {
            mWifiGroup.setVisibility(View.GONE);
        }

        if (DEBUG) Log.d(TAG,
                String.format("wifi: %s sig=%d",
                    (mWifiVisible ? "VISIBLE" : "GONE"),
                    mWifiStrengthId));

        boolean anyMobileVisible = false;
        /// M: Support for [Network Type on Statusbar]
        /// A spacer is set between networktype and WIFI icon @ {
        if (FeatureOptions.MTK_CTA_SET) {
            anyMobileVisible = true;
        }
        /// @ }
        int firstMobileTypeId = 0;
        for (PhoneState state : mPhoneStates) {
            if (state.apply(anyMobileVisible)) {
                if (!anyMobileVisible) {
                    firstMobileTypeId = state.mMobileTypeId;
                    anyMobileVisible = true;
                }
            }
        }

        if (mIsAirplaneMode) {
             /* PRIZE-cancel mLastAirplaneIconId-liufan-2016-06-17 -start*/
            //if (mLastAirplaneIconId != mAirplaneIconId) {
                setIconForView(mAirplane, mAirplaneIconId);
	            //add for statusbar inverse. prize-linkh-20150903
	            if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && !mIgnoreStatusBarStyleChanged) {
	                int icon = TelephonyIcons.getFlightModeIcon(mCurStatusBarStyle);
                    /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
                    if(PhoneStatusBar.OPEN_LIUHAI_SCREEN){
                        icon = exchangeAirplaneIconForLiuHai(icon);
                    }
                    /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
	                mAirplane.setImageResource(icon);
	            } else {
                    /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
                    if(PhoneStatusBar.OPEN_LIUHAI_SCREEN){
                        int icon = mAirplaneIconId;
                        icon = exchangeAirplaneIconForLiuHai(icon);
                        mAirplane.setImageResource(icon);
                    }
                    /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
                }//end...
             //   mLastAirplaneIconId = mAirplaneIconId;
            //}
             /* PRIZE-cancel mLastAirplaneIconId-liufan-2016-06-17 -end*/
            mAirplane.setContentDescription(mAirplaneContentDescription);
            mAirplane.setVisibility(View.VISIBLE);
        } else {
            mAirplane.setVisibility(View.GONE);
        }

        /*if (mIsAirplaneMode && mWifiVisible) {
            mWifiAirplaneSpacer.setVisibility(View.VISIBLE);
        } else {
            mWifiAirplaneSpacer.setVisibility(View.GONE);
        }*/

        //if (((anyMobileVisible && firstMobileTypeId != 0) || mNoSimsVisible) && mWifiVisible) {
             /* PRIZE-set mWifiSignalSpacer visible when no sim card-liyao-2015-07-29 -start*/
            //if(PluginManager.isDefaultSystemUIStatusBarExt() && mNoSimsVisible) mWifiSignalSpacer.setVisibility(View.VISIBLE);
            /* PRIZE-set mWifiSignalSpacer visible when no sim card-liyao-2015-07-29 -end*/
        //} else {
        //    mWifiSignalSpacer.setVisibility(View.GONE);
        //}


        /* PRIZE-set NoSims icon GONE-liyao-2015-07-29 -start*/
        if(PluginManager.isDefaultSystemUIStatusBarExt()){
            mNoSimsCombo.setVisibility(mNoSimsVisible && !FeatureOption.PRIZE_QS_SORT ? View.VISIBLE : View.GONE);
        } else {
            mNoSimsCombo.setVisibility(mNoSimsVisible ? View.VISIBLE : View.GONE);
        }
        /* PRIZE-set NoSims icon GONE-liyao-2015-07-29 -end*/
        /// M: Add for Plugin feature @ {
        mStatusBarExt.setCustomizedNoSimsVisible(mNoSimsVisible);
        mStatusBarExt.setCustomizedAirplaneView(mNoSimsCombo, mIsAirplaneMode);
        /// @ }

        boolean anythingVisible = mNoSimsVisible || mWifiVisible || mIsAirplaneMode
                || anyMobileVisible || mVpnVisible || mEthernetVisible;
        setPaddingRelative(0, 0, anythingVisible ? mEndPadding : mEndPaddingNothingVisible, 0);
        //add for statusbar inverse. prize-linkh-20150903
        //mNoSimsPrize.setTextColor(PrizeStatusBarStyle.getInstance(getContext()).getColor(0));
        mNoSimsPrize.setImageResource(R.drawable.liuhai_no_sims);
        if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && !mIgnoreStatusBarStyleChanged) {
            int color = PrizeStatusBarStyle.getInstance(getContext()).getColor(mCurStatusBarStyle);
            //if(PluginManager.isDefaultSystemUIStatusBarExt()) mNoSimsPrize.setTextColor(color);
            if(PluginManager.isDefaultSystemUIStatusBarExt() && mCurStatusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_GRAY) {
                mNoSimsPrize.setImageResource(R.drawable.liuhai_no_sims_gray);
            }
            /*prize-add-the signal data has not changed bugid:38619-yaoshu-20170913 begin*/
			if(!PluginManager.isDefaultSystemUIStatusBarExt()){
	            for (PhoneState state : mPhoneStates) {
	                state.setViewColorFilter(color);
	            }
			}
			/*prize-add-the signal data has not changed bugid:38619-yaoshu-20170913 end*/
            if(!PluginManager.isDefaultSystemUIStatusBarExt()){
                if(mNoSims != null) setTint(mNoSims, color);
            }
        } //end...
        /*PRIZE-show no sim card textview-liyao 20150629 start*/
        if(PluginManager.isDefaultSystemUIStatusBarExt()){
            mNoSimsPrize.setVisibility(mNoSimsVisible && !mIsAirplaneMode ? View.VISIBLE : View.GONE);
        } else {
            mNoSimsPrize.setVisibility(View.GONE);
        }
        /*PRIZE-show no sim card textview-liyao 20150629 end*/
		
		
		//Add for backgroud_reflect-luyingying-2017-09-09-Start
		/*if(colorFlag){
			for (PhoneState state : mPhoneStates) {
				if(textColor == 0){
					state.setViewColorFilter(0xff000000);
				}else{
					state.setViewColorFilter(0xffffffff);
				}
			}
		}*/
		//Add end
    }

    /**
     * Sets the given drawable id on the view. This method will also scale the icon by
     * {@link #mIconScaleFactor} if appropriate.
     */
    private void setIconForView(ImageView imageView, @DrawableRes int iconId) {
        /* PRIZE-add-liufan-2016-11-14-start*/
        if(PluginManager.isDefaultSystemUIStatusBarExt() && iconId == 0){
            return ;
        }
        /* PRIZE-add-liufan-2016-11-14-end*/
        // Using the imageView's context to retrieve the Drawable so that theme is preserved.
        Drawable icon = imageView.getContext().getDrawable(iconId);

        if (mIconScaleFactor == 1.f) {
            imageView.setImageDrawable(icon);
        } else {
            imageView.setImageDrawable(new ScalingDrawableWrapper(icon, mIconScaleFactor));
        }
    }

    public void setIconTint(int tint, float darkIntensity, Rect tintArea) {
        boolean changed = tint != mIconTint || darkIntensity != mDarkIntensity
                || !mTintArea.equals(tintArea);
        mIconTint = tint;
        mDarkIntensity = darkIntensity;
        mTintArea.set(tintArea);
        if (changed && isAttachedToWindow()) {
            applyIconTint();
        }
    }

    private void applyIconTint() {
        /* PRIZE-cancel setTint-liufan-2016-06-17 -start*/
        //setTint(mVpn, StatusBarIconController.getTint(mTintArea, mVpn, mIconTint));
        //setTint(mAirplane, StatusBarIconController.getTint(mTintArea, mAirplane, mIconTint));
        /* PRIZE-cancel setTint-liufan-2016-06-17 -end*/
        /* PRIZE-cancel setTint,for bugid:54696-liufan-2018-04-09-start*/
        //applyDarkIntensity(
        //        StatusBarIconController.getDarkIntensity(mTintArea, mNoSims, mDarkIntensity),
        //        mNoSims, mNoSimsDark);
        /// M: Add for noSim view in tint mode. @{
        //mStatusBarExt.setNoSimIconTint(mIconTint, mNoSims);
        /* PRIZE-cancel setTint,for bugid:54696-liufan-2018-04-09-end*/
        /// @}

        /// M: Add for plugin items tint handling. @{
        mStatusBarExt.setCustomizedPlmnTextTint(mIconTint);
        /// @}

        applyDarkIntensity(
                StatusBarIconController.getDarkIntensity(mTintArea, mWifi, mDarkIntensity),
                mWifi, mWifiDark);
        applyDarkIntensity(
                StatusBarIconController.getDarkIntensity(mTintArea, mEthernet, mDarkIntensity),
                mEthernet, mEthernetDark);
        for (int i = 0; i < mPhoneStates.size(); i++) {
            mPhoneStates.get(i).setIconTint(mIconTint, mDarkIntensity, mTintArea);
        }
    }

    private void applyDarkIntensity(float darkIntensity, View lightIcon, View darkIcon) {
        lightIcon.setAlpha(1 - darkIntensity);
        darkIcon.setAlpha(darkIntensity);
    }

    private void setTint(ImageView v, int tint) {
        v.setImageTintList(ColorStateList.valueOf(tint));
    }
    
    private void setViewGroupColorFilter(ViewGroup vg, int color){
        int count = vg.getChildCount();
        for(int i = 0;i<count;i++){
            View v = vg.getChildAt(i);
            //Log.d("signalview","v--->"+v);
            if(v instanceof ImageView){
                ImageView iv = (ImageView)v;
                if(iv != null) setTint(iv, color);
            }
            if(v instanceof ViewGroup){
                setViewGroupColorFilter((ViewGroup)v, color);
            }
        }
    }

    private int currentVpnIconId(boolean isBranded) {
        return isBranded ? R.drawable.stat_sys_branded_vpn : R.drawable.stat_sys_vpn_ic;
    }

    private class PhoneState {
        private final int mSubId;
        private boolean mMobileVisible = false;
        private int mMobileStrengthId = 0, mMobileTypeId = 0, mNetworkIcon = 0;
        private int mVolteIcon = 0;
        private int mLastMobileStrengthId = -1;
        private int mLastMobileTypeId = -1;
        private boolean mIsMobileTypeIconWide;
        private String mMobileDescription, mMobileTypeDescription;

        private ViewGroup mMobileGroup;

        private ImageView mMobile, mMobileDark, mMobileType;

        /// M: Add for new features @ {
        // Add for [Network Type and volte on Statusbar]
        private ImageView mNetworkType;
        private ImageView mVolteType;
        private boolean mIsWfcCase;
        /// @ }

        /// M: Add for plugin features. @ {
        private boolean mDataActivityIn, mDataActivityOut;
        private ISystemUIStatusBarExt mPhoneStateExt;
        /// @ }
        /*PRIZE-data activity, up and down arrow-liyao-2015-05-25 start*/
        private int  mMobileActivityId = 0;
        private ImageView mMobileActivity;
        /*PRIZE-data activity, up and down arrow-liyao-2015-05-25 end*/
        /**PRIZE-No Service exchange String from icon-liyao-20150707-start*/
        private ImageView mNoService;
        /**PRIZE-No Service exchange String from icon-liyao-20150707-end*/

        public PhoneState(int subId, Context context) {
            ViewGroup root = null;
            /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
            if(PhoneStatusBar.OPEN_LIUHAI_SCREEN){
                root = (ViewGroup) LayoutInflater.from(context)
                            .inflate(R.layout.mobile_signal_group_ext_liuhai_prize, null);
            } else
            /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
            if(PluginManager.isDefaultSystemUIStatusBarExt()){
                root = (ViewGroup) LayoutInflater.from(context)
                            .inflate(R.layout.mobile_signal_group_ext_op_prize, null);
            } else {
                root = (ViewGroup) LayoutInflater.from(context)
                            .inflate(R.layout.mobile_signal_group_ext, null);
            }

            /// M: Add data group for plugin feature. @ {
            mPhoneStateExt = PluginManager.getSystemUIStatusBarExt(context);
            mPhoneStateExt.addCustomizedView(subId, context, root);
            /// @ }

            setViews(root);
            mSubId = subId;
        }

        public void setViews(ViewGroup root) {
            mMobileGroup    = root;
            mMobile         = (ImageView) root.findViewById(R.id.mobile_signal);
            mMobileDark     = (ImageView) root.findViewById(R.id.mobile_signal_dark);
            mMobileType     = (ImageView) root.findViewById(R.id.mobile_type);
            mNetworkType    = (ImageView) root.findViewById(R.id.network_type);
            mVolteType      = (ImageView) root.findViewById(R.id.volte_indicator_ext);
            if(PluginManager.isDefaultSystemUIStatusBarExt()){
                /*PRIZE-data activity, up and down arrow-liyao-2015-05-25 start*/
                mMobileActivity     = (ImageView) root.findViewById(R.id.mobile_inout);
                /*PRIZE-data activity, up and down arrow-liyao-2015-05-25 end*/
                /**PRIZE-No Service exchange String from icon-liyao-20150707-start*/
                mNoService = (ImageView) root.findViewById(R.id.no_service_prize);
                /**PRIZE-No Service exchange String from icon-liyao-20150707-end*/
            }
            /*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-Start
            if(colorFlag){
                Log.d("lyy", "set color");
                if(textColor == 0){
                    Log.d("lyy", "set black");
                    mMobile.setImageTintList(ColorStateList.valueOf(0xff000000));
                    mMobileDark.setImageTintList(ColorStateList.valueOf(0xff000000));
                    mMobileType.setImageTintList(ColorStateList.valueOf(0xff000000));
                    mNetworkType.setImageTintList(ColorStateList.valueOf(0xff000000));
                    mVolteType.setImageTintList(ColorStateList.valueOf(0xff000000));
                    mMobileActivity.setImageTintList(ColorStateList.valueOf(0xff000000));
                    mNoService.setTextColor(0xff000000);
                } else {
                    Log.d("lyy", "set white");
                    mMobile.setImageTintList(ColorStateList.valueOf(0xffffffff));
                    mMobileDark.setImageTintList(ColorStateList.valueOf(0xffffffff));
                    mMobileType.setImageTintList(ColorStateList.valueOf(0xffffffff));
                    mNetworkType.setImageTintList(ColorStateList.valueOf(0xffffffff));
                    mVolteType.setImageTintList(ColorStateList.valueOf(0xffffffff));
                    mMobileActivity.setImageTintList(ColorStateList.valueOf(0xffffffff));
                    mNoService.setTextColor(0xffffffff);
                }
            }
            /*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-end*/
        }

        public boolean apply(boolean isSecondaryIcon) {
            /*PRIZE-dismiss MobileGroup when mNoSimsVisible becomes true-liufan-2016-02-25-start*/
            Log.d(TAG, "apply(" + mSubId + ")," + " mMobileVisible= " + mMobileVisible +
                   ", mIsAirplaneMode= " + mIsAirplaneMode+
                   ", mNoSimsVisible= " + mNoSimsVisible);

            /*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-Start  for refresh
            if(colorFlag){
                Log.d("lyy", "set color");
                if(textColor == 0){
                    Log.d("lyy", "set black");
                    mMobile.setImageTintList(ColorStateList.valueOf(0xff000000));
                    mMobileDark.setImageTintList(ColorStateList.valueOf(0xff000000));
                    mMobileType.setImageTintList(ColorStateList.valueOf(0xff000000));
                    mNetworkType.setImageTintList(ColorStateList.valueOf(0xff000000));
                    mVolteType.setImageTintList(ColorStateList.valueOf(0xff000000));
                    mMobileActivity.setImageTintList(ColorStateList.valueOf(0xff000000));
                    mNoService.setTextColor(0xff000000);
                } else {
                    Log.d("lyy", "set white");
                    mMobile.setImageTintList(ColorStateList.valueOf(0xffffffff));
                    mMobileDark.setImageTintList(ColorStateList.valueOf(0xffffffff));
                    mMobileType.setImageTintList(ColorStateList.valueOf(0xffffffff));
                    mNetworkType.setImageTintList(ColorStateList.valueOf(0xffffffff));
                    mVolteType.setImageTintList(ColorStateList.valueOf(0xffffffff));
                    mMobileActivity.setImageTintList(ColorStateList.valueOf(0xffffffff));
                    mNoService.setTextColor(0xffffffff);
                }
            }
            /*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-end*/

            boolean result = PluginManager.isDefaultSystemUIStatusBarExt()
                            ? mMobileVisible && !mIsAirplaneMode && !mNoSimsVisible
                            : mMobileVisible && !mIsAirplaneMode;
            if (result) {
            /*PRIZE-dismiss MobileGroup when mNoSimsVisible becomes true-liufan-2016-02-25-end*/
                if (mLastMobileStrengthId != mMobileStrengthId) {
                    updateAnimatableIcon(mMobile, mMobileStrengthId);
                    updateAnimatableIcon(mMobileDark, mMobileStrengthId);
                    mLastMobileStrengthId = mMobileStrengthId;
                }

                if (mLastMobileTypeId != mMobileTypeId) {
                    mMobileType.setImageResource(mMobileTypeId);
                    mLastMobileTypeId = mMobileTypeId;
                }
                mMobileGroup.setContentDescription(mMobileTypeDescription
                        + " " + mMobileDescription);
                mMobileGroup.setVisibility(View.VISIBLE);
                showViewInWfcCase();
                /*PRIZE-data activity, up and down arrow-liyao-2015-05-25 start*/
                if(PluginManager.isDefaultSystemUIStatusBarExt()){
                    mMobileActivity.setImageResource(mMobileActivityId);
                    mMobileActivity.setVisibility(View.VISIBLE);
                }
                /*PRIZE-data activity, up and down arrow-liyao-2015-05-25 end*/
            } else {
                if (mIsAirplaneMode && (mIsWfcEnable && mVolteIcon != 0)) {
                    /// M:Bug fix for show vowifi icon in flight mode
                    mMobileGroup.setVisibility(View.VISIBLE);
                    hideViewInWfcCase();
                } else {
                    mMobileGroup.setVisibility(View.GONE);
                }
            }

            /// M: Set all added or customised view. @ {
            setCustomizeViewProperty();
            /// @ }

            // When this isn't next to wifi, give it some extra padding between the signals.
            /*PRIZE-update for liuhai screen-liufan-2018-04-09-start*/
            if(PhoneStatusBar.OPEN_LIUHAI_SCREEN){
                mMobileGroup.setPaddingRelative(0, 0, 0, 0);
            } else {
                mMobileGroup.setPaddingRelative(isSecondaryIcon ? mSecondaryTelephonyPadding : 0,
                        0, 0, 0);
            }
            /*PRIZE-update for liuhai screen-liufan-2018-04-09-end*/
            /*PRIZE-cancel the padding-liyao-2015-05-25 start*/
            if(!PluginManager.isDefaultSystemUIStatusBarExt()){
                mMobile.setPaddingRelative(mIsMobileTypeIconWide ? mWideTypeIconStartPadding : 0,
                        0, 0, 0);
                mMobileDark.setPaddingRelative(mIsMobileTypeIconWide ? mWideTypeIconStartPadding : 0,
                        0, 0, 0);
            }
            /*PRIZE-cancel the padding-liyao-2015-05-25 end*/

            if (DEBUG) Log.d(TAG, String.format("mobile: %s sig=%d typ=%d",
                        (mMobileVisible ? "VISIBLE" : "GONE"), mMobileStrengthId, mMobileTypeId));

            mMobileType.setVisibility(mMobileTypeId != 0 ? View.VISIBLE : View.GONE);

            /// M: Add for support plugin featurs. @ {
            setCustomizedOpViews();
            /// @ }

            /*PRIZE-data activity,up and down arrow-liyao-2015-05-25 start*/
            if(!FeatureOption.PRIZE_QS_SORT){
                mMobileType.setVisibility(mMobileTypeId != 0 ? View.VISIBLE : View.GONE);
            } else{
                if(PluginManager.isDefaultSystemUIStatusBarExt()){
	                mMobileType.setVisibility(View.GONE);
	                /*prize-public-bug:mobile data icon-add-liuweiquan-20160218-start*/
	                //final TelephonyManager tm =(TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
	                //boolean enable = tm.getDataEnabled();
	                //int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
	                boolean vis = mMobileTypeId != 0
	                    && !mWifiVisible && !mIsAirplaneMode && mNetworkIcon != 0;
	                mMobileActivity.setVisibility(vis ? View.VISIBLE : View.GONE);
	                //mMobileActivity.setVisibility(mMobileTypeId != 0 ? View.VISIBLE : View.GONE);
	                /*prize-public-bug:mobile data icon-add-liuweiquan-20160218-end*/
	                /**PRIZE no service exchanged string from icon liyao 20150707 start*/

	                //add for statusbar inverse. prize-linkh-20150903
	                //mNoService.setTextColor(PrizeStatusBarStyle.getInstance(getContext()).getColor(0));
                    mNoService.setImageResource(R.drawable.liuhai_no_service);
	                if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && !mIgnoreStatusBarStyleChanged) {
	                    //mNoService.setTextColor(PrizeStatusBarStyle.getInstance(getContext()).getColor(mCurStatusBarStyle));
                        if(mCurStatusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_GRAY){
                            mNoService.setImageResource(R.drawable.liuhai_no_service_gray);
                        }
	                } //end...                
                
	                if (!mIsAirplaneMode && mNetworkIcon == 0) {
	                    //FontSizeUtils.updateFontSize(mNoService, R.dimen.status_bar_clock_size);
	                    //mNoService.setText(R.string.stat_no_service);
	                    mNoService.setVisibility(View.VISIBLE);
	                } else{
	                    mNoService.setVisibility(View.GONE);
	                }
	                /**PRIZE no service exchanged string from icon liyao 20150707 end*/
                } else {
                    mMobileType.setVisibility(View.GONE);
                }
            }
            /*PRIZE-data activity,up and down arrow-liyao-2015-05-25 end*/
            return mMobileVisible;
        }
        
        public void setViewColorFilter(int color){
            if(mMobile != null) setTint(mMobile, color);
            //if(mVolteIcon != null) setTint(mVolteIcon, color);
            //if(mMobileType != null) setTint(mMobileType, color);
            //if(mNetworkType != null) setTint(mNetworkType, color);
            ViewGroup vg = (ViewGroup)mNetworkType.getParent();
            setViewGroupColorFilter(vg, color);
        }

        private void updateAnimatableIcon(ImageView view, int resId) {
            maybeStopAnimatableDrawable(view);
            setIconForView(view, resId);
            maybeStartAnimatableDrawable(view);
        }

        private void maybeStopAnimatableDrawable(ImageView view) {
            Drawable drawable = view.getDrawable();

            // Check if the icon has been scaled. If it has retrieve the actual drawable out of the
            // wrapper.
            if (drawable instanceof ScalingDrawableWrapper) {
                drawable = ((ScalingDrawableWrapper) drawable).getDrawable();
            }

            if (drawable instanceof Animatable) {
                Animatable ad = (Animatable) drawable;
                if (ad.isRunning()) {
                    ad.stop();
                }
            }
        }

        private void maybeStartAnimatableDrawable(ImageView view) {
            Drawable drawable = view.getDrawable();

            // Check if the icon has been scaled. If it has retrieve the actual drawable out of the
            // wrapper.
            if (drawable instanceof ScalingDrawableWrapper) {
                drawable = ((ScalingDrawableWrapper) drawable).getDrawable();
            }

            if (drawable instanceof Animatable) {
                Animatable ad = (Animatable) drawable;
                if (ad instanceof AnimatedVectorDrawable) {
                    ((AnimatedVectorDrawable) ad).forceAnimationOnUI();
                }
                if (!ad.isRunning()) {
                    ad.start();
                }
            }
        }

        public void populateAccessibilityEvent(AccessibilityEvent event) {
            if (mMobileVisible && mMobileGroup != null
                    && mMobileGroup.getContentDescription() != null) {
                event.getText().add(mMobileGroup.getContentDescription());
            }
        }

        public void setIconTint(int tint, float darkIntensity, Rect tintArea) {
            /*PRIZE-cancel the Icon Tint-liufan-2016-11-12-start*/
            /*applyDarkIntensity(
                    StatusBarIconController.getDarkIntensity(tintArea, mMobile, darkIntensity),
                    mMobile, mMobileDark);
            setTint(mMobileType, StatusBarIconController.getTint(tintArea, mMobileType, tint));
            setTint(mNetworkType, StatusBarIconController.getTint(tintArea, mNetworkType, tint));
            setTint(mVolteType, StatusBarIconController.getTint(tintArea, mVolteType, tint));*/
            /*PRIZE-cancel the Icon Tint-liufan-2016-11-12-end*/
        }

        /// M: Set all added or customised view. @ {
        private void setCustomizeViewProperty() {
            // Add for [Network Type on Statusbar], the place to set network type icon.
            setNetworkIcon();
            /// M: Add for volte icon.
            setVolteIcon();
        }

        /// M: Add for volte icon on Statusbar @{
        private void setVolteIcon() {
            /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
            if(PhoneStatusBar.OPEN_LIUHAI_SCREEN){
                mVolteType.setVisibility(View.GONE);
                LiuHaiStatusBarIconController.ShowOnRightListener listener = getShowOnRightListener();
                if(listener == null) {
                    PhoneStatusBar.debugLiuHai("LiuHaiStatusBarIconController.ShowOnRightListener is null");
                    return ;
                }
                if (mVolteIcon == 0) {
                    listener.hideVolteIcon(mSubId, mPhoneStates.size());
                } else {
                    listener.showVolteIcon(mSubId, mVolteIcon, mPhoneStates.size());
                }
                return ;
            }
            /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
            if (mVolteIcon == 0) {
                mVolteType.setVisibility(View.GONE);
            } else {
                mVolteType.setImageResource(mVolteIcon);
                mVolteType.setVisibility(View.VISIBLE);
            }
            /// M: customize VoLTE icon. @{
            mStatusBarExt.setCustomizedVolteView(mVolteIcon, mVolteType);
            mStatusBarExt.setDisVolteView(mSubId, mVolteIcon, mVolteType);
            /// M: customize VoLTE icon. @}
        }
        ///@}

        /// M : Add for [Network Type on Statusbar]
        private void setNetworkIcon() {
            // Network type is CTA feature, so non CTA project should not set this.
            if (!FeatureOptions.MTK_CTA_SET) {
                return;
            }
            if (mNetworkIcon == 0) {
                mNetworkType.setVisibility(View.GONE);
            } else {
                if(PhoneStatusBar.OPEN_LIUHAI_SCREEN){
                    int icon = mNetworkIcon;
                    icon = exchangeIconForLiuHai(icon, mCurStatusBarStyle);
                    mNetworkType.setImageResource(icon);
                    mNetworkType.setVisibility(View.VISIBLE);
                    return ;
                }
                if(PluginManager.isDefaultSystemUIStatusBarExt()){
                    int icon = mNetworkIcon;
                    if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && !mIgnoreStatusBarStyleChanged){
                        if(mCurStatusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_GRAY){
                            icon = exchangeGrayIcon(mNetworkIcon);
                        }
                    }
                    mNetworkType.setImageResource(icon);
                    mNetworkType.setVisibility(View.VISIBLE);
                } else {
                    mNetworkType.setImageResource(mNetworkIcon);
                    mNetworkType.setVisibility(View.VISIBLE);
                }
            }
        }

        /// M: Add for plugin features. @ {
        private void setCustomizedOpViews() {
            if (mMobileVisible && !mIsAirplaneMode) {
                mPhoneStateExt.getServiceStateForCustomizedView(mSubId);

                mPhoneStateExt.setCustomizedAirplaneView(
                    mNoSimsCombo, mIsAirplaneMode);
                mPhoneStateExt.setCustomizedNetworkTypeView(
                    mSubId, mNetworkIcon, mNetworkType);
                mPhoneStateExt.setCustomizedDataTypeView(
                    mSubId, mMobileTypeId,
                    mDataActivityIn, mDataActivityOut);
               //if (mLastMobileStrengthId != mMobileStrengthId) {//add prizeliup 20180322 cmcc
                mPhoneStateExt.setCustomizedSignalStrengthView(
                    mSubId, mMobileStrengthId, mMobile);
                mPhoneStateExt.setCustomizedSignalStrengthView(
                    mSubId, mMobileStrengthId, mMobileDark);
                //}
                mPhoneStateExt.setCustomizedMobileTypeView(
                    mSubId, mMobileType);
                mPhoneStateExt.setCustomizedView(mSubId);
            }
        }
        /// @ }

        private void hideViewInWfcCase() {
            Log.d(TAG, "hideViewInWfcCase, isWfcEnabled = " + mIsWfcEnable + " mSubId =" + mSubId);
            mMobile.setVisibility(View.GONE);
            mMobileDark.setVisibility(View.GONE);
            mMobileType.setVisibility(View.GONE);
            mNetworkType.setVisibility(View.GONE);
            mIsWfcCase = true;
        }

        private void showViewInWfcCase() {
            Log.d(TAG, "showViewInWfcCase: mSubId = " + mSubId + ", mIsWfcCase=" + mIsWfcCase);
            if (mIsWfcCase) {
                mMobile.setVisibility(View.VISIBLE);
                mMobileDark.setVisibility(View.VISIBLE);
                mMobileType.setVisibility(View.VISIBLE);
                mNetworkType.setVisibility(View.VISIBLE);
                mIsWfcCase = false;
            }
        }
    }


    /*PRIZE*mobiledata,up and down arrow(bug 699) 2015-05-25 start*/
    public void setMobileActivityId(final int mobileActivityId, final int subId) {
        Log.d(TAG, "setMobileActivityId(" + subId + "), mobileActivityId= " + mobileActivityId);

        /*PRIZE-add postDelayed,update view in UI thread-liufan-2016-06-02 start*/
        postDelayed(new Runnable(){
            @Override
            public void run() {
                PhoneState state = getOrInflateState(subId);
                if(PhoneStatusBar.OPEN_LIUHAI_SCREEN){
                    state.mMobileActivityId = exchangeMobileInOutIconForLiuHai(mobileActivityId);
                } else {
                    state.mMobileActivityId = mobileActivityId;
                }
                apply();
            }
        },0);
        /*PRIZE-add postDelayed,update view in UI thread-liufan-2016-06-02 end*/
    }
    /*PRIZE*mobiledata,up and down arrow(bug 699) 2015-05-25 end*/
    
    /*PRIZE-Override methos-liufan-2016-04-20-start*/
    @Override
    public void setWifiIndicators(boolean enabled, IconState statusIcon, IconState qsIcon,
            boolean activityIn, boolean activityOut, String description) {
        mWifiVisible = statusIcon.visible && !mBlockWifi;
        mWifiStrengthId = statusIcon.icon;
        mWifiDescription = statusIcon.contentDescription;

        apply();
    }
    ///M: Support[Network Type and volte on StatusBar]. Add more parameter networkType and volte .
    @Override
    public void setMobileDataIndicators(IconState statusIcon, IconState qsIcon, int statusType,
            int networkType, int volteIcon, int qsType, boolean activityIn, boolean activityOut,
            String typeContentDescription, String description, boolean isWide, int subId) {
        PhoneState state = getState(subId);
        if (state == null) {
            return;
        }
        state.mMobileVisible = statusIcon.visible && !mBlockMobile;
        state.mMobileStrengthId = statusIcon.icon;
        state.mMobileTypeId = statusType;
        state.mMobileDescription = statusIcon.contentDescription;
        state.mMobileTypeDescription = typeContentDescription;
        state.mIsMobileTypeIconWide = statusType != 0 && isWide;
        state.mNetworkIcon = networkType;
        state.mVolteIcon = volteIcon;

        /// M: Add for plugin features. @ {
        state.mDataActivityIn = activityIn;
        state.mDataActivityOut = activityOut;
        /// @ }

        apply();
    }

    /*PRIZE-Override methos-liufan-2016-04-20-end*/
    
    public int exchangeGrayIcon(int iconid){
        int result = iconid;
        switch (iconid) {
        case R.drawable.stat_sys_network_type_3g:
            result = R.drawable.stat_sys_network_type_3g_gray_prize;
            break;
        case R.drawable.stat_sys_network_type_1x:
            result = R.drawable.stat_sys_network_type_1x_gray_prize;
            break;
        case R.drawable.stat_sys_network_type_e:
            result = R.drawable.stat_sys_network_type_e_gray_prize;
            break;
        case R.drawable.stat_sys_network_type_4g:
            result = R.drawable.stat_sys_network_type_4g_gray_prize;
            break;
        case R.drawable.stat_sys_network_type_1x_3g:
            result = R.drawable.stat_sys_network_type_1x_3g_gray_prize;
            break;
        case R.drawable.stat_sys_network_type_g:
            result = R.drawable.stat_sys_network_type_g_gray_prize;
            break;
        case R.drawable.stat_sys_network_type_4g_plus:
            result = R.drawable.stat_sys_network_type_4g_plus_gray_prize;
            break;
        }
        return result;
    }

    /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
    public int exchangeAirplaneIconForLiuHai(int iconid){
        int result = iconid;
        switch (iconid) {
        case R.drawable.stat_sys_airplane_mode:
            result = R.drawable.stat_sys_liuhai_airplane_mode;
            break;
        case R.drawable.stat_sys_airplane_mode_gray_prize:
            result = R.drawable.stat_sys_liuhai_airplane_mode_gray_prize;
            break;
        }
        return result;
    }

    public int exchangeWifiInOutIconForLiuHai(int iconid){
        int result = iconid;
        switch (iconid) {
        case R.drawable.stat_sys_wifi_in_prize:
            result = R.drawable.stat_sys_wifi_liuhai_in_prize;
            break;
        case R.drawable.stat_sys_wifi_out_prize:
            result = R.drawable.stat_sys_wifi_liuhai_out_prize;
            break;
        case R.drawable.stat_sys_wifi_inout_prize:
            result = R.drawable.stat_sys_wifi_liuhai_inout_prize;
            break;

        case R.drawable.stat_sys_wifi_in_gray_prize:
            result = R.drawable.stat_sys_wifi_liuhai_in_gray_prize;
            break;
        case R.drawable.stat_sys_wifi_out_gray_prize:
            result = R.drawable.stat_sys_wifi_liuhai_out_gray_prize;
            break;
        case R.drawable.stat_sys_wifi_inout_gray_prize:
            result = R.drawable.stat_sys_wifi_liuhai_inout_gray_prize;
            break;
        }
        return result;
    }

    public int exchangeMobileInOutIconForLiuHai(int iconid){
        int result = iconid;
        switch (iconid) {
        case R.drawable.stat_sys_signal_data_not_inout:
            result = R.drawable.stat_sys_signal_data_liuhai_not_inout;
            break;
        case R.drawable.stat_sys_signal_data_in:
            result = R.drawable.stat_sys_signal_data_liuhai_in;
            break;
        case R.drawable.stat_sys_signal_data_out:
            result = R.drawable.stat_sys_signal_data_liuhai_out;
            break;
        case R.drawable.stat_sys_signal_data_inout:
            result = R.drawable.stat_sys_signal_data_liuhai_inout;
            break;

        case R.drawable.stat_sys_signal_data_not_inout_gray_prize:
            result = R.drawable.stat_sys_signal_data_liuhai_not_inout_gray_prize;
            break;
        case R.drawable.stat_sys_signal_data_in_gray_prize:
            result = R.drawable.stat_sys_signal_data_liuhai_in_gray_prize;
            break;
        case R.drawable.stat_sys_signal_data_out_gray_prize:
            result = R.drawable.stat_sys_signal_data_liuhai_out_gray_prize;
            break;
        case R.drawable.stat_sys_signal_data_inout_gray_prize:
            result = R.drawable.stat_sys_signal_data_liuhai_inout_gray_prize;
            break;
        }
        return result;
    }

    public int exchangeIconForLiuHai(int iconid, int statusBarStyle){
        boolean isNeedGrayIcon = PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && !mIgnoreStatusBarStyleChanged
            && statusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_GRAY;
        int result = iconid;
        switch (iconid) {
        case R.drawable.stat_sys_network_type_3g:
            if(isNeedGrayIcon){
                result = R.drawable.stat_sys_network_type_liuhai_3g_gray_prize;
            } else {
                result = R.drawable.stat_sys_network_type_liuhai_3g;
            }
            break;
        case R.drawable.stat_sys_network_type_1x:
            if(isNeedGrayIcon){
                result = R.drawable.stat_sys_network_type_liuhai_1x_gray_prize;
            } else {
                result = R.drawable.stat_sys_network_type_liuhai_1x;
            }
            break;
        case R.drawable.stat_sys_network_type_e:
            if(isNeedGrayIcon){
                result = R.drawable.stat_sys_network_type_liuhai_e_gray_prize;
            } else {
                result = R.drawable.stat_sys_network_type_liuhai_e;
            }
            break;
        case R.drawable.stat_sys_network_type_4g:
            if(isNeedGrayIcon){
                result = R.drawable.stat_sys_network_type_liuhai_4g_gray_prize;
            } else {
                result = R.drawable.stat_sys_network_type_liuhai_4g;
            }
            break;
        case R.drawable.stat_sys_network_type_2g:
            if(isNeedGrayIcon){
                result = R.drawable.stat_sys_network_type_liuhai_2g_gray_prize;
            } else {
                result = R.drawable.stat_sys_network_type_liuhai_2g;
            }
            break;
        /*case R.drawable.stat_sys_network_type_1x_3g:
            if(isNeedGrayIcon){
                result = R.drawable.stat_sys_network_type_liuhai_1x_3g_gray_prize;
            } else {
                result = R.drawable.stat_sys_network_type_liuhai_1x_3g;
            }
            break;*/
        case R.drawable.stat_sys_network_type_g:
            if(isNeedGrayIcon){
                result = R.drawable.stat_sys_network_type_liuhai_g_gray_prize;
            } else {
                result = R.drawable.stat_sys_network_type_liuhai_g;
            }
            break;
        case R.drawable.stat_sys_network_type_4g_plus:
            if(isNeedGrayIcon){
                result = R.drawable.stat_sys_network_type_liuhai_4g_plus_gray_prize;
            } else {
                result = R.drawable.stat_sys_network_type_liuhai_4g_plus;
            }
            break;
        }
        return result;
    }
    /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

    private boolean inverseFlag = false;
    public void setInverseFlag(boolean flag){
        inverseFlag = flag;
    }
    public boolean isInverseFlag(){
        return inverseFlag;
    }
    public void setStatusBarStyle(int style){
        mCurStatusBarStyle = style;
    }

    /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
    private LiuHaiStatusBarIconController mLiuHaiStatusBarIconController;
    private LiuHaiStatusBarIconController.ShowOnLeftListener mShowOnLeftListener;//implements in this class
    private LiuHaiStatusBarIconController.ShowOnRightListener mShowOnRightListener;//get from controller

    public void setLiuHaiStatusBarIconController(LiuHaiStatusBarIconController controller){
        mLiuHaiStatusBarIconController = controller;
        if(mLiuHaiStatusBarIconController != null){
            mShowOnLeftListener = new ShowOnLeftImpI();
            mLiuHaiStatusBarIconController.setShowOnLeftListener(mShowOnLeftListener);
        }
    }

    private LiuHaiStatusBarIconController.ShowOnRightListener getShowOnRightListener(){
        if(mShowOnRightListener == null){
            if(mLiuHaiStatusBarIconController != null){
                mShowOnRightListener = mLiuHaiStatusBarIconController.getShowOnRightListener();
            }
        }
        return mShowOnRightListener;
    }

    class ShowOnLeftImpI implements LiuHaiStatusBarIconController.ShowOnLeftListener{
        public void showHotSpotsIcon(View view){
            if(view != null){
                if(view.getParent() == LiuHaiSignalClusterView.this){
                    LiuHaiSignalClusterView.this.removeView(view);
                }
                LiuHaiSignalClusterView.this.addView(view);
            }
        }
        public void hideHotSpotsIcon(View view){
            if(view != null){
                if(view.getParent() == LiuHaiSignalClusterView.this){
                    LiuHaiSignalClusterView.this.removeView(view);
                }
            }
        }
    }
    /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
}

