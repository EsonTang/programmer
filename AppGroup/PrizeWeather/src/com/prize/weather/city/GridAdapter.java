package com.prize.weather.city;

import java.util.ArrayList;
import java.util.List;

import com.prize.weather.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter{

	private Context mContext = null;
	private List<String> mItems = new ArrayList<String>();
	private int resourceId;
	private int selectedPosition = -1;

	public GridAdapter(Context context, int res){
		mContext = context;
		resourceId = res;
	}		

	public void addItem(String item){
		mItems.add(item);
	}
	
	public void removeItem(int position){
		mItems.remove(position);
	}
	
	public void setListItems(List<String> list){
		mItems = list;
	}
		
	public void setSelectedPosition(int position){
		selectedPosition = position;
	}
	
	public int getCount(){
		return mItems.size();
	}
	
	public String getItem(int position){
		return mItems.get(position);
	}
	
	public long getItemId(int position){
		return position;
	}
	
	
	public View getView(int position, View convertView, ViewGroup parent){
		View itemView = convertView;
		GridViewWrapper wrapper = null;
		TextView city = null;
				
		if(itemView == null){
			itemView = LayoutInflater.from(mContext).inflate(resourceId, parent, false);
			wrapper = new GridViewWrapper(itemView);
			itemView.setTag(wrapper);			
			city = wrapper.getCityTextView();
		}else{
			wrapper = (GridViewWrapper)itemView.getTag();
			city = wrapper.getCityTextView();
		}
		
		if(position<mItems.size() &&position >=0 ){
			city.setText(mItems.get(position));	
		}			
		return itemView;
	}
	
	class GridViewWrapper{
		View base;	
		TextView city= null;
		GridViewWrapper(View base){
			this.base = base;
		}
		
		public TextView getCityTextView(){
			if(city == null)
				city = (TextView)base.findViewById(R.id.itemCity);
			return city;
		}
	}
}


