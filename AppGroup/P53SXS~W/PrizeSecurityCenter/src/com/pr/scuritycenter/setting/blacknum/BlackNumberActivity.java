package com.pr.scuritycenter.setting.blacknum;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.pr.scuritycenter.R;
import com.pr.scuritycenter.framework.BaseActivity;
import com.pr.scuritycenter.utils.DeviceUtils;

/**
 * 
 * @author wangzhong
 *
 */
public class BlackNumberActivity extends BaseActivity implements OnClickListener {
	
	private LinearLayout ll_blacknumbers_home;
	
	public static final int LOAD_DATA_SUCCESS = 1;
	
	private static final int ACTION_ADD		= 0;
	private static final int ACTION_UPDATE	= 1;

	private ListView lv_blacknumbers;
	private Button bt_add_blacknumber;
	
	private BlackNumberDao mBlackNumberDao;
	private List<BlackNumberBean> mListBlackNumberBeans;
	private BlackNumberAdapter mBlackNumberAdapter;
	
	//private boolean isStarted = false;

	@Override
	public void initInfo() {
		
	}

	@Override
	public void initView() {
		setContentView(R.layout.black_numbers_home);
		
		ll_blacknumbers_home = (LinearLayout) findViewById(R.id.ll_blacknumbers_home);
		ll_blacknumbers_home.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
		
		
		lv_blacknumbers = (ListView) findViewById(R.id.lv_blacknumbers);
		bt_add_blacknumber = (Button) findViewById(R.id.bt_add_blacknumber);
		registerForContextMenu(lv_blacknumbers);
		bt_add_blacknumber.setOnClickListener(this);
	}

	@Override
	public void initData() {
		mBlackNumberDao = new BlackNumberDao(this);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				mListBlackNumberBeans = mBlackNumberDao.findAll();
				
				Message msg = Message.obtain();
				msg.what = LOAD_DATA_SUCCESS;
				mHandler.sendMessage(msg);
			}
		}).start();
	}

	@Override
	protected void initTopbar() {
		super.initTopbar();
		setTitle(getResources().getString(R.string.blacknumber_title));
		bt_topbar_assist.setVisibility(View.GONE);
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == LOAD_DATA_SUCCESS) {
				mBlackNumberAdapter = new BlackNumberAdapter(BlackNumberActivity.this, mListBlackNumberBeans);
				lv_blacknumbers.setAdapter(mBlackNumberAdapter);
			}
			super.handleMessage(msg);
		}
		
	};

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.safe_intercept_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int position = (int) info.id;
		switch (item.getItemId()) {
		case R.id.item_delete:
			deleteBlackNumber(position);
			return true;
		case R.id.item_update:
			updateBlackNumber(position);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void addBlackNumber() {
		showBlackNumberDialog(ACTION_ADD, 0);
	}

	private void updateBlackNumber(int position) {
		showBlackNumberDialog(ACTION_UPDATE, position);
	}

	private void deleteBlackNumber(int position) {
		BlackNumberBean blackNumberBean = (BlackNumberBean) lv_blacknumbers.getItemAtPosition(position);
		mBlackNumberDao.delete(blackNumberBean.getNumber());
		mListBlackNumberBeans.remove(blackNumberBean);
		mBlackNumberAdapter.notifyDataSetChanged();
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
//			Intent i = new Intent(this, SafeInterceptService.class);
//			if (isStarted) {
//				isStarted = false;
//				Toast.makeText(getApplicationContext(), "正在停止拦截服务！", Toast.LENGTH_SHORT).show();
//				stopService(i);
//			} else {
//				isStarted = true;
//				Toast.makeText(getApplicationContext(), "正在开启拦截服务！", Toast.LENGTH_SHORT).show();
//				startService(i);
//			}
//			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.bt_add_blacknumber:
			addBlackNumber();
			break;

		default:
			break;
		}
	}

	/**
	 * 
	 * @param flag		0:add;	1:update.
	 * @param position	
	 */
	private void showBlackNumberDialog(final int flag, final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View dialogView = View.inflate(this, R.layout.add_black_number_dialog, null);
		
		final EditText et_number = (EditText) dialogView.findViewById(R.id.et_add_black_number);
		final CheckBox cb_phone = (CheckBox) dialogView.findViewById(R.id.cb_block_phone);
		final CheckBox cb_sms = (CheckBox) dialogView.findViewById(R.id.cb_block_sms);
		
		if (flag == ACTION_UPDATE) {
			builder.setTitle("修改");
			BlackNumberBean blackNumberBean = (BlackNumberBean) lv_blacknumbers.getItemAtPosition(position);
			et_number.setText(blackNumberBean.getNumber());
			et_number.setEnabled(false);
			switch (blackNumberBean.getMode()) {
			case BlackNumberBean.STOP_SMS:
				cb_sms.setChecked(true);
                cb_phone.setChecked(false);
				break;
			case BlackNumberBean.STOP_CALL:
				cb_phone.setChecked(true);
                cb_sms.setChecked(false);
				break;
			case BlackNumberBean.STOP_ALL:
				cb_phone.setChecked(true);
                cb_sms.setChecked(true);
				break;

			default:
				break;
			}
		} else {
			builder.setTitle("添加");
		}
		
		
		builder.setView(dialogView);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String number = et_number.getText().toString().trim();
				if (number.isEmpty()) {
					Toast.makeText(getApplicationContext(), "请填写号码！", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (flag == ACTION_ADD && mBlackNumberDao.find(number)) {
					Toast.makeText(getApplicationContext(), "要添加的号码已经存在！", Toast.LENGTH_SHORT).show();
					return;
				}

				int mode = -1;
				if (cb_phone.isChecked() && cb_sms.isChecked()) {	// STOP_ALL
					mode = BlackNumberBean.STOP_ALL;
				} else if (cb_phone.isChecked()) {					// STOP_CALL
					mode = BlackNumberBean.STOP_CALL;
				} else if (cb_sms.isChecked()) {					// STOP_SMS
					mode = BlackNumberBean.STOP_SMS;
				} else {											// Unselected。
					mode = -1;
					Toast.makeText(getApplicationContext(), "拦截模式不能为空!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (flag == ACTION_ADD) {
					boolean result = mBlackNumberDao.add(number, mode);
					if (result) {
						BlackNumberBean blackNumberBean = new BlackNumberBean();
						blackNumberBean.setNumber(number);
						blackNumberBean.setMode(mode);
						mListBlackNumberBeans.add(blackNumberBean);
						mBlackNumberAdapter.notifyDataSetChanged();
					}
				} else if (flag == ACTION_UPDATE) {
					BlackNumberBean blackNumberBean = (BlackNumberBean) lv_blacknumbers.getItemAtPosition(position);
					mBlackNumberDao.update(blackNumberBean.getNumber(), number, mode);
					blackNumberBean.setMode(mode);
					blackNumberBean.setNumber(number);
					mBlackNumberAdapter.notifyDataSetChanged();
				}
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		builder.create().show();
	}

}
