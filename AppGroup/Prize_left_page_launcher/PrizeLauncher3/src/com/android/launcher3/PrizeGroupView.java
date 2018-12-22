package com.android.launcher3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.android.launcher3.Launcher.SpringState;
import com.mediatek.launcher3.ext.LauncherLog;

import android.animation.AnimatorSet;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;

public class PrizeGroupView extends AppsCustomizePagedView {

	public PrizeGroupView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	HashMap<String, List<Object>> mGroupWidgets;
	
	HashMap<String, View> mGroupViews;

	private State mState;
	
	private String mPkg;

	
	
	public boolean showGroupList() {
		return mState==State.ENTER;
	}
	
	
	public void notifyMenuView() {
		
		generateGrouItem();
		resetDataIsReady();
		if(mGroupViews==null) {
			return;
		}
       Iterator<View> it = mGroupViews.values().iterator();
        while (it.hasNext()) {
        	AppsCustomizePagedView t = (AppsCustomizePagedView) it.next();
        	t.resetDataIsReady();
			
		}
	}
	
	
	public void release() {
		removeAllViews();
			Iterator<Entry<String, View>> it = mGroupViews.entrySet().iterator();
		while (it.hasNext()) {
			try {
				Entry<String, View> c = it.next();
				View v = c.getValue();
				ViewGroup p = (ViewGroup) v.getParent();
				p.removeView(v);
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
		mGroupViews.clear();
	}

	public void onPackagesUpdated(ArrayList<Object> widgetsAndShortcuts) {

		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();

		// Get the list of widgets and shortcuts


		ViewGroup vp = (ViewGroup) this.getParent();
		if(mGroupViews!=null) {
			for(View v :mGroupViews.values()) {
				
				if(vp.indexOfChild(v)!=-1) {
					vp.removeView(v);
				}
			}
		}
		if(mLauncher.getSpringState()==SpringState.WIDGET ) {
			this.setVisibility(View.VISIBLE);
			this.bringToFront();
		}
		mWidgets.clear();
		if(mGroupWidgets!=null)
		mGroupWidgets.clear();
		if(mGroupViews!=null)
		mGroupViews.clear();
		/*ViewGroup vp = (ViewGroup) this.getParent();
		int count=((ViewGroup)this.getParent()).getChildCount();
		for(int i=0;i<count;i++) {
			View child = vp.getChildAt(i);
			if(child instanceof AppsCustomizePagedView) {
				vp.removeView(child);
			}
		}*/

		mLayoutInflater = LayoutInflater.from(mLauncher);

		if (LauncherLog.DEBUG) {
			LauncherLog.d(TAG, "updatePackages: widgetsAndShortcuts size = "
					+ widgetsAndShortcuts.size());
		}
		for (Object o : widgetsAndShortcuts) {
			if (o instanceof AppWidgetProviderInfo) {
				AppWidgetProviderInfo widget = (AppWidgetProviderInfo) o;
				/*
				 * if (!app.shouldShowAppOrWidgetProvider(widget.provider)) {
				 * continue; }
				 */
				widget.label = widget.label.trim();
				if (widget.minWidth > 0 && widget.minHeight > 0) {
					// Ensure that all widgets we show can be added on a
					// workspace of this size
					int[] spanXY = Launcher.getSpanForWidget(mLauncher, widget);
					int[] minSpanXY = Launcher.getMinSpanForWidget(mLauncher,
							widget);
					int minSpanX = Math.min(spanXY[0], minSpanXY[0]);
					int minSpanY = Math.min(spanXY[1], minSpanXY[1]);
					if (minSpanX <= (int) grid.numColumns
							&& minSpanY <= (int) grid.numRows) {

						if (mFilterWidgets == null) {
							mFilterWidgets = Utilities.getFilterWidgets();
						}

						if (widget.provider != null && mFilterWidgets != null) {
							if (!mFilterWidgets.contains(widget.provider
									.getPackageName())) {
								mWidgets.add(widget);
							}
						} else {

							mWidgets.add(widget);
						}

					} else {
						Log.e(TAG, "Widget " + widget.provider
								+ " can not fit on this device ("
								+ widget.minWidth + ", " + widget.minHeight
								+ "), min span is (" + minSpanX + ", "
								+ minSpanY + ")" + "), span is (" + spanXY[0]
								+ ", " + spanXY[1] + ")");
					}
				} else {
					LauncherLog.e(TAG, "Widget " + widget.provider
							+ " has invalid dimensions (" + widget.minWidth
							+ ", " + widget.minHeight);
				}
			} else {
				// just add shortcuts

				String pkg = WidgetPreviewLoader.getObjectPackage(o);

				if (mFilterWidgets == null) {
					mFilterWidgets = Utilities.getFilterWidgets();
				}

				if (mFilterWidgets != null) {
					if (!mFilterWidgets.contains(pkg)) {
						mWidgets.add(o);
					}
				} else {

					mWidgets.add(o);
				}
			}
		}
		mGroupWidgets = getGroupPkgs(mWidgets);
		toGroup(mWidgets);
		generateGrouItem();
		

        resetDataIsReady();
        updatePageCountsAndInvalidateData();

	}

	public void generateGrouItem() {
		mWidgets.clear();
		Collection<List<Object>> gps = mGroupWidgets.values();
		for (List<Object> gp : gps) {
			mWidgets.add(gp.get(0));
			if (gp.size() > 1) {
				addGroupChildView(getObjectPackage(gp.get(0)));
			}
		}
	}
	
	
	
	/**@see 加载当前页面的weigets
     * @param page
     * @param immediate
     */
    public void syncWidgetPageItems(final int page, final boolean immediate) {
        int numItemsPerPage = mWidgetCountX * mWidgetCountY;

        // Calculate the dimensions of each cell we are giving to each widget
        final ArrayList<Object> items = new ArrayList<Object>();
        int contentWidth = mContentWidth;
        final int cellWidth = ((contentWidth - mPageLayoutPaddingLeft - mPageLayoutPaddingRight
                - ((mWidgetCountX - 1) * mWidgetWidthGap)) / mWidgetCountX);
        int contentHeight = mContentHeight;
        final int cellHeight = ((contentHeight - mPageLayoutPaddingTop - mPageLayoutPaddingBottom
                - ((mWidgetCountY - 1) * mWidgetHeightGap)) / mWidgetCountY);

        // Prepare the set of widgets to load previews for in the background
        int offset = page * numItemsPerPage;
        for (int i = offset; i < Math.min(offset + numItemsPerPage, mWidgets.size()); ++i) {
            items.add(mWidgets.get(i));
        }
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "syncWidgetPageItems: page = " + page + ", immediate = " + immediate
                    + ", numItemsPerPage = " + numItemsPerPage
                    + ", contentWidth = " + contentWidth + ", cellWidth = " + cellWidth
                    + ", contentHeight = " + contentHeight + ", cellHeight = " + cellHeight
                    + ", offset = " + offset + ", this = " + this);
        }

        // Prepopulate the pages with the other widget info, and fill in the previews later
        final PagedViewGridLayout layout = (PagedViewGridLayout) getPageAt(page);
        layout.setColumnCount(layout.getCellCountX());
        LauncherLog.d(TAG, "syncWidgetPageItems: cell count x = " + layout.getCellCountX() 
                    + ", layout column count = " + layout.getColumnCount());
        for (int i = 0; i < items.size(); ++i) {
            Object rawInfo = items.get(i);
            PendingAddItemInfo createItemInfo = null;
            PagedViewWidget widget=null;
                 widget = (PagedViewWidget) mLayoutInflater.inflate(
                        R.layout.apps_customize_widget, null);
                 

                 
                
            if (rawInfo instanceof AppWidgetProviderInfo) {
                // Fill in the widget information
                AppWidgetProviderInfo info = (AppWidgetProviderInfo) rawInfo;
                createItemInfo = new PendingAddWidgetInfo(info, null, null);

                // Determine the widget spans and min resize spans.
                int[] spanXY = Launcher.getSpanForWidget(mLauncher, info);
                createItemInfo.spanX = spanXY[0];
                createItemInfo.spanY = spanXY[1];
                int[] minSpanXY = Launcher.getMinSpanForWidget(mLauncher, info);
                createItemInfo.minSpanX = minSpanXY[0];
                createItemInfo.minSpanY = minSpanXY[1]; 
                if(mGroupWidgets.containsKey(createItemInfo.componentName.getPackageName())) {
                 	String pkg = createItemInfo.componentName.getPackageName();
                 	if(mGroupWidgets.get(pkg).size()>1) {
                 		createItemInfo.isGroup=true;
                 		widget.isGroup=true;
                 		createItemInfo.groupSize=mGroupWidgets.get(pkg).size();
                 		widget.groupSize=createItemInfo.groupSize;
                 	}
                 }
                
                widget.applyFromAppWidgetProviderInfo(info, -1, spanXY, mWidgetPreviewLoader);
                widget.setTag(createItemInfo);
                widget.setShortPressListener(this);
            } else if (rawInfo instanceof ResolveInfo) {
                // Fill in the shortcuts information
                ResolveInfo info = (ResolveInfo) rawInfo;
                createItemInfo = new PendingAddShortcutInfo(info.activityInfo);
                createItemInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
                createItemInfo.componentName = new ComponentName(info.activityInfo.packageName,
                        info.activityInfo.name); 
                if(mGroupWidgets.containsKey(createItemInfo.componentName.getPackageName())) {
                         	String pkg = createItemInfo.componentName.getPackageName();
                         	if(mGroupWidgets.get(pkg).size()>1) {
                         		createItemInfo.isGroup=true;
                         		widget.isGroup=true;
                         		createItemInfo.groupSize=mGroupWidgets.get(pkg).size();
                         		widget.groupSize=createItemInfo.groupSize;
                         	}
                         }
                widget.applyFromResolveInfo(mPackageManager, info, mWidgetPreviewLoader);
                widget.setTag(createItemInfo);
            }
            widget.setOnClickListener(this);
            widget.setOnLongClickListener(this);
            widget.setOnTouchListener(this);
            widget.setOnKeyListener(this);

            // Layout each widget
            int ix = i % mWidgetCountX;
            int iy = i / mWidgetCountX;
            LauncherLog.d(TAG, "syncWidgetPageItems: i = " + i + 
                        ", ix = " + ix + ", iy = " + iy + ", mWidgetCountX = " + mWidgetCountX);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(iy, GridLayout.START),
                    GridLayout.spec(ix, GridLayout.TOP));
            lp.width = cellWidth;
            lp.height = cellHeight;
            lp.setGravity(Gravity.TOP | Gravity.START);
            if (ix > 0) lp.leftMargin = 0;
            if (iy > 0) lp.topMargin = mWidgetHeightGap;
            layout.addView(widget, lp);
        }

        // wait until a call on onLayout to start loading, because
        // PagedViewWidget.getPreviewSize() will return 0 if it hasn't been laid out
        // TODO: can we do a measure/layout immediately?
        layout.setOnLayoutListener(new Runnable() {
            public void run() {
                // Load the widget previews
                int maxPreviewWidth = cellWidth;
                int maxPreviewHeight = cellHeight;
                if (layout.getChildCount() > 0) {
                    PagedViewWidget w = (PagedViewWidget) layout.getChildAt(0);
                    int[] maxSize = w.getPreviewSize();
                    maxPreviewWidth = maxSize[0];
                    maxPreviewHeight = maxSize[1];
                    if ((maxPreviewWidth <= 0) || (maxPreviewHeight <= 0)) {
                        if (LauncherLog.DEBUG) {
                            LauncherLog.d(TAG, "syncWidgetPageItems: maxPreviewWidth = " + maxPreviewWidth
                                + ", maxPreviewHeight = " + maxPreviewHeight);
                        }
                    }
                }

                mWidgetPreviewLoader.setPreviewSize(
                        maxPreviewWidth, maxPreviewHeight, mWidgetSpacingLayout);
                if (immediate) {
                    AsyncTaskPageData data = new AsyncTaskPageData(page, items,
                            maxPreviewWidth, maxPreviewHeight, null, null, mWidgetPreviewLoader);
                    loadWidgetPreviewsInBackground(null, data);
                    onSyncWidgetPageItems(data);
                } else {
                    if (mInTransition) {
                        mDeferredPrepareLoadWidgetPreviewsTasks.add(this);
                    } else {
                        prepareLoadWidgetPreviewsTask(page, items,
                                maxPreviewWidth, maxPreviewHeight, mWidgetCountX);
                    }
                }
                layout.setOnLayoutListener(null);
            }
        });
    }

	public String getObjectPackage(Object o) {
		if (o instanceof AppWidgetProviderInfo) {
			return ((AppWidgetProviderInfo) o).provider.getPackageName();
		} else {
			ResolveInfo info = (ResolveInfo) o;
			return info.activityInfo.packageName;
		}
	}

	private HashMap<String, List<Object>> getGroupPkgs(List<Object> infos) {

		HashMap<String, List<Object>> groups = new HashMap<>();
		for (Object obj : infos) {
			String pkg = getObjectPackage(obj);
			if (!groups.containsKey(pkg)) {
				List<Object> objs = new ArrayList<>();
				groups.put(pkg, objs);
			}
		}
		return groups;
	}
	
	private void addGroupChildView(String pkg) {
		
		AppsCustomizePagedView child = (AppsCustomizePagedView) mLayoutInflater
				.inflate(R.layout.widget_group_page_view,
						null);
		child.setup(mLauncher, mLauncher.getDragController());

		child
				.setContentType(AppsCustomizePagedView.ContentType.Widgets);
		ViewGroup p = (ViewGroup) this.getParent();
		p.addView(child, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		if(mGroupViews == null) {
			mGroupViews = new HashMap<>();
		}
		if(!mGroupViews.containsKey(child)) {
			mGroupViews.put(pkg, child);
		}
		child.onPackagesUpdated((ArrayList<Object>) mGroupWidgets.get(pkg));
		Log.i("zzz", "");
	}

	@Override
	public void onClick(View v) {

		PendingAddItemInfo item =(PendingAddItemInfo) v.getTag();
		if(item.isGroup) {
			toGroupViewAnim(State.ENTER,item.componentName.getPackageName());
		}else {
			super.onClick(v);
		}
	}
	

	public enum State {
		NONE, ENTER,BACK// add by
																		// zel
	};// M by zhouerlong
	public void toGroupViewAnim(State state,String pkg) {
		mState = state;
		if(pkg!=null) {
			mPkg = pkg;
		}
		PagedView toView = (AppsCustomizePagedView) (state==State.ENTER?mGroupViews.get(mPkg):this);
		PagedView  fromView= (AppsCustomizePagedView) (state==State.BACK?mGroupViews.get(mPkg):this);
		// bugid 18511
		if(toView!=null && fromView!=null){
			toView.bringToFront();
			if (state == State.ENTER) {
				toView.show();
				fromView.back();

			}else {
				toView.show();
				if(fromView!=null){
					fromView.back(); 
				}
			}
			toView.setVisibility(View.VISIBLE);
		}
	}

	private void toGroup(Collection<Object> infos) {
		for (Object obj : infos) {
			String pkg = getObjectPackage(obj);
			if (mGroupWidgets.containsKey(pkg)) {
				List<Object> v = mGroupWidgets.get(pkg);
				if (!v.contains(obj)) {
					v.add(obj);
				}
			}
		}
	}

}
