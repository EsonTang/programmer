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
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadHelper;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.custmerxutils.XExtends;
import com.prize.music.IApolloService;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.MusicInfo;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.MusicUtils.AddCollectCallBack;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.adapters.BatchDownloadedEditAdapter;
import com.prize.music.ui.fragments.base.PromptDialogFragment;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 类描述：批编辑已下载数据
 *
 * @author longbaoxiu
 * @version v1.0
 */
public class BatchEditDownloadedActivity extends FragmentActivity implements
		OnClickListener, OnItemClickListener, ServiceConnection {
	private ListView batch_list;
	private TextView cancel_Tv;
	private static TextView item_select_Tv;
	private static TextView all_select_Tv;
	private TextView mEdit_add;
	private TextView mEdit_delete;
	private TextView mEdit_setRing;
	private TextView mEdit_collection;
	private static ArrayList<SongDetailInfo> items = new ArrayList<SongDetailInfo>();
	private ArrayList<SongDetailInfo> itemSelected = new ArrayList<SongDetailInfo>();
	static BatchDownloadedEditAdapter mBatchEditAdapter;
	private final String TAG = "BatchSelectActivity";
	static RelativeLayout nota_Rlyt;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		StateBarUtils.initStateBar(this,
				getResources().getColor(R.color.statusbar_color));
		setContentView(R.layout.batch_editdownloaded_layout);
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
		mEdit_setRing.setOnClickListener(this);
		mEdit_delete.setOnClickListener(this);
		mEdit_add.setOnClickListener(this);
		all_select_Tv.setOnClickListener(this);
		batch_list.setOnItemClickListener(this);

	}

	private void findViewById() {
		batch_list = (ListView) findViewById(R.id.batch_list);
		mEdit_collection = (TextView) findViewById(R.id.mEdit_collection);
		mEdit_setRing = (TextView) findViewById(R.id.mEdit_setRing);
		mEdit_delete = (TextView) findViewById(R.id.mEdit_delete);
		mEdit_add = (TextView) findViewById(R.id.mEdit_add);
		cancel_Tv = (TextView) findViewById(R.id.cancel_Tv);
		item_select_Tv = (TextView) findViewById(R.id.item_select_Tv);
		all_select_Tv = (TextView) findViewById(R.id.all_select_Tv);
		nota_Rlyt = (RelativeLayout) findViewById(R.id.nota_Rlyt);
	}

	private void init() {
		if (getIntent() != null
				&& getIntent().getParcelableArrayListExtra(
						Constants.INTENTTRANSBEAN) != null) {
			items = getIntent().getParcelableArrayListExtra(
					Constants.INTENTTRANSBEAN);
			if (items != null && items.size() > 0) {
				mBatchEditAdapter = new BatchDownloadedEditAdapter(this);
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
		case R.id.mEdit_setRing:

			MusicUtils
					.setRingtone(getApplicationContext(), itemSelected.get(0));
			break;
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
			itemSelected.clear();
			int[] postionsDel = mBatchEditAdapter.getSelectedItemPositions();
			for (int i = 0; i < postionsDel.length; i++) {
				itemSelected.add(items.get(postionsDel[i]));
			}
			if (itemSelected == null || itemSelected.size() <= 0) {
				return;
			}
			df = PromptDialogFragment.newInstance("确定要删除" + itemSelected.size()
					+ "首歌吗？", mDeletePromptListener);
			df.setmListener(mDeletePromptListener);
			df.show(getSupportFragmentManager(), "delete");
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

			MusicUtils.addAllOnLineSongDetailInfoToMyCollectClound(
					itemSelected, BatchEditDownloadedActivity.this,
					new AddCollectCallBack() {

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

	void requestSort(String headParams) {
		JLog.i(TAG, "headParams=" + headParams);
		if (ClientInfo.networkType == ClientInfo.NONET) {
			ToastUtils.showToast(R.string.nonet_connect);
			return;
		}
		String url = Constants.GIS_URL + "/collection/songs";
		RequestParams params = new RequestParams(url);
		params.addBodyParameter("userId", 0 + "");
		params.addBodyParameter("songs", headParams);
		XExtends.http().post(params, new Callback.ProgressCallback<String>() {

			@Override
			public void onSuccess(String result) {
				JSONObject o;
				try {
					o = new JSONObject(result);
					if (o.getInt("code") == 0) {
						ToastUtils.showToast(R.string.collectionSuccessful);

					}
				} catch (JSONException e) {

					e.printStackTrace();

				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				ToastUtils.showToast(R.string.net_error);
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}

			@Override
			public void onWaiting() {

			}

			@Override
			public void onStarted() {

			}

			@Override
			public void onLoading(long total, long current,
					boolean isDownloading) {

			}

		});
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
				int len = itemSelected.size();
				int deleteNum = 0;
				AppManagerCenter.cancelBatchDownload(itemSelected);
				for (int i = 0; i < len; i++) {
					SongDetailInfo songDetailInfo = itemSelected.get(i);
					if (DownloadHelper.deleteDownloadedFile(songDetailInfo)) {
						deleteNum++;
					}
				}
				operataResut.num = deleteNum;
				operataResut.isSuccess = true;
			}
			return operataResut;
		}

		@Override
		// 处理界面
		protected void onPostExecute(OperataResut result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			// all_select_Tv.setText(R.string.all_select);
			// mBatchEditAdapter.selectAllItem(false);

			updateState();
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

				filterDeleteSong();
			}
		}
	}
	
	private void setEnabled(){
		mEdit_setRing.setActivated(false);
		mEdit_setRing.setEnabled(false);
		mEdit_delete.setEnabled(false);
		mEdit_delete.setActivated(false);
		mEdit_add.setActivated(false);
		mEdit_add.setEnabled(false);
		mEdit_collection.setActivated(false);
		mEdit_collection.setEnabled(false);
	}

	private View.OnClickListener mDeletePromptListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			df.dismissAllowingStateLoss();
			new AsyncLoader_GuessInfo(Constants.ACTION_DELETE, itemSelected,
					BatchEditDownloadedActivity.this).execute();
			setEnabled();
		}

	};

	static class OperataResut {
		boolean isSuccess;
		int num;
	}

	private PromptDialogFragment df;

	private static void filterDeleteSong() {
		ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();
		if (items == null || items.size() <= 0)
			return;
		int len = items.size();
		for (int index = 0; index < len; index++) {
			SongDetailInfo info = items.get(index);
			if (DownloadHelper.isFileExists(info)) {
				list.add(info);
			}

		}
		items.clear();
		items.addAll(list);
		if (items.size() <= 0) {
			nota_Rlyt.setVisibility(View.VISIBLE);
		}
		mBatchEditAdapter.setData(items);

	}

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
	private ServiceToken mToken;

	private static void updateState() {
		if (all_select_Tv != null && mBatchEditAdapter != null
				&& item_select_Tv != null) {

			all_select_Tv.setText(R.string.all_select);
			mBatchEditAdapter.selectAllItem(false);
			item_select_Tv.setText("已选择0项");
		}
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

	/**
	 * 监听下载删除按钮状态
	 * 
	 * @return void
	 * @see
	 */
	private void responseItemClick() {
		itemSelected = mBatchEditAdapter.getSelectedSongDetailInfo();

		if (itemSelected == null) {
			mEdit_setRing.setActivated(false);
			mEdit_setRing.setEnabled(false);
			mEdit_delete.setEnabled(false);
			mEdit_delete.setActivated(false);
			mEdit_add.setActivated(false);
			mEdit_add.setEnabled(false);
			mEdit_collection.setActivated(false);
			mEdit_collection.setEnabled(false);
		} else {
			int size = itemSelected.size();
			mEdit_setRing.setActivated(size == 1);
			mEdit_setRing.setEnabled(size == 1);
			mEdit_delete.setEnabled(size > 0);
			mEdit_delete.setActivated(size > 0);
			mEdit_add.setActivated(size > 0);
			mEdit_add.setEnabled(size > 0);
			mEdit_collection.setActivated(size > 0);
			mEdit_collection.setEnabled(size > 0);
		}

		// if (itemSelected != null && itemSelected.size() > 0) {
		// if (itemSelected.size() == 1) {
		// mEdit_setRing.setEnabled(true);
		// mEdit_setRing.setActivated(true);
		// } else {
		// mEdit_setRing.setActivated(false);
		// mEdit_setRing.setEnabled(false);
		//
		// }
		// mEdit_delete.setEnabled(true);
		// mEdit_delete.setActivated(true);
		// mEdit_collection.setActivated(true);
		// mEdit_collection.setEnabled(true);
		// mEdit_add.setActivated(true);
		// mEdit_add.setEnabled(true);
		// } else {
		// mEdit_setRing.setEnabled(false);
		// mEdit_setRing.setActivated(false);
		// mEdit_delete.setEnabled(false);
		// mEdit_delete.setActivated(false);
		// mEdit_add.setActivated(false);
		// mEdit_add.setEnabled(false);
		// mEdit_collection.setActivated(false);
		// mEdit_collection.setEnabled(false);
		// }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
	}
}
