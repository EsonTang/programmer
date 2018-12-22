package com.android.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.lqsoft.LqServiceUpdater.LqService;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayLargerImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

/**
 * 
 * IML工具加载图片选项
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class UILimageUtil {

	private static DisplayImageOptions getBaseUILoptions(Drawable imageResourse) {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showImageOnLoading(imageResourse)
				.showImageForEmptyUri(imageResourse)
				.showImageOnFail(imageResourse).cacheInMemory(true)
				.imageScaleType(ImageScaleType.EXACTLY).cacheOnDisk(true)
				.considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型
				.build();
		return options;
	}
	
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

	
	public static Bitmap  getDefaultBitmap(Context c) {

		Drawable mMask = c.getDrawable(R.drawable.wall_local_press);
		Bitmap mMaskBitmap = ImageUtils.drawableToBitmap1(mMask);
			mMaskBitmap = IconCache.getLqIcon(null,
					mMaskBitmap, true, "");
			if (mMaskBitmap.getHeight() != Utilities.sIconTextureHeight) {

				mMaskBitmap = ImageUtils
						.resizeIcon(
								mMaskBitmap,
								Utilities.sIconTextureHeight,
								Utilities.sIconTextureWidth);
			}
			return mMaskBitmap;
	}
	public static Drawable  getDefaultIcon(Context c) {

		Drawable mMask = c.getDrawable(R.drawable.wall_local_press);
		Bitmap mMaskBitmap = ImageUtils.drawableToBitmap1(mMask);
			mMaskBitmap = IconCache.getLqIcon(null,
					mMaskBitmap, true, "");
			return ImageUtils.bitmapToDrawable(mMaskBitmap);
	}

	public static DisplayImageOptions getUILoptions(Context c) {
		
		return getBaseUILoptions(getDefaultIcon(c));
	}

}
