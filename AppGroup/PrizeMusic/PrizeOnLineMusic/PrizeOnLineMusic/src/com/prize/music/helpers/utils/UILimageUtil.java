package com.prize.music.helpers.utils;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayLargerImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.prize.music.R;

/**
 * 
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
				.imageScaleType(ImageScaleType.EXACTLY).cacheOnDisk(true)
				.considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
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

	public static DisplayLargerImageOptions getADCacheUILoptions() {
		return getBaseUILoptions(R.drawable.banner_default);
	}

	public static DisplayLargerImageOptions getPersonHeadImg() {
		return getBaseUILoptions(R.drawable.local_head_icon);
	}

	public static DisplayLargerImageOptions getDetaiHeadImgUILoptions() {
		return getBaseUILoptions(R.drawable.icon_detail_head_img);
	}

	public static DisplayLargerImageOptions getTwoOneZeroDpLoptions() {
		return getBaseUILoptions(R.drawable.bg_two_one_zero_dp);
	}

	public static DisplayLargerImageOptions getUILoptions() {
		return getBaseUILoptions(R.drawable.banner_default);
	}

}
