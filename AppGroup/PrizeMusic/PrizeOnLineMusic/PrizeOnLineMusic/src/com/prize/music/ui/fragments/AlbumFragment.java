package com.prize.music.ui.fragments;

import java.util.HashMap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.util.SDKUtil;
import com.prize.app.xiami.RequestManager;
import com.prize.music.activities.SingerOnlineActivity;
import com.prize.music.online.task.SingerByTypeTask;
import com.prize.music.page.BasePager.ReloadFunction;
import com.prize.music.ui.adapters.AlbumListAdapter;
import com.prize.music.R;
import com.prize.onlinemusibean.SearchAlbumsResponse;
import com.xiami.sdk.XiamiSDK;

/**
 * 
 **
 * 歌手的专辑列表
 * 
 * @author 彭阳
 * @version V1.0
 */
public class AlbumFragment extends Fragment {

	private RequestManager requestManager;
	private XiamiSDK xiamiSDK;
	private SingerOnlineActivity mCxt;
	// private AllbumShowAdapter adapter;
	private AlbumListAdapter adapter;
	private View root;
	private ListView listView;
	private View reloadView;
	/** 是否有下一页 */
	private boolean more;
	private int pageIndex = 1;
	private int artist_id;

	/** 当前可见的最后位置 */
	private int lastVisiblePosition;
	private boolean isLoadingMore;

	/** 加载更多 */
	private View loading = null;
	private View noLoading;
	private boolean hasFootView = false;
	private boolean isHasMore = true;
	
	public AlbumFragment(){}

	public AlbumFragment(SingerOnlineActivity mCxt, int artist_id) {
		this.mCxt = mCxt;
		requestManager = RequestManager.getInstance();
		xiamiSDK = new XiamiSDK(mCxt, SDKUtil.KEY, SDKUtil.SECRET);
		this.artist_id = artist_id;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.singer_album_show_gridview, container,
				false);
		listView = (ListView) root
				.findViewById(R.id.id_stickynavlayout_innerscrollview);
		reloadView = (View) root.findViewById(R.id.reload_Llyt);
		waitView = (View) root.findViewById(R.id.loading_Llyt_id);
		if (mCxt != null) {  //prize-add- fix bug 27065 -tangzeming-20170112
		loading = LayoutInflater.from(mCxt).inflate(
				R.layout.footer_loading_small, null);
		noLoading = LayoutInflater.from(mCxt).inflate(
				R.layout.footer_no_loading, null);
		adapter = new AlbumListAdapter(mCxt);
		listView.setAdapter(adapter);
		// listView.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// // 专辑详情
		// // UiUtils.gotoAlbumDeatail(mCxt,
		// // adapter.getItem(position).album_id,
		// // R.id.AlbumFragment_container, Constants.KEY_ALBUM);
		// }
		// });
        
		listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));

		loadData();
		}
		return root;
	}

	OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (lastVisiblePosition >= adapter.getCount() - 1
					&& isLoadingMore == false) {
				if (isHasMore) {
					isLoadingMore = true;
					addFootView();
					loadData();
				}else {
					if (!hasFootViewNoMore) {
						addFootViewNoMore();
					}
				}
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastVisiblePosition = listView.getLastVisiblePosition();
		}
	};

	/** 请求专辑 */
	public void loadData() {
		// if (null != adapter && adapter.getCount() == 0 ) {
		requestData();
		// }
	}

	private void requestData() {
		SingerByTypeTask task = new SingerByTypeTask(xiamiSDK,
				RequestMethods.METHOD_ARTIST_ALBUMS, albumHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("artist_id", artist_id);
		params.put("page", pageIndex);
		params.put("limit", 20);
		task.execute(params);
	}

	private Handler albumHandler = new Handler() {
		public void handleMessage(Message msg) {

			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:

				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				SearchAlbumsResponse bean = gson.fromJson(element,
						SearchAlbumsResponse.class);
				removeFootView();
				listView.setVisibility(View.VISIBLE);
				isLoadingMore = false;

				if (adapter != null && bean.albums != null) {
					if (pageIndex == 1) {
						adapter.setData(bean.albums);
					} else {
						adapter.addData(bean.albums);
					}
				}
				if (!bean.more) {
					addFootViewNoMore();
					pageIndex = 1;
					isHasMore = false;
				} else {
					removeFootViewNoMore();
					isHasMore = true;
					pageIndex++;
				}
				// 设置数量
				if (albumsCallBack != null) {
					albumsCallBack.AlbumSetTotal(bean.total);
				}
//				pageIndex++;
				break;
			case RequestResCode.REQUEST_FAILE:
				pageIndex=1;
				removeFootView();
				isLoadingMore = false;
				if (null != adapter && adapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							requestData();
						}
					});
				}
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

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

	private View waitView = null;

	/**
	 * 隐藏等待框
	 */
	public void hideWaiting() {
		if (waitView == null)
			return;
		waitView.setVisibility(View.GONE);
		reloadView.setVisibility(View.GONE);
	}

	/**
	 * 加载失败
	 */
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

	/**
	 * 显示等待框
	 */
	public void showWaiting() {
		if (waitView == null)
			return;
		waitView.setVisibility(View.VISIBLE);
		reloadView.setVisibility(View.GONE);
	}

	/**
	 * 回调
	 */
	public static interface AlbumsCallBack {
		void AlbumSetTotal(int total);
	}

	private AlbumsCallBack albumsCallBack;

	public void setSingerCallBack(AlbumsCallBack albumsCallBack) {
		this.albumsCallBack = albumsCallBack;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if (isVisibleToUser) {
			loadData();
		}
		super.setUserVisibleHint(isVisibleToUser);
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

	private boolean hasFootViewNoMore = false;

	private void removeFootViewNoMore() {
		if (hasFootViewNoMore) {
			listView.removeFooterView(noLoading);
			hasFootViewNoMore = false;
		}
	}
}
