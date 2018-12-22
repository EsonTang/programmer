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
        path = "ThemeStore/ThemeInfo/SingleTheme",
        builder = DefaultParamsBuilder.class)
public class SimpleThemeDetailRequest extends RequestParams {
    /**请求主题的id*/
    public int themeId ;
    /**每次请求数量*/
    public int pageSize ;
    /**云账号ID*/
    public String userId;
    public SimpleThemeDetailRequest(){

    }

}
