/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：全部音乐Fragment
 *当前版本：V1.0
 *作  者：longbaoxiu
 *完成日期：2015-7-21
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ********************************************/
package com.prize.music.ui.fragments;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.constants.Constants;
import com.prize.music.IfragToActivityLister;
import com.prize.music.R;
import com.prize.music.activities.MainActivity;
import com.prize.music.activities.SearchBrowserActivity;
import com.prize.music.base.BaseFragment;
import com.prize.music.ui.adapters.LocalMusicScrollingTabsAdapter;
import com.prize.music.ui.adapters.PagerAdapter;
import com.prize.music.ui.fragments.base.SongsListViewFragment;
import com.prize.music.ui.fragments.grid.AlbumsFragment;
import com.prize.music.ui.fragments.grid.ArtistsFragment;
import com.prize.music.ui.fragments.list.SongsFragment;
import com.prize.music.ui.widgets.ScrollableTabView;
import com.prize.music.ui.widgets.ScrollableTabView.OnPageSelectedListener;

/**
 **
 * 类描述：全部音乐Fragment
 * 
 * @author longbaoxiu
 * @version v1.0
 */

public class LocalMusicLibraryFragment extends BaseFragment implements
		OnPageSelectedListener {
	public static final String SAVED_STATE_ACTION_BAR_HIDDEN = "saved_state_action_bar_hidden";

	private TextView action_back,action_title;

	private TextView action_search;

	private Context mContext;

	private View layoutView;

	private ViewPager mViewPager;

	public RelativeLayout action_back_Rlyt;
	public RelativeLayout select_Rlyt;

	public PagerAdapter mPagerAdapter;
	private TextView action_cancel;
	private TextView action_sure;
	private IfragToActivityLister mIfragToActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		layoutView = inflater.inflate(R.layout.fragment_library_browser, null);
		findViewById();
		init();
		setListener();
		// 缓存的rootView需要判断是否已经被加过parent，
		ViewGroup parent = (ViewGroup) layoutView.getParent();
		if (parent != null) {
			parent.removeView(layoutView);
		}
		return layoutView;
	}

	@Override
	protected void init() {
		initPager();

	}

	protected void setListener() {
		if (action_title!=null) 
			action_title.setText(R.string.local_music);
		action_search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent searchIntent = new Intent(mContext,
						SearchBrowserActivity.class);
				startActivity(searchIntent);
			}
		});
		action_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*
				 * Fragment mMeFragment = getActivity()
				 * .getSupportFragmentManager().findFragmentByTag(
				 * MeFragment.class.getSimpleName()); if (mMeFragment != null &&
				 * mMeFragment.isAdded()) {
				 * getActivity().getSupportFragmentManager()
				 * .beginTransaction().hide(LocalMusicLibraryFragment.this)
				 * .show(mMeFragment).commitAllowingStateLoss(); }
				 */
				getActivity().finish();
			}
		});

		action_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				updateViews(false);
				action_sure.setText(getString(R.string.all_select));
				mIfragToActivity
						.processAction(Constants.ACTION_CANCEL_FR_TO_FR);

			}

		});
		action_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mIfragToActivity.processAction(Constants.ACTION_FR_2_FR_SURE);
			}
		});

	}

	/**
	 * 控制全选 反选按钮
	 * 
	 * @param flag为
	 *            ture时，设置为全选
	 */
	public void updateSelectState(boolean flag) {
		if (!flag) {
			action_sure.setText(getString(R.string.no_select));
		} else {
			action_sure.setText(getString(R.string.all_select));
		}
	}

	/**
	 * 是否显示编辑选择菜单
	 * 
	 * @param isNeedShow
	 *            true：显示 ，反之；不显示
	 */
	public void updateViews(boolean isNeedShow) {
		if (!isNeedShow) {
			select_Rlyt.setVisibility(View.GONE);
			action_back_Rlyt.setVisibility(View.VISIBLE);

		} else {
			select_Rlyt.setVisibility(View.VISIBLE);
			action_back_Rlyt.setVisibility(View.GONE);
		}

	}

	protected void findViewById() {
		action_back = (TextView) layoutView.findViewById(R.id.action_back);
		action_title = (TextView) layoutView.findViewById(R.id.action_title);	
		action_search = (TextView) layoutView.findViewById(R.id.action_search);
		action_back_Rlyt = (RelativeLayout) layoutView
				.findViewById(R.id.action_back_Rlyt);
		select_Rlyt = (RelativeLayout) layoutView
				.findViewById(R.id.select_Rlyt);

		action_cancel = (TextView) layoutView.findViewById(R.id.action_cancel);
		action_sure = (TextView) layoutView.findViewById(R.id.action_sure);

	}

	/**
	 * 方法描述：Initiate ViewPager and PagerAdapter
	 * 
	 * @param 参数名
	 *            说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void initPager() {
		// Initiate PagerAdapter
		mPagerAdapter = new PagerAdapter(getFragmentManager());

		// Get tab visibility preferences
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Set<String> defaults = new HashSet<String>(Arrays.asList(getResources()
				.getStringArray(R.array.tab_titles)));
		Set<String> tabs_set = sp
				.getStringSet(Constants.TABS_ENABLED, defaults);
		if (tabs_set.size() == 0) {
			tabs_set = defaults;
		}

		// Only show tabs that were set in preferences
		// Recently added tracks
		// // Tracks
		if (tabs_set.contains(getResources().getString(R.string.tab_songs))) {
			SongsFragment mSongsFragment = new SongsFragment();
			mPagerAdapter.addFragment(mSongsFragment);
		}
		if (tabs_set.contains(getResources().getString(R.string.tab_artists)))
			mPagerAdapter.addFragment(new ArtistsFragment());
		// Albums
		if (tabs_set.contains(getResources().getString(R.string.tab_albums)))
			mPagerAdapter.addFragment(new AlbumsFragment());

		// Initiate ViewPager
		mViewPager = (ViewPager) layoutView.findViewById(R.id.viewPager);
		mViewPager.setPageMargin(getResources().getInteger(
				R.integer.viewpager_margin_width));
		// mViewPager.setPageMarginDrawable(R.drawable.viewpager_margin);
		mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
		mViewPager.setAdapter(mPagerAdapter);

		initScrollableTabs(mViewPager);
	}

	/**
	 * 方法描述：Initiate the tabs
	 * 
	 * @param mViewPager
	 *            ViewPager控件
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void initScrollableTabs(ViewPager mViewPager) {
		ScrollableTabView mScrollingTabs = (ScrollableTabView) layoutView
				.findViewById(R.id.scrollingTabs);
		LocalMusicScrollingTabsAdapter mScrollingTabsAdapter = new LocalMusicScrollingTabsAdapter(
				(Activity) mContext);
		mScrollingTabs.setAdapter(mScrollingTabsAdapter);
		mScrollingTabs.setViewPager(mViewPager);
		mScrollingTabs.setPagerSelectLister(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Intent i = mContext.getPackageManager().getLaunchIntentForPackage(
				mContext.getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onAttach(Activity activity) {

		try {
			mIfragToActivity = (IfragToActivityLister) activity;
		} catch (Exception e) {
			throw new ClassCastException(activity.toString()
					+ "must implement  IfragToActivity");
		}
		super.onAttach(activity);
	}

	@Override
	public void onPageSelected(int position) {
		if (position != 0) {
			// 刷新界面
			updateViews(false);

			action_sure.setText(getString(R.string.all_select));
			mIfragToActivity.processAction(Constants.ACTION_CANCEL_FR_TO_FR);
		}

	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		if (!hidden) {
			SongsListViewFragment mSongsListViewFragment = (SongsListViewFragment) mPagerAdapter
					.getItem(0);
			if (mSongsListViewFragment != null
					&& mSongsListViewFragment.isAdded()
					&& mSongsListViewFragment.getActivity() != null) {
				MainActivity activity = (MainActivity) mSongsListViewFragment
						.getActivity();
				activity.mIfragToActivity = mSongsListViewFragment;
			}
		}
		super.onHiddenChanged(hidden);
	}
}
