package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * Created by pengy on 2016/9/2.
 * 首页主题请求
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/Search/getSimilarityList",
        builder = DefaultParamsBuilder.class)
public class SimilarityThemeRequest extends  BaseRequest {
    /**请求参数*/
    public String tag ;
    /**每次请求数量*/
    public int pageSize ;

    public SimilarityThemeRequest(){

    }

}
