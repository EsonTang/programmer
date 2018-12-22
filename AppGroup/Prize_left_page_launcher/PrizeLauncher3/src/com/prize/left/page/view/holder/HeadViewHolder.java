package com.prize.left.page.view.holder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.R;
/***
 * recycleView头信息
 * @author fanjunchen
 *
 */
public class HeadViewHolder extends RecyclerView.ViewHolder {
	/**弹出左边菜单的图标*/
	public ImageView menuView;
	/**中间的文字及搜索框*/
//	public TextView txtCenter, txtAddr;
	
	public View txtSearch;

	public HeadViewHolder(View v) {
		super(v);
		// txtCenter = (TextView) v.findViewById(R.id.txt_center);
		menuView = (ImageView) v.findViewById(R.id.img_menu);

		txtSearch = v.findViewById(R.id.lay_search);
//		txtAddr = (TextView) v.findViewById(R.id.txt_addr);
		
	}
	
//	public void setAddrText(String str) {
//		if (txtAddr!= null && !TextUtils.isEmpty(str))
//			txtAddr.setText(str);
//	}
	
	public void doBindImg() {
		
	}
}
