package com.prize.app.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.app.constants.RequestMethods;
import com.prize.app.excepiton.URLInvalidException;
import com.prize.app.lyric.LyricDownloadManager;
import com.prize.app.lyric.LyricLoadHelper;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.SDKUtil;
import com.prize.app.util.ToastUtils;
import com.prize.app.xiami.RequestManager;
import com.prize.onlinemusibean.SongDetailInfo;
import com.xiami.sdk.XiamiSDK;

public class Download implements Runnable {
	/** 下载缓冲区 */
	private static final int BUFFER_LEN = 1024;
	/** 更新数据库的频率 */
	private static final int UPDATE_SQL_NUM = 100;
	private static final String TAG = "Download";

	private File downloadTmpFile; // 下载文件备份
	private String downloadFilePath;
	private String downloadUrl;
	private int song_id;
	private DownloadClient mHttpClient;

	/*** 当前下载的位置 */
	private int downloadPosition;

	/** * 游戏的APK大小 */
	private int downloadFileSize;

	private boolean isRunning = false;

	private int downloadResult = 0;

	private DownloadState backListener;
	/** 下载状态 */
	private int downloadState;
	private SongDetailInfo loadGame;
	private XiamiSDK mXiamiSDK;
	private RequestManager requestManager;

	public Download(SongDetailInfo loadGame, DownloadState listener) {
		this.loadGame = loadGame;
		this.downloadUrl = this.loadGame.listen_file;
		backListener = listener;
		song_id = loadGame.song_id;
		downloadState = DownloadState.STATE_DOWNLOAD_WAIT;
		mXiamiSDK = new XiamiSDK(BaseApplication.curContext, SDKUtil.KEY,
				SDKUtil.SECRET);
		requestManager = RequestManager.getInstance();
	}

	/**
	 * 状态置成等待下载，并通知UI，下载准备中
	 */
	public void readyDownload() {
		notifyDownloadState(DownloadState.STATE_DOWNLOAD_WAIT);
	}

	public long getDownloadPosition() {
		return downloadPosition;
	}

	private void notifyDownloadState(int state) {
		backListener.onDownloadState(state, song_id, downloadResult);
		downloadState = state;
	}

	/**
	 * 停止当前任务
	 * 
	 * @param result
	 *            ：停止当前任务的原因
	 */
	public void stopDownloadByResult(int result) {
		isRunning = false;
		// 只有是未知错误的时候，设置成错误下载，其他情况只是暂停。错误下载，会删除原数据重新下载
		if (DownloadState.ERROR_CODE_UNKOWN == result) {
			notifyDownloadState(DownloadState.STATE_DOWNLOAD_ERROR);
		} else {
			notifyDownloadState(DownloadState.STATE_DOWNLOAD_PAUSE);
		}
		downloadResult = result;
	}

	@Override
	public void run() {
		if (DownloadState.STATE_DOWNLOAD_WAIT != downloadState) {
			return;
		}
		if (isRunning) {
			return;
		}
		try {
			android.os.Process.setThreadPriority((int) Thread.currentThread()
					.getId(), android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		} catch (IllegalArgumentException

		e) {
		} catch (SecurityException

		e) {

		}

		if(!requestSongDetail(song_id))
			return ;

		isRunning = true;
		notifyDownloadState(DownloadState.STATE_DOWNLOAD_START_LOADING);
		// 通知下载开始
		mHttpClient = new DownloadClient(downloadUrl, song_id + "");

		try {
			// 创建临时文件*.prize
			downloadFilePath = FileUtils.getDownloadTmpFilePath(DownloadHelper
					.getTempFileName(this.loadGame));
			downloadTmpFile = new File(downloadFilePath); // 下载的临时文件
			if (downloadTmpFile.exists()) {
				// 如果文件已经存在,断点续传
				downloadPosition = (int) downloadTmpFile.length();
			} else {
				File parentFile = downloadTmpFile.getParentFile();
				if (null != parentFile && !parentFile.exists()) {
					parentFile.mkdirs();
				}
				// File.createTempFile(null, null);
				downloadTmpFile.createNewFile();
			}
			// 开始下载
			download(downloadPosition);
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			stopDownloadByResult(DownloadState.ERROR_CODE_TIME_OUT);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			stopDownloadByResult(DownloadState.ERROR_CODE_HTTP);
		} catch (URLInvalidException e) {
			e.printStackTrace();
			stopDownloadByResult(DownloadState.ERROR_CODE_URL_ERROR);
		} catch (IOException e) {
			e.printStackTrace();
			stopDownloadByResult(DownloadState.ERROR_CODE_IO);
		} catch (Exception e) {
			e.printStackTrace();
			stopDownloadByResult(DownloadState.ERROR_CODE_UNKOWN);
		} finally {
			if (mHttpClient != null) {
				mHttpClient.close();
				mHttpClient = null;
			}
		}
	}

	/**
	 * 检查SD是否有足够空间
	 * 
	 * @param loadSize
	 * @return
	 */
	private boolean checkSDSpaceIsEnough(long loadSize) {
		if (FileUtils.getSDAvailaleSize() > (loadSize * 2)) {
			return true;
		}
		return false;
	}

	/**
	 * 断点续传
	 * 
	 * @param serverPos
	 * @throws SocketTimeoutException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private void download(final int serverPos) throws SocketTimeoutException,
			MalformedURLException, IOException {
		InputStream inputStream = null;
		RandomAccessFile randomAccessFile = null;

		try {
			inputStream = mHttpClient.getInputStream(serverPos);
			if (inputStream == null) {
				stopDownloadByResult(DownloadState.ERROR_CODE_HTTP);
				return;
			}
		} catch (Exception e) {
			throw new URLInvalidException("下载地址异常: url:\"" + downloadUrl + "\"");
		}

		try {
			randomAccessFile = new RandomAccessFile(downloadTmpFile, "rw");
			int size = mHttpClient.getContentLength();
//			JLog.i(TAG, "---->服务器的返回流大小：" + size);
			if (size > 0) {
				downloadFileSize = size;
			}

			if (!checkSDSpaceIsEnough(downloadFileSize)) {
				// 空间不足
				stopDownloadByResult(DownloadState.ERROR_CODE_SD_NOSAPCE);
				return;
			}

			if (size > 0) {
				downloadFileSize = downloadFileSize + serverPos; // 文件是实际大小
			}

			if (downloadPosition >= downloadFileSize) {
				notifyDownloadState(DownloadState.STATE_DOWNLOAD_SUCESS);
				return;
			}

			byte[] buf = new byte[BUFFER_LEN];// 从服务端读取的byte流
												// //缓存
			int len;// 从服务端读取的byte长度
			randomAccessFile.seek(serverPos);
			randomAccessFile.setLength(serverPos);

			int nowDownModNum = 0;
			JLog.e(TAG, "开始读取");
			long startTime = System.currentTimeMillis();
			int downloadSize = 0;
			while (isRunning && (-1 != (len = inputStream.read(buf)))) {
				// 2015-12-04
				if (!downloadTmpFile.exists()) {
					notifyDownloadState(DownloadState.STATE_DOWNLOAD_PAUSE);
					if (randomAccessFile != null) {
						randomAccessFile.close();
					}
					if (inputStream != null) {
						inputStream.close();
					}
					return;
				}
				randomAccessFile.write(buf, 0, len);
				downloadPosition += len;
				downloadSize += len;
				if (nowDownModNum == UPDATE_SQL_NUM) {
					nowDownModNum = 0;
					long curTime = System.currentTimeMillis();
					int usedTime = (int) ((curTime - startTime) / 1000);

					if (usedTime == 0)
						usedTime = 1;

					int downloadSpeed = (downloadSize / usedTime) / 1024;
					if (backListener != null) {
						backListener.updateDownloadProgress(song_id,
								downloadFileSize, downloadPosition,
								downloadSpeed);
					}
				}
				nowDownModNum++;
			}
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (randomAccessFile != null) {
					randomAccessFile.close();
				}
			} catch (Exception ex) {
			} finally {
				try {
					if (inputStream != null)
						inputStream.close();
				} catch (Exception exp) {
				}
			}
		}
		if (isRunning) {
			if (downloadPosition >= downloadFileSize) {
				JLog.i(TAG, "downloadPosition=" + downloadPosition);
				JLog.i(TAG, "downloadFileSize=" + downloadFileSize);
				// download success
				backListener.updateDownloadProgress(song_id, downloadFileSize,
						downloadPosition, 0);
				notifyDownloadState(DownloadState.STATE_DOWNLOAD_SUCESS);
				JLog.e(TAG, "下载完成通知");
			}
		}
	}

	/**
	 * 
	 * 请求歌曲详情
	 * @param song_id
	 * @return boolean true：成功返回 false：请求不到数据
	 */
	private boolean requestSongDetail(int song_id) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.clear();
		params.put("song_id", song_id);
		params.put("quality", "h");
		params.put("lyric_type", 2);// 指定歌词类型, 1为text,2为lrc,3为trc,4为翻译歌词
		try {
			String res = mXiamiSDK.xiamiSDKRequest(RequestMethods.SONG_DETAIL,
					params);
			JSONObject obi = new JSONObject(res);
			if (obi.getInt("state") == 0) {
				String result = obi.getString("data");
				if (result != null) {
					SongDetailInfo info = requestManager.getGson().fromJson(
							result, SongDetailInfo.class);
//					// 前后的下载地址不一样的话
//					if (!TextUtils.isEmpty(info.listen_file)
//							&&!TextUtils.isEmpty(downloadUrl)
//							&& !info.listen_file.equals(downloadUrl)) {
//						DownloadHelper.deleteTmpDownloadFile(info);
//					}
					downloadUrl=info.listen_file;
					downLoadXiamiLyricAndSaveFile(info.lyric, info.song_name, info.singers);
				}
				
				return true;
			} else {
				String msg = obi.getString("message");
				if (!TextUtils.isEmpty(msg)) {
					ToastUtils.showToast(msg);

				}
				notifyDownloadState(DownloadState.ERROR_CODE_HTTP);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JLog.i("requestSongDetail", "egetMessage="+e.getMessage());
			notifyDownloadState(DownloadState.ERROR_CODE_HTTP);
			return false;
		}

	}
	
	/**
	 * 
	 * 下载歌词并保存到本地 
	 * @param url 歌词下载的地址
	 * @return String 保存路径
	 */
	private void downLoadXiamiLyricAndSaveFile(String url,String songName,String singer){
		if(LyricLoadHelper.isLrcFileExists(songName, singer)){
			return;
		}
		if(TextUtils.isEmpty(url)){
			return;
		}
		BufferedReader br = null;
		StringBuilder content = null;
		String temp = null;
		JLog.i(TAG, "歌词的真实下载地址:" + url);
		URL mUrl = null;
		try {
			mUrl = new URL(url);
		} catch (MalformedURLException e2) {
			e2.printStackTrace();
		}
		
		// 获取歌词文本，存在字符串类中
		try {
			// 建立网络连接
			br = new BufferedReader(new InputStreamReader(mUrl.openStream(),"utf-8"));
			if (br != null) {
				content = new StringBuilder();
				// 逐行获取歌词文本
				while ((temp = br.readLine()) != null) {
					content.append(temp);
				}
				br.close();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (content != null) {
			// 检查保存的目录是否已经创建
			String folderPath = Constants.LYRIC_SAVE_PATH;
			File savefolder = new File(folderPath);
			if (!savefolder.exists()) {
				savefolder.mkdirs();
			}
			String savePath = folderPath +File.separator+ LyricLoadHelper.getFileName(songName, singer);
			LyricDownloadManager.saveLyric(content.toString(), savePath);
		}
	}
}
