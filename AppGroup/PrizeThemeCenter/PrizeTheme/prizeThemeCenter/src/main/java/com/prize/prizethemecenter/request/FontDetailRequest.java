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
        path = "ThemeStore/FontInfo/SingleFont",
        builder = DefaultParamsBuilder.class)
public class FontDetailRequest extends RequestParams {
    /**请求单个字体的id*/
    public int fontId ;
    /**每次请求数量*/
    public int pageSize ;
    /**每次请求数量*/
    public String userId;

    public FontDetailRequest(){

    }

}
