package com.prize.music.ui.fragments.list;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.View;
import android.widget.RelativeLayout;

import com.prize.music.R;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.history.HistoryColumns;
import com.prize.music.history.HistoryDao;
import com.prize.music.service.ApolloService;
import com.prize.music.ui.adapters.list.RecentlyPlayAdapter;
import com.prize.music.ui.fragments.base.RecentPlayTopTitleListViewFragment;

/**
 * 最近播放
 * 
 * @author Administrator
 *
 */
public class RecentlyPlayFragment extends RecentPlayTopTitleListViewFragment {
	private ContentObserver observer = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			refresh();
		}

	};

	public void setupFragmentData() {
		// mCursor = HistoryDao.getInstance(getActivity()).queryWithCursor();
		View shuffle_temp = View.inflate(getActivity(), R.layout.shuffle_all,
				null);
		mListView.addHeaderView(shuffle_temp);
		RelativeLayout shuffle = (RelativeLayout) shuffle_temp
				.findViewById(R.id.shuffle_wrapper);
		shuffle.setVisibility(View.VISIBLE);
		shuffle.setOnClickListener(new RelativeLayout.OnClickListener() {
			public void onClick(View v) {
				if (isSelectMode) {
					return;
				}
				MusicUtils.shuffleAll2(getActivity(), mCursor);
				try {
					MusicUtils.mService
							.setRepeatMode(ApolloService.REPEAT_NONE);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});

		isSongsInDataBase();
		mAdapter = new RecentlyPlayAdapter(getActivity(),
				R.layout.listview_items, mCursor, new String[] {},
				new int[] {}, 0);

	}

	private void isSongsInDataBase() {
		Cursor mAllCursor = HistoryDao.getInstance(getActivity())
				.queryWithCursor();
		long[] audioIds = null;
		if (mAllCursor != null && mAllCursor.getCount() > 0) {
			audioIds = new long[mAllCursor.getCount()];
			int i = 0;
			while (mAllCursor.moveToNext()) {
				int index = mAllCursor.getColumnIndex(HistoryColumns.AUDIO_ID);
				if (index != -1) {
					audioIds[i] = mAllCursor.getLong(index);
					i++;
				}
			}
		}
		if (getActivity() != null) {
			mCursor = MusicUtils.getAudioIDs(getActivity(), audioIds);
		}
	}

	@Override
	public void onDestroy() {
		if (getActivity() != null && observer != null) {
			getActivity().getContentResolver().unregisterContentObserver(
					observer);
		}
		super.onDestroy();
	}

	@Override
	public void refresh() {
		// mCursor = HistoryDao.getInstance(getActivity()).queryWithCursor();
		isSongsInDataBase();
		mAdapter.changeCursor(mCursor);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		getActivity().getContentResolver().registerContentObserver(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		// startWatchingExternalStorage();
	}

	// private void startWatchingExternalStorage() {
	// IntentFilter intentFilter = new IntentFilter();
	// intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
	// intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
	// intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
	// intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
	// intentFilter.setPriority(1000);
	// intentFilter.addDataScheme("file");
	// getActivity().registerReceiver(mExternalStorageReceiver, intentFilter);
	// }

	// private BroadcastReceiver mExternalStorageReceiver = new
	// BroadcastReceiver() {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)
	// || intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)
	// || intent.getAction().equals(
	// Intent.ACTION_MEDIA_BAD_REMOVAL)) {
	// // SD卡移除，设置列表为空
	//
	// } else if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
	// // SD卡正常挂载,重新加载数据
	//
	// }
	//
	// }
	// };

	@Override
	public void onStop() {
		super.onStop();

	}
}
