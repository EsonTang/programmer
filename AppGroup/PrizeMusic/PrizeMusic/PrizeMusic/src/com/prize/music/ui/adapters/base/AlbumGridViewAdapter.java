package com.prize.music.ui.adapters.base;

import static com.prize.music.Constants.SIZE_THUMB;
import static com.prize.music.Constants.SIZE_MEDIU;
import static com.prize.music.Constants.SRC_FIRST_AVAILABLE;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.prize.music.cache.ImageInfo;
import com.prize.music.cache.ImageProvider;
import com.prize.music.views.Album_ViewHolderGrid;

import java.lang.ref.WeakReference;

public abstract class AlbumGridViewAdapter extends SimpleCursorAdapter {

	private WeakReference<Album_ViewHolderGrid> holderReference;

	protected Context mContext;

	private ImageProvider mImageProvider;

	public String mGridType = null, mLineOneText = null, mLineTwoText = null;

	public String[] mImageData = null;

	public long mPlayingId = 0, mCurrentId = 0;

	public AlbumGridViewAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		mContext = context;
		mImageProvider = ImageProvider.getInstance((Activity) mContext);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		Cursor mCursor = (Cursor) getItem(position);
		setupViewData(mCursor);
		// ViewHolderGrid
		final Album_ViewHolderGrid viewholder;
		if (view != null) {
			viewholder = new Album_ViewHolderGrid(view);
			holderReference = new WeakReference<Album_ViewHolderGrid>(
					viewholder);
			view.setTag(holderReference.get());
		} else {
			viewholder = (Album_ViewHolderGrid) convertView.getTag();
		}

		holderReference.get().mViewHolderLineOne.setText(mLineOneText);

		ImageInfo mInfo = new ImageInfo();
		mInfo.type = mGridType;
		// mInfo.size = SIZE_THUMB;
		mInfo.size = SIZE_MEDIU;
		mInfo.source = SRC_FIRST_AVAILABLE;
		mInfo.data = mImageData;
		mImageProvider.loadImage(viewholder.mViewHolderImage, mInfo);

		// if (mPlayingId == mCurrentId) {
		// holderReference.get().mPeakOne
		// .setImageResource(R.anim.peak_meter_1);
		// holderReference.get().mPeakTwo
		// .setImageResource(R.anim.peak_meter_2);
		// mPeakOneAnimation = (AnimationDrawable)
		// holderReference.get().mPeakOne
		// .getDrawable();
		// mPeakTwoAnimation = (AnimationDrawable)
		// holderReference.get().mPeakTwo
		// .getDrawable();
		// try {
		// if (MusicUtils.mService.isPlaying()) {
		// mPeakOneAnimation.start();
		// mPeakTwoAnimation.start();
		// } else {
		// mPeakOneAnimation.stop();
		// mPeakTwoAnimation.stop();
		// }
		// } catch (RemoteException e) {
		// e.printStackTrace();
		// }
		// } else {
		// holderReference.get().mPeakOne.setImageResource(0);
		// holderReference.get().mPeakTwo.setImageResource(0);
		// }

		return view;
	}

	public abstract void setupViewData(Cursor mCursor);
}
