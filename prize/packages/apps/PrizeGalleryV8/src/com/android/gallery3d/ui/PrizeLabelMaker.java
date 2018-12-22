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
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import com.android.gallery3d.R;
import com.android.gallery3d.app.Log;
import com.android.gallery3d.data.DataSourceType;
import com.android.photos.data.GalleryBitmapPool;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.ThreadPool.JobContext;

import com.mediatek.gallery3d.layout.FancyHelper;
import com.mediatek.gallery3d.util.TraceHelper;

public class PrizeLabelMaker {

    private final LabelSpec mSpec;
    private final TextPaint mTitlePaint;
    private final TextPaint mCountPaint;
    private final Bitmap mCountBackgroupBitmap;
    private final Bitmap mCountBackgroupTwoBitmap;
    private final Context mContext;

    private int mBitmapWidth;
    private int mBitmapHeight;

    // private final LazyLoadedBitmap mLocalSetIcon;
    
    public static class LabelSpec {
        public int labelHeight;
        public int labelWidth;
        public int titleOffset;
        public int countOffset;
        public int titleFontSize;
        public int countFontSize;
        public int leftMargin;
        public int iconSize;
        public int titleRightMargin;
        public int backgroundColor;
        public int titleColor;
        public int countColor;
        public int borderSize;
    }

    public PrizeLabelMaker(Context context, LabelSpec spec) {
        mContext = context;
        mSpec = spec;
        mTitlePaint = getTextPaint(spec.titleFontSize, spec.titleColor, false);
        mCountPaint = getTextPaint(spec.countFontSize, spec.countColor, false);
        mCountBackgroupBitmap =  BitmapFactory.decodeResource(context.getResources(), R.drawable.prize_albumset_lable_countsize);
        mCountBackgroupTwoBitmap =  BitmapFactory.decodeResource(context.getResources(), R.drawable.prize_albumset_lable_countsizelong);
        // mLocalSetIcon = new LazyLoadedBitmap(R.drawable.frame_overlay_gallery_folder);
    }

    public static int getBorderSize() {
        return 0;
    }

    private Bitmap getOverlayAlbumIcon(int sourceType) {
//        switch (sourceType) {
//            case DataSourceType.TYPE_LOCAL:
//                return mLocalSetIcon.get();
//        }
        return null;
    }

    private static TextPaint getTextPaint(int textSize, int color, boolean isBold) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        //paint.setShadowLayer(2f, 0f, 0f, Color.LTGRAY);
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
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                mBitmap = BitmapFactory.decodeResource(
                        mContext.getResources(), mResId, options);
            }
            return mBitmap;
        }
    }

    public ThreadPool.Job<Bitmap> requestLabel(
            String title, String count, int sourceType) {
        return new PrizeLabelJob(title, count, sourceType);
    }

    static void drawText(Canvas canvas,
            int x, int y, String text, int lengthLimit, TextPaint p) {
        // The TextPaint cannot be used concurrently
        synchronized (p) {
            text = TextUtils.ellipsize(
                    text, p, lengthLimit, TextUtils.TruncateAt.END).toString();
            canvas.drawText(text, x, y - p.getFontMetricsInt().ascent, p);
        }
    }

    private class PrizeLabelJob implements ThreadPool.Job<Bitmap> {
        private final String mTitle;
        private final String mCount;
        private final int mSourceType;
        /// M: [FEATURE.ADD] fancy layout @{
        private final boolean mIsLandCamera;
        private final boolean mIsFancyLayout;
        public PrizeLabelJob(String title, String count, int sourceType, boolean isLandCamera, boolean isFancy) {
            mTitle = title;
            mCount = count;
            mSourceType = sourceType;
            mIsLandCamera = isLandCamera;
            mIsFancyLayout = isFancy;
        }
        /// @}
        public PrizeLabelJob(String title, String count, int sourceType) {
            mTitle = title;
            mCount = count;
            mSourceType = sourceType;
            /// M: [FEATURE.ADD] fancy layout @{
            mIsLandCamera = false;
            mIsFancyLayout = false;
            /// @}
        }

        @Override
        public Bitmap run(JobContext jc) {
            /// M: [DEBUG.ADD] @{
            TraceHelper.traceBegin(">>>>AlbumLabelMaker-PrizeLabelJob.run");
            /// @}
            LabelSpec s = mSpec;

            String title = mTitle;
            String count = mCount;
            Bitmap icon = getOverlayAlbumIcon(mSourceType);

            Bitmap bitmap = null;
            int labelWidth;

            synchronized (this) {
                /// M: [FEATURE.MODIFY] fancy layout @{
                /*
                labelWidth = mLabelWidth;
                bitmap = GalleryBitmapPool.getInstance().get(mBitmapWidth, mBitmapHeight);
                */
                if (FancyHelper.isFancyLayoutSupported() && mIsFancyLayout && mIsLandCamera) {
                    labelWidth = FancyHelper.getScreenWidthAtFancyMode();
                } else {
                    labelWidth = s.labelWidth;
                    bitmap = GalleryBitmapPool.getInstance().get(mBitmapWidth, mBitmapHeight);
                }
                /// @}
            }

            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(s.labelWidth,
                        s.labelHeight, Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            canvas.clipRect(0, 0,
                    bitmap.getWidth(),
                    bitmap.getHeight());
           
            canvas.drawColor(mSpec.backgroundColor, PorterDuff.Mode.SRC);

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
            int y = (s.labelHeight - s.titleFontSize) / 2;
            drawText(canvas, x, y, title, labelWidth - s.leftMargin - x - 
                    s.titleRightMargin, mTitlePaint);
 
            // draw count
            /// M: [DEBUG.MODIFY] @{
            /*if (jc.isCancelled()) return null;*/
            if (jc.isCancelled()) {
                TraceHelper.traceEnd();
                return null;
            }
            /// @}
            x = labelWidth - s.titleRightMargin;
            y = (s.labelHeight - s.countFontSize) / 2;
            /// M: [FEATURE.MODIFY] fancy layout @{
            /*
            drawText(canvas, x, y, count,
                    labelWidth - x , mCountPaint);
            */
            if (FancyHelper.isFancyLayoutSupported()) {
                // use the same font for title and count
                drawText(canvas, x-s.leftMargin, y, count,
                        labelWidth - x , mTitlePaint);
                RectF dst = new RectF();
                if(count!=null&&count.length()<3){
	                dst.left = x-mCountBackgroupBitmap.getWidth()/2 +mCountPaint.measureText(count)/2-s.leftMargin;
	                dst.top  = s.labelHeight/2-mCountBackgroupBitmap.getHeight()/2;
	                dst.right = x+mCountBackgroupBitmap.getWidth()/2 + mCountPaint.measureText(count)/2-s.leftMargin;
	                dst.bottom = s.labelHeight/2+mCountBackgroupBitmap.getHeight()/2;
                	canvas.drawBitmap(mCountBackgroupBitmap, null, dst, mTitlePaint);
                }else{
	                dst.left = x-mCountBackgroupTwoBitmap.getWidth()/2 + mCountPaint.measureText(count)/2-s.leftMargin;
	                dst.top  = s.labelHeight/2-mCountBackgroupBitmap.getHeight()/2;
	                dst.right = x+mCountBackgroupTwoBitmap.getWidth()/2 + mCountPaint.measureText(count)/2-s.leftMargin;
	                dst.bottom = s.labelHeight/2+mCountBackgroupBitmap.getHeight()/2;
                	canvas.drawBitmap(mCountBackgroupTwoBitmap, null, dst, mTitlePaint);
                }
               
            } else {

               
                RectF dst = new RectF();
                if(count!=null&&count.length()<3){
                    drawText(canvas, x-s.leftMargin/2, y, count,
                            labelWidth - x , mCountPaint);
	                dst.left = x-mCountBackgroupBitmap.getWidth()/2 +mCountPaint.measureText(count)/2-s.leftMargin/2;
	                dst.top  = s.labelHeight/2-mCountBackgroupBitmap.getHeight()/2;
	                dst.right = x+mCountBackgroupBitmap.getWidth()/2 + mCountPaint.measureText(count)/2-s.leftMargin/2;
	                dst.bottom = s.labelHeight/2+mCountBackgroupBitmap.getHeight()/2;
                	canvas.drawBitmap(mCountBackgroupBitmap, null, dst, mTitlePaint);
                }else{
                    drawText(canvas, x-s.leftMargin, y, count,
                            labelWidth - x , mCountPaint);
	                dst.left = x-mCountBackgroupTwoBitmap.getWidth()/2 + mCountPaint.measureText(count)/2-s.leftMargin;
	                dst.top  = s.labelHeight/2-mCountBackgroupBitmap.getHeight()/2;
	                dst.right = x+mCountBackgroupTwoBitmap.getWidth()/2 + mCountPaint.measureText(count)/2-s.leftMargin;
	                dst.bottom = s.labelHeight/2+mCountBackgroupBitmap.getHeight()/2;
                	canvas.drawBitmap(mCountBackgroupTwoBitmap, null, dst, mTitlePaint);
                }
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
                float scale = (float) s.iconSize / icon.getWidth();
                canvas.translate(s.leftMargin, 0);
                canvas.scale(scale, scale);
                canvas.drawBitmap(icon, 0, 0, null);
            }
            /// M: [DEBUG.ADD] @{
            TraceHelper.traceEnd();
            /// @}
            return bitmap;
        }
    }

    public void recycleLabel(Bitmap label) {
        GalleryBitmapPool.getInstance().put(label);
    }

//********************************************************************
//*                              MTK                                 *
//********************************************************************

    /// M: [FEATURE.ADD] fancy layout @{
    public ThreadPool.Job<Bitmap> requestLabel(
            String title, String count, int sourceType, boolean isLandCamera, boolean isFancy) {
        return new PrizeLabelJob(title, count, sourceType, isLandCamera, isFancy);
    }
    /// @}
}
