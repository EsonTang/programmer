package com.android.soundrecorder;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import java.io.File;
import java.io.IOException;

/**
 * M: we split player from recorder, the player is only responsible for play back
 */
public class Player implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "SR/Player";
    private MediaPlayer mPlayer = null;
    private String mCurrentFilePath = null;
    private PlayerListener mListener = null;

    // M: the listener when error occurs and state changes
    public interface PlayerListener {
        // M: when error occurs, we will notify listener the error code
        void onError(Player player, int errorCode);

        // M: when state changes, we will notify listener the new state code
        void onStateChanged(Player player, int stateCode);
    }

    /**
     * M: Constructor of player, only PlayListener needed
     * @param listener the listener of player
     */
    public Player(PlayerListener listener) {
        mListener = listener;
    }

    @Override
    /**
     * M: the completion callback of MediaPlayer
     */
    public void onCompletion(MediaPlayer player) {
        LogUtils.i(TAG, "<onCompletion>");
        stopPlayback();
    }

    @Override
    /**
     * M: the error callback of MediaPlayer
     */
    public boolean onError(MediaPlayer player, int errorType, int extraCode) {
    	LogUtils.i(TAG, "<onError> errorType=" + errorType + " extraCode=" + extraCode);
        mListener.onError(this, ErrorHandle.ERROR_PLAYING_FAILED);
        return true;
    }

    /**
     * M: set the path of audio file which will play, used by
     * SoundRecorderService
     *
     * @param filePath
     */
    public void setCurrentFilePath(String filePath) {
        mCurrentFilePath = filePath;
    }

    /**
     * M: start play the audio file which is set in setCurrentFilePath
     * @return the result of play back, success or fail
     */
    public boolean startPlayback() {
        if (null == mCurrentFilePath) {
            return false;
        }

        File file = new File(mCurrentFilePath);
        if (!file.exists()) {
            mListener.onError(this, ErrorHandle.ERROR_FILE_DELETED_WHEN_PLAY);
            return false;
        }

        LogUtils.i(TAG, "<startPlayback> mCurrentFilePath= "
                + mCurrentFilePath);
        synchronized (this) {
            if (null == mPlayer) {
                mPlayer = new MediaPlayer();
            } else {
            	try {
                	mPlayer.reset();
                } catch (IllegalStateException e) {
                    return handleException(e);
                }
            }
            try {
                mPlayer.setDataSource(mCurrentFilePath);
                mPlayer.setOnCompletionListener(this);
                mPlayer.prepare();
                mPlayer.start();
                LogUtils.i(TAG, "<startPlayback> The length of recording file is "
                        + mPlayer.getDuration());
                setState(SoundRecorderService.STATE_PLAYING);
            } catch (IllegalStateException e) {
                return handleException(e);
            } catch (IOException e) {
                return handleException(e);
            }
        }
        return true;
    }
    
    public boolean seekTo(int msec) {
    	if (null == mPlayer) {
            return false;
        }
    	try {
    		mPlayer.seekTo(msec);
    		if (!mPlayer.isPlaying()) {
    			mPlayer.start();
    			setState(SoundRecorderService.STATE_PLAYING);
    		}
        } catch (IllegalStateException e) {
            return handleException(e);
        }
    	return true;
    }

    /**
     * M: Handle the Exception when call the function of MediaPlayer
     * @return false
     */
    public boolean handleException(Exception exception) {
        LogUtils.i(TAG, "<handleException>");
        exception.printStackTrace();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        mListener.onError(this, ErrorHandle.ERROR_PLAYING_FAILED);
        return false;
    }

    /**
     * M: pause play the audio file which is set in setCurrentFilePath
     */
    public boolean pausePlayback() {
        if (null == mPlayer) {
            return false;
        }
        try {
            mPlayer.pause();
            setState(SoundRecorderService.STATE_PAUSE_PLAYING);
        } catch (IllegalStateException e) {
            return handleException(e);
        }
        return true;
    }

    /**
     * M: goon play the audio file which is set in setCurrentFilePath
     */
    public boolean goonPlayback() {
        if (null == mPlayer) {
            return false;
        }

        try {
            mPlayer.start();
            setState(SoundRecorderService.STATE_PLAYING);
        } catch (IllegalStateException e) {
            return handleException(e);
        }
        return true;
    }

    /**
     * M: stop play the audio file which is set in setCurrentFilePath
     */
    public boolean stopPlayback() {
        // we were not in playback
        synchronized (this) {
            if (null == mPlayer) {
                return false;
            }
            try {
                mPlayer.stop();
                setState(SoundRecorderService.STATE_IDLE);
            } catch (IllegalStateException e) {
                return handleException(e);
            }
            mPlayer.release();
            mPlayer = null;
        }
        return true;
    }

    /**
     * M: reset Player to initial state
     */
    public void reset() {
        synchronized (this) {
            if (null != mPlayer) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
        }
        // prize-cancel mCurrentFilePath clearing-liguizeng-2015-4-21
//        mCurrentFilePath = null;
    }

    /**
     * M: get the current position of audio which is playing
     * @return the current position in millseconds
     */
    public int getCurrentProgress() {
        if (null != mPlayer) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * M: get the duration of audio file
     * @return the duration in millseconds
     */
    public int getFileDuration() {
        if (null != mPlayer) {
            return mPlayer.getDuration();
        }
        return 0;
    }

    private void setState(int state) {
        mListener.onStateChanged(this, state);
    }
}
