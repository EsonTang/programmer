package com.android.launcher3;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class PrizeSimpleDeviceProfile {

	private int mRows = 1;

	private int mCols = 4;

	private static PrizeSimpleDeviceProfile instance = null;
	/** 一个单元的宽度与高度 */
	private int mCellW, mCellH;
	/** 屏幕左右间距 */
	private int leftPadding, rightPadding, topPadding, bottomPadding;
	/** 水平间隔 */
	private int horizontalPadding;
	/** 垂直间隔 */
	private int verticalPadding;

	public static PrizeSimpleDeviceProfile getInstance() {
		return instance;
	}

	public PrizeSimpleDeviceProfile(Context ctx, int minW, int minH, int w,
			int h, int availableWidth, int availableHeight) {
		mRows = 1;// res.getInteger(R.integer.simple_rows);

		mCols = 4;// res.getInteger(R.integer.simple_cols);

		verticalPadding = 0;// res.getDimensionPixelSize(R.dimen.item_vertical_padding);

		horizontalPadding = 0;// res.getDimensionPixelSize(R.dimen.item_horizontal_padding);

		leftPadding = 0;// res.getDimensionPixelSize(R.dimen.page_left_padding);
		rightPadding = 0;// res.getDimensionPixelSize(R.dimen.page_right_padding);
		topPadding = 0;// res.getDimensionPixelSize(R.dimen.page_top_padding);
		bottomPadding = 0;// res.getDimensionPixelSize(R.dimen.page_bottom_padding);

		calc(availableWidth, availableHeight);

		instance = this;
	}

	/***
	 * 计算单元高度与宽度
	 */
	private void calc(int w, int h) {
		mCellW = (w - leftPadding - rightPadding - (mCols - 1)
				* horizontalPadding)
				/ mCols;

		mCellH = (h - topPadding - bottomPadding - (mRows - 1)
				* verticalPadding)
				/ mRows;
	}

	public static float dpiFromPx(int size, DisplayMetrics metrics) {
		float densityRatio = (float) metrics.densityDpi
				/ DisplayMetrics.DENSITY_DEFAULT;
		return (size / densityRatio);
	}

	public static int pxFromDp(float size, DisplayMetrics metrics) {
		return (int) Math.round(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, size, metrics));
	}

	public static int pxFromSp(float size, DisplayMetrics metrics) {
		return (int) Math.round(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, size, metrics));
	}

	/***
	 * 获取单元格宽度
	 * 
	 * @return
	 */
	public int getCellW() {
		return mCellW;
	}

	/***
	 * 获取单元格高度
	 * 
	 * @return
	 */
	public int getCellH() {
		return mCellH;
	}

	public int getRows() {
		return mRows;
	}

	public int getCols() {
		return mCols;
	}

	public int getLeftPadding() {
		return leftPadding;
	}

	public int getRightPadding() {
		return rightPadding;
	}

	public int getTopPadding() {
		return topPadding;
	}

	public int getBottomPadding() {
		return bottomPadding;
	}

	public int getHorizontalPadding() {
		return horizontalPadding;
	}

	public int getVerticalPadding() {
		return verticalPadding;
	}

	public int calCellWidth(int w, int cols) {
		int cw = (w - horizontalPadding * (cols - 1) - (leftPadding + rightPadding))
				/ cols;
		mCellW = cw;
		return cw;
	}

	public int calCellHeight(int h, int rows) {
		int ch = (h - verticalPadding * (rows - 1) - (topPadding + bottomPadding))
				/ rows;
		mCellH = ch;
		return ch;
	}

}
