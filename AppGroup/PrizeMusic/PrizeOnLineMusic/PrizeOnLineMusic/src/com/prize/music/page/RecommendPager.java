package com.prize.music.page;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.HomeAdBean;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.database.beans.HomeRecord;
import com.prize.app.download.DownloadHelper;
import com.prize.app.net.datasource.home.HomeDataSource;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.ToastUtils;
import com.prize.music.activities.MainActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.online.task.BannerTask;
import com.prize.music.online.task.RankTask;
import com.prize.music.ui.adapters.GalleryFlowAdapter;
import com.prize.music.ui.adapters.HomeCollectListAdapter;
import com.prize.music.ui.adapters.NewAlbumsAdapter;
import com.prize.music.ui.adapters.RecommendCollectListAdapter;
import com.prize.music.ui.adapters.RecommendDailyAdapter;
import com.prize.music.ui.adapters.SceneSongsAdapter;
import com.prize.music.views.FlowIndicator;
import com.prize.music.views.GalleryFlow;
import com.prize.music.views.ParabolaView;
import com.prize.music.views.ReflectionImage;
import com.prize.music.views.SongListGridView;
import com.prize.music.R;
import com.prize.onlinemusibean.AlbumsBean;
import com.prize.onlinemusibean.CollectBean;
import com.prize.onlinemusibean.RadioSceneBean;
import com.prize.onlinemusibean.RecomendRankBean;
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.response.AlbumsResponse;
import com.prize.onlinemusibean.response.BannerResponse;
import com.prize.onlinemusibean.response.RadioSceneResponse;
import com.prize.onlinemusibean.response.RecomendCollectResponse;
import com.prize.onlinemusibean.response.RecomendHotSongsResponse;
import com.prize.onlinemusibean.response.RecomendRankResponse;
import com.xiami.sdk.utils.ImageUtil;

/**
 **
 * 首页推荐今日歌单adapter
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RecommendPager extends BasePager implements OnClickListener

{
	private final String TAG = "RecommendPager";
	private static final int SCROLL = 0;
	/** 滚动区 */
	private GalleryFlow galleryFlow = null;
	private GalleryFlowAdapter adsAdapter = null; // 广告区
	/** 图片滚动间隔 */
	private final long delayMillis = 3 * 1000;
	/** 自动滚动 */
	private boolean isAutoScroll = true;
	// 初始化推荐列表 初始个数
	private static final int DEFAULT_RECOMMAND_NUMBER = 60;

	private FragmentActivity context;
	private FlowIndicator flowIndicator;
	private SongListGridView mGridView;
	private SongListGridView new_albums_GridView;
	private SongListGridView hot_songs_GridView;
	private SongListGridView collect_GridView;
	private SongListGridView scene_songs_GridView;
	RecommendDailyAdapter mRecommendDailyAdapter;
	RecommendDailyAdapter hotSongsAdapter;
	NewAlbumsAdapter mNewAlbumsAdapter;
	HomeCollectListAdapter mRecommendCollectListAdapter;
	SceneSongsAdapter mSceneSongsAdapter;;
	private TextView num_songs_Tv;
	private TextView titleTwo_Tv;
	private TextView singerTwo_Tv;
	private TextView titleone_Tv;
	private TextView singerone_Tv;
	private TextView titlethree_Tv;
	private TextView singerthree_Tv;
	ReflectionImage numOneOne_Iv;
	ReflectionImage numOnethree_Iv;
	ReflectionImage numOneTwo_Iv;
	RecomendRankBean currentRankBean;
	// private RecomendHotSongsResponse bean;
	private ParabolaView parabolaView;
	ImageView icon_fly2;
	ImageView icon_fly1;
	ImageView icon_fly3;
	private ArrayList<SongDetailInfo> filterList;

	public RecommendPager(FragmentActivity activity) {
		super(activity);
		this.context = activity;
		setNeedAddWaitingView(true);
		mNewAlbumsAdapter = new NewAlbumsAdapter(context);
	}

	public void onActivityCreated() {
	}

	/** 设置是否自动滚动 */
	public void setAutoScroll(boolean auto) {
		isAutoScroll = auto;
	}

	/** 图片滚动任务 */
	private Handler flowHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SCROLL: {
				if (isAutoScroll && (null != galleryFlow)) {
					galleryFlow.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
					flowHandler.removeMessages(SCROLL);
				}
				flowHandler.sendEmptyMessageDelayed(SCROLL, delayMillis);
			}
				break;
			}
		}
	};
	private ArrayList<SongDetailInfo> hotSong;

	/**
	 * 初始化界面
	 */
	public View onCreateView() {
		LayoutInflater inflater = LayoutInflater.from(activity);
		View root = inflater.inflate(R.layout.apppage_layout, null);
		ViewGroup rootView = (ViewGroup) context.getWindow().getDecorView();
		parabolaView = (ParabolaView) rootView.findViewById(R.id.parabolaView1);
		findViewById(root);

		initAdapter();

		galleryFlow.setSelection(DEFAULT_RECOMMAND_NUMBER * 10);
		galleryFlow.setFocusable(false);
		galleryFlow.setFocusableInTouchMode(false);
		galleryFlow.setVerticalFadingEdgeEnabled(false);
		galleryFlow.setHorizontalFadingEdgeEnabled(false);
		// galleryFlow.setSpacing(0); //不能设置，设置了，就不会自动滑动了
		// 发送一条，开始滚动
		flowHandler.sendEmptyMessage(SCROLL);

		galleryFlow.setOnTouchListener(new View.OnTouchListener() {
			/** 处理当用户点中图片时，不进行滚动 */
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
				case MotionEvent.ACTION_CANCEL:
					flowHandler.sendEmptyMessageDelayed(SCROLL, delayMillis);
					break;
				case MotionEvent.ACTION_DOWN:
					flowHandler.removeMessages(SCROLL);
					break;
				}
				return false;
			}
		});

		galleryFlow.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (flowIndicator != null && adsAdapter != null)
					flowIndicator.setSeletion(adsAdapter.getItemIndex(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		// gridView.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1,
		// int position, long id) {
		// }
		// });
		galleryFlow.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				HomeAdBean galleryItem = adsAdapter.getItem(position);// TopicItemBean
				if (galleryItem.url != null
						&& galleryItem.url.contains(Constants.KEY_COLLECT)) {// 歌单
					UiUtils.gotoMoreDaily(context,
							Integer.parseInt(galleryItem.url.split(":")[1]),
							Constants.KEY_COLLECT);
					return;
				}
				if (galleryItem.url != null
						&& galleryItem.url.contains(Constants.KEY_ALBUM)) {// 专辑
					UiUtils.gotoMoreDaily(context,
							Integer.parseInt(galleryItem.url.split(":")[1]),
							Constants.KEY_ALBUM);
					return;
				}
				if (galleryItem.url != null
						&& galleryItem.url.contains("artist")) {// 歌手
					UiUtils.JumpToSingerOnlineActivity(activity, null,
							Integer.parseInt(galleryItem.url.split(":")[1]));
				}
				if (galleryItem.url != null && galleryItem.url.contains("song")) {// 歌曲
					if(ClientInfo.networkType==ClientInfo.NONET){
						ToastUtils.showToast(R.string.net_error);
						return;
					}
					MusicUtils.playNetData(
							context,
							Integer.parseInt(galleryItem.url.split(":")[1]),
							RecommendPager.class.getSimpleName(), -1L,
							new ArrayList<SongDetailInfo>(),
							Constants.KEY_SONGS);
				}
				if (galleryItem.url != null && galleryItem.url.contains("h5")) {// h5
				}
			}
		});
		setListener(root);
		getCacheFromDB();

		return root;
	}

	private void setListener(View root) {
		root.findViewById(R.id.more_songs_Tv).setOnClickListener(this);
		root.findViewById(R.id.more_hot_rank_songs_Tv).setOnClickListener(this);
		new_albums_GridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AlbumsBean albumsBean = mNewAlbumsAdapter.getItem(position);
				if (albumsBean == null)
					return;

				if (albumsBean != null) {
					UiUtils.gotoMoreDaily(context, albumsBean.album_id,
							Constants.KEY_ALBUM);

				}

			}
		});
		collect_GridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mRecommendCollectListAdapter.getItem(position) != null) {
					UiUtils.gotoMoreDaily(
							context,
							mRecommendCollectListAdapter.getItem(position).list_id,
							Constants.KEY_COLLECT);

				}

			}
		});
		hot_songs_GridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SongDetailInfo beanInfo = hotSongsAdapter.getItem(position);
				if (beanInfo != null) {
					if(!DownloadHelper.isFileExists(beanInfo)){
						if(ClientInfo.networkType==ClientInfo.NONET){
							ToastUtils.showToast(R.string.net_error);
							return;
						}
					}
				
					MusicUtils.playSongDetailInfo(
							activity,
							hotSongsAdapter.getItem(position),
							RecommendPager.class.getSimpleName(), -1L, hotSong,
							Constants.TYPE_ALBUM);
					
					// 动画
					ImageView icon_fly = (ImageView) view
							.findViewById(R.id.icon_fly);
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

			}
		});
		scene_songs_GridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mSceneSongsAdapter.getItem(position) != null) {
					UiUtils.gotoMoreDaily(context,
							mSceneSongsAdapter.getItem(position).radio_id + "",
							Constants.KEY_RADIO, String
									.valueOf(mSceneSongsAdapter
											.getItem(position).title),
							mSceneSongsAdapter.getItem(position).logo);
				}

			}
		});
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SongDetailInfo beanInfo = mRecommendDailyAdapter
						.getItem(position);
				if (beanInfo != null) {
					if(!DownloadHelper.isFileExists(beanInfo)){
						if(ClientInfo.networkType==ClientInfo.NONET){
							ToastUtils.showToast(R.string.net_error);
							return;
						}
						
					}
					MusicUtils.playSongDetailInfo(
							activity,
							mRecommendDailyAdapter.getItem(position),
							RecommendPager.class.getSimpleName(), -1L,
							filterList, Constants.TYPE_ALBUM);

					// 动画
					ImageView icon_fly = (ImageView) view
							.findViewById(R.id.icon_fly);
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

			}
		});
		root.findViewById(R.id.more_hot_songs_Tv).setOnClickListener(this);
		root.findViewById(R.id.rank_three_Rlyt).setOnClickListener(this);
		root.findViewById(R.id.rank_one_Rlyt).setOnClickListener(this);
		root.findViewById(R.id.rank_two_Rlyt).setOnClickListener(this);
	}

	private void getCacheFromDB() {

		ArrayList<HomeAdBean> ads = HomeDataSource.getADs();
		if (adsAdapter == null) {
			adsAdapter = new GalleryFlowAdapter(context);
		}
		adsAdapter.setData(ads);
		flowIndicator.setCount(adsAdapter.getItemsSize());
		flowIndicator
				.setVisibility(adsAdapter.getItemsSize() > 0 ? View.VISIBLE
						: View.GONE);
		//ArrayList<SongDetailInfo> songs = HomeDataSource.getRecordsDailySongs();
		//今日歌单缓存
		filterList= HomeDataSource.getRecordsDailySongs();
		if (mRecommendDailyAdapter == null) {
			mRecommendDailyAdapter = new RecommendDailyAdapter(context);
		}
		if (filterList.size() > 6) {
			mRecommendDailyAdapter.setData(filterList.subList(0, 6));
		} else {
			mRecommendDailyAdapter.setData(filterList);

		}
		ArrayList<AlbumsBean> newAlbums = HomeDataSource.getRecordNewAlbums();
		if (mNewAlbumsAdapter == null) {
			mNewAlbumsAdapter = new NewAlbumsAdapter(context);
		}
		mNewAlbumsAdapter.setData(newAlbums);
		hotSong = HomeDataSource.getRecordsHotSong();
		if (hotSongsAdapter == null) {
			hotSongsAdapter = new RecommendDailyAdapter(context);
		}
//		hotSongsAdapter.setData(hotSong);
		hotSongsAdapter
		.setData(hotSong.size() > 6 ? hotSong
				.subList(0, 6) : hotSong);
		ArrayList<CollectBean> collects = HomeDataSource.getRecordsCollect();
		if (mRecommendCollectListAdapter == null) {
			mRecommendCollectListAdapter = new HomeCollectListAdapter(
					context);
		}
		mRecommendCollectListAdapter.setData(collects);
		ArrayList<RadioSceneBean> sceneSongs = HomeDataSource
				.getRecordsSceneSongs();
		if (mSceneSongsAdapter == null) {
			mSceneSongsAdapter = new SceneSongsAdapter(context);
		}
		mSceneSongsAdapter.setData(sceneSongs);
		ArrayList<SongDetailInfo> rankSongs = HomeDataSource
				.getRecordsRankSongs();

		processRankData(rankSongs);

	}

	OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	};
	private Handler sceneSongsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				RadioSceneResponse bean = gson.fromJson(element,
						RadioSceneResponse.class);
				ArrayList<HomeRecord> records = new ArrayList<HomeRecord>();
				for (RadioSceneBean homeAdBean : bean.list) {
					HomeRecord re = new HomeRecord();
					re.id = homeAdBean.radio_id;
					re.iconUrl = homeAdBean.logo;
					re.name = homeAdBean.title;
					re.contentType = HomeRecord.CONTENT_TYPE_SCENE_SONGS;
					records.add(re);
				}
				HomeDataSource.replaceHomeRecords(records,
						HomeRecord.CONTENT_TYPE_SCENE_SONGS);
				mSceneSongsAdapter.setData(bean.list);
				// }
				questRankData();
				break;
			case RequestResCode.REQUEST_FAILE:
				hideWaiting();
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};
	private Handler newAlbumsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				AlbumsResponse bean = gson.fromJson(element,
						AlbumsResponse.class);
				mNewAlbumsAdapter.setData(bean.albums);
				ArrayList<HomeRecord> records = new ArrayList<HomeRecord>();
				for (AlbumsBean homeAdBean : bean.albums) {
					HomeRecord re = new HomeRecord();
					re.id = homeAdBean.album_id;
					re.iconUrl = homeAdBean.album_logo;
					re.name = homeAdBean.album_name;
					re.subTitle = homeAdBean.artist_name;
					re.contentType = HomeRecord.CONTENT_TYPE_NEW_ALBUMS_LIST;
					records.add(re);
				}
				HomeDataSource.replaceHomeRecords(records,
						HomeRecord.CONTENT_TYPE_NEW_ALBUMS_LIST);
				initHotSongs();
				break;
			case RequestResCode.REQUEST_FAILE:
				hideWaiting();
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};
	private Handler hotSongsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				RecomendHotSongsResponse bean = gson.fromJson(element,
						RecomendHotSongsResponse.class);
				hotSong = CommonUtils.filterUnabelListerSong(bean.songs);
				hotSongsAdapter
				.setData(hotSong.size() > 6 ? hotSong
						.subList(0, 6) : hotSong);
				ArrayList<HomeRecord> records = new ArrayList<HomeRecord>();
				
				for (SongDetailInfo homeAdBean : hotSong) {
					HomeRecord re = new HomeRecord();
					re.id = homeAdBean.song_id;
					re.iconUrl = homeAdBean.album_logo;
					re.name = homeAdBean.song_name;
					re.subTitle = homeAdBean.singers;
					re.contentType = HomeRecord.CONTENT_TYPE_HOT_SONGS_LIST;
					records.add(re);
				}
				HomeDataSource.replaceHomeRecords(records,
						HomeRecord.CONTENT_TYPE_HOT_SONGS_LIST);
				initcollect();
				break;
			case RequestResCode.REQUEST_FAILE:
				hideWaiting();
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};
	private Handler rankHandler = new Handler() {
		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				List<RecomendRankResponse> rs = new ArrayList<RecomendRankResponse>();
				Type type = new TypeToken<ArrayList<RecomendRankResponse>>() {
				}.getType();
				rs = gson.fromJson(element, type);
				ArrayList<RecomendRankBean> items = new ArrayList<RecomendRankBean>();
				for (RecomendRankResponse o : rs) {
					items.addAll(o.items);
				}
				for (RecomendRankBean bean : items) {
					if (bean.title.contains("新歌")
							|| bean.type.contains("newmusic")) {
						currentRankBean = bean;
						ArrayList<HomeRecord> records = new ArrayList<HomeRecord>();
						for (SongDetailInfo homeAdBean : bean.songs) {
							HomeRecord re = new HomeRecord();
							re.id = homeAdBean.song_id;
							re.iconUrl = homeAdBean.album_logo;
							re.name = ImageUtil.transferImgUrl(
									homeAdBean.song_name, 220);
							re.subTitle = homeAdBean.singers;
							re.contentType = HomeRecord.CONTENT_TYPE_HOTSONG_RANK;
							records.add(re);
						}
						HomeDataSource.replaceHomeRecords(records,
								HomeRecord.CONTENT_TYPE_HOTSONG_RANK);
						processRankData(bean.songs);
						return;
					}
				}
				break;
			case RequestResCode.REQUEST_FAILE:

				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	private void questRankData() {
		RankTask task = new RankTask(xiamiSDK, RequestMethods.RANK_LIST,
				rankHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		task.execute(params);
	}

	private Handler bannerHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				BannerResponse bean = gson.fromJson(element,
						BannerResponse.class);
				adsAdapter.setData(bean.imgs);
				flowIndicator.setCount(adsAdapter.getItemsSize());
				flowIndicator
						.setVisibility(adsAdapter.getItemsSize() > 0 ? View.VISIBLE
								: View.GONE);
				ArrayList<HomeRecord> records = new ArrayList<HomeRecord>();
				for (HomeAdBean homeAdBean : bean.imgs) {
					HomeRecord re = new HomeRecord();
					re.iconUrl = homeAdBean.pic_url_yasha;
					re.adType = homeAdBean.url;
					re.contentType = HomeRecord.CONTENT_TYPE_AD;
					records.add(re);
				}
				HomeDataSource.replaceHomeRecords(records,
						HomeRecord.CONTENT_TYPE_AD);
				initDailySongs();
				break;
			case RequestResCode.REQUEST_FAILE:
				hideWaiting();
				if (adsAdapter != null && adsAdapter.getCount() <= 0) {
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

	private void initAdapter() {
		adsAdapter = new GalleryFlowAdapter(context);
		mRecommendDailyAdapter = new RecommendDailyAdapter(context);
		mGridView.setAdapter(mRecommendDailyAdapter);
		galleryFlow.setAdapter(adsAdapter);
		new_albums_GridView.setAdapter(mNewAlbumsAdapter);
		hotSongsAdapter = new RecommendDailyAdapter(context);
		hot_songs_GridView.setAdapter(hotSongsAdapter);
		mRecommendCollectListAdapter = new HomeCollectListAdapter(context);
		collect_GridView.setAdapter(mRecommendCollectListAdapter);
		mSceneSongsAdapter = new SceneSongsAdapter(context);
		scene_songs_GridView.setAdapter(mSceneSongsAdapter);
	}

	/**
	 * 请求推荐歌单
	 * 
	 * @return void
	 * @see
	 */
	void initDailySongs() {
		BannerTask task = new BannerTask(xiamiSDK,
				RequestMethods.RECOMMEND_DAILY_SONGS, dailyHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("limit", 30);
		task.execute(params);
	}

	private Handler dailyHandler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				RecomendHotSongsResponse bean = gson.fromJson(element,
						RecomendHotSongsResponse.class);
				filterList = CommonUtils.filterUnabelListerSong(bean.songs);
				if (filterList != null && filterList.size() > 0) {
					num_songs_Tv.setText("(" + filterList.size() + "首)");
					mRecommendDailyAdapter
							.setData(filterList.size() > 6 ? filterList
									.subList(0, 6) : filterList);
				}
				ArrayList<HomeRecord> records = new ArrayList<HomeRecord>();
				List<SongDetailInfo> list = null;
				if (filterList.size() > 6) {
					list = filterList.subList(0, 6);
				} else {
					list = filterList;
				}
				for (SongDetailInfo homeAdBean : list) {
					HomeRecord re = new HomeRecord();
					re.id = homeAdBean.song_id;
					re.iconUrl = homeAdBean.album_logo;
					re.name = homeAdBean.song_name;
					re.subTitle = homeAdBean.singers;
					re.contentType = HomeRecord.CONTENT_TYPE_DAILY_SONGS;
					records.add(re);
				}
				HomeDataSource.replaceHomeRecords(records,
						HomeRecord.CONTENT_TYPE_DAILY_SONGS);

				initNewAlbums();
				break;
			case RequestResCode.REQUEST_FAILE:
				hideWaiting();
				if (null != mRecommendDailyAdapter
						&& mRecommendDailyAdapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							initDailySongs();
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
	 * 请求热门歌曲列表
	 * 
	 * @return void
	 * @see
	 */
	private void initHotSongs() {
		BannerTask task = new BannerTask(xiamiSDK,
				RequestMethods.RECOMMEND_HOT_SONGS, hotSongsHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("limit", 10);
		task.execute(params);
	}

	private Handler collectHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				RecomendCollectResponse bean = gson.fromJson(element,
						RecomendCollectResponse.class);
				mRecommendCollectListAdapter.setData(bean.collects);
				ArrayList<HomeRecord> records = new ArrayList<HomeRecord>();
				for (CollectBean homeAdBean : bean.collects) {
					HomeRecord re = new HomeRecord();
					re.id = homeAdBean.list_id;
					if (TextUtils.isEmpty(homeAdBean.collect_logo)) {
						re.iconUrl = homeAdBean.author_avatar;
					} else {

						re.iconUrl = homeAdBean.collect_logo;
					}
					re.playCount = homeAdBean.play_count;
					re.name = homeAdBean.collect_name;
					re.contentType = HomeRecord.CONTENT_TYPE_RECOMMEND_COLLECT;
					records.add(re);
				}
				HomeDataSource.replaceHomeRecords(records,
						HomeRecord.CONTENT_TYPE_RECOMMEND_COLLECT);
				initSceneSongs();
				break;
			case RequestResCode.REQUEST_FAILE:
				hideWaiting();
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	/**
	 * 请求热门歌曲列表
	 * 
	 * @return void
	 * @see
	 */
	private void initcollect() {
		BannerTask task = new BannerTask(xiamiSDK,
				RequestMethods.COLLECT_RECOMMEND, collectHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("page", 1);
		params.put("limit", 6);
		task.execute(params);
	}

	protected void initSceneSongs() {
		BannerTask task = new BannerTask(xiamiSDK, RequestMethods.RADIO_SCENE,
				sceneSongsHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		task.execute(params);
	}

	/**
	 * 新碟上架
	 * 
	 * @return void
	 * @see
	 */
	private void initNewAlbums() {
		BannerTask task = new BannerTask(xiamiSDK,
				RequestMethods.RANK_NEW_ALBUMS, newAlbumsHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("page", 1);
		params.put("limit", 6);
		task.execute(params);
	}

	private void findViewById(View root) {
		flowIndicator = (FlowIndicator) root.findViewById(R.id.flowIndicator);
		mGridView = (SongListGridView) root.findViewById(R.id.mGridView);
		new_albums_GridView = (SongListGridView) root
				.findViewById(R.id.new_albums_GridView);
		hot_songs_GridView = (SongListGridView) root
				.findViewById(R.id.hot_songs_GridView);
		collect_GridView = (SongListGridView) root
				.findViewById(R.id.collect_GridView);
		scene_songs_GridView = (SongListGridView) root
				.findViewById(R.id.scene_songs_GridView);
		galleryFlow = (GalleryFlow) root
				.findViewById(R.id.recommand_galleryflow);
		flowIndicator = (FlowIndicator) root.findViewById(R.id.flowIndicator);
		num_songs_Tv = (TextView) root.findViewById(R.id.num_songs_Tv);
		singerthree_Tv = (TextView) root.findViewById(R.id.singerthree_Tv);
		singerone_Tv = (TextView) root.findViewById(R.id.singerone_Tv);
		titleone_Tv = (TextView) root.findViewById(R.id.titleone_Tv);
		singerTwo_Tv = (TextView) root.findViewById(R.id.singerTwo_Tv);
		titleTwo_Tv = (TextView) root.findViewById(R.id.titleTwo_Tv);
		titlethree_Tv = (TextView) root.findViewById(R.id.titlethree_Tv);
		singerthree_Tv = (TextView) root.findViewById(R.id.singerthree_Tv);
		numOneTwo_Iv = (ReflectionImage) root.findViewById(R.id.numOneTwo_Iv);
		numOneOne_Iv = (ReflectionImage) root.findViewById(R.id.numOneOne_Iv);
		numOnethree_Iv = (ReflectionImage) root
				.findViewById(R.id.numOnethree_Iv);
		icon_fly2 = (ImageView) root.findViewById(R.id.icon_fly2);
		icon_fly1 = (ImageView) root.findViewById(R.id.icon_fly1);
		icon_fly3 = (ImageView) root.findViewById(R.id.icon_fly3);

	}

	public void loadData() {
		if (!flowHandler.hasMessages(SCROLL)) {
			flowHandler.sendEmptyMessageDelayed(SCROLL, 1000);
		}
		isAutoScroll = true;
		BannerTask task = new BannerTask(xiamiSDK,
				RequestMethods.MOBILE_SDK_IMAGE, bannerHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("show_h5", false);
		task.execute(params);

	}

	@Override
	public void onDestroy() {
		if (flowHandler != null) {
			flowHandler.removeCallbacksAndMessages(null);
		}
		if (bannerHandler != null) {
			bannerHandler.removeCallbacksAndMessages(null);
		}
		if (dailyHandler != null) {
			dailyHandler.removeCallbacksAndMessages(null);
		}
		if (newAlbumsHandler != null) {
			newAlbumsHandler.removeCallbacksAndMessages(null);
		}
		if (hotSongsHandler != null) {
			hotSongsHandler.removeCallbacksAndMessages(null);
		}
		if (collectHandler != null) {
			collectHandler.removeCallbacksAndMessages(null);
		}
		if (sceneSongsHandler != null) {
			sceneSongsHandler.removeCallbacksAndMessages(null);
		}
		if (rankHandler != null) {
			rankHandler.removeCallbacksAndMessages(null);
		}
	}

	@Override
	public String getPageName() {
		return "RecommendPager";
	}

	@Override
	public void onResume() {
		super.onResume();
		isAutoScroll = true;

	}

	@Override
	public void onPause() {
		super.onPause();
		isAutoScroll = false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.more_songs_Tv:
			UiUtils.gotoDailyMoreDaily(context,
					context.getString(R.string.daily_songs));
			break;
		case R.id.more_hot_songs_Tv:
			UiUtils.gotoDailyMoreDaily(context,
					context.getString(R.string.hot_songs));
			break;
		case R.id.more_hot_rank_songs_Tv://新歌榜
			if (currentRankBean != null
					&& !TextUtils.isEmpty(currentRankBean.type)) {
				UiUtils.gotoMoreDaily(context, currentRankBean.type,
						Constants.KEY_RANK);
			}else{
				UiUtils.gotoMoreDaily(context, "newmusic_all",
						Constants.KEY_RANK);
			}
			break;
		case R.id.rank_three_Rlyt:
				processOnClickRank(icon_fly3, 2);
			break;
		case R.id.rank_one_Rlyt:
				processOnClickRank(icon_fly1, 0);
			break;
		case R.id.rank_two_Rlyt:
				processOnClickRank(icon_fly2, 1);
			break;

		default:
			break;
		}

	}

	private void processOnClickRank(ImageView icon_fly, int position) {
		if (currentRankBean != null
				&& currentRankBean.songs.size() > (position + 1)) {
			SongDetailInfo beanInfo = currentRankBean.songs.get(position);
			if (beanInfo != null) {
				if(!DownloadHelper.isFileExists(beanInfo)){
					if (ClientInfo.networkType == ClientInfo.NONET) {
						ToastUtils.showToast(R.string.net_error);
						return;
					}
				}
				if (beanInfo.permission != null
						&& !beanInfo.permission.available) {
					ToastUtils.showToast(R.string.listen_is_forbidden);
					return;
				}
				MusicUtils.playSongDetailInfo(
						activity,
						beanInfo, RecommendPager.class
						.getSimpleName(), -1L, CommonUtils
						.filterUnabelListerSong(currentRankBean.songs),
						Constants.TYPE_PLAYLIST);

				// 动画
				if (parabolaView != null) {
					ImageView bottomView = null;
					if (context instanceof MainActivity) {
						bottomView = ((MainActivity) context).getBottomView();
					}
					parabolaView.setAnimationPara(icon_fly, bottomView);
					if (!parabolaView.isRunning()) {
						parabolaView.showMovie();
					}
				}
			}
		}
	}

	private void processRankData(ArrayList<SongDetailInfo> rankSongs) {
		if (rankSongs == null || rankSongs.size() <= 2) {
			return;
		}
		titleone_Tv.setText(rankSongs.get(0).song_name);
		singerone_Tv.setText(rankSongs.get(0).singers);
		titleTwo_Tv.setText(rankSongs.get(1).song_name);
		singerTwo_Tv.setText(rankSongs.get(1).singers);
		titlethree_Tv.setText(rankSongs.get(2).song_name);
		singerthree_Tv.setText(rankSongs.get(2).singers);
		String url = rankSongs.get(0).album_logo;
		ImageLoader.getInstance().displayImage(
				ImageUtil.transferImgUrl(url, 220), numOneOne_Iv,
				UILimageUtil.getTwoOneZeroDpLoptions(),
				new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {

					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {

					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						if (loadedImage != null) {
							numOneOne_Iv.DoReflection(loadedImage);
						}

					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {

						// TODO Auto-generated method stub

					}
				});
		ImageLoader.getInstance().displayImage(
				ImageUtil.transferImgUrl(rankSongs.get(2).album_logo, 220),
				numOnethree_Iv, UILimageUtil.getTwoOneZeroDpLoptions(),
				new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {

					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {

					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						if (loadedImage != null) {
							numOnethree_Iv.DoReflection(loadedImage);
						}

					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {

						// TODO Auto-generated method stub

					}
				});
		ImageLoader.getInstance().displayImage(
				ImageUtil.transferImgUrl(rankSongs.get(1).album_logo, 220),
				numOneTwo_Iv, UILimageUtil.getTwoOneZeroDpLoptions(),
				new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {

					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {

					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						if (loadedImage != null) {
							numOneTwo_Iv.DoReflection(loadedImage);
						}

					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {

					}
				});
	}

}
