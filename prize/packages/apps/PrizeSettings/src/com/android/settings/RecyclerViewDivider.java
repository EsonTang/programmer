package com.android.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.View;
import android.graphics.PorterDuff;
public class RecyclerViewDivider extends RecyclerView.ItemDecoration {
    private Paint mPaint;
    private Drawable mDivider;
    private int mDividerHeight = 1; //Item Divider Height，By default 1px
    private int mOrientation; // LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
	private Context mContext;
	private int preference_divider;
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    /**
     * Default Item Divider：Height:2px，Color:gray
     *
     * @param context
     * @param orientation
     */
    public RecyclerViewDivider(Context context, int orientation) {
    	mContext = context;
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("Please input right parameter");
        }
        mOrientation = orientation;

        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    /**
     * Custom Item Divider
     *
     * @param context
     * @param orientation
     * @param drawableId
     */
    public RecyclerViewDivider(Context context, int orientation, int drawableId) {
        this(context, orientation);
        mDivider = ContextCompat.getDrawable(context, drawableId);
        mDividerHeight = mDivider.getIntrinsicHeight();
    }

    /**
     * Custom Item Divider
     *
     * @param context
     * @param orientation
     * @param dividerHeight
     * @param dividerColor
     */
    public RecyclerViewDivider(Context context, int orientation, int dividerHeight, int dividerColor) {
        this(context, orientation);
		preference_divider = dividerColor;
        mDividerHeight = dividerHeight;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.FILL);
    }


    //Get item divider height
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
		if(shouldDrawDividerBelow(view,parent)){
			 outRect.set(0, 0, 0, mDividerHeight);
		}
       
    }

    //Draw item divider
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
			drawHorizontal(c, parent);
		}
            
    }

    //Draw Horizontal item divider
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        // final int left = parent.getPaddingLeft();
        // final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        
    	final int left = mContext.getResources().getDimensionPixelSize(R.dimen.dashboard_tile_image_margin_start_prize);
    	final int right = parent.getMeasuredWidth() - mContext.getResources().getDimensionPixelSize(R.dimen.dashboard_tile_image_margin_end_prize);
        
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
			if (shouldDrawDividerBelow(child, parent)) {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
				final int top = child.getBottom() + layoutParams.bottomMargin;
				final int bottom = top + mDividerHeight;
				if (mDivider != null) {
					mDivider.setBounds(0, top, parent.getMeasuredWidth()-1, bottom);
					// mDivider.setColorFilter(mContext.getResources().getColor(R.color.red,null),PorterDuff.Mode.CLEAR);
					mDivider.draw(canvas);
				}
				if (mPaint != null) {
					 mPaint.setColor(mContext.getResources().getColor(R.color.settings_background,null));
					 canvas.drawRect(0, top, parent.getMeasuredWidth()-1, bottom, mPaint);
				     mPaint.setColor(preference_divider);
					canvas.drawRect(left, top, right, bottom, mPaint);
				}
			}
		}
           
     }
 
     //Draw Vertical item divider
     private void drawVertical(Canvas canvas, RecyclerView parent) {
         final int top = parent.getPaddingTop();
         final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
         final int childSize = parent.getChildCount();
         for (int i = 0; i < childSize; i++) {
             final View child = parent.getChildAt(i);
             RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
             final int left = child.getRight() + layoutParams.rightMargin;
             final int right = left + mDividerHeight;
             if (mDivider != null) {
                 mDivider.setBounds(left, top, right, bottom);
                 mDivider.draw(canvas);
             }
             if (mPaint != null) {
                 canvas.drawRect(left, top, right, bottom, mPaint);
             }
         }
     }
	
	 private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
            final RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);
            final boolean dividerAllowedBelow = holder instanceof PreferenceViewHolder
                    && ((PreferenceViewHolder) holder).isDividerAllowedBelow();
            if (!dividerAllowedBelow) {
                return false;
            }
            boolean nextAllowed = true;
            int index = parent.indexOfChild(view);
            if (index < parent.getChildCount() - 1) {
                final View nextView = parent.getChildAt(index + 1);
                final RecyclerView.ViewHolder nextHolder = parent.getChildViewHolder(nextView);
                nextAllowed = nextHolder instanceof PreferenceViewHolder
                        && ((PreferenceViewHolder) nextHolder).isDividerAllowedAbove();
            }
            return nextAllowed;
        }
 }
