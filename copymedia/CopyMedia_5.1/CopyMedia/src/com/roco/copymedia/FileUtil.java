package com.roco.copymedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.util.Log;

public class FileUtil {

	private static void debug(String msg) {
		android.util.Log.d("xxczy", "roco=>" + msg);
	}

	public static void copyFolder(File src, File dst) {
		debug("copyFolder " + src.getAbsolutePath() + " to "
				+ dst.getAbsolutePath());
		long startTime = System.currentTimeMillis();
		debug("src.isDir " + src.isDirectory() + " dst.isDir "
				+ dst.isDirectory());
		if (src.isDirectory() && dst.isDirectory()) {
			File[] srcFiles = src.listFiles();
			debug("srcFiles.len " + srcFiles.length);
			if (srcFiles != null) {
				File toFile = null;
				for (int i = 0; i < srcFiles.length; i++) {

					toFile = new File(dst, new String(srcFiles[i].getName()
							.getBytes()));
					if (srcFiles[i].isDirectory()) {
						if (!toFile.exists()) {
							if (!toFile.mkdirs()) {
								debug("mkdirs failed "
										+ toFile.getAbsolutePath());
								continue;
							}
						}
						copyFolder(srcFiles[i], toFile);
					} else {
						copyfile(srcFiles[i], toFile, true);
					}
				}
			}
		}

		long endTime = System.currentTimeMillis();
		long totalMs = (endTime - startTime) / 1000;
		debug("totalTime = " + totalMs / 1000 + "s " + totalMs % 1000 + " ms");
	}

	public static void copyfile(File fromFile, File toFile, Boolean rewrite) {

		debug("start copy " + new String(fromFile.getAbsolutePath().getBytes())
				+ " to " + toFile.getAbsolutePath());

		long startTime = System.currentTimeMillis();
		if (!fromFile.exists()) {
			return;
		}

		if (!fromFile.isFile()) {
			return;
		}
		if (!fromFile.canRead()) {
			return;
		}
		if (!toFile.getParentFile().exists()) {
			toFile.getParentFile().mkdirs();
		}
		if (toFile.exists() && rewrite) {
			toFile.delete();
		}
		debug("check permission ok !");

		try {
			FileInputStream fosfrom = new FileInputStream(fromFile);
			FileOutputStream fosto = new FileOutputStream(toFile);

			byte[] bt = new byte[1024 * 128];
			int c;
			while ((c = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, c);
			}
			fosfrom.close();
			fosto.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		long totalMs = (endTime - startTime) / 1000;
		debug("end copy file time = " + totalMs / 1000 + "s " + totalMs % 1000
				+ " ms");
	}

	public static void delete(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}

		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}

			for (int i = 0; i < childFiles.length; i++) {
				delete(childFiles[i]);
			}
			file.delete();
		}
	}

}
