package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * 主题评论提交
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/FontInfo/setCommentById",
        builder = DefaultParamsBuilder.class)
public class FontSubmitRequest extends BaseRequest {
    /**提交字体的id*/
    public int fontId ;
    /**提交字体的内容*/
    public String content;
    /**提交评论用户名*/
    public String user_name;
    /**提交评论用户头像*/
    public String icon;
    /**用户ID*/
    public String user_id;
    /**用户的机型*/
    public String phone_model;

    public FontSubmitRequest(){

    }

}
