/**
 * 
 */

package com.prize.music.ui.widgets;

import static com.prize.music.Constants.SIZE_THUMB;
import static com.prize.music.Constants.SRC_FIRST_AVAILABLE;
import static com.prize.music.Constants.TYPE_ALBUM;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.music.R;
import com.prize.music.cache.ImageInfo;
import com.prize.music.cache.ImageProvider;
import com.prize.music.helpers.utils.MusicUtils;

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

			if (MusicUtils.getCurrentAudioId() != -1) {
				mTrackName.setText(MusicUtils.getTrackName());
				mArtistName.setText(MusicUtils.getArtistName());
				ImageInfo mInfo = new ImageInfo();
				mInfo.type = TYPE_ALBUM;
				mInfo.size = SIZE_THUMB;
				mInfo.source = SRC_FIRST_AVAILABLE;
				mInfo.data = new String[] {
						String.valueOf(MusicUtils.getCurrentAlbumId()),
						MusicUtils.getArtistName(), MusicUtils.getAlbumName() };

				ImageProvider.getInstance(activity).loadImage(mAlbumArt, mInfo);
			} else if (MusicUtils.mService != null
					&& MusicUtils.getCurrentAudioId() == -1) {
				mTrackName.setText("");
				mArtistName.setText("");
				mAlbumArt.setImageResource(R.drawable.no_art_small);
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
