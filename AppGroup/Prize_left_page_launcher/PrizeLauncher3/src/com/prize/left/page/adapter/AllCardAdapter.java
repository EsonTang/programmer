package com.prize.left.page.adapter;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.android.launcher3.R;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.view.holder.AllCardViewHolder;
/***
 * 类型适配器
 * @author fanjunchen
 *
 */
public class AllCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	
	private List<CardType> datas = null;
	
	private LayoutInflater mInflater;
	
	private Context mCtx;
	
	private OnClickListener mClick = null;
	
	public AllCardAdapter(Context ctx) {
		mCtx = ctx;
		mInflater = LayoutInflater.from(mCtx);
	}
	
	/***
	 * 设置数据, 调用此方法前先调用setSelCodes方法
	 * @param codes
	 */
	public void setData(List<CardType> codes) {
		datas = codes;
	}
	
	public void setOnClickListener(OnClickListener l) {
		mClick = l;
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return datas == null ? 0 : datas.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int type) {
		// TODO Auto-generated method stub
		AllCardViewHolder h = (AllCardViewHolder)holder;
		CardType c = datas.get(type);
		h.itemView.setTag(c);
		h.itemView.setOnClickListener(mClick);
		h.titleTxt.setText(c.name);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int pos) {
		View v = mInflater.inflate(R.layout.left_all_card_item, parent, false);
		return new AllCardViewHolder(v);
	}
}
