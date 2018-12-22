/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.android.gallery3d.filtershow;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.sticker.WatermarkBean;
import com.prize.sticker.pager.FoodFragment;
import com.prize.sticker.pager.HotFragment;
import com.prize.sticker.pager.InterestFragment;
import com.prize.sticker.pager.MoodFragment;
import com.prize.sticker.pager.OriginalityFragment;
import com.prize.sticker.pager.PagerTab;
import com.prize.sticker.pager.PagerTab.PagerTabListener;
import com.prize.sticker.pager.TravelFragment;
import com.android.gallery3d.R;

import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

// Nav bar color customized feature. prize-linkh-20170926 @{
import com.mediatek.common.prizeoption.PrizeOption;
import android.graphics.Color;
// @}

public class PickStickerActivity extends Activity {

	// //////////////////////////////////////////
	public static final String KEY_STICKER_RER = "pick_sticker";
	private ViewPager mViewPager;
	private ViewPagerAdapter mAdapter;
	private TextView mHotTv;
	private TextView mInterestTv;
	private TextView mFoodTv;
	private TextView mTravelTv;
	private TextView mOriginalityTv;
	private TextView mMoodTv;
	private ImageView mBackIm;

	private static final int TAB_INDEX_HOT = 0;
	private static final int TAB_INDEX_INTEREST = 1;
	private static final int TAB_INDEX_FOOD = 2;
	private static final int TAB_INDEX_TRAVEL = 3;
	private static final int TAB_INDEX_ORIGINALITY = 4;
	private static final int TAB_INDEX_MOOD = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prize_pick_sticker_main);
		if (getIntent().getBooleanExtra(FilterShowActivity.LAUNCH_FULLSCREEN, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		findViews();
        // Nav bar color customized feature. prize-linkh-2017.08.31 @{
        if(PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            getWindow().setDisableCustNavBarColor(true);
        } // @} 
	}

	private void findViews() {
		mViewPager = (ViewPager) findViewById(R.id.viewpager);

		mAdapter = new ViewPagerAdapter(this, mViewPager);

		mHotTv = (TextView) findViewById(R.id.tv_hot);
		mInterestTv = (TextView) findViewById(R.id.tv_interest);
		mFoodTv = (TextView) findViewById(R.id.tv_food);
		mTravelTv = (TextView) findViewById(R.id.tv_travel);
		mOriginalityTv = (TextView) findViewById(R.id.tv_originality);
		mMoodTv = (TextView) findViewById(R.id.tv_mood);
		mBackIm = (ImageView) findViewById(R.id.im_back);
		mBackIm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mAdapter.addTab(new PagerTab(mHotTv, TAB_INDEX_HOT), HotFragment.class,
				null);
		mAdapter.addTab(new PagerTab(mInterestTv, TAB_INDEX_INTEREST),
				InterestFragment.class, null);
		mAdapter.addTab(new PagerTab(mFoodTv, TAB_INDEX_FOOD),
				FoodFragment.class, null);
		mAdapter.addTab(new PagerTab(mTravelTv, TAB_INDEX_TRAVEL),
				TravelFragment.class, null);

		mAdapter.addTab(new PagerTab(mOriginalityTv, TAB_INDEX_ORIGINALITY),
				OriginalityFragment.class, null);
		mAdapter.addTab(new PagerTab(mMoodTv, TAB_INDEX_MOOD),
				MoodFragment.class, null);

		mViewPager.setCurrentItem(1);

	}

	private static class ViewPagerAdapter extends FragmentPagerAdapter
			implements PagerTabListener, OnPageChangeListener {

		private Context mContext;
		private ViewPager mViewPager = null;
		private ArrayList<TabInfo> mTabList = new ArrayList<TabInfo>();

		public ViewPagerAdapter(Activity activity, ViewPager viewPager) {
			super(activity.getFragmentManager());
			mContext = activity;
			mViewPager = viewPager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {

		}

		@Override
		public void onPageSelected(int position) {
			for (int i = 0, size = mTabList.size(); i < size; i++) {
				TabInfo tabInfo = mTabList.get(i);
				if (i == position) {
					tabInfo.tab.onSelected(true);
				} else {
					tabInfo.tab.onSelected(false);
				}
			}
		}

		@Override
		public void onTabSelected(PagerTab tab) {
			mViewPager.setCurrentItem(tab.getIndex());
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo tab = mTabList.get(position);
			if (tab.fragment == null) {
				tab.fragment = Fragment.instantiate(mContext,
						tab.clazz.getName(), tab.bundle);
			}
			return tab.fragment;
		}

		@Override
		public int getCount() {
			return mTabList.size();
		}

		public void addTab(PagerTab tab, Class<?> clazz, Bundle bundle) {
			TabInfo tabInfo = new TabInfo(clazz, bundle, tab);
			tab.setPagerTabListener(this);
			mTabList.add(tabInfo);
			notifyDataSetChanged();
		}

		private static final class TabInfo {
			private final Class<?> clazz;
			private final Bundle bundle;
			private final PagerTab tab;
			Fragment fragment;

			TabInfo(Class<?> clazz, Bundle bundle, PagerTab tab) {
				this.clazz = clazz;
				this.bundle = bundle;
				this.tab = tab;
			}
		}
	}

	public void setPick(WatermarkBean obj) {
		Intent data = new Intent();
		data.putExtra(KEY_STICKER_RER, obj);
		setResult(RESULT_OK, data);
		finish();
	}
}
