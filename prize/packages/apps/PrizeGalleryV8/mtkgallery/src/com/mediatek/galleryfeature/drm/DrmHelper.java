package com.mediatek.galleryfeature.drm;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.drm.DrmManagerClient;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.mediatek.dcfdecoder.DcfDecoder;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryframework.util.Utils;
import com.mediatek.omadrm.OmaDrmStore;
import com.mediatek.omadrm.OmaDrmUtils;

public class DrmHelper {
    private static final String TAG = "MtkGallery2/DrmHelper";
    public static final String CTA_DATA_PROTECTION_SUFFIX = ".mudp";
    public static final String DRM_SUFFIX = ".dcf";
    public static final String PLACE_HOLDER_COLOR = "#333333";
    private static DrmManagerClient sClient = null;

    public static DrmManagerClient getOmaDrmClient(Context context) {
        if (sClient == null) {
            sClient = new DrmManagerClient(context);
        }
        return sClient;
    }

    public static boolean checkRightsStatus(Context context, String filePath, int action) {
        if (null == filePath || filePath.equals("")) {
            Log.e(TAG, "<checkRightsStatus> got null filepath");
        }
        int valid = getOmaDrmClient(context).checkRightsStatus(filePath, action);
        return valid == DrmStore.RightsStatus.RIGHTS_VALID;
    }

    public static boolean hasRightsToShow(Context context, String filePath, boolean isVideo) {
        int action = isVideo ? DrmStore.Action.PLAY : DrmStore.Action.DISPLAY;
        return checkRightsStatus(context, filePath, action);
    }

    public static boolean isFLDrm(int drmMethod) {
        return drmMethod == OmaDrmStore.Method.FL;
    }

    public static boolean isDrmFile(String filePath) {
        return filePath != null && filePath.toLowerCase().endsWith(DRM_SUFFIX);
    }

    public static boolean isDataProtectionFile(String filePath) {
        return filePath != null && filePath.toLowerCase().endsWith(CTA_DATA_PROTECTION_SUFFIX);
    }

    public static Bitmap getLockIcon(Context context, String filePath) {
        return OmaDrmUtils.getOriginalLockIcon(DrmHelper.getOmaDrmClient(context), context
                .getResources(), filePath);
    }

    public static void clearToken(Context context, String tokenKey, String token) {
        OmaDrmUtils.clearToken(getOmaDrmClient(context), tokenKey, token);
    }

    public static boolean isTokenValid(Context context, String tokenKey, String token) {
        return OmaDrmUtils.isTokenValid(getOmaDrmClient(context), tokenKey, token);
    }

    public static byte[] forceDecryptFile(String filePath, boolean consume) {
        if (null == filePath
                || (!filePath.toLowerCase().endsWith(DRM_SUFFIX) && !filePath.toLowerCase()
                        .endsWith(CTA_DATA_PROTECTION_SUFFIX))) {
            return null;
        }
        DcfDecoder dcfDecoder = new DcfDecoder();
        return dcfDecoder.forceDecryptFile(filePath, consume);
    }

    /**
     * Create dialog for display drm image information. If there has special characters file scheme
     * Uri, getPath function should truncate the Uri. So should use substring function for absolute
     * path.
     * @param context
     *            for show protection Info Dialog.
     * @param uri
     *            the uri of the drm image.
     */
    public static void showProtectionInfoDialog(final Context context, final Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())
                && Utils.hasSpecialCharaters(uri)) {
            String filePath = uri.toString().substring(Utils.SIZE_SCHEME_FILE);
            OmaDrmUtils.showProtectionInfoDialog(context, sClient, filePath);
        } else {
            OmaDrmUtils.showProtectionInfoDialog(context, sClient, uri);
        }
    }

    /**
     * Query filePath by Uri.
     * @param context The context for query.
     * @param uri The file Uri.
     * @return The file path.
     */
    public static String convertUriToPath(Context context, Uri uri) {
        if (null == uri) {
            return null;
        }
        String path = null;
        String scheme = uri.getScheme();
        if (null == scheme || scheme.equals("")
                || scheme.equals(ContentResolver.SCHEME_FILE)) {
            path = uri.getPath();
        } else if (scheme.equals("http")) {
            path = uri.toString();
        } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            String[] projection = new String[] {
                MediaStore.MediaColumns.DATA
            };
            Cursor cursor = null;
            try {
                cursor =
                        context.getContentResolver().query(uri, projection, null, null,
                                null);
                if (null == cursor || 0 == cursor.getCount() || !cursor.moveToFirst()) {
                    throw new IllegalArgumentException("Given Uri could not be found"
                            + " in media store");
                }
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                path = cursor.getString(pathIndex);
            } catch (SQLiteException e) {
                throw new IllegalArgumentException("Given Uri is not formatted in a way "
                        + "so that it can be found in media store.");
            } finally {
                if (null != cursor) {
                    cursor.close();
                }
            }
        } else {
            throw new IllegalArgumentException("Given Uri scheme is not supported");
        }
        return path;
    }
}
