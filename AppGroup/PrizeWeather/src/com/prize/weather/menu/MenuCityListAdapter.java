package com.prize.weather.menu;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.prize.weather.R;
import com.prize.weather.framework.ISPCallBack;

@SuppressLint("ViewHolder")
public class MenuCityListAdapter extends BaseAdapter {
	
//	private ViewHolder viewHolder;

	private Context mContext;
	private ArrayList<String> cityNameList = new ArrayList<String>();
	private ArrayList<Integer> cityFlagList = new ArrayList<Integer>();
	private ArrayList<Integer> cityCodeList = new ArrayList<Integer>();
	
	public boolean isEdit = false;
	private static boolean isDeleting = false;
	
	// Delete city in menu fragment.
	private ICityDeleteListener mICityDeleteListener = null;
	
	public void setmICityDeleteListener(ICityDeleteListener mICityDeleteListener) {
		this.mICityDeleteListener = mICityDeleteListener;
	}

	public MenuCityListAdapter(Context context) {
		super();
		this.mContext = context;
		getCityNames();
	}

	@Override
	public int getCount() {
		return cityNameList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
	
	@SuppressLint("InflateParams")
	@Override
	public View getView(final int position, View view, ViewGroup arg2) {
//		final int index = position;
//		String mContent = cityNameList.get(index);
//		
//		if (null == view) {
//			viewHolder = new ViewHolder();
//			view = LayoutInflater.from(mContext).inflate(R.layout.city_set_item, null);
//			viewHolder.cityName = (TextView) view.findViewById(R.id.menu_city_name);
//			viewHolder.deleteCity = (Button) view.findViewById(R.id.delete_city);
//			viewHolder.cityDefaultImg = (ImageView) view.findViewById(R.id.city_defalt);
//			viewHolder.cityLocationImg = (ImageView) view.findViewById(R.id.city_location);
//			view.setTag(viewHolder);
//		} else {
//			viewHolder = (ViewHolder) view.getTag();
//		}
//		
//		viewHolder.cityName.setText(mContent);
//		if (position == 0) {
//			viewHolder.cityLocationImg.setVisibility(View.VISIBLE);
//		} else {
//			viewHolder.cityLocationImg.setVisibility(View.GONE);
//		}
//			
//		if (isEdit == false) {
//			if (cityFlagList.get(index) == 0) {
//				/*viewHolder.deleteCity.setVisibility(View.VISIBLE);
//				viewHolder.setDefaultCity.setVisibility(View.VISIBLE);
//				viewHolder.defaultCity.setVisibility(View.GONE);*/
//				viewHolder.cityDefaultImg.setVisibility(View.GONE);
//			} else {
//				/*viewHolder.deleteCity.setVisibility(View.GONE);
//				viewHolder.setDefaultCity.setVisibility(View.GONE);
//				viewHolder.defaultCity.setVisibility(View.VISIBLE);*/
//				viewHolder.cityDefaultImg.setVisibility(View.VISIBLE);
//			}				
//			viewHolder.deleteCity.setVisibility(View.GONE);
//		} else {
//			viewHolder.cityDefaultImg.setVisibility(View.GONE);
//			viewHolder.deleteCity.setVisibility(View.VISIBLE);
//			if ((cityFlagList.get(index) == 1) || (position == 0)) {
//				viewHolder.deleteCity.setBackgroundResource(R.drawable.city_delete_unable);
//				viewHolder.deleteCity.setEnabled(false);
//				viewHolder.deleteCity.setTextColor(Color.rgb(121, 121, 121));
//				if (cityFlagList.get(0) == 1) {
//					viewHolder.cityLocationImg.setVisibility(View.GONE);
//				}
//			} else {
//				if (isDeleting) {
//					viewHolder.deleteCity.setBackgroundResource(R.drawable.city_delete_unable);
//					viewHolder.deleteCity.setEnabled(false);
//					viewHolder.deleteCity.setTextColor(Color.rgb(121, 121, 121));
//				} else {
//					viewHolder.deleteCity.setBackgroundResource(R.drawable.city_delete_img);
//					viewHolder.deleteCity.setEnabled(true);
//					viewHolder.deleteCity.setTextColor(Color.rgb(255, 255, 255));
//				}
//			}
//		}
//		viewHolder.deleteCity.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				if (!isDeleting) {
//					isDeleting = true;
//					getCityNames();
//					SharedPreferences citySharePreferences = mContext.getSharedPreferences(ISPCallBack.SHARED_PREFERENCES_FILE_NAME, Activity.MODE_PRIVATE);
//					SharedPreferences.Editor editor = citySharePreferences.edit();
//					int cityNum = citySharePreferences.getInt(ISPCallBack.SP_CITY_NUM, 1);
//					cityNum = cityNum - 1;
//					if ((cityNameList.size() > index) || (cityFlagList.size() > index) || (cityCodeList.size() > index)) {
//						cityNameList.remove(index);
//						cityFlagList.remove(index);
//						cityCodeList.remove(index);
//					}
//					editor.clear().commit();
//					editor.putInt(ISPCallBack.SP_CITY_NUM, cityNum);
//					for (int i = 0; i < cityNum; i++) {
//						editor.putString(ISPCallBack.SP_CITY_NAME + i, cityNameList.get(i));
//						editor.putInt(ISPCallBack.SP_CITY_FLAG + i, cityFlagList.get(i));
//						editor.putInt(ISPCallBack.SP_CITY_CODE + i, cityCodeList.get(i));
//					}
//					editor.commit();
//					if (null != mICityDeleteListener) {
//						if (mICityDeleteListener.onCityDeleteListener(position)) {
//							isDeleting = false;
//							notifyDataSetChanged();
//						}
//					}
//				}
//			}
//		});
//		return view;
		
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.city_set_item, null);
		TextView cityName = (TextView) itemView.findViewById(R.id.menu_city_name);
		final Button deleteCity = (Button) itemView.findViewById(R.id.delete_city);
		ImageView cityDefaultImg = (ImageView) itemView.findViewById(R.id.city_defalt);
		ImageView cityLocationImg = (ImageView) itemView.findViewById(R.id.city_location);
		
		cityName.setText(cityNameList.get(position));
		if (position == 0) {
			cityLocationImg.setVisibility(View.VISIBLE);
		} else {
			cityLocationImg.setVisibility(View.GONE);
		}
		if (isEdit == false) {
			if (cityFlagList.get(position) == 0) {
				cityDefaultImg.setVisibility(View.GONE);
			} else {
				cityDefaultImg.setVisibility(View.VISIBLE);
			}				
			deleteCity.setVisibility(View.GONE);
		} else {
			cityDefaultImg.setVisibility(View.GONE);
			deleteCity.setVisibility(View.VISIBLE);
			if ((cityFlagList.get(position) == 1) || (position == 0)) {
				deleteCity.setBackgroundResource(R.drawable.city_delete_unable);
				deleteCity.setEnabled(false);
				deleteCity.setTextColor(Color.rgb(121, 121, 121));
				if (cityFlagList.get(0) == 1) {
					cityLocationImg.setVisibility(View.GONE);
				}
			} else {
				if (isDeleting) {
					deleteCity.setBackgroundResource(R.drawable.city_delete_unable);
					deleteCity.setEnabled(false);
					deleteCity.setTextColor(Color.rgb(121, 121, 121));
				} else {
					deleteCity.setBackgroundResource(R.drawable.city_delete_img);
					deleteCity.setEnabled(true);
//					deleteCity.setTextColor(Color.rgb(255, 255, 255));
				}
			}
		}
		deleteCity.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!isDeleting) {
					deleteCity.setEnabled(false);
					isDeleting = true;
					getCityNames();
					SharedPreferences citySharePreferences = mContext.getSharedPreferences(ISPCallBack.SHARED_PREFERENCES_FILE_NAME, Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = citySharePreferences.edit();
					int cityNum = citySharePreferences.getInt(ISPCallBack.SP_CITY_NUM, 1);
					cityNum = cityNum - 1;
					if ((cityNameList.size() > position) || (cityFlagList.size() > position) || (cityCodeList.size() > position)) {
						cityNameList.remove(position);
						cityFlagList.remove(position);
						cityCodeList.remove(position);
					}
					editor.clear().commit();
					editor.putInt(ISPCallBack.SP_CITY_NUM, cityNum);
					for (int i = 0; i < cityNum; i++) {
						editor.putString(ISPCallBack.SP_CITY_NAME + i, cityNameList.get(i));
						editor.putInt(ISPCallBack.SP_CITY_FLAG + i, cityFlagList.get(i));
						editor.putInt(ISPCallBack.SP_CITY_CODE + i, cityCodeList.get(i));
					}
					editor.commit();
					if (null != mICityDeleteListener) {
						if (mICityDeleteListener.onCityDeleteListener(position)) {
							notifyDataSetChanged();
							isDeleting = false;
						}
					} else {
						notifyDataSetChanged();
						isDeleting = false;
					}
				}
			}
		});
		
		return itemView;
	}
	
	public void getCityNames() {
		SharedPreferences citySharePreferences = mContext.getSharedPreferences(ISPCallBack.SHARED_PREFERENCES_FILE_NAME, Activity.MODE_PRIVATE);
		int cityNum = citySharePreferences.getInt(ISPCallBack.SP_CITY_NUM, 1);
//		cityNameList.clear();
//		cityFlagList.clear();
//		cityCodeList.clear();
		cityNameList = new ArrayList<String>();
		cityFlagList = new ArrayList<Integer>();
		cityCodeList = new ArrayList<Integer>();
		for (int i = 0; i < cityNum; i++) {
			cityNameList.add(citySharePreferences.getString(ISPCallBack.SP_CITY_NAME + i, ""));
			cityFlagList.add(citySharePreferences.getInt(ISPCallBack.SP_CITY_FLAG + i, 0));
			cityCodeList.add(citySharePreferences.getInt(ISPCallBack.SP_CITY_CODE + i, 0));
		}
	}
	
	/*private class ViewHolder {
	//private static class ViewHolder {
		TextView cityName;
		Button deleteCity;
		ImageView cityDefaultImg;
		ImageView cityLocationImg;
	}*/
	
}
