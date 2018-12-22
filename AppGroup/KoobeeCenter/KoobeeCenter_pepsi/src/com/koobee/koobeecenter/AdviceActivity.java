package com.koobee.koobeecenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.koobee.koobeecenter.base.BaseActivity;
import com.koobee.koobeecenter02.R;

/**
 * 选择问题反馈类别界面
 * 
 * @author longbaoxiu
 *
 */
public class AdviceActivity extends BaseActivity {
	private GridView mGridView;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			// | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.feedback_status_color));
			// window.setNavigationBarColor(Color.TRANSPARENT);
		}
		setContentView(R.layout.activity_feedback);
		findViewById();
		init();
		setListener();
	}

	@Override
	protected void findViewById() {
		mGridView = (GridView) findViewById(R.id.mGridView);

	}

	@Override
	protected void init() {
		ArrayList<String> localArrayList = new ArrayList<String>();
		String[] arrayOfString = getResources().getStringArray(
				R.array.array_items);
		int len = arrayOfString.length;
		for (int i = 0; i < len; i++) {
			localArrayList.add(arrayOfString[i]);
		}

		int[] arrayOfInt = new int[len];
		arrayOfInt[0] = R.drawable.power_and_heat_selector;
		arrayOfInt[1] = R.drawable.signal_and_comm_selector;
		arrayOfInt[2] = R.drawable.off_and_restart_selector;
		arrayOfInt[3] = R.drawable.bluetooth_selector;
		arrayOfInt[4] = R.drawable.camara_selector;
		arrayOfInt[5] = R.drawable.wlan_selector;
		arrayOfInt[6] = R.drawable.gps_selector;
		arrayOfInt[7] = R.drawable.advice_item_selector;
		arrayOfInt[8] = R.drawable.application_item_selector;

		SimpleAdapter localSimpleAdapter = new SimpleAdapter(this, getData(
				localArrayList, arrayOfInt),
				R.layout.main_operation_manual_list_item, new String[] { "img",
						"title" }, new int[] { R.id.column_img,
						R.id.column_title });

		mGridView.setAdapter(localSimpleAdapter);
	}

	private List<Map<String, Object>> getData(List<String> paramList,
			int[] paramArrayOfInt) {
		ArrayList localArrayList = new ArrayList();
		for (int i = 0; i < paramList.size(); i++) {
			HashMap localHashMap = new HashMap();
			localHashMap.put("img", Integer.valueOf(paramArrayOfInt[i]));
			localHashMap.put("title", paramList.get(i));
			localArrayList.add(localHashMap);
		}
		return localArrayList;
	}

	@Override
	protected void setListener() {
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(AdviceActivity.this,
						FeedBackRequestActivity.class);
				intent.putExtra("qtype", ""+(position + 1));  // modify-by-yanghao-20151026
				startActivity(intent);
				overridePendingTransition(R.anim.in_from_right,
						R.anim.out_to_left);
			}
		});

	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}
}
