package com.prize.weather.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 ** 
 * 类描述：
 * 
 * @author hekeyi
 * @version 版本
 */
public class BlurScrollView extends ScrollView {
	
	private OnScrollListener onScrollListener;
	
	public BlurScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
	}
	
	/**
	 * 设置滚动接口
	 * @param onScrollListener
	 */
	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(onScrollListener != null){
			onScrollListener.onScroll(t);
		}
	}
	
	/**
	 * 滚动的回调接口
	 */
	public interface OnScrollListener{
		/**
		 * 回调方法， 返回MyScrollView滑动的Y方向距离
		 * @param scrollY
		 */
		public void onScroll(int scrollY);
	}

}
