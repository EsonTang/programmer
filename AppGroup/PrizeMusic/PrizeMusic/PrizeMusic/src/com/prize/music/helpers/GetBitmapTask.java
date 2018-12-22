package com.prize.music.helpers;

import static com.prize.music.Constants.SIZE_NORMAL;
import static com.prize.music.Constants.SIZE_THUMB;
import static com.prize.music.Constants.SRC_FILE;
import static com.prize.music.Constants.SRC_FIRST_AVAILABLE;
import static com.prize.music.Constants.SRC_GALLERY;
import static com.prize.music.Constants.SRC_LASTFM;
import static com.prize.music.Constants.TYPE_ALBUM;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.prize.music.cache.ImageInfo;
import com.prize.music.helpers.utils.ImageUtils;
import com.prize.music.helpers.utils.LogUtils;

public class GetBitmapTask extends AsyncTask<String, Integer, Bitmap> {
	private String TAG = "GetBitmapTask";
	// private WeakReference<OnBitmapReadyListener> mListenerReference;
	private OnBitmapReadyListener mListenerReference;

	// private Reference<Context> mContextReference;
	private Context mContext;

	private ImageInfo mImageInfo;

	private int mThumbSize;

	public GetBitmapTask(int thumbSize, ImageInfo imageInfo,
			OnBitmapReadyListener listener, Context context) {
		// mListenerReference = new
		// WeakReference<OnBitmapReadyListener>(listener);
		mListenerReference = listener;
		// mContextReference = new WeakReference<Context>(context);
		mContext = context.getApplicationContext();
		mImageInfo = imageInfo;
		mThumbSize = thumbSize;
	}

	@Override
	protected Bitmap doInBackground(String... ignored) {
		// Context context = mContextReference.get();
		Context context = mContext;
		if (context == null) {
			return null;
		}
		// Get bitmap from proper source
		File nFile = null;

		if (mImageInfo.source.equals(SRC_FILE) && !isCancelled()) {
			nFile = ImageUtils.getImageFromMediaStore(context, mImageInfo);
		} else if (mImageInfo.source.equals(SRC_LASTFM) && !isCancelled()) {
			// nFile = ImageUtils.getImageFromWeb(context, mImageInfo);
		} else if (mImageInfo.source.equals(SRC_GALLERY) && !isCancelled()) {
			nFile = ImageUtils.getImageFromGallery(context, mImageInfo);
		} else if (mImageInfo.source.equals(SRC_FIRST_AVAILABLE)
				&& !isCancelled()) {
			Bitmap bitmap = null;
			if (mImageInfo.size.equals(SIZE_NORMAL)) {
				bitmap = ImageUtils.getNormalImageFromDisk(context, mImageInfo);
			} else if (mImageInfo.size.equals(SIZE_THUMB)) {
				bitmap = ImageUtils.getThumbImageFromDisk(context, mImageInfo,
						mThumbSize);
			}
			// if we have a bitmap here then its already properly sized
			if (bitmap != null) {
				return bitmap;
			}

			if (mImageInfo.type.equals(TYPE_ALBUM)) {
				nFile = ImageUtils.getImageFromMediaStore(context, mImageInfo);
			}
			// if (nFile == null
			// && (mImageInfo.type.equals(TYPE_ALBUM) || mImageInfo.type
			// .equals(TYPE_ARTIST)))
			// nFile = ImageUtils.getImageFromWeb(context, mImageInfo);
		}
		if (nFile != null) {
			// if requested size is normal return it
			if (mImageInfo.size.equals(SIZE_NORMAL))
				return BitmapFactory.decodeFile(nFile.getAbsolutePath());
			// if it makes it here we want a thumbnail image
			return ImageUtils.getThumbImageFromDisk(context, nFile, mThumbSize);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		super.onPostExecute(bitmap);
		// OnBitmapReadyListener listener = mListenerReference.get();
		OnBitmapReadyListener listener = mListenerReference;
		// if (bitmap == null && !isCancelled()) {
		// if (mImageInfo.size.equals(SIZE_THUMB))
		// bitmap = BitmapFactory.decodeResource(mContext
		// .getResources(), R.drawable.no_art_small);
		// else if (mImageInfo.size.equals(SIZE_NORMAL))
		// bitmap = BitmapFactory.decodeResource(mContext
		// .getResources(), R.drawable.no_art_normal);
		// }
		// if (bitmap != null && !isCancelled()) {
		if (!isCancelled()) {
			if (listener != null && bitmap != null && !bitmap.isRecycled()) {
				listener.bitmapReady(bitmap,
						ImageUtils.createShortTag(mImageInfo) + mImageInfo.size);
			}
		}
	}

	public static interface OnBitmapReadyListener {
		public void bitmapReady(Bitmap bitmap, String tag);
	}
}
