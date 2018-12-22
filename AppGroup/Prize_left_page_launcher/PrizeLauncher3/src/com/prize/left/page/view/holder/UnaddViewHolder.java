package com.prize.left.page.view.holder;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.model.CardDemoModel;
/**
 * 示例未添加的卡片 Holder
 */
public class UnaddViewHolder extends RecyclerView.ViewHolder {
	
	public TextView titleTxt;

	public LinearLayout contents;
	
	private LayoutInflater mInflater = null;
	
	private List<CardType> datas = null;

	private CardDemoModel mModel;
	
	private Context mCtx;
	
	public UnaddViewHolder(View v) {
		super(v);
		mCtx = v.getContext();
		titleTxt = (TextView) v.findViewById(R.id.txt_title);
		contents = (LinearLayout) v.findViewById(R.id.content);
		
		mInflater = LayoutInflater.from(mCtx);
	}
	
	private View.OnClickListener mClick = null;
	
	public void setClickListener(View.OnClickListener c) {
		mClick = c;
	}
	
	
	public void setModel(CardDemoModel m) {
		mModel = m;
	}
	/***
	 * 设置美食(团购)数据
	 * @param ls
	 */
	public void setDatas(List<CardType> ls) {
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
				CardType item = datas.get(startIndex);
				View itemView = contents.getChildAt(startIndex);
				View bv = itemView.findViewById(R.id.bottom_line);
				if (startIndex == dataSize - 1) {
					bv.setVisibility(View.INVISIBLE);
				}
				else {
					bv.setVisibility(View.VISIBLE);
				}
				initItemView(itemView, item);
			}
		}
		
		for (; startIndex < dataSize; startIndex ++) {
			CardType item = datas.get(startIndex);
			View itemView = mInflater.inflate(R.layout.left_demo_unadd_item, null);
			if (startIndex == dataSize - 1) {
				View bv = itemView.findViewById(R.id.bottom_line);
				bv.setVisibility(View.INVISIBLE);
			}
			initItemView(itemView, item);
			contents.addView(itemView);
		}
	}
	/***
	 * 绑定指定的VIEW
	 * @param v
	 * @param data
	 */
	private void initItemView(View v, CardType data) {
		
		TextView txt = (TextView) v.findViewById(R.id.txt_title);
		txt.setText(data.name);
		
		ImageView img = (ImageView)v.findViewById(R.id.img_logo);
		// setLogo(data.code);
		
		View btn = v.findViewById(R.id.btn_add);
		btn.setTag(data);
		btn.setOnClickListener(mClick);
	}
	
	/***
	 * 获取 英威诺 项数据
	 */
	public List<CardType> getDatas() {
		return datas;
	}
	
	/**
	 * 加载图片
	 */
	public void doBindImg() {
		
	}
}
