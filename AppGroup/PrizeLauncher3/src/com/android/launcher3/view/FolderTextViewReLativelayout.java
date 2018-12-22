package com.android.launcher3.view;

import com.android.launcher3.DragLayer.LayoutParams;
import com.android.launcher3.FolderInfo;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FolderTextViewReLativelayout extends RelativeLayout {

	private ViewPager pager;
	private int currentPosition = 0;
	private int tabCount;

	private float currentPositionOffset = 0f;

	public FolderTextViewReLativelayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public FolderTextViewReLativelayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public FolderTextViewReLativelayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public FolderTextViewReLativelayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	private final PageListener pageListener = new PageListener();
	private int childWidth;

	public void setOnPageSelected(OnPageSelected l) {
		
		mListner = l;
	}
	private OnPageSelected mListner;
	private int togleLeft;
	private class PageListener implements OnPageChangeListener {


		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			if (currentPosition != position) {
				currentPosition = position;
				mListner.onPageSelected(position);
			}
			currentPositionOffset = positionOffset;
			

			Log.i("zhouerlong", "currentPosition:" + currentPosition
					+ ",,,currentPositionOffset:" + currentPositionOffset);
			scrollScreen();
			invalidate();
		}

		@Override
		public void onPageScrollStateChanged(int state) {

			Log.i("zhouerlong", "currentPosition:" + currentPosition
					+ ",,,currentPositionOffset:" + currentPositionOffset);
		}

		@Override
		public void onPageSelected(int position) {
//			mListner.onPageSelected(position);
		}

	}
	
	public interface OnPageSelected {
		public void onPageSelected(int position);
	}

	public void notifyDataSetChanged() {

		tabCount = pager.getAdapter().getCount();
		currentPosition = pager.getCurrentItem();

	}

	public static class LayoutParams extends RelativeLayout.LayoutParams {
		public int x, y;
		public boolean customPosition = false;

		/**
		 * {@inheritDoc}
		 */
		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getWidth() {
			return width;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public int getHeight() {
			return height;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getX() {
			return x;
		}

		public void setY(int y) {
			this.y = y;
		}

		public int getY() {
			return y;
		}
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			final RelativeLayout.LayoutParams flp = (RelativeLayout.LayoutParams) child
					.getLayoutParams();
			if (flp instanceof LayoutParams) {
				final LayoutParams lp = (LayoutParams) flp;
				child.layout(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height);
			}
		}
	}

	public void addTextView(int index, Object info, int widths, int height,
			int size,int lefts) {
		childWidth = widths;
		int childHeight = height;
		int width = this.getLayoutParams().width;
		int wgap = 0;
		int togleWidth = childWidth * tabCount + wgap * (tabCount - 1);
		 togleLeft = 0;
		int j = index;
		TextView child = new TextView(this.getContext());
		child.setGravity(Gravity.CENTER);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		lp.width = childWidth;
		lp.height = childHeight;
		int left = (int) togleLeft + j * (childWidth + wgap);
		lp.x = left;
		lp.y = 0;
		if (info instanceof FolderInfo) {
			child.setText(((FolderInfo) info).title);
			child.setTag(info);
		}
		child.setTextSize(size);
		child.setSingleLine(true);
		addView(child, lp);
	}
	
	public void updateFolderTexts(FolderInfo info) {
		
	}
	public void scrollScreen() {
		int wgap = 0;
		int width = this.getLayoutParams().width;
		currentPositionOffset = (1 - currentPositionOffset);
		int togleWidth = childWidth * tabCount + wgap * (tabCount - 1);
//		int togleLeft = togleLeft;
		int mCenterLeft = togleLeft/* + (childWidth + wgap)*/;
		int ntLeft;
		for (int i = 0; i < tabCount; i++) {
			int j = i - currentPosition;
			int left = (int) togleLeft + j * (childWidth + wgap);
			ntLeft = (int) togleLeft + (j + 1) * (childWidth + wgap);
			int leftOffset = (int) (currentPositionOffset * (childWidth + wgap) + left)-(childWidth + wgap);
			float percent = (float)Math.abs(leftOffset)
					/ (childWidth + wgap);
			Log.i("zhouerlong", "leftOffset:" + leftOffset + ",,,mCenterLeft:"
					+ mCenterLeft + "::::pecenter:" + percent);
			if (percent <=1f&&percent>=0) {
				percent=1-percent;
			}else {
				percent=0;
			}
			View child = this.getChildAt(i);
			 child.setAlpha(percent);

			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			lp.x = leftOffset;
			this.requestLayout();
		}
	}

	public void setViewPager(ViewPager pager) {
		this.pager = pager;

		if (pager.getAdapter() == null) {
			throw new IllegalStateException(
					"ViewPager does not have adapter instance.");
		}
		pager.setOnPageChangeListener(pageListener);
		notifyDataSetChanged();
	}

}
