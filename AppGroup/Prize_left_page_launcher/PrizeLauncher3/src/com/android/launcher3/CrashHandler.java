package com.android.launcher3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class CrashHandler implements UncaughtExceptionHandler {

	// 系统默认的UncaughtException处理类
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandler实例
	private static CrashHandler instance;
	// 程序的Context对象
	private Context mContext;
	// 用来存储设备信息和异常信息
	private Map infos = new HashMap();

	// 用于格式化日期,作为日志文件名的一部分
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	private CrashHandler() {
	}

	public static CrashHandler getInstance() {
		if (instance == null)
			instance = new CrashHandler();
		return instance;
	}

	public void init(Context context) {
		mContext = context;
		// Log.e(Constants.TAG, "error : ");
		// 获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
			// 退出程序
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		// 收集设备参数信息
		collectDeviceInfo(mContext);

		// 使用Toast来显示异常信息
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_SHORT)
						.show();
				Looper.loop();
			}
		}.start();
		// 保存日志文件
		try {
			saveCatchInfo2File(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			// Log.e(Constants.TAG,
			// "an error occured when collect package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				// Log.d(Constants.TAG, field.getName() + " : " +
				// field.get(null));
			} catch (Exception e) {
				// Log.e(Constants.TAG,
				// "an error occured when collect crash info", e);
			}
		}
	}

	private String saveCatchInfo2File(Throwable ex) {

		StringBuffer sb = new StringBuffer();
		/*
		 * for (Map.Entry entry : infos.entrySet()) { String key =
		 * entry.getKey(); String value = entry.getValue(); sb.append(key + "="
		 * + value + "\n"); }
		 */
		Set keySet = infos.keySet();
		Iterator iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = (String) infos.get(key);
			sb.append(key + "=" + value + "\n");
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
		sb.append(result);
		try {
			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String fileName = "crash-" + time + "-" + timestamp + ".log";
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				// String path = Constants.SDPATH + "/crash/";
				String path = Environment.getExternalStorageDirectory()
						+ "/crash/";
				File dir = new File(path);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(path + fileName);
				fos.write(sb.toString().getBytes());
				// 发送给开发人员
				sendCrashLog2PM(path + fileName);
				fos.close();
			}
			return fileName;
		} catch (Exception e) {
			Log.v("bian", "caca");
			// Log.e(Constants.TAG, "an error occured while writing file...",
			// e);
		}
		return null;
	}

	private void sendCrashLog2PM(String fileName) {
		if (!new File(fileName).exists()) {
			Toast.makeText(mContext, "日志文件不存在！", Toast.LENGTH_SHORT).show();
			return;
		}
		FileInputStream fis = null;
		BufferedReader reader = null;
		String s = null;
		try {
			fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis, "GBK"));
			while (true) {
				s = reader.readLine();
				if (s == null)
					break;
				// 由于目前尚未确定以何种方式发送，所以先打出log日志。
				Log.i("info", s.toString());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally { // 关闭流
			try {
				reader.close();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
