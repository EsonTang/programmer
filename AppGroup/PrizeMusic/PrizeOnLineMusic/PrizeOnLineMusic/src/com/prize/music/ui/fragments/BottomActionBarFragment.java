package com.prize.music.ui.fragments;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.prize.app.util.JLog;
import com.prize.music.IApolloService;
import com.prize.music.R;
import com.prize.music.helpers.utils.CommonClickUtils;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.playview.MusicPlayerView;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.widgets.BottomActionBar;

/**
 * 底部进度条修改
 * 
 * @author Administrator
 *
 */
public class BottomActionBarFragment extends Fragment{

	private ImageButton mNext;
	private MusicPlayerView mPlay;
	private BottomActionBar mBottomActionBar;
	private RelativeLayout bottom_action_bar_dragview;

//	private ServiceToken mToken;

	private static final int REFRESH = 1, UPDATEINFO = 2;

	private boolean paused = false;

	private long mDuration, mPosOverride = -1;
	
	private LinearLayout  bottom_action_bar_container;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.bottom_action_bar, container);
		mBottomActionBar = new BottomActionBar(getActivity());
        
		bottom_action_bar_container = (LinearLayout)root.findViewById(R.id.bottom_action_bar_container);
		bottom_action_bar_dragview = (RelativeLayout) root
				.findViewById(R.id.bottom_action_bar_dragview);

		mPlay = (MusicPlayerView) root
				.findViewById(R.id.bottom_action_bar_play);
		mPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doPauseResume();
			}
		});
        
		mPlay.setMax(1000);
		// try {
		// if (MusicUtils.mService != null && MusicUtils.mService.isPlaying()) {
		// mPlay.start();
		// } else {
		// mPlay.stop();
		// }
		// } catch (RemoteException e) {
		//
		// e.printStackTrace();
		//
		// }
		mNext = (ImageButton) root.findViewById(R.id.bottom_action_bar_next);
		mNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (CommonClickUtils.isFastDoubleClick())
					return;
				if (MusicUtils.mService == null)
					return;
				try {
					MusicUtils.mService.next();
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});
		return root;
	}

	public RelativeLayout getBottom_action_bar_dragview() {
		return bottom_action_bar_dragview;
	}

	/**
	 * Update the list as needed
	 */
	private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateBottomActionBar();

			if (intent.getAction().equals(ApolloService.META_CHANGED)) {
				mHandler.sendMessage(mHandler.obtainMessage(UPDATEINFO));
				// setPauseButtonImage();
				// setRepeatButtonImage();
			}
		}
	};

	/**
	 * We need to refresh the time via a Handler
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH:
				long next = refreshNow();
				queueNextRefresh(next);
				break;
			case UPDATEINFO:
				updateMusicInfo();
				break;
			default:
				break;
			}
		}
	};

	private void updateMusicInfo() {
		if (MusicUtils.mService == null) {
			return;
		}
		mDuration = MusicUtils.getDuration();
	}

	/**
	 * @param delay
	 */
	private void queueNextRefresh(long delay) {
		if (!paused) {
			Message msg = mHandler.obtainMessage(REFRESH);
			mHandler.removeMessages(REFRESH);
			mHandler.sendMessageDelayed(msg, delay);
		}
	}

	public void updateBottomActionBar() {
		if (mBottomActionBar != null && !ActivityManager.isUserAMonkey()) {
			mBottomActionBar.updateBottomActionBar(getActivity());
		}
		setPauseButtonImage();
//		refreshNow();
//		doPauseResume();
	}

	/**
	 * Play and pause music
	 */
	private void doPauseResume() {
		try {
			if (MusicUtils.mService != null) {
				if (MusicUtils.mService.isPlaying()) {
					MusicUtils.mService.pause();
					if (mPlay.isRotating()) {
						mPlay.stop();
						mHandler.removeMessages(REFRESH);
					}
				} else {
					MusicUtils.mService.play();
					if (!mPlay.isRotating()) {
						mPlay.start();
						queueNextRefresh(500);
					}
				}
			}
			// refreshNow();
//			 setPauseButtonImage();
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @return current time
	 */
	private long refreshNow() {
		if (MusicUtils.mService == null)
			return 500;
		try {
			if (MusicUtils.mService != null && MusicUtils.mService.isPlaying()) {
				mPlay.start();
			} else {
				mPlay.stop();
			}
			long pos = mPosOverride < 0 ? MusicUtils.mService.position()
					: mPosOverride;
			if ((pos >= 0) && (mDuration > 0)) {
				mPlay.setProgress((int) (1000 * pos / mDuration));
//				JLog.i("hu", "pos=="+pos+"~~~mDuration=="+mDuration+"~~~setProgress=="+(1000 * pos / mDuration));
			}
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
		return 500;
	}

	/**
	 * Set the play and pause image
	 */
	private void setPauseButtonImage() {
		try {
			if (MusicUtils.mService != null && MusicUtils.mService.isPlaying()) {
				if (!mPlay.isRotating()) {
					mPlay.start();
					JLog.i("hu", "setPauseButtonImage -mPlay.start() ");
					queueNextRefresh(500);
				}
			} else {
				if (mPlay.isRotating()) {
					mPlay.stop();
					mHandler.removeMessages(REFRESH);
				}
			}
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
//		mToken = MusicUtils.bindToService(getActivity(), this);
//		JLog.i("hu", "bottomAction==onStart");
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.PLAYSTATE_CHANGED);
		filter.addAction(ApolloService.META_CHANGED);
		getActivity().registerReceiver(mMediaStatusReceiver, filter);
		long next = refreshNow();
		queueNextRefresh(next);
	}

	@Override
	public void onStop() {
		getActivity().unregisterReceiver(mMediaStatusReceiver);	
//		if (MusicUtils.mService != null)
//			MusicUtils.unbindFromService(mToken);
		super.onStop();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		paused = true;
		if(mPlay !=null){
			mPlay.stop();
		}
//		if (MusicUtils.mService != null)
//			MusicUtils.unbindFromService(mToken);
		mHandler.removeCallbacksAndMessages(null);
//		mHandler.removeMessages(REFRESH);
	}
	
	public void setBackgroud(int Color){
		bottom_action_bar_container.setBackgroundColor(getResources().getColor(Color));
	}

//	@Override
//	public void onServiceConnected(ComponentName arg0, IBinder obj) {
//		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
//	}
//
//	@Override
//	public void onServiceDisconnected(ComponentName arg0) {
//		MusicUtils.mService = null;
//	}
}
