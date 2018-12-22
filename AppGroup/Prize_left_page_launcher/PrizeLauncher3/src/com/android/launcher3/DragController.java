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

package com.android.launcher3;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.inputmethod.InputMethodManager;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.CellLayout.CellInfo;
import com.android.launcher3.R;
import com.android.launcher3.DropTarget.DragObject;

import com.mediatek.launcher3.ext.LauncherLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Class for initiating a drag within a view or across multiple views.
 */
public class DragController {
    private static final String TAG = "Launcher.DragController";

    /** Indicates the drag is a move.  */
    public static int DRAG_ACTION_MOVE = 0;

    /** Indicates the drag is a copy.  */
    public static int DRAG_ACTION_COPY = 1;

    private static final int SCROLL_DELAY = 500;
    private static final int RESCROLL_DELAY = PagedView.PAGE_SNAP_ANIMATION_DURATION + 150;

    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;

    private static final int SCROLL_OUTSIDE_ZONE = 0;
    private static final int SCROLL_WAITING_IN_ZONE = 1;

    static final int SCROLL_NONE = -1;
    static final int SCROLL_LEFT = 0;
    static final int SCROLL_RIGHT = 1;

    private static final float MAX_FLING_DEGREES = 35f;

    private Launcher mLauncher;
    private Handler mHandler;

    // temporaries to avoid gc thrash
    private Rect mRectTemp = new Rect();
    private final int[] mCoordinatesTemp = new int[2];

    /** Whether or not we're dragging. */
    private boolean mDragging;

    /** X coordinate of the down event. */
    private int mMotionDownX;

    /** Y coordinate of the down event. */
    private int mMotionDownY;

    /** the area at the edge of the screen that makes the workspace go left
     *   or right while you're dragging.
     */
    private int mScrollZone;

    public DropTarget.DragObject mDragObject;

    /** Who can receive drop events */
    private ArrayList<DropTarget> mDropTargets = new ArrayList<DropTarget>();
    private ArrayList<DragListener> mListeners = new ArrayList<DragListener>();
    private DropTarget mFlingToDeleteDropTarget;

    /** The window token used as the parent for the DragView. */
    private IBinder mWindowToken;

    /** The view that will be scrolled when dragging to the left and right edges of the screen. */
    private View mScrollView;

    private View mMoveTarget;

    private DragScroller mDragScroller;
    private int mScrollState = SCROLL_OUTSIDE_ZONE;
    private ScrollRunnable mScrollRunnable = new ScrollRunnable();

    private DropTarget mLastDropTarget;

    public DropTarget getLastDropTarget() {
		return mLastDropTarget;
	}

	private InputMethodManager mInputMethodManager;

    /**
     * 批量编辑的时候 判断是否选中图标动画都结束
     */
    public boolean mMultipleDraging = false;

    private int mLastTouch[] = new int[2];
    private long mLastTouchUpTime = -1;
    private int mDistanceSinceScroll = 0;

    private int mTmpPoint[] = new int[2];
    private Rect mDragLayerRect = new Rect();

    protected int mFlingToDeleteThresholdVelocity;
    private VelocityTracker mVelocityTracker;

	private MotionEvent mMotionEvent;

    public MotionEvent getmMotionEvent() {
		return mMotionEvent;
	}

	/**
     * Interface to receive notifications when a drag starts or stops
     */
    interface DragListener {
        /**
         * A drag has begun
         *
         * @param source An object representing where the drag originated
         * @param info The data associated with the object that is being dragged
         * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE}
         *        or {@link DragController#DRAG_ACTION_COPY}
         */
        void onDragStart(DragSource source, Object info, int dragAction);

        /**
         * The drag has ended
         */
        void onDragEnd();
    }
    
    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     */
    public DragController(Launcher launcher) {
        Resources r = launcher.getResources();
        mLauncher = launcher;
        mHandler = new Handler();
        mScrollZone = r.getDimensionPixelSize(R.dimen.scroll_zone);
        mVelocityTracker = VelocityTracker.obtain();

        float density = r.getDisplayMetrics().density;
        mFlingToDeleteThresholdVelocity =
                (int) (r.getInteger(R.integer.config_flingToDeleteMinVelocity) * density);
    }

    public boolean dragging() {
        return mDragging;
    }

    /**
     * Starts a drag.
     *
     * @param v The view that is being dragged
     * @param bmp The bitmap that represents the view being dragged
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *        {@link #DRAG_ACTION_COPY}
     * @param dragRegion Coordinates within the bitmap b for the position of item being dragged.
     *          Makes dragging feel more precise, e.g. you can clip out a transparent border
     */
    public void startDrag(View v, Bitmap bmp, DragSource source, Object dragInfo, int dragAction,
            Point extraPadding, float initialDragViewScale) {
        int[] loc = mCoordinatesTemp;
        mLauncher.getDragLayer().getLocationInDragLayer(v, loc);
        int viewExtraPaddingLeft = extraPadding != null ? extraPadding.x : 0;
        int viewExtraPaddingTop = extraPadding != null ? extraPadding.y : 0;
        int dragLayerX = loc[0] + v.getPaddingLeft() + viewExtraPaddingLeft +
                (int) ((initialDragViewScale * bmp.getWidth() - bmp.getWidth()) / 2);
        int dragLayerY = loc[1] + v.getPaddingTop() + viewExtraPaddingTop +
                (int) ((initialDragViewScale * bmp.getHeight() - bmp.getHeight()) / 2);

        startDrag(bmp, dragLayerX, dragLayerY, source, dragInfo, dragAction, null,
                null, initialDragViewScale,v);

        if (dragAction == DRAG_ACTION_MOVE) {
            v.setVisibility(View.GONE);
        }
    }

    
   
    
    /**
     * Starts a drag.
     *
     * @param b The bitmap to display as the drag image.  It will be re-scaled to the
     *          enlarged size.
     * @param dragLayerX The x position in the DragLayer of the left-top of the bitmap.
     * @param dragLayerY The y position in the DragLayer of the left-top of the bitmap.
     * @param source An object representing where the drag originated
     * @param dragInfo The data associated with the object that is being dragged
     * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
     *        {@link #DRAG_ACTION_COPY}
     * @param dragRegion Coordinates within the bitmap b for the position of item being dragged.
     *          Makes dragging feel more precise, e.g. you can clip out a transparent border
     */
    public void startDrag(Bitmap b, int dragLayerX, int dragLayerY,
            DragSource source, Object dragInfo, int dragAction, Point dragOffset, Rect dragRegion,
            float initialDragViewScale,View v) {
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        // Hide soft keyboard, if visible
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager)
                    mLauncher.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(mWindowToken, 0);
        for (DragListener listener : mListeners) {

		/*	if (source instanceof AppsCustomizePagedView
					&& mLauncher.getWorkspace().isInSpringLoadMoed()
					&& listener instanceof SearchDropTargetBar) {
				continue;

			}*/
            listener.onDragStart(source, dragInfo, dragAction);
        }

        final int registrationX = mMotionDownX - dragLayerX;
        final int registrationY = mMotionDownY - dragLayerY;

        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "startDrag: dragLayerX = " + dragLayerX + ", dragLayerY = " + dragLayerY
                    + ", dragInfo = " + dragInfo + ", registrationX = " + registrationX
                    + ", registrationY = " + registrationY + ", dragRegion = " + dragRegion);
        }

        final int dragRegionLeft = dragRegion == null ? 0 : dragRegion.left;
        final int dragRegionTop = dragRegion == null ? 0 : dragRegion.top;

        mDragging = true;
        if(source instanceof  AppsCustomizePagedView) {
        if(mDragObject !=null) {
        	if(mDragObject.dragInfo!=null && (mDragObject.dragInfo instanceof ShortcutInfo|| mDragObject.dragInfo instanceof FolderInfo)) {
        		if(mDragObject.dragSource instanceof Workspace) {
        			Workspace w = (Workspace) mDragObject.dragSource;
        			if(w.getDragInfo() !=null && w.getDragInfo().cell !=null) {
        				View vs = w.getDragInfo().cell;
        				vs.setVisibility(View.VISIBLE);
        				w.getCurrentDropLayout().markCellsAsOccupiedForView(vs);
        			}
        		}
        	}
        }
        if(mDragObject !=null) {
        	if(mDragObject.dragInfo!=null && mDragObject.dragInfo instanceof LauncherAppWidgetInfo) {
        		LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) mDragObject.dragInfo;
        		if(mDragObject.dragSource instanceof Workspace) {
        			Workspace w = (Workspace) mDragObject.dragSource;
            		if(info.hostView!=null) {
                  		 info.hostView.setVisibility(View.VISIBLE);
           				w.getCurrentDropLayout().markCellsAsOccupiedForView(info.hostView);
               		}
        			}
        		}
        	}
    	
    }
        mDragObject = new DropTarget.DragObject();

        mDragObject.dragComplete = false;
        mDragObject.xOffset = mMotionDownX - (dragLayerX + dragRegionLeft);
        mDragObject.yOffset = mMotionDownY - (dragLayerY + dragRegionTop);
        mDragObject.dragSource = source;
        mDragObject.dragInfo = dragInfo;
        
         DragView dragView = mDragObject.dragView = new DragView(mLauncher, b, registrationX,
                registrationY, 0, 0, b.getWidth(), b.getHeight(), initialDragViewScale);
        
        if(dragInfo instanceof ShortcutInfo) {
        	ShortcutInfo info =(ShortcutInfo) dragInfo;
        	
        	if(null != info.intent&&null != info.intent.getComponent() && mLauncher.isDeskClockIcon(info.intent.getComponent())) {
        		dragView = mDragObject.dragView = new DeskClockDragView(mLauncher, b, registrationX,
                        registrationY, 0, 0, b.getWidth(), b.getHeight(), initialDragViewScale);
        	}
        }

        if (dragOffset != null) {
            dragView.setDragVisualizeOffset(new Point(dragOffset));
        }
        if (dragRegion != null) {
            dragView.setDragRegion(new Rect(dragRegion));
        }

        mLauncher.getDragLayer().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        onStartMuitipleDrag(v,mDragObject,source,dragView);
        if(mDragObject.dragViews.size()>0) {
            mMultipleDraging =false;
        }else {
            dragView.show(mMotionDownX, mMotionDownY);
            handleMoveEvent(mMotionDownX, mMotionDownY,null,null);
            
        }
    }
    
    
	/**启动所有的选中图标动画预处理
	 * @param child 动画到目标View
	 * @param dragobject 拖拽源信息
	 * @param source 拖动原始的父控件
	 * @param dragView 拖动View
	 */
	public void onStartMuitipleDrag(View child, DragObject dragobject,
			DragSource source, DragView dragView) {
		int index = 0;
		HashMap<Long, View> dragchilds = mLauncher.getworkspace()
				.getMultipleDragViews();
		for (long dragViewid : dragchilds.keySet()) {
			View dragChild = dragchilds.get(dragViewid);
			StartMultipleDragRunnable r = new StartMultipleDragRunnable(dragView);
			source.onStartMuitipleDrag(child, mDragObject,
					r, dragChild);
			r.setDragChild(dragChild);
			if (index == dragchilds.size()-1) {
//				mMultipleDraging = true;
				r.setLasted(true);
				
			}
			index++;

		}

	}
    
	/**
	 * 检测动画结束 类
	 * @author Administrator
	 *
	 */
	class StartMultipleDragRunnable implements Runnable {
		DragView dragView;
		boolean lasted = false;
		View dragChild;

		public View getDragChild() {
			return dragChild;
		}

		public void setDragChild(View dragChild) {
			this.dragChild = dragChild;
		}

		public boolean isLasted() {
			return lasted;
		}

		public void setLasted(boolean lasted) {
			this.lasted = lasted;
		}

		public StartMultipleDragRunnable(DragView dragView) {
			super();
			this.dragView = dragView;
		}

		@Override
		public void run() {
			if (dragChild != null) {
				if(!mDragObject.deferDragViewCleanupPostAnimation) {
					dragView.clearDragView();
					return;
				}
				DragView dv = mDragObject.dragViews.get(dragChild);
				dv.setRegistrationX(dragView.getRegistrationX());
				dv.setRegistrationY(dragView.getRegistrationY());
				dv.shows(mMotionDownX, mMotionDownY);
				dragView.shows(mMotionDownX, mMotionDownY);
				handleMoveEvent(mMotionDownX, mMotionDownY, null,dv);
				if (lasted) {
					mMultipleDraging = true;
				}
			}
		}

	}

    /**
     * Draw the view into a bitmap.
     */
    Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        float alpha = v.getAlpha();
        v.setAlpha(1.0f);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setAlpha(alpha);
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    /**
     * Call this from a drag source view like this:
     *
     * <pre>
     *  @Override
     *  public boolean dispatchKeyEvent(KeyEvent event) {
     *      return mDragController.dispatchKeyEvent(this, event)
     *              || super.dispatchKeyEvent(event);
     * </pre>
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (LauncherLog.DEBUG_KEY) {
            LauncherLog.d(TAG, "dispatchKeyEvent: keycode = " + event.getKeyCode() + ", action = "
                    + event.getAction() + ", mDragging = " + mDragging);
        }

        return mDragging;
    }

    public boolean isDragging() {
        return mDragging;
    }

    /**
     * Stop dragging without dropping.
     */
    public void cancelDrag() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "cancelDrag: mDragging = " + mDragging + ", mLastDropTarget = " + mLastDropTarget);
        }

        if (mDragging) {

            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
            mDragObject.deferDragViewCleanupPostAnimation = false;
            mDragObject.cancelled = true;
            mDragObject.dragComplete = true;
            mDragObject.dragSource.onDropCompleted(null, mDragObject, false, false);
            
            if(mDragObject!=null&&mDragObject.dragViews!=null&&mDragObject.dragViews.size()>0) {
                Iterator<View> drags = mDragObject.dragViews.keySet().iterator();
                while (drags.hasNext()) {
                	View v=drags.next();
                	DragView dragView =mDragObject.dragViews.get(v);
                	dragView.remove();
                	dragView=null;
                	v.setVisibility(View.VISIBLE);
                 	if(v.getParent()!=null&&v.getParent().getParent() instanceof CellLayout) {
                 		CellLayout cellParent = (CellLayout) v.getParent().getParent();
                 		cellParent.markCellsAsOccupiedForView(v);
                 	}
        			
        		}
            }
        }
        endDrag();
    }
    public void onAppsRemoved(ArrayList<AppInfo> appInfos, Context context) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onAppsRemoved: mDragging = " + mDragging + ", mDragObject = " + mDragObject);
        }

        // Cancel the current drag if we are removing an app that we are dragging

        // M: If we aren't dragging the shortcut, mDragObject could have incorrect
        // field values now. Don't need to check it in this situation. See log of
        // ALPS01335043 for details.
        if (mDragging && mDragObject != null) {
            Object rawDragInfo = mDragObject.dragInfo;
            if (rawDragInfo instanceof ShortcutInfo) {
                ShortcutInfo dragInfo = (ShortcutInfo) rawDragInfo;
                for (AppInfo info : appInfos) {
                    // Added null checks to prevent NPE we've seen in the wild
                    if (dragInfo != null &&
                        dragInfo.intent != null) {
                    	try {
                            boolean isSameComponent =
                                    dragInfo.intent.getComponent().equals(info.componentName);
                            if (isSameComponent) {
                                cancelDrag();
                                return;
                            }
						} catch (Exception e) {
							e.printStackTrace();
						}
                    }
                }
            }
        }
    }

    
    private void endDrag() {
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "endDrag: mDragging = " + mDragging + ", mDragObject = " + mDragObject);
        }
        if (mDragging) {

//            mLauncher.getSearchBar().OnPause();
            mDragging = false;
            clearScrollRunnable();
            boolean isDeferred = false;
            if (mDragObject.dragView != null) {
                isDeferred = mDragObject.deferDragViewCleanupPostAnimation;
                if (!isDeferred) {
                    mDragObject.dragView.remove();
                    mDragObject.dragView=null;
                }
                mDragObject.dragView = null;
               
            }

            // Only end the drag if we are not deferred
            if (!isDeferred) {
                for (DragListener listener : mListeners) {
                	/*if (mDragObject.dragSource instanceof AppsCustomizePagedView
        					&& mLauncher.getWorkspace().isInSpringLoadMoed()
        					&& listener instanceof SearchDropTargetBar) {
        				continue;

        			}*/
                    listener.onDragEnd();
                }
            }
        }

        releaseVelocityTracker();
    }

    /**
     * This only gets called as a result of drag view cleanup being deferred in endDrag();
     */
    void onDeferredEndDrag(DragView dragView) {
        dragView.remove();
        dragView=null;

        if (mDragObject!= null&&mDragObject.deferDragViewCleanupPostAnimation) {//add by zel
            // If we skipped calling onDragEnd() before, do it now
            for (DragListener listener : mListeners) {
            	/*if (mDragObject.dragSource instanceof AppsCustomizePagedView
    					&& mLauncher.getWorkspace().isInSpringLoadMoed()
    					&& listener instanceof SearchDropTargetBar) {
    				continue;

    			}*/
                listener.onDragEnd();
            }
        }
    }

    void onDeferredEndFling(DropTarget.DragObject d) {
        d.dragSource.onFlingToDeleteCompleted();
    }

    /**
     * Clamps the position to the drag layer bounds.
     */
    private int[] getClampedDragLayerPos(float x, float y) {
        mLauncher.getDragLayer().getLocalVisibleRect(mDragLayerRect);
        mTmpPoint[0] = (int) Math.max(mDragLayerRect.left, Math.min(x, mDragLayerRect.right - 1));
        mTmpPoint[1] = (int) Math.max(mDragLayerRect.top, Math.min(y, mDragLayerRect.bottom - 1));
        return mTmpPoint;
    }

    long getLastGestureUpTime() {
        if (mDragging) {
            return System.currentTimeMillis();
        } else {
            return mLastTouchUpTime;
        }
    }

    void resetLastGestureUpTime() {
        mLastTouchUpTime = -1;
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean debug = false;
        if (debug) {
            Log.d(Launcher.TAG, "DragController.onInterceptTouchEvent " + ev + " mDragging="
                    + mDragging);
        }

        // Update the velocity tracker
        acquireVelocityTrackerAndAddMovement(ev);

        mMotionEvent = ev;
        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d(TAG, "onInterceptTouchEvent: action = " + action + ", mDragging = " + mDragging
                    + ", dragLayerX = " + dragLayerX + ", dragLayerY = " + dragLayerY);
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                mLastDropTarget = null;
                break;
            case MotionEvent.ACTION_UP:
                mLastTouchUpTime = System.currentTimeMillis();
                if (mDragging) {
                    PointF vec = isFlingingToDelete(mDragObject.dragSource);
                    if (!DeleteDropTarget.willAcceptDrop(mDragObject.dragInfo)) {
                        vec = null;
                    }
                    if (vec != null) {
                        dropOnFlingToDeleteTarget(dragLayerX, dragLayerY, vec);
                    } else {
                        drop(dragLayerX, dragLayerY);
                    }
                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelDrag();
                break;
        }

        return mDragging;
    }

    /**
     * Sets the view that should handle move events.
     */
    void setMoveTarget(View view) {
        mMoveTarget = view;
    }    

    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (LauncherLog.DEBUG_KEY) {
            LauncherLog.d(TAG, "dispatchUnhandledMove: focused = " + focused + ", direction = " + direction);
        }

        return mMoveTarget != null && mMoveTarget.dispatchUnhandledMove(focused, direction);
    }

    private void clearScrollRunnable() {
        mHandler.removeCallbacks(mScrollRunnable);
        if (mScrollState == SCROLL_WAITING_IN_ZONE) {
            mScrollState = SCROLL_OUTSIDE_ZONE;
            mScrollRunnable.setDirection(SCROLL_RIGHT);
            mDragScroller.onExitScrollArea();
            mLauncher.getDragLayer().onExitScrollArea();
        }
    }

    int mLastPointX=0;
    int mLastPointY=0;
    /**批处理方法 
     * 移动批处理图标
     * @param x
     * @param y
     * @param e
     * @param dv
     */
    private void moves(int x,int y,MotionEvent e,DragView dv) {
    	Log.i("zhouerlong", "moves====================");
		int i=0;
    	for(View v :mDragObject.dragViews.keySet()) {
    		DragView dragview = mDragObject.dragViews.get(v);
    		if (dragview != null) {
    		/*	dragview.clearAnimation();
    		AnimationRingForDragview value = mDragObject.mDragAnimations.get(dragview);
    		value.setPoint(x, y,e);
    		value.move(0);*/
    			dragview.move(x, y,null);
    		}
    		i++;
    	}

    }
    


	/**此类为了提高多拽桌面性能
	 * @author Administrator
	 *
	 */
	class OnMyAlarmListener implements OnAlarmListener {

		private int pos;

		public OnMyAlarmListener(int x, int y, MotionEvent ev,DragView dv,int pos) {
			super();
			this.x = x;
			this.y = y;
			this.ev = ev;
			this.dv = dv;
			this.pos = pos;
		}

		int x, y;
		MotionEvent ev;
		
		DragView dv;

		@Override
		public void onAlarm(Alarm alarm) {
//			moves(x, y, ev,dv);

//    		DragView dragview = mDragObject.dragViews.get(pos);
    		if (dv != null) {
    			Log.i("zhouerlong", "xy:"+"["+x+","+y+"]");
    			dv.move(x, y,null);
    		}
		}

	}
    private void handleMoveEvent(int x, int y,MotionEvent event,DragView dv) {
    	if (mDragObject.dragView != null) {
    		

            ViewConfiguration config = ViewConfiguration.get(mLauncher);
            mVelocityTracker.computeCurrentVelocity(600, config.getScaledMaximumFlingVelocity());
            mDragObject.dragView.move(x, y,mVelocityTracker);
    	}

/*//    	if (dv != null) {
    	for(int i=0)
            mReorderAlarm.cancelAlarm();
            mReorderAlarm.setOnAlarmListener(new OnMyAlarmListener(x,y,event,dv));
            mReorderAlarm.setAlarm(0);
//    	}
*/            

    		int i=0;
        	for(View v :mDragObject.dragViews.keySet()) {
        		DragView dragView = mDragObject.dragViews.get(v);
        		dragView.bringToFront();
        		
                Alarm	reorderAlarm =new Alarm();
                reorderAlarm.setOnAlarmListener(new OnMyAlarmListener(x,y,event,dragView,i));
                reorderAlarm.setAlarm(i*30);
//        		DragView dragview = mDragObject.dragViews.get(v);
        	/*	if (dragview != null) {
        			dragview.move(x, y);
        		}*/
        		i++;
        	}
        

//		moves(x, y, event);
        

        // Drop on someone?
        final int[] coordinates = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget(x, y, coordinates);
        float p;
        if(dropTarget instanceof Folder) {
            coordinates[1]-=100;
        }
        p=findDropTargetAndScrollProgress(x,y,coordinates);

        LogUtils.i("zhouerlong", "findDropTarget::::"+p);
        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];

        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "handleMoveEvent: x = " + x + ", y = " + y + ", dragView = "
                    + mDragObject.dragView + ", dragX = " + mDragObject.x + ", dragY = " + mDragObject.y);
        }

        checkTouchMove(dropTarget);

        // Check if we are hovering over the scroll areas
        mDistanceSinceScroll +=
            Math.sqrt(Math.pow(mLastTouch[0] - x, 2) + Math.pow(mLastTouch[1] - y, 2));
        mLastTouch[0] = x;
        mLastTouch[1] = y;
        checkScrollState(x, y);
    }

    public void forceTouchMove() {
        int[] dummyCoordinates = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget(mLastTouch[0], mLastTouch[1], dummyCoordinates);
        mDragObject.x = dummyCoordinates[0];
        mDragObject.y = dummyCoordinates[1];
        checkTouchMove(dropTarget);
    }
	//M by zhouerlong
    /**
     * @param dropTarget 此参数表示 拖动的目标容器 例如有workspace hotseat 或者其他的
     */
    private void checkTouchMove(DropTarget dropTarget) { 
        if (dropTarget != null) {

            Workspace workspace=null;
            if (dropTarget instanceof Workspace) {
            	workspace = (Workspace) dropTarget;
            }
            if (mLastDropTarget != dropTarget) { //如果之前的mLastDropTarget 不同于现在的容器 也就是说droptarget 刚刚开始进入 mLastDropTarget需要退出 如果mLastDropTarget == null 表示是刚开始长按拖动
                if (mLastDropTarget != null) {
                	if (mLastDropTarget instanceof DeleteDropTarget) {
                		mLauncher.enterDeleteDropTarget(false);
                	}
                    mLastDropTarget.onDragExit(mDragObject);//退出旧的容器
                }
                if (mLastDropTarget == null) {
                	dropTarget.onFristEnter(mDragObject);
                }
                dropTarget.onDragEnter(mDragObject); //进入新的容器
            }
            dropTarget.onDragOver(mDragObject);//这个方法会在拖动的时候一直毁掉 这里表示在同一个容器内移动
        } else {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
        }
        mLastDropTarget = dropTarget; //这里是将之前的容器重新赋值
    }

    private void checkScrollState(int x, int y) {
        final int slop = ViewConfiguration.get(mLauncher).getScaledWindowTouchSlop();
        final int delay = mDistanceSinceScroll < slop ? RESCROLL_DELAY : SCROLL_DELAY;
        final DragLayer dragLayer = mLauncher.getDragLayer();
        final boolean isRtl = (dragLayer.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
        final int forwardDirection = isRtl ? SCROLL_RIGHT : SCROLL_LEFT;
        final int backwardsDirection = isRtl ? SCROLL_LEFT : SCROLL_RIGHT;
        Rect hitRect = new Rect();
		mLauncher.getDragLayer().getDescendantRectRelativeToSelf(mLauncher.getHotseat(), hitRect);

        if (x < mScrollZone&&y<hitRect.top) {
            if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                mScrollState = SCROLL_WAITING_IN_ZONE;
                if (mDragScroller.onEnterScrollArea(x, y, forwardDirection)) {
                    dragLayer.onEnterScrollArea(forwardDirection);
                    mScrollRunnable.setDirection(forwardDirection);
                    mHandler.postDelayed(mScrollRunnable, delay);
                }
            }
        } else if (y<hitRect.top&&(x > mScrollView.getWidth() - mScrollZone)) {
            if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                mScrollState = SCROLL_WAITING_IN_ZONE;
                if (mDragScroller.onEnterScrollArea(x, y, backwardsDirection)) {
                    dragLayer.onEnterScrollArea(backwardsDirection);
                    mScrollRunnable.setDirection(backwardsDirection);
                    mHandler.postDelayed(mScrollRunnable, delay);
                }
            }
        } else {
            clearScrollRunnable();
        }
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging) {
            return false;
        }
        //add by zhouerlong begin 20150901 如果所有的图标还没有结束收缩动画 不执行Ontouch
        if (mDragObject.dragViews.size()>0) {
            if (!mMultipleDraging&&ev.getAction()==MotionEvent.ACTION_MOVE) {
            	return true;
            }
        }
        //add by zhouerlong end 20150901
        if(!(mLastDropTarget instanceof DeleteDropTarget)) {
            mLauncher.getworkspace().onTouchEvent(ev);
        }
        // Update the velocity tracker
        acquireVelocityTrackerAndAddMovement(ev);

        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];

        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d(TAG, "onTouchEvent: action = " + action + ", dragLayerX = " + dragLayerX
                    + ", dragLayerY = " + dragLayerY + ", mMotionDownX = " + mMotionDownX
                    + ", mMotionDownY = " + mMotionDownY + ", mScrollState = " + mScrollState);
        }

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            // Remember where the motion event started
            mMotionDownX = dragLayerX;
            mMotionDownY = dragLayerY;

            if ((dragLayerX < mScrollZone) || (dragLayerX > mScrollView.getWidth() - mScrollZone)) {
                mScrollState = SCROLL_WAITING_IN_ZONE;
                mHandler.postDelayed(mScrollRunnable, SCROLL_DELAY);
            } else {
                mScrollState = SCROLL_OUTSIDE_ZONE;
            }
            handleMoveEvent(dragLayerX, dragLayerY,ev,null);
            break;
        case MotionEvent.ACTION_MOVE:
            handleMoveEvent(dragLayerX, dragLayerY,ev,null);
            break;
        case MotionEvent.ACTION_UP:
            // Ensure that we've processed a move event at the current pointer location.
            handleMoveEvent(dragLayerX, dragLayerY,ev,null);
            mHandler.removeCallbacks(mScrollRunnable);

            if (mDragging) {
                PointF vec = isFlingingToDelete(mDragObject.dragSource);
                if (!DeleteDropTarget.willAcceptDrop(mDragObject.dragInfo)||(mLauncher.getworkspace()!=null&&mLauncher.getworkspace().isInSpringLoadMoed())) {
                    vec = null;
                }
                if (vec != null&&mDragObject.dragSource instanceof Workspace) {
                		Workspace w = (Workspace) mDragObject.dragSource;
                		if(!(w.getMultipleDragViews().size()>0)) {
                            dropOnFlingToDeleteTarget(dragLayerX, dragLayerY, vec);
                	}else {

                        drop(dragLayerX, dragLayerY);
                	}
                } else {
                    drop(dragLayerX, dragLayerY);
                }
            }
            endDrag();
            break;
        case MotionEvent.ACTION_CANCEL:
            mHandler.removeCallbacks(mScrollRunnable);
            cancelDrag();
            break;
        }

        return true;
    }

    /**
     * Determines whether the user flung the current item to delete it.
     *
     * @return the vector at which the item was flung, or null if no fling was detected.
     */
    public PointF isFlingingToDelete(DragSource source) {
        /// M: return null if apps list is in edit mode, for op09.
        if (Launcher.isInEditMode()) return null;
        if (mFlingToDeleteDropTarget == null) return null;
        if (!source.supportsFlingToDelete()) return null;

        ViewConfiguration config = ViewConfiguration.get(mLauncher);
        mVelocityTracker.computeCurrentVelocity(1000, config.getScaledMaximumFlingVelocity());

        if (mVelocityTracker.getYVelocity() < mFlingToDeleteThresholdVelocity) {
            // Do a quick dot product test to ensure that we are flinging upwards
            PointF vel = new PointF(mVelocityTracker.getXVelocity(),
                    mVelocityTracker.getYVelocity());
            PointF upVec = new PointF(0f, -1f);
            float theta = (float) Math.acos(((vel.x * upVec.x) + (vel.y * upVec.y)) /
                    (vel.length() * upVec.length()));
            if (theta <= Math.toRadians(MAX_FLING_DEGREES)) {
                return vel;
            }
        }
        return null;
    }

    private void dropOnFlingToDeleteTarget(float x, float y, PointF vel) {
        final int[] coordinates = mCoordinatesTemp;

        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];

        // Clean up dragging on the target if it's not the current fling delete target otherwise,
        // start dragging to it.
        if (mLastDropTarget != null && mFlingToDeleteDropTarget != mLastDropTarget) {
            mLastDropTarget.onDragExit(mDragObject);
        }

        // Drop onto the fling-to-delete target
        boolean accepted = false;
        mFlingToDeleteDropTarget.onDragEnter(mDragObject);
        // We must set dragComplete to true _only_ after we "enter" the fling-to-delete target for
        // "drop"
        mDragObject.dragComplete = true;
        mFlingToDeleteDropTarget.onDragExit(mDragObject);
        if (mFlingToDeleteDropTarget.acceptDrop(mDragObject)) {
            mFlingToDeleteDropTarget.onFlingToDelete(mDragObject, mDragObject.x, mDragObject.y,
                    vel);
            accepted = true;
        }
        mDragObject.dragSource.onDropCompleted((View) mFlingToDeleteDropTarget, mDragObject, true,
                accepted);
    }

    private void drop(float x, float y) {
        final int[] coordinates = mCoordinatesTemp;
        final DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);

        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];
        if (LauncherLog.DEBUG_DRAG) {
            LauncherLog.d(TAG, "drop: x = " + x + ", y = " + y + ", mDragObject.x = " + mDragObject.x
                    + ", mDragObject.y = " + mDragObject.y + ", dropTarget = " + dropTarget);
        }

        boolean accepted = false;
        if (dropTarget != null) {
            mDragObject.dragComplete = true;
            dropTarget.onDragExit(mDragObject);
            if (dropTarget.acceptDrop(mDragObject)) {
                dropTarget.onDrop(mDragObject);
                accepted = true;
            }
        }
        mDragObject.dragSource.onDropCompleted((View) dropTarget, mDragObject, false, accepted);
    }
    
    
    private float findDropTargetAndScrollProgress(int x,int y,int[] dropCoordinates ) {
        final Rect r = mRectTemp;
        float progress=0;
        final ArrayList<DropTarget> dropTargets = mDropTargets;
        final int count = dropTargets.size();
        for (int i=count-1; i>=0; i--) {
            DropTarget target = dropTargets.get(i);
            if (!target.isDropEnabled())
                continue;

            target.getHitRectRelativeToDragLayer(r);

            mDragObject.x = x;
            mDragObject.y = y;
            if ((target instanceof DeleteDropTarget)&&r.contains(x, y)) {

                dropCoordinates[0] = x;
                dropCoordinates[1] = y;
                mLauncher.getDragLayer().mapCoordInSelfToDescendent((View) target, dropCoordinates);
                int h = r.height();
                  progress =Math.abs((-r.bottom+y)/(float)(h/2));
                  progress = Math.min(1f, Math.max(0, progress));
//                  if(!(mDragObject.dragInfo instanceof LauncherAppWidgetInfo))  {
                  		if(isFlingingToDelete(mDragObject.dragSource)!=null&&Launcher.isSupportTranslateByUninstall) {
                  			progress=0;
                  			h=0;
                  		}
                  		if(Launcher.isSupportTranslateByUninstall) {
                            mLauncher.setTranslationYByProgress(progress,progress*h,h);
                  		}else {
                            mLauncher.setTranslationYByProgress(0,0*h,h);
                  		}
//                  }
            }
        }
        return progress;
    
    	
    }

    private DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        final Rect r = mRectTemp;

        final ArrayList<DropTarget> dropTargets = mDropTargets;
        final int count = dropTargets.size();
        for (int i=count-1; i>=0; i--) {
            DropTarget target = dropTargets.get(i);
            if (!target.isDropEnabled())
                continue;

            target.getHitRectRelativeToDragLayer(r);

            mDragObject.x = x;
            mDragObject.y = y;
            if (r.contains(x, y)) {

                dropCoordinates[0] = x;
                dropCoordinates[1] = y;
                mLauncher.getDragLayer().mapCoordInSelfToDescendent((View) target, dropCoordinates);

                return target;
            }
        }
        return null;
    }
    
    
    

    public void setDragScoller(DragScroller scroller) {
        mDragScroller = scroller;
    }

    public void setWindowToken(IBinder token) {
        mWindowToken = token;
    }

    /**
     * Sets the drag listner which will be notified when a drag starts or ends.
     */
    public void addDragListener(DragListener l) {
        mListeners.add(l);
    }

    /**
     * Remove a previously installed drag listener.
     */
    public void removeDragListener(DragListener l) {
        mListeners.remove(l);
    }

    /**
     * Add a DropTarget to the list of potential places to receive drop events.
     */
    public void addDropTarget(DropTarget target) {
        mDropTargets.add(target);
    }

    /**
     * Don't send drop events to <em>target</em> any more.
     */
    public void removeDropTarget(DropTarget target) {
        mDropTargets.remove(target);
    }

    /**
     * Sets the current fling-to-delete drop target.
     */
    public void setFlingToDeleteDropTarget(DropTarget target) {
        mFlingToDeleteDropTarget = target;
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * Set which view scrolls for touch events near the edge of the screen.
     */
    public void setScrollView(View v) {
        mScrollView = v;
    }

    DragView getDragView() {
        return mDragObject.dragView;
    }

    private class ScrollRunnable implements Runnable {
        private int mDirection;

        ScrollRunnable() {
        }

        public void run() {
            if (mDragScroller != null) {
                if (mDirection == SCROLL_LEFT) {
                    mDragScroller.scrollLeft();
                } else {
                    mDragScroller.scrollRight();
                }
                mScrollState = SCROLL_OUTSIDE_ZONE;
                mDistanceSinceScroll = 0;
                mDragScroller.onExitScrollArea();
                mLauncher.getDragLayer().onExitScrollArea();

                if (isDragging()) {
                    // Check the scroll again so that we can requeue the scroller if necessary
                    checkScrollState(mLastTouch[0], mLastTouch[1]);
                }
            }
        }

        void setDirection(int direction) {
            mDirection = direction;      
			 }
    }
}
