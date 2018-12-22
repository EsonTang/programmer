package com.prize.permissionmanage;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by prize on 2018/2/26.
 */

public class CoutomListView extends ListView {
    public CoutomListView(Context context) {
        super(context);
    }

    public CoutomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoutomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int hight = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, hight);
    }
}
