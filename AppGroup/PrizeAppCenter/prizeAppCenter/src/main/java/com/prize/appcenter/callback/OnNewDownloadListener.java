package com.prize.appcenter.callback;

import com.prize.app.net.datasource.base.AppsItemBean;

import org.xutils.common.Callback;

import java.util.List;

/**
 * 第一次点击下载按钮，请求抽屉数据
 * longbaoxiu
 *  2017/8/16.15:01
 *
 */

public interface OnNewDownloadListener {

    /**
     * 请求抽屉数据返回成功后
     *
     * @param appId      应用id
     * @param appName    应用名称
     * @param drawerData 应用列表List<AppsItemBean>
     */
    void onDownLoad(String appId, String appName, List<AppsItemBean> drawerData,int positon);

    /**
     * 请求抽屉数据
     * @param cancelable Callback
     */
    void onRequestDrawerData(Callback.Cancelable cancelable);
}
