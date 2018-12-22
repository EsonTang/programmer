package com.prize.music.ui.fragments.list;

import static com.prize.music.Constants.TYPE_ARTIST;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.prize.music.R;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.list.ArtistListAdapter;

/**
 * 按照歌手分类的歌曲列表
 * 
 * @author
 *
 */
public class ArtistListFragment extends ScrollerFragment {

	private String TAG = ArtistListFragment.class.getSimpleName();

	public ArtistListFragment(Bundle args) {
		setArguments(args);
	}
	
	public ArtistListFragment() {
		super();
	}

	@Override
	public void setupFragmentData() {
		mAdapter = new ArtistListAdapter(getActivity(),
				R.layout.item_songs_layout, null, new String[] {},
				new int[] {}, 0);
		mProjection = new String[] { BaseColumns._ID, MediaColumns.TITLE,
				AudioColumns.ALBUM, AudioColumns.ARTIST };
		StringBuilder where = new StringBuilder();
		where.append(AudioColumns.IS_MUSIC + "=1").append(
				" AND " + MediaColumns.TITLE + " != ''");
		long artist_id = getArguments().getLong(BaseColumns._ID);
		where.append(" AND " + AudioColumns.ARTIST_ID + "=" + artist_id);
		mWhere = where.toString();
		mSortOrder = MediaColumns.TITLE;
		mUri = Audio.Media.EXTERNAL_CONTENT_URI;
		mFragmentGroupId = 88;
		mType = TYPE_ARTIST;
		mTitleColumn = MediaColumns.TITLE;

		View shuffle_temp = View.inflate(getActivity(),
				R.layout.artist_shuffle_all, null);
		mListView.addHeaderView(shuffle_temp);
		View shuffle = shuffle_temp.findViewById(R.id.shuffle_wrapper);
		shuffle.setVisibility(View.VISIBLE);
		shuffle.setOnClickListener(new RelativeLayout.OnClickListener() {
			public void onClick(View v) {
				MusicUtils.removeAllTracks();
				MusicUtils.shuffleAll2(getActivity(), mCursor);
				try {
					MusicUtils.mService
							.setRepeatMode(ApolloService.REPEAT_NONE);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		MusicUtils.removeAllTracks();
		final int pos = position - mListView.getHeaderViewsCount();
		if (pos < 0)
			return;
		mAdapter.play(v);
		parent.post(new Runnable() {

			@Override
			public void run() {
				MusicUtils.playAll(getActivity(), mCursor, pos);

			}
		});
	}

	/**
	 * 屏蔽长按事件
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		return true;
	}
}
