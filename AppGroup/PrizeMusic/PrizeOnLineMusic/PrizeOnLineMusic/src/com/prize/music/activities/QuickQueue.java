/**
 * 
 */

package com.prize.music.activities;

import android.media.AudioManager;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.support.v4.app.FragmentActivity;

import com.prize.app.constants.Constants;
import com.prize.music.ui.fragments.grid.QuickQueueFragment;

/**
 */
public class QuickQueue extends FragmentActivity {

	@Override
	protected void onCreate(Bundle icicle) {
		// This needs to be called first
		super.onCreate(icicle);

		
		// Control Media volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		Bundle bundle = new Bundle();
		bundle.putString(Constants.MIME_TYPE, Audio.Playlists.CONTENT_TYPE);
		bundle.putLong(BaseColumns._ID, Constants.PLAYLIST_QUEUE);
		getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, new QuickQueueFragment(bundle))
				.commit();
		
		
	}
}
