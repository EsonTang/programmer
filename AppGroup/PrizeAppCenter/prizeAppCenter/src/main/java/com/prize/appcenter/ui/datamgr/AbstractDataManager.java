package com.prize.appcenter.ui.datamgr;

import android.os.Handler;
import android.os.Message;

import com.prize.app.net.NetSourceListener;

import java.lang.ref.SoftReference;

/**
 * 所有的dataManager都必须继承该类
 * 
 * @author prize
 * 
 */
public abstract class AbstractDataManager extends Handler {

	private SoftReference<DataManagerCallBack> callback = null;

	public AbstractDataManager() {
	}

	/**
	 * 有对话框时必须使用此构造函数
	 * 
	 * @param callback DataManagerCallBack
	 */
	public AbstractDataManager(DataManagerCallBack callback) {
		this.callback = new SoftReference<DataManagerCallBack>(callback);
	}

	/**
	 * 处理方法 AbstractDataManager.this.sendMessageDelayed(msg, 200); }的消息 ，通过
	 * DataManagerCallBack 的onBack方法回调
	 */
	public final void handleMessage(Message msg) {
		handleMessage(msg.what, msg.arg1, msg.arg2, msg.obj);
		DataManagerCallBack callback = getCallBack();
		if (callback != null) {
			callback.onBack(msg.what, msg.arg1, msg.arg2, msg.obj);
		}
	}

	/**
	 * 处理返回结果<br/>
	 * 不要做耗时操作
	 * 
	 * @param what int
	 * @param arg1 int
	 * @param arg2 int
	 * @param obj  Object
	 */
	protected abstract void handleMessage(int what, int arg1, int arg2,
			Object obj);

	/**
	 * 获取Callback 实例
	 * 
	 * @return DataManagerCallBack
	 */
	private DataManagerCallBack getCallBack() {
		return callback == null ? null : callback.get();
	}

	/**
	 * 数据管理用的Listener<br/>
	 * 实现了NetSourceListener 接口<br/>
	 * 请实例化此Listener,并设置给AbstractNetSource,用于数据返回的监听。<br/>
	 * 可在此处理传输数据
	 * 
	 * 
	 * @param <T>
	 */
	protected class DataManagerListener<T> implements NetSourceListener<T> {

		@Override
		public final void sendMessage(int what, T data) {
			Message msg = null;
			switch (what) {
			case WHAT_SUCCESS:
				msg = onSuccess(what, data);
				break;
			case WHAT_NETERR:
				msg = onFailed(what);
				break;
			}
			if (msg != null) {
				// 防止请求响应太快 ,对话框无法消失
				AbstractDataManager.this.sendMessageDelayed(msg, 200);
			}
		}

		/**
		 * 响应的具体处理过程,需子类实现<br/>
		 * 传输数据的message
		 * 
		 * @param what int
		 * @param data T
		 */
		protected Message  onSuccess(int what, T data) {
			return Message.obtain(AbstractDataManager.this, what, data);
		}

		/**
		 * 获取数据失败, 若需要可 Override
		 */
		protected Message onFailed(int what) {
			return Message.obtain(AbstractDataManager.this, what);
		}
	}

}
