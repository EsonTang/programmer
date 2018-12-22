package com.mediatek.gallery3d.ext;

import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.PhotoView.Picture;
import com.android.gallery3d.ui.PositionController;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.ui.TileImageViewAdapter;

import com.mediatek.galleryframework.base.MediaData;

/**
 * IImageOptionsExt, used for op01 plugin.
 */
public interface IImageOptionsExt {
    /**
     * set MediaItem to SlideShowView.
     *
     * @param mediaItem current mediaItem
     * @internal
     */
    public void setMediaItem(MediaItem mediaItem);

    /**
     * update initScale when doing SlideShow Animation.
     *
     * @param initScale slide show default initScale
     * @return updated initScale
     * @internal
     */
    public float getImageDisplayScale(float initScale, MediaItem mediaItem);//add prize liup 20180322 cmcc , MediaItem mediaItem

    /**
     * get scale limit by mediaType.
     *
     * @param mediaData current mediaData
     * @param scale     default scale
     * @return minimal scale limit
     * @internal
     */
    public float getMinScaleLimit(MediaData mediaData, float scale);

    /**
     * update width and height of TileProvider with sceenNail size.
     *
     * @param adapter    TileImageViewAdapter
     * @param screenNail current screenNail
     * @internal
     */
    public void updateTileProviderWithScreenNail(TileImageViewAdapter adapter,
            ScreenNail screenNail);

    /**
     * update mediaType of FullPicture or ScreenNailPicture with screenNail.
     *
     * @param picture    current Picture
     * @param screenNail current screenNail
     * @internal
     */
    public void updateMediaData(Picture picture, ScreenNail screenNail);

    /**
     * update certain box's mediaType.
     *
     * @param controller PositionController
     * @param index      the index of box
     * @param mediaData  input mediaData
     * @internal
     */
    public void updateBoxMediaData(PositionController controller, int index,
            MediaData mediaData);
}
