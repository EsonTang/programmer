package com.prize.factorytest.MICRe;

import java.io.File;
import java.io.IOException;
import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.KeyEvent;

public class MICRe extends Activity {
	MediaRecorder mMediaRecorder;
	Button recordButton = null;
	Button stopButton = null;
	AudioManager mAudioManager;
	private int mStatus = 0;
	private Button mBtnTest;
	private Button buttonPass;
	private File mSoundFile;
	private MediaPlayer mMediaPlayer;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.micre);
		getService();
		mBtnTest = (Button) findViewById(R.id.micre_button);
		mBtnTest.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switch (mStatus) {
				case 0: {
					startRecording();
					mBtnTest.setText(R.string.mic_stop_recording);
					mStatus = 1;
					break;
				}
				case 1: {
					stopRecording();
					startPlayback();
					mBtnTest.setText(R.string.mic_stop_playing);
					mStatus = 2;
					break;
				}
				case 2: {
					stopPlayback();
					mBtnTest.setText(R.string.mic_recording);
					buttonPass.setEnabled(true);
					mStatus = 0;
				}
				default :
					return;
				}
			}
		});

		confirmButton();
	}

	private void getService() {
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAudioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setMode(AudioManager.MODE_RINGTONE);
	}

	void startRecording() {
		mSoundFile = new File(getCacheDir(), "MicRecorder.amr");
		try {
			if (mSoundFile.exists()) {
				mSoundFile.delete();
			}
			mSoundFile.createNewFile();
		} catch (IOException e) {
			return;
		}
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		mMediaRecorder.setOutputFile(mSoundFile.getAbsolutePath());
		mMediaRecorder.setAudioEncodingBitRate(128000);
		mMediaRecorder.setAudioSamplingRate(48000);
		try {
			mMediaRecorder.prepare();
		} catch (IOException e) {
			mMediaRecorder.reset();
			mMediaRecorder.release();
			if (mSoundFile != null) {
				mSoundFile.delete();
			}
		}
		try {
			mMediaRecorder.start();
		} catch (IllegalStateException e) {
			mMediaRecorder.reset();
			mMediaRecorder.release();
			if (mSoundFile != null) {
				mSoundFile.delete();
			}
		}

	}

	private void stopRecording() {
		if (mMediaRecorder != null) {
			try {
				mMediaRecorder.reset();
				mMediaRecorder.release();
			}finally{
				mMediaRecorder = null;
			}
		}
	}

	private void startPlayback() {
		mMediaPlayer = new MediaPlayer();
		try {
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(mSoundFile.getAbsolutePath());
			mMediaPlayer.setLooping(true);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (Exception e) {
			Log.e("liup", "startPlayback");
		}
	}

	private void stopPlayback() {
		if (mMediaPlayer != null) {
			try {
				mMediaPlayer.stop();
				mMediaPlayer.release();
			}finally{
				mMediaPlayer = null;
			}
		}
	}

	private BroadcastReceiver mHeadSetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				int state = intent.getIntExtra("state", -1);
				switch (state) {
				case 0:
					mBtnTest.setText(R.string.mic_recording);
					mBtnTest.setEnabled(true);
					break;
				case 1:
					mBtnTest.setText(R.string.remove_headset);
					mBtnTest.setEnabled(false);
					buttonPass.setEnabled(false);
					break;
				default:
					break;
				}

			}
		}

	};

	public void confirmButton() {
		buttonPass = (Button) findViewById(R.id.passButton);
		buttonPass.setEnabled(false);
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		buttonPass.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		buttonFail.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_CANCELED);
				finish();

			}

		});
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(mHeadSetReceiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(null != mHeadSetReceiver){
			unregisterReceiver(mHeadSetReceiver);
		}
	}
	
	@Override
	public void finish() {
		AudioManager mAudioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		if (mMediaRecorder != null) {
			try {
				mMediaRecorder.reset();
				mMediaRecorder.release();
			}finally{
				mMediaRecorder = null;
			}
		}
		if (mMediaPlayer != null) {
			try {
				mMediaPlayer.stop();
				mMediaPlayer.release();
			}finally{
				mMediaPlayer = null;
			}
		}
		super.finish();
	}
}
