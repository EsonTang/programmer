package com.prize.lockscreen.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
/***
 * 图片处理工具类
 * @author fanjunchen
 *
 */
public class BitmapUtils {
    private static final String TAG = "BitmapUtils";
    /**剪切图的宽度*/
    public static int CROP_IMG_WIDTH = 720;
    /**剪切图的高度*/
    public static int CROP_IMG_HEIGHT = 640;
    /**剪切图的名称*/
    public static final String COP_FILE_NAME = "croppedimg";

    /**
     * Returns the bitmap from the given uri loaded using the given options.
     * Returns null on failure.
     */
    public static Bitmap loadBitmap(Context context, InputStream is, BitmapFactory.Options o) {
        try {
            return BitmapFactory.decodeStream(is, null, o);
        } finally {
            closeSilently(is);
        }
    }

    public static Bitmap decodeFile(String path, int reqWidth, int reqHeight) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            return decodeStream(fis, reqWidth, reqHeight);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to open resource in path" + path, e);
        } finally {
            closeSilently(fis);
        }
        return null;
    }

    public static Bitmap decodeStream(InputStream is, int reqWidth, int reqHeight) {
        BitmapFactory.Options opts = new BitmapFactory.Options();

        // Determine insample size
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, opts);
        opts.inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);

        // Decode the bitmap, regionally if necessary
        Bitmap bitmap = null;
        opts.inJustDecodeBounds = false;
        Rect rect = getCropRectIfNecessary(opts, reqWidth, reqHeight);
        try {
            if (rect != null) {
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
                bitmap = decoder.decodeRegion(rect, opts);
            } else {
                bitmap = BitmapFactory.decodeStream(is, null, opts);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to decode bitmap from stream", e);
        }
        return bitmap;
    }

    public static Bitmap decodeResource(Resources res, int resId, int reqWidth, int reqHeight) {
        BitmapFactory.Options opts = new BitmapFactory.Options();

        // Determine insample size
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, opts);
        opts.inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);

        // Decode the bitmap, regionally if necessary
        Bitmap bitmap = null;
        opts.inJustDecodeBounds = false;
        Rect rect = getCropRectIfNecessary(opts, reqWidth, reqHeight);

        InputStream stream = null;
        try {
            if (rect != null) {
                stream = res.openRawResource(resId, new TypedValue());
                if (stream == null) return null;
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(stream, false);
                bitmap = decoder.decodeRegion(rect, opts);
            } else {
                bitmap = BitmapFactory.decodeResource(res, resId, opts);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to open resource " + resId, e);
        } finally {
            closeSilently(stream);
        }
        return bitmap;
    }


    public static Bitmap decodeByteArray(byte[] buffer, int reqWidth, int reqHeight) {
        BitmapFactory.Options opts = new BitmapFactory.Options();

        // Determine insample size
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(buffer, 0, buffer.length, opts);
        opts.inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);

        // Decode the bitmap, regionally if necessary
        Bitmap bitmap = null;
        opts.inJustDecodeBounds = false;
        Rect rect = getCropRectIfNecessary(opts, reqWidth, reqHeight);
        try {
            if (rect != null) {
                BitmapRegionDecoder decoder =
                        BitmapRegionDecoder.newInstance(buffer, 0, buffer.length, false);
                bitmap = decoder.decodeRegion(rect, opts);
            } else {
                bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length, opts);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to decode bitmap from stream", e);
        }
        return bitmap;
    }

    public static Bitmap getBitmapFromAsset(Context ctx, String path, int reqWidth, int reqHeight) {
        if (ctx == null || path == null)
            return null;

        Bitmap bitmap = null;
        try {
            AssetManager assets = ctx.getAssets();
            InputStream is = assets.open(path);
            bitmap = decodeStream(is, reqWidth, reqHeight);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    /***
     * 获取某图片从assets目录下
     * @param ctx
     * @param path
     * @return
     */
    public static Bitmap getBitmapFromAsset(Context ctx, String path) {
        if (ctx == null || path == null)
            return null;

        Bitmap bitmap = null;
        try {
            AssetManager assets = ctx.getAssets();
            InputStream is = assets.open(path);
            bitmap = decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    /***
     * 得到某图片
     * @param is
     * @return
     */
    public static Bitmap decodeStream(InputStream is) {
        BitmapFactory.Options opts = new BitmapFactory.Options();

        // Determine insample size
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, opts);
        opts.inSampleSize = 1;

        // Decode the bitmap, regionally if necessary
        Bitmap bitmap = null;
        opts.inJustDecodeBounds = false;
        Rect rect = getCropRectIfNecessary(opts, opts.outWidth, opts.outHeight);
        try {
            if (rect != null) {
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
                bitmap = decoder.decodeRegion(rect, opts);
            } else {
                bitmap = BitmapFactory.decodeStream(is, null, opts);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to decode bitmap from stream", e);
        }
        return bitmap;
    }
    
    /***
     * 得到某图片
     * @param f
     * @return
     */
    public static Bitmap decodeStream(File f) {
    	Bitmap bitmap = null;
    	FileInputStream is = null;
        try {
        	is = new FileInputStream(f);
        	BitmapFactory.Options opts = new BitmapFactory.Options();
            // Determine insample size
        	// Decode the bitmap, regionally if necessary
            opts.inJustDecodeBounds = false;
//            BitmapFactory.decodeStream(is, null, opts);
//            opts.inSampleSize = 1;
            bitmap = BitmapFactory.decodeStream(is, null, opts);
        } catch (Exception e) {
            Log.e(TAG, "Unable to decode bitmap from stream", e);
        } 
        catch (OutOfMemoryError e) {
        	e.printStackTrace();
        }
        finally {
        	if (is != null)
        		try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        }
        return bitmap;
    }


    /**
     * For excessively large images with an awkward ratio we
     * will want to crop them
     * @return
     */
    public static Rect getCropRectIfNecessary(
            BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        Rect rect = new Rect(0, 0, width, height);
        // Determine downsampled size
        int targetWidth = reqWidth * options.inSampleSize;
        int targetHeight = reqHeight * options.inSampleSize;

        if (targetHeight < height) {
            rect.top = (height - targetHeight) / 2;
            rect.bottom = rect.top + targetHeight;
        }
        if (targetWidth < width) {
            rect.left = (width - targetWidth) / 2;
            rect.right = rect.left + targetWidth;
        }
        return rect;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        return calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);
    }

    // Modified from original source:
    // http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    public static int calculateInSampleSize(
            int decodeWidth, int decodeHeight, int reqWidth, int reqHeight) {
        // Raw height and width of image
        int inSampleSize = 1;

        if (decodeHeight > reqHeight || decodeWidth > reqWidth) {
            final int halfHeight = decodeHeight / 2;
            final int halfWidth = decodeWidth / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight &&
                    (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException t) {
            Log.w(TAG, "close fail ", t);
        }
    }
    /***
     * 压缩图片，按指定的质量要求，并存储到指定的文件中
     * @param image
     * @param path 存储路径
     * @return
     */
    public static boolean compressImage(Bitmap image, String path) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 80, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		File f = new File(path);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f);
			out.write(baos.toByteArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return false;
		}
		finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			baos = null;
			f = null;
		}
		return true;
	}
    
    /***
     * 压缩图片，按指定的质量要求，并存储到指定的文件中
     * @param image
     * @param 存储文件
     * @return
     */
    public static boolean compressImage(Bitmap image, File f) {

    	if (null == f)
    		return false;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 80, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f);
			out.write(baos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return false;
		}
		finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			baos = null;
		}
		return true;
	}
    
    /***
     * 压缩图片，按指定的质量要求，并存储到指定的文件中
     * @param image
     * @param f 存储文件
     * @param cent 1-100
     * @return
     */
    public static boolean compressImage(Bitmap image, File f, int cent) {

    	if (null == f)
    		return false;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, cent, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f);
			out.write(baos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return false;
		}
		finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			baos = null;
		}
		return true;
	}
    
    /**
	 * 从某个URI中获取图片
	 * @return
	 */
	public static Bitmap sampleBitmap(Context context, Uri uri,
									  int width, int height){

		Bitmap bitmap = null;
		//先量尺寸，如果太大，要作sample，否则会报OutOfMemory错误
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeStream(context.getContentResolver()
					.openInputStream(uri), null, options);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return bitmap;
		}
		Log.i(TAG, "Original Width = " + options.outWidth);
		Log.i(TAG, "Original Height = " + options.outHeight);

		int sampleWidth = 0;
		int sampleHeight = 0;
		if(options.outWidth < options.outHeight){
			if(width < height){
				sampleWidth = options.outWidth / width;
				sampleHeight = options.outHeight / height;
			}else{
				sampleWidth = options.outWidth / height;
				sampleHeight = options.outHeight / width;
			}
		}else{
			if(width < height){
				sampleWidth = options.outHeight / width;
				sampleHeight = options.outWidth / height;
			}else{
				sampleWidth = options.outHeight / height;
				sampleHeight = options.outWidth / width;
			}
		}
		int sampleSize = Math.max(sampleWidth, sampleHeight);
		options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		// options.inSampleSize = sampleSize;
		options.inSampleSize = 1; // 无须放大或缩小 fanjunchen
		try {
			bitmap = BitmapFactory.decodeStream(context.getContentResolver()
					.openInputStream(uri), null, options);
			Log.i(TAG, "Scaled Width = " + bitmap.getWidth());
			Log.i(TAG, "Scaled Height = " + bitmap.getHeight());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	/***
	 * 设置壁纸
	 * @param ctx
	 * @param bitmap
	 */
	public static boolean setWallpaperPrize(Context ctx, Bitmap bitmap) {
		try {
			WallpaperManager wallpaperManager = WallpaperManager.getInstance(ctx);
			wallpaperManager.setBitmap(bitmap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/***
	 * 设置壁纸
	 * @param ctx
	 * @param f 图片文件
	 */
	public static boolean setWallpaperPrize(Context ctx, File f) {
		FileInputStream fin = null;
		try {
			if (f.exists()) {
				fin = new FileInputStream(f);
				WallpaperManager wallpaperManager = WallpaperManager.getInstance(ctx);
				wallpaperManager.setStream(fin);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
}
