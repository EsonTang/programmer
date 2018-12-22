/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker.pager;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class PagerTab {
	
	private PagerTabListener mTabListener;
	private TextView mTextView;
	private int mIndex;
	
	public interface PagerTabListener {
		void onTabSelected(PagerTab tab);
	}
	
	public void setPagerTabListener(PagerTabListener listener) {
		mTabListener = listener;
	}
	
	public PagerTab(TextView tv, int index) {
		this(tv, index, null);
	}
	
	public PagerTab(TextView tv, int index, PagerTabListener listener) {
		mTextView = tv;
		mIndex = index;
		setPagerTabListener(listener);
		mTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mTabListener != null) {
					mTabListener.onTabSelected(PagerTab.this);
				}
			}
		});
	}
	
	public int getIndex() {
		return mIndex;
	}
	
	public void onSelected(boolean selected) {
		mTextView.setSelected(selected);
	}
}
