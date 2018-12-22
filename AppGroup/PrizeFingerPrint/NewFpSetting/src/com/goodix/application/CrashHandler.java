/************************************************************************
 * <p>Title: CrashHandler.java</p>
 * <p>Description: </p>
 * <p>Copyright (C), 1997-2014, Shenzhen Goodix Technology Co.,Ltd.</p>
 * <p>Company: Goodix</p>
 * @author  peng.hu
 * @date    2014-5-20
 * @version  1.0
 ************************************************************************/
package com.goodix.application;

import android.content.pm.PackageManager.NameNotFoundException;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.io.FileOutputStream;

import android.content.Context;
import android.os.Environment;

import java.lang.reflect.Field;

import android.widget.Toast;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.io.PrintWriter;

import android.os.Looper;
import android.util.Log;
import android.os.Build;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.io.Writer;
import java.io.File;

import com.goodix.fpsetting.R;
import com.goodix.util.ToastUtil;

/**
 * <p>
 * Title: CrashHandler.java
 * </p>
 * <p>
 * Description:
 * </p>
 */
public class CrashHandler implements UncaughtExceptionHandler {
	private static final String TAG = "CrashHandler";
	private static CrashHandler INSTANCE = new CrashHandler();
	private Context mContext = null;

	private Map<String, String> mInfo = new HashMap<String, String>();
	private Thread.UncaughtExceptionHandler mDefaultHandler = null;

	private SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd-HH-mm-ss", Locale.getDefault());

	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(1);
			}
		}
	}

	/**
	 * @Title: init
	 * @Description:
	 * @param @param context d
	 * @return void
	 * @throws
	 */
	public void init(Context context) {
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * @Title: handleException
	 * @Description:
	 * @param @param ex
	 * @return boolean true
	 * @throws
	 */
	public boolean handleException(Throwable ex) {
		if (ex == null)
			return false;
		new Thread() {
			public void run() {
				Looper.prepare();
				ToastUtil.showToast(mContext,mContext.getResources().getString(R.string.application_crash));
				Looper.loop();
			}
		}.start();
		collectDeviceInfo(mContext);
		saveCrashInfo2File(ex);
		return true;
	}

	/**
	 * @Title: collectDeviceInfo
	 * @Description:
	 * @param @param context
	 * @return void
	 * @throws
	 */
	public void collectDeviceInfo(Context context) {
		try {
			PackageManager pm = context.getPackageManager();

			PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				mInfo.put("versionName", versionName);
				mInfo.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				mInfo.put(field.getName(), field.get("").toString());
				Log.d(TAG, field.getName() + ":" + field.get(""));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @Title: saveCrashInfo2File
	 * @Description: /sdcard/crash/crash-XXX.log
	 * @param @param ex
	 * @return String
	 * @throws
	 */
	private String saveCrashInfo2File(Throwable ex) {
		StringBuffer stringBuffer = new StringBuffer();
		String key;
		String value;

		for (Map.Entry<String, String> entry : mInfo.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			stringBuffer.append(key + "=" + value + "\r\n");
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		stringBuffer.append(result);

		long timetamp = System.currentTimeMillis();
		String time = format.format(new Date());
		String fileName = "crash-" + time + "-" + timetamp + ".log";
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				/*
				 * File dir = new File(Environment.getExternalStorageDirectory()
				 * .getAbsolutePath() + File.separator + "crash");
				 */
				File dir = new File("sdcard" + File.separator + "crash");
				Log.i(TAG, "the dir is  " + dir.toString());
				if (!dir.exists()) {
					dir.mkdir();
				}
				FileOutputStream fos = new FileOutputStream(new File(dir,
						fileName));
				fos.write(stringBuffer.toString().getBytes());
				fos.close();
				return fileName;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
