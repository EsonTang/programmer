/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：播放界面歌曲列表
 *当前版本：V1.0
 *作  者：longbaoxiu
 *完成日期：2015-7-21
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ********************************************/
package com.prize.music.ui.fragments.list;

import static com.prize.music.Constants.TYPE_ARTIST;
import static com.prize.music.Constants.TYPE_SONG;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.prize.music.R;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.ui.adapters.base.ListViewAdapter;
import com.prize.music.ui.adapters.list.SonglistAdapter;
import com.prize.music.ui.fragments.base.PlayingListViewFragment;
import com.prize.music.views.ViewHolderList;

/**
 * 播放界面歌曲列表
 * 
 * @author longbaoxiu
 *
 */
public class SongsInPlayingFragment extends PlayingListViewFragment {
	public void setupFragmentData() {
		long[] queue = MusicUtils.getQueue();
		StringBuilder where = new StringBuilder();
		where.append(AudioColumns.IS_MUSIC + "=1").append(
				" AND " + MediaColumns.TITLE + " != ''");
		if (queue != null && queue.length > 0) {
			StringBuilder build = new StringBuilder("(");
			for (int i = 0; i < queue.length; i++) {
				if (i == queue.length - 1) {
					build.append(queue[i] + ")");
					break;
				}
				build.append(queue[i] + ",");
			}
			where.append(" AND " + BaseColumns._ID + " != ''")
					.append(" AND " + BaseColumns._ID + " in ").append(build);
		}
		if (mAdapter == null) {
			mAdapter = new SongsInPlayingAdapter(getActivity(),
					R.layout.songsinplaying_list_item, null, new String[] {},
					new int[] {}, 0);
		}
		if (mProjection == null) {
			mProjection = new String[] { BaseColumns._ID, MediaColumns.TITLE,
					AudioColumns.ALBUM, AudioColumns.ARTIST };
		}
		mWhere = where.toString();
		// mSortOrder = MediaColumns.DATE_ADDED + " DESC";
		mSortOrder = MediaColumns.TITLE;
		mUri = Audio.Media.EXTERNAL_CONTENT_URI;
		mFragmentGroupId = 3;
		mType = TYPE_SONG;
		mTitleColumn = MediaColumns.TITLE;
		
		mListView.setDivider(getActivity().getDrawable(R.drawable.line_separator_black));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, final int position,
			long id) {
		mAdapter.play(v);
		parent.post(new Runnable() {

			@Override
			public void run() {
				MusicUtils.playAll(getActivity(), mCursor, position);

			}
		});
	}
	
	
	
	private class SongsInPlayingAdapter extends ListViewAdapter{
		public SongsInPlayingAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}

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
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			Cursor mCursor = (Cursor) getItem(position);
			setupViewData(mCursor);
			final ViewHolderList viewholder;
			if (view != null) {
				viewholder = new ViewHolderList(view);
				viewholder.mPeakTwo.setImageResource(R.anim.peak_meter_white_2);
				view.setTag(viewholder);
			} else {
				viewholder = (ViewHolderList) convertView.getTag();
			}

			if (viewholder != null && viewholder.checkBox != null) {

				if (isSelectMode) {
					viewholder.checkBox.setVisibility(View.VISIBLE);

				} else {
					viewholder.checkBox.setVisibility(View.INVISIBLE);
				}
			}
			if (mCheckedStates.get(position)) {
				viewholder.checkBox.setChecked(true);
			} else {
				viewholder.checkBox.setChecked(false);
			}

			if (mLineOneText != null) {
				viewholder.mViewHolderLineOne.setText(mLineOneText);
			} else {
				viewholder.mViewHolderLineOne.setVisibility(View.GONE);
			}

			if (mLineTwoText != null) {
				viewholder.mViewHolderLineTwo.setText(mLineTwoText);
			} else {
				viewholder.mViewHolderLineOne.setPadding(left, top, 0, 0);
				viewholder.mViewHolderLineTwo.setVisibility(View.GONE);
			}

			if ((mPlayingId != 0 && mCurrentId != 0) && mPlayingId == mCurrentId) {
				viewholder.mPeakTwo.setVisibility(View.VISIBLE);
				mPeakTwoAnimation = (AnimationDrawable) viewholder.mPeakTwo
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
						mPeakTwoAnimation.stop();
						viewholder.mPeakTwo
								.setImageResource(R.drawable.icon_play_stop_white);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else {
				viewholder.mPeakTwo.setVisibility(View.INVISIBLE);
			}
			return view;
		}
		
	}
	
	
	
	

}
