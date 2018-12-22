package com.prize.music.ui.adapters.base;

import static com.prize.app.constants.Constants.SIZE_THUMB;
import static com.prize.app.constants.Constants.SRC_FIRST_AVAILABLE;
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
import com.prize.music.views.ViewHolderGrid;
import com.prize.music.R;

public abstract class GridViewAdapter extends SimpleCursorAdapter {

	private AnimationDrawable mPeakTwoAnimation;

	// private WeakReference<ViewHolderGrid> holderReference;

	protected Context mContext;

	private ImageProvider mImageProvider;

	public String mGridType = null, mLineOneText = null, mLineTwoText = null;

	public String[] mImageData = null;

	public long mPlayingId = 0, mCurrentId = 0;

	public GridViewAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		mContext = context;
		mImageProvider = ImageProvider.getInstance((Activity) mContext);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		Cursor mCursor = (Cursor) getItem(position);
		if (!scrollState) {
			setupViewData(mCursor);

			// ViewHolderGrid
			final ViewHolderGrid viewholder;
			if (view != null) {
				viewholder = new ViewHolderGrid(view);
				// holderReference = new
				// WeakReference<ViewHolderGrid>(viewholder);
				view.setTag(viewholder);
			} else {
				viewholder = (ViewHolderGrid) convertView.getTag();
			}

			viewholder.mViewHolderLineOne.setText(mLineOneText);
			viewholder.mViewHolderLineTwo.setText(mLineTwoText);
			// viewholder.mViewHolderLineOne.setText(mLineOneText);
			// viewholder.mViewHolderLineTwo.setText(mLineTwoText);

			ImageInfo mInfo = new ImageInfo();
			mInfo.type = mGridType;
			mInfo.size = SIZE_THUMB;
			mInfo.source = SRC_FIRST_AVAILABLE;
			mInfo.data = mImageData;
			mImageProvider.loadImage(viewholder.mViewHolderImage, mInfo);

			if (mPlayingId == mCurrentId) {
				// viewholder.mPeakOne
				// .setImageResource(R.anim.peak_meter_1);
				viewholder.mPeakTwo.setImageResource(R.anim.peak_meter_orange);
				// mPeakOneAnimation = (AnimationDrawable)
				// viewholder.mPeakOne
				// .getDrawable();
				mPeakTwoAnimation = (AnimationDrawable) viewholder.mPeakTwo
						.getDrawable();
				try {
					if (MusicUtils.mService.isPlaying()) {
						// mPeakOneAnimation.start();
						mPeakTwoAnimation.start();
					} else {
						// mPeakOneAnimation.stop();
						mPeakTwoAnimation.stop();
						viewholder.mPeakTwo
								.setImageResource(R.drawable.icon_play_stop_black);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else {
				// viewholder.mPeakOne.setImageResource(0);
				viewholder.mPeakTwo.setImageResource(0);
			}
		}
		return view;
	}

	public abstract void setupViewData(Cursor mCursor);

	private boolean scrollState = false;

	public void setScrollState(boolean scrollState) {
		this.scrollState = scrollState;
	}

}
