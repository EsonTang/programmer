package com.prize.prizeappoutad.activity;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.prize.prizeappoutad.R;
import com.prize.prizeappoutad.constants.Constants;
import com.prize.prizeappoutad.manager.XExtends;
import com.prize.prizeappoutad.utils.JLog;
import com.prize.prizeappoutad.utils.PreferencesUtils;

public class MainActivity extends Activity {

	protected static final String TAG = "huang-MainActivity";
	Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		XExtends.Ext.init(getApplication());
		XExtends.Ext.setDebug(false);
		setContentView(R.layout.activity_main);
		getTargetPackageName();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				String string = PreferencesUtils.getString(
						getApplicationContext(), Constants.PACKAGENAMES, null);
				JLog.i(TAG, "     string:" + string);
				Toast.makeText(MainActivity.this, string, 0);

			}
		}, 3000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void getTargetPackageName() {

		String mFilterUrl = null;
		mFilterUrl = Constants.AD_FILTER_URL;

		RequestParams params = new RequestParams(mFilterUrl);
		JLog.i(TAG, "getTargetPackageName-params");
		XExtends.http(this).post(params, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				JLog.i(TAG, "getTargetPackageName-onSuccess");
				try {
					JSONObject obj = new JSONObject(result);
					if (obj.getInt("code") == 0) {
						PreferencesUtils.putString(getApplicationContext(),
								Constants.PACKAGENAMES, result);
					}
				} catch (JSONException e) {
					JLog.i(TAG, "filterPackageName-JSONException");
					e.printStackTrace();

				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				JLog.i(TAG, "filterPackageName-onError");
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
