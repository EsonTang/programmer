package com.prize.weather.framework;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.prize.weather.framework.mvp.presenter.BasePresenter;
import com.prize.weather.framework.mvp.view.IView;

/**
 * 
 * @author wangzhong
 *
 * @param <P>
 */
public abstract class BaseFragment<P extends BasePresenter<?, ?, ?>> extends Fragment implements IView {
	
	protected View mView;
	
	protected P mPresenter;
	
	protected Activity mActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = getActivity();
	}

	@Override
	public Context getContext() {
		return mActivity.getApplicationContext();
	}

	@Override
	public void handleException(Exception e) {
		
	}

	@Override
	public void handleJsonExcetpion(JSONException e) {
		Toast.makeText(mActivity, "数据解析异常！", Toast.LENGTH_LONG).show();
		closeUpdateStatus();
	}
	
	/**
	 * show dialog.
	 * @param message
	 */
	protected void showWarningMessage(String message) {
		
	}

}
