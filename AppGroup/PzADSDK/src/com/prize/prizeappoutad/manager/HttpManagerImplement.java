/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
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

package com.prize.prizeappoutad.manager;

import java.util.HashMap;

import org.xutils.HttpManager;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.HttpTask;
import org.xutils.http.RequestParams;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.prize.prizeappoutad.bean.ClientInfo;
import com.prize.prizeappoutad.utils.JLog;
import com.prize.prizeappoutad.utils.XXTEAUtil;

/**
 * 
 ** 
 * Xutils的 HttpManager实现类,为了实现自己添加头信息
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public final class HttpManagerImplement implements HttpManager {
	static Context mContext;
	private ClientInfo mClientInfo = ClientInfo.getInstance(mContext);
	private static final Object lock = new Object();
	private static HttpManagerImplement instance;
	private static final HashMap<String, HttpTask<?>> DOWNLOAD_TASK = new HashMap<String, HttpTask<?>>(
			1);

	private HttpManagerImplement() {
	}

	public static void registerInstance(Context context) {
		mContext = context;
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new HttpManagerImplement();
				}
			}
		}
		x.Ext.setHttpManager(instance);
	}

	@Override
	public <T> Callback.Cancelable get(RequestParams entity,
			Callback.CommonCallback<T> callback) {
		final String saveFilePath = entity.getSaveFilePath();
		if (!TextUtils.isEmpty(saveFilePath)) {
			HttpTask<?> task = DOWNLOAD_TASK.get(saveFilePath);
			if (task != null) {
				task.cancel();
				task = null;
			}
		}
		entity.setMethod(HttpMethod.GET);
		// mClientInfo.setUserId(CommonUtils.queryUserId());
		// mClientInfo.setClientStartTime(System.currentTimeMillis());
		// // mClientInfo.tid = PreferencesUtils.getKEY_TID();
		// mClientInfo.setNetStatus(ClientInfo.networkType);
		//

		String headParams = new Gson().toJson(mClientInfo);
		JLog.i("huang-clientInfo:", headParams);
		headParams = XXTEAUtil.getParamsEncypt(headParams);
		if (!TextUtils.isEmpty(headParams)) {
			entity.addHeader("params", headParams);
		}
		Callback.Cancelable cancelable = null;
		if (callback instanceof Callback.Cancelable) {
			cancelable = (Callback.Cancelable) callback;
		}
		HttpTask<T> task = null;
		if (!TextUtils.isEmpty(saveFilePath)) {
			task = new HttpTask<T>(entity, cancelable, callback) {
				@Override
				protected void onFinished() {
					super.onFinished();
					synchronized (DOWNLOAD_TASK) {
						HttpTask<?> task = DOWNLOAD_TASK.get(saveFilePath);
						if (task == this) {
							DOWNLOAD_TASK.remove(saveFilePath);
						}
					}
				}
			};
			synchronized (DOWNLOAD_TASK) {
				DOWNLOAD_TASK.put(saveFilePath, task);
			}
		} else {
			task = new HttpTask<T>(entity, cancelable, callback);
		}
		return x.task().start(task);
	}

	@Override
	public <T> Callback.Cancelable post(RequestParams entity,
			Callback.CommonCallback<T> callback) {
		return request(HttpMethod.POST, entity, callback);
	}

	@Override
	public <T> Callback.Cancelable request(HttpMethod method,
			RequestParams entity, Callback.CommonCallback<T> callback) {
		// mClientInfo.setUserId(CommonUtils.queryUserId());
		mClientInfo.setClientStartTime(System.currentTimeMillis());
		mClientInfo.setNetStatus(ClientInfo.networkType);

		// mClientInfo.tid = PreferencesUtils.getKEY_TID();
		String headParams = new Gson().toJson(mClientInfo);

		headParams = XXTEAUtil.getParamsEncypt(headParams);
		if (!TextUtils.isEmpty(headParams)) {
			entity.addHeader("params", headParams);
		}
		if (method == HttpMethod.GET) {
			return get(entity, callback);
		} else {
			entity.setMethod(method);
			Callback.Cancelable cancelable = null;
			if (callback instanceof Callback.Cancelable) {
				cancelable = (Callback.Cancelable) callback;
			}
			HttpTask<T> task = new HttpTask<T>(entity, cancelable, callback);
			return x.task().start(task);
		}
	}

	@Override
	public <T> T getSync(RequestParams entity, Class<T> resultType)
			throws Throwable {
		return requestSync(HttpMethod.GET, entity, resultType);
	}

	@Override
	public <T> T postSync(RequestParams entity, Class<T> resultType)
			throws Throwable {
		return requestSync(HttpMethod.POST, entity, resultType);
	}

	@Override
	public <T> T requestSync(HttpMethod method, RequestParams entity,
			Class<T> resultType) throws Throwable {
		entity.setMethod(method);
		entity.setMethod(HttpMethod.GET);
		// mClientInfo.setUserId(CommonUtils.queryUserId());
		mClientInfo.setClientStartTime(System.currentTimeMillis());
		mClientInfo.setNetStatus(ClientInfo.networkType);

		String headParams = new Gson().toJson(mClientInfo);

		headParams = XXTEAUtil.getParamsEncypt(headParams);
		if (!TextUtils.isEmpty(headParams)) {
			entity.addHeader("params", headParams);
		}
		SyncCallback<T> callback = new SyncCallback<T>(resultType);
		HttpTask<T> task = new HttpTask<T>(entity, null, callback);
		return x.task().startSync(task);
	}

	private class SyncCallback<T> implements Callback.TypedCallback<T> {

		private final Class<T> resultType;

		public SyncCallback(Class<T> resultType) {
			this.resultType = resultType;
		}

		@Override
		public Class<?> getResultType() {
			return resultType;
		}

		@Override
		public void onSuccess(T result) {

		}

		@Override
		public void onError(Throwable ex, boolean isOnCallback) {

		}

		@Override
		public void onCancelled(CancelledException cex) {

		}

		@Override
		public void onFinished() {

		}
	}
}
