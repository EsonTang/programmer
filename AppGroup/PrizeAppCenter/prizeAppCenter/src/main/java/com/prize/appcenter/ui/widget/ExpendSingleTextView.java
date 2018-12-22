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
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.appcenter.R;

/**
 * 类描述：点击展开textView的自定义控件
 * 
 * @author huanglingjun
 * @version 1.0
 */
public class ExpendSingleTextView extends LinearLayout implements
		OnClickListener {

	private static final int DEFAULT_MAX_LINE_COUNT = 1;

	private static final int COLLAPSIBLE_STATE_NONE = 0;
	private static final int COLLAPSIBLE_STATE_SHRINKUP = 1;
	private static final int COLLAPSIBLE_STATE_SPREAD = 2;

	private Context mContext;
	private TextView mContentDesc;
	private TextView mIntroduce;
	private LinearLayout container_Llyt;

	private int mState;
	private boolean flag;

	public ExpendSingleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		View view = inflate(context, R.layout.single_expend_textview, this);
		mContentDesc = (TextView) view
				.findViewById(R.id.app_content_introduce_id);
		mIntroduce = (TextView) view.findViewById(R.id.app_introduce_id);
		container_Llyt = (LinearLayout) view.findViewById(R.id.container_Llyt);
		mIntroduce.setOnClickListener(this);
		mContentDesc.setOnClickListener(this);
		container_Llyt.setOnClickListener(this);
		mContentDesc.setMaxLines(DEFAULT_MAX_LINE_COUNT);
	}

	public ExpendSingleTextView(Context context) {
		this(context, null);
	}

	public final void setContentDesc(CharSequence title,
			CharSequence contentDesc) {
		mIntroduce.setText(title);
		if (contentDesc == null || contentDesc.length() <= 0) {
			mContentDesc.setText(mContext.getResources().getString(
					R.string.none));
		} else {
			mContentDesc.setText(contentDesc);
		}
		mState = COLLAPSIBLE_STATE_SPREAD;
		requestLayout();
	}

	// public final void setRelayoutDesc(CharSequence title,
	// CharSequence contentDesc, CharSequence update, CharSequence version) {
	// mRelayout.setVisibility(View.VISIBLE);
	// mIntroduce.setText(title);
	// mContentDesc.setText(contentDesc);
	// mUpdate.setText("更新时间：" + update);
	// mVersion.setText("版本：" + version);
	// mIntroduce.setText(mContext.getString(R.string.app_update_log));
	// mState = COLLAPSIBLE_STATE_SPREAD;
	// requestLayout();
	// }

	@Override
	public void onClick(View v) {
		flag = false;
		requestLayout();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (!flag) {
			flag = true;
			if (mContentDesc.getLineCount() <= DEFAULT_MAX_LINE_COUNT) {
				mState = COLLAPSIBLE_STATE_NONE;
				// mIntroduce.setVisibility(View.GONE);
				mContentDesc.setMaxLines(DEFAULT_MAX_LINE_COUNT);
			} else {
				post(new InnerRunnable());
			}
		}
	}

	class InnerRunnable implements Runnable {
		@Override
		public void run() {
			if (mState == COLLAPSIBLE_STATE_SPREAD) {
				mContentDesc.setMaxLines(DEFAULT_MAX_LINE_COUNT);
				mIntroduce.setCompoundDrawablesWithIntrinsicBounds(0, 0,
						R.drawable.icon_up, 0);
				// mIntroduce.setVisibility(View.VISIBLE);
				// mIntroduce.setText(spread);
				// mIntroduce.setCompoundDrawables(null, null,
				// mContext.getResources().getDrawable(R.drawable.prize_drop_icon),
				// null);

				mState = COLLAPSIBLE_STATE_SHRINKUP;
			} else if (mState == COLLAPSIBLE_STATE_SHRINKUP) {
				mContentDesc.setMaxLines(Integer.MAX_VALUE);
				// mIntroduce.setVisibility(View.VISIBLE);
				// mIntroduce.setText(shrinkup);
				// / mIntroduce.setCompoundDrawables(null, null,
				// mContext.getResources().getDrawable(R.drawable.prize_drop_icon),
				// null);
				mIntroduce.setCompoundDrawablesWithIntrinsicBounds(0, 0,
						R.drawable.icon_down, 0);
				mState = COLLAPSIBLE_STATE_SPREAD;
			}
		}
	}
}
