package com.prize.left.page.view.holder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.InvnoNewsItem;
import com.prize.left.page.model.InvnoNewsModel;
import com.prize.left.page.ui.AnimFrameLayout;
/**
 * 英威诺资讯 Holder
 */
public class InvnoNewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
	
	public TextView titleTxt;

	public ImageView imgRefresh;
	
	public ImageView imgMenu;
	
	public LinearLayout contents;
	/**下方的更多*/
	public TextView txtMore;
	
	private LayoutInflater mInflater = null;
	
	public List<InvnoNewsItem> datas = null;

	private InvnoNewsModel mModel;
	
	private Context mCtx;
	
	private CardBean cardBean = null;
	
	public View mTitleView;
	
	//private String strWH = "&width=170&height=130";
	
	private ImageOptions imgOption = null;
	
	public InvnoNewsViewHolder(View v) {
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
		if (txtMore != null)
			txtMore.setOnClickListener(this);
		
		//strWH = "&width=" + mCtx.getResources().getDimensionPixelSize(R.dimen.left_img_width);
		//strWH += "&height=" + mCtx.getResources().getDimensionPixelSize(R.dimen.left_img_height);
		
		imgOption = new ImageOptions.Builder()
		.setSize(mCtx.getResources().getDimensionPixelSize(R.dimen.left_img_width), mCtx.getResources().getDimensionPixelSize(R.dimen.left_img_height))
		.setImageScaleType(ImageView.ScaleType.FIT_XY)
		.setFailureDrawableId(R.color.white_64)
		.setLoadingDrawableId(R.color.white_64)
		.build();
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
			case R.id.group_item:
				Object o = v.getTag();
				if (o instanceof InvnoNewsItem) {
					InvnoNewsItem item = (InvnoNewsItem)o;
					if (item != null) {
						int pos = datas.indexOf(item);
						mModel.jumpDetail(item, pos);
					}
				}
				break;
			case R.id.txt_footer_more:
				if (mModel != null) {
					mModel.moreNews("0");
				}
				break;
		}
	}
	
	public void setModel(InvnoNewsModel m) {
		mModel = m;
	}
	/***
	 * 设置美食(团购)数据
	 * @param ls
	 */
	public void setDatas(List<InvnoNewsItem> ls) {
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
//				FlowNewsinfo item = datas.get(startIndex);
				InvnoNewsItem item=datas.get(startIndex);
				AnimFrameLayout frame = (AnimFrameLayout)contents.getChildAt(startIndex);
				View itemView = mInflater.inflate(R.layout.left_invno_item, null);
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
			
			InvnoNewsItem item = datas.get(startIndex);
			View itemView = mInflater.inflate(R.layout.left_invno_item, null);
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
	 * 设置广告
	 * @param ad
	 */
//	public void setFlowAd(FlowAd ad) {
//		int c = contents.getChildCount() - 1;
//		if (c <= 0)
//			return;
//		AnimFrameLayout frame = (AnimFrameLayout)contents.getChildAt(c);
//		View itemView = mInflater.inflate(R.layout.left_invno_item, null);
//		/*if (startIndex == dataSize - 1) {*/
//			View bv = itemView.findViewById(R.id.bottom_line);
//			if (bv != null) {
//				RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)bv.getLayoutParams();
//				p.bottomMargin = 0;
//				bv.setLayoutParams(p);
//				bv.setVisibility(View.INVISIBLE);
//			}
//			/*}*/
//		initAdView(itemView, ad);
//		frame.replaceView(itemView, c);
//	}
	
	/***
	 * 绑定指定的VIEW
	 * @param v
	 * @param data
	 */
//	private void initAdView(View v, FlowAd data) {
//		v.setTag(data);
//		v.setOnClickListener(this);
//		
//		TextView txt = (TextView) v.findViewById(R.id.txt_title);
//		txt.setText(data.getTitle());
//		
//		txt = (TextView) v.findViewById(R.id.txt_from);
//		txt.setText("");
//		
//		txt = (TextView) v.findViewById(R.id.txt_time);
//		txt.setText("");
//		
//		ImageView imgView = (ImageView) v.findViewById(R.id.left_img);
//		
//		String img = data.getImg();
//		//图片支持动态压缩，即在线即时压缩，在图片url后代参数&width和&height
//		/*Glide.with(v.getContext()).load(img + strWH)
//        .centerCrop()
//        .placeholder(R.color.white_64)
//        .crossFade()
//        .into(imgView);*/
//		
//		x.image().bind(imgView, img, imgOption);
//	}
	/***
	 * 绑定指定的VIEW
	 * @param v
	 * @param data
	 */
	private void initItemView(View v, InvnoNewsItem data) {
		v.setTag(data);
		v.setOnClickListener(this);
		
		TextView txt = (TextView) v.findViewById(R.id.txt_title);
		txt.setText(data.getTitle());
		
		txt = (TextView) v.findViewById(R.id.txt_from);
		txt.setText(data.getSrc());
		
		txt = (TextView) v.findViewById(R.id.txt_time);
		txt.setText(data.getTime());
		
		ImageView imgView = (ImageView) v.findViewById(R.id.left_img);
		
		/*Imgs imgs = (Imgs)imgView.getTag();
		if (null == imgs) {
			imgs = data.getImgs();
			imgView.setTag(imgs);
		}*/
		//Imgs imgs = data.getImgs();
		//图片支持动态压缩，即在线即时压缩，在图片url后代参数&width和&height
		/*Glide.with(v.getContext()).load(imgs.getUrl() + strWH)
        .centerCrop()
        .placeholder(R.color.white_64)
        .crossFade()
        .into(imgView);*/
		x.image().bind(imgView, data.getImageUrl(), imgOption);
	}
	
	/***
	 * 获取 英威诺 项数据
	 */
	public List<InvnoNewsItem> getDatas() {
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
			if (imgLeft != null) {
				//FlowNewsinfo data = datas.get(i);
				InvnoNewsItem data=datas.get(i);
				//Imgs imgs = data.getImgs();
				/*Glide.with(imgLeft.getContext()).load(imgs.getUrl() + strWH)
				.centerCrop()
		        .placeholder(R.color.white_64)
		        .crossFade()
		        .into(imgLeft);*/
				x.image().bind(imgLeft, data.getImageUrl(), imgOption);
			}
		}
	}
}
