package com.prize.factorytest.Headset;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;
import java.io.IOException;
import android.util.Log;

public class Headset extends Activity {
	private AudioRecord m_record;
	private AudioTrack m_track;
	private int bufferSize;
	private byte[] buffer;
	private AudioManager mAudioManager;
	private Thread mThread;
	boolean flag = false;
	private TextView mTextView;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.headset);
		mTextView = (TextView) findViewById(R.id.headset_hint);
		getService();

	}

	void getService() {
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setSpeakerphoneOn(false);
		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
	}

	void confirmButton(String title, boolean enable) {
		mTextView.setText(title);
		mTextView.setTextSize(20);
		final Button buttonPass = (Button) findViewById(R.id.passButton);
		buttonPass.setEnabled(enable);
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

	private BroadcastReceiver mHeadSetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				int state = intent.getIntExtra("state", -1);
				switch (state) {
				case 0:
					flag = false;
					confirmButton(getString(R.string.headset_insert), false);
					break;
				case 1:
					mThread = new Thread() {
						public void run() {
							flag = true;
							startServer();
						}
					};
					mThread.start();
					confirmButton(getString(R.string.headset_tip), true);
					break;
				default:
					break;
				}

			}
		}

	};

	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		this.registerReceiver(mHeadSetReceiver, filter);
		
		if (!mAudioManager.isWiredHeadsetOn()) {
			confirmButton(getString(R.string.headset_insert), false);
			return;
		} else {
			confirmButton(getString(R.string.headset_tip), true);
		}
		mThread = new Thread() {
			public void run() {
				flag = true;
				startServer();
			}
		};
		mThread.start();
	}

	void startServer() {
		int SAMPLE_RATE = 8000;
		int BUF_SIZE = 1024;

		bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		bufferSize = Math.max(bufferSize, AudioTrack.getMinBufferSize(
				SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT));
		bufferSize = Math.max(bufferSize, BUF_SIZE);

		buffer = new byte[bufferSize];

		m_record = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				bufferSize);

		m_track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				bufferSize, AudioTrack.MODE_STREAM);

		m_track.setPlaybackRate(SAMPLE_RATE);
		m_record.startRecording();
		m_track.play();

		while (flag) {
			int readSize = m_record.read(buffer, 0, bufferSize);
			if (readSize > 0) {
				m_track.write(buffer, 0, readSize);
			}
		}
		try {
			if(m_record != null){
				m_record.stop();
				m_record.release();
				m_record = null;
			}
			if(m_track != null){
				m_track.stop();
				m_track.release();
				m_track = null;
			}
		}catch (IllegalStateException e) {
			Log.e("liup", "m_record,m_track error");
		}
	}

	@Override
	public void finish() {
		flag = false;
		AudioManager mAudioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		super.finish();
	}

	protected void onDestroy() {
		super.onDestroy();
		finish();
	}

	protected void onPause() {
		this.unregisterReceiver(mHeadSetReceiver);
		super.onPause();
		finish();
	}
}
