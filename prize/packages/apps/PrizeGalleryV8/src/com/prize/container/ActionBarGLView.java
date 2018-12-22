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

package com.prize.container;

import android.content.Context;
import android.view.MotionEvent;

import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.NinePatchTexture;
import com.android.gallery3d.glrenderer.ResourceTexture;
import com.android.gallery3d.glrenderer.StringTexture;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.util.GalleryUtils;

public class ActionBarGLView extends GLView {
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/ActionBarGLView";

    private final StringTexture mCancelText;
    private final StringTexture mDoneText;
    private final StringTexture mTitleText;
    private final int mActionBarHeight;
    private final int mMarginH;
    private final int mClickRegionH;
    private static final int TITLE_GREEN = 0xFF3478f6;
    private static final int WHITE = 0xFFFFFFFF;

    private IOnClickListener mOnClickListener;
    private boolean mDownOnCancelButton;
    private boolean mDownOnDoneButton;

    public interface IOnClickListener {
        void onClick(boolean isCancel);
    }

    // This is the layout of UndoBarView. The unit is dp.
    //
    //    +-+----+----------------+-+--+----+-+------+--+-+
    // 48 | |    | Deleted        | |  | <- | | UNDO |  | |
    //    +-+----+----------------+-+--+----+-+------+--+-+
    //     4  16                   1 12  32  8        16 4
    public ActionBarGLView(Context context) {
        mCancelText = StringTexture.newInstance(context.getString(R.string.cancel),
                GalleryUtils.dpToPixel(14), TITLE_GREEN);
        mDoneText = StringTexture.newInstance(
                context.getString(R.string.done),
                GalleryUtils.dpToPixel(14), TITLE_GREEN);
        mTitleText = StringTexture.newInstance(
                context.getString(R.string.container_title),
                GalleryUtils.dpToPixel(16), WHITE);
        mActionBarHeight = GalleryUtils.dpToPixel(40);
        mMarginH  = GalleryUtils.dpToPixel(15);
        mClickRegionH = mMarginH + mMarginH + mDoneText.getWidth();

    }

    public void setClickListener(IOnClickListener listener) {
        mOnClickListener = listener;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        setMeasuredSize(0 /* unused */, mActionBarHeight);
    }

    @Override
    protected void render(GLCanvas canvas) {
        super.render(canvas);

        canvas.save(GLCanvas.SAVE_FLAG_ALPHA);
//        canvas.multiplyAlpha(mAlpha);

        int w = getWidth();
        int h = getHeight();

        int x = mMarginH;
        int y = (mActionBarHeight - mCancelText.getHeight()) / 2;
        mCancelText.draw(canvas, x, y);


        x = w - mMarginH - mDoneText.getWidth();
        y = (mActionBarHeight - mDoneText.getHeight()) / 2;
        mDoneText.draw(canvas, x, y);

        x = (w - mTitleText.getWidth()) / 2;
        y = (mActionBarHeight - mTitleText.getHeight()) / 2;
        mTitleText.draw(canvas, x, y);

        canvas.restore();
    }

    @Override
    protected boolean onTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownOnCancelButton = inCancelButton(event);
                mDownOnDoneButton = inDoneButton(event);
                break;
            case MotionEvent.ACTION_UP:
                if (mDownOnCancelButton) {
                    if (mOnClickListener != null && inCancelButton(event)) {
                        mOnClickListener.onClick(true);
                    }
                    mDownOnCancelButton = false;
                }

                if (mDownOnDoneButton) {
                    if (mOnClickListener != null && inDoneButton(event)) {
                        mOnClickListener.onClick(false);
                    }
                    mDownOnDoneButton = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mDownOnCancelButton = false;
                mDownOnDoneButton = false;
                break;
        }
        return true;
    }

    // Check if the event is on the right of the separator
    private boolean inCancelButton(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int w = getWidth();
        int h = getHeight();
        return (x >= 0 && x < mClickRegionH && y >= 0 && y < h);
    }

    private boolean inDoneButton(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int w = getWidth();
        int h = getHeight();
        return (x >= w - mClickRegionH && x < w && y >= 0 && y < h);
    }
}
