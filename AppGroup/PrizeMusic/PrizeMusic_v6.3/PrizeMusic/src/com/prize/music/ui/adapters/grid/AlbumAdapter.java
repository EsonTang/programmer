package com.prize.music.ui.adapters.grid;

import static com.prize.music.Constants.TYPE_ALBUM;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio.AlbumColumns;

import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.ui.adapters.base.ListViewAlbumAdapter;

public class AlbumAdapter extends ListViewAlbumAdapter {

	public AlbumAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to, int flags) {
		super(context, layout, c, from, to, flags);
	}

	public void setupViewData(Cursor mCursor) {

		mLineOneText = mCursor.getString(mCursor
				.getColumnIndexOrThrow(AlbumColumns.ALBUM));
		mLineTwoText = mCursor.getString(mCursor
				.getColumnIndexOrThrow(AlbumColumns.ARTIST));
		int albums_plural = mCursor.getInt(mCursor
				.getColumnIndexOrThrow(AlbumColumns.NUMBER_OF_SONGS));
		mLineThreeText = MusicUtils.makeAlbumsLabel(mContext, albums_plural, 0,
				false);
		mGridType = TYPE_ALBUM;
		mImageData = new String[] {
				mCursor.getString(mCursor
						.getColumnIndexOrThrow(BaseColumns._ID)), mLineTwoText,
				mLineOneText };
		mPlayingId = MusicUtils.getCurrentAlbumId();
		mCurrentId = mCursor.getLong(mCursor
				.getColumnIndexOrThrow(BaseColumns._ID));

	}
}
