package com.mediatek.galleryfeature.raw;

import android.content.Context;

import com.mediatek.galleryfeature.config.FeatureConfig;
import com.mediatek.galleryframework.base.BottomControlLayer;
import com.mediatek.galleryframework.base.ComboLayer;
import com.mediatek.galleryframework.base.ExtItem;
import com.mediatek.galleryframework.base.Layer;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryframework.base.MediaMember;

import java.util.ArrayList;

/**
 * One type of MediaMember special for raw. 1. Check if one MediaData is raw 2. Return the special
 * layer for raw
 */
public class RawMember extends MediaMember {
    private static final String TAG = "MtkGallery2/RawMember";
    public static final String[] RAW_MIME_TYPE = new String[] {
            "image/x-adobe-dng", "image/x-canon-cr2", "image/x-nikon-nef",
            "image/x-nikon-nrw", "image/x-sony-arw", "image/x-panasonic-rw2",
            "image/x-olympus-orf", "image/x-fuji-raf", "image/x-pentax-pef",
            "image/x-samsung-srw"
    };

    private Layer mLayer;

    /**
     * Constructor for RawMember, no special operation, but same as parent.
     * @param context
     *            The context of current application environment
     */
    public RawMember(Context context) {
        super(context);
    }

    @Override
    public boolean isMatching(MediaData md) {
        for (String mimetype : RAW_MIME_TYPE) {
            if (mimetype.equals(md.mimeType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ExtItem getItem(MediaData md) {
        return new RawItem(md);
    }

    @Override
    public Layer getLayer() {
        if (mLayer == null) {
            mLayer = new RawLayer();
        }
        return mLayer;
    }

    @Override
    public MediaData.MediaType getMediaType() {
        return MediaData.MediaType.RAW;
    }
}
