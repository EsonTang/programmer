/**/
package com.koobee.koobeecenter;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.koobee.koobeecenter.base.BaseActivity;
import com.koobee.koobeecenter.utils.CTelephoneInfo;
import com.koobee.koobeecenter.utils.NetUtils;
import com.koobee.koobeecenter.utils.ToastUtils;
import com.koobee.koobeecenter02.R;

/**
 * 提交反馈意见界面
 * 
 * @author longbaoxiu
 *
 */
@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class FeedBackRequestActivity extends BaseActivity {
	private View left;
	private TextView send_Btn;
	private EditText content_Edtv;
	private EditText phone_email_Edtv;
	// private String uri =
	// "http://www.yiruyi.cn/koobee/imei.php?";//http://dt.koobeemobile.com/feedback.php
	private String uri = "http://dt.szprize.cn/feedback.php?";
	private String qtype;
	private SalesStatisTask mSalesStatisTask;
	// private String imei;
	private String imeiSIM1;// IMEI
	private String imeiSIM2;// IMEI
	private String mobile;
	private String content;

	private String cuurentImei;
	private String snNUmber;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			String message = (String) msg.obj;
			int id = msg.arg1;
			send_Btn.setClickable(true);
			if (message != null && message.length() > 0) {
				ToastUtils.showOnceToast(getApplicationContext(), message);
				if (id == 0) {
					FeedBackRequestActivity.this.finish();
				}
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			// | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.status_color));
			// window.setNavigationBarColor(Color.TRANSPARENT);
		}
		setContentView(R.layout.activity_feedback_request_layout);
		init();
		findViewById();
		setListener();
	}

	@Override
	protected void init() {
		qtype = getIntent().getStringExtra("qtype");
		cuurentImei = getImei();
		snNUmber = Build.SERIAL;
	}

	@Override
	protected void findViewById() {
		left = findViewById(R.id.left);
		send_Btn = (TextView) findViewById(R.id.send_Btn);
		phone_email_Edtv = (EditText) findViewById(R.id.phone_email_Edtv);
		content_Edtv = (EditText) findViewById(R.id.content_Edtv);

	}

	@Override
	protected void setListener() {
		left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FeedBackRequestActivity.this.finish();

			}
		});

		content_Edtv.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String content = content_Edtv.getText().toString();
				if (content != null) {
					int len = content.length();
					if (len >= 5) {
						send_Btn.setEnabled(true);
					} else {
						send_Btn.setEnabled(false);
					}
				}
			}
		});

		send_Btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (NetUtils.isNetConnected(FeedBackRequestActivity.this)) {
					send_Btn.setClickable(false);
					content = content_Edtv.getText().toString().trim();
					mobile = phone_email_Edtv.getText().toString();
					request();
				} else {
					NetUtils.openNet(FeedBackRequestActivity.this,
							getString(R.string.netInfo),
							getString(R.string.netError_pl_check));
				}
				;

			}

		});
	}

	/**
	 * 提交反馈
	 */
	private void request() {

		mSalesStatisTask = new SalesStatisTask();
		mSalesStatisTask.execute("");
	}

	private class SalesStatisTask extends AsyncTask<String, String, String> {
		// private boolean isRunning = false;

		public SalesStatisTask() {
			// isRunning = true;
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
				HttpPost httppost = new HttpPost(uri);
				httppost.addHeader("charset", HTTP.UTF_8);
				httppost.addHeader("KOOBEE", "dido");
				JSONObject obj = new JSONObject();
				// obj.put("type", "6");// 6为上传问题反馈信息
				if (!TextUtils.isEmpty(cuurentImei)) {
					obj.put("imei", cuurentImei);
				}
				if (!TextUtils.isEmpty(cuurentImei)) {
					obj.put("sn", snNUmber);
				}
				obj.put("qtype", qtype);
				obj.put("content", content);
				obj.put("mobile", mobile);
				httppost.setEntity(new StringEntity(obj.toString(), "utf-8"));
				HttpResponse response;
				response = httpclient.execute(httppost);
				// 检验状态码，如果成功接收数据
				int code = response.getStatusLine().getStatusCode();
				String msg = "";
				if (code == 200) {
					String rev = EntityUtils.toString(response.getEntity());// 返回json格式：
					obj = new JSONObject(rev);
					int id = obj.getInt("errcode");
					if (id == 0) {
						msg = getString(R.string.commit_success);
					} else if (id == 40001) {
						msg = getString(R.string.illegal_imei);
					} else if (id == -1) {
						msg = getString(R.string.sys_busy);
					} else if (id == 40002) {
						msg = getString(R.string.illegal_operate_type);
					}
					Message message = Message.obtain();
					message.arg1 = id;
					message.obj = msg;
					mHandler.sendMessage(message);
				}
			} catch (ClientProtocolException e) {
			} catch (IOException e) {
			} catch (Exception e) {
			} finally {
				if (httpclient != null) {
					httpclient.getConnectionManager().shutdown();
				}
			}
			return null;
		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);
			// isRunning = false;
		}

		// public void cancle() {
		// isRunning = false;
		// onCancelled();
		// }

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	private String getImei() {
		String curImei = "";
		CTelephoneInfo telephonyInfo = CTelephoneInfo.getInstance(this);
		telephonyInfo.setCTelephoneInfo();
		String imeiSIM1 = telephonyInfo.getImeiSIM1();
		String imeiSIM2 = telephonyInfo.getImeiSIM2();
		if (!TextUtils.isEmpty(imeiSIM1)) {

			curImei = imeiSIM1;
		}
		if (!TextUtils.isEmpty(imeiSIM2)) {
			if (!TextUtils.isEmpty(curImei)) {
				curImei = curImei + "," + imeiSIM2;
			}
		}
		return curImei;
	}

}
