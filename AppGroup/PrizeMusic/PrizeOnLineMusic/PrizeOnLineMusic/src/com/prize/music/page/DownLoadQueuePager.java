package com.prize.music.page;

import java.util.ArrayList;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.UIDownLoadListener;
import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.music.activities.BatchDeleteTaskActivity;
import com.prize.music.helpers.DownLoadUtils;
import com.prize.music.ui.adapters.DownLoadingSongsAdapter;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 **
 * 下载队列
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class DownLoadQueuePager extends BasePager implements OnClickListener {
	private static final String TAG = "DownLoadQueuePager";
	/** 推荐应用游戏列表 **/
	private ListView gameListView;

	private FragmentActivity context;
	DownLoadingSongsAdapter mRankAdapter;
	private ArrayList<SongDetailInfo> list=new ArrayList<SongDetailInfo>();
	private TextView all_pause_Tv;
	// private TextView all_down_Tv;
	private TextView num_downtask_Tv;
	private LinearLayout all_down_Llyt;
	private RelativeLayout nota_Rlyt;

	public DownLoadQueuePager(FragmentActivity activity) {
		super(activity);
		this.context = activity;
		mRankAdapter = new DownLoadingSongsAdapter(context);
		setNeedAddWaitingView(true);
	}

	public void onActivityCreated() {
	}

	/**
	 * 初始化界面
	 */
	public View onCreateView() {
		LayoutInflater inflater = LayoutInflater.from(activity);
		View root = inflater.inflate(R.layout.downing_listview, null);
		gameListView = (ListView) root.findViewById(R.id.rank_list);
		all_pause_Tv = (TextView) root.findViewById(R.id.all_pause_Tv);
		// all_down_Tv = (TextView) root.findViewById(R.id.all_down_Tv);
		num_downtask_Tv = (TextView) root.findViewById(R.id.num_downtask_Tv);
		all_down_Llyt = (LinearLayout) root.findViewById(R.id.all_down_Llyt);
		nota_Rlyt = (RelativeLayout) root.findViewById(R.id.nota_Rlyt);
		gameListView.setAdapter(mRankAdapter);
		gameListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mRankAdapter != null) {
					mRankAdapter.onItemClick(position);
				}
			}
		});

		gameListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (list != null || list.size() >= 0) {
					Intent intent = new Intent(context,
							BatchDeleteTaskActivity.class);
					intent.putParcelableArrayListExtra(
							Constants.INTENTTRANSBEAN, list);
					context.startActivity(intent);
					return true;
				} else {
					return false;
				}
			}
		});
		all_pause_Tv.setOnClickListener(this);
		// all_down_Tv.setOnClickListener(this);
		all_down_Llyt.setOnClickListener(this);
		AppManagerCenter.setDownloadRefreshHandle(listener);
		return root;
	}

	public void loadData() {
		if (null != mRankAdapter && mRankAdapter.getCount() == 0) {
			questData();

		}

	}

	private void questData() {
		list = GameDAO.getInstance().getDownAppList();
		JLog.i(TAG, "list.size()="+list.size());
		hideWaiting();
		if (list == null || list.size() <= 0) {
			nota_Rlyt.setVisibility(View.VISIBLE);
		} else {
			nota_Rlyt.setVisibility(View.GONE);
			mRankAdapter.setData(list);
			refreshView(!AppManagerCenter.hasDownloadingApp(), false,
					list.size(),AppManagerCenter.hasDownloadingApp());
		}
	}

	@Override
	public void onDestroy() {
		AppManagerCenter.removeDownloadRefreshHandle(listener);
	}

	private UIDownLoadListener listener = new UIDownLoadListener() {
		protected void onError(int song_Id) {
			refreshView(!AppManagerCenter.hasDownloadingApp(), false,
					list.size(),AppManagerCenter.hasDownloadingApp());

		}

		protected void onPause(int song_Id) {
			refreshView(!AppManagerCenter.hasDownloadingApp(), false,
					list.size(),AppManagerCenter.hasDownloadingApp());
		};

		protected void onStart(int song_Id) {
			refreshView(!AppManagerCenter.hasDownloadingApp(), false,
					list.size(),AppManagerCenter.hasDownloadingApp());
		};

		protected void onFinish(int song_Id) {
			questData();
		}

		@Override
		public void onRefreshUI(int song_Id) {
		};
	};

	@Override
	public String getPageName() {
		return "DownLoadQueuePager";
	}

	@Override
	public void onResume() {
		if (mRankAdapter != null) {
			mRankAdapter.setIsActivity(true);
			mRankAdapter.setDownlaodRefreshHandle();
		}

		questData();
		super.onResume();
	}

	@Override
	public void onPause() {
		if (mRankAdapter != null) {
			mRankAdapter.setIsActivity(false);
			mRankAdapter.removeDownLoadHandler();
		}
		super.onPause();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.all_down_Llyt:
			if (mRankAdapter != null && mRankAdapter.getCount() <= 0) {
				return;
			}
			if (ClientInfo.networkType == ClientInfo.NONET) {
				ToastUtils.showToast(R.string.net_error);
				return;

			}
			DownLoadUtils.downloadMultMusic(list);
			refreshView(false, true, list.size(),AppManagerCenter.hasDownloadingApp());
			break;
		case R.id.all_pause_Tv:
			if (mRankAdapter != null && mRankAdapter.getCount() <= 0) {
				return;
			}
			AppManagerCenter.pauseBatchDownload();
//			AppManagerCenter.pauseAllDownload();
			refreshView(true, true, list.size(),AppManagerCenter.hasDownloadingApp());
			break;

		default:
			break;
		}

	}

	/**
	 * 改变全部下载及全部暂停的状态
	 * 
	 * @param pause
	 * @param size
	 * @return void
	 * @see
	 */
	private void refreshView(boolean pause, boolean isClick, int size,boolean hasTask) {
		if (isClick) {
			if (pause) {
				all_down_Llyt.setVisibility(View.VISIBLE);
				all_pause_Tv.setVisibility(View.GONE);
				num_downtask_Tv.setText("(" + size + ")");
				if (mRankAdapter != null) {
					mRankAdapter.notifyDataSetChanged();
				}
			} else {
				all_pause_Tv.setVisibility(View.VISIBLE);
				all_down_Llyt.setVisibility(View.GONE);
			}
		} else {
			if (hasTask) {//有下载任务
//				if(size == 1){//只有一个时，显示全部暂停
				if(size>=1){
					all_pause_Tv.setVisibility(View.VISIBLE);
					all_down_Llyt.setVisibility(View.GONE);
				}else{
					ArrayList<SongDetailInfo > pauseArrayList = new ArrayList<SongDetailInfo>();
					boolean isHasPauseTask=false;
					for(SongDetailInfo bean : list){
						int stat = AppManagerCenter.getGameAppState(bean);
						if( stat==AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE){
							pauseArrayList.add(bean);
							if(pauseArrayList.size()>0){
								isHasPauseTask=true;
								break;
							}
						}
					}
					
					if(isHasPauseTask){
						all_down_Llyt.setVisibility(View.VISIBLE);
						all_pause_Tv.setVisibility(View.GONE);
						num_downtask_Tv.setText("(" + size + ")");
					}else{
						all_pause_Tv.setVisibility(View.VISIBLE);
						all_down_Llyt.setVisibility(View.GONE);
					}
				}
			} else {
				all_down_Llyt.setVisibility(View.VISIBLE);
				all_pause_Tv.setVisibility(View.GONE);
				num_downtask_Tv.setText("(" + size + ")");
			}

		}

	}

}
