package com.prize.music.ui.adapters;

import java.util.ArrayList;

import com.prize.music.R;
import com.prize.music.bean.PopBean;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainPopAdapter extends BaseAdapter {

	private ArrayList<PopBean> list = new ArrayList<PopBean>();
	private Activity activity = null;
	private Handler handler;

	public MainPopAdapter(Activity activity, Handler handler,
			ArrayList<PopBean> list) {
		this.activity = activity;
		this.list = list;
		this.handler = handler;
		init();
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private OnItemClickListener mOnItemClickListener;
	private OnClickListener onClickListener;
	private int selectedPos = -1;
	private String selectedText = "";

	private void init() {
		onClickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				selectedPos = (Integer) view.getTag();
				setSelectedPosition(selectedPos);
				if (mOnItemClickListener != null) {

					mOnItemClickListener.onItemClick(view, selectedPos);
				}
				Message msg = new Message();
				Bundle data = new Bundle();
				data.putLong("selIndex",
						Long.parseLong(list.get(selectedPos).getAreaId()));
				data.putString("tabelName", list.get(selectedPos).getAreaName());
				msg.setData(data);
				msg.what = 1;
				handler.sendMessage(msg);
			}
		};
	}

	/**
	 * 设置选中的position,并通知列表刷新
	 */
	public void setSelectedPosition(int pos) {
		if (list != null && pos < list.size()) {
			selectedPos = pos;
			selectedText = list.get(pos).getAreaName();
			notifyDataSetChanged();
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = (RelativeLayout) LayoutInflater.from(activity).inflate(
				R.layout.mainpopupwindow_options_item, null);
		RelativeLayout rl = (RelativeLayout) view
				.findViewById(R.id.shop_area_rl);
		TextView textView = (TextView) view.findViewById(R.id.item_text_tv);

		view.setTag(position);
		String mString = list.get(position).getAreaName();
		textView.setText(list.get(position).getAreaName());
		if (selectedText != null && selectedText.equals(mString)) {
			// rl.setBackgroundColor(activity.getResources().getColor(R.color.bg_searche_pressed));
			// textView.setTextColor(activity.getResources().getColor(
			// R.color.actionbar_text));
		} else {
			rl.setSelected(false);
		}
		view.setOnClickListener(onClickListener);
		return view;
	}

	/**
	 * 重新定义菜单选项单击接口
	 */
	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

}
