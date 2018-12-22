package com.android.notepad.note.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

public class FileUtil {
	public static String CONFIG_FILE = Environment.getExternalStorageDirectory()+ File.separator + ".[NotePad]";
	public static String CLIP_BOARD_FILE = Environment.getExternalStorageDirectory()+ File.separator + ".[NotePad]" 
											+ File.separator + "ShearStorage";

	public static List<File> getDirFiles(String fileName) {
		String filePath = CONFIG_FILE + File.separator +fileName;
		File file = new File(filePath);
		File patentFile = file.getParentFile();
		List<File> fileList = null;
		try {
			if(!patentFile.exists()){
				patentFile.mkdirs();
			}
			if(!file.exists()){
				file.mkdirs();
				return new ArrayList<File>();
			}
			fileList = new ArrayList<File>();
			File[] files = file.listFiles();
			for (File oneFile:files) {
				fileList.add(oneFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileList;
	}

	public static String copyNewNoteImageFile(String imagePath, String uuid){
		File file = null;
		String fileName = null;
		int index = 0;
		File newFile = null;
		File parentFile = null;
		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			file = new File(imagePath);
			if(!file.exists()){
				return null;
			}
			index = imagePath.lastIndexOf(File.separator);
			fileName = imagePath.substring(index+1);
			fileName = CONFIG_FILE + File.separator + uuid + File.separator + fileName;
			newFile = new File(fileName);
			parentFile = newFile.getParentFile();
			if(!parentFile.exists()){
				parentFile.mkdirs();
			}
			if(newFile.exists()){
				return fileName;
			}
			fi = new FileInputStream(file);
			fo = new FileOutputStream(newFile);
			in = fi.getChannel();
			out = fo.getChannel();
			in.transferTo(0, in.size(), out);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(null != fi){
					fi.close();
				}
				if(null != in){
					in.close();
				}
				if(null != fo){
					fo.close();
				}
				if(null != out){
					out.close();
				}
			} catch (IOException e) {              
				e.printStackTrace();
			}
		}
		return fileName;
	}

	public static void deleteNoteImageFile(String uuid){
		String dirPath = null;
		File dirFile = null;
		try {
			dirPath = CONFIG_FILE + File.separator + uuid;
			dirFile = new File(dirPath);
			if(dirFile.exists()){
				File[] files = dirFile.listFiles();
				for(File file:files){
					file.delete();
				}
				dirFile.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean copyFile(String oldFilePath, String newFilePath){
		File file = null;
		boolean result = false;
		File newFile = null;
		File parentFile = null;
		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			file = new File(oldFilePath);
			if(!file.exists()){
				return false;
			}
			newFile = new File(newFilePath);
			parentFile = newFile.getParentFile();
			if(!parentFile.exists()){
				parentFile.mkdirs();
			}
			if(newFile.exists()){
				parentFile.delete();
				parentFile.mkdirs();
			}
			fi = new FileInputStream(file);
			fo = new FileOutputStream(newFile);
			in = fi.getChannel();
			out = fo.getChannel();
			in.transferTo(0, in.size(), out);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(null != fi){
					fi.close();
				}
				if(null != in){
					in.close();
				}
				if(null != fo){
					fo.close();
				}
				if(null != out){
					out.close();
				}
			} catch (IOException e) {              
				e.printStackTrace();
			}
		}
		return result;
	}	
}
