package com.prize.appcenter.ui.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.TopicItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.util.UILimageUtil;

/**
 * 专题列表适配器
 * 
 * @authorlongbaoxiu
 * 
 */
public class TopicListAdapter extends GameListBaseAdapter {
	private ArrayList<TopicItemBean> items = new ArrayList<TopicItemBean>();
	protected Activity activity;

	public TopicListAdapter(RootActivity activity) {
		super(activity);
		this.activity = activity;
	}

	/**
	 * 添加新游戏列表到已有集合中
	 */
	public void addData(ArrayList<TopicItemBean> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}

	/**
	 * 清空游戏列表
	 */
	public void clearAll() {
		if (items != null) {
			items.clear();
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public TopicItemBean getItem(int position) {
		if (position < 0 || items.isEmpty() || position >= items.size()) {
			return null;
		}
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.activity_topic_listview_item, null);
			viewHolder = new ViewHolder();
			viewHolder.gameIcon = (ImageView) convertView
					.findViewById(R.id.game_iv);
			viewHolder.gameName = (TextView) convertView
					.findViewById(R.id.game_name_tv);
			viewHolder.gameComment = (TextView) convertView
					.findViewById(R.id.game_desc);
			convertView.setTag(viewHolder);
			super.getView(position, convertView, parent);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		TopicItemBean gameBean = items.get(position);
		if (null == gameBean) {
			return convertView;
		}

		if ((gameBean.imageUrl != null)) {
			ImageLoader imageLoader = ImageLoader.getInstance();
//			imageLoader.setRotate(false);
			imageLoader.displayImage(gameBean.imageUrl, viewHolder.gameIcon,
					UILimageUtil.getTopicListUILoptions(), null);
		}

		if (gameBean.title != null) {
			viewHolder.gameName.setText(gameBean.title);
		} else {
			viewHolder.gameName.setText("");
		}

		String comment = gameBean.createTime;
		if (!TextUtils.isEmpty(comment)) {
			viewHolder.gameComment.setText(comment);
		}

		return convertView;
	}

	static class ViewHolder {
		// 专题图标
		ImageView gameIcon;
		// 专题名称
		TextView gameName;
		// 专题介绍
		TextView gameComment;

	}

}
