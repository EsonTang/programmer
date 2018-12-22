package com.prize.music.ui.fragments.base;

import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.prize.music.Constants;
import com.prize.music.IfragToActivityLister;
import com.prize.music.R;
import com.prize.music.helpers.RefreshableFragment;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.history.HistoryDao;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.base.EditListViewAdapter;
import com.prize.music.ui.adapters.base.ListViewAdapter;
import com.prize.music.ui.adapters.list.EditSonglistAdapter;
import com.prize.music.ui.fragments.MusicLibraryFragment;

import static com.prize.music.Constants.INTENT_ADD_TO_PLAYLIST;
import static com.prize.music.Constants.INTENT_PLAYLIST_LIST;

/**
 * 全部音乐歌曲列表专用
 * 
 * @author Administrator
 *
 */
public abstract class EditSongsListViewFragment extends RefreshableFragment
		implements LoaderCallbacks<Cursor>, OnItemClickListener,
		OnTouchListener, OnItemLongClickListener, IfragToActivityLister {
	private String TAG = EditSongsListViewFragment.class.getSimpleName();
	// Adapter
	protected EditSonglistAdapter mAdapter;
	private IfragToActivityLister mIfragToActivity;
	// ListView
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

	protected int mFragmentGroupId = 0;

	protected String mCurrentId, mSortOrder = null, mWhere = null,
			mType = null, mTitleColumn = null;

	protected String[] mProjection = null;

	protected Uri mUri = null;
	private String title;

	//
	// private TextView action_back;
	//
	// private TextView search_Tv;
	// protected ViewPager viewPager;

	// Bundle
	public EditSongsListViewFragment() {
	}

	public EditSongsListViewFragment(Bundle args) {
		setArguments(args);
	}

	/*
	 * To be overrode in child classes to setup fragment data
	 */
	public abstract void setupFragmentData();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupFragmentData();
		mListView.setOnItemClickListener(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void refresh() {
		// The data need to be refreshed
		if (mListView != null) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	// private OnClickListener listener = new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// int key = v.getId();
	// switch (key) {
	// case R.id.action_back:
	// getActivity().getSupportFragmentManager().popBackStack();
	// break;
	// case R.id.action_search:
	// getActivity().onSearchRequested();
	// break;
	//
	// default:
	// break;
	// }
	//
	// }
	// };

	public void onViewCreated(View view, Bundle savedInstanceState) {

		view.setOnTouchListener(this);
		super.onViewCreated(view, savedInstanceState);
	};

	/**
	 * 解决fragment事件穿透问题
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View root = inflater
				.inflate(R.layout.listview_layout, container, false);
		mListView = (ListView) root.findViewById(android.R.id.list);
		if (getArguments() != null) {
			title = getArguments().getString("flag");
			if (!TextUtils.isEmpty(title)) {
				// action_back.setText(title);
			}

		}
		setListener();

		return root;
	}

	private void setListener() {

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), mUri, mProjection, mWhere, null,
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
		mAdapter.changeCursor(data);
		mListView.invalidateViews();
		mCursor = data;

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (mAdapter != null)
			mAdapter.changeCursor(null);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putAll(getArguments() != null ? getArguments() : new Bundle());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		// if (!isSelectMode) {
		MusicUtils.playAll(getActivity(), mCursor, position - 1);
		// } else {
		// mAdapter.toggleCheckedState(position - 1);
		// mIfragToActivity.countNum(mAdapter.getSelectedAudioIds().length);
		// }
	}

	/**
	 * p 更新数据在需要的时候
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
		getActivity().registerReceiver(mMediaStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		getActivity().unregisterReceiver(mMediaStatusReceiver);
		super.onStop();
	}

	@Override
	public void onAttach(Activity activity) {

		try {
			mIfragToActivity = (IfragToActivityLister) activity;
		} catch (Exception e) {
			throw new ClassCastException(activity.toString()
					+ "must implement  IfragToActivity");
		}
		super.onAttach(activity);
	}

	@Override
	public void countNum(int count) {

	}

	@Override
	public void processAction(String action) {
		long[] selectIds = mAdapter.getSelectedAudioIds();
		if (Constants.ACTION_BELL == action) {
			MusicUtils.setRingtone(getActivity(),
					mAdapter.getSelectedAudioIds()[0]);

		} else if (Constants.ACTION_SORT.equals(action)) {
			MusicUtils.addToaddToFavorites(getActivity(), selectIds);
		} else if (Constants.ACTION_DELETE.equals(action)) {
			int len = selectIds.length;
			for (int i = 0; i < len; i++) {
				HistoryDao.getInstance(getActivity()).deleteByAudioId(
						selectIds[i]);
			}
			refresh();
		}
		updateViews();
		// 通知取消刷MainActivity新界面
		mIfragToActivity.processAction(Constants.ACTION_CANCE);

	}

	private boolean isSelectMode = false;

	private void updateViews() {
		isSelectMode = false;
		mAdapter.setSelectMode(false);
		mAdapter.notifyDataSetChanged();
		mAdapter.selectAllItem(false);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		return false;
	}

}
