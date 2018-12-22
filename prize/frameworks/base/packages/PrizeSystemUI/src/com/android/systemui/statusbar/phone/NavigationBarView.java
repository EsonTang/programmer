/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.systemui.statusbar.phone;

import android.animation.LayoutTransition;
import android.animation.LayoutTransition.TransitionListener;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.ActivityManagerNative;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.IDockedStackListener.Stub;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.android.systemui.RecentsComponent;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.policy.DeadZone;
/// M: BMW restore Window
import com.mediatek.multiwindow.IFreeformStackListener;
import com.mediatek.multiwindow.MultiWindowManager;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import com.mediatek.common.MPlugin;
import com.mediatek.systemui.ext.DefaultNavigationBarPlugin;
import com.mediatek.systemui.ext.INavigationBarPlugin;
/// M: BMW restore Window
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.SystemUIApplication;

/* Reposition nav bar back key feature & Dynamically hiding nav bar feature. prize-linkh-20161115 */
import com.mediatek.common.prizeoption.PrizeOption;
import android.provider.Settings;
//END...

import com.android.systemui.recents.events.EventBus; // prize-add-split screen-liyongli-20170612
import com.android.systemui.recents.events.component.PrizeOpenPendingIntentEvent;

// Nav bar color customized feature. prize-linkh-2017.07.10
import android.util.ArrayMap;
import com.android.systemui.PrizeNavBarIconsGroup;
import android.graphics.Color;
// @}

public class NavigationBarView extends LinearLayout {
    final static boolean DEBUG = false;
    final static String TAG = "StatusBar/NavBarView";

    // slippery nav bar when everything is disabled, e.g. during setup
    final static boolean SLIPPERY_WHEN_DISABLED = true;

    final static boolean ALTERNATE_CAR_MODE_UI = false;

    final Display mDisplay;
    View mCurrentView = null;
    View[] mRotatedViews = new View[4];

    boolean mVertical;
    boolean mScreenOn;
    private int mCurrentRotation = -1;

    boolean mShowMenu;
    int mDisabledFlags = 0;
    int mNavigationIconHints = 0;

    private Drawable mBackIcon, mBackLandIcon, mBackAltIcon, mBackAltLandIcon;
    private Drawable mBackCarModeIcon, mBackLandCarModeIcon;
    private Drawable mBackAltCarModeIcon, mBackAltLandCarModeIcon;
    private Drawable mHomeDefaultIcon, mHomeCarModeIcon;
    private Drawable mRecentIcon;
    private Drawable mDockedIcon;
    private Drawable mImeIcon;
    private Drawable mMenuIcon;
    /// M: BMW @{
    private Drawable mRestoreIcon;
    private boolean mResizeMode;
    private boolean mRestoreShow;
    /// @}

    private NavigationBarGestureHelper mGestureHelper;
    private DeadZone mDeadZone;
    private final NavigationBarTransitions mBarTransitions;

    // workaround for LayoutTransitions leaving the nav buttons in a weird state (bug 5549288)
    final static boolean WORKAROUND_INVALID_LAYOUT = true;
    final static int MSG_CHECK_INVALID_LAYOUT = 8686;

    // performs manual animation in sync with layout transitions
    private final NavTransitionListener mTransitionListener = new NavTransitionListener();

    private OnVerticalChangedListener mOnVerticalChangedListener;
    private boolean mLayoutTransitionsEnabled = true;
    private boolean mWakeAndUnlocking;
    private boolean mUseCarModeUi = false;
    private boolean mInCarMode = false;
    private boolean mDockedStackExists;

    private final SparseArray<ButtonDispatcher> mButtonDisatchers = new SparseArray<>();
    private Configuration mConfiguration;

    // MPlugin for Navigation Bar
    private INavigationBarPlugin mNavBarPlugin;
    /// M: BMW @{
    private KeyguardViewMediator mKeyguardViewMediator;
    /// @}

    private NavigationBarInflaterView mNavigationInflaterView;

    private class NavTransitionListener implements TransitionListener {
        private boolean mBackTransitioning;
        private boolean mHomeAppearing;
        private long mStartDelay;
        private long mDuration;
        private TimeInterpolator mInterpolator;

        @Override
        public void startTransition(LayoutTransition transition, ViewGroup container,
                View view, int transitionType) {
            if (view.getId() == R.id.back) {
                mBackTransitioning = true;
            } else if (view.getId() == R.id.home && transitionType == LayoutTransition.APPEARING) {
                mHomeAppearing = true;
                mStartDelay = transition.getStartDelay(transitionType);
                mDuration = transition.getDuration(transitionType);
                mInterpolator = transition.getInterpolator(transitionType);
            }
        }

        @Override
        public void endTransition(LayoutTransition transition, ViewGroup container,
                View view, int transitionType) {
            if (view.getId() == R.id.back) {
                mBackTransitioning = false;
            } else if (view.getId() == R.id.home && transitionType == LayoutTransition.APPEARING) {
                mHomeAppearing = false;
            }
        }

        public void onBackAltCleared() {
            ButtonDispatcher backButton = getBackButton();

            // When dismissing ime during unlock, force the back button to run the same appearance
            // animation as home (if we catch this condition early enough).
            if (!mBackTransitioning && backButton.getVisibility() == VISIBLE
                    && mHomeAppearing && getHomeButton().getAlpha() == 0) {
                getBackButton().setAlpha(0);
                ValueAnimator a = ObjectAnimator.ofFloat(backButton, "alpha", 0, 1);
                a.setStartDelay(mStartDelay);
                a.setDuration(mDuration);
                a.setInterpolator(mInterpolator);
                a.start();
            }
        }
    }

    private final OnClickListener mImeSwitcherClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mContext.getSystemService(InputMethodManager.class)
                    .showInputMethodPicker(true /* showAuxiliarySubtypes */);
        }
    };

    private class H extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
                case MSG_CHECK_INVALID_LAYOUT:
                    final String how = "" + m.obj;
                    final int w = getWidth();
                    final int h = getHeight();
                    final int vw = getCurrentView().getWidth();
                    final int vh = getCurrentView().getHeight();

                    if (h != vh || w != vw) {
                        Log.w(TAG, String.format(
                            "*** Invalid layout in navigation bar (%s this=%dx%d cur=%dx%d)",
                            how, w, h, vw, vh));
                        if (WORKAROUND_INVALID_LAYOUT) {
                            requestLayout();
                        }
                    }
                    break;
            }
        }
    }

    public NavigationBarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDisplay = ((WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay();

        mVertical = false;
        mShowMenu = false;
        mGestureHelper = new NavigationBarGestureHelper(context);

        mConfiguration = new Configuration();
        mConfiguration.updateFrom(context.getResources().getConfiguration());
        updateIcons(context, Configuration.EMPTY, mConfiguration);

       // MPlugin Navigation Bar creation and initialization
        try {
            mNavBarPlugin = (INavigationBarPlugin) MPlugin.createInstance(
            INavigationBarPlugin.class.getName(), context);
        } catch (Exception e) {
            Log.e(TAG, "Catch INavigationBarPlugin exception: ", e);
        }
        if (mNavBarPlugin == null) {
            Log.d(TAG, "DefaultNavigationBarPlugin");
            mNavBarPlugin = new DefaultNavigationBarPlugin(context);
        }

        mBarTransitions = new NavigationBarTransitions(this);

        /* Dynamically hiding nav bar feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR) {
            mButtonDisatchers.put(R.id.hide, new ButtonDispatcher(R.id.hide));
        } //END...        
        mButtonDisatchers.put(R.id.back, new ButtonDispatcher(R.id.back));
        mButtonDisatchers.put(R.id.home, new ButtonDispatcher(R.id.home));
        mButtonDisatchers.put(R.id.recent_apps, new ButtonDispatcher(R.id.recent_apps));
        /// M: BMW @{
        if (MultiWindowManager.isSupported()) {
            mButtonDisatchers.put(R.id.restore, new ButtonDispatcher(R.id.restore));
            mKeyguardViewMediator = ((SystemUIApplication)context)
                .getComponent(KeyguardViewMediator.class);
        }
        /// @}
        mButtonDisatchers.put(R.id.menu, new ButtonDispatcher(R.id.menu));
        mButtonDisatchers.put(R.id.ime_switcher, new ButtonDispatcher(R.id.ime_switcher));
    }

    public BarTransitions getBarTransitions() {
        return mBarTransitions;
    }

    // Nav bar color customized feature. prize-linkh-2017.07.08 @{
    public static final boolean DEBUG_NAV_BAR_COLOR = BarTransitions.DEBUG_NAV_BAR_COLOR;
    public static final int NAV_BAR_ICON_WHITE_STYLE = 0;
    public static final int NAV_BAR_ICON_GRAY_STYLE = 1;
    public static final int NAV_BAR_ICON_STYLE_TOTAL = 2;

    public static final int PORTRAIT = 0;
    public static final int LANDSCAPE = 1;

    private int mCurNavBarIconStyle = NAV_BAR_ICON_WHITE_STYLE;
    private int mCurForcingIconStyle = -1;
    private boolean mIsBackAlt;
    
    private int getNavBarIcon(int id, int orientation, int style, 
            boolean isDocked, boolean isBackAlt, boolean isLandAlt) {      
        final int icon = PrizeNavBarIconsGroup.getIcon(id, orientation, style, isDocked, isBackAlt, isLandAlt);

        if (DEBUG_NAV_BAR_COLOR) {
            Log.d(TAG, "getNavBarIcon() id=0x" + Integer.toHexString(id) + ", orientation=" + orientation
                    + ", style=" + style + ", isBackAlt=" + isBackAlt + ", isLandAlt=" + isLandAlt
                    + ", isDocked=" + isDocked + ", icon=0x" + Integer.toHexString(icon));
        }

        return icon;
    }

    private int getNavBarIcon(int id, int orientation, int style, boolean isBackAlt) {
        return getNavBarIcon(id, orientation, style, false, isBackAlt, false);
    }

    private int getNavBarIcon(int id, int orientation, int style) {
        return getNavBarIcon(id, orientation, style, false, false, false);
    }
    
    public void clearForcedBarColor() {
        if (DEBUG_NAV_BAR_COLOR) {
            Log.d(TAG, "clearForcedBarColor()...");
        }
        mBarTransitions.clearForcedBarColor();
        adjustNavBarIcons(0/*any value*/, true);
    }

    public void updateNavBarColor(int color, boolean enableNavBarColor, 
                boolean force, boolean animate, boolean enableSystemUINavBarColorBackground) {
        if (DEBUG_NAV_BAR_COLOR) {
            Log.d(TAG, "updateNavBarColor() color=" + color 
                    + ", enableNavBarColor=" + enableNavBarColor
                    + ", force=" + force + ", animate=" + animate
                    + ", enableSystemUINavBarColorBackground=" + enableSystemUINavBarColorBackground);
        }

        if (enableNavBarColor) {
            if (enableSystemUINavBarColorBackground) {
                mBarTransitions.updateForcedBarColor(color, force, animate);
            } else {
                mBarTransitions.clearForcedBarColor();
            }

            adjustNavBarIcons(color, false);
        } else {
            clearForcedBarColor();
        }        
    }

    public int getNavBarColor() {
        return mBarTransitions.getForcedBarColor();
    }

    private void adjustNavBarIcons(int color, boolean forceUsingOriginalIcons) {
        if (DEBUG_NAV_BAR_COLOR) {
            Log.d(TAG, "adjustNavBarIcons() color=" + color + ", forceUsingOriginalIcons=" 
                + forceUsingOriginalIcons);
        }

        int style = NAV_BAR_ICON_WHITE_STYLE;
        if (!forceUsingOriginalIcons) {
            style = figureOutIconStyle(color);
        }

        applyNavBarIconStyle(style);
    }

    private int figureOutIconStyle(int color) {
        int style = NAV_BAR_ICON_WHITE_STYLE;
        
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        if ((red + green + blue) / 3 > 180) {
            // white
            style = NAV_BAR_ICON_GRAY_STYLE;
        } else {
            // black
            style = NAV_BAR_ICON_WHITE_STYLE;
        }
        
        if (DEBUG_NAV_BAR_COLOR) {
            Log.d(TAG, "figureOutIconstyle() color=" + Integer.toHexString(color) + ", style=" + style);
        }

        return style;
    }

    public void setForcingNavBarIconStyle(int style) {
        if (DEBUG_NAV_BAR_COLOR) {
            Log.d(TAG, "setForcingNavBarIconStyle() style=" + style);
        }

        applyNavBarIconStyle(style, false, true);
    }

    public void clearForcingNavBarIconStyle() {
        mCurForcingIconStyle = -1;
        applyNavBarIconStyle(mCurNavBarIconStyle, true, false);
    }
    
    private void applyNavBarIconStyle(int style) {
        applyNavBarIconStyle(style, false, false);
    }
    
    private void applyNavBarIconStyle(int style, boolean force, boolean enterForcingMode) {
        if (DEBUG_NAV_BAR_COLOR) {
            Log.d(TAG, "applyNavBarIconStyle() style=" + style + ", force=" + force
                + ", enterForcingMode=" + enterForcingMode + ", mCurForcingIconStyle="
                + mCurForcingIconStyle);
        }

        if (enterForcingMode || mCurForcingIconStyle >= 0) {
            // force mode.        
            if (!enterForcingMode) {
                // normal state. save it for later use.
                mCurNavBarIconStyle = style;
                //It's already in force mode, we must force to use that style.
                style = mCurForcingIconStyle;
            } else {
                if (!force && style == mCurForcingIconStyle) {
                    return;
                }                
                mCurForcingIconStyle = style;
            }
        } else {
            if (!force && style == mCurNavBarIconStyle) {
                return;
            }

            mCurNavBarIconStyle = style;
        }

        style = ensureValidNavBarIconStyle(style);
        final int orientation = mVertical ? LANDSCAPE : PORTRAIT;

        if (DEBUG_NAV_BAR_COLOR) {
            Log.d(TAG, "applyNavBarIconStyle() udpate icons. orientation=" + orientation + ", style=" + style
                        + ", isDocked=" + mDockedStackExists + ", isBackAlt=" + mIsBackAlt);
        }

        // Recents icon
        getRecentsButton().setImageResource(getNavBarIcon(R.id.recent_apps, orientation, style, mDockedStackExists, false, false));
        // Home icon
        getHomeButton().setImageResource(getNavBarIcon(R.id.home, orientation, style));
        // Back icon
        getBackButton().setImageResource(getNavBarIcon(R.id.back, orientation, style, mIsBackAlt));
        // MENU icon
        getMenuButton().setImageResource(getNavBarIcon(R.id.menu, orientation, style));
        // IME icon
        getImeSwitchButton().setImageResource(getNavBarIcon(R.id.ime_switcher, orientation, style));
        // Hide icon
        if (PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR) {
            getHideButton().setImageResource(getNavBarIcon(R.id.hide, orientation, style, false, false, mIsLandAlt));
            /* prize-add-split screen pop softkeyboard, not use NavBar Hide Button-liyongli-20171031-start */            
 //           if( PrizeOption.PRIZE_SPLIT_SCREEN_NOT_HIDE_NAVBAR && mDockedStackExists ){
//                getHideButton().setVisibility(mIsBackAlt ? View.INVISIBLE : View.VISIBLE);
//            }/* prize-add-split screen-liyongli-20171031-end */
//2018/2/8 20:13 split screen not hide nav bar; replace up lines
            /* prize-add-split screen not use NavBar Hide Button-liyongli-20180208-start */            
            if( PrizeOption.PRIZE_SPLIT_SCREEN_NOT_HIDE_NAVBAR ){
                getHideButton().setVisibility(mDockedStackExists ? View.INVISIBLE : View.VISIBLE);
            }/* prize-add-split screen-liyongli-20180208-end */            
        }
    }

    public int ensureValidNavBarIconStyle(int style) {
        if (style < 0 || style >= NAV_BAR_ICON_STYLE_TOTAL) {
            return NAV_BAR_ICON_WHITE_STYLE;
        } else {
            return style;
        }
    }
    public int getCurNavBarIconStyle() {
        return mCurForcingIconStyle >= 0 ? mCurForcingIconStyle : mCurNavBarIconStyle;
        
    }

    public int getCustNavBarColorForIME() {
        return Settings.System.getInt(
                getContext().getContentResolver(), Settings.System.PRIZE_NAV_BAR_BG_COLOR, 
                StatusBarManager.DEFAULT_NAV_BAR_COLOR);
    }

    public void updateRecentsIcon2() {
        final int orientation = mVertical ? LANDSCAPE : PORTRAIT;
        final int style = getCurNavBarIconStyle();

        if (DEBUG_NAV_BAR_COLOR) {
            Log.d(TAG, "updateRecentsIconForDocked() orientation=" + orientation + ", style=" + style
                        + ", isDocked=" + mDockedStackExists);
        }
        getRecentsButton().setImageResource(
            getNavBarIcon(R.id.recent_apps, orientation, style, mDockedStackExists, false, false));
    }
    // @}

    public void setComponents(RecentsComponent recentsComponent, Divider divider) {
        mGestureHelper.setComponents(recentsComponent, divider, this);
    }

    public void setOnVerticalChangedListener(OnVerticalChangedListener onVerticalChangedListener) {
        mOnVerticalChangedListener = onVerticalChangedListener;
        notifyVerticalChangedListener(mVertical);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureHelper.onTouchEvent(event)) {
            return true;
        }
        if (mDeadZone != null && event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            mDeadZone.poke(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mGestureHelper.onInterceptTouchEvent(event);
    }

    public void abortCurrentGesture() {
        getHomeButton().abortCurrentGesture();
    }

    private H mHandler = new H();

    public View getCurrentView() {
        return mCurrentView;
    }

    public View[] getAllViews() {
        return mRotatedViews;
    }
    //TODO:: Temp remove plugin for build pass, need to add back
    public ButtonDispatcher getRecentsButton() {
        return mButtonDisatchers.get(R.id.recent_apps);
    }

    public ButtonDispatcher getMenuButton() {
        return mButtonDisatchers.get(R.id.menu);
   }

    public ButtonDispatcher getBackButton() {
        return mButtonDisatchers.get(R.id.back);
    }

    public ButtonDispatcher getHomeButton() {
        return mButtonDisatchers.get(R.id.home);
    }

    public ButtonDispatcher getImeSwitchButton() {
        return mButtonDisatchers.get(R.id.ime_switcher);
    }

    /// M: BMW @{
    public ButtonDispatcher getRestoreButton() {
        return mButtonDisatchers.get(R.id.restore);
    }
    /// @}

    /* Reposition nav bar back key feature & Dynamically hiding nav bar feature. prize-linkh-20161115 */
    final static boolean DEBUG_NAV_BAR = false;

    private Drawable mHideIcon, mHideLandIcon, mHideLandAltIcon;
    
    private int mCurNavBarStyle = NavigationBarInflaterView.STYLE_ORIGINAL;
    private NavigationBarInflaterView mNavBarInflaterView;    
    public boolean mHideByUser = false;
    // case: When nav bar is shown at the left of app win in landscape mode(270 degree rotation), 
    //  we must use a special icon. See bug-56013 for details.
    public boolean mIsLandAlt = false;

    public boolean isNavBarAtLeftOfAppWin(int rotation) {
        boolean isAtLeft = false;;
        if (rotation == Surface.ROTATION_270) {
            // Note: Android originally supports left position of nav bar. But
            //       PrizeOS doesn't allow it except notch device.
            if (PrizeOption.PRIZE_NOTCH_SCREEN) {
                isAtLeft = true;
            }
        }
        
        if (DEBUG_NAV_BAR) {
            Log.d(TAG, "isNavBarAtLeftOfAppWin() rotation=" + rotation + ", isAtLeft=" + isAtLeft);
        }

        return isAtLeft;
    }
    
    private Drawable getHideIcon(boolean landscape, boolean isLandAlt) {
        return landscape ? (isLandAlt ? mHideLandAltIcon : mHideLandIcon) : mHideIcon;
    }
    
    public ButtonDispatcher getHideButton() {
        return mButtonDisatchers.get(R.id.hide);
    } 

    public boolean canHideNavBarForCurStyle() {
        if (mNavBarInflaterView == null) {
            return true;
        }

        return mNavBarInflaterView.canHideNavBar(mCurNavBarStyle);
    }
    
    public void updateNavBarStyle(int style) {
        updateNavBarStyle(style, false);
    }

    public void updateNavBarStyle(int style, boolean force) {
        if (DEBUG_NAV_BAR) {
            Log.d(TAG, "updateNavBarStyle() style=" + style + ", force=" + force
                        + ", currentStyle=" + mCurNavBarStyle);
        }

        final int fixedStyle = mNavBarInflaterView.fixStyle(style);
        if (DEBUG_NAV_BAR) {
            Log.d(TAG, "updateNavBarStyle() fixed style=" + fixedStyle);
        }
        
        if (mCurNavBarStyle == fixedStyle && !force) {
            return;
        }

        mCurNavBarStyle = fixedStyle;
        mNavBarInflaterView.setNewLayout(mNavBarInflaterView.getLayout(fixedStyle));
    }
    //END...

    /* Dynamically changing Recents function feature. prize-linkh-20161115 */
    private boolean mTreatRecentsAsMenu;
    private boolean mShowMenuForRestoring;
    private boolean mHasStoredShowMenuState;
    public void enableTreatRecentsAsMenu(boolean enable) {
        mTreatRecentsAsMenu = enable;
        final boolean showMenu = mHasStoredShowMenuState ? mShowMenuForRestoring : mShowMenu;
        setMenuVisibility(showMenu);
    }  
    //END... 

    private void updateCarModeIcons(Context ctx) {
        mBackCarModeIcon = ctx.getDrawable(R.drawable.ic_sysbar_back_carmode);
        mBackLandCarModeIcon = mBackCarModeIcon;
        mBackAltCarModeIcon = ctx.getDrawable(R.drawable.ic_sysbar_back_ime_carmode);
        mBackAltLandCarModeIcon = mBackAltCarModeIcon;
        mHomeCarModeIcon = ctx.getDrawable(R.drawable.ic_sysbar_home_carmode);
    }

    private void updateIcons(Context ctx, Configuration oldConfig, Configuration newConfig) {
        if (oldConfig.orientation != newConfig.orientation
                || oldConfig.densityDpi != newConfig.densityDpi) {
            mDockedIcon = ctx.getDrawable(R.drawable.ic_sysbar_docked);
            /* Dynamically hiding nav bar feature. prize-linkh-20161115 */
            if (PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR) {
                if (DEBUG_NAV_BAR) {
                    Log.d(TAG, "updateIcons() ...");
                }
                
                mHideLandIcon = ctx.getDrawable(R.drawable.ic_hide_navbar_land_prize);
                mHideIcon = ctx.getDrawable(R.drawable.ic_hide_navbar_prize);
                mHideLandAltIcon = ctx.getDrawable(R.drawable.ic_hide_navbar_270_land_prize);
            } //END...               
        }
        if (oldConfig.densityDpi != newConfig.densityDpi) {
            mBackIcon = ctx.getDrawable(R.drawable.ic_sysbar_back);
            mBackLandIcon = mBackIcon;
            mBackAltIcon = ctx.getDrawable(R.drawable.ic_sysbar_back_ime);
            mBackAltLandIcon = mBackAltIcon;

            mHomeDefaultIcon = ctx.getDrawable(R.drawable.ic_sysbar_home);
            mRecentIcon = ctx.getDrawable(R.drawable.ic_sysbar_recent);
            mMenuIcon = ctx.getDrawable(R.drawable.ic_sysbar_menu);
            mImeIcon = ctx.getDrawable(R.drawable.ic_ime_switcher_default);
            /// M: BMW @{
            if (MultiWindowManager.isSupported()) {
                mRestoreIcon = ctx.getDrawable(R.drawable.ic_sysbar_restore);
            }
            /// @}
            if (ALTERNATE_CAR_MODE_UI) {
                updateCarModeIcons(ctx);
            }
        }
    }

    @Override
    public void setLayoutDirection(int layoutDirection) {
        // Reload all the icons
        updateIcons(getContext(), Configuration.EMPTY, mConfiguration);

        super.setLayoutDirection(layoutDirection);
    }

    public void notifyScreenOn(boolean screenOn) {
        mScreenOn = screenOn;
        setDisabledFlags(mDisabledFlags, true);
    }

    public void setNavigationIconHints(int hints) {
        setNavigationIconHints(hints, false);
    }

    private Drawable getBackIconWithAlt(boolean carMode, boolean landscape) {
        return landscape
                ? carMode ? mBackAltLandCarModeIcon : mBackAltLandIcon
                : carMode ? mBackAltCarModeIcon : mBackAltIcon;
    }

    private Drawable getBackIcon(boolean carMode, boolean landscape) {
        return landscape
                ? carMode ? mBackLandCarModeIcon : mBackLandIcon
                : carMode ? mBackCarModeIcon : mBackIcon;
    }

    public void setNavigationIconHints(int hints, boolean force) {
        if (!force && hints == mNavigationIconHints) return;
        final boolean backAlt = (hints & StatusBarManager.NAVIGATION_HINT_BACK_ALT) != 0;
        if ((mNavigationIconHints & StatusBarManager.NAVIGATION_HINT_BACK_ALT) != 0 && !backAlt) {
            mTransitionListener.onBackAltCleared();
        }
        if (DEBUG) {
            android.widget.Toast.makeText(getContext(),
                "Navigation icon hints = " + hints,
                500).show();
        }

        mNavigationIconHints = hints;

        // We have to replace or restore the back and home button icons when exiting or entering
        // carmode, respectively. Recents are not available in CarMode in nav bar so change
        // to recent icon is not required.
        Drawable backIcon = (backAlt)
                ? getBackIconWithAlt(mUseCarModeUi, mVertical)
                : getBackIcon(mUseCarModeUi, mVertical);

        /// M: Support plugin customize.
        //getBackButton().setImageDrawable(backIcon);
        getBackButton().setImageDrawable(mNavBarPlugin.getBackImage(backIcon));

        /* Dynamically hiding nav bar feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR) {
            if (DEBUG_NAV_BAR) {
                Log.d(TAG, "setNavigationIconHints() set hide icon.");
            }

            mIsLandAlt = isNavBarAtLeftOfAppWin(mCurrentRotation);            
            getHideButton().setImageDrawable(getHideIcon(mVertical, mIsLandAlt));
            getHideButton().setVisibility(View.VISIBLE);
        } //END...

        updateRecentsIcon();
        /// M: BMW @{
        if (MultiWindowManager.isSupported()) {
            updateRestoreIcon();
        }
        /// @}

        if (mUseCarModeUi) {
            getHomeButton().setImageDrawable(mHomeCarModeIcon);
        } else {
            /// M: Support plugin customize.
            //getHomeButton().setImageDrawable(mHomeDefaultIcon);
            getHomeButton().setImageDrawable(mNavBarPlugin.getHomeImage(mHomeDefaultIcon));
        }

        final boolean showImeButton = ((hints & StatusBarManager.NAVIGATION_HINT_IME_SHOWN) != 0);
        getImeSwitchButton().setVisibility(showImeButton ? View.VISIBLE : View.INVISIBLE);
        // Force to hide IME Button. prize-linkh-20171109 @{
        if (true) {
            Log.d(TAG, "setNavigationIconHints() force to hide IME button!");
            getImeSwitchButton().setVisibility(View.INVISIBLE);
        } // @}        
        getImeSwitchButton().setImageDrawable(mImeIcon);

        // Update menu button in case the IME state has changed.
        setMenuVisibility(mShowMenu, true);
        getMenuButton().setImageDrawable(mMenuIcon);

        /* prize-add-split screen pop softkeyboard, not use NavBar Hide Button-liyongli-20171031-start */
        if( PrizeOption.PRIZE_SPLIT_SCREEN_NOT_HIDE_NAVBAR ){
            mIsBackAlt = backAlt;
        }/* prize-add-split screen-liyongli-20171031-end */
        
        // Nav bar color customized feature. prize-linkh-2017.07.10 @{
        if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            mIsBackAlt = backAlt;
            boolean inEnforcingMode = mCurForcingIconStyle >= 0 ? true : false;
            int style = inEnforcingMode ? mCurForcingIconStyle : mCurNavBarIconStyle;

            style = ensureValidNavBarIconStyle(style);

            if (DEBUG_NAV_BAR_COLOR) {
                Log.d(TAG, "setNavigationIconHints(). style=" + style + ", backAlt=" + backAlt
                        + ", mVertical=" + mVertical + ", inEnforcingMode=" + inEnforcingMode);
            }
            applyNavBarIconStyle(style, true, inEnforcingMode);
        } 
        // }@

        setDisabledFlags(mDisabledFlags, true);
    }

    public void setDisabledFlags(int disabledFlags) {
        setDisabledFlags(disabledFlags, false);
    }

    public void setDisabledFlags(int disabledFlags, boolean force) {
        if (!force && mDisabledFlags == disabledFlags) return;

        mDisabledFlags = disabledFlags;

        final boolean disableHome = ((disabledFlags & View.STATUS_BAR_DISABLE_HOME) != 0);

        // Always disable recents when alternate car mode UI is active.
        boolean disableRecent = mUseCarModeUi
                        || ((disabledFlags & View.STATUS_BAR_DISABLE_RECENT) != 0);
        final boolean disableBack = ((disabledFlags & View.STATUS_BAR_DISABLE_BACK) != 0)
                && ((mNavigationIconHints & StatusBarManager.NAVIGATION_HINT_BACK_ALT) == 0);
        final boolean disableSearch = ((disabledFlags & View.STATUS_BAR_DISABLE_SEARCH) != 0);

        ViewGroup navButtons = (ViewGroup) getCurrentView().findViewById(R.id.nav_buttons);
        if (navButtons != null) {
            LayoutTransition lt = navButtons.getLayoutTransition();
            if (lt != null) {
                if (!lt.getTransitionListeners().contains(mTransitionListener)) {
                    lt.addTransitionListener(mTransitionListener);
                }
            }
        }
        if (inLockTask() && disableRecent && !disableHome) {
            // Don't hide recents when in lock task, it is used for exiting.
            // Unless home is hidden, then in DPM locked mode and no exit available.
            disableRecent = false;
        }

        getBackButton().setVisibility(disableBack      ? View.INVISIBLE : View.VISIBLE);
        getHomeButton().setVisibility(disableHome      ? View.INVISIBLE : View.VISIBLE);
        getRecentsButton().setVisibility(disableRecent ? View.INVISIBLE : View.VISIBLE);

        /// M: BMW @{
        //hide restore when keyguard is showing
        if (MultiWindowManager.isSupported() && mKeyguardViewMediator != null) {
            boolean isKeyguardShowing = mKeyguardViewMediator.isShowing();
            mResizeMode = mRestoreShow && !isKeyguardShowing;
            updateRestoreIcon();
        }
        /// @}
   }

    private boolean inLockTask() {
        try {
            return ActivityManagerNative.getDefault().isInLockTaskMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setLayoutTransitionsEnabled(boolean enabled) {
        mLayoutTransitionsEnabled = enabled;
        updateLayoutTransitionsEnabled();
    }

    public void setWakeAndUnlocking(boolean wakeAndUnlocking) {
        setUseFadingAnimations(wakeAndUnlocking);
        mWakeAndUnlocking = wakeAndUnlocking;
        updateLayoutTransitionsEnabled();
    }

    private void updateLayoutTransitionsEnabled() {
        boolean enabled = !mWakeAndUnlocking && mLayoutTransitionsEnabled;
        ViewGroup navButtons = (ViewGroup) getCurrentView().findViewById(R.id.nav_buttons);
        LayoutTransition lt = navButtons.getLayoutTransition();
        if (lt != null) {
            if (enabled) {
                lt.enableTransitionType(LayoutTransition.APPEARING);
                lt.enableTransitionType(LayoutTransition.DISAPPEARING);
                lt.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
                lt.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
            } else {
                lt.disableTransitionType(LayoutTransition.APPEARING);
                lt.disableTransitionType(LayoutTransition.DISAPPEARING);
                lt.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
                lt.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
            }
        }
    }

    private void setUseFadingAnimations(boolean useFadingAnimations) {
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
        if (lp != null) {
            boolean old = lp.windowAnimations != 0;
            if (!old && useFadingAnimations) {
                lp.windowAnimations = R.style.Animation_NavigationBarFadeIn;
            } else if (old && !useFadingAnimations) {
                lp.windowAnimations = 0;
            } else {
                return;
            }
            WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.updateViewLayout(this, lp);
        }
    }

    public void setMenuVisibility(final boolean show) {
        setMenuVisibility(show, false);
    }

    public void setMenuVisibility(final boolean show, final boolean force) {
        if (!force && mShowMenu == show) return;

        mShowMenu = show;

        /* Dynamically changing Recents function feature. prize-linkh-20161115 */
        if(PrizeOption.PRIZE_TREAT_RECENTS_AS_MENU) {
            mShowMenuForRestoring = mShowMenu;
            mHasStoredShowMenuState = true;
            mShowMenu = mTreatRecentsAsMenu ? false : mShowMenu;
        } //end...

        // Only show Menu if IME switcher not shown.
        final boolean shouldShow = mShowMenu &&
                ((mNavigationIconHints & StatusBarManager.NAVIGATION_HINT_IME_SHOWN) == 0);

        getMenuButton().setVisibility(shouldShow ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onFinishInflate() {       
        mNavigationInflaterView = (NavigationBarInflaterView) findViewById(
                R.id.navigation_inflater);
 
        /* Reposition nav bar back key feature & Dynamically hiding nav bar feature. prize-linkh-20161115 */        
        if (PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR || PrizeOption.PRIZE_REPOSITION_BACK_KEY) {
            mNavBarInflaterView = (NavigationBarInflaterView)findViewById(R.id.navigation_inflater);
            int style = Settings.System.getInt(getContext().getContentResolver(), 
                    Settings.System.PRIZE_NAVIGATION_BAR_STYLE,
                    NavigationBarInflaterView.STYLE_ORIGINAL);
            updateNavBarStyle(style);
        }
        //END...
        updateRotatedViews();
        mNavigationInflaterView.setButtonDispatchers(mButtonDisatchers);

        getImeSwitchButton().setOnClickListener(mImeSwitcherClickListener);

        try {
            WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(new Stub() {
                @Override
                public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
                }

                @Override
                public void onDockedStackExistsChanged(final boolean exists) throws RemoteException {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDockedStackExists = exists;
                            /* prize-add-split screen not use NavBar Hide Button-liyongli-20180208-start */  
                            if( PrizeOption.PRIZE_SPLIT_SCREEN_NOT_HIDE_NAVBAR ){
                                getHideButton().setVisibility(mDockedStackExists ? View.INVISIBLE : View.VISIBLE); 
                            }/* prize-add-split screen-liyongli-20180208-end */
                            
                            // Nav bar color customized feature. prize-linkh-20171021 @{
                            if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
                                updateRecentsIcon2();
                            } else { // @}
                                updateRecentsIcon();
                            }
                        }
                    });
                }

                @Override
                public void onDockedStackMinimizedChanged(boolean minimized, long animDuration)
                        throws RemoteException {
                }

                @Override
                public void onAdjustedForImeChanged(boolean adjustedForIme, long animDuration)
                        throws RemoteException {
                }

                @Override
                public void onDockSideChanged(int newDockSide) throws RemoteException {
                }
                
                /* prize-add-split screen-liyongli-20170708-start */
                @Override
                public void prizeOnFocusStackChanged(int focusedStackId, int lastFocusedStackId) throws RemoteException {
                }
                /* prize-add-split screen-liyongli-20170708-end */
                /* prize-add-split screen, return btn exit split screen-liyongli-20170724-start */
                @Override
                public void prizeOnRequestExitSplitScreen(int btnType, int focusedStackId) throws RemoteException {
                }
                /* prize-add-split screen, return btn exit split screen-liyongli-20170724-end */
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Failed registering docked stack exists listener", e);
        }


        /// M: BMW restore button @{
        if (MultiWindowManager.isSupported()) {
            try {
                WindowManagerGlobal.getWindowManagerService()
                    .registerFreeformStackListener(new IFreeformStackListener.Stub() {
                    @Override
                    public void onShowRestoreButtonChanged(final boolean isShown)
                                 throws RemoteException {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //hide restore when keyguard is showing
                                boolean isKeyguardShowing = false;
                                if (mKeyguardViewMediator != null) {
                                    isKeyguardShowing = mKeyguardViewMediator.isShowing();
                                }
                                mRestoreShow = isShown;
                                mResizeMode = isShown && !isKeyguardShowing;
                                updateRestoreIcon();
                            }
                    });
                    }
                });
            } catch (RemoteException e) {
                Log.e(TAG, "Failed registering freeform stack exists listener", e);
            }

        }

        /// @}
    }

    /// M: BMW restore button @{
    private void updateRestoreIcon() {
        if (MultiWindowManager.DEBUG)
            Log.d(TAG, "BMW, updateRestoreIcon, mResizeMode = " + mResizeMode);
        getRestoreButton().setImageDrawable(mRestoreIcon);
        getRestoreButton().setVisibility(mResizeMode ? View.VISIBLE : View.INVISIBLE);
    }
    /// @}

    void updateRotatedViews() {
        mRotatedViews[Surface.ROTATION_0] =
                mRotatedViews[Surface.ROTATION_180] = findViewById(R.id.rot0);
        mRotatedViews[Surface.ROTATION_270] =
                mRotatedViews[Surface.ROTATION_90] = findViewById(R.id.rot90);

        updateCurrentView();
    }

    public boolean needsReorient(int rotation) {
        return mCurrentRotation != rotation;
    }

    private void updateCurrentView() {
        final int rot = mDisplay.getRotation();
        for (int i=0; i<4; i++) {
            mRotatedViews[i].setVisibility(View.GONE);
        }
        mCurrentView = mRotatedViews[rot];
        mCurrentView.setVisibility(View.VISIBLE);
        mNavigationInflaterView.setAlternativeOrder(rot == Surface.ROTATION_90);
        for (int i = 0; i < mButtonDisatchers.size(); i++) {
            mButtonDisatchers.valueAt(i).setCurrentView(mCurrentView);
        }
        updateLayoutTransitionsEnabled();
        mCurrentRotation = rot;
    }

    private void updateRecentsIcon() {
        /// M: Support plugin customize.
        //getRecentsButton().setImageDrawable(mDockedStackExists ? mDockedIcon : mRecentIcon);
        getRecentsButton().setImageDrawable(
                mNavBarPlugin.getRecentImage(mDockedStackExists ? mDockedIcon : mRecentIcon));
    }

    public boolean isVertical() {
        return mVertical;
    }

    public void reorient() {
        /* Reposition nav bar back key feature & Dynamically hiding nav bar feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR || PrizeOption.PRIZE_REPOSITION_BACK_KEY) {
            int style = Settings.System.getInt(getContext().getContentResolver(), 
                    Settings.System.PRIZE_NAVIGATION_BAR_STYLE,
                    NavigationBarInflaterView.STYLE_ORIGINAL);
            if (DEBUG_NAV_BAR) {
                Log.d(TAG, "reorient() ori style=" + NavigationBarInflaterView.getStyleDecription(style));
            }           
            updateNavBarStyle(style);
        } //END...

        updateCurrentView();

        getImeSwitchButton().setOnClickListener(mImeSwitcherClickListener);

        mDeadZone = (DeadZone) mCurrentView.findViewById(R.id.deadzone);

        // force the low profile & disabled states into compliance
        mBarTransitions.init();
        setDisabledFlags(mDisabledFlags, true /* force */);
        setMenuVisibility(mShowMenu, true /* force */);

        if (DEBUG) {
            Log.d(TAG, "reorient(): rot=" + mCurrentRotation);
        }

        updateTaskSwitchHelper();
        setNavigationIconHints(mNavigationIconHints, true);
    }

    private void updateTaskSwitchHelper() {
        boolean isRtl = (getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
        mGestureHelper.setBarState(mVertical, isRtl);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (DEBUG) Log.d(TAG, String.format(
                    "onSizeChanged: (%dx%d) old: (%dx%d)", w, h, oldw, oldh));

        final boolean newVertical = w > 0 && h > w;
        if (newVertical != mVertical) {
            mVertical = newVertical;
            //Log.v(TAG, String.format("onSizeChanged: h=%d, w=%d, vert=%s", h, w, mVertical?"y":"n"));
            reorient();
            notifyVerticalChangedListener(newVertical);
            // Nav bar color customized feature. prize-linkh-2017.07.10
            if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
                applyNavBarIconStyle(mCurNavBarIconStyle, true, false);
            } // @}         
        }

        postCheckForInvalidLayout("sizeChanged");
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void notifyVerticalChangedListener(boolean newVertical) {
        if (mOnVerticalChangedListener != null) {
            mOnVerticalChangedListener.onVerticalChanged(newVertical);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean uiCarModeChanged = updateCarMode(newConfig);
        updateTaskSwitchHelper();
        updateIcons(getContext(), mConfiguration, newConfig);
        updateRecentsIcon();
        if (uiCarModeChanged || mConfiguration.densityDpi != newConfig.densityDpi) {
            // If car mode or density changes, we need to reset the icons.
            setNavigationIconHints(mNavigationIconHints, true);
        }
        // Nav bar color customized feature. prize-linkh-20171021 @{
        else if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            updateRecentsIcon2();
        } // @}

        mConfiguration.updateFrom(newConfig);
    }

    /**
     * If the configuration changed, update the carmode and return that it was updated.
     */
    private boolean updateCarMode(Configuration newConfig) {
        boolean uiCarModeChanged = false;
        if (newConfig != null) {
            int uiMode = newConfig.uiMode & Configuration.UI_MODE_TYPE_MASK;
            final boolean isCarMode = (uiMode == Configuration.UI_MODE_TYPE_CAR);

            if (isCarMode != mInCarMode) {
                mInCarMode = isCarMode;
                getHomeButton().setCarMode(isCarMode);

                if (ALTERNATE_CAR_MODE_UI) {
                    mUseCarModeUi = isCarMode;
                    uiCarModeChanged = true;
                } else {
                    // Don't use car mode behavior if ALTERNATE_CAR_MODE_UI not set.
                    mUseCarModeUi = false;
                }
            }
        }
        return uiCarModeChanged;
    }

    /*
    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        if (DEBUG) Log.d(TAG, String.format(
                    "onLayout: %s (%d,%d,%d,%d)",
                    changed?"changed":"notchanged", left, top, right, bottom));
        super.onLayout(changed, left, top, right, bottom);
    }

    // uncomment this for extra defensiveness in WORKAROUND_INVALID_LAYOUT situations: if all else
    // fails, any touch on the display will fix the layout.
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (DEBUG) Log.d(TAG, "onInterceptTouchEvent: " + ev.toString());
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            postCheckForInvalidLayout("touch");
        }
        return super.onInterceptTouchEvent(ev);
    }
    */


    private String getResourceName(int resId) {
        if (resId != 0) {
            final android.content.res.Resources res = getContext().getResources();
            try {
                return res.getResourceName(resId);
            } catch (android.content.res.Resources.NotFoundException ex) {
                return "(unknown)";
            }
        } else {
            return "(null)";
        }
    }

    private void postCheckForInvalidLayout(final String how) {
        mHandler.obtainMessage(MSG_CHECK_INVALID_LAYOUT, 0, 0, how).sendToTarget();
    }

    private static String visibilityToString(int vis) {
        switch (vis) {
            case View.INVISIBLE:
                return "INVISIBLE";
            case View.GONE:
                return "GONE";
            default:
                return "VISIBLE";
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("NavigationBarView {");
        final Rect r = new Rect();
        final Point size = new Point();
        mDisplay.getRealSize(size);

        pw.println(String.format("      this: " + PhoneStatusBar.viewInfo(this)
                        + " " + visibilityToString(getVisibility())));

        getWindowVisibleDisplayFrame(r);
        final boolean offscreen = r.right > size.x || r.bottom > size.y;
        pw.println("      window: "
                + r.toShortString()
                + " " + visibilityToString(getWindowVisibility())
                + (offscreen ? " OFFSCREEN!" : ""));

        pw.println(String.format("      mCurrentView: id=%s (%dx%d) %s",
                        getResourceName(getCurrentView().getId()),
                        getCurrentView().getWidth(), getCurrentView().getHeight(),
                        visibilityToString(getCurrentView().getVisibility())));

        pw.println(String.format("      disabled=0x%08x vertical=%s menu=%s",
                        mDisabledFlags,
                        mVertical ? "true" : "false",
                        mShowMenu ? "true" : "false"));

        dumpButton(pw, "back", getBackButton());
        dumpButton(pw, "home", getHomeButton());
        dumpButton(pw, "rcnt", getRecentsButton());
        dumpButton(pw, "menu", getMenuButton());

        pw.println("    }");
    }

    private static void dumpButton(PrintWriter pw, String caption, ButtonDispatcher button) {
        pw.print("      " + caption + ": ");
        if (button == null) {
            pw.print("null");
        } else {
            pw.print(visibilityToString(button.getVisibility())
                    + " alpha=" + button.getAlpha()
                    );
        }
        pw.println();
    }

    public interface OnVerticalChangedListener {
        void onVerticalChanged(boolean isVertical);
    }
    
/* prize-add-split screen-liyongli-20170612-start */   
     protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
      }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
      }
/* prize-add-split screen-liyongli-20170612-end */
}
