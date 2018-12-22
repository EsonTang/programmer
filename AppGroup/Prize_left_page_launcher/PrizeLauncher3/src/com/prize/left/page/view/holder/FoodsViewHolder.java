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
import com.prize.left.page.bean.FoodsCardItem;
import com.prize.left.page.response.FoodsResponse;
import com.prize.left.page.ui.AnimFrameLayout;
import com.prize.left.page.util.CommonUtils;

/**
 * 美食(团购)Card Holder
 */
public class FoodsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
	/**卡片标题*/
	public TextView titleTxt;
	/**删除按钮*/
	public ImageView imgRefresh;
	/**菜单/更多 按钮*/
	public ImageView imgMenu;
	/**导航容器*/
	public LinearLayout contents;
	/**下方的更多*/
	public TextView txtMore;
	
	private LayoutInflater mInflater = null;
	
	private List<FoodsCardItem> datas = null;
	
	private RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;
	
	private int pos = -1;
	
	/**图片配置器*/
	private ImageOptions imgOption = new ImageOptions.Builder()
    	.setSize(100, 100)
    	//.setRadius(4)
    	//.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
    	.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
    	.build();

	public FoodsViewHolder(View v) {
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
				String json = v.getContext().getString(R.string.foods_refresh);
				FoodsResponse rsp = CommonUtils.getObject(json, FoodsResponse.class);
				setDatas(rsp.data.items);
				break;
		}
	}
	
	public void setAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> ad, int pos) {
		mAdapter = ad;
		this.pos = pos;
	}
	/***
	 * 设置美食(团购)数据
	 * @param ls
	 */
	public void setDatas(List<FoodsCardItem> ls) {
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
				View itemView = mInflater.inflate(R.layout.food_content_item, null);
				initItemView(itemView, datas.get(startIndex));
				frame.replaceView(itemView, startIndex);
			}
		}
		
		for (; startIndex < dataSize; startIndex ++) {
			AnimFrameLayout frame = (AnimFrameLayout)mInflater.inflate(R.layout.item_base, null);
			
			View itemView = mInflater.inflate(R.layout.food_content_item, null);
			initItemView(itemView, datas.get(startIndex));
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
	private void initItemView(View v, FoodsCardItem data) {
		// 左边图片
		ImageView imgLeft = (ImageView)v.findViewById(R.id.img_food_ico);
		String url = (String)imgLeft.getTag();
		if (null == url || !url.equals(data.iconUrl)) {
			imgLeft.setTag(data.iconUrl);
			x.image().bind(imgLeft, data.iconUrl, imgOption);
		}
		// 标题或名称
		TextView txtName = (TextView)v.findViewById(R.id.txt_name);
		txtName.setText(data.name);
		
		// 是否支持(票,套餐)之类的东东
		TextView txtIcon = (TextView)v.findViewById(R.id.txt_icon);
				
		// 说明或摘要
		TextView txtSubstract = (TextView)v.findViewById(R.id.txt_substract);
		txtSubstract.setText(data.describe);
		
		// 价格
		TextView txtPrice = (TextView)v.findViewById(R.id.txt_price);
		txtPrice.setText(data.price);
		
		// 距离或其他
		TextView txtHint = (TextView)v.findViewById(R.id.txt_hint);
		txtHint.setText(data.grade);
	}
	/***
	 * 获取美食(团队)项数据
	 */
	public List<FoodsCardItem> getDatas() {
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
				x.image().bind(imgLeft, url, imgOption);
			}
		}
	}
}
