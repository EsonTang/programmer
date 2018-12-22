/*
 * Copyright (C) 2010 The Android Open Source Project
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


import static android.app.StatusBarManager.NAVIGATION_HINT_BACK_ALT;
import static android.app.StatusBarManager.NAVIGATION_HINT_IME_SHOWN;
import static android.app.StatusBarManager.WINDOW_STATE_HIDDEN;
import static android.app.StatusBarManager.WINDOW_STATE_SHOWING;
import static android.app.StatusBarManager.windowStateToString;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_LIGHTS_OUT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_LIGHTS_OUT_TRANSPARENT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_OPAQUE;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_SEMI_TRANSPARENT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_TRANSLUCENT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_TRANSPARENT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_WARNING;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.NonNull;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.IActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.Trace;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import android.telephony.SubscriptionManager;
import android.telecom.TelecomManager;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.view.Display;
import android.view.IRotationWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.AutoReinflateContainer.InflateListener;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.DemoMode;
import com.android.systemui.EventLogConstants;
import com.android.systemui.EventLogTags;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.classifier.FalsingLog;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.qs.QSContainer;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.recents.ScreenPinningRequest;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.AppTransitionFinishedEvent;
import com.android.systemui.recents.events.activity.UndockingTaskEvent;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.WindowManagerProxy;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.BackDropView;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.DismissView;
import com.android.systemui.statusbar.DragDownHelper;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.KeyboardShortcuts;
/*PRIZE-Add for background_reflecting-luyingying-2017-09-09-Start*/
import com.android.systemui.statusbar.KeyguardAffordanceView;
/*PRIZE-Add for background_reflecting-luyingying-2017-09-09-end*/
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.NotificationOverflowContainer;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.StatusBarState;
import com.android.systemui.statusbar.phone.UnlockMethodCache.OnUnlockMethodChangedListener;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;
import com.android.systemui.statusbar.policy.BatteryControllerImpl;
import com.android.systemui.statusbar.policy.BluetoothControllerImpl;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.statusbar.policy.CastControllerImpl;
import com.android.systemui.statusbar.policy.EncryptionHelper;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.HotspotControllerImpl;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.PreviewInflater;
import com.android.systemui.statusbar.policy.RotationLockControllerImpl;
import com.android.systemui.statusbar.policy.SecurityControllerImpl;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout
        .OnChildLocationsChangedListener;
import com.android.systemui.statusbar.stack.StackStateAnimator;
import com.android.systemui.statusbar.stack.StackViewState;
import com.android.systemui.volume.VolumeComponent;
/// M: BMW
import com.mediatek.multiwindow.MultiWindowManager;

//prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar start
import android.hardware.input.InputManager;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.Gravity;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation;
import android.view.Surface;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.view.HapticFeedbackConstants;

//prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar end

import com.mediatek.systemui.ext.IStatusBarPlmnPlugin;
import com.mediatek.systemui.PluginManager;
/// M: Modify statusbar style for GMO
import com.mediatek.systemui.statusbar.util.FeatureOptions;
/// M: Add extra tiles
import com.mediatek.systemui.statusbar.policy.HotKnotControllerImpl;
import com.mediatek.systemui.statusbar.util.SIMHelper;
// /@}

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.StatusBarManager.NAVIGATION_HINT_BACK_ALT;
import static android.app.StatusBarManager.NAVIGATION_HINT_IME_SHOWN;
import static android.app.StatusBarManager.WINDOW_STATE_HIDDEN;
import static android.app.StatusBarManager.WINDOW_STATE_SHOWING;
import static android.app.StatusBarManager.windowStateToString;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_LIGHTS_OUT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_LIGHTS_OUT_TRANSPARENT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_OPAQUE;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_SEMI_TRANSPARENT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_TRANSLUCENT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_TRANSPARENT;
import static com.android.systemui.statusbar.phone.BarTransitions.MODE_WARNING;

/// M: BMW
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.Recents;
/*PRIZE-import package- liufan-2015-04-10-start*/
import android.text.TextUtils;
import android.os.SystemProperties;
import com.android.systemui.FontSizeUtils;
import android.view.ViewGroup;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.RecentsActivityLaunchState;
import android.widget.ImageView.ScaleType;
import android.widget.FrameLayout;
import com.android.systemui.statusbar.phone.FeatureOption;
import android.widget.Toast;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.OnTileClickListener;
import android.content.ComponentName;
import android.os.PowerManager;
import android.view.WindowManager;
import android.view.SurfaceControl;
import android.renderscript.RenderScript;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.Element;
import android.renderscript.Allocation;
import android.graphics.Canvas;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import com.android.systemui.settings.ToggleSlider;
import com.android.systemui.settings.BrightnessController;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.IPowerManager;
import android.os.ServiceManager;
import com.android.systemui.statusbar.ExpandableView ;
import android.widget.RelativeLayout;
import android.graphics.Shader.TileMode;
import android.os.AsyncTask;
import android.app.WallpaperManager;
import android.animation.ValueAnimator;
import com.mediatek.common.prizeoption.PrizeOption;
import android.os.BatteryManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import java.lang.StackTraceElement;
import android.util.Slog;
import com.android.keyguard.KeyguardAbsKeyInputView;
import android.widget.Button;
import android.content.ContentValues;
import android.graphics.Paint;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.views.TaskView;
import com.android.systemui.recents.views.TaskStackView;
import android.text.format.Formatter;
import com.android.systemui.recents.LoadIconUtils;
import android.telecom.TelecomManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;



import com.android.systemui.qs.QSDetail;
import java.io.Writer; 
/*PRIZE-import package- liufan-2015-04-10-end*/
//PRIZE-import package liyao-2015-07-09
import com.android.systemui.power.PowerNotificationWarnings;
/*PRIZE-import package- liyao-2015-07-28-start*/
import android.database.Cursor;
import android.graphics.BitmapFactory;
import java.lang.ref.WeakReference;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
/*PRIZE-import package- liyao-2015-07-28-end*/

//add for statusbar inverse. prize-linkh-20150903
import com.android.systemui.BatteryMeterViewDefinedNew;
/* Dynamically changing Recents function feature. prize-linkh-20161115 */
import com.android.systemui.statusbar.policy.KeyButtonView;
//END...
/*PRIZE-Add for BluLight-zhudaopeng-2017-05-12-Start*/
import com.mediatek.pq.PictureQuality;
/*PRIZE-Add for BluLight-zhudaopeng-2017-05-12-End*/

/*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-Start*/
import com.android.keyguard.KeyguardStatusView;
import com.android.keyguard.EmergencyButton;
import android.content.res.ColorStateList;
/*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-end*/
/* prize-add-split screen-liyongli-20170612-start */
import com.android.systemui.PrizeFloatQQService;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.component.PrizeOpenPendingIntentEvent;
/* prize-add-split screen-liyongli-20170612-end */
import com.mediatek.common.prizeoption.PrizeOption;
/*prize add by xiarui 2018-01-25 start*/
import android.app.AlarmManager;
import android.os.FileObserver;
/*prize add by xiarui 2018-01-25 end*/
/*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
import com.android.systemui.statusbar.LiuHaiSignalClusterView;
import com.android.systemui.statusbar.LiuHaiStatusBarIconController;
import com.android.systemui.statusbar.LiuHaiStatusBarIconController.ShowOnRightListener;
import com.android.systemui.LiuHaiBatteryMeterViewDefinedNew;
/*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
/*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
import com.android.systemui.statusbar.LiuHaiSignalView2;
/*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/


public class PhoneStatusBar extends BaseStatusBar implements DemoMode,
        DragDownHelper.DragDownCallback, ActivityStarter, OnUnlockMethodChangedListener,
        HeadsUpManager.OnHeadsUpChangedListener {
    static final String TAG = "PhoneStatusBar";
    final String PRIZE_TAG = "prize_PhoneStatusBar";
    public static final String TICKER_TAG = "PrizeTickerController"; //prize add by xiarui 2018-04-24
	/* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 start */
    public static final String DISABLE_NAVIGATION = "prize_disable_navigation";
    /* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 end */
    /// M: Enable the PhoneStatusBar log.
    public static final boolean DEBUG = true;/**BaseStatusBar.DEBUG;*/
    public static final boolean SPEW = false;
    public static final boolean DUMPTRUCK = true; // extra dumpsys info
    public static final boolean DEBUG_GESTURES = false;
    public static final boolean DEBUG_MEDIA = false;
    public static final boolean DEBUG_MEDIA_FAKE_ARTWORK = false;

    public static final boolean DEBUG_WINDOW_STATE = false;

    // additional instrumentation for testing purposes; intended to be left on during development
    public static final boolean CHATTY = DEBUG;
	/*PRIZE-PowerExtendMode-to solve can't unlock problem-wangxianzhen-2016-01-13-start bugid 10971*/
    public static final String ACTION_ENTER_SUPERPOWER = "android.intent.action.ACTION_CLOSE_SUPERPOWER_NOTIFICATION";
    /*PRIZE-PowerExtendMode-to solve can't unlock problem-wangxianzhen-2016-01-13-end bugid 10971*/
    public static final boolean SHOW_LOCKSCREEN_MEDIA_ARTWORK = true;

    public static final String ACTION_FAKE_ARTWORK = "fake_artwork";

    private static final int MSG_OPEN_NOTIFICATION_PANEL = 1000;
    private static final int MSG_CLOSE_PANELS = 1001;
    private static final int MSG_OPEN_SETTINGS_PANEL = 1002;
    private static final int MSG_LAUNCH_TRANSITION_TIMEOUT = 1003;
    // 1020-1040 reserved for BaseStatusBar

    // Time after we abort the launch transition.
    private static final long LAUNCH_TRANSITION_TIMEOUT_MS = 5000;

    private static final boolean CLOSE_PANEL_WHEN_EMPTIED = true;

    private static final int STATUS_OR_NAV_TRANSIENT =
            View.STATUS_BAR_TRANSIENT | View.NAVIGATION_BAR_TRANSIENT;
    private static final long AUTOHIDE_TIMEOUT_MS = 3000;

    /** The minimum delay in ms between reports of notification visibility. */
    private static final int VISIBILITY_REPORT_MIN_DELAY_MS = 500;

    /**
     * The delay to reset the hint text when the hint animation is finished running.
     */
    private static final int HINT_RESET_DELAY_MS = 1200;

    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .build();

    public static final int FADE_KEYGUARD_START_DELAY = 100;
    public static final int FADE_KEYGUARD_DURATION = 300;
    public static final int FADE_KEYGUARD_DURATION_PULSING = 96;

    /** Allow some time inbetween the long press for back and recents. */
    private static final int LOCK_TO_APP_GESTURE_TOLERENCE = 200;

    /** If true, the system is in the half-boot-to-decryption-screen state.
     * Prudently disable QS and notifications.  */
    private static final boolean ONLY_CORE_APPS;

    /** If true, the lockscreen will show a distinct wallpaper */
    private static final boolean ENABLE_LOCKSCREEN_WALLPAPER = true;

    /* If true, the device supports freeform window management.
     * This affects the status bar UI. */
    private static final boolean FREEFORM_WINDOW_MANAGEMENT;

    /**
     * How long to wait before auto-dismissing a notification that was kept for remote input, and
     * has now sent a remote input. We auto-dismiss, because the app may not see a reason to cancel
     * these given that they technically don't exist anymore. We wait a bit in case the app issues
     * an update.
     */
    private static final int REMOTE_INPUT_KEPT_ENTRY_AUTO_CANCEL_DELAY = 200;

    /**
     * Never let the alpha become zero for surfaces that draw with SRC - otherwise the RenderNode
     * won't draw anything and uninitialized memory will show through
     * if mScrimSrcModeEnabled. Note that 0.001 is rounded down to 0 in
     * libhwui.
     */
    private static final float SRC_MIN_ALPHA = 0.002f;

    static {
        boolean onlyCoreApps;
        boolean freeformWindowManagement;
        try {
            IPackageManager packageManager =
                    IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            onlyCoreApps = packageManager.isOnlyCoreApps();
            freeformWindowManagement = packageManager.hasSystemFeature(
                    PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT, 0);
        } catch (RemoteException e) {
            onlyCoreApps = false;
            freeformWindowManagement = false;
        }
        ONLY_CORE_APPS = onlyCoreApps;
        FREEFORM_WINDOW_MANAGEMENT = freeformWindowManagement;
    }

    PhoneStatusBarPolicy mIconPolicy;

    // These are no longer handled by the policy, because we need custom strategies for them
    BluetoothControllerImpl mBluetoothController;
    SecurityControllerImpl mSecurityController;
    protected BatteryController mBatteryController;
    LocationControllerImpl mLocationController;
    NetworkControllerImpl mNetworkController;
    HotspotControllerImpl mHotspotController;
    RotationLockControllerImpl mRotationLockController;
    UserInfoController mUserInfoController;
    protected ZenModeController mZenModeController;
    CastControllerImpl mCastController;
    VolumeComponent mVolumeComponent;
    KeyguardUserSwitcher mKeyguardUserSwitcher;
    FlashlightController mFlashlightController;
    protected UserSwitcherController mUserSwitcherController;
    NextAlarmController mNextAlarmController;
    protected KeyguardMonitor mKeyguardMonitor;
    BrightnessMirrorController mBrightnessMirrorController;
    AccessibilityController mAccessibilityController;
    /// M: Add extra tiles @{
    //add HotKnot in quicksetting
    HotKnotControllerImpl mHotKnotController;
    // /@}
    FingerprintUnlockController mFingerprintUnlockController;
    LightStatusBarController mLightStatusBarController;
    protected LockscreenWallpaper mLockscreenWallpaper;
    //prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar start
	GestureIndicatorManager mGestureIndicatorManager;
	//prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar end
    int mNaturalBarHeight = -1;

    Display mDisplay;
    Point mCurrentDisplaySize = new Point();

    protected StatusBarWindowView mStatusBarWindow;
    protected PhoneStatusBarView mStatusBarView;
    private int mStatusBarWindowState = WINDOW_STATE_SHOWING;
    protected StatusBarWindowManager mStatusBarWindowManager;
    private UnlockMethodCache mUnlockMethodCache;
    private DozeServiceHost mDozeServiceHost;
    private boolean mWakeUpComingFromTouch;
    private PointF mWakeUpTouchLocation;
    private boolean mScreenTurningOn;

    //prize add by xiarui 2018-04-11 @{
    PrizeTickerController mTickerController;
    //@}

    int mPixelFormat;
    Object mQueueLock = new Object();

    protected StatusBarIconController mIconController;

    // expanded notifications
    protected NotificationPanelView mNotificationPanel; // the sliding/resizing panel within the notification window
    View mExpandedContents;
    TextView mNotificationPanelDebugText;

    // settings
    private QSPanel mQSPanel;

    // top bar
    BaseStatusBarHeader mHeader;
    protected KeyguardStatusBarView mKeyguardStatusBar;
    /*PRIZE-Modify for backgroud_reflect-luyingying-2017-09-09-Start*/
    KeyguardStatusView mKeyguardStatusView;
    private EmergencyButton mEmergencyButton;
    //Modify end
    KeyguardBottomAreaView mKeyguardBottomArea;
    boolean mLeaveOpenOnKeyguardHide;
    KeyguardIndicationController mKeyguardIndicationController;

    // Keyguard is going away soon.
    private boolean mKeyguardGoingAway;
    // Keyguard is actually fading away now.
    private boolean mKeyguardFadingAway;
    private long mKeyguardFadingAwayDelay;
    private long mKeyguardFadingAwayDuration;

    // RemoteInputView to be activated after unlock
    private View mPendingRemoteInputView;
    private View mPendingWorkRemoteInputView;

    private View mReportRejectedTouch;

    int mMaxAllowedKeyguardNotifications;

    boolean mExpandedVisible;

    private int mNavigationBarWindowState = WINDOW_STATE_SHOWING;

    // the tracker view
    int mTrackingPosition; // the position of the top of the tracking view.

    // Tracking finger for opening/closing.
    boolean mTracking;

    int[] mAbsPos = new int[2];
    ArrayList<Runnable> mPostCollapseRunnables = new ArrayList<>();

    // for disabling the status bar
    int mDisabled1 = 0;
    int mDisabled2 = 0;

    // tracking calls to View.setSystemUiVisibility()
    int mSystemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;
    private final Rect mLastFullscreenStackBounds = new Rect();
    private final Rect mLastDockedStackBounds = new Rect();

    // last value sent to window manager
    private int mLastDispatchedSystemUiVisibility = ~View.SYSTEM_UI_FLAG_VISIBLE;

    DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    // XXX: gesture research
    private final GestureRecorder mGestureRec = DEBUG_GESTURES
        ? new GestureRecorder("/sdcard/statusbar_gestures.dat")
        : null;

    private ScreenPinningRequest mScreenPinningRequest;

    private int mNavigationIconHints = 0;
    private HandlerThread mHandlerThread;

    // ensure quick settings is disabled until the current user makes it through the setup wizard
    private boolean mUserSetup = false;
    private ContentObserver mUserSetupObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            final boolean userSetup = 0 != Settings.Secure.getIntForUser(
                    mContext.getContentResolver(),
                    Settings.Secure.USER_SETUP_COMPLETE,
                    0 /*default */,
                    mCurrentUserId);
            if (MULTIUSER_DEBUG) Log.d(TAG, String.format("User setup changed: " +
                    "selfChange=%s userSetup=%s mUserSetup=%s",
                    selfChange, userSetup, mUserSetup));

            if (userSetup != mUserSetup) {
                mUserSetup = userSetup;
                if (!mUserSetup && mStatusBarView != null)
                    animateCollapseQuickSettings();
                if (mKeyguardBottomArea != null) {
                    mKeyguardBottomArea.setUserSetupComplete(mUserSetup);
                }
                if (mNetworkController != null) {
                    mNetworkController.setUserSetupComplete(mUserSetup);
                }
            }
            if (mIconPolicy != null) {
                mIconPolicy.setCurrentUserSetup(mUserSetup);
            }
        }
    };

    final private ContentObserver mHeadsUpObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            boolean wasUsing = mUseHeadsUp;
            mUseHeadsUp = ENABLE_HEADS_UP && !mDisableNotificationAlerts
                    && Settings.Global.HEADS_UP_OFF != Settings.Global.getInt(
                    mContext.getContentResolver(), Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED,
                    Settings.Global.HEADS_UP_OFF);
            mHeadsUpTicker = mUseHeadsUp && 0 != Settings.Global.getInt(
                    mContext.getContentResolver(), SETTING_HEADS_UP_TICKER, 0);
            Log.d(TAG, "heads up is " + (mUseHeadsUp ? "enabled" : "disabled"));
            if (wasUsing != mUseHeadsUp) {
                if (!mUseHeadsUp) {
                    Log.d(TAG, "dismissing any existing heads up notification on disable event");
                    mHeadsUpManager.releaseAllImmediately();
                }
            }
        }
    };

    private int mInteractingWindows;
    private boolean mAutohideSuspended;
    private int mStatusBarMode;
    private int mNavigationBarMode;
    private int mMaxKeyguardNotifications;

    private ViewMediatorCallback mKeyguardViewMediatorCallback;
    protected ScrimController mScrimController;
    protected DozeScrimController mDozeScrimController;

    private final Runnable mAutohide = new Runnable() {
        @Override
        public void run() {
            int requested = mSystemUiVisibility & ~STATUS_OR_NAV_TRANSIENT;
            if (mSystemUiVisibility != requested) {
                notifyUiVisibilityChanged(requested);
            }
        }
     };
     private final Runnable mUpdateFloatByShowNav = new Runnable() {
         @Override
         public void run() {
             int requested = mSystemUiVisibility & ~STATUS_OR_NAV_TRANSIENT;
             if (mSystemUiVisibility != requested) {
             	Log.d("snail_nav", "--------Runnable------mUpdateFloatByShowNav--------------");
             	if(mContext != null){
             		Intent floatwin =  new Intent("com.prize.showNavForFloatwin");
     				floatwin.setPackage("com.android.prizefloatwindow");
     				mContext.sendBroadcast(floatwin);
     			}
             }
         }
      };
      private final Runnable mUpdateFloatByHideNav = new Runnable() {
          @Override
          public void run() {
              Log.d("snail_nav", "--------Runnable------mUpdateFloatByHideNav--------------");
              if(mContext != null){
              	  Intent floatwin =  new Intent("com.prize.hideNavForFloatwin");
      			  floatwin.setPackage("com.android.prizefloatwindow");
      			  mContext.sendBroadcast(floatwin);
      		  }
          }
          
       };

    private boolean mWaitingForKeyguardExit;
    private boolean mDozing;
    private boolean mDozingRequested;
    protected boolean mScrimSrcModeEnabled;

    public static final Interpolator ALPHA_IN = Interpolators.ALPHA_IN;
    public static final Interpolator ALPHA_OUT = Interpolators.ALPHA_OUT;

    private BackDropView mBackdrop;
    private ImageView mBackdropFront, mBackdropBack;
    private LinearLayout mBlurBack;
    private PorterDuffXfermode mSrcXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
    private PorterDuffXfermode mSrcOverXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);

    private MediaSessionManager mMediaSessionManager;
    private MediaController mMediaController;
    private String mMediaNotificationKey;
    private MediaMetadata mMediaMetadata;
    private MediaController.Callback mMediaListener
            = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            super.onPlaybackStateChanged(state);
            if (DEBUG_MEDIA) Log.v(TAG, "DEBUG_MEDIA: onPlaybackStateChanged: " + state);
            if (state != null) {
                if (!isPlaybackActive(state.getState())) {
                    clearCurrentMediaNotification();
                    updateMediaMetaData(true, true);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            if (DEBUG_MEDIA) Log.v(TAG, "DEBUG_MEDIA: onMetadataChanged: " + metadata);
            mMediaMetadata = metadata;
            updateMediaMetaData(true, true);
        }
    };

    private final OnChildLocationsChangedListener mOnChildLocationsChangedListener =
            new OnChildLocationsChangedListener() {
        @Override
        public void onChildLocationsChanged(NotificationStackScrollLayout stackScrollLayout) {
            userActivity();
        }
    };

    private int mDisabledUnmodified1;
    private int mDisabledUnmodified2;

    /** Keys of notifications currently visible to the user. */
    private final ArraySet<NotificationVisibility> mCurrentlyVisibleNotifications =
            new ArraySet<>();
    private long mLastVisibilityReportUptimeMs;

    private final ShadeUpdates mShadeUpdates = new ShadeUpdates();

    private Runnable mLaunchTransitionEndRunnable;
    private boolean mLaunchTransitionFadingAway;
    private ExpandableNotificationRow mDraggedDownRow;
    private boolean mLaunchCameraOnScreenTurningOn;
    private boolean mLaunchCameraOnFinishedGoingToSleep;
    private int mLastCameraLaunchSource;
    private PowerManager.WakeLock mGestureWakeLock;
    private Vibrator mVibrator;

    // Fingerprint (as computed by getLoggingFingerprint() of the last logged state.
    private int mLastLoggedStateFingerprint;

    /**
     * If set, the device has started going to sleep but isn't fully non-interactive yet.
     */
    protected boolean mStartedGoingToSleep;

    private static final int VISIBLE_LOCATIONS = StackViewState.LOCATION_FIRST_HUN
            | StackViewState.LOCATION_MAIN_AREA;

    private final OnChildLocationsChangedListener mNotificationLocationsChangedListener =
            new OnChildLocationsChangedListener() {
                @Override
                public void onChildLocationsChanged(
                        NotificationStackScrollLayout stackScrollLayout) {
                    if (mHandler.hasCallbacks(mVisibilityReporter)) {
                        // Visibilities will be reported when the existing
                        // callback is executed.
                        return;
                    }
                    // Calculate when we're allowed to run the visibility
                    // reporter. Note that this timestamp might already have
                    // passed. That's OK, the callback will just be executed
                    // ASAP.
                    long nextReportUptimeMs =
                            mLastVisibilityReportUptimeMs + VISIBILITY_REPORT_MIN_DELAY_MS;
                    mHandler.postAtTime(mVisibilityReporter, nextReportUptimeMs);
                }
            };

    // Tracks notifications currently visible in mNotificationStackScroller and
    // emits visibility events via NoMan on changes.
    private final Runnable mVisibilityReporter = new Runnable() {
        private final ArraySet<NotificationVisibility> mTmpNewlyVisibleNotifications =
                new ArraySet<>();
        private final ArraySet<NotificationVisibility> mTmpCurrentlyVisibleNotifications =
                new ArraySet<>();
        private final ArraySet<NotificationVisibility> mTmpNoLongerVisibleNotifications =
                new ArraySet<>();

        @Override
        public void run() {
            mLastVisibilityReportUptimeMs = SystemClock.uptimeMillis();
            final String mediaKey = getCurrentMediaNotificationKey();

            // 1. Loop over mNotificationData entries:
            //   A. Keep list of visible notifications.
            //   B. Keep list of previously hidden, now visible notifications.
            // 2. Compute no-longer visible notifications by removing currently
            //    visible notifications from the set of previously visible
            //    notifications.
            // 3. Report newly visible and no-longer visible notifications.
            // 4. Keep currently visible notifications for next report.
            ArrayList<Entry> activeNotifications = mNotificationData.getActiveNotifications();
            int N = activeNotifications.size();
            for (int i = 0; i < N; i++) {
                Entry entry = activeNotifications.get(i);
                String key = entry.notification.getKey();
                boolean isVisible =
                        (mStackScroller.getChildLocation(entry.row) & VISIBLE_LOCATIONS) != 0;
                NotificationVisibility visObj = NotificationVisibility.obtain(key, i, isVisible);
                boolean previouslyVisible = mCurrentlyVisibleNotifications.contains(visObj);
                if (isVisible) {
                    // Build new set of visible notifications.
                    mTmpCurrentlyVisibleNotifications.add(visObj);
                    if (!previouslyVisible) {
                        mTmpNewlyVisibleNotifications.add(visObj);
                    }
                } else {
                    // release object
                    visObj.recycle();
                }
            }
            mTmpNoLongerVisibleNotifications.addAll(mCurrentlyVisibleNotifications);
            mTmpNoLongerVisibleNotifications.removeAll(mTmpCurrentlyVisibleNotifications);

            logNotificationVisibilityChanges(
                    mTmpNewlyVisibleNotifications, mTmpNoLongerVisibleNotifications);

            recycleAllVisibilityObjects(mCurrentlyVisibleNotifications);
            mCurrentlyVisibleNotifications.addAll(mTmpCurrentlyVisibleNotifications);

            recycleAllVisibilityObjects(mTmpNoLongerVisibleNotifications);
            mTmpCurrentlyVisibleNotifications.clear();
            mTmpNewlyVisibleNotifications.clear();
            mTmpNoLongerVisibleNotifications.clear();
        }
    };

    private void recycleAllVisibilityObjects(ArraySet<NotificationVisibility> array) {
        final int N = array.size();
        for (int i = 0 ; i < N; i++) {
            array.valueAt(i).recycle();
        }
        array.clear();
    }

    private final View.OnClickListener mOverflowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            goToLockedShade(null);
        }
    };
    private HashMap<ExpandableNotificationRow, List<ExpandableNotificationRow>> mTmpChildOrderMap
            = new HashMap<>();
    private RankingMap mLatestRankingMap;
    private boolean mNoAnimationOnNextBarModeChange;
    private FalsingManager mFalsingManager;

    private KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() {
        @Override
        public void onDreamingStateChanged(boolean dreaming) {
            if (dreaming) {
                Log.d(TAG, "onDreamingStateChanged maybeEscalateHeadsUp dreaming = " + dreaming);
                maybeEscalateHeadsUp();
            }
        }
    };

    @Override
    public void start() {
        mDisplay = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        updateDisplaySize();
        mScrimSrcModeEnabled = mContext.getResources().getBoolean(
                R.bool.config_status_bar_scrim_behind_use_src);

        /*PRIZE-add for liuhai screen-liufan-2018-05-17-start*/
        if(OPEN_LIUHAI_SCREEN2){
            LiuHaiStatusBarIconController2 controller = new LiuHaiStatusBarIconController2(mContext, this);//for statusbar icon
            //controller.setDebugSwitch(true, 0);
            mLiuHaiIconControllerList2.add(controller);
            controller = new LiuHaiStatusBarIconController2(mContext, this);//for keyguard icon
            //controller.setDebugSwitch(true, 1);
            mLiuHaiIconControllerList2.add(controller);
            controller = new LiuHaiStatusBarIconController2(mContext, this);//for pulled statusbar icon
            //controller.setDebugSwitch(true, 2);
            mLiuHaiIconControllerList2.add(controller);

            registerBroadcastForLiuHai(mContext);//add for debug log
        }
        /*PRIZE-add for liuhai screen-liufan-2018-05-17-end*/

        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(OPEN_LIUHAI_SCREEN && !OPEN_LIUHAI_SCREEN2){
            mLiuHaiIconControllerList.add(new LiuHaiStatusBarIconController(mContext,this));//for statusbar icon
            mLiuHaiIconControllerList.add(new LiuHaiStatusBarIconController(mContext,this));//for keyguard statusbar icon
            mLiuHaiIconControllerList.get(1).setKeyguardController(true);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

		/* prize-add-split screen-liyongli-20170612-start */
		if( PrizeOption.PRIZE_SYSTEMUI_QQPOP_ICON ){
			EventBus.getDefault().register(this);
		}
		/* prize-add-split screen-liyongli-20170612-end */

		/*prize-public-standard:Changed lock screen-liuweiquan-20151212-start*/
		//if(PrizeOption.PRIZE_CHANGED_WALLPAPER&&0!=Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_KGWALLPAPER_SWITCH,0)){
		if(PrizeOption.PRIZE_CHANGED_WALLPAPER || PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW){
			IntentFilter kgFilter = new IntentFilter();
			kgFilter.setPriority(1000);
			kgFilter.addAction(Intent.ACTION_SCREEN_ON);
			kgFilter.addAction(Intent.ACTION_SCREEN_OFF);
			kgFilter.addAction(KGWALLPAPER_SETTING_ON_ACTION);
			kgFilter.addAction(KGWALLPAPER_SETTING_OFF_ACTION);
			if(PrizeOption.PRIZE_POWER_EXTEND_MODE){
				kgFilter.addAction(ACTION_CLOSE_SUPERPOWER_NOTIFICATION);
				kgFilter.addAction(ACTION_KILL_SUPERPOWER);
				kgFilter.addAction(ACTION_EXIT_POWERSAVING);				
				bIntoSuperSavingPower= PowerManager.isSuperSaverMode();
			}			
			mContext.registerReceiver(mChangedWallpaperReceiver, kgFilter);
			mBaiBianWallpaperObserver = new BaiBianWallpaperObserver(mHandler);
            mBaiBianWallpaperObserver.startObserving();
			
			bChangedWallpaperIsOpen = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_KGWALLPAPER_SWITCH,0) == 1;
			sChangedWallpaperPath = Settings.Global.getString(mContext.getContentResolver(),Settings.Global.PRIZE_KGWALLPAPER_PATH);
		}
		/*prize-public-standard:Changed lock screen-liuweiquan-20151212-end*/
        super.start(); // calls createAndAddWindows()

        mMediaSessionManager
                = (MediaSessionManager) mContext.getSystemService(Context.MEDIA_SESSION_SERVICE);
        // TODO: use MediaSessionManager.SessionListener to hook us up to future updates
        // in session state

        addNavigationBar();

        /* Reposition nav bar back key feature & Dynamically hiding nav bar feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR || PrizeOption.PRIZE_REPOSITION_BACK_KEY) {
            if (mNavigationBarView != null) {
                mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.PRIZE_NAVIGATION_BAR_STYLE),
                    false, mNavbarStyleObserver);
            }

        } //END...

        /* Dynamically changing Recents function feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_TREAT_RECENTS_AS_MENU) {
            if (mNavigationBarView != null) {
                mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.PRIZE_TREAT_RECENTS_AS_MENU),
                    false, mEnableTreatRecentsAsMenuObserver);
            }
        } //END...

        /* Nav bar related to mBack key feature. prize-linkh-20160804 */
        if(SUPPORT_NAV_BAR_FOR_MBACK_DEVICE) {
            if (mNavigationBarView != null) {
                mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.PRIZE_NAVBAR_STATE_FOR_MBACK),
                    false, mNavBarStateFormBackObserver);
            }
        }        
        //END....
        //prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar start
        if(OPEN_GESTURE_NAVIGATION) {
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.PRIZE_HIDE_SWIPE_UP_GESTURE_INDICATOR),
                    false, mGestureIndicatorStateObserver);
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.PRIZE_SWIPE_UP_GESTURE_STATE),
                    false, mGestureStateObserver);
        }
        //prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar start
        // Lastly, call to the icon policy to install/update all the icons.
        mIconPolicy = new PhoneStatusBarPolicy(mContext, mIconController, mCastController,
                mHotspotController, mUserInfoController, mBluetoothController,
                mRotationLockController, mNetworkController.getDataSaverController());
        mIconPolicy.setCurrentUserSetup(mUserSetup);
        mSettingsObserver.onChange(false); // set up

        mHeadsUpObserver.onChange(true); // set up
        if (ENABLE_HEADS_UP) {
            mContext.getContentResolver().registerContentObserver(
                    Settings.Global.getUriFor(Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED), true,
                    mHeadsUpObserver);
            mContext.getContentResolver().registerContentObserver(
                    Settings.Global.getUriFor(SETTING_HEADS_UP_TICKER), true,
                    mHeadsUpObserver);
        }
        mUnlockMethodCache = UnlockMethodCache.getInstance(mContext);
        mUnlockMethodCache.addListener(this);
        startKeyguard();

        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mUpdateCallback);
        mDozeServiceHost = new DozeServiceHost();
        putComponent(DozeHost.class, mDozeServiceHost);
        putComponent(PhoneStatusBar.class, this);

        setControllerUsers();

        notifyUserAboutHiddenNotifications();

        mScreenPinningRequest = new ScreenPinningRequest(mContext);
        mFalsingManager = FalsingManager.getInstance(mContext);
        /*PRIZE-add for magazine-liufan-2018-01-19-start*/
        if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW) {
            if(mMagazineGateObserver == null){
                mMagazineGateObserver = new MagazineGateObserver(mHandler);
            }
            mMagazineGateObserver.startObserving();
        }
        /*PRIZE-add for magazine-liufan-2018-01-19-end*/
        /*PRIZE-add for bugid:50292-liufan-2018-02-26-start*/
        Log.d(TAG,"isCollapseAllPanelsAnim = " + isCollapseAllPanelsAnim);
        isCollapseAllPanelsAnim = false;
        /*PRIZE-add for bugid:50292-liufan-2018-02-26-end*/
    }
	//add for statusbar inverse. prize-linkh-20150903
    private int mCurStatusBarStyle = StatusBarManager.STATUS_BAR_INVERSE_DEFALUT; 
    private ArrayList<PrizeStatusBarStyleListener> mStatusBarStyleListeners = new ArrayList<PrizeStatusBarStyleListener>();
    private TextView mBatteryPercentageView;
    private TextView mClockView;
    private BatteryMeterViewDefinedNew mBatteryMeterView;

    
    public boolean isValidStatusBarStyle(int style) {
        boolean valid = true;
        if(style < 0 || style >= StatusBarManager.STATUS_BAR_INVERSE_TOTAL) {
            valid = false;
        }
        
        Log.d(TAG, "isValidStatusBarStyle(). style=" + style + ", valid=" + valid);
        return valid;
    }
    
    private void onStatusBarStyleChanged(int newStyle) {
        Log.d(TAG, "onStatusBarStyleChanged(). style=" + newStyle);
        //mGestureIndicatorManager.updateViewStyle(newStyle);
        if(mCurStatusBarStyle != newStyle) {
            setStatusBarStyle(newStyle);
        }
    }

    private void setStatusBarStyle(int style) {
        if(!isValidStatusBarStyle(style)) {
            return;
        }

        mCurStatusBarStyle = style;
        notifyStausBarStyleChanged();
        updateViewsForStatusBarStyleChanged();
    }
    
    private void notifyStausBarStyleChanged() {
        Log.d(TAG, "notifyStausBarStyleChanged().");
        
        final int length = mStatusBarStyleListeners.size();
        for (int i = 0; i < length; i++) {
            mStatusBarStyleListeners.get(i).onStatusBarStyleChanged(mCurStatusBarStyle);
        }
    }

    public void addStatusBarStyleListener(PrizeStatusBarStyleListener l) {
        addStatusBarStyleListener(l, false);
    }
    
    public void addStatusBarStyleListener(PrizeStatusBarStyleListener l, boolean immediatelyNotify) {
        if(l != null) {
            mStatusBarStyleListeners.add(l);
            if(immediatelyNotify) {
                l.onStatusBarStyleChanged(mCurStatusBarStyle);
            }
        }
    }

    public void removeStatusBarStyleListener(PrizeStatusBarStyleListener l) {
        mStatusBarStyleListeners.remove(l);
    }


    
    public void updateViewsForStatusBarStyleChanged() {
        Log.d(TAG, "updateViewsForStatusBarStyleChanged()....");
        PrizeStatusBarStyle statusBarStyle = PrizeStatusBarStyle.getInstance(mContext);
        int textColor = statusBarStyle.getColor(mCurStatusBarStyle);

        mIconController.updateViewsForStatusBarStyleChanged();
        //status bar
        if(mStatusBarView != null) {
            //Clock
            if(mClockView != null) {
                mClockView.setTextColor(textColor);
            }


            // Signal cluster Area
            // Update them through StatusBarStyleListener callback.


            // Battery area
            if(mBatteryPercentageView != null) {
                mBatteryPercentageView.setTextColor(textColor);
            }
            
            //For design reason, we update this view in its class.
            if(mBatteryMeterView != null) {
                mBatteryMeterView.onStatusBarStyleChanged(mCurStatusBarStyle);
            }

            /*PRIZE-add for liuhai screen-liufan-2018-04-17-start*/
            if(OPEN_LIUHAI_SCREEN || OPEN_LIUHAI_SCREEN2){
                mLiuHaiStatusBarTv.setTextColor(textColor);
                mLiuHaiStatusBarRightTv.setTextColor(textColor);
            } else
            /*PRIZE-add for liuhai screen-liufan-2018-04-17-end*/
            if (mStatusBarTv !=null){
                mStatusBarTv.setTextColor(textColor);
            }
        }
        /*PRIZE-add for network speed-liufan-2016-09-20-start*/
        if(mNetworkSpeedTxt!=null){
            mNetworkSpeedTxt.setTextColor(textColor);
        }
        /*PRIZE-add for network speed-liufan-2016-09-20-end*/

    }
   
    private void getViewsForStatusBarStyle() {
        mBatteryPercentageView = (TextView)mStatusBarView.findViewById(R.id.battery_percentage);        
		View mNotificationIconArea = mStatusBarView.findViewById(R.id.notification_icon_area_inner);
        mClockView = (TextView)mNotificationIconArea.findViewById(R.id.clock);
        /**PRIZE-new Battery icon-liufan-2015-10-30-start */
        mBatteryMeterView = (BatteryMeterViewDefinedNew)mStatusBarView.findViewById(R.id.battery_new);
        /**PRIZE-new Battery icon-liufan-2015-10-30-end */
        
    }

   @Override
   public void onStatusBarInverseChanged(int style) {
       /*if (mStatusBarBackgroundColor !=0){
           onStatusBarStyleChanged(StatusBarManager.STATUS_BAR_INVERSE_WHITE);
           return;
       }*/
        onStatusBarStyleChanged(style);
   }
   //end..............

    protected void createIconController() {
        mIconController = new StatusBarIconController(
                mContext, mStatusBarView, mKeyguardStatusBar, this);
		//Add for backgroud_reflect-luyingying-2017-09-09-Start*/
        mIconController.setTextColor(textColor, true);
        //Add end
        /*PRIZE-add for clock view- liufan-2016-11-07-start*/
        View mNotificationIconAreaInner = mIconController.getNotificationIconLayout();
		View mClock = mNotificationIconAreaInner.findViewById(R.id.clock);
		PhoneStatusBarTransitions mPhoneStatusBarTransitions = (PhoneStatusBarTransitions)mStatusBarView.getBarTransitions();
        mPhoneStatusBarTransitions.setClockView(mClock);
        /*PRIZE-add for clock view- liufan-2016-11-07-end*/
    }

   /* Reposition nav bar back key feature & Dynamically hiding nav bar feature. prize-linkh-20161115 */
   private static final boolean DEBUG_NAV_BAR = NavigationBarView.DEBUG_NAV_BAR;
   private static final String NAV_BAR_CONTROL_INTENT = "com.prize.nav_bar_control";
   private static final String NAV_BAR_CONTROL_CMD = "command";
   private static final String NAV_BAR_STATE = "state";

   private NavigationBarView mNavigationBarViewBackup = null;
   private boolean mIsNavBarHidden = false;
   private int mCurNavBarStyle;

   // Solve an issue for dynamically hiding nav bar(bug-19908). prize-linkh-20160808
   // recording for Phone Window Manager to choose different policies.
   public static final String NAV_BAR_GONE_BECAUSE_OF_KEYGUARD = "keyguard";
   public static final String NAV_BAR_GONE_BECAUSE_OF_MBACK = "mback";
   public static final String NAV_BAR_GONE_BECAUSE_OF_USER = "user";
   //private String mNavBarGoneReason;
   private WindowManager.LayoutParams mNavBarLayoutParams;
   // END...
   
   /* Nav bar related to mBack key feature. prize-linkh-20160804 */
   // When mBack doesn't work for some reasons, we can provide
   // nav bar for user to rescue this device. 
   public static final boolean SUPPORT_NAV_BAR_FOR_MBACK_DEVICE = PrizeOption.PRIZE_SUPPORT_NAV_BAR_FOR_MBACK_DEVICE;
   // System supports nav bar and we hide nav bar by default.    
   private boolean mDisallowShowingNavBarFormBack = SUPPORT_NAV_BAR_FOR_MBACK_DEVICE;
   private ContentObserver mNavBarStateFormBackObserver = new ContentObserver(new Handler()) {
           @Override
           public void onChange(boolean selfChange) {
               printMyLog("mNavBarStateFormBackObserver--onChange() --");
               int state = Settings.System.getInt(mContext.getContentResolver(), 
                               Settings.System.PRIZE_NAVBAR_STATE_FOR_MBACK, 0);
               boolean disallow = (state == 1) ? false : true;
               if(mNavigationBarView != null) {
                   mDisallowShowingNavBarFormBack = disallow;
                   if (mDisallowShowingNavBarFormBack) {
                       hideNavBarFormBack();
                   } else {
                       showNavBarFormBack();
                   }
               }
           }
   
   };
   //prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar start
   private ContentObserver mGestureIndicatorStateObserver = new ContentObserver(new Handler()) {
           @Override
           public void onChange(boolean selfChange) {
               int state = Settings.System.getInt(mContext.getContentResolver(), 
                               Settings.System.PRIZE_HIDE_SWIPE_UP_GESTURE_INDICATOR, 0);
               boolean needHide = (state == 1) ? true : false;
               if(needHide)
               	mGestureIndicatorManager.hideView();
               else
               	mGestureIndicatorManager.showView();
           }
   
   };

   private ContentObserver mGestureStateObserver = new ContentObserver(new Handler()) {
           @Override
           public void onChange(boolean selfChange) {
               int state = Settings.System.getInt(mContext.getContentResolver(), 
                               Settings.System.PRIZE_SWIPE_UP_GESTURE_STATE, 0);
               boolean mEnableGestureNavigation = (state == 1) ? true : false;
               Log.d(TAG, "mEnableGestureNavigation" + mEnableGestureNavigation);
               if(mEnableGestureNavigation){
               	mGestureIndicatorManager.showView();
               }else{
               	mGestureIndicatorManager.hideView();
               }
           }
   
   };
   //prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar end


   private boolean needHideNavBarFormBack() {
   		//prize tangzhengrong 20180426 Swipe-up Gesture Navigation bar start
   		if(OPEN_GESTURE_NAVIGATION)
			return Settings.System.getInt(mContext.getContentResolver(), 
                               Settings.System.PRIZE_NAVBAR_STATE_FOR_MBACK, 1) != 1;
   		//prize tangzhengrong 20180426 Swipe-up Gesture Navigation bar end
       return Settings.System.getInt(mContext.getContentResolver(), 
                               Settings.System.PRIZE_NAVBAR_STATE_FOR_MBACK, 0) != 1;
   }
   private void hideNavBarFormBack() {
       printMyLog("-hideNavBarFormBack()");
       hideNavBar(true);
   }
   
   private void showNavBarFormBack() {
       printMyLog("-showNavBarFormBack()");
       showNavBar(true);
   }
   //END...
   
   private View.OnClickListener mHideNavBarListener = new View.OnClickListener() {
       @Override
       public void onClick(View v) {
              printMyLog("onClick() hide nav bar!");
              hideNavBar();
         }
   };
   
   private void printMyLog(String msg) {
       if (DEBUG_NAV_BAR) {
           Log.d("PhoneStatusBar",  msg);           
       }
   }
   
   private void registerHideNavBarClickListener() {
       printMyLog("registerHideNavBarClickListener() ...");
       ButtonDispatcher hideButton = mNavigationBarView.getHideButton();
       hideButton.setOnClickListener(mHideNavBarListener);
   }
   
   private void hideNavBar() {
       hideNavBar(false);
   }
   
   private void hideNavBar(boolean ismBack) {
      printMyLog("hideNavBar() ismBack=" + ismBack);
      if(mNavigationBarView == null) {
          return;
      } else if (mIsNavBarHidden) {
          return;
      } else if (!SUPPORT_NAV_BAR_FOR_MBACK_DEVICE && !mNavigationBarView.canHideNavBarForCurStyle()) {
          printMyLog("hideNavBar() Can't hide nav bar because of style disallowed!");      
          return;
      }
       
      mIsNavBarHidden = true;
      mNavigationBarView.mHideByUser = true;
      mNavigationBarView.setVisibility(View.GONE);
      if (ismBack) {
           updateNavigationBarView(NAV_BAR_GONE_BECAUSE_OF_MBACK);
      } else {
          updateNavigationBarView(NAV_BAR_GONE_BECAUSE_OF_USER);
      }
      
      Settings.System.putInt(mContext.getContentResolver(),
           Settings.System.PRIZE_NAVBAR_STATE, 0);
   }
   
   private void showNavBar() {
       showNavBar(false);
   }
   
   private void showNavBar(boolean ismBack) {
       printMyLog("-showNavBar()");
      
       if (mNavigationBarView == null) {
           return;
       } else if (!mIsNavBarHidden) {
           return;
       }
   
       mIsNavBarHidden = false;
       mNavigationBarView.mHideByUser = false;
       mNavigationBarView.setVisibility(View.VISIBLE);
       //mNavBarGoneReason = null;
       updateNavigationBarView(null);
       
       Settings.System.putInt(mContext.getContentResolver(),
           Settings.System.PRIZE_NAVBAR_STATE, 1);
   }
   
   public void updateNavigationBarView(String navBarGoneReason) {
       printMyLog("updateNavigationBarView() goneReason=" + navBarGoneReason);
       if (mNavBarLayoutParams != null && mNavigationBarView != null) {
           mNavBarLayoutParams.navBarGoneReason = navBarGoneReason;
           mWindowManager.updateViewLayout(mNavigationBarView, mNavBarLayoutParams);
       }
   }
   
   private ContentObserver mNavbarStyleObserver = new ContentObserver(new Handler()) {
       @Override
       public void onChange(boolean selfChange) {
           int style = Settings.System.getInt(
                       mContext.getContentResolver(), 
                       Settings.System.PRIZE_NAVIGATION_BAR_STYLE,
                       NavigationBarInflaterView.STYLE_ORIGINAL);

           printMyLog("mNavbarStyleObserver--onChange() --style=" + style);
           prepareNavigationBarView();
           // Always show nav bar.
           showNavBar();
           /* Dynamically changing Recents function feature. prize-linkh-20161115 */  
           if (PrizeOption.PRIZE_TREAT_RECENTS_AS_MENU) {
               enableTreatRecentsAsMenu(mEnableTreatRecentsAsMenu, true);
           } //END...
       }   
    }; 
    //END......

    /* Dynamically changing Recents function feature. prize-linkh-20161115 */  
    private boolean mEnableTreatRecentsAsMenu;
    private ContentObserver mEnableTreatRecentsAsMenuObserver = new ContentObserver(new Handler()) {
       @Override
       public void onChange(boolean selfChange) {
           printMyLog("EnableTreatRecentsAsMenuObserver--onChange()..");
           final boolean enabled = Settings.System.getInt(mContext.getContentResolver(),                 
                                   Settings.System.PRIZE_TREAT_RECENTS_AS_MENU, 0) == 1;
           if (mEnableTreatRecentsAsMenu == enabled) {
               printMyLog("same state. Ignore.");
               return;
           }
           prepareNavigationBarView();
           enableTreatRecentsAsMenu(enabled);
       }
    };

    private void enableTreatRecentsAsMenu(boolean enable) {
        enableTreatRecentsAsMenu(enable, false);
    }
    private void enableTreatRecentsAsMenu(boolean enable, boolean force) {
        printMyLog("enableTreatRecentsAsMenu() enabled=" + enable + ", force=" + force);
        if (mNavigationBarView == null) {
            printMyLog("enableTreatRecentsAsMenu() mNavigationBarView=NULL! Ignore.");
            return;
        }
       
        if (!force && mEnableTreatRecentsAsMenu == enable) {
           printMyLog("enableTreatRecentsAsMenu() same state!");
           return;
        }

        mEnableTreatRecentsAsMenu = enable;
        mNavigationBarView.enableTreatRecentsAsMenu(enable);
        if (enable) {
           // Recents button
           ButtonDispatcher recentsButton = mNavigationBarView.getRecentsButton();
           View v = recentsButton.getCurrentView();
           if (v instanceof KeyButtonView) {
               ((KeyButtonView)v).setCode(android.view.KeyEvent.KEYCODE_MENU);
               recentsButton.setOnClickListener(null);
               recentsButton.setOnTouchListener(null);
               recentsButton.setLongClickable(true);
               recentsButton.setOnLongClickListener(mRecentsLongClickListener);
           }
           
           // Home button
           ButtonDispatcher homeButton = mNavigationBarView.getHomeButton();
           homeButton.setOnTouchListener(mRecentsPreloadForHomeOnTouchListener);
           homeButton.setOnLongClickListener(mLongPressHomeToRecentsListener);
        } else {
           // Recents button
           ButtonDispatcher recentsButton = mNavigationBarView.getRecentsButton();
           View v = recentsButton.getCurrentView();
           if (v instanceof KeyButtonView) {
               ((KeyButtonView)v).setCode(0);
           }
        }
    }

    private boolean mHasLongPressEvent =false;
    private View.OnLongClickListener mLongPressHomeToRecentsListener =
            new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {            
            if (DEBUG_NAV_BAR) {
                Log.d(TAG, "LongPressHomeToRecentsListener onLongClick() v=" + v);
            }
            //only simply tranfer it.
            mRecentsClickListener.onClick(v);
            mHasLongPressEvent = true;
            return true;
        }
    };

    private View.OnTouchListener mRecentsPreloadForHomeOnTouchListener = new View.OnTouchListener() {
        // additional optimization when we have software system buttons - start loading the recent
        // tasks on touch down
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction() & MotionEvent.ACTION_MASK;
            if (DEBUG_NAV_BAR) {
                if (action != MotionEvent.ACTION_MOVE) {
                    Log.d(TAG, "mRecentsPreloadForHomeOnTouchListener onTouch() action=" + action);
                }
            }            
            if (action == MotionEvent.ACTION_DOWN) {
                mHasLongPressEvent = false;
                if (DEBUG_NAV_BAR) {
                    Log.d(TAG, "preload recents...");
                }
                preloadRecents();
            } else if (action == MotionEvent.ACTION_CANCEL) {
                mHasLongPressEvent = false;
                /*if((mSystemUiVisibility & View.RECENT_APPS_VISIBLE) == 0) {
                    if (DEBUG_NAV_BAR) {
                        Log.d(TAG, "Recents isn't visible! cancel preloading recents!");
                    }                  
                    cancelPreloadingRecents();
                }*/
            } else if (action == MotionEvent.ACTION_UP) {
                if(mHasLongPressEvent) {
                    //In toggling recent. Send home cancel event instead of sending up event.
                    if (DEBUG_NAV_BAR) {
                        Log.d(TAG, "Send home cancel event instead of sending up event!");
                    }                    
                    ((KeyButtonView)v).setSendHomeCancelEventOnce(true);
                } else {                
                    ((KeyButtonView)v).setSendHomeCancelEventOnce(false);
                    //cancelPreloadingRecents();
                }
                
                mHasLongPressEvent = false;
            }
            
            return false;
        }
    };    
    //END....

    // Nav bar color customized feature. prize-linkh-2017.07.08 @{
    public static final boolean DEBUG_NAV_BAR_COLOR = NavigationBarView.DEBUG_NAV_BAR_COLOR;

    @Override
    public void updateNavBarColor(int color, boolean enableNavBarColor, boolean enableSystemUINavBarColorBackground) {
        updateNavBarColor(color, enableNavBarColor, false, false, enableSystemUINavBarColorBackground);
    }
    
    private void updateNavBarColor(int color, boolean enableNavBarColor, 
                    boolean force, boolean animate, boolean enableSystemUINavBarColorBackground) {
        if (DEBUG_NAV_BAR_COLOR) {
            Log.d(TAG, "updateNavBarColor() color=" + color 
                    + ", enableNavBarColor=" + enableNavBarColor
                    + ", force=" + force + ", animate=" + animate
                    + ", enableSystemUINavBarColorBackground=" + enableSystemUINavBarColorBackground);
        }

        if (mNavigationBarView != null) {
            mNavigationBarView.updateNavBarColor(color, enableNavBarColor, force, animate, enableSystemUINavBarColorBackground);
        }
    }
    // @}

   public static boolean BLUR_BG_CONTROL = true;//true:blur bg; false:scrim bg
   public static boolean STATUS_BAR_DROPDOWN_STYLE = true;//true:drop down; false:alpha change
    // ================================================================================
    // Constructing the view
    // ================================================================================
    protected PhoneStatusBarView makeStatusBarView() {
        final Context context = mContext;
        //prize add by xiarui 2018-04-11 start@{
        if (PRIZE_HEADS_UP_STYLE) {
            mTickerController = new PrizeTickerController(context);
            mTickerController.setStatusBar(this);
            mTickerController.registerBroadcastReceiver(context);
        }
        //---end@}
        updateDisplaySize(); // populates mDisplayMetrics
        updateResources();

        inflateStatusBarWindow(context);
        mStatusBarWindow.setService(this);
        mStatusBarWindow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                checkUserAutohide(v, event);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mExpandedVisible) {
                        animateCollapsePanels();
                    }
                }
                return mStatusBarWindow.onTouchEvent(event);
            }
        });

        mNotificationPanel = (NotificationPanelView) mStatusBarWindow.findViewById(
                R.id.notification_panel);
        mNotificationPanel.setStatusBar(this);
        mNotificationPanel.setGroupManager(mGroupManager);

        mStatusBarView = (PhoneStatusBarView) mStatusBarWindow.findViewById(R.id.status_bar);
        mStatusBarView.setBar(this);
        mStatusBarView.setPanel(mNotificationPanel);        
        /**PRIZE-control the battery show-liufan-2015-10-30-start */
        if(PrizeOption.PRIZE_SYSTEMUI_BATTERY_METER){
            (mStatusBarView.findViewById(R.id.battery_level)).setVisibility(View.VISIBLE);
            (mStatusBarView.findViewById(R.id.battery_new)).setVisibility(View.VISIBLE);
            (mStatusBarView.findViewById(R.id.battery)).setVisibility(View.GONE);
        } else {
            (mStatusBarView.findViewById(R.id.battery_level)).setVisibility(View.GONE);
            (mStatusBarView.findViewById(R.id.battery_new)).setVisibility(View.GONE);
            (mStatusBarView.findViewById(R.id.battery)).setVisibility(View.VISIBLE);
        }
        /**PRIZE-control the battery show-liufan-2015-10-30-end */
        /*PRIZE-lockscreen blur bg layout- liufan-2015-09-02-start*/
        mBlurBack = (LinearLayout) mStatusBarWindow.findViewById(R.id.blur_back);
        FrameLayout.LayoutParams blurBackParams = new FrameLayout.LayoutParams(mDisplayMetrics.widthPixels, LayoutParams.MATCH_PARENT);
        mBlurBack.setLayoutParams(blurBackParams);
        /*PRIZE-lockscreen blur bg layout- liufan-2015-09-02-end*/
        /*PRIZE-the blur layout- liufan-2015-06-09-start*/
        mNotificationBg = (LinearLayout) mStatusBarWindow.findViewById(
                R.id.notification_bg);
        /*PRIZE-the blur layout- liufan-2015-06-09-end*/
		
        /*PRIZE-KeyguardChargeAnimationView- liufan-2015-07-08-start*/
        mKeyguardChargeAnimationView = (KeyguardChargeAnimationView) mStatusBarWindow.findViewById(R.id.keyguard_charge_animation_view);
        mKeyguardChargeAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });
        /*PRIZE-KeyguardChargeAnimationView- liufan-2015-07-08-end*/
        

        //  M: setBackground in 512 low ram device
        if (!ActivityManager.isHighEndGfx() && !FeatureOptions.LOW_RAM_SUPPORT) {
            mStatusBarWindow.setBackground(null);
            mNotificationPanel.setBackground(new FastColorDrawable(context.getColor(
                    R.color.notification_panel_solid_background)));
        }

        mHeadsUpManager = new HeadsUpManager(context, mStatusBarWindow, mGroupManager);
        mHeadsUpManager.setBar(this);
        mHeadsUpManager.addListener(this);
        mHeadsUpManager.addListener(mNotificationPanel);
        mHeadsUpManager.addListener(mGroupManager);
        mNotificationPanel.setHeadsUpManager(mHeadsUpManager);
        mNotificationData.setHeadsUpManager(mHeadsUpManager);
        mGroupManager.setHeadsUpManager(mHeadsUpManager);

        if (MULTIUSER_DEBUG) {
            mNotificationPanelDebugText = (TextView) mNotificationPanel.findViewById(
                    R.id.header_debug_info);
            mNotificationPanelDebugText.setVisibility(View.VISIBLE);
        }

        try {
            boolean showNav = mWindowManagerService.hasNavigationBar();
            if (DEBUG) Log.v(TAG, "hasNavigationBar=" + showNav);
            if (showNav) {
                createNavigationBarView(context);
            }
        } catch (RemoteException ex) {
            // no window manager? good luck with that
        }
		//prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar start
		mGestureIndicatorManager = new GestureIndicatorManager(context);
		//prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar end
        mAssistManager = SystemUIFactory.getInstance().createAssistManager(this, context);

        // figure out which pixel-format to use for the status bar.
        mPixelFormat = PixelFormat.OPAQUE;

        mStackScroller = (NotificationStackScrollLayout) mStatusBarWindow.findViewById(
                R.id.notification_stack_scroller);
        /*PRIZE-cancel longclick press,bugid:45894,44702-liufan-2017-12-25-start*/
        //mStackScroller.setLongPressListener(getNotificationLongClicker());
        /*PRIZE-cancel longclick press,bugid:45894,44702-liufan-2017-12-25-end*/
        mStackScroller.setPhoneStatusBar(this);
        mStackScroller.setGroupManager(mGroupManager);
        mStackScroller.setHeadsUpManager(mHeadsUpManager);
        mGroupManager.setOnGroupChangeListener(mStackScroller);

        inflateOverflowContainer();
        inflateEmptyShadeView();
        inflateDismissView();
        mExpandedContents = mStackScroller;

        mBackdrop = (BackDropView) mStatusBarWindow.findViewById(R.id.backdrop);
        mBackdropFront = (ImageView) mBackdrop.findViewById(R.id.backdrop_front);
        mBackdropBack = (ImageView) mBackdrop.findViewById(R.id.backdrop_back);

        if (ENABLE_LOCKSCREEN_WALLPAPER) {
            mLockscreenWallpaper = new LockscreenWallpaper(mContext, this, mHandler);
        }

        ScrimView scrimBehind = (ScrimView) mStatusBarWindow.findViewById(R.id.scrim_behind);
        ScrimView scrimInFront = (ScrimView) mStatusBarWindow.findViewById(R.id.scrim_in_front);
        View headsUpScrim = mStatusBarWindow.findViewById(R.id.heads_up_scrim);
        mScrimController = SystemUIFactory.getInstance().createScrimController(
                scrimBehind, scrimInFront, headsUpScrim, mLockscreenWallpaper);
        /*PRIZE-send the blur layout to ScrimController- liufan-2015-06-09-start*/
        if (VersionControl.CUR_VERSION == VersionControl.BLUR_BG_VER) {
            if(STATUS_BAR_DROPDOWN_STYLE) mScrimController.setNotificationBackgroundLayout(mNotificationBg);
            mScrimController.setLockscreenBlurLayout(mBlurBack);
        }
        /*PRIZE-send the blur layout to ScrimController- liufan-2015-06-09-end*/
        if (mScrimSrcModeEnabled) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    boolean asSrc = mBackdrop.getVisibility() != View.VISIBLE;
                    mScrimController.setDrawBehindAsSrc(asSrc);
                    mStackScroller.setDrawBackgroundAsSrc(asSrc);
                }
            };
            mBackdrop.setOnVisibilityChangedRunnable(runnable);
            runnable.run();
        }
        mHeadsUpManager.addListener(mScrimController);
        mStackScroller.setScrimController(mScrimController);
        mStatusBarView.setScrimController(mScrimController);
        mDozeScrimController = new DozeScrimController(mScrimController, context);

        mKeyguardStatusBar = (KeyguardStatusBarView) mStatusBarWindow.findViewById(R.id.keyguard_header);

		//Modify by background_reflecting luyingying 2017-09-09
        mKeyguardStatusView = (KeyguardStatusView)mStatusBarWindow.findViewById(R.id.keyguard_status_view);
        //mKeyguardStatusView = mStatusBarWindow.findViewById(R.id.keyguard_status_view);
		//Modify end
        mKeyguardBottomArea =
                (KeyguardBottomAreaView) mStatusBarWindow.findViewById(R.id.keyguard_bottom_area);
		mEmergencyButton = mKeyguardBottomArea.getEmergencyButton();

        mKeyguardBottomArea.setActivityStarter(this);
        mKeyguardBottomArea.setAssistManager(mAssistManager);
        mKeyguardIndicationController = new KeyguardIndicationController(mContext,
                (KeyguardIndicationTextView) mStatusBarWindow.findViewById(
                        R.id.keyguard_indication_text),
                mKeyguardBottomArea.getLockIcon());
        mKeyguardBottomArea.setKeyguardIndicationController(mKeyguardIndicationController);

        // set the initial view visibility
        setAreThereNotifications();

        createIconController();
		//add for statusbar inverse. prize-linkh-20150903
		addStatusBarStyleListener(mIconController,false);
		//end...

        // Background thread for any controllers that need it.
        mHandlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();

        // Other icons
        mLocationController = new LocationControllerImpl(mContext,
                mHandlerThread.getLooper()); // will post a notification
        mBatteryController = createBatteryController();
        /**PRIZE 电量图标及百分比修改 2015-06-25 start */
        mBatteryController.addLevelView((TextView)mStatusBarView.findViewById(R.id.battery_percentage));
        /**PRIZE 电量图标及百分比修改 2015-06-25 end */
        mBatteryController.addStateChangedCallback(new BatteryStateChangeCallback() {
            @Override
            public void onPowerSaveChanged(boolean isPowerSave) {
                mHandler.post(mCheckBarModes);
                if (mDozeServiceHost != null) {
                    mDozeServiceHost.firePowerSaveChanged(isPowerSave);
                }
            }
            @Override
            public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
                // noop
            }
        });
        mNetworkController = new NetworkControllerImpl(mContext, mHandlerThread.getLooper());
        mNetworkController.setUserSetupComplete(mUserSetup);
        mHotspotController = new HotspotControllerImpl(mContext);
        mBluetoothController = new BluetoothControllerImpl(mContext, mHandlerThread.getLooper());
        mSecurityController = new SecurityControllerImpl(mContext);
        /// M: add extra tiles @{
        // add HotKnot in quicksetting
        if (SIMHelper.isMtkHotKnotSupport()) {
            Log.d(TAG, "makeStatusBarView : HotKnotControllerImpl");
            mHotKnotController = new HotKnotControllerImpl(mContext);
        } else {
            mHotKnotController = null;
        }

        SIMHelper.setContext(mContext);
        // /@}

        if (mContext.getResources().getBoolean(R.bool.config_showRotationLock)) {
            mRotationLockController = new RotationLockControllerImpl(mContext);
        }
        mUserInfoController = new UserInfoController(mContext);
        mVolumeComponent = getComponent(VolumeComponent.class);
        if (mVolumeComponent != null) {
            mZenModeController = mVolumeComponent.getZenController();
        }
        Log.d(TAG, "makeStatusBarView : CastControllerImpl +");
        mCastController = new CastControllerImpl(mContext);

        initSignalCluster(mStatusBarView);
        //add for statusbar inverse. prize-linkh-20150903
        final SignalClusterView signalCluster =
                (SignalClusterView) mStatusBarView.findViewById(R.id.signal_cluster);
        if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR) {
            signalCluster.setIgnoreStatusBarStyleChanged(false);
            addStatusBarStyleListener(mNetworkController, false);
        }
        initSignalCluster(mKeyguardStatusBar);
        initEmergencyCryptkeeperText();

        /// M: Support "Operator plugin - Customize Carrier Label for PLMN" @{
        mStatusBarPlmnPlugin = PluginManager.getStatusBarPlmnPlugin(context);
        if (supportCustomizeCarrierLabel()) {
            mCustomizeCarrierLabel = mStatusBarPlmnPlugin.customizeCarrierLabel(
                    mNotificationPanel, null);
        }
        /// M: Support "Operator plugin - Customize Carrier Label for PLMN" @}

        mFlashlightController = new FlashlightController(mContext);
        mKeyguardBottomArea.setFlashlightController(mFlashlightController);
        mKeyguardBottomArea.setPhoneStatusBar(this);
        mKeyguardBottomArea.setUserSetupComplete(mUserSetup);
        mAccessibilityController = new AccessibilityController(mContext);
        mKeyguardBottomArea.setAccessibilityController(mAccessibilityController);
        mNextAlarmController = new NextAlarmController(mContext);
        mLightStatusBarController = new LightStatusBarController(mIconController,
                mBatteryController);
        mKeyguardMonitor = new KeyguardMonitor(mContext);
        mUserSwitcherController = new UserSwitcherController(mContext, mKeyguardMonitor,
                mHandler, this);
        if (UserManager.get(mContext).isUserSwitcherEnabled()) {
            createUserSwitcher();
        }

        // Set up the quick settings tile panel
        AutoReinflateContainer container = (AutoReinflateContainer) mStatusBarWindow.findViewById(
                R.id.qs_auto_reinflate_container);
        if (container != null) {

            mBrightnessMirrorController = new BrightnessMirrorController(mStatusBarWindow);
            /*PRIZE-add for brightness controller- liufan-2016-06-29-start*/
            mBrightnessMirrorController.setPhoneStatusBar(this);
            /*PRIZE-add for brightness controller- liufan-2016-06-29-end*/
            container.addInflateListener(new InflateListener() {
                @Override
                public void onInflated(View v) {
            		/*PRIZE-set the listener of the base tile- liufan-2015-04-10-start*/
                    QSContainer qsContainer = (QSContainer) v.findViewById(
                            R.id.quick_settings_container);
                    mHeader = qsContainer.getHeader();
                    /*PRIZE-dismiss edit icon,bugid:43965-liufan-2017-11-30-start*/
                    mHeader.setPhoneStatusBar(PhoneStatusBar.this);
                    /*PRIZE-dismiss edit icon,bugid:43965-liufan-2017-11-30-end*/
            		mNotificationHeaderBg = (NotificationHeaderLayout) mHeader.findViewById(
            		        R.id.notification_bg_header);
                    mQSDetail = qsContainer.getQSDetail();
							
		            QSTileHost qsh =  FeatureOption.PRIZE_QS_SORT ? new QSTileHost(mContext, PhoneStatusBar.this,
		                    mBluetoothController, mLocationController, mRotationLockController,
		                    mNetworkController, mZenModeController, mHotspotController,
		                    mCastController, mFlashlightController,
		                    mUserSwitcherController, mUserInfoController, mKeyguardMonitor,
		                    mSecurityController, mBatteryController, mIconController,
		                    mNextAlarmController,
		                    /// M: add HotKnot in quicksetting
		                    mHotKnotController,
		                    onTileClickListener,
		                    mHeader,mBatteryController)
		            : SystemUIFactory.getInstance().createQSTileHost(mContext, PhoneStatusBar.this,
		                    mBluetoothController, mLocationController, mRotationLockController,
		                    mNetworkController, mZenModeController, mHotspotController,
		                    mCastController, mFlashlightController,
		                    mUserSwitcherController, mUserInfoController, mKeyguardMonitor,
		                    mSecurityController, mBatteryController, mIconController,
		                    mNextAlarmController,
		                    /// M: add HotKnot in quicksetting
		                    mHotKnotController);
					
                    qsContainer.setHost(qsh);
                    mQSPanel = qsContainer.getQsPanel();
                    mQSPanel.setBrightnessMirror(mBrightnessMirrorController);
                    mKeyguardStatusBar.setQSPanel(mQSPanel);
                    initSignalCluster(mHeader);
                    mHeader.setActivityStarter(PhoneStatusBar.this);
					/*PRIZE-set the listener of the base tile- liufan-2015-04-10-end*/
                }
            });
        }

        // User info. Trigger first load.
        mKeyguardStatusBar.setUserInfoController(mUserInfoController);
        mKeyguardStatusBar.setUserSwitcherController(mUserSwitcherController);
        mUserInfoController.reloadUserInfo();

        ((BatteryMeterView) mStatusBarView.findViewById(R.id.battery)).setBatteryController(
                mBatteryController);
        mKeyguardStatusBar.setBatteryController(mBatteryController);

        mReportRejectedTouch = mStatusBarWindow.findViewById(R.id.report_rejected_touch);
        if (mReportRejectedTouch != null) {
            updateReportRejectedTouchVisibility();
            mReportRejectedTouch.setOnClickListener(v -> {
                Uri session = mFalsingManager.reportRejectedTouch();
                if (session == null) { return; }

                StringWriter message = new StringWriter();
                message.write("Build info: ");
                message.write(SystemProperties.get("ro.build.description"));
                message.write("\nSerial number: ");
                message.write(SystemProperties.get("ro.serialno"));
                message.write("\n");

                PrintWriter falsingPw = new PrintWriter(message);
                FalsingLog.dump(falsingPw);
                falsingPw.flush();

                startActivityDismissingKeyguard(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                                .setType("*/*")
                                .putExtra(Intent.EXTRA_SUBJECT, "Rejected touch report")
                                .putExtra(Intent.EXTRA_STREAM, session)
                                .putExtra(Intent.EXTRA_TEXT, message.toString()),
                        "Share rejected touch report")
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        true /* onlyProvisioned */, true /* dismissShade */);
            });
        }

        //add for statusbar inverse. prize-linkh-20150903
        if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR) {
            //Init it.
            PrizeStatusBarStyle.getInstance(context);
            getViewsForStatusBarStyle();
        }
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mBroadcastReceiver.onReceive(mContext,
                new Intent(pm.isScreenOn() ? Intent.ACTION_SCREEN_ON : Intent.ACTION_SCREEN_OFF));
        mGestureWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                "GestureWakeLock");
        mVibrator = mContext.getSystemService(Vibrator.class);

        /*prize add by xiarui for face id 2018-04-04 start*/
        if (PrizeOption.PRIZE_FACE_ID) {
            if (mKeyguardBottomArea != null) {
                mKeyguardBottomArea.registerFaceIdReceiver();
                mKeyguardBottomArea.registerFaceIdSwitchObserver();
            }
        }
        /*prize add by xiarui for face id 2018-04-04 end*/

        /** prize add by xiarui 2018-01-25 start **/
        registerAutoClearReceiver();
        startWatchingCloudListFile();
        /** prize add by xiarui 2018-01-25 end **/

        //prize tangzhengrong 20180417 Swipe-up Gesture Navigation bar start
        if(OPEN_GESTURE_NAVIGATION){
        	registerSwipeUpGestureReceiver();
        }
        //prize tangzhengrong 20180417 Swipe-up Gesture Navigation bar end

        // receive broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
		
		/*PRIZE-PowerExtendMode-to solve can't unlock problem-wangxianzhen-2016-01-13-start bugid 10971*/
        filter.addAction(ACTION_ENTER_SUPERPOWER);
        /*PRIZE-PowerExtendMode-to solve can't unlock problem-wangxianzhen-2016-01-13-end bugid 10971*/

        /* Dynamically hiding nav bar feature. prize-linkh-20161115 */
        if(PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR) {
            filter.addAction(NAV_BAR_CONTROL_INTENT);
        } //END...        
        context.registerReceiverAsUser(mBroadcastReceiver, UserHandle.ALL, filter, null, null);

        IntentFilter demoFilter = new IntentFilter();
        if (DEBUG_MEDIA_FAKE_ARTWORK) {
            demoFilter.addAction(ACTION_FAKE_ARTWORK);
        }
        demoFilter.addAction(ACTION_DEMO);
        context.registerReceiverAsUser(mDemoReceiver, UserHandle.ALL, demoFilter,
                android.Manifest.permission.DUMP, null);

        // listen for USER_SETUP_COMPLETE setting (per-user)
        resetUserSetupObserver();

        // disable profiling bars, since they overlap and clutter the output on app windows
        ThreadedRenderer.overrideProperty("disableProfileBars", "true");

        // Private API call to make the shadows look better for Recents
        ThreadedRenderer.overrideProperty("ambientRatio", String.valueOf(1.5f));
        mStatusBarPlmnPlugin.addPlmn((LinearLayout)mStatusBarView.
                                     findViewById(R.id.status_bar_contents), mContext);

		
        /*PRIZE-show the percent of the power-liyao-2015-7-3-start*/
        if(PrizeOption.PRIZE_SYSTEMUI_BATTERY_METER){
            mShowBatteryPercentageObserver = new ShowBatteryPercentageObserver(mHandler);
            mShowBatteryPercentageObserver.startObserving();
        }
        /*PRIZE-show the percent of the power-liyao-2015-7-3-end*/

        /*PRIZE-listen the battery change-liufan-2015-7-8-start*/
        IntentFilter batteryFilter = new IntentFilter();
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mBatteryTracker, batteryFilter);
        /*PRIZE-listen the battery change-liufan-2015-7-8-end*/
        /*PRIZE-register launcher theme change receiver-liufan-2016-05-12-start*/
        LoadIconUtils.registerLauncherThemeReceiver(mContext, mReceiver);
        /*PRIZE-register launcher theme change receiver-liufan-2016-05-12-end*/

        /*PRIZE-add for network speed-liufan-2016-09-20-start*/
        IntentFilter networkFilter = new IntentFilter();
        networkFilter.addAction(NETSTATE_CHANGE_ACTION);
        mContext.registerReceiver(mNetworkStateReceiver, networkFilter);

        mNetworkSpeedObserver = new NetworkSpeedObserver(mHandler);
        mNetworkSpeedObserver.startObserving();

        mNetworkSpeedTxt = (TextView)(mStatusBarView.findViewById(R.id.network_speed_prize));
        mNetworkSpeedTxt.setTextColor(PrizeStatusBarStyle.getInstance(mContext).getColor(mCurStatusBarStyle));

        mHandler.removeCallbacks(networkSpeedRunable);
        mHandler.post(networkSpeedRunable);
        /*PRIZE-add for network speed-liufan-2016-09-20-end*/

        /*PRIZE-add for background color-liufan-2017-08-28-start*/
        mStatubBarBgView = mStatusBarView.findViewById(R.id.status_bar_bg_prize);
        mStatusBarTv = (TextView) mStatusBarView.findViewById(R.id.status_bar_text);
        mStatubBarBgView.setVisibility(View.GONE);
        mStatusBarTv.setVisibility(View.GONE);
        objectAnimator = ObjectAnimator.ofFloat(mStatusBarTv, "alpha", 1.0f, 0.9f, 0.1f, 0.0f, 0.0f, 0.1f, 0.9f, 1.0f);
        SET_STATUSBAR_BACKGROUND_PKG = null;
        /*PRIZE-add for background color-liufan-2017-08-28-end*/
        
		/*PRIZE-three_finger_moveup_open split screen-liyongli-2018-04-23-start*/
        IntentFilter openSplitScreenFilter = new IntentFilter();
        openSplitScreenFilter.addAction("org.android.prize.OPENSPLITSCREEN"); //
        mContext.registerReceiver(m3FingerMoveUpReceiver, openSplitScreenFilter);
		/*PRIZE-three_finger_moveup_open split screen-liyongli-2018-04-23-end*/

        /*PRIZE-add for liuhai screen-liufan-2018-04-17-start*/
        mLiuHaiLayout = mStatusBarView.findViewById(R.id.prize_liuhai_status_bar_layout);
        mLiuHaiLayout.setVisibility(View.GONE);
        mLiuHaiStatusBarTv = (TextView)mLiuHaiLayout.findViewById(R.id.prize_liuhai_status_bar_text);
        mLiuHaiStatusBarRightTv = (TextView)mLiuHaiLayout.findViewById(R.id.prize_liuhai_status_bar_right_text);
        if(OPEN_LIUHAI_SCREEN || OPEN_LIUHAI_SCREEN2){
            objectAnimator = ObjectAnimator.ofFloat(mLiuHaiLayout, "alpha", 1.0f, 0.9f, 0.1f, 0.0f, 0.0f, 0.1f, 0.9f, 1.0f);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-17-end*/
        return mStatusBarView;
    }

    private void initEmergencyCryptkeeperText() {
        View emergencyViewStub = mStatusBarWindow.findViewById(R.id.emergency_cryptkeeper_text);
        if (mNetworkController.hasEmergencyCryptKeeperText()) {
            if (emergencyViewStub != null) {
                ((ViewStub) emergencyViewStub).inflate();
            }
            mNetworkController.addSignalCallback(new NetworkController.SignalCallback() {
                @Override
                public void setIsAirplaneMode(NetworkController.IconState icon) {
                    recomputeDisableFlags(true /* animate */);
                }
            });
        } else if (emergencyViewStub != null) {
            ViewGroup parent = (ViewGroup) emergencyViewStub.getParent();
            parent.removeView(emergencyViewStub);
        }
    }

    /*PRIZE-add for background color-liufan-2017-08-28-start*/
    private View mStatubBarBgView;
    private TextView mStatusBarTv;
    private ObjectAnimator objectAnimator;
    private int mStatusBarBackgroundColor;
    public static String SET_STATUSBAR_BACKGROUND_PKG;
    private Intent statusbarBackgroundIntent;
    private PendingIntent statusbarBackgroundPendingIntent;

    /*PRIZE-add for liuhai screen-liufan-2018-04-17-start*/
    private View mLiuHaiLayout;
    private TextView mLiuHaiStatusBarTv;
    private TextView mLiuHaiStatusBarRightTv;
    /*PRIZE-add for liuhai screen-liufan-2018-04-17-end*/

    @Override
    public void setStatusBarBackgroundColor(String pkg, int color) {
        if(mStatusBarBackgroundColor != color || SET_STATUSBAR_BACKGROUND_PKG != pkg) {
            mStatusBarBackgroundColor = color;
            SET_STATUSBAR_BACKGROUND_PKG = pkg;
        }
        Log.d(TAG,"prize setStatusBarBackgroundColor : pkg = " + pkg + ", color = " + color);
        refreshStatusBarBackgroundColor();
    }

    @Override
    public void setStatusBarText(String str){
        /*PRIZE-add for liuhai screen-liufan-2018-04-17-start*/
        if(OPEN_LIUHAI_SCREEN || OPEN_LIUHAI_SCREEN2){
            String[] split = str.split("#");
            if (split.length > 1) {
                mLiuHaiStatusBarTv.setText(split[0]);
                mLiuHaiStatusBarRightTv.setText(split[1]);
            } else {
                mLiuHaiStatusBarTv.setText(str);
                mLiuHaiStatusBarRightTv.setText(mContext.getString(R.string.status_bar_right_text));
            }
            return ;
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-17-end*/
        mStatusBarTv.setText(str);
    }

    public void responseStatusBarBackgroundClick(){
        if(mStatubBarBgView.getVisibility() == View.VISIBLE){
            if(statusbarBackgroundIntent!=null){
                try{
                    mContext.startActivity(statusbarBackgroundIntent);
                    Log.d(TAG, "responseStatusBarBackgroundClick startActivity " + statusbarBackgroundIntent);
                }catch(Exception e){
                    Log.d(TAG, "responseStatusBarBackgroundClick error e = ", e);
                }
            } else if (statusbarBackgroundPendingIntent != null) {
                try {
                    statusbarBackgroundPendingIntent.send();
                    Log.i(TAG, "sending statusbarBackgroundPendingIntent success");
                } catch (PendingIntent.CanceledException e) {
                    Log.i(TAG, "sending statusbarBackgroundPendingIntent failed");
                }
            } else {
                Log.d(TAG, "responseStatusBarBackgroundClick statusbarBackgroundIntent & statusbarBackgroundPendingIntent is null");
            }
        } else {
            Log.d(TAG, "responseStatusBarBackgroundClick mStatubBarBgView.visibile = " + mStatubBarBgView.getVisibility());
        }
    }

    @Override
    public void setStatusBarBackgroundClickEvent(final Intent intent) {
        Log.d(TAG,"prize setStatusBarBackgroundClickEvent intent = " + intent);
        statusbarBackgroundIntent = intent;
    }

    @Override
    public void setStatusBarBackgroundClickPendingEvent(PendingIntent pendingIntent) {
        Log.d(TAG,"prize setStatusBarBackgroundClickPendingEvent pendingEvent = " + pendingIntent);
        statusbarBackgroundPendingIntent = pendingIntent;
    }

    @Override
    public void clearStatusBarBackgroundColor() {
        mStatusBarBackgroundColor = 0;
        statusbarBackgroundIntent = null;
        SET_STATUSBAR_BACKGROUND_PKG = null;
        Log.d(TAG,"prize clearStatusBarBackgroundColor.");
        refreshStatusBarBackgroundColor();
    }

    public void refreshStatusBarBackgroundColor(){
        if(mStatubBarBgView == null){
            return ;
        }
        if(mStatusBarBackgroundColor != 0 && mState == StatusBarState.SHADE
            /*&& !mStatusBarKeyguardViewManager.isOccluded()*/){
            mStatubBarBgView.setVisibility(View.VISIBLE);
            mStatubBarBgView.setBackgroundColor(mStatusBarBackgroundColor);
            /*PRIZE-add for liuhai screen-liufan-2018-04-17-start*/
            if(OPEN_LIUHAI_SCREEN || OPEN_LIUHAI_SCREEN2){
                mLiuHaiLayout.setVisibility(View.VISIBLE);
                mLiuHaiLayout.setBackgroundColor(mStatusBarBackgroundColor);
            } else {
                mStatusBarTv.setVisibility(View.VISIBLE);
                mStatusBarTv.setBackgroundColor(mStatusBarBackgroundColor);
            }
            /*PRIZE-add for liuhai screen-liufan-2018-04-17-end*/
             objectAnimator.setDuration(5000);
            objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
            objectAnimator.setRepeatMode(ValueAnimator.INFINITE);
            objectAnimator.start();
            Log.d(TAG,"refresh setStatusBarBackgroundColor : "+mStatusBarBackgroundColor);
        } else {
            mStatubBarBgView.setBackground(null);
            mStatubBarBgView.setVisibility(View.GONE);
            objectAnimator.end();
            /*PRIZE-add for liuhai screen-liufan-2018-04-17-start*/
            if(OPEN_LIUHAI_SCREEN || OPEN_LIUHAI_SCREEN2){
                mLiuHaiLayout.setVisibility(View.GONE);
                mLiuHaiLayout.setBackground(null);
            } else {
                mStatusBarTv.setVisibility(View.GONE);
                mStatusBarTv.setBackground(null);
            }
            /*PRIZE-add for liuhai screen-liufan-2018-04-17-end*/
            Log.d(TAG,"refresh clearStatusBarBackgroundColor.");
        }
    }
    /*PRIZE-add for background color-liufan-2017-08-28-end*/

    protected BatteryController createBatteryController() {
        return new BatteryControllerImpl(mContext);
    }

    private void inflateOverflowContainer() {
        mKeyguardIconOverflowContainer =
                (NotificationOverflowContainer) LayoutInflater.from(mContext).inflate(
                        R.layout.status_bar_notification_keyguard_overflow, mStackScroller, false);
        mKeyguardIconOverflowContainer.setOnActivatedListener(this);
        mKeyguardIconOverflowContainer.setOnClickListener(mOverflowClickListener);
        mStackScroller.setOverflowContainer(mKeyguardIconOverflowContainer);
    }

    @Override
    protected void onDensityOrFontScaleChanged() {
        super.onDensityOrFontScaleChanged();
        mScrimController.onDensityOrFontScaleChanged();
        mStatusBarView.onDensityOrFontScaleChanged();
        if (mBrightnessMirrorController != null) {
            mBrightnessMirrorController.onDensityOrFontScaleChanged();
        }
        inflateSignalClusters();
        mIconController.onDensityOrFontScaleChanged();
        inflateDismissView();
        updateClearAll();
        inflateEmptyShadeView();
        updateEmptyShadeView();
        inflateOverflowContainer();
        mStatusBarKeyguardViewManager.onDensityOrFontScaleChanged();
        mUserInfoController.onDensityOrFontScaleChanged();
        if (mUserSwitcherController != null) {
            mUserSwitcherController.onDensityOrFontScaleChanged();
        }
        if (mKeyguardUserSwitcher != null) {
            mKeyguardUserSwitcher.onDensityOrFontScaleChanged();
        }
    }

    private void inflateSignalClusters() {
        SignalClusterView signalClusterView = reinflateSignalCluster(mStatusBarView);
        mIconController.setSignalCluster(signalClusterView);
        //add for statusbar inverse. prize-linkh-20150903
        if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR) {
            /*PRIZE-add for bugid:52545-liufan-2018-03-13-start*/
            signalClusterView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        signalClusterView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        signalClusterView.setIgnoreStatusBarStyleChanged(false);
                        signalClusterView.onStatusBarStyleChanged(mCurStatusBarStyle);
                        mNetworkController.refreshCurStatusBarStyle(mCurStatusBarStyle);
                    }
                });
            /*PRIZE-add for bugid:52545-liufan-2018-03-13-end*/
        }
        //end...
        reinflateSignalCluster(mKeyguardStatusBar);
        keyguardSignalClusterView = null;
        refreshKeyguardStatusBar(textColor);
        //Add for backgroud_reflect-luyingying-2017-09-09-Start*/
        //mKeyguardStatusBar.refreshObject();
        //Add for backgroud_reflect-luyingying-2017-09-09-end*/
    }

    private SignalClusterView reinflateSignalCluster(View view) {
        SignalClusterView signalCluster =
                (SignalClusterView) view.findViewById(R.id.signal_cluster);
        if (signalCluster != null) {
            ViewParent parent = signalCluster.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup viewParent = (ViewGroup) parent;
                int index = viewParent.indexOfChild(signalCluster);
                viewParent.removeView(signalCluster);
                SignalClusterView newCluster = (SignalClusterView) LayoutInflater.from(mContext)
                        .inflate(R.layout.signal_cluster_view, viewParent, false);
                ViewGroup.MarginLayoutParams layoutParams =
                        (ViewGroup.MarginLayoutParams) viewParent.getLayoutParams();
                layoutParams.setMarginsRelative(
                        mContext.getResources().getDimensionPixelSize(
                                R.dimen.signal_cluster_margin_start),
                        0, 0, 0);
                newCluster.setLayoutParams(layoutParams);
                newCluster.setSecurityController(mSecurityController);
                newCluster.setNetworkController(mNetworkController);
                viewParent.addView(newCluster, index);
                return newCluster;
            }
            return signalCluster;
        }
        return null;
    }

    private void inflateEmptyShadeView() {
        mEmptyShadeView = (EmptyShadeView) LayoutInflater.from(mContext).inflate(
                R.layout.status_bar_no_notifications, mStackScroller, false);
        mStackScroller.setEmptyShadeView(mEmptyShadeView);
    }

    private void inflateDismissView() {
        mDismissView = (DismissView) LayoutInflater.from(mContext).inflate(
                R.layout.status_bar_notification_dismiss_all, mStackScroller, false);
        mDismissView.setOnButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MetricsLogger.action(mContext, MetricsEvent.ACTION_DISMISS_ALL_NOTES);
                clearAllNotifications();
            }
        });
        mStackScroller.setDismissView(mDismissView);
    }

    protected void createUserSwitcher() {
        mKeyguardUserSwitcher = new KeyguardUserSwitcher(mContext,
                (ViewStub) mStatusBarWindow.findViewById(R.id.keyguard_user_switcher),
                mKeyguardStatusBar, mNotificationPanel, mUserSwitcherController);
    }

    protected void inflateStatusBarWindow(Context context) {
        mStatusBarWindow = (StatusBarWindowView) View.inflate(context,
                R.layout.super_status_bar, null);
    }

    protected void createNavigationBarView(Context context) {
        inflateNavigationBarView(context);
        mNavigationBarView.setDisabledFlags(mDisabled1);
        mNavigationBarView.setComponents(mRecents, getComponent(Divider.class));
        mNavigationBarView.setOnVerticalChangedListener(
                new NavigationBarView.OnVerticalChangedListener() {
            @Override
            public void onVerticalChanged(boolean isVertical) {
                if (mAssistManager != null) {
                    mAssistManager.onConfigurationChanged();
                }
                mNotificationPanel.setQsScrimEnabled(!isVertical);
            }
        });
        mNavigationBarView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                checkUserAutohide(v, event);
                return false;
            }});

        /* Dynamically hiding nav bar feature. prize-linkh-20161115 */
        if(PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR) {
             //The navbar is always visibile in default, so we
            // set 1 to SetingsProvider.
            Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.PRIZE_NAVBAR_STATE, 1);
        } //END...         

        /* Dynamically changing Recents function feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_TREAT_RECENTS_AS_MENU) {
            mEnableTreatRecentsAsMenu = Settings.System.getInt(mContext.getContentResolver(),                 
                                    Settings.System.PRIZE_TREAT_RECENTS_AS_MENU, 0) == 1;
        } //END...
    }
    //prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar start
    private class GestureIndicatorManager{
    	private LinearLayout mGestureIndicatorRot0View = null;
    	private LinearLayout mGestureIndicatorRot90View = null;
    	private LinearLayout mGestureIndicatorRot180View = null;
    	private LinearLayout mGestureIndicatorRot270View = null;
    	Context mContext;
    	int currentOrientation;
    	WindowManager mWindowManager;
    	private LinearLayout activeView = null;
    	private Animation animation = null;
    	private Animation rot0Animation = null;
    	private Animation rot90Animation = null;
    	private Animation rot180Animation = null;
    	private Animation rot270Animation = null;
    	//private int curIndicatorStyle = mCurStatusBarStyle;
    	private ImageView leftImageView = null;
    	private ImageView centerImageView = null;
    	private ImageView rightImageView = null;
    	private int displayHeight;
    	private int displayWidth;
    	private final static int VIEW_HEIGHT = 60;
    	private final static int VIEW_WIDTH = 843;
    	private final static int NORMAL_ALPHA = 100;
    	private final static int LIGHT_ALPHA = 255;
    	private final static int DIM_DELAY_TIME = 2500;


    	public GestureIndicatorManager(Context context){
    		mContext = context;
    		currentOrientation = mContext.getResources().getConfiguration().orientation;
    		mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    		rot0Animation = AnimationUtils.loadAnimation(mContext, R.anim.prize_swipe_up_gesture_animation_rot0);
    		rot90Animation = AnimationUtils.loadAnimation(mContext, R.anim.prize_swipe_up_gesture_animation_rot90);
    		rot180Animation = AnimationUtils.loadAnimation(mContext, R.anim.prize_swipe_up_gesture_animation_rot180);
    		rot270Animation = AnimationUtils.loadAnimation(mContext, R.anim.prize_swipe_up_gesture_animation_rot270);
    		animation = rot0Animation;
    		DisplayMetrics mDisplayMetrics = mContext.getResources().getDisplayMetrics();
            mWindowManager.getDefaultDisplay().getRealMetrics(mDisplayMetrics);
            displayHeight = mDisplayMetrics.heightPixels;
            displayWidth = mDisplayMetrics.widthPixels;
    		initView();
    	}

    	public void initView(){
			mGestureIndicatorRot0View = (LinearLayout) View.inflate(
	            mContext, R.layout.gesture_indicator_rot0, null);
			mGestureIndicatorRot90View = (LinearLayout) View.inflate(
	            mContext, R.layout.gesture_indicator_rot90, null);
			mGestureIndicatorRot180View = (LinearLayout) View.inflate(
	            mContext, R.layout.gesture_indicator_rot180, null);
			mGestureIndicatorRot270View = (LinearLayout) View.inflate(
	            mContext, R.layout.gesture_indicator_rot270, null);
    	}

    	public void addView(){
			mWindowManager.addView(mGestureIndicatorRot0View, getGestureIndicatorParams(Surface.ROTATION_0));
			mWindowManager.addView(mGestureIndicatorRot90View, getGestureIndicatorParams(Surface.ROTATION_90));
			mWindowManager.addView(mGestureIndicatorRot180View, getGestureIndicatorParams(Surface.ROTATION_180));
			mWindowManager.addView(mGestureIndicatorRot270View, getGestureIndicatorParams(Surface.ROTATION_270));
			clearView();
			updateActiveView(mGestureIndicatorRot0View);
			//updateViewStyle(curIndicatorStyle);
			showView();
    	}

    	private void updateActiveView(LinearLayout view){
    		activeView = view;
    		leftImageView = (ImageView)activeView.findViewById(R.id.left_gesture_indicator);
    		centerImageView = (ImageView)activeView.findViewById(R.id.center_gesture_indicator);
    		rightImageView = (ImageView)activeView.findViewById(R.id.right_gesture_indicator);
    	}

    	public void updateView(int rot){
    		if(!isEnableGestureNavigation())
    			return;
    		clearView();
			switch(rot){
				case Surface.ROTATION_0:
					animation = rot0Animation;
					updateActiveView(mGestureIndicatorRot0View);
					break;
				case Surface.ROTATION_90:
					animation = rot90Animation;
					updateActiveView(mGestureIndicatorRot90View);
					break;	
				case Surface.ROTATION_180:	
					animation = rot180Animation;
					updateActiveView(mGestureIndicatorRot180View);
					break;
				case Surface.ROTATION_270:
					animation = rot270Animation;
					updateActiveView(mGestureIndicatorRot270View);
					break;
			}
			showView();
    	}

    	/*public void updateViewStyle(int style){
    		curIndicatorStyle = style;
    		if(activeView == mGestureIndicatorRot0View || activeView == mGestureIndicatorRot180View){
    			setPortraitImageDrawable(style);
    		}else if(activeView == mGestureIndicatorRot90View || activeView == mGestureIndicatorRot270View){
    			setLandscapeImageDrawable(style);
    		}
    	}*/

    	/*public void setPortraitImageDrawable(int style){
		    if(style == STYLE_WHITE){
    			leftImageView.setImageDrawable(mContext.getDrawable(R.drawable.left_gesture_indicator_white_normal));
    			centerImageView.setImageDrawable(mContext.getDrawable(R.drawable.center_gesture_indicator_white_normal));
    			rightImageView.setImageDrawable(mContext.getDrawable(R.drawable.right_gesture_indicator_white_normal));    				
			}else if(style == STYLE_GRAY){
				leftImageView.setImageDrawable(mContext.getDrawable(R.drawable.left_gesture_indicator_gray_normal));
    			centerImageView.setImageDrawable(mContext.getDrawable(R.drawable.center_gesture_indicator_gray_normal));
    			rightImageView.setImageDrawable(mContext.getDrawable(R.drawable.right_gesture_indicator_gray_normal));
			}
    	}


    	public void setLandscapeImageDrawable(int style){
    		if(style == STYLE_WHITE){
    			leftImageView.setImageDrawable(mContext.getDrawable(R.drawable.left_gesture_indicator_white_normal_land));
    			centerImageView.setImageDrawable(mContext.getDrawable(R.drawable.center_gesture_indicator_white_normal_land));
    			rightImageView.setImageDrawable(mContext.getDrawable(R.drawable.right_gesture_indicator_white_normal_land));    				
			}else if(style == STYLE_GRAY){
				leftImageView.setImageDrawable(mContext.getDrawable(R.drawable.left_gesture_indicator_gray_normal_land));
    			centerImageView.setImageDrawable(mContext.getDrawable(R.drawable.center_gesture_indicator_gray_normal_land));
    			rightImageView.setImageDrawable(mContext.getDrawable(R.drawable.right_gesture_indicator_gray_normal_land));
			}
    	}*/

    	public void hideView(){
    		if(activeView != null)
    			activeView.setVisibility(View.INVISIBLE); 
    	}

    	public void showView(){
    		if(!isEnableGestureNavigation())
    			return;
    		if(!needHideGestureIndicator() && activeView != null){
    			animation.cancel(); 
    			setActiveViewImageAlpha(NORMAL_ALPHA);
    			/*if(curIndicatorStyle != mCurStatusBarStyle){
    				updateViewStyle(mCurStatusBarStyle);
    			}*/
    			activeView.setVisibility(View.VISIBLE);    			
    		}
    	}

    	public void clearView(){
    		animation.cancel(); 
    		mGestureIndicatorRot0View.setVisibility(View.INVISIBLE);
    		mGestureIndicatorRot90View.setVisibility(View.INVISIBLE);
			mGestureIndicatorRot180View.setVisibility(View.INVISIBLE);
			mGestureIndicatorRot270View.setVisibility(View.INVISIBLE);
    	}

    	public void setActiveViewImageAlpha(int alpha){
    		if(activeView == null){
    			return;
    		}
    		leftImageView.setImageAlpha(alpha);
    		centerImageView.setImageAlpha(alpha);
    		rightImageView.setImageAlpha(alpha);
    	}

    	public void onGestureComplete(int position){
    		lightView();
    		performHapticFeedback();
    		switch(position)
    		{
    			case LEFT_BOTTOM:
    				leftImageView.startAnimation(animation);
    				break;
    			case CENTER_BOTTOM:
    				centerImageView.startAnimation(animation);
    				break;
    			case RIGHT_BOTTOM:
    				rightImageView.startAnimation(animation);
    				break;
    		}
    	}

    	Runnable DiwViewRunable = new Runnable(){
        	@Override
	        public void run() {
	            mGestureIndicatorManager.dimView();
	        }
    	};
    	
    	public void lightView(){
    		if(DiwViewRunable != null)
    			mHandler.removeCallbacks(DiwViewRunable);
    		setActiveViewImageAlpha(LIGHT_ALPHA); 
    		mHandler.postDelayed(DiwViewRunable,DIM_DELAY_TIME);
    	}

		public void dimView(){
    		setActiveViewImageAlpha(NORMAL_ALPHA);
    	}    	

    	public void performHapticFeedback(){
    		if(activeView != null)
    		activeView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    	}

    	protected WindowManager.LayoutParams getGestureIndicatorParams(int rot){
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
						0
						| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
						| WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
					PixelFormat.TRANSLUCENT);
			if(Surface.ROTATION_0 == rot){
					lp.width = VIEW_WIDTH; 
					lp.height = VIEW_HEIGHT;
					lp.gravity = Gravity.CENTER;
					//lp.x = 0;
					lp.y = displayHeight;
					lp.setTitle("Rot0");
			}else if(Surface.ROTATION_90 == rot){
					lp.width = VIEW_HEIGHT; 
					lp.height = VIEW_WIDTH;
					lp.gravity = Gravity.CENTER;
					lp.x = displayHeight;
					//lp.y = 0;
					lp.setTitle("Rot90");
			}else if(Surface.ROTATION_180 == rot){
					lp.width = VIEW_WIDTH;
					lp.height = VIEW_HEIGHT;
					lp.gravity = Gravity.CENTER;
					//lp.x = 115;
					lp.y = -displayHeight;
					lp.setTitle("Rot180");
			}else if(Surface.ROTATION_270 == rot){
					lp.width = VIEW_HEIGHT; 
					lp.height = VIEW_WIDTH;
					lp.gravity = Gravity.CENTER;
					lp.x = -displayHeight;
					//lp.y = 698;
					lp.setTitle("Rot270");
			}
			return lp;
		}
    }

    private boolean needHideGestureIndicator() {
    	//add for keyguardView hide the gesture indicator
       return Settings.System.getInt(mContext.getContentResolver(), 
                               Settings.System.PRIZE_HIDE_SWIPE_UP_GESTURE_INDICATOR, 0) == 1;
   }
    
	public boolean isEnableGestureNavigation(){
		return Settings.System.getInt(mContext.getContentResolver(), 
                               Settings.System.PRIZE_SWIPE_UP_GESTURE_STATE, 0) == 1;
	}

	private boolean isKeyguardHostWindow(){
		if(mStatusBarKeyguardViewManager != null){
			return (mStatusBarKeyguardViewManager.isShowing() && !isOccluded() 
				&& !mStatusBarKeyguardViewManager.isBouncerShowing());
		}
		return false;
	}

	public void showGestureIndicator(){
		mGestureIndicatorManager.showView();
	}

	public void hideGestureIndicator(){
		mGestureIndicatorManager.hideView();
	}

	//prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar end

    protected void inflateNavigationBarView(Context context) {
        mNavigationBarView = (NavigationBarView) View.inflate(
                context, R.layout.navigation_bar, null);
    }

    protected void initSignalCluster(View containerView) {
        SignalClusterView signalCluster =
                (SignalClusterView) containerView.findViewById(R.id.signal_cluster);
        if (signalCluster != null) {
            signalCluster.setSecurityController(mSecurityController);
            signalCluster.setNetworkController(mNetworkController);
        }

        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(OPEN_LIUHAI_SCREEN2){
            if(containerView instanceof PhoneStatusBarView){
                LiuHaiSignalView2 sc =
                        (LiuHaiSignalView2) containerView.findViewById(R.id.prize_liuhai_signal_cluster2);
                if (sc != null) {
                    sc.setSecurityController(mSecurityController);
                    sc.setNetworkController(mNetworkController);
                    //mNetworkController.addSignalCallback(sc);
                    mIconController.initLiuHaiStatusBarIconListener(mLiuHaiIconControllerList2.get(0));//init
                    sc.setLiuHaiStatusBarIconListener(mLiuHaiIconControllerList2.get(0));
                }
            }

            if(containerView instanceof KeyguardStatusBarView){
                LiuHaiSignalView2 sc =
                    (LiuHaiSignalView2) containerView.findViewById(R.id.prize_liuhai_signal_cluster2);
                if (sc != null) {
                    sc.setSecurityController(mSecurityController);
                    sc.setNetworkController(mNetworkController);
                    //mNetworkController.addSignalCallback(sc);
                    mIconController.initLiuHaiKeyguardIconListener(mLiuHaiIconControllerList2.get(1));//init
                    sc.setLiuHaiStatusBarIconListener(mLiuHaiIconControllerList2.get(1));
                }
            }

            if(containerView instanceof BaseStatusBarHeader){
                mIconController.setStatusBarHeaderView(mHeader);
                LiuHaiSignalView2 sc =(LiuHaiSignalView2) containerView.findViewById(R.id.prize_liuhai_signal_cluster2);
                if (sc != null) {
                    sc.setSecurityController(mSecurityController);
                    sc.setNetworkController(mNetworkController);
                    //mNetworkController.addSignalCallback(sc);
                    mIconController.initLiuHaiHeaderIconListener(mLiuHaiIconControllerList2.get(2));//init
                    sc.setLiuHaiStatusBarIconListener(mLiuHaiIconControllerList2.get(2));
                }
            }
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/

        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(OPEN_LIUHAI_SCREEN && !OPEN_LIUHAI_SCREEN2){
            if(containerView instanceof PhoneStatusBarView){
                LiuHaiSignalClusterView sc =
                        (LiuHaiSignalClusterView) containerView.findViewById(R.id.prize_liuhai_signal_cluster);
                if (sc != null) {
                    sc.setSecurityController(mSecurityController);
                    sc.setNetworkController(mNetworkController);
                    sc.setLiuHaiStatusBarIconController(mLiuHaiIconControllerList.get(0));
                }
                if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR) {
                    sc.setIgnoreStatusBarStyleChanged(false);
                }
            }
            if(containerView instanceof KeyguardStatusBarView){
                LiuHaiSignalClusterView sc =
                        (LiuHaiSignalClusterView) containerView.findViewById(R.id.prize_liuhai_signal_cluster);
                if (sc != null) {
                    sc.setSecurityController(mSecurityController);
                    sc.setNetworkController(mNetworkController);
                    sc.setLiuHaiStatusBarIconController(mLiuHaiIconControllerList.get(1));
                }
            }
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
    }

    /*PRIZE-add for network speed-liufan-2016-09-20-start*/
    private String NETSTATE_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public final static boolean NETWORK_SPEED_GATE = true;
    private boolean isNetworkConnect;
    private TextView mNetworkSpeedTxt;
    private long lastTotalRxBytes;
    private NetworkSpeedObserver mNetworkSpeedObserver;
    Runnable networkSpeedRunable = new Runnable(){
        @Override
        public void run() {
            showNetworkSpeedWhenNetworking(mContext);
        }
    };
    private void showNetworkSpeedWhenNetworking(Context context){
        if(!isShowNetworkSpeed(context)){
            lastTotalRxBytes = 0;
            mNetworkSpeedTxt.setText("");
            mNetworkSpeedTxt.setVisibility(View.GONE);
            /*prize add by xiarui for Bug#43623 2017-12-14 start*/
            if (mKeyguardStatusBar != null) {
                mKeyguardStatusBar.updateNetworkSpeed(View.GONE, "");
            }
            /*prize add by xiarui for Bug#43623 2017-12-14 end*/

            /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
            if(OPEN_LIUHAI_SCREEN){
                showNetworkSpeedOnLiuHaiRight(View.GONE, "");
            }
            /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
            /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
            if(OPEN_LIUHAI_SCREEN2){
                showNetworkSpeedOnLiuHai2(View.GONE, "");
            }
            /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
            return ;
        }
        int space = 4;
        if(isNetworkAvailable(context)){
            if(lastTotalRxBytes != 0){
                long curTotalRxBytes = TrafficStats.getTotalRxBytes();
                String speed = Formatter.formatFileSize(context, (curTotalRxBytes - lastTotalRxBytes)/space) + "/s";
                if((OPEN_LIUHAI_SCREEN || OPEN_LIUHAI_SCREEN2) && speed.indexOf(".") < 0){
                    String[] arr = speed.split(" ");
                    if(arr.length > 1){
                        if(arr[0].length() < 2){
                            arr[0] += ".0 ";
                            String s = "";
                            for(int i = 0; i < arr.length; i++){
                                s += arr[i];
                            }
                            speed = s;
                        }
                    }
                    arr = null;
                }
                if(speed.indexOf("位元組") >= 0) speed = speed.replace("位元組","B");
                mNetworkSpeedTxt.setText(speed);
                lastTotalRxBytes = curTotalRxBytes;
            } else{
                lastTotalRxBytes = TrafficStats.getTotalRxBytes();
                String speed = Formatter.formatFileSize(context, 0) + "/s";
                if(OPEN_LIUHAI_SCREEN || OPEN_LIUHAI_SCREEN2){
                    speed = "0.0 B/s";
                }
                if(speed.indexOf("位元組") >= 0) speed = speed.replace("位元組","B");
                mNetworkSpeedTxt.setText(speed);
            }
            if(mNetworkSpeedTxt.getVisibility() != View.VISIBLE) mNetworkSpeedTxt.setVisibility(View.VISIBLE);
        } else {
            lastTotalRxBytes = 0;
            mNetworkSpeedTxt.setText("");
            mNetworkSpeedTxt.setVisibility(View.GONE);
        }

        /*prize add by xiarui for Bug#43623 2017-12-14 start*/
        if (mKeyguardStatusBar != null && mNetworkSpeedTxt != null) {
            mKeyguardStatusBar.updateNetworkSpeed(mNetworkSpeedTxt.getVisibility(), mNetworkSpeedTxt.getText());
        }
        /*prize add by xiarui for Bug#43623 2017-12-14 end*/

        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(OPEN_LIUHAI_SCREEN){
            showNetworkSpeedOnLiuHaiRight(mNetworkSpeedTxt.getVisibility(), mNetworkSpeedTxt.getText().toString().toUpperCase());
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(OPEN_LIUHAI_SCREEN2){
            showNetworkSpeedOnLiuHai2(mNetworkSpeedTxt.getVisibility(), mNetworkSpeedTxt.getText().toString().toUpperCase());
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
        mHandler.postDelayed(networkSpeedRunable,space * 1000);
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isShowNetworkSpeed(Context context){
        boolean isShow = Settings.System.getInt(context.getContentResolver(),
            Settings.System.PRIZE_REAL_TIME_NETWORK_SPEED_SWITCH, 0) == 1 ? true : false;
        return isShow;
    }

    private class NetworkSpeedObserver extends ContentObserver {
        public NetworkSpeedObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (selfChange) return;
            mHandler.removeCallbacks(networkSpeedRunable);
            mHandler.post(networkSpeedRunable);
        }
        public void startObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.PRIZE_REAL_TIME_NETWORK_SPEED_SWITCH),
                    false, this, UserHandle.USER_ALL);       
        }
        public void stopObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
        } 
    }

    private BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!isShowNetworkSpeed(mContext)){
                return;
            }
            String action = intent.getAction();
            if(action.equalsIgnoreCase(NETSTATE_CHANGE_ACTION)){
                boolean isConnect = isNetworkAvailable(mContext);
                if(isNetworkConnect != isConnect){
                    isNetworkConnect = isConnect;
                    mHandler.removeCallbacks(networkSpeedRunable);
                    mHandler.post(networkSpeedRunable);
                }
            }
        }
    };
    /*PRIZE-add for network speed-liufan-2016-09-20-end*/

    public void refreshSystemUIByThemeValue(){
        updateMediaMetaData(true,false);
        IS_USE_HAOKAN = true;
        refreshHaoKanState();
        mNotificationPanel.requestLayout();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(LoadIconUtils.THEME_EXE_ACTION)){
                String themePath = intent.getStringExtra(LoadIconUtils.THEME_EXE_PATH_KEY);
                if(themePath.equals(LoadIconUtils.path)){
                    return;
                }
                LoadIconUtils.path = themePath;
                LoadIconUtils.saveThemePath(context,themePath);
                refreshSystemUIByThemeValue();
            }
        }
    };
    
    //add by haokan-liufan-2016-10-11-start
    public static boolean IS_USE_HAOKAN = false;
    private int isFristRun = 0;
    public boolean isUseHaoKan(){
        return PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW && IS_USE_HAOKAN ;//&& mScreenView != null && mScreenView.getVisibility() != View.GONE;
    }
    public void refreshHaoKanState(){
        if(!PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW){
            //if(NotificationPanelView.IS_ShowHaoKanView) mNotificationPanel.setShowHaokanFunction(false);
            mNotificationPanel.setShowHaokanFunction(false);
            return;
        }
        if(IS_USE_HAOKAN){
            if(mState != StatusBarState.SHADE){
                //if(!NotificationPanelView.IS_ShowHaoKanView) mNotificationPanel.setShowHaokanFunction(true);
                mNotificationPanel.setShowHaokanFunction(true);
            } else {
                //if(NotificationPanelView.IS_ShowHaoKanView) mNotificationPanel.setShowHaokanFunction(false);
                mNotificationPanel.setShowHaokanFunction(false);
                NotificationPanelView.debugMagazine("mState is StatusBarState.SHADE, don't show magazine view");
            }
        } else {
            //if(NotificationPanelView.IS_ShowHaoKanView) mNotificationPanel.setShowHaokanFunction(false);
            mNotificationPanel.setShowHaokanFunction(false);
        }
    }

    public void setHaoKanBlurLayout(LinearLayout blurlayout){
        if(mBlurBack!=null){
            FrameLayout.LayoutParams blurBackParams = (FrameLayout.LayoutParams) mBlurBack.getLayoutParams();
            blurlayout.setLayoutParams(blurBackParams);
        }
        if (VersionControl.CUR_VERSION == VersionControl.BLUR_BG_VER) {
            if(mScrimController!=null) mScrimController.setLockscreenBlurLayout(blurlayout);
        }
        mBlurBack.setBackground(null);
        mBlurBack.setVisibility(View.GONE);
        mBlurBack = blurlayout;
    }

    private MagazineGateObserver mMagazineGateObserver = null;
    private class MagazineGateObserver extends ContentObserver {
        private final Uri MAGAZINE_URI =
                Settings.System.getUriFor(Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH);
        public MagazineGateObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (selfChange) return;
            if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW){
                if(isOpenMagazine()){
                    NotificationPanelView.debugMagazine("MagazineGateObserver open magazine");
                    if(mNotificationPanel != null){
                        mNotificationPanel.initHaoKanView();
                        if(mState == StatusBarState.KEYGUARD && !mNotificationPanel.isHaoKanViewShow()){
                            showHaoKanView();
                            mNotificationPanel.onBackPressedForHaoKan();
                        }
                    }
                }else{
                    NotificationPanelView.debugMagazine("MagazineGateObserver close magazine");
                }
            }
        }

        public void startObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                    MAGAZINE_URI,
                    false, this, UserHandle.USER_ALL);

        }

        public void stopObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
        }
    }

    PrizeCrashHandler crashHandler;
    public void startListenErrorOccured(){
        if(crashHandler != null){
            return;
        }
        NotificationPanelView.debugMagazine("startListenErrorOccured");
        crashHandler = PrizeCrashHandler.getInstance();
        crashHandler.init(mContext);
        crashHandler.setCrashCallBack(mCrashCallBack);
    }

    PrizeCrashHandler.CrashCallBack mCrashCallBack = new PrizeCrashHandler.CrashCallBack(){
        @Override
        public void onCrashOccured(Throwable ex) {
            notifyAnErrorOccured(ex);
        }
    };

    public void notifyAnErrorOccured(Throwable ex){
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        printWriter.close();
        String result = writer.toString(); 
        NotificationPanelView.debugMagazine("systemui crash cause: " + result);

        String mClass = mNotificationPanel.getMagazineViewClass();
        if(result != null && mClass != null && result.contains(mClass)){
            NotificationPanelView.debugMagazine("An error has occurred with magazine apk mState : " + mState);
            IS_USE_HAOKAN = false;
            Settings.System.putInt(mContext.getContentResolver(),Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH, 0);
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private int magazineInverse = 1;
    public void notifyInverseNumToSystemui(int inverse){
        Log.d("BooLor","notifyInverseNumToSystemui = " + inverse);
        magazineInverse = inverse == 1 ? 1 : 0;
        Log.d("BooLor","notifyInverseNumToSystemui = " + magazineInverse);
        setTextColor();
    }

    public boolean isShouldIgnoreInverse(){
        int N = mNotificationData.getActiveNotifications().size();
        boolean isShowBlur = isShowLimitByNotifications(N);
        NotificationPanelView.debugMagazine("isShouldIgnoreInverse result = " + !isShowBlur);
        return !isShowBlur;
    }
    //add by haokan-liufan-2016-10-11-end

    /*PRIZE-blur background- liufan-2015-06-08-start*/
    private int CRIM_COLOR = 0xe524373d;
    private Bitmap mWallPapaerBitmap;
    private LinearLayout mNotificationBg;
    private NotificationHeaderLayout mNotificationHeaderBg;
    private QSDetail mQSDetail;
    private boolean isShowBg = false;
    private boolean isShowBlurBg = false;
    
    /*PRIZE-add for alpha background,bugid: 28859- liufan-2017-03-02-start*/
    private Bitmap mWallPapaerBitmapBack;
    private ImmediateSetBgRunnable mImmediateSetBgRunnable;
    
    class ImmediateSetBgRunnable implements Runnable{
        
        private BlurBackgroundRunnable mBlurBackgroundRunnable;
        
        public ImmediateSetBgRunnable(BlurBackgroundRunnable runnable){
            mBlurBackgroundRunnable = runnable;
        }
        
        @Override
        public void run() {
            if(mBlurBackgroundRunnable.isNeedShowBgImmediately){
                if(mWallPapaerBitmapBack!=null){
                    Log.e("liufan","setNotificationRunnable show bg in 500ms");
                    mWallPapaerBitmap = mWallPapaerBitmapBack;
                    mBlurBackgroundRunnable.isNeedShowBgImmediately = false;
                    isShowBlurBg = false;
                    setNotificationBackground();
                } else {
                    Log.e("liufan","setNotificationRunnable mWallPapaerBitmapBack is null");
                }
            }
        }
    }
    
    class BlurBackgroundRunnable implements Runnable{

        private int flag = -1;
        public boolean isNeedShowBgImmediately = true;
        
        public BlurBackgroundRunnable(int value){
            flag = value;
        }
    
        @Override
        public void run() {
            //update scrim_bg-2017-12-18-liufan
            //mWallPapaerBitmap = blur(null);//flag == 1 ? getBlurBackground() : getBlurWallpaper();
            mWallPapaerBitmap = flag == 1 ? getBlurBackground() : getBlurWallpaper();
            Log.e("liufan","BlurBackgroundRunnable mWallPapaerBitmap : "+mWallPapaerBitmap);
            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            tileHandler.postDelayed(new Runnable(){
                @Override
                public void run() {
                    Log.e("liufan","BlurBackgroundRunnable isNeedShowBgImmediately: "+isNeedShowBgImmediately);
                    Log.e("liufan","BlurBackgroundRunnable isShowBg: "+isShowBg);
                    if(!isNeedShowBgImmediately){
                        return ;
                    }
                    isNeedShowBgImmediately = false;
                    if(mImmediateSetBgRunnable!=null){
                        mHandler.removeCallbacks(mImmediateSetBgRunnable);
                        mImmediateSetBgRunnable = null;
                    }
                    if(isShowBg){
                        Log.e("liufan","BlurBackgroundRunnable setNotificationBackground-1");
                        setNotificationBackground();
                    }
                    isShowBlurBg = false;
                }
            },0);
        }
    }
    /*PRIZE-add for alpha background,bugid: 28859- liufan-2017-03-02-end*/
    
    /**
    * Method Description：show the blur background (Screenshots for the background)
    * update for alpha background,bugid: 28859- liufan-2017-03-02
    */
    public void showBlurBackground(){
        Log.e("liufan","showBlurBackground-0 : "+isShowBlurBg);
        if(isShowBlurBg){
            return;
        }
        isShowBg = true;
        isShowBlurBg = true;
        BlurBackgroundRunnable blurBackgroundRunnable = new BlurBackgroundRunnable(1);
        if(!BLUR_BG_CONTROL){
            mWallPapaerBitmap = getHalfAlphaColorBackground(BACK_COLOR);
            if(isShowBg && blurBackgroundRunnable.isNeedShowBgImmediately){
                setNotificationBackground();
            }
            isShowBlurBg = false;
            blurBackgroundRunnable.isNeedShowBgImmediately = false;
            return;
        }
        if(mImmediateSetBgRunnable!=null){
            mHandler.removeCallbacks(mImmediateSetBgRunnable);
            mImmediateSetBgRunnable = null;
        }
        mImmediateSetBgRunnable = new ImmediateSetBgRunnable(blurBackgroundRunnable);
        mHandler.postDelayed(mImmediateSetBgRunnable,500);
        AsyncTask.THREAD_POOL_EXECUTOR.execute(blurBackgroundRunnable);
    }
    
    /**
    * Method Description：show the blur background(the wallpaper for the background)
    * update for alpha background,bugid: 28859- liufan-2017-03-02
    */
    public void showBlurWallPaper(){
        Log.e("liufan","showBlurWallPaper-0 : "+isShowBlurBg);
        if(isShowBlurBg){
            return;
        }
        isShowBg = true;
        isShowBlurBg = true;
        BlurBackgroundRunnable blurBackgroundRunnable = new BlurBackgroundRunnable(0);
        if(!BLUR_BG_CONTROL){
            mWallPapaerBitmap = getHalfAlphaColorBackground(BACK_COLOR);
            if(isShowBg && blurBackgroundRunnable.isNeedShowBgImmediately){
                setNotificationBackground();
            }
            isShowBlurBg = false;
            blurBackgroundRunnable.isNeedShowBgImmediately = false;
            return;
        }
        if(mImmediateSetBgRunnable!=null){
            mHandler.removeCallbacks(mImmediateSetBgRunnable);
            mImmediateSetBgRunnable = null;
        }
        mImmediateSetBgRunnable = new ImmediateSetBgRunnable(blurBackgroundRunnable);
        mHandler.postDelayed(mImmediateSetBgRunnable,500);
        AsyncTask.THREAD_POOL_EXECUTOR.execute(blurBackgroundRunnable);
    }

    /**
    * Method Description：dismiss the blur background
    */
    public void cancelNotificationBackground(){
        Log.e("liufan","cancelNotificationBackground-0");
        /*PRIZE-background alpha,bugid: 21404- liufan-2016-09-14-start*/
        if(!mNotificationPanel.isFullyCollapsed() && mState == StatusBarState.SHADE){
            Log.d(TAG,"not allow to cancelNotificationBackground");
            return ;
        }
        /*PRIZE-background alpha,bugid: 21404- liufan-2016-09-14-end*/
        if(isShowBlurBg){
            return;
        }
        isShowBg = false;
        Log.e("liufan","cancelNotificationBackground-1");
        if(mQSDetail!=null) mQSDetail.setBg(null);
        mNotificationBg.setBackground(null);
        mNotificationBg.setAlpha(0f);
        mNotificationHeaderBg.setBg(null);
        if(mWallPapaerBitmap != null){
            mWallPapaerBitmap.recycle();
            mWallPapaerBitmap = null;
        }
    }

    private ValueAnimator dismissNotificationAnimation;
    public ValueAnimator getDismissNotificationAnimation(){
        return dismissNotificationAnimation;
    }
    public float getNotificationBgAlpha(){
        return mNotificationBg.getAlpha();
    }
    public void dismissNotificationBackgroundAnimation(boolean anim, final Runnable r){
        if(dismissNotificationAnimation!=null || mNotificationBg.getAlpha() == 0){
            return;
        }
        if(showNotificationAnimation != null){
            showNotificationAnimation.cancel();
        }
        if(anim){
            dismissNotificationAnimation = ValueAnimator.ofFloat(mNotificationBg.getAlpha(), 0f);
            int duration = mState == StatusBarState.SHADE ? 500 : 0;
            dismissNotificationAnimation.setDuration(duration);
            dismissNotificationAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mNotificationBg.setAlpha((Float) animation.getAnimatedValue());
                    //mNotificationBg.setTranslationY((Float) animation.getAnimatedValue() * mDisplayMetrics.heightPixels - mDisplayMetrics.heightPixels);
                }
            });
            dismissNotificationAnimation.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dismissNotificationAnimation = null;
                    cancelNotificationBackground();
                    if(r!=null){
                        r.run();
                    }
                }

            });
            dismissNotificationAnimation.start();
        }else{
            cancelNotificationBackground();
        }
    }
    
    private ValueAnimator showNotificationAnimation;
    /**
    * Method Description：set blur background with animation
    */
    private void setNotificationBackground(){
        Log.e("liufan","setNotificationBackground mWallPapaerBitmap: "+mWallPapaerBitmap);
        if(mWallPapaerBitmap!=null){
            BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), mWallPapaerBitmap);
            mNotificationBg.setAlpha(0);
            mNotificationBg.setBackground(bd);
        }
        if(showNotificationAnimation!=null || mNotificationBg.getAlpha() == 1){
            return;
        }
        if(dismissNotificationAnimation != null){
            dismissNotificationAnimation.cancel();
        }
        boolean isExpanded = mNotificationPanel.getExpandedHeight() == mNotificationPanel.getMaxPanelHeight() ? true : false;
        if(STATUS_BAR_DROPDOWN_STYLE && !isExpanded){
            mNotificationBg.setAlpha(mNotificationPanel.getExpandedFraction());
        }else{
            //mNotificationBg.setTranslationY(0);
            showNotificationAnimation = ValueAnimator.ofFloat(0f, 1f);
            int duration = (mState == StatusBarState.SHADE) ? 500 : 0;
            if(isAnimateExpandNotificationsPanel) isAnimateExpandNotificationsPanel = false;
            showNotificationAnimation.setDuration(duration);
            showNotificationAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mNotificationBg.setAlpha((Float) animation.getAnimatedValue());
                }
            });
            showNotificationAnimation.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    //float alpha = Math.max(MIN_ALPHA,mNotificationPanel.getExpandedFraction());
                    float alpha = STATUS_BAR_DROPDOWN_STYLE ? mNotificationPanel.getExpandedFraction() : 1f;
                    mNotificationBg.setAlpha(alpha);
                    showNotificationAnimation = null;
                }

            });
            showNotificationAnimation.start();
        }
        //BitmapDrawable bd2 = new BitmapDrawable(mContext.getResources(), mWallPapaerBitmap);
        //bd2.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        if(mWallPapaerBitmap!=null){
            mNotificationHeaderBg.setBg(mWallPapaerBitmap);
            if(mQSDetail!=null) mQSDetail.setBg(mWallPapaerBitmap);
            mNotificationPanel.setBottomBlurBg(mWallPapaerBitmap);
        
            /*PRIZE-add for alpha background,bugid: 28859- liufan-2017-03-02-start*/
            if(mWallPapaerBitmapBack!=null && mWallPapaerBitmap != mWallPapaerBitmapBack){
                mWallPapaerBitmapBack.recycle();
                mWallPapaerBitmapBack = null;
            }
            mWallPapaerBitmapBack = mWallPapaerBitmap.copy(Bitmap.Config.ARGB_8888,true);
            /*PRIZE-add for alpha background,bugid: 28859- liufan-2017-03-02-end*/
        }

        /*-modify for haokan-liufan-2017-10-26-start-*/
        if(mShowHaoKanRunnable != null){
            mShowHaoKanRunnable.run();
            mShowHaoKanRunnable = null;
        }
        /*-modify for haokan-liufan-2017-10-26-end-*/
    }

    /*PRIZE-finish the showNotificationAnimation-liufan-2016-06-03-start*/
    public void finishShowNotificationAnimation(float alpha){
        if(showNotificationAnimation != null){
            showNotificationAnimation.cancel();
            showNotificationAnimation = null;
            mNotificationBg.setAlpha(alpha);
        }
    }
    /*PRIZE-finish the showNotificationAnimation-liufan-2016-06-03-end*/
    
    public int BACK_COLOR = 0xee000000;
    public Bitmap getHalfAlphaColorBackground(int color){
        /*Bitmap bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(color);*/
        mNotificationBg.setBackgroundColor(color);
        mNotificationHeaderBg.setBackgroundColor(color);
        return null;
    }
    
    /**
    * Method Description：get the blur background
    */
    private Bitmap getBlurBackground(){
        Bitmap bitmap = screenshot();
        return blur(bitmap);
    }
    
    /**
    * Method Description：get the blur wallpapaer
    */
    private Bitmap getBlurWallpaper(){
        Bitmap bitmap = null;
        Drawable d = mBackdropBack.getDrawable();
        if(d instanceof BitmapDrawable){
            BitmapDrawable bd = (BitmapDrawable)d;
            bitmap = bd.getBitmap();
        }
        if(d instanceof LockscreenWallpaper.WallpaperDrawable){
            LockscreenWallpaper.WallpaperDrawable wd = (LockscreenWallpaper.WallpaperDrawable)d;
            if(wd != null) bitmap = wd.getConstantState().getBackground();
        }

        //add by haokan-liufan-2016-10-11-start
        //modify by zookingsoft-vincent 20161116
        if(//!NotificationPanelView.USE_VLIFE && !NotificationPanelView.USE_ZOOKING 
                //&& 
				isUseHaoKan() && mNotificationPanel.isHaoKanViewShow()){
            //bitmap = mNotificationPanel.getCurrentImage();
            return blur(null);
        }
        //add by haokan-liufan-2016-10-11-end
        if(bitmap == null){
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        }
        Log.d(TAG,"wallpaper bitmap ------>("+bitmap.getWidth()+" x "+bitmap.getHeight()+")");
        long time1 = System.currentTimeMillis();
        return blur(bitmap);
    }
    
    /**
    * Method Description：blur algorithm
    */
    public Bitmap blur(Bitmap bitmap){
        long time1 = System.currentTimeMillis();
        if(bitmap!=null){
            int value = 3;
            if(value == 1){
                bitmap = BlurPic.BoxBlurFilter(bitmap);
            }else if(value == 2){
                bitmap = blurBitmap(bitmap,false);
                bitmap = bitmap!=null ? blurBitmap(bitmap,false) : null;
            }else if(value == 3){
                bitmap = BlurPic.blurScale(bitmap);
            }
            if(bitmap!=null){
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(0xbb000000);
            }
        }else{
            bitmap = Bitmap.createBitmap(8, 8, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            //canvas.drawColor(BACK_COLOR);
            canvas.drawColor(0xf4000000);
        }
        long time2 = System.currentTimeMillis();
        Log.d(TAG,"Blur time ------>"+(time2-time1));
        return bitmap;
    }
    
    /**
    * Method Description：screenshot
    */
    private Bitmap screenshot(){
        long time1 = System.currentTimeMillis();
        Bitmap mScreenBitmap = null;
        WindowManager mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display mDisplay = mWindowManager.getDefaultDisplay();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mDisplay.getRealMetrics(mDisplayMetrics);
        float[] dims = {mDisplayMetrics.widthPixels / 1.2f , mDisplayMetrics.heightPixels / 1.2f };
        if (dims[0]>dims[1]) {
            mScreenBitmap = SurfaceControl.screenshot((int) dims[1], (int) dims[0]);
            Matrix matrix = new Matrix();  
            matrix.reset();
            int rotation = mDisplay.getRotation();
            if(rotation==3){//rotation==3 
                matrix.setRotate(90);
            }else{//rotation==1 
                matrix.setRotate(-90);
            }
            Bitmap bitmap = mScreenBitmap;
            mScreenBitmap = Bitmap.createBitmap(bitmap,0,0, bitmap.getWidth(), bitmap.getHeight(),matrix, true);
            Log.e(TAG,"mScreenBitmap------------rotation-------->"+mScreenBitmap+", width---->"+mScreenBitmap.getWidth()+", height----->"+mScreenBitmap.getHeight());
            bitmap.recycle();
            bitmap = null;
        }else{
            mScreenBitmap = SurfaceControl.screenshot((int) dims[0], ( int) dims[1]);
        }
        long time2 = System.currentTimeMillis();
        Log.d(TAG,"screenshot time ------>"+(time2-time1));
        return mScreenBitmap;
    }
    
    /**
    * Method Description：blur bitmap with the RenderScript
    */
    public Bitmap blurBitmap(Bitmap bitmap,boolean isDrawScrim){  
          
        //Let's create an empty bitmap with the same size of the bitmap we want to blur  
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
          
        //Instantiate a new Renderscript  
        RenderScript rs = RenderScript.create(mContext.getApplicationContext());  
          
        //Create an Intrinsic Blur Script using the Renderscript  
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));  
          
        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps  
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);  
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);  
          
        //Set the radius of the blur  
        blurScript.setRadius(25f);  
          
        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);
        
        //two times 
        //Copy the final bitmap created by the out Allocation to the outBitmap  
        allOut.copyTo(outBitmap);  
        ///*
        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps  
        allIn = Allocation.createFromBitmap(rs, bitmap);  
        allOut = Allocation.createFromBitmap(rs, outBitmap);  
          
        //Set the radius of the blur  
        blurScript.setRadius(25f);  
          
        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);
          
        //Copy the final bitmap created by the out Allocation to the outBitmap  
        allOut.copyTo(outBitmap);
        //*/
        //recycle the original bitmap  
        bitmap.recycle();  
          
        //After finishing everything, we destroy the Renderscript.  
        rs.destroy();  
        
        if(isDrawScrim){
            Canvas canvas = new Canvas(outBitmap);
            //canvas.drawColor(0xbb000000);
            //canvas.drawColor(0xe51b394d);
            
            //canvas.drawColor(0xe51f3243);
            //canvas.drawColor(0xe51e3d52);
            //canvas.drawColor(0xe52e3f4a);
            //canvas.drawColor(0xe52e364a);
            //canvas.drawColor(0xe5293e51);
            //canvas.drawColor(0xe5223034);
            //canvas.drawColor(0x8024373d);
            canvas.drawColor(CRIM_COLOR);
        }
        return outBitmap;
    }
    /*PRIZE-blur background- liufan-2015-06-08-end*/
    
    /*PRIZE-the listener of the base tile- liufan-2015-04-10-start*/
    
    OnTileClickListener onTileClickListener = new QSTile.OnTileClickListener(){
        @Override
        public void onTileClick(boolean newState,String tileSpec){
            Message msg = Message.obtain();
            msg.obj = tileSpec;
            msg.arg1 = newState ? 1 : 0;
            tileHandler.sendMessageDelayed(msg, 0);
        }
    };
    
    private static final String SCREEN_SHOT_ACTION = "org.android.broadcastreceiverregister.SLIDESCREENSHOT";//截屏的广播
    private final int COLLAPSE_PANELS_TIME_SPACE = 510;
    
    Handler tileHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String tileSpec = (String)msg.obj;
            boolean newState = msg.arg1 == 1 ? true : false;
            if (tileSpec.equals("wifi")) {
                
            } else if (tileSpec.equals("bt")){
                
            } else if (tileSpec.equals("dataconnection")) {
                
            } else if(tileSpec.equals("airplane") ){
                
            } else if(tileSpec.equals("audioprofile") ){
                
            } else if(tileSpec.equals("cell") ){
                
            } else if(tileSpec.equals("rotation") ){
                
            } else if(tileSpec.equals("flashlight") ){
                
            } else if(tileSpec.equals("location") ){
                
            } else if(tileSpec.equals("gps") ){
                
            } else if(tileSpec.equals("cast") ){
                
            } else if(tileSpec.equals("hotknot") ){
                
            } else if(tileSpec.equals("screenshot") ){
                takeScreenShot();
	/**shiyicheng-add-for-supershot-2015-11-03-start*/
 		}else if(tileSpec.equals("superscreenshot") ){
                supertakeScreenShot();

	/**shiyicheng-add-for-supershot-2015-11-03-end*/
            } else if(tileSpec.equals("lockscreen") ){
                lockScreen();
            } else if (tileSpec.equals("speedup")) {
                startCleanRubbishActivity();
            } else if(tileSpec.equals("cleanupkey") ){
                startCleanUpProcessActivity();
                /*PRIZE-create thread, delete recents data- liufan-2016-01-28-start*/
                deleteRecentsData();
                /*PRIZE-create thread, delete recents data- liufan-2016-01-28-end*/
            } else if(tileSpec.equals("brightness") ){
                
            } else if(tileSpec.equals("power") ){
                startBatteryActivity();
            /*PRIZE-Add for BluLight-zhudaopeng-2017-05-10-Start*/
            } else if(tileSpec.equals("blulight")){
            /*PRIZE-Add for BluLight-zhudaopeng-2017-05-10-End*/
            } else if(tileSpec.equals("dormancy") ){//dormancy
                
            } else if(tileSpec.equals("more") ){
                startEditQsActivity();
            }else if(tileSpec.equals("prizerings") ){
               
			   
            }
        }
        
    };

    //prize add by xiarui 2018-02-08 @{
    private void startCleanRubbishActivity() {
        prizeanimateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE, false);
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.pr.scuritycenter", "com.pr.scuritycenter.rubbish.CleanRubbish");
        intent.setComponent(comp);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent, false);
    }
    //---end@}

    /*PRIZE-create thread, delete recents data- liufan-2016-01-28-start*/
    public void deleteRecentsData(){
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG,"deleteRecentsData time start");
                RecentsTaskLoader loader = Recents.getTaskLoader();
                RecentsTaskLoadPlan plan = loader.createLoadPlan(mContext);
                SystemServicesProxy ssp = Recents.getSystemServices();
                RecentsConfiguration mConfig = Recents.getConfiguration();
                RecentsActivityLaunchState launchState = mConfig.getLaunchState();
                loader.preloadTasks(plan, -1 /* runningTaskId */,
                        false /* includeFrontMostExcludedTask */);
                RecentsTaskLoadPlan.Options loadOpts = new RecentsTaskLoadPlan.Options();
                loadOpts.numVisibleTasks = launchState.launchedNumVisibleTasks;
                loadOpts.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
                loader.loadTasks(mContext, plan, loadOpts);
                TaskStack stacks = plan.getTaskStack();
                ArrayList<Task> lockList = new ArrayList<Task>();
                try{
                    ActivityManagerNative.getDefault().setDumpKill(true);
                } catch (RemoteException ex) {
                }

                final long used_before = RecentsActivity.getAvailMemory(mContext);
                final long total = RecentsActivity.totalMemoryB == 0 ? RecentsActivity.getTotalMemory() : RecentsActivity.totalMemoryB;

                ArrayList<Task> tasks = new ArrayList<Task>();
                tasks.addAll(stacks.getStackTasks());
                for(Task task : tasks){
                    task.isLock = task.isLock(mContext);
                    if(task!=null){
                        if(!task.isLock){
                            stacks.removeTask(task,null,false);
                            loader.deleteTaskData(task, false);
                            ssp.removeTask(task.key.id);
                            task.unlockByRemoveTask(mContext); //prize add by xiarui 2018-02-05
                            Log.d(TAG,"cls=="+task.key.getComponent().getClassName());
                        } else {
                            lockList.add(task);
                        }
                    }
                }

                try{
                    ActivityManagerNative.getDefault().setDumpKill(false);
                } catch (RemoteException ex) {
                }
                TaskStackView.killAll(mContext,lockList,true);
                final long used_after = RecentsActivity.getAvailMemory(mContext);
                long rel = used_after - used_before;
                rel = RecentsActivity.numericConversions(rel, RecentsActivity.MEMORY_UNIT_MB) < 1 ? 0 : rel;
                final long rel_c = rel;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isCleanActivityFinish = true;
                RecentsActivity.showCleanResultByToast(mContext, 
                    RecentsActivity.numericConversions(rel_c, RecentsActivity.MEMORY_UNIT_MB) + RecentsActivity.MEMORY_UNIT_MB,
                    Formatter.formatFileSize(mContext,used_after));
                        Log.d(TAG,"deleteRecentsData time end");
                    }
                },800);
            }
        }.start();
    }
    public static boolean isCleanActivityFinish = false;
    /*PRIZE-create thread, delete recents data- liufan-2016-01-28-end*/
    
    /**
    * Method Description：open the BatteryActivity
    */
    public void startBatteryActivity() {
        startActivity(new Intent(Intent.ACTION_POWER_USAGE_SUMMARY),true /* dismissShade */);
    }
    
    /**
    * Method Description：cleanup by onekey
    */
    public void startCleanUpProcessActivity(){
        animateCollapsePanels();
        
        tileHandler.postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent intent = new Intent();
                ComponentName component = new ComponentName(mContext, CleanUpProcessActivity.class);
                intent.setComponent(component);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                // mPhoneStatusBar.animateExpandSettingsPanel();
            }
        },COLLAPSE_PANELS_TIME_SPACE);
    }
    
    
    /**
    * Method Description：lockscreen 
    */
    public void lockScreen(){
        PowerManager mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mPowerManager.goToSleep(SystemClock.uptimeMillis(),
                PowerManager.GO_TO_SLEEP_REASON_POWER_BUTTON, 0);
    }
    
    /**
    * Method Description：Screenshot
    */
    public void takeScreenShot(){
        animateCollapsePanels();
                
        tileHandler.postDelayed(new Runnable(){
            @Override
            public void run() {
                //PhoneWindowManager pwm = (PhoneWindowManager)PolicyManager.makeNewWindowManager();
                //pwm.takeScreenShotByOneButton();
                final Intent intent=new Intent(SCREEN_SHOT_ACTION);
                mContext.sendBroadcast(intent);
            }
        },COLLAPSE_PANELS_TIME_SPACE);
    }

/**shiyicheng-add-for-supershot-2015-11-03-start*/
 	public void supertakeScreenShot(){
		 //animateCollapsePanels();
		 prizeanimateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE, false);
		 //Intent intent = new Intent();
                //ComponentName component = new ComponentName("com.example.longshotscreen", "com.example.longshotscreen.MainActivity");
                //intent.setComponent(component);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //mContext.startActivity(intent);
			Intent intent = new Intent("com.freeme.supershot.MainFloatMenu");	
			intent.setPackage("com.example.longshotscreen");
			mContext.startService(intent);	
			}

/**shiyicheng-add-for-supershot-2015-11-03-end*/

    /**
    * Method Description：open EditQSActivity
    */
    private void startEditQsActivity() {
        //mActivityStarter.startActivity(new Intent("com.android.action_edit_qs"), true /* dismissShade */);
        /*animateCollapsePanels();
        
        tileHandler.postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent intent = new Intent();
                ComponentName component = new ComponentName(mContext, EditQSActivity.class);
                intent.setComponent(component);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                // mPhoneStatusBar.animateExpandSettingsPanel();
            }
        },COLLAPSE_PANELS_TIME_SPACE);*/
    }
    /*PRIZE-the listener of the base tile- liufan-2015-04-10-end*/

    public void clearAllNotifications() {

        // animate-swipe all dismissable notifications, then animate the shade closed
        int numChildren = mStackScroller.getChildCount();

        final ArrayList<View> viewsToHide = new ArrayList<View>(numChildren);
        for (int i = 0; i < numChildren; i++) {
            final View child = mStackScroller.getChildAt(i);
            if (child instanceof ExpandableNotificationRow) {
                if (mStackScroller.canChildBeDismissed(child)) {
                    if (child.getVisibility() == View.VISIBLE) {
                        viewsToHide.add(child);
                    }
                }
                ExpandableNotificationRow row = (ExpandableNotificationRow) child;
                List<ExpandableNotificationRow> children = row.getNotificationChildren();
                if (row.areChildrenExpanded() && children != null) {
                    for (ExpandableNotificationRow childRow : children) {
                        if (childRow.getVisibility() == View.VISIBLE) {
                            viewsToHide.add(childRow);
                        }
                    }
                }
            }
        }
        if (viewsToHide.isEmpty()) {
            animateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE);
            return;
        }

        addPostCollapseAction(new Runnable() {
            @Override
            public void run() {
                mStackScroller.setDismissAllInProgress(false);
                try {
                    mBarService.onClearAllNotifications(mCurrentUserId);
                } catch (Exception ex) { }
            }
        });

        performDismissAllAnimations(viewsToHide);

    }

    private void performDismissAllAnimations(ArrayList<View> hideAnimatedList) {
        Runnable animationFinishAction = new Runnable() {
            @Override
            public void run() {
                animateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE);
            }
        };

        // let's disable our normal animations
        mStackScroller.setDismissAllInProgress(true);

        // Decrease the delay for every row we animate to give the sense of
        // accelerating the swipes
        int rowDelayDecrement = 10;
        int currentDelay = 140;
        int totalDelay = 180;
        int numItems = hideAnimatedList.size();
        for (int i = numItems - 1; i >= 0; i--) {
            View view = hideAnimatedList.get(i);
            Runnable endRunnable = null;
            if (i == 0) {
                endRunnable = animationFinishAction;
            }
            mStackScroller.dismissViewAnimated(view, endRunnable, totalDelay, 260);
            currentDelay = Math.max(50, currentDelay - rowDelayDecrement);
            totalDelay += currentDelay;
        }
    }

    @Override
    protected void setZenMode(int mode) {
        super.setZenMode(mode);
        if (mIconPolicy != null) {
            mIconPolicy.setZenMode(mode);
        }
    }

    protected void startKeyguard() {
        Trace.beginSection("PhoneStatusBar#startKeyguard");
        KeyguardViewMediator keyguardViewMediator = getComponent(KeyguardViewMediator.class);
        mFingerprintUnlockController = new FingerprintUnlockController(mContext,
                mStatusBarWindowManager, mDozeScrimController, keyguardViewMediator,
                mScrimController, this);
        mStatusBarKeyguardViewManager = keyguardViewMediator.registerStatusBar(this,
                getBouncerContainer(), mStatusBarWindowManager, mScrimController,
                mFingerprintUnlockController);
        mKeyguardIndicationController.setStatusBarKeyguardViewManager(
                mStatusBarKeyguardViewManager);
        mFingerprintUnlockController.setStatusBarKeyguardViewManager(mStatusBarKeyguardViewManager);
        mIconPolicy.setStatusBarKeyguardViewManager(mStatusBarKeyguardViewManager);
        mRemoteInputController.addCallback(mStatusBarKeyguardViewManager);

        mRemoteInputController.addCallback(new RemoteInputController.Callback() {
            @Override
            public void onRemoteInputSent(Entry entry) {
                if (FORCE_REMOTE_INPUT_HISTORY && mKeysKeptForRemoteInput.contains(entry.key)) {
                    removeNotification(entry.key, null);
                } else if (mRemoteInputEntriesToRemoveOnCollapse.contains(entry)) {
                    // We're currently holding onto this notification, but from the apps point of
                    // view it is already canceled, so we'll need to cancel it on the apps behalf
                    // after sending - unless the app posts an update in the mean time, so wait a
                    // bit.
                    mHandler.postDelayed(() -> {
                        if (mRemoteInputEntriesToRemoveOnCollapse.remove(entry)) {
                            removeNotification(entry.key, null);
                        }
                    }, REMOTE_INPUT_KEPT_ENTRY_AUTO_CANCEL_DELAY);
                }
            }
        });

        mKeyguardViewMediatorCallback = keyguardViewMediator.getViewMediatorCallback();
        mLightStatusBarController.setFingerprintUnlockController(mFingerprintUnlockController);
        Trace.endSection();
        /*PRIZE-add for haokan-liufan-2018-01-16-start*/
        if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW && isOpenMagazine()){
            mNotificationPanel.initHaoKanView();
        }
        /*PRIZE-add for haokan-liufan-2018-01-16-end*/
    }

    @Override
    protected View getStatusBarView() {
        return mStatusBarView;
    }

    public StatusBarWindowView getStatusBarWindow() {
        return mStatusBarWindow;
    }

    protected ViewGroup getBouncerContainer() {
        return mStatusBarWindow;
    }

    public int getStatusBarHeight() {
        if (mNaturalBarHeight < 0) {
            final Resources res = mContext.getResources();
            mNaturalBarHeight =
                    res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
        }
        return mNaturalBarHeight;
    }

    private View.OnClickListener mRecentsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

			/*PRIZE-PowerExtendMode-disable Recents-wangxianzhen-2015-07-21-start*/
            if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
                Log.i(TAG,"PowerExtendMode mRecentsClickListener");
                return;
            }
            /*PRIZE-PowerExtendMode-disable Recents-wangxianzhen-2015-07-21-end*/

			/* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 start */
            if (Settings.System.getInt(mContext.getContentResolver(), DISABLE_NAVIGATION, 0) == 1) {
                Log.i(TAG,"Contacts requirement, disable click recent");
                return;
            }
            /* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 end */
			
            awakenDreams();
            toggleRecentApps();
        }
    };

    /// M: BMW restore button@{
    private View.OnClickListener mRestoreClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "mRestoreClickListener");
            SystemServicesProxy ssp = Recents.getSystemServices();
            ssp.restoreWindow();
        }
    };
   /// @}

    private View.OnLongClickListener mLongPressBackListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
			/*PRIZE-PowerExtendMode-disable Recents-wangxianzhen-2015-07-21-start*/
            if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
                Log.i(TAG,"PowerExtendMode mLongPressBackRecentsListener");
                return true;
            }
            /*PRIZE-PowerExtendMode-disable Recents-wangxianzhen-2015-07-21-end*/
			
			/* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 start */
            if (Settings.System.getInt(mContext.getContentResolver(), DISABLE_NAVIGATION, 0) == 1) {
                Log.i(TAG,"Contacts requirement, disable long click back");
                return true;
            }
            /* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 end */
			
            return handleLongPressBack();
        }
    };

   private boolean prizeSplitScreenJumpWelcomeActivity(final String className) {
    final String welcomeActivity[]={
        "com.pplive.androidphone.ui.FirstActivity",
        "com.yy.mobile.ui.splash.SplashActivity",
        "com.duowan.kiwi.simpleactivity.SplashActivity",
        "com.huajiao.cover.CoverActivity",
        "com.husor.beibei.activity.SplashActivity",
        "com.shuqi.activity.SplashActivity",
        "com.tmall.wireless.splash.TMSplashActivity",
        "com.netease.nr.biz.ad.AdActivity",
        "com.meelive.ingkee.business.commercial.launcher.ui.IngkeeLauncher",
        "com.immomo.momo.android.activity.WelcomeActivity",
        "com.readingjoy.iyd.ui.activity.IydLogoActivity",
        "com.tuniu.app.ui.homepage.LaunchActivity",
    };
    for( String str:welcomeActivity){
        if( str.equals(className) ){
            return true;
        }
    }
    return false;
   }

        private boolean longClickOpenSplitScreen(View viewrcent) {
            if (mRecents == null || !ActivityManager.supportsMultiWindow()
                    || !getComponent(Divider.class).getView().getSnapAlgorithm()
                            .isSplitScreenFeasible()) {
                return false;
            }
            /* prize-add-split screen, PRIZE_OLD_LAUNCHER not use split screnn-liyongli-20171125-start */
            if (PrizeOption.PRIZE_OLD_LAUNCHER){
                int isOldLauncher = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_OLD_LAUNCHER, 0);
                if(isOldLauncher==1){
                    Log.i(TAG," onLongClick Recents, PRIZE_OLD_LAUNCHER return");
                    return true;
                }
            }/* prize-add-split screen, PRIZE_OLD_LAUNCHER not use split screnn-liyongli-20171125-end */
            
            if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
                Log.i(TAG," onLongClick Recents, isSuperSaverMode return");
                Toast.makeText(mContext, R.string.super_saver_not_splite_app,
                        Toast.LENGTH_SHORT).show();
                return true;
            }
			
			/* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 start */
            if (Settings.System.getInt(mContext.getContentResolver(), DISABLE_NAVIGATION, 0) == 1) {
                Log.i(TAG,"Contacts requirement, disable long click recent");
                return true;
            }
            /* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 end */
            
            if( PrizeOption.PRIZE_SPLIT_SCREEN_ALLAPP ){
                SystemServicesProxy ssp = SystemServicesProxy.getInstance(mContext);
                ActivityManager.RunningTaskInfo runningTask = ssp.getRunningTask();
                if( runningTask!=null && runningTask.topActivity!=null ){
                    final String activityClass = runningTask.topActivity.getClassName();
                    final String pkg = runningTask.topActivity.getPackageName();
                    //Log.i(TAG," onLongClick Recents : "+ activityClass + " "+pkg);
                    if( "com.tencent.mm.plugin.voip.ui.VideoActivity".equals(activityClass) 
                        ||"com.tencent.av.ui.AVActivity".equals(activityClass)
                    ){
                        return true;
                    }
                    if( prizeSplitScreenJumpWelcomeActivity(activityClass) ){
                        Toast.makeText(mContext, com.android.internal.R.string.android_upgrading_starting_apps,
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    
                    //not in split screen  com.baidu.duer.phone.DuerPhoneMainActivity
                    if( "com.prize.applock.fingerprintapplock.LockUI".equals(activityClass)
                        ||"com.baidu.duer.phone".equals(pkg)                    
                    ){
                        Toast.makeText(mContext, R.string.recents_incompatible_app_message,
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
            }

            toggleSplitScreenMode(MetricsEvent.ACTION_WINDOW_DOCK_LONGPRESS,
                    MetricsEvent.ACTION_WINDOW_UNDOCK_LONGPRESS);
            return true;
        }
           
    private View.OnLongClickListener mRecentsLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            return longClickOpenSplitScreen(v);

        }
    };

    @Override
    protected void toggleSplitScreenMode(int metricsDockAction, int metricsUndockAction) {
        if (mRecents == null) {
            return;
        }
        int dockSide = WindowManagerProxy.getInstance().getDockSide();
        if (dockSide == WindowManager.DOCKED_INVALID) {
            mRecents.dockTopTask(NavigationBarGestureHelper.DRAG_MODE_NONE,
                    ActivityManager.DOCKED_STACK_CREATE_MODE_TOP_OR_LEFT, null, metricsDockAction);
        } else {
            EventBus.getDefault().send(new UndockingTaskEvent());
            if (metricsUndockAction != -1) {
                MetricsLogger.action(mContext, metricsUndockAction);
            }
        }
    }

    private final View.OnLongClickListener mLongPressHomeListener
            = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (shouldDisableNavbarGestures()) {
                return false;
            }
            MetricsLogger.action(mContext, MetricsEvent.ACTION_ASSIST_LONG_PRESS);
            mAssistManager.startAssist(new Bundle() /* args */);
            awakenDreams();
            if (mNavigationBarView != null) {
                mNavigationBarView.abortCurrentGesture();
            }
			/*prize-XiaoKu-add-yangming-20170922-start*/
	        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
			SystemServicesProxy ssp = Recents.getSystemServices();
            if(runningActivity.equals("com.android.incallui.InCallActivity") && mStatusBarKeyguardViewManager.isShowing()){
                 Log.i("xiaoku","in lockscreen InCallActivity can not boot xiaoku");
                 return true;
            }
            if(runningActivity.equals("com.android.gallery3d.app.GalleryActivity") && mStatusBarKeyguardViewManager.isShowing()){
                 Log.i("xiaoku","in lockscreen GalleryActivity can not boot xiaoku");
                 return true;
            }
            if(runningActivity.equals("com.android.camera.SecureCameraActivity") && mStatusBarKeyguardViewManager.isShowing()){
                 Log.i("xiaoku","in lockscreen SecureCameraActivity can not boot xiaoku");
                 return true;
            }
            if(runningActivity.equals("com.android.phone.EmergencyDialer")){
                Log.i("xiaoku","in EmergencyDialer can not boot xiaoku");
                return true;
            }
	        if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
                Log.i("xiaoku","PowerExtendMode mLongPressHomeListener");
                return true;
            }
			/* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 start */
            if (Settings.System.getInt(mContext.getContentResolver(), DISABLE_NAVIGATION, 0) == 1) {
                Log.i(TAG,"Contacts requirement, disable long click home");
                return true;
            }
            /* PRIZE add by yueliu for disable navigation(Contacts requirement) on 2018-5-24 end */
			/*prize-XiaoKu-add-huhuan-20180519-start*/
			if(ssp!=null && ssp.hasDockedTask()){
				Log.i("xiaoku","in InMultiWindowMode can not boot xiaoku");
				return true;
			}
			/*prize-XiaoKu-add-huhuan-20180519-end*/
            if(PrizeOption.PRIZE_GAME_MODE){
                boolean bPrizeGameMode = (Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_GAME_MODE,0) == 1);
                if(bPrizeGameMode){
                    Log.i("xiaoku","in GAME MODE can not boot xiaoku");
                    return true;
                }
            }
            if(PrizeOption.PRIZE_XIAOKU){
                try{
                    Log.i("xiaoku","Systemui OLD&& LongPressHome boot xiaoku");
					if (ActivityManagerNative.isSystemReady()) {
                        try {
                            ActivityManagerNative.getDefault().closeSystemDialogs("boot xiaoku");
                        } catch (RemoteException e) {
							Log.i("xiaoku","Systemui boot xiaoku closeSystemDialogs Exception = " + e);
                        }
                    }
                    Intent intent = new Intent();
                    intent.setAction("com.baidu.duer.phone");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }catch (Exception e){
                    Log.i("xiaoku","start xiaoku Exception is" + e);
                }
                return true;
            }
			/*prize-XiaoKu-add-yangming-20170922-end*/
            return true;
        }
    };

    private final View.OnTouchListener mHomeActionListener = new View.OnTouchListener() {
        public boolean mBlockedThisTouch;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mBlockedThisTouch && event.getActionMasked() != MotionEvent.ACTION_DOWN) {
                return true;
            }
            // If an incoming call is ringing, HOME is totally disabled.
            // (The user is already on the InCallUI at this point,
            // and his ONLY options are to answer or reject the call.)
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mBlockedThisTouch = false;
                    TelecomManager telecomManager = mContext.getSystemService(TelecomManager.class);
                    if (telecomManager != null && telecomManager.isRinging()) {
                        if (mStatusBarKeyguardViewManager.isShowing()) {
                            Log.i(TAG, "Ignoring HOME; there's a ringing incoming call. " +
                                    "No heads up");
                            mBlockedThisTouch = true;
                            return true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    awakenDreams();
                    break;
            }
            return false;
        }
    };

    private void awakenDreams() {
        if (mDreamManager != null) {
            try {
                mDreamManager.awaken();
            } catch (RemoteException e) {
                // fine, stay asleep then
            }
        }
    }

    private void prepareNavigationBarView() {
        mNavigationBarView.reorient();

        ButtonDispatcher recentsButton = mNavigationBarView.getRecentsButton();
        recentsButton.setOnClickListener(mRecentsClickListener);
        recentsButton.setOnTouchListener(mRecentsPreloadOnTouchListener);
        recentsButton.setLongClickable(true);
        recentsButton.setOnLongClickListener(mRecentsLongClickListener);

        ButtonDispatcher backButton = mNavigationBarView.getBackButton();
        backButton.setLongClickable(true);
        backButton.setOnLongClickListener(mLongPressBackListener);

        ButtonDispatcher homeButton = mNavigationBarView.getHomeButton();
        homeButton.setOnTouchListener(mHomeActionListener);
        homeButton.setOnLongClickListener(mLongPressHomeListener);

        /// M: BMW  restore button @{
        if (MultiWindowManager.isSupported()) {
            ButtonDispatcher restoreButton = mNavigationBarView.getRestoreButton();
            restoreButton.setOnClickListener(mRestoreClickListener);

        }
        /// @}

        /* Dynamically hiding nav bar feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR) {
            registerHideNavBarClickListener();
        } //END...

        mAssistManager.onConfigurationChanged();
    }

    // For small-screen devices (read: phones) that lack hardware navigation buttons
    protected void addNavigationBar() {
        if (DEBUG) Log.v(TAG, "addNavigationBar: about to add " + mNavigationBarView);
        if (mNavigationBarView == null) return;

        try {
            WindowManagerGlobal.getWindowManagerService()
                    .watchRotation(new IRotationWatcher.Stub() {
                @Override
                public void onRotationChanged(int rotation) throws RemoteException {
                    // We need this to be scheduled as early as possible to beat the redrawing of
                    // window in response to the orientation change.
                    Message msg = Message.obtain(mHandler, () -> {
                        if (mNavigationBarView != null
                                && mNavigationBarView.needsReorient(rotation)) {
                            repositionNavigationBar();
                        }
                        //prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar start
                        if(OPEN_GESTURE_NAVIGATION){
                        	mGestureIndicatorManager.updateView(rotation);
                        }
                        //prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar end
                    });
                    msg.setAsynchronous(true);
                    mHandler.sendMessageAtFrontOfQueue(msg);
                }
            });
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }

        prepareNavigationBarView();

        /* Dynamically changing Recents function feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_TREAT_RECENTS_AS_MENU) {
            enableTreatRecentsAsMenu(mEnableTreatRecentsAsMenu, true);
        } //end...

        /* Nav bar related to mBack key feature. prize-linkh-20161115 */
        if (SUPPORT_NAV_BAR_FOR_MBACK_DEVICE && needHideNavBarFormBack()) {
            hideNavBarFormBack();
        } //END...

        /* Dynamically hiding nav bar feature & Nav bar related to mBack key feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR || SUPPORT_NAV_BAR_FOR_MBACK_DEVICE) {
            mNavBarLayoutParams = getNavigationBarLayoutParams();
        } //END...

        mWindowManager.addView(mNavigationBarView, getNavigationBarLayoutParams());
        //prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar start
		if(OPEN_GESTURE_NAVIGATION){
			mGestureIndicatorManager.addView();
		}
		//prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar end
    }

    protected void repositionNavigationBar() {
        if (mNavigationBarView == null || !mNavigationBarView.isAttachedToWindow()) return;

        prepareNavigationBarView();

        /* Dynamically changing Recents function feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_TREAT_RECENTS_AS_MENU) {
            enableTreatRecentsAsMenu(mEnableTreatRecentsAsMenu, true);
        } //end...

        mWindowManager.updateViewLayout(mNavigationBarView, getNavigationBarLayoutParams());
    }

    private void notifyNavigationBarScreenOn(boolean screenOn) {
        if (mNavigationBarView == null) return;
        mNavigationBarView.notifyScreenOn(screenOn);
    }

    private WindowManager.LayoutParams getNavigationBarLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_NAVIGATION_BAR,
                    0
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                    | WindowManager.LayoutParams.FLAG_SLIPPERY,
                PixelFormat.TRANSLUCENT);
        // this will allow the navbar to run in an overlay on devices that support this
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        }

        lp.setTitle("NavigationBar");
        lp.windowAnimations = 0;
        return lp;
    }

    @Override
    public void setIcon(String slot, StatusBarIcon icon) {
        mIconController.setIcon(slot, icon);
    }

    @Override
    public void removeIcon(String slot) {
        mIconController.removeIcon(slot);
    }

    public UserHandle getCurrentUserHandle() {
        return new UserHandle(mCurrentUserId);
    }

    /* prize-add-split screen-liyongli-20170610-start */
    private boolean sendOpenQQFloatIconMsg(StatusBarNotification n, ExpandableNotificationRow row) {
    	if( !PrizeOption.PRIZE_SYSTEMUI_QQPOP_ICON ){
    		return false;
    	}
        if( mStatusBarWindowState==WINDOW_STATE_SHOWING ){
        	//1.  statubar is showing
        	 return false;
        }
    	
        String pkg = n.getPackageName();
        int type = PrizeOpenPendingIntentEvent.QQ_TYPE;
        Log.d(TAG, " lylqq pkg=" + pkg );
        if(  PrizeFloatQQService.PKG_QQ.equals(pkg) ){
        	 type = PrizeOpenPendingIntentEvent.QQ_TYPE;
        }else if( PrizeFloatQQService.PKG_MM.equals(pkg) ){
        	 type = PrizeOpenPendingIntentEvent.MM_TYPE;
        }else{
        	return false;
        }
        
        SystemServicesProxy ssp = SystemServicesProxy.getInstance(mContext);
        ActivityManager.RunningTaskInfo runningTask = ssp.getRunningTask();
//        if( runningTask!=null && runningTask.topActivity!=null ){
//            pkg = runningTask.topActivity.getPackageName();
//            if( "com.qq.reader".equals(pkg) ){
//                Log.d(TAG, " lylqq  com.qq.reader not split screen" );
//                return false;                
//            }
//        }
//  com.qq.reader   .... not surpot split screen, set in 
// frameworks/base/core/java/android/content/pm/PrizeSplitScreenApk.java
// use bellow (runningTask.isDockable)
        if( runningTask!=null && runningTask.isDockable ){
        mHandler.removeMessages(MSG_OPEN_QQFLOAT_ICON);
        Message msg = mHandler.obtainMessage(MSG_OPEN_QQFLOAT_ICON);
        msg.obj = row;
        msg.arg1 = type; //1 qq, 2 mm
        mHandler.sendMessage(msg);
        }
        
        return true;
    }
    /* prize-add-split screen-liyongli-20170610-end */
    
    @Override
    public void addNotification(StatusBarNotification notification, RankingMap ranking,
            Entry oldEntry) {
        if (DEBUG) Log.d(TAG, "addNotification key=" + notification.getKey());
        /// M: [ALPS02738355] fix foreground service flag_hide_notification issue. @{
        if (notification != null && notification.getNotification() != null &&
               (notification.getNotification().flags & Notification.FLAG_HIDE_NOTIFICATION) != 0) {
            Log.d(TAG, "Will not add the notification.flags contains FLAG_HIDE_NOTIFICATION");
            return;
        }
        /// @}
        mNotificationData.updateRanking(ranking);
        Entry shadeEntry = createNotificationViews(notification);
        if (shadeEntry == null) {
            Log.d(TAG, "addNotification shadeEntry  is null");
            return;
        }
        
        /* prize-add-split screen-liyongli-20170610-start */
        boolean isOpenQQFloat = sendOpenQQFloatIconMsg(notification, shadeEntry.row);
        /* prize-add-split screen-liyongli-20170610-end */
        
        boolean isHeadsUped = shouldPeek(shadeEntry);
        Log.d(TAG, "addNotification isHeadsUped = " + isHeadsUped + ", isOpenQQFloat = " + isOpenQQFloat);
        if (isHeadsUped && !isOpenQQFloat) {
            //prize modify by xiarui 2018-04-18 @{
			if (PRIZE_HEADS_UP_STYLE) {
                Log.d(TICKER_TAG, "addNotification");
                showNewStyleHeadsUp(shadeEntry);
                mHeadsUpManager.showNotification(shadeEntry);
            } else {
                mHeadsUpManager.showNotification(shadeEntry);
                // Mark as seen immediately
                setNotificationShown(notification);
            }
			//---end @}
        }

        if (!isHeadsUped && notification.getNotification().fullScreenIntent != null) {
            if (shouldSuppressFullScreenIntent(notification.getKey())) {
                if (DEBUG) {
                    Log.d(TAG, "No Fullscreen intent: suppressed by DND: " + notification.getKey());
                }
            } else if (mNotificationData.getImportance(notification.getKey())
                    < NotificationListenerService.Ranking.IMPORTANCE_MAX) {
                if (DEBUG) {
                    Log.d(TAG, "No Fullscreen intent: not important enough: "
                            + notification.getKey());
                }
            } else {
                // Stop screensaver if the notification has a full-screen intent.
                // (like an incoming phone call)
                awakenDreams();

                // not immersive & a full-screen alert should be shown
                if (DEBUG)
                    Log.d(TAG, "Notification has fullScreenIntent; sending fullScreenIntent");
                try {
                    EventLog.writeEvent(EventLogTags.SYSUI_FULLSCREEN_NOTIFICATION,
                            notification.getKey());
                    notification.getNotification().fullScreenIntent.send();
                    /*PRIZE-add for magazine-liufan-2018-1-27-start*/
                    mNotificationPanel.setWillOccluded(true);
                    NotificationPanelView.debugMagazine("isWillOccluded turn true");
                    //showHaoKanWallPaperInBackdropBack();
                    //mNotificationPanel.setHaokanViewVisible(false);
                    /*PRIZE-add for magazine-liufan-2018-1-27-end*/
                    shadeEntry.notifyFullScreenIntentLaunched();
                    MetricsLogger.count(mContext, "note_fullscreen", 1);
                } catch (PendingIntent.CanceledException e) {
                }
            }
        }
        addNotificationViews(shadeEntry, ranking);
        // Recalculate the position of the sliding windows and the titles.
        setAreThereNotifications();
    }

    //prize modify by xiarui 2018-04-12 @{
    private void showNewStyleHeadsUp(NotificationData.Entry entry) {
        boolean isFullyCollapsed = mNotificationPanel.isFullyCollapsed();
        Log.d(TICKER_TAG, "showNewStyleHeadsUp mExpandedVisible = " + mExpandedVisible + " , isFullyCollapsed = " + isFullyCollapsed);
        if (isFullyCollapsed/* && !mKeyguardManager.isKeyguardLocked()*/
                && (checkNeedShowTicker(entry) || "com.android.dialer".equals(entry.notification.getPackageName()))) {
            mTickerController.showTickerView(entry, mRemoteInputController);
        }
    }

    public void removeHeadsUpIfNeeded(String key) {
        Log.d(TICKER_TAG, "removeHeadsUpIfNeeded key = " + key);
        if (mTickerController != null && mTickerController.removeTickerViewIfNeed(key)) {
            mTickerController.animateTickerCollapse();
        }
    }

    private boolean checkNeedShowTicker(NotificationData.Entry entry) {
        CharSequence tickerText = entry.notification.getNotification().tickerText;
        Log.d(TICKER_TAG, "checkNeedShowTicker pkg = " + entry.notification.getPackageName() + " , tickerText = " + (tickerText != null ? tickerText.toString() : "tickerText is null"));
        return (tickerText != null && !tickerText.toString().isEmpty()) || noCustomViewNotification(entry);
    }

    private boolean noCustomViewNotification(NotificationData.Entry entry) {
        Log.d(TICKER_TAG, "noCustomViewNotification entry.row = " + (entry.row != null ? "row" : "row is null"));
        return entry.row == null || !entry.row.isCustomView();
    }
    //end by xiarui 2018-04-12 @}

    private boolean shouldSuppressFullScreenIntent(String key) {
        if (isDeviceInVrMode()) {
            return true;
        }

        if (mPowerManager.isInteractive()) {
            return mNotificationData.shouldSuppressScreenOn(key);
        } else {
            return mNotificationData.shouldSuppressScreenOff(key);
        }
    }

    @Override
    protected void updateNotificationRanking(RankingMap ranking) {
        mNotificationData.updateRanking(ranking);
        updateNotifications();
    }

    @Override
    public void removeNotification(String key, RankingMap ranking) {
        boolean deferRemoval = false;

        /**
         * prize delete by xiarui for bug63242\bug65189 2018-08-11
         * {@link com.android.systemui.statusbar.policy.HeadsUpManager#removeNotification(String, boolean)}
         * {@link com.android.systemui.statusbar.policy.HeadsUpManager.HeadsUpEntry#mRemoveHeadsUpRunnable}
         */
        //prize add by xiarui 2018-04-13 start @{
        //if (PRIZE_HEADS_UP_STYLE) {
        //    removeHeadsUpIfNeeded(key);
        //}
        //---end@}

        if (mHeadsUpManager.isHeadsUp(key)) {
            // A cancel() in repsonse to a remote input shouldn't be delayed, as it makes the
            // sending look longer than it takes.
            boolean ignoreEarliestRemovalTime = mRemoteInputController.isSpinning(key)
                    && !FORCE_REMOTE_INPUT_HISTORY;
            deferRemoval = !mHeadsUpManager.removeNotification(key,  ignoreEarliestRemovalTime);
        }
        if (key.equals(mMediaNotificationKey)) {
            clearCurrentMediaNotification();
            updateMediaMetaData(true, true);
        }
        if (FORCE_REMOTE_INPUT_HISTORY && mRemoteInputController.isSpinning(key)) {
            Entry entry = mNotificationData.get(key);
            StatusBarNotification sbn = entry.notification;

            Notification.Builder b = Notification.Builder
                    .recoverBuilder(mContext, sbn.getNotification().clone());
            CharSequence[] oldHistory = sbn.getNotification().extras
                    .getCharSequenceArray(Notification.EXTRA_REMOTE_INPUT_HISTORY);
            CharSequence[] newHistory;
            if (oldHistory == null) {
                newHistory = new CharSequence[1];
            } else {
                newHistory = new CharSequence[oldHistory.length + 1];
                for (int i = 0; i < oldHistory.length; i++) {
                    newHistory[i + 1] = oldHistory[i];
                }
            }
            newHistory[0] = String.valueOf(entry.remoteInputText);
            b.setRemoteInputHistory(newHistory);

            Notification newNotification = b.build();

            // Undo any compatibility view inflation
            newNotification.contentView = sbn.getNotification().contentView;
            newNotification.bigContentView = sbn.getNotification().bigContentView;
            newNotification.headsUpContentView = sbn.getNotification().headsUpContentView;

            StatusBarNotification newSbn = new StatusBarNotification(sbn.getPackageName(),
                    sbn.getOpPkg(),
                    sbn.getId(), sbn.getTag(), sbn.getUid(), sbn.getInitialPid(),
                    0, newNotification, sbn.getUser(), sbn.getPostTime());

            updateNotification(newSbn, null);
            mKeysKeptForRemoteInput.add(entry.key);
            return;
        }
        if (deferRemoval) {
            mLatestRankingMap = ranking;
            mHeadsUpEntriesToRemoveOnSwitch.add(mHeadsUpManager.getEntry(key));
            return;
        }
        Entry entry = mNotificationData.get(key);

        if (entry != null && mRemoteInputController.isRemoteInputActive(entry)
                && (entry.row != null && !entry.row.isDismissed())) {
            mLatestRankingMap = ranking;
            mRemoteInputEntriesToRemoveOnCollapse.add(entry);
            return;
        }

        if (entry != null && entry.row != null) {
            entry.row.setRemoved();
        }
        // Let's remove the children if this was a summary
        handleGroupSummaryRemoved(key, ranking);
        StatusBarNotification old = removeNotificationViews(key, ranking);
        /// M: Enable this log for unusual case debug.
        /*if (SPEW)*/ Log.d(TAG, "removeNotification key=" + key + " old=" + old);
        //removesendUnReadInfo(old);//add by zhouerlong 20160414
        if (old != null) {
            if (CLOSE_PANEL_WHEN_EMPTIED && !hasActiveNotifications()
                    && !mNotificationPanel.isTracking() && !mNotificationPanel.isQsExpanded()) {
                if (mState == StatusBarState.SHADE) {
                    animateCollapsePanels();
                } else if (mState == StatusBarState.SHADE_LOCKED && !isCollapsing()) {
                    goToKeyguard();
                }
            }
        }
        setAreThereNotifications();
    }

    //add by zhouerlong 20160414
	public void removesendUnReadInfo(StatusBarNotification notification) {

		if (notification!=null&&notification.getPackageName() != null) {

			if (notification.getPackageName().equals("com.tencent.mm")|| notification.getPackageName().equals("com.tencent.mobileqq")) {
				
				ComponentName c =null;
				if(notification.getPackageName().equals("com.tencent.mm")) {
					c= new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
				}else if(notification.getPackageName().equals("com.tencent.mobileqq")) {
					c= new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.SplashActivity");
				}
				Intent intent = new Intent(Intent.ACTION_UNREAD_CHANGED);
				intent.putExtra(Intent.EXTRA_UNREAD_COMPONENT,
						c);
				//intent.setAppInstanceIndex(notification.appInstanceIndex);-temp-delete-
				intent.putExtra(Intent.EXTRA_UNREAD_NUMBER, 0);
				mContext.sendBroadcast(intent);
			}
		}
	}

    //add by zhouerlong 20160414

    /**
     * Ensures that the group children are cancelled immediately when the group summary is cancelled
     * instead of waiting for the notification manager to send all cancels. Otherwise this could
     * lead to flickers.
     *
     * This also ensures that the animation looks nice and only consists of a single disappear
     * animation instead of multiple.
     *
     * @param key the key of the notification was removed
     * @param ranking the current ranking
     */
    private void handleGroupSummaryRemoved(String key,
            RankingMap ranking) {
        Entry entry = mNotificationData.get(key);
        if (entry != null && entry.row != null
                && entry.row.isSummaryWithChildren()) {
            if (entry.notification.getOverrideGroupKey() != null && !entry.row.isDismissed()) {
                // We don't want to remove children for autobundled notifications as they are not
                // always cancelled. We only remove them if they were dismissed by the user.
                return;
            }
            List<ExpandableNotificationRow> notificationChildren =
                    entry.row.getNotificationChildren();
            ArrayList<ExpandableNotificationRow> toRemove = new ArrayList<>(notificationChildren);
            for (int i = 0; i < toRemove.size(); i++) {
                toRemove.get(i).setKeepInParent(true);
                // we need to set this state earlier as otherwise we might generate some weird
                // animations
                toRemove.get(i).setRemoved();
            }
            for (int i = 0; i < toRemove.size(); i++) {
                removeNotification(toRemove.get(i).getStatusBarNotification().getKey(), ranking);
                // we need to ensure that the view is actually properly removed from the viewstate
                // as this won't happen anymore when kept in the parent.
                mStackScroller.removeViewStateForView(toRemove.get(i));
            }
        }
    }

    @Override
    protected void performRemoveNotification(StatusBarNotification n, boolean removeView) {
        Entry entry = mNotificationData.get(n.getKey());
        if (mRemoteInputController.isRemoteInputActive(entry)) {
            mRemoteInputController.removeRemoteInput(entry, null);
        }
        super.performRemoveNotification(n, removeView);
    }

    @Override
    protected void refreshLayout(int layoutDirection) {
        if (mNavigationBarView != null) {
            mNavigationBarView.setLayoutDirection(layoutDirection);
        }
    }

    private void updateNotificationShade() {
        if (mStackScroller == null) return;

        // Do not modify the notifications during collapse.
        if (isCollapsing()) {
            addPostCollapseAction(new Runnable() {
                @Override
                public void run() {
                    updateNotificationShade();
                }
            });
            return;
        }

        ArrayList<Entry> activeNotifications = mNotificationData.getActiveNotifications();
        ArrayList<ExpandableNotificationRow> toShow = new ArrayList<>(activeNotifications.size());
        final int N = activeNotifications.size();
        for (int i=0; i<N; i++) {
            Entry ent = activeNotifications.get(i);
            int vis = ent.notification.getNotification().visibility;

            // Display public version of the notification if we need to redact.
            final boolean hideSensitive =
                    !userAllowsPrivateNotificationsInPublic(ent.notification.getUserId());
            boolean sensitiveNote = vis == Notification.VISIBILITY_PRIVATE;
            boolean sensitivePackage = packageHasVisibilityOverride(ent.notification.getKey());
            boolean sensitive = (sensitiveNote && hideSensitive) || sensitivePackage;
            boolean showingPublic = sensitive && isLockscreenPublicMode();
            if (showingPublic) {
                updatePublicContentView(ent, ent.notification);
            }
            ent.row.setSensitive(sensitive, hideSensitive);
            if (ent.autoRedacted && ent.legacy) {
                // TODO: Also fade this? Or, maybe easier (and better), provide a dark redacted form
                // for legacy auto redacted notifications.
                if (showingPublic) {
                    ent.row.setShowingLegacyBackground(false);
                } else {
                    ent.row.setShowingLegacyBackground(true);
                }
            }
            if (mGroupManager.isChildInGroupWithSummary(ent.row.getStatusBarNotification())) {
                ExpandableNotificationRow summary = mGroupManager.getGroupSummary(
                        ent.row.getStatusBarNotification());
                List<ExpandableNotificationRow> orderedChildren =
                        mTmpChildOrderMap.get(summary);
                if (orderedChildren == null) {
                    orderedChildren = new ArrayList<>();
                    mTmpChildOrderMap.put(summary, orderedChildren);
                }
                orderedChildren.add(ent.row);
            } else {
                toShow.add(ent.row);
            }

        }

        ArrayList<ExpandableNotificationRow> toRemove = new ArrayList<>();
        for (int i=0; i< mStackScroller.getChildCount(); i++) {
            View child = mStackScroller.getChildAt(i);
            if (!toShow.contains(child) && child instanceof ExpandableNotificationRow) {
                toRemove.add((ExpandableNotificationRow) child);
            }
        }

        for (ExpandableNotificationRow remove : toRemove) {
            if (mGroupManager.isChildInGroupWithSummary(remove.getStatusBarNotification())) {
                // we are only transfering this notification to its parent, don't generate an animation
                mStackScroller.setChildTransferInProgress(true);
            }
            if (remove.isSummaryWithChildren()) {
                remove.removeAllChildren();
            }
            mStackScroller.removeView(remove);
            mStackScroller.setChildTransferInProgress(false);
        }

        removeNotificationChildren();

        for (int i=0; i<toShow.size(); i++) {
            View v = toShow.get(i);
            if (v.getParent() == null) {
                makeViewAlpha(v);
                mStackScroller.addView(v);
            }
        }

        addNotificationChildrenAndSort();

        // So after all this work notifications still aren't sorted correctly.
        // Let's do that now by advancing through toShow and mStackScroller in
        // lock-step, making sure mStackScroller matches what we see in toShow.
        int j = 0;
        for (int i = 0; i < mStackScroller.getChildCount(); i++) {
            View child = mStackScroller.getChildAt(i);
            if (!(child instanceof ExpandableNotificationRow)) {
                // We don't care about non-notification views.
                continue;
            }

            ExpandableNotificationRow targetChild = toShow.get(j);
            if (child != targetChild) {
                // Oops, wrong notification at this position. Put the right one
                // here and advance both lists.
                mStackScroller.changeViewPosition(targetChild, i);
            }
            j++;

        }

        // clear the map again for the next usage
        mTmpChildOrderMap.clear();

        updateRowStates();
        updateSpeedbump();
        updateClearAll();
        updateEmptyShadeView();

        updateQsExpansionEnabled();
        mShadeUpdates.check();
        
        //Add for backgroud_reflect-luyingying-2017-09-09-Start*/
        setTextColor();
        //add end
    }
    
    public static void makeViewAlpha(View view) {
        /*PRIZE-add for third app notification(wangyi music) bacground- liufan-2018-01-02-start*/
        if(view instanceof ExpandableNotificationRow){
            ExpandableNotificationRow row = (ExpandableNotificationRow)view;
            StatusBarNotification sbn = row.getStatusBarNotification();
            if(sbn!=null && ("com.qihoo.browser".equals(sbn.getPackageName())//360 browser
                )){
                return ;
            }
            if(sbn!=null && ("com.netease.cloudmusic".equals(sbn.getPackageName())// wangyi music
            )){
                View sv = row.findViewById(R.id.notification_scrim_view2);
                if(sv != null){
                    sv.setVisibility(View.VISIBLE);
                    sv.setBackgroundColor(0xbbffffff);
                    return ;
                }
            }
        }
        /*PRIZE-add for third app notification(wangyi music) bacground- liufan-2018-01-02-end*/
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            if(vg.getId() == R.id.notification_guts){
                return ;
            }
            vg.setBackgroundColor(0x00000000);
            int count = vg.getChildCount();
            for (int i = 0; i < count; i++) {
                View v = vg.getChildAt(i);
                makeViewAlpha(v);
            }
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setTextColor(0xffffffff);
            Drawable[] dd = tv.getCompoundDrawables();
            for (int i = 0; i < dd.length; i++) {
                Drawable d = dd[i];
                if (d != null) {
                    d.mutate();
                    d.setColorFilter(0xffffffff, PorterDuff.Mode.MULTIPLY);
                }
            }
        } else if (view instanceof Button) {
            Button btn = (Button) view;
            btn.setTextColor(0xffffffff);
        } else if (view instanceof ImageView) {
            ImageView iv = (ImageView)view;
            //iv.setColorFilter(0x22ffffff);
        }
    }

    /**
     * Disable QS if device not provisioned.
     * If the user switcher is simple then disable QS during setup because
     * the user intends to use the lock screen user switcher, QS in not needed.
     */
    private void updateQsExpansionEnabled() {
        mNotificationPanel.setQsExpansionEnabled(isDeviceProvisioned()
                && (mUserSetup || mUserSwitcherController == null
                        || !mUserSwitcherController.isSimpleUserSwitcher())
                && ((mDisabled2 & StatusBarManager.DISABLE2_QUICK_SETTINGS) == 0)
                && !ONLY_CORE_APPS);
    }

    private void addNotificationChildrenAndSort() {
        // Let's now add all notification children which are missing
        boolean orderChanged = false;
        for (int i = 0; i < mStackScroller.getChildCount(); i++) {
            View view = mStackScroller.getChildAt(i);
            if (!(view instanceof ExpandableNotificationRow)) {
                // We don't care about non-notification views.
                continue;
            }

            ExpandableNotificationRow parent = (ExpandableNotificationRow) view;
            List<ExpandableNotificationRow> children = parent.getNotificationChildren();
            List<ExpandableNotificationRow> orderedChildren = mTmpChildOrderMap.get(parent);

            for (int childIndex = 0; orderedChildren != null && childIndex < orderedChildren.size();
                    childIndex++) {
                ExpandableNotificationRow childView = orderedChildren.get(childIndex);
                if (children == null || !children.contains(childView)) {
                    parent.addChildNotification(childView, childIndex);
                    mStackScroller.notifyGroupChildAdded(childView);
                }
            }

            // Finally after removing and adding has been beformed we can apply the order.
            orderChanged |= parent.applyChildOrder(orderedChildren);
        }
        if (orderChanged) {
            mStackScroller.generateChildOrderChangedEvent();
        }
    }

    private void removeNotificationChildren() {
        // First let's remove all children which don't belong in the parents
        ArrayList<ExpandableNotificationRow> toRemove = new ArrayList<>();
        for (int i = 0; i < mStackScroller.getChildCount(); i++) {
            View view = mStackScroller.getChildAt(i);
            if (!(view instanceof ExpandableNotificationRow)) {
                // We don't care about non-notification views.
                continue;
            }

            ExpandableNotificationRow parent = (ExpandableNotificationRow) view;
            List<ExpandableNotificationRow> children = parent.getNotificationChildren();
            List<ExpandableNotificationRow> orderedChildren = mTmpChildOrderMap.get(parent);

            if (children != null) {
                toRemove.clear();
                for (ExpandableNotificationRow childRow : children) {
                    if ((orderedChildren == null
                            || !orderedChildren.contains(childRow))
                            && !childRow.keepInParent()) {
                        toRemove.add(childRow);
                    }
                }
                for (ExpandableNotificationRow remove : toRemove) {
                    parent.removeChildNotification(remove);
                    if (mNotificationData.get(remove.getStatusBarNotification().getKey()) == null) {
                        // We only want to add an animation if the view is completely removed
                        // otherwise it's just a transfer
                        mStackScroller.notifyGroupChildRemoved(remove,
                                parent.getChildrenContainer());
                    }
                }
            }
        }
    }

    @Override
    public void addQsTile(ComponentName tile) {
        mQSPanel.getHost().addTile(tile);
    }

    @Override
    public void remQsTile(ComponentName tile) {
        mQSPanel.getHost().removeTile(tile);
    }

    @Override
    public void clickTile(ComponentName tile) {
        mQSPanel.clickTile(tile);
    }

    private boolean packageHasVisibilityOverride(String key) {
        return mNotificationData.getVisibilityOverride(key) == Notification.VISIBILITY_PRIVATE;
    }

    private void updateClearAll() {
        boolean showDismissView =
                mState != StatusBarState.KEYGUARD &&
                mNotificationData.hasActiveClearableNotifications();
        mStackScroller.updateDismissView(showDismissView);
    }

    private void updateEmptyShadeView() {
        boolean showEmptyShade =
                mState != StatusBarState.KEYGUARD &&
                        mNotificationData.getActiveNotifications().size() == 0;
        mNotificationPanel.setShadeEmpty(showEmptyShade);
    }

    private void updateSpeedbump() {
        int speedbumpIndex = -1;
        int currentIndex = 0;
        final int N = mStackScroller.getChildCount();
        for (int i = 0; i < N; i++) {
            View view = mStackScroller.getChildAt(i);
            if (view.getVisibility() == View.GONE || !(view instanceof ExpandableNotificationRow)) {
                continue;
            }
            ExpandableNotificationRow row = (ExpandableNotificationRow) view;
            if (mNotificationData.isAmbient(row.getStatusBarNotification().getKey())) {
                speedbumpIndex = currentIndex;
                break;
            }
            currentIndex++;
        }
        mStackScroller.updateSpeedBumpIndex(speedbumpIndex);
    }

    public static boolean isTopLevelChild(Entry entry) {
        return entry.row.getParent() instanceof NotificationStackScrollLayout;
    }

    @Override
    protected void updateNotifications() {
        mNotificationData.filterAndSort();

        /*PRIZE-resolve the overlapping when add or delete the notification-liufan-2015-9-15-start*/
        NotificationStackScrollLayout.isChildChanged = true;
        updateNotificationShade();
        mIconController.updateNotificationIcons(mNotificationData);
        
        mHandler.removeCallbacks(childChangeRunnable);
        mHandler.postDelayed(childChangeRunnable, 500);
        mHandler.postDelayed(clearOverlayRunnable, 100);
        /*PRIZE-resolve the overlapping when add or delete the notification-liufan-2015-9-15-end*/

    }
	
    /*PRIZE-resolve the overlapping when add or delete the notification-liufan-2015-11-07-start*/
    Runnable childChangeRunnable = new Runnable() {
        @Override
        public void run() {
            /*PRIZE-Modify for bugid: 31652、31896-zhudaopeng-2017-04-10-Start*/
            mStackScroller.cancelAllNotificationRowBg();
            if(NotificationStackScrollLayout.isChildChanged){
                NotificationStackScrollLayout.isChildChanged = false;
            }
            /*PRIZE-Modify for bugid: 31652、31896-zhudaopeng-2017-04-10-End*/
        }
    };

    Runnable clearOverlayRunnable = new Runnable() {
        @Override
        public void run() {
            mStackScroller.getOverlay().clear();
        }
    };

    protected void updateNotificationAgain() {

        mNotificationData.filterAndSort();

        updateNotificationShade();
        mIconController.updateNotificationIcons(mNotificationData);
        
    }
    /*PRIZE-resolve the overlapping when add or delete the notification-liufan-2015-11-07-end*/

    public void requestNotificationUpdate() {
        updateNotifications();
    }


    /*-modify for haokan-liufan-2017-10-10-start-*/
    public void updateNoticeStates(boolean show){
		//setShowLockscreenNotifications(show);
        updateRowStates();
    }
    /*-modify for haokan-liufan-2017-10-10-end-*/

    @Override
    protected void setAreThereNotifications() {

        if (SPEW) {
            final boolean clearable = hasActiveNotifications() &&
                    mNotificationData.hasActiveClearableNotifications();
            Log.d(TAG, "setAreThereNotifications: N=" +
                    mNotificationData.getActiveNotifications().size() + " any=" +
                    hasActiveNotifications() + " clearable=" + clearable);
        }

        final View nlo = mStatusBarView.findViewById(R.id.notification_lights_out);
        final boolean showDot = hasActiveNotifications() && !areLightsOn();
        if (showDot != (nlo.getAlpha() == 1.0f)) {
            if (showDot) {
                nlo.setAlpha(0f);
                nlo.setVisibility(View.VISIBLE);
            }
            nlo.animate()
                .alpha(showDot?1:0)
                .setDuration(showDot?750:250)
                .setInterpolator(new AccelerateInterpolator(2.0f))
                .setListener(showDot ? null : new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator _a) {
                        nlo.setVisibility(View.GONE);
                    }
                })
                .start();
        }

        findAndUpdateMediaNotifications();

        /// M: Support "Operator plugin - Customize Carrier Label for PLMN". @{
        updateCarrierLabelVisibility(false);
        /// M: Support "Operator plugin - Customize Carrier Label for PLMN". @}
    }

    public void findAndUpdateMediaNotifications() {
        boolean metaDataChanged = false;

        synchronized (mNotificationData) {
            ArrayList<Entry> activeNotifications = mNotificationData.getActiveNotifications();
            final int N = activeNotifications.size();

            // Promote the media notification with a controller in 'playing' state, if any.
            Entry mediaNotification = null;
            MediaController controller = null;
            for (int i = 0; i < N; i++) {
                final Entry entry = activeNotifications.get(i);
                if (isMediaNotification(entry)) {
                    final MediaSession.Token token =
                            entry.notification.getNotification().extras
                            .getParcelable(Notification.EXTRA_MEDIA_SESSION);
                    if (token != null) {
                        MediaController aController = new MediaController(mContext, token);
                        if (PlaybackState.STATE_PLAYING ==
                                getMediaControllerPlaybackState(aController)) {
                            if (DEBUG_MEDIA) {
                                Log.v(TAG, "DEBUG_MEDIA: found mediastyle controller matching "
                                        + entry.notification.getKey());
                            }
                            mediaNotification = entry;
                            controller = aController;
                            break;
                        }
                    }
                }
            }
            if (mediaNotification == null) {
                // Still nothing? OK, let's just look for live media sessions and see if they match
                // one of our notifications. This will catch apps that aren't (yet!) using media
                // notifications.

                if (mMediaSessionManager != null) {
                    final List<MediaController> sessions
                            = mMediaSessionManager.getActiveSessionsForUser(
                                    null,
                                    UserHandle.USER_ALL);

                    for (MediaController aController : sessions) {
                        if (PlaybackState.STATE_PLAYING ==
                                getMediaControllerPlaybackState(aController)) {
                            // now to see if we have one like this
                            final String pkg = aController.getPackageName();

                            for (int i = 0; i < N; i++) {
                                final Entry entry = activeNotifications.get(i);
                                if (entry.notification.getPackageName().equals(pkg)) {
                                    if (DEBUG_MEDIA) {
                                        Log.v(TAG, "DEBUG_MEDIA: found controller matching "
                                            + entry.notification.getKey());
                                    }
                                    controller = aController;
                                    mediaNotification = entry;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (controller != null && !sameSessions(mMediaController, controller)) {
                // We have a new media session
                clearCurrentMediaNotification();
                mMediaController = controller;
                mMediaController.registerCallback(mMediaListener);
                mMediaMetadata = mMediaController.getMetadata();
                if (DEBUG_MEDIA) {
                    Log.v(TAG, "DEBUG_MEDIA: insert listener, receive metadata: "
                            + mMediaMetadata);
                }

                if (mediaNotification != null) {
                    mMediaNotificationKey = mediaNotification.notification.getKey();
                    if (DEBUG_MEDIA) {
                        Log.v(TAG, "DEBUG_MEDIA: Found new media notification: key="
                                + mMediaNotificationKey + " controller=" + mMediaController);
                    }
                }
                metaDataChanged = true;
            }
        }

        if (metaDataChanged) {
            updateNotifications();
        }
        updateMediaMetaData(metaDataChanged, true);
    }

    private int getMediaControllerPlaybackState(MediaController controller) {
        if (controller != null) {
            final PlaybackState playbackState = controller.getPlaybackState();
            if (playbackState != null) {
                return playbackState.getState();
            }
        }
        return PlaybackState.STATE_NONE;
    }

    private boolean isPlaybackActive(int state) {
        if (state != PlaybackState.STATE_STOPPED
                && state != PlaybackState.STATE_ERROR
                && state != PlaybackState.STATE_NONE) {
            return true;
        }
        return false;
    }

    private void clearCurrentMediaNotification() {
        mMediaNotificationKey = null;
        mMediaMetadata = null;
        if (mMediaController != null) {
            if (DEBUG_MEDIA) {
                Log.v(TAG, "DEBUG_MEDIA: Disconnecting from old controller: "
                        + mMediaController.getPackageName());
            }
            mMediaController.unregisterCallback(mMediaListener);
        }
        mMediaController = null;
    }

    private boolean sameSessions(MediaController a, MediaController b) {
        if (a == b) return true;
        if (a == null) return false;
        return a.controlsSameSession(b);
    }

    /**
     * Hide the album artwork that is fading out and release its bitmap.
     */
    private Runnable mHideBackdropFront = new Runnable() {
        @Override
        public void run() {
            if (DEBUG_MEDIA) {
                Log.v(TAG, "DEBUG_MEDIA: removing fade layer");
            }
            mBackdropFront.setVisibility(View.INVISIBLE);
            mBackdropFront.animate().cancel();
            mBackdropFront.setImageDrawable(null);
        }
    };

    /*PRIZE-show blur background-liufan-2015-09-04-start*/
    /**
     * Method Description：isShowBlurBgWhenLockscreen
     */
    public void isShowBlurBgWhenLockscreen(boolean anim){
        if(isShowKeyguard){//PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW && || mScreenView == null
            Log.d(PRIZE_TAG,"show with keyguard or screenview");
            return;
        }
        if(isUseHaoKan() && !mNotificationPanel.IS_ShowNotification_WhenShowHaoKan){
            //add for bugid:56647-liufan-2018-5-9-start
            boolean isNeedFullscreenBouncer = mStatusBarKeyguardViewManager != null && mStatusBarKeyguardViewManager.needsFullscreenBouncer();
            if(isNeedFullscreenBouncer){
                NotificationPanelView.debugMagazine("isShowBlurBgWhenLockscreen isNeedFullscreenBouncer = " + isNeedFullscreenBouncer);
                showLockscreenWallpaper(false);
            }
            //add for bugid:56647-liufan-2018-5-9-end
            return ;
        }
        if(mState == StatusBarState.SHADE || (mState == StatusBarState.SHADE_LOCKED && !BLUR_BG_CONTROL)){// || !NotificationPanelView.HaokanShow
            dismissBlurBackAnimation(anim);
            showLockscreenWallpaper(true);
            return ;
        }
        if(!BLUR_BG_CONTROL && mState == StatusBarState.KEYGUARD && isQsExpanded()){
            dismissBlurBackAnimation(anim);
            showLockscreenWallpaper(true);
            return ;
        }
        if(mStatusBarKeyguardViewManager.isBouncerShowing()){
            dismissBlurBackAnimation(anim);
            showLockscreenWallpaper(true);
            return ;
        }
        int N = showNotificationOnKeyguardNums();//mNotificationData.getActiveNotifications().size();
        boolean isShowBlur = isShowLimitByNotifications(N);
		/*prize-public-standard:Changed lock screen-liuweiquan-20151214-start*/
		//if(isShowBlur||(PrizeOption.PRIZE_CHANGED_WALLPAPER&&bChangedWallpaperIsOpen&&!bIntoSuperSavingPower)){// no notification
        if(isShowBlur){// no notification
		/*prize-public-standard:Changed lock screen-liuweiquan-20151214-end*/
            //if(blurValue!=1){
                dismissBlurBackAnimation(anim);
                blurValue = 1;
            //}
            ScrimController.isDismissScrim = false;
            mScrimController.setKeyguardShowing(mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED);
        } else {//have notification
            if(isLocalBg()){
                //if(blurValue!=2){
                    blurValue = 2;
                    setBlurBackBg(1);
                    showBlurBackAnimation(anim);
                //}
            }else{
                //if(blurValue!=3){
                    blurValue = 3;
                    setBlurBackBg(2);
                    showBlurBackAnimation(anim);
                //}
            }
            if(BLUR_BG_CONTROL){//dismiss scrim when show blur bg
                ScrimController.isDismissScrim = true;
            }
        }
        /*PRIZE-add,don't show lockscreen wallpaper when show keyguard-liufan-2016-06-03-start*/
        if(!isShowKeyguard){
            showLockscreenWallpaper(true);
        }
        /*PRIZE-add,don't show lockscreen wallpaper when show keyguard-liufan-2016-06-03-end*/
    }

    public void showLockscreenWallpaper(boolean anim){
        /*-add for haokan-liufan-2017-10-27-start-*/
        //add for bugid:56647-liufan-2018-5-9
        boolean isNeedFullscreenBouncer = mStatusBarKeyguardViewManager != null && mStatusBarKeyguardViewManager.needsFullscreenBouncer();
        if(isUseHaoKan() && !isNeedFullscreenBouncer){
            NotificationPanelView.debugMagazine("not showLockscreenWallpaper when UseHaoKan");
            return ;
        }
        /*-add for haokan-liufan-2017-10-27-end-*/
        if(!anim){
            mBackdropBack.setAlpha(1f);
            mBackdropBack.setVisibility(View.VISIBLE);
            return;
        }
        if(mBackdropBack.getDrawable() != null && mBackdropBack.getVisibility() != View.VISIBLE){
            ValueAnimator showBackdropBackAnimation = ValueAnimator.ofFloat(0f, 1f);
            showBackdropBackAnimation.setDuration(KEYGUARD_CHARGE_ANIMATION_TIME);
            showBackdropBackAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mBackdropBack.setAlpha((Float) animation.getAnimatedValue());
                }
            });
            showBackdropBackAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mBackdropBack.setAlpha(1f);
                    mBackdropBack.setVisibility(View.VISIBLE);
                }
            });
            showBackdropBackAnimation.start();
        }
    }
    
    private int blurValue = -1;
    private ValueAnimator showBlurAnimation;
    private ValueAnimator dismissBlurAnimation;
    
    /**
     * Method Description：show blur background with animation
     */
    public void showBlurBackAnimation(boolean anim){
        if(dismissBlurAnimation != null){
            dismissBlurAnimation.cancel();
            dismissBlurAnimation = null;
        }
        if(showBlurAnimation!=null){
            return;
        }
        if(mBlurBack.getVisibility() == View.VISIBLE){
            return;
        }
        mBlurBack.setVisibility(View.VISIBLE);
        showBlurAnimation = ValueAnimator.ofFloat(0f, 1f);
        showBlurAnimation.setDuration(anim ? KEYGUARD_CHARGE_ANIMATION_TIME : 0);
        showBlurAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBlurBack.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        showBlurAnimation.addListener(new AnimatorListenerAdapter() {
            
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                showBlurAnimation = null;
                mBlurBack.setAlpha(1f);
                mBlurBack.setVisibility(View.VISIBLE);
                /*PRIZE-set LockScreen Wallpaper VISIBLE when show mBlurBack-liufan-2016-06-03-start*/
                mBackdropBack.setVisibility(View.VISIBLE);
                /*PRIZE-set LockScreen Wallpaper VISIBLE when show mBlurBack-liufan-2016-06-03-end*/
                mScrimController.setKeyguardShowing(mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED);
            }
        });
        showBlurAnimation.start();
    }
    
    /**
     * Method Description：dismiss blur background with animation
     */
    public void dismissBlurBackAnimation(boolean anim){
        if(showBlurAnimation != null){
            showBlurAnimation.cancel();
            showBlurAnimation = null;
        }
        if(dismissBlurAnimation!=null){
            return;
        }
        if(mBlurBack.getVisibility() == View.GONE){
            return;
        }
        dismissBlurAnimation = ValueAnimator.ofFloat(1f, 0f);
        dismissBlurAnimation.setDuration(anim ? KEYGUARD_CHARGE_ANIMATION_TIME : 0);
        dismissBlurAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBlurBack.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        dismissBlurAnimation.addListener(new AnimatorListenerAdapter() {
            
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                dismissBlurAnimation = null;
                mBlurBack.setAlpha(0f);
                mBlurBack.setVisibility(View.GONE);
            }
        });
        dismissBlurAnimation.start();
    }
    
    /**
     * Method Description：set blur background when lockscreen:1->set wallpaper to background, 2-> set other wallpaper to background(picture)
     */
    public void setBlurBackBg(int value){
        NotificationPanelView.debugMagazine("setBlurBackBg value : " + value 
            + ", isHaoKanViewShow : " + mNotificationPanel.isHaoKanViewShow());
        recycleBlurWallpaper();
		//update by haokan-liufan-2017-10-16-start
		if(//!NotificationPanelView.USE_VLIFE && !NotificationPanelView.USE_ZOOKING
            //&& 
			isUseHaoKan() && mNotificationPanel.isHaoKanViewShow()){
            Bitmap bitmap = null;
            //bitmap = mNotificationPanel.getCurrentImage();
            //if(bitmap!=null && !bitmap.isRecycled()){
                BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), blur(bitmap));
                bd.setAlpha(200);//180
                mBlurBack.setBackground(bd);
            //}
            return ;
		}
		//update by haokan-liufan-2017-10-16-end
        if(value == 1){
            setBlurBackBgBySystemWallpaper();
        }else if(value == 2){
            //update scrim_bg-2017-12-18-liufan
            setBlurBackBgByOtherWallpaper();
            //BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), blur(null));
            //bd.setAlpha(200);
            //mBlurBack.setBackground(bd);
        }
    }

    public void setBlurBackVisibility(int visible){
        NotificationPanelView.debugMagazine("setBlurBackVisibility visible = " + visible);
        mBlurBack.setVisibility(visible);
        mBlurBack.setAlpha(1f);
    }
    
    /**
     * Method Description：set wallpaper to background
     */
    public void setBlurBackBgBySystemWallpaper(){
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), getBlurWallpaper());
        //BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), blur(null));//update scrim_bg-2017-12-18-liufan
        //bd.setAlpha(200);
        mBlurBack.setBackground(bd);
    }
    
    /**
     * Method Description：set other picture to background
     */
    public void setBlurBackBgByOtherWallpaper(){
        Bitmap bitmap = null;
        Drawable d = mBackdropBack.getDrawable();
        if(d instanceof BitmapDrawable){
            BitmapDrawable bd = (BitmapDrawable)d;
            bitmap = bd.getBitmap();
        }
        if(d instanceof LockscreenWallpaper.WallpaperDrawable){
            LockscreenWallpaper.WallpaperDrawable wd = (LockscreenWallpaper.WallpaperDrawable)d;
            if(wd != null) bitmap = wd.getConstantState().getBackground();
        }
        /*PRIZE-add for haokan-liufan-2017-10-16-start*/
        if(//!NotificationPanelView.USE_VLIFE && !NotificationPanelView.USE_ZOOKING 
                //&& 
				isUseHaoKan() && mNotificationPanel.isHaoKanViewShow()){
            //bitmap = mNotificationPanel.getCurrentImage();

            BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), blur(null));
            mBlurBack.setBackground(bd);
            return ;
        }
        /*PRIZE-add for haokan-liufan-2017-10-16-end*/

        /*if(SUPPORT_KEYGUARD_WALLPAPER){
            String dataPath = mContext.getFilesDir().getPath();
            File f = new File(dataPath ,"KeyguardWallpaper.png");
            if (f.exists()) {
                bitmap = convertToBitmap(f.getPath());
            } else{
                String strPath = Settings.System.getString(mContext.getContentResolver(),KEYGUARD_WALLPAPER_URI);
                if(strPath != null ){
                    File file = new File(strPath);
                    if(file.exists()){
                        bitmap = convertToBitmap(strPath);
                    }
                }
            }
        }*/
        if(bitmap!=null){
            BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), blur(bitmap));
            mBlurBack.setBackground(bd);
        }
    }
    
    /**
     * Method Description：weather show the local wallpaper 
     */
    public boolean isLocalBg(){
        boolean isLocal = true;
        if(mBackdropBack != null && mBackdropBack.getDrawable() != null && mBackdrop.getVisibility() == View.VISIBLE){
            isLocal = false;
        }
        return isLocal ;
    }
    /*PRIZE-show the blur background-liufan-2015-09-04-end*/
    
    /**
     * Refresh or remove lockscreen artwork from media metadata or the lockscreen wallpaper.
     */
    public void updateMediaMetaData(boolean metaDataChanged, boolean allowEnterAnimation) {
        Trace.beginSection("PhoneStatusBar#updateMediaMetaData");
        if (!SHOW_LOCKSCREEN_MEDIA_ARTWORK) {
            Trace.endSection();
            return;
        }

        if (mBackdrop == null) {
            Trace.endSection();
            return; // called too early
        }

        if (mLaunchTransitionFadingAway) {
            mBackdrop.setVisibility(View.INVISIBLE);
            Trace.endSection();
            return;
        }

        if (DEBUG_MEDIA) {
            Log.v(TAG, "DEBUG_MEDIA: updating album art for notification " + mMediaNotificationKey
                    + " metadata=" + mMediaMetadata
                    + " metaDataChanged=" + metaDataChanged
                    + " state=" + mState);
        }

        Drawable artworkDrawable = null;
        /*if (mMediaMetadata != null) {
            Bitmap artworkBitmap = null;
            artworkBitmap = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
            if (artworkBitmap == null) {
                artworkBitmap = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
                // might still be null
            }
            if (artworkBitmap != null) {
                artworkDrawable = new BitmapDrawable(mBackdropBack.getResources(), artworkBitmap);
            }
        }*/
        /*PRIZE set lockscreen wallpaper liyao-2015-07-22-start*/
        //add for bugid:56647-liufan-2018-5-9-start
        boolean isNeedFullscreenBouncer = mStatusBarKeyguardViewManager != null && mStatusBarKeyguardViewManager.needsFullscreenBouncer();
        boolean useMagazine = isUseHaoKan() && !isNeedFullscreenBouncer;
        NotificationPanelView.debugMagazine("updateMediaMetaData !useMagazine : "+!useMagazine
                +", !isNeedFullscreenBouncer = "+ !isNeedFullscreenBouncer);
        //add for bugid:56647-liufan-2018-5-9-end
        boolean allowWhenShade = false;
        Bitmap artworkBitmap = null;
        File f = null;
        /*prize-public-standard:Changed lock screen-liuweiquan-20151210-start*/	    
        if(PrizeOption.PRIZE_CHANGED_WALLPAPER&&bChangedWallpaperIsOpen&&!bIntoSuperSavingPower
            && artworkDrawable == null
			&& !useMagazine){
            String kgPath = sChangedWallpaperPath;
            Log.d("kgPath","kgPath----------->"+kgPath);
            if(!TextUtils.isEmpty(kgPath) && !"-1".equals(kgPath)){
                f = new File(kgPath); // changed keyguard wallpaper
                if (f.exists()) {
                    artworkBitmap = convertToBitmap(f.getPath());
                } 
            }
            if (artworkBitmap != null) {
                artworkDrawable = new BitmapDrawable(mBackdropBack.getResources(), artworkBitmap);
            }
        /*prize-public-standard:Changed lock screen-liuweiquan-20151210-end*/
        }
        if (ENABLE_LOCKSCREEN_WALLPAPER && artworkDrawable == null
            && !useMagazine) {

            Bitmap lockWallpaper = mLockscreenWallpaper.getBitmap();
            if (lockWallpaper != null) {
                artworkDrawable = new LockscreenWallpaper.WallpaperDrawable(
                        mBackdropBack.getResources(), lockWallpaper);
                // We're in the SHADE mode on the SIM screen - yet we still need to show
                // the lockscreen wallpaper in that mode.
                allowWhenShade = mStatusBarKeyguardViewManager != null
                        && mStatusBarKeyguardViewManager.isShowing();
            }

        }
        if(artworkDrawable == null
            && !useMagazine){
            File file = new File("/system/lockscreen.png");
            if(file.exists()) {
                artworkBitmap = convertToBitmap("/system/lockscreen.png");
                if (artworkBitmap != null) {
                    artworkDrawable = new BitmapDrawable(mBackdropBack.getResources(), artworkBitmap);
                }
            } else {
                Log.d(TAG,"default KeyguardWallpaper file not exists");
            }
        }
        if(artworkDrawable == null
            && !useMagazine){
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
            Drawable wallpaperDrawable = wallpaperManager.getDrawable();
            artworkBitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
            artworkDrawable = new BitmapDrawable(mBackdropBack.getResources(), artworkBitmap);
            Log.d(TAG,"default KeyguardWallpaper file not exists, user WallPaper");
        }

        boolean hideBecauseOccluded = mStatusBarKeyguardViewManager != null
                && mStatusBarKeyguardViewManager.isOccluded();

        final boolean hasArtwork = artworkDrawable != null;

        if ((hasArtwork || DEBUG_MEDIA_FAKE_ARTWORK)
                && (mState != StatusBarState.SHADE || allowWhenShade)
                && mFingerprintUnlockController.getMode()
                        != FingerprintUnlockController.MODE_WAKE_AND_UNLOCK_PULSING
                && !hideBecauseOccluded) {
            // time to show some art!
            if (mBackdrop.getVisibility() != View.VISIBLE) {
                mBackdrop.setVisibility(View.VISIBLE);
                if (allowEnterAnimation) {
                    mBackdrop.setAlpha(SRC_MIN_ALPHA);
                    mBackdrop.animate().alpha(1f);
                } else {
                    mBackdrop.animate().cancel();
                    mBackdrop.setAlpha(1f);
                }
                mStatusBarWindowManager.setBackdropShowing(true);
                metaDataChanged = true;
                if (DEBUG_MEDIA) {
                    Log.v(TAG, "DEBUG_MEDIA: Fading in album artwork");
                }
            }
            if (metaDataChanged) {
                /*PRIZE-set LockScreen Wallpaper null-liufan-2016-06-03-start*/
                mBackdropBack.setImageBitmap(null);
                /*PRIZE-set LockScreen Wallpaper null-liufan-2016-06-03-end*/
                if (mBackdropBack.getDrawable() != null) {
                    Drawable drawable =
                            mBackdropBack.getDrawable().getConstantState()
                                    .newDrawable(mBackdropFront.getResources()).mutate();
                    mBackdropFront.setImageDrawable(drawable);
                    if (mScrimSrcModeEnabled) {
                        mBackdropFront.getDrawable().mutate().setXfermode(mSrcOverXferMode);
                    }
                    mBackdropFront.setAlpha(1f);
                    mBackdropFront.setVisibility(View.VISIBLE);
                } else {
                    mBackdropFront.setVisibility(View.INVISIBLE);
                }

                if (DEBUG_MEDIA_FAKE_ARTWORK) {
                    final int c = 0xFF000000 | (int)(Math.random() * 0xFFFFFF);
                    Log.v(TAG, String.format("DEBUG_MEDIA: setting new color: 0x%08x", c));
                    mBackdropBack.setBackgroundColor(0xFFFFFFFF);
                    mBackdropBack.setImageDrawable(new ColorDrawable(c));
                } else {
                    mBackdropBack.setImageDrawable(artworkDrawable);
                }
                if (mScrimSrcModeEnabled) {
                    mBackdropBack.getDrawable().mutate().setXfermode(mSrcXferMode);
                }

                if (mBackdropFront.getVisibility() == View.VISIBLE) {
                    if (DEBUG_MEDIA) {
                        Log.v(TAG, "DEBUG_MEDIA: Crossfading album artwork from "
                                + mBackdropFront.getDrawable()
                                + " to "
                                + mBackdropBack.getDrawable());
                    }
                    mBackdropFront.animate()
                            .setDuration(250)
                            .alpha(0f).withEndAction(mHideBackdropFront);
                }
            }
        } else {
            // need to hide the album art, either because we are unlocked or because
            // the metadata isn't there to support it
            if (mBackdrop.getVisibility() != View.GONE) {
                /*PRIZE-add for haokan-2017-10-30-start*/
                if(isUseHaoKan()){
                    //if(!isBouncerShowing() && !mNotificationPanel.isWillOccluded()){
                    if(!isBouncerShowing()){
                        mBackdrop.setVisibility(View.GONE);
                        mBackdropFront.animate().cancel();
                        mBackdropBack.animate().cancel();
                        mHandler.post(mHideBackdropFront);
                        NotificationPanelView.debugMagazine("set mBackdrop.visibile immediately gone");
                    }else{
                        NotificationPanelView.debugMagazine("beacuse BouncerShowing mBackdrop.visibile don't set gone");
                    }
                    return;
                }
                /*PRIZE-add for haokan-2017-10-30-end*/
                if (DEBUG_MEDIA) {
                    Log.v(TAG, "DEBUG_MEDIA: Fading out album artwork");
                }
                if (mFingerprintUnlockController.getMode()
                        == FingerprintUnlockController.MODE_WAKE_AND_UNLOCK_PULSING
                        || hideBecauseOccluded) {

                    // We are unlocking directly - no animation!
                    mBackdrop.setVisibility(View.GONE);
                    mBackdropBack.setImageDrawable(null);
                    mStatusBarWindowManager.setBackdropShowing(false);
                } else {
                    mStatusBarWindowManager.setBackdropShowing(false);
                    mBackdrop.animate()
                            // Never let the alpha become zero - otherwise the RenderNode
                            // won't draw anything and uninitialized memory will show through
                            // if mScrimSrcModeEnabled. Note that 0.001 is rounded down to 0 in
                            // libhwui.
                            .alpha(SRC_MIN_ALPHA)
                            .setInterpolator(Interpolators.ACCELERATE_DECELERATE)
                            .setDuration(300)
                            .setStartDelay(0)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    mBackdrop.setVisibility(View.GONE);
                                    mBackdropFront.animate().cancel();
                                    mBackdropBack.setImageDrawable(null);
                                    mHandler.post(mHideBackdropFront);
                                }
                            });
                    if (mKeyguardFadingAway) {
                        mBackdrop.animate()

                                // Make it disappear faster, as the focus should be on the activity
                                // behind.
                                .setDuration(mKeyguardFadingAwayDuration / 2)
                                .setStartDelay(mKeyguardFadingAwayDelay)
                                .setInterpolator(Interpolators.LINEAR)
                                .start();
                    }
                }
            }else{
                NotificationPanelView.debugMagazine("mBackdrop has gone");
            }
        }
        /*PRIZE-blur background-liufan-2015-09-04-start*/
        isShowBlurBgWhenLockscreen(false);
        /*PRIZE-blur background-liufan-2015-09-04-end*/
		setTextColor();
        Trace.endSection();
    }

    private void updateReportRejectedTouchVisibility() {
        if (mReportRejectedTouch == null) {
            return;
        }
        mReportRejectedTouch.setVisibility(mState == StatusBarState.KEYGUARD
                && mFalsingManager.isReportingEnabled() ? View.VISIBLE : View.INVISIBLE);
    }

    protected int adjustDisableFlags(int state) {
        if (!mLaunchTransitionFadingAway && !mKeyguardFadingAway
                && (mExpandedVisible || mBouncerShowing || mWaitingForKeyguardExit)) {
            state |= StatusBarManager.DISABLE_NOTIFICATION_ICONS;
            state |= StatusBarManager.DISABLE_SYSTEM_INFO;
        }
        if (mNetworkController != null && EncryptionHelper.IS_DATA_ENCRYPTED) {
            if (mNetworkController.hasEmergencyCryptKeeperText()) {
                state |= StatusBarManager.DISABLE_NOTIFICATION_ICONS;
            }
            if (!mNetworkController.isRadioOn()) {
                state |= StatusBarManager.DISABLE_SYSTEM_INFO;
            }
        }
        return state;
    }

    /**
     * State is one or more of the DISABLE constants from StatusBarManager.
     */
    @Override
    public void disable(int state1, int state2, boolean animate) {
        animate &= mStatusBarWindowState != WINDOW_STATE_HIDDEN;
        mDisabledUnmodified1 = state1;
        mDisabledUnmodified2 = state2;
        state1 = adjustDisableFlags(state1);
        final int old1 = mDisabled1;
        final int diff1 = state1 ^ old1;
        mDisabled1 = state1;

        final int old2 = mDisabled2;
        final int diff2 = state2 ^ old2;
        mDisabled2 = state2;

        if (DEBUG) {
            Log.d(TAG, String.format("disable1: 0x%08x -> 0x%08x (diff1: 0x%08x)",
                old1, state1, diff1));
            Log.d(TAG, String.format("disable2: 0x%08x -> 0x%08x (diff2: 0x%08x)",
                old2, state2, diff2));
        }

        StringBuilder flagdbg = new StringBuilder();
        flagdbg.append("disable: < ");
        flagdbg.append(((state1 & StatusBarManager.DISABLE_EXPAND) != 0) ? "EXPAND" : "expand");
        flagdbg.append(((diff1  & StatusBarManager.DISABLE_EXPAND) != 0) ? "* " : " ");
        flagdbg.append(((state1 & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) ? "ICONS" : "icons");
        flagdbg.append(((diff1  & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) ? "* " : " ");
        flagdbg.append(((state1 & StatusBarManager.DISABLE_NOTIFICATION_ALERTS) != 0) ? "ALERTS" : "alerts");
        flagdbg.append(((diff1  & StatusBarManager.DISABLE_NOTIFICATION_ALERTS) != 0) ? "* " : " ");
        flagdbg.append(((state1 & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) ? "SYSTEM_INFO" : "system_info");
        flagdbg.append(((diff1  & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) ? "* " : " ");
        flagdbg.append(((state1 & StatusBarManager.DISABLE_BACK) != 0) ? "BACK" : "back");
        flagdbg.append(((diff1  & StatusBarManager.DISABLE_BACK) != 0) ? "* " : " ");
        flagdbg.append(((state1 & StatusBarManager.DISABLE_HOME) != 0) ? "HOME" : "home");
        flagdbg.append(((diff1  & StatusBarManager.DISABLE_HOME) != 0) ? "* " : " ");
        flagdbg.append(((state1 & StatusBarManager.DISABLE_RECENT) != 0) ? "RECENT" : "recent");
        flagdbg.append(((diff1  & StatusBarManager.DISABLE_RECENT) != 0) ? "* " : " ");
        flagdbg.append(((state1 & StatusBarManager.DISABLE_CLOCK) != 0) ? "CLOCK" : "clock");
        flagdbg.append(((diff1  & StatusBarManager.DISABLE_CLOCK) != 0) ? "* " : " ");
        flagdbg.append(((state1 & StatusBarManager.DISABLE_SEARCH) != 0) ? "SEARCH" : "search");
        flagdbg.append(((diff1  & StatusBarManager.DISABLE_SEARCH) != 0) ? "* " : " ");
        flagdbg.append(((state2 & StatusBarManager.DISABLE2_QUICK_SETTINGS) != 0) ? "QUICK_SETTINGS"
                : "quick_settings");
        flagdbg.append(((diff2  & StatusBarManager.DISABLE2_QUICK_SETTINGS) != 0) ? "* " : " ");
        flagdbg.append(">");
        Log.d(TAG, flagdbg.toString());

        if ((diff1 & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) {
            if ((state1 & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) {
                mIconController.hideSystemIconArea(animate);
                if (mStatusBarPlmnPlugin != null) {
                    mStatusBarPlmnPlugin.setPlmnVisibility(View.GONE);
                }
            } else {
                mIconController.showSystemIconArea(animate);
                if (mStatusBarPlmnPlugin != null) {
                    mStatusBarPlmnPlugin.setPlmnVisibility(View.VISIBLE);
                }
            }
        }

        if ((diff1 & StatusBarManager.DISABLE_CLOCK) != 0) {
            boolean visible = (state1 & StatusBarManager.DISABLE_CLOCK) == 0;
            mIconController.setClockVisibility(visible);
        }
        if ((diff1 & StatusBarManager.DISABLE_EXPAND) != 0) {
            if ((state1 & StatusBarManager.DISABLE_EXPAND) != 0) {
                animateCollapsePanels();
            }
        }

        if ((diff1 & (StatusBarManager.DISABLE_HOME
                        | StatusBarManager.DISABLE_RECENT
                        | StatusBarManager.DISABLE_BACK
                        | StatusBarManager.DISABLE_SEARCH)) != 0) {
            // the nav bar will take care of these
            if (mNavigationBarView != null) mNavigationBarView.setDisabledFlags(state1);

            if ((state1 & StatusBarManager.DISABLE_RECENT) != 0) {
                // close recents if it's visible
                mHandler.removeMessages(MSG_HIDE_RECENT_APPS);
                mHandler.sendEmptyMessage(MSG_HIDE_RECENT_APPS);
            }
        }

        if ((diff1 & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
            if ((state1 & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
                mIconController.hideNotificationIconArea(animate);
            } else {
                mIconController.showNotificationIconArea(animate);
            }
        }

        if ((diff1 & StatusBarManager.DISABLE_NOTIFICATION_ALERTS) != 0) {
            mDisableNotificationAlerts =
                    (state1 & StatusBarManager.DISABLE_NOTIFICATION_ALERTS) != 0;
            mHeadsUpObserver.onChange(true);
        }

        if ((diff2 & StatusBarManager.DISABLE2_QUICK_SETTINGS) != 0) {
            updateQsExpansionEnabled();
        }
    }

    /**
     * Reapplies the disable flags as last requested by StatusBarManager.
     *
     * This needs to be called if state used by {@link #adjustDisableFlags} changes.
     */
    private void recomputeDisableFlags(boolean animate) {
        disable(mDisabledUnmodified1, mDisabledUnmodified2, animate);
    }

    @Override
    protected BaseStatusBar.H createHandler() {
        return new PhoneStatusBar.H();
    }

    @Override
    public void startActivity(Intent intent, boolean dismissShade) {
        startActivityDismissingKeyguard(intent, false, dismissShade);
    }

    @Override
    public void startActivity(Intent intent, boolean dismissShade, Callback callback) {
        startActivityDismissingKeyguard(intent, false, dismissShade, callback);
    }

    @Override
    public void preventNextAnimation() {
        overrideActivityPendingAppTransition(true /* keyguardShowing */);
    }

    public void setQsExpanded(boolean expanded) {
        mStatusBarWindowManager.setQsExpanded(expanded);
        mKeyguardStatusView.setImportantForAccessibility(expanded
                ? View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                : View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
    }

    public boolean isGoingToNotificationShade() {
        return mLeaveOpenOnKeyguardHide;
    }

    public boolean isQsExpanded() {
        return mNotificationPanel.isQsExpanded();
    }

    public boolean isWakeUpComingFromTouch() {
        return mWakeUpComingFromTouch;
    }

    public boolean isFalsingThresholdNeeded() {
        return getBarState() == StatusBarState.KEYGUARD;
    }

    public boolean isDozing() {
        return mDozing;
    }

    @Override  // NotificationData.Environment
    public String getCurrentMediaNotificationKey() {
        return mMediaNotificationKey;
    }

    public boolean isScrimSrcModeEnabled() {
        return mScrimSrcModeEnabled;
    }

    /**
     * To be called when there's a state change in StatusBarKeyguardViewManager.
     */
    public void onKeyguardViewManagerStatesUpdated() {
        logStateToEventlog();
    }

    @Override  // UnlockMethodCache.OnUnlockMethodChangedListener
    public void onUnlockMethodStateChanged() {
        logStateToEventlog();
    }

    @Override
    public void onHeadsUpPinnedModeChanged(boolean inPinnedMode) {
        if (inPinnedMode) {
            mStatusBarWindowManager.setHeadsUpShowing(true);
            mStatusBarWindowManager.setForceStatusBarVisible(true);
            if (mNotificationPanel.isFullyCollapsed()) {
                // We need to ensure that the touchable region is updated before the window will be
                // resized, in order to not catch any touches. A layout will ensure that
                // onComputeInternalInsets will be called and after that we can resize the layout. Let's
                // make sure that the window stays small for one frame until the touchableRegion is set.
                mNotificationPanel.requestLayout();
                mStatusBarWindowManager.setForceWindowCollapsed(true);
                mNotificationPanel.post(new Runnable() {
                    @Override
                    public void run() {
                        mStatusBarWindowManager.setForceWindowCollapsed(false);
                    }
                });
            }
        } else {
            if (!mNotificationPanel.isFullyCollapsed() || mNotificationPanel.isTracking()) {
                // We are currently tracking or is open and the shade doesn't need to be kept
                // open artificially.
                mStatusBarWindowManager.setHeadsUpShowing(false);
            } else {
                // we need to keep the panel open artificially, let's wait until the animation
                // is finished.
                mHeadsUpManager.setHeadsUpGoingAway(true);
                mStackScroller.runAfterAnimationFinished(new Runnable() {
                    @Override
                    public void run() {
                        if (!mHeadsUpManager.hasPinnedHeadsUp()) {
                            mStatusBarWindowManager.setHeadsUpShowing(false);
                            mHeadsUpManager.setHeadsUpGoingAway(false);
                        }
                        removeRemoteInputEntriesKeptUntilCollapsed();
                    }
                });
            }
        }
    }

    @Override
    public void onHeadsUpPinned(ExpandableNotificationRow headsUp) {
        dismissVolumeDialog();
    }

    @Override
    public void onHeadsUpUnPinned(ExpandableNotificationRow headsUp) {
    }

    @Override
    public void onHeadsUpStateChanged(Entry entry, boolean isHeadsUp) {
        if (!isHeadsUp && mHeadsUpEntriesToRemoveOnSwitch.contains(entry)) {
            removeNotification(entry.key, mLatestRankingMap);
            mHeadsUpEntriesToRemoveOnSwitch.remove(entry);
            if (mHeadsUpEntriesToRemoveOnSwitch.isEmpty()) {
                mLatestRankingMap = null;
            }
        } else {
            updateNotificationRanking(null);
        }

    }

    @Override
    protected void updateHeadsUp(String key, Entry entry, boolean shouldPeek,
            boolean alertAgain) {
        final boolean wasHeadsUp = isHeadsUp(key);
        Log.d(TICKER_TAG, "updateHeadsUp wasHeadsUp = " + wasHeadsUp + ", shouldPeek = " + shouldPeek + ", alertAgain = " + alertAgain + ", key = " + key);
        if (wasHeadsUp) {
            if (!shouldPeek) {
                // We don't want this to be interrupting anymore, lets remove it
                mHeadsUpManager.removeNotification(key, false /* ignoreEarliestRemovalTime */);
            } else {
                mHeadsUpManager.updateNotification(entry, alertAgain);
                //prize modify by xiarui 2018-04-12 @{
                if (PRIZE_HEADS_UP_STYLE) {
                    showNewStyleHeadsUp(entry);
                }
                //@}
            }
        /// M: Fix ALPS02328815, for update heads up, also needs mUseHeadsUp is true.
        /// TODO:: Maybe no need this CR
        } else if (mUseHeadsUp && shouldPeek && alertAgain) {
            /* prize-add-split screen-liyongli-20170613-start */
            if( sendOpenQQFloatIconMsg(entry.notification,  entry.row) )
                return;
            /* prize-add-split screen-liyongli-20170613-end */

            // This notification was updated to be a heads-up, show it!
            //prize modify by xiarui 2018-04-12 @{
            if (PRIZE_HEADS_UP_STYLE) {
                showNewStyleHeadsUp(entry);
                mHeadsUpManager.showNotification(entry);
            } else {
                mHeadsUpManager.showNotification(entry);
            }
            //@}
        }
    }

    @Override
    protected void setHeadsUpUser(int newUserId) {
        if (mHeadsUpManager != null) {
            mHeadsUpManager.setUser(newUserId);
        }
    }

    public boolean isHeadsUp(String key) {
        return mHeadsUpManager.isHeadsUp(key);
    }

    @Override
    protected boolean isSnoozedPackage(StatusBarNotification sbn) {
        return mHeadsUpManager.isSnoozed(sbn.getPackageName());
    }

    public boolean isKeyguardCurrentlySecure() {
        return !mUnlockMethodCache.canSkipBouncer();
    }

    public void setPanelExpanded(boolean isExpanded) {
        mStatusBarWindowManager.setPanelExpanded(isExpanded);

        if (isExpanded && getBarState() != StatusBarState.KEYGUARD) {
            if (DEBUG) {
                Log.v(TAG, "clearing notification effects from setPanelExpanded");
            }
            clearNotificationEffects();
        }

        if (!isExpanded) {
            removeRemoteInputEntriesKeptUntilCollapsed();
        }
    }

    private void removeRemoteInputEntriesKeptUntilCollapsed() {
        for (int i = 0; i < mRemoteInputEntriesToRemoveOnCollapse.size(); i++) {
            Entry entry = mRemoteInputEntriesToRemoveOnCollapse.valueAt(i);
            mRemoteInputController.removeRemoteInput(entry, null);
            removeNotification(entry.key, mLatestRankingMap);
        }
        mRemoteInputEntriesToRemoveOnCollapse.clear();
    }

    public void onScreenTurnedOff() {
        mFalsingManager.onScreenOff();
    }

    /**
     * All changes to the status bar and notifications funnel through here and are batched.
     */
    private class H extends BaseStatusBar.H {
        @Override
        public void handleMessage(Message m) {
            super.handleMessage(m);
            switch (m.what) {
                case MSG_OPEN_NOTIFICATION_PANEL:
                    animateExpandNotificationsPanel();
                    break;
                case MSG_OPEN_SETTINGS_PANEL:
                    animateExpandSettingsPanel((String) m.obj);
                    break;
                case MSG_CLOSE_PANELS:
                    animateCollapsePanels();
                    break;
                case MSG_LAUNCH_TRANSITION_TIMEOUT:
                    onLaunchTransitionTimeout();
                    break;
            }
        }
    }

    @Override
    public void maybeEscalateHeadsUp() {
        Collection<HeadsUpManager.HeadsUpEntry> entries = mHeadsUpManager.getAllEntries();
        for (HeadsUpManager.HeadsUpEntry entry : entries) {
            final StatusBarNotification sbn = entry.entry.notification;
            final Notification notification = sbn.getNotification();
            if (notification.fullScreenIntent != null) {
                if (DEBUG) {
                    Log.d(TAG, "converting a heads up to fullScreen " + sbn.getKey());
                }
                try {
                    EventLog.writeEvent(EventLogTags.SYSUI_HEADS_UP_ESCALATION,
                            sbn.getKey());
                    notification.fullScreenIntent.send();
                    entry.entry.notifyFullScreenIntentLaunched();
                } catch (PendingIntent.CanceledException e) {
                }
            }
        }
        mHeadsUpManager.releaseAllImmediately();
        
        //prize add by xiarui 2018-05-29 when back to keyguard from EmergencyDialer hide ticker view @{
        if (PRIZE_HEADS_UP_STYLE && mTickerController != null) {
            Log.d(TICKER_TAG, "maybeEscalateHeadsUp animateTickerCollapse");
            mTickerController.animateTickerCollapse();
        }
        //@}
    }

    /**
     * Called for system navigation gestures. First action opens the panel, second opens
     * settings. Down action closes the entire panel.
     */
    @Override
    public void handleSystemNavigationKey(int key) {
        if (SPEW) Log.d(TAG, "handleSystemNavigationKey: " + key);
        if (!panelsEnabled() || !mKeyguardMonitor.isDeviceInteractive()
                || mKeyguardMonitor.isShowing() && !mKeyguardMonitor.isOccluded()) {
            return;
        }

        // Panels are not available in setup
        if (!mUserSetup) return;

        if (KeyEvent.KEYCODE_SYSTEM_NAVIGATION_UP == key) {
            MetricsLogger.action(mContext, MetricsEvent.ACTION_SYSTEM_NAVIGATION_KEY_UP);
            mNotificationPanel.collapse(false /* delayed */, 1.0f /* speedUpFactor */);
        } else if (KeyEvent.KEYCODE_SYSTEM_NAVIGATION_DOWN == key) {
            MetricsLogger.action(mContext, MetricsEvent.ACTION_SYSTEM_NAVIGATION_KEY_DOWN);
            if (mNotificationPanel.isFullyCollapsed()) {
                mNotificationPanel.expand(true /* animate */);
                MetricsLogger.count(mContext, NotificationPanelView.COUNTER_PANEL_OPEN, 1);
            } else if (!mNotificationPanel.isInSettings() && !mNotificationPanel.isExpanding()){
                mNotificationPanel.flingSettings(0 /* velocity */, true /* expand */);
                MetricsLogger.count(mContext, NotificationPanelView.COUNTER_PANEL_OPEN_QS, 1);
            }
        }

    }

    boolean panelsEnabled() {
        return (mDisabled1 & StatusBarManager.DISABLE_EXPAND) == 0 && !ONLY_CORE_APPS;
    }

    void makeExpandedVisible(boolean force) {
        if (SPEW) Log.d(TAG, "Make expanded visible: expanded visible=" + mExpandedVisible);
        if (!force && (mExpandedVisible || !panelsEnabled())) {
            return;
        }

        mExpandedVisible = true;

        /// M: Support "Operator plugin - Customize Carrier Label for PLMN". @{
        updateCarrierLabelVisibility(true);
        /// M: Support "Operator plugin - Customize Carrier Label for PLMN". @}

        // Expand the window to encompass the full screen in anticipation of the drag.
        // This is only possible to do atomically because the status bar is at the top of the screen!
        mStatusBarWindowManager.setPanelVisible(true);

        visibilityChanged(true);
        mWaitingForKeyguardExit = false;
        recomputeDisableFlags(!force /* animate */);
        setInteracting(StatusBarManager.WINDOW_STATUS_BAR, true);
    }

    /*PRIZE-add for brightness controller- liufan-2016-06-29-start*/
    public void collapsePanels(boolean anim){
        if(mState == StatusBarState.SHADE){
            collapsePanels(CommandQueue.FLAG_EXCLUDE_NONE,false,false,1f,anim);
        }
    }

    public void collapseQsSetting(){
        if(mState == StatusBarState.KEYGUARD){
            mNotificationPanel.animateCloseQs();
        } else if(mState == StatusBarState.SHADE_LOCKED){
            goToKeyguard();
        }
    }

    public boolean isShadeState(){
        return mState == StatusBarState.SHADE;
    }

    public void collapsePanels(int flags, boolean force, boolean delayed,
            float speedUpFactor, boolean anim) {
        if (!force &&
                (mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED)) {
            runPostCollapseRunnables();
            return;
        }
        if (SPEW) {
            Log.d(TAG, "animateCollapse():"
                    + " mExpandedVisible=" + mExpandedVisible
                    + " flags=" + flags);
        }

        if ((flags & CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL) == 0) {
            if (!mHandler.hasMessages(MSG_HIDE_RECENT_APPS)) {
                mHandler.removeMessages(MSG_HIDE_RECENT_APPS);
                mHandler.sendEmptyMessage(MSG_HIDE_RECENT_APPS);
            }
        }

        if (mStatusBarWindow != null) {
            // release focus immediately to kick off focus change transition
            mStatusBarWindowManager.setStatusBarFocusable(false);

            mStatusBarWindow.cancelExpandHelper();
            mStatusBarView.collapsePanel(anim /* animate */, delayed, speedUpFactor);
        }
    }
    /*PRIZE-add for brightness controller- liufan-2016-06-29-end*/

    public void animateCollapsePanels() {
        animateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE);
    }

    private final Runnable mAnimateCollapsePanels = new Runnable() {
        @Override
        public void run() {
            animateCollapsePanels();
        }
    };

    public void postAnimateCollapsePanels() {
        mHandler.post(mAnimateCollapsePanels);
    }

    public void postAnimateOpenPanels() {
        mHandler.sendEmptyMessage(MSG_OPEN_SETTINGS_PANEL);
    }

    @Override
    public void animateCollapsePanels(int flags) {
        animateCollapsePanels(flags, false /* force */, false /* delayed */,
                1.0f /* speedUpFactor */);
    }

    @Override
    public void animateCollapsePanels(int flags, boolean force) {
        animateCollapsePanels(flags, force, false /* delayed */, 1.0f /* speedUpFactor */);
    }

    @Override
    public void animateCollapsePanels(int flags, boolean force, boolean delayed) {
        animateCollapsePanels(flags, force, delayed, 1.0f /* speedUpFactor */);
    }

    public void animateCollapsePanels(int flags, boolean force, boolean delayed,
            float speedUpFactor) {
        if (!force && mState != StatusBarState.SHADE) {
            runPostCollapseRunnables();
            return;
        }
        if (SPEW) {
            Log.d(TAG, "animateCollapse():"
                    + " mExpandedVisible=" + mExpandedVisible
                    + " flags=" + flags);
        }

        if ((flags & CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL) == 0) {
            if (!mHandler.hasMessages(MSG_HIDE_RECENT_APPS)) {
                mHandler.removeMessages(MSG_HIDE_RECENT_APPS);
                mHandler.sendEmptyMessage(MSG_HIDE_RECENT_APPS);
            }
        }

        if (mStatusBarWindow != null) {
            // release focus immediately to kick off focus change transition
            mStatusBarWindowManager.setStatusBarFocusable(false);

            /*PRIZE-for phone top notification bg alpha- liufan-2016-09-20-start*/
            if(!isPanelFullyCollapsed() && mState == StatusBarState.SHADE) {
                isCollapseAllPanelsAnim = true;
                Log.d(TAG,"isCollapseAllPanelsAnim turn true");
            }
            /*PRIZE-for phone top notification bg alpha- liufan-2016-09-20-end*/
            mStatusBarWindow.cancelExpandHelper();
            mStatusBarView.collapsePanel(true /* animate */, delayed, speedUpFactor);
        }
    }
    /*PRIZE-for phone top notification bg alpha- liufan-2016-09-20-start*/
    public static boolean isCollapseAllPanelsAnim;
    /*PRIZE-for phone top notification bg alpha- liufan-2016-09-20-end*/

/**shiyicheng-add-for-supershot-2015-11-03-start*/
 public void prizeanimateCollapsePanels(int flags, boolean force) {
       // if (!force &&
      //          (mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED)) {
      //      runPostCollapseRunnables();
      //      return;
      //  }
        if (SPEW) {
            Log.d(TAG, "animateCollapse():"
                    + " mExpandedVisible=" + mExpandedVisible
                    + " flags=" + flags);
        }

        if ((flags & CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL) == 0) {
            if (!mHandler.hasMessages(MSG_HIDE_RECENT_APPS)) {
                mHandler.removeMessages(MSG_HIDE_RECENT_APPS);
                mHandler.sendEmptyMessage(MSG_HIDE_RECENT_APPS);
            }
        }

        if ((flags & CommandQueue.FLAG_EXCLUDE_SEARCH_PANEL) == 0) {
            mHandler.removeMessages(MSG_CLOSE_SEARCH_PANEL);
            mHandler.sendEmptyMessage(MSG_CLOSE_SEARCH_PANEL);
        }

        if (mStatusBarWindow != null) {
            // release focus immediately to kick off focus change transition
            mStatusBarWindowManager.setStatusBarFocusable(false);

            mStatusBarWindow.cancelExpandHelper();
            mStatusBarView.collapsePanel(true, false /* delayed */, 1.0f /* speedUpFactor */);
        }
    }
/**shiyicheng-add-for-supershot-2015-11-03-end*/

    private void runPostCollapseRunnables() {
        ArrayList<Runnable> clonedList = new ArrayList<>(mPostCollapseRunnables);
        mPostCollapseRunnables.clear();
        int size = clonedList.size();
        for (int i = 0; i < size; i++) {
            clonedList.get(i).run();
        }

    }


    /*PRIZE-add animateExpandNotificationsPanel flag-liufan-2016-06-03-start*/
    private boolean isAnimateExpandNotificationsPanel = false;
    /*PRIZE-add animateExpandNotificationsPanel flag-liufan-2016-06-03-end*/

    @Override
    public void animateExpandNotificationsPanel() {
        if (SPEW) Log.d(TAG, "animateExpand: mExpandedVisible=" + mExpandedVisible);
        if (!panelsEnabled()) {
            return ;
        }
        /*PRIZE-set animateExpandNotificationsPanel flag true-liufan-2016-06-03-start*/
        isAnimateExpandNotificationsPanel = true;
        /*PRIZE-set animateExpandNotificationsPanel flag true-liufan-2016-06-03-end*/

        /*PRIZE-pull to quick setting when there is no notification-liufan-2015-09-16-start*/
        if(mNotificationPanel.isShouldExpandQs()){
            animateExpandSettingsPanel(null);
            return ;
        }
        /*PRIZE-pull to quick setting when there is no notification-liufan-2015-09-16-end*/
        mNotificationPanel.expand(true /* animate */);

        if (false) postStartTracing();
    }

    @Override
    public void animateExpandSettingsPanel(String subPanel) {
        if (SPEW) Log.d(TAG, "animateExpand: mExpandedVisible=" + mExpandedVisible);
        if (!panelsEnabled()) {
            return;
        }

        // Settings are not available in setup
        if (!mUserSetup) return;


        if (subPanel != null) {
            mQSPanel.openDetails(subPanel);
        }
        mNotificationPanel.expandWithQs();

        if (false) postStartTracing();
    }

    public void animateCollapseQuickSettings() {
        if (mState == StatusBarState.SHADE) {
            mStatusBarView.collapsePanel(true, false /* delayed */, 1.0f /* speedUpFactor */);
        }
    }

    void makeExpandedInvisible() {
        if (SPEW) Log.d(TAG, "makeExpandedInvisible: mExpandedVisible=" + mExpandedVisible
                + " mExpandedVisible=" + mExpandedVisible);

        if (!mExpandedVisible || mStatusBarWindow == null) {
            return;
        }

        // Ensure the panel is fully collapsed (just in case; bug 6765842, 7260868)
        mStatusBarView.collapsePanel(/*animate=*/ false, false /* delayed*/,
                1.0f /* speedUpFactor */);

        mNotificationPanel.closeQs();

        mExpandedVisible = false;

        visibilityChanged(false);

        // Shrink the window to the size of the status bar only
        mStatusBarWindowManager.setPanelVisible(false);
        mStatusBarWindowManager.setForceStatusBarVisible(false);

        // Close any "App info" popups that might have snuck on-screen
        dismissPopups();

        runPostCollapseRunnables();
        setInteracting(StatusBarManager.WINDOW_STATUS_BAR, false);
        showBouncer();
        recomputeDisableFlags(true /* animate */);

        // Trimming will happen later if Keyguard is showing - doing it here might cause a jank in
        // the bouncer appear animation.
        if (!mStatusBarKeyguardViewManager.isShowing()) {
            WindowManagerGlobal.getInstance().trimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
        }
    }

    public boolean interceptTouchEvent(MotionEvent event) {
        /*PRIZE-don't allow to fingerdown statusbar- liufan-2016-07-22-start*/
        Log.d("intercept_statusbar","isOccluded------>"+isOccluded()+"--isShowing------->"+mStatusBarKeyguardViewManager.isShowing());
        if(isOccluded() && mStatusBarKeyguardViewManager != null && mStatusBarKeyguardViewManager.isShowing()){
            Log.d("intercept_statusbar","interceptTouchEvent");
            return true;
        }
        /*PRIZE-don't allow to fingerdown statusbar- liufan-2016-07-22-end*/
        if (DEBUG_GESTURES) {
            if (event.getActionMasked() != MotionEvent.ACTION_MOVE) {
                EventLog.writeEvent(EventLogTags.SYSUI_STATUSBAR_TOUCH,
                        event.getActionMasked(), (int) event.getX(), (int) event.getY(),
                        mDisabled1, mDisabled2);
            }

        }

        if (SPEW) {
            Log.d(TAG, "Touch: rawY=" + event.getRawY() + " event=" + event + " mDisabled1="
                + mDisabled1 + " mDisabled2=" + mDisabled2 + " mTracking=" + mTracking);
        } else if (CHATTY) {
            if (event.getAction() != MotionEvent.ACTION_MOVE) {
                Log.d(TAG, String.format(
                            "panel: %s at (%f, %f) mDisabled1=0x%08x mDisabled2=0x%08x",
                            MotionEvent.actionToString(event.getAction()),
                            event.getRawX(), event.getRawY(), mDisabled1, mDisabled2));
            }
        }

        if (DEBUG_GESTURES) {
            mGestureRec.add(event);
        }

        if (mStatusBarWindowState == WINDOW_STATE_SHOWING) {
            final boolean upOrCancel =
                    event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL;
            if (upOrCancel && !mExpandedVisible) {
                setInteracting(StatusBarManager.WINDOW_STATUS_BAR, false);
            } else {
                setInteracting(StatusBarManager.WINDOW_STATUS_BAR, true);
            }
        }
        return false;
    }

    public GestureRecorder getGestureRecorder() {
        return mGestureRec;
    }

    private void setNavigationIconHints(int hints) {
        if (hints == mNavigationIconHints) return;

        mNavigationIconHints = hints;

        if (mNavigationBarView != null) {
            mNavigationBarView.setNavigationIconHints(hints);
        }
        checkBarModes();
    }

    @Override // CommandQueue
    public void setWindowState(int window, int state) {
        boolean showing = state == WINDOW_STATE_SHOWING;
        if (mStatusBarWindow != null
                && window == StatusBarManager.WINDOW_STATUS_BAR
                && mStatusBarWindowState != state) {
            mStatusBarWindowState = state;
            if (DEBUG_WINDOW_STATE) Log.d(TAG, "Status bar " + windowStateToString(state));
            if (!showing && mState == StatusBarState.SHADE) {
                mStatusBarView.collapsePanel(false /* animate */, false /* delayed */,
                        1.0f /* speedUpFactor */);
            }
        }
        if (mNavigationBarView != null
                && window == StatusBarManager.WINDOW_NAVIGATION_BAR
                && mNavigationBarWindowState != state) {
            mNavigationBarWindowState = state;
            if (DEBUG_WINDOW_STATE) Log.d(TAG, "Navigation bar " + windowStateToString(state));
        }
    }

    @Override // CommandQueue
    public void buzzBeepBlinked() {
        if (mDozeServiceHost != null) {
            mDozeServiceHost.fireBuzzBeepBlinked();
        }
    }

    @Override
    public void notificationLightOff() {
        if (mDozeServiceHost != null) {
            mDozeServiceHost.fireNotificationLight(false);
        }
    }

    @Override
    public void notificationLightPulse(int argb, int onMillis, int offMillis) {
        if (mDozeServiceHost != null) {
            mDozeServiceHost.fireNotificationLight(true);
        }
    }

    @Override // CommandQueue
    public void setSystemUiVisibility(int vis, int fullscreenStackVis, int dockedStackVis,
            int mask, Rect fullscreenStackBounds, Rect dockedStackBounds) {
        final int oldVal = mSystemUiVisibility;
        final int newVal = (oldVal&~mask) | (vis&mask);
        final int diff = newVal ^ oldVal;
        if (DEBUG) Log.d(TAG, String.format(
                "setSystemUiVisibility vis=%s mask=%s oldVal=%s newVal=%s diff=%s",
                Integer.toHexString(vis), Integer.toHexString(mask),
                Integer.toHexString(oldVal), Integer.toHexString(newVal),
                Integer.toHexString(diff)));
        boolean sbModeChanged = false;
        if (diff != 0) {
            // we never set the recents bit via this method, so save the prior state to prevent
            // clobbering the bit below
            //final boolean wasRecentsVisible = (mSystemUiVisibility & View.RECENT_APPS_VISIBLE) > 0;

            mSystemUiVisibility = newVal;

            // update low profile
            if ((diff & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                setAreThereNotifications();
            }

            // ready to unhide
            if ((vis & View.STATUS_BAR_UNHIDE) != 0) {
                mSystemUiVisibility &= ~View.STATUS_BAR_UNHIDE;
                mNoAnimationOnNextBarModeChange = true;
            }

            /* prize-add-split screen, for landscape statubar not use Anim  -liyongli-20171027-start */
            if( PrizeOption.PRIZE_SPLIT_SCREEN_LANDSCAPE_ONE_STATUBAR
                  && vis==View.SYSTEM_UI_FLAG_VISIBLE
                  && !mNoAnimationOnNextBarModeChange
                  && dockedStackBounds.right !=0 && dockedStackBounds.bottom!=0
            ){ 
                mNoAnimationOnNextBarModeChange = true;
            }/* prize-add-split screen-liyongli-20171027-end */
            
            // update status bar mode
            final int sbMode = computeBarMode(oldVal, newVal, mStatusBarView.getBarTransitions(),
                    View.STATUS_BAR_TRANSIENT, View.STATUS_BAR_TRANSLUCENT,
                    View.STATUS_BAR_TRANSPARENT);

            // update navigation bar mode
            final int nbMode = mNavigationBarView == null ? -1 : computeBarMode(
                    oldVal, newVal, mNavigationBarView.getBarTransitions(),
                    View.NAVIGATION_BAR_TRANSIENT, View.NAVIGATION_BAR_TRANSLUCENT,
                    View.NAVIGATION_BAR_TRANSPARENT);
            sbModeChanged = sbMode != -1;
            final boolean nbModeChanged = nbMode != -1;
            boolean checkBarModes = false;
            if (sbModeChanged && sbMode != mStatusBarMode) {
                mStatusBarMode = sbMode;
                checkBarModes = true;
            }
            if (nbModeChanged && nbMode != mNavigationBarMode) {
                mNavigationBarMode = nbMode;
                checkBarModes = true;
            }
            if (checkBarModes) {
                checkBarModes();
            }
            if (sbModeChanged || nbModeChanged) {
                // update transient bar autohide
                if (mStatusBarMode == MODE_SEMI_TRANSPARENT || mNavigationBarMode == MODE_SEMI_TRANSPARENT) {
                    scheduleAutohide();
                    if(mNavigationBarMode == MODE_SEMI_TRANSPARENT){
                    	scheduleUpdateFloatNav(true);
                    }
                } else {
                    cancelAutohide();
                    scheduleUpdateFloatNav(false);
                }
            }

            if ((vis & View.NAVIGATION_BAR_UNHIDE) != 0) {
                mSystemUiVisibility &= ~View.NAVIGATION_BAR_UNHIDE;
            }

            // restore the recents bit
            /*if (wasRecentsVisible) {
                mSystemUiVisibility |= View.RECENT_APPS_VISIBLE;
            }*/

            // send updated sysui visibility to window manager
            notifyUiVisibilityChanged(mSystemUiVisibility);
        }

        mLightStatusBarController.onSystemUiVisibilityChanged(fullscreenStackVis, dockedStackVis,
                mask, fullscreenStackBounds, dockedStackBounds, sbModeChanged, mStatusBarMode);
    }

    private int computeBarMode(int oldVis, int newVis, BarTransitions transitions,
            int transientFlag, int translucentFlag, int transparentFlag) {
        final int oldMode = barMode(oldVis, transientFlag, translucentFlag, transparentFlag);
        final int newMode = barMode(newVis, transientFlag, translucentFlag, transparentFlag);
        if (oldMode == newMode) {
            return -1; // no mode change
        }
        return newMode;
    }

    private int barMode(int vis, int transientFlag, int translucentFlag, int transparentFlag) {
        int lightsOutTransparent = View.SYSTEM_UI_FLAG_LOW_PROFILE | transparentFlag;
        return (vis & transientFlag) != 0 ? MODE_SEMI_TRANSPARENT
                : (vis & translucentFlag) != 0 ? MODE_TRANSLUCENT
                : (vis & lightsOutTransparent) == lightsOutTransparent ? MODE_LIGHTS_OUT_TRANSPARENT
                : (vis & transparentFlag) != 0 ? MODE_TRANSPARENT
                : (vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0 ? MODE_LIGHTS_OUT
                : MODE_OPAQUE;
    }

    private void checkBarModes() {
        if (mDemoMode) return;
        checkBarMode(mStatusBarMode, mStatusBarWindowState, mStatusBarView.getBarTransitions(),
                mNoAnimationOnNextBarModeChange);
        if (mNavigationBarView != null) {
            checkBarMode(mNavigationBarMode,
                    mNavigationBarWindowState, mNavigationBarView.getBarTransitions(),
                    mNoAnimationOnNextBarModeChange);
        }
        mNoAnimationOnNextBarModeChange = false;
    }

    private void checkBarMode(int mode, int windowState, BarTransitions transitions,
            boolean noAnimation) {
        final boolean powerSave = mBatteryController.isPowerSave();
        /*final */boolean anim = !noAnimation && mDeviceInteractive
                && windowState != WINDOW_STATE_HIDDEN && !powerSave;
        if (powerSave && getBarState() == StatusBarState.SHADE) {
            mode = MODE_WARNING;
        }
        /// M: Fix bug alps02830922 @{
        if (FeatureOptions.LOW_RAM_SUPPORT && !ActivityManager.isHighEndGfx()
                && getBarState() != StatusBarState.KEYGUARD
                && (mode == MODE_SEMI_TRANSPARENT || mode == MODE_TRANSLUCENT
                || mode == MODE_TRANSPARENT)) {
                mode = MODE_OPAQUE;
                anim = false;
        }
        /// @}
        transitions.transitionTo(mode, anim);
    }

    private void finishBarAnimations() {
        mStatusBarView.getBarTransitions().finishAnimations();
        if (mNavigationBarView != null) {
            mNavigationBarView.getBarTransitions().finishAnimations();
        }
    }

    private final Runnable mCheckBarModes = new Runnable() {
        @Override
        public void run() {
            checkBarModes();
        }
    };

    @Override
    public void setInteracting(int barWindow, boolean interacting) {
        final boolean changing = ((mInteractingWindows & barWindow) != 0) != interacting;
        mInteractingWindows = interacting
                ? (mInteractingWindows | barWindow)
                : (mInteractingWindows & ~barWindow);
        if (mInteractingWindows != 0) {
            suspendAutohide();
        } else {
            resumeSuspendedAutohide();
        }
        // manually dismiss the volume panel when interacting with the nav bar
        if (changing && interacting && barWindow == StatusBarManager.WINDOW_NAVIGATION_BAR) {
            dismissVolumeDialog();
        }
        checkBarModes();
    }

    private void dismissVolumeDialog() {
        if (mVolumeComponent != null) {
            mVolumeComponent.dismissNow();
        }
    }

    private void resumeSuspendedAutohide() {
        if (mAutohideSuspended) {
            scheduleAutohide();
            mHandler.postDelayed(mCheckBarModes, 500); // longer than home -> launcher
        }
    }

    private void suspendAutohide() {
        mHandler.removeCallbacks(mAutohide);
        mHandler.removeCallbacks(mCheckBarModes);
        mAutohideSuspended = (mSystemUiVisibility & STATUS_OR_NAV_TRANSIENT) != 0;
    }

    /*private void cancelUpdateFloatByNav() {
        mHandler.removeCallbacks(mUpdateFloatByNav);
    }*/

    private void scheduleUpdateFloatNav(boolean isShowNav) {
		if(mContext != null && PrizeOption.PRIZE_NEW_FLOAT_WINDOW){
			int isFloatShow = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_NEW_FLOAT_WINDOW,0);
			if(isFloatShow == 1){
				if(isShowNav){
					mHandler.postDelayed(mUpdateFloatByShowNav, 50);//
				}else {
					mHandler.postDelayed(mUpdateFloatByHideNav, 50);
				}
			}
		}
    }
    
    private void cancelAutohide() {
        mAutohideSuspended = false;
        mHandler.removeCallbacks(mAutohide);
    }

    private void scheduleAutohide() {
        cancelAutohide();
        mHandler.postDelayed(mAutohide, AUTOHIDE_TIMEOUT_MS);
    }

    private void checkUserAutohide(View v, MotionEvent event) {
        if ((mSystemUiVisibility & STATUS_OR_NAV_TRANSIENT) != 0  // a transient bar is revealed
                && event.getAction() == MotionEvent.ACTION_OUTSIDE // touch outside the source bar
                && event.getX() == 0 && event.getY() == 0  // a touch outside both bars
                && !mRemoteInputController.isRemoteInputActive()) { // not due to typing in IME
            userAutohide();
        }
    }

    private void userAutohide() {
        cancelAutohide();
        mHandler.postDelayed(mAutohide, 350); // longer than app gesture -> flag clear
    }

    private boolean areLightsOn() {
        return 0 == (mSystemUiVisibility & View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    public void setLightsOn(boolean on) {
        Log.v(TAG, "setLightsOn(" + on + ")");
        if (on) {
            setSystemUiVisibility(0, 0, 0, View.SYSTEM_UI_FLAG_LOW_PROFILE,
                    mLastFullscreenStackBounds, mLastDockedStackBounds);
        } else {
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE, 0, 0,
                    View.SYSTEM_UI_FLAG_LOW_PROFILE, mLastFullscreenStackBounds,
                    mLastDockedStackBounds);
        }
    }

    private void notifyUiVisibilityChanged(int vis) {
        try {
            if (mLastDispatchedSystemUiVisibility != vis) {
                mWindowManagerService.statusBarVisibilityChanged(vis);
                mLastDispatchedSystemUiVisibility = vis;
            }
        } catch (RemoteException ex) {
        }
    }

    @Override
    public void topAppWindowChanged(boolean showMenu) {
        if (SPEW) {
            Log.d(TAG, (showMenu?"showing":"hiding") + " the MENU button");
        }
        if (mNavigationBarView != null) {
            mNavigationBarView.setMenuVisibility(showMenu);
        }

        // See above re: lights-out policy for legacy apps.
        if (showMenu) setLightsOn(true);
    }

    @Override
    public void setImeWindowStatus(IBinder token, int vis, int backDisposition,
            boolean showImeSwitcher) {
        boolean imeShown = (vis & InputMethodService.IME_VISIBLE) != 0;
        int flags = mNavigationIconHints;
        if ((backDisposition == InputMethodService.BACK_DISPOSITION_WILL_DISMISS) || imeShown) {
            flags |= NAVIGATION_HINT_BACK_ALT;
        } else {
            flags &= ~NAVIGATION_HINT_BACK_ALT;
        }
        if (showImeSwitcher) {
            flags |= NAVIGATION_HINT_IME_SHOWN;
        } else {
            flags &= ~NAVIGATION_HINT_IME_SHOWN;
        }

        setNavigationIconHints(flags);
    }

    public static String viewInfo(View v) {
        return "[(" + v.getLeft() + "," + v.getTop() + ")(" + v.getRight() + "," + v.getBottom()
                + ") " + v.getWidth() + "x" + v.getHeight() + "]";
    }

    @Override
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (mQueueLock) {
            pw.println("Current Status Bar state:");
            pw.println("  mExpandedVisible=" + mExpandedVisible
                    + ", mTrackingPosition=" + mTrackingPosition);
            pw.println("  mTracking=" + mTracking);
            pw.println("  mDisplayMetrics=" + mDisplayMetrics);
            pw.println("  mStackScroller: " + viewInfo(mStackScroller));
            pw.println("  mStackScroller: " + viewInfo(mStackScroller)
                    + " scroll " + mStackScroller.getScrollX()
                    + "," + mStackScroller.getScrollY());
        }

        pw.print("  mInteractingWindows="); pw.println(mInteractingWindows);
        pw.print("  mStatusBarWindowState=");
        pw.println(windowStateToString(mStatusBarWindowState));
        pw.print("  mStatusBarMode=");
        pw.println(BarTransitions.modeToString(mStatusBarMode));
        pw.print("  mDozing="); pw.println(mDozing);
        pw.print("  mZenMode=");
        pw.println(Settings.Global.zenModeToString(mZenMode));
        pw.print("  mUseHeadsUp=");
        pw.println(mUseHeadsUp);
        dumpBarTransitions(pw, "mStatusBarView", mStatusBarView.getBarTransitions());
        if (mNavigationBarView != null) {
            pw.print("  mNavigationBarWindowState=");
            pw.println(windowStateToString(mNavigationBarWindowState));
            pw.print("  mNavigationBarMode=");
            pw.println(BarTransitions.modeToString(mNavigationBarMode));
            dumpBarTransitions(pw, "mNavigationBarView", mNavigationBarView.getBarTransitions());
        }

        pw.print("  mNavigationBarView=");
        if (mNavigationBarView == null) {
            pw.println("null");
        } else {
            mNavigationBarView.dump(fd, pw, args);
        }

        pw.print("  mMediaSessionManager=");
        pw.println(mMediaSessionManager);
        pw.print("  mMediaNotificationKey=");
        pw.println(mMediaNotificationKey);
        pw.print("  mMediaController=");
        pw.print(mMediaController);
        if (mMediaController != null) {
            pw.print(" state=" + mMediaController.getPlaybackState());
        }
        pw.println();
        pw.print("  mMediaMetadata=");
        pw.print(mMediaMetadata);
        if (mMediaMetadata != null) {
            pw.print(" title=" + mMediaMetadata.getText(MediaMetadata.METADATA_KEY_TITLE));
        }
        pw.println();

        pw.println("  Panels: ");
        if (mNotificationPanel != null) {
            pw.println("    mNotificationPanel=" +
                mNotificationPanel + " params=" + mNotificationPanel.getLayoutParams().debug(""));
            pw.print  ("      ");
            mNotificationPanel.dump(fd, pw, args);
        }

        DozeLog.dump(pw);

        if (DUMPTRUCK) {
            synchronized (mNotificationData) {
                mNotificationData.dump(pw, "  ");
            }

            mIconController.dump(pw);

            if (false) {
                pw.println("see the logcat for a dump of the views we have created.");
                // must happen on ui thread
                mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mStatusBarView.getLocationOnScreen(mAbsPos);
                            Log.d(TAG, "mStatusBarView: ----- (" + mAbsPos[0] + "," + mAbsPos[1]
                                    + ") " + mStatusBarView.getWidth() + "x"
                                    + getStatusBarHeight());
                            mStatusBarView.debug();
                        }
                    });
            }
        }

        if (DEBUG_GESTURES) {
            pw.print("  status bar gestures: ");
            mGestureRec.dump(fd, pw, args);
        }
        if (mStatusBarWindowManager != null) {
            mStatusBarWindowManager.dump(fd, pw, args);
        }
        if (mNetworkController != null) {
            mNetworkController.dump(fd, pw, args);
        }
        if (mBluetoothController != null) {
            mBluetoothController.dump(fd, pw, args);
        }
        if (mHotspotController != null) {
            mHotspotController.dump(fd, pw, args);
        }
        if (mCastController != null) {
            mCastController.dump(fd, pw, args);
        }
        if (mUserSwitcherController != null) {
            mUserSwitcherController.dump(fd, pw, args);
        }
        if (mBatteryController != null) {
            mBatteryController.dump(fd, pw, args);
        }
        if (mNextAlarmController != null) {
            mNextAlarmController.dump(fd, pw, args);
        }
        if (mSecurityController != null) {
            mSecurityController.dump(fd, pw, args);
        }
        if (mHeadsUpManager != null) {
            mHeadsUpManager.dump(fd, pw, args);
        } else {
            pw.println("  mHeadsUpManager: null");
        }
        if (mGroupManager != null) {
            mGroupManager.dump(fd, pw, args);
        } else {
            pw.println("  mGroupManager: null");
        }
        if (KeyguardUpdateMonitor.getInstance(mContext) != null) {
            KeyguardUpdateMonitor.getInstance(mContext).dump(fd, pw, args);
        }
        if (mFlashlightController != null) {
            mFlashlightController.dump(fd, pw, args);
        }

        FalsingManager.getInstance(mContext).dump(pw);
        FalsingLog.dump(pw);

        pw.println("SharedPreferences:");
        for (Map.Entry<String, ?> entry : Prefs.getAll(mContext).entrySet()) {
            pw.print("  "); pw.print(entry.getKey()); pw.print("="); pw.println(entry.getValue());
        }
    }

    private static void dumpBarTransitions(PrintWriter pw, String var, BarTransitions transitions) {
        pw.print("  "); pw.print(var); pw.print(".BarTransitions.mMode=");
        pw.println(BarTransitions.modeToString(transitions.getMode()));
    }

    @Override
    public void createAndAddWindows() {
        addStatusBarWindow();
    }

    private void addStatusBarWindow() {
        makeStatusBarView();
        mStatusBarWindowManager = new StatusBarWindowManager(mContext);
        mRemoteInputController = new RemoteInputController(mStatusBarWindowManager,
                mHeadsUpManager);
        mStatusBarWindowManager.add(mStatusBarWindow, getStatusBarHeight());
        /*PRIZE-add for haokan-liufan-2017-11-17-start*/
        mStatusBarWindowManager.setPhoneStatusBar(this);
        /*PRIZE-add for haokan-liufan-2017-11-17-end*/
        //prize add by xiarui 2018-04-18 start@{
		if (PRIZE_HEADS_UP_STYLE) {
            mRemoteInputController.addCallback(mTickerController);
        }
		//---end@}
    }

    // called by makeStatusbar and also by PhoneStatusBarView
    void updateDisplaySize() {
        mDisplay.getMetrics(mDisplayMetrics);
        mDisplay.getSize(mCurrentDisplaySize);
        if (DEBUG_GESTURES) {
            mGestureRec.tag("display",
                    String.format("%dx%d", mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels));
        }
    }

    float getDisplayDensity() {
        return mDisplayMetrics.density;
    }

    public void startActivityDismissingKeyguard(final Intent intent, boolean onlyProvisioned,
            boolean dismissShade) {
        startActivityDismissingKeyguard(intent, onlyProvisioned, dismissShade, null /* callback */);
    }

    public void startActivityDismissingKeyguard(final Intent intent, boolean onlyProvisioned,
            final boolean dismissShade, final Callback callback) {
        if (onlyProvisioned && !isDeviceProvisioned()) return;

        final boolean afterKeyguardGone = PreviewInflater.wouldLaunchResolverActivity(
                mContext, intent, mCurrentUserId);
        final boolean keyguardShowing = mStatusBarKeyguardViewManager.isShowing();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mAssistManager.hideAssist();
                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                int result = ActivityManager.START_CANCELED;
                ActivityOptions options = new ActivityOptions(getActivityOptions());
                if (intent == KeyguardBottomAreaView.INSECURE_CAMERA_INTENT) {
                    // Normally an activity will set it's requested rotation
                    // animation on its window. However when launching an activity
                    // causes the orientation to change this is too late. In these cases
                    // the default animation is used. This doesn't look good for
                    // the camera (as it rotates the camera contents out of sync
                    // with physical reality). So, we ask the WindowManager to
                    // force the crossfade animation if an orientation change
                    // happens to occur during the launch.
                    options.setRotationAnimationHint(
                            WindowManager.LayoutParams.ROTATION_ANIMATION_SEAMLESS);
                }
                try {
                    result = ActivityManagerNative.getDefault().startActivityAsUser(
                            null, mContext.getBasePackageName(),
                            intent,
                            intent.resolveTypeIfNeeded(mContext.getContentResolver()),
                            null, null, 0, Intent.FLAG_ACTIVITY_NEW_TASK, null,
                            options.toBundle(), UserHandle.CURRENT.getIdentifier());
                } catch (RemoteException e) {
                    Log.w(TAG, "Unable to start activity", e);
                }
                overrideActivityPendingAppTransition(
                        keyguardShowing && !afterKeyguardGone);
                if (callback != null) {
                    callback.onActivityStarted(result);
                }
            }
        };
        Runnable cancelRunnable = new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onActivityStarted(ActivityManager.START_CANCELED);
                }
            }
        };
        executeRunnableDismissingKeyguard(runnable, cancelRunnable, dismissShade,
                afterKeyguardGone, true /* deferred */);
    }

    public void executeRunnableDismissingKeyguard(final Runnable runnable,
            final Runnable cancelAction,
            final boolean dismissShade,
            final boolean afterKeyguardGone,
            final boolean deferred) {
        final boolean keyguardShowing = mStatusBarKeyguardViewManager.isShowing();
        dismissKeyguardThenExecute(new OnDismissAction() {
            @Override
            public boolean onDismiss() {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (keyguardShowing && !afterKeyguardGone) {
                                ActivityManagerNative.getDefault()
                                        .keyguardWaitingForActivityDrawn();
                            }
                            if (runnable != null) {
                                runnable.run();
                            }
                        } catch (RemoteException e) {
                        }
                    }
                });
                if (dismissShade) {
                    animateCollapsePanels(CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL, true /* force */,
                            true /* delayed*/);
                }
                return deferred;
            }
        }, cancelAction, afterKeyguardGone);
    }
	
    // @prize fanjunchen 2015-09-01{
    public void setBackdropGone() {
        NotificationPanelView.debugMagazine("setBackdropGone gone");
        if (mBackdrop != null && View.VISIBLE == mBackdrop.getVisibility())
            mBackdrop.setVisibility(View.GONE);
    }
    // @prize}
	
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.v(TAG, "onReceive: " + intent);
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                KeyboardShortcuts.dismiss();
                if (mRemoteInputController != null) {
                    mRemoteInputController.closeRemoteInputs();
                }
                if (isCurrentProfile(getSendingUserId())) {
                    int flags = CommandQueue.FLAG_EXCLUDE_NONE;
                    String reason = intent.getStringExtra("reason");
                    if (reason != null && reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        flags |= CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL;
                    }
                    animateCollapsePanels(flags);
                }
            }
            else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                notifyNavigationBarScreenOn(false);
                notifyHeadsUpScreenOff();
                finishBarAnimations();
                resetUserExpandedStates();
            }
            else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                notifyNavigationBarScreenOn(true);
            }			
			/*PRIZE-PowerExtendMode-to solve can't unlock problem-wangxianzhen-2016-01-13-start bugid 10971*/
            else if(ACTION_ENTER_SUPERPOWER.equals(action)) {
                Log.i(TAG,"PowerExtendMode ACTION_ENTER_SUPERPOWER");
                animateCollapsePanels(StatusBarState.KEYGUARD, true);
            }
            /*PRIZE-PowerExtendMode-to solve can't unlock problem-wangxianzhen-2016-01-13-end bugid 10971*/
			
            /* Dynamically hiding nav bar feature. prize-linkh-20161115 */
            else if(NAV_BAR_CONTROL_INTENT.equals(action)) {
                printMyLog("Receive " + NAV_BAR_CONTROL_INTENT);
                Bundle bundle = intent.getExtras();
                printMyLog("bundle = " + bundle); 
                if(bundle != null) {
                    String command = bundle.getString(NAV_BAR_CONTROL_CMD, "").trim().toLowerCase();
                    printMyLog("command = " + command); 
                    if("show".equals(command)) {
                        showNavBar();
                    } else if("hide".equals(command)) {
                        hideNavBar();
                    }
                }
            } //END...
        }
    };

    private BroadcastReceiver mDemoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.v(TAG, "onReceive: " + intent);
            String action = intent.getAction();
            if (ACTION_DEMO.equals(action)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    String command = bundle.getString("command", "").trim().toLowerCase();
                    if (command.length() > 0) {
                        try {
                            dispatchDemoCommand(command, bundle);
                        } catch (Throwable t) {
                            Log.w(TAG, "Error running demo command, intent=" + intent, t);
                        }
                    }
                }
            } else if (ACTION_FAKE_ARTWORK.equals(action)) {
                if (DEBUG_MEDIA_FAKE_ARTWORK) {
                    updateMediaMetaData(true, true);
                }
            }
        }
    };

    public void resetUserExpandedStates() {
        ArrayList<Entry> activeNotifications = mNotificationData.getActiveNotifications();
        final int notificationCount = activeNotifications.size();
        for (int i = 0; i < notificationCount; i++) {
            NotificationData.Entry entry = activeNotifications.get(i);
            if (entry.row != null) {
                entry.row.resetUserExpansion();
            }
        }
    }

    @Override
    protected void dismissKeyguardThenExecute(OnDismissAction action, boolean afterKeyguardGone) {
        dismissKeyguardThenExecute(action, null /* cancelRunnable */, afterKeyguardGone);
    }

    public void dismissKeyguard() {
        mStatusBarKeyguardViewManager.dismiss();
    }

    private void dismissKeyguardThenExecute(OnDismissAction action, Runnable cancelAction,
            boolean afterKeyguardGone) {
        if (mStatusBarKeyguardViewManager.isShowing()) {
            mStatusBarKeyguardViewManager.dismissWithAction(action, cancelAction,
                    afterKeyguardGone);
        } else {
            action.onDismiss();
        }
    }

    // SystemUIService notifies SystemBars of configuration changes, which then calls down here
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        updateResources();
        updateDisplaySize(); // populates mDisplayMetrics
        super.onConfigurationChanged(newConfig); // calls refreshLayout

        if (DEBUG) {
            Log.v(TAG, "configuration changed: " + mContext.getResources().getConfiguration());
        }

        repositionNavigationBar();
        updateRowStates();
        mScreenPinningRequest.onConfigurationChanged();
        mNetworkController.onConfigurationChanged();
        /*PRIZE-collapse panel when ConfigurationChanged- liufan-2015-06-12-start*/
        if (VersionControl.CUR_VERSION == VersionControl.BLUR_BG_VER) {
            animateCollapsePanels();
        }
        /*PRIZE-collapse panel when ConfigurationChanged- liufan-2015-06-12-end*/
        /*PRIZE-refresh the text of battery percent- liyao-2015-07-01 start*/
        FontSizeUtils.updateFontSize((TextView)mStatusBarView.findViewById(R.id.battery_percentage), R.dimen.status_bar_clock_size);
        /*PRIZE-refresh the text of battery percent- liyao-2015-07-01 end*/
    }

    @Override
    public void userSwitched(int newUserId) {
        super.userSwitched(newUserId);
        if (MULTIUSER_DEBUG) mNotificationPanelDebugText.setText("USER " + newUserId);
        animateCollapsePanels();
        updatePublicMode();
        updateNotifications();
        resetUserSetupObserver();
        setControllerUsers();
        clearCurrentMediaNotification();
        mLockscreenWallpaper.setCurrentUser(newUserId);
        mScrimController.setCurrentUser(newUserId);
        updateMediaMetaData(true, false);
    }

    private void setControllerUsers() {
        if (mZenModeController != null) {
            mZenModeController.setUserId(mCurrentUserId);
        }
        if (mSecurityController != null) {
            mSecurityController.onUserSwitched(mCurrentUserId);
        }
    }

    private void resetUserSetupObserver() {
        mContext.getContentResolver().unregisterContentObserver(mUserSetupObserver);
        mUserSetupObserver.onChange(false);
        mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.USER_SETUP_COMPLETE), true,
                mUserSetupObserver, mCurrentUserId);
    }

    /**
     * Reload some of our resources when the configuration changes.
     *
     * We don't reload everything when the configuration changes -- we probably
     * should, but getting that smooth is tough.  Someday we'll fix that.  In the
     * meantime, just update the things that we know change.
     */
    void updateResources() {
        // Update the quick setting tiles
        if (mQSPanel != null) {
            mQSPanel.updateResources();
        }

        loadDimens();

        if (mNotificationPanel != null) {
            mNotificationPanel.updateResources();
        }
        if (mBrightnessMirrorController != null) {
            mBrightnessMirrorController.updateResources();
        }
    }

    protected void loadDimens() {
        final Resources res = mContext.getResources();

        int oldBarHeight = mNaturalBarHeight;
        mNaturalBarHeight = res.getDimensionPixelSize(
                com.android.internal.R.dimen.status_bar_height);
        if (mStatusBarWindowManager != null && mNaturalBarHeight != oldBarHeight) {
            mStatusBarWindowManager.setBarHeight(mNaturalBarHeight);
        }
        mMaxAllowedKeyguardNotifications = res.getInteger(
                R.integer.keyguard_max_notification_count);

        /*-modify for haokan-liufan-2017-10-25-start-*/
        //if(NotificationPanelView.IS_ShowHaoKanView)
		    mMaxAllowedKeyguardNotifications=3;
        /*-modify for haokan-liufan-2017-10-25-end-*/
        if (DEBUG) Log.v(TAG, "defineSlots");
    }

    // Visibility reporting

    @Override
    protected void handleVisibleToUserChanged(boolean visibleToUser) {
        if (visibleToUser) {
            super.handleVisibleToUserChanged(visibleToUser);
            startNotificationLogging();
        } else {
            stopNotificationLogging();
            super.handleVisibleToUserChanged(visibleToUser);
        }
    }

    private void stopNotificationLogging() {
        // Report all notifications as invisible and turn down the
        // reporter.
        if (!mCurrentlyVisibleNotifications.isEmpty()) {
            logNotificationVisibilityChanges(Collections.<NotificationVisibility>emptyList(),
                    mCurrentlyVisibleNotifications);
            recycleAllVisibilityObjects(mCurrentlyVisibleNotifications);
        }
        mHandler.removeCallbacks(mVisibilityReporter);
        mStackScroller.setChildLocationsChangedListener(null);
    }

    private void startNotificationLogging() {
        mStackScroller.setChildLocationsChangedListener(mNotificationLocationsChangedListener);
        // Some transitions like mVisibleToUser=false -> mVisibleToUser=true don't
        // cause the scroller to emit child location events. Hence generate
        // one ourselves to guarantee that we're reporting visible
        // notifications.
        // (Note that in cases where the scroller does emit events, this
        // additional event doesn't break anything.)
        mNotificationLocationsChangedListener.onChildLocationsChanged(mStackScroller);
    }

    private void logNotificationVisibilityChanges(
            Collection<NotificationVisibility> newlyVisible,
            Collection<NotificationVisibility> noLongerVisible) {
        if (newlyVisible.isEmpty() && noLongerVisible.isEmpty()) {
            return;
        }
        NotificationVisibility[] newlyVisibleAr =
                newlyVisible.toArray(new NotificationVisibility[newlyVisible.size()]);
        NotificationVisibility[] noLongerVisibleAr =
                noLongerVisible.toArray(new NotificationVisibility[noLongerVisible.size()]);
        try {
            mBarService.onNotificationVisibilityChanged(newlyVisibleAr, noLongerVisibleAr);
        } catch (RemoteException e) {
            // Ignore.
        }

        final int N = newlyVisible.size();
        if (N > 0) {
            String[] newlyVisibleKeyAr = new String[N];
            for (int i = 0; i < N; i++) {
                newlyVisibleKeyAr[i] = newlyVisibleAr[i].key;
            }

            setNotificationsShown(newlyVisibleKeyAr);
        }
    }

    // State logging

    private void logStateToEventlog() {
        boolean isShowing = mStatusBarKeyguardViewManager.isShowing();
        boolean isOccluded = mStatusBarKeyguardViewManager.isOccluded();
        boolean isBouncerShowing = mStatusBarKeyguardViewManager.isBouncerShowing();
        boolean isSecure = mUnlockMethodCache.isMethodSecure();
        boolean canSkipBouncer = mUnlockMethodCache.canSkipBouncer();
        int stateFingerprint = getLoggingFingerprint(mState,
                isShowing,
                isOccluded,
                isBouncerShowing,
                isSecure,
                canSkipBouncer);
        if (stateFingerprint != mLastLoggedStateFingerprint) {
            EventLogTags.writeSysuiStatusBarState(mState,
                    isShowing ? 1 : 0,
                    isOccluded ? 1 : 0,
                    isBouncerShowing ? 1 : 0,
                    isSecure ? 1 : 0,
                    canSkipBouncer ? 1 : 0);
            mLastLoggedStateFingerprint = stateFingerprint;
        }
    }

    /*PRIZE-add isOccluded()-avoid expand notification panel,bugid:16582- liufan-2016-05-31-start*/
    public boolean isOccluded(){
        return mStatusBarKeyguardViewManager != null ? mStatusBarKeyguardViewManager.isOccluded() : false;
    }
    /*PRIZE-add isOccluded()-avoid expand notification panel,bugid:16582- liufan-2016-05-31-end*/

    /**
     * Returns a fingerprint of fields logged to eventlog
     */
    private static int getLoggingFingerprint(int statusBarState, boolean keyguardShowing,
            boolean keyguardOccluded, boolean bouncerShowing, boolean secure,
            boolean currentlyInsecure) {
        // Reserve 8 bits for statusBarState. We'll never go higher than
        // that, right? Riiiight.
        return (statusBarState & 0xFF)
                | ((keyguardShowing   ? 1 : 0) <<  8)
                | ((keyguardOccluded  ? 1 : 0) <<  9)
                | ((bouncerShowing    ? 1 : 0) << 10)
                | ((secure            ? 1 : 0) << 11)
                | ((currentlyInsecure ? 1 : 0) << 12);
    }

    //
    // tracing
    //

    void postStartTracing() {
        mHandler.postDelayed(mStartTracing, 3000);
    }

    void vibrate() {
        android.os.Vibrator vib = (android.os.Vibrator)mContext.getSystemService(
                Context.VIBRATOR_SERVICE);
        vib.vibrate(250, VIBRATION_ATTRIBUTES);
    }

    Runnable mStartTracing = new Runnable() {
        @Override
        public void run() {
            vibrate();
            SystemClock.sleep(250);
            Log.d(TAG, "startTracing");
            android.os.Debug.startMethodTracing("/data/statusbar-traces/trace");
            mHandler.postDelayed(mStopTracing, 10000);
        }
    };

    Runnable mStopTracing = new Runnable() {
        @Override
        public void run() {
            android.os.Debug.stopMethodTracing();
            Log.d(TAG, "stopTracing");
            vibrate();
        }
    };

    @Override
    public boolean shouldDisableNavbarGestures() {
        /* Dynamically changing Recents function feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_TREAT_RECENTS_AS_MENU) {
            if (mEnableTreatRecentsAsMenu) {
                printMyLog("shouldDisableNavbarGestures() disable because of treating recents as menu!");
                return true;
            }
        } //end...

        return !isDeviceProvisioned() || (mDisabled1 & StatusBarManager.DISABLE_SEARCH) != 0;
    }

    public void postQSRunnableDismissingKeyguard(final Runnable runnable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mLeaveOpenOnKeyguardHide = true;
                executeRunnableDismissingKeyguard(runnable, null, false, false, false);
            }
        });
    }

    public void postStartActivityDismissingKeyguard(final PendingIntent intent) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                startPendingIntentDismissingKeyguard(intent);
            }
        });
    }

    public void postStartActivityDismissingKeyguard(final Intent intent, int delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handleStartActivityDismissingKeyguard(intent, true /*onlyProvisioned*/);
            }
        }, delay);
    }

    private void handleStartActivityDismissingKeyguard(Intent intent, boolean onlyProvisioned) {
        startActivityDismissingKeyguard(intent, onlyProvisioned, true /* dismissShade */);
    }

    private static class FastColorDrawable extends Drawable {
        private final int mColor;

        public FastColorDrawable(int color) {
            mColor = 0xff000000 | color;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawColor(mColor, PorterDuff.Mode.SRC);
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
        }

        @Override
        public void setBounds(Rect bounds) {
        }
    }

    @Override
    public void destroy() {
        super.destroy();
         /* prize-add-split screen-liyongli-20170612-start */
		if( PrizeOption.PRIZE_SYSTEMUI_QQPOP_ICON ){
			EventBus.getDefault().unregister(this);
		}
		/* prize-add-split screen-liyongli-20170612-end */
        
        if (mStatusBarWindow != null) {
            mWindowManager.removeViewImmediate(mStatusBarWindow);
            mStatusBarWindow = null;
        }
        if (mNavigationBarView != null) {
            mWindowManager.removeViewImmediate(mNavigationBarView);
            mNavigationBarView = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;
        }

        //prize add by xiarui 2018-04-16 start @{
        if (PRIZE_HEADS_UP_STYLE) {
            mTickerController.unRegisterBroadcastReceiver(mContext);
        }
        //---end@}

        mContext.unregisterReceiver(mBroadcastReceiver);
        /*prize add by xiarui for face id 2018-04-04 start*/
        if (PrizeOption.PRIZE_FACE_ID) {
            if (mKeyguardBottomArea != null) {
                mKeyguardBottomArea.unregisterFaceIdReceiver();
                mKeyguardBottomArea.unregisterFaceIdSwitchObserver();
            }
        }
        /*prize add by xiarui for face id 2018-04-04 end*/
        /**prize add by xiarui 2018-01-25 start **/
        unregisterAutoClearReceiver();
        stopWatchingCloudListFile();
        /**prize add by xiarui 2018-01-25 end **/
        /*PRIZE-cancel listen the battery change-liufan-2015-7-8-start*/
        mContext.unregisterReceiver(mBatteryTracker);
        /*PRIZE-cancel listen the battery change-liufan-2015-7-8-end*/

        //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar start
        if(OPEN_GESTURE_NAVIGATION){
        	unregisterSwipeUpGestureReceiver();
        }
        //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar end

		/*prize-public-standard:Changed lock screen-liuweiquan-20151212-start*/
		if(PrizeOption.PRIZE_CHANGED_WALLPAPER){
			mContext.unregisterReceiver(mChangedWallpaperReceiver);
			mBaiBianWallpaperObserver.stopObserving();
		}		
		/*prize-public-standard:Changed lock screen-liuweiquan-20151212-end*/

        /*PRIZE-add for network speed-liufan-2016-09-20-start*/
        if(mNetworkSpeedObserver!=null) mNetworkSpeedObserver.stopObserving();
        mContext.unregisterReceiver(mNetworkStateReceiver);
        /*PRIZE-add for network speed-liufan-2016-09-20-end*/
        if(PrizeOption.PRIZE_SYSTEMUI_BATTERY_METER){
            mShowBatteryPercentageObserver.stopObserving();
        }
        mContext.unregisterReceiver(mDemoReceiver);

        /* Reposition nav bar back key feature & Dynamically hiding nav bar feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR || PrizeOption.PRIZE_REPOSITION_BACK_KEY) {
            if (mNavigationBarView != null) {
                mContext.getContentResolver().unregisterContentObserver(mNavbarStyleObserver);
            }
        } //END...

        /* Dynamically changing Recents function feature. prize-linkh-20161115 */
        if (PrizeOption.PRIZE_TREAT_RECENTS_AS_MENU) {
            if (mNavigationBarView != null) {
                mContext.getContentResolver().unregisterContentObserver(mEnableTreatRecentsAsMenuObserver);
            }
        } //END...

        /* Nav bar related to mBack key feature. prize-linkh-20160804 */
        if(SUPPORT_NAV_BAR_FOR_MBACK_DEVICE) {
            if (mNavigationBarView != null) {
                mContext.getContentResolver().unregisterContentObserver(mNavBarStateFormBackObserver);
            }
        }        
        //END....
        //prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar start
        if(OPEN_GESTURE_NAVIGATION) {
            mContext.getContentResolver().unregisterContentObserver(mGestureIndicatorStateObserver);
            mContext.getContentResolver().unregisterContentObserver(mGestureStateObserver);
        }
        //prize tangzhengrong 20180503 Swipe-up Gesture Navigation bar end
        mContext.unregisterReceiver(m3FingerMoveUpReceiver); //PRIZE-three_finger_moveup_open split screen-liyongli-2018-04-23

        mAssistManager.destroy();

        final SignalClusterView signalCluster =
                (SignalClusterView) mStatusBarView.findViewById(R.id.signal_cluster);
        final SignalClusterView signalClusterKeyguard =
                (SignalClusterView) mKeyguardStatusBar.findViewById(R.id.signal_cluster);
        final SignalClusterView signalClusterQs =
                (SignalClusterView) mHeader.findViewById(R.id.signal_cluster);
        mNetworkController.removeSignalCallback(signalCluster);
        mNetworkController.removeSignalCallback(signalClusterKeyguard);
        mNetworkController.removeSignalCallback(signalClusterQs);
        if (mQSPanel != null && mQSPanel.getHost() != null) {
            mQSPanel.getHost().destroy();
        }
        /*PRIZE-register launcher theme change receiver-liufan-2016-05-12-start*/
        LoadIconUtils.unRegisterLauncherThemeReceiver(mContext, mReceiver);
        /*PRIZE-register launcher theme change receiver-liufan-2016-05-12-end*/
        /*PRIZE-add for magazine-liufan-2018-01-19-start*/
        if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW) {
            if(mMagazineGateObserver != null){
                mMagazineGateObserver.stopObserving();
            }
        }
        /*PRIZE-add for magazine-liufan-2018-01-19-end*/

        /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
        if(OPEN_LIUHAI_SCREEN2){
            unregisterBroadcastForLiuHai(mContext);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
    }

    /*PRIZE-锁屏充电动画-liufan-2015-7-8-start*/
    private float downY;
    private int battery;
    private int batteryStatus;
    private KeyguardChargeAnimationView mKeyguardChargeAnimationView;
    private int keyguardAnimState;
    private final int KEYGUARD_CHARGE_ANIMATION_TIME = 500;
    private final int KEYGUARD_CHARGE_ANIMATION_SHOWING_TIME = 3500;
    private ValueAnimator chargeViewShowAnimator;
    private ValueAnimator chargeViewHideAnimator;
    private boolean isShowAnimationWhenCharge;
    
    private BroadcastReceiver mBatteryTracker = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                battery = (int)(100f
                        * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                        / intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));

                int plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                boolean plugged = plugType != 0;
                int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH,
                        BatteryManager.BATTERY_HEALTH_UNKNOWN);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN);
                String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
                int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                Log.d("BatteryTileDefined","BatteryTracker---->level--->"+battery+"---plugType--->"+plugType+"---plugged--->"+plugged+"---health--->"+health
                        +"---status--->"+status+"---technology--->"+technology+"---voltage--->"+voltage+"---temperature--->"+temperature);
                //status == BatteryManager.BATTERY_STATUS_CHARGING 
                //status == BatteryManager.BATTERY_STATUS_FULL;
                mKeyguardChargeAnimationView.setBattery(battery);
                if(batteryStatus != status){
                    isShowAnimationWhenCharge = false;
                    batteryStatus = status;
                    //isShowKeyguardChargingAnimation(true,false,true);
                    showKeyguardChargingAnimationWhenCharge(true);
                }
            }
        }
    };

    public void setKeyguardChargeAnimationViewBackground(){
        if(mKeyguardChargeAnimationView != null){
            Drawable d = mKeyguardChargeAnimationView.getBackground();
            if(d instanceof BitmapDrawable){
                BitmapDrawable bd = (BitmapDrawable)d;
                Bitmap bitmap = bd.getBitmap();
                if(bitmap != null && !bitmap.isRecycled()){
                    bitmap.recycle();
                    bitmap = null;
                }
            }
            Bitmap bitmap = null;
            d = mBackdropBack.getDrawable();
            if(d instanceof BitmapDrawable){
                BitmapDrawable bd = (BitmapDrawable)d;
                bitmap = bd.getBitmap();
            }
            if(d instanceof LockscreenWallpaper.WallpaperDrawable){
                LockscreenWallpaper.WallpaperDrawable wd = (LockscreenWallpaper.WallpaperDrawable)d;
                if(wd != null) bitmap = wd.getConstantState().getBackground();
            }
            if(isUseHaoKan()){//update by haokan-liufan-2016-10-11
				if(isUseHaoKan() && mNotificationPanel.isHaoKanViewShow()){
					bitmap = mNotificationPanel.getCurrentImage();
				}else{
				    bitmap = screenshot();
				}
			}
            if(bitmap == null){
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
                Drawable wallpaperDrawable = wallpaperManager.getDrawable();
                bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
            }
            if(bitmap!=null){
                bitmap = BlurPic.blurScaleOtherRadius(bitmap,5);
            }
            if(bitmap!=null){
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(0xbb000000);
            }
            BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), bitmap);
            mKeyguardChargeAnimationView.bringToFront();
            mKeyguardChargeAnimationView.setBackground(bd);
        }
    }
    
    public void showKeyguardChargingAnimationWhenCharge(boolean isNeedStopAnim){
        if ((mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED) 
            && (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING || batteryStatus == BatteryManager.BATTERY_STATUS_FULL)){
			Log.d(TAG,"showKeyguardChargingAnimationWhenCharge--isShowAnimationWhenCharge--->"+isShowAnimationWhenCharge);
            if(isShowAnimationWhenCharge){
                return;
            }
            if(chargeViewHideAnimator != null){
                chargeViewHideAnimator.cancel();
                chargeViewHideAnimator = null;
            }
            if(chargeViewShowAnimator!=null){
                chargeViewShowAnimator.cancel();
                chargeViewShowAnimator = null;
            }
            setKeyguardChargeAnimationViewBackground();
            mKeyguardChargeAnimationView.setVisibility(View.VISIBLE);
            mKeyguardChargeAnimationView.start();
            final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(KEYGUARD_CHARGE_ANIMATION_TIME);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mKeyguardChargeAnimationView.setAlpha((Float) animation.getAnimatedValue());
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mKeyguardChargeAnimationView.setAlpha(1f);
                    mKeyguardChargeAnimationView.setVisibility(View.VISIBLE);
                    
                    int N = mNotificationData.getActiveNotifications().size();
                    //N = 0;
                    //if(!isShowLimitByNotifications(N)){
                        mHandler.postDelayed(hideKeyguardChargeRunable, KEYGUARD_CHARGE_ANIMATION_SHOWING_TIME);
                    //}
                    isShowAnimationWhenCharge = false;
                }
            });
            animator.start();
            isShowAnimationWhenCharge = true;
        } else {
			Log.d(TAG,"showKeyguardChargingAnimationWhenCharge--hideKeyguardChargeAnimation");
            hideKeyguardChargeAnimation(isNeedStopAnim);
        }
    }
    /**
    * 方法描述：根据通知判断是否显示充电动画
    */
    public boolean isShowLimitByNotifications(int notificationSize){
        boolean show = true;
        if(notificationSize != 0){
            if(Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, 0) != 0){
                show = false;
            }
        }
        return show;
    }
    
    /**
    * 方法描述：是否显示锁屏充电动画
    */
    public void isShowKeyguardChargingAnimation(boolean isShow, boolean nSize,boolean isNeedStopAnim){
        if(isShowAnimationWhenCharge){
            return;
        }
        if(!isShow){
            hideKeyguardChargeAnimation(isNeedStopAnim);
            return;
        }
        
        int N = nSize ? mNotificationData.getActiveNotifications().size() : 0;
        //N = 0;
        if (mState == StatusBarState.KEYGUARD && (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING
            || batteryStatus == BatteryManager.BATTERY_STATUS_FULL) && isShowLimitByNotifications(N) && !isQsExpanded() && !mStatusBarKeyguardViewManager.isBouncerShowing()){
            showKeyguardChargingAnimation();
        } else {
            hideKeyguardChargeAnimation(isNeedStopAnim);
        }
    }
    
    /**
    * 方法描述：显示锁屏充电动画
    */
    public void showKeyguardChargingAnimation(){
        if(chargeViewHideAnimator != null){
            chargeViewHideAnimator.cancel();
            chargeViewHideAnimator = null;
        }
        if(chargeViewShowAnimator!=null){
            return;
        }
        mKeyguardChargeAnimationView.setVisibility(View.VISIBLE);
        chargeViewShowAnimator = ValueAnimator.ofFloat(0f, 1f);
        chargeViewShowAnimator.setDuration(KEYGUARD_CHARGE_ANIMATION_TIME);
        chargeViewShowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mKeyguardChargeAnimationView.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        chargeViewShowAnimator.addListener(new AnimatorListenerAdapter() {
            
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                chargeViewShowAnimator = null;
                mKeyguardChargeAnimationView.setAlpha(1f);
                mKeyguardChargeAnimationView.setVisibility(View.VISIBLE);
                mKeyguardChargeAnimationView.start();
                
                int N = mNotificationData.getActiveNotifications().size();
                //N = 0;
                if(!isShowLimitByNotifications(N)){
                    mHandler.postDelayed(hideKeyguardChargeRunable, KEYGUARD_CHARGE_ANIMATION_SHOWING_TIME);
                }
            }
        });
        chargeViewShowAnimator.start();
    }
    
    /**
    * 方法描述：隐藏锁屏充电动画
    */
    public void hideKeyguardChargeAnimation(final boolean isNeedStopAnim){
        if(chargeViewShowAnimator != null){
            chargeViewShowAnimator.cancel();
            chargeViewShowAnimator = null;
        }
        if(chargeViewHideAnimator!=null){
            return;
        }
        chargeViewHideAnimator = ValueAnimator.ofFloat(1f, 0f);
        chargeViewHideAnimator.setDuration(KEYGUARD_CHARGE_ANIMATION_TIME);
        chargeViewHideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mKeyguardChargeAnimationView.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        chargeViewHideAnimator.addListener(new AnimatorListenerAdapter() {
            
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                chargeViewHideAnimator = null;
                mKeyguardChargeAnimationView.setAlpha(0f);
                mKeyguardChargeAnimationView.setVisibility(View.INVISIBLE);
                if(isNeedStopAnim){
                    mKeyguardChargeAnimationView.stop();
                }
            }
        });
        chargeViewHideAnimator.start();
    }
    
    Runnable hideKeyguardChargeRunable = new Runnable(){
        @Override
        public void run() {
            hideKeyguardChargeAnimation(true);
        }
    };
    Runnable showKeyguardChargeRunable = new Runnable(){
        @Override
        public void run() {
            showKeyguardChargingAnimation();
        }
    };
    /*PRIZE-锁屏充电动画-liufan-2015-7-8-end*/
    private boolean mDemoModeAllowed;
    private boolean mDemoMode;

    @Override
    public void dispatchDemoCommand(String command, Bundle args) {
        if (!mDemoModeAllowed) {
            mDemoModeAllowed = Settings.Global.getInt(mContext.getContentResolver(),
                    DEMO_MODE_ALLOWED, 0) != 0;
        }
        if (!mDemoModeAllowed) return;
        if (command.equals(COMMAND_ENTER)) {
            mDemoMode = true;
        } else if (command.equals(COMMAND_EXIT)) {
            mDemoMode = false;
            checkBarModes();
        } else if (!mDemoMode) {
            // automatically enter demo mode on first demo command
            dispatchDemoCommand(COMMAND_ENTER, new Bundle());
        }
        boolean modeChange = command.equals(COMMAND_ENTER) || command.equals(COMMAND_EXIT);
        if ((modeChange || command.equals(COMMAND_VOLUME)) && mVolumeComponent != null) {
            mVolumeComponent.dispatchDemoCommand(command, args);
        }
        if (modeChange || command.equals(COMMAND_CLOCK)) {
            dispatchDemoCommandToView(command, args, R.id.clock);
        }
        if (modeChange || command.equals(COMMAND_BATTERY)) {
            mBatteryController.dispatchDemoCommand(command, args);
        }
        if (modeChange || command.equals(COMMAND_STATUS)) {
            mIconController.dispatchDemoCommand(command, args);
        }
        if (mNetworkController != null && (modeChange || command.equals(COMMAND_NETWORK))) {
            mNetworkController.dispatchDemoCommand(command, args);
        }
        if (modeChange || command.equals(COMMAND_NOTIFICATIONS)) {
            View notifications = mStatusBarView == null ? null
                    : mStatusBarView.findViewById(R.id.notification_icon_area);
            if (notifications != null) {
                String visible = args.getString("visible");
                int vis = mDemoMode && "false".equals(visible) ? View.INVISIBLE : View.VISIBLE;
                notifications.setVisibility(vis);
            }
        }
        if (command.equals(COMMAND_BARS)) {
            String mode = args.getString("mode");
            int barMode = "opaque".equals(mode) ? MODE_OPAQUE :
                    "translucent".equals(mode) ? MODE_TRANSLUCENT :
                    "semi-transparent".equals(mode) ? MODE_SEMI_TRANSPARENT :
                    "transparent".equals(mode) ? MODE_TRANSPARENT :
                    "warning".equals(mode) ? MODE_WARNING :
                    -1;
            if (barMode != -1) {
                boolean animate = true;
                if (mStatusBarView != null) {
                    mStatusBarView.getBarTransitions().transitionTo(barMode, animate);
                }
                if (mNavigationBarView != null) {
                    mNavigationBarView.getBarTransitions().transitionTo(barMode, animate);
                }
            }
        }
    }

    private void dispatchDemoCommandToView(String command, Bundle args, int id) {
        if (mStatusBarView == null) return;
        View v = mStatusBarView.findViewById(id);
        if (v instanceof DemoMode) {
            ((DemoMode)v).dispatchDemoCommand(command, args);
        }
    }

    /**
     * @return The {@link StatusBarState} the status bar is in.
     */
    public int getBarState() {
        return mState;
    }

    /*PRIZE-add,add isshowkeyguard flag-liufan-2016-06-03-start*/
    private boolean isShowKeyguard = false;
    public boolean isShowKeyguard(){
        return isShowKeyguard;
    }
    /*PRIZE-add,add isshowkeyguard flag-liufan-2016-06-03-end*/

    @Override
    public boolean isPanelFullyCollapsed() {
        return mNotificationPanel.isFullyCollapsed();
    }

    public void showKeyguard() {
        if (mLaunchTransitionFadingAway) {
            mNotificationPanel.animate().cancel();
            onLaunchTransitionFadingEnded();
        }
        mHandler.removeMessages(MSG_LAUNCH_TRANSITION_TIMEOUT);
        /*PRIZE-add,set alpha to 0 before show-liufan-2016-06-03-start*/
        if(isHideKeyguard && isPanelFullyCollapsed()){
            isHideKeyguard = false;
            isShowKeyguard = true;
            mBlurBack.setAlpha(0f);
            mBackdropBack.setAlpha(0f);
        }
        /*PRIZE-add,set alpha to 0 before show-liufan-2016-06-03-end*/
        if (mUserSwitcherController != null && mUserSwitcherController.useFullscreenUserSwitcher()) {
            setBarState(StatusBarState.FULLSCREEN_USER_SWITCHER);
        } else {
            setBarState(StatusBarState.KEYGUARD);
        }

        updateKeyguardState(false /* goingToFullShade */, false /* fromShadeLocked */);
        if (!mDeviceInteractive) {

            // If the screen is off already, we need to disable touch events because these might
            // collapse the panel after we expanded it, and thus we would end up with a blank
            // Keyguard.
            mNotificationPanel.setTouchDisabled(true);
        }
        if (mState == StatusBarState.KEYGUARD) {
            instantExpandNotificationsPanel();
        } else if (mState == StatusBarState.FULLSCREEN_USER_SWITCHER) {
            instantCollapseNotificationPanel();
        }
        mLeaveOpenOnKeyguardHide = false;
        if (mDraggedDownRow != null) {
            mDraggedDownRow.setUserLocked(false);
            mDraggedDownRow.notifyHeightChanged(false  /* needsAnimation */);
            mDraggedDownRow = null;
        }
        mPendingRemoteInputView = null;
        mAssistManager.onLockscreenShown();
        /*PRIZE-add for bugid:46915-liufan-2017-12-27-start*/
        mNotificationPanel.dismissNotificationBgWhenHeadsUp(true);
        /*PRIZE-add for bugid:46915-liufan-2017-12-27-end*/
    }

	/*PRIZE-add for bugid: 33832-zhudaopeng-2017-05-19-Start*/
    public void overTouchEvent(){
        mNotificationPanel.overTouchEvent();
    }
    /*PRIZE-add for bugid: 33832-zhudaopeng-2017-05-19-End*/
    
    /*PRIZE-show with panelview,bugid:29275- liufan-2017-03-01-start*/
    public void showHaoKanView(){
        boolean isUseHaoKan = isOpenMagazine();
        NotificationPanelView.debugMagazine("showkeyguard isUseHaoKan : " + isUseHaoKan + ", isFristRun : " + isFristRun
                + ", isSlipInMagazine : " + !NotificationPanelView.IS_ShowNotification_WhenShowHaoKan
                + ", IS_USE_HAOKAN : " + IS_USE_HAOKAN + ", isShowMagazineView : " + !NotificationPanelView.IS_ShowHaoKanView);
        boolean flag = false;
        if(isUseHaoKan){
            if(isUseHaoKan != IS_USE_HAOKAN
                || isFristRun == 0
                || !NotificationPanelView.IS_ShowHaoKanView
                || !NotificationPanelView.IS_ShowNotification_WhenShowHaoKan){
                flag = true;
            }
        } else {
            if(isFristRun != 0) {
                flag = true;
            }
        }
        IS_USE_HAOKAN = isUseHaoKan;
        if(flag){
            /*if(NotificationPanelView.USE_VLIFE || NotificationPanelView.USE_ZOOKING){ //add by zookingsoft 20170112
                IS_USE_HAOKAN = false;
            }*/
            refreshHaoKanState();
            if(isFristRun == 0 && IS_USE_HAOKAN){
                if(mNotificationPanel!=null) mNotificationPanel.updateScreenOn();
            }
        }
        if(isFristRun == 0){
            isFristRun = 1;
        }
        if(!isUseHaoKan) {
            updateMediaMetaData(true,false);
            if(mNotificationPanel!=null) {
                if(mState != StatusBarState.SHADE){
                    mNotificationPanel.showKeyguardViewWhenCloseHaoKan();
                } else {
                    NotificationPanelView.debugMagazine("mState is StatusBarState.SHADE");
                }
            }
        } else {
            hideHaoKanWallPaperInBackdropBack();
        }
        setTextColor();//add for bugid:53800-liufan-2018-3-27
    }

    public boolean isOpenMagazine(){
        boolean isOpen = Settings.System.getInt(mContext.getContentResolver(),
            Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH, 0) == 1 ? true : false;
        return isOpen;
    }
    
    public void onStatusBarHeightChange(int height){
        NotificationPanelView.debugMagazine("statusbar height changed to : " + height);
        if(height != -1){
            if(NotificationPanelView.IS_ShowHaoKanView){ 
                mNotificationPanel.setShowHaokanFunction(false);
            }
            hideHaoKanWallPaperInBackdropBack();
        } else {
            
        }
    }
    /*PRIZE-show with panelview,bugid:29275- liufan-2017-03-01-end*/
    
    /*PRIZE-add for bugid: 23195-liufan-2016-10-17-start*/
    public void refreshBlurBgWhenLockscreen(){
        if((PrizeOption.PRIZE_CHANGED_WALLPAPER || PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW)&&!bChangedWallpaperIsOpen&&!bIntoSuperSavingPower){
            isShowBlurBgWhenLockscreen(false);
        }
    }
    /*PRIZE-add for bugid: 23195-liufan-2016-10-17-end*/

    /*PRIZE-add,show blur layout when after show keyguard-liufan-2016-06-03-start*/
    public void showBlurOnGloableLayout(){
        if(isShowKeyguard){
            isShowKeyguard = false;
            showLockscreenWallpaper(false);
        }
        mBlurBack.setAlpha(1f);
        //mBlurBack.setVisibility(View.VISIBLE);
        mBackdropBack.setAlpha(1f);
    }
	
	public ImageView getBackdropBack(){
		return mBackdropBack;
	}
    /*PRIZE-add,show blur layout when after show keyguard-liufan-2016-06-03-end*/

    private void onLaunchTransitionFadingEnded() {
        mNotificationPanel.setAlpha(1.0f);
        mNotificationPanel.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        runLaunchTransitionEndRunnable();
        mLaunchTransitionFadingAway = false;
        mScrimController.forceHideScrims(false /* hide */);
        updateMediaMetaData(true /* metaDataChanged */, true);
    }

    @Override
    public boolean isCollapsing() {
        return mNotificationPanel.isCollapsing();
    }

    @Override
    public void addPostCollapseAction(Runnable r) {
        mPostCollapseRunnables.add(r);
    }

    public boolean isInLaunchTransition() {
        return mNotificationPanel.isLaunchTransitionRunning()
                || mNotificationPanel.isLaunchTransitionFinished();
    }

    //prize modify by xiarui for Bug#52143 2018-03-16 @{

    public boolean isInLaunchTransitionRunning() {
        return mNotificationPanel.isLaunchTransitionRunning();
    }

    //---end by xiarui@}

    /**
     * Fades the content of the keyguard away after the launch transition is done.
     *
     * @param beforeFading the runnable to be run when the circle is fully expanded and the fading
     *                     starts
     * @param endRunnable the runnable to be run when the transition is done
     */
    public void fadeKeyguardAfterLaunchTransition(final Runnable beforeFading,
            Runnable endRunnable) {
        mHandler.removeMessages(MSG_LAUNCH_TRANSITION_TIMEOUT);
        mLaunchTransitionEndRunnable = endRunnable;
        Runnable hideRunnable = new Runnable() {
            @Override
            public void run() {
                mLaunchTransitionFadingAway = true;
                if (beforeFading != null) {
                    beforeFading.run();
                }
                mScrimController.forceHideScrims(true /* hide */);
                updateMediaMetaData(false, true);
                mNotificationPanel.setAlpha(1);
                mStackScroller.setParentFadingOut(true);
                mNotificationPanel.animate()
                        .alpha(0)
                        .setStartDelay(FADE_KEYGUARD_START_DELAY)
                        .setDuration(FADE_KEYGUARD_DURATION)
                        .withLayer()
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                onLaunchTransitionFadingEnded();
                            }
                        });
                mIconController.appTransitionStarting(SystemClock.uptimeMillis(),
                        StatusBarIconController.DEFAULT_TINT_ANIMATION_DURATION);
            }
        };
        if (mNotificationPanel.isLaunchTransitionRunning()) {
            /*PRIZE-show status bar,bugid: 21521-liufan-2016-09-24-start*/
            mLaunchTransitionFadingAway = true;
            /*PRIZE-show status bar,bugid: 21521-liufan-2016-09-24-end*/
            mNotificationPanel.setLaunchTransitionEndRunnable(hideRunnable);
        } else {
            hideRunnable.run();
        }
    }

    /**
     * Fades the content of the Keyguard while we are dozing and makes it invisible when finished
     * fading.
     */
    public void fadeKeyguardWhilePulsing() {
        mNotificationPanel.animate()
                .alpha(0f)
                .setStartDelay(0)
                .setDuration(FADE_KEYGUARD_DURATION_PULSING)
                .setInterpolator(ScrimController.KEYGUARD_FADE_OUT_INTERPOLATOR)
                .start();
    }

    /**
     * Plays the animation when an activity that was occluding Keyguard goes away.
     */
    public void animateKeyguardUnoccluding() {
        mScrimController.animateKeyguardUnoccluding(500);
        mNotificationPanel.setExpandedFraction(0f);
        animateExpandNotificationsPanel();
    }

    /**
     * Starts the timeout when we try to start the affordances on Keyguard. We usually rely that
     * Keyguard goes away via fadeKeyguardAfterLaunchTransition, however, that might not happen
     * because the launched app crashed or something else went wrong.
     */
    public void startLaunchTransitionTimeout() {
        mHandler.sendEmptyMessageDelayed(MSG_LAUNCH_TRANSITION_TIMEOUT,
                LAUNCH_TRANSITION_TIMEOUT_MS);
    }

    private void onLaunchTransitionTimeout() {
        Log.w(TAG, "Launch transition: Timeout!");
        mNotificationPanel.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        mNotificationPanel.resetViews();
    }

    private void runLaunchTransitionEndRunnable() {
        if (mLaunchTransitionEndRunnable != null) {
            Runnable r = mLaunchTransitionEndRunnable;

            // mLaunchTransitionEndRunnable might call showKeyguard, which would execute it again,
            // which would lead to infinite recursion. Protect against it.
            mLaunchTransitionEndRunnable = null;
            r.run();
        }
    }

    /**
     * @return true if we would like to stay in the shade, false if it should go away entirely
     */
    public boolean hideKeyguard() {
        Log.d("123456","hideKeyguard() start");//add by lihuangyuan
        Trace.beginSection("PhoneStatusBar#hideKeyguard");
        boolean staying = mLeaveOpenOnKeyguardHide;
        setBarState(StatusBarState.SHADE);
        View viewToClick = null;
        if (mLeaveOpenOnKeyguardHide) {
            mLeaveOpenOnKeyguardHide = false;
            long delay = calculateGoingToFullShadeDelay();
            mNotificationPanel.animateToFullShade(delay);
            if (mDraggedDownRow != null) {
                mDraggedDownRow.setUserLocked(false);
                mDraggedDownRow = null;
            }
            viewToClick = mPendingRemoteInputView;
            mPendingRemoteInputView = null;

            // Disable layout transitions in navbar for this transition because the load is just
            // too heavy for the CPU and GPU on any device.
            if (mNavigationBarView != null) {
                mNavigationBarView.setLayoutTransitionsEnabled(false);
                mNavigationBarView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNavigationBarView.setLayoutTransitionsEnabled(true);
                    }
                }, delay + StackStateAnimator.ANIMATION_DURATION_GO_TO_FULL_SHADE);
            }
        } else {
            instantCollapseNotificationPanel();
        }
        updateKeyguardState(staying, false /* fromShadeLocked */);

        if (viewToClick != null && viewToClick.isAttachedToWindow()) {
            viewToClick.callOnClick();
        }

        // Keyguard state has changed, but QS is not listening anymore. Make sure to update the tile
        // visibilities so next time we open the panel we know the correct height already.
        if (mQSPanel != null) {
            mQSPanel.refreshAllTiles();
        }
        mHandler.removeMessages(MSG_LAUNCH_TRANSITION_TIMEOUT);
        releaseGestureWakeLock();
        mNotificationPanel.onAffordanceLaunchEnded();
        mNotificationPanel.animate().cancel();
        mNotificationPanel.setAlpha(1f);
        Trace.endSection();
        /*PRIZE-set mBlurBack Layout Gone when hideKeyguard-liufan-2016-06-03-start*/
        int isSystemFlashOn = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, -1);
        if(isSystemFlashOn==0){
            ReleaseFlash();
        }
        hideKeyguardChargeAnimation(true);
        recycleLockscreenWallpaper();
        recycleBlurWallpaper();
        if(mBlurBack.getVisibility() != View.GONE){
            mBlurBack.setVisibility(View.GONE);
        }
        /*PRIZE-set mBlurBack Layout Gone when hideKeyguard-liufan-2016-06-03-end*/
        if(!isOccluded()){
            hideHaoKanWallPaperInBackdropBack();
        }
        NotificationPanelView.debugMagazine("hideKeyguard() End");
        Log.d("123456","hideKeyguard() End staying:"+staying);//add by lihuangyuan
        return staying;
    }

    private void releaseGestureWakeLock() {
        if (mGestureWakeLock.isHeld()) {
            mGestureWakeLock.release();
        }
    }

    public void ReleaseFlash() {  
        /*modify-by-zhongweilin
        ContentValues values = new ContentValues();  
        values.put("flashstatus","2"); 
        mContext.getContentResolver().update(Uri.parse("content://com.android.flash/systemflashs"), values, null, null);
       */
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, 2);
    }

    public long calculateGoingToFullShadeDelay() {
        return mKeyguardFadingAwayDelay + mKeyguardFadingAwayDuration;
    }

    /**
     * Notifies the status bar that Keyguard is going away very soon.
     */
    public void keyguardGoingAway() {

        // Treat Keyguard exit animation as an app transition to achieve nice transition for status
        // bar.
        mKeyguardGoingAway = true;
        mIconController.appTransitionPending();
    }

    /**
     * Notifies the status bar the Keyguard is fading away with the specified timings.
     *
     * @param startTime the start time of the animations in uptime millis
     * @param delay the precalculated animation delay in miliseconds
     * @param fadeoutDuration the duration of the exit animation, in milliseconds
     */
    public void setKeyguardFadingAway(long startTime, long delay, long fadeoutDuration) {
        mKeyguardFadingAway = true;
        mKeyguardFadingAwayDelay = delay;
        mKeyguardFadingAwayDuration = fadeoutDuration;
        mWaitingForKeyguardExit = false;
        mIconController.appTransitionStarting(
                startTime + fadeoutDuration
                        - StatusBarIconController.DEFAULT_TINT_ANIMATION_DURATION,
                StatusBarIconController.DEFAULT_TINT_ANIMATION_DURATION);
        recomputeDisableFlags(fadeoutDuration > 0 /* animate */);
    }

    public boolean isKeyguardFadingAway() {
        return mKeyguardFadingAway;
    }

    /**
     * Notifies that the Keyguard fading away animation is done.
     */
    public void finishKeyguardFadingAway() {
        mKeyguardFadingAway = false;
        mKeyguardGoingAway = false;
    }

    public void stopWaitingForKeyguardExit() {
        mWaitingForKeyguardExit = false;
    }

    private void updatePublicMode() {
        boolean isPublic = false;
        if (mStatusBarKeyguardViewManager.isShowing()) {
            for (int i = mCurrentProfiles.size() - 1; i >= 0; i--) {
                UserInfo userInfo = mCurrentProfiles.valueAt(i);
                if (mStatusBarKeyguardViewManager.isSecure(userInfo.id)) {
                    isPublic = true;
                    break;
                }
            }
        }
        setLockscreenPublicMode(isPublic);
    }

    protected void updateKeyguardState(boolean goingToFullShade, boolean fromShadeLocked) {
        Trace.beginSection("PhoneStatusBar#updateKeyguardState");
        if (mState == StatusBarState.KEYGUARD) {
            mKeyguardIndicationController.setVisible(true);
            mNotificationPanel.resetViews();
            if (mKeyguardUserSwitcher != null) {
                mKeyguardUserSwitcher.setKeyguard(true, fromShadeLocked);
            }
            mStatusBarView.removePendingHideExpandedRunnables();
        } else {
            mKeyguardIndicationController.setVisible(false);
            if (mKeyguardUserSwitcher != null) {
                mKeyguardUserSwitcher.setKeyguard(false,
                        goingToFullShade ||
                        mState == StatusBarState.SHADE_LOCKED ||
                        fromShadeLocked);
            }
        }
        if (mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED) {
            mScrimController.setKeyguardShowing(true);
        } else {
            mScrimController.setKeyguardShowing(false);
        }
        mIconPolicy.notifyKeyguardShowingChanged();
        mNotificationPanel.setBarState(mState, mKeyguardFadingAway, goingToFullShade);
        updateDozingState();
        updatePublicMode();
        updateStackScrollerState(goingToFullShade, fromShadeLocked);
        updateNotifications();
        checkBarModes();
        /// M: Support "Operator plugin - Customize Carrier Label for PLMN". @{
        updateCarrierLabelVisibility(false);
        /// M: Support "Operator plugin - Customize Carrier Label for PLMN". @}
        updateMediaMetaData(false, mState != StatusBarState.KEYGUARD);
        mKeyguardMonitor.notifyKeyguardState(mStatusBarKeyguardViewManager.isShowing(),
                mStatusBarKeyguardViewManager.isSecure(),
                mStatusBarKeyguardViewManager.isOccluded());
        /*PRIZE-dismiss the blur background when lockscreen- liufan-2015-06-10-start*/
        if (VersionControl.CUR_VERSION == VersionControl.BLUR_BG_VER) {
            if(mState == StatusBarState.KEYGUARD  || mState == StatusBarState.SHADE_LOCKED){
                Log.e("liufan","updateKeyguardState-----cancelNotificationBackground---->");
                cancelNotificationBackground();
            }
        }
        /*PRIZE-dismiss the blur background when lockscreen- liufan-2015-06-10-end*/
        /*PRIZE-dismiss the text of no SIM card when lockscreen- liyao-2015-07-01-start*/
        final boolean curLockScreen = mState == StatusBarState.KEYGUARD  || mState == StatusBarState.SHADE_LOCKED;
        Log.d(TAG, "curLockScreen: " + curLockScreen);
        //if(mLastLockScreen != curLockScreen){
            AsyncTask.execute(new Runnable() {
                public void run() {
                    Settings.System.putInt(mContext.getContentResolver(), "in_lock_screen", curLockScreen ? 1:0);
                }
            });
        //}
       // mLastLockScreen = curLockScreen;
        /*PRIZE-dismiss the text of no SIM card when lockscreen- liyao-2015-07-01-end*/
        /*PRIZE-add for background color-liufan-2017-08-28-start*/
        refreshStatusBarBackgroundColor();
        /*PRIZE-add for background color-liufan-2017-08-28-end*/

        /*PRIZE-update for bugid:44751-liufan-2017-12-11-start*/
        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams)mStackScroller.getLayoutParams();
        if(mState == StatusBarState.SHADE){
            p.bottomMargin = 0;
        }else{
            int mBottom = mContext.getResources().getDimensionPixelSize(R.dimen.close_handle_underlap);
            p.bottomMargin = mBottom;
        }
        mStackScroller.setLayoutParams(p);
        /*PRIZE-update for bugid:44751-liufan-2017-12-11-end*/
        /*PRIZE-add for bugid:43587-liufan-2017-12-12-start*/
        if(mState == StatusBarState.KEYGUARD){
            if(mNotificationPanel != null) mNotificationPanel.changePadding(1);
        } else {
            if(mNotificationPanel != null) mNotificationPanel.changePadding(0);
        }
        /*PRIZE-add for bugid:43587-liufan-2017-12-12-end*/
        Trace.endSection();
    }

    private void updateDozingState() {
        Trace.beginSection("PhoneStatusBar#updateDozingState");
        boolean animate = !mDozing && mDozeScrimController.isPulsing();
        mNotificationPanel.setDozing(mDozing, animate);
        mStackScroller.setDark(mDozing, animate, mWakeUpTouchLocation);
        mScrimController.setDozing(mDozing);

        // Immediately abort the dozing from the doze scrim controller in case of wake-and-unlock
        // for pulsing so the Keyguard fade-out animation scrim can take over.
        mDozeScrimController.setDozing(mDozing &&
                mFingerprintUnlockController.getMode()
                        != FingerprintUnlockController.MODE_WAKE_AND_UNLOCK_PULSING, animate);
        Trace.endSection();
    }

    public void updateStackScrollerState(boolean goingToFullShade, boolean fromShadeLocked) {
        if (mStackScroller == null) return;
        boolean onKeyguard = mState == StatusBarState.KEYGUARD;
        mStackScroller.setHideSensitive(isLockscreenPublicMode(), goingToFullShade);
        mStackScroller.setDimmed(onKeyguard, fromShadeLocked /* animate */);
        mStackScroller.setExpandingEnabled(!onKeyguard);
        ActivatableNotificationView activatedChild = mStackScroller.getActivatedChild();
        mStackScroller.setActivatedChild(null);
        if (activatedChild != null) {
            activatedChild.makeInactive(false /* animate */);
        }
    }

    public void userActivity() {
        if (mState == StatusBarState.KEYGUARD) {
            mKeyguardViewMediatorCallback.userActivity();
        }
    }

    public boolean interceptMediaKey(KeyEvent event) {
        return mState == StatusBarState.KEYGUARD
                && mStatusBarKeyguardViewManager.interceptMediaKey(event);
    }

    public boolean onMenuPressed() {
        if (mDeviceInteractive && mState != StatusBarState.SHADE
                && mStatusBarKeyguardViewManager.shouldDismissOnMenuPressed()) {
            animateCollapsePanels(
                    CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL /* flags */, true /* force */);
            return true;
        }
        return false;
    }

    public void endAffordanceLaunch() {
        releaseGestureWakeLock();
        mNotificationPanel.onAffordanceLaunchEnded();
    }

    public boolean onBackPressed() {
        if (mStatusBarKeyguardViewManager.onBackPressed()) {
            /*PRIZE-add for haokan-liufan-2017-10-31-start*/
            if(isUseHaoKan()){
                mNotificationPanel.onBackPressedForHaoKan();
                hideHaoKanWallPaperInBackdropBack();
                mKeyguardBottomArea.refreshView();
                isShowBlurBgWhenLockscreen(false);
                return true;
            }
            mKeyguardBottomArea.refreshView();
            /*PRIZE-add for haokan-liufan-2017-10-31-end*/
            isShowBlurBgWhenLockscreen(false);
            return true;
        }
        if (mNotificationPanel.isQsExpanded()) {
            /*PRIZE-quit the notification when press the back- liufan-2015-07-07-start*/
            if (mNotificationPanel.isQsDetailShowing()) {
                mNotificationPanel.closeQsDetail();
                return true;
            }
            /*else {
                mNotificationPanel.animateCloseQs();
            }*/
            /*PRIZE-quit the notification when press the back- liufan-2015-07-07-end*/
        }
        if (mState != StatusBarState.KEYGUARD && mState != StatusBarState.SHADE_LOCKED) {
            animateCollapsePanels();
            return true;
        }
        return false;
    }

    public boolean onSpacePressed() {
        if (mDeviceInteractive && mState != StatusBarState.SHADE) {
            animateCollapsePanels(
                    CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL /* flags */, true /* force */);
            return true;
        }
        return false;
    }

    private void showBouncer() {
        if (mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED) {
            mWaitingForKeyguardExit = mStatusBarKeyguardViewManager.isShowing();
            /*PRIZE-KeyguardBouncer avoid overlapping with the LockScreen-liufan-2015-09-17-start*/
            dismissKeyguardImmediately();
            /*PRIZE-KeyguardBouncer avoid overlapping with the LockScreen-liufan-2015-09-17-end*/
            mStatusBarKeyguardViewManager.dismiss();
            /*PRIZE-add for haokan-liufan-2017-10-30-start*/
            showHaoKanWallPaperWhenBouncerShowing();
            /*PRIZE-add for haokan-liufan-2017-10-30-end*/
            
            /*PRIZE-blur background-liufan-2015-09-04-start*/
            isShowBlurBgWhenLockscreen(false);
            ScrimController.isDismissScrim = false;
            mScrimController.setKeyguardShowing(mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED);
            /*PRIZE-blur background-liufan-2015-09-04-end*/
        }
    }
    
    /*PRIZE-KeyguardBouncer avoid overlapping with the LockScreen-liufan-2015-09-17-start*/
    public void dismissKeyguardImmediately(){
        mNotificationPanel.flingImmediately(0,false);

        /*PRIZE-add for haokan-liufan-2017-10-30-start*/
        showHaoKanWallPaperWhenBouncerShowing();
        /*PRIZE-add for haokan-liufan-2017-10-30-end*/
    }
    /*PRIZE-KeyguardBouncer avoid overlapping with the LockScreen-liufan-2015-09-17-end*/
    
    private void instantExpandNotificationsPanel() {

        // Make our window larger and the panel expanded.
        makeExpandedVisible(true);
        mNotificationPanel.expand(false /* animate */);
    }

    private void instantCollapseNotificationPanel() {
        mNotificationPanel.instantCollapse();
    }

    @Override
    public void onActivated(ActivatableNotificationView view) {
        EventLogTags.writeSysuiLockscreenGesture(
                EventLogConstants.SYSUI_LOCKSCREEN_GESTURE_TAP_NOTIFICATION_ACTIVATE,
                0 /* lengthDp - N/A */, 0 /* velocityDp - N/A */);
        /*PRIZE-update tip click mKeyguardIconOverflowContainer-liufan-2016-12-06-start*/
        if(!VersionControl.isAllowDropDown(mContext)){
            //Modify for background reflect by luyingying - 2017-09-09 
            //mKeyguardIndicationController.showTransientIndication(R.string.notification_tap_not_allow_dropdown);
            mKeyguardIndicationController.showTransientIndication(R.string.notification_tap_not_allow_dropdown, textColor);
            //Modify end
            if(mKeyguardIconOverflowContainer != null) mKeyguardIconOverflowContainer.setOnClickListener(null);
        } else {
            //mKeyguardIndicationController.showTransientIndication(R.string.notification_tap_again);
            //Modify for background reflect by luyingying - 2017-09-09 
            mKeyguardIndicationController.showTransientIndication(R.string.notification_tap_again, textColor);
            //Modify end
            if(mKeyguardIconOverflowContainer != null) mKeyguardIconOverflowContainer.setOnClickListener(mOverflowClickListener);
        }
        /*PRIZE-update tip click mKeyguardIconOverflowContainer-liufan-2016-12-06-end*/
        ActivatableNotificationView previousView = mStackScroller.getActivatedChild();
        if (previousView != null) {
            previousView.makeInactive(true /* animate */);
        }
        mStackScroller.setActivatedChild(view);
    }

    /*PRIZE-add isHideKeyguard-liufan-2016-06-20-start*/
    private boolean isHideKeyguard = false;
    /*PRIZE-add isHideKeyguard-liufan-2016-06-20-end*/


    public ButtonDispatcher getHomeButton() {
        return mNavigationBarView.getHomeButton();
    }
    /**
     * @param state The {@link StatusBarState} to set.
     */
    public void setBarState(int state) {
        // If we're visible and switched to SHADE_LOCKED (the user dragged
        // down on the lockscreen), clear notification LED, vibration,
        // ringing.
        // Other transitions are covered in handleVisibleToUserChanged().
        if (state != mState && mVisible && (state == StatusBarState.SHADE_LOCKED
                || (state == StatusBarState.SHADE && isGoingToNotificationShade()))) {
            clearNotificationEffects();
        }
        if (state == StatusBarState.KEYGUARD) {
            removeRemoteInputEntriesKeptUntilCollapsed();
            maybeEscalateHeadsUp();
        }
        mState = state;
        /*PRIZE-add isHideKeyguard-liufan-2016-06-20-start*/
        if(state == StatusBarState.SHADE){
            isHideKeyguard = true;
        }
        /*PRIZE-add isHideKeyguard-liufan-2016-06-20-end*/
        mGroupManager.setStatusBarState(state);
        mFalsingManager.setStatusBarState(state);
        mStatusBarWindowManager.setStatusBarState(state);
        updateReportRejectedTouchVisibility();
        /*PRIZE-refresh the virtual key show state-liufan-2015-12-14-start*/
        KeyguardViewMediator keyguardViewMediator = getComponent(KeyguardViewMediator.class);
        keyguardViewMediator.setStatusBarState(state,mStatusBarKeyguardViewManager.isBouncerShowing());
        /*PRIZE-refresh the virtual key show state-liufan-2015-12-14-start*/
        updateDozing();
    }

    @Override
    public void onActivationReset(ActivatableNotificationView view) {
        if (view == mStackScroller.getActivatedChild()) {
            mKeyguardIndicationController.hideTransientIndication();
            mStackScroller.setActivatedChild(null);
        }
    }

    public void onTrackingStarted() {
        runPostCollapseRunnables();
    }

    public void onClosingFinished() {
        runPostCollapseRunnables();
    }

    public void onUnlockHintStarted() {
        /*PRIZE-add for bugid:51190-liufan-2018-02-27-start*/
        if(mFalsingManager!=null) mFalsingManager.onUnlockHintStarted();
        /*PRIZE-add for bugid:51190-liufan-2018-02-27-end*/
        //Modify for background reflect by luyingying - 2017-09-09
        mKeyguardIndicationController.showTransientIndication(R.string.keyguard_unlock, textColor);
    }

    public void onHintFinished() {
        // Delay the reset a bit so the user can read the text.
        mKeyguardIndicationController.hideTransientIndicationDelayed(HINT_RESET_DELAY_MS);
    }

    public void onCameraHintStarted() {
        mFalsingManager.onCameraHintStarted();
        //Modify for background reflect by luyingying - 2017-09-09 
        mKeyguardIndicationController.showTransientIndication(R.string.camera_hint, textColor);
    }

    public void onVoiceAssistHintStarted() {
        mFalsingManager.onLeftAffordanceHintStarted();
        //Modify for background reflect by luyingying - 2017-09-09 
        mKeyguardIndicationController.showTransientIndication(R.string.voice_hint, textColor);
    }

    public void onPhoneHintStarted() {
        mFalsingManager.onLeftAffordanceHintStarted();

			mKeyguardIndicationController.showTransientIndication(R.string.phone_hint, textColor);

    }

    public void onTrackingStopped(boolean expand) {
        if (mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED) {
            if (!expand && !mUnlockMethodCache.canSkipBouncer()) {
                showBouncer();
            }
        }
    }

    @Override
    protected int getMaxKeyguardNotifications(boolean recompute) {
        if (recompute) {
            mMaxKeyguardNotifications = Math.max(1,
                    mNotificationPanel.computeMaxKeyguardNotifications(
                            mMaxAllowedKeyguardNotifications));
            return mMaxKeyguardNotifications;
        }
        return mMaxKeyguardNotifications;
    }

    public int getMaxKeyguardNotifications() {
        return getMaxKeyguardNotifications(false /* recompute */);
    }

    public NavigationBarView getNavigationBarView() {
        return mNavigationBarView;
    }

    // ---------------------- DragDownHelper.OnDragDownListener ------------------------------------


    /* Only ever called as a consequence of a lockscreen expansion gesture. */
    @Override
    public boolean onDraggedDown(View startingChild, int dragLengthY) {
        if (hasActiveNotifications()) {
            EventLogTags.writeSysuiLockscreenGesture(
                    EventLogConstants.SYSUI_LOCKSCREEN_GESTURE_SWIPE_DOWN_FULL_SHADE,
                    (int) (dragLengthY / mDisplayMetrics.density),
                    0 /* velocityDp - N/A */);

            // We have notifications, go to locked shade.
            goToLockedShade(startingChild);
            /*PRIZE-show BlurBack-liufan-2015-8-25-start*/
            /*tileHandler.postDelayed(new Runnable(){//cancel by haokan-2017-11-15
                @Override
                public void run() {
                    setBlurBackVisibility(View.INVISIBLE);
                }
            },100);*/
            /*PRIZE-show BlurBack-liufan-2015-8-25-end*/
            if (startingChild instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow row = (ExpandableNotificationRow) startingChild;
                row.onExpandedByGesture(true /* drag down is always an open */);
            }
            return true;
        } else {

            // No notifications - abort gesture.
            return false;
        }
    }

    @Override
    public void onDragDownReset() {
        mStackScroller.setDimmed(true /* dimmed */, true /* animated */);
        mStackScroller.resetScrollPosition();
    }

    @Override
    public void onCrossedThreshold(boolean above) {
        mStackScroller.setDimmed(!above /* dimmed */, true /* animate */);
    }

    @Override
    public void onTouchSlopExceeded() {
        mStackScroller.removeLongPressCallback();
    }

    @Override
    public void setEmptyDragAmount(float amount) {
        mNotificationPanel.setEmptyDragAmount(amount);
    }

    /**
     * If secure with redaction: Show bouncer, go to unlocked shade.
     *
     * <p>If secure without redaction or no security: Go to {@link StatusBarState#SHADE_LOCKED}.</p>
     *
     * @param expandView The view to expand after going to the shade.
     */
    public void goToLockedShade(View expandView) {
        ExpandableNotificationRow row = null;
        if (expandView instanceof ExpandableNotificationRow) {
            row = (ExpandableNotificationRow) expandView;
            row.setUserExpanded(true /* userExpanded */, true /* allowChildExpansion */);
            // Indicate that the group expansion is changing at this time -- this way the group
            // and children backgrounds / divider animations will look correct.
            row.setGroupExpansionChanging(true);
        }
        boolean fullShadeNeedsBouncer = !userAllowsPrivateNotificationsInPublic(mCurrentUserId)
                || !mShowLockscreenNotifications || mFalsingManager.shouldEnforceBouncer();
        if (isLockscreenPublicMode() && fullShadeNeedsBouncer) {
            mLeaveOpenOnKeyguardHide = true;
            showBouncer();
            mDraggedDownRow = row;
            mPendingRemoteInputView = null;
        } else {
            mNotificationPanel.animateToFullShade(0 /* delay */);
            setBarState(StatusBarState.SHADE_LOCKED);
            updateKeyguardState(false /* goingToFullShade */, false /* fromShadeLocked */);
        }
        /*-modify for haokan-liufan-2017-10-26-start-*/
        if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW
            && mState == StatusBarState.SHADE_LOCKED){
            mShowHaoKanRunnable = new Runnable(){
                public void run(){
                    if(NotificationPanelView.IS_ShowHaoKanView){
                        showHaoKanWallPaperInBackdropBack();
                        mNotificationPanel.setShowHaokanFunction(false);
                        View v = mNotificationPanel.getKeyguardBottomAreaView();
                        if(v != null){
                            v.setVisibility(View.VISIBLE);
                        }
                    }
                }
            };
        }
        /*-modify for haokan-liufan-2017-10-26-end-*/

        /*PRIZE-show blur background when pull on the lockscreen-liufan-2015-06-15-start*/
        if (VersionControl.CUR_VERSION == VersionControl.BLUR_BG_VER) {
            showBlurWallPaper();
        }
        /*PRIZE-show blur background when pull on the lockscreen-liufan-2015-06-15-end*/
    }

    @Override
    public void onLockedNotificationImportanceChange(OnDismissAction dismissAction) {
        mLeaveOpenOnKeyguardHide = true;
        dismissKeyguardThenExecute(dismissAction, true /* afterKeyguardGone */);
    }

    @Override
    protected void onLockedRemoteInput(ExpandableNotificationRow row, View clicked) {
        mLeaveOpenOnKeyguardHide = true;
        showBouncer();
        mPendingRemoteInputView = clicked;
    }

    @Override
    protected boolean startWorkChallengeIfNecessary(int userId, IntentSender intendSender,
            String notificationKey) {
        // Clear pending remote view, as we do not want to trigger pending remote input view when
        // it's called by other code
        mPendingWorkRemoteInputView = null;
        return super.startWorkChallengeIfNecessary(userId, intendSender, notificationKey);
    }

    @Override
    protected void onLockedWorkRemoteInput(int userId, ExpandableNotificationRow row,
            View clicked) {
        // Collapse notification and show work challenge
        animateCollapsePanels();
        startWorkChallengeIfNecessary(userId, null, null);
        // Add pending remote input view after starting work challenge, as starting work challenge
        // will clear all previous pending review view
        mPendingWorkRemoteInputView = clicked;
    }

    @Override
    protected void onWorkChallengeUnlocked() {
        if (mPendingWorkRemoteInputView != null) {
            final View pendingWorkRemoteInputView = mPendingWorkRemoteInputView;
            // Expand notification panel and the notification row, then click on remote input view
            final Runnable clickPendingViewRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mPendingWorkRemoteInputView != null) {
                        final View pendingWorkRemoteInputView = mPendingWorkRemoteInputView;
                        ViewParent p = pendingWorkRemoteInputView.getParent();
                        while (p != null) {
                            if (p instanceof ExpandableNotificationRow) {
                                final ExpandableNotificationRow row = (ExpandableNotificationRow) p;
                                ViewParent viewParent = row.getParent();
                                if (viewParent instanceof NotificationStackScrollLayout) {
                                    final NotificationStackScrollLayout scrollLayout =
                                            (NotificationStackScrollLayout) viewParent;
                                    row.makeActionsVisibile();
                                    row.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            final Runnable finishScrollingCallback = new Runnable()
                                            {
                                                @Override
                                                public void run() {
                                                    mPendingWorkRemoteInputView.callOnClick();
                                                    mPendingWorkRemoteInputView = null;
                                                    scrollLayout.setFinishScrollingCallback(null);
                                                }
                                            };
                                            if (scrollLayout.scrollTo(row)) {
                                                // It scrolls! So call it when it's finished.
                                                scrollLayout.setFinishScrollingCallback(
                                                        finishScrollingCallback);
                                            } else {
                                                // It does not scroll, so call it now!
                                                finishScrollingCallback.run();
                                            }
                                        }
                                    });
                                }
                                break;
                            }
                            p = p.getParent();
                        }
                    }
                }
            };
            mNotificationPanel.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (mNotificationPanel.mStatusBar.getStatusBarWindow()
                                    .getHeight() != mNotificationPanel.mStatusBar
                                            .getStatusBarHeight()) {
                                mNotificationPanel.getViewTreeObserver()
                                        .removeOnGlobalLayoutListener(this);
                                mNotificationPanel.post(clickPendingViewRunnable);
                            }
                        }
                    });
            instantExpandNotificationsPanel();
        }
    }

    @Override
    public void onExpandClicked(Entry clickedEntry, boolean nowExpanded) {
        mHeadsUpManager.setExpanded(clickedEntry, nowExpanded);
        if (mState == StatusBarState.KEYGUARD && nowExpanded) {
            goToLockedShade(clickedEntry.row);
        }
    }

    /*-modify for haokan-liufan-2017-10-26-start-*/
    public StatusBarKeyguardViewManager getStatusBarKeyguardViewManager(){
        return mStatusBarKeyguardViewManager;
    }
    private Runnable mShowHaoKanRunnable;

    public void showHaoKanWallPaperInBackdropBack(){
        if(isUseHaoKan()){
            Bitmap bm = null;
            Drawable d = mBackdropBack.getDrawable();
            if(d instanceof BitmapDrawable){
                BitmapDrawable bd = (BitmapDrawable)d;
                Bitmap bitmap = bd.getBitmap();
                if(bitmap != null){
                    NotificationPanelView.debugMagazine("showHaoKanWallPaperInBackdropBack bitmap is not null ");
                    return ;
                }
            }
            if(mNotificationPanel != null){
                bm = mNotificationPanel.getCurrentImage();
            }
            if(bm != null){
                NotificationPanelView.debugMagazine("showHaoKanWallPaperInBackdropBack bm : " + bm);
                mBackdropBack.setImageBitmap(bm);
                mBackdropBack.setAlpha(1f);
                mBackdrop.setVisibility(View.VISIBLE);
                mBackdrop.setAlpha(1f);
            }else{
                NotificationPanelView.debugMagazine("showHaoKanWallPaperInBackdropBack getCurrentImage is null");
            }
        }
    }

    private void hideHaoKanWallPaperInBackdropBack(){
        //add for bugid:56647-liufan-2018-5-9
        boolean isNeedFullscreenBouncer = mStatusBarKeyguardViewManager != null && mStatusBarKeyguardViewManager.needsFullscreenBouncer();
        if(isUseHaoKan() && mBackdropBack != null && !isNeedFullscreenBouncer){
            Drawable d = mBackdropBack.getDrawable();
            mBackdropBack.setImageBitmap(null);
            keyGuardBg = null;
            if(d instanceof BitmapDrawable){
                BitmapDrawable bd = (BitmapDrawable)d;
                Bitmap bitmap = bd.getBitmap();
                if(bitmap != null && !bitmap.isRecycled()){
                    bitmap.recycle();
                    bitmap = null;
                }
            }
            NotificationPanelView.debugMagazine("hide magazineWallPaperInBackdropBack");
            mBackdropBack.setAlpha(0f);
            mBackdrop.setVisibility(View.GONE);
            mBackdrop.setAlpha(0f);
        }
    }

    public void showHaoKanWallPaperWhenBouncerShowing(){
        boolean isBouncerShowing = mStatusBarKeyguardViewManager.isBouncerShowing();
        NotificationPanelView.debugMagazine("dismissKeyguard isBouncerShowing : "+isBouncerShowing);
        if(isBouncerShowing && !(PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode())) showHaoKanWallPaperInBackdropBack();
    }
    /*-modify for haokan-liufan-2017-10-26-end-*/

    /**
     * Goes back to the keyguard after hanging around in {@link StatusBarState#SHADE_LOCKED}.
     */
    public void goToKeyguard() {
        /*PRIZE-dismiss blur background when pull on the lockscreen-liufan-2015-06-15-start*/
        if (VersionControl.CUR_VERSION == VersionControl.BLUR_BG_VER) {
            cancelNotificationBackground();
                            Log.e("liufan","goToKeyguard-----cancelNotificationBackground---->");
        }
        /*PRIZE-dismiss blur background when pull on the lockscreen-liufan-2015-06-15-end*/
        if (mState == StatusBarState.SHADE_LOCKED) {
            //setBlurBackVisibility(View.VISIBLE);//cancel by haokan-2017-11-15
            mStackScroller.onGoToKeyguard();
            setBarState(StatusBarState.KEYGUARD);
            updateKeyguardState(false /* goingToFullShade */, true /* fromShadeLocked*/);
        }
        /*-modify for haokan-liufan-2017-10-10-start-*/
        if(isUseHaoKan() && !NotificationPanelView.IS_ShowHaoKanView){
            mNotificationPanel.setShowHaokanFunction(true);
            isShowBlurBgWhenLockscreen(false);
            hideHaoKanWallPaperInBackdropBack();
        }
        /*-modify for haokan-liufan-2017-10-10-end-*/
    }

    public long getKeyguardFadingAwayDelay() {
        return mKeyguardFadingAwayDelay;
    }

    public long getKeyguardFadingAwayDuration() {
        return mKeyguardFadingAwayDuration;
    }

    @Override
    public void setBouncerShowing(boolean bouncerShowing) {
        super.setBouncerShowing(bouncerShowing);
        mStatusBarView.setBouncerShowing(bouncerShowing);
        recomputeDisableFlags(true /* animate */);
        /*PRIZE-refresh the virtual key show state-liufan-2015-12-14-start*/
        KeyguardViewMediator keyguardViewMediator = getComponent(KeyguardViewMediator.class);
        keyguardViewMediator.setStatusBarState(mState,bouncerShowing);
        /*PRIZE-refresh the virtual key show state-liufan-2015-12-14-end*/
    }

    public void onStartedGoingToSleep() {
        mStartedGoingToSleep = true;
    }

    public void onFinishedGoingToSleep() {
        mNotificationPanel.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        mLaunchCameraOnScreenTurningOn = false;
        mStartedGoingToSleep = false;
        mDeviceInteractive = false;
        mWakeUpComingFromTouch = false;
        mWakeUpTouchLocation = null;
        mStackScroller.setAnimationsEnabled(false);
        updateVisibleToUser();
        if (mLaunchCameraOnFinishedGoingToSleep) {
            mLaunchCameraOnFinishedGoingToSleep = false;

            // This gets executed before we will show Keyguard, so post it in order that the state
            // is correct.
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onCameraLaunchGestureDetected(mLastCameraLaunchSource);
                }
            });
        }
    }

    public void onStartedWakingUp() {
        mDeviceInteractive = true;
        mStackScroller.setAnimationsEnabled(true);
        mNotificationPanel.setTouchDisabled(false);
        updateVisibleToUser();
        /*PRIZE-add for bugid:51190-liufan-2018-02-27-start*/
        mKeyguardBottomArea.showIndication();
        /*PRIZE-add for bugid:51190-liufan-2018-02-27-end*/
    }

    public void onScreenTurningOn() {
        mScreenTurningOn = true;
        mFalsingManager.onScreenTurningOn();
        mNotificationPanel.onScreenTurningOn();
        if (mLaunchCameraOnScreenTurningOn) {
            mNotificationPanel.launchCamera(false, mLastCameraLaunchSource);
            mLaunchCameraOnScreenTurningOn = false;
        }
    }

    private void vibrateForCameraGesture() {
        // Make sure to pass -1 for repeat so VibratorService doesn't stop us when going to sleep.
        mVibrator.vibrate(new long[]{0, 400}, -1 /* repeat */);
    }

    public void onScreenTurnedOn() {
        mScreenTurningOn = false;
        mDozeScrimController.onScreenTurnedOn();
    }

    /**
     * Handles long press for back button. This exits screen pinning.
     */
    private boolean handleLongPressBack() {
        try {
            IActivityManager activityManager = ActivityManagerNative.getDefault();
            if (activityManager.isInLockTaskMode()) {
                activityManager.stopSystemLockTaskMode();

                // When exiting refresh disabled flags.
                mNavigationBarView.setDisabledFlags(mDisabled1, true);
                return true;
            }
        } catch (RemoteException e) {
            Log.d(TAG, "Unable to reach activity manager", e);
        }
        return false;
    }

    public void updateRecentsVisibility(boolean visible) {
        // Update the recents visibility flag
        /*if (visible) {
            mSystemUiVisibility |= View.RECENT_APPS_VISIBLE;
        } else {
            mSystemUiVisibility &= ~View.RECENT_APPS_VISIBLE;
        }
        notifyUiVisibilityChanged(mSystemUiVisibility);*/
    }

    @Override
    public void showScreenPinningRequest(int taskId) {
        if (mKeyguardMonitor.isShowing()) {
            // Don't allow apps to trigger this from keyguard.
            return;
        }
        // Show screen pinning request, since this comes from an app, show 'no thanks', button.
        showScreenPinningRequest(taskId, true);
    }

    public void showScreenPinningRequest(int taskId, boolean allowCancel) {
        mScreenPinningRequest.showPrompt(taskId, allowCancel);
    }

    public boolean hasActiveNotifications() {
        return !mNotificationData.getActiveNotifications().isEmpty();
    }

    public void wakeUpIfDozing(long time, MotionEvent event) {
        if (mDozing && mDozeScrimController.isPulsing()) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            // pm.wakeUp(time, "com.android.systemui:NODOZE");
            mWakeUpComingFromTouch = true;
            mWakeUpTouchLocation = new PointF(event.getX(), event.getY());
            mNotificationPanel.setTouchDisabled(false);
            mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
            mFalsingManager.onScreenOnFromTouch();
        }
    }

    @Override
    public void appTransitionPending() {

        // Use own timings when Keyguard is going away, see keyguardGoingAway and
        // setKeyguardFadingAway
        if (!mKeyguardFadingAway) {
            mIconController.appTransitionPending();
        }
    }

    @Override
    public void appTransitionCancelled() {
        mIconController.appTransitionCancelled();
        EventBus.getDefault().send(new AppTransitionFinishedEvent());
    }

    @Override
    public void appTransitionStarting(long startTime, long duration) {

        // Use own timings when Keyguard is going away, see keyguardGoingAway and
        // setKeyguardFadingAway.
        if (!mKeyguardGoingAway) {
            mIconController.appTransitionStarting(startTime, duration);
        }
        if (mIconPolicy != null) {
            mIconPolicy.appTransitionStarting(startTime, duration);
        }
    }

    @Override
    public void appTransitionFinished() {
        EventBus.getDefault().send(new AppTransitionFinishedEvent());
    }

    @Override
    public void onCameraLaunchGestureDetected(int source) {
        /*prize-add by lihuangyuan,for PowerExtend 2017-12-08-start*/
        if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode())
	 {
                Log.i("lhy","PowerExtendMode onCameraLaunchGestureDetected");
                return ;
        }
	 /*prize-add by lihuangyuan,for PowerExtend 2017-12-08-end*/
        mLastCameraLaunchSource = source;
        if (mStartedGoingToSleep) {
            mLaunchCameraOnFinishedGoingToSleep = true;
            return;
        }
        if (!mNotificationPanel.canCameraGestureBeLaunched(
                mStatusBarKeyguardViewManager.isShowing() && mExpandedVisible)) {
            return;
        }
        if (!mDeviceInteractive) {
            PowerManager pm = mContext.getSystemService(PowerManager.class);
            pm.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:CAMERA_GESTURE");
            mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
        }
        vibrateForCameraGesture();
        if (!mStatusBarKeyguardViewManager.isShowing()) {
            startActivity(KeyguardBottomAreaView.INSECURE_CAMERA_INTENT,
                    true /* dismissShade */);
        } else {
            if (!mDeviceInteractive) {
                // Avoid flickering of the scrim when we instant launch the camera and the bouncer
                // comes on.
                mScrimController.dontAnimateBouncerChangesUntilNextFrame();
                mGestureWakeLock.acquire(LAUNCH_TRANSITION_TIMEOUT_MS + 1000L);
            }
            if (mScreenTurningOn || mStatusBarKeyguardViewManager.isScreenTurnedOn()) {
                mNotificationPanel.launchCamera(mDeviceInteractive /* animate */, source);
            } else {
                // We need to defer the camera launch until the screen comes on, since otherwise
                // we will dismiss us too early since we are waiting on an activity to be drawn and
                // incorrectly get notified because of the screen on event (which resumes and pauses
                // some activities)
                mLaunchCameraOnScreenTurningOn = true;
            }
        }
    }

    @Override
    public void showTvPictureInPictureMenu() {
        // no-op.
    }

    public void notifyFpAuthModeChanged() {
        updateDozing();
    }

    private void updateDozing() {
        Trace.beginSection("PhoneStatusBar#updateDozing");
        // When in wake-and-unlock while pulsing, keep dozing state until fully unlocked.
        mDozing = mDozingRequested && mState == StatusBarState.KEYGUARD
                || mFingerprintUnlockController.getMode()
                        == FingerprintUnlockController.MODE_WAKE_AND_UNLOCK_PULSING;
        updateDozingState();
        Trace.endSection();
    }

    private final class ShadeUpdates {
        private final ArraySet<String> mVisibleNotifications = new ArraySet<String>();
        private final ArraySet<String> mNewVisibleNotifications = new ArraySet<String>();

        public void check() {
            mNewVisibleNotifications.clear();
            ArrayList<Entry> activeNotifications = mNotificationData.getActiveNotifications();
            for (int i = 0; i < activeNotifications.size(); i++) {
                final Entry entry = activeNotifications.get(i);
                final boolean visible = entry.row != null
                        && entry.row.getVisibility() == View.VISIBLE;
                if (visible) {
                    mNewVisibleNotifications.add(entry.key + entry.notification.getPostTime());
                }
            }
            final boolean updates = !mVisibleNotifications.containsAll(mNewVisibleNotifications);
            mVisibleNotifications.clear();
            mVisibleNotifications.addAll(mNewVisibleNotifications);

            // We have new notifications
            if (updates && mDozeServiceHost != null) {
                mDozeServiceHost.fireNewNotifications();
            }
        }
    }

    private final class DozeServiceHost implements DozeHost {
        // Amount of time to allow to update the time shown on the screen before releasing
        // the wakelock.  This timeout is design to compensate for the fact that we don't
        // currently have a way to know when time display contents have actually been
        // refreshed once we've finished rendering a new frame.
        private static final long PROCESSING_TIME = 500;

        private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
        private final H mHandler = new H();

        // Keeps the last reported state by fireNotificationLight.
        private boolean mNotificationLightOn;

        @Override
        public String toString() {
            return "PSB.DozeServiceHost[mCallbacks=" + mCallbacks.size() + "]";
        }

        public void firePowerSaveChanged(boolean active) {
            for (Callback callback : mCallbacks) {
                callback.onPowerSaveChanged(active);
            }
        }

        public void fireBuzzBeepBlinked() {
            for (Callback callback : mCallbacks) {
                callback.onBuzzBeepBlinked();
            }
        }

        public void fireNotificationLight(boolean on) {
            mNotificationLightOn = on;
            for (Callback callback : mCallbacks) {
                callback.onNotificationLight(on);
            }
        }

        public void fireNewNotifications() {
            for (Callback callback : mCallbacks) {
                callback.onNewNotifications();
            }
        }

        @Override
        public void addCallback(@NonNull Callback callback) {
            mCallbacks.add(callback);
        }

        @Override
        public void removeCallback(@NonNull Callback callback) {
            mCallbacks.remove(callback);
        }

        @Override
        public void startDozing(@NonNull Runnable ready) {
            mHandler.obtainMessage(H.MSG_START_DOZING, ready).sendToTarget();
        }

        @Override
        public void pulseWhileDozing(@NonNull PulseCallback callback, int reason) {
            mHandler.obtainMessage(H.MSG_PULSE_WHILE_DOZING, reason, 0, callback).sendToTarget();
        }

        @Override
        public void stopDozing() {
            mHandler.obtainMessage(H.MSG_STOP_DOZING).sendToTarget();
        }

        @Override
        public boolean isPowerSaveActive() {
            return mBatteryController != null && mBatteryController.isPowerSave();
        }

        @Override
        public boolean isPulsingBlocked() {
            return mFingerprintUnlockController.getMode()
                    == FingerprintUnlockController.MODE_WAKE_AND_UNLOCK;
        }

        @Override
        public boolean isNotificationLightOn() {
            return mNotificationLightOn;
        }

        private void handleStartDozing(@NonNull Runnable ready) {
            if (!mDozingRequested) {
                mDozingRequested = true;
                DozeLog.traceDozing(mContext, mDozing);
                updateDozing();
            }
            ready.run();
        }

        private void handlePulseWhileDozing(@NonNull PulseCallback callback, int reason) {
            mDozeScrimController.pulse(new PulseCallback() {

                @Override
                public void onPulseStarted() {
                    callback.onPulseStarted();
                    mStackScroller.setPulsing(true);
                }

                @Override
                public void onPulseFinished() {
                    callback.onPulseFinished();
                    mStackScroller.setPulsing(false);
                }
            }, reason);
        }

        private void handleStopDozing() {
            if (mDozingRequested) {
                mDozingRequested = false;
                DozeLog.traceDozing(mContext, mDozing);
                updateDozing();
            }
        }

        private final class H extends Handler {
            private static final int MSG_START_DOZING = 1;
            private static final int MSG_PULSE_WHILE_DOZING = 2;
            private static final int MSG_STOP_DOZING = 3;

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_START_DOZING:
                        handleStartDozing((Runnable) msg.obj);
                        break;
                    case MSG_PULSE_WHILE_DOZING:
                        handlePulseWhileDozing((PulseCallback) msg.obj, msg.arg1);
                        break;
                    case MSG_STOP_DOZING:
                        handleStopDozing();
                        break;
                }
            }
        }
    }
 
    /// M: Support "Operator plugin - Customize Carrier Label for PLMN". @{
    private IStatusBarPlmnPlugin mStatusBarPlmnPlugin = null;
    private View mCustomizeCarrierLabel = null;

    private final boolean supportCustomizeCarrierLabel() {
        return mStatusBarPlmnPlugin != null && mStatusBarPlmnPlugin.supportCustomizeCarrierLabel()
                && mNetworkController != null && mNetworkController.hasMobileDataFeature();
    }

    private final void updateCustomizeCarrierLabelVisibility(boolean force) {
        if (DEBUG) {
            Log.d(TAG, "updateCustomizeCarrierLabelVisibility(), force = " + force
                    + ", mState = " + mState);
        }

        final boolean makeVisible = mStackScroller.getVisibility() == View.VISIBLE
                && mState != StatusBarState.KEYGUARD;

        mStatusBarPlmnPlugin.updateCarrierLabelVisibility(force, makeVisible);
    }

    /*PRIZE-show the percent of the battery-liyao-2015-7-3-start*/
    ShowBatteryPercentageObserver mShowBatteryPercentageObserver;
    private class ShowBatteryPercentageObserver extends ContentObserver {

        private final Uri SHOW_BATTERY_PERCENTAGE_URI =
                Settings.System.getUriFor("battery_percentage_enabled");

        public ShowBatteryPercentageObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (DEBUG) {
                Log.d(TAG, "ShowBatteryPercentageObserver onChange selfChange "+selfChange);
            }
            if (selfChange) return;
            boolean showLevel =  Settings.System.getInt(mContext.getContentResolver(),
                    "battery_percentage_enabled", 0) == 1;
            if (DEBUG) {
                Log.d(TAG, "ShowBatteryPercentageObserver onChange showLevel "+showLevel);
            }
            mStatusBarView.findViewById(R.id.battery_percentage).setVisibility(showLevel ? View.VISIBLE : View.GONE);
            mStatusBarView.findViewById(R.id.battery_level).setVisibility(showLevel ? View.VISIBLE : View.GONE);
            
            
        }

        public void startObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                    SHOW_BATTERY_PERCENTAGE_URI,
                    false, this,mCurrentUserId);

        }

        public void stopObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
        }
    }
    /*PRIZE-show the percent of the battery-liyao-2015-7-3-end*/
	/*prize-public-standard:Changed lock screen-liuweiquan-20151212-start*/
	private static final String KGWALLPAPER_SETTING_ON_ACTION = "system.settings.changedwallpaper.on";
	private static final String KGWALLPAPER_SETTING_OFF_ACTION = "system.settings.changedwallpaper.off";
	private static final String ACTION_CLOSE_SUPERPOWER_NOTIFICATION = "android.intent.action.ACTION_CLOSE_SUPERPOWER_NOTIFICATION";
	private static final String ACTION_KILL_SUPERPOWER = "android.intent.action.ACTION_KILL_SUPERPOWER";
	private static final String ACTION_EXIT_POWERSAVING = "android.intent.action.ACTION_EXIT_POWERSAVING";
	
	private static boolean bChangedWallpaperIsOpen;
	private static String sChangedWallpaperPath;
	private static boolean bIntoSuperSavingPower = false;

	BaiBianWallpaperObserver mBaiBianWallpaperObserver;
	BroadcastReceiver mChangedWallpaperReceiver =new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//boolean bChangedWallpaper = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_KGWALLPAPER_SWITCH,0) == 1;
			if(!bIntoSuperSavingPower){
				if(bChangedWallpaperIsOpen&&action.equals(Intent.ACTION_SCREEN_OFF)){	
					changeKeyguardWallpaper();					
				}
				if(bChangedWallpaperIsOpen&&action.equals(Intent.ACTION_SCREEN_ON)){	
				}		
			}
            /*PRIZE-add for haokan-liufan-2017-10-31-start*/
            if(isUseHaoKan()){
                Settings.System.putInt(mContext.getContentResolver(), Settings.System.PRIZE_KGWALLPAPER_SWITCH, 0);
            }
            /*PRIZE-add for haokan-liufan-2017-10-31-end*/
			if(action.equals(KGWALLPAPER_SETTING_ON_ACTION)){	
				bChangedWallpaperIsOpen = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_KGWALLPAPER_SWITCH,0) == 1;
            }
			if(action.equals(KGWALLPAPER_SETTING_OFF_ACTION)){
				bChangedWallpaperIsOpen = Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_KGWALLPAPER_SWITCH,0) == 1;
				isPrizeChange = true;
            }
			if(PrizeOption.PRIZE_POWER_EXTEND_MODE&&bChangedWallpaperIsOpen){
				if(action.equals(ACTION_CLOSE_SUPERPOWER_NOTIFICATION)){
					bIntoSuperSavingPower = true;
					isPrizeChange = true;
					updateMediaMetaData(true,false);
				}
				if(action.equals(ACTION_KILL_SUPERPOWER)){
					bIntoSuperSavingPower = false;
					isPrizeChange = true;
					updateMediaMetaData(true,false);
				}
				if(action.equals(ACTION_EXIT_POWERSAVING)){
					bIntoSuperSavingPower = false;
					isPrizeChange = true;
					updateMediaMetaData(true,false);
				}
			}				
            
            mKeyguardBottomArea.refreshView();
		}	
	};
	private void changeKeyguardWallpaper(){
		String currentWallPaper=sChangedWallpaperPath;
		if(currentWallPaper!=null){
			String keyguardPath=new String();
			int nextWallPaper=Integer.parseInt(currentWallPaper.substring(currentWallPaper.length()-6, currentWallPaper.length()-4))+1;
			if(nextWallPaper==20){
				nextWallPaper=0;
			}
			if(PrizeOption.PRIZE_CUSTOMER_NAME.equals("coosea")){
				keyguardPath="/system/keyguard-wallpapers/keyguardwallpaper"+String.format("%02d",nextWallPaper)+".png";
			}else{
				keyguardPath="/system/keyguard-wallpapers/keyguardwallpaper"+String.format("%02d",nextWallPaper)+".jpg";
			}
			/*
			if(nextWallPaper<10&&nextWallPaper>=0){
				keyguardPath="/system/keyguard-wallpapers/keyguardwallpaper0"+nextWallPaper+".jpg";
			}else if(nextWallPaper>=10&&nextWallPaper<20){
				keyguardPath="/system/keyguard-wallpapers/keyguardwallpaper"+nextWallPaper+".jpg";
			}else if(nextWallPaper==20){
				keyguardPath="/system/keyguard-wallpapers/keyguardwallpaper"+"00"+".jpg";
			}
			*/
			//Settings.System.putString(mContext.getContentResolver(),Settings.System.PRIZE_KGWALLPAPER_PATH,keyguardPath);		
			//sChangedWallpaperPath = keyguardPath;
			Settings.Global.putString(mContext.getContentResolver(),Settings.Global.PRIZE_KGWALLPAPER_PATH,keyguardPath);			
		}
	}
	private class BaiBianWallpaperObserver extends ContentObserver {
        public BaiBianWallpaperObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.v(TAG, "BaiBianWallpaperObserver onChange" );
            if (selfChange) return;  
			sChangedWallpaperPath = Settings.Global.getString(mContext.getContentResolver(),Settings.Global.PRIZE_KGWALLPAPER_PATH);
			if(sChangedWallpaperPath!=null && !TextUtils.isEmpty(sChangedWallpaperPath) && !"-1".equals(sChangedWallpaperPath)){
				isPrizeChange = true;
			}	  
            updateMediaMetaData(true,false);
        }
        public void startObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
			cr.registerContentObserver(
                    Settings.System.getUriFor(Settings.Global.PRIZE_KGWALLPAPER_PATH),
                    false, this, UserHandle.USER_ALL);       
        }
        public void stopObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
        } 
    }
	/*prize-public-standard:Changed lock screen-liuweiquan-20151212-end*/
	/*prize-public-standard:Changed lock screen-liuweiquan-20160407-start*/
	private DisplayMetrics getDisplayMetrics(){
		try{
			if(mWindowManagerService.hasNavigationBar()){
				WindowManager mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
				Display mDisplay = mWindowManager.getDefaultDisplay();
				DisplayMetrics displayMetrics = new DisplayMetrics();
				mDisplay.getRealMetrics(displayMetrics);
				return displayMetrics;
			}
		} catch (RemoteException ex) {
            // no window manager? good luck with that
        }
		return mDisplayMetrics;
	}
	/*prize-public-standard:Changed lock screen-liuweiquan-20160407-end*/
	
    protected void updateCarrierLabelVisibility(boolean force) {
        if (supportCustomizeCarrierLabel()) {
            if (mState == StatusBarState.KEYGUARD ||
                    mNotificationPanel.isPanelVisibleBecauseOfHeadsUp()) {
                if (mCustomizeCarrierLabel != null) {
                    mCustomizeCarrierLabel.setVisibility(View.GONE);
                }
            } else {
                updateCustomizeCarrierLabelVisibility(force);
                return;
            }
        }
    }
    /// M: Support "Operator plugin - Customize Carrier Label for PLMN". @}
    /*PRIZE lockscreen background liyao-2015-07-22-start*/
    private Bitmap keyGuardBg = null; // added by fanjunchen for recycle.
    private boolean isPrizeChange = true;// added by fanjunchen
    /*@prize fanjunchen start { not use prizeLockscreen so it to be true 2015-11-05 modified*/
    private final static boolean SUPPORT_KEYGUARD_WALLPAPER = true;
    /*@prize fanjunchen end }*/
    private static final String KEYGUARD_WALLPAPER_URI = "keyguard_wallpaper";
    private Bitmap convertToBitmap(String path) {
        long time1 = System.currentTimeMillis();
        if (!isPrizeChange && keyGuardBg != null && !keyGuardBg.isRecycled())
            return keyGuardBg;
        recycleLockscreenWallpaper();
        isPrizeChange = false;
		/*prize-public-standard:Changed lock screen-liuweiquan-20160407-start*/
		DisplayMetrics mDisplayMetrics = getDisplayMetrics();
		/*prize-public-standard:Changed lock screen-liuweiquan-20160407-end*/
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // get the picture size when inJustDecodeBounds is true
        opts.inJustDecodeBounds = true;
        //opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // BitmapFactory.decodeFile return null
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth;
        int height = opts.outHeight;
        int newWidth = mDisplayMetrics.widthPixels; 
        int newHeight = mDisplayMetrics.heightPixels;
		/*prize-public-bug:Changed lock screen-liuweiquan-20160309-start*/
		if(mContext.getResources().getConfiguration().orientation==2 && newWidth > newHeight){
			newHeight = mDisplayMetrics.widthPixels; 
			newWidth = mDisplayMetrics.heightPixels;
		}
		/*prize-public-bug:Changed lock screen-liuweiquan-20160309-end*/
        float scaleWidth = 1.f, scaleHeight = 1.f;
        if (width > newWidth || height > newHeight) {
            // scale
            scaleWidth = ((float) width) / newWidth;
            scaleHeight = ((float) height) / newHeight;
            
            if (scaleWidth < 1)
                scaleWidth = 1;
            if (scaleHeight < 1)
                scaleHeight = 1;
        }
        float scale = Math.min(scaleWidth, scaleHeight);
        opts.inJustDecodeBounds = false;
        //opts.inSampleSize = 1;//(int)scale;
        opts.inSampleSize = (int)scale;

        Bitmap bitmap = null;
        try{
            bitmap = BitmapFactory.decodeFile(path, opts);
            width = bitmap.getWidth();
            height = bitmap.getHeight();
            newWidth = mDisplayMetrics.widthPixels;
            newHeight = mDisplayMetrics.heightPixels;			
			/*prize-public-bug:Changed lock screen-liuweiquan-20160309-start*/
			if(mContext.getResources().getConfiguration().orientation==2 && newWidth > newHeight){
				newHeight = mDisplayMetrics.widthPixels; 
				newWidth = mDisplayMetrics.heightPixels;
			}	
			/*prize-public-bug:Changed lock screen-liuweiquan-20160309-end*/
            scale = 0;
            Bitmap bmp = Bitmap.createBitmap(newWidth,newHeight,Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            Rect src = new Rect();
            if (newWidth * height > width * newHeight) {
                scale = newWidth / (float)width;
                src.left = 0;
                src.right = width;
                src.top = (int)((height - newHeight / scale) / 2);
                src.bottom = (int)((height + newHeight / scale) / 2);
            }else{
                scale = newHeight / (float)height;
                src.left = (int)((width - newWidth / scale) / 2);
                src.right = (int)((width + newWidth / scale) / 2);
                src.top = 0;
                src.bottom = height;
            }
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);
            canvas.drawBitmap(bitmap, src, new Rect(0,0,newWidth,newHeight), paint);
            bitmap.recycle();
            bitmap = bmp;
            keyGuardBg = bitmap;
            NotificationPanelView.debugMagazine("keyGuardBg : " + keyGuardBg);
        }catch(Exception e){
        }
        long time2 = System.currentTimeMillis();
        Log.d(TAG,"time time--------->"+(time2-time1));
        //Add for backgroud_reflect-luyingying-2017-09-09-Start*/
        //setTextColor();
        //Add end
        return bitmap;
    }

    /*PRIZE-add,recycle bitmap-liufan-2016-06-03-start*/
    public void recycleLockscreenWallpaper(){
        NotificationPanelView.debugMagazine("recycleLockscreenWallpaper keyGuardBg : " + keyGuardBg);
        if(keyGuardBg != null) NotificationPanelView.debugMagazine("keyGuardBg.isRecycled() : " + keyGuardBg.isRecycled());
        if(keyGuardBg != null && !keyGuardBg.isRecycled()){
            mBackdropBack.setImageBitmap(null);
            keyGuardBg.recycle();
            keyGuardBg = null;
        }
    }

    public void recycleBlurWallpaper(){
        Bitmap bitmap = null;
        Drawable d = mBlurBack.getBackground();
        if(d != null && d instanceof BitmapDrawable){
            BitmapDrawable bd = (BitmapDrawable)d;
            bitmap = bd.getBitmap();
            if(bitmap != null){
                mBlurBack.setBackground(null);
                bitmap.recycle();
                bitmap = null;
            }
        }
    }
    /*PRIZE-add,recycle bitmap-liufan-2016-06-03-end*/
	
	public void closeLockScreenWallpaperSwitch(){
        //prize-disable KGWallpager auto-change function-pengcancan-00160906-start
        NotificationPanelView.debugMagazine("closeLockScreenWallpaperSwitch isUseHaoKan()- " + isUseHaoKan());
        NotificationPanelView.debugMagazine("closeLockScreenWallpaperSwitch isOpenMagazine()- " + isOpenMagazine());
        if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW && isUseHaoKan()){
            NotificationPanelView.debugMagazine("closeLockScreenWallpaperSwitch abc");
            Settings.System.putInt(mContext.getContentResolver(),Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH, 0);
            bChangedWallpaperIsOpen = false;
            IS_USE_HAOKAN = false;
            NotificationPanelView.IS_ShowHaoKanView = true;
            NotificationPanelView.IS_ShowNotification_WhenShowHaoKan = true;
            refreshHaoKanState();
        }
        if(PrizeOption.PRIZE_CHANGED_WALLPAPER){
            Settings.System.putInt(mContext.getContentResolver(),Settings.System.PRIZE_KGWALLPAPER_SWITCH, 0);
            bChangedWallpaperIsOpen = false;
        }
        //prize-disable KGWallpager auto-change function-pengcancan-00160906-end
	}

    /*PRIZE-when unlock keguard appear splash screen bugId17935-dengyu-2016-7-6-start*/
    public ScrimController getScrimController() {
            return mScrimController;
    }
    /*PRIZE-when unlock keguard appear splash screen bugId17935-dengyu-2016-7-6-end*/
    
    
       
/* prize-add-split screen-liyongli-20170612-start */   
       public final  void onBusEvent(final PrizeOpenPendingIntentEvent event) {
       	if( PrizeOption.PRIZE_SYSTEMUI_QQPOP_ICON ){
       	super.openQQPendingIntent(event);
       	}
    }
/* prize-add-split screen-liyongli-20170612-end */

    /*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-Start*/
    public static int textColor = -1;
    public static final int LOCK_LIGHT_COLOR = 0xFFFFFFFF;
    public static final int LOCK_DARK_COLOR = 0xFF505050;
    private void setTextColor(){
        Log.d("BooLor","PrizeOption.PRIZE_LOCKSCREEN_INVERSE = " + PrizeOption.PRIZE_LOCKSCREEN_INVERSE);
        if(!PrizeOption.PRIZE_LOCKSCREEN_INVERSE){
            Log.d("BooLor","PRIZE_LOCKSCREEN_INVERSE is true");
            return ;
        }
        if(mState != StatusBarState.KEYGUARD){
            Log.d("BooLor","bindTextColor mState is not in StatusBarState.KEYGUARD " + mState);
            return ;
        }
        int N = showNotificationOnKeyguardNums();//mNotificationData.getActiveNotifications().size();
        boolean isShowBlur = isShowLimitByNotifications(N);
        int tmpColor = 1;
        //isShowBlur = true;
        if(!isShowBlur){
            tmpColor = 1;
        }else{
            boolean isMagazineInverse = PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW && isOpenMagazine();
            Log.d("BooLor","magazineInverse = "+ magazineInverse +", isMagazineInverse = " + isMagazineInverse);
            if(isMagazineInverse){
                tmpColor = magazineInverse;
            }else{
                Bitmap bm = getCurLockScreenWallpaper();
                //Bitmap bm = isUseHaoKan() && mNotificationPanel.isHaoKanViewShow()
                //		? mNotificationPanel.getCurrentImage(): keyGuardBg;
                //if(bm != null){
                tmpColor = bm != null ? BgColor.bindTextColor(bm): 1;//1 means white, 0 means black
            }
        }
        if(isUseHaoKan()){
            mNotificationPanel.refreshMagazineDescriptionColor(tmpColor);
        }
        Log.d("BooLor","tmpColor = " + tmpColor);
        if(tmpColor != textColor){
            textColor = tmpColor;
            Log.d("BooLor","here1");
            if (textColor == 0){ 
                mEmergencyButton.setTextColor(LOCK_DARK_COLOR);
            } else {
                mEmergencyButton.setTextColor(LOCK_LIGHT_COLOR);
            }
            mKeyguardStatusView.setTextColor(textColor);
            mKeyguardBottomArea.setTextColor(textColor);
            mKeyguardStatusBar.setTextColor(textColor);
            refreshKeyguardStatusBar(textColor);
            mKeyguardIndicationController.setTextColor(textColor);
            KeyguardAffordanceView leftView = mKeyguardBottomArea.getLeftView();
            KeyguardAffordanceView rightView = mKeyguardBottomArea.getRightView();
            KeyguardAffordanceView iconView = mKeyguardBottomArea.getLockIcon();
            leftView.setTextColor(textColor, true);
            rightView.setTextColor(textColor, true);
            iconView.setTextColor(textColor, true);
            if(mIconController != null){
                Log.d("BYY", "mIconController is ok!!!");
                mIconController.setTextColor(textColor, true);
            }
        }
    }

    private BatteryMeterViewDefinedNew keyguardStatusBarBattery;
    private SignalClusterView keyguardSignalClusterView;
    /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
    private LiuHaiSignalClusterView mLiuHaiKeyguardSignalClusterView;
    private LiuHaiBatteryMeterViewDefinedNew mLiuHaiKeyguardBatteryView;
    /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
    public void refreshKeyguardStatusBar(int textColor){
        if(mKeyguardStatusBar == null){
            return ;
        }
        if(keyguardStatusBarBattery == null){
            keyguardStatusBarBattery = (BatteryMeterViewDefinedNew)mKeyguardStatusBar.findViewById(R.id.battery_new);
        }
        if(keyguardSignalClusterView == null){
            keyguardSignalClusterView = (SignalClusterView)mKeyguardStatusBar.findViewById(R.id.signal_cluster);
            keyguardSignalClusterView.setInverseFlag(true);
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
        if(OPEN_LIUHAI_SCREEN){
            if(mLiuHaiKeyguardSignalClusterView == null){
                mLiuHaiKeyguardSignalClusterView = (LiuHaiSignalClusterView)mKeyguardStatusBar.findViewById(R.id.prize_liuhai_signal_cluster);
                mLiuHaiKeyguardSignalClusterView.setInverseFlag(true);
            }

            if(mLiuHaiKeyguardBatteryView == null){
                mLiuHaiKeyguardBatteryView = (LiuHaiBatteryMeterViewDefinedNew)mKeyguardStatusBar.findViewById(R.id.liuhai_battery_new);
            }
        }
        /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
        if (textColor == 0){
            keyguardStatusBarBattery.onStatusBarStyleChanged(StatusBarManager.STATUS_BAR_INVERSE_GRAY);

            keyguardSignalClusterView.setIgnoreStatusBarStyleChanged(false);
            keyguardSignalClusterView.setStatusBarStyle(StatusBarManager.STATUS_BAR_INVERSE_GRAY);
            /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
            if(OPEN_LIUHAI_SCREEN){
                mLiuHaiKeyguardBatteryView.onStatusBarStyleChanged(StatusBarManager.STATUS_BAR_INVERSE_GRAY);

                mLiuHaiKeyguardSignalClusterView.setIgnoreStatusBarStyleChanged(false);
                mLiuHaiKeyguardSignalClusterView.setStatusBarStyle(StatusBarManager.STATUS_BAR_INVERSE_GRAY);
            }
            /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
            mNetworkController.notifyAllListenersForInverse();
        } else {
            keyguardStatusBarBattery.onStatusBarStyleChanged(StatusBarManager.STATUS_BAR_INVERSE_WHITE);

            keyguardSignalClusterView.setIgnoreStatusBarStyleChanged(true);
            keyguardSignalClusterView.setStatusBarStyle(StatusBarManager.STATUS_BAR_INVERSE_WHITE);
            /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
            if(OPEN_LIUHAI_SCREEN){
                mLiuHaiKeyguardBatteryView.onStatusBarStyleChanged(StatusBarManager.STATUS_BAR_INVERSE_WHITE);

                mLiuHaiKeyguardSignalClusterView.setIgnoreStatusBarStyleChanged(true);
                mLiuHaiKeyguardSignalClusterView.setStatusBarStyle(StatusBarManager.STATUS_BAR_INVERSE_WHITE);
            }
            /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/
            mNetworkController.notifyAllListenersForInverse();
        }
    }

    private Bitmap getCurLockScreenWallpaper(){
        Bitmap bitmap = null;
        Drawable d = mBackdropBack.getDrawable();
        if(d instanceof BitmapDrawable){
            BitmapDrawable bd = (BitmapDrawable)d;
            bitmap = bd.getBitmap();
        }
        if(d instanceof LockscreenWallpaper.WallpaperDrawable){
            LockscreenWallpaper.WallpaperDrawable wd = (LockscreenWallpaper.WallpaperDrawable)d;
            if(wd != null) bitmap = wd.getConstantState().getBackground();
        }
        return bitmap;
    }
    /*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-end*/

    /** prize add by xiarui 2018-01-25 start **/

    private MyFileObserver mCloudListFileObserver;

    private static final String SCREEN_OFF_KILLER_CLOUD_PATH = "/data/system/cloudlist/";
    private static final String SCREEN_OFF_KILLER_CLOUD_FILE = "screenofkill.xml";

    private static final String PKG_NAME_SC = "com.prize.smartcleaner";
    private static final String CLS_NAME_AC = "com.prize.smartcleaner.PrizeAutoClearService";
    private static final String CLS_NAME_CS = "com.prize.smartcleaner.PrizeClearSystemService";

    private static final String ACTION_ACSTART = "com.prize.android.service.ACSTART";
    private static final String ACTION_DELAY_ACSTART = "com.prize.android.service.DELAY_ACSTART";
    private static final String ACTION_UPDATE_FILTER_LIST = "com.prize.android.service.UPDATE_FILTER_LIST";

    private static final String PROP_LOOP_KILL_COUNT = "debug.loop.kill.count";

    private void registerAutoClearReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
        intentFilter.addAction(PowerManager.ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);//set the priority high
        mContext.registerReceiver(mAutoClearBroadcastReceiver, intentFilter);
    }

    private void unregisterAutoClearReceiver() {
        mContext.unregisterReceiver(mAutoClearBroadcastReceiver);
    }

    private BroadcastReceiver mAutoClearBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive action:" + action);
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Log.d(TAG, "SCREEN OFF BROADCAST RECEIVER!!");
                setAutoClearAlarm(context);
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                cancelAutoClearAlarm(context);
            } else if (PowerManager.ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED.equals(action)
                    || PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED.equals(action)) {
                updateIdleMode(context, mPowerManager != null
                        ? (mPowerManager.isDeviceIdleMode()
                        || mPowerManager.isLightDeviceIdleMode())
                        : false);
            }
        }
    };
    //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar start
    private static final String ACTION_BACK = "com.android.systemui.action.SEND_BACK";
    private static final String ACTION_HOME = "com.android.systemui.action.SEND_HOME";
    private static final String ACTION_RECENT_TASKS = "com.android.systemui.action.SEND_RECENT_TASKS";
    private final static int NON_BOTTOM = 0;
    private final static int CENTER_BOTTOM = 1;
    private final static int LEFT_BOTTOM = 2;
    private final static int RIGHT_BOTTOM = 3;
    private static final String EXTRA_LOCATION = "location";
    private static final int STYLE_WHITE = 0;
    private static final int STYLE_GRAY = 1;
    private void registerSwipeUpGestureReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_BACK);
        intentFilter.addAction(ACTION_HOME);
        intentFilter.addAction(ACTION_RECENT_TASKS);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);//set the priority high
        mContext.registerReceiver(mSwipeUpGestureReceiver, intentFilter);
    }

    private void unregisterSwipeUpGestureReceiver() {
        mContext.unregisterReceiver(mSwipeUpGestureReceiver);
    }

    private BroadcastReceiver mSwipeUpGestureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Log.d("tzr", "BROADCAST RECEIVER!  " + "isKeyguardHostWindow = " + isKeyguardHostWindow());
        	if(isKeyguardHostWindow())
    			return;
            String action = intent.getAction();
            if (ACTION_BACK.equals(action)) {
                Log.d("tzr", "BACK BROADCAST RECEIVER!  ");
                int location = intent.getIntExtra(EXTRA_LOCATION, NON_BOTTOM);
                mGestureIndicatorManager.onGestureComplete(location);
                sendKeyEvent(KeyEvent.KEYCODE_BACK);
            } else if (ACTION_HOME.equals(action)) {
                Log.d("tzr", "HOME BROADCAST RECEIVER! ");
            	if(mStatusBarKeyguardViewManager != null && mStatusBarKeyguardViewManager.isBouncerShowing())
            		return;
            	mGestureIndicatorManager.onGestureComplete(CENTER_BOTTOM);
            	sendKeyEvent(KeyEvent.KEYCODE_HOME);
            } else if (ACTION_RECENT_TASKS.equals(action)) {
            	if(mKeyguardManager.inKeyguardRestrictedInputMode()){
            		return;
            	}
                Log.d("tzr", "RECENT TASKS BROADCAST RECEIVER!!");
            	mGestureIndicatorManager.onGestureComplete(CENTER_BOTTOM);
				preloadRecents();
            	toggleRecentApps();
            }
        }
    };

    public void sendKeyEvent(int keyCode){
        long now = System.currentTimeMillis();
        injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, 0,
            KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
        injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, 0,
            KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
    }

    public void injectKeyEvent(KeyEvent event){
        InputManager.getInstance().injectInputEvent(event,
                InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }
    //prize tangzhengrong 20180417 Swipe-up Gesture Nagation bar end

    /**
     * if not in idle mode, do somethings
     * @param enabled
     */
    private void updateIdleMode(Context context, boolean enabled) {
        Log.d(TAG, "updateIdleMode, enabled: " + enabled);
        if (!enabled) {
            Intent intent = new Intent(ACTION_DELAY_ACSTART);
            intent.setComponent(new ComponentName(PKG_NAME_SC, CLS_NAME_AC));
            if (intent != null) {
                Log.d(TAG, "start PrizeAutoClearService, reason: exit idle mode");
                context.startServiceAsUser(intent, UserHandle.CURRENT);
            }
        }
    }

    /**
     * set screen off clear alarm
     * @param context
     */
    private void setAutoClearAlarm(Context context) {
        setClearAlarm(context, PKG_NAME_SC, ACTION_ACSTART, 3);
        setClearAlarm(context, PKG_NAME_SC, ACTION_DELAY_ACSTART, 15);
    }

    /**
     * cancel screen off clear alarm
     * @param context
     */
    private void cancelAutoClearAlarm(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            SystemProperties.set(PROP_LOOP_KILL_COUNT, "1");
        }
        cancelClearAlarm(context, PKG_NAME_SC, ACTION_ACSTART);
        cancelClearAlarm(context, PKG_NAME_SC, ACTION_DELAY_ACSTART);
    }

    /**
     * set clear alarm
     * @param context
     * @param pkgName
     * @param action
     * @param delayMin
     */
    private void setClearAlarm(Context context, String pkgName, String action, int delayMin) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setPackage(pkgName);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmMgr.cancel(pendingIntent);
        //int min = 3;//new Random().nextInt(5) + 10; //[10, 15)
        long totalMillis = (long) (delayMin * 60 * 1000);
        Log.d(TAG, "setClearAlarm [" + pkgName + " , " + action + " , " + delayMin + "]");
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + totalMillis, pendingIntent);
    }

    /**
     * cancel clear alarm
     * @param context
     * @param pkgName
     * @param action
     */
    private void cancelClearAlarm(Context context, String pkgName, String action) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setPackage(pkgName);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmMgr.cancel(pendingIntent);
        Log.d(TAG, "cancelClearAlarm [" + pkgName + " , " + action + "]");
    }

    private void fileChanged(String path) {
        if (DEBUG) {
            Log.d(TAG, "fileChanged() path=" + path);
        }

        if (path != null) {
            if (SCREEN_OFF_KILLER_CLOUD_FILE.equals(path)) {
                Intent intent = new Intent(ACTION_UPDATE_FILTER_LIST);
                intent.setComponent(new ComponentName(PKG_NAME_SC, CLS_NAME_CS));
                if (intent != null) {
                    if (DEBUG) {
                        Log.d(TAG, "start PrizeClearSystemService to update filter list");
                    }
                    mContext.startServiceAsUser(intent, UserHandle.CURRENT);
                }
            }
        }
    }

    private void startWatchingCloudListFile() {
        mCloudListFileObserver = new MyFileObserver(SCREEN_OFF_KILLER_CLOUD_PATH, FileObserver.CLOSE_WRITE);
        if (mCloudListFileObserver != null) {
            mCloudListFileObserver.startWatching();
        }
    }

    private void stopWatchingCloudListFile() {
        if (mCloudListFileObserver != null) {
            mCloudListFileObserver.stopWatching();
        }
    }

    private class MyFileObserver extends FileObserver {
        public MyFileObserver(String path, int mask) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            fileChanged(path);
        }
    }

    /** prize add by xiarui 2018-01-25 end **/

	/*prize-xuchunming-20180413:KeyguradBottomAreaView set faceid info at press powerkey-start*/
	public void nofityPowerKeyPower(boolean interactive) {
		if(mNotificationPanel != null){
			mNotificationPanel.nofityPowerKeyPower(interactive);
		}
    }
	/*prize-xuchunming-20180413:KeyguradBottomAreaView set faceid info at press powerkey-end*/

    /*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
    public static final boolean DEBUG_LIUHAI = true;
    public static final String DEBUG_LIUHAI_TAG = "liuhai_screen";
    public static void debugLiuHai(String msg){
        if(DEBUG_LIUHAI){
            android.util.Log.d(DEBUG_LIUHAI_TAG, msg);
        }
    }

    public static final boolean OPEN_LIUHAI_SCREEN = PrizeOption.PRIZE_NOTCH_SCREEN;
    //prize tangzhengrong 20180515 Swipe-up Gesture Navigation bar start
    public static final boolean OPEN_GESTURE_NAVIGATION = PrizeOption.PRIZE_SWIPE_UP_GESTURE_NAVIGATION;
    //prize tangzhengrong 20180515 Swipe-up Gesture Navigation bar end
    /*
    * 0--StatusBarIconController;
    * 1--KeyguardStatusBarIconController;
    */
    private ArrayList<LiuHaiStatusBarIconController> mLiuHaiIconControllerList = new ArrayList<LiuHaiStatusBarIconController>();
    public ArrayList<LiuHaiStatusBarIconController> getLiuHaiIconControllerList(){
        return mLiuHaiIconControllerList;
    }

    public void showNetworkSpeedOnLiuHaiRight(int visibility, String text){
        for(LiuHaiStatusBarIconController controller : mLiuHaiIconControllerList){
            LiuHaiStatusBarIconController.ShowOnRightListener listener = 
                    controller.getShowOnRightListener();
            if(listener == null){
                debugLiuHai("showNetworkSpeedOnLiuHaiRight listener is null, return");
                return;
            }
            if(TextUtils.isEmpty(text)){
                visibility = View.GONE;
            }
            listener.refreshNetworkSpeed(visibility, text);
        }
    }
    /*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

    /*PRIZE-add for liuhai screen-liufan-2018-06-25-start*/
    public static final boolean DEBUG_LIUHAI2 = true;
    public static final String DEBUG_LIUHAI_TAG2 = "liuhai_screen";
    public static void debugLiuHai2(String msg){
        if(DEBUG_LIUHAI2){
            android.util.Log.d(DEBUG_LIUHAI_TAG2, msg);
        }
    }

    /*
    * Compatible with all liuhai screen
    */
    public static final boolean OPEN_LIUHAI_SCREEN2 = PrizeOption.PRIZE_NOTCH_SCREEN2;
    /*
    * 0--StatusBarIconController;
    * 1--KeyguardStatusBarIconController;
    * 2--PulledStatusBarIconController;
    */
    private ArrayList<LiuHaiStatusBarIconController2> mLiuHaiIconControllerList2 = new ArrayList<LiuHaiStatusBarIconController2>();
    public ArrayList<LiuHaiStatusBarIconController2> getLiuHaiIconControllerList2(){
        return mLiuHaiIconControllerList2;
    }

    public void showNetworkSpeedOnLiuHai2(int visibility, String text){
        for(LiuHaiStatusBarIconController2 controller : mLiuHaiIconControllerList2){
            LiuHaiStatusBarIconController2.ShowOnLeftListenerImpI listener =
                    controller.getShowOnLeftListenerImpI();
            if(listener == null){
                debugLiuHai("showNetworkSpeedOnLiuHai2 listener is null, return");
                return;
            }
            if(TextUtils.isEmpty(text)){
                visibility = View.GONE;
            }
            listener.refreshNetworkSpeed(visibility, text);
        }
    }

    private final String LIUHAI_STATUSBAR_DEBUG = "debug.liuhai.statusbar";
    private final String LIUHAI_KEYGUARD_DEBUG = "debug.liuhai.keyguard";
    private final String LIUHAI_HEADER_DEBUG = "debug.liuhai.header";
    private BroadcastReceiver mDebugLiuHaiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            boolean isOpen = false;
            String value = null;
            if (bundle != null) {
                value = bundle.getString("open", "").trim().toLowerCase();
                isOpen = "yes".equals(value);
            }
            int index = -1;
            if (LIUHAI_STATUSBAR_DEBUG.equals(action)) {
                index = 0;
            } else if(LIUHAI_KEYGUARD_DEBUG.equals(action)){
                index = 1;
            } else if(LIUHAI_HEADER_DEBUG.equals(action)){
                index = 2;
            }
            debugLiuHai("receiver action = " + action + ", index = " + index + ", value = " + value + ", isOpen = " + isOpen);
            if(index >= 0 && index < mLiuHaiIconControllerList2.size()){
                LiuHaiStatusBarIconController2 controller = mLiuHaiIconControllerList2.get(index);
                if(controller != null) controller.setDebugSwitch(isOpen, index);
            }
        }
    };

    private void registerBroadcastForLiuHai(Context context){
        if(context == null) {
            return ;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(LIUHAI_STATUSBAR_DEBUG);
        filter.addAction(LIUHAI_KEYGUARD_DEBUG);
        filter.addAction(LIUHAI_HEADER_DEBUG);
        context.registerReceiverAsUser(mDebugLiuHaiReceiver, UserHandle.ALL, filter, null, null);
    }

    private void unregisterBroadcastForLiuHai(Context context){
        if(context != null) context.unregisterReceiver(mDebugLiuHaiReceiver);
    }
    /*PRIZE-add for liuhai screen-liufan-2018-06-25-end*/
    
  /*PRIZE-three_finger_moveup_open split screen-liyongli-2018-04-23-start*/
    private BroadcastReceiver m3FingerMoveUpReceiver = new BroadcastReceiver() {
        @Override 
        public void onReceive(Context context, Intent intent) {
            //if (false) {//on off
            //    return;
            //}
            int dockSide = WindowManagerProxy.getInstance().getDockSide();
            if (dockSide != WindowManager.DOCKED_INVALID) {//only  DOCKED_INVALID can open split screen
                return;
            }
            
            mHandler.postDelayed(mOpenSplitScreenRunnable, 30);
        }
    };
    private final OpenSplitScreenRunnable mOpenSplitScreenRunnable = new OpenSplitScreenRunnable();
    private class OpenSplitScreenRunnable implements Runnable {
        @Override
        public void run() {
            longClickOpenSplitScreen(null);
        }
    }    
  /*PRIZE-three_finger_moveup_open split screen-liyongli-2018-04-23-end*/

}
