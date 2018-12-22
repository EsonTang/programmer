/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：长按条目跳转的编辑activity
 *当前版本：V1.0
 *作  者：huanglingjun
 *完成日期：2015-7-21
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ********************************************/
package com.prize.music.activities;

import static com.prize.music.Constants.INTENT_PLAYLIST_LIST;
import static com.prize.music.Constants.TYPE_SONG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prize.music.R;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.ui.adapters.list.NewListAdapter;
import com.prize.music.ui.adapters.list.NewListAdapter.NewViewHolder;

/**
 * 类描述：长按条目跳转的编辑activity
 * 
 * @author huanglingjun
 * @version v1.0
 */
public class MyEditListActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>, OnClickListener {
	// Adapter
	protected NewListAdapter mAdapter;

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
	// 数据源
	private List<Map<String, Object>> mData;
	// 全部
	private TextView mEdit_all;
	// 取消
	private TextView mEdit_neg;
	// 添加，删除，铃声，收藏
	private LinearLayout mEdit_add, mEdit_delete, mEdit_bell, mEdit_collection;

	private long[] mList = new long[] {};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		StateBarUtils.initStateBar(this);
		setContentView(R.layout.activity_my_edit_list);

		mListView = (ListView) findViewById(R.id.mEdit_list);
		mEdit_all = (TextView) findViewById(R.id.mEdit_all);
		mEdit_neg = (TextView) findViewById(R.id.mEdit_neg);
		mEdit_add = (LinearLayout) findViewById(R.id.mEdit_add);
		mEdit_delete = (LinearLayout) findViewById(R.id.mEdit_delete);
		mEdit_bell = (LinearLayout) findViewById(R.id.mEdit_bell);
		mEdit_collection = (LinearLayout) findViewById(R.id.mEdit_collection);

		mEdit_all.setOnClickListener(this);
		mEdit_neg.setOnClickListener(this);
		mEdit_add.setOnClickListener(this);
		mEdit_delete.setOnClickListener(this);
		mEdit_bell.setOnClickListener(this);
		mEdit_collection.setOnClickListener(this);

		mData = new ArrayList<Map<String, Object>>();
		initData();

		mListView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				return false;
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				NewViewHolder vHollder = (NewViewHolder) view.getTag();
				// 在每次获取点击的item时将对于的checkbox状态改变，同时修改map的值。
				vHollder.cb.toggle();
				NewListAdapter.isSelected.put(position, vHollder.cb.isChecked());

			}
		});
		getSupportLoaderManager().initLoader(0, null, MyEditListActivity.this);
	}

	/**
	 * @Description:
	 * @param:
	 * @return: void
	 * @see
	 */
	private void initData() {
		StringBuilder where = new StringBuilder();
		where.append(AudioColumns.IS_MUSIC + "=1").append(
				" AND " + MediaColumns.TITLE + " != ''");
		mWhere = where.toString();
		mSortOrder = Audio.Media.DEFAULT_SORT_ORDER;
		mUri = Audio.Media.EXTERNAL_CONTENT_URI;
		mFragmentGroupId = 3;
		mType = TYPE_SONG;
		mTitleColumn = MediaColumns.TITLE;

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, mUri, mProjection, mWhere, null,
				mSortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data == null) {
			return;
		}
		if (mCursor != null)
			mCursor.close();

		// mAdapter.changeCursor(data);
		mListView.invalidateViews();
		initListData(data);
		mListView.setAdapter(mAdapter);
		mCursor = data;

	}

	private void initListData(Cursor cursor) {
		while (cursor.moveToNext()) {
			String title = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaColumns.TITLE));

			String artist = cursor.getString(cursor
					.getColumnIndexOrThrow(AudioColumns.ARTIST));

			Long base_id = cursor.getLong(cursor
					.getColumnIndexOrThrow(BaseColumns._ID));

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", title);
			map.put("artist", artist);
			map.put("base_id", base_id);
			mData.add(map);
		}

		mAdapter = new NewListAdapter(this, mData, false);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// outState.putAll(getArguments() != null ? getArguments() : new
		// Bundle());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.mEdit_all:
			for (int i = 0; i < mListView.getCount(); i++) {
				mAdapter = new NewListAdapter(this, mData, true);
				mListView.setAdapter(mAdapter);

			}
			break;

		case R.id.mEdit_neg:

			finish();
			break;

		case R.id.mEdit_add:
			break;
		case R.id.mEdit_delete:
			for (int i = 0; i < mData.size(); i++) {
				if (NewListAdapter.isSelected.get(i)) {

					/*
					 * mCursor.moveToPosition(i); mCurrentId =
					 * mCursor.getString(mCursor //获取当前歌曲的id
					 * .getColumnIndexOrThrow(BaseColumns._ID));
					 */
					// mSelectedId = Long.parseLong(mCurrentId);

					mSelectedId = (Long) mData.get(i).get("base_id");
					mList = getIntent().getLongArrayExtra(INTENT_PLAYLIST_LIST);
					mList = new long[] { mSelectedId };
					/*
					 * MusicUtils.addToPlaylist(this, mList, //添加到播放列表 new_id);
					 */
				}
			}
			break;
		case R.id.mEdit_bell:
			for (int i = 0; i < mData.size(); i++) {
				if (NewListAdapter.isSelected.get(i)) {

					mSelectedId = (Long) mData.get(i).get("base_id");
					mList = getIntent().getLongArrayExtra(INTENT_PLAYLIST_LIST);
					mList = new long[] { mSelectedId };
					MusicUtils
							.setRingtone(MyEditListActivity.this, mSelectedId);
				}
			}
			break;
		case R.id.mEdit_collection:
			for (int i = 0; i < mData.size(); i++) {
				if (NewListAdapter.isSelected.get(i)) {

					mSelectedId = (Long) mData.get(i).get("base_id");
					mList = getIntent().getLongArrayExtra(INTENT_PLAYLIST_LIST);
					mList = new long[] { mSelectedId };
					long new_id = MusicUtils
							.getFavoritesId(getApplicationContext());
					MusicUtils.addToPlaylist(this, mList, // 添加到播放列表
							new_id);
				}
			}
			break;

		default:
			break;
		}

	}

}
