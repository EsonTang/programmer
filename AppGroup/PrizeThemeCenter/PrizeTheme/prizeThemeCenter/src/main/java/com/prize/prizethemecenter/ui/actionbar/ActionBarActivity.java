package com.prize.prizethemecenter.ui.actionbar;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.RootActivity;
import com.prize.prizethemecenter.ui.widget.GifView;
import com.prize.prizethemecenter.ui.widget.SlideLinearLayout;

public abstract class ActionBarActivity extends RootActivity  implements ServiceConnection  {
	private SlideLinearLayout rootSliedLayout;

	protected View waitView = null;
	private View contentView = null;
	private View reloadView = null;
	private boolean isNeedAddWaitingView;

	@Override
	public void setContentView(int layoutResID) {
		try {
			super.setContentView(createRootView(LayoutInflater.from(this)
					.inflate(layoutResID, null)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		initActionBar();
	}

	@Override
	public void setContentView(View view,
			android.view.ViewGroup.LayoutParams params) {
		super.setContentView(createRootView(view), params);
		initActionBar();
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(createRootView(view));
		initActionBar();
	}

	/**
	 * 创建根View
	 * 
	 * @param view
	 * @return
	 */
	private SlideLinearLayout createRootView(View view) {
		contentView = view;
		rootSliedLayout = new SlideLinearLayout(this);
		rootSliedLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutInflater.from(this).inflate(R.layout.action_bar, rootSliedLayout);
		if (isNeedAddWaitingView) {
			waitView = addWaitingView(rootSliedLayout);
		}
		rootSliedLayout.addView(view, LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		return rootSliedLayout;
	}

	/**
	 * 是否需要添加等待框 setContentView 之前调用
	 * @param isNeedAddWaitingView
	 */
	public void setNeedAddWaitingView(boolean isNeedAddWaitingView) {
		this.isNeedAddWaitingView = isNeedAddWaitingView;
	}

	/**
	 * 添加等待框
	 * @param root
	 */
	private View addWaitingView(ViewGroup root) {
		View waitView = LayoutInflater.from(this).inflate(
				R.layout.waiting_view, null);
		root.addView(waitView, LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		return waitView;
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
	}

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
	 * @author prize
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

		if (null == reloadView) {
			reloadView = LayoutInflater.from(this).inflate(
					R.layout.reload_layout, null);
			if(isFromDetail){
				reloadView.setPadding(0,440,0,0);
			}else{
				reloadView.setPadding(0,300,0,0);
			}
			LinearLayout reloadLinearLayout = (LinearLayout) reloadView
					.findViewById(R.id.reload_Llyt);
			reloadLinearLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					reloadView.setVisibility(View.GONE);
					contentView.setVisibility(View.VISIBLE);
					showWaiting();
					reload.reload();
				}
			});
			rootSliedLayout.addView(reloadView,
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
		} else {
			if(isFromDetail){
				reloadView.setPadding(0,440,0,0);
			}else{
				reloadView.setPadding(0,300,0,0);
			}
			reloadView.setVisibility(View.VISIBLE);
		}
		contentView.setVisibility(View.GONE);
	}

	/**
	 * 初始化Action Bar
	 */
	protected abstract void initActionBar();

	protected void enableSlideLayout(boolean enabled) {
		if (rootSliedLayout != null) {
			rootSliedLayout.enableSlide(enabled);
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {

	}

	@Override
	public void onServiceDisconnected(ComponentName name) {

	}

	private boolean isFromDetail;
	public void setIsDetail(boolean isDetail){
        this.isFromDetail = isDetail;
	}
}
