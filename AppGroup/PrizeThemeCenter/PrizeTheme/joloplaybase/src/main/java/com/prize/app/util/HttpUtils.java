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

import android.content.Intent;

import com.prize.app.BaseApplication;
import com.prize.app.beans.HeadResultCallBack;
import com.prize.app.constants.Constants;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;

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
//		DownLoadDataBean dataBean = new DownLoadDataBean();
//		dataBean.downloadType = downloadType;
//		dataBean.packageName = packageName;
//		dataBean.timeDelta = 0;
//		ArrayList<DownLoadDataBean> datas = new ArrayList<DownLoadDataBean>();
//		datas.add(dataBean);
//		String json = new Gson().toJson(datas);
//
//		String url = Constants.GIS_URL + "/stat/upload";
//		RequestParams reqParams = new RequestParams(url);
//		reqParams.addBodyParameter("type", "download");
//		reqParams.addBodyParameter("datas", json);
//		JLog.e(TAG, "downloadType=" + downloadType + " ,packageName="
//				+ packageName);
//		XExtends.http().post(reqParams, new CommonCallback<String>() {
//
//			@Override
//			public void onSuccess(String result) {
//				JSONObject obj;
//				try {
//					obj = new JSONObject(result);
//					int code = obj.optInt("code");
//					// String msg = obj.optString("msg");
//					if (code == 0) {
//						String content = "code=" + code + "----packageName="
//								+ packageName + "--downloadType="
//								+ downloadType + "/r/n";
//						JLog.writeFileToSD(content);
//					} else {
//						DownLoadDataDAO.getInstance().insertApp(downloadType,
//								packageName, System.currentTimeMillis() + "");
//					}
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//
//			@Override
//			public void onError(Throwable ex, boolean isOnCallback) {
//				JLog.e(TAG,
//						"==========onErrorOne(Throwable ex, boolean isOnCallback)===========");
//				DownLoadDataDAO.getInstance().insertApp(downloadType,
//						packageName, System.currentTimeMillis() + "");
//			}
//
//			@Override
//			public void onCancelled(CancelledException cex) {
//				DownLoadDataDAO.getInstance().insertApp(downloadType,
//						packageName, System.currentTimeMillis() + "");
//			}
//
//			@Override
//			public void onFinished() {
//
//			}
//		});

	}

	public static void uploadDownAppInfo(String json) {
//		String url = Constants.GIS_URL + "/stat/upload";
//		RequestParams reqParams = new RequestParams(url);
//		reqParams.addBodyParameter("type", "download");
//		reqParams.addBodyParameter("datas", json);
//		JLog.e(TAG, "json=" + json.toString());
//		XExtends.http().post(reqParams, new CommonCallback<String>() {
//
//			@Override
//			public void onSuccess(String result) {
//				JSONObject obj;
//				try {
//					obj = new JSONObject(result);
//					int code = obj.optInt("code");
//					if (code == 0) {
//						DownLoadDataDAO.getInstance().deleteAll();
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//
//			}
//
//			@Override
//			public void onError(Throwable ex, boolean isOnCallback) {
//				JLog.e(TAG,
//						"==========onErrorTwo(Throwable ex, boolean isOnCallback)===========");
//			}
//
//			@Override
//			public void onCancelled(CancelledException cex) {
//
//			}
//
//			@Override
//			public void onFinished() {
//
//			}
//		});

	}

	public static void uploadCarClickDataInfo() {
//		if (ClientInfo.networkType == ClientInfo.NONET)
//			return;
//		ArrayList<CardClickDataBean> datas = CardClickDataDAO.getInstance()
//				.query();
//		if (datas != null && datas.size() > 0) {
//			String json = new Gson().toJson(datas);
//			String url = Constants.GIS_URL + "/stat/cardStat";
//			RequestParams reqParams = new RequestParams(url);
//			reqParams.addBodyParameter("datas", json);
//			JLog.e(TAG, "json=" + json.toString());
//			XExtends.http().post(reqParams, new CommonCallback<String>() {
//
//				@Override
//				public void onSuccess(String result) {
//					JSONObject obj;
//					try {
//						obj = new JSONObject(result);
//						int code = obj.optInt("code");
//						if (code == 0) {
//							JLog.e(TAG,
//									"==========onSuccess(String result)===========");
//							CardClickDataDAO.getInstance().deleteAll();
//						}
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//				}
//
//				@Override
//				public void onError(Throwable ex, boolean isOnCallback) {
//					JLog.e(TAG,
//							"==========onErrorTwo(Throwable ex, boolean isOnCallback)===========");
//				}
//
//				@Override
//				public void onCancelled(CancelledException cex) {
//
//				}
//
//				@Override
//				public void onFinished() {
//
//				}
//			});
//		}
	}

	public static void uploadSearchDataInfo(String searchWord,
			String packageName, String status) {
//		if (ClientInfo.networkType == ClientInfo.NONET)
//			return;
//		SearchBean bean = new SearchBean();
//		bean.searchWord = searchWord;
//		bean.packageName = packageName;
//		bean.status = status;
//		String json = new Gson().toJson(bean);
//		String url = Constants.GIS_URL + "/stat/searchStat";
//		RequestParams reqParams = new RequestParams(url);
//		reqParams.addBodyParameter("datas", json);
//		JLog.e(TAG, "json=" + json.toString());
//		XExtends.http().post(reqParams, new CommonCallback<String>() {
//
//			@Override
//			public void onSuccess(String result) {
//				JSONObject obj;
//				try {
//					obj = new JSONObject(result);
//					int code = obj.optInt("code");
//					if (code == 0) {
//						JLog.e(TAG,
//								"==========onSuccess(String result)===========");
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//
//			@Override
//			public void onError(Throwable ex, boolean isOnCallback) {
//				JLog.e(TAG,
//						"==========onErrorTwo(Throwable ex, boolean isOnCallback)===========");
//			}
//
//			@Override
//			public void onCancelled(CancelledException cex) {
//
//			}
//
//			@Override
//			public void onFinished() {
//
//			}
//		});
	}

	/**
	 * 获取服务器德推送设置信息
	 */
	public static void uploadPushTime() {
//		if (ClientInfo.networkType == ClientInfo.NONET)
//			return;
//		String url = Constants.GIS_URL + "/push/setting";
//		RequestParams reqParams = new RequestParams(url);
//		XExtends.http().post(reqParams, new CommonCallback<String>() {
//
//			@Override
//			public void onSuccess(String result) {
//				JSONObject obj;
//				try {
//					obj = new JSONObject(result);
//					int code = obj.optInt("code");
//					if (code == 0) {
//						JLog.e(TAG,
//								"==========uploadPushTime  onSuccess(String result)===========");
//						PushTimeBean pushTimeBean = new Gson().fromJson(
//								obj.optString("data"), PushTimeBean.class);
//						if (pushTimeBean != null) {
//							int pushRequestFrequency = pushTimeBean.settings.pushRequestFrequency;
//							int pushFrequency = pushTimeBean.settings.pushFrequency;
//							DataStoreUtils.saveLocalInfo(
//									DataStoreUtils.PUSHREQUESTFREQUENCY,
//									String.valueOf(pushRequestFrequency));
//							JLog.e(TAG, "pushTimeBean=" + pushTimeBean);
//							if (Boolean
//									.valueOf(pushTimeBean.settings.pushSwitch)
//									&& Boolean
//											.valueOf(pushTimeBean.settings.validPushTime)) {
//								if (!TextUtils.isEmpty(DataStoreUtils
//										.readLocalInfo(DataStoreUtils.PUSH_TIME))) {
//									long lastTime = Long.valueOf(DataStoreUtils
//											.readLocalInfo(DataStoreUtils.PUSH_TIME));
//									long currentTime = System
//											.currentTimeMillis();
//									// if ((currentTime - lastTime)
//									// / (60 * 1000) > pushFrequency) {
//									if ((currentTime - lastTime)
//											/ (60 * 60 * 1000) > pushFrequency) {
//										startService(6);
//									}
//								} else {
//									startService(6);
//								}
//							}
//						}
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//
//			@Override
//			public void onError(Throwable ex, boolean isOnCallback) {
//				JLog.e(TAG,
//						"==========uploadPushTime onErrorTwo(Throwable ex, boolean isOnCallback)===========");
//			}
//
//			@Override
//			public void onCancelled(CancelledException cex) {
//
//			}
//
//			@Override
//			public void onFinished() {
//
//			}
//		});
	}

	private static void startService(int optType) {
		Intent intent = new Intent(
				"com.prize.appcenter.service.PrizeAppCenterService");
		intent.setClassName(BaseApplication.curContext,
				"com.prize.appcenter.service.PrizeAppCenterService");
		intent.putExtra("optType", optType);
		BaseApplication.curContext.startService(intent);
	}

	public static String getUrl(Map<String, String> params, String url) {
		// 添加url参数
		if (params != null) {
			Iterator<String> it = params.keySet().iterator();
			StringBuffer sb = null;
			while (it.hasNext()) {
				String key = it.next();
				String value = params.get(key);
				if (sb == null) {
					sb = new StringBuffer();
					sb.append("?");
				} else {
					sb.append("&");
				}
				sb.append(key);
				sb.append("=");
				sb.append(value);
			}
			url += sb.toString();
		}
		return url;
	}

	public static void doHeadRequest(String url,
			final HeadResultCallBack callBack) {
//		StringRequest stringRequest = new StringRequest(Request.Method.HEAD,
//				url, new Response.Listener<String>() {
//					@Override
//					public void onResponse(String response) {
//
//					}
//
//					@Override
//					public void onResponseHeaders(Map<String, String> headers) {
//						JLog.i("hu", "onResponseHeaders=" + headers);
//						callBack.onResponseHeaders(headers);
//					}
//
//				}, new ErrorListener() {
//
//					@Override
//					public void onErrorResponse(VolleyError error) {
//						JLog.e("hu", "onErrorResponse(VolleyError error)=");
//						// TODO Auto-generated method stub
//					}
//				}) {
//
//		};
//		// 设置连接超时时间15秒
//		stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
//				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//		// requestQueue.add(stringRequest);
//		BaseApplication.addToRequestQueue(stringRequest, "head");
	}

	/**
	 * 从服务器获取pid
	 * 
	 * @param back
	 *            RequestCallBack
	 */
	public static void getPidFromServer(final RequestPIDCallBack back) {
		RequestParams reqParams = new RequestParams(Constants.PID_URL);
		reqParams.addBodyParameter("KOOBEE", "dido");
		XExtends.http().post(reqParams, new CommonCallback<String>() {

			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {

			}

			@Override
			public void onFinished() {

			}

			@Override
			public void onSuccess(String result) {
				JLog.i(TAG, "getPidFromServer-result=" + result);
				JSONObject obj;
				try {
					obj = new JSONObject(result);
					int code = obj.optInt("code");
					if (code == 00000) {
						JSONObject o2 = (JSONObject) obj.opt("data");
						String pid = o2.optString("pid");
						if (back != null) {
							back.requestOk(pid);
						}
					} else {
					}
				} catch (JSONException e) {
				}
			}

		});
	}

	/**
	 * 从服务器获取手机唯一标识，并保存到设置
	 * 
	 * @param pid
	 *            这个从服务器获取HttpUtils.getPidFromServer（）；
	 */
	public static void getUuidFromServer(String pid) {
		RequestParams reqParams = new RequestParams(Constants.UUID_URL);
		reqParams.addBodyParameter("pid", pid);
		String sign = Verification.getInstance().getSign(
				reqParams.getBodyParams());
		reqParams.addBodyParameter("sign", sign);
		XExtends.http().post(reqParams, new CommonCallback<String>() {

			@Override
			public void onCancelled(CancelledException arg0) {

			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {

			}

			@Override
			public void onFinished() {

			}

			@Override
			public void onSuccess(String result) {
				JLog.i(TAG, "getUuidFromServer-result=" + result);
				JSONObject obj;
				try {
					obj = new JSONObject(result);
					int code = obj.optInt("code");
					if (code == 00000) {// 得到pid
						JSONObject o2 = (JSONObject) obj.opt("data");
						String uuid = o2.optString("uuid");
						PreferencesUtils.saveKEY_TID(uuid);
					} else {
					}
				} catch (JSONException e) {
				}
			}

		});
	}

	/**
	 * 请求pid成功后回调
	 * 
	 * @author prize
	 *
	 */
	public interface RequestPIDCallBack {
		/****
		 * 请求pid成功后回调
		 * 
		 * @param pid
		 *            服务器获取的校验码
		 */
		void requestOk(String pid);

	};
}
