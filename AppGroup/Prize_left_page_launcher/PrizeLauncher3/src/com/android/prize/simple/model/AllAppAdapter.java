package com.android.prize.simple.model;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.prize.simple.table.ItemTable;

public class AllAppAdapter extends BaseAdapter {
	
	List<ItemTable> datas = null;
	
	LayoutInflater mInflater = null;
	
	PagedDataModel pModel = null;
	
	public AllAppAdapter(Context ctx) {
		mInflater = LayoutInflater.from(ctx);
		pModel = PagedDataModel.getInstance();
	}
	
	public void setData(List<ItemTable> d) {
		datas = d;
		notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return datas != null ? datas.size() : 0;
	}

	@Override
	public ItemTable getItem(int pos) {
		// TODO Auto-generated method stub
		return datas != null ? datas.get(pos) : null;
	}

	@Override
	public long getItemId(int pos) {
		// TODO Auto-generated method stub
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		AppHolder holder = null;
		if (null == convertView) {/*
			holder = new AppHolder();
			convertView = mInflater.inflate(R.layout.simple_list_app_item, null);
			holder.leftImg = (ImageView)convertView.findViewById(R.id.img_left);
			holder.icImg = (ImageView)convertView.findViewById(R.id.img_ico);
			holder.txtName = (TextView)convertView.findViewById(R.id.txt_name);
			
			convertView.setTag(holder);
		*/}
		else {
			holder = (AppHolder)convertView.getTag();
		}
		
		ItemTable item = datas.get(pos);
		
		holder.txtName.setText(item.title);
		
		if (pModel != null) {
			Drawable d = pModel.iconCache.get(item.clsName);
			
			if (d == null)
				d = pModel.iconCache.get(PagedDataModel.DEFAULT);
			if (d != null) {
				holder.leftImg.setImageDrawable(d);
			}
			
			if (item.bgResId > 0) {
				holder.leftImg.setBackgroundResource(item.bgResId);
			}
			else {
				item.bgResId = pModel.getAppBgId();
				holder.leftImg.setBackgroundResource(item.bgResId);
			}
			
		}
		convertView.setEnabled(true);
		if (!item.canDel) {
//			holder.icImg.setImageResource(R.drawable.simple_icon_del_disabled);
			convertView.setEnabled(false);
		}
		else if(item.isExist) {
			
		}
//			holder.icImg.setImageResource(R.drawable.simple_icon_del);
//		else
//			holder.icImg.setImageResource(R.drawable.simple_icon_add);
		return convertView;
	}
	
	static class AppHolder {
		ImageView icImg, leftImg;
		TextView txtName;
	}
}
