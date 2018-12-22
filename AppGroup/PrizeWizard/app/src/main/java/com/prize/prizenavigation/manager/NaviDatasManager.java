package com.prize.prizenavigation.manager;

import android.os.AsyncTask;

import com.prize.prizenavigation.NavigationApplication;
import com.prize.prizenavigation.bean.ClientInfo;
import com.prize.prizenavigation.bean.NaviDatas;
import com.prize.prizenavigation.utils.CommonUtils;
import com.prize.prizenavigation.utils.IConstants;
import com.prize.prizenavigation.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * 数据获取处理
 * Created by liukun on 2017/3/10.
 */
public class NaviDatasManager {

    private static final String TAG = "NaviDatasManager";

    private List<NaviDatas.ListBean> naviDatasList = new ArrayList<>();

    private static NaviDatasManager mInstance;

    private NaviDatasCallback naviDatasCallback;

    private setUpDownCallback setUpDownCallback;

    public static NaviDatasManager getInstance() {
        if (mInstance == null) {
            synchronized (NaviDatasManager.class) {
                if (mInstance == null) {
                    mInstance = new NaviDatasManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 详情数据获取
     *
     * @param datasCallback
     */
    public void getNaviDatas(final NaviDatasCallback datasCallback) {
        this.naviDatasCallback = datasCallback;
//        String useId = NavigationApplication.queryUserId();
//        String c=ClientInfo.getInstance().toString();
        String imei = ClientInfo.getInstance().getImei();
        String model = ClientInfo.getInstance().getModel();
        String type = String.valueOf(UIUtils.getWindowXP());
        if (naviDatasCallback != null) {
            try {
                //查询数据库数据
                naviDatasList = NavigationApplication.getDbManager().findAll(NaviDatas.ListBean.class);
               /* if (naviDatasList != null && naviDatasList.size() > 0) {
                    naviDatasCallback.onSuccess(naviDatasList, naviDatasList.size());
                }*///bug 51586 注释 本地缓存导致，每次进入应用同步本地与服务器数据，网络异常时才读取本地缓存
            } catch (DbException e1) {
                e1.printStackTrace();
            }
        }
        OkHttpUtils
                .post()
                .url(IConstants.NAVIDATAS_FORMAL_URL)
                .addParams("model", model)
                .addParams("imei",imei)
                .addParams("type",type)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (naviDatasCallback != null) {
                            naviDatasCallback.onFail(e,naviDatasList);
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                            new NaviDatasTask().execute(response);
                    }
                });

    }

    /**
     * 解析网络请求返回数据
     */
    class NaviDatasTask extends AsyncTask<String, Void, NaviDatas> {

        private int total;

        @Override
        protected NaviDatas doInBackground(String... params) {
            String result = params[0];
            NaviDatas naviDatas = CommonUtils.getObject(result, NaviDatas.class);
            return naviDatas;
        }

        @Override
        protected void onPostExecute(NaviDatas naviDatas) {
            if (naviDatas != null) {
                //数据库不为空
                if (naviDatasList != null && naviDatasList.size() > 0) {
                    try {
                        if (naviDatasCallback != null)
                            naviDatasCallback.onSuccess(naviDatas.getList(), naviDatas.getList().size());

                        NavigationApplication.getDbManager().delete(NaviDatas.ListBean.class);
                        NavigationApplication.getDbManager().save(naviDatas.getList());
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                } else {
                    total = naviDatas.getTotal();
                    naviDatasList = naviDatas.getList();
                    if (naviDatasCallback != null)
                        naviDatasCallback.onSuccess(naviDatasList, total);
                    try {
                        if (naviDatasList != null && naviDatasList.size() > 0) {
                            NavigationApplication.getDbManager().delete(NaviDatas.ListBean.class);
                            NavigationApplication.getDbManager().save(naviDatasList);
                        }
                    } catch (DbException e) {
                        e.printStackTrace();
                    }

                }
//                if (naviDatasCallback != null)
//                    naviDatasCallback.onSuccess(naviDatasList, total);

            }
        }
    }

    /**
     * 详情数据回调接口
     */
    public interface NaviDatasCallback {
        void onSuccess(List<NaviDatas.ListBean> datasList, int total);
        void onFail(Exception e,List<NaviDatas.ListBean> datasList);
    }

    /**
     * 提交点赞
     */
    public void setUpDown(String promptId, final int type, final setUpDownCallback setUpDownCallback) {
//        String useId = NavigationApplication.queryUserId();
        String imei = ClientInfo.getInstance().getImei();
        this.setUpDownCallback=setUpDownCallback;
        OkHttpUtils
                .post()
                .url(IConstants.NAVIUPDOWN_FORMAL_URL)
                .addParams("prompt_id", promptId)
                .addParams("imei", imei)
                .addParams("type", String.valueOf(type))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
//                        ToastUtils.showOneToast("提交失败！");
                        if (setUpDownCallback!=null){
                            setUpDownCallback.onUpDown(false,"提交失败!",type);
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            JSONObject object = new JSONObject(response);
                            String code = object.getString("code");
                            String msg = object.getString("msg");
                            if ("0".equals(code)) {
                                if (setUpDownCallback!=null){
                                    setUpDownCallback.onUpDown(true,msg,type);
                                }
//                                isUpDown[0] =true;
//                                ToastUtils.showToast("提交成功！");
                            }else {
                                if (setUpDownCallback!=null){
                                    setUpDownCallback.onUpDown(false,msg,type);
                                }
//                                isUpDown[0] =false;
//                                ToastUtils.showToast(msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    /**
     * 点赞回调接口
     */
    public interface setUpDownCallback {
        //是否成功、返回信息、赞踩
        void onUpDown(Boolean isUpDown,String msg,int type);
    }
}