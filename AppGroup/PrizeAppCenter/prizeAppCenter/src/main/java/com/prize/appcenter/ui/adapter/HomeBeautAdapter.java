package com.prize.appcenter.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.Navblocks;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.util.UILimageUtil;

/**
 **
 * 首页宫格adapter
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class HomeBeautAdapter extends BaseAdapter {

	/** 列表项 */
	private ArrayList<Navblocks> items = new ArrayList<Navblocks>();
	private RootActivity context;

	public HomeBeautAdapter(Context context) {
		this.context = (RootActivity) context;

	}

	/**
	 * 设置列表项
	 * 
	 * @param data
	 */
	public void setData(ArrayList<Navblocks> data) {
		if (null == data) {
			return;
		}

		items = data;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		int size = items.size();
		return size;
	}

	@Override
	public Navblocks getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (null == convertView) {
			convertView = LayoutInflater.from(MainApplication.curContext)
					.inflate(R.layout.item_home_beauti_grideview, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.ImageView);
			holder.content_Tv = (TextView) convertView
					.findViewById(R.id.content_Tv);
			holder.title_Tv = (TextView) convertView
					.findViewById(R.id.title_Tv);
			holder.seperator = (View) convertView.findViewById(R.id.seperator);
			convertView.setTag(holder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Navblocks bean = items.get(position);
		holder.image.setAdjustViewBounds(true);
		// holder.image.setMaxHeight(132);
		if (TextUtils.isEmpty(bean.backgroudUrl)) {
			int margin_top = (int) context.getResources().getDimension(
					R.dimen.icon_width);
			MarginLayoutParams mp = new MarginLayoutParams(margin_top,
					margin_top); // item的宽高
			mp.setMargins(0, 0, 25, 0);// 分别是margin_top那四个属性
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					mp);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			holder.image.setLayoutParams(params);
			holder.image.setScaleType(ScaleType.FIT_XY);
			holder.title_Tv.setText(bean.title);
			holder.content_Tv.setText(bean.brief);
			String url = bean.iconUrl;
			ImageLoader.getInstance().displayImage(url, holder.image,
					UILimageUtil.getUILoptions(), null);
			if (position % 2 == 0) {
				holder.seperator.setVisibility(View.VISIBLE);
			} else {
				holder.seperator.setVisibility(View.GONE);
			}

		} else {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
			// params.addRule(RelativeLayout.CENTER_VERTICAL);
			holder.image.setLayoutParams(params);
			holder.image.setBackgroundResource(R.drawable.default_icon);
			holder.image.setScaleType(ScaleType.FIT_XY);
			ImageLoader.getInstance()
					.displayImage(bean.backgroudUrl, holder.image,
							UILimageUtil.getADHalfCacheUILoptions(), null);
			holder.seperator.setVisibility(View.GONE);
		}
		return convertView;
	}

	private static class ViewHolder {
		ImageView image;
		TextView title_Tv;
		TextView content_Tv;
		View seperator;
	}
}
