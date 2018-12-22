package com.prize.app.net;

import java.util.Map;

import org.json.JSONObject;

import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.net.AppNetTask.OnReslutListener;
import com.prize.app.net.req.BaseResp;
import com.prize.app.util.JLog;

/**
 * *
 * 请求接收网络数据，当数据返回时处理，不为空时 解析 并通过NetSourceListener
 * sendMessage(NetSourceListener.WHAT_SUCCESS, data);发送出去（data属于T extends
 * AbstractNetData类型）
 *
 * @author longbaoxiu
 * @version V1.0
 */
public abstract class AppAbstractNetSource<T extends AbstractNetData, V extends Map<String, String>, Q extends BaseResp>
        implements OnReslutListener {
    private final String TAG = "AppAbstractNetSource";

    protected abstract Map<String, String> getRequest();

    /**
     * 获取响应类型
     */
    protected abstract Class<? extends BaseResp> getRespClass();

    protected abstract T parseStrResp(String resp);

    /**
     * 请求URL
     */
    public abstract String getUrl();

    protected T data = null;

    protected Q resp = null;

    @SuppressWarnings("unchecked")
    @Override
    public final void onSucess(String responseData) {
        if (null != listener) {
            if (null == responseData) {
                onFailed();
                return;
            }

            try {
                JSONObject obj = new JSONObject(responseData);
                if (obj == null) {
                    onFailed();
                    return;
                } else {
                    if (!TextUtils.isEmpty(obj.optString("data"))) {
                        data = parseStrResp(obj.optString("data"));
                        int code = obj.optInt("code");
                        String msg = obj.optString("msg");
                        if (data != null) {
                            data.code = code;
                            data.msg = msg;
                        }
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
     **/
    private NetSourceListener<T> listener = null;

    /**
     * msg.what 取值范围
     *
     * @param listener NetSourceListener
     */
    public void setListener(NetSourceListener<T> listener) {
        this.listener = listener;
    }

    /**
     * 开启线程，请求网络
     */
    public void doRequest(String requestTAG) {
        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
            if (listener != null) {
                listener.sendMessage(NetSourceListener.WHAT_NETERR, null);
            }
            return;
        }
        AppNetTask task = new AppNetTask(getUrl(),
                this, getRequest());
        task.postAppInfoByVolley(requestTAG);
    }

    public final T getData() {
        return data;
    }

}
