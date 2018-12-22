package com.prize.appcenter.ui.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import com.nostra13.universalimageloader.core.DisplayLargerImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.prize.appcenter.R;

/**
 * IML工具加载图片选项
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class UILimageUtil {

    private static DisplayLargerImageOptions getBaseUILoptions(int imageResourse) {
        DisplayLargerImageOptions options = new DisplayLargerImageOptions.Builder()
                .showImageOnLoading(imageResourse)
                .showImageForEmptyUri(imageResourse)
                .showImageOnFail(imageResourse).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
                .build();
        return options;
    }
    public static DisplayLargerImageOptions getNoImageUILoptions() {
        DisplayLargerImageOptions options = new DisplayLargerImageOptions.Builder()
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.NONE)
                .cacheOnDisk(true)
                .considerExifParams(true).bitmapConfig(Bitmap.Config.ARGB_8888)// 设置图片的解码类型
                .build();
        return options;
    }

    /**
     *
     * @param imageResourse 图片资源id
     * @param cornerRadiusPixels 圆角 eg:90,就是圆形图片
     * @return DisplayLargerImageOptions
     */
    private static DisplayLargerImageOptions getBaseUILoptions(int imageResourse,int cornerRadiusPixels) {
        DisplayLargerImageOptions options = new DisplayLargerImageOptions.Builder()
                .showImageOnLoading(imageResourse)
                .showImageForEmptyUri(imageResourse)
                .showImageOnFail(imageResourse).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
                .displayer(new RoundedBitmapDisplayer(cornerRadiusPixels))
                .build();
        return options;
    }

    private static DisplayLargerImageOptions getNoChcheLoptions(
            int imageResourse) {
        DisplayLargerImageOptions options = new DisplayLargerImageOptions.Builder()
                .showImageOnLoading(imageResourse)
                .showImageForEmptyUri(imageResourse)
                .showImageOnFail(imageResourse).cacheInMemory(false)
                .imageScaleType(ImageScaleType.EXACTLY).cacheOnDisk(true)
                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
                .build();
        return options;
    }

//    private static DisplayLargerImageOptions getBaseUILoptions() {
//        return new DisplayLargerImageOptions.Builder()
//                .cacheInMemory(true)
//                .imageScaleType(ImageScaleType.EXACTLY).cacheOnDisk(true)
//                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
//                .build();
//    }

    public static DisplayLargerImageOptions getNoLoadLoptions() {
        DisplayLargerImageOptions options = new DisplayLargerImageOptions.Builder()
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY).cacheOnDisk(true)
                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
                .build();
        return options;
    }

    public static DisplayLargerImageOptions getTopicListUILoptions() {
        return getBaseUILoptions(R.drawable.icon_topiclist_default);
    }

    public static DisplayLargerImageOptions getDeatailLoptions() {
        return getBaseUILoptions(R.drawable.icon_detail_default);
    }

    public static DisplayLargerImageOptions getUILoptions() {
        return getBaseUILoptions(R.drawable.default_icon);
    }

    public static DisplayLargerImageOptions getBroadcastLoptions() {
        return getBaseUILoptions(R.drawable.broadcast_default_icon);
    }

    public static DisplayLargerImageOptions getHottestImgLoptions() {
        return getBaseUILoptions(R.drawable.hottest_img_default);
    }

    public static DisplayLargerImageOptions getHottestAppLoptions() {
        return getBaseUILoptions(R.drawable.hottest_app_default);
    }

    public static DisplayLargerImageOptions getUINewAppHeader() {
        return getBaseUILoptions(R.drawable.bg_app_game_header);
    }

    public static DisplayLargerImageOptions getADUILoptions() {
        return getNoChcheLoptions(R.drawable.banner_default);
    }

    public static DisplayLargerImageOptions getADCacheUILoptions() {
        return getBaseUILoptions(R.drawable.banner_default);
    }

    public static DisplayLargerImageOptions getHomeADCacheUILoptions() {
        return getBaseUILoptions(R.drawable.home_banner_default, (int) com.prize.app.util.DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP,2.0f));
    }

    public static DisplayLargerImageOptions getADHalfCacheUILoptions() {
        return getBaseUILoptions(R.drawable.half_banner_default);
    }

//    public static DisplayLargerImageOptions getLargerUILoptions() {
//        return getBaseUILoptions(R.drawable.default_larger);
//    }

    public static DisplayLargerImageOptions getLoadingLoptions() {
        return getBaseUILoptions(R.drawable.front_cover_wel);
    }

    public static DisplayLargerImageOptions getFullScreenUILoptions() {
        return new DisplayLargerImageOptions.Builder()
                .showImageOnLoading(R.drawable.bg_ad)
                .showImageForEmptyUri(R.drawable.bg_ad)
                .showImageOnFail(R.drawable.bg_ad).cacheInMemory(false)
                .imageScaleType(ImageScaleType.EXACTLY).cacheOnDisk(true)
                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
                .build();
    }

    public static DisplayLargerImageOptions getUILoptions(int drawable) {
        return getBaseUILoptions(drawable);
    }
    public static DisplayLargerImageOptions getUILoptions(int drawable,float cornerRadiusDp) {
        return getBaseUILoptions(drawable,(int) com.prize.app.util.DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP,cornerRadiusDp));
    }

    public static DisplayLargerImageOptions getUINoChcheLoptions(int drawable) {
        return getNoChcheLoptions(drawable);
    }

//    public static DisplayLargerImageOptions getUINoLoadingoptions() {
//        return getBaseUILoptions();
////    }

    public static DisplayLargerImageOptions getADNoLoadingoptions() {
        return new DisplayLargerImageOptions.Builder()
                .cacheInMemory(false)
                .imageScaleType(ImageScaleType.NONE).cacheOnDisk(true)
                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
                .build();
    }

    public static DisplayLargerImageOptions getColorDrawableOptions(Drawable imageResourse) {
        return new DisplayLargerImageOptions.Builder()
                .showImageOnLoading(imageResourse)
                .showImageForEmptyUri(imageResourse)
                .showImageOnFail(imageResourse).cacheInMemory(false)
                .imageScaleType(ImageScaleType.EXACTLY).cacheOnDisk(true)
                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
                .build();

    }
}
