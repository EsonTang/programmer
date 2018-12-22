/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.music.ui.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.music.R;

public class SearchRecordAdapter extends BaseAdapter {
	private Activity activity;
	private ArrayList<String> list = new ArrayList<String>();

	public SearchRecordAdapter(FragmentActivity activity, ArrayList<String> list) {
		this.activity = activity;
		this.list = list;
	}

	@Override
	public int getCount() {

		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public String getItem(int position) {
		if (position < 0 || list.isEmpty() || position >= list.size()) {
			return null;
		}
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {

		// TODO Auto-generated method stub
		return position;
	}

	public void setList(ArrayList<String> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public void clearData() {
		if (this.list != null) {
			list.clear();
			notifyDataSetChanged();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.item_search_history, null);
			holder.textView = (TextView) convertView
					.findViewById(R.id.textView);
			convertView.setTag(holder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String key = list.get(position);
		if (!TextUtils.isEmpty(key)) {
			holder.textView.setText(key);

		}
		return convertView;
	}

	static class ViewHolder {
		TextView textView;
	}
}
