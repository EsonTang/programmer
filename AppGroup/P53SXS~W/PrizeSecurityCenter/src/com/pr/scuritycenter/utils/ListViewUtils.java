package com.pr.scuritycenter.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 
 * @author wangzhong
 *
 */
public class ListViewUtils {

	/**
	 * To the ListView set height.
	 */
	public static void setHeight(ListView listView, int count) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int totalHeight = 0;
		if (listAdapter.getCount() <= count) {
			for (int i = 0; i < listAdapter.getCount(); i++) {
				View listItem = listAdapter.getView(i, null, listView);
				listItem.measure(0, 0);
				totalHeight += listItem.getMeasuredHeight();
			}
		} else {
			for (int i = 0; i < count; i++) {
				View listItem = listAdapter.getView(i, null, listView);
				listItem.measure(0, 0);
				totalHeight += listItem.getMeasuredHeight();
			}
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		//((MarginLayoutParams) params).setMargins(30, 10, 30, 10);
		listView.setLayoutParams(params);
	}

}
