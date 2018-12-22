package com.android.settings;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TextView;

public class RadioAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<String> pathnames;
	private viewHolder holder;
	private int index = -1;
	private Context content;

	public RadioAdapter(Context content, List<String> list,int index) {
		super();
		this.content = content;
		this.pathnames = list;
		this.index = index;
		inflater = LayoutInflater.from(content);
	}
	public void setIndex(int index){
		this.index = index;
	}

	@Override
	public int getCount() {
		return pathnames.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		holder = new viewHolder();
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.prize_lucky_money_notice_sound_item, null);
			holder.nameTxt = (TextView) convertView.findViewById(R.id.item);
			holder.selectBtn = (RadioButton) convertView.findViewById(R.id.radio_btn);
			convertView.setTag(holder);
		} else {
			holder = (viewHolder) convertView.getTag();
		}

		holder.nameTxt.setText(pathnames.get(position));
		holder.selectBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					index = position;
					notifyDataSetChanged();
				}
			}
		});

		if (index == position) {
			holder.selectBtn.setChecked(true);
		} else {
			holder.selectBtn.setChecked(false);
		}
		return convertView;
	}

	public class viewHolder {
		public TextView nameTxt;
		public RadioButton selectBtn;
	}
}
