/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.appcenter.ui.adapter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.prize.appcenter.R;

public class ScrollingTabsAdapter implements TabAdapter {
	private final Activity activity;
	private int arrayId;

	public ScrollingTabsAdapter(Activity act, int arrayId) {
		activity = act;
		this.arrayId = arrayId;
	}

	@Override
	public View getView(int position) {
		LayoutInflater inflater = activity.getLayoutInflater();
		final TextView tab = (TextView) inflater.inflate(R.layout.tabs, null);

		// Get default values for tab visibility preferences
		final String[] mTitles = activity.getResources().getStringArray(
				this.arrayId);
		// final String[] mTitles = activity.getResources().getStringArray(
		// R.array.apps_tab_titles);

		// Get tab visibility preferences
		// SharedPreferences sp = PreferenceManager
		// .getDefaultSharedPreferences(activity);
		Set<String> defaults = new HashSet<String>(Arrays.asList(mTitles));
		// Set<String> tabs_set = sp.getStringSet(TABS_ENABLED, defaults);
		// if its empty fill reset it to full defaults
		// stops app from crashing when no tabs are shown
		// TODO:rewrite activity to not crash when no tabs are chosen to show
		// or display error when no option is chosen

		// MultiSelectListPreference fails to preserve order of options chosen
		// Re-order based on order of default options array
		// This ensures titles are attached to correct tabs/pages
		String[] tabs_new = new String[defaults.size()];
		int cnt = 0;
		for (int i = 0; i < mTitles.length; i++) {
			if (defaults.contains(mTitles[i])) {
				tabs_new[cnt] = mTitles[i];
				cnt++;
			}
		}
		// Set the tab text
		if (position < tabs_new.length)
			tab.setText(tabs_new[position].toUpperCase());

		return tab;
	}
}
