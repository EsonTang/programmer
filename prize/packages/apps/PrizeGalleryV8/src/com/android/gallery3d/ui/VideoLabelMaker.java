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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import com.android.gallery3d.R;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.photos.data.GalleryBitmapPool;
import com.mediatek.gallery3d.layout.FancyHelper;
import com.mediatek.gallery3d.util.TraceHelper;

public class VideoLabelMaker {

    private static final String TAG = "VideoLabelMaker";

    private LabelSpec mSpec;
    private TextPaint mDurationPaint;
    private Context mContext;

    private LazyLoadedBitmap mVideoThumbIcon;

    public static class LabelSpec {
        public int labelHeight;
        public int labelWidth;
        public int durationOffset;
        public int durationFontSize;
        public int leftMargin;
        public int iconSize;
        public int durationRightMargin;
        public int backgroundColor;
        public int durationColor;
        public int borderSize;
    }

    public VideoLabelMaker(Context context, LabelSpec spec) {
        mContext = context;
        mSpec = spec;
        init();
    }

    private void init() {
        mDurationPaint = getTextPaint(mSpec.durationFontSize, mSpec.durationColor, false);
        mDurationPaint.setTextAlign(Align.RIGHT);
        mVideoThumbIcon = new LazyLoadedBitmap(R.drawable.ic_video_identify_prize);
    }

    public VideoLabelMaker(Context context) {
        mContext = context;
        LabelSpec spec = new LabelSpec();
        Resources r = mContext.getResources();
        spec.labelWidth = r.getDimensionPixelSize(R.dimen.video_label_width);
        spec.labelHeight = r.getDimensionPixelSize(R.dimen.video_label_height);
        spec.durationFontSize = r.getDimensionPixelSize(R.dimen.video_duration_size);
        spec.leftMargin = r.getDimensionPixelSize(R.dimen.video_icon_left);
        spec.durationRightMargin = r.getDimensionPixelSize(R.dimen.video_duration_right);
        spec.durationColor = r.getColor(R.color.video_duration);
        spec.borderSize = r.getDimensionPixelSize(R.dimen.video_duration_border);
        mSpec = spec;
        init();
    }

    private static TextPaint getTextPaint(int textSize, int color, boolean isBold) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        if (isBold) {
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        return paint;
    }

    private class LazyLoadedBitmap {
        private Bitmap mBitmap;
        private int mResId;

        public LazyLoadedBitmap(int resId) {
            mResId = resId;
        }

        public synchronized Bitmap get() {
            if (mBitmap == null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Config.ARGB_8888;
                mBitmap = BitmapFactory.decodeResource(
                        mContext.getResources(), mResId, options);
            }
            return mBitmap;
        }
    }

    public ThreadPool.Job<Bitmap> requestLabel(
            String duration) {
        return new VideoLabelJob(duration);
    }

    static void drawText(Canvas canvas,
            int x, int y, String text, int lengthLimit, TextPaint p) {
        // The TextPaint cannot be used concurrently
        synchronized (p) {
            text = TextUtils.ellipsize(
                    text, p, lengthLimit, TextUtils.TruncateAt.END).toString();
            Log.i(TAG, "drawText x=" + x + " y=" + y + " text=" + text + " lengthLimit=" + lengthLimit);
            canvas.drawText(text, x, y - p.getFontMetricsInt().ascent, p);
        }
    }

    private class VideoLabelJob implements ThreadPool.Job<Bitmap> {
        private final String mDuration;
        public VideoLabelJob(String duration) {
            mDuration = duration;
        }

        @Override
        public Bitmap run(JobContext jc) {
            /// M: [DEBUG.ADD] @{
            TraceHelper.traceBegin(">>>>AlbumLabelMaker-AlbumLabelJob.run");
            /// @}
            LabelSpec s = mSpec;

            String duration = mDuration;
            Bitmap icon = mVideoThumbIcon.get();

            Bitmap bitmap = null;
            int labelWidth;

            synchronized (this) {
                if (FancyHelper.isFancyLayoutSupported()) {
                    labelWidth = FancyHelper.getScreenWidthAtFancyMode();
                } else {
                    labelWidth = s.labelWidth;
                    bitmap = GalleryBitmapPool.getInstance().get(s.labelWidth, s.labelHeight);
                }
                /// @}
            }

            if (bitmap == null) {
                int borders = 2 * s.borderSize;
                bitmap = Bitmap.createBitmap(s.labelWidth, s.labelHeight + borders, Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            canvas.clipRect(0, s.borderSize,
                    bitmap.getWidth(),
                    bitmap.getHeight() - s.borderSize);
            canvas.drawColor(mSpec.backgroundColor, PorterDuff.Mode.SRC);

            canvas.translate(0, s.borderSize);

            // draw title
            /// M: [DEBUG.MODIFY] @{
            /*if (jc.isCancelled()) return null;*/
            if (jc.isCancelled()) {
                TraceHelper.traceEnd();
                return null;
            }
            /// @}
            int x = s.leftMargin;
            // TODO: is the offset relevant in new reskin?
            // int y = s.titleOffset;
            int y = (s.labelHeight - s.durationFontSize - s.borderSize) / 2;
            drawText(canvas, labelWidth - s.durationRightMargin, y, duration, labelWidth - x, mDurationPaint);
            // draw count
            /// M: [DEBUG.MODIFY] @{
            /*if (jc.isCancelled()) return null;*/
            if (jc.isCancelled()) {
                TraceHelper.traceEnd();
                return null;
            }
            /// @}

            // draw the icon
            if (icon != null) {
                /// M: [DEBUG.MODIFY] @{
                /*if (jc.isCancelled()) return null;*/
                if (jc.isCancelled()) {
                    TraceHelper.traceEnd();
                    return null;
                }
                /// @}
                canvas.translate(s.leftMargin, 0);
                canvas.drawBitmap(icon, 0, s.labelHeight - icon.getHeight(), null);
            }
            /// M: [DEBUG.ADD] @{
            TraceHelper.traceEnd();
            /// @}
            Log.i(TAG, "VideoLabelJob bitmap" + bitmap);
            return bitmap;
        }
    }

    public void recycleLabel(Bitmap label) {
        GalleryBitmapPool.getInstance().put(label);
    }

}
