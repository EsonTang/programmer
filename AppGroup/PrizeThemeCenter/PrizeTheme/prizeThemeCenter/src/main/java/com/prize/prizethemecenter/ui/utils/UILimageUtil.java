package com.prize.prizethemecenter.ui.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayLargerImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.prize.prizethemecenter.R;

/**
 * IML工具加载图片选项
 * @version V1.0
 */
public class UILimageUtil {

	private static DisplayLargerImageOptions getBaseUILoptions(int imageResourse) {
		DisplayLargerImageOptions options = new DisplayLargerImageOptions.Builder()
				.showImageOnLoading(imageResourse)
				.showImageForEmptyUri(imageResourse)
				.showImageOnFail(imageResourse).cacheInMemory(true)
				.imageScaleType(ImageScaleType.EXACTLY).cacheOnDisk(true)
				.considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
//				.cacheInMemory(true)
//				.cacheOnDisc(true)
				.delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
				.build();
		return options;
	}

	private static DisplayLargerImageOptions getWallUILoptions(int imageResourse) {
		DisplayLargerImageOptions options = new DisplayLargerImageOptions.Builder()
				.showImageOnLoading(imageResourse)
				.showImageForEmptyUri(imageResourse)
				.showImageOnFail(imageResourse).cacheInMemory(true)
				.imageScaleType(ImageScaleType.NONE).cacheOnDisk(true)
				.considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
//				.cacheInMemory(true)
//				.cacheOnDisc(true)
				.delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
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

	public static DisplayLargerImageOptions getHomeADCacheUILoptions() {
		return getBaseUILoptions(R.drawable.home_banner_default);
	}
	
	public static DisplayLargerImageOptions getHomeThemeDpLoptions() {
		return getBaseUILoptions(R.drawable.bg_home_theme_item);
	}

	public static DisplayLargerImageOptions getSingleThemeDpLoptions() {
		return getBaseUILoptions(R.drawable.bg_single_theme_item);
	}

	public static DisplayLargerImageOptions getWallpaperFontLoptions() {
		return getBaseUILoptions(R.drawable.bg_head_logo);
	}
	
	public static DisplayLargerImageOptions getFontDefaultLoptions() {
		return getBaseUILoptions(R.drawable.bg_font_default);
	}

	public static DisplayLargerImageOptions getLoadingLoptions() {
		return getBaseUILoptions(R.drawable.bg_initlogo);
	}

	public static DisplayLargerImageOptions getClassifyLoptions() {
		return getBaseUILoptions(R.drawable.classify);
	}

	public static DisplayLargerImageOptions getTopicLoptions() {
		return getBaseUILoptions(R.drawable.topic_list_bg);
	}

	public static DisplayLargerImageOptions getTopicDetailLoptions() {
		return getBaseUILoptions(R.drawable.topic_detail_logo);
	}
	public static DisplayLargerImageOptions getThemeCommentDefaultLoptions(){
		return getBaseUILoptions(R.drawable.comment_default_icon);
	}
//	public static DisplayLargerImageOptions getADUILoptions() {
//		return getNoChcheLoptions(R.drawable.banner_default);
//	}

	public static DisplayLargerImageOptions getFullScreenUILoptions() {
		return getBaseUILoptions(R.drawable.bg_full_screen);
	}

	public static DisplayLargerImageOptions getFullScreenWallUILoptions() {
		return getWallUILoptions(R.drawable.bg_full_screen);
	}
	public static DisplayLargerImageOptions getPersonHeadImg() {
		return getBaseUILoptions(R.drawable.cloud_man_small);
	}
	public static DisplayLargerImageOptions getMineImgOption(){
		return getBaseUILoptions(R.drawable.bg_home_theme_item);
	}
	public static String getPicPath(Activity context,String wallpaper_pic){
		//请求不同图片尺寸
		int w = (int)context.getResources().getDimension(R.dimen.imageView_item_weight);
		int h = (int)context.getResources().getDimension(R.dimen.imageView_item_hight);
		StringBuffer b = new StringBuffer(wallpaper_pic);
		//?x-oss-process=image/resize,m_fill,h_高,w_宽
		String p = b.append("?x-oss-process=image/resize,m_fill,h_").append(h).append(",w_").append(w).toString();
//        JLog.i("hu","width="+w+"--height="+h+"--p="+p);
		return p;
	}
	public static String spitPicPath(Activity context,String path){
		String[] split = path.split("\\?");
		return split[0];
	}

	public static ImageLoadingListener setTagHolder(final ImageView imageView, final String imageUrl){
		ImageLoadingListener loadingListener=new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {

			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				imageView.setTag(imageUrl);
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {

			}
		};
				return loadingListener;
	}

	public static String formatDirPic(String contentDesc){
		String contentOne = null;
		if (!TextUtils.isEmpty(contentDesc)) {
			 contentOne = contentDesc.replaceAll("\\\\", "\\");

		}
		return contentOne;
	}

}
