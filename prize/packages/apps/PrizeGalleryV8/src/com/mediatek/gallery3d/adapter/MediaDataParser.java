package com.mediatek.gallery3d.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.util.Log;

import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.LocalVideo;
import com.android.gallery3d.data.UriImage;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryframework.util.Utils;

public class MediaDataParser {
    private static final String TAG = "MtkGallery2/MediaDataParser";

    public static MediaData parseLocalImageMediaData(Cursor cursor) {
        MediaData data = new MediaData();
        data.width = cursor.getInt(LocalImage.INDEX_WIDTH);
        data.height = cursor.getInt(LocalImage.INDEX_HEIGHT);
        data.orientation = cursor.getInt(LocalImage.INDEX_ORIENTATION);
        data.caption = cursor.getString(LocalImage.INDEX_CAPTION);
        data.mimeType = cursor.getString(LocalImage.INDEX_MIME_TYPE);
        data.isDRM = cursor.getInt(LocalImage.INDEX_IS_DRM);
        data.drmMethod = cursor.getInt(LocalImage.INDEX_DRM_METHOD);
        data.groupID = cursor.getLong(LocalImage.INDEX_GROUP_ID);
        data.groupCount = cursor.getInt(LocalImage.INDEX_GROUP_COUNT);
        data.groupIndex = cursor.getInt(LocalImage.INDEX_GROUP_INDEX);
        data.bestShotMark = cursor.getInt(LocalImage.INDEX_IS_BEST_SHOT);
        data.filePath = cursor.getString(LocalImage.INDEX_DATA);
        data.bucketId = cursor.getInt(LocalImage.INDEX_BUCKET_ID);
        data.id = cursor.getLong(LocalImage.INDEX_ID);
        data.fileSize = cursor.getLong(LocalImage.INDEX_SIZE);
        data.depth_image = cursor.getInt(LocalImage.INDEX_CAMERA_REFOCUS);
        data.dateModifiedInSec = cursor.getLong(LocalImage.INDEX_DATE_MODIFIED);
        data.uri = Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                .appendPath(String.valueOf(data.id)).build();
        return data;
    }

    public static MediaData parseLocalVideoMediaData(LocalVideo item, Cursor cursor) {
        MediaData data = new MediaData();
        data.width = item.width;
        data.height = item.height;
        data.orientation = cursor.getInt(LocalVideo.INDEX_VIDEO_ORIENTATION);
        data.mimeType = cursor.getString(LocalVideo.INDEX_MIME_TYPE);
        data.isDRM = cursor.getInt(LocalVideo.INDEX_IS_DRM);
        data.drmMethod = cursor.getInt(LocalVideo.INDEX_DRM_METHOD);
        data.filePath = cursor.getString(LocalVideo.INDEX_DATA);
        data.bucketId = cursor.getInt(LocalVideo.INDEX_BUCKET_ID);
        data.isVideo = true;
        data.isSlowMotion = Utils.parseSlowMotionFromString(cursor
                .getString(LocalVideo.INDEX_IS_SLOWMOTION));
        data.duration = cursor.getInt(LocalVideo.INDEX_DURATION);
        data.caption = cursor.getString(LocalImage.INDEX_CAPTION);
        data.dateModifiedInSec = cursor.getLong(LocalVideo.INDEX_DATE_MODIFIED);
        data.id = cursor.getLong(LocalVideo.INDEX_ID);
        data.uri = Video.Media.EXTERNAL_CONTENT_URI.buildUpon()
                .appendPath(String.valueOf(data.id)).build();
        return data;
    }

    public static MediaData parseUriImageMediaData(UriImage item, Context context) {
        Uri uri = item.getContentUri();
        MediaData data = new MediaData();
        data.mimeType = item.getMimeType();
        data.width = item.getWidth();
        data.height = item.getHeight();
        data.orientation = item.getRotation();
        data.uri = uri;
        if (FeatureHelper.isLocalUri(uri)) {
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                if (Utils.hasSpecialCharaters(uri)) {
                    data.filePath = data.uri.toString().substring(Utils.SIZE_SCHEME_FILE);
                } else {
                    data.filePath = data.uri.getPath();
                }
            } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                MediaData mediaData = query(uri, context);
                if (mediaData != null) {
                    mediaData.uri = uri;
                    data = mediaData;
                }
            }
        }
        return data;
    }

    public static MediaData parseLocalImageMediaData(LocalImage item) {
        MediaData data = new MediaData();
        data.mimeType = item.getMimeType();
        data.filePath = item.getFilePath();
        data.uri = item.getContentUri();
        data.width = item.getWidth();
        data.height = item.getHeight();
        data.orientation = item.getRotation();
        return data;
    }

    private static MediaData query(Uri uri, Context context) {
        Log.d(TAG, "<query> data = " + uri);
        MediaData data = null;
        Cursor cursor = MediaData.queryImage(context, uri, null, null, null);
        try {
            if (cursor != null) {
                data = MediaData.parseImage(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return data;
    }
}
