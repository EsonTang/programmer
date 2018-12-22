package com.prize.appcenter.fragment.base;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.prize.appcenter.R;
import com.prize.appcenter.ui.widget.GifView;

/**
 * 类描述：
 * 
 * @author huanglingjun
 * @version 版本
 */
public abstract class BaseLoadActivity extends FragmentActivity {

	private View waitView = null;
	private View contentView = null;
	private View reloadView = null;

	protected void initAllView(View waitView, View contentView, View reloadView) {
		this.waitView = waitView;
		this.contentView = contentView;
		this.reloadView = reloadView;
	}

	/**
	 * 显示等待框
	 */
	public void showWaiting() {
		if (waitView == null)
			return;
		GifView gifWaitingView = (GifView) waitView
				.findViewById(R.id.gif_waiting);
		gifWaitingView.setPaused(false);
		waitView.setVisibility(View.VISIBLE);
		contentView.setVisibility(View.GONE);
		this.reloadView.setVisibility(View.GONE);
	}

//	/**
//	 * 隐藏等待框
//	 */
//	public void hideWaiting() {
//		if (waitView == null)
//			return;
//		waitView.setVisibility(View.GONE);
//		contentView.setVisibility(View.VISIBLE);
//	}

	/**
	 * 隐藏等待框
	 */
	public void hideWaiting() {
		if (waitView == null)
			return;
		waitView.setVisibility(View.GONE);
		GifView gifWaitingView = (GifView) waitView
				.findViewById(R.id.gif_waiting);
		gifWaitingView.setPaused(true);
		contentView.setVisibility(View.VISIBLE);

	}
	
	/**
	 * 重新加载数据
	 */
	public interface ReloadFunction {
		void reload();
	}

	/**
	 * 加载失败
	 */
	public void loadingFailed(final ReloadFunction reload) {
		if (null == reload) {
			return;
		}
		hideWaiting();
		reloadView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				reloadView.setVisibility(View.GONE);
				contentView.setVisibility(View.VISIBLE);
				showWaiting();
				reload.reload();
			}
		});

		reloadView.setVisibility(View.VISIBLE);
		contentView.setVisibility(View.GONE);
	}

}
