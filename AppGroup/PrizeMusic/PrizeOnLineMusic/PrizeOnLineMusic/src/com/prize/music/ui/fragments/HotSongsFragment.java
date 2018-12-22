package com.prize.music.ui.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.SDKUtil;
import com.prize.app.util.ToastUtils;
import com.prize.app.xiami.RequestManager;
import com.prize.music.activities.SingerOnlineActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.online.task.SingerByTypeTask;
import com.prize.music.page.BasePager.ReloadFunction;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.SongsPlayAdapter;
import com.prize.music.views.GifView;
import com.prize.music.views.ParabolaView;
import com.prize.music.R;
import com.prize.onlinemusibean.SearchSongsResponse;
import com.prize.onlinemusibean.SongDetailInfo;
import com.xiami.sdk.XiamiSDK;

/**
 * 
 **
 * 歌手的歌曲列表
 * 
 * @author 彭阳
 * @version V1.0
 */
public class HotSongsFragment extends Fragment {

	private RequestManager requestManager;
	private XiamiSDK xiamiSDK;
	private SingerOnlineActivity mCxt;
	private SongsPlayAdapter adapter;

	private int artist_id;
	private View root;
	private View reloadView;
	private View waitView = null;
	private ListView listView;
	private View headView;
	/** 是否有下一页 */
	private boolean more;
	private int pageIndex = 1;

	/** 当前可见的最后位置 */
	private int lastVisiblePosition;
	private boolean isLoadingMore;

	/** 加载更多 */
	private View loading = null;
	private boolean hasFootView = false;
	private View forbid_Llyt_id = null;
	TextView type_caution;
	private ParabolaView parabolaView;

	private ArrayList<SongDetailInfo> filterList = new ArrayList<SongDetailInfo>();

	//prize-bug:22143 no zero argument constructor -20160920-pengcancan-start
	public HotSongsFragment() {
	}
	//prize-bug:22143 no zero argument constructor -20160920-pengcancan-start

	public HotSongsFragment(SingerOnlineActivity mCxt, int artist_id) {
		requestManager = RequestManager.getInstance();
		this.mCxt = mCxt;
		xiamiSDK = new XiamiSDK(mCxt, SDKUtil.KEY, SDKUtil.SECRET);
		this.artist_id = artist_id;
		loadData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.singer_play_song_list, container,
				false);
		headView = inflater.inflate(R.layout.onlineartist_shuffle_all, null);
		loading = inflater.inflate(R.layout.footer_loading_small, null);
		forbid_Llyt_id = (View) root.findViewById(R.id.forbid_Llyt_id);
		type_caution = (TextView) root.findViewById(R.id.type_caution);
		findById();
		init();
		return root;
	}

	private void init() {
		adapter = new SongsPlayAdapter(mCxt);
		listView.setAdapter(adapter);
		listView.addHeaderView(headView);
		listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				if (adapter != null && adapter.getCount() > 0) {//prize-public-bug:20959 monkey-pengcancan-20160906
					if (view.getId() != R.id.edit_Llyt) {
						int pos = position - listView.getHeaderViewsCount();
						SongDetailInfo bean = null;
						if (pos >= 0) {
							bean = adapter.getItem(position - listView.getHeaderViewsCount());
						}
						if (bean == null)
							return;
						if (bean.permission != null && !bean.permission.available) {
							ToastUtils.showToast(R.string.listen_is_forbidden);
							return;
						}
						MusicUtils.playSongDetailInfo(getActivity(), bean, HotSongsFragment.class.getSimpleName(), -1L,
								filterList, Constants.KEY_SONGS);
					}
					// 动画
					ImageView icon_fly = (ImageView) view.findViewById(R.id.icon_fly);
					if (parabolaView != null) {
						ImageView bottomView = ((SingerOnlineActivity) getActivity()).getBottomView();
						parabolaView.setAnimationPara(icon_fly, bottomView);
						if (!parabolaView.isRunning()) {
							parabolaView.showMovie();
						}
					} 
				}
			}
		});
		// 全部播放
		headView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (filterList == null || filterList.size() <= 0) {
					return;
				}
				MusicUtils.playSongDetailInfo(
						getActivity(),
						filterList.get(0),
						HotSongsFragment.class.getSimpleName(), -1L,
						filterList, Constants.KEY_SONGS);
			}
		});

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

	private void findById() {
		reloadView = (View) root.findViewById(R.id.reload_Llyt);
		waitView = (View) root.findViewById(R.id.loading_Llyt_id);
		listView = (ListView) root
				.findViewById(R.id.id_stickynavlayout_innerscrollview);

		ViewGroup rootView = (ViewGroup) getActivity().getWindow()
				.getDecorView();
		parabolaView = (ParabolaView) rootView.findViewById(R.id.parabolaView);
	}

	/** 请求热门歌曲 */
	public void loadData() {
		/*
		 * if (!isLoadingMore) { if(adapter !=null&&adapter.getCount()>0){
		 * return; } }
		 */
		requestData();
	}

	private void requestData() {
		SingerByTypeTask task = new SingerByTypeTask(xiamiSDK,
				RequestMethods.METHOD_ARTIST_HOT_SONGS, hotsongsHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("artist_id", artist_id);
		params.put("page", pageIndex);
		// if(artist_id==1260){//周杰伦
		params.put("limit", 50);
		// }else{
		// params.put("limit", 20);
		// }
		task.execute(params);
	}

	private Handler hotsongsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				SearchSongsResponse bean = gson.fromJson(element,
						SearchSongsResponse.class);

				removeFootView();
				if(listView==null)
					return;
				listView.setVisibility(View.VISIBLE);
				isLoadingMore = false;
				JLog.i("0000", "bean.songs==" + bean.songs.size()+"---"+bean.more+"--pageIndex="+pageIndex);
				// 设置数量
				more = bean.more;
				if (adapter != null && bean.songs != null) {
					if(pageIndex==1){
						filterList.clear();
						filterList = CommonUtils
								.filterUnabelListerSong(bean.songs);
					}else{
						filterList.addAll(CommonUtils
								.filterUnabelListerSong(bean.songs));
					}
					pageIndex++;
					if (bean.more) {
						loadData();
						return;
					}
					if ((filterList == null | filterList.size() <= 0)
							&& forbid_Llyt_id != null&&type_caution !=null) {
						type_caution.setText(R.string.forbid_singer);
						forbid_Llyt_id.setVisibility(View.VISIBLE);
						return;
					}
					hideWaiting();
					if (hotSongsCallBack !=null) {
						hotSongsCallBack.AlbumSetHotSongsTotal(filterList.size());
					}
					adapter.setData(filterList);
				} 
				break;
			case RequestResCode.REQUEST_FAILE:
				pageIndex=1;
				removeFootView();
				isLoadingMore = false;
				if (null != adapter && adapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							showWaiting();
							loadData();
						}
					});
				}
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				pageIndex=1;
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

	/**
	 * 隐藏等待框
	 */
	public void hideWaiting() {
		if (waitView == null)
			return;
		waitView.setVisibility(View.GONE);
		GifView gifWaitingView = (GifView) waitView
				.findViewById(R.id.gif_waiting);
		gifWaitingView.setPaused(false);
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
		GifView gifWaitingView = (GifView) waitView
				.findViewById(R.id.gif_waiting);
		gifWaitingView.setPaused(true);
		reloadView.setVisibility(View.GONE);
	}

	/**
	 * 回调
	 */
	public static interface HotSongsCallBack {
		void AlbumSetHotSongsTotal(int total);
	}

	private HotSongsCallBack hotSongsCallBack;

	public void setSingerCallBack(HotSongsCallBack hotSongsCallBack) {
		this.hotSongsCallBack = hotSongsCallBack;
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
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if (isVisibleToUser) {
			if (adapter != null) {
				adapter.setDownlaodRefreshHandle();
			}
		} else {
			if (adapter != null) {
				adapter.removeDownLoadHandler();
			}
		}
		super.setUserVisibleHint(isVisibleToUser);
	}

	@Override
	public void onStart() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		filter.addAction(ApolloService.PLAYSTATE_CHANGED);
		getActivity().registerReceiver(mMediaStatusReceiver, filter);
		super.onStart();
	}

	@Override
	public void onStop() {
		getActivity().unregisterReceiver(mMediaStatusReceiver);
		super.onStop();
	}

	/**
	 * p 更新数据在需要的时候
	 */
	private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (listView != null && adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}
	};
}
