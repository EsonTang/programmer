package com.prize.prizenavigation.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by liukun on 2017/3/1.
 */
public class TestBeans implements Serializable {

    /**
     * code : 00000
     * msg : ok
     * data : {"list":[{"id":"1","largeUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214279.jpg/168","smallUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214279.jpg/168","title":"号外：集宁到乌兰花的班车出事了！！！！！","content":"内蒙那点事儿"},{"id":"2","largeUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214521.jpg/168","smallUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214521.jpg/168","title":"【夜读】梁晓声：你追求的，就是你人生的意义","content":"人民日报"},{"id":"3","largeUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214277.jpg/168","smallUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214277.jpg/168","title":"孩子们喜欢怎样的房间 你不能装作不知道","content":"尚品宅配"},{"id":"4","largeUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214279.jpg/168","smallUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214279.jpg/168","title":"号外：集宁到乌兰花的班车出事了！！！！！","content":"内蒙那点事儿"},{"id":"5","largeUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214267.jpg/168","smallUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214267.jpg/168","title":"国家电网员工竟然这样办婚礼\u2026\u2026","content":"国网江苏省电力公司"},{"id":"6","largeUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214479.jpg/168","smallUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214479.jpg/168","title":"今天吃点后悔药 开始巅峰人生吧","content":"冷笑话精选"},{"id":"7","largeUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214279.jpg/168","smallUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214279.jpg/168","title":"号外：集宁到乌兰花的班车出事了！！！！！","content":"内蒙那点事儿"},{"id":"8","largeUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214279.jpg/168","smallUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214279.jpg/168","title":"号外：集宁到乌兰花的班车出事了！！！！！","content":"内蒙那点事儿"},{"id":"9","largeUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214279.jpg/168","smallUrl":"http://zxpic.gtimg.com/infonew/0/wechat_pics_-214279.jpg/168","title":"号外：集宁到乌兰花的班车出事了！！！！！","content":"内蒙那点事儿"}]}
     */

    private String code;
    private String msg;
    private DataBean data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean implements Serializable {
        /**
         * id : 1
         * largeUrl : http://zxpic.gtimg.com/infonew/0/wechat_pics_-214279.jpg/168
         * smallUrl : http://zxpic.gtimg.com/infonew/0/wechat_pics_-214279.jpg/168
         * title : 号外：集宁到乌兰花的班车出事了！！！！！
         * content : 内蒙那点事儿
         */

        private List<ListBean> list;

        public List<ListBean> getList() {
            return list;
        }

        public void setList(List<ListBean> list) {
            this.list = list;
        }

        public static class ListBean implements Serializable {
            private String id;
            private String largeUrl;
            private String smallUrl;


            private String listUrl;
            private String title;
            private String content;
            private String count;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getLargeUrl() {
                return largeUrl;
            }

            public void setLargeUrl(String largeUrl) {
                this.largeUrl = largeUrl;
            }

            public String getSmallUrl() {
                return smallUrl;
            }

            public void setSmallUrl(String smallUrl) {
                this.smallUrl = smallUrl;
            }

            public String getListUrl() {
                return listUrl;
            }

            public void setListUrl(String listUrl) {
                this.listUrl = listUrl;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public String getCount() {
                return count;
            }

            public void setCount(String count) {
                this.count = count;
            }
        }
    }
}
