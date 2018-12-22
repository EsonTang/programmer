package com.prize.music.ui.fragments.list;

import static com.prize.app.constants.Constants.EXTERNAL;
import static com.prize.app.constants.Constants.TYPE_GENRE;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Genres;
import android.provider.MediaStore.MediaColumns;

import com.prize.music.ui.adapters.list.GenreListAdapter;
import com.prize.music.ui.fragments.base.ListViewFragment;
import com.prize.music.R;

public class GenreListFragment extends ListViewFragment {

	public GenreListFragment(Bundle args) {
		setArguments(args);
	}

	public GenreListFragment() {
	}
	public void setupFragmentData() {
		mAdapter = new GenreListAdapter(getActivity(), R.layout.listview_items,
				null, new String[] {}, new int[] {}, 0);
		mProjection = new String[] { BaseColumns._ID, MediaColumns.TITLE,
				AudioColumns.ALBUM, AudioColumns.ARTIST };
		StringBuilder where = new StringBuilder();
		where.append(AudioColumns.IS_MUSIC + "=1").append(
				" AND " + MediaColumns.TITLE + " != ''");
		mWhere = where.toString();
		mSortOrder = Genres.Members.DEFAULT_SORT_ORDER;
		mUri = Genres.Members.getContentUri(EXTERNAL,
				getArguments().getLong(BaseColumns._ID));
		mFragmentGroupId = 3;
		mType = TYPE_GENRE;
		mTitleColumn = MediaColumns.TITLE;
	}
}
