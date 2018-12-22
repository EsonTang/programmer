package com.koobee.koobeecenter.utils;

import java.io.File;

import android.os.Environment;

public class FileUtils {
	
	public static final String	ROOT_DIR= "koobeeCenter";


	/** 获取SD下的应用目录 */
	public static String getExternalStoragePath()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
		sb.append(File.separator);
		sb.append(ROOT_DIR);
		sb.append(File.separator);
		return sb.toString();
	}
}
