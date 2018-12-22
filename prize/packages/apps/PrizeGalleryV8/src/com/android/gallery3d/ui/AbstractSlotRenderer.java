/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.Context;
import android.graphics.Rect;

import com.android.gallery3d.R;
import com.android.gallery3d.glrenderer.FadeOutTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.NinePatchTexture;
import com.android.gallery3d.glrenderer.ResourceTexture;
import com.android.gallery3d.glrenderer.Texture;
import com.android.gallery3d.util.GalleryUtils;
import com.mediatek.gallery3d.layout.FancyHelper;

public abstract class AbstractSlotRenderer implements SlotView.SlotRenderer {

    private final ResourceTexture mVideoOverlay;
    private final ResourceTexture mVideoPlayIcon;
    private final ResourceTexture mPanoramaIcon;
//    private final ResourceTexture mFrameSelectedIcon;
    private final NinePatchTexture mFramePressed;
    private final NinePatchTexture mFrameSelected;
    private FadeOutTexture mFramePressedUp;
    protected boolean mIsActive;

    protected AbstractSlotRenderer(Context context) {
        mVideoOverlay = new ResourceTexture(context, R.drawable.ic_video_thumb);
        mVideoPlayIcon = new ResourceTexture(context, R.drawable.ic_gallery_play);
        mPanoramaIcon = new ResourceTexture(context, R.drawable.ic_360pano_holo_light);
//        mFrameSelectedIcon = new ResourceTexture(context, R.drawable.ic_picture_sel_prize);
        mFramePressed = new NinePatchTexture(context, getPressedIconId());
        mFrameSelected = new NinePatchTexture(context, R.drawable.ic_picture_sel);
    }

    protected int getPressedIconId() {
        return R.drawable.grid_pressed;
    }

    protected void drawContent(GLCanvas canvas,
            Texture content, int width, int height, int rotation) {
        canvas.save(GLCanvas.SAVE_FLAG_MATRIX);

        // The content is always rendered in to the largest square that fits
        // inside the slot, aligned to the top of the slot.
        //width = height = Math.min(width, height);
        if ((rotation != 0)&&(content.getWidth() < content.getHeight())) {
            canvas.translate(width / 2, height / 2);
            canvas.rotate(rotation, 0, 0, 1);
            canvas.translate(-width / 2, -height / 2);
            if (((rotation % 90) & 1) != 0) {
                int temp = height;
                height = width;
                width = temp;
            }
        }

        // Fit the content into the box
        float scale = Math.min(
                (float) width / content.getWidth(),
                (float) height / content.getHeight());
        canvas.scale(scale, scale, 1);
        content.draw(canvas, 0, 0);

        canvas.restore();
    }
    
    /// @prize fanjunchen 2015-05-20{
    protected void drawLabel(GLCanvas canvas,
            Texture content, int width, int height, int rotation) {
        canvas.save(GLCanvas.SAVE_FLAG_MATRIX);

        // Fit the content into the box
//        float scale = Math.min(
//                (float) width / content.getWidth(),
//                (float) height / content.getHeight());
        // canvas.scale(scale, scale, 1);
        content.draw(canvas, 0, 0);

        canvas.restore();
    }
    /// @prize }

    protected void drawVideoOverlay(GLCanvas canvas, int width, int height) {
        /// M: [FEATURE.MODIFY] fancy layout @{
        /*
        // Scale the video overlay to the height of the thumbnail and put it
        // on the left side.
        ResourceTexture v = mVideoOverlay;
        float scale = (float) height / v.getHeight();
        int w = Math.round(scale * v.getWidth());
        int h = Math.round(scale * v.getHeight());
        v.draw(canvas, 0, 0, w, h);
        */
        if (FancyHelper.isFancyLayoutSupported()) {
            // draw black rect one by one instead of using overlay resource
            canvas.save(GLCanvas.SAVE_FLAG_MATRIX);
            float rectHeight = FancyHelper.VIDEO_OVERLAY_RECT_HEIGHT;
            float gap = FancyHelper.VIDEO_OVERLAY_RECT_GAP;
            int count = height / ((int) (rectHeight + gap));
            for (int i = 0; i < count; i++) {
                float y = gap + (float) i * (rectHeight + gap);
                canvas.fillRect(FancyHelper.VIDEO_OVERLAY_LEFT_OFFSET, y,
                        rectHeight, rectHeight, FancyHelper.VIDEO_OVERLAY_COLOR);
            }
            canvas.restore();
        } else {
            // Scale the video overlay to the height of the thumbnail and put it
            // on the left side.
            ResourceTexture v = mVideoOverlay;
            float scale = (float) height / v.getHeight();
            int w = Math.round(scale * v.getWidth());
            int h = Math.round(scale * v.getHeight());
            v.draw(canvas, 0, 0, w, h);
        }
        /// @}

        /// M: [FEATURE.MODIFY] do not show play icon@{
        // int s = Math.min(width, height) / 6;
        // mVideoPlayIcon.draw(canvas, (width - s) / 2, (height - s) / 2, s, s);
        /// @}
    }

    protected void drawVideoOverlay(GLCanvas canvas, Texture content, int width, int height) {
        if (GalleryUtils.isShowVideoThumbnail()) {
            drawVideoOverlay(canvas, width, height);
        } else {
            drawVideoDuration(canvas, content, width, height);
        }
    }

    protected void drawVideoDuration(GLCanvas canvas, Texture content, int width, int height) {
        if (content != null) {
            //durationInSec
            canvas.save(GLCanvas.SAVE_FLAG_MATRIX);
            float scale = (float) width / content.getWidth();
            int w = Math.round(scale * content.getWidth());
            int h = Math.round(scale * content.getHeight());
            content.draw(canvas, 0, width - h, w, h);
            canvas.restore();
        }
    }



    protected void drawPanoramaIcon(GLCanvas canvas, int width, int height) {
        int iconSize = Math.min(width, height) / 6;
        mPanoramaIcon.draw(canvas, (width - iconSize) / 2, (height - iconSize) / 2,
                iconSize, iconSize);
    }

    protected boolean isPressedUpFrameFinished() {
        if (mFramePressedUp != null) {
            if (mFramePressedUp.isAnimating()) {
                return false;
            } else {
                mFramePressedUp = null;
            }
        }
        return true;
    }

    protected void drawPressedUpFrame(GLCanvas canvas, int width, int height) {
        if (mFramePressedUp == null) {
            mFramePressedUp = new FadeOutTexture(mFramePressed);
        }
        drawFrame(canvas, mFramePressed.getPaddings(), mFramePressedUp, 0, 0, width, height);
    }

    protected void drawPressedFrame(GLCanvas canvas, int width, int height) {
        drawFrame(canvas, mFramePressed.getPaddings(), mFramePressed, 0, 0, width, height);
    }

    protected void drawSelectedFrame(GLCanvas canvas, int width, int height) {
        /*ResourceTexture v = mFrameSelectedIcon;
        float scale = (float) height / v.getHeight();
        int w = Math.round(scale * v.getWidth());
        int h = Math.round(scale * v.getHeight());
        v.draw(canvas, 0, 0, w, h);*/
        drawFrame(canvas, mFrameSelected.getPaddings(), mFrameSelected, 0, 0, width, height);
    }

    protected static void drawFrame(GLCanvas canvas, Rect padding, Texture frame,
            int x, int y, int width, int height) {
        frame.draw(canvas, x - padding.left, y - padding.top, width + padding.left + padding.right,
                 height + padding.top + padding.bottom);
    }
}
