/**
 * 
 */

package com.prize.music.views;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.music.R;

/**
 * @author Andrew Neal
 */
public class Album_ViewHolderGrid {

	public final ImageView mViewHolderImage;

	public final TextView mViewHolderLineOne;

	public final RelativeLayout mInfoHolder;

	public Album_ViewHolderGrid(View view) {
		mViewHolderImage = (ImageView) view
				.findViewById(R.id.action_bar_album_art);
		mViewHolderLineOne = (TextView) view
				.findViewById(R.id.action_bar_album_name);

		mInfoHolder = (RelativeLayout) view
				.findViewById(R.id.gridview_info_holder);
		// mInfoHolder.setBackgroundColor(view.getResources().getColor(
		// R.color.app_background_color));
	}

}
