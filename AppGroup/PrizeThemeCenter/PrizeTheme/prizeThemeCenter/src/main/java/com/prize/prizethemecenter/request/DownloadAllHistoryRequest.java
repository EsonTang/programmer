package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * Created by pengy on 2016/9/2.
 * 首页主题请求
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/ThemeInfo/download_all",
        builder = DefaultParamsBuilder.class)
public class DownloadAllHistoryRequest extends RequestParams {
    public int page ;
    /**每次请求数量*/
    public int nums ;
    /**云账号ID*/
    public String userid;
    public DownloadAllHistoryRequest(){

    }

}
