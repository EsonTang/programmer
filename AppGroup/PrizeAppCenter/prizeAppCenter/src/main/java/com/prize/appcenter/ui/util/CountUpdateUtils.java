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
package com.prize.appcenter.ui.util;

import android.content.Context;

import com.google.gson.Gson;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppSyncRestoreData;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.app.util.Verification;
import com.prize.appcenter.bean.AppSyncInfo;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帐号联网更新工具类，主要用于同步助手--备份&同步&检测更新时间
 *
 * @author huangchangguo
 *         <p>
 *         2016/7/19
 */

public class CountUpdateUtils {

    private static final String TAG = "CountUpdateUtils";

    /**
     * 更新时间回传接口
     */
    public interface LastUpoadTimeLinstener {

        void getLastUpoadTime(String lastUploadTime, int type);
    }

    /**
     * 同步应用回传接口
     */
    public interface SyncUplaodLinstener {

        void getSyncUplaodData(int type);
    }

    /**
     * 恢复应用回传接口
     */
    public interface RestoreDataLinstener {

        void getRestoreData(ArrayList<AppsItemBean> restoreData, int type);
    }

    /**
     * 联网获取更新的时间
     *
     * @param userId
     * @return
     */
    public static void requestData(String userId,
                                   final LastUpoadTimeLinstener lastTimeLinstener) {
        if (userId == null) {
            return;
        }

        RequestParams params = new RequestParams(Constants.GIS_URL
                + "/appsync/lastuploadtime");

        // MD5加密校验key
        Map<String, String> signKey = new HashMap<String, String>();
        signKey.put("accountId", userId);

        String sign = Verification.getInstance().getSign(signKey);

        params.addBodyParameter("accountId", userId);
        params.addBodyParameter("sign", sign);

        JLog.i(TAG, "这里是发送更新时间的请求,帐号为---------" + userId);
        XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {
            private String lastUploadTime = null;

            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject o = new JSONObject(result);

                    String code = (String) o.get("code");

                    JLog.i(TAG, "---onSuccess这里是发送更新时间的返回值---code:" + code);

                    if (code.equals("0")) {

                        JSONObject data = o.getJSONObject("data");

                        lastUploadTime = (String) data.get("lastUploadTime");
                        JLog.i(TAG, "---这里是发送更新时间的返回值---lastUploadTime:"
                                + lastUploadTime.toString()
                                + "---lastTimeLinstener=" + lastTimeLinstener);

                        lastTimeLinstener.getLastUpoadTime(lastUploadTime,
                                Constants.SYNC_SUCESS);

                    } else {
                        lastUploadTime = null;
                        lastTimeLinstener.getLastUpoadTime(lastUploadTime,
                                Constants.SYNC_SUCESS);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                JLog.i(TAG, "---onError网络连接错误---ex=" + ex.getMessage());
                ToastUtils.showToast("网络连接错误");
                lastTimeLinstener.getLastUpoadTime(null, Constants.SYNC_ERROR);
            }

        });
    }

    /**
     * 扫描手机，获得本地App应用信息，过滤本身
     * <p>
     * return localAppInfos
     */

    public static ArrayList<AppSyncInfo> getLocalAppData(Context context) {

        ArrayList<AppSyncInfo> usersInfos = new ArrayList<AppSyncInfo>();
        /*
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub
		 */
        // 如果有值，则返回
        final String packageName = context.getPackageName();

        List<AppSyncInfo> allSyncAppsInfo = AppUtil.getAllSyncApp(context);
        // mSystemInfos = new ArrayList<AppInfo>();
        // 开始分类所有程序
        for (AppSyncInfo appInfo : allSyncAppsInfo) {
            // 如果是系统程序，就放到系统集合中
            // if (appInfo.isSystemApp) {
            // mSystemInfos.add(appInfo);
            // } else {
            // 反之，就是用户程序
            // 自己是第三方包的话，不存入appInfo
            if (appInfo.packageName.equals(packageName)) {
                continue;
            } else {
                usersInfos.add(appInfo);

            }


        }

		/*
		 * }) { }.start();
		 */
		/*
		 * getAllSyncAppTask getAllSyncAppTask = new getAllSyncAppTask(context,
		 * new LocalAppDataCallBack() {
		 * 
		 * @Override public void getLocalAppDataCallBack( ArrayList<AppSyncInfo>
		 * appInfo) { mUsersInfos = appInfo;
		 * 
		 * } });
		 * 
		 * getAllSyncAppTask.execute();
		 */

        return usersInfos;

    }

	/*
	 * public interface LocalAppDataCallBack { void
	 * getLocalAppDataCallBack(ArrayList<AppSyncInfo> appInfo); }
	 * 
	 * private class getAllSyncAppTask extends AsyncTask<Void, Void, Void> {
	 * Context mContext; LocalAppDataCallBack mCallbackApps;
	 * 
	 * public getAllSyncAppTask(Context context, LocalAppDataCallBack
	 * callbackApps) { this.mContext = context; this.mCallbackApps =
	 * callbackApps; };
	 * 
	 * @Override protected Void doInBackground(Void... params) {
	 * ArrayList<AppSyncInfo> usersInfos = new ArrayList<AppSyncInfo>();
	 * JLog.i(TAG, "联网上传同步信息---getLocalAppData---mUsersInfos.size():" +
	 * mUsersInfos.size()); // 如果有值，则返回
	 * 
	 * final String packageName = mContext.getPackageName();
	 * 
	 * List<AppSyncInfo> allSyncAppsInfo = AppUtil.getAllSyncApp(mContext); //
	 * mSystemInfos = new ArrayList<AppInfo>(); // 开始分类所有程序 for (AppSyncInfo
	 * appInfo : allSyncAppsInfo) { // 如果是系统程序，就放到系统集合中 // if
	 * (appInfo.isSystemApp) { // mSystemInfos.add(appInfo); // } else { //
	 * 反之，就是用户程序 // 自己是第三方包的话，不存入appInfo if
	 * (appInfo.packageName.equals(packageName)) { continue; } else {
	 * 
	 * usersInfos.add(appInfo); }
	 * 
	 * }
	 * 
	 * mCallbackApps.getLocalAppDataCallBack(usersInfos); return null;
	 * 
	 * }
	 * 
	 * }
	 */

    /**
     * 联网同步应用
     *
     * @param userId
     * @param jsonData
     * @return
     */
    public static void requestSyncupload(String userId, String jsonData,
                                         final SyncUplaodLinstener syncUplaodLinstener) {

		RequestParams params = new RequestParams(Constants.GIS_URL
				+ "/appsync/upapps");

        // MD5加密校验key
        Map<String, String> signKey = new HashMap<String, String>();
        signKey.put("accountId", userId);
        signKey.put("datas", jsonData);

        String sign = Verification.getInstance().getSign(signKey);

        params.addBodyParameter("accountId", userId);
        params.addBodyParameter("datas", jsonData);

        params.addBodyParameter("sign", sign);

        XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {

            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject o = new JSONObject(result);

                    String code = (String) o.get("code");
                    String msg = (String) o.get("msg");

                    if (code.equals("0")) {
                        syncUplaodLinstener.getSyncUplaodData(Constants.SYNC_SUCESS);

                    } else if (code.equals("-1")) {
                        ToastUtils.showToast(msg);
                        syncUplaodLinstener.getSyncUplaodData(Constants.SYNC_ERROR);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ToastUtils.showToast("网络连接错误，应用同步失败");
                syncUplaodLinstener.getSyncUplaodData(Constants.SYNC_ERROR);

            }

            @Override
            public void onCancelled(CancelledException cex) {
                ToastUtils.showToast("网络连接被取消,应用同步失败");
                syncUplaodLinstener.getSyncUplaodData(Constants.SYNC_ERROR);
                JLog.d(TAG, "---这里是发送更新时间的返回---onCancelled");
            }

        });

    }

    /**
     * 应用恢复，对比已安装的应用，如果本地不存在，则显示，如果存在则过滤
     * @param context  Context
     * @param restoreApps  ArrayList<AppsItemBean>
     * @return
     */
    public static ArrayList<AppsItemBean> filterRestoreApp(Context context,
                                                           ArrayList<AppsItemBean> restoreApps) {

        ArrayList<AppsItemBean> listFilterApps = new ArrayList<AppsItemBean>();

        int size = restoreApps.size();

        for (int i = 0; i < size; i++) {

            AppsItemBean restoreApp = restoreApps.get(i);

            String restorePcgNm = restoreApp.packageName;

            if (AppManagerCenter.isAppExist(restorePcgNm)) {
                continue;
            }
            listFilterApps.add(restoreApp);
        }

        return listFilterApps;
    }

    /**
     * 恢复应用到本地,返回上次备份的所有Apps
     *
     * @param userId
     * @return
     */
    public static void requestSyncdownload(String userId,
                                           final RestoreDataLinstener restoreAppLinstener) {

        RequestParams params = new RequestParams(Constants.GIS_URL
                + "/appsync/download");

        // MD5加密校验key
        Map<String, String> signKey = new HashMap<String, String>();
        signKey.put("accountId", userId);

        String sign = Verification.getInstance().getSign(signKey);

        params.addBodyParameter("accountId", userId);

        params.addBodyParameter("sign", sign);

        JLog.d(TAG, "恢复应用到本地,帐号userI是---------" + userId);

        XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {

            @Override
            public void onSuccess(String result) {
                Gson gson = new Gson();

                // 解析成ArrayList
                AppSyncRestoreData restoreData = gson.fromJson(result,
                        AppSyncRestoreData.class);

                int code = restoreData.code;
                JLog.d(TAG, "恢复应用到本地,onSuccess----code：" + code + "----msg："
                        + restoreData.msg);
                // 第一次返回没有备份数据，不用传数据，直接返回
                if (code == -1) {
                    String msg = restoreData.msg;
                    ToastUtils.showToast(msg);
                    restoreAppLinstener.getRestoreData(null, Constants.SYNC_ERROR);
                    return;
                }

                ArrayList<AppsItemBean> restoreApps = restoreData.data.maps.apps;

                JLog.i(TAG, "这里是恢复应用到本地的返回值---restoreApps:" + restoreApps);

                // 返回上次备份的所有Apps
                restoreAppLinstener.getRestoreData(restoreApps,
                        Constants.SYNC_SUCESS);

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ToastUtils.showToast("网络连接错误，应用恢复失败");
                restoreAppLinstener.getRestoreData(null, Constants.SYNC_ERROR);

            }

            @Override
            public void onCancelled(CancelledException cex) {
                ToastUtils.showToast("网络连接被取消,应用恢复失败");
                restoreAppLinstener.getRestoreData(null, Constants.SYNC_ERROR);
            }

        });

    }

}
