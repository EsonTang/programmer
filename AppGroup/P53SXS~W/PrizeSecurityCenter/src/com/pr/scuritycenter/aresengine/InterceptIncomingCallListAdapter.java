package com.pr.scuritycenter.aresengine;

import java.util.List;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.pr.scuritycenter.R;
import com.pr.scuritycenter.framework.MyBaseAdapter;

/**
 * 
 * @author wangzhong
 *
 */
public class InterceptIncomingCallListAdapter extends MyBaseAdapter {

	public InterceptIncomingCallListAdapter(LayoutInflater layoutInflater,
			List<? extends Object> listData) {
		super(layoutInflater, listData);
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		InterceptIncomingCallBean interceptIncomingCallBean = (InterceptIncomingCallBean) getItem(position);
		
		ViewHolder holder;
		if (null == convertView) {
			convertView = layoutInflater.inflate(R.layout.intercept_phone_list_item, null);
			holder = new ViewHolder();
			holder.tv_intercept_num = (TextView) convertView.findViewById(R.id.tv_intercept_num);
			holder.tv_intercept_time = (TextView) convertView.findViewById(R.id.tv_intercept_time);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (interceptIncomingCallBean != null) {
			holder.tv_intercept_num.setText(interceptIncomingCallBean.getNumber());
			holder.tv_intercept_time.setText(interceptIncomingCallBean.getTime());
		}
		return convertView;
	}

	private static class ViewHolder {
		TextView tv_intercept_num;
		TextView tv_intercept_time;
	}

}
