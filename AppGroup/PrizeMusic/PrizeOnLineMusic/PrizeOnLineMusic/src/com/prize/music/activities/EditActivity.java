package com.prize.music.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.app.constants.Constants;
import com.prize.app.util.ToastUtils;
import com.prize.music.R;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.ui.adapters.list.EditActivityAdapter;
import com.prize.music.ui.adapters.list.EditActivityAdapter.EditViewHolder;

/**
 * 首页编辑点击编辑按钮跳转的activity
 * 
 * @author huanglingjun
 * 
 */
public class EditActivity extends FragmentActivity implements OnClickListener,
		LoaderCallbacks<Cursor> {
	// Adapter
	protected EditActivityAdapter mAdapter;

	// ListView
	protected GridView edit_grideView;

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

	private TextView edit_fg_all;
	private Button edit_sure, edit_neg;

	private ImageView edit_fg_back;

	private ArrayList<String> myIds;

	private Button dia_edit_neg, dia_edit_sure;

	private AlertDialog alertDialog;

	private int selectCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		StateBarUtils.initStateBar(this);

		setContentView(R.layout.activity_edit);
		StateBarUtils.changeStatus(getWindow());

		if (null!=getIntent()) {
			if(getIntent().getStringArrayListExtra("myIds") != null) {
			myIds = getIntent().getStringArrayListExtra("myIds");
			}
			if(getIntent().getSerializableExtra("myEditDatas") != null) {
				mData = (List<Map<String, Object>>) getIntent().getSerializableExtra("myEditDatas");
				}
		}
		edit_grideView = (GridView) findViewById(R.id.edit_grideView);
		edit_fg_all = (TextView) findViewById(R.id.edit_fg_all);
		edit_sure = (Button) findViewById(R.id.edit_sure);
		edit_neg = (Button) findViewById(R.id.edit_neg);
		edit_fg_back = (ImageView) findViewById(R.id.edit_fg_back);

		edit_fg_all.setOnClickListener(this);
		edit_sure.setOnClickListener(this);
		edit_neg.setOnClickListener(this);
		edit_fg_back.setOnClickListener(this);

//		mData = new ArrayList<Map<String, Object>>();

		edit_grideView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				return false;
			}
		});

		edit_grideView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				EditViewHolder vHollder = (EditViewHolder) view.getTag();
				// 在每次获取点击的item时将对于的checkbox状态改变，同时修改map的值。
				vHollder.cb.toggle();
				EditActivityAdapter.isSelected.put(position,
						vHollder.cb.isChecked());
				if (vHollder.cb.isChecked()) {
					selectCount++;
				} else {
					selectCount--;
				}
				if (selectCount == edit_grideView.getCount()) {
					edit_fg_all.setText(EditActivity.this
							.getString(R.string.no_select));
				} else {
					edit_fg_all.setText(EditActivity.this
							.getString(R.string.all_select));
				}
			}
		});

		/* sqlite = SQLUtils.getInstance(this); */
		initData();
		// initCur();
		getSupportLoaderManager().initLoader(0, null, EditActivity.this);

	}

	private void initData() {
		mProjection = new String[] { BaseColumns._ID, PlaylistsColumns.NAME };
		mSortOrder = Audio.Playlists.DEFAULT_SORT_ORDER;
		mUri = Audio.Playlists.EXTERNAL_CONTENT_URI;
		mFragmentGroupId = 5;
		mType = Constants.TYPE_PLAYLIST;
		mTitleColumn = PlaylistsColumns.NAME;

	}

//	private void initListData(Cursor cursor) {
//		while (cursor.moveToNext()) {
//			String title = cursor.getString(cursor
//					.getColumnIndexOrThrow(PlaylistsColumns.NAME));
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("title", title);
//			mData.add(map);
//
//		}
//		SharedPreferences sharedPreferences = this.getSharedPreferences(
//				"ShareXML", Context.MODE_PRIVATE);
//		String favorites = sharedPreferences.getString("Favorites",
//				this.getString(R.string.my_love));
//		String newList = sharedPreferences.getString("NewList",
//				this.getString(R.string.create_list));
//		// 去除我喜欢的和新建列表
//		if (favorites != null && newList != null) {
//			for (int i = 0; i < mData.size(); i++) {
//				if (mData.get(i).get("title").equals(favorites)) {
//					String temp = (String) mData.get(i).get("title");
//					mData.remove(i);
//				}
//
//				if (mData.get(i).get("title").equals(newList)) {
//					mData.remove(i);
//				}
//			}
//		}
//
//		if (mData.size() > 0) {
//			mAdapter = new EditActivityAdapter(EditActivity.this, mData, false);
//			edit_grideView.setAdapter(mAdapter);
//		}
//	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.edit_fg_all:
			String stringContent = edit_fg_all.getText().toString().trim();
			if (stringContent != null
					&& stringContent.equals(EditActivity.this
							.getString(R.string.all_select))) {
				mAdapter.init(true);
				mAdapter.notifyDataSetChanged();
				edit_fg_all.setText(EditActivity.this
						.getString(R.string.no_select));
				selectCount = mAdapter.getCount();
			} else if (stringContent != null
					&& stringContent.equals(EditActivity.this
							.getString(R.string.no_select))) {
				mAdapter.init(false);
				mAdapter.notifyDataSetChanged();
				edit_fg_all.setText(EditActivity.this
						.getString(R.string.all_select));
				selectCount = 0;
			}
			break;

		case R.id.edit_sure:
			int selectCount = 0;
			for (int i = 0; i < myIds.size(); i++) {
				if (EditActivityAdapter.isSelected.get(i)) {
					selectCount++;
				}
			}
			if (selectCount == 0) {
				ToastUtils.showOnceToast(getApplicationContext(),
						this.getString(R.string.Not_selected_playlists));
			} else {
				initDialog();
			}
			break;

		case R.id.edit_neg:
			finish();
			break;
		case R.id.edit_fg_back:
			finish();
			break;
		case R.id.dia_edit_neg:
			if (alertDialog != null && alertDialog.isShowing()) {
				alertDialog.dismiss();
			}
			break;
		case R.id.dia_edit_sure:
			for (int i = 0; i < myIds.size(); i++) {
				if (EditActivityAdapter.isSelected.get(i)) {
					// long mId=edit_grideView.getItemIdAtPosition(i);
					long mId = Long.parseLong(myIds.get(i));
					Uri uri = ContentUris.withAppendedId(
							MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
							mId);
					getContentResolver().delete(uri, null, null);
					// mData.remove(i);
				}
			}
			if (alertDialog != null && alertDialog.isShowing()) {
				alertDialog.dismiss();
			}
			// Toast.makeText(getApplicationContext(),
			// "删除成功！",Toast.LENGTH_LONG).show();
			finish();
			break;

		default:
			break;
		}

	}

	private void initDialog() {
		alertDialog = new AlertDialog.Builder(EditActivity.this).create();
		LayoutInflater inflater = LayoutInflater.from(EditActivity.this);
		View view = inflater.inflate(R.layout.dialog_edit, null);
		view.setBackgroundResource(R.drawable.icon_dialog);

		dia_edit_neg = (Button) view.findViewById(R.id.dia_edit_neg);
		dia_edit_sure = (Button) view.findViewById(R.id.dia_edit_sure);

		dia_edit_neg.setOnClickListener(this);
		dia_edit_sure.setOnClickListener(this);
		alertDialog.setView(view);
		alertDialog.show();
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
		edit_grideView.invalidateViews();
		if (mData.size() > 0) {
			mAdapter = new EditActivityAdapter(EditActivity.this, mData, false);
			edit_grideView.setAdapter(mAdapter);
		}
//		initListData(data);
		// edit_grideView.setAdapter(mAdapter);
		// mCursor = data;

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

	}

}
