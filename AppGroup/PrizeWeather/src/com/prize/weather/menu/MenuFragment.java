package com.prize.weather.menu;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.prize.weather.IBDLocationRefreshListener;
import com.prize.weather.R;
import com.prize.weather.WeatherHomeActivity;
import com.prize.weather.city.CitySelectActivity;
import com.prize.weather.framework.BaseFragment;
import com.prize.weather.framework.ISPCallBack;
import com.prize.weather.util.Common;

@SuppressWarnings("rawtypes")
public class MenuFragment extends BaseFragment implements IBDLocationRefreshListener, OnClickListener {
	
	private ListView cityList;
	private MenuCityListAdapter cityAdapter;

	private RelativeLayout notiTimeLayout;
	private ImageButton cityAddBt , cityEditBt;
	
	private ArrayList<Integer> cityFlagList = new ArrayList<Integer>();
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = mActivity.getLayoutInflater().inflate(R.layout.weather_menu, container, false);
		initView();
		
		/*// @Deprecated the city location refresh listener.     DeprecatedDrawerLayout.1/2
		@author wangzhong  2015.07.21  stard.
		((WeatherHomeActivity) mActivity).setmIBDLocationRefreshListener(this);
		@author wangzhong  2015.07.21  end.*/
		
		return mView;
	}

	private void initView() {
		cityList = (ListView) mView.findViewById(R.id.menu_city_list);
		cityAdapter = new MenuCityListAdapter(mActivity);
		
		/*// @Deprecated the city delete listener.     DeprecatedDrawerLayout.2/2
		@author wangzhong  2015.07.21  stard.
		cityAdapter.setmICityDeleteListener((WeatherHomeActivity) mActivity);
		@author wangzhong  2015.07.21  end.*/
		
		cityList.setAdapter(cityAdapter);

		notiTimeLayout = (RelativeLayout) mView.findViewById(R.id.noti_time_layout);
		notiTimeLayout.setOnClickListener(this);
		
		cityAddBt = (ImageButton)mView.findViewById(R.id.city_add_bt);
		cityAddBt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SharedPreferences citySharePreferences = mActivity.getSharedPreferences(ISPCallBack.SHARED_PREFERENCES_FILE_NAME, Activity.MODE_PRIVATE);
				int cityNum = citySharePreferences.getInt(ISPCallBack.SP_CITY_NUM, 1);
				if (cityNum >= 7) {
					Toast.makeText(getContext(), R.string.max_city_num, Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent = new Intent(mActivity, CitySelectActivity.class);
				startActivity(intent);
			}
		});
		
		cityEditBt = (ImageButton) mView.findViewById(R.id.city_edit_bt);
		cityEditBt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (cityAdapter.isEdit == false) {
					cityAdapter.isEdit = true;
					cityEditBt.setBackgroundResource(R.drawable.city_ok_img);
					cityAddBt.setEnabled(false);
					cityAddBt.setBackgroundResource(R.drawable.city_add_sel);
				} else {
					cityAdapter.isEdit = false;
					cityEditBt.setBackgroundResource(R.drawable.city_edit_img);
					cityAddBt.setEnabled(true);
					cityAddBt.setBackgroundResource(R.drawable.city_add_img);
				}
				cityAdapter.notifyDataSetChanged();
			}
		});

		cityList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				SharedPreferences citySharePreferences = mActivity.getSharedPreferences(ISPCallBack.SHARED_PREFERENCES_FILE_NAME, Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = citySharePreferences.edit();
				int cityNum = citySharePreferences.getInt(ISPCallBack.SP_CITY_NUM, 1);
				cityFlagList.clear();
				for (int i = 0; i < cityNum; i++) {
					cityFlagList.add(citySharePreferences.getInt(ISPCallBack.SP_CITY_FLAG + i, 0));
					cityFlagList.set(i, 0);
				}
				cityFlagList.set(arg2, 1);
				
				for (int i = 0; i < cityFlagList.size(); i++) {
					editor.putInt(ISPCallBack.SP_CITY_FLAG + i, cityFlagList.get(i));
				}
				editor.commit();

				cityAdapter.getCityNames();
				cityAdapter.notifyDataSetChanged();
				
				Intent intent = new Intent(Common.UPDATE_WEATHET_WIDGET);
				mActivity.sendBroadcast(intent);
				
				WeatherHomeActivity.cityScrollSelected = arg2;
				Intent intent2 = new Intent(mActivity, WeatherHomeActivity.class);				
				startActivity(intent2);
				
				mActivity.finish();
			}
		});
		
	}

	@Override
	public void openUpdateStatus() {
		
	}

	@Override
	public void closeUpdateStatus() {
		
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshMenuFragment();
	}

	@Override
	public void updateView(Object o) {
		
	}
	
	@Override
	public void refreshMenuFragment() {
		cityAdapter.getCityNames();
		cityAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		
	}

	@Override
	public void showNodataView(boolean isNodata) {
		
	}
	
}
