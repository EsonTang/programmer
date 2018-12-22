package com.prize.lockscreen.widget;

import com.prize.lockscreen.utils.LogUtil;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.prizelockscreen.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.media.RemoteController.MetadataEditor;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class MusicControlView extends FrameLayout {
	
	private final static String TAG = MusicControlView.class.getName();
	private Context mContext;
	private AudioManager mAudioManager;
	private RemoteController mRemoteController;
	private RemoteController.OnClientUpdateListener mClientUpdateListener;
	private Metadata mMetadata;
	private int mCurrentPlayState;
	
	private ImageView mImageIcon;
    private TextView mTitle;
    private TextView mTime;
    private ImageView mBtnPrev;
    private ImageView mBtnPlay;
    private ImageView mBtnNext;
    private SeekBar mSeekBar;
    protected static final long QUIESCENT_PLAYBACK_FACTOR = 1000;
    
    private String mTotalTime="00:00";
    
    private final UpdateSeekBarRunnable mUpdateSeekBars = new UpdateSeekBarRunnable();
    
	public MusicControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}
	
	private void init() {
		LogUtil.d(TAG,"------->init()");
		mMetadata = new Metadata();
		mAudioManager = ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE));
		mCurrentPlayState = 0;
		mClientUpdateListener = new RemoteController.OnClientUpdateListener() {

			@Override
			public void onClientTransportControlUpdate(int transportControlFlags) {
				LogUtil.d(TAG,"------->onClientPlaybackStateUpdate  transportControlFlags = "
								+ transportControlFlags);
			}

			@Override
			public void onClientPlaybackStateUpdate(int state,
					long stateChangeTimeMs, long currentPosMs, float speed) {
	            updatePlayPauseState(state);
				LogUtil.d(TAG, "------->onClientPlaybackStateUpdate(state=" + state
						+ ", stateChangeTimeMs=" + stateChangeTimeMs
						+ ", currentPosMs=" + currentPosMs + ", speed=" + speed
						+ ")");
				
				removeCallbacks(mUpdateSeekBars);
				if(mCurrentPlayState==RemoteControlClient.PLAYSTATE_PLAYING){
					mSeekBar.setProgress(0);
					postDelayed(mUpdateSeekBars, QUIESCENT_PLAYBACK_FACTOR);
				}
			}

			@Override
			public void onClientPlaybackStateUpdate(int state) {
				LogUtil.d(TAG, "------->onClientPlaybackStateUpdate  state = "
						+ state);
				 updatePlayPauseState(state);

			}

			@Override
			public void onClientMetadataUpdate(MetadataEditor metadataEditor) {
				updateMetadata(metadataEditor);
			}

			@Override
			public void onClientChange(boolean clearing) {
	            if (clearing) {
	                clearMetadata();
	            }
			}
		};
		mRemoteController = new RemoteController(mContext,mClientUpdateListener);
        final DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        final int dim = Math.max(dm.widthPixels, dm.heightPixels);
        mRemoteController.setArtworkConfiguration(dim, dim);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mMetadata.clear();
        mAudioManager.registerRemoteController(mRemoteController);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mAudioManager.unregisterRemoteController(mRemoteController);
	    mMetadata.clear();
	    removeCallbacks(mUpdateSeekBars);
	}
	
	void updateMetadata(RemoteController.MetadataEditor data) {
		mMetadata.artist = data.getString(
				MediaMetadataRetriever.METADATA_KEY_ARTIST,
				mMetadata.artist);
		mMetadata.trackTitle = data
				.getString(MediaMetadataRetriever.METADATA_KEY_TITLE,
						mMetadata.trackTitle);
		mMetadata.albumTitle = data
				.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM,
						mMetadata.albumTitle);
		mMetadata.duration = data.getLong(
				MediaMetadataRetriever.METADATA_KEY_DURATION, -1);
		mMetadata.bitmap = data.getBitmap(
				MediaMetadataEditor.BITMAP_KEY_ARTWORK, mMetadata.bitmap);
		populateMetadata();
	}
	
    void clearMetadata() {
        mMetadata.clear();
        populateMetadata();
    }
    
	private void populateMetadata() {
		LogUtil.d(TAG, "----->mMetadata = "+mMetadata.toString());
		mImageIcon.setImageBitmap(mMetadata.bitmap);
		mTitle.setText(mMetadata.trackTitle+" - "+mMetadata.artist);
		if (mMetadata.duration > 0) {
			mTotalTime = TimeUtil.millisecondToMM(mMetadata.duration);
		}
		mSeekBar.setProgress(0);
		mTime.setText("00:00/" + mTotalTime);
        if (mMetadata.duration >= 0) {
            setSeekBarDuration(mMetadata.duration);}
	}
	
    void setSeekBarDuration(long duration) {
    	mSeekBar.setMax((int) duration);
    }
	
    private class UpdateSeekBarRunnable implements  Runnable {
    	
        public void run() {
            boolean seekAble = updateOnce();
            if (seekAble) {
                removeCallbacks(this);
                postDelayed(this, 1000);
            }
        }
        public boolean updateOnce() {
            return updateSeekBars();
        }
    };
    
    boolean updateSeekBars() {
        final int position = (int) mRemoteController.getEstimatedMediaPosition();
        if (position >= 0) {
        	mSeekBar.setProgress(position);
        	String currentTime="00:00";
    		if (mMetadata.duration > 0) {
    			currentTime = TimeUtil.millisecondToMM(position);
    		}
    		mTime.setText(currentTime+"/" + mTotalTime);
            return true;
        }
        return false;
    }
    
	private void updatePlayPauseState(int state) {
		LogUtil.d(TAG, "------>updatePlayPauseState(), old = " + mCurrentPlayState + ", state = " + state);
        if (state == mCurrentPlayState) {
            return;
        }
        switch (state) {
            case RemoteControlClient.PLAYSTATE_ERROR:
                break;
            case RemoteControlClient.PLAYSTATE_PLAYING:
            	mBtnPlay.setBackground(mContext.getResources().getDrawable(R.drawable.btn_pause_selector));
            	removeCallbacks(mUpdateSeekBars);
            	postDelayed(mUpdateSeekBars, QUIESCENT_PLAYBACK_FACTOR);
                break;
            case RemoteControlClient.PLAYSTATE_BUFFERING:
                break;
            case RemoteControlClient.PLAYSTATE_PAUSED:
            	mBtnPlay.setBackground(mContext.getResources().getDrawable(R.drawable.btn_play_selector));
            	removeCallbacks(mUpdateSeekBars);
            default:
                break;
        }
        mCurrentPlayState = state;
	}

	
	
	private class Metadata {
		private String artist;
		private String trackTitle;
		private String albumTitle;
		private Bitmap bitmap;
		private long duration;

		public void clear() {
			artist = null;
			trackTitle = null;
			albumTitle = null;
			bitmap = null;
			duration = -1;
		}

		public String toString() {
			return "Metadata[artist=" + artist + " trackTitle=" + trackTitle
					+ " albumTitle=" + albumTitle + " duration=" + duration
					+ "]";
		}
	}
	
	
	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		mImageIcon=(ImageView) findViewById(R.id.music_icon);
	    mTitle=(TextView) findViewById(R.id.music_title);
	    mTime=(TextView) findViewById(R.id.music_time);
        mBtnPrev = (ImageView) findViewById(R.id.btn_play_prev);
        mBtnPlay = (ImageView) findViewById(R.id.btn_play);
        mBtnNext = (ImageView) findViewById(R.id.btn_play_next);
        mSeekBar=(SeekBar) findViewById(R.id.music_seekBar);
        final View buttons[] = { mBtnPrev, mBtnPlay, mBtnNext };
        for (View view : buttons) {
            view.setOnClickListener(mTransportCommandListener);
        }
	}
	
    private final OnClickListener mTransportCommandListener = new OnClickListener() {
        public void onClick(View v) {
            int keyCode = -1;
            if (v == mBtnPrev) {
                keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
            } else if (v == mBtnNext) {
                keyCode = KeyEvent.KEYCODE_MEDIA_NEXT;
            } else if (v == mBtnPlay) {
                keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
				if (mCurrentPlayState == RemoteControlClient.PLAYSTATE_PAUSED) {
					mBtnPlay.setBackground(mContext.getResources().getDrawable(R.drawable.btn_pause_selector));
				} else if (mCurrentPlayState == RemoteControlClient.PLAYSTATE_PLAYING) {
					mBtnPlay.setBackground(mContext.getResources().getDrawable(R.drawable.btn_play_selector));
				}
            }
            if (keyCode != -1) {
                sendMediaButtonClick(keyCode);
            }
        }
    };

    private void sendMediaButtonClick(int keyCode) {
        mRemoteController.sendMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        mRemoteController.sendMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
    }
    
}
