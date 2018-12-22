package com.prize.left.page.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.launcher3.Insettable;
/***
 * 左一屏搜索最外层布局
 * @author fanjunchen
 *
 */
public class SearchFrameLayout extends FrameLayout {

	public SearchFrameLayout(Context context) {
		this(context, null);
	}

	public SearchFrameLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SearchFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public SearchFrameLayout(Context ctx, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(ctx, attrs, defStyleAttr, defStyleRes);
	}
	
	private final Rect mInsets = new Rect();
	
	
    public boolean fitSystemWindowsByPrizeScrollView(Rect insets) {
        final int n = getChildCount();
        for (int i = 0; i < n; i++) {
            final View child = getChildAt(i);
            final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) child.getLayoutParams();
            if (child instanceof Insettable) {
                ((Insettable)child).setInsets(insets);
            } else {
                flp.topMargin += (insets.top - mInsets.top);
                flp.leftMargin += (insets.left - mInsets.left);
                flp.rightMargin += (insets.right - mInsets.right);
                flp.bottomMargin += (insets.bottom - mInsets.bottom);
            }
            if (child instanceof SearchFrameLayout)
            	child.setLayoutParams(flp);
        }
        mInsets.set(insets);
        return true; // I'll take it from here
    }
	
	@Override
    protected boolean fitSystemWindows(Rect insets) {
        final int n = getChildCount();
        for (int i = 0; i < n; i++) {
            final View child = getChildAt(i);
            final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) child.getLayoutParams();
            if (child instanceof Insettable) {
                ((Insettable)child).setInsets(insets);
            } else {
                flp.topMargin += (insets.top - mInsets.top);
                flp.leftMargin += (insets.left - mInsets.left);
                flp.rightMargin += (insets.right - mInsets.right);
                flp.bottomMargin += (insets.bottom - mInsets.bottom);
            }
            if (child instanceof SearchFrameLayout)
            	child.setLayoutParams(flp);
        }
        mInsets.set(insets);
        return true; // I'll take it from here
    }
	
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}
	/***
	 * 当activity销毁时, 销毁多余对象
	 */
	public void onDestroy() {
	}
}
