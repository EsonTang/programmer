/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：huanglingjun
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * 类描述：详情页面内部scrollview
 * 
 * @author huanglingjun
 * @version 版本
 */
public class InnerScrollView extends ScrollView {
	public ScrollView parentScrollView;
	public ScrollChangeListener scrollChangeListener;

	public InnerScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private int lastScrollDelta = 0;

	public void resume() {
		overScrollBy(0, -lastScrollDelta, 0, getScrollY(), 0, getScrollRange(),
				0, 0, true);
		lastScrollDelta = 0;
	}

	int mTop = 10;

	/**
	 * 将targetView滚到最顶端
	 */
	public void scrollTo(View targetView) {
		int oldScrollY = getScrollY();
		int top = targetView.getTop() - mTop;
		int delatY = top - oldScrollY;
		lastScrollDelta = delatY;
		overScrollBy(0, delatY, 0, getScrollY(), 0, getScrollRange(), 0, 0,
				true);
	}

	private int getScrollRange() {
		int scrollRange = 0;
		if (getChildCount() > 0) {
			View child = getChildAt(0);
			scrollRange = Math.max(0, child.getHeight() - (getHeight()));
		}
		return scrollRange;
	}

	int currentY;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (parentScrollView == null) {
			return super.onInterceptTouchEvent(ev);
		} else {
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				// 将父scrollview的滚动事件拦截
				View child = getChildAt(0);
				int height = child.getMeasuredHeight();
				height = height - getMeasuredHeight();
				if (height > 0) {
					setParentScrollAble(true);
				}
				int y = (int) ev.getY();
				currentY = y; // 防止onTouchEvent里面捕捉不到点击事件currentY无法初始化
				// setParentScrollAble(true);
				return super.onInterceptTouchEvent(ev);
			} else if (ev.getAction() == MotionEvent.ACTION_UP) {
				// 把滚动事件恢复给父Scrollview
				// setParentScrollAble(false);
			} else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			}
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int scrollY = getScrollY();
		if (parentScrollView != null) {
			int y = (int) ev.getY();
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				currentY = y;
			}
			if (ev.getAction() == MotionEvent.ACTION_MOVE) {
				// 判断父scrollview是否完全将头部滑出去了
				if (parentScrollView.getScrollY()
						+ parentScrollView.getHeight() != parentScrollView
						.getChildAt(0).getHeight() && scrollY <= 0) {
					setParentScrollAble(false);
					return false;
				} else {
					setParentScrollAble(true);
					if (currentY < y) {
						setParentScrollAble(true);
						// 手指向下滑动
						if (scrollY <= 0) {
							// 如果向下滑动到头，就把滚动交给父Scrollview
							setParentScrollAble(false);
							return false;
						} else {
							setParentScrollAble(true);
						}
					} else if (currentY >= y) {
						setParentScrollAble(true);
					}
					currentY = y;
				}
			}
		}
		return super.onTouchEvent(ev);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {

		// TODO Auto-generated method stub
		super.onScrollChanged(l, t, oldl, oldt);
		if (scrollChangeListener != null
				&& getHeight() + getScrollY() >= computeVerticalScrollRange()) {
			scrollChangeListener.onScroll(this);
		}
	}

	public void setOnScrollChangedListener(
			ScrollChangeListener scrollChangeListener) {
		this.scrollChangeListener = scrollChangeListener;
	}

	public static interface ScrollChangeListener {
		void onScroll(InnerScrollView v);
	}

	/**
	 * 是否把滚动事件交给父scrollview
	 * 
	 * @param flag
	 */
	private void setParentScrollAble(boolean flag) {
		parentScrollView.requestDisallowInterceptTouchEvent(flag);
	}

	public void setParentScrollView(ScrollView scrollView) {
		this.parentScrollView = scrollView;
	}
}
