package com.prize.app.net;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.Verification;
import com.prize.app.util.safe.XXTEAUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * * 网络请求
 *
 * @author huanglingjun
 * @version V1.0
 */
public class AppNetTask {
    private OnReslutListener listener = null;
    private String url = null;
    private Map<String, String> params = null;
    private String TAG = "AppNetTask";

    private static ClientInfo mClientInfo = ClientInfo.getInstance();


    /**
     * @param url      请求url
     * @param listener 结果返回监听
     * @param req      请求参数
     */
    public AppNetTask(String url, OnReslutListener listener, Map<String, String> req) {
        this.url = url;
        this.listener = listener;
        this.params = req;
        if (req != null) {
            String sign = Verification.getInstance().getSign(
                    req);
            req.put("sign", sign);
        }

    }

    /**
     * 方法描述：通过volley执行网络请求
     */
    public void postAppInfoByVolley(final String requestTAG) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (null == response) {
                    listener.onFailed();
                } else {
                    listener.onSucess(response);
                }
            }

            @Override
            public void onResponseHeaders(Map<String, String> headers) {
//                String header = headers.get(Constants.LAST_MODIFY);
//                if (TextUtils.isEmpty(header)) {
//                    return;
//                }
//                if (params == null || params.get("pageIndex") == null) {
//                    UpdateCach.getInstance().setlastModifyTime(
//                            requestTAG, header);
//                    return;
//                }
//                if (Integer.parseInt(params.get("pageIndex")) <= 1) {
//                    UpdateCach.getInstance().setlastModifyTime(
//                            requestTAG, header);
//                }
            }

        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                JLog.e("huang", "onErrorResponse(VolleyError error)=");
                listener.onFailed();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // TODO Auto-generated method stub
                return getHeaderParamas();
            }

        };
        // 设置连接超时时间15秒
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // requestQueue.add(stringRequest);
        BaseApplication.addToRequestQueue(stringRequest, requestTAG);
    }

    /**
     * 方法描述：添加header
     *
     * @return Map
     */
    private static Map<String, String> getHeaderParamas() {
        Map<String, String> map = new HashMap<String, String>();
        if(TextUtils.isEmpty(mClientInfo.userId)){
            mClientInfo.setUserId(CommonUtils.queryUserId());
        }
        if(TextUtils.isEmpty(mClientInfo.tid)){
            mClientInfo.tid =PreferencesUtils.getKEY_TID();
        }
        mClientInfo.setClientStartTime(System.currentTimeMillis());
        mClientInfo.setNetStatus(ClientInfo.networkType);

        if (BaseApplication.isNewSign) {
            mClientInfo.setApkSign("new");
        }
        if (JLog.isDebug) {
            JLog.i("MainActivity-","getHeaderParamas---"+mClientInfo);
        }
        String headParams = new Gson().toJson(mClientInfo);

        headParams = XXTEAUtil.getParamsEncypt(headParams);
        if (!TextUtils.isEmpty(headParams)) {
            map.put("params", headParams);
        }
        return map;
    }

    /**
     * 网络请求 结果监听
     *
     * @author prize
     */
    public interface OnReslutListener {

        /**
         * 成功 原始流
         *
         * @param result String
         */
        void onSucess(String result);


        /**
         * 失败 请求后返回数据为null
         */
        void onFailed();
    }

}