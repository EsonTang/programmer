///*******************************************
// *版权所有©2015,深圳市铂睿智恒科技有限公司
// *
// *内容摘要：
// *当前版本：
// *作	者：
// *完成日期：
// *修改记录：
// *修改日期：
// *版 本 号：
// *修 改 人：
// *修改内容：
//...
// *修改记录：
// *修改日期：
// *版 本 号：
// *修 改 人：
// *修改内容：
// *********************************************/
//
//package com.prize.appcenter.activity;
//
//import android.content.ComponentName;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewTreeObserver.OnGlobalLayoutListener;
//import android.widget.AbsListView;
//import android.widget.AbsListView.OnScrollListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
//import com.prize.app.BaseApplication;
//import com.prize.app.net.NetSourceListener;
//import com.prize.app.net.datasource.base.MoreListData;
//import com.prize.app.util.CommonUtils;
//import com.prize.app.util.MTAUtil;
//import com.prize.app.util.WindowMangerUtils;
//import com.prize.appcenter.R;
//import com.prize.appcenter.ui.actionBar.ActionBarTabActivity;
//import com.prize.appcenter.ui.adapter.AppListAdapter;
//import com.prize.appcenter.ui.datamgr.AppListDataManager;
//import com.prize.appcenter.ui.util.AIDLUtils;
//import com.prize.appcenter.ui.util.ToastUtils;
//import com.prize.appcenter.ui.util.TopicFadingActionBarHelper;
//import com.prize.appcenter.ui.util.UIUtils;
//
///**
// * 类描述：更多列表
// *
// * @author huanglingjun
// * @version 版本
// */
//public class MoreAppListActivity extends ActionBarTabActivity {
//
//	private AppListAdapter adapter;
//	private ListView appListView;
//	private String title;
//	private TopicFadingActionBarHelper mFadingActionBarHelper;
//	private RelativeLayout topic_title_actionbar;
//	private int imgHeight;
//	private int actionBarHeight;
//	private ImageView topic_detail_Iv;
//	private View headView;
//	private String brief;
//	private String cardId;
//	// 无更多内容加载
//	private View noLoading = null;
//	private View loading = null;
//	private boolean hasFootView;
//	private boolean isLoadMore = true;
//	private boolean isFootViewNoMore = true;
//	private int lastVisiblePosition;
//	private AppListDataManager dataManager;
//	private static final int PAGERSIZE = 20;
//	private int currentPager = 1;
//	private int pageIndex;
//	private int pageCount;
//	protected String TAG = "MoreAppListActivity";
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
//		setNeedAddWaitingView(true);
//		if (!BaseApplication.isThird) {
//			// 沉浸式
//			WindowMangerUtils.initStateBar(getWindow(), this);
//		}
//		setContentView(R.layout.activity_more);
//		WindowMangerUtils.changeStatus(getWindow());
//
//		Bundle mBundle = getIntent().getBundleExtra("data");
//		if (mBundle != null) {
//			title = mBundle.getString("title");
//			brief = mBundle.getString("brief");
//			cardId = mBundle.getString("cardId");
//		}
//		findViewById();
//		initActionBar();
//		mFadingActionBarHelper = new TopicFadingActionBarHelper(this,
//				getWindow(), topic_title_actionbar, getResources().getDrawable(
//						R.drawable.actionbar_bg));
//		setListener();
//		requestData();
//	}
//
//	private void setListener() {
//		appListView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				if (position == 0)
//					return;
//				// adapter.onItemClick(position, true);
//				if (CommonUtils.isFastDoubleClick())
//					return;
//				if (adapter.getItem(position - 1) != null) {
////					View shareView = view.findViewById(R.id.game_iv);
//					UIUtils.gotoAppDetail(adapter.getItem(position - 1),
//							adapter.getItem(position - 1).id,
//							MoreAppListActivity.this);
//					MTAUtil.onDetailClick(MoreAppListActivity.this,
//							adapter.getItem(position - 1).name,
//							adapter.getItem(position - 1).packageName);
//				}
////				CardClickDataBean clickDataBean = UpdateDataUtils
////						.getCardClickDataBean(cardId,
////								adapter.getItem(position - 1).packageName,
////								position, 1);
////				if (clickDataBean != null) {
////					CardClickDataDAO.getInstance().insertApp(clickDataBean);
////				}
//			}
//		});
//		OnScrollListener mOnScrollListener = new OnScrollListener() {
//
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//				if (lastVisiblePosition >= appListView.getCount() - 1
//						&& isLoadMore) {
//					isLoadMore = false;
//					if (hasNext()) {
//						addFootView();
//						dataManager.doMoreListPost(cardId, currentPager,
//								PAGERSIZE, TAG);
//					} else {
//						addFootViewNoMore();
//					}
//				}
//			}
//
//			@Override
//			public void onScroll(AbsListView listView, int firstVisibleItem,
//					int visibleItemCount, int totalItemCount) {
//				lastVisiblePosition = appListView.getLastVisiblePosition();
//				if (firstVisibleItem == 0) {
//					if (actionBarHeight != 0 && imgHeight != 0) {
//						float progress = (float) getScrollY()
//								/ (imgHeight - actionBarHeight);
//						if (progress > 1f)
//							progress = 1f;
//						mFadingActionBarHelper
//								.setActionBarAlpha((int) (255 * progress));
//					}
//				} else {
//					mFadingActionBarHelper.setActionBarAlpha(255);
//				}
//			}
//		};
//
//		appListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
//				.getInstance(), true, true, mOnScrollListener));
//
//		topic_title_actionbar.getViewTreeObserver().addOnGlobalLayoutListener(
//				new OnGlobalLayoutListener() {
//
//					@Override
//					public void onGlobalLayout() {
//						// TODO Auto-generated method stub
//						actionBarHeight = topic_title_actionbar.getHeight();
//					}
//				});
//
//		topic_detail_Iv.getViewTreeObserver().addOnGlobalLayoutListener(
//				new OnGlobalLayoutListener() {
//
//					@Override
//					public void onGlobalLayout() {
//						// TODO Auto-generated method stub
//						imgHeight = headView.getHeight();
//					}
//				});
//	}
//
//	private void findViewById() {
//		headView = LayoutInflater.from(this).inflate(
//				R.layout.activity_more_head_view, null);
//		TextView description_Tv = (TextView) headView.findViewById(R.id.description_Tv);
//		TextView title_Tv = (TextView) headView.findViewById(R.id.title_Tv);
//		topic_detail_Iv = (ImageView) headView
//				.findViewById(R.id.topic_detail_Iv);
//		topic_title_actionbar = (RelativeLayout) findViewById(R.id.action_bar_topic);
//		appListView = (ListView) findViewById(android.R.id.list);
//		noLoading = LayoutInflater.from(this).inflate(
//				R.layout.footer_nomore_show, null);
//		loading = LayoutInflater.from(this).inflate(
//				R.layout.footer_loading_small, null);
//
//		adapter = new AppListAdapter(this);
//		appListView.setAdapter(adapter);
//		appListView.addHeaderView(headView);
//		if (adapter == null) {
//			adapter = new AppListAdapter(this);
//		}
//		adapter.setDownlaodRefreshHandle();
//		mToken=AIDLUtils.bindToService(this,this);
//		// adapter.setData(data.apps,cardId);
//		title_Tv.setText(title);
//		description_Tv.setText(brief);
//	}
//
//	protected void initActionBar() {
//		enableSlideLayout(false);
//		ImageButton action_bar_back = (ImageButton) findViewById(R.id.action_bar_back_topic);
//		ImageButton action_bar_search = (ImageButton) findViewById(R.id.action_bar_search_topic);
//		ImageButton action_go_downQueen = (ImageButton) findViewById(R.id.action_go_downQueen_topic);
//
//		action_bar_back
//				.setBackgroundResource(R.drawable.detail_back_white_normal);
//		action_bar_search
//				.setBackgroundResource(R.drawable.icon_search_personpage_nomal);
//		action_go_downQueen
//				.setBackgroundResource(R.drawable.detail_down_white_normal);
//
//		mTitle = (TextView) findViewById(R.id.app_title_Tv);
//		action_bar_back.setOnClickListener(onClickListener);
//		action_bar_search.setOnClickListener(onClickListener);
//		action_go_downQueen.setOnClickListener(onClickListener);
//		mTitle.setOnClickListener(onClickListener);
//		if (!TextUtils.isEmpty(title)) {
//			mTitle.setText(title);
//		}
//	}
//
//	OnClickListener onClickListener = new OnClickListener() {
//
//		@Override
//		public void onClick(View v) {
//			int id = v.getId();
//			switch (id) {
//			case R.id.action_bar_back_topic:
//				onBackPressed();
//				break;
//			case R.id.app_title_Tv:
//				onBackPressed();
//				break;
//			case R.id.action_bar_search_topic:
//				UIUtils.goSearchActivity(MoreAppListActivity.this);
//				break;
//			case R.id.action_go_downQueen_topic:
//				UIUtils.gotoActivity(AppDownLoadQueenActivity.class,
//						MoreAppListActivity.this);
//				break;
//			}
//		}
//	};
//
//	@Override
//	protected void onDestroy() {
//		BaseApplication.cancelPendingRequests(TAG);
//		if (adapter != null) {
//			adapter.removeDownLoadHandler();
//			dataManager.setNullListener();
//		}
//		AIDLUtils.unbindFromService(mToken);
//		super.onDestroy();
//	}
//
//	@Override
//	public String getActivityName() {
//		return null;
//	}
//
//	@Override
//	public void onBack(int what, int arg1, int arg2, Object obj) {
//		hideWaiting();
//		synchronized (MoreAppListActivity.class) {
//			switch (what) {
//			case AppListDataManager.MORE_SUCCESS:
//				if (obj == null) {
//					loadingFailed(new ReloadFunction() {
//						@Override
//						public void reload() {
//							requestData();
//						}
//					});
//					return;
//				}
//				currentPager++;
//				MoreListData data = (MoreListData) obj;
//				pageIndex = data.getPageIndex();
//				pageCount = data.getPageCount();
//				if (data.apps.size() > 0) {
//					if (pageIndex == 1) {
//						adapter.setData(data.apps, cardId);
//					} else if (pageIndex > 1) {
//						adapter.addData(data.apps);
//					}
//				} else if (pageCount == 0) {
//					adapter.clearAll();
//				}
//				removeFootView();
//				break;
//			case AppListDataManager.MORE_FAILURE:
//				Toast.makeText(this, this.getString(R.string.failure),
//						Toast.LENGTH_SHORT).show();
//				break;
//
//			case NetSourceListener.WHAT_NETERR:
//				if (isLoadMore) {
//					loadingFailed(new ReloadFunction() {
//
//						@Override
//						public void reload() {
//							requestData();
//						}
//
//					});
//				} else {
//					ToastUtils.showToast(R.string.net_error);
//				}
//				removeFootView();
//			default:
//				break;
//			}
//			isLoadMore = true;
//		}
//	}
//
//	public int getScrollY() {
//		View c = appListView.getChildAt(0);
//		if (c == null) {
//			return 0;
//		}
//		int firstVisiblePosition = appListView.getFirstVisiblePosition();
//		int top = c.getTop();
//		return -top + firstVisiblePosition * c.getHeight();
//	}
//
//	/**
//	 * 添加无更多加载布局
//	 */
//	private void addFootViewNoMore() {
//		if (isFootViewNoMore) {
//			removeFootView();
//			appListView.addFooterView(noLoading, null, false);
//			isFootViewNoMore = false;
//		}
//	}
//
//	/**
//	 * 添加加载更多
//	 */
//	private void addFootView() {
//		if (hasFootView) {
//			return;
//		}
//		appListView.addFooterView(loading);
//		hasFootView = true;
//	}
//
//	/**
//	 * 移除加载更多
//	 */
//	private void removeFootView() {
//		if (hasFootView && (null != appListView)) {
//			appListView.removeFooterView(loading);
//			hasFootView = false;
//		}
//	}
//
//	private void requestData() {
//		showWaiting();
//		appListView.setAdapter(adapter);
//		if (dataManager == null) {
//			dataManager = new AppListDataManager(this);
//		}
//		dataManager.doMoreListPost(cardId, currentPager, PAGERSIZE, TAG);
//	}
//
//	/**
//	 * 是否有下一页
//	 *
//	 * @return boolean
//	 */
//	public boolean hasNext() {
//		return pageIndex + 1<=pageCount;
//	}
//
//
//	@Override
//	public void onServiceConnected(ComponentName name, IBinder service) {
//		adapter.setDownlaodRefreshHandle();
//	}
//}
