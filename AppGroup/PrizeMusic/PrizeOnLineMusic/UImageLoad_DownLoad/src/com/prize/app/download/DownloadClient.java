package com.prize.app.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.safe.XXTEAUtil;
import com.xiami.sdk.utils.Encryptor;

public class DownloadClient {
	HttpURLConnection conn;
	String url;
	public static final int CONNECTION_TIME_OUT = 10 * 1000; // 10 secs
	public static final int READING_TIME_OUT = 50 * 1000; // 50 secs
	int song_id;
	private final String TAG = "DownloadClient";
	private ClientInfo mClientInfo = ClientInfo.getInstance();

	public DownloadClient(String dl_url, String pkg) {
		if (!TextUtils.isEmpty(dl_url)) {
			this.url = dl_url.replace(" ", "%20");
		}
		this.song_id = Integer.parseInt(pkg);
	}

	public InputStream getInputStream() throws IOException,
			SocketTimeoutException {
		return getInputStream(0);
	}

	/**
	 * 从网络读取数据
	 * 
	 * @param sizePos
	 *            ： 断点续传时的起始位置
	 * @return
	 * @throws MalformedURLException
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	public InputStream getInputStream(long sizePos)
			throws MalformedURLException, SocketTimeoutException, IOException {
//		JLog.i(TAG, "---->getInputStreamurl url：" + url);
		if (TextUtils.isEmpty(url))
			return null;
		URL aURL = new URL(Encryptor.decryptUrl(url));
		JLog.i(TAG, "---->getInputStreamurl解密后：" + aURL);

		conn = (HttpURLConnection) aURL.openConnection();
		conn.setConnectTimeout(CONNECTION_TIME_OUT);
		conn.setReadTimeout(READING_TIME_OUT);

		mClientInfo.setUserId(CommonUtils.queryUserId());
		mClientInfo.setClientStartTime(System.currentTimeMillis());
		mClientInfo.setNetStatus(ClientInfo.networkType);
		//
		String headParams = new Gson().toJson(mClientInfo);

		headParams = XXTEAUtil.getParamsEncypt(headParams);
		// JLog.i(TAG, "加密参数headParams--->" + headParams);
		if (!TextUtils.isEmpty(headParams)) {
			conn.setRequestProperty("params", headParams);
		}

		if (0 != sizePos) {
			conn.setRequestProperty("Range", "bytes=" + sizePos + "-");
			JLog.i(TAG, "---->断点续传sizePos：" + sizePos);
		}

		conn.connect();

		InputStream is = conn.getInputStream();
		// URL redirectUrl = conn.getURL();
		String header = conn.getHeaderField("Content-Type");
//		JLog.i(TAG, "---->header：" + header);
		// 无效的网络会返回流的大小，造成下载任务的错误
		if (Constants.QES_UNACCEPT_CONTENT_TYPE.contains(header)) {
			conn.disconnect();
			return null;
		}
		return is;
	}

	public int getContentLength() {
		if (conn != null) {
			return conn.getContentLength();
		}
		return 0;
	}

	// public long getFileSize() throws IOException {
	// URL aURL = new URL(url);
	// conn = (HttpURLConnection) aURL.openConnection();
	// conn.setConnectTimeout(CONNECTION_TIME_OUT);
	// conn.setReadTimeout(READING_TIME_OUT);
	// long size = conn.getContentLength();
	// return size;
	// }

	public void close() {
		disconnect();
		conn = null;
	}

	public void disconnect() {
		if (conn != null)
			conn.disconnect();
	}

}
