package com.prize.permissionmanage;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.util.Log;
import android.Manifest;
import java.util.ArrayList;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Parcelable;

public class LocalActivity extends FragmentActivity implements
			ActionBar.TabListener {
	
		private Fragment mPrizeAppPermissionFragment = new PrizeAppPermissionFragment();
		private Fragment mPrizePermissionFragment = new PrizePermissionFragment();
        private final String TAG = "LocalActivity";
		
		private static final int TAB_INDEX_COUNT = 2;
		private static final int TAB_INDEX_ONE = 0;
		private static final int TAB_INDEX_TWO = 1;
	
		private ViewPager mViewPager;
		private ViewPagerAdapter mViewPagerAdapter;
	
		/** Called when the activity is first created. */
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.prize_permission_control);
			setUpActionBar();
			setUpViewPager();
			setUpTabs();
		}
	
		private void setUpActionBar() {
			final ActionBar actionBar = getActionBar();
			if(actionBar != null){
    			actionBar.setHomeButtonEnabled(false);
    			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    			actionBar.setDisplayShowTitleEnabled(false);
    			actionBar.setDisplayShowHomeEnabled(false);
			}else{
				Log.w(TAG,"setUpActionBar actionBar = null");
			}
		}
	
		private void setUpViewPager() {
			mViewPagerAdapter = new ViewPagerAdapter(getFragmentManager());
	
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mViewPagerAdapter);
			mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {
					final ActionBar actionBar = getActionBar();
					if(actionBar != null){
						actionBar.setSelectedNavigationItem(position);
					}else{
						Log.w(TAG,"onPageSelected actionBar = null");
					}
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
			if(actionBar != null){
				for (int i = 0; i < mViewPagerAdapter.getCount(); i++) {
					ActionBar.Tab tab = actionBar.newTab();
					if(i==0){
						tab.setCustomView(R.layout.prize_permission_tab_one);
						TextView title = (TextView) tab.getCustomView().findViewById(R.id.tab);
						title.setText(mViewPagerAdapter.getPageTitle(i));
						title.setTextColor(getResources().getColor(R.color.prize_text_tab_selected));
						ImageButton imageButton = (ImageButton) tab.getCustomView().findViewById(R.id.home);
						imageButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								finish();
							}
						});
					}else{
						tab.setCustomView(R.layout.prize_permission_tab_two);
						TextView title = (TextView) tab.getCustomView().findViewById(R.id.tab);
						title.setText(mViewPagerAdapter.getPageTitle(i));
						title.setTextColor(getResources().getColor(R.color.prize_text_tab_unselected));
					}
					tab.setTabListener(this);
					actionBar.addTab(tab);
				}
			}else{
				Log.w(TAG,"setUpTabs actionBar = null");
			}
	
		}
	
		@Override
		protected void onDestroy() {
			super.onDestroy();
		}
	
		public class ViewPagerAdapter extends FragmentStatePagerAdapter {
	
			public ViewPagerAdapter(FragmentManager fm) {
				super(fm);
			}
	
			@Override
			public Fragment getItem(int position) {
				// TODO Auto-generated method stub
				switch (position) {
					case TAB_INDEX_ONE:
							return mPrizeAppPermissionFragment;
					case TAB_INDEX_TWO:
						return mPrizePermissionFragment;
	
				}
				throw new IllegalStateException("No fragment at position "
						+ position);
			}
	
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return TAB_INDEX_COUNT;
			}
	
			@Override
			public CharSequence getPageTitle(int position) {
				String tabLabel = null;
				switch (position) {
					case TAB_INDEX_ONE:
						tabLabel = getResources().getString(
								R.string.prize_permission_app_sort);
						break;
					case TAB_INDEX_TWO:
						tabLabel = getResources().getString(
								R.string.prize_permission_sort);
						break;
				}
				return tabLabel;
			}
			@Override
			public Parcelable saveState() {
				Bundle state = null;
				return state;
			}
			@Override  
            public void restoreState(Parcelable state, ClassLoader loader) {  
            }  
		}
	
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
	
			mViewPager.setCurrentItem(tab.getPosition());
			TextView title = (TextView) tab.getCustomView().findViewById(R.id.tab);
			title.setTextColor(getResources().getColor(R.color.prize_text_tab_selected));
		}
	
		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			TextView title = (TextView) tab.getCustomView().findViewById(R.id.tab);
			title.setTextColor(getResources().getColor(R.color.prize_text_tab_unselected));
	
		}
	
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
	
		}
}

