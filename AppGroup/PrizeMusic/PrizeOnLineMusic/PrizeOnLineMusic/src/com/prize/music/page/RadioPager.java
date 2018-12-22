package com.prize.music.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.os.AsyncTask.Status;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.ToastUtils;
import com.prize.music.activities.MainActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.online.task.BannerTask;
import com.prize.music.ui.adapters.RadioCategoryAdapter;
import com.prize.music.ui.adapters.RadioListAdapter;
import com.prize.music.views.ParabolaView;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.response.CategoryResponse;
import com.prize.onlinemusibean.response.RadioResponse;
import com.prize.onlinemusibean.response.SceneSongsResponse;
import com.prize.onlinemusibean.response.SceneSongsResponse.SceneSubRes;
import com.prize.onlinemusibean.response.XiamicaiResponse;

/**
 **
 * 首页电台界面
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RadioPager extends BasePager {
	private final String TAG = "RadioPager";
	/** 滚动区 */
	/** 推荐位（eg:装机必备，热门主题，上线新品等） */
	private ListView radio_ListView = null;
	/** 推荐应用游戏列表 **/
	private ListView category_list;

	RadioCategoryAdapter mRadioCategoryAdapter;
	RadioListAdapter mRadioListAdapter;
	private int mCurrentCategory_id = -1;
	private int mCurrentPage = 1;
	private FragmentActivity context;
	private boolean isHasMore = true;
	private ParabolaView parabolaView;
	private TextView title_Tv;
	private TextView title_inView_Tv;
	boolean isNeedPlay = false;

	// ArrayList<RadioItemBean> data = new ArrayList<RadioItemBean>();

	public RadioPager(FragmentActivity activity) {
		super(activity);
		this.context = activity;
		mRadioCategoryAdapter = new RadioCategoryAdapter(activity);
		mRadioListAdapter = new RadioListAdapter(activity);
		setNeedAddWaitingView(true);
		ViewGroup rootView = (ViewGroup) context.getWindow().getDecorView();
		parabolaView = (ParabolaView) rootView.findViewById(R.id.parabolaView1);
	}

	public void onActivityCreated() {
	}

	@Override
	/**
	 * 初始化界面
	 */
	public View onCreateView() {
		LayoutInflater inflater = LayoutInflater.from(context);
		View root = inflater.inflate(R.layout.radio_page_layout, null);
		View headerView = inflater.inflate(R.layout.header_radio_layout, null);
		loading = LayoutInflater.from(activity).inflate(
				R.layout.footer_loading_small, null);
		noLoading = LayoutInflater.from(activity).inflate(
				R.layout.footer_no_loading, null);
		title_Tv = (TextView) headerView.findViewById(R.id.title_Tv);
		title_inView_Tv = (TextView) headerView.findViewById(R.id.title_inView_Tv);
		headerView.findViewById(R.id.xiamicai_Rlyt).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (ClientInfo.networkType == ClientInfo.NONET) {
							ToastUtils.showToast(R.string.net_error);
							return;
						}
						ImageView icon_fly = (ImageView) v
								.findViewById(R.id.icon_fly);
						requestXiamiCaiData();
						if (parabolaView != null) {
							ImageView bottomView = null;
							if (context instanceof MainActivity) {
								bottomView = ((MainActivity) context)
										.getBottomView();
							}
							parabolaView.setAnimationPara(icon_fly, bottomView);
							if (!parabolaView.isRunning()) {
								parabolaView.showMovie();
							}
						}
					}
				});
		headerView.findViewById(R.id.scene_Rlyt).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (ClientInfo.networkType == ClientInfo.NONET) {
							ToastUtils.showToast(R.string.net_error);
							return;
						}
						isNeedPlay=true;
						ImageView icon_fly = (ImageView) v
								.findViewById(R.id.icon2_fly);
						requestSceneneSongsData();
						if (parabolaView != null) {
							ImageView bottomView = null;
							if (context instanceof MainActivity) {
								bottomView = ((MainActivity) context)
										.getBottomView();
							}
							parabolaView.setAnimationPara(icon_fly, bottomView);
							if (!parabolaView.isRunning()) {
								parabolaView.showMovie();
							}
						}
					}
				});
		category_list = (ListView) root.findViewById(R.id.category_list);
		category_list.setAdapter(mRadioCategoryAdapter);
		radio_ListView = (ListView) root.findViewById(R.id.radio_ListView);
		radio_ListView.addHeaderView(headerView);
		radio_ListView.setAdapter(mRadioListAdapter);
		radio_ListView.setOnScrollListener(new PauseOnScrollListener(
				ImageLoader.getInstance(), true, true, mOnScrollListener));

		setListener();

		return root;
	}

	private void setListener() {

		category_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mRadioCategoryAdapter.getItem(position) != null) {
					if (mCurrentCategory_id != mRadioCategoryAdapter
							.getItem(position).category_id) {
						mCurrentCategory_id = mRadioCategoryAdapter
								.getItem(position).category_id;
						mCurrentPage = 1;
						mRadioCategoryAdapter.setSelectPostion(position);
						InitRadioListData();

					}

				}

			}
		});

	}

	OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:
				if (lastVisiblePosition >= view.getAdapter().getCount() - 1
				&& !isLoadingMore) {
					if (isHasMore) {
						isLoadingMore=true;					
						addFootView();
						InitRadioListData();
					} else {
						if (!hasFootViewNoMore) {
							addFootViewNoMore();
						}
					}
				}
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastVisiblePosition = view.getLastVisiblePosition();
		}
	};
	private Handler bannerHandler = new Handler() {

		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				CategoryResponse res = gson.fromJson(element,
						CategoryResponse.class);
				mRadioCategoryAdapter.setData(res.categories);
				mCurrentCategory_id = res.categories.get(0).category_id;

				InitRadioListData();
				requestSceneneSongsData();
				break;
			case RequestResCode.REQUEST_FAILE:
				loadingFailed(new ReloadFunction() {

					@Override
					public void reload() {
						loadData();
					}
				});
				break;
			case RequestResCode.REQUEST_EXCEPTION:
//				loadingFailed(new ReloadFunction() {
//
//					@Override
//					public void reload() {
//
//					}
//				});
				break;
			}
		};
	};
	private Handler radioListHandler = new Handler() {
		public void handleMessage(Message msg) {
			removeFootView();
			removeFootViewNoMore();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				isLoadingMore=false;
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				RadioResponse res = gson.fromJson(element, RadioResponse.class);
				if (mCurrentPage == 1) {
					mRadioListAdapter.setData(res.radios);
					radio_ListView.setSelection(0);
				} else {
					mRadioListAdapter.addData(res.radios);
				}
				if (!res.more) {
					addFootViewNoMore();
					mCurrentPage = 1;
					isHasMore = false;
				} else {
					isHasMore = true;
					mCurrentPage++;
					removeFootViewNoMore();
				}
				break;
			case RequestResCode.REQUEST_FAILE:
				isLoadingMore=false;
				if(ClientInfo.networkType==ClientInfo.NONET){
					ToastUtils.showToast(R.string.net_error);
				}
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				isLoadingMore=false;
				break;
			}
		};
	};

	public void loadData() {
		if(mRadioCategoryAdapter.getCount()>0){
			return;
		}
		BannerTask task = new BannerTask(xiamiSDK,
				RequestMethods.RADIO_CATEGORIES, bannerHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		task.execute(params);

	}
	protected void InitRadioListData() {
		if(task !=null&&task.getStatus()==Status.RUNNING){
			task.cancel(true);
			radioListHandler.removeCallbacksAndMessages(null);
		}
		 task = new BannerTask(xiamiSDK, RequestMethods.RADIO_LIST,
				radioListHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("category_id", mCurrentCategory_id);
		params.put("limit", 20);
		params.put("page", mCurrentPage);
		task.execute(params);

	}

	@Override
	public void onDestroy() {
		if (radioListHandler != null) {
			radioListHandler.removeCallbacksAndMessages(null);
		}
		if (bannerHandler != null) {
			bannerHandler.removeCallbacksAndMessages(null);
		}
		if (sceneSongHandler != null) {
			sceneSongHandler.removeCallbacksAndMessages(null);
		}
	}

	@Override
	public String getPageName() {
		return "RadioPager";
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * 取消加载更多
	 */
	private void removeFootView() {
		if (hasFootView) {
			radio_ListView.removeFooterView(loading);
			hasFootView = false;
		}

	}

	/**
	 * 加载更多
	 */
	private void addFootView() {
		removeFootViewNoMore();
		radio_ListView.addFooterView(loading);
		hasFootView = true;
	}

	/**
	 * 添加无更多加载
	 */
	private void addFootViewNoMore() {
		removeFootView();
		radio_ListView.addFooterView(noLoading, null, false);
		hasFootViewNoMore = true;
	}

	private void removeFootViewNoMore() {
		if (hasFootViewNoMore) {
			radio_ListView.removeFooterView(noLoading);
			hasFootViewNoMore = false;
		}
	}

	// 加载更多
	private View loading = null;
	private boolean hasFootView = false;

	// 无更多内容加载
	private View noLoading = null;
	private boolean hasFootViewNoMore;

	// 当前可见的最后位置
	private int lastVisiblePosition;
	private boolean isLoadingMore;
	protected XiamicaiResponse res;
	protected SceneSongsResponse mSceneSongsResponse;
	private BannerTask task;

	void requestSceneneSongsData() {
		if (mSceneSongsResponse != null && mSceneSongsResponse.list != null
				&& mSceneSongsResponse.list.size() > 0) {
			ArrayList<SongDetailInfo> list = CommonUtils
					.filterUnabelListerSong(mSceneSongsResponse.list.get(0).songs);
			Random random = new Random();
			int index = random.nextInt(list.size());
			MusicUtils.playSongDetailInfo(
					activity,
					list.get(index),
					RadioPager.class.getSimpleName(), -1L, list,
					Constants.KEY_RADIO_SCENE); //午后小调
			isNeedPlay=false;
			return;
		}
		BannerTask	 task = new BannerTask(xiamiSDK,
				RequestMethods.RECOMMEND_SCENE_SONGS, sceneSongHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		task.execute(params);
	}

	void requestXiamiCaiData() {
		if (res != null && res.songs != null && res.songs.size() > 0) {
			ArrayList<SongDetailInfo> list = CommonUtils
					.filterUnabelListerSong(res.songs);
			Random random = new Random();
			int index = random.nextInt(list.size());
			MusicUtils.playSongDetailInfo(
					activity,
					list.get(index),
					RadioPager.class.getSimpleName(), -1L, list,
					Constants.KEY_RADIO_GUESS_YOU_LIKE);
			return;
		}
		BannerTask task = new BannerTask(xiamiSDK, RequestMethods.RADIO_GUESS,
				radioGuessHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		task.execute(params);
	}

	private Handler radioGuessHandler = new Handler() {
		public void handleMessage(Message msg) {
			// radio_GridView.onRefreshComplete();
			removeFootView();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				res = gson.fromJson(element, XiamicaiResponse.class);
				ArrayList<SongDetailInfo> list = CommonUtils
						.filterUnabelListerSong(res.songs);
				Random random = new Random();
				int index = random.nextInt(list.size());
				MusicUtils.playSongDetailInfo(
						activity,
						list.get(index),
						RadioPager.class.getSimpleName(), -1L, list,
						Constants.KEY_RADIO_GUESS_YOU_LIKE); //猜你喜欢
				break;
			case RequestResCode.REQUEST_FAILE:
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};
	private Handler sceneSongHandler = new Handler() { //场景音乐
		public void handleMessage(Message msg) {
			// radio_GridView.onRefreshComplete();
			removeFootView();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;

				mSceneSongsResponse = gson.fromJson(element,
						SceneSongsResponse.class);
				if (mSceneSongsResponse != null
						&& mSceneSongsResponse.list != null
						&& mSceneSongsResponse.list.size() > 0) {
				SceneSubRes bean = mSceneSongsResponse.list.get(0);
					title_Tv.setText(bean.title);
					title_inView_Tv.setText(bean.title);
					if(!isNeedPlay)
						return;
					ArrayList<SongDetailInfo> list = CommonUtils
							.filterUnabelListerSong(mSceneSongsResponse.list
									.get(0).songs);
					Random random = new Random();
					int index = random.nextInt(list.size());
					MusicUtils.playSongDetailInfo(
							activity,
							list.get(index),
							RadioPager.class.getSimpleName(), -1L, list,
							Constants.KEY_RADIO_SCENE); 
				}
				break;
			case RequestResCode.REQUEST_FAILE:
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};
}
