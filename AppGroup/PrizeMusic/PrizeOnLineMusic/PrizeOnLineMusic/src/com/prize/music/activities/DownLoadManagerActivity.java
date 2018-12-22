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

package com.prize.music.activities;

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
import android.widget.ImageView;

import com.prize.music.IApolloService;
import com.prize.music.R;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.page.BasePager;
import com.prize.music.page.DownLoadCompletePager;
import com.prize.music.page.DownLoadQueuePager;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.fragments.BottomActionBarFragment;
import com.prize.music.views.indicator.IconTextPagerAdapter;
import com.prize.music.views.indicator.TabTextPageIndicator;

/**
 * 
 **
 * 下载管理类
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class DownLoadManagerActivity extends FragmentActivity implements
		OnClickListener, ServiceConnection {
	private static final int DOWNOK_PAGER_ID = 0;
	private static final int DOWNING_PAGER_ID = 1;
	private ServiceToken mToken;
	private int currentPage;
	private TabTextPageIndicator mIndicator;
	private ViewPager viewPager;
	private static final int[] ICONS = { R.string.download_complete,
			R.string.download_ing };
	private BasePager[] pagers = new BasePager[ICONS.length];
	private BottomActionBarFragment mBActionbar;
	private ImageView bottom_action_bar_album_art;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		StateBarUtils.initStateBar(this,
				getResources().getColor(R.color.statusbar_color));
		setContentView(R.layout.activity_download_manager_layout);
		StateBarUtils.changeStatus(getWindow());
		findViewById();
		init();
		setListener();
	}

	public ImageView getBottomView() {
		return bottom_action_bar_album_art;
	}

	private void setListener() {
		mIndicator = (TabTextPageIndicator) findViewById(R.id.indicator);
		findViewById(R.id.action_back).setOnClickListener(this);
		findViewById(R.id.action_search).setOnClickListener(this);
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

	private void init() {
		initPager();
	}

	private void findViewById() {
		mIndicator = (TabTextPageIndicator) findViewById(R.id.indicator);
		mBActionbar = (BottomActionBarFragment) getSupportFragmentManager()
				.findFragmentById(R.id.bottomactionbar_new);
		bottom_action_bar_album_art = (ImageView) findViewById(R.id.bottom_action_bar_album_art);
	}

	public void initPager() {
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		MainAdapter adapter = new MainAdapter();
		viewPager.setAdapter(adapter);
		mIndicator.setViewPager(viewPager);

	}

	class MainAdapter extends PagerAdapter implements IconTextPagerAdapter {

		public MainAdapter() {
			DownLoadCompletePager mDownLoadCompletePager = new DownLoadCompletePager(
					DownLoadManagerActivity.this);
			mDownLoadCompletePager.getView(); // 需要初始化，原因：page 可以跳转
			pagers[DOWNOK_PAGER_ID] = mDownLoadCompletePager;
			mDownLoadCompletePager.loadData();
			DownLoadQueuePager mDownLoadQueuePager = new DownLoadQueuePager(
					DownLoadManagerActivity.this);
			mDownLoadQueuePager.getView(); // 需要初始化，原因：page 可以跳转
			pagers[DOWNING_PAGER_ID] = mDownLoadQueuePager;
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
	public void onDestroy() {
		pagers[DOWNOK_PAGER_ID].onDestroy();
		pagers[DOWNING_PAGER_ID].onDestroy();
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.action_back:
			this.finish();
			break;
		case R.id.action_search:
			UiUtils.goToSearchtActivity(this);
			break;

		default:
			break;
		}

	}

	@Override
	protected void onPause() {
		pagers[currentPage].onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		pagers[currentPage].onResume();
		mBActionbar.getBottom_action_bar_dragview().setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(
								DownLoadManagerActivity.this,
								AudioPlayerActivity.class);
						startActivity(intent);

					}
				});
		super.onResume();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
		mBActionbar.updateBottomActionBar();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		MusicUtils.mService = null;
	}

	@Override
	protected void onStart() {
		// Bind to Service
		mToken = MusicUtils.bindToService(this, this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		super.onStart();
	}

	@Override
	protected void onStop() {
		// Unbind
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onStop();

	}
}
