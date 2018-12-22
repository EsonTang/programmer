package com.prize.prizenavigation.utils;

import android.net.Uri;
import android.text.TextUtils;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;


/**
 * IML工具加载图片选项
 *
 * @version V1.0
 */
public class UILimageUtil {

//    private static DisplayImageOptions getBaseUILoptions(int imageResourse) {
//        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(imageResourse)
//                .showImageForEmptyUri(imageResourse)
//                .showImageOnFail(imageResourse).cacheInMemory(true)
//                .imageScaleType(ImageScaleType.EXACTLY).cacheOnDisk(true)
//                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
//				.cacheInMemory(true)
//				.cacheOnDisc(true)
//                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
//                .build();
//        return options;
//    }
//
//    private static DisplayImageOptions getWallUILoptions(int imageResourse) {
//        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(imageResourse)
//                .showImageForEmptyUri(imageResourse)
//                .showImageOnFail(imageResourse).cacheInMemory(true)
//                .imageScaleType(ImageScaleType.NONE).cacheOnDisk(true)
//                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
////				.cacheInMemory(true)
////				.cacheOnDisc(true)
//                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
//                .build();
//        return options;
//    }
//
//    private static DisplayImageOptions getNoChcheLoptions(
//            int imageResourse) {
//        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(imageResourse)
//                .showImageForEmptyUri(imageResourse)
//                .showImageOnFail(imageResourse).cacheInMemory(false)
//                .imageScaleType(ImageScaleType.EXACTLY).cacheOnDisk(true)
//                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
//                .build();
//        return options;
//    }
//
////    public static DisplayImageOptions getNaviFragmentLargeCacheUILoptions() {
////        return getBaseUILoptions(R.drawable.icon_hongbao);
////    }
////
////    public static DisplayImageOptions getNaviFragmentSmallCacheUILoptions() {
////        return getBaseUILoptions(R.drawable.icon_hongbao_title);
////    }
//
//    public static void displayLargeImg(String tag, ImageView imageView, NaviDatas.ListBean fraDatas) {
//        if (fraDatas.getLarge_icon_url() != null && (tag == null || !tag.equals(fraDatas.getLarge_icon_url()))) {
//            ImageLoader.getInstance().displayImage(fraDatas.getLarge_icon_url(), imageView,
//                    UILimageUtil.setTagHolder(imageView, fraDatas.getLarge_icon_url()));
//        }
//    }
//
//    public static void displaySmallImg(String tag, ImageView imageView, NaviDatas.ListBean fraDatas) {
//        if (fraDatas.getSmall_icon_url() != null && (tag == null || !tag.equals(fraDatas.getSmall_icon_url()))) {
//            ImageLoader.getInstance().displayImage(fraDatas.getSmall_icon_url(), imageView,
//                    UILimageUtil.setTagHolder(imageView, fraDatas.getSmall_icon_url()));
//        }
//    }
//
//    //	public static String getPicPath(Activity context, String wallpaper_pic){
////		//请求不同图片尺寸
////		int w = (int)context.getResources().getDimension(R.dimen.imageView_item_weight);
////		int h = (int)context.getResources().getDimension(R.dimen.imageView_item_hight);
////		StringBuffer b = new StringBuffer(wallpaper_pic);
////		//?x-oss-process=image/resize,m_fill,h_高,w_宽
////		String p = b.append("?x-oss-process=image/resize,m_fill,h_").append(h).append(",w_").append(w).toString();
//////        JLog.i("hu","width="+w+"--height="+h+"--p="+p);
////		return p;
////	}
//    public static String spitPicPath(Activity context, String path) {
//        String[] split = path.split("\\?");
//        return split[0];
//    }
//
//    public static ImageLoadingListener setTagHolder(final ImageView imageView, final String imageUrl) {
//        ImageLoadingListener loadingListener = new ImageLoadingListener() {
//            @Override
//            public void onLoadingStarted(String imageUri, View view) {
//
//            }
//
//            @Override
//            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//
//            }
//
//            @Override
//            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                imageView.setTag(imageUrl);
//            }
//
//            @Override
//            public void onLoadingCancelled(String imageUri, View view) {
//
//            }
//        };
//        return loadingListener;
//    }

    public static String formatDirPic(String contentDesc) {
        String contentOne = null;
        if (!TextUtils.isEmpty(contentDesc)) {
            contentOne = contentDesc.replaceAll("\\\\", "\\");

        }
        return contentOne;
    }

    /*Glide 加载gif内存泄漏-------------------*/
//    public static void displayImg(Context context,String url,ImageView img){
//        Glide.with(context).load(url).centerCrop().error(R.drawable.icon_net_error).crossFade().into(img);
//    }
    /*Fresco-------------------*/
    public static void displayImg(String url,SimpleDraweeView mSimpleDraweeView){
        Uri uri = Uri.parse(url);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)


        .build();
        mSimpleDraweeView.setController(controller);
    }

}
