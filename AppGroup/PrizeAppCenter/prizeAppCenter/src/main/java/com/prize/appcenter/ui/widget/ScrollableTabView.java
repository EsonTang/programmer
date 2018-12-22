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

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.prize.appcenter.ui.adapter.TabAdapter;

public class ScrollableTabView extends HorizontalScrollView implements
		OnPageChangeListener {
	private OnPageSelectedListener mOnPageSelectedListener = null;
	private ViewPager mPager = null;

	private TabAdapter mAdapter = null;

	private final LinearLayout mContainer;

	private final ArrayList<View> mTabs = new ArrayList<View>();

	public ScrollableTabView(Context context) {
		this(context, null);
	}

	public ScrollableTabView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScrollableTabView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);

		this.setHorizontalScrollBarEnabled(false);
		this.setHorizontalFadingEdgeEnabled(false);

		mContainer = new LinearLayout(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.BOTTOM;
		mContainer.setLayoutParams(params);
		// mContainer.setPadding(60, 0, 100, 0);
		mContainer.setOrientation(LinearLayout.HORIZONTAL);

		this.addView(mContainer);

	}

	public void setAdapter(TabAdapter adapter) {
		this.mAdapter = adapter;

		if (mPager != null && mAdapter != null)
			initTabs();
	}

	public void setViewPager(ViewPager pager) {
		this.mPager = pager;
		mPager.setOnPageChangeListener(this);

		if (mPager != null && mAdapter != null)
			initTabs();
	}

	private void initTabs() {

		mContainer.removeAllViews();
		mTabs.clear();

		if (mAdapter == null)
			return;
		// View view = LayoutInflater.from(getContext()).inflate(R.layout.view,
		// null);
		// mContainer.addView(view);
		for (int i = 0; i < mPager.getAdapter().getCount(); i++) {

			final int index = i;

			View tab = mAdapter.getView(i);
			mContainer.addView(tab);

			tab.setFocusable(true);

			mTabs.add(tab);

			tab.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mPager.getCurrentItem() == index) {
						selectTab(index);
					} else {
						mPager.setCurrentItem(index, true);
					}
				}
			});

		}
		// View view2 = LayoutInflater.from(getContext()).inflate(R.layout.view,
		// null);
		// mContainer.addView(view2);
		selectTab(mPager.getCurrentItem());
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		if (position != 0 && mOnPageSelectedListener != null) {
			mOnPageSelectedListener.onPageSelected(position);

		}
		selectTab(position);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		if (changed && mPager != null)
			selectTab(mPager.getCurrentItem());
	}

	private void selectTab(int position) {

		for (int i = 0, pos = 0; i < mContainer.getChildCount(); i++, pos++) {
			View tab = mContainer.getChildAt(i);
			tab.setSelected(pos == position);
		}

		View selectedTab = mContainer.getChildAt(position);

		final int w = selectedTab.getMeasuredWidth();
		final int l = selectedTab.getLeft();

		final int x = l - this.getWidth() / 2 + w / 2;
		// smoothScrollTo(x, this.getScrollY());

	}

	public interface OnPageSelectedListener {
		public void onPageSelected(int position);
	}

	public void setPagerSelectLister(
			OnPageSelectedListener mOnPageSelectedListener) {
		this.mOnPageSelectedListener = mOnPageSelectedListener;
	}
}
