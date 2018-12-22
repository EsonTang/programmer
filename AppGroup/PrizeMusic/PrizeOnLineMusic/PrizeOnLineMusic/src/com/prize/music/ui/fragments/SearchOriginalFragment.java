package com.prize.music.ui.fragments;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.database.dao.SearchHistoryDao;
import com.prize.app.util.SDKUtil;
import com.prize.app.xiami.RequestManager;
import com.prize.music.activities.SearchActivity;
import com.prize.music.base.BaseFragment;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.online.task.HotWordsTask;
import com.prize.music.online.task.RecommendSingerTask;
import com.prize.music.page.BasePager.ReloadFunction;
import com.prize.music.ui.adapters.RecommandSingerAdapter;
import com.prize.music.ui.adapters.SearchHotWordsAdapter;
import com.prize.music.ui.adapters.SearchRecordAdapter;
import com.prize.music.ui.widgets.ScrollGridView;
import com.prize.music.R;
import com.prize.onlinemusibean.HotWordsResponse;
import com.prize.onlinemusibean.HotWordsResponse.SearchWords;
import com.prize.onlinemusibean.RecommendSingerResponse;
import com.xiami.sdk.XiamiSDK;

import org.xutils.common.Callback.Cancelable;

/**
 * 搜索初始化界面
 * 
 * @author pengyang
 */
public class SearchOriginalFragment extends BaseFragment {
	private ListView mListView;
	private GridView mHorGrideView;

	private TextView clear_history_Tv;
//	private TextView change_data_Tv;
	private RelativeLayout search_Rlyt;
	private TextView recommand_more;
	private View root;
	private View headerView;
	private SearchActivity activity;

	private HorizontalScrollView mHorizontalScrollView;
	ScrollGridView mHotApp_gv;
	private Cancelable mCancelable;

	private View search_line_View;
	private View reloadView;

	private RequestManager requestManager;
	private RecommandSingerAdapter singerAdapter;
	private SearchHotWordsAdapter mSearchHotWordsAdapter;
	protected XiamiSDK xiamiSDK;
	private SearchRecordAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// 配置生成SDK
		activity = (SearchActivity) getActivity();
		xiamiSDK = new XiamiSDK(activity, SDKUtil.KEY, SDKUtil.SECRET);
		root = inflater.inflate(R.layout.fragment_search_original, container,
				false);
		headerView = inflater.inflate(R.layout.head_search_original, null);
		findViewById();
		init();
		setListener();
		return root;
	}

	@Override
	protected void findViewById() {
		reloadView = (View) root.findViewById(R.id.reload_Llyt);
		waitView = (View) root.findViewById(R.id.loading_Llyt_id);
		mListView = (ListView) root.findViewById(R.id.mListView);
//		search_line_View = (View) headerView
//				.findViewById(R.id.search_line_View);
		search_Rlyt = (RelativeLayout) headerView
				.findViewById(R.id.search_Rlyt);
		recommand_more =(TextView) headerView
				.findViewById(R.id.recommand_more);
		clear_history_Tv = (TextView) headerView
				.findViewById(R.id.clear_history_Tv);
//		change_data_Tv = (TextView) headerView
//				.findViewById(R.id.change_data_Tv);
		mHorizontalScrollView = (HorizontalScrollView) headerView
				.findViewById(R.id.mHorizontalScrollView);
		mHorGrideView = (GridView) headerView
				.findViewById(R.id.mAlwaysRecommendList);
		mHotApp_gv = (ScrollGridView) headerView.findViewById(R.id.mHotApp_gv);

		recommand_more.setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						 activity.gotoRecommandActivity();
//						 activity.finish();
					}
				});
	  }

	@Override
	protected void setListener() {

		mHotApp_gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mSearchHotWordsAdapter.getItem(position) != null) {
					SearchWords bean = mSearchHotWordsAdapter.getItem(position);
					if (!TextUtils.isEmpty(bean.word)) {
						activity.searchView.setTextForEditText(bean.word);
						activity.goToSearResFragmnet(bean.word);
					}
				}
			}
		});

		clear_history_Tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SearchHistoryDao.cleardata();
				search_Rlyt.setVisibility(View.GONE);
//				search_line_View.setVisibility(View.GONE);
			    adapter.clearData();
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				 if (position >= 1) {
				 String text = adapter.getItem(position - 1);
				 if (TextUtils.isEmpty(text))
				 return;
				 activity.searchView.setTextForEditText(text);
				 activity.goToSearResFragmnet(text);
				}
			}
		});

		mHorGrideView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				//跳转到单个歌手
				if(singerAdapter.getItem(position) !=null){
					UiUtils.JumpToSingerOnlineActivity(activity,singerAdapter.getItem(position),
							singerAdapter.getItem(position).artist_id);
				}
			}
		});

		mHorizontalScrollView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (activity == null || activity.searchView == null
							|| mHorGrideView == null) {
						return false;
					}
					mHorizontalScrollView.setFocusable(false);
					mHorGrideView.setFocusable(false);
					activity.searchView.requstFocus();
				}
				return false;
			}
		});
	}

	@Override
	protected void init() {
		mListView.addHeaderView(headerView);
		requestManager = RequestManager.getInstance();
		singerAdapter = new RecommandSingerAdapter(activity);

		mHorGrideView.setAdapter(singerAdapter);

		mSearchHotWordsAdapter = new SearchHotWordsAdapter(activity);
		mHotApp_gv.setAdapter(mSearchHotWordsAdapter);

	
		 ArrayList<String> list = SearchHistoryDao.getSearchHistoryList();
		 int size = list.size();
		 adapter = new SearchRecordAdapter(activity, list);
		 if (size <= 0) {
		      search_Rlyt.setVisibility(View.GONE);
//		      search_line_View.setVisibility(View.INVISIBLE);
		 }

		 mListView.setAdapter(adapter);
		 doRequestRecommandSingerData();
		 requestHotTipsData();
	}

	/**
	 * 请求推荐热词数据
	 */
	private void requestHotTipsData() {

		HotWordsTask hotWordsTask = new HotWordsTask(xiamiSDK,
				RequestMethods.METHOD_SEARCH_HOTWORDS, hotWordsHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		hotWordsTask.execute(params);
	}

	private Handler hotWordsHandler = new Handler() {
		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:

				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				HotWordsResponse bean = gson.fromJson(element, HotWordsResponse.class);
				mListView.setVisibility(View.VISIBLE);
				if (mSearchHotWordsAdapter != null && bean.search_words != null) {
						mSearchHotWordsAdapter.setData(bean.search_words);
				}
				break;
			case RequestResCode.REQUEST_FAILE:
				if (null != singerAdapter && singerAdapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							showWaiting();
							doRequestRecommandSingerData();
							requestHotTipsData();
						}
					});
				}
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	private Handler recSingerHandler = new Handler() {
		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:

				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				RecommendSingerResponse beans= gson.fromJson(element, RecommendSingerResponse.class);

				// 显示
				mListView.setVisibility(View.VISIBLE);
				if (singerAdapter != null && beans.artists!= null) {
					    singerAdapter.setData(beans.artists);
						DisplayMetrics dm = new DisplayMetrics();
						activity.getWindowManager().getDefaultDisplay()
								.getMetrics(dm);
						int w = (int) ((dm.widthPixels-40 )/ 5);
						// int w = 720 / 4; 四个
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
								w * singerAdapter.getCount(),
								LayoutParams.MATCH_PARENT);
						mHorGrideView.setLayoutParams(lp);
						mHorGrideView.setNumColumns(singerAdapter.getCount());
				} else {
						mHorizontalScrollView.setVisibility(View.GONE);
						headerView.findViewById(R.id.view_line).setVisibility(
								View.GONE);
						headerView.findViewById(R.id.hot_search_Rlyt)
								.setVisibility(View.GONE);
				}
				break;
			case RequestResCode.REQUEST_FAILE:
				if (null != singerAdapter && singerAdapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							showWaiting();
							doRequestRecommandSingerData();
							requestHotTipsData();
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
	 * 方法描述：推荐的歌手
	 */
	private void doRequestRecommandSingerData() {

		RecommendSingerTask singertask = new RecommendSingerTask(xiamiSDK,
				RequestMethods.METHOD_RECOMMEND_ARTIST, recSingerHandler);

		HashMap<String, Object> params = new HashMap<String, Object>();
		singertask.execute(params);
	}

	@Override
	public void onDestroy() {
		if (recSingerHandler != null) {
			recSingerHandler.removeCallbacksAndMessages(null);
		}
		if (hotWordsHandler != null) {
			hotWordsHandler.removeCallbacksAndMessages(null);
		}
		if (mCancelable != null) {
			mCancelable.cancel();
		}
		super.onDestroy();
	}

	private View waitView = null;

	/**
	 * 隐藏等待框
	 */
	public void hideWaiting() {
		if (waitView == null)
			return;
		waitView.setVisibility(View.GONE);
//		ProgressBar gifWaitingView = (ProgressBar) waitView
//				.findViewById(R.id.progress_loading_loading);
		reloadView.setVisibility(View.GONE);

	}

	/**
	 * 加载失败
	 */
	public void loadingFailed(final ReloadFunction reload) {
		waitView.setVisibility(View.GONE);
		reloadView.setVisibility(View.VISIBLE);
		mListView.setVisibility(View.GONE);
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
		mListView.setVisibility(View.GONE);
		reloadView.setVisibility(View.GONE);
	}
}
