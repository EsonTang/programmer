/**
 * 
 */

package com.prize.music.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.app.constants.Constants;
import com.prize.music.cache.ImageInfo;
import com.prize.music.cache.ImageProvider;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.playview.MusicPlayerView;
import com.prize.music.R;

/**
 * @author Andrew Neal
 */
public class BottomActionBar extends LinearLayout implements
		OnLongClickListener {

	public BottomActionBar(Context context) {
		super(context);
	}

	public BottomActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnLongClickListener(this);
	}

	public BottomActionBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Updates the bottom ActionBar's info{eg:artist,music and so on}
	 * 
	 * @param activity
	 */
	public void updateBottomActionBar(Activity activity) {
		View bottomActionBar = activity.findViewById(R.id.bottom_action_bar);
		if (bottomActionBar == null) {
			return;
		}
		TextView mTrackName = null, mArtistName = null;
		ImageView mAlbumArt = null;
		MusicPlayerView mPlay = null;
		if (MusicUtils.mService != null) {
			// Track name
			mTrackName = (TextView) bottomActionBar
					.findViewById(R.id.bottom_action_bar_track_name);
			// Artist name
			mArtistName = (TextView) bottomActionBar
					.findViewById(R.id.bottom_action_bar_artist_name);

			// Album art
			mAlbumArt = (ImageView) bottomActionBar
					.findViewById(R.id.bottom_action_bar_album_art);
			
			mPlay = (MusicPlayerView) bottomActionBar
					.findViewById(R.id.bottom_action_bar_play);

			if(MusicUtils.getIsPlayNetSong()){
				mTrackName.setText(MusicUtils.getTrackName());
				mArtistName.setText(MusicUtils.getArtistName());
				try {
					mAlbumArt.setImageBitmap(MusicUtils.mService.getAlbumBitmap());
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{			
				if (MusicUtils.getCurrentAudioId() != -1 && MusicUtils.getCurrentAudioPath() != null) {//prize-bug:20041-20160818-pengcancan
					mTrackName.setText(MusicUtils.getTrackName());
					mArtistName.setText(MusicUtils.getArtistName());
					ImageInfo mInfo = new ImageInfo();
					mInfo.type = Constants.TYPE_ALBUM;
					mInfo.size = Constants.SIZE_THUMB;
					mInfo.source = Constants.SRC_FIRST_AVAILABLE;
					mInfo.data = new String[] {
							String.valueOf(MusicUtils.getCurrentAlbumId()),
							MusicUtils.getArtistName(),
							MusicUtils.getAlbumName() };

					ImageProvider.getInstance(activity).loadImage(mAlbumArt,
							mInfo);
				} else if (MusicUtils.mService != null
						&& (MusicUtils.getCurrentAudioId() == -1||MusicUtils.getCurrentAudioPath() == null)) {//prize-bug:20041-20160818-pengcancan
					mTrackName.setText("");
					mArtistName.setText("");
					mAlbumArt.setImageResource(R.drawable.no_art_small);
					mPlay.setProgress(0);
				}
			}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// Context context = v.getContext();
		// context.startActivity(new Intent(context, QuickQueue.class));
		return true;
	}

}
