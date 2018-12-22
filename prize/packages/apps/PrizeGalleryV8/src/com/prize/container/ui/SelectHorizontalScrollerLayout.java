package com.prize.container.ui;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;

import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.LogUtil;
import com.prize.container.Utils;
import com.android.gallery3d.R;

public class SelectHorizontalScrollerLayout extends MyHorizontalScrollView {

    protected static final String TAG = "HorizontalScrollerLayout";

    private int mSelectIndex = -1;
    private HorizontalScrollLayoutAdapter mAdapter;
    private OnItemClickListener mOnItemClickListener;
    private Map<View, Integer> mViewPos = new HashMap<View, Integer>();
    protected final HorizontalScrollStrip mScrollStrip;
    private int mCenterImWidth;

    private static final int STATE_IDLE = 0;
    private static final int STATE_CLICK = 1;
    private static final int STATE_TOUCH = 2;

    private int mState = STATE_IDLE;

    private OnClickListener mClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int index = mViewPos.get(v);
            LogUtil.i(TAG, "onClick index=" + index + " mSelectIndex=" + mSelectIndex);
            if (index != mSelectIndex) {
                mState = STATE_CLICK;
                scrollToCenter(index);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mSelectIndex);
                }
            }
        }
    };

    public void destroy() {
        mScrollStrip.removeAllViews();
        mViewPos.clear();
        for (Bitmap bitmap : mBitmapMap.values()) {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        mBitmapMap.clear();
        mSelectIndex = -1;
        mState = STATE_IDLE;
        mPreScrollX = 0;
        LogUtil.i(TAG, "SelectHorizontalScrollerLayout destroy");
    }

    public void toggle(String path) {
        if (mAdapter != null) {
            mAdapter.toggle(path);
            updateView();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int pos);

        void updateIndex(int pos);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public SelectHorizontalScrollerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
//        setBackgroundColor(Color.TRANSPARENT);
        // Disable the Scroll Bar
        LogUtil.i(TAG, "SelectHorizontalScrollerLayout init");
        mCenterImWidth = context.getResources().getDimensionPixelOffset(R.dimen.center_item_width);
        setHorizontalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
        this.mScrollStrip = new HorizontalScrollStrip(context, attrs);
        setFillViewport(false);
        addView(mScrollStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ViewConfiguration configuration = ViewConfiguration.get(context);
    }

    private int mPreScrollX;

    @Override
    protected void scrollEnd() {
        View firstTab = mScrollStrip.getChildAt(0);
        if (firstTab != null) {
            int scrollX = getScrollX();
            LogUtil.i(TAG, "scrollEnd mPreScrollX=" + mPreScrollX + " scrollX=" + scrollX);
            if (scrollX == mPreScrollX) {
                mState = STATE_IDLE;
                LogUtil.i(TAG, "scrollEnd center scrollX=" + getScrollX());
            } else {
                int index = (scrollX + mCenterImWidth / 2) / mCenterImWidth;
                mPreScrollX = index * mCenterImWidth;
                mSelectIndex = index;
                if (mPreScrollX != scrollX) {
                    scrollToCenter(index);
                } else {
                    mState = STATE_IDLE;
                }
                LogUtil.i(TAG, "scrollEnd mSelectIndex=" + mSelectIndex);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.updateIndex(mSelectIndex);
                }
            }
            /*if (scrollX > mPreScrollX) { // right
                int index = (scrollX + mCenterImWidth - 1) / mCenterImWidth;
                mPreScrollX = index * mCenterImWidth;
                mSelectIndex = index;
                if (mPreScrollX != scrollX) {
                    scrollToCenter(index);
                } else {
                    mState = STATE_IDLE;
                }
                LogUtil.i(TAG, "scrollEnd right mSelectIndex=" + mSelectIndex);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.updateIndex(mSelectIndex);
                }
            } else if (scrollX < mPreScrollX) { // left
                int index = (scrollX) / mCenterImWidth;
                mPreScrollX = index * mCenterImWidth;
                mSelectIndex = index;
                if (mPreScrollX != scrollX) {
                    scrollToCenter(index);
                } else {
                    mState = STATE_IDLE;
                }
                LogUtil.i(TAG, "scrollEnd left mSelectIndex=" + mSelectIndex);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.updateIndex(mSelectIndex);
                }
            } else {
                mState = STATE_IDLE;
                LogUtil.i(TAG, "scrollEnd center scrollX=" + getScrollX());
            }*/
        }
    }

    private int judgeScrollX(int scrollX) {
        if (scrollX > mPreScrollX) { // right
            int index = (scrollX + mCenterImWidth - 1) / mCenterImWidth;
            return index * mCenterImWidth;
        } else if (scrollX < mPreScrollX) { // left
            int index = (scrollX) / mCenterImWidth;
            return index * mCenterImWidth;
        }
        return scrollX;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mState == STATE_TOUCH) {
            if (l > oldl) { // right
                int index = (l + mCenterImWidth - 1) / mCenterImWidth;
                LogUtil.i(TAG, "onScrollChanged right index=" + index);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.updateIndex(index);
                }
            } else if (l < oldl) { // left
                int index = (l) / mCenterImWidth;
                LogUtil.i(TAG, "onScrollChanged left index=" + index);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.updateIndex(index);
                }
            }
        }
    }

    public void setAdapter(HorizontalScrollLayoutAdapter adapter) {
        mAdapter = adapter;
        initView();
    }

    private HashMap<String, Bitmap> mBitmapMap = new HashMap<>();

    public Bitmap getBitmapByPath(Path path) {
        return mBitmapMap.get(path.toString());
    }

    public Bitmap getBitmapByPath(String path) {
        return mBitmapMap.get(path);
    }

    public void notifyDataSetChanged(Path path, Bitmap bitmap) {
        Bitmap scaleBitmap = mBitmapMap.get(path.toString());
        if (scaleBitmap == null || scaleBitmap.isRecycled()) {
            mBitmapMap.put(path.toString(), scaleBitmap = Utils.centerSquareScaleBitmap(bitmap, mCenterImWidth));
        }
        if (mAdapter != null) {
            updateView();
        }
    }

    private void updateView() {
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            mAdapter.updateView(i, mScrollStrip.getChildAt(i));
        }
    }

    private void initView() {
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            View childView = mAdapter.getView(i, null, this);
            mViewPos.put(childView, i);
            childView.setOnClickListener(mClickListener);
            mScrollStrip.addView(childView);
        }
        setRelativePadding();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setRelativePadding();
    }

    private void setRelativePadding() {
        if (mScrollStrip.getChildCount() > 0) {
            View firstTab = mScrollStrip.getChildAt(0);
            View lastTab = mScrollStrip.getChildAt(mScrollStrip.getChildCount() - 1);
            int start = (getWidth() - mCenterImWidth) / 2;//Utils.getMeasuredWidth(firstTab)) / 2;
//					- Utils.getMarginStart(firstTab);
            int end = (getWidth() - mCenterImWidth) / 2;//Utils.getMeasuredWidth(lastTab)) / 2;
//					- Utils.getMarginEnd(lastTab);
            mScrollStrip.setMinimumWidth(mScrollStrip.getMeasuredWidth());
            setPaddingRelative(start, getPaddingTop(), end, getPaddingBottom());
//			ViewCompat.setPaddingRelative(this, start, getPaddingTop(), end,
//					getPaddingBottom());
            setClipToPadding(false);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && mSelectIndex != -1) {
            scrollToCenter(mSelectIndex);
        }
    }

    private void scrollToCenter(int tabIndex) {
        final int tabStripChildCount = mScrollStrip.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0
                || tabIndex >= tabStripChildCount) {
            return;
        }
        View selectedTab = mScrollStrip.getChildAt(tabIndex);
        View firstTab = mScrollStrip.getChildAt(0);
        int first = Utils.getWidth(firstTab);
//				+ Utils.getMarginStart(firstTab);
        int selected = Utils.getWidth(selectedTab);
//				+ Utils.getMarginStart(selectedTab);
        int x = Utils.getStart(selectedTab);
//				- Utils.getMarginStart(selectedTab);
        x -= (first - selected) / 2;
        //        setSelect(mSelectIndex);
//		scrollTo(x, 0);
        smoothScrollTo(x, 0);
        mSelectIndex = tabIndex;
        LogUtil.i(TAG, "scrollToCenter tabIndex=" + tabIndex + " x=" + x + " mSelectIndex=" + mSelectIndex);

    }

    private void setSelect(int index) {
        for (int i = 0, count = mScrollStrip.getChildCount(); i < count; i++) {
            View childView = mScrollStrip.getChildAt(i);
            if (i == index) {
                childView.setSelected(true);
            } else {
                childView.setSelected(false);
            }
        }
    }

    private boolean mDownHandle;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LogUtil.i(TAG, "onTouchEvent down x=" + event.getX() + " y=" + event.getY() + " state=" + mState);
                mState = STATE_TOUCH;
                break;
            case MotionEvent.ACTION_MOVE:
                LogUtil.i(TAG, "onTouchEvent move x=" + event.getX() + " y=" + event.getY());
                mState = STATE_TOUCH;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                LogUtil.i(TAG, "onTouchEvent up or cancel x=" + event.getX() + " y=" + event.getY());
                mState = STATE_IDLE;
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void fling(int velocityX) {
        mState = STATE_TOUCH;
        super.fling(velocityX / 4);
        int finalX = getScroller().getFinalX();
        int newFinalX = judgeScrollX(finalX);
        LogUtil.i(TAG, "fling finalX=" + finalX + " newFinalX=" + newFinalX);
        getScroller().setFinalX(newFinalX);
    }

    public void setSelectIndex(int index) {
        // TODO Auto-generated method stub
        LogUtil.i(TAG, "setSelectIndex index=" + index + " mSelectIndex=" + mSelectIndex + " mState=" + mState);
        if (index != mSelectIndex && mState == STATE_IDLE) {
            abortScroll();
            scrollToCenter(index);
        }
    }

}
