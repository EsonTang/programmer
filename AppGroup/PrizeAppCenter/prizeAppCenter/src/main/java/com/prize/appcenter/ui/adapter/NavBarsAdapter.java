package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.HomeAdBean;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.GiftCenterActivity;
import com.prize.appcenter.activity.TopicDetailActivity;
import com.prize.appcenter.activity.WebViewActivity;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;

import java.util.ArrayList;

/**
 **
 * 推荐位（eg:装机必备，热门主题，上线新品等）adapter
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class NavBarsAdapter extends BaseAdapter {

	private Activity context;
	/** 列表项 */
	private ArrayList<HomeAdBean> items = new ArrayList<HomeAdBean>();

	public NavBarsAdapter(Activity context) {
		this.context = context;
	}

	/**
	 * 设置列表项
	 * 
	 * @param data
	 */
	public void setData(ArrayList<HomeAdBean> data) {
		if (null == data) {
			return;
		}

		items = data;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		int size = items.size();

		if (size > 2) {
			size = 2;
		}
		return size;
	}

	@Override
	public HomeAdBean getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (null == convertView) {
			convertView = LayoutInflater.from(context)
					.inflate(R.layout.nav_imageview, parent, false);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.img_id);
			convertView.setTag(holder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		HomeAdBean homeAdBean = items.get(position);
		String url = homeAdBean.imageUrl;
		ImageLoader.getInstance().displayImage(url, holder.image,
				UILimageUtil.getUINewAppHeader(), null);

		return convertView;
	}

	public void onItemClic(HomeAdBean homeAdBean, int position) {
		if (homeAdBean.adType != null && "topic".equals(homeAdBean.adType)) {// 属于专题
			com.prize.app.beans.TopicItemBean bean = new com.prize.app.beans.TopicItemBean();
			bean.description = homeAdBean.description;
			bean.title = homeAdBean.title;
			bean.imageUrl = homeAdBean.imageUrl;
			bean.id = homeAdBean.associateId;

			Intent intent = new Intent(context, TopicDetailActivity.class);
			Bundle b = new Bundle();
			b.putSerializable("bean", bean);
			intent.putExtras(b);
			context.startActivity(intent);
			// context.overridePendingTransition(R.anim.fade_in,
			// R.anim.fade_out);
			return;
		}
		if ("web".equals(homeAdBean.adType)) {// 属于网页
			if (!TextUtils.isEmpty(homeAdBean.url)) {
				// @prize { added by fanjunchen
				Intent intent = new Intent(context, WebViewActivity.class);
				intent.putExtra(WebViewActivity.P_URL, homeAdBean.url);
				intent.putExtra(WebViewActivity.P_APP_ID,
						homeAdBean.associateId);
				context.startActivity(intent);
				// context.overridePendingTransition(R.anim.fade_in,

				// R.anim.fade_out);
				// @prize }
				return;
			}

		}
		if ("app".equals(homeAdBean.adType)) {
			UIUtils.gotoAppDetail(homeAdBean.associateId,context);
		}

		if ("cat_app".equals(homeAdBean.adType)) {// 应用page的分类
		// Intent intent = new Intent(MainApplication.curContext,
		// AppCategoryActivity.class);
		// MainApplication.curContext.startActivity(intent);
			return;
		}
		if ("giftcenter".equals(homeAdBean.adType)) {// 礼包（2.2版本）
			Intent intent = new Intent(context, GiftCenterActivity.class);
			intent.putExtra(GiftCenterActivity.TOPICIDKEY,homeAdBean.associateId);
			context.startActivity(intent);
			return;
		}
	}

	private static class ViewHolder {
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
