package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * 主题评论提交
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/ThemeInfo/setCommentById",
        builder = DefaultParamsBuilder.class)
public class ThemeSubmitRequest extends BaseRequest {
    /**提交主题的id*/
    public int themeId ;
    /**提交主题的内容*/
    public String content;
    /**提交评论用户名*/
    public String user_name;
    /**提交评论用户头像*/
    public String icon;
    /**用户ID*/
    public String user_id;
    /**用户的机型*/
    public String phone_model;

    public ThemeSubmitRequest(){

    }

}
