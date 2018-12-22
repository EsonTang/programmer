package com.prize.left.page.view.holder;

import com.android.launcher3.R;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

/***
 * recycleView脚信息
 * @author fanjunchen
 *
 */
public class ImgViewHolder extends RecyclerView.ViewHolder {
	/**新闻*/
	public static final int TYPE_NEWS = 0;
	/**电影*/
	public static final int TYPE_MOVIE = TYPE_NEWS + 1;
	/**团购*/
	public static final int TYPE_GROUPON = TYPE_MOVIE + 1;
	/**导航*/
	public static final int TYPE_NAVI = TYPE_GROUPON + 1;
	/**建议*/
	public static final int TYPE_SUGG = TYPE_NAVI + 1;
	/**热门搜索*/
	public static final int TYPE_HOTW = TYPE_SUGG + 1;
	
	/**示例图片*/
	public ImageView imgDemo;

	public ImgViewHolder(View v) {
		super(v);
		imgDemo = (ImageView) itemView;
	}
	
	public void doBindImg() {
		
	}
	
	public void setType(int t) {
		switch (t) {
			case TYPE_NEWS:
				imgDemo.setImageResource(R.drawable.left_demo_news);
				break;
			case TYPE_MOVIE:
				imgDemo.setImageResource(R.drawable.left_demo_movie);
				break;
			case TYPE_GROUPON:
				imgDemo.setImageResource(R.drawable.left_demo_groupon);
				break;
			case TYPE_NAVI:
				imgDemo.setImageResource(R.drawable.left_demo_groupon);
				break;
			case TYPE_SUGG:
				imgDemo.setImageResource(R.drawable.left_demo_suggest);
				break;
			case TYPE_HOTW:
				imgDemo.setImageResource(R.drawable.left_demo_hotword);
				break;
		}
	}
}
