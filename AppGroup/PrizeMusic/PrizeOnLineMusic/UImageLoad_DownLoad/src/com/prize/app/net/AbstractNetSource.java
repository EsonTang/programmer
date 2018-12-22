package com.prize.app.net;

import org.json.JSONObject;

import android.text.TextUtils;

import com.prize.app.beans.ClientInfo;
import com.prize.app.net.NetSourceTask.OnReslutListener;
import com.prize.app.net.req.BaseReq;
import com.prize.app.net.req.BaseResp;
import com.prize.app.threads.DataSourceThreadPool;
import com.prize.app.util.JLog;

/**
 **
 * 请求接收网络数据，当数据返回时处理，不为空时 解析 并通过NetSourceListener
 * sendMessage(NetSourceListener.WHAT_SUCCESS, data);发送出去（data属于T extends
 * AbstractNetData类型）
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public abstract class AbstractNetSource<T extends AbstractNetData, V extends BaseReq, Q extends BaseResp>
		implements OnReslutListener {
	private final String TAG = "AbstractNetSource";
	private static DataSourceThreadPool mMessageManager;

	/** 获取请求参数 */
	protected abstract V getRequest();

	/**
	 * 获取响应类型
	 * 
	 * @return
	 */
	protected abstract Class<? extends BaseResp> getRespClass();

	/** 处理响应数据 */
	protected abstract T parseResp(Q resp);

	protected abstract T parseStrResp(String resp);

	/** 请求URL */
	public abstract String getUrl();

	protected T data = null;

	protected Q resp = null;

	@SuppressWarnings("unchecked")
	@Override
	public final void onSucess(byte[] bytes) {
		String responseData = null;
		if (null != listener) {
			// BeanTLVDecoder beanDecoder = new BeanTLVDecoder();
			// TLVDecodeContext context = new BeanTLVDecoder()
			// .getDecodeContextFactory().createDecodeContext(
			// getRespClass(), null);
			// Q response = null;
			try {
				responseData = new String(bytes, "UTF-8");
				// response = (Q) beanDecoder.decode(bytes.length, bytes,
				// context);
				// JLog.i(TAG, "----" + response.toString());
			} catch (Exception e) {
				// e.printStackTrace();
				onFailed();
			}
			if (null == responseData) {
				onFailed();
				return;
			}

			try {
				JSONObject obj = new JSONObject(responseData);
				// data = parseResp(response);
				if (obj == null) {
					onFailed();
					return;
				} else {
					if (!TextUtils.isEmpty(obj.optString("data"))) {
						data = parseStrResp(obj.optString("data"));
						int code = obj.optInt("code");
						String msg = obj.optString("msg");
						if (code == -1) {
							data = null;
							onFailed();
							return;
						}
					} else {
						data = null;
						onFailed();
						return;
					}
					// data.reponseCode = response.getResponseCode();
					// data.responseMsg = response.getResponseMsg();
				}
			} catch (Exception e) {
				e.printStackTrace();
				onFailed();
				return;
			}
			if (null != listener) {
				listener.sendMessage(NetSourceListener.WHAT_SUCCESS, data);
			}
		}
	}

	@Override
	public final void onFailed() {
		JLog.i(TAG, "onFailed()请求返回数据错误");
		if (null != listener) {
			listener.sendMessage(NetSourceListener.WHAT_NETERR, null);
		}
	}

	/**
	 * eg :{@link #NetSourceListener} 的实现类{@link #DataManagerListener}
	 * 中复写的方法sendMessage（）处理
	 * **/
	private NetSourceListener<T> listener = null;

	/**
	 * msg.what 取值范围
	 * 
	 * @param handler
	 */
	public void setListener(NetSourceListener<T> listener) {
		this.listener = listener;
	}

	/**
	 * 开启线程，请求网络
	 * 
	 * @return void
	 */
	public final void doRequest() {
		if (ClientInfo.networkType == ClientInfo.NONET) {
			if (listener != null) {
				listener.sendMessage(NetSourceListener.WHAT_NETERR, null);
			}
			return;
		}
		mMessageManager = DataSourceThreadPool.getInstance();
		V req = getRequest();
		// req.setUserAgent(getUA());
		NetSourceTask<V, Q> connect = new NetSourceTask<V, Q>(getUrl(), this,
				req);
		mMessageManager.execute(connect);
	}

	public final T getData() {
		return data;
	}

	// private static String terminalId;
	// private static String activatedTime;
	// private static Long activatedTimeL;

	// private final UserAgent getUA() {
	// if (ua == null) {
	// ua = new UserAgent();
	// ClientInfo clientInfo = ClientInfo.getInstance();
	// ua.setAndroidSystemVer(clientInfo.androidVer);
	// ua.setApkVer(clientInfo.apkVerName);
	// ua.setApkverInt(clientInfo.apkVerCode);
	// ua.setCpu(clientInfo.cpu);
	// ua.setHsman(clientInfo.hsman);
	// ua.setHstype(clientInfo.hstype);
	// ua.setImei(clientInfo.imei);
	// ua.setImsi(clientInfo.imsi);
	// ua.setNetworkType(ClientInfo.networkType);
	// ua.setPackegeName(clientInfo.packageName);
	// ua.setProvider(clientInfo.provider);
	// ua.setChannelCode(clientInfo.channelCode);
	// ua.setRamSize(clientInfo.ramSize);
	// ua.setRomSize(clientInfo.romSize);
	// ua.setScreenSize(clientInfo.screenSize);
	// ua.setDpi(clientInfo.dpi);
	// ua.setMac(clientInfo.mac);
	// if (null == terminalId) {
	// terminalId = FileUtils.getFixedInfo(FileUtils.TERMINAL_ID);
	// }
	//
	// if (null == activatedTime) {
	// activatedTime = FileUtils
	// .getFixedInfo(FileUtils.ACTIVATED_TIME);
	// try {
	// activatedTimeL = Long.valueOf(activatedTime);
	// } catch (Exception e) {
	// activatedTimeL = null;
	// }
	//
	// if (null == activatedTimeL) {
	// activatedTimeL = System.currentTimeMillis();
	// FileUtils.saveFixedInfo(FileUtils.ACTIVATED_TIME,
	// String.valueOf(activatedTimeL));
	// }
	// }
	// ua.setTerminalId(terminalId);
	// ua.setFirstVisitTime(activatedTimeL);
	// // 如果存在配置文件，读取配置文件信息
	// setVirtualConfig();
	// } else {
	// ua.setNetworkType(ClientInfo.networkType); // 网络类型经常发生变化,每次要重新设置
	// }
	//
	// return ua;
	// }

	public static void onStop() {
		if (mMessageManager != null) {
			mMessageManager.stop();
			mMessageManager = null;
		}
	}
}
