package com.prize.left.page.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.launcher3.R;
import com.prize.left.page.bean.table.SubCardType;
import com.prize.left.page.ui.StatusTextView;
/***
 * 频道适配器
 * @author fanjunchen
 *
 */
public class ChannelAdapter extends BaseAdapter {
	
	private List<SubCardType> datas = null;
	
	private LayoutInflater mInflater;
	
	private List<String> selCodes = null;
	
	private Context mCtx;
	
	public ChannelAdapter(Context ctx) {
		mCtx = ctx;
		mInflater = LayoutInflater.from(mCtx);
	}
	
	public void setSelCodes(List<String> codes) {
		selCodes = codes;
	}
	/***
	 * 设置数据, 调用此方法前先调用setSelCodes方法
	 * @param codes
	 */
	public void setData(List<SubCardType> codes) {
		datas = codes;
	}
	
	@Override
	public int getCount() {
		return datas != null ? datas.size() : 0;
	}

	@Override
	public SubCardType getItem(int pos) {
		return datas != null ? datas.get(pos) : null;
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		ChannelHolder holder = null;
		if (null == convertView) {
			holder = new ChannelHolder();
			convertView = mInflater.inflate(R.layout.channel_item, null);
			holder.txtName = (StatusTextView)convertView.findViewById(R.id.txt_name);
			convertView.setTag(holder);
		}
		else {
			holder = (ChannelHolder)convertView.getTag();
		}
		
		SubCardType item = datas.get(pos);
		holder.txtName.setText(item.name);
		
		if (selCodes != null && selCodes.contains(String.valueOf(item.code))) {
			holder.txtName.setSel(true);
		}
		else
			holder.txtName.setSel(false);
		return convertView;
	}
	
	static class ChannelHolder {
		
		public StatusTextView txtName;
	}
}
