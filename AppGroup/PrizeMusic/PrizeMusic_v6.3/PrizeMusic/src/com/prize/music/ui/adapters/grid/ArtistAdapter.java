package com.prize.music.ui.adapters.grid;

import static com.prize.music.Constants.EXTERNAL;
import static com.prize.music.Constants.TYPE_ARTIST;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.text.TextUtils;

import com.prize.music.Constants;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.ui.adapters.base.GridViewAdapter;

/**
 * 歌手列表adapter
 * 
 * @author longbaoxiu
 *
 */
public class ArtistAdapter extends GridViewAdapter {

	public ArtistAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to, int flags) {
		super(context, layout, c, from, to, flags);
	}

	public void setupViewData(Cursor mCursor) {

		mLineOneText = mCursor.getString(mCursor
				.getColumnIndexOrThrow(ArtistColumns.ARTIST));
		int albums_plural = mCursor.getInt(mCursor
				.getColumnIndexOrThrow(ArtistColumns.NUMBER_OF_TRACKS));
		boolean unknown = mLineOneText == null
				|| mLineOneText.equals(MediaStore.UNKNOWN_STRING);
		mLineTwoText = MusicUtils.makeAlbumsLabel(mContext, albums_plural, 0,
				unknown);
		mGridType = Constants.TYPE_ALBUM;
		mImageData = new String[] { mLineOneText };
		// mImageData = new String[] { mLineOneText };
		mPlayingId = MusicUtils.getCurrentArtistId();
		mCurrentId = mCursor.getLong(mCursor
				.getColumnIndexOrThrow(BaseColumns._ID));

		long artistId = mCursor.getLong(mCursor
				.getColumnIndexOrThrow(BaseColumns._ID));
		Uri mUri = Audio.Artists.Albums.getContentUri(EXTERNAL, artistId);
		// new AsyncLoader_GuessInfo().execute(mUri);
		Cursor result = MusicUtils.query(mContext, mUri, new String[] {
				AlbumColumns.ALBUM, BaseColumns._ID, }, null, null, null);
		if (result != null && result.getCount() > 0) {

			while (result.moveToFirst()) {
				// String album_art = result.getString(result
				// .getColumnIndex(AlbumColumns.ALBUM_ART));
				// if (!TextUtils.isEmpty(album_art)) {
				try {
					String albumName = result.getString(result
							.getColumnIndex(AlbumColumns.ALBUM));
					String albumId = result.getString(result
							.getColumnIndex(BaseColumns._ID));
					mImageData = new String[] { albumId, albumName,
							mLineOneText };

				} catch (Exception e) {
					return;
				}
				if (result != null && !result.isClosed()) {
					result.close();
					return;
				} else {
					return;

				}
				// }
			}

		}
	}

	class AsyncLoader_GuessInfo extends AsyncTask<Uri, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Uri... params) {
			return MusicUtils.query(mContext, params[0], new String[] {
					AlbumColumns.ARTIST, AlbumColumns.ALBUM, BaseColumns._ID,
					AlbumColumns.ALBUM_ART }, null, null, null);
		}

		@Override
		protected void onPostExecute(Cursor result) {
			if (result != null && result.getCount() > 0) {

				while (result.moveToFirst()) {
					String album_art = result.getString(result
							.getColumnIndex(AlbumColumns.ALBUM_ART));
					if (!TextUtils.isEmpty(album_art)) {
						try {
							String albumName = result.getString(result
									.getColumnIndex(AlbumColumns.ALBUM));
							String albumId = result.getString(result
									.getColumnIndex(BaseColumns._ID));
							LogUtils.i("long", "albumId=" + albumId
									+ "---albumName=" + albumName);
							mImageData = new String[] { albumId, albumName,
									mLineOneText };

						} catch (Exception e) {
							return;
						}
						if (result != null && !result.isClosed()) {
							result.close();
							return;
						} else {
							return;

						}
					}
				}

			}

		}
	}

}
