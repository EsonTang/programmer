package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.appcenter.R;

/**
 * 
 ** 
 * 消息中心adapter
 * 
 * @author zhouerlong
 * @version V1.0
 */
public class SignInRuleAdapter extends BaseAdapter {

	private String[] rules;
	private Context ctx;

	public SignInRuleAdapter(String[] items, Context c) {
		rules = items;
		ctx = c;
	}

	@Override
	public int getCount() {
		if (rules == null) {
			return 0;
		}
		return rules.length;
	}

	@Override
	public String getItem(int position) {
		return rules[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		String rule = rules[position];
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(ctx).inflate(
					R.layout.signin_rules_item, null);
			viewHolder.rule = (TextView) convertView.findViewById(R.id.sign_in_rule_tv);
			viewHolder.dot = (ImageView) convertView.findViewById(R.id.sign_in_rule_iv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.dot.setBackgroundResource(R.drawable.sign_in_rule_dot);
		viewHolder.rule.setText(rule);

		return convertView;

	}

	private class ViewHolder {
		public ImageView dot;
		public TextView rule;
	}
}
