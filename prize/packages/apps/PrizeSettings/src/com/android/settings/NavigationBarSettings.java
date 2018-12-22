/*
* created by prize-linkh at 20150724
*/

package com.android.settings;

import android.os.Bundle;
import android.app.Activity;

import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import android.util.Log;
import android.provider.Settings;
import com.mediatek.common.prizeoption.PrizeOption;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

//prize tangzhengrong 20180510 Swipe-up Gesture Navigation bar start
import android.support.v7.preference.TwoStatePreference;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.os.RemoteException;
//prize tangzhengrong 20180510 Swipe-up Gesture Navigation bar end

public class NavigationBarSettings extends SettingsPreferenceFragment implements
    Preference.OnPreferenceChangeListener, NavBarStylePreference.OnClickListener {
    private static final String TAG = "NavigationBarSettings";
    private static final boolean DBG = true;

    private static final String KEY_HIDE_NAVBAR = "hide_navbar";
    private static final String KEY_SELECT_NAVBAR_STYLE = "select_navbar_style";
    private static final String KEY_NAVBAR_STYLE_PREFIX = "navbar_style_";
    private static final String KEY_TREAT_RECENTS_AS_MENU = "treat_recents_as_menu";
    private static final String KEY_HIDE_NAVBAR_FOR_MBACK = "hide_navbar_for_mback";

    // These style_* values must keep sync with those from NavigationBarInflaterView.java
    public static final int STYLE_BACK_AT_LEFT = 0;
    public static final int STYLE_BACK_AT_RIGHT = 1;
    public static final int STYLE_BACK_AT_LEFT_WITH_HIDE = 2;
    public static final int STYLE_BACK_AT_RIGHT_WITH_HIDE = 3;
    public static final int STYLE_TOTAL = 4;

    // The default design is that back key is at left.
    public static final int STYLE_ORIGINAL = STYLE_BACK_AT_LEFT;

    private boolean mSupportRepositioningBackKey = PrizeOption.PRIZE_REPOSITION_BACK_KEY;
    private boolean mSupportHidingNavBar = PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR;
    private boolean mNeedNavBarStyle;
    PreferenceCategory mSelectNavBarStylePrefCat;
    private int mCurrentNavBarStyle;
    
    private NavBarStylePreference mPreSelectedNavBarStylePreference;
    private SwitchPreference mHideNavBarSwitchPref;

    private boolean mTreatRecentsAsMenu = PrizeOption.PRIZE_TREAT_RECENTS_AS_MENU;
    private boolean mEnableTreatRecentsAsMenu;
    private SwitchPreference mTreatRecentsAsMenuSwitchPref;

    // add for mBack device. prize-linkh-20160805
    private boolean mSupportNavBarFormBackDev = PrizeOption.PRIZE_SUPPORT_NAV_BAR_FOR_MBACK_DEVICE;
    private boolean mNeedHideNavBarFormBack;
    private TwoStatePreference mHideNavBarFormBackSwitchPref;

    //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar start
    public static final boolean OPEN_GESTURE_NAVIGATION = PrizeOption.PRIZE_SWIPE_UP_GESTURE_NAVIGATION;
    private static final String KEY_SWIPE_UP_GESTURE_NAVBAR = "swipe_up_gesture_navbar";
    private boolean mEnableSwipeUpGesture;
    private CheckBoxPreference mSwipeUpGestureSwitchPref;
    private PreferenceCategory mSwipeUpGestureStylePrefCat;
    private PreferenceCategory navBarColorPanelPrefCat;
    private static final String KEY_SELECT_SWIPE_UP_GESTURE_STYLE = "select_swipe_up_gesture_style";
    private static final String KEY_HIDE_GESTURE_INDICATOR = "hide_gesture_indicator";
    private SwitchPreference mHideGestureIndicatorPref;
    private boolean mNeedHideGestureIndicator;
    //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar end
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        
        final Activity activity = getActivity();
        
        IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
        boolean showNav = false;
        try {
            showNav = windowManagerService.hasNavigationBar();
        } catch(Exception e) {
            Log.e(TAG, "failed: " + e);
        }
        if (!showNav) {
            if (DBG) {
                Log.d(TAG, "Device doesn't support nav bar. finish!");
            }
            activity.finish();
        }

        if (DBG) {
            Log.d(TAG, "mSupportRepositioningBackKey=" + mSupportRepositioningBackKey + ", mSupportHidingNavBar=" + mSupportHidingNavBar);
        }
        //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar start
        if(OPEN_GESTURE_NAVIGATION){
            addPreferencesFromResource(R.xml.navigation_bar_prize_liuhai);            
        }else {
            addPreferencesFromResource(R.xml.navigation_bar_prize);
        }
        //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar end
        final int style = Settings.System.getInt(
                        getActivity().getContentResolver(), 
                        Settings.System.PRIZE_NAVIGATION_BAR_STYLE,
                        mCurrentNavBarStyle);

        mCurrentNavBarStyle = fixStyle(style);
        mNeedNavBarStyle = isHidingNavBarStyle(mCurrentNavBarStyle);

        if (DBG) {
            Log.d(TAG, "mCurrentNavBarStyle=" + mCurrentNavBarStyle + ", mNeedNavBarStyle=" + mNeedNavBarStyle);
        }
        mEnableTreatRecentsAsMenu = Settings.System.getInt(getActivity().getContentResolver(),
                                Settings.System.PRIZE_TREAT_RECENTS_AS_MENU, 0) == 1;
        if (DBG) {
            Log.d(TAG, "mTreatRecentsAsMenu=" + mTreatRecentsAsMenu 
                    + ", mEnableTreatRecentsAsMenu=" + mEnableTreatRecentsAsMenu);
        }
        // add for mBack device.
        //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar start
        if(OPEN_GESTURE_NAVIGATION){
            mNeedHideNavBarFormBack = Settings.System.getInt(getActivity().getContentResolver(), 
                                        Settings.System.PRIZE_NAVBAR_STATE_FOR_MBACK, 1) != 1;
            mNeedHideGestureIndicator = Settings.System.getInt(getActivity().getContentResolver(), 
                                        Settings.System.PRIZE_HIDE_SWIPE_UP_GESTURE_INDICATOR, 0) == 1;
            mEnableSwipeUpGesture = mNeedHideNavBarFormBack;
            notifyScreenPinning(mEnableSwipeUpGesture);
        }else{
            mNeedHideNavBarFormBack = Settings.System.getInt(getActivity().getContentResolver(), 
                                        Settings.System.PRIZE_NAVBAR_STATE_FOR_MBACK, 0) != 1;            
        }

        //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar end
        if (DBG) {
            Log.d(TAG, "mNeedHideNavBarFormBack=" + mNeedHideNavBarFormBack);
        }
        initAllPreference();
    }

    private void notifyScreenPinning(boolean swipeUpGestureStatus){
        if(swipeUpGestureStatus){
            try {
                IActivityManager activityManager = ActivityManagerNative.getDefault();
                if (activityManager.isInLockTaskMode()){
                    activityManager.stopSystemLockTaskMode();
                }
            }catch(RemoteException e){
                Log.d(TAG, "Unable to reach activity manager", e);
            }
            Settings.System.putInt(getActivity().getContentResolver(), 
                Settings.System.LOCK_TO_APP_ENABLED, 0);
        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.PRIVACY;
    }

    private boolean isValidStyle(int style) {
        if (style < 0 || style >= STYLE_TOTAL) {
            return false;
        }

        return true;
    }

    private boolean isHidingNavBarStyle(int style) {
        if (style == STYLE_BACK_AT_LEFT_WITH_HIDE || 
                style == STYLE_BACK_AT_RIGHT_WITH_HIDE) {
            return true;
        }

        return false;
    }
    
    private boolean isRepositioningBackKeyStyle(int style) {
        if (style == STYLE_BACK_AT_RIGHT || 
                style == STYLE_BACK_AT_RIGHT_WITH_HIDE) {
            return true;
        }

        return false;
    }

    private int filterOutHidingNavBarStyle(int style) {
        int newStyle = style;
        if (style == STYLE_BACK_AT_LEFT_WITH_HIDE) {
            newStyle = STYLE_BACK_AT_LEFT;
        } else if (style == STYLE_BACK_AT_RIGHT_WITH_HIDE) {
            newStyle = STYLE_BACK_AT_RIGHT;
        }

        return newStyle;
    }
    
    private int fixStyle(int style) {
        if (DBG) {
            Log.d(TAG, "fixStyle() style=" + style);
        }
        if (mSupportHidingNavBar || mSupportRepositioningBackKey) {
            if (!mSupportHidingNavBar) {
                if (style == STYLE_BACK_AT_LEFT_WITH_HIDE) {
                    style = STYLE_BACK_AT_LEFT;
                } else if (style == STYLE_BACK_AT_RIGHT_WITH_HIDE) {
                    style = STYLE_BACK_AT_RIGHT;
                }
            } else if (!mSupportRepositioningBackKey) {
                if (style == STYLE_BACK_AT_RIGHT_WITH_HIDE) {
                    style = STYLE_BACK_AT_LEFT_WITH_HIDE;
                } else if (style == STYLE_BACK_AT_RIGHT) {
                    style = STYLE_BACK_AT_LEFT;
                }
            }
        } else {
            style = STYLE_ORIGINAL;
        }

        if (style < 0 || style >= STYLE_TOTAL) {
            style = STYLE_ORIGINAL;
        }

        if (DBG) {        
            Log.d(TAG, "fixStyle() fixed style=" + style);
        }
        return style;
    }

    private void enableHideNavBar(boolean enable) {
        if (DBG) {        
            Log.d(TAG, "enableHideNavBar() enable=" + enable);
            Log.d(TAG, "current nav bar style : " + mCurrentNavBarStyle);
        }

        if (mNeedNavBarStyle == enable) {
            return;
        }

        mNeedNavBarStyle = enable;
        // let it choose best style.
        changeNavBarStyle(-1);
    }

    private boolean changeNavBarStyle(int newStyle) {
        Log.d(TAG, "changeNavBarStyle(). newStyle=" + newStyle);
        Log.d(TAG, "Current nav bar style: " + mCurrentNavBarStyle);

        int targetStyle = 0;
        final int curStyle = mCurrentNavBarStyle;
        if (newStyle < 0) {
            // ok. we try to find target style according to current nav bar style.
            newStyle = mCurrentNavBarStyle;
        }

        if (mNeedNavBarStyle) {
            if (isRepositioningBackKeyStyle(newStyle)) {
                targetStyle = STYLE_BACK_AT_RIGHT_WITH_HIDE;
            } else {
                targetStyle = STYLE_BACK_AT_LEFT_WITH_HIDE;
            }
        } else {
            if (isRepositioningBackKeyStyle(newStyle)) {
                targetStyle = STYLE_BACK_AT_RIGHT;
            } else {
                targetStyle = STYLE_BACK_AT_LEFT;
            }
        }
        
        final int fixedStyle = fixStyle(targetStyle);
        Log.d(TAG, "targetStyle=" + targetStyle + ", fixedStyle=" + fixedStyle);

        if (curStyle == fixedStyle) {
            Log.w(TAG, "same style. Ignore.");
            return false;
        }

        Log.d(TAG, "save style " + fixedStyle);        
        Settings.System.putInt(
                        getActivity().getContentResolver(), 
                        Settings.System.PRIZE_NAVIGATION_BAR_STYLE,
                        fixedStyle);
        mCurrentNavBarStyle = fixedStyle;
        return true;
    }

    private void initAllPreference() {
        // mBack preference.
        if(OPEN_GESTURE_NAVIGATION){
            mHideNavBarFormBackSwitchPref = (CheckBoxPreference)findPreference(KEY_HIDE_NAVBAR_FOR_MBACK);            
        }else{
            mHideNavBarFormBackSwitchPref = (SwitchPreference)findPreference(KEY_HIDE_NAVBAR_FOR_MBACK);            
        }
        if (mSupportNavBarFormBackDev) {
            mHideNavBarFormBackSwitchPref.setChecked(!mNeedHideNavBarFormBack);
            mHideNavBarFormBackSwitchPref.setOnPreferenceChangeListener(this);
            registerDependencyFormBack();
        } else {
            getPreferenceScreen().removePreference(mHideNavBarFormBackSwitchPref);
        }
        mHideNavBarSwitchPref = (SwitchPreference)findPreference(KEY_HIDE_NAVBAR);
        if(mSupportHidingNavBar) {
            mHideNavBarSwitchPref.setChecked(mNeedNavBarStyle);
            mHideNavBarSwitchPref.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mHideNavBarSwitchPref);
        }

        mTreatRecentsAsMenuSwitchPref = (SwitchPreference)findPreference(KEY_TREAT_RECENTS_AS_MENU);
        if (mTreatRecentsAsMenu) {
            mTreatRecentsAsMenuSwitchPref.setChecked(mEnableTreatRecentsAsMenu);
            mTreatRecentsAsMenuSwitchPref.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mTreatRecentsAsMenuSwitchPref);
        }


        mSelectNavBarStylePrefCat = (PreferenceCategory)findPreference(KEY_SELECT_NAVBAR_STYLE);
        if(mSupportRepositioningBackKey) {
            final int backKeyStyle = filterOutHidingNavBarStyle(mCurrentNavBarStyle);
            int N = mSelectNavBarStylePrefCat.getPreferenceCount();
            for(int i = 0; i < N; ++i) {
                Preference pref = mSelectNavBarStylePrefCat.getPreference(i);
                if(pref instanceof NavBarStylePreference) {
                    NavBarStylePreference nbsPref = (NavBarStylePreference)pref;
                    int style = nbsPref.getStyleIndex();
                    // If its style is purposed for hiding nav bar, then we must remove it.
                    // we have an indepent menu item to do this.
                    if (!isValidStyle(style) || isHidingNavBarStyle(style)
                            || (!mSupportRepositioningBackKey && isRepositioningBackKeyStyle(style))) {
                        Log.w(TAG, "Remove pref " + nbsPref + " because of invalid style " + style);
                        // Preference group uses array list to store data. 
                        mSelectNavBarStylePrefCat.removePreference(nbsPref);
                        --i;
                        --N;
                        continue;
                    }
                    
                    nbsPref.setOnClickListener(this);

                    // Nav bar color customized feature. prize-linkh-2017.07.22 @{
                    if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
                        if ("navbar_style_0".equals(nbsPref.getKey())) {
                            nbsPref.setIcon(R.drawable.navbar_style_0_preview_s8_prize);
                        } else if ("navbar_style_1".equals(nbsPref.getKey())) {
                            nbsPref.setIcon(R.drawable.navbar_style_1_preview_s8_prize);
                        }
                    } // @}

                    if(backKeyStyle >= 0 && backKeyStyle == nbsPref.getStyleIndex()) {
                        nbsPref.setChecked(true);
                        mPreSelectedNavBarStylePreference = nbsPref;
                    } else {
                        nbsPref.setChecked(false);
                    }
                }
            }
        } else {
            getPreferenceScreen().removePreference(mSelectNavBarStylePrefCat);
        }

        // Nav bar color customized feature. prize-linkh-2017.07.11 @{
        navBarColorPanelPrefCat = (PreferenceCategory)findPreference("nav_bar_color_panel");        
        if (!PrizeOption.PRIZE_NAVBAR_COLOR_CUST && navBarColorPanelPrefCat != null) {
            getPreferenceScreen().removePreference(navBarColorPanelPrefCat);
        } // @}
        //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar start
        if(OPEN_GESTURE_NAVIGATION) {
            mSwipeUpGestureSwitchPref = (CheckBoxPreference)findPreference(KEY_SWIPE_UP_GESTURE_NAVBAR);
            mSwipeUpGestureSwitchPref.setChecked(mEnableSwipeUpGesture);
            mSwipeUpGestureSwitchPref.setOnPreferenceChangeListener(this);
            mSwipeUpGestureStylePrefCat = (PreferenceCategory)findPreference(KEY_SELECT_SWIPE_UP_GESTURE_STYLE);
            mHideGestureIndicatorPref = (SwitchPreference)findPreference(KEY_HIDE_GESTURE_INDICATOR);
            mHideGestureIndicatorPref.setChecked(mNeedHideGestureIndicator);
            mHideGestureIndicatorPref.setOnPreferenceChangeListener(this);
            displayViewDependGesture(mEnableSwipeUpGesture);
        }
        //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar end
    }

    private void registerDependencyFormBack() {
        if ((mSupportHidingNavBar || mSupportRepositioningBackKey) 
                && mSelectNavBarStylePrefCat != null) {
            mSelectNavBarStylePrefCat.setDependency(KEY_HIDE_NAVBAR_FOR_MBACK);
        }

        if (mTreatRecentsAsMenu && mTreatRecentsAsMenuSwitchPref != null) {
            mTreatRecentsAsMenuSwitchPref.setDependency(KEY_HIDE_NAVBAR_FOR_MBACK);
        }        
    }
    
    private void updateNavBarStylePreferences(NavBarStylePreference activedPref) {
        if(activedPref != null) {
            if(mPreSelectedNavBarStylePreference != null) {
                mPreSelectedNavBarStylePreference.setChecked(false);
            }
            mPreSelectedNavBarStylePreference = activedPref;
        }
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (DBG) {
            Log.d(TAG, "onPreferenceChange() pref=" + preference + ", key=" + preference.getKey());
        }

        if(mHideNavBarSwitchPref == preference) {
            boolean enable = (boolean)objValue;
            enableHideNavBar(enable);
            return true;
        } else if (mTreatRecentsAsMenuSwitchPref == preference) {
            boolean value = (boolean)objValue;
            int allow = value ? 1 : 0;
            Settings.System.putInt(
                            getActivity().getContentResolver(), 
                            Settings.System.PRIZE_TREAT_RECENTS_AS_MENU,
                            allow);
            return true;
        } else if (mHideNavBarFormBackSwitchPref == preference) {
            onHideNavBarFormBackClicked(objValue);
            return true;
        }
        //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar start
        if (OPEN_GESTURE_NAVIGATION){
            if (mSwipeUpGestureSwitchPref == preference){
                onSwipeUpGestureClicked(objValue);
                return true;
            }else if (mHideGestureIndicatorPref == preference){
                boolean value = (boolean)objValue;
                int allow = value ? 1 : 0;
                Settings.System.putInt(
                                getActivity().getContentResolver(), 
                                Settings.System.PRIZE_HIDE_SWIPE_UP_GESTURE_INDICATOR,
                                allow);
                return true;
            }
        }
        //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar end
        return false;
    }

    private void onSwipeUpGestureClicked(Object value){
        boolean isChecked = (boolean)value;
        notifyScreenPinning(isChecked);
        int show = isChecked ? 1 : 0;
        mHideNavBarFormBackSwitchPref.setChecked(!isChecked);
        Settings.System.putInt(
                    getActivity().getContentResolver(), 
                    Settings.System.PRIZE_SWIPE_UP_GESTURE_STATE,
                    show);
        int reverseShow = show == 1 ? 0 : 1;
        Settings.System.putInt(
                    getActivity().getContentResolver(), 
                    Settings.System.PRIZE_NAVBAR_STATE_FOR_MBACK,
                    reverseShow);
        displayViewDependGesture(isChecked);
    }

    private void onHideNavBarFormBackClicked(Object value){
        boolean isChecked = (boolean)value;
        notifyScreenPinning(!isChecked);
        int show = isChecked ? 1 : 0;
		if(OPEN_GESTURE_NAVIGATION){
			mSwipeUpGestureSwitchPref.setChecked(!isChecked);
			int reverseShow = show == 1 ? 0 : 1;
			Settings.System.putInt(
						getActivity().getContentResolver(),
						Settings.System.PRIZE_SWIPE_UP_GESTURE_STATE,
						reverseShow);
			displayViewDependGesture(!isChecked);
		}
        Settings.System.putInt(
                        getActivity().getContentResolver(), 
                        Settings.System.PRIZE_NAVBAR_STATE_FOR_MBACK,
                        show);
    }

    private void displayViewDependGesture(boolean isChecked){
        if(isChecked){
            if(PrizeOption.PRIZE_NAVBAR_COLOR_CUST && findPreference("nav_bar_color_panel") != null)
                getPreferenceScreen().removePreference(navBarColorPanelPrefCat);
            if(mSupportRepositioningBackKey && findPreference(KEY_SELECT_NAVBAR_STYLE) != null)
                getPreferenceScreen().removePreference(mSelectNavBarStylePrefCat);
            if(mTreatRecentsAsMenu && findPreference(KEY_TREAT_RECENTS_AS_MENU) != null)
                getPreferenceScreen().removePreference(mTreatRecentsAsMenuSwitchPref);
            if(mSupportHidingNavBar && findPreference(KEY_HIDE_NAVBAR) != null)
                getPreferenceScreen().removePreference(mHideNavBarSwitchPref);
            if(findPreference(KEY_SELECT_SWIPE_UP_GESTURE_STYLE) == null)
                getPreferenceScreen().addPreference(mSwipeUpGestureStylePrefCat);
            if(findPreference(KEY_HIDE_GESTURE_INDICATOR) == null)
                getPreferenceScreen().addPreference(mHideGestureIndicatorPref);
        }else{
            if(PrizeOption.PRIZE_NAVBAR_COLOR_CUST && findPreference("nav_bar_color_panel") == null)
                getPreferenceScreen().addPreference(navBarColorPanelPrefCat);
            if(mSupportRepositioningBackKey && findPreference(KEY_SELECT_NAVBAR_STYLE) == null)
                getPreferenceScreen().addPreference(mSelectNavBarStylePrefCat);
            if(mTreatRecentsAsMenu && findPreference(KEY_TREAT_RECENTS_AS_MENU) == null)
                getPreferenceScreen().addPreference(mTreatRecentsAsMenuSwitchPref);
            if(mSupportHidingNavBar && findPreference(KEY_HIDE_NAVBAR) == null)
                getPreferenceScreen().addPreference(mHideNavBarSwitchPref);
            if(findPreference(KEY_SELECT_SWIPE_UP_GESTURE_STYLE) != null)
                getPreferenceScreen().removePreference(mSwipeUpGestureStylePrefCat);
            if(findPreference(KEY_HIDE_GESTURE_INDICATOR) != null)
                getPreferenceScreen().removePreference(mHideGestureIndicatorPref);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return false;
    }    

    @Override
     public void onRadioButtonClicked(NavBarStylePreference pref) {
        if (DBG) {
            Log.d(TAG, "onRadioButtonClicked(). pref=" + pref);
        }

        if (changeNavBarStyle(pref.getStyleIndex())) {
            updateNavBarStylePreferences(pref);
        }
    }
}
