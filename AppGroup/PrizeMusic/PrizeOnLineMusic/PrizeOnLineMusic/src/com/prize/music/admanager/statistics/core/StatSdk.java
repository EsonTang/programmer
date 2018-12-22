package com.prize.music.admanager.statistics.core;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import com.google.gson.Gson;
import com.prize.app.beans.ClientInfo;
import com.prize.app.util.safe.XXTEAUtil;
import com.prize.custmerxutils.XExtends;
import com.prize.music.admanager.presenter.JLog;
import com.prize.music.admanager.statistics.db.utils.StaticsAgent;
import com.prize.music.admanager.statistics.model.DataBlock;
import com.prize.music.admanager.statistics.model.ServerTimeBean;
import com.prize.music.admanager.statistics.model.StatisConstant;
import com.prize.music.admanager.statistics.model.TcNote;
import com.prize.music.admanager.statistics.utils.GsonParseUtils;
import com.prize.music.admanager.statistics.utils.SharedPreferencesHelper;
import com.prize.music.admanager.statistics.utils.Verification;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * @创建者 longbaoxiu
 * @创建者 2016/12/17.15:19
 * @描述
 */

public class StatSdk {

    private static final String TAG = "PrizeStatSdk";
    /**
     * context
     */
    private        Context mContext;
    /**
     * Instance
     */
    private static StatSdk sInstance;

    private static StaticsManagerImpl staticsManager;


    /**
     * constructor
     *
     * @param aContext context
     */
    private StatSdk(Context aContext, StaticsManagerImpl aStaticsManager) {
        mContext = aContext;
        staticsManager = aStaticsManager;

    }

    /**
     * getInstance
     *
     * @param aContext context
     * @return 返回 TcStaticsManager
     */
    public static synchronized StatSdk getInstance(Context aContext) {
        if (sInstance == null) {
            synchronized (StatSdk.class) {
                if (sInstance == null) {
                    sInstance = new StatSdk(aContext, new StaticsManagerImpl(aContext));
                }
            }
        }
        return sInstance;
    }

    /**
     * @param eventName 事件名称
     * @param p
     */
    public void setEventParameter(String eventName, Properties p) {
        staticsManager.onInitEvent(eventName);
        staticsManager.onEventParameter(p, mContext);
    }

    /**
     * 获取服务器时间
     */
    public void getServeTime(final Context mContext) {
        if (ClientInfo.networkType == ClientInfo.NONET)
            return;
        RequestParams params = new RequestParams(StatisConstant.SERVER_TIME_URL);
        XExtends.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                JLog.i("PRIZE", "getServeTime-result=" + result);
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject o = new JSONObject(result);
                        if (o.getString("code").equals("00000")) {
                            ServerTimeBean bean = GsonParseUtils.parseSingleBean(o.getString("data"), ServerTimeBean.class);
                            if (bean != null && bean.settings != null && !TextUtils.isEmpty(bean.settings.serverTime)) {
                                long serverTime = Long.parseLong(bean.settings.serverTime);
                                JLog.i("PRIZE2016", "PrizeStatSdk-getServeTime-存储值=" + (serverTime - System.currentTimeMillis()));
                                SharedPreferencesHelper.putLong(serverTime - System.currentTimeMillis());
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.i("PRIZE", "getServeTime-er=" + e.getMessage());
                    }
                }

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
        });
    }

    /**
     * 上传单条数据
     */
    public synchronized void uploadSingleEven(final TcNote note) {
        if (ClientInfo.networkType == ClientInfo.NONET)
            return;
        if (note == null)
            return;
        final DataBlock dataBlock = StaticsAgent.findSingleData(note.timeStamp);
        if (dataBlock == null || dataBlock.events == null || dataBlock.events.size() <= 0) {
            return;
        }
        String datas = new Gson().toJson(dataBlock);
        if (TextUtils.isEmpty(datas)) {
            return;
        }
        RequestParams params = new RequestParams(StatisConstant.SERVER_LOGS_URL);
        JLog.i("PRIZE2016", "uploadSingleEven-datas=" + datas);
        //        datas = EncryptUtil.encryptDES(datas);
        //        datas = CompressionHelper.compress(datas) == null ? datas : CompressionHelper.compress(datas);
        params.addBodyParameter("params", datas);

        // mClientInfo.setUserId(CommonUtils.queryUserId());
        ClientInfo instance = ClientInfo.getInstance();
        instance.setClientStartTime(System.currentTimeMillis());
        instance.setNetStatus(ClientInfo.networkType);

        String headParams = new Gson().toJson(instance);
        JLog.i("PRIZE2016", "headParams: " + headParams);
        headParams = XXTEAUtil.getParamsEncypt(headParams);
        if (!TextUtils.isEmpty(headParams)) {
            params.addHeader("params", headParams);
        }
        params.addHeader("packageName", mContext.getPackageName());
        params.addHeader("encode", "none");
        //加密签名
        if (params != null && params.getBodyParams() != null) {
            String sign = Verification.getInstance().getSign(
                    params.getBodyParams());
            params.addBodyParameter("sign", sign);
        }

        try {
            String result = XExtends.http().postSync(params, String.class);
            processSingRes(result, note, dataBlock);
        } catch (Throwable throwable) {
            JLog.i("PRIZE2016", "uploadSingleEven-throwable.detail=" + throwable.getMessage() + "--throwable=" + throwable);
        }

    }

    private void processSingRes(String result, TcNote note, DataBlock dataBlock) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject o = new JSONObject(result);
                if (o.getString("code").equals("00000")) {
                    StaticsAgent.deletSingleData(note.timeStamp);
                    JLog.i("PRIZE2016", "uploadSingleEven-processSingRes-dataBlock=" + new Gson().toJson(dataBlock));
                    JLog.writeDataFileToSD("uploadSingleEven" + new Gson().toJson(dataBlock) + "\n");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("PRIZE2016", "uploadEven-er=" + e.getMessage());
            }
        }
    }

    /**
     * 上传s数据库数据
     */
    public void uploadAllEven() {
        if (ClientInfo.networkType == ClientInfo.NONET)
            return;
        JLog.i("PRIZE2016", "uploadAllEven");
        final Long timeStamp = System.currentTimeMillis();
        RequestParams params = new RequestParams(StatisConstant.SERVER_LOGS_URL);
        final DataBlock dataBlock = StaticsAgent.getDataBlock();
        if (dataBlock == null || dataBlock.events == null || dataBlock.events.size() <= 0)
            return;
        String datas = new Gson().toJson(dataBlock);
        if (TextUtils.isEmpty(datas))
            return;
        //        datas = EncryptUtil.encryptDES(datas);
        //        datas = CompressionHelper.compress(datas) == null ? datas : CompressionHelper.compress(datas);
        params.addBodyParameter("params", datas);

        // mClientInfo.setUserId(CommonUtils.queryUserId());
        ClientInfo instance = ClientInfo.getInstance();
        instance.setClientStartTime(System.currentTimeMillis());
        instance.setNetStatus(ClientInfo.networkType);

        String headParams = new Gson().toJson(instance);
        JLog.i(TAG, "headParams: "+headParams);
        headParams = XXTEAUtil.getParamsEncypt(headParams);
        if (!TextUtils.isEmpty(headParams)) {
            params.addHeader("params", headParams);
        }
        params.addHeader("packageName", mContext.getPackageName());
        params.addHeader("encode", "none");
        //加密签名
        if (params != null && params.getBodyParams() != null) {
            String sign = Verification.getInstance().getSign(
                    params.getBodyParams());
            params.addBodyParameter("sign", sign);
        }
        params.setMaxRetryCount(1);
        try {
            String result = XExtends.http().postSync(params, String.class);
            processResponse(result, timeStamp, dataBlock);
        } catch (Throwable throwable) {
            JLog.i("PRIZE2016", "uploadAllEven-throwable.detail=" + throwable.getMessage() + "--throwable=" + throwable);
        }
    }

    private void processResponse(String result, Long timeStamp, DataBlock dataBlock) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject o = new JSONObject(result);
                if (o.getString("code").equals("00000")) {
                    StaticsAgent.deletData(timeStamp);
                    JLog.i("PRIZE2016", "uploadAllEven-processResponse-dataBlock=" + new Gson().toJson(dataBlock));
                    JLog.writeDataFileToSD("uploadAllEven" + new Gson().toJson(dataBlock) + "\n");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("PRIZE2016", "uploadEven-er=" + e.getMessage());
            }
        }
    }


}
