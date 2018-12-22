package com.prize.weather.util;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

/**
 * @Description TODO(图片优化)
 * @author 彭甜
 * @version 1.0.0
 * @Date 2014-6-20 13:45:00
 * 
 */
@SuppressLint("NewApi")
public class OptimizeImage {
	private static LruCache<String, Bitmap> mMemoryCache; // 图片缓存技术
	private static OptimizeImage optimizeImage;

	/**
	 * @Description TODO(根据传入的宽和高，计算出合适的inSampleSize值)
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// 计算出实际宽高和目标宽高的比率
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	/**
	 * @Description TODO(首先你要将BitmapFactory.Options的inJustDecodeBounds属性设置为true，
	 *              解析一次图片。然后将BitmapFactory.
	 *              Options连同期望的宽度和高度一起传递到到calculateInSampleSize方法中，
	 *              就可以得到合适的inSampleSize值了
	 *              。之后再解析一次图片，使用新获取到的inSampleSize值，并把inJustDecodeBounds
	 *              设置为false，就可以得到压缩后的图片了)
	 * @param filePath
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromFile(String filePath,
			int reqWidth, int reqHeight) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}

	/**
	 * @Description TODO(异步加载图片)
	 * @author 彭甜
	 * 
	 */
	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private ImageView imageView;
		private Bitmap bitmap;
		private SimpleAdapter simpleAdapter;

		public BitmapWorkerTask(ImageView imageView) {
			this.imageView = imageView;
		}

		/**
		 * @Description TODO(在后台加载图片)
		 */
		@Override
		protected Bitmap doInBackground(String... params) {
			try {
				Thread.sleep(50);

				// 读取本地图片进行优化
				final Bitmap bitmap = decodeSampledBitmapFromFile(params[0],100, 100);
				this.bitmap = bitmap;
				addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (imageView != null) {
				imageView.setImageBitmap(bitmap);
				if (simpleAdapter != null) {
					simpleAdapter.notifyDataSetChanged();
				}
			}

		}

	}

	/**
	 * @Description TODO(把最近使用的对象用强引用存储在 LinkedHashMap 中，
	 *              并且把最近最少使用的对象在缓存值达到预设定值之前从内存中移除)
	 * @param key
	 * @param bitmap
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (key != null && getBitmapFromMemCache(key) == null && bitmap != null) {

			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * @Description TODO(获取之前存入图片缓存中的图片引用对象)
	 * @param key
	 *            ,String 缓存引用对象
	 * @return
	 */
	@SuppressLint("NewApi")
	public static Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	/**
	 * @Description TODO(初始化图片缓存)
	 */
	public static void initLruCache() {
		// 获取到可用内存的最大值，使用内存超出这个值会引起OutOfMemory异常。
		// LruCache通过构造函数传入缓存值，以KB为单位。
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		// 使用最大可用内存值的1/8作为缓存的大小。
		int cacheSize = maxMemory / 8;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// 重写此方法来衡量每张图片的大小，默认返回图片数量。
				return bitmap.getByteCount() / 1024;
			}
		};

	}

	/**
	 * @Description TODO(加载图片)
	 * @param imageKey
	 *            ,String 图片缓存 Key也是图片文件路径
	 * @param imageView
	 *            ,ImageView 需要显示的控件
	 */
	public void loadBitmap(String imageKey, ImageView imageView,int resId) {
		final Bitmap bitmap = getBitmapFromMemCache(imageKey);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		} else {
			// 默认展示图片
			imageView.setImageResource(resId);
			BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			task.execute(imageKey);
		}
	}

	/**
	 * @Description TODO(生成一个图片缓存单例)
	 * @return
	 */
	public static synchronized OptimizeImage initializeInstance() {
		if (optimizeImage == null) {
			optimizeImage = new OptimizeImage();
		}
		return optimizeImage;
	}

}
