package com.prize.music.ui.fragments.grid;

import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.ArtistColumns;

import com.prize.music.R;
import com.prize.music.ui.adapters.grid.ArtistAdapter;
import com.prize.music.ui.fragments.base.GridViewFragment;

import static com.prize.music.Constants.TYPE_ARTIST;

/**
 * 歌手列表
 * 
 * @author Administrator
 *
 */
public class ArtistsFragment extends GridViewFragment {

	public void setupFragmentData() {
		mAdapter = new ArtistAdapter(getActivity(), R.layout.gridview_items,
				null, new String[] {}, new int[] {}, 0);
		mProjection = new String[] { BaseColumns._ID, ArtistColumns.ARTIST,
				ArtistColumns.NUMBER_OF_TRACKS };
		mUri = Audio.Artists.EXTERNAL_CONTENT_URI;
		mSortOrder = Audio.Artists.DEFAULT_SORT_ORDER;
		mFragmentGroupId = 1;
		mType = TYPE_ARTIST;
	}
}
