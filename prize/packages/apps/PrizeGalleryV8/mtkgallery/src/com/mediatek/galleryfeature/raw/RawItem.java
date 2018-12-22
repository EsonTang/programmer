package com.mediatek.galleryfeature.raw;

import android.content.Context;

import com.mediatek.galleryframework.base.ExtItem;
import com.mediatek.galleryframework.base.ExtItem.SupportOperation;
import com.mediatek.galleryframework.base.MediaData;

import java.util.ArrayList;

/**
 * The item for all raw types.
 */
class RawItem extends ExtItem {
    public RawItem(Context context, MediaData md) {
        super(context, md);
    }

    public RawItem(MediaData md) {
        super(md);
    }

    @Override
    public ArrayList<SupportOperation> getNotSupportedOperations() {
        ArrayList<SupportOperation> res = new ArrayList<SupportOperation>();
        res.add(SupportOperation.EDIT);
        return res;
    }
}
