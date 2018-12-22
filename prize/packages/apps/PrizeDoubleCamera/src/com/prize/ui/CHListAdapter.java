package com.prize.ui;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.camera.R;

public class CHListAdapter extends RecyclerView.Adapter<CHListAdapter.ViewHolder> {

	public interface OnItemClickLitener {
		void onItemClick(View view, int position);
	}

	private OnItemClickLitener mOnItemClickLitener;

	public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
		this.mOnItemClickLitener = mOnItemClickLitener;
	}

	private LayoutInflater mInflater;
	private List<String> mDatas;
	public CHListAdapter(Context context, List<String> datats) {
		mInflater = LayoutInflater.from(context);
		mDatas = datats;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View arg0) {
			super(arg0);
		}

		TextView mTxt;
	}

	@Override
	public int getItemCount() {
		return Integer.MAX_VALUE;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View view = mInflater.inflate(R.layout.center_scorll_item, viewGroup,
				false);
		ViewHolder viewHolder = new ViewHolder(view);

		viewHolder.mTxt = (TextView) view.findViewById(R.id.tv);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
		viewHolder.mTxt.setText(mDatas.get(i % mDatas.size()));
		if (mOnItemClickLitener != null) {
			viewHolder.itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mOnItemClickLitener.onItemClick(viewHolder.itemView, i);
				}
			});
		}

	}

}
