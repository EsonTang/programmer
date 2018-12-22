/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import com.android.gallery3d.R;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.StickerViewHolder> {

	public interface OnRecyclerItemClickLitener {
		void onItemClick(View view, int position);

		void onItemLongClick(View view, int position);
	}
    private Context mContext;
    private List<WatermarkBean> mWatermarkBeans = new ArrayList<WatermarkBean>();
    
    public StickerAdapter(Context context) {
    	mContext = context;
    }
    
    public StickerAdapter(Context context, List<WatermarkBean> watermarkBeans) {
    	mContext = context;
    	mWatermarkBeans.clear();
    	mWatermarkBeans.addAll(watermarkBeans);
    }
    
	public WatermarkBean getItem(int position) {
		// TODO Auto-generated method stub
		return mWatermarkBeans.get(position);
	}
	@Override
	public StickerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		StickerViewHolder holder = new StickerViewHolder(LayoutInflater.from(
				mContext).inflate(R.layout.prize_pick_sticker_item, parent,
				false));
		return holder;
	}
	
	@Override
	public void onBindViewHolder(final StickerViewHolder holder, int position) {
		holder.im.setImageResource(mWatermarkBeans.get(position).getThumbId());
		// 如果设置了回调，则设置点击事件
		if (mOnItemClickLitener != null) {
			holder.im.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = holder.getLayoutPosition();
					mOnItemClickLitener.onItemClick(holder.im, pos);
				}
			});

			holder.im.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					int pos = holder.getLayoutPosition();
					mOnItemClickLitener.onItemLongClick(holder.im, pos);
					return false;
				}
			});
		}
	}

	@Override
	public int getItemCount() {
		return mWatermarkBeans.size();
	}

	private OnRecyclerItemClickLitener mOnItemClickLitener;

	public void setOnItemClickLitener(OnRecyclerItemClickLitener mOnItemClickLitener) {
		this.mOnItemClickLitener = mOnItemClickLitener;
	}

	class StickerViewHolder extends ViewHolder {

		ImageView im;

		public StickerViewHolder(View view) {
			super(view);
			im = (ImageView) view.findViewById(R.id.id_stick);
		}
	}
}
