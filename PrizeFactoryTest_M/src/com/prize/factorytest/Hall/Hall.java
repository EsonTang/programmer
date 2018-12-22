package com.prize.factorytest.Hall;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import java.io.IOException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;

public class Hall extends Activity {
	private static final int MSG_CLOSE_LID = 0x1234;
	private static final int MSG_OPEN_LID = 0x1235;
	protected static final String TAG = "HallActivity";
	private TextView resultShow = null;
	protected String result = null;
	private static Button buttonPass;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CLOSE_LID:
				resultShow.setText("0");
				break;
			case MSG_OPEN_LID:
				resultShow.setText("1");
				break;
			}
			mHandler.postDelayed(HallTestRunnable, 200);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hall);
		resultShow = (TextView) findViewById(R.id.result_show);
		confirmButton();

	}

	public void confirmButton() {
		buttonPass = (Button) findViewById(R.id.passButton);
		buttonPass.setEnabled(false);
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		buttonPass.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		buttonFail.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_CANCELED);
				finish();

			}

		});
	}

	Runnable HallTestRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				CMDExecute cmdexe = new CMDExecute();
				String[] args = { "/system/bin/cat",
						"/sys/hall_state/hall_status" };
				result = cmdexe.run(args, "system/bin/");

			} catch (IOException e) {
				e.printStackTrace();
			}
			if (result.trim().equals("0")) {
				buttonPass.setEnabled(true);
				Message message = new Message();
				message.what = MSG_CLOSE_LID;
				mHandler.sendMessage(message);
			} else {
				Message message = new Message();
				message.what = MSG_OPEN_LID;
				mHandler.sendMessage(message);
			}
		}

	};

	public void onResume() {
		super.onResume();
		mHandler.post(HallTestRunnable);
	}

	public void onPause() {
		super.onPause();
		mHandler.removeMessages(MSG_CLOSE_LID);
		mHandler.removeMessages(MSG_OPEN_LID);
		mHandler.removeCallbacks(HallTestRunnable);
	}
}
