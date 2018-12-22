/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.recents;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DebugFlagsChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.DockedFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowLastAnimationFrameEvent;
import com.android.systemui.recents.events.activity.ExitRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.IterateRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskSucceededEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.ToggleRecentsEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.events.ui.HideIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.events.ui.SendClearTaskServiceEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.events.ui.ShowIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.StackViewScrolledEvent;
import com.android.systemui.recents.events.ui.UpdateFreeformTaskViewVisibilityEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusNextTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusPreviousTaskViewEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.RecentsPackageMonitor;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.RecentsView;
import com.android.systemui.recents.views.SystemBarScrimViews;
import com.android.systemui.recents.views.TaskStackAnimationHelper;
import com.android.systemui.statusbar.BaseStatusBar;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/*prize-import-liufan-2015-11-04-start*/
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.widget.TextView;
import com.android.systemui.recents.views.ArcView;
import android.widget.ImageView;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.animation.ValueAnimator;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.app.WallpaperManager;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.os.Handler;
import com.android.systemui.statusbar.phone.BlurPic;
import android.animation.AnimatorListenerAdapter;
import android.animation.Animator;
import android.content.SharedPreferences;
import android.text.format.Formatter;
/*prize-import-liufan-2015-11-04-end*/

import com.mediatek.common.prizeoption.PrizeOption; // prize-add-split screen-liyongli-20170727 add

/*prize add by xiarui 2017-11-21 start*/
import android.view.Display;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.view.Window;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.SendClearTaskServiceEvent;
import com.android.systemui.recents.events.ui.PrizeShowCleanResultByToastEvent;
/*prize add by xiarui 2017-11-21 end*/

/**
 * The main Recents activity that is started from RecentsComponent.
 */
public class RecentsActivity extends Activity implements ViewTreeObserver.OnPreDrawListener {

    private final static String TAG = "RecentsActivity";
    private final static boolean DEBUG = false;

    public final static int EVENT_BUS_PRIORITY = Recents.EVENT_BUS_PRIORITY + 1;
    public final static int INCOMPATIBLE_APP_ALPHA_DURATION = 150;

    private RecentsPackageMonitor mPackageMonitor;
    private Handler mHandler = new Handler();
    private long mLastTabKeyEventTime;
    private int mLastDeviceOrientation = Configuration.ORIENTATION_UNDEFINED;
    private int mLastDisplayDensity;
    private boolean mFinishedOnStartup;
    private boolean mIgnoreAltTabRelease;
    private boolean mIsVisible;
    private boolean mReceivedNewIntent;
    public static boolean mNeedSendAnimationCompletedEvent = false; //prize add by xiarui for Bug#44285 2017-12-12

    // Top level views
    private RecentsView mRecentsView;
    private SystemBarScrimViews mScrimViews;
    private View mIncompatibleAppOverlay;

    // Runnables to finish the Recents activity
    private Intent mHomeIntent;

    // The trigger to automatically launch the current task
    private int mFocusTimerDuration;
    private DozeTrigger mIterateTrigger;
    private final UserInteractionEvent mUserInteractionEvent = new UserInteractionEvent();
    private final Runnable mSendEnterWindowAnimationCompleteRunnable = () -> {
        EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
    };

    private ViewGroup mCleanMemoryLayout; //prize add by xiarui 2017-11-10
    private ViewGroup mMemoryInfoLayout; //prize add by xiarui 2017-11-28
    private TextView mSelSpliteApp;

    /*prize-blur background-liufan-2015-11-04-start*/
    private LinearLayout blurLayout;
    private ImageView mGuideView;
    private TextView usedMemoryTxt;
    private TextView allMemoryTxt;
    public static final String MEMORY_UNIT = "GB";
    public static final String MEMORY_UNIT_MB = "MB";
    private ArcView mClearRecents;
    private float totalMemory;
    private float totalMemoryOffset;
    public static long totalMemoryB;
    /*prize-blur background-liufan-2015-11-04-end*/

    /**
     * A common Runnable to finish Recents by launching Home with an animation depending on the
     * last activity launch state. Generally we always launch home when we exit Recents rather than
     * just finishing the activity since we don't know what is behind Recents in the task stack.
     */
    class LaunchHomeRunnable implements Runnable {

        Intent mLaunchIntent;
        ActivityOptions mOpts;

        /**
         * Creates a finish runnable that starts the specified intent.
         */
        public LaunchHomeRunnable(Intent launchIntent, ActivityOptions opts) {
            mLaunchIntent = launchIntent;
            mOpts = opts;
        }

        @Override
        public void run() {
            try {
                mHandler.post(() -> {
                    ActivityOptions opts = mOpts;
                    if (opts == null) {
                        opts = ActivityOptions.makeCustomAnimation(RecentsActivity.this,
                                R.anim.recents_to_launcher_enter, R.anim.recents_to_launcher_exit);
                    }
                    LogUtils.d("xx", "LaunchHomeRunnable");
                    startActivityAsUser(mLaunchIntent, opts.toBundle(), UserHandle.CURRENT);
                });
            } catch (Exception e) {
                Log.e(TAG, getString(R.string.recents_launch_error_message, "Home"), e);
            }
        }
    }

    /**
     * Broadcast receiver to handle messages from the system
     */
    final BroadcastReceiver mSystemBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                /* prize-add-split screen-liyongli-20170825-start */
                if(PrizeOption.PRIZE_SPLIT_SCREEN_NOT_CLR_RECENT && isInMultiWindowMode() ) {
                    return;
                }/* prize-add-split screen-liyongli-20170825-end */

                // When the screen turns off, dismiss Recents to Home
                dismissRecentsToHomeIfVisible(false);
            } else if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                // If the time shifts but the currentTime >= lastStackActiveTime, then that boundary
                // is still valid.  Otherwise, we need to reset the lastStackactiveTime to the
                // currentTime and remove the old tasks in between which would not be previously
                // visible, but currently would be in the new currentTime
                long oldLastStackActiveTime = Prefs.getLong(RecentsActivity.this,
                        Prefs.Key.OVERVIEW_LAST_STACK_TASK_ACTIVE_TIME, -1);
                if (oldLastStackActiveTime != -1) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime < oldLastStackActiveTime) {
                        // We are only removing tasks that are between the new current time
                        // and the old last stack active time, they were not visible and in the
                        // TaskStack so we don't need to remove any associated TaskViews but we do
                        // need to load the task id's from the system
                        RecentsTaskLoadPlan loadPlan = Recents.getTaskLoader().createLoadPlan(ctx);
                        loadPlan.preloadRawTasks(false /* includeFrontMostExcludedTask */);
                        List<ActivityManager.RecentTaskInfo> tasks = loadPlan.getRawTasks();
                        /** Prize modify by xiarui 2017-12-15 (tasks.size() - 1 - 1) for Bug#44937 don't remove current task
                         * See {@link RecentsTaskLoadPlan#preloadPlan(RecentsTaskLoader, int, boolean)} for Bug#46145 ,
                         * Need to make two changes in sync
                         * **/
                        for (int i = tasks.size() - 1 - 1; i >= 0; i--) {
                            ActivityManager.RecentTaskInfo task = tasks.get(i);
                            if (currentTime <= task.lastActiveTime && task.lastActiveTime <
                                    oldLastStackActiveTime) {
                                Recents.getSystemServices().removeTask(task.persistentId);
                                //prize add by xiarui 2018-02-05 @{
                                if (task.baseIntent != null) {
                                    Task.unlockByRemoveTask(ctx, task.baseIntent.getComponent().getPackageName());
                                }
                                //@}
                                Log.d(TAG, "ACTION_TIME_CHANGED---removeTask packageName = "
                                        + ((task.baseIntent != null) ? task.baseIntent.getComponent().getPackageName() : "null"));
                            }
                        }
                        Prefs.putLong(RecentsActivity.this,
                                Prefs.Key.OVERVIEW_LAST_STACK_TASK_ACTIVE_TIME, currentTime);
                    }
                }
            }
        }
    };

    private final OnPreDrawListener mRecentsDrawnEventListener =
            new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
                    EventBus.getDefault().post(new RecentsDrawnEvent());
                    return true;
                }
            };

    /**
     * Dismisses recents if we are already visible and the intent is to toggle the recents view.
     */
    boolean dismissRecentsToFocusedTask(int logCategory) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.isRecentsActivityVisible()) {
            // If we have a focused Task, launch that Task now
            if (mRecentsView.launchFocusedTask(logCategory)) return true;
        }
        return false;
    }

    /**
     * Dismisses recents back to the launch target task.
     */
    boolean dismissRecentsToLaunchTargetTaskOrHome() {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.isRecentsActivityVisible()) {
            // If we have a focused Task, launch that Task now
            if (mRecentsView.launchPreviousTask()) return true;
            // If none of the other cases apply, then just go Home
            dismissRecentsToHome(true /* animateTaskViews */);
        }
        return false;
    }
    /*prize-measure memory-liufan-2016-01-27-start*/
    private void measureMemory(){
        float availMemory = numericConversions(getAvailMemory(this),MEMORY_UNIT);
        String a = getResources().getString(R.string.recents_avail_memory);
        String space = " ";
        if (usedMemoryTxt != null) {
            usedMemoryTxt.setText(Formatter.formatFileSize(RecentsActivity.this, getAvailMemory(this)) + space + a + space);
        }
        if (mClearRecents != null) {
            mClearRecents.setCurProgress(1-availMemory/(totalMemory+totalMemoryOffset));
        }
    }
    /**
    * Numeric conversions
    */
    public static float numericConversions(long value, String unit) {
        if(unit.equalsIgnoreCase("gb")){
            float result = value/1024f/1024f/1024f;
            result = (float)(Math.round(result*100))/100f;
            return result;
        } else if(unit.equalsIgnoreCase("mb")){
            float result = value/1024f/1024f;
            result = (float)(Math.round(result*100))/100f;
            return result;
        }
        return value;
    }

    /**
    * getCurrentMemory
    */
    public static long getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    /**
    * getTotalMemory
    */
    public static long getTotalMemory() {
        long mTotal = -1;
        String path = "/proc/meminfo";
        String content = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path), 8);
            String line;
            if ((line = br.readLine()) != null) {
                content = line;
            }
            // beginIndex
            int begin = content.indexOf(':');
            // endIndex
            int end = content.indexOf('k');

            content = content.substring(begin + 1, end).trim();
            mTotal = Long.valueOf(content).longValue()*1024;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return mTotal;
    }
    /*prize-measure memory-liufan-2016-01-27-end*/

    /**
     * Dismisses recents if we are already visible and the intent is to toggle the recents view.
     */
    boolean dismissRecentsToFocusedTaskOrHome() {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.isRecentsActivityVisible()) {
            // If we have a focused Task, launch that Task now
            if (mRecentsView.launchFocusedTask(0 /* logCategory */)) return true;
            // If none of the other cases apply, then just go Home
            dismissRecentsToHome(true /* animateTaskViews */);
            return true;
        }
        return false;
    }

    /**
     * Dismisses Recents directly to Home without checking whether it is currently visible.
     */
    void dismissRecentsToHome(boolean animateTaskViews) {
        dismissRecentsToHome(animateTaskViews, null);
    }

    /**
     * Dismisses Recents directly to Home without checking whether it is currently visible.
     *
     * @param overrideAnimation If not null, will override the default animation that is based on
     *                          how Recents was launched.
     */
    void dismissRecentsToHome(boolean animateTaskViews, ActivityOptions overrideAnimation) {
        DismissRecentsToHomeAnimationStarted dismissEvent =
                new DismissRecentsToHomeAnimationStarted(animateTaskViews);
        dismissEvent.addPostAnimationCallback(new LaunchHomeRunnable(mHomeIntent,
                overrideAnimation));
        Recents.getSystemServices().sendCloseSystemWindows(
                BaseStatusBar.SYSTEM_DIALOG_REASON_HOME_KEY);
        EventBus.getDefault().send(dismissEvent);
    }

    /** Dismisses Recents directly to Home if we currently aren't transitioning. */
    boolean dismissRecentsToHomeIfVisible(boolean animated) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.isRecentsActivityVisible()) {
            // Return to Home
            dismissRecentsToHome(animated);
            return true;
        }
        return false;
    }

    /*prize-the click cleanRecents flag -liufan-2016-02-18-start*/
    public static boolean isClickCleanRecents = false;
    private long used_before;
    /*prize-the click cleanRecents flag -liufan-2016-02-18-end*/
    
    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFinishedOnStartup = false;
        Log.d(TAG, "onCreate");
        // In the case that the activity starts up before the Recents component has initialized
        // (usually when debugging/pushing the SysUI apk), just finish this activity.
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp == null) {
            mFinishedOnStartup = true;
            finish();
            return;
        }
        // Register this activity with the event bus
        EventBus.getDefault().register(this, EVENT_BUS_PRIORITY);

        // Initialize the package monitor
        mPackageMonitor = new RecentsPackageMonitor();
        mPackageMonitor.register(this);

        // Set the Recents layout
        /*prize modify by xiarui 2017-11-21 start*/
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams. FLAG_FULLSCREEN);
            setContentView(R.layout.prize_recents);
        } else {
            setContentView(R.layout.recents);
        }
        /*prize modify by xiarui 2017-11-21 end*/
        takeKeyEvents(true);
        mRecentsView = (RecentsView) findViewById(R.id.recents_view);
        mRecentsView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mScrimViews = new SystemBarScrimViews(this);
        getWindow().getAttributes().privateFlags |=
                WindowManager.LayoutParams.PRIVATE_FLAG_FORCE_DECOR_VIEW_VISIBILITY;

        Configuration appConfiguration = Utilities.getAppConfiguration(this);
        mLastDeviceOrientation = appConfiguration.orientation;
        mLastDisplayDensity = appConfiguration.densityDpi;
        mFocusTimerDuration = getResources().getInteger(R.integer.recents_auto_advance_duration);
        mIterateTrigger = new DozeTrigger(mFocusTimerDuration, new Runnable() {
            @Override
            public void run() {
                dismissRecentsToFocusedTask(MetricsEvent.OVERVIEW_SELECT_TIMEOUT);
            }
        });

        // Set the window background
        //prize hide by xiarui 2018-03-03 @{
        //getWindow().setBackgroundDrawable(mRecentsView.getBackgroundScrim());
        //--end by xiarui @}

        // Create the home intent runnable
        mHomeIntent = new Intent(Intent.ACTION_MAIN, null);
        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        // Register the broadcast receiver to handle messages when the screen is turned off
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //filter.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(mSystemBroadcastReceiver, filter);

		/*prize add by xiarui 2017-11-10 start*/
		if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
        	mCleanMemoryLayout = (ViewGroup) findViewById(R.id.clean_memory_layout);
            mMemoryInfoLayout = (ViewGroup) findViewById(R.id.memory_layout);
        	blurLayout = (LinearLayout)findViewById(R.id.blur_layout);
		}
		/*prize add by xiarui 2017-11-10 end*/

		if (PrizeOption.PRIZE_SPLIT_SCREEN_NOT_CLR_RECENT) {
            mSelSpliteApp = (TextView) findViewById(R.id.sel_splite_app);
        }

        /*prize-clean-liufan-2015-11-04-start*/
        usedMemoryTxt = (TextView)findViewById(R.id.used_memory_txt);
        allMemoryTxt = (TextView)findViewById(R.id.all_memory_txt);
        ((TextView)findViewById(R.id.memory_spilt_txt)).setText("/");
        double d = (double)(getTotalMemory()/1024f/1024f/1024f);
        totalMemoryB = (Math.round(d))*1024*1024*1024;//getTotalMemory();
        totalMemory = numericConversions(totalMemoryB,MEMORY_UNIT);
        Log.d(TAG,"tm : " + totalMemory);
        totalMemoryOffset = measureOffset(totalMemory);
        Log.d(TAG,"tm_Offset : " + totalMemoryOffset);
        final String space = " ";
        String total = getResources().getString(R.string.recents_total_memory);
        final String a = getResources().getString(R.string.recents_avail_memory);
        allMemoryTxt.setText(total + space + totalMemory + space + MEMORY_UNIT);

        mClearRecents = (ArcView)findViewById(R.id.clear_recents);
        mClearRecents.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mClearRecents.getAlpha() != 1f) {
                    return;
                }
                mClearRecents.setEnabled(false);
                //Recents.isClearRecentsActivity = true;
                //final long used_before = getAvailMemory(RecentsActivity.this);
                used_before = getAvailMemory(RecentsActivity.this);
                final float availMemory = numericConversions(used_before, MEMORY_UNIT);
                float ratio = 1 - availMemory / (totalMemory+totalMemoryOffset);
                mClearRecents.switchingNewProgress(ratio, 600, new Runnable(){
                    public void run(){
                        usedMemoryTxt.setText(Formatter.formatFileSize(RecentsActivity.this,used_before) + space + a + space);
                        //mRecentsView.onSomeOneLockChanged(true);
                        isClickCleanRecents = true;
                        /*prize add by xiarui 2017-12-05 start*/
                        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                        	EventBus.getDefault().send(new SendClearTaskServiceEvent());
                            EventBus.getDefault().send(new DismissAllTaskViewsEvent());//post & send
                            mClearRecents.setEnabled(true);
                            //Recents.isClearRecentsActivity = false;
                        } else {
                            mRecentsView.dismissAllTasksAnimated(new Runnable() {
                                public void run() {
                                    final long used_after = getAvailMemory(RecentsActivity.this);
                                    final float availMemory = numericConversions(used_after, MEMORY_UNIT);
                                    usedMemoryTxt.setText(Formatter.formatFileSize(RecentsActivity.this, used_after) + space + a + space);
                                    //mRecentsView.onSomeOneLockChanged(true);
                                    resetData();
                                    long rel = used_after - used_before;
                                    rel = numericConversions(rel, MEMORY_UNIT_MB) < 1 ? 0 : rel;
                                    showCleanResultByToast(RecentsActivity.this,
                                            numericConversions(rel, MEMORY_UNIT_MB) + MEMORY_UNIT_MB,
                                            Formatter.formatFileSize(RecentsActivity.this, used_after));
                                    isClickCleanRecents = false;
                                    Recents.isClearRecentsActivity = false;
                                }
                            });
                            Recents.isClearRecentsActivity = false;
                            mRecentsView.postDelayed(new Runnable() {
                                public void run() {
                                    mRecentsView.shouldFinishActivity();
                                }
                            }, 200);
                        }
                        /*prize add by xiarui 2017-12-05 end*/
                    }
                });
            }
        });
        /*prize-clean-liufan-2015-11-04-end*/

        getWindow().addPrivateFlags(LayoutParams.PRIVATE_FLAG_NO_MOVE_ANIMATION);

        // Nav bar color customized feature. prize-linkh-2017.09.07 @{
        /** prize delete by xiarui 2017-11-24 see {@link RecentsActivity#onConfigurationChanged(Configuration)} **/
        //if(PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
        //    getWindow().setDisableCustNavBarColor(true);
        //} // @}

        // Reload the stack view
        reloadStackView();
    }

    private float measureOffset(float memory){
        if(memory >= 3){
            return -0.5f;
        }
        return -0.3f;
    }

    /**prize add by xiarui 2017-12-05 start**/
    public final void onBusEvent(PrizeShowCleanResultByToastEvent event) {
        final String space = " ";
        final String a = getResources().getString(R.string.recents_avail_memory);

        final long used_after = getAvailMemory(RecentsActivity.this);
        final float availMemory = numericConversions(used_after,MEMORY_UNIT);
        usedMemoryTxt.setText(Formatter.formatFileSize(RecentsActivity.this, used_after) + space + a + space);
        //mRecentsView.onSomeOneLockChanged(true);
        resetData();
        long rel = used_after - used_before;
        rel = numericConversions(rel, MEMORY_UNIT_MB) < 1 ? 0 : rel;
        showCleanResultByToast(RecentsActivity.this, numericConversions(rel, MEMORY_UNIT_MB) + MEMORY_UNIT_MB,
        Formatter.formatFileSize(RecentsActivity.this,used_after));
        isClickCleanRecents = false;
        Recents.isClearRecentsActivity = false;
    }
    /**prize add by xiarui 2017-12-05 end**/

    /*prize-add blur function-liufan-2015-11-04-start*/

    public static void showCleanResultByToast(final Context context, final String release, final String available){
        String rel_txt = context.getResources().getString(R.string.recents_clean_release);
        String avail_txt = context.getResources().getString(R.string.recents_clean_available);
        Toast.makeText(context, rel_txt + release + ", " + avail_txt + available, Toast.LENGTH_SHORT).show();
    }

    Runnable blurRunnable = new Runnable() {
        public void run() {
            showBlurLayout();
        }
    };

    /**
     * 方法描述：显示模糊的背景
     */
    private void showBlurLayout(){
        Bitmap bitmap = getBlurWallpaper();
        if(bitmap == null){
            return ;
        }
        BitmapDrawable bd = new BitmapDrawable(this.getResources(), bitmap);
        blurLayout.setBackground(bd);
        /*if(blurLayout.getVisibility() == View.VISIBLE && blurLayout.getAlpha() == 1){
            return ;
        }
        ValueAnimator showBlurLayoutAnimation = ValueAnimator.ofFloat(0f, 1f);
        showBlurLayoutAnimation.setDuration(400);
        showBlurLayoutAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                blurLayout.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        showBlurLayoutAnimation.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                blurLayout.setAlpha(1f);
                blurLayout.setVisibility(View.VISIBLE);
            }
        });
        showBlurLayoutAnimation.start();*/
    }

    /**
     * 方法描述：显示模糊的背景
     */
    private void cancelBlurLayout(){
        float alpha = blurLayout.getAlpha();
        if(alpha == 0){
            return ;
        }
        ValueAnimator cancelBlurLayoutAnimation = ValueAnimator.ofFloat(1f, 0f);
        cancelBlurLayoutAnimation.setDuration(400);
        cancelBlurLayoutAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                blurLayout.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        cancelBlurLayoutAnimation.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                blurLayout.setAlpha(0f);
                blurLayout.setBackground(null);
            }
        });
        cancelBlurLayoutAnimation.start();
    }

    /**
     * 方法描述：得到模糊的壁纸图片
     */
    private Bitmap getBlurWallpaper(){
        Bitmap bitmap = null;
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        return blur(bitmap);
    }

    /**
     * 方法描述：模糊图片算法    */
    public static Bitmap blur(Bitmap bitmap){
        if(bitmap!=null){
            bitmap = BlurPic.blurScale(bitmap);
            if(bitmap!=null){
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(0x66000000);
            }
        }
        return bitmap;
    }
    /*prize-add blur function-liufan-2015-11-04-end*/

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        // Notify that recents is now visible
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, true));
        MetricsLogger.visible(this, MetricsEvent.OVERVIEW_ACTIVITY);
        /*prize-set blur background-liufan-2015-11-04-start*/
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            mHandler.postDelayed(blurRunnable, 0);
        }
        /*prize-set blur background-liufan-2015-11-04-end*/
        // Notify of the next draw
        mRecentsView.getViewTreeObserver().addOnPreDrawListener(mRecentsDrawnEventListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        /*prize add by xiarui for Bug#44285 2017-12-12 start*/
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            LogUtils.d("xx", "onNewIntent  mReceivedNewIntent = " + mReceivedNewIntent);
            if (mReceivedNewIntent) {
                mNeedSendAnimationCompletedEvent = true;
            } else {
                mNeedSendAnimationCompletedEvent = false;
            }
        }
        /*prize add by xiarui for Bug#44285 2017-12-12 end*/

        mReceivedNewIntent = true;

        // Reload the stack view
        reloadStackView();
    }

    /**
     * Reloads the stack views upon launching Recents.
     */
    private void reloadStackView() {
        // If the Recents component has preloaded a load plan, then use that to prevent
        // reconstructing the task stack
        RecentsTaskLoader loader = Recents.getTaskLoader();
        RecentsTaskLoadPlan loadPlan = RecentsImpl.consumeInstanceLoadPlan();
        if (loadPlan == null) {
            loadPlan = loader.createLoadPlan(this);
        }

        // Start loading tasks according to the load plan
        RecentsConfiguration config = Recents.getConfiguration();
        RecentsActivityLaunchState launchState = config.getLaunchState();
        if (!loadPlan.hasTasks()) {
            loader.preloadTasks(loadPlan, launchState.launchedToTaskId,
                    !launchState.launchedFromHome);
        }

        RecentsTaskLoadPlan.Options loadOpts = new RecentsTaskLoadPlan.Options();
        loadOpts.runningTaskId = launchState.launchedToTaskId;
        loadOpts.numVisibleTasks = launchState.launchedNumVisibleTasks;
        loadOpts.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
        loader.loadTasks(this, loadPlan, loadOpts);
        TaskStack stack = loadPlan.getTaskStack();
        mRecentsView.onReload(mIsVisible, stack.getTaskCount() == 0);
        mRecentsView.updateStack(stack, true /* setStackViewTasks */);

        /*prize-show recents view by count-liufan-2016-11-15-start*/
        showRecentsViewbyCount(stack);
        isResetData = false;
        if(mClearRecents != null) mClearRecents.setEnabled(true);
        /*prize-show recents view by count-liufan-2016-11-15-end*/

        // Update the nav bar scrim, but defer the animation until the enter-window event
        boolean animateNavBarScrim = !launchState.launchedViaDockGesture;
        mScrimViews.updateNavBarScrim(animateNavBarScrim, stack.getTaskCount() > 0, null);

        // If this is a new instance relaunched by AM, without going through the normal mechanisms,
        // then we have to manually trigger the enter animation state
        boolean wasLaunchedByAm = !launchState.launchedFromHome &&
                !launchState.launchedFromApp;
        LogUtils.d("xx", "reloadStackView  wasLaunchedByAm = " + wasLaunchedByAm);
        if (wasLaunchedByAm) {
            EventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
        }

        // Keep track of whether we launched from the nav bar button or via alt-tab
        if (launchState.launchedWithAltTab) {
            MetricsLogger.count(this, "overview_trigger_alttab", 1);
        } else {
            MetricsLogger.count(this, "overview_trigger_nav_btn", 1);
        }

        // Keep track of whether we launched from an app or from home
        if (launchState.launchedFromApp) {
            Task launchTarget = stack.getLaunchTarget();
            int launchTaskIndexInStack = launchTarget != null
                    ? stack.indexOfStackTask(launchTarget)
                    : 0;
            MetricsLogger.count(this, "overview_source_app", 1);
            // If from an app, track the stack index of the app in the stack (for affiliated tasks)
            MetricsLogger.histogram(this, "overview_source_app_index", launchTaskIndexInStack);
        } else {
            MetricsLogger.count(this, "overview_source_home", 1);
        }

        // Keep track of the total stack task count
        int taskCount = mRecentsView.getStack().getTaskCount();
        MetricsLogger.histogram(this, "overview_task_count", taskCount);

        // After we have resumed, set the visible state until the next onStop() call
        mIsVisible = true;
    }

    /*prize-show recents view by count-liufan-2016-11-15-start*/
    public void showRecentsViewbyCount(TaskStack stack){
        //updateUI();
    }
    /*prize-show recents view by count-liufan-2016-11-15-end*/

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        LogUtils.d("xx", "onEnterAnimationComplete  mReceivedNewIntent = " + mReceivedNewIntent);
        // Workaround for b/28705801, on first docking, we may receive the enter animation callback
        // before the first layout, so in such cases, send the event on the next frame after all
        // the views are laid out and attached (and registered to the EventBus).
        mHandler.removeCallbacks(mSendEnterWindowAnimationCompleteRunnable);
        if (!mReceivedNewIntent) {
            mHandler.post(mSendEnterWindowAnimationCompleteRunnable);
        } else {
            mSendEnterWindowAnimationCompleteRunnable.run();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mIgnoreAltTabRelease = false;
        mIterateTrigger.stopDozing();
    }

    /**prize add by xiarui 2017-12-11 start**/

    @Override
    protected void onResume() {
        super.onResume();
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            Log.d(TAG, "onResume -------- mIsVisible = " + mIsVisible);
            int numStackTasks = mRecentsView.getStack().getStackTaskCount();
            updateUI(numStackTasks);
        }
    }

    private void updateUI(int numStackTasks) {
        /*prize add by xiarui 2017-11-14 start*/
        //Configuration config = getResources().getConfiguration();
        int orientation = Utilities.getAppConfiguration(this).orientation;
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (mCleanMemoryLayout != null) {
                    mCleanMemoryLayout.setPadding(0, mCleanMemoryLayout.getPaddingTop(), 0, 20);
                }
                if (mMemoryInfoLayout != null) {
                    mMemoryInfoLayout.setPadding(0, 0, 0, 0);
                }
            } else {
                if (mCleanMemoryLayout != null) {
                    int bottom = getResources().getDimensionPixelSize(R.dimen.recents_memerytext_bottom);
                    mCleanMemoryLayout.setPadding(0, mCleanMemoryLayout.getPaddingTop(), 0, bottom);
                }
                if (mMemoryInfoLayout != null) {
                    int top = getResources().getDimensionPixelSize(R.dimen.memory_layout_top);
                    mMemoryInfoLayout.setPadding(0, top, 0, 0);
                }
            }
        }

        if (isInMultiWindowMode()) {
            if (PrizeOption.PRIZE_SPLIT_SCREEN_NOT_CLR_RECENT) {
                mSelSpliteApp.setVisibility(View.VISIBLE);
                if (PrizeOption.PRIZE_NOTCH_SCREEN) {
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mSelSpliteApp.setPadding(0, 100, 0, 0);
                    } else {
                        mSelSpliteApp.setPadding(0, 30, 0, 0);
                    }
                }
            }
            if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                if (mCleanMemoryLayout != null) {
                    mCleanMemoryLayout.setVisibility(View.GONE);
                }
                getWindow().clearFlags(View.NAVIGATION_BAR_TRANSLUCENT | View.NAVIGATION_BAR_TRANSPARENT);
            }
        } else {
            if (PrizeOption.PRIZE_SPLIT_SCREEN_NOT_CLR_RECENT) {
                mSelSpliteApp.setVisibility(View.GONE);
            }
            if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                if (mCleanMemoryLayout != null) {
                    Log.d(TAG, "updateUI numStackTasks : " + numStackTasks);
                    if (numStackTasks > 0) {
                        mCleanMemoryLayout.setVisibility(View.VISIBLE);
                        measureMemory();
                    } else {
                        mCleanMemoryLayout.setVisibility(View.GONE);
                    }
                }
                getWindow().addFlags(View.NAVIGATION_BAR_TRANSLUCENT | View.NAVIGATION_BAR_TRANSPARENT);
            }
        }

        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            //prize add by xiarui for Bug#43241 start@{
            SystemServicesProxy ssp = Recents.getSystemServices();
            boolean hasDocked = ssp.hasDockedTask();
            //Log.d(TAG, "updateUI -------- mIsVisible = " + mIsVisible + " , orientation = " + orientation + " , hasDocked = " + hasDocked);
            if (!hasDocked) {
                Window window = getWindow();
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    //Log.d(TAG, "updateUI -------- setNavigationBarColor  0x88000000");
                    window.setNavigationBarColor(0x88000000);
                } else {
                    //Log.d(TAG, "updateUI -------- setNavigationBarColor  0x00000000");
                    window.setNavigationBarColor(0x00000000);
                }
            }
            //@}
            //prize add by xiarui 2017-12-05 @{
            if (mClearRecents != null) {
                mClearRecents.setEnabled(true);
            }
            //@}
        }
    }

    /**prize add by xiarui 2017-12-11 end**/

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Notify of the config change
        Configuration newDeviceConfiguration = Utilities.getAppConfiguration(this);
        int numStackTasks = mRecentsView.getStack().getStackTaskCount();
        EventBus.getDefault().send(new ConfigurationChangedEvent(false /* fromMultiWindow */,
                mLastDeviceOrientation != newDeviceConfiguration.orientation,
                mLastDisplayDensity != newDeviceConfiguration.densityDpi, numStackTasks > 0));
        mLastDeviceOrientation = newDeviceConfiguration.orientation;
        mLastDisplayDensity = newDeviceConfiguration.densityDpi;

        /*prize add by xiarui 2017-11-14 start*/
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            Log.d(TAG, "onConfigurationChanged -------- mIsVisible = " + mIsVisible + " , newConfig.orientation = " + newConfig.orientation);
            Log.d(TAG, "onConfigurationChanged numStackTasks : " + numStackTasks);
            updateUI(numStackTasks);
        }
        /*prize add by xiarui 2017-11-14 end*/
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);

        // Reload the task stack completely
        RecentsConfiguration config = Recents.getConfiguration();
        RecentsActivityLaunchState launchState = config.getLaunchState();
        RecentsTaskLoader loader = Recents.getTaskLoader();
        RecentsTaskLoadPlan loadPlan = loader.createLoadPlan(this);
        loader.preloadTasks(loadPlan, -1 /* runningTaskId */,
                false /* includeFrontMostExcludedTask */);

        RecentsTaskLoadPlan.Options loadOpts = new RecentsTaskLoadPlan.Options();
        loadOpts.numVisibleTasks = launchState.launchedNumVisibleTasks;
        loadOpts.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
        loader.loadTasks(this, loadPlan, loadOpts);

        TaskStack stack = loadPlan.getTaskStack();
        int numStackTasks = stack.getStackTaskCount();
        boolean showDeferredAnimation = numStackTasks > 0;
        //prize add by xiarui 2018-04-25 start @{
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            Log.d(TAG, "onMultiWindowModeChanged numStackTasks : " + numStackTasks);
            updateUI(numStackTasks);
        }
        //@}
        EventBus.getDefault().send(new ConfigurationChangedEvent(true /* fromMultiWindow */,
                false /* fromDeviceOrientationChange */, false /* fromDisplayDensityChange */,
                numStackTasks > 0));
        EventBus.getDefault().send(new MultiWindowStateChangedEvent(isInMultiWindowMode,
                showDeferredAnimation, stack));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        // Notify that recents is now hidden
        mIsVisible = false;
        mReceivedNewIntent = false;
		
        /*prize-delete data-liufan-2016-11-18-start*/
        SystemServicesProxy ssp = Recents.getSystemServices();
        Log.d(TAG , "onStop ssp.isRecentsActivityVisible() = " + ssp.isRecentsActivityVisible());
        if(!isResetData && !isClickCleanRecents && !ssp.isRecentsActivityVisible()) { //prize modify by xiarui 2017-12-27 for Bug#46846
            resetData();
        }

        /*prize-don't allow to enter RecentsActivity when is cleaning,bugid:18562-liufan-2016-07-20-start*/
        //Recents.isStackTaskRemoved = false;
        /*prize-don't allow to enter RecentsActivity when is cleaning,bugid:18562-liufan-2016-07-20-end*/
        /*EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, false));
        MetricsLogger.hidden(this, MetricsEvent.OVERVIEW_ACTIVITY);

        // Workaround for b/22542869, if the RecentsActivity is started again, but without going
        // through SystemUI, we need to reset the config launch flags to ensure that we do not
        // wait on the system to send a signal that was never queued.
        RecentsConfiguration config = Recents.getConfiguration();
        RecentsActivityLaunchState launchState = config.getLaunchState();
        launchState.reset();*/

        /*prize-delete data-liufan-2016-11-18-end*/
    }
	
    /*prize-delete data-liufan-2016-11-18-start*/
    private boolean isResetData = false;
    public void resetData(){
        isResetData = true;
        EventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, false));
        MetricsLogger.hidden(this, MetricsEvent.OVERVIEW_ACTIVITY);

        // Workaround for b/22542869, if the RecentsActivity is started again, but without going
        // through SystemUI, we need to reset the config launch flags to ensure that we do not
        // wait on the system to send a signal that was never queued.
        RecentsConfiguration config = Recents.getConfiguration();
        RecentsActivityLaunchState launchState = config.getLaunchState();
        launchState.reset();
    }
    /*prize-delete data-liufan-2016-11-18-end*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        // In the case that the activity finished on startup, just skip the unregistration below
        if (mFinishedOnStartup) {
            return;
        }

        // Unregister the system broadcast receivers
        unregisterReceiver(mSystemBroadcastReceiver);

        // Unregister any broadcast receivers for the task loader
        mPackageMonitor.unregister();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(mScrimViews, EVENT_BUS_PRIORITY);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(mScrimViews);
    }

    @Override
    public void onTrimMemory(int level) {
        RecentsTaskLoader loader = Recents.getTaskLoader();
        if (loader != null) {
            loader.onTrimMemory(level);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_TAB: {
                int altTabKeyDelay = getResources().getInteger(R.integer.recents_alt_tab_key_delay);
                boolean hasRepKeyTimeElapsed = (SystemClock.elapsedRealtime() -
                        mLastTabKeyEventTime) > altTabKeyDelay;
                if (event.getRepeatCount() <= 0 || hasRepKeyTimeElapsed) {
                    // Focus the next task in the stack
                    final boolean backward = event.isShiftPressed();
                    if (backward) {
                        EventBus.getDefault().send(new FocusPreviousTaskViewEvent());
                    } else {
                        EventBus.getDefault().send(
                                new FocusNextTaskViewEvent(0 /* timerIndicatorDuration */));
                    }
                    mLastTabKeyEventTime = SystemClock.elapsedRealtime();

                    // In the case of another ALT event, don't ignore the next release
                    if (event.isAltPressed()) {
                        mIgnoreAltTabRelease = false;
                    }
                }
                return true;
            }
            case KeyEvent.KEYCODE_DPAD_UP: {
                EventBus.getDefault().send(
                        new FocusNextTaskViewEvent(0 /* timerIndicatorDuration */));
                return true;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN: {
                EventBus.getDefault().send(new FocusPreviousTaskViewEvent());
                return true;
            }
            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_FORWARD_DEL: {
                if (event.getRepeatCount() <= 0) {
                    EventBus.getDefault().send(new DismissFocusedTaskViewEvent());

                    // Keep track of deletions by keyboard
                    MetricsLogger.histogram(this, "overview_task_dismissed_source",
                            Constants.Metrics.DismissSourceKeyboard);
                    return true;
                }
            }
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onUserInteraction() {
        EventBus.getDefault().send(mUserInteractionEvent);
    }

    /*prize add by xiarui for Bug#43910 2017-12-01 start*/
    protected long mLastToggleTime;
    private final static int MIN_TOGGLE_DELAY_MS = 350;
    /*prize add by xiarui for Bug#43910 2017-12-01 end*/

    @Override
    public void onBackPressed() {
        // Back behaves like the recents button so just trigger a toggle event

        /*prize add by xiarui for Bug#43910 2017-12-01 start*/
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            long elapsedTime = SystemClock.elapsedRealtime() - mLastToggleTime;
            if (elapsedTime < MIN_TOGGLE_DELAY_MS) {
                return;
            }

            EventBus.getDefault().send(new ToggleRecentsEvent());

            mLastToggleTime = SystemClock.elapsedRealtime();
        } else {
            EventBus.getDefault().send(new ToggleRecentsEvent());
        }
        /*prize add by xiarui for Bug#43910 2017-12-01 end*/
    }

    /**** EventBus events ****/

    public final void onBusEvent(ToggleRecentsEvent event) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (launchState.launchedFromHome) {
            dismissRecentsToHome(true /* animateTaskViews */);
        } else {
            dismissRecentsToLaunchTargetTaskOrHome();
        }
    }

    public final void onBusEvent(IterateRecentsEvent event) {
        final RecentsDebugFlags debugFlags = Recents.getDebugFlags();

        // Start dozing after the recents button is clicked
        int timerIndicatorDuration = 0;
        if (debugFlags.isFastToggleRecentsEnabled()) {
            timerIndicatorDuration = getResources().getInteger(
                    R.integer.recents_subsequent_auto_advance_duration);

            mIterateTrigger.setDozeDuration(timerIndicatorDuration);
            if (!mIterateTrigger.isDozing()) {
                mIterateTrigger.startDozing();
            } else {
                mIterateTrigger.poke();
            }
        }

        // Focus the next task
        EventBus.getDefault().send(new FocusNextTaskViewEvent(timerIndicatorDuration));

        MetricsLogger.action(this, MetricsEvent.ACTION_OVERVIEW_PAGE);
    }

    public final void onBusEvent(UserInteractionEvent event) {
        // Stop the fast-toggle dozer
        mIterateTrigger.stopDozing();
    }

    public final void onBusEvent(HideRecentsEvent event) {
        if (event.triggeredFromAltTab) {
            // If we are hiding from releasing Alt-Tab, dismiss Recents to the focused app
            if (!mIgnoreAltTabRelease) {
                dismissRecentsToFocusedTaskOrHome();
            }
        } else if (event.triggeredFromHomeKey) {
            dismissRecentsToHome(true /* animateTaskViews */);

            // Cancel any pending dozes
            EventBus.getDefault().send(mUserInteractionEvent);
        } else {
            // Do nothing
        }
    }

    public final void onBusEvent(EnterRecentsWindowLastAnimationFrameEvent event) {
        EventBus.getDefault().send(new UpdateFreeformTaskViewVisibilityEvent(true));
        mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        mRecentsView.invalidate();
    }

    public final void onBusEvent(ExitRecentsWindowFirstAnimationFrameEvent event) {
        if (mRecentsView.isLastTaskLaunchedFreeform()) {
            EventBus.getDefault().send(new UpdateFreeformTaskViewVisibilityEvent(false));
        }
        mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        mRecentsView.invalidate();
    }

    public final void onBusEvent(DockedFirstAnimationFrameEvent event) {
        mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        mRecentsView.invalidate();
    }

    public final void onBusEvent(CancelEnterRecentsWindowAnimationEvent event) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        int launchToTaskId = launchState.launchedToTaskId;
        if (launchToTaskId != -1 &&
                (event.launchTask == null || launchToTaskId != event.launchTask.key.id)) {
            SystemServicesProxy ssp = Recents.getSystemServices();
            ssp.cancelWindowTransition(launchState.launchedToTaskId);
            ssp.cancelThumbnailTransition(getTaskId());
        }
    }

    public final void onBusEvent(ShowApplicationInfoEvent event) {
        // Create a new task stack with the application info details activity
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", event.task.key.getComponent().getPackageName(), null));
        intent.setComponent(intent.resolveActivity(getPackageManager()));
        TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(intent).startActivities(null,
                        new UserHandle(event.task.key.userId));

        // Keep track of app-info invocations
        MetricsLogger.count(this, "overview_app_info", 1);
    }

    public final void onBusEvent(ShowIncompatibleAppOverlayEvent event) {
        if (mIncompatibleAppOverlay == null) {
            mIncompatibleAppOverlay = Utilities.findViewStubById(this,
                    R.id.incompatible_app_overlay_stub).inflate();
            mIncompatibleAppOverlay.setWillNotDraw(false);
            mIncompatibleAppOverlay.setVisibility(View.VISIBLE);
        }
        mIncompatibleAppOverlay.animate()
                .alpha(1f)
                .setDuration(INCOMPATIBLE_APP_ALPHA_DURATION)
                .setInterpolator(Interpolators.ALPHA_IN)
                .start();
    }

    public final void onBusEvent(HideIncompatibleAppOverlayEvent event) {
        if (mIncompatibleAppOverlay != null) {
            mIncompatibleAppOverlay.animate()
                    .alpha(0f)
                    .setDuration(INCOMPATIBLE_APP_ALPHA_DURATION)
                    .setInterpolator(Interpolators.ALPHA_OUT)
                    .start();
        }
    }

    public final void onBusEvent(DeleteTaskDataEvent event) {
        // Remove any stored data from the loader
        RecentsTaskLoader loader = Recents.getTaskLoader();
        loader.deleteTaskData(event.task, false);

        // Remove the task from activity manager
        SystemServicesProxy ssp = Recents.getSystemServices();
        ssp.removeTask(event.task.key.id);
        //prize add by xiarui 2018-02-05 @{
        if (event.task != null) {
            event.task.unlockByRemoveTask(getApplicationContext());
        }
        Log.d(TAG, "====DeleteTaskDataEvent===");
        //---end@}
    }

    public final void onBusEvent(AllTaskViewsDismissedEvent event) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.hasDockedTask()) {
            mRecentsView.showEmptyView(event.msgResId);
        } else {
            /*prize modify by xiarui 2018-01-02 don't remove current task start*/
            if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
                if (launchState.launchedFromHome) {
                    dismissRecentsToHome(true /* animateTaskViews */);
                } else {
                    dismissRecentsToLaunchTargetTaskOrHome();
                }
            } else {
                // Just go straight home (no animation necessary because there are no more task views)
                dismissRecentsToHome(false /* animateTaskViews */);
            }
            /*prize modify by xiarui 2018-01-02 don't remove current task end*/
        }

        // Keep track of all-deletions
        MetricsLogger.count(this, "overview_task_all_dismissed", 1);
    }

    public final void onBusEvent(LaunchTaskSucceededEvent event) {
        MetricsLogger.histogram(this, "overview_task_launch_index", event.taskIndexFromStackFront);
    }

    public final void onBusEvent(LaunchTaskFailedEvent event) {
        // Return to Home
        dismissRecentsToHome(true /* animateTaskViews */);

        MetricsLogger.count(this, "overview_task_launch_failed", 1);
    }

    public final void onBusEvent(ScreenPinningRequestEvent event) {
        MetricsLogger.count(this, "overview_screen_pinned", 1);
    }

    public final void onBusEvent(DebugFlagsChangedEvent event) {
        // Just finish recents so that we can reload the flags anew on the next instantiation
        finish();
    }

    public final void onBusEvent(StackViewScrolledEvent event) {
        // Once the user has scrolled while holding alt-tab, then we should ignore the release of
        // the key
        mIgnoreAltTabRelease = true;
    }

    public final void onBusEvent(final DockedTopTaskEvent event) {
        mRecentsView.getViewTreeObserver().addOnPreDrawListener(mRecentsDrawnEventListener);
        mRecentsView.invalidate();
    }

    @Override
    public boolean onPreDraw() {
        mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
        // We post to make sure that this information is delivered after this traversals is
        // finished.
        mRecentsView.post(new Runnable() {
            @Override
            public void run() {
                Recents.getSystemServices().endProlongedAnimations();
            }
        });
        return true;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        EventBus.getDefault().dump(prefix, writer);
        Recents.getTaskLoader().dump(prefix, writer);

        String id = Integer.toHexString(System.identityHashCode(this));

        writer.print(prefix); writer.print(TAG);
        writer.print(" visible="); writer.print(mIsVisible ? "Y" : "N");
        writer.print(" [0x"); writer.print(id); writer.print("]");
        writer.println();

        if (mRecentsView != null) {
            mRecentsView.dump(prefix, writer);
        }
    }
    /*prize-add by lihuangyuan,for fingerapplock-2017-07-04-start*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
		if(resultCode == 10001)
	       {
			mRecentsView.doLaunchRecentTasksByAppLock();
		}
    }
    /*prize-add by lihuangyuan,for fingerapplock-2017-07-04-end*/
}
