/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p/>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/
package com.prize.appcenter.ui.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.appcenter.bean.RecommandAppData;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

/**
 * Title:
 * Desc:    推荐池工具类-下载队列应用推荐
 * Version:
 * Created by huangchangguo
 * on   2016/9/9  11:16
 * <p/>
 * Update Description: 更新描述
 * Updater:  更新者
 * Update Time:  更新时间
 */

public class RecommendPoolUtils {


//    private static Callback.Cancelable mCancelable;


    public interface RecommendPoolDataCallBack {

        void getRecommendPoolData(boolean isRequestSuccess, RecommandAppData datas);
    }

    /**
     * Desc: 请求数据
     *
     * @param type  1、2、3
     * @param catId type为3的时候使用
     * @param appId type为3的时候使用
     *              <p/>
     *              Created by huangchangguo
     *              Date:  2016/9/9 12:06
     */
    public static void requestRecommendPoolData(String type, String catId, String appId, final RecommendPoolDataCallBack callBack) {
        RequestParams entity = new RequestParams(Constants.GIS_URL + "/recommand/pooltype");
        entity.addBodyParameter("type", type);
        //只有type为3的时候，后面两个参数才使用
        if (type.contains("3") && !TextUtils.isEmpty(appId)) {
            if (!TextUtils.isEmpty(catId)) {
                entity.addBodyParameter("catId", catId);
            }
            entity.addBodyParameter("appId", appId);
        }
        XExtends.http().post(entity, new PrizeXutilStringCallBack<String>() {

            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject o = new JSONObject(result);
                    String data = o.getString("data");
                    RecommandAppData recommandAppData = new Gson().fromJson(data, RecommandAppData.class);
                    callBack.getRecommendPoolData(true, recommandAppData);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                JLog.i("huang-requestRecommendPool", "onError" + ex);
                callBack.getRecommendPoolData(false, null);
            }

        });

    }

    /**
     * 过滤传递过来的App
     *
     * @param preList        以这个集合为标准
     * @param needfilterlist 需要过滤的集合
     */

    public static ArrayList<AppsItemBean> filterSameApp(ArrayList<AppsItemBean> preList, ArrayList<AppsItemBean> needfilterlist) {
        for (int j = 0; j < needfilterlist.size(); j++) {
            for (int i = 0; i < preList.size(); i++) {
                if (needfilterlist.get(j).packageName.contains(preList.get(i).packageName))
                    needfilterlist.remove(j);
            }
        }

        return needfilterlist;

    }

}