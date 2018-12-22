package com.android.launcher3.tsearch;

import java.util.Hashtable;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class PrizeHorizontalScrollView extends HorizontalScrollView {
	private static final String TAG = "DrawerHScrollView";

	private int currentPage = 0;
	private int totalPages = 1;

	private int mScrollDisX;
	private static Hashtable<Integer, Integer> positionLeftTopOfPages = new Hashtable();

	public PrizeHorizontalScrollView(Context context) {
		super(context);
	}

	public PrizeHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PrizeHorizontalScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public void cleanup() {
		currentPage = 0;
		totalPages = 1;
		if (positionLeftTopOfPages != null) {
			positionLeftTopOfPages.clear();
		}
	}

	public void setParameters(int totalPages, int currentPage, int scrollDisX) {
		Log.d(TAG, "~~~~~setParameters totalPages:" + totalPages
				+ ",currentPage:" + currentPage + ",scrollDisX:" + scrollDisX);
		this.totalPages = totalPages;
		this.currentPage = currentPage;
		mScrollDisX = scrollDisX;
		positionLeftTopOfPages.clear();
		for (int i = 0; i < totalPages; i++) {
			int posx = (scrollDisX) * i;
			positionLeftTopOfPages.put(i, posx);
			Log.d(TAG, "~~~~~setParameters i:" + i + ",posx:" + posx);
		}
		smoothScrollTo(0, 0);
	}

	/**
	 * �ý��������ָ�ƶ�����ָ�ƶ��ĵص�
	 */
	public void snapToDestination(int x, int y, int oldx, int oldy) {

		Log.e(TAG, "----snapToDestination---");
		final int screenWidth = mScrollDisX; // ��view�Ŀ�ȣ�������Ϊ������ĸ�view�Ŀ��
		Log.e(TAG, "screenWidth = " + screenWidth);
		final int destScreen = (x + screenWidth / 2) / screenWidth; // ĳ���㷨�ɣ�
		Log.e(TAG, "[destScreen] : " + destScreen); // �Ҽ�����һ�µ�ȷ���ܹ�׼ȷ���Ŀ��view
		// getScroolX()ֵΪ
		int postionTo = (Integer) positionLeftTopOfPages.get(
				new Integer(destScreen)).intValue();
		smoothScrollTo(postionTo, 0);
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {

		int sumX = computeHorizontalScrollOffset();
		sumX = sumX - 1;
		int destPage;
		if (sumX <= 0) {
			destPage = 0;
		} else {
			destPage = sumX / mScrollDisX + 1;
		}

		// if (destPage != currentPage) {
		currentPage = destPage;

		// updatePageIndicator(totalPages, currentPage);
		// ���Լ�����
		// }

		super.onScrollChanged(x, y, oldx, oldy);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getAction()) {
		case MotionEvent.ACTION_UP:

			int postionTo = (Integer) positionLeftTopOfPages.get(
					new Integer(currentPage)).intValue();
			// smoothScrollTo(postionTo, 0);
			// scrollTo(postionTo, 0);
			break;

		default:
			break;
		}
		return super.onTouchEvent(ev);

	}

	@Override
	public void fling(int velocityX) {
		Log.v(TAG, "-->fling velocityX:" + velocityX);
		boolean change_flag = false;
		if (velocityX > 0 && (currentPage < totalPages - 1)) {
			currentPage++;
			change_flag = true;
		} else if (velocityX < 0 && (currentPage > 0)) {
			currentPage--;
			change_flag = true;
		}
		if (change_flag) {
			int postionTo = (Integer) positionLeftTopOfPages.get(
					new Integer(currentPage)).intValue();
			Log.v(TAG, "------smoothScrollTo posx:" + postionTo);
			smoothScrollTo(postionTo, 0);
		}
	}
}