
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：图片处理工具类
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/
package com.android.launcher3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.View;
import android.view.WindowManager;

/**
 * 主要为针对图标的处理
 * @author Administrator
 *
 */
public class ImageUtils {
	public static Drawable icon_bg;
	public static Drawable folder_bg;
	public static Drawable mask_icon;// add by zhouerlong

	// 截取View的Bitmap 位图
	public static Bitmap convertViewToBitmap(View view, int bitmapWidth,
			int bitmapHeight) {
		Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
				Bitmap.Config.ARGB_8888);
		view.draw(new Canvas(bitmap));

		return bitmap;
	}
	static public Bitmap reCreateBitmap(Bitmap src) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						src.getWidth(),
						src.getHeight(),Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		 canvas.drawBitmap(bitmap, 0, 0, null);
		return bitmap;

	}
	


	public static Bitmap screenshot(Launcher c){ // View是你需要截图的View   
        View view = c.getWindow().getDecorView();   
        view.setDrawingCacheEnabled(true);   
        view.buildDrawingCache();   
        Bitmap b1 = view.getDrawingCache();   
        // 获取状态栏高度   
        Rect frame = new Rect();   
        c.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);   
        int statusBarHeight = frame.top;   
        System.out.println(statusBarHeight);   
        // 获取屏幕长和高   
        int width = c.getWindowManager().getDefaultDisplay().getWidth();   
        int height = c.getWindowManager().getDefaultDisplay()   
                .getHeight();   
        // 去掉标题栏 //Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455);   
        Bitmap b = Bitmap.createBitmap(b1, 0, 0, width, height   
                - statusBarHeight);


		android.app.WallpaperManager wallpaperManager = android.app.WallpaperManager
				.getInstance(c);
		Bitmap  wall = Bitmap.createBitmap(wallpaperManager.getBitmap());
        view.destroyDrawingCache();   
       
        return   doodletest(b,wall);  }
	
	public static Bitmap blurScale(Bitmap bmp,float scaleFactor){
//		String  v=FileUtils.loadFileToString(FileUtils.getRawFile("hello.txt",mContext));
		
//		float scaleFactor = 20f;//Float.valueOf(v);
        float radius = 10;
        Bitmap overlay = Bitmap.createBitmap(
                (int) (bmp.getWidth() / scaleFactor),
                (int) (bmp.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bmp, 0, 0, paint);
        canvas.drawColor(0x88000000);
        Log.d("xxf", "blurScale");
        overlay = doBlur(overlay, (int) radius, true);
        if(bmp!=null&&!bmp.isRecycled()) {
        	bmp.recycle();
        	bmp=null;
        }
        canvas.release();
        return overlay;
    }
	
	/**图片模糊处理方法
     * @param sentBitmap 原图
     * @param radius
     * @param canReuseInBitmap
     * @return
     */
    public static Bitmap doBlur(Bitmap sentBitmap, int radius,  
            boolean canReuseInBitmap) {  
        Bitmap bitmap;  
        if (canReuseInBitmap) {  
            bitmap = sentBitmap;  
        } else {  
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);  
        }  
  
        if (radius < 1) {  
            return (null);  
        }  
  
        int w = bitmap.getWidth();  
        int h = bitmap.getHeight();  
  
        int[] pix = new int[w * h];  
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);  
  
        int wm = w - 1;  
        int hm = h - 1;  
        int wh = w * h;  
        int div = radius + radius + 1;  
  
        int r[] = new int[wh];  
        int g[] = new int[wh];  
        int b[] = new int[wh];  
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;  
        int vmin[] = new int[Math.max(w, h)];  
  
        int divsum = (div + 1) >> 1;  
        divsum *= divsum;  
        int dv[] = new int[256 * divsum];  
        for (i = 0; i < 256 * divsum; i++) {  
            dv[i] = (i / divsum);  
        }  
  
        yw = yi = 0;  
  
        int[][] stack = new int[div][3];  
        int stackpointer;  
        int stackstart;  
        int[] sir;  
        int rbs;  
        int r1 = radius + 1;  
        int routsum, goutsum, boutsum;  
        int rinsum, ginsum, binsum;  
  
        for (y = 0; y < h; y++) {  
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;  
            for (i = -radius; i <= radius; i++) {  
                p = pix[yi + Math.min(wm, Math.max(i, 0))];  
                sir = stack[i + radius];  
                sir[0] = (p & 0xff0000) >> 16;  
                sir[1] = (p & 0x00ff00) >> 8;  
                sir[2] = (p & 0x0000ff);  
                rbs = r1 - Math.abs(i);  
                rsum += sir[0] * rbs;  
                gsum += sir[1] * rbs;  
                bsum += sir[2] * rbs;  
                if (i > 0) {  
                    rinsum += sir[0];  
                    ginsum += sir[1];  
                    binsum += sir[2];  
                } else {  
                    routsum += sir[0];  
                    goutsum += sir[1];  
                    boutsum += sir[2];  
                }  
            }  
            stackpointer = radius;  
  
            for (x = 0; x < w; x++) {  
  
                r[yi] = dv[rsum];  
                g[yi] = dv[gsum];  
                b[yi] = dv[bsum];  
  
                rsum -= routsum;  
                gsum -= goutsum;  
                bsum -= boutsum;  
  
                stackstart = stackpointer - radius + div;  
                sir = stack[stackstart % div];  
  
                routsum -= sir[0];  
                goutsum -= sir[1];  
                boutsum -= sir[2];  
  
                if (y == 0) {  
                    vmin[x] = Math.min(x + radius + 1, wm);  
                }  
                p = pix[yw + vmin[x]];  
  
                sir[0] = (p & 0xff0000) >> 16;  
                sir[1] = (p & 0x00ff00) >> 8;  
                sir[2] = (p & 0x0000ff);  
  
                rinsum += sir[0];  
                ginsum += sir[1];  
                binsum += sir[2];  
  
                rsum += rinsum;  
                gsum += ginsum;  
                bsum += binsum;  
  
                stackpointer = (stackpointer + 1) % div;  
                sir = stack[(stackpointer) % div];  
  
                routsum += sir[0];  
                goutsum += sir[1];  
                boutsum += sir[2];  
  
                rinsum -= sir[0];  
                ginsum -= sir[1];  
                binsum -= sir[2];  
  
                yi++;  
            }  
            yw += w;  
        }  
        for (x = 0; x < w; x++) {  
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;  
            yp = -radius * w;  
            for (i = -radius; i <= radius; i++) {  
                yi = Math.max(0, yp) + x;  
  
                sir = stack[i + radius];  
  
                sir[0] = r[yi];  
                sir[1] = g[yi];  
                sir[2] = b[yi];  
  
                rbs = r1 - Math.abs(i);  
  
                rsum += r[yi] * rbs;  
                gsum += g[yi] * rbs;  
                bsum += b[yi] * rbs;  
  
                if (i > 0) {  
                    rinsum += sir[0];  
                    ginsum += sir[1];  
                    binsum += sir[2];  
                } else {  
                    routsum += sir[0];  
                    goutsum += sir[1];  
                    boutsum += sir[2];  
                }  
  
                if (i < hm) {  
                    yp += w;  
                }  
            }  
            yi = x;  
            stackpointer = radius;  
            for (y = 0; y < h; y++) {  
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )  
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)  
                        | (dv[gsum] << 8) | dv[bsum];  
  
                rsum -= routsum;  
                gsum -= goutsum;  
                bsum -= boutsum;  
  
                stackstart = stackpointer - radius + div;  
                sir = stack[stackstart % div];  
  
                routsum -= sir[0];  
                goutsum -= sir[1];  
                boutsum -= sir[2];  
  
                if (x == 0) {  
                    vmin[y] = Math.min(y + r1, hm) * w;  
                }  
                p = x + vmin[y];  
  
                sir[0] = r[p];  
                sir[1] = g[p];  
                sir[2] = b[p];  
  
                rinsum += sir[0];  
                ginsum += sir[1];  
                binsum += sir[2];  
  
                rsum += rinsum;  
                gsum += ginsum;  
                bsum += binsum;  
  
                stackpointer = (stackpointer + 1) % div;  
                sir = stack[stackpointer];  
  
                routsum += sir[0];  
                goutsum += sir[1];  
                boutsum += sir[2];  
  
                rinsum -= sir[0];  
                ginsum -= sir[1];  
                binsum -= sir[2];  
  
                yi += w;  
            }  
        }  
  
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);  
  
        return (bitmap);  
    }

	// PRIZE-launcher3-zhouerlong-2015-8-3-start
	/**
	 * 将icon和mask图标重叠 如果没有达到mask重叠指标将用icon_bg和icon重叠
	 * 
	 * @param orignal
	 * @param pixels
	 * @param mask
	 * @param c
	 * @param bg
	 * @return
	 */
	public static Bitmap toMaskBitmap(Bitmap orignal, int pixels,
			Drawable mask, Context c, Bitmap bg) {
		Bitmap maskbmp = Utilities.createIconBitmap(mask, c);
		int orignalW = orignal.getWidth();
		int orignalH = orignal.getHeight();
		if (neddResizeIcon(orignal, pixels)) {
			orignal = cropCenter(orignal, orignal.getWidth() - pixels,
					orignal.getHeight() - pixels);
			orignal = resize(orignal, orignalW, orignalH);
			Bitmap maskBt =createMaskImage(orignal, maskbmp);
			return maskBt;
			
		}
		return doodle(orignal, bg);
	}

	// PRIZE-launcher3-zhouerlong-2015-8-3-end
	/**
	 * 创建一些launcher必须用到的辅助图标
	 * @param context
	 */
	public static void createHelpericon(Context context) {
		icon_bg = context.getDrawable(R.drawable.bg_icon);
		folder_bg = context.getDrawable(R.drawable.folder_bg); 
		mask_icon = context.getDrawable(R.drawable.mask);// PRIZE-launcher3-zhouerlong-2015-8-3-start
	}

	// A by zel
	// public static final int resize = 14;

	public static int[] getBitmapCantPixel(Bitmap bit, int resize) {
		int width = bit.getWidth();
		int height = bit.getHeight();
		int pixels[] = new int[4];
		int offsetX = resize;
		int offsetY = resize;

		pixels[0] = bit.getPixel(offsetX, offsetY); // left & top
		pixels[1] = bit.getPixel(width - offsetX, offsetY); // right& top
		pixels[2] = bit.getPixel(offsetX, height - offsetY); // left& bottom
		pixels[3] = bit.getPixel(width - offsetX, height - offsetY); // right
																		// &bottom
		// pixels[4] = bit.getPixel(offsetX, height/2);//center left

		return pixels;
	}

	public static Bitmap dividePart(Bitmap bmp, Rect src) {
		int width = src.width();
		int height = src.height();
		Rect des = new Rect(0, 0, width, height);
		Bitmap croppedImage = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(croppedImage);
		canvas.drawBitmap(bmp, src, des, null);
		return croppedImage;
	}

	public static Bitmap cropCenter(Bitmap bm, int newWidth, int newHeight) {
		int dstWidth = newWidth;
		int dstHeight = newHeight;
		int startWidth = (bm.getWidth() - dstWidth) / 2;
		int startHeight = ((bm.getHeight() - dstHeight) / 2);
		Rect src = new Rect(startWidth, startHeight, startWidth + dstWidth,
				startHeight + dstHeight);

		return dividePart(bm, src);
	}

	// A by zel

	static int[] getBitmapCantPixel(Drawable icon, int resize) {
		Bitmap bit = drawableToBitmap(icon);
		int width = bit.getWidth();
		int height = bit.getHeight();
		int pixels[] = new int[5];
		int offsetX = width / resize;
		int offsetY = height / resize;

		pixels[0] = bit.getPixel(offsetX, offsetY);
		pixels[1] = bit.getPixel(width - offsetX, offsetY);
		pixels[2] = bit.getPixel(offsetX, height - offsetY);
		pixels[3] = bit.getPixel(width - offsetX, height - offsetY);
		pixels[4] = bit.getPixel(offsetX, height / 2);

		return pixels;
	}

	final static int TRANSPARENT = 0x00;

	static boolean neddResizeIcon(Bitmap bit, int resize) {
		int pixels[] = getBitmapCantPixel(bit, resize);

		boolean need = (pixels[0] == TRANSPARENT && pixels[1] == TRANSPARENT
				&& pixels[2] == TRANSPARENT && pixels[3] == TRANSPARENT) ? false
				: true;

		return need;
	}

	public static Bitmap resizeIcon(Drawable icon, int w, int h, int resize) {
		if (neddResizeIcon(icon, resize)) {
			return resize(icon, w, h);
		} else {
			return null;
		}
	}
	

	// 是否需要重新设计大小
	static boolean neddResizeIcon(Drawable icon, int resize) {
		int pixels[] = getBitmapCantPixel(icon, resize);

		boolean need = (pixels[0] != TRANSPARENT && pixels[1] != TRANSPARENT
				&& pixels[2] != TRANSPARENT && pixels[3] != TRANSPARENT) ? true
				: false;

		return need;
	}

	static public Bitmap drawableToBitmap1(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.save();
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		canvas.restore();
		if (drawable!=null&&drawable instanceof BitmapDrawable) {
			BitmapDrawable b = (BitmapDrawable) drawable;
			 if (b != null&&b.getBitmap()!=bitmap) {
		            b.setCallback(null);
		            b = null;
		            System.gc();
			 }}
		
		return bitmap;

	}

	
	 /**
	    * 方法描述：缩小图片模糊法，速度快，性能好，当前采用的算法
	    */

 	public static float SCALE_VAL = 3.0f;
	    public static Bitmap scaleBitmap(Bitmap bmp){
	        float scaleFactor = SCALE_VAL;//图片缩放比例
	        float radius = 15;//模糊程度 
	        Bitmap overlay = Bitmap.createBitmap(
	                (int) (bmp.getWidth() / scaleFactor),
	                (int) (bmp.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
	        Canvas canvas = new Canvas(overlay);
	        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
	        Paint paint = new Paint();
	        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
	        canvas.drawBitmap(bmp, 0, 0, paint);
	       /* if(bmp!=null&&!bmp.isRecycled()) {
	        	bmp.recycle();
	        	bmp=null;
	        }*/
//	        overlay = doBlur(overlay, (int) radius, true);
	        return overlay;
	    }
	    
	    public static Bitmap scaleBitmap(Bitmap bmp,int w,int h){
	        float scaleFactor = SCALE_VAL;//图片缩放比例
	        Bitmap overlay = Bitmap.createBitmap(
	                (int) (w),
	                (int) (h), Bitmap.Config.ARGB_8888);
	        Canvas canvas = new Canvas(overlay);
	        canvas.scale(1 * scaleFactor, 1 * scaleFactor);
	        Paint paint = new Paint();
	        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
	        canvas.drawBitmap(bmp, 0, 0, paint);
	        /*if(bmp!=null&&!bmp.isRecycled()) {
	        	bmp.recycle();
	        	bmp=null;
	        }*/
//	        overlay = doBlur(overlay, (int) radius, true);
	        canvas.release();
	        return overlay;
	    }

	
	static public Bitmap drawableToBitmap(Drawable drawable) {

		return ((BitmapDrawable) drawable).getBitmap();

	}

	public static void saveBefore(String path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false;
		int be = (int) (options.outHeight / (float) 200);
		if (be <= 0)
			be = 1;
		options.inSampleSize = 2;
		bitmap = BitmapFactory.decodeFile(path, options);
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		System.out.println(w + " " + h);
		// savePNG_After(bitmap,path);
		saveJPGE_After(bitmap, path);
	}

	public static Bitmap resize(Drawable icon, int w, int h) {
		Bitmap bmp = drawableToBitmap(icon);

		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int newWidth = w;
		int newHeight = h;

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width,
				height, matrix, true);

       /* if(bmp!=null&&!bmp.isRecycled()) {
        	bmp.recycle();
        	bmp=null;
        }*/
		return resizedBitmap;
	}

	public static Bitmap resize(Bitmap bm, int w, int h) {
		Bitmap BitmapOrg = bm;
		
		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);
		Bitmap bg = Bitmap.createBitmap(w, h, Config.ARGB_8888);// modify by
																// zhouerlong
		if (resizedBitmap != null) {
			return ImageUtils.doodle(resizedBitmap, bg);
		}
		if(bm!=null&&resizedBitmap!= bm)
		{
			bm.recycle();
		}
		return resizedBitmap;
	}
	
	public static Bitmap resizeIcon(Bitmap bm, int w, int h) {
		Bitmap BitmapOrg = bm;
		Bitmap resizedBitmap=null;
		try {
			
		if(bm !=null&&bm.getWidth()==Utilities.sIconTextureWidth&&bm.getHeight()==Utilities.sIconTextureWidth) {
			return bm;
		}
		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;
		if(width<=0&&height<=0) {
			return null;
		}

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		 resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);
		if(BitmapOrg!=null&&BitmapOrg!=resizedBitmap) {
			BitmapOrg.recycle();
		}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resizedBitmap;
	}

	public static void savePNG_After(Drawable icon, String name) {
		name += ".png";
		Bitmap bitmap = drawableToBitmap(icon);
		File file = new File(name);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void savePNG_After(Bitmap bitmap, String name) {
		// add by zhouerlong 0728 start
		File dir = new File(Environment.getExternalStorageDirectory(), "desk");
		if (!dir.exists()) {
			dir.mkdir();
		}
		File file = new File(Environment.getExternalStorageDirectory(), name);
		// add by zhouerlong 0728 end
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveJPGE_After(Bitmap bitmap, String path) {
		File file = new File(path);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void savePNG1_After(Bitmap bitmap, String path) {
		File file = new File(path);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// M by zhouerlong
	public static Bitmap doodle(Bitmap src, Bitmap bg) {
		Bitmap newb = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(newb);
		canvas.drawBitmap(bg, 0, 0, null);

		canvas.drawBitmap(src, (bg.getWidth() - src.getWidth()) / 2,
				(bg.getHeight() - src.getHeight()) / 2, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		if(src!=null&&src!=newb) {
			src.recycle();
			src=null;
			
		}
		if(bg!=null) {
			bg.recycle();
		}

		// src.recycle();
		// src = null;

		return newb;
	}
	
	
	public static Bitmap doodletest(Bitmap src, Bitmap bg) {
		Bitmap newb = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(newb);
		canvas.drawBitmap(bg, 0, 0, null);

		canvas.drawBitmap(src, (bg.getWidth() - src.getWidth()) / 2,
				(bg.getHeight() - src.getHeight()) / 2, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		/*if(src!=null&&src!=newb) {
			src.recycle();
			src=null;
			
		}
		if(bg!=null) {
			bg.recycle();
		}*/

		// src.recycle();
		// src = null;

		return newb;
	}
	
	
	public static Bitmap reDoodle(Bitmap src) {
		if(true) {
			return src;
		}
		Bitmap newb = Bitmap.createBitmap(Utilities.sIconTextureWidth, Utilities.sIconTextureWidth,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(newb);

		canvas.drawBitmap(src, (Utilities.sIconTextureWidth - src.getWidth()) / 2,
				(Utilities.sIconTextureWidth - src.getHeight()) / 2, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		if(src!=null&&src !=newb)
		{
			src.recycle();
		}

		return newb;
	}

	// PRIZE-launcher3-zhouerlong-2015-8-3-start

	/**
	 * 创建遮罩图片 
	 * @param source原图
	 * @param mask 遮罩图片 可以实现不同形状的图片
	 * @return
	 */
	public static Bitmap createMaskImage(Bitmap source, Bitmap mask) {
		Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),
				Config.ARGB_4444);
		// 将遮罩层的图片放到画布中
		Canvas mCanvas = new Canvas(result);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));// 叠加重复的部分，
		
		mCanvas.drawBitmap(source, 0, 0, null);

		mCanvas.drawBitmap(mask, 0,
				0, paint);
		paint.setXfermode(null);
		/*if(source!=null&& source != result) {
			source.recycle();
		}
		if(mask!=null) {
			mask.recycle();
		}*/
		return result;
	}
	
	public static Bitmap getMaskIcon(Bitmap src, Bitmap mask) {
		src=resize(src, Utilities.sIconTextureHeight, Utilities.sIconTextureHeight);
		final Bitmap result = ImageUtils.createMaskImage(src, mask);

		return result;
	}
	
	
	/**
	 * 创建遮罩图片
	 * 
	 * @param source原图
	 * @param mask
	 *            遮罩图片 可以实现不同形状的图片
	 * @return
	 */
	public static Bitmap createMaskImage(Drawable source, Bitmap mask) {
		Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),
				Config.ARGB_4444);
		// 将遮罩层的图片放到画布中
		Canvas mCanvas = new Canvas(result);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));// 叠加重复的部分，
		int w = source.getIntrinsicWidth();
		int h = source.getIntrinsicHeight();
		source.setBounds(0, 0, w, h);
		// mCanvas.drawBitmap(source, 0, 0, null);
		source.draw(mCanvas);
		mCanvas.drawBitmap(mask, (w - mask.getWidth()) / 2,
				(h - mask.getHeight()) / 2, paint);
		paint.setXfermode(null);
		return result;
	}

	// PRIZE-launcher3-zhouerlong-2015-8-3-end
	

	// add by zhouerlong start 0728
	/**
	 * 实现图片组合
	 * @param decade:十位数字
	 * @param unit:各位数字
	 * @param bg:背景
	 * @return
	 */
	public static Bitmap doodlesrc(Bitmap decade, Bitmap unit, Bitmap bg,Bitmap weekIcon,Context context,int count) {
		Bitmap newb = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(newb);
		canvas.drawBitmap(bg, 0, 0, null);
		
		int p=(int) (0*Launcher.scale);
		int childWidth=(int) (decade.getWidth());
		int width=Utilities.sIconTextureWidth;
		int left1 = childWidth * count + p * (count - 1);
		int decadeLeft = (int) (width / 2f - left1 / 2f) + 0
				* (childWidth + p);
		int weekTop =(int) (bg.getWidth() / 2.6f);
		int weekLeft = 0;
		int decadeTop =0;
		int unitLeft = (int) (width / 2f - left1 / 2f) + 1
				* (childWidth + p);
		int unittop = 0;
		Rect decadeRect = new Rect(decadeLeft, decadeTop, decadeLeft
				+ childWidth, decade.getHeight()+decadeTop);
		Rect unitRect = new Rect(decadeLeft, decadeTop, decadeLeft
				+ childWidth, decade.getHeight()+decadeTop);
		
		if(count>1) {
			 decadeRect = new Rect(decadeLeft, decadeTop, decadeLeft
					+ childWidth, decade.getHeight()+decadeTop);
			 unitRect = new Rect(unitLeft, unittop, unitLeft + childWidth,
					unit.getHeight()+unittop);
				canvas.drawBitmap(decade, null, decadeRect, null);
		}
		canvas.drawBitmap(unit, null, unitRect, null);
		if (weekIcon != null) {
		/*	Rect weekRect = new Rect(weekLeft, weekTop, weekLeft + weekIcon.getWidth(),
					weekIcon.getHeight()+weekTop);
			canvas.drawBitmap(weekIcon, null, weekRect, null);*/
			String testString  = getDayOfWeek();
			Paint mPaint = new Paint();
			mPaint.setStrokeWidth(2);  
			mPaint.setTextSize(9*Launcher.scale);
			/*if(Launcher.scale == 3){
				mPaint.setTextSize(9*Launcher.scale);
			}*/
			mPaint.setFlags(Paint.ANTI_ALIAS_FLAG); 
			mPaint.setTypeface(Launcher.mTypeface);
			mPaint.setColor(context.getResources().getColor(R.color.text_color_f93454));
			mPaint.setTextAlign(Align.LEFT);  
			Rect bounds = new Rect(); 
			mPaint.getTextBounds(testString, 0, testString.length(), bounds); 
			FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();  
			int baseline = bg.getWidth()/2 + (fontMetrics.descent- fontMetrics.ascent)/2 - fontMetrics.descent;
			canvas.drawText(testString, bg.getWidth()/2 - bounds.width()/2-Launcher.scale, bg.getHeight()/4 + bounds.height()/4, mPaint);
		}
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();

		return newb;
	}
	
	
	
	/**
	 * 实现图片组合
	 * @param decade:十位数字
	 * @param unit:各位数字
	 * @param bg:背景
	 * @return
	 */
	public static Bitmap doodleWeather(Bitmap decade, Bitmap unit,Bitmap icon, Bitmap degress) {
		Bitmap newb = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(newb);
		canvas.drawBitmap(icon, 0, 0, null);
		
		int p=(int) (0*Launcher.scale);
		int childWidth=(int) (decade.getWidth());
		int width=Utilities.sIconTextureWidth;
		int left1 = childWidth * 2 + p * (2 - 1);
		int decadeLeft = (int) (width / 2f - left1 / 2f) + 0
				* (childWidth + p);
		int weekTop =(int) (icon.getWidth() / 2.6f);
		int weekLeft = 0;
		int decadeTop =0;
		int unitLeft = (int) (width / 2f - left1 / 2f) + 1
				* (childWidth + p);
		int degressLeft = (int) (width / 2f - left1 / 2f) + 2
				* (childWidth + p);
		int unittop = 0;
		int degresstop = 0;
		Rect decadeRect = new Rect(decadeLeft, decadeTop, decadeLeft
				+ childWidth, decade.getHeight()+decadeTop);
		Rect unitRect = new Rect(unitLeft, unittop, unitLeft + childWidth,
				unit.getHeight()+unittop);
		Rect degressRect = new Rect(degressLeft, degresstop, degressLeft + degress.getWidth(),
				degress.getHeight()+unittop);
		canvas.drawBitmap(decade, null, decadeRect, null);
		canvas.drawBitmap(unit, null, unitRect, null);
		canvas.drawBitmap(degress, null, degressRect, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		if(decade!=null) {
			decade.recycle();
			unit.recycle();
			degress.recycle();
        	System.gc();
		}

		return newb;
	}

	

	public static Bitmap bytesToBimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	

	public static byte[] bitmapToBytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static Drawable bitmapToDrawable(Bitmap bmp) {
		return new BitmapDrawable(bmp);
	}
	
	private static String mWay;
	public static String getDayOfWeek(){
		 final Calendar c = Calendar.getInstance();  
	        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));  
	        mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK)); 
	        if("1".equals(mWay)){  
	            mWay ="天";  
	        }else if("2".equals(mWay)){  
	            mWay ="一";  
	        }else if("3".equals(mWay)){  
	            mWay ="二";  
	        }else if("4".equals(mWay)){  
	            mWay ="三";  
	        }else if("5".equals(mWay)){  
	            mWay ="四";  
	        }else if("6".equals(mWay)){  
	            mWay ="五";  
	        }else if("7".equals(mWay)){  
	            mWay ="六";  
	        }  
		
		return "星期"+ mWay;
	}

}