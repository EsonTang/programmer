/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.viewpagerindicator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * This widget implements the dynamic action bar tab behavior that can change
 * across different configurations or circumstances.
 */
public class TabTextPageIndicator extends HorizontalScrollView implements
		PageIndicator {
	/** Title text used when no title is provided by the adapter. */
	private static final CharSequence EMPTY_TITLE = "";

	/**
	 * Interface for a callback when the selected tab has been reselected.
	 */
	public interface OnTabReselectedListener {
		/**
		 * Callback when the selected tab has been reselected.
		 * 
		 * @param position
		 *            Position of the current center item.
		 */
		void onTabReselected(int position);
	}

	private Runnable mTabSelector;

	private final OnClickListener mTabClickListener = new OnClickListener() {
		public void onClick(View view) {
			TabView tabView = (TabView) view;
			final int oldSelected = mViewPager.getCurrentItem();
			final int newSelected = tabView.getIndex();
			mViewPager.setCurrentItem(newSelected);
			if (oldSelected == newSelected && mTabReselectedListener != null) {
				mTabReselectedListener.onTabReselected(newSelected);
			}
		}
	};

	private final IcsLinearLayout mTabLayout;
	// private final LinearLayout mTabLayout;

	private ViewPager mViewPager;
	private ViewPager.OnPageChangeListener mListener;

	private int mMaxTabWidth;
	private int mSelectedTabIndex;

	private OnTabReselectedListener mTabReselectedListener;

	private boolean isLayoutWeight = false;// 判断是否平均显示
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Boolean> newsCirle = new HashMap<Integer, Boolean>();// 显示是否有新的新闻

	public boolean getNewsCirle(int index) {
		if (newsCirle.get(index) == null) {
			return false;
		}
		return newsCirle.get(index);
	}

	public void setNewsCirle(int index, boolean isNews) {
		newsCirle.put(index, isNews);
	}

	public TabTextPageIndicator(Context context) {
		this(context, null);
	}

	public void setLayoutWeight(boolean isLayoutWeight) {
		this.isLayoutWeight = isLayoutWeight;
	}

	public TabTextPageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		setHorizontalScrollBarEnabled(false);
		this.setHorizontalFadingEdgeEnabled(false);
		mTabLayout = new IcsLinearLayout(context,
				R.attr.vpiTabPageIndicatorStyle);
		// mTabLayout = new LinearLayout(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER;
		mTabLayout.setLayoutParams(params);
		mTabLayout.setOrientation(LinearLayout.HORIZONTAL);
		addView(mTabLayout, new ViewGroup.LayoutParams(WRAP_CONTENT,
				MATCH_PARENT));
	}

	public void setOnTabReselectedListener(OnTabReselectedListener listener) {
		mTabReselectedListener = listener;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final boolean lockedExpanded = widthMode == MeasureSpec.EXACTLY;
		setFillViewport(lockedExpanded);

		final int childCount = mTabLayout.getChildCount();
		if (childCount > 1
				&& (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST)) {
			if (childCount > 2) {
				mMaxTabWidth = (int) (MeasureSpec.getSize(widthMeasureSpec) * 0.4f);
			} else {
				mMaxTabWidth = MeasureSpec.getSize(widthMeasureSpec) / 2;
			}
		} else {
			mMaxTabWidth = -1;
		}

		final int oldWidth = getMeasuredWidth();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int newWidth = getMeasuredWidth();

		if (lockedExpanded && oldWidth != newWidth) {
			// Recenter the tab display if we're at a new (scrollable) size.
			setCurrentItem(mSelectedTabIndex);
		}
	}

	private void animateToTab(final int position) {
		final View tabView = mTabLayout.getChildAt(position);
		if (mTabSelector != null) {
			removeCallbacks(mTabSelector);
		}
		mTabSelector = new Runnable() {
			public void run() {
				final int scrollPos = tabView.getLeft()
						- (getWidth() - tabView.getWidth()) / 2;
				smoothScrollTo(scrollPos, 0);
				mTabSelector = null;
			}
		};
		post(mTabSelector);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mTabSelector != null) {
			// Re-post the selector we saved
			post(mTabSelector);
		}
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mTabSelector != null) {
			removeCallbacks(mTabSelector);
		}
	}

	private void addTab(int index, CharSequence text, int layoutID,
			boolean showLine) {
		final TabView tabView = new TabView(getContext());
		tabView.mIndex = index;
		tabView.setOnClickListener(mTabClickListener);
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.pager_title, null);
		TextView tv = (TextView) view.findViewById(R.id.pager_title);
		tv.setText(text);
		// 设置字体加粗
		// tv.getPaint().setFakeBoldText(true);
		// setTextColor(tv);
		// ImageView newsIv = (ImageView) view.findViewById(R.id.pager_news);
		// if (getNewsCirle(index)) {
		// newsIv.setVisibility(View.VISIBLE);
		// }
		// ImageView line = (ImageView) view.findViewById(R.id.pager_line);
		// if (!showLine) {
		// line.setVisibility(View.INVISIBLE);
		// }

		tabView.addView(view);
		tabView.setGravity(Gravity.CENTER);
		if (isLayoutWeight) {
			mTabLayout.addView(tabView, new LinearLayout.LayoutParams(0,
					WRAP_CONTENT, 1));
		} else {
			mTabLayout.addView(tabView, new LinearLayout.LayoutParams(
					WRAP_CONTENT, WRAP_CONTENT));
		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		if (mListener != null) {
			mListener.onPageScrollStateChanged(arg0);
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		if (mListener != null) {
			mListener.onPageScrolled(arg0, arg1, arg2);
		}
	}

	@Override
	public void onPageSelected(int arg0) {
		setCurrentItem(arg0);
		if (mListener != null) {
			mListener.onPageSelected(arg0);
		}
	}

	@Override
	public void setViewPager(ViewPager view) {
		if (mViewPager == view) {
			return;
		}
		if (mViewPager != null) {
			mViewPager.setOnPageChangeListener(null);
		}
		final PagerAdapter adapter = view.getAdapter();
		if (adapter == null) {
			throw new IllegalStateException(
					"ViewPager does not have adapter instance.");
		}
		mViewPager = view;
		view.setOnPageChangeListener(this);
		notifyDataSetChanged();
	}

	public void notifyDataSetChanged() {
		mTabLayout.removeAllViews();
		PagerAdapter adapter = mViewPager.getAdapter();
		IconPagerAdapter iconAdapter = null;
		if (adapter instanceof IconPagerAdapter) {
			iconAdapter = (IconPagerAdapter) adapter;
		}
		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			CharSequence title = adapter.getPageTitle(i);
			if (title == null) {
				title = EMPTY_TITLE;
			}
			int layoutID = 0;
			if (iconAdapter != null) {
				layoutID = iconAdapter.getIconResId(i);
			}
			if (i == count - 1) {
				addTab(i, title, layoutID, false);
			} else {
				addTab(i, title, layoutID, true);
			}
		}
		if (mSelectedTabIndex > count) {
			mSelectedTabIndex = count - 1;
		}
		setCurrentItem(mSelectedTabIndex);
		requestLayout();
	}

	@Override
	public void setViewPager(ViewPager view, int initialPosition) {
		setViewPager(view);
		setCurrentItem(initialPosition);
	}

	@Override
	public void setCurrentItem(int item) {
		if (mViewPager == null) {
			throw new IllegalStateException("ViewPager has not been bound.");
		}
		mSelectedTabIndex = item;
		mViewPager.setCurrentItem(item);

		final int tabCount = mTabLayout.getChildCount();
		for (int i = 0; i < tabCount; i++) {
			final View child = mTabLayout.getChildAt(i);
			final boolean isSelected = (i == item);
			child.setSelected(isSelected);
			if (isSelected) {
				animateToTab(item);
			}
		}
	}

	@Override
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mListener = listener;
	}

	private class TabView extends LinearLayout {

		private int mIndex;

		public TabView(Context context) {
			super(context);
		}

		public TabView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			// Re-measure if we went beyond our maximum size.
			if (mMaxTabWidth > 0 && getMeasuredWidth() > mMaxTabWidth) {
				super.onMeasure(MeasureSpec.makeMeasureSpec(mMaxTabWidth,
						MeasureSpec.EXACTLY), heightMeasureSpec);
			}
		}

		public int getIndex() {
			return mIndex;
		}
	}

//	/**
//	 * 修改textview部分文字的颜色
//	 *
//	 * @param textView
//	 */
	// private void setTextColor(TextView textView) {
	// if (textView == null) {
	// return;
	// }
	// String textString = textView.getText().toString();
	// if (textString.isEmpty()) {
	// return;
	// }
	// int start = textString.indexOf("(");
	// int end = textString.indexOf(")");
	// if (start < 0 || end < 0 || end < start) {
	// return;
	// }
	// SpannableStringBuilder builder = new SpannableStringBuilder(textString);
	// // ForegroundColorSpan 为文字前景色，BackgroundColorSpan为文字背景色
	// ForegroundColorSpan redSpan = new ForegroundColorSpan(
	// Color.parseColor("#ff0000"));
	// builder.setSpan(redSpan, start, end + 1,
	// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	// // 部分字体不加粗
	// builder.setSpan(new MyStyleSpan(Typeface.NORMAL), start, end + 1,
	// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	//
	// textView.setText(builder);
	// }

	class MyStyleSpan extends StyleSpan {

		public MyStyleSpan(int style) {
			super(style);
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setFakeBoldText(false);
			super.updateDrawState(ds);
		}

		@Override
		public void updateMeasureState(TextPaint paint) {
			paint.setFakeBoldText(false);
			super.updateMeasureState(paint);
		}
	}
}
