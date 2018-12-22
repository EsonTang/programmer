package com.prize.music.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.prize.music.IfragToActivityLister;

/**
 * @ClassName: BaseFragment
 * @Description: Fragment基类
 * @date 2014-3-18 下午2:38:21
 */
public abstract class BaseFragment extends Fragment implements OnTouchListener,
		IfragToActivityLister {
	protected static final String TAG = BaseFragment.class.getSimpleName();
	private int index;

	/**
	 * 解决事件穿透问题
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		view.setOnTouchListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/** 绑定界面UI **/
	protected abstract void findViewById();

	/** 界面UI事件监听 **/
	protected abstract void setListener();

	/** 界面数据初始化 **/
	protected abstract void init();

	/**
	 * 解决fragment事件穿透问题
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	@Override
	public void countNum(int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processAction(String action) {
		// TODO Auto-generated method stub

	}
}
