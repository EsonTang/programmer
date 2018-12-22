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

package com.prize.appcenter.activity;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.PrizeTopicTypeData;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.TopicListAdapter;
import com.prize.appcenter.ui.datamgr.TopicManager;
import com.prize.appcenter.ui.util.ToastUtils;
import com.umeng.analytics.MobclickAgent;

/**
 ** 
 * 专题
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class TopicActivity extends ActionBarNoTabActivity {
	protected final String TAG = "TopicActivity";
	private ListView mListView;
	private TopicListAdapter mTopicListAdapter;
	private TopicManager mTopicManager;
	private boolean hasFootView = false;
	// 加载更多
	private View loading = null;
	// 无更多内容加载
	private View noLoading = null;
	private boolean hasFootViewNoMore = true;

	// 当前可见的最后位置
	private int lastVisiblePosition;
	private boolean isLoadMore = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setNeedAddWaitingView(true);
		setContentView(R.layout.activity_game_ranking);
		WindowMangerUtils.changeStatus(getWindow());
		String title = getIntent().getStringExtra("title");
		if (!TextUtils.isEmpty(title)) {
			super.setTitle(title);
		} else {
			super.setTitle(R.string.hot_topic);
		}

		findViewById();

		init();

		setListener();

	}

	OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (lastVisiblePosition >= mTopicListAdapter.getCount() - 1
					&& isLoadMore) {
				isLoadMore = false;
				// 分页显示
				if (mTopicManager.isListNextPage()) {
					addFootView();
					mTopicManager.getTopicListData(TAG);
				} else {
					// if (!hasFootViewNoMore) {
					addFootViewNoMore();
					// }
				}
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastVisiblePosition = mListView.getLastVisiblePosition();
		}
	};

	private void setListener() {

		mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Map<String, String> map = new HashMap<String, String>();
				map.put(Constants.EVT_CLICK_APP_TOPIC_ID,
						Constants.E_CLICK_APP_TOPIC);
				MobclickAgent.onEventValue(
						MainApplication.curContext,
						Constants.EVT_CLICK_APP_TOPIC_ID
								+ String.valueOf(position + 1), map, 100);

				Intent intent = new Intent(TopicActivity.this,
						TopicDetailActivity.class);
				Bundle b = new Bundle();
				b.putSerializable("bean", mTopicListAdapter.getItem(position));
				intent.putExtras(b);
				startActivity(intent);
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
			}
		});

	}

	private void init() {
		loading = LayoutInflater.from(this).inflate(
				R.layout.footer_loading_small, null);
		noLoading = LayoutInflater.from(this).inflate(
				R.layout.footer_no_loading, null);
		requestData();
	}

	private void findViewById() {

		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setDividerHeight(10);
	}

	@Override
	public String getActivityName() {
		return "NewProductorActivity";
	}

	private void requestData() {
		showWaiting();
		if (mTopicListAdapter == null) {
			mTopicListAdapter = new TopicListAdapter(this);
		}
		mListView.setAdapter(mTopicListAdapter);
		if (mTopicManager == null) {
			mTopicManager = new TopicManager(this, null, null);
		}
		mTopicManager.getTopicListData(TAG);

	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {
		hideWaiting();
		switch (what) {
		case TopicManager.TOPIC_SUCCESS__LIST:// 请求成共返回
			PrizeTopicTypeData data = (PrizeTopicTypeData) obj;
			removeFootView();
			if (mTopicListAdapter != null) {
				mTopicListAdapter.addData(data.topics);
				if (!mTopicManager.isListNextPage()) {
					addFootViewNoMore();
				}
			}
			break;

		case TopicManager.TOPIC_FAILE_LIST:
			removeFootView();
			ToastUtils.showErrorToast(R.string.toast_net_error);
			if (mTopicListAdapter != null) {
				try {
					mTopicListAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;

		case NetSourceListener.WHAT_NETERR:
			loadingFailed(new ReloadFunction() {

				@Override
				public void reload() {
					requestData();
				}

			});
			removeFootView();
			break;
		}
	}

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
		if (hasFootView) {
			return;
		}
		mListView.addFooterView(loading);
		hasFootView = true;
	}

	/**
	 * 添加无更多加载
	 */
	private void addFootViewNoMore() {
		if (hasFootViewNoMore) {
			removeFootView();
			mListView.addFooterView(noLoading, null, false);
			hasFootViewNoMore = false;
		}
	}

	@Override
	protected void onDestroy() {
		if (mTopicManager != null) {
			mTopicManager.setNullListener();
		}
		BaseApplication.cancelPendingRequests(TAG);
		super.onDestroy();
	}
}
