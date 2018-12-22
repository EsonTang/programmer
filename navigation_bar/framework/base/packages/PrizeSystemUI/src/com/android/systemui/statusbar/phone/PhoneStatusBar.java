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


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.NonNull;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
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
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import android.telephony.SubscriptionManager;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ThreadedRenderer;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.DemoMode;
import com.android.systemui.EventLogConstants;
import com.android.systemui.EventLogTags;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.recents.ScreenPinningRequest;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.BackDropView;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.DismissView;
import com.android.systemui.statusbar.DragDownHelper;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.NotificationOverflowContainer;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.SpeedBumpView;
import com.android.systemui.statusbar.StatusBarState;
import com.android.systemui.statusbar.phone.UnlockMethodCache.OnUnlockMethodChangedListener;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;
import com.android.systemui.statusbar.policy.BluetoothControllerImpl;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.statusbar.policy.CastControllerImpl;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.HotspotControllerImpl;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.PreviewInflater;
import com.android.systemui.statusbar.policy.RotationLockControllerImpl;
import com.android.systemui.statusbar.policy.SecurityControllerImpl;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout.OnChildLocationsChangedListener;
import com.android.systemui.statusbar.stack.StackViewState;
import com.android.systemui.volume.VolumeComponent;
import com.haokan.yitu.view.ScreenView;
import com.mediatek.systemui.ext.IStatusBarPlmnPlugin;
import com.mediatek.systemui.statusbar.extcb.PluginFactory;
/// M: Modify statusbar style for GMO
import com.mediatek.systemui.statusbar.util.FeatureOptions;
/// M: Add extra tiles
import com.mediatek.systemui.statusbar.policy.AudioProfileControllerImpl;
import com.mediatek.systemui.statusbar.policy.HotKnotControllerImpl;
import com.mediatek.systemui.statusbar.util.SIMHelper;
// /@}
import com.mediatek.systemui.statusbar.defaultaccount.DefaultAccountStatus;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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
/// M: add for multi window @{
import android.widget.FrameLayout;
import android.view.ViewGroup;
import android.view.Gravity;
import com.android.systemui.recents.RecentsActivity;
import com.mediatek.common.multiwindow.IMWSystemUiCallback;
import com.mediatek.multiwindow.MultiWindowProxy;
/// @}

/*PRIZE-import package- liufan-2015-04-10-start*/
import android.text.TextUtils;
import android.os.SystemProperties;
import com.android.systemui.FontSizeUtils;
import android.view.ViewGroup;
import com.android.systemui.recents.RecentsActivity;
import android.widget.ImageView.ScaleType;
import com.mediatek.systemui.statusbar.policy.DataConnectionControllerImpl;
import com.android.systemui.recents.Recents;
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
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.views.TaskView;
import com.android.systemui.recents.views.TaskStackView;
import android.text.format.Formatter;
import com.android.systemui.recents.LoadIconUtils;
import android.telecom.TelecomManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
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
/* app multi instances feature. prize-linkh-20151228 */
import android.util.PrizeAppInstanceUtils;
/*PRIZE-Add for BluLight-zhudaopeng-2017-05-12-Start*/
import com.mediatek.pq.PictureQuality;
/*PRIZE-Add for BluLight-zhudaopeng-2017-05-12-End*/


public class PhoneStatusBar extends BaseStatusBar implements DemoMode,
        DragDownHelper.DragDownCallback, ActivityStarter, OnUnlockMethodChangedListener,
        HeadsUpManager.OnHeadsUpChangedListener {
    static final String TAG = "PhoneStatusBar";
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
    public static final int FADE_KEYGUARD_DURATION_PULSING = 120;

    /** Allow some time inbetween the long press for back and recents. */
    private static final int LOCK_TO_APP_GESTURE_TOLERENCE = 200;

    /** If true, the system is in the half-boot-to-decryption-screen state.
     * Prudently disable QS and notifications.  */
    private static final boolean ONLY_CORE_APPS;
    private boolean mIsSplitModeOn = MultiWindowProxy.isSplitMode();
    private boolean mIsSplitModeEnable = MultiWindowProxy.isSplitModeEnabled();
    static {
        boolean onlyCoreApps;
        try {
            onlyCoreApps = IPackageManager.Stub.asInterface(ServiceManager.getService("package"))
                    .isOnlyCoreApps();
        } catch (RemoteException e) {
            onlyCoreApps = false;
        }
        ONLY_CORE_APPS = onlyCoreApps;
    }

    PhoneStatusBarPolicy mIconPolicy;

    // These are no longer handled by the policy, because we need custom strategies for them
    BluetoothControllerImpl mBluetoothController;
    SecurityControllerImpl mSecurityController;
    BatteryController mBatteryController;
    LocationControllerImpl mLocationController;
    NetworkControllerImpl mNetworkController;
    HotspotControllerImpl mHotspotController;
    RotationLockControllerImpl mRotationLockController;
    UserInfoController mUserInfoController;
    ZenModeController mZenModeController;
    CastControllerImpl mCastController;
    VolumeComponent mVolumeComponent;
    KeyguardUserSwitcher mKeyguardUserSwitcher;
    FlashlightController mFlashlightController;
    UserSwitcherController mUserSwitcherController;
    NextAlarmController mNextAlarmController;
    KeyguardMonitor mKeyguardMonitor;
    BrightnessMirrorController mBrightnessMirrorController;
    AccessibilityController mAccessibilityController;
    /// M: Add extra tiles @{
    //add HotKnot in quicksetting
    HotKnotControllerImpl mHotKnotController;
    //add AudioProfile in quicksetting
    AudioProfileControllerImpl mAudioProfileController;
    // /@}
    FingerprintUnlockController mFingerprintUnlockController;
    DataConnectionControllerImpl mDataConnectionController;

    int mNaturalBarHeight = -1;

    Display mDisplay;
    Point mCurrentDisplaySize = new Point();

    StatusBarWindowView mStatusBarWindow;
    PhoneStatusBarView mStatusBarView;
    ScreenView mScreenView;
    private int mStatusBarWindowState = WINDOW_STATE_SHOWING;
    private StatusBarWindowManager mStatusBarWindowManager;
    private UnlockMethodCache mUnlockMethodCache;
    private DozeServiceHost mDozeServiceHost;
    private boolean mWakeUpComingFromTouch;
    private PointF mWakeUpTouchLocation;

    int mPixelFormat;
    Object mQueueLock = new Object();

    StatusBarIconController mIconController;

    // expanded notifications
    NotificationPanelView mNotificationPanel; // the sliding/resizing panel within the notification window
    View mExpandedContents;
    TextView mNotificationPanelDebugText;

    // settings
    private QSPanel mQSPanel;

    // top bar
    StatusBarHeaderView mHeader;
    KeyguardStatusBarView mKeyguardStatusBar;
    View mKeyguardStatusView;
    KeyguardBottomAreaView mKeyguardBottomArea;
    boolean mLeaveOpenOnKeyguardHide;
    KeyguardIndicationController mKeyguardIndicationController;

    private boolean mKeyguardFadingAway;
    private long mKeyguardFadingAwayDelay;
    private long mKeyguardFadingAwayDuration;

    int mKeyguardMaxNotificationCount;

    private TextView mCarrierLabel;
    private boolean mShowCarrierInPanel = false;
    boolean mExpandedVisible;

    private int mNavigationBarWindowState = WINDOW_STATE_SHOWING;

    // the tracker view
    int mTrackingPosition; // the position of the top of the tracking view.

    // Tracking finger for opening/closing.
    boolean mTracking;
    VelocityTracker mVelocityTracker;

    int[] mAbsPos = new int[2];
    ArrayList<Runnable> mPostCollapseRunnables = new ArrayList<>();

    // for disabling the status bar
    int mDisabled1 = 0;
    int mDisabled2 = 0;

    // tracking calls to View.setSystemUiVisibility()
    int mSystemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;

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

    private ViewMediatorCallback mKeyguardViewMediatorCallback;
    private ScrimController mScrimController;
    private DozeScrimController mDozeScrimController;

    private final Runnable mAutohide = new Runnable() {
        @Override
        public void run() {
            int requested = mSystemUiVisibility & ~STATUS_OR_NAV_TRANSIENT;
            if (mSystemUiVisibility != requested) {
                notifyUiVisibilityChanged(requested);
            }
        }};

    private boolean mWaitingForKeyguardExit;
    private boolean mDozing;
    private boolean mDozingRequested;
    private boolean mScrimSrcModeEnabled;

    private Interpolator mLinearInterpolator = new LinearInterpolator();
    private Interpolator mBackdropInterpolator = new AccelerateDecelerateInterpolator();
    public static final Interpolator ALPHA_IN = new PathInterpolator(0.4f, 0f, 1f, 1f);
    public static final Interpolator ALPHA_OUT = new PathInterpolator(0f, 0f, 0.8f, 1f);

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
                    updateMediaMetaData(true);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            if (DEBUG_MEDIA) Log.v(TAG, "DEBUG_MEDIA: onMetadataChanged: " + metadata);
            mMediaMetadata = metadata;
            updateMediaMetaData(true);
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

    private int mDrawCount;
    private Runnable mLaunchTransitionEndRunnable;
    private boolean mLaunchTransitionFadingAway;
    private ExpandableNotificationRow mDraggedDownRow;

    // Fingerprint (as computed by getLoggingFingerprint() of the last logged state.
    private int mLastLoggedStateFingerprint;

    private static final int VISIBLE_LOCATIONS = StackViewState.LOCATION_FIRST_CARD
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
    private HashSet<Entry> mHeadsUpEntriesToRemoveOnSwitch = new HashSet<>();
    private RankingMap mLatestRankingMap;
    private boolean mNoAnimationOnNextBarModeChange;

    @Override
    public void start() {
        mDisplay = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        updateDisplaySize();
        mScrimSrcModeEnabled = mContext.getResources().getBoolean(
                R.bool.config_status_bar_scrim_behind_use_src);

		/*prize-public-standard:Changed lock screen-liuweiquan-20151212-start*/
		//if(PrizeOption.PRIZE_CHANGED_WALLPAPER&&0!=Settings.System.getInt(mContext.getContentResolver(),Settings.System.PRIZE_KGWALLPAPER_SWITCH,0)){
		if(PrizeOption.PRIZE_CHANGED_WALLPAPER){
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

        //start..............prize-linkh-20150724
        //add for navbar style. 
        if(mSupportNavbarStyle) {
            mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.PRIZE_NAVIGATION_BAR_STYLE),
                false, mNavbarStyleObserver);
        }
        //add for always showing nav bar.
        if(mSupportHidingNavBar) {
            mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.PRIZE_ALLOW_HIDING_NAVBAR),
                false, mAlwaysShowNavBarObserver);
        }
        // add for dynamically changing Recents function. 
        if(mSupportSettingRecentsAsMenu) {
            mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.PRIZE_USE_RECENTS_AS_MENU),
                false, mUseRecentsAsMenuObserver);
        }
        
        // add for mBack device. prize-linkh-20160805
        if(SUPPORT_NAV_BAR_FOR_MBACK_DEVICE) {
            mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.PRIZE_NAVBAR_STATE_FOR_MBACK),
                false, mNavBarStateFormBackObserver);
        }        
        //end............
        
        // Lastly, call to the icon policy to install/update all the icons.
        mIconPolicy = new PhoneStatusBarPolicy(mContext, mCastController, mHotspotController,
                mUserInfoController, mBluetoothController);
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

        mDozeServiceHost = new DozeServiceHost();
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mDozeServiceHost);
        putComponent(DozeHost.class, mDozeServiceHost);
        putComponent(PhoneStatusBar.class, this);

        /// M:add for multi window @{
        if(MultiWindowProxy.isSupported()) {
            registerMWProxyAgain();
        }
        /// @}
        setControllerUsers();

        notifyUserAboutHiddenNotifications();

        mScreenPinningRequest = new ScreenPinningRequest(mContext);
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
        onStatusBarStyleChanged(style);
   }
   //end..............

    //add for hiding nav bar. prize-linkh-20150714
    private static final boolean MY_DEBUG = "eng".equals(android.os.Build.TYPE);
    private static final String NAV_BAR_CONTROL_INTENT = "com.prize.nav_bar_control";
    private static final String NAV_BAR_CONTROL_CMD = "command";
    private static final String NAV_BAR_STATE = "state";
    private boolean mSupportHidingNavBar;
    private boolean mAlwaysShowNavBar = false;
    private NavigationBarView mNavigationBarViewBackup = null;
    private boolean mIsNavBarHidden = false;
    
    // Solve an issue for dynamically hiding nav bar(bug-19908). prize-linkh-20160808
    // recording for Phone Window Manager to choose different policies.
    public static final String NAV_BAR_GONE_BECAUSE_OF_KEYGUARD = "keyguard";
    public static final String NAV_BAR_GONE_BECAUSE_OF_MBACK = "mback";
    public static final String NAV_BAR_GONE_BECAUSE_OF_USER = "user";
    //private String mNavBarGoneReason;
    private WindowManager.LayoutParams mNavBarLayoutParams;
    // END...

    // add for mBack device. prize-linkh-20160804
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
    private boolean needHideNavBarFormBack() {
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
               printMyLog("--click. hide Navigation Bar!");
               hideNavBar();
          }
    };    
    private ContentObserver mAlwaysShowNavBarObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                printMyLog("mAlwaysShowNavBarObserver--onChange() --");
                int allow = Settings.System.getInt(
                    mContext.getContentResolver(), 
                    Settings.System.PRIZE_ALLOW_HIDING_NAVBAR,
                    1);
                boolean alwaysShow = (allow == 1) ? false : true;
                if(alwaysShow != mAlwaysShowNavBar && mNavigationBarView != null) {
                    mAlwaysShowNavBar = alwaysShow;
                    mNavigationBarView.setAlwaysShowNavBar(mAlwaysShowNavBar);
                    if(mAlwaysShowNavBar) {
                        showNavBar();
                    }
                }
            }

     };

    private void printMyLog(String msg) {
        if(MY_DEBUG) {
            Log.d("SystemUI-iroot",  msg);
        }
    }

    private void registerHideNavBarClickListener() {
        printMyLog("--registerHideNavBarClickListener() .");
        View v = mNavigationBarView.getForceHideButton();
        if(v != null) {
            v.setOnClickListener(mHideNavBarListener);
        }
    }

    private void hideNavBar() {
        hideNavBar(false);
    }
    
    private void hideNavBar(boolean ismBack) {
       printMyLog("hideNavBar() ismBack=" + ismBack);

       if((!mSupportHidingNavBar && !SUPPORT_NAV_BAR_FOR_MBACK_DEVICE) 
            /*|| mAlwaysShowNavBar*/ || mNavigationBarView == null) {
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
       
        if((!mSupportHidingNavBar && !SUPPORT_NAV_BAR_FOR_MBACK_DEVICE)
            /*|| !mIsNavBarHidden*/ || mNavigationBarView == null) {
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
    
    //add for nav bar style.
    private boolean mSupportNavbarStyle = false;
    private ContentObserver mNavbarStyleObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                printMyLog("mNavbarStyleObserver--onChange() --");
                int style = Settings.System.getInt(
                    mContext.getContentResolver(), 
                    Settings.System.PRIZE_NAVIGATION_BAR_STYLE,
                    0);
                if(mNavigationBarView != null) {
                    mNavigationBarView.updateNavButtonOrientation(style);
                }
            }

     };

    //force navbar to have the same function of the virtual keys. prize-linkh-20151123
    private boolean mHasLongPressEvent =false;
    private View.OnLongClickListener mLongPressHomeToRecentsListener =
            new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            /*PRIZE-PowerExtendMode-disable Recents-wangxianzhen-2015-07-21-start*/
            //if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
                //Log.i(TAG,"PowerExtendMode mLongPressHomeToRecentsListener");
                //return true;
            //}
            /*PRIZE-PowerExtendMode-disable Recents-wangxianzhen-2015-07-21-end*/
            
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
            /*PRIZE-PowerExtendMode-disable Recents-wangxianzhen-2015-07-21-start*/
            //if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
              //  Log.i(TAG,"PowerExtendMode mRecentsPreloadForHomeOnTouchListener");
               // return false;
            //}
            /*PRIZE-PowerExtendMode-disable Recents-wangxianzhen-2015-07-21-end*/
            
            int action = event.getAction() & MotionEvent.ACTION_MASK;
            if (action == MotionEvent.ACTION_DOWN) {
                mHasLongPressEvent = false;
                preloadRecents();
            } else if (action == MotionEvent.ACTION_CANCEL) {
                mHasLongPressEvent = false;
                if((mSystemUiVisibility & View.RECENT_APPS_VISIBLE) == 0) {
                    //Log.i("daxian-sb","onTouch. cancel event. Recents isn't visible!");
                    cancelPreloadingRecents();
                }
            } else if (action == MotionEvent.ACTION_UP) {
                if(mHasLongPressEvent) {
                    //In toggling recent. Send home cancel event instead of sending up event.
                    //Log.i("daxian-sb","onTouch. up event. In showing/hiding Recents!");
                    
                    ((KeyButtonView)v).setSendHomeCancelEventOnce(true);
                } else {
                    //Log.i("daxian-sb","onTouch. up event.");
                
                    ((KeyButtonView)v).setSendHomeCancelEventOnce(false);
                    //cancelPreloadingRecents();
                }
                
                mHasLongPressEvent = false;
            }
            
            return false;
        }
    }; 

    // add for dynamically changing Recents function. prize-linkh-20160722    
    private boolean mSupportSettingRecentsAsMenu;
    private boolean mUseRecentsAsMenu;
    private ContentObserver mUseRecentsAsMenuObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            printMyLog("mUseRecentsAsMenuObserver--onChange()..");
            final boolean enabled = Settings.System.getInt(mContext.getContentResolver(),                 
                                    Settings.System.PRIZE_USE_RECENTS_AS_MENU, 0) == 1;
            if (mUseRecentsAsMenu == enabled) {
                return;
            }
            prepareNavigationBarView();
            setRecentsAsMenu(enabled, false);
        }
    };

    private void setRecentsAsMenu(boolean enabled, boolean force) {
        printMyLog("setRecentsAsMenu() enabled=" + enabled + ", force=" + force);
        if (!force && mUseRecentsAsMenu == enabled) {
            printMyLog("setRecentsAsMenu() same state!");
            return;
        }

        mUseRecentsAsMenu = enabled;
        if (mNavigationBarView != null) {
            mNavigationBarView.setRecentsAsMenu(enabled);            
            if (enabled) {
                // Recents button
                KeyButtonView v = (KeyButtonView)mNavigationBarView.getRecentsButton();
                v.adjustKeyCode(android.view.KeyEvent.KEYCODE_MENU);
                v.setOnClickListener(null);
                v.setOnTouchListener(null);
                
                // Home button
                v = (KeyButtonView)mNavigationBarView.getHomeButton();
                v.setOnTouchListener(mRecentsPreloadForHomeOnTouchListener);
                v.setOnLongClickListener(mLongPressHomeToRecentsListener);
            } else {
                // Recents button
                KeyButtonView v = (KeyButtonView)mNavigationBarView.getRecentsButton();
                v.adjustKeyCode(0);
            }
        }
    }
    //END....


   public static boolean BLUR_BG_CONTROL = true;//true:blur bg; false:scrim bg
   public static boolean STATUS_BAR_DROPDOWN_STYLE = true;//true:drop down; false:alpha change
    // ================================================================================
    // Constructing the view
    // ================================================================================
    protected PhoneStatusBarView makeStatusBarView() {
        final Context context = mContext;

        Resources res = context.getResources();

        updateDisplaySize(); // populates mDisplayMetrics
        updateResources();

        /**PRIZE-new layout-liufan-2015-10-30-start */
            mStatusBarWindow = (StatusBarWindowView) View.inflate(context,
                R.layout.super_status_bar_prize, null);
        /**PRIZE-new layout-liufan-2015-10-30-end */
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
                /*PRIZE-dismiss KeyguardChargeAnimationView when move happened on lockscreen-liufan-2015-07-15-start*/
                if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
                    downY = event.getY();
                } else if(event.getActionMasked() == MotionEvent.ACTION_MOVE){
                    if((event.getY()-downY) > 0){
                        //isShowKeyguardChargingAnimation(false,false,false);
                    }
                }else if(event.getActionMasked() == MotionEvent.ACTION_CANCEL || event.getActionMasked() == MotionEvent.ACTION_UP){
                    //isShowKeyguardChargingAnimation(true,true,false);
                }
                /*PRIZE-dismiss KeyguardChargeAnimationView when move happened on lockscreen-liufan-2015-07-15-end*/
                return mStatusBarWindow.onTouchEvent(event);
            }
        });

        mStatusBarView = (PhoneStatusBarView) mStatusBarWindow.findViewById(R.id.status_bar);
        mStatusBarView.setBar(this);
        
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

        PanelHolder holder = (PanelHolder) mStatusBarWindow.findViewById(R.id.panel_holder);
		
        mStatusBarView.setPanelHolder(holder);

        mNotificationPanel = (NotificationPanelView) mStatusBarWindow.findViewById(
                R.id.notification_panel);
        mNotificationPanel.setStatusBar(this);
		
        /**PRIZE-haokan lockscreen wallpaper-liufan-2016-06-18-start 
        mScreenView = (ScreenView) mStatusBarWindow.findViewById(R.id.screenview);
        if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW){
            mScreenView.setVisibility(View.VISIBLE);
			mScreenView.setEntryApp();
        } else {
            mScreenView.setVisibility(View.GONE);
        }
        //holder.setVisibility(View.GONE);
		mScreenView.setNotificationPanel(mNotificationPanel);
        /**PRIZE-haokan lockscreen wallpaper-liufan-2016-06-18-end */
		
        /*PRIZE-KeyguardChargeAnimationView- liufan-2015-07-08-start*/
        mKeyguardChargeAnimationView = (KeyguardChargeAnimationView) mStatusBarWindow.findViewById(R.id.keyguard_charge_animation_view);
        mKeyguardChargeAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });
        /*PRIZE-KeyguardChargeAnimationView- liufan-2015-07-08-end*/
        
        /*PRIZE-the blur layout- liufan-2015-06-09-start*/
        mNotificationBg = (LinearLayout) mStatusBarWindow.findViewById(
                R.id.notification_bg);
        mNotificationHeaderBg = (NotificationHeaderLayout) mStatusBarWindow.findViewById(
                R.id.notification_bg_header);
        /*PRIZE-the blur layout- liufan-2015-06-09-end*/
        //  M: setBackground in 512 low ram device
        if (!ActivityManager.isHighEndGfx() && !FeatureOptions.LOW_RAM_SUPPORT) {
            mStatusBarWindow.setBackground(null);
            mNotificationPanel.setBackground(new FastColorDrawable(context.getColor(
                    R.color.notification_panel_solid_background)));
        }

        mHeadsUpManager = new HeadsUpManager(context, mStatusBarWindow);
        mHeadsUpManager.setBar(this);
        mHeadsUpManager.addListener(this);
        mHeadsUpManager.addListener(mNotificationPanel);
        mNotificationPanel.setHeadsUpManager(mHeadsUpManager);
        mNotificationData.setHeadsUpManager(mHeadsUpManager);

        if (MULTIUSER_DEBUG) {
            mNotificationPanelDebugText = (TextView) mNotificationPanel.findViewById(
                    R.id.header_debug_info);
            mNotificationPanelDebugText.setVisibility(View.VISIBLE);
        }

        try {
            boolean showNav = mWindowManagerService.hasNavigationBar();
            if (DEBUG) Log.v(TAG, "hasNavigationBar=" + showNav);
            
            //add for hiding nav bar. prize-linkh-20150714
            if(PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR) {
                mSupportHidingNavBar = showNav;
            }
            int allow = Settings.System.getInt(
                    mContext.getContentResolver(), 
                    Settings.System.PRIZE_ALLOW_HIDING_NAVBAR,
                    1);
            mAlwaysShowNavBar = (allow == 1) ? false : true;
            if(showNav) {
                mSupportNavbarStyle = mContext.getResources().getBoolean(
                    com.prize.internal.R.bool.support_navbar_style);
            } else {
                mSupportNavbarStyle = false;
            }

            if(PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR && mSupportHidingNavBar) {
                 //The navbar is always visibile in default, so we
                // set 1 to SetingsProvider.
                Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.PRIZE_NAVBAR_STATE, 1);
            }
            //end...
            
            // add for dynamically changing Recents function. 
            if (showNav && PrizeOption.PRIZE_SUPPORT_SETTING_RECENTS_AS_MENU) {
                mSupportSettingRecentsAsMenu = true;
                mUseRecentsAsMenu = Settings.System.getInt(mContext.getContentResolver(),                 
                                        Settings.System.PRIZE_USE_RECENTS_AS_MENU, 0) == 1;
            } else {
                mSupportSettingRecentsAsMenu = false;
                mUseRecentsAsMenu = false;
            } //END...
            
            if (showNav) {
                /// M: add for multi window @{
                int layoutId = R.layout.navigation_bar;
                if(MultiWindowProxy.isSupported()) {
                    layoutId = R.layout.navigation_bar_float_window;
                }
                mNavigationBarView = (NavigationBarView) View.inflate(context,
                        /*R.layout.navigation_bar*/layoutId, null);
                /// @}

                mNavigationBarView.setDisabledFlags(mDisabled1);
                mNavigationBarView.setBar(this);
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

                //add for hiding nav bar. prize-linkh-20150714
                if(PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR) {
                    registerHideNavBarClickListener();
                }
                    
            }
        } catch (RemoteException ex) {
            // no window manager? good luck with that
        }

        mAssistManager = new AssistManager(this, context);

        // figure out which pixel-format to use for the status bar.
        mPixelFormat = PixelFormat.OPAQUE;

        mStackScroller = (NotificationStackScrollLayout) mStatusBarWindow.findViewById(
                R.id.notification_stack_scroller);
        mStackScroller.setLongPressListener(getNotificationLongClicker());
        mStackScroller.setPhoneStatusBar(this);
        mStackScroller.setGroupManager(mGroupManager);
        mStackScroller.setHeadsUpManager(mHeadsUpManager);
        mGroupManager.setOnGroupChangeListener(mStackScroller);

        mKeyguardIconOverflowContainer =
                (NotificationOverflowContainer) LayoutInflater.from(mContext).inflate(
                        R.layout.status_bar_notification_keyguard_overflow, mStackScroller, false);
        mKeyguardIconOverflowContainer.setOnActivatedListener(this);
        mKeyguardIconOverflowContainer.setOnClickListener(mOverflowClickListener);
        mStackScroller.setOverflowContainer(mKeyguardIconOverflowContainer);

        SpeedBumpView speedBump = (SpeedBumpView) LayoutInflater.from(mContext).inflate(
                        R.layout.status_bar_notification_speed_bump, mStackScroller, false);
        mStackScroller.setSpeedBumpView(speedBump);
        mEmptyShadeView = (EmptyShadeView) LayoutInflater.from(mContext).inflate(
                R.layout.status_bar_no_notifications, mStackScroller, false);
        mStackScroller.setEmptyShadeView(mEmptyShadeView);
        mDismissView = (DismissView) LayoutInflater.from(mContext).inflate(
                R.layout.status_bar_notification_dismiss_all, mStackScroller, false);
        mDismissView.setOnButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MetricsLogger.action(mContext, MetricsLogger.ACTION_DISMISS_ALL_NOTES);
                clearAllNotifications();
            }
        });
        mStackScroller.setDismissView(mDismissView);
        mExpandedContents = mStackScroller;

        mBackdrop = (BackDropView) mStatusBarWindow.findViewById(R.id.backdrop);
        mBackdropFront = (ImageView) mBackdrop.findViewById(R.id.backdrop_front);
        mBackdropBack = (ImageView) mBackdrop.findViewById(R.id.backdrop_back);
        /*PRIZE-lockscreen blur bg layout- liufan-2015-09-02-start*/
        mBlurBack = (LinearLayout) mStatusBarWindow.findViewById(R.id.blur_back);
        FrameLayout.LayoutParams blurBackParams = new FrameLayout.LayoutParams(mDisplayMetrics.widthPixels, LayoutParams.MATCH_PARENT);
        mBlurBack.setLayoutParams(blurBackParams);
        /*PRIZE-lockscreen blur bg layout- liufan-2015-09-02-end*/

        ScrimView scrimBehind = (ScrimView) mStatusBarWindow.findViewById(R.id.scrim_behind);
        ScrimView scrimInFront = (ScrimView) mStatusBarWindow.findViewById(R.id.scrim_in_front);
        View headsUpScrim = mStatusBarWindow.findViewById(R.id.heads_up_scrim);
        mScrimController = new ScrimController(scrimBehind, scrimInFront, headsUpScrim,
                mScrimSrcModeEnabled);
        mHeadsUpManager.addListener(mScrimController);
        mStackScroller.setScrimController(mScrimController);
        mScrimController.setBackDropView(mBackdrop);
        /*PRIZE-send the blur layout to ScrimController- liufan-2015-06-09-start*/
        if (VersionControl.CUR_VERSION == VersionControl.BLUR_BG_VER) {
            if(STATUS_BAR_DROPDOWN_STYLE) mScrimController.setNotificationBackgroundLayout(mNotificationBg);
            mScrimController.setLockscreenBlurLayout(mBlurBack);
        }
        /*PRIZE-send the blur layout to ScrimController- liufan-2015-06-09-end*/
		
		mNotificationPanel.setLockscreenBlurLayout(mBlurBack);
		
        mStatusBarView.setScrimController(mScrimController);
        mDozeScrimController = new DozeScrimController(mScrimController, context);

        mHeader = (StatusBarHeaderView) mStatusBarWindow.findViewById(R.id.header);
        mHeader.setActivityStarter(this);
        mKeyguardStatusBar = (KeyguardStatusBarView) mStatusBarWindow.findViewById(R.id.keyguard_header);
        mKeyguardStatusView = mStatusBarWindow.findViewById(R.id.keyguard_status_view);
        mKeyguardBottomArea =
                (KeyguardBottomAreaView) mStatusBarWindow.findViewById(R.id.keyguard_bottom_area);
        mKeyguardBottomArea.setActivityStarter(this);
        mKeyguardBottomArea.setAssistManager(mAssistManager);
        mKeyguardIndicationController = new KeyguardIndicationController(mContext,
                (KeyguardIndicationTextView) mStatusBarWindow.findViewById(
                        R.id.keyguard_indication_text),
                mKeyguardBottomArea.getLockIcon());
        mKeyguardBottomArea.setKeyguardIndicationController(mKeyguardIndicationController);

        // set the inital view visibility
        setAreThereNotifications();

        mIconController = new StatusBarIconController(
                mContext, mStatusBarView, mKeyguardStatusBar, this);
		//add for statusbar inverse. prize-linkh-20150903
		addStatusBarStyleListener(mIconController,false);
		//end...

        // Background thread for any controllers that need it.
        mHandlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();

        // Other icons
        mLocationController = new LocationControllerImpl(mContext,
                mHandlerThread.getLooper()); // will post a notification
        mBatteryController = new BatteryController(mContext);
        mBatteryController.addStateChangedCallback(new BatteryStateChangeCallback() {
            @Override
            public void onPowerSaveChanged() {
                mHandler.post(mCheckBarModes);
                if (mDozeServiceHost != null) {
                    mDozeServiceHost.firePowerSaveChanged(mBatteryController.isPowerSave());
                }
            }
            @Override
            public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
                // noop
            }
        });
        /**PRIZE 电量图标及百分比修改 2015-06-25 start */
        //mBatteryController.addIconView((ImageView)mStatusBarView.findViewById(R.id.battery));
        mBatteryController.addLevelView((TextView)mStatusBarView.findViewById(R.id.battery_percentage));
        /**PRIZE 电量图标及百分比修改 2015-06-25 end */
        mNetworkController = new NetworkControllerImpl(mContext, mHandlerThread.getLooper());
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

        // add AudioProfile in quicksetting
        if (SIMHelper.isMtkAudioProfilesSupport()) {
            Log.d(TAG, "makeStatusBarView : AudioProfileControllerImpl");
            mAudioProfileController = new AudioProfileControllerImpl(mContext);
        } else {
            mAudioProfileController = null;
        }

        SIMHelper.setContext(mContext);
        // /@}
        if(!SIMHelper.isWifiOnlyDevice()) {
            Log.d(TAG, "makeStatusBarView : DataConnectionControllerImpl");
            mDataConnectionController = new DataConnectionControllerImpl(mContext);
        } else {
            mDataConnectionController = null;
        }

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
        Log.d(TAG, "makeStatusBarView : CastControllerImpl -");
        final SignalClusterView signalCluster =
                (SignalClusterView) mStatusBarView.findViewById(R.id.signal_cluster);
        //add for statusbar inverse. prize-linkh-20150903
        if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR) {
            signalCluster.setIgnoreStatusBarStyleChanged(false);
            addStatusBarStyleListener(mNetworkController, false);
        }
        final SignalClusterView signalClusterKeyguard =
                (SignalClusterView) mKeyguardStatusBar.findViewById(R.id.signal_cluster);
        final SignalClusterView signalClusterQs =
                (SignalClusterView) mHeader.findViewById(R.id.signal_cluster);
        mNetworkController.addSignalCallback(signalCluster);
        mNetworkController.addSignalCallback(signalClusterKeyguard);
        mNetworkController.addSignalCallback(signalClusterQs);
        signalCluster.setSecurityController(mSecurityController);
        signalCluster.setNetworkController(mNetworkController);
        signalClusterKeyguard.setSecurityController(mSecurityController);
        signalClusterKeyguard.setNetworkController(mNetworkController);
        signalClusterQs.setSecurityController(mSecurityController);
        signalClusterQs.setNetworkController(mNetworkController);
        final boolean isAPhone = mNetworkController.hasVoiceCallingFeature();
        if (isAPhone) {
            mNetworkController.addEmergencyListener(mHeader);
        }

        mCarrierLabel = (TextView)mStatusBarWindow.findViewById(R.id.carrier_label);
        /*PRIZE-dismiss the operator Information liyao-2015-07-13-start*/
        mShowCarrierInPanel = FeatureOption.PRIZE_QS_SORT ? false :(mCarrierLabel != null);
        /*PRIZE-dismiss the operator Information liyao-2015-07-13-end*/
        /// M: Support "Operator plugin - Customize Carrier Label for PLMN" @{
        mStatusBarPlmnPlugin = PluginFactory.getStatusBarPlmnPlugin(context);
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
        mKeyguardMonitor = new KeyguardMonitor(mContext);
        if (UserSwitcherController.isUserSwitcherAvailable(UserManager.get(mContext))) {
            mUserSwitcherController = new UserSwitcherController(mContext, mKeyguardMonitor,
                    mHandler);
        }
        mKeyguardUserSwitcher = new KeyguardUserSwitcher(mContext,
                (ViewStub) mStatusBarWindow.findViewById(R.id.keyguard_user_switcher),
                mKeyguardStatusBar, mNotificationPanel, mUserSwitcherController);


        // Set up the quick settings tile panel
        mQSPanel = (QSPanel) mStatusBarWindow.findViewById(R.id.quick_settings_panel);
        if (mQSPanel != null) {
            /*PRIZE-set the listener of the base tile- liufan-2015-04-10-start*/
            final QSTileHost qsh = FeatureOption.PRIZE_QS_SORT ? new QSTileHost(mContext, this,
                    mBluetoothController, mLocationController, mRotationLockController,
                    mNetworkController, mZenModeController, mHotspotController,
                    mCastController, mFlashlightController,
                    mUserSwitcherController, mKeyguardMonitor,
                    mSecurityController,
                    /// M: add HotKnot in quicksetting
                    mHotKnotController,
                    /// M: add AudioProfile in quicksetting
                    mAudioProfileController,
                    /// M: add DataConnection in quicksetting
                    mDataConnectionController,
                    onTileClickListener,
                    mHeader,mBatteryController)
            : new QSTileHost(mContext, this,
                    mBluetoothController, mLocationController, mRotationLockController,
                    mNetworkController, mZenModeController, mHotspotController,
                    mCastController, mFlashlightController,
                    mUserSwitcherController, mKeyguardMonitor,
                    mSecurityController,
                    /// M: add HotKnot in quicksetting
                    mHotKnotController,
                    /// M: add AudioProfile in quicksetting
                    mAudioProfileController
            );
            /*PRIZE-set the listener of the base tile- liufan-2015-04-10-end*/
            mQSPanel.setHost(qsh);
            mQSPanel.setTiles(qsh.getTiles());
            mBrightnessMirrorController = new BrightnessMirrorController(mStatusBarWindow);
            /*PRIZE-add for brightness controller- liufan-2016-06-29-start*/
            mBrightnessMirrorController.setPhoneStatusBar(this);
            /*PRIZE-add for brightness controller- liufan-2016-06-29-end*/
            mQSPanel.setBrightnessMirror(mBrightnessMirrorController);
            mHeader.setQSPanel(mQSPanel);
            qsh.setCallback(new QSTileHost.Callback() {
                @Override
                public void onTilesChanged() {
                    mQSPanel.setTiles(qsh.getTiles());
                }
            });
        }

        // User info. Trigger first load.
        mHeader.setUserInfoController(mUserInfoController);
        mKeyguardStatusBar.setUserInfoController(mUserInfoController);
        mKeyguardStatusBar.setUserSwitcherController(mUserSwitcherController);
        mUserInfoController.reloadUserInfo();

        mHeader.setBatteryController(mBatteryController);

        if(!PrizeOption.PRIZE_SYSTEMUI_BATTERY_METER){
            ((BatteryMeterView) mStatusBarView.findViewById(R.id.battery)).setBatteryController(
                mBatteryController);
        }

        mKeyguardStatusBar.setBatteryController(mBatteryController);
        mHeader.setNextAlarmController(mNextAlarmController);
        //add for statusbar inverse. prize-linkh-20150903
        if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR) {
            //Init it.
            PrizeStatusBarStyle.getInstance(context);
            getViewsForStatusBarStyle();
        }
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mBroadcastReceiver.onReceive(mContext,
                new Intent(pm.isScreenOn() ? Intent.ACTION_SCREEN_ON : Intent.ACTION_SCREEN_OFF));


        // receive broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
		
        //add for hiding nav bar. prize-linkh-20150714      
        if(PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR) {
            filter.addAction(NAV_BAR_CONTROL_INTENT);
        }
		
		/*PRIZE-PowerExtendMode-to solve can't unlock problem-wangxianzhen-2016-01-13-start bugid 10971*/
        filter.addAction(ACTION_ENTER_SUPERPOWER);
        /*PRIZE-PowerExtendMode-to solve can't unlock problem-wangxianzhen-2016-01-13-end bugid 10971*/
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
        if(SUPPORT_KEYGUARD_WALLPAPER){
            mKeyguardWallpaperObserver = new KeyguardWallpaperObserver(mHandler);
            mKeyguardWallpaperObserver.startObserving();
        }
        /*PRIZE-show the percent of the power-liyao-2015-7-3-end*/

        /*PRIZE-listen the battery change-liufan-2015-7-8-start*/
        IntentFilter batteryFilter = new IntentFilter();
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mBatteryTracker, batteryFilter);
        /*PRIZE-listen the battery change-liufan-2015-7-8-end*/
        /*PRIZE-listen the launcher clean broadcast-liufan-2016-12-28-start*/
        IntentFilter launcherCleanFilter = new IntentFilter();
        launcherCleanFilter.addAction(mLauncherCleanAction);
        mContext.registerReceiver(mLauncherCleanReceiver, launcherCleanFilter);
        /*PRIZE-listen the launcher clean broadcast-liufan-2016-12-28-end*/
        /*PRIZE-register launcher theme change receiver-liufan-2016-05-12-start*/
        LoadIconUtils.registerLauncherThemeReceiver(mContext, mReceiver);
        /*PRIZE-register launcher theme change receiver-liufan-2016-05-12-end*/
        initThemeValue();//add by vlife-liufan-2016-08-22

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
        return mStatusBarView;
    }

    /*PRIZE-listen the launcher clean broadcast-liufan-2016-12-28-start*/
    private final String mLauncherCleanAction = "action.prize.launcher.clean";
    private final String mLauncherCleanEndAction = "action.prize.launcher.clean.end";
    private BroadcastReceiver mLauncherCleanReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(mLauncherCleanAction)) {
                Log.d(TAG,"receive broadcast action : " + action);
                deleteRecentsData();
                sendCleanEndBroadcast();
            }
        }
    };
    private void sendCleanEndBroadcast(){
        Log.d(TAG,"send clean end broadcast to launcher, action : " + mLauncherCleanEndAction);
        Intent intent = new Intent(mLauncherCleanEndAction);
        mContext.sendBroadcast(intent);
    }
    /*PRIZE-listen the launcher clean broadcast-liufan-2016-12-28-end*/

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
            return ;
        }
        int space = 4;
        if(isNetworkAvailable(context)){
            if(lastTotalRxBytes != 0){
                long curTotalRxBytes = TrafficStats.getTotalRxBytes();
                String speed = Formatter.formatFileSize(context, (curTotalRxBytes - lastTotalRxBytes)/space)+"/s";
                if(speed.indexOf("位元組") >= 0) speed = speed.replace("位元組","B");
                mNetworkSpeedTxt.setText(speed);
                lastTotalRxBytes = curTotalRxBytes;
            } else{
                lastTotalRxBytes = TrafficStats.getTotalRxBytes();
                String speed = Formatter.formatFileSize(context, 0) + "/s";
                if(speed.indexOf("位元組") >= 0) speed = speed.replace("位元組","B");
                mNetworkSpeedTxt.setText(speed);
            }
            if(mNetworkSpeedTxt.getVisibility() != View.VISIBLE) mNetworkSpeedTxt.setVisibility(View.VISIBLE);
        } else {
            lastTotalRxBytes = 0;
            mNetworkSpeedTxt.setText("");
            mNetworkSpeedTxt.setVisibility(View.GONE);
        }
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

	//add by haokan-liufan-2016-10-11-start
	public boolean isShowBlurBg(){
        int N = mNotificationData.getActiveNotifications().size();
        return !isShowLimitByNotifications(N);
	}
	
	public void openHaoKanSettings(){
		Intent intent = new Intent();
		ComponentName comp = new ComponentName("com.haokan.kubi","com.haokan.yitu.activity.LockScreenSettingActivity");
		intent.setComponent(comp);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent,false);
	}
	//add by haokan-liufan-2016-10-11-end
	
    //add by vlife-liufan-2016-08-22-start
    private void initThemeValue(){
        if(NotificationPanelView.VLIFE_GATE || NotificationPanelView.ZOOKING_GATE){
            String path = LoadIconUtils.getCurrrentThemePath(mContext);
            if(path != null) {
            	//Modify by zookingsoft-vincent 20161115 start
            	NotificationPanelView.USE_ZOOKING = LoadIconUtils.isZookingKeyguardTheme(mContext, path,true);
            	if (NotificationPanelView.USE_ZOOKING) {
            		mNotificationPanel.setStatusBar(this);
            		NotificationPanelView.USE_VLIFE = false;
            	} else {
            		NotificationPanelView.USE_VLIFE = LoadIconUtils.isVlifeKeyguardTheme(mContext, path);
            	}
            	//Modify by zookingsoft-vincent 20161115 end
            }
            refreshSystemUIByThemeValue(true);
        }
    }

    public void refreshSystemUIByThemeValue(boolean isInit){
        Log.d(TAG,"NotificationPanelView.USE_VLIFE--->"+NotificationPanelView.USE_VLIFE);
        //modify by zookingsoft-vincent-20161116
        mNotificationPanel.changeLockWallpaper(isInit);
        updateMediaMetaData(true);
		//add by haokan-2016-10-09-start
		if(NotificationPanelView.USE_VLIFE || NotificationPanelView.USE_ZOOKING){ //add by zookingsoft 20161115
			IS_USE_HAOKAN = false;
		}else{
			IS_USE_HAOKAN = true;
		}
        Log.d(TAG,"NotificationPanelView.USE_VLIFE IS_USE_HAOKAN--->"+IS_USE_HAOKAN);
		refreshHaoKanState();
		//add by haokan-2016-10-09-end
        mNotificationPanel.requestLayout();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(NotificationPanelView.VLIFE_GATE && action.equals(LoadIconUtils.THEME_EXE_ACTION)){
                String themePath = intent.getStringExtra(LoadIconUtils.THEME_EXE_PATH_KEY);
                if(themePath.equals(LoadIconUtils.path)){
                    return;
                }
              //Modify by zookingsoft 20161115 start
        Log.d(TAG,"NotificationPanelView.USE_VLIFE themePath--->"+themePath);
                LoadIconUtils.path = themePath;
                LoadIconUtils.saveThemePath(context,themePath);
                if(LoadIconUtils.path != null) {
                	NotificationPanelView.USE_ZOOKING = LoadIconUtils.isZookingKeyguardTheme(mContext, LoadIconUtils.path,false);
                	if (NotificationPanelView.USE_ZOOKING) {
                		NotificationPanelView.USE_VLIFE = false;
                	} else {
                		NotificationPanelView.USE_VLIFE = LoadIconUtils.isVlifeKeyguardTheme(mContext, LoadIconUtils.path);
                	}
                }
                
        Log.d(TAG,"NotificationPanelView.USE_VLIFE USE_ZOOKING--->"+NotificationPanelView.USE_ZOOKING);
        Log.d(TAG,"NotificationPanelView.USE_VLIFE USE_VLIFE--->"+NotificationPanelView.USE_VLIFE);
                if (NotificationPanelView.USE_VLIFE) {
                	if (!NotificationPanelView.VLIFE_GATE) {
                		NotificationPanelView.USE_VLIFE = false;
                		return;
                	} else {
                		mNotificationPanel.setStatusBar(PhoneStatusBar.this);
                	}
                }
                
                if (NotificationPanelView.USE_ZOOKING) {
                	if (!NotificationPanelView.ZOOKING_GATE) {
                		NotificationPanelView.USE_ZOOKING = false;
                		return;
                	} else {
                		mNotificationPanel.setStatusBar(PhoneStatusBar.this);
                	}
                }
               //Modify by zookingsoft 20161115 end
                refreshSystemUIByThemeValue(false);
            }
        }
    };
    //add by vlife-liufan-2016-08-22-end
    
    //add by haokan-liufan-2016-10-11-start
	public static boolean IS_USE_HAOKAN = true;
	public boolean isUseHaoKan(){
		return PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW && IS_USE_HAOKAN && mScreenView != null && mScreenView.getVisibility() == View.VISIBLE;
	}
    public void refreshHaoKanState(){
		if(!PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW){
			if(mScreenView!=null) mScreenView.setVisibility(View.GONE);
			mNotificationPanel.showLockIcon(false);
			return;
		}
        if(IS_USE_HAOKAN){
			if(mScreenView!=null) mScreenView.setVisibility(View.VISIBLE);
			mNotificationPanel.showLockIcon(true);
		} else {
			if(mScreenView!=null) mScreenView.setVisibility(View.GONE);
			mNotificationPanel.showLockIcon(false);
		}
	}
    //add by haokan-liufan-2016-10-11-end
	
    /*PRIZE-blur background- liufan-2015-06-08-start*/
    private int CRIM_COLOR = 0xe524373d;
    private Bitmap mWallPapaerBitmap;
    private LinearLayout mNotificationBg;
    private NotificationHeaderLayout mNotificationHeaderBg;
    private boolean isShowBg = false;
    private boolean isShowBlurBg = false;
    
    /**
    * Method Description：show the blur background (Screenshots for the background)
    */
    public void showBlurBackground(){
        Log.e("liufan","showBlurBackground-----00000---->");
        if(isShowBlurBg){
            return;
        }
        isShowBg = true;
        isShowBlurBg = true;
        if(!BLUR_BG_CONTROL){
            mWallPapaerBitmap = getHalfAlphaColorBackground(BACK_COLOR);
            if(isShowBg){
                setNotificationBackground();
            }
            isShowBlurBg = false;
            return;
        }
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable(){

            @Override
            public void run() {
                mWallPapaerBitmap = getBlurBackground();
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                tileHandler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        if(isShowBg){
        Log.e("liufan","showBlurBackground-----11111---->");
                            setNotificationBackground();
                        }
                        isShowBlurBg = false;
                    }
                },0);
            }
            
        });
    }
    
    /**
    * Method Description：show the blur background(the wallpaper for the background)
    */
    public void showBlurWallPaper(){
        Log.e("liufan","showBlurWallPaper-----00000---->");
        if(isShowBlurBg){
            return;
        }
        isShowBg = true;
        isShowBlurBg = true;
        if(!BLUR_BG_CONTROL){
            mWallPapaerBitmap = getHalfAlphaColorBackground(BACK_COLOR);
            if(isShowBg){
                setNotificationBackground();
            }
            isShowBlurBg = false;
            return;
        }
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable(){

            @Override
            public void run() {
                mWallPapaerBitmap = getBlurWallpaper();
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                tileHandler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        isShowBlurBg = false;
                        if(isShowBg){
        Log.e("liufan","showBlurWallPaper-----11111---->");
                            setNotificationBackground();
                        }
                    }
                },0);
            }
            
        });
    }

    //add by vlife-liufan-2016-08-22-start
    Runnable cancelNotificationRunnable = new Runnable(){
        @Override
        public void run() {
            cancelNotificationBackground();
        }
    };
    //add by vlife-liufan-2016-08-22-end
    
    /**
    * Method Description：dismiss the blur background
    */
    public void cancelNotificationBackground(){
        Log.e("liufan","cancelNotificationBackground-----00000---->");
        /*PRIZE-background alpha,bugid: 21404- liufan-2016-09-14-start*/
        if(!mNotificationPanel.isFullyCollapsed() && mState == StatusBarState.SHADE){
            Log.d(TAG,"not allow to cancelNotificationBackground");
            return ;
        }
        /*PRIZE-background alpha,bugid: 21404- liufan-2016-09-14-end*/
        if(isShowBlurBg){
            //add by vlife-liufan-2016-08-22-start
            mHandler.removeCallbacks(cancelNotificationRunnable);
            mHandler.postDelayed(cancelNotificationRunnable,300);
            //add by vlife-liufan-2016-08-22-end
            return;
        }
        isShowBg = false;
        Log.e("liufan","cancelNotificationBackground-----11111---->");
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
        Log.e("liufan","setNotificationBackground-----bitmap--0---->"+mWallPapaerBitmap);
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
            /*PRIZE-resolve the overlapping when add or delete the notification-liufan-2015-9-15-start*/
            mNotificationPanel.setBottomBlurBg(mWallPapaerBitmap);
        }
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
        //add by haokan-liufan-2016-10-11-start
        //modify by zookingsoft-vincent 20161116
		if(!NotificationPanelView.USE_VLIFE && !NotificationPanelView.USE_ZOOKING 
				&& isUseHaoKan()){
            if(mScreenView != null) bitmap = mScreenView.getScreenBitmap();
        }
        //add by haokan-liufan-2016-10-11-end
        //add by vlife-start-2016-07-30
        if(NotificationPanelView.USE_VLIFE && mState == StatusBarState.KEYGUARD){
            bitmap = mNotificationPanel.getVlifeKeyguardView().getVlifeKeyguardBackground();
        }
        //add by vlife-end-2016-07-30
        if (NotificationPanelView.USE_ZOOKING && mState == StatusBarState.KEYGUARD) {
        	bitmap = mNotificationPanel.getZookingKeyguardBg();
        }
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
    private Bitmap blur(Bitmap bitmap){
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
            canvas.drawColor(BACK_COLOR);
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
        float[] dims = {mDisplayMetrics.widthPixels , mDisplayMetrics.heightPixels };
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
            }
        }
        
    };

    /*PRIZE-create thread, delete recents data- liufan-2016-01-28-start*/
    public void deleteRecentsData(){
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG,"deleteRecentsData time start");
                RecentsTaskLoader loader = RecentsTaskLoader.getInstance();
                RecentsTaskLoadPlan plan = Recents.consumeInstanceLoadPlan();
                SystemServicesProxy ssp = loader.getSystemServicesProxy();
                RecentsConfiguration mConfig = RecentsConfiguration.reinitialize(mContext, ssp);
                if (plan == null) {
                    plan = loader.createLoadPlan(mContext);
                }
                // Start loading tasks according to the load plan
                if (!plan.hasTasks()) {
                    loader.preloadTasks(plan, mConfig.launchedFromHome);
                }
                RecentsTaskLoadPlan.Options loadOpts = new RecentsTaskLoadPlan.Options();
                loadOpts.runningTaskId = mConfig.launchedToTaskId;
                loadOpts.numVisibleTasks = mConfig.launchedNumVisibleTasks;
                loadOpts.numVisibleTaskThumbnails = mConfig.launchedNumVisibleThumbnails;
                loader.loadTasks(mContext, plan, loadOpts);
                ArrayList<TaskStack> stacks = plan.getAllTaskStacks();
                ArrayList<Task> lockList = new ArrayList<Task>();
                try{
                    ActivityManagerNative.getDefault().setDumpKill(true);
                } catch (RemoteException ex) {
                }

                final long used_before = RecentsActivity.getAvailMemory(mContext);
                final long total = RecentsActivity.totalMemoryB == 0 ? RecentsActivity.getTotalMemory() : RecentsActivity.totalMemoryB;
                for(TaskStack ts : stacks){
                    ArrayList<Task> tasks = new ArrayList<Task>();
                    tasks.addAll(ts.getTasks());
                    for(Task task : tasks){
                        task.isLock = TaskView.isLock(task,mContext);
                        if(task!=null){
                            if(!task.isLock){
                                ts.removeTask(task);
                                loader.deleteTaskData(task, false);
                                ssp.removeTask(task.key.id);
                                Log.d(TAG,"cls=="+task.key.getComponentNameKey().getComponentName().getClassName());
                            } else {
                                lockList.add(task);
                            }
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
        animateCollapsePanels();
        
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
        },COLLAPSE_PANELS_TIME_SPACE);
    }
    /*PRIZE-the listener of the base tile- liufan-2015-04-10-end*/

    private void clearAllNotifications() {

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
		/*PRIZE-show notification bg,bugid:33874- zhudaopeng-2017-05-16-start*/
         mNotificationPanel.dismissNotificationBgWhenHeadsUp(false);
         /*PRIZE-show notification bg,bugid:33874- zhudaopeng-2017-05-16-end*/
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

    private void startKeyguard() {
        KeyguardViewMediator keyguardViewMediator = getComponent(KeyguardViewMediator.class);
        mFingerprintUnlockController = new FingerprintUnlockController(mContext,
                mStatusBarWindowManager, mDozeScrimController, keyguardViewMediator,
                mScrimController, this);
        mStatusBarKeyguardViewManager = keyguardViewMediator.registerStatusBar(this,
                mStatusBarWindow, mStatusBarWindowManager, mScrimController,
                mFingerprintUnlockController);
        mKeyguardIndicationController.setStatusBarKeyguardViewManager(
                mStatusBarKeyguardViewManager);
        mFingerprintUnlockController.setStatusBarKeyguardViewManager(mStatusBarKeyguardViewManager);
        mKeyguardViewMediatorCallback = keyguardViewMediator.getViewMediatorCallback();
    }

    @Override
    protected View getStatusBarView() {
        return mStatusBarView;
    }

    public StatusBarWindowView getStatusBarWindow() {
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
        public void onClick(View v) {
			/*PRIZE-add for changesimcard reset dialog-lihuangyuan-2017-06-15-start*/
            /*if(PrizeOption.PRIZE_CHANGESIM_RESET_DIALOG){
                if(mContext != null) {
                    final boolean bPrizeResetMode = Settings.System.getInt(mContext.getContentResolver(),"prizeresetmode",0) == 1;
                    if (bPrizeResetMode) {
                        Log.i("prizeadb", "mRecentsClickListener, this is reset activity, not go to recent");
                        return;
                    }
                }
            }*/
            /*PRIZE-add for changesimcard reset dialog-lihuangyuan-2017-06-15-end*/			
			/*PRIZE-PowerExtendMode-disable Recents-wangxianzhen-2015-07-21-start*/
            if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
                Log.i(TAG,"PowerExtendMode mRecentsClickListener");
                return;
            }
            /*PRIZE-PowerExtendMode-disable Recents-wangxianzhen-2015-07-21-end*/
            awakenDreams();
            toggleRecentApps();
        }
    };

    private long mLastLockToAppLongPress;
    private View.OnLongClickListener mLongPressBackRecentsListener =
            new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
			/*PRIZE-PowerExtendMode-disable Recents-wangxianzhen-2015-07-21-start*/
            if (PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
                Log.i(TAG,"PowerExtendMode mLongPressBackRecentsListener");
                return true;
            }
            /*PRIZE-PowerExtendMode-disable Recents-wangxianzhen-2015-07-21-end*/
            handleLongPressBackRecents(v);
            return true;
        }
    };

    private final View.OnLongClickListener mLongPressHomeListener
            = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (shouldDisableNavbarGestures()) {
                return false;
            }
            mAssistManager.startAssist(new Bundle() /* args */);
            awakenDreams();
            if (mNavigationBarView != null) {
                mNavigationBarView.abortCurrentGesture();
            }
            return true;
        }
    };


    private final View.OnTouchListener mHomeActionListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
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

        mNavigationBarView.getRecentsButton().setOnClickListener(mRecentsClickListener);
        mNavigationBarView.getRecentsButton().setOnTouchListener(mRecentsPreloadOnTouchListener);
        mNavigationBarView.getRecentsButton().setLongClickable(true);
        mNavigationBarView.getRecentsButton().setOnLongClickListener(mLongPressBackRecentsListener);
        mNavigationBarView.getBackButton().setLongClickable(true);
        mNavigationBarView.getBackButton().setOnLongClickListener(mLongPressBackRecentsListener);
        mNavigationBarView.getHomeButton().setOnTouchListener(mHomeActionListener);
        mNavigationBarView.getHomeButton().setOnLongClickListener(mLongPressHomeListener);
        mAssistManager.onConfigurationChanged();
        /// M: add for multi window @{
        if(MultiWindowProxy.isSupported()){
            mNavigationBarView.getFloatButton().setOnClickListener(mFloatClickListener);
            if(mIsSplitModeEnable){
                mNavigationBarView.getFloatModeButton().setOnClickListener(mFloatModeClickListener);
                mNavigationBarView.getSplitModeButton().setOnClickListener(mSplitModeClickListener);
            }
            MultiWindowProxy.getInstance().setSystemUiCallback(new MWSystemUiCallback());
        }
        /// @}        
    }

    // For small-screen devices (read: phones) that lack hardware navigation buttons
    private void addNavigationBar() {
        if (DEBUG) Log.v(TAG, "addNavigationBar: about to add " + mNavigationBarView);
        if (mNavigationBarView == null) return;

        prepareNavigationBarView();
        /* add for dynamically changing Recents function. prize-linkh-20160723 */
        if (mSupportSettingRecentsAsMenu) {
            setRecentsAsMenu(mUseRecentsAsMenu, true);
        } //end...
        
        // add for mBack device. prize-linkh-20160805
        if (SUPPORT_NAV_BAR_FOR_MBACK_DEVICE && needHideNavBarFormBack()) {
            hideNavBarFormBack();
        } //END....
        
        // Recording this for conveniently updating nav bar view. prize-linkh-20160808
        if (mSupportHidingNavBar || SUPPORT_NAV_BAR_FOR_MBACK_DEVICE) {
            mNavBarLayoutParams = getNavigationBarLayoutParams();
        } //END...

        mWindowManager.addView(mNavigationBarView, getNavigationBarLayoutParams());
    }

    private void repositionNavigationBar() {
        if (mNavigationBarView == null || !mNavigationBarView.isAttachedToWindow()) return;

        prepareNavigationBarView();
        
        /* add for dynamically changing Recents function. prize-linkh-20160723 */
        if (mSupportSettingRecentsAsMenu) {
            setRecentsAsMenu(mUseRecentsAsMenu, true);
        } //end...
        
        //add for hiding nav bar. prize-linkh-20150714
        if(PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR) {
            registerHideNavBarClickListener();
        }
        
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
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);
        // this will allow the navbar to run in an overlay on devices that support this
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        }

        lp.setTitle("NavigationBar");
        lp.windowAnimations = 0;
        return lp;
    }

    public void addIcon(String slot, int index, int viewIndex, StatusBarIcon icon) {
        mIconController.addSystemIcon(slot, index, viewIndex, icon);
    }

    public void updateIcon(String slot, int index, int viewIndex,
            StatusBarIcon old, StatusBarIcon icon) {
        mIconController.updateSystemIcon(slot, index, viewIndex, old, icon);
    }

    public void removeIcon(String slot, int index, int viewIndex) {
        mIconController.removeSystemIcon(slot, index, viewIndex);
    }

    public UserHandle getCurrentUserHandle() {
        return new UserHandle(mCurrentUserId);
    }

    @Override
    public void addNotification(StatusBarNotification notification, RankingMap ranking,
            Entry oldEntry) {
        if (DEBUG) Log.d(TAG, "addNotification key=" + notification.getKey());
// MTK patch P7 content start 
        /// M: add for fix foreground service hide notification issue. @{
        if (notification != null && (
                notification.getPackageName().equalsIgnoreCase("com.mediatek.selfregister") ||
                notification.getPackageName().equalsIgnoreCase("com.mediatek.deviceregister")) &&
                (notification.getNotification().flags & Notification.FLAG_HIDE_NOTIFICATION) != 0) {
            return;
        }
        /// @}
// MTK patch P7 content end 
        Entry shadeEntry = createNotificationViews(notification);
        if (shadeEntry == null) {
            return;
        }
        boolean isHeadsUped = mUseHeadsUp && shouldInterrupt(shadeEntry);
        if (isHeadsUped) {
            mHeadsUpManager.showNotification(shadeEntry);
            // Mark as seen immediately
            setNotificationShown(notification);
        }

        if (!isHeadsUped && notification.getNotification().fullScreenIntent != null) {
            // Stop screensaver if the notification has a full-screen intent.
            // (like an incoming phone call)
            awakenDreams();

            // not immersive & a full-screen alert should be shown
            if (DEBUG) Log.d(TAG, "Notification has fullScreenIntent; sending fullScreenIntent");
            try {
                EventLog.writeEvent(EventLogTags.SYSUI_FULLSCREEN_NOTIFICATION,
                        notification.getKey());
                notification.getNotification().fullScreenIntent.send();
                shadeEntry.notifyFullScreenIntentLaunched();
                MetricsLogger.count(mContext, "note_fullscreen", 1);
            } catch (PendingIntent.CanceledException e) {
            }
        }
        addNotificationViews(shadeEntry, ranking);
        // Recalculate the position of the sliding windows and the titles.
        setAreThereNotifications();
        /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-7-8-start*/
        //isShowKeyguardChargingAnimation(true,true,true);

        /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-7-8-end*/
    }

    @Override
    protected void updateNotificationRanking(RankingMap ranking) {
        mNotificationData.updateRanking(ranking);
        updateNotifications();
    }

    @Override
    public void removeNotification(String key, RankingMap ranking) {
        boolean deferRemoval = false;
        if (mHeadsUpManager.isHeadsUp(key)) {
            deferRemoval = !mHeadsUpManager.removeNotification(key);
        }
        if (key.equals(mMediaNotificationKey)) {
            clearCurrentMediaNotification();
            updateMediaMetaData(true);
        }
        if (deferRemoval) {
            mLatestRankingMap = ranking;
            mHeadsUpEntriesToRemoveOnSwitch.add(mHeadsUpManager.getEntry(key));
            return;
        }
        StatusBarNotification old = removeNotificationViews(key, ranking);
        if (SPEW) Log.d(TAG, "removeNotification key=" + key + " old=" + old);
        removesendUnReadInfo(old);//add by zhouerlong 20160414
        if (old != null) {
            if (CLOSE_PANEL_WHEN_EMPTIED && !hasActiveNotifications()
                    && !mNotificationPanel.isTracking() && !mNotificationPanel.isQsExpanded()) {
                if (mState == StatusBarState.SHADE) {
                    animateCollapsePanels();
                } else if (mState == StatusBarState.SHADE_LOCKED) {
                    goToKeyguard();
                }
            }
        }
        setAreThereNotifications();
        /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-7-8-start*/
        //isShowKeyguardChargingAnimation(true,true,true);
        /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-7-8-end*/
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
				intent.setAppInstanceIndex(notification.appInstanceIndex);
				intent.putExtra(Intent.EXTRA_UNREAD_NUMBER, 0);
				mContext.sendBroadcast(intent);
			}
		}
	}

    //add by zhouerlong 20160414
    @Override
    protected void refreshLayout(int layoutDirection) {
        if (mNavigationBarView != null) {
            mNavigationBarView.setLayoutDirection(layoutDirection);
        }
    }

    private void updateNotificationShade() {
        Log.d(TAG,"updateNotificationShade()");
        if (mStackScroller == null) return;

        // Do not modify the notifications during collapse.
        Log.d(TAG,"updateNotificationShade() isCollapsing = "+isCollapsing());
        if (isCollapsing()) {
            Log.d(TAG,"updateNotificationShade(1)");
            boolean hasHeadsUpNotification = mHeadsUpManager.hasHeadsUpNotification();
            if(hasHeadsUpNotification){
                animateCollapsePanels();
                Log.d(TAG,"updateNotificationShade(3) hasHeadsUpNotification = "+hasHeadsUpNotification);
            } else {
                addPostCollapseAction(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG,"updateNotificationShade(4)");
                        updateNotificationShade();
                    }
                });
                return;
            }
        }
        Log.d(TAG,"updateNotificationShade(5)");
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
            ent.row.setSensitive(sensitive);
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

        ArrayList<View> toRemove = new ArrayList<>();
        for (int i=0; i< mStackScroller.getChildCount(); i++) {
            View child = mStackScroller.getChildAt(i);
            if (!toShow.contains(child) && child instanceof ExpandableNotificationRow) {
                toRemove.add(child);
            }
        }

        for (View remove : toRemove) {
            mStackScroller.removeView(remove);
        }
        for (int i=0; i<toShow.size(); i++) {
            View v = toShow.get(i);
            if (v.getParent() == null) {
                makeViewAlpha(v);
                mStackScroller.addView(v);
            }
        }

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

        // lets handle the child notifications now
        updateNotificationShadeForChildren();

        // clear the map again for the next usage
        mTmpChildOrderMap.clear();

        updateRowStates();
        updateSpeedbump();
        updateClearAll();
        updateEmptyShadeView();

        updateQsExpansionEnabled();
        mShadeUpdates.check();
    }
    
    public void makeViewAlpha(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
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

    private void updateNotificationShadeForChildren() {
        ArrayList<ExpandableNotificationRow> toRemove = new ArrayList<>();
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

            // lets first remove all undesired children
            if (children != null) {
                toRemove.clear();
                for (ExpandableNotificationRow childRow : children) {
                    if (orderedChildren == null || !orderedChildren.contains(childRow)) {
                        toRemove.add(childRow);
                    }
                }
                for (ExpandableNotificationRow remove : toRemove) {
                    parent.removeChildNotification(remove);
                    mStackScroller.notifyGroupChildRemoved(remove);
                }
            }

            // We now add all the children which are not in there already
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

    private boolean packageHasVisibilityOverride(String key) {
        return mNotificationData.getVisibilityOverride(key)
                != NotificationListenerService.Ranking.VISIBILITY_NO_OVERRIDE;
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
        ArrayList<Entry> activeNotifications = mNotificationData.getActiveNotifications();
        final int N = activeNotifications.size();
        for (int i = 0; i < N; i++) {
            Entry entry = activeNotifications.get(i);
            boolean isChild = !isTopLevelChild(entry);
            if (isChild) {
                continue;
            }
            if (entry.row.getVisibility() != View.GONE &&
                    mNotificationData.isAmbient(entry.key)) {
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
            // if(NotificationStackScrollLayout.isChildChanged){
            //    NotificationStackScrollLayout.isChildChanged = false;
            // }
            //updateNotificationAgain();
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

    @Override
    protected void updateRowStates() {
        super.updateRowStates();
        mNotificationPanel.notifyVisibleChildrenChanged();
    }

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
        updateMediaMetaData(metaDataChanged);
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
        //modify by vlife start,vlife keyguadview-2016-07-30
        if(NotificationPanelView.USE_VLIFE || NotificationPanelView.USE_ZOOKING){
            return ;
        }
        //modify by vlife end-2016-07-30
        if(mState == StatusBarState.SHADE || (mState == StatusBarState.SHADE_LOCKED && !BLUR_BG_CONTROL) || !NotificationPanelView.HaokanShow){
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
        int N = mNotificationData.getActiveNotifications().size();
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
        recycleBlurWallpaper();
		//update by haokan-liufan-2016-10-11-start
		if(!NotificationPanelView.USE_VLIFE && !NotificationPanelView.USE_ZOOKING && isUseHaoKan()){
            Bitmap bitmap = null;
            if(mScreenView != null) bitmap = mScreenView.getScreenBitmap();
            if(bitmap!=null){
                BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), blur(bitmap));
                /**PRIZE-haokanscreen iteration one-liufan-2016-06-23-start */
                bd.setAlpha(180);
                /**PRIZE-haokanscreen iteration one-liufan-2016-06-23-end */
                mBlurBack.setBackground(bd);
            }
            return ;
		}
		//update by haokan-liufan-2016-10-11-end
        if(value == 1){
            setBlurBackBgBySystemWallpaper();
        }else if(value == 2){
            setBlurBackBgByOtherWallpaper();
        }
    }

    public void setBlurBackVisibility(int visible){
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
     * Refresh or remove lockscreen artwork from media metadata.
     */
    public void updateMediaMetaData(boolean metaDataChanged) {
        if (!SHOW_LOCKSCREEN_MEDIA_ARTWORK) return;

        if (mBackdrop == null) return; // called too early

        if (mLaunchTransitionFadingAway) {
            mBackdrop.setVisibility(View.INVISIBLE);
            return;
        }

        if (DEBUG_MEDIA) {
            Log.v(TAG, "DEBUG_MEDIA: updating album art for notification " + mMediaNotificationKey
                    + " metadata=" + mMediaMetadata
                    + " metaDataChanged=" + metaDataChanged
                    + " state=" + mState);
        }

        Bitmap artworkBitmap = null;
        /*if (mMediaMetadata != null) {
            artworkBitmap = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
            if (artworkBitmap == null) {
                artworkBitmap = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
                // might still be null
            }
        }*/
        /*PRIZE set lockscreen wallpaper liyao-2015-07-22-start*/
        if(artworkBitmap == null && SUPPORT_KEYGUARD_WALLPAPER && !NotificationPanelView.USE_VLIFE && !NotificationPanelView.USE_ZOOKING){//modify by vlife start,vlife keyguadview-2016-07-30
            String dataPath = mContext.getFilesDir().getPath();
            File f = new File(dataPath, "jrlkp.png"); // 
            if (f.exists()) {
                artworkBitmap = convertToBitmap(f.getPath());
            } else {
					/*prize-public-standard:Changed lock screen-liuweiquan-20151210-start*/	    
					if(PrizeOption.PRIZE_CHANGED_WALLPAPER&&bChangedWallpaperIsOpen&&!bIntoSuperSavingPower){
						String kgPath = sChangedWallpaperPath;
                        Log.d("kgPath","kgPath----------->"+kgPath);
						if(!TextUtils.isEmpty(kgPath) && !"-1".equals(kgPath)){
							f = new File(kgPath); // changed keyguard wallpaper
							if (f.exists()) {
								artworkBitmap = convertToBitmap(f.getPath());				
							} 
						}
                        if(artworkBitmap == null){
                            File file = new File("/system/lockscreen.png");
                            if(file.exists()) {
                                artworkBitmap = convertToBitmap("/system/lockscreen.png");
                            }
                            else
                                Log.d(TAG,"custom KeyguardWallpaper file not exists");
                        }
					/*prize-public-standard:Changed lock screen-liuweiquan-20151210-end*/
					}else{									
						f = new File(dataPath, "KeyguardWallpaper.png"); // the wallpaper setted by user
						if (f.exists()) {
							artworkBitmap = convertToBitmap(f.getPath());
						} 
						else {
							String strPath = Settings.System.getString(mContext.getContentResolver(),KEYGUARD_WALLPAPER_URI);					
							Log.d(TAG,"changeKeyguardWallpaper strPath :"+strPath);
							if(!TextUtils.isEmpty(strPath) && !"-1".equals(strPath)){
								File file = new File(strPath);
								if(file.exists()){									
									Log.d(TAG,"changeKeyguardWallpaper file.exists");
									artworkBitmap = convertToBitmap(strPath);
								}
							}
							// @prize fanjunchen 2015-09-07{
							else {
								File file = new File("/system/lockscreen.png");
								if(file.exists()) {
									artworkBitmap = convertToBitmap("/system/lockscreen.png");
								}
								else
									Log.d(TAG,"custom KeyguardWallpaper file not exists");
							}
							// @prize end }
						}
						
					}
				
            	
            }
        }
        /*PRIZE set the lockscreen wallpaper liyao-2015-07-22-end*/
        final boolean hasArtwork = artworkBitmap != null;

        if ((hasArtwork || DEBUG_MEDIA_FAKE_ARTWORK)
                && (mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED)) {
            // time to show some art!
            if (mBackdrop.getVisibility() != View.VISIBLE) {
                mBackdrop.setVisibility(View.VISIBLE);
                mBackdrop.animate().alpha(1f);
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
                            mBackdropBack.getDrawable().getConstantState().newDrawable().mutate();
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
                	mBackdropBack.setScaleType(ScaleType.CENTER_CROP);
                    mBackdropBack.setImageBitmap(artworkBitmap);
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
                if (DEBUG_MEDIA) {
                    Log.v(TAG, "DEBUG_MEDIA: Fading out album artwork");
                }
                mBackdrop.animate()
                        // Never let the alpha become zero - otherwise the RenderNode
                        // won't draw anything and uninitialized memory will show through
                        // if mScrimSrcModeEnabled. Note that 0.001 is rounded down to 0 in libhwui.
                        .alpha(0.002f)
                        .setInterpolator(mBackdropInterpolator)
                        .setDuration(300)
                        .setStartDelay(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                mBackdrop.setVisibility(View.GONE);
                                mBackdropFront.animate().cancel();
                                mBackdropBack.animate().cancel();
                                mHandler.post(mHideBackdropFront);
                            }
                        });
                if (mKeyguardFadingAway) {
                    mBackdrop.animate()

                            // Make it disappear faster, as the focus should be on the activity behind.
                            .setDuration(mKeyguardFadingAwayDuration / 2)
                            .setStartDelay(mKeyguardFadingAwayDelay)
                            .setInterpolator(mLinearInterpolator)
                            .start();
                }
            }
        }
        /*PRIZE-blur background-liufan-2015-09-04-start*/
        isShowBlurBgWhenLockscreen(false);
        /*PRIZE-blur background-liufan-2015-09-04-end*/
    }

    private int adjustDisableFlags(int state) {
        if (!mLaunchTransitionFadingAway && !mKeyguardFadingAway
                && (mExpandedVisible || mBouncerShowing || mWaitingForKeyguardExit)) {
            state |= StatusBarManager.DISABLE_NOTIFICATION_ICONS;
            state |= StatusBarManager.DISABLE_SYSTEM_INFO;
        }
        return state;
    }

    /**
     * State is one or more of the DISABLE constants from StatusBarManager.
     */
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
                mStatusBarPlmnPlugin.setPlmnVisibility(View.GONE);
            } else {
                mIconController.showSystemIconArea(animate);
                mStatusBarPlmnPlugin.setPlmnVisibility(View.VISIBLE);
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

    protected void updateHeadsUp(String key, Entry entry, boolean shouldInterrupt,
            boolean alertAgain) {
        final boolean wasHeadsUp = isHeadsUp(key);
        if (wasHeadsUp) {
            /*PRIZE-delete from removeList, bugid: 28454,28579-liufan-2017-02-23-start*/
            if(mHeadsUpEntriesToRemoveOnSwitch!=null && mHeadsUpEntriesToRemoveOnSwitch.contains(entry)){
                mHeadsUpEntriesToRemoveOnSwitch.remove(entry);
            }
            /*PRIZE-delete from removeList, bugid: 28454,28579-liufan-2017-02-23-end*/
            if (!shouldInterrupt) {
                // We don't want this to be interrupting anymore, lets remove it
                mHeadsUpManager.removeNotification(key);
            } else {
                mHeadsUpManager.updateNotification(entry, alertAgain);
            }
        /// M: Fix ALPS02328815, for update heads up, also needs mUseHeadsUp is true.
        } else if (mUseHeadsUp && shouldInterrupt && alertAgain) {
            // This notification was updated to be a heads-up, show it!
            mHeadsUpManager.showNotification(entry);
        }
    }

    protected void setHeadsUpUser(int newUserId) {
        if (mHeadsUpManager != null) {
            mHeadsUpManager.setUser(newUserId);
        }
    }

    public boolean isHeadsUp(String key) {
        return mHeadsUpManager.isHeadsUp(key);
    }

    protected boolean isSnoozedPackage(StatusBarNotification sbn) {
        return mHeadsUpManager.isSnoozed(sbn.getPackageName());
    }

    public boolean isKeyguardCurrentlySecure() {
        return !mUnlockMethodCache.canSkipBouncer();
    }

    public void setPanelExpanded(boolean isExpanded) {
        mStatusBarWindowManager.setPanelExpanded(isExpanded);
    }

    /**
     * All changes to the status bar and notifications funnel through here and are batched.
     */
    private class H extends BaseStatusBar.H {
        public void handleMessage(Message m) {
            super.handleMessage(m);
            switch (m.what) {
                case MSG_OPEN_NOTIFICATION_PANEL:
                    animateExpandNotificationsPanel();
                    break;
                case MSG_OPEN_SETTINGS_PANEL:
                    animateExpandSettingsPanel();
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
        TreeSet<HeadsUpManager.HeadsUpEntry> entries = mHeadsUpManager.getSortedEntries();
        for (HeadsUpManager.HeadsUpEntry entry : entries) {
            final StatusBarNotification sbn = entry.entry.notification;
            final Notification notification = sbn.getNotification();
            if (notification.fullScreenIntent != null) {
                if (DEBUG) {
                    Log.d(TAG, "converting a heads up to fullScreen");
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
        if (mNavigationBarView != null)
            mNavigationBarView.setSlippery(true);

        /// M: Support "Operator plugin - Customize Carrier Label for PLMN". @{
        updateCarrierLabelVisibility(true);
        /// M: Support "Operator plugin - Customize Carrier Label for PLMN". @}

        // Expand the window to encompass the full screen in anticipation of the drag.
        // This is only possible to do atomically because the status bar is at the top of the screen!
        mStatusBarWindowManager.setPanelVisible(true);

        visibilityChanged(true);
        mWaitingForKeyguardExit = false;
        disable(mDisabledUnmodified1, mDisabledUnmodified2, !force /* animate */);
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
            mStatusBarView.collapseAllPanels(anim /* animate */, delayed, speedUpFactor);
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

    public void animateCollapsePanels(int flags) {
        animateCollapsePanels(flags, false /* force */, false /* delayed */,
                1.0f /* speedUpFactor */);
    }

    public void animateCollapsePanels(int flags, boolean force) {
        animateCollapsePanels(flags, force, false /* delayed */, 1.0f /* speedUpFactor */);
    }

    public void animateCollapsePanels(int flags, boolean force, boolean delayed) {
        animateCollapsePanels(flags, force, delayed, 1.0f /* speedUpFactor */);
    }

    public void animateCollapsePanels(int flags, boolean force, boolean delayed,
            float speedUpFactor) {
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

            /*PRIZE-for phone top notification bg alpha- liufan-2016-09-20-start*/
            if(!isPanelFullyCollapsed() && mState == StatusBarState.SHADE) {
                isCollapseAllPanelsAnim = true;
                Log.d(TAG,"isCollapseAllPanelsAnim turn true");
            }
            /*PRIZE-for phone top notification bg alpha- liufan-2016-09-20-end*/
            mStatusBarWindow.cancelExpandHelper();
			/*PRIZE-liyu-for probability loss lock screen-2016-11-28-start*/
           // mStatusBarView.collapseAllPanels(true /* animate */, delayed, speedUpFactor);
		   mStatusBarView.collapseAllPanels(false /* animate */, delayed, speedUpFactor);
		   /*PRIZE-liyu-for probability loss lock screen-2016-11-28-end*/
		   
		   /*PRIZE-add for bugid: 34046-zhudaopeng-2017-05-24-Start*/
           mHandler.postDelayed(new Runnable() {
               @Override
               public void run() {
                   mNotificationPanel.resetNotificationScrimBg();
               }
           },150);
           /*PRIZE-add for bugid: 34046-zhudaopeng-2017-05-24-End*/
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
            mStatusBarView.collapseAllPanels(true, false /* delayed */, 1.0f /* speedUpFactor */);
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

    Animator mScrollViewAnim, mClearButtonAnim;

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
            animateExpandSettingsPanel();
            return ;
        }
        /*PRIZE-pull to quick setting when there is no notification-liufan-2015-09-16-end*/
        mNotificationPanel.expand();

        if (false) postStartTracing();
    }

    @Override
    public void animateExpandSettingsPanel() {
        if (SPEW) Log.d(TAG, "animateExpand: mExpandedVisible=" + mExpandedVisible);
        if (!panelsEnabled()) {
            return;
        }

        // Settings are not available in setup
        if (!mUserSetup) return;

        mNotificationPanel.expandWithQs();
        /*PRIZE-delay 50ms to OpenQs- liufan-2015-04-08-start*/
        mHandler.postDelayed(new Runnable(){
            @Override
                public void run() {
                     mNotificationPanel.openQs();
                }
            },50);
        /*PRIZE-delay 50ms to OpenQs- liufan-2015-04-08-end*/
        if (false) postStartTracing();
    }

    public void animateCollapseQuickSettings() {
        if (mState == StatusBarState.SHADE) {
            mStatusBarView.collapseAllPanels(true, false /* delayed */, 1.0f /* speedUpFactor */);
        }
    }

    void makeExpandedInvisible() {
        if (SPEW) Log.d(TAG, "makeExpandedInvisible: mExpandedVisible=" + mExpandedVisible
                + " mExpandedVisible=" + mExpandedVisible);

        if (!mExpandedVisible || mStatusBarWindow == null) {
            return;
        }

        // Ensure the panel is fully collapsed (just in case; bug 6765842, 7260868)
        mStatusBarView.collapseAllPanels(/*animate=*/ false, false /* delayed*/,
                1.0f /* speedUpFactor */);

        mNotificationPanel.closeQs();

        mExpandedVisible = false;
        if (mNavigationBarView != null)
            mNavigationBarView.setSlippery(false);
        visibilityChanged(false);

        // Shrink the window to the size of the status bar only
        mStatusBarWindowManager.setPanelVisible(false);
        mStatusBarWindowManager.setForceStatusBarVisible(false);

        // Close any "App info" popups that might have snuck on-screen
        dismissPopups();

        runPostCollapseRunnables();
        setInteracting(StatusBarManager.WINDOW_STATUS_BAR, false);
        showBouncer();
        disable(mDisabledUnmodified1, mDisabledUnmodified2, true /* animate */);

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
                mStatusBarView.collapseAllPanels(false /* animate */, false /* delayed */,
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
    public void setSystemUiVisibility(int vis, int mask) {
        final int oldVal = mSystemUiVisibility;
        final int newVal = (oldVal&~mask) | (vis&mask);
        final int diff = newVal ^ oldVal;
        if (DEBUG) Log.d(TAG, String.format(
                "setSystemUiVisibility vis=%s mask=%s oldVal=%s newVal=%s diff=%s",
                Integer.toHexString(vis), Integer.toHexString(mask),
                Integer.toHexString(oldVal), Integer.toHexString(newVal),
                Integer.toHexString(diff)));
        if (diff != 0) {
            // we never set the recents bit via this method, so save the prior state to prevent
            // clobbering the bit below
            final boolean wasRecentsVisible = (mSystemUiVisibility & View.RECENT_APPS_VISIBLE) > 0;

            mSystemUiVisibility = newVal;

            // update low profile
            if ((diff & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                final boolean lightsOut = (vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0;
                if (lightsOut) {
                    animateCollapsePanels();
                }

                setAreThereNotifications();
            }

            // ready to unhide
            if ((vis & View.STATUS_BAR_UNHIDE) != 0) {
                mSystemUiVisibility &= ~View.STATUS_BAR_UNHIDE;
                mNoAnimationOnNextBarModeChange = true;
            }

            // update status bar mode
            final int sbMode = computeBarMode(oldVal, newVal, mStatusBarView.getBarTransitions(),
                    View.STATUS_BAR_TRANSIENT, View.STATUS_BAR_TRANSLUCENT);

            // update navigation bar mode
            final int nbMode = mNavigationBarView == null ? -1 : computeBarMode(
                    oldVal, newVal, mNavigationBarView.getBarTransitions(),
                    View.NAVIGATION_BAR_TRANSIENT, View.NAVIGATION_BAR_TRANSLUCENT);
            final boolean sbModeChanged = sbMode != -1;
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
                /// M: add for multi window @{
                if(!((MultiWindowProxy.isSupported()) && isFloatPanelOpened())) {
                /// @}
                    checkBarModes();
                /// M: add for multi window @{
                }
                /// @}
            }
            if (sbModeChanged || nbModeChanged) {
                // update transient bar autohide
                if (mStatusBarMode == MODE_SEMI_TRANSPARENT || mNavigationBarMode == MODE_SEMI_TRANSPARENT) {
                    scheduleAutohide();
                } else {
                    cancelAutohide();
                }
            }

            if ((vis & View.NAVIGATION_BAR_UNHIDE) != 0) {
                mSystemUiVisibility &= ~View.NAVIGATION_BAR_UNHIDE;
            }

            if ((diff & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0 || sbModeChanged) {
                boolean isTransparentBar = (mStatusBarMode == MODE_TRANSPARENT
                        || mStatusBarMode == MODE_LIGHTS_OUT_TRANSPARENT);
                boolean allowLight = isTransparentBar && !mBatteryController.isPowerSave();
                boolean light = (vis & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0;

                mIconController.setIconsDark(allowLight && light);
            }
            // restore the recents bit
            if (wasRecentsVisible) {
                mSystemUiVisibility |= View.RECENT_APPS_VISIBLE;
            }

            // send updated sysui visibility to window manager
            notifyUiVisibilityChanged(mSystemUiVisibility);
        }
    }

    private int computeBarMode(int oldVis, int newVis, BarTransitions transitions,
            int transientFlag, int translucentFlag) {
        final int oldMode = barMode(oldVis, transientFlag, translucentFlag);
        final int newMode = barMode(newVis, transientFlag, translucentFlag);
        if (oldMode == newMode) {
            return -1; // no mode change
        }
        return newMode;
    }

    private int barMode(int vis, int transientFlag, int translucentFlag) {
        int lightsOutTransparent = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_TRANSPARENT;
        return (vis & transientFlag) != 0 ? MODE_SEMI_TRANSPARENT
                : (vis & translucentFlag) != 0 ? MODE_TRANSLUCENT
                : (vis & lightsOutTransparent) == lightsOutTransparent ? MODE_LIGHTS_OUT_TRANSPARENT
                : (vis & View.SYSTEM_UI_TRANSPARENT) != 0 ? MODE_TRANSPARENT
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
        final boolean anim = !noAnimation && mDeviceInteractive
                && windowState != WINDOW_STATE_HIDDEN && !powerSave;
        if (powerSave && getBarState() == StatusBarState.SHADE) {
            mode = MODE_WARNING;
        }
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
        /// M: add for multi window @{
        if(!((MultiWindowProxy.isSupported()) && isFloatPanelOpened())) {
        /// @}
            checkBarModes();
        /// M: add for multi window @{
        }
        /// @}
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
                ) {
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
            setSystemUiVisibility(0, View.SYSTEM_UI_FLAG_LOW_PROFILE);
        } else {
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE, View.SYSTEM_UI_FLAG_LOW_PROFILE);
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

    public void topAppWindowChanged(boolean showMenu) {
        if (DEBUG) {
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
        if (mAssistManager != null) {
            mAssistManager.dump(fd, pw, args);
        }
        if (mSecurityController != null) {
            mSecurityController.dump(fd, pw, args);
        }
        if (mHeadsUpManager != null) {
            mHeadsUpManager.dump(fd, pw, args);
        } else {
            pw.println("  mHeadsUpManager: null");
        }
        if (KeyguardUpdateMonitor.getInstance(mContext) != null) {
            KeyguardUpdateMonitor.getInstance(mContext).dump(fd, pw, args);
        }

        pw.println("SharedPreferences:");
        for (Map.Entry<String, ?> entry : Prefs.getAll(mContext).entrySet()) {
            pw.print("  "); pw.print(entry.getKey()); pw.print("="); pw.println(entry.getValue());
        }
    }

    private String hunStateToString(Entry entry) {
        if (entry == null) return "null";
        if (entry.notification == null) return "corrupt";
        return entry.notification.getPackageName();
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
        mStatusBarWindowManager.add(mStatusBarWindow, getStatusBarHeight());
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
            public void run() {
                mAssistManager.hideAssist();
                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                int result = ActivityManager.START_CANCELED;
                try {
                    result = ActivityManagerNative.getDefault().startActivityAsUser(
                            null, mContext.getBasePackageName(),
                            intent,
                            intent.resolveTypeIfNeeded(mContext.getContentResolver()),
                            null, null, 0, Intent.FLAG_ACTIVITY_NEW_TASK, null, null,
                            UserHandle.CURRENT.getIdentifier());
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
                afterKeyguardGone);
    }

    public void executeRunnableDismissingKeyguard(final Runnable runnable,
            final Runnable cancelAction,
            final boolean dismissShade,
            final boolean afterKeyguardGone) {
        final boolean keyguardShowing = mStatusBarKeyguardViewManager.isShowing();
        dismissKeyguardThenExecute(new OnDismissAction() {
            @Override
            public boolean onDismiss() {
                AsyncTask.execute(new Runnable() {
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
                return true;
            }
        }, cancelAction, afterKeyguardGone);
    }
    // @prize fanjunchen 2015-09-01{
    public void setBackdropGone() {
        if (mBackdrop != null && View.VISIBLE == mBackdrop.getVisibility())
            mBackdrop.setVisibility(View.GONE);
    }
    // @prize}
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.v(TAG, "onReceive: " + intent);
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
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
            //add for controlling nav bar. prize-linkh-20150714
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
            }//end...			
        }
    };

    private BroadcastReceiver mDemoReceiver = new BroadcastReceiver() {
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
                    updateMediaMetaData(true);
                }
            }
        }
    };

    private void resetUserExpandedStates() {
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
        super.onConfigurationChanged(newConfig); // calls refreshLayout

        if (DEBUG) {
            Log.v(TAG, "configuration changed: " + mContext.getResources().getConfiguration());
        }
        updateDisplaySize(); // populates mDisplayMetrics

        updateResources();
        repositionNavigationBar();
        updateRowStates();
        mIconController.updateResources();
        mScreenPinningRequest.onConfigurationChanged();
        mNetworkController.onConfigurationChanged();
        /// M: add for multi window @{
        if(MultiWindowProxy.isSupported()) {
            updateFloatButtonIconOnly(isFloatPanelOpened());
            updateFloatModeButton(!mIsSplitModeOn);
            updateSpilitModeButton(mIsSplitModeOn);
        }
        /// @}
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
        mAssistManager.onUserSwitched(newUserId);
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

        mNaturalBarHeight = res.getDimensionPixelSize(
                com.android.internal.R.dimen.status_bar_height);

        mRowMinHeight =  res.getDimensionPixelSize(R.dimen.notification_min_height);
        mRowMaxHeight =  res.getDimensionPixelSize(R.dimen.notification_max_height);

        mKeyguardMaxNotificationCount = res.getInteger(R.integer.keyguard_max_notification_count);

        if (DEBUG) Log.v(TAG, "updateResources");
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
        public void run() {
            vibrate();
            SystemClock.sleep(250);
            Log.d(TAG, "startTracing");
            android.os.Debug.startMethodTracing("/data/statusbar-traces/trace");
            mHandler.postDelayed(mStopTracing, 10000);
        }
    };

    Runnable mStopTracing = new Runnable() {
        public void run() {
            android.os.Debug.stopMethodTracing();
            Log.d(TAG, "stopTracing");
            vibrate();
        }
    };

    @Override
    public boolean shouldDisableNavbarGestures() {
        /* force navbar to have the same function of the virtual keys. prize-linkh-20151123 */
        if(PrizeOption.PRIZE_SUPPORT_SETTING_RECENTS_AS_MENU) {
            if (mUseRecentsAsMenu) {
                return true;
            }
        } //end...
	
        return !isDeviceProvisioned() || (mDisabled1 & StatusBarManager.DISABLE_SEARCH) != 0;
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
        mContext.unregisterReceiver(mBroadcastReceiver);
        /*PRIZE-cancel listen the battery change-liufan-2015-7-8-start*/
        mContext.unregisterReceiver(mBatteryTracker);
        /*PRIZE-cancel listen the battery change-liufan-2015-7-8-end*/
        /*PRIZE-cancel listen the launcher clean broadcast-liufan-2016-12-28-start*/
        mContext.unregisterReceiver(mLauncherCleanReceiver);
        /*PRIZE-cancel listen the launcher clean broadcast-liufan-2016-12-28-end*/

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
        if(SUPPORT_KEYGUARD_WALLPAPER){
            mKeyguardWallpaperObserver.stopObserving();
        }
        mContext.unregisterReceiver(mDemoReceiver);
		
        //start................. prize-linkh-20150724
        //add for navbar style.
        if(mSupportNavbarStyle) {
            mContext.getContentResolver().unregisterContentObserver(mNavbarStyleObserver);
        }
        //add for always showing nav bar.
        if(mSupportHidingNavBar) {
            mContext.getContentResolver().unregisterContentObserver(mAlwaysShowNavBarObserver);
        }
        // add for dynamically changing Recents function. 
        if (mSupportSettingRecentsAsMenu) {
            mContext.getContentResolver().unregisterContentObserver(mUseRecentsAsMenuObserver);
        }
        // add for mBack device. prize-linkh-20160805
        if (SUPPORT_NAV_BAR_FOR_MBACK_DEVICE) {
            mContext.getContentResolver().unregisterContentObserver(mNavBarStateFormBackObserver);
        }        
        //end..............

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
                
                /*PRIZE-kill the app who call KeyguardManager.disableKeyguard(),bugid: 26842-liufan-2016-12-20-start*/
                boolean isCharge = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING || batteryStatus == BatteryManager.BATTERY_STATUS_FULL;
                if(!isCharge){
                    //kill shouji lianjie zhushou
                    ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                    for(String pkg : killPkg){
                        Log.d(TAG,"kill " + pkg + " when not charge");
                        activityManager.forceStopPackage(pkg);
                    }
                }
                /*PRIZE-kill the app who call KeyguardManager.disableKeyguard(),bugid: 26842-liufan-2016-12-20-end*/
            }
        }
    };

    /*PRIZE-who call KeyguardManager.disableKeyguard(),bugid: 26842-liufan-2016-12-20-start*/
    private String[] killPkg = new String[]{"com.skymobi.suit"};
    /*PRIZE-who call KeyguardManager.disableKeyguard(),bugid: 26842-liufan-2016-12-20-end*/
    
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
            //add by vlife-start-2016-07-30
            if(NotificationPanelView.USE_VLIFE){
                bitmap = mNotificationPanel.getVlifeKeyguardView().getVlifeKeyguardBackground();
            } else if (NotificationPanelView.USE_ZOOKING) {
				bitmap = mNotificationPanel.getZookingKeyguardBg();
            } else if(isUseHaoKan()){//update by haokan-liufan-2016-10-11
				if(NotificationPanelView.HaokanShow){
					if(mScreenView != null) bitmap = mScreenView.getScreenBitmap();
				}else{
				    bitmap = screenshot();
				}
			}
            //add by vlife-end-2016-07-30
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
            dispatchDemoCommandToView(command, args, R.id.battery);
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
    protected boolean isPanelFullyCollapsed() {
        return mNotificationPanel.isFullyCollapsed();
    }

    public void showKeyguard() {
        if (mLaunchTransitionFadingAway) {
            mNotificationPanel.animate().cancel();
            onLaunchTransitionFadingEnded();
        }
        mHandler.removeMessages(MSG_LAUNCH_TRANSITION_TIMEOUT);
        /*PRIZE-add,set alpha to 0 before show-liufan-2016-06-03-start*/
        if(isHideKeyguard){
            isHideKeyguard = false;
            isShowKeyguard = true;
            mBlurBack.setAlpha(0f);
            mBackdropBack.setAlpha(0f);
        }
        /*PRIZE-add,set alpha to 0 before show-liufan-2016-06-03-end*/
        setBarState(StatusBarState.KEYGUARD);
		
		//add by haokan-liufan-2016-10-09-start
		if(mScreenView==null){
			mScreenView = (ScreenView) mStatusBarWindow.findViewById(R.id.screenview);
			if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW){
				mScreenView.setVisibility(View.VISIBLE);
				mScreenView.setEntryApp();
				mScreenView.startActivityBySystemUI(null);
			} else {
				mScreenView.setVisibility(View.GONE);
			}
			//holder.setVisibility(View.GONE);
			mScreenView.setNotificationPanel(mNotificationPanel);
		}
		boolean isUseHaoKan = Settings.System.getInt(mContext.getContentResolver(),
				Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH, 0) == 1 ? true : false;
		if(isUseHaoKan != IS_USE_HAOKAN){
			IS_USE_HAOKAN = isUseHaoKan;
			if(NotificationPanelView.USE_VLIFE || NotificationPanelView.USE_ZOOKING){ //add by zookingsoft 20170112
				IS_USE_HAOKAN = false;
			}
			refreshHaoKanState();
		}
		//add by haokan-liufan-2016-10-09-end
		
        updateKeyguardState(false /* goingToFullShade */, false /* fromShadeLocked */);
        if (!mDeviceInteractive) {

            // If the screen is off already, we need to disable touch events because these might
            // collapse the panel after we expanded it, and thus we would end up with a blank
            // Keyguard.
            mNotificationPanel.setTouchDisabled(true);
        }
        instantExpandNotificationsPanel();
        mLeaveOpenOnKeyguardHide = false;
        if (mDraggedDownRow != null) {
            mDraggedDownRow.setUserLocked(false);
            mDraggedDownRow.notifyHeightChanged(false  /* needsAnimation */);
            mDraggedDownRow = null;
        }
        /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-7-8-start*/
        //isShowKeyguardChargingAnimation(true,true,true);
        /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-7-8-end*/
        mAssistManager.onLockscreenShown();
    }

    /*PRIZE-add for bugid: 33832-zhudaopeng-2017-05-19-Start*/
    public void overTouchEvent(){
        mNotificationPanel.overTouchEvent();
    }
    /*PRIZE-add for bugid: 33832-zhudaopeng-2017-05-19-End*/
    
    /*PRIZE-add for bugid: 23195-liufan-2016-10-17-start*/
    public void refreshBlurBgWhenLockscreen(){
        if((PrizeOption.PRIZE_CHANGED_WALLPAPER || PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW)&&!bChangedWallpaperIsOpen&&!bIntoSuperSavingPower){
            isShowBlurBgWhenLockscreen(false);
        }
    }
    /*PRIZE-add for bugid: 23195-liufan-2016-10-17-end*/
    
    //modify by vlife start,show VlifeKeyguardView-2016-07-30
	public void showVlifeKeyguardView(){
        if(NotificationPanelView.USE_VLIFE){
            mNotificationPanel.getVlifeKeyguardView().setVisibility(View.VISIBLE);
        }
	}
    //modify by vlife end,show VlifeKeyguardView-2016-07-30
	//modify by Zookingsoft start,show VlifeKeyguardView-2016-07-30
		public void showZookingKeyguardView(){
	        if(NotificationPanelView.USE_ZOOKING){
	            mNotificationPanel.getZookingKeyguardView().setVisibility(View.VISIBLE);
	        }
		}
	    //modify by Zookingsoft end,show VlifeKeyguardView-2016-07-30

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
    /*PRIZE-add,show blur layout when after show keyguard-liufan-2016-06-03-end*/

	public ImageView getBackdropBack(){
		return mBackdropBack;
	}

    private void onLaunchTransitionFadingEnded() {
        mNotificationPanel.setAlpha(1.0f);
        runLaunchTransitionEndRunnable();
        mLaunchTransitionFadingAway = false;
        mScrimController.forceHideScrims(false /* hide */);
        updateMediaMetaData(true /* metaDataChanged */);
    }

    public boolean isCollapsing() {
        return mNotificationPanel.isCollapsing();
    }

    public void addPostCollapseAction(Runnable r) {
        mPostCollapseRunnables.add(r);
    }

    public boolean isInLaunchTransition() {
        return mNotificationPanel.isLaunchTransitionRunning()
                || mNotificationPanel.isLaunchTransitionFinished();
    }

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
                updateMediaMetaData(false);
                mNotificationPanel.setAlpha(1);
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
                .withLayer()
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mNotificationPanel.setAlpha(1f);
                        hideKeyguard();
                    }
                })
                .start();
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
        boolean staying = mLeaveOpenOnKeyguardHide;
        recycleLockscreenWallpaper();
        recycleBlurWallpaper();
        setBarState(StatusBarState.SHADE);
        if (mLeaveOpenOnKeyguardHide) {
            mLeaveOpenOnKeyguardHide = false;
            mNotificationPanel.animateToFullShade(calculateGoingToFullShadeDelay());
            if (mDraggedDownRow != null) {
                mDraggedDownRow.setUserLocked(false);
                mDraggedDownRow = null;
            }
        } else {
            instantCollapseNotificationPanel();
        }
        updateKeyguardState(staying, false /* fromShadeLocked */);

        // Keyguard state has changed, but QS is not listening anymore. Make sure to update the tile
        // visibilities so next time we open the panel we know the correct height already.
        if (mQSPanel != null) {
            mQSPanel.refreshAllTiles();
        }
        mHandler.removeMessages(MSG_LAUNCH_TRANSITION_TIMEOUT);
        /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-7-8-start*/
        //isShowKeyguardChargingAnimation(false,true,true);
        /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-7-8-end*/
        //prize-wangyunhe-add_20151021
        //prize-modify-by-zhongweilin
        //int isSystemFlashOn = SystemProperties.getInt("persist.sys.prizeflash",0);
        int isSystemFlashOn = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, -1);
        if(isSystemFlashOn==0){
            ReleaseFlash();
        }
        hideKeyguardChargeAnimation(true);
        recycleLockscreenWallpaper();
        recycleBlurWallpaper();
        /*PRIZE-set mBlurBack Layout Gone when hideKeyguard-liufan-2016-06-03-start*/
        if(mBlurBack.getVisibility() != View.GONE){
            mBlurBack.setVisibility(View.GONE);
        }
        /*PRIZE-set mBlurBack Layout Gone when hideKeyguard-liufan-2016-06-03-end*/
        return staying;
    }
	
	//add by vlife-start-2016-08-20-start
	public void hideVlifeKeyguardView(){
        if(NotificationPanelView.USE_VLIFE) mNotificationPanel.getVlifeKeyguardView().setVisibility(View.GONE);
	}
	
	public void openApp(int type){
		if(!NotificationPanelView.USE_VLIFE){
			return ;
		}
		if(type == 1){
			launchPhone();
		} else if(type == 2){
			if(mKeyguardBottomArea!=null) mKeyguardBottomArea.launchCamera();
		} else if(type == 3){
			launchSms();
		}
	}
	private final Intent PHONE_INTENT = new Intent(Intent.ACTION_DIAL);
    private void launchPhone() {
        final TelecomManager tm = TelecomManager.from(mContext);
        if (tm.isInCall()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    tm.showInCallScreen(false /* showDialpad */);
                }
            });
        } else {
            startActivity(PHONE_INTENT, false /* dismissShade */);
        }
    }
	
    private void launchSms() {
		Intent intent = new Intent();
		//intent.setAction(Intent.ACTION_MAIN);
		//intent.addCategory(Intent.CATEGORY_DEFAULT);
		//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//intent.setType("vnd.android-dir/mms-sms");
		//intent.putExtra("type","sms");
		 ComponentName comp = new ComponentName("com.android.mms","com.android.mms.ui.BootActivity");
		intent.setComponent(comp);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent, false /* dismissShade */);
    }
	//add by vlife-end-2016-08-20-end
   //add by zookingsoft-start-20161114
  	public void hideZookingKeyguardView(){
  		  Log.d(TAG, "hideZookingKeyguardView");
          if(NotificationPanelView.USE_ZOOKING) {
        	  mNotificationPanel.getZookingKeyguardView().setVisibility(View.GONE);
          }
  	}
  //add by zookingsoft-end-20161114

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
        disable(mDisabledUnmodified1, mDisabledUnmodified2, fadeoutDuration > 0 /* animate */);
    }

    public boolean isKeyguardFadingAway() {
        return mKeyguardFadingAway;
    }

    /**
     * Notifies that the Keyguard fading away animation is done.
     */
    public void finishKeyguardFadingAway() {
        mKeyguardFadingAway = false;
    }

    public void stopWaitingForKeyguardExit() {
        mWaitingForKeyguardExit = false;
    }

    private void updatePublicMode() {
        setLockscreenPublicMode(
                mStatusBarKeyguardViewManager.isShowing() && mStatusBarKeyguardViewManager
                        .isSecure(mCurrentUserId));
    }

    private void updateKeyguardState(boolean goingToFullShade, boolean fromShadeLocked) {
        if (mState == StatusBarState.KEYGUARD) {
            mKeyguardIndicationController.setVisible(true);
            mNotificationPanel.resetViews();
            mKeyguardUserSwitcher.setKeyguard(true, fromShadeLocked);
            mStatusBarView.removePendingHideExpandedRunnables();
        } else {
            mKeyguardIndicationController.setVisible(false);
            mKeyguardUserSwitcher.setKeyguard(false,
                    goingToFullShade || mState == StatusBarState.SHADE_LOCKED || fromShadeLocked);
        }
        if (mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED) {
            mScrimController.setKeyguardShowing(true);
            mIconPolicy.setKeyguardShowing(true);
        } else {
            mScrimController.setKeyguardShowing(false);
            mIconPolicy.setKeyguardShowing(false);
        }
        mNotificationPanel.setBarState(mState, mKeyguardFadingAway, goingToFullShade);
        updateDozingState();
        updatePublicMode();
        updateStackScrollerState(goingToFullShade);
        updateNotifications();
        checkBarModes();

        /// M: Support "Operator plugin - Customize Carrier Label for PLMN". @{
        updateCarrierLabelVisibility(false);
        /// M: Support "Operator plugin - Customize Carrier Label for PLMN". @}

        updateMediaMetaData(false);
        mKeyguardMonitor.notifyKeyguardState(mStatusBarKeyguardViewManager.isShowing(),
                mStatusBarKeyguardViewManager.isSecure());
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
    }

    private void updateDozingState() {
        boolean animate = !mDozing && mDozeScrimController.isPulsing();
        mNotificationPanel.setDozing(mDozing, animate);
        mStackScroller.setDark(mDozing, animate, mWakeUpTouchLocation);
        mScrimController.setDozing(mDozing);

        // Immediately abort the dozing from the doze scrim controller in case of wake-and-unlock
        // for pulsing so the Keyguard fade-out animation scrim can take over.
        mDozeScrimController.setDozing(mDozing &&
                mFingerprintUnlockController.getMode()
                        != FingerprintUnlockController.MODE_WAKE_AND_UNLOCK_PULSING, animate);
    }

    public void updateStackScrollerState(boolean goingToFullShade) {
        if (mStackScroller == null) return;
        boolean onKeyguard = mState == StatusBarState.KEYGUARD;
        mStackScroller.setHideSensitive(isLockscreenPublicMode(), goingToFullShade);
        mStackScroller.setDimmed(onKeyguard, false /* animate */);
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
        /*PRIZE-delete call mStatusBarKeyguardViewManager.onMenuPressed()- liufan-2016-09-08-start*/
        return mState == StatusBarState.KEYGUARD;
        //return mState == StatusBarState.KEYGUARD && mStatusBarKeyguardViewManager.onMenuPressed();
        /*PRIZE-delete call mStatusBarKeyguardViewManager.onMenuPressed()- liufan-2016-09-08-end*/
    }

    public boolean onBackPressed() {
        if (mStatusBarKeyguardViewManager.onBackPressed()) {
            isShowBlurBgWhenLockscreen(false);
            //add by vlife-start-2016-07-30
            mNotificationPanel.againSendInToEnegine();
            //add by vlife-end-2016-07-30
            //add by zookingsoft 20161116 start
            if (NotificationPanelView.USE_ZOOKING) {
            	mNotificationPanel.onBackPressedTOZookingKeyguard();
            }
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
        if (mDeviceInteractive
                && (mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED)) {
            animateCollapsePanels(
                    CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL /* flags */, true /* force */);
            return true;
        }
        return false;
    }

    /*PRIZE-add for lockscreen EmptySpaceClick, bugid: 26198-liufan-2016-12-08-start*/
    public boolean isBouncerShowing(){
        if(mStatusBarKeyguardViewManager != null) {
            return mStatusBarKeyguardViewManager.isBouncerShowing();
        }
        return false;
    }
    /*PRIZE-add for lockscreen EmptySpaceClick, bugid: 26198-liufan-2016-12-08-end*/
    
    private void showBouncer() {
        if (mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED) {
            mWaitingForKeyguardExit = mStatusBarKeyguardViewManager.isShowing();
            /*PRIZE-KeyguardBouncer avoid overlapping with the LockScreen-liufan-2015-09-17-start*/
            dismissKeyguard();
            /*PRIZE-KeyguardBouncer avoid overlapping with the LockScreen-liufan-2015-09-17-end*/
            mStatusBarKeyguardViewManager.dismiss();
            
            /*PRIZE-blur background-liufan-2015-09-04-start*/
            isShowBlurBgWhenLockscreen(false);
            ScrimController.isDismissScrim = false;
            mScrimController.setKeyguardShowing(mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED);
            /*PRIZE-blur background-liufan-2015-09-04-end*/
        }
    }
    
    /*PRIZE-KeyguardBouncer avoid overlapping with the LockScreen-liufan-2015-09-17-start*/
    public void dismissKeyguard(){
    	//modify by zookingsoft 20161116
		if(mNotificationPanel.USE_VLIFE || NotificationPanelView.USE_ZOOKING) return;//add by vlife-2016-07-30
        mNotificationPanel.flingImmediately(0,false);
    }
    /*PRIZE-KeyguardBouncer avoid overlapping with the LockScreen-liufan-2015-09-17-end*/
    
    private void instantExpandNotificationsPanel() {

        // Make our window larger and the panel expanded.
        makeExpandedVisible(true);
        mNotificationPanel.instantExpand();
    }

    private void instantCollapseNotificationPanel() {
        mNotificationPanel.instantCollapse();
    }

    @Override
    public void onActivated(ActivatableNotificationView view) {
        EventLogTags.writeSysuiLockscreenGesture(
                EventLogConstants.SYSUI_LOCKSCREEN_GESTURE_TAP_NOTIFICATION_ACTIVATE,
                0 /* lengthDp - N/A */, 0 /* velocityDp - N/A */);
        mKeyguardIndicationController.showTransientIndication(R.string.notification_tap_again);
        ActivatableNotificationView previousView = mStackScroller.getActivatedChild();
        if (previousView != null) {
            previousView.makeInactive(true /* animate */);
        }
        mStackScroller.setActivatedChild(view);
    }

    /*PRIZE-add isHideKeyguard-liufan-2016-06-20-start*/
    private boolean isHideKeyguard = false;
    /*PRIZE-add isHideKeyguard-liufan-2016-06-20-end*/

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
        mState = state;
        /*PRIZE-add isHideKeyguard-liufan-2016-06-20-start*/
        if(state == StatusBarState.SHADE){
            isHideKeyguard = true;
        }
        /*PRIZE-add isHideKeyguard-liufan-2016-06-20-end*/
        mGroupManager.setStatusBarState(state);
        mStatusBarWindowManager.setStatusBarState(state);
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
        mKeyguardIndicationController.showTransientIndication(R.string.keyguard_unlock);
    }

    public void onHintFinished() {
        // Delay the reset a bit so the user can read the text.
        mKeyguardIndicationController.hideTransientIndicationDelayed(HINT_RESET_DELAY_MS);
    }

    public void onCameraHintStarted() {
        mKeyguardIndicationController.showTransientIndication(R.string.camera_hint);
    }

    public void onVoiceAssistHintStarted() {
        mKeyguardIndicationController.showTransientIndication(R.string.voice_hint);
    }

    public void onPhoneHintStarted() {
        mKeyguardIndicationController.showTransientIndication(R.string.phone_hint);
    }

    public void onTrackingStopped(boolean expand) {
        if (mState == StatusBarState.KEYGUARD || mState == StatusBarState.SHADE_LOCKED) {
            if (!expand && !mUnlockMethodCache.canSkipBouncer()) {
                showBouncer();
            }
        }
    }

    @Override
    protected int getMaxKeyguardNotifications() {
        return mKeyguardMaxNotificationCount;
    }

    public NavigationBarView getNavigationBarView() {
        return mNavigationBarView;
    }

    // ---------------------- DragDownHelper.OnDragDownListener ------------------------------------

    @Override
    public boolean onDraggedDown(View startingChild, int dragLengthY) {
        if (hasActiveNotifications()) {
            EventLogTags.writeSysuiLockscreenGesture(
                    EventLogConstants.SYSUI_LOCKSCREEN_GESTURE_SWIPE_DOWN_FULL_SHADE,
                    (int) (dragLengthY / mDisplayMetrics.density),
                    0 /* velocityDp - N/A */);

            // We have notifications, go to locked shade.
            goToLockedShade(startingChild);
            /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-8-25-start*/
            //isShowKeyguardChargingAnimation(true,true,false);
            /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-8-25-end*/
            tileHandler.postDelayed(new Runnable(){
                @Override
                public void run() {
                    setBlurBackVisibility(View.INVISIBLE);
                }
            },100);
            return true;
        } else {

            // No notifications - abort gesture.
            return false;
        }
    }

    @Override
    public void onDragDownReset() {
        mStackScroller.setDimmed(true /* dimmed */, true /* animated */);
    }

    @Override
    public void onThresholdReached() {
        mStackScroller.setDimmed(false /* dimmed */, true /* animate */);
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
            row.setUserExpanded(true);
        }
        boolean fullShadeNeedsBouncer = !userAllowsPrivateNotificationsInPublic(mCurrentUserId)
                || !mShowLockscreenNotifications;
        if (isLockscreenPublicMode() && fullShadeNeedsBouncer) {
            mLeaveOpenOnKeyguardHide = true;
            showBouncer();
            mDraggedDownRow = row;
        } else {
            mNotificationPanel.animateToFullShade(0 /* delay */);
            setBarState(StatusBarState.SHADE_LOCKED);
            updateKeyguardState(false /* goingToFullShade */, false /* fromShadeLocked */);
            if (row != null) {
                row.setUserLocked(false);
            }
        }
        /*PRIZE-show blur background when pull on the lockscreen-liufan-2015-06-15-start*/
        if (VersionControl.CUR_VERSION == VersionControl.BLUR_BG_VER) {
            showBlurWallPaper();
        }
        /*PRIZE-show blur background when pull on the lockscreen-liufan-2015-06-15-end*/
    }

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
            setBlurBackVisibility(View.VISIBLE);
            mStackScroller.onGoToKeyguard();
            setBarState(StatusBarState.KEYGUARD);
            updateKeyguardState(false /* goingToFullShade */, true /* fromShadeLocked*/);
        }
        /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-8-25-start*/
        //isShowKeyguardChargingAnimation(true,true,false);
        /*PRIZE-weather show KeyguardChargeAnimationView-liufan-2015-8-25-end*/
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
        disable(mDisabledUnmodified1, mDisabledUnmodified2, true /* animate */);
        /*PRIZE-refresh the virtual key show state-liufan-2015-12-14-start*/
        KeyguardViewMediator keyguardViewMediator = getComponent(KeyguardViewMediator.class);
        keyguardViewMediator.setStatusBarState(mState,bouncerShowing);
        /*PRIZE-refresh the virtual key show state-liufan-2015-12-14-end*/
    }

    public void onFinishedGoingToSleep() {
        mDeviceInteractive = false;
        mWakeUpComingFromTouch = false;
        mWakeUpTouchLocation = null;
        mStackScroller.setAnimationsEnabled(false);
        updateVisibleToUser();
    }

    public void onStartedWakingUp() {
        mDeviceInteractive = true;
        mStackScroller.setAnimationsEnabled(true);
        mNotificationPanel.setTouchDisabled(false);
        updateVisibleToUser();
    }

    public void onScreenTurningOn() {
        mNotificationPanel.onScreenTurningOn();
    }

    public void onScreenTurnedOn() {
        mDozeScrimController.onScreenTurnedOn();
    }

    /**
     * This handles long-press of both back and recents.  They are
     * handled together to capture them both being long-pressed
     * at the same time to exit screen pinning (lock task).
     *
     * When accessibility mode is on, only a long-press from recents
     * is required to exit.
     *
     * In all other circumstances we try to pass through long-press events
     * for Back, so that apps can still use it.  Which can be from two things.
     * 1) Not currently in screen pinning (lock task).
     * 2) Back is long-pressed without recents.
     */
    private void handleLongPressBackRecents(View v) {
        try {
            boolean sendBackLongPress = false;
            IActivityManager activityManager = ActivityManagerNative.getDefault();
            boolean isAccessiblityEnabled = mAccessibilityManager.isEnabled();
            if (activityManager.isInLockTaskMode() && !isAccessiblityEnabled) {
                long time = System.currentTimeMillis();
                // If we recently long-pressed the other button then they were
                // long-pressed 'together'
                if ((time - mLastLockToAppLongPress) < LOCK_TO_APP_GESTURE_TOLERENCE) {
                    activityManager.stopLockTaskModeOnCurrent();
                    // When exiting refresh disabled flags.
                    mNavigationBarView.setDisabledFlags(mDisabled1, true);
                } else if ((v.getId() == R.id.back)
                        && !mNavigationBarView.getRecentsButton().isPressed()) {
                    // If we aren't pressing recents right now then they presses
                    // won't be together, so send the standard long-press action.
                    sendBackLongPress = true;
                }
                mLastLockToAppLongPress = time;
            } else {
                // If this is back still need to handle sending the long-press event.
                if (v.getId() == R.id.back) {
                    sendBackLongPress = true;
                } else if (isAccessiblityEnabled && activityManager.isInLockTaskMode()) {
                    // When in accessibility mode a long press that is recents (not back)
                    // should stop lock task.
                    activityManager.stopLockTaskModeOnCurrent();
                    // When exiting refresh disabled flags.
                    mNavigationBarView.setDisabledFlags(mDisabled1, true);
                }
            }
            if (sendBackLongPress) {
                KeyButtonView keyButtonView = (KeyButtonView) v;
                keyButtonView.sendEvent(KeyEvent.ACTION_DOWN, KeyEvent.FLAG_LONG_PRESS);
                keyButtonView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
            }
        } catch (RemoteException e) {
            Log.d(TAG, "Unable to reach activity manager", e);
        }
    }

    // Recents

    @Override
    protected void showRecents(boolean triggeredFromAltTab) {
        // Set the recents visibility flag
        mSystemUiVisibility |= View.RECENT_APPS_VISIBLE;
        notifyUiVisibilityChanged(mSystemUiVisibility);
        super.showRecents(triggeredFromAltTab);
    }

    @Override
    protected void hideRecents(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) {
        // Unset the recents visibility flag
        mSystemUiVisibility &= ~View.RECENT_APPS_VISIBLE;
        notifyUiVisibilityChanged(mSystemUiVisibility);
        super.hideRecents(triggeredFromAltTab, triggeredFromHomeKey);
    }

    @Override
    protected void toggleRecents() {
        // Toggle the recents visibility flag
        mSystemUiVisibility ^= View.RECENT_APPS_VISIBLE;
        notifyUiVisibilityChanged(mSystemUiVisibility);
        super.toggleRecents();
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        // Update the recents visibility flag
        if (visible) {
            mSystemUiVisibility |= View.RECENT_APPS_VISIBLE;
        } else {
            mSystemUiVisibility &= ~View.RECENT_APPS_VISIBLE;
        }
        notifyUiVisibilityChanged(mSystemUiVisibility);
    }

    @Override
    public void showScreenPinningRequest() {
        if (mKeyguardMonitor.isShowing()) {
            // Don't allow apps to trigger this from keyguard.
            return;
        }
        // Show screen pinning request, since this comes from an app, show 'no thanks', button.
        showScreenPinningRequest(true);
    }

    public void showScreenPinningRequest(boolean allowCancel) {
        mScreenPinningRequest.showPrompt(allowCancel);
    }

    public boolean hasActiveNotifications() {
        return !mNotificationData.getActiveNotifications().isEmpty();
    }

    public void wakeUpIfDozing(long time, MotionEvent event) {
        if (mDozing && mDozeScrimController.isPulsing()) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            pm.wakeUp(time, "com.android.systemui:NODOZE");
            mWakeUpComingFromTouch = true;
            mWakeUpTouchLocation = new PointF(event.getX(), event.getY());
            mNotificationPanel.setTouchDisabled(false);
            mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
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
    }

    @Override
    public void appTransitionStarting(long startTime, long duration) {

        // Use own timings when Keyguard is going away, see keyguardGoingAway and
        // setKeyguardFadingAway
        if (!mKeyguardFadingAway) {
            mIconController.appTransitionStarting(startTime, duration);
        }
        if (mIconPolicy != null) {
            mIconPolicy.appTransitionStarting(startTime, duration);
        }
    }

    public void notifyFpAuthModeChanged() {
        updateDozing();
    }

    private void updateDozing() {
        // When in wake-and-unlock while pulsing, keep dozing state until fully unlocked.
        mDozing = mDozingRequested && mState == StatusBarState.KEYGUARD
                || mFingerprintUnlockController.getMode()
                        == FingerprintUnlockController.MODE_WAKE_AND_UNLOCK_PULSING;
        updateDozingState();
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

    private final class DozeServiceHost extends KeyguardUpdateMonitorCallback implements DozeHost  {
        // Amount of time to allow to update the time shown on the screen before releasing
        // the wakelock.  This timeout is design to compensate for the fact that we don't
        // currently have a way to know when time display contents have actually been
        // refreshed once we've finished rendering a new frame.
        private static final long PROCESSING_TIME = 500;

        private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
        private final H mHandler = new H();

        // Keeps the last reported state by fireNotificationLight.
        private boolean mNotificationLightOn;
        private boolean mWakeAndUnlocking;

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
            mDozeScrimController.pulse(callback, reason);
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

    /// Support [SIM Indicator] @ {
    @Override
    public void showDefaultAccountStatus(int subId) {
        DefaultAccountStatus status = new DefaultAccountStatus(subId);
        mNetworkController.showDefaultAccountStatus(status);
    }

    @Override
    public void hideDefaultAccountStatus() {
        mNetworkController.showDefaultAccountStatus(null);
    }
    // @ }

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
					updateMediaMetaData(true);
				}
				if(action.equals(ACTION_KILL_SUPERPOWER)){
					bIntoSuperSavingPower = false;
					isPrizeChange = true;
					updateMediaMetaData(true);
				}
				if(action.equals(ACTION_EXIT_POWERSAVING)){
					bIntoSuperSavingPower = false;
					isPrizeChange = true;
					updateMediaMetaData(true);
				}
			}				
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
            updateMediaMetaData(true);
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

    /// M:add for multi window @{
    public void registerMWProxyAgain()
    {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (DEBUG) Log.v(TAG, "Receive ACTION_BOOT_COMPLETED");
                if (MultiWindowProxy.isSupported()) {
                     if (DEBUG) Log.v(TAG, "When bootCompleted Make Sure "
                            + "Multi-window setSystemUiCallback ");
                     MultiWindowProxy.getInstance().setSystemUiCallback(new MWSystemUiCallback());
                }
            }
        }, filter);
    }

    public void updateNavigationBarIcon(int state){
        if (mNavigationBarView != null) mNavigationBarView.setDisabledFlags(state);
    }

    public void updateFloatButtonIcon(boolean flaotPanelOpen) {
        if (mNavigationBarView != null) {
            if (flaotPanelOpen) {
                mNavigationBarView.getFloatButton().setImageResource(R.drawable.ic_sysbar_up);
                checkBarModes();
            } else {
                mNavigationBarView.getFloatButton().setImageResource(R.drawable.ic_sysbar_down);
                mNavigationBarView.getBarTransitions().transitionTo(MODE_OPAQUE, true);
            }
        }
    }


    public void updateFloatModeButton(boolean modeclick) {
        if (mNavigationBarView != null) {
            if (modeclick) {
                mNavigationBarView.getFloatModeButton().setImageResource(
                  R.drawable.ic_sysbar_float_mode_on);
            } else {
                mNavigationBarView.getFloatModeButton().setImageResource(
                  R.drawable.ic_sysbar_float_mode_off);
            }
        }
    }

    public void updateSpilitModeButton(boolean modeclick) {
        if (mNavigationBarView != null) {
            if (modeclick) {
                mNavigationBarView.getSplitModeButton().setImageResource(
                  R.drawable.ic_sysbar_split_mode_on);
            } else {
                mNavigationBarView.getSplitModeButton().setImageResource(
                  R.drawable.ic_sysbar_split_mode_off);
            }
        }
    }
    public void updateFloatButtonIconOnly(boolean floatPanelOpen) {
        /// M: [SystemUI] [Tablet Only]Support "Multi Window"
        Log.v(TAG, "updateFloatButtonIconOnly, floatPanelOpen=" + floatPanelOpen);
        if (mNavigationBarView != null) {
            if (floatPanelOpen) {
                mNavigationBarView.getFloatButton().setImageResource(R.drawable.ic_sysbar_down);
            } else {
                mNavigationBarView.getFloatButton().setImageResource(R.drawable.ic_sysbar_up);
            }
        }
    }

    public void setNavigationBarEditFloatListener(View.OnClickListener listener) {
        if (mNavigationBarView != null) {
            mNavigationBarView.getExtensionButton().setOnClickListener(listener);
        }
    }

    public void setFloatModeButtonVisibility(int visibility) {
        if (mNavigationBarView != null) {
            mNavigationBarView.getFloatModeButton().setVisibility(visibility);
        }
    }

    public void setSplitModeButtonVisibility(int visibility) {
        if (mNavigationBarView != null) {
            mNavigationBarView.getSplitModeButton().setVisibility(visibility);
        }
    }

    public void setLineVisibility(int visibility) {
        if (mNavigationBarView != null) {
            mNavigationBarView.getLineView().setVisibility(visibility);
        }
    }

    public void setExtensionButtonVisibility(int visibility) {
        if (mNavigationBarView != null) {
            mNavigationBarView.getExtensionButton().setText(
              com.android.internal.R.string.more_item_label);
            mNavigationBarView.getExtensionButton().setVisibility(visibility);
        }
    }

    ///* M:ALPS01454734 Icon of multi window display on lock screen*/
    public void setFloatButtonVisibility(int visibility) {
        if ((mNavigationBarView != null) && (mNavigationBarView.getFloatButton() != null)) {
            mNavigationBarView.getFloatButton().setVisibility(visibility);
        }
    }

    @Override
    protected WindowManager.LayoutParams getFloatLayoutParams(
            LayoutParams layoutParams, boolean focus) {
        boolean opaque = false;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                 WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                ,
                (opaque ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT));

        /*show multi-window screen in multi-user ui  */
        lp.privateFlags |=
                WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;

        if(!focus){
            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        } else {
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lp.dimAmount = 0.75f;
        }
        lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        lp.setTitle("FloatPanel");
        lp.windowAnimations = com.android.internal.R.style.Animation_RecentApplications;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        return lp;
    }

    private View.OnClickListener mFloatClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            awakenDreams();
            toggleFloatApps();
            if(mIsSplitModeEnable){
                updateFloatModeButton(!mIsSplitModeOn);
                updateSpilitModeButton(mIsSplitModeOn);
            }
        }
    };

    private View.OnClickListener mFloatModeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //for button use
            Log.v(TAG, "onClick, mFloatModeClickListener=");
            if (mIsSplitModeOn) {
                MultiWindowProxy.getInstance().switchToFloatingMode();
                mIsSplitModeOn = MultiWindowProxy.isSplitMode();
                updateFloatModeButton(!mIsSplitModeOn);
                updateSpilitModeButton(mIsSplitModeOn);
            }
            closeFloatPanel();
        }
    };

    private View.OnClickListener mSplitModeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //for button use
            if (!mIsSplitModeOn) {
                MultiWindowProxy.getInstance().switchToSplitMode();
                mIsSplitModeOn = MultiWindowProxy.isSplitMode();
                updateFloatModeButton(!mIsSplitModeOn);
                updateSpilitModeButton(mIsSplitModeOn);
            }
            closeFloatPanel();
        }
    };


    public void showRestoreButtonInner(boolean flag) {
        mNavigationBarView.showRestoreButton(flag);
    }

    public class MWSystemUiCallback extends IMWSystemUiCallback.Stub{
        public MWSystemUiCallback (){
        }
        @Override
        public void showRestoreButton(boolean flag){
            showRestoreButtonInner(flag);
        }
    }
     /// @}
	
    /*PRIZE lockscreen background liyao-2015-07-22-start*/
    private Bitmap keyGuardBg = null; // added by fanjunchen for recycle.
    private boolean isPrizeChange = true;// added by fanjunchen
    /*@prize fanjunchen start { not use prizeLockscreen so it to be true 2015-11-05 modified*/
    private final static boolean SUPPORT_KEYGUARD_WALLPAPER = true;
    /*@prize fanjunchen end }*/
    private static final String KEYGUARD_WALLPAPER_URI = "keyguard_wallpaper";
    private Bitmap convertToBitmap(String path) {
        long time1 = System.currentTimeMillis();
        if (!isPrizeChange && keyGuardBg != null)
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
        }catch(Exception e){
        }
        long time2 = System.currentTimeMillis();
        Log.d(TAG,"time time--------->"+(time2-time1));
        return bitmap;
    }

    /*PRIZE-add,recycle bitmap-liufan-2016-06-03-start*/
    public void recycleLockscreenWallpaper(){
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

    KeyguardWallpaperObserver mKeyguardWallpaperObserver;
    private String curWallpaperPath = null;
    /** ContentObserver to watch KeyguardWallpaper **/
    private class KeyguardWallpaperObserver extends ContentObserver {

        public KeyguardWallpaperObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.v(TAG, "KeyguardWallpaperObserver onChange" );
            if (selfChange) return;
            final String strPath = Settings.System.getString(mContext.getContentResolver(),KEYGUARD_WALLPAPER_URI);

            /**PRIZE-haokanscreen iteration one-liufan-2016-06-23-start */
			
			/*if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW){
            	String screenPath = coverScreenImg(strPath,"haokanwallpaper");
           		Intent inte = new Intent("com.haokan.yitu.ACTION_KEYGUARD_BACKGROUND_CHANGED");
                inte.putExtra("keyguard_background",screenPath);
                Log.v("xsy", "KeyguardWallpaperObserver onChange uri="+uri+",selfChange="+selfChange +",screenPath="+screenPath);
                mContext.sendBroadcast(inte);
       		} else {*/
           		 if(strPath != null && !strPath.equals(curWallpaperPath)){
               		 curWallpaperPath = strPath;
                     copyFile(strPath);
           		 }
       		//}
		
			
            //copyFile(strPath);
			/**PRIZE-haokanscreen iteration one-liufan-2016-06-23-end */

            //prize-disable KGWallpager auto-change function-pengcancan-00160906-start
			if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW){
                Settings.System.putInt(mContext.getContentResolver(),Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH, 0);
                bChangedWallpaperIsOpen = false;
			}else{
                Settings.System.putInt(mContext.getContentResolver(),Settings.System.PRIZE_KGWALLPAPER_SWITCH, 0);
                bChangedWallpaperIsOpen = false;
			}
            //prize-disable KGWallpager auto-change function-pengcancan-00160906-end
        }

        public void startObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                    Settings.System.getUriFor(KEYGUARD_WALLPAPER_URI),
                    false, this, UserHandle.USER_ALL);

        }

        public void stopObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
        }

    }
    /*PRIZE set lockscreen background liyao-2015-07-22-end*/
    
	/**PRIZE-haokanscreen iteration one-liufan-2016-06-23-start */
	public  String coverScreenImg(String path,String covername) {
		try {
		    File oldfile = new File(path);
		    String parent = oldfile.getParent();
		    System.out.println("parent="+parent);
		   
		    if(oldfile.exists()){
			    File srceenfile = new File(parent,covername);
				if(srceenfile.exists()){
					srceenfile.delete();
				}
		   
				oldfile.renameTo(srceenfile);
				return srceenfile.getPath();
		    }
		   
		    
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return "";
	}
	/**PRIZE-haokanscreen iteration one-liufan-2016-06-23-end */
	
    /**  
     * PRIZE set lockscreen wallpaper liyao-2015-07-28
     * copy singel file
     * @param oldPath String the path of source file
     * @param newPath String the path which is copy to 
     * @return boolean  
     */   
    public void copyFile(final String oldPath) {
        if(oldPath == null || oldPath.equals("")) return;
        // @prize fanjunchen added 2015-09-16 {
        CopyTask c = new CopyTask();
		if(copyTaskList == null){
			copyTaskList = new ArrayList<CopyTask>();
		}
		if(isCopyTaskExe){
			c.setFocusEndExe(true);
			copyTaskList.add(c);
		}else{
			c.execute(oldPath);
		}
        // @prize }
   }
   List<CopyTask> copyTaskList = new ArrayList<CopyTask>();
   private boolean isCopyTaskExe = false;
    // @prize fanjunchen added 2015-09-16 {
    /***
     * 
     * @author fanjunchen
     *
     */
    class CopyTask extends AsyncTask<String, Void, Boolean> {
        private String path = null;
		private boolean isFocusEndExe = false;
		public void setFocusEndExe(boolean focusEndExe){
			isFocusEndExe = focusEndExe;
		}
    	@Override
    	public Boolean doInBackground(String[] params) {
    		isCopyTaskExe = true;
    		if (params == null && params.length <1)
    			return false;
    		
    		path = params[0];
    		
    		String outFileName = "KeyguardWallpaper.png";
    		if (path.indexOf("jrlkp") != -1) {
    			outFileName = "jrlkp.png";
    		}
    		else {
    			String dataPath = mContext.getFilesDir().getPath();
                File f = new File(dataPath, "jrlkp.png");
                f.delete();
                f = null;
                if ("-1".equals(path)) {
                	isPrizeChange = true;
                	return true;
                }
    		}
    		Log.i(TAG,"start copyFile");
            InputStream inStream = null;
            FileOutputStream fs = null;
            try {
                int bytesum = 0;
                int byteread = 0;
                File oldfile = new File(path);
                if (oldfile.exists()) { //
                    String dataPath = mContext.getFilesDir().getPath();
                    File f = new File(dataPath, outFileName);
                    String newPath = f.getPath();
                    if(f.exists()) {
                        f.delete();
                    }
                    inStream = new FileInputStream(path);    
                    fs = new FileOutputStream(newPath);   
                    byte[] buffer = new byte[1444];
                    while (!isFocusEndExe && (byteread = inStream.read(buffer)) != -1) {
                        bytesum += byteread;
                        System.out.println(bytesum);
                        fs.write(buffer, 0, byteread);
                    }
                    isPrizeChange = true;
                    Log.i(TAG,"copy file OK!");
                    oldfile.delete();
                    return true;
                } else {
                    Log.i(TAG,"copy file not exists");
                    return false;
                } 
            }
            catch (Exception e) {
                Log.i(TAG,"Not Copy File");
                e.printStackTrace();
                return false;
            } finally{
                try {
                     if(inStream != null) inStream.close();
                     if(fs != null) fs.close();
                 } catch (IOException e1) {
                     // TODO Auto-generated catch block
                     e1.printStackTrace();
                 }
                Log.i(TAG,"close I/O");
            }
        }
    	@Override
    	protected void onPostExecute(Boolean b) {
			isCopyTaskExe = false;
			if(!isFocusEndExe){
				if (path != null && (path.indexOf("jrlkp") != -1 || "-1".equals(path)))
					return;
				if (b){
					/* prize-modify-by-lijimeng-bugid 34890-20170619-start*/
					//Toast.makeText(mContext, mContext.getString(R.string.lock_scr_success), Toast.LENGTH_SHORT).show();
					/* prize-modify-by-lijimeng-bugid 34890-20170619-end*/
				}else
					Toast.makeText(mContext, mContext.getString(R.string.lock_scr_fail), Toast.LENGTH_SHORT).show();
				updateMediaMetaData(true);
			}
			if(copyTaskList != null && copyTaskList.size() != 0){
				String strPath = Settings.System.getString(mContext.getContentResolver(),KEYGUARD_WALLPAPER_URI);
				CopyTask c = copyTaskList.get(copyTaskList.size()-1);
				copyTaskList.clear();
				c.execute(strPath);
			}
    	}
    }
    // @prize end}
    /*PRIZE-when unlock keguard appear splash screen bugId17935-dengyu-2016-7-6-start*/
    public ScrimController getScrimController() {
            return mScrimController;
    }
    /*PRIZE-when unlock keguard appear splash screen bugId17935-dengyu-2016-7-6-end*/
}
