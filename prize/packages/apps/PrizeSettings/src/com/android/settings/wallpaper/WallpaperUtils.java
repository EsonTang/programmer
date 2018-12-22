package com.android.settings.wallpaper;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.settings.R;
import android.provider.Settings;

import com.mediatek.common.prizeoption.PrizeOption;

public class WallpaperUtils {

	public static final String ROOT_DIR = "PrizeLiveStore";

	public static String getDir(String name) {
		StringBuilder sb = new StringBuilder();
		if (isSDCardAvailable()) {
			sb.append(getExternalStoragePath());
		}
		sb.append(name);
		sb.append(File.separator);
		String path = sb.toString();
		if (createDirs(path)) {
			return path;
		} else {
			return null;
		}
	}

	public static boolean isSDCardAvailable() {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			return true;
		} else {
			return false;
		}
	}

	public static String getExternalStoragePath() {
		StringBuilder sb = new StringBuilder();
		sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
		sb.append(File.separator);
		sb.append(ROOT_DIR);
		sb.append(File.separator);
		return sb.toString();
	}

	public static boolean createDirs(String dirPath) {
		File file = new File(dirPath);
		if (!file.exists() || !file.isDirectory()) {
			return file.mkdirs();
		}
		return true;
	}

	private static Bitmap mBitmap;

	public static boolean isExistFile(String path) {
		File f = new File(path);
		boolean b = f.exists();
		f = null;
		return b;
	}

	public static void applyType(Context context, int type, Bitmap bit,
			String path, int activityType, boolean isScroll) {
		new ApplyTask(context, type, bit, path, activityType, isScroll)
				.execute();
	}

	static class ApplyTask extends AsyncTask<Void, Void, Boolean> {

		private Context mCtx;

		private int type;

		private Bitmap bitmap;

		private String path;

		private boolean isLockSuccess = false;

		private int mActivityType;
		private boolean isScroll = false;

		public ApplyTask(Context context, int t, Bitmap bit, String p,
				int pType, boolean Scroll) {
			mCtx = context;
			type = t;
			bitmap = bit;
			path = p;
			mActivityType = pType;
			isScroll = Scroll;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			boolean b = false;
			publishProgress();
			/*prize-delete-bugid:60082-bxh-2018_6_6-start*/
			if (type != 1) {
				ShutDownKGWALLPAPE(mCtx);
			} 
			/*prize-modify-bugid:62522-bxh-018_6_20-start*/	
			if( isScroll || mActivityType ==4){
			/*prize-modify-bugid:62522-bxh-018_6_20-end*/	
				b = setWallpaperPrizeByStream(mCtx, getWallpaperInputStream(mCtx,path),type);
			} else {
				b = setWallpaperPrize(mCtx,bitmap,type);
			}
			/*prize-delete-bugid:60082-bxh-2018_6_6-end*/
			return b;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			initPop((Activity) mCtx);
		}

		@Override
		public void onPostExecute(Boolean b) {
			if (b) {
				rightPopupWindow.dismiss();
				if (type != 2)
					Toast.makeText(mCtx, mCtx.getString(R.string.wall_is_set),
							Toast.LENGTH_SHORT).show();
				if (mCtx instanceof PreviewActivity && mActivityType == 1) {
					PreviewActivity p = (PreviewActivity) mCtx;
					Intent intent = new Intent();
					intent.setAction("com.prize.local.wall");
					intent.putExtra("localWallPath", path);
					p.sendBroadcast(intent);
				}
			} else {
				Toast.makeText(mCtx, mCtx.getString(R.string.failed_to_set),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public static boolean setWallpaperPrizeByStream(Context ctx, InputStream in,int type) {
		try {
			WallpaperManager wallpaperManager = WallpaperManager
					.getInstance(ctx);
			/* prize-change-bugid:44857-yangming-2017_12_11-start */
			// wallpaperManager.setStream(in);
			if (type == 1) {
				wallpaperManager.setStream(in, null, true,
						WallpaperManager.FLAG_SYSTEM);
		/*prize-change-bugid:60082-bxh-2018_6_6-start*/		
			} else if (type == 2) {
				
				wallpaperManager.setStream(in, null, true,
						WallpaperManager.FLAG_LOCK);
			} else {
				wallpaperManager.setStream(in);
			}

		/* prize-change-bugid:44857-yangming-2017_12_11-end */
			
			/*prize-change-bugid:60082-bxh-2018_6_6-end*/
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean setWallpaperPrize(Context ctx, Bitmap bitmap,int type) {
		try {
			/*prize-change-bxh-2018_6_7-start*/	
			WallpaperManager wallpaperManager = WallpaperManager
					.getInstance(ctx);
			if( type == 1 ) {
				wallpaperManager.setBitmap(bitmap, null, true,
						WallpaperManager.FLAG_SYSTEM);
			} else if (type == 2 ){
				wallpaperManager.setBitmap(bitmap, null, true,
						WallpaperManager.FLAG_LOCK);
			} else {
				wallpaperManager.setBitmap(bitmap); 
			}
			
			/*prize-change-bxh-2018_6_7-end*/	
			/* prize-change-bugid:44857-yangming-2017_12_11-start */
			// wallpaperManager.setBitmap(bitmap);
			/* prize-change-bugid:44857-yangming-2017_12_11-end */
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void ShutDownKGWALLPAPE(Context context) {
		Log.i("settings", "ShutDownKGWALLPAPE sendBrodcast!!");
		/* prize-bugid:51650-change-yangming-2018_3_1-start */
		/*
		 * Settings.System.putInt(context.getContentResolver(),
		 * Settings.System.PRIZE_KGWALLPAPER_SWITCH, 0); Intent intent = new
		 * Intent("system.settings.changedwallpaper.off");
		 * context.sendBroadcast(intent);
		 */
		if (PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW) {
			Settings.System.putInt(context.getContentResolver(),
					Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH, 0);
		} else {
			Settings.System.putInt(context.getContentResolver(),
					Settings.System.PRIZE_KGWALLPAPER_SWITCH, 0);
			Intent intent = new Intent("system.settings.changedwallpaper.off");
			context.sendBroadcast(intent);
		}
		/* prize-bugid:51650-change-yangming-2018_3_1-end */
	}

	private static AlertDialog rightPopupWindow = null;

	private static void initPop(Activity context) {
		rightPopupWindow = new AlertDialog.Builder(context).create();
		rightPopupWindow.show();
		rightPopupWindow.setCancelable(false);
		rightPopupWindow.setCanceledOnTouchOutside(false);
		View loginwindow = context.getLayoutInflater().inflate(
				R.layout.popwindow_setwallpaper_layout, null);
		Window window = rightPopupWindow.getWindow();
		window.setContentView(loginwindow);
		WindowManager.LayoutParams p = window.getAttributes();
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		if (width <= 720) {
			p.width = 600;
		} else {
			p.width = 900;
		}
		p.height = WindowManager.LayoutParams.WRAP_CONTENT;

		window.setAttributes(p);
		window.setGravity(Gravity.CENTER);
	}

	public static File createFile(String dir, String name) {
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}

		File file = new File(dirFile, name);
		return file;
	}

	public static void saveBitmapToFile(Bitmap bitmap, File fileName) {
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(fileName));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setScrollBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
	}

	public static Bitmap getWallpaper(Context applicationContext,
			String wallPath) {
		Resources resources = null;
		if (resources == null) {
			resources = getWallpaperRes(applicationContext, wallPath);
		}
		InputStream instr = null;
		Bitmap rettemp = null;
		int dpi = 2;
		try {
			if (resources != null)
				if (dpi == 1) {
					instr = resources.getAssets().open("wallpaper/h.jpg");
				} else if (dpi == 2) {
					instr = resources.getAssets().open("wallpaper/x.jpg");
				} else if (dpi == 3) {
					instr = resources.getAssets().open("wallpaper/xx.jpg");
				}
			if (instr != null) {
				rettemp = BitmapFactory.decodeStream(instr);
				instr.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rettemp;
	}
	/*prize-add-bugid:60082-bxh-2018_6_6-start*/
	/**
	 * 
	 * @param applicationContext
	 * @param wallPath
	 * @return
	 */
	public static InputStream getWallpaperInputStream(Context applicationContext, String wallPath) {

		InputStream instr = null;
		File imgFile = new File(wallPath);
		if (imgFile.exists()) {
			try {
				instr = new FileInputStream(wallPath);
			} catch (Exception e) {
			}
		}
		return instr;
	}
	/*prize-add-bugid:60082-bxh-2018_6_6-end*/
	public static Resources getWallpaperRes(Context context, String themePath) {
		Resources s = null;
		try {
			AssetManager asm = AssetManager.class.newInstance();
			AssetManager.class.getMethod("addAssetPath", String.class).invoke(
					asm, themePath);
			Resources res = context.getResources();
			s = new Resources(asm, res.getDisplayMetrics(),
					res.getConfiguration());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return s;
	}

}
