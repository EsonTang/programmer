package com.prize.appcenter.ui.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class BitmapUtils {
	public static Bitmap scaleBitmap(Bitmap bmp, int w, int h) {
		float scaleFactor = 0.5f;// 图片缩放比例
		Bitmap overlay = Bitmap.createBitmap((int) (w), (int) (h),
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(overlay);
		canvas.save();
		canvas.scale(0.5f, 0.5f);
		Paint paint = new Paint();
		paint.setFlags(Paint.FILTER_BITMAP_FLAG);
		canvas.drawBitmap(bmp, 0, 0, paint);
		/*
		 * if(bmp!=null&&!bmp.isRecycled()) { bmp.recycle(); bmp=null; }
		 */
		// overlay = doBlur(overlay, (int) radius, true);
		canvas.restore();
		return overlay;
	}

	/**
	 * 实现图片组合
	 * 
	 * @param bg
	 *            :背景
	 * @return
	 */
	public static Bitmap doodleicons(Bitmap bg, Bitmap[] drags, int pading) {
		Bitmap newb = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(newb);
		canvas.drawBitmap(bg, 0, 0, null);
		int w = bg.getWidth();
		int h = bg.getHeight();
		int pw = (w) / 2;
		int ph = (h) / 2;

		for (int i = 0; i < 4; i++) {

			Bitmap child = scaleBitmap(drags[i], pw, ph);
			int width = child.getWidth();
			int height = child.getHeight();
			int left1 = width * 2 + pading * (2 - 1);
			int top1 = height * 2 + pading * (2 - 1);
			int left = (int) (w / 2f - left1 / 2f) + i % 2 * (width + pading);
			int top = (int) (h / 2f - top1 / 2f) + i / 2 * (height + pading);
			int right = left + width;
			int bottom = top + height;
			Rect r = new Rect(left, top, right, bottom);
			canvas.drawBitmap(child, null, r, null);
		}
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();

		return newb;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap.createBitmap(

		drawable.getIntrinsicWidth(),

		drawable.getIntrinsicHeight(),

		drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

		: Bitmap.Config.RGB_565);

		Canvas canvas = new Canvas(bitmap);

		// canvas.setBitmap(bitmap);

		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());

		drawable.draw(canvas);

		return bitmap;

	}
}
