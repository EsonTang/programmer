package com.android.prize;

import java.io.IOException;

import com.android.camera.CameraActivity;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.util.Log;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class DoubleCaptureSound {
	private static final String TAG = "DoubleCaptureSound";
	private AssetFileDescriptor fileDescriptor;
	private MediaPlayer myMediaPlayer;
	private Activity mActivity;

	public DoubleCaptureSound(Activity mActivity) {
		Log.i(TAG, "[DoubleCaptureSound]constructor...");
		this.mActivity = mActivity;
		load();
	}

	public void load() {
		Log.i(TAG, "[load]");
		try {
			fileDescriptor = mActivity.getAssets().openFd("double_shutter.ogg");
			myMediaPlayer = new MediaPlayer();
			myMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			myMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),

			fileDescriptor.getStartOffset(),

			fileDescriptor.getLength());
			myMediaPlayer.prepare();
		} catch (Exception e) {
			Log.i(TAG, "load Exception :" + e.toString());
			e.printStackTrace();
		}
	}

	public void play() {
		try {
			myMediaPlayer.start();
		} catch (Exception e) {
			Log.i(TAG, "play Exception :" + e.toString());
			e.printStackTrace();
		}
	}

	public void stop() {
		
	}

	public void release() {
		
	}

	protected boolean isCameraUnMute() {
		String value = ((CameraActivity) mActivity).getISettingCtrl()
				.getListPreference(SettingConstants.KEY_CAMERA_MUTE).getValue();
		Log.i(TAG, "isCameraUnMute() value=" + value);
		boolean isUnMute = "on".equals(value) ? false : true;
		return isUnMute;
	}
}
