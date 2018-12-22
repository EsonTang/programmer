package com.prize.music.page;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.SDKUtil;
import com.prize.app.xiami.RequestManager;
import com.prize.music.SearchResult.Watcher;
import com.prize.music.activities.SearchActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.WatchedManager;
import com.prize.music.online.task.SearchSongTask;
import com.prize.music.ui.adapters.SearchSongAdapter;
import com.prize.music.views.ParabolaView;
import com.prize.music.R;
import com.prize.onlinemusibean.SearchSongsResponse;
import com.prize.onlinemusibean.SongDetailInfo;
import com.xiami.sdk.XiamiSDK;

public class SongPager extends BasePager implements Watcher {

	private SearchActivity context;
	private XiamiSDK xiamiSDK;
	private RequestManager requestManager;
	private SearchSongAdapter adapter;
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
	private View noContent = null;

	/** 是否有下一页 */
	private boolean more;

	private int pageIndex = 1;

	private ArrayList<SongDetailInfo> mlist = new ArrayList<SongDetailInfo>();

	private ParabolaView parabolaView;

	public SongPager(SearchActivity activity) {
		super(activity);
		this.context = activity;
		xiamiSDK = new XiamiSDK(activity, SDKUtil.KEY, SDKUtil.SECRET);
		requestManager = RequestManager.getInstance();
		adapter = new SearchSongAdapter(context);
		WatchedManager.registObserver(this);

	}

	@Override
	public View onCreateView() {
		LayoutInflater inflater = LayoutInflater.from(activity);
		View root = inflater.inflate(R.layout.search_result_page_list, null);
		listView = (ListView) root.findViewById(android.R.id.list);

		loading = LayoutInflater.from(context).inflate(
				R.layout.footer_loading_small, null);
		loading.setOnClickListener(null);
		noLoading = LayoutInflater.from(context).inflate(
				R.layout.footer_no_loading, null);

		ViewGroup rootView = (ViewGroup) context.getWindow().getDecorView();
		parabolaView = (ParabolaView) rootView.findViewById(R.id.parabolaView);

		/** 播放歌曲 */
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {

				SongDetailInfo bean = adapter.getItem(position
						- listView.getHeaderViewsCount());
				if (bean == null)
					return;
				MusicUtils.playSongDetailInfo(
						activity,
						bean,
						SongPager.class.getSimpleName(), -1L, mlist,
						Constants.KEY_SONGS);

				// 动画
				ImageView icon_fly = (ImageView) view
						.findViewById(R.id.icon_fly);
				if (parabolaView != null) {
					ImageView bottomView = ((SearchActivity) context)
							.getBottomView();
					parabolaView.setAnimationPara(icon_fly, bottomView);
					if (!parabolaView.isRunning()) {
						parabolaView.showMovie();
					}
				}
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
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastVisiblePosition = listView.getLastVisiblePosition();
		}
	};

	private Handler searchSongHandler = new Handler() {

		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				if (requestManager == null)
					return;
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				SearchSongsResponse beans = gson.fromJson(element,
						SearchSongsResponse.class);
				removeFootView();
				listView.setVisibility(View.VISIBLE);
				isLoadingMore = false;
                
				// 显示
				if (adapter != null && beans.songs != null && beans.songs.size()>0) {
					ArrayList<SongDetailInfo> list = CommonUtils
							.filterUnabelListerSong(beans.songs);
					adapter.setData(list);
					mlist.addAll(list);
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

	}

	private void requestData() {
		SearchSongTask task = new SearchSongTask(xiamiSDK,
				RequestMethods.SEARCH_SONGS, searchSongHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("key", keyWord);
		params.put("page", pageIndex);
		task.execute(params);
		
		removeFootViewNoData();
	}

	/** 设置关键字 */
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
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
	
	private boolean isFootViewNoData = false;
	/**
	 * 添加无数据布局
	 */
	private void addHeadViewNoData() {
		if (!isFootViewNoData) {
			removeFootView();
			removeFootViewNoMore();
			listView.addHeaderView(noContent);
			isFootViewNoData = true;
		}
	}

	/**
	 * 取消无数据提醒
	 */
	private void removeFootViewNoData() {
		if (isFootViewNoData) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				listView.removeHeaderView(noContent);
			}
			isFootViewNoData = false;
		}

	}
	
	@Override
	public void onActivityCreated() {

	}

	@Override
	public String getPageName() {
		return "SongPager";
	}

	@Override
	public void onDestroy() {
		if (searchSongHandler != null) {
			searchSongHandler.removeCallbacksAndMessages(null);
		}
		if (requestManager != null) {
			requestManager = null;
		}
		WatchedManager.unregistObserver(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (adapter != null) {
			adapter.removeDownLoadHandler();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (adapter != null) {
			adapter.setDownlaodRefreshHandle();
		}
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
