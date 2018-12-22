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
 * limitations under the License.
 */

package com.android.systemui.recents.views;

import static android.app.ActivityManager.StackId.FREEFORM_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.FULLSCREEN_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.INVALID_STACK_ID;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.IntDef;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.MutableBoolean;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.RecentsDebugFlags;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.EnterRecentsTaskStackAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.HideStackActionButtonEvent;
import com.android.systemui.recents.events.activity.IterateRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchNextTaskRequestEvent;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.activity.LaunchTaskStartedEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;
import com.android.systemui.recents.events.activity.ShowStackActionButtonEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.DismissTaskViewEvent;
import com.android.systemui.recents.events.ui.RecentsGrowingEvent;
import com.android.systemui.recents.events.ui.TaskViewDismissedEvent;
import com.android.systemui.recents.events.ui.UpdateFreeformTaskViewVisibilityEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartInitializeDropTargetsEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusNextTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusPreviousTaskViewEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;

import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
/*prize-import-liufan-2015-11-03-start*/
import java.util.HashMap;
import android.view.Display;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import android.util.Log;
import com.android.systemui.recents.RecentsActivity;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager;
import java.util.List;
import android.provider.Settings;
import com.android.systemui.recents.Recents;
import android.net.Uri;
import android.database.Cursor;
import com.mediatek.common.prizeoption.PrizeOption;
import android.util.PrizeAppInstanceUtils;
import android.app.ActivityManagerNative;
import android.os.UserHandle;
import android.os.RemoteException;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import android.app.StatusBarManager;
/*prize-import-liufan-2015-11-03-end*/
/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
//import android.os.WhiteListManager;-temp-delete-
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/

/*prize add by xiarui 2017-11-20 start*/
import com.mediatek.common.prizeoption.PrizeOption;
import android.os.Handler;
import android.os.Message;
import com.android.systemui.recents.LogUtils;
import com.android.systemui.recents.events.ui.PrizeKillAllProcessEvent;
import com.android.systemui.recents.events.ui.PrizeShowCleanResultByToastEvent;
import android.os.SystemClock;
import android.content.Intent;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.RecentsImpl;
import com.android.systemui.recents.utils.AppLockManager;
import com.android.systemui.recents.events.ui.SendClearTaskServiceEvent;
/*prize add by xiarui 2017-11-20 end*/


/* The visual representation of a task stack view */
public class TaskStackView extends FrameLayout implements TaskStack.TaskStackCallbacks,
        TaskView.TaskViewCallbacks, TaskStackViewScroller.TaskStackViewScrollerCallbacks,
        TaskStackLayoutAlgorithm.TaskStackLayoutAlgorithmCallbacks,
        ViewPool.ViewPoolConsumer<TaskView, Task> {

    private static final String TAG = "TaskStackView";

    // The thresholds at which to show/hide the stack action button.
    private static final float SHOW_STACK_ACTION_BUTTON_SCROLL_THRESHOLD = 0.3f;
    private static final float HIDE_STACK_ACTION_BUTTON_SCROLL_THRESHOLD = 0.3f;

    public static final int DEFAULT_SYNC_STACK_DURATION = 200;
    public static final int SLOW_SYNC_STACK_DURATION = 250;
    private static final int DRAG_SCALE_DURATION = 175;
    static final float DRAG_SCALE_FACTOR = 1.05f;

    private static final int LAUNCH_NEXT_SCROLL_BASE_DURATION = 216;
    private static final int LAUNCH_NEXT_SCROLL_INCR_DURATION = 32;

    // The actions to perform when resetting to initial state,
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INITIAL_STATE_UPDATE_NONE, INITIAL_STATE_UPDATE_ALL, INITIAL_STATE_UPDATE_LAYOUT_ONLY})
    public @interface InitialStateAction {}
    /** Do not update the stack and layout to the initial state. */
    private static final int INITIAL_STATE_UPDATE_NONE = 0;
    /** Update both the stack and layout to the initial state. */
    private static final int INITIAL_STATE_UPDATE_ALL = 1;
    /** Update only the layout to the initial state. */
    private static final int INITIAL_STATE_UPDATE_LAYOUT_ONLY = 2;

    private LayoutInflater mInflater;
    private TaskStack mStack = new TaskStack();
    @ViewDebug.ExportedProperty(deepExport=true, prefix="layout_")
    TaskStackLayoutAlgorithm mLayoutAlgorithm;
    // The stable layout algorithm is only used to calculate the task rect with the stable bounds
    private TaskStackLayoutAlgorithm mStableLayoutAlgorithm;
    @ViewDebug.ExportedProperty(deepExport=true, prefix="scroller_")
    private TaskStackViewScroller mStackScroller;
    @ViewDebug.ExportedProperty(deepExport=true, prefix="touch_")
    private TaskStackViewTouchHandler mTouchHandler;
    private TaskStackAnimationHelper mAnimationHelper;
    private GradientDrawable mFreeformWorkspaceBackground;
    private ObjectAnimator mFreeformWorkspaceBackgroundAnimator;
    private ViewPool<TaskView, Task> mViewPool;

    private ArrayList<TaskView> mTaskViews = new ArrayList<>();
    private ArrayList<TaskViewTransform> mCurrentTaskTransforms = new ArrayList<>();
    private ArraySet<Task.TaskKey> mIgnoreTasks = new ArraySet<>();
    private AnimationProps mDeferredTaskViewLayoutAnimation = null;

    @ViewDebug.ExportedProperty(deepExport=true, prefix="doze_")
    private DozeTrigger mUIDozeTrigger;
    @ViewDebug.ExportedProperty(deepExport=true, prefix="focused_task_")
    private Task mFocusedTask;

    private int mTaskCornerRadiusPx;
    private int mDividerSize;
    private int mStartTimerIndicatorDuration;

    @ViewDebug.ExportedProperty(category="recents")
    private boolean mTaskViewsClipDirty = true;
    @ViewDebug.ExportedProperty(category="recents")
    private boolean mAwaitingFirstLayout = true;
    @ViewDebug.ExportedProperty(category="recents")
    private boolean mLaunchNextAfterFirstMeasure = false;
    @ViewDebug.ExportedProperty(category="recents")
    @InitialStateAction
    private int mInitialState = INITIAL_STATE_UPDATE_ALL;
    @ViewDebug.ExportedProperty(category="recents")
    private boolean mInMeasureLayout = false;
    @ViewDebug.ExportedProperty(category="recents")
    private boolean mEnterAnimationComplete = false;
    @ViewDebug.ExportedProperty(category="recents")
    boolean mTouchExplorationEnabled;
    @ViewDebug.ExportedProperty(category="recents")
    boolean mScreenPinningEnabled;

    // The stable stack bounds are the full bounds that we were measured with from RecentsView
    @ViewDebug.ExportedProperty(category="recents")
    private Rect mStableStackBounds = new Rect();
    // The current stack bounds are dynamic and may change as the user drags and drops
    @ViewDebug.ExportedProperty(category="recents")
    private Rect mStackBounds = new Rect();
    // The current window bounds at the point we were measured
    @ViewDebug.ExportedProperty(category="recents")
    private Rect mStableWindowRect = new Rect();
    // The current window bounds are dynamic and may change as the user drags and drops
    @ViewDebug.ExportedProperty(category="recents")
    private Rect mWindowRect = new Rect();
    // The current display bounds
    @ViewDebug.ExportedProperty(category="recents")
    private Rect mDisplayRect = new Rect();
    // The current display orientation
    @ViewDebug.ExportedProperty(category="recents")
    private int mDisplayOrientation = Configuration.ORIENTATION_UNDEFINED;

    private Rect mTmpRect = new Rect();
    private ArrayMap<Task.TaskKey, TaskView> mTmpTaskViewMap = new ArrayMap<>();
    private List<TaskView> mTmpTaskViews = new ArrayList<>();
    private TaskViewTransform mTmpTransform = new TaskViewTransform();
    private int[] mTmpIntPair = new int[2];
    private boolean mResetToInitialStateWhenResized;
    private int mLastWidth;
    private int mLastHeight;

    //prize add by xiarui 2018-03-26 for bug#53817 @{
    public boolean isDeleteAllTasksAnimation = false;
    //@}

    // A convenience update listener to request updating clipping of tasks
    private ValueAnimator.AnimatorUpdateListener mRequestUpdateClippingListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (!mTaskViewsClipDirty) {
                        mTaskViewsClipDirty = true;
                        invalidate();
                    }
                }
            };

    // The drop targets for a task drag
    private DropTarget mFreeformWorkspaceDropTarget = new DropTarget() {
        @Override
        public boolean acceptsDrop(int x, int y, int width, int height, Rect insets,
                boolean isCurrentTarget) {
            // This drop target has a fixed bounds and should be checked last, so just fall through
            // if it is the current target
            if (!isCurrentTarget) {
                return mLayoutAlgorithm.mFreeformRect.contains(x, y);
            }
            return false;
        }
    };

    private DropTarget mStackDropTarget = new DropTarget() {
        @Override
        public boolean acceptsDrop(int x, int y, int width, int height, Rect insets,
                boolean isCurrentTarget) {
            // This drop target has a fixed bounds and should be checked last, so just fall through
            // if it is the current target
            if (!isCurrentTarget) {
                return mLayoutAlgorithm.mStackRect.contains(x, y);
            }
            return false;
        }
    };

    public TaskStackView(Context context) {
        super(context);
        SystemServicesProxy ssp = Recents.getSystemServices();
        Resources res = context.getResources();

        // Set the stack first
        mStack.setCallbacks(this);
        mViewPool = new ViewPool<>(context, this);
        mInflater = LayoutInflater.from(context);
        mLayoutAlgorithm = new TaskStackLayoutAlgorithm(context, this);
        mStableLayoutAlgorithm = new TaskStackLayoutAlgorithm(context, null);
        mStackScroller = new TaskStackViewScroller(context, this, mLayoutAlgorithm);
        mTouchHandler = new TaskStackViewTouchHandler(context, this, mStackScroller);
        mAnimationHelper = new TaskStackAnimationHelper(context, this);
        mTaskCornerRadiusPx = res.getDimensionPixelSize(
                R.dimen.recents_task_view_rounded_corners_radius);
        mDividerSize = ssp.getDockedDividerSize(context);
        mDisplayOrientation = Utilities.getAppConfiguration(mContext).orientation;
        mDisplayRect = ssp.getDisplayRect();

        int taskBarDismissDozeDelaySeconds = getResources().getInteger(
                R.integer.recents_task_bar_dismiss_delay_seconds);
        mUIDozeTrigger = new DozeTrigger(taskBarDismissDozeDelaySeconds, new Runnable() {
            @Override
            public void run() {
                // Show the task bar dismiss buttons
                List<TaskView> taskViews = getTaskViews();
                int taskViewCount = taskViews.size();
                for (int i = 0; i < taskViewCount; i++) {
                    TaskView tv = taskViews.get(i);
                    tv.startNoUserInteractionAnimation();
                }
            }
        });
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);

        mFreeformWorkspaceBackground = (GradientDrawable) getContext().getDrawable(
                R.drawable.recents_freeform_workspace_bg);
        mFreeformWorkspaceBackground.setCallback(this);
        if (ssp.hasFreeformWorkspaceSupport()) {
            mFreeformWorkspaceBackground.setColor(
                    getContext().getColor(R.color.recents_freeform_workspace_bg_color));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        EventBus.getDefault().register(this, RecentsActivity.EVENT_BUS_PRIORITY + 1);
        super.onAttachedToWindow();
        readSystemFlags();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Called from RecentsActivity when it is relaunched.
     */
    void onReload(boolean isResumingFromVisible) {
        if (!isResumingFromVisible) {
            // Reset the focused task
            resetFocusedTask(getFocusedTask());
        }

        // Reset the state of each of the task views
        List<TaskView> taskViews = new ArrayList<>();
        taskViews.addAll(getTaskViews());
        taskViews.addAll(mViewPool.getViews());
        for (int i = taskViews.size() - 1; i >= 0; i--) {
            taskViews.get(i).onReload(isResumingFromVisible);
        }

        // Reset the stack state
        readSystemFlags();
        mTaskViewsClipDirty = true;
        mEnterAnimationComplete = false;
        mUIDozeTrigger.stopDozing();
        if (isResumingFromVisible) {
            // Animate in the freeform workspace
            int ffBgAlpha = mLayoutAlgorithm.getStackState().freeformBackgroundAlpha;
            animateFreeformWorkspaceBackgroundAlpha(ffBgAlpha, new AnimationProps(150,
                    Interpolators.FAST_OUT_SLOW_IN));
        } else {
            mStackScroller.reset();
            mStableLayoutAlgorithm.reset();
            mLayoutAlgorithm.reset();
        }

        // Since we always animate to the same place in (the initial state), always reset the stack
        // to the initial state when resuming
        mAwaitingFirstLayout = true;
        mLaunchNextAfterFirstMeasure = false;
        mInitialState = INITIAL_STATE_UPDATE_ALL;
        requestLayout();
    }

    /**
     * Sets the stack tasks of this TaskStackView from the given TaskStack.
     */
    public void setTasks(TaskStack stack, boolean allowNotifyStackChanges) {
        boolean isInitialized = mLayoutAlgorithm.isInitialized();

        // Only notify if we are already initialized, otherwise, everything will pick up all the
        // new and old tasks when we next layout
        mStack.setTasks(getContext(), stack.computeAllTasksList(),
                allowNotifyStackChanges && isInitialized);
        /*prize-init task.isLock-liufan-2016-11-17-start*/
	    Log.d(EncryptionTAG,"TaskStackView.setTasks!");
        ArrayList<Task> tasks = mStack.getStackTasks();
		/*prize-add by lihuangyuan,for fingerapplock-2017-07-04-start*/
        HashMap<String,EncryptionData> map = obtainEncryptionList();
		/*prize-add by lihuangyuan,for fingerapplock-2017-07-04-end*/
        for (Task t : tasks) {
            if (t != null) {
                /**
                String pkg = t.key.getComponent().getPackageName();
                if (AppLockManager.getInstance(getContext()).isLockedApp(pkg, t.key.userId)) {
                    t.setLock(getContext(), true);
                }
                **/

                /**
                String cls = t.key.getComponent().getClassName();
                for (String str : TaskView.default_LockList) {
                    if (cls.contains(str)) {
                        TaskView.setLock(getContext(), cls, true);
                        break;
                    }
                }
                t.isLock = TaskView.isLock(t, getContext());
                **/

                /*prize-add by lihuangyuan,for fingerapplock-2017-07-04-start*/
                if (PrizeOption.PRIZE_FINGERPRINT_APPLOCK && map != null) {
                    String key = t.key.getComponent().getPackageName();
                    t.isEncryption = map.get(key) != null ? true : false;
                    Log.d(EncryptionTAG, "TaskStackView.setTasks key:" + key + ",isEncryption:" + t.isEncryption);
                }
                /*prize-add by lihuangyuan,for fingerapplock-2017-07-04-end*/
            }
        }
        /*prize-init task.isLock-liufan-2016-11-17-end*/
    }

    /*prize-encrypt-liufan-2016-11-17-start*/
    public class EncryptionData{
        public String pkgName;
        public String className;
        public int lockStatus;//1--lock, 0--unlock;
    }
    /*prize-encrypt-liufan-2016-11-17-end*/
    /*prize-add by lihuangyuan,for fingerapplock-2017-07-04-start*/
    Uri FP_FUNCTION_DATA = Uri.parse("content://com.android.settings.provider.fpdata.share/fp_function_data");
    Uri LOCK_APP_DATA = Uri.parse("content://com.android.settings.provider.fpdata.share/lock_app_data");
    public static final String EncryptionTAG = "EncryptionData";

    public HashMap<String,EncryptionData> obtainEncryptionList(){
        HashMap<String,EncryptionData> map = new HashMap<String,EncryptionData>();
	 if(!PrizeOption.PRIZE_FINGERPRINT_APPLOCK)return map;

	 Cursor lock_app_cursor = null;
        Cursor mCursor = getContext().getContentResolver().query(FP_FUNCTION_DATA, null, "function_name=?", new String[]{"app_lock_fc"},null);
        if(mCursor != null && mCursor.getCount() > 0){
            int mFunctStatus = 0;
            while (mCursor.moveToNext()) {
                mFunctStatus = mCursor.getInt(mCursor.getColumnIndex("function_status"));
                if(mFunctStatus == 1){
                    lock_app_cursor = getContext().getContentResolver().query(LOCK_APP_DATA, null, "lock_status=1", null,null);
                    if(lock_app_cursor != null && lock_app_cursor.getCount() > 0){
                        while (lock_app_cursor.moveToNext()) {
                            EncryptionData ed = new EncryptionData();
                            ed.lockStatus = lock_app_cursor.getInt(lock_app_cursor.getColumnIndex("lock_status"));
                            ed.pkgName = lock_app_cursor.getString(lock_app_cursor.getColumnIndex("pkg_name"));
                            ed.className = lock_app_cursor.getString(lock_app_cursor.getColumnIndex("class_name"));
                            //Log.d(EncryptionTAG,"Encryption app pkg----------->"+ed.pkgName);
                            //Log.d(EncryptionTAG,"Encryption app className----->"+ed.className);
                            map.put(ed.pkgName,ed);
                        }
                        lock_app_cursor.close();
			    lock_app_cursor = null;
                    }
                    Log.d(EncryptionTAG,"EncryptionList.size---->"+map.size());
                }
            }
            mCursor.close();
	     mCursor = null;
        }
	 if(mCursor != null)mCursor.close();
	 if(lock_app_cursor != null)lock_app_cursor.close();
        return map;
    }
    /*prize-add by lihuangyuan,for fingerapplock-2017-07-04-end*/

    /** Returns the task stack. */
    public TaskStack getStack() {
        return mStack;
    }

    /**
     * Updates this TaskStackView to the initial state.
     */
    public void updateToInitialState() {
        mStackScroller.setStackScrollToInitialState();
        mLayoutAlgorithm.setTaskOverridesForInitialState(mStack, false /* ignoreScrollToFront */);
    }

    /** Updates the list of task views */
    void updateTaskViewsList() {
        mTaskViews.clear();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof TaskView) {
                mTaskViews.add((TaskView) v);
            }
        }
    }

    /** Gets the list of task views */
    List<TaskView> getTaskViews() {
        return mTaskViews;
    }

    /**
     * Returns the front most task view.
     *
     * @param stackTasksOnly if set, will return the front most task view in the stack (by default
     *                       the front most task view will be freeform since they are placed above
     *                       stack tasks)
     */
    private TaskView getFrontMostTaskView(boolean stackTasksOnly) {
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = taskViewCount - 1; i >= 0; i--) {
            TaskView tv = taskViews.get(i);
            Task task = tv.getTask();
            if (stackTasksOnly && task.isFreeformTask()) {
                continue;
            }
            return tv;
        }
        return null;
    }

    /**
     * Finds the child view given a specific {@param task}.
     */
    public TaskView getChildViewForTask(Task t) {
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = taskViews.get(i);
            if (tv.getTask() == t) {
                return tv;
            }
        }
        return null;
    }

    /** Returns the stack algorithm for this task stack. */
    public TaskStackLayoutAlgorithm getStackAlgorithm() {
        return mLayoutAlgorithm;
    }

    /**
     * Returns the touch handler for this task stack.
     */
    public TaskStackViewTouchHandler getTouchHandler() {
        return mTouchHandler;
    }

    /**
     * Adds a task to the ignored set.
     */
    void addIgnoreTask(Task task) {
        mIgnoreTasks.add(task.key);
    }

    /**
     * Removes a task from the ignored set.
     */
    void removeIgnoreTask(Task task) {
        mIgnoreTasks.remove(task.key);
    }

    /**
     * Returns whether the specified {@param task} is ignored.
     */
    boolean isIgnoredTask(Task task) {
        return mIgnoreTasks.contains(task.key);
    }

    /**
     * Computes the task transforms at the current stack scroll for all visible tasks. If a valid
     * target stack scroll is provided (ie. is different than {@param curStackScroll}), then the
     * visible range includes all tasks at the target stack scroll. This is useful for ensure that
     * all views necessary for a transition or animation will be visible at the start.
     *
     * This call ignores freeform tasks.
     *
     * @param taskTransforms The set of task view transforms to reuse, this list will be sized to
     *                       match the size of {@param tasks}
     * @param tasks The set of tasks for which to generate transforms
     * @param curStackScroll The current stack scroll
     * @param targetStackScroll The stack scroll that we anticipate we are going to be scrolling to.
     *                          The range of the union of the visible views at the current and
     *                          target stack scrolls will be returned.
     * @param ignoreTasksSet The set of tasks to skip for purposes of calculaing the visible range.
     *                       Transforms will still be calculated for the ignore tasks.
     * @return the front and back most visible task indices (there may be non visible tasks in
     *         between this range)
     */
    int[] computeVisibleTaskTransforms(ArrayList<TaskViewTransform> taskTransforms,
            ArrayList<Task> tasks, float curStackScroll, float targetStackScroll,
            ArraySet<Task.TaskKey> ignoreTasksSet, boolean ignoreTaskOverrides) {
        int taskCount = tasks.size();
        int[] visibleTaskRange = mTmpIntPair;
        visibleTaskRange[0] = -1;
        visibleTaskRange[1] = -1;
        boolean useTargetStackScroll = Float.compare(curStackScroll, targetStackScroll) != 0;

        // We can reuse the task transforms where possible to reduce object allocation
        Utilities.matchTaskListSize(tasks, taskTransforms);

        // Update the stack transforms
        TaskViewTransform frontTransform = null;
        TaskViewTransform frontTransformAtTarget = null;
        TaskViewTransform transform = null;
        TaskViewTransform transformAtTarget = null;
        for (int i = taskCount - 1; i >= 0; i--) {
            Task task = tasks.get(i);

            // Calculate the current and (if necessary) the target transform for the task
            transform = mLayoutAlgorithm.getStackTransform(task, curStackScroll,
                    taskTransforms.get(i), frontTransform, ignoreTaskOverrides);
            if (useTargetStackScroll && !transform.visible) {
                // If we have a target stack scroll and the task is not currently visible, then we
                // just update the transform at the new scroll
                // TODO: Optimize this
                transformAtTarget = mLayoutAlgorithm.getStackTransform(task,
                        targetStackScroll, new TaskViewTransform(), frontTransformAtTarget);
                if (transformAtTarget.visible) {
                    transform.copyFrom(transformAtTarget);
                }
            }

            // For ignore tasks, only calculate the stack transform and skip the calculation of the
            // visible stack indices
            if (ignoreTasksSet.contains(task.key)) {
                continue;
            }

            // For freeform tasks, only calculate the stack transform and skip the calculation of
            // the visible stack indices
            if (task.isFreeformTask()) {
                continue;
            }

            frontTransform = transform;
            frontTransformAtTarget = transformAtTarget;
            if (transform.visible) {
                if (visibleTaskRange[0] < 0) {
                    visibleTaskRange[0] = i;
                }
                visibleTaskRange[1] = i;
            }
        }
        return visibleTaskRange;
    }

    /**
     * Binds the visible {@link TaskView}s at the given target scroll.
     */
    void bindVisibleTaskViews(float targetStackScroll) {
        bindVisibleTaskViews(targetStackScroll, false /* ignoreTaskOverrides */);
    }

    /**
     * Synchronizes the set of children {@link TaskView}s to match the visible set of tasks in the
     * current {@link TaskStack}. This call does not continue on to update their position to the
     * computed {@link TaskViewTransform}s of the visible range, but only ensures that they will
     * be added/removed from the view hierarchy and placed in the correct Z order and initial
     * position (if not currently on screen).
     *
     * @param targetStackScroll If provided, will ensure that the set of visible {@link TaskView}s
     *                          includes those visible at the current stack scroll, and all at the
     *                          target stack scroll.
     * @param ignoreTaskOverrides If set, the visible task computation will get the transforms for
     *                            tasks at their non-overridden task progress
     */
    void bindVisibleTaskViews(float targetStackScroll, boolean ignoreTaskOverrides) {
        // Get all the task transforms
        ArrayList<Task> tasks = mStack.getStackTasks();
        int[] visibleTaskRange = computeVisibleTaskTransforms(mCurrentTaskTransforms, tasks,
                mStackScroller.getStackScroll(), targetStackScroll, mIgnoreTasks,
                ignoreTaskOverrides);

        // Return all the invisible children to the pool
        mTmpTaskViewMap.clear();
        List<TaskView> taskViews = getTaskViews();
        int lastFocusedTaskIndex = -1;
        int taskViewCount = taskViews.size();
        for (int i = taskViewCount - 1; i >= 0; i--) {
            TaskView tv = taskViews.get(i);
            Task task = tv.getTask();

            // Skip ignored tasks
            if (mIgnoreTasks.contains(task.key)) {
                continue;
            }

            // It is possible for the set of lingering TaskViews to differ from the stack if the
            // stack was updated before the relayout.  If the task view is no longer in the stack,
            // then just return it back to the view pool.
            int taskIndex = mStack.indexOfStackTask(task);
            TaskViewTransform transform = null;
            if (taskIndex != -1) {
                transform = mCurrentTaskTransforms.get(taskIndex);
            }

            if (task.isFreeformTask() || (transform != null && transform.visible)) {
                mTmpTaskViewMap.put(task.key, tv);
            } else {
                if (mTouchExplorationEnabled && Utilities.isDescendentAccessibilityFocused(tv)) {
                    lastFocusedTaskIndex = taskIndex;
                    resetFocusedTask(task);
                }
                mViewPool.returnViewToPool(tv);
            }
        }

        // Pick up all the newly visible children
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = tasks.get(i);
            TaskViewTransform transform = mCurrentTaskTransforms.get(i);

            // Skip ignored tasks
            if (mIgnoreTasks.contains(task.key)) {
                continue;
            }

            // Skip the invisible non-freeform stack tasks
            if (!task.isFreeformTask() && !transform.visible) {
                continue;
            }

            TaskView tv = mTmpTaskViewMap.get(task.key);
            if (tv == null) {
                tv = mViewPool.pickUpViewFromPool(task, task);
                if (task.isFreeformTask()) {
                    updateTaskViewToTransform(tv, transform, AnimationProps.IMMEDIATE);
                } else {
                    /*prize modify by xiarui for Bug#44805 Bug #43102 2017-12-12 start*/
                    if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                        if (transform.rect.left <= mLayoutAlgorithm.mStackRect.left) {
                            updateTaskViewToTransform(tv, mLayoutAlgorithm.getBackOfStackTransform(),
                                    AnimationProps.IMMEDIATE);
                        } else {
                            updateTaskViewToTransform(tv, mLayoutAlgorithm.getFrontOfStackTransform(),
                                    AnimationProps.IMMEDIATE);
                        }
                    } else {
                        if (transform.rect.top <= mLayoutAlgorithm.mStackRect.top) {
                            updateTaskViewToTransform(tv, mLayoutAlgorithm.getBackOfStackTransform(),
                                    AnimationProps.IMMEDIATE);
                        } else {
                            updateTaskViewToTransform(tv, mLayoutAlgorithm.getFrontOfStackTransform(),
                                    AnimationProps.IMMEDIATE);
                        }
                    }
                    /*prize modify by xiarui for Bug#44805 Bug#43102 2017-12-12 end*/
                }
            } else {
                // Reattach it in the right z order
                final int taskIndex = mStack.indexOfStackTask(task);
                final int insertIndex = findTaskViewInsertIndex(task, taskIndex);
                if (insertIndex != getTaskViews().indexOf(tv)){
                    detachViewFromParent(tv);
                    attachViewToParent(tv, insertIndex, tv.getLayoutParams());
                    updateTaskViewsList();
                }
            }
        }

        // Update the focus if the previous focused task was returned to the view pool
        if (lastFocusedTaskIndex != -1) {
            int newFocusedTaskIndex = (lastFocusedTaskIndex < visibleTaskRange[1])
                    ? visibleTaskRange[1]
                    : visibleTaskRange[0];
            setFocusedTask(newFocusedTaskIndex, false /* scrollToTask */,
                    true /* requestViewFocus */);
            TaskView focusedTaskView = getChildViewForTask(mFocusedTask);
            if (focusedTaskView != null) {
                focusedTaskView.requestAccessibilityFocus();
            }
        }
    }

    /**
     * @see #relayoutTaskViews(AnimationProps, ArrayMap<Task, AnimationProps>, boolean)
     */
    public void relayoutTaskViews(AnimationProps animation) {
        relayoutTaskViews(animation, null /* animationOverrides */,
                false /* ignoreTaskOverrides */);
    }

    /**
     * Relayout the the visible {@link TaskView}s to their current transforms as specified by the
     * {@link TaskStackLayoutAlgorithm} with the given {@param animation}. This call cancels any
     * animations that are current running on those task views, and will ensure that the children
     * {@link TaskView}s will match the set of visible tasks in the stack.  If a {@link Task} has
     * an animation provided in {@param animationOverrides}, that will be used instead.
     */
    private void relayoutTaskViews(AnimationProps animation,
            ArrayMap<Task, AnimationProps> animationOverrides,
            boolean ignoreTaskOverrides) {
        // If we had a deferred animation, cancel that
        cancelDeferredTaskViewLayoutAnimation();

        // Synchronize the current set of TaskViews
        bindVisibleTaskViews(mStackScroller.getStackScroll(),
                ignoreTaskOverrides /* ignoreTaskOverrides */);

        // Animate them to their final transforms with the given animation
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = taskViews.get(i);
            Task task = tv.getTask();

            if (mIgnoreTasks.contains(task.key)) {
                continue;
            }

            int taskIndex = mStack.indexOfStackTask(task);
            /// M: Avoid JE when taskIndex is -1 @{
            if(taskIndex == -1){
                continue;
            }
            /// @ }
            TaskViewTransform transform = mCurrentTaskTransforms.get(taskIndex);
            if (animationOverrides != null && animationOverrides.containsKey(task)) {
                animation = animationOverrides.get(task);
            }
            updateTaskViewToTransform(tv, transform, animation);
        }
    }

    /**
     * Posts an update to synchronize the {@link TaskView}s with the stack on the next frame.
     */
    void relayoutTaskViewsOnNextFrame(AnimationProps animation) {
        mDeferredTaskViewLayoutAnimation = animation;
        invalidate();
    }

    /**
     * Called to update a specific {@link TaskView} to a given {@link TaskViewTransform} with a
     * given set of {@link AnimationProps} properties.
     */
    public void updateTaskViewToTransform(TaskView taskView, TaskViewTransform transform,
            AnimationProps animation) {
        //prize modify by xiarui 2018-03-26 for bug#53817 @{
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            if (isDeleteAllTasksAnimation) {
                return;
            }
        }
        //@}
        if (taskView.isAnimatingTo(transform)) {
            return;
        }
        taskView.cancelTransformAnimation();
        taskView.updateViewPropertiesToTaskTransform(transform, animation,
                mRequestUpdateClippingListener);
    }

	/**prize add by xiarui for Bug#44285 2017-12-09 start**/
    public void updateTaskViewToTransform(
            boolean animated,
            ReferenceCountedTrigger postAnimationTrigger,
            TaskView taskView,
            TaskViewTransform transform,
            AnimationProps animation) {

        if (taskView.isAnimatingTo(transform)) {
            return;
        }
        if (animated) {
            postAnimationTrigger.increment();
        }
        taskView.cancelTransformAnimation();
        taskView.updateViewPropertiesToTaskTransform(transform, animation,
                mRequestUpdateClippingListener);
    }
	/**prize add by xiarui for Bug#44285 2017-12-09 end**/

    /**
     * Returns the current task transforms of all tasks, falling back to the stack layout if there
     * is no {@link TaskView} for the task.
     */
    public void getCurrentTaskTransforms(ArrayList<Task> tasks,
            ArrayList<TaskViewTransform> transformsOut) {
        Utilities.matchTaskListSize(tasks, transformsOut);
        int focusState = mLayoutAlgorithm.getFocusState();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = tasks.get(i);
            TaskViewTransform transform = transformsOut.get(i);
            TaskView tv = getChildViewForTask(task);
            if (tv != null) {
                transform.fillIn(tv);
            } else {
                mLayoutAlgorithm.getStackTransform(task, mStackScroller.getStackScroll(),
                        focusState, transform, null, true /* forceUpdate */,
                        false /* ignoreTaskOverrides */);
            }
            transform.visible = true;
        }
    }

    /**
     * Returns the task transforms for all the tasks in the stack if the stack was at the given
     * {@param stackScroll} and {@param focusState}.
     */
    public void getLayoutTaskTransforms(float stackScroll, int focusState, ArrayList<Task> tasks,
            boolean ignoreTaskOverrides, ArrayList<TaskViewTransform> transformsOut) {
        Utilities.matchTaskListSize(tasks, transformsOut);
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = tasks.get(i);
            TaskViewTransform transform = transformsOut.get(i);
            mLayoutAlgorithm.getStackTransform(task, stackScroll, focusState, transform, null,
                    true /* forceUpdate */, ignoreTaskOverrides);
            transform.visible = true;
        }
    }

    /**
     * Cancels the next deferred task view layout.
     */
    void cancelDeferredTaskViewLayoutAnimation() {
        mDeferredTaskViewLayoutAnimation = null;
    }

    /**
     * Cancels all {@link TaskView} animations.
     */
    void cancelAllTaskViewAnimations() {
        List<TaskView> taskViews = getTaskViews();
        for (int i = taskViews.size() - 1; i >= 0; i--) {
            final TaskView tv = taskViews.get(i);
            if (!mIgnoreTasks.contains(tv.getTask().key)) {
                tv.cancelTransformAnimation();
            }
        }
    }

    /**
     * Updates the clip for each of the task views from back to front.
     */
    private void clipTaskViews() {
        // Update the clip on each task child
        List<TaskView> taskViews = getTaskViews();
        TaskView tmpTv = null;
        TaskView prevVisibleTv = null;
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = taskViews.get(i);
            TaskView frontTv = null;
            int clipBottom = 0;

            if (isIgnoredTask(tv.getTask())) {
                // For each of the ignore tasks, update the translationZ of its TaskView to be
                // between the translationZ of the tasks immediately underneath it
                if (prevVisibleTv != null) {
                    tv.setTranslationZ(Math.max(tv.getTranslationZ(),
                            prevVisibleTv.getTranslationZ() + 0.1f));
                }
            }

            if (i < (taskViewCount - 1) && tv.shouldClipViewInStack()) {
                // Find the next view to clip against
                for (int j = i + 1; j < taskViewCount; j++) {
                    tmpTv = taskViews.get(j);

                    if (tmpTv.shouldClipViewInStack()) {
                        frontTv = tmpTv;
                        break;
                    }
                }

                // Clip against the next view, this is just an approximation since we are
                // stacked and we can make assumptions about the visibility of the this
                // task relative to the ones in front of it.
                if (frontTv != null) {
                    float taskBottom = tv.getBottom();
                    float frontTaskTop = frontTv.getTop();
                    if (frontTaskTop < taskBottom) {
                        // Map the stack view space coordinate (the rects) to view space
                        /*prize modify for disable clip bottom-xiarui-2017-11-06-start*/
						if (!PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                        	clipBottom = (int) (taskBottom - frontTaskTop) - mTaskCornerRadiusPx;
						}
                        /*prize modify for disable clip bottom-xiarui-2017-11-06-end*/
                    }
                }
            }
            tv.getViewBounds().setClipBottom(clipBottom);
            tv.mThumbnailView.updateThumbnailVisibility(clipBottom - tv.getPaddingBottom());
            prevVisibleTv = tv;
        }
        mTaskViewsClipDirty = false;
    }

    /**
     * Updates the layout algorithm min and max virtual scroll bounds.
     */
   public void updateLayoutAlgorithm(boolean boundScrollToNewMinMax) {
        // Compute the min and max scroll values
        mLayoutAlgorithm.update(mStack, mIgnoreTasks);

        // Update the freeform workspace background
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.hasFreeformWorkspaceSupport()) {
            mTmpRect.set(mLayoutAlgorithm.mFreeformRect);
            mFreeformWorkspaceBackground.setBounds(mTmpRect);
        }

        if (boundScrollToNewMinMax) {
            mStackScroller.boundScroll();
        }
    }

    /**
     * Updates the stack layout to its stable places.
     */
    private void updateLayoutToStableBounds() {
        mWindowRect.set(mStableWindowRect);
        mStackBounds.set(mStableStackBounds);
        mLayoutAlgorithm.setSystemInsets(mStableLayoutAlgorithm.mSystemInsets);
        mLayoutAlgorithm.initialize(mDisplayRect, mWindowRect, mStackBounds,
                TaskStackLayoutAlgorithm.StackState.getStackStateForStack(mStack));
        updateLayoutAlgorithm(true /* boundScroll */);
    }

    /** Returns the scroller. */
    public TaskStackViewScroller getScroller() {
        return mStackScroller;
    }

    /**
     * Sets the focused task to the provided (bounded taskIndex).
     *
     * @return whether or not the stack will scroll as a part of this focus change
     */
    private boolean setFocusedTask(int taskIndex, boolean scrollToTask,
            final boolean requestViewFocus) {
        return setFocusedTask(taskIndex, scrollToTask, requestViewFocus, 0);
    }

    /**
     * Sets the focused task to the provided (bounded focusTaskIndex).
     *
     * @return whether or not the stack will scroll as a part of this focus change
     */
    private boolean setFocusedTask(int focusTaskIndex, boolean scrollToTask,
            boolean requestViewFocus, int timerIndicatorDuration) {
        // Find the next task to focus
        int newFocusedTaskIndex = mStack.getTaskCount() > 0 ?
                Utilities.clamp(focusTaskIndex, 0, mStack.getTaskCount() - 1) : -1;
        final Task newFocusedTask = (newFocusedTaskIndex != -1) ?
                mStack.getStackTasks().get(newFocusedTaskIndex) : null;

        // Reset the last focused task state if changed
        if (mFocusedTask != null) {
            // Cancel the timer indicator, if applicable
            if (timerIndicatorDuration > 0) {
                final TaskView tv = getChildViewForTask(mFocusedTask);
                if (tv != null) {
                    tv.getHeaderView().cancelFocusTimerIndicator();
                }
            }

            resetFocusedTask(mFocusedTask);
        }

        boolean willScroll = false;
        mFocusedTask = newFocusedTask;

        if (newFocusedTask != null) {
            // Start the timer indicator, if applicable
            if (timerIndicatorDuration > 0) {
                final TaskView tv = getChildViewForTask(mFocusedTask);
                if (tv != null) {
                    tv.getHeaderView().startFocusTimerIndicator(timerIndicatorDuration);
                } else {
                    // The view is null; set a flag for later
                    mStartTimerIndicatorDuration = timerIndicatorDuration;
                }
            }

            if (scrollToTask) {
                // Cancel any running enter animations at this point when we scroll or change focus
                if (!mEnterAnimationComplete) {
                    cancelAllTaskViewAnimations();
                }

                mLayoutAlgorithm.clearUnfocusedTaskOverrides();
                willScroll = mAnimationHelper.startScrollToFocusedTaskAnimation(newFocusedTask,
                        requestViewFocus);
            } else {
                // Focus the task view
                TaskView newFocusedTaskView = getChildViewForTask(newFocusedTask);
                if (newFocusedTaskView != null) {
                    newFocusedTaskView.setFocusedState(true, requestViewFocus);
                }
            }
        }
        return willScroll;
    }

    /**
     * Sets the focused task relative to the currently focused task.
     *
     * @param forward whether to go to the next task in the stack (along the curve) or the previous
     * @param stackTasksOnly if set, will ensure that the traversal only goes along stack tasks, and
     *                       if the currently focused task is not a stack task, will set the focus
     *                       to the first visible stack task
     * @param animated determines whether to actually draw the highlight along with the change in
     *                            focus.
     */
    public void setRelativeFocusedTask(boolean forward, boolean stackTasksOnly, boolean animated) {
        setRelativeFocusedTask(forward, stackTasksOnly, animated, false, 0);
    }

    /**
     * Sets the focused task relative to the currently focused task.
     *
     * @param forward whether to go to the next task in the stack (along the curve) or the previous
     * @param stackTasksOnly if set, will ensure that the traversal only goes along stack tasks, and
     *                       if the currently focused task is not a stack task, will set the focus
     *                       to the first visible stack task
     * @param animated determines whether to actually draw the highlight along with the change in
     *                            focus.
     * @param cancelWindowAnimations if set, will attempt to cancel window animations if a scroll
     *                               happens.
     * @param timerIndicatorDuration the duration to initialize the auto-advance timer indicator
     */
    public void setRelativeFocusedTask(boolean forward, boolean stackTasksOnly, boolean animated,
                                       boolean cancelWindowAnimations, int timerIndicatorDuration) {
        Task focusedTask = getFocusedTask();
        int newIndex = mStack.indexOfStackTask(focusedTask);
        if (focusedTask != null) {
            if (stackTasksOnly) {
                List<Task> tasks =  mStack.getStackTasks();
                if (focusedTask.isFreeformTask()) {
                    // Try and focus the front most stack task
                    TaskView tv = getFrontMostTaskView(stackTasksOnly);
                    if (tv != null) {
                        newIndex = mStack.indexOfStackTask(tv.getTask());
                    }
                } else {
                    // Try the next task if it is a stack task
                    int tmpNewIndex = newIndex + (forward ? -1 : 1);
                    if (0 <= tmpNewIndex && tmpNewIndex < tasks.size()) {
                        Task t = tasks.get(tmpNewIndex);
                        if (!t.isFreeformTask()) {
                            newIndex = tmpNewIndex;
                        }
                    }
                }
            } else {
                // No restrictions, lets just move to the new task (looping forward/backwards if
                // necessary)
                int taskCount = mStack.getTaskCount();
                newIndex = (newIndex + (forward ? -1 : 1) + taskCount) % taskCount;
            }
        } else {
            // We don't have a focused task
            float stackScroll = mStackScroller.getStackScroll();
            ArrayList<Task> tasks = mStack.getStackTasks();
            int taskCount = tasks.size();
            if (forward) {
                // Walk backwards and focus the next task smaller than the current stack scroll
                for (newIndex = taskCount - 1; newIndex >= 0; newIndex--) {
                    float taskP = mLayoutAlgorithm.getStackScrollForTask(tasks.get(newIndex));
                    if (Float.compare(taskP, stackScroll) <= 0) {
                        break;
                    }
                }
            } else {
                // Walk forwards and focus the next task larger than the current stack scroll
                for (newIndex = 0; newIndex < taskCount; newIndex++) {
                    float taskP = mLayoutAlgorithm.getStackScrollForTask(tasks.get(newIndex));
                    if (Float.compare(taskP, stackScroll) >= 0) {
                        break;
                    }
                }
            }
        }
        if (newIndex != -1) {
            boolean willScroll = setFocusedTask(newIndex, true /* scrollToTask */,
                    true /* requestViewFocus */, timerIndicatorDuration);
            if (willScroll && cancelWindowAnimations) {
                // As we iterate to the next/previous task, cancel any current/lagging window
                // transition animations
                EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(null));
            }
        }
    }

    /**
     * Resets the focused task.
     */
    void resetFocusedTask(Task task) {
        if (task != null) {
            TaskView tv = getChildViewForTask(task);
            if (tv != null) {
                tv.setFocusedState(false, false /* requestViewFocus */);
            }
        }
        mFocusedTask = null;
    }

    /**
     * Returns the focused task.
     */
    Task getFocusedTask() {
        return mFocusedTask;
    }

    /**
     * Returns the accessibility focused task.
     */
    Task getAccessibilityFocusedTask() {
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = taskViews.get(i);
            if (Utilities.isDescendentAccessibilityFocused(tv)) {
                return tv.getTask();
            }
        }
        TaskView frontTv = getFrontMostTaskView(true /* stackTasksOnly */);
        if (frontTv != null) {
            return frontTv.getTask();
        }
        return null;
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        if (taskViewCount > 0) {
            TaskView backMostTask = taskViews.get(0);
            TaskView frontMostTask = taskViews.get(taskViewCount - 1);
            event.setFromIndex(mStack.indexOfStackTask(backMostTask.getTask()));
            event.setToIndex(mStack.indexOfStackTask(frontMostTask.getTask()));
            event.setContentDescription(frontMostTask.getTask().title);
        }
        event.setItemCount(mStack.getTaskCount());

        int stackHeight = mLayoutAlgorithm.mStackRect.height();
        event.setScrollY((int) (mStackScroller.getStackScroll() * stackHeight));
        event.setMaxScrollY((int) (mLayoutAlgorithm.mMaxScrollP * stackHeight));
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        if (taskViewCount > 1) {
            // Find the accessibility focused task
            Task focusedTask = getAccessibilityFocusedTask();
            info.setScrollable(true);
            int focusedTaskIndex = mStack.indexOfStackTask(focusedTask);
            if (focusedTaskIndex > 0) {
                info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
            }
            if (0 <= focusedTaskIndex && focusedTaskIndex < mStack.getTaskCount() - 1) {
                info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
        }
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return ScrollView.class.getName();
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (super.performAccessibilityAction(action, arguments)) {
            return true;
        }
        Task focusedTask = getAccessibilityFocusedTask();
        int taskIndex = mStack.indexOfStackTask(focusedTask);
        if (0 <= taskIndex && taskIndex < mStack.getTaskCount()) {
            switch (action) {
                case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD: {
                    setFocusedTask(taskIndex + 1, true /* scrollToTask */, true /* requestViewFocus */,
                            0);
                    return true;
                }
                case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD: {
                    setFocusedTask(taskIndex - 1, true /* scrollToTask */, true /* requestViewFocus */,
                            0);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mTouchHandler.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mTouchHandler.onTouchEvent(ev);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent ev) {
        return mTouchHandler.onGenericMotionEvent(ev);
    }

    @Override
    public void computeScroll() {
        if (mStackScroller.computeScroll()) {
            // Notify accessibility
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SCROLLED);
        }
        if (mDeferredTaskViewLayoutAnimation != null) {
            relayoutTaskViews(mDeferredTaskViewLayoutAnimation);
            mTaskViewsClipDirty = true;
            mDeferredTaskViewLayoutAnimation = null;
        }
        if (mTaskViewsClipDirty) {
            clipTaskViews();
        }
    }

    /**
     * Computes the maximum number of visible tasks and thumbnails. Requires that
     * updateLayoutForStack() is called first.
     */
    public TaskStackLayoutAlgorithm.VisibilityReport computeStackVisibilityReport() {
        return mLayoutAlgorithm.computeStackVisibilityReport(mStack.getStackTasks());
    }

    /**
     * Updates the system insets.
     */
    public void setSystemInsets(Rect systemInsets) {
        boolean changed = false;
        changed |= mStableLayoutAlgorithm.setSystemInsets(systemInsets);
        changed |= mLayoutAlgorithm.setSystemInsets(systemInsets);
        if (changed) {
            requestLayout();
        }
    }

    /**
     * This is called with the full window width and height to allow stack view children to
     * perform the full screen transition down.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mInMeasureLayout = true;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Update the stable stack bounds, but only update the current stack bounds if the stable
        // bounds have changed.  This is because we may get spurious measures while dragging where
        // our current stack bounds reflect the target drop region.
        mLayoutAlgorithm.getTaskStackBounds(mDisplayRect, new Rect(0, 0, width, height),
                mLayoutAlgorithm.mSystemInsets.top, mLayoutAlgorithm.mSystemInsets.left,
                mLayoutAlgorithm.mSystemInsets.right, mTmpRect);
        if (!mTmpRect.equals(mStableStackBounds)) {
            mStableStackBounds.set(mTmpRect);
            mStackBounds.set(mTmpRect);
            mStableWindowRect.set(0, 0, width, height);
            mWindowRect.set(0, 0, width, height);
        }

        // Compute the rects in the stack algorithm
        mStableLayoutAlgorithm.initialize(mDisplayRect, mStableWindowRect, mStableStackBounds,
                TaskStackLayoutAlgorithm.StackState.getStackStateForStack(mStack));
        mLayoutAlgorithm.initialize(mDisplayRect, mWindowRect, mStackBounds,
                TaskStackLayoutAlgorithm.StackState.getStackStateForStack(mStack));
        updateLayoutAlgorithm(false /* boundScroll */);

        // If this is the first layout, then scroll to the front of the stack, then update the
        // TaskViews with the stack so that we can lay them out
        boolean resetToInitialState = (width != mLastWidth || height != mLastHeight)
                && mResetToInitialStateWhenResized;
        if (mAwaitingFirstLayout || mInitialState != INITIAL_STATE_UPDATE_NONE
                || resetToInitialState) {
            if (mInitialState != INITIAL_STATE_UPDATE_LAYOUT_ONLY || resetToInitialState) {
                updateToInitialState();
                mResetToInitialStateWhenResized = false;
            }
            if (!mAwaitingFirstLayout) {
                mInitialState = INITIAL_STATE_UPDATE_NONE;
            }
        }
        // If we got the launch-next event before the first layout pass, then re-send it after the
        // initial state has been updated
        if (mLaunchNextAfterFirstMeasure) {
            mLaunchNextAfterFirstMeasure = false;
            EventBus.getDefault().post(new LaunchNextTaskRequestEvent());
        }

        // Rebind all the views, including the ignore ones
        bindVisibleTaskViews(mStackScroller.getStackScroll(), false /* ignoreTaskOverrides */);

        // Measure each of the TaskViews
        mTmpTaskViews.clear();
        mTmpTaskViews.addAll(getTaskViews());
        mTmpTaskViews.addAll(mViewPool.getViews());
        int taskViewCount = mTmpTaskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            measureTaskView(mTmpTaskViews.get(i));
        }

        setMeasuredDimension(width, height);
        mLastWidth = width;
        mLastHeight = height;
        mInMeasureLayout = false;
    }

    /**
     * Measures a TaskView.
     */
    private void measureTaskView(TaskView tv) {
        Rect padding = new Rect();
        if (tv.getBackground() != null) {
            tv.getBackground().getPadding(padding);
        }
        mTmpRect.set(mStableLayoutAlgorithm.mTaskRect);
        mTmpRect.union(mLayoutAlgorithm.mTaskRect);
        tv.measure(
                MeasureSpec.makeMeasureSpec(mTmpRect.width() + padding.left + padding.right,
                        MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mTmpRect.height() + padding.top + padding.bottom,
                        MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // Layout each of the TaskViews
        mTmpTaskViews.clear();
        mTmpTaskViews.addAll(getTaskViews());
        mTmpTaskViews.addAll(mViewPool.getViews());
        int taskViewCount = mTmpTaskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            layoutTaskView(changed, mTmpTaskViews.get(i));
        }

        if (changed) {
            if (mStackScroller.isScrollOutOfBounds()) {
                mStackScroller.boundScroll();
            }
        }

        // Relayout all of the task views including the ignored ones
        relayoutTaskViews(AnimationProps.IMMEDIATE);
        clipTaskViews();

        if (mAwaitingFirstLayout || !mEnterAnimationComplete) {
            mAwaitingFirstLayout = false;
            mInitialState = INITIAL_STATE_UPDATE_NONE;
            onFirstLayout();
        }
    }

    /**
     * Lays out a TaskView.
     */
    private void layoutTaskView(boolean changed, TaskView tv) {
        if (changed) {
            Rect padding = new Rect();
            if (tv.getBackground() != null) {
                tv.getBackground().getPadding(padding);
            }
            mTmpRect.set(mStableLayoutAlgorithm.mTaskRect);
            mTmpRect.union(mLayoutAlgorithm.mTaskRect);
            tv.cancelTransformAnimation();
            tv.layout(mTmpRect.left - padding.left, mTmpRect.top - padding.top,
                    mTmpRect.right + padding.right, mTmpRect.bottom + padding.bottom);
        } else {
            // If the layout has not changed, then just lay it out again in-place
            tv.layout(tv.getLeft(), tv.getTop(), tv.getRight(), tv.getBottom());
        }
    }

    /** Handler for the first layout. */
    void onFirstLayout() {
        // Setup the view for the enter animation
        mAnimationHelper.prepareForEnterAnimation();

        // Animate in the freeform workspace
        int ffBgAlpha = mLayoutAlgorithm.getStackState().freeformBackgroundAlpha;
        animateFreeformWorkspaceBackgroundAlpha(ffBgAlpha, new AnimationProps(150,
                Interpolators.FAST_OUT_SLOW_IN));

        // Set the task focused state without requesting view focus, and leave the focus animations
        // until after the enter-animation
        RecentsConfiguration config = Recents.getConfiguration();
        RecentsActivityLaunchState launchState = config.getLaunchState();
        int focusedTaskIndex = launchState.getInitialFocusTaskIndex(mStack.getTaskCount());
        if (focusedTaskIndex != -1) {
            setFocusedTask(focusedTaskIndex, false /* scrollToTask */,
                    false /* requestViewFocus */);
        }

        // Update the stack action button visibility
        if (mStackScroller.getStackScroll() < SHOW_STACK_ACTION_BUTTON_SCROLL_THRESHOLD &&
                mStack.getTaskCount() > 0) {
            EventBus.getDefault().send(new ShowStackActionButtonEvent(false /* translate */));
        } else {
            EventBus.getDefault().send(new HideStackActionButtonEvent());
        }
    }

    public boolean isTouchPointInView(float x, float y, TaskView tv) {
        mTmpRect.set(tv.getLeft(), tv.getTop(), tv.getRight(), tv.getBottom());
        mTmpRect.offset((int) tv.getTranslationX(), (int) tv.getTranslationY());
        return mTmpRect.contains((int) x, (int) y);
    }

    /**
     * Returns a non-ignored task in the {@param tasks} list that can be used as an achor when
     * calculating the scroll position before and after a layout change.
     */
    public Task findAnchorTask(List<Task> tasks, MutableBoolean isFrontMostTask) {
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = tasks.get(i);

            // Ignore deleting tasks
            if (isIgnoredTask(task)) {
                if (i == tasks.size() - 1) {
                    isFrontMostTask.value = true;
                }
                continue;

            }
            return task;
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the freeform workspace background
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.hasFreeformWorkspaceSupport()) {
            if (mFreeformWorkspaceBackground.getAlpha() > 0) {
                mFreeformWorkspaceBackground.draw(canvas);
            }
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        if (who == mFreeformWorkspaceBackground) {
            return true;
        }
        return super.verifyDrawable(who);
    }

    /**
     * Launches the freeform tasks.
     */
    public boolean launchFreeformTasks() {
        ArrayList<Task> tasks = mStack.getFreeformTasks();
        if (!tasks.isEmpty()) {
            Task frontTask = tasks.get(tasks.size() - 1);
            if (frontTask != null && frontTask.isFreeformTask()) {
                EventBus.getDefault().send(new LaunchTaskEvent(getChildViewForTask(frontTask),
                        frontTask, null, INVALID_STACK_ID, false));
                return true;
            }
        }
        return false;
    }

    /**** TaskStackCallbacks Implementation ****/

    @Override
    public void onStackTaskAdded(TaskStack stack, Task newTask) {
        // Update the min/max scroll and animate other task views into their new positions
        updateLayoutAlgorithm(true /* boundScroll */);

        // Animate all the tasks into place
        relayoutTaskViews(mAwaitingFirstLayout
                ? AnimationProps.IMMEDIATE
                : new AnimationProps(DEFAULT_SYNC_STACK_DURATION, Interpolators.FAST_OUT_SLOW_IN));
    }

    /**
     * We expect that the {@link TaskView} associated with the removed task is already hidden.
     */
    @Override
    public void onStackTaskRemoved(TaskStack stack, Task removedTask, Task newFrontMostTask,
            AnimationProps animation, boolean fromDockGesture, boolean forceStop) { //modify by xiarui for Bug#45761 2017-12-16
        if (mFocusedTask == removedTask) {
            resetFocusedTask(removedTask);
        }

        // Remove the view associated with this task, we can't rely on updateTransforms
        // to work here because the task is no longer in the list
        TaskView tv = getChildViewForTask(removedTask);
        if (tv != null) {
            mViewPool.returnViewToPool(tv);
        }

        // Remove the task from the ignored set
        removeIgnoreTask(removedTask);

        // If requested, relayout with the given animation
        if (animation != null) {
            updateLayoutAlgorithm(true /* boundScroll */);
            relayoutTaskViews(animation);
        }

        // Update the new front most task's action button
        if (mScreenPinningEnabled && newFrontMostTask != null) {
            TaskView frontTv = getChildViewForTask(newFrontMostTask);
            if (frontTv != null) {
                frontTv.showActionButton(true /* fadeIn */, DEFAULT_SYNC_STACK_DURATION);
            }
        }

        // If there are no remaining tasks, then just close recents
        if (mStack.getTaskCount() == 0) {
            EventBus.getDefault().send(new AllTaskViewsDismissedEvent(fromDockGesture
                    ? R.string.recents_empty_message
                    : R.string.recents_empty_message_dismissed_all));
        }

        /*prize-kill the whole process-liufan-2016-07-05-start*/
        Log.d(TAG, "onStackTaskRemoved UserSlide = " + forceStop);
        if (forceStop) {
            ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            String packageName = removedTask.key.getComponent().getPackageName();

            //prize add by xiarui 2018-01-15 @{
            if (!packageName.equals("com.tencent.mm") && !packageName.equals("com.tencent.mobileqq")) {
                removedTask.setLock(mContext, false);
            }
            //@}
            
            boolean isInFmMusicList = isInList(packageName, mRemoveTaskOfFmMusicList);
            boolean isInForceStopWhiteList = isInList(packageName, mForceStopWhiteList);
            boolean isSystemApp = isSystemApp(getContext(), packageName);
            boolean isForceStop = isInFmMusicList || (!isSystemApp && !isInForceStopWhiteList);
            Log.d(TAG, "[F:" + isForceStop + ", M:" + isInFmMusicList + ", W:" + isInForceStopWhiteList + ", S:" + isSystemApp + "]-" + packageName);
            if (isForceStop) {
                Log.d(TAG, "onStackTaskRemoved : [F: " + packageName + ", appInstanceIndex--->" + removedTask.appInstanceIndex + "]");
                if (PrizeOption.PRIZE_APP_MULTI_INSTANCES && PrizeAppInstanceUtils.getInstance(mContext).supportMultiInstance(packageName)) {
                    try {
                        clearStatusbarBackgroundColor(mContext, packageName); //for Bug#46600
                        ActivityManagerNative.getDefault().forceStopPackage(packageName, removedTask.appInstanceIndex, UserHandle.myUserId());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    clearStatusbarBackgroundColor(mContext, packageName); //for Bug#46600
                    activityManager.forceStopPackage(packageName);
                }
            }
        }
        /*prize-kill the whole process-liufan-2016-07-05-end*/
    }

    //prize add by xiarui 2018-03-02 @{
    private boolean isSystemApp(Context context, String packageName) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (applicationInfo == null || (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    //--end by xiarui @}

    @Override
    public void onStackTasksRemoved(TaskStack stack) {
        // Reset the focused task
        resetFocusedTask(getFocusedTask());

        // Return all the views to the pool
        List<TaskView> taskViews = new ArrayList<>();
        taskViews.addAll(getTaskViews());
        //prize modify by xiarui 2018-01-02 don't remove current task @{
        int count = taskViews.size() - 1;
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            boolean launchedFromApp = Recents.getConfiguration().getLaunchState().launchedFromApp;
            count = launchedFromApp ? count - 1 : count;
        }
        //@}
        for (int i = count; i >= 0; i--) {   //prize modify by xiarui 2018-01-02 don't remove current task
            mViewPool.returnViewToPool(taskViews.get(i));
        }

        // Remove all the ignore tasks
        mIgnoreTasks.clear();
        // If there are no remaining tasks, then just close recents
        EventBus.getDefault().send(new AllTaskViewsDismissedEvent(
                R.string.recents_empty_message_dismissed_all));
    }

    @Override
    public void onStackTasksUpdated(TaskStack stack) {
        // Update the layout and immediately layout
        updateLayoutAlgorithm(false /* boundScroll */);
        relayoutTaskViews(AnimationProps.IMMEDIATE);

        // Rebind all the task views.  This will not trigger new resources to be loaded
        // unless they have actually changed
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
		/*prize-add by lihuangyuan,for fingerapplock-2017-07-04-start*/
	    HashMap<String,EncryptionData> map = obtainEncryptionList();
	    //Log.d(EncryptionTAG,"TaskStackView.onStackTasksUpdated!");
	    /*prize-add by lihuangyuan,for fingerapplock-2017-07-04-end*/

        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = taskViews.get(i);
		/*prize-add by lihuangyuan,for fingerapplock-2017-07-04-start*/
		if(PrizeOption.PRIZE_FINGERPRINT_APPLOCK && map!=null)
		{
	     		Task t = tv.getTask();
                String key = t.key.getComponent().getPackageName();		      
                t.isEncryption = map.get(key)!=null ? true : false;
		       //Log.d(EncryptionTAG,"TaskStackView.onStackTasksUpdated key:"+key+",isEncryption:"+t.isEncryption);
             }
	      /*prize-add by lihuangyuan,for fingerapplock-2017-07-04-end*/

            bindTaskView(tv, tv.getTask());
        }
    }

    /**** ViewPoolConsumer Implementation ****/

    @Override
    public TaskView createView(Context context) {
        /*prize modify by xiarui 2017-11-21 start*/
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            return (TaskView) mInflater.inflate(R.layout.prize_recents_task_view, this, false);
        } else {
            return (TaskView) mInflater.inflate(R.layout.recents_task_view, this, false);
        }
        /*prize modify by xiarui 2017-11-21 end*/
    }

    @Override
    public void onReturnViewToPool(TaskView tv) {
        final Task task = tv.getTask();

        // Unbind the task from the task view
        unbindTaskView(tv, task);

        // Reset the view properties and view state
        tv.clearAccessibilityFocus();
        tv.resetViewProperties();
        tv.setFocusedState(false, false /* requestViewFocus */);
        tv.setClipViewInStack(false);
        if (mScreenPinningEnabled) {
            tv.hideActionButton(false /* fadeOut */, 0 /* duration */, false /* scaleDown */, null);
        }

        // Detach the view from the hierarchy
        detachViewFromParent(tv);
        // Update the task views list after removing the task view
        updateTaskViewsList();
    }

    @Override
    public void onPickUpViewFromPool(TaskView tv, Task task, boolean isNewView) {
        // Find the index where this task should be placed in the stack
        int taskIndex = mStack.indexOfStackTask(task);
        int insertIndex = findTaskViewInsertIndex(task, taskIndex);

        // Add/attach the view to the hierarchy
        if (isNewView) {
            if (mInMeasureLayout) {
                // If we are measuring the layout, then just add the view normally as it will be
                // laid out during the layout pass
                addView(tv, insertIndex);
            } else {
                // Otherwise, this is from a bindVisibleTaskViews() call outside the measure/layout
                // pass, and we should layout the new child ourselves
                ViewGroup.LayoutParams params = tv.getLayoutParams();
                if (params == null) {
                    params = generateDefaultLayoutParams();
                }
                addViewInLayout(tv, insertIndex, params, true /* preventRequestLayout */);
                measureTaskView(tv);
                layoutTaskView(true /* changed */, tv);
            }
        } else {
            attachViewToParent(tv, insertIndex, tv.getLayoutParams());
        }
        // Update the task views list after adding the new task view
        updateTaskViewsList();

        // Bind the task view to the new task
        bindTaskView(tv, task);

        // If the doze trigger has already fired, then update the state for this task view
        if (mUIDozeTrigger.isAsleep()) {
            tv.setNoUserInteractionState();
        }

        // Set the new state for this view, including the callbacks and view clipping
        tv.setCallbacks(this);
        tv.setTouchEnabled(true);
        tv.setClipViewInStack(true);
        if (mFocusedTask == task) {
            tv.setFocusedState(true, false /* requestViewFocus */);
            if (mStartTimerIndicatorDuration > 0) {
                // The timer indicator couldn't be started before, so start it now
                tv.getHeaderView().startFocusTimerIndicator(mStartTimerIndicatorDuration);
                mStartTimerIndicatorDuration = 0;
            }
        }

        // Restore the action button visibility if it is the front most task view
        if (mScreenPinningEnabled && tv.getTask() ==
                mStack.getStackFrontMostTask(false /* includeFreeform */)) {
            tv.showActionButton(false /* fadeIn */, 0 /* fadeInDuration */);
        }
    }

    @Override
    public boolean hasPreferredData(TaskView tv, Task preferredData) {
        return (tv.getTask() == preferredData);
    }

    private void bindTaskView(TaskView tv, Task task) {
        /*prize-reset task.isLock-liufan-2016-01-30-start*/
        if(task!=null && tv != null){
            task.isLock = task.isLock(getContext());
            Task t = tv.getTask();
			if(t!=null) t.isLock = task.isLock;
        }
        /*prize-reset task.isLock-liufan-2016-01-30-end*/
		
        // Rebind the task and request that this task's data be filled into the TaskView
        tv.onTaskBound(task, mTouchExplorationEnabled, mDisplayOrientation, mDisplayRect);

        // Load the task data
        Recents.getTaskLoader().loadTaskData(task);
    }

    private void unbindTaskView(TaskView tv, Task task) {
        // Report that this task's data is no longer being used
        Recents.getTaskLoader().unloadTaskData(task);
    }

    /**** TaskViewCallbacks Implementation ****/

    @Override
    public void onTaskViewClipStateChanged(TaskView tv) {
        if (!mTaskViewsClipDirty) {
            mTaskViewsClipDirty = true;
            invalidate();
        }
    }

    /**** TaskStackLayoutAlgorithm.TaskStackLayoutAlgorithmCallbacks ****/

    @Override
    public void onFocusStateChanged(int prevFocusState, int curFocusState) {
        if (mDeferredTaskViewLayoutAnimation == null) {
            mUIDozeTrigger.poke();
            relayoutTaskViewsOnNextFrame(AnimationProps.IMMEDIATE);
        }
    }

    /**** TaskStackViewScroller.TaskStackViewScrollerCallbacks ****/

    @Override
    public void onStackScrollChanged(float prevScroll, float curScroll, AnimationProps animation) {
        mUIDozeTrigger.poke();
        if (animation != null) {
            relayoutTaskViewsOnNextFrame(animation);
        }

        if (mEnterAnimationComplete) {
            if (prevScroll > SHOW_STACK_ACTION_BUTTON_SCROLL_THRESHOLD &&
                    curScroll <= SHOW_STACK_ACTION_BUTTON_SCROLL_THRESHOLD &&
                    mStack.getTaskCount() > 0) {
                EventBus.getDefault().send(new ShowStackActionButtonEvent(true /* translate */));
            } else if (prevScroll < HIDE_STACK_ACTION_BUTTON_SCROLL_THRESHOLD &&
                    curScroll >= HIDE_STACK_ACTION_BUTTON_SCROLL_THRESHOLD) {
                EventBus.getDefault().send(new HideStackActionButtonEvent());
            }
        }
    }

    /**** EventBus Events ****/

    public final void onBusEvent(PackagesChangedEvent event) {
        // Compute which components need to be removed
        ArraySet<ComponentName> removedComponents = mStack.computeComponentsRemoved(
                event.packageName, event.userId);

        // For other tasks, just remove them directly if they no longer exist
        ArrayList<Task> tasks = mStack.getStackTasks();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            final Task t = tasks.get(i);
            if (removedComponents.contains(t.key.getComponent())) {
                final TaskView tv = getChildViewForTask(t);
                if (tv != null) {
                    // For visible children, defer removing the task until after the animation
                    tv.dismissTask();
                } else {
                    // Otherwise, remove the task from the stack immediately
                    mStack.removeTask(t, AnimationProps.IMMEDIATE, false /* fromDockGesture */);
                }
            }
        }
    }

    public final void onBusEvent(LaunchTaskEvent event) {
        // Cancel any doze triggers once a task is launched
        mUIDozeTrigger.stopDozing();
    }

    public final void onBusEvent(LaunchNextTaskRequestEvent event) {
        if (mAwaitingFirstLayout) {
            mLaunchNextAfterFirstMeasure = true;
            return;
        }

        int launchTaskIndex = mStack.indexOfStackTask(mStack.getLaunchTarget());
        /*prize modify by xiarui {launch last task} 2017-11-22 start*/
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            launchTaskIndex = mStack.getTaskCount() - 1;
        } else {
            if (launchTaskIndex != -1) {
                launchTaskIndex = Math.max(0, launchTaskIndex - 1);
            } else {
                launchTaskIndex = mStack.getTaskCount() - 1;
            }
        }
        /*prize modify by xiarui {launch last task}  2017-11-22 end*/
        if (launchTaskIndex != -1) {
            // Stop all animations
            cancelAllTaskViewAnimations();

            final Task launchTask = mStack.getStackTasks().get(launchTaskIndex);
            float curScroll = mStackScroller.getStackScroll();
            float targetScroll = mLayoutAlgorithm.getStackScrollForTaskAtInitialOffset(launchTask);
            float absScrollDiff = Math.abs(targetScroll - curScroll);
            if (getChildViewForTask(launchTask) == null || absScrollDiff > 0.35f) {
                int duration = (int) (LAUNCH_NEXT_SCROLL_BASE_DURATION +
                        absScrollDiff * LAUNCH_NEXT_SCROLL_INCR_DURATION);
                mStackScroller.animateScroll(targetScroll,
                        duration, new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().send(new LaunchTaskEvent(
                                        getChildViewForTask(launchTask), launchTask, null,
                                        INVALID_STACK_ID, false /* screenPinningRequested */));
                            }
                        });
            } else {
                EventBus.getDefault().send(new LaunchTaskEvent(getChildViewForTask(launchTask),
                        launchTask, null, INVALID_STACK_ID, false /* screenPinningRequested */));
            }

            MetricsLogger.action(getContext(), MetricsEvent.OVERVIEW_LAUNCH_PREVIOUS_TASK,
                    launchTask.key.getComponent().toString());
        } else if (mStack.getTaskCount() == 0) {
            // If there are no tasks, then just hide recents back to home.
            EventBus.getDefault().send(new HideRecentsEvent(false, true));
        }
    }

    public final void onBusEvent(LaunchTaskStartedEvent event) {
        mAnimationHelper.startLaunchTaskAnimation(event.taskView, event.screenPinningRequested,
                event.getAnimationTrigger());
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted event) {
        // Stop any scrolling
        mTouchHandler.cancelNonDismissTaskAnimations();
        mStackScroller.stopScroller();
        mStackScroller.stopBoundScrollAnimation();
        cancelDeferredTaskViewLayoutAnimation();
        
        /* prize-add-split screen  RecentsActivity HOME not use anim -liyongli-20171222-start */
        if(PrizeOption.PRIZE_SPLIT_SCREEN_HOME){
            SystemServicesProxy ssp = Recents.getSystemServices();
            if(ssp!=null && ssp.hasDockedTask()){
                return;
            }
        }/* prize-add-split screen-liyongli-20171222-end */

        /**prize add by xiarui 2018-05-18 for bug#57182\bug#58877
         * don't run exit home animation while delete all animation is running **/
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            if (isDeleteAllTasksAnimation) {
                LogUtils.d(TAG, "don't run exit home animation while delete all animation is running");
                return;
            }
        }
        /**end by xiarui 2018-05-18*/

        // Start the task animations
        mAnimationHelper.startExitToHomeAnimation(event.animated, event.getAnimationTrigger());

        // Dismiss the freeform workspace background
        int taskViewExitToHomeDuration = TaskStackAnimationHelper.EXIT_TO_HOME_TRANSLATION_DURATION;
        animateFreeformWorkspaceBackgroundAlpha(0, new AnimationProps(taskViewExitToHomeDuration,
                Interpolators.FAST_OUT_SLOW_IN));
                
    }

    public final void onBusEvent(DismissFocusedTaskViewEvent event) {
        if (mFocusedTask != null) {
            TaskView tv = getChildViewForTask(mFocusedTask);
            if (tv != null) {
                tv.dismissTask();
            }
            resetFocusedTask(mFocusedTask);
        }
    }

    public final void onBusEvent(DismissTaskViewEvent event) {
        // For visible children, defer removing the task until after the animation
        mAnimationHelper.startDeleteTaskAnimation(event.taskView, event.getAnimationTrigger());
    }

    public final void onBusEvent(final SendClearTaskServiceEvent event) {
        final ArrayList<Task> tasks = new ArrayList<>(mStack.getStackTasks());
        boolean launchedFromApp = Recents.getConfiguration().getLaunchState().launchedFromApp;
        Intent intent = new Intent("prize.intent.action.REQUEST_APP_CLEAN_RUNNING");
        intent.setPackage("com.prize.smartcleaner");
        if (launchedFromApp) {
            Task task = tasks.get(tasks.size() - 1);
            intent.putExtra("KillAppFilterName", task.key.getComponent().getPackageName());
            intent.putExtra("KillAppFilterUserId", task.key.userId);
            intent.putExtra("KillAppFilterAppIndex", task.appInstanceIndex);
            intent.putExtra("KillAppFilterActivityName", task.key.getComponent().getClassName());
        }
        mContext.startServiceAsUser(intent, UserHandle.CURRENT);
    }

    public final void onBusEvent(final DismissAllTaskViewsEvent event) {
        // Keep track of the tasks which will have their data removed
        ArrayList<Task> tasks = new ArrayList<>(mStack.getStackTasks());
        /*prize add by xiarui 2017-12-04 start*/
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            Recents.isClearRecentsActivity = false;
        }
        /*prize add by xiarui 2017-12-04 end*/
        mAnimationHelper.startDeleteAllTasksAnimation(getTaskViews(), event.getAnimationTrigger());
        event.addPostAnimationCallback(new Runnable() {
            @Override
            public void run() {
                // Announce for accessibility
                announceForAccessibility(getContext().getString(
                        R.string.accessibility_recents_all_items_dismissed));

                // Remove all tasks and delete the task data for all tasks
                mStack.removeAllTasks(getContext()); //prize modify by xiarui 2017-12-04
                //prize modify by xiarui 2018-01-02 don't remove current task @{
                int count = tasks.size() - 1;
                if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                    boolean launchedFromApp = Recents.getConfiguration().getLaunchState().launchedFromApp;
                    count = launchedFromApp ? count - 1 : count;
                }
                //@}
				if (!PrizeOption.PRIZE_SMART_CLEANER) {
					for (int i = count; i >= 0; i--) {
						/*prize add by xiarui 2017-12-04 start*/
						if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
							Task t = tasks.get(i);
							t.isLock = t.isLock(getContext());
							if (t == null || t.isLock) {
								continue;
							}
						}
						/*prize add by xiarui 2017-12-04 end*/
						EventBus.getDefault().send(new DeleteTaskDataEvent(tasks.get(i)));
					}
				}
                //prize add by xiarui 2018-03-26 for bug#53817 @{
                if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                    isDeleteAllTasksAnimation = false;
                }
                //@}
                MetricsLogger.action(getContext(), MetricsEvent.OVERVIEW_DISMISS_ALL);
            }
        });

        /*prize add by xiarui 2017-12-04 start*/
        if (!PrizeOption.PRIZE_SMART_CLEANER) {
            if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
                event.addPostAnimationCallback(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().send(new PrizeKillAllProcessEvent());
                    }
                });
            }
        }
        /*prize add by xiarui 2017-12-04 end*/
    }


    /**prize add by xiarui 2017-12-04 start**/
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //EventBus.getDefault().send(new AllTaskViewsDismissedEvent(R.string.recents_empty_message_dismissed_all));
            EventBus.getDefault().send(new PrizeShowCleanResultByToastEvent());
        }
    };

    public final void onBusEvent(PrizeKillAllProcessEvent event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //long start = System.currentTimeMillis();
                killAll(TaskStackView.this.getContext().getApplicationContext(), mStack.getStackTasks(), false);
                //LogUtils.d("KillAll-Time", "TotalTime : " + (System.currentTimeMillis() - start));
                mHandler.sendEmptyMessage(0);
            }
        }).start();

    }
    /**prize add by xiarui 2017-12-04 end**/

    public final void onBusEvent(TaskViewDismissedEvent event) {
        // Announce for accessibility
        announceForAccessibility(getContext().getString(
                R.string.accessibility_recents_item_dismissed, event.task.title));

        // Remove the task from the stack
        //prize add by xiarui 2017-12-16 for Bug#45761 @{
        if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            mStack.removeTask(event.task, event.animation, false /* fromDockGesture */, event.forceStop);
        } else {
            mStack.removeTask(event.task, event.animation, false /* fromDockGesture */);
        }
        //--end by xiarui @}
        EventBus.getDefault().send(new DeleteTaskDataEvent(event.task));

        MetricsLogger.action(getContext(), MetricsEvent.OVERVIEW_DISMISS,
                event.task.key.getComponent().toString());
    }

    public final void onBusEvent(FocusNextTaskViewEvent event) {
        // Stop any scrolling
        mStackScroller.stopScroller();
        mStackScroller.stopBoundScrollAnimation();

        setRelativeFocusedTask(true, false /* stackTasksOnly */, true /* animated */, false,
                event.timerIndicatorDuration);
    }

    public final void onBusEvent(FocusPreviousTaskViewEvent event) {
        // Stop any scrolling
        mStackScroller.stopScroller();
        mStackScroller.stopBoundScrollAnimation();

        setRelativeFocusedTask(false, false /* stackTasksOnly */, true /* animated */);
    }

    public final void onBusEvent(UserInteractionEvent event) {
        // Poke the doze trigger on user interaction
        mUIDozeTrigger.poke();

        RecentsDebugFlags debugFlags = Recents.getDebugFlags();
        if (debugFlags.isFastToggleRecentsEnabled() && mFocusedTask != null) {
            TaskView tv = getChildViewForTask(mFocusedTask);
            if (tv != null) {
                tv.getHeaderView().cancelFocusTimerIndicator();
            }
        }
    }

    public final void onBusEvent(DragStartEvent event) {
        // Ensure that the drag task is not animated
        addIgnoreTask(event.task);

        if (event.task.isFreeformTask()) {
            // Animate to the front of the stack
            mStackScroller.animateScroll(mLayoutAlgorithm.mInitialScrollP, null);
        }

        // Enlarge the dragged view slightly
        float finalScale = event.taskView.getScaleX() * DRAG_SCALE_FACTOR;
        mLayoutAlgorithm.getStackTransform(event.task, getScroller().getStackScroll(),
                mTmpTransform, null);
        mTmpTransform.scale = finalScale;
        mTmpTransform.translationZ = mLayoutAlgorithm.mMaxTranslationZ + 1;
        mTmpTransform.dimAlpha = 0f;
        updateTaskViewToTransform(event.taskView, mTmpTransform,
                new AnimationProps(DRAG_SCALE_DURATION, Interpolators.FAST_OUT_SLOW_IN));
    }

    public final void onBusEvent(DragStartInitializeDropTargetsEvent event) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.hasFreeformWorkspaceSupport()) {
            event.handler.registerDropTargetForCurrentDrag(mStackDropTarget);
            event.handler.registerDropTargetForCurrentDrag(mFreeformWorkspaceDropTarget);
        }
    }

    public final void onBusEvent(DragDropTargetChangedEvent event) {
        AnimationProps animation = new AnimationProps(SLOW_SYNC_STACK_DURATION,
                Interpolators.FAST_OUT_SLOW_IN);
        boolean ignoreTaskOverrides = false;
        if (event.dropTarget instanceof TaskStack.DockState) {
            // Calculate the new task stack bounds that matches the window size that Recents will
            // have after the drop
            final TaskStack.DockState dockState = (TaskStack.DockState) event.dropTarget;
            Rect systemInsets = new Rect(mStableLayoutAlgorithm.mSystemInsets);
            // When docked, the nav bar insets are consumed and the activity is measured without
            // insets.  However, the window bounds include the insets, so we need to subtract them
            // here to make them identical.
            int height = getMeasuredHeight();
            height -= systemInsets.bottom;
            systemInsets.bottom = 0;
            mStackBounds.set(dockState.getDockedTaskStackBounds(mDisplayRect, getMeasuredWidth(),
                    height, mDividerSize, systemInsets,
                    mLayoutAlgorithm, getResources(), mWindowRect));
            mLayoutAlgorithm.setSystemInsets(systemInsets);
            mLayoutAlgorithm.initialize(mDisplayRect, mWindowRect, mStackBounds,
                    TaskStackLayoutAlgorithm.StackState.getStackStateForStack(mStack));
            updateLayoutAlgorithm(true /* boundScroll */);
            ignoreTaskOverrides = true;
        } else {
            // Restore the pre-drag task stack bounds, but ensure that we don't layout the dragging
            // task view, so add it back to the ignore set after updating the layout
            removeIgnoreTask(event.task);
            updateLayoutToStableBounds();
            addIgnoreTask(event.task);
        }
        relayoutTaskViews(animation, null /* animationOverrides */, ignoreTaskOverrides);
    }

    public final void onBusEvent(final DragEndEvent event) {
        // We don't handle drops on the dock regions
        if (event.dropTarget instanceof TaskStack.DockState) {
            // However, we do need to reset the overrides, since the last state of this task stack
            // view layout was ignoring task overrides (see DragDropTargetChangedEvent handler)
            mLayoutAlgorithm.clearUnfocusedTaskOverrides();
            return;
        }

        boolean isFreeformTask = event.task.isFreeformTask();
        boolean hasChangedStacks =
                (!isFreeformTask && event.dropTarget == mFreeformWorkspaceDropTarget) ||
                        (isFreeformTask && event.dropTarget == mStackDropTarget);

        if (hasChangedStacks) {
            // Move the task to the right position in the stack (ie. the front of the stack if
            // freeform or the front of the stack if fullscreen). Note, we MUST move the tasks
            // before we update their stack ids, otherwise, the keys will have changed.
            if (event.dropTarget == mFreeformWorkspaceDropTarget) {
                mStack.moveTaskToStack(event.task, FREEFORM_WORKSPACE_STACK_ID);
            } else if (event.dropTarget == mStackDropTarget) {
                mStack.moveTaskToStack(event.task, FULLSCREEN_WORKSPACE_STACK_ID);
            }
            updateLayoutAlgorithm(true /* boundScroll */);

            // Move the task to the new stack in the system after the animation completes
            event.addPostAnimationCallback(new Runnable() {
                @Override
                public void run() {
                    SystemServicesProxy ssp = Recents.getSystemServices();
                    ssp.moveTaskToStack(event.task.key.id, event.task.key.stackId);
                }
            });
        }

        // Restore the task, so that relayout will apply to it below
        removeIgnoreTask(event.task);

        // Convert the dragging task view back to its final layout-space rect
        Utilities.setViewFrameFromTranslation(event.taskView);

        // Animate all the tasks into place
        ArrayMap<Task, AnimationProps> animationOverrides = new ArrayMap<>();
        animationOverrides.put(event.task, new AnimationProps(SLOW_SYNC_STACK_DURATION,
                Interpolators.FAST_OUT_SLOW_IN,
                event.getAnimationTrigger().decrementOnAnimationEnd()));
        relayoutTaskViews(new AnimationProps(SLOW_SYNC_STACK_DURATION,
                Interpolators.FAST_OUT_SLOW_IN));
        event.getAnimationTrigger().increment();
    }

    public final void onBusEvent(final DragEndCancelledEvent event) {
        // Restore the pre-drag task stack bounds, including the dragging task view
        removeIgnoreTask(event.task);
        updateLayoutToStableBounds();

        // Convert the dragging task view back to its final layout-space rect
        Utilities.setViewFrameFromTranslation(event.taskView);

        // Animate all the tasks into place
        ArrayMap<Task, AnimationProps> animationOverrides = new ArrayMap<>();
        animationOverrides.put(event.task, new AnimationProps(SLOW_SYNC_STACK_DURATION,
                Interpolators.FAST_OUT_SLOW_IN,
                event.getAnimationTrigger().decrementOnAnimationEnd()));
        relayoutTaskViews(new AnimationProps(SLOW_SYNC_STACK_DURATION,
                Interpolators.FAST_OUT_SLOW_IN));
        event.getAnimationTrigger().increment();
    }

    public final void onBusEvent(IterateRecentsEvent event) {
        if (!mEnterAnimationComplete) {
            // Cancel the previous task's window transition before animating the focused state
            EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(null));
        }
    }

    public final void onBusEvent(EnterRecentsWindowAnimationCompletedEvent event) {
        mEnterAnimationComplete = true;

        if (mStack.getTaskCount() > 0) {
            // Start the task enter animations
            mAnimationHelper.startEnterAnimation(event.getAnimationTrigger());

            // Add a runnable to the post animation ref counter to clear all the views
            event.addPostAnimationCallback(new Runnable() {
                @Override
                public void run() {
                    // Start the dozer to trigger to trigger any UI that shows after a timeout
                    mUIDozeTrigger.startDozing();

                    // Update the focused state here -- since we only set the focused task without
                    // requesting view focus in onFirstLayout(), actually request view focus and
                    // animate the focused state if we are alt-tabbing now, after the window enter
                    // animation is completed
                    if (mFocusedTask != null) {
                        RecentsConfiguration config = Recents.getConfiguration();
                        RecentsActivityLaunchState launchState = config.getLaunchState();
                        setFocusedTask(mStack.indexOfStackTask(mFocusedTask),
                                false /* scrollToTask */, launchState.launchedWithAltTab);
                        TaskView focusedTaskView = getChildViewForTask(mFocusedTask);
                        if (mTouchExplorationEnabled && focusedTaskView != null) {
                            focusedTaskView.requestAccessibilityFocus();
                        }
                    }

                    EventBus.getDefault().send(new EnterRecentsTaskStackAnimationCompletedEvent());
                }
            });
        }
    }

    public final void onBusEvent(UpdateFreeformTaskViewVisibilityEvent event) {
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = taskViews.get(i);
            Task task = tv.getTask();
            if (task.isFreeformTask()) {
                tv.setVisibility(event.visible ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    public final void onBusEvent(final MultiWindowStateChangedEvent event) {
        if (event.inMultiWindow || !event.showDeferredAnimation) {
            setTasks(event.stack, true /* allowNotifyStackChanges */);
        } else {
            // Reset the launch state before handling the multiwindow change
            RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
            launchState.reset();

            // Defer until the next frame to ensure that we have received all the system insets, and
            // initial layout updates
            event.getAnimationTrigger().increment();
            post(new Runnable() {
                @Override
                public void run() {
                    // Scroll the stack to the front to see the undocked task
                    mAnimationHelper.startNewStackScrollAnimation(event.stack,
                            event.getAnimationTrigger());
                    event.getAnimationTrigger().decrement();
                }
            });
        }
    }

    /**
     * prize add by xiarui 2017-11-17
     * update {@link TaskViewThumbnail#mDisplayOrientation} when configuration change
     * fix a bug : Image compression deformation when configuration changed
     */
    public void updateConfigurationChange() {
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = taskViews.get(i);
            bindTaskView(tv, tv.getTask());
        }
    }

    public final void onBusEvent(ConfigurationChangedEvent event) {
        if (event.fromDeviceOrientationChange) {
            mDisplayOrientation = Utilities.getAppConfiguration(mContext).orientation;
            mDisplayRect = Recents.getSystemServices().getDisplayRect();

			/*prize add by xiarui 2017-11-17 start*/
			if (PrizeOption.PRIZE_SYSTEMUI_RECENTS) {
            	updateConfigurationChange(); 
			}
			/*prize add by xiarui 2017-11-17 end*/
			
            // Always stop the scroller, otherwise, we may continue setting the stack scroll to the
            // wrong bounds in the new layout
            mStackScroller.stopScroller();
        }
        reloadOnConfigurationChange();

        // Notify the task views of the configuration change so they can reload their resources
        if (!event.fromMultiWindow) {
            mTmpTaskViews.clear();
            mTmpTaskViews.addAll(getTaskViews());
            mTmpTaskViews.addAll(mViewPool.getViews());
            int taskViewCount = mTmpTaskViews.size();
            for (int i = 0; i < taskViewCount; i++) {
                mTmpTaskViews.get(i).onConfigurationChanged();
            }
        }

        // Trigger a new layout and update to the initial state if necessary
        if (event.fromMultiWindow) {
            mInitialState = INITIAL_STATE_UPDATE_LAYOUT_ONLY;
            requestLayout();
        } else if (event.fromDeviceOrientationChange) {
            mInitialState = INITIAL_STATE_UPDATE_ALL;
            requestLayout();
        }
    }

    public final void onBusEvent(RecentsGrowingEvent event) {
        mResetToInitialStateWhenResized = true;
    }

    public void reloadOnConfigurationChange() {
        mStableLayoutAlgorithm.reloadOnConfigurationChange(getContext());
        mLayoutAlgorithm.reloadOnConfigurationChange(getContext());
    }

    /**
     * Starts an alpha animation on the freeform workspace background.
     */
    private void animateFreeformWorkspaceBackgroundAlpha(int targetAlpha,
            AnimationProps animation) {
        if (mFreeformWorkspaceBackground.getAlpha() == targetAlpha) {
            return;
        }

        Utilities.cancelAnimationWithoutCallbacks(mFreeformWorkspaceBackgroundAnimator);
        mFreeformWorkspaceBackgroundAnimator = ObjectAnimator.ofInt(mFreeformWorkspaceBackground,
                Utilities.DRAWABLE_ALPHA, mFreeformWorkspaceBackground.getAlpha(), targetAlpha);
        mFreeformWorkspaceBackgroundAnimator.setStartDelay(
                animation.getDuration(AnimationProps.ALPHA));
        mFreeformWorkspaceBackgroundAnimator.setDuration(
                animation.getDuration(AnimationProps.ALPHA));
        mFreeformWorkspaceBackgroundAnimator.setInterpolator(
                animation.getInterpolator(AnimationProps.ALPHA));
        mFreeformWorkspaceBackgroundAnimator.start();
    }

    /**
     * Returns the insert index for the task in the current set of task views. If the given task
     * is already in the task view list, then this method returns the insert index assuming it
     * is first removed at the previous index.
     *
     * @param task the task we are finding the index for
     * @param taskIndex the index of the task in the stack
     */
    private int findTaskViewInsertIndex(Task task, int taskIndex) {
        if (taskIndex != -1) {
            List<TaskView> taskViews = getTaskViews();
            boolean foundTaskView = false;
            int taskViewCount = taskViews.size();
            for (int i = 0; i < taskViewCount; i++) {
                Task tvTask = taskViews.get(i).getTask();
                if (tvTask == task) {
                    foundTaskView = true;
                } else if (taskIndex < mStack.indexOfStackTask(tvTask)) {
                    if (foundTaskView) {
                        return i - 1;
                    } else {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Reads current system flags related to accessibility and screen pinning.
     */
    private void readSystemFlags() {
        SystemServicesProxy ssp = Recents.getSystemServices();
        mTouchExplorationEnabled = ssp.isTouchExplorationEnabled();
        mScreenPinningEnabled = ssp.getSystemSetting(getContext(),
                Settings.System.LOCK_TO_APP_ENABLED) != 0;
    }

    public void dump(String prefix, PrintWriter writer) {
        String innerPrefix = prefix + "  ";
        String id = Integer.toHexString(System.identityHashCode(this));

        writer.print(prefix); writer.print(TAG);
        writer.print(" hasDefRelayout=");
        writer.print(mDeferredTaskViewLayoutAnimation != null ? "Y" : "N");
        writer.print(" clipDirty="); writer.print(mTaskViewsClipDirty ? "Y" : "N");
        writer.print(" awaitingFirstLayout="); writer.print(mAwaitingFirstLayout ? "Y" : "N");
        writer.print(" initialState="); writer.print(mInitialState);
        writer.print(" inMeasureLayout="); writer.print(mInMeasureLayout ? "Y" : "N");
        writer.print(" enterAnimCompleted="); writer.print(mEnterAnimationComplete ? "Y" : "N");
        writer.print(" touchExplorationOn="); writer.print(mTouchExplorationEnabled ? "Y" : "N");
        writer.print(" screenPinningOn="); writer.print(mScreenPinningEnabled ? "Y" : "N");
        writer.print(" numIgnoreTasks="); writer.print(mIgnoreTasks.size());
        writer.print(" numViewPool="); writer.print(mViewPool.getViews().size());
        writer.print(" stableStackBounds="); writer.print(Utilities.dumpRect(mStableStackBounds));
        writer.print(" stackBounds="); writer.print(Utilities.dumpRect(mStackBounds));
        writer.print(" stableWindow="); writer.print(Utilities.dumpRect(mStableWindowRect));
        writer.print(" window="); writer.print(Utilities.dumpRect(mWindowRect));
        writer.print(" display="); writer.print(Utilities.dumpRect(mDisplayRect));
        writer.print(" orientation="); writer.print(mDisplayOrientation);
        writer.print(" [0x"); writer.print(id); writer.print("]");
        writer.println();

        if (mFocusedTask != null) {
            writer.print(innerPrefix);
            writer.print("Focused task: ");
            mFocusedTask.dump("", writer);
        }

        mLayoutAlgorithm.dump(innerPrefix, writer);
        mStackScroller.dump(innerPrefix, writer);
    }


    /*prize-dismiss all task-liufan-2016-11-18-start*/
	public void shouldFinishActivity(){
        //if (mStack.getTaskCount() == 0) {
            EventBus.getDefault().send(new AllTaskViewsDismissedEvent(R.string.recents_empty_message_dismissed_all));
        //}
	}

    public void dismissAllTasks(final Runnable r) {
        post(new Runnable() {
            @Override
            public void run() {
                final RecentsTaskLoader loader = Recents.getTaskLoader();
                final SystemServicesProxy ssp = Recents.getSystemServices();
                ArrayList<Task> tasks = new ArrayList<Task>();
                for (int i = 0; i < mStack.getStackTasks().size(); i++) {
                    Task task = mStack.getStackTasks().get(i);
                    task.isLock = task.isLock(getContext());
                    if(task != null && !task.isLock){
                        tasks.add(task);
                    }
                }

                // Remove visible TaskViews
                long dismissDelay = 0;
				List<TaskView> taskViews = getTaskViews();
                int childCount = taskViews.size();
                int lockNum = 0;
                for (int i = 0; i < childCount; i++) {
                    TaskView tv = (TaskView) getChildAt(i);
                    if (!tv.getTask().isLock(tv.getContext())) {
                        lockNum++;
                    }
                }
                if(lockNum == 0){
                    deleteTask(r, tasks);
                    return;
                }
                int delay = TaskStackAnimationHelper.DOUBLE_FRAME_OFFSET_MS;
                int index = 0;
                for (int i = 0; i < childCount; i++) {
                    TaskView tv = (TaskView) getChildAt(i);
                    if (tv.getTask().isLock(tv.getContext())) {
                        continue;
                    }
                    //two step：
                    //a,delete view from ViewPool
                    //b,delete Task from mStack
                    if(index != lockNum - 1){
                        tv.dismissTask(dismissDelay);  //step a
                    }else{
                        final ArrayList<Task> tks = new ArrayList<Task>();
                        tks.addAll(tasks);
                        tv.dismissTask(dismissDelay,new Runnable() {
                            @Override
                            public void run() {
                                //step b
                                deleteTask(r,tks);
                            }
                        });
                    }
                    index++;
                    dismissDelay += delay;
                }
            }
        });
    }

    void deleteTask(Runnable r, ArrayList<Task> tks){
        long time1 = System.currentTimeMillis();
        int size = tks.size();
        Log.d(TAG,"deleteTask:size--->"+size);
        for (int i = 0; i < size; i++) {
            Task t = tks.get(i);
            if (mStack.getStackTasks().contains(t)) {
                Log.d(TAG,"deleteTask:packageName--->"+t.key.getComponent().getPackageName());
                mStack.removeTask(t,null,false);
				
                // Remove any stored data from the loader
                RecentsTaskLoader loader = Recents.getTaskLoader();
                loader.deleteTaskData(t, false);

                // Remove the task from activity manager
                SystemServicesProxy ssp = Recents.getSystemServices();
                ssp.removeTask(t.key.id);
            }
        }
        long time2 = System.currentTimeMillis();
        Log.d(TAG,"deleteTask:usetime--->"+(time2-time1));
        time1 = System.currentTimeMillis();
        killAll(getContext(),mStack.getStackTasks(),false);
        time2 = System.currentTimeMillis();
        Log.d(TAG,"killAll:usetime--->"+(time2-time1));
        //requestSynchronizeStackViewsWithModel();
        //synchronizeStackViewsWithModel();
        if(r!=null){
            r.run();
        }
    }

    private boolean isKillDeep = false;// removeTask_WhiteList be used only the value is true
    public static final String[] mRemoveTaskOfFmMusicList = new String[] {    //prize add xiarui 2017-12-11
            //contains music will be hide
            "music",
            "com.kugou.android",
            "cn.kuwo.player",
            "com.ting.mp3.android",
            "com.duomi.android",
            "com.tencent.karaoke",

            //FM
            "fm.qingting.qtradio",
            "com.douban.radio",
            "com.ximalaya.ting.android",
            "com.sds.android.ttpod",
            "bubei.tingshu",
            "fm.xiami.main",
            "com.android.fmradio",
            "com.android.soundrecorder",
    };

    public static final String[] mForceStopWhiteList = new String[] {
            "com.tencent.mobileqq",
            "com.tencent.mm",
    };

    public static final String[] removeTask_WhiteList = new String[]{//these app skip forceStopPackage(deep kill)
        "com.android.deskclock",
        "com.android.calendar",
        "com.android.dialer",
        "com.mediatek.mtklogger"
    };
    //these app must forceStopPackage
    public static final String removeTask_BlackList = "com.prize.music,com.android.soundrecorder,com.android.fmradio";

    public static final String[] kill_WhiteList = new String[]{
        "com.prize.weather",
        "com.android.dialer",
        "com.mediatek.security",
        "com.example.framerecorder",
        "android.process.acore",
        "android.process.media",
        "com.prize.flash",
        "com.android.launcher3",
        "com.android.floatwindow",
        "com.android.gallery3d",
        "com.tencent.mm:push",
        "com.tencent.mobileqq:MSF",
        "com.mediatek.mtklogger",
        "com.prize.appcenter:remote",
        "com.prize.luckymonkeyhelper",
		"com.android.prizefloatwindow",
		"com.android.lpserver",
		"com.prize.prizeappoutad",
         "com.android.systemui", //prize add by xiarui for {BUG #43049} 20171122
		/*-prize-add by lihuangyuan for rootcheck do not kill rootcheck -2017-04-26-start-*/
		"com.prize.rootcheck"
		/*-prize-add by lihuangyuan for rootcheck do not kill rootcheck -2017-04-26-end-*/

    };

    private static final String[] qs_DumpList = new String[]{
        
    };
    /*-prize add by lihuangyuan,for whitelist -2017-11-08-start-*/
    public static boolean isInList(String pkgname,String []arylist)
	{
	   	if(arylist == null)return false;
		for(int i=0;i<arylist.length;i++)
		{
			if(pkgname.contains(arylist[i]))
			{
				return true;
			}
		}
		return false;
	}

    /*-prize add by lihuangyuan,for whitelist -2017-11-08-end-*/
    public static void killAll(Context context, ArrayList<Task> lockList, boolean isDump) {
        //获取一个ActivityManager 对象
        ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        //获取系统中所有正在运行的进程
        List<RunningAppProcessInfo> appProcessInfos = activityManager
                .getRunningAppProcesses();
        /*-prize add by lihuangyuan,for whitelist -2017-11-08-start- -temp-delete-
        WhiteListManager whiteListMgr = (WhiteListManager) context.getSystemService(Context.WHITELIST_SERVICE);
        String[] hidelist = whiteListMgr.getPurebackgroundHideList();
	    /*-prize add by lihuangyuan,for whitelist -2017-11-08-end-*/
        String[] hidelist = null;
        boolean launchedFromApp = Recents.getConfiguration().getLaunchState().launchedFromApp;
        Log.d(TAG, "killAll --- launchedFromApp = " + launchedFromApp);
        for (RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            //first, jump kill_WhiteList
            boolean isInWhiteList = false;
            for (String name : kill_WhiteList) {
                if (appProcessInfo.processName.contains(name)) {//屏蔽天气APP
                    isInWhiteList = true;
                    break;
                }
            }
            if (isDump) {
                for (String name : qs_DumpList) {
                    if (appProcessInfo.processName.contains(name)) {
                        isInWhiteList = true;
                        break;
                    }
                }
            }
            if (isInWhiteList) {
                continue;
            }
            //secend, kill third app and black list app
            String[] pkgList = appProcessInfo.pkgList;
            for (int i = 0; i < pkgList.length; i++) {
                String pkName = pkgList[i];
                boolean isThirdAppAndUnlock = false;
		        /*-prize add by lihuangyuan,for whitelist -2017-11-08-start-*/
                boolean isInHideList = isInList(pkName, hidelist);
                boolean isInFmMusicList = isInList(pkName, mRemoveTaskOfFmMusicList);
                Log.d(TAG, "killAll " + pkName + " isInHideList:" + isInHideList);
		        /*-prize add by lihuangyuan,for whitelist -2017-11-08-end-*/
                if (isContainsPkg(removeTask_BlackList, pkName) || !isInHideList || isInFmMusicList) {//third app, eg: aiqiyi;
                    isThirdAppAndUnlock = true;
                    //prize modify by xiarui 2018-01-02 don't remove current task
                    for (int j = lockList.size() - 1; j >= 0; j--) {
                    //for (int j = 0; j < lockList.size(); j++) {
                        Task task = lockList.get(j);
                        String pkg = task.key.getComponent().getPackageName();
                        if ((task.isLock || (launchedFromApp && (j == lockList.size() - 1))) && pkName.contains(pkg)) {
                            isThirdAppAndUnlock = false;
                        }
                    }
                }

                if (isThirdAppAndUnlock) {
                    if (PrizeOption.PRIZE_APP_MULTI_INSTANCES && PrizeAppInstanceUtils.getInstance(context).supportMultiInstance(pkName)) {
                        /*try {-temp-delete-
                            int appInstanceIndex = ActivityManagerNative.getDefault().getAppInstanceIndex(appProcessInfo.pid);
                            Log.d(TAG, "dismissAllTasks:kill third app------forceStopPackage--->" + pkName + ", appInstanceIndex----->" + appInstanceIndex);
                            clearStatusbarBackgroundColor(context, pkName);
                            ActivityManagerNative.getDefault().forceStopPackage(pkName, appInstanceIndex, UserHandle.myUserId());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }*/
                    } else {
                        Log.d(TAG, "dismissAllTasks:kill third app------forceStopPackage------->" + pkName);
                        clearStatusbarBackgroundColor(context, pkName);
                        activityManager.forceStopPackage(pkName);//kill all process in this app
                    }
                }
            }

            //third,kill the importance of app more than RunningAppProcessInfo.IMPORTANCE_VISIBLE(200)
            Log.d(TAG, "dismissAllTasks:processName--->" + appProcessInfo.processName);
            Log.d(TAG, "dismissAllTasks:appProcessInfo.importance--->" + appProcessInfo.importance);
            if (appProcessInfo.importance >= RunningAppProcessInfo.IMPORTANCE_VISIBLE) {// >200
                String[] pkNameList = appProcessInfo.pkgList;//进程下的所有包名
                boolean isContain = false;
                for (int i = 0; i < pkNameList.length; i++) {
                    String pkName = pkNameList[i];
                    for (int j = lockList.size() - 1; j >= 0; j--) {
                    //for (int j = 0; j < lockList.size(); j++) {
                        Task task = lockList.get(j);
                        String pkg = task.key.getComponent().getPackageName();
                        //prize modify by xiarui 2018-01-02 don't remove current task
                        if ((task.isLock || (launchedFromApp && (j == lockList.size() - 1))) && pkName.contains(pkg)) {
                            isContain = true;
                            break;
                        }
                    }
                }
                if (isContain) {
                    continue;
                }
                for (int i = 0; i < pkNameList.length; i++) {
                    String pkName = pkNameList[i];
                    boolean flag = false;
                    for (int j = lockList.size() - 1; j >= 0; j--) {
                    //for (int j = 0; j < lockList.size(); j++) {
                        Task task = lockList.get(j);
                        String pkg = task.key.getComponent().getPackageName();
                        //prize modify by xiarui 2018-01-02 don't remove current task
                        if ((task.isLock || (launchedFromApp && (j == lockList.size() - 1))) && pkName.contains(pkg)) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        continue;
                    }
                    Log.d(TAG, "dismissAllTasks:kill packageName--->" + pkName);
                    clearStatusbarBackgroundColor(context, pkName);
                    activityManager.killBackgroundProcesses(pkName);//杀死该进程              
                }
            }
        }
    }

    private static String getAppList(Context context, String key){
        return Settings.System.getString(context.getContentResolver(), key);
    }

    private static boolean isContainsPkg(String str, String pkg){
        if(str != null && pkg != null && str.contains(pkg)){
            String[] attr = str.split(",");
            for(String s : attr){
                if(s.equals(pkg)){
                    return true;
                }
            }
        }
        return false;
    }

    public static void clearStatusbarBackgroundColor(Context context, String pkg){
        if(PhoneStatusBar.SET_STATUSBAR_BACKGROUND_PKG != null && pkg.contains(PhoneStatusBar.SET_STATUSBAR_BACKGROUND_PKG)){
            StatusBarManager mStatusBarManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
            mStatusBarManager.clearStatusBarBackgroundColor();
        }
    }
    /*prize-dismiss all task-liufan-2016-11-18-end*/

}
