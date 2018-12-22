package com.prize.left.page.view.holder;

import java.util.List;

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
import com.prize.left.page.bean.table.HotTipsTable;
import com.prize.left.page.model.HotTipsModel;
import com.prize.left.page.ui.AnimFrameLayout;
import com.tencent.stat.StatService;
/**
 * 一点资讯新闻Holder
 */
public class HotTipsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
	public TextView titleTxt;

	public ImageView imgRefresh;
	
	public ImageView imgMenu;
	
	public LinearLayout contents;
	/**下方的更多*/
	public TextView txtMore;
	
	private LayoutInflater mInflater = null;
	
	private List<HotTipsTable> datas = null;

//	private int pos = -1;
	
	private HotTipsModel mModel;
	
	private Context mCtx;
	
	private CardBean cardBean = null;
	
	public HotTipsViewHolder(View v) {
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
				if (mModel != null) {
					mModel.doPost();
					mModel.onResume();
				}
				
				StatService.trackCustomEvent(mCtx, "CardTips", "");
				break;
			case R.id.txt_title:
			case R.id.txt_title2:
			    HotTipsTable item = (HotTipsTable)v.getTag();
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
				StatService.trackCustomEvent(mCtx, "CardTips", "");
				break;
		}
	}
	
	public void setModel(HotTipsModel m) {
		mModel = m;
	}
	/***
	 * 设置美食(团购)数据
	 * @param ls
	 */
	public void setDatas(List<HotTipsTable> ls) {
		if (null == ls || ls.size() < 1)
			return;
		datas = ls;
		dealTypeDz();
	}
	/***
	 * 处理 段子 频道
	 */
	private void dealTypeDz() {
		int childSz = contents.getChildCount();
		int dataSize = datas.size();
		int count = dataSize / 2 +  dataSize % 2;
		if (count < childSz) { // 先删除多余的VIEW
			for (int i = count; i < childSz; i++) {
				contents.removeViewAt(count);
			}
		}
		int startIndex = 0;
		// 若有需要 替换VIEW
		int min = Math.min(count, childSz);
		if (min > 0) {
			for (; startIndex < min; startIndex ++) {
				int index = startIndex * 2;
				HotTipsTable item = datas.get(index);
				AnimFrameLayout frame = (AnimFrameLayout)contents.getChildAt(startIndex);
				
				View itemView = mInflater.inflate(R.layout.left_hot_word_item, null);
				if (startIndex == count - 1) {
					View bv = itemView.findViewById(R.id.bottom_line);
					if (bv != null)
						bv.setVisibility(View.INVISIBLE);
				}
				TextView t1 = (TextView)itemView.findViewById(R.id.txt_title);
				initItemView(t1, item);
				
				if (index + 1 < dataSize) {
					item = datas.get(index + 1);
					t1 = (TextView)itemView.findViewById(R.id.txt_title2);
					initItemView(t1, item);
				}
				
				frame.replaceView(itemView, startIndex);
			}
		}
		
		for (; startIndex < count; startIndex ++) {
			AnimFrameLayout frame = (AnimFrameLayout)mInflater.inflate(R.layout.item_base, null);
			
			int index = startIndex * 2;
			
			HotTipsTable item = datas.get(index);
			
			View itemView = mInflater.inflate(R.layout.left_hot_word_item, null);
			if (startIndex == count - 1) {
				View bv = itemView.findViewById(R.id.bottom_line);
				if (bv != null)
					bv.setVisibility(View.INVISIBLE);
			}
			TextView t1 = (TextView)itemView.findViewById(R.id.txt_title);
			initItemView(t1, item);
			
			if (index + 1 < dataSize) {
				item = datas.get(index + 1);
				t1 = (TextView)itemView.findViewById(R.id.txt_title2);
				initItemView(t1, item);
			}
			
			frame.replaceView(itemView, startIndex);
			
			contents.addView(frame);
		}
	}
	/***
	 * 绑定指定的VIEW
	 * @param v
	 * @param data
	 */
	private void initItemView(View v, HotTipsTable data) {
		v.setTag(data);
		v.setOnClickListener(this);
		TextView txtWord = (TextView)v.findViewById(R.id.txt_title);
		txtWord.setText(data.word);
	}
	
	/***
	 * 绑定指定的VIEW
	 * @param v
	 * @param data
	 */
	private void initItemView(TextView v, HotTipsTable data) {
		v.setTag(data);
		v.setOnClickListener(this);
		v.setText(data.word);
	}
	
	/***
	 * 获取美食(团队)项数据
	 */
	public List<HotTipsTable> getDatas() {
		return datas;
	}
	
	/**
	 * 加载图片
	 */
	public void doBindImg() {
	}
}
