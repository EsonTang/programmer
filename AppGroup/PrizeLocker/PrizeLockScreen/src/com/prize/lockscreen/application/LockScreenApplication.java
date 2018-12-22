package com.prize.lockscreen.application;

import java.io.File;
import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.SoundPool;
import android.os.AsyncTask;

import com.prize.lockscreen.service.LockScreenService;
import com.prize.lockscreen.utils.ColorHelper;
import com.prize.lockscreen.utils.DisplayUtil;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.prizelockscreen.R;
import com.prize.theme.util.BitmapUtils;
/***
 * 用来存放一些全局变量
 * @author fanjunchen
 *
 */
public class LockScreenApplication extends Application {

	private static Context mContext;
	/**锁屏的壁纸图片*/
	private static Bitmap bgImg = null;
	/**模糊的锁屏壁纸图片*/
	private static Bitmap blurBitmap = null;
	/**锁屏背景图片名称*/
	private static final String IMG_FILE_NAME = "bg.data";
	/**锁屏图片路径*/
	public static String IMG_PATH = null;
	
	private static SoundPool soundPool = null;
	/**是否有模糊化任务在运行*/
	private static boolean isBlurRun = false;
	
	private static final HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
	
	private static Bitmap tmpBitmap = null;

	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.i("LockScreenApplication", "===>onCreate()");
		mContext = getApplicationContext();
		Intent intent = new Intent(this, LockScreenService.class);
		mContext.startService(intent);
		
		IMG_PATH = mContext.getFilesDir().getAbsolutePath() + File.separator + IMG_FILE_NAME;
		soundPool = new SoundPool.Builder().setMaxStreams(2).build();
		
		soundMap.put(1, soundPool.load(mContext, R.raw.lock, 1));
		soundMap.put(2, soundPool.load(mContext, R.raw.unlock, 1));
		
		tmpBitmap = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.default_bg);
	}
	
	public static Context getContext() {
		return mContext;
	}
	/***
	 * 获取壁纸存放路径
	 * @return
	 */
	public static String getPaperPath() {
		return IMG_PATH;
	}
	/***
	 * 获取背景图片
	 * @return
	 */
	public static Bitmap getBgImg() {
		
		if (null == bgImg || bgImg.isRecycled()) {
			File f = new File(IMG_PATH);
			if (f.exists())
				bgImg = BitmapUtils.decodeStream(f);
			else
				bgImg = tmpBitmap;
			f = null;
			if (bgImg != null) {
				int width = bgImg.getWidth();
				int height = bgImg.getHeight();
				// 设置想要的大小
				int newWidth = DisplayUtil.getScreenWidthPixels();
				int newHeight = DisplayUtil.getScreenHeightPixels();
				// 计算缩放比例
				float scaleWidth = ((float) newWidth) / width;
				float scaleHeight = ((float) newHeight) / height;
				// 取得想要缩放的matrix参数
				Matrix matrix = new Matrix();
				matrix.postScale(scaleWidth, scaleHeight);
				Bitmap tempBitmap = Bitmap.createBitmap(bgImg, 0, 0, width, height,
						matrix, true);
				
				if (tempBitmap != null && bgImg != tempBitmap) {
					bgImg.recycle();
					bgImg = tempBitmap;
				}
				
				new BlurTask().execute(bgImg);
			}
		}
		
		return bgImg;
	}
	
	public static Bitmap getBlurBg() {
		if (null == blurBitmap || blurBitmap.isRecycled()) {
			getBgImg();
			if (!isBlurRun)
				new BlurTask().execute(bgImg);
		}
		return blurBitmap;
	}
	/***
	 * 强制获取背景图片
	 * @return
	 */
	public static void forceGetBgImg() {
		
		if (null != bgImg && !bgImg.isRecycled())
			bgImg.recycle();
		bgImg = null;
		getBgImg();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		
		if (bgImg != null)
			bgImg.recycle();
		bgImg = null;
		
		if (blurBitmap != null)
			blurBitmap.recycle();
		blurBitmap = null;
		
		isBlurRun = false;
	}
	/***
	 * 播放声音
	 * @param i 1表示锁屏， 2表示解锁
	 */
	public static void playSound(int i) {
		if (soundPool != null && i>0 && i<3)
			soundPool.play(soundMap.get(i), 5, 5, 0, 0, 1);
	}
	/***
	 * 异步处理图片模糊化
	 * @author fanjunchen
	 *
	 */
	static class BlurTask extends AsyncTask<Bitmap, Void, Void> {
		
		public void onPreExecute() {
			isBlurRun = true;
		}
		@Override
		protected Void doInBackground(Bitmap... params) {
			if (null == params || null == params[0] || 
					params[0].isRecycled())
				return null;
			
			Bitmap ac = ColorHelper.blurScale(params[0]);
			
			if (ac != null) {
				if (blurBitmap != null)
					blurBitmap.recycle();
				blurBitmap = null;
				blurBitmap = ac;
			}
			return null;
		}
		
		public void onPostExecute(Void a) {
			isBlurRun = false;
		}
	}
}
