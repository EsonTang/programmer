package com.prize.factorytest.HeadsetRe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;

public class HeadsetRe extends Activity {

	String mAudiofilePath;
	static String TAG = "Headset";
	MediaRecorder mMediaRecorder = new MediaRecorder();
	boolean isRecording = false;
	Button recordButton = null;
	Button stopButton = null;
	AudioManager mAudioManager;
	Context mContext;
	private final Timer timer = new Timer();
	private boolean isstopButton = false;

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
		setContentView(R.layout.headsetre);

		mContext = this;
		isRecording = false;

		getService();
		if (!mAudioManager.isWiredHeadsetOn()) {
			showWarningDialog(getString(R.string.insert_headset));
		} else {
			bindView();
			setAudio();
		}

	}

	@Override
	public void finish() {
		AudioManager mAudioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		super.finish();
	}

	void record() throws IllegalStateException, IOException,
			InterruptedException {

		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		mMediaRecorder.setOutputFile(this.getCacheDir().getAbsolutePath()
				+ "/test.aac");
		mAudiofilePath = this.getCacheDir().getAbsolutePath() + "/test.aac";
		mMediaRecorder.prepare();
		mMediaRecorder.start();
	}

	@SuppressWarnings("resource")
	void replay() throws IllegalArgumentException, IllegalStateException,
			IOException {
		final TextView mTextView = (TextView) findViewById(R.id.headsetre_hint);
		mTextView.setText(getString(R.string.headset_playing));
		stopButton.setClickable(false);
		File file = new File(mAudiofilePath);
		FileInputStream mFileInputStream = new FileInputStream(file);
		final MediaPlayer mMediaPlayer = new MediaPlayer();

		mMediaPlayer.reset();
		mMediaPlayer.setDataSource(mFileInputStream.getFD());
		mMediaPlayer.prepare();
		mMediaPlayer.start();
		showDialog();
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mPlayer) {
				mPlayer.stop();
				mPlayer.release();
				File file = new File(mAudiofilePath);
				file.delete();

				final TextView mTextView = (TextView) findViewById(R.id.headsetre_hint);
				mTextView.setText(getString(R.string.headset_replay_end));

			}
		});

	}

	void showWarningDialog(String title) {

		new AlertDialog.Builder(mContext)
				.setCancelable(false)
				.setTitle(title)
				.setPositiveButton(getString(R.string.ok),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								setResult(3);
								finish();
							}
						}).show();

	}

	public void showDialog() {

		new AlertDialog.Builder(mContext).setCancelable(false)
				.setTitle(R.string.headset_confirm).setPositiveButton

				(R.string.yes, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialoginterface, int i) {
						if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
							PrizeFactoryTestListActivity.itempos++;
						}
						setResult(RESULT_OK);
						finish();
					}
				}).setNeutralButton

				(R.string.no, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialoginterface, int i) {
						if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
							PrizeFactoryTestListActivity.itempos++;
						}
						setResult(RESULT_CANCELED);
						finish();
					}
				}).setNegativeButton

				(R.string.again, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialoginterface, int i) {
						setResult(3);
						finish();
					}
				}).show();
	}

	public void setAudio() {

		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		float ratio = 0.8f;

		mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_ALARM)), 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
		mAudioManager
				.setStreamVolume(
						AudioManager.STREAM_VOICE_CALL,
						(int) (ratio * mAudioManager
								.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)),
						0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_DTMF,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_DTMF)), 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)),
				0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_RING)), 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)), 0);
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (false == isstopButton) {
				if (isRecording) {
					mMediaRecorder.stop();
					mMediaRecorder.release();

					try {
						replay();
					} catch (Exception e) {
					}
				} else {
					showWarningDialog(getString(R.string.transmitter_receiver_record_first));
				}
			}
			super.handleMessage(msg);
		}
	};

	TimerTask task = new TimerTask() {
		public void run() {
			Message message = new Message();
			message.what = 1;
			handler.sendMessage(message);
		}
	};

	@SuppressWarnings("deprecation")
	void bindView() {
		stopButton = (Button) findViewById(R.id.headsetre_stop);
		final TextView mTextView = (TextView) findViewById(R.id.headsetre_hint);
		mTextView.setText(getString(R.string.headset_to_record));
		if (mAudioManager.isWiredHeadsetOn()) {
			try {
				record();
				isRecording = true;

			} catch (Exception e) {
			}
		} else {
			showWarningDialog(getString(R.string.insert_headset));
		}

		timer.schedule(task, 5000);
		stopButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if (isRecording) {
					mMediaRecorder.stop();
					mMediaRecorder.release();
					isstopButton = true;
					try {
						handler.removeCallbacks(task);
						replay();
					} catch (Exception e) {
					}
				} else
					showWarningDialog(getString(R.string.headset_record_first));
			}
		});
	}

	void getService() {
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	void fail(Object msg) {
		toast(msg);
		setResult(RESULT_CANCELED);
		finish();
	}

	void pass() {

		setResult(RESULT_OK);
		finish();
	}

	public void toast(Object s) {

		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}
}
