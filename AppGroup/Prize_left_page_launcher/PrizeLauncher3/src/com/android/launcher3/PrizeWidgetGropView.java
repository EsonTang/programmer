package com.android.launcher3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.GridLayout;

import com.android.prize.simple.ui.SimplePage;
import com.mediatek.launcher3.ext.LauncherLog;

public class PrizeWidgetGropView extends PagedView {

	private final int DEFAULT_PAGE = 3;

	private Context mCtx;

	private int mPages = DEFAULT_PAGE;

	private Launcher mLauncher;

	private List<String> mFilterWidgets;

	private ArrayList<Object> mWidgets;

	HashMap<String, List<Object>> mGroupWidgets;
	

    private int mWidgetCountX, mWidgetCountY;

	private LayoutInflater mLayoutInflater;

	public static String TAG = "PrizeWidgetGropView";

	public void setLauncher(Launcher mLauncher) {
		this.mLauncher = mLauncher;
	}

	@Override
	public void requestDisallowInterceptTouchEventByScrllLayout(
			boolean disallowIntercept) {
		// TODO Auto-generated method stub

	}

	public void onPackagesUpdated(ArrayList<Object> widgetsAndShortcuts) {

		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();

		// Get the list of widgets and shortcuts
		mWidgets.clear();
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

	}

	@Override
	public void syncPages() {

		for (int i = 0; i < mPages; i++) {

			if (i >= getChildCount()) {
				SimplePage page = new SimplePage(mCtx);
				// int color = R.color.black_87;
				// page.setBackgroundColor(mCtx.getResources().getColor(color));
				PagedView.LayoutParams params = new PagedView.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				addView(page, params);
			}
			// syncPageItems(i, false);
		}
	}

	public PrizeWidgetGropView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public PrizeWidgetGropView(Context context, AttributeSet attrs) {
		super(context, attrs);

        mLayoutInflater = LayoutInflater.from(context);
	}

	public PrizeWidgetGropView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void syncPageItems(int page, boolean immediate) {
		
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

	public String getObjectPackage(Object o) {
		if (o instanceof AppWidgetProviderInfo) {
			return ((AppWidgetProviderInfo) o).provider.getPackageName();
		} else {
			ResolveInfo info = (ResolveInfo) o;
			return info.activityInfo.packageName;
		}
	}
	private void groupItemInfoWithPkg(Collection<Object> infos,
			String tgtPkgName) {
		for (Object obj : infos) {
			String pkg = getObjectPackage(obj);
			if (mGroupWidgets.containsKey(pkg)) {
				List<Object> v = mGroupWidgets.get(pkg);
				v.add(obj);
			}
			mGroupWidgets.containsKey(pkg);
		}
	}

}
