package com.android.launcher3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;

public class PrizeCellLayout extends ViewGroup implements Page{

	private PrizeSimpleDeviceProfile profile;
	private int mCountX;
	private int mCountY;
	private int mCellWidth;
	private int mCellHeight;
	private boolean mChanged;
	private int preHeight;

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//        super.layout(left, top, right, bottom);

		int p = 0;//(int) (12*Launcher.scale);
		int childCount = getChildCount();
		int width = this.getMeasuredWidth();
		int childleft = 0;
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			int height = childView.getLayoutParams().height;
			final int childWidth = (int) (85*Launcher.scale);//childView.getLayoutParams().width;
			int left1 = childWidth * childCount + p * (childCount - 1);
//			if (childView.getVisibility() != View.GONE) {
				int pageNum =0;
				// 此处获取到的宽度就是在onMeasure中设置的值
			/*	if(childCount>5) {
					pageNum = i/6;
					left = ((childWidth+p)*i - pageNum*p);
				}else {*/
					 left = (int) (width / 2f - left1 / 2f) + i
							* (childWidth + p);
//				}
				 right = left + childWidth;

				// 为每一个子View布局
				childView.layout(left, 0, right, mCellHeight);

//			}
		}
	
    }




	public void addViewToCellLayout(View child, int index, int childId,
			CellLayout.LayoutParams params) {

		params.customPosition = true;
		this.addView(child, index, params);
	}

/*	public static class LayoutParams extends ViewGroup.MarginLayoutParams {
		*//**
		 * Horizontal location of the item in the grid.
		 *//*
		@ViewDebug.ExportedProperty
		public int cellX;

		*//**
		 * Vertical location of the item in the grid.
		 *//*
		@ViewDebug.ExportedProperty
		public int cellY;

		*//**
		 * Number of cells spanned horizontally by the item.
		 *//*
		@ViewDebug.ExportedProperty
		public int cellHSpan;

		*//**
		 * Number of cells spanned vertically by the item.
		 *//*
		@ViewDebug.ExportedProperty
		public int cellVSpan;

		// X coordinate of the view in the layout.
		@ViewDebug.ExportedProperty
		int x;
		// Y coordinate of the view in the layout.
		@ViewDebug.ExportedProperty
		int y;

		public boolean customPosition = false;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
			cellHSpan = 1;
			cellVSpan = 1;
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
			cellHSpan = 1;
			cellVSpan = 1;
		}

		public LayoutParams(LayoutParams source) {
			super(source);
			this.cellX = source.cellX;
			this.cellY = source.cellY;
			this.cellHSpan = source.cellHSpan;
			this.cellVSpan = source.cellVSpan;
		}

		public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan) {
			super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			this.cellX = cellX;
			this.cellY = cellY;
			this.cellHSpan = cellHSpan;
			this.cellVSpan = cellVSpan;
		}

		*//***
		 * 这个方法暂时不调用
		 * 
		 * @param cellWidth
		 * @param cellHeight
		 * @param widthGap
		 * @param heightGap
		 * @param invertHorizontally
		 * @param colCount
		 *//*
		public void setup(int cellWidth, int cellHeight, int widthGap,
				int heightGap, boolean invertHorizontally, int colCount) {
			final int myCellHSpan = cellHSpan;
			final int myCellVSpan = cellVSpan;

			int myCellX = cellX;
			int myCellY = cellY;

			if (invertHorizontally) {
				myCellX = colCount - myCellX - cellHSpan;
			}

			width = myCellHSpan * cellWidth + ((myCellHSpan - 1) * widthGap)
					- leftMargin - rightMargin;
			height = myCellVSpan * cellHeight + ((myCellVSpan - 1) * heightGap)
					- topMargin - bottomMargin;
			x = (int) (myCellX * (cellWidth + widthGap) + widthGap + leftMargin);
			y = (int) (myCellY * (cellHeight + heightGap) + topMargin);
		}

		public void setup1(int cellW, int cellH) {
			int startX = PrizeSimpleDeviceProfile.getInstance()
					.getLeftPadding();
			int startY = PrizeSimpleDeviceProfile.getInstance().getTopPadding();
			int hPadding = PrizeSimpleDeviceProfile.getInstance()
					.getHorizontalPadding();
			x = startX + cellW * cellX + cellX * hPadding;
			y = cellH
					* cellY
					+ cellY
					* PrizeSimpleDeviceProfile.getInstance()
							.getVerticalPadding() + startY;
			width = cellHSpan * cellW + (cellHSpan - 1) * hPadding;
			height = cellVSpan
					* cellH
					+ (cellVSpan - 1)
					* PrizeSimpleDeviceProfile.getInstance()
							.getVerticalPadding();
		}

		public String toString() {
			return "(" + this.x + ", " + this.y + ", " + this.width + ", "
					+ this.height + ")";
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
	}*/

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// TODO Auto-generated method stub
		/*
		 * int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec); int
		 * heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		 */

		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int childWidthSize = widthSize - (getPaddingLeft() + getPaddingRight());
		int childHeightSize = heightSize
				- (getPaddingTop() + getPaddingBottom());

		mCellWidth = profile.getCellW();
		mCellHeight = profile.getCellH();
		if (preHeight != mCellHeight && !mChanged) {
			mChanged = true;
			preHeight = mCellHeight;
	        profile.calCellWidth(childWidthSize, mCountX);
	        profile.calCellHeight(childHeightSize, mCountY);
		}

		int count = getChildCount();

		int childW = Math.min(childWidthSize, mCellWidth);
		int childH = Math.min(childHeightSize, mCellHeight);
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int childWidthMeasureSpec = 0;
			int childheightMeasureSpec = 0;
			childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childW,
					MeasureSpec.EXACTLY);
			childheightMeasureSpec = MeasureSpec.makeMeasureSpec(childH,
					MeasureSpec.EXACTLY);
			child.measure(childWidthMeasureSpec, childheightMeasureSpec);
			// maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
			// maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
		}

		setMeasuredDimension(widthSize, heightSize);

	}

	public PrizeCellLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public PrizeCellLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public PrizeCellLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PrizeCellLayout(Context context) {
		super(context);
		profile = PrizeSimpleDeviceProfile.getInstance();
		mCountX = profile.getCols();
		mCountY = profile.getRows();
	}

	@Override
	public int getPageChildCount() {
		// TODO Auto-generated method stub
		return this.getChildCount();
	}

	@Override
	public View getChildOnPageAt(int i) {
		// TODO Auto-generated method stub
		return getChildAt(i);
	}

	@Override
	public void removeAllViewsOnPage() {
		this.removeAllViews();
		
	}

	@Override
	public void removeViewOnPageAt(int i) {
		this.removeViewAt(i);
		
	}

	@Override
	public int indexOfChildOnPage(View v) {
		// TODO Auto-generated method stub
		return this.indexOfChild(v);
	}

}
