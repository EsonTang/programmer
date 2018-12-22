package com.prize.left.page.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/***
 * 不带滚动条, 自适应高度的GridView
 * @author fanjunchen
 *
 */
public class NoScroolGridView extends GridView {

    public NoScroolGridView(Context context) {
        super(context);
    }

    public NoScroolGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScroolGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
