/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：正在播放的歌曲列表信息父类
 *当前版本：V1.0
 *作  者：longbaoxiu
 *完成日期：2015-7-21
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ********************************************/
package com.prize.music.ui.fragments.base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.prize.music.IfragToActivityLister;
import com.prize.music.R;
import com.prize.music.helpers.RefreshableFragment;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.base.ListViewAdapter;

/**
 **
 * 正在播放的歌曲列表信息父类
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public abstract class PlayingListViewFragment extends RefreshableFragment
		implements LoaderCallbacks<Cursor>, OnItemClickListener,
		OnTouchListener {
	private String TAG = "PlayingListViewFragment";
	// Adapter
	protected ListViewAdapter mAdapter;
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

	public PlayingListViewFragment() {
	}

	public PlayingListViewFragment(Bundle args) {
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
		// mListView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void refresh() {
		// The data need to be refreshed
		if (mListView != null) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

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

		View root = inflater.inflate(R.layout.listview, container, false);
		mListView = (ListView) root.findViewById(android.R.id.list);
		// 播放界面效果
		mListView.setPadding(mListView.getPaddingLeft(), (int) getResources()
				.getDimension(R.dimen.list_padding_top), mListView
				.getPaddingRight(), mListView.getPaddingBottom());
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
	public void onItemClick(AdapterView<?> parent, View v, final int position,
			long id) {
		mAdapter.play(v);
		parent.post(new Runnable() {

			@Override
			public void run() {
				MusicUtils.playAll(getActivity(), mCursor, position);

			}
		});
	}

	/**
	 * 更新数据在需要的时候
	 */
	private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mListView != null) {
				if (mCursor != null && !mCursor.isClosed()) {
					mAdapter.notifyDataSetChanged();

				}
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

}
