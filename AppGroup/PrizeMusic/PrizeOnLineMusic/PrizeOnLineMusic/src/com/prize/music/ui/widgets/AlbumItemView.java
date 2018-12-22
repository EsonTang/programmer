/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.music.ui.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.util.ToastUtils;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.R;
import com.prize.onlinemusibean.AlbumBean;
import com.xiami.sdk.utils.ImageUtil;

public class AlbumItemView extends RelativeLayout {

	public AlbumItemView(Context context) {

		super(context);
		init(context);
	}

	public AlbumItemView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {

		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);

	}

	public AlbumItemView(Context context, AttributeSet attrs, int defStyleAttr) {

		super(context, attrs, defStyleAttr);
		init(context);

	}

	public AlbumItemView(Context context, AttributeSet attrs) {

		super(context, attrs);
		init(context);

	}

	private void init(Context context) {
		View convertView = LayoutInflater.from(context).inflate(
				R.layout.item_album_view_layout, this);
		album_logo = (ImageView) convertView.findViewById(R.id.album_logo);
		albumName_Tv = (TextView) convertView.findViewById(R.id.albumName_Tv);
		play_Iv = (ImageView) convertView.findViewById(R.id.play_Iv);
	}

	// 榜单排行图标
	ImageView album_logo;
	ImageView play_Iv;
	// 榜单排行名称
	TextView albumName_Tv;

	AlbumBean albumBean;

	public void setAlbumItemBean(AlbumBean bean) {
		this.albumBean = bean;
//		if (this.albumBean == null) {
//			albumName_Tv.setText("");
//			album_logo.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
//			return;
//		}
		albumName_Tv.setText(bean.album_name);
		ImageLoader.getInstance().displayImage(
				ImageUtil.transferImgUrl(bean.album_logo, 220), album_logo,
				UILimageUtil.getTwoOneZeroDpLoptions(), null);

	}
	
	public void defaultTransState() {
		albumName_Tv.setText("");
		album_logo.setImageResource(R.drawable.tranport_shape);
		this.albumBean=null;
	}
	
}
