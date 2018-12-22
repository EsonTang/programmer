package com.prize.music.ui.adapters.list;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.music.R;
import com.prize.music.ui.adapters.list.NewListAdapter.NewViewHolder;

public class EditActivityAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Map<String, Object>> mData;
	public static Map<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();;

	public EditActivityAdapter(Context context, List<Map<String, Object>> data,
			Boolean boo) {
		mInflater = LayoutInflater.from(context);
		mData = data;
		init(boo);
	}

	// 初始化
	public void init(Boolean boo) {
		// 这儿定义isSelected这个map是记录每个listitem的状态，初始状态全部为false。
		isSelected.clear();
		for (int i = 0; i < mData.size(); i++) {
			isSelected.put(i, boo);
		}
	}

	@Override
	public int getCount() {
		return mData.size();
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
	public View getView(int position, View convertView, ViewGroup parent) {
		EditViewHolder holder = null;
		// convertView为null的时候初始化convertView。
		if (convertView == null) {
			holder = new EditViewHolder();
			convertView = mInflater.inflate(R.layout.edit_activity_item, null);
			holder.title = (TextView) convertView.findViewById(R.id.edit_title);
			holder.img = (ImageView) convertView.findViewById(R.id.eidt_img);
			holder.cb = (CheckBox) convertView.findViewById(R.id.edit_check);
			convertView.setTag(holder);
		} else {
			holder = (EditViewHolder) convertView.getTag();
		}
		holder.img.setBackgroundResource(R.drawable.new_list);
		holder.title.setText(mData.get(position).get("title").toString());
		holder.cb.setChecked(isSelected.get(position));
		return convertView;
	}

	public final class EditViewHolder {
		public TextView title;
		public ImageView img;
		public CheckBox cb;
	}

}
