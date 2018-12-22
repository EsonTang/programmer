package com.android.launcher3;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class FitSystemWindow extends FrameLayout {

	public FitSystemWindow(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public FitSystemWindow(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public FitSystemWindow(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public FitSystemWindow(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

    private final Rect mInsets = new Rect();
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
            if (child instanceof FitSystemWindow)
            child.setLayoutParams(flp);
        }
        mInsets.set(insets);
        return true; // I'll take it from here
    }

}
