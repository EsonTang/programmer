package com.prize.music.ui.adapters.list;

import static com.prize.app.constants.Constants.TYPE_ALBUM;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;

import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.ui.adapters.base.ListViewAdapter;

public class RecentlyPlayAdapter extends ListViewAdapter {

	public RecentlyPlayAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
	}

	public void setupViewData(Cursor mCursor) {
		mLineOneText = mCursor.getString(mCursor
				.getColumnIndex(MediaColumns.TITLE));

		mLineTwoText = mCursor.getString(mCursor
				.getColumnIndex(AudioColumns.ARTIST));

		mPlayingId = MusicUtils.getCurrentAudioId();
		mCurrentId = mCursor.getLong(mCursor.getColumnIndex(BaseColumns._ID));
		// .getColumnIndex(HistoryColumns.AUDIO_ID));

		mListType = TYPE_ALBUM;
		showContextEnabled = false;
	}
}
