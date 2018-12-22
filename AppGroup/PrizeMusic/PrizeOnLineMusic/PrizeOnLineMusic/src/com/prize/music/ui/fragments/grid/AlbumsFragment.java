package com.prize.music.ui.fragments.grid;

import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AlbumColumns;

import com.prize.app.constants.Constants;
import com.prize.music.ui.adapters.grid.AlbumAdapter;
import com.prize.music.ui.fragments.base.AlbumsListViewFragment;
import com.prize.music.R;

/**
 * 专辑
 * 
 * @author Administrator
 *
 */
public class AlbumsFragment extends AlbumsListViewFragment {

	public void setupFragmentData() {
		mAdapter = new AlbumAdapter(getActivity(),
				R.layout.list_view_album_items, null, new String[] {},
				new int[] {}, 0);
		mProjection = new String[] { BaseColumns._ID, AlbumColumns.ALBUM,
				AlbumColumns.ARTIST, AlbumColumns.ALBUM_ART,
				AlbumColumns.NUMBER_OF_SONGS };
		mUri = Audio.Albums.EXTERNAL_CONTENT_URI;
		mSortOrder = Audio.Albums.DEFAULT_SORT_ORDER;
		mFragmentGroupId = 2;
		mType = Constants.TYPE_ALBUM;
	}

}
