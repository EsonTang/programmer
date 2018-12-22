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

package com.prize.appcenter.activity;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.ScrollingTabsAdapter;
import com.prize.appcenter.ui.pager.BasePager;
import com.prize.appcenter.ui.pager.GameGiftPager;
import com.prize.appcenter.ui.pager.MyGameGiftPager;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.widget.CustomViewPager;
import com.prize.appcenter.ui.widget.ScrollableTabView;
import com.prize.appcenter.ui.widget.ScrollableTabView.OnPageSelectedListener;
import com.viewpagerindicator.IconPagerAdapter;

/**
 **
 * 游戏礼包入口跳转后的界面
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class GameGiftActivity extends ActionBarNoTabActivity {
	private static int[] GAMEICONS = { R.string.gift, R.string.me };

	private BasePager[] pagers = new BasePager[GAMEICONS.length];
	/*** pageID */
	private static final int GAME_GIFT_PAGER_ID = 0;
	private static final int MY_GIFT_PAGER_ID = 1;
	private int currentPage;
	public GameGiftPager appPager;
	public MyGameGiftPager gamePager;
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_requiresoft);
		WindowMangerUtils.changeStatus(getWindow());
		setTitle(R.string.gamegift);
		findViewById();
		mToken = AIDLUtils.bindToService(this, this);
		setListener();
		init();
		initScrollableTabs(mViewPager);
	}

	/**
	 * 方法描述：Initiate the tabs
	 * 
	 * @param mViewPager
	 *            ViewPager控件
	 */
	public void initScrollableTabs(ViewPager mViewPager) {
		ScrollableTabView mScrollingTabs = (ScrollableTabView) findViewById(R.id.scrollingTabs);
		ScrollingTabsAdapter mScrollingTabsAdapter = new ScrollingTabsAdapter(
				this, R.array.gamegift_and_me);
		mScrollingTabs.setAdapter(mScrollingTabsAdapter);
		mScrollingTabs.setViewPager(mViewPager);
		mScrollingTabs.setPagerSelectLister(new OnPageSelectedListener() {

			@Override
			public void onPageSelected(int position) {
				if (currentPage != position) {// 友盟统计
					pagers[currentPage].onPause(); // 前一页pause
					pagers[position].onResume();
					pagers[position].loadData();
					currentPage = position;
				}

			}
		});
	}

	private void init() {

		RequiredAdapter adapter = new RequiredAdapter();
		mViewPager.setAdapter(adapter);

	}

	private void setListener() {

		// TODO Auto-generated method stub

	}

	private void findViewById() {

		mViewPager = (CustomViewPager) findViewById(R.id.pager);

	}

	@Override
	public String getActivityName() {

		// TODO Auto-generated method stub
		return "GameGiftActivity";
	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {

		// TODO Auto-generated method stub

	}

	class RequiredAdapter extends PagerAdapter implements IconPagerAdapter {

		public RequiredAdapter() {
			appPager = new GameGiftPager(GameGiftActivity.this);
			pagers[GAME_GIFT_PAGER_ID] = appPager;
			pagers[GAME_GIFT_PAGER_ID].getView();
			pagers[GAME_GIFT_PAGER_ID].loadData();
			gamePager = new MyGameGiftPager(GameGiftActivity.this);
			pagers[MY_GIFT_PAGER_ID] = gamePager;
			pagers[MY_GIFT_PAGER_ID].getView();
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
			int id = GAMEICONS[position % GAMEICONS.length];
			return getString(id);

		}

		@Override
		public int getCount() {
			return GAMEICONS.length;
		}

		@Override
		public int getIconResId(int index) {
			return GAMEICONS[index];

		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	@Override
	protected void onResume() {
		pagers[currentPage].onResume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		pagers[currentPage].onPause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		pagers[GAME_GIFT_PAGER_ID].onDestroy();
		pagers[MY_GIFT_PAGER_ID].onDestroy();
		AIDLUtils.unbindFromService(mToken);
		super.onDestroy();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		pagers[GAME_GIFT_PAGER_ID].onResume();
	}
}
