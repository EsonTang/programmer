package com.prize.music.ui.adapters.list;

import static com.prize.app.constants.Constants.TYPE_ARTIST;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;

import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.ui.adapters.base.ListViewAdapter;

public class GenreListAdapter extends ListViewAdapter {

	public GenreListAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
	}

	@Override
	public void setupViewData(Cursor mCursor) {
		mLineOneText = mCursor.getString(mCursor
				.getColumnIndexOrThrow(MediaColumns.TITLE));

		mLineTwoText = mCursor.getString(mCursor
				.getColumnIndexOrThrow(AudioColumns.ARTIST));

		mImageData = new String[] { mLineTwoText };

		mPlayingId = MusicUtils.getCurrentAudioId();
		mCurrentId = mCursor.getLong(mCursor
				.getColumnIndexOrThrow(BaseColumns._ID));

		mListType = TYPE_ARTIST;
	}

}
