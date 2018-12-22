
package com.mediatek.gallery3d.video;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.Locale;

/**
 * Util class for Movie functions. *
 */
public class MovieUtils {
    private static final String TAG = "Gallery2/VideoPlayer/MovieUtils";
    private static final boolean LOG = true;
    private static final String HTTP_LIVE_SUFFIX = ".m3u8";

    // video type
    public static final int VIDEO_TYPE_LOCAL = 0;
    public static final int VIDEO_TYPE_HTTP = 1;
    public static final int VIDEO_TYPE_RTSP = 2;
    public static final int VIDEO_TYPE_LIVE = 3;

    private MovieUtils() {
    }

    /**
     * Judge the video type
     *
     * @param uri The video uri.
     * @param mimeType The mimeType of the video.
     */
    public static int judgeStreamingType(Uri uri, String mimeType) {
        int videoType = VIDEO_TYPE_LOCAL;
        Log.v(TAG, "judgeStreamingType entry with uri is: " + uri
                    + " and mimeType is: " + mimeType);
        if (uri == null) {
            return -1;
        }
        if (isRtspStreaming(uri, mimeType)) {
            videoType = VIDEO_TYPE_RTSP;
        } else if (isHttpStreaming(uri, mimeType)
                || isHttpLiveStreaming(uri, mimeType)) {
            videoType = VIDEO_TYPE_HTTP;
        } else {
            videoType = VIDEO_TYPE_LOCAL;
        }
        Log.v(TAG, "videoType is " + videoType);
        return videoType;
    }

    /**
     * Whether current video(Uri) is RTSP streaming or not.
     *
     * @param uri
     * @param mimeType
     * @return
     */
    public static boolean isRtspStreaming(Uri uri, String mimeType) {
        boolean rtsp = false;
        if (uri != null) {
            if ("application/sdp".equals(mimeType)) {
                rtsp = true;
            } else if (uri.toString().toLowerCase(Locale.ENGLISH)
                    .endsWith(".sdp")) {
                rtsp = true;
            } else if ("rtsp".equalsIgnoreCase(uri.getScheme())) {
                rtsp = true;
            }
        }
        return rtsp;
    }

    /**
     * Whether current video(Uri) is HTTP streaming or not.
     *
     * @param uri
     * @param mimeType
     * @return
     */
    public static boolean isHttpStreaming(Uri uri, String mimeType) {
        boolean http = false;
        if (uri != null) {
            if (("http".equalsIgnoreCase(uri.getScheme()) || "https"
                    .equalsIgnoreCase(uri.getScheme()))
                    && !(uri.toString().toLowerCase(Locale.ENGLISH)
                            .contains(HTTP_LIVE_SUFFIX))
                    && !(uri.toString().toLowerCase(Locale.ENGLISH)
                            .contains(".sdp"))
                    && !(uri.toString().toLowerCase(Locale.ENGLISH)
                            .contains(".smil"))) {
                http = true;
            }
        }
        return http;
    }

    /**
     * Whether current video(Uri) is http live streaming or not.
     *
     * @param uri The video Uri.
     * @param mimeType The mimeType of the video.
     * @return True if the video is a http live streaming,false otherwise.
     */
    public static boolean isHttpLiveStreaming(Uri uri, String mimeType) {
        boolean isHttpLive = false;
        if (uri != null) {
            if (("http".equalsIgnoreCase(uri.getScheme()) || "https"
                    .equalsIgnoreCase(uri.getScheme()))
                    && (uri.toString().toLowerCase(Locale.ENGLISH)
                            .contains(HTTP_LIVE_SUFFIX))) {
                isHttpLive = true;
            }
        }
        return isHttpLive;
    }

    /**
     * Whether current video(Uri) is local file or not.
     *
     * @param uri
     * @param mimeType
     * @return
     */
    public static boolean isLocalFile(Uri uri, String mimeType) {
        boolean local = (!isRtspStreaming(uri, mimeType)
                && !isHttpStreaming(uri, mimeType) && !isHttpLiveStreaming(uri,
                mimeType));
        Log.v(TAG, "isLocalFile(" + uri + ", " + mimeType + ") return "
                    + local);
        return local;
    }

    /**
     * Check whether the video is a local video or not.
     *
     * @return True if the video is a local video,false otherwise.
     */
    public static boolean isLocalFile(int videoType) {
        if (videoType == VIDEO_TYPE_LOCAL) {
            Log.v(TAG, "isLocalFile() is local");
            return true;
        }
        Log.v(TAG, "isLocalFile() is not local video type: " + videoType);
        return false;
    }

    /**
     * Check whether the video is a rtsp streaming video or not.
     *
     * @return True if the video is a rtsp streaming video,false otherwise.
     */
    public static boolean isRTSP(int videoType) {
        if (videoType == VIDEO_TYPE_RTSP) {
            Log.v(TAG, "isRTSP() is RTSP");
            return true;
        }
        Log.v(TAG, "isRTSP() is not RTSP videoType: " + videoType);
        return false;
    }

    // keep the api for operator
    @Deprecated
    public static boolean isRtspOrSdp(int videoType) {
        return isRTSP(videoType);
    }

    /**
     * Check whether the video is a live streaming video or not.
     *
     * @return True if the video is a live streaming video,false otherwise.
     */
    public static boolean isLiveStreaming(int videoType) {
        if (videoType == VIDEO_TYPE_LIVE) {
            Log.v(TAG, "isLiveStreaming() is live streaming");
            return true;
        }
        Log.v(TAG, "isLiveStreaming() is not live video type: " + videoType);
        return false;
    }

    /**
     * Whether current video is support trim and mute?
     */
    public static boolean isSupportTrim(String mimeType) {
        Log.v(TAG, "isSupportTrim(" + mimeType + ")");
        if (mimeType != null) {
            return mimeType.toLowerCase().equals("video/mp4")
                    || mimeType.toLowerCase().equals("video/3gpp")
                    || mimeType.toLowerCase().equals("video/quicktime");
        }
        return false;
    }

    /**
     * Whether current video is support trim and mute?
     */
    public static boolean isSupportTrim(final Context context, Uri uri) {
        String mimeType = null;
        Cursor cursor = null;
        Log.v(TAG, "isSupportTrim(" + uri + ")");
        try {
            cursor = context.getContentResolver().query(uri,
                    new String[] {
                        MediaStore.Video.Media.MIME_TYPE
                    }, null,
                    null, null);

            if (cursor == null) {
                String data = Uri.decode(uri.toString());
                if (data == null) {
                    return false;
                }
                data = data.replaceAll("'", "''");
                final String where = "_data LIKE '%"
                        + data.replaceFirst("file:///", "") + "'";

                cursor = context.getContentResolver().query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        new String[] {
                            MediaStore.Video.Media.MIME_TYPE
                        },
                        where, null, null);
            }

            if (cursor != null && cursor.moveToFirst()) {
                mimeType = cursor.getString(0);
            }
        } catch (final SQLiteException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException e) {
            // if this exception happen, return false.
            Log.v(TAG, "ContentResolver query IllegalArgumentException");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isSupportTrim(mimeType);
    }

    private static final String EXTRA_CAN_SHARE = "CanShare";

    public static boolean canShare(Bundle extra) {
        boolean canshare = true;
        if (extra != null) {
            canshare = extra.getBoolean(EXTRA_CAN_SHARE, true);
        }
        Log.v(TAG, "canShare(" + extra + ") return " + canshare);
        return canshare;
    }

    /**
     * Convert byteArry to Drawable
     *
     * @param byteArry The picture in byte array.
     * @return A Drawable.
     */
    public static Drawable bytesToDrawable(byte[] byteArray) {
        Bitmap bitmap = null;
        Drawable drawable = null;
        int length = byteArray.length;
        if (length != 0) {
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, length);
            drawable = new BitmapDrawable(bitmap);
        }
        Log.v(TAG, "bytesToDrawable() exit with the drawable is " + drawable);
        return drawable;
    }

    /**
     * Judge whether the uri is valid or not, the uri is valid means that the
     * file related file exist.
     * The uri should be file content or media content
     *
     * @param uri Current video uri
     * @return True if the uri is valid , false otherwise.
     */
    public static boolean isUriValid(final Context context, final Uri uri) {
        Log.v(TAG, "isUriValid() entry with the uri is " + uri);
        boolean isValid = false;
        if (uri != null) {
            String[] proj = {
                MediaStore.Video.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, proj, null, null, null);
                if (cursor == null) {
                    String data = Uri.decode(uri.toString());
                    if (data == null) {
                        return false;
                    }
                    data = data.replaceAll("'", "''");
                    final String where = "_data LIKE '%" + data.replaceFirst("file:///", "") + "'";

                    cursor = context.getContentResolver().query(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            proj, where, null, null);
                }
                if (cursor != null) {
                    int index = 0;
                    if (cursor.getCount() != 0 && cursor.moveToFirst()) {
                        index = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                        if (index != -1) {
                            String fileName = cursor.getString(index);
                            if (fileName != null) {
                                File file = new File(fileName);
                                if (file.exists()) {
                                    isValid = true;
                                }
                            }
                        }
                    }
                }
            } catch (final SQLiteException ex) {
                Log.e(TAG, "isUriValid() Ignore error for lacking data column from the source");
                ex.printStackTrace();
                return false;
            } catch (final IllegalArgumentException ex) {
                Log.e(TAG, "isUriValid() IllegalArgumentException happen, return false");
                return false;
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
        }
        Log.v(TAG, "isUriValid() exit with isValid is " + isValid);
        return isValid;
    }
}
