/*
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

package com.prize.custmerxutils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.SharedPreferencesHelper;
import com.prize.app.util.Verification;
import com.prize.app.util.safe.XXTEAUtil;

import org.xutils.HttpManager;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.HttpTask;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.HashMap;

/**
 * *
 * Xutils的 HttpManager实现类,为了实现自己添加头信息
 *
 * @author longbaoxiu
 * @version V1.0
 */
public final class HttpManagerImplement implements HttpManager {
    private ClientInfo mClientInfo = ClientInfo.getInstance();
    private static final Object lock = new Object();
    private static HttpManagerImplement instance;
    private static final HashMap<String, HttpTask<?>> DOWNLOAD_TASK = new HashMap<String, HttpTask<?>>(
            1);

    private HttpManagerImplement() {
    }

    public static void registerInstance() {
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
        if (TextUtils.isEmpty(mClientInfo.userId)) {
            mClientInfo.setUserId(CommonUtils.queryUserId());
        }
        if (TextUtils.isEmpty(mClientInfo.tid)) {
            mClientInfo.tid = PreferencesUtils.getKEY_TID();
        }
        mClientInfo.setClientStartTime(System.currentTimeMillis());
        mClientInfo.setNetStatus(ClientInfo.networkType);
        if (BaseApplication.isNewSign) {
            mClientInfo.setApkSign("new");
        }
        //
        String headParams = new Gson().toJson(mClientInfo);
        if (JLog.isDebug) {
            JLog.i("longbaoxiu", "HttpManagerImplement-get-headParams=" + headParams);
        }
        headParams = XXTEAUtil.getParamsEncypt(headParams);
        if (!TextUtils.isEmpty(headParams)) {
            entity.addHeader("params", headParams);
        }

        if (entity != null && entity.getBodyParams() != null) {
            String sign = Verification.getInstance().getSign(
                    entity.getBodyParams());
            entity.addBodyParameter("sign", sign);
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
        if (entity != null && entity.getBodyParams() != null) {
            String sign = Verification.getInstance().getSign(
                    entity.getBodyParams());
            entity.addBodyParameter("sign", sign);
        }

        return request(HttpMethod.POST, entity, callback);
    }

    @Override
    public <T> Callback.Cancelable request(HttpMethod method,
                                           RequestParams entity, Callback.CommonCallback<T> callback) {
        if (TextUtils.isEmpty(mClientInfo.userId)) {
            mClientInfo.setUserId(CommonUtils.queryUserId());
        }
        if (TextUtils.isEmpty(mClientInfo.tid)) {
            mClientInfo.tid = PreferencesUtils.getKEY_TID();
        }

        mClientInfo.setClientStartTime(System.currentTimeMillis());
        mClientInfo.setNetStatus(ClientInfo.networkType);
        if (BaseApplication.isNewSign) {
            mClientInfo.setApkSign("new");
        }
        String headParams = new Gson().toJson(mClientInfo);
        if (JLog.isDebug) {
            JLog.i("longbaoxiu", "HttpManagerImplement-request-headParams=" + headParams);
        }
        headParams = XXTEAUtil.getParamsEncypt(headParams);
        if (!TextUtils.isEmpty(headParams)) {
            entity.addHeader("params", headParams);
            entity.addHeader("tid", mClientInfo.tid);
            entity.addHeader("encode", "gzip");
            entity.addHeader("packageName", mClientInfo.packageName);
            entity.addHeader("utc-time", (System.currentTimeMillis() + SharedPreferencesHelper.getTimeDifference(0)) + "");
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
        if (!TextUtils.isEmpty(entity.getUri()) && !entity.getUri().contains("access.szprize.cn")) {//新统计后台不需要加头信息
            if (TextUtils.isEmpty(mClientInfo.userId)) {
                mClientInfo.setUserId(CommonUtils.queryUserId());
            }
            if (TextUtils.isEmpty(mClientInfo.tid)) {
                mClientInfo.tid = PreferencesUtils.getKEY_TID();
            }
            mClientInfo.setClientStartTime(System.currentTimeMillis());
            mClientInfo.setNetStatus(ClientInfo.networkType);
            if (BaseApplication.isNewSign) {
                mClientInfo.setApkSign("new");
            }
            String headParams = new Gson().toJson(mClientInfo);

            headParams = XXTEAUtil.getParamsEncypt(headParams);
            if (!TextUtils.isEmpty(headParams)) {
                entity.addHeader("params", headParams);
                entity.addHeader("tid", mClientInfo.tid);
//			entity.addHeader("encode", "none");
                entity.addHeader("encode", "gzip");
                entity.addHeader("packageName", mClientInfo.packageName);
                entity.addHeader("utc-time", (System.currentTimeMillis() + SharedPreferencesHelper.getTimeDifference(0)) + "");

            }
            if (entity != null && entity.getBodyParams() != null) {
                String sign = Verification.getInstance().getSign(
                        entity.getBodyParams());
                entity.addBodyParameter("sign", sign);
            }
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
