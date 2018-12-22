package com.android.prize.salesstatis;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.android.prize.salesstatis.util.PhoneStute;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemProperties;
import android.util.Log;

public class SalesStatisService extends Service {
	private static final String TAG = "PrizeSalesStatis";
	private SalesStatisTask mSalesStatisTask;

	private ClientInfo clientInfo;

	private final String URI_PRIZE_DT = "http://dt.szprize.cn/mbinfo.php";
	private final String URI_ODM_DT = "http://odmdt.szprize.cn/odminfo.php";
	private String uri = URI_PRIZE_DT;

	@Override
	public void onCreate() {
		Log.e(TAG, "[SalesStatisService]-- onCreate()");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		clientInfo = ClientInfo.getInstance(getApplicationContext());
		PhoneStute phoneStute = (PhoneStute)intent.getParcelableExtra("addr");
		clientInfo.setLatitude(phoneStute.latitude);
		clientInfo.setLongitude(phoneStute.longitude);
		clientInfo.setPosition(phoneStute.position);
		Log.e(TAG, "---SalesStatisService-- onStartCommand ---> clientInfo = " + clientInfo.toString());
		mSalesStatisTask = getSalesStatisTask();
		mSalesStatisTask.execute("");
		return super.onStartCommand(intent, flags, startId);
	}

	private class SalesStatisTask extends AsyncTask<String, String, String> {
		private boolean isRunning = false;

		public SalesStatisTask() {
			isRunning = true;
		}

		@Override
		protected String doInBackground(String... params) {
			HttpClient httpclient = null;
			// TODO Auto-generated method stub
			try {
				HttpParams httpParams = new BasicHttpParams();
				httpParams.setParameter("charset", HTTP.UTF_8);
				HttpConnectionParams.setConnectionTimeout(httpParams, 8 * 1000);
				HttpConnectionParams.setSoTimeout(httpParams, 8 * 1000);
				httpclient = new DefaultHttpClient(httpParams);
				if (SystemProperties.get("ro.prize_customer").equals("odm")) {
					uri = URI_ODM_DT;
				}
				HttpPost httppost = new HttpPost(uri);
				httppost.addHeader("charset", HTTP.UTF_8);
				httppost.addHeader("KOOBEE", "dido");

				JSONObject obj = new JSONObject();
				String json = clientInfo.getJson();
				Log.v(TAG, json);
				StringEntity se = new StringEntity(json, HTTP.UTF_8);
				httppost.setEntity(se);
				HttpResponse response;
				response = httpclient.execute(httppost);
				// 检验状态码，如果成功接收数据
				int code = response.getStatusLine().getStatusCode();
				Log.v(TAG, "--------PRIZE_SALES_STATIS---code-->" + code);
				if (code == 200) {
					String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
																			// {"id":
																			// "27JpL~j4vsL0LX00E00005","version":
																			// "abc"}
					obj = new JSONObject(rev);
					int id = obj.getInt("errcode");
					if (id == 0) {
						Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_SALES_STATIS_NET, 0);
						stopSelf();
						Log.v(TAG, "--------PRIZE_SALES_STATIS---succusseful---");
					} else if (id == 40001) {
						Log.v(TAG, "--------PRIZE_SALES_STATIS---fail---");
					} else {
						Log.v(TAG, "--------PRIZE_SALES_STATIS---id-->" + id);
					}
				}
			} catch (ClientProtocolException e) {
				Log.v(TAG, "--------PRIZE_SALES_STATIS---ClientProtocolException-->");
			} catch (IOException e) {
				Log.v(TAG, "--------PRIZE_SALES_STATIS---IOException->");
			} catch (Exception e) {
				Log.v(TAG, "--------PRIZE_SALES_STATIS---Exception>");
			} finally {
				if (httpclient != null) {
					httpclient.getConnectionManager().shutdown();
				}
			}
			return null;
		}

		@Override
		protected void onPreExecute() {

			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String result) {

			// TODO Auto-generated method stub
			super.onPostExecute(result);
			isRunning = false;
		}

		public void cancle() {
			isRunning = false;
			onCancelled();
		}

	}

	// 初始化SalesStatisTask
	public SalesStatisTask getSalesStatisTask() {
		if (mSalesStatisTask != null && mSalesStatisTask.isRunning) {
			mSalesStatisTask.cancle();
			mSalesStatisTask = null;
		}
		return new SalesStatisTask();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
