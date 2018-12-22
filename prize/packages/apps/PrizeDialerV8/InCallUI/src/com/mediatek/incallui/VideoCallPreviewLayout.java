package com.mediatek.incallui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.mediatek.incallui.ext.ExtensionManager;

public class VideoCallPreviewLayout extends FrameLayout {
    private float mLastMotionX;
    private float mLastMotionY;
    private final String Tag = VideoCallPreviewLayout.class.getSimpleName();
    private int mScreenWith = 0;
    private int mScreenHeight = 0;

    public VideoCallPreviewLayout(Context context) {
        this(context, null);
    }

    public VideoCallPreviewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoCallPreviewLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getScreenSize(context);
        ExtensionManager.getVideoCallExt().setContext(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        float x = ev.getRawX();
        float y = ev.getRawY();
        if (ExtensionManager.getVideoCallExt().onTouchEvent(this, ev)) {
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                break;

            case MotionEvent.ACTION_UP:
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (x - mLastMotionX);
                int deltaY = (int) (y - mLastMotionY);
                mLastMotionX = x;
                mLastMotionY = y;
                moveToPosition(deltaX, deltaY);
                break;

            case MotionEvent.ACTION_CANCEL:

                break;

            default:
                break;
        }
        invalidate();
        // it means the MotionEvent did not pass to parent.
        return true;
    }

    /**
     * M: [video call]get the current phone screen size.
     * @param context
     */
    public void getScreenSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mScreenWith = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // it means the MotionEvent did not pass to children.
        return true;
    }

    /**
     * M: [video call]calculate the accurate move distance.
     * @param deltaX
     * @param deltaY
     */
    private void moveToPosition(int deltaX, int deltaY) {
        /// insure can't move out of the screen
        int paddingLeft = getLeft() + deltaX;
        int paddingTop = getTop() + deltaY;
        int paddingButtom = getBottom() + deltaY;
        int paddingRight = getRight() + deltaX;

        /**
         * The range of dragging surface view of local video should be
         * depends on its parent view, cau's in Landscape video fragment
         * only takes half of screen size with call card displayed.
         */
        int w = ((FrameLayout)getParent()).getMeasuredWidthAndState();
        int h = ((FrameLayout)getParent()).getMeasuredHeightAndState();
        w = (w > mScreenWith) ? mScreenWith : w;
        h = (h > mScreenHeight) ? mScreenHeight : h;

        if (paddingLeft < 0) {
            deltaX = 0;
        }
        if (paddingTop < 0) {
            deltaY = 0;
        }
        if (paddingRight > w) {
            deltaX = w - getRight();
        }
        if (paddingButtom > h) {
            deltaY = h - getBottom();
        }

        if (deltaY != 0) {
            this.offsetTopAndBottom(deltaY);
        }
        if (deltaX != 0) {
            this.offsetLeftAndRight(deltaX);
        }
    }
}
