package com.prize.appcenter.ui.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

/***
 * 自定义WebView
 * 
 * @author fanjunchen
 *
 */
public class CustomWebView extends WebView {

	/** 按下的y轴坐标, 移动的y轴坐标 */
	private int downY = 0, moveY = 0;
	/** 可以操作的视图控件 */
	private View optView;
	/** 是否加载完成 */
	private boolean isLoadFinish = false;

	public CustomWebView(Context context) {
		super(context);
	}

	public CustomWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	// modify by huanglingjun 2015-12-2
	/*
	 * public CustomWebView(Context context, AttributeSet attrs, int
	 * defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr,
	 * defStyleRes); init(); }
	 * 
	 * public CustomWebView(Context context, AttributeSet attrs, int
	 * defStyleAttr, Map<String, Object> javaScriptInterfaces, boolean
	 * privateBrowsing) { super(context, attrs, defStyleAttr,
	 * javaScriptInterfaces, privateBrowsing); init(); }
	 * 
	 * public CustomWebView(Context context, AttributeSet attrs, int
	 * defStyleAttr, int defStyleRes, Map<String, Object> javaScriptInterfaces,
	 * boolean privateBrowsing) { super(context, attrs, defStyleAttr,
	 * defStyleRes, javaScriptInterfaces, privateBrowsing); init(); }
	 */

	private void init() {

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int act = event.getAction() & MotionEvent.ACTION_MASK;
		switch (act) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			moveY = (int) event.getY();

			if (Math.abs(moveY - downY) > 10) {
				changeState();
			}
			break;
		case MotionEvent.ACTION_UP:
			downY = moveY = 0;
			break;
		case MotionEvent.ACTION_CANCEL:
			downY = moveY = 0;
			break;
		}
		return super.onTouchEvent(event);
	}

	/***
	 * 改变状态
	 */
	private void changeState() {
		if (null == optView || !isLoadFinish)
			return;
		boolean isVisible = optView.getVisibility() == View.VISIBLE;
		if (moveY > downY && !isVisible) { // 往下
			optView.setVisibility(View.VISIBLE);
		} else if (moveY < downY && isVisible) { // 往上
			optView.setVisibility(View.GONE);
		}
		/*
		 * int sY = getScrollY(); if(getContentHeight() * getScale() ==
		 * (getHeight() + sY)){ // 到底部 }
		 */
	}

	public void setTargetView(View v) {
		optView = v;
	}

	/***
	 * 设置加载是否完成
	 *
	 * @param isFinish boolean
	 */
	public void setLoadState(boolean isFinish) {
		isLoadFinish = isFinish;
	}
}
