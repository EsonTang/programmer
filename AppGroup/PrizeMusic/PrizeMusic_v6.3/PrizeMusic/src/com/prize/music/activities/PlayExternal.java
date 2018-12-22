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
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.prize.music.IApolloService;
import com.prize.music.R;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.service.ServiceToken;

/**
 * An activity that lets external browsers launching music inside Apollo
 */
public class PlayExternal extends Activity implements ServiceConnection,
		DialogInterface.OnCancelListener {

	private static final String TAG = "PlayExternal";

	private ServiceToken mToken;
	private Uri mUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the external file to play
		Intent intent = getIntent();
		if (intent == null) {
			finish();
			return;
		}
		mUri = intent.getData();
		if (mUri == null) {
			finish();
			return;
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
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
	
	private void sendBroadCasetToUpdateFile(Uri uri){
	    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
	    Log.d(TAG,"have send scanfile broadcast");
	}
   
	private void play(Uri uri) {
		try {
			final String file = URLDecoder.decode(uri.toString(), "UTF-8");
			final String name = new File(file).getName();
			Log.d(TAG,"file name is:" + mUri.toString());
			// Try to resolve the file to a media id
			long id = MusicUtils.mService.getIdFromPath(file);
			Log.d(TAG,"file id is:" + id);
			if (id == -1) {
				// Open the stream, But we will not have album information
				if(name.endsWith(".mp3")){
					sendBroadCasetToUpdateFile(uri);
				}
				Thread.sleep(2000);
				long ida = MusicUtils.mService.getIdFromPath(file);
				Log.d(TAG,"file ida is:" + ida);
				playOrEnqueuFile(file, ida, false);
			} else {
				Log.d(TAG,"file id is not -----> 0");
				playOrEnqueuFile(file, id, false);

				// // Show a dialog asking the user for play or queue the song
				/* AlertDialog.Builder builder = new AlertDialog.Builder(this,
				 R.style.Theme_Light_Translucent_Dialog);
				 builder.setTitle(R.string.app_name);
				 builder.setMessage(getString(
				 R.string.play_external_question_msg, name));
				
				DialogInterface.OnClickListener listener = new
				 DialogInterface.OnClickListener() {
				 @Override
				 public void onClick(DialogInterface dialog, int which) {
				 try {
				 switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
				 playOrEnqueuFile(file, id, false);
				 break;
				
				case DialogInterface.BUTTON_NEUTRAL:
				playOrEnqueuFile(file, id, true);
				 break;
				
				case DialogInterface.BUTTON_NEGATIVE:
				 break;
				
				 default:
				 break;
				 }
				 } finally {
				finish();
				 }
				 }
				 };
				 builder.setPositiveButton(
				 R.string.play_external_question_button_play, listener);
				 builder.setNeutralButton(
				 R.string.play_external_question_button_queue, listener);
				 builder.setNegativeButton(
				 R.string.play_external_question_button_cancel, listener);
				
				 Dialog dialog = builder.create();
				 dialog.setOnCancelListener(this);
				 dialog.show();*/
			}

		} catch (Exception e) {
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
