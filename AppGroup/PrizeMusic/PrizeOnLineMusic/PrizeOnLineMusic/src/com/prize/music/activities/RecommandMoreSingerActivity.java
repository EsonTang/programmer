package com.prize.music.activities;

import com.prize.app.threads.PriorityRunnable;
import com.prize.app.util.JLog;
import com.prize.music.R;
import com.prize.music.IApolloService;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.page.BasePager;
import com.prize.music.page.ChinesePager;
import com.prize.music.page.EuropePager;
import com.prize.music.page.JapanesePager;
import com.prize.music.page.KoreaPager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.adapters.ScrollingRecommendTabAdapter;
import com.prize.music.ui.fragments.BottomActionBarFragment;
import com.prize.music.ui.widgets.ScrollableTabView;
import com.prize.music.ui.widgets.ScrollableTabView.OnPageSelectedListener;
import com.prize.music.views.indicator.IconTextPagerAdapter;
import com.prize.music.views.indicator.TabTextPageIndicator;
/**
 * 更多歌手推荐
 * @author pengyang
 *
 */
public class RecommandMoreSingerActivity extends FragmentActivity implements OnPageSelectedListener, 
            OnClickListener,ServiceConnection{
       
	private ViewPager mViewPager;
	private static final int[] ICONS = { R.string.search_chinese,
			R.string.search_Europe, R.string.search_japanese,R.string.search_korea };
	private BasePager[] pagers = new BasePager[ICONS.length];
	
	/*** pageID */
	private static final int HOME_CHINESE_PAGER_ID = 0;
	private static final int EUROPE_PAGER_ID = 1;
	private static final int JAPANESE_PAGER_ID = 2;
	private static final int KOREA_PAGER_ID = 3;
	
	private ChinesePager chinesePager;
	private int currentPage;
	
	private ImageButton action_bar_back;
	private TextView action_bar_title;
	private ImageButton action_bar_search;
	
	private TabTextPageIndicator mIndicator;
    
	private BottomActionBarFragment mBActionbar;
	private ServiceToken mToken;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		StateBarUtils.initStateBar(this,getResources().getColor(R.color.statusbar_color));
		setContentView(R.layout.activity_recommand_more_singer);
		StateBarUtils.changeStatus(getWindow());
		findViewById();
		init();
//		initScrollableTabs(mViewPager);
        setIndicator();
	}

	private void setIndicator() {
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

	/**
	 * 方法描述：Initiate the tabs
	 * 
	 * @param mViewPager
	 *            ViewPager控件
	 */
	public void initScrollableTabs(ViewPager mViewPager) {
		ScrollableTabView mScrollingTabs = (ScrollableTabView) findViewById(R.id.scrollingTabs);
		ScrollingRecommendTabAdapter mScrollingTabsAdapter = new ScrollingRecommendTabAdapter(this);
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

	private void init() {
		RecommandmoreAdapter adapter = new RecommandmoreAdapter();
		mViewPager.setAdapter(adapter);
		mIndicator.setViewPager(mViewPager);
	}

	private void findViewById() {
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mIndicator = (TabTextPageIndicator)findViewById(R.id.indicator);
		
		action_bar_back = (ImageButton) findViewById(R.id.action_bar_back);
		action_bar_title= (TextView) findViewById(R.id.action_bar_title);
		action_bar_search = (ImageButton) findViewById(R.id.action_bar_search);
		action_bar_back.setOnClickListener(this);
		action_bar_title.setOnClickListener(this);
		action_bar_search.setOnClickListener(this);
		
		mBActionbar = (BottomActionBarFragment) getSupportFragmentManager()
				.findFragmentById(R.id.bottomactionbar_new);
	}

	class RecommandmoreAdapter extends PagerAdapter implements IconTextPagerAdapter {

		public RecommandmoreAdapter() {
			// 华语
			chinesePager = new ChinesePager(RecommandMoreSingerActivity.this);
			chinesePager.getView(); // 需要初始化，原因：page 可以跳转
			pagers[HOME_CHINESE_PAGER_ID] = chinesePager;
			chinesePager.loadData();
			
			// 欧美
			pagers[EUROPE_PAGER_ID] = new EuropePager(
					RecommandMoreSingerActivity.this);
			pagers[EUROPE_PAGER_ID].getView(); // 需要初始化，原因：page 可以跳转
			// 日本
			pagers[JAPANESE_PAGER_ID] = new JapanesePager(
					RecommandMoreSingerActivity.this);
			pagers[JAPANESE_PAGER_ID].getView(); // 需要初始化，原因：page 可以跳转
			
			// 韩国
		    pagers[KOREA_PAGER_ID] = new KoreaPager(
					RecommandMoreSingerActivity.this);
			pagers[KOREA_PAGER_ID].getView(); // 需要初始化，原因：page 可以跳转

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

	@Override
	protected void onResume() {
		pagers[currentPage].onResume();
		mBActionbar.getBottom_action_bar_dragview().setOnClickListener(
				new OnClickListener() {

			@Override
			public void onClick(View v) {
				   Intent intent = new Intent(RecommandMoreSingerActivity.this,
						AudioPlayerActivity.class);
				   startActivity(intent);
			}
		});
		super.onResume();
	}

	@Override
	protected void onPause() {
		pagers[currentPage].onPause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		pagers[HOME_CHINESE_PAGER_ID].onDestroy();
		pagers[EUROPE_PAGER_ID].onDestroy();
		pagers[JAPANESE_PAGER_ID].onDestroy();
		pagers[KOREA_PAGER_ID].onDestroy();
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onDestroy();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.action_bar_back:
			onBackPressed();
			break;
		case R.id.action_bar_title:
			onBackPressed();
			break;
		case R.id.action_bar_search:
			UiUtils.goToSearchtActivity(this);
			this.finish();
			break;
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
		mBActionbar.updateBottomActionBar();
		JLog.i("hu", "RecommandMoreAct====onServiceConnected");
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		MusicUtils.mService = null;
	}
	
	@Override
	protected void onStart() {
		mToken = MusicUtils.bindToService(this, this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onStop();
	}
}
