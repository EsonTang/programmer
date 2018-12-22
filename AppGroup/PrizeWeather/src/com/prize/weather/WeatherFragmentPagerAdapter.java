package com.prize.weather;

import java.util.ArrayList;

import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

/**
 * 
 * @author wangzhong
 *
 */
/*PRIZE-Change-PrizeWeather-wangzhong-2016_8_10*/
public class WeatherFragmentPagerAdapter extends FragmentStatePagerAdapter {

	public ArrayList<Fragment> list;

	public WeatherFragmentPagerAdapter(FragmentManager fragmentManager, 
			ArrayList<Fragment> list) {
		super(fragmentManager);
		this.list = list;
	}

	@Override
	public Fragment getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		Log.d("WeatherFragmentPagerAdapter","[destroyItem] Triggered!");
		FragmentManager manager = ((Fragment)object).getFragmentManager();
		android.support.v4.app.FragmentTransaction trans = manager.beginTransaction();
		trans.remove((Fragment)object);
		trans.commit();
		super.destroyItem(container, position, object);
	}
	
	

}
