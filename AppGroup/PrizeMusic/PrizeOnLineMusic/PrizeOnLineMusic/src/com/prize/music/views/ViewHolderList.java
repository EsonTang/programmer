/**
 * 
 */

package com.prize.music.views;

import com.prize.music.R;

import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Andrew Neal
 */
public class ViewHolderList {
	// public final ImageView mViewHolderImage;
	public final ImageView mPeakOne, mPeakTwo, mQuickContextDivider,
			mQuickContextTip;

	public final TextView mViewHolderLineOne;

	public final TextView mViewHolderLineTwo;

	public final FrameLayout mQuickContext;
	public final CheckBox checkBox;

	public ViewHolderList(View view) {
		mViewHolderLineOne = (TextView) view
				.findViewById(R.id.listview_item_line_one);
		mViewHolderLineTwo = (TextView) view
				.findViewById(R.id.listview_item_line_two);
		mQuickContext = (FrameLayout) view
				.findViewById(R.id.track_list_context_frame);
		mPeakOne = (ImageView) view.findViewById(R.id.peak_one);
		mPeakTwo = (ImageView) view.findViewById(R.id.peak_two);
		mQuickContextDivider = (ImageView) view
				.findViewById(R.id.quick_context_line);
		mQuickContextTip = (ImageView) view
				.findViewById(R.id.quick_context_tip);
		checkBox = (CheckBox) view.findViewById(R.id.checkBox);

	}
}
