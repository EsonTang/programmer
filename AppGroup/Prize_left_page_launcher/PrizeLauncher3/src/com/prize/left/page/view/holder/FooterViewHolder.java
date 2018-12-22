package com.prize.left.page.view.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.android.launcher3.R;
/***
 * recycleView脚信息
 * @author fanjunchen
 *
 */
public class FooterViewHolder extends RecyclerView.ViewHolder {
	/**中间的文字及搜索框*/
	public TextView txtAdd;

	public FooterViewHolder(View v) {
		super(v);
		txtAdd = (TextView) v.findViewById(R.id.txt_add_card);
	}
	
	public void doBindImg() {
		
	}
}
