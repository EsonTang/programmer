package com.android.prize.simple.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.prize.simple.model.IConstant;
import com.android.prize.simple.model.PagedDataModel;
import com.android.prize.simple.utils.SimplePrefUtils;
import com.prize.left.page.util.CommonUtils;

/***
 * 设置activity
 * @author fanjunchen
 *
 */
public class SettingsActivity extends Activity {/*

	private final String TAG = "SettingsActivity";
	
	private TextView titleView;
	
	private final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
	
	private TextView txtSafe;
	
	private View managerView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		initStatusBar();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_settings_lay);
		
		CommonUtils.changeStatusWhite(getWindow());
        
        initView();
	}
	*//***
	 * 初始化状态栏
	 *//*
	protected void initStatusBar() {
		
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.simple_setting_head_color));//status_color
		}
		
	}
	*//***
	 * 设置标题及使刷新按钮不可见
	 *//*
	private void setTitle() {
		findViewById(R.id.title_lay).setBackgroundResource(R.color.simple_setting_head_color);
		titleView = (TextView) findViewById(R.id.tv_title);
		titleView.setText(R.string.simple_settings);
	}

	*//***
	 * 初始化控件
	 *//*
	private void initView() {
		
		setTitle();
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		mainIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));
		
		txtSafe = (TextView)findViewById(R.id.txt_safe_lock);
		managerView = findViewById(R.id.lay_desk_manager);
		
		boolean b = SimplePrefUtils.getBoolean(this, IConstant.KEY_LOCK);
		if (b) {
			managerView.setVisibility(View.GONE);
			txtSafe.setCompoundDrawablesWithIntrinsicBounds(null,
					null, getDrawable(R.drawable.simple_icon_locked), null);
			txtSafe.setText(R.string.simple_locked);
		}
		
	}
	
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.txt_exit_desk:
				PagedDataModel.getInstance().exitDesk();
				finish();
				break;
			case R.id.txt_desk_manager:
				PagedDataModel.getInstance().enterMode(PagedDataModel.STATUS_EDIT);
				finish();
				break;
			case R.id.txt_sys_settings:
				startActivity(mainIntent);
				finish();
				break;
			case R.id.txt_safe_lock:
				boolean b = SimplePrefUtils.getBoolean(this, IConstant.KEY_LOCK);
				if (b) { // 解锁
					managerView.setVisibility(View.VISIBLE);
					txtSafe.setCompoundDrawablesWithIntrinsicBounds(null,
							null, getDrawable(R.drawable.simple_icon_unlock), null);
					txtSafe.setText(R.string.simple_unlock);
				}
				else {
					managerView.setVisibility(View.GONE);
					txtSafe.setCompoundDrawablesWithIntrinsicBounds(null, 
							null, getDrawable(R.drawable.simple_icon_locked), null);
					txtSafe.setText(R.string.simple_locked);
				}
				SimplePrefUtils.putBoolean(this, IConstant.KEY_LOCK, !b);
				PagedDataModel.getInstance().safeLock(!b);
				break;
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		finish();
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
*/}
