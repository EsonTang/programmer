package com.prize.uploadappinfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CancelledException;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import com.prize.uploadappinfo.constants.Constant;
import com.prize.uploadappinfo.http.HttpUtils;
import com.prize.uploadappinfo.http.HttpUtils.RequestPIDCallBack;
import com.prize.uploadappinfo.http.XExtends;
import com.prize.uploadappinfo.service.UploadService;
import com.prize.uploadappinfo.utils.JLog;
import com.prize.uploadappinfo.utils.PollingUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainlayout);
	}

	public void startService(View View) {
//		getPidFromServer();
		PollingUtils.startService(this,0);
	}
	public void startload(View View) {
//		getPidFromServer();
		PollingUtils.startService(this,3);
	}
	public void request(View View) {
		HttpUtils.getPidFromServer(new RequestPIDCallBack() {
			
			@Override
			public void requestOk(String pid) {
				HttpUtils.getUuidFromServer(pid);
				
			}
		});
	}

	/**
	 * 
	 */
	public static void getPidFromServer() {
		String url = Constant.PID_URL;
		RequestParams reqParams = new RequestParams(url);
		reqParams.addBodyParameter("KOOBEE", "dido");
		XExtends.http().post(reqParams, new CommonCallback<String>() {

			@Override
			public void onCancelled(CancelledException arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {

			}

			@Override
			public void onFinished() {

			}

			@Override
			public void onSuccess(String result) {
				JSONObject obj;
				try {
					obj = new JSONObject(result);
					int code = obj.optInt("code");
					if (code == 00000) {
						JSONObject o2 = (JSONObject) obj.opt("data");
						String pid = o2.optString("pid");
						HttpUtils.getUuidFromServer(pid);

					} else {
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
				}
			}

		});
	}
}
