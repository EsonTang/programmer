/**/
package com.koobee.koobeecenter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.koobee.koobeecenter.base.BaseActivity;
import com.koobee.koobeecenter.utils.CTelephoneInfo;
import com.koobee.koobeecenter.utils.NetUtils;
import com.koobee.koobeecenter.utils.ToastUtils;
import com.koobee.koobeecenter.utils.WindowMangerUtils;
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
	//private TextView tipOnetextview;
	private TextView tipTwotextview;
    public 	static Activity feedbackRequestActivity;  
 	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			String message = (String) msg.obj;
			int id = msg.arg1;
			send_Btn.setClickable(true);
			if (message != null && message.length() > 0) {
				if(id==0){
					Intent intent = new Intent(FeedBackRequestActivity.this,
							ShowFeedbackResultActivity.class);
					intent.putExtra("resultmessage", message);
					intent.putExtra("qtype", qtype);
					
					startActivity(intent);
				}else if(id==-1){
					Toast.makeText(FeedBackRequestActivity.this, R.string.sys_busy,3000).show();
				}else if(id==40001){
					Toast.makeText(FeedBackRequestActivity.this, R.string.illegal_imei,3000).show();
					
				}else if(id==40002){
					Toast.makeText(FeedBackRequestActivity.this, R.string.illegal_operate_type,3000).show();
				}
				//ToastUtils.showOnceToast(getApplicationContext(), message);
				/*if (id == 0) {
					FeedBackRequestActivity.this.finish();
				}*/
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		Window window = getWindow();
//		window.requestFeature(Window.FEATURE_NO_TITLE);
//		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
//			window = getWindow();
//			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//			window.getDecorView().setSystemUiVisibility(
//					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//			// | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//			window.setStatusBarColor(getResources().getColor(
//					R.color.status_color));
//			// window.setNavigationBarColor(Color.TRANSPARENT);
//		}
//		setContentView(R.layout.activity_feedback_request_layout);
	//	getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
	//	WindowMangerUtils.initStateBar(getWindow(), this);
		
		
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
					R.color.feedback_status_color));
			// window.setNavigationBarColor(Color.TRANSPARENT);
		}
		
		
		
		setContentView(R.layout.activity_feedback_request_layout);
	//	WindowMangerUtils.changeStatus(getWindow());
		feedbackRequestActivity=this;
		init();
		findViewById();
		setListener();
		initStatusBar();
	}

	private void initStatusBar() {
		Window window = getWindow();
		window.setStatusBarColor(getResources().getColor(R.color.color_fafafa));

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		try {
			Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
			Field grayField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_GRAY");
			Object gray = grayField.get(statusBarManagerClazz);
			Class windowManagerLpClazz = lp.getClass();
			Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
			statusBarInverseField.set(lp,gray);
			getWindow().setAttributes(lp);
		} catch (Exception e) {
		}
	}
	
	@Override
	protected void init() {
		
		qtype = getIntent().getStringExtra("qtype");
		
		android.util.Log.d("andy", "----------init qtype A : "+qtype);
		
		/*if (qtype.equals("4"))   // bluetooth flag
		{
			qtype = "10";   // system flag 
		}else if (qtype.equals("7"))  // GPS flag 
		{
			qtype = "11";   // screen flag 
		}*/
		cuurentImei = getImei();
		snNUmber = Build.SERIAL;
	}

	@Override
	protected void findViewById() {
		left = findViewById(R.id.back_btn_problem);
		send_Btn = (TextView) findViewById(R.id.send_Btn);
		phone_email_Edtv = (EditText) findViewById(R.id.phone_email_Edtv);
		content_Edtv = (EditText) findViewById(R.id.content_Edtv);
	//	tipOnetextview=(TextView)findViewById(R.id.content_tip_one);
		tipTwotextview=(TextView)findViewById(R.id.contnet_tip_two);
		TextView PromblenView=(TextView)findViewById(R.id.title_text_problem);
		PromblenView.setText(getResources().getString(R.string.problemback));
		//suggestion
		if(qtype.equals("8")){
			PromblenView.setText(getResources().getString(R.string.suggestion));
			SpannableString s = new SpannableString(getResources().getString(R.string.pl_input_tip));
			content_Edtv.setHint(s);  
		}
		SetPhonenumber();
	}
	 private void SetPhonenumber(){
		 /*CTelephoneInfo telephonyInfo = CTelephoneInfo.getInstance(this);
			telephonyInfo.setCTelephoneInfo();*/
		String phonenumber=	getMsisdn(0);
		if(TextUtils.isEmpty(phonenumber)||phonenumber.equals("null")){
			phonenumber=	getMsisdn(1);
		}
		if(!(TextUtils.isEmpty(phonenumber))&&!phonenumber.equals("null")){
			 phone_email_Edtv.setText(phonenumber);
		}
		
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
					if(len>=1){
						send_Btn.setEnabled(true);
						send_Btn.setTextColor(getResources().getColor(R.color.color_3478f6));
					}else{
						send_Btn.setEnabled(false);
						send_Btn.setTextColor(getResources().getColor(R.color.color_c8c8c8));
					}
					/*if (len >= 5) {
						send_Btn.setEnabled(true);
					} else {
						send_Btn.setEnabled(false);
					}*/
				}
			}
		});

		send_Btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//String mcontent=content_Edtv.getText().toString().trim();
				String toastcontent=getResources().getString(R.string.toastcontent);
			/*if(TextUtils.isEmpty(content_Edtv.getText().toString().trim())||content_Edtv.getText().toString().trim().length()<5)	
			{
				//TextUtils.isEmpty(phone_email_Edtv.getText().toString().trim())
				tipOnetextview.setVisibility(View.VISIBLE);*/
				//Toast.makeText(FeedBackRequestActivity.this, toastcontent, 2000).show();
				/*if(phone_email_Edtv.getText().toString().trim().length()>0&&phone_email_Edtv.getText().toString().trim().length()<9){
					tipTwotextview.setVisibility(View.VISIBLE);
					
				}else{
					tipTwotextview.setVisibility(View.INVISIBLE);
				}*/
			//	return;
				
			/*}else{
				tipOnetextview.setVisibility(View.INVISIBLE);
				
			}*/
				if(content_Edtv.getText().toString().trim().equals("")){
					Toast.makeText(FeedBackRequestActivity.this, R.string.toastcontent,3000).show();
					return;
				}
			if(phone_email_Edtv.getText().toString().trim().length()>0&&phone_email_Edtv.getText().toString().trim().length()!=11){
				tipTwotextview.setVisibility(View.VISIBLE);
				return;
			}else if(phone_email_Edtv.getText().toString().trim().length()>0&&!isPhoneNumber(phone_email_Edtv.getText().toString().trim())){
				tipTwotextview.setVisibility(View.VISIBLE);
				tipTwotextview.setText(getResources().getString(R.string.numbertipthree));
				return;
			}else{
				tipTwotextview.setVisibility(View.GONE);
			}
				
				
				if (NetUtils.isNetConnected(FeedBackRequestActivity.this)) {
					send_Btn.setClickable(false);
					content = content_Edtv.getText().toString().trim();
					mobile = phone_email_Edtv.getText().toString();
					request();
				} else {
					
					Toast.makeText(FeedBackRequestActivity.this, R.string.netError_pl_check,3000).show();
				/*	NetUtils.openNet(FeedBackRequestActivity.this,
							getString(R.string.netInfo),
							getString(R.string.netError_pl_check));*/
				}
				;

			}

		});
	}
	
	
	private  boolean isPhoneNumber(String inputText) {  
        Pattern p = Pattern.compile("^((14[0-9])|(13[0-9])|(15[0-9])|(18[0-9])|(17[0-9]))\\d{8}$");  
        Matcher m = p.matcher(inputText);  
        return m.matches();  
    }  
	
	
	/*private void ShowAlertdiolog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(FeedBackRequestActivity.this);
        View view = View.inflate(this, R.layout.alertdilog, null);
        builder.setView(view);
       
        TextView title= (TextView) view
                .findViewById(R.id.title);//设置标题
       
        TextView btn_confirm=(TextView)view.findViewById(R.id.surebtg);//取消按钮
        
        //取消或确定按钮监听事件处理
        AlertDialog dialog = builder.create();
        dialog.show(); 
	}*/

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
				obj.put("model", Build.MODEL);
				obj.put("mver", Build.DISPLAY);
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
	
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		tipTwotextview.setVisibility(View.INVISIBLE);
	//	tipOnetextview.setVisibility(View.INVISIBLE);
		send_Btn.setEnabled(false);
		send_Btn.setTextColor(getResources().getColor(R.color.color_c8c8c8));
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
	
	
	private String getMsisdn(int slotId) {//slotId 0为卡1 ，1为卡2  
	    return getLine1NumberForSubscriber(getSubIdForSlotId(slotId));  
	}  
	
	
	private int getSubIdForSlotId(int slotId) {  
	    int[] subIds = getSubId(slotId);  
	    if (subIds == null || subIds.length < 1 || subIds[0] < 0) {  
	        return -1;  
	    }  
	   // MLog.d("getSubIdForSlotId = "+subIds[0]);  
	    return subIds[0];  
	}  
	
	private  int[] getSubId(int slotId) {  
	    Method declaredMethod;  
	    int[] subArr = null;  
	    SubscriptionManager   mSubscriptionManager = SubscriptionManager.from(FeedBackRequestActivity.this);
	    try {  
	        declaredMethod = Class.forName("android.telephony.SubscriptionManager").getDeclaredMethod("getSubId", new Class[]{Integer.TYPE});  
	        declaredMethod.setAccessible(true);  
	        subArr =  (int[]) declaredMethod.invoke(mSubscriptionManager,slotId);  
	    } catch (ClassNotFoundException e) {  
	        e.printStackTrace();  
	        declaredMethod = null;  
	    } catch (IllegalArgumentException e2) {  
	        e2.printStackTrace();  
	        declaredMethod = null;  
	    } catch (NoSuchMethodException e3) {  
	        e3.printStackTrace();  
	        declaredMethod = null;  
	    } catch (ClassCastException e4) {  
	        e4.printStackTrace();  
	        declaredMethod = null;  
	    } catch (IllegalAccessException e5){  
	        e5.printStackTrace();  
	        declaredMethod = null;  
	    }catch (InvocationTargetException e6){  
	        e6.printStackTrace();  
	        declaredMethod = null;  
	    }  
	    if(declaredMethod == null) {  
	        subArr = null;  
	    }  
	//    MLog.d("getSubId = "+subArr[0]);  
	    return subArr;  
	}  
	
	private String getLine1NumberForSubscriber(int subId){  
	    Method method;  
	    String status = null;  
		 TelephonyManager mTelephonyManager = (TelephonyManager)this.getSystemService(
			        Context.TELEPHONY_SERVICE);
	    try {  
	        method = mTelephonyManager.getClass().getMethod("getLine1NumberForSubscriber", int.class);  
	        method.setAccessible(true);  
	        status = String.valueOf(method.invoke(mTelephonyManager, subId));  
	    } catch (NoSuchMethodException e) {  
	        e.printStackTrace();  
	    } catch (IllegalAccessException e) {  
	        e.printStackTrace();  
	    } catch (IllegalArgumentException e) {  
	        e.printStackTrace();  
	    } catch (InvocationTargetException e) {  
	        e.printStackTrace();  
	    }  
	 //   MLog.d("getLine1NumberForSubscriber = "+status);  
	    return status;  
	} 
	

}
