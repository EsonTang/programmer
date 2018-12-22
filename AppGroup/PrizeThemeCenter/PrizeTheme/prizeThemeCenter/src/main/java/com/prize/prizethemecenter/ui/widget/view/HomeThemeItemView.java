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

package com.prize.prizethemecenter.ui.widget.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.ThemeItemBean;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.widget.CornerImageView;

public class HomeThemeItemView extends RelativeLayout {

	public HomeThemeItemView(Context context) {

		super(context);
		init(context);
	}

	public HomeThemeItemView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {

		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);

	}

	public HomeThemeItemView(Context context, AttributeSet attrs, int defStyleAttr) {

		super(context, attrs, defStyleAttr);
		init(context);

	}

	public HomeThemeItemView(Context context, AttributeSet attrs) {

		super(context, attrs);
		init(context);

	}
	
	private void init(Context context) {
		View convertView = LayoutInflater.from(context).inflate(
				R.layout.item_theme_layout, this);
		theme_logo = (CornerImageView) convertView
				.findViewById(R.id.theme_logo);
		theme_title = (TextView) convertView
				.findViewById(R.id.theme_title);
		theme_prize = (TextView) convertView
				.findViewById(R.id.theme_prize);
	}

	CornerImageView theme_logo;
	TextView theme_title;
	TextView theme_prize;
	ThemeItemBean mRadioItemBean;

	public void setmCollectBean(ThemeItemBean bean) {
		this.mRadioItemBean = bean;
		if(bean!=null && !TextUtils.isEmpty(bean.name)){
			theme_title.setText(bean.name);
		}
		if(bean!=null && !TextUtils.isEmpty(bean.price)){
			theme_prize.setText(bean.price);
		}
		if(bean!=null && bean.ad_pictrue!=null){
			ImageLoader.getInstance().displayImage(bean.ad_pictrue,theme_logo,
					UILimageUtil.getHomeThemeDpLoptions(), null);
		}
		
	}
	public void defaultTransState() {
		theme_title.setText("");
		theme_prize.setText("");
		theme_logo.setImageResource(R.drawable.tranport_shape);
		this.mRadioItemBean=null;
	}
}
