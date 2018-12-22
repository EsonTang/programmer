//package com.prize.appcenter.ui.pager;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Color;
//import android.support.v4.content.LocalBroadcastManager;
//import android.support.v4.view.PagerAdapter;
//import android.support.v4.view.ViewPager;
//import android.support.v4.view.ViewPager.OnPageChangeListener;
//import android.util.TypedValue;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.prize.app.net.HeadResultCallBack;
//import com.prize.app.threads.PriorityRunnable;
//import com.prize.app.util.JLog;
//import com.prize.app.util.MTAUtil;
//import com.prize.appcenter.R;
//import com.prize.appcenter.activity.MainActivity;
//import com.prize.appcenter.activity.RootActivity;
//import com.prize.appcenter.ui.util.PagerSlidingTabStripExtends;
//import com.viewpagerindicator.IconPagerAdapter;
//
//import java.util.Map;
//
//import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
//
///***
// * 主界面游戏或者应用的pager(1.8release改动)
// *
// * 类名称：AppCategoryPager
// *
// * 创建人：longbaoxiu
// *
// * 修改时间：2016年6月21日 下午2:59:33
// *
// * @version 1.0.0
// *
// */
//public class AppCategoryPager extends BasePager implements HeadResultCallBack {
//
//	private PagerSlidingTabStripExtends mTabs;
//
//	private AppTypePagerGifts mAppTypePager;
//	private GameTypePagerGifts mGameTypePager;
//
//	private int mCurrentPage;
//
//	private static final int[] ICONS = { R.string.app_category_nice,
//			R.string.app_category_type, R.string.app_category_rank };
//	private BasePager[] pagers = new BasePager[ICONS.length];
//	/***精品 */
//	private static final int HOME_PAGER_ID = 0;
//	/***排行*/
//	private static final int GAME_RANK_PAGER_ID = 2;
//	/***分类 */
//	private static final int GAME_TYPE_PAGER_ID = 1;
//	protected static final String TAG = "AppCategoryPager";
//
//	/**
//	 *
//	 * 创建一个新的实例 AppCategoryPager.
//	 *
//	 * @param activity RootActivity
//	 * @param isGame   是否是游戏选项
//	 */
//	public AppCategoryPager(RootActivity activity, boolean isGame) {
//		super(activity);
//		this.isPopular = isGame;
//	}
//
//	@Override
//	public void onBack(int what, int arg1, int arg2, Object obj) {
//	}
//
//	@Override
//	public View onCreateView() {
//		LayoutInflater inflater = LayoutInflater.from(activity);
//		View mView = inflater.inflate(R.layout.app_category_pager, rootView,false);
//		// 初始化ViewPager并且添加适配器
//		ViewPager pager = (ViewPager) mView.findViewById(R.id.slidingpager);
//		mTabs = (PagerSlidingTabStripExtends) mView
//				.findViewById(R.id.slidingtabs);
//
//		AppCategoryPagerAdapter adapter = new AppCategoryPagerAdapter((MainActivity) activity);
//		pager.setOffscreenPageLimit(3);
//		pager.setAdapter(adapter);
//		mTabs.setViewPager(pager);
//		initTabsValue();
//		setOnclickListener();
//
//		return mView;
//	}
//
//	private void setOnclickListener() {
//
//		mTabs.setOnPageChangeListener(new OnPageChangeListener() {
//
//			@Override
//			public void onPageSelected(int position) {
//				//释放所有视频
//				JCVideoPlayer.releaseAllVideos();
//
//				PriorityRunnable.decreaseBase();
//				// 友盟统计
//				if (mCurrentPage != position) {
//					pagers[mCurrentPage].onPause(); // 前一页pause
//					JLog.i(TAG, "mTabs中执行了onPause()");
//					pagers[position].onResume();
//					int id = ICONS[position % ICONS.length];
//					MTAUtil.onAPPOrGamePageTab(activity.getString(id), isPopular);
//				}
//
//				pagers[position].loadData();
//
//				mCurrentPage = position;
//				if(mCurrentPage==2){
//					MTAUtil.onclicGameAppRank(isPopular);
//				}
//
//			}
//
//			@Override
//			public void onPageScrolled(int arg0, float arg1, int arg2) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void onPageScrollStateChanged(int arg0) {
//				// TODO Auto-generated method stub
//
//			}
//		});
//	}
//	private BroadcastReceiver mBroadcastReceiver;
//	/**
//	 * mPagerSlidingTabStrip默认值配置
//	 *
//	 */
//	private void initTabsValue() {
//		// 底部游标颜色
//		mTabs.setIndicatorColor(Color.parseColor("#12b7f5"));
//		// tab的分割线颜色
//		mTabs.setDividerColor(Color.TRANSPARENT);
//		// tab背景
//		mTabs.setBackgroundColor(activity.getResources().getColor(R.color.app_background));
//		// 游标高度
//		mTabs.setIndicatorHeight((int) TypedValue.applyDimension(
//				TypedValue.COMPLEX_UNIT_DIP, 2, activity.getResources()
//						.getDisplayMetrics()));
//		mBroadcastReceiver=new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				if("com.prize.pageOne".equals(intent.getAction())){
//					mTabs.getPager().setCurrentItem(0);
//				}else{
//				boolean flag=intent.getBooleanExtra("isPopular",false);
//				if(isPopular ==flag){
//					mTabs.getPager().setCurrentItem(2);
//				}
//				}
//			}
//		};
//
//		IntentFilter filter = new IntentFilter();
//		filter.addAction("com.prize.torank");
//		if(isPopular){
//			filter.addAction("com.prize.pageOne");
//		}
//		LocalBroadcastManager.getInstance(activity.getApplicationContext()).registerReceiver(mBroadcastReceiver,filter);
//	}
//
//	@Override
//	public void loadData() {
//		// 当选中的时候读取精品页的数据
//		if(isPopular){
//			mGameTypePager.loadData();
//		}else {
//			mAppTypePager.loadData();
//		}
//	}
//
//	@Override
//	public void onActivityCreated() {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public String getPageName() {
//		return isPopular ?activity.getResources().getString(R.string.game):activity.getResources().getString(R.string.apps);
//	}
//
//	@Override
//	public void onDestroy() {
//		pagers[HOME_PAGER_ID].onDestroy();
//		pagers[GAME_RANK_PAGER_ID].onDestroy();
//		pagers[GAME_TYPE_PAGER_ID].onDestroy();
//		if(mBroadcastReceiver !=null&&activity !=null){
//			LocalBroadcastManager.getInstance(activity.getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
//		}
//
//	}
//
//	@Override
//	public void onResponseHeaders(Map<String, String> headers) {
//		// TODO Auto-generated method stub
//
//	}
//
//
//	private class AppCategoryPagerAdapter extends PagerAdapter implements	IconPagerAdapter {
//		public AppCategoryPagerAdapter(MainActivity activity) {
//			super();
//			if(isPopular){
//				mGameTypePager=new GameTypePagerGifts(activity, true);
//				pagers[HOME_PAGER_ID] = mGameTypePager; // 需要初始化，原因：page 可以跳转
//				mGameTypePager.getView();
//				mGameTypePager.onResume();//防止无法绑定下载监听
//			}else{
//				// 精品
//				mAppTypePager = new AppTypePagerGifts(activity, false);
//				pagers[HOME_PAGER_ID] = mAppTypePager; // 需要初始化，原因：page 可以跳转
//				mAppTypePager.getView();
//				mAppTypePager.onResume();//防止无法绑定下载监听
//			}
//
//			// 分类
//			AppTypeCategoryPager mAppTypeCategoryPager = new AppTypeCategoryPager(activity, isPopular);
//			pagers[GAME_TYPE_PAGER_ID] = mAppTypeCategoryPager;
////			pagers[GAME_TYPE_PAGER_ID].getView(); // 需要初始化，原因：page 可以跳转
//
//			// 榜单
//			AppCategoryRankingPager mAppCategoryRankingPager = new AppCategoryRankingPager(activity, isPopular);
//			pagers[GAME_RANK_PAGER_ID] = mAppCategoryRankingPager;
////			pagers[GAME_RANK_PAGER_ID].getView(); // 需要初始化，原因：page 可以跳转
//
//		}
//
//		@Override
//		public void destroyItem(ViewGroup container, int position, Object object) {
//			container.removeView(pagers[position].getView());
//			// container.removeView(container.getChildAt(position));
//		}
//
//		@Override
//		public Object instantiateItem(ViewGroup container, int position) {
//
//			container.addView(pagers[position].getView());
//			return pagers[position].getView();
//		}
//
//		@Override
//		public int getIconResId(int index) {
//			// TODO Auto-generated method stub
//			return ICONS[index];
//		}
//
//		@Override
//		public int getCount() {
//			return pagers.length;
//		}
//
//		@Override
//		public boolean isViewFromObject(View arg0, Object arg1) {
//			return arg0 == arg1;
//		}
//
//		@Override
//		public CharSequence getPageTitle(int position) {
//			int id = ICONS[position % ICONS.length];
//			return activity.getString(id);
//		}
//
//	}
//
//	@Override
//	public void onResume() {
//		pagers[mCurrentPage].onResume();
//		super.onResume();
//	}
//
//	@Override
//	public void onPause() {
//		pagers[mCurrentPage].onPause();
//		super.onPause();
//	}
//
//}
