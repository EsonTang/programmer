package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;

import com.prize.app.util.JLog;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;

/**
 * 应用举报的原因adapter
 * 
 * @author prize
 * 
 */
public class FeedBackReasonAdapter extends GameListBaseAdapter {
	private String[] items = new String[] {};
	protected Activity activity;
	/** 存储每个条目勾选的状态 */

	private String complainType;

	public FeedBackReasonAdapter(RootActivity activity) {
		super(activity);
		this.activity = activity;
	}

	/**
	 * 设置游戏列表集合,注意直接替换数据类型的,故需要注意数据是在UI线程
	 */
	public void setData(String[] data) {
		if (data != null && data.length > 0) {
			items = data;
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return items.length;
	}

	@Override
	public String getItem(int position) {
		if (position < 0 || position >= items.length) {
			return null;
		}
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		// if (convertView == null) {
		// convertView = new CheckBox(activity);
		// convertView.setBackgroundResource(R.drawable.radiobtn_selector);
		convertView = LayoutInflater.from(activity).inflate(R.layout.checkbox,
				null);
		// convertView = LayoutInflater.from(activity).inflate(
		// R.layout.activity_game_listview_item, null);
		viewHolder = new ViewHolder();
		viewHolder.checkBox = (CheckBox) convertView;
		// viewHolder.checkBox.requestFocus();
		// } else {
		// viewHolder = (ViewHolder) convertView.getTag();
		// }
		String collectionBean = items[position];
		if (null == collectionBean) {
			return convertView;
		}
		if (!TextUtils.isEmpty(complainType)
				&& complainType.equals(collectionBean)) {
			viewHolder.checkBox.setChecked(true);
		} else {
			viewHolder.checkBox.setChecked(false);
		}
		LayoutParams lp = new GridView.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		viewHolder.checkBox.setLayoutParams(lp);
		viewHolder.checkBox.setPadding(20, 10, 20, 10);
		viewHolder.checkBox.setButtonDrawable(null);
		viewHolder.checkBox.requestFocus();
		viewHolder.checkBox.setText(collectionBean);
		viewHolder.checkBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							complainType = viewHolder.checkBox.getText()
									.toString();
						}
						if(mResOnItemClick !=null){
							mResOnItemClick.callBack(complainType);
						}
						notifyDataSetChanged();
					}
				});
		return convertView;
	}

	static class ViewHolder {
		// 游戏图标
		CheckBox checkBox;

	}

	public String getChooseString() {
		return complainType;
	}

	private ResOnItemClick mResOnItemClick;

	public void setmResOnItemClick(ResOnItemClick mResOnItemClick) {
		this.mResOnItemClick = mResOnItemClick;
	}

	public interface ResOnItemClick {
		void callBack(String value);
	}
}
