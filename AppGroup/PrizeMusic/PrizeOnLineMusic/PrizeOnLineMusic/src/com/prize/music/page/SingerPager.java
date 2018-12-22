package com.prize.music.page;

import java.util.HashMap;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.util.JLog;
import com.prize.app.util.SDKUtil;
import com.prize.app.xiami.RequestManager;
import com.prize.music.SearchResult.Watcher;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.helpers.utils.WatchedManager;
import com.prize.music.online.task.SearchSingerTask;
import com.prize.music.ui.adapters.SearchSingerAdapter;
import com.prize.music.R;
import com.prize.onlinemusibean.SearchArtistsResponse;
import com.xiami.sdk.XiamiSDK;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class SingerPager extends BasePager implements Watcher {

	private FragmentActivity context;
	private XiamiSDK xiamiSDK;
	private SearchSingerAdapter adapter;
	private String keyWord;
	private ListView listView;

	/** 当前可见的最后位置 */
	private int lastVisiblePosition;
	private boolean isLoadingMore;

	/** 加载更多 */
	private View loading = null;
	private boolean hasFootView = false;

	/** 无更多内容加载 */
	private View noLoading = null;
	private boolean hasFootViewNoMore = false;

	/** 是否有下一页 */
	private boolean more;

	private int pageIndex = 1;

	public SingerPager(FragmentActivity activity) {
		super(activity);
		this.context = activity;
		requestManager = RequestManager.getInstance();
		xiamiSDK = new XiamiSDK(activity, SDKUtil.KEY, SDKUtil.SECRET);
		adapter = new SearchSingerAdapter(context);
		WatchedManager.registObserver(this);
	}

	@Override
	public View onCreateView() {
		LayoutInflater inflater = LayoutInflater.from(activity);
		View root = inflater.inflate(R.layout.search_result_page_list, null);
		listView = (ListView) root.findViewById(android.R.id.list);

		loading = LayoutInflater.from(context).inflate(
				R.layout.footer_loading_small, null);
		noLoading = LayoutInflater.from(context).inflate(
				R.layout.footer_no_loading, null);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				UiUtils.JumpToSingerOnlineActivity(context,
						adapter.getItem(position),
						adapter.getItem(position).artist_id);
			}
		});
		listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));
		addFootView();
		listView.setAdapter(adapter);
		removeFootView();
		return root;
	}

	OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

			if (lastVisiblePosition >= adapter.getCount() - 1
					&& isLoadingMore == false) {
				// 分页显示
				if (more) {
					isLoadingMore = true;
					addFootView();
					pageIndex++;
					loadData();
				} 
//				else {
//					if (!hasFootViewNoMore) {
//						addFootViewNoMore();
//					}
//				}
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastVisiblePosition = listView.getLastVisiblePosition();
		}
	};

	private Handler searchSingerHandler = new Handler() {

		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
                
				if (requestManager == null)
					return;
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				SearchArtistsResponse beans = gson.fromJson(element,
						SearchArtistsResponse.class);

				removeFootView();
				listView.setVisibility(View.VISIBLE);
				isLoadingMore = false;

				// 显示
				if (adapter != null && beans.artists != null && beans.artists.size()>0) {
					adapter.setData(beans.artists);
					((TextView) noLoading.findViewById(R.id.caution_tv)).setText(R.string.reach_nomore);
				}else{
					((TextView) noLoading.findViewById(R.id.caution_tv)).setText(R.string.reach_nodata);
				}
				more = beans.more;
				if (!beans.more) {
					addFootViewNoMore();
				} else {
					removeFootViewNoMore();
				}
				break;
			case RequestResCode.REQUEST_FAILE:
				removeFootView();
				isLoadingMore = false;
				if (null != adapter && adapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							loadData();
						}
					});
				}
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	@Override
	public void loadData() {
		if (!isLoadingMore) {
			if (adapter != null && adapter.getCount() > 0) {
				return;
			}
		}
		requestData();
		removeFootViewNoMore();
	}

	private void requestData() {
		SearchSingerTask task = new SearchSingerTask(xiamiSDK,
				RequestMethods.SEARCH_ARTISTS, searchSingerHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("key", keyWord);
		params.put("page", pageIndex);
		task.execute(params);
	}

	/**
	 * 取消加载更多
	 */
	private void removeFootView() {
		if (hasFootView) {
			listView.removeFooterView(loading);
			hasFootView = false;
		}
	}

	/**
	 * 加载更多
	 */
	private void addFootView() {
		listView.addFooterView(loading);
		hasFootView = true;
	}

	/**
	 * 添加无更多加载
	 */
	private void addFootViewNoMore() {
		if(!hasFootViewNoMore) {
			listView.addFooterView(noLoading, null, false);
		}
		hasFootViewNoMore = true;
	}
	
	private void removeFootViewNoMore() {
		if (hasFootViewNoMore) {
			listView.removeFooterView(noLoading);
			hasFootViewNoMore = false;
		}
	}

	@Override
	public void onActivityCreated() {

	}

	@Override
	public String getPageName() {
		return "SingerPager";
	}

	@Override
	public void onDestroy() {
		if (searchSingerHandler != null) {
			searchSingerHandler.removeCallbacksAndMessages(null);
		}
		if (requestManager != null) {
			requestManager = null;
		}
		WatchedManager.unregistObserver(this);
	}

	/** 设置关键字 */
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	@Override
	public void updateNotify(String keyWord) {
		if (this.keyWord != keyWord) {
			adapter.clearData();
			this.keyWord = keyWord;
			requestData();
		}
	}
}
