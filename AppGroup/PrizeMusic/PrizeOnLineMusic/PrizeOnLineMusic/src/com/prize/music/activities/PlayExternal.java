package com.prize.music.activities;

import java.io.File;
import java.net.URLDecoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.music.IApolloService;
import com.prize.music.R;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.service.ServiceToken;
import com.prize.music.utils.GetPathFromUri;

/**
 * An activity that lets external browsers launching music inside Apollo
 */
public class PlayExternal extends Activity implements ServiceConnection,
		DialogInterface.OnCancelListener {

	private static final String TAG = "PlayExternal";

	private ServiceToken mToken;
	private Uri mUri;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the external file to play
		Intent intent = getIntent();
		if (intent == null) {
			finish();
			return;
		}
//		mUri = intent.getData();
		mUri = Uri.fromFile(new File(GetPathFromUri.getPath(this, intent.getData()))); 
		JLog.i("555555", "mUri=" + mUri);
		if (mUri == null) {
			finish();
			return;
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
		JLog.i("555555", "onServiceDisconnected");
		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
		play(this.mUri);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {

		MusicUtils.mService = null;
	}

	@Override
	protected void onStart() {
		// Bind to Service
		mToken = MusicUtils.bindToService(this, this);
		super.onStart();
	}

	@Override
	protected void onStop() {
		// Unbind
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
	 if (MusicUtils.mService != null)
		MusicUtils.unbindFromService(mToken);
		if(mHandler !=null){
			mHandler.removeCallbacksAndMessages(null);
		}
		super.onDestroy();
	}

	private void sendBroadCasetToUpdateFile(Uri uri) {
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
		JLog.i("555555", "have send scanfile broadcast");
	}

	private void play(Uri uri) {
		try {
			final String file = URLDecoder.decode(uri.toString(), "UTF-8");
			final String name = new File(file).getName();
			JLog.i("555555", "play-file name is:" + mUri.toString());
			// Try to resolve the file to a media id
			long id = MusicUtils.mService.getIdFromPath(file);
			JLog.i("555555", "play-file id :" + id);
			if (id == -1) {
				// Open the stream, But we will not have album information
				if (name.endsWith(".mp3")) {
					sendBroadCasetToUpdateFile(uri);
				}
//				 Thread.sleep(2000);
//				long ida = MusicUtils.mService.getIdFromPath(file);
//				playOrEnqueuFile(file, ida, false);
				mHandler.removeCallbacksAndMessages(null);
				mHandler.postAtTime(new Runnable() {

					@Override
					public void run() {
						long ida = 0;
						try {
							ida = MusicUtils.mService.getIdFromPath(file);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						JLog.i("555555", "file ida :" + ida);
						playOrEnqueuFile(file, ida, false);

					}
				}, 2000);
			} else {
				playOrEnqueuFile(file, id, false);

				// // Show a dialog asking the user for play or queue the song
				/*
				 * AlertDialog.Builder builder = new AlertDialog.Builder(this,
				 * R.style.Theme_Light_Translucent_Dialog);
				 * builder.setTitle(R.string.app_name);
				 * builder.setMessage(getString(
				 * R.string.play_external_question_msg, name));
				 * 
				 * DialogInterface.OnClickListener listener = new
				 * DialogInterface.OnClickListener() {
				 * 
				 * @Override public void onClick(DialogInterface dialog, int
				 * which) { try { switch (which) { case
				 * DialogInterface.BUTTON_POSITIVE: playOrEnqueuFile(file, id,
				 * false); break;
				 * 
				 * case DialogInterface.BUTTON_NEUTRAL: playOrEnqueuFile(file,
				 * id, true); break;
				 * 
				 * case DialogInterface.BUTTON_NEGATIVE: break;
				 * 
				 * default: break; } } finally { finish(); } } };
				 * builder.setPositiveButton(
				 * R.string.play_external_question_button_play, listener);
				 * builder.setNeutralButton(
				 * R.string.play_external_question_button_queue, listener);
				 * builder.setNegativeButton(
				 * R.string.play_external_question_button_cancel, listener);
				 * 
				 * Dialog dialog = builder.create();
				 * dialog.setOnCancelListener(this); dialog.show();
				 */
			}

		} catch (Exception e) {
			Log.e("LK","无法播放音乐文件");
			Toast.makeText(getApplicationContext(),
					R.string.play_external_error, Toast.LENGTH_SHORT).show();
			try {
				Thread.sleep(1000L);
			} catch (Exception e2) {
			}
			finish();
		}

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		finish();
	}

	private void playOrEnqueuFile(String file, long id, boolean enqueue) {
		JLog.i("555555", "playOrEnqueuFile--" + file + "----");
		if (id < 0) {
			ToastUtils.showToast(R.string.play_external_error);
			finish();
			return;
		}
		final long[] list = new long[] { id };
		if (!enqueue) {
			// Remove the actual queue
			MusicUtils.removeAllTracks();
			MusicUtils.playAll(getApplicationContext(), list, 0);
		} else {
			MusicUtils.addToCurrentPlaylist(getApplicationContext(), list);
		}

		// Show now playing
		Intent intent = new Intent(this, AudioPlayerActivity.class);
		intent.putExtra("started_from", "NOTIF_SERVICE");  
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private void openFile(String file) throws RemoteException {
		// Stop, load and play
		MusicUtils.mService.stop();
		MusicUtils.mService.openFile(file);
		MusicUtils.mService.play();

		// Show now playing
		Intent nowPlayingIntent = new Intent(this, AudioPlayerActivity.class)
				.putExtra("started_from", "NOTIF_SERVICE");
		startActivity(nowPlayingIntent);
	}

}
