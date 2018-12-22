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

package com.prize.appcenter.ui.widget;

import android.app.Activity;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.home.CarParentBean;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UILimageUtil;

/**
 * 类描述：WebView card
 *
 * @author 龙宝修
 * @version 1.0
 */
public class CardWebView extends LinearLayout {
	private ImageView mIcon;
	public CardWebView(Activity context) {
		super(context);
		mContext = context;
		setOrientation(VERTICAL);
		View view = inflate(context, R.layout.card_webview, this);
		findViewById(view);
	}

	public CardWebView(Activity context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		setOrientation(VERTICAL);
		setGravity(Gravity.CENTER_VERTICAL);
		View view = inflate(context, R.layout.card_webview, this);
		findViewById(view);
	}

	private void findViewById(View view) {
		mIcon = (ImageView) findViewById(R.id.card_listview_icon_id);
	}

	public void setData(CarParentBean bean) {
		if (bean == null) return;
		ImageLoader.getInstance().displayImage(bean.focus.imageUrl, mIcon, UILimageUtil
				.getUILoptions(R.drawable.topic_icon_background), null);
	}


}
