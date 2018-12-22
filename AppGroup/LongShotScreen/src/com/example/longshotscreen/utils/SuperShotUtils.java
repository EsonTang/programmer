package com.example.longshotscreen.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import com.example.longshotscreen.utils.Log;
import android.widget.Toast;
import android.util.TypedValue;

import com.example.longshotscreen.R;
import com.example.longshotscreen.manager.NoticeParam;
import com.example.longshotscreen.manager.SuperShotNotificationManager;
import com.example.longshotscreen.ui.SavingAnimation;

public class SuperShotUtils
{
	public static boolean mIsSaveComplete = false;
	private static SavingAnimation mSavingAnimation;
	private static Thread mThread;

	public static boolean isStringEmpty(String str)
	{
		return ((str != null) && (str.trim().length() != 0));
	}

	public static boolean isStringNotEmpty(String str)
	{
		return (!isStringEmpty(str));
	}

	public static String getResourceString(Context context, int resId)
	{
		return context.getResources().getString(resId);
	}

	public static Intent getImageFileIntent(File file, Context context)
	{
		Intent intent = new Intent("android.intent.action.VIEW");
		/*prize fix bug 34221 View photos by Gallery app huangpengfei 2017-6-5 start*/
		isRecognizedFileType(context, intent);
		/*prize fix bug 34221 View photos by Gallery app huangpengfei 2017-6-5 end*/
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(file), "image/*");
		return intent;
	}
	
	/*prize fix bug 34221 View photos by Gallery app huangpengfei 2017-6-5 start*/
	public static boolean isRecognizedFileType(Context context, Intent intent) {
		
		boolean ret = true;
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo("com.android.gallery3d", 0);
			if(packageInfo != null){
				intent.setPackage("com.android.gallery3d");
				ret = true;
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = false;
		}
        return ret;
    }
	/*prize fix bug 34221 View photos by Gallery app huangpengfei 2017-6-5 end*/

	public static boolean isInternalSDCard()
	{
		String str = Environment.getExternalStorageDirectory().getAbsolutePath();
		return ((isStringNotEmpty(str)) && (str.equals("/storage/sdcard0")));
	}

	public static String getAvaibleSDCardPath()
	{
		String str = Environment.getExternalStorageDirectory().getAbsolutePath();
		if (!Environment.getExternalStorageState().equals("mounted"))
			str = "/storage/sdcard0";
		return str;
	}

	public static String getSaveFilePath()
	{
		return getAvaibleSDCardPath() + "/超级截屏/";
	}

	private static void saveImage(Bitmap bitmap, String type, Context context)
	{
		mIsSaveComplete = false;
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String date = df.format(new Date(System.currentTimeMillis()));
		File fileDir = new File(getSaveFilePath());
		Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
		File file = new File(getSaveFilePath(), "_" + date + ".png");
		try
		{
			if(!fileDir.exists()) {
				fileDir.mkdirs();
			}
			if(!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fileOps = new FileOutputStream(file);
			if(bitmap.compress(compressFormat, 50, fileOps)) {
				//Looper.prepare();
				int content = isInternalSDCard() ? R.string.screenshot_saved_to_tf_card : R.string.screenshot_saved_to_tf_card;
				NoticeParam notice = new NoticeParam(100, 
						getResourceString(context, R.string.screenshot_saved), 
						getResourceString(context, content), 
						getImageFileIntent(file, context), R.drawable.image_save_noti, 
						getResourceString(context, R.string.screenshot_saving));
				
				SuperShotNotificationManager notificationManager = new SuperShotNotificationManager(context);
				notificationManager.sendImageSaveNoticefication(notice);
				context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(file)));
				fileOps.flush();
				fileOps.close();
				mIsSaveComplete = true;
				return;
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void setSavingAnimation(Context context)
	{
		mSavingAnimation = SavingAnimation.getInstance(context);
	}

	public static void saveImageToTFCard(Bitmap bitmap, String type, Context context)
	{
		if (mSavingAnimation == null){
			setSavingAnimation(context);
		}
		mSavingAnimation.show(bitmap);
		mThread = new Thread(new SuperShotUtilRunnable(bitmap, type, context));
		mThread.start();
	}

	private static class SuperShotUtilRunnable implements Runnable
	{
		Bitmap mBitmap = null;
		Context mContext = null;
		String mType = null;

		SuperShotUtilRunnable(Bitmap bitmap, String type, Context context)
		{
			mBitmap = bitmap;
			mType = type;
			mContext = context;
		}

		public void run()
		{
			saveImage(mBitmap, mType, mContext);
			/*prize-remove fix-bug[31383]-huangpengfei-2017-3-31-start*/
//			if ((SuperShotUtils.mThread != null) && (SuperShotUtils.mThread.isAlive()))
//			{
//			SuperShotUtils.mThread.interrupt();
//			mThread = null;
//			System.exit(0);
//			}
			/*prize-remove fix-bug[31383]-huangpengfei-2017-3-31-end*/
		}
	}

	public static boolean getColorAndRemoveColor(Bitmap backBitmap, Bitmap middleBitmap, Rect rect, int backPaintColor, int middlePaintColor, int backStrokeWidth)
	{
		boolean bool1 = false;
		if ((backBitmap.isRecycled()) || (middleBitmap.isRecycled())){

			return false;					
		}
		ArrayList<Integer> arrayList1 = new ArrayList<Integer>();
		ArrayList<Integer> arrayList2 = new ArrayList<Integer>();

		for (int verCell = rect.top; verCell < rect.bottom; verCell = verCell + 1)
		{
			for(int horCell = rect.left; horCell < rect.right; horCell = horCell + 1)
			{
				if(((verCell - backStrokeWidth) >= 0) && (middleBitmap.getPixel(horCell, (verCell - backStrokeWidth)) == 0))
				{
					arrayList1.add(Integer.valueOf(horCell));
					arrayList2.add(Integer.valueOf(verCell));
				}
				else if(((verCell + backStrokeWidth) < rect.bottom) && (middleBitmap.getPixel(horCell, (verCell + backStrokeWidth)) == 0)) {
					arrayList1.add(Integer.valueOf(horCell));
					arrayList2.add(Integer.valueOf(verCell));
				}else if(((horCell - backStrokeWidth) >= 0) && (middleBitmap.getPixel((horCell - backStrokeWidth), verCell) == 0)) {
					arrayList1.add(Integer.valueOf(horCell));
					arrayList2.add(Integer.valueOf(verCell));
				}
				else if(((horCell + backStrokeWidth) < rect.right) && (middleBitmap.getPixel((horCell + backStrokeWidth), verCell) == 0)) {
					arrayList1.add(Integer.valueOf(horCell));
					arrayList2.add(Integer.valueOf(verCell));
				}
				if((middleBitmap.getPixel(horCell, verCell) == middlePaintColor) && (!bool1)) {
					bool1 = true;
				}
			}

		}
		for(int j = 0; j < arrayList1.size(); j = j + 1) {
			middleBitmap.setPixel((Integer)arrayList1.get(j).intValue(), (Integer)arrayList2.get(j).intValue(), 0);
		}
		return bool1;
	}

	public static Bitmap mergeImage(Bitmap paramBitmap1, Bitmap paramBitmap2, PorterDuff.Mode paramMode)
	{
		if (!paramBitmap1.hasAlpha()){
			return paramBitmap1;
		}

		Canvas localCanvas = new Canvas(paramBitmap1);
		Paint localPaint = new Paint();
		localPaint.setXfermode(new PorterDuffXfermode(paramMode));
		localCanvas.drawBitmap(paramBitmap2, 0.0F, 0.0F, localPaint);
		return paramBitmap1;
	}

	public static float getDistance(ImageRangInfo.PixelCoor paramPixelCoor1, ImageRangInfo.PixelCoor paramPixelCoor2)
	{
		float f1 = Math.abs(paramPixelCoor1.mx - paramPixelCoor2.mx);
		float f2 = Math.abs(paramPixelCoor1.my - paramPixelCoor2.my);
		return (float)Math.sqrt((f1 * f1) + (f2 * f2));
	}

	public static int getStatusBarHeight(Context context)
	{
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;

	}

	public static int getActionBarHeight(Context context)
	{
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();  
		if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {  
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());  
		}
		return actionBarHeight;

	}

	public static void saveScrollShotImageToTFCard(Bitmap bitmap, String str, Context context)
	{
		if (bitmap != null)
		{
			saveImage(bitmap, str, context);
		}
	}

	public static boolean isHome(Context context)
	{
		ArrayList<String> arrayList = new ArrayList<String>();
		PackageManager packageManager = context.getPackageManager();
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		Iterator iterator = packageManager.queryIntentActivities(intent, 100).iterator();
		while (iterator.hasNext()){
			arrayList.add(((ResolveInfo)iterator.next()).activityInfo.packageName);	
		}
		Toast.makeText(context, "app is:" + (((ActivityManager)context.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getPackageName(), 0);
		return arrayList.contains(((ActivityManager.RunningTaskInfo)((ActivityManager)context.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getPackageName());
	}

	public static boolean isInUnmoveableApp(Context context)
	{
	
	/*PRIZE-IN-CALL-UI-unable-execute-longshotscreen-shiyicheng-2015-12-07-start*/
	/*PRIZE-camera-unable-execute-longshotscreen-shiyicheng-2015-12-07-start*/
		//return ((ActivityManager.RunningTaskInfo)((ActivityManager)context.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getClassName().contains("com.android.dialer.DialtactsActivity");
		return ((ActivityManager.RunningTaskInfo)((ActivityManager)context
				.getSystemService("activity"))
				.getRunningTasks(1).get(0))
				.topActivity.getClassName()
				.contains("com.android.dialer.DialtactsActivity") ||
				((ActivityManager.RunningTaskInfo)((ActivityManager)context
				.getSystemService("activity"))
				.getRunningTasks(1).get(0))
				.topActivity.getClassName()
				.contains("com.android.incallui.InCallActivity") ||
				((ActivityManager.RunningTaskInfo)((ActivityManager)context
				.getSystemService("activity"))
				.getRunningTasks(1).get(0))
				.topActivity.getClassName()
				.contains("com.android.camera.CameraLauncher");
	/*PRIZE-camera-unable-execute-longshotscreen-shiyicheng-2015-12-07-end*/
	/*PRIZE-IN-CALL-UI-unable-execute-longshotscreen-shiyicheng-2015-12-07-end*/
	}
	
	/** 
     * 用来判断服务是否运行. 
     * @param context 
     * @param className 判断的服务名字 
     * @return true 在运行 false 不在运行 
     */
    public static boolean isServiceRunning(Context mContext,String className) { 
        boolean isRunning = false; 
        	ActivityManager activityManager = (ActivityManager) 
        			mContext.getSystemService(Context.ACTIVITY_SERVICE);  
        List<ActivityManager.RunningServiceInfo> serviceList  
        = activityManager.getRunningServices(30); 
       if (!(serviceList.size()>0)) { 
            return false; 
        } 
        for (int i=0; i<serviceList.size(); i++) { 
            if (serviceList.get(i).service.getClassName().equals(className) == true) { 
                isRunning = true; 
                break; 
            } 
        } 
        return isRunning; 
    } 

}
