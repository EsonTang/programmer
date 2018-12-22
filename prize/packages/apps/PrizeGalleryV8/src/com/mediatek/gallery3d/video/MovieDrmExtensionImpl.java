package com.mediatek.gallery3d.video;

import android.content.Context;
import android.content.DialogInterface;
import android.drm.DrmManagerClient;
import android.drm.DrmStore;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.mediatek.gallery3d.video.IMovieDrmExtension.IMovieDrmCallback;
import com.mediatek.omadrm.OmaDrmUtils;

public class MovieDrmExtensionImpl extends DefaultMovieDrmExtension {
    private static final String TAG = "Gallery2/VideoPlayer/MovieDrmExtensionImpl";
    private static final boolean LOG = true;

    @Override
    public boolean handleDrmFile(final Context context, final IMovieItem item, final IMovieDrmCallback callback) {
        boolean handle = false;
        DrmManagerClient client = ensureDrmClient(context);
        if (!OmaDrmUtils.isDrm(client, item.getUri())) {
            return false;
        }
        OmaDrmUtils.showConsumerDialog(context, client, item.getUri(),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            if (callback != null) {
                                callback.onContinue();
                            }
                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                            if (callback != null) {
                                callback.onStop();
                            }
                        }
                    }
        });
        return true;
    }

    @Override
    public boolean canShare(final Context context, final IMovieItem item) {
        return canShare(context, item.getUri());
    }

    private static DrmManagerClient sDrmClient;
    private static DrmManagerClient ensureDrmClient(final Context context) {
        if (sDrmClient == null) {
            sDrmClient = new DrmManagerClient(context.getApplicationContext());
        }
        return sDrmClient;
    }

    private static boolean canShare(final Context context, final Uri uri) {
        Log.v(TAG, "canShare(" + uri + ")");
        final DrmManagerClient client = ensureDrmClient(context);
        boolean share = false;
        boolean isDrm = false;
        try {
            isDrm = OmaDrmUtils.isDrm(client, uri);
        } catch (final IllegalArgumentException e) {
            Log.w(TAG, "canShare() : raise exception, we assume it's not a OMA DRM file");
        }

        if (isDrm) {
            int rightsStatus = DrmStore.RightsStatus.RIGHTS_INVALID;
            try {
                rightsStatus = client.checkRightsStatus(uri, DrmStore.Action.TRANSFER);
            } catch (final IllegalArgumentException e) {
                Log.w(TAG, "canShare() : raise exception, we assume it has no rights to be shared");
            }
            share = (DrmStore.RightsStatus.RIGHTS_VALID == rightsStatus);
            if (LOG) {
                Log.v(TAG, "canShare(" + uri + "), rightsStatus=" + rightsStatus);
            }
        } else {
            share = true;
        }
        Log.v(TAG, "canShare(" + uri + "), share=" + share);
        return share;
    }
}
