package com.prize.autotest.audio;

import com.prize.autotest.AutoConstant;
import com.prize.autotest.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

//import android.widget.Toast;

public class AutoAudioTestActivity extends Activity implements OnClickListener {
	private static final String AUDIO_MIC_CMD = "1,2";
	private static final String AUDIO_MIC_CMD_SUB = "3,2";
	private static final String AUDIO_PLAYER_CMD = "2,3";
	private static final String AUDIO_RECEIVER_CMD = "2,1";
	private static final String AUDIO_HEADSET_CMD = "2,2";

	private BroadcastReceiver mbr = null;
	private String cmdOrder = null;

	private AudioManager mAudioManager;

	private Button btn_mic;
	private Button btn_player;
	private Button btn_receiver;
	private Button btn_headset;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_test_activity);
		Intent intent = getIntent();
		cmdOrder = intent.getStringExtra("back");

		this.mbr = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AutoConstant.ACTION_UI);
		registerReceiver(this.mbr, filter);

		mAudioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);

		btn_mic = (Button) findViewById(R.id.button_audio_mic);
		btn_mic.setOnClickListener(this);
		btn_player = (Button) findViewById(R.id.button_audio_player);
		btn_player.setOnClickListener(this);
		btn_receiver = (Button) findViewById(R.id.button_audio_receiver);
		btn_receiver.setOnClickListener(this);
		btn_headset = (Button) findViewById(R.id.button_audio_headset);
		btn_headset.setOnClickListener(this);
		if (cmdOrder != null) {
			new Handler().postDelayed(new Runnable() {
				public void run() {
					runCmdOrder();
				}
			}, 300);
		}
	}

	public void onClick(View v) {
		mAudioManager.setParameter("SET_LOOPBACK_TYPE", "0");
		switch (v.getId()) {
		case R.id.button_audio_mic:
			mAudioManager.setParameter("SET_LOOPBACK_TYPE", AUDIO_MIC_CMD);
			break;
		case R.id.button_audio_player:
			mAudioManager.setParameter("SET_LOOPBACK_TYPE", AUDIO_PLAYER_CMD);
			break;
		case R.id.button_audio_receiver:
			mAudioManager.setParameter("SET_LOOPBACK_TYPE", AUDIO_RECEIVER_CMD);
			break;
		case R.id.button_audio_headset:
			mAudioManager.setParameter("SET_LOOPBACK_TYPE", AUDIO_HEADSET_CMD);
			break;
		default:
			break;
		}
	}

	private void runCmdOrder() {
		if (cmdOrder == null || cmdOrder.length() < 1) {
			return;
		}
		setAudio();
		String temp = cmdOrder.substring(1);
		mAudioManager.setParameter("SET_LOOPBACK_TYPE", "0");
		if (temp.startsWith(AutoConstant.CMD_AUDIO_MIC)) {
			mAudioManager.setParameter("SET_LOOPBACK_TYPE", AUDIO_MIC_CMD);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
		} else if (temp.startsWith(AutoConstant.CMD_AUDIO_MIC_SUB)) {
			mAudioManager.setParameter("SET_LOOPBACK_TYPE", AUDIO_MIC_CMD_SUB);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
		} else if (temp.startsWith(AutoConstant.CMD_AUDIO_PLAYER)) {
			SystemProperties.set("sys.audio.leftspk.mute", "1");
			mAudioManager.setParameter("SET_LOOPBACK_TYPE", AUDIO_PLAYER_CMD);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
		} else if (temp.startsWith(AutoConstant.CMD_AUDIO_RECEIVER)) {
			mAudioManager.setParameter("SET_LOOPBACK_TYPE", AUDIO_RECEIVER_CMD);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
		} else if (temp.startsWith(AutoConstant.CMD_AUDIO_HEADSET)) {
			mAudioManager.setParameter("SET_LOOPBACK_TYPE", AUDIO_HEADSET_CMD);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
		} else if (temp.startsWith(AutoConstant.CMD_AUDIO_FINISH)) {
			SystemProperties.set("sys.audio.leftspk.mute", "0");
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
			finish();
		} else if (temp.startsWith(AutoConstant.CMD_AUDIO_SUCCESS)) {
			AutoConstant.writeProInfo("P", 40);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
			finish();
		} else if (temp.startsWith(AutoConstant.CMD_AUDIO_FAIL)) {
			AutoConstant.writeProInfo("F", 40);
			AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS, this);
			finish();
		}
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				runCmdOrder();
			}
		}
	}
	
	public void setAudio() {
		Log.e("liup", "setAudio()");
		Log.e("liup", "Default STREAM_ALARM = " + AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_ALARM) 
					+ "Default STREAM_MUSIC = " + AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_MUSIC)
					+ "Default STREAM_VOICE_CALL = " + AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_VOICE_CALL)
					+ "Default STREAM_DTMF = " + AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_DTMF)
					+ "Default STREAM_NOTIFICATION = " + AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_NOTIFICATION)
					+ "Default STREAM_SYSTEM = " + AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_SYSTEM)
					+ "Default STREAM_RING = " + AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_RING));
		setDefaultVolume(AudioManager.STREAM_ALARM, 
				AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_ALARM));
		setDefaultVolume(AudioManager.STREAM_MUSIC, 
				AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_MUSIC));
		setDefaultVolume(AudioManager.STREAM_VOICE_CALL,
				AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_VOICE_CALL));
		setDefaultVolume(AudioManager.STREAM_DTMF,
				AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_DTMF));
		setDefaultVolume(AudioManager.STREAM_NOTIFICATION,
				AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_NOTIFICATION));
		setDefaultVolume(AudioManager.STREAM_SYSTEM,
				AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_SYSTEM));
		setDefaultVolume(AudioManager.STREAM_RING,
				AudioSystem.getDefaultStreamVolume(AudioManager.STREAM_RING));
	}
	
    public void setDefaultVolume(final int streamType, final int volume) {
        // mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 0,
        // AudioManager.FLAG_SHOW_UI);
        new Thread(new Runnable() {
            public void run() {
                mAudioManager.setAudioProfileStreamVolume(streamType, volume, 0);
            }
        }, "setVolume").start();
	}

}
