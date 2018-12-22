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

import static com.prize.music.Constants.TYPE_SONG;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.widget.AdapterView;

import com.prize.music.R;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.ui.adapters.list.SonglistAdapter;
import com.prize.music.ui.fragments.base.PlayingListViewFragment;

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
			mAdapter = new SonglistAdapter(getActivity(),
					R.layout.listview_items, null, new String[] {},
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

}
