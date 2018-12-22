/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.android.launcher3.DragLayer.TouchCompleteListener;
import com.android.launcher3.Launcher.IconChangeState;
import com.android.launcher3.nifty.IconUninstallIndicatorAnim;
import com.android.launcher3.nifty.NiftyObserables;
import com.android.launcher3.nifty.NiftyObservers;
import com.mediatek.launcher3.ext.LauncherLog;

/**
 * {@inheritDoc}
 */
public class LauncherAppWidgetHostView extends AppWidgetHostView implements TouchCompleteListener ,NiftyObservers{
    private final static String TAG = "LauncherAppWidgetHostView";

    private CheckLongPressHelper mLongPressHelper;
    private LayoutInflater mInflater;
    private Context mContext;
    private int mPreviousOrientation;
    private DragLayer mDragLayer;

	protected  NiftyObserables mObservable;
	public void setObservable(NiftyObserables obs) {
		mObservable = obs;
	}
    public LauncherAppWidgetHostView(Context context) {
        super(context);
        mContext = context;
        mLongPressHelper = new CheckLongPressHelper(this);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDragLayer = ((Launcher) context).getDragLayer();
        
    }
    
    public void registerObserver() {
    	mObservable.registerObserver(this);
    }

    @Override
    protected View getErrorView() {
        return mInflater.inflate(R.layout.appwidget_error, this, false);
    }

    @Override
    public void updateAppWidget(RemoteViews remoteViews) {
        // Store the orientation in which the widget was inflated
        mPreviousOrientation = mContext.getResources().getConfiguration().orientation;
        super.updateAppWidget(remoteViews);
    }

    public boolean orientationChangedSincedInflation() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (mPreviousOrientation != orientation) {
           return true;
       }
       return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d(TAG, "onInterceptTouchEvent: ev = " + ev);
        }

        // Consume any touch events for ourselves after longpress is triggered
        if (mLongPressHelper.hasPerformedLongPress()) {
            mLongPressHelper.cancelLongPress();
            return true;
        }
        
        

        // Remove the widget from the workspace
     
		
    
        
        
        

        // Watch for longpress events at this level to make sure
        // users can always pick up this widget
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLongPressHelper.postCheckForLongPress();
                mDragLayer.setTouchCompleteListener(this);
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLongPressHelper.cancelLongPress();
                ItemInfo item = (ItemInfo) this.getTag();
        		Launcher launcher = (Launcher) this.getContext();
        		if (launcher.getworkspace().isInDragModed()) {
        			launcher.getworkspace().setDoing(true);
        			if (launcher.getIconState() == IconChangeState.DEL) {
        				launcher.explosition(this,null);
        				launcher.removeAppWidget((LauncherAppWidgetInfo) item);
        				LauncherModel.deleteItemFromDatabase(launcher, item);

        				CellLayout cell =(CellLayout) this.getParent().getParent();
        				cell.removeView(this);
        				final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) item;
        				final LauncherAppWidgetHost appWidgetHost = launcher
        						.getAppWidgetHost();

        				if (appWidgetHost != null) {
        					// Deleting an app widget ID is a void call but writes to
        					// disk before returning
        					// to the caller...
        					new Thread("deleteAppWidgetId") {
        						public void run() {
        							appWidgetHost
        									.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
        						}
        					}.start();
        				}
        			}
        		}
                break;
        }

        // Otherwise continue letting touch events fall through to children
        return false;
    }
   
    
    
    public boolean onTouchEvent(MotionEvent ev) {
        if (LauncherLog.DEBUG_MOTION) {
            LauncherLog.d(TAG, "onTouchEvent: ev = " + ev);
        }
        // If the widget does not handle touch, then cancel
        // long press when we release the touch
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLongPressHelper.cancelLongPress();
                break;
        }
        return false;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mLongPressHelper.cancelLongPress();
    }

    @Override
    public void onTouchComplete() {
        mLongPressHelper.cancelLongPress();
    }

    @Override
    public int getDescendantFocusability() {
        return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
    }

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		Launcher launcher = null;
		if (this.getContext() instanceof Launcher) {
			launcher = (Launcher) this.getContext();

		}
		
		if (launcher != null) {

			if (launcher.getSpringState() != Launcher.SpringState.BATCH_EDIT_APPS) {

				Drawable d = this.getContext().getDrawable(
						R.drawable.ic_launcher_delete_holo);
				int w = d.getIntrinsicWidth();
				int h = d.getIntrinsicHeight();
					DrawEditIcons.drawStateIcon(canvas, this,
							R.drawable.ic_launcher_delete_holo, w, h,
							anim.getOuterRingSize());

				 
			}
		
		}
	}

    IconUninstallIndicatorAnim anim = new IconUninstallIndicatorAnim();
	@Override
	public void onChanged(StateInfo p) {
		this.requestLayout();

		Launcher launcher = null;
		if (this.getContext() instanceof Launcher) {
			launcher = (Launcher) this.getContext();

		}
		if (launcher.getworkspace().isInDragModed()&p.state) {

			if (launcher.getIconState() == IconChangeState.EDIT) {

				anim.animateToIconIndcatorDraw(this,1,0f,p);
			}else {

				anim.animateToIconIndcatorDraw(this,0,1f,p);
			}
		}else if (!p.state){
			anim.animateToIconIndcatorDraw(this,1f,0f,p);
		}else if (p.lasted){
			launcher.getworkspace().OnSpringFinish(true);
		}
		
	
	
		
	}



}
