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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prize.music.activities.MainActivity;
import com.prize.music.base.BaseFragment;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.page.BasePager;
import com.prize.music.page.RankPager;
import com.prize.music.page.RecommendCollectPager;
import com.prize.music.page.RecommendPager;
import com.prize.music.ui.adapters.ScrollingHomeSubTabsAdapter;
import com.prize.music.ui.widgets.ScrollableTabView;
import com.prize.music.ui.widgets.ScrollableTabView.OnPageSelectedListener;
import com.prize.music.views.indicator.IconTextPagerAdapter;
import com.prize.music.R;

/**
 * 
 **
 * 榜单详情，专辑 歌单详情界面界面
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class Rank_Album_detal_Fragment extends BaseFragment {
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		if (layoutView == null) {
			layoutView = inflater.inflate(R.layout.fragment_album_detail, null);
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

	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void init() {
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
