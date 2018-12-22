package com.mediatek.galleryfeature.drm;

import android.content.Context;

import com.android.gallery3d.ui.Log;
import com.mediatek.gallery3d.adapter.FeatureHelper;
import com.mediatek.galleryframework.base.ExtItem;
import com.mediatek.galleryframework.base.Layer;
import com.mediatek.galleryframework.base.MediaCenter;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryframework.base.MediaMember;
import com.mediatek.galleryframework.base.Player;
import com.mediatek.galleryframework.base.Player.OutputType;
import com.mediatek.galleryframework.base.ThumbType;
import com.mediatek.galleryframework.util.MtkLog;
import com.mediatek.omadrm.OmaDrmUtils;

/**
 * Drm Member.
 */
public class DrmMember extends MediaMember {
    private static final String TAG = "MtkGallery2/DrmMember";
    private MediaCenter mMediaCenter;

    /**
     * Constructor.
     * @param context
     *            The context for Drm.
     * @param center
     *            The media center.
     */
    public DrmMember(Context context, MediaCenter center) {
        super(context);
        mMediaCenter = center;
    }

    @Override
    public boolean isMatching(MediaData md) {
        boolean isDrm = md.isDRM != 0;
        String fileName = md.filePath;
        // all DRM file name is end with .dcf,should not modify '.dcf'
        if (isDrm
                && !(DrmHelper.isDataProtectionFile(fileName) || DrmHelper.isDrmFile(fileName))) {
            MtkLog.d(TAG, "<isMatching> DRM fileName = " + fileName);
            return false;
        }
        boolean isCTADataProtection = DrmHelper.isDataProtectionFile(fileName);
        if (isCTADataProtection) {
            md.drmMethod = com.mediatek.omadrm.OmaDrmStore.Method.FL;
        }
        if (!isDrm && (md.uri != null) && (fileName == null || fileName.equals(""))) {
            isDrm =
                    OmaDrmUtils.isDrm(DrmHelper.getOmaDrmClient(mContext), md.uri)
                            || DrmHelper.isDataProtectionFile(md.uri.toString());
            if (isDrm) {
                md.isDRM = 1;
                try {
                    md.filePath = DrmHelper.convertUriToPath(mContext, md.uri);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "<isMatching> is fail: " + e);
                }
            }
            Log.d(TAG, "<isMatching> filePath = " + md.filePath + " is drm " + md.isDRM
                    + "  md.uri = " + md.uri);
        }
        return isDrm;
    }

    @Override
    public Player getPlayer(MediaData md, ThumbType type) {
        if (type == ThumbType.MIDDLE) {
            return new DrmPlayer(mContext, md, OutputType.TEXTURE, mMediaCenter);
        }
        return null;
    }

    public Layer getLayer() {
        return new DrmLayer(mMediaCenter);
    }

    @Override
    public ExtItem getItem(MediaData md) {
        return new DrmItem(mContext, md, mMediaCenter);
    }

    @Override
    public MediaData.MediaType getMediaType() {
        return MediaData.MediaType.DRM;
    }
}
