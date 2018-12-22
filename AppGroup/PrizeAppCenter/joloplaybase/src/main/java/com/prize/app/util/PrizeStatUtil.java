package com.prize.app.util;

import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.statistics.PrizeStatService;
import com.prize.statistics.model.ExposureBean;

import java.util.List;
import java.util.Properties;

import static com.prize.app.constants.Constants.EVEN_NAME_DOWNLOAD;
import static com.prize.app.constants.Constants.EVEN_NAME_UPDATE;

/**
 * 自定义的统计
 * <p>
 * longbaoxiu
 *
 * @version 1.0.0
 */
public class PrizeStatUtil {
    /**
     * 统计应用下载完成(不包含更新)
     */
    public static void onAPPDownloadCusSuccess(String appId, String appName, String pageInfo, String backParams) {
        if (TextUtils.isEmpty(appId)) {
            return;
        }
        if (JLog.isDebug) {
            JLog.i("PrizeStatUtil", "onAPPDownloadCusSuccess-pageInfo=" + pageInfo);
        }
        Properties prop = new Properties();
        prop.setProperty("appId", appId);
        prop.setProperty("appName", appName);
        if (!TextUtils.isEmpty(pageInfo)) {
            prop.setProperty("prizeAppPageInfo", pageInfo);
        }
        if (!TextUtils.isEmpty(backParams)) {
            prop.setProperty("backParams", backParams);
        }
        PrizeStatService.trackDownEvent(BaseApplication.curContext, EVEN_NAME_DOWNLOAD, prop);
    }

    /**
     * 统计应用下载完成(包含更新) 3.2add
     */
    public static void appDownloadSuccess(String appId, String packageName, String appName, String pageInfo) {
        if (TextUtils.isEmpty(appId)) {
            return;
        }
        PrizeStatService.trackNewDown(BaseApplication.curContext, Constants.EVEN_NAME_NEWDOWNLOAD
                ,CommonUtils.formNewPagerExposure(appId, packageName, appName, pageInfo));
    }

    /**
     * 统计应用下载完成(更新)
     */
    public static void onAPPDownloadUpdateSuccess(String appId, String appName, String pageInfo) {
        if (TextUtils.isEmpty(appId)) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("appId", appId);
        prop.setProperty("appName", appName);
        if (!TextUtils.isEmpty(pageInfo)) {
            prop.setProperty("prizeAppPageInfo", pageInfo);
        }
        PrizeStatService.trackDownEvent(BaseApplication.curContext, EVEN_NAME_UPDATE, prop);
    }


    /**
     * 搜索结果点击事件统计
     *
     * @param appId       应用id
     * @param packageName 应用包名
     * @param appName     应用包名称
     * @param searchKey   搜索词
     * @param isDetail    (detail 和 download)
     */
    public static void onSearchResultItemClick(String appId, String packageName, String appName, String searchKey, boolean isDetail) {
        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(packageName) || TextUtils.isEmpty(appName) || TextUtils.isEmpty(searchKey)) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("appId", appId);
        prop.setProperty("packageName", packageName);
        prop.setProperty("searchKey", searchKey);
        prop.setProperty("appName", appName);
        if (isDetail) {
            prop.setProperty("op", "detail");
        } else {
            prop.setProperty("op", "download");
        }
        PrizeStatService.trackCustomKVEvent(BaseApplication.curContext, "search", prop,false);
    }

    /**
     * 设置开关状态
     *
     * @param swith 开关名称
     * @param state 开关状态（on/off）
     */
    public static void onSettingSwith(String swith, String state
    ) {
        if (TextUtils.isEmpty(swith) || TextUtils.isEmpty(state)) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("swithName", swith);
        prop.setProperty("swithState", state);
        PrizeStatService.trackCustomKVEvent(BaseApplication.curContext, "swithState", prop,false);
    }

//    /**
//     * 上传360曝光数据及时上传
//     * @param list List<ExposureBean>
//     */
//    public static void startUploadExposure(List<ExposureBean> list) {
//        if (list == null || list.size() <= 0) {
//            return;
//        }
//        PrizeStatService.trackExposureEvent(BaseApplication.curContext, "exposure", list,true);
//    }

    /**
     * 上传曝光数据 3.2add
     *
     * @param list List<ExposureBean>
     */
    public static void startNewUploadExposure(List<ExposureBean> list) {
        if(!JLog.isDebug&&TextUtils.isEmpty(CommonUtils.getNewTid()))return;
        if (list == null || list.size() <= 0) {
            return;
        }
        PrizeStatService.trackExposureEvent(BaseApplication.curContext, Constants.EVEN_NAME_NEWEXPOSURE, list,false);
    }


//    /**
//     * 统计360应用打点点击
//     */
//    public static void onClickBackParams(String backParams, String appName, String packageName) {
//        if (TextUtils.isEmpty(backParams)) {
//            return;
//        }
//        if (JLog.isDebug) {
//            JLog.i("PrizeStatUtil", "onClickBackParams-appName=" + appName);
//        }
//        Properties prop = new Properties();
//        prop.setProperty("backParams", backParams);
//        prop.setProperty("appName", appName);
//        prop.setProperty("packageName", packageName);
//        PrizeStatService.trackCustomKVEvent(BaseApplication.curContext, Constants.EVEN_NAME_BACKPARAMS, prop,true);
//    }

    /**
     * 统计应用打点安装完成（不包含更新）
     */
    public static void onBackParamsAppInstalled(String backParams) {
        if (TextUtils.isEmpty(backParams)) {
            return;
        }
        Properties prop = new Properties();
        prop.setProperty("backParams", backParams);
        PrizeStatService.trackCustomKVEvent(BaseApplication.curContext, "onBackParamsAppInstalled", prop,false);
    }

    /**
     * push消息收到（2.9add）
     */
    public static void onPushArrive(int pushID) {
        Properties prop = new Properties();
        prop.setProperty("pushID", String.valueOf(pushID));
        PrizeStatService.trackCustomKVEvent(BaseApplication.curContext, "onPushArrive", prop,false);
    }

    /**
     * push消息展示（2.9add）
     */
    public static void onPushShow(int pushID) {
        Properties prop = new Properties();
        prop.setProperty("pushID", String.valueOf(pushID));
        PrizeStatService.trackCustomKVEvent(BaseApplication.curContext, "onPushShow", prop,false);
    }

    /**
     * 点击（2.9add）
     */
    public static void onPushCIick(int pushID) {
        Properties prop = new Properties();
        prop.setProperty("pushID", String.valueOf(pushID));
        PrizeStatService.trackCustomKVEvent(BaseApplication.curContext, "onPushCIick", prop,false);
    }

    /**
     * 静默安装push执行（2.9add）
     */
    public static void onSilentInstallPush(String packageName) {
        Properties prop = new Properties();
        prop.setProperty("pushID", packageName);
        PrizeStatService.trackCustomKVEvent(BaseApplication.curContext, "onSilentInstallPush", prop,false);
    }

    /**
     * 静默卸载push执行（2.9add）
     */
    public static void onSilentUninstallPush(String packageName) {
        Properties prop = new Properties();
        prop.setProperty("pushID", packageName);
        PrizeStatService.trackCustomKVEvent(BaseApplication.curContext, "onSilentUninstallPush", prop,false);
    }
}
