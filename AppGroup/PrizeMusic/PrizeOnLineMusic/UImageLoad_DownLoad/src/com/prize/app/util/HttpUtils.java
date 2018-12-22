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

package com.prize.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CancelledException;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.prize.app.constants.Constants;
import com.prize.app.database.beans.DownLoadDataBean;
import com.prize.app.database.dao.DownLoadDataDAO;
import com.prize.custmerxutils.XExtends;

public class HttpUtils {
	private static final String TAG = "HttpUtils";

	/**
	 * Post Request
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String doPost(String pkg, String msg) throws Exception {
		StringBuffer b = new StringBuffer("packageName=").append(pkg)
				.append("&msg=").append(msg);
		String parameterData = b.toString();

		URL localURL = new URL(Constants.GIS_URL + "/appinfo/downloadfault?");
		URLConnection connection = localURL.openConnection();
		HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
		httpURLConnection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		httpURLConnection.setRequestProperty("Content-Length",
				String.valueOf(parameterData.length()));

		OutputStream outputStream = null;
		OutputStreamWriter outputStreamWriter = null;
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader reader = null;
		StringBuffer resultBuffer = new StringBuffer();
		String tempLine = null;

		try {
			outputStream = httpURLConnection.getOutputStream();
			outputStreamWriter = new OutputStreamWriter(outputStream);

			outputStreamWriter.write(parameterData.toString());
			outputStreamWriter.flush();

			if (httpURLConnection.getResponseCode() >= 300) {
				throw new Exception(
						"HTTP Request is not success, Response code is "
								+ httpURLConnection.getResponseCode());
			}

			inputStream = httpURLConnection.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream);
			reader = new BufferedReader(inputStreamReader);

			while ((tempLine = reader.readLine()) != null) {
				resultBuffer.append(tempLine);
			}

		} finally {

			if (outputStreamWriter != null) {
				outputStreamWriter.close();
			}

			if (outputStream != null) {
				outputStream.close();
			}

			if (reader != null) {
				reader.close();
			}

			if (inputStreamReader != null) {
				inputStreamReader.close();
			}

			if (inputStream != null) {
				inputStream.close();
			}

		}

		return resultBuffer.toString();
	}

	public static void uploadDownAppInfo(final String downloadType,
			final String packageName) {
		DownLoadDataBean dataBean = new DownLoadDataBean();
		dataBean.downloadType = downloadType;
		dataBean.packageName = packageName;
		dataBean.timeDelta = 0;
		ArrayList<DownLoadDataBean> datas = new ArrayList<DownLoadDataBean>();
		datas.add(dataBean);
		String json = new Gson().toJson(datas);

		String url = Constants.GIS_URL + "/stat/upload";
		RequestParams reqParams = new RequestParams(url);
		reqParams.addBodyParameter("type", "download");
		reqParams.addBodyParameter("datas", json);
		JLog.e(TAG, "downloadType=" + downloadType + " ,packageName="
				+ packageName);
		XExtends.http().post(reqParams, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				JSONObject obj;
				try {
					obj = new JSONObject(result);
					int code = obj.optInt("code");
					// String msg = obj.optString("msg");
					if (code == 0) {
						String content = "--downloadType=" + downloadType
								+ "----packageName=" + packageName + "-->"
								+ "\r\n";
						JLog.writeFileToSD(content);
					} else {
						DownLoadDataDAO.getInstance().insertApp(downloadType,
								packageName, System.currentTimeMillis() + "");
						String content = "-uploadfaile-code != 0downloadType="
								+ downloadType + "----packageName="
								+ packageName + "-->" + "\r\n";
						JLog.writeFileToSD(content);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				String content = "-uploadfaile-onError-downloadType="
						+ downloadType + "----packageName=" + packageName
						+ "-->" + "\r\n";
				JLog.writeFileToSD(content);
				DownLoadDataDAO.getInstance().insertApp(downloadType,
						packageName, System.currentTimeMillis() + "");
			}

			@Override
			public void onCancelled(CancelledException cex) {
				DownLoadDataDAO.getInstance().insertApp(downloadType,
						packageName, System.currentTimeMillis() + "");
			}

			@Override
			public void onFinished() {

			}
		});

	}

	public static void uploadDownAppInfo(String json) {
		String url = Constants.GIS_URL + "/stat/upload";
		RequestParams reqParams = new RequestParams(url);
		reqParams.addBodyParameter("type", "download");
		reqParams.addBodyParameter("datas", json);
		JLog.e(TAG, "json=" + json.toString());
		XExtends.http().post(reqParams, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				JSONObject obj;
				try {
					obj = new JSONObject(result);
					int code = obj.optInt("code");
					if (code == 0) {
						DownLoadDataDAO.getInstance().deleteAll();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				JLog.e(TAG,
						"==========onErrorTwo(Throwable ex, boolean isOnCallback)===========");
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});

	}
}
