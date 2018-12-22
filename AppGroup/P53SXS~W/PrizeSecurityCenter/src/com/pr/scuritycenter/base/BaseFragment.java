package com.pr.scuritycenter.base;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/**
 * 
 * @author Bian所有fragment的基类
 *
 */

public abstract class BaseFragment extends Fragment {
	
	private Activity mActivity;//把Fragmnet绑定到那个Activity上，上下文就是Activity

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = getActivity();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = initView(inflater);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		initDate();
	}
	
	/**
	 * 当子类需要初始化数据，只要覆盖该方法就行
	 */
	private void initDate() {
	}

	
   /**
   *子类必须实现此方法，返回一个view对象，作为当前Fragment的布局展示 
   * @param inflater
   * @return
   */
	
	public abstract View initView(LayoutInflater inflater);
	
}
