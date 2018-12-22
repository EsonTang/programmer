package com.prize.music.ui.adapters.base;

import static com.prize.music.Constants.SIZE_THUMB;
import static com.prize.music.Constants.SRC_FIRST_AVAILABLE;
import static com.prize.music.Constants.TYPE_ARTIST;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Audio.Playlists;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;
import com.prize.music.R;
import com.prize.music.cache.ImageInfo;
import com.prize.music.cache.ImageProvider;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.history.HistoryColumns;
import com.prize.music.views.ViewHolderList;

public abstract class DragSortListViewAdapter extends
		SimpleDragSortCursorAdapter {

	private AnimationDrawable mPeakTwoAnimation;
	// private AnimationDrawable mPeakOneAnimation, mPeakTwoAnimation;

	private WeakReference<ViewHolderList> holderReference;

	protected Context mContext;

	private ImageProvider mImageProvider;

	public String mLineOneText = null, mLineTwoText = null;

	public String[] mImageData = null;

	public long mPlayingId = 0, mCurrentId = 0;

	public static Map<Integer, Boolean> isSelected;
	private boolean mode = false;

	public static ArrayList<String> names;

	private Handler mHandler;
	private final View.OnClickListener showContextMenu = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			v.showContextMenu();
		}
	};

	public void changeCursor(Cursor cursor) {
		init(cursor);
		super.changeCursor(cursor);
	};

	public void setMode(boolean flag) {
		mode = flag;
	}

	public void setSelectAll(boolean boo) {
		isSelected.clear();
		for (int i = 0; i < getCount(); i++) {
			isSelected.put(i, boo);
		}
	}

	// 初始化
	private void init(Cursor cursor) {
		// 这儿定义isSelected这个map是记录每个listitem的状态，初始状态全部为false。
		isSelected.clear();
		names.clear();
		while (cursor.moveToNext()) {
			String title = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaColumns.TITLE));
			names.add(title);
		}
		for (int i = 0; i < cursor.getCount(); i++) {
			isSelected.put(i, false);
		}
	}

	public DragSortListViewAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		mContext = context;
		mImageProvider = ImageProvider.getInstance((Activity) mContext);
		isSelected = new HashMap<Integer, Boolean>();
		names = new ArrayList<String>();
		mHandler = new Handler();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);

		Cursor mCursor = (Cursor) getItem(position);
		setupViewData(mCursor);

		final ViewHolderList viewholder;
		if (view != null) {
			viewholder = new ViewHolderList(view);
			holderReference = new WeakReference<ViewHolderList>(viewholder);
			view.setTag(holderReference.get());
		} else {
			viewholder = (ViewHolderList) convertView.getTag();
		}
		holderReference.get().mViewHolderLineOne.setText(mLineOneText);

		holderReference.get().mViewHolderLineTwo.setText(mLineTwoText);

		ImageInfo mInfo = new ImageInfo();
		mInfo.type = TYPE_ARTIST;
		mInfo.size = SIZE_THUMB;
		mInfo.source = SRC_FIRST_AVAILABLE;
		mInfo.data = mImageData;
		// mImageProvider.loadImage(viewholder.mViewHolderImage, mInfo);

		// holderReference.get().mQuickContext.setOnClickListener(showContextMenu);

		if (mPlayingId == mCurrentId) {
			// holderReference.get().mPeakOne
			// .setImageResource(R.anim.peak_meter_1);
			holderReference.get().mPeakTwo
					.setImageResource(R.anim.peak_meter_2);
			// mPeakOneAnimation = (AnimationDrawable)
			// holderReference.get().mPeakOne
			// .getDrawable();
			mPeakTwoAnimation = (AnimationDrawable) holderReference.get().mPeakTwo
					.getDrawable();
			try {
				if (MusicUtils.mService != null
						&& MusicUtils.mService.isPlaying()) {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							mPeakTwoAnimation.start();
						}
					});
				} else {
					holderReference.get().mPeakTwo
							.setImageResource(R.drawable.icon_play_stop);
					mPeakTwoAnimation.stop();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			holderReference.get().mPeakTwo.setImageResource(0);
		}

		if (mode) {
			holderReference.get().checkBox.setVisibility(View.VISIBLE);
		} else {
			holderReference.get().checkBox.setVisibility(View.INVISIBLE);
		}

		holderReference.get().checkBox.setChecked(isSelected.get(position));
		return view;
	}

	public abstract void setupViewData(Cursor mCursor);

	public int[] getSelectedItemPositions() {
		int count = 0;
		for (int i = 0; i < getCount(); i++) {
			if (isSelected.get(i)) {
				count++;
			}
		}
		int[] checkedPostions = new int[count];
		for (int i = 0, j = 0; i < getCount(); i++) {
			if (isSelected.get(i)) {
				checkedPostions[j] = i;
				j++;
			}
		}
		return checkedPostions;
	}

	public long[] getSelectedAudioIds() {
		int[] checkedPostions = getSelectedItemPositions();
		long[] selectedAudioIds = new long[checkedPostions.length];
		Cursor mCursor;
		int len = checkedPostions.length;
		for (int i = 0; i < len; i++) {
			mCursor = (Cursor) getItem(checkedPostions[i]);
			long mSelectedId = mCursor.getLong(mCursor
					.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID));
			selectedAudioIds[i] = mSelectedId;
		}
		return selectedAudioIds;
	}
}
