/*
 * Copyright (c) 2015, The Linux Foundation. All rights reserved.
 * Not a Contribution
 *
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.gallery3d.ui;

import android.graphics.Rect;
import android.os.Handler;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.app.AbstractGalleryActivity;
//import com.android.gallery3d.common.ApiHelper.SystemProperties;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.util.LogUtil;
import com.prize.slideselect.ISelectMode;

import java.util.Locale;

public class TimeLineSlotView extends GLView {
    @SuppressWarnings("unused")
    private static final String TAG = "TimeLineSlotView";

    public static final int INDEX_NONE = -1;
    private static final int mainKey = 1;//SystemProperties.getInt("qemu.hw.mainkeys", 1);

    public static final int RENDER_MORE_PASS = 1;
    public static final int RENDER_MORE_FRAME = 2;

    private int mWidth  = 0;

    public static final int OVERSCROLL_3D = 0;
    public static final int OVERSCROLL_SYSTEM = 1;
    public static final int OVERSCROLL_NONE = 2;
    private int mOverscrollEffect = OVERSCROLL_NONE;
    private final Paper mPaper = new Paper();

    public interface Listener {
        public void onDown(int index);
        public void onUp(boolean followedByLongPress);
        public void onSingleTapUp(Slot slot);
        public void onLongTap(Slot slot);
        public void onScrollPositionChanged(int position, int total);
    }

    public static class SimpleListener implements Listener {
        @Override public void onDown(int index) {}
        @Override public void onUp(boolean followedByLongPress) {}
        @Override public void onSingleTapUp(Slot slot) {}
        @Override public void onLongTap(Slot slot) {}
        @Override public void onScrollPositionChanged(int position, int total) {}
    }

    private final GestureDetector mGestureDetector;
    private final ScrollerHelper mScroller;

    private Listener mListener;
    private SlotAnimation mAnimation = null;
    private final Layout mLayout = new Layout();
    private int mStartIndex = INDEX_NONE;

    // whether the down action happened while the view is scrolling.
    private boolean mDownInScrolling;

    private TimeLineSlotRenderer mRenderer;

    private int[] mRequestRenderSlots = new int[16];

    // Flag to check whether it is come from Photo Page.
    private boolean isFromPhotoPage = false;
    private ISelectMode mSelectMode;
    private Handler mHandler;

    public TimeLineSlotView(AbstractGalleryActivity activity, Spec spec) {
        mGestureDetector = new GestureDetector(activity, new MyGestureListener());
        mScroller = new ScrollerHelper(activity);
        setSlotSpec(spec);
        mHandler = new SynchronizedHandler(activity.getGLRoot());
    }

    public void setSlotRenderer(TimeLineSlotRenderer slotDrawer) {
        mRenderer = slotDrawer;
        if (mRenderer != null) {
            mRenderer.onVisibleRangeChanged(getVisibleStart(), getVisibleEnd());
        }
    }

    public void setCenterIndex(int index) {
        int size = mLayout.getSlotSize();
        if (index < 0 || index >= size) {
            return;
        }
        Rect rect = mLayout.getSlotRect(index);
        if (rect != null) {
            int position = (rect.top + rect.bottom - getHeight()) / 2;
            setScrollPosition(position);
        }
    }

    public void setSelectMode(ISelectMode selectMode) {
        mSelectMode = selectMode;
    }

    private boolean isSelectMode() {
        if (mSelectMode != null) {
            return mSelectMode.isSelectMode();
        }
        return false;
    }

    public void makeSlotVisible(int index) {
        Rect rect = mLayout.getSlotRect(index);
        if (rect == null) return;
        int visibleBegin = mScrollY;
        int visibleLength = getHeight();
        int visibleEnd = visibleBegin + visibleLength;
        int slotBegin = rect.top;
        int slotEnd = rect.bottom;

        int position = visibleBegin;
        if (visibleLength < slotEnd - slotBegin) {
            position = visibleBegin;
        } else if (slotBegin < visibleBegin) {
            position = slotBegin;
        } else if (slotEnd > visibleEnd && mainKey == 1) {
            position = slotEnd - visibleLength;
        } else if (slotBegin > visibleEnd && mainKey == 0) {
            position = slotBegin - visibleLength;
        }

        setScrollPosition(position);
    }

    /**
     * Set the flag which used for check whether it is come from Photo Page.
     */
    public void setIsFromPhotoPage(boolean flag) {
        isFromPhotoPage = flag;
    }

    public void setScrollPosition(int position) {
        position = Utils.clamp(position, 0, mLayout.getScrollLimit());
        mScroller.setPositionY(position);
        updateScrollPosition(position, false);
    }

    public void setSlotSpec(Spec spec) {
        mLayout.setSlotSpec(spec);
    }

    @Override
    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        if (!changeSize) return;
        mWidth = r - l;

        // Make sure we are still at a reasonable scroll position after the size
        // is changed (like orientation change). We choose to keep the center
        // visible slot still visible. This is arbitrary but reasonable.
        int visibleIndex =
                (mLayout.getVisibleStart() + mLayout.getVisibleEnd()) / 2;
        mLayout.setSize(r - l, b - t);
        makeSlotVisible(visibleIndex);
        if (mOverscrollEffect == OVERSCROLL_3D) {
            mPaper.setSize(b - t, r - l);
        }
    }

    public void setOverscrollEffect(int kind) {
        mOverscrollEffect = kind;
        mScroller.setOverfling(kind == OVERSCROLL_SYSTEM);
    }

    public void startScatteringAnimation(RelativePosition position) {
        mAnimation = new ScatteringAnimation(position);
        mAnimation.start();
        if (mLayout.getSlotSize() != 0) invalidate();
    }

    public void startRisingAnimation() {
        mAnimation = new RisingAnimation();
        mAnimation.start();
        if (mLayout.getSlotSize() != 0) invalidate();
    }

    private void updateScrollPosition(int position, boolean force) {
        if (!force && (position == mScrollY)) return;
        mScrollY = position;
        mLayout.setScrollPosition(position);
        onScrollPositionChanged(position);
    }

    protected void onScrollPositionChanged(int newPosition) {
        int limit = mLayout.getScrollLimit();
        mListener.onScrollPositionChanged(newPosition, limit);
    }

    public Rect getSlotRect(int slotIndex) {
        return mLayout.getSlotRect(slotIndex);
    }

    private float mDownX;
    private float mDownY;
    private float mMoveX;
    private float mMoveY;
    private boolean mIsSlideSelect;
    private SlotCell mDownSlotCell;
    private SlotCell mCurrentCell;

    private void handleSlotCell(SlotCell slotCell) {
        LogUtil.i(TAG, "&&&&&&&&handleSlotCell slotCell=" + slotCell + " mDownSlotCell=" + mDownSlotCell + " mCurrentCell=" + mCurrentCell);
        if (slotCell == null || mDownSlotCell == null) {
            LogUtil.i(TAG, "&&&&&&&&handleSlotCell slotCell == null || mDownSlotCell == null");
            return;
        }
        if (mCurrentCell == null || !mCurrentCell.equals(slotCell)) {
            if (mCurrentCell == null) {
                if (mSelectMode != null) {
                    mSelectMode.slideControlStart();
                }
            }
            mCurrentCell = slotCell;
            publishSlotCell();
        }
    }

    private void publishSlotCell() {
        if (mSelectMode != null) {
            mSelectMode.slideControlSelect(true, mDownSlotCell.slotIndex, mCurrentCell.slotIndex);
        }
    }

    private void publishSlotCellEnd() {
        if (mSelectMode != null) {
            mSelectMode.slideControlEnd();
        }
    }

    public static class SlotCell {
        public int rowId;
        public int colId;
        public int slotIndex;

        public SlotCell(int rId, int cId, int index) {
            rowId = rId;
            colId = cId;
            slotIndex = index;
        }

        @Override
        public String toString() {
            return "[" + rowId + " ," + colId + " ," + slotIndex + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SlotCell slotCell = (SlotCell) o;

            if (rowId != slotCell.rowId) return false;
            return colId == slotCell.colId;

        }

        @Override
        public int hashCode() {
            int result = rowId;
            result = 31 * result + colId;
            return result;
        }
    }

    @Override
    protected boolean onTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownInScrolling = !mScroller.isFinished();
                mScroller.forceFinished();
                mDownX = event.getX();
                mDownY = event.getY();
                mIsSlideSelect = false;
                mGestureDetector.onTouchEvent(event);
                if (isSelectMode()) {
                    mDownSlotCell = mLayout.getSlotCellByPosition(mDownX, mDownY);
                    mCurrentCell = null;
                } else {
                    mDownSlotCell = null;
                }
                LogUtil.i(TAG, "&&&&&&&&onTouch down downx=" + mDownX + " downy=" + mDownY + " mDownSlotCell=" + mDownSlotCell);
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveX = event.getX();
                mMoveY = event.getY();
                if (mIsSlideSelect) {
                    SlotCell slotCell = mLayout.getSlotCellByPosition(mMoveX, mMoveY);
                    handleSlotCell(slotCell);
                    if (mMoveY < 0 && mScrollY != 0) {
                        if (!mScroller.isFinished()) {
                            mScroller.forceFinished();
                        }
                        mScroller.startScrollY(-mLayout.getSlotHeight() / 20, 0, mLayout.getScrollLimit());
                        invalidate();
                    } else if (mMoveY > mLayout.getViewHeight() - mLayout.getSlotHeight() && mScrollY != mLayout.getScrollLimit()) {
                        if (!mScroller.isFinished()) {
                            mScroller.forceFinished();
                        }
                        mScroller.startScrollY(mLayout.getSlotHeight() / 20, 0, mLayout.getScrollLimit());
                        invalidate();
                    } else {
                        mScroller.forceFinished();
                    }
                } else {
                    mGestureDetector.onTouchEvent(event);
                }
                LogUtil.i(TAG, "&&&&&&&&onTouch downx=" + mDownX + " downy=" + mDownY + "movex=" + mMoveX + " mMovey=" + mMoveY + " mIsSlideSelect=" + mIsSlideSelect);
                break;
            case MotionEvent.ACTION_UP:
                mPaper.onRelease();
                invalidate();
                float upX = event.getX();
                float upY = event.getY();
                if (mIsSlideSelect) {
                    SlotCell slotCell = mLayout.getSlotCellByPosition(upX, upY);
                    handleSlotCell(slotCell);
                    publishSlotCellEnd();
                } else {
                    mGestureDetector.onTouchEvent(event);
                }
                LogUtil.i(TAG, "&&&&&&&&onTouch up downx=" + mDownX + " downy=" + mDownY + " mIsSlideSelect=" + mIsSlideSelect);
                break;
        }
        return true;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private static int[] expandIntArray(int array[], int capacity) {
        while (array.length < capacity) {
            array = new int[array.length * 2];
        }
        return array;
    }

    @Override
    protected void render(GLCanvas canvas) {
        super.render(canvas);

        if (mRenderer == null) return;
        mRenderer.prepareDrawing();

        long animTime = AnimationTime.get();
        boolean more = mScroller.advanceAnimation(animTime);
        int oldX = mScrollY;
        updateScrollPosition(mScroller.getPositionY(), false);

        boolean paperActive = false;
        if (mOverscrollEffect == OVERSCROLL_3D) {
            // Check if an edge is reached and notify mPaper if so.
            int newX = mScrollX;
            newX = mScrollY;
            int limit = mLayout.getScrollLimit();
            if (oldX > 0 && newX == 0 || oldX < limit && newX == limit) {
                float v = mScroller.getCurrVelocity();
                if (newX == limit) v = -v;

                // I don't know why, but getCurrVelocity() can return NaN.
                if (!Float.isNaN(v)) {
                    mPaper.edgeReached(v);
                }
            }
            paperActive = mPaper.advanceAnimation();
        }
        more |= paperActive;

        if (mAnimation != null) {
            more |= mAnimation.calculate(animTime);
        }

        canvas.translate(-mScrollX, -mScrollY);

        int requestCount = 0;
        int requestedSlot[] = expandIntArray(mRequestRenderSlots,
                mLayout.getVisibleEnd() - mLayout.getVisibleStart());

        for (int i = mLayout.getVisibleEnd() - 1; i >= mLayout.getVisibleStart(); --i) {
            int r = renderItem(canvas, i, 0, paperActive);
            if ((r & RENDER_MORE_FRAME) != 0) more = true;
            if ((r & RENDER_MORE_PASS) != 0) requestedSlot[requestCount++] = i;
        }

        for (int pass = 1; requestCount != 0; ++pass) {
            int newCount = 0;
            for (int i = 0; i < requestCount; ++i) {
                int r = renderItem(canvas, requestedSlot[i], pass, paperActive);
                if ((r & RENDER_MORE_FRAME) != 0) more = true;
                if ((r & RENDER_MORE_PASS) != 0) requestedSlot[newCount++] = i;
            }
            requestCount = newCount;
        }

        canvas.translate(mScrollX, mScrollY);

        if (more) invalidate();

    }

    private int renderItem(GLCanvas canvas, int index, int pass, boolean paperActive) {
        Rect rect = mLayout.getSlotRect(index);
        if (rect == null) return 0;
        canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
        if (paperActive) {
            /// M: [FEATURE.MODIFY] fancy layout @{
            //canvas.multiplyMatrix(mPaper.getTransform(rect, mScrollX), 0);
            canvas.multiplyMatrix(mPaper.getTransformY(rect, mScrollY), 0);
            /// @}
        } else {
            canvas.translate(rect.left, rect.top, 0);
        }
        if (mAnimation != null && mAnimation.isActive()) {
            mAnimation.apply(canvas, index, rect);
        }
        int result = mRenderer.renderSlot(
                canvas, index, pass, rect.right - rect.left, rect.bottom - rect.top);
        canvas.restore();
        return result;
    }

    public static abstract class SlotAnimation extends Animation {
        protected float mProgress = 0;

        public SlotAnimation() {
            setInterpolator(new DecelerateInterpolator(4));
            setDuration(1500);
        }

        @Override
        protected void onCalculate(float progress) {
            mProgress = progress;
        }

        abstract public void apply(GLCanvas canvas, int slotIndex, Rect target);
    }

    public static class RisingAnimation extends SlotAnimation {
        private static final int RISING_DISTANCE = 128;

        @Override
        public void apply(GLCanvas canvas, int slotIndex, Rect target) {
            canvas.translate(0, 0, RISING_DISTANCE * (1 - mProgress));
        }
    }

    public static class ScatteringAnimation extends SlotAnimation {
        private int PHOTO_DISTANCE = 1000;
        private RelativePosition mCenter;

        public ScatteringAnimation(RelativePosition center) {
            mCenter = center;
        }

        @Override
        public void apply(GLCanvas canvas, int slotIndex, Rect target) {
            canvas.translate(
                    (mCenter.getX() - target.centerX()) * (1 - mProgress),
                    (mCenter.getY() - target.centerY()) * (1 - mProgress),
                    slotIndex * PHOTO_DISTANCE * (1 - mProgress));
            canvas.setAlpha(mProgress);
        }
    }

    private class MyGestureListener implements GestureDetector.OnGestureListener {
        private boolean isDown;
        private boolean isFirstScroll;

        // We call the listener's onDown() when our onShowPress() is called and
        // call the listener's onUp() when we receive any further event.
        @Override
        public void onShowPress(MotionEvent e) {
            if (isSelectMode()) {
                return;
            }
            GLRoot root = getGLRoot();
            if (root != null) {
                root.lockRenderThread();
                try {
                    if (isDown) return;
                    Slot slot = mLayout.getSlotByPosition(e.getX(), e.getY());
                    if (slot != null && !slot.isTitle) {
                        isDown = true;
                        mListener.onDown(slot.index);
                    }
                } finally {
                    root.unlockRenderThread();
                }
            }
        }

        private void cancelDown(boolean byLongPress) {
            if (!isDown) return;
            isDown = false;
            mListener.onUp(byLongPress);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            LogUtil.i(TAG, "&&&&&&&&onDown");
            isFirstScroll = true;
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1,
                MotionEvent e2, float velocityX, float velocityY) {
            LogUtil.i(TAG, "&&&&&&&&onFling velocityX=" + velocityX + " velocityY=" + velocityY);
            if (mIsSlideSelect) {
                return true;
            }
            LogUtil.i(TAG, "&&&&&&&&onFling start");
            cancelDown(false);
            int scrollLimit = mLayout.getScrollLimit();
            if (scrollLimit == 0) return false;
            mScroller.flingY((int) -velocityY / 2, 0, scrollLimit);
            invalidate();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1,
                MotionEvent e2, float distanceX, float distanceY) {
            LogUtil.i(TAG, "&&&&&&&&onScroll distanceX=" + distanceX + " distanceY=" + distanceY + " isFirstScroll=" + isFirstScroll);
            if (mIsSlideSelect || (isFirstScroll && (isSelectMode() && (Math.abs(distanceX) * 0.839f > Math.abs(distanceY))))) { // 40
                mIsSlideSelect = true;
                return true;
            }
            LogUtil.i(TAG, "&&&&&&&&onScroll start");
            isFirstScroll = false;
            cancelDown(false);
            int overDistance = mScroller.startScrollY(
                    Math.round(distanceY), 0, mLayout.getScrollLimit());
            if (mOverscrollEffect == OVERSCROLL_3D && overDistance != 0) {
                mPaper.overScroll(overDistance);
            }
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            cancelDown(false);
            if (mDownInScrolling) return true;
            Slot slot = mLayout.getSlotByPosition(e.getX(), e.getY());
            if (slot != null) {
                LogUtil.i(TAG, "onSingleTapUp slot.index=" + slot.index + " slot.isTitle=" + slot.isTitle + " slot.indexBase=" + slot.titleIndex + " slot.col=" + slot.col);
                mListener.onSingleTapUp(slot);
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (isSelectMode()) {
                mIsSlideSelect = true;
                return;
            }
            cancelDown(true);
            if (mDownInScrolling) return;
            lockRendering();
            try {
                Slot slot = mLayout.getSlotByPosition(e.getX(), e.getY());
                if (slot != null) {
                    mListener.onLongTap(slot);
                }
            } finally {
                unlockRendering();
            }
        }
    }

    public void setStartIndex(int index) {
        mStartIndex = index;
    }

    public void setSlotCount(int[] count) {
        mLayout.setSlotCount(count);

        // mStartIndex is applied the first time setSlotCount is called.
        if (mStartIndex != INDEX_NONE) {
            setCenterIndex(mStartIndex);
            mStartIndex = INDEX_NONE;
        }
        // Reset the scroll position to avoid scrolling over the updated limit.
        setScrollPosition(mScrollY);
    }

    public int getVisibleStart() {
        return mLayout.getVisibleStart();
    }

    public int getVisibleEnd() {
        return mLayout.getVisibleEnd();
    }

    public int getScrollX() {
        return mScrollX;
    }

    public int getScrollY() {
        return mScrollY;
    }

    public Rect getSlotRect(int slotIndex, GLView rootPane) {
        // Get slot rectangle relative to this root pane.
        Rect offset = new Rect();
        rootPane.getBoundsOf(this, offset);
        Rect r = getSlotRect(slotIndex);
        if (r != null) {
            r.offset(offset.left - getScrollX(),
                    offset.top - getScrollY());
            return r;
        }
        return offset;
    }

    public int getTitleWidth() {
        return mWidth;
    }

    // This Spec class is used to specify the size of each slot in the SlotView.
    // There are two ways to do it:
    //
    // Specify colsLand, colsPort, and slotGap: they specify the number
    // of rows in landscape/portrait mode and the gap between slots. The
    // width and height of each slot is determined automatically.
    //
    // The initial value of -1 means they are not specified.
    public static class Spec {
        public int colsLand = -1;
        public int colsPort = -1;
        public int titleHeight = -1;
        public int slotGapPort = -1;
        public int slotGapLand = -1;
    }

    public class Layout {
        private int mVisibleStart;
        private int mVisibleEnd;

        public int mSlotSize;
        private int mSlotWidth;
        private int mSlotHeight;
        private int mSlotGap;
        private int[] mSlotCount;

        private Spec mSpec;

        private int mWidth;
        private int mHeight;

        private int mUnitCount;
        private int mContentLength;
        private int mScrollPosition;

        public void setSlotSpec(TimeLineSlotView.Spec spec) {
            mSpec = spec;
        }

        public void setSlotCount(int[] count) {
            mSlotCount = count;
            if (mHeight != 0) {
                initLayoutParameters();
                createSlots();
            }
        }

        public int getSlotSize() {
            return mSlotSize;
        }

        public int getSlotHeight() {
            return  mSlotHeight;
        }

        public int getViewHeight() {
            return  mHeight;
        }

        public Rect getSlotRect(int index) {
            if (index >= mVisibleStart && index < mVisibleEnd && mVisibleEnd != 0) {
                int height = 0, base = 0, top = 0;
                for (int count : mSlotCount) {
                    if (index == base) {
                        return getSlotRect(getSlot(true, index, index, top));
                    }
                    top += mSpec.titleHeight;
                    ++base;

                    if (index >= base && index < base + count) {
                        return getSlotRect(getSlot(false, index, base, top));
                    }
                    int rows = (count + mUnitCount - 1) / mUnitCount;
                    top += mSlotHeight * rows + mSlotGap * (rows > 0 ? rows - 1 : 0);
                    base += count;
                }
            }
            return null;
        }

        private void initLayoutParameters() {
            initLayoutParameters(true);
        }

        private void initLayoutParameters(boolean isWidthChange) {
            mUnitCount = (mWidth > mHeight) ? mSpec.colsLand : mSpec.colsPort;
            mSlotGap = (mWidth > mHeight) ? mSpec.slotGapLand: mSpec.slotGapPort;
            mSlotWidth = Math.round((mWidth - (mUnitCount + 1) * mSlotGap) / mUnitCount);
            mSlotHeight = mSlotWidth;
            if (mRenderer != null && isWidthChange) {
                mRenderer.onSlotSizeChanged(mSlotWidth, mSlotHeight);
            }
        }

        private void setSize(int width, int height) {
            if (width != mWidth || height != mHeight) {
                boolean isChange = width != mWidth;
                mWidth = width;
                mHeight = height;
                initLayoutParameters(isChange);
                createSlots();
            }
        }

        public void setScrollPosition(int position) {
            if (mScrollPosition == position) return;
            mScrollPosition = position;
            updateVisibleSlotRange();
            if (mIsSlideSelect) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SlotCell slotCell = getSlotCellByPosition(mMoveX, mMoveY);
                        handleSlotCell(slotCell);
                    }
                });
            }
        }

        private Rect getSlotRect(Slot slot) {
            int x, y, w, h;
            if (slot.isTitle) {
                x = 0;
                y = slot.top;
                w = mWidth;
                h = mSpec.titleHeight;
            } else {
                x = slot.col * mSlotWidth + mSlotGap * (slot.col + 1);
                y = slot.top;
                w = mSlotWidth;
                h = mSlotHeight;
            }
            return new Rect(x, y, x + w, y + h);
        }

        private synchronized void updateVisibleSlotRange() {
            int position = mScrollPosition;
            if (mSlotCount != null) {
                Slot begin = getSlotByPosition(0, mScrollPosition, true, false),
                        end = getSlotByPosition(0, mScrollPosition + mHeight, true, true);
                LogUtil.i(TAG, "updateVisibleSlotRange end=" + end.index);
                if (begin == null && end != null && end.index == 0) {
                    setVisibleRange(0, 0);
                } else if (begin != null && end != null) {
                    setVisibleRange(begin.index, end.index);
                }
            }
        }

        private void setVisibleRange(int start, int end) {
            if (start == mVisibleStart && end == mVisibleEnd) return;
            if (start < end) {
                mVisibleStart = start;
                mVisibleEnd = end;
            } else {
                mVisibleStart = mVisibleEnd = 0;
            }
            if (mRenderer != null) {
                mRenderer.onVisibleRangeChanged(mVisibleStart, mVisibleEnd);
            }
        }

        public int getVisibleStart() {
            return mVisibleStart;
        }

        public int getVisibleEnd() {
            return mVisibleEnd;
        }

        private Slot getSlot(boolean isTitle, int index, int indexBase, int top) {
            if (isTitle) {
                return new Slot(true, index, indexBase, 0, top);
            } else {
                int row = (index - indexBase) / mUnitCount;
                return new Slot(false, index, indexBase, (index - indexBase) % mUnitCount,
                        top + row * (mSlotHeight + mSlotGap));
            }
        }

        public SlotCell getSlotCellByPosition(float x, float y) {
            Slot slot = null;
            SlotCell slotCell = null;
            int absoluteX = Math.round(x);
            int absoluteY = Math.round(y) + mScrollPosition;
            if (absoluteX < 0 || absoluteY < 0 || mSlotCount == null) {
                return null;
            }
            int pos = (int) absoluteY, index = 0, top = 0;
            for (int count : mSlotCount) {
                int h = mSpec.titleHeight;
                if (pos < top + h) {
                    LogUtil.i(TAG, "&&&&&&&& index=" + index + " top=" + top);
                    slot = getSlot(true, index, index, top);
                    break;
                }
                top += h;
                ++index;

                int rows = (count + mUnitCount - 1) / mUnitCount;
                h = mSlotHeight * rows + mSlotGap * (rows > 0 ? rows - 1 : 0);
                if (pos < top + h) {
                    int row = ((int) pos - top) / (mSlotHeight + mSlotGap);
                    int col = ((int) absoluteX) / (mSlotWidth + mSlotGap);
                    LogUtil.i(TAG, "&&&&&&&& row=" + row + " col=" + col + " mUnitCount=" + mUnitCount + " count=" + count + " top=" + top);
                    if (row * mUnitCount + col >= count) {
                        col = (count - 1) % mUnitCount;
                    }
                    LogUtil.i(TAG, "&&&&&&&& index=" + index + " col=" + col);
                    slot = getSlot(false, index + row * mUnitCount + col, index, top);
                    break;
                }
                top += h;
                index += count;
            }
            LogUtil.i(TAG, "&&&&&&&& absoluteY=" + absoluteY + " top=" + top + " index=" + index + " mContentLength=" + mContentLength);
            if (slot == null) {
                slotCell = new SlotCell(0, index, index);
            } else {
                slotCell = new SlotCell(slot.col, slot.index, slot.index);
            }
            return slotCell;
        }

        public Slot getSlotByPosition(float x, float y, boolean rowStart, boolean roundUp) {
            if (x < 0 || y < 0 || mSlotCount == null) {
                return null;
            }
            int pos = (int) y, index = 0, top = 0;
            for (int count : mSlotCount) {
                int h = mSpec.titleHeight;
                if (pos < top + h) {
                    if (roundUp) {
                        return getSlot(false, index + 1, index, top + h);
                    } else {
                        return getSlot(true, index, index, top);
                    }
                }
                top += h;
                ++index;

                int rows = (count + mUnitCount - 1) / mUnitCount;
                h = mSlotHeight * rows + mSlotGap * (rows > 0 ? rows - 1 : 0);
                if (pos < top + h) {
                    int row = ((int) pos - top) / (mSlotHeight + mSlotGap);
                    int col = 0;
                    if (roundUp) {
                        int idx = (row + 1) * mUnitCount;
                        if (idx > count)
                            idx = count + 1;
                        return getSlot(false, index + idx, index, top + mSlotHeight);
                    }
                    if (!rowStart) {
                        col = ((int) x) / (mSlotWidth + mSlotGap);
                        if (row * mUnitCount + col >= count) {
                            break;
                        }
                    }
                    return getSlot(false, index + row * mUnitCount + col, index, top);
                }
                top += h;
                index += count;
            }
            if (roundUp) {
                return getSlot(false, index, index, top);
            }
            return null;
        }

        public Slot getSlotByPosition(float x, float y) {
            return getSlotByPosition(x, mScrollPosition + y, false, false);
        }

        public int getScrollLimit() {
            return Math.max(0, mContentLength - mHeight);
        }

        public void createSlots() {
            int height = 0;
            int size = 0;
            if (mSlotCount != null) {
                for (int count : mSlotCount) {
                    int rows = (count + mUnitCount - 1) / mUnitCount;
                    height += mSlotHeight * rows + mSlotGap * (rows > 0 ? rows - 1 : 0);
                    size += 1 + count;
                }
                height += mSpec.titleHeight * mSlotCount.length + mSlotGap;
                mContentLength = height;
                mSlotSize = size;
                updateVisibleSlotRange();
            }
        }
    }

    public static class Slot {
        public boolean isTitle;
        public int index;
        public int titleIndex;
        public int col;
        public int top;

        public Slot(boolean isTitle, int index, int indexBase, int col, int top) {
            this.isTitle = isTitle;
            this.index = index;
            this.titleIndex = isTitle ? index : indexBase - 1;
            this.col = col;
            this.top = top;
        }
    }
}
