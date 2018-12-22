package com.android.launcher3;

import java.io.File;
import android.content.Context;
import android.os.Environment;
public class DataCleanManager {
	public static void cleanInternalCache(Context context) {
		deleteFilesByDirectory(context.getCacheDir());
	}
	public static void cleanDatabases(Context context) {
		deleteFilesByDirectory(new File("/data/data/"
				+ context.getPackageName() + "/databases"));
	}
	public static void cleanSharedPreference(Context context) {
		deleteFilesByDirectory(new File("/data/data/"
				+ context.getPackageName() + "/shared_prefs"));
	}
	public static void cleanDatabaseByName(Context context, String dbName) {
		context.deleteDatabase(dbName);
	}

	public static void cleanFiles(Context context) {
		deleteFilesByDirectory(context.getFilesDir());
	}
	public static void cleanExternalCache(Context context) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			deleteFilesByDirectory(context.getExternalCacheDir());
		}
	}
	public static void cleanCustomCache(String filePath) {
		deleteFilesByDirectory(new File(filePath));
	}
	public static void cleanApplicationData(Context context, String... filepath) {
		cleanInternalCache(context);
		cleanExternalCache(context);
		cleanDatabases(context);
		cleanSharedPreference(context);
		cleanFiles(context);
		for (String filePath : filepath) {
			cleanCustomCache(filePath);
		}
	}
	public static void cleanApplicationData(Context context) {
		cleanInternalCache(context);
		cleanExternalCache(context);
		cleanDatabases(context);
		cleanSharedPreference(context);
		cleanFiles(context);
		/*for (String filePath : filepath) {
			cleanCustomCache(filePath);
		}*/
	}
	private static void deleteFilesByDirectory(File directory) {
		if (directory != null && directory.exists() && directory.isDirectory()) {
			for (File item : directory.listFiles()) {
				item.delete();
			}
		}
	}
}