package com.prize.music.ui.fragments.list;

import static com.prize.music.Constants.EXTERNAL;
import static com.prize.music.Constants.PLAYLIST_FAVORITES;
import static com.prize.music.Constants.TYPE_PLAYLIST;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.music.Constants;
import com.prize.music.IfragToActivityLister;
import com.prize.music.R;
import com.prize.music.activities.MainActivity;
import com.prize.music.activities.NewListActivity;
import com.prize.music.activities.SearchBrowserActivity;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.ToastUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.base.DragSortListViewAdapter;
import com.prize.music.ui.adapters.list.PlaylistListAdapter;
import com.prize.music.ui.adapters.list.PopupWindowAdapter;
import com.prize.music.ui.fragments.MeFragment;
import com.prize.music.ui.fragments.base.DragSortListViewFragment;

public class PlaylistListFragment extends DragSortListViewFragment {
	// Playlist ID
	private long mPlaylistId = -1;
	private String TAG = "PlaylistListFragment";

	public PlaylistListFragment(Bundle args) {
		setArguments(args);
		mPlaylistId = getArguments().getLong(BaseColumns._ID);
	}

	public PlaylistListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mPlaylistId = getArguments().getLong(BaseColumns._ID);
	}

	@Override
	public void setupFragmentData() {
		View shuffle_temp = View.inflate(getActivity(), R.layout.shuffle_all,
				null);
		mListView.addHeaderView(shuffle_temp);
		RelativeLayout shuffle = (RelativeLayout) shuffle_temp
				.findViewById(R.id.shuffle_wrapper);
		shuffle.setVisibility(View.VISIBLE);
		shuffle.setOnClickListener(new RelativeLayout.OnClickListener() {
			public void onClick(View v) {
				if (sonFlag) {
					return;
				}
				MusicUtils.shuffleAll2(getActivity(), mCursor);
				try {
					MusicUtils.mService
							.setRepeatMode(ApolloService.REPEAT_NONE);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		mAdapter = new PlaylistListAdapter(mainActivity,
				R.layout.listview_items, null, new String[] {}, new int[] {},
				0, mPlaylistId);

		StringBuilder where = new StringBuilder();
		where.append(AudioColumns.IS_MUSIC + "=1").append(
				" AND " + MediaColumns.TITLE + " != ''");
		mWhere = where.toString();
		mSortOrder = MediaColumns.TITLE;
		// mSortOrder = Playlists.Members.PLAY_ORDER;
		if (mPlaylistId == PLAYLIST_FAVORITES) {
			List<Map<String, Object>> mLists = MusicUtils
					.getTableList(getActivity());
			long favorites_id = MusicUtils.getFavoritesId(mainActivity);
			mProjection = new String[] { Playlists.Members._ID,
					Playlists.Members.AUDIO_ID, MediaColumns.TITLE,
					AudioColumns.ALBUM, AudioColumns.ARTIST };
			mUri = Playlists.Members.getContentUri(EXTERNAL, favorites_id);
		} else {
			mProjection = new String[] { Playlists.Members._ID,
					Playlists.Members.AUDIO_ID, MediaColumns.TITLE,
					AudioColumns.ALBUM, AudioColumns.ARTIST };
			mUri = Playlists.Members.getContentUri(EXTERNAL, mPlaylistId);
		}
		mMediaIdColumn = Playlists.Members.AUDIO_ID;
		mType = TYPE_PLAYLIST;
		mFragmentGroupId = 90;
	}

	/**
	 * @param which
	 */
	public boolean removePlaylistItem(int which, String audioIdsstring) {
		mCursor.moveToPosition(which);
		long id = mCursor.getLong(mCursor
				.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID));
		if (mPlaylistId >= 0) {
			Uri uri = Playlists.Members.getContentUri(EXTERNAL, mPlaylistId);
			/*
			 * mainActivity.getContentResolver().delete(uri,
			 * Playlists.Members.AUDIO_ID + "=" + id, null);
			 */
			if (mainActivity != null) {
				mainActivity.getContentResolver().delete(uri,
						Playlists.Members.AUDIO_ID + " in " + audioIdsstring, null);
			}
			return true;
		} else if (mPlaylistId == PLAYLIST_FAVORITES) {
			MusicUtils.removeFromFavorites(mainActivity, id);
		}
		// mListView.invalidateViews();
		return false;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.play_fg_back:
			hideInputMethod();
			mainActivity.getSupportFragmentManager().popBackStack();
			break;

		case R.id.play_add:
			showPopupWindow();
			break;

		case R.id.popu_add:

			Intent intent = new Intent(mainActivity, NewListActivity.class);
			intent.putExtra("new_id", new_id);
			mListData = mAdapter.names;
			intent.putStringArrayListExtra("mListData", mListData);
			mainActivity.startActivity(intent);
			mainActivity.overridePendingTransition(R.anim.in_from_left,
					R.anim.out_to_right);
			dismissMyPopuWindow();
			break;

		case R.id.popu_rename:
			/*
			 * Intent intent2 = new Intent(INTENT_RENAME_PLAYLIST);
			 * intent2.putExtra(INTENT_KEY_RENAME, new_id);
			 * mainActivity.startActivityForResult(intent2, 1000);
			 */
			List<Map<String, Object>> lists = MusicUtils
					.getTableList(getActivity());
			renameTbleDialog(name, lists, new_id);
			dismissMyPopuWindow();
			break;

		case R.id.drag_mEdit_all:
			String stringContent = drag_mEdit_all.getText().toString().trim();
			if (stringContent != null
					&& stringContent.equals(mainActivity
							.getString(R.string.all_select))) {
				mAdapter.setSelectAll(true);
				mAdapter.notifyDataSetChanged();
				drag_mEdit_all.setText(mainActivity
						.getString(R.string.no_select));
				if(mCursor != null && mCursor.getCount() > 0){
					count = mCursor.getCount();
				}
				mIfragToActivity.countNum(count);
			} else if (stringContent != null
					&& stringContent.equals(mainActivity
							.getString(R.string.no_select))) {
				mAdapter.setSelectAll(false);
				mAdapter.notifyDataSetChanged();
				drag_mEdit_all.setText(mainActivity
						.getString(R.string.all_select));
				count = 0;
				mIfragToActivity.countNum(count);
			}

			break;

		case R.id.drag_mEdit_neg:
			reSetView();
			break;

		case R.id.dia_rename_sure:
			rename();
			break;

		case R.id.dia_rename_neg:
			dismissDialog();
			break;

		case R.id.dia_edit_neg:
			dismissDialog();
			break;

		case R.id.dia_edit_sure:
			new AddAsyncTask(Constants.ACTION_DELETE, null, 0).execute();
			dismissDialog();
			break;
		case R.id.play_search:
			Intent searchIntent = new Intent(mainActivity,
					SearchBrowserActivity.class);
			startActivity(searchIntent);
			break;
		case R.id.popu_cancle:
			dismissPopuWindow();
			break;
		default:
			break;
		}
	}

	private void initAddPopu(final List<Map<String, Object>> mLists) {
		// 初始化弹出菜单
		View popupView = LayoutInflater.from(mainActivity).inflate(
				R.layout.popupwindow_add_list, null);

		LinearLayout popu_add_linearlayout = (LinearLayout) popupView
				.findViewById(R.id.popu_add_linearlayout);
		popu_cancle = (TextView) popupView.findViewById(R.id.popu_cancle);
		popu_cancle.setOnClickListener(this);

		window = new PopupWindow(popupView,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
		// 设置菜单背景，不设置背景菜单不会显示
		window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		// 菜单外点击菜单自动消失
		window.setOutsideTouchable(true);
		// 初始化菜单上的按键，并设置监听
		ListView li = (ListView) popupView.findViewById(R.id.popul_list);
		PopupWindowAdapter pAdapter = new PopupWindowAdapter(mLists,
				mainActivity);

		if (pAdapter != null && pAdapter.getCount() > 5) {
			LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) popu_add_linearlayout
					.getLayoutParams();
			linearParams.height = 600;// 当控件的高强制设成600象素
			popu_add_linearlayout.setLayoutParams(linearParams);
		}

		li.setAdapter(pAdapter);
		window.setAnimationStyle(R.style.mypopwindow_anim_style);
		window.showAtLocation(drag_mEdit_all, Gravity.BOTTOM
				| Gravity.CENTER_HORIZONTAL, 0, 0);
		li.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final long new_id = (Long) mLists.get(position).get("id");
				final long[] selectIds = mAdapter.getSelectedAudioIds();
				new AddAsyncTask(Constants.ACTION_ADD, selectIds, new_id)
						.execute();
				dismissPopuWindow();
			}
		});

	}

	// huanglingjun
	public void showPopupWindow() {
		// 初始化弹出菜单
		View popupContent = LayoutInflater.from(mainActivity).inflate(
				R.layout.paly_popu, null);
		myPopupWindow = new PopupWindow(popupContent,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		// 设置菜单背景，不设置背景菜单不会显示
		myPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		// 菜单外点击菜单自动消失
		myPopupWindow.setOutsideTouchable(true);
		// 设置显示在某控件下
		myPopupWindow.showAsDropDown(play_add, 0, 10);
		// 初始化菜单上的按键，并设置监听
		popupContent.findViewById(R.id.popu_add).setOnClickListener(this);
		popupContent.findViewById(R.id.popu_rename).setOnClickListener(this);
	}

	// huanglingjun 重命名后回传值，改变表名
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1000 && resultCode == 1001) {
			String result_value = data.getStringExtra("result");
			play_name.setText(result_value);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		getView().setFocusableInTouchMode(true);
		getView().requestFocus();
		getView().setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (event.getAction() == KeyEvent.ACTION_UP
						&& keyCode == KeyEvent.KEYCODE_BACK) {
					return reSetView();
				}
				if (myPopupWindow != null && myPopupWindow.isShowing()) {
					myPopupWindow.dismiss();
					return true;
				}
				dismissPopuWindow();
				return false;
			}
		});
	}

	// 重命名列表dialog
	public void renameTbleDialog(String orName,
			List<Map<String, Object>> lists, long renameId) {
		this.lists = lists;
		this.renameId = renameId;
		renameDialog = new AlertDialog.Builder(mainActivity).create();
		LayoutInflater inflater = LayoutInflater.from(mainActivity);
		View view = inflater.inflate(R.layout.dialog_rename, null);
		view.setBackgroundResource(R.drawable.icon_dialog);

		Button dia_rename_neg = (Button) view.findViewById(R.id.dia_rename_neg);
		Button dia_rename_sure = (Button) view
				.findViewById(R.id.dia_rename_sure);
		dia_rename_edit = (EditText) view.findViewById(R.id.dia_rename_edit);

		dia_rename_edit.setText(orName);
		dia_rename_edit.setSelection(orName.length());
		dia_rename_edit.selectAll();
		dia_rename_neg.setOnClickListener(this);
		dia_rename_sure.setOnClickListener(this);
		renameDialog.setView(view);
		renameDialog.setCanceledOnTouchOutside(false);
		renameDialog.show();
		dia_rename_edit.requestFocus();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.toggleSoftInput(0,
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 300);
	}

	public void rename() {
		String editReName = dia_rename_edit.getText().toString().trim();
		if (!TextUtils.isEmpty(editReName)) {
			int id = idForplaylist(editReName);
			if (id >= 0) {
				// MusicUtils.clearPlaylist(mainActivity, id);
				Toast.makeText(mainActivity,
						mainActivity.getString(R.string.List_name_repetition),
						Toast.LENGTH_LONG).show();
			} else {
				MusicUtils.renamePlaylist(mainActivity, renameId, editReName);
				lastTableName = editReName;
				play_name.setText(lastTableName);
				name = editReName;
			}
			dismissDialog();
		} else {
			Toast.makeText(mainActivity,
					mainActivity.getString(R.string.name_cannot_be_empty),
					Toast.LENGTH_LONG).show();
		}
	}

	// 判断列表名是否重复
	private int idForplaylist(String name) {

		Cursor cursor = MusicUtils.query(mainActivity,
				Audio.Playlists.EXTERNAL_CONTENT_URI,
				new String[] { Audio.Playlists._ID }, Audio.Playlists.NAME
						+ "=?", new String[] { name }, Audio.Playlists.NAME, 0);
		int id = -1;
		if (cursor != null) {
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				id = cursor.getInt(0);
			}
			cursor.close();
		} else {
			Toast.makeText(mainActivity,
					mainActivity.getString(R.string.List_name_repetition),
					Toast.LENGTH_LONG).show();
		}

		return id;
	}

	// 删除列表dialog
	public void deleteListDialog(int deleteCount) {
		deleteDialog = new AlertDialog.Builder(mainActivity).create();
		LayoutInflater inflater = LayoutInflater.from(mainActivity);
		View view = inflater.inflate(R.layout.dialog_edit, null);
		view.setBackgroundResource(R.drawable.icon_dialog);

		Button dia_edit_neg = (Button) view.findViewById(R.id.dia_edit_neg);
		Button dia_edit_sure = (Button) view.findViewById(R.id.dia_edit_sure);
		TextView dia_edit_content = (TextView) view
				.findViewById(R.id.dia_edit_content);

		dia_edit_content.setText(mainActivity
				.getString(R.string.delete_from_list)
				+ deleteCount
				+ mainActivity.getString(R.string.songs));
		dia_edit_neg.setOnClickListener(this);
		dia_edit_sure.setOnClickListener(this);
		deleteDialog.setView(view);
		deleteDialog.show();
	}

	@Override
	public void countNum(int count) {
		// TODO Auto-generated method stub

	}

	// maiActivity回调的方法
	@Override
	public void processAction(String action) {
		LogUtils.i("long", "列表响应action=" + action);
		if (action == null) {
			return;
		}
		List<Map<String, Object>> mLists = MusicUtils
				.getTableList(getActivity());
		// 添加
		if (action.equals(Constants.ACTION_ADD)) {
			if (mLists != null && mLists.size() > 0) {
				for (int i = 0; i < mLists.size(); i++) {
					if (mLists
							.get(i)
							.get("name")
							.equals(mainActivity
									.getString(R.string.create_list))) {
						mLists.remove(i);
					}
					// 去除本列表
					if (name.equals(mLists.get(i).get("name"))) {
						mLists.remove(i);
					}
				}
			}

			LogUtils.i(TAG, "mLists.size()=" + mLists.size());
			if (mLists.size() > 0) {
				initAddPopu(mLists);
			} else {
				ToastUtils.showOnceToast(mainActivity,
						mainActivity.getString(R.string.no_other_list));
			}
		}
		// 设为铃声
		if (action.equals(Constants.ACTION_BELL)) {
			if(mCursor ==  null || mCursor.getCount() <= 0){
				return;
			}
			for (int i = 0; i < mCursor.getCount(); i++) {
				if (DragSortListViewAdapter.isSelected.get(i)) {

					mCursor.moveToPosition(i);
					mSelectedId = mCursor.getLong(mCursor
							.getColumnIndexOrThrow(mMediaIdColumn));

					MusicUtils.setRingtone(mainActivity, mSelectedId);
					reSetView();
				}
			}
		}
		// 收藏
		if (action.equals(Constants.ACTION_SORT)) {
			if (mCursor != null && mCursor.getCount() > 0 
					&& !name.equals(mainActivity.getString(R.string.my_love))) {
				long[] ids = new long[mCursor.getCount()];
				long new_id = MusicUtils.getFavoritesId(mainActivity);
				long[] threadIds = mAdapter.getSelectedAudioIds();
				new AddAsyncTask(action, threadIds, new_id).execute();
			} else {
				ToastUtils.showOnceToast(mainActivity,
						mainActivity.getString(R.string.song_has_bean));
			}
		}
		// 删除
		int deleteCount = 0;
		if (action.equals(Constants.ACTION_DELETE)) {
			for (int i = 0; i < mCursor.getCount(); i++) {
				if (DragSortListViewAdapter.isSelected.get(i)) {
					deleteCount++;
				}
			}
			deleteListDialog(deleteCount);
		}

	}

	@Override
	public void refresh() {
		if (mListView != null) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putLong(BaseColumns._ID, mPlaylistId);
		outState.putString("name", name);
	}

	public void dismissDialog() {
		if (deleteDialog != null && deleteDialog.isShowing()) {
			deleteDialog.dismiss();
		}

		if (renameDialog != null && renameDialog.isShowing()) {
			InputMethodManager inputmanger = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(
					dia_rename_edit.getWindowToken(), 0);
			renameDialog.dismiss();
		}
	}

	public void dismissPopuWindow() {
		if (window != null && window.isShowing()) {
			window.dismiss();
		}
	}

	public void dismissMyPopuWindow() {
		if (myPopupWindow != null && myPopupWindow.isShowing()) {
			myPopupWindow.dismiss();
		}
	}

	class AddAsyncTask extends AsyncTask<Void, Void, Boolean> {
		String action = null;
		long[] ids = null;
		long new_id = 0;
		ProgressDialog dialog = null;

		public AddAsyncTask(String action, long[] ids, long new_id) {
			this.action = action;
			this.ids = ids;
			this.new_id = new_id;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = new ProgressDialog(mainActivity,
					ProgressDialog.THEME_HOLO_LIGHT);
			if (action.equals(Constants.ACTION_ADD)) {
				dialog.setMessage(mainActivity.getString(R.string.adding));
				if (mAdapter.getSelectedAudioIds().length > 50) {
					dialog.show();
				}
			} else if (action.equals(Constants.ACTION_SORT)) {
				dialog.setMessage(mainActivity
						.getString(R.string.collectioning));
				if (mAdapter.getSelectedAudioIds().length > 50) {
					dialog.show();
				}
			} else if (action.equals(Constants.ACTION_DELETE)) {
				dialog.setMessage(mainActivity.getString(R.string.deleteing));
				if (mAdapter.getSelectedAudioIds().length > 100) {
					dialog.show();
				}
			}
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean isExisted = false;
			// 添加
			if (action.equals(Constants.ACTION_ADD)
					|| action.equals(Constants.ACTION_SORT)) {
				isExisted = MusicUtils.addTrackToPlaylist(mainActivity, new_id,
						ids);
				if (dialog.isShowing()) {
					SystemClock.sleep(500);
				}
				return isExisted;
			}

			if (action.equals(Constants.ACTION_DELETE)) {
				long[] audioIds = mAdapter.getSelectedAudioIds();
				StringBuffer audioIdsstring = new StringBuffer("(");
				for (int i = 0; i < audioIds.length; i++) {
					audioIdsstring.append(audioIds[i] + ",");
				}
				audioIdsstring.setCharAt(audioIdsstring.length() - 1, ')');
				isExisted = removePlaylistItem(0, audioIdsstring.toString());
				if (dialog.isShowing()) {
					SystemClock.sleep(500);
				}
				//prize-public-bug:14628 clear selected num -pengcancan-20160426-start
				if (isExisted) {
					count = 0;
				}
				//prize-public-bug:14628 clear selected num -pengcancan-20160426-end
				return isExisted;
			}
			return false;
		}

		@Override
		// 处理界面
		protected void onPostExecute(Boolean result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			if (action.equals(Constants.ACTION_ADD)) {
				if (result) {
					ToastUtils.showOnceToast(mainActivity,
							mainActivity.getString(R.string.Song_has_been));
				} else {
					ToastUtils.showOnceToast(mainActivity,
							mainActivity.getString(R.string.addSuccessful));
				}
				reSetView();
			} else if (action.equals(Constants.ACTION_SORT)) {
				if (result) {
					ToastUtils.showOnceToast(mainActivity,
							mainActivity.getString(R.string.Song_has_been));
				} else {
					ToastUtils.showOnceToast(mainActivity, mainActivity
							.getString(R.string.collectionSuccessful));
				}
				reSetView();
			} else if (action.equals(Constants.ACTION_DELETE)) {
				if (result) {
					ToastUtils.showOnceToast(mainActivity,
							mainActivity.getString(R.string.deleteSuccessful));
				} else {
					ToastUtils.showOnceToast(mainActivity,
							mainActivity.getString(R.string.deleteFail));
				}
				// refresh();
			}
		}
	}

	@Override
	public boolean removePlaylistItem(int which) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onAttach(Activity activity) {

		try {
			mIfragToActivity = (IfragToActivityLister) activity;
			mainActivity = (MainActivity) getActivity();
		} catch (Exception e) {
			throw new ClassCastException(activity.toString()
					+ "must implement  IfragToActivity");
		}
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		if (mIfragToActivity != null) {
			mIfragToActivity = null;
		}
		super.onDetach();
	}//

	/**
	 * Hides the input method.
	 */
	protected void hideInputMethod() {
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && imm.isActive()) {
			imm.hideSoftInputFromWindow(play_fg_back.getWindowToken(), 0);
		}
	}
}
