/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：列表添加歌曲acitivity
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.prize.app.constants.Constants;
import com.prize.app.util.ToastUtils;
import com.prize.music.R;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.MusicInfo;
import com.prize.music.database.SQLUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.MusicUtils.AddCollectCallBack;
import com.prize.music.ui.adapters.list.NewListAdapter;
import com.prize.music.ui.adapters.list.NewListAdapter.NewViewHolder;

/**
 * 类描述： 列表添加歌曲acitivity
 * 
 * @author huanglingjun
 * @version v1.0
 */
public class NewListActivity extends FragmentActivity implements
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

	private List<Map<String, Object>> mData;
	private List<MusicInfo> mArrayList;
	private List<MusicInfo> mSelectedArrayList;

	private TextView add_fg_all;
	private Button add_sure, add_neg;

	private Long new_id;
	private String table_name;

	private ImageView add_fg_back;
	private SQLUtils sqlite;
	private long[] mList = new long[] {};

	protected Handler mHandler;
	private int selectCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		StateBarUtils.initStateBar(this);
		setContentView(R.layout.activity_new_list);
		StateBarUtils.changeStatus(getWindow());

		mListView = (ListView) findViewById(android.R.id.list);
		add_fg_all = (TextView) findViewById(R.id.add_fg_all);
		add_sure = (Button) findViewById(R.id.add_sure);
		add_neg = (Button) findViewById(R.id.add_neg);
		add_fg_back = (ImageView) findViewById(R.id.add_fg_back);

		add_fg_all.setOnClickListener(this);
		add_sure.setOnClickListener(this);
		add_neg.setOnClickListener(this);
		add_fg_back.setOnClickListener(this);

		/*
		 * if(getIntent().getStringExtra("tableName") != null){ tableName =
		 * getIntent().getStringExtra("tableName"); }
		 */

		new_id = getIntent().getLongExtra("new_id", -1);
		table_name = (String) getIntent()
				.getExtra(LocalSongActivity.TABLE_NAME);

		mData = new ArrayList<Map<String, Object>>();
		mArrayList = new ArrayList<MusicInfo>();
		mSelectedArrayList = new ArrayList<MusicInfo>();
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
				if (vHollder.cb.isChecked()) {
					selectCount++;
				} else {
					selectCount--;
				}
				countNum(selectCount);
				
				if (selectCount == mListView.getCount()) {
					add_fg_all.setText(NewListActivity.this
							.getString(R.string.no_select));
				} else {
					add_fg_all.setText(NewListActivity.this
							.getString(R.string.all_select));
				}
			}
		});

		sqlite = SQLUtils.getInstance(this);
		mHandler = new Handler();
		getSupportLoaderManager().initLoader(0, null, NewListActivity.this);
	}
	
	private void countNum(int num){
		if(num == 0){
			add_sure.setEnabled(false);
		}else{
			add_sure.setEnabled(true);
		}
	}

	private void initData() {
		StringBuilder where = new StringBuilder();
		where.append(AudioColumns.IS_MUSIC + "=1").append(
				" AND " + MediaColumns.TITLE + " != ''");
		mWhere = where.toString();
		mSortOrder = Audio.Media.DEFAULT_SORT_ORDER;
		mUri = Audio.Media.EXTERNAL_CONTENT_URI;
		mFragmentGroupId = 3;
		mType = Constants.TYPE_SONG;
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
		if(mAdapter!=null){
			mAdapter.clearData();
		}
		initListData(data);
		mListView.setAdapter(mAdapter);
		mCursor = data;
		countNum(0);
	}

	private void initListData(Cursor cursor) {
		while (cursor.moveToNext()) {
			String title = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaColumns.TITLE));

			String artist = cursor.getString(cursor
					.getColumnIndexOrThrow(AudioColumns.ARTIST));

			Long base_id = cursor.getLong(cursor
					.getColumnIndexOrThrow(BaseColumns._ID));

			String source_type = DatabaseConstant.LOCAL_TYPE;
			
			String user_id = MusicUtils.getUserId();
			
			// 增加查询字段，未添加到列表，显示第一首歌曲准备
			String albumName=cursor.getString(cursor
						.getColumnIndexOrThrow(AudioColumns.ALBUM));
			String albumId=cursor.getString(cursor
					.getColumnIndexOrThrow(AudioColumns.ALBUM_ID));
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", title);
			map.put("artist", artist);
			map.put("base_id", base_id);
			mData.add(map);
			MusicInfo music_info = new MusicInfo(title, artist, base_id,
					user_id, source_type,albumName,albumId);
			mArrayList.add(music_info);
		}
		/*
		 * //去除已经添加的歌曲 ArrayList<String>
		 * mListData=getIntent().getStringArrayListExtra("mListData");
		 * if(mListData != null){ if(mListData.size()>0 && mData.size()>0){
		 * for(int i= 0 ;i<mData.size();i++){ for(int
		 * j=0;j<mListData.size();j++){
		 * if(mData.get(i).get("title").equals(mListData.get(j))){
		 * mData.remove(i); } } } } }
		 */
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
		case R.id.add_fg_all:
			String stringContent = add_fg_all.getText().toString().trim();
			if (stringContent != null
					&& stringContent.equals(NewListActivity.this
							.getString(R.string.all_select))) {
				mAdapter.init(true);
				mAdapter.notifyDataSetChanged();
				add_fg_all.setText(NewListActivity.this
						.getString(R.string.no_select));
				selectCount = mAdapter.getCount();
			} else if (stringContent != null
					&& stringContent.equals(NewListActivity.this
							.getString(R.string.no_select))) {
				mAdapter.init(false);
				mAdapter.notifyDataSetChanged();
				add_fg_all.setText(NewListActivity.this
						.getString(R.string.all_select));
				selectCount = 0;
			}
			countNum(selectCount);
			break;

		case R.id.add_sure:
			mList = new long[NewListAdapter.isSelected.size()];
			if (mList.length <= 0) {
				finish();
				return;
			}
			new AddAsyncTask().execute();

			break;

		case R.id.add_neg:
			finish();
			break;
		case R.id.add_fg_back:
			finish();
			break;

		default:
			break;
		}

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	class AddAsyncTask extends AsyncTask<Void, Void, Boolean> {
		int count = 0;
		ProgressDialog dialog = null;

		public AddAsyncTask() {
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = new ProgressDialog(NewListActivity.this,
					ProgressDialog.THEME_HOLO_LIGHT);
			dialog.setMessage(NewListActivity.this.getString(R.string.adding));
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(true);

			for (int i = 0; i < mData.size(); i++) {
				if (NewListAdapter.isSelected.get(i)) {
					mSelectedId = (Long) mData.get(i).get("base_id");
					mList[count] = mSelectedId;
					count++;
					mSelectedArrayList.add(mArrayList.get(i));
				}
			}
			if (count > 50) {
				dialog.show();
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean isExisted = false;
			if (count != 0) {
				long[] playIds = new long[count];
				for (int i = 0; i < playIds.length; i++) {
					playIds[i] = mList[i];
				}
				if (new_id != -1) {
					isExisted = MusicUtils.addTrackToPlaylist(
							NewListActivity.this, new_id, playIds);
					
					if (null!=table_name&&table_name.equals(NewListActivity.this.getString(R.string.my_love))) {						
//						isExisted = !MusicUtils.addAllMusicInfoToLocalTable(table_name,
//								mSelectedArrayList, NewListActivity.this, null);
						
						isExisted =!MusicUtils.AsyncAddAllMusicInfoToLocalTable(DatabaseConstant.TABLENAME_LOVE,mSelectedArrayList, NewListActivity.this, new AddCollectCallBack() {
						public void addCollectResult(boolean result,String name) {
//							ToastUtils.showToast(R.string.sort_love_list_yet);
						}

						@Override
						public void isCollected() {							
//							ToastUtils.showToast(R.string.sort_love_list_yet);
							
						}
					});
					}
				}else if (table_name != null) {
					isExisted = !MusicUtils.addAllMusicInfoToLocalTable(table_name,
							mSelectedArrayList, NewListActivity.this, null);
					

				}
			}
			return isExisted;
		}

		@Override
		// 处理界面
		protected void onPostExecute(Boolean result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			if (count == 0) {
				ToastUtils.showOnceToast(getApplicationContext(),
						NewListActivity.this.getString(R.string.no_add_song));
				NewListActivity.this.finish();
				return;
			}
			if (result) {
				ToastUtils.showOnceToast(getApplicationContext(),
						NewListActivity.this.getString(R.string.Song_has_been));
			} else {
				ToastUtils.showOnceToast(getApplicationContext(),
						NewListActivity.this.getString(R.string.addSuccessful));
			}
			NewListActivity.this.finish();
		}

	}
}
