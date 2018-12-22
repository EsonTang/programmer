/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.android.gallery3d.filtershow.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.android.gallery3d.filtershow.FilterShowActivity;
import com.prize.sticker.StickerManager;

public class StickerView extends View {

    public StickerView(Context context) {
        this(context, null);
    }

    public StickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(FilterShowActivity activity) {
        StickerManager.getStickerManager().init(activity, this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        StickerManager.getStickerManager().onDrawSticker(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return StickerManager.getStickerManager().onTouchEvent(event, getWidth(), getHeight());
    }

}
