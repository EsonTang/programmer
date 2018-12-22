/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.server.policy;

import static android.app.ActivityManager.StackId.DOCKED_STACK_ID;
import static android.app.ActivityManager.StackId.FREEFORM_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.HOME_STACK_ID;
import static android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE;
import static android.content.pm.PackageManager.FEATURE_TELEVISION;
import static android.content.pm.PackageManager.FEATURE_WATCH;
import static android.content.res.Configuration.EMPTY;
import static android.content.res.Configuration.UI_MODE_TYPE_CAR;
import static android.content.res.Configuration.UI_MODE_TYPE_MASK;
import static android.view.WindowManager.DOCKED_TOP;
import static android.view.WindowManager.DOCKED_LEFT;
import static android.view.WindowManager.DOCKED_RIGHT;
import static android.view.WindowManager.TAKE_SCREENSHOT_FULLSCREEN;
import static android.view.WindowManager.TAKE_SCREENSHOT_SELECTED_REGION;
import static android.view.WindowManager.LayoutParams.*;
import static android.view.WindowManagerPolicy.WindowManagerFuncs.CAMERA_LENS_COVERED;
import static android.view.WindowManagerPolicy.WindowManagerFuncs.CAMERA_LENS_COVER_ABSENT;
import static android.view.WindowManagerPolicy.WindowManagerFuncs.CAMERA_LENS_UNCOVERED;
import static android.view.WindowManagerPolicy.WindowManagerFuncs.LID_ABSENT;
import static android.view.WindowManagerPolicy.WindowManagerFuncs.LID_CLOSED;
import static android.view.WindowManagerPolicy.WindowManagerFuncs.LID_OPEN;

import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerInternal.SleepToken;
import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.IUiModeManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.StatusBarManager;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.hdmi.HdmiPlaybackClient.OneTouchPlayCallback;
import android.hardware.input.InputManagerInternal;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.session.MediaSessionLegacyHelper;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.FactoryTest;
import android.os.Handler;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.service.dreams.DreamManagerInternal;
import android.service.dreams.DreamService;
import android.service.dreams.IDreamManager;
import android.speech.RecognizerIntent;
import android.telecom.TelecomManager;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.util.MutableBoolean;
import android.util.Slog;
import android.util.SparseArray;
import android.util.LongSparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.IApplicationToken;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.KeyCharacterMap;
import android.view.KeyCharacterMap.FallbackAction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerInternal;
import android.view.WindowManagerPolicy;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.policy.IShortcutService;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.ScreenShapeHelper;
import com.android.internal.widget.PointerLocationView;
import com.android.server.GestureLauncherService;
import com.android.server.LocalServices;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;
import com.android.server.policy.keyguard.KeyguardServiceDelegate.DrawnListener;
import com.android.server.statusbar.StatusBarManagerInternal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;

/*--prize --add by lihuangyuan,for write screen state --start--*/
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
/*--prize --add by lihuangyuan,for write screen state --end--*/
/// M: Add import.
import android.hardware.input.InputManager;
import android.content.res.TypedArray;
/*PRIZE-PowerExtendMode-wangxianzhen-2015-04-14-start*/
import com.mediatek.common.prizeoption.PrizeOption;
/*PRIZE-PowerExtendMode-wangxianzhen-2015-04-14-end*/
/**prize-sleepgesture-liup-20150419-start*/
import android.util.Log;
import java.io.*;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.database.Cursor;
/**prize-sleepgesture-liup-20150419-end*/

/*Prize-pull cpu core num and freq when wake up-chenlong-20180405-start*/
import com.mediatek.perfservice.IPerfServiceWrapper;
import com.mediatek.perfservice.PerfServiceWrapper;
/*Prize-pull cpu core num and freq when wake up-chenlong-20180405-end*/

// Nav bar color customized feature. prize-linkh-2018.03.13 @{
import android.util.PrizeNavBarColorHintUtil;
// @}

/**
 * WindowManagerPolicy implementation for the Android phone UI.  This
 * introduces a new method suffix, Lp, for an internal lock of the
 * PhoneWindowManager.  This is used to protect some internal state, and
 * can be acquired with either the Lw and Li lock held, so has the restrictions
 * of both of those when held.
 */
public class PhoneWindowManager implements WindowManagerPolicy {
    static final String TAG = "WindowManager";
    /// M: runtime switch debug flags @{
    static boolean DEBUG = false;
    static boolean localLOGV = false;
    static boolean DEBUG_INPUT = false;
    static boolean DEBUG_KEYGUARD = false;
    static boolean DEBUG_LAYOUT = false;
    static boolean DEBUG_STARTING_WINDOW = false;
    static boolean DEBUG_WAKEUP = false;//prize-modify by lihuangyuan,for open log temp-2018-03-21
    static boolean DEBUG_ORIENTATION = false;
    static boolean DEBUG_NOTSCREEN = false;//add by lihuangyuan,for debug notscreen layout-2018-08-02
    /// @}
    static final boolean SHOW_STARTING_ANIMATIONS = true;

    // Whether to allow dock apps with METADATA_DOCK_HOME to temporarily take over the Home key.
    // No longer recommended for desk docks;
    static final boolean ENABLE_DESK_DOCK_HOME_CAPTURE = false;

    static final boolean ALTERNATE_CAR_MODE_NAV_SIZE = false;

    /* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 start */
    public static final String DISABLE_NAVIGATION = "prize_disable_navigation";
    /* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 end */
    static final int SHORT_PRESS_POWER_NOTHING = 0;
    static final int SHORT_PRESS_POWER_GO_TO_SLEEP = 1;
    static final int SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP = 2;
    static final int SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP_AND_GO_HOME = 3;
    static final int SHORT_PRESS_POWER_GO_HOME = 4;

    static final int LONG_PRESS_POWER_NOTHING = 0;
    static final int LONG_PRESS_POWER_GLOBAL_ACTIONS = 1;
    static final int LONG_PRESS_POWER_SHUT_OFF = 2;
    static final int LONG_PRESS_POWER_SHUT_OFF_NO_CONFIRM = 3;

    static final int LONG_PRESS_BACK_NOTHING = 0;
    static final int LONG_PRESS_BACK_GO_TO_VOICE_ASSIST = 1;

    static final int MULTI_PRESS_POWER_NOTHING = 0;
    static final int MULTI_PRESS_POWER_THEATER_MODE = 1;
    static final int MULTI_PRESS_POWER_BRIGHTNESS_BOOST = 2;

    // These need to match the documentation/constant in
    // core/res/res/values/config.xml
    static final int LONG_PRESS_HOME_NOTHING = 0;
    static final int LONG_PRESS_HOME_RECENT_SYSTEM_UI = 1;
    static final int LONG_PRESS_HOME_ASSIST = 2;
    static final int LAST_LONG_PRESS_HOME_BEHAVIOR = LONG_PRESS_HOME_ASSIST;

    static final int DOUBLE_TAP_HOME_NOTHING = 0;
    static final int DOUBLE_TAP_HOME_RECENT_SYSTEM_UI = 1;

    static final int SHORT_PRESS_WINDOW_NOTHING = 0;
    static final int SHORT_PRESS_WINDOW_PICTURE_IN_PICTURE = 1;

    static final int SHORT_PRESS_SLEEP_GO_TO_SLEEP = 0;
    static final int SHORT_PRESS_SLEEP_GO_TO_SLEEP_AND_GO_HOME = 1;

    static final int PENDING_KEY_NULL = -1;

    // Controls navigation bar opacity depending on which workspace stacks are currently
    // visible.
    // Nav bar is always opaque when either the freeform stack or docked stack is visible.
    static final int NAV_BAR_OPAQUE_WHEN_FREEFORM_OR_DOCKED = 0;
    // Nav bar is always translucent when the freeform stack is visible, otherwise always opaque.
    static final int NAV_BAR_TRANSLUCENT_WHEN_FREEFORM_OPAQUE_OTHERWISE = 1;

    static final int APPLICATION_MEDIA_SUBLAYER = -2;
    static final int APPLICATION_MEDIA_OVERLAY_SUBLAYER = -1;
    static final int APPLICATION_PANEL_SUBLAYER = 1;
    static final int APPLICATION_SUB_PANEL_SUBLAYER = 2;
    static final int APPLICATION_ABOVE_SUB_PANEL_SUBLAYER = 3;

    static public final String SYSTEM_DIALOG_REASON_KEY = "reason";
    static public final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
    static public final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    static public final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    static public final String SYSTEM_DIALOG_REASON_ASSIST = "assist";
	//wangyh add 
	public static final String PRIZE_LOCK_STATE = "prize_lock_state";

    /**
     * These are the system UI flags that, when changing, can cause the layout
     * of the screen to change.
     */
    static final int SYSTEM_UI_CHANGING_LAYOUT =
              View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.STATUS_BAR_TRANSLUCENT
            | View.NAVIGATION_BAR_TRANSLUCENT
            | View.STATUS_BAR_TRANSPARENT
            | View.NAVIGATION_BAR_TRANSPARENT;

    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .build();

    // The panic gesture may become active only after the keyguard is dismissed and the immersive
    // app shows again. If that doesn't happen for 30s we drop the gesture.
    private static final long PANIC_GESTURE_EXPIRATION = 30000;

    private static final String SYSUI_PACKAGE = "com.android.systemui";
    private static final String SYSUI_SCREENSHOT_SERVICE =
            "com.android.systemui.screenshot.TakeScreenshotService";
    private static final String SYSUI_SCREENSHOT_ERROR_RECEIVER =
            "com.android.systemui.screenshot.ScreenshotServiceErrorReceiver";

    private static final int NAV_BAR_BOTTOM = 0;
    private static final int NAV_BAR_RIGHT = 1;
    private static final int NAV_BAR_LEFT = 2;


	/*Prize-pull cpu core num and freq when wake up-chenlong-20180405-start*/
	private IPerfServiceWrapper mPerfService = null;
	/*Prize-pull cpu core num and freq when wake up-chenlong-20180405-end*/

    /**
     * Keyguard stuff
     */
    private WindowState mKeyguardScrim;
    private boolean mKeyguardHidden;
    private boolean mKeyguardDrawnOnce;

    /* Table of Application Launch keys.  Maps from key codes to intent categories.
     *
     * These are special keys that are used to launch particular kinds of applications,
     * such as a web browser.  HID defines nearly a hundred of them in the Consumer (0x0C)
     * usage page.  We don't support quite that many yet...
     */
    static SparseArray<String> sApplicationLaunchKeyCategories;
    static {
        sApplicationLaunchKeyCategories = new SparseArray<String>();
        sApplicationLaunchKeyCategories.append(
                KeyEvent.KEYCODE_EXPLORER, Intent.CATEGORY_APP_BROWSER);
        sApplicationLaunchKeyCategories.append(
                KeyEvent.KEYCODE_ENVELOPE, Intent.CATEGORY_APP_EMAIL);
        sApplicationLaunchKeyCategories.append(
                KeyEvent.KEYCODE_CONTACTS, Intent.CATEGORY_APP_CONTACTS);
        sApplicationLaunchKeyCategories.append(
                KeyEvent.KEYCODE_CALENDAR, Intent.CATEGORY_APP_CALENDAR);
        sApplicationLaunchKeyCategories.append(
                KeyEvent.KEYCODE_MUSIC, Intent.CATEGORY_APP_MUSIC);
        sApplicationLaunchKeyCategories.append(
                KeyEvent.KEYCODE_CALCULATOR, Intent.CATEGORY_APP_CALCULATOR);
    }

    /** Amount of time (in milliseconds) to wait for windows drawn before powering on. */
    static final int WAITING_FOR_DRAWN_TIMEOUT = 1000;

    /** Amount of time (in milliseconds) a toast window can be shown. */
    public static final int TOAST_WINDOW_TIMEOUT = 3500; // 3.5 seconds

    /**
     * Lock protecting internal state.  Must not call out into window
     * manager with lock held.  (This lock will be acquired in places
     * where the window manager is calling in with its own lock held.)
     */
    private final Object mLock = new Object();

    Context mContext;
    IWindowManager mWindowManager;
    WindowManagerFuncs mWindowManagerFuncs;
    WindowManagerInternal mWindowManagerInternal;
    PowerManager mPowerManager;
    ActivityManagerInternal mActivityManagerInternal;
    InputManagerInternal mInputManagerInternal;
    DreamManagerInternal mDreamManagerInternal;
    PowerManagerInternal mPowerManagerInternal;
    IStatusBarService mStatusBarService;
    StatusBarManagerInternal mStatusBarManagerInternal;
    boolean mPreloadedRecentApps;
    final Object mServiceAquireLock = new Object();
    Vibrator mVibrator; // Vibrator for giving feedback of orientation changes
    SearchManager mSearchManager;
    AccessibilityManager mAccessibilityManager;
    BurnInProtectionHelper mBurnInProtectionHelper;
    AppOpsManager mAppOpsManager;
    private boolean mHasFeatureWatch;
	private ActivityManager mAM;
    // Vibrator pattern for haptic feedback of a long press.
    long[] mLongPressVibePattern;

    // Vibrator pattern for haptic feedback of virtual key press.
    long[] mVirtualKeyVibePattern;

    // Vibrator pattern for a short vibration.
    long[] mKeyboardTapVibePattern;

    // Vibrator pattern for a short vibration when tapping on an hour/minute tick of a Clock.
    long[] mClockTickVibePattern;

    // Vibrator pattern for a short vibration when tapping on a day/month/year date of a Calendar.
    long[] mCalendarDateVibePattern;

    // Vibrator pattern for haptic feedback during boot when safe mode is disabled.
    long[] mSafeModeDisabledVibePattern;

    // Vibrator pattern for haptic feedback during boot when safe mode is enabled.
    long[] mSafeModeEnabledVibePattern;

    // Vibrator pattern for haptic feedback of a context click.
    long[] mContextClickVibePattern;

    /** If true, hitting shift & menu will broadcast Intent.ACTION_BUG_REPORT */
    boolean mEnableShiftMenuBugReports = false;

    boolean mSafeMode;
    WindowState mStatusBar = null;
    int mStatusBarHeight;
    WindowState mNavigationBar = null;
    boolean mHasNavigationBar = false;
    boolean mNavigationBarCanMove = false; // can the navigation bar ever move to the side?
    int mNavigationBarPosition = NAV_BAR_BOTTOM;
    int[] mNavigationBarHeightForRotationDefault = new int[4];
    int[] mNavigationBarWidthForRotationDefault = new int[4];
    int[] mNavigationBarHeightForRotationInCarMode = new int[4];
    int[] mNavigationBarWidthForRotationInCarMode = new int[4];

    private LongSparseArray<IShortcutService> mShortcutKeyServices = new LongSparseArray<>();

    // Whether to allow dock apps with METADATA_DOCK_HOME to temporarily take over the Home key.
    // This is for car dock and this is updated from resource.
    private boolean mEnableCarDockHomeCapture = true;

    boolean mBootMessageNeedsHiding;
    KeyguardServiceDelegate mKeyguardDelegate;
    final Runnable mWindowManagerDrawCallback = new Runnable() {
        @Override
        public void run() {
            if (DEBUG_WAKEUP) Slog.i(TAG, "All windows ready for display!");
            mHandler.sendEmptyMessage(MSG_WINDOW_MANAGER_DRAWN_COMPLETE);
        }
    };
    final DrawnListener mKeyguardDrawnCallback = new DrawnListener() {
        @Override
        public void onDrawn() {
            if (DEBUG_WAKEUP) Slog.d(TAG, "mKeyguardDelegate.ShowListener.onDrawn.");
            mHandler.sendEmptyMessage(MSG_KEYGUARD_DRAWN_COMPLETE);
        }
    };

    GlobalActions mGlobalActions;
    Handler mHandler;
    WindowState mLastInputMethodWindow = null;
    WindowState mLastInputMethodTargetWindow = null;

    // FIXME This state is shared between the input reader and handler thread.
    // Technically it's broken and buggy but it has been like this for many years
    // and we have not yet seen any problems.  Someday we'll rewrite this logic
    // so that only one thread is involved in handling input policy.  Unfortunately
    // it's on a critical path for power management so we can't just post the work to the
    // handler thread.  We'll need to resolve this someday by teaching the input dispatcher
    // to hold wakelocks during dispatch and eliminating the critical path.
    volatile boolean mPowerKeyHandled;
    volatile boolean mBackKeyHandled;
    volatile boolean mBeganFromNonInteractive;
    volatile int mPowerKeyPressCounter;
    volatile boolean mEndCallKeyHandled;
    volatile boolean mCameraGestureTriggeredDuringGoingToSleep;
    volatile boolean mGoingToSleep;
    volatile boolean mRecentsVisible;
    volatile boolean mTvPictureInPictureVisible;

    // Used to hold the last user key used to wake the device.  This helps us prevent up events
    // from being passed to the foregrounded app without a corresponding down event
    volatile int mPendingWakeKey = PENDING_KEY_NULL;

    int mRecentAppsHeldModifiers;
    boolean mLanguageSwitchKeyPressed;

    int mLidState = LID_ABSENT;
    int mCameraLensCoverState = CAMERA_LENS_COVER_ABSENT;
    boolean mHaveBuiltInKeyboard;

    boolean mSystemReady;
    boolean mSystemBooted;
    private boolean mDeferBindKeyguard;
    boolean mHdmiPlugged;
    HdmiControl mHdmiControl;
    IUiModeManager mUiModeManager;
    int mUiMode;
    int mDockMode = Intent.EXTRA_DOCK_STATE_UNDOCKED;
    int mLidOpenRotation;
    int mCarDockRotation;
    int mDeskDockRotation;
    int mUndockedHdmiRotation;
    int mDemoHdmiRotation;
    boolean mDemoHdmiRotationLock;
    int mDemoRotation;
    boolean mDemoRotationLock;

    boolean mWakeGestureEnabledSetting;
    MyWakeGestureListener mWakeGestureListener;

    // Default display does not rotate, apps that require non-default orientation will have to
    // have the orientation emulated.
    private boolean mForceDefaultOrientation = false;

    int mUserRotationMode = WindowManagerPolicy.USER_ROTATION_FREE;
    int mUserRotation = Surface.ROTATION_0;
    boolean mAccelerometerDefault;

    boolean mSupportAutoRotation;
    int mAllowAllRotations = -1;
    boolean mCarDockEnablesAccelerometer;
    boolean mDeskDockEnablesAccelerometer;
    int mLidKeyboardAccessibility;
    int mLidNavigationAccessibility;
    boolean mLidControlsScreenLock;
    boolean mLidControlsSleep;
    int mShortPressOnPowerBehavior;
    int mLongPressOnPowerBehavior;
    int mDoublePressOnPowerBehavior;
    int mTriplePressOnPowerBehavior;
    int mLongPressOnBackBehavior;
    int mShortPressOnSleepBehavior;
    int mShortPressWindowBehavior;
    boolean mAwake;
    boolean mScreenOnEarly;
    boolean mScreenOnFully;
    ScreenOnListener mScreenOnListener;
    boolean mKeyguardDrawComplete;
    boolean mWindowManagerDrawComplete;
    boolean mOrientationSensorEnabled = false;
    int mCurrentAppOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    boolean mHasSoftInput = false;
    boolean mTranslucentDecorEnabled = true;
    boolean mUseTvRouting;

    int mPointerLocationMode = 0; // guarded by mLock

    // The last window we were told about in focusChanged.
    WindowState mFocusedWindow;
    IApplicationToken mFocusedApp;

    PointerLocationView mPointerLocationView;

    // The current size of the screen; really; extends into the overscan area of
    // the screen and doesn't account for any system elements like the status bar.
    int mOverscanScreenLeft, mOverscanScreenTop;
    int mOverscanScreenWidth, mOverscanScreenHeight;
    // The current visible size of the screen; really; (ir)regardless of whether the status
    // bar can be hidden but not extending into the overscan area.
    int mUnrestrictedScreenLeft, mUnrestrictedScreenTop;
    int mUnrestrictedScreenWidth, mUnrestrictedScreenHeight;
    // Like mOverscanScreen*, but allowed to move into the overscan region where appropriate.
    int mRestrictedOverscanScreenLeft, mRestrictedOverscanScreenTop;
    int mRestrictedOverscanScreenWidth, mRestrictedOverscanScreenHeight;
    // The current size of the screen; these may be different than (0,0)-(dw,dh)
    // if the status bar can't be hidden; in that case it effectively carves out
    // that area of the display from all other windows.
    int mRestrictedScreenLeft, mRestrictedScreenTop;
    int mRestrictedScreenWidth, mRestrictedScreenHeight;
    // During layout, the current screen borders accounting for any currently
    // visible system UI elements.
    int mSystemLeft, mSystemTop, mSystemRight, mSystemBottom;
    // For applications requesting stable content insets, these are them.
    int mStableLeft, mStableTop, mStableRight, mStableBottom;
    // For applications requesting stable content insets but have also set the
    // fullscreen window flag, these are the stable dimensions without the status bar.
    int mStableFullscreenLeft, mStableFullscreenTop;
    int mStableFullscreenRight, mStableFullscreenBottom;
    // During layout, the current screen borders with all outer decoration
    // (status bar, input method dock) accounted for.
    int mCurLeft, mCurTop, mCurRight, mCurBottom;
    // During layout, the frame in which content should be displayed
    // to the user, accounting for all screen decoration except for any
    // space they deem as available for other content.  This is usually
    // the same as mCur*, but may be larger if the screen decor has supplied
    // content insets.
    int mContentLeft, mContentTop, mContentRight, mContentBottom;
    // During layout, the frame in which voice content should be displayed
    // to the user, accounting for all screen decoration except for any
    // space they deem as available for other content.
    int mVoiceContentLeft, mVoiceContentTop, mVoiceContentRight, mVoiceContentBottom;
    // During layout, the current screen borders along which input method
    // windows are placed.
    int mDockLeft, mDockTop, mDockRight, mDockBottom;
    // During layout, the layer at which the doc window is placed.
    int mDockLayer;
    // During layout, this is the layer of the status bar.
    int mStatusBarLayer;
    int mLastSystemUiFlags;
    // Bits that we are in the process of clearing, so we want to prevent
    // them from being set by applications until everything has been updated
    // to have them clear.
    int mResettingSystemUiFlags = 0;
    // Bits that we are currently always keeping cleared.
    int mForceClearedSystemUiFlags = 0;
    int mLastFullscreenStackSysUiFlags;
    int mLastDockedStackSysUiFlags;
    final Rect mNonDockedStackBounds = new Rect();
    final Rect mDockedStackBounds = new Rect();
    final Rect mLastNonDockedStackBounds = new Rect();
    final Rect mLastDockedStackBounds = new Rect();

    // What we last reported to system UI about whether the compatibility
    // menu needs to be displayed.
    boolean mLastFocusNeedsMenu = false;
    // If nonzero, a panic gesture was performed at that time in uptime millis and is still pending.
    private long mPendingPanicGestureUptime;

    InputConsumer mInputConsumer = null;

    static final Rect mTmpParentFrame = new Rect();
    static final Rect mTmpDisplayFrame = new Rect();
    static final Rect mTmpOverscanFrame = new Rect();
    static final Rect mTmpContentFrame = new Rect();
    static final Rect mTmpVisibleFrame = new Rect();
    static final Rect mTmpDecorFrame = new Rect();
    static final Rect mTmpStableFrame = new Rect();
    static final Rect mTmpNavigationFrame = new Rect();
    static final Rect mTmpOutsetFrame = new Rect();
    private static final Rect mTmpRect = new Rect();

    WindowState mTopFullscreenOpaqueWindowState;
    WindowState mTopFullscreenOpaqueOrDimmingWindowState;
    WindowState mTopDockedOpaqueWindowState;
    WindowState mTopDockedOpaqueOrDimmingWindowState;
    HashSet<IApplicationToken> mAppsToBeHidden = new HashSet<IApplicationToken>();
    HashSet<IApplicationToken> mAppsThatDismissKeyguard = new HashSet<IApplicationToken>();
    boolean mTopIsFullscreen;
    boolean mForceStatusBar;
    boolean mForceStatusBarFromKeyguard;
    private boolean mForceStatusBarTransparent;
    int mNavBarOpacityMode = NAV_BAR_OPAQUE_WHEN_FREEFORM_OR_DOCKED;
    boolean mHideLockScreen;
    boolean mForcingShowNavBar;
    int mForcingShowNavBarLayer;
    /*-prize-add by lihuangyuan,for check whether has showonlock window-2018-04-04-start*/
    boolean mIsShowOnLockScreen;
    /*-prize-add by lihuangyuan,for check whether has showonlock window-2018-04-04-start*/

    //add for status bar inverse style. prize-linkh-20150906
    int mLastStatusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_DEFALUT;
    int mLastStatusBarAutoInverse = 0; // 0->false, 1->true.
    
    // States of keyguard dismiss.
    private static final int DISMISS_KEYGUARD_NONE = 0; // Keyguard not being dismissed.
    private static final int DISMISS_KEYGUARD_START = 1; // Keyguard needs to be dismissed.
    private static final int DISMISS_KEYGUARD_CONTINUE = 2; // Keyguard has been dismissed.
    int mDismissKeyguard = DISMISS_KEYGUARD_NONE;

    /**
     * Indicates that we asked the Keyguard to be dismissed and we just wait for the Keyguard to
     * dismiss itself.
     */
    @GuardedBy("Lw")
    private boolean mCurrentlyDismissingKeyguard;

    /** The window that is currently dismissing the keyguard. Dismissing the keyguard must only
     * be done once per window. */
    private WindowState mWinDismissingKeyguard;

    /** When window is currently dismissing the keyguard, dismissing the keyguard must handle
     * the keygaurd secure state change instantly case, e.g. the use case of inserting a PIN
     * lock SIM card. This variable is used to record the previous keyguard secure state for
     * monitoring secure state change on window dismissing keyguard. */
    private boolean mSecureDismissingKeyguard;

    /** The window that is currently showing "over" the keyguard. If there is an app window
     * belonging to another app on top of this the keyguard shows. If there is a fullscreen
     * app window under this, still dismiss the keyguard but don't show the app underneath. Show
     * the wallpaper. */
    private WindowState mWinShowWhenLocked;

    boolean mShowingLockscreen;
    boolean mShowingDream;
    boolean mDreamingLockscreen;
    boolean mDreamingSleepTokenNeeded;
    SleepToken mDreamingSleepToken;
    SleepToken mScreenOffSleepToken;
    boolean mKeyguardSecure;
    boolean mKeyguardSecureIncludingHidden;
    volatile boolean mKeyguardOccluded;
    boolean mHomePressed;
    boolean mHomeConsumed;
    boolean mHomeDoubleTapPending;
    Intent mHomeIntent;
    Intent mCarDockIntent;
    Intent mDeskDockIntent;
    boolean mSearchKeyShortcutPending;
    boolean mConsumeSearchKeyUp;
    boolean mAssistKeyLongPressed;
    boolean mPendingMetaAction;
    boolean mPendingCapsLockToggle;
    int mMetaState;
    int mInitialMetaState;
    boolean mForceShowSystemBars;

    // support for activating the lock screen while the screen is on
    boolean mAllowLockscreenWhenOn;
    int mLockScreenTimeout;
    boolean mLockScreenTimerActive;

    // Behavior of ENDCALL Button.  (See Settings.System.END_BUTTON_BEHAVIOR.)
    int mEndcallBehavior;

    // Behavior of POWER button while in-call and screen on.
    // (See Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR.)
    int mIncallPowerBehavior;

    Display mDisplay;

    private int mDisplayRotation;

    int mLandscapeRotation = 0;  // default landscape rotation
    int mSeascapeRotation = 0;   // "other" landscape rotation, 180 degrees from mLandscapeRotation
    int mPortraitRotation = 0;   // default portrait rotation
    int mUpsideDownRotation = 0; // "other" portrait rotation

    int mOverscanLeft = 0;
    int mOverscanTop = 0;
    int mOverscanRight = 0;
    int mOverscanBottom = 0;

    // What we do when the user long presses on home
    private int mLongPressOnHomeBehavior;

    // What we do when the user double-taps on home
    private int mDoubleTapOnHomeBehavior;

    // Allowed theater mode wake actions
    private boolean mAllowTheaterModeWakeFromKey;
    private boolean mAllowTheaterModeWakeFromPowerKey;
    private boolean mAllowTheaterModeWakeFromMotion;
    private boolean mAllowTheaterModeWakeFromMotionWhenNotDreaming;
    private boolean mAllowTheaterModeWakeFromCameraLens;
    private boolean mAllowTheaterModeWakeFromLidSwitch;
    private boolean mAllowTheaterModeWakeFromWakeGesture;

    // Whether to support long press from power button in non-interactive mode
    private boolean mSupportLongPressPowerWhenNonInteractive;

    // Whether to go to sleep entering theater mode from power button
    private boolean mGoToSleepOnButtonPressTheaterMode;

    // Screenshot trigger states
    // Time to volume and power must be pressed within this interval of each other.
    private static final long SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS = 150;
    // Increase the chord delay when taking a screenshot from the keyguard
    private static final float KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER = 2.5f;
    private boolean mScreenshotChordEnabled;
    private boolean mScreenshotChordVolumeDownKeyTriggered;
    private long mScreenshotChordVolumeDownKeyTime;
    private boolean mScreenshotChordVolumeDownKeyConsumed;
    private boolean mScreenshotChordVolumeUpKeyTriggered;
    private boolean mScreenshotChordPowerKeyTriggered;
    private long mScreenshotChordPowerKeyTime;

    /* The number of steps between min and max brightness */
    private static final int BRIGHTNESS_STEPS = 10;

    SettingsObserver mSettingsObserver;
    ShortcutManager mShortcutManager;
    PowerManager.WakeLock mBroadcastWakeLock;
    PowerManager.WakeLock mPowerKeyWakeLock;
	//add liup 20150601 sleepgesture start
	PowerManager.WakeLock mSleepGestureWakeLock;
	//add liup 20150601 sleepgesture end
    boolean mHavePendingMediaKeyRepeatWithWakeLock;

    private int mCurrentUserId;

    // Maps global key codes to the components that will handle them.
    private GlobalKeyManager mGlobalKeyManager;

    // Fallback actions by key code.
    private final SparseArray<KeyCharacterMap.FallbackAction> mFallbackActions =
            new SparseArray<KeyCharacterMap.FallbackAction>();

    private final LogDecelerateInterpolator mLogDecelerateInterpolator
            = new LogDecelerateInterpolator(100, 0);

    private final MutableBoolean mTmpBoolean = new MutableBoolean(false);

    private static final int MSG_ENABLE_POINTER_LOCATION = 1;
    private static final int MSG_DISABLE_POINTER_LOCATION = 2;
    private static final int MSG_DISPATCH_MEDIA_KEY_WITH_WAKE_LOCK = 3;
    private static final int MSG_DISPATCH_MEDIA_KEY_REPEAT_WITH_WAKE_LOCK = 4;
    private static final int MSG_KEYGUARD_DRAWN_COMPLETE = 5;
    private static final int MSG_KEYGUARD_DRAWN_TIMEOUT = 6;
    private static final int MSG_WINDOW_MANAGER_DRAWN_COMPLETE = 7;
    private static final int MSG_DISPATCH_SHOW_RECENTS = 9;
    private static final int MSG_DISPATCH_SHOW_GLOBAL_ACTIONS = 10;
    private static final int MSG_HIDE_BOOT_MESSAGE = 11;
    private static final int MSG_LAUNCH_VOICE_ASSIST_WITH_WAKE_LOCK = 12;
    private static final int MSG_POWER_DELAYED_PRESS = 13;
    private static final int MSG_POWER_LONG_PRESS = 14;
    private static final int MSG_UPDATE_DREAMING_SLEEP_TOKEN = 15;
    private static final int MSG_REQUEST_TRANSIENT_BARS = 16;
    private static final int MSG_SHOW_TV_PICTURE_IN_PICTURE_MENU = 17;
    private static final int MSG_BACK_LONG_PRESS = 18;
    private static final int MSG_DISPOSE_INPUT_CONSUMER = 19;

    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_STATUS = 0;
    private static final int MSG_REQUEST_TRANSIENT_BARS_ARG_NAVIGATION = 1;

    /* prize-add- 0 180 rotation, cut lcd ears then cover with black view-liyongli-20180628-begin */
    private WindowState prizeCoverEarsWin = null;
    private WindowManager.LayoutParams  coverEarsWinAttrs = null;
    /* prize-add- 0 180 rotation, cut lcd ears then cover with black view-liyongli-20180628-end */
    
	/*PRIZE-three_slideshot-huangdianjun-2015-04-06-start*/
    private static final String ACTION = "org.android.broadcastreceiverregister.SLIDESCREENSHOT";
    /*PRIZE-three_slideshot-huangdianjun-2015-04-06-end*/
  /*PRIZE-three_slideshot-huangdianjun-2015-04-06-start*/
    BroadcastReceiver mSlideScreenshotReceiver = new BroadcastReceiver() {
        @Override 
        public void onReceive(Context context, Intent intent) {
              
            Log.v("huangdianjun","**********SlideScreenshot****************");
            if (mScreenshotChordEnabled) {
                // reduce timeout to take screenshot. prize-linkh-20160804
                //mHandler.postDelayed(mScreenshotRunnable, getScreenshotChordLongPressDelay());
				if(isFloatShow()){
					hideFloatForScreenshot();
				}
                mHandler.postDelayed(mScreenshotRunnable, 100);
            }   
        }
    };
  /*PRIZE-three_slideshot-huangdianjun-2015-04-06-end*/
    private class PolicyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENABLE_POINTER_LOCATION:
                    enablePointerLocation();
                    break;
                case MSG_DISABLE_POINTER_LOCATION:
                    disablePointerLocation();
                    break;
                case MSG_DISPATCH_MEDIA_KEY_WITH_WAKE_LOCK:
                    dispatchMediaKeyWithWakeLock((KeyEvent)msg.obj);
                    break;
                case MSG_DISPATCH_MEDIA_KEY_REPEAT_WITH_WAKE_LOCK:
                    dispatchMediaKeyRepeatWithWakeLock((KeyEvent)msg.obj);
                    break;
                case MSG_DISPATCH_SHOW_RECENTS:
                    showRecentApps(false, msg.arg1 != 0);
                    break;
                case MSG_DISPATCH_SHOW_GLOBAL_ACTIONS:
                    showGlobalActionsInternal();
                    break;
                case MSG_KEYGUARD_DRAWN_COMPLETE:
                    if (DEBUG_WAKEUP) Slog.w(TAG, "Setting mKeyguardDrawComplete");
                    finishKeyguardDrawn();
                    break;
                case MSG_KEYGUARD_DRAWN_TIMEOUT:
                    Slog.w(TAG, "Keyguard drawn timeout. Setting mKeyguardDrawComplete");
                    finishKeyguardDrawn();
                    break;
                case MSG_WINDOW_MANAGER_DRAWN_COMPLETE:
                    if (DEBUG_WAKEUP) Slog.w(TAG, "Setting mWindowManagerDrawComplete");
                    finishWindowsDrawn();
                    break;
                case MSG_HIDE_BOOT_MESSAGE:
                    handleHideBootMessage();
                    break;
                case MSG_LAUNCH_VOICE_ASSIST_WITH_WAKE_LOCK:
                    launchVoiceAssistWithWakeLock(msg.arg1 != 0);
                    break;
                case MSG_POWER_DELAYED_PRESS:
                    powerPress((Long)msg.obj, msg.arg1 != 0, msg.arg2);
                    finishPowerKeyPress();
                    break;
                case MSG_POWER_LONG_PRESS:
                    powerLongPress();
                    break;
                case MSG_UPDATE_DREAMING_SLEEP_TOKEN:
                    updateDreamingSleepToken(msg.arg1 != 0);
                    break;
                case MSG_REQUEST_TRANSIENT_BARS:
                    WindowState targetBar = (msg.arg1 == MSG_REQUEST_TRANSIENT_BARS_ARG_STATUS) ?
                            mStatusBar : mNavigationBar;
                    if (targetBar != null) {
                        requestTransientBars(targetBar);
                    }
                    break;
                case MSG_SHOW_TV_PICTURE_IN_PICTURE_MENU:
                    showTvPictureInPictureMenuInternal();
                    break;
                case MSG_BACK_LONG_PRESS:
                    backLongPress();
                    break;
                case MSG_DISPOSE_INPUT_CONSUMER:
                    disposeInputConsumer((InputConsumer) msg.obj);
                    break;
            }
        }
    }

    private UEventObserver mHDMIObserver = new UEventObserver() {
        @Override
        public void onUEvent(UEventObserver.UEvent event) {
            setHdmiPlugged("1".equals(event.get("SWITCH_STATE")));
        }
    };

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            // Observe all users' changes
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.END_BUTTON_BEHAVIOR), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.WAKE_GESTURE_ENABLED), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.ACCELEROMETER_ROTATION), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.USER_ROTATION), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SCREEN_OFF_TIMEOUT), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.POINTER_LOCATION), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.DEFAULT_INPUT_METHOD), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.IMMERSIVE_MODE_CONFIRMATIONS), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.Global.getUriFor(
                    Settings.Global.POLICY_CONTROL), false, this,
                    UserHandle.USER_ALL);
            updateSettings();
        }

        @Override public void onChange(boolean selfChange) {
            updateSettings();
            updateRotation(false);
        }
    }

    class MyWakeGestureListener extends WakeGestureListener {
        MyWakeGestureListener(Context context, Handler handler) {
            super(context, handler);
        }

        @Override
        public void onWakeUp() {
            synchronized (mLock) {
                if (shouldEnableWakeGestureLp()) {
                    performHapticFeedbackLw(null, HapticFeedbackConstants.VIRTUAL_KEY, false);
                    wakeUp(SystemClock.uptimeMillis(), mAllowTheaterModeWakeFromWakeGesture,
                            "android.policy:GESTURE");
                }
            }
        }
    }

    class MyOrientationListener extends WindowOrientationListener {
        private final Runnable mUpdateRotationRunnable = new Runnable() {
            @Override
            public void run() {
                // send interaction hint to improve redraw performance
                mPowerManagerInternal.powerHint(PowerManagerInternal.POWER_HINT_INTERACTION, 0);
                updateRotation(false);
            }
        };

        MyOrientationListener(Context context, Handler handler) {
            super(context, handler);
        }

        @Override
        public void onProposedRotationChanged(int rotation) {
            if (localLOGV) Slog.v(TAG, "onProposedRotationChanged, rotation=" + rotation);
            mHandler.post(mUpdateRotationRunnable);
        }
    }
    MyOrientationListener mOrientationListener;

    private final StatusBarController mStatusBarController = new StatusBarController();

    private final BarController mNavigationBarController = new BarController("NavigationBar",
            View.NAVIGATION_BAR_TRANSIENT,
            View.NAVIGATION_BAR_UNHIDE,
            View.NAVIGATION_BAR_TRANSLUCENT,
            StatusBarManager.WINDOW_NAVIGATION_BAR,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            View.NAVIGATION_BAR_TRANSPARENT);

    private ImmersiveModeConfirmation mImmersiveModeConfirmation;

    private SystemGesturesPointerEventListener mSystemGestures;

    IStatusBarService getStatusBarService() {
        synchronized (mServiceAquireLock) {
            if (mStatusBarService == null) {
                mStatusBarService = IStatusBarService.Stub.asInterface(
                        ServiceManager.getService("statusbar"));
            }
            return mStatusBarService;
        }
    }

    StatusBarManagerInternal getStatusBarManagerInternal() {
        synchronized (mServiceAquireLock) {
            if (mStatusBarManagerInternal == null) {
                mStatusBarManagerInternal =
                        LocalServices.getService(StatusBarManagerInternal.class);
            }
            return mStatusBarManagerInternal;
        }
    }

    /*
     * We always let the sensor be switched on by default except when
     * the user has explicitly disabled sensor based rotation or when the
     * screen is switched off.
     */
    boolean needSensorRunningLp() {
        if (mSupportAutoRotation) {
            if (mCurrentAppOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    || mCurrentAppOrientation == ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                    || mCurrentAppOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                    || mCurrentAppOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                // If the application has explicitly requested to follow the
                // orientation, then we need to turn the sensor on.
                return true;
            }
        }
        if ((mCarDockEnablesAccelerometer && mDockMode == Intent.EXTRA_DOCK_STATE_CAR) ||
                (mDeskDockEnablesAccelerometer && (mDockMode == Intent.EXTRA_DOCK_STATE_DESK
                        || mDockMode == Intent.EXTRA_DOCK_STATE_LE_DESK
                        || mDockMode == Intent.EXTRA_DOCK_STATE_HE_DESK))) {
            // enable accelerometer if we are docked in a dock that enables accelerometer
            // orientation management,
            return true;
        }
        if (mUserRotationMode == USER_ROTATION_LOCKED) {
            // If the setting for using the sensor by default is enabled, then
            // we will always leave it on.  Note that the user could go to
            // a window that forces an orientation that does not use the
            // sensor and in theory we could turn it off... however, when next
            // turning it on we won't have a good value for the current
            // orientation for a little bit, which can cause orientation
            // changes to lag, so we'd like to keep it always on.  (It will
            // still be turned off when the screen is off.)
            return false;
        }
        return mSupportAutoRotation;
    }

    /*
     * Various use cases for invoking this function
     * screen turning off, should always disable listeners if already enabled
     * screen turned on and current app has sensor based orientation, enable listeners
     * if not already enabled
     * screen turned on and current app does not have sensor orientation, disable listeners if
     * already enabled
     * screen turning on and current app has sensor based orientation, enable listeners if needed
     * screen turning on and current app has nosensor based orientation, do nothing
     */
    void updateOrientationListenerLp() {
        if (!mOrientationListener.canDetectOrientation()) {
            // If sensor is turned off or nonexistent for some reason
            return;
        }
        // Could have been invoked due to screen turning on or off or
        // change of the currently visible window's orientation.
        if (localLOGV) Slog.v(TAG, "mScreenOnEarly=" + mScreenOnEarly
                + ", mAwake=" + mAwake + ", mCurrentAppOrientation=" + mCurrentAppOrientation
                + ", mOrientationSensorEnabled=" + mOrientationSensorEnabled
                + ", mKeyguardDrawComplete=" + mKeyguardDrawComplete
                + ", mWindowManagerDrawComplete=" + mWindowManagerDrawComplete);
        boolean disable = true;
        // Note: We postpone the rotating of the screen until the keyguard as well as the
        // window manager have reported a draw complete.
        if (mScreenOnEarly && mAwake &&
                mKeyguardDrawComplete && mWindowManagerDrawComplete) {
            if (needSensorRunningLp()) {
                disable = false;
                //enable listener if not already enabled
                if (!mOrientationSensorEnabled) {
                    mOrientationListener.enable();
                    if(localLOGV) Slog.v(TAG, "Enabling listeners");
                    mOrientationSensorEnabled = true;
                }
            }
        }
        //check if sensors need to be disabled
        if (disable && mOrientationSensorEnabled) {
            mOrientationListener.disable();
            if(localLOGV) Slog.v(TAG, "Disabling listeners");
            mOrientationSensorEnabled = false;
        }
    }

	//2018-04-09 liuwei add for faceid.
    private void nofityPowerKeyPower(boolean interactive){
        if(mKeyguardDelegate != null){
            mKeyguardDelegate.nofityPowerKeyPower(interactive);
        }
    }
	//2018-04-09 liuwei add for faceid end
	private void hideFloatForScreenshot(){
        if (mScreenshotConnection == null && mContext != null) {
            Intent iNewFloatWindow = new Intent("android.intent.action.PRIZEHIDETEMP");
            iNewFloatWindow.setPackage("com.android.prizefloatwindow");
            mContext.sendBroadcast(iNewFloatWindow);
        }
    }
	private boolean isFloatShow(){
		if(PrizeOption.PRIZE_NEW_FLOAT_WINDOW && mContext != null){
			int mNewOriginalPrizeFloatWindow = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_NEW_FLOAT_WINDOW,0);
			if(mNewOriginalPrizeFloatWindow == 1){
				return true;
			}
		}
		return false;
	}
    private void interceptPowerKeyDown(KeyEvent event, boolean interactive) {
        // Hold a wake lock until the power key is released.
        if (!mPowerKeyWakeLock.isHeld()) {
            mPowerKeyWakeLock.acquire();
        }

        // Cancel multi-press detection timeout.
        if (mPowerKeyPressCounter != 0) {
            mHandler.removeMessages(MSG_POWER_DELAYED_PRESS);
        }


	      boolean isOpen = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_FACEID_SWITCH , 1) == 1;
		    /* 2018-04-09 liuwei add for faceid.*/
		    if(PrizeOption.PRIZE_FACE_ID && isOpen && !(mAM.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_PINNED)) {
            //sendPowerKeyScreenOnBroadcast(1);
            perfBoost(3000);

            nofityPowerKeyPower(interactive);
        }

        // Detect user pressing the power button in panic when an application has
        // taken over the whole screen.
        boolean panic = mImmersiveModeConfirmation.onPowerKeyDown(interactive,
                SystemClock.elapsedRealtime(), isImmersiveMode(mLastSystemUiFlags),
                isNavBarEmpty(mLastSystemUiFlags));
        if (panic) {
            mHandler.post(mHiddenNavPanic);
        }

        // Latch power key state to detect screenshot chord.
        if (interactive && !mScreenshotChordPowerKeyTriggered
                && (event.getFlags() & KeyEvent.FLAG_FALLBACK) == 0) {
            mScreenshotChordPowerKeyTriggered = true;
            mScreenshotChordPowerKeyTime = event.getDownTime();
            interceptScreenshotChord();
        }

        // Stop ringing or end call if configured to do so when power is pressed.
        TelecomManager telecomManager = getTelecommService();
        boolean hungUp = false;
        if (telecomManager != null) {
            if (telecomManager.isRinging()) {
                // Pressing Power while there's a ringing incoming
                // call should silence the ringer.
                telecomManager.silenceRinger();
            } else if ((mIncallPowerBehavior
                    & Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP) != 0
                    && telecomManager.isInCall() && interactive) {
                // Otherwise, if "Power button ends call" is enabled,
                // the Power button will hang up any current active call.
                hungUp = telecomManager.endCall();
            }
        }

        GestureLauncherService gestureService = LocalServices.getService(
                GestureLauncherService.class);
        boolean gesturedServiceIntercepted = false;
        if (gestureService != null) {
            gesturedServiceIntercepted = gestureService.interceptPowerKeyDown(event, interactive,
                    mTmpBoolean);
            if (mTmpBoolean.value && mGoingToSleep) {
                mCameraGestureTriggeredDuringGoingToSleep = true;
            }
        }

        // If the power key has still not yet been handled, then detect short
        // press, long press, or multi press and decide what to do.
        mPowerKeyHandled = hungUp || mScreenshotChordVolumeDownKeyTriggered
                || mScreenshotChordVolumeUpKeyTriggered || gesturedServiceIntercepted;
        if (!mPowerKeyHandled) {
            if (interactive) {
                // When interactive, we're already awake.
                // Wait for a long press or for the button to be released to decide what to do.
                if (hasLongPressOnPowerBehavior()) {
                    Message msg = mHandler.obtainMessage(MSG_POWER_LONG_PRESS);
                    msg.setAsynchronous(true);
					/* prize modify Delayed popup global actions dialog by lijimeng 20170303 start*/
                    mHandler.sendMessageDelayed(msg,
                            2000);
                           // ViewConfiguration.get(mContext).getDeviceGlobalActionKeyTimeout());
					/* prize modify Delayed popup global actions dialog by lijimeng 20170303 end*/
                }
            } else {
                wakeUpFromPowerKey(event.getDownTime());

                if (mSupportLongPressPowerWhenNonInteractive && hasLongPressOnPowerBehavior()) {
                    Message msg = mHandler.obtainMessage(MSG_POWER_LONG_PRESS);
                    msg.setAsynchronous(true);
					/* prize modify Delayed popup global actions dialog by lijimeng 20170303 start*/
                    mHandler.sendMessageDelayed(msg,
                            2000);
                           // ViewConfiguration.get(mContext).getDeviceGlobalActionKeyTimeout());
					/* prize modify Delayed popup global actions dialog by lijimeng 20170303 end*/
                    mBeganFromNonInteractive = true;
                } else {
                    final int maxCount = getMaxMultiPressPowerCount();

                    if (maxCount <= 1) {
                        mPowerKeyHandled = true;
                    } else {
                        mBeganFromNonInteractive = true;
                    }
                }
            }
        }
    }

    private void interceptPowerKeyUp(KeyEvent event, boolean interactive, boolean canceled) {
        final boolean handled = canceled || mPowerKeyHandled;
        mScreenshotChordPowerKeyTriggered = false;
        cancelPendingScreenshotChordAction();
        cancelPendingPowerKeyAction();

        if (!handled) {
            // Figure out how to handle the key now that it has been released.
            mPowerKeyPressCounter += 1;

            final int maxCount = getMaxMultiPressPowerCount();
            final long eventTime = event.getDownTime();
            if (mPowerKeyPressCounter < maxCount) {
                // This could be a multi-press.  Wait a little bit longer to confirm.
                // Continue holding the wake lock.
                Message msg = mHandler.obtainMessage(MSG_POWER_DELAYED_PRESS,
                        interactive ? 1 : 0, mPowerKeyPressCounter, eventTime);
                msg.setAsynchronous(true);
                mHandler.sendMessageDelayed(msg, ViewConfiguration.getDoubleTapTimeout());
                return;
            }

            // No other actions.  Handle it immediately.
            powerPress(eventTime, interactive, mPowerKeyPressCounter);
        }

        // Done.  Reset our state.
        finishPowerKeyPress();
    }

    private void finishPowerKeyPress() {
        mBeganFromNonInteractive = false;
        mPowerKeyPressCounter = 0;
        if (mPowerKeyWakeLock.isHeld()) {
            mPowerKeyWakeLock.release();
        }
    }

    private void cancelPendingPowerKeyAction() {
        if (!mPowerKeyHandled) {
            mPowerKeyHandled = true;
            mHandler.removeMessages(MSG_POWER_LONG_PRESS);
        }
    }

    private void cancelPendingBackKeyAction() {
        if (!mBackKeyHandled) {
            mBackKeyHandled = true;
            mHandler.removeMessages(MSG_BACK_LONG_PRESS);
        }
    }

    private void powerPress(long eventTime, boolean interactive, int count) {
        if (mScreenOnEarly && !mScreenOnFully) {
            Slog.i(TAG, "Suppressed redundant power key press while "
                    + "already in the process of turning the screen on.");
            /*-prize-add by lihuangyuan,for do finger & powerkey sync,can not turn on screen -2018-04-25-start*/
            if(mWakeupReason == WAKEUP_REASON_FINGERPRINT)
            {
                  //prize add by  lihuangyuan for faceid
                  boolean isOpen = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_FACEID_SWITCH , 1) == 1;
		    if(PrizeOption.PRIZE_FACE_ID && isOpen && !(mAM.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_PINNED)) 
                 {
                    //sendPowerKeyScreenOnBroadcast(1);
                    perfBoost(3000);
                    /*prize modify for 59199 because of fingerprint and power key async come liuwei 2018 2018-06-08 begin*/
                    nofityPowerKeyPower(false);
                    /*prize modify for 59199 because of fingerprint and power key async come liuwei 2018 2018-06-08 end*/
                }
                wakeUpFromPowerKey(SystemClock.uptimeMillis());
            }
            /*-prize-add by lihuangyuan,for do finger & powerkey sync,can not turn on screen -2018-04-25-end*/
            return;
        }

        if (count == 2) {
            powerMultiPressAction(eventTime, interactive, mDoublePressOnPowerBehavior);
        } else if (count == 3) {
            powerMultiPressAction(eventTime, interactive, mTriplePressOnPowerBehavior);
        } else if (interactive && !mBeganFromNonInteractive) {
            switch (mShortPressOnPowerBehavior) {
                case SHORT_PRESS_POWER_NOTHING:
                    break;
                case SHORT_PRESS_POWER_GO_TO_SLEEP:
                    mPowerManager.goToSleep(eventTime,
                            PowerManager.GO_TO_SLEEP_REASON_POWER_BUTTON, 0);
                    break;
                case SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP:
                    mPowerManager.goToSleep(eventTime,
                            PowerManager.GO_TO_SLEEP_REASON_POWER_BUTTON,
                            PowerManager.GO_TO_SLEEP_FLAG_NO_DOZE);
                    break;
                case SHORT_PRESS_POWER_REALLY_GO_TO_SLEEP_AND_GO_HOME:
                    mPowerManager.goToSleep(eventTime,
                            PowerManager.GO_TO_SLEEP_REASON_POWER_BUTTON,
                            PowerManager.GO_TO_SLEEP_FLAG_NO_DOZE);
                    launchHomeFromHotKey();
                    break;
                case SHORT_PRESS_POWER_GO_HOME:
                    launchHomeFromHotKey(true /* awakenFromDreams */, false /*respectKeyguard*/);
                    break;
            }
        }
    }

    private void powerMultiPressAction(long eventTime, boolean interactive, int behavior) {
        switch (behavior) {
            case MULTI_PRESS_POWER_NOTHING:
                break;
            case MULTI_PRESS_POWER_THEATER_MODE:
                if (!isUserSetupComplete()) {
                    Slog.i(TAG, "Ignoring toggling theater mode - device not setup.");
                    break;
                }

                if (isTheaterModeEnabled()) {
                    Slog.i(TAG, "Toggling theater mode off.");
                    Settings.Global.putInt(mContext.getContentResolver(),
                            Settings.Global.THEATER_MODE_ON, 0);
                    if (!interactive) {
                        wakeUpFromPowerKey(eventTime);
                    }
                } else {
                    Slog.i(TAG, "Toggling theater mode on.");
                    Settings.Global.putInt(mContext.getContentResolver(),
                            Settings.Global.THEATER_MODE_ON, 1);

                    if (mGoToSleepOnButtonPressTheaterMode && interactive) {
                        mPowerManager.goToSleep(eventTime,
                                PowerManager.GO_TO_SLEEP_REASON_POWER_BUTTON, 0);
                    }
                }
                break;
            case MULTI_PRESS_POWER_BRIGHTNESS_BOOST:
                Slog.i(TAG, "Starting brightness boost.");
                if (!interactive) {
                    wakeUpFromPowerKey(eventTime);
                }
                mPowerManager.boostScreenBrightness(eventTime);
                break;
        }
    }

    private int getMaxMultiPressPowerCount() {
        if (mTriplePressOnPowerBehavior != MULTI_PRESS_POWER_NOTHING) {
            return 3;
        }
        if (mDoublePressOnPowerBehavior != MULTI_PRESS_POWER_NOTHING) {
            return 2;
        }
        return 1;
    }

    private void powerLongPress() {
		/*PRIZE-add for changesimcard reset dialog-lihuangyuan-2017-06-15-start*/
		if(PrizeOption.PRIZE_CHANGESIM_RESET_DIALOG)
		{
			int prizeresetmode = Settings.System.getInt(mContext.getContentResolver(),"prizeresetmode",0);
			if(prizeresetmode == 1)
			{
				Log.d(TAG, "powerLongPress prizeresetmode:"+prizeresetmode);
				return ;
			}
		}
		/*PRIZE-add for changesimcard reset dialog-lihuangyuan-2017-06-15-end*/
        final int behavior = getResolvedLongPressOnPowerBehavior();
        switch (behavior) {
        case LONG_PRESS_POWER_NOTHING:
            break;
        case LONG_PRESS_POWER_GLOBAL_ACTIONS:
            mPowerKeyHandled = true;
            if (!performHapticFeedbackLw(null, HapticFeedbackConstants.LONG_PRESS, false)) {
                performAuditoryFeedbackForAccessibilityIfNeed();
            }
            showGlobalActionsInternal();
            break;
        case LONG_PRESS_POWER_SHUT_OFF:
        case LONG_PRESS_POWER_SHUT_OFF_NO_CONFIRM:
            mPowerKeyHandled = true;
            performHapticFeedbackLw(null, HapticFeedbackConstants.LONG_PRESS, false);
            sendCloseSystemWindows(SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS);
            mWindowManagerFuncs.shutdown(behavior == LONG_PRESS_POWER_SHUT_OFF);
            break;
        }
    }

    private void backLongPress() {
        mBackKeyHandled = true;

        switch (mLongPressOnBackBehavior) {
            case LONG_PRESS_BACK_NOTHING:
                break;
            case LONG_PRESS_BACK_GO_TO_VOICE_ASSIST:
                Intent intent = new Intent(Intent.ACTION_VOICE_ASSIST);
                startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
                break;
        }
    }

    private void disposeInputConsumer(InputConsumer inputConsumer) {
        if (inputConsumer != null) {
            inputConsumer.dismiss();
        }
    }

    private void sleepPress(long eventTime) {
        if (mShortPressOnSleepBehavior == SHORT_PRESS_SLEEP_GO_TO_SLEEP_AND_GO_HOME) {
            launchHomeFromHotKey(false /* awakenDreams */, true /*respectKeyguard*/);
        }
    }

    private void sleepRelease(long eventTime) {
        switch (mShortPressOnSleepBehavior) {
            case SHORT_PRESS_SLEEP_GO_TO_SLEEP:
            case SHORT_PRESS_SLEEP_GO_TO_SLEEP_AND_GO_HOME:
                Slog.i(TAG, "sleepRelease() calling goToSleep(GO_TO_SLEEP_REASON_SLEEP_BUTTON)");
                mPowerManager.goToSleep(eventTime,
                       PowerManager.GO_TO_SLEEP_REASON_SLEEP_BUTTON, 0);
                break;
        }
    }

    private int getResolvedLongPressOnPowerBehavior() {
        if (FactoryTest.isLongPressOnPowerOffEnabled()) {
            return LONG_PRESS_POWER_SHUT_OFF_NO_CONFIRM;
        }
        return mLongPressOnPowerBehavior;
    }

    private boolean hasLongPressOnPowerBehavior() {
        return getResolvedLongPressOnPowerBehavior() != LONG_PRESS_POWER_NOTHING;
    }

    private boolean hasLongPressOnBackBehavior() {
        return mLongPressOnBackBehavior != LONG_PRESS_BACK_NOTHING;
    }

    private void interceptScreenshotChord() {
        if (mScreenshotChordEnabled
                && mScreenshotChordVolumeDownKeyTriggered && mScreenshotChordPowerKeyTriggered
                && !mScreenshotChordVolumeUpKeyTriggered) {
            final long now = SystemClock.uptimeMillis();
            if (now <= mScreenshotChordVolumeDownKeyTime + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS
                    && now <= mScreenshotChordPowerKeyTime
                            + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS) {
                mScreenshotChordVolumeDownKeyConsumed = true;
                cancelPendingPowerKeyAction();
                mScreenshotRunnable.setScreenshotType(TAKE_SCREENSHOT_FULLSCREEN);
                mHandler.postDelayed(mScreenshotRunnable, getScreenshotChordLongPressDelay());
				if(mHideFloatRunnable != null && isFloatShow()){
					mHandler.postDelayed(mHideFloatRunnable, 300);
				}
            }
        }
    }

    private long getScreenshotChordLongPressDelay() {
        if (mKeyguardDelegate.isShowing()) {
            // Double the time it takes to take a screenshot from the keyguard
            return (long) (KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER *
                    ViewConfiguration.get(mContext).getDeviceGlobalActionKeyTimeout());
        }
        return ViewConfiguration.get(mContext).getDeviceGlobalActionKeyTimeout();
    }

    private void cancelPendingScreenshotChordAction() {
        mHandler.removeCallbacks(mScreenshotRunnable);
		mHandler.removeCallbacks(mHideFloatRunnable);
    }

    private final Runnable mEndCallLongPress = new Runnable() {
        @Override
        public void run() {
            mEndCallKeyHandled = true;
            if (!performHapticFeedbackLw(null, HapticFeedbackConstants.LONG_PRESS, false)) {
                performAuditoryFeedbackForAccessibilityIfNeed();
            }
            showGlobalActionsInternal();
        }
    };

    private class ScreenshotRunnable implements Runnable {
        private int mScreenshotType = TAKE_SCREENSHOT_FULLSCREEN;

        public void setScreenshotType(int screenshotType) {
            mScreenshotType = screenshotType;
        }

        @Override
        public void run() {
            takeScreenshot(mScreenshotType);
        }
    }

    private final ScreenshotRunnable mScreenshotRunnable = new ScreenshotRunnable();

	private class HideFloatRunnable implements Runnable {
		
        @Override
        public void run() {
			hideFloatForScreenshot();
        }
    }

    private final HideFloatRunnable mHideFloatRunnable = new HideFloatRunnable();
	
    @Override
    public void showGlobalActions() {
        mHandler.removeMessages(MSG_DISPATCH_SHOW_GLOBAL_ACTIONS);
        mHandler.sendEmptyMessage(MSG_DISPATCH_SHOW_GLOBAL_ACTIONS);
    }

    void showGlobalActionsInternal() {
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS);
        if (mGlobalActions == null) {
            mGlobalActions = new GlobalActions(mContext, mWindowManagerFuncs);
        }
        final boolean keyguardShowing = isKeyguardShowingAndNotOccluded();
        mGlobalActions.showDialog(keyguardShowing, isDeviceProvisioned());
        if (keyguardShowing) {
            // since it took two seconds of long press to bring this up,
            // poke the wake lock so they have some time to see the dialog.
            mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }
    }

    boolean isDeviceProvisioned() {
        return Settings.Global.getInt(
                mContext.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0) != 0;
    }

    boolean isUserSetupComplete() {
        return Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE, 0, UserHandle.USER_CURRENT) != 0;
    }

    private void handleShortPressOnHome() {
        // Turn on the connected TV and switch HDMI input if we're a HDMI playback device.
        getHdmiControl().turnOnTv();

        // If there's a dream running then use home to escape the dream
        // but don't actually go home.
        if (mDreamManagerInternal != null && mDreamManagerInternal.isDreaming()) {
            mDreamManagerInternal.stopDream(false /*immediate*/);
            return;
        }

        // Go home!
        launchHomeFromHotKey();
    }

    /**
     * Creates an accessor to HDMI control service that performs the operation of
     * turning on TV (optional) and switching input to us. If HDMI control service
     * is not available or we're not a HDMI playback device, the operation is no-op.
     */
    private HdmiControl getHdmiControl() {
        if (null == mHdmiControl) {
            HdmiControlManager manager = (HdmiControlManager) mContext.getSystemService(
                        Context.HDMI_CONTROL_SERVICE);
            HdmiPlaybackClient client = null;
            if (manager != null) {
                client = manager.getPlaybackClient();
            }
            mHdmiControl = new HdmiControl(client);
        }
        return mHdmiControl;
    }

    private static class HdmiControl {
        private final HdmiPlaybackClient mClient;

        private HdmiControl(HdmiPlaybackClient client) {
            mClient = client;
        }

        public void turnOnTv() {
            if (mClient == null) {
                return;
            }
            mClient.oneTouchPlay(new OneTouchPlayCallback() {
                @Override
                public void onComplete(int result) {
                    if (result != HdmiControlManager.RESULT_SUCCESS) {
                        Log.w(TAG, "One touch play failed: " + result);
                    }
                }
            });
        }
    }

    private void handleLongPressOnHome(int deviceId) {
        if (mLongPressOnHomeBehavior == LONG_PRESS_HOME_NOTHING) {
            return;
        }
        mHomeConsumed = true;
        performHapticFeedbackLw(null, HapticFeedbackConstants.LONG_PRESS, false);

        switch (mLongPressOnHomeBehavior) {
            case LONG_PRESS_HOME_RECENT_SYSTEM_UI:
                toggleRecentApps();
                break;
            case LONG_PRESS_HOME_ASSIST:
                launchAssistAction(null, deviceId);
                break;
            default:
                Log.w(TAG, "Undefined home long press behavior: " + mLongPressOnHomeBehavior);
                break;
        }
    }

    private void handleDoubleTapOnHome() {
        if (mDoubleTapOnHomeBehavior == DOUBLE_TAP_HOME_RECENT_SYSTEM_UI) {
            mHomeConsumed = true;
            toggleRecentApps();
        }
    }

    private void showTvPictureInPictureMenu(KeyEvent event) {
        if (DEBUG_INPUT) Log.d(TAG, "showTvPictureInPictureMenu event=" + event);
        mHandler.removeMessages(MSG_SHOW_TV_PICTURE_IN_PICTURE_MENU);
        Message msg = mHandler.obtainMessage(MSG_SHOW_TV_PICTURE_IN_PICTURE_MENU);
        msg.setAsynchronous(true);
        msg.sendToTarget();
    }

    private void showTvPictureInPictureMenuInternal() {
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.showTvPictureInPictureMenu();
        }
    }

    private final Runnable mHomeDoubleTapTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mHomeDoubleTapPending) {
                mHomeDoubleTapPending = false;
                handleShortPressOnHome();
            }
        }
    };

    private boolean isRoundWindow() {
        return mContext.getResources().getConfiguration().isScreenRound();
    }
    
    /* Dynamically hiding nav bar feature. prize-linkh-20150714 */
    static final String LOG_TAG = "PhoneWidowManager";
    private static final boolean IS_ENG_BUILD = "eng".equals(android.os.Build.TYPE);
    private static final boolean MY_DEBUG = "eng".equals(android.os.Build.TYPE);
    static boolean DEBUG_FOR_HIDING_NAVBAR = false;
    private boolean mSupportHidingNavBar = PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR;
    private boolean mHasRegisteredPointerEventListener = false;
    private int mLastNavBarPostion = -1;
    
    /* Nav bar related to mBack key feature. prize-linkh-20160804 */
    // When mBack doesn't work for some reasons, we can provide
    // nav bar for user to rescue this device. 
    public static final boolean SUPPORT_NAV_BAR_FOR_MBACK_DEVICE = PrizeOption.PRIZE_SUPPORT_NAV_BAR_FOR_MBACK_DEVICE;
    //prize tangzhengrong 20180526 Swipe-up Gesture Navigation bar start
    public static final boolean OPEN_GESTURE_NAVIGATION = PrizeOption.PRIZE_SWIPE_UP_GESTURE_NAVIGATION;
    SwipeUpGestureController mSwipeUpGestureController;
    private final static int BACK_GESTURE_STATE = 0;
    private final static int HOME_GESTURE_STATE = 1;
    private final static int RECENT_TASKS_GESTURE_STATE = 2;

    private final static int NON_BOTTOM = 0;
    private final static int CENTER_BOTTOM = 1;
    private final static int LEFT_BOTTOM = 2;
    private final static int RIGHT_BOTTOM = 3;

    private final static int NO_TRACK_POINT_ID = -1;

    private static final String ACTION_BACK = "com.android.systemui.action.SEND_BACK";
    private static final String ACTION_HOME = "com.android.systemui.action.SEND_HOME";
    private static final String ACTION_RECENT_TASKS = "com.android.systemui.action.SEND_RECENT_TASKS";

    private static final String EXTRA_LOCATION = "location";


    public static final boolean SUPPORT_SWIPE_UP_GESTURE_NAVIGATION = PrizeOption.PRIZE_SWIPE_UP_GESTURE_NAVIGATION;
    private boolean EnableGestureNavigation(){
        return Settings.System.getInt(
                mContext.getContentResolver(), Settings.System.PRIZE_SWIPE_UP_GESTURE_STATE, 0) != 0;
    }

    //prize tangzhengrong 20180526 Swipe-up Gesture Navigation bar end
    private boolean canShowNavBarFormBack() {
		//prize tangzhengrong 20180426 Swipe-up Gesture Navigation bar start`
		if(PrizeOption.PRIZE_SWIPE_UP_GESTURE_NAVIGATION)
			return Settings.System.getInt(
                mContext.getContentResolver(), Settings.System.PRIZE_NAVBAR_STATE_FOR_MBACK, 1) != 0;
		//prize tangzhengrong 20180426 Swipe-up Gesture Navigation bar end
        // nav bar is hidden as default.
        return Settings.System.getInt(
                mContext.getContentResolver(), Settings.System.PRIZE_NAVBAR_STATE_FOR_MBACK, 0) != 0;
    }

    
    private void printMyLog(String msg) {
        if(MY_DEBUG) {
            Slog.d(TAG, msg);
        }
    }

    private void showNavBar() {
        if(DEBUG_FOR_HIDING_NAVBAR) {
            Slog.d(LOG_TAG, "showNavBar() ----");
        }
        Intent intent = new Intent("com.prize.nav_bar_control");
        intent.putExtra("command", "show");
        mContext.sendBroadcast(intent);
    }

    private void swipeOutNavigationBar(String reason) {
        if(DEBUG_FOR_HIDING_NAVBAR) {
            Slog.d(LOG_TAG, "swipeOutNavigationBar() reason=" + reason);
        }
        
        //mKeyguardDelegate.isAllowShowVirtualKey() update by liufan-20151214
        if(mKeyguardDelegate != null && mKeyguardDelegate.isAllowShowVirtualKey()
            && mNavigationBar.isGoneForLayoutLw()) {
            //case 1: if we are in the lock screen, we don't show the nav bar.
            //case 2: if the app requests hiding nav bar, we enable the transient state.
            showNavBar();
        } else {
            requestTransientBars(mNavigationBar);
        }
    }
    //END...
   
    /** {@inheritDoc} */
    @Override
    public void init(Context context, IWindowManager windowManager,
            WindowManagerFuncs windowManagerFuncs) {
        mContext = context;
        mWindowManager = windowManager;
        mWindowManagerFuncs = windowManagerFuncs;
        mWindowManagerInternal = LocalServices.getService(WindowManagerInternal.class);
        mActivityManagerInternal = LocalServices.getService(ActivityManagerInternal.class);
        mInputManagerInternal = LocalServices.getService(InputManagerInternal.class);
        mDreamManagerInternal = LocalServices.getService(DreamManagerInternal.class);
        mPowerManagerInternal = LocalServices.getService(PowerManagerInternal.class);
        mAppOpsManager = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
        mHasFeatureWatch = mContext.getPackageManager().hasSystemFeature(FEATURE_WATCH);
		mAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        // Init display burn-in protection
        boolean burnInProtectionEnabled = context.getResources().getBoolean(
                com.android.internal.R.bool.config_enableBurnInProtection);
        // Allow a system property to override this. Used by developer settings.
        boolean burnInProtectionDevMode =
                SystemProperties.getBoolean("persist.debug.force_burn_in", false);
        if (burnInProtectionEnabled || burnInProtectionDevMode) {
            final int minHorizontal;
            final int maxHorizontal;
            final int minVertical;
            final int maxVertical;
            final int maxRadius;
            if (burnInProtectionDevMode) {
                minHorizontal = -8;
                maxHorizontal = 8;
                minVertical = -8;
                maxVertical = -4;
                maxRadius = (isRoundWindow()) ? 6 : -1;
            } else {
                Resources resources = context.getResources();
                minHorizontal = resources.getInteger(
                        com.android.internal.R.integer.config_burnInProtectionMinHorizontalOffset);
                maxHorizontal = resources.getInteger(
                        com.android.internal.R.integer.config_burnInProtectionMaxHorizontalOffset);
                minVertical = resources.getInteger(
                        com.android.internal.R.integer.config_burnInProtectionMinVerticalOffset);
                maxVertical = resources.getInteger(
                        com.android.internal.R.integer.config_burnInProtectionMaxVerticalOffset);
                maxRadius = resources.getInteger(
                        com.android.internal.R.integer.config_burnInProtectionMaxRadius);
            }
            mBurnInProtectionHelper = new BurnInProtectionHelper(
                    context, minHorizontal, maxHorizontal, minVertical, maxVertical, maxRadius);
        }

        mHandler = new PolicyHandler();
        mWakeGestureListener = new MyWakeGestureListener(mContext, mHandler);
        mOrientationListener = new MyOrientationListener(mContext, mHandler);
        try {
            mOrientationListener.setCurrentRotation(windowManager.getRotation());
        } catch (RemoteException ex) { }
        mSettingsObserver = new SettingsObserver(mHandler);
        mSettingsObserver.observe();
        mShortcutManager = new ShortcutManager(context);
        mUiMode = context.getResources().getInteger(
                com.android.internal.R.integer.config_defaultUiModeType);
        mHomeIntent =  new Intent(Intent.ACTION_MAIN, null);
        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        mEnableCarDockHomeCapture = context.getResources().getBoolean(
                com.android.internal.R.bool.config_enableCarDockHomeLaunch);
        mCarDockIntent =  new Intent(Intent.ACTION_MAIN, null);
        mCarDockIntent.addCategory(Intent.CATEGORY_CAR_DOCK);
        mCarDockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        mDeskDockIntent =  new Intent(Intent.ACTION_MAIN, null);
        mDeskDockIntent.addCategory(Intent.CATEGORY_DESK_DOCK);
        mDeskDockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        mPowerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mBroadcastWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "PhoneWindowManager.mBroadcastWakeLock");
        mPowerKeyWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "PhoneWindowManager.mPowerKeyWakeLock");
		//add liup 20150601 sleepgesture start
		mSleepGestureWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.FULL_WAKE_LOCK, 
				"wakeUpScreen"); 
		//add liup 20150601 sleepgesture end
        mEnableShiftMenuBugReports = "1".equals(SystemProperties.get("ro.debuggable"));
        mSupportAutoRotation = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_supportAutoRotation);
        mLidOpenRotation = readRotation(
                com.android.internal.R.integer.config_lidOpenRotation);
        mCarDockRotation = readRotation(
                com.android.internal.R.integer.config_carDockRotation);
        mDeskDockRotation = readRotation(
                com.android.internal.R.integer.config_deskDockRotation);
        mUndockedHdmiRotation = readRotation(
                com.android.internal.R.integer.config_undockedHdmiRotation);
        mCarDockEnablesAccelerometer = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_carDockEnablesAccelerometer);
        mDeskDockEnablesAccelerometer = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_deskDockEnablesAccelerometer);
        mLidKeyboardAccessibility = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lidKeyboardAccessibility);
        mLidNavigationAccessibility = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lidNavigationAccessibility);
        mLidControlsScreenLock = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_lidControlsScreenLock);
        mLidControlsSleep = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_lidControlsSleep);
        mTranslucentDecorEnabled = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_enableTranslucentDecor);

        mAllowTheaterModeWakeFromKey = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_allowTheaterModeWakeFromKey);
        mAllowTheaterModeWakeFromPowerKey = mAllowTheaterModeWakeFromKey
                || mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_allowTheaterModeWakeFromPowerKey);
        mAllowTheaterModeWakeFromMotion = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_allowTheaterModeWakeFromMotion);
        mAllowTheaterModeWakeFromMotionWhenNotDreaming = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_allowTheaterModeWakeFromMotionWhenNotDreaming);
        mAllowTheaterModeWakeFromCameraLens = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_allowTheaterModeWakeFromCameraLens);
        mAllowTheaterModeWakeFromLidSwitch = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_allowTheaterModeWakeFromLidSwitch);
        mAllowTheaterModeWakeFromWakeGesture = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_allowTheaterModeWakeFromGesture);

        mGoToSleepOnButtonPressTheaterMode = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_goToSleepOnButtonPressTheaterMode);

        mSupportLongPressPowerWhenNonInteractive = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_supportLongPressPowerWhenNonInteractive);

        mLongPressOnBackBehavior = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_longPressOnBackBehavior);

        mShortPressOnPowerBehavior = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_shortPressOnPowerBehavior);
        mLongPressOnPowerBehavior = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_longPressOnPowerBehavior);
        mDoublePressOnPowerBehavior = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_doublePressOnPowerBehavior);
        mTriplePressOnPowerBehavior = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_triplePressOnPowerBehavior);
        mShortPressOnSleepBehavior = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_shortPressOnSleepBehavior);

        mUseTvRouting = AudioSystem.getPlatformType(mContext) == AudioSystem.PLATFORM_TELEVISION;

        readConfigurationDependentBehaviors();

        mAccessibilityManager = (AccessibilityManager) context.getSystemService(
                Context.ACCESSIBILITY_SERVICE);

        // register for dock events
        IntentFilter filter = new IntentFilter();
        filter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_CAR_MODE);
        filter.addAction(UiModeManager.ACTION_ENTER_DESK_MODE);
        filter.addAction(UiModeManager.ACTION_EXIT_DESK_MODE);
        filter.addAction(Intent.ACTION_DOCK_EVENT);
        Intent intent = context.registerReceiver(mDockReceiver, filter);
        if (intent != null) {
            // Retrieve current sticky dock event broadcast.
            mDockMode = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,
                    Intent.EXTRA_DOCK_STATE_UNDOCKED);
        }
		/*PRIZE-three_slideshot-huangdianjun-2015-04-06-start*/
        IntentFilter slideScreenshotFilter = new IntentFilter();
        slideScreenshotFilter.addAction(ACTION);
        context.registerReceiver(mSlideScreenshotReceiver, slideScreenshotFilter);
        /*PRIZE-three_slideshot-huangdianjun-2015-04-06-end*/
        //prize tangzhengrong 20180526 Swipe-up Gesture Navigation bar start
        mSwipeUpGestureController = new SwipeUpGestureController();
        //prize tangzhengrong 20180526 Swipe-up Gesture Navigation bar end
        /// M: register for oma events @{
        IntentFilter ipoEventFilter = new IntentFilter();
        ipoEventFilter.addAction(IPO_ENABLE);
        ipoEventFilter.addAction(IPO_DISABLE);
        context.registerReceiver(mIpoEventReceiver, ipoEventFilter);
        /// @}

        ///M: register for power-off alarm shutDown @{
        IntentFilter poweroffAlarmFilter = new IntentFilter();
        poweroffAlarmFilter.addAction(NORMAL_SHUTDOWN_ACTION);
        poweroffAlarmFilter.addAction(NORMAL_BOOT_ACTION);
        context.registerReceiver(mPoweroffAlarmReceiver, poweroffAlarmFilter);
        /// @}

        /// M: [ALPS00062902]register for stk  events @{
        IntentFilter stkUserActivityFilter = new IntentFilter();
        stkUserActivityFilter.addAction(STK_USERACTIVITY_ENABLE);
        context.registerReceiver(mStkUserActivityEnReceiver, stkUserActivityFilter);
        /// @}

        // register for dream-related broadcasts
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DREAMING_STARTED);
        filter.addAction(Intent.ACTION_DREAMING_STOPPED);
        context.registerReceiver(mDreamReceiver, filter);

        // register for multiuser-relevant broadcasts
        filter = new IntentFilter(Intent.ACTION_USER_SWITCHED);
        context.registerReceiver(mMultiuserReceiver, filter);

	
        /*prize add zhaojian 8.0 2017725 start*/
		if(PrizeOption.PRIZE_HONGBAO_AUTO_HELPER) {
        filter = new IntentFilter("prize.set.keyguard.state");
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!intent.hasExtra("hide")) {
                    Slog.d(TAG, "set keyguard state: invalid format. ignore!");
                    return;
                }

                final boolean hide = intent.getBooleanExtra("hide", false);
                final boolean includeSecure = intent.getBooleanExtra("secure", false);
				final boolean includeSleep = intent.getBooleanExtra("sleep", false);
                Slog.d(TAG, "set keyguard state: hide=" + hide + ", includeSecure=" + includeSecure);
                if (mKeyguardDelegate != null) {
                    if (hide) {
                        if (includeSecure) {
                            mKeyguardDelegate.keyguardDone(false, true);
                        }else{
                            mKeyguardDelegate.dismiss(false);
                        }
                    } else if(includeSleep){
						turnOffScreen();
					}else {
                        mKeyguardDelegate.doKeyguardTimeout(null);
                    }
                }
            }}, filter);
			
	    }
         /*prize add zhaojian 8.0 2017725 end*/
        // monitor for system gestures
        mSystemGestures = new SystemGesturesPointerEventListener(context,
                new SystemGesturesPointerEventListener.Callbacks() {
                    @Override
                    public void onSwipeFromTop() {
						//add by prize-liup 20150910 factorytest force statusbar start
						String topApp = getTopApp();
						if (topApp.contains("com.prize.factorytest") || topApp.contains("com.prize.autotest")) {
                            Slog.d(TAG, "onSwipeFromTop() topApp = " + topApp + ", not allow swipe from top");
							return;
						}
						//add by prize-liup 20150910 factorytest force statusbar end
                        /// M: Disable gesture in immersive mode.
                        if (isGestureIsolated()) return;
                        if (mStatusBar != null) {
                            requestTransientBars(mStatusBar);
                        }
                    }
                    @Override
                    public void onSwipeFromBottom() {
                        /// M: Disable gesture in immersive mode.
                        if (isGestureIsolated()) return;
                        if (mNavigationBar != null && mNavigationBarPosition == NAV_BAR_BOTTOM) {
                            /* Nav bar related to mBack key feature. prize-linkh-20160804 */
                            if (SUPPORT_NAV_BAR_FOR_MBACK_DEVICE && !canShowNavBarFormBack()) {
                                if (DEBUG) {
                                    Slog.d(TAG, "onSwipeFromBottom() - mBack device. Disallow showing nav bar.");
                                }
                                return;
                            } //END...
                            
                            /* Dynamically hiding nav bar feature. prize-linkh-20150714 */
                            if(mSupportHidingNavBar) {
                                swipeOutNavigationBar("onSwipeFromBottom");                          
                            } else { // END...
                                requestTransientBars(mNavigationBar);
                            }
                        }
                    }
                    @Override
                    public void onSwipeFromRight() {
                        /// M: Disable gesture in immersive mode.
                        if (isGestureIsolated()) return;
                        if (mNavigationBar != null && mNavigationBarPosition == NAV_BAR_RIGHT) {
                            if (SUPPORT_NAV_BAR_FOR_MBACK_DEVICE && !canShowNavBarFormBack()) {
                                if (DEBUG) {
                                    Slog.d(TAG, "onSwipeFromRight() - mBack device. Disallow showing nav bar.");
                                }
                                return;
                            } //END...
                            
                            /* Dynamically hiding nav bar feature. prize-linkh-20150714 */
                            if(mSupportHidingNavBar) {
                                swipeOutNavigationBar("onSwipeFromRight");                          
                            } else { // END...
                                requestTransientBars(mNavigationBar);
                            }
                        }
                    }
                    @Override
                    public void onSwipeFromLeft() {
                        /// M: Disable gesture in immersive mode.
                        if (isGestureIsolated()) return;
                        if (mNavigationBar != null && mNavigationBarPosition == NAV_BAR_LEFT) {
                            /* Nav bar related to mBack key feature. prize-linkh-20160804 */
                            if (SUPPORT_NAV_BAR_FOR_MBACK_DEVICE && !canShowNavBarFormBack()) {
                                if (DEBUG) {
                                    Slog.d(TAG, "onSwipeFromLeft() - mBack device. Disallow showing nav bar.");
                                }
                                return;
                            } //END...
                            
                            /* Dynamically hiding nav bar feature. prize-linkh-20150714 */
                            if(mSupportHidingNavBar) {
                                swipeOutNavigationBar("onSwipeFromLeft");
                            } else { // END...
                                requestTransientBars(mNavigationBar);
                            }
                        }
                    }
                    @Override
                    public void onFling(int duration) {
                        if (mPowerManagerInternal != null) {
                            mPowerManagerInternal.powerHint(
                                    PowerManagerInternal.POWER_HINT_INTERACTION, duration);
                        }
                    }
                    @Override
                    public void onDebug() {
                        // no-op
                    }
                    @Override
                    public void onDown() {
                        mOrientationListener.onTouchStart();
                    }
                    @Override
                    public void onUpOrCancel() {
                        mOrientationListener.onTouchEnd();
                    }
                    @Override
                    public void onMouseHoverAtTop() {
                        /// M: Disable gesture in immersive mode. {@
                        if (isGestureIsolated()) {
                            return;
                        }
                        /// @}
                        mHandler.removeMessages(MSG_REQUEST_TRANSIENT_BARS);
                        Message msg = mHandler.obtainMessage(MSG_REQUEST_TRANSIENT_BARS);
                        msg.arg1 = MSG_REQUEST_TRANSIENT_BARS_ARG_STATUS;
                        mHandler.sendMessageDelayed(msg, 500);
                    }
                    @Override
                    public void onMouseHoverAtBottom() {
                        /// M: Disable gesture in immersive mode. {@
                        if (isGestureIsolated()) {
                            return;
                        }
                        /// @}
                        mHandler.removeMessages(MSG_REQUEST_TRANSIENT_BARS);
                        Message msg = mHandler.obtainMessage(MSG_REQUEST_TRANSIENT_BARS);
                        msg.arg1 = MSG_REQUEST_TRANSIENT_BARS_ARG_NAVIGATION;
                        mHandler.sendMessageDelayed(msg, 500);
                    }
                    @Override
                    public void onMouseLeaveFromEdge() {
                        mHandler.removeMessages(MSG_REQUEST_TRANSIENT_BARS);
                    }
                    /// M: Disable gesture in immersive mode. {@
                    private boolean isGestureIsolated() {
                        WindowState win = mFocusedWindow != null
                                ? mFocusedWindow : mTopFullscreenOpaqueWindowState;
                        if (win != null
                            && (win.getSystemUiVisibility()
                            & View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED) != 0) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    /// @}
                });
        mImmersiveModeConfirmation = new ImmersiveModeConfirmation(mContext);
        mWindowManagerFuncs.registerPointerEventListener(mSystemGestures);
        /* Dynamically hiding nav bar feature. prize-linkh-20150714 */
        mHasRegisteredPointerEventListener = true;
        //END...
        mVibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        mLongPressVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_longPressVibePattern);
        mVirtualKeyVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_virtualKeyVibePattern);
        mKeyboardTapVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_keyboardTapVibePattern);
        mClockTickVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_clockTickVibePattern);
        mCalendarDateVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_calendarDateVibePattern);
        mSafeModeDisabledVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_safeModeDisabledVibePattern);
        mSafeModeEnabledVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_safeModeEnabledVibePattern);
        mContextClickVibePattern = getLongIntArray(mContext.getResources(),
                com.android.internal.R.array.config_contextClickVibePattern);

        mScreenshotChordEnabled = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_enableScreenshotChord);

        mGlobalKeyManager = new GlobalKeyManager(mContext);

        // Controls rotation and the like.
        initializeHdmiState();
		//add by liup 20150519 for sleep gestures start
		if(PrizeOption.PRIZE_SLEEP_GESTURE){
			mContentResolver = mContext.getContentResolver();  
			queryUri = Uri.parse("content://com.prize.sleepgesture/sleepgesture"); 
		}
		//add by liup 20150519 for sleep gestures end

        // Match current screen state.
        if (!mPowerManager.isInteractive()) {
            startedGoingToSleep(WindowManagerPolicy.OFF_BECAUSE_OF_USER);
            finishedGoingToSleep(WindowManagerPolicy.OFF_BECAUSE_OF_USER);
        }

        mWindowManagerInternal.registerAppTransitionListener(
                mStatusBarController.getAppTransitionListener());

        /// M: add for fullscreen switch feature @{
        if ("1".equals(SystemProperties.get("ro.mtk_fullscreen_switch"))) {
            mSupportFullscreenSwitch = true;
        }
        /// @}
    }

    /**
     * Read values from config.xml that may be overridden depending on
     * the configuration of the device.
     * eg. Disable long press on home goes to recents on sw600dp.
     */
    private void readConfigurationDependentBehaviors() {
        final Resources res = mContext.getResources();

        mLongPressOnHomeBehavior = res.getInteger(
                com.android.internal.R.integer.config_longPressOnHomeBehavior);
        if (mLongPressOnHomeBehavior < LONG_PRESS_HOME_NOTHING ||
                mLongPressOnHomeBehavior > LAST_LONG_PRESS_HOME_BEHAVIOR) {
            mLongPressOnHomeBehavior = LONG_PRESS_HOME_NOTHING;
        }

        mDoubleTapOnHomeBehavior = res.getInteger(
                com.android.internal.R.integer.config_doubleTapOnHomeBehavior);
        if (mDoubleTapOnHomeBehavior < DOUBLE_TAP_HOME_NOTHING ||
                mDoubleTapOnHomeBehavior > DOUBLE_TAP_HOME_RECENT_SYSTEM_UI) {
            mDoubleTapOnHomeBehavior = LONG_PRESS_HOME_NOTHING;
        }

        mShortPressWindowBehavior = SHORT_PRESS_WINDOW_NOTHING;
        if (mContext.getPackageManager().hasSystemFeature(FEATURE_PICTURE_IN_PICTURE)) {
            mShortPressWindowBehavior = SHORT_PRESS_WINDOW_PICTURE_IN_PICTURE;
        }

        mNavBarOpacityMode = res.getInteger(
                com.android.internal.R.integer.config_navBarOpacityMode);
    }

    @Override
    public void setInitialDisplaySize(Display display, int width, int height, int density) {
        // This method might be called before the policy has been fully initialized
        // or for other displays we don't care about.
        if (mContext == null || display.getDisplayId() != Display.DEFAULT_DISPLAY) {
            return;
        }
        mDisplay = display;

        final Resources res = mContext.getResources();
        int shortSize, longSize;
        if (width > height) {
            shortSize = height;
            longSize = width;
            mLandscapeRotation = Surface.ROTATION_0;
            mSeascapeRotation = Surface.ROTATION_180;
            if (res.getBoolean(com.android.internal.R.bool.config_reverseDefaultRotation)) {
                mPortraitRotation = Surface.ROTATION_90;
                mUpsideDownRotation = Surface.ROTATION_270;
            } else {
                mPortraitRotation = Surface.ROTATION_270;
                mUpsideDownRotation = Surface.ROTATION_90;
            }
        } else {
            shortSize = width;
            longSize = height;
            mPortraitRotation = Surface.ROTATION_0;
            mUpsideDownRotation = Surface.ROTATION_180;
            if (res.getBoolean(com.android.internal.R.bool.config_reverseDefaultRotation)) {
                mLandscapeRotation = Surface.ROTATION_270;
                mSeascapeRotation = Surface.ROTATION_90;
            } else {
                mLandscapeRotation = Surface.ROTATION_90;
                mSeascapeRotation = Surface.ROTATION_270;
            }
        }

        // SystemUI (status bar) layout policy
        int shortSizeDp = shortSize * DisplayMetrics.DENSITY_DEFAULT / density;
        int longSizeDp = longSize * DisplayMetrics.DENSITY_DEFAULT / density;

        // Allow the navigation bar to move on non-square small devices (phones).
        mNavigationBarCanMove = width != height && shortSizeDp < 600;

        mHasNavigationBar = res.getBoolean(com.android.internal.R.bool.config_showNavigationBar);

        // Allow a system property to override this. Used by the emulator.
        // See also hasNavigationBar().
        String navBarOverride = SystemProperties.get("qemu.hw.mainkeys");
        if ("1".equals(navBarOverride)) {
            mHasNavigationBar = false;
        } else if ("0".equals(navBarOverride)) {
            mHasNavigationBar = true;
        }

        
        /* Dynamically hiding nav bar feature. prize-linkh-20150714 */
        if(!mHasNavigationBar) {
            if(mSupportHidingNavBar && !mHasRegisteredPointerEventListener) {
                mWindowManagerFuncs.registerPointerEventListener(mSystemGestures);
                mSystemGestures.reset();
                mHasRegisteredPointerEventListener = true;
            }
            //Note: Althoungh the value of variable mHasNavigationBar 
            // can be changed many times in runtime, we only give one chance to change the 
            // value of varibale mSupportHidingNavBar.
            mSupportHidingNavBar = false;
            mSystemGestures.mSupportHidingNavBar = false;
        } else if(mSupportHidingNavBar) {
            mSystemGestures.mSupportHidingNavBar = true;
            if(mHasRegisteredPointerEventListener) {
                //we deliver motion events through interceptMotionBeforeQueueingInteractive()
                // by invoking mSystemGestures.onPointerEvent().
                mWindowManagerFuncs.unregisterPointerEventListener(mSystemGestures);
                mSystemGestures.reset();
                mHasRegisteredPointerEventListener = false;
            }
        }
        //end.....
        
        // For demo purposes, allow the rotation of the HDMI display to be controlled.
        // By default, HDMI locks rotation to landscape.
        if ("portrait".equals(SystemProperties.get("persist.demo.hdmirotation"))) {
            mDemoHdmiRotation = mPortraitRotation;
        } else {
            mDemoHdmiRotation = mLandscapeRotation;
        }
        mDemoHdmiRotationLock = SystemProperties.getBoolean("persist.demo.hdmirotationlock", false);

        // For demo purposes, allow the rotation of the remote display to be controlled.
        // By default, remote display locks rotation to landscape.
        if ("portrait".equals(SystemProperties.get("persist.demo.remoterotation"))) {
            mDemoRotation = mPortraitRotation;
        } else {
            mDemoRotation = mLandscapeRotation;
        }
        mDemoRotationLock = SystemProperties.getBoolean(
                "persist.demo.rotationlock", false);

        // Only force the default orientation if the screen is xlarge, at least 960dp x 720dp, per
        // http://developer.android.com/guide/practices/screens_support.html#range
        mForceDefaultOrientation = longSizeDp >= 960 && shortSizeDp >= 720 &&
                res.getBoolean(com.android.internal.R.bool.config_forceDefaultOrientation) &&
                // For debug purposes the next line turns this feature off with:
                // $ adb shell setprop config.override_forced_orient true
                // $ adb shell wm size reset
                !"true".equals(SystemProperties.get("config.override_forced_orient"));
    }

    /**
     * @return whether the navigation bar can be hidden, e.g. the device has a
     *         navigation bar and touch exploration is not enabled
     */
    private boolean canHideNavigationBar() {
        return mHasNavigationBar;
    }

    @Override
    public boolean isDefaultOrientationForced() {
        return mForceDefaultOrientation;
    }

    @Override
    public void setDisplayOverscan(Display display, int left, int top, int right, int bottom) {
        if (display.getDisplayId() == Display.DEFAULT_DISPLAY) {
            mOverscanLeft = left;
            mOverscanTop = top;
            mOverscanRight = right;
            mOverscanBottom = bottom;
        }
    }

    public void updateSettings() {
	   /*PRIZE-wangyunhe for home open flash -20151023-start*/
        int isSystemFlashOn = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, -1);
		if (isSystemFlashOn == 3) {  
            Settings.System.putInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, 4);
		}
		/*PRIZE-wangyunhe for home open flash -20151023-start*/
        ContentResolver resolver = mContext.getContentResolver();
        boolean updateRotation = false;
        synchronized (mLock) {
            mEndcallBehavior = Settings.System.getIntForUser(resolver,
                    Settings.System.END_BUTTON_BEHAVIOR,
                    Settings.System.END_BUTTON_BEHAVIOR_DEFAULT,
                    UserHandle.USER_CURRENT);
            mIncallPowerBehavior = Settings.Secure.getIntForUser(resolver,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT,
                    UserHandle.USER_CURRENT);

            // Configure wake gesture.
            boolean wakeGestureEnabledSetting = Settings.Secure.getIntForUser(resolver,
                    Settings.Secure.WAKE_GESTURE_ENABLED, 0,
                    UserHandle.USER_CURRENT) != 0;
            if (mWakeGestureEnabledSetting != wakeGestureEnabledSetting) {
                mWakeGestureEnabledSetting = wakeGestureEnabledSetting;
                updateWakeGestureListenerLp();
            }

            // Configure rotation lock.
            int userRotation = Settings.System.getIntForUser(resolver,
                    Settings.System.USER_ROTATION, Surface.ROTATION_0,
                    UserHandle.USER_CURRENT);
            if (mUserRotation != userRotation) {
                mUserRotation = userRotation;
                updateRotation = true;
            }
            int userRotationMode = Settings.System.getIntForUser(resolver,
                    Settings.System.ACCELEROMETER_ROTATION, 0, UserHandle.USER_CURRENT) != 0 ?
                            WindowManagerPolicy.USER_ROTATION_FREE :
                                    WindowManagerPolicy.USER_ROTATION_LOCKED;
            if (mUserRotationMode != userRotationMode) {
                mUserRotationMode = userRotationMode;
                updateRotation = true;
                updateOrientationListenerLp();
            }

            if (mSystemReady) {
                int pointerLocation = Settings.System.getIntForUser(resolver,
                        Settings.System.POINTER_LOCATION, 0, UserHandle.USER_CURRENT);
                if (mPointerLocationMode != pointerLocation) {
                    mPointerLocationMode = pointerLocation;
                    mHandler.sendEmptyMessage(pointerLocation != 0 ?
                            MSG_ENABLE_POINTER_LOCATION : MSG_DISABLE_POINTER_LOCATION);
                }
            }
            // use screen off timeout setting as the timeout for the lockscreen
            mLockScreenTimeout = Settings.System.getIntForUser(resolver,
                    Settings.System.SCREEN_OFF_TIMEOUT, 0, UserHandle.USER_CURRENT);
            String imId = Settings.Secure.getStringForUser(resolver,
                    Settings.Secure.DEFAULT_INPUT_METHOD, UserHandle.USER_CURRENT);
            boolean hasSoftInput = imId != null && imId.length() > 0;
            if (mHasSoftInput != hasSoftInput) {
                mHasSoftInput = hasSoftInput;
                updateRotation = true;
            }
            if (mImmersiveModeConfirmation != null) {
                mImmersiveModeConfirmation.loadSetting(mCurrentUserId);
            }
        }
        synchronized (mWindowManagerFuncs.getWindowManagerLock()) {
            PolicyControl.reloadFromSetting(mContext);
        }
        if (updateRotation) {
            updateRotation(true);
        }
    }

    private void updateWakeGestureListenerLp() {
        if (shouldEnableWakeGestureLp()) {
            mWakeGestureListener.requestWakeUpTrigger();
        } else {
            mWakeGestureListener.cancelWakeUpTrigger();
        }
    }

    private boolean shouldEnableWakeGestureLp() {
        return mWakeGestureEnabledSetting && !mAwake
                && (!mLidControlsSleep || mLidState != LID_CLOSED)
                && mWakeGestureListener.isSupported()
                /// M: Disable WakeGesture in IPO ShutDown. {@
                && !mIsIpoShutDown;
                /// @}
    }

    private void enablePointerLocation() {
        if (mPointerLocationView == null) {
            mPointerLocationView = new PointerLocationView(mContext);
            mPointerLocationView.setPrintCoords(false);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            lp.type = WindowManager.LayoutParams.TYPE_SECURE_SYSTEM_OVERLAY;
            lp.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            if (ActivityManager.isHighEndGfx()) {
                lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
                lp.privateFlags |=
                        WindowManager.LayoutParams.PRIVATE_FLAG_FORCE_HARDWARE_ACCELERATED;
            }
            lp.format = PixelFormat.TRANSLUCENT;
            lp.setTitle("PointerLocation");
            WindowManager wm = (WindowManager)
                    mContext.getSystemService(Context.WINDOW_SERVICE);
            lp.inputFeatures |= WindowManager.LayoutParams.INPUT_FEATURE_NO_INPUT_CHANNEL;
            wm.addView(mPointerLocationView, lp);
            mWindowManagerFuncs.registerPointerEventListener(mPointerLocationView);
        }
    }

    private void disablePointerLocation() {
        if (mPointerLocationView != null) {
            mWindowManagerFuncs.unregisterPointerEventListener(mPointerLocationView);
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mPointerLocationView);
            mPointerLocationView = null;
        }
    }

    private int readRotation(int resID) {
        try {
            int rotation = mContext.getResources().getInteger(resID);
            switch (rotation) {
                case 0:
                    return Surface.ROTATION_0;
                case 90:
                    return Surface.ROTATION_90;
                case 180:
                    return Surface.ROTATION_180;
                case 270:
                    return Surface.ROTATION_270;
            }
        } catch (Resources.NotFoundException e) {
            // fall through
        }
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public int checkAddPermission(WindowManager.LayoutParams attrs, int[] outAppOp) {
        int type = attrs.type;

        outAppOp[0] = AppOpsManager.OP_NONE;

        if (!((type >= FIRST_APPLICATION_WINDOW && type <= LAST_APPLICATION_WINDOW)
                || (type >= FIRST_SUB_WINDOW && type <= LAST_SUB_WINDOW)
                || (type >= FIRST_SYSTEM_WINDOW && type <= LAST_SYSTEM_WINDOW))) {
            return WindowManagerGlobal.ADD_INVALID_TYPE;
        }

        if (type < FIRST_SYSTEM_WINDOW || type > LAST_SYSTEM_WINDOW) {
            // Window manager will make sure these are okay.
            return WindowManagerGlobal.ADD_OKAY;
        }
        String permission = null;
        switch (type) {
            case TYPE_TOAST:
                // XXX right now the app process has complete control over
                // this...  should introduce a token to let the system
                // monitor/control what they are doing.
                outAppOp[0] = AppOpsManager.OP_TOAST_WINDOW;
                break;
            case TYPE_DREAM:
            case TYPE_INPUT_METHOD:
            case TYPE_WALLPAPER:
            case TYPE_PRIVATE_PRESENTATION:
            case TYPE_VOICE_INTERACTION:
            case TYPE_ACCESSIBILITY_OVERLAY:
            case TYPE_QS_DIALOG:
                /// M: Support IPO window.
            case TYPE_TOP_MOST:
                // The window manager will check these.
                break;
            case TYPE_PHONE:
            case TYPE_PRIORITY_PHONE:
            case TYPE_SYSTEM_ALERT:
            case TYPE_SYSTEM_ERROR:
            case TYPE_SYSTEM_OVERLAY:
                permission = android.Manifest.permission.SYSTEM_ALERT_WINDOW;
                outAppOp[0] = AppOpsManager.OP_SYSTEM_ALERT_WINDOW;
                break;
            default:
                permission = android.Manifest.permission.INTERNAL_SYSTEM_WINDOW;
        }
        if (permission != null) {
            if (android.Manifest.permission.SYSTEM_ALERT_WINDOW.equals(permission)) {
                final int callingUid = Binder.getCallingUid();
                // system processes will be automatically allowed privilege to draw
                if (callingUid == Process.SYSTEM_UID) {
                    return WindowManagerGlobal.ADD_OKAY;
                }

                // check if user has enabled this operation. SecurityException will be thrown if
                // this app has not been allowed by the user
                final int mode = mAppOpsManager.checkOpNoThrow(outAppOp[0], callingUid,
                        attrs.packageName);
                switch (mode) {
                    case AppOpsManager.MODE_ALLOWED:
                    case AppOpsManager.MODE_IGNORED:
                        // although we return ADD_OKAY for MODE_IGNORED, the added window will
                        // actually be hidden in WindowManagerService
                        return WindowManagerGlobal.ADD_OKAY;
                    case AppOpsManager.MODE_ERRORED:
                        try {
                            ApplicationInfo appInfo = mContext.getPackageManager()
                                    .getApplicationInfo(attrs.packageName,
                                            UserHandle.getUserId(callingUid));
                            // Don't crash legacy apps
                            if (appInfo.targetSdkVersion < Build.VERSION_CODES.M) {
                                return WindowManagerGlobal.ADD_OKAY;
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            /* ignore */
                        }
                        return WindowManagerGlobal.ADD_PERMISSION_DENIED;
                    default:
                        // in the default mode, we will make a decision here based on
                        // checkCallingPermission()
                        if (mContext.checkCallingPermission(permission) !=
                                PackageManager.PERMISSION_GRANTED) {
                            return WindowManagerGlobal.ADD_PERMISSION_DENIED;
                        } else {
                            return WindowManagerGlobal.ADD_OKAY;
                        }
                }
            }

            if (mContext.checkCallingOrSelfPermission(permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return WindowManagerGlobal.ADD_PERMISSION_DENIED;
            }
        }
        return WindowManagerGlobal.ADD_OKAY;
    }


    @Override
    public boolean checkShowToOwnerOnly(WindowManager.LayoutParams attrs) {

        // If this switch statement is modified, modify the comment in the declarations of
        // the type in {@link WindowManager.LayoutParams} as well.
        switch (attrs.type) {
            default:
                // These are the windows that by default are shown only to the user that created
                // them. If this needs to be overridden, set
                // {@link WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS} in
                // {@link WindowManager.LayoutParams}. Note that permission
                // {@link android.Manifest.permission.INTERNAL_SYSTEM_WINDOW} is required as well.
                if ((attrs.privateFlags & PRIVATE_FLAG_SHOW_FOR_ALL_USERS) == 0) {
                    return true;
                }
                break;

            // These are the windows that by default are shown to all users. However, to
            // protect against spoofing, check permissions below.
            case TYPE_APPLICATION_STARTING:
            case TYPE_BOOT_PROGRESS:
            case TYPE_DISPLAY_OVERLAY:
            case TYPE_INPUT_CONSUMER:
            case TYPE_KEYGUARD_SCRIM:
            case TYPE_KEYGUARD_DIALOG:
            case TYPE_MAGNIFICATION_OVERLAY:
            case TYPE_NAVIGATION_BAR:
            case TYPE_NAVIGATION_BAR_PANEL:
            case TYPE_PHONE:
            case TYPE_POINTER:
            case TYPE_PRIORITY_PHONE:
            case TYPE_SEARCH_BAR:
            case TYPE_STATUS_BAR:
            case TYPE_STATUS_BAR_PANEL:
            case TYPE_STATUS_BAR_SUB_PANEL:
            case TYPE_SYSTEM_DIALOG:
            case TYPE_VOLUME_OVERLAY:
            case TYPE_PRIVATE_PRESENTATION:
            case TYPE_DOCK_DIVIDER:
                break;
        }

        // Check if third party app has set window to system window type.
        return mContext.checkCallingOrSelfPermission(
                android.Manifest.permission.INTERNAL_SYSTEM_WINDOW)
                        != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void adjustWindowParamsLw(WindowManager.LayoutParams attrs) {
        switch (attrs.type) {
            case TYPE_SYSTEM_OVERLAY:
            case TYPE_SECURE_SYSTEM_OVERLAY:
                // These types of windows can't receive input events.
                attrs.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                attrs.flags &= ~WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                break;
            case TYPE_STATUS_BAR:

                // If the Keyguard is in a hidden state (occluded by another window), we force to
                // remove the wallpaper and keyguard flag so that any change in-flight after setting
                // the keyguard as occluded wouldn't set these flags again.
                // See {@link #processKeyguardSetHiddenResultLw}.
                if (mKeyguardHidden) {
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
                    attrs.privateFlags &= ~WindowManager.LayoutParams.PRIVATE_FLAG_KEYGUARD;
                }
                break;

            case TYPE_SCREENSHOT:
                attrs.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                break;

            case TYPE_TOAST:
                // While apps should use the dedicated toast APIs to add such windows
                // it possible legacy apps to add the window directly. Therefore, we
                // make windows added directly by the app behave as a toast as much
                // as possible in terms of timeout and animation.
                if (attrs.hideTimeoutMilliseconds < 0
                        || attrs.hideTimeoutMilliseconds > TOAST_WINDOW_TIMEOUT) {
                    attrs.hideTimeoutMilliseconds = TOAST_WINDOW_TIMEOUT;
                }
                attrs.windowAnimations = com.android.internal.R.style.Animation_Toast;
                break;
        }

        if (attrs.type != TYPE_STATUS_BAR) {
            // The status bar is the only window allowed to exhibit keyguard behavior.
            attrs.privateFlags &= ~WindowManager.LayoutParams.PRIVATE_FLAG_KEYGUARD;
        }

        if (ActivityManager.isHighEndGfx()) {
            if ((attrs.flags & FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) != 0) {
                attrs.subtreeSystemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }
            final boolean forceWindowDrawsStatusBarBackground =
                    (attrs.privateFlags & PRIVATE_FLAG_FORCE_DRAW_STATUS_BAR_BACKGROUND)
                            != 0;
            if ((attrs.flags & FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) != 0
                    || forceWindowDrawsStatusBarBackground
                            && attrs.height == MATCH_PARENT && attrs.width == MATCH_PARENT) {
                attrs.subtreeSystemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }
        }

        // Nav bar color customized feature. prize-linkh-2017.08.07 @{
        /* solve bug-45605.  Don't need these code.
        if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            int sysUiVis = attrs.systemUiVisibility | attrs.subtreeSystemUiVisibility;
            if ((sysUiVis & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0) {
                // Some apps(eg, tencent news/Sina weibo) use light status bar mode and their backgrounds
                // look white.
                // Status Bar Inverse will remove this mode, so SystemUI draws opaque background itself and
                // user can't see nav bar color on this window. En..en.., we force to remove this mode prior 
                // here. If we remove it, status bar backgroud will be trasparent, user can't see status bar
                // icons clearly(My god!), hu..hu.., ok we add translucent flag to status bar.
                if (DBG_NAV_BAR_COLOR_CUST) {
                    Slog.d(TAG, "adjustWindowParamsLw() old attrs=" + attrs);
                }

                attrs.systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                attrs.subtreeSystemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                // It can't take effect. Because this window will set FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag
                // and this will makes SystemUI becomes to be transparent.
                //attrs.subtreeSystemUiVisibility |= View.STATUS_BAR_TRANSLUCENT;
                attrs.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

                if (DBG_NAV_BAR_COLOR_CUST) {
                    Slog.d(TAG, "adjustWindowParamsLw() remove light status bar mode! attrs=" + attrs);
                }
            }
        } */
        // @}        
    }

    void readLidState() {
        mLidState = mWindowManagerFuncs.getLidState();
    }

    private void readCameraLensCoverState() {
        mCameraLensCoverState = mWindowManagerFuncs.getCameraLensCoverState();
    }

    private boolean isHidden(int accessibilityMode) {
        switch (accessibilityMode) {
            case 1:
                return mLidState == LID_CLOSED;
            case 2:
                return mLidState == LID_OPEN;
            default:
                return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void adjustConfigurationLw(Configuration config, int keyboardPresence,
            int navigationPresence) {
        mHaveBuiltInKeyboard = (keyboardPresence & PRESENCE_INTERNAL) != 0;

        readConfigurationDependentBehaviors();
        readLidState();

        if (config.keyboard == Configuration.KEYBOARD_NOKEYS
                || (keyboardPresence == PRESENCE_INTERNAL
                        && isHidden(mLidKeyboardAccessibility))) {
            config.hardKeyboardHidden = Configuration.HARDKEYBOARDHIDDEN_YES;
            if (!mHasSoftInput) {
                config.keyboardHidden = Configuration.KEYBOARDHIDDEN_YES;
            }
        }

        if (config.navigation == Configuration.NAVIGATION_NONAV
                || (navigationPresence == PRESENCE_INTERNAL
                        && isHidden(mLidNavigationAccessibility))) {
            config.navigationHidden = Configuration.NAVIGATIONHIDDEN_YES;
        }
    }

    @Override
    public void onConfigurationChanged() {
        final Resources res = mContext.getResources();

        mStatusBarHeight =
                res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);

        // Height of the navigation bar when presented horizontally at bottom
        mNavigationBarHeightForRotationDefault[mPortraitRotation] =
        mNavigationBarHeightForRotationDefault[mUpsideDownRotation] =
                res.getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
        mNavigationBarHeightForRotationDefault[mLandscapeRotation] =
        mNavigationBarHeightForRotationDefault[mSeascapeRotation] = res.getDimensionPixelSize(
                com.android.internal.R.dimen.navigation_bar_height_landscape);

        // Width of the navigation bar when presented vertically along one side
        mNavigationBarWidthForRotationDefault[mPortraitRotation] =
        mNavigationBarWidthForRotationDefault[mUpsideDownRotation] =
        mNavigationBarWidthForRotationDefault[mLandscapeRotation] =
        mNavigationBarWidthForRotationDefault[mSeascapeRotation] =
                res.getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_width);

        if (ALTERNATE_CAR_MODE_NAV_SIZE) {
            // Height of the navigation bar when presented horizontally at bottom
            mNavigationBarHeightForRotationInCarMode[mPortraitRotation] =
            mNavigationBarHeightForRotationInCarMode[mUpsideDownRotation] =
                    res.getDimensionPixelSize(
                            com.android.internal.R.dimen.navigation_bar_height_car_mode);
            mNavigationBarHeightForRotationInCarMode[mLandscapeRotation] =
            mNavigationBarHeightForRotationInCarMode[mSeascapeRotation] = res.getDimensionPixelSize(
                    com.android.internal.R.dimen.navigation_bar_height_landscape_car_mode);

            // Width of the navigation bar when presented vertically along one side
            mNavigationBarWidthForRotationInCarMode[mPortraitRotation] =
            mNavigationBarWidthForRotationInCarMode[mUpsideDownRotation] =
            mNavigationBarWidthForRotationInCarMode[mLandscapeRotation] =
            mNavigationBarWidthForRotationInCarMode[mSeascapeRotation] =
                    res.getDimensionPixelSize(
                            com.android.internal.R.dimen.navigation_bar_width_car_mode);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int windowTypeToLayerLw(int type) {
        if (type >= FIRST_APPLICATION_WINDOW && type <= LAST_APPLICATION_WINDOW) {
            return 2;
        }
        switch (type) {
        case TYPE_PRIVATE_PRESENTATION:
            return 2;
        case TYPE_WALLPAPER:
            // wallpaper is at the bottom, though the window manager may move it.
            return 2;
        case TYPE_DOCK_DIVIDER:
            return 2;
        case TYPE_QS_DIALOG:
            return 2;
        case TYPE_PHONE:
            return 3;
        case TYPE_SEARCH_BAR:
        case TYPE_VOICE_INTERACTION_STARTING:
            return 4;
        case TYPE_VOICE_INTERACTION:
            // voice interaction layer is almost immediately above apps.
            return 5;
        case TYPE_INPUT_CONSUMER:
            return 6;
        case TYPE_SYSTEM_DIALOG:
            return 7;
        case TYPE_TOAST:
            // toasts and the plugged-in battery thing
            return 8;
        case TYPE_PRIORITY_PHONE:
            // SIM errors and unlock.  Not sure if this really should be in a high layer.
            return 9;
        case TYPE_DREAM:
            // used for Dreams (screensavers with TYPE_DREAM windows)
            return 10;
        case TYPE_SYSTEM_ALERT:
            // like the ANR / app crashed dialogs
            return 11;
        case TYPE_INPUT_METHOD:
            // on-screen keyboards and other such input method user interfaces go here.
            return 12;
        case TYPE_INPUT_METHOD_DIALOG:
            // on-screen keyboards and other such input method user interfaces go here.
            return 13;
        case TYPE_KEYGUARD_SCRIM:
            // the safety window that shows behind keyguard while keyguard is starting
            return 14;
        case TYPE_STATUS_BAR_SUB_PANEL:
            return 15;
        case TYPE_STATUS_BAR:
            return 16;
        case TYPE_STATUS_BAR_PANEL:
            return 17;
        case TYPE_KEYGUARD_DIALOG:
            return 18;
        case TYPE_VOLUME_OVERLAY:
            // the on-screen volume indicator and controller shown when the user
            // changes the device volume
            return 19;
        case TYPE_SYSTEM_OVERLAY:
            // the on-screen volume indicator and controller shown when the user
            // changes the device volume
            return 20;
        case TYPE_NAVIGATION_BAR:
            // the navigation bar, if available, shows atop most things
            return 21;
        case TYPE_NAVIGATION_BAR_PANEL:
            // some panels (e.g. search) need to show on top of the navigation bar
            return 22;
        case TYPE_SCREENSHOT:
            // screenshot selection layer shouldn't go above system error, but it should cover
            // navigation bars at the very least.
            return 23;
        case TYPE_SYSTEM_ERROR:
            // system-level error dialogs
            return 24;
        case TYPE_MAGNIFICATION_OVERLAY:
            // used to highlight the magnified portion of a display
            return 25;
        case TYPE_DISPLAY_OVERLAY:
            // used to simulate secondary display devices
            return 26;
        case TYPE_DRAG:
            // the drag layer: input for drag-and-drop is associated with this window,
            // which sits above all other focusable windows
            return 27;
        case TYPE_ACCESSIBILITY_OVERLAY:
            // overlay put by accessibility services to intercept user interaction
            return 28;
        case TYPE_SECURE_SYSTEM_OVERLAY:
            return 29;
        case TYPE_BOOT_PROGRESS:
            return 30;
        case TYPE_POINTER:
            // the (mouse) pointer layer
            return 31;
        /// M: Support IPO window.
        case TYPE_TOP_MOST:
            return 32;
        }
        Log.e(TAG, "Unknown window type: " + type);
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public int subWindowTypeToLayerLw(int type) {
        switch (type) {
        case TYPE_APPLICATION_PANEL:
        case TYPE_APPLICATION_ATTACHED_DIALOG:
            return APPLICATION_PANEL_SUBLAYER;
        case TYPE_APPLICATION_MEDIA:
            return APPLICATION_MEDIA_SUBLAYER;
        case TYPE_APPLICATION_MEDIA_OVERLAY:
            return APPLICATION_MEDIA_OVERLAY_SUBLAYER;
        case TYPE_APPLICATION_SUB_PANEL:
            return APPLICATION_SUB_PANEL_SUBLAYER;
        case TYPE_APPLICATION_ABOVE_SUB_PANEL:
            return APPLICATION_ABOVE_SUB_PANEL_SUBLAYER;
        }
        Log.e(TAG, "Unknown sub-window type: " + type);
        return 0;
    }

    @Override
    public int getMaxWallpaperLayer() {
        return windowTypeToLayerLw(TYPE_STATUS_BAR);
    }

    private int getNavigationBarWidth(int rotation, int uiMode) {
        if (ALTERNATE_CAR_MODE_NAV_SIZE && (uiMode & UI_MODE_TYPE_MASK) == UI_MODE_TYPE_CAR) {
            return mNavigationBarWidthForRotationInCarMode[rotation];
        } else {
            return mNavigationBarWidthForRotationDefault[rotation];
        }
    }

    @Override
    public int getNonDecorDisplayWidth(int fullWidth, int fullHeight, int rotation,
            int uiMode) {

        /* prize-add-landscape split screen cut lcd ears, pop soft keyboard display rect-liyongli-20180417-start 
          *   place the cut lcd ears same as navigation bar to the side,
          *   so need  w -= mStatusBarHeight(lcd ears hight)
          */
        if( PrizeOption.PRIZE_NOTCH_SCREEN
              && (rotation==Surface.ROTATION_90 ||rotation== Surface.ROTATION_270)
        ){
            fullWidth -= mStatusBarHeight;
            Slog.d(TAG, "notch lcd 90  270 cut screen Width to ="+fullWidth); 
        }
        /* prize-add-landscape split screen cut lcd ears-liyongli-20180417-end */
                
        if (mHasNavigationBar) {
            // For a basic navigation bar, when we are in landscape mode we place
            // the navigation bar to the side.
            if (mNavigationBarCanMove && fullWidth > fullHeight) {
                return fullWidth - getNavigationBarWidth(rotation, uiMode);
            }
        }
        return fullWidth;
    }

    private int getNavigationBarHeight(int rotation, int uiMode) {
        if (ALTERNATE_CAR_MODE_NAV_SIZE && (uiMode & UI_MODE_TYPE_MASK) == UI_MODE_TYPE_CAR) {
            return mNavigationBarHeightForRotationInCarMode[rotation];
        } else {
            return mNavigationBarHeightForRotationDefault[rotation];
        }
    }

    @Override
    public int getNonDecorDisplayHeight(int fullWidth, int fullHeight, int rotation,
            int uiMode) {
        if (mHasNavigationBar) {
            // For a basic navigation bar, when we are in portrait mode we place
            // the navigation bar to the bottom.
            if (!mNavigationBarCanMove || fullWidth < fullHeight) {
                /* prize-add-Dynamically hiding nav bar, fix DouYin video not show FullScreen -liyongli-20180703-start 
                *   hide NavBar, turn 90 land, pop softKeyboard, softKeyboard not show fullscreen
                */
                if(mSupportHidingNavBar && mNavigationBar!=null &&  mNavigationBar.isGoneForLayoutLw() ){
                    return fullHeight;
                }/* prize-add-Dynamically hiding nav bar, fix DouYin video not show FullScreen -liyongli-20180703-end */
                return fullHeight - getNavigationBarHeight(rotation, uiMode);
            }
        }
        return fullHeight;
    }

    @Override
    public int getConfigDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode) {
        return getNonDecorDisplayWidth(fullWidth, fullHeight, rotation, uiMode);
    }

    @Override
    public int getConfigDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode) {
        // There is a separate status bar at the top of the display.  We don't count that as part
        // of the fixed decor, since it can hide; however, for purposes of configurations,
        // we do want to exclude it since applications can't generally use that part
        // of the screen.
        return getNonDecorDisplayHeight(
                fullWidth, fullHeight, rotation, uiMode) - mStatusBarHeight;
    }

    @Override
    public boolean isForceHiding(WindowManager.LayoutParams attrs) {
        return (attrs.privateFlags & PRIVATE_FLAG_KEYGUARD) != 0 ||
        /// M: [ALPS01939364][ALPS01948669] Fix app window is hidden even when Keyguard is occluded
            (isKeyguardHostWindow(attrs) && isKeyguardShowingAndNotOccluded()) ||
            (attrs.type == TYPE_KEYGUARD_SCRIM);
    }

    @Override
    public boolean isKeyguardHostWindow(WindowManager.LayoutParams attrs) {
        return attrs.type == TYPE_STATUS_BAR;
    }

    @Override
    public boolean canBeForceHidden(WindowState win, WindowManager.LayoutParams attrs) {
        switch (attrs.type) {
            case TYPE_STATUS_BAR:
            case TYPE_NAVIGATION_BAR:
            case TYPE_WALLPAPER:
            case TYPE_DREAM:
            case TYPE_KEYGUARD_SCRIM:
                return false;
            default:
                // Hide only windows below the keyguard host window.
                return windowTypeToLayerLw(win.getBaseType())
                        < windowTypeToLayerLw(TYPE_STATUS_BAR);
        }
    }

    @Override
    public WindowState getWinShowWhenLockedLw() {
        return mWinShowWhenLocked;
    }

    /** {@inheritDoc} */
    @Override
    public View addStartingWindow(IBinder appToken, String packageName, int theme,
            CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes,
            int icon, int logo, int windowFlags, Configuration overrideConfig) {
        if (!SHOW_STARTING_ANIMATIONS) {
            return null;
        }
        if (packageName == null) {
            return null;
        }

        WindowManager wm = null;
        View view = null;

        try {
            Context context = mContext;
            if (DEBUG_STARTING_WINDOW) Slog.d(TAG, "addStartingWindow " + packageName
                    + ": nonLocalizedLabel=" + nonLocalizedLabel + " theme="
                    + Integer.toHexString(theme));
            if (theme != context.getThemeResId() || labelRes != 0) {
                try {
                    context = context.createPackageContext(packageName, 0);
                    context.setTheme(theme);
                } catch (PackageManager.NameNotFoundException e) {
                    // Ignore
                }
            }

            if (overrideConfig != null && overrideConfig != EMPTY) {
                if (DEBUG_STARTING_WINDOW) Slog.d(TAG, "addStartingWindow: creating context based"
                        + " on overrideConfig" + overrideConfig + " for starting window");
                final Context overrideContext = context.createConfigurationContext(overrideConfig);
                overrideContext.setTheme(theme);
                final TypedArray typedArray = overrideContext.obtainStyledAttributes(
                        com.android.internal.R.styleable.Window);
                final int resId = typedArray.getResourceId(R.styleable.Window_windowBackground, 0);
                if (resId != 0 && overrideContext.getDrawable(resId) != null) {
                    // We want to use the windowBackground for the override context if it is
                    // available, otherwise we use the default one to make sure a themed starting
                    // window is displayed for the app.
                    if (DEBUG_STARTING_WINDOW) Slog.d(TAG, "addStartingWindow: apply overrideConfig"
                            + overrideConfig + " to starting window resId=" + resId);
                    context = overrideContext;
                }
            }

            final PhoneWindow win = new PhoneWindow(context);
            win.setIsStartingWindow(true);

            CharSequence label = context.getResources().getText(labelRes, null);
            // Only change the accessibility title if the label is localized
            if (label != null) {
                win.setTitle(label, true);
            } else {
                win.setTitle(nonLocalizedLabel, false);
            }

            win.setType(
                WindowManager.LayoutParams.TYPE_APPLICATION_STARTING);

            synchronized (mWindowManagerFuncs.getWindowManagerLock()) {
                // Assumes it's safe to show starting windows of launched apps while
                // the keyguard is being hidden. This is okay because starting windows never show
                // secret information.
                if (mKeyguardHidden) {
                    windowFlags |= FLAG_SHOW_WHEN_LOCKED;
                }
            }

            // Force the window flags: this is a fake window, so it is not really
            // touchable or focusable by the user.  We also add in the ALT_FOCUSABLE_IM
            // flag because we do know that the next window will take input
            // focus, so we want to get the IME window up on top of us right away.
            win.setFlags(
                windowFlags|
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                windowFlags|
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            win.setDefaultIcon(icon);
            win.setDefaultLogo(logo);

            win.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);

            final WindowManager.LayoutParams params = win.getAttributes();
            params.token = appToken;
            params.packageName = packageName;
            params.windowAnimations = win.getWindowStyle().getResourceId(
                    com.android.internal.R.styleable.Window_windowAnimationStyle, 0);
            params.privateFlags |=
                    WindowManager.LayoutParams.PRIVATE_FLAG_FAKE_HARDWARE_ACCELERATED;
            params.privateFlags |= WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;

            if (!compatInfo.supportsScreen()) {
                params.privateFlags |= WindowManager.LayoutParams.PRIVATE_FLAG_COMPATIBLE_WINDOW;
            }

            params.setTitle("Starting " + packageName);

            wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            view = win.getDecorView();

            if (DEBUG_STARTING_WINDOW) Slog.d(TAG, "Adding starting window for "
                + packageName + " / " + appToken + ": " + (view.getParent() != null ? view : null));

            wm.addView(view, params);

            /// M: [App Launch Reponse Time Enhancement] Merge Traversal. {@
            if (mAppLaunchTimeEnabled) {
                WindowManagerGlobal.getInstance().doTraversal(view, true);
            }
            /// @}
            // Only return the view if it was successfully added to the
            // window manager... which we can tell by it having a parent.
            return view.getParent() != null ? view : null;
        } catch (WindowManager.BadTokenException e) {
            // ignore
            Log.w(TAG, appToken + " already running, starting window not displayed. " +
                    e.getMessage());
        } catch (RuntimeException e) {
            // don't crash if something else bad happens, for example a
            // failure loading resources because we are loading from an app
            // on external storage that has been unmounted.
            Log.w(TAG, appToken + " failed creating starting window", e);
        } finally {
            if (view != null && view.getParent() == null) {
                Log.w(TAG, "view not successfully added to wm, removing view");
                wm.removeViewImmediate(view);
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void removeStartingWindow(IBinder appToken, View window) {
        if (DEBUG_STARTING_WINDOW) Slog.v(TAG, "Removing starting window for " + appToken + ": "
                + window + " Callers=" + Debug.getCallers(4));

        if (window != null) {
            WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(window);
        }
    }

    /**
     * Preflight adding a window to the system.
     *
     * Currently enforces that three window types are singletons:
     * <ul>
     * <li>STATUS_BAR_TYPE</li>
     * <li>KEYGUARD_TYPE</li>
     * </ul>
     *
     * @param win The window to be added
     * @param attrs Information about the window to be added
     *
     * @return If ok, WindowManagerImpl.ADD_OKAY.  If too many singletons,
     * WindowManagerImpl.ADD_MULTIPLE_SINGLETON
     */
    @Override
    public int prepareAddWindowLw(WindowState win, WindowManager.LayoutParams attrs) {
        switch (attrs.type) {
            case TYPE_STATUS_BAR:
                mContext.enforceCallingOrSelfPermission(
                        android.Manifest.permission.STATUS_BAR_SERVICE,
                        "PhoneWindowManager");
                if (mStatusBar != null) {
                    if (mStatusBar.isAlive()) {
                        return WindowManagerGlobal.ADD_MULTIPLE_SINGLETON;
                    }
                }
                mStatusBar = win;
                mStatusBarController.setWindow(win);
                break;
            case TYPE_NAVIGATION_BAR:
                mContext.enforceCallingOrSelfPermission(
                        android.Manifest.permission.STATUS_BAR_SERVICE,
                        "PhoneWindowManager");
                if (mNavigationBar != null) {
                    if (mNavigationBar.isAlive()) {
                        return WindowManagerGlobal.ADD_MULTIPLE_SINGLETON;
                    }
                }
                mNavigationBar = win;
                mNavigationBarController.setWindow(win);
                if (DEBUG_LAYOUT) Slog.i(TAG, "NAVIGATION BAR: " + mNavigationBar);
                break;
            case TYPE_NAVIGATION_BAR_PANEL:
            case TYPE_STATUS_BAR_PANEL:
            case TYPE_STATUS_BAR_SUB_PANEL:
            case TYPE_VOICE_INTERACTION_STARTING:
                mContext.enforceCallingOrSelfPermission(
                        android.Manifest.permission.STATUS_BAR_SERVICE,
                        "PhoneWindowManager");
                break;
            case TYPE_KEYGUARD_SCRIM:
                if (mKeyguardScrim != null) {
                    return WindowManagerGlobal.ADD_MULTIPLE_SINGLETON;
                }
                mKeyguardScrim = win;
                break;
            // Nav bar color customized feature. prize-linkh-2017.08.02 @{
            case TYPE_INPUT_METHOD:
                if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
                    mInputMethodWindow = win;
                }
                break;
            // @}
            
            /* prize-add- 0 180 rotation, cut lcd ears then cover with black view-liyongli-20180628-begin */
            case TYPE_SEARCH_BAR://TYPE_TOP_MOST:
                if (PrizeOption.PRIZE_NOTCH_SCREEN) {
                    String strTitle = win.getAttrs().getTitle()+"";
                    if("CoverEars".equals(strTitle)){ //ActivityManagerService.java  getNotchScreenLayoutParams(x,y, "CoverEars");
                        prizeCoverEarsWin = win;
                        coverEarsWinAttrs = prizeCoverEarsWin.getAttrs();
                    }
                }
                break;
            /* prize-add- 0 180 rotation, cut lcd ears then cover with black view-liyongli-20180628-end */
                        
        }
        return WindowManagerGlobal.ADD_OKAY;
    }

    /** {@inheritDoc} */
    @Override
    public void removeWindowLw(WindowState win) {
        if (mStatusBar == win) {
            mStatusBar = null;
            mStatusBarController.setWindow(null);
            mKeyguardDelegate.showScrim();
        } else if (mKeyguardScrim == win) {
            Log.v(TAG, "Removing keyguard scrim");
            mKeyguardScrim = null;
        } if (mNavigationBar == win) {
            mNavigationBar = null;
            mNavigationBarController.setWindow(null);
        }
        // Nav bar color customized feature. prize-linkh-2017.08.02 @{
        if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            if (mInputMethodWindow == win) {
                mInputMethodWindow = null;
            }
        }
        // @}
    }

    static final boolean PRINT_ANIM = false;

    /** {@inheritDoc} */
    @Override
    public int selectAnimationLw(WindowState win, int transit) {
        if (PRINT_ANIM) Log.i(TAG, "selectAnimation in " + win
              + ": transit=" + transit);
        if (win == mStatusBar) {
            boolean isKeyguard = (win.getAttrs().privateFlags & PRIVATE_FLAG_KEYGUARD) != 0;
            if (transit == TRANSIT_EXIT
                    || transit == TRANSIT_HIDE) {
                return isKeyguard ? -1 : R.anim.dock_top_exit;
            } else if (transit == TRANSIT_ENTER
                    || transit == TRANSIT_SHOW) {
                return isKeyguard ? -1 : R.anim.dock_top_enter;
            }
        } else if (win == mNavigationBar) {
            if (win.getAttrs().windowAnimations != 0) {
                return 0;
            }
            // This can be on either the bottom or the right or the left.
            if (mNavigationBarPosition == NAV_BAR_BOTTOM) {
                if (transit == TRANSIT_EXIT
                        || transit == TRANSIT_HIDE) {
                    if (isKeyguardShowingAndNotOccluded()) {
                        return R.anim.dock_bottom_exit_keyguard;
                    } else {
                        return R.anim.dock_bottom_exit;
                    }
                } else if (transit == TRANSIT_ENTER
                        || transit == TRANSIT_SHOW) {
                    return R.anim.dock_bottom_enter;
                }
            } else if (mNavigationBarPosition == NAV_BAR_RIGHT) {
                if (transit == TRANSIT_EXIT
                        || transit == TRANSIT_HIDE) {
                    return R.anim.dock_right_exit;
                } else if (transit == TRANSIT_ENTER
                        || transit == TRANSIT_SHOW) {
                    return R.anim.dock_right_enter;
                }
            } else if (mNavigationBarPosition == NAV_BAR_LEFT) {
                if (transit == TRANSIT_EXIT
                        || transit == TRANSIT_HIDE) {
                    return R.anim.dock_left_exit;
                } else if (transit == TRANSIT_ENTER
                        || transit == TRANSIT_SHOW) {
                    return R.anim.dock_left_enter;
                }
            }
        } else if (win.getAttrs().type == TYPE_DOCK_DIVIDER) {
            return selectDockedDividerAnimationLw(win, transit);
        }

        if (transit == TRANSIT_PREVIEW_DONE) {
            if (win.hasAppShownWindows()) {
                if (PRINT_ANIM) Log.i(TAG, "**** STARTING EXIT");
                return com.android.internal.R.anim.app_starting_exit;
            }
        } else if (win.getAttrs().type == TYPE_DREAM && mDreamingLockscreen
                && transit == TRANSIT_ENTER) {
            // Special case: we are animating in a dream, while the keyguard
            // is shown.  We don't want an animation on the dream, because
            // we need it shown immediately with the keyguard animating away
            // to reveal it.
            return -1;
        }

        return 0;
    }

    private int selectDockedDividerAnimationLw(WindowState win, int transit) {
        int insets = mWindowManagerFuncs.getDockedDividerInsetsLw();

        // If the divider is behind the navigation bar, don't animate.
        final Rect frame = win.getFrameLw();
        final boolean behindNavBar = mNavigationBar != null
                && ((mNavigationBarPosition == NAV_BAR_BOTTOM
                        && frame.top + insets >= mNavigationBar.getFrameLw().top)
                || (mNavigationBarPosition == NAV_BAR_RIGHT
                        && frame.left + insets >= mNavigationBar.getFrameLw().left)
                || (mNavigationBarPosition == NAV_BAR_LEFT
                        && frame.right - insets <= mNavigationBar.getFrameLw().right));
        final boolean landscape = frame.height() > frame.width();
        final boolean offscreenLandscape = landscape && (frame.right - insets <= 0
                || frame.left + insets >= win.getDisplayFrameLw().right);
        final boolean offscreenPortrait = !landscape && (frame.top - insets <= 0
                || frame.bottom + insets >= win.getDisplayFrameLw().bottom);
        final boolean offscreen = offscreenLandscape || offscreenPortrait;
        if (behindNavBar || offscreen) {
            return 0;
        }
        if (transit == TRANSIT_ENTER || transit == TRANSIT_SHOW) {
            return R.anim.fade_in;
        } else if (transit == TRANSIT_EXIT) {
            return R.anim.fade_out;
        } else {
            return 0;
        }
    }

    @Override
    public void selectRotationAnimationLw(int anim[]) {
        if (PRINT_ANIM) Slog.i(TAG, "selectRotationAnimation mTopFullscreen="
                + mTopFullscreenOpaqueWindowState + " rotationAnimation="
                + (mTopFullscreenOpaqueWindowState == null ?
                        "0" : mTopFullscreenOpaqueWindowState.getAttrs().rotationAnimation));
        if (mTopFullscreenOpaqueWindowState != null) {
            int animationHint = mTopFullscreenOpaqueWindowState.getRotationAnimationHint();
            if (animationHint < 0 && mTopIsFullscreen) {
                animationHint = mTopFullscreenOpaqueWindowState.getAttrs().rotationAnimation;
            }
            switch (animationHint) {
                case ROTATION_ANIMATION_CROSSFADE:
                case ROTATION_ANIMATION_SEAMLESS: // Crossfade is fallback for seamless.
                    anim[0] = R.anim.rotation_animation_xfade_exit;
                    anim[1] = R.anim.rotation_animation_enter;
                    break;
                case ROTATION_ANIMATION_JUMPCUT:
                    anim[0] = R.anim.rotation_animation_jump_exit;
                    anim[1] = R.anim.rotation_animation_enter;
                    break;
                case ROTATION_ANIMATION_ROTATE:
                default:
                    anim[0] = anim[1] = 0;
                    break;
            }
        } else {
            anim[0] = anim[1] = 0;
        }
    }

    @Override
    public boolean validateRotationAnimationLw(int exitAnimId, int enterAnimId,
            boolean forceDefault) {
        switch (exitAnimId) {
            case R.anim.rotation_animation_xfade_exit:
            case R.anim.rotation_animation_jump_exit:
                // These are the only cases that matter.
                if (forceDefault) {
                    return false;
                }
                int anim[] = new int[2];
                selectRotationAnimationLw(anim);
                return (exitAnimId == anim[0] && enterAnimId == anim[1]);
            default:
                return true;
        }
    }

    @Override
    public Animation createForceHideEnterAnimation(boolean onWallpaper,
            boolean goingToNotificationShade) {
        if (goingToNotificationShade) {
            return AnimationUtils.loadAnimation(mContext, R.anim.lock_screen_behind_enter_fade_in);
        }

        AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(mContext, onWallpaper ?
                    R.anim.lock_screen_behind_enter_wallpaper :
                    R.anim.lock_screen_behind_enter);

        // TODO: Use XML interpolators when we have log interpolators available in XML.
        final List<Animation> animations = set.getAnimations();
        for (int i = animations.size() - 1; i >= 0; --i) {
            animations.get(i).setInterpolator(mLogDecelerateInterpolator);
        }

        return set;
    }


    @Override
    public Animation createForceHideWallpaperExitAnimation(boolean goingToNotificationShade) {
        if (goingToNotificationShade) {
            return null;
        } else {
            return AnimationUtils.loadAnimation(mContext, R.anim.lock_screen_wallpaper_exit);
        }
    }

    private static void awakenDreams() {
        IDreamManager dreamManager = getDreamManager();
        if (dreamManager != null) {
            try {
                dreamManager.awaken();
            } catch (RemoteException e) {
                // fine, stay asleep then
            }
        }
    }

    static IDreamManager getDreamManager() {
        return IDreamManager.Stub.asInterface(
                ServiceManager.checkService(DreamService.DREAM_SERVICE));
    }

    TelecomManager getTelecommService() {
        return (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
    }

    static IAudioService getAudioService() {
        IAudioService audioService = IAudioService.Stub.asInterface(
                ServiceManager.checkService(Context.AUDIO_SERVICE));
        if (audioService == null) {
            Log.w(TAG, "Unable to find IAudioService interface.");
        }
        return audioService;
    }

    boolean keyguardOn() {
        return isKeyguardShowingAndNotOccluded() || inKeyguardRestrictedKeyInputMode();
    }

    private static final int[] WINDOW_TYPES_WHERE_HOME_DOESNT_WORK = {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
        };

/**prize-add_factorytest-by-liup-20150415-start*/
	String getTopApp() {
		ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
		if(null == RunningTask || RunningTask.size() == 0){
			return "";
		}
		
		ActivityManager.RunningTaskInfo taskInfo = RunningTask.get(0);
		if(taskInfo == null){
			return "";
		}else{
			return taskInfo.topActivity.getClassName().toString();
		}
		
	}
/**prize-add_factorytest-by-liup-20150415-end*/
/* add by lyt-lanwm 20131113 */
    private void sendKeyEvent(int keycode) {
	long  now = SystemClock.uptimeMillis();
	sendKeyEventTime = now; //add for Double taping to close screen off. lyt-linkh 2013.12.27
	KeyEvent event = new KeyEvent(now, now, KeyEvent.ACTION_DOWN,keycode, 0);
	InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC); 
	event = new KeyEvent(now, now, KeyEvent.ACTION_UP, keycode, 0);
	InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC); 
    } 
   //end

//add for Double taping to close screen off. prize-wangyunhe
    private final Object mDoubleTapLock = new Object();
    private boolean preIsDown = false;
    private boolean secondDown = false;	
    private boolean preDoubleTap = false;
    private static final long DOUBLE_TAP_TIME_OUT = 200; //ms
    private static long sendKeyEventTime = 0;
    private static final boolean DT_TO_CLOSE_SCREEN = true;
	
    private final Runnable mDoubleTapTimeout = new Runnable() {
        @Override public void run() {
            synchronized (mDoubleTapLock) {
			printMyLog("--- DoubleTapTimeout runnable. set preDoubleTap = false");
			preDoubleTap = false;
			
			printMyLog("-- inject home event !!!!!------");
			sendKeyEvent(KeyEvent.KEYCODE_HOME);
            }
        }
    };
    private void turnOffScreen() {
	printMyLog("--- Double tap to turn off screen -----");
	if(mPowerManager != null) {
		if(mPowerManager.isScreenOn()) {
			mPowerManager.goToSleep(SystemClock.uptimeMillis()); 
		} else {
			printMyLog("--- skip !! The device is screen off -----");
		}
	} else {
		printMyLog("--- mPowerManager = null !! -----");
	}
   }
   private void startFlash() { 
       /*modify-by-zhongweilin
		ContentValues values = new ContentValues();  
        values.put("flashstatus","3"); 
		mContext.getContentResolver().update(Uri.parse("content://com.android.flash/systemflashs"), values, null, null);
        */
       Settings.System.putInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, 3);
       
	} 
	
	private void closeFlash() { 
        /*modify-by-zhongweilin
		ContentValues values = new ContentValues();  
        values.put("flashstatus","0"); 
		mContext.getContentResolver().update(Uri.parse("content://com.android.flash/systemflashs"), values, null, null);
        */
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, 4);
       
	} 

    /** {@inheritDoc} */
    @Override
    public long interceptKeyBeforeDispatching(WindowState win, KeyEvent event, int policyFlags) {
        final boolean keyguardOn = keyguardOn();
        final int keyCode = event.getKeyCode();
        final int repeatCount = event.getRepeatCount();
        final int metaState = event.getMetaState();
        final int flags = event.getFlags();
        final boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        final boolean canceled = event.isCanceled();

        /// M: Add more log at WMS
        //prize-add by lihuangyuan ,for open key dispatch log 2017-12-12
        if (true || (false == IS_USER_BUILD) || DEBUG_INPUT) {
            Log.d(TAG, "interceptKeyTi keyCode=" + keyCode + " down=" + down + " repeatCount="
                    + repeatCount + " keyguardOn=" + keyguardOn + " mHomePressed=" + mHomePressed
                    + " canceled=" + canceled + " metaState:" + metaState);
        }

        // If we think we might have a volume down & power key chord on the way
        // but we're not sure, then tell the dispatcher to wait a little while and
        // try again later before dispatching.
        if (mScreenshotChordEnabled && (flags & KeyEvent.FLAG_FALLBACK) == 0) {
            if (mScreenshotChordVolumeDownKeyTriggered && !mScreenshotChordPowerKeyTriggered) {
                final long now = SystemClock.uptimeMillis();
                final long timeoutTime = mScreenshotChordVolumeDownKeyTime
                        + SCREENSHOT_CHORD_DEBOUNCE_DELAY_MILLIS;
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                    && mScreenshotChordVolumeDownKeyConsumed) {
                if (!down) {
                    mScreenshotChordVolumeDownKeyConsumed = false;
                }
                return -1;
            }
        }

        /// M: Screen unpinning @{
        if (!mHasNavigationBar
                && (flags & KeyEvent.FLAG_FALLBACK) == 0
                && keyCode == DISMISS_SCREEN_PINNING_KEY_CODE
                && (down && repeatCount == 1)) { // long press
            interceptDismissPinningChord();
        }
        /// @}

        // Cancel any pending meta actions if we see any other keys being pressed between the down
        // of the meta key and its corresponding up.
        if (mPendingMetaAction && !KeyEvent.isMetaKey(keyCode)) {
            mPendingMetaAction = false;
        }
        // Any key that is not Alt or Meta cancels Caps Lock combo tracking.
        if (mPendingCapsLockToggle && !KeyEvent.isMetaKey(keyCode) && !KeyEvent.isAltKey(keyCode)) {
            mPendingCapsLockToggle = false;
        }
	//add for double taping to close screen off. prize-wangyunhe 2015.10.08
	final boolean bLockscreenOpenTorch = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_LOCKSCREEN_OPEN_TORCH,0) == 1;
	     final boolean prizekeyguardOn = Settings.System.getInt(mContext.getContentResolver(),PRIZE_LOCK_STATE,0)== 1;
	if(bLockscreenOpenTorch == true && keyCode == KeyEvent.KEYCODE_HOME && (keyguardOn||prizekeyguardOn)){
		if ((event.getFlags() & KeyEvent.FLAG_LONG_PRESS) != 0) {
			if(down){
				//dismissKeyguardLw();
				//Intent intent = new Intent();
				//intent.setClassName("com.android.flash", "com.android.flash.FlashLightMainActivity");
				//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//mContext.startActivity(intent);
				int isSystemFlashOn = SystemProperties.getInt("persist.sys.prizeflash",0);
				if(isSystemFlashOn==3 || isSystemFlashOn==1){
					closeFlash();
				}else{
					startFlash();
				}
				

				return -1;
			}
		}
	}

	final boolean bDblclickSleep = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_DBLCLICK_SLEEP,0) == 1;
	if (DT_TO_CLOSE_SCREEN && bDblclickSleep &&
		keyCode == KeyEvent.KEYCODE_HOME && 
			sendKeyEventTime != event.getEventTime()) {
		printMyLog("---preDoubleTap = " + preDoubleTap);
		if(preDoubleTap == true) {
			printMyLog("--- remove  mDoubleTapTimeout runnable ---");
			mHandler.removeCallbacks(mDoubleTapTimeout);
			printMyLog("--- it's a double tap event ---");
			if(down) {
				if(repeatCount == 0) {
					secondDown = true;
				} else {
					secondDown = false;
					preDoubleTap = false;
				}
			} else {			
				if(secondDown) {
					secondDown = false;
					preDoubleTap = false;
									
					if(event.getAction() == KeyEvent.ACTION_UP) {
						turnOffScreen();
						return -1;
					}
				}
			}

		} else {
			if(down) {
				if(repeatCount == 0) {
					preIsDown = true;
					printMyLog("---intercept Down event.  preIsDown=ture");
					return -1;
				} else {
					printMyLog("---long press event.  preIsDown=false");
					preIsDown = false;
				}
			} else {
				printMyLog("--- Not Down key event. preIsDown=" + preIsDown);
				if(preIsDown) {
					if(event.getAction() == KeyEvent.ACTION_UP) {
						preDoubleTap = true;
						printMyLog(" --- post DoubleTap runnable. timeout:  " + DOUBLE_TAP_TIME_OUT);
						mHandler.postDelayed(mDoubleTapTimeout, DOUBLE_TAP_TIME_OUT);
						return -1;
					} else {
						preDoubleTap = false;
					}
					// reset
					preIsDown = false;	
				}
			}
		}
	}
	//end....

        // First we always handle the home key here, so applications
        // can never break it, although if keyguard is on, we do let
        // it handle it, because that gives us the correct 5 second
        // timeout.
        if (keyCode == KeyEvent.KEYCODE_HOME) {
			/**prize-add_factorytest-by-liup-20150415-start*/
				String topApp = getTopApp();
				if (topApp.equals("com.prize.factorytest.Key.Key") || topApp.equals("com.prize.autotest.mmi.AutoKeyTestActivity") ) {
					return 0;
				}
				if (topApp.equals("com.prize.factorytest.AgingTestImplActivity")) {
					return 0;
				}
			/**prize-add_factorytest-by-liup-20150413-end*/

            /**prize-add-game-reading-mode-liup-20150420-start*/
			if(PrizeOption.PRIZE_GAME_MODE){
				final boolean bPrizeGameMode = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_GAME_MODE,0) == 1;
				if (bPrizeGameMode) {
					Log.d(TAG, "bPrizeGameMode discard home key!");
					return -1;
				}
			}
			/**prize-add-game-reading-mode-liup-20150420-end*/

			/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
            if(PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
                Log.i(TAG, "PowerExtendMode, not go home!");
                /*PRIZE-click home go to PowerExtendMode-add by wangxianzhen-2015-12-08-start bugid 9641*/
                Intent powerIntent = new Intent();
                powerIntent.setClassName("com.android.superpowersave", "com.android.superpowersave.SuperPowerActivity");
                powerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivityAsUser(powerIntent, UserHandle.CURRENT);
                mContext.sendBroadcast(new Intent("android.intent.action.ACTION_CLOSE_SUPERPOWER_NOTIFICATION"));
                /*PRIZE-click home go to PowerExtendMode-add by wangxianzhen-2015-12-08-end bugid 9641*/
                return 0;
            }
            /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/

            /* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 start */
            if (Settings.System.getInt(mContext.getContentResolver(), DISABLE_NAVIGATION, 0) == 1) {
                Log.i(TAG,"Contacts requirement, disable click home");
                return 0;
            }
            /* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 end */
            // If we have released the home key, and didn't do anything else
            // while it was pressed, then it is time to go home!
            if (!down) {
                cancelPreloadRecentApps();

                mHomePressed = false;
                if (mHomeConsumed) {
                    mHomeConsumed = false;
                    return -1;
                }

                if (canceled) {
                    Log.i(TAG, "Ignoring HOME; event canceled.");
                    return -1;
                }

				/*prize-xuchunming-20180411-bugid:50825-start*/
				if (topApp.equals("com.android.camera.CameraLauncher")) {
					Log.d(TAG, "camera recevie home key ");
					Intent floatwin = new  Intent("com.prize.home.click");
					floatwin.setPackage("com.android.prizefloatwindow");
					mContext.sendBroadcast(floatwin);
					return 0;
				}
				/*prize-xuchunming-20180411-bugid:50825-end*/

                // Delay handling home if a double-tap is possible.
                if (mDoubleTapOnHomeBehavior != DOUBLE_TAP_HOME_NOTHING) {
                    mHandler.removeCallbacks(mHomeDoubleTapTimeoutRunnable); // just in case
                    mHomeDoubleTapPending = true;
                    mHandler.postDelayed(mHomeDoubleTapTimeoutRunnable,
                            ViewConfiguration.getDoubleTapTimeout());
                    return -1;
                }

                handleShortPressOnHome();
                mContext.sendBroadcast(new Intent("com.prize.home.click"));//PRIZE-Add-ContactsV8-huangpengfei-2018_2_7
                return -1;
            }

            // If a system window has focus, then it doesn't make sense
            // right now to interact with applications.
            WindowManager.LayoutParams attrs = win != null ? win.getAttrs() : null;
            if (attrs != null) {
                final int type = attrs.type;
                if (type == WindowManager.LayoutParams.TYPE_KEYGUARD_SCRIM
                        || type == WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG
                        || (attrs.privateFlags & PRIVATE_FLAG_KEYGUARD) != 0) {
                    // the "app" is keyguard, so give it the key
                    return 0;
                }
                final int typeCount = WINDOW_TYPES_WHERE_HOME_DOESNT_WORK.length;
                for (int i=0; i<typeCount; i++) {
                    if (type == WINDOW_TYPES_WHERE_HOME_DOESNT_WORK[i]) {
                        // don't do anything, but also don't pass it to the app
                        return -1;
                    }
                }
            }

            // Remember that home is pressed and handle special actions.
            if (repeatCount == 0) {
                mHomePressed = true;
                if (mHomeDoubleTapPending) {
                    mHomeDoubleTapPending = false;
                    mHandler.removeCallbacks(mHomeDoubleTapTimeoutRunnable);
                    handleDoubleTapOnHome();
                } else if (mLongPressOnHomeBehavior == LONG_PRESS_HOME_RECENT_SYSTEM_UI
                        || mDoubleTapOnHomeBehavior == DOUBLE_TAP_HOME_RECENT_SYSTEM_UI) {
                    preloadRecentApps();
                }
            } else if ((event.getFlags() & KeyEvent.FLAG_LONG_PRESS) != 0) {
                if (!keyguardOn) {
                    handleLongPressOnHome(event.getDeviceId());
                }
            }
            return -1;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            // Hijack modified menu keys for debugging features
            final int chordBug = KeyEvent.META_SHIFT_ON;

            if (down && repeatCount == 0) {
				Log.d("morgan","KEYCODE_MENU" );
                if (mEnableShiftMenuBugReports && (metaState & chordBug) == chordBug) {
                    Intent intent = new Intent(Intent.ACTION_BUG_REPORT);
                    mContext.sendOrderedBroadcastAsUser(intent, UserHandle.CURRENT,
                            null, null, null, 0, null, null);
                    return -1;
                }
            }
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            if (down) {
                if (repeatCount == 0) {
                    mSearchKeyShortcutPending = true;
                    mConsumeSearchKeyUp = false;
                }
            } else {
                mSearchKeyShortcutPending = false;
                if (mConsumeSearchKeyUp) {
                    mConsumeSearchKeyUp = false;
                    return -1;
                }
            }
            return 0;
        } else if (keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
            if (!keyguardOn) {
                if (down && repeatCount == 0) {
                    preloadRecentApps();
                } else if (!down) {
                    toggleRecentApps();
                }
            }
            return -1;
        } else if (keyCode == KeyEvent.KEYCODE_N && event.isMetaPressed()) {
            if (down) {
                IStatusBarService service = getStatusBarService();
                if (service != null) {
                    try {
                        service.expandNotificationsPanel();
                    } catch (RemoteException e) {
                        // do nothing.
                    }
                }
            }
        } else if (keyCode == KeyEvent.KEYCODE_S && event.isMetaPressed()
                && event.isCtrlPressed()) {
            if (down && repeatCount == 0) {
                int type = event.isShiftPressed() ? TAKE_SCREENSHOT_SELECTED_REGION
                        : TAKE_SCREENSHOT_FULLSCREEN;
                mScreenshotRunnable.setScreenshotType(type);
                mHandler.post(mScreenshotRunnable);
                return -1;
            }
        } else if (keyCode == KeyEvent.KEYCODE_SLASH && event.isMetaPressed()) {
            if (down && repeatCount == 0 && !isKeyguardLocked()) {
                toggleKeyboardShortcutsMenu(event.getDeviceId());
            }
        } else if (keyCode == KeyEvent.KEYCODE_ASSIST) {
            if (down) {
                if (repeatCount == 0) {
                    mAssistKeyLongPressed = false;
                } else if (repeatCount == 1) {
                    mAssistKeyLongPressed = true;
                    if (!keyguardOn) {
                         launchAssistLongPressAction();
                    }
                }
            } else {
                if (mAssistKeyLongPressed) {
                    mAssistKeyLongPressed = false;
                } else {
                    if (!keyguardOn) {
                        launchAssistAction(null, event.getDeviceId());
                    }
                }
            }
            return -1;
        } else if (keyCode == KeyEvent.KEYCODE_VOICE_ASSIST) {
            if (!down) {
                Intent voiceIntent;
                if (!keyguardOn) {
                    voiceIntent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
                } else {
                    IDeviceIdleController dic = IDeviceIdleController.Stub.asInterface(
                            ServiceManager.getService(Context.DEVICE_IDLE_CONTROLLER));
                    if (dic != null) {
                        try {
                            dic.exitIdle("voice-search");
                        } catch (RemoteException e) {
                        }
                    }
                    voiceIntent = new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
                    voiceIntent.putExtra(RecognizerIntent.EXTRA_SECURE, true);
                }
                startActivityAsUser(voiceIntent, UserHandle.CURRENT_OR_SELF);
            }
        } else if (keyCode == KeyEvent.KEYCODE_SYSRQ) {
            if (down && repeatCount == 0) {
                mScreenshotRunnable.setScreenshotType(TAKE_SCREENSHOT_FULLSCREEN);
                mHandler.post(mScreenshotRunnable);
            }
            return -1;
        } else if (keyCode == KeyEvent.KEYCODE_BRIGHTNESS_UP
                || keyCode == KeyEvent.KEYCODE_BRIGHTNESS_DOWN) {
            if (down) {
                int direction = keyCode == KeyEvent.KEYCODE_BRIGHTNESS_UP ? 1 : -1;

                // Disable autobrightness if it's on
                int auto = Settings.System.getIntForUser(
                        mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                        UserHandle.USER_CURRENT_OR_SELF);
                if (auto != 0) {
                    Settings.System.putIntForUser(mContext.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                            UserHandle.USER_CURRENT_OR_SELF);
                }

                int min = mPowerManager.getMinimumScreenBrightnessSetting();
                int max = mPowerManager.getMaximumScreenBrightnessSetting();
                int step = (max - min + BRIGHTNESS_STEPS - 1) / BRIGHTNESS_STEPS * direction;
                int brightness = Settings.System.getIntForUser(mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,
                        mPowerManager.getDefaultScreenBrightnessSetting(),
                        UserHandle.USER_CURRENT_OR_SELF);
                brightness += step;
                // Make sure we don't go beyond the limits.
                brightness = Math.min(max, brightness);
                brightness = Math.max(min, brightness);

                Settings.System.putIntForUser(mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, brightness,
                        UserHandle.USER_CURRENT_OR_SELF);
                startActivityAsUser(new Intent(Intent.ACTION_SHOW_BRIGHTNESS_DIALOG),
                        UserHandle.CURRENT_OR_SELF);
            }
            return -1;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            if (mUseTvRouting) {
                // On TVs volume keys never go to the foreground app.
                dispatchDirectAudioEvent(event);
                return -1;
            }
        }

        // Toggle Caps Lock on META-ALT.
        boolean actionTriggered = false;
        if (KeyEvent.isModifierKey(keyCode)) {
            if (!mPendingCapsLockToggle) {
                // Start tracking meta state for combo.
                mInitialMetaState = mMetaState;
                mPendingCapsLockToggle = true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                int altOnMask = mMetaState & KeyEvent.META_ALT_MASK;
                int metaOnMask = mMetaState & KeyEvent.META_META_MASK;

                // Check for Caps Lock toggle
                if ((metaOnMask != 0) && (altOnMask != 0)) {
                    // Check if nothing else is pressed
                    if (mInitialMetaState == (mMetaState ^ (altOnMask | metaOnMask))) {
                        // Handle Caps Lock Toggle
                        mInputManagerInternal.toggleCapsLock(event.getDeviceId());
                        actionTriggered = true;
                    }
                }

                // Always stop tracking when key goes up.
                mPendingCapsLockToggle = false;
            }
        }
        // Store current meta state to be able to evaluate it later.
        mMetaState = metaState;

        if (actionTriggered) {
            return -1;
        }

        if (KeyEvent.isMetaKey(keyCode)) {
            if (down) {
                mPendingMetaAction = true;
            } else if (mPendingMetaAction) {
                launchAssistAction(Intent.EXTRA_ASSIST_INPUT_HINT_KEYBOARD, event.getDeviceId());
            }
            return -1;
        }

        // Shortcuts are invoked through Search+key, so intercept those here
        // Any printing key that is chorded with Search should be consumed
        // even if no shortcut was invoked.  This prevents text from being
        // inadvertently inserted when using a keyboard that has built-in macro
        // shortcut keys (that emit Search+x) and some of them are not registered.
        if (mSearchKeyShortcutPending) {
            final KeyCharacterMap kcm = event.getKeyCharacterMap();
            if (kcm.isPrintingKey(keyCode)) {
                mConsumeSearchKeyUp = true;
                mSearchKeyShortcutPending = false;
                if (down && repeatCount == 0 && !keyguardOn) {
                    Intent shortcutIntent = mShortcutManager.getIntent(kcm, keyCode, metaState);
                    if (shortcutIntent != null) {
                        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivityAsUser(shortcutIntent, UserHandle.CURRENT);
                            dismissKeyboardShortcutsMenu();
                        } catch (ActivityNotFoundException ex) {
                            Slog.w(TAG, "Dropping shortcut key combination because "
                                    + "the activity to which it is registered was not found: "
                                    + "SEARCH+" + KeyEvent.keyCodeToString(keyCode), ex);
                        }
                    } else {
                        Slog.i(TAG, "Dropping unregistered shortcut key combination: "
                                + "SEARCH+" + KeyEvent.keyCodeToString(keyCode));
                    }
                }
                return -1;
            }
        }

        // Invoke shortcuts using Meta.
        if (down && repeatCount == 0 && !keyguardOn
                && (metaState & KeyEvent.META_META_ON) != 0) {
            final KeyCharacterMap kcm = event.getKeyCharacterMap();
            if (kcm.isPrintingKey(keyCode)) {
                Intent shortcutIntent = mShortcutManager.getIntent(kcm, keyCode,
                        metaState & ~(KeyEvent.META_META_ON
                                | KeyEvent.META_META_LEFT_ON | KeyEvent.META_META_RIGHT_ON));
                if (shortcutIntent != null) {
                    shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivityAsUser(shortcutIntent, UserHandle.CURRENT);
                        dismissKeyboardShortcutsMenu();
                    } catch (ActivityNotFoundException ex) {
                        Slog.w(TAG, "Dropping shortcut key combination because "
                                + "the activity to which it is registered was not found: "
                                + "META+" + KeyEvent.keyCodeToString(keyCode), ex);
                    }
                    return -1;
                }
            }
        }

        // Handle application launch keys.
        if (down && repeatCount == 0 && !keyguardOn) {
            String category = sApplicationLaunchKeyCategories.get(keyCode);
            if (category != null) {
                Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, category);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivityAsUser(intent, UserHandle.CURRENT);
                    dismissKeyboardShortcutsMenu();
                } catch (ActivityNotFoundException ex) {
                    Slog.w(TAG, "Dropping application launch key because "
                            + "the activity to which it is registered was not found: "
                            + "keyCode=" + keyCode + ", category=" + category, ex);
                }
                return -1;
            }
        }

        // Display task switcher for ALT-TAB.
        if (down && repeatCount == 0 && keyCode == KeyEvent.KEYCODE_TAB) {
            if (mRecentAppsHeldModifiers == 0 && !keyguardOn && isUserSetupComplete()) {
                final int shiftlessModifiers = event.getModifiers() & ~KeyEvent.META_SHIFT_MASK;
                if (KeyEvent.metaStateHasModifiers(shiftlessModifiers, KeyEvent.META_ALT_ON)) {
                    mRecentAppsHeldModifiers = shiftlessModifiers;
                    showRecentApps(true, false);
                    return -1;
                }
            }
        } else if (!down && mRecentAppsHeldModifiers != 0
                && (metaState & mRecentAppsHeldModifiers) == 0) {
            mRecentAppsHeldModifiers = 0;
            hideRecentApps(true, false);
        }

        // Handle input method switching.
        if (down && repeatCount == 0
                && (keyCode == KeyEvent.KEYCODE_LANGUAGE_SWITCH
                        || (keyCode == KeyEvent.KEYCODE_SPACE
                                && (metaState & KeyEvent.META_META_MASK) != 0))) {
            final boolean forwardDirection = (metaState & KeyEvent.META_SHIFT_MASK) == 0;
            mWindowManagerFuncs.switchInputMethod(forwardDirection);
            return -1;
        }
        if (mLanguageSwitchKeyPressed && !down
                && (keyCode == KeyEvent.KEYCODE_LANGUAGE_SWITCH
                        || keyCode == KeyEvent.KEYCODE_SPACE)) {
            mLanguageSwitchKeyPressed = false;
            return -1;
        }

        if (isValidGlobalKey(keyCode)
                && mGlobalKeyManager.handleGlobalKey(mContext, keyCode, event)) {
            return -1;
        }

        if (down) {
            long shortcutCode = keyCode;
            if (event.isCtrlPressed()) {
                shortcutCode |= ((long) KeyEvent.META_CTRL_ON) << Integer.SIZE;
            }

            if (event.isAltPressed()) {
                shortcutCode |= ((long) KeyEvent.META_ALT_ON) << Integer.SIZE;
            }

            if (event.isShiftPressed()) {
                shortcutCode |= ((long) KeyEvent.META_SHIFT_ON) << Integer.SIZE;
            }

            if (event.isMetaPressed()) {
                shortcutCode |= ((long) KeyEvent.META_META_ON) << Integer.SIZE;
            }

            IShortcutService shortcutService = mShortcutKeyServices.get(shortcutCode);
            if (shortcutService != null) {
                try {
                    if (isUserSetupComplete()) {
                        shortcutService.notifyShortcutKeyPressed(shortcutCode);
                    }
                } catch (RemoteException e) {
                    mShortcutKeyServices.delete(shortcutCode);
                }
                return -1;
            }
        }

        // Reserve all the META modifier combos for system behavior
        if ((metaState & KeyEvent.META_META_ON) != 0) {
            return -1;
        }

        // Let the application handle the key.
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public KeyEvent dispatchUnhandledKey(WindowState win, KeyEvent event, int policyFlags) {
        // Note: This method is only called if the initial down was unhandled.
        if (DEBUG_INPUT) {
            Slog.d(TAG, "Unhandled key: win=" + win + ", action=" + event.getAction()
                    + ", flags=" + event.getFlags()
                    + ", keyCode=" + event.getKeyCode()
                    + ", scanCode=" + event.getScanCode()
                    + ", metaState=" + event.getMetaState()
                    + ", repeatCount=" + event.getRepeatCount()
                    + ", policyFlags=" + policyFlags);
        }

        KeyEvent fallbackEvent = null;
        if ((event.getFlags() & KeyEvent.FLAG_FALLBACK) == 0) {
            final KeyCharacterMap kcm = event.getKeyCharacterMap();
            final int keyCode = event.getKeyCode();
            final int metaState = event.getMetaState();
            final boolean initialDown = event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getRepeatCount() == 0;

            // Check for fallback actions specified by the key character map.
            final FallbackAction fallbackAction;
            if (initialDown) {
                fallbackAction = kcm.getFallbackAction(keyCode, metaState);
            } else {
                fallbackAction = mFallbackActions.get(keyCode);
            }

            if (fallbackAction != null) {
                if (DEBUG_INPUT) {
                    Slog.d(TAG, "Fallback: keyCode=" + fallbackAction.keyCode
                            + " metaState=" + Integer.toHexString(fallbackAction.metaState));
                }

                final int flags = event.getFlags() | KeyEvent.FLAG_FALLBACK;
                fallbackEvent = KeyEvent.obtain(
                        event.getDownTime(), event.getEventTime(),
                        event.getAction(), fallbackAction.keyCode,
                        event.getRepeatCount(), fallbackAction.metaState,
                        event.getDeviceId(), event.getScanCode(),
                        flags, event.getSource(), null);

                if (!interceptFallback(win, fallbackEvent, policyFlags)) {
                    fallbackEvent.recycle();
                    fallbackEvent = null;
                }

                if (initialDown) {
                    mFallbackActions.put(keyCode, fallbackAction);
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    mFallbackActions.remove(keyCode);
                    fallbackAction.recycle();
                }
            }
        }

        if (DEBUG_INPUT) {
            if (fallbackEvent == null) {
                Slog.d(TAG, "No fallback.");
            } else {
                Slog.d(TAG, "Performing fallback: " + fallbackEvent);
            }
        }
        return fallbackEvent;
    }

    private boolean interceptFallback(WindowState win, KeyEvent fallbackEvent, int policyFlags) {
        int actions = interceptKeyBeforeQueueing(fallbackEvent, policyFlags);
        if ((actions & ACTION_PASS_TO_USER) != 0) {
            long delayMillis = interceptKeyBeforeDispatching(
                    win, fallbackEvent, policyFlags);
            if (delayMillis == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void registerShortcutKey(long shortcutCode, IShortcutService shortcutService)
            throws RemoteException {
        synchronized (mLock) {
            IShortcutService service = mShortcutKeyServices.get(shortcutCode);
            if (service != null && service.asBinder().pingBinder()) {
                throw new RemoteException("Key already exists.");
            }

            mShortcutKeyServices.put(shortcutCode, shortcutService);
        }
    }

    @Override
    public boolean canShowDismissingWindowWhileLockedLw() {
        // If the keyguard is trusted, it will unlock without a challenge. Therefore, if we are in
        // the process of dismissing Keyguard, we don't need to hide them as the phone will be
        // unlocked right away in any case.
        return mKeyguardDelegate != null && mKeyguardDelegate.isTrusted()
                && mCurrentlyDismissingKeyguard;
    }

    private void launchAssistLongPressAction() {
        performHapticFeedbackLw(null, HapticFeedbackConstants.LONG_PRESS, false);
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_ASSIST);

        // launch the search activity
        Intent intent = new Intent(Intent.ACTION_SEARCH_LONG_PRESS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            // TODO: This only stops the factory-installed search manager.
            // Need to formalize an API to handle others
            SearchManager searchManager = getSearchManager();
            if (searchManager != null) {
                searchManager.stopSearch();
            }
            startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
            Slog.w(TAG, "No activity to handle assist long press action.", e);
        }
    }

    private void launchAssistAction(String hint, int deviceId) {
        sendCloseSystemWindows(SYSTEM_DIALOG_REASON_ASSIST);
        if (!isUserSetupComplete()) {
            // Disable opening assist window during setup
            return;
        }
        Bundle args = null;
        if (deviceId > Integer.MIN_VALUE) {
            args = new Bundle();
            args.putInt(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID, deviceId);
        }
        if ((mContext.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION) {
            // On TV, use legacy handling until assistants are implemented in the proper way.
            ((SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE))
                    .launchLegacyAssist(hint, UserHandle.myUserId(), args);
        } else {
            if (hint != null) {
                if (args == null) {
                    args = new Bundle();
                }
                args.putBoolean(hint, true);
            }
            StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
            if (statusbar != null) {
                statusbar.startAssist(args);
            }
        }
    }

    private void startActivityAsUser(Intent intent, UserHandle handle) {
        if (isUserSetupComplete()) {
            mContext.startActivityAsUser(intent, handle);
        } else {
            Slog.i(TAG, "Not starting activity because user setup is in progress: " + intent);
        }
    }

    private SearchManager getSearchManager() {
        if (mSearchManager == null) {
            mSearchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        }
        return mSearchManager;
    }

    private void preloadRecentApps() {
        mPreloadedRecentApps = true;
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.preloadRecentApps();
        }
    }

    private void cancelPreloadRecentApps() {
        if (mPreloadedRecentApps) {
            mPreloadedRecentApps = false;
            StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
            if (statusbar != null) {
                statusbar.cancelPreloadRecentApps();
            }
        }
    }

    private void toggleRecentApps() {
        mPreloadedRecentApps = false; // preloading no longer needs to be canceled
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.toggleRecentApps();
        }
    }

    @Override
    public void showRecentApps(boolean fromHome) {
        mHandler.removeMessages(MSG_DISPATCH_SHOW_RECENTS);
        mHandler.obtainMessage(MSG_DISPATCH_SHOW_RECENTS, fromHome ? 1 : 0, 0).sendToTarget();
    }

    private void showRecentApps(boolean triggeredFromAltTab, boolean fromHome) {
        mPreloadedRecentApps = false; // preloading no longer needs to be canceled
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.showRecentApps(triggeredFromAltTab, fromHome);
        }
    }

    private void toggleKeyboardShortcutsMenu(int deviceId) {
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.toggleKeyboardShortcutsMenu(deviceId);
        }
    }

    private void dismissKeyboardShortcutsMenu() {
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.dismissKeyboardShortcutsMenu();
        }
    }

    private void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHome) {
        mPreloadedRecentApps = false; // preloading no longer needs to be canceled
        StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
        if (statusbar != null) {
            statusbar.hideRecentApps(triggeredFromAltTab, triggeredFromHome);
        }
    }

    void launchHomeFromHotKey() {
        launchHomeFromHotKey(true /* awakenFromDreams */, true /*respectKeyguard*/);
    }

    /**
     * A home key -> launch home action was detected.  Take the appropriate action
     * given the situation with the keyguard.
     */
    void launchHomeFromHotKey(final boolean awakenFromDreams, final boolean respectKeyguard) {
        if (respectKeyguard) {
            if (isKeyguardShowingAndNotOccluded()) {
                // don't launch home if keyguard showing
                Slog.i(TAG, "launchHomeFromHotKey isKeyguardShowingAndNotOccluded return");//prize-add by lihuangyuan,for bug51875
                return;
            }

            if (!mHideLockScreen && mKeyguardDelegate.isInputRestricted()) {
                // when in keyguard restricted mode, must first verify unlock
                // before launching home
                mKeyguardDelegate.verifyUnlock(new OnKeyguardExitResult() {
                    @Override
                    public void onKeyguardExitResult(boolean success) {
                        if (success) {
                            try {
                                ActivityManagerNative.getDefault().stopAppSwitches();
                            } catch (RemoteException e) {
                            }
                            sendCloseSystemWindows(SYSTEM_DIALOG_REASON_HOME_KEY);
                            startDockOrHome(true /*fromHomeKey*/, awakenFromDreams);
                        }
                    }
                });
		  Slog.i(TAG, "launchHomeFromHotKey verifyUnlock return");//prize-add by lihuangyuan,for bug51875
                return;
            }
        }

        // no keyguard stuff to worry about, just launch home!
        try {
            ActivityManagerNative.getDefault().stopAppSwitches();
        } catch (RemoteException e) {
        }
        if (mRecentsVisible) {
            // Hide Recents and notify it to launch Home
            if (awakenFromDreams) {
                awakenDreams();
            }
	     Slog.i(TAG, "launchHomeFromHotKey hideRecentApps");//prize-add by lihuangyuan,for bug51875
            hideRecentApps(false, true);
            /*-prize-add by lihuangyuan,for bug51439-2018-03-06-start*/
	     mRecentsVisible = false;
	     /*-prize-add by lihuangyuan,for bug51439-2018-03-06-end*/
        } else {
            // Otherwise, just launch Home
            sendCloseSystemWindows(SYSTEM_DIALOG_REASON_HOME_KEY);
            startDockOrHome(true /*fromHomeKey*/, awakenFromDreams);
        }
    }

    private final Runnable mClearHideNavigationFlag = new Runnable() {
        @Override
        public void run() {
            synchronized (mWindowManagerFuncs.getWindowManagerLock()) {
                // Clear flags.
                mForceClearedSystemUiFlags &=
                        ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            mWindowManagerFuncs.reevaluateStatusBarVisibility();
        }
    };

    /**
     * Input handler used while nav bar is hidden.  Captures any touch on the screen,
     * to determine when the nav bar should be shown and prevent applications from
     * receiving those touches.
     */
    final class HideNavInputEventReceiver extends InputEventReceiver {
        public HideNavInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        @Override
        public void onInputEvent(InputEvent event) {
            boolean handled = false;
            try {
                if (event instanceof MotionEvent
                        && (event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
                    final MotionEvent motionEvent = (MotionEvent)event;
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        // When the user taps down, we re-show the nav bar.
                        boolean changed = false;
                        synchronized (mWindowManagerFuncs.getWindowManagerLock()) {
                            if (mInputConsumer == null) {
                                return;
                            }
                            // Any user activity always causes us to show the
                            // navigation controls, if they had been hidden.
                            // We also clear the low profile and only content
                            // flags so that tapping on the screen will atomically
                            // restore all currently hidden screen decorations.
                            int newVal = mResettingSystemUiFlags |
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_LOW_PROFILE |
                                    View.SYSTEM_UI_FLAG_FULLSCREEN;
                            if (mResettingSystemUiFlags != newVal) {
                                mResettingSystemUiFlags = newVal;
                                changed = true;
                            }
                            // We don't allow the system's nav bar to be hidden
                            // again for 1 second, to prevent applications from
                            // spamming us and keeping it from being shown.
                            newVal = mForceClearedSystemUiFlags |
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                            if (mForceClearedSystemUiFlags != newVal) {
                                mForceClearedSystemUiFlags = newVal;
                                changed = true;
                                mHandler.postDelayed(mClearHideNavigationFlag, 1000);
                            }
                        }
                        if (changed) {
                            mWindowManagerFuncs.reevaluateStatusBarVisibility();
                        }
                    }
                }
            } finally {
                finishInputEvent(event, handled);
            }
        }
    }
    final InputEventReceiver.Factory mHideNavInputEventReceiverFactory =
            new InputEventReceiver.Factory() {
        @Override
        public InputEventReceiver createInputEventReceiver(
                InputChannel inputChannel, Looper looper) {
            return new HideNavInputEventReceiver(inputChannel, looper);
        }
    };

    @Override
    public void setRecentsVisibilityLw(boolean visible) {
        Slog.d(TAG,"setRecentsVisibilityLw visible:"+visible);//prize-add by lihuangyuan,for bug51875
        mRecentsVisible = visible;
    }

    @Override
    public void setTvPipVisibilityLw(boolean visible) {
        mTvPictureInPictureVisible = visible;
    }

    @Override
    public int adjustSystemUiVisibilityLw(int visibility) {
        mStatusBarController.adjustSystemUiVisibilityLw(mLastSystemUiFlags, visibility);
        mNavigationBarController.adjustSystemUiVisibilityLw(mLastSystemUiFlags, visibility);

        // Reset any bits in mForceClearingStatusBarVisibility that
        // are now clear.
        mResettingSystemUiFlags &= visibility;
        // Clear any bits in the new visibility that are currently being
        // force cleared, before reporting it.
        return visibility & ~mResettingSystemUiFlags
                & ~mForceClearedSystemUiFlags;
    }

    @Override
    public boolean getInsetHintLw(WindowManager.LayoutParams attrs, Rect taskBounds,
            int displayRotation, int displayWidth, int displayHeight, Rect outContentInsets,
            Rect outStableInsets, Rect outOutsets) {
        final int fl = PolicyControl.getWindowFlags(null, attrs);
        final int sysuiVis = PolicyControl.getSystemUiVisibility(null, attrs);
        final int systemUiVisibility = (sysuiVis | attrs.subtreeSystemUiVisibility);

        final boolean useOutsets = outOutsets != null && shouldUseOutsets(attrs, fl);
        if (useOutsets) {
            int outset = ScreenShapeHelper.getWindowOutsetBottomPx(mContext.getResources());
            if (outset > 0) {
                if (displayRotation == Surface.ROTATION_0) {
                    outOutsets.bottom += outset;
                } else if (displayRotation == Surface.ROTATION_90) {
                    outOutsets.right += outset;
                } else if (displayRotation == Surface.ROTATION_180) {
                    outOutsets.top += outset;
                } else if (displayRotation == Surface.ROTATION_270) {
                    outOutsets.left += outset;
                }
            }
        }

        if ((fl & (FLAG_LAYOUT_IN_SCREEN | FLAG_LAYOUT_INSET_DECOR))
                == (FLAG_LAYOUT_IN_SCREEN | FLAG_LAYOUT_INSET_DECOR)) {
            int availRight, availBottom;
            if (canHideNavigationBar() &&
                    (systemUiVisibility & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0) {
                availRight = mUnrestrictedScreenLeft + mUnrestrictedScreenWidth;
                availBottom = mUnrestrictedScreenTop + mUnrestrictedScreenHeight;
            } else {
                availRight = mRestrictedScreenLeft + mRestrictedScreenWidth;
                availBottom = mRestrictedScreenTop + mRestrictedScreenHeight;
            }
            if ((systemUiVisibility & View.SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0) {
                if ((fl & FLAG_FULLSCREEN) != 0) {
                    outContentInsets.set(mStableFullscreenLeft, mStableFullscreenTop,
                            availRight - mStableFullscreenRight,
                            availBottom - mStableFullscreenBottom);
                } else {
                    outContentInsets.set(mStableLeft, mStableTop,
                            availRight - mStableRight, availBottom - mStableBottom);
                }
            } else if ((fl & FLAG_FULLSCREEN) != 0 || (fl & FLAG_LAYOUT_IN_OVERSCAN) != 0) {
                outContentInsets.setEmpty();
            } else if ((systemUiVisibility & (View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)) == 0) {
                outContentInsets.set(mCurLeft, mCurTop,
                        availRight - mCurRight, availBottom - mCurBottom);
            } else {
                outContentInsets.set(mCurLeft, mCurTop,
                        availRight - mCurRight, availBottom - mCurBottom);
            }

            outStableInsets.set(mStableLeft, mStableTop,
                    availRight - mStableRight, availBottom - mStableBottom);
            if (taskBounds != null) {
                calculateRelevantTaskInsets(taskBounds, outContentInsets,
                        displayWidth, displayHeight);
                calculateRelevantTaskInsets(taskBounds, outStableInsets,
                        displayWidth, displayHeight);
            }
            return mForceShowSystemBars;
        }
        outContentInsets.setEmpty();
        outStableInsets.setEmpty();
        return mForceShowSystemBars;
    }

    /**
     * For any given task bounds, the insets relevant for these bounds given the insets relevant
     * for the entire display.
     */
    private void calculateRelevantTaskInsets(Rect taskBounds, Rect inOutInsets, int displayWidth,
            int displayHeight) {
        mTmpRect.set(0, 0, displayWidth, displayHeight);
        mTmpRect.inset(inOutInsets);
        mTmpRect.intersect(taskBounds);
        int leftInset = mTmpRect.left - taskBounds.left;
        int topInset = mTmpRect.top - taskBounds.top;
        int rightInset = taskBounds.right - mTmpRect.right;
        int bottomInset = taskBounds.bottom - mTmpRect.bottom;
        inOutInsets.set(leftInset, topInset, rightInset, bottomInset);
    }

    private boolean shouldUseOutsets(WindowManager.LayoutParams attrs, int fl) {
        return attrs.type == TYPE_WALLPAPER || (fl & (WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN)) != 0;
    }

    /* prize-add-landscape split screen cut lcd ears-liyongli-20180417-start */
    private void prizeCutLcdEars(int displayWidth, int displayHeight, int displayRotation,
            int uiMode, int overscanLeft, int overscanRight, int overscanBottom, Rect dcf){
        if(!PrizeOption.PRIZE_NOTCH_SCREEN){
            return;
        }
        int cutWidth = mStatusBarHeight;
        if(displayRotation==Surface.ROTATION_270){
            updateRect(0, 0, cutWidth, 0);
        }else if(displayRotation==Surface.ROTATION_90){
            updateRect(cutWidth, 0, 0, 0);
        }
    }/* prize-add-landscape split screen cut lcd ears-liyongli-20180417-end */
    
    /** {@inheritDoc} */
    @Override
    public void beginLayoutLw(boolean isDefaultDisplay, int displayWidth, int displayHeight,
                              int displayRotation, int uiMode) {
        mDisplayRotation = displayRotation;
        final int overscanLeft, overscanTop, overscanRight, overscanBottom;
        if (isDefaultDisplay) {
            switch (displayRotation) {
                case Surface.ROTATION_90:
                    overscanLeft = mOverscanTop;
                    overscanTop = mOverscanRight;
                    overscanRight = mOverscanBottom;
                    overscanBottom = mOverscanLeft;
                    break;
                case Surface.ROTATION_180:
                    overscanLeft = mOverscanRight;
                    overscanTop = mOverscanBottom;
                    overscanRight = mOverscanLeft;
                    overscanBottom = mOverscanTop;
                    break;
                case Surface.ROTATION_270:
                    overscanLeft = mOverscanBottom;
                    overscanTop = mOverscanLeft;
                    overscanRight = mOverscanTop;
                    overscanBottom = mOverscanRight;
                    break;
                default:
                    overscanLeft = mOverscanLeft;
                    overscanTop = mOverscanTop;
                    overscanRight = mOverscanRight;
                    overscanBottom = mOverscanBottom;
                    break;
            }
        } else {
            overscanLeft = 0;
            overscanTop = 0;
            overscanRight = 0;
            overscanBottom = 0;
        }
        mOverscanScreenLeft = mRestrictedOverscanScreenLeft = 0;
        mOverscanScreenTop = mRestrictedOverscanScreenTop = 0;
        mOverscanScreenWidth = mRestrictedOverscanScreenWidth = displayWidth;
        mOverscanScreenHeight = mRestrictedOverscanScreenHeight = displayHeight;
        mSystemLeft = 0;
        mSystemTop = 0;
        mSystemRight = displayWidth;
        mSystemBottom = displayHeight;
        mUnrestrictedScreenLeft = overscanLeft;
        mUnrestrictedScreenTop = overscanTop;
        mUnrestrictedScreenWidth = displayWidth - overscanLeft - overscanRight;
        mUnrestrictedScreenHeight = displayHeight - overscanTop - overscanBottom;
        mRestrictedScreenLeft = mUnrestrictedScreenLeft;
        mRestrictedScreenTop = mUnrestrictedScreenTop;
        mRestrictedScreenWidth = mSystemGestures.screenWidth = mUnrestrictedScreenWidth;
        mRestrictedScreenHeight = mSystemGestures.screenHeight = mUnrestrictedScreenHeight;
        mDockLeft = mContentLeft = mVoiceContentLeft = mStableLeft = mStableFullscreenLeft
                = mCurLeft = mUnrestrictedScreenLeft;
        mDockTop = mContentTop = mVoiceContentTop = mStableTop = mStableFullscreenTop
                = mCurTop = mUnrestrictedScreenTop;
        mDockRight = mContentRight = mVoiceContentRight = mStableRight = mStableFullscreenRight
                = mCurRight = displayWidth - overscanRight;
        mDockBottom = mContentBottom = mVoiceContentBottom = mStableBottom = mStableFullscreenBottom
                = mCurBottom = displayHeight - overscanBottom;
        mDockLayer = 0x10000000;
        mStatusBarLayer = -1;

        prizeCutLcdEars(displayWidth, displayHeight,
                displayRotation, uiMode, overscanLeft, overscanRight, overscanBottom, null); 
        //prize-add-landscape cut the lcd ears-liyongli-20180417
            
        // start with the current dock rect, which will be (0,0,displayWidth,displayHeight)
        final Rect pf = mTmpParentFrame;
        final Rect df = mTmpDisplayFrame;
        final Rect of = mTmpOverscanFrame;
        final Rect vf = mTmpVisibleFrame;
        final Rect dcf = mTmpDecorFrame;
        pf.left = df.left = of.left = vf.left = mDockLeft;
        pf.top = df.top = of.top = vf.top = mDockTop;
        pf.right = df.right = of.right = vf.right = mDockRight;
        pf.bottom = df.bottom = of.bottom = vf.bottom = mDockBottom;
        dcf.setEmpty();  // Decor frame N/A for system bars.

        if (isDefaultDisplay) {

            /// M: add for fullscreen switch feature @{
            if (mSupportFullscreenSwitch) {
                if (mFocusedWindow != null && !mFocusedWindow.isFullscreenOn()) {
                    getSwitchFrame(mFocusedWindow);
                    mLastSystemUiFlags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
            }
            /// @}

            // For purposes of putting out fake window up to steal focus, we will
            // drive nav being hidden only by whether it is requested.
            final int sysui = mLastSystemUiFlags;
            boolean navVisible = (sysui & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
            boolean navTranslucent = (sysui
                    & (View.NAVIGATION_BAR_TRANSLUCENT | View.NAVIGATION_BAR_TRANSPARENT)) != 0;
            boolean immersive = (sysui & View.SYSTEM_UI_FLAG_IMMERSIVE) != 0;
            boolean immersiveSticky = (sysui & View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) != 0;
            boolean navAllowedHidden = immersive || immersiveSticky;
            navTranslucent &= !immersiveSticky;  // transient trumps translucent
            boolean isKeyguardShowing = isStatusBarKeyguard() && !mHideLockScreen;
            if (!isKeyguardShowing) {
                navTranslucent &= areTranslucentBarsAllowed();
            }
            boolean statusBarExpandedNotKeyguard = !isKeyguardShowing && mStatusBar != null
                    && mStatusBar.getAttrs().height == MATCH_PARENT
                    && mStatusBar.getAttrs().width == MATCH_PARENT;

            // When the navigation bar isn't visible, we put up a fake
            // input window to catch all touch events.  This way we can
            // detect when the user presses anywhere to bring back the nav
            // bar and ensure the application doesn't see the event.
            if (navVisible || navAllowedHidden) {
                if (mInputConsumer != null) {
                    mHandler.sendMessage(
                            mHandler.obtainMessage(MSG_DISPOSE_INPUT_CONSUMER, mInputConsumer));
                    mInputConsumer = null;
                }
            } else if (mInputConsumer == null) {
                mInputConsumer = mWindowManagerFuncs.addInputConsumer(mHandler.getLooper(),
                        mHideNavInputEventReceiverFactory);
            }

            // For purposes of positioning and showing the nav bar, if we have
            // decided that it can't be hidden (because of the screen aspect ratio),
            // then take that into account.
            navVisible |= !canHideNavigationBar();

            boolean updateSysUiVisibility = layoutNavigationBar(displayWidth, displayHeight,
                    displayRotation, uiMode, overscanLeft, overscanRight, overscanBottom, dcf, navVisible, navTranslucent,
                    navAllowedHidden, statusBarExpandedNotKeyguard);
            if (DEBUG_LAYOUT) Slog.i(TAG, String.format("mDock rect: (%d,%d - %d,%d)",
                    mDockLeft, mDockTop, mDockRight, mDockBottom));
            updateSysUiVisibility |= layoutStatusBar(pf, df, of, vf, dcf, sysui, isKeyguardShowing);
            // Nav bar color customized feature. prize-linkh-2017.08.04 @{
            if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
                mInputMethodChildWinBehindNavBar = null;
                // Assign it before invoking updateSystemUiVisibilityLw method;
                mInBeginLayout = true;
            
                mIsNavBarGone = mNavigationBar == null || mNavigationBar.isGoneForLayoutLw();
            } //@}

            if (updateSysUiVisibility) {
                updateSystemUiVisibilityLw();
            }

            // Nav bar color customized feature. prize-linkh-2017.09.01 @{
            if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
                mInBeginLayout = false;
            } //@}            
        }
    }

    private boolean layoutStatusBar(Rect pf, Rect df, Rect of, Rect vf, Rect dcf, int sysui,
            boolean isKeyguardShowing) {
        // decide where the status bar goes ahead of time
        if (mStatusBar != null) {
            // apply any navigation bar insets
            pf.left = df.left = of.left = mUnrestrictedScreenLeft;
            pf.top = df.top = of.top = mUnrestrictedScreenTop;
            pf.right = df.right = of.right = mUnrestrictedScreenWidth + mUnrestrictedScreenLeft;
            pf.bottom = df.bottom = of.bottom = mUnrestrictedScreenHeight
                    + mUnrestrictedScreenTop;
            vf.left = mStableLeft;
            vf.top = mStableTop;
            vf.right = mStableRight;
            vf.bottom = mStableBottom;

            mStatusBarLayer = mStatusBar.getSurfaceLayer();

            // Let the status bar determine its size.
            mStatusBar.computeFrameLw(pf /* parentFrame */, df /* displayFrame */,
                    vf /* overlayFrame */, vf /* contentFrame */, vf /* visibleFrame */,
                    dcf /* decorFrame */, vf /* stableFrame */, vf /* outsetFrame */);

            // For layout, the status bar is always at the top with our fixed height.
            mStableTop = mUnrestrictedScreenTop + mStatusBarHeight;

            boolean statusBarTransient = (sysui & View.STATUS_BAR_TRANSIENT) != 0;
            boolean statusBarTranslucent = (sysui
                    & (View.STATUS_BAR_TRANSLUCENT | View.STATUS_BAR_TRANSPARENT)) != 0;
            if (!isKeyguardShowing) {
                statusBarTranslucent &= areTranslucentBarsAllowed();
            }

            // If the status bar is hidden, we don't want to cause
            // windows behind it to scroll.
            if (mStatusBar.isVisibleLw() && !statusBarTransient) {
                // Status bar may go away, so the screen area it occupies
                // is available to apps but just covering them when the
                // status bar is visible.
                mDockTop = mUnrestrictedScreenTop + mStatusBarHeight;

                mContentTop = mVoiceContentTop = mCurTop = mDockTop;
                mContentBottom = mVoiceContentBottom = mCurBottom = mDockBottom;
                mContentLeft = mVoiceContentLeft = mCurLeft = mDockLeft;
                mContentRight = mVoiceContentRight = mCurRight = mDockRight;

                if (DEBUG_LAYOUT) Slog.v(TAG, "Status bar: " +
                        String.format(
                                "dock=[%d,%d][%d,%d] content=[%d,%d][%d,%d] cur=[%d,%d][%d,%d]",
                                mDockLeft, mDockTop, mDockRight, mDockBottom,
                                mContentLeft, mContentTop, mContentRight, mContentBottom,
                                mCurLeft, mCurTop, mCurRight, mCurBottom));
            }
            if (mStatusBar.isVisibleLw() && !mStatusBar.isAnimatingLw()
                    && !statusBarTransient && !statusBarTranslucent
                    && !mStatusBarController.wasRecentlyTranslucent()) {
                // If the opaque status bar is currently requested to be visible,
                // and not in the process of animating on or off, then
                // we can tell the app that it is covered by it.
                mSystemTop = mUnrestrictedScreenTop + mStatusBarHeight;
            }
            if (mStatusBarController.checkHiddenLw()) {
                return true;
            }
        }
        return false;
    }

    private boolean layoutNavigationBar(int displayWidth, int displayHeight, int displayRotation,
            int uiMode, int overscanLeft, int overscanRight, int overscanBottom, Rect dcf,
            boolean navVisible, boolean navTranslucent, boolean navAllowedHidden,
            boolean statusBarExpandedNotKeyguard) {
        /* Dynamically hiding nav bar feature. prize-linkh-20150714 */
        //we always set this value. 
        boolean shouldCalNavBar = mNavigationBar != null;
        
        // If it's a mBack device, then we also need this calculation for force-hiding nav bar.
        if(mSupportHidingNavBar || SUPPORT_NAV_BAR_FOR_MBACK_DEVICE) {
            mNavigationBarPosition = navigationBarPosition(displayWidth, displayHeight,
                    displayRotation); 
            if (mLastNavBarPostion != mNavigationBarPosition) {
                mLastNavBarPostion = mNavigationBarPosition;
                mSystemGestures.setNavBarPosition(mNavigationBarPosition);
            }

            // PRIZE BEGIN 
            // ID: 58995 
            // DESCRIPTION: Force hide navigationbar window when alarm boot 
            // AUTHOR: tangzhengrong 
            if(isAlarmBoot()){
              shouldCalNavBar = false;  
            }
            // PRIZE END 
            if (shouldCalNavBar) {
                final boolean isNavBarGone = mNavigationBar.isGoneForLayoutLw();
                final WindowManager.LayoutParams attrs = mNavigationBar.getAttrs();
                if (DEBUG_FOR_HIDING_NAVBAR) {
                    Slog.d(TAG, "layoutNavigationBar() isNavBarGone=" + isNavBarGone);
                    if (attrs.navBarGoneReason != null) {
                        Slog.d(LOG_TAG, "layoutNavigationBar() navBarGoneReason=" + attrs.navBarGoneReason);
                    }
                }
                if(isNavBarGone) {
                    // Solve an issue for dynamically hiding nav bar(bug-19908). prize-linkh-20160808
                    // If keyguard sets Gone for nav bar. then we keep calculating it for avoid 
                    // causing to relayout other windows.
                    if ("keyguard".equals(attrs.navBarGoneReason)) {
                        shouldCalNavBar = true;
                    } else {
                        shouldCalNavBar = false;
                    }
                    
                    if (DEBUG_FOR_HIDING_NAVBAR) {
                        Slog.d(LOG_TAG, "layoutNavigationBar() shouldCalNavBar=" + shouldCalNavBar);
                    }
                }
            }
        }//END....

        if (shouldCalNavBar) {//(mNavigationBar != null) { /* Dynamically hiding nav bar feature. prize-linkh-20150714 */
            boolean transientNavBarShowing = mNavigationBarController.isTransientShowing();
            // Force the navigation bar to its appropriate place and
            // size.  We need to do this directly, instead of relying on
            // it to bubble up from the nav bar, because this needs to
            // change atomically with screen rotations.
            mNavigationBarPosition = navigationBarPosition(displayWidth, displayHeight,
                    displayRotation);
            if (mNavigationBarPosition == NAV_BAR_BOTTOM) {
                // It's a system nav bar or a portrait screen; nav bar goes on bottom.
                int top = displayHeight - overscanBottom
                        - getNavigationBarHeight(displayRotation, uiMode);
                mTmpNavigationFrame.set(0, top, displayWidth, displayHeight - overscanBottom);
                mStableBottom = mStableFullscreenBottom = mTmpNavigationFrame.top;
                if (transientNavBarShowing) {
                    mNavigationBarController.setBarShowingLw(true);
                } else if (navVisible) {
                    /// M: Add condition.
                    if (!mIsAlarmBoot && !mIsShutDown) {
                        mNavigationBarController.setBarShowingLw(true);
                        mDockBottom = mTmpNavigationFrame.top;
                        mRestrictedScreenHeight = mDockBottom - mRestrictedScreenTop;
                        mRestrictedOverscanScreenHeight
                                = mDockBottom - mRestrictedOverscanScreenTop;
                    }
                } else {
                    // We currently want to hide the navigation UI - unless we expanded the status
                    // bar.
                    mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                }
                if (navVisible && !navTranslucent && !navAllowedHidden
                        && !mNavigationBar.isAnimatingLw()
                        && !mNavigationBarController.wasRecentlyTranslucent()) {
                    // If the opaque nav bar is currently requested to be visible,
                    // and not in the process of animating on or off, then
                    // we can tell the app that it is covered by it.
                    mSystemBottom = mTmpNavigationFrame.top;
                }
            } else if (mNavigationBarPosition == NAV_BAR_RIGHT) {
                // Landscape screen; nav bar goes to the right.
                int left = displayWidth - overscanRight
                        - getNavigationBarWidth(displayRotation, uiMode);
                mTmpNavigationFrame.set(left, 0, displayWidth - overscanRight, displayHeight);
                mStableRight = mStableFullscreenRight = mTmpNavigationFrame.left;
                if (transientNavBarShowing) {
                    mNavigationBarController.setBarShowingLw(true);
                } else if (navVisible) {
                    /// M: Add condition.
                    if (!mIsAlarmBoot && !mIsShutDown) {
                        mNavigationBarController.setBarShowingLw(true);
                        mDockRight = mTmpNavigationFrame.left;
                        mRestrictedScreenWidth = mDockRight - mRestrictedScreenLeft;
                        mRestrictedOverscanScreenWidth
                                = mDockRight - mRestrictedOverscanScreenLeft;
                    }
                } else {
                    // We currently want to hide the navigation UI - unless we expanded the status
                    // bar.
                    mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                }
                if (navVisible && !navTranslucent && !navAllowedHidden
                        && !mNavigationBar.isAnimatingLw()
                        && !mNavigationBarController.wasRecentlyTranslucent()) {
                    // If the nav bar is currently requested to be visible,
                    // and not in the process of animating on or off, then
                    // we can tell the app that it is covered by it.
                    mSystemRight = mTmpNavigationFrame.left;
                }
            } else if (mNavigationBarPosition == NAV_BAR_LEFT) {
                // Seascape screen; nav bar goes to the left.
                int right = overscanLeft + getNavigationBarWidth(displayRotation, uiMode);
                mTmpNavigationFrame.set(overscanLeft, 0, right, displayHeight);
                mStableLeft = mStableFullscreenLeft = mTmpNavigationFrame.right;
                if (transientNavBarShowing) {
                    mNavigationBarController.setBarShowingLw(true);
                } else if (navVisible) {
                    /// M: Add condition.
                    if (!mIsAlarmBoot && !mIsShutDown) {
                        mNavigationBarController.setBarShowingLw(true);
                        mDockLeft = mTmpNavigationFrame.right;
                        // TODO: not so sure about those:
                        mRestrictedScreenLeft = mRestrictedOverscanScreenLeft = mDockLeft;
                        mRestrictedScreenWidth = mDockRight - mRestrictedScreenLeft;
                        mRestrictedOverscanScreenWidth = mDockRight - mRestrictedOverscanScreenLeft;
                    }
                } else {
                    // We currently want to hide the navigation UI - unless we expanded the status
                    // bar.
                    mNavigationBarController.setBarShowingLw(statusBarExpandedNotKeyguard);
                }
                if (navVisible && !navTranslucent && !navAllowedHidden
                        && !mNavigationBar.isAnimatingLw()
                        && !mNavigationBarController.wasRecentlyTranslucent()) {
                    // If the nav bar is currently requested to be visible,
                    // and not in the process of animating on or off, then
                    // we can tell the app that it is covered by it.
                    mSystemLeft = mTmpNavigationFrame.right;
                }
            }
            // Make sure the content and current rectangles are updated to
            // account for the restrictions from the navigation bar.
            mContentTop = mVoiceContentTop = mCurTop = mDockTop;
            mContentBottom = mVoiceContentBottom = mCurBottom = mDockBottom;
            mContentLeft = mVoiceContentLeft = mCurLeft = mDockLeft;
            mContentRight = mVoiceContentRight = mCurRight = mDockRight;
            mStatusBarLayer = mNavigationBar.getSurfaceLayer();
            // And compute the final frame.
            mNavigationBar.computeFrameLw(mTmpNavigationFrame, mTmpNavigationFrame,
                    mTmpNavigationFrame, mTmpNavigationFrame, mTmpNavigationFrame, dcf,
                    mTmpNavigationFrame, mTmpNavigationFrame);
            if (DEBUG_LAYOUT) Slog.i(TAG, "mNavigationBar frame: " + mTmpNavigationFrame);
            if (mNavigationBarController.checkHiddenLw()) {
                return true;
            }
        }
        return false;
    }

    private int navigationBarPosition(int displayWidth, int displayHeight, int displayRotation) {
        if (mNavigationBarCanMove && displayWidth > displayHeight) {
            if (displayRotation == Surface.ROTATION_270) {
				/*prize--hold navigationbar at the bottom of screen on heteromorphism project--chenlong--20180417*/
				if(PrizeOption.PRIZE_NOTCH_SCREEN) {
					return NAV_BAR_LEFT;
				} else {
					return NAV_BAR_RIGHT;//prize use the NAV_BAR_LEFT  -liyongli-20171219
				}
            } else {
                return NAV_BAR_RIGHT;
            }
        }
        return NAV_BAR_BOTTOM;
    }

    /** {@inheritDoc} */
    @Override
    public int getSystemDecorLayerLw() {
        if (mStatusBar != null && mStatusBar.isVisibleLw()) {
            return mStatusBar.getSurfaceLayer();
        }

        if (mNavigationBar != null && mNavigationBar.isVisibleLw()) {
            return mNavigationBar.getSurfaceLayer();
        }

        return 0;
    }

    @Override
    public void getContentRectLw(Rect r) {
        r.set(mContentLeft, mContentTop, mContentRight, mContentBottom);
    }

    void setAttachedWindowFrames(WindowState win, int fl, int adjust, WindowState attached,
            boolean insetDecors, Rect pf, Rect df, Rect of, Rect cf, Rect vf) {
        if (win.getSurfaceLayer() > mDockLayer && attached.getSurfaceLayer() < mDockLayer) {
            // Here's a special case: if this attached window is a panel that is
            // above the dock window, and the window it is attached to is below
            // the dock window, then the frames we computed for the window it is
            // attached to can not be used because the dock is effectively part
            // of the underlying window and the attached window is floating on top
            // of the whole thing.  So, we ignore the attached window and explicitly
            // compute the frames that would be appropriate without the dock.
            df.left = of.left = cf.left = vf.left = mDockLeft;
            df.top = of.top = cf.top = vf.top = mDockTop;
            df.right = of.right = cf.right = vf.right = mDockRight;
            df.bottom = of.bottom = cf.bottom = vf.bottom = mDockBottom;
        } else {
            // The effective display frame of the attached window depends on
            // whether it is taking care of insetting its content.  If not,
            // we need to use the parent's content frame so that the entire
            // window is positioned within that content.  Otherwise we can use
            // the overscan frame and let the attached window take care of
            // positioning its content appropriately.
            if (adjust != SOFT_INPUT_ADJUST_RESIZE) {
                // Set the content frame of the attached window to the parent's decor frame
                // (same as content frame when IME isn't present) if specifically requested by
                // setting {@link WindowManager.LayoutParams#FLAG_LAYOUT_ATTACHED_IN_DECOR} flag.
                // Otherwise, use the overscan frame.
                cf.set((fl & FLAG_LAYOUT_ATTACHED_IN_DECOR) != 0
                        ? attached.getContentFrameLw() : attached.getOverscanFrameLw());
            } else {
                // If the window is resizing, then we want to base the content
                // frame on our attached content frame to resize...  however,
                // things can be tricky if the attached window is NOT in resize
                // mode, in which case its content frame will be larger.
                // Ungh.  So to deal with that, make sure the content frame
                // we end up using is not covering the IM dock.
                cf.set(attached.getContentFrameLw());
                if (attached.isVoiceInteraction()) {
                    if (cf.left < mVoiceContentLeft) cf.left = mVoiceContentLeft;
                    if (cf.top < mVoiceContentTop) cf.top = mVoiceContentTop;
                    if (cf.right > mVoiceContentRight) cf.right = mVoiceContentRight;
                    if (cf.bottom > mVoiceContentBottom) cf.bottom = mVoiceContentBottom;
                } else if (attached.getSurfaceLayer() < mDockLayer) {
                    if (cf.left < mContentLeft) cf.left = mContentLeft;
                    if (cf.top < mContentTop) cf.top = mContentTop;
                    if (cf.right > mContentRight) cf.right = mContentRight;
                    if (cf.bottom > mContentBottom) cf.bottom = mContentBottom;
                }
            }
            df.set(insetDecors ? attached.getDisplayFrameLw() : cf);
            of.set(insetDecors ? attached.getOverscanFrameLw() : cf);
            vf.set(attached.getVisibleFrameLw());
        }
        // The LAYOUT_IN_SCREEN flag is used to determine whether the attached
        // window should be positioned relative to its parent or the entire
        // screen.
        pf.set((fl & FLAG_LAYOUT_IN_SCREEN) == 0
                ? attached.getFrameLw() : df);
    }

    private void applyStableConstraints(int sysui, int fl, Rect r) {
        if ((sysui & View.SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0) {
            // If app is requesting a stable layout, don't let the
            // content insets go below the stable values.
            if ((fl & FLAG_FULLSCREEN) != 0) {
                if (r.left < mStableFullscreenLeft) r.left = mStableFullscreenLeft;
                if (r.top < mStableFullscreenTop) r.top = mStableFullscreenTop;
                if (r.right > mStableFullscreenRight) r.right = mStableFullscreenRight;
                if (r.bottom > mStableFullscreenBottom) r.bottom = mStableFullscreenBottom;
            } else {
                if (r.left < mStableLeft) r.left = mStableLeft;
                if (r.top < mStableTop) r.top = mStableTop;
                if (r.right > mStableRight) r.right = mStableRight;
                if (r.bottom > mStableBottom) r.bottom = mStableBottom;
            }
        }
    }

    private boolean canReceiveInput(WindowState win) {
        boolean notFocusable =
                (win.getAttrs().flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != 0;
        boolean altFocusableIm =
                (win.getAttrs().flags & WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM) != 0;
        boolean notFocusableForIm = notFocusable ^ altFocusableIm;
        return !notFocusableForIm;
    }

    /** {@inheritDoc} */
    @Override
    public void layoutWindowLw(WindowState win, WindowState attached) {
        // We've already done the navigation bar and status bar. If the status bar can receive
        // input, we need to layout it again to accomodate for the IME window.
        if ((win == mStatusBar && !canReceiveInput(win)) || win == mNavigationBar) {
            return;
        }
        final WindowManager.LayoutParams attrs = win.getAttrs();
        final boolean isDefaultDisplay = win.isDefaultDisplay();
        final boolean needsToOffsetInputMethodTarget = isDefaultDisplay &&
                (win == mLastInputMethodTargetWindow && mLastInputMethodWindow != null);
        if (needsToOffsetInputMethodTarget) {
            if (DEBUG_LAYOUT) Slog.i(TAG, "Offset ime target window by the last ime window state");
            offsetInputMethodWindowLw(mLastInputMethodWindow);
        }

        /* prize-add-split screen, not use FullScreen -liyongli-20171024-start */
        //final int fl = PolicyControl.getWindowFlags(win, attrs);   // prize liyongli -- remove final --20171024
        //final int sysUiFl = PolicyControl.getSystemUiVisibility(win, null);   // prize liyongli -- remove final --20171024
        
// liyongli use bellow , replace  the line above (original code)
        int prize_fl = PolicyControl.getWindowFlags(win, attrs);
        int prize_sysUiFl = PolicyControl.getSystemUiVisibility(win, null);
        if( PrizeOption.PRIZE_SPLIT_SCREEN_STATUBAR_USE_DOCKEDWIN
             && mWindowManagerInternal.isStackVisible(DOCKED_STACK_ID) 
             //&& win == mTopDockedOpaqueWindowState
             ){
                //Slog.d(TAG, "fulllyl  --- remove FULLSCREEN  " +win);
                prize_fl &= ~(FLAG_FULLSCREEN);
                prize_sysUiFl &= ~( View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        final int fl = prize_fl;
        final int sysUiFl = prize_sysUiFl;
        /* prize-add-split screen-liyongli-20171024-end */
        
        final int pfl = attrs.privateFlags;
        final int sim = attrs.softInputMode;

        final Rect pf = mTmpParentFrame;
        final Rect df = mTmpDisplayFrame;
        final Rect of = mTmpOverscanFrame;
        final Rect cf = mTmpContentFrame;
        final Rect vf = mTmpVisibleFrame;
        final Rect dcf = mTmpDecorFrame;
        final Rect sf = mTmpStableFrame;
        Rect osf = null;
        dcf.setEmpty();

        /// M: add for fullscreen switch feature @{
        if (mSupportFullscreenSwitch) {
            applyFullScreenSwitch(win);
        }
        /// @}

        final boolean hasNavBar = (isDefaultDisplay && mHasNavigationBar
                && mNavigationBar != null && mNavigationBar.isVisibleLw());

        final int adjust = sim & SOFT_INPUT_MASK_ADJUST;

        if (isDefaultDisplay) {
            sf.set(mStableLeft, mStableTop, mStableRight, mStableBottom);
        } else {
            sf.set(mOverscanLeft, mOverscanTop, mOverscanRight, mOverscanBottom);
        }

        if (!isDefaultDisplay) {
            if (attached != null) {
                // If this window is attached to another, our display
                // frame is the same as the one we are attached to.
                setAttachedWindowFrames(win, fl, adjust, attached, true, pf, df, of, cf, vf);
            } else {
                // Give the window full screen.
                pf.left = df.left = of.left = cf.left = mOverscanScreenLeft;
                pf.top = df.top = of.top = cf.top = mOverscanScreenTop;
                pf.right = df.right = of.right = cf.right
                        = mOverscanScreenLeft + mOverscanScreenWidth;
                pf.bottom = df.bottom = of.bottom = cf.bottom
                        = mOverscanScreenTop + mOverscanScreenHeight;
            }
        } else if (attrs.type == TYPE_INPUT_METHOD) {
            pf.left = df.left = of.left = cf.left = vf.left = mDockLeft;
            pf.top = df.top = of.top = cf.top = vf.top = mDockTop;
            pf.right = df.right = of.right = cf.right = vf.right = mDockRight;
            // IM dock windows layout below the nav bar...
            pf.bottom = df.bottom = of.bottom = mUnrestrictedScreenTop + mUnrestrictedScreenHeight;
            // ...with content insets above the nav bar
            cf.bottom = vf.bottom = mStableBottom;
            if (mStatusBar != null && mFocusedWindow == mStatusBar && canReceiveInput(mStatusBar)) {
                // The status bar forces the navigation bar while it's visible. Make sure the IME
                // avoids the navigation bar in that case.
                if (mNavigationBarPosition == NAV_BAR_RIGHT) {
                    pf.right = df.right = of.right = cf.right = vf.right = mStableRight;
                } else if (mNavigationBarPosition == NAV_BAR_LEFT) {
                    pf.left = df.left = of.left = cf.left = vf.left = mStableLeft;
                }
            }
            // IM dock windows always go to the bottom of the screen.
            attrs.gravity = Gravity.BOTTOM;
            mDockLayer = win.getSurfaceLayer();
        } else if (attrs.type == TYPE_VOICE_INTERACTION) {
            pf.left = df.left = of.left = mUnrestrictedScreenLeft;
            pf.top = df.top = of.top = mUnrestrictedScreenTop;
            pf.right = df.right = of.right = mUnrestrictedScreenLeft + mUnrestrictedScreenWidth;
            pf.bottom = df.bottom = of.bottom = mUnrestrictedScreenTop + mUnrestrictedScreenHeight;
            if (adjust != SOFT_INPUT_ADJUST_RESIZE) {
                cf.left = mDockLeft;
                cf.top = mDockTop;
                cf.right = mDockRight;
                cf.bottom = mDockBottom;
            } else {
                cf.left = mContentLeft;
                cf.top = mContentTop;
                cf.right = mContentRight;
                cf.bottom = mContentBottom;
            }
            if (adjust != SOFT_INPUT_ADJUST_NOTHING) {
                vf.left = mCurLeft;
                vf.top = mCurTop;
                vf.right = mCurRight;
                vf.bottom = mCurBottom;
            } else {
                vf.set(cf);
            }
        } else if (attrs.type == TYPE_WALLPAPER) {
           layoutWallpaper(win, pf, df, of, cf);
        } else if (win == mStatusBar) {
            pf.left = df.left = of.left = mUnrestrictedScreenLeft;
            pf.top = df.top = of.top = mUnrestrictedScreenTop;
            pf.right = df.right = of.right = mUnrestrictedScreenWidth + mUnrestrictedScreenLeft;
            pf.bottom = df.bottom = of.bottom = mUnrestrictedScreenHeight + mUnrestrictedScreenTop;
            cf.left = vf.left = mStableLeft;
            cf.top = vf.top = mStableTop;
            cf.right = vf.right = mStableRight;
            vf.bottom = mStableBottom;

            if (adjust == SOFT_INPUT_ADJUST_RESIZE) {
                cf.bottom = mContentBottom;
            } else {
                cf.bottom = mDockBottom;
                vf.bottom = mContentBottom;
            }
        } else {
            boolean prizeNeedsmall = false; // add for layout bellow ears, liyongli -2018-06-26

            // Default policy decor for the default display
            dcf.left = mSystemLeft;
            dcf.top = mSystemTop;
            dcf.right = mSystemRight;
            dcf.bottom = mSystemBottom;
            final boolean inheritTranslucentDecor = (attrs.privateFlags
                    & WindowManager.LayoutParams.PRIVATE_FLAG_INHERIT_TRANSLUCENT_DECOR) != 0;
            final boolean isAppWindow =
                    attrs.type >= WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW &&
                    attrs.type <= WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
            final boolean topAtRest =
                    win == mTopFullscreenOpaqueWindowState && !win.isAnimatingLw();
            if (isAppWindow && !inheritTranslucentDecor && !topAtRest) {
                if ((sysUiFl & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0
                        && (fl & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0
                        && (fl & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) == 0
                        && (fl & WindowManager.LayoutParams.
                                FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) == 0
                        && (pfl & PRIVATE_FLAG_FORCE_DRAW_STATUS_BAR_BACKGROUND) == 0) {
                    // Ensure policy decor includes status bar
                    dcf.top = mStableTop;
                }
                if ((fl & WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) == 0
                        && (sysUiFl & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0
                        && (fl & WindowManager.LayoutParams.
                                FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) == 0) {
                    // Ensure policy decor includes navigation bar
                    dcf.bottom = mStableBottom;
                    dcf.right = mStableRight;
                }
            }
            
            if ((fl & (FLAG_LAYOUT_IN_SCREEN | FLAG_LAYOUT_INSET_DECOR))
                    == (FLAG_LAYOUT_IN_SCREEN | FLAG_LAYOUT_INSET_DECOR)) {
                /// M: Add more log at WMS
                if (DEBUG_LAYOUT) Slog.v(TAG, "layoutWindowLw(" + attrs.getTitle()
                            + "): IN_SCREEN, INSET_DECOR, sim=#" + Integer.toHexString(adjust)
                            + ", type=" + attrs.type
                            + ", flag=" + fl
                            + ", canHideNavigationBar=" + canHideNavigationBar()
                            + ", sysUiFl=" + sysUiFl);
                // This is the case for a normal activity window: we want it
                // to cover all of the screen space, and it can take care of
                // moving its contents to account for screen decorations that
                // intrude into that space.
                if (attached != null) {
                    // If this window is attached to another, our display
                    // frame is the same as the one we are attached to.
                    setAttachedWindowFrames(win, fl, adjust, attached, true, pf, df, of, cf, vf);
                } else {
                    if (attrs.type == TYPE_STATUS_BAR_PANEL
                            || attrs.type == TYPE_STATUS_BAR_SUB_PANEL) {
                        // Status bar panels are the only windows who can go on top of
                        // the status bar.  They are protected by the STATUS_BAR_SERVICE
                        // permission, so they have the same privileges as the status
                        // bar itself.
                        //
                        // However, they should still dodge the navigation bar if it exists.

                        pf.left = df.left = of.left = hasNavBar
                                ? mDockLeft : mUnrestrictedScreenLeft;
                        pf.top = df.top = of.top = mUnrestrictedScreenTop;
                        pf.right = df.right = of.right = hasNavBar
                                ? mRestrictedScreenLeft+mRestrictedScreenWidth
                                : mUnrestrictedScreenLeft + mUnrestrictedScreenWidth;
                        pf.bottom = df.bottom = of.bottom = hasNavBar
                                ? mRestrictedScreenTop+mRestrictedScreenHeight
                                : mUnrestrictedScreenTop + mUnrestrictedScreenHeight;

                        if (DEBUG_LAYOUT) Slog.v(TAG, String.format(
                                        "Laying out status bar window: (%d,%d - %d,%d)",
                                        pf.left, pf.top, pf.right, pf.bottom));
                    } else if ((fl & FLAG_LAYOUT_IN_OVERSCAN) != 0
                            && attrs.type >= WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW
                            && attrs.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW) {
                        // Asking to layout into the overscan region, so give it that pure
                        // unrestricted area.
                        pf.left = df.left = of.left = mOverscanScreenLeft;
                        pf.top = df.top = of.top = mOverscanScreenTop;
                        pf.right = df.right = of.right = mOverscanScreenLeft + mOverscanScreenWidth;
                        pf.bottom = df.bottom = of.bottom = mOverscanScreenTop
                                + mOverscanScreenHeight;
                    } else if (canHideNavigationBar()
                            && (sysUiFl & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0
                            /// M:[ALPS01186390]Fix IPO flash issue
                            && (attrs.type == TYPE_TOP_MOST || (
                               attrs.type >= WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW
                            && attrs.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW))) {
                        // Asking for layout as if the nav bar is hidden, lets the
                        // application extend into the unrestricted overscan screen area.  We
                        // only do this for application windows to ensure no window that
                        // can be above the nav bar can do this.
                        pf.left = df.left = mOverscanScreenLeft;
                        pf.top = df.top = mOverscanScreenTop;
                        pf.right = df.right = mOverscanScreenLeft + mOverscanScreenWidth;
                        pf.bottom = df.bottom = mOverscanScreenTop + mOverscanScreenHeight;
                        // We need to tell the app about where the frame inside the overscan
                        // is, so it can inset its content by that amount -- it didn't ask
                        // to actually extend itself into the overscan region.
                        of.left = mUnrestrictedScreenLeft;
                        of.top = mUnrestrictedScreenTop;
                        of.right = mUnrestrictedScreenLeft + mUnrestrictedScreenWidth;
                        of.bottom = mUnrestrictedScreenTop + mUnrestrictedScreenHeight;
                    } else {
                        pf.left = df.left = mRestrictedOverscanScreenLeft;
                        pf.top = df.top = mRestrictedOverscanScreenTop;
                        pf.right = df.right = mRestrictedOverscanScreenLeft
                                + mRestrictedOverscanScreenWidth;
                        pf.bottom = df.bottom = mRestrictedOverscanScreenTop
                                + mRestrictedOverscanScreenHeight;
                        // We need to tell the app about where the frame inside the overscan
                        // is, so it can inset its content by that amount -- it didn't ask
                        // to actually extend itself into the overscan region.
                        of.left = mUnrestrictedScreenLeft;
                        of.top = mUnrestrictedScreenTop;
                        of.right = mUnrestrictedScreenLeft + mUnrestrictedScreenWidth;
                        of.bottom = mUnrestrictedScreenTop + mUnrestrictedScreenHeight;
                    }

                    /* prize-add- 0 180 rotation, fullscreen app,  cut lcd ears-liyongli-20180509-start */                 
                    if(PrizeOption.PRIZE_NOTCH_SCREEN 
                         //&& (mFocusedWindow==win)   //use it Cause bug MeiTu  -->FaXian-->pen down FaBu, new page show up to down ANIM                    
                         //&& win.isVisibleLw() //use it Cause bug MeiTuXiuXiu -->pull left to FaXian--> pen down FaBu--> Return, StatuBar Rect  Black
                         && !win.prizeAnimatingExit()  //use it replace up two lines
                         && isAppWindow 
                         && attrs.type != TYPE_APPLICATION_STARTING //start
                         && (attrs.applicationInfoFlags & ApplicationInfo.FLAG_SYSTEM) == 0 //not system app
                         && (mDisplayRotation == Surface.ROTATION_0 || mDisplayRotation == Surface.ROTATION_180)
                     ){
                        boolean statusBarTransient = (sysUiFl & View.STATUS_BAR_TRANSIENT) != 0;
                        if(DEBUG_NOTSCREEN)Slog.v(TAG, " lyl statusBarTransient:"+statusBarTransient);
                        if( mStatusBar.isVisibleLw() ){
                            if( mStatusBarController.isTransientShowing() ){  //if((fl & FLAG_FULLSCREEN)!=0){
                                if(DEBUG_NOTSCREEN)Slog.v(TAG, " lyl -1- visble  isTransientShowing   prizeNeedsmall = true");//Slog.v(TAG, " lyl -1- visble && FLAG_FULLSCREEN" );
                                prizeNeedsmall = true; // fullscreen book,  on statubar rect pull form top to bottom, show the statubar
                            }else if((fl & FLAG_FULLSCREEN)!=0){
                                if(DEBUG_NOTSCREEN)Slog.v(TAG, " lyl -1- visble && FLAG_FULLSCREEN" );
                                prizeNeedsmall = true; //when has StatuBar view Open fullscreen book cut ears, 
                                                                               // if here not set true, cause first show y=0 then show y=87. 
                            }
                        }else if( mStatusBar.isAnimatingLw() || statusBarTransient ){
                            prizeNeedsmall = false;
                        }else{//statubar not show
                            if(DEBUG_NOTSCREEN)Slog.v(TAG, " lyl -2- not visble " + (win == mTopFullscreenOpaqueWindowState) );
                            prizeNeedsmall = true;
                            //removed by lihuangyuan-2018-08-02,cause add cutears,this problem not exist (e.g. shaoniansanguozhi show from statusbar)
                            /*if(win != mTopFullscreenOpaqueWindowState){
                                prizeNeedsmall = false;//from recent, StatusBar from transparent to black. e.g. com.lightsky.video
                           }*/  
                           //emoved end
                        }

                        if( (sysUiFl & View.SYSTEM_UI_FLAG_IMMERSIVE)!= 0 
                           // ||(sysUiFl & View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)!= 0  //cause KuaiKanXiaoShuo error Cut LiuHai
                        ){
                            if(DEBUG_NOTSCREEN)Slog.v(TAG, " lyl -3- IMMERSIVE" ); //sysUiVis & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            prizeNeedsmall = true; 
                        }
                        if( (sysUiFl & View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)!= 0  // KuaiKanXiaoShuo set IMMERSIVE_STICKY and Show statusBar, so no need cut ears
                              && !mStatusBar.isVisibleLw() //HuangShiZhanZheng  set IMMERSIVE_STICKY and hidden StatusBar, so need cut ears
                        ){
                            if(DEBUG_NOTSCREEN)Slog.v(TAG, " lyl -3-2-- SYSTEM_UI_FLAG_IMMERSIVE_STICKY  " 
                                            +mStatusBar.isVisibleLw()+" "+mStatusBar.isAnimatingLw()+" "+statusBarTransient
                                            +" fl="+Integer.toHexString(fl)
                                            +" sysUiFl="+Integer.toHexString(sysUiFl)
                                        );
                            prizeNeedsmall = true; 
                        }                        
                        
                        String str =  attrs.getTitle()+""; //power on, first run, str=[Starting com.android.mms]                        
                        if( prizeNeedsmall  )
                        {
                            //Slog.v(TAG, "-- lyltitle --  "+ str );
                            if( str.equals("com.mt.mtxx.mtxx/com.meitu.mtxx.MainActivity") //MeitTuXiuXiu  the main page has been layout in notch lcd
                                 || str.equals("com.mt.mtxx.mtxx/com.mt.mtxx.mtxx.TopViewActivity") //MeiTuXiuXiu Ad
                                 || str.equals("com.youdao.dict/com.youdao.dict.activity.OCRFromCameraActivity")//fix 58340 pen down  position not match the text 
                                                                                                                    //it use the [com.oppo.feature.screen.heteromorphism], so no need cutears
                                 || str.equals("com.tencent.mobileqq/com.tencent.biz.pubaccount.readinjoy.video.VideoFeedsPlayActivity")
                                                         //QQ-->KanDian-->video play not in mid, it not change 90, only draw change.
                                 || str.startsWith("com.happyelements.AndroidAnimal")//KaiXinXiaoXiaoLe
                                 || str.equals("com.tencent.mm/com.tencent.mm.plugin.voip.ui.VideoActivity")
                                 || str.equals("com.tencent.mobileqq/com.tencent.av.ui.AVActivity")                                 
                                 || str.startsWith("android.view.cts")
/*                                 ||str.startsWith("my.beautyCamera") //MeiRenXiangJi, no method fix , so the app not cut Lcd ears
                                 || str.startsWith("com.ss.android.ugc.live") //HuoShanXiaoShiPin

                                 || str.equals("com.lightsky.video/.LauncherActivity") //KuaiShiPin
                                 //|| str.equals("com.qq.ac.android/com.qq.ac.android.view.activity.RollPaperReadingActivity") //TengXunDongMan
                                 || str.contains("LogoActivity") // com.aikan/com.dzbook.activity.LogoActivity   MianFeiXiaoShuoDaQuan 
                                 || str.contains("Splash") 
                                 || str.contains("Welcome") 
                                 || str.contains("AdActivity")  */
                            ){
                                //Slog.v(TAG, " lyl -4- ignor activity  "+str );
                                prizeNeedsmall = false; 
                            }
                        }                        
                        if(DEBUG_NOTSCREEN)Slog.v(TAG, "prizeNeedsmall:"+prizeNeedsmall+" -- lyltitle --  "+ str );                        
                        //Slog.v(TAG, "-- lyl-  0x"+ Integer.toHexString(sysUiFl)
                                         //+" LIGHT_STATUS_BAR="+Integer.toHexString((sysUiFl & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR))
                                         //+" SYSTEM_UI_FLAG_LAYOUT_STABLE="+Integer.toHexString((sysUiFl & View.SYSTEM_UI_FLAG_LAYOUT_STABLE))+" "
                                         //+" FLAG_FULLSCREEN="+Integer.toHexString((fl & FLAG_FULLSCREEN))
                                         //+"  "+mStatusBar.isVisibleLw()
                                         //+" fl="+Integer.toHexString(fl)
                                         //+" "+prizeNeedsmall+"  "+ attrs.getTitle()) ;

                        int cutHight = mStatusBarHeight - (mStatusBarHeight&0x01);//DiTiePaoKu--com.kiloo.subwaysurf
                        if( !prizeNeedsmall && mDisplayRotation == Surface.ROTATION_0
                             && coverEarsWinAttrs!=null && coverEarsWinAttrs.y == 0 
                             &&  win.isVisibleLw() && win.isDisplayedLw() && win.isDrawnLw()
                             && ( mStatusBar.isGoneForLayoutLw()  //caseA.. No StatusBar
                                        || (mStatusBar.isDisplayedLw() && !mStatusBar.isAnimatingLw()  ) //case B. has StatusBar
                                     )
                               //&& mStatusBar.isDisplayedLw() //use it Cause bug,  CoverEars all show
                        ){
                                        coverEarsWinAttrs.y = -100;
                                        //prizeCoverEarsWin.computeFrameLw(pf, df, of, cf, vf, dcf, sf, osf); //no need it
                                        if(DEBUG_NOTSCREEN)Slog.v(TAG, " lyl  coverear  -100     " + win.isVisibleLw()  + " "+win.isDisplayedLw()+ " "+win.isGoneForLayoutLw()+" "+win.isDrawnLw()
                                                        +"  "+ win.isAnimatingLw()
                                                        + "  ---  "+mStatusBar.isDisplayedLw()+ " "+mStatusBar.isGoneForLayoutLw()+" "+mStatusBar.isDrawnLw() 
                                                        +"  "+ mStatusBar.isAnimatingLw()
                                                      );
                        }
                        if( prizeNeedsmall  )
                        {
                                if(mDisplayRotation == Surface.ROTATION_0){
                                    if(coverEarsWinAttrs != null && coverEarsWinAttrs.y == -100 ){
                                        String pkgFocus = mFocusedWindow.getOwningPackage()+"";
                                        String pkgCur = win.getOwningPackage()+"";
                                        //if( mFocusedWindow==win ){//fix  ShenMiaoTaoWang push HOME, desktop error show cover; But cuase bug 64591
                                        if( pkgFocus.equals(pkgCur) ){//fix  ShenMiaoTaoWang push HOME, desktop error show cover, and fix 64591
                                            coverEarsWinAttrs.y = 0;
                                            prizeCoverEarsWin.computeFrameLw(pf, df, of, cf, vf, dcf, sf, osf);
                                        }
                                        if(DEBUG_NOTSCREEN)Slog.v(TAG, "  lyl  coverear  y=0       " + (mFocusedWindow==win));
                                        if( coverEarsWinAttrs.y!=0 ) Slog.v(TAG, "  lyl  coverear  y!=0       " + mFocusedWindow + " "+ win );
                                        //lyl  ear  y=0       false     -------------    ShenMiaoTaoWang push HOME, desktop error show cover
                                    }
                                    pf.top += cutHight;
                                    of.top += cutHight;
                                    df.top += cutHight; //ZheShangYinHang the bottom icon show half
                                }else if(mDisplayRotation == Surface.ROTATION_180){
                                    pf.bottom -= cutHight;
                                    of.bottom -= cutHight;
                                }
                                
                                if(mDisplayRotation == Surface.ROTATION_0){
                                    attrs.prizeWinMoveEarlcdBottom = 0;//1;  K6206 not show the statusBar
                                    //Slog.v(TAG, "-------- lyl6------ set  1  "+ attrs.getTitle()) ;
                                }else{
                                    attrs.prizeWinMoveEarlcdBottom = 0;
                                }
                                //Slog.v(TAG, "-- lyl +++  "+ Integer.toHexString(sysUiFl)+" "+ attrs.getTitle()) ;
                        }//end if( prizeNeedsmall  )
                    }
                    /* prize-add- 0 180 rotation, fullscreen app,  cut lcd ears-liyongli-20180509-end */
                    
                                        
                    if ((fl & FLAG_FULLSCREEN) == 0) {
                        if (win.isVoiceInteraction()) {
                            cf.left = mVoiceContentLeft;
                            cf.top = mVoiceContentTop;
                            cf.right = mVoiceContentRight;
                            cf.bottom = mVoiceContentBottom;
                        } else {
                            if (adjust != SOFT_INPUT_ADJUST_RESIZE) {
                                cf.left = mDockLeft;
                                cf.top = mDockTop;
                                cf.right = mDockRight;
                                cf.bottom = mDockBottom;
                            } else {
                                cf.left = mContentLeft;
                                cf.top = mContentTop;
                                cf.right = mContentRight;
                                cf.bottom = mContentBottom;
                            }
                        }
                    } else {
                        // Full screen windows are always given a layout that is as if the
                        // status bar and other transient decors are gone.  This is to avoid
                        // bad states when moving from a window that is not hding the
                        // status bar to one that is.
                        cf.left = mRestrictedScreenLeft;
                        cf.top = mRestrictedScreenTop;
                        cf.right = mRestrictedScreenLeft + mRestrictedScreenWidth;
                        cf.bottom = mRestrictedScreenTop + mRestrictedScreenHeight;
                    }
                    applyStableConstraints(sysUiFl, fl, cf);
                    if (adjust != SOFT_INPUT_ADJUST_NOTHING) {
                        vf.left = mCurLeft;
                        vf.top = mCurTop;
                        vf.right = mCurRight;
                        vf.bottom = mCurBottom;
                    } else {
                        vf.set(cf);
                    }
                }
            } else if ((fl & FLAG_LAYOUT_IN_SCREEN) != 0 || (sysUiFl
                    & (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)) != 0) {
                if (DEBUG_LAYOUT) Slog.v(TAG, "layoutWindowLw(" + attrs.getTitle() +
                            "): IN_SCREEN, type=" + attrs.type +
                            ", flag=" + fl +
                            ", canHideNavigationBar=" + canHideNavigationBar() +
                            ", sysUiFl=" + sysUiFl);
                // A window that has requested to fill the entire screen just
                // gets everything, period.
                if (attrs.type == TYPE_STATUS_BAR_PANEL
                        || attrs.type == TYPE_STATUS_BAR_SUB_PANEL
                        || attrs.type == TYPE_VOLUME_OVERLAY) {
                    pf.left = df.left = of.left = cf.left = hasNavBar
                            ? mDockLeft : mUnrestrictedScreenLeft;
                    pf.top = df.top = of.top = cf.top = mUnrestrictedScreenTop;
                    pf.right = df.right = of.right = cf.right = hasNavBar
                                        ? mRestrictedScreenLeft+mRestrictedScreenWidth
                                        : mUnrestrictedScreenLeft + mUnrestrictedScreenWidth;
                    pf.bottom = df.bottom = of.bottom = cf.bottom = hasNavBar
                                          ? mRestrictedScreenTop+mRestrictedScreenHeight
                                          : mUnrestrictedScreenTop + mUnrestrictedScreenHeight;
                    if (DEBUG_LAYOUT) Slog.v(TAG, String.format(
                                    "Laying out IN_SCREEN status bar window: (%d,%d - %d,%d)",
                                    pf.left, pf.top, pf.right, pf.bottom));
                } else if (attrs.type == TYPE_NAVIGATION_BAR
                        || attrs.type == TYPE_NAVIGATION_BAR_PANEL) {
                    // The navigation bar has Real Ultimate Power.
                    pf.left = df.left = of.left = mUnrestrictedScreenLeft;
                    pf.top = df.top = of.top = mUnrestrictedScreenTop;
                    pf.right = df.right = of.right = mUnrestrictedScreenLeft
                            + mUnrestrictedScreenWidth;
                    pf.bottom = df.bottom = of.bottom = mUnrestrictedScreenTop
                            + mUnrestrictedScreenHeight;
                    if (DEBUG_LAYOUT) Slog.v(TAG, String.format(
                                    "Laying out navigation bar window: (%d,%d - %d,%d)",
                                    pf.left, pf.top, pf.right, pf.bottom));
                } else if ((attrs.type == TYPE_SECURE_SYSTEM_OVERLAY
                                || attrs.type == TYPE_BOOT_PROGRESS
                                || attrs.type == TYPE_SCREENSHOT)
                        && ((fl & FLAG_FULLSCREEN) != 0)) {
                    // Fullscreen secure system overlays get what they ask for. Screenshot region
                    // selection overlay should also expand to full screen.
                    pf.left = df.left = of.left = cf.left = mOverscanScreenLeft;
                    pf.top = df.top = of.top = cf.top = mOverscanScreenTop;
                    pf.right = df.right = of.right = cf.right = mOverscanScreenLeft
                            + mOverscanScreenWidth;
                    pf.bottom = df.bottom = of.bottom = cf.bottom = mOverscanScreenTop
                            + mOverscanScreenHeight;
                } else if (attrs.type == TYPE_BOOT_PROGRESS) {
                    // Boot progress screen always covers entire display.
                    pf.left = df.left = of.left = cf.left = mOverscanScreenLeft;
                    pf.top = df.top = of.top = cf.top = mOverscanScreenTop;
                    pf.right = df.right = of.right = cf.right = mOverscanScreenLeft
                            + mOverscanScreenWidth;
                    pf.bottom = df.bottom = of.bottom = cf.bottom = mOverscanScreenTop
                            + mOverscanScreenHeight;
                } else if ((fl & FLAG_LAYOUT_IN_OVERSCAN) != 0
                        && attrs.type >= WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW
                        && attrs.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW) {
                    // Asking to layout into the overscan region, so give it that pure
                    // unrestricted area.
                    pf.left = df.left = of.left = cf.left = mOverscanScreenLeft;
                    pf.top = df.top = of.top = cf.top = mOverscanScreenTop;
                    pf.right = df.right = of.right = cf.right
                            = mOverscanScreenLeft + mOverscanScreenWidth;
                    pf.bottom = df.bottom = of.bottom = cf.bottom
                            = mOverscanScreenTop + mOverscanScreenHeight;
                } else if (canHideNavigationBar()
                        && (sysUiFl & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0
                        && (attrs.type == TYPE_STATUS_BAR
                            || attrs.type == TYPE_TOAST
                            || attrs.type == TYPE_DOCK_DIVIDER
                            || attrs.type == TYPE_VOICE_INTERACTION_STARTING
                            || (attrs.type >= WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW
                            && attrs.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW))) {
                    // Asking for layout as if the nav bar is hidden, lets the
                    // application extend into the unrestricted screen area.  We
                    // only do this for application windows (or toasts) to ensure no window that
                    // can be above the nav bar can do this.
                    // XXX This assumes that an app asking for this will also
                    // ask for layout in only content.  We can't currently figure out
                    // what the screen would be if only laying out to hide the nav bar.
                    pf.left = df.left = of.left = cf.left = mUnrestrictedScreenLeft;
                    pf.top = df.top = of.top = cf.top = mUnrestrictedScreenTop;
                    pf.right = df.right = of.right = cf.right = mUnrestrictedScreenLeft
                            + mUnrestrictedScreenWidth;
                    pf.bottom = df.bottom = of.bottom = cf.bottom = mUnrestrictedScreenTop
                            + mUnrestrictedScreenHeight;
                } else if ((sysUiFl & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) != 0) {
                    pf.left = df.left = of.left = mRestrictedScreenLeft;
                    pf.top = df.top = of.top  = mRestrictedScreenTop;
                    pf.right = df.right = of.right = mRestrictedScreenLeft + mRestrictedScreenWidth;
                    pf.bottom = df.bottom = of.bottom = mRestrictedScreenTop
                            + mRestrictedScreenHeight;
                    if (adjust != SOFT_INPUT_ADJUST_RESIZE) {
                        cf.left = mDockLeft;
                        cf.top = mDockTop;
                        cf.right = mDockRight;
                        cf.bottom = mDockBottom;
                    } else {
                        cf.left = mContentLeft;
                        cf.top = mContentTop;
                        cf.right = mContentRight;
                        cf.bottom = mContentBottom;
                    }
                } else {
                    pf.left = df.left = of.left = cf.left = mRestrictedScreenLeft;
                    pf.top = df.top = of.top = cf.top = mRestrictedScreenTop;
                    pf.right = df.right = of.right = cf.right = mRestrictedScreenLeft
                            + mRestrictedScreenWidth;
                    pf.bottom = df.bottom = of.bottom = cf.bottom = mRestrictedScreenTop
                            + mRestrictedScreenHeight;
                }

                applyStableConstraints(sysUiFl, fl, cf);

                if (adjust != SOFT_INPUT_ADJUST_NOTHING) {
                    vf.left = mCurLeft;
                    vf.top = mCurTop;
                    vf.right = mCurRight;
                    vf.bottom = mCurBottom;
                } else {
                    vf.set(cf);
                }
            } else if (attached != null) {
                if (DEBUG_LAYOUT) Slog.v(TAG, "layoutWindowLw(" + attrs.getTitle() +
                        "): attached to " + attached);
                // A child window should be placed inside of the same visible
                // frame that its parent had.
                setAttachedWindowFrames(win, fl, adjust, attached, false, pf, df, of, cf, vf);
            } else {
                if (DEBUG_LAYOUT) Slog.v(TAG, "layoutWindowLw(" + attrs.getTitle() +
                        "): normal window");
                // Otherwise, a normal window must be placed inside the content
                // of all screen decorations.
                if (attrs.type == TYPE_STATUS_BAR_PANEL || attrs.type == TYPE_VOLUME_OVERLAY) {
                    // Status bar panels and the volume dialog are the only windows who can go on
                    // top of the status bar.  They are protected by the STATUS_BAR_SERVICE
                    // permission, so they have the same privileges as the status
                    // bar itself.
                    pf.left = df.left = of.left = cf.left = mRestrictedScreenLeft;
                    pf.top = df.top = of.top = cf.top = mRestrictedScreenTop;
                    pf.right = df.right = of.right = cf.right = mRestrictedScreenLeft
                            + mRestrictedScreenWidth;
                    pf.bottom = df.bottom = of.bottom = cf.bottom = mRestrictedScreenTop
                            + mRestrictedScreenHeight;
                } else if (attrs.type == TYPE_TOAST || attrs.type == TYPE_SYSTEM_ALERT) {
                    // These dialogs are stable to interim decor changes.
                    pf.left = df.left = of.left = cf.left = mStableLeft;
                    pf.top = df.top = of.top = cf.top = mStableTop;
                    pf.right = df.right = of.right = cf.right = mStableRight;
                    pf.bottom = df.bottom = of.bottom = cf.bottom = mStableBottom;
                } else {
                    pf.left = mContentLeft;
                    pf.top = mContentTop;
                    pf.right = mContentRight;
                    pf.bottom = mContentBottom;
                    if (win.isVoiceInteraction()) {
                        df.left = of.left = cf.left = mVoiceContentLeft;
                        df.top = of.top = cf.top = mVoiceContentTop;
                        df.right = of.right = cf.right = mVoiceContentRight;
                        df.bottom = of.bottom = cf.bottom = mVoiceContentBottom;
                    } else if (adjust != SOFT_INPUT_ADJUST_RESIZE) {
                        df.left = of.left = cf.left = mDockLeft;
                        df.top = of.top = cf.top = mDockTop;
                        df.right = of.right = cf.right = mDockRight;
                        df.bottom = of.bottom = cf.bottom = mDockBottom;
                    } else {
                        df.left = of.left = cf.left = mContentLeft;
                        df.top = of.top = cf.top = mContentTop;
                        df.right = of.right = cf.right = mContentRight;
                        df.bottom = of.bottom = cf.bottom = mContentBottom;
                    }
                    if (adjust != SOFT_INPUT_ADJUST_NOTHING) {
                        vf.left = mCurLeft;
                        vf.top = mCurTop;
                        vf.right = mCurRight;
                        vf.bottom = mCurBottom;
                    } else {
                        vf.set(cf);
                    }
                }
            }
            
            /* prize-add- 0 180 rotation, fullscreen app,  cut lcd ears-liyongli-20180509-start */                 
            if( PrizeOption.PRIZE_NOTCH_SCREEN  && mDisplayRotation == Surface.ROTATION_0
                && isAppWindow && (mFocusedWindow==win)            
                && attrs.type != TYPE_APPLICATION_STARTING //start
                && !prizeNeedsmall && coverEarsWinAttrs !=null  && coverEarsWinAttrs.y == 0 
    /*            &&  win.isVisibleLw() && win.isDisplayedLw() && win.isDrawnLw()
                && ( mStatusBar.isGoneForLayoutLw()  //caseA.. No StatusBar
                           || (mStatusBar.isDisplayedLw() && !mStatusBar.isAnimatingLw()  ) //case B. has StatusBar
                        )*/
            ){//RECENT  HOME  set hidden the CoverEars
                coverEarsWinAttrs.y = -100;
                if(DEBUG_NOTSCREEN)Slog.v(TAG, " lyl  2  coverear  -100  " + win.isVisibleLw()  + " "+win.isDisplayedLw()+ " "+win.isGoneForLayoutLw()+" "+win.isDrawnLw() 
                 +"  "+ win.isAnimatingLw()
                  + "  ---  "+mStatusBar.isDisplayedLw()+ " "+mStatusBar.isGoneForLayoutLw()+" "+mStatusBar.isDrawnLw() 
                +"  "+ mStatusBar.isAnimatingLw()
                );
            }
        }

        // TYPE_SYSTEM_ERROR is above the NavigationBar so it can't be allowed to extend over it.
        // Also, we don't allow windows in multi-window mode to extend out of the screen.
        if ((fl & FLAG_LAYOUT_NO_LIMITS) != 0 && attrs.type != TYPE_SYSTEM_ERROR
                && !win.isInMultiWindowMode()) {
            df.left = df.top = -10000;
            df.right = df.bottom = 10000;
            if (attrs.type != TYPE_WALLPAPER) {
                of.left = of.top = cf.left = cf.top = vf.left = vf.top = -10000;
                of.right = of.bottom = cf.right = cf.bottom = vf.right = vf.bottom = 10000;
            }
        }

        // If the device has a chin (e.g. some watches), a dead area at the bottom of the screen we
        // need to provide information to the clients that want to pretend that you can draw there.
        // We only want to apply outsets to certain types of windows. For example, we never want to
        // apply the outsets to floating dialogs, because they wouldn't make sense there.
        final boolean useOutsets = shouldUseOutsets(attrs, fl);
        if (isDefaultDisplay && useOutsets) {
            osf = mTmpOutsetFrame;
            osf.set(cf.left, cf.top, cf.right, cf.bottom);
            int outset = ScreenShapeHelper.getWindowOutsetBottomPx(mContext.getResources());
            if (outset > 0) {
                int rotation = mDisplayRotation;
                if (rotation == Surface.ROTATION_0) {
                    osf.bottom += outset;
                } else if (rotation == Surface.ROTATION_90) {
                    osf.right += outset;
                } else if (rotation == Surface.ROTATION_180) {
                    osf.top -= outset;
                } else if (rotation == Surface.ROTATION_270) {
                    osf.left -= outset;
                }
                if (DEBUG_LAYOUT) Slog.v(TAG, "applying bottom outset of " + outset
                        + " with rotation " + rotation + ", result: " + osf);
            }
        }

        if (DEBUG_LAYOUT) Slog.v(TAG, "Compute frame " + attrs.getTitle()
                + ": sim=#" + Integer.toHexString(sim)
                + " attach=" + attached + " type=" + attrs.type
                + String.format(" flags=0x%08x", fl)
                + " pf=" + pf.toShortString() + " df=" + df.toShortString()
                + " of=" + of.toShortString()
                + " cf=" + cf.toShortString() + " vf=" + vf.toShortString()
                + " dcf=" + dcf.toShortString()
                + " sf=" + sf.toShortString()
                + " osf=" + (osf == null ? "null" : osf.toShortString()));

        /// M: add for fullscreen switch feature @{
        if (mSupportFullscreenSwitch) {
            resetFullScreenSwitch(win, of);
        }
        /// @}
        

         /*-prize-add by lihuangyuan,for insertsim reboot-2018-05-22-start*/
        if(attrs.getTitle() != null && attrs.getTitle().equals("simreboot"))
        {
            pf.bottom = df.bottom = of.bottom = mOverscanScreenTop+ mOverscanScreenHeight;
        }
         /*-prize-add by lihuangyuan,for insertsim reboot-2018-05-22-end*/
        
        /* prize-add-split screen, LANDSCAPE_ONE_STATUBAR fix 49712 -liyongli-20180227-start */
        if (PrizeOption.PRIZE_SPLIT_SCREEN_LANDSCAPE_ONE_STATUBAR && win.getAttrs().type == TYPE_DOCK_DIVIDER){
            dcf.top = 0;
            //Slog.v(TAG,   "    dcf="+dcf );
        }/* prize-add-split screen, LANDSCAPE_ONE_STATUBAR fix 49712 -liyongli-20180227-end */
        
        String attrsTitle =  attrs.getTitle()+"";
        //Slog.v(TAG,   "    attrsTitle="+attrsTitle+" "+ pf + " "+df+" "+of );//PointerLocation
        if(PrizeOption.PRIZE_NOTCH_SCREEN && attrsTitle!=null){
            if(attrsTitle.equals("CoverEars") || attrsTitle.equals("notchscreen")){
                //prize -add by lihuangyuan,for notchscreen -2018-04-20
                pf.top = 0;
                pf.left = 0;
            }else if(attrsTitle.equals("prizeDivMenu")){
                pf.left = 0; //prize-add fix bug, (notch lcd) landspace split screen, pop close menu position error -liyongli-20180504
            }else if(attrsTitle.equals("PointerLocation")){//fix bug,  Setting ->Developer options->Pointer location not draw in pen down position
                pf.top = 0;
                pf.left = 0;
                df.left = 0;
                of.left = cf.left = vf.left = dcf.left = 0;
            }
        }
        
        win.computeFrameLw(pf, df, of, cf, vf, dcf, sf, osf);
        // Nav bar color customized feature. prize-linkh-2017.08.04 @{
        if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            if (isDefaultDisplay && win.isImWindow() && attached != null
                && !win.isGoneForLayoutLw() && mInputMethodChildWinBehindNavBar == null) {
                // input method child window. Almost it's a popup window.
                Rect frame = win.getFrameLw();
                if (frame.bottom == mUnrestrictedScreenHeight) {
                    mInputMethodChildWinBehindNavBar = win;
                }
            }
        } //@}

        // Dock windows carve out the bottom of the screen, so normal windows
        // can't appear underneath them.
        if (attrs.type == TYPE_INPUT_METHOD && win.isVisibleOrBehindKeyguardLw()
                && win.isDisplayedLw() && !win.getGivenInsetsPendingLw()) {
            setLastInputMethodWindowLw(null, null);
            offsetInputMethodWindowLw(win);
        }
        if (attrs.type == TYPE_VOICE_INTERACTION && win.isVisibleOrBehindKeyguardLw()
                && !win.getGivenInsetsPendingLw()) {
            offsetVoiceInputWindowLw(win);
        }
    }

    private void layoutWallpaper(WindowState win, Rect pf, Rect df, Rect of, Rect cf) {

        // The wallpaper also has Real Ultimate Power, but we want to tell
        // it about the overscan area.
        pf.left = df.left = mOverscanScreenLeft;
        pf.top = df.top = mOverscanScreenTop;
        pf.right = df.right = mOverscanScreenLeft + mOverscanScreenWidth;
        pf.bottom = df.bottom = mOverscanScreenTop + mOverscanScreenHeight;
        of.left = cf.left = mUnrestrictedScreenLeft;
        of.top = cf.top = mUnrestrictedScreenTop;
        of.right = cf.right = mUnrestrictedScreenLeft + mUnrestrictedScreenWidth;
        of.bottom = cf.bottom = mUnrestrictedScreenTop + mUnrestrictedScreenHeight;
    }

    private void offsetInputMethodWindowLw(WindowState win) {
        int top = Math.max(win.getDisplayFrameLw().top, win.getContentFrameLw().top);
        top += win.getGivenContentInsetsLw().top;
        if (mContentBottom > top) {
            mContentBottom = top;
        }
        if (mVoiceContentBottom > top) {
            mVoiceContentBottom = top;
        }
        top = win.getVisibleFrameLw().top;
        top += win.getGivenVisibleInsetsLw().top;
        if (mCurBottom > top) {
            mCurBottom = top;
        }
        if (DEBUG_LAYOUT) Slog.v(TAG, "Input method: mDockBottom="
                + mDockBottom + " mContentBottom="
                + mContentBottom + " mCurBottom=" + mCurBottom);
    }

    private void offsetVoiceInputWindowLw(WindowState win) {
        int top = Math.max(win.getDisplayFrameLw().top, win.getContentFrameLw().top);
        top += win.getGivenContentInsetsLw().top;
        if (mVoiceContentBottom > top) {
            mVoiceContentBottom = top;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void finishLayoutLw() {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public void beginPostLayoutPolicyLw(int displayWidth, int displayHeight) {
        mTopFullscreenOpaqueWindowState = null;
        mTopFullscreenOpaqueOrDimmingWindowState = null;
        mTopDockedOpaqueWindowState = null;
        mTopDockedOpaqueOrDimmingWindowState = null;
        mAppsToBeHidden.clear();
        mAppsThatDismissKeyguard.clear();
        mForceStatusBar = false;
        mForceStatusBarFromKeyguard = false;
        mForceStatusBarTransparent = false;
        mForcingShowNavBar = false;
        mForcingShowNavBarLayer = -1;

        mHideLockScreen = false;
        /*-prize-add by lihuangyuan,for check whether has showonlock window-2018-04-04-start*/
        mIsShowOnLockScreen = false;
        /*-prize-add by lihuangyuan,for check whether has showonlock window-2018-04-04-end*/
        mAllowLockscreenWhenOn = false;
        mDismissKeyguard = DISMISS_KEYGUARD_NONE;
        mShowingLockscreen = false;
        mShowingDream = false;
        mWinShowWhenLocked = null;
        mKeyguardSecure = isKeyguardSecure(mCurrentUserId);
        mKeyguardSecureIncludingHidden = mKeyguardSecure
                && (mKeyguardDelegate != null && mKeyguardDelegate.isShowing());

        /// [ALPS02869412] when wms init, dream manager is not started yet...
        if (mDreamManagerInternal == null) {
            mDreamManagerInternal = LocalServices.getService(DreamManagerInternal.class);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void applyPostLayoutPolicyLw(WindowState win, WindowManager.LayoutParams attrs,
            WindowState attached) {
        if (DEBUG_LAYOUT) Slog.i(TAG, "applyPostLayoutPolicyLw Win " + win +
            ": isVisibleOrBehindKeyguardLw=" + win.isVisibleOrBehindKeyguardLw() +
            ", win.isVisibleLw()=" + win.isVisibleLw() +
            ", win.hasDrawnLw()=" + win.hasDrawnLw() +
            ", win.isDrawnLw()=" + win.isDrawnLw() +
            ", attrs.type=" + attrs.type +
            ", attrs.privateFlags=#" + Integer.toHexString(attrs.privateFlags) +
            ", fl=#" + Integer.toHexString(PolicyControl.getWindowFlags(win, attrs)) +
            ", stackId=" + win.getStackId() +
            ", mTopFullscreenOpaqueWindowState=" + mTopFullscreenOpaqueWindowState +
            ", win.isVisibleOrBehindKeyguardLw()=" + win.isVisibleOrBehindKeyguardLw() +
            ", win.isGoneForLayoutLw()=" + win.isGoneForLayoutLw() +
            ", attached=" + attached +
            ", isFullscreen=" + isFullscreen(attrs) +
            ", normallyFullscreenWindows=" + StackId.normallyFullscreenWindows(win.getStackId()) +
            ", mDreamingLockscreen=" + mDreamingLockscreen +
            ", mShowingDream=" + mShowingDream);

        final int fl = PolicyControl.getWindowFlags(win, attrs);
        if (mTopFullscreenOpaqueWindowState == null
                && win.isVisibleLw() && attrs.type == TYPE_INPUT_METHOD) {
            mForcingShowNavBar = true;
            mForcingShowNavBarLayer = win.getSurfaceLayer();
        }
        if (attrs.type == TYPE_STATUS_BAR) {
            if ((attrs.privateFlags & PRIVATE_FLAG_KEYGUARD) != 0) {
                mForceStatusBarFromKeyguard = true;
                mShowingLockscreen = true;
            }
            if ((attrs.privateFlags & PRIVATE_FLAG_FORCE_STATUS_BAR_VISIBLE_TRANSPARENT) != 0) {
                mForceStatusBarTransparent = true;
            }
        }

        boolean appWindow = attrs.type >= FIRST_APPLICATION_WINDOW
                && attrs.type < FIRST_SYSTEM_WINDOW;
        final boolean showWhenLocked = (fl & FLAG_SHOW_WHEN_LOCKED) != 0;
        final boolean dismissKeyguard = (fl & FLAG_DISMISS_KEYGUARD) != 0;
        final int stackId = win.getStackId();
        if (mTopFullscreenOpaqueWindowState == null &&
                win.isVisibleOrBehindKeyguardLw() && !win.isGoneForLayoutLw()) {
            if ((fl & FLAG_FORCE_NOT_FULLSCREEN) != 0) {
                if ((attrs.privateFlags & PRIVATE_FLAG_KEYGUARD) != 0) {
                    mForceStatusBarFromKeyguard = true;
                } else {
                    mForceStatusBar = true;
                }
            }
            if (attrs.type == TYPE_DREAM) {
                // If the lockscreen was showing when the dream started then wait
                // for the dream to draw before hiding the lockscreen.
                if (!mDreamingLockscreen
                        || (win.isVisibleLw() && win.hasDrawnLw())) {
                    mShowingDream = true;
                    appWindow = true;
                }
            }

            final IApplicationToken appToken = win.getAppToken();

            // For app windows that are not attached, we decide if all windows in the app they
            // represent should be hidden or if we should hide the lockscreen. For attached app
            // windows we defer the decision to the window it is attached to.
            if (appWindow && attached == null) {
                if (showWhenLocked) {
                    // Remove any previous windows with the same appToken.
                    mAppsToBeHidden.remove(appToken);
                    mAppsThatDismissKeyguard.remove(appToken);
                    if (mAppsToBeHidden.isEmpty()) {
                        if (dismissKeyguard && !mKeyguardSecure) {
                            mAppsThatDismissKeyguard.add(appToken);
                        } else if (win.isDrawnLw() || win.hasAppShownWindows()) {
                            if (DEBUG_KEYGUARD) Slog.v(TAG,  "ShowWhenLocked: " + win);
                            mWinShowWhenLocked = win;
                            mHideLockScreen = true;
                            mForceStatusBarFromKeyguard = false;
                        }
                    }
                } else if (dismissKeyguard) {
                    if (mKeyguardSecure) {
                        mAppsToBeHidden.add(appToken);
                    } else {
                        mAppsToBeHidden.remove(appToken);
                    }
                    mAppsThatDismissKeyguard.add(appToken);
                } else {
                    mAppsToBeHidden.add(appToken);
                }
                if (isFullscreen(attrs) && StackId.normallyFullscreenWindows(stackId)) {
                    if (DEBUG_LAYOUT) Slog.v(TAG, "Fullscreen window: " + win);
                    mTopFullscreenOpaqueWindowState = win;
                    if (mTopFullscreenOpaqueOrDimmingWindowState == null) {
                        mTopFullscreenOpaqueOrDimmingWindowState = win;
                    }
                    if (!mAppsThatDismissKeyguard.isEmpty() &&
                            mDismissKeyguard == DISMISS_KEYGUARD_NONE) {
                        if (DEBUG_LAYOUT) Slog.v(TAG,
                                "Setting mDismissKeyguard true by win " + win);
                        mDismissKeyguard = (mWinDismissingKeyguard == win
                                && mSecureDismissingKeyguard == mKeyguardSecure)
                                ? DISMISS_KEYGUARD_CONTINUE : DISMISS_KEYGUARD_START;
                        mWinDismissingKeyguard = win;
                        mSecureDismissingKeyguard = mKeyguardSecure;
                        mForceStatusBarFromKeyguard = mShowingLockscreen && mKeyguardSecure;
                    } else if (mAppsToBeHidden.isEmpty() && showWhenLocked
                            && (win.isDrawnLw() || win.hasAppShownWindows())) {
                        if (DEBUG_LAYOUT) Slog.v(TAG,
                                "Setting mHideLockScreen to true by win " + win);
                        mHideLockScreen = true;
                        mForceStatusBarFromKeyguard = false;
                    }
                    if ((fl & FLAG_ALLOW_LOCK_WHILE_SCREEN_ON) != 0) {
                        mAllowLockscreenWhenOn = true;
                    }
                }

                if (!mKeyguardHidden && mWinShowWhenLocked != null &&
                        mWinShowWhenLocked.getAppToken() != win.getAppToken() &&
                        (attrs.flags & FLAG_SHOW_WHEN_LOCKED) == 0) {
                    win.hideLw(false);
                }
            }
        } else if (mTopFullscreenOpaqueWindowState == null && mWinShowWhenLocked == null) {
            // No TopFullscreenOpaqueWindow is showing, but we found a SHOW_WHEN_LOCKED window
            // that is being hidden in an animation - keep the
            // keyguard hidden until the new window shows up and
            // we know whether to show the keyguard or not.
            if (win.isAnimatingLw() && appWindow && showWhenLocked && mKeyguardHidden) {
                if (DEBUG_KEYGUARD) Slog.v(TAG,  "ShowWhenLocked no top: " + win);
                mHideLockScreen = true;
                mWinShowWhenLocked = win;
            }
        }
        /*-prize-add by lihuangyuan,for check whether has showonlock window-2018-04-04-start*/
        if((win.isVisibleLw()||!win.hasDrawnLw()) && (WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED & win.getAttrs().flags) != 0
            && ("com.mediatek.camera/com.android.camera.SecureCameraActivity".equals(win.getAttrs().getTitle()) 
            ||"com.prize.applock.fingerprintapplock/com.prize.applock.fingerprintapplock.LockUI".equals(win.getAttrs().getTitle()) 
            ||"com.android.gallery3d/com.android.gallery3d.app.GalleryActivity".equals(win.getAttrs().getTitle())))
        {
            Slog.d(TAG,"FLAG_SHOW_WHEN_LOCKED win :"+win);
            mIsShowOnLockScreen = true;
        }
        /*-prize-add by lihuangyuan,for check whether has showonlock window-2018-04-04-end*/
        
        /*prize-add- 0 180 rotation, fullscreen app,  cut lcd ears then Force SHOW statusbar-liyongli-20180510-start*/
        if( PrizeOption.PRIZE_NOTCH_SCREEN
            && (mDisplayRotation == Surface.ROTATION_0 || mDisplayRotation == Surface.ROTATION_180)
        ){
            boolean tmp = mForceStatusBar;
            if(appWindow && attrs.prizeWinMoveEarlcdBottom==1){
                mForceStatusBar = true;
            }else{
                mForceStatusBar = tmp;
            }
        }/*prize-add- 0 180 rotation, fullscreen app,  cut lcd ears then Force SHOW statusbar-liyongli-20180510-end*/
        
        final boolean reallyVisible = win.isVisibleOrBehindKeyguardLw() && !win.isGoneForLayoutLw();

        // Voice interaction overrides both top fullscreen and top docked.
        if (reallyVisible && win.getAttrs().type == TYPE_VOICE_INTERACTION) {
            if (mTopFullscreenOpaqueWindowState == null) {
                mTopFullscreenOpaqueWindowState = win;
                if (mTopFullscreenOpaqueOrDimmingWindowState == null) {
                    mTopFullscreenOpaqueOrDimmingWindowState = win;
                }
            }
            if (mTopDockedOpaqueWindowState == null) {
                mTopDockedOpaqueWindowState = win;
                if (mTopDockedOpaqueOrDimmingWindowState == null) {
                    mTopDockedOpaqueOrDimmingWindowState = win;
                }
            }
        }

        // Keep track of the window if it's dimming but not necessarily fullscreen.
        if (mTopFullscreenOpaqueOrDimmingWindowState == null && reallyVisible
                && win.isDimming() && StackId.normallyFullscreenWindows(stackId)) {
            mTopFullscreenOpaqueOrDimmingWindowState = win;
        }

        // We need to keep track of the top "fullscreen" opaque window for the docked stack
        // separately, because both the "real fullscreen" opaque window and the one for the docked
        // stack can control View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.
        if (mTopDockedOpaqueWindowState == null && reallyVisible && appWindow && attached == null
                && isFullscreen(attrs) && stackId == DOCKED_STACK_ID) {
            mTopDockedOpaqueWindowState = win;
            if (mTopDockedOpaqueOrDimmingWindowState == null) {
                mTopDockedOpaqueOrDimmingWindowState = win;
            }
        }

        // Also keep track of any windows that are dimming but not necessarily fullscreen in the
        // docked stack.
        if (mTopDockedOpaqueOrDimmingWindowState == null && reallyVisible && win.isDimming()
                && stackId == DOCKED_STACK_ID) {
            mTopDockedOpaqueOrDimmingWindowState = win;
        }
    }

    private boolean isFullscreen(WindowManager.LayoutParams attrs) {
        return attrs.x == 0 && attrs.y == 0
                && attrs.width == WindowManager.LayoutParams.MATCH_PARENT
                && attrs.height == WindowManager.LayoutParams.MATCH_PARENT;
    }

    /** {@inheritDoc} */
    @Override
    public int finishPostLayoutPolicyLw() {
        if (mWinShowWhenLocked != null && mTopFullscreenOpaqueWindowState != null &&
                mWinShowWhenLocked.getAppToken() != mTopFullscreenOpaqueWindowState.getAppToken()
                && isKeyguardLocked()) {
            // A dialog is dismissing the keyguard. Put the wallpaper behind it and hide the
            // fullscreen window.
            // TODO: Make sure FLAG_SHOW_WALLPAPER is restored when dialog is dismissed. Or not.
            mWinShowWhenLocked.getAttrs().flags |= FLAG_SHOW_WALLPAPER;
            /// M: Check null object. {@
            if (mTopFullscreenOpaqueWindowState != null) {
                mTopFullscreenOpaqueWindowState.hideLw(false);
            }
            /// @}
            mTopFullscreenOpaqueWindowState = mWinShowWhenLocked;
        }

        int changes = 0;
        boolean topIsFullscreen = false;

        final WindowManager.LayoutParams lp = (mTopFullscreenOpaqueWindowState != null)
                ? mTopFullscreenOpaqueWindowState.getAttrs()
                : null;

        // If we are not currently showing a dream then remember the current
        // lockscreen state.  We will use this to determine whether the dream
        // started while the lockscreen was showing and remember this state
        // while the dream is showing.
        if (!mShowingDream) {
            mDreamingLockscreen = mShowingLockscreen;
            if (mDreamingSleepTokenNeeded) {
                mDreamingSleepTokenNeeded = false;
                mHandler.obtainMessage(MSG_UPDATE_DREAMING_SLEEP_TOKEN, 0, 1).sendToTarget();
            }
        } else {
            if (!mDreamingSleepTokenNeeded) {
                mDreamingSleepTokenNeeded = true;
                mHandler.obtainMessage(MSG_UPDATE_DREAMING_SLEEP_TOKEN, 1, 1).sendToTarget();
            }
        }

        if (mStatusBar != null) {
            if (DEBUG_LAYOUT) Slog.i(TAG, "force=" + mForceStatusBar
                    + " forcefkg=" + mForceStatusBarFromKeyguard
                    + " top=" + mTopFullscreenOpaqueWindowState
                    + " dream=" + (mDreamManagerInternal != null ?
                        mDreamManagerInternal.isDreaming() : "null"));
            boolean shouldBeTransparent = mForceStatusBarTransparent
                    && !mForceStatusBar
                    && !mForceStatusBarFromKeyguard;
            if (!shouldBeTransparent) {
                mStatusBarController.setShowTransparent(false /* transparent */);
            } else if (!mStatusBar.isVisibleLw()) {
                mStatusBarController.setShowTransparent(true /* transparent */);
            }

            WindowManager.LayoutParams statusBarAttrs = mStatusBar.getAttrs();
            boolean statusBarExpanded = statusBarAttrs.height == MATCH_PARENT
                    && statusBarAttrs.width == MATCH_PARENT;
            /// [ALPS02869412] when dreaming, force hide status bar to avoid keyguard flash
            if(mDreamManagerInternal != null && mDreamManagerInternal.isDreaming()) {
                if (DEBUG_LAYOUT) Slog.v(TAG, "** HIDING status bar: dreaming");
                if (mStatusBarController.setBarShowingLw(false)) {
                    changes |= FINISH_LAYOUT_REDO_LAYOUT;
                }
            } else if (mForceStatusBar || mForceStatusBarFromKeyguard || mForceStatusBarTransparent
                    || statusBarExpanded) {
                if (DEBUG_LAYOUT) Slog.v(TAG, "Showing status bar: forced");
                if (mStatusBarController.setBarShowingLw(true)) {
                    changes |= FINISH_LAYOUT_REDO_LAYOUT;
                }
                // Maintain fullscreen layout until incoming animation is complete.
                topIsFullscreen = mTopIsFullscreen && mStatusBar.isAnimatingLw();
                // Transient status bar on the lockscreen is not allowed
                if (mForceStatusBarFromKeyguard && mStatusBarController.isTransientShowing()) {
                    mStatusBarController.updateVisibilityLw(false /*transientAllowed*/,
                            mLastSystemUiFlags, mLastSystemUiFlags);
                }
                if (statusBarExpanded && mNavigationBar != null) {
                    if (mNavigationBarController.setBarShowingLw(true)) {
                        changes |= FINISH_LAYOUT_REDO_LAYOUT;
                    }
                }
            } else if (mTopFullscreenOpaqueWindowState != null) {
                final int fl = PolicyControl.getWindowFlags(null, lp);
                if (localLOGV) {
                    Slog.d(TAG, "frame: " + mTopFullscreenOpaqueWindowState.getFrameLw()
                            + " shown position: "
                            + mTopFullscreenOpaqueWindowState.getShownPositionLw());
                    Slog.d(TAG, "attr: " + mTopFullscreenOpaqueWindowState.getAttrs()
                            + " lp.flags=0x" + Integer.toHexString(fl));
                }
                topIsFullscreen = (fl & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0
                        || (mLastSystemUiFlags & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0;
                
                /* prize-add-split screen, not use FullScreen -liyongli-20171024-start */
                if( PrizeOption.PRIZE_SPLIT_SCREEN_STATUBAR_USE_DOCKEDWIN
                     && mWindowManagerInternal.isStackVisible(DOCKED_STACK_ID) ){
                        topIsFullscreen = false; //(e.g.  com.andreader.prein  OR com.shuqi.controller ) 
                }
                /* prize-add-split screen-liyongli-20171024-end */

                // The subtle difference between the window for mTopFullscreenOpaqueWindowState
                // and mTopIsFullscreen is that mTopIsFullscreen is set only if the window
                // has the FLAG_FULLSCREEN set.  Not sure if there is another way that to be the
                // case though.
                if (mStatusBarController.isTransientShowing()) {
                    if (mStatusBarController.setBarShowingLw(true)) {
                        changes |= FINISH_LAYOUT_REDO_LAYOUT;
                    }
                } else if (topIsFullscreen
                        && !mWindowManagerInternal.isStackVisible(FREEFORM_WORKSPACE_STACK_ID)
                        && !mWindowManagerInternal.isStackVisible(DOCKED_STACK_ID)) {
                    if (DEBUG_LAYOUT) Slog.v(TAG, "** HIDING status bar");
                    if (mStatusBarController.setBarShowingLw(false)) {
                        changes |= FINISH_LAYOUT_REDO_LAYOUT;
                    } else {
                        if (DEBUG_LAYOUT) Slog.v(TAG, "Status bar already hiding");
                    }
                } else {
                    if (DEBUG_LAYOUT) Slog.v(TAG, "** SHOWING status bar: top is not fullscreen");
                    if (mStatusBarController.setBarShowingLw(true)) {
                        changes |= FINISH_LAYOUT_REDO_LAYOUT;
                    }
                }
            }
        }

        if (mTopIsFullscreen != topIsFullscreen) {
            if (!topIsFullscreen) {
                // Force another layout when status bar becomes fully shown.
                changes |= FINISH_LAYOUT_REDO_LAYOUT;
            }
            mTopIsFullscreen = topIsFullscreen;
        }

        // Hide the key guard if a visible window explicitly specifies that it wants to be
        // displayed when the screen is locked.
        if (mKeyguardDelegate != null && mStatusBar != null) {
            if (localLOGV) Slog.v(TAG, "finishPostLayoutPolicyLw: mHideKeyguard="
                    + mHideLockScreen
                    /// M: Add more log at WMS @{
                    + " mDismissKeyguard=" + mDismissKeyguard
                    + " mKeyguardDelegate.isSecure()= "
                    + mKeyguardDelegate.isSecure(mCurrentUserId));
                    /// @}
            if (mDismissKeyguard != DISMISS_KEYGUARD_NONE && !mKeyguardSecure) {
                mKeyguardHidden = true;
                if (setKeyguardOccludedLw(true)) {
                    changes |= FINISH_LAYOUT_REDO_LAYOUT
                            | FINISH_LAYOUT_REDO_CONFIG
                            | FINISH_LAYOUT_REDO_WALLPAPER;
                }
                if (mKeyguardDelegate.isShowing()) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mKeyguardDelegate.keyguardDone(false, false);
                        }
                    });
                }
            } else if (mHideLockScreen) {
                mKeyguardHidden = true;
                mWinDismissingKeyguard = null;
                if (setKeyguardOccludedLw(true)) {
                    changes |= FINISH_LAYOUT_REDO_LAYOUT
                            | FINISH_LAYOUT_REDO_CONFIG
                            | FINISH_LAYOUT_REDO_WALLPAPER;
                }
            } else if (mDismissKeyguard != DISMISS_KEYGUARD_NONE) {
                mKeyguardHidden = false;
                boolean dismissKeyguard = false;
                final boolean trusted = mKeyguardDelegate.isTrusted();
                if (mDismissKeyguard == DISMISS_KEYGUARD_START) {
                    final boolean willDismiss = trusted && mKeyguardOccluded
                            && mKeyguardDelegate != null && mKeyguardDelegate.isShowing();
                    if (willDismiss) {
                        mCurrentlyDismissingKeyguard = true;
                    }
                    dismissKeyguard = true;
                }

                // If we are currently dismissing Keyguard, there is no need to unocclude it.
                if (!mCurrentlyDismissingKeyguard) {
                    if (setKeyguardOccludedLw(false)) {
                        changes |= FINISH_LAYOUT_REDO_LAYOUT
                                | FINISH_LAYOUT_REDO_CONFIG
                                | FINISH_LAYOUT_REDO_WALLPAPER;
                    }
                }

                if (dismissKeyguard) {
                    // Only launch the next keyguard unlock window once per window.
                    mHandler.post(() -> mKeyguardDelegate.dismiss(
                            trusted /* allowWhileOccluded */));
                }
            }             
            /*-prize-add by lihuangyuan,for check whether has showonlock window-2018-04-04-start*/
            else if(mIsShowOnLockScreen)
            {
                mKeyguardHidden = true;
                mWinDismissingKeyguard = null;
                if (setKeyguardOccludedLw(true)) {
                    changes |= FINISH_LAYOUT_REDO_LAYOUT
                            | FINISH_LAYOUT_REDO_CONFIG
                            | FINISH_LAYOUT_REDO_WALLPAPER;
                }
            }
            /*-prize-add by lihuangyuan,for check whether has showonlock window-2018-04-04-end*/
            else
            {
                mWinDismissingKeyguard = null;
                mSecureDismissingKeyguard = false;
                mKeyguardHidden = false;
                if (setKeyguardOccludedLw(false)) {
                    changes |= FINISH_LAYOUT_REDO_LAYOUT
                            | FINISH_LAYOUT_REDO_CONFIG
                            | FINISH_LAYOUT_REDO_WALLPAPER;
                }
            }
        }

        if ((updateSystemUiVisibilityLw()&SYSTEM_UI_CHANGING_LAYOUT) != 0) {
            // If the navigation bar has been hidden or shown, we need to do another
            // layout pass to update that window.
            changes |= FINISH_LAYOUT_REDO_LAYOUT;
        }

        // Nav bar color customized feature. prize-linkh-20171017 @{
        if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            if (!mLastHasWinSetNavBarColor && !mLastDispatchedEnableSysUINavBarColorBg
                && mTopFullscreenOpaqueWindowState != null
                && mTopFullscreenOpaqueOrDimmingWindowState != mTopFullscreenOpaqueWindowState
                 && mFocusedWindow == mTopFullscreenOpaqueOrDimmingWindowState) {
                final int visToBeCleard = View.SYSTEM_UI_FLAG_LOW_PROFILE;
                if ((mTopFullscreenOpaqueWindowState.getSystemUiVisibility() & visToBeCleard) != 0) {
                    if (true) {
                        Slog.d(TAG, "finishPostLayoutPolicyLw() force clear sys ui vis. win=" 
                            + mTopFullscreenOpaqueWindowState + ", vis=0x" + Integer.toHexString(visToBeCleard));
                    }
                    mWindowManagerFuncs.forceClearSysUiVisLw(mTopFullscreenOpaqueWindowState, visToBeCleard);
                }
            }
        } // @}

        // update since mAllowLockscreenWhenOn might have changed
        updateLockScreenTimeout();
        return changes;
    }

    /**
     * Updates the occluded state of the Keyguard.
     *
     * @return Whether the flags have changed and we have to redo the layout.
     */
    private boolean setKeyguardOccludedLw(boolean isOccluded) {
        boolean wasOccluded = mKeyguardOccluded;
        boolean showing = mKeyguardDelegate.isShowing();
        if (wasOccluded && !isOccluded && showing) {
            mKeyguardOccluded = false;
            mKeyguardDelegate.setOccluded(false, true /* animate */);
            mStatusBar.getAttrs().privateFlags |= PRIVATE_FLAG_KEYGUARD;
            if (!mKeyguardDelegate.hasLockscreenWallpaper()) {
                mStatusBar.getAttrs().flags |= FLAG_SHOW_WALLPAPER;
            }
            Animation anim = AnimationUtils.loadAnimation(mContext,
                    com.android.internal.R.anim.wallpaper_open_exit);
            mWindowManagerFuncs.overridePlayingAppAnimationsLw(anim);
            return true;
        } else if (!wasOccluded && isOccluded && showing) {
            mKeyguardOccluded = true;
            mKeyguardDelegate.setOccluded(true, false /* animate */);
            mStatusBar.getAttrs().privateFlags &= ~PRIVATE_FLAG_KEYGUARD;
            mStatusBar.getAttrs().flags &= ~FLAG_SHOW_WALLPAPER;
            return true;
        } else {
            return false;
        }
    }

    private void onKeyguardShowingStateChanged(boolean showing) {
        if (!showing) {
            synchronized (mWindowManagerFuncs.getWindowManagerLock()) {
                mCurrentlyDismissingKeyguard = false;
            }
        }
    }

    private boolean isStatusBarKeyguard() {
        return mStatusBar != null
                && (mStatusBar.getAttrs().privateFlags & PRIVATE_FLAG_KEYGUARD) != 0;
    }

    @Override
    public boolean allowAppAnimationsLw() {
        if (isStatusBarKeyguard() || mShowingDream) {
            // If keyguard or dreams is currently visible, no reason to animate behind it.
            return false;
        }
        return true;
    }

    @Override
    public int focusChangedLw(WindowState lastFocus, WindowState newFocus) {
        // Nav bar color customized feature. prize-linkh-2017.09.01 @{
        if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            // If this win has computed frame, then we still update nav bar color.
            // focus changed cases: 
            //   1.  null -> non-null
            //   2.  non-null -> null
            //   --> These two cases mean that wm will fire a relayout event.
            if (mFocusedWindow == null || newFocus == null || !newFocus.hasFrame()) {
                mInFocusChanged = true;
            }
        } //@}

        mFocusedWindow = newFocus;
        if ((updateSystemUiVisibilityLw()&SYSTEM_UI_CHANGING_LAYOUT) != 0) {
            // Nav bar color customized feature. prize-linkh-2017.09.01 @{
            if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
                mInFocusChanged = false;
            } //@}            
            // If the navigation bar has been hidden or shown, we need to do another
            // layout pass to update that window.
            return FINISH_LAYOUT_REDO_LAYOUT;
        }

        // Nav bar color customized feature. prize-linkh-2017.09.01 @{
        if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            mInFocusChanged = false;
        } //@}        

        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void notifyLidSwitchChanged(long whenNanos, boolean lidOpen) {
        // lid changed state
        final int newLidState = lidOpen ? LID_OPEN : LID_CLOSED;
        if (newLidState == mLidState) {
            return;
        }

        mLidState = newLidState;
        applyLidSwitchState();
        updateRotation(true);

        if (lidOpen) {
            wakeUp(SystemClock.uptimeMillis(), mAllowTheaterModeWakeFromLidSwitch,
                    "android.policy:LID");
        } else if (!mLidControlsSleep) {
            mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }
    }

    @Override
    public void notifyCameraLensCoverSwitchChanged(long whenNanos, boolean lensCovered) {
        int lensCoverState = lensCovered ? CAMERA_LENS_COVERED : CAMERA_LENS_UNCOVERED;
        if (mCameraLensCoverState == lensCoverState) {
            return;
        }
        if (mCameraLensCoverState == CAMERA_LENS_COVERED &&
                lensCoverState == CAMERA_LENS_UNCOVERED) {
            Intent intent;
            final boolean keyguardActive = mKeyguardDelegate == null ? false :
                    mKeyguardDelegate.isShowing();
            if (keyguardActive) {
                intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
            } else {
                intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            }
            wakeUp(whenNanos / 1000000, mAllowTheaterModeWakeFromCameraLens,
                    "android.policy:CAMERA_COVER");
            startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        }
        mCameraLensCoverState = lensCoverState;
    }

    void setHdmiPlugged(boolean plugged) {
        if (mHdmiPlugged != plugged) {
            mHdmiPlugged = plugged;
            updateRotation(true, true);
            Intent intent = new Intent(ACTION_HDMI_PLUGGED);
            intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
            intent.putExtra(EXTRA_HDMI_PLUGGED_STATE, plugged);
            mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    void initializeHdmiState() {
        boolean plugged = false;
        // watch for HDMI plug messages if the hdmi switch exists
        if (new File("/sys/devices/virtual/switch/hdmi/state").exists()) {
            mHDMIObserver.startObserving("DEVPATH=/devices/virtual/switch/hdmi");

            final String filename = "/sys/class/switch/hdmi/state";
            FileReader reader = null;
            try {
                reader = new FileReader(filename);
                char[] buf = new char[15];
                int n = reader.read(buf);
                if (n > 1) {
                    plugged = 0 != Integer.parseInt(new String(buf, 0, n-1));
                }
            } catch (IOException ex) {
                Slog.w(TAG, "Couldn't read hdmi state from " + filename + ": " + ex);
            } catch (NumberFormatException ex) {
                Slog.w(TAG, "Couldn't read hdmi state from " + filename + ": " + ex);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        // This dance forces the code in setHdmiPlugged to run.
        // Always do this so the sticky intent is stuck (to false) if there is no hdmi.
        mHdmiPlugged = !plugged;
        setHdmiPlugged(!mHdmiPlugged);
    }

    final Object mScreenshotLock = new Object();
    ServiceConnection mScreenshotConnection = null;

    final Runnable mScreenshotTimeout = new Runnable() {
        @Override public void run() {
            synchronized (mScreenshotLock) {
                if (mScreenshotConnection != null) {
                    mContext.unbindService(mScreenshotConnection);
                    mScreenshotConnection = null;
                    notifyScreenshotError();
                }
            }
        }
    };

    // Assume this is called from the Handler thread.
    private void takeScreenshot(final int screenshotType) {
        synchronized (mScreenshotLock) {
	    /*PRIZE-PowerExtendMode-dengyu-2016-08-11-start bugid 19299*/
	    if(PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
		 Log.i(TAG, "PowerExtendMode, forbid shot screen!");
		 return;
	    }
	    /*PRIZE-PowerExtendMode-dengyu-2016-08-11-end bugid 19299*/
            if (mScreenshotConnection != null) {
                return;
            }
            final ComponentName serviceComponent = new ComponentName(SYSUI_PACKAGE,
                    SYSUI_SCREENSHOT_SERVICE);
            final Intent serviceIntent = new Intent();
            serviceIntent.setComponent(serviceComponent);
            ServiceConnection conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    synchronized (mScreenshotLock) {
                        if (mScreenshotConnection != this) {
                            return;
                        }
                        Messenger messenger = new Messenger(service);
                        Message msg = Message.obtain(null, screenshotType);
                        final ServiceConnection myConn = this;
                        Handler h = new Handler(mHandler.getLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                synchronized (mScreenshotLock) {
                                    if (mScreenshotConnection == myConn) {
                                        mContext.unbindService(mScreenshotConnection);
                                        mScreenshotConnection = null;
                                        mHandler.removeCallbacks(mScreenshotTimeout);
                                    }
                                }
                            }
                        };
                        msg.replyTo = new Messenger(h);
                        msg.arg1 = msg.arg2 = 0;
                        if (mStatusBar != null && mStatusBar.isVisibleLw())
                            msg.arg1 = 1;
                        if (mNavigationBar != null && mNavigationBar.isVisibleLw())
                            msg.arg2 = 1;
                        try {
                            messenger.send(msg);
                        } catch (RemoteException e) {
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    synchronized (mScreenshotLock) {
                        if (mScreenshotConnection != null) {
                            mContext.unbindService(mScreenshotConnection);
                            mScreenshotConnection = null;
                            mHandler.removeCallbacks(mScreenshotTimeout);
                            notifyScreenshotError();
                        }
                    }
                }
            };
            if (mContext.bindServiceAsUser(serviceIntent, conn,
                    Context.BIND_AUTO_CREATE | Context.BIND_FOREGROUND_SERVICE_WHILE_AWAKE,
                    UserHandle.CURRENT)) {
                mScreenshotConnection = conn;
                mHandler.postDelayed(mScreenshotTimeout, 10000);
            }
        }
    }

    //solve bug-993. prize-linkh-20150519
    private void playFeedBack() {
	AudioManager audioManager = (AudioManager) mContext.getSystemService(
			Context.AUDIO_SERVICE);
	if (audioManager != null) {
		audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
	} else {
		Log.w(TAG, "Couldn't get audio manager");
	}
    } //end........
	/**
	
     * Notifies the screenshot service to show an error.
     */
//add by liup 20150519 for sleepgesture
	private String result = null;
	private static ContentResolver mContentResolver;
	private static Uri queryUri;
	private static Cursor cursor;
	static public final int  GESTURE_KEY_CODE = 139;
	
	private void init_sleep_gesture(){
		int bGesture = 0;
		final boolean bSleepGesture = Settings.System.getInt(mContentResolver,Settings.System.PRIZE_SLEEP_GESTURE,0) == 1;
		Cursor cursor = mContentResolver.query(queryUri, null, null, null,null);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				int onoff = cursor.getInt(cursor.getColumnIndex("onoff"));
				bGesture = bGesture + onoff;
			}while (cursor.moveToNext());
		}	
		if(cursor != null) {
			cursor.close();
		}
		if(bSleepGesture && bGesture > 0){
			/*
			File file = new File("/proc/gt9xx_enable");
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(file);
				fos.write("1".getBytes());
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			*/
			try {	
	    		String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + 1 + " > /proc/gt9xx_enable"};
				Runtime.getRuntime().exec(cmdMode);				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}else{
			/*
			File file = new File("/proc/gt9xx_enable");
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(file);
				fos.write("0".getBytes());
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			*/
			try {	
	    		String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + 0 + " > /proc/gt9xx_enable"};
				Runtime.getRuntime().exec(cmdMode);				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void prizeSleepGesture(){
		/*PRIZE-PowerExtendMode-wangxianzhen-2015-08-17-start bugid 4110*/
		if(PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
		    Log.i(TAG, "PowerExtendMode, not go prizeSleepGesture!");
		    return;
		}
		/*PRIZE-PowerExtendMode-wangxianzhen-2015-08-17-end bugid 4110*/
		try {
			CMDExecute cmdexe = new CMDExecute();
			String[] args = {"/system/bin/cat", "/proc/gt9xx_gesval"};
			result = cmdexe.run(args, "system/bin/");
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		Log.e("liup","result1 = " + result.substring(0,4));
		Cursor cursor = mContentResolver.query(queryUri, null,"item" + "=?",new String[] { result.substring(0,4) }, null);
		if (cursor != null && cursor.moveToFirst()) {
			int onoff = cursor.getInt(cursor.getColumnIndex("onoff"));
			String item = cursor.getString(cursor.getColumnIndex("item"));	
			String packagename = cursor.getString(cursor.getColumnIndex("packagename"));
			Log.e("liup", "item=" + item + " packagename =" + packagename + " onoff= " + onoff);
			if(onoff == 1){
				if(packagename.equals("mPreviousMusic")){
					Intent service = new Intent();
					//service.setClassName("com.android.music", "com.android.music.MediaPlaybackService");
					service.setClassName("com.prize.music", "com.prize.music.service.ApolloService");
					mContext.startService(service);
		
					//Intent intent = new Intent("com.android.music.musicservicecommand");
					Intent intent = new Intent("com.andrew.apolloMod.musicservicecommand");
					intent.putExtra("command", "previous");
					mContext.sendBroadcast(intent);
				}else if(packagename.equals("mPlayPauseMusic")){
					Intent service = new Intent();
					//service.setClassName("com.android.music", "com.android.music.MediaPlaybackService");
					service.setClassName("com.prize.music", "com.prize.music.service.ApolloService");
					mContext.startService(service);
			
					//Intent intent = new Intent("com.android.music.musicservicecommand");
					Intent intent = new Intent("com.andrew.apolloMod.musicservicecommand");
					intent.putExtra("command", "togglepause");
					mContext.sendBroadcast(intent);
				}else if(packagename.equals("mNextMusic")){
					Intent service = new Intent();
					//service.setClassName("com.android.music", "com.android.music.MediaPlaybackService");
					service.setClassName("com.prize.music", "com.prize.music.service.ApolloService");
					mContext.startService(service);
			
					//Intent intent = new Intent("com.android.music.musicservicecommand");
					Intent intent = new Intent("com.andrew.apolloMod.musicservicecommand");
					intent.putExtra("command", "next");
					mContext.sendBroadcast(intent);
				}else {	
					if(packagename.equals("awake") || mKeyguardSecure){
						mSleepGestureWakeLock.acquire();
						mSleepGestureWakeLock.release();
						return;
					}
					 mHandler.post(new Runnable() {
						@Override
						public void run() {
							mKeyguardDelegate.keyguardDone(false, true);
						}
					});
					
					Intent intent = new Intent();
					intent.putExtra("gesture",result.substring(0,4));
					intent.setClassName("com.android.settings", "com.android.settings.PrizeAnimationActivity");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					mContext.startActivityAsUser(intent, UserHandle.CURRENT);
					
				}
			}
		}	
		if(cursor != null) {
			cursor.close();
		}		
	}
    private void notifyScreenshotError() {
        // If the service process is killed, then ask it to clean up after itself
        final ComponentName errorComponent = new ComponentName(SYSUI_PACKAGE,
                SYSUI_SCREENSHOT_ERROR_RECEIVER);
        Intent errorIntent = new Intent(Intent.ACTION_USER_PRESENT);
        errorIntent.setComponent(errorComponent);
        errorIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT |
                Intent.FLAG_RECEIVER_FOREGROUND);
        mContext.sendBroadcastAsUser(errorIntent, UserHandle.CURRENT);
    }

	/*prize-add by lihuangyuan,for faceid-2017-10-26-start*/
	private void sendPowerKeyScreenOnBroadcast(int screenonoff)
	{
		/*Intent intent = new Intent("com.prize.faceid.service");
		mContext.sendBroadcast(intent);*/
		String faceCompany = "sensetime";
		ComponentName cn ;
		if(faceCompany.equals("sensetime")){
			cn = new ComponentName("com.prize.faceunlock",
                "com.sensetime.faceunlock.service.PrizeFaceDetectService");
		}else{
		    cn = new ComponentName("com.android.settings",
                "com.android.settings.face.FaceShowService");
		}
				
        Intent intent = new Intent();
        intent.putExtra("screen_onoff", screenonoff);
        intent.setComponent(cn);
        /*intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        mContext.startActivity(intent);*/
        mContext.startService(intent);
	}
	/*prize-add by lihuangyuan,for faceid-2017-10-26-end*/
    /** {@inheritDoc} */
    @Override
    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        if (!mSystemBooted) {
            // If we have not yet booted, don't let key events do anything.
            return 0;
        }

        /// M: If USP service freeze display, disable power key
        if (interceptKeyBeforeHandling(event)) {
            return 0;
        }
		/*PRIZE-add for changesimcard reset dialog-lihuangyuan-2017-06-15-start*/
		if(PrizeOption.PRIZE_CHANGESIM_RESET_DIALOG && KeyEvent.KEYCODE_HOME == event.getKeyCode())
		{
			int prizeresetmode = Settings.System.getInt(mContext.getContentResolver(),"prizeresetmode",0);
			if(prizeresetmode == 1)
			{
				Log.d(TAG, "interceptKeyBeforeQueueing prizeresetmode:"+prizeresetmode);
				return 0;
			}
		}
		/*PRIZE-add for changesimcard reset dialog-lihuangyuan-2017-06-15-end*/

        /// M: power-off alarm, disable power_key @{
        if (KeyEvent.KEYCODE_POWER == event.getKeyCode() && mIsAlarmBoot) {
            return 0;
        }
        /// @}

        /// M: IPO migration @{
        synchronized (mKeyDispatchLock) {
            if (KEY_DISPATCH_MODE_ALL_DISABLE == mKeyDispatcMode) {
                return 0;
            }
        }
        /// @}
        final boolean interactive = (policyFlags & FLAG_INTERACTIVE) != 0;
        final boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        final boolean canceled = event.isCanceled();
        final int keyCode = event.getKeyCode();

        final boolean isInjected = (policyFlags & WindowManagerPolicy.FLAG_INJECTED) != 0;

        // If screen is off then we treat the case where the keyguard is open but hidden
        // the same as if it were open and in front.
        // This will prevent any keys other than the power button from waking the screen
        // when the keyguard is hidden by another activity.
        final boolean keyguardActive = (mKeyguardDelegate == null ? false :
                                            (interactive ?
                                                isKeyguardShowingAndNotOccluded() :
                                                mKeyguardDelegate.isShowing()));
        /// M: Remove this log.
        if (false && DEBUG_INPUT) {
            Log.d(TAG, "interceptKeyTq keycode=" + keyCode
                    + " interactive=" + interactive + " keyguardActive=" + keyguardActive
                    + " policyFlags=" + Integer.toHexString(policyFlags));
        }

		//add by liup 20150519 for sleepgesture start	
		if(PrizeOption.PRIZE_SLEEP_GESTURE){
			if(keyCode == GESTURE_KEY_CODE && down){
                    prizeSleepGesture();
                }
		}
		//add by liup 20150519 for sleepgesture end
        // Basic policy based on interactive state.
        int result;
        boolean isWakeKey = (policyFlags & WindowManagerPolicy.FLAG_WAKE) != 0
                || event.isWakeKey();
        if (interactive || (isInjected && !isWakeKey)) {
            // When the device is interactive or the key is injected pass the
            // key to the application.
            result = ACTION_PASS_TO_USER;
            isWakeKey = false;

            if (interactive) {
                // If the screen is awake, but the button pressed was the one that woke the device
                // then don't pass it to the application
                if (keyCode == mPendingWakeKey && !down) {
                    result = 0;
                }
                // Reset the pending key
                mPendingWakeKey = PENDING_KEY_NULL;
            }
        } else if (!interactive && shouldDispatchInputWhenNonInteractive(event)) {
            // If we're currently dozing with the screen on and the keyguard showing, pass the key
            // to the application but preserve its wake key status to make sure we still move
            // from dozing to fully interactive if we would normally go from off to fully
            // interactive.
            result = ACTION_PASS_TO_USER;
            // Since we're dispatching the input, reset the pending key
            mPendingWakeKey = PENDING_KEY_NULL;
        } else {
            // When the screen is off and the key is not injected, determine whether
            // to wake the device but don't pass the key to the application.
            result = 0;
            if (isWakeKey && (!down || !isWakeKeyWhenScreenOff(keyCode))) {
                isWakeKey = false;
            }
            // Cache the wake key on down event so we can also avoid sending the up event to the app
            if (isWakeKey && down) {
                mPendingWakeKey = keyCode;
            }
        }

        // If the key would be handled globally, just return the result, don't worry about special
        // key processing.
        if (isValidGlobalKey(keyCode)
                && mGlobalKeyManager.shouldHandleGlobalKey(keyCode, event)) {
            if (isWakeKey) {
                wakeUp(event.getEventTime(), mAllowTheaterModeWakeFromKey, "android.policy:KEY");
            }
            return result;
        }

        boolean useHapticFeedback = down
                && (policyFlags & WindowManagerPolicy.FLAG_VIRTUAL) != 0
                && event.getRepeatCount() == 0;

        /// M: Add more log at WMS
        if (true || false == IS_USER_BUILD) {
            Log.d(TAG, "interceptKeyTq keycode=" + keyCode
                + " interactive=" + interactive + " keyguardActive=" + keyguardActive
                + " policyFlags=" + Integer.toHexString(policyFlags)
                + " down =" + down + " canceled = " + canceled
                + " isWakeKey=" + isWakeKey
                + " mVolumeDownKeyTriggered =" + mScreenshotChordVolumeDownKeyTriggered
                + " mVolumeUpKeyTriggered =" + mScreenshotChordVolumeUpKeyTriggered
                + " result = " + result
                + " useHapticFeedback = " + useHapticFeedback
                + " isInjected = " + isInjected);
        }

        // Handle special keys.
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                if (down) {
                    mBackKeyHandled = false;
                    if (hasLongPressOnBackBehavior()) {
                        Message msg = mHandler.obtainMessage(MSG_BACK_LONG_PRESS);
                        msg.setAsynchronous(true);
                        mHandler.sendMessageDelayed(msg,
                                ViewConfiguration.get(mContext).getDeviceGlobalActionKeyTimeout());
                    }
                } else {
                    boolean handled = mBackKeyHandled;

                    // Reset back key state
                    cancelPendingBackKeyAction();

                    // Don't pass back press to app if we've already handled it
                    if (handled) {
                        result &= ~ACTION_PASS_TO_USER;
                    }
                }
                break;
            }

            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_MUTE: {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    if (down) {
                        if (interactive && !mScreenshotChordVolumeDownKeyTriggered
                                && (event.getFlags() & KeyEvent.FLAG_FALLBACK) == 0) {
                            mScreenshotChordVolumeDownKeyTriggered = true;
                            mScreenshotChordVolumeDownKeyTime = event.getDownTime();
                            mScreenshotChordVolumeDownKeyConsumed = false;
                            cancelPendingPowerKeyAction();
                            interceptScreenshotChord();
                        }
                    } else {
                        mScreenshotChordVolumeDownKeyTriggered = false;
                        cancelPendingScreenshotChordAction();
                    }
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    /// M: Key remapping
                    if ((false == IS_USER_BUILD)
                                && SystemProperties.get("persist.sys.anr_sys_key").equals("1")) {
                        mHandler.postDelayed(mKeyRemappingVolumeDownLongPress_Test, 0);
                    }
                    if (down) {
                        if (interactive && !mScreenshotChordVolumeUpKeyTriggered
                                && (event.getFlags() & KeyEvent.FLAG_FALLBACK) == 0) {
                            mScreenshotChordVolumeUpKeyTriggered = true;
                            cancelPendingPowerKeyAction();
                            cancelPendingScreenshotChordAction();
                        }
                    } else {
                        mScreenshotChordVolumeUpKeyTriggered = false;
                        cancelPendingScreenshotChordAction();
                    }
                }
                if (down) {
                    TelecomManager telecomManager = getTelecommService();
                    if (telecomManager != null) {
                        if (telecomManager.isRinging()) {
                            // If an incoming call is ringing, either VOLUME key means
                            // "silence ringer".  We handle these keys here, rather than
                            // in the InCallScreen, to make sure we'll respond to them
                            // even if the InCallScreen hasn't come to the foreground yet.
                            // Look for the DOWN event here, to agree with the "fallback"
                            // behavior in the InCallScreen.
                            Log.i(TAG, "interceptKeyBeforeQueueing:"
                                  + " VOLUME key-down while ringing: Silence ringer!");

                            // Silence the ringer.  (It's safe to call this
                            // even if the ringer has already been silenced.)
                            telecomManager.silenceRinger();

                            // And *don't* pass this key thru to the current activity
                            // (which is probably the InCallScreen.)
                            result &= ~ACTION_PASS_TO_USER;
                            break;
                        }
                        if (telecomManager.isInCall()
                                && (result & ACTION_PASS_TO_USER) == 0) {
                            // If we are in call but we decided not to pass the key to
                            // the application, just pass it to the session service.

                            MediaSessionLegacyHelper.getHelper(mContext)
                                    .sendVolumeKeyEvent(event, false);
                            break;
                        }
                    }
                }
                if (mUseTvRouting) {
                    // On TVs, defer special key handlings to
                    // {@link interceptKeyBeforeDispatching()}.
                    result |= ACTION_PASS_TO_USER;
                } else if ((result & ACTION_PASS_TO_USER) == 0) {
                    // If we aren't passing to the user and no one else
                    // handled it send it to the session manager to
                    // figure out.
                    MediaSessionLegacyHelper.getHelper(mContext)
                            .sendVolumeKeyEvent(event, true);
                }
                break;
            }

            case KeyEvent.KEYCODE_ENDCALL: {
                result &= ~ACTION_PASS_TO_USER;
                if (down) {
                    TelecomManager telecomManager = getTelecommService();
                    boolean hungUp = false;
                    if (telecomManager != null) {
                        hungUp = telecomManager.endCall();
                    }
                    if (interactive && !hungUp) {
                        mEndCallKeyHandled = false;
                        mHandler.postDelayed(mEndCallLongPress,
                                ViewConfiguration.get(mContext).getDeviceGlobalActionKeyTimeout());
                    } else {
                        mEndCallKeyHandled = true;
                    }
                } else {
                    if (!mEndCallKeyHandled) {
                        mHandler.removeCallbacks(mEndCallLongPress);
                        if (!canceled) {
                            if ((mEndcallBehavior
                                    & Settings.System.END_BUTTON_BEHAVIOR_HOME) != 0) {
                                if (goHome()) {
                                    break;
                                }
                            }
                            if ((mEndcallBehavior
                                    & Settings.System.END_BUTTON_BEHAVIOR_SLEEP) != 0) {
                                mPowerManager.goToSleep(event.getEventTime(),
                                        PowerManager.GO_TO_SLEEP_REASON_POWER_BUTTON, 0);
                                isWakeKey = false;
                            }
                        }
                    }
                }
                break;
            }

            case KeyEvent.KEYCODE_POWER: {
                result &= ~ACTION_PASS_TO_USER;
                isWakeKey = false; // wake-up will be handled separately
                if (down) {
                    interceptPowerKeyDown(event, interactive);
                } else {
                    interceptPowerKeyUp(event, interactive, canceled);
                }
                break;
            }

            case KeyEvent.KEYCODE_SYSTEM_NAVIGATION_DOWN:
                // fall through
            case KeyEvent.KEYCODE_SYSTEM_NAVIGATION_UP:
                // fall through
            case KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT:
                // fall through
            case KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT: {
                result &= ~ACTION_PASS_TO_USER;
                interceptSystemNavigationKey(event);
                break;
            }

            case KeyEvent.KEYCODE_SLEEP: {
                result &= ~ACTION_PASS_TO_USER;
                isWakeKey = false;
                if (!mPowerManager.isInteractive()) {
                    useHapticFeedback = false; // suppress feedback if already non-interactive
                }
                if (down) {
                    sleepPress(event.getEventTime());
                } else {
                    sleepRelease(event.getEventTime());
                }
                break;
            }

            case KeyEvent.KEYCODE_SOFT_SLEEP: {
                result &= ~ACTION_PASS_TO_USER;
                isWakeKey = false;
                if (!down) {
                    mPowerManagerInternal.setUserInactiveOverrideFromWindowManager();
                }
                break;
            }

            case KeyEvent.KEYCODE_WAKEUP: {
                result &= ~ACTION_PASS_TO_USER;
                isWakeKey = true;
                break;
            }

            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MUTE:
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_MEDIA_RECORD:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK: {
                if (MediaSessionLegacyHelper.getHelper(mContext).isGlobalPriorityActive()) {
                    // If the global session is active pass all media keys to it
                    // instead of the active window.
                    result &= ~ACTION_PASS_TO_USER;
                }
                if ((result & ACTION_PASS_TO_USER) == 0) {
                    // Only do this if we would otherwise not pass it to the user. In that
                    // case, the PhoneWindow class will do the same thing, except it will
                    // only do it if the showing app doesn't process the key on its own.
                    // Note that we need to make a copy of the key event here because the
                    // original key event will be recycled when we return.
                    mBroadcastWakeLock.acquire();
                    Message msg = mHandler.obtainMessage(MSG_DISPATCH_MEDIA_KEY_WITH_WAKE_LOCK,
                            new KeyEvent(event));
                    msg.setAsynchronous(true);
                    msg.sendToTarget();
                }
                break;
            }

            case KeyEvent.KEYCODE_CALL: {
                if (down) {
                    TelecomManager telecomManager = getTelecommService();
                    if (telecomManager != null) {
                        if (telecomManager.isRinging()) {
                            Log.i(TAG, "interceptKeyBeforeQueueing:"
                                  + " CALL key-down while ringing: Answer the call!");
                            telecomManager.acceptRingingCall();

                            // And *don't* pass this key thru to the current activity
                            // (which is presumably the InCallScreen.)
                            result &= ~ACTION_PASS_TO_USER;
                        }
                    }
                }
                break;
            }
            case KeyEvent.KEYCODE_VOICE_ASSIST: {
                // Only do this if we would otherwise not pass it to the user. In that case,
                // interceptKeyBeforeDispatching would apply a similar but different policy in
                // order to invoke voice assist actions. Note that we need to make a copy of the
                // key event here because the original key event will be recycled when we return.
                if ((result & ACTION_PASS_TO_USER) == 0 && !down) {
                    mBroadcastWakeLock.acquire();
                    Message msg = mHandler.obtainMessage(MSG_LAUNCH_VOICE_ASSIST_WITH_WAKE_LOCK,
                            keyguardActive ? 1 : 0, 0);
                    msg.setAsynchronous(true);
                    msg.sendToTarget();
                }
                break;
            }
            case KeyEvent.KEYCODE_WINDOW: {
                if (mShortPressWindowBehavior == SHORT_PRESS_WINDOW_PICTURE_IN_PICTURE) {
                    if (mTvPictureInPictureVisible) {
                        // Consumes the key only if picture-in-picture is visible
                        // to show picture-in-picture control menu.
                        // This gives a chance to the foreground activity
                        // to customize PIP key behavior.
                        if (!down) {
                            showTvPictureInPictureMenu(event);
                        }
                        result &= ~ACTION_PASS_TO_USER;
                    }
                }
                break;
            }
        }

        if (useHapticFeedback) {
            performHapticFeedbackLw(null, HapticFeedbackConstants.VIRTUAL_KEY, false);
        }

        if (isWakeKey) {
            wakeUp(event.getEventTime(), mAllowTheaterModeWakeFromKey, "android.policy:KEY");
        }

        return result;
    }

    /**
     * Handle statusbar expansion events.
     * @param event
     */
    private void interceptSystemNavigationKey(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && areSystemNavigationKeysEnabled()) {
            IStatusBarService sbar = getStatusBarService();
            if (sbar != null) {
                try {
                    sbar.handleSystemNavigationKey(event.getKeyCode());
                } catch (RemoteException e1) {
                    // oops, no statusbar. Ignore event.
                }
            }
        }
    }

    /**
     * Returns true if the key can have global actions attached to it.
     * We reserve all power management keys for the system since they require
     * very careful handling.
     */
    private static boolean isValidGlobalKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_POWER:
            case KeyEvent.KEYCODE_WAKEUP:
            case KeyEvent.KEYCODE_SLEEP:
                return false;
            default:
                return true;
        }
    }

    /**
     * When the screen is off we ignore some keys that might otherwise typically
     * be considered wake keys.  We filter them out here.
     *
     * {@link KeyEvent#KEYCODE_POWER} is notably absent from this list because it
     * is always considered a wake key.
     */
    private boolean isWakeKeyWhenScreenOff(int keyCode) {
        switch (keyCode) {
            // ignore volume keys unless docked
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                return mDockMode != Intent.EXTRA_DOCK_STATE_UNDOCKED;

            // ignore media and camera keys
            case KeyEvent.KEYCODE_MUTE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_MEDIA_RECORD:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK:
            case KeyEvent.KEYCODE_CAMERA:
                return false;
        }
        return true;
    }


    /** {@inheritDoc} */
    @Override
    public int interceptMotionBeforeQueueingNonInteractive(long whenNanos, int policyFlags) {
        if ((policyFlags & FLAG_WAKE) != 0) {
            if (wakeUp(whenNanos / 1000000, mAllowTheaterModeWakeFromMotion,
                    "android.policy:MOTION")) {
                return 0;
            }
        }

        if (shouldDispatchInputWhenNonInteractive(null)) {
            return ACTION_PASS_TO_USER;
        }

        // If we have not passed the action up and we are in theater mode without dreaming,
        // there will be no dream to intercept the touch and wake into ambient.  The device should
        // wake up in this case.
        if (isTheaterModeEnabled() && (policyFlags & FLAG_WAKE) != 0) {
            wakeUp(whenNanos / 1000000, mAllowTheaterModeWakeFromMotionWhenNotDreaming,
                    "android.policy:MOTION");
        }

        return 0;
    }

    /* Dynamically hiding nav bar feature. prize-linkh-20150805 */
    //called by InputMonitor class.
    //prize tangzhengrong 20180526 Swipe-up Gesture Navigation bar start
    private boolean hasAllowDispatchInDownEvent = false;
    private boolean hasAllowDispatchInDownEventForGestureNavigation = false;
    @Override
    public int interceptMotionBeforeQueueingInteractive(MotionEvent event, int policyFlags) {
        if(DEBUG_FOR_HIDING_NAVBAR) {
            Slog.d(LOG_TAG, "interceptMotionBeforeQueueingInteractive(). action=" + event.actionToString(event.getActionMasked()));
        }
        
        if(mSystemBooted && isScreenOn()) {
            boolean allowDispatch = false;
            boolean allowDispatchForGestureNavigation = false;
            /*
            if(!mSystemGestures.mIsProcessingEvent) {
                if(mNavigationBar != null && mNavigationBar.isVisibleLw()) {
                    //Slog.d(LOG_TAG, "Allow queueing because of visible nav bar!!!");
                    allowDispatch = true;
                }
                
                if(isKeyguardShowingAndNotOccluded()) {
                    allowDispatch = true;
                }
            }*/
            
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                hasAllowDispatchInDownEvent = false;
                hasAllowDispatchInDownEventForGestureNavigation = false;
                if(mNavigationBar != null && mNavigationBar.isVisibleLw()) {
                    if(DEBUG_FOR_HIDING_NAVBAR) {
                        Slog.d(LOG_TAG, "Allow queueing because of visible nav bar!!!");
                    }
                    hasAllowDispatchInDownEvent = true;
                }
                
                if(isKeyguardShowingAndNotOccluded()) {
                    hasAllowDispatchInDownEvent = true;
                }

                /* Nav bar related to mBack key feature. prize-linkh-20160804 */
                // yes. we only care about it when receving first down event.
                // Even status is changed when receiving next motion
                // events, we still keep our steps until next down event happens.
                if (SUPPORT_NAV_BAR_FOR_MBACK_DEVICE && !canShowNavBarFormBack()) {
                    hasAllowDispatchInDownEvent = true;
                }

                if(!SUPPORT_SWIPE_UP_GESTURE_NAVIGATION || !EnableGestureNavigation()){
                    hasAllowDispatchInDownEventForGestureNavigation = true;
                }
            }

            if(hasAllowDispatchInDownEventForGestureNavigation){
                allowDispatchForGestureNavigation = true;
            }

            if(!allowDispatchForGestureNavigation){
                mSwipeUpGestureController.processMotionEvent(event);
                if(mSwipeUpGestureController.isGestureProcess()){
                    return 0;
                }
            }
            
            if(DEBUG_FOR_HIDING_NAVBAR) {
                Slog.d(LOG_TAG, "hasAllowDispatchInDownEvent=" + hasAllowDispatchInDownEvent);
            }
            if(hasAllowDispatchInDownEvent){
                allowDispatch = true;
            }

            //prize tangzhengrong 20180526 Swipe-up Gesture Navigation bar end
            if(DEBUG_FOR_HIDING_NAVBAR) {
                Slog.d(LOG_TAG, "invoking mSystemGestures.onPointerEvent()....");
            }
            mSystemGestures.onPointerEvent(event);
            if(mSystemGestures.mBlocked && !allowDispatch) {
                Slog.d(LOG_TAG, "Disable queueing this motion event!");
                return 0;
            }
        }
        return ACTION_PASS_TO_USER;
    } // END...

    //prize tangzhengrong 20180526 Swipe-up Gesture Navigation bar start
    private class TrackPointData{
        public float x;  
        public float y;
        public long  time;
        public TrackPointData(float x, float y, long time){
            this.x = x;
            this.y = y;
            this.time = time;
        }
    }

    private interface GestureState{
        public void sendGestureFeature();
        public int getState();
    }

    private class BackGestureState implements GestureState{
        private final int state = BACK_GESTURE_STATE;
        private int location;
        public BackGestureState(int location){
            this.location = location;
        }
        public void sendGestureFeature(){
            final Intent intent = new Intent(ACTION_BACK);
            intent.putExtra(EXTRA_LOCATION, location);
            mContext.sendBroadcast(intent);
        }
        public int getState(){
            return state;
        }
    } 
    private class HomeGestureState implements GestureState{
        private final int state = HOME_GESTURE_STATE;
        public HomeGestureState(){
        }
        public void sendGestureFeature(){
            final Intent intent = new Intent(ACTION_HOME);
            mContext.sendBroadcast(intent);
        }
        public int getState(){
            return state;
        }
    }
    private class RecentTasksGestureState implements GestureState{
        private final int state = RECENT_TASKS_GESTURE_STATE;
        public RecentTasksGestureState(){
        }
        public void sendGestureFeature(){
            final Intent intent = new Intent(ACTION_RECENT_TASKS);
            mContext.sendBroadcast(intent);
        }
        public int getState(){
            return state;
        }
    }
  

    private class MotionEventCoordinateProcessor{
        private int currentRotation;
        private int displayHeight;
        private int displayWidth;
        //for adapt vertical and horizontal
        private int bottomHeight;
        private int bottomWidth;
        private int bottomGestureBoundary;
        private int centerBottomLeftBoundary;
        private int centerBottomRightBoundary;
        private WindowManager mWindowManager;
        //check downpoint 
        private final double bottomGestureScale = 0.01;
        //check swipe length
        private final float minGestureLength = 100;

        public MotionEventCoordinateProcessor(){
        
        }

        public int getDownRegion(float x, float y){
            if(mWindowManager == null){
                mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
            }
            if(displayHeight == 0 && displayWidth == 0){
                getSystemDisplay();
            }
            int rotation = mWindowManager.getDefaultDisplay().getRotation();
            if(rotation != currentRotation){
                getSystemDisplay();
            }
            if(rotation == Surface.ROTATION_0){
                if(y <= bottomGestureBoundary || y >= bottomHeight)
                    return NON_BOTTOM;
                if(x >= 0 && x < centerBottomLeftBoundary)
                    return LEFT_BOTTOM;
                if(x >= centerBottomLeftBoundary && x <= centerBottomRightBoundary)
                    return CENTER_BOTTOM;
                if(x > centerBottomRightBoundary && x <= bottomWidth)
                    return RIGHT_BOTTOM;                
            }else if(rotation == Surface.ROTATION_90){
                if(x <= bottomGestureBoundary || x >= bottomWidth)
                    return NON_BOTTOM;
                if(y >= 0 && y < centerBottomLeftBoundary)
                    return LEFT_BOTTOM;
                if(y >= centerBottomLeftBoundary && y <= centerBottomRightBoundary)
                    return CENTER_BOTTOM;
                if(y > centerBottomRightBoundary && y <= bottomHeight)
                    return RIGHT_BOTTOM;
            }else if(rotation == Surface.ROTATION_270){
                if(x < 0 || x >= bottomGestureBoundary)
                    return NON_BOTTOM;
                if(y >= 0 && y < centerBottomLeftBoundary)
                    return LEFT_BOTTOM;
                if(y >= centerBottomLeftBoundary && y <= centerBottomRightBoundary)
                    return CENTER_BOTTOM;
                if(y > centerBottomRightBoundary && y <= bottomHeight)
                    return RIGHT_BOTTOM;
            }
            return NON_BOTTOM; 
        }
        private void getSystemDisplay(){
            DisplayMetrics mDisplayMetrics = mContext.getResources().getDisplayMetrics();
            mWindowManager.getDefaultDisplay().getRealMetrics(mDisplayMetrics);
            //displayHeight = mDisplayMetrics.heightPixels;
            //displayWidth = mDisplayMetrics.widthPixels;
            
            currentRotation = mWindowManager.getDefaultDisplay().getRotation();
            displayHeight = mDisplayMetrics.heightPixels;
            displayWidth = mDisplayMetrics.widthPixels;

            /* prize-add-LiuHai,  ROTATION_90 Gesture Navigation bar invalid -liyongli-20180727-start
             *  because  frameworks/base/core/java/android/view/DisplayInfo.java  getMetricsWithSize()
             *  is modify     outMetrics.widthPixels -=87
             * so here  displayWidth mast add the notch width
            */
            //prize-removed by lihuangyuan,for ROTATION_90 Gesture Navigation bar invalid-2018-08-14-start
            //see to DisplayInfo.java  getMetricsWithSize() modify has removed
            /*if( PrizeOption.PRIZE_NOTCH_SCREEN  && currentRotation == Surface.ROTATION_90){
                displayWidth += mStatusBarHeight;//87;
            }*/
            //prize-removed by lihuangyuan,for ROTATION_90 Gesture Navigation bar invalid-2018-08-14-end
            /* prize-add-LiuHai,  ROTATION_90 Gesture Navigation bar invalid -liyongli-20180727-end*/

            bottomHeight = displayHeight;
            bottomWidth = displayWidth;
            if(currentRotation == Surface.ROTATION_0){
                bottomGestureBoundary = (int)(bottomHeight - bottomHeight * bottomGestureScale);
                //screen is divided into 3 part
                centerBottomLeftBoundary = bottomWidth / 3;
                centerBottomRightBoundary = bottomWidth * 2 / 3;                
            }else if(currentRotation == Surface.ROTATION_90){
                bottomGestureBoundary = (int)(bottomWidth - bottomWidth * bottomGestureScale);
                //screen is divided into 3 part
                centerBottomLeftBoundary = bottomHeight / 3;
                centerBottomRightBoundary = bottomHeight * 2 / 3;
            }else if(currentRotation == Surface.ROTATION_270){
                bottomGestureBoundary = (int)(bottomWidth * bottomGestureScale);
                //screen is divided into 3 part
                centerBottomLeftBoundary = bottomHeight / 3;
                centerBottomRightBoundary = bottomHeight * 2 / 3;
            }            
        }
        public boolean CheckGestureSuccess(float startX, float startY, float endX, float endY){
            if(currentRotation == Surface.ROTATION_0)
                return Math.abs(endY - startY) > minGestureLength ? true : false;
            else if(currentRotation == Surface.ROTATION_90 || currentRotation == Surface.ROTATION_270)
                return Math.abs(endX - startX) > minGestureLength ? true : false;
            return false;
        }
    }

    private class SwipeUpGestureController{
        private boolean mEnableSwipeUpGesture; 
        private boolean inSwipeUpGestureProcess;
        private final int GESTURE_TIME_OUT = 400;
        private final int RECENT_TASKS_DELAY_TIME = 500;
        private GestureState mGestureState = null;
        private int mTrackPointId = NO_TRACK_POINT_ID;
        private TrackPointData mStartDownPoint;
        private TrackPointData mlastMovePoint;
        private MotionEvent mTrackEvent;
        private boolean gestureHasComplete;
        private CheckRecentTasksState mPendingCheckRecentTasksState;
        private MotionEventCoordinateProcessor mProcessor = new MotionEventCoordinateProcessor();

        public SwipeUpGestureController(){
        
        }

        public void processMotionEvent(MotionEvent ev){
            switch (ev.getAction() & ev.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    mSwipeUpGestureController.processDownEvents(ev);
                    break;
                }
                case MotionEvent.ACTION_POINTER_DOWN: {
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    mSwipeUpGestureController.processMoveEvents(ev);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    mSwipeUpGestureController.processUpEvents(ev);
                    break;
                }
                case MotionEvent.ACTION_POINTER_UP: {
                    mSwipeUpGestureController.processUpEvents(ev);
                    break;
                }
                case MotionEvent.ACTION_CANCEL: {
                    mSwipeUpGestureController.processCancelEvents(ev);
                    break;
                }
            }
        }

        public void resetGestureStatus(){
            //reset every thing
            mGestureState = null;
            mTrackPointId = NO_TRACK_POINT_ID;
            mStartDownPoint = null;
            gestureHasComplete = false;
            mTrackEvent = null;
            if(mPendingCheckRecentTasksState != null)
                mHandler.removeCallbacks(mPendingCheckRecentTasksState);
            //remove callback
        }

        public boolean isGestureProcess(){
            return inSwipeUpGestureProcess;
        }

        public boolean isGestureComplete(){
            return gestureHasComplete;
        }
        public void setGestureState(GestureState mGestureState){
            this.mGestureState = mGestureState;
        }
        //ev.getAction() & ev.ACTION_MASK = MotionEvent.ACTION_DOWN
        public void processDownEvents(MotionEvent ev){
            inSwipeUpGestureProcess = false;
            float currentEventX = ev.getRawX();
            float currentEventY = ev.getRawY();
            int location = mProcessor.getDownRegion(currentEventX, currentEventY);
            if(location == NON_BOTTOM){
                return ;
            }
            inSwipeUpGestureProcess = true;
            resetGestureStatus();
            //mTrackEvent = MotionEvent.obtain(ev);
            mTrackPointId = ev.getActionIndex();
            long DownTime = System.currentTimeMillis(); 
            mStartDownPoint = new TrackPointData(currentEventX, currentEventY, DownTime);
            if(location == RIGHT_BOTTOM || location == LEFT_BOTTOM){
                BackGestureState mBackState = new BackGestureState(location);
                setGestureState(mBackState);
            }
            if(location == CENTER_BOTTOM){
                HomeGestureState mHomeState = new HomeGestureState();
                //now is preHomeState maybe will touch off RecentTasksState 
                setGestureState(mHomeState);
                if(mPendingCheckRecentTasksState == null)
                    mPendingCheckRecentTasksState = new CheckRecentTasksState();
                mHandler.postDelayed(mPendingCheckRecentTasksState, RECENT_TASKS_DELAY_TIME);
            }
            return;
        }
        //ev.getAction() & ev.ACTION_MASK = MotionEvent.ACTION_MOVE
        public void processMoveEvents(MotionEvent ev){
            if(mTrackPointId == NO_TRACK_POINT_ID)
                return;
             if(mGestureState.getState() == BACK_GESTURE_STATE){
                if((System.currentTimeMillis() - mStartDownPoint.time) < GESTURE_TIME_OUT 
                    && !gestureHasComplete
                    && mProcessor.CheckGestureSuccess(mStartDownPoint.x, mStartDownPoint.y, ev.getRawX(), ev.getRawY())){
                        mGestureState.sendGestureFeature();
                        gestureHasComplete = true;
                        //interceptNextEvents(ev);                        
                }
             }else if(mGestureState.getState() == HOME_GESTURE_STATE){
                long currentMoveTime = System.currentTimeMillis();
                if((currentMoveTime - mStartDownPoint.time) < RECENT_TASKS_DELAY_TIME){
                    mlastMovePoint = new TrackPointData(ev.getRawX(), ev.getRawY(), currentMoveTime);
                }
             }
        }  
        //ev.getAction() & ev.ACTION_MASK = MotionEvent.ACTION_UP
        public void processUpEvents(MotionEvent ev){
            if(mTrackPointId == NO_TRACK_POINT_ID)
                return;
            if(ev.getActionIndex() == mTrackPointId){
                inSwipeUpGestureProcess = false;
                if(!gestureHasComplete){
                    if((System.currentTimeMillis() - mStartDownPoint.time) < GESTURE_TIME_OUT 
                    && mProcessor.CheckGestureSuccess(mStartDownPoint.x, mStartDownPoint.y, ev.getRawX(), ev.getRawY())){
                        mGestureState.sendGestureFeature();
                        gestureHasComplete = true;
                        //interceptNextEvents(ev);
                    }
                }
                if(mPendingCheckRecentTasksState != null)
                    mHandler.removeCallbacks(mPendingCheckRecentTasksState);
            }
            if(ev.getPointerCount() == 1)
                resetGestureStatus();            
        }
        //ev.getAction() & ev.ACTION_MASK = MotionEvent.ACTION_CANCEL
        public void processCancelEvents(MotionEvent ev){
            if(ev.getActionIndex() == mTrackPointId){
                inSwipeUpGestureProcess = false;
            }
        }
        
        public final class CheckRecentTasksState implements Runnable{
            @Override
            public void run() {
                if(mStartDownPoint == null || mlastMovePoint == null)
                    return;
                if(mProcessor.CheckGestureSuccess(mStartDownPoint.x, mStartDownPoint.y, mlastMovePoint.x, mlastMovePoint.y)){
                    RecentTasksGestureState mRecentTasksGestureState = new RecentTasksGestureState();
                    setGestureState(mRecentTasksGestureState);
                    mGestureState.sendGestureFeature();
                    gestureHasComplete = true;
                    //interceptNextEvents(mTrackEvent);
                }
            }

        }

        /*protected void destroy(){
            mContext.getContentResolver().unregisterContentObserver(mEnableGestureFeatureObserver);
        }

        @Override
        protected void finalize() throws java.lang.Throwable{
            destroy();
            super.finalize();
        }*/
    }

    //prize tangzhengrong 20180526 Swipe-up Gesture Navigation bar end


    
    private boolean shouldDispatchInputWhenNonInteractive(KeyEvent event) {
        final boolean displayOff = (mDisplay == null || mDisplay.getState() == Display.STATE_OFF);

        if (displayOff && !mHasFeatureWatch) {
            return false;
        }

        // Send events to keyguard while the screen is on and it's showing.
        if (isKeyguardShowingAndNotOccluded() && !displayOff) {
            return true;
        }

        // Watches handle BACK specially
        if (mHasFeatureWatch
                && event != null
                && (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                        || event.getKeyCode() == KeyEvent.KEYCODE_STEM_PRIMARY)) {
            return false;
        }

        // Send events to a dozing dream even if the screen is off since the dream
        // is in control of the state of the screen.
        IDreamManager dreamManager = getDreamManager();

        try {
            if (dreamManager != null && dreamManager.isDreaming()) {
                return true;
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "RemoteException when checking if dreaming", e);
        }

        // Otherwise, consume events since the user can't see what is being
        // interacted with.
        return false;
    }

    private void dispatchDirectAudioEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return;
        }
        int keyCode = event.getKeyCode();
        int flags = AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND
                | AudioManager.FLAG_FROM_KEY;
        String pkgName = mContext.getOpPackageName();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                try {
                    getAudioService().adjustSuggestedStreamVolume(AudioManager.ADJUST_RAISE,
                            AudioManager.USE_DEFAULT_STREAM_TYPE, flags, pkgName, TAG);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error dispatching volume up in dispatchTvAudioEvent.", e);
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                try {
                    getAudioService().adjustSuggestedStreamVolume(AudioManager.ADJUST_LOWER,
                            AudioManager.USE_DEFAULT_STREAM_TYPE, flags, pkgName, TAG);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error dispatching volume down in dispatchTvAudioEvent.", e);
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                try {
                    if (event.getRepeatCount() == 0) {
                        getAudioService().adjustSuggestedStreamVolume(
                                AudioManager.ADJUST_TOGGLE_MUTE,
                                AudioManager.USE_DEFAULT_STREAM_TYPE, flags, pkgName, TAG);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error dispatching mute in dispatchTvAudioEvent.", e);
                }
                break;
        }
    }

    void dispatchMediaKeyWithWakeLock(KeyEvent event) {
        if (DEBUG_INPUT) {
            Slog.d(TAG, "dispatchMediaKeyWithWakeLock: " + event);
        }

        if (mHavePendingMediaKeyRepeatWithWakeLock) {
            if (DEBUG_INPUT) {
                Slog.d(TAG, "dispatchMediaKeyWithWakeLock: canceled repeat");
            }

            mHandler.removeMessages(MSG_DISPATCH_MEDIA_KEY_REPEAT_WITH_WAKE_LOCK);
            mHavePendingMediaKeyRepeatWithWakeLock = false;
            mBroadcastWakeLock.release(); // pending repeat was holding onto the wake lock
        }

        dispatchMediaKeyWithWakeLockToAudioService(event);

        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            mHavePendingMediaKeyRepeatWithWakeLock = true;

            Message msg = mHandler.obtainMessage(
                    MSG_DISPATCH_MEDIA_KEY_REPEAT_WITH_WAKE_LOCK, event);
            msg.setAsynchronous(true);
            mHandler.sendMessageDelayed(msg, ViewConfiguration.getKeyRepeatTimeout());
        } else {
            mBroadcastWakeLock.release();
        }
    }

    void dispatchMediaKeyRepeatWithWakeLock(KeyEvent event) {
        mHavePendingMediaKeyRepeatWithWakeLock = false;

        KeyEvent repeatEvent = KeyEvent.changeTimeRepeat(event,
                SystemClock.uptimeMillis(), 1, event.getFlags() | KeyEvent.FLAG_LONG_PRESS);
        if (DEBUG_INPUT) {
            Slog.d(TAG, "dispatchMediaKeyRepeatWithWakeLock: " + repeatEvent);
        }

        dispatchMediaKeyWithWakeLockToAudioService(repeatEvent);
        mBroadcastWakeLock.release();
    }

    void dispatchMediaKeyWithWakeLockToAudioService(KeyEvent event) {
        if (ActivityManagerNative.isSystemReady()) {
            MediaSessionLegacyHelper.getHelper(mContext).sendMediaButtonEvent(event, true);
        }
    }

    void launchVoiceAssistWithWakeLock(boolean keyguardActive) {
        IDeviceIdleController dic = IDeviceIdleController.Stub.asInterface(
                ServiceManager.getService(Context.DEVICE_IDLE_CONTROLLER));
        if (dic != null) {
            try {
                dic.exitIdle("voice-search");
            } catch (RemoteException e) {
            }
        }
        Intent voiceIntent =
            new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_SECURE, keyguardActive);
        startActivityAsUser(voiceIntent, UserHandle.CURRENT_OR_SELF);
        mBroadcastWakeLock.release();
    }

    BroadcastReceiver mDockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_DOCK_EVENT.equals(intent.getAction())) {
                mDockMode = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,
                        Intent.EXTRA_DOCK_STATE_UNDOCKED);
            } else {
                try {
                    IUiModeManager uiModeService = IUiModeManager.Stub.asInterface(
                            ServiceManager.getService(Context.UI_MODE_SERVICE));
                    mUiMode = uiModeService.getCurrentModeType();
                } catch (RemoteException e) {
                }
            }
            updateRotation(true);
            synchronized (mLock) {
                updateOrientationListenerLp();
            }
        }
    };

    BroadcastReceiver mDreamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_DREAMING_STARTED.equals(intent.getAction())) {
                Slog.v(TAG, "*** onDreamingStarted");
                if (mKeyguardDelegate != null) {
                    mKeyguardDelegate.onDreamingStarted();
                }
            } else if (Intent.ACTION_DREAMING_STOPPED.equals(intent.getAction())) {
                Slog.v(TAG, "*** onDreamingStopped");
                if (mKeyguardDelegate != null) {
                    mKeyguardDelegate.onDreamingStopped();
                }
            }
        }
    };

    BroadcastReceiver mMultiuserReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_USER_SWITCHED.equals(intent.getAction())) {
                // tickle the settings observer: this first ensures that we're
                // observing the relevant settings for the newly-active user,
                // and then updates our own bookkeeping based on the now-
                // current user.
                mSettingsObserver.onChange(false);

                // force a re-application of focused window sysui visibility.
                // the window may never have been shown for this user
                // e.g. the keyguard when going through the new-user setup flow
                synchronized (mWindowManagerFuncs.getWindowManagerLock()) {
                    mLastSystemUiFlags = 0;
                    updateSystemUiVisibilityLw();
                }
            }
        }
    };

    private final Runnable mHiddenNavPanic = new Runnable() {
        @Override
        public void run() {
            synchronized (mWindowManagerFuncs.getWindowManagerLock()) {
                if (!isUserSetupComplete()) {
                    // Swipe-up for navigation bar is disabled during setup
                    return;
                }
                mPendingPanicGestureUptime = SystemClock.uptimeMillis();
                if (!isNavBarEmpty(mLastSystemUiFlags)) {
                    mNavigationBarController.showTransient();
                }
            }
        }
    };

    private void requestTransientBars(WindowState swipeTarget) {
        synchronized (mWindowManagerFuncs.getWindowManagerLock()) {
            if (!isUserSetupComplete()) {
                Slog.d(TAG, "onSwipeFromTop() - isUserSetupComplete is false");
                // Swipe-up for navigation bar is disabled during setup
                return;
            }
            Slog.d(TAG, "---------requestTransientBars - isUserSetupComplete is true");
            boolean sb = mStatusBarController.checkShowTransientBarLw();
            boolean nb = mNavigationBarController.checkShowTransientBarLw()
                    && !isNavBarEmpty(mLastSystemUiFlags);
            if (sb || nb) {
                // Don't show status bar when swiping on already visible navigation bar
                if (!nb && swipeTarget == mNavigationBar) {
                    if (DEBUG) Slog.d(TAG, "Not showing transient bar, wrong swipe target");
                    return;
                }
                if (sb) mStatusBarController.showTransient();
                if (nb) mNavigationBarController.showTransient();
                mImmersiveModeConfirmation.confirmCurrentPrompt();
                updateSystemUiVisibilityLw();
            }
        }
    }

    /*--prize --add by lihuangyuan,for write screen state --start--*/
    private void writeScreenState(String state)
       {
               Slog.i(TAG,"writeScreenState "+state);
               File file = new File("/proc/screen_state");
               FileOutputStream os = null;
               try
               {
                       os = new FileOutputStream(file);                        
                       os.write(state.getBytes());
               }
               catch(IOException e)
               {
                       e.printStackTrace();
               }
               finally
               {
                       if(os != null)
                       {
                               try
                               {
                                       os.close();
                               }
                               catch(IOException e)
                               {
                                       e.printStackTrace();
                               }
                       }
               }
       }
    /*--prize --add by lihuangyuan,for write screen state --end--*/
    /*-prize-add by lihuangyuan,for faceid -2018-03-07-start*/
    @Override
    public void setWakeOrSleep(boolean wakeorsleep)
    {
        mIsWakeup = wakeorsleep;
    }
    private boolean mIsWakeup = true;
    @Override	   	
    public void setWakeupReason(int reason)
    {
        mWakeupReason = reason;
    }
    private int mWakeupReason = 0;
    public static final int WAKEUP_REASON_FINGERPRINT = 1;
    /*-prize-add by lihuangyuan,for faceid -2018-03-07-end*/
    // Called on the PowerManager's Notifier thread.
    @Override
    public void startedGoingToSleep(int why) {
        if (DEBUG_WAKEUP) Slog.i(TAG, "Started going to sleep... (why=" + why + ")");
	 synchronized (mLock) {           
	     //writeScreenState("0");	    
        }
	 /*--prize --add by lihuangyuan,for write screen state 2017-03-27--start--*/
	 mHandler.post(new Runnable() {
            @Override
            public void run() {
                //writeScreenState("0\0");
            }
         });
	  /*--prize --add by lihuangyuan,for write screen state 2017-03-27--end--*/
        mCameraGestureTriggeredDuringGoingToSleep = false;
        mGoingToSleep = true;
        if (mKeyguardDelegate != null) {
            mKeyguardDelegate.onStartedGoingToSleep(why);
        }
    }

    // Called on the PowerManager's Notifier thread.
    @Override
    public void finishedGoingToSleep(int why) {
        EventLog.writeEvent(70000, 0);
        if (DEBUG_WAKEUP) Slog.i(TAG, "Finished going to sleep... (why=" + why + ")");
        MetricsLogger.histogram(mContext, "screen_timeout", mLockScreenTimeout / 1000);

        mGoingToSleep = false;

        // We must get this work done here because the power manager will drop
        // the wake lock and let the system suspend once this function returns.
        synchronized (mLock) {
            mAwake = false;
            updateWakeGestureListenerLp();
            updateOrientationListenerLp();
            updateLockScreenTimeout();
        }
        if (mKeyguardDelegate != null) {
            mKeyguardDelegate.onFinishedGoingToSleep(why,
                    mCameraGestureTriggeredDuringGoingToSleep);
        }
        mCameraGestureTriggeredDuringGoingToSleep = false;
		/* prize-add-by-lijimeng-for faceid switch in setting-20180322-start*/
		boolean isOpen = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_FACEID_SWITCH , 1) == 1;
		/* prize-add-by-lijimeng-for faceid switch in setting-20180322-end*/
		if(PrizeOption.PRIZE_FACE_ID && isOpen && !(mAM.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_PINNED)) {
            sendPowerKeyScreenOnBroadcast(0);
        }
    }

    // Called on the PowerManager's Notifier thread.
    @Override
    public void startedWakingUp() {
        EventLog.writeEvent(70000, 1);
        if (DEBUG_WAKEUP) Slog.i(TAG, "Started waking up...");

        // Since goToSleep performs these functions synchronously, we must
        // do the same here.  We cannot post this work to a handler because
        // that might cause it to become reordered with respect to what
        // may happen in a future call to goToSleep.
        synchronized (mLock) {
            mAwake = true;

            updateWakeGestureListenerLp();
            updateOrientationListenerLp();
            updateLockScreenTimeout();
	     /*--prize --add by lihuangyuan,for write screen state 2017-03-27--start--*/
	     //writeScreenState("1");
	     mHandler.post(new Runnable() {
	            @Override
	            public void run() {
	                //writeScreenState("1\0");
	            }
           });
	     /*--prize --add by lihuangyuan,for write screen state 2017-03-27--end--*/
        }
		/* prize-add-by-lijimeng-for faceid switch in setting-20180322-start*/
		boolean isOpen = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_FACEID_SWITCH , 1) == 1;
		/* prize-add-by-lijimeng-for faceid switch in setting-20180322-end*/
		if(PrizeOption.PRIZE_FACE_ID && isOpen && !(mAM.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_PINNED)
                 && mWakeupReason != WAKEUP_REASON_FINGERPRINT)
             {
                sendPowerKeyScreenOnBroadcast(1);
             }

        if (mKeyguardDelegate != null) {
            mKeyguardDelegate.onStartedWakingUp();
        }
    }

    // Called on the PowerManager's Notifier thread.
    @Override
    public void finishedWakingUp() {
        if (DEBUG_WAKEUP) Slog.i(TAG, "Finished waking up...");
    }

    private void wakeUpFromPowerKey(long eventTime) {
		//wangyunhe
        //prizemodify-by-zhongweilin
        int isSystemFlashOn= Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, -1);
        //int isSystemFlashOn = SystemProperties.getInt("persist.sys.prizeflash",0);
        if(isSystemFlashOn==3||isSystemFlashOn == 1){
            closeFlash();
        }	

        wakeUp(eventTime, mAllowTheaterModeWakeFromPowerKey, "android.policy:POWER");
    }

	/*Prize-pull cpu core num and freq when wake up-chenlong-20180405-start*/
	private void perfBoost(int timeout){
		int mPerfHandle = -1;
		if (mPerfService == null) {
			mPerfService = new PerfServiceWrapper(null);
		}
		if (mPerfService != null) {
			mPerfHandle = mPerfService.userRegScn();
			mPerfService.userRegScnConfig(mPerfHandle, 50,99,0,0,0);
			mPerfService.userRegScnConfig(mPerfHandle, 15,1,4,0,0);
			mPerfService.userRegScnConfig(mPerfHandle, 15,0,4,0,0);
			mPerfService.userRegScnConfig(mPerfHandle, 17,1,2500000,0,0);
			mPerfService.userRegScnConfig(mPerfHandle, 17,0,2500000,0,0);
			mPerfService.userRegScnConfig(mPerfHandle, 10,3,0,0,0);
			mPerfService.userEnableTimeoutMsAsync(mPerfHandle, timeout);
		} else {
			Slog.e(TAG, "PerfService is not ready!");
		}
	}
	/*Prize-pull cpu core num and freq when wake up-chenlong-20180405-end*/

    private boolean wakeUp(long wakeTime, boolean wakeInTheaterMode, String reason) {
        final boolean theaterModeEnabled = isTheaterModeEnabled();
        if (!wakeInTheaterMode && theaterModeEnabled) {
            return false;
        }

        if (theaterModeEnabled) {
            Settings.Global.putInt(mContext.getContentResolver(),
                    Settings.Global.THEATER_MODE_ON, 0);
        }

        mPowerManager.wakeUp(wakeTime, reason);
        return true;
    }

    private void finishKeyguardDrawn() {
        synchronized (mLock) {
            if (!mScreenOnEarly || mKeyguardDrawComplete) {
                return; // We are not awake yet or we have already informed of this event.
            }

            mKeyguardDrawComplete = true;
            if (mKeyguardDelegate != null) {
                mHandler.removeMessages(MSG_KEYGUARD_DRAWN_TIMEOUT);
            }
            mWindowManagerDrawComplete = false;
        }

        // ... eventually calls finishWindowsDrawn which will finalize our screen turn on
        // as well as enabling the orientation change logic/sensor.
        mWindowManagerInternal.waitForAllWindowsDrawn(mWindowManagerDrawCallback,
                WAITING_FOR_DRAWN_TIMEOUT);
    }

    // Called on the DisplayManager's DisplayPowerController thread.
    @Override
    public void screenTurnedOff() {
        if (DEBUG_WAKEUP) Slog.i(TAG, "Screen turned off...");

        updateScreenOffSleepToken(true);
        synchronized (mLock) {
            mScreenOnEarly = false;
            mScreenOnFully = false;
            mKeyguardDrawComplete = false;
            mWindowManagerDrawComplete = false;
            mScreenOnListener = null;
            updateOrientationListenerLp();

            if (mKeyguardDelegate != null) {
                mKeyguardDelegate.onScreenTurnedOff();
            }
        }
    }

    // Called on the DisplayManager's DisplayPowerController thread.
    @Override
    public void screenTurningOn(final ScreenOnListener screenOnListener) {
        if (DEBUG_WAKEUP) Slog.i(TAG, "Screen turning on...");

        updateScreenOffSleepToken(false);
        synchronized (mLock) {
            mScreenOnEarly = true;
            mScreenOnFully = false;
            mKeyguardDrawComplete = false;
            mWindowManagerDrawComplete = false;
            mScreenOnListener = screenOnListener;

            if (mKeyguardDelegate != null) {
                mHandler.removeMessages(MSG_KEYGUARD_DRAWN_TIMEOUT);
                mHandler.sendEmptyMessageDelayed(MSG_KEYGUARD_DRAWN_TIMEOUT, 1000);
                mKeyguardDelegate.onScreenTurningOn(mKeyguardDrawnCallback);
            } else {
                if (DEBUG_WAKEUP) Slog.d(TAG,
                        "null mKeyguardDelegate: setting mKeyguardDrawComplete.");
                finishKeyguardDrawn();
            }
        }
    }

    // Called on the DisplayManager's DisplayPowerController thread.
    @Override
    public void screenTurnedOn() {
        synchronized (mLock) {
            if (mKeyguardDelegate != null) {
                mKeyguardDelegate.onScreenTurnedOn();
            }
        }
    }

    private void finishWindowsDrawn() {
        synchronized (mLock) {
            if (!mScreenOnEarly || mWindowManagerDrawComplete) {
                return; // Screen is not turned on or we did already handle this case earlier.
            }

            mWindowManagerDrawComplete = true;
        }

        finishScreenTurningOn();
    }

    private void finishScreenTurningOn() {
        synchronized (mLock) {
            // We have just finished drawing screen content. Since the orientation listener
            // gets only installed when all windows are drawn, we try to install it again.
            updateOrientationListenerLp();
        }
        final ScreenOnListener listener;
        final boolean enableScreen;
        synchronized (mLock) {
            if (DEBUG_WAKEUP) Slog.d(TAG,
                    "finishScreenTurningOn: mAwake=" + mAwake
                            + ", mScreenOnEarly=" + mScreenOnEarly
                            + ", mScreenOnFully=" + mScreenOnFully
                            + ", mKeyguardDrawComplete=" + mKeyguardDrawComplete
                            + ", mWindowManagerDrawComplete=" + mWindowManagerDrawComplete);

            if (mScreenOnFully || !mScreenOnEarly || !mWindowManagerDrawComplete
                    || (mAwake && !mKeyguardDrawComplete)) {
                return; // spurious or not ready yet
            }

            if (DEBUG_WAKEUP) Slog.i(TAG, "Finished screen turning on...");
            listener = mScreenOnListener;
            mScreenOnListener = null;
            mScreenOnFully = true;

            // Remember the first time we draw the keyguard so we know when we're done with
            // the main part of booting and can enable the screen and hide boot messages.
            if (!mKeyguardDrawnOnce && mAwake) {
                mKeyguardDrawnOnce = true;
                enableScreen = true;
                if (mBootMessageNeedsHiding) {
                    mBootMessageNeedsHiding = false;
                    hideBootMessages();
                }
            } else {
                enableScreen = false;
            }
        }

        if (listener != null) {
            listener.onScreenOn();
        }

        if (enableScreen) {
            try {
                mWindowManager.enableScreenIfNeeded();
            } catch (RemoteException unhandled) {
            }
        }
    }

    private void handleHideBootMessage() {
        synchronized (mLock) {
            if (!mKeyguardDrawnOnce) {
                mBootMessageNeedsHiding = true;
                return; // keyguard hasn't drawn the first time yet, not done booting
            }
        }

        if (mBootMsgDialog != null) {
            if (DEBUG_WAKEUP) Slog.d(TAG, "handleHideBootMessage: dismissing");
            mBootMsgDialog.dismiss();
            mBootMsgDialog = null;
        }
    }

    @Override
    public boolean isScreenOn() {
        return mScreenOnFully;
    }

    /** {@inheritDoc} */
    @Override
    public void enableKeyguard(boolean enabled) {
        if (mKeyguardDelegate != null) {
            mKeyguardDelegate.setKeyguardEnabled(enabled);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void exitKeyguardSecurely(OnKeyguardExitResult callback) {
        if (mKeyguardDelegate != null) {
            mKeyguardDelegate.verifyUnlock(callback);
        }
    }

    private boolean isKeyguardShowingAndNotOccluded() {
        if (mKeyguardDelegate == null) return false;
        return mKeyguardDelegate.isShowing() && !mKeyguardOccluded;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isKeyguardLocked() {
        return keyguardOn();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isKeyguardSecure(int userId) {
        if (mKeyguardDelegate == null) return false;
        return mKeyguardDelegate.isSecure(userId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isKeyguardShowingOrOccluded() {
        return mKeyguardDelegate == null ? false : mKeyguardDelegate.isShowing();
    }

    /** {@inheritDoc} */
    @Override
    public boolean inKeyguardRestrictedKeyInputMode() {
        if (mKeyguardDelegate == null) return false;
        return mKeyguardDelegate.isInputRestricted();
    }

    @Override
    public void dismissKeyguardLw() {
        if (mKeyguardDelegate != null && mKeyguardDelegate.isShowing()) {
            if (DEBUG_KEYGUARD) Slog.d(TAG, "PWM.dismissKeyguardLw");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // ask the keyguard to prompt the user to authenticate if necessary
					/* prize-add-by-lijimeng-for faceid switch in setting-20180322-start*/
					boolean isOpen = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_FACEID_SWITCH , 1) == 1;
					/* prize-add-by-lijimeng-for faceid switch in setting-20180322-end*/
                    if(PrizeOption.PRIZE_FACE_ID && isOpen && !(mAM.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_PINNED)) {
			    /*-prize-add by lihuangyuan,for faceid -2018-03-07-start*/
			    if(mIsWakeup)
			    {
                            mKeyguardDelegate.keyguardDone(false, true);
			    }
			    /*-prize-add by lihuangyuan,for faceid -2018-03-07-end*/
                    }else {
                    mKeyguardDelegate.dismiss(false /* allowWhileOccluded */);
					}
                }
            });
        }
    }

    @Override
    public void notifyActivityDrawnForKeyguardLw() {
        if (mKeyguardDelegate != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mKeyguardDelegate.onActivityDrawn();
                }
            });
        }
    }

    @Override
    public boolean isKeyguardDrawnLw() {
        synchronized (mLock) {
            return mKeyguardDrawnOnce;
        }
    }

    @Override
    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        if (mKeyguardDelegate != null) {
            if (DEBUG_KEYGUARD) Slog.d(TAG, "PWM.startKeyguardExitAnimation");
            mKeyguardDelegate.startKeyguardExitAnimation(startTime, fadeoutDuration);
        }
    }

    @Override
    public void getStableInsetsLw(int displayRotation, int displayWidth, int displayHeight,
            Rect outInsets) {
        outInsets.setEmpty();

        // Navigation bar and status bar.
        getNonDecorInsetsLw(displayRotation, displayWidth, displayHeight, outInsets);
        if (mStatusBar != null) {
            outInsets.top = mStatusBarHeight;
        }
    }

    @Override
    public void getNonDecorInsetsLw(int displayRotation, int displayWidth, int displayHeight,
            Rect outInsets) {
        outInsets.setEmpty();
        
        /* prize-add-landscape split screen cut lcd ears-liyongli-20180419-start
        *   display Rect exclude the ears Rect
        */        
        if (PrizeOption.PRIZE_NOTCH_SCREEN
            //&& mWindowManagerInternal != null
            //&& mWindowManagerInternal.isStackVisible(DOCKED_STACK_ID) 
        ) {
            if(displayRotation==Surface.ROTATION_90){
                outInsets.left = mStatusBarHeight;
            }else if(displayRotation==Surface.ROTATION_270){
                outInsets.right = mStatusBarHeight;
            }
        }
        /* prize-add-landscape split screen cut lcd ears-liyongli-20180419-end */
        
        // Only navigation bar
        if (mNavigationBar != null) {
            /* prize-add- Dynamically hiding nav bar feature -liyongli-20180418-start 
             *  not set outInsets when nav bar hiding
            */
            if( mSupportHidingNavBar && mNavigationBar.isGoneForLayoutLw() ){
                return;
            }
            /* prize-add- Dynamically hiding nav bar feature -liyongli-20180418-end */
            
            int position = navigationBarPosition(displayWidth, displayHeight, displayRotation);
            if (position == NAV_BAR_BOTTOM) {
                outInsets.bottom = getNavigationBarHeight(displayRotation, mUiMode);
            } else if (position == NAV_BAR_RIGHT) {
                outInsets.right = getNavigationBarWidth(displayRotation, mUiMode);
            } else if (position == NAV_BAR_LEFT) {
                outInsets.left = getNavigationBarWidth(displayRotation, mUiMode);
            }
        }
    }

    @Override
    public boolean isNavBarForcedShownLw(WindowState windowState) {
        return mForceShowSystemBars;
    }

    @Override
    public boolean isDockSideAllowed(int dockSide) {

        // We do not allow all dock sides at which the navigation bar touches the docked stack.
        if (!mNavigationBarCanMove) {
            return dockSide == DOCKED_TOP || dockSide == DOCKED_LEFT || dockSide == DOCKED_RIGHT;
        } else {
            return dockSide == DOCKED_TOP || dockSide == DOCKED_LEFT;
        }
    }

    void sendCloseSystemWindows() {
        PhoneWindow.sendCloseSystemWindows(mContext, null);
    }

    void sendCloseSystemWindows(String reason) {
        PhoneWindow.sendCloseSystemWindows(mContext, reason);
    }

    @Override
    public int rotationForOrientationLw(int orientation, int lastRotation) {
        if (false) {
            Slog.v(TAG, "rotationForOrientationLw(orient="
                        + orientation + ", last=" + lastRotation
                        + "); user=" + mUserRotation + " "
                        + ((mUserRotationMode == WindowManagerPolicy.USER_ROTATION_LOCKED)
                            ? "USER_ROTATION_LOCKED" : "")
                        );
        }

        if (mForceDefaultOrientation) {
            return Surface.ROTATION_0;
        }

        synchronized (mLock) {
            int sensorRotation = mOrientationListener.getProposedRotation(); // may be -1
            if (sensorRotation < 0) {
                sensorRotation = lastRotation;
            }

            final int preferredRotation;
            if (mLidState == LID_OPEN && mLidOpenRotation >= 0) {
                // Ignore sensor when lid switch is open and rotation is forced.
                preferredRotation = mLidOpenRotation;
            } else if (mDockMode == Intent.EXTRA_DOCK_STATE_CAR
                    && (mCarDockEnablesAccelerometer || mCarDockRotation >= 0)) {
                // Ignore sensor when in car dock unless explicitly enabled.
                // This case can override the behavior of NOSENSOR, and can also
                // enable 180 degree rotation while docked.
                preferredRotation = mCarDockEnablesAccelerometer
                        ? sensorRotation : mCarDockRotation;
            } else if ((mDockMode == Intent.EXTRA_DOCK_STATE_DESK
                    || mDockMode == Intent.EXTRA_DOCK_STATE_LE_DESK
                    || mDockMode == Intent.EXTRA_DOCK_STATE_HE_DESK)
                    && (mDeskDockEnablesAccelerometer || mDeskDockRotation >= 0)) {
                // Ignore sensor when in desk dock unless explicitly enabled.
                // This case can override the behavior of NOSENSOR, and can also
                // enable 180 degree rotation while docked.
                preferredRotation = mDeskDockEnablesAccelerometer
                        ? sensorRotation : mDeskDockRotation;
            } else if (mHdmiPlugged && mDemoHdmiRotationLock) {
                // Ignore sensor when plugged into HDMI when demo HDMI rotation lock enabled.
                // Note that the dock orientation overrides the HDMI orientation.
                preferredRotation = mDemoHdmiRotation;
            } else if (mHdmiPlugged && mDockMode == Intent.EXTRA_DOCK_STATE_UNDOCKED
                    && mUndockedHdmiRotation >= 0) {
                // Ignore sensor when plugged into HDMI and an undocked orientation has
                // been specified in the configuration (only for legacy devices without
                // full multi-display support).
                // Note that the dock orientation overrides the HDMI orientation.
                preferredRotation = mUndockedHdmiRotation;
            } else if (mDemoRotationLock) {
                // Ignore sensor when demo rotation lock is enabled.
                // Note that the dock orientation and HDMI rotation lock override this.
                preferredRotation = mDemoRotation;
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LOCKED) {
                // Application just wants to remain locked in the last rotation.
                preferredRotation = lastRotation;
            } else if (!mSupportAutoRotation) {
                // If we don't support auto-rotation then bail out here and ignore
                // the sensor and any rotation lock settings.
                preferredRotation = -1;
            } else if ((mUserRotationMode == WindowManagerPolicy.USER_ROTATION_FREE
                            && (orientation == ActivityInfo.SCREEN_ORIENTATION_USER
                                    || orientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                    || orientation == ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
                                    || orientation == ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                                    || orientation == ActivityInfo.SCREEN_ORIENTATION_FULL_USER))
                    || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    || orientation == ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                    || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT) {
                // Otherwise, use sensor only if requested by the application or enabled
                // by default for USER or UNSPECIFIED modes.  Does not apply to NOSENSOR.
                if (mAllowAllRotations < 0) {
                    // Can't read this during init() because the context doesn't
                    // have display metrics at that time so we cannot determine
                    // tablet vs. phone then.
                    mAllowAllRotations = mContext.getResources().getBoolean(
                            com.android.internal.R.bool.config_allowAllRotations) ? 1 : 0;
                }
                if (sensorRotation != Surface.ROTATION_180
                        || mAllowAllRotations == 1
                        || orientation == ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                        || orientation == ActivityInfo.SCREEN_ORIENTATION_FULL_USER) {
                    preferredRotation = sensorRotation;
                } else {
                    preferredRotation = lastRotation;
                }
            } else if (mUserRotationMode == WindowManagerPolicy.USER_ROTATION_LOCKED
                    && orientation != ActivityInfo.SCREEN_ORIENTATION_NOSENSOR) {
                // Apply rotation lock.  Does not apply to NOSENSOR.
                // The idea is that the user rotation expresses a weak preference for the direction
                // of gravity and as NOSENSOR is never affected by gravity, then neither should
                // NOSENSOR be affected by rotation lock (although it will be affected by docks).
                preferredRotation = mUserRotation;
            } else {
                // No overriding preference.
                // We will do exactly what the application asked us to do.
                preferredRotation = -1;
            }

            /// M:[ALPS00117318] @{
            if (DEBUG_ORIENTATION) {
                Slog.v(TAG, "rotationForOrientationLw(appReqQrientation = "
                            + orientation + ", lastOrientation = " + lastRotation
                            + ", sensorRotation = " + sensorRotation
                            + ", UserRotation = " + mUserRotation
                            + ", LidState = " + mLidState
                            + ", DockMode = " + mDockMode
                            + ", DeskDockEnable = " + mDeskDockEnablesAccelerometer
                            + ", CarDockEnable = " + mCarDockEnablesAccelerometer
                            + ", HdmiPlugged = " + mHdmiPlugged
                            + ", Accelerometer = " + mAccelerometerDefault
                            + ", AllowAllRotations = " + mAllowAllRotations
                            + ")");
            }
            /// @}

            switch (orientation) {
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                    // Return portrait unless overridden.
                    if (isAnyPortrait(preferredRotation)) {
                        return preferredRotation;
                    }
                    return mPortraitRotation;

                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                    // Return landscape unless overridden.
                    if (isLandscapeOrSeascape(preferredRotation)) {
                        return preferredRotation;
                    }
                    return mLandscapeRotation;

                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                    // Return reverse portrait unless overridden.
                    if (isAnyPortrait(preferredRotation)) {
                        return preferredRotation;
                    }
                    return mUpsideDownRotation;

                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                    // Return seascape unless overridden.
                    if (isLandscapeOrSeascape(preferredRotation)) {
                        return preferredRotation;
                    }
                    return mSeascapeRotation;

                case ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:
                case ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE:
                    // Return either landscape rotation.
                    if (isLandscapeOrSeascape(preferredRotation)) {
                        return preferredRotation;
                    }
                    if (isLandscapeOrSeascape(lastRotation)) {
                        return lastRotation;
                    }
                    return mLandscapeRotation;

                case ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT:
                case ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT:
                    // Return either portrait rotation.
                    if (isAnyPortrait(preferredRotation)) {
                        return preferredRotation;
                    }
                    if (isAnyPortrait(lastRotation)) {
                        return lastRotation;
                    }
                    return mPortraitRotation;

                default:
                    // For USER, UNSPECIFIED, NOSENSOR, SENSOR and FULL_SENSOR,
                    // just return the preferred orientation we already calculated.
                    /*prize-add-bugid:46187-yangming-2017_12_21-start*/
                    if(mGlobalActions != null && ShutdownDialog.dialogIsShowing()){
                        Log.i("globalactions","rotationForOrientationLw..  defaults return lastRotation;" + ShutdownDialog.dialogIsShowing());
                        return lastRotation;
                    }
                    /*prize-add-bugid:46187-yangming-2017_12_21-end*/
                    if (preferredRotation >= 0) {
                        return preferredRotation;
                    }
                    return Surface.ROTATION_0;
            }
        }
    }

    @Override
    public boolean rotationHasCompatibleMetricsLw(int orientation, int rotation) {
        switch (orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
            case ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT:
                return isAnyPortrait(rotation);

            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
            case ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:
                return isLandscapeOrSeascape(rotation);

            default:
                return true;
        }
    }

    @Override
    public void setRotationLw(int rotation) {
        mOrientationListener.setCurrentRotation(rotation);
    }

    private boolean isLandscapeOrSeascape(int rotation) {
        return rotation == mLandscapeRotation || rotation == mSeascapeRotation;
    }

    private boolean isAnyPortrait(int rotation) {
        return rotation == mPortraitRotation || rotation == mUpsideDownRotation;
    }

    @Override
    public int getUserRotationMode() {
        return Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0, UserHandle.USER_CURRENT) != 0 ?
                        WindowManagerPolicy.USER_ROTATION_FREE :
                                WindowManagerPolicy.USER_ROTATION_LOCKED;
    }

    // User rotation: to be used when all else fails in assigning an orientation to the device
    @Override
    public void setUserRotationMode(int mode, int rot) {
        ContentResolver res = mContext.getContentResolver();

        // mUserRotationMode and mUserRotation will be assigned by the content observer
        if (mode == WindowManagerPolicy.USER_ROTATION_LOCKED) {
            Settings.System.putIntForUser(res,
                    Settings.System.USER_ROTATION,
                    rot,
                    UserHandle.USER_CURRENT);
            Settings.System.putIntForUser(res,
                    Settings.System.ACCELEROMETER_ROTATION,
                    0,
                    UserHandle.USER_CURRENT);
        } else {
            Settings.System.putIntForUser(res,
                    Settings.System.ACCELEROMETER_ROTATION,
                    1,
                    UserHandle.USER_CURRENT);
        }
    }

    @Override
    public void setSafeMode(boolean safeMode) {
        mSafeMode = safeMode;
        performHapticFeedbackLw(null, safeMode
                ? HapticFeedbackConstants.SAFE_MODE_ENABLED
                : HapticFeedbackConstants.SAFE_MODE_DISABLED, true);
    }

    static long[] getLongIntArray(Resources r, int resid) {
        int[] ar = r.getIntArray(resid);
        if (ar == null) {
            return null;
        }
        long[] out = new long[ar.length];
        for (int i=0; i<ar.length; i++) {
            out[i] = ar[i];
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public void systemReady() {
        mKeyguardDelegate = new KeyguardServiceDelegate(mContext,
                this::onKeyguardShowingStateChanged);
        mKeyguardDelegate.onSystemReady();

        readCameraLensCoverState();
        updateUiMode();
        boolean bindKeyguardNow;
        synchronized (mLock) {
            updateOrientationListenerLp();
            mSystemReady = true;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateSettings();
                }
            });

            bindKeyguardNow = mDeferBindKeyguard;
            if (bindKeyguardNow) {
                // systemBooted ran but wasn't able to bind to the Keyguard, we'll do it now.
                mDeferBindKeyguard = false;
            }
        }

        if (bindKeyguardNow) {
            mKeyguardDelegate.bindService(mContext);
            mKeyguardDelegate.onBootCompleted();
        }
        mSystemGestures.systemReady();
        mImmersiveModeConfirmation.systemReady();
    }

    /** {@inheritDoc} */
    @Override
    public void systemBooted() {
        boolean bindKeyguardNow = false;
        synchronized (mLock) {
            // Time to bind Keyguard; take care to only bind it once, either here if ready or
            // in systemReady if not.
            if (mKeyguardDelegate != null) {
                bindKeyguardNow = true;
            } else {
                // Because mKeyguardDelegate is null, we know that the synchronized block in
                // systemReady didn't run yet and setting this will actually have an effect.
                mDeferBindKeyguard = true;
            }
        }
        if (bindKeyguardNow) {
            mKeyguardDelegate.bindService(mContext);
            mKeyguardDelegate.onBootCompleted();
        }
        synchronized (mLock) {
            mSystemBooted = true;
        }
        startedWakingUp();
        screenTurningOn(null);
        screenTurnedOn();
    }

    ProgressDialog mBootMsgDialog = null;

    /** {@inheritDoc} */
    @Override
    public void showBootMessage(final CharSequence msg, final boolean always) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                if (mBootMsgDialog == null) {
                    int theme;
                    if (mContext.getPackageManager().hasSystemFeature(FEATURE_TELEVISION)) {
                        theme = com.android.internal.R.style.Theme_Leanback_Dialog_Alert;
                    } else {
                        theme = 0;
                    }

                    mBootMsgDialog = new ProgressDialog(mContext, theme) {
                        // This dialog will consume all events coming in to
                        // it, to avoid it trying to do things too early in boot.
                        @Override public boolean dispatchKeyEvent(KeyEvent event) {
                            return true;
                        }
                        @Override public boolean dispatchKeyShortcutEvent(KeyEvent event) {
                            return true;
                        }
                        @Override public boolean dispatchTouchEvent(MotionEvent ev) {
                            return true;
                        }
                        @Override public boolean dispatchTrackballEvent(MotionEvent ev) {
                            return true;
                        }
                        @Override public boolean dispatchGenericMotionEvent(MotionEvent ev) {
                            return true;
                        }
                        @Override public boolean dispatchPopulateAccessibilityEvent(
                                AccessibilityEvent event) {
                            return true;
                        }
                    };
                    if (mContext.getPackageManager().isUpgrade()) {
                        mBootMsgDialog.setTitle(R.string.android_upgrading_title);
                    } else {
                        mBootMsgDialog.setTitle(R.string.android_start_title);
                    }
                    mBootMsgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mBootMsgDialog.setIndeterminate(true);
                    mBootMsgDialog.getWindow().setType(
                            WindowManager.LayoutParams.TYPE_BOOT_PROGRESS);
                    mBootMsgDialog.getWindow().addFlags(
                            WindowManager.LayoutParams.FLAG_DIM_BEHIND
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
                    mBootMsgDialog.getWindow().setDimAmount(1);
                    WindowManager.LayoutParams lp = mBootMsgDialog.getWindow().getAttributes();
                    lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
                    mBootMsgDialog.getWindow().setAttributes(lp);
                    mBootMsgDialog.setCancelable(false);
                    mBootMsgDialog.show();
                }
                mBootMsgDialog.setMessage(msg);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void hideBootMessages() {
        mHandler.sendEmptyMessage(MSG_HIDE_BOOT_MESSAGE);
    }

    /** {@inheritDoc} */
    @Override
    public void userActivity() {
        // ***************************************
        // NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE
        // ***************************************
        // THIS IS CALLED FROM DEEP IN THE POWER MANAGER
        // WITH ITS LOCKS HELD.
        //
        // This code must be VERY careful about the locks
        // it acquires.
        // In fact, the current code acquires way too many,
        // and probably has lurking deadlocks.

        /// M:[ALPS00062902] When the user activiy flag is enabled,
        /// it notifies the intent "STK_USERACTIVITY" @{
        synchronized (mStkLock) {
            if (mIsStkUserActivityEnabled) {
                /// M:[ALPS00389865]
                mHandler.post(mNotifyStk);
            }
        }
        /// @}

        synchronized (mScreenLockTimeout) {
            if (mLockScreenTimerActive) {
                // reset the timer
                mHandler.removeCallbacks(mScreenLockTimeout);
                mHandler.postDelayed(mScreenLockTimeout, mLockScreenTimeout);
            }
        }
    }

    class ScreenLockTimeout implements Runnable {
        Bundle options;

        @Override
        public void run() {
            synchronized (this) {
                if (localLOGV) Log.v(TAG, "mScreenLockTimeout activating keyguard");
                if (mKeyguardDelegate != null) {
                    mKeyguardDelegate.doKeyguardTimeout(options);
                }
                mLockScreenTimerActive = false;
                options = null;
            }
        }

        public void setLockOptions(Bundle options) {
            this.options = options;
        }
    }

    ScreenLockTimeout mScreenLockTimeout = new ScreenLockTimeout();

    @Override
    public void lockNow(Bundle options) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.DEVICE_POWER, null);
        mHandler.removeCallbacks(mScreenLockTimeout);
        if (options != null) {
            // In case multiple calls are made to lockNow, we don't wipe out the options
            // until the runnable actually executes.
            mScreenLockTimeout.setLockOptions(options);
        }
        mHandler.post(mScreenLockTimeout);
    }

    private void updateLockScreenTimeout() {
        synchronized (mScreenLockTimeout) {
            boolean enable = (mAllowLockscreenWhenOn && mAwake &&
                    mKeyguardDelegate != null && mKeyguardDelegate.isSecure(mCurrentUserId));
            if (mLockScreenTimerActive != enable) {
                if (enable) {
                    if (localLOGV) Log.v(TAG, "setting lockscreen timer");
                    mHandler.removeCallbacks(mScreenLockTimeout); // remove any pending requests
                    mHandler.postDelayed(mScreenLockTimeout, mLockScreenTimeout);
                } else {
                    if (localLOGV) Log.v(TAG, "clearing lockscreen timer");
                    mHandler.removeCallbacks(mScreenLockTimeout);
                }
                mLockScreenTimerActive = enable;
            }
        }
    }

    private void updateDreamingSleepToken(boolean acquire) {
        if (acquire) {
            if (mDreamingSleepToken == null) {
                mDreamingSleepToken = mActivityManagerInternal.acquireSleepToken("Dream");
            }
        } else {
            if (mDreamingSleepToken != null) {
                mDreamingSleepToken.release();
                mDreamingSleepToken = null;
            }
        }
    }

    private void updateScreenOffSleepToken(boolean acquire) {
        if (acquire) {
            if (mScreenOffSleepToken == null) {
                mScreenOffSleepToken = mActivityManagerInternal.acquireSleepToken("ScreenOff");
            }
        } else {
            if (mScreenOffSleepToken != null) {
                mScreenOffSleepToken.release();
                mScreenOffSleepToken = null;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void enableScreenAfterBoot() {
        readLidState();
        applyLidSwitchState();
        updateRotation(true);
    }

    private void applyLidSwitchState() {
        if (mLidState == LID_CLOSED && mLidControlsSleep) {
            mPowerManager.goToSleep(SystemClock.uptimeMillis(),
                    PowerManager.GO_TO_SLEEP_REASON_LID_SWITCH,
                    PowerManager.GO_TO_SLEEP_FLAG_NO_DOZE);
        } else if (mLidState == LID_CLOSED && mLidControlsScreenLock) {
            mWindowManagerFuncs.lockDeviceNow();
        }

        synchronized (mLock) {
            updateWakeGestureListenerLp();
        }
    }

    void updateUiMode() {
        if (mUiModeManager == null) {
            mUiModeManager = IUiModeManager.Stub.asInterface(
                    ServiceManager.getService(Context.UI_MODE_SERVICE));
        }
        try {
            mUiMode = mUiModeManager.getCurrentModeType();
        } catch (RemoteException e) {
        }
    }

    void updateRotation(boolean alwaysSendConfiguration) {
        try {
            //set orientation on WindowManager
            mWindowManager.updateRotation(alwaysSendConfiguration, false);
        } catch (RemoteException e) {
            // Ignore
        }
    }

    void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) {
        try {
            //set orientation on WindowManager
            mWindowManager.updateRotation(alwaysSendConfiguration, forceRelayout);
        } catch (RemoteException e) {
            // Ignore
        }
    }

    /**
     * Return an Intent to launch the currently active dock app as home.  Returns
     * null if the standard home should be launched, which is the case if any of the following is
     * true:
     * <ul>
     *  <li>The device is not in either car mode or desk mode
     *  <li>The device is in car mode but mEnableCarDockHomeCapture is false
     *  <li>The device is in desk mode but ENABLE_DESK_DOCK_HOME_CAPTURE is false
     *  <li>The device is in car mode but there's no CAR_DOCK app with METADATA_DOCK_HOME
     *  <li>The device is in desk mode but there's no DESK_DOCK app with METADATA_DOCK_HOME
     * </ul>
     * @return A dock intent.
     */
    Intent createHomeDockIntent() {
        Intent intent = null;

        // What home does is based on the mode, not the dock state.  That
        // is, when in car mode you should be taken to car home regardless
        // of whether we are actually in a car dock.
        if (mUiMode == Configuration.UI_MODE_TYPE_CAR) {
            if (mEnableCarDockHomeCapture) {
                intent = mCarDockIntent;
            }
        } else if (mUiMode == Configuration.UI_MODE_TYPE_DESK) {
            if (ENABLE_DESK_DOCK_HOME_CAPTURE) {
                intent = mDeskDockIntent;
            }
        } else if (mUiMode == Configuration.UI_MODE_TYPE_WATCH
                && (mDockMode == Intent.EXTRA_DOCK_STATE_DESK
                        || mDockMode == Intent.EXTRA_DOCK_STATE_HE_DESK
                        || mDockMode == Intent.EXTRA_DOCK_STATE_LE_DESK)) {
            // Always launch dock home from home when watch is docked, if it exists.
            intent = mDeskDockIntent;
        }

        if (intent == null) {
            return null;
        }

        ActivityInfo ai = null;
        ResolveInfo info = mContext.getPackageManager().resolveActivityAsUser(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_META_DATA,
                mCurrentUserId);
        if (info != null) {
            ai = info.activityInfo;
        }
        if (ai != null
                && ai.metaData != null
                && ai.metaData.getBoolean(Intent.METADATA_DOCK_HOME)) {
            intent = new Intent(intent);
            intent.setClassName(ai.packageName, ai.name);
            return intent;
        }

        return null;
    }

    void startDockOrHome(boolean fromHomeKey, boolean awakenFromDreams) {
        if (awakenFromDreams) {
            awakenDreams();
        }

        Intent dock = createHomeDockIntent();
        if (dock != null) {
            try {
                if (fromHomeKey) {
                    dock.putExtra(WindowManagerPolicy.EXTRA_FROM_HOME_KEY, fromHomeKey);
                }
                startActivityAsUser(dock, UserHandle.CURRENT);
                return;
            } catch (ActivityNotFoundException e) {
            }
        }

        Intent intent;

        if (fromHomeKey) {
            intent = new Intent(mHomeIntent);
            intent.putExtra(WindowManagerPolicy.EXTRA_FROM_HOME_KEY, fromHomeKey);
        } else {
            intent = mHomeIntent;
        }

        /*prize add for feature: frozen app 2018-05-02 begin */
        com.android.server.am.ActivityManagerService ams;
        if(fromHomeKey){
            ams = (com.android.server.am.ActivityManagerService) ServiceManager.getService("activity");
            ams.startHomeActivityResponse();
        }
        /*prize add for feature: frozen app 2018-05-02 end */

        Slog.i(TAG, "startDockOrHome intent:"+intent.toString());//prize-add by lihuangyuan,for bug51875
        startActivityAsUser(intent, UserHandle.CURRENT);
    }

    /**
     * goes to the home screen
     * @return whether it did anything
     */
    boolean goHome() {
        if (!isUserSetupComplete()) {
            Slog.i(TAG, "Not going home because user setup is in progress.");
            return false;
        }
        if (false) {
            // This code always brings home to the front.
            try {
                ActivityManagerNative.getDefault().stopAppSwitches();
            } catch (RemoteException e) {
            }
            sendCloseSystemWindows();
            startDockOrHome(false /*fromHomeKey*/, true /* awakenFromDreams */);
        } else {
            // This code brings home to the front or, if it is already
            // at the front, puts the device to sleep.
            try {
                if (SystemProperties.getInt("persist.sys.uts-test-mode", 0) == 1) {
                    /// Roll back EndcallBehavior as the cupcake design to pass P1 lab entry.
                    Log.d(TAG, "UTS-TEST-MODE");
                } else {
                    ActivityManagerNative.getDefault().stopAppSwitches();
                    sendCloseSystemWindows();
                    Intent dock = createHomeDockIntent();
                    if (dock != null) {
                        int result = ActivityManagerNative.getDefault()
                                .startActivityAsUser(null, null, dock,
                                        dock.resolveTypeIfNeeded(mContext.getContentResolver()),
                                        null, null, 0,
                                        ActivityManager.START_FLAG_ONLY_IF_NEEDED,
                                        null, null, UserHandle.USER_CURRENT);
                        if (result == ActivityManager.START_RETURN_INTENT_TO_CALLER) {
                            return false;
                        }
                    }
                }
                int result = ActivityManagerNative.getDefault()
                        .startActivityAsUser(null, null, mHomeIntent,
                                mHomeIntent.resolveTypeIfNeeded(mContext.getContentResolver()),
                                null, null, 0,
                                ActivityManager.START_FLAG_ONLY_IF_NEEDED,
                                null, null, UserHandle.USER_CURRENT);
                if (result == ActivityManager.START_RETURN_INTENT_TO_CALLER) {
                    return false;
                }
            } catch (RemoteException ex) {
                // bummer, the activity manager, which is in this process, is dead
            }
        }
        return true;
    }

    @Override
    public void setCurrentOrientationLw(int newOrientation) {
        synchronized (mLock) {
            if (newOrientation != mCurrentAppOrientation) {
                mCurrentAppOrientation = newOrientation;
                updateOrientationListenerLp();
            }
        }
    }

    private void performAuditoryFeedbackForAccessibilityIfNeed() {
        if (!isGlobalAccessibilityGestureEnabled()) {
            return;
        }
        AudioManager audioManager = (AudioManager) mContext.getSystemService(
                Context.AUDIO_SERVICE);
        if (audioManager.isSilentMode()) {
            return;
        }
        Ringtone ringTone = RingtoneManager.getRingtone(mContext,
                Settings.System.DEFAULT_NOTIFICATION_URI);
        ringTone.setStreamType(AudioManager.STREAM_MUSIC);
        ringTone.play();
    }

    private boolean isTheaterModeEnabled() {
        return Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.THEATER_MODE_ON, 0) == 1;
    }

    private boolean isGlobalAccessibilityGestureEnabled() {
        return Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED, 0) == 1;
    }

    private boolean areSystemNavigationKeysEnabled() {
        return Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.SYSTEM_NAVIGATION_KEYS_ENABLED, 0, UserHandle.USER_CURRENT) == 1;
    }

    @Override
    public boolean performHapticFeedbackLw(WindowState win, int effectId, boolean always) {
        if (!mVibrator.hasVibrator()) {
            return false;
        }
        final boolean hapticsDisabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 0, UserHandle.USER_CURRENT) == 0;
        if (hapticsDisabled && !always) {
            return false;
        }
        long[] pattern = null;
        switch (effectId) {
            case HapticFeedbackConstants.LONG_PRESS:
                pattern = mLongPressVibePattern;
                break;
            case HapticFeedbackConstants.VIRTUAL_KEY:
                pattern = mVirtualKeyVibePattern;
                break;
            case HapticFeedbackConstants.KEYBOARD_TAP:
                pattern = mKeyboardTapVibePattern;
                break;
            case HapticFeedbackConstants.CLOCK_TICK:
                pattern = mClockTickVibePattern;
                break;
            case HapticFeedbackConstants.CALENDAR_DATE:
                pattern = mCalendarDateVibePattern;
                break;
            case HapticFeedbackConstants.SAFE_MODE_DISABLED:
                pattern = mSafeModeDisabledVibePattern;
                break;
            case HapticFeedbackConstants.SAFE_MODE_ENABLED:
                pattern = mSafeModeEnabledVibePattern;
                break;
            case HapticFeedbackConstants.CONTEXT_CLICK:
                pattern = mContextClickVibePattern;
                break;
            default:
                return false;
        }
        int owningUid;
        String owningPackage;
        if (win != null) {
            owningUid = win.getOwningUid();
            owningPackage = win.getOwningPackage();
        } else {
            owningUid = android.os.Process.myUid();
            owningPackage = mContext.getOpPackageName();
        }
        if (pattern.length == 1) {
            // One-shot vibration
            mVibrator.vibrate(owningUid, owningPackage, pattern[0], VIBRATION_ATTRIBUTES);
        } else {
            // Pattern vibration
            mVibrator.vibrate(owningUid, owningPackage, pattern, -1, VIBRATION_ATTRIBUTES);
        }
        return true;
    }

    @Override
    public void keepScreenOnStartedLw() {
    }

    @Override
    public void keepScreenOnStoppedLw() {
        if (isKeyguardShowingAndNotOccluded()) {
            mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }
    }

    //add for status bar inverse style. prize-linkh-20150906
    private boolean shouldIgnoreStatusBarInversedChange(WindowState win) {
        if(!mSystemBooted) {
            return true;
        }
        if(win == null) {
            return true;
        }
        
        if(win == mStatusBar || win == mNavigationBar) {
            return true;
        }
        int type = win.getAttrs().type;
        if(type >= WindowManager.LayoutParams.FIRST_SUB_WINDOW && type <= WindowManager.LayoutParams.LAST_SUB_WINDOW) {
            return true;
        }
        
        return false;
    }
    
    private void updateStatusBarInverseStyle(WindowState win) {
        if(win == null) {
            return;
        }
        
        final int sbInverse;
        /* prize-add-split screen for landscape statubar Text Color-liyongli-20171027-start */
        if( PrizeOption.PRIZE_SPLIT_SCREEN_LANDSCAPE_ONE_STATUBAR 
             && mWindowManagerInternal.isStackVisible(DOCKED_STACK_ID)   
             && (mDisplayRotation==Surface.ROTATION_90||mDisplayRotation==Surface.ROTATION_270)
        ){
            sbInverse = StatusBarManager.STATUS_BAR_INVERSE_DEFALUT; //landscape split screen, not use Inverse color
        }else  /* prize-add-split screen-liyongli-20171027-end */
        if(win.getAttrs().statusBarInverse < 0) {
            //If the app don't specify which inverse style to use, we use the default.
            sbInverse = StatusBarManager.STATUS_BAR_INVERSE_DEFALUT;
        } else {
            sbInverse = win.getAttrs().statusBarInverse;
        }
        int type = win.getAttrs().type;
        //Log.d(TAG, "updateSystemUiVisibilityLw().win=" + win);
        //Log.d(TAG, "sbInverse=" + sbInverse + ", mLastStatusBarInverse=" + mLastStatusBarInverse + ", type=" + type); 
        if(!shouldIgnoreStatusBarInversedChange(win) && mLastStatusBarInverse != sbInverse) {
            mLastStatusBarInverse = sbInverse;
            //Log.d(TAG, "updateSystemUiVisibilityLw()   "+ sbInverse+ " win=" + win);
            mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IStatusBarService statusbar = getStatusBarService();
                            if (statusbar != null) {
                                Slog.d(TAG, "onStatusBarInverseChanged(): sbInverse=" + sbInverse);
                                statusbar.onStatusBarInverseChanged(sbInverse);
                            }
                        } catch (RemoteException e) {
                            // re-acquire status bar service next time it is needed.
                            mStatusBarService = null;
                        }                        
                    }
            });
        }            
      
    }
    //end....

    // Nav bar color customized feature. prize-linkh-2017.07.12
    private static final boolean DBG_NAV_BAR_COLOR_CUST = "eng".equals(android.os.Build.TYPE);
    private boolean mIsNavBarGone = false;
    private boolean mInBeginLayout = false;
    private boolean mInFocusChanged = false;
    private boolean mLastDispatchedEnableSysUINavBarColorBg;
    private boolean mLastHasWinSetNavBarColor;
    private int mLastDiaptchedNavBarColor = -1;
    private boolean mTempEnableSysUINavBarColorBg;
    private boolean mTempHasWinSetNavBarColor;
    private int mTempNavBarColor = -1;
    private boolean mEnforceSystemUiTransparent;
    private WindowState mInputMethodWindow = null;
    private WindowState mInputMethodChildWinBehindNavBar = null;
    private NavBarColorRunnable mNavBarColorRunnable;
    
    // Keep sync with BarTransitions class
    private static final int MODE_OPAQUE = 0;
    private static final int MODE_SEMI_TRANSPARENT = 1;
    private static final int MODE_TRANSLUCENT = 2;
    private static final int MODE_LIGHTS_OUT = 3;
    private static final int MODE_TRANSPARENT = 4;
    private static final int MODE_WARNING = 5;
    private static final int MODE_LIGHTS_OUT_TRANSPARENT = 6;    

    private boolean maybeNavBarInOpaqueMode(int vis) {
        final int mode = getNavigationBarMode(vis);
        if (DBG_NAV_BAR_COLOR_CUST) {
            Slog.d(TAG, "maybeNavBarInOpaqueMode() isOpaqueMode=" + (mode == MODE_OPAQUE));
        }
        return mode == MODE_OPAQUE;
    }

    // This rule is from PhoneStatusBar.barMode()
    private int getNavigationBarMode(int vis) {
        final int lightsOutTransparent = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_TRANSPARENT;
        final int mode = (vis & View.NAVIGATION_BAR_TRANSIENT) != 0 ? MODE_SEMI_TRANSPARENT
                : (vis & View.NAVIGATION_BAR_TRANSLUCENT) != 0 ? MODE_TRANSLUCENT
                : (vis & lightsOutTransparent) == lightsOutTransparent ? MODE_LIGHTS_OUT_TRANSPARENT
                : (vis & View.NAVIGATION_BAR_TRANSPARENT) != 0 ? MODE_TRANSPARENT
                : (vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0 ? MODE_LIGHTS_OUT
                : MODE_OPAQUE;
        if (DBG_NAV_BAR_COLOR_CUST) {
            Slog.d(TAG, "getNavigationBarMode() vis=0x" + Integer.toHexString(vis)
                    + ", mode=" + mode);
        }
        return mode;
    }

    private int getSystemCustNavBarColor() {
        return Settings.System.getInt(
                mContext.getContentResolver(),Settings.System.PRIZE_NAV_BAR_BG_COLOR, 
                StatusBarManager.DEFAULT_NAV_BAR_COLOR);
    }
    
    // Note: If win hasn't been computed frame, this method still returns true.
    private boolean maybeShowNavBarColorViewLw(int rotation, WindowState win) {
        if (win == null) {
            return false;
        }

        final Rect contentInsets = win.getContentInsets();
        final boolean hasframe = win.hasFrame();

        int insets = 0;
        if (hasframe) {
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
                // portrait 
                insets = contentInsets.bottom;
            } else {
                // landscape
                if (mNavigationBarPosition == NAV_BAR_LEFT) {
                    insets = contentInsets.left;
                } else {
                    insets = contentInsets.right;
                }
            }
        }

        if (DBG_NAV_BAR_COLOR_CUST) {
            Slog.d(TAG, "maybeShowNavBarColorView() rotation=" + rotation + ", navPos=" + mNavigationBarPosition
                    + ", contentInsets=" + contentInsets
                    + ", hasframe=" + hasframe
                    + ", show=" + (insets > 0) + ", win=" + win);
        }
        // If this window hasn't been computed frame, then we still assume
        // it has shown nav bar color view. See updateNavbarColorLw for details.
        return insets > 0 || !hasframe;
    }

    private boolean isStatusBarExpandedNotKeyguardLw() {
        // See beginLayoutLw() for detatils.
        boolean statusBarExpandedNotKeyguard = false;
        if (mStatusBar != null) {
            boolean isKeyguardShowing = isStatusBarKeyguard() && !mHideLockScreen;    
            statusBarExpandedNotKeyguard = !isKeyguardShowing
                    && mStatusBar.getAttrs().height == MATCH_PARENT
                    && mStatusBar.getAttrs().width == MATCH_PARENT;
        }

        if (DBG_NAV_BAR_COLOR_CUST) {
            Slog.d(TAG, "isStatusBarExpandedNotKeyguardLw() statusBarExpandedNotKeyguard=" + statusBarExpandedNotKeyguard);
        }

        return statusBarExpandedNotKeyguard;
    }

    final class NavBarColorRunnable implements Runnable {
        final int navBarColor;
        final boolean hasWinSetNavBarColor;
        final boolean enableSysUiNavBarColorBg;

        public NavBarColorRunnable(int navBarColor, boolean hasWinSetNavBarColor, boolean enableSysUiNavBarColorBg) {
            this.navBarColor = navBarColor;
            this.hasWinSetNavBarColor = hasWinSetNavBarColor;
            this.enableSysUiNavBarColorBg = enableSysUiNavBarColorBg;
        }

        @Override
        public void run() {
            try {
                IStatusBarService statusbar = getStatusBarService();
                if (statusbar != null) {
                    if (true) { //(DBG_NAV_BAR_COLOR_CUST) {
                        Slog.d(TAG, "updateNavbarColorLw() color=0x" + Integer.toHexString(navBarColor)
                                + ", hasWinSetNavBarColor=" + hasWinSetNavBarColor
                                + ", enableSysUINavBarColorBg=" + enableSysUiNavBarColorBg
                                + " : Run Runnable--" + this);
                    }
                    statusbar.updateNavBarColor(navBarColor, hasWinSetNavBarColor, enableSysUiNavBarColorBg);
                }
            } catch (RemoteException e) {
                // re-acquire status bar service next time it is needed.
                mStatusBarService = null;
            }
        }        
    }
    
    private void postNavBarColorRunnableLw(WindowState win, final int navBarColor, 
            final boolean hasWinSetNavBarColor, final boolean enableSysUINavBarColorBg, long delay) {        
        if (mNavBarColorRunnable != null) {
            mHandler.removeCallbacks(mNavBarColorRunnable);
        }

        mNavBarColorRunnable = new NavBarColorRunnable(navBarColor, hasWinSetNavBarColor, enableSysUINavBarColorBg);

        if (true) { //(DBG_NAV_BAR_COLOR_CUST) {
            Slog.d(TAG, "updateNavbarColorLw() color=0x" + Integer.toHexString(navBarColor)
                   + ", hasWinSetNavBarColor=" + hasWinSetNavBarColor
                   + ", enableSysUINavBarColorBg=" + enableSysUINavBarColorBg
                   + ", delay=" + delay
                   + "ms, win=" + win
                   + " : Post Runnable--" + mNavBarColorRunnable);
        }
        mHandler.postDelayed(mNavBarColorRunnable, delay);
    }

    private boolean updateNavbarColorLw(WindowState focusWin, int sysUiVis, boolean isChecking) {
        if (DBG_NAV_BAR_COLOR_CUST) {
            Slog.d(TAG, "updateNavbarColorLw() focusWin=" + focusWin + ", lp=" + focusWin.getAttrs());
            Slog.d(TAG, "  isDrawnLw=" + focusWin.isDrawnLw() + ", hasDrawnLw=" + focusWin.hasDrawnLw()
                        + ", isDisplayedLw=" + focusWin.isDisplayedLw());
            Slog.d(TAG, "updateNavbarColorLw() mTopFullscreenOpaqueWindowState=" + mTopFullscreenOpaqueWindowState);
            if (mTopFullscreenOpaqueWindowState != null && mTopFullscreenOpaqueWindowState != focusWin) {
                Slog.d(TAG, "  isDrawnLw=" + mTopFullscreenOpaqueWindowState.isDrawnLw() + ", hasDrawnLw=" + mTopFullscreenOpaqueWindowState.hasDrawnLw()
                            + ", isDisplayedLw=" + mTopFullscreenOpaqueWindowState.isDisplayedLw());
            }         
        }

        final WindowManager.LayoutParams lp = focusWin.getAttrs();

        if (!focusWin.hasDrawnLw() || mInBeginLayout || mInFocusChanged || mIsNavBarGone) {
            if (DBG_NAV_BAR_COLOR_CUST) {
                StringBuilder sb = new StringBuilder();
                sb.append("Reason(");
                if (!focusWin.hasDrawnLw()) {
                    sb.append("NotHasDrawn");
                }
                if (mInBeginLayout) {
                    sb.append(",InBeginLayout");
                }
                if (mInFocusChanged) {
                    sb.append(",InFocusChanged");
                }
                if (mIsNavBarGone) {
                    sb.append(",NavBarIsGone");
                }                
                sb.append(")");

                Slog.d(TAG, "updateNavbarColorLw() Cancel! " + sb.toString());
            }

            return false;
        }else if (mForceShowSystemBars && maybeNavBarInOpaqueMode(sysUiVis)
                && "com.android.systemui".equals(lp.packageName) && lp.getTitle() != null && 
                lp.getTitle().toString().contains("RecentsActivity")) {
            if (DBG_NAV_BAR_COLOR_CUST) {
                Slog.d(TAG, "updateNavbarColorLw() Focus win is RecentsActivity and phone is in Docked Mode. "
                    + "So jump to change nav bar color!");
            }
            return false;
        }
    
        //final IApplicationToken token = focusWin.getAppToken();
        //final boolean appFullScreen = focusWin.isAppFullScreen();
        final Rect contentInsets = focusWin.getContentInsets();
        final boolean isApp = lp.type >= WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW
                && lp.type <= WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
        final boolean isSubWin = lp.type >= WindowManager.LayoutParams.FIRST_SUB_WINDOW
                && lp.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW;
        final WindowState attachedWin = focusWin.getAttachedWindow();
        final boolean maybeShowNavBarColorView = maybeShowNavBarColorViewLw(mDisplayRotation, focusWin);
        final boolean isInLandScapeMode = (mDisplayRotation == Surface.ROTATION_90) || (mDisplayRotation == Surface.ROTATION_270);
        // 0.0f --> transparent. 1.0f --> full opaque
        final boolean transparentOrFullOpaqueDimAmount = lp.dimAmount == 1.0f || lp.dimAmount == 0.0f;
        final boolean isStartingWin = lp.type == WindowManager.LayoutParams.TYPE_APPLICATION_STARTING;
        int tmpNavBarColor = 0;
        boolean tmpHasWinSetNavBarColor = false;
        boolean forcedSystemUINavBarColorBackground = false;
        boolean useNavBarColorBecauseOfIME = false;;

        if (mInputMethodWindow != null && mInputMethodWindow.isVisibleLw()) {
            if (mInputMethodChildWinBehindNavBar != null) {
                // An input method child window is showing behind nav bar.
                // So we enable system ui nav bar color bk to avoid ugly look.
                useNavBarColorBecauseOfIME = true;
                forcedSystemUINavBarColorBackground = true;
            } else if (mWindowManagerFuncs.getInputMethodTarget() == focusWin) {
                // Input method is serving focus window.
                useNavBarColorBecauseOfIME = true;
            } else if (focusWin.isImWindow()) {
                // 1. input method window is showing a child window
                //       or it has request focus itself. 
                // 2. The system select input window has dim amount. So don't consider it.
                // 3. Almost child window is popup window type. This popup window maybe has
                //    a dim background(eg, sogou inputmethod popup). So we enable system ui
                //    nav bar color bk to avoid ugly look.
                if (!lp.packageName.equals("android")) {
                    useNavBarColorBecauseOfIME = true;
                    forcedSystemUINavBarColorBackground = true;
                }
            }
        }

        if (useNavBarColorBecauseOfIME) {
            tmpNavBarColor = mInputMethodWindow.getAttrs().navBarColor;
            // Assume this win has set nav bar color.
            tmpHasWinSetNavBarColor = true;
        } else if (lp.alwaysDisableNavbarColorCust) {
            if (false) { //(isStartingWin && maybeNavBarInOpaqueMode(sysUiVis)) {
                // only consider starting win.
                tmpNavBarColor = !lp.hasSetNavBarColor ? getSystemCustNavBarColor() : lp.navBarColor;
                tmpHasWinSetNavBarColor = true;
                forcedSystemUINavBarColorBackground = true;
                if (DBG_NAV_BAR_COLOR_CUST) {
                    Slog.d(TAG, "updateNavbarColorLw() Request disable nav bar color! "
                            + "But it makes sys ui opaque mode. Enable system ui nav bar color background!");
                }
            } else {
                // respect it
                tmpHasWinSetNavBarColor = false;
                if (DBG_NAV_BAR_COLOR_CUST) {
                    Slog.d(TAG, "updateNavbarColorLw() Request disable nav bar color!");
                }
            }
        } else if (lp.hasSetNavBarColor && !lp.enableSystemUINavBarColorBackground && 
            (lp.flags & FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) == 0) {
            // this window doesn't set this flag. it represents its nav bar color can't take effect.
            // If it wants to enable system ui nav bar color background, we must
            // respect it and doesn't changed it.
            tmpHasWinSetNavBarColor = false;
            if (DBG_NAV_BAR_COLOR_CUST) {
                Slog.d(TAG, "updateNavbarColorLw() this win hasn't set FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag! Disable nav bar color!");
            }
        } else if (lp.hasSetNavBarColor && isApp) {
                tmpNavBarColor = lp.navBarColor;
                tmpHasWinSetNavBarColor = true;
                if (!maybeShowNavBarColorView) {
                    if (DBG_NAV_BAR_COLOR_CUST) {
                        // eg. cmcc app.
                        Slog.d(TAG, "updateNavbarColorLw() this win maybe don't show nav bar color view. " 
                            + "Enable system ui nav bar color background!");
                    }
                    forcedSystemUINavBarColorBackground = true;
                }
        } else if (mLastDispatchedEnableSysUINavBarColorBg && !lp.enableSystemUINavBarColorBackground && !lp.alwaysDisableNavbarColorCust
            && !lp.hasSetNavBarColor && (focusWin != mStatusBar || !isStatusBarKeyguard())) {
            // If SystemUi draw nav color itself for last focus win, then 
            // SystemUi has gained focus, we keep it draw last nav bar color itself 
            // for avoid screen flick on navigation bar region. Except keyguard
            // state.
            if (DBG_NAV_BAR_COLOR_CUST) {
                Slog.d(TAG, "updateNavbarColorLw() Keep prev nav bar color because SystemUI has drawn its background.");
            }
            forcedSystemUINavBarColorBackground = true;
            tmpNavBarColor = mLastDiaptchedNavBarColor;
            tmpHasWinSetNavBarColor = true;
        } else if (isSubWin && !lp.hasSetNavBarColor && transparentOrFullOpaqueDimAmount && attachedWin != null) {
            // case: Sub window that doesn't use any nav bar color and without setting dim amount.
            // If attached window has shown nav bar color view, then 
            // we make this sub win to use same nav bar color. Eg, menu popup.
            final boolean maybeShowNavBarColorViewInAttachedWin = 
                maybeShowNavBarColorViewLw(mDisplayRotation, attachedWin);
            final WindowManager.LayoutParams lpInAttachedWin = attachedWin.getAttrs();
            if (lpInAttachedWin.hasSetNavBarColor) {
                tmpNavBarColor = lpInAttachedWin.navBarColor;
                forcedSystemUINavBarColorBackground = !maybeShowNavBarColorViewInAttachedWin;
            } else {
                tmpNavBarColor = lp.navBarColor;
            }
            tmpHasWinSetNavBarColor = true;
        } else if (!transparentOrFullOpaqueDimAmount && lp.type == WindowManager.LayoutParams.TYPE_APPLICATION
                    && maybeNavBarInOpaqueMode(sysUiVis)) {
            // It's almost a dialog. It makes systemui use opaque mode. 
            // Oh, we don't want to see black background.
            // If this win has non-transparent/full-opaque dim, then
            // PhoneWindow can calculate it.

            // Note: Because we can't read real nav bar color from this win.
            //       So we instead read from Settings and we know this isn't 
            //       A GOOD WAY!
            tmpNavBarColor = getSystemCustNavBarColor();
            forcedSystemUINavBarColorBackground = true;
            tmpHasWinSetNavBarColor = true;
            if (DBG_NAV_BAR_COLOR_CUST) {
                Slog.d(TAG, "updateNavbarColorLw() A TYPE_APPLICATION type of dim win makes sysui opaque mode!"
                        + " Enable system ui nav bar color background!");
            }
        } else {
            tmpNavBarColor = lp.navBarColor;
            tmpHasWinSetNavBarColor = lp.hasSetNavBarColor;
        }

        boolean tmpEnableSysUINavBarColorBg = forcedSystemUINavBarColorBackground ? 
                                                true : lp.enableSystemUINavBarColorBackground;
        // Special case
        if ((sysUiVis & (View.NAVIGATION_BAR_TRANSLUCENT | View.NAVIGATION_BAR_TRANSIENT)) != 0) {
            // Request translucent/transient nav bar.
            tmpNavBarColor = 0;
            tmpHasWinSetNavBarColor = false;
            tmpEnableSysUINavBarColorBg = false;
            if (DBG_NAV_BAR_COLOR_CUST) {
                Slog.d(TAG, "updateNavbarColorLw() Request translucent/transient nav bar. So disable nav bar color.");
            }
        } else if (focusWin == mStatusBar && !tmpEnableSysUINavBarColorBg 
            && mTopFullscreenOpaqueOrDimmingWindowState != null) {
            WindowManager.LayoutParams lpOfTopFullscreenOpaqueOrDimmingWin = mTopFullscreenOpaqueOrDimmingWindowState.getAttrs();
            if (isInLandScapeMode) {
                if (DBG_NAV_BAR_COLOR_CUST) {
                    Slog.d(TAG, "updateNavbarColorLw() StatusBar has gained focus in landscape. So use nav bar color of "
                        + "mTopFullscreenOpaqueOrDimmingWindowState--" + mTopFullscreenOpaqueOrDimmingWindowState);
                }
                tmpNavBarColor = lpOfTopFullscreenOpaqueOrDimmingWin.navBarColor;
                tmpHasWinSetNavBarColor = lpOfTopFullscreenOpaqueOrDimmingWin.hasSetNavBarColor;
            } else {
                // Portrait mode
                // It's not full exact!
                if (lpOfTopFullscreenOpaqueOrDimmingWin.enableSystemUINavBarColorBackground 
                    && lpOfTopFullscreenOpaqueOrDimmingWin.navBarColor != 0 && !isStatusBarKeyguard()) {
                    if (DBG_NAV_BAR_COLOR_CUST) {
                        Slog.d(TAG, "updateNavbarColorLw() StatusBar has gained focus in portrait mode. But use nav bar color of "
                            + "mTopFullscreenOpaqueOrDimmingWindowState--" + mTopFullscreenOpaqueOrDimmingWindowState);
                    }
                    tmpNavBarColor = lpOfTopFullscreenOpaqueOrDimmingWin.navBarColor;
                    tmpHasWinSetNavBarColor = true;
                    tmpEnableSysUINavBarColorBg = true;
                }
            }
        }

        if (tmpHasWinSetNavBarColor && !tmpEnableSysUINavBarColorBg) {
            final int mode = getNavigationBarMode(sysUiVis);
            if (mode == MODE_OPAQUE) {
                // If the win requests nav bar color in its ui and the current sys ui mode is opaque, 
                // oh, ennn... we must enable nav bar color in sys ui. See bug-39757                
                if (DBG_NAV_BAR_COLOR_CUST) {
                    Slog.d(TAG, "updateNavbarColorLw() This win requests nav bar color in its ui. "
                            + ", But the sys ui mode is opaque!! So enable system ui nav bar color background!");
                }

                tmpEnableSysUINavBarColorBg = true;
            } else if (mode == MODE_TRANSPARENT && isStatusBarExpandedNotKeyguardLw() && focusWin != mStatusBar
                && (sysUiVis & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0) {
                // Solve bug-44882: If nav bar is hidden and a notification floating view is showing
                // , then the bg of nav bar is transparent. This is android design.
                // If the icon style of nav bar is gray, ohhh....It's like a bug. Ok
                // Let us force to show the nav bar color in sys ui.
                // See beginLayoutLw() for this condition.
                if (DBG_NAV_BAR_COLOR_CUST) {
                    Slog.d(TAG, "updateNavbarColorLw() This win requests nav bar color in its ui, "
                            + " sys ui mode is transparent and Status Bar maybe is showing a notification floating view!" 
                            + " So enable system ui nav bar color background!");
                }

                tmpEnableSysUINavBarColorBg = true;
            }
        }

        final int navBarColor = tmpNavBarColor;
        final boolean enableSystemUINavBarColorBackground = tmpEnableSysUINavBarColorBg;
        final boolean hasWinSetNavBarColor = tmpHasWinSetNavBarColor;
        if (DBG_NAV_BAR_COLOR_CUST) {
            Slog.d(TAG, "updateNavbarColorLw() useNavBarColorBecauseOfIME=" + useNavBarColorBecauseOfIME
                    + ", sysUi=0x" + Integer.toHexString(sysUiVis)
                    + ", isChecking=" + isChecking
                    + ", mInputMethodChildWinBehindNavBar=" + mInputMethodChildWinBehindNavBar
                    + ", mDisplayRotation=" + mDisplayRotation
                    + ", contentInsets=" + contentInsets);
            Slog.d(TAG, "updateNavbarColorLw() navBarColor=" + Integer.toHexString(navBarColor)
                    + ", hasWinSetNavBarColor=" + hasWinSetNavBarColor
                    + ", enableSystemUINavBarColorBackground=" + enableSystemUINavBarColorBackground);
        }

        if (mLastDiaptchedNavBarColor != navBarColor 
            || mLastDispatchedEnableSysUINavBarColorBg != enableSystemUINavBarColorBackground
            || mLastHasWinSetNavBarColor != hasWinSetNavBarColor
            || (isChecking && (mTempNavBarColor != navBarColor 
                    || mTempEnableSysUINavBarColorBg != enableSystemUINavBarColorBackground
                    || mTempHasWinSetNavBarColor != hasWinSetNavBarColor))) {
            mTempNavBarColor = navBarColor;
            mTempHasWinSetNavBarColor = hasWinSetNavBarColor;
            mTempEnableSysUINavBarColorBg = enableSystemUINavBarColorBackground;
            if (isChecking) {
                return true;
            }

            long delay = 0;
            if (mLastHasWinSetNavBarColor && hasWinSetNavBarColor
                && mLastDispatchedEnableSysUINavBarColorBg && !enableSystemUINavBarColorBackground
                && navBarColor != 0) {
                // If it changes nav-bar color bg mode to app color bg mode, then delay some time
                // to avoid black flash.
                delay = 300; // 300ms
            }
            mLastDiaptchedNavBarColor = navBarColor;
            mLastHasWinSetNavBarColor = hasWinSetNavBarColor;
            mLastDispatchedEnableSysUINavBarColorBg = enableSystemUINavBarColorBackground;
            postNavBarColorRunnableLw(focusWin, navBarColor, hasWinSetNavBarColor, enableSystemUINavBarColorBackground, delay);            
            return true;
        }

        return false;
    }
    // @}
    
    private int updateSystemUiVisibilityLw() {
        // If there is no window focused, there will be nobody to handle the events
        // anyway, so just hang on in whatever state we're in until things settle down.
        WindowState winCandidate = mFocusedWindow != null ? mFocusedWindow
                : mTopFullscreenOpaqueWindowState;
        if (winCandidate == null) {
            return 0;
        }
        if (winCandidate.getAttrs().token == mImmersiveModeConfirmation.getWindowToken()) {
            // The immersive mode confirmation should never affect the system bar visibility,
            // otherwise it will unhide the navigation bar and hide itself.
            winCandidate = isStatusBarKeyguard() ? mStatusBar : mTopFullscreenOpaqueWindowState;
            if (winCandidate == null) {
                return 0;
            }
        }
        //2018-3-20 liyongli fix 47847, pop close || exchange menu, then push HOME, navBar blur
        String str0  = ""+winCandidate.toString();
        if( str0.contains("prizeDivMenu") ||  mWindowManagerInternal.isDockedDividerResizing()){
            return 0;
        }
        
        /* prize-add-split screen,  (portrait) the statusBar property ONLY use Docked win  -liyongli-20171018-start */
        if( PrizeOption.PRIZE_SPLIT_SCREEN_STATUBAR_USE_DOCKEDWIN 
             && mWindowManagerInternal.isStackVisible(DOCKED_STACK_ID)  //is split screen mode 
           ){
            if( DOCKED_STACK_ID!=winCandidate.getStackId() ){
                Slog.d(TAG, "split screen mode, updateSystemUiVisibilityLw use  " +winCandidate.getStackId()
                              +" "+mTopDockedOpaqueWindowState); //winCandidate.getOwningPackage()
                if( mTopDockedOpaqueWindowState !=null ){
                    winCandidate = mTopDockedOpaqueWindowState;
                }
/*                if( mTopDockedOpaqueWindowState ==null ){//2017/12/21  add fix bug 44839 statubar blink
                    //Log.d(TAG, "updateSystemUiVisibilityLw mFocusedWindow= "+mFocusedWindow + " "+winCandidate);
                    return 0;
                }
                // use the dockedstack WindowState
                winCandidate = mTopDockedOpaqueWindowState;*/
            }
        }
        final WindowState win = winCandidate;
        if ((win.getAttrs().privateFlags & PRIVATE_FLAG_KEYGUARD) != 0 && mHideLockScreen == true) {
            // We are updating at a point where the keyguard has gotten
            // focus, but we were last in a state where the top window is
            // hiding it.  This is probably because the keyguard as been
            // shown while the top window was displayed, so we want to ignore
            // it here because this is just a very transient change and it
            // will quickly lose focus once it correctly gets hidden.
            return 0;
        }

        int tmpVisibility = PolicyControl.getSystemUiVisibility(win, null)
                & ~mResettingSystemUiFlags
                & ~mForceClearedSystemUiFlags;
        if (mForcingShowNavBar && win.getSurfaceLayer() < mForcingShowNavBarLayer) {
            tmpVisibility &= ~PolicyControl.adjustClearableFlags(win, View.SYSTEM_UI_CLEARABLE_FLAGS);
        }

        final int fullscreenVisibility = updateLightStatusBarLw(0 /* vis */,
                mTopFullscreenOpaqueWindowState, mTopFullscreenOpaqueOrDimmingWindowState);
        final int dockedVisibility = updateLightStatusBarLw(0 /* vis */,
                mTopDockedOpaqueWindowState, mTopDockedOpaqueOrDimmingWindowState);
        mWindowManagerFuncs.getStackBounds(HOME_STACK_ID, mNonDockedStackBounds);
        mWindowManagerFuncs.getStackBounds(DOCKED_STACK_ID, mDockedStackBounds);
        final int visibility = updateSystemBarsLw(win, mLastSystemUiFlags, tmpVisibility);
        final int diff = visibility ^ mLastSystemUiFlags;
        final int fullscreenDiff = fullscreenVisibility ^ mLastFullscreenStackSysUiFlags;
        final int dockedDiff = dockedVisibility ^ mLastDockedStackSysUiFlags;
        final boolean needsMenu = win.getNeedsMenuLw(mTopFullscreenOpaqueWindowState);

        // Nav bar color customized feature. prize-linkh-20170801@{
        if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            WindowState winNavBar = win;
            final boolean dockedStackVisible = mWindowManagerInternal.isStackVisible(DOCKED_STACK_ID);
            //liyongli  2018/3/23  fix 53452, remove _0||_180  
            if( /*(mDisplayRotation==Surface.ROTATION_0||mDisplayRotation==Surface.ROTATION_180)
                &&*/ dockedStackVisible  //is split screen mode 
                && mTopFullscreenOpaqueWindowState!=null
            ){
                //mWindowManagerInternal.isStackVisible(HOME_STACK_ID)
                winNavBar = mTopFullscreenOpaqueWindowState;
                 //because recentsActivity is  BLUR and transparent navbar ,
                 //but in split screen navbar can't show the BLUR background,
                 //so  set navbat opaque. other app in split use it's property
                if( winNavBar.getStackId() == HOME_STACK_ID){
//                    winNavBar = win; //navbar use the up window APP property, --- not use
                }
            }

            //updateNavbarColorLw(winNavBar, visibility, false);
            //replace with bellow, liyongli modify for split screen navbar color cust
            if(mNavigationBarPosition == NAV_BAR_LEFT||!dockedStackVisible){
                updateNavbarColorLw(win, visibility, false);
            }else if(dockedStackVisible){
                //liyongli  2018/3/23  fix 53452, remove _0||_180  
                if( mFocusedWindow!=null /*&& (mDisplayRotation==Surface.ROTATION_0||mDisplayRotation==Surface.ROTATION_180)*/ ){
                    //mFocusedWindow.getWindowTag();
                    String strTmp  = winNavBar.toString();
                    int defColor = getSystemCustNavBarColor();
                    
                    //2018/3/20  for HOME fix 47847
                    if( mFocusedWindow.toString().contains("com.android.launcher3") ){
                    //if( mFocusedWindow.getStackId() == HOME_STACK_ID){
                        winNavBar = mFocusedWindow;
                    }
                    
                    //if(false == mFocusedWindow.isAnimatingLw() && strTmp.contains("com.android.launcher3"))
                    if( !strTmp.contains("com.android.systemui") )
                    {//fix 47847
                        final int visibilityBottomWin = updateSystemBarsLw(winNavBar, mLastSystemUiFlags, tmpVisibility);
                        updateNavbarColorLw(winNavBar, visibilityBottomWin, false);
                    }
                    else{//split screen  recents activity navBar, force use the  getSystemCustNavBarColor,  2018/3/24 liyongli
                        //color=0xffededed, hasWinSetNavBarColor=true, enableSysUINavBarColorBg=true
                        //color=0xfff4dade, hasWinSetNavBarColor=true, enableSysUINavBarColorBg=false
                        boolean enableSysUINavBarColorBg = (defColor==getSystemCustNavBarColor()) ? true : false;
                        if( mLastDiaptchedNavBarColor != defColor 
                              || mLastDispatchedEnableSysUINavBarColorBg!=enableSysUINavBarColorBg){
                            mLastDiaptchedNavBarColor = defColor;
                            mLastHasWinSetNavBarColor = true;
                            mLastDispatchedEnableSysUINavBarColorBg = enableSysUINavBarColorBg;//true;
                            postNavBarColorRunnableLw(winNavBar, mLastDiaptchedNavBarColor, true, enableSysUINavBarColorBg, 0);    
                        }
                        
                    }

 /*                   if( strTmp !=null && strTmp.contains("com.android.gallery3d/com.android.gallery3d.app.MovieActivity") ){
                        //color=0x0, hasWinSetNavBarColor=false, enableSysUINavBarColorBg=false, win=u0 com.android.gallery3d/com.android.gallery3d.app.MovieActivity
                        if( mLastDiaptchedNavBarColor!=0){
                            mLastDiaptchedNavBarColor = 0;
                            mLastHasWinSetNavBarColor = false;
                            mLastDispatchedEnableSysUINavBarColorBg = false;
                            postNavBarColorRunnableLw(winNavBar, mLastDiaptchedNavBarColor, false, false, 0);    
                        }
                    }else if( strTmp !=null && strTmp.contains("com.android.calculator2") ){
                        //Color=0xff373b4a, hasWinSetNavBarColor=true, enableSysUINavBarColorBg=false, 
                        //win=com.android.calculator2/com.android.calculator2.Calculator} 
                        defColor = 0xff373b4a;
                        if( mLastDiaptchedNavBarColor!=defColor){
                            mLastDiaptchedNavBarColor = defColor;
                            mLastHasWinSetNavBarColor = true;
                            mLastDispatchedEnableSysUINavBarColorBg = false;
                            postNavBarColorRunnableLw(winNavBar, mLastDiaptchedNavBarColor, true, false, 0);    
                        }
                        
                    }else{
                        //color=0xffededed, hasWinSetNavBarColor=true, enableSysUINavBarColorBg=true
                        //color=0xfff4dade, hasWinSetNavBarColor=true, enableSysUINavBarColorBg=false
                        boolean enableSysUINavBarColorBg = (defColor==getSystemCustNavBarColor()) ? true : false;
                        if( mLastDiaptchedNavBarColor != defColor 
                              || mLastDispatchedEnableSysUINavBarColorBg!=enableSysUINavBarColorBg){
                            mLastDiaptchedNavBarColor = defColor;
                            mLastHasWinSetNavBarColor = true;
                            mLastDispatchedEnableSysUINavBarColorBg = enableSysUINavBarColorBg;//true;
                            postNavBarColorRunnableLw(winNavBar, mLastDiaptchedNavBarColor, true, enableSysUINavBarColorBg, 0);    
                        }
                        
                    }*/
                }
//                if(mWindowManagerInternal.isDockedDividerResizing()==false //into split screen anim finished
//                     && winNavBar.isAnimatingLw() //open app anim finished
//                )
                {
//                    updateNavbarColorLw(winNavBar, visibility, false);
                }
            }//end liyongli add
        } // }@

        //add for status bar inverse style. prize-linkh-20150906
        if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR) {
            updateStatusBarInverseStyle(win);
        } 
        //end....
        
        if (diff == 0 && fullscreenDiff == 0 && dockedDiff == 0 && mLastFocusNeedsMenu == needsMenu
                && mFocusedApp == win.getAppToken()
                && mLastNonDockedStackBounds.equals(mNonDockedStackBounds)
                && mLastDockedStackBounds.equals(mDockedStackBounds)) {
            return 0;
        }
        mLastSystemUiFlags = visibility;
        mLastFullscreenStackSysUiFlags = fullscreenVisibility;
        mLastDockedStackSysUiFlags = dockedVisibility;
        mLastFocusNeedsMenu = needsMenu;
        mFocusedApp = win.getAppToken();
        final Rect fullscreenStackBounds = new Rect(mNonDockedStackBounds);
        final Rect dockedStackBounds = new Rect(mDockedStackBounds);
        mHandler.post(new Runnable() {
                @Override
                public void run() {
                    StatusBarManagerInternal statusbar = getStatusBarManagerInternal();
                    if (statusbar != null) {
                        statusbar.setSystemUiVisibility(visibility, fullscreenVisibility,
                                dockedVisibility, 0xffffffff, fullscreenStackBounds,
                                dockedStackBounds, win.toString());
                        statusbar.topAppWindowChanged(needsMenu);
                    }
                }
            });
        return diff;
    }

    private int updateLightStatusBarLw(int vis, WindowState opaque, WindowState opaqueOrDimming) {
        WindowState statusColorWin = isStatusBarKeyguard() && !mHideLockScreen
                ? mStatusBar
                : opaqueOrDimming;

        if (statusColorWin != null) {
            if (statusColorWin == opaque) {
                // If the top fullscreen-or-dimming window is also the top fullscreen, respect
                // its light flag.
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                vis |= PolicyControl.getSystemUiVisibility(statusColorWin, null)
                        & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else if (statusColorWin != null && statusColorWin.isDimming()) {
                // Otherwise if it's dimming, clear the light flag.
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        }
        return vis;
    }

    private boolean drawsSystemBarBackground(WindowState win) {
        return win == null || (win.getAttrs().flags & FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) != 0;
    }

    private boolean forcesDrawStatusBarBackground(WindowState win) {
        return win == null || (win.getAttrs().privateFlags
                & PRIVATE_FLAG_FORCE_DRAW_STATUS_BAR_BACKGROUND) != 0;
    }

    private int updateSystemBarsLw(WindowState win, int oldVis, int vis) {
        final boolean dockedStackVisible = mWindowManagerInternal.isStackVisible(DOCKED_STACK_ID);
        final boolean freeformStackVisible =
                mWindowManagerInternal.isStackVisible(FREEFORM_WORKSPACE_STACK_ID);
        final boolean resizing = mWindowManagerInternal.isDockedDividerResizing();

        // We need to force system bars when the docked stack is visible, when the freeform stack
        // is visible but also when we are resizing for the transitions when docked stack
        // visibility changes.
        mForceShowSystemBars = dockedStackVisible || freeformStackVisible || resizing;
        final boolean forceOpaqueStatusBar = mForceShowSystemBars && !mForceStatusBarFromKeyguard;

        // apply translucent bar vis flags
        WindowState fullscreenTransWin = isStatusBarKeyguard() && !mHideLockScreen
                ? mStatusBar
                : mTopFullscreenOpaqueWindowState;
                
        /* prize-add-split screen,  (portrait) the statusBar property ONLY use Docked win  -liyongli-20171018-start */
        if( PrizeOption.PRIZE_SPLIT_SCREEN_STATUBAR_USE_DOCKEDWIN&& dockedStackVisible ){
            //fullscreenTransWin = null; 
            fullscreenTransWin = mTopDockedOpaqueWindowState; //to  applyTranslucentFlagLw [QQ/Recents]  [QQ/Setting]
        }/* prize-add-split screen-liyongli-20171018-end */
        
        vis = mStatusBarController.applyTranslucentFlagLw(fullscreenTransWin, vis, oldVis);
        vis = mNavigationBarController.applyTranslucentFlagLw(fullscreenTransWin, vis, oldVis);
        final int dockedVis = mStatusBarController.applyTranslucentFlagLw(
                mTopDockedOpaqueWindowState, 0, 0);

        final boolean fullscreenDrawsStatusBarBackground =
                (drawsSystemBarBackground(mTopFullscreenOpaqueWindowState)
                        && (vis & View.STATUS_BAR_TRANSLUCENT) == 0)
                || forcesDrawStatusBarBackground(mTopFullscreenOpaqueWindowState);
        final boolean dockedDrawsStatusBarBackground =
                (drawsSystemBarBackground(mTopDockedOpaqueWindowState)
                        && (dockedVis & View.STATUS_BAR_TRANSLUCENT) == 0)
                || forcesDrawStatusBarBackground(mTopDockedOpaqueWindowState);

        // prevent status bar interaction from clearing certain flags
        int type = win.getAttrs().type;
        boolean statusBarHasFocus = type == TYPE_STATUS_BAR;
        if (statusBarHasFocus && !isStatusBarKeyguard()) {
            int flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (mHideLockScreen) {
                flags |= View.STATUS_BAR_TRANSLUCENT | View.NAVIGATION_BAR_TRANSLUCENT;
            }
            vis = (vis & ~flags) | (oldVis & flags);
        }

        if (fullscreenDrawsStatusBarBackground && dockedDrawsStatusBarBackground) {
            vis |= View.STATUS_BAR_TRANSPARENT;
            vis &= ~View.STATUS_BAR_TRANSLUCENT;
        } else if ((!areTranslucentBarsAllowed() && fullscreenTransWin != mStatusBar)
                || forceOpaqueStatusBar) {
            /* prize-add-split screen,  use inverse color, not remove app translucent setting(e.g. QQ)  -liyongli-20171018-start */
            if( PrizeOption.PRIZE_SPLIT_SCREEN_STATUSBAR_INVERSE_COLOR ){
                if(!dockedStackVisible){
                    vis &= ~(View.STATUS_BAR_TRANSLUCENT | View.STATUS_BAR_TRANSPARENT);
                }
            }else /* prize-add-split screen-liyongli-20171018-end */
            {            
            vis &= ~(View.STATUS_BAR_TRANSLUCENT | View.STATUS_BAR_TRANSPARENT);
            }
        }
        
        /* prize-add-split screen -liyongli-20171214 start*/
        //split mode, only RecentsActivity show in bottom screen, navbar set opaque; otherwise not set navbar  opaque;
        if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            if(dockedStackVisible 
                && fullscreenTransWin != null
                && fullscreenTransWin.getAttrs() != null
                && fullscreenTransWin.getAttrs().statusBarInverse < 0 //APP not use statubar inverse color
               ){
                //sogou.mobile.explorer in split screen background color same statubar color , so remove transparent.
                vis &= ~( View.STATUS_BAR_TRANSPARENT);
            }
        }else /* prize-add-split screen -liyongli-20171214 end */
        {
        vis = configureNavBarOpacity(vis, dockedStackVisible, freeformStackVisible, resizing);
        }
        /* prize-add-split screen-liyongli-20171017-start */
        if( PrizeOption.PRIZE_SPLIT_SCREEN_LANDSCAPE_ONE_STATUBAR && dockedStackVisible
            && (mDisplayRotation==Surface.ROTATION_90||mDisplayRotation==Surface.ROTATION_270)
        ){
            vis = View.SYSTEM_UI_FLAG_VISIBLE;
        }
        /* prize-add-split screen-liyongli-20171017-end */

        // update status bar
        boolean immersiveSticky =
                (vis & View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) != 0;
        final boolean hideStatusBarWM =
                mTopFullscreenOpaqueWindowState != null
                && (PolicyControl.getWindowFlags(mTopFullscreenOpaqueWindowState, null)
                        & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        final boolean hideStatusBarSysui =
                (vis & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0;
        final boolean hideNavBarSysui =
                (vis & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0;

        final boolean transientStatusBarAllowed = mStatusBar != null
                && (statusBarHasFocus || (!mForceShowSystemBars
                        && (hideStatusBarWM || (hideStatusBarSysui && immersiveSticky))));

        final boolean transientNavBarAllowed = mNavigationBar != null
                && !mForceShowSystemBars && hideNavBarSysui && immersiveSticky;

        final long now = SystemClock.uptimeMillis();
        final boolean pendingPanic = mPendingPanicGestureUptime != 0
                && now - mPendingPanicGestureUptime <= PANIC_GESTURE_EXPIRATION;
        if (pendingPanic && hideNavBarSysui && !isStatusBarKeyguard() && mKeyguardDrawComplete) {
            // The user performed the panic gesture recently, we're about to hide the bars,
            // we're no longer on the Keyguard and the screen is ready. We can now request the bars.
            mPendingPanicGestureUptime = 0;
            mStatusBarController.showTransient();
            if (!isNavBarEmpty(vis)) {
                mNavigationBarController.showTransient();
            }
        }

        final boolean denyTransientStatus = mStatusBarController.isTransientShowRequested()
                && !transientStatusBarAllowed && hideStatusBarSysui;
        final boolean denyTransientNav = mNavigationBarController.isTransientShowRequested()
                && !transientNavBarAllowed;
        if (denyTransientStatus || denyTransientNav || mForceShowSystemBars) {
            // clear the clearable flags instead
            clearClearableFlagsLw();
            vis &= ~View.SYSTEM_UI_CLEARABLE_FLAGS;
        }

        final boolean immersive = (vis & View.SYSTEM_UI_FLAG_IMMERSIVE) != 0;
        immersiveSticky = (vis & View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) != 0;
        final boolean navAllowedHidden = immersive || immersiveSticky;

        if (hideNavBarSysui && !navAllowedHidden && windowTypeToLayerLw(win.getBaseType())
                > windowTypeToLayerLw(TYPE_INPUT_CONSUMER)) {
            // We can't hide the navbar from this window otherwise the input consumer would not get
            // the input events.
            vis = (vis & ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        vis = mStatusBarController.updateVisibilityLw(transientStatusBarAllowed, oldVis, vis);

        // update navigation bar
        boolean oldImmersiveMode = isImmersiveMode(oldVis);
        boolean newImmersiveMode = isImmersiveMode(vis);
        if (win != null && oldImmersiveMode != newImmersiveMode
            /// M: When gesture disabled, don't show the immersive mode user guide
            && (win.getSystemUiVisibility()
            & View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED) == 0) {
            final String pkg = win.getOwningPackage();
            mImmersiveModeConfirmation.immersiveModeChangedLw(pkg, newImmersiveMode,
                    isUserSetupComplete(), isNavBarEmpty(win.getSystemUiVisibility()));
        }

        vis = mNavigationBarController.updateVisibilityLw(transientNavBarAllowed, oldVis, vis);
        
        //add for status bar inverse style. prize-linkh-20150906
        // Isssue: 
        // Some apps(eg, weibo) will enable Light status bar mode and
        // light status bar mode is confilct with status bar inverse mode. So 
        // we filter out light & transparent flags. 
        if (PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR && (vis & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0
              && win != null && win != mStatusBar && win != mNavigationBar) {
            if (DEBUG) {
                Slog.d(TAG, "onStatusBarInverseChanged(): Remove View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR & View.SYSTEM_UI_TRANSPARENT flags");            
            }
            int mask = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.STATUS_BAR_TRANSPARENT;
            if (DEBUG) {
                Slog.d(TAG, "onStatusBarInverseChanged(): oldVis=0x" + Integer.toHexString(vis));
            }
            vis &= ~mask;
            if (DEBUG) {
                Slog.d(TAG, "onStatusBarInverseChanged(): newVis=0x" + Integer.toHexString(vis));
            }
        } //END....
        
        /*prize-add- 0 180 rotation, fullscreen app,  cut lcd ears then Force SHOW statusbar-liyongli-20180510-start*/
        if( PrizeOption.PRIZE_NOTCH_SCREEN&&win.getAttrs().prizeWinMoveEarlcdBottom==1){
            vis &= ~(View.STATUS_BAR_TRANSLUCENT | View.STATUS_BAR_TRANSPARENT); 
        }/*prize-add- 0 180 rotation, fullscreen app,  cut lcd ears then Force SHOW statusbar-liyongli-20180510-end*/
        
        return vis;
    }

    /**
     * @return the current visibility flags with the nav-bar opacity related flags toggled based
     *         on the nav bar opacity rules chosen by {@link #mNavBarOpacityMode}.
     */
    private int configureNavBarOpacity(int visibility, boolean dockedStackVisible,
            boolean freeformStackVisible, boolean isDockedDividerResizing) {
        if (mNavBarOpacityMode == NAV_BAR_OPAQUE_WHEN_FREEFORM_OR_DOCKED) {
            if (dockedStackVisible || freeformStackVisible || isDockedDividerResizing) {
                visibility = setNavBarOpaqueFlag(visibility);
            }
        } else if (mNavBarOpacityMode == NAV_BAR_TRANSLUCENT_WHEN_FREEFORM_OPAQUE_OTHERWISE) {
            if (isDockedDividerResizing) {
                visibility = setNavBarOpaqueFlag(visibility);
            } else if (freeformStackVisible) {
                visibility = setNavBarTranslucentFlag(visibility);
            } else {
                visibility = setNavBarOpaqueFlag(visibility);
            }
        }

        if (!areTranslucentBarsAllowed()) {
            visibility &= ~View.NAVIGATION_BAR_TRANSLUCENT;
        }
        return visibility;
    }

    private int setNavBarOpaqueFlag(int visibility) {
        return visibility &= ~(View.NAVIGATION_BAR_TRANSLUCENT | View.NAVIGATION_BAR_TRANSPARENT);
    }

    private int setNavBarTranslucentFlag(int visibility) {
        visibility &= ~View.NAVIGATION_BAR_TRANSPARENT;
        return visibility |= View.NAVIGATION_BAR_TRANSLUCENT;
    }

    private void clearClearableFlagsLw() {
        int newVal = mResettingSystemUiFlags | View.SYSTEM_UI_CLEARABLE_FLAGS;
        if (newVal != mResettingSystemUiFlags) {
            mResettingSystemUiFlags = newVal;
            mWindowManagerFuncs.reevaluateStatusBarVisibility();
        }
    }

    private boolean isImmersiveMode(int vis) {
        final int flags = View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        return mNavigationBar != null
                && (vis & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
                && (vis & flags) != 0
                && canHideNavigationBar();
    }

    private static boolean isNavBarEmpty(int systemUiFlags) {
        final int disableNavigationBar = (View.STATUS_BAR_DISABLE_HOME
                | View.STATUS_BAR_DISABLE_BACK
                | View.STATUS_BAR_DISABLE_RECENT);

        return (systemUiFlags & disableNavigationBar) == disableNavigationBar;
    }

    /**
     * @return whether the navigation or status bar can be made translucent
     *
     * This should return true unless touch exploration is not enabled or
     * R.boolean.config_enableTranslucentDecor is false.
     */
    private boolean areTranslucentBarsAllowed() {
        return mTranslucentDecorEnabled;
    }

    // Use this instead of checking config_showNavigationBar so that it can be consistently
    // overridden by qemu.hw.mainkeys in the emulator.
    @Override
    public boolean hasNavigationBar() {
        return mHasNavigationBar;
    }

    @Override
    public void setLastInputMethodWindowLw(WindowState ime, WindowState target) {
        mLastInputMethodWindow = ime;
        mLastInputMethodTargetWindow = target;
    }

    @Override
    public int getInputMethodWindowVisibleHeightLw() {
        return mDockBottom - mCurBottom;
    }

    @Override
    public void setCurrentUserLw(int newUserId) {
        mCurrentUserId = newUserId;
        if (mKeyguardDelegate != null) {
            mKeyguardDelegate.setCurrentUser(newUserId);
        }
        StatusBarManagerInternal statusBar = getStatusBarManagerInternal();
        if (statusBar != null) {
            statusBar.setCurrentUser(newUserId);
        }
        setLastInputMethodWindowLw(null, null);
    }

    @Override
    public boolean canMagnifyWindow(int windowType) {
        switch (windowType) {
            case WindowManager.LayoutParams.TYPE_INPUT_METHOD:
            case WindowManager.LayoutParams.TYPE_INPUT_METHOD_DIALOG:
            case WindowManager.LayoutParams.TYPE_NAVIGATION_BAR:
            case WindowManager.LayoutParams.TYPE_MAGNIFICATION_OVERLAY: {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isTopLevelWindow(int windowType) {
        if (windowType >= WindowManager.LayoutParams.FIRST_SUB_WINDOW
                && windowType <= WindowManager.LayoutParams.LAST_SUB_WINDOW) {
            return (windowType == WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
        }
        return true;
    }

    @Override
    public boolean shouldRotateSeamlessly(int oldRotation, int newRotation) {
        // For the upside down rotation we don't rotate seamlessly as the navigation
        // bar moves position.
        // Note most apps (using orientation:sensor or user as opposed to fullSensor)
        // will not enter the reverse portrait orientation, so actually the
        // orientation won't change at all.
        if (oldRotation == mUpsideDownRotation || newRotation == mUpsideDownRotation) {
            return false;
        }
        int delta = newRotation - oldRotation;
        if (delta < 0) delta += 4;
        // Likewise we don't rotate seamlessly for 180 degree rotations
        // in this case the surfaces never resize, and our logic to
        // revert the transformations on size change will fail. We could
        // fix this in the future with the "tagged" frames idea.
        if (delta == Surface.ROTATION_180) {
            return false;
        }

        final WindowState w = mTopFullscreenOpaqueWindowState;
        if (w != mFocusedWindow) {
            return false;
        }

        // We only enable seamless rotation if the top window has requested
        // it and is in the fullscreen opaque state. Seamless rotation
        // requires freezing various Surface states and won't work well
        // with animations, so we disable it in the animation case for now.
        if (w != null && !w.isAnimatingLw() &&
                ((w.getAttrs().rotationAnimation == ROTATION_ANIMATION_JUMPCUT) ||
                        (w.getAttrs().rotationAnimation == ROTATION_ANIMATION_SEAMLESS))) {
            return true;
        }
        return false;
    }

    @Override
    public void dump(String prefix, PrintWriter pw, String[] args) {
        /// M: MTK local varibles
        pw.print(prefix); pw.print("mIsAlarmBoot="); pw.print(mIsAlarmBoot);
                pw.print(" mIPOUserRotation="); pw.print(mIPOUserRotation);
                pw.print(" mIsShutDown="); pw.print(mIsShutDown);
                pw.print(" mScreenOffReason="); pw.print(mScreenOffReason);
                pw.print(" mIsAlarmBoot="); pw.print(mIsAlarmBoot);
                synchronized (mKeyDispatchLock) {
                    pw.print(" mKeyDispatcMode="); pw.println(mKeyDispatcMode);
                }
        pw.print(prefix); pw.print("mSafeMode="); pw.print(mSafeMode);
                pw.print(" mSystemReady="); pw.print(mSystemReady);
                pw.print(" mSystemBooted="); pw.println(mSystemBooted);
        pw.print(prefix); pw.print("mLidState="); pw.print(mLidState);
                pw.print(" mLidOpenRotation="); pw.print(mLidOpenRotation);
                pw.print(" mCameraLensCoverState="); pw.print(mCameraLensCoverState);
                pw.print(" mHdmiPlugged="); pw.println(mHdmiPlugged);
        if (mLastSystemUiFlags != 0 || mResettingSystemUiFlags != 0
                || mForceClearedSystemUiFlags != 0) {
            pw.print(prefix); pw.print("mLastSystemUiFlags=0x");
                    pw.print(Integer.toHexString(mLastSystemUiFlags));
                    pw.print(" mResettingSystemUiFlags=0x");
                    pw.print(Integer.toHexString(mResettingSystemUiFlags));
                    pw.print(" mForceClearedSystemUiFlags=0x");
                    pw.println(Integer.toHexString(mForceClearedSystemUiFlags));
        }
        if (mLastFocusNeedsMenu) {
            pw.print(prefix); pw.print("mLastFocusNeedsMenu=");
                    pw.println(mLastFocusNeedsMenu);
        }
        pw.print(prefix); pw.print("mWakeGestureEnabledSetting=");
                pw.println(mWakeGestureEnabledSetting);

        pw.print(prefix); pw.print("mSupportAutoRotation="); pw.println(mSupportAutoRotation);
        pw.print(prefix); pw.print("mUiMode="); pw.print(mUiMode);
                pw.print(" mDockMode="); pw.print(mDockMode);
                pw.print(" mEnableCarDockHomeCapture="); pw.print(mEnableCarDockHomeCapture);
                pw.print(" mCarDockRotation="); pw.print(mCarDockRotation);
                pw.print(" mDeskDockRotation="); pw.println(mDeskDockRotation);
        pw.print(prefix); pw.print("mUserRotationMode="); pw.print(mUserRotationMode);
                pw.print(" mUserRotation="); pw.print(mUserRotation);
                pw.print(" mAllowAllRotations="); pw.println(mAllowAllRotations);
        pw.print(prefix); pw.print("mCurrentAppOrientation="); pw.println(mCurrentAppOrientation);
        pw.print(prefix); pw.print("mCarDockEnablesAccelerometer=");
                pw.print(mCarDockEnablesAccelerometer);
                pw.print(" mDeskDockEnablesAccelerometer=");
                pw.println(mDeskDockEnablesAccelerometer);
        pw.print(prefix); pw.print("mLidKeyboardAccessibility=");
                pw.print(mLidKeyboardAccessibility);
                pw.print(" mLidNavigationAccessibility="); pw.print(mLidNavigationAccessibility);
                pw.print(" mLidControlsScreenLock="); pw.println(mLidControlsScreenLock);
                pw.print(" mLidControlsSleep="); pw.println(mLidControlsSleep);
        pw.print(prefix);
                pw.print(" mLongPressOnBackBehavior="); pw.println(mLongPressOnBackBehavior);
        pw.print(prefix);
                pw.print("mShortPressOnPowerBehavior="); pw.print(mShortPressOnPowerBehavior);
                pw.print(" mLongPressOnPowerBehavior="); pw.println(mLongPressOnPowerBehavior);
        pw.print(prefix);
                pw.print("mDoublePressOnPowerBehavior="); pw.print(mDoublePressOnPowerBehavior);
                pw.print(" mTriplePressOnPowerBehavior="); pw.println(mTriplePressOnPowerBehavior);
        pw.print(prefix); pw.print("mHasSoftInput="); pw.println(mHasSoftInput);
        pw.print(prefix); pw.print("mAwake="); pw.println(mAwake);
        pw.print(prefix); pw.print("mScreenOnEarly="); pw.print(mScreenOnEarly);
                pw.print(" mScreenOnFully="); pw.println(mScreenOnFully);
        pw.print(prefix); pw.print("mKeyguardDrawComplete="); pw.print(mKeyguardDrawComplete);
                pw.print(" mWindowManagerDrawComplete="); pw.println(mWindowManagerDrawComplete);
        pw.print(prefix); pw.print("mOrientationSensorEnabled=");
                pw.println(mOrientationSensorEnabled);
        pw.print(prefix); pw.print("mOverscanScreen=("); pw.print(mOverscanScreenLeft);
                pw.print(","); pw.print(mOverscanScreenTop);
                pw.print(") "); pw.print(mOverscanScreenWidth);
                pw.print("x"); pw.println(mOverscanScreenHeight);
        if (mOverscanLeft != 0 || mOverscanTop != 0
                || mOverscanRight != 0 || mOverscanBottom != 0) {
            pw.print(prefix); pw.print("mOverscan left="); pw.print(mOverscanLeft);
                    pw.print(" top="); pw.print(mOverscanTop);
                    pw.print(" right="); pw.print(mOverscanRight);
                    pw.print(" bottom="); pw.println(mOverscanBottom);
        }
        pw.print(prefix); pw.print("mRestrictedOverscanScreen=(");
                pw.print(mRestrictedOverscanScreenLeft);
                pw.print(","); pw.print(mRestrictedOverscanScreenTop);
                pw.print(") "); pw.print(mRestrictedOverscanScreenWidth);
                pw.print("x"); pw.println(mRestrictedOverscanScreenHeight);
        pw.print(prefix); pw.print("mUnrestrictedScreen=("); pw.print(mUnrestrictedScreenLeft);
                pw.print(","); pw.print(mUnrestrictedScreenTop);
                pw.print(") "); pw.print(mUnrestrictedScreenWidth);
                pw.print("x"); pw.println(mUnrestrictedScreenHeight);
        pw.print(prefix); pw.print("mRestrictedScreen=("); pw.print(mRestrictedScreenLeft);
                pw.print(","); pw.print(mRestrictedScreenTop);
                pw.print(") "); pw.print(mRestrictedScreenWidth);
                pw.print("x"); pw.println(mRestrictedScreenHeight);
        pw.print(prefix); pw.print("mStableFullscreen=("); pw.print(mStableFullscreenLeft);
                pw.print(","); pw.print(mStableFullscreenTop);
                pw.print(")-("); pw.print(mStableFullscreenRight);
                pw.print(","); pw.print(mStableFullscreenBottom); pw.println(")");
        pw.print(prefix); pw.print("mStable=("); pw.print(mStableLeft);
                pw.print(","); pw.print(mStableTop);
                pw.print(")-("); pw.print(mStableRight);
                pw.print(","); pw.print(mStableBottom); pw.println(")");
        pw.print(prefix); pw.print("mSystem=("); pw.print(mSystemLeft);
                pw.print(","); pw.print(mSystemTop);
                pw.print(")-("); pw.print(mSystemRight);
                pw.print(","); pw.print(mSystemBottom); pw.println(")");
        pw.print(prefix); pw.print("mCur=("); pw.print(mCurLeft);
                pw.print(","); pw.print(mCurTop);
                pw.print(")-("); pw.print(mCurRight);
                pw.print(","); pw.print(mCurBottom); pw.println(")");
        pw.print(prefix); pw.print("mContent=("); pw.print(mContentLeft);
                pw.print(","); pw.print(mContentTop);
                pw.print(")-("); pw.print(mContentRight);
                pw.print(","); pw.print(mContentBottom); pw.println(")");
        pw.print(prefix); pw.print("mVoiceContent=("); pw.print(mVoiceContentLeft);
                pw.print(","); pw.print(mVoiceContentTop);
                pw.print(")-("); pw.print(mVoiceContentRight);
                pw.print(","); pw.print(mVoiceContentBottom); pw.println(")");
        pw.print(prefix); pw.print("mDock=("); pw.print(mDockLeft);
                pw.print(","); pw.print(mDockTop);
                pw.print(")-("); pw.print(mDockRight);
                pw.print(","); pw.print(mDockBottom); pw.println(")");
        pw.print(prefix); pw.print("mDockLayer="); pw.print(mDockLayer);
                pw.print(" mStatusBarLayer="); pw.println(mStatusBarLayer);
        pw.print(prefix); pw.print("mShowingLockscreen="); pw.print(mShowingLockscreen);
                pw.print(" mShowingDream="); pw.print(mShowingDream);
                pw.print(" mDreamingLockscreen="); pw.print(mDreamingLockscreen);
                pw.print(" mDreamingSleepToken="); pw.println(mDreamingSleepToken);
        if (mLastInputMethodWindow != null) {
            pw.print(prefix); pw.print("mLastInputMethodWindow=");
                    pw.println(mLastInputMethodWindow);
        }
        if (mLastInputMethodTargetWindow != null) {
            pw.print(prefix); pw.print("mLastInputMethodTargetWindow=");
                    pw.println(mLastInputMethodTargetWindow);
        }
        if (mStatusBar != null) {
            pw.print(prefix); pw.print("mStatusBar=");
                    pw.print(mStatusBar); pw.print(" isStatusBarKeyguard=");
                    pw.println(isStatusBarKeyguard());
        }
        if (mNavigationBar != null) {
            pw.print(prefix); pw.print("mNavigationBar=");
                    pw.println(mNavigationBar);
        }
        if (mFocusedWindow != null) {
            pw.print(prefix); pw.print("mFocusedWindow=");
                    pw.println(mFocusedWindow);
        }
        if (mFocusedApp != null) {
            pw.print(prefix); pw.print("mFocusedApp=");
                    pw.println(mFocusedApp);
        }
        if (mWinDismissingKeyguard != null) {
            pw.print(prefix); pw.print("mWinDismissingKeyguard=");
                    pw.println(mWinDismissingKeyguard);
        }
        if (mTopFullscreenOpaqueWindowState != null) {
            pw.print(prefix); pw.print("mTopFullscreenOpaqueWindowState=");
                    pw.println(mTopFullscreenOpaqueWindowState);
        }
        if (mTopFullscreenOpaqueOrDimmingWindowState != null) {
            pw.print(prefix); pw.print("mTopFullscreenOpaqueOrDimmingWindowState=");
                    pw.println(mTopFullscreenOpaqueOrDimmingWindowState);
        }
        if (mForcingShowNavBar) {
            pw.print(prefix); pw.print("mForcingShowNavBar=");
                    pw.println(mForcingShowNavBar); pw.print( "mForcingShowNavBarLayer=");
                    pw.println(mForcingShowNavBarLayer);
        }
        pw.print(prefix); pw.print("mTopIsFullscreen="); pw.print(mTopIsFullscreen);
                pw.print(" mHideLockScreen="); pw.println(mHideLockScreen);
        pw.print(prefix); pw.print("mForceStatusBar="); pw.print(mForceStatusBar);
                pw.print(" mForceStatusBarFromKeyguard=");
                pw.println(mForceStatusBarFromKeyguard);
        pw.print(prefix); pw.print("mDismissKeyguard="); pw.print(mDismissKeyguard);
                pw.print(" mCurrentlyDismissingKeyguard="); pw.println(mCurrentlyDismissingKeyguard);
                pw.print(" mWinDismissingKeyguard="); pw.print(mWinDismissingKeyguard);
                pw.print(" mHomePressed="); pw.println(mHomePressed);
        pw.print(prefix); pw.print("mAllowLockscreenWhenOn="); pw.print(mAllowLockscreenWhenOn);
                pw.print(" mLockScreenTimeout="); pw.print(mLockScreenTimeout);
                pw.print(" mLockScreenTimerActive="); pw.println(mLockScreenTimerActive);
        pw.print(prefix); pw.print("mEndcallBehavior="); pw.print(mEndcallBehavior);
                pw.print(" mIncallPowerBehavior="); pw.print(mIncallPowerBehavior);
                pw.print(" mLongPressOnHomeBehavior="); pw.println(mLongPressOnHomeBehavior);
        pw.print(prefix); pw.print("mLandscapeRotation="); pw.print(mLandscapeRotation);
                pw.print(" mSeascapeRotation="); pw.println(mSeascapeRotation);
        pw.print(prefix); pw.print("mPortraitRotation="); pw.print(mPortraitRotation);
                pw.print(" mUpsideDownRotation="); pw.println(mUpsideDownRotation);
        pw.print(prefix); pw.print("mDemoHdmiRotation="); pw.print(mDemoHdmiRotation);
                pw.print(" mDemoHdmiRotationLock="); pw.println(mDemoHdmiRotationLock);
        pw.print(prefix); pw.print("mUndockedHdmiRotation="); pw.println(mUndockedHdmiRotation);

        mGlobalKeyManager.dump(prefix, pw);
        mStatusBarController.dump(pw, prefix);
        mNavigationBarController.dump(pw, prefix);
        PolicyControl.dump(prefix, pw);

        if (mWakeGestureListener != null) {
            mWakeGestureListener.dump(pw, prefix);
        }
        if (mOrientationListener != null) {
            mOrientationListener.dump(pw, prefix);
        }
        if (mBurnInProtectionHelper != null) {
            mBurnInProtectionHelper.dump(prefix, pw);
        }
        if (mKeyguardDelegate != null) {
            mKeyguardDelegate.dump(prefix, pw);
        }

        // Nav bar color customized feature. prize-linkh-2017.08.29 @{
        if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            pw.print(prefix); pw.print("mLastDiaptchedNavBarColor=0x");
                    pw.println(Integer.toHexString(mLastDiaptchedNavBarColor));
            pw.print(prefix); pw.print("mLastDispatchedEnableSysUINavBarColorBg=");
                    pw.println(mLastDispatchedEnableSysUINavBarColorBg);
        } //@}        
    }

    /// M: for build type check
    static final boolean IS_USER_BUILD = ("user".equals(Build.TYPE)
            || "userdebug".equals(Build.TYPE));

    /// M: power-off alarm @{
    private boolean mIsAlarmBoot = isAlarmBoot();
    private boolean mIsShutDown = false;
    private static final String NORMAL_SHUTDOWN_ACTION = "android.intent.action.normal.shutdown";
    private static final String NORMAL_BOOT_ACTION = "android.intent.action.normal.boot";
    ///@}

    ///M : power-off alarm @{
    BroadcastReceiver mPoweroffAlarmReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "mIpoEventReceiver -- onReceive -- entry");
            String action = intent.getAction();
            SystemProperties.set("sys.boot.reason", "0");
            mIsAlarmBoot = false;
            if (action.equals(NORMAL_SHUTDOWN_ACTION)) {
                Log.v(TAG, "Receive NORMAL_SHUTDOWN_ACTION");
                mIsShutDown = true;
            } else if (NORMAL_BOOT_ACTION.equals(action)) {
                Log.v(TAG, "Receive NORMAL_BOOT_ACTION");
                SystemProperties.set("service.bootanim.exit", "0");
                SystemProperties.set("ctl.start", "bootanim");
            }
        }
    };
    ///@}

    /// M: power-off alarm
    ///    add for power-off alarm Check the boot mode whether alarm boot or
    ///    normal boot (including ipo boot). {@
    private boolean isAlarmBoot() {
        String bootReason = SystemProperties.get("sys.boot.reason");
        boolean ret = (bootReason != null && bootReason.equals("1")) ? true
                : false;
        return ret;
    }
    /// @}

    /// M: IPO migration @{
    final Object mKeyDispatchLock = new Object();
    /// M: [ALPS01186555]Fix WUXGA IPO charging animation issue
    int mIPOUserRotation = Surface.ROTATION_0;
    /// M: Disable WakeGesture in IPO ShutDown. {@
    private boolean mIsIpoShutDown = false;
    /// @}
    public static final String IPO_DISABLE = "android.intent.action.ACTION_BOOT_IPO";
    public static final String IPO_ENABLE = "android.intent.action.ACTION_SHUTDOWN_IPO";
    /// @}

    /// M: IPO migration @{
    BroadcastReceiver mIpoEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "mIpoEventReceiver -- onReceive -- entry");
            String action = intent.getAction();
            if (action.equals(IPO_ENABLE)) {
                Log.v(TAG, "Receive IPO_ENABLE");
                /// M: Disable WakeGesture in IPO ShutDown. {@
                mIsIpoShutDown = true;
                /// @}
                ipoSystemShutdown();
            } else if (action.equals(IPO_DISABLE)) {
                Log.v(TAG, "Receive IPO_DISABLE");
                ipoSystemBooted();
                /// M: Disable WakeGesture in IPO ShutDown. {@
                mIsIpoShutDown = false;
                /// @}
            } else {
                Log.v(TAG, "Receive Fake Intent");
            }
        }
    };
    /// @}

    /// M: IPO migration
    ///    Called after IPO system boot @{
    private void ipoSystemBooted() {

        ///M: power-off alarm @{
        mIsAlarmBoot = isAlarmBoot();
        mIsShutDown = false;
        ///@}

        /// M: [ALPS00519547] Reset effect of FLAG_SHOW_WHEN_LOCKED @{
        mHideLockScreen = false;
        /// @}

        /// M:[ALPS00637635]Solve the disappear GlobalActions dialog
        mScreenshotChordVolumeDownKeyTriggered = false;
        mScreenshotChordVolumeUpKeyTriggered = false;

        // Enable key dispatch
        synchronized (mKeyDispatchLock) {
            mKeyDispatcMode = KEY_DISPATCH_MODE_ALL_ENABLE;
            if (DEBUG_INPUT) {
                Log.v(TAG, "mIpoEventReceiver=" + mKeyDispatcMode);
            }
        }
        /// M: [ALPS01186555]Fix WUXGA IPO charging animation issue @{
        if (mIPOUserRotation != Surface.ROTATION_0) {
            mUserRotation = mIPOUserRotation;
            mIPOUserRotation = Surface.ROTATION_0;
        }
        /// @}
    }
    /// @}

    /// M: IPO migration
    ///    Called before IPO system shutdown @{
    private void ipoSystemShutdown() {
        // Disable key dispatch
        synchronized (mKeyDispatchLock) {
            mKeyDispatcMode = KEY_DISPATCH_MODE_ALL_DISABLE;
            if (DEBUG_INPUT) {
                Log.v(TAG, "mIpoEventReceiver=" + mKeyDispatcMode);
            }
        }
        /// M: [ALPS01186555]Fix WUXGA IPO charging animation issue @{
        if (mUserRotationMode == WindowManagerPolicy.USER_ROTATION_LOCKED
            && mUserRotation != Surface.ROTATION_0) {
            mIPOUserRotation = mUserRotation;
            mUserRotation = Surface.ROTATION_0;
        }
        /// @}
    }
    /// @}

    /// M: [ALPS00062902]THE INTENT of STK UserActivity
    public static final String STK_USERACTIVITY =
        "android.intent.action.stk.USER_ACTIVITY";
    public static final String STK_USERACTIVITY_ENABLE =
        "android.intent.action.stk.USER_ACTIVITY.enable";
    /// M: [ALPS00062902]The global variable to save the state of stk.enable.user_activity
    boolean mIsStkUserActivityEnabled = false;
    /// M: [ALPS00062902]Protect mIsStkUserActivityEnabled be accessed at the multiple places
    private Object mStkLock = new Object();

    /// M: [ALPS00062902]
    BroadcastReceiver mStkUserActivityEnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.v(TAG, "mStkUserActivityEnReceiver -- onReceive -- entry");

            synchronized (mStkLock) {
                if (action.equals(STK_USERACTIVITY_ENABLE)) {
                    if (DEBUG_INPUT) {
                        Log.v(TAG, "Receive STK_ENABLE");
                    }
                    boolean enabled = intent.getBooleanExtra("state", false);
                    if (enabled != mIsStkUserActivityEnabled) {
                        mIsStkUserActivityEnabled = enabled;
                    }
                } else {
                    if (DEBUG_INPUT) {
                        Log.e(TAG, "Receive Fake Intent");
                    }
                }
                if (DEBUG_INPUT) {
                    Log.v(TAG, "mStkUserActivityEnReceiver -- onReceive -- exist "
                                + mIsStkUserActivityEnabled);
                }
            }
        }
    };

    /// M: [ALPS00062902][ALPS00389865]Avoid deadlock @{
    Runnable mNotifyStk = new Runnable() {
        public void run() {
            Intent intent = new Intent(STK_USERACTIVITY);
            mContext.sendBroadcast(intent);
        }
    };
    /// @}

    /// M: Save the screen off reason from the power manager service.
    int mScreenOffReason = -1; //useless

    /// M: KeyDispatch mode @{
    static final int KEY_DISPATCH_MODE_ALL_ENABLE = 0;
    static final int KEY_DISPATCH_MODE_ALL_DISABLE = 1;
    static final int KEY_DISPATCH_MODE_HOME_DISABLE = 2;
    /// @}
    /// M: mKeyDispatcMode : the default value is all enabled.
    int mKeyDispatcMode = KEY_DISPATCH_MODE_ALL_ENABLE;

    private Runnable mKeyRemappingVolumeDownLongPress_Test = new Runnable() {
        public void run() {
            //            mHandler.postDelayed( mKeyRemappingVolumeDownLongPress,0);
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
            InputManager inputManager
                    = (InputManager) mContext.getSystemService(Context.INPUT_SERVICE);
            Log.d(TAG, ">>>>>>>> InjectEvent Start");
            inputManager.injectInputEvent(keyEvent
                    , InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
            try {
                Log.d(TAG, "***** Sleeping.");
                Thread.sleep(10 * 1000);
                Log.d(TAG, "***** Waking up.");
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "IllegalArgumentException: ", e);
            } catch (SecurityException e) {
                Log.d(TAG, "SecurityException: ", e);
            } catch (InterruptedException e) {
                Log.d(TAG, "InterruptedException: ", e);
            }
            Log.d(TAG, "<<<<<<<< InjectEvent End");
        }
    };
    private long mKeyRemappingSendFakeKeyDownTime;
    private void keyRemappingSendFakeKeyEvent(int action, int keyCode) {
        long eventTime = SystemClock.uptimeMillis();
        if (action == KeyEvent.ACTION_DOWN) {
            mKeyRemappingSendFakeKeyDownTime = eventTime;
        }

        KeyEvent keyEvent
                = new KeyEvent(mKeyRemappingSendFakeKeyDownTime, eventTime, action, keyCode, 0);
        InputManager inputManager = (InputManager) mContext.getSystemService(Context.INPUT_SERVICE);
        inputManager.injectInputEvent(keyEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    private boolean mKeyRemappingVolumeUpLongPressed;

    private Runnable mKeyRemappingVolumeUpLongPress = new Runnable() {
        public void run() {
            showRecentApps(false);

            mKeyRemappingVolumeUpLongPressed = true;
        }
    };

    private boolean mKeyRemappingVolumeDownLongPressed;

    private Runnable mKeyRemappingVolumeDownLongPress = new Runnable() {
        public void run() {
            // Emulate clicking Menu key
            keyRemappingSendFakeKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU);
            keyRemappingSendFakeKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU);

            mKeyRemappingVolumeDownLongPressed = true;
        }
    };

    /// M: Screen unpinning @{
    private static final int DISMISS_SCREEN_PINNING_KEY_CODE = KeyEvent.KEYCODE_BACK;
    private void interceptDismissPinningChord() {
        IActivityManager activityManager =
            ActivityManagerNative.asInterface(ServiceManager.checkService("activity"));
        try {
            if (activityManager.isInLockTaskMode()) {
                activityManager.stopLockTaskMode();
            }
        } catch (RemoteException e) {
        }
    }
    /// @}

    /// M:[AppLaunchTime] Improve the mechanism of AppLaunchTime {@
    boolean mAppLaunchTimeEnabled
        = (1 == SystemProperties.getInt("ro.mtk_perf_response_time", 0)) ? true : false;
    /// @}

    /// M: [App Launch Reponse Time Enhancement][FSW] Policy implementation. {@
    /** {@inheritDoc} */
    @Override
    public View addFastStartingWindow(IBinder appToken, String packageName, int theme,
            CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes,
            int icon, int logo, int windowFlags, Bitmap bitmap) {
        if (!SHOW_STARTING_ANIMATIONS) {
            return null;
        }
        if (packageName == null) {
            return null;
        }

        WindowManager wm = null;

        if (true) {
            View view = new View(mContext);

            try {
                Context context = mContext;
                if (DEBUG_STARTING_WINDOW) {
                    Slog.d(TAG, "addFastStartingWindow " + packageName
                        + ": nonLocalizedLabel=" + nonLocalizedLabel + " theme="
                        + Integer.toHexString(theme));
                }

                final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);

                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_STARTING;
                params.flags = windowFlags |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                    ///| WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN

                TypedArray windowStyle = mContext.obtainStyledAttributes(
                    com.android.internal.R.styleable.Window);
                params.windowAnimations = windowStyle.getResourceId(
                    com.android.internal.R.styleable.Window_windowAnimationStyle, 0);
                // solve memory leak problem that caused by MTK. prize-linkh-20171205 @{
                windowStyle.recycle();
                // @}
                params.token = appToken;
                params.packageName = packageName;
                params.privateFlags |=
                        WindowManager.LayoutParams.PRIVATE_FLAG_FAKE_HARDWARE_ACCELERATED;
                params.privateFlags |= WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;

                if (!compatInfo.supportsScreen()) {
                    params.privateFlags
                            |= WindowManager.LayoutParams.PRIVATE_FLAG_COMPATIBLE_WINDOW;
                }
                params.setTitle("FastStarting");
                wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

                // Nav bar color customized feature. prize-linkh-20170922 @{
                if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
                    Context appContext = context;
                    if (theme != appContext.getThemeResId()) {
                        try {
                            if (DBG_NAV_BAR_COLOR_CUST) {
                                Slog.d(TAG, "addFastStartingWindow() Create package context for " + packageName);
                            }
                            appContext = appContext.createPackageContext(packageName, 0);
                            appContext.setTheme(theme);                            
                        } catch (PackageManager.NameNotFoundException e) {
                            Slog.e(TAG, "addFastStartingWindow() Fail to create package context for " + packageName);
                        }
                    }

                    TypedArray ta = appContext.obtainStyledAttributes(
                                new int[] {android.R.attr.disableNavbarColorCustInStartingWin});

                    boolean disableNavbarColorCustInStartingWin = false;
                    if (!ta.hasValue(0)) {
                        // If this flag isn't defined in theme style, then we retrieve
                        // from Nav Bar Hint Data.
                        final PrizeNavBarColorHintUtil hintUtil = PrizeNavBarColorHintUtil.getInstance(mContext);
                        final int hint = hintUtil.getNavBarColorHint(packageName);                        
                        if (DBG_NAV_BAR_COLOR_CUST) {
                            Slog.d(TAG, "addFastStartingWindow() navBarColorHint=" 
                                + hintUtil.getNavBarColorHintDescription(hint));
                        }
                        if ((hint & PrizeNavBarColorHintUtil.NB_COLOR_HINT_DISABLE_FOR_STARTING_WIN) != 0) {
                            disableNavbarColorCustInStartingWin = true;
                        }
                    } else {
                        disableNavbarColorCustInStartingWin= ta.getBoolean(0, false);
                    }
                    
                    ta.recycle();
                    
                    if (DBG_NAV_BAR_COLOR_CUST) {
                        Slog.d(TAG, "addFastStartingWindow() disableNavbarColorCustInStartingWin=" 
                            + disableNavbarColorCustInStartingWin);
                    }

                    if (disableNavbarColorCustInStartingWin) {
                        params.alwaysDisableNavbarColorCust = true;
                    } else {
                        params.hasSetNavBarColor = true;
                        params.navBarColor = getSystemCustNavBarColor();
                        params.enableSystemUINavBarColorBackground = true;
                    }

                } // @}

                if (DEBUG_STARTING_WINDOW) {
                    Slog.d(
                        TAG, "Adding starting window for " + packageName
                        + " / " + appToken + ": "
                        + (view.getParent() != null ? view : null));
                }

                //view.setBackground(new BitmapDrawable(mContext.getResources(), bitmap));
                wm.addView(view, params);

                if (mAppLaunchTimeEnabled) {
                    /// M: [App Launch Reponse Time Enhancement] Merge Traversal.
                    WindowManagerGlobal.getInstance().doTraversal(view, true);
                }

                // Only return the view if it was successfully added to the
                // window manager... which we can tell by it having a parent.
                return view.getParent() != null ? view : null;
            } catch (WindowManager.BadTokenException e) {
                // ignore
                Log.w(TAG, appToken + " already running, starting window not displayed. " +
                        e.getMessage());
            } catch (RuntimeException e) {
                // don't crash if something else bad happens, for example a
                // failure loading resources because we are loading from an app
                // on external storage that has been unmounted.
                Log.w(TAG, appToken + " failed creating starting window", e);
            } finally {
                if (view != null && view.getParent() == null) {
                    Log.w(TAG, "view not successfully added to wm, removing view");
                    wm.removeViewImmediate(view);
                }
            }
        }
        return null;
    }
    /// @}

    /// M: Support feature that intercept key before WMS handle @{
    boolean isUspEnable = !"no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"));
    private boolean interceptKeyBeforeHandling(KeyEvent event) {
        /// M: Support USP feature: disable KEYCODE_POWER when USP is freezed
        if (isUspEnable && KeyEvent.KEYCODE_POWER == event.getKeyCode() &&
                (SystemProperties.getInt("persist.mtk_usp_cfg_ctrl", 0) & 0x4) == 4) {
            return true;
        }
        return false;
    }
    /// @}

    /// M: add for fullscreen switch feature @{
    static final Rect mTmpSwitchFrame = new Rect();
    private static final int SWITCH_TARGET_WIDTH = 9;
    private static final int SWITCH_TARGET_HEIGHT = 16;
    private boolean mSupportFullscreenSwitch = false;

    /**
     * @param left , left shit value
     * @param top , top shit value
     * @param right , right shit value
     * @param bottom , bottom shit value
     */
    private void updateRect(int left, int top, int right, int bottom) {
        mStableLeft += left;
        mStableTop += top;
        mStableRight -= right;
        mStableBottom -= bottom;

        mDockLeft += left;
        mDockTop += top;
        mDockRight -= right;
        mDockBottom -= bottom;

        mSystemLeft = mDockLeft;
        mSystemTop = mDockTop;
        mSystemRight = mDockRight;
        mSystemBottom = mDockBottom;

        mStableFullscreenLeft += left;
        mStableFullscreenTop += top;
        mStableFullscreenRight -= right;
        mStableFullscreenBottom -= bottom;

        mContentLeft += left;
        mContentTop += top;
        mContentRight -= right;
        mContentBottom -= bottom;

        mCurLeft += left;
        mCurTop += top;
        mCurRight -= right;
        mCurBottom -= bottom;

        mOverscanScreenLeft += left;
        mOverscanScreenTop += top;
        mOverscanScreenWidth -= (left + right);
        mOverscanScreenHeight -= (top + bottom);

        mUnrestrictedScreenLeft += left;
        mUnrestrictedScreenTop += top;
        mUnrestrictedScreenWidth -= (left + right);
        mUnrestrictedScreenHeight -= (top + bottom);

        mRestrictedScreenLeft += left;
        mRestrictedScreenTop += top;
        mRestrictedScreenWidth -= (left + right);
        mRestrictedScreenHeight -= (top + bottom);

        mRestrictedOverscanScreenLeft += left;
        mRestrictedOverscanScreenTop += top;
        mRestrictedOverscanScreenWidth -= (left + right);
        mRestrictedOverscanScreenHeight -= (top + bottom);
    }

    private void applyFullScreenSwitch(WindowState win) {
        if (DEBUG_LAYOUT) {
            Slog.i(TAG, "applyFullScreenSwitch win.isFullscreenOn() = "
                    + win.isFullscreenOn());
        }

        if (!win.isFullscreenOn() && !win.isInMultiWindowMode()) {
            getSwitchFrame(win);
            if (mTmpSwitchFrame.left != 0 || mTmpSwitchFrame.right != 0
                    || mTmpSwitchFrame.top != 0 || mTmpSwitchFrame.bottom != 0) {
                updateRect(mTmpSwitchFrame.left, mTmpSwitchFrame.top,
                        mTmpSwitchFrame.right, mTmpSwitchFrame.bottom);
            }
        }
    }

    /**
     * Compute screen shift value if not at fullscreen mode.
     */
    private void getSwitchFrame(WindowState win) {
        mTmpSwitchFrame.setEmpty();
        int diff = 0;

        if (mOverscanScreenWidth > mOverscanScreenHeight) {
            diff = (mOverscanScreenWidth - (mOverscanScreenHeight / SWITCH_TARGET_WIDTH)
                    * SWITCH_TARGET_HEIGHT) / 2;
            if (diff > 0) {
                mTmpSwitchFrame.left = diff;
                mTmpSwitchFrame.top = 0;
                mTmpSwitchFrame.right = diff;
                mTmpSwitchFrame.bottom = 0;
            }
        } else {
            diff = (mOverscanScreenHeight - (mOverscanScreenWidth / SWITCH_TARGET_WIDTH)
                    * SWITCH_TARGET_HEIGHT) / 2;
            if (diff > 0) {
                mTmpSwitchFrame.left = 0;
                mTmpSwitchFrame.top = diff;
                mTmpSwitchFrame.right = 0;
                mTmpSwitchFrame.bottom = diff;
            }
        }

        Slog.i(TAG, "applyFullScreenSwitch mOverscanScreenWidth = "
                + mOverscanScreenWidth + " mOverscanScreenHeight ="
                + mOverscanScreenHeight + " diff =" + diff
                + " mTmpSwitchFrame =" + mTmpSwitchFrame);
    }

    private void resetFullScreenSwitch(WindowState win, Rect of) {
        if (!mTmpSwitchFrame.isEmpty()) {
            Slog.i(TAG, "resetFullScreenSwitch mTmpSwitchFrame = "
                    + mTmpSwitchFrame);
            updateRect(-mTmpSwitchFrame.left, -mTmpSwitchFrame.top,
                    -mTmpSwitchFrame.right, -mTmpSwitchFrame.bottom);

            mTmpSwitchFrame.setEmpty();
        }
    }
    /// @}
}
