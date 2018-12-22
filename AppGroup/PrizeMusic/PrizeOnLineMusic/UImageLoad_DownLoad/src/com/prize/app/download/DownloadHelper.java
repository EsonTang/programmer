/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
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

package com.prize.app.download;

import java.io.File;
import java.util.ArrayList;

import android.app.DownloadManager;
import android.content.Context;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.app.util.FileUtils;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 
 * 获取下载文件名称
 * 
 * @param songDetailInfo
 * @param downloadFormat
 * @return
 * @return String
 * @see
 */
public class DownloadHelper {
	/**
	 * 
	 * 获取下载文件名称（包含后缀）
	 * 
	 * @param songDetailInfo
	 * @return String
	 */
	public static String getFileName(SongDetailInfo songDetailInfo) {
		if(songDetailInfo==null){
			return "";
		}
		String pattern = "%s - %s - %s";

		pattern += ".mp3";
		return String.format(pattern, songDetailInfo.song_name,
				songDetailInfo.singers,songDetailInfo.album_name);
	}

	/**
	 * 
	 * 获取临时下载文件名称（包含后缀）
	 * 
	 * @param songDetailInfo
	 * @return String
	 */
	public static String getTempFileName(SongDetailInfo songDetailInfo) {
		String pattern = "%s - %s - %s";

		pattern += ".tmp";
		return String.format(pattern, songDetailInfo.song_name,
				songDetailInfo.singers,songDetailInfo.album_name);
	}

//	public static String getRelativePath(SongDetailInfo songDetailInfo) {
//		return String.format("/%s/%s", songDetailInfo.singers,
//				songDetailInfo.song_name,songDetailInfo.album_name);
//	}
//
//	public static String getAbsolutePath(SongDetailInfo songDetailInfo,
//			String destination) {
//		return destination + getRelativePath(songDetailInfo);
//	}

	/**
	 * 
	 * 根据名称删除临时文件
	 * 
	 * @param songDetailInfo
	 * @return void
	 */
	public static void deleteTmpDownloadFile(SongDetailInfo songDetailInfo) {
		DownloadTask.deleteTmpDownloadFile(getTempFileName(songDetailInfo));
	}

	/**
	 * 
	 * 批量删除临时文件(批量删除下任务)
	 * 
	 * @param songDetailInfo
	 * @return void
	 */
	public static void deleteBatchTmpFile(ArrayList<SongDetailInfo> infos) {
		for (SongDetailInfo info : infos) {
			deleteTmpDownloadFile(info);
			deleteDownloadedFile(info);
		}
	}

	/**
	 * 
	 * 根据名称删除下载完成的文件(同时删除数据库信息)
	 * 
	 * @param songDetailInfo
	 * @return boolean false:不存在或者删除失败；true：删除成功
	 */
	public static boolean deleteDownloadedFile(SongDetailInfo songDetailInfo) {
		if (songDetailInfo == null
				|| TextUtils.isEmpty(songDetailInfo.song_name)
				|| TextUtils.isEmpty(songDetailInfo.singers)) {
			return false;
		}
		String downloadFile = FileUtils
				.getDownMusicFilePath(getFileName(songDetailInfo));
		File file = new File(downloadFile);
		BaseApplication.curContext.getContentResolver().delete(
				MediaStore.Audio.Media.getContentUri("external"), "_DATA=?",
				new String[] { downloadFile });
		if (file.exists()) {
			return file.delete();
		}
		return false;
	}

	/**
	 * 判断音乐文件是否已经下载
	 * 
	 * @param songDetailInfo
	 * @return boolean
	 */
	public static boolean isFileExists(SongDetailInfo songDetailInfo) {
		String downloadFile = FileUtils
				.getDownMusicFilePath(getFileName(songDetailInfo));
		return new File(downloadFile).exists();
	}

}
