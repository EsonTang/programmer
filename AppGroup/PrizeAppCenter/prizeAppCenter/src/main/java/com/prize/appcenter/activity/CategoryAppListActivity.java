package com.prize.appcenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.GameListAdapter;
import com.prize.appcenter.ui.datamgr.ListForSubClassDataManager;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;

import java.util.ArrayList;

/**
 ** 
 * 根据分类跳转的app列表或者开发者列表
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class CategoryAppListActivity extends ActionBarNoTabActivity {
	private final String TAG = "CategoryAppListActivity";
	private String id;
	private ListForSubClassDataManager manager;
	private GameListAdapter mAdapter;
	private ListView gameListView;
	// 无更多内容加载
	private View noLoading = null;
	private View loading = null;
	private boolean hasFootView;
	private boolean isFootViewNoMore = true;
	private String developer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String title = null;
		if (null != intent) {
			id = intent.getStringExtra("id");
			title = intent.getStringExtra("title");
			developer = intent.getStringExtra("developer");
		}
		setNeedAddWaitingView(true);
		setContentView(R.layout.activity_main_home);
		WindowMangerUtils.changeStatus(getWindow());
		if (null != title) {
			setTitle(title);
		} else if (developer != null) {
			setTitle(developer);
		} else {
			setTitle(R.string.app_name);
		}
		findViewById();
		init();
		setListener();
		requestData();
	}

	private void init() {
		LayoutInflater inflater = LayoutInflater.from(this);
		noLoading = inflater.inflate(R.layout.footer_nomore_show, null);
		loading = inflater.inflate(R.layout.footer_loading_small, null);

	}

	private int lastVisiblePosition;
	private boolean isLoadMore = true;

	private void setListener() {

		gameListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (mAdapter.getItem(position) != null) {
//					View shareView = view.findViewById(R.id.game_iv);
					UIUtils.gotoAppDetail(mAdapter.getItem(position),
							mAdapter.getItem(position).id,
							CategoryAppListActivity.this);
					MTAUtil.onDetailClick(CategoryAppListActivity.this,mAdapter.getItem(position).name,
							mAdapter.getItem(position).packageName);
				}

			}
		});

		gameListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));

	}

	OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (lastVisiblePosition >= gameListView.getCount() - 1
					&& isLoadMore) {
				isLoadMore = false;
				if (manager.hasNextPage()) {
					addFootView();
					manager.getRecommandList(TAG);
					// MobclickAgent.onEvent(
					// MainApplication.curContext,
					// Constants.EVT_LOAD_MORE,
					// Constants.EVT_P_MAXPAGE
					// + homeDataManager.getGameListCurPage()
					// + Constants.EVT_P_LISTCODE
					// + "HomeGameList");
				} else {
					addFootViewNoMore();
				}
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastVisiblePosition = gameListView.getLastVisiblePosition();
		}
	};

	private void findViewById() {
		gameListView = (ListView) findViewById(android.R.id.list);
	}

	private void requestData() {
		showWaiting();
		if (mAdapter == null) {
			mAdapter = new GameListAdapter(this,null,null);
			mAdapter.setDownlaodRefreshHandle();
		}
		gameListView.setAdapter(mAdapter);
		if (manager == null) {
			manager = new ListForSubClassDataManager(this, id, developer);
		}
		manager.getRecommandList(TAG);

	}

	/**
	 * 添加无更多加载布局
	 */
	private void addFootViewNoMore() {
		if (isFootViewNoMore) {
			removeFootView();
			gameListView.addFooterView(noLoading, null, false);
			isFootViewNoMore = false;
		}
	}

	/**
	 * 添加加载更多
	 */
	private void addFootView() {
		if (hasFootView) {
			return;
		}
		gameListView.addFooterView(loading);
		hasFootView = true;
	}

	/**
	 * 移除加载更多
	 */
	private void removeFootView() {
		if (hasFootView && (null != gameListView)) {
			gameListView.removeFooterView(loading);
			hasFootView = false;
		}
	}

	public void onDestroy() {
		super.onDestroy();
		if (manager != null) {
			manager.setNullListener();
		}
		if (mAdapter != null) {
			mAdapter.removeDownLoadHandler();
		}
	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {
		hideWaiting();
		switch (what) {
		case ListForSubClassDataManager.WHAT_SUCESS_LIST:

			ArrayList<AppsItemBean> gameList =CommonUtils.filterResData(((PrizeAppsTypeData) obj).apps,manager.isFirstPage(),5);
			mAdapter.addData(gameList);
			// modify huanglingjun 2015-12-19 必须注销不然每次分页加载完都会闪现已经到底view
			// if (!manager.hasNextPage()) {
			// addFootViewNoMore();
			// }
			isLoadMore = true;
			break;

		case ListForSubClassDataManager.WHAT_FAILED_LIST:
			ToastUtils.showToast(R.string.no_data);

			break;

		case NetSourceListener.WHAT_NETERR:
			if (null != mAdapter && mAdapter.getCount() == 0) {
				loadingFailed(new ReloadFunction() {

					@Override
					public void reload() {
						requestData();
					}

				});
			} else {
				ToastUtils.showToast(R.string.net_error);
				isLoadMore = true;
			}
			break;
		}
		removeFootView();

	}

	@Override
	public String getActivityName() {
		return "CategoryAppListActivity";
	}

	@Override
	protected void onResume() {
		if (mAdapter != null) {
			mAdapter.setIsActivity(true);
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (mAdapter != null) {
			mAdapter.setIsActivity(false);
		}
		super.onPause();
	}
}
