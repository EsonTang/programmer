package com.android.launcher3.view;

import com.android.prize.simple.model.SimpleDeviceProfile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
/***
 * 简单页面
 * @author fanjunchen
 *
 */
public class CellWidgetLayout extends ViewGroup {

	/**单元格宽度*/
	private int mCellWidth;
	/**单元格高度*/
    private int mCellHeight;
    /**每页的列数*/
    private int mCountX;
    /**每页的行数*/
    private int mCountY;
    
    SimpleDeviceProfile profile;
    /**第一页为特殊页*/
    public int index = 0;
	
	public CellWidgetLayout(Context ctx) {
		this(ctx, null);
		// TODO Auto-generated constructor stub
	}

	public CellWidgetLayout(Context ctx, AttributeSet attrs) {
		this(ctx, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public CellWidgetLayout(Context ctx, AttributeSet attrs, int defStyleAttr) {
		this(ctx, attrs, defStyleAttr, 0);
		// TODO Auto-generated constructor stub
	}

	public CellWidgetLayout(Context ctx, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(ctx, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		profile = SimpleDeviceProfile.getInstance();
		mCountX = profile.getCols();
		mCountY = profile.getRows();
	}

	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		
		int offset = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() -
                (mCountX * mCellWidth);
        int left = getPaddingLeft() + (int) Math.ceil(offset / 2f);
        int top = getPaddingTop();
        int count = getChildCount();
        
        for (int i = 0; i < count; i++) {
        	
            View child = getChildAt(i);
            final LayoutParams flp =  (LayoutParams) child.getLayoutParams();
            if (flp instanceof ViewGroup.LayoutParams) {
                final LayoutParams lp = (LayoutParams) flp;
                if (lp.customPosition) {
                    // child.layout(left + lp.x, top + lp.y, left + lp.x + lp.width, top + lp.y + lp.height);
                	if (mChanged)
                		lp.setup1(mCellWidth, mCellHeight);
                    child.layout(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height);
                }
                else { //整屏
                    child.layout(left, top,
                            left + r - l,
                            top + b - t);
                }
            }
        }
        mChanged = false;
	}
	/**为适配virtual navigation 用*/
	private int preHeight = 0;
	private boolean mChanged = false;
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		/*int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);*/
        
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize =  MeasureSpec.getSize(heightMeasureSpec);
        int childWidthSize = widthSize - (getPaddingLeft() + getPaddingRight());
        int childHeightSize = heightSize - (getPaddingTop() + getPaddingBottom());
        
        
        mCellWidth = profile.getCellW();
        mCellHeight = profile.getCellH();
        if (preHeight != mCellHeight && !mChanged) {
        	mChanged = true;
        	preHeight = mCellHeight;
        }
        
        int count = getChildCount();
        
        int childW = Math.min(childWidthSize, mCellWidth);
        int childH = Math.min(childHeightSize, mCellHeight);
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childWidthMeasureSpec = 0;
            int childheightMeasureSpec = 0;
            if (index == 1 && i == 0) {
            	childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childW * 2 + SimpleDeviceProfile.getInstance().getHorizontalPadding(),
                        MeasureSpec.EXACTLY);
                	childheightMeasureSpec = MeasureSpec.makeMeasureSpec(childH * 2 + + SimpleDeviceProfile.getInstance().getVerticalPadding(),
                        MeasureSpec.EXACTLY);
            }
            else {
            	childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childW,
                    MeasureSpec.EXACTLY);
            	childheightMeasureSpec = MeasureSpec.makeMeasureSpec(childH,
                    MeasureSpec.EXACTLY);
            }
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
//            maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
//            maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
        }
        
        setMeasuredDimension(widthSize, heightSize);
	}
	
	@Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CellWidgetLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof CellWidgetLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new CellWidgetLayout.LayoutParams(p);
    }
	
	public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        /**
         * Horizontal location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellX;

        /**
         * Vertical location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellY;

        /**
         * Number of cells spanned horizontally by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellHSpan;

        /**
         * Number of cells spanned vertically by the item.
         */
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

        /***
         * 这个方法暂时不调用
         * @param cellWidth
         * @param cellHeight
         * @param widthGap
         * @param heightGap
         * @param invertHorizontally
         * @param colCount
         */
        public void setup(int cellWidth, int cellHeight, int widthGap, int heightGap,
                boolean invertHorizontally, int colCount) {
            final int myCellHSpan = cellHSpan;
            final int myCellVSpan = cellVSpan;
            
            int myCellX = cellX;
            int myCellY = cellY;

            if (invertHorizontally) {
                myCellX = colCount - myCellX - cellHSpan;
            }

            width = myCellHSpan * cellWidth + ((myCellHSpan - 1) * widthGap) -
                    leftMargin - rightMargin;
            height = myCellVSpan * cellHeight + ((myCellVSpan - 1) * heightGap) -
                    topMargin - bottomMargin;
            x = (int) (myCellX * (cellWidth + widthGap)+widthGap + leftMargin);
            y = (int) (myCellY * (cellHeight + heightGap) + topMargin);
        }
        
        public void setup1(int cellW, int cellH) {
    		int startX = SimpleDeviceProfile.getInstance().getLeftPadding();
    		int startY = SimpleDeviceProfile.getInstance().getTopPadding();
    		int hPadding = SimpleDeviceProfile.getInstance().getHorizontalPadding();
    		x = startX + cellW * cellX + cellX * hPadding;
    		y = cellH * cellY + cellY * SimpleDeviceProfile.getInstance().getVerticalPadding() + startY;
    		width = cellHSpan * cellW + (cellHSpan - 1) * hPadding;
    		height = cellVSpan * cellH + (cellVSpan - 1) * SimpleDeviceProfile.getInstance().getVerticalPadding();
        }

        public String toString() {
            return "(" + this.x + ", " + this.y + ", " + this.width + ", " + this.height + ")";
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
}
