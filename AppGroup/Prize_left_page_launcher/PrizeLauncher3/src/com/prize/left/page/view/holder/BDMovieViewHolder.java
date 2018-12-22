package com.prize.left.page.view.holder;

import java.util.List;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.bean.BDMovieItem;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.model.BDMovieModel;
import com.prize.left.page.ui.AnimFrameLayout;
/**
 * 百度电影Holder
 */
public class BDMovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
	
	public TextView titleTxt;

	public ImageView imgRefresh;
	
	public ImageView imgMenu;
	
	public LinearLayout contents;
	/**下方的更多*/
	public TextView txtMore;
	
	private LayoutInflater mInflater = null;
	
	public List<BDMovieItem> datas = null;

	private int pos = -1;
	
	private BDMovieModel mModel;
	
	private Context mCtx;
	
	private CardBean cardBean = null;
	
	private LinearLayout.LayoutParams params;
	
	public View mTitleView;
	/**图片配置器*/
	private ImageOptions leftImgOption = new ImageOptions.Builder()
    	//.setSize(240, 360)
    	.setImageScaleType(ImageView.ScaleType.FIT_XY)
    	.setLoadingDrawableId(R.color.white_64)
    	.setFailureDrawableId(R.color.white_64)
    	.build();
	
	public BDMovieViewHolder(View v) {
		super(v);
		mCtx = v.getContext();
		titleTxt = (TextView) v.findViewById(R.id.txt_title);
		imgRefresh = (ImageView) v.findViewById(R.id.img_refresh);
		imgMenu = (ImageView) v.findViewById(R.id.img_more);
		contents = (LinearLayout) v.findViewById(R.id.content);
		txtMore = (TextView) v.findViewById(R.id.txt_footer_more);
		
		mTitleView = v.findViewById(R.id.card_title);
		
		mInflater = LayoutInflater.from(mCtx);
		
		imgRefresh.setOnClickListener(this);
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
		if (cardBean.cardType.moreType==0||cardBean.cardType.moreUrl==null) {
			if(txtMore!=null)
			txtMore.setVisibility(View.GONE);
		}else{
			if(txtMore!=null)
			txtMore.setVisibility(View.VISIBLE);
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.img_refresh:
				if (mModel != null)
					mModel.getNextPage();
				break;
			case R.id.movie_item:
				BDMovieItem item = (BDMovieItem)v.getTag();
				if (mModel != null && item != null) {
					int pos = datas.indexOf(item);
					mModel.jumpMovieDetail(item, pos);
				}
				break;
			case R.id.txt_footer_more:
				if (mModel != null)
					mModel.clickMore();
				break;
		}
	}
	
	public void setModel(BDMovieModel m) {
		mModel = m;
	}
	/***
	 * 设置美食(团购)数据
	 * @param ls
	 */
	public void setDatas(List<BDMovieItem> ls) {
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
		
		int rightPx = 0;
		if (null == params) {
			rightPx = mCtx.getResources().getDimensionPixelSize(R.dimen.dp_4);
		}
		int startIndex = 0;
		// 若有需要 替换VIEW
		int min = Math.min(dataSize, childSz);
		if (min > 0) {
			for (; startIndex < min; startIndex ++) {
				BDMovieItem item = datas.get(startIndex);
				AnimFrameLayout frame = (AnimFrameLayout)contents.getChildAt(startIndex);
				View itemView = mInflater.inflate(R.layout.bd_movie_item, null);
				
				initItemView(itemView, item);
				frame.replaceView(itemView, startIndex);
			}
		}
		
		for (; startIndex < dataSize; startIndex ++) {
			AnimFrameLayout frame = (AnimFrameLayout)mInflater.inflate(R.layout.item_base, null);
			
			BDMovieItem item = datas.get(startIndex);
			View itemView = mInflater.inflate(R.layout.bd_movie_item, null);
			
			initItemView(itemView, item);
			
			frame.replaceView(itemView, startIndex);
			params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
			if (startIndex == 0) {
				params.leftMargin = 0;
				params.rightMargin = rightPx;
			}
			else if (startIndex == dataSize - 1) {
				params.leftMargin = rightPx;
				params.rightMargin = 0;
			}
			else {
				params.leftMargin = rightPx;
				params.rightMargin = rightPx;
			}
			contents.addView(frame, params);
		}
	}
	/***
	 * 绑定指定的VIEW
	 * @param v
	 * @param data
	 */
	private void initItemView(View v, BDMovieItem data) {
		v.setTag(data);
		v.setOnClickListener(this);
		// 标题或名称
		TextView txtName = (TextView)v.findViewById(R.id.txt_title);
		txtName.setText(data.title);
		txtName = (TextView)v.findViewById(R.id.txt_score);
		txtName.setText(data.figure);
		String imgUrl = data.imageUrl;
		ImageView imgView = (ImageView)v.findViewById(R.id.img_left);
		String url = (String)imgView.getTag();
		if (null == url || !url.equals(imgUrl)) {
			imgView.setTag(imgUrl);
			x.image().bind(imgView, imgUrl, leftImgOption);
		}
	}
	
	/***
	 * 获取美食(团队)项数据
	 */
	public List<BDMovieItem> getDatas() {
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
		}
	}
}
