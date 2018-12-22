package com.example.longshotscreen;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import com.example.longshotscreen.utils.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Environment;

import android.content.Context;
import android.content.Intent;
/**
 * Created by xf on 2014/9/14.
 */
public class SurfaceControl {
	private static Context mcontext;
	public static Bitmap screenshot(View view) {

		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap bmp = view.getDrawingCache();
		return bmp;
	}

	public static void savePic(Bitmap b,String strFileName){
		FileOutputStream fos = null;

		try {

			fos = new FileOutputStream(strFileName);

			if (null != fos)

			{

				b.compress(Bitmap.CompressFormat.PNG, 90, fos);

				fos.flush();

				fos.close();

			}

		} catch (FileNotFoundException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

	public static void shoot(View view){

		long l = System.currentTimeMillis();
		String str1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(l));
		Object[] arrayOfObject = new Object[1];
		arrayOfObject[0] = str1;
		String str2 = String.format("ScreenShot_%s.png", arrayOfObject);
		File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "ScreenShot");
		localFile.mkdirs();
		// ScreenShotApp.mFilePath = new File(localFile, str2).getAbsolutePath();
		//Log.i("xxx" , "mFilePath" + ScreenShotApp.mFilePath);

		//SurfaceControl.savePic(SurfaceControl.screenshot(view), ScreenShotApp.mFilePath);

		//Intent intent = new Intent();  
		//intent.setAction("com.syc.picture.open"); 
		//intent.putExtra("file_path", mFilePath);
		//mcontext.sendBroadcast(intent);

	}


}
