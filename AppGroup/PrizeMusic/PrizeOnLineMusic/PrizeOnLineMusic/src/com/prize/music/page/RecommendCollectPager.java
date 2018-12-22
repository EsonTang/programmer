package com.prize.music.page;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.util.JLog;
import com.prize.music.activities.MainActivity;
import com.prize.music.activities.MainActivity.TagsOnItemClick;
import com.prize.music.online.task.BannerTask;
import com.prize.music.ui.adapters.HotTagsAdapter;
import com.prize.music.ui.adapters.HotTagsAdapter.OnItemCallBack;
import com.prize.music.ui.adapters.RecommendCollectListAdapter;
import com.prize.music.R;
import com.prize.onlinemusibean.CollectBean;
import com.prize.onlinemusibean.response.RecomendCollectResponse;
import com.prize.onlinemusibean.response.RecomendTagsResponse;

/**
 **
 * 首页歌单界面
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RecommendCollectPager extends BasePager implements
		TagsOnItemClick, OnItemCallBack {
	private final static String TAG = "RecommendCollectPager";
	// private PullToRefreshGridView mGridView = null;
	ListView mListView;
	RecommendCollectListAdapter mAdapter;
	private int currentIndex = 1;
	private RecomendCollectResponse bean;
	TextView select_class_Tv;
	TextView class_name_Tv;
	String param = null;
	private boolean isHasMore = true;
	HotTagsAdapter mHotTagsAdapter;
	private ArrayList<RecomendTagsResponse> rs;
//	private String currentCategory;
	private AlertDialog rightPopupWindow = null;
	private ListView listView;
	private TextView all_Tv;

	public RecommendCollectPager(FragmentActivity activity) {
		super(activity);
		mAdapter = new RecommendCollectListAdapter(activity);
		setNeedAddWaitingView(true);
	}

	public void onActivityCreated() {
	}

	/**
	 * 初始化界面
	 */
	public View onCreateView() {
		LayoutInflater inflater = LayoutInflater.from(activity);
		View root = inflater.inflate(R.layout.collect_page_list_layout, null);
		mListView = (ListView) root.findViewById(R.id.mListView);
		View headerView = inflater.inflate(R.layout.head_collect_page_layout,
				null);
		// mGridView = (PullToRefreshGridView)
		// root.findViewById(R.id.mGridView);
		select_class_Tv = (TextView) headerView
				.findViewById(R.id.select_class_Tv);
		class_name_Tv = (TextView) headerView.findViewById(R.id.class_name_Tv);
		param = class_name_Tv.getText().toString();

		mListView.setAdapter(mAdapter);
		mListView.addHeaderView(headerView);
		loading = LayoutInflater.from(activity).inflate(
				R.layout.footer_loading_small, null);
		noLoading = LayoutInflater.from(activity).inflate(
				R.layout.footer_no_loading, null);
		mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));
		select_class_Tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (rightPopupWindow != null) {
					if (!rightPopupWindow.isShowing()) {
						rightPopupWindow.show();
					} else {
						rightPopupWindow.dismiss();
					}
				} else {
					initPop();
				}
			}

		});

		if (this.activity instanceof MainActivity) {
			((MainActivity) this.activity).setmTagsOnItemClick(this);
		}
		return root;
	}

	OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:
				if (lastVisiblePosition >= view.getAdapter().getCount() - 1
						&& !isLoadingMore) {
					if (isHasMore) {
						isLoadingMore = true;
						addFootView();
						if (TextUtils.isEmpty(param)||activity.getString(R.string.all_param).equals(param)) {
							questData();
						} else {
							questCategoryData();

						}
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
	private Handler collectHandler = new Handler() {

		public void handleMessage(Message msg) {
			hideWaiting();
			removeFootView();
			removeFootViewNoMore();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				isLoadingMore=false;
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				bean = gson.fromJson(element, RecomendCollectResponse.class);
				if (currentIndex == 1) {
					mAdapter.setData(bean.collects);
				} else {
					mAdapter.addData(bean.collects);
				}
				if (!bean.more) {
					addFootViewNoMore();
					currentIndex = 1;
					isHasMore = false;
				} else {
					isHasMore = true;
					currentIndex++;
					removeFootViewNoMore();
				}
				break;
			case RequestResCode.REQUEST_FAILE:
				isLoadingMore=false;
				if (null != mAdapter && mAdapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							questData();
						}
					});
				}
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				isLoadingMore=false;
				break;
			}
		};
	};
	private ImageView back_Iv;

	public void loadData() {
		if (null != mAdapter && mAdapter.getCount() == 0) {
			questData();
		}
		questTagsData();
	}

	/**
	 * 
	 * 请求全部
	 * 
	 * @return void
	 * @see
	 */
	private void questData() {
		JLog.i(TAG, "请求全部currentIndex="+currentIndex);
		BannerTask task = new BannerTask(xiamiSDK,
				RequestMethods.COLLECT_RECOMMEND, collectHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("page", currentIndex);
		params.put("limit", 21);// 接口不知道什么原因limit传入 返回limit值-1条
		task.execute(params);
	}
	/**
	 * 
	 * 根据不同类型获取数据 
	 * @return void 
	 * @see
	 */
	private void questCategoryData() {
		JLog.i(TAG, "根据不同类型获取数据 currentIndex="+currentIndex+"--this.param="+this.param);
			BannerTask task = new BannerTask(xiamiSDK,
					RequestMethods.SEARCH_COLLECTS, collectHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("page", currentIndex);
		params.put("limit", 21);
		params.put("key", this.param);
		task.execute(params);
	}

	@Override
	public void onDestroy() {
		if (collectHandler != null) {
			collectHandler.removeCallbacksAndMessages(null);
		}
	}

	@Override
	public String getPageName() {
		return "RecommendCollectPager";
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void callBack(String param) {
		if (rightPopupWindow != null && rightPopupWindow.isShowing()) {
			rightPopupWindow.dismiss();
			mHotTagsAdapter.notifyDataSetChanged();
			mHotTagsAdapter.setCurrentCategory(param);
		}
		if (!TextUtils.isEmpty(param)) {
			if (!this.param.equals(param)) {
				this.param = param;
				class_name_Tv.setText(param);
				currentIndex = 1;
				if (activity.getString(R.string.all_param).equals(param)) {
					questData();
				} else {
					questCategoryData();

				}
			}

			if (activity.getString(R.string.all_param).equals(this.param)) {
				all_Tv.setSelected(true);
			} else {
				all_Tv.setSelected(false);
			}
		}

	}

	/**
	 * @Description:[初始化popwindow]
	 */
	private void initPop() {
		rightPopupWindow = new AlertDialog.Builder(activity,
				R.style.show_addmusic_dialog_style).create();
		rightPopupWindow.show();
		View loginwindow = (View) activity.getLayoutInflater().inflate(
				R.layout.popwindow_tags_layout, null);
		listView = (ListView) loginwindow.findViewById(R.id.mListView);
		all_Tv = (TextView) loginwindow.findViewById(R.id.all_Tv);
		all_Tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				callBack(all_Tv.getText().toString());

			}
		});
		if (!TextUtils.isEmpty(this.param)) {
			if (activity.getString(R.string.all_param).equals(this.param)) {
				all_Tv.setSelected(true);
			} else {
				all_Tv.setSelected(false);
			}
		} else {
			all_Tv.setSelected(true);
		}

		back_Iv = (ImageView) loginwindow.findViewById(R.id.back_Iv);
		mHotTagsAdapter = new HotTagsAdapter(activity);
		listView.setAdapter(mHotTagsAdapter);
		if (rs == null || rs.size() <= 0) {
			questTagsData();
		} else {
			mHotTagsAdapter.setData(rs);
		}
		mHotTagsAdapter.setCallBack(this);

		Window window = rightPopupWindow.getWindow();
		// 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
		window.setContentView(loginwindow);

		DisplayMetrics dm = new DisplayMetrics();
		dm = activity.getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
		WindowManager.LayoutParams p = window.getAttributes();
		p.width = screenWidth;
		p.height = WindowManager.LayoutParams.WRAP_CONTENT;
		// p.alpha = (float) 0.8;
		window.setAttributes(p);

		// *** 主要就是在这里实现这种效果的
		window.setGravity(Gravity.CENTER | Gravity.BOTTOM); // 此处可以设置dialog显示的位置

		rightPopupWindow.setContentView(loginwindow);

		back_Iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (rightPopupWindow != null && rightPopupWindow.isShowing()) {
					rightPopupWindow.dismiss();
				}
			}
		});
	}

	private void questTagsData() {
		BannerTask task = new BannerTask(xiamiSDK,
				RequestMethods.COLLECT_RECOMMEND_TAGS, tagstHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		task.execute(params);
	}

	private Handler tagstHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();// RecomendTagsResponse
				JsonElement element = (JsonElement) msg.obj;
				rs = new ArrayList<RecomendTagsResponse>();
				Type type = new TypeToken<ArrayList<RecomendTagsResponse>>() {
				}.getType();
				rs = gson.fromJson(element, type);
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
			mListView.removeFooterView(loading);
			hasFootView = false;
		}

	}

	/**
	 * 加载更多
	 */
	private void addFootView() {
		removeFootViewNoMore();
		mListView.addFooterView(loading);
		hasFootView = true;
	}

	/**
	 * 添加无更多加载
	 */
	private void addFootViewNoMore() {
		removeFootView();
		mListView.addFooterView(noLoading, null, false);
		hasFootViewNoMore = true;
	}

	private void removeFootViewNoMore() {
		if (hasFootViewNoMore) {
			mListView.removeFooterView(noLoading);
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
}
