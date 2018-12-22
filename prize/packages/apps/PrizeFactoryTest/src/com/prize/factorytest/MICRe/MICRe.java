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
import android.media.AudioSystem;
import android.os.SystemProperties;
import android.os.Handler;
import android.widget.TextView;
import java.util.ArrayList;

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
	private TextView mDBView;

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
					//if(mDBList.size() > 0 && mSunDB / mDBList.size() > MIN_DB){
						buttonPass.setEnabled(true);
					//}
					mStatus = 0;
				}
				default :
					return;
				}
			}
		});
		
		mDBView = (TextView) findViewById(R.id.db_text);
		

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
			mDBList.clear();
			mSunDB = 0;
			mHandler.postDelayed(mUpdateMicStatusTimer, 500); 
		} catch (IllegalStateException e) {
			mMediaRecorder.reset();
			mMediaRecorder.release();
			if (mSoundFile != null) {
				mSoundFile.delete();
			}
		}

	}

  private final Handler mHandler = new Handler();  
    private Runnable mUpdateMicStatusTimer = new Runnable() {  
        public void run() {  
            updateMicStatus();  
        }  
    };  
	
    private int BASE = 1;  
    private int SPACE = 300;
	private int MIN_DB = 70;
	private ArrayList<Double> mDBList = new ArrayList<Double>();;
	private double mSunDB; 
	
    private void updateMicStatus() {  
        if (mMediaRecorder != null) {  
            int maxAmplitude = mMediaRecorder.getMaxAmplitude();
            double ratio = (double) maxAmplitude / BASE;  
			Log.d("MICRe","getMaxAmplitude()" + maxAmplitude); 
            double db = 0;
            if (ratio > 1) {  
                db = 20 * Math.log10(ratio);  
            	Log.d("MICRe","db = " + db);  
				mDBList.add(db);
				mSunDB +=db; 
				mDBView.setText(String.format("%.2f,%d,%.2f" , db , mDBList.size() , mSunDB / mDBList.size()));
				if(mSunDB / mDBList.size() > MIN_DB && mDBList.size() > 10){
					//buttonPass.setEnabled(true);
				}
			}
            mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);  
        }  
    }  
	
	
	private void stopRecording() {
		mHandler.removeCallbacks(mUpdateMicStatusTimer);
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
		if(!SystemProperties.get("ro.mtk_dual_mic_support").equals("0")) { 
			AudioSystem.setParameters("SET_MIC_CHOOSE=1");//close main mic
			Log.e("wuliang", "wuliang SET_MIC_CHOOSE=1");	
		}
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(mHeadSetReceiver, filter);
	}
	
	protected void onPause() {
		super.onPause();
		if(null != mHeadSetReceiver){
			unregisterReceiver(mHeadSetReceiver);
			mHeadSetReceiver = null;
		}
		
		if(!SystemProperties.get("ro.mtk_dual_mic_support").equals("0")) { 
			AudioSystem.setParameters("SET_MIC_CHOOSE=0");
			Log.e("wuliang", "wuliang SET_MIC_CHOOSE=0");	
		}
	}

	
	@Override
	public void finish() {
		AudioManager mAudioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		stopRecording();
		stopPlayback();
		super.finish();
	}
}
