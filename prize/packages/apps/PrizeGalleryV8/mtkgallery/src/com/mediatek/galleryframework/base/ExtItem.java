package com.mediatek.galleryframework.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.mediatek.gallery3d.util.DecodeSpecLimitor;
import com.mediatek.galleryframework.util.DecodeUtils;
import com.mediatek.galleryframework.util.Utils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ExtItem {
    private static final String TAG = "MtkGallery2/ExtItem";
    protected Context mContext;
    protected int mWidth;
    protected int mHeight;

    public enum SupportOperation {
        DELETE, ROTATE, SHARE, CROP, SHOW_ON_MAP, SETAS,
        FULL_IMAGE, PLAY, CACHE, EDIT, INFO, TRIM, UNLOCK,
        BACK, ACTION, CAMERA_SHORTCUT, MUTE, PRINT, EXPORT,
        PROTECTION_INFO
    }

    public class Thumbnail {
        public Bitmap mBitmap;
        public boolean mStillNeedDecode;

        // if new Thumbnail(null, true), it will still decode thumbnail with google flow
        // if new Thumbnail(null, false), it will not decode thumbnail, display as no thumbnail
        public Thumbnail(Bitmap b, boolean stillNeedDecode) {
            mBitmap = b;
            mStillNeedDecode = stillNeedDecode;
        }
    }

    protected MediaData mMediaData;
    protected boolean mIsEnable = true;

    public ExtItem(Context context, MediaData md) {
        mContext = context;
        mMediaData = md;
    }

    public ExtItem(MediaData md) {
        mMediaData = md;
    }

    public synchronized void updateMediaData(MediaData md) {
        mMediaData = md;
    }

    public Thumbnail getThumbnail(ThumbType thumbType) {
        return null;
    }

    public Bitmap getOriginRatioBitmap(BitmapFactory.Options options) {
        if (mMediaData.isVideo) {
            return DecodeUtils.decodeVideoThumbnail(mMediaData.filePath, options);
        } else {
            if (mMediaData.filePath != null) {
                return DecodeUtils.decodeBitmap(mMediaData.filePath, options);
            } else if (mMediaData.uri != null) {
                return DecodeUtils.decodeBitmap(mContext, mMediaData.uri, options);
            }
        }
        return null;
    }

    public ArrayList<SupportOperation> getSupportedOperations() {
        return null;
    }

    public ArrayList<SupportOperation> getNotSupportedOperations() {
        return null;
    }

    public boolean supportHighQuality() {
        return true;
    }

    public void setEnable(boolean isEnable) {
        mIsEnable = isEnable;
    }

    public boolean isEnable() {
        return mIsEnable;
    }

    public void delete() {
    }

    public boolean isNeedToCacheThumb(ThumbType thumbType) {
        return true;
    }

    public boolean isNeedToGetThumbFromCache(ThumbType thumbType) {
        return true;
    }

    public boolean isAllowPQWhenDecodeCache(ThumbType thumbType) {
        return true;
    }

    public Uri[] getContentUris() {
        return null;
    }

    // The index and string must match with MediaDetails.INDEX_XXX - 1
    public String[] getDetails() {
        return null;
    }

    public boolean isDeleteOriginFileAfterEdit() {
        if (mMediaData.subType == MediaData.SubType.CONSHOT) {
            return false;
        }
        return true;
    }

    public int getWidth() {
        return mWidth > 0 ? mWidth : mMediaData.width;
    }

    public int getHeight() {
        return mHeight > 0 ? mHeight : mMediaData.height;
    }

    /**
     * Decode bounds of the image.
     */
    public void decodeBounds() {
        if (mContext == null) {
            return;
        }
        if (mMediaData.isVideo) {
            return;
        }
        if (DecodeSpecLimitor.isOutOfSpecLimit(mMediaData.fileSize, mMediaData.width,
                mMediaData.height, mMediaData.mimeType)) {
            return;
        }
        BitmapFactory.Options boundsOption = new BitmapFactory.Options();
        boundsOption.inJustDecodeBounds = true;
        if (mMediaData.filePath != null) {
            DecodeUtils.decodeBitmap(mMediaData.filePath, boundsOption);
        } else if (mMediaData.uri != null) {
            DecodeUtils.decodeBitmap(mContext, mMediaData.uri, boundsOption);
        }
        mWidth = boundsOption.outWidth;
        mHeight = boundsOption.outHeight;
        Log.d(TAG, "<decodeBounds> mWidth = " + mWidth + " mHeight = " + mHeight);
    }
}
