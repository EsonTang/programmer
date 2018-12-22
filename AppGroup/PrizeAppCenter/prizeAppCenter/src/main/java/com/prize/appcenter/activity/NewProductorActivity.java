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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.beans.Category;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarTabActivity;
import com.prize.appcenter.ui.adapter.NewProductListAdapter;
import com.prize.appcenter.ui.datamgr.NewProductManager;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 ** 
 * 新品
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class NewProductorActivity extends ActionBarTabActivity {
	private ListView mListView;
	private NewProductListAdapter mNewProductListAdapter;
	private NewProductManager mNewProductManager;
	private boolean hasFootView = false;
	// 加载更多
	private View loading = null;
	// 无更多内容加载
	private View noLoading = null;
	private boolean hasFootViewNoMore;

	// 当前可见的最后位置
	private int lastVisiblePosition;
	protected final String TAG = "NewProductorActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setNeedAddWaitingView(true);
		setContentView(R.layout.activity_game_ranking);
		WindowMangerUtils.changeStatus(getWindow());
		String title = getIntent().getStringExtra("title");
		if (!TextUtils.isEmpty(title)) {
			super.setTitle(title);
		}
		findViewById();
		init();

		setListener();

	}

	private void setListener() {

		mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));

	}

	OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// selecteItem.put(currentTitle,
			// listView.getFirstVisiblePosition());
			if (lastVisiblePosition >= mNewProductListAdapter.getCount() - 1
					&& isLoadingMore == false) {
				// 分页显示
				if (mNewProductManager.isListNextPage()) {
					isLoadingMore = true;
					addFootView();
					mNewProductManager.getRankingListData(TAG);
					// doListRequest = dataManager.doListGameRequest(true);
					// MobclickAgent.onEvent(
					// MainApplication.curContext,
					// Constants.EVT_LOAD_MORE,
					// Constants.EVT_P_MAXPAGE
					// + dataManager.getCurPageId()
					// + Constants.EVT_P_LISTCODE + listCode);
				} else {
					if (!hasFootViewNoMore) {
						addFootViewNoMore();
					}
				}
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastVisiblePosition = mListView.getLastVisiblePosition();
		}
	};

	private void init() {
		mToken = AIDLUtils.bindToService(this, this);
		View headView = LayoutInflater.from(this).inflate(
				R.layout.head_new_hot, null);
		ImageView topic_detail_Iv = (ImageView) headView
				.findViewById(R.id.topic_detail_Iv);
//		topic_detail_Iv.setBackgroundResource(R.drawable.head_bg_new_product);
		loading = LayoutInflater.from(this).inflate(
				R.layout.footer_loading_small, null);
		noLoading = LayoutInflater.from(this).inflate(
				R.layout.footer_no_loading, null);
		if (mNewProductListAdapter == null) {
			mNewProductListAdapter = new NewProductListAdapter(this);
			mNewProductListAdapter.setDownlaodRefreshHandle();
		}
		mListView.setAdapter(mNewProductListAdapter);
		mListView.addHeaderView(headView);
		requestData();
	}

	private void findViewById() {

		mListView = (ListView) findViewById(android.R.id.list);
	}

	@Override
	public String getActivityName() {
		return "NewProductorActivity";
	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {
		hideWaiting();
		switch (what) {
		case NewProductManager.GAME_SUCCESS_RANKING_LIST_MY:// 请求成共返回
			PrizeAppsTypeData data = (PrizeAppsTypeData) obj;
			removeFootView();
			isLoadingMore = false;
			if (mNewProductListAdapter != null) {
				ArrayList<Category> list = generateHeaderId(data.apps);
				mNewProductListAdapter.addData(list);
			}
			break;

		case NewProductManager.GAME_FAILE_RANGKING_LIST:
			removeFootView();
			isLoadingMore = false;
			ToastUtils.showErrorToast(R.string.toast_net_error);
			if (mNewProductListAdapter != null) {
				mNewProductListAdapter.notifyDataSetChanged();
			}
			break;
		case NetSourceListener.WHAT_NETERR:
			loadingFailed(new ReloadFunction() {

				@Override
				public void reload() {
					requestData();
				}

			});
			removeFootView();
			break;
		}

	}

	private void requestData() {
		showWaiting();
		if (mNewProductManager == null) {
			mNewProductManager = new NewProductManager(this);
		}
		mNewProductManager.getRankingListData(TAG);

	}

	private boolean isLoadingMore;

	/**
	 * 取消加载更多
	 */
	private void removeFootView() {
		if (hasFootView) {
			mListView.removeFooterView(loading);
			hasFootView = false;
		}

	}

	/**
	 * 加载更多
	 */
	private void addFootView() {
		mListView.addFooterView(loading);
		hasFootView = true;
	}

	/**
	 * 添加无更多加载
	 */
	private void addFootViewNoMore() {
		mListView.addFooterView(noLoading, null, false);
		hasFootViewNoMore = true;
	}

	/**
	 * 对GridView的Item生成HeaderId, 根据图片的添加时间的年、月、日来生成HeaderId 年、月、日相等HeaderId就相同
	 * 
	 * @param nonHeaderIdList
	 * @return
	 */
	protected ArrayList<Category> generateHeaderId(
			List<AppsItemBean> nonHeaderIdList) {
		Map<String, Category> mHeaderIdMap = new HashMap<String, Category>();
		// int mHeaderId = 1;
		ArrayList<Category> hasHeaderIdList = new ArrayList<Category>();

		for (ListIterator<AppsItemBean> it = nonHeaderIdList.listIterator(); it
				.hasNext();) {
			AppsItemBean mGridItem = it.next();
			String ymd = mGridItem.updateTime;
			Category category = null;
			if (!mHeaderIdMap.containsKey(ymd)) {
				category = new Category(mGridItem.updateTime);
				mHeaderIdMap.put(ymd, category);
				// mHeaderId++;
				category.addItem(mGridItem);
				hasHeaderIdList.add(category);
			} else {
				mHeaderIdMap.get(ymd).addItem(mGridItem);
			}
		}
		Collections.sort(hasHeaderIdList, this.nameComparator);
		return hasHeaderIdList;
	}

	private final Comparator<Category> nameComparator = new Comparator<Category>() {

		@Override
		public int compare(Category lhs, Category lhs2) {
			return lhs2.getmCategoryName().compareTo(lhs.getmCategoryName());
		}
	};

	public void onDestroy() {
		super.onDestroy();
		BaseApplication.cancelPendingRequests(TAG);
		if (mNewProductManager != null) {
			mNewProductManager.setNullListener();
		}
		if (mNewProductListAdapter != null) {
			mNewProductListAdapter.removeDownLoadHandler();
		}

		AIDLUtils.unbindFromService(mToken);
	}

	@Override
	public void onResume() {
		if (mNewProductListAdapter != null) {
			mNewProductListAdapter.setIsActivity(true);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if (mNewProductListAdapter != null) {
			mNewProductListAdapter.setIsActivity(false);
		}
		super.onPause();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		if (!mNewProductListAdapter.setDownlaodRefreshHandle()) {
			mNewProductListAdapter.setDownlaodRefreshHandle();
		}
	}

}
