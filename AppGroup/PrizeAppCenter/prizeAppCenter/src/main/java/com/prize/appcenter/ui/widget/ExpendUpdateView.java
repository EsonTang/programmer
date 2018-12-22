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

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.beans.GiftPkgItemBean;
import com.prize.appcenter.R;

/**
 * 类描述：点击展开textView的自定义控件
 * 
 * @author huanglingjun
 * @version 1.0
 */
public class ExpendUpdateView extends LinearLayout {
	// 默认显示多少行
	private static final int DEFAULT_MAX_LINE_COUNT = 1;
	// 使用方法
	private TextView user_way_content;

	private TextView gift_content;
	private TextView Exchange_period;
	private boolean isClick=false;
	/**展开**/
	private static final int COLLAPSIBLE_STATE_SHRINKUP = 1;
	/**折叠**/
	private static final int COLLAPSIBLE_STATE_SPREAD = 2;
	private int mState=COLLAPSIBLE_STATE_SPREAD;
	private LinearLayout expendView;
	private RelativeLayout expend_Rlyt;
	private ImageView expend_Iv;
	public ExpendUpdateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		View view = inflate(context, R.layout.expend_giftview, this);
		gift_content = (TextView) findViewById(R.id.gift_content);
		Exchange_period = (TextView) findViewById(R.id.Exchange_period);
		user_way_content = (TextView) findViewById(R.id.user_way_content);
		expendView = (LinearLayout) findViewById(R.id.expendView);
		expend_Rlyt = (RelativeLayout) findViewById(R.id.expend_Rlyt);
		expend_Iv = (ImageView) findViewById(R.id.expend_Iv);
		gift_content.setMaxLines(DEFAULT_MAX_LINE_COUNT);
		expend_Rlyt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isClick = false;
				requestLayout();
			}
		});
		expendView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isClick = false;
				requestLayout();
			}
		});
	}

	public ExpendUpdateView(Context context) {
		this(context, null);
	}

	public final void setContentDesc(GiftPkgItemBean gameBean ) {
		gift_content.setText(gameBean.content.trim());
		if (TextUtils.isEmpty(gameBean.usage)) {
			user_way_content.setText("");
		} else {
			user_way_content.setText(gameBean.usage.trim());
		}
		Exchange_period.setText(gameBean.startTime + "—"
				+ gameBean.endTime);
		mState = COLLAPSIBLE_STATE_SPREAD;
		requestLayout();
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(!isClick){
			isClick=true;
			post(new ExpendUpdateView.InnerRunnable());
		}
	}


	class InnerRunnable implements Runnable {
		@Override
		public void run() {
			if (mState == COLLAPSIBLE_STATE_SPREAD) {
				gift_content.setMaxLines(DEFAULT_MAX_LINE_COUNT);
				expendView.setVisibility(View.GONE);
				expend_Iv.setBackgroundResource(R.drawable.icon_up);
				mState = COLLAPSIBLE_STATE_SHRINKUP;
			} else if (mState == COLLAPSIBLE_STATE_SHRINKUP) {
				gift_content.setMaxLines(Integer.MAX_VALUE);
				expend_Iv.setBackgroundResource(R.drawable.icon_down);
				expendView.setVisibility(View.VISIBLE);
				mState = COLLAPSIBLE_STATE_SPREAD;
			}
		}
	}
}
