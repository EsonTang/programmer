/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.android.gallery3d.filtershow.filters;

import java.util.ArrayList;
import java.util.List;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.editors.ImageOnlyEditor;
import com.prize.sticker.WatermarkBean;

public class FilterImageStickerRepresentation extends FilterRepresentation {
    private WatermarkBean mWatermarkBean;
    public FilterImageStickerRepresentation(WatermarkBean watermarkBean) {
        super("ImageSticker");
        setFilterClass(ImageFilterSticker.class);
        mWatermarkBean = watermarkBean;
        setFilterType(FilterRepresentation.TYPE_STICKER);
        setTextId(R.string.borders);
        setEditorId(ImageOnlyEditor.ID);
        setShowParameterValue(false);
    }

    public String toString() {
        return "FilterSticker: " + getName();
    }

    @Override
    public FilterRepresentation copy() {
        FilterImageStickerRepresentation representation =
                new FilterImageStickerRepresentation(mWatermarkBean);
        copyAllParameters(representation);
        return representation;
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterImageStickerRepresentation) {
            FilterImageStickerRepresentation representation = (FilterImageStickerRepresentation) a;
            setName(representation.getName());
            setWatermarkBean(representation.getWatermarkBean());
        }
    }

    @Override
    public boolean equals(FilterRepresentation representation) {
        if (!super.equals(representation)) {
            return false;
        }
        if (representation instanceof FilterImageStickerRepresentation) {
            FilterImageStickerRepresentation border = (FilterImageStickerRepresentation) representation;
            if (border.mWatermarkBean == mWatermarkBean) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getTextId() {
        return R.string.none;
    }

    public boolean allowsSingleInstanceOnly() {
        return true;
    }

    public WatermarkBean getWatermarkBean() {
        return mWatermarkBean;
    }

    public void setWatermarkBean(WatermarkBean watermarkBean) {
    	mWatermarkBean = watermarkBean;
    }
}
