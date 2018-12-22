/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：主界面
 *当前版本：V1.0
 *作  者：longbaoxiu
 *完成日期：2015-7-21
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ********************************************/
package com.prize.music.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadHelper;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.ToastUtils;
import com.prize.music.IApolloService;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.MusicInfo;
import com.prize.music.helpers.DownLoadUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.MusicUtils.AddCollectCallBack;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.adapters.BatchEditAdapter;
import com.prize.music.ui.fragments.base.PromptDialogFragment;
import com.prize.music.R;
import com.prize.onlinemusibean.PopBean;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 类描述：批编辑
 *
 * @author longbaoxiu
 * @version v1.0
 */
public class BatchSelectActivity extends FragmentActivity implements
		OnClickListener, OnItemClickListener, ServiceConnection {
	private ListView batch_list;
	private TextView cancel_Tv;
	private TextView item_select_Tv;
	private TextView all_select_Tv;
	private static TextView mEdit_add;
	private static TextView mEdit_delete;
	private static TextView mEdit_download;
	private static TextView mEdit_collection;
	private ArrayList<SongDetailInfo> items = new ArrayList<SongDetailInfo>();
	private static ArrayList<SongDetailInfo> itemSelected = new ArrayList<SongDetailInfo>();
	/***** 已下载 ***/
	private static ArrayList<SongDetailInfo> itemSelectedDownlaoded = new ArrayList<SongDetailInfo>();
	/***** 待下载的任务 ***/
	private static ArrayList<SongDetailInfo> itemSelectedDownlaod = new ArrayList<SongDetailInfo>();
	static BatchEditAdapter mBatchEditAdapter;
	private final String TAG = "BatchSelectActivity";
	private PopupWindow window;
	protected TextView popu_cancle;
	private ArrayList<PopBean> areaDatas = new ArrayList<PopBean>();
	private PromptDialogFragment df;
	private ArrayList<SongDetailInfo> waiterDeleteList;
	private ServiceToken mToken;

	private AddCollectCallBack callback = new AddCollectCallBack() {

		@Override
		public void addCollectResult(boolean result,String tableName) {
			if (result) {
				ToastUtils.showToast(R.string.addSuccessful);
			}
			updateState();
		}

		@Override
		public void isCollected() {
			
			ToastUtils.showToast(R.string.song_has_bean);
			
		}

	};

	private void updateState() {
		if (all_select_Tv != null && mBatchEditAdapter != null
				&& item_select_Tv != null) {

			all_select_Tv.setText(R.string.all_select);
			mBatchEditAdapter.selectAllItem(false);
			item_select_Tv.setText(getString(R.string.number_item_selected, 0));
		}
		responseItemClick();
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		StateBarUtils.initStateBar(this,
				getResources().getColor(R.color.statusbar_color));
		setContentView(R.layout.batch_edit_layout);
		StateBarUtils.changeStatus(getWindow());
		findViewById();
		init();
		setListener();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void setListener() {
		cancel_Tv.setOnClickListener(this);
		mEdit_collection.setOnClickListener(this);
		mEdit_download.setOnClickListener(this);
		mEdit_delete.setOnClickListener(this);
		mEdit_add.setOnClickListener(this);
		all_select_Tv.setOnClickListener(this);
		batch_list.setOnItemClickListener(this);

	}

	private void findViewById() {
		batch_list = (ListView) findViewById(R.id.batch_list);
		mEdit_collection = (TextView) findViewById(R.id.mEdit_collection);
		mEdit_download = (TextView) findViewById(R.id.mEdit_download);
		mEdit_delete = (TextView) findViewById(R.id.mEdit_delete);
		mEdit_add = (TextView) findViewById(R.id.mEdit_add);
		cancel_Tv = (TextView) findViewById(R.id.cancel_Tv);
		item_select_Tv = (TextView) findViewById(R.id.item_select_Tv);
		all_select_Tv = (TextView) findViewById(R.id.all_select_Tv);
		mEdit_download.setActivated(false);
	}

	private void init() {
		if (getIntent() != null
				&& getIntent().getParcelableArrayListExtra(
						Constants.INTENTTRANSBEAN) != null) {
			items = getIntent().getParcelableArrayListExtra(
					Constants.INTENTTRANSBEAN);
			if (items != null && items.size() > 0) {
				mBatchEditAdapter = new BatchEditAdapter(this);
				mBatchEditAdapter.setData(items);
				batch_list.setAdapter(mBatchEditAdapter);
				item_select_Tv.setText(getString(R.string.number_item_selected,
						0));
			} else {
				this.finish();
			}
		} else {
			this.finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancel_Tv:
			this.finish();
			break;
		case R.id.all_select_Tv:
			if (mBatchEditAdapter.isSelectAll()) {
				all_select_Tv.setText(R.string.all_select);
				mBatchEditAdapter.selectAllItem(false);
			} else {
				mBatchEditAdapter.selectAllItem(true);
				all_select_Tv.setText(R.string.no_select);
			}
			int selectCount = mBatchEditAdapter.getSelectedAudioIds().length;
			item_select_Tv.setText(getString(R.string.number_item_selected,
					selectCount));
			responseItemClick();
			break;
		case R.id.item_select_Tv:
			break;
		case R.id.mEdit_add:
			if (itemSelected == null || itemSelected.size() <= 0) {
				ToastUtils.showToast(R.string.pl_select);
				return;
			}
			int len = itemSelected.size();
			List<MusicInfo> listMusicInfo = new ArrayList<MusicInfo>();
			for (int i = 0; i < len; i++) {
				listMusicInfo.add(MusicUtils.SongDetailInfoToMusicInfo(
						itemSelected.get(i), CommonUtils.queryUserId()));
			}
			MusicUtils.addAllMusicToTableDialog(this, listMusicInfo, callback,null);
			break;
		case R.id.mEdit_delete:
			processAction(Constants.ACTION_DELETE);
			break;
		case R.id.mEdit_download:

			ArrayList<SongDetailInfo> list = CommonUtils
					.filterHadDownloadedSong(false, itemSelected);
			// list = CommonUtils.filterUnabelListerSong(list);传入已经过滤
//			for (SongDetailInfo songDetailInfo : list) {
				DownLoadUtils.downloadMultMusic(list);
//			}
			if (list != null && list.size() > 0) {
				if (ClientInfo.networkType == ClientInfo.NONET) {
					ToastUtils.showToast(R.string.net_error);
					return;
				}
				ToastUtils.showToast(getString(R.string.num_download_task,
						list.size()));
			} else {
				ToastUtils.showToast(R.string.no_downloadTask);
			}
			updateState();
			break;
		case R.id.mEdit_collection:
			if (itemSelected == null || itemSelected.size() <= 0) {
				ToastUtils.showToast(R.string.pl_select);
				return;
			}
			if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
				UiUtils.jumpToLoginActivity();
				return;
			}

			MusicUtils.addAllOnLineSongDetailInfoToMyCollectClound(itemSelected,
					BatchSelectActivity.this, new AddCollectCallBack() {

						@Override
						public void addCollectResult(boolean result,String tableName) {
							ToastUtils.showToast(R.string.collectionSuccessful);
							updateState();

						}

						@Override
						public void isCollected() {
							ToastUtils.showToast(R.string.all_data_has_sort);
							
							
						}
					}, DatabaseConstant.TABLENAME_LOVE);

			// String headParams = new Gson().toJson(itemSelected);
			// requestSort(headParams);
			break;

		default:
			break;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mBatchEditAdapter.toggleCheckedState(position);
		int selectCount = mBatchEditAdapter.getSelectedAudioIds().length;
		item_select_Tv.setText(getString(R.string.number_item_selected,
				selectCount));
		if (selectCount == batch_list.getCount()) {
			mBatchEditAdapter.setIsSelectAll(true);
			all_select_Tv.setText(R.string.no_select);
		} else {
			all_select_Tv.setText(R.string.all_select);
			mBatchEditAdapter.setIsSelectAll(false);
		}
		responseItemClick();
	}

//	private void requestSort(String headParams) {
//		JLog.i(TAG, "headParams=" + headParams);
//		if (ClientInfo.networkType == ClientInfo.NONET) {
//			ToastUtils.showToast(R.string.nonet_connect);
//			return;
//		}
//		String url = Constants.GIS_URL + "/collection/songs";
//		RequestParams params = new RequestParams(url);
//		params.addBodyParameter("userId", 0 + "");
//		params.addBodyParameter("songs", headParams);
//		XExtends.http().post(params, new Callback.ProgressCallback<String>() {
//
//			@Override
//			public void onSuccess(String result) {
//				JSONObject o;
//				try {
//					o = new JSONObject(result);
//					if (o.getInt("code") == 0) {
//						ToastUtils.showToast(R.string.collectionSuccessful);
//						JSONObject data = (JSONObject) o.get("data");
//						int count = data.getInt("count");
//						if (count == 0) {
//							ToastUtils.showToast(R.string.all_data_has_sort);
//						} else {
//							ToastUtils.showToast(getString(
//									R.string.some_has_sort, count));
//						}
//					}
//				} catch (JSONException e) {
//
//					e.printStackTrace();
//
//				}
//
//			}
//
//			@Override
//			public void onError(Throwable ex, boolean isOnCallback) {
//				ToastUtils.showToast(R.string.net_error);
//			}
//
//			@Override
//			public void onCancelled(CancelledException cex) {
//
//			}
//
//			@Override
//			public void onFinished() {
//
//			}
//
//			@Override
//			public void onWaiting() {
//
//			}
//
//			@Override
//			public void onStarted() {
//
//			}
//
//			@Override
//			public void onLoading(long total, long current,
//					boolean isDownloading) {
//
//			}
//
//		});
//	}

	public void processAction(String action) {
			if (Constants.ACTION_DELETE.equals(action)) {
			waiterDeleteList = CommonUtils.filterHadDownloadedSong(true,
					itemSelected);
			if (waiterDeleteList.size() <= 0) {
				ToastUtils.showToast(R.string.nodata_hasDownloaded);
				return;
			}
			df = com.prize.music.ui.fragments.base.PromptDialogFragment
					.newInstance(
							"确定要删除已下载的" + waiterDeleteList.size() + "首歌吗？",
							mDeletePromptListener);
			df.setmListener(mDeletePromptListener);
			df.show(this.getSupportFragmentManager(), "deleteDialog");
			// }
		} 
			
//			else if (Constants.ACTION_ADD.equals(action)) {
//			final long[] selectIds = mBatchEditAdapter.getSelectedAudioIds();
//			if (selectIds == null || selectIds.length <= 0) {
//				return;
//			}
//			initAddPopu();
//		} else if (Constants.ACTION_CANCEL_FR_TO_FR.equals(action)) {
//			if (mBatchEditAdapter != null) {
//				mBatchEditAdapter.notifyDataSetChanged();
//				mBatchEditAdapter.selectAllItem(false);
//			}
//
//		}
	}
//
//	private void initAddPopu() {
//
//		if (areaDatas != null) {
//			areaDatas.clear();
//
//		}
//
//		if (this == null) {
//			return;
//		}
//		List<Map<String, Object>> mListss = MusicUtils.getTableList(this);
//
//		// List<Map<String, Object>> mListss = MeFragment.getTableList();
//		// 初始化弹出菜单
//		int len = mListss.size();
//		// if (len <= 0) {
//		for (int i = 0; i < len; i++) {
//			if (!mListss.get(i).get("name")
//					.equals(this.getString(R.string.create_list))) {
//				long id = (Long) mListss.get(i).get("id");
//				String name = (String) mListss.get(i).get("name");
//				PopBean mPopBean = new PopBean(id + "", name);
//				areaDatas.add(mPopBean);
//			}
//		}
//		// 初始化弹出菜单
//		View popupView = LayoutInflater.from(this).inflate(
//				R.layout.popupwindow_add_list, null);
//
//		LinearLayout popu_add_linearlayout = (LinearLayout) popupView
//				.findViewById(R.id.popu_add_linearlayout);
//		popu_cancle = (TextView) popupView.findViewById(R.id.popu_cancle);
//		if (window == null) {
//			window = new PopupWindow(popupView,
//					WindowManager.LayoutParams.MATCH_PARENT,
//					WindowManager.LayoutParams.WRAP_CONTENT);
//		}
//
//		// 设置菜单背景，不设置背景菜单不会显示
//		window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//		// 菜单外点击菜单自动消失
//		window.setOutsideTouchable(true);
//		// 初始化菜单上的按键，并设置监听
//		ListView li = (ListView) popupView.findViewById(R.id.popul_list);
//		MainPopAdapter pAdapter = new MainPopAdapter(BatchSelectActivity.this,
//				getPopWindowHandler(), areaDatas);
//
//		if (pAdapter != null && pAdapter.getCount() > 5) {
//			LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) popu_add_linearlayout
//					.getLayoutParams(); // 取控件mGrid当前的布局参数
//			linearParams.height = 600;// 当控件的高强制设成600象素
//			popu_add_linearlayout.setLayoutParams(linearParams);
//		}
//
//		li.setAdapter(pAdapter);
//		window.setAnimationStyle(R.style.mypopwindow_anim_style);
//		window.showAtLocation(batch_list, Gravity.BOTTOM, 0, 0);
//		popu_cancle.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				window.dismiss();
//
//			}
//		});
//	}

	/**
	 * @Description:[popwindow的handler]
	 * @return
	 */
	private Handler getPopWindowHandler() {
		return new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Bundle data = msg.getData();
				if (msg.what == 1) {
					long playlistid = data.getLong("selIndex");
					// String tabelName = data.getString("tabelName");
					boolean isExisted = MusicUtils.addTrackToPlaylist(
							BatchSelectActivity.this, playlistid,
							mBatchEditAdapter.getSelectedAudioIds());
					if (isExisted) {
						ToastUtils.showToast("添加歌曲已存在");
					} else {
						ToastUtils.showOnceToast(BatchSelectActivity.this,
								"添加成功");

					}
				}
				window.dismiss();
				// 通知取消刷MainActivity新界面
			}
		};
	}

	static class AsyncLoader_GuessInfo extends
			AsyncTask<Void, Void, OperataResut> {
		private WeakReference<Context> weakReference;
		String action = null;
		ProgressDialog dialog = null;
		ArrayList<SongDetailInfo> itemSelected = null;

		public AsyncLoader_GuessInfo(String action,
				ArrayList<SongDetailInfo> itemSelected, Context context) {
			this.action = action;
			this.itemSelected = itemSelected;
			weakReference = new WeakReference<>(context);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = new ProgressDialog(weakReference.get(),
					ProgressDialog.THEME_HOLO_LIGHT);
			 if (action.equals(Constants.ACTION_DELETE)) {
				dialog.setMessage("正在删除，请稍后...");
				if (mBatchEditAdapter.getSelectedAudioIds().length > 30) {
					dialog.show();
				}
			}
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(true);
		}

		@Override
		protected OperataResut doInBackground(Void... params) {
			OperataResut operataResut = new OperataResut();
			if (action.equals(Constants.ACTION_DELETE)) {
				long[] selectIds = mBatchEditAdapter.getSelectedAudioIds();
				// String[] paths = MusicUtils.getAudioPaths(
				// BatchSelectActivity.this, selectIds);
				int len = itemSelected.size();
				long currentId = MusicUtils.getCurrentAudioId();
				Arrays.sort(selectIds);
				int deleteNum = 0;
				for (int i = 0; i < len; i++) {
					SongDetailInfo songDetailInfo = itemSelected.get(i);
//					long currenPositiontId = songDetailInfo.song_id;
//					MusicUtils.removeTrack(currenPositiontId);
//					HistoryDao.getInstance(weakReference.get())
//							.deleteByAudioId(currenPositiontId);
					AppManagerCenter.cancelDownload(songDetailInfo);
					if (DownloadHelper.deleteDownloadedFile(songDetailInfo)) {
						deleteNum++;
					}
				}
				operataResut.num = deleteNum;
				operataResut.isSuccess = true;
//				if ((Arrays.binarySearch(selectIds, currentId)) >= 0) {
//					try {
//						MusicUtils.mService.stop();
//						MusicUtils.mService.next();
//					} catch (RemoteException e) {
//
//						e.printStackTrace();
//					}
//				}

			}

			return operataResut;
		}

		@Override
		// 处理界面
		protected void onPostExecute(OperataResut result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

		 if (action.equals(Constants.ACTION_DELETE)) {
				if (result.isSuccess) {
					if (result.num > 0) {
						ToastUtils.showOnceToast(
								weakReference.get(),
								weakReference.get().getString(
										R.string.num_item_deleteSuccessful,
										result.num));
					} else {
						ToastUtils.showOnceToast(
								weakReference.get(),
								weakReference.get().getString(
										R.string.nodata_hasDownloaded));
					}
				} else {
					ToastUtils.showOnceToast(weakReference.get(), weakReference
							.get().getString(R.string.deleteFail));

				}
				mBatchEditAdapter.notifyDataSetChanged();
				responseItemClick();
			}
		}
	}

	private View.OnClickListener mDeletePromptListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			df.dismissAllowingStateLoss();
			new AsyncLoader_GuessInfo(Constants.ACTION_DELETE,
					waiterDeleteList, BatchSelectActivity.this).execute();
		}

	};

	static class OperataResut {
		boolean isSuccess;
		int num;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		MusicUtils.mService = null;
	}

	@Override
	protected void onStart() {
		// Bind to Service
		mToken = MusicUtils.bindToService(this, this);
		super.onStart();
	}

	@Override
	protected void onStop() {
		// Unbind
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onStop();

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onDestroy();
	}

	/**
	 *  监听下载删除按钮状态
	 *  
	 * @return void 
	 * @see
	 */
	private static void responseItemClick() {
		itemSelected = mBatchEditAdapter.getSelectedSongDetailInfo();
		itemSelectedDownlaoded = CommonUtils.filterHadDownloadedSong(true,
				itemSelected);
		itemSelectedDownlaod = CommonUtils.filterHadDownloadedSong(false,
				itemSelected);

		if (itemSelected != null && itemSelected.size() > 0) {
			mEdit_collection.setActivated(true);
			mEdit_collection.setEnabled(true);
			mEdit_add.setActivated(true);
			mEdit_add.setEnabled(true);
		} else {
			mEdit_add.setActivated(false);
			mEdit_add.setEnabled(false);
			mEdit_collection.setActivated(false);
			mEdit_collection.setEnabled(false);
		}
		if (itemSelectedDownlaod != null && itemSelectedDownlaod.size() > 0) {
			mEdit_download.setActivated(true);
			mEdit_download.setEnabled(true);
		} else {
			mEdit_download.setActivated(false);
			mEdit_download.setEnabled(false);
		}
		if (itemSelectedDownlaoded != null && itemSelectedDownlaoded.size() > 0) {
			mEdit_delete.setEnabled(true);
			mEdit_delete.setActivated(true);
		} else {
			mEdit_delete.setActivated(false);
			mEdit_delete.setEnabled(false);
		}
	}
}
