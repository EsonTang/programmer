package com.prize.prizethemecenter.request;

import com.prize.app.constants.Constants;

import org.xutils.http.annotation.HttpRequest;
import org.xutils.http.app.DefaultParamsBuilder;

/**
 * Created by pengy on 2016/9/2.
 * 字体评论请求
 */
@HttpRequest(
        host = Constants.GIS_URL,
        path = "ThemeStore/FontInfo/getCommentById",
        builder = DefaultParamsBuilder.class)
public class FontCommitRequest extends BaseRequest {
    /**请求主题的id*/
    public int fontId ;
    /**请求主题的索引*/
    public int pageIndex;
    /**每次请求数量*/
    public int pageSize ;

    public FontCommitRequest(){

    }

}
