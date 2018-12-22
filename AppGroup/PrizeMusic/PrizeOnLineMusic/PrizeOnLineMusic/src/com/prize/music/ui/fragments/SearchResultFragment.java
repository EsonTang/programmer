package com.prize.music.ui.fragments;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.prize.app.database.dao.SearchHistoryDao;
import com.prize.app.threads.PriorityRunnable;
import com.prize.music.activities.SearchActivity;
import com.prize.music.base.BaseFragment;
import com.prize.music.page.AlbumPager;
import com.prize.music.page.BasePager.ReloadFunction;
import com.prize.music.page.BasePager;
import com.prize.music.page.SingerPager;
import com.prize.music.page.SongPager;
import com.prize.music.ui.adapters.ScrollingSearchResultTabAdapter;
import com.prize.music.ui.widgets.ScrollableTabView;
import com.prize.music.ui.widgets.ScrollableTabView.OnPageSelectedListener;
import com.prize.music.views.indicator.IconTextPagerAdapter;
import com.prize.music.views.indicator.TabTextPageIndicator;
import com.prize.music.R;

/**
 * 搜索结果
 * @author pengyang
 */
public class SearchResultFragment extends BaseFragment implements OnPageSelectedListener {
	private View root;
    private SearchActivity activity;
	
	private View reloadView;
	private View waitView = null;
	
	private ViewPager mViewPager;
	private static final int[] ICONS = { R.string.search_song,
			R.string.search_singer, R.string.search_album };
	private BasePager[] pagers = new BasePager[ICONS.length];
	/*** pageID */
	private static final int HOME_SONG_PAGER_ID = 0;
	private static final int SINGER_PAGER_ID = 1;
	private static final int ALBUM_PAGER_ID = 2;
	
	private SongPager songPager;
	private int currentPage;
	
	private TabTextPageIndicator mIndicator;
	private String keyWord;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.fragment_search_result, container,
				false);
		activity = (SearchActivity) getActivity();
		if(!TextUtils.isEmpty(getArguments().getString("keyword"))){
			keyWord=getArguments().getString("keyword");
			SearchHistoryDao.insert(keyWord, java.lang.System.currentTimeMillis());
		};
		findViewById();
		init();
		setListener();
//		initScrollableTabs(mViewPager);
		return root;
	}

	@Override
	protected void findViewById() {
		reloadView = (View) root.findViewById(R.id.reload_Llyt);
		waitView = (View) root.findViewById(R.id.loading_Llyt_id);
		mViewPager = (ViewPager) root.findViewById(R.id.pager);
		
		mIndicator = (TabTextPageIndicator)root.findViewById(R.id.indicator);
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
		SearchResultAdapter adapter = new SearchResultAdapter();
		mViewPager.setAdapter(adapter);
		mIndicator.setViewPager(mViewPager);
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
		ScrollableTabView mScrollingTabs = (ScrollableTabView)root.findViewById(R.id.scrollingTabs);
		ScrollingSearchResultTabAdapter mScrollingTabsAdapter = new ScrollingSearchResultTabAdapter(activity);
		mScrollingTabs.setAdapter(mScrollingTabsAdapter);
		mScrollingTabs.setViewPager(mViewPager);
		mScrollingTabs.setPagerSelectLister(this);
	}
	
	@Override
	public void onPageSelected(int position) {
		PriorityRunnable.decreaseBase();
		pagers[position].loadData();
		currentPage = position;
	}
	
	class SearchResultAdapter extends PagerAdapter implements IconTextPagerAdapter {

		public SearchResultAdapter() {
			// 歌曲
		    songPager = new SongPager(activity);
		    songPager.getView(); // 需要初始化，原因：page 可以跳转
			pagers[HOME_SONG_PAGER_ID] = songPager;
			songPager.setKeyWord(keyWord);
			songPager.loadData();
			
			// 歌手
			SingerPager sing = new SingerPager(activity);
			pagers[SINGER_PAGER_ID]=sing; // 需要初始化，原因：page 可以跳转
			pagers[SINGER_PAGER_ID].getView(); // 需要初始化，原因：page 可以跳转
			sing.setKeyWord(keyWord);
			 // 专辑
			AlbumPager mAlbumPager = new AlbumPager(activity);
			pagers[ALBUM_PAGER_ID]=mAlbumPager; // 需要初始化，原因：page 可以跳转
			pagers[ALBUM_PAGER_ID].getView(); // 需要初始化，原因：page 可以跳转
			mAlbumPager.setKeyWord(keyWord);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(pagers[position].getView());
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
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
	
	/**
	 * 请求搜索结果
	 * @param keyword关键字
	 */
	public void requestData(String keyWord) {
		SearchHistoryDao.insert(keyWord, java.lang.System.currentTimeMillis());
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		pagers[HOME_SONG_PAGER_ID].onDestroy();
		pagers[SINGER_PAGER_ID].onDestroy();
		pagers[ALBUM_PAGER_ID].onDestroy();
	}

	@Override
	public void onResume() {
		pagers[currentPage].onResume();
		super.onResume();
	}

	@Override
	public void onPause() {
		pagers[currentPage].onPause();
		super.onPause();
	}
	
	/** 隐藏等待框*/
	public void hideWaiting() {
		if (waitView == null)
			return;
		waitView.setVisibility(View.GONE);
//		GifView gifWaitingView = (GifView) waitView
//				.findViewById(R.id.progress_loading_loading);
//		gifWaitingView.setPaused(true);
		reloadView.setVisibility(View.GONE);
	}

	/** 加载失败*/
	public void loadingFailed(final ReloadFunction reload) {
		waitView.setVisibility(View.GONE);
		reloadView.setVisibility(View.VISIBLE);
		LinearLayout reloadLinearLayout = (LinearLayout) reloadView
				.findViewById(R.id.reload_Llyt);
		if (reloadLinearLayout != null) {
			reloadLinearLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					reload.reload();
				}
			});
		}
	}

	/** 显示等待框*/
	public void showWaiting() {
		if (waitView == null)
			return;
//		GifView gifWaitingView = (GifView) waitView
//				.findViewById(R.id.progress_loading_loading);
//		gifWaitingView.setPaused(false);
		waitView.setVisibility(View.VISIBLE);
		reloadView.setVisibility(View.GONE);
	}
}
