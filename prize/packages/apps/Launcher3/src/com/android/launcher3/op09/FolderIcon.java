/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.launcher3.folder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
/// M: Add for OP customization.
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
/// M: Add for OP customization.
import android.widget.ImageView;
import android.widget.TextView;

/// M: Add for OP customization.
import com.android.launcher3.allapps.AllAppsRecyclerView;
import com.android.launcher3.Alarm;
import com.android.launcher3.AppInfo;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.CellLayout;
import com.android.launcher3.CheckLongPressHelper;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.FolderInfo.FolderListener;
import com.android.launcher3.IconCache;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAnimUtils;
import com.android.launcher3.LauncherAppState;
/// M: Add for OP customization. @{
import com.android.launcher3.LauncherExtPlugin;
import com.android.launcher3.LauncherPluginEx;
/// @}
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.OnAlarmListener;
import com.android.launcher3.PreloadIconDrawable;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.SimpleOnStylusPressListener;
import com.android.launcher3.StylusEventHelper;
import com.android.launcher3.Utilities;
import com.android.launcher3.Workspace;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.dragndrop.DragLayer;
import com.android.launcher3.dragndrop.DragView;
/// M: Add for OP customization.
import com.android.launcher3.op.LauncherLog;
import com.android.launcher3.util.Thunk;

import java.util.ArrayList;

///M: modify folderIcon animate. @{
import android.content.res.Resources;
import android.os.Looper;
/// @}
/**
 * An icon that can appear on in the workspace representing an {@link Folder}.
 */
public class FolderIcon extends FrameLayout implements FolderListener {
    private static final String TAG = "FolderIcon";
    @Thunk Launcher mLauncher;
    @Thunk Folder mFolder;
    private FolderInfo mInfo;
    @Thunk static boolean sStaticValuesDirty = true;

    public static final int NUM_ITEMS_IN_PREVIEW = FeatureFlags.LAUNCHER3_LEGACY_FOLDER_ICON ?
            StackFolderIconLayoutRule.MAX_NUM_ITEMS_IN_PREVIEW :
            ClippedFolderIconLayoutRule.MAX_NUM_ITEMS_IN_PREVIEW;

    private CheckLongPressHelper mLongPressHelper;
    private StylusEventHelper mStylusEventHelper;

    // The number of icons to display in the
    private static final int CONSUMPTION_ANIMATION_DURATION = 100;
    private static final int DROP_IN_ANIMATION_DURATION = 400;
    private static final int INITIAL_ITEM_ANIMATION_DURATION = 350;
    private static final int FINAL_ITEM_ANIMATION_DURATION = 200;

    // Flag whether the folder should open itself when an item is dragged over is enabled.
    public static final boolean SPRING_LOADING_ENABLED = true;

    // Delay when drag enters until the folder opens, in miliseconds.
    private static final int ON_OPEN_DELAY = 800;

    /// M: Add for OP customization. @{
    @Thunk ImageView mPreviewBackground;
    FolderRingAnimator mFolderRingAnimator = null;
    /// @}
    @Thunk BubbleTextView mFolderName;

    // These variables are all associated with the drawing of the preview; they are stored
    // as member variables for shared usage and to avoid computation on each frame
    private int mIntrinsicIconSize = -1;
    private int mTotalWidth = -1;
    private int mPrevTopPadding = -1;

    PreviewBackground mBackground = new PreviewBackground();

    private PreviewLayoutRule mPreviewLayoutRule;

    boolean mAnimating = false;
    private Rect mOldBounds = new Rect();

    private float mSlop;

    private PreviewItemDrawingParams mTmpParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    private ArrayList<PreviewItemDrawingParams> mDrawingParams =
            new ArrayList<PreviewItemDrawingParams>();
    private Drawable mReferenceDrawable = null;

    Paint mBgPaint = new Paint();

    private Alarm mOpenAlarm = new Alarm();

    /// M: Add for OP customization. @{
    public static boolean mSupportEditAndHideApps;
    public boolean mIsPageViewFolderIcon = false;
    // Flag as to whether or not to draw an outer ring. Currently none is designed.
    public static final boolean HAS_OUTER_RING = true;

    public static Drawable sSharedFolderLeaveBehind = null;
    // The degree to which the outer ring is scaled in its natural state
    private static final float OUTER_RING_GROWTH_FACTOR = 0.3f;
    // The degree to which the inner ring grows when accepting drop
    private static final float INNER_RING_GROWTH_FACTOR = 0.15f;
    /// @}

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FolderIcon(Context context) {
        super(context);
        init();
    }

    private void init() {
        mLongPressHelper = new CheckLongPressHelper(this);
        mStylusEventHelper = new StylusEventHelper(new SimpleOnStylusPressListener(this), this);
        mPreviewLayoutRule = FeatureFlags.LAUNCHER3_LEGACY_FOLDER_ICON ?
                new StackFolderIconLayoutRule() :
                new ClippedFolderIconLayoutRule();

        /// M: Add for OP customization. @{
        mSupportEditAndHideApps = LauncherExtPlugin.getInstance().supportEditAndHideApps();
        /// @}
    }

    /// M: Add for OP customization. @{

    public static FolderIcon getFolderIcon(int resId, Launcher launcher, ViewGroup group) {
        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);
        return icon;
    }

    public static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
            FolderInfo folderInfo, IconCache iconCache, boolean fromAllApp) {
            return fromXml(null, resId, launcher, group, folderInfo, iconCache, false);
    }

    public static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
            FolderInfo folderInfo, IconCache iconCache) {
        return fromXml(resId, launcher, group, folderInfo, iconCache, false);
    }

    /// @}

    /// M: Modify for OP customization.
    public static FolderIcon fromXml(FolderIcon folderIcon, int resId, Launcher launcher,
            ViewGroup group, FolderInfo folderInfo, IconCache iconCache, boolean fromAllApp) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean error = INITIAL_ITEM_ANIMATION_DURATION >= DROP_IN_ANIMATION_DURATION;
        if (error) {
            throw new IllegalStateException("DROP_IN_ANIMATION_DURATION must be greater than " +
                    "INITIAL_ITEM_ANIMATION_DURATION, as sequencing of adding first two items " +
                    "is dependent on this");
        }

        DeviceProfile grid = launcher.getDeviceProfile();
        /// M: Modify for OP customization. @{
        FolderIcon icon = null;
        if (mSupportEditAndHideApps && fromAllApp && folderIcon != null) {
            icon = folderIcon;
        } else {
            icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);
        }
        /// @}

        // For performance and compatibility reasons we render the preview using a software layer.
        // In particular, hardware path clipping has spotty ecosystem support and bad performance.
        // Software rendering also allows us to use shadow layers.
        icon.setLayerType(LAYER_TYPE_SOFTWARE, new Paint(Paint.FILTER_BITMAP_FLAG));

        icon.setClipToPadding(false);
        icon.mFolderName = (BubbleTextView) icon.findViewById(R.id.folder_icon_name);
        icon.mFolderName.setText(folderInfo.title);
        icon.mFolderName.setCompoundDrawablePadding(0);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) icon.mFolderName.getLayoutParams();
        lp.topMargin = grid.iconSizePx + grid.iconDrawablePaddingPx;

        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        icon.setContentDescription(launcher.getString(R.string.folder_name_format,
                folderInfo.title));
        Folder folder = Folder.fromXml(launcher);
        folder.setDragController(launcher.getDragController());
        folder.setFolderIcon(icon);

        /// M: Add for OP customization. @{
        // Offset the preview background to center this view accordingly
        if (folder.mFolderPluginEx.isSupportEditAndHideApps() && LauncherPluginEx.sIsInEditMode) {
            icon.mPreviewBackground = (ImageView) icon.findViewById(R.id.preview_background);
            lp = (FrameLayout.LayoutParams) icon.mPreviewBackground.getLayoutParams();
            lp.topMargin = grid.folderBackgroundOffset;
            lp.width = grid.folderIconSizePx;
            lp.height = grid.folderIconSizePx;
        }
        if (folder.mFolderPluginEx.isSupportEditAndHideApps()) {
            folder.mFolderPluginEx.setIsPageViewFolder(fromAllApp);
        }
        /// @}

        folder.bind(folderInfo);
        icon.setFolder(folder);
        icon.setAccessibilityDelegate(launcher.getAccessibilityDelegate());

        /// M: Add for OP customization. @{
        icon.mFolderRingAnimator = new FolderRingAnimator(launcher, icon);
        if (mSupportEditAndHideApps && fromAllApp && folderIcon != null) {
            icon.mFolderRingAnimator.mIsPageViewFolderIcon = true;
            LauncherLog.d(TAG, "create FolderIcon, icon.mFolderRingAnimator.mIsPageViewFolderIcon: "
                            + icon.mFolderRingAnimator.mIsPageViewFolderIcon);
        }
        /// @}

        folderInfo.addListener(icon);

        icon.setOnFocusChangeListener(launcher.mFocusHandler);
        return icon;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        sStaticValuesDirty = true;
        return super.onSaveInstanceState();
    }

    public Folder getFolder() {
        return mFolder;
    }

    private void setFolder(Folder folder) {
        mFolder = folder;
        updateItemDrawingParams(false);
    }

    public FolderInfo getFolderInfo() {
        return mInfo;
    }

    private boolean willAcceptItem(ItemInfo item) {
        final int itemType = item.itemType;
        return ((itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT) &&
                !mFolder.isFull() && item != mInfo && !mInfo.opened);
    }

    public boolean acceptDrop(ItemInfo dragInfo) {
        final ItemInfo item = dragInfo;
        return !mFolder.isDestroyed() && willAcceptItem(item);
    }

    public void addItem(ShortcutInfo item) {
        mInfo.add(item, true);
    }

    public void onDragEnter(ItemInfo dragInfo) {
        if (mFolder.isDestroyed() || !willAcceptItem(dragInfo)) return;

        /// M: Modify for OP customization. @{
        if (Launcher.mSupportEditAndHideApps && mIsPageViewFolderIcon) {
            RecyclerView mRecyclerView = (RecyclerView) getParent();
            mFolderRingAnimator.setCell(mInfo.cellX, mInfo.cellY);
            mFolderRingAnimator.setRecyclerView(mRecyclerView);
            mFolderRingAnimator.animateToAcceptState();
        } else {
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) getLayoutParams();
            CellLayout cl = (CellLayout) getParent().getParent();
            mBackground.animateToAccept(cl, lp.cellX, lp.cellY);
        }
        /// @}

        mOpenAlarm.setOnAlarmListener(mOnOpenListener);
        if (SPRING_LOADING_ENABLED &&
                ((dragInfo instanceof AppInfo) || (dragInfo instanceof ShortcutInfo))) {
            // TODO: we currently don't support spring-loading for PendingAddShortcutInfos even
            // though widget-style shortcuts can be added to folders. The issue is that we need
            // to deal with configuration activities which are currently handled in
            // Workspace#onDropExternal.
            mOpenAlarm.setAlarm(ON_OPEN_DELAY);
        }
    }

    OnAlarmListener mOnOpenListener = new OnAlarmListener() {
        public void onAlarm(Alarm alarm) {
            mFolder.beginExternalDrag();
            mLauncher.openFolder(FolderIcon.this);
        }
    };

    public Drawable prepareCreate(final View destView) {
        Drawable animateDrawable = getTopDrawable((TextView) destView);
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(),
                destView.getMeasuredWidth());
        return animateDrawable;
    }

    public void performCreateAnimation(final ShortcutInfo destInfo, final View destView,
            final ShortcutInfo srcInfo, final DragView srcView, Rect dstRect,
            float scaleRelativeToDragLayer, Runnable postAnimationRunnable) {

        // These correspond two the drawable and view that the icon was dropped _onto_
        Drawable animateDrawable = prepareCreate(destView);

        mReferenceDrawable = animateDrawable;

        addItem(destInfo);
        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, INITIAL_ITEM_ANIMATION_DURATION, false, null);

        // This will animate the dragView (srcView) into the new folder
        onDrop(srcInfo, srcView, dstRect, scaleRelativeToDragLayer, 1, postAnimationRunnable);
    }

    public void performDestroyAnimation(final View finalView, Runnable onCompleteRunnable) {
        Drawable animateDrawable = getTopDrawable((TextView) finalView);
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(),
                finalView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, FINAL_ITEM_ANIMATION_DURATION, true,
                onCompleteRunnable);
    }

    public void onDragExit() {
        mBackground.animateToRest();
        mOpenAlarm.cancelAlarm();
    }

    private void onDrop(final ShortcutInfo item, DragView animateView, Rect finalRect,
            float scaleRelativeToDragLayer, int index, Runnable postAnimationRunnable) {
        item.cellX = -1;
        item.cellY = -1;

        // Typically, the animateView corresponds to the DragView; however, if this is being done
        // after a configuration activity (ie. for a Shortcut being dragged from AllApps) we
        // will not have a view to animate
        if (animateView != null) {
            DragLayer dragLayer = mLauncher.getDragLayer();
            Rect from = new Rect();
            dragLayer.getViewRectRelativeToSelf(animateView, from);
            Rect to = finalRect;
            if (to == null) {
                to = new Rect();
                Workspace workspace = mLauncher.getWorkspace();
                /// M: Modify for OP customization. @{
                if (!mSupportEditAndHideApps) {
                    // Set cellLayout and this to it's final state to compute
                    // final animation locations
                    workspace.setFinalTransitionTransform((CellLayout) getParent().getParent());
                }
                /// @}

                float scaleX = getScaleX();
                float scaleY = getScaleY();
                setScaleX(1.0f);
                setScaleY(1.0f);
                scaleRelativeToDragLayer = dragLayer.getDescendantRectRelativeToSelf(this, to);
                // Finished computing final animation locations, restore current state
                setScaleX(scaleX);
                setScaleY(scaleY);
                /// M: Modify for OP customization. @{
                if (!mSupportEditAndHideApps) {
                    workspace.resetTransitionTransform((CellLayout) getParent().getParent());
                }
                /// @}
            }

            int[] center = new int[2];
            float scale = getLocalCenterForIndex(index, index + 1, center);
            center[0] = (int) Math.round(scaleRelativeToDragLayer * center[0]);
            center[1] = (int) Math.round(scaleRelativeToDragLayer * center[1]);

            to.offset(center[0] - animateView.getMeasuredWidth() / 2,
                      center[1] - animateView.getMeasuredHeight() / 2);

            float finalAlpha = index < mPreviewLayoutRule.numItems() ? 0.5f : 0f;

            float finalScale = scale * scaleRelativeToDragLayer;
            dragLayer.animateView(animateView, from, to, finalAlpha,
                    1, 1, finalScale, finalScale, DROP_IN_ANIMATION_DURATION,
                    new DecelerateInterpolator(2), new AccelerateInterpolator(2),
                    postAnimationRunnable, DragLayer.ANIMATION_END_DISAPPEAR, null);
            addItem(item);
            mFolder.hideItem(item);

            final PreviewItemDrawingParams params = index < mDrawingParams.size() ?
                    mDrawingParams.get(index) : null;
            if (params != null) params.hidden = true;
            postDelayed(new Runnable() {
                public void run() {
                    if (params != null) params.hidden = false;
                    mFolder.showItem(item);
                    invalidate();
                }
            }, DROP_IN_ANIMATION_DURATION);
        } else {
            addItem(item);
        }
    }

    public void onDrop(DragObject d) {
        ShortcutInfo item;
        if (d.dragInfo instanceof AppInfo) {
            // Came from all apps -- make a copy
            item = ((AppInfo) d.dragInfo).makeShortcut();
        } else {
            item = (ShortcutInfo) d.dragInfo;
        }
        mFolder.notifyDrop();
        onDrop(item, d.dragView, null, 1.0f, mInfo.contents.size(), d.postAnimationRunnable);
    }

    private void computePreviewDrawingParams(int drawableSize, int totalSize) {
        if (mIntrinsicIconSize != drawableSize || mTotalWidth != totalSize ||
                mPrevTopPadding != getPaddingTop()) {
            DeviceProfile grid = mLauncher.getDeviceProfile();

            mIntrinsicIconSize = drawableSize;
            mTotalWidth = totalSize;
            mPrevTopPadding = getPaddingTop();

            mBackground.setup(getResources().getDisplayMetrics(), grid, this, mTotalWidth,
                    getPaddingTop());
            mPreviewLayoutRule.init(mBackground.previewSize, mIntrinsicIconSize,
                    Utilities.isRtl(getResources()));

            updateItemDrawingParams(false);
        }
    }

    private void computePreviewDrawingParams(Drawable d) {
        computePreviewDrawingParams(d.getIntrinsicWidth(), getMeasuredWidth());
    }

    static class PreviewItemDrawingParams {
        PreviewItemDrawingParams(float transX, float transY, float scale, float overlayAlpha) {
            this.transX = transX;
            this.transY = transY;
            this.scale = scale;
            this.overlayAlpha = overlayAlpha;
        }

        public void update(float transX, float transY, float scale) {
            // We ensure the update will not interfere with an animation on the layout params
            // If the final values differ, we cancel the animation.
            if (anim != null) {
                if (anim.finalTransX == transX || anim.finalTransY == transY
                        || anim.finalScale == scale) {
                    return;
                }
                anim.cancel();
            }

            this.transX = transX;
            this.transY = transY;
            this.scale = scale;
        }

        float transX;
        float transY;
        float scale;
        public float overlayAlpha;
        boolean hidden;
        FolderPreviewItemAnim anim;
        Drawable drawable;
    }

    private float getLocalCenterForIndex(int index, int curNumItems, int[] center) {
        mTmpParams = computePreviewItemDrawingParams(Math.min(mPreviewLayoutRule.numItems(), index),
                curNumItems, mTmpParams);

        mTmpParams.transX += mBackground.basePreviewOffsetX;
        mTmpParams.transY += mBackground.basePreviewOffsetY;
        float offsetX = mTmpParams.transX + (mTmpParams.scale * mIntrinsicIconSize) / 2;
        float offsetY = mTmpParams.transY + (mTmpParams.scale * mIntrinsicIconSize) / 2;

        center[0] = (int) Math.round(offsetX);
        center[1] = (int) Math.round(offsetY);
        return mTmpParams.scale;
    }

    private PreviewItemDrawingParams computePreviewItemDrawingParams(int index, int curNumItems,
            PreviewItemDrawingParams params) {
        // We use an index of -1 to represent an icon on the workspace for the destroy and
        // create animations
        if (index == -1) {
            return getFinalIconParams(params);
        }
        return mPreviewLayoutRule.computePreviewItemDrawingParams(index, curNumItems, params);
    }

    private PreviewItemDrawingParams getFinalIconParams(PreviewItemDrawingParams params) {
        float iconSize = mLauncher.getDeviceProfile().iconSizePx;

        final float scale = iconSize / mReferenceDrawable.getIntrinsicWidth();
        final float trans = (mBackground.previewSize - iconSize) / 2;

        params.update(trans, trans, scale);
        return params;
    }

    private void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams params) {
        canvas.save();
        canvas.translate(params.transX, params.transY);
        canvas.scale(params.scale, params.scale);
        Drawable d = params.drawable;

        if (d != null) {
            mOldBounds.set(d.getBounds());
            d.setBounds(0, 0, mIntrinsicIconSize, mIntrinsicIconSize);
            if (d instanceof FastBitmapDrawable) {
                FastBitmapDrawable fd = (FastBitmapDrawable) d;
                float oldBrightness = fd.getBrightness();
                fd.setBrightness(params.overlayAlpha);
                d.draw(canvas);
                fd.setBrightness(oldBrightness);
            } else {
                d.setColorFilter(Color.argb((int) (params.overlayAlpha * 255), 255, 255, 255),
                        PorterDuff.Mode.SRC_ATOP);
                d.draw(canvas);
                d.clearColorFilter();
            }
            d.setBounds(mOldBounds);
        }
        canvas.restore();
    }

    /// M: Add for OP customization. @{
    public static class FolderRingAnimator {
        public int mCellX;
        public int mCellY;
        public float mOuterRingSize;
        public float mInnerRingSize;
        public FolderIcon mFolderIcon = null;
        public static Drawable sSharedOuterRingDrawable = null;
        public static Drawable sSharedInnerRingDrawable = null;
        public static int sPreviewSize = -1;
        public static int sPreviewPadding = -1;

        private ValueAnimator mAcceptAnimator;
        private ValueAnimator mNeutralAnimator;

        public RecyclerView mRecyclerView;
        public boolean mIsPageViewFolderIcon = false;

        public FolderRingAnimator(Launcher launcher, FolderIcon folderIcon) {
            mFolderIcon = folderIcon;
            Resources res = launcher.getResources();

            // We need to reload the static values when configuration changes in case they are
            // different in another configuration
            if (sStaticValuesDirty) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    throw new RuntimeException("FolderRingAnimator loading drawables "
                        + "on non-UI thread " + Thread.currentThread());
                }

                DeviceProfile grid = launcher.getDeviceProfile();
                sPreviewSize = grid.folderIconSizePx;
                sPreviewPadding = res.getDimensionPixelSize(R.dimen.folder_preview_padding);
                sSharedOuterRingDrawable = res.getDrawable(R.drawable.portal_ring_outer);
                sSharedInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_inner_nolip);
                sSharedFolderLeaveBehind = res.getDrawable(R.drawable.portal_ring_rest);
                sStaticValuesDirty = false;
            }
        }

        public void animateToAcceptState() {
            if (mNeutralAnimator != null) {
                mNeutralAnimator.cancel();
            }
            if (Launcher.mSupportEditAndHideApps && mIsPageViewFolderIcon) {
                mAcceptAnimator = LauncherAnimUtils.ofFloat(mRecyclerView, 0f, 1f);
            }

            mAcceptAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mAcceptAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + percent * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    mInnerRingSize = (1 + percent * INNER_RING_GROWTH_FACTOR) * previewSize;

                    if (Launcher.mSupportEditAndHideApps && mIsPageViewFolderIcon) {
                        if (mRecyclerView != null) {
                            mRecyclerView.invalidate();
                        }
                    }
                }
            });
            mAcceptAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(INVISIBLE);
                    }
                }
            });
            mAcceptAnimator.start();
        }

        public void animateToNaturalState() {
            if (mAcceptAnimator != null) {
                mAcceptAnimator.cancel();
            }
            if (Launcher.mSupportEditAndHideApps && mIsPageViewFolderIcon) {
                mNeutralAnimator = LauncherAnimUtils.ofFloat(mRecyclerView, 0f, 1f);
            }

            mNeutralAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mNeutralAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + (1 - percent) * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    mInnerRingSize = (1 + (1 - percent) * INNER_RING_GROWTH_FACTOR) * previewSize;

                    if (Launcher.mSupportEditAndHideApps && mIsPageViewFolderIcon) {
                        if (mRecyclerView != null) {
                            mRecyclerView.invalidate();
                        }
                    }
                }
            });
            mNeutralAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (Launcher.mSupportEditAndHideApps && mIsPageViewFolderIcon) {
                        if (mRecyclerView != null) {
                            ((AllAppsRecyclerView) mRecyclerView).hideFolderAccept(
                                FolderRingAnimator.this);
                        }
                    }

                    if (mFolderIcon != null && mFolderIcon.mPreviewBackground != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(VISIBLE);
                    }
                }
            });
            mNeutralAnimator.start();
        }

        // Location is expressed in window coordinates
        public void getCell(int[] loc) {
            loc[0] = mCellX;
            loc[1] = mCellY;
        }

        // Location is expressed in window coordinates
        public void setCell(int x, int y) {
            mCellX = x;
            mCellY = y;
        }

        public float getOuterRingSize() {
            return mOuterRingSize;
        }

        public float getInnerRingSize() {
            return mInnerRingSize;
        }

        public void setRecyclerView(RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
        }
    }
    /// @}

    /**
     * This object represents a FolderIcon preview background. It stores drawing / measurement
     * information, handles drawing, and animation (accept state <--> rest state).
     */
    public static class PreviewBackground {
        private float mScale = 1f;
        private float mColorMultiplier = 1f;
        private Path mClipPath = new Path();
        private int mStrokeWidth;
        private View mInvalidateDelegate;

        public int previewSize;
        private int basePreviewOffsetX;
        private int basePreviewOffsetY;

        private CellLayout mDrawingDelegate;
        public int delegateCellX;  // folderIcon x position
        public int delegateCellY;  // folderIcon y position

        // When the PreviewBackground is drawn under an icon (for creating a folder) the border
        // should not occlude the icon
        public boolean isClipping = true;

        // Drawing / animation configurations
        private static final float ACCEPT_SCALE_FACTOR = 1.25f;
        private static final float ACCEPT_COLOR_MULTIPLIER = 1.5f;

        // Expressed on a scale from 0 to 255.
        private static final int BG_OPACITY = 160;
        private static final int MAX_BG_OPACITY = 225;
        private static final int BG_INTENSITY = 245;
        private static final int SHADOW_OPACITY = 80;

        ValueAnimator mScaleAnimator;

        public void setup(DisplayMetrics dm, DeviceProfile grid, View invalidateDelegate,
                   int availableSpace, int topPadding) {
            mInvalidateDelegate = invalidateDelegate;

            final int previewSize = grid.folderIconSizePx;
            final int previewPadding = grid.folderIconPreviewPadding;

            this.previewSize = (previewSize - 2 * previewPadding);

            basePreviewOffsetX = (availableSpace - this.previewSize) / 2;
            basePreviewOffsetY = previewPadding + grid.folderBackgroundOffset + topPadding;

            mStrokeWidth = Utilities.pxFromDp(1, dm);

            invalidate();
        }

        int getRadius() {
            return previewSize / 2;
        }

        int getScaledRadius() {
            return (int) (mScale * getRadius());
        }

        int getOffsetX() {
            return basePreviewOffsetX - (getScaledRadius() - getRadius());
        }

        int getOffsetY() {
            return basePreviewOffsetY - (getScaledRadius() - getRadius());
        }

        void invalidate() {
            int radius = getScaledRadius();
            mClipPath.reset();
            mClipPath.addCircle(radius, radius, radius, Path.Direction.CW);

            if (mInvalidateDelegate != null) {
                mInvalidateDelegate.invalidate();
            }

            if (mDrawingDelegate != null) {
                mDrawingDelegate.invalidate();
            }
        }

        void setInvalidateDelegate(View invalidateDelegate) {
            mInvalidateDelegate = invalidateDelegate;
            invalidate();
        }

        // FolderIcon.dispatchDraw() -> this
        public void drawBackground(Canvas canvas, Paint paint) {
            canvas.save();
            canvas.translate(getOffsetX(), getOffsetY());

            paint.reset();
            paint.setStyle(Paint.Style.FILL);
            paint.setXfermode(null);
            paint.setAntiAlias(true);

            int alpha = (int) Math.min(MAX_BG_OPACITY, BG_OPACITY * mColorMultiplier);
            paint.setColor(Color.argb(alpha, BG_INTENSITY, BG_INTENSITY, BG_INTENSITY));

            float radius = getScaledRadius();

            canvas.drawCircle(radius, radius, radius, paint);
            canvas.clipPath(mClipPath, Region.Op.DIFFERENCE);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.TRANSPARENT);
            paint.setShadowLayer(mStrokeWidth, 0, mStrokeWidth,
                    Color.argb(SHADOW_OPACITY, 0, 0, 0));
            canvas.drawCircle(radius, radius, radius, paint);

            canvas.restore();
        }

        public void drawBackgroundStroke(Canvas canvas, Paint paint) {
            canvas.save();
            canvas.translate(getOffsetX(), getOffsetY());

            paint.reset();
            paint.setAntiAlias(true);
            paint.setColor(Color.argb(255, BG_INTENSITY, BG_INTENSITY, BG_INTENSITY));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(mStrokeWidth);

            float radius = getScaledRadius();
            canvas.drawCircle(radius, radius, radius - 1, paint);

            canvas.restore();
        }

        public void drawLeaveBehind(Canvas canvas, Paint paint) {
            float originalScale = mScale;
            mScale = 0.5f;

            canvas.save();
            canvas.translate(getOffsetX(), getOffsetY());

            paint.reset();
            paint.setAntiAlias(true);
            paint.setColor(Color.argb(160, 245, 245, 245));

            float radius = getScaledRadius();
            canvas.drawCircle(radius, radius, radius, paint);

            canvas.restore();
            mScale = originalScale;
        }

        // It is the callers responsibility to save and restore the canvas.
        private void clipCanvas(Canvas canvas) {
            canvas.translate(getOffsetX(), getOffsetY());
            canvas.clipPath(mClipPath);
            canvas.translate(-getOffsetX(), -getOffsetY());
        }

        private void delegateDrawing(CellLayout delegate, int cellX, int cellY) {
            if (mDrawingDelegate != delegate) {
                // add folder background to list in workspace,
                // then CellLayout.onDraw() call bg.drawBackground()
                delegate.addFolderBackground(this);
            }

            mDrawingDelegate = delegate;
            delegateCellX = cellX;
            delegateCellY = cellY;

            invalidate();
        }

        private void clearDrawingDelegate() {
            if (mDrawingDelegate != null) {
                // remove folder background in list
                mDrawingDelegate.removeFolderBackground(this);
            }

            mDrawingDelegate = null;
            invalidate();
        }

        private boolean drawingDelegated() {
            return mDrawingDelegate != null;
        }

        private void animateScale(float finalScale, float finalMultiplier,
                final Runnable onStart, final Runnable onEnd) {
            final float scale0 = mScale;
            final float scale1 = finalScale;

            final float bgMultiplier0 = mColorMultiplier;
            final float bgMultiplier1 = finalMultiplier;

            if (mScaleAnimator != null) {
                mScaleAnimator.cancel();
            }

            mScaleAnimator = LauncherAnimUtils.ofFloat(null, 0f, 1.0f);

            mScaleAnimator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float prog = animation.getAnimatedFraction();
                    mScale = prog * scale1 + (1 - prog) * scale0;
                    mColorMultiplier = prog * bgMultiplier1 + (1 - prog) * bgMultiplier0;
                    invalidate();
                }
            });
            mScaleAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (onStart != null) {
                        onStart.run();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onEnd != null) {
                        onEnd.run();
                    }
                    mScaleAnimator = null;
                }
            });

            mScaleAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);
            mScaleAnimator.start();
        }

        public void animateToAccept(final CellLayout cl, final int cellX, final int cellY) {
            Runnable onStart = new Runnable() {
                @Override
                public void run() {
                    delegateDrawing(cl, cellX, cellY);
                }
            };
            animateScale(ACCEPT_SCALE_FACTOR, ACCEPT_COLOR_MULTIPLIER, onStart, null);
        }

        public void animateToRest() {
            // This can be called multiple times -- we need to make sure the drawing delegate
            // is saved and restored at the beginning of the animation, since cancelling the
            // existing animation can clear the delgate.
            final CellLayout cl = mDrawingDelegate;
            final int cellX = delegateCellX;
            final int cellY = delegateCellY;

            Runnable onStart = new Runnable() {
                @Override
                public void run() {
                    delegateDrawing(cl, cellX, cellY);
                }
            };
            Runnable onEnd = new Runnable() {
                @Override
                public void run() {
                    clearDrawingDelegate();
                }
            };
            animateScale(1f, 1f, onStart, onEnd);
        }
    }

    public void setFolderBackground(PreviewBackground bg) {
        mBackground = bg;
        mBackground.setInvalidateDelegate(this);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mReferenceDrawable != null) {
            computePreviewDrawingParams(mReferenceDrawable);
        }

        if (!mBackground.drawingDelegated()) {
            mBackground.drawBackground(canvas, mBgPaint);
        }

        if (mFolder == null) return;
        if (mFolder.getItemCount() == 0 && !mAnimating) return;

        canvas.save();


        if (mPreviewLayoutRule.clipToBackground()) {
            mBackground.clipCanvas(canvas);
        }

        // The items are drawn in coordinates relative to the preview offset
        canvas.translate(mBackground.basePreviewOffsetX, mBackground.basePreviewOffsetY);

        // The first item should be drawn last (ie. on top of later items)
        for (int i = mDrawingParams.size() - 1; i >= 0; i--) {
            PreviewItemDrawingParams p = mDrawingParams.get(i);
            if (!p.hidden) {
                drawPreviewItem(canvas, p);
            }
        }
        canvas.restore();

        if (mPreviewLayoutRule.clipToBackground() && !mBackground.drawingDelegated()) {
            mBackground.drawBackgroundStroke(canvas, mBgPaint);
        }
    }

    private Drawable getTopDrawable(TextView v) {
        Drawable d = v.getCompoundDrawables()[1];
        return (d instanceof PreloadIconDrawable) ? ((PreloadIconDrawable) d).mIcon : d;
    }

    class FolderPreviewItemAnim {
        ValueAnimator mValueAnimator;
        float finalScale;
        float finalTransX;
        float finalTransY;

        /**
         *
         * @param params layout params to animate
         * @param index0 original index of the item to be animated
         * @param nItems0 original number of items in the preview
         * @param index1 new index of the item to be animated
         * @param nItems1 new number of items in the preview
         * @param duration duration in ms of the animation
         * @param onCompleteRunnable runnable to execute upon animation completion
         */
        public FolderPreviewItemAnim(final PreviewItemDrawingParams params, int index0, int nItems0,
                int index1, int nItems1, int duration, final Runnable onCompleteRunnable) {

            computePreviewItemDrawingParams(index1, nItems1, mTmpParams);

            finalScale = mTmpParams.scale;
            finalTransX = mTmpParams.transX;
            finalTransY = mTmpParams.transY;

            computePreviewItemDrawingParams(index0, nItems0, mTmpParams);

            final float scale0 = mTmpParams.scale;
            final float transX0 = mTmpParams.transX;
            final float transY0 = mTmpParams.transY;

            mValueAnimator = LauncherAnimUtils.ofFloat(FolderIcon.this, 0f, 1.0f);
            mValueAnimator.addUpdateListener(new AnimatorUpdateListener(){
                public void onAnimationUpdate(ValueAnimator animation) {
                    float progress = animation.getAnimatedFraction();

                    params.transX = transX0 + progress * (finalTransX - transX0);
                    params.transY = transY0 + progress * (finalTransY - transY0);
                    params.scale = scale0 + progress * (finalScale - scale0);
                    invalidate();
                }
            });

            mValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }
                    params.anim = null;
                }
            });
            mValueAnimator.setDuration(duration);
        }

        public void start() {
            mValueAnimator.start();
        }

        public void cancel() {
            mValueAnimator.cancel();
        }

        public boolean hasEqualFinalState(FolderPreviewItemAnim anim) {
            return finalTransY == anim.finalTransY && finalTransX == anim.finalTransX &&
                    finalScale == anim.finalScale;

        }
    }

    private void animateFirstItem(final Drawable d, int duration, final boolean reverse,
            final Runnable onCompleteRunnable) {

        FolderPreviewItemAnim anim;
        if (!reverse) {
            anim = new FolderPreviewItemAnim(mDrawingParams.get(0), -1, -1, 0, 2, duration,
                    onCompleteRunnable);
        } else {
            anim = new FolderPreviewItemAnim(mDrawingParams.get(0), 0, 2, -1, -1, duration,
                    onCompleteRunnable);
        }
        anim.start();
    }

    public void setTextVisible(boolean visible) {
        if (visible) {
            mFolderName.setVisibility(VISIBLE);
        } else {
            mFolderName.setVisibility(INVISIBLE);
        }
    }

    public boolean getTextVisible() {
        return mFolderName.getVisibility() == VISIBLE;
    }

    private void updateItemDrawingParams(boolean animate) {
        ArrayList<View> items = mFolder.getItemsInReadingOrder();
        int nItemsInPreview = Math.min(items.size(), mPreviewLayoutRule.numItems());

        int prevNumItems = mDrawingParams.size();

        // We adjust the size of the list to match the number of items in the preview
        while (nItemsInPreview < mDrawingParams.size()) {
            mDrawingParams.remove(mDrawingParams.size() - 1);
        }
        while (nItemsInPreview > mDrawingParams.size()) {
            mDrawingParams.add(new PreviewItemDrawingParams(0, 0, 0, 0));
        }

        for (int i = 0; i < mDrawingParams.size(); i++) {
            PreviewItemDrawingParams p = mDrawingParams.get(i);
            p.drawable = getTopDrawable((TextView) items.get(i));

            if (!animate || FeatureFlags.LAUNCHER3_LEGACY_FOLDER_ICON) {
                computePreviewItemDrawingParams(i, nItemsInPreview, p);
                if (mReferenceDrawable == null) {
                    mReferenceDrawable = p.drawable;
                }
            } else {
                FolderPreviewItemAnim anim = new FolderPreviewItemAnim(p, i, prevNumItems, i,
                        nItemsInPreview, DROP_IN_ANIMATION_DURATION, null);

                if (p.anim != null) {
                    if (p.anim.hasEqualFinalState(anim)) {
                        // do nothing, let the current animation finish
                        continue;
                    }
                    p.anim.cancel();
                }
                p.anim = anim;
                p.anim.start();
            }
        }
    }

    @Override
    public void onItemsChanged(boolean animate) {
        updateItemDrawingParams(animate);
        invalidate();
        requestLayout();
    }

    public void onAdd(ShortcutInfo item) {
        invalidate();
        requestLayout();
    }

    public void onRemove(ShortcutInfo item) {
        invalidate();
        requestLayout();
    }

    public void onTitleChanged(CharSequence title) {
        mFolderName.setText(title);
        setContentDescription(getContext().getString(R.string.folder_name_format, title));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);

        // Check for a stylus button press, if it occurs cancel any long press checks.
        if (mStylusEventHelper.onMotionEvent(event)) {
            mLongPressHelper.cancelLongPress();
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mLongPressHelper.cancelLongPress();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!Utilities.pointInView(this, event.getX(), event.getY(), mSlop)) {
                    mLongPressHelper.cancelLongPress();
                }
                break;
        }
        return result;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mLongPressHelper.cancelLongPress();
    }

    public void removeListeners() {
        mInfo.removeListener(this);
        mInfo.removeListener(mFolder);
    }

    public interface PreviewLayoutRule {
        public PreviewItemDrawingParams computePreviewItemDrawingParams(int index, int curNumItems,
            PreviewItemDrawingParams params);

        public void init(int availableSpace, int intrinsicIconSize, boolean rtl);

        public int numItems();
        public boolean clipToBackground();
    }
}
