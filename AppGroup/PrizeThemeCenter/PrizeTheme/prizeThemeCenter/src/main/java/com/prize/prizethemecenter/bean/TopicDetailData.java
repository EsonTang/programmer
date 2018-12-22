package com.prize.prizethemecenter.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/9/7.
 */
public class TopicDetailData implements Serializable {

    public int pageCount;
    public int pageIndex;
    public int pageSize;
    public int pageItemCount;

    public TopicDetailHead specials;

    public ArrayList<ThemeItemBean> items = new ArrayList<>();

    public class TopicDetailHead implements Serializable {

        public  String id;
        /**title*/
        public String name;
        /**简介*/
        public String intro;
        /**图片路径*/
        public String image;
        /**图片路径*/
        public String big_image;
        public String type;
        public String special_status;
        public String status;
        public String addtime;
    }

}
