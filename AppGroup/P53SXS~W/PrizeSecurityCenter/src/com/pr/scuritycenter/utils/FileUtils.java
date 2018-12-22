/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：this is about files' operate utils
 *当前版本：V1.0
 *作	者：Bianxinhao
 *完成日期：2015-4-2
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.pr.scuritycenter.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class FileUtils {

	/**
	 * 
	 * function：the function is deleting Directory
	 * 
	 * @param
	 * @return
	 * @see
	 */
	public static void daleteDirectory(File file) {
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
				daleteDirectory(childFiles[i]);
			}
			file.delete();
		}
	}

	/**
	 * 
	 * Recursive search files
	 * 
	 * @param baseDirName
	 *            ：the path which is to find targetFifleName ：the fifleName
	 * @return
	 * @see
	 */
	public static void findFiles(String baseDirName, String targetFileName,
			List fileList) {
		File baseDir = new File(baseDirName); // creat a File object
		if (!baseDir.exists() || !baseDir.isDirectory()) { // judge the
															// Directory isExist
			System.out.println("Fifle can't find：" + baseDirName
					+ "not a Directory！");
		}
		String tempName = null;
		File tempFile;
		File[] files = baseDir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				tempFile = files[i];
				if (tempFile.isDirectory()) {
					findFiles(tempFile.getAbsolutePath(), targetFileName,
							fileList);
				} else if (tempFile.isFile()) {
					tempName = tempFile.getName();
					if (wildcardMatch(targetFileName, tempName)) {
						fileList.add(tempFile.getAbsoluteFile());
					}
				}
			}
		}

	}

	/**
	 * wildcard character match
	 * 
	 * @param pattern
	 *            wildcard character mode
	 * @param str
	 *            the string which is to wildcard
	 * @return succed return true else return false
	 */
	private static boolean wildcardMatch(String pattern, String str) {
		int patternLength = pattern.length();
		int strLength = str.length();
		int strIndex = 0;
		char ch;
		for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {
			ch = pattern.charAt(patternIndex);
			if (ch == '*') {
				while (strIndex < strLength) {
					if (wildcardMatch(pattern.substring(patternIndex + 1),
							str.substring(strIndex))) {
						return true;
					}
					strIndex++;
				}
			} else if (ch == '?') {
				strIndex++;
				if (strIndex > strLength) {
					return false;
				}
			} else {
				if ((strIndex >= strLength) || (ch != str.charAt(strIndex))) {
					return false;
				}
				strIndex++;
			}
		}
		return (strIndex == strLength);
	}

	/**
	 * 
	 * funcation：get the all Directorys
	 * 
	 * @param File
	 *            path
	 * @return the File path list
	 * @see
	 */
	public static List getAllDirectotys(File root) {
		List list = new ArrayList();
		File[] dirs = root.listFiles();
		if (dirs != null) {
			for (int i = 0; i < list.size(); i++) {
				if (dirs[i].isDirectory()) {
					System.out.println("name" + dirs[i].getPath());
					list.add(dirs[i]);
				}
			}
		}

		return list;
	}

	/**
	 * get the empty directorys' numbers
	 * @param 
	 * @return
	 * @see
	 */

	public static int getAllNullFile(List<File> list) {
		int num = 0;
		for (int i = 0; i < list.size(); i++) {
			File temp = list.get(i);
			if (temp.isDirectory() && temp.listFiles().length <= 0) {
				num++;
			}
		}

		return num;
	}

	/**
	 * delete the empty directory
	 * @param
	 * @return
	 * @see
	 */
	public static void removeNullFile(List<File> list) {
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				File temp = list.get(i);
				if (temp.isDirectory() && temp.listFiles().length <= 0) {
					temp.delete();
				}
			}
		}
	}
	
	/**
	 * 按行读取文件
	 */

	public static void readTxtFile(String filePath){
		  try {
              String encoding="GBK";
              File file=new File(filePath);
              if(file.isFile() && file.exists()){ //判断文件是否存在
                  InputStreamReader read = new InputStreamReader(
                  new FileInputStream(file),encoding);//考虑到编码格式
                  BufferedReader bufferedReader = new BufferedReader(read);
                  String lineTxt = null;
                  while((lineTxt = bufferedReader.readLine()) != null){
                     // System.out.println(lineTxt);
                	  Log.v("bian", "lineTxt="+lineTxt);
                  }
                  read.close();
      }else{
          System.out.println("找不到指定的文件");
      }
      } catch (Exception e) {
          System.out.println("读取文件内容出错");
          e.printStackTrace();
      }
	}
	
	public static final long ONE_KB = 1024L;
	public static final long ONE_MB = ONE_KB * 1024L;
	public static final long ONE_GB = ONE_MB * 1024L;
	public static final long ONE_TB = ONE_GB * 1024L;
	public static String transformShortType(long bytes, boolean isShortType) {
		long currenUnit = ONE_KB;
		int unitLevel = 0;
		boolean isNegative = false;
		if (bytes < 0) {
			isNegative = true;
			bytes = (-1) * bytes;
		}

		while ((bytes / currenUnit) > 0) {
			unitLevel++;
			currenUnit *= ONE_KB;
		}

		String result_text = null;
		double currenResult = 0;
//		int skipLevel = 1000;//如果大于等于1000就用更大一级单位显示
		switch (unitLevel) {
		case 0:
			result_text = "0K";
			break;
		case 1:
			currenResult = bytes / ONE_KB;
//			if (currenResult < skipLevel) {
				result_text = getFloatValue(currenResult, 1) + "K";
//			} else {
//				result_text = getFloatValue(bytes * 1.0 / ONE_MB) + "M";
//			}
			break;
		case 2:
			currenResult = bytes * 1.0 / ONE_MB;
//			if (currenResult < skipLevel) {
				result_text = getFloatValue(currenResult, 1) + "M";
//			} else {
//				result_text = getFloatValue(bytes * 1.0 / ONE_GB) + "G";
//			}
			break;
		case 3:
			currenResult = bytes * 1.0 / ONE_GB;
//			if (currenResult < skipLevel) {
				result_text = getFloatValue(currenResult, 2) + "G";
//			} else {
//				result_text = getFloatValue(bytes * 1.0 / ONE_TB) + "T";
//			}
			break;
		case 4:
			result_text = getFloatValue(bytes * 1.0 / ONE_TB, 2) + "T";
		}

		if (isNegative) {
			result_text = "-" + result_text;
		}
		return result_text;
	}
	
	private static String getFloatValue(double oldValue, int decimalCount){
		if (oldValue >= 1000) {//大于四位整数  不出现小数部分
			decimalCount = 0;
		}else if(oldValue >= 100){
			decimalCount = 1;
		}
		
		BigDecimal b = new BigDecimal(oldValue);
		try {
			if (decimalCount <= 0) {
				oldValue = b.setScale(0, BigDecimal.ROUND_DOWN).floatValue(); //ROUND_DOWN 表示舍弃末尾
			}else{
				oldValue = b.setScale(decimalCount, BigDecimal.ROUND_DOWN).floatValue(); //ROUND_DOWN 表示舍弃末尾,decimalCount 位小数保留
			}
		} catch (ArithmeticException e) {
			Log.w("Unit.getFloatValue", e.getMessage());
		}
		String decimalStr = "";
		if (decimalCount <= 0) {
			decimalStr = "#";
		} else {
			for (int i = 0; i < decimalCount; i++) {
				decimalStr += "#";
			}
		}
		// decimalCount 位小数保留
		DecimalFormat format = new DecimalFormat("###." + decimalStr);
		return  format.format(oldValue);
	}
}
