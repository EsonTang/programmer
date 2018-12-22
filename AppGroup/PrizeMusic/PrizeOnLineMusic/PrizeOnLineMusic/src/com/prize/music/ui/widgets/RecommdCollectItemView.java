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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.R;
import com.prize.onlinemusibean.CollectBean;
import com.prize.onlinemusibean.RadioItemBean;
import com.xiami.sdk.utils.ImageUtil;

public class RecommdCollectItemView extends RelativeLayout {

	public RecommdCollectItemView(Context context) {

		super(context);
		init(context);
	}

	public RecommdCollectItemView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {

		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);

	}

	public RecommdCollectItemView(Context context, AttributeSet attrs, int defStyleAttr) {

		super(context, attrs, defStyleAttr);
		init(context);

	}

	public RecommdCollectItemView(Context context, AttributeSet attrs) {

		super(context, attrs);
		init(context);

	}
	
	private void init(Context context) {
		View convertView = LayoutInflater.from(context).inflate(
				R.layout.item_collect_layout, this);
		rank_logo = (ImageView) convertView
				.findViewById(R.id.rank_logo);
		play_Iv = (ImageView) convertView
				.findViewById(R.id.play_Iv);
		collectName_Tv = (TextView) convertView
				.findViewById(R.id.collectName_Tv);
		icon_fly = (ImageView) convertView
				.findViewById(R.id.icon_fly);
	}

	// 榜单排行图标
	ImageView rank_logo;
	ImageView play_Iv;
	// 榜单排行名称
	TextView collectName_Tv;
	ImageView icon_fly;
	CollectBean mRadioItemBean;

	public void setmCollectBean(CollectBean bean) {
		this.mRadioItemBean = bean;
		collectName_Tv.setText(bean.collect_name);
		ImageLoader.getInstance().displayImage(ImageUtil.transferImgUrl(bean.collect_logo, 220),rank_logo,
				UILimageUtil.getTwoOneZeroDpLoptions(), null);

		// requestLayout();
	}
	public void defaultTransState() {
		collectName_Tv.setText("");
		rank_logo.setImageResource(R.drawable.tranport_shape);
		this.mRadioItemBean=null;
	}



}
