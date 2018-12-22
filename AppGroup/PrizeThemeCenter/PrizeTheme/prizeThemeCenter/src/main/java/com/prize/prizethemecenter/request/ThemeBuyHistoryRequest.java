package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * Created by pengy on 2016/9/14.
 * 字体支付请求
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/ThemeInfo/buyHistory",
        builder = DefaultParamsBuilder.class)
public class ThemeBuyHistoryRequest extends  BaseRequest {
    /**用户ID*/
    public String userid;
    /**字体请求*/
    public int theme_id;
    /**购买的价钱*/
    public String price;
    /**用户的机型*/
    public String model;
    /**用户头像*/
    public String user_icon;
    /**用户昵称*/
    public String user_name;

    public ThemeBuyHistoryRequest(){
    }
}
