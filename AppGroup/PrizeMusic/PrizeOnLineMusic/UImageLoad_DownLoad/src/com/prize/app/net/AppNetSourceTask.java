package com.prize.app.net;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.net.req.BaseResp;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.HttpClientUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.safe.XXTEAUtil;

/**
 ** 网络请求（实现了Runnable接口，在run方法中请求且回调请求返回结果）
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class AppNetSourceTask<T extends Map<String, String>, Q extends BaseResp>
		implements Runnable {
	private OnReslutListener listener = null;
	private String url = null;
	// private T req = null;
	private T params = null;
	private String TAG = "AppNetSourceTask";

	private static final int MAX_TIMEOUT_TIME = 30 * 1000;
	private static final String GZIP_ENCODING = "gzip";
	private ClientInfo mClientInfo = ClientInfo.getInstance();

	protected String mUserId;

	/**
	 * @param url
	 *            请求url
	 * @param listener
	 *            结果返回监听
	 * @param req
	 *            请求参数
	 */
	public AppNetSourceTask(String url, OnReslutListener listener, T req,
			String id) {
		this.url = url;
		this.listener = listener;
		this.params = req;
		mUserId = id;
	}

	/**
	 * post请求
	 * 
	 * @return byte[] 请求返回内容
	 */
	@SuppressWarnings("deprecation")
	private String postMethod() {
		JLog.i(TAG, " post请求ur=" + url);
		HttpPost httpRequest = new HttpPost(url);

		mClientInfo.setUserId(CommonUtils.queryUserId());
		mClientInfo.setClientStartTime(System.currentTimeMillis());
		mClientInfo.setNetStatus(ClientInfo.networkType);
		//
		String headParams = new Gson().toJson(mClientInfo);

		headParams = XXTEAUtil.getParamsEncypt(headParams);
		if (!TextUtils.isEmpty(headParams)) {
			httpRequest.setHeader("params", headParams);
		}

		// httpRequest.setHeader("androidVersion", mClientInfo.androidVersion);
		// httpRequest.setHeader("model", mClientInfo.model);
		// httpRequest.setHeader("language", mClientInfo.language);
		// httpRequest.setHeader("operator", mClientInfo.operator);
		// httpRequest.setHeader("brand", mClientInfo.brand);
		// httpRequest.setHeader("netStatus", ClientInfo.networkType + "");
		// httpRequest.setHeader("appVersion", mClientInfo.appVersion + "");
		// httpRequest.setHeader("appVersionCode", mClientInfo.appVersionCode);
		// httpRequest.setHeader("userId", mUserId);
		// if (!TextUtils.isEmpty(mClientInfo.imei1)) {
		// httpRequest.setHeader("imei", mClientInfo.imei1);
		// } else {
		// if (!TextUtils.isEmpty(mClientInfo.imei2)) {
		// httpRequest.setHeader("imei", mClientInfo.imei2);
		// } else {
		// String uid = DataStoreUtils.readLocalInfo(DataStoreUtils.UUID);
		// if (TextUtils.isEmpty(uid)) {
		// UUID uuid = UUID.randomUUID();
		// DataStoreUtils.saveLocalInfo(DataStoreUtils.UUID,
		// uuid.toString());
		// httpRequest.setHeader("imei", uuid.toString());
		//
		// } else {
		// httpRequest.setHeader("imei", uid);
		// }
		// }
		// }
		// httpRequest.setHeader("clientStartTime", System.currentTimeMillis()
		// + "");
		// 封装请求参数
		List<NameValuePair> pair = new ArrayList<NameValuePair>();
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				JLog.i(TAG, "-请求参数key=" + entry.getKey() + "--请求参数value="
						+ entry.getValue() + "");
				pair.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));

			}

		}
		// 把请求参数变成请求体部分
		UrlEncodedFormEntity uee = null;
		try {
			uee = new UrlEncodedFormEntity(pair, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();

		}
		httpRequest.setEntity(uee);
		HttpClient httpClient = HttpClientUtils.getHttpClient();

		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, MAX_TIMEOUT_TIME);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				MAX_TIMEOUT_TIME);
		HttpResponse httpResponse = null;
		int status = -1;
		// GZIPInputStream inPutStream = null;
		ByteArrayInputStream gzipByteStream = null;
		try {
			httpResponse = httpClient.execute(httpRequest);

			status = httpResponse.getStatusLine().getStatusCode();
			JLog.i(TAG, "--status=" + status);
			if (status == 200) {
				/** 判断是否是GZIP **/
				boolean isGzipEncoding = false;
				// httpResponse.ge
				if (httpResponse.getLastHeader("switch") != null) {
					String isNeedBack = httpResponse.getLastHeader("switch")
							.getValue();

					if ("true".equals(isNeedBack)) {
						String uuid = httpResponse.getLastHeader("uuid")
								.getValue();
						readContentFromGet(uuid);
					}

				}
				// 读取数据
				HttpEntity entity = httpResponse.getEntity();
				Header header = entity.getContentEncoding();
				if (null != header) {
					String contentEncoding = header.getValue();
					if ((null != contentEncoding)
							&& contentEncoding.contains(GZIP_ENCODING)) {
						isGzipEncoding = true;
					}
				}
				return EntityUtils.toString(entity, "utf-8");
			} else {
				httpRequest.abort();
			}
		} catch (Exception e) {
			JLog.i(TAG, e.toString());
		} finally {

			if (null != gzipByteStream) {
				try {
					gzipByteStream.close();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	@Override
	public void run() {
		String result = postMethod();
		JLog.i(TAG, " 请求响应返回-->" + result);
		if (null == result) {
			listener.onFailed();
		} else {
			listener.onSucess(result);
		}
	}

	/**
	 * 网络请求 结果监听
	 * 
	 * @author prize
	 * 
	 */
	public interface OnReslutListener {

		/**
		 * 成功 原始流
		 * 
		 * @param bytes
		 */
		void onSucess(String result);

		// void onSucess(byte[] bytes);

		/**
		 * 失败 请求后返回数据为null
		 */
		void onFailed();
	}

	private void readContentFromGet(String uuid) throws IOException {
		// 拼凑get请求的URL字串，使用URLEncoder.encode对特殊和不可见字符进行编码
		StringBuilder getURL = new StringBuilder(Constants.GIS_URL)
				.append("/stat/post?").append("uuid=").append(uuid)
				.append("&clientEndTime=")
				.append(System.currentTimeMillis() + "");
		URL getUrl = new URL(getURL.toString());
		// 根据拼凑的URL，打开连接，URL.openConnection函数会根据URL的类型，
		// 返回不同的URLConnection子类的对象，这里URL是一个http，因此实际返回的是HttpURLConnection
		HttpURLConnection connection = (HttpURLConnection) getUrl
				.openConnection();
		// 进行连接，但是实际上get request要在下一句的connection.getInputStream()函数中才会真正发到
		// 服务器
		connection.connect();
		// 取得输入流，并使用Reader读取
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String lines;
		while ((lines = reader.readLine()) != null) {
			JLog.i(TAG, lines);
		}
		reader.close();
		// 断开连接
		connection.disconnect();
	}
}