package com.prize.prizethemecenter.ui.adapter;


import android.content.Context;
import android.database.DataSetObserver;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.HomeAdBean;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;

import java.util.ArrayList;

/**
 * 主题页滚动条的adapter
 */
public class HomeGalleryFlowAdapter extends BaseAdapter {
	private ArrayList<HomeAdBean> items = new ArrayList<HomeAdBean>();
	public Context ctx;

	/**
	 * @param  : 是否同步加载图片
	 */
	public HomeGalleryFlowAdapter(Context activity) {
		this.ctx = activity;
	}

	public void setData(ArrayList<HomeAdBean> items) {
		if (null == items || 0 == items.size()) {
			return;
		}
		this.items = items;
		notifyDataSetChanged();
	}
	public void addData(ArrayList<HomeAdBean> items) {
		if (items != null) {
			items.addAll(items);
		}
		notifyDataSetChanged();
	}

	public int getItemsSize() {
		if (null == items) {
			return 0;
		}
		return items.size();
	}

	public int getCount() {
		if (items.size() > 0) {
			// 为了循环显示
			return Short.MAX_VALUE;
		} else {
			return 0;
		}
	}

	public HomeAdBean getItem(int position) {
		if (items.size() > 0) {
			return items.get(position % items.size());
		} else {
			return items.get(position);
		}
	}

	public int getItemIndex(int position) {
		if (items.size() > 0) {
			return (position % items.size());
		} else {
			return 0;
		}
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		HomeAdBean item = getItem(position);
		if (null == convertView) {
			convertView = LayoutInflater.from(MainApplication.curContext)
					.inflate(R.layout.activity_home_galleryflow_item, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView
					.findViewById(R.id.recommand_flow_img);
			convertView.setTag(holder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String url = !TextUtils.isEmpty(item.bigImageUrl) ? item.bigImageUrl
				: item.imageUrl;
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.setRotate(false);
		imageLoader.init(ImageLoaderConfiguration.createDefault(ctx));
		imageLoader.displayImage(url, holder.image,
				UILimageUtil.getHomeADCacheUILoptions(), null);
		return convertView;
	}

	private class ViewHolder {
		ImageView image;
	}

	/**
	 * 重写原因 ViewPager在Android4.0上有兼容性错误
	 * ViewPager在移除View时会调用ListView的unregisterDataSetObserver方法
	 * ，而ListView本身也会调用该方法，所以在第二次调用时就会报“The observer is null”错误。
	 * http://blog.csdn.net/guxiao1201/article/details/8818734
	 */
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		if (observer != null) {
			super.unregisterDataSetObserver(observer);
		}
	}

}
