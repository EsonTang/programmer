package com.prize.left.page.view.holder;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.prize.left.page.bean.AppBean;
import com.prize.left.page.ui.NoScroolGridView;

/**
 * 搜索APP Card Holder
 */
public class AppViewHolder extends RecyclerView.ViewHolder {
	/**卡片标题*/
	public TextView titleTxt;
	/**删除按钮*/
	public ImageView imgRefresh;
	/**菜单/更多 按钮*/
	public ImageView imgMenu;
	/**导航容器*/
	public NoScroolGridView contents;
	
	public TextView txtExpand;
	/**适配器*/
	private GridAdapter mAdapter;
	
	private LayoutInflater mInflater;
	
	private RecyclerView.Adapter<RecyclerView.ViewHolder> mRecycleAdapter;
	
	private int pos = -1;
	
	private Context mCtx;
	
	private View mTitleView;
	
	/*private ImageOptions imgOption = new ImageOptions.Builder()
    	.setSize(100, 100)
    	.setRadius(4)
    	//.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
    	.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
    	.build();*/
	
	public AppViewHolder(View v) {
		super(v);
		mCtx = v.getContext();
		titleTxt = (TextView) v.findViewById(R.id.txt_title);
		imgRefresh = (ImageView) v.findViewById(R.id.img_refresh);
		imgMenu = (ImageView) v.findViewById(R.id.img_more);
		contents = (NoScroolGridView) v.findViewById(R.id.content);
		mAdapter = new GridAdapter();
		contents.setAdapter(mAdapter);
		
		txtExpand = (TextView) v.findViewById(R.id.txt_expand);
		
		contents.setSelector(new ColorDrawable(Color.TRANSPARENT));
		
		contents.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int pos, long id) {
				// 点击事件处理
				if (null == mAdapter.datas || pos < 0 ||
						pos >= mAdapter.datas.size())
					return;
				AppBean bean = mAdapter.datas.get(pos);
				if (null == bean || null == bean.it)
					return;
				try {
					Intent it = bean.it;
					mCtx.startActivity(it);
					it = null;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
		mTitleView = v.findViewById(R.id.card_title);
		
		mInflater = LayoutInflater.from(mCtx);
	}
	
	public void setAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> ad, int pos) {
		mRecycleAdapter = ad;
		this.pos = pos;
	}
	
	private View.OnClickListener mExClick = null;
	
	public void setExpandClick(View.OnClickListener clk) {
		mExClick = clk;
		if (txtExpand != null)
			txtExpand.setOnClickListener(mExClick);
	}
	/***
	 * 设置导航数据
	 * @param ls
	 */
	public void setDatas(List<AppBean> ls) {
		
		if (null == mAdapter)
			return;
		
		if (null == ls || ls.size() < 1) {
			mTitleView.setVisibility(View.GONE);
			RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) itemView.getLayoutParams();
			if (p != null)
				p.bottomMargin = 0;
			itemView.setLayoutParams(p);
			itemView.setVisibility(View.GONE);
			itemView.invalidate();
		}
		else {
			mTitleView.setVisibility(View.VISIBLE);
			RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) itemView.getLayoutParams();
			if (p != null)
				p.bottomMargin = mCtx.getResources().getDimensionPixelSize(R.dimen.search_left_margin);
			itemView.setLayoutParams(p);
			itemView.setVisibility(View.VISIBLE);
		}
		mAdapter.datas = ls;
		mAdapter.notifyDataSetChanged();
		
		/*if (mRecycleAdapter != null && pos != -1)
			mRecycleAdapter.notifyItemChanged(pos);*/
	}
	
	public List<AppBean> getDatas() {
		if (mAdapter != null)
			return mAdapter.datas;
		
		return null;
	}
	
	/***
	 * GridAdapter
	 * @author fanjunchen
	 *
	 */
	class GridAdapter extends BaseAdapter {
		
		List<AppBean> datas = null;
		@Override
		public int getCount() {
			return datas != null ? datas.size() : 0;
		}

		@Override
		public AppBean getItem(int pos) {
			return datas != null ? datas.get(pos) : null;
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			AppHolder holder = null;
			if (null == convertView) {
				holder = new AppHolder();
				convertView = mInflater.inflate(R.layout.left_search_app_grid_item, null);
				// holder.icImg = (ImageView)convertView.findViewById(R.id.img_ico);
				holder.txtName = (TextView)convertView.findViewById(R.id.txt_name);
				
				convertView.setTag(holder);
			}
			else {
				holder = (AppHolder)convertView.getTag();
			}
			
			AppBean item = datas.get(pos);
			
			holder.txtName.setText(item.title);
			
			Drawable topDrawable = new BitmapDrawable(mCtx.getResources(), LauncherAppState.getInstance().getIconCache().getIcon(item.it));
			holder.txtName.setCompoundDrawablesWithIntrinsicBounds(null, topDrawable, null, null);
			// holder.icImg.setImageBitmap(LauncherAppState.getInstance().getIconCache().getIcon(item.it));
			// x.image().bind(holder.icImg, item.iconUrl, imgOption);
			return convertView;
		}
	}
	
	static class AppHolder {
		
		// public ImageView icImg;
		
		public TextView txtName;
	}
	
	/***
	 * 设置展开按钮的可见性
	 * @param isVisible
	 */
	public void setExpandVisible(boolean isVisible) {
		if (isVisible)
			txtExpand.setVisibility(View.VISIBLE);
		else
			txtExpand.setVisibility(View.INVISIBLE);
	}
	/***
	 * 设置展开按钮的文本
	 * @param isVisible
	 */
	public void setExpandText(boolean isExpand) {
		if (isExpand)
			txtExpand.setText(R.string.str_unexpand);
		else
			txtExpand.setText(R.string.str_expand);
	}
}
