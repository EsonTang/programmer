package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.prize.appcenter.R;

/**
 * 类描述：详情页viewpager指示器
 * 
 * @author huanglingjun
 * @version 版本
 */
public class SimpleViewPagerIndicator extends LinearLayout {

	private static final int COLOR_TEXT_NORMAL = 0xff616161;
	private static final int COLOR_INDICATOR_COLOR = 0xFFe6691e;

	private String[] mTitles;
	private int mTabCount;
	private int mIndicatorColor = COLOR_INDICATOR_COLOR;
	private float mTranslationX;
	private Paint mPaint = new Paint();
	private int mTabWidth;
	private TextView detail;
	private TextView comment;

	private ScrollView mParentScrollView;
	private CallBackInfo info;

	public SimpleViewPagerIndicator(Context context) {
		this(context, null);
	}

	public SimpleViewPagerIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint.setColor(mIndicatorColor);
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

	public interface CallBackInfo {
		void setPosition(int position);
	}

	public void setParentScrollView(ScrollView scrollView) {
		this.mParentScrollView = scrollView;
	}

	public void setCallBackInfo(CallBackInfo info) {
		this.info = info;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mParentScrollView.getScrollY() + mParentScrollView.getHeight() == mParentScrollView
				.getChildAt(0).getHeight()) {
			mParentScrollView.requestDisallowInterceptTouchEvent(true);
		}
		return super.dispatchTouchEvent(ev);
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

	/*
	 * private void generateTitleView() { if (getChildCount() > 0)
	 * this.removeAllViews(); int count = mTitles.length;
	 * 
	 * setWeightSum(count); for (int i = 0; i < count; i++) { TextView tv = new
	 * TextView(getContext()); LinearLayout.LayoutParams lp = new
	 * LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT); lp.weight = 1;
	 * lp.gravity = Gravity.CENTER; tv.setGravity(Gravity.CENTER);
	 * tv.setTextColor(COLOR_TEXT_NORMAL); //tv.setPadding(40, 0, 0, 0);
	 * tv.setText(mTitles[i]); tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
	 * tv.setLayoutParams(lp); tv.setOnClickListener(new OnClickListener() {
	 * 
	 * @Override public void onClick(View v) {
	 * 
	 * } }); //addView(tv); } }
	 */

	/*
	 * @Override protected void onSizeChanged(int w, int h, int oldw, int oldh)
	 * { super.onSizeChanged(w, h, oldw, oldh); mTabWidth = w / mTabCount; }
	 * 
	 * public void setTitles(String[] titles) { mTitles = titles; mTabCount =
	 * titles.length; generateTitleView();
	 * 
	 * }
	 * 
	 * public void setIndicatorColor(int indicatorColor) { this.mIndicatorColor
	 * = indicatorColor; }
	 * 
	 * @Override protected void dispatchDraw(Canvas canvas) {
	 * super.dispatchDraw(canvas); mPaint.setColor(Color.BLUE); canvas.save();
	 * canvas.translate(mTranslationX, getHeight() - 2); //canvas.drawLine(0, 0,
	 * mTabWidth, 0, mPaint); canvas.restore(); }
	 * 
	 * public void scroll(int position, float offset) {
	 *//**
	 * <pre>
	 *  0-1:position=0 ;1-0:postion=0;
	 * </pre>
	 */
	/*
	 * mTranslationX = getWidth() / mTabCount * (position + offset);
	 * invalidate(); }
	 */

}
