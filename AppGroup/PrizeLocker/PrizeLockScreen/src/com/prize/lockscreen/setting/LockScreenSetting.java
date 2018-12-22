package com.prize.lockscreen.setting;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.lockscreen.service.KeyguardWallpaperObserver;
import com.prize.lockscreen.utils.SharedPreferencesTool;
import com.prize.prizelockscreen.R;
/***
 * 锁屏设置
 * @author fanjunchen
 *
 */
public class LockScreenSetting extends Activity {

	private final int ID_LOCKSCREEN_TITLE = -1;
	private final int ID_LOCKSCREEN_ENABLE = 0;
	private final int ID_LOCKSCREEN_OPEN = 1;

	private ListView mListView;
	private SettingListViewAdapter mSettingListViewAdapter;
	private List<SettingData> mList = new ArrayList<LockScreenSetting.SettingData>();
	
	private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

	
	private EditText mEdit;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			int color = getResources().getColor(R.color.color_title);
			window.setStatusBarColor(color);
		}
		
		setContentView(R.layout.lockscreen_setting);
		init();
	}
	/***
	 * 初始化控件
	 */
	private void init() {
		
		mListView = (ListView) findViewById(R.id.setting_list);
		
		mList.add(new SettingData(ID_LOCKSCREEN_ENABLE, null, getResources()
				.getString(R.string.is_lockscreen_enable_text), false, 2));
		
		mList.add(new SettingData(ID_LOCKSCREEN_TITLE, getResources()
				.getString(R.string.lockscreen_setting_text), null, true, 0));
		
		mList.add(new SettingData(ID_LOCKSCREEN_OPEN, null, getResources()
				.getString(R.string.open_lockscreen_pwd_text), false, 1));
		
		mSettingListViewAdapter = new SettingListViewAdapter();
		mListView.setAdapter(mSettingListViewAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SettingData sd = mList.get(position);
				LockScreenSetting.this.onItemClick(sd);
			}

		});
		
		mEdit = (EditText)findViewById(R.id.lock_type);
		
		TextView tv = (TextView)findViewById(R.id.title);
		tv.setText(R.string.lockscreen_setting_text);
		
		// findViewById(R.id.back).setVisibility(View.GONE);
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.open_notice_lay:
				startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
				break;
			case R.id.close_system_keyguard_lay:
				startActivity(new Intent("android.app.action.SET_NEW_PASSWORD"));
				break;
			case R.id.btn_ok:
				String a = mEdit.getText().toString().trim();
				if (!TextUtils.isEmpty(a)) {
					Settings.System.putInt(getContentResolver(), SharedPreferencesTool.KEYGUARD_TYPE, Integer.parseInt(a));
				}
				break;
			case R.id.back:
				finish();
				break;
		}
	}

	private void onItemClick(SettingData sd) {
		switch (sd.rightViewType) {
		case 1:// TextView
			switch (sd.id) {
			case ID_LOCKSCREEN_OPEN:
				openOrUpdateLockScreenPwd(sd);
				break;
			}
			break;
		case 2:// checkbox
			switch (sd.id) {
			case ID_LOCKSCREEN_ENABLE:
				makeLockScreenIsEnable(sd);
				break;
			}
			break;
		}
	}

	private void makeLockScreenIsEnable(SettingData sd) {
		boolean state = SharedPreferencesTool.isLockScreenEnable(this);
		SharedPreferencesTool.setLockScreenEnable(this, !state);
		try {
			CheckBox cb = (CheckBox) sd.view;
			cb.setChecked(!state);
		} catch (Exception e) {
			Log.e("error",
					"SettingData.rightViewType and SettingData.view is not match");
		}
	}

	private void openOrUpdateLockScreenPwd(SettingData sd) {
		Intent intent = new Intent();
		ComponentName cn = new ComponentName(this, OpenLockScreenPwd.class);
		intent.setComponent(cn);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mSettingListViewAdapter != null) {
			mSettingListViewAdapter.notifyDataSetChanged();
		}
	}

	class SettingData {
		public SettingData(int id, String title, String content,
				boolean isTitle, int type) {
			this.id = id;
			this.title = title;
			this.content = content;
			this.isTitle = isTitle;
			this.rightViewType = type;
		}

		public String title;
		public String content;
		public boolean isTitle;
		public int rightViewType;// 右边View的类型：0表示无View，1表示TextView，2表示checkbox
		public View view;
		public int id;
	}

	class ViewHolder {
		public RelativeLayout relativeLayout;
		public TextView titleTxt;
		public LinearLayout linearLayout;
	}

	class SettingListViewAdapter extends BaseAdapter {

		public SettingListViewAdapter() {
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(LockScreenSetting.this)
						.inflate(R.layout.lockscreen_setting_item, null);
				holder = new ViewHolder();
				holder.relativeLayout = (RelativeLayout) convertView
						.findViewById(R.id.item_layout);
				holder.titleTxt = (TextView) convertView
						.findViewById(R.id.item_title);
				holder.linearLayout = (LinearLayout) convertView
						.findViewById(R.id.item_right_layout);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			SettingData sd = mList.get(position);
			if (sd.isTitle) {
				holder.relativeLayout.setPadding(5, 10, 5, 10);
				holder.relativeLayout.setBackgroundColor(getResources()
						.getColor(R.color.gray_e));
				holder.titleTxt.setTextSize(getResources().getDimension(
						R.dimen.setting_title_textsize));
				holder.titleTxt.setTextColor(getResources().getColor(
						R.color.gray_3));
				if (sd.title != null) {
					holder.titleTxt.setText(sd.title);
				}
			} else {
				holder.relativeLayout.setPadding(5, 20, 5, 20);
				holder.relativeLayout
						.setBackgroundResource(R.drawable.setting_listview_item_selector);
				holder.titleTxt.setTextSize(getResources().getDimension(
						R.dimen.setting_content_textsize));
				holder.titleTxt.setTextColor(getResources().getColor(
						R.color.black));
				if (sd.content != null) {
					holder.titleTxt.setText(sd.content);
				}
			}
			switch (sd.rightViewType) {
			case 0:
				holder.linearLayout.removeAllViews();
				break;
			case 1:
				addTextView(holder, sd);
				break;
			case 2:
				addCheckBox(holder, sd);
				break;
			}
			return convertView;
		}
	}

	@SuppressLint("InflateParams")
	public void addCheckBox(ViewHolder holder, SettingData sd) {
		CheckBox cb = (CheckBox) LayoutInflater.from(LockScreenSetting.this)
				.inflate(R.layout.lockscreen_setting_item_checkbox, null);
		holder.linearLayout.removeAllViews();
		sd.view = cb;
		cb.setChecked(SharedPreferencesTool
				.isLockScreenEnable(LockScreenSetting.this));
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		holder.linearLayout.addView(cb, p);
	}

	@SuppressLint("InflateParams")
	public void addTextView(ViewHolder holder, SettingData sd) {
		TextView tv = (TextView) LayoutInflater.from(LockScreenSetting.this)
				.inflate(R.layout.lockscreen_setting_item_textview, null);
		holder.linearLayout.removeAllViews();
		sd.view = tv;
		tv.setTextColor(getResources().getColor(R.color.black));
		tv.setTextSize(getResources().getDimension(
				R.dimen.setting_content_textsize));
		int style = SharedPreferencesTool
				.getLockPwdType(LockScreenSetting.this);
		String title = getResources().getString(
				R.string.update_lockscreen_pwd_text);
		switch (style) {
		case 0:
			tv.setVisibility(View.GONE);
			break;
		case 1:
			holder.titleTxt.setText(title);
			tv.setText(getResources().getString(
					R.string.lockscreen_style_number_text));
			break;
		case 2:
			holder.titleTxt.setText(title);
			tv.setText(getResources().getString(
					R.string.lockscreen_style_icon_text));
			break;
		case 3:
			holder.titleTxt.setText(title);
			tv.setText(getResources().getString(
					R.string.lockscreen_style_complex_text));
			break;
		}
		if (tv.getVisibility() == View.VISIBLE) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			holder.linearLayout.addView(tv, params);
		}
	}

}
