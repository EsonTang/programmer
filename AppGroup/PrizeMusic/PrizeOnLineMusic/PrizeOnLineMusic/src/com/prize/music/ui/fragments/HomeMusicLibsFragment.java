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

package com.prize.music.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prize.music.activities.MainActivity;
import com.prize.music.base.BaseFragment;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.page.BasePager;
import com.prize.music.page.RadioPager;
import com.prize.music.page.RankPager;
import com.prize.music.page.RecommendCollectPager;
import com.prize.music.page.RecommendPager;
import com.prize.music.ui.adapters.ScrollingHomeSubTabsAdapter;
import com.prize.music.ui.adapters.ScrollingTabsAdapter;
import com.prize.music.ui.fragments.grid.ArtistsFragment;
import com.prize.music.ui.fragments.list.PlaylistListFragment;
import com.prize.music.ui.widgets.ScrollableTabView;
import com.prize.music.ui.widgets.ScrollableTabView.OnPageSelectedListener;
import com.prize.music.views.indicator.IconTextPagerAdapter;
import com.prize.music.views.indicator.TabTextPageIndicator;
import com.prize.music.R;

/**
 * 
 **
 * 音乐馆界面
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class HomeMusicLibsFragment extends BaseFragment {
	protected static final String TAG = "MusicLibsFragment";
	private static final int[] ICONS = { R.string.recommend, R.string.rank,
			R.string.collect, R.string.radio };
	private BasePager[] pagers = new BasePager[ICONS.length];
	private View layoutView;
	private MainActivity mainActivity;
	public PagerAdapter mPagerAdapter;
	RecommendPager mRecommendPager;
	/*** pageID */
	private static final int RECOMMEND_PAGER_ID = 0;
	private static final int RANK_PAGER_ID = 1;
	private static final int COLLECT_PAGER_ID = 2;
	private static final int RADIO_PAGE_ID = 3;
	private int currentPage;
	private TabTextPageIndicator mIndicator;
	private ViewPager viewPager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		if (layoutView == null) {
			layoutView = inflater.inflate(
					R.layout.fragment_subcontainer_layout, null);
			findViewById();
			init();
			setListener();
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) layoutView.getParent();
		if (parent != null) {
			parent.removeView(layoutView);
		}
		return layoutView;
	}

	@Override
	protected void findViewById() {
		mIndicator = (TabTextPageIndicator) layoutView
				.findViewById(R.id.indicator);

	}

	@Override
	protected void setListener() {
		mIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (currentPage != position) {
					pagers[currentPage].onPause(); // 前一页pause
					pagers[position].onResume();
				}
				pagers[position].loadData();
				currentPage = position;

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	@Override
	protected void init() {
		initPager();
	}

	public void initPager() {
		viewPager = (ViewPager) layoutView.findViewById(R.id.viewPager);
		MainAdapter adapter = new MainAdapter();
		viewPager.setAdapter(adapter);
		mIndicator.setViewPager(viewPager);

	}

	class MainAdapter extends PagerAdapter implements IconTextPagerAdapter {

		public MainAdapter() {
			mRecommendPager = new RecommendPager(mainActivity);
			mRecommendPager.getView(); // 需要初始化，原因：page 可以跳转
			pagers[RECOMMEND_PAGER_ID] = mRecommendPager;
			mRecommendPager.loadData();
			RankPager mRankPager = new RankPager(mainActivity);
			mRecommendPager.getView(); // 需要初始化，原因：page 可以跳转
			pagers[RANK_PAGER_ID] = mRankPager;
			RecommendCollectPager mRecommendCollectPager = new RecommendCollectPager(
					mainActivity);
			mRecommendCollectPager.getView(); // 需要初始化，原因：page 可以跳转
			pagers[COLLECT_PAGER_ID] = mRecommendCollectPager;
			RadioPager mRadioPager = new RadioPager(mainActivity);
			mRadioPager.getView(); // 需要初始化，原因：page 可以跳转
			pagers[RADIO_PAGE_ID] = mRadioPager;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(pagers[position].getView());
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			LogUtils.i(TAG, "pagers=" + pagers);
			container.addView(pagers[position].getView());
			return pagers[position].getView();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			int id = ICONS[position % ICONS.length];
			return getString(id);
		}

		@Override
		public int getCount() {
			return ICONS.length;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	@Override
	public void onDestroy() {
		pagers[RECOMMEND_PAGER_ID].onDestroy();
		pagers[RANK_PAGER_ID].onDestroy();
		pagers[COLLECT_PAGER_ID].onDestroy();
		pagers[RADIO_PAGE_ID].onDestroy();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		pagers[currentPage].onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		pagers[currentPage].onResume();
		super.onResume();
	}
}
