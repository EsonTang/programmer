package com.prize.music.ui.adapters.list;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Audio.AudioColumns;

import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.ui.adapters.base.ListViewAdapter;

import static com.prize.music.Constants.TYPE_ALBUM;

public class RecentlyAddedAdapter extends ListViewAdapter {

	public RecentlyAddedAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
	}

	public void setupViewData(Cursor mCursor) {
		mLineOneText = mCursor.getString(mCursor
				.getColumnIndexOrThrow(MediaColumns.TITLE));

		mLineTwoText = mCursor.getString(mCursor
				.getColumnIndexOrThrow(AudioColumns.ARTIST));

		mPlayingId = MusicUtils.getCurrentAudioId();
		mCurrentId = mCursor.getLong(mCursor
				.getColumnIndexOrThrow(BaseColumns._ID));

		mListType = TYPE_ALBUM;
		showContextEnabled = false;
	}

}
