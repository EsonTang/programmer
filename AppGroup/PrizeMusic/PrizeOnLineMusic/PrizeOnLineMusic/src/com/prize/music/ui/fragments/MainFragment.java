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

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.prize.music.activities.MainActivity;
import com.prize.music.base.BaseFragment;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.ui.adapters.PagerAdapter;
import com.prize.music.ui.adapters.ScrollingTabsAdapter;
import com.prize.music.ui.widgets.ScrollableTabView;
import com.prize.music.ui.widgets.ScrollableTabView.OnPageSelectedListener;
import com.prize.music.R;

/**
 * 
 **
 * 主界面最外层
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class MainFragment extends BaseFragment implements
		OnPageSelectedListener {

	private View layoutView;
	private MainActivity mainActivity;
	public PagerAdapter mPagerAdapter;
	private ViewPager mViewPager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		if (layoutView == null) {
			layoutView = inflater.inflate(R.layout.fragment_main_layout, null);
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
		layoutView.findViewById(R.id.search_Iv).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// Intent intent = new Intent(mainActivity,
						// FeedbackExActivity.class);
						// mainActivity.startActivity(intent);
						UiUtils.goToSearchtActivity(mainActivity);
					}
				});

	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void init() {
		initPager();
	}

	public void initPager() {
		// Initiate PagerAdapter
		mPagerAdapter = new PagerAdapter(getFragmentManager());

		mPagerAdapter.addFragment(new HomeMusicLibsFragment());
		mPagerAdapter.addFragment(new MeFragment());

		// Initiate ViewPager
		mViewPager = (ViewPager) layoutView.findViewById(R.id.viewPager);
		mViewPager.setPageMargin(getResources().getInteger(
				R.integer.viewpager_margin_width));
		// mViewPager.setPageMarginDrawable(R.drawable.viewpager_margin);
		mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
		mViewPager.setAdapter(mPagerAdapter);

		initScrollableTabs(mViewPager);
	}

	public void initScrollableTabs(ViewPager mViewPager) {
		ScrollableTabView mScrollingTabs = (ScrollableTabView) layoutView
				.findViewById(R.id.scrollingTabs);
		ScrollingTabsAdapter mScrollingTabsAdapter = new ScrollingTabsAdapter(
				mainActivity);
		mScrollingTabs.setAdapter(mScrollingTabsAdapter);
		mScrollingTabs.setViewPager(mViewPager);
		mScrollingTabs.setPagerSelectLister(this);
	}

	@Override
	public void onPageSelected(int position) {

	}
}
