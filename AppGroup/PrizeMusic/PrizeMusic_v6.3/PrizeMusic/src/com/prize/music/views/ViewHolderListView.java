/**
 * 
 */

package com.prize.music.views;

import com.prize.music.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 
 * 专为专辑列表
 */
public class ViewHolderListView {

	public final ImageView mViewHolderImage, mPeakOne, mPeakTwo;

	public final TextView mViewHolderLineOne;

	public final TextView mViewHolderLineThree;
	public final TextView mViewHolderLineTwo;

	public final LinearLayout mInfoHolder;

	public ViewHolderListView(View view) {
		mViewHolderImage = (ImageView) view.findViewById(R.id.gridview_image);
		mViewHolderLineOne = (TextView) view
				.findViewById(R.id.gridview_line_one);
		mViewHolderLineTwo = (TextView) view
				.findViewById(R.id.gridview_line_two);
		mPeakOne = (ImageView) view.findViewById(R.id.peak_one);
		mPeakTwo = (ImageView) view.findViewById(R.id.peak_two);
		mInfoHolder = (LinearLayout) view
				.findViewById(R.id.gridview_info_holder);
		mViewHolderLineThree = (TextView) view
				.findViewById(R.id.mViewHolderLineThree);
		// mInfoHolder.setBackgroundColor(view.getResources().getColor(
		// R.color.app_background_color));
	}

}
