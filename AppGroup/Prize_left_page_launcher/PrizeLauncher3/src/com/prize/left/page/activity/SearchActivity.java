package com.prize.left.page.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.launcher3.BlueTaskWall;
import com.android.launcher3.R;
import com.android.launcher3.view.LauncherBackgroudView;
import com.prize.left.page.model.SearchModel;

/***
 * 搜索activity
 * @author fanjunchen
 *
 */
public class SearchActivity extends Activity {

	private final String TAG = "SearchActivity";
	
	private TextView titleView;
	
	private String mTitle = null;
	
	private SearchModel model;
	
	private LauncherBackgroudView bgView;
	
	private BlueTaskWall bgTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		initStatusBar();
		
		super.onCreate(savedInstanceState);
		
		View topView = LayoutInflater.from(this).inflate(R.layout.search_lay, null);
		setContentView(topView);
		
		// CommonUtils.changeStatus(getWindow());
        initView(topView);
	}
	/***
	 * 初始化状态栏
	 */
	protected void initStatusBar() {
		
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
//		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.status_color));
		}*/
	}
	/***
	 * 设置标题及使刷新按钮不可见
	 */
	private void setTitle() {
		titleView = (TextView) findViewById(R.id.tv_title);
		titleView.setText(R.string.str_manager_card);
	}

	/***
	 * 初始化控件
	 */
	private void initView(View topView) {
		// setTitle();
		model = new SearchModel(topView, this);
		
		bgView = (LauncherBackgroudView)topView.findViewById(R.id.view_bg);
		bgTask = new BlueTaskWall(this, bgView);
		
		model.setActivity(this);
		model.onFinishedInflate();
		
		bgTask.execute();
	}
	
	@Override
	protected void onDestroy() {
		bgTask.cancel(true);
		bgTask = null;
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		model.onDestroy();
	}
}
