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
 * limitations under the License.
 */

package com.android.systemui.stackdivider;

import static android.view.PointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW;
import static android.view.PointerIcon.TYPE_VERTICAL_DOUBLE_ARROW;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.Nullable;
import android.app.ActivityManager.StackId;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.internal.policy.DividerSnapAlgorithm.SnapTarget;
import com.android.internal.policy.DockedDividerUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.activity.UndockingTaskEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.events.ui.RecentsGrowingEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.stackdivider.events.StartedDragingEvent;
import com.android.systemui.stackdivider.events.StoppedDragingEvent;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.phone.NavigationBarGestureHelper;

import com.mediatek.common.prizeoption.PrizeOption;
import com.android.systemui.recents.events.component.PrizeOpenPendingIntentEvent;
import android.util.DisplayMetrics;
import android.util.Log;
import static android.app.ActivityManager.StackId.DOCKED_STACK_ID;
import static android.app.ActivityManager.StackId.HOME_STACK_ID;
import static android.app.ActivityManager.StackId.FULLSCREEN_WORKSPACE_STACK_ID;
import android.app.ActivityManagerNative;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;
import com.android.systemui.recents.events.activity.QQdockedTopTaskEvent;

/**
 * Docked stack divider.
 */
public class DividerView extends FrameLayout implements OnTouchListener,
        OnComputeInternalInsetsListener {

    static final long TOUCH_ANIMATION_DURATION = 150;
    static final long TOUCH_RELEASE_ANIMATION_DURATION = 200;

    public static final int INVALID_RECENTS_GROW_TARGET = -1;

    private static final int LOG_VALUE_RESIZE_50_50 = 0;
    private static final int LOG_VALUE_RESIZE_DOCKED_SMALLER = 1;
    private static final int LOG_VALUE_RESIZE_DOCKED_LARGER = 2;

    private static final int LOG_VALUE_UNDOCK_MAX_DOCKED = 0;
    private static final int LOG_VALUE_UNDOCK_MAX_OTHER = 1;

    private static final int TASK_POSITION_SAME = Integer.MAX_VALUE;
    
    /* prize-modify-split screen-liyongli-20170608-start */
    //private static final boolean SWAPPING_ENABLED = false;
    private static final boolean SWAPPING_ENABLED = PrizeOption.PRIZE_SPLIT_SCREEN_SWAP;
    /* prize-modify-split screen-liyongli-20170608-end */

    /**
     * How much the background gets scaled when we are in the minimized dock state.
     */
    private static final float MINIMIZE_DOCK_SCALE = 0f;
    private static final float ADJUSTED_FOR_IME_SCALE = 0.5f;

    private static final PathInterpolator SLOWDOWN_INTERPOLATOR =
            new PathInterpolator(0.5f, 1f, 0.5f, 1f);
    private static final PathInterpolator DIM_INTERPOLATOR =
            new PathInterpolator(.23f, .87f, .52f, -0.11f);
    private static final Interpolator IME_ADJUST_INTERPOLATOR =
            new PathInterpolator(0.2f, 0f, 0.1f, 1f);

    private static final long ONE_MS_IN_NS = 1000000;
    private static final long ONE_S_IN_NS = ONE_MS_IN_NS * 1000;

    private static final int MSG_RESIZE_STACK = 0;

    private DividerHandleView mHandle;
    private View mBackground;
    private MinimizedDockShadow mMinimizedShadow;
    private PrizeDividerMenu mMenuManager = null;  //prize-modify-split screen-liyongli-20170713
    private int mStartX;
    private int mStartY;
    private int mStartPosition;
    private int mDockSide;
    private final int[] mTempInt2 = new int[2];
    private boolean mMoving;
    private int mTouchSlop;
    private boolean mBackgroundLifted;

    private int mDividerInsets;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mDividerWindowWidth;
    private int mDividerSize;
    private int mTouchElevation;
    private int mLongPressEntraceAnimDuration;

    private final Rect mDockedRect = new Rect();
    private final Rect mDockedTaskRect = new Rect();
    private final Rect mOtherTaskRect = new Rect();
    private final Rect mOtherRect = new Rect();
    private final Rect mDockedInsetRect = new Rect();
    private final Rect mOtherInsetRect = new Rect();
    private final Rect mLastResizeRect = new Rect();
    private final Rect mDisplayRect = new Rect();
    private final WindowManagerProxy mWindowManagerProxy = WindowManagerProxy.getInstance();
    private DividerWindowManager mWindowManager;
    private VelocityTracker mVelocityTracker;
    private FlingAnimationUtils mFlingAnimationUtils;
    private DividerSnapAlgorithm mSnapAlgorithm;
    private final Rect mStableInsets = new Rect();

    private boolean mGrowRecents;
    private ValueAnimator mCurrentAnimator;
    private boolean mEntranceAnimationRunning;
    private boolean mExitAnimationRunning;
    private int mExitStartPosition;
    private GestureDetector mGestureDetector;
    private boolean mDockedStackMinimized;
    private boolean mAdjustedForIme;
    private DividerState mState;
    
    private boolean  prizeDockedFocused = false; // prize-modify-split screen-liyongli-20170715
    private boolean prizeLandscape=false; // prize-modify-split screen-liyongli-20170715
    private FrameLayout.LayoutParams prizeLayoutParams = null; // prize-modify-split screen-liyongli-20170717
    private int mLastX, mLastY;
    private long begin, end;
    private float prizeDpi; // prize-add-split screen-liyongli-20180207
    private int mScreenWidth; // prize-add-split screen-liyongli-20180208
    private int mScreenHeight; // prize-add-split screen-liyongli-20180208
    
    private static final boolean  DEBUG = false;//true;

    /**
     * The offset between vsync-app and vsync-surfaceflinger. See
     * {@link #calculateAppSurfaceFlingerVsyncOffsetMs} why this is necessary.
     */
    private long mSurfaceFlingerOffsetMs;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RESIZE_STACK:
                    resizeStack(msg.arg1, msg.arg2, (SnapTarget) msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private final AccessibilityDelegate mHandleDelegate = new AccessibilityDelegate() {
        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            if (isHorizontalDivision()) {
                info.addAction(new AccessibilityAction(R.id.action_move_tl_full,
                        mContext.getString(R.string.accessibility_action_divider_top_full)));
                if (mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                    info.addAction(new AccessibilityAction(R.id.action_move_tl_70,
                            mContext.getString(R.string.accessibility_action_divider_top_70)));
                }
                info.addAction(new AccessibilityAction(R.id.action_move_tl_50,
                        mContext.getString(R.string.accessibility_action_divider_top_50)));
                if (mSnapAlgorithm.isLastSplitTargetAvailable()) {
                    info.addAction(new AccessibilityAction(R.id.action_move_tl_30,
                            mContext.getString(R.string.accessibility_action_divider_top_30)));
                }
                info.addAction(new AccessibilityAction(R.id.action_move_rb_full,
                        mContext.getString(R.string.accessibility_action_divider_bottom_full)));
            } else {
                info.addAction(new AccessibilityAction(R.id.action_move_tl_full,
                        mContext.getString(R.string.accessibility_action_divider_left_full)));
                if (mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                    info.addAction(new AccessibilityAction(R.id.action_move_tl_70,
                            mContext.getString(R.string.accessibility_action_divider_left_70)));
                }
                info.addAction(new AccessibilityAction(R.id.action_move_tl_50,
                        mContext.getString(R.string.accessibility_action_divider_left_50)));
                if (mSnapAlgorithm.isLastSplitTargetAvailable()) {
                    info.addAction(new AccessibilityAction(R.id.action_move_tl_30,
                            mContext.getString(R.string.accessibility_action_divider_left_30)));
                }
                info.addAction(new AccessibilityAction(R.id.action_move_rb_full,
                        mContext.getString(R.string.accessibility_action_divider_right_full)));
            }
        }

        @Override
        public boolean performAccessibilityAction(View host, int action, Bundle args) {
            int currentPosition = getCurrentPosition();
            SnapTarget nextTarget = null;
            switch (action) {
                case R.id.action_move_tl_full:
                    nextTarget = mSnapAlgorithm.getDismissEndTarget();
                    break;
                case R.id.action_move_tl_70:
                    nextTarget = mSnapAlgorithm.getLastSplitTarget();
                    break;
                case R.id.action_move_tl_50:
                    nextTarget = mSnapAlgorithm.getMiddleTarget();
                    break;
                case R.id.action_move_tl_30:
                    nextTarget = mSnapAlgorithm.getFirstSplitTarget();
                    break;
                case R.id.action_move_rb_full:
                    nextTarget = mSnapAlgorithm.getDismissStartTarget();
                    break;
            }
            if (nextTarget != null) {
                startDragging(true /* animate */, false /* touching */);
                stopDragging(currentPosition, nextTarget, 250, Interpolators.FAST_OUT_SLOW_IN);
                return true;
            }
            return super.performAccessibilityAction(host, action, args);
        }
    };

    private final Runnable mResetBackgroundRunnable = new Runnable() {
        @Override
        public void run() {
            resetBackground();
        }
    };

    public DividerView(Context context) {
        super(context);
    }

    public DividerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DividerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DividerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHandle = (DividerHandleView) findViewById(R.id.docked_divider_handle);
        mBackground = findViewById(R.id.docked_divider_background);
        mMinimizedShadow = (MinimizedDockShadow) findViewById(R.id.minimized_dock_shadow);
        mHandle.setOnTouchListener(this);
        mDividerWindowWidth = getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.docked_stack_divider_thickness);
                
        /* prize-add-split screen-liyongli-20170826-start */
        if(PrizeOption.PRIZE_SPLIT_SCREEN_BG_HINT_FOCUS) {
            prizeDpi =  getResources().getDisplayMetrics().density;
            mDividerInsets = getResources().getDimensionPixelSize(
                    com.android.internal.R.dimen.prize_docked_stack_divider_insets);
        }else/* prize-add-split screen-liyongli-20170826-end */
        {
        mDividerInsets = getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.docked_stack_divider_insets);
        }
        mDividerSize = mDividerWindowWidth - 2 * mDividerInsets;
        mTouchElevation = getResources().getDimensionPixelSize(
                R.dimen.docked_stack_divider_lift_elevation);
        mLongPressEntraceAnimDuration = getResources().getInteger(
                R.integer.long_press_dock_anim_duration);
        if(DEBUG){//enter splite screen, anim time, MS
            mLongPressEntraceAnimDuration = 5000;//lyl
            Log.d("DividerView", "lyl -- debug -- set anim time 5s");
        }
        mGrowRecents = getResources().getBoolean(R.bool.recents_grow_in_multiwindow);
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mFlingAnimationUtils = new FlingAnimationUtils(getContext(), 0.3f);
        updateDisplayInfo();
        boolean landscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        mHandle.setPointerIcon(PointerIcon.getSystemIcon(getContext(),
                landscape ? TYPE_HORIZONTAL_DOUBLE_ARROW : TYPE_VERTICAL_DOUBLE_ARROW));
        getViewTreeObserver().addOnComputeInternalInsetsListener(this);
        mHandle.setAccessibilityDelegate(mHandleDelegate);
        mGestureDetector = new GestureDetector(mContext, new SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (SWAPPING_ENABLED) {
                    SystemServicesProxy ssp = Recents.getSystemServices();
                    
                    /* prize-modify-split screen-liyongli-20170713-start */
                    if(PrizeOption.PRIZE_SPLIT_SCREEN_MENU) {
                        if( ssp.isRecentsActivityVisible() ){
                            mHandle.setTouching(false, true );
                            return true;
                        }
// -x                        mWindowManagerProxy.resizeDockedStack(mDockedRect, mDockedRect, null, null, null);
                        prizeInitDockedRect();
                        //Log.d("DividerView", "lyl onSingleTapUp mDockedRect="+mDockedRect + ", mDockedTaskRect="+mDockedTaskRect);
                        //Log.d("DividerView", "lyl onSingleTapUp mOtherRect="+mOtherRect + ", mOtherTaskRect="+mOtherTaskRect);
                        releaseBackground();

                        mMenuManager.ToggleShowMenuView( mDockedRect,
                                   prizeLandscape, mHandle.prizeDockedStackIsFocus() );  //show , hiden
                        mHandle.setTouching(false, true );
                        return true;
                    }
                    /* prize-modify-split screen-liyongli-20170713-start */
                    
                    updateDockSide();
                    //SystemServicesProxy ssp = Recents.getSystemServices();
                    if (mDockSide != WindowManager.DOCKED_INVALID
                            && !ssp.isRecentsActivityVisible()) {
                        if(PrizeOption.PRIZE_SPLIT_SCREEN_BG_HINT_FOCUS) {
                            prizeDockedFocused = !prizeDockedFocused;
                            prizeChangeBackgroud(prizeDockedFocused);
                        }
                        mHandle.prizeExchangePositionChangeFocus();
                        mWindowManagerProxy.swapTasks();
                        return true;
                    }
                }
                return false;
            }
        });
        
        /* prize-modify-split screen-liyongli-20170713-start */
        if(PrizeOption.PRIZE_SPLIT_SCREEN_MENU) {
          mMenuManager = new PrizeDividerMenu();
          mMenuManager
                .setOnBtnClickListener(new PrizeDividerMenu.OnBtnClickListener() {
                    @Override
                    public void onBtnClick(int btnId) {
                        if(btnId == mMenuManager.BTN_CLOSE){
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(prizeDockedFocused){
                                        //mWindowManagerProxy.dismissDockedStack(); 
                                        //mWindowManagerProxy.maximizeDockedStack(); 
                                        //1. close DockedStack 
                                        prizeAnimCloseDockSide();
                                    }else{
                                        //2. close FullScreenStack use bellow
                                        prizeCloseFullScreenSide();
                                    }
                                }
                            }, 100);
                            
                        }else if(btnId == mMenuManager.BTN_EXCHANGE){
                            updateDockSide();
                                SystemServicesProxy ssp = Recents.getSystemServices();
                                if (mDockSide != WindowManager.DOCKED_INVALID
                                        && !ssp.isRecentsActivityVisible()) {
                                    if(PrizeOption.PRIZE_SPLIT_SCREEN_BG_HINT_FOCUS) {
                                        //prizeDockedFocused = !prizeDockedFocused;
                                        prizeDockedFocused = false;
                                        prizeChangeBackgroud(prizeDockedFocused);
                                        mHandle.prizeChangeDockedStackFocus(false);
                                    }
                                    //mHandle.prizeExchangePositionChangeFocus();
                                    
                                    mWindowManagerProxy.setResizing(true);
                                    mWindowManagerProxy.swapTasks();
                                    mWindowManagerProxy.setResizing(false); //add for fix bug, touch not change the focus
                                    }
                        }

                    }
                });
          mMenuManager.CreateMenu(landscape, mContext);
        }
        prizeLandscape = landscape;
        /* prize-modify-split screen-liyongli-20170713-end */
        /* prize-modify-split screen-liyongli-20170715-start */
        if(PrizeOption.PRIZE_SPLIT_SCREEN_BG_HINT_FOCUS) {
            prizeDockedFocused = false;
            prizeLayoutParams = (LayoutParams) mBackground.getLayoutParams();
            prizeChangeBackgroud(prizeDockedFocused);
        }
        /* prize-modify-split screen-liyongli-20170715-end */
        
        /* prize-modify-split screen-liyongli-20180208-start */
        if(PrizeOption.PRIZE_SPLIT_SCREEN_DRAG) {
            DisplayMetrics dm = getResources().getDisplayMetrics(); 
            mScreenWidth = dm.widthPixels;
            mScreenHeight = dm.heightPixels;
        }
        /* prize-modify-split screen-liyongli-20180208-end */
    }

    private void prizeCloseFullScreenSide() {
        int dockSide = mWindowManagerProxy.getDockSide();
        if (dockSide != WindowManager.DOCKED_INVALID && !mDockedStackMinimized) {
            startDragging(false /* animate */, false /* touching */);
            SnapTarget target = dockSideTopLeft(dockSide)
                  ? mSnapAlgorithm.getDismissEndTarget()
                  : mSnapAlgorithm.getDismissStartTarget();
                  
            // Don't start immediately - give a little bit time to settle the drag resize change.
            mExitAnimationRunning = true;
            mExitStartPosition = getCurrentPosition();
            stopDragging(mExitStartPosition, target, 336 /* duration */, 100 /* startDelay */,
                  0 /* endDelay */, Interpolators.FAST_OUT_SLOW_IN);
        }
    }
    
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
        /* prize-modify-split screen-liyongli-20170713-start */
        if(PrizeOption.PRIZE_SPLIT_SCREEN_MENU && mMenuManager!=null) {
            mMenuManager.AddMenuView();
        }
        /* prize-modify-split screen-liyongli-20170713-end */
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        /* prize-modify-split screen-liyongli-20170713-start */
        if(PrizeOption.PRIZE_SPLIT_SCREEN_MENU && mMenuManager!=null) {
            mMenuManager.RemoveMenuView();
        }
        /* prize-modify-split screen-liyongli-20170713-end */
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (mStableInsets.left != insets.getStableInsetLeft()
                || mStableInsets.top != insets.getStableInsetTop()
                || mStableInsets.right != insets.getStableInsetRight()
                || mStableInsets.bottom != insets.getStableInsetBottom()) {
            mStableInsets.set(insets.getStableInsetLeft(), insets.getStableInsetTop(),
                    insets.getStableInsetRight(), insets.getStableInsetBottom());
            if (mSnapAlgorithm != null) {
                mSnapAlgorithm = null;
                initializeSnapAlgorithm();
            }
        }
        return super.onApplyWindowInsets(insets);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int minimizeLeft = 0;
        int minimizeTop = 0;
        if (mDockSide == WindowManager.DOCKED_TOP) {
            minimizeTop = mBackground.getTop();
        } else if (mDockSide == WindowManager.DOCKED_LEFT) {
            minimizeLeft = mBackground.getLeft();
        } else if (mDockSide == WindowManager.DOCKED_RIGHT) {
            minimizeLeft = mBackground.getRight() - mMinimizedShadow.getWidth();
        }
        mMinimizedShadow.layout(minimizeLeft, minimizeTop,
                minimizeLeft + mMinimizedShadow.getMeasuredWidth(),
                minimizeTop + mMinimizedShadow.getMeasuredHeight());
        if (changed) {
            mWindowManagerProxy.setTouchRegion(new Rect(mHandle.getLeft(), mHandle.getTop(),
                    mHandle.getRight(), mHandle.getBottom()));
        }
    }

    public void injectDependencies(DividerWindowManager windowManager, DividerState dividerState) {
        mWindowManager = windowManager;
        mState = dividerState;
    }

    public WindowManagerProxy getWindowManagerProxy() {
        return mWindowManagerProxy;
    }

    public boolean startDragging(boolean animate, boolean touching) {
        cancelFlingAnimation();
        if (touching) {
            mHandle.setTouching(true, animate);
        }
        mDockSide = mWindowManagerProxy.getDockSide();
        initializeSnapAlgorithm();
        mWindowManagerProxy.setResizing(true);
        if (touching) {
            mWindowManager.setSlippery(false);
            liftBackground();
        }
        EventBus.getDefault().send(new StartedDragingEvent());
        return mDockSide != WindowManager.DOCKED_INVALID;
    }

    public void stopDragging(int position, float velocity, boolean avoidDismissStart,
            boolean logMetrics) {
        mHandle.setTouching(false, true /* animate */);
        fling(position, velocity, avoidDismissStart, logMetrics);
        mWindowManager.setSlippery(true);
        releaseBackground();
    }

    public void stopDragging(int position, SnapTarget target, long duration,
            Interpolator interpolator) {
        stopDragging(position, target, duration, 0 /* startDelay*/, 0 /* endDelay */, interpolator);
    }

    public void stopDragging(int position, SnapTarget target, long duration,
            Interpolator interpolator, long endDelay) {
        stopDragging(position, target, duration, 0 /* startDelay*/, endDelay, interpolator);
    }

    public void stopDragging(int position, SnapTarget target, long duration, long startDelay,
            long endDelay, Interpolator interpolator) {
        mHandle.setTouching(false, true /* animate */);
        flingTo(position, target, duration, startDelay, endDelay, interpolator);
        mWindowManager.setSlippery(true);
        releaseBackground();
    }

    private void stopDragging() {
        mHandle.setTouching(false, true /* animate */);
        mWindowManager.setSlippery(true);
        releaseBackground();
    }

    private void updateDockSide() {
        mDockSide = mWindowManagerProxy.getDockSide();
        mMinimizedShadow.setDockSide(mDockSide);
    }
    
    
    private void prizeInitDockedRect() {
        if (mDockedRect.bottom==mDisplayHeight||mDockedRect.right==mDisplayHeight
            || mDockedTaskRect.bottom==mDisplayHeight
            ||mDockedRect.bottom==0||mDockedRect.right==0
            ||mDockSide < WindowManager.DOCKED_LEFT
            ||mDockSide > WindowManager.DOCKED_BOTTOM
            ) {
            mDockSide = mWindowManagerProxy.getDockSide();
            calculateBoundsForPosition( mSnapAlgorithm.getMiddleTarget().taskPosition,
                mDockSide, mDockedRect);
        }
    }
    
    private void initializeSnapAlgorithm() {
        if (mSnapAlgorithm == null) {
            mSnapAlgorithm = new DividerSnapAlgorithm(getContext().getResources(), mDisplayWidth,
                    mDisplayHeight, mDividerSize, isHorizontalDivision(), mStableInsets);
        }
    }

    public DividerSnapAlgorithm getSnapAlgorithm() {
        initializeSnapAlgorithm();
        return mSnapAlgorithm;
    }

    public int getCurrentPosition() {
        getLocationOnScreen(mTempInt2);
        if (isHorizontalDivision()) {
            return mTempInt2[1] + mDividerInsets;
        } else {
            return mTempInt2[0] + mDividerInsets;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        convertToScreenCoordinates(event);
        mGestureDetector.onTouchEvent(event);
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                begin = System.currentTimeMillis();
                
                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(event);
                mStartX = (int) event.getX();
                mStartY = (int) event.getY();
                boolean result = startDragging(true /* animate */, true /* touching */);
                if (!result) {

                    // Weren't able to start dragging successfully, so cancel it again.
                    stopDragging();
                }
                mStartPosition = getCurrentPosition();
                mMoving = false;
                return result;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                int x = (int) event.getX();
                int y = (int) event.getY();
                boolean exceededTouchSlop =
                        isHorizontalDivision() && Math.abs(y - mStartY) > mTouchSlop
                                || (!isHorizontalDivision() && Math.abs(x - mStartX) > mTouchSlop);
                boolean doDrag = false; //liyongli add
                if(mMoving){
                    doDrag = true;
                   //     doDrag = isHorizontalDivision() && Math.abs(y - mLastY) > mTouchSlop
                   //             || (!isHorizontalDivision() && Math.abs(x - mLastX) > mTouchSlop);
                }
                                
                if (!mMoving && exceededTouchSlop) {
                    mStartX = x;
                    mStartY = y;
                    mMoving = true;
                    mLastX = x; //liyongli add
                    mLastY = y;
                }
                if(doDrag&&mMoving){//liyongli add
                    mLastX = x;
                    mLastY = y;
                }
                end = System.currentTimeMillis();//liyongli add
                if( end-begin < 100){ //after 100ms  do dragResizeStack
                    break;
                }
                
                if (mMoving && mDockSide != WindowManager.DOCKED_INVALID && doDrag) {
                    SnapTarget snapTarget = mSnapAlgorithm.calculateSnapTarget(
                            mStartPosition, 0 /* velocity */, false /* hardDismiss */);
                    resizeStackDelayed(calculatePosition(x, y), mStartPosition, snapTarget);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.addMovement(event);

                x = (int) event.getRawX();
                y = (int) event.getRawY();

                mVelocityTracker.computeCurrentVelocity(1000);
                int position = calculatePosition(x, y);
                
                
                /* prize-modify-split screen-liyongli-20170608-start */
                if(PrizeOption.PRIZE_SPLIT_SCREEN_DRAG && isHorizontalDivision() ) {
                    if( prizeMoveEdgeExitSplite(x, y, position)==0 ){
                        break;
                    }
                    
                    if (mMoving && mDockSide != WindowManager.DOCKED_INVALID) {                    
                        mWindowManagerProxy.resizeDockedStack(mDockedRect, null, null, null, null);
                        stopDragging();
                        mWindowManagerProxy.setResizing(false);
                        mDockSide = WindowManager.DOCKED_INVALID;
                        //prizeMoveBorderView();  
                        if(mStartPosition!=position){
                            notifyDragChangedListener(mHandle.prizeGetFocusStack(), mDockedRect , mOtherRect ); 
                        }
                    }else{
                        mHandle.setTouching(false, true ); //O big blue to small white
                        //mWindowManagerProxy.setResizing(false);//if here set false, cause exchange window show BLACK screen
                                                                                                                     //put PrizeDividerMenu.java set
                    }
                }else{
                stopDragging(position, isHorizontalDivision() ? mVelocityTracker.getYVelocity()
                        : mVelocityTracker.getXVelocity(), false /* avoidDismissStart */,
                        true /* log */);
                }
                /* prize-modify-split screen-liyongli-20170608-end */
                mMoving = false;
                break;
        }
        return true;
    }

    /* prize-add-split screen-liyongli-20170726-start */
    private int prizeMoveEdgeExitSplite(int x, int y, int position) {
        int ret = -1;
        float v = 10;
        float vClose = 650*prizeDpi; //vClose
        int upClosePos = mScreenHeight/6;  //150
        int dnClosePos = mScreenHeight - upClosePos;  //1050
        
//Log.d("DividerView", "lyl --  "+x+", "+y + "; "+upClosePos+", "+dnClosePos);            
        if( isHorizontalDivision() ){
            if( y<upClosePos ){
                v = -vClose;//-1300;
                ret =0;
            }else if( y>dnClosePos ){
                v = vClose;
                ret =0;
            }
        }else{
            if( x<upClosePos ){
                v = -vClose;
                ret =0;
            }else if( x>dnClosePos ){
                v = vClose;
                ret =0;
            }
        }
        
        if( ret==0 ){
            stopDragging(position, v, false /* avoidDismissStart */,true /* log */);
        }
        return ret;
    }
    /* prize-add-split screen-liyongli-20170726-end */
    
    /* prize-add-split screen-liyongli-20170725-start */
    private void prizeAnimCloseDockSide() {
        int position = (int)(250*prizeDpi);
        float velocity = 700*prizeDpi;
        startDragging(true,true);
        //fling(500, -1400, false, true); 
        fling(position, -velocity, false, true); 
    }
    /* prize-add-split screen-liyongli-20170725-end */


    private void logResizeEvent(SnapTarget snapTarget) {
        if (snapTarget == mSnapAlgorithm.getDismissStartTarget()) {
            MetricsLogger.action(
                    mContext, MetricsEvent.ACTION_WINDOW_UNDOCK_MAX, dockSideTopLeft(mDockSide)
                            ? LOG_VALUE_UNDOCK_MAX_OTHER
                            : LOG_VALUE_UNDOCK_MAX_DOCKED);
        } else if (snapTarget == mSnapAlgorithm.getDismissEndTarget()) {
            MetricsLogger.action(
                    mContext, MetricsEvent.ACTION_WINDOW_UNDOCK_MAX, dockSideBottomRight(mDockSide)
                            ? LOG_VALUE_UNDOCK_MAX_OTHER
                            : LOG_VALUE_UNDOCK_MAX_DOCKED);
        } else if (snapTarget == mSnapAlgorithm.getMiddleTarget()) {
            MetricsLogger.action(mContext, MetricsEvent.ACTION_WINDOW_DOCK_RESIZE,
                    LOG_VALUE_RESIZE_50_50);
        } else if (snapTarget == mSnapAlgorithm.getFirstSplitTarget()) {
            MetricsLogger.action(mContext, MetricsEvent.ACTION_WINDOW_DOCK_RESIZE,
                    dockSideTopLeft(mDockSide)
                            ? LOG_VALUE_RESIZE_DOCKED_SMALLER
                            : LOG_VALUE_RESIZE_DOCKED_LARGER);
        } else if (snapTarget == mSnapAlgorithm.getLastSplitTarget()) {
            MetricsLogger.action(mContext, MetricsEvent.ACTION_WINDOW_DOCK_RESIZE,
                    dockSideTopLeft(mDockSide)
                            ? LOG_VALUE_RESIZE_DOCKED_LARGER
                            : LOG_VALUE_RESIZE_DOCKED_SMALLER);
        }
    }

    private void convertToScreenCoordinates(MotionEvent event) {
        event.setLocation(event.getRawX(), event.getRawY());
    }

    private void fling(int position, float velocity, boolean avoidDismissStart,
            boolean logMetrics) {
        SnapTarget snapTarget = mSnapAlgorithm.calculateSnapTarget(position, velocity);
        if (avoidDismissStart && snapTarget == mSnapAlgorithm.getDismissStartTarget()) {
            snapTarget = mSnapAlgorithm.getFirstSplitTarget();
        }
        if (logMetrics) {
            logResizeEvent(snapTarget);
        }
        ValueAnimator anim = getFlingAnimator(position, snapTarget, 0 /* endDelay */);
        mFlingAnimationUtils.apply(anim, position, snapTarget.position, velocity);
        anim.start();
    }

    private void flingTo(int position, SnapTarget target, long duration, long startDelay,
            long endDelay, Interpolator interpolator) {
        ValueAnimator anim = getFlingAnimator(position, target, endDelay);
        anim.setDuration(duration);
        anim.setStartDelay(startDelay);
        anim.setInterpolator(interpolator);
        anim.start();
    }

    private ValueAnimator getFlingAnimator(int position, final SnapTarget snapTarget,
            final long endDelay) {
        final boolean taskPositionSameAtEnd = snapTarget.flag == SnapTarget.FLAG_NONE;
        ValueAnimator anim = ValueAnimator.ofInt(position, snapTarget.position);
        if(DEBUG){//enter splite screen, set the  splite positon
         if(position==1280&&snapTarget.position==620){
             Log.d("DividerView", "lyl -- debug -- set divider position 800");
             anim = ValueAnimator.ofInt(position, 800);
         }
        }
        anim.addUpdateListener(animation -> resizeStackDelayed((int) animation.getAnimatedValue(),
                taskPositionSameAtEnd && animation.getAnimatedFraction() == 1f
                        ? TASK_POSITION_SAME
                        : snapTarget.taskPosition,
                snapTarget));
        Runnable endAction = () -> {
            commitSnapFlags(snapTarget);
            mWindowManagerProxy.setResizing(false);
            mDockSide = WindowManager.DOCKED_INVALID;
            mCurrentAnimator = null;
            mEntranceAnimationRunning = false;
            mExitAnimationRunning = false;
            EventBus.getDefault().send(new StoppedDragingEvent());
        };
        anim.addListener(new AnimatorListenerAdapter() {

            private boolean mCancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                mHandler.removeMessages(MSG_RESIZE_STACK);
                mCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                long delay = 0;
                if (endDelay != 0) {
                    delay = endDelay;
                } else if (mCancelled) {
                    delay = 0;
                } else if (mSurfaceFlingerOffsetMs != 0) {
                    delay = mSurfaceFlingerOffsetMs;
                }
                if (delay == 0) {
                    endAction.run();
                } else {
                    mHandler.postDelayed(endAction, delay);
                }
            }
        });
        mCurrentAnimator = anim;
        return anim;
    }

    private void cancelFlingAnimation() {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }
    }

    private void commitSnapFlags(SnapTarget target) {
        if (target.flag == SnapTarget.FLAG_NONE) {
            return;
        }
        boolean dismissOrMaximize;
        if (target.flag == SnapTarget.FLAG_DISMISS_START) {
            dismissOrMaximize = mDockSide == WindowManager.DOCKED_LEFT
                    || mDockSide == WindowManager.DOCKED_TOP;
        } else {
            dismissOrMaximize = mDockSide == WindowManager.DOCKED_RIGHT
                    || mDockSide == WindowManager.DOCKED_BOTTOM;
        }
        if (dismissOrMaximize) {
            mWindowManagerProxy.dismissDockedStack();
        } else {
            mWindowManagerProxy.maximizeDockedStack();
        }
        mWindowManagerProxy.setResizeDimLayer(false, -1, 0f);
    }

    private void liftBackground() {
        if (mBackgroundLifted) {
            return;
        }
        if (isHorizontalDivision()) {
            mBackground.animate().scaleY(1.4f);
        } else {
            mBackground.animate().scaleX(1.4f);
        }
        mBackground.animate()
                .setInterpolator(Interpolators.TOUCH_RESPONSE)
                .setDuration(TOUCH_ANIMATION_DURATION)
                .translationZ(mTouchElevation)
                .start();

        // Lift handle as well so it doesn't get behind the background, even though it doesn't
        // cast shadow.
        mHandle.animate()
                .setInterpolator(Interpolators.TOUCH_RESPONSE)
                .setDuration(TOUCH_ANIMATION_DURATION)
                .translationZ(mTouchElevation)
                .start();
        mBackgroundLifted = true;
    }

    private void releaseBackground() {
        if (!mBackgroundLifted) {
            return;
        }
        mBackground.animate()
                .setInterpolator(Interpolators.FAST_OUT_SLOW_IN)
                .setDuration(TOUCH_RELEASE_ANIMATION_DURATION)
                .translationZ(0)
                .scaleX(1f)
                .scaleY(1f)
                .start();
        mHandle.animate()
                .setInterpolator(Interpolators.FAST_OUT_SLOW_IN)
                .setDuration(TOUCH_RELEASE_ANIMATION_DURATION)
                .translationZ(0)
                .start();
        mBackgroundLifted = false;
    }


    public void setMinimizedDockStack(boolean minimized) {
        updateDockSide();
        mHandle.setAlpha(minimized ? 0f : 1f);
        if (!minimized) {
            resetBackground();
        } else if (mDockSide == WindowManager.DOCKED_TOP) {
            mBackground.setPivotY(0);
            mBackground.setScaleY(MINIMIZE_DOCK_SCALE);
        } else if (mDockSide == WindowManager.DOCKED_LEFT
                || mDockSide == WindowManager.DOCKED_RIGHT) {
            mBackground.setPivotX(mDockSide == WindowManager.DOCKED_LEFT
                    ? 0
                    : mBackground.getWidth());
            mBackground.setScaleX(MINIMIZE_DOCK_SCALE);
        }
        mMinimizedShadow.setAlpha(minimized ? 1f : 0f);
        mDockedStackMinimized = minimized;
    }

    public void setMinimizedDockStack(boolean minimized, long animDuration) {
        updateDockSide();
        mHandle.animate()
                .setInterpolator(Interpolators.FAST_OUT_SLOW_IN)
                .setDuration(animDuration)
                .alpha(minimized ? 0f : 1f)
                .start();
        if (mDockSide == WindowManager.DOCKED_TOP) {
            mBackground.setPivotY(0);
            mBackground.animate()
                    .scaleY(minimized ? MINIMIZE_DOCK_SCALE : 1f);
        } else if (mDockSide == WindowManager.DOCKED_LEFT
                || mDockSide == WindowManager.DOCKED_RIGHT) {
            mBackground.setPivotX(mDockSide == WindowManager.DOCKED_LEFT
                    ? 0
                    : mBackground.getWidth());
            mBackground.animate()
                    .scaleX(minimized ? MINIMIZE_DOCK_SCALE : 1f);
        }
        if (!minimized) {
            mBackground.animate().withEndAction(mResetBackgroundRunnable);
        }
        mMinimizedShadow.animate()
                .alpha(minimized ? 1f : 0f)
                .setInterpolator(Interpolators.ALPHA_IN)
                .setDuration(animDuration)
                .start();
        mBackground.animate()
                .setInterpolator(Interpolators.FAST_OUT_SLOW_IN)
                .setDuration(animDuration)
                .start();
        mDockedStackMinimized = minimized;
    }

    public void setAdjustedForIme(boolean adjustedForIme) {
        updateDockSide();
        mHandle.setAlpha(adjustedForIme ? 0f : 1f);
        if (!adjustedForIme) {
            resetBackground();
        } else if (mDockSide == WindowManager.DOCKED_TOP) {
            mBackground.setPivotY(0);
            mBackground.setScaleY(ADJUSTED_FOR_IME_SCALE);
        }
        mAdjustedForIme = adjustedForIme;
    }

    public void setAdjustedForIme(boolean adjustedForIme, long animDuration) {

        updateDockSide();
        mHandle.animate()
                .setInterpolator(IME_ADJUST_INTERPOLATOR)
                .setDuration(animDuration)
                .alpha(adjustedForIme ? 0f : 1f)
                .start();
        if(PrizeOption.PRIZE_SPLIT_SCREEN_BG_HINT_FOCUS) {         
            resetBackground();
            //updateDockSide();
            mAdjustedForIme = adjustedForIme;
            return;
        }
                        
        if (mDockSide == WindowManager.DOCKED_TOP) {
            mBackground.setPivotY(0);
            mBackground.animate()
                    .scaleY(adjustedForIme ? ADJUSTED_FOR_IME_SCALE : 1f);
        }
        if (!adjustedForIme) {
            mBackground.animate().withEndAction(mResetBackgroundRunnable);
        }
        mBackground.animate()
                .setInterpolator(IME_ADJUST_INTERPOLATOR)
                .setDuration(animDuration)
                .start();
        mAdjustedForIme = adjustedForIme;
    }

    private void resetBackground() {
        mBackground.setPivotX(mBackground.getWidth() / 2);
        mBackground.setPivotY(mBackground.getHeight() / 2);
        mBackground.setScaleX(1f);
        mBackground.setScaleY(1f);
        mMinimizedShadow.setAlpha(0f);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateDisplayInfo();
        mWindowManagerProxy.setResizing(false); // liyongli  add for fix bug,  after turn screen, touch not change the focus
    }


    public void notifyDockSideChanged(int newDockSide) {
        mDockSide = newDockSide;
        mMinimizedShadow.setDockSide(mDockSide);
        requestLayout();
    }

    /* prize-add-split screen-liyongli-20170715-start */
    public void prizeChangeBackgroud(boolean dockedFocus) {
        if(!PrizeOption.PRIZE_SPLIT_SCREEN_BG_HINT_FOCUS) {
            return;
        }
 //frameworks/base/packages/PrizeSystemUI/res/values/styles.xml       
//<style name="PrizeDockedDividerBackgroundFocus">
//<item name="android:layout_height">10dp</item>
        int focusBgHi = 10*(int)prizeDpi; //10dp
        
        int divHi = (mDividerWindowWidth - mDividerInsets*2)/2;
        int upGap = mDividerWindowWidth/2 - focusBgHi -divHi; //25; //pix
        int dnGap = mDividerWindowWidth/2 +divHi;//51;
        
        if(dockedFocus){
            if(prizeLandscape){
                mBackground.setBackgroundResource(R.drawable.divider_focus_left);
                prizeLayoutParams.topMargin=0;
                prizeLayoutParams.leftMargin=upGap;
            }else{
                mBackground.setBackgroundResource(R.drawable.divider_focus_up);
                prizeLayoutParams.topMargin=upGap;//12*2;//12dp
                prizeLayoutParams.leftMargin=0;
            }
        }else{
            if(prizeLandscape){
                mBackground.setBackgroundResource(R.drawable.divider_focus_right);
                prizeLayoutParams.topMargin=0;
                prizeLayoutParams.leftMargin=dnGap;
            }else{
                mBackground.setBackgroundResource(R.drawable.divider_focus_down);
                prizeLayoutParams.topMargin=dnGap;//26*2;//26dp
                prizeLayoutParams.leftMargin=0;
            }
        }
        mBackground.setLayoutParams(prizeLayoutParams);
    }
    /* prize-add-split screen-liyongli-20170715-end */
    
    /* prize-add-split screen-liyongli-20170708-start */
    public void prizeNotifyDockFocusChanged(int focusedStackId, int lastFocusedStackId) {
        if(!PrizeOption.PRIZE_SPLIT_SCREEN_FOCUS){
            return;
        }
        
        if( focusedStackId==DOCKED_STACK_ID ){
            prizeDockedFocused = true;
        }else{
            prizeDockedFocused = false;
        }
        if(PrizeOption.PRIZE_SPLIT_SCREEN_BG_HINT_FOCUS) {
            prizeChangeBackgroud(prizeDockedFocused);
        }
        mHandle.prizeSetDockedStackFocus(prizeDockedFocused);
        
        //mHandle.invalidate();
        //requestLayout();
        notifyDragChangedListener(focusedStackId, mDockedRect , mOtherRect ); 
    }
    /* prize-add-split screen-liyongli-20170708-end */
    
    /* prize-add-split screen-liyongli-20170726-start */
    public void prizeOnConfigurationChangedSetDockFocus(boolean focus) {
        if(!PrizeOption.PRIZE_SPLIT_SCREEN_FOCUS){
            return;
        }
        
        prizeDockedFocused = focus;
        
        if(PrizeOption.PRIZE_SPLIT_SCREEN_BG_HINT_FOCUS) {
            prizeChangeBackgroud(prizeDockedFocused);
        }
        mHandle.prizeSetDockedStackFocus(prizeDockedFocused);
        
        //mHandle.invalidate();
        //requestLayout();
        //notifyDragChangedListener(focusedStackId, mDockedRect , mOtherRect ); 
    }
    /* prize-add-split screen-liyongli-20170726-end */
    
    /* prize-add-split screen, return btn exit split screen-liyongli-20170724-start */
    public void prizeRequestExitSplitScreen(int btnType, int focusedStackId) {
        if(!PrizeOption.PRIZE_SPLIT_SCREEN_RETURN){
            return;
        }
        
        //1. set setResizing
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //startDragging(true /* animate */, true /* touching */);
                mWindowManagerProxy.setResizing(true);  //if no this call, return will show Black Screen
                                                                                                          //copy from  startDragging( true, true )
            }
        }, 100);
        
        //2.0
        if(focusedStackId==DOCKED_STACK_ID){
            prizeDockedFocused = true;
        }else{
            prizeDockedFocused = false;
        }
        
        //2. exit animation
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if( prizeDockedFocused ){
                    prizeAnimCloseDockSide(); //use bellow replace
                    //startDragging(true,true);
                    //flingClose(500, -1400, false, true); //
                }else{
                    prizeCloseFullScreenSide();
                }
            }
        }, 300);
    }
    /* prize-add-split screen, return btn exit split screen-liyongli-20170724-end */
    

    private void updateDisplayInfo() {
        final DisplayManager displayManager =
                (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        Display display = displayManager.getDisplay(Display.DEFAULT_DISPLAY);
        final DisplayInfo info = new DisplayInfo();
        display.getDisplayInfo(info);
        mDisplayWidth = info.logicalWidth;
        mDisplayHeight = info.logicalHeight;
        mSnapAlgorithm = null;
        initializeSnapAlgorithm();
    }

    private int calculatePosition(int touchX, int touchY) {
        return isHorizontalDivision() ? calculateYPosition(touchY) : calculateXPosition(touchX);
    }

    public boolean isHorizontalDivision() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private int calculateXPosition(int touchX) {
        return mStartPosition + touchX - mStartX;
    }

    private int calculateYPosition(int touchY) {
        return mStartPosition + touchY - mStartY;
    }

    private void alignTopLeft(Rect containingRect, Rect rect) {
        int width = rect.width();
        int height = rect.height();
        rect.set(containingRect.left, containingRect.top,
                containingRect.left + width, containingRect.top + height);
    }

    private void alignBottomRight(Rect containingRect, Rect rect) {
        int width = rect.width();
        int height = rect.height();
        rect.set(containingRect.right - width, containingRect.bottom - height,
                containingRect.right, containingRect.bottom);
    }

    public void calculateBoundsForPosition(int position, int dockSide, Rect outRect) {
        DockedDividerUtils.calculateBoundsForPosition(position, dockSide, outRect, mDisplayWidth,
                mDisplayHeight, mDividerSize);
    }

    public void resizeStackDelayed(int position, int taskPosition, SnapTarget taskSnapTarget) {
        if (mSurfaceFlingerOffsetMs != 0) {
            Message message = mHandler.obtainMessage(MSG_RESIZE_STACK, position, taskPosition,
                    taskSnapTarget);
            message.setAsynchronous(true);
            mHandler.sendMessageDelayed(message, mSurfaceFlingerOffsetMs);
        } else {
            resizeStack(position, taskPosition, taskSnapTarget);
        }
    }

    public void resizeStack(int position, int taskPosition, SnapTarget taskSnapTarget) {
        calculateBoundsForPosition(position, mDockSide, mDockedRect);

        if (mDockedRect.equals(mLastResizeRect) && !mEntranceAnimationRunning) {
            return;
        }

        // Make sure shadows are updated
        if (mBackground.getZ() > 0f) {
            mBackground.invalidate();
        }

        mLastResizeRect.set(mDockedRect);
        if (mEntranceAnimationRunning && taskPosition != TASK_POSITION_SAME) {
            if (mCurrentAnimator != null) {
                calculateBoundsForPosition(taskPosition, mDockSide, mDockedTaskRect);
            } else {
                calculateBoundsForPosition(isHorizontalDivision() ? mDisplayHeight : mDisplayWidth,
                        mDockSide, mDockedTaskRect);
            }
            calculateBoundsForPosition(taskPosition, DockedDividerUtils.invertDockSide(mDockSide),
                    mOtherTaskRect);
            mWindowManagerProxy.resizeDockedStack(mDockedRect, mDockedTaskRect, null,
                    mOtherTaskRect, null);
        } else if (mExitAnimationRunning && taskPosition != TASK_POSITION_SAME) {
            calculateBoundsForPosition(taskPosition,
                    mDockSide, mDockedTaskRect);
            calculateBoundsForPosition(mExitStartPosition,
                    DockedDividerUtils.invertDockSide(mDockSide), mOtherTaskRect);
            mOtherInsetRect.set(mOtherTaskRect);
            applyExitAnimationParallax(mOtherTaskRect, position);
            mWindowManagerProxy.resizeDockedStack(mDockedRect, mDockedTaskRect, null,
                    mOtherTaskRect, mOtherInsetRect);
        } else if (taskPosition != TASK_POSITION_SAME) {
            calculateBoundsForPosition(position, DockedDividerUtils.invertDockSide(mDockSide),
                    mOtherRect);
            int dockSideInverted = DockedDividerUtils.invertDockSide(mDockSide);
            int taskPositionDocked =
                    restrictDismissingTaskPosition(taskPosition, mDockSide, taskSnapTarget);
            int taskPositionOther =
                    restrictDismissingTaskPosition(taskPosition, dockSideInverted, taskSnapTarget);
            calculateBoundsForPosition(taskPositionDocked, mDockSide, mDockedTaskRect);
            calculateBoundsForPosition(taskPositionOther, dockSideInverted, mOtherTaskRect);
            mDisplayRect.set(0, 0, mDisplayWidth, mDisplayHeight);
            alignTopLeft(mDockedRect, mDockedTaskRect);
            alignTopLeft(mOtherRect, mOtherTaskRect);
            mDockedInsetRect.set(mDockedTaskRect);
            mOtherInsetRect.set(mOtherTaskRect);
            if (dockSideTopLeft(mDockSide)) {
                alignTopLeft(mDisplayRect, mDockedInsetRect);
                alignBottomRight(mDisplayRect, mOtherInsetRect);
            } else {
                alignBottomRight(mDisplayRect, mDockedInsetRect);
                alignTopLeft(mDisplayRect, mOtherInsetRect);
            }
            applyDismissingParallax(mDockedTaskRect, mDockSide, taskSnapTarget, position,
                    taskPositionDocked);
            applyDismissingParallax(mOtherTaskRect, dockSideInverted, taskSnapTarget, position,
                    taskPositionOther);
            mWindowManagerProxy.resizeDockedStack(mDockedRect, mDockedTaskRect, mDockedInsetRect,
                    mOtherTaskRect, mOtherInsetRect);
        } else {
            mWindowManagerProxy.resizeDockedStack(mDockedRect, null, null, null, null);
        }
        /* prize-modify-split screen-liyongli-20170608-start */
        if(!PrizeOption.PRIZE_SPLIT_SCREEN_DRAG) {
        SnapTarget closestDismissTarget = mSnapAlgorithm.getClosestDismissTarget(position);
        float dimFraction = getDimFraction(position, closestDismissTarget);
        mWindowManagerProxy.setResizeDimLayer(dimFraction != 0f,
                getStackIdForDismissTarget(closestDismissTarget),
                dimFraction);
        }
        /* prize-modify-split screen-liyongli-20170608-end */
    }

    private void applyExitAnimationParallax(Rect taskRect, int position) {
        if (mDockSide == WindowManager.DOCKED_TOP) {
            taskRect.offset(0, (int) ((position - mExitStartPosition) * 0.25f));
        } else if (mDockSide == WindowManager.DOCKED_LEFT) {
            taskRect.offset((int) ((position - mExitStartPosition) * 0.25f), 0);
        } else if (mDockSide == WindowManager.DOCKED_RIGHT) {
            taskRect.offset((int) ((mExitStartPosition - position) * 0.25f), 0);
        }
    }

    private float getDimFraction(int position, SnapTarget dismissTarget) {
        if (mEntranceAnimationRunning) {
            return 0f;
        }
        float fraction = mSnapAlgorithm.calculateDismissingFraction(position);
        fraction = Math.max(0, Math.min(fraction, 1f));
        fraction = DIM_INTERPOLATOR.getInterpolation(fraction);
        if (hasInsetsAtDismissTarget(dismissTarget)) {

            // Less darkening with system insets.
            fraction *= 0.8f;
        }
        return fraction;
    }

    /**
     * @return true if and only if there are system insets at the location of the dismiss target
     */
    private boolean hasInsetsAtDismissTarget(SnapTarget dismissTarget) {
        if (isHorizontalDivision()) {
            if (dismissTarget == mSnapAlgorithm.getDismissStartTarget()) {
                return mStableInsets.top != 0;
            } else {
                return mStableInsets.bottom != 0;
            }
        } else {
            if (dismissTarget == mSnapAlgorithm.getDismissStartTarget()) {
                return mStableInsets.left != 0;
            } else {
                return mStableInsets.right != 0;
            }
        }
    }

    /**
     * When the snap target is dismissing one side, make sure that the dismissing side doesn't get
     * 0 size.
     */
    private int restrictDismissingTaskPosition(int taskPosition, int dockSide,
            SnapTarget snapTarget) {
        if (snapTarget.flag == SnapTarget.FLAG_DISMISS_START && dockSideTopLeft(dockSide)) {
            return Math.max(mSnapAlgorithm.getFirstSplitTarget().position, mStartPosition);
        } else if (snapTarget.flag == SnapTarget.FLAG_DISMISS_END
                && dockSideBottomRight(dockSide)) {
            return Math.min(mSnapAlgorithm.getLastSplitTarget().position, mStartPosition);
        } else {
            return taskPosition;
        }
    }

    /**
     * Applies a parallax to the task when dismissing.
     */
    private void applyDismissingParallax(Rect taskRect, int dockSide, SnapTarget snapTarget,
            int position, int taskPosition) {
        float fraction = Math.min(1, Math.max(0,
                mSnapAlgorithm.calculateDismissingFraction(position)));
        SnapTarget dismissTarget = null;
        SnapTarget splitTarget = null;
        int start = 0;
        if (position <= mSnapAlgorithm.getLastSplitTarget().position
                && dockSideTopLeft(dockSide)) {
            dismissTarget = mSnapAlgorithm.getDismissStartTarget();
            splitTarget = mSnapAlgorithm.getFirstSplitTarget();
            start = taskPosition;
        } else if (position >= mSnapAlgorithm.getLastSplitTarget().position
                && dockSideBottomRight(dockSide)) {
            dismissTarget = mSnapAlgorithm.getDismissEndTarget();
            splitTarget = mSnapAlgorithm.getLastSplitTarget();
            start = splitTarget.position;
        }
        if (dismissTarget != null && fraction > 0f
                && isDismissing(splitTarget, position, dockSide)) {
            fraction = calculateParallaxDismissingFraction(fraction, dockSide);
            int offsetPosition = (int) (start +
                    fraction * (dismissTarget.position - splitTarget.position));
            int width = taskRect.width();
            int height = taskRect.height();
            switch (dockSide) {
                case WindowManager.DOCKED_LEFT:
                    taskRect.left = offsetPosition - width;
                    taskRect.right = offsetPosition;
                    break;
                case WindowManager.DOCKED_RIGHT:
                    taskRect.left = offsetPosition + mDividerSize;
                    taskRect.right = offsetPosition + width + mDividerSize;
                    break;
                case WindowManager.DOCKED_TOP:
                    taskRect.top = offsetPosition - height;
                    taskRect.bottom = offsetPosition;
                    break;
                case WindowManager.DOCKED_BOTTOM:
                    taskRect.top = offsetPosition + mDividerSize;
                    taskRect.bottom = offsetPosition + height + mDividerSize;
                    break;
            }
        }
    }

    /**
     * @return for a specified {@code fraction}, this returns an adjusted value that simulates a
     *         slowing down parallax effect
     */
    private static float calculateParallaxDismissingFraction(float fraction, int dockSide) {
        float result = SLOWDOWN_INTERPOLATOR.getInterpolation(fraction) / 3.5f;

        // Less parallax at the top, just because.
        if (dockSide == WindowManager.DOCKED_TOP) {
            result /= 2f;
        }
        return result;
    }

    private static boolean isDismissing(SnapTarget snapTarget, int position, int dockSide) {
        if (dockSide == WindowManager.DOCKED_TOP || dockSide == WindowManager.DOCKED_LEFT) {
            return position < snapTarget.position;
        } else {
            return position > snapTarget.position;
        }
    }

    private int getStackIdForDismissTarget(SnapTarget dismissTarget) {
        if ((dismissTarget.flag == SnapTarget.FLAG_DISMISS_START && dockSideTopLeft(mDockSide))
                || (dismissTarget.flag == SnapTarget.FLAG_DISMISS_END
                        && dockSideBottomRight(mDockSide))) {
            return StackId.DOCKED_STACK_ID;
        } else {
            return StackId.HOME_STACK_ID;
        }
    }

    /**
     * @return true if and only if {@code dockSide} is top or left
     */
    private static boolean dockSideTopLeft(int dockSide) {
        return dockSide == WindowManager.DOCKED_TOP || dockSide == WindowManager.DOCKED_LEFT;
    }

    /**
     * @return true if and only if {@code dockSide} is bottom or right
     */
    private static boolean dockSideBottomRight(int dockSide) {
        return dockSide == WindowManager.DOCKED_BOTTOM || dockSide == WindowManager.DOCKED_RIGHT;
    }

    @Override
    public void onComputeInternalInsets(InternalInsetsInfo inoutInfo) {
        inoutInfo.setTouchableInsets(InternalInsetsInfo.TOUCHABLE_INSETS_REGION);
        inoutInfo.touchableRegion.set(mHandle.getLeft(), mHandle.getTop(), mHandle.getRight(),
                mHandle.getBottom());
        inoutInfo.touchableRegion.op(mBackground.getLeft(), mBackground.getTop(),
                mBackground.getRight(), mBackground.getBottom(), Op.UNION);
    }

    /**
     * Checks whether recents will grow when invoked. This happens in multi-window when recents is
     * very small. When invoking recents, we shrink the docked stack so recents has more space.
     *
     * @return the position of the divider when recents grows, or
     *         {@link #INVALID_RECENTS_GROW_TARGET} if recents won't grow
     */
    public int growsRecents() {
        boolean result = mGrowRecents
                && mWindowManagerProxy.getDockSide() == WindowManager.DOCKED_TOP
                && getCurrentPosition() == getSnapAlgorithm().getLastSplitTarget().position;
        if (result) {
            return getSnapAlgorithm().getMiddleTarget().position;
        } else {
            return INVALID_RECENTS_GROW_TARGET;
        }
    }

    public final void onBusEvent(RecentsActivityStartingEvent recentsActivityStartingEvent) {
        if (mGrowRecents && getWindowManagerProxy().getDockSide() == WindowManager.DOCKED_TOP
                && getCurrentPosition() == getSnapAlgorithm().getLastSplitTarget().position) {
            mState.growAfterRecentsDrawn = true;
            startDragging(false /* animate */, false /* touching */);
        }
    }

    public final void onBusEvent(DockedTopTaskEvent event) {
        if (event.dragMode == NavigationBarGestureHelper.DRAG_MODE_NONE) {
            mState.growAfterRecentsDrawn = false;
            mState.animateAfterRecentsDrawn = true;
            startDragging(false /* animate */, false /* touching */);
        }
        updateDockSide();
        int position = DockedDividerUtils.calculatePositionForBounds(event.initialRect,
                mDockSide, mDividerSize);
        mEntranceAnimationRunning = true;

        // Insets might not have been fetched yet, so fetch manually if needed.
        if (mStableInsets.isEmpty()) {
            SystemServicesProxy.getInstance(mContext).getStableInsets(mStableInsets);
            mSnapAlgorithm = null;
            initializeSnapAlgorithm();
        }

        resizeStack(position, mSnapAlgorithm.getMiddleTarget().position,
                mSnapAlgorithm.getMiddleTarget());
    }
    

    private void QQcalculateBoundsForPosition(int position, int dockSide, Rect outRect) {

        DockedDividerUtils.calculateBoundsForPosition(position, dockSide, outRect, mDisplayWidth,
                mDisplayHeight, mDividerSize);

    }
    private void QQresizeStack(int position, int taskPosition, SnapTarget taskSnapTarget) {
                                                            //1280                 620
                                                            
        QQcalculateBoundsForPosition(position, mDockSide, mDockedRect);
            // ------- 1280, 2, 720 1280
           //Rect(0, 0 - 720, 1280)  ---- mDockedRect
        QQcalculateBoundsForPosition(taskPosition, mDockSide, mDockedTaskRect);
                //------- 620, 2, 720 1280
                //Rect(0, 0 - 720, 620), -- mDockedTaskRect
 /*               
        QQcalculateBoundsForPosition(taskPosition, mDockSide, mDockedRect);
                //------- 620, 2, 720 1280
                //Rect(0, 0 - 720, 620), ---- mDockedRect
         QQcalculateBoundsForPosition(isHorizontalDivision() ? mDisplayHeight : mDisplayWidth,
                        mDockSide, mDockedTaskRect);
               // ------- 1280, 2, 720 1280
              //Rect(0, 0 - 720, 1280),  -- mDockedTaskRect
*/            
        mLastResizeRect.set(mDockedRect);

        QQcalculateBoundsForPosition(taskPosition, DockedDividerUtils.invertDockSide(mDockSide),
                    mOtherTaskRect);
                //------- 620, 4, 720 1280
                //Rect(0, 624 - 720, 1280), --- mOtherTaskRect
            
        mWindowManagerProxy.QQresizeDockedStack(mDockedRect, mDockedTaskRect, null,
                    mOtherTaskRect, null);

    }
        
    public final void onBusEvent(QQdockedTopTaskEvent event) {
        if (event.dragMode == NavigationBarGestureHelper.DRAG_MODE_NONE) {
            cancelFlingAnimation();
            mDockSide = mWindowManagerProxy.getDockSide();
            initializeSnapAlgorithm();
            mWindowManagerProxy.setResizing(true);          
        }
        updateDockSide();
        int position = DockedDividerUtils.calculatePositionForBounds(event.initialRect,
                mDockSide, mDividerSize);
       mEntranceAnimationRunning = true;
                
        // Insets might not have been fetched yet, so fetch manually if needed.
        if (mStableInsets.isEmpty()) {
            SystemServicesProxy.getInstance(mContext).getStableInsets(mStableInsets);
            mSnapAlgorithm = null;
            initializeSnapAlgorithm();
        }
        
        final int taskPosition = mSnapAlgorithm.getMiddleTarget().position;
        mWindowManagerProxy.setResizing(true);
        //1. set qq Task Rect
        QQresizeStack(position, taskPosition, mSnapAlgorithm.getMiddleTarget());

        //2 open qq
        EventBus.getDefault().send(new PrizeOpenPendingIntentEvent( event.appType));

        //3 show
        int endDelay = 300;//100 black 300 no black;//250;
        SnapTarget target = mSnapAlgorithm.getMiddleTarget();
        //same as  up stopDraggingTest()
        calculateBoundsForPosition(target.position, mDockSide, mDockedRect);
        
        mWindowManager.setSlippery(true);
        releaseBackground();
        
                Runnable endAction = () -> {
                    mWindowManagerProxy.setResizing(true);
            //mWindowManagerProxy.QQresizeDockedStack(mDockedRect, null, null, null, null);
            mWindowManagerProxy.resizeDockedStack(mDockedRect, null, null, null, null);

            commitSnapFlags(target);
            mWindowManagerProxy.setResizing(false);
            mDockSide = WindowManager.DOCKED_INVALID;
            mCurrentAnimator = null;
            mEntranceAnimationRunning = false;
            mExitAnimationRunning = false;
            //EventBus.getDefault().send(new StoppedDragingEvent());
        };
        mHandler.postDelayed(endAction, endDelay);
    }
    
    public final void onBusEvent(RecentsDrawnEvent drawnEvent) {
        if (mState.animateAfterRecentsDrawn) {
            mState.animateAfterRecentsDrawn = false;
            updateDockSide();

            mHandler.post(() -> {
                // Delay switching resizing mode because this might cause jank in recents animation
                // that's longer than this animation.
                stopDragging(getCurrentPosition(), mSnapAlgorithm.getMiddleTarget(),
                        mLongPressEntraceAnimDuration, Interpolators.FAST_OUT_SLOW_IN,
                        200 /* endDelay */);
            });
        }
        if (mState.growAfterRecentsDrawn) {
            mState.growAfterRecentsDrawn = false;
            updateDockSide();
            EventBus.getDefault().send(new RecentsGrowingEvent());
            stopDragging(getCurrentPosition(), mSnapAlgorithm.getMiddleTarget(), 336,
                    Interpolators.FAST_OUT_SLOW_IN);
        }
    }

    public final void onBusEvent(UndockingTaskEvent undockingTaskEvent) {
        int dockSide = mWindowManagerProxy.getDockSide();
        if (dockSide != WindowManager.DOCKED_INVALID && !mDockedStackMinimized) {
            /* prize-modify-split screen-liyongli-20170713-start */
            if(PrizeOption.PRIZE_SPLIT_SCREEN_MENU && mMenuManager!=null) {
                mMenuManager.HideMenuViewNoAnim();
            }
            /* prize-modify-split screen-liyongli-20170713-end */
            startDragging(false /* animate */, false /* touching */);
            SnapTarget target = dockSideTopLeft(dockSide)
                    ? mSnapAlgorithm.getDismissEndTarget()
                    : mSnapAlgorithm.getDismissStartTarget();

            // Don't start immediately - give a little bit time to settle the drag resize change.
            mExitAnimationRunning = true;
            mExitStartPosition = getCurrentPosition();
            stopDragging(mExitStartPosition, target, 336 /* duration */, 100 /* startDelay */,
                    0 /* endDelay */, Interpolators.FAST_OUT_SLOW_IN);
        }
    }
    
    /* prize-add-split screen-liyongli-20170708-start */
    public void prizeNotifyDockedStackCreate(boolean exists) {
        
        mHandle.prizeChangeDockedStackFocus(false);
        if(PrizeOption.PRIZE_SPLIT_SCREEN_BG_HINT_FOCUS) {
            prizeDockedFocused = false;
            prizeChangeBackgroud(prizeDockedFocused);
        }
    }
    
    public Rect prizeGetDockedRect() {
        return mDockedRect;
    }
    public boolean prizeDockedIsFocused() {
        return prizeDockedFocused;
    }
    /* prize-add-split screen-liyongli-20170708-end */
    
    //for  quare side focus, not use
    public interface OnDragChangedListener {
        void onDragChanged(int focusStack, Rect dockedRect, Rect otherRect);
    }
    private OnDragChangedListener mOnDragChangedListener;
    public void setOnDragChangedListener(OnDragChangedListener onDragChangedListener) {
        mOnDragChangedListener = onDragChangedListener;
    }
    private void notifyDragChangedListener(int focusStack, Rect dockedRect, Rect otherRect) {
        if (mOnDragChangedListener != null) {
            mOnDragChangedListener.onDragChanged( focusStack, dockedRect, otherRect);
        }
    }
    
    /* prize-add-split screen, for HOME exit split screen -liyongli-20170911-start */
    public void hideDragCircle() {
        mHandle.setAlpha( 0f );
        if(PrizeOption.PRIZE_SPLIT_SCREEN_MENU && mMenuManager!=null) {
            mMenuManager.HideMenuViewNoAnim(); //2017/11/11  add, HOME hiden the pop menu
            mWindowManagerProxy.setResizing(false); //2017/11/11 add, fix bug 42071  pop menu HOME, NavBar not Transparent 
        }
    }
    /* prize-add-split screen, for HOME exit split screen -liyongli-20170911-end */

}
