package com.prize.music.ui.fragments.list;

import java.util.Calendar;

import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.widget.RelativeLayout;

import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.list.SonglistAdapter;
import com.prize.music.ui.fragments.base.SongsListViewFragment;
import com.prize.music.R;
import static com.prize.app.constants.Constants.*;

/**
 * 全部音乐歌曲列表
 * 
 * @author longbaoxiu
 *
 */
public class SongsFragment extends SongsListViewFragment {

	public void setupFragmentData() {
		mAdapter = new SonglistAdapter(getActivity(),
				R.layout.item_songs_layout, null, new String[] {},
				new int[] {}, 0);
		mProjection = new String[] { BaseColumns._ID, MediaColumns.TITLE,
				AudioColumns.ALBUM, AudioColumns.ARTIST };
		StringBuilder where = new StringBuilder();
		where.append(AudioColumns.IS_MUSIC + "=1").append(
				" AND " + MediaColumns.TITLE + " != ''");
		mWhere = where.toString();
		mSortOrder = MediaColumns.TITLE;
		mUri = Audio.Media.EXTERNAL_CONTENT_URI;
		mFragmentGroupId = 3;
		mType = TYPE_SONG;
		mTitleColumn = MediaColumns.TITLE;
		View shuffle_temp = View.inflate(getActivity(),
				R.layout.artist_shuffle_all, null);
		mListView.addHeaderView(shuffle_temp);
		RelativeLayout shuffle = (RelativeLayout) shuffle_temp
				.findViewById(R.id.shuffle_wrapper);
		shuffle.setVisibility(View.VISIBLE);
		shuffle.setOnClickListener(new RelativeLayout.OnClickListener() {
			public void onClick(View v) {
				//prize-add-bug:29635-tangzeming-20170309-start
				currentTime = Calendar.getInstance().getTimeInMillis(); 
				android.util.Log.d("tzm", "songshuffleClick: "+"num="+(currentTime - lastClickTime));
				if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
				lastClickTime = currentTime;	
				//prize-add-bug:29635-tangzeming-20170309-end
				if (isSelectMode) {
					return;
				}
				MusicUtils.shuffleAll2(getActivity(), mCursor);
				try {
					if (MusicUtils.mService != null) {
						MusicUtils.mService
								.setRepeatMode(ApolloService.REPEAT_NONE);
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			  }
			}
		});
	}
	
	//prize-add-bug:29635-tangzeming-20170309-start
	public static final int MIN_CLICK_DELAY_TIME = 1000;
	private long lastClickTime = 0;
	private long currentTime = 0;
	//prize-add-bug:29635-tangzeming-20170309-end

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		// TODO Auto-generated method stub
		super.setUserVisibleHint(isVisibleToUser);
	}
}
