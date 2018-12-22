package com.android.prize.simple.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.prize.simple.model.AllAppsModel;
import com.prize.left.page.util.CommonUtils;

/***
 * 所有应用管理activity
 * @author fanjunchen
 *
 */
public class AllAppsActivity extends Activity {/*

	private final String TAG = "AllAppsActivity";
	
	private TextView titleView;
	
	private AllAppsModel appModel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		initStatusBar();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_all_app_lay);
		
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
					R.color.simple_app_head_color));
		}
		
	}
	*//***
	 * 设置标题及使刷新按钮不可见
	 *//*
	private void setTitle() {
		findViewById(R.id.title_lay).setBackgroundResource(R.color.simple_app_head_color);
		titleView = (TextView) findViewById(R.id.tv_title);
		titleView.setText(R.string.simple_all_app);
	}

	*//***
	 * 初始化控件
	 *//*
	private void initView() {
		
		setTitle();
		
		appModel = new AllAppsModel(this);
		
		ListView mListView = (ListView) findViewById(R.id.recycle_list);
		
		appModel.setListView(mListView);
	}
	
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
			case R.id.btn_back:
				finish();
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		appModel.destroy();
		super.onDestroy();
	}
*/}
