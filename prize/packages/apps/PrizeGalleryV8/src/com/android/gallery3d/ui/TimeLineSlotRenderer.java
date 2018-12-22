/*
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


import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextPaint;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.TimeLineDataLoader;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.glrenderer.ColorTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.StringTexture;
import com.android.gallery3d.glrenderer.Texture;
import com.android.gallery3d.glrenderer.TiledTexture;
import com.android.gallery3d.util.LogUtil;
import com.mediatek.gallery3d.adapter.FeatureHelper;
import com.android.gallery3d.glrenderer.UploadedTexture;
import com.android.gallery3d.R;

public class TimeLineSlotRenderer extends AbstractSlotRenderer {

    @SuppressWarnings("unused")
    private static final String TAG = "TimeLineSlotRenderer";

    private final int mPlaceholderColor;
    private static final int CACHE_SIZE = 96;

    private TimeLineSlidingWindow mDataWindow;
    private final AbstractGalleryActivity mActivity;
    private final ColorTexture mWaitLoadingTexture;
    private final TimeLineSlotView mSlotView;
    private final SelectionManager mSelectionManager;

    private int mPressedIndex = -1;
    private boolean mAnimatePressedUp;
    private Path mHighlightItemPath = null;
    private boolean mInSelectionMode;

    private AlbumSlotRenderer.SlotFilter mSlotFilter;
    private final LabelSpec mLabelSpec;
    private final TextPaint mCountPaint;
    private StringTexture mSelectTexture;
    private StringTexture mUnSelectTexture;
    private final ColorTexture mWhiteTexture;

    public static class LabelSpec {

        public int slotGapPort;
        public int slotGapLand;
        public int timeLineTitleHeight;
        public int timeLineTitlePaddingBottom;
        public int timeLineTitleFontSize;
        public int timeLineTitleTextColor;
        public int timeLineNumberFontSize;
        public int timeLineNumberTextColor;
        public int timeLineLocationFontSize;
        public int timeLineLocationTextColor;
        public int timeLineTitleMarginLeft;
        public int timeLineTitleMarginRight;
        public int timeLineTimePadding;
        public int timeLineCountPadding;
        public int timeLineTitleBackgroundColor;
        public int timeLineLocationPaddingLeft;
}
    public TimeLineSlotRenderer(AbstractGalleryActivity activity, TimeLineSlotView slotView,
                                    SelectionManager selectionManager, LabelSpec labelSpec,
                                    int placeholderColor) {
        super(activity);
        mActivity = activity;
        mSlotView = slotView;
        mLabelSpec = labelSpec;
        mSelectionManager = selectionManager;
        mPlaceholderColor = placeholderColor;
        mWaitLoadingTexture = new ColorTexture(mPlaceholderColor);
        mWaitLoadingTexture.setSize(1, 1);
        mCountPaint = getTextPaint(mLabelSpec.timeLineNumberFontSize, mLabelSpec.timeLineNumberTextColor, false);


        mSelectTexture = StringTexture.newInstance(mActivity.getString(R.string.title_select), mCountPaint);
        mUnSelectTexture = StringTexture.newInstance(mActivity.getString(R.string.title_unselect), mCountPaint);
        mWhiteTexture = new ColorTexture(Color.WHITE);
    }

    public void setPressedIndex(int index) {
        if (mPressedIndex == index) return;
        mPressedIndex = index;
        mSlotView.invalidate();
    }

    public void setPressedUp() {
        if (mPressedIndex == -1) return;
        mAnimatePressedUp = true;
        mSlotView.invalidate();
    }

    public void setHighlightItemPath(Path path) {
        if (mHighlightItemPath == path) return;
        mHighlightItemPath = path;
        mSlotView.invalidate();
    }

    protected static Texture checkContentTexture(Texture texture) {
        return (texture instanceof TiledTexture)
                && !((TiledTexture) texture).isReady()
                ? null
                : texture;
    }

    protected int renderOverlay(GLCanvas canvas, int index,
            TimeLineSlidingWindow.AlbumEntry entry, int width, int height) {
        int renderRequestFlags = 0;
        if (mPressedIndex == index) {
            if (mAnimatePressedUp) {
                drawPressedUpFrame(canvas, width, height);
                renderRequestFlags |= SlotView.RENDER_MORE_FRAME;
                if (isPressedUpFrameFinished()) {
                    mAnimatePressedUp = false;
                    mPressedIndex = -1;
                }
            } else {
                drawPressedFrame(canvas, width, height);
            }
        } else if ((entry.path != null) && (mHighlightItemPath == entry.path)) {
            drawSelectedFrame(canvas, width, height);
        } else if (mInSelectionMode && entry.mediaType != MediaItem.MEDIA_TYPE_TIMELINE_TITLE
                && mSelectionManager.isItemSelected(entry.path)) {
            drawSelectedFrame(canvas, width, height);
        } else if (mInSelectionMode && entry.mediaType == MediaItem.MEDIA_TYPE_TIMELINE_TITLE) {
            mWhiteTexture.draw(canvas, mLabelSpec.timeLineLocationPaddingLeft, 0, width - mLabelSpec.timeLineLocationPaddingLeft, height);
            if (mSelectionManager.inSelectAllMode()) {
                mUnSelectTexture.draw(canvas, width - mUnSelectTexture.getWidth() - mLabelSpec.timeLineTitleMarginRight, (height - mUnSelectTexture.getHeight()) / 2 + mLabelSpec.slotGapPort + mLabelSpec.timeLineTitlePaddingBottom);
                entry.isAllSelect = true;
            } else if (mSelectionManager.getSelectedCount() == 0 || !entry.isAllSelect) {
                mSelectTexture.draw(canvas, width - mSelectTexture.getWidth() - mLabelSpec.timeLineTitleMarginRight, (height - mSelectTexture.getHeight()) / 2 + mLabelSpec.slotGapPort + mLabelSpec.timeLineTitlePaddingBottom);
                entry.isAllSelect = false;
            } else if (entry.isAllSelect) {
                mUnSelectTexture.draw(canvas, width - mUnSelectTexture.getWidth() - mLabelSpec.timeLineTitleMarginRight, (height - mUnSelectTexture.getHeight()) / 2 + mLabelSpec.slotGapPort + mLabelSpec.timeLineTitlePaddingBottom);
            }
        }
        LogUtil.i(TAG, "renderOverlay mediaType=" + entry.mediaType + " mInSelectionMode=" + mInSelectionMode + " path=" + entry.path
        + " width=" + width + " height=" + height + " mLabelSpec.slotGapPort=" + mLabelSpec.slotGapPort);
        return renderRequestFlags;
    }

    protected class MyDataModelListener implements TimeLineSlidingWindow.Listener {
        @Override
        public void onContentChanged() {
            mSlotView.invalidate();
        }

        @Override
        public void onSizeChanged(int[] size) {
            mSlotView.setSlotCount(size);
            mSlotView.invalidate();
        }
    }

    public void resume() {
        mDataWindow.resume();
    }

    public void pause() {
        mDataWindow.pause();
    }

    @Override
    public void prepareDrawing() {
        mInSelectionMode = mSelectionManager.inSelectionMode();
    }

    @Override
    public void onVisibleRangeChanged(int visibleStart, int visibleEnd) {
        if (mDataWindow != null) {
            mDataWindow.setActiveWindow(visibleStart, visibleEnd);
			if(mSelectionManager.inSelectionMode()){
				updateAllTimelineSelect();
			}
        }
    }

    @Override
    public void onSlotSizeChanged(int width, int height) {
        if (mDataWindow != null) {
            mDataWindow.onSlotSizeChanged(width, height);
        }
    }

    private static TextPaint getTextPaint(
            int textSize, int color, boolean isBold) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setTypeface(Typeface.SANS_SERIF);
        if (isBold) {
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        return paint;
    }

    public void setSlotFilter(AlbumSlotRenderer.SlotFilter slotFilter) {
        mSlotFilter = slotFilter;
    }

    public void setModel(TimeLineDataLoader model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            mDataWindow = null;
        }
        LogUtil.i(TAG, "setModel selectionManager=" + mSelectionManager + " model=" + model);
        if (model != null) {
            mDataWindow = new TimeLineSlidingWindow(mActivity, model, CACHE_SIZE, mLabelSpec,
                    mSlotView, mSelectionManager);
            mDataWindow.setListener(new MyDataModelListener());
        }
    }

    public void updateAllTimelineTitle(boolean switchTitle, int titleIndex, int slotIndex) {
        if (mDataWindow != null) {
            mDataWindow.updateAllTimelineTitle(switchTitle, titleIndex, slotIndex);
        }
    }

    public void updateAllTimelineSelect(){
        if(mDataWindow != null){
			mDataWindow.updateAllTimelineSelect();
        }
    }
    private static Texture checkTexture(Texture texture) {
        return (texture instanceof UploadedTexture) && ((UploadedTexture) texture).isUploading() ? null
                : texture;
    }

    @Override
    public int renderSlot(GLCanvas canvas, int index, int pass, int width, int height) {
        if (mSlotFilter != null && !mSlotFilter.acceptSlot(index)) return 0;
        LogUtil.i(TAG, "renderSlot index=" + index);
        TimeLineSlidingWindow.AlbumEntry entry = mDataWindow.get(index);
        int renderRequestFlags = 0;
        if (entry != null) {
            Texture content = /*checkTexture*/(entry.content);
            if (content == null) {
                LogUtil.i(TAG, "renderSlot content == null index=" + index + " entry.content=" + entry.content);
                if (entry.item == null || entry.item.isSelectable()) {
                    content = mWaitLoadingTexture;
                }
                entry.isWaitDisplayed = true;
            } else if (entry.isWaitDisplayed) {
                entry.isWaitDisplayed = false;
            }
            if (content != null) {
                drawContent(canvas, content, width, height, entry.rotation);
            }

			if (entry.mediaType == MediaObject.MEDIA_TYPE_VIDEO
                && entry.item.getMediaData() != null
                && !(entry.item.getMediaData().isSlowMotion)) {
            	drawVideoOverlay(canvas, entry.videoThumb, width, height);
        	}

        	if (entry.isPanorama) {
            	drawPanoramaIcon(canvas, width, height);
        	}

            renderRequestFlags |= renderOverlay(canvas, index, entry, width, height);

            FeatureHelper.drawMicroThumbOverLay(mActivity, canvas, width, height, entry.item);
        }
        return renderRequestFlags;
    }
	
}
