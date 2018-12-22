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

package com.prize.music.ui.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.download.DownloadHelper;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.SDKUtil;
import com.prize.app.util.ToastUtils;
import com.prize.app.xiami.RequestManager;
import com.prize.music.activities.MainActivity;
import com.prize.music.activities.ToAlbumDetailActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.online.task.BannerTask;
import com.prize.music.page.BasePager.ReloadFunction;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.SongsListAdapter;
import com.prize.music.views.GifView;
import com.prize.music.views.ParabolaView;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.response.AlbumDetailResponse;
import com.prize.onlinemusibean.response.CollectDetailResponse;
import com.prize.onlinemusibean.response.RankDetailResponse;
import com.prize.onlinemusibean.response.RecomendHotSongsResponse;
import com.prize.onlinemusibean.response.SceneDetailResponse;
import com.xiami.sdk.XiamiSDK;

public class DetailListFragment extends Fragment {

	protected ListView mListView;
	private RequestManager requestManager;
	XiamiSDK xiamiSDK;
	SongsListAdapter mRecommendDailyAdapter;
	private String id = null;
	private String type;
	private String title;
	private ArrayList<SongDetailInfo> canListerList = new ArrayList<SongDetailInfo>();
	private ParabolaView parabolaView;
	private View reloadView;
	private View waitView = null;
	private View forbid_Llyt_id = null;
	private BannerTask task;
	private String where;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			id = getArguments().getString(Constants.KEY);
			type = getArguments().getString(Constants.TYPE);
			title = getArguments().getString(Constants.TITLE);
			where = getArguments().getString(Constants.WHERE);
		}
		requestManager = RequestManager.getInstance();
		xiamiSDK = new XiamiSDK(getActivity().getApplicationContext(),
				SDKUtil.KEY, SDKUtil.SECRET);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View root = inflater.inflate(R.layout.detail_listview_layout,
				container, false);
		View shuffle_temp = View.inflate(getActivity(),
				R.layout.onlineartist_shuffle_all, null);
		mListView = (ListView) root
				.findViewById(R.id.id_stickynavlayout_innerscrollview);
		reloadView = (View) root.findViewById(R.id.reload_Llyt);
		forbid_Llyt_id = (View) root.findViewById(R.id.forbid_Llyt_id);
		waitView = (View) root.findViewById(R.id.loading_Llyt_id);
		mListView.addHeaderView(shuffle_temp);
		shuffle_temp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (canListerList == null || canListerList.size() <= 0) {
					return;
				}
				MusicUtils.playSongDetailInfo(getActivity(),canListerList.get(0),
						DetailListFragment.class.getSimpleName(), -1L,
						canListerList, type);

			}
		});

		ViewGroup rootView = (ViewGroup) getActivity().getWindow()
				.getDecorView();
		parabolaView = (ParabolaView) rootView.findViewById(R.id.parabolaView1);
		init();
		setListener();
		return root;
	}

	protected void findViewById() {
	}

	protected void setListener() {
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				if (view.getId() != R.id.edit_Llyt) {
					SongDetailInfo bean = mRecommendDailyAdapter
							.getItem(position - mListView.getHeaderViewsCount());
					if (bean == null)
						return;
					MusicUtils.playSongDetailInfo(
							getActivity(),
							mRecommendDailyAdapter.getItem(position
									- mListView.getHeaderViewsCount()),
							DetailListFragment.class.getSimpleName(), -1L,
							canListerList, type);
				}

				// 动画
				ImageView icon_fly = (ImageView) view
						.findViewById(R.id.icon_fly);
				if (parabolaView != null) {
					ImageView bottomView = null;
					if (getActivity() instanceof MainActivity) {
						bottomView = ((MainActivity) getActivity())
								.getBottomView();
					}
					if (getActivity() instanceof ToAlbumDetailActivity) {
						bottomView = ((ToAlbumDetailActivity) getActivity())
								.getBottomView();
					}
					parabolaView.setAnimationPara(icon_fly, bottomView);
					if (!parabolaView.isRunning()) {
						parabolaView.showMovie();
					}
				}
			}
		});

	}

	protected void init() {
		mRecommendDailyAdapter = new SongsListAdapter(getActivity());
		if(!TextUtils.isEmpty(where)){
			mRecommendDailyAdapter.setWhere(where);
		}
		requestData();
	}

	private Handler dailyHandler = new Handler() {

		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				RecomendHotSongsResponse bean = gson.fromJson(element,
						RecomendHotSongsResponse.class);
				canListerList = CommonUtils.filterUnabelListerSong(bean.songs);
				if (back != null) {
					back.back(bean, canListerList);
				}
				if ((canListerList == null | canListerList.size() <= 0)
						&& forbid_Llyt_id != null) {
					forbid_Llyt_id.setVisibility(View.VISIBLE);
					return;
				}
				mRecommendDailyAdapter.setData(canListerList);
				mListView.setAdapter(mRecommendDailyAdapter);
				break;
			case RequestResCode.REQUEST_FAILE:
				if (null != mRecommendDailyAdapter
						&& mRecommendDailyAdapter.getCount() == 0) {
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
	private Handler collectHandler = new Handler() {

		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				CollectDetailResponse bean = gson.fromJson(element,
						CollectDetailResponse.class);
				canListerList = CommonUtils.filterUnabelListerSong(bean.songs);
				if (back != null) {
					back.back(bean, canListerList);
				}
				if ((canListerList == null | canListerList.size() <= 0)
						&& forbid_Llyt_id != null) {
					forbid_Llyt_id.setVisibility(View.VISIBLE);
					return;
				}
				mRecommendDailyAdapter.setData(canListerList);
				mListView.setAdapter(mRecommendDailyAdapter);
				break;
			case RequestResCode.REQUEST_FAILE:
				if (null != mRecommendDailyAdapter
						&& mRecommendDailyAdapter.getCount() == 0) {
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
	private Handler albumHandler = new Handler() {

		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				AlbumDetailResponse bean = gson.fromJson(element,
						AlbumDetailResponse.class);
				canListerList = CommonUtils.filterAlbumSong(bean.songs,bean);
				if (back != null) {
					back.back(bean, canListerList);
				}
				if ((canListerList == null | canListerList.size() <= 0)
						&& forbid_Llyt_id != null) {
					forbid_Llyt_id.setVisibility(View.VISIBLE);
					return;
				}

				mRecommendDailyAdapter.setData(canListerList);
				mListView.setAdapter(mRecommendDailyAdapter);
				break;
			case RequestResCode.REQUEST_FAILE:
				if (null != mRecommendDailyAdapter
						&& mRecommendDailyAdapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							showWaiting();
							requestAlbumDetailData();
						}
					});
				}
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	RequestBack back;

	public void setBack(RequestBack back) {
		this.back = back;
	}

	public interface RequestBack {
		void back(Object bean, ArrayList<SongDetailInfo> list);
	}

	/**
	 * 请求推荐歌单
	 * 
	 * @return void
	 * @see
	 */
	private void requestData() {
		showWaiting();
		if (TextUtils.isEmpty(type)) {

			if (!TextUtils.isEmpty(title)) {
				if (getActivity().getString(R.string.hot_songs).equals(title)) {
					requestHotSongsData();
				} else {
					requestDailyData();

				}
			}

			return;
		}
		switch (type) {
		case Constants.KEY_ALBUM:
			requestAlbumDetailData();
			break;
		case Constants.KEY_COLLECT:
			requestCollectDetailData();
			break;
		case Constants.KEY_RANK:
			requestRankDetailData();
			break;
		case Constants.KEY_RADIO:
			requestRadioDetailData();
			break;
		}
	}

	private void requestDailyData() {
		if (task != null && task.getStatus() == Status.RUNNING) {
			task.cancel(true);
			task = null;
		}
		task = new BannerTask(xiamiSDK, RequestMethods.RECOMMEND_DAILY_SONGS,
				dailyHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("limit", 30);
		task.execute(params);
	}

	private void requestAlbumDetailData() {
		if (task != null && task.getStatus() == Status.RUNNING) {
			task.cancel(true);
			task = null;
		}
		task = new BannerTask(xiamiSDK, RequestMethods.AlBUM_DETAIL,
				albumHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("album_id", Integer.parseInt(id));
		params.put("full_des", false);
		task.execute(params);
	}

	private void requestCollectDetailData() {
		if (task != null && task.getStatus() == Status.RUNNING) {
			task.cancel(true);
			task = null;
		}
		task = new BannerTask(xiamiSDK, RequestMethods.COLLECT_DETAIL,
				collectHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("list_id", Integer.parseInt(id));
		params.put("full_des", false);
		task.execute(params);
	}

	private void requestRadioDetailData() {
		if (task != null && task.getStatus() == Status.RUNNING) {
			task.cancel(true);
			task = null;
		}
		task = new BannerTask(xiamiSDK, RequestMethods.RADIO_DETAIL,
				radioHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("id", Integer.parseInt(id));
		task.execute(params);
	}

	private Handler radioHandler = new Handler() {

		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				SceneDetailResponse bean = gson.fromJson(element,
						SceneDetailResponse.class);
				canListerList = CommonUtils.filterUnabelListerSong(bean.songs);
				if ((canListerList == null | canListerList.size() <= 0)
						&& forbid_Llyt_id != null) {
					forbid_Llyt_id.setVisibility(View.VISIBLE);
					return;
				}
				if (back != null) {
					back.back(bean, canListerList);
				}
				mRecommendDailyAdapter.setData(canListerList);
				mListView.setAdapter(mRecommendDailyAdapter);
				break;
			case RequestResCode.REQUEST_FAILE:
				if (null != mRecommendDailyAdapter
						&& mRecommendDailyAdapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							showWaiting();
							requestRadioDetailData();
						}
					});
				}
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	private void requestRankDetailData() {
		if (task != null && task.getStatus() == Status.RUNNING) {
			task.cancel(true);
			task = null;
		}
		task = new BannerTask(xiamiSDK, RequestMethods.RANK_DETAIL, rankHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("type", id);
		task.execute(params);
	}

	private Handler rankHandler = new Handler() {

		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				RankDetailResponse bean = gson.fromJson(element,
 						RankDetailResponse.class);
				canListerList = CommonUtils.filterUnabelListerSong(bean.songs);
				if (back != null) {
					back.back(bean, canListerList);
				}
				mRecommendDailyAdapter.setData(canListerList);
				mListView.setAdapter(mRecommendDailyAdapter);
				break;
			case RequestResCode.REQUEST_FAILE:
				if (null != mRecommendDailyAdapter
						&& mRecommendDailyAdapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							showWaiting();
							requestRankDetailData();
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
	private void requestHotSongsData() {
		if (task != null && task.getStatus() == Status.RUNNING) {
			task.cancel(true);
			task = null;
		}
		task = new BannerTask(xiamiSDK, RequestMethods.RECOMMEND_HOT_SONGS,
				hotSongsHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		task.execute(params);
	}

	private Handler hotSongsHandler = new Handler() {
		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				switch (msg.what) {
				case RequestResCode.REQUEST_OK:
					Gson gson = requestManager.getGson();
					JsonElement element = (JsonElement) msg.obj;
					RecomendHotSongsResponse bean = gson.fromJson(element,
							RecomendHotSongsResponse.class);
					canListerList = CommonUtils
							.filterUnabelListerSong(bean.songs);
					if (back != null) {
						back.back(bean, canListerList);
					}
					mRecommendDailyAdapter.setData(canListerList);
					mListView.setAdapter(mRecommendDailyAdapter);
					break;
				case RequestResCode.REQUEST_FAILE:
					if (null != mRecommendDailyAdapter
							&& mRecommendDailyAdapter.getCount() == 0) {
						loadingFailed(new ReloadFunction() {

							@Override
							public void reload() {
								showWaiting();
								requestHotSongsData();
							}
						});
					}
					break;
				case RequestResCode.REQUEST_EXCEPTION:
					if (null != mRecommendDailyAdapter
							&& mRecommendDailyAdapter.getCount() == 0) {
						loadingFailed(new ReloadFunction() {

							@Override
							public void reload() {
								showWaiting();
								requestHotSongsData();
							}
						});
					}
					break;
				}
				break;
			case RequestResCode.REQUEST_FAILE:
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	@Override
	public void onDestroy() {
		if (dailyHandler != null) {
			dailyHandler.removeCallbacksAndMessages(null);
		}
		if (collectHandler != null) {
			collectHandler.removeCallbacksAndMessages(null);
		}
		if (albumHandler != null) {
			albumHandler.removeCallbacksAndMessages(null);
		}
		if (radioHandler != null) {
			radioHandler.removeCallbacksAndMessages(null);
		}
		if (rankHandler != null) {
			rankHandler.removeCallbacksAndMessages(null);
		}
		if (hotSongsHandler != null) {
			hotSongsHandler.removeCallbacksAndMessages(null);
		}
		if (task != null && task.getStatus() == Status.RUNNING) {
			task.cancel(true);
		}
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mRecommendDailyAdapter != null) {
			mRecommendDailyAdapter.removeDownLoadHandler();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mRecommendDailyAdapter != null) {
			mRecommendDailyAdapter.setDownlaodRefreshHandle();
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if (isVisibleToUser) {
			if (mRecommendDailyAdapter != null) {
				mRecommendDailyAdapter.setDownlaodRefreshHandle();
			}
		} else {
			if (mRecommendDailyAdapter != null) {
				mRecommendDailyAdapter.removeDownLoadHandler();
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
			if (mListView != null && mRecommendDailyAdapter != null) {
				mRecommendDailyAdapter.notifyDataSetChanged();
			}
		}
	};

	/**
	 * 隐藏等待框
	 */
	public void hideWaiting() {
		if (waitView == null)
			return;
		waitView.setVisibility(View.GONE);
		GifView gifWaitingView = (GifView) waitView
				.findViewById(R.id.gif_waiting);
		gifWaitingView.setPaused(true);
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
		gifWaitingView.setPaused(false);
		reloadView.setVisibility(View.GONE);
	}

}
