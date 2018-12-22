package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 *
 * 我的主题请求
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/ThemeInfo/getUserBuy",
        builder = DefaultParamsBuilder.class)
public class MineThemeRequest extends  BaseRequest {

    public String userid;

    /**请求页数*/
    public int page ;
    /**每次请求数量*/
    public int nums ;

    public MineThemeRequest(){

    }

}
