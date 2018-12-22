package com.prize.music.ui.fragments.list;

import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.widget.RelativeLayout;

import com.prize.music.R;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.list.EditSonglistAdapter;
import com.prize.music.ui.adapters.list.SonglistAdapter;
import com.prize.music.ui.fragments.base.EditSongsListViewFragment;
import com.prize.music.ui.fragments.base.SongsListViewFragment;

import static com.prize.music.Constants.TYPE_SONG;

/**
 * 全部音乐歌曲列表
 * 
 * @author longbaoxiu
 *
 */
public class EditSongsFragment extends EditSongsListViewFragment {

	public void setupFragmentData() {
		mAdapter = new EditSonglistAdapter(getActivity(),
				R.layout.item_songs_layout, null, new String[] {},
				new int[] {}, 0);
		mProjection = new String[] { BaseColumns._ID, MediaColumns.TITLE,
				AudioColumns.ALBUM, AudioColumns.ARTIST };
		StringBuilder where = new StringBuilder();
		where.append(AudioColumns.IS_MUSIC + "=1").append(
				" AND " + MediaColumns.TITLE + " != ''");
		mWhere = where.toString();
		mSortOrder = MediaColumns.TITLE;
		mUri = Audio.Media.EXTERNAL_CONTENT_URI;
		mFragmentGroupId = 3;
		mType = TYPE_SONG;
		mTitleColumn = MediaColumns.TITLE;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
