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

package com.android.launcher3;

import java.util.ArrayList;

import android.animation.Animator;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.launcher3.PagedView.FLING_STATE;
import com.android.launcher3.notify.PreferencesManager;
import com.lqsoft.LqServiceUpdater.LqService;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.lqsoft.lqtheme.OLThemeChangeListener;
import com.lqsoft.lqtheme.OLThemeNotification;
import com.mediatek.launcher3.ext.LauncherLog;

public class Hotseat extends FrameLayout implements OLThemeChangeListener{
    private static final String TAG = "Hotseat";

    private CellLayout mContent;

    private Launcher mLauncher;

    private int mAllAppsButtonRank;

    private boolean mTransposeLayoutWithOrientation;
    private boolean mIsLandscape;

	private VelocityTracker  mVelocityTracker;
    protected int mFlingToShowSystemUIThresholdYVelocity = 1200;//add by zhouerlong
    protected int mFlingToShowSystemUIThresholdXVelocity = 3000;//add by zhouerlong
    protected FLING_STATE mFlingState= FLING_STATE.NONE;
    protected float mLastMotionY;

    protected int mTouchSlop;//使让桌面滑动起来的最低限度值

    protected static final int INVALID_POINTER = -1;
	private int mActivePointerId =INVALID_POINTER ;
    
	//add by zhouerlong

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //begin add by ouayngjin for lqtheme
        OLThemeNotification.registerThemeChange(this, this, null);
        //end add by ouayngjin for lqtheme
        Resources r = context.getResources();
        mTransposeLayoutWithOrientation = 
                r.getBoolean(R.bool.hotseat_transpose_layout_with_orientation);
        mIsLandscape = context.getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_LANDSCAPE;

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledPagingTouchSlop();
    }

    public void setup(Launcher launcher) {
        mLauncher = launcher;
        setOnKeyListener(new HotseatIconKeyEventListener());
    }

    CellLayout getLayout() {
        return mContent;
    }

    /**
     * Registers the specified listener on the cell layout of the hotseat.
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mContent.setOnLongClickListener(l);
    }
  
    private boolean hasVerticalHotseat() {
        return (mIsLandscape && mTransposeLayoutWithOrientation);
    }

    /* Get the orientation invariant order of the item in the hotseat for persistence. */
    int getOrderInHotseat(int x, int y) {
        return hasVerticalHotseat() ? (mContent.getCountY() - y - 1) : x;
    }
    /* Get the orientation specific coordinates given an invariant order in the hotseat. */
    int getCellXFromOrder(int rank) {
        return hasVerticalHotseat() ? 0 : rank;
    }
    int getCellYFromOrder(int rank) {
        return hasVerticalHotseat() ? (mContent.getCountY() - (rank + 1)) : 0;
    }
    public boolean isAllAppsButtonRank(int rank) {
        if (AppsCustomizePagedView.DISABLE_ALL_APPS) {
            return false;
        } else {
            return rank == mAllAppsButtonRank;
        }
    }

    /** This returns the coordinates of an app in a given cell, relative to the DragLayer */
    Rect getCellCoordinates(int cellX, int cellY) {
        Rect coords = new Rect();
        mContent.cellToRect(cellX, cellY, 1, 1, coords);
        int[] hotseatInParent = new int[2];
        Utilities.getDescendantCoordRelativeToParent(this, mLauncher.getDragLayer(),
                hotseatInParent, false);
        coords.offset(hotseatInParent[0], hotseatInParent[1]);

        // Center the icon
        int cWidth = mContent.getShortcutsAndWidgets().getCellContentWidth();
        int cHeight = mContent.getShortcutsAndWidgets().getCellContentHeight();
        int cellPaddingX = (int) Math.max(0, ((coords.width() - cWidth) / 2f));
        int cellPaddingY = (int) Math.max(0, ((coords.height() - cHeight) / 2f));
        coords.offset(cellPaddingX, cellPaddingY);

        return coords;
    }


    public static  enum HotseatDragState {NONE, DRAG_IN, DRAG_OUT};
    private HotseatDragState mDragState = HotseatDragState.NONE;
    private View mDragItemView;
    
    /**
     * @param child
     * 进入hotseat均分入口
     */
    public void OnEnterHotseat(View child) {
//    	reLayout();
    	mDragItemView = child;
    	if (mDragState != HotseatDragState.DRAG_IN) {
        	mDragState = HotseatDragState.DRAG_IN;
    	mContent.reverCellLayout(CellLayout.MODE_DRAG_OVER, HotseatDragState.DRAG_IN,child,this);
    	}
    }
    
    /**
     * @param dragItemView
     * 退出hotseat 平分hotseat
     */
    public void OnExitHotseat(View dragItemView) {
    	
    	if (mDragState != HotseatDragState.DRAG_OUT) {
        	mDragState = HotseatDragState.DRAG_OUT;
        	mContent.reverCellLayout(CellLayout.MODE_DRAG_OVER, HotseatDragState.DRAG_OUT,dragItemView,this);
    	}
    }
    
    
    
    public void reLayoutContent() {
    	OnExitHotseat(null);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();

        mAllAppsButtonRank = grid.hotseatAllAppsRank;
        mContent = (CellLayout) findViewById(R.id.layout);
        if (grid.isLandscape && !grid.isLargeTablet()) {
            mContent.setGridSize(1, (int) grid.numHotseatIcons);
        } else {
            mContent.setGridSize((int) grid.numHotseatIcons, 1);
        }
        mContent.setIsHotseat(true);

        resetLayout();
        
    }

	/**
	 * 中心设定布局
	 * 
	 * @param hotseat
	 * @param solution
	 * @param visibleCount
	 * @param list
	 */
	public void reLayout() {
		int visibleCount = mContent.getShortcutsAndWidgets().getChildCount();
		if (visibleCount > 0) {

			int width = this.getRight() - this.getLeft();
			width = width - getPaddingLeft() - getPaddingRight();

			int wGap = 0;
			int cellW = mContent.getCellWidth();
			int cellH = mContent.getCellHeight();
			// int wGap = 0;
			LauncherAppState app = LauncherAppState.getInstance();
			DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
			int workspaceCountX = (int) grid.numColumns;
			if (visibleCount > workspaceCountX) {
				visibleCount = workspaceCountX;
			}
			int space = (int) (width - visibleCount * cellW);
			if (visibleCount > workspaceCountX) {
				wGap = (int) (space / (float) (visibleCount - 1));
			} else {
				wGap = (int) (space / (float) (visibleCount + 1));
			}
			Log.i("zhouerlong", "wGap::::" + wGap);
			mContent.getShortcutsAndWidgets().setCellDimensions(
					mContent.getCellWidth(), mContent.getCellHeight(), wGap, 0,
					visibleCount, 1);
			mContent.getShortcutsAndWidgets().requestLayout();
		}
	}
    void resetLayout() {
        mContent.removeAllViewsInLayout();

        if (!AppsCustomizePagedView.DISABLE_ALL_APPS) {
            // Add the Apps button
            Context context = getContext();

            LayoutInflater inflater = LayoutInflater.from(context);
            TextView allAppsButton = (TextView)
                    inflater.inflate(R.layout.all_apps_button, mContent, false);

            Drawable d = context.getResources().getDrawable(R.drawable.all_apps_button_icon_ot);
            
            String theme = PreferencesManager.getCurrentTheme(mContext);
            
            
            Utilities.resizeIconDrawable(d);
            allAppsButton.setCompoundDrawables(null, d, null, null);

            allAppsButton.setContentDescription(context.getString(R.string.all_apps_button_label));
            if (mLauncher != null) {
                allAppsButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());
            }
            allAppsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    Trace.traceBegin(Trace.TRACE_TAG_INPUT, "onClick");
                    if (LauncherLog.DEBUG) {
                        LauncherLog.d(TAG, "Click on all apps view on hotseat: mLauncher = " + mLauncher);
                    }
                    if (mLauncher != null) {
                        mLauncher.onClickAllAppsButton(v);
                    }
                    Trace.traceEnd(Trace.TRACE_TAG_INPUT);
                }
            });

            // Note: We do this to ensure that the hotseat is always laid out in the orientation of
            // the hotseat in order regardless of which orientation they were added
            int x = getCellXFromOrder(mAllAppsButtonRank);
            int y = getCellYFromOrder(mAllAppsButtonRank);
            CellLayout.LayoutParams lp = new CellLayout.LayoutParams(x,y,1,1);
            lp.canReorder = false;
            mContent.addViewToCellLayout(allAppsButton, -1, 0, lp, true);
        }
    }
    


	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();//读取速率
        }
        mVelocityTracker.addMovement(ev);//增加MoventEvent
    }
    /*
     * Flinging to delete - IN PROGRESS
     */
	private FLING_STATE isFlingingToShowSysteUI() {
		ViewConfiguration config = ViewConfiguration.get(getContext());
		mVelocityTracker.computeCurrentVelocity(500,
				config.getScaledMaximumFlingVelocity());//add by zhouerlong
		float velocityTracker = mVelocityTracker.getYVelocity();
		float velocitxTracker = mVelocityTracker.getXVelocity();
		if (velocityTracker > mFlingToShowSystemUIThresholdYVelocity
				&& velocitxTracker < mFlingToShowSystemUIThresholdXVelocity
				&& velocitxTracker > -mFlingToShowSystemUIThresholdXVelocity) {
			return FLING_STATE.DOWN;
		} else if (velocityTracker < -mFlingToShowSystemUIThresholdYVelocity
				&& velocitxTracker < mFlingToShowSystemUIThresholdXVelocity
				&& velocitxTracker > -mFlingToShowSystemUIThresholdXVelocity) {
			return FLING_STATE.UP;
		} else {
			return FLING_STATE.NONE;
		}
		// add by zhouerlong
	}

	protected void determineScrollingStart(MotionEvent ev) {

		// Disallow scrolling if we don't have a valid pointer index
		final int pointerIndex = ev.findPointerIndex(mActivePointerId);
		if (pointerIndex == -1) {
			if(LauncherLog.DEBUG) {
				LauncherLog.d(TAG, "determineScrollingStart pointerIndex == -1.");
			}
			return;
		}

		// Disallow scrolling if we started the gesture from outside the
		// viewport
		final float x = ev.getX(pointerIndex);
		final float y = ev.getY(pointerIndex);

		final int yDiff = (int) Math.abs(y - mLastMotionY);// 如果在y轴滑动的距离大于down下的距离
															// 表示启动滑动状态

		final int touchSlop = Math.round(1.0f * mTouchSlop); // 读取滑动距离最小值
																		// 超出此范围表示需要滑动
		boolean yMoved = yDiff > touchSlop / 8;// 判断Y轴是否触发滑动
		if (yMoved) {
			

			FLING_STATE state = isFlingingToShowSysteUI();
			if (state == FLING_STATE.DOWN) {
				mFlingState = FLING_STATE.DOWN;
			} else if (state == FLING_STATE.UP) {
				if(!mLauncher.getWorkspace().isInSpringLoadMoed()
						&& mLauncher.getworkspace().isReleaseSearchModel()
						&& !isInDragModed()&& Launcher.isSupportT9Search) {
					mFlingState = FLING_STATE.UP;
				}
			} else {

				mLastMotionY = y;
			}

		}
	}
	 public void closeSearchView() {
	    	mFlingState = FLING_STATE.DOWN;
	    }
    public boolean isEnterSearchModel() {
    	return mFlingState == FLING_STATE.UP;
    }
    public boolean isInDragModed() {

    	return mLauncher.getworkspace().isInDragModed();
    }
	public void extEnterSearchModel() {
		mLauncher.getSearchView().closeSearchView();
	}
    /* (non-Javadoc)
     * @see android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
     * 快速上下滑动 目前已经关闭
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

		acquireVelocityTrackerAndAddMovement(ev);
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionY = ev.getY();

			mFlingState = FLING_STATE.NONE;
			mActivePointerId = ev.getPointerId(0);
			break;
		case MotionEvent.ACTION_MOVE:
			if (mActivePointerId != INVALID_POINTER) {
				determineScrollingStart(ev);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mFlingState == FLING_STATE.UP) {

				if (!mLauncher.getWorkspace().isInSpringLoadMoed()
						&& mLauncher.getworkspace().isReleaseSearchModel()
						&& !isInDragModed()&& Launcher.isSupportT9Search) {
					boolean up = (mFlingState == FLING_STATE.UP) ? true : false;
					Animator a = mLauncher.setupSearchViewAnimation(
							mFlingState, false);
					a.start();
				}
			}
			
			break;

        case MotionEvent.ACTION_POINTER_UP:
            releaseVelocityTracker();
            break;
		default:
			break;
		}
        // We don't want any clicks to go through to the hotseat unless the workspace is in
        // the normal state.
        if (mLauncher.getWorkspace().isSmall()) {
            return true;
        }
        return false;
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    void addAllAppsFolder(IconCache iconCache,
            ArrayList<AppInfo> allApps, ArrayList<ComponentName> onWorkspace,
            Launcher launcher, Workspace workspace) {
        if (AppsCustomizePagedView.DISABLE_ALL_APPS) {
            FolderInfo fi = new FolderInfo();

            fi.cellX = getCellXFromOrder(mAllAppsButtonRank);
            fi.cellY = getCellYFromOrder(mAllAppsButtonRank);
            fi.spanX = 1;
            fi.spanY = 1;
            fi.container = LauncherSettings.Favorites.CONTAINER_HOTSEAT;
            fi.screenId = mAllAppsButtonRank;
            fi.itemType = LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
            fi.title = "More Apps";
            LauncherModel.addItemToDatabase(launcher, fi, fi.container, fi.screenId, fi.cellX,
                    fi.cellY, false);
            FolderIcon folder = FolderIcon.fromXml(R.layout.folder_icon, launcher,
                    getLayout(), fi, iconCache);
            workspace.addInScreen(folder, fi.container, fi.screenId, fi.cellX, fi.cellY,
                    fi.spanX, fi.spanY);

            for (AppInfo info: allApps) {
                ComponentName cn = info.intent.getComponent();
                if (!onWorkspace.contains(cn)) {
                    Log.d(TAG, "Adding to 'more apps': " + info.intent);
                    ShortcutInfo si = info.makeShortcut();
                    fi.add(si);
                }
            }
        }
    }

    //begin add by ouyangjin for lqtheme
	@Override
	public void onThemeChange() {/*
		if(allAppsButton==null){
			return;
		}
		final Bitmap bitmap = getLqThemeHotseatApp();
		if(bitmap != null){
			post(new Runnable() {
				@Override
				public void run() {
					allAppsButton.setCompoundDrawables(null, new FastBitmapDrawable(bitmap), null, null);
				}
			});
			
		}
	*/}
	private Bitmap getLqThemeHotseatApp() {
		ComponentName componentName = new ComponentName("","allapp");
		Bitmap bitmap=null;
		if (LqShredPreferences.isLqtheme(getContext())) {
			 bitmap = LqService.getInstance().getIcon(componentName, null, true, "");
		}
		return bitmap;
	}
	public void finalize() throws Throwable {
		super.finalize();
		OLThemeNotification.unRegisterThemeChange(this, this);
	}
	//begin add by ouyangjin for lqtheme
    void addAppsToAllAppsFolder(ArrayList<AppInfo> apps) {
        if (AppsCustomizePagedView.DISABLE_ALL_APPS) {
            View v = mContent.getChildAt(getCellXFromOrder(mAllAppsButtonRank), getCellYFromOrder(mAllAppsButtonRank));
            FolderIcon fi = null;

            if (v instanceof FolderIcon) {
                fi = (FolderIcon) v;
            } else {
                return;
            }

            FolderInfo info = fi.getFolderInfo();
            for (AppInfo a: apps) {
                ShortcutInfo si = a.makeShortcut();
                info.add(si);
            }
        }
    }
}
