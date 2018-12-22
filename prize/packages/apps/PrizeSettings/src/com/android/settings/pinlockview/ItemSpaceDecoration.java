package com.android.settings.pinlockview;

import com.android.settings.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * @author wangzhong
 */
public class ItemSpaceDecoration extends RecyclerView.ItemDecoration {

    private final int mHorizontalSpaceWidth;
    private final int mVerticalSpaceHeight;
    private final int mSpanCount;
    private final boolean mIncludeEdge;

    private Drawable mDivider;

    public ItemSpaceDecoration(Context context, int horizontalSpaceWidth, int verticalSpaceHeight, int spanCount, boolean includeEdge) {
        this.mHorizontalSpaceWidth = horizontalSpaceWidth;
        this.mVerticalSpaceHeight = verticalSpaceHeight;
        this.mSpanCount = spanCount;
        this.mIncludeEdge = includeEdge;

        //mDivider = new ColorDrawable(0xDCDCDC);
        mDivider = context.getDrawable(R.drawable.prize_shape_pinlockview_item_divider);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent) {
        //Log.v("ItemSpaceDecoration", "onDraw()");
        drawHorizontal(c, parent);
        drawVertical(c, parent);
    }

    /* transverse line */
    private void drawHorizontal(Canvas c, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getLeft() - params.leftMargin;
            final int right = child.getRight() + params.rightMargin
                    + mHorizontalSpaceWidth;
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mHorizontalSpaceWidth;
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    /**/
    private void drawVertical(Canvas c, RecyclerView parent) {
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getTop() - params.topMargin;
            final int bottom = child.getBottom() + params.bottomMargin;
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mVerticalSpaceHeight;

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view);
        int column = position % mSpanCount;

        if (mIncludeEdge) {
            outRect.left = mHorizontalSpaceWidth - column * mHorizontalSpaceWidth / mSpanCount;
            outRect.right = (column + 1) * mHorizontalSpaceWidth / mSpanCount;

            if (position < mSpanCount) {
                outRect.top = mVerticalSpaceHeight;
            }
            outRect.bottom = mVerticalSpaceHeight;
        } else {
            outRect.left = column * mHorizontalSpaceWidth / mSpanCount;
            outRect.right = mHorizontalSpaceWidth - (column + 1) * mHorizontalSpaceWidth / mSpanCount;
            if (position >= mSpanCount) {
                outRect.top = mVerticalSpaceHeight;
            }
        }
    }
}
