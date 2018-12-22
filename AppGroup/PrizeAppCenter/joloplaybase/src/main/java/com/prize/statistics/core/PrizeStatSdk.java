package com.prize.statistics.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.HttpUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.SharedPreferencesHelper;
import com.prize.custmerxutils.XExtends;
import com.prize.statistics.CompressionHelper;
import com.prize.statistics.db.utils.StaticsAgent;
import com.prize.statistics.model.DataBlock;
import com.prize.statistics.model.ExposureBean;
import com.prize.statistics.model.ExposureDataBlock;
import com.prize.statistics.model.ServerTimeBean;
import com.prize.statistics.model.StatisConstant;
import com.prize.statistics.model.TcNote;
import com.prize.statistics.utils.EncryptUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.util.MD5;
import org.xutils.http.RequestParams;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * longbaoxiu
 * 2016/12/17.15:19
 */

public class PrizeStatSdk {

//    private static final String TAG = "PrizeStatSdk";
    /**
     * context
     */
    private Context mContext;
    /**
     * Instance
     */
    private static PrizeStatSdk sInstance;

    private static PrizeStaticsManagerImpl staticsManager;


    /**
     * constructor
     *
     * @param aContext context
     */
    private PrizeStatSdk(Context aContext, PrizeStaticsManagerImpl aStaticsManager) {
        mContext = aContext;
        staticsManager = aStaticsManager;

    }

    /**
     * getInstance
     *
     * @param aContext context
     * @return 返回 TcStaticsManager
     */
    public static synchronized PrizeStatSdk getInstance(Context aContext) {
        if (sInstance == null) {
            synchronized (PrizeStatSdk.class) {
                if (sInstance == null) {
                    sInstance = new PrizeStatSdk(aContext, new PrizeStaticsManagerImpl(aContext));
                }
            }
        }
        return sInstance;
    }

    /**
     * @param eventName 事件名称
     * @param p         Properties
     */
    public void setEventParameter(String eventName, Properties p, boolean upLoadNow) {
        staticsManager.onInitEvent(eventName);
        staticsManager.onEventParameter(p, mContext, upLoadNow);
    }

    /**
     * @param eventName 事件名称
     * @param p         Properties
     */
    public void setDownEventParameter(String eventName, Properties p) {
        staticsManager.onInitEvent(eventName);
        staticsManager.onDownEventParameter(p, mContext);
    }

    /**
     * @param eventName 事件名称
     * @param p         List<ExposureBean>
     */
    public void setExposurearameter(String eventName, List<ExposureBean> p, boolean upLoadNow) {
        staticsManager.onInitExposureEvent(eventName);
        staticsManager.onExposureParameter(p, mContext, upLoadNow);
    }

    /**
     * @param eventName 事件名称
     * @param p         List<ExposureBean>
     */
    public void trackNewDown(String eventName, ExposureBean p) {
        staticsManager.onInitExposureEvent(eventName);
        staticsManager.onNewDown(p, mContext);
    }

    /**
     * 获取服务器时间
     */
    public void getServeTime() {
        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET)
            return;
        RequestParams params = new RequestParams(StatisConstant.SERVER_TIME_URL);
        XExtends.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject o = new JSONObject(result);
                        if (o.getString("code").equals("00000")) {
                            ServerTimeBean bean = GsonParseUtils.parseSingleBean(o.getString("data"), ServerTimeBean.class);
                            if (bean != null && bean.settings != null && !TextUtils.isEmpty(bean.settings.serverTime)) {
                                long serverTime = Long.parseLong(bean.settings.serverTime);
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
     * 上传单条360点击事件数据
     *
     * @param note TcNote
     */
    synchronized void upload360ClickEven(final TcNote note) {

        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET)
            return;
        if (TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
            getPid();
            return;
        }
        if (note == null)
            return;
        final DataBlock dataBlock = StaticsAgent.find360ClickData(note.timeStamp);
        if (dataBlock == null || dataBlock.events == null || dataBlock.events.size() <= 0) {
            return;
        }
        String datas = new Gson().toJson(dataBlock);
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "PrizeStatSdk-upload360ClickEven-datas=" + datas);
        }
        if (TextUtils.isEmpty(datas)) {
            return;
        }
        RequestParams params = new RequestParams(StatisConstant.SERVER_LOGS_URL);
        datas = CompressionHelper.compress(datas) == null ? datas : CompressionHelper.compress(datas);
        params.addBodyParameter("params", datas);
        try {
            String result = XExtends.http().postSync(params, String.class);
            processSingRes(result, note);
        } catch (Throwable throwable) {
            JLog.i("PRIZE2016", "upload360ClickEven-throwable.detail=" + throwable.getMessage() + "--throwable=" + throwable);
        }

    }

    /**
     * 上传单条下载更新数据
     */
    synchronized void uploadSingleDownEven(final TcNote note) {
        JLog.i("PRIZE2016", "uploadSingleDownEven");

        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET)
            return;
        if (TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
            getPid();
            return;
        }
        if(note==null)
            return;
        final DataBlock dataBlock = StaticsAgent.findSingleDownData(note.timeStamp);
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadSingleDownEven-dataBlock="+dataBlock);
        }
        if (dataBlock == null || dataBlock.events == null || dataBlock.events.size() <= 0) {
            return;
        }
        String datas = new Gson().toJson(dataBlock);
        JLog.i("PRIZE2016", "PrizeStatSdk-uploadSingleDownEven-datas="+datas);
        if (TextUtils.isEmpty(datas)) {
            return;
        }
        RequestParams params = new RequestParams(StatisConstant.SERVER_ICSVIP_URL);
        datas = CompressionHelper.compress(datas) == null ? datas : CompressionHelper.compress(datas);
        params.addBodyParameter("params", datas);
        try {
            String result = XExtends.http().postSync(params, String.class);
            processSingRes(result, note);
        } catch (Throwable throwable) {
            JLog.i("PRIZE2016", "upload360ClickEven-throwable.detail=" + throwable.getMessage() + "--throwable=" + throwable);
        }

    }

    /**
     * 删除曝光统计
     *
     * @param result 上传数据返回结果
     * @param note   TcNote
     */
    private void processSingRes(String result, TcNote note) {
        if (!TextUtils.isEmpty(result)) {
            try {
                if (JLog.isDebug) {
                    JLog.i("PRIZE2016", "processSingRes-result=" + result);
                }
                JSONObject o = new JSONObject(result);
                if (o.getString("code").equals("00000")) {
                    StaticsAgent.deletSingleData(note.timeStamp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("PRIZE2016", "uploadEven-er=" + e.getMessage());
            }
        }
    }

    /**
     * 上传事件数据库数据
     */
    void uploadAllEven() {
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadAllEven-=" + ClientInfo.getAPNType(BaseApplication.curContext) + "--=" + PreferencesUtils.getKEY_TID());
        }
        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET)
            return;
        if (TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
            getPid();
            return;
        }
//        JLog.i("PRIZE2016", "PrizeStatSdk-uploadAllEven");
        final Long timeStamp = System.currentTimeMillis();
        RequestParams params = new RequestParams(StatisConstant.SERVER_LOGS_URL);
        final DataBlock dataBlock = StaticsAgent.getDataBlock();
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadAllEven-=" + dataBlock);
        }
        if (dataBlock == null || dataBlock.events == null || dataBlock.events.size() <= 0)
            return;
        String datas = new Gson().toJson(dataBlock);
        if (TextUtils.isEmpty(datas))
            return;
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadAllEven-=" + datas);
        }
        datas = CompressionHelper.compress(datas) == null ? datas : CompressionHelper.compress(datas);
        params.addBodyParameter("params", datas);
        params.setMaxRetryCount(1);
        try {
            String result = XExtends.http().postSync(params, String.class);
            processResponse(result, timeStamp, dataBlock);
        } catch (Throwable throwable) {
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadAllEven-throwable.detail=" + throwable.getMessage() + "--throwable=" + throwable);
        }

    }

    /**
     * 上传曝光数据
     */
    void uploadAllExposureEven() {
        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET)
            return;

        if (TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
            getPid();
            return;
        }
        final Long timeStamp = System.currentTimeMillis();
        RequestParams params = new RequestParams(StatisConstant.SERVER_LOGS_URL);
        final ExposureDataBlock dataBlock = StaticsAgent.getExposureDataBlock();
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadAllExposureEven-dataBlock==null=" + (dataBlock == null));
        }
        if (dataBlock == null || dataBlock.events == null || dataBlock.events.size() <= 0)
            return;
        String datas = new Gson().toJson(dataBlock);
        if (TextUtils.isEmpty(datas))
            return;
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadAllExposureEven-datas=" + datas);

        }
        datas = CompressionHelper.compress(datas) == null ? datas : CompressionHelper.compress(datas);
        params.addBodyParameter("params", datas);
        params.setMaxRetryCount(1);
        try {
            String result = XExtends.http().postSync(params, String.class);
            processAllExposureResponse(result, timeStamp, dataBlock);
        } catch (Throwable throwable) {
//            JLog.writeDataFileToSD("请求上传失败");
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadAllExposureEven-throwable.detail=" + throwable.getMessage() + "--throwable=" + throwable);
        }
    }

    /**
     * 上传事件成功后，删除本地数据
     *
     * @param result    上传返回结果
     * @param timeStamp 时间戳
     * @param dataBlock DataBlock
     */
    private void processResponse(String result, Long timeStamp, DataBlock dataBlock) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject o = new JSONObject(result);
                if (o.getString("code").equals("00000")) {
                    StaticsAgent.deletData(timeStamp, dataBlock);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("PRIZE2016", "PrizeStatSdk-processResponse-er=" + e.getMessage());
            }
        }
    }

    /**
     * 上传事件成功后，删除本地数据
     *
     * @param result    上传返回结果
     * @param timeStamp 时间戳
     * @param dataBlock ExposureDataBlock
     */
    private void processAllExposureResponse(String result, Long timeStamp, ExposureDataBlock dataBlock) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JLog.i("PRIZE2016", "PrizeStatSdk-processAllExposureResponse-" + result);
                JSONObject o = new JSONObject(result);
                if (o.getString("code").equals("00000")) {
                    StaticsAgent.deletData(timeStamp, dataBlock);
//                    JLog.writeDataFileToSD(new Gson().toJson(dataBlock));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("PRIZE2016", "uploadEven-er=" + e.getMessage());
            }
        }
    }

    /**
     * 上传事件成功后，删除本地数据
     *
     * @param result    上传返回结果
     * @param timeStamp 时间戳
     */
    private void processDownResponse(String result, Long timeStamp) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject o = new JSONObject(result);
                if (o.getString("code").equals("00000")) {
                    StaticsAgent.deletDBdownData(timeStamp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取手机唯一id（服务器分配）
     */
    private void getPid() {
        HttpUtils.getPidFromServer(new HttpUtils.RequestPIDCallBack() {

            @Override
            public void requestOk(String pid) {
                HttpUtils.getUuidFromServer(pid, null);
            }
        });
    }


    /**
     * 上传单条360曝光数据
     */
    synchronized void uploadSingleExposureEven(final TcNote note) {
        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET)
            return;
        if (TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
            getPid();
            return;
        }
        if (note == null)
            return;
        final ExposureDataBlock dataBlock = StaticsAgent.findSingleExousureData(note.timeStamp);
        if (dataBlock == null || dataBlock.events == null || dataBlock.events.size() <= 0) {
            return;
        }
        String datas = new Gson().toJson(dataBlock);
        if (TextUtils.isEmpty(datas)) {
            return;
        }
        RequestParams params = new RequestParams(StatisConstant.SERVER_LOGS_URL);
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "uploadSingleExposureEven-datas=" + datas);
        }
//        JLog.i("PRIZE2016", "uploadSingleExposureEven-加密前datas=" + datas.length());
//        datas = EncryptUtil.encryptDES(datas);
        datas = CompressionHelper.compress(datas) == null ? datas : CompressionHelper.compress(datas);
//        JLog.i("PRIZE2016", "uploadSingleExposureEven-压缩后datas=" + datas.length());
        params.addBodyParameter("params", datas);
        try {
            String result = XExtends.http().postSync(params, String.class);
            processSingRes(result, note);
        } catch (Throwable throwable) {
            JLog.i("PRIZE2016", "uploadSingleExposureEven-throwable.detail=" + throwable.getMessage() + "--throwable=" + throwable);
        }

    }

    /**
     * 上传下载数据
     */
    void uploadDownEven() {
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadDownEven");
        }
        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET)
            return;
        if (TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
            getPid();
            return;
        }
        final Long timeStamp = System.currentTimeMillis();
        RequestParams params = new RequestParams(StatisConstant.SERVER_ICSVIP_URL);
        final DataBlock dataBlock = StaticsAgent.getDownBlock();
        if (dataBlock == null || dataBlock.events == null || dataBlock.events.size() <= 0)
            return;
        String datas = new Gson().toJson(dataBlock);
        if (TextUtils.isEmpty(datas))
            return;
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadDownEven-datas=" + datas);
        }
        datas = CompressionHelper.compress(datas) == null ? datas : CompressionHelper.compress(datas);
        params.addBodyParameter("params", datas);
        params.setMaxRetryCount(1);
        try {
            String result = XExtends.http().postSync(params, String.class);
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadDownEven-result=" + result);
            processDownResponse(result, timeStamp);
        } catch (Throwable throwable) {
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadDownEven-throwable.detail=" + throwable.getMessage() + "--throwable=" + throwable);
        }
    }

    /**
     * 上传曝光数据 3.2add
     */
    void uploadNewAllExposureEven() {
        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET)
            return;

        final ExposureDataBlock dataBlock = StaticsAgent.getNewExposureDataBlock();
        if (dataBlock == null || dataBlock.events == null || dataBlock.events.size() <= 0)
            return;
        String tid = CommonUtils.getNewTid();
        if (!JLog.isDebug && TextUtils.isEmpty(tid)) {
            return;
        }
        ClientInfo mClientInfo = ClientInfo.getInstance();
        final Long timeStamp = System.currentTimeMillis();//type=1：曝光；Type=2:下载
        if (JLog.isDebug && TextUtils.isEmpty(tid)) {
            if (TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
                getPid();
                return;
            }
            tid = MD5.md5(PreferencesUtils.getKEY_TID());
        }
        String url = String.format(Locale.getDefault(), StatisConstant.NEW_STATICS,
                tid, 1, mClientInfo.appVersion, mClientInfo.model, mClientInfo.channel,
                mClientInfo.brand);
        RequestParams params = new RequestParams(url);
        String datas = new Gson().toJson(dataBlock);
        if (TextUtils.isEmpty(datas))
            return;
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadNewAllExposureEven-dataBlock=" + dataBlock);
        }
        byte[] encode = EncryptUtil.encrypt(datas.getBytes(Charset.forName("UTF-8")));
        params.addBodyParameter("data", new String(encode, Charset.forName("UTF-8")));
        params.setMaxRetryCount(1);
        try {
            String result = XExtends.http().postSync(params, String.class);
            processNewExposureRes(result, timeStamp, 1);
        } catch (Throwable throwable) {
//            JLog.writeDataFileToSD("请求上传失败");
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadNewAllExposureEven-throwable.detail=" + throwable.getMessage() + "--throwable=" + throwable);
        }
    }


    /**
     * 上传下载数据 3.2add
     */
    void uploadNewDownEven() {
        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET)
            return;
        final ExposureDataBlock dataBlock = StaticsAgent.getNewDownBlock();
        if (dataBlock == null || dataBlock.events == null || dataBlock.events.size() <= 0) {
            return;
        }
        String tid = CommonUtils.getNewTid();
        if (!JLog.isDebug && TextUtils.isEmpty(tid)) {
            return;
        }
        ClientInfo mClientInfo = ClientInfo.getInstance();
        final Long timeStamp = System.currentTimeMillis();//type=1：曝光；Type=2:下载
        if (JLog.isDebug && TextUtils.isEmpty(tid)) {
            if (TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
                getPid();
                return;
            }
            tid = MD5.md5(PreferencesUtils.getKEY_TID());
        }
        String url = String.format(Locale.getDefault(), StatisConstant.NEW_STATICS,
                tid, 2, mClientInfo.appVersion, mClientInfo.model, mClientInfo.channel,
                mClientInfo.brand);
        RequestParams params = new RequestParams(url);
        String datas = new Gson().toJson(dataBlock);
        if (TextUtils.isEmpty(datas))
            return;
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadNewDownEven-datas=" + datas);

        }
        byte[] encode = EncryptUtil.encrypt(datas.getBytes(Charset.forName("UTF-8")));
        params.addBodyParameter("data", new String(encode, Charset.forName("UTF-8")));
        params.setMaxRetryCount(1);
//        if (JLog.isDebug) {
//            JLog.writeDataFileToSD(new String(encode,Charset.forName("UTF-8")));
////
//        }
        try {
            String result = XExtends.http().postSync(params, String.class);
            processNewExposureRes(result, timeStamp, 2);
        } catch (Throwable throwable) {
//            JLog.writeDataFileToSD("请求上传失败");
            JLog.i("PRIZE2016", "PrizeStatSdk-uploadNewDownEven-throwable.detail=" + throwable.getMessage() + "--throwable=" + throwable);
        }
    }

    /**
     * 上传事件成功后，删除本地数据
     *
     * @param result    上传返回结果
     * @param timeStamp 时间戳
     * @param type      1:曝光 2：下载
     */
    private void processNewExposureRes(String result, Long timeStamp, int type) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JLog.i("PRIZE2016", "PrizeStatSdk-processNewExposureRes-" + result);
                JSONObject o = new JSONObject(result);
                if (o.getString("code").equals("200")) {
                    StaticsAgent.deletNewStaticsRecord(timeStamp, type);
//                    JLog.writeDataFileToSD(new Gson().toJson(dataBlock));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("PRIZE2016", "uploadEven-er=" + e.getMessage());
            }
        }
    }
}
