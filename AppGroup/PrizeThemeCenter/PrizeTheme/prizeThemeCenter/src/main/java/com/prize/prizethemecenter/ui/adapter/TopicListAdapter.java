package com.prize.prizethemecenter.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.TopicData.TopicBean;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.widget.CornerImageView;

import java.util.ArrayList;

/**
 * 专题列表适配器
 * @author pengy
 * 
 */
public class TopicListAdapter extends BaseAdapter {
	private ArrayList<TopicBean> items = new ArrayList<>();
	protected Context context;

	public TopicListAdapter(Context activity) {
		this.context = activity;
	}

	public void addData(ArrayList<TopicBean> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}

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
	public TopicBean getItem(int position) {
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
			convertView = LayoutInflater.from(context).inflate(
					R.layout.topic_item_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.topic_tv = (CornerImageView) convertView
					.findViewById(R.id.topic_tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		TopicBean topciBean = items.get(position);
//		String url = !TextUtils.isEmpty(topciBean.big_image) ? topciBean.big_image
//				: topciBean.image;
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.setRotate(false);
		imageLoader.init(ImageLoaderConfiguration.createDefault(context));
		imageLoader.displayImage(topciBean.image, viewHolder.topic_tv,
				UILimageUtil.getTopicLoptions(), null);
		return convertView;
	}

	static class ViewHolder {
		CornerImageView topic_tv;
	}
}
