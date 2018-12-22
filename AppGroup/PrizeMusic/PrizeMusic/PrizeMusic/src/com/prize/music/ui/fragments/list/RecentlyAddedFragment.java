package com.prize.music.ui.fragments.list;

import static com.prize.music.Constants.NUMWEEKS;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.RelativeLayout;

import com.prize.music.R;
import com.prize.music.helpers.AddIdCursorLoader;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.list.RecentlyAddedAdapter;
import com.prize.music.ui.fragments.base.TopTitleListViewFragment;

/**
 * 最近添加
 * 
 * @author Administrator
 *
 */
public class RecentlyAddedFragment extends TopTitleListViewFragment {
	// private DataObserver mDataObserver;

	public void setupFragmentData() {
		mAdapter = new RecentlyAddedAdapter(getActivity(),
				R.layout.listview_items, null, new String[] {}, new int[] {}, 0);
		mProjection = new String[] { BaseColumns._ID, MediaColumns.TITLE,
				MediaColumns.DATA, AudioColumns.ALBUM, AudioColumns.ARTIST, };
		StringBuilder where = new StringBuilder();
		int X = MusicUtils.getIntPref(getActivity(), NUMWEEKS, 5) * 3600 * 24 * 7;
		where.append(MediaColumns.TITLE + " != ''");
		where.append(" AND " + AudioColumns.IS_MUSIC + "=1");
		where.append(" AND " + MediaColumns.DATE_ADDED + ">"
				+ (System.currentTimeMillis() / 1000 - X));
		mWhere = where.toString();
		mSortOrder = MediaColumns.TITLE;
		// mSortOrder = MediaColumns.DATE_ADDED + " DESC";
		mUri = Audio.Media.EXTERNAL_CONTENT_URI;
		mTitleColumn = MediaColumns.TITLE;
		// mDataObserver = new DataObserver(new Handler());
		// getActivity().getContentResolver().registerContentObserver(
		// MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false,
		// mDataObserver);
		View shuffle_temp = View.inflate(getActivity(), R.layout.shuffle_all,
				null);
		mListView.addHeaderView(shuffle_temp);
		RelativeLayout shuffle = (RelativeLayout) shuffle_temp
				.findViewById(R.id.shuffle_wrapper);
		shuffle.setVisibility(View.VISIBLE);
		shuffle.setOnClickListener(new RelativeLayout.OnClickListener() {
			public void onClick(View v) {
				if (isSelectMode) {
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
	}

	// private class DataObserver extends ContentObserver {// 监听
	// public DataObserver(Handler handler) {
	// super(handler);
	// }
	//
	// // 当ContentProvier数据发生改变，则触发该函数
	// @Override
	// public void onChange(boolean selfChange) {
	// super.onChange(selfChange);
	// refresh();
	// }
	// }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new AddIdCursorLoader(getActivity(), mUri, mProjection, mWhere,
				null, mSortOrder);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// getActivity().getContentResolver().unregisterContentObserver(
		// mDataObserver);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

}
