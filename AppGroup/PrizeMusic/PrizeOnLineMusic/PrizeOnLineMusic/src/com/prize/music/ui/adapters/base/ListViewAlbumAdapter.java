package com.prize.music.ui.adapters.base;

import static com.prize.app.constants.Constants.SIZE_THUMB;
import static com.prize.app.constants.Constants.SRC_FIRST_AVAILABLE;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.RemoteException;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.prize.music.cache.ImageInfo;
import com.prize.music.cache.ImageProvider;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.views.ViewHolderListView;
import com.prize.music.R;

public abstract class ListViewAlbumAdapter extends SimpleCursorAdapter {

	private AnimationDrawable mPeakOneAnimation, mPeakTwoAnimation;

	private WeakReference<ViewHolderListView> holderReference;

	protected Context mContext;

	private ImageProvider mImageProvider;

	public String mGridType = null, mLineOneText = null, mLineTwoText = null,
			mLineThreeText = null;

	public String[] mImageData = null;

	public long mPlayingId = 0, mCurrentId = 0;

	public ListViewAlbumAdapter(Context context, int layout, Cursor c,
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
		final ViewHolderListView viewholder;
		if (view != null) {
			viewholder = new ViewHolderListView(view);
			holderReference = new WeakReference<ViewHolderListView>(viewholder);
			view.setTag(holderReference.get());
		} else {
			viewholder = (ViewHolderListView) convertView.getTag();
		}

		holderReference.get().mViewHolderLineOne.setText(mLineOneText);
		holderReference.get().mViewHolderLineTwo.setText(mLineTwoText);
		holderReference.get().mViewHolderLineThree.setText(mLineThreeText);

		ImageInfo mInfo = new ImageInfo();
		mInfo.type = mGridType;
		mInfo.size = SIZE_THUMB;
		mInfo.source = SRC_FIRST_AVAILABLE;
		mInfo.data = mImageData;
		mImageProvider.loadImage(viewholder.mViewHolderImage, mInfo);

		if (mPlayingId == mCurrentId) {
			// holderReference.get().mPeakOne
			// .setImageResource(R.anim.peak_meter_1);
			holderReference.get().mPeakTwo
					.setImageResource(R.anim.peak_meter_orange);
			// mPeakOneAnimation = (AnimationDrawable)
			// holderReference.get().mPeakOne
			// .getDrawable();
			mPeakTwoAnimation = (AnimationDrawable) holderReference.get().mPeakTwo
					.getDrawable();
			try {
				if (MusicUtils.mService.isPlaying()) {
					// mPeakOneAnimation.start();
					mPeakTwoAnimation.start();
				} else {
					// mPeakOneAnimation.stop();
					mPeakTwoAnimation.stop();
					holderReference.get().mPeakTwo
							.setImageResource(R.drawable.icon_play_stop_black);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			// holderReference.get().mPeakOne.setImageResource(0);
			holderReference.get().mPeakTwo.setImageResource(0);
		}

		return view;
	}

	public abstract void setupViewData(Cursor mCursor);
}
