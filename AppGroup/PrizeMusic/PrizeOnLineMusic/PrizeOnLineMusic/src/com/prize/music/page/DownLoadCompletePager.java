package com.prize.music.page;

import java.util.ArrayList;
import java.util.Collections;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.prize.app.constants.Constants;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadHelper;
import com.prize.app.download.UIDownLoadListener;
import com.prize.app.util.JLog;
import com.prize.music.activities.BatchEditDownloadedActivity;
import com.prize.music.activities.DownLoadManagerActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.DownLoadedSongsAdapter;
import com.prize.music.ui.adapters.DownLoadedSongsAdapter.INoData;
import com.prize.music.ui.fragments.DetailListFragment;
import com.prize.music.views.ParabolaView;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 **
 * 下载完成
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class DownLoadCompletePager extends BasePager {
	private static final String TAG = "DownLoadCompletePager";
	private ArrayList<SongDetailInfo> items = new ArrayList<SongDetailInfo>();

	/** 推荐应用游戏列表 **/
	private ListView gameListView;
	private boolean isNeedRefresh = false;
	// private FragmentActivity activity;
	DownLoadedSongsAdapter mRankAdapter;
	private RelativeLayout nota_Rlyt;
	private ParabolaView parabolaView;
	private boolean isRegistered = false;//prize-public-bug:22386 broadcast receiver didn't register-pengcancan-20160920-start

	public DownLoadCompletePager(FragmentActivity activity) {
		super(activity);
		this.activity = activity;
		mRankAdapter = new DownLoadedSongsAdapter(activity);
		setNeedAddWaitingView(true);
	}

	public void onActivityCreated() {
	}

	/**
	 * 初始化界面
	 */
	public View onCreateView() {
		LayoutInflater inflater = LayoutInflater.from(activity);
		View root = inflater.inflate(R.layout.down_complete_listview, null);
		gameListView = (ListView) root.findViewById(R.id.rank_list);
		nota_Rlyt = (RelativeLayout) root.findViewById(R.id.nota_Rlyt);
		View shuffle_temp = View.inflate(activity,
				R.layout.downloaded_shuffle_all, null);
		ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
		parabolaView = (ParabolaView) rootView.findViewById(R.id.parabolaView1);
		gameListView.addHeaderView(shuffle_temp);
		shuffle_temp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mRankAdapter == null || items == null || items.size() <= 0) {
					return;
				}
				MusicUtils.playSongDetailInfo(activity, items.get(0),
						DownLoadCompletePager.class.getSimpleName(), -1L,
						items, Constants.TYPE_PLAYLIST);

			}
		});
		gameListView.setAdapter(mRankAdapter);
		gameListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mRankAdapter.getItem(position
						- gameListView.getHeaderViewsCount()) == null)
					return;
				MusicUtils.playSongDetailInfo(
						activity,
						mRankAdapter.getItem(position
								- gameListView.getHeaderViewsCount()),
						DetailListFragment.class.getSimpleName(), -1L, items,
						Constants.TYPE_PLAYLIST);
				// 动画
				ImageView icon_fly = (ImageView) view
						.findViewById(R.id.icon_fly);
				if (parabolaView != null) {
					ImageView bottomView = null;
					if (activity instanceof DownLoadManagerActivity) {
						bottomView = ((DownLoadManagerActivity) activity)
								.getBottomView();
					}
					parabolaView.setAnimationPara(icon_fly, bottomView);
					if (!parabolaView.isRunning()) {
						parabolaView.showMovie();
					}
				}
			}
		});

		gameListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (items != null || items.size() >= 0) {
					Intent intent = new Intent(activity,
							BatchEditDownloadedActivity.class);
					intent.putParcelableArrayListExtra(
							Constants.INTENTTRANSBEAN, items);
					activity.startActivity(intent);
					return true;
				} else {
					return false;
				}
			}
		});
		AppManagerCenter.setDownloadRefreshHandle(listener);
		mRankAdapter.setmINoData(new INoData() {

			@Override
			public void onNoCallBack() {
				responseNoData(null);
			}
		});

		return root;
	}

	private UIDownLoadListener listener = new UIDownLoadListener() {

		protected void onFinish(int song_Id) {
			isNeedRefresh = true;
			loadData();
		}

		@Override
		public void onRefreshUI(int song_Id) {
		};
	};

	public void loadData() {
		if (null != mRankAdapter && mRankAdapter.getCount() == 0
				|| isNeedRefresh) {
			questData();
			isNeedRefresh = false;
		}

	}

	private void questData() {

		items = GameDAO.getInstance().getDownLoadedAppList();
		Collections.reverse(items);
		JLog.i(TAG, "list-=" + items.size());
		filterDeleteSong();
		hideWaiting();
		responseNoData(items);
	}

	private void responseNoData(ArrayList<SongDetailInfo> items) {
		if (items == null || items.size() <= 0) {
			nota_Rlyt.setVisibility(View.VISIBLE);
		} else {
			nota_Rlyt.setVisibility(View.GONE);
			mRankAdapter.setData(items);
		}
	}

	@Override
	public void onDestroy() {
		if (mRankAdapter != null) {
			mRankAdapter.setmINoData(null);
		}
		AppManagerCenter.removeDownloadRefreshHandle(listener);
	}

	@Override
	public String getPageName() {
		return "DownLoadCompletePager";
	}

	@Override
	public void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		filter.addAction(ApolloService.PLAYSTATE_CHANGED);
		activity.registerReceiver(mMediaStatusReceiver, filter);
		isRegistered = true;//prize-public-bug:22386 broadcast receiver didn't register-pengcancan-20160920
		questData();
		super.onResume();
	}

	@Override
	public void onPause() {
		//prize-public-bug:22386 broadcast receiver didn't register-pengcancan-20160920-start
		if (isRegistered) {
			activity.unregisterReceiver(mMediaStatusReceiver);
			isRegistered = false;
		}
		//prize-public-bug:22386 broadcast receiver didn't register-pengcancan-20160920-end
		super.onPause();
	}

	/**
	 * p 更新数据在需要的时候
	 */
	private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context activity, Intent intent) {
			if (gameListView != null && mRankAdapter != null) {
				mRankAdapter.notifyDataSetChanged();
			}
		}
	};

	void filterDeleteSong() {
		ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
		if (this.items == null || this.items.size() <= 0)
			return;
		int len = this.items.size();
		for (int index = 0; index < len; index++) {
			SongDetailInfo info = this.items.get(index);
			if (DownloadHelper.isFileExists(info)) {
				list.add(info);
			} else {
				AppManagerCenter.cancelDownload(info);
			}
		}
		this.items.clear();
		this.items.addAll(list);
	}

}
