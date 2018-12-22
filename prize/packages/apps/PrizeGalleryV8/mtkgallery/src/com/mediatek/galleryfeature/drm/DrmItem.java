package com.mediatek.galleryfeature.drm;

import android.content.Context;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.mediatek.galleryframework.base.ExtItem;
import com.mediatek.galleryframework.base.MediaCenter;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryframework.base.MediaData.MediaType;
import com.mediatek.galleryframework.base.ThumbType;
import com.mediatek.galleryframework.util.BitmapUtils;
import com.mediatek.galleryframework.util.GalleryPluginUtils;
import com.mediatek.galleryframework.util.MtkLog;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Create bitmap and get supported operations.
 */
public class DrmItem extends ExtItem {
    private static final String TAG = "MtkGallery2/DrmItem";
    private final static int FLAG_DRM = 0x100;
    // We try to scale up the image to fill the screen. But in order not to
    // scale too much for small icons, we limit the max up-scaling factor here.
    private static final float SCALE_LIMIT = 4;
    private final static float RATIO_WITH_SCREEN = 0.5f;
    private static int sLength = 0;
    private MediaCenter mMediaCenter;
    private ExtItem mRealItem;
    private HashMap<Integer, Boolean> mSupportedOperations = new HashMap<Integer, Boolean>();

    /**
     * Constructor.
     * @param context
     *            the Context.
     * @param data
     *            The drm data.
     * @param center
     *            The media center.
     */
    public DrmItem(Context context, MediaData data, MediaCenter center) {
        super(context, data);
        mMediaCenter = center;
        mRealItem = mMediaCenter.getRealItem(mMediaData);
        if (sLength <= 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm =
                    (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(metrics);
            int minPixels = Math.min(metrics.heightPixels, metrics.widthPixels);
            float scaleLimit =
                    GalleryPluginUtils.getImageOptionsPlugin().getMinScaleLimit(data,
                            SCALE_LIMIT);
            int lenght = (int) (RATIO_WITH_SCREEN * (minPixels / scaleLimit));
            sLength = Integer.highestOneBit(lenght);
        }
        MtkLog.i(TAG, "<DrmItem> LENGTH = " + sLength + ", caption = " + mMediaData.caption);
    }

    public MediaData.MediaType getType() {
        return MediaData.MediaType.DRM;
    }

    @Override
    public Thumbnail getThumbnail(ThumbType thumbType) {
        MtkLog.i(TAG, "<getThumbnail> caption = " + mMediaData.caption + " mRealItem = "
                + mRealItem);
        if (mRealItem == null || mMediaData.filePath == null || mMediaData.filePath.equals("")) {
            MtkLog.i(TAG, "<getThumbnail> mRealItem == null, return");
            return new Thumbnail(null, false);
        }
        Bitmap bitmap = null;
        if (DrmHelper.isDataProtectionFile(mMediaData.filePath)) {
            bitmap = getCTAThumbNail(thumbType);
        } else {
            bitmap = getDrmThumbnail(thumbType);
        }
        if (bitmap == null) {
            MtkLog.i(TAG, "<getThumbnail> bitmap == null, return");
            return new Thumbnail(null, false);
        }
        bitmap = BitmapUtils.ensureGLCompatibleBitmap(bitmap);
        switch (thumbType) {
            case MICRO:
            case MIDDLE:
            case HIGHQUALITY:
            case FANCY:
                return new Thumbnail(bitmap, bitmap == null && mMediaData.isVideo);
            default:
                MtkLog.i(TAG, "<getThumbnail> invalid thumb type " + thumbType + ", return");
                return new Thumbnail(null, false);
        }
    }

    @Override
    public ArrayList<SupportOperation> getSupportedOperations() {
        ArrayList<SupportOperation> res = new ArrayList<SupportOperation>();
        if ((!mMediaData.isVideo) && checkRightsStatus(DrmStore.Action.WALLPAPER)) {
            res.add(SupportOperation.SETAS);
        }
        if (checkRightsStatus(DrmStore.Action.TRANSFER)) {
            res.add(SupportOperation.SHARE);
        }
        if ((!mMediaData.isVideo) && checkRightsStatus(DrmStore.Action.PRINT)) {
            res.add(SupportOperation.PRINT);
        }
        if (DrmHelper.isDrmFile(mMediaData.filePath)) {
            res.add(SupportOperation.PROTECTION_INFO);
        }
        return res;
    }

    @Override
    public ArrayList<SupportOperation> getNotSupportedOperations() {
        ArrayList<SupportOperation> res = new ArrayList<SupportOperation>();
        res.add(SupportOperation.FULL_IMAGE);
        res.add(SupportOperation.EDIT);
        res.add(SupportOperation.CROP);
        res.add(SupportOperation.ROTATE);
        res.add(SupportOperation.MUTE);
        res.add(SupportOperation.TRIM);
        if (mMediaData.isVideo || !checkRightsStatus(DrmStore.Action.WALLPAPER)) {
            res.add(SupportOperation.SETAS);
        }
        if (!checkRightsStatus(DrmStore.Action.TRANSFER)) {
            res.add(SupportOperation.SHARE);
        }
        if (mMediaData.isVideo || !checkRightsStatus(DrmStore.Action.PRINT)) {
            res.add(SupportOperation.PRINT);
        }
        return res;
    }

    @Override
    public boolean isNeedToCacheThumb(ThumbType thumbType) {
        return false;
    }

    @Override
    public boolean isNeedToGetThumbFromCache(ThumbType thumbType) {
        return false;
    }

    /**
     * Decode bitmap by thumb type.
     * @param thumbType
     *            The thumb type.
     * @return The bitmap for drm texture.
     */
    public Bitmap getDrmThumbnail(ThumbType thumbType) {
        int targetSize = getDrmTargeSize(thumbType);
        MtkLog.i(TAG, "<getDrmThumbnail> mMediaData " + mMediaData.filePath + " targetSize = "
                + targetSize);
        Bitmap bitmap = getDrmBitmap(targetSize);
        if (thumbType == ThumbType.MICRO) {
            return BitmapUtils.resizeAndCropCenter(bitmap, targetSize, true);
        } else {
            return BitmapUtils.resizeDownBySideLength(bitmap, targetSize, true);
        }
    }

    @Override
    public Bitmap getOriginRatioBitmap(BitmapFactory.Options options) {
        if (mRealItem == null) {
            return super.getOriginRatioBitmap(options);
        }
        return mRealItem.getOriginRatioBitmap(options);
    }

    @Override
    public boolean supportHighQuality() {
        return false;
    }

    @Override
    public void decodeBounds() {
        if (mMediaData.width <= 0 || mMediaData.height <= 0) {
            if (mRealItem == null) {
                super.decodeBounds();
            } else {
                mRealItem.decodeBounds();
                mWidth = mRealItem.getWidth();
                mHeight = mRealItem.getHeight();
            }
        }

    }

    @Override
    public int getWidth() {
        if (super.getWidth() <= 0) {
            decodeBounds();
        }
        return super.getWidth() > 0 ? super.getWidth() : sLength;
    }

    @Override
    public int getHeight() {
        if (super.getHeight() <= 0) {
            decodeBounds();
        }
        return super.getHeight() > 0 ? super.getHeight() : sLength;
    }

    private boolean checkRightsStatus(int action) {
        Boolean value = mSupportedOperations.get(action);
        if (value == null) {
            if (mMediaData.filePath != null && !mMediaData.filePath.equals("")) {
                boolean right =
                        DrmHelper.checkRightsStatus(mContext, mMediaData.filePath, action);
                mSupportedOperations.put(action, right);
                return right;
            } else {
                return false;
            }
        } else {
            return value.booleanValue();
        }
    }

    private int getDrmTargeSize(ThumbType thumbType) {
        switch (thumbType) {
            case FANCY:
                return thumbType.getTargetSize();
            case MICRO:
            case MIDDLE:
            case HIGHQUALITY:
                return sLength;
            default:
                return -1;
        }
    }

    private Bitmap getDrmBitmap(int targetSize) {
        int width = getWidth();
        int height = getHeight();
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (targetSize != -1 && (width > 0 && height > 0)) {
            options.inSampleSize =
                    BitmapUtils.computeSampleSizeLarger(width, height, targetSize);
        }
        options.inSampleSize |= FLAG_DRM;
        Bitmap bitmap = getOriginRatioBitmap(options);
        if (bitmap == null) {
            if (width > 0 && height > 0) {
                bitmap = createBackground(width, height);
            } else {
                bitmap = createBackground(targetSize, targetSize);
            }
        }
        return bitmap;
    }

    private Bitmap createBackground(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor(DrmHelper.PLACE_HOLDER_COLOR));
        return bitmap;
    }

    private Bitmap getCTAThumbNail(ThumbType thumbType) {
        int targetSize = getDrmTargeSize(thumbType);
        MtkLog.i(TAG, "<getCTAThumbNail> mMediaData " + mMediaData.filePath + " targetSize = "
                + targetSize);
        int width = getWidth();
        int height = getHeight();
        Bitmap bitmap = null;
        if (width > 0 && height > 0) {
            bitmap = createBackground(width, height);
        } else {
            bitmap = createBackground(targetSize, targetSize);
        }
        return bitmap;
    }
}
