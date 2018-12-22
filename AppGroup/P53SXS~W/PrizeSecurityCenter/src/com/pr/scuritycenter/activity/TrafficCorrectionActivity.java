package com.pr.scuritycenter.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tmsdk.bg.module.network.CodeName;
import tmsdk.bg.module.network.ITrafficCorrectionListener;
import tmsdk.common.ErrorCode;
import tmsdk.common.IDualPhoneInfoFetcher;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.pr.scuritycenter.R;
import com.pr.scuritycenter.SecurityCenterApplication;
import com.pr.scuritycenter.utils.StateBarUtils;

public class TrafficCorrectionActivity extends Activity implements
		OnClickListener {
	public static final String TAG = "TrafficCorrectionUser";
	String mQueryCode1 = "";
	String mQueryPort1 = "";
	String mTrafficMsg1 = "";

	String mQueryCode2 = "";
	String mQueryPort2 = "";
	String mTrafficMsg2 = "";

	TextView mTVSim1Detail, mTVSim2Detail;

	@ViewInject(R.id.tv_setting_location)
	private TextView tv_setting_location;

	@ViewInject(R.id.sim1_status)
	private CheckBox sim1_status;

	@ViewInject(R.id.sim1_status)
	private CheckBox sim2_status;

	@ViewInject(R.id.return_to_main)
	private ImageButton return_to_main;

	@ViewInject(R.id.today_traffic)
	private TextView today_traffic;

	@ViewInject(R.id.mothy_traffic)
	private TextView mothy_traffic;

	@ViewInject(R.id.mothy_traffic_remain)
	private TextView mothy_traffic_remain;

	@ViewInject(R.id.traffic_setting)
	private RelativeLayout traffic_setting;

	@ViewInject(R.id.traffic_details)
	private RelativeLayout traffic_details;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StateBarUtils.initSateBar(this);
		setContentView(R.layout.traffic_correction);

		sim1imsi = ((SecurityCenterApplication) getApplication()).getSim1imsi();
		sim2imsi = ((SecurityCenterApplication) getApplication()).getSim2imsi();

		mTVSim1Detail = (TextView) findViewById(R.id.sim1_detail);
		mTVSim2Detail = (TextView) findViewById(R.id.sim2_detail);

		ViewUtils.inject(this);
		initData();

		traffic_setting.setOnClickListener(this);
		traffic_details.setOnClickListener(this);
		return_to_main.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TrafficCorrectionActivity.this.finish();
			}
		});

		tv_setting_location.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				if (sim1imsi == null && sim2imsi == null) {
					AlertDialog.Builder builder = new Builder(
							TrafficCorrectionActivity.this);
					builder.setTitle(R.string.no_sim_reminder);
					WindowManager windowManager = getWindowManager();
					Display display = windowManager.getDefaultDisplay();
					AlertDialog dialog = builder.create();
					Window window = dialog.getWindow();
					LayoutParams params = window.getAttributes();
					window.setGravity(Gravity.LEFT|Gravity.TOP);
					params.x = display.getHeight()/2;
					params.y = display.getWidth()/2;
					window.setAttributes(params);
					dialog.show();
				} else if (sim1imsi == null) {
					LocationSetDlg locationSetDlg = new LocationSetDlg(
							TrafficCorrectionActivity.this,
							IDualPhoneInfoFetcher.SECOND_SIM_INDEX);
					Log.v("bian", "卡2");
					locationSetDlg.startDlg();
				} else if (sim1imsi == sim2imsi) {
					LocationSetDlg locationSetDlg = new LocationSetDlg(
							TrafficCorrectionActivity.this,
							IDualPhoneInfoFetcher.FIRST_SIM_INDEX);
					Log.v("bian", "卡1");
					locationSetDlg.startDlg();
				} else {// two SIM
					if (sim1_status.isChecked()) {
						LocationSetDlg locationSetDlg = new LocationSetDlg(
								TrafficCorrectionActivity.this,
								IDualPhoneInfoFetcher.FIRST_SIM_INDEX);
						Log.v("bian", "卡1");
						locationSetDlg.startDlg();
					} else {
						LocationSetDlg locationSetDlg = new LocationSetDlg(
								TrafficCorrectionActivity.this,
								IDualPhoneInfoFetcher.SECOND_SIM_INDEX);
						Log.v("bian", "卡2");
						locationSetDlg.startDlg();
					}
				}
			}
		});

		TrafficCorrectionWrapper.getInstance().init(getApplicationContext());
		TrafficCorrectionWrapper.getInstance().setTrafficCorrectionListener(
				new ITrafficCorrectionListener() {

					@Override
					public void onNeedSmsCorrection(int simIndex,
							String queryCode, String queryPort) {
						android.util.Log.v(TAG,
								"onNeedSmsCorrection--simIndex:[" + simIndex
										+ "]--queryCode:[" + queryCode
										+ "]queryPort:[" + queryPort + "]");
						String strState = "";
						strState += "卡：[" + (simIndex + 1) + "]需要发查询短信校正\n";

						final int simIndexF = simIndex;
						if (IDualPhoneInfoFetcher.FIRST_SIM_INDEX == simIndexF) {
							mQueryCode1 = queryCode;
							mQueryPort1 = queryPort;

							mTVSim1Detail.setText(mTVSim1Detail.getText()
									+ strState);
							sendMessage(mQueryCode1, mQueryPort1);
							mTrafficMsg1 = getSmsInPhone().split("#")[0];

						} else if (IDualPhoneInfoFetcher.SECOND_SIM_INDEX == simIndexF) {
							mQueryCode2 = queryCode;
							mQueryPort2 = queryPort;
							sendMessage(mQueryCode2, mQueryPort2);
							mTrafficMsg1 = getSmsInPhone().split("#")[0];
							mTVSim2Detail.setText(mTVSim2Detail.getText()
									+ strState);
						}

						new Thread(new Runnable() {
							@Override
							public void run() {
								String strDetail = "";

								if (IDualPhoneInfoFetcher.FIRST_SIM_INDEX == simIndexF) {
									TrafficCorrectionWrapper.getInstance()
											.analysisSMS(simIndexF,
													mQueryCode1, mQueryPort1,
													mTrafficMsg1);
									strDetail += "[" + mQueryCode1 + "]["
											+ mQueryPort1 + "]\n["
											+ mTrafficMsg1 + "]\n";

								} else if (IDualPhoneInfoFetcher.SECOND_SIM_INDEX == simIndexF) {
									TrafficCorrectionWrapper.getInstance()
											.analysisSMS(simIndexF,
													mQueryCode2, mQueryPort2,
													mTrafficMsg2);
									strDetail += "[" + mQueryCode2 + "]["
											+ mQueryPort2 + "]\n["
											+ mTrafficMsg2 + "]\n";
								}
								Message msg = uiHandler.obtainMessage(
										MSG_NEED_SEND_MSG, simIndexF, 0);
								msg.obj = strDetail;
								msg.sendToTarget();
							}

						}).start();
					}

					@Override
					public void onTrafficInfoNotify(int simIndex,
							int trafficClass, int subClass, int kBytes) {
						Message msg = uiHandler.obtainMessage(
								MSG_TRAFfICT_NOTIFY, simIndex, 0);
						msg.obj = logTrafficInfo(simIndex, trafficClass,
								subClass, kBytes);
						msg.sendToTarget();
						android.util.Log.v(TAG, "onTrafficNotify-"
								+ (String) msg.obj);
					}

					@Override
					public void onError(int simIndex, int errorCode) {

						String strState = "状态信息：";
						strState += "卡：[" + simIndex + "]校正出错:[" + errorCode
								+ "]";

						if (IDualPhoneInfoFetcher.FIRST_SIM_INDEX == simIndex) {
							mTVSim1Detail.setText(strState);
						} else if (IDualPhoneInfoFetcher.SECOND_SIM_INDEX == simIndex) {
							mTVSim2Detail.setText(strState);
						}
						android.util.Log.v(TAG, "onError--simIndex:["
								+ simIndex + "]errorCode:[" + errorCode + "]");
					}
				});

		// 卡槽1设置
		Button bt_sim0_setting = (Button) findViewById(R.id.sim1_setting);
		bt_sim0_setting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LocationSetDlg locationSetDlg = new LocationSetDlg(
						TrafficCorrectionActivity.this,
						IDualPhoneInfoFetcher.FIRST_SIM_INDEX);
				locationSetDlg.startDlg();
			}
		});
		// 卡槽1校正
		Button bt_sim0_correction = (Button) findViewById(R.id.sim1_correction);
		bt_sim0_correction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mTVSim1Detail.setText("卡1校正...\n");

				int retCode = TrafficCorrectionWrapper.getInstance()
						.startCorrection(IDualPhoneInfoFetcher.FIRST_SIM_INDEX);
				if (retCode != ErrorCode.ERR_NONE) {
					mTVSim1Detail.setText("卡1校正出错终止\n");
				}
			}
		});

		// 卡槽2设置
		Button bt_sim1_setting = (Button) findViewById(R.id.sim2_setting);
		bt_sim1_setting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LocationSetDlg locationSetDlg = new LocationSetDlg(
						TrafficCorrectionActivity.this,
						IDualPhoneInfoFetcher.SECOND_SIM_INDEX);
				locationSetDlg.startDlg();
			}
		});
		// 卡槽2校正
		Button bt_sim1_correction = (Button) findViewById(R.id.sim2_correction);
		bt_sim1_correction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mTVSim2Detail.setText("卡2校正...\n");
				int retCode = TrafficCorrectionWrapper
						.getInstance()
						.startCorrection(IDualPhoneInfoFetcher.SECOND_SIM_INDEX);
				if (retCode != ErrorCode.ERR_NONE) {
					mTVSim2Detail.setText("卡2校正出错终止\n");
				}
			}
		});

		String logTemp = "";
		int simIndex = IDualPhoneInfoFetcher.FIRST_SIM_INDEX;
		int retTrafficInfo[] = TrafficCorrectionWrapper.getInstance()
				.getTrafficInfo(simIndex);
		logTemp += "常规-剩余[" + retTrafficInfo[0] + "]已用[" + retTrafficInfo[1]
				+ "]总量[" + retTrafficInfo[2] + "]\n";
		logTemp += "闲时-剩余[" + retTrafficInfo[3] + "]已用[" + retTrafficInfo[4]
				+ "]总量[" + retTrafficInfo[5] + "]\n";
		logTemp += "4G-剩余[" + retTrafficInfo[6] + "]已用[" + retTrafficInfo[7]
				+ "]总量[" + retTrafficInfo[8] + "]\n";
		mTVSim1Detail.setText(logTemp);

		logTemp = "";
		simIndex = IDualPhoneInfoFetcher.SECOND_SIM_INDEX;
		retTrafficInfo = TrafficCorrectionWrapper.getInstance().getTrafficInfo(
				simIndex);
		logTemp += "常规-剩余[" + retTrafficInfo[0] + "]已用[" + retTrafficInfo[1]
				+ "]总量[" + retTrafficInfo[2] + "]\n";
		logTemp += "闲时-剩余[" + retTrafficInfo[3] + "]已用[" + retTrafficInfo[4]
				+ "]总量[" + retTrafficInfo[5] + "]\n";
		logTemp += "4G-剩余[" + retTrafficInfo[6] + "]已用[" + retTrafficInfo[7]
				+ "]总量[" + retTrafficInfo[8] + "]\n";
		mTVSim2Detail.setText(logTemp);

		android.util.Log.v(TAG, "onTrafficNotify-" + logTemp);

	}

	private void initData() {
		if (sim1imsi == null && sim2imsi == null) {
			Log.v("bian", "1");
			sim1_status.setChecked(false);
			sim1_status.setEnabled(false);
			sim2_status.setChecked(false);
			sim2_status.setEnabled(false);
		} else if (sim1imsi == null && sim2imsi != null) {
			Log.v("bian", "2");
			sim1_status.setChecked(false);
			sim1_status.setEnabled(false);
			sim2_status.setChecked(true);
			sim2_status.setEnabled(false);
		} else if (sim1imsi != null && sim1imsi.equals(sim2imsi)) {
			Log.v("bian", "3");
			sim1_status.setChecked(true);
			sim1_status.setEnabled(false);
			sim2_status.setChecked(false);
			sim2_status.setEnabled(false);

		} else {
			Log.v("bian", "4");
			sim1_status.setChecked(true);
			sim1_status
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								sim1_status.setChecked(true);
								sim2_status.setChecked(false);
							} else {
								sim1_status.setChecked(true);
								sim2_status.setChecked(false);
							}
						}
					});
			sim2_status
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								sim1_status.setChecked(true);
								sim2_status.setChecked(false);
							} else {
								sim1_status.setChecked(true);
								sim2_status.setChecked(false);
							}
						}
					});
		}
	}

	protected void onDestroy() {
		TrafficCorrectionWrapper.getInstance().setTrafficCorrectionListener(
				null);
		super.onDestroy();
	}

	void showArrayList(ArrayList<CodeName> list) {
		StringBuilder sb = new StringBuilder();
		for (CodeName cn : list) {
			sb.append("(" + cn.mCode + "," + cn.mName + ")");
		}
		showLogToastNote(sb.toString());
	}

	void showToastNote(String msg) {
		Toast a = Toast.makeText(TrafficCorrectionActivity.this, msg,
				Toast.LENGTH_SHORT);
		a.show();
	}

	void showLogToastNote(String msg) {
		Toast a = Toast.makeText(TrafficCorrectionActivity.this, msg,
				Toast.LENGTH_LONG);
		a.show();
	}

	String logTrafficInfo(int simIndex, int trafficClass, int subClass,
			int kBytes) {
		String logTemp = "";

		if (trafficClass == ITrafficCorrectionListener.TC_TrafficCommon) {
			logTemp += "--常规";
		} else if (trafficClass == ITrafficCorrectionListener.TC_TrafficFree) {
			logTemp += "--闲时";
		} else if (trafficClass == ITrafficCorrectionListener.TC_Traffic4G) {
			logTemp += "--4G";
		}

		if (subClass == ITrafficCorrectionListener.TSC_LeftKByte) {
			logTemp = logTemp + "-剩余:[" + kBytes + "]";
		} else if (subClass == ITrafficCorrectionListener.TSC_UsedKBytes) {
			logTemp = logTemp + "-已用:[" + kBytes + "]";
		} else if (subClass == ITrafficCorrectionListener.TSC_TotalKBytes) {
			logTemp = logTemp + "-总额:[" + kBytes + "]";
		}
		logTemp += "\n";
		return logTemp;
	}

	/**
	 * 校正成功提示
	 */
	private final int MSG_TRAFfICT_NOTIFY = 0x1a;

	/***
	 * 需要发送短信
	 */
	private final int MSG_NEED_SEND_MSG = 0x1b;
	Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MSG_TRAFfICT_NOTIFY:
				String logTemp = (String) msg.obj;
				if (IDualPhoneInfoFetcher.FIRST_SIM_INDEX == msg.arg1) {
					mTVSim1Detail.setText(mTVSim1Detail.getText() + logTemp);
				} else if (IDualPhoneInfoFetcher.SECOND_SIM_INDEX == msg.arg1) {
					mTVSim2Detail.setText(mTVSim2Detail.getText() + logTemp);
				}
				break;
			case MSG_NEED_SEND_MSG:
				if (IDualPhoneInfoFetcher.FIRST_SIM_INDEX == msg.arg1) {
					mTVSim1Detail.setText(mTVSim1Detail.getText()
							+ (String) msg.obj);
				} else if (IDualPhoneInfoFetcher.SECOND_SIM_INDEX == msg.arg1) {
					mTVSim2Detail.setText(mTVSim2Detail.getText()
							+ (String) msg.obj);
				}
				break;
			}
		}
	};
	private int smsStatus;
	private String sim1imsi;
	private String sim2imsi;
	private Intent intent;

	/**
	 * get the latest sms
	 * 
	 * @return
	 */
	public String getSmsInPhone() {

		final String SMS_URI_ALL = "content://sms/";
		StringBuilder smsBuilder = new StringBuilder();

		try {
			Uri uri = Uri.parse(SMS_URI_ALL);
			String[] projection = new String[] { "_id", "address", "person",
					"body", "date", "type" };
			Cursor cur = getContentResolver().query(uri, projection, null,
					null, "date desc");
			if (cur.moveToFirst()) {

				int index_Address = cur.getColumnIndex("address");
				int index_Body = cur.getColumnIndex("body");
				int index_Date = cur.getColumnIndex("date");

				String strbody = cur.getString(index_Body);
				long longDate = cur.getLong(index_Date);
				String strAddress = cur.getString(index_Address);

				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd hh:mm:ss");
				Date d = new Date(longDate);
				String strDate = dateFormat.format(d);

				smsBuilder.append(strbody + "#");
				smsBuilder.append(strDate + "#");
				smsBuilder.append(strAddress);
				if (!cur.isClosed()) {
					cur.close();
					cur = null;
				}
			} else {
				smsBuilder.append("no result!");
			}

		} catch (SQLiteException ex) {
			Log.d("SQLiteException in getSmsInPhone", ex.getMessage());
		}

		return smsBuilder.toString();
	}

	// sendMessage to
	private void sendMessage(String mQueryCode, String mQueryPort) {
		SmsManager smsManager = android.telephony.SmsManager.getDefault();
		PendingIntent sentIntent = PendingIntent.getBroadcast(
				TrafficCorrectionActivity.this, 0, new Intent(), 0);
		smsManager.sendTextMessage(mQueryPort, null, mQueryCode, sentIntent,
				null);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.traffic_setting:
			intent = new Intent(this, TrafficCorrectionSetting.class);
			startActivity(intent);
			break;
		case R.id.traffic_details:
			break;
		}
		
	}

}
