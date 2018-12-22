package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.appcenter.R;

/**
 * 类描述：详情页viewpager指示器
 * 
 * @author huanglingjun
 * @version 版本
 */
public class DownLoadViewPagerIndicator extends LinearLayout {
	private static final int COLOR_TEXT_NORMAL = 0xff616161;
	private static final int COLOR_INDICATOR_COLOR = 0xFFe6691e;
	private Paint mPaint = new Paint();
	private TextView detail;
	private TextView comment;

	private CallBackInfoTwo info;

	public DownLoadViewPagerIndicator(Context context) {
		this(context, null);
	}

	public DownLoadViewPagerIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint.setColor(COLOR_INDICATOR_COLOR);
		mPaint.setStrokeWidth(9.0F);
		initUI(context);
	}

	private void initUI(Context context) {
		View view = inflate(context, R.layout.viewpager_indicator, this);
		detail = (TextView) view.findViewById(R.id.detail_id);
		comment = (TextView) view.findViewById(R.id.comment_id);
		detail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				info.setPosition(0);
			}
		});
		comment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				info.setPosition(1);
			}
		});
	}

	public void setData(String tv_one, String tv_two) {
		detail.setText(tv_one);
		comment.setText(tv_two);
	}

	public interface CallBackInfoTwo {
		void setPosition(int position);
	}

	public void setCallBackInfo(CallBackInfoTwo info) {
		this.info = info;
	}

	public void scroll(int position) {
		if (position == 0) {
			detail.setTextColor(COLOR_INDICATOR_COLOR);
			comment.setTextColor(COLOR_TEXT_NORMAL);
		} else if (position == 1) {
			comment.setTextColor(COLOR_INDICATOR_COLOR);
			detail.setTextColor(COLOR_TEXT_NORMAL);
		}
	}

}
