package com.pr.scuritycenter.aresengine;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.pr.scuritycenter.R;
import com.pr.scuritycenter.framework.BaseActivity;
import com.pr.scuritycenter.utils.DeviceUtils;

/**
 * 
 * @author wangzhong
 *
 */
public class InterceptActivity extends BaseActivity {
	
	private LinearLayout ll_intercept_home;
	
	private ListView lv_intercept_phone;
	private Button bt_intercept_status;

	/**
	 * Incoming call intercept form data.
	 */
	private InterceptIncomingCallDao mInterceptIncomingCallDao;
	private List<InterceptIncomingCallBean> mListInterceptIncomingCallBeans;
	private InterceptIncomingCallListAdapter mInterceptIncomingCallListAdapter;
	
	
	private RefreshDataUIBroadcastReceiver mRefreshDataUIBroadcastReceiver;
	
	@Override
	public void initInfo() {
		
	}

	@Override
	public void initView() {
		setContentView(R.layout.intercept_activity);
		
		ll_intercept_home = (LinearLayout) findViewById(R.id.ll_intercept_home);
		ll_intercept_home.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
		
		
		lv_intercept_phone = (ListView) findViewById(R.id.lv_intercept_phone);
		bt_intercept_status = (Button) findViewById(R.id.bt_intercept_status);
		bt_intercept_status.setOnClickListener(this);
		
		// Incoming call intercept form data.
		mInterceptIncomingCallDao = new InterceptIncomingCallDao(this);
		mListInterceptIncomingCallBeans = mInterceptIncomingCallDao.findAll();
		mInterceptIncomingCallListAdapter = new InterceptIncomingCallListAdapter(this.getLayoutInflater(), mListInterceptIncomingCallBeans);
		lv_intercept_phone.setAdapter(mInterceptIncomingCallListAdapter);
	}

	@Override
	protected void initTopbar() {
		super.initTopbar();
		
		setTitle(getResources().getString(R.string.intercept_title));
//		setAssistBG(R.drawable.optimizing_warning);
		bt_topbar_assist.setTag(false);
		bt_topbar_assist.setVisibility(View.GONE);
	}

	// test.
	/*@Override
	protected void actionAssist() {
		Intent i = new Intent(this, InterceptService.class);
		if ((boolean) bt_topbar_assist.getTag()) {
			Toast.makeText(InterceptActivity.this, "来电拦截已停止！", Toast.LENGTH_SHORT).show();
			bt_topbar_assist.setTag(false);
			stopService(i);
		} else {
			Toast.makeText(InterceptActivity.this, "来电拦截已开启！", Toast.LENGTH_SHORT).show();
			bt_topbar_assist.setTag(true);
			startService(i);
		}
	}*/

	@Override
	public void initData() {
		mRefreshDataUIBroadcastReceiver = new RefreshDataUIBroadcastReceiver();
		IntentFilter refreshIntentFilter = new IntentFilter();
		refreshIntentFilter.addAction(RefreshDataUIBroadcastReceiver.ACTION_INTERCEPT_REFRESH);
		InterceptActivity.this.registerReceiver(mRefreshDataUIBroadcastReceiver, refreshIntentFilter);
	}

	@Override
	public void finish() {
		super.finish();
		if (null != mRefreshDataUIBroadcastReceiver) {
			unregisterReceiver(mRefreshDataUIBroadcastReceiver);
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.bt_intercept_status:
			if (null != mListInterceptIncomingCallBeans && mListInterceptIncomingCallBeans.size() > 0) {
				mInterceptIncomingCallDao.deleteAll();
				mListInterceptIncomingCallBeans.clear();
				mInterceptIncomingCallListAdapter.notifyDataSetChanged();
			} else {
				
			}
			
			break;
		default:
			break;
		}
	}
	
	protected static final int MSG_NOTIFYDATASETCHANGED = 11;
	@SuppressLint("HandlerLeak")
	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NOTIFYDATASETCHANGED:
				mListInterceptIncomingCallBeans = mInterceptIncomingCallDao.findAll();
				mInterceptIncomingCallListAdapter = new InterceptIncomingCallListAdapter(
						InterceptActivity.this.getLayoutInflater(), mListInterceptIncomingCallBeans);
				lv_intercept_phone.setAdapter(mInterceptIncomingCallListAdapter);
				break;
				
			default:
				break;
			}
		}
	};

	public class RefreshDataUIBroadcastReceiver extends BroadcastReceiver {
		
		public final static String ACTION_INTERCEPT_REFRESH = "com.pr.securitycenter.ACTION_INTERCEPT_REFRESH";

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Log.v("JOHN", "RefreshDataUIBroadcastReceiver  onReceive()");
			myHandler.sendEmptyMessage(MSG_NOTIFYDATASETCHANGED);
		}
		
	}

}
