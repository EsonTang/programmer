/**
 * 
 */

package com.prize.music.ui.fragments.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.music.IfragToActivityLister;
import com.prize.music.R;
import com.prize.music.activities.LocalSongActivity;
import com.prize.music.activities.MainActivity;
import com.prize.music.activities.NewListActivity;
import com.prize.music.helpers.RefreshableFragment;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.base.DragSortListViewAdapter;
import com.prize.music.ui.adapters.list.MyListViewAdapter;
import com.prize.music.ui.fragments.MeFragment;

public abstract class DragSortListViewFragment extends RefreshableFragment
		implements LoaderCallbacks<Cursor>, OnItemClickListener,
		IfragToActivityLister, OnClickListener, OnTouchListener {
	// Adapter
	protected DragSortListViewAdapter mAdapter;

	// ListView
	// protected DragSortListView mListView;
	protected ListView mListView;
	// Cursor
	protected Cursor mCursor;

	// Selected position
	protected int mSelectedPosition;

	// Used to set ringtone
	protected long mSelectedId;

	// Options
	protected final int PLAY_SELECTION = 0;

	protected final int USE_AS_RINGTONE = 1;

	protected final int ADD_TO_PLAYLIST = 2;

	protected final int SEARCH = 3;

	protected final int REMOVE = 4;

	protected int mFragmentGroupId = 0;

	protected String mSortOrder = null, mWhere = null, mType = null,
			mMediaIdColumn = null;

	protected String[] mProjection = null;

	protected Uri mUri = null;
	// huanglingjun
	private RelativeLayout none_data;

	private TextView drog_new_songs;

	protected TextView play_name;

	protected TextView play_add, play_search;

	protected TextView play_fg_back;

//	protected PopupWindow myPopupWindow;
	protected PopupMenu popupMenu;
	protected Long new_id;

	protected ArrayList<String> mListData;
	// 添加，删除，收藏，铃声
	protected TextView main_mEdit_add, main_mEdit_bell, main_mEdit_collection,
			main_mEdit_delete;
	// 添加和铃声img
	protected ImageView main_img_add, main_img_bell;

	protected MyListViewAdapter myAdapter;
	protected List<Map<String, Object>> mData;

	protected IfragToActivityLister mIfragToActivity;

	protected Boolean sonFlag = false;
	// 头布局
	protected RelativeLayout drag_head_layout_edit, drag_head_layout_normaly;
	// 编辑页面全部，取消按钮
	protected TextView drag_mEdit_all, drag_mEdit_neg;
	protected TextView popu_cancle;
	protected View bottomactionbar_new, main_bottom_layout;

	protected long[] mList = new long[] {};
	// 表名
	protected String name;
	// 添加到其他列表
	protected PopupWindow window;

	protected int count = 0;

	protected AlertDialog renameDialog;
	protected AlertDialog deleteDialog;
	protected EditText dia_rename_edit;
	protected long renameId;

	public String lastTableName = null;
	protected List<Map<String, Object>> lists;

	protected MainActivity mainActivity;

	// Bundle
	public DragSortListViewFragment() {
	}

	public DragSortListViewFragment(Bundle args) {
		setArguments(args);
	}

	/*
	 * To be overrode in child classes to setup fragment data
	 */
	public abstract void setupFragmentData();

	/*
	 * To be overrode in child classes to remove item from list
	 */
	public abstract boolean removePlaylistItem(int which);

	/**
	 * 解决事件穿透问题
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		view.setOnTouchListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupFragmentData();
		mListView.setOnCreateContextMenuListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setAdapter(mAdapter);
		// DragSortController controller = new DragSortController(mListView);
		// controller.setDragHandleId(R.id.listview_drag_handle);
		// controller.setRemoveEnabled(true);
		// controller.setRemoveMode(1);
		// mListView.setFloatViewManager(controller);
		// mListView.setOnTouchListener(controller);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void refresh() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.dragsort_listview, container,
				false);

		mListView = (ListView) root.findViewById(android.R.id.list);
		initData(root);
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				bottomactionbar_new.setVisibility(View.GONE);
				main_bottom_layout.setVisibility(View.VISIBLE);
				drag_head_layout_edit.setVisibility(View.VISIBLE);
				drag_head_layout_normaly.setVisibility(View.GONE);
				mAdapter.setMode(true);
				mAdapter.notifyDataSetChanged();
				sonFlag = true;
				return false;
			}
		});
		return root;
	}

	public void initData(View root) {
		// mListData = new ArrayList<String>();
		mData = new ArrayList<Map<String, Object>>();
		play_name = (TextView) root.findViewById(R.id.play_name);
		name = getArguments().getString("name");
		if (name != null) {
			play_name.setText(name);
		} else {
			play_name.setText("新建列表");
		}

		none_data = (RelativeLayout) root.findViewById(R.id.none_data);
		drog_new_songs = (TextView) root.findViewById(R.id.drog_new_songs);
		play_add = (TextView) root.findViewById(R.id.play_add);
		play_fg_back = (TextView) root.findViewById(R.id.play_fg_back);
		play_search = (TextView) root.findViewById(R.id.play_search);
		drag_head_layout_edit = (RelativeLayout) root
				.findViewById(R.id.drag_head_layout_edit);
		drag_head_layout_normaly = (RelativeLayout) root
				.findViewById(R.id.drag_head_layout_normaly);
		drag_mEdit_all = (TextView) root.findViewById(R.id.drag_mEdit_all);
		drag_mEdit_neg = (TextView) root.findViewById(R.id.drag_mEdit_neg);

		// 找到mainactivity 里面的四个编辑控件
		main_bottom_layout = mainActivity.findViewById(R.id.main_bottom_layout);
		bottomactionbar_new = mainActivity
				.findViewById(R.id.bottomactionbar_new);

		play_fg_back.setOnClickListener(this);
		play_add.setOnClickListener(this);
		play_search.setOnClickListener(this);
		drag_mEdit_all.setOnClickListener(this);
		drag_mEdit_neg.setOnClickListener(this);

		new_id = getArguments().getLong("new_id");
		drog_new_songs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mainActivity, NewListActivity.class);
				intent.putExtra("new_id", new_id);
				intent.putExtra(LocalSongActivity.TABLE_NAME, name);
				mListData = mAdapter.names;
				intent.putStringArrayListExtra("mListData", mListData);
				mainActivity.startActivity(intent);
				mainActivity.overridePendingTransition(R.anim.in_from_left,
						R.anim.out_to_right);
			}
		});

		if (name.equals(getResources().getString(R.string.my_love))) {
			// main_mEdit_collection.setEnabled(false);
			play_add.setVisibility(View.GONE);
			play_search.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(mainActivity, mUri, mProjection, mWhere, null,
				mSortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Check for database errors
		if (data == null) {
			return;
		}
		if (mCursor != null)
			mCursor.close();
		// mAdapter.reset();
		// mAdapter.isSelected.
		mAdapter.changeCursor(data);
		mListView.invalidateViews();
		mCursor = data;
		if (data.getCount() <= 0) {
			none_data.setVisibility(View.VISIBLE);
			bottomactionbar_new.setVisibility(View.VISIBLE);
			main_bottom_layout.setVisibility(View.GONE);
			drag_head_layout_edit.setVisibility(View.GONE);
			drag_head_layout_normaly.setVisibility(View.VISIBLE);
			mAdapter.setMode(false);
		} else {
			none_data.setVisibility(View.GONE);
		}
		initListData(data);
		sonFlag = false;
		reSetView();
	}

	private void initListData(Cursor cursor) {
		// mListData.clear();
		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			Long base_id = cursor.getLong(cursor
					.getColumnIndexOrThrow(BaseColumns._ID));

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("base_id", base_id);
			mData.add(map);
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		/*
		 * if (mAdapter != null) mAdapter.changeCursor(null);
		 */
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putAll(getArguments() != null ? getArguments() : new Bundle());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		if (sonFlag) {
			// ViewHolder vHollder = (ViewHolder) v.getTag();
			// 在每次获取点击的item时将对于的checkbox状态改变，同时修改map的值。
			// vHollder.item_checkBox.toggle();
			CheckBox box = (CheckBox) v.findViewById(R.id.checkBox);
			box.toggle();
			DragSortListViewAdapter.isSelected.put(position - 1,
					box.isChecked());
			if (box.isChecked()) {
				count++;
			} else {
				count--;
			}
			if (count == mListView.getCount()-1) {
				drag_mEdit_all.setText(mainActivity.getString(R.string.no_select));
			} else {
				drag_mEdit_all.setText(mainActivity.getString(R.string.all_select));	
			}
			mIfragToActivity.countNum(count);
		} else {
			MusicUtils.playAll(mainActivity, mCursor, position - 1);
		}

	}

	/**
	 * Update the list as needed
	 */
	private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mListView != null) {
				mAdapter.notifyDataSetChanged();
			}
		}
	};

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		filter.addAction(ApolloService.PLAYSTATE_CHANGED);
		mainActivity.registerReceiver(mMediaStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		mainActivity.unregisterReceiver(mMediaStatusReceiver);
		super.onStop();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	// 编辑完重置页面
	public boolean reSetView() {
		if (drag_head_layout_edit.getVisibility() == View.VISIBLE) {
			drag_head_layout_edit.setVisibility(View.GONE);
			drag_head_layout_normaly.setVisibility(View.VISIBLE);
			bottomactionbar_new.setVisibility(View.VISIBLE);
			main_bottom_layout.setVisibility(View.GONE);
			mAdapter.setMode(false);
			mAdapter.setSelectAll(false);
			count = 0;
			sonFlag = false;
			mIfragToActivity.countNum(count);
			mAdapter.notifyDataSetChanged();
			return true;
		}
		return false;
	}

}
