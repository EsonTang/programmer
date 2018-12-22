package com.prize.container.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * <p>
 * Forked from Google Samples &gt; SlidingTabsBasic &gt; <a href=
 * "https://developer.android.com/samples/SlidingTabsBasic/src/com.example.android.common/view/SlidingTabLayout.html"
 * >SlidingTabStrip</a>
 */
class HorizontalScrollStrip extends LinearLayout {

    protected static final String TAG = "HorizontalScrollStrip";

    HorizontalScrollStrip(Context context, AttributeSet attrs) {
        super(context);
        setWillNotDraw(false);
        setBackgroundColor(Color.TRANSPARENT);
    }
}