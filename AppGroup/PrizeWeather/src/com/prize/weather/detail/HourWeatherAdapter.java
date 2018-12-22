package com.prize.weather.detail;

import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.weather.R;
import com.prize.weather.util.CalendarUtils;
import com.prize.weather.util.WeatherImageUtils;

@SuppressLint({ "ViewHolder", "InflateParams" })
public class HourWeatherAdapter extends BaseAdapter {
	
	private ViewHolder holder;
	private Context mContext;
	public List<Map<String, Object>> list;
	
	public HourWeatherAdapter(Context context, List<Map<String, Object>> list) {
		this.mContext = context;
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Map<String, Object> getItem(int position) {
		return list.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Map<String, Object> map = getItem(position);
		
		if (null == convertView) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.weather_hour_listitem, null);
			holder = new ViewHolder();
			holder.hour_weather_time = (TextView) convertView.findViewById(R.id.hour_weather_time);
			holder.hour_weather_icon = (ImageView) convertView.findViewById(R.id.hour_weather_icon);
			holder.hour_weather_temp = (TextView) convertView.findViewById(R.id.hour_weather_temp);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.hour_weather_time.setText(map.get("hourTime").toString() + ":00");
		WeatherImageUtils.setWeatherImage(Integer.valueOf((String) map.get("hourIcon")), 
				holder.hour_weather_icon, CalendarUtils.isDayTime(), 3);
		holder.hour_weather_temp.setText(map.get("hourTemp").toString() + "℃");
		return convertView;
	}
	
	private static class ViewHolder {
		TextView hour_weather_time;
		ImageView hour_weather_icon;
		TextView hour_weather_temp;
	}
	
}
