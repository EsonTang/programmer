package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.HotCategoryBean;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UILimageUtil;

import java.util.ArrayList;

/**
 **
 * 分类顶部adapter
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class CatNoticeAdapter extends BaseAdapter {

	/** 列表项 */
	private ArrayList<HotCategoryBean> hot_topic = new ArrayList<HotCategoryBean>();
	private  Context mContext;
	public CatNoticeAdapter(Context mContext) {
		this.mContext=mContext;
	}

	/**
	 * 设置列表项
	 * 
	 * @param data ArrayList<Navbars>
	 */
	public void setData(ArrayList<HotCategoryBean> data) {
		if (null == data) {
			return;
		}

		hot_topic = data;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return hot_topic.size();
	}

	@Override
	public HotCategoryBean getItem(int position) {
		return hot_topic.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (null == convertView) {
			convertView = LayoutInflater.from(mContext)	.inflate(R.layout.cat_pager_notice_item, null);
			holder = new ViewHolder();
			holder.notice_item_Tv = (TextView) convertView
					.findViewById(R.id.notice_item_Tv);
			holder.image = (ImageView) convertView
					.findViewById(R.id.notice_item_iv);
			convertView.setTag(holder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		HotCategoryBean bean = hot_topic.get(position);
		holder.notice_item_Tv.setText(bean.title);
		ImageLoader.getInstance().displayImage(bean.iconUrl, holder.image,
				UILimageUtil.getUILoptions(), null);
		return convertView;
	}

	private static class ViewHolder {
		ImageView image;
//		ImageView notice_Tag_iv;
		TextView notice_item_Tv;
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
