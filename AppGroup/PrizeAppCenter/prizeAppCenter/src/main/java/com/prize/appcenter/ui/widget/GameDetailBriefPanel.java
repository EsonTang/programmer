package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.appcenter.R;

/**
 * 游戏简介面板
 * 
 * @author prize
 * 
 */
public class GameDetailBriefPanel extends LinearLayout implements
		OnClickListener {
	private static final String TAG = "GameDetailBriefPanel";
	// 简介标题
	private TextView briefTitleTV;
	// 简介展开Btn
	private ImageView briefExpandBtn;
	// 简介展开文字
	private TextView briefExpandTV;
	// 简介内容View
	private TextView briefTV;
	// 简介内容字符串
	private CharSequence brief;
	// 显示部分或者全部简介
	private boolean isShowAll;

	public GameDetailBriefPanel(Context context) {
		super(context);
		init(context);
	}

	public GameDetailBriefPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.activity_gamedetail_briefpanel, this);
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.brief_panel_titlebar);
		briefTitleTV = (TextView) findViewById(R.id.panel_title_tv);
		briefTitleTV.setText(R.string.gamedetail_brief_title);
		briefExpandBtn = (ImageView) findViewById(R.id.panel_title_iv);
		rl.setOnClickListener(this);
		briefExpandBtn.setVisibility(View.VISIBLE);
		briefExpandTV = (TextView) findViewById(R.id.panel_expand_tv);
		briefExpandTV.setVisibility(View.VISIBLE);
		briefTV = (TextView) findViewById(R.id.brief_panel_tv);
		briefTV.setOnClickListener(this);
	}

	// 设置显示属性
	public void setVisiable(boolean isVisiable) {
		int visible = isVisiable ? View.VISIBLE : View.GONE;
		setVisibility(visible);
	}

	// 设置显示内容
	public void setBriefContent(String inBrief) {
		brief = Html.fromHtml(inBrief); // 搜索得来的string 包含HTML 标签

		if (!TextUtils.isEmpty(brief)) {
			briefTV.setText(brief);
		}

		showShortContent(brief);
		isShowAll = false;
		setVisiable(!TextUtils.isEmpty(brief));
	}

	// 简介点击响应
	@Override
	public void onClick(View v) {
		if (isShowAll) {
			isShowAll = false;
			showShortContent(brief);
			briefExpandBtn
					.setBackgroundResource(R.drawable.panel_title_iv_down);
		} else {
			isShowAll = true;
			showAllContent(brief);
			briefExpandBtn.setBackgroundResource(R.drawable.panel_title_iv_up);
		}
	}

	private void showShortContent(CharSequence brief) {
		briefTV.setMaxLines(5);
		briefTV.setEllipsize(TextUtils.TruncateAt.END);
		briefExpandTV.setText(R.string.gamedetail_brief_expend);
		// JLog.d(TAG, "showShortContent brief = " + brief);
	}

	private void showAllContent(CharSequence brief) {
		briefTV.setMaxLines(100);
		briefTV.setEllipsize(null);
		briefExpandTV.setText(R.string.gamedetail_brief_unexpend);

		// JLog.d(TAG, "showAllContent brief = " + brief);
	}
}
