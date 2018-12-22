package com.prize.left.page.test;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.test.TestAdapter.NaviViewHolder;
/***
 * recycleView适配器
 * @author fanjunchen
 *
 */
public class TestAdapter extends RecyclerView.Adapter<NaviViewHolder> {

	private Context mContext;
	
	int lastPosition = -1;
	
	public TestAdapter(Context context) {
		this.mContext = context;
	}
	
	@Override
	public NaviViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		// 给ViewHolder设置布局文件
		View v = LayoutInflater.from(mContext).inflate(
				R.layout.test_card_item, viewGroup, false);
		return new NaviViewHolder(v);
	}

	@Override
	public void onBindViewHolder(NaviViewHolder viewHolder, int i) {
		// 给ViewHolder设置元素
		NaviViewHolder nsHolder = viewHolder;
	}

	/*protected void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.item_slide_bottom_up);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }*/
	
	@Override
    public void onViewDetachedFromWindow(NaviViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
	
	@Override
	public int getItemCount() {
		// 返回数据总数
		return 0;
	}
	
	public class NaviViewHolder extends RecyclerView.ViewHolder {
		
		public TextView titleTxt;

		public ImageView imgIco;
		
		public NaviViewHolder(View v) {
			super(v);
			titleTxt = (TextView) v.findViewById(R.id.txt_title);
			imgIco = (ImageView) v.findViewById(R.id.img_ico);
		}
	}
	
	public void addItem(int pos) {
		//actors.add(pos, tmp);
        //notifyItemInserted(pos);
        notifyItemMoved(pos, 1);
    }

    public void removeItem(int pos) {
        notifyItemRemoved(pos);
        //addItem(pos, item);
    }
}
