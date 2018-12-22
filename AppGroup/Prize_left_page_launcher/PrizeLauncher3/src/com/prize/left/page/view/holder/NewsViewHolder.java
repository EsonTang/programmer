package com.prize.left.page.view.holder;

import java.util.List;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.bean.NewsCardItem;
import com.prize.left.page.response.HeadNewsResponse;
import com.prize.left.page.ui.AnimFrameLayout;
import com.prize.left.page.util.CommonUtils;
/**
 * 新闻Holder
 */
public class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
	public TextView titleTxt;

	public ImageView imgRefresh;
	
	public ImageView imgMenu;
	
	public LinearLayout contents;
	
	private LayoutInflater mInflater = null;
	
	private List<NewsCardItem> datas = null;

	private int pos = -1;
	/**下方的更多*/
	public TextView txtMore;
	
	/**图片配置器*/
	private ImageOptions leftImgOption = new ImageOptions.Builder()
    	.setSize(100, 100)
    	//.setRadius(4)
    	//.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
    	.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
    	.build();
	
	/**图片配置器*/
	private ImageOptions imgOption = new ImageOptions.Builder()
    	.setSize(720, 400)
    	//.setRadius(4)
    	//.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
    	.setImageScaleType(ImageView.ScaleType.FIT_XY)
    	.build();
	
	public NewsViewHolder(View v) {
		super(v);
		Context ctx = v.getContext();
		titleTxt = (TextView) v.findViewById(R.id.txt_title);
		imgRefresh = (ImageView) v.findViewById(R.id.img_refresh);
		imgMenu = (ImageView) v.findViewById(R.id.img_more);
		contents = (LinearLayout) v.findViewById(R.id.content);
		txtMore = (TextView) v.findViewById(R.id.txt_footer_more);
		
		mInflater = LayoutInflater.from(ctx);
		
		imgRefresh.setOnClickListener(this);
		
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.img_refresh:
				String json = v.getContext().getString(R.string.news_refresh);
				HeadNewsResponse rsp = CommonUtils.getObject(json, HeadNewsResponse.class);
				setDatas(rsp.data.items);
				break;
		}
	}
	/***
	 * 设置美食(团购)数据
	 * @param ls
	 */
	public void setDatas(List<NewsCardItem> ls) {
		if (null == ls || ls.size() < 1)
			return;
		datas = ls;
		int childSz = contents.getChildCount();
		int dataSize = datas.size();
		
		if (dataSize < childSz) { // 先删除多余的VIEW
			for (int i = dataSize; i < childSz; i++) {
				contents.removeViewAt(dataSize);
			}
		}
		int startIndex = 0;
		// 若有需要 替换VIEW
		int min = Math.min(dataSize, childSz);
		if (min > 0) {
			for (; startIndex < min; startIndex ++) {
				AnimFrameLayout frame = (AnimFrameLayout)contents.getChildAt(startIndex);
				View itemView = null;
				if (startIndex == 0)
					itemView = mInflater.inflate(R.layout.news_first_item, null);
				else
					itemView = mInflater.inflate(R.layout.news_second_item, null);
				
				initItemView(itemView, startIndex);
				frame.replaceView(itemView, startIndex);
			}
		}
		
		for (; startIndex < dataSize; startIndex ++) {
			AnimFrameLayout frame = (AnimFrameLayout)mInflater.inflate(R.layout.item_base, null);
			
			View itemView = null;
			if (startIndex == 0)
				itemView = mInflater.inflate(R.layout.news_first_item, null);
			else
				itemView = mInflater.inflate(R.layout.news_second_item, null);
			
			initItemView(itemView, startIndex);
			
			frame.replaceView(itemView, startIndex);
			
			contents.addView(frame);
		}
		/*if (mAdapter != null && pos != -1)
			mAdapter.notifyItemChanged(pos);*/
	}
	/***
	 * 绑定指定的VIEW
	 * @param v
	 * @param data
	 */
	private void initItemView(View v, int pos) {
		NewsCardItem data = datas.get(pos);
		// 左边图片
		ImageView imgLeft = (ImageView)v.findViewById(R.id.img_left);
		String url = (String)imgLeft.getTag();
		// 标题或名称
		TextView txtName = (TextView)v.findViewById(R.id.txt_title);
		txtName.setText(data.name);
		if (pos != 0) {
			// 说明或摘要
			TextView txtSubstract = (TextView)v.findViewById(R.id.txt_substract);
			txtSubstract.setText(data.describe);
			
			if (null == url || !url.equals(data.iconUrl)) {
				imgLeft.setTag(data.iconUrl);
				x.image().bind(imgLeft, data.iconUrl, leftImgOption);
			}
		}
		else {
			if (null == url || !url.equals(data.iconUrl)) {
				imgLeft.setTag(data.iconUrl);
				x.image().bind(imgLeft, data.iconUrl, imgOption);
			}
		}
	}
	/***
	 * 获取美食(团队)项数据
	 */
	public List<NewsCardItem> getDatas() {
		return datas;
	}
	/**
	 * 加载图片
	 */
	public void doBindImg() {
		int childSz = contents.getChildCount();
		for (int i=0; i < childSz; i ++) {
			AnimFrameLayout frame = (AnimFrameLayout)contents.getChildAt(i);
			ImageView imgLeft = (ImageView)frame.getChildAt(0).findViewById(R.id.img_left);
			if (imgLeft != null && imgLeft.getTag() != null) {
				String url = (String)imgLeft.getTag();
				if (i != 0)
					x.image().bind(imgLeft, url, leftImgOption);
				else
					x.image().bind(imgLeft, url, imgOption);
			}
		}
	}
}
