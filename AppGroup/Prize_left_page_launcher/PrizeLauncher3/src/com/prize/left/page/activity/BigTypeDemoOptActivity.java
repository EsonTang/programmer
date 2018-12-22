package com.prize.left.page.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.model.CardDemoModel;
import com.prize.left.page.util.CommonUtils;

/***
 * 大类下的卡片管理及demo展示activity
 * @author fanjunchen
 *
 */
public class BigTypeDemoOptActivity extends Activity {

	private final String TAG = "BigTypeDemoOptActivity";
	
	private TextView titleView;
	
	private CardDemoModel model;
	/**标题**/
	public static final String KEY_TITLE = "p_title";
	/**key类型**/
	public static final String KEY_CODE = "p_code";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		initStatusBar();
		
		super.onCreate(savedInstanceState);
		
		View topView = LayoutInflater.from(this).inflate(R.layout.left_card_demo_lay, null);
		
		setContentView(topView);
		
		CommonUtils.changeStatus(getWindow());
        
        initView(topView);
	}
	/***
	 * 初始化状态栏
	 */
	protected void initStatusBar() {
		
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.white));//status_color
		}
		
	}
	/***
	 * 设置标题及使刷新按钮不可见
	 */
	private void setTitle(String title) { // 传过来的
		titleView = (TextView) findViewById(R.id.tv_title);
		findViewById(R.id.btn_refresh).setVisibility(View.GONE);
		titleView.setText(title);
	}

	/***
	 * 初始化控件
	 */
	private void initView(View v) {
		Intent it = getIntent();
		String tt = it.getStringExtra(KEY_TITLE);
		setTitle(tt);
		
		String key = it.getStringExtra(KEY_CODE);
		
		model = new CardDemoModel(v , this, key);
	}
	
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btn_back:
				onBackPressed();
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		/*if (cardChange != null)
			cardChange.onSelCardChange();*/
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
