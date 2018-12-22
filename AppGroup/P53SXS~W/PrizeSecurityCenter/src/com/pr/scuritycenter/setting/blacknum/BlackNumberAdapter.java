package com.pr.scuritycenter.setting.blacknum;

import java.util.List;

import com.pr.scuritycenter.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 
 * @author wangzhong
 *
 */
public class BlackNumberAdapter extends BaseAdapter {
	
	private Context mContext;
	private List<BlackNumberBean> mListBlackNumbers;
	
	public BlackNumberAdapter(Context context, List<BlackNumberBean> blackNumbers) {
		super();
		this.mContext = context;
		this.mListBlackNumbers = blackNumbers;
	}

	@Override
	public int getCount() {
		return mListBlackNumbers == null ? 0 : mListBlackNumbers.size();
	}

	@Override
	public Object getItem(int position) {
		return mListBlackNumbers == null ? null : mListBlackNumbers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.black_numbers_list_item, null);
			holder = new ViewHolder();
			holder.tv_number = (TextView) convertView.findViewById(R.id.tv_safe_intercept_item_name);
			holder.tv_mode = (TextView) convertView.findViewById(R.id.tv_safe_intercept_item_mode);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		BlackNumberBean blackNumberBean = (BlackNumberBean) getItem(position);
		holder.tv_number.setText(blackNumberBean.getNumber());
		switch (blackNumberBean.getMode()) {
		case BlackNumberBean.STOP_SMS:
			holder.tv_mode.setText("拦截短信");
			break;
		case BlackNumberBean.STOP_CALL:
			holder.tv_mode.setText("拦截电话");
			break;
		case BlackNumberBean.STOP_ALL:
			holder.tv_mode.setText("拦截全部");
			break;

		default:
			break;
		}
		
		return convertView;
	}

	private static class ViewHolder {
		TextView tv_number;
		TextView tv_mode;
	}
	
}
