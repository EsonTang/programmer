package com.prize.left.page.view.holder;

import java.util.List;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.activity.WebViewActivity;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.OneNewsCardItem;
import com.prize.left.page.model.OneNewsModel;
import com.prize.left.page.ui.AnimFrameLayout;
/**
 * 一点资讯新闻Holder
 */
public class OneNewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
	public TextView titleTxt;

	public ImageView imgRefresh;
	
	public ImageView imgMenu;
	
	public LinearLayout contents;
	/**下方的更多*/
	public TextView txtMore;
	
	private LayoutInflater mInflater = null;
	
	private List<OneNewsCardItem> datas = null;

	private int pos = -1;
	
	private OneNewsModel mModel;
	
	private Context mCtx;
	
	private CardBean cardBean = null;
	/**一点资讯 下面的美女频道*/
	private final int MN_TYPE = 9;
	/**一点资讯 下面的段子频道*/
	private final int DZ_TYPE = 10;
	
	/**图片配置器*/
	private ImageOptions leftImgOption = new ImageOptions.Builder()
    	.setSize(600, 830)
    	.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
    	.setLoadingDrawableId(R.color.white_64)
    	.setFailureDrawableId(R.color.white_64)
    	.build();
	
	public OneNewsViewHolder(View v) {
		super(v);
		mCtx = v.getContext();
		titleTxt = (TextView) v.findViewById(R.id.txt_title);
		imgRefresh = (ImageView) v.findViewById(R.id.img_refresh);
		imgMenu = (ImageView) v.findViewById(R.id.img_more);
		contents = (LinearLayout) v.findViewById(R.id.content);
		txtMore = (TextView) v.findViewById(R.id.txt_footer_more);
		
		mInflater = LayoutInflater.from(mCtx);
		
		imgRefresh.setOnClickListener(this);
	}
	
	private ObjectAnimator mAnim = null;
	/***
	 * 开始动画
	 */
	public void start() {
		if (null == mAnim) {
			mAnim = ObjectAnimator  
					.ofFloat(imgRefresh, "rotation", 0.0F, 360.0F);
			mAnim.setRepeatMode(ObjectAnimator.RESTART);
			mAnim.setRepeatCount(ObjectAnimator.INFINITE);
			mAnim.setDuration(900);
		}
		mAnim.start();
	}
	/***
	 * 结束动画
	 */
	public void end() {
		if (mAnim != null) {
			mAnim.end();
		}
	}
	/***
	 * 设置卡片数据类型实体
	 * @param b
	 */
	public void setCardBean(CardBean b) {
		if (null == cardBean)
			cardBean = b;
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.img_refresh:
				/*String json = v.getContext().getString(R.string.news_refresh);
				OneNewsResponse rsp = CommonUtils.getObject(json, OneNewsResponse.class);
				setDatas(rsp.data.items);*/
				if (mModel != null)
					mModel.getNextPage();
				break;
			case R.id.one_news_three:
			case R.id.one_news_one:
				OneNewsCardItem item = (OneNewsCardItem)v.getTag();
				Intent it;
				if (!item.url.startsWith("https")) {
					it = new Intent(mCtx, WebViewActivity.class);
					it.putExtra(WebViewActivity.P_URL, item.url);
				}
				else {
					it = new Intent();        
					it.setAction("android.intent.action.VIEW");    
					Uri url = Uri.parse(item.url);   
					it.setData(url);
				}
				mCtx.startActivity(it);
				it = null;
				break;
		}
	}
	
	public void setModel(OneNewsModel m) {
		mModel = m;
	}
	/***
	 * 设置美食(团购)数据
	 * @param ls
	 */
	public void setDatas(List<OneNewsCardItem> ls) {
		if (null == ls || ls.size() < 1)
			return;
		datas = ls;
		if (MN_TYPE == cardBean.cardType.subCode) {
			dealTypeMM();
		}
		else if (DZ_TYPE == cardBean.cardType.subCode) {
			dealTypeDz();
		}
		else 
			dealTypeOthers();
	}
	/***
	 * 处理 美女 频道
	 */
	private void dealTypeMM() {
		
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
				OneNewsCardItem item = datas.get(startIndex);
				AnimFrameLayout frame = (AnimFrameLayout)contents.getChildAt(startIndex);
				
				View itemView = mInflater.inflate(R.layout.one_news_big_img, null);
				initMMItemView(itemView, item);
				
				frame.replaceView(itemView, startIndex);
			}
		}
		
		for (; startIndex < dataSize; startIndex ++) {
			AnimFrameLayout frame = (AnimFrameLayout)mInflater.inflate(R.layout.item_base, null);
			
			OneNewsCardItem item = datas.get(startIndex);
			
			View itemView = mInflater.inflate(R.layout.one_news_big_img, null);
			
			initMMItemView(itemView, item);
			
			frame.replaceView(itemView, startIndex);
			
			contents.addView(frame);
		}
	}
	/***
	 * 处理 段子 频道
	 */
	private void dealTypeDz() {
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
				OneNewsCardItem item = datas.get(startIndex);
				AnimFrameLayout frame = (AnimFrameLayout)contents.getChildAt(startIndex);
				
				View itemView = mInflater.inflate(R.layout.one_news_dz, null);
				initDzItemView(itemView, item);
				
				frame.replaceView(itemView, startIndex);
			}
		}
		
		for (; startIndex < dataSize; startIndex ++) {
			AnimFrameLayout frame = (AnimFrameLayout)mInflater.inflate(R.layout.item_base, null);
			
			OneNewsCardItem item = datas.get(startIndex);
			
			View itemView = mInflater.inflate(R.layout.one_news_dz, null);
			
			initDzItemView(itemView, item);
			
			frame.replaceView(itemView, startIndex);
			
			contents.addView(frame);
		}
	}
	/***
	 * 处理其他 频道
	 */
	private void dealTypeOthers() {
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
				OneNewsCardItem item = datas.get(startIndex);
				AnimFrameLayout frame = (AnimFrameLayout)contents.getChildAt(startIndex);
				View itemView = null;
				if (item.images.size() <= 1)
					itemView = mInflater.inflate(R.layout.one_news_item_one, null);
				else
					itemView = mInflater.inflate(R.layout.one_news_item_three, null);
				
				initItemView(itemView, item);
				
				frame.replaceView(itemView, startIndex);
			}
		}
		
		for (; startIndex < dataSize; startIndex ++) {
			AnimFrameLayout frame = (AnimFrameLayout)mInflater.inflate(R.layout.item_base, null);
			
			OneNewsCardItem item = datas.get(startIndex);
			View itemView = null;
			if (item.images == null || item.images.size() <= 1)
				itemView = mInflater.inflate(R.layout.one_news_item_one, null);
			else
				itemView = mInflater.inflate(R.layout.one_news_item_three, null);
			
			initItemView(itemView, item);
			
			frame.replaceView(itemView, startIndex);
			
			contents.addView(frame);
		}
	}
	/***
	 * 绑定指定的VIEW
	 * @param v
	 * @param data
	 */
	private void initItemView(View v, OneNewsCardItem data) {
		v.setTag(data);
		v.setOnClickListener(this);
		// 标题或名称
		TextView txtName = (TextView)v.findViewById(R.id.txt_title);
		txtName.setText(data.title);
		int sz = data.images == null ? 1 : data.images.size();
		if (sz > 1) {
			String imgUrl = data.images.get(0);
			ImageView imgView = (ImageView)v.findViewById(R.id.img_one);
			String url = (String)imgView.getTag();
			if (null == url || !url.equals(imgUrl)) {
				imgView.setTag(imgUrl);
				x.image().bind(imgView, imgUrl, leftImgOption);
			}
			imgUrl = data.images.get(1);
			imgView = (ImageView)v.findViewById(R.id.img_two);
			url = (String)imgView.getTag();
			if (null == url || !url.equals(imgUrl)) {
				imgView.setVisibility(View.VISIBLE);
				imgView.setTag(imgUrl);
				x.image().bind(imgView, imgUrl, leftImgOption);
			}
			else
				imgView.setVisibility(View.INVISIBLE);
			if (sz > 2) {
				imgUrl = data.images.get(2);
				imgView = (ImageView)v.findViewById(R.id.img_three);
				url = (String)imgView.getTag();
				if (null == url || !url.equals(imgUrl)) {
					imgView.setTag(imgUrl);
					x.image().bind(imgView, imgUrl, leftImgOption);
				}
				else
					imgView.setVisibility(View.INVISIBLE);
			}
		}
		else if (sz > 0){
			String imgUrl = data.images == null ? data.image : data.images.get(0);
			ImageView imgLeft = (ImageView)v.findViewById(R.id.img_left);
			String url = (String)imgLeft.getTag();
			if (null == url || !url.equals(imgUrl)) {
				imgLeft.setTag(imgUrl);
				x.image().bind(imgLeft, imgUrl, leftImgOption);
			}
		}
	}
	
	/***
	 * 绑定指定的VIEW
	 * @param v
	 * @param data
	 */
	private void initMMItemView(View v, OneNewsCardItem data) {
		v.setTag(data);
		v.setOnClickListener(this);
		String imgUrl = data.image;
		ImageView imgView = (ImageView)v.findViewById(R.id.img_big);
		String url = (String)imgView.getTag();
		if (null == url || !url.equals(imgUrl)) {
			imgView.setTag(imgUrl);
			x.image().bind(imgView, imgUrl, leftImgOption);
		}
	}
	
	/***
	 * 绑定指定的VIEW
	 * @param v
	 * @param data
	 */
	private void initDzItemView(View v, OneNewsCardItem data) {
		v.setTag(data);
		v.setOnClickListener(this);
		TextView txt = (TextView)v.findViewById(R.id.txt_summary);
		txt.setText(data.summary);
	}
	/***
	 * 获取美食(团队)项数据
	 */
	public List<OneNewsCardItem> getDatas() {
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
				x.image().bind(imgLeft, url, leftImgOption);
			}
			
			imgLeft = (ImageView)frame.getChildAt(0).findViewById(R.id.img_one);
			if (imgLeft != null && imgLeft.getTag() != null) {
				String url = (String)imgLeft.getTag();
				x.image().bind(imgLeft, url, leftImgOption);
			}
			
			imgLeft = (ImageView)frame.getChildAt(0).findViewById(R.id.img_two);
			if (imgLeft != null && imgLeft.getTag() != null) {
				String url = (String)imgLeft.getTag();
				x.image().bind(imgLeft, url, leftImgOption);
			}
			
			imgLeft = (ImageView)frame.getChildAt(0).findViewById(R.id.img_three);
			if (imgLeft != null && imgLeft.getTag() != null) {
				String url = (String)imgLeft.getTag();
				x.image().bind(imgLeft, url, leftImgOption);
			}
		}
	}
}
