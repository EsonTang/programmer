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

package com.prize.appcenter.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Space;

import com.prize.appcenter.R;
import com.prize.appcenter.ui.widget.NotifyingScrollView;

public abstract class AppDetailHeadFragment extends Fragment {

	public static final int HEADER_BACKGROUND_SCROLL_NORMAL = 0;
	public static final int HEADER_BACKGROUND_SCROLL_PARALLAX = 1;
	public static final int HEADER_BACKGROUND_SCROLL_STATIC = 2;

	private FrameLayout mFrameLayout;
	// private LinearLayout mFrameLayout;
	private View mContentOverlay;

	// header
	private View mHeader;
	private View mHeaderHeader;
	private View mHeaderBackground;
	private int mHeaderHeight;
	private int mHeaderScroll;

	private int mHeaderBackgroundScrollMode = HEADER_BACKGROUND_SCROLL_NORMAL;

	private Space mFakeHeader;

	// listeners
	private OnHeaderScrollChangedListener mOnHeaderScrollChangedListener;

	public interface OnHeaderScrollChangedListener {
		public void onHeaderScrollChanged(float progress, int height, int scroll);
	}

	public void setOnHeaderScrollChangedListener(
			OnHeaderScrollChangedListener listener) {
		mOnHeaderScrollChangedListener = listener;
	}

	public void setHeaderBackgroundScrollMode(int scrollMode) {
		mHeaderBackgroundScrollMode = scrollMode;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final Activity activity = getActivity();
		assert activity != null;
		mFrameLayout = new FrameLayout(activity);

		mHeader = onCreateHeaderView(inflater, mFrameLayout);
		mHeaderBackground = mHeader.findViewById(R.id.myhead_id);
		assert mHeader.getLayoutParams() != null;
		mHeaderHeight = mHeader.getLayoutParams().height;

		mFakeHeader = new Space(activity);
		mFakeHeader
				.setLayoutParams(new ListView.LayoutParams(0, mHeaderHeight));

		View content = onCreateContentView(inflater, mFrameLayout);
		if (content instanceof NotifyingScrollView) {
			NotifyingScrollView scrollView = (NotifyingScrollView) content;
			scrollView
					.setOnScrollChangedListener(new NotifyingScrollView.OnScrollChangedListener() {
						@Override
						public void onScrollChanged(ScrollView who, int l,
								int t, int oldl, int oldt) {
							scrollHeaderTo(-t);
						}
					});
			content = scrollView;
		}
		mFrameLayout.addView(content);
		mFrameLayout.addView(mHeader);

		// Content overlay view always shows at the top of content.
		if ((mContentOverlay = onCreateContentOverlayView(inflater,
				mFrameLayout)) != null) {
			mFrameLayout.addView(mContentOverlay, new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT));
		}

		// Post initial scroll
		mFrameLayout.post(new Runnable() {
			@Override
			public void run() {
				scrollHeaderTo(0, true);
			}
		});

		return mFrameLayout;
	}

	private void scrollHeaderTo(int scrollTo) {
		scrollHeaderTo(scrollTo, false);
	}

	private void scrollHeaderTo(int scrollTo, boolean forceChange) {
		scrollTo = Math.min(Math.max(scrollTo, -mHeaderHeight), 0);
		if (mHeaderScroll == (mHeaderScroll = scrollTo) & !forceChange)
			return;

		setViewTranslationY(mHeader, scrollTo);
		setViewTranslationY(mHeaderHeader, -scrollTo);

		switch (mHeaderBackgroundScrollMode) {
		case HEADER_BACKGROUND_SCROLL_NORMAL:
			setViewTranslationY(mHeaderBackground, 0);
			break;
		case HEADER_BACKGROUND_SCROLL_PARALLAX:
			setViewTranslationY(mHeaderBackground, -scrollTo / 1.6f);
			break;
		case HEADER_BACKGROUND_SCROLL_STATIC:
			setViewTranslationY(mHeaderBackground, -scrollTo);
			break;
		}
		if (mContentOverlay != null) {
			final ViewGroup.LayoutParams lp = mContentOverlay.getLayoutParams();
			final int delta = mHeaderHeight + scrollTo;
			lp.height = mFrameLayout.getHeight() - delta;
			mContentOverlay.setLayoutParams(lp);
			mContentOverlay.setTranslationY(delta);
		}
		notifyOnHeaderScrollChangeListener((float) -scrollTo / mHeaderHeight,
				mHeaderHeight, -scrollTo);
	}

	private void setViewTranslationY(View view, float translationY) {
		if (view != null)
			view.setTranslationY(translationY);
	}

	private void notifyOnHeaderScrollChangeListener(float progress, int height,
			int scroll) {
		if (mOnHeaderScrollChangedListener != null) {
			mOnHeaderScrollChangedListener.onHeaderScrollChanged(progress,
					height, scroll);
		}
	}

	public abstract View onCreateHeaderView(LayoutInflater inflater,
			ViewGroup container);

	public abstract View onCreateContentView(LayoutInflater inflater,
			ViewGroup container);

	public abstract View onCreateContentOverlayView(LayoutInflater inflater,
			ViewGroup container);

	public View getHeaderView() {
		return mHeader;
	}

	public View getHeaderBackgroundView() {
		return mHeaderBackground;
	}

	public int getHeaderBackgroundScrollMode() {
		return mHeaderBackgroundScrollMode;
	}
}
