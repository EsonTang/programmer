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
 * author:huangdianjun-floatwindow_manager-20151118
 */
package com.android.settings;

import com.android.settings.notification.NotificationAppList;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
/*prize-by-liuweiquan-20160525-start*/
import com.mediatek.common.prizeoption.PrizeOption;
/*prize-by-liuweiquan-20160525-end*/

/// add new menu to search db liup 20160622 start
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import java.util.List;
import android.content.Context;
import java.util.ArrayList;
/// add new menu to search db liup 20160622 end
import android.widget.ImageButton;
import java.util.Locale;
public class PrizeNotificationCentreActivity extends FragmentActivity implements
		ActionBar.TabListener ,Indexable,View.OnClickListener{///add Indexable liup 20160622

	private Fragment mNotificationAppList = new NotificationAppList();
	private Fragment mFloatWindowManager = new PrizeFloatWindowManager();

	private static final int TAB_INDEX_COUNT = 2;
	private static final int TAB_INDEX_ONE = 0;
	private static final int TAB_INDEX_TWO = 1;

	private ViewPager mViewPager;
	private ViewPagerAdapter mViewPagerAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prize_notification_centre_main);

		setUpActionBar();
		setUpViewPager();
		setUpTabs();
	}

	private void setUpActionBar() {
		final ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
	}

	private void setUpViewPager() {
		mViewPagerAdapter = new ViewPagerAdapter(getFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mViewPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

					@Override
					public void onPageSelected(int position) {
						final ActionBar actionBar = getActionBar();
						actionBar.setSelectedNavigationItem(position);
					}

					@Override
					public void onPageScrollStateChanged(int state) {
						switch (state) {
						case ViewPager.SCROLL_STATE_IDLE:
							// TODO
							break;
						case ViewPager.SCROLL_STATE_DRAGGING:
							// TODO
							break;
						case ViewPager.SCROLL_STATE_SETTLING:
							// TODO
							break;
						default:
							// TODO
							break;
						}
					}
				});
	}

	private void setUpTabs() {
		final ActionBar actionBar = getActionBar();
		String locale = Locale.getDefault().getLanguage();
		for (int i = 0; i < mViewPagerAdapter.getCount(); i++) {
			ActionBar.Tab tab = actionBar.newTab();
			if(i==0){
				tab.setCustomView(R.layout.prize_tab_noti);
				TextView title = (TextView) tab.getCustomView().findViewById(R.id.tab);
				title.setText(mViewPagerAdapter.getPageTitle(i));
				title.setTextColor(getResources().getColor(R.color.text_tab_selected));
				if(locale != null && locale.equals("en")){
					title.setTextSize(14);
				}
				// if(!PrizeOption.PRIZE_FLOAT_WINDOW_CONTROL){
					// RelativeLayout.LayoutParams rl=new RelativeLayout.LayoutParams(
							// RelativeLayout.LayoutParams.MATCH_PARENT,
							// RelativeLayout.LayoutParams.MATCH_PARENT);
					// rl.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
					// title.setGravity(Gravity.CENTER);
					// title.setLayoutParams(rl);
				// }	
				ImageButton home = (ImageButton) tab.getCustomView().findViewById(R.id.home);
				home.setOnClickListener(this);
			}else{
				tab.setCustomView(R.layout.prize_tab_dropzone);
				TextView title = (TextView) tab.getCustomView().findViewById(R.id.tab);
				title.setText(mViewPagerAdapter.getPageTitle(i));
				title.setTextColor(getResources().getColor(R.color.text_tab_unselected));
				if(locale != null && locale.equals("en")){
					title.setTextSize(14);
				}
			}
			tab.setTabListener(this);
			actionBar.addTab(tab);
		}
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public class ViewPagerAdapter extends FragmentPagerAdapter {

		public ViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// TODO Auto-generated method stub
			switch (position) {
			case TAB_INDEX_ONE:

				return mNotificationAppList;
			case TAB_INDEX_TWO:
				return mFloatWindowManager;

			}
			throw new IllegalStateException("No fragment at position "
					+ position);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			/*prize-by-liuweiquan-20160525-start*/
			if(!PrizeOption.PRIZE_FLOAT_WINDOW_CONTROL){
				return TAB_INDEX_COUNT-1;
			}
			/*prize-by-liuweiquan-20160525-end*/
			return TAB_INDEX_COUNT;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			String tabLabel = null;
			switch (position) {
			case TAB_INDEX_ONE:
				tabLabel = getResources().getString(
						R.string.prize_notification_manager_title);
				break;
			case TAB_INDEX_TWO:
				tabLabel = getResources().getString(
						R.string.prize_dropzone_manager_title);
				break;
			}
			return tabLabel;
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

		mViewPager.setCurrentItem(tab.getPosition());
		TextView title = (TextView) tab.getCustomView().findViewById(R.id.tab);
		title.setTextColor(getResources().getColor(R.color.text_tab_selected));
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		TextView title = (TextView) tab.getCustomView().findViewById(R.id.tab);
		title.setTextColor(getResources().getColor(R.color.text_tab_unselected));

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}
	/// add new menu to search db liup 20160622 start
	public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                final List<SearchIndexableRaw> indexables = new ArrayList<SearchIndexableRaw>();
               SearchIndexableRaw indexable = new SearchIndexableRaw(context);
                indexable.title = context.getString(R.string.prize_notification_manager_title);
                indexable.intentAction = "com.android.settings.NOTIFICATION_CENTRE";
                indexables.add(indexable);
                return indexables;
            }
        };
	/// add new menu to search db liup 20160622 end	
	
	 @Override
    public void onClick(View v) {
		finish();
    }
}
