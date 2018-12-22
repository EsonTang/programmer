package com.prize.prizethemecenter.ui.utils;

import com.prize.prizethemecenter.bean.AutoTipsData;
import com.prize.prizethemecenter.bean.SearchOriginData;
import com.prize.prizethemecenter.request.AutoTipsRequest;
import com.prize.prizethemecenter.request.SearchOriginRequest;
import com.prize.prizethemecenter.response.AutoTipsResponse;
import com.prize.prizethemecenter.response.SearchOriginResponse;
import com.prize.prizethemecenter.ui.adapter.AutoTipsAdapter;
import com.prize.prizethemecenter.ui.adapter.OriginAdapter;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/9/18.
 */
public class SearchRequest {

    private static SearchOriginRequest originRequest;
    private static SearchOriginResponse originResponse;
    private static Callback.Cancelable originHandler;

    private static AutoTipsRequest autoTipsRequest;
    private static AutoTipsResponse autoTipsResponse;
    private static Callback.Cancelable mHandler;

    public static void loadOriginData(String type,final OriginAdapter adapter) {
        originRequest = new SearchOriginRequest();
        originRequest.type =type ;
        originHandler = x.http().post(originRequest, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if(obj.getInt("code")==00000){
                        originResponse = CommonUtils.getObject(result,
                                SearchOriginResponse.class);
                        ArrayList<SearchOriginData.OriginData> origin =originResponse.data.hot_word;
                        if(origin!=null){
                            adapter.setData(origin);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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


    public static void loadAutoTips(String type ,String tips,final  AutoTipsAdapter autoTipsAdapter) {
        autoTipsRequest = new AutoTipsRequest();
        autoTipsRequest.query = tips;
        autoTipsRequest.type = type;
        mHandler = x.http().post(autoTipsRequest, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if(obj.getInt("code")==00000){
                        autoTipsResponse = CommonUtils.getObject(result,
                                AutoTipsResponse.class);
                        ArrayList<AutoTipsData.AutoTip> autoTips =autoTipsResponse.data.tag;
                        if(autoTips!=null){
                            autoTipsAdapter.clearAll();
                            autoTipsAdapter.setData(autoTips);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
}
