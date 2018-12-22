package com.prize.music.page;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.xiami.RequestManager;
import com.prize.music.online.task.RankTask;
import com.prize.music.ui.adapters.RankAdapter;
import com.prize.music.R;
import com.prize.onlinemusibean.RecomendRankBean;
import com.prize.onlinemusibean.response.RecomendRankResponse;

/**
 **
 * 首页排行界面
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RankPager extends BasePager {

	/** 推荐应用游戏列表 **/
	private ListView gameListView;

	private FragmentActivity context;
	private RequestManager requestManager;
	RankAdapter mRankAdapter;

	public RankPager(FragmentActivity activity) {
		super(activity);
		this.context = activity;
		requestManager = RequestManager.getInstance();
		mRankAdapter = new RankAdapter(context);
		setNeedAddWaitingView(true);
	}

	public void onActivityCreated() {
	}

	/**
	 * 初始化界面
	 */
	public View onCreateView() {
		LayoutInflater inflater = LayoutInflater.from(activity);
		View root = inflater.inflate(R.layout.listview, null);
		gameListView = (ListView) root.findViewById(R.id.rank_list);

		gameListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), true, true, mOnScrollListener));
		gameListView.setAdapter(mRankAdapter);
		gameListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (mRankAdapter != null) {
					// UiUtils.gotoMoreDaily(context,
					// mRankAdapter.getItem(position).type,
					// Constants.KEY_RANK);
					mRankAdapter.onItemClick(position);
				}

			}
		});
		return root;
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
	private Handler rankHandler = new Handler() {
		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case com.prize.app.constants.RequestResCode.REQUEST_OK:
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
				mRankAdapter.setData(items);
				break;
			case RequestResCode.REQUEST_FAILE:
				if (null != mRankAdapter && mRankAdapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							questData();
						}
					});
				}
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	public void loadData() {
		if (null != mRankAdapter && mRankAdapter.getCount() == 0) {
			questData();

		}

	}

	private void questData() {
		RankTask task = new RankTask(xiamiSDK, RequestMethods.RANK_LIST,
				rankHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		task.execute(params);
	}

	@Override
	public void onDestroy() {
		if (rankHandler != null) {
			rankHandler.removeCallbacksAndMessages(null);
		}
	}

	@Override
	public String getPageName() {
		return "RankPager";
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

}
