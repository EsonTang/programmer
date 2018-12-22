/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.android.gallery3d.filtershow.filters;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.HashMap;


public class ImageFilterSticker extends ImageFilter {
    private static final float NINEPATCH_ICON_SCALING = 10;
    private static final float BITMAP_ICON_SCALING = 1 / 3.0f;
    private static final String LOGTAG = "Gallery2/ImageFilterBorder";
    private FilterImageStickerRepresentation mParameters = null;
    private Resources mResources = null;

    private HashMap<Integer, Drawable> mDrawables = new HashMap<Integer, Drawable>();

    public ImageFilterSticker() {
        mName = "Sticker";
    }

    public void useRepresentation(FilterRepresentation representation) {
        FilterImageStickerRepresentation parameters = (FilterImageStickerRepresentation) representation;
        mParameters = parameters;
    }

    public FilterImageStickerRepresentation getParameters() {
        return mParameters;
    }

    public void freeResources() {
       mDrawables.clear();
    }

    public Bitmap applyHelper(Bitmap bitmap, float scale1, float scale2 ) {
        Bitmap bm = BitmapFactory.decodeResource(mResources, getParameters().getWatermarkBean().getThumbId());
        return bm;
    }

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (getParameters() == null || getParameters().getWatermarkBean().getThumbId() == 0) {
            return bitmap;
        }
        float scale2 = scaleFactor * 2.0f;
        float scale1 = 1 / scale2;
        return applyHelper(bitmap, scale1, scale2);
    }

    public void setResources(Resources resources) {
        if (mResources != resources) {
            mResources = resources;
            mDrawables.clear();
        }
    }

    public Drawable getDrawable(int rsc) {
        Drawable drawable = mDrawables.get(rsc);
        if (drawable == null && mResources != null && rsc != 0) {
            /// M: [BUG.MODIFY] @{
            /*
             * drawable = new BitmapDrawable(mResources, BitmapFactory.decodeResource(mResources, rsc));
             */
            // adjust sample size, preventing OOM @{
            // drawable = new BitmapDrawable(mResources, BitmapFactory.decodeResource(mResources, rsc));
            BitmapFactory.Options options = new BitmapFactory.Options();
            // dynamically adjust sample size according to resolution
            options.inSampleSize = sStickerSampleSize;
            Log.i(LOGTAG, "getDrawable, set border sampleSize to " + options.inSampleSize);
            drawable = new BitmapDrawable(mResources, BitmapFactory.decodeResource(mResources, rsc, options));
            /// @}
            mDrawables.put(rsc, drawable);
        }
        return drawable;
    }

    /// M: [BUG.ADD] @{
    // fix OOM issue caused by borders
    private static int sStickerSampleSize = 2;
    public static void setStickerSampleSize(int sampleSize) {
        sStickerSampleSize = sampleSize;
    }
    /// @}

}
