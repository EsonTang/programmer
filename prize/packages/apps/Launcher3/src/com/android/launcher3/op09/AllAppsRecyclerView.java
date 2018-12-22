/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.launcher3.allapps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
/// M: Add for OP customization.
import android.view.MotionEvent;
import android.view.View;

import com.android.launcher3.BaseRecyclerView;
/// M: Add for OP customization.
import com.android.launcher3.BaseRecyclerViewFastScrollBar;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DeviceProfile;
/// M: Add for OP customization. @{
import com.android.launcher3.folder.FolderIcon;
//import com.android.launcher3.FolderIcon.FolderRingAnimator;
import com.android.launcher3.folder.FolderIcon.FolderRingAnimator;
/// @}
import com.android.launcher3.Launcher;
/// M: Add for OP customization.
import com.android.launcher3.LauncherExtPlugin;
import com.android.launcher3.R;
/// M: Add for OP customization.
import com.android.launcher3.op.LauncherLog;
import com.android.launcher3.userevent.nano.LauncherLogProto;
/// M: Add for OP customization.
import com.android.launcher3.util.Thunk;

/// M: Add for OP customization.
import java.util.ArrayList;
import java.util.List;

/**
 * A RecyclerView with custom fast scroll support for the all apps view.
 */
public class AllAppsRecyclerView extends BaseRecyclerView {
    /// M: Add for OP customization.
    static final String TAG = "Launcher3.AllAppsRecyclerView";

    private AlphabeticalAppsList mApps;
    private AllAppsFastScrollHelper mFastScrollHelper;
    private int mNumAppsPerRow;

    // The specific view heights that we use to calculate scroll
    private SparseIntArray mViewHeights = new SparseIntArray();
    private SparseIntArray mCachedScrollPositions = new SparseIntArray();

    // The empty-search result background
    private AllAppsBackgroundDrawable mEmptySearchBackground;
    private int mEmptySearchBackgroundTopOffset;

    private HeaderElevationController mElevationController;

    /// M: Add for OP customization. @{
    private ScrollPositionState mScrollPosState =
            new ScrollPositionState();
    @Thunk int mFastScrollFrameIndex;
    @Thunk final int[] mFastScrollFrames = new int[10];

    private boolean mSupportEditAndHideApps;
    private Drawable mCrosshairsDrawable;
    private int[] mCellSize = new int[2];
    private int[] mCrrentDragCell = {-1, -1};
    private float mLastTouchY = 0;

    private ArrayList<FolderRingAnimator> mFolderOuterRings =
        new ArrayList<FolderRingAnimator>();
    private Drawable sSharedOuterRingDrawable;
    private Drawable sSharedInnerRingDrawable;
    DeviceProfile mGrid;

    private int mNumAppRowsPerPage; // The row number in one page
    public int mTargetPage = 0; // The current target page
    /// @}

    public AllAppsRecyclerView(Context context) {
        this(context, null);
    }

    public AllAppsRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AllAppsRecyclerView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr);
        Resources res = getResources();
        addOnItemTouchListener(this);
        mScrollbar.setDetachThumbOnFastScroll();
        mEmptySearchBackgroundTopOffset = res.getDimensionPixelSize(
                R.dimen.all_apps_empty_search_bg_top_offset);

        /// M: Add for OP customization. @{
        mSupportEditAndHideApps = LauncherExtPlugin.getInstance()
                .supportEditAndHideApps();
        Resources resources = context.getResources();
        mCrosshairsDrawable = resources.getDrawable(R.drawable.ic_launcher_sort);
        sSharedOuterRingDrawable = resources.getDrawable(R.drawable.portal_ring_outer);
        sSharedInnerRingDrawable = resources.getDrawable(R.drawable.portal_ring_inner_nolip);
        /// @}
    }

    /**
     * Sets the list of apps in this view, used to determine the fastscroll position.
     */
    public void setApps(AlphabeticalAppsList apps) {
        mApps = apps;
        mFastScrollHelper = new AllAppsFastScrollHelper(this, apps);
    }

    public void setElevationController(HeaderElevationController elevationController) {
        mElevationController = elevationController;
    }

    /**
     * Sets the number of apps per row in this recycler view.
     */
    public void setNumAppsPerRow(DeviceProfile grid, int numAppsPerRow) {
        mNumAppsPerRow = numAppsPerRow;

        RecyclerView.RecycledViewPool pool = getRecycledViewPool();
        int approxRows = (int) Math.ceil(grid.availableHeightPx / grid.allAppsIconSizePx);
        pool.setMaxRecycledViews(AllAppsGridAdapter.VIEW_TYPE_EMPTY_SEARCH, 1);
        pool.setMaxRecycledViews(AllAppsGridAdapter.VIEW_TYPE_SEARCH_DIVIDER, 1);
        pool.setMaxRecycledViews(AllAppsGridAdapter.VIEW_TYPE_SEARCH_MARKET_DIVIDER, 1);
        pool.setMaxRecycledViews(AllAppsGridAdapter.VIEW_TYPE_SEARCH_MARKET, 1);
        pool.setMaxRecycledViews(AllAppsGridAdapter.VIEW_TYPE_ICON, approxRows * mNumAppsPerRow);
        pool.setMaxRecycledViews(AllAppsGridAdapter.VIEW_TYPE_PREDICTION_ICON, mNumAppsPerRow);
        pool.setMaxRecycledViews(AllAppsGridAdapter.VIEW_TYPE_PREDICTION_DIVIDER, 1);
        pool.setMaxRecycledViews(AllAppsGridAdapter.VIEW_TYPE_SECTION_BREAK, approxRows);
        /// M: Add for OP customization.
        pool.setMaxRecycledViews(AllAppsGridAdapter.FOLDER_VIEW_TYPE, approxRows * mNumAppsPerRow);
    }

    /**
     * Ensures that we can present a stable scrollbar for views of varying types by pre-measuring
     * all the different view types.
     */
    public void preMeasureViews(AllAppsGridAdapter adapter) {
        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                getResources().getDisplayMetrics().widthPixels, View.MeasureSpec.AT_MOST);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                getResources().getDisplayMetrics().heightPixels, View.MeasureSpec.AT_MOST);

        // Icons
        BubbleTextView icon = (BubbleTextView) adapter.onCreateViewHolder(this,
                AllAppsGridAdapter.VIEW_TYPE_ICON).mContent;
        int iconHeight = icon.getLayoutParams().height;
        mViewHeights.put(AllAppsGridAdapter.VIEW_TYPE_ICON, iconHeight);
        mViewHeights.put(AllAppsGridAdapter.VIEW_TYPE_PREDICTION_ICON, iconHeight);

        // Search divider
        View searchDivider = adapter.onCreateViewHolder(this,
                AllAppsGridAdapter.VIEW_TYPE_SEARCH_DIVIDER).mContent;
        searchDivider.measure(widthMeasureSpec, heightMeasureSpec);
        int searchDividerHeight = searchDivider.getMeasuredHeight();
        mViewHeights.put(AllAppsGridAdapter.VIEW_TYPE_SEARCH_DIVIDER, searchDividerHeight);

        // Generic dividers
        View divider = adapter.onCreateViewHolder(this,
                AllAppsGridAdapter.VIEW_TYPE_PREDICTION_DIVIDER).mContent;
        divider.measure(widthMeasureSpec, heightMeasureSpec);
        int dividerHeight = divider.getMeasuredHeight();
        mViewHeights.put(AllAppsGridAdapter.VIEW_TYPE_PREDICTION_DIVIDER, dividerHeight);
        mViewHeights.put(AllAppsGridAdapter.VIEW_TYPE_SEARCH_MARKET_DIVIDER, dividerHeight);

        // Search views
        View emptySearch = adapter.onCreateViewHolder(this,
                AllAppsGridAdapter.VIEW_TYPE_EMPTY_SEARCH).mContent;
        emptySearch.measure(widthMeasureSpec, heightMeasureSpec);
        mViewHeights.put(AllAppsGridAdapter.VIEW_TYPE_EMPTY_SEARCH,
                emptySearch.getMeasuredHeight());
        View searchMarket = adapter.onCreateViewHolder(this,
                AllAppsGridAdapter.VIEW_TYPE_SEARCH_MARKET).mContent;
        searchMarket.measure(widthMeasureSpec, heightMeasureSpec);
        mViewHeights.put(AllAppsGridAdapter.VIEW_TYPE_SEARCH_MARKET,
                searchMarket.getMeasuredHeight());

        // Section breaks
        mViewHeights.put(AllAppsGridAdapter.VIEW_TYPE_SECTION_BREAK, 0);
    }

    /**
     * Scrolls this recycler view to the top.
     */
    public void scrollToTop() {
        // Ensure we reattach the scrollbar if it was previously detached while fast-scrolling
        if (mScrollbar.isThumbDetached()) {
            mScrollbar.reattachThumbToScroll();
        }
        scrollToPosition(0);
        if (mElevationController != null) {
            mElevationController.reset();
        }
    }

    /**
     * We need to override the draw to ensure that we don't draw the overscroll effect beyond the
     * background bounds.
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        // Clip to ensure that we don't draw the overscroll effect beyond the background bounds
        canvas.clipRect(mBackgroundPadding.left, mBackgroundPadding.top,
                getWidth() - mBackgroundPadding.right,
                getHeight() - mBackgroundPadding.bottom);

        /// M: Add for OP customization. @{
        if (Launcher.isInEditMode() && getChildCount() > 0) {
            int rowCount = mApps.getNumAppRows();

            // Draw cross drawable when in edit mode.
            final int countX = mNumAppsPerRow;
            if (mCellSize[0] == 0 || mCellSize[1] == 0) {
                getCellSize();
            }

            final int countY = getHeight() / mCellSize[1];

            mNumAppRowsPerPage = countY;

            final Drawable d = mCrosshairsDrawable;
            final int crossWidth = d.getIntrinsicWidth();
            final int crossHeight = d.getIntrinsicHeight();
            final int measuredWidth = getMeasuredWidth();
            final int measuredHeight = getMeasuredHeight();
            final int colGap = mCellSize[0]; //(measuredWidth - crossWidth) / countX;
            final int rowGap = mCellSize[1]; //(measuredHeight - crossHeight) / countY;

            int x = 0;
            for (int col = 0; col <= countX; col++) {
                int y = getScrollY();
                for (int row = 0; row <= countY; row++) {
                    d.setBounds(x, y, x + crossWidth, y + crossHeight);
                    d.draw(canvas);
                    y += rowGap;
                }
                x += colGap;
            }

            if (mCrrentDragCell[0] != -1 && mCrrentDragCell[1] != -1 && mGrid != null) {
                int previewOffset = FolderRingAnimator.sPreviewSize;
                for (int i = 0; i < mFolderOuterRings.size(); i++) {
                    FolderRingAnimator fra = mFolderOuterRings.get(i);
                    int[] tempLocation = new int[2];

                    Drawable drawable;
                    int width, height;
                    cellToPoint(fra.mCellX, fra.mCellY, tempLocation);
                    View child = getChildAt(fra.mCellX + fra.mCellY * mNumAppsPerRow);

                    if (child != null) {
                        int centerX = tempLocation[0] + mCellSize[0] / 2;
                        int centerY = tempLocation[1] + previewOffset / 2
                                + child.getPaddingTop() + mGrid.folderBackgroundOffset;

                        // Draw outer ring when it exists.
                        if (FolderIcon.HAS_OUTER_RING) {
                            drawable = FolderRingAnimator.sSharedOuterRingDrawable;
                            width = (int) (fra.getOuterRingSize() * 1);
                            height = width;
                            canvas.save();
                            canvas.translate(centerX - width / 2, centerY - height / 2);
                            drawable.setBounds(0, 0, width, height);
                            drawable.draw(canvas);
                            canvas.restore();
                        }

                        // Draw inner ring.
                        drawable = FolderRingAnimator.sSharedInnerRingDrawable;
                        width = (int) (fra.getInnerRingSize() * 1);
                        height = width;
                        canvas.save();
                        canvas.translate(centerX - width / 2, centerY - width / 2);
                        drawable.setBounds(0, 0, width, height);
                        drawable.draw(canvas);
                        canvas.restore();
                    }
                }
            }
        }
        /// @}

        super.dispatchDraw(canvas);
    }

    @Override
    public void onDraw(Canvas c) {
        // Draw the background
        if (mEmptySearchBackground != null && mEmptySearchBackground.getAlpha() > 0) {
            c.clipRect(mBackgroundPadding.left, mBackgroundPadding.top,
                    getWidth() - mBackgroundPadding.right,
                    getHeight() - mBackgroundPadding.bottom);

            mEmptySearchBackground.draw(c);
        }

        super.onDraw(c);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mEmptySearchBackground || super.verifyDrawable(who);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateEmptySearchBackgroundBounds();
    }

    public int getContainerType(View v) {
        if (mApps.hasFilter()) {
            return LauncherLogProto.SEARCHRESULT;
        } else {
            if (v instanceof BubbleTextView) {
                BubbleTextView icon = (BubbleTextView) v;
                int position = getChildPosition(icon);
                if (position != NO_POSITION) {
                    List<AlphabeticalAppsList.AdapterItem> items = mApps.getAdapterItems();
                    AlphabeticalAppsList.AdapterItem item = items.get(position);
                    if (item.viewType == AllAppsGridAdapter.VIEW_TYPE_PREDICTION_ICON) {
                        return LauncherLogProto.PREDICTION;
                    }
                }
            }
            return LauncherLogProto.ALLAPPS;
        }
    }

    public void onSearchResultsChanged() {
        // Always scroll the view to the top so the user can see the changed results
        scrollToTop();

        if (mApps.hasNoFilteredResults()) {
            if (mEmptySearchBackground == null) {
                mEmptySearchBackground = new AllAppsBackgroundDrawable(getContext());
                mEmptySearchBackground.setAlpha(0);
                mEmptySearchBackground.setCallback(this);
                updateEmptySearchBackgroundBounds();
            }
            mEmptySearchBackground.animateBgAlpha(1f, 150);
        } else if (mEmptySearchBackground != null) {
            // For the time being, we just immediately hide the background to ensure that it does
            // not overlap with the results
            mEmptySearchBackground.setBgAlpha(0f);
        }
    }

    /**
     * Maps the touch (from 0..1) to the adapter position that should be visible.
     */
    /// fast scroll
    @Override
    public String scrollToPositionAtProgress(float touchFraction) {
        int rowCount = mApps.getNumAppRows();
        if (rowCount == 0) {
            return "";
        }

        // Stop the scroller if it is scrolling
        stopScroll();

        // Find the fastscroll section that maps to this touch fraction
        List<AlphabeticalAppsList.FastScrollSectionInfo> fastScrollSections =
                mApps.getFastScrollerSections();
        AlphabeticalAppsList.FastScrollSectionInfo lastInfo = fastScrollSections.get(0);
        for (int i = 1; i < fastScrollSections.size(); i++) {
            AlphabeticalAppsList.FastScrollSectionInfo info = fastScrollSections.get(i);
            if (info.touchFraction > touchFraction) {
                break;
            }
            lastInfo = info;
        }

        // Update the fast scroll
        int scrollY = getCurrentScrollY();
        int availableScrollHeight = getAvailableScrollHeight();
        mFastScrollHelper.smoothScrollToSection(scrollY, availableScrollHeight, lastInfo);
        return lastInfo.sectionName;
    }

    @Override
    public void onFastScrollCompleted() {
        super.onFastScrollCompleted();
        mFastScrollHelper.onFastScrollCompleted();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onChanged() {
                mCachedScrollPositions.clear();
            }
        });
        mFastScrollHelper.onSetAdapter((AllAppsGridAdapter) adapter);
    }

    /**
     * Updates the bounds for the scrollbar.
     */
    @Override
    public void onUpdateScrollbar(int dy) {
        Log.d("OP09Launcher-AllAppsRecyclerView.onUpdateScrollbar", "dy: " + dy);
        List<AlphabeticalAppsList.AdapterItem> items = mApps.getAdapterItems();

        // Skip early if there are no items or we haven't been measured
        if (items.isEmpty() || mNumAppsPerRow == 0) {
            // do not show scroll bar
            mScrollbar.setThumbOffset(-1, -1);
            return;
        }

        // Skip early if, there no child laid out in the container.
        int scrollY = getCurrentScrollY();
        if (scrollY < 0) {
            mScrollbar.setThumbOffset(-1, -1);
            return;
        }

        // Only show the scrollbar if there is height to be scrolled
        int availableScrollBarHeight = getAvailableScrollBarHeight();
        int availableScrollHeight = getAvailableScrollHeight();
        if (availableScrollHeight <= 0) {
            mScrollbar.setThumbOffset(-1, -1);
            return;
        }

        if (mScrollbar.isThumbDetached()) {
            if (!mScrollbar.isDraggingThumb()) {
                // Calculate the current scroll position, the scrollY of the recycler view accounts
                // for the view padding, while the scrollBarY is drawn right up to the background
                // padding (ignoring padding)
                int scrollBarX = getScrollBarX();
                // scroll bar view y position
                int scrollBarY = mBackgroundPadding.top +
                        (int) (((float) scrollY / availableScrollHeight) *
                                availableScrollBarHeight);

                int thumbScrollY = mScrollbar.getThumbOffset().y;
                int diffScrollY = scrollBarY - thumbScrollY;
                if (diffScrollY * dy > 0f) {
                    // User is scrolling in the same direction the thumb needs to catch up to the
                    // current scroll position.  We do this by mapping the difference in movement
                    // from the original scroll bar position to the difference in movement necessary
                    // in the detached thumb position to ensure that both speed towards the same
                    // position at either end of the list.
                    if (dy < 0) {
                        int offset = (int) ((dy * thumbScrollY) / (float) scrollBarY);
                        thumbScrollY += Math.max(offset, diffScrollY);
                    } else {
                        int offset = (int) ((dy * (availableScrollBarHeight - thumbScrollY)) /
                                (float) (availableScrollBarHeight - scrollBarY));
                        thumbScrollY += Math.min(offset, diffScrollY);
                    }
                    thumbScrollY = Math.max(0, Math.min(availableScrollBarHeight, thumbScrollY));
                    mScrollbar.setThumbOffset(scrollBarX, thumbScrollY);
                    if (scrollBarY == thumbScrollY) {
                        mScrollbar.reattachThumbToScroll();
                    }
                } else {
                    // User is scrolling in an opposite direction to the direction that the thumb
                    // needs to catch up to the scroll position.  Do nothing except for updating
                    // the scroll bar x to match the thumb width.
                    mScrollbar.setThumbOffset(scrollBarX, thumbScrollY);
                }
            }
        } else {
            synchronizeScrollBarThumbOffsetToViewScroll(scrollY, availableScrollHeight);
        }
    }

    @Override
    protected boolean supportsFastScrolling() {
        // Only allow fast scrolling when the user is not searching, since the results are not
        // grouped in a meaningful order
        return !mApps.hasFilter();
    }

    @Override
    public int getCurrentScrollY() {
        // Return early if there are no items or we haven't been measured
        List<AlphabeticalAppsList.AdapterItem> items = mApps.getAdapterItems();
        if (items.isEmpty() || mNumAppsPerRow == 0 || getChildCount() == 0) {
            return -1;
        }

        // Calculate the y and offset for the item
        View child = getChildAt(0);
        int position = getChildPosition(child);
        if (position == NO_POSITION) {
            return -1;
        }
        return getCurrentScrollY(position, getLayoutManager().getDecoratedTop(child));
    }

    public int getCurrentScrollY(int position, int offset) {
        List<AlphabeticalAppsList.AdapterItem> items = mApps.getAdapterItems();
        AlphabeticalAppsList.AdapterItem posItem = position < items.size() ?
                items.get(position) : null;

        int y = mCachedScrollPositions.get(position, -1);

        if (posItem != null) {
        }
        if (y < 0) {
            y = 0;
            for (int i = 0; i < position; i++) {
                AlphabeticalAppsList.AdapterItem item = items.get(i);
                if (AllAppsGridAdapter.isIconViewType(item.viewType)) {
                    // Break once we reach the desired row
                    if (posItem != null && posItem.viewType == item.viewType &&
                            posItem.rowIndex == item.rowIndex) {
                        break;
                    }
                    // Otherwise, only account for the first icon in the row since they are the same
                    // size within a row
                    if (item.rowAppIndex == 0) {
                        y += mViewHeights.get(item.viewType, 0);
                    }
                } else {
                    // Rest of the views span the full width
                    y += mViewHeights.get(item.viewType, 0);
                }
            }
            mCachedScrollPositions.put(position, y);
        }

        return getPaddingTop() + y - offset;
    }

    @Override
    protected int getVisibleHeight() {
        return super.getVisibleHeight()
                - Launcher.getLauncher(getContext()).getDragLayer().getInsets().bottom;
    }

    /**
     * Returns the available scroll height:
     *   AvailableScrollHeight = Total height of the all items - last page height
     */
    @Override
    protected int getAvailableScrollHeight() {
        int paddedHeight = getCurrentScrollY(mApps.getAdapterItems().size(), 0);
        int totalHeight = paddedHeight + getPaddingBottom();
        return totalHeight - getVisibleHeight();
    }

    /**
     * Updates the bounds of the empty search background.
     */
    private void updateEmptySearchBackgroundBounds() {
        if (mEmptySearchBackground == null) {
            return;
        }

        // Center the empty search background on this new view bounds
        int x = (getMeasuredWidth() - mEmptySearchBackground.getIntrinsicWidth()) / 2;
        int y = mEmptySearchBackgroundTopOffset;
        mEmptySearchBackground.setBounds(x, y,
                x + mEmptySearchBackground.getIntrinsicWidth(),
                y + mEmptySearchBackground.getIntrinsicHeight());
    }

    /// M: Add for OP customization. @{

    /**
     * This runnable runs a single frame of the smooth scroll animation and posts the next frame
     * if necessary.
     */
    @Thunk Runnable mSmoothSnapNextFrameRunnable = new Runnable() {
        @Override
        public void run() {
            if (mFastScrollFrameIndex < mFastScrollFrames.length) {
                scrollBy(0, mFastScrollFrames[mFastScrollFrameIndex]);
                mFastScrollFrameIndex++;
                postOnAnimation(mSmoothSnapNextFrameRunnable);
            }
        }
    };

    /**
     * Smoothly snaps to a given position.  We do this manually by calculating the keyframes
     * ourselves and animating the scroll on the recycler view.
     */
    public void smoothSnapToPosition(final int position, ScrollPositionState scrollPosState) {
        removeCallbacks(mSmoothSnapNextFrameRunnable);

        // Calculate the full animation from the current scroll position to the final scroll
        // position, and then run the animation for the duration.
        int curScrollY = getPaddingTop() +
                (scrollPosState.rowIndex * scrollPosState.rowHeight) - scrollPosState.rowTopOffset;
        int newScrollY = getScrollAtPosition(position, scrollPosState.rowHeight);
        if (scrollPosState.rowIndex < 0 || scrollPosState.rowHeight <= 0) {
            LauncherLog.d(TAG, "smoothSnapToPosition: curScrollY=" + curScrollY
                + ", newScrollY=" + newScrollY
                + ", getPaddingTop=" + getPaddingTop()
                + ", rowIndex=" + scrollPosState.rowIndex
                + ", rowHeight=" + scrollPosState.rowHeight
                + ", rowTopOffset=" + scrollPosState.rowTopOffset
                + ", position=" + position);
        }
        int numFrames = mFastScrollFrames.length;
        for (int i = 0; i < numFrames; i++) {
            // TODO(winsonc): We can interpolate this as well.
            /// M: Modify for OP customization. @{
            if (mSupportEditAndHideApps) {
                if (i == (numFrames - 1)) {
                    mFastScrollFrames[i] = (newScrollY - curScrollY)
                        - mFastScrollFrames[0] * (numFrames - 1);
                } else {
                    mFastScrollFrames[i] = (newScrollY - curScrollY) / numFrames;
                }
            } else {
                mFastScrollFrames[i] = (newScrollY - curScrollY) / numFrames;
            }
            /// @}
        }
        mFastScrollFrameIndex = 0;
        postOnAnimation(mSmoothSnapNextFrameRunnable);
    }

    /**
     * Returns the current scroll state of the apps rows.
     */
    public void getCurScrollState(ScrollPositionState stateOut) {
        stateOut.rowIndex = -1;
        stateOut.rowTopOffset = -1;
        stateOut.rowHeight = -1;

        // Return early if there are no items or we haven't been measured
        List<AlphabeticalAppsList.AdapterItem> items = mApps.getAdapterItems();
        if (items.isEmpty() || mNumAppsPerRow == 0) {
            return;
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int position = getChildPosition(child);
            if (position != NO_POSITION) {
                AlphabeticalAppsList.AdapterItem item = items.get(position);
                if (item.viewType == AllAppsGridAdapter.VIEW_TYPE_ICON ||
                        item.viewType == AllAppsGridAdapter.VIEW_TYPE_PREDICTION_ICON
                        /// M: Add for OP customization.
                        || item.viewType == AllAppsGridAdapter.FOLDER_VIEW_TYPE) {
                    stateOut.rowIndex = item.rowIndex;
                    stateOut.rowTopOffset = getLayoutManager().getDecoratedTop(child);
                    stateOut.rowHeight = child.getHeight();
                    break;
                }
            }
        }
    }

    /**
     * Returns the scrollY for the given position in the adapter.
     */
    private int getScrollAtPosition(int position, int rowHeight) {
        AlphabeticalAppsList.AdapterItem item = mApps.getAdapterItems().get(position);
        if (item.viewType == AllAppsGridAdapter.VIEW_TYPE_ICON ||
                item.viewType == AllAppsGridAdapter.VIEW_TYPE_PREDICTION_ICON
                /// M: Add for OP customization.
                || item.viewType == AllAppsGridAdapter.FOLDER_VIEW_TYPE) {
            int offset = item.rowIndex > 0 ? getPaddingTop() : 0;
            /// M: Add for OP customization. @{
            if (mSupportEditAndHideApps && Launcher.isInEditMode()) {
                offset = 0;
            }
            /// @}
            return offset + item.rowIndex * rowHeight;
        } else {
            return 0;
        }
    }

    /**
     * The current scroll state of the recycler view.  We use this in onUpdateScrollbar()
     * and scrollToPositionAtProgress() to determine the scroll position of the recycler view so
     * that we can calculate what the scroll bar looks like, and where to jump to from the fast
     * scroller.
     */
    public static class ScrollPositionState {
        // The index of the first visible row
        public int rowIndex;
        // The offset of the first visible row
        public int rowTopOffset;
        // The adapter position of the first visible item
        public int itemPos;

        // The height of a given row (they are currently all the same height)
        public int rowHeight;
    }

    public int[] getCellSize() {
        if (mCellSize[0] != 0 && mCellSize[1] != 0) {
            return mCellSize;
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int position = getChildPosition(child);
            if (position != NO_POSITION) {
                AlphabeticalAppsList.AdapterItem item =
                    mApps.getAdapterItems().get(position);
                if (item.viewType == AllAppsGridAdapter.VIEW_TYPE_ICON
                    || item.viewType == AllAppsGridAdapter.VIEW_TYPE_PREDICTION_ICON
                    || item.viewType == AllAppsGridAdapter.FOLDER_VIEW_TYPE) {
                    mCellSize[0] = child.getWidth();
                    mCellSize[1] = child.getHeight();
                    break;
                }
            }
        }
        return mCellSize;
    }

    public void setDeviceProfile(DeviceProfile grid) {
        mGrid = grid;
    }

    public void setCrrentDragCell(int[] cell) {
        mCrrentDragCell[0] = cell[0];
        mCrrentDragCell[1] = cell[1];

    }
    public void showFolderAccept(FolderRingAnimator fra) {
        mFolderOuterRings.add(fra);
    }

    public void hideFolderAccept(FolderRingAnimator fra) {
        if (mFolderOuterRings.contains(fra)) {
            mFolderOuterRings.remove(fra);
        }
        invalidate();
    }

    public void clearDragOutlines() {
    }

    public void cellToPoint(int cellX, int cellY, int[] result) {
        final int hStartPadding = getPaddingLeft();
        final int vStartPadding = getPaddingTop();

        result[0] = hStartPadding + cellX * (mCellSize [0] + 0); // Set mWidthGap is 0
        result[1] = vStartPadding + cellY * (mCellSize [1] + 0); // Set mHeightGap is 0
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mLastTouchY = ev.getY();
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mSupportEditAndHideApps) {
            return super.onTouchEvent(ev);
        }

        if (!Launcher.isInEditMode()) {
            return super.onTouchEvent(ev);
        }

        super.onTouchEvent(ev);

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchY = ev.getY();

                break;
            case MotionEvent.ACTION_UP:
                stopScroll();
                getCurScrollState(mScrollPosState);

                float currentY = ev.getY();
                float thredhold =  mScrollPosState.rowHeight * 1.0f;
                int pageIndex = 0;

                if (mLastTouchY - currentY > thredhold) {
                    pageIndex = mScrollPosState.rowIndex / mNumAppRowsPerPage + 1;
                    if (pageIndex > mApps.getNumAppRows() / mNumAppRowsPerPage - 1) {
                        pageIndex = mApps.getNumAppRows() / mNumAppRowsPerPage - 1;
                    }
                } else if (currentY - mLastTouchY > thredhold) {
                    pageIndex = mScrollPosState.rowIndex / mNumAppRowsPerPage;
                    if (pageIndex < 0) {
                        pageIndex = 0;
                    }
                } else {
                    pageIndex = (mScrollPosState.rowIndex + 1) / mNumAppRowsPerPage;
                }
                mTargetPage = pageIndex;

                int pos = pageIndex * mNumAppsPerRow * mNumAppRowsPerPage + 2;
                LauncherLog.d(TAG, "onTouchEvent.ACTION_UP: pos=" + pos
                    + ", mTargetPage=" + mTargetPage
                    + ", mNumAppsPerRow=" + mNumAppsPerRow
                    + ", mNumAppRowsPerPage=" + mNumAppRowsPerPage);
                smoothSnapToPosition(pos, mScrollPosState);

                break;
        }
        return mScrollbar.isDraggingThumb();
    }

    /// @}
}
