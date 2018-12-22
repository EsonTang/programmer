package com.prize.left.page.view.holder;

import java.util.List;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.bean.BDGroupItem;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.model.BDGroupModel;
import com.prize.left.page.ui.AnimFrameLayout;
import com.prize.left.page.util.BDImageLoadHelper;
import com.prize.left.page.util.CommonUtils;
/**
 * 百度团购Holder
 */
public class BDGroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
	
	public TextView titleTxt;

	public ImageView imgRefresh;
	
	public ImageView imgMenu;
	
	public LinearLayout contents;
	/**下方的更多*/
	public TextView txtMore;
	
	private LayoutInflater mInflater = null;
	
	public List<BDGroupItem> datas = null;

	private BDGroupModel mModel;
	
	private Context mCtx;
	
	private CardBean cardBean = null;
	
	private BDImageLoadHelper mBDImgHelper;
	
	public View mTitleView;
	/**图片配置器*/
	/*private ImageOptions leftImgOption = new ImageOptions.Builder()
    	.setSize(170, 130)
    	.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
    	.build();*/
	
	public BDGroupViewHolder(View v) {
		super(v);
		mCtx = v.getContext();
		mBDImgHelper = new BDImageLoadHelper(mCtx);
		titleTxt = (TextView) v.findViewById(R.id.txt_title);
		imgRefresh = (ImageView) v.findViewById(R.id.img_refresh);
		imgMenu = (ImageView) v.findViewById(R.id.img_more);
		contents = (LinearLayout) v.findViewById(R.id.content);
		txtMore = (TextView) v.findViewById(R.id.txt_footer_more);
		
		mTitleView = v.findViewById(R.id.card_title);
		
		mInflater = LayoutInflater.from(mCtx);
		
		imgRefresh.setOnClickListener(this);
		if (txtMore != null)
			txtMore.setOnClickListener(this);
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
		cardBean = b;
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.img_refresh:
				if (mModel != null)
					mModel.getNextPage();
				break;
			case R.id.group_item:
				BDGroupItem item = (BDGroupItem)v.getTag();
				if (mModel != null && item != null) {
					int pos = datas.indexOf(item);
					mModel.jumpGroupDetail(item, pos);
				}
				break;
			case R.id.txt_footer_more:
				if (mModel != null)
					mModel.clickMore();
				break;
		}
	}
	
	public void setModel(BDGroupModel m) {
		mModel = m;
	}
	/***
	 * 设置美食(团购)数据
	 * @param ls
	 */
	public void setDatas(List<BDGroupItem> ls) {
		if (null == ls || ls.size() < 1)
			return;
		datas = ls;
		dealTypeOthers();
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
				BDGroupItem item = datas.get(startIndex);
				AnimFrameLayout frame = (AnimFrameLayout)contents.getChildAt(startIndex);
				View itemView = mInflater.inflate(R.layout.bd_group_item, null);
				if (startIndex == dataSize - 1) {
					View bv = itemView.findViewById(R.id.bottom_line);
					if (bv != null) {
						RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)bv.getLayoutParams();
						p.bottomMargin = 0;
						bv.setLayoutParams(p);
						bv.setVisibility(View.INVISIBLE);
					}
				}
				initItemView(itemView, item);
				
				frame.replaceView(itemView, startIndex);
			}
		}
		
		for (; startIndex < dataSize; startIndex ++) {
			AnimFrameLayout frame = (AnimFrameLayout)mInflater.inflate(R.layout.item_base, null);
			
			BDGroupItem item = datas.get(startIndex);
			View itemView = mInflater.inflate(R.layout.bd_group_item, null);
			if (startIndex == dataSize - 1) {
				View bv = itemView.findViewById(R.id.bottom_line);
				if (bv != null) {
					RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)bv.getLayoutParams();
					p.bottomMargin = 0;
					bv.setLayoutParams(p);
					bv.setVisibility(View.INVISIBLE);
				}
			}
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
	private void initItemView(View v, BDGroupItem data) {
		v.setTag(data);
		v.setOnClickListener(this);
		// 标题或名称
		TextView txt = (TextView)v.findViewById(R.id.txt_name);
		txt.setText(data.title);
		
		txt = (TextView)v.findViewById(R.id.txt_substract);
		txt.setText(data.subTitle);
		
		txt = (TextView)v.findViewById(R.id.txt_score);		
		txt.setText(data.score);
		
		txt = (TextView)v.findViewById(R.id.txt_distance);
		txt.setText(data.distance);
		
		txt = (TextView)v.findViewById(R.id.txt_current_price);
		txt.setText(data.price);
		
		txt = (TextView)v.findViewById(R.id.txt_market_price);
		txt.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		txt.setText(data.originalPrice);
		
		String imgUrl = data.imageUrl;// tiny_image
		ImageView imgView = (ImageView)v.findViewById(R.id.img_left);
		String url = (String)imgView.getTag();
		if (null == url || !url.equals(imgUrl)) {
			imgView.setTag(imgUrl);
			mBDImgHelper.executeImageUrl(imgUrl, imgView);
			//x.image().bind(imgView, imgUrl, leftImgOption);
		}
	}
	
	/***
	 * 获取美食(团队)项数据
	 */
	public List<BDGroupItem> getDatas() {
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
				String imgUrl = (String)imgLeft.getTag();
				mBDImgHelper.executeImageUrl(imgUrl, imgLeft);
			}
		}
	}
}
