package com.prize.left.page.adapter;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.launcher3.R;
import com.prize.left.page.bean.table.BigCardType;
import com.prize.left.page.view.holder.BigTypeViewHolder;
/***
 * 大类适配器
 * @author fanjunchen
 *
 */
public class BigCardTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	
	private List<BigCardType> datas = null;
	
	private LayoutInflater mInflater;
	
	private Context mCtx;
	
	private OnClickListener mClick = null;
	
	public BigCardTypeAdapter(Context ctx) {
		mCtx = ctx;
		mInflater = LayoutInflater.from(mCtx);
	}
	
	/***
	 * 设置数据, 调用此方法前先调用setSelCodes方法
	 * @param codes
	 */
	public void setData(List<BigCardType> codes) {
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
		BigTypeViewHolder h = (BigTypeViewHolder)holder;
		BigCardType c = datas.get(type);
		h.itemView.setTag(c);
		h.itemView.setOnClickListener(mClick);
		h.titleTxt.setText(c.name);
		
		setFlagImg(h.imgFlag, c);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int pos) {
		View v = mInflater.inflate(R.layout.left_big_type_item, parent, false);
		return new BigTypeViewHolder(v);
	}
	
	private void setFlagImg(ImageView v, BigCardType b) {
		if ("horizontal_message".equals(b.uitype)) {
			v.setImageResource(R.drawable.left_news_ico);
		}
		else if ("vertical".equals(b.uitype)) {
			v.setImageResource(R.drawable.left_movie_ico);
		}
		else if ("horizontal_goods".equals(b.uitype)) {
			v.setImageResource(R.drawable.left_groupon_ico);
		}
		else if ("navigation".equals(b.uitype)) {
			v.setImageResource(R.drawable.left_net_navi_ico);
		}
		else if ("common".equals(b.uitype)) {
			v.setImageResource(R.drawable.left_default_ico);
		}
		else if ("hotsearch".equals(b.uitype)) {
			v.setImageResource(R.drawable.left_hot_search_ico);
		}
	}
}
