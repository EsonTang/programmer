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

package com.prize.app.util;

import android.content.Intent;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.net.HeadResultCallBack;
import com.prize.app.net.datasource.base.PushTimeBean;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpUtils {
    private static final String TAG = "HttpUtils";

    /**
     * Post Request
     *
     * @return String
     * @throws Exception 异常
     */
    public static String doPost(String pkg, String msg) throws Exception {
        StringBuffer b = new StringBuffer("packageName=").append(pkg)
                .append("&msg=").append(msg);
        String parameterData = b.toString();

        URL localURL = new URL(Constants.GIS_URL + "/appinfo/downloadfault?");
        URLConnection connection = localURL.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
        httpURLConnection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        httpURLConnection.setRequestProperty("Content-Length",
                String.valueOf(parameterData.length()));

        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;

        try {
            outputStream = httpURLConnection.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream);

            outputStreamWriter.write(parameterData.toString());
            outputStreamWriter.flush();

            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception(
                        "HTTP Request is not success, Response code is "
                                + httpURLConnection.getResponseCode());
            }

            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }

        } finally {

            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }

            if (reader != null) {
                reader.close();
            }

            if (inputStreamReader != null) {
                inputStreamReader.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }

        }

        return resultBuffer.toString();
    }
//
//    public static void uploadDownAppInfo(final String downloadType,
//                                         final String packageName) {
//        DownLoadDataBean dataBean = new DownLoadDataBean();
//        dataBean.downloadType = downloadType;
//        dataBean.packageName = packageName;
//        dataBean.timeDelta = 0;
//        ArrayList<DownLoadDataBean> datas = new ArrayList<DownLoadDataBean>();
//        datas.add(dataBean);
//        String json = new Gson().toJson(datas);
//
//        String url = Constants.GIS_URL + "/stat/upload";
//        RequestParams reqParams = new RequestParams(url);
//        reqParams.addBodyParameter("type", "download");
//        reqParams.addBodyParameter("datas", json);
//        JLog.e(TAG, "downloadType=" + downloadType + " ,packageName="
//                + packageName);
//        XExtends.http().post(reqParams, new CommonCallback<String>() {
//
//            @Override
//            public void onSuccess(String result) {
//                JSONObject obj;
//                try {
//                    obj = new JSONObject(result);
//                    int code = obj.optInt("code");
//                    // String msg = obj.optString("msg");
//                    if (code == 0) {
////						String content = "code=" + code + "----packageName="
////								+ packageName + "--downloadType="
////								+ downloadType + "/r/n";
////						JLog.writeFileToSD(content);
//                    } else {
//                        DownLoadDataDAO.getInstance().insertApp(downloadType,
//                                packageName, System.currentTimeMillis() + "");
//                    }
//                } catch (JSONException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Throwable ex, boolean isOnCallback) {
//                JLog.e(TAG,
//                        "==========onErrorOne(Throwable ex, boolean isOnCallback)===========");
//                DownLoadDataDAO.getInstance().insertApp(downloadType,
//                        packageName, System.currentTimeMillis() + "");
//            }
//
//            @Override
//            public void onCancelled(CancelledException cex) {
//                DownLoadDataDAO.getInstance().insertApp(downloadType,
//                        packageName, System.currentTimeMillis() + "");
//            }
//
//            @Override
//            public void onFinished() {
//
//            }
//        });
//
//    }

//    public static void uploadDownAppInfo(String json) {
//        String url = Constants.GIS_URL + "/stat/upload";
//        RequestParams reqParams = new RequestParams(url);
//        reqParams.addBodyParameter("type", "download");
//        reqParams.addBodyParameter("datas", json);
//        JLog.e(TAG, "json=" + json.toString());
//        XExtends.http().post(reqParams, new CommonCallback<String>() {
//
//            @Override
//            public void onSuccess(String result) {
//                JSONObject obj;
//                try {
//                    obj = new JSONObject(result);
//                    int code = obj.optInt("code");
//                    if (code == 0) {
//                        DownLoadDataDAO.getInstance().deleteAllDownloadedData();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void onError(Throwable ex, boolean isOnCallback) {
//                JLog.e(TAG,
//                        "==========onErrorTwo(Throwable ex, boolean isOnCallback)===========");
//            }
//
//            @Override
//            public void onCancelled(CancelledException cex) {
//
//            }
//
//            @Override
//            public void onFinished() {
//
//            }
//        });
//
//    }

    /**
     * 获取服务器德推送设置信息
     */
    public static void uploadPushTime() {
        JLog.i("long2017", "uploadPushTime-ClientInfo.networkType=" + ClientInfo.networkType);
        if (ClientInfo.networkType == ClientInfo.NONET)
            return;
        String url = Constants.GIS_URL + "/push/setting";
        RequestParams reqParams = new RequestParams(url);
        XExtends.http().post(reqParams, new CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                JSONObject obj;
                try {
                    obj = new JSONObject(result);
                    int code = obj.optInt("code");
                    if (code == 0) {
                        PushTimeBean pushTimeBean = new Gson().fromJson(
                                obj.optString("data"), PushTimeBean.class);
                        if (pushTimeBean != null) {
                            int pushRequestFrequency = pushTimeBean.settings.pushRequestFrequency;
                            int pushFrequency = pushTimeBean.settings.pushFrequency;
                            boolean garbageSwitch = pushTimeBean.settings.garbageSwitch;
                            int garbageCleanTime = pushTimeBean.settings.garbageCleanTime;
                            int garbageCleanSize = pushTimeBean.settings.garbageCleanSize;
                            float storageOcuppySize = pushTimeBean.settings.storageOcuppySize;
                            DataStoreUtils.saveLocalInfo(
                                    DataStoreUtils.PUSHREQUESTFREQUENCY,
                                    String.valueOf(pushRequestFrequency));
                            /*垃圾清理开关状态*/
                            DataStoreUtils.saveLocalInfo(
                                    DataStoreUtils.TRASHCLEARPUSHONOFF,
                                    String.valueOf(garbageSwitch));
                            /*垃圾清理检测时间间隔*/
                            DataStoreUtils.saveLocalInfo(
                                    DataStoreUtils.TRASHCLEARPUSHFREQUENCY,
                                    String.valueOf(garbageCleanTime));
                            /*垃圾大小超过该size弹push*/
                            DataStoreUtils.saveLocalInfo(
                                    DataStoreUtils.TRASHCLEARGARBAGECLEANSIZE,
                                    String.valueOf(garbageCleanSize));
                            /*内存占用超过该百分比弹push*/
                            DataStoreUtils.saveLocalInfo(
                                    DataStoreUtils.TRASHCLEARSTORAGEOCUPPYSIZE,
                                    String.valueOf(storageOcuppySize));
                            JLog.i("long2017", "pushTimeBean=" + pushTimeBean);
                            if (Boolean.valueOf(pushTimeBean.settings.pushSwitch) && Boolean.valueOf(pushTimeBean.settings.validPushTime)) {
                                if (!TextUtils.isEmpty(DataStoreUtils
                                        .readLocalInfo(DataStoreUtils.PUSH_TIME))) {
                                    long lastTime = Long.valueOf(DataStoreUtils
                                            .readLocalInfo(DataStoreUtils.PUSH_TIME));
                                    long currentTime = System
                                            .currentTimeMillis();
                                    JLog.i("long2017", "(currentTime - lastTime)="+(currentTime - lastTime));
                                    // if ((currentTime - lastTime)
                                    // / (60 * 1000) > pushFrequency) {
                                    if ((currentTime - lastTime)
                                            / (60 * 60 * 1000) > pushFrequency) {
                                        startService(6);
                                    }
                                } else {
                                    //2.1可能需要改版#bugid21354 不执行推送 只保存一个值
//									startService(6);
                                    JLog.i("long2017", "DataStoreUtils.saveLocalInfo(DataStoreUtils.PUSH_TIME,\n" +
                                            "                                            String.valueOf(System.currentTimeMillis()));=");
                                    /** bugId:21354   longbaoixu 2.1版本修复  20160913***/
                                    DataStoreUtils.saveLocalInfo(DataStoreUtils.PUSH_TIME,
                                            String.valueOf(System.currentTimeMillis()));
                                }

                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                JLog.i("long2017", "==========uploadPushTime onError(Throwable ex, boolean isOnCallback)===========");
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
     * 获取服务器德推送设置信息此方法主要针对垃圾清理
     */
    public static void updateCleanSettingInfo() {
        String url = Constants.GIS_URL + "/push/setting";
        RequestParams reqParams = new RequestParams(url);
        XExtends.http().post(reqParams, new CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                JSONObject obj;
                try {
                    obj = new JSONObject(result);
                    int code = obj.optInt("code");
                    if (code == 0) {
                        PushTimeBean pushTimeBean = new Gson().fromJson(
                                obj.optString("data"), PushTimeBean.class);
                        if (pushTimeBean != null) {
                            boolean garbageSwitch = pushTimeBean.settings.garbageSwitch;
                            int garbageCleanTime = pushTimeBean.settings.garbageCleanTime;
                            int garbageCleanSize = pushTimeBean.settings.garbageCleanSize;
                            float storageOcuppySize = pushTimeBean.settings.storageOcuppySize;
                            /*垃圾清理开关状态*/
                            DataStoreUtils.saveLocalInfo(
                                    DataStoreUtils.TRASHCLEARPUSHONOFF,
                                    String.valueOf(garbageSwitch));
                            /*垃圾清理检测时间间隔*/
                            DataStoreUtils.saveLocalInfo(
                                    DataStoreUtils.TRASHCLEARPUSHFREQUENCY,
                                    String.valueOf(garbageCleanTime));
							/*垃圾大小超过该size弹push*/
                            DataStoreUtils.saveLocalInfo(
                                    DataStoreUtils.TRASHCLEARGARBAGECLEANSIZE,
                                    String.valueOf(garbageCleanSize));
							/*内存占用超过该百分比弹push*/
                            DataStoreUtils.saveLocalInfo(
                                    DataStoreUtils.TRASHCLEARSTORAGEOCUPPYSIZE,
                                    String.valueOf(storageOcuppySize));
                            JLog.i("long2017", "updateCleanSettingInfo-pushTimeBean=" + pushTimeBean);
                            if (Boolean.valueOf(garbageSwitch)) {
                                startService(9);
                            }

                        }
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                JLog.e(TAG, "updateCleanSettingInfo onErrorTwo(Throwable ex, boolean isOnCallback)===========");
//                startService(9);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private static void startService(int optType) {
        Intent intent = new Intent(
                "com.prize.appcenter.service.PrizeAppCenterService");
        intent.setClassName(BaseApplication.curContext.getApplicationContext(),
                "com.prize.appcenter.service.PrizeAppCenterService");
        intent.putExtra("optType", optType);
        BaseApplication.curContext.getApplicationContext().startService(intent);
    }

    public static String getUrl(Map<String, String> params, String url) {
        // 添加url参数
        if (params != null) {
            String sign = Verification.getInstance().getSign(
                    params);
            params.put("sign", sign);
            Iterator<String> it = params.keySet().iterator();
            StringBuffer sb = null;
            while (it.hasNext()) {
                String key = it.next();
                String value = params.get(key);
                if (sb == null) {
                    sb = new StringBuffer();
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(key);
                sb.append("=");
                sb.append(value);
            }
            url += sb.toString();
        }
        return url;
    }

    public static void doHeadRequest(String url,
                                     final HeadResultCallBack callBack) {
        StringRequest stringRequest = new StringRequest(Request.Method.HEAD,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }

            @Override
            public void onResponseHeaders(Map<String, String> headers) {
                callBack.onResponseHeaders(headers);
            }

        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                JLog.e("huang", "onErrorResponse(VolleyError error)=");
                // TODO Auto-generated method stub
            }
        }) {

        };
        // 设置连接超时时间15秒
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // requestQueue.add(stringRequest);
        BaseApplication.addToRequestQueue(stringRequest, "head");
    }

    /**
     * 从服务器获取pid
     *
     * @param back RequestCallBack
     */
    public static void getPidFromServer(final RequestPIDCallBack back) {
        RequestParams reqParams = new RequestParams(Constants.PID_URL);
        reqParams.addBodyParameter("KOOBEE", "dido");
        XExtends.http().post(reqParams, new CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                JLog.i(TAG, "getPidFromServer-onError=" + arg0);
            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onSuccess(String result) {
                JLog.i(TAG, "getPidFromServer-result=" + result);
                JSONObject obj;
                try {
                    obj = new JSONObject(result);
                    int code = obj.optInt("code");
                    if (code == 00000) {
                        JSONObject o2 = (JSONObject) obj.opt("data");
                        String pid = o2.optString("pid");
                        if (back != null) {
                            back.requestOk(pid);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * 从服务器获取手机唯一标识，并保存到设置
     *
     * @param pid      这个从服务器获取HttpUtils.getPidFromServer（）；
     * @param callBack RequestUuidCallBack 保存uuid成功后的回调
     */
    public static void getUuidFromServer(String pid, final RequestUuidCallBack callBack) {
        RequestParams reqParams = new RequestParams(Constants.UUID_URL);
        reqParams.addBodyParameter("pid", pid);
        XExtends.http().post(reqParams, new CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {

            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {

            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onSuccess(String result) {
                JLog.i(TAG, "getUuidFromServer-result=" + result);
                JSONObject obj;
                try {
                    obj = new JSONObject(result);
                    int code = obj.optInt("code");
                    if (code == 00000) {// 得到pid
                        JSONObject o2 = (JSONObject) obj.opt("data");
                        String uuid = o2.optString("uuid");
                        PreferencesUtils.saveKEY_TID(uuid);
                        if (callBack == null)
                            return;
                        callBack.onSaveUuidOk(uuid);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * 请求pid成功后回调
     *
     * @author prize
     */
    public interface RequestPIDCallBack {
        /****
         * 请求pid成功后回调
         *
         * @param pid
         *            服务器获取的校验码
         */
        void requestOk(String pid);

    }


    /**
     * 请求uuid成功后回调
     *
     * @author prize
     */
    public interface RequestUuidCallBack {
        /****
         * 请求pid成功后回调
         *
         * @param uuid
         *            服务器获取的uuid
         */
        void onSaveUuidOk(String uuid);

    }

    ;


    public static String jsCallRequestServer(String url, String params) {
        RequestParams reqParams = new RequestParams(Constants.GIS_URL + url);
        Map<String, String> map = getParams(params);
        for (String key : map.keySet()) {
            reqParams.addBodyParameter(key, map.get(key));
        }
        try {
            return XExtends.http().postSync(reqParams, String.class);
        } catch (Throwable e) {
            JLog.i(TAG, e.getMessage());
            e.printStackTrace();
            return "";
        }

    }

    public static Map getParams(String params) {
        Map<String, String> mapParams = new HashMap<String, String>();
        if (!TextUtils.isEmpty(params)) {
            String items[] = params.split("&");
            if (items != null) {
                for (String item : items) {
                    String[] temps = item.split("=");
                    if (temps != null && temps.length == 2) {
                        mapParams.put(temps[0], temps[1]);
                    }
                }
            }
        }
        return mapParams;
    }
}
