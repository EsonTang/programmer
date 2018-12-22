package com.pr.scuritycenter.setting;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.pr.scuritycenter.R;
import com.pr.scuritycenter.framework.BaseActivity;
import com.pr.scuritycenter.setting.blacknum.BlackNumberActivity;
import com.pr.scuritycenter.utils.DeviceUtils;

/**
 * 
 * @author wangzhong
 *
 */
public class SettingActivity extends BaseActivity implements OnItemClickListener {

	private LinearLayout ll_setting_home;

	private ListView lv_setting_content;
	private SettingListAdapter mSettingListAdapter;
	
	@Override
	public void initInfo() {
		
	}

	@Override
	public void initView() {
		setContentView(R.layout.setting_activity);
		
		ll_setting_home = (LinearLayout) findViewById(R.id.ll_setting_home);
		ll_setting_home.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
		

		lv_setting_content = (ListView) findViewById(R.id.lv_setting_content);
		String[] arraySettingContent = getResources().getStringArray(R.array.array_setting_list);
		List<String> listData = new ArrayList<String>();
		for (int i = 0; i < arraySettingContent.length; i++) {
			listData.add(arraySettingContent[i]);
		}
		mSettingListAdapter = new SettingListAdapter(this.getLayoutInflater(), listData);
		lv_setting_content.setAdapter(mSettingListAdapter);
		lv_setting_content.setOnItemClickListener(this);
	}

	@Override
	protected void initTopbar() {
		super.initTopbar();
		
		setTitle(getResources().getString(R.string.setting_title));
		setAssistBG(R.drawable.top_bar_back_selected);
		bt_topbar_assist.setVisibility(View.GONE);
	}

	@Override
	public void initData() {
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		switch (arg2) {
		case 0:
			Intent iBlackNumber = new Intent(this, BlackNumberActivity.class);
			startActivity(iBlackNumber);
			break;

		default:
			break;
		}
	}

}
