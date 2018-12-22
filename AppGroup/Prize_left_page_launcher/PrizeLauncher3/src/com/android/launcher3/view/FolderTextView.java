package com.android.launcher3.view;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class FolderTextView extends View {

	private int tabCount;

	private Drawable use;
	private Drawable nor;
	private ViewPager pager;
	private int currentPosition = 0;

	private float currentPositionOffset = 0f;
	private float mTempcurrentPositionOffset = 0f;

	public FolderTextView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public FolderTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public FolderTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		use = context.getDrawable(R.drawable.ic_launcher_page_point_focused);
		nor = context.getDrawable(R.drawable.ic_launcher_page_point_normal);
		// TODO Auto-generated constructor stub
	}

	public FolderTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	private final PageListener pageListener = new PageListener();

	private class PageListener implements OnPageChangeListener {

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {

			currentPosition = position;
			currentPositionOffset = positionOffset;

			LogUtils.i("zhouerlong", "currentPosition:" + currentPosition
					+ ",,,currentPositionOffset:" + currentPositionOffset);

			invalidate();
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onPageSelected(int position) {
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (isInEditMode() || tabCount == 0) {
			return;
		}

		final int height = getHeight();

		// draw indicator line

		int wgap = 0;
		int childWidth = nor.getIntrinsicWidth() / 2;

		int childHeight = nor.getIntrinsicHeight() / 2;
		int width = this.getWidth();
		int togleWidth = childWidth * tabCount + wgap * (tabCount - 1);
		int curLeft = (int) (width / 2f - togleWidth / 2f) + currentPosition
				* (childWidth + wgap);
		int useLeft = curLeft;

		// if there is an offset, start interpolating left and right coordinates
		// between current and next tab

		int togleLeft = (int) (width / 2f - togleWidth / 2f);
		currentPositionOffset = (1 - currentPositionOffset);
		int nextLeft = (int) togleLeft + (currentPosition + 1)
				* (childWidth + wgap);
		useLeft = (int) (currentPositionOffset * (nextLeft - curLeft) + curLeft);
		LogUtils.i("zhouerlong", "useLeft:" + useLeft + "----currentPositionOffset:"
				+ currentPositionOffset + "---nextLeft:" + nextLeft
				+ "----curLeft:" + curLeft );
		int ntLeft;
		for (int i = 0; i < tabCount; i++) {
			canvas.save();
			int j = i - currentPosition;
			int left = (int) togleLeft + j * (childWidth + wgap);
			ntLeft = (int) togleLeft + (j + 1) * (childWidth + wgap);
			int leftOffset = (int) (currentPositionOffset * (ntLeft - left) + left);
			nor.setBounds(0, 0, childWidth, childHeight);
			canvas.translate(leftOffset, 0);
			nor.draw(canvas);
			Paint p = new Paint();
			int alpha = 0;

			p.setColor(Color.WHITE);
			alpha = (int) (currentPositionOffset * 255);
			// p.setAlpha(alpha);
			p.setTextSize(25);
			canvas.drawText(i + "", 20, 40, p);
			canvas.restore();

		}
		canvas.save();
		use.setBounds(0, 0, childWidth, childHeight);
		canvas.translate(useLeft, 0);
		// use.draw(canvas);
		canvas.restore();
		// }

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

	public void notifyDataSetChanged() {

		tabCount = pager.getAdapter().getCount();
		currentPosition = pager.getCurrentItem();

	}

}