package com.prize.weather.detail;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.weather.R;
import com.prize.weather.framework.MyBaseAdapter;
import com.prize.weather.util.CalendarUtils;
import com.prize.weather.util.WeatherImageUtils;

/**
 * 
 * @author wangzhong
 *
 */
@SuppressLint("InflateParams")
public class WeatherFutureListItemAdapter extends MyBaseAdapter {
	
	private ViewHolder holder;
	
	public WeatherFutureListItemAdapter(Context mContext,
			List<? extends Object> list) {
		super(mContext, list);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		WeatherFuture7DayBean forecastEntity = (WeatherFuture7DayBean) getItem(position);
		if (null == convertView) {
			convertView = layoutInflater.inflate(R.layout.weather_future_listitem, null);
			holder = new ViewHolder();
			holder.future_date = (TextView) convertView.findViewById(R.id.future_date);
			holder.future_day = (TextView) convertView.findViewById(R.id.future_day);
			holder.future_icon = (ImageView) convertView.findViewById(R.id.future_icon);
			holder.future_temp = (TextView) convertView.findViewById(R.id.future_temp);
			holder.future_select = (ImageView) convertView.findViewById(R.id.future_select);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if ((position == 1) || (position == 0)) {
			//holder.future_select.setVisibility(View.VISIBLE);
			holder.future_select.setVisibility(View.INVISIBLE);
		} else {
			holder.future_select.setVisibility(View.INVISIBLE);
		}
		holder.future_date.setText(forecastEntity.getDate());
		holder.future_day.setText(forecastEntity.getDay());
		holder.future_temp.setText(forecastEntity.getLow() + "~" + forecastEntity.getHigh() + "℃");
		WeatherImageUtils.setWeatherImage(Integer.valueOf(forecastEntity.getCode()), holder.future_icon, CalendarUtils.isDayTime(), 3);
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView future_date;
		TextView future_day;
		ImageView future_icon;
		TextView future_temp;
		ImageView future_select;
	}
	
}
