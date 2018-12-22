package com.prize.left.page.model;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

public abstract class BaseModel<T> implements IModel<T> {
	/***
	 * 回调对象
	 */
	protected IResponse<T> callback;
	/**请求网络时需要传的回调*/
	protected Callback.CommonCallback<String> httpCallback;
	/**可以用来取消请求的对象*/
	protected Cancelable cancelObj;
	
	protected Context mCtx;
	/**是否需要暂停*/
	protected boolean isPause = false;
	/**是否需要刷新*/
	protected boolean isNeedFresh = false;
	/**卡片类型, -1表示无类型*/
	protected int mCdType = -1;
	/**用于回调若数据发生了变化*/
	protected ICardNotify mICdNotify = null;
	/***
	 * 设置请求回调
	 * @param ic
	 */
	public void setIResponse(IResponse<T> ic) {
		callback = ic;
	}
	/***
	 * 实例化httpCallback, 若httpCallback不存在则实例化.
	 */
	protected abstract void newHttpCallback();
	
	public void setViewHolder(RecyclerView.ViewHolder holder) {}
	
	/***
	 * 绑定图片
	 */
	public abstract void doBindImg();
	
	/**
	 * 刷新, 注:若需要做刷新则其子类需要重写上方法
	 */
	public void doRefresh() {}
	/**暂停更新*/
	public void onPause() {}
	/**继续更新*/
	public void onResume() {}
	/**设置卡片类型*/
	public void setCardType(int type) {
		mCdType = type;
	}
	/***
	 * 设置清除卡片回调
	 * @param n
	 */
	public void setICardNotify(ICardNotify n) {
		mICdNotify = n;
	}
	
}
