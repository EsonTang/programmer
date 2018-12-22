package com.prize.left.page.view.holder;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.android.launcher3.R;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.model.CardDemoModel;
/**
 * 示例未添加的卡片 Holder
 */
public class UnSpcAddViewHolder extends RecyclerView.ViewHolder {
	
	public View btn;

	private LayoutInflater mInflater = null;
	
	private CardDemoModel mModel;
	
	private Context mCtx;
	
	private List<CardType> datas = null;
	
	public UnSpcAddViewHolder(View v) {
		super(v);
		mCtx = v.getContext();
		btn = v.findViewById(R.id.btn_add_spc);
		
		mInflater = LayoutInflater.from(mCtx);
	}
	
	private View.OnClickListener mClick = null;
	
	public void setClickListener(View.OnClickListener c) {
		mClick = c;
		if (btn != null) {
			btn.setOnClickListener(mClick);
			if (datas != null && datas.size() > 0)
				btn.setTag(datas.get(0));
		}
	}
	
	
	public void setModel(CardDemoModel m) {
		mModel = m;
	}
	
	public void setDatas(List<CardType> ls) {
		datas = ls;
	}
	
	/**
	 * 加载图片
	 */
	public void doBindImg() {
		
	}
}
