package com.prize.prizenavigation.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.List;

/**
 * Created by liukun on 2017/3/10.
 */
public class NaviDatas implements Serializable{

    /**
     * code : 0
     * msg : ok
     * total : 3
     * list : [{"id":"9","large_icon_url":"http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Prompt/1488878412.jpg","small_icon_url":"http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Prompt/1488878409.jpg","title":"桌面下滑搜索","content":"设置方式：桌面-下滑桌面搜索可快速完成：本地应用、通讯录、短信、互联网新闻内容等","pagenum":1},{"id":"10","large_icon_url":"http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Prompt/1488876953.jpg","small_icon_url":"http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Prompt/1488876947.jpg","title":"桌面下滑搜索","content":"设置方式：桌面-下滑桌面搜索可快速完成：本地应用、通讯录、短信、互联网新闻内容等","pagenum":2},{"id":"11","large_icon_url":"http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Prompt/1488882362.jpg","small_icon_url":"http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Prompt/1488882358.jpg","title":"2222222","content":"设置方式：桌面\u2014\u2014视频本地视频在页面的最底部，可通过右上角的编辑功能，进行批量处理等&lt;/p&gt;","pagenum":3}]
     */

    private String code;
    private String msg;
    private int total;
    /**
     * id : 9
     * large_icon_url : http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Prompt/1488878412.jpg
     * small_icon_url : http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Prompt/1488878409.jpg
     * title : 桌面下滑搜索
     * content : 设置方式：桌面-下滑桌面搜索可快速完成：本地应用、通讯录、短信、互联网新闻内容等
     * pagenum : 1
     */

    private List<ListBean> list;

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

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }
    @Table(name = "prize_navidatas")
    public static class ListBean implements Serializable{
        @Column(name = "id", isId = true)
        private String id;
        @Column(name = "large_icon_url")
        private String large_icon_url;
        @Column(name = "small_icon_url")
        private String small_icon_url;
        @Column(name = "list_icon_url")
        private String list_icon_url;
        @Column(name = "title")
        private String title;
        @Column(name = "content")
        private String content;
        @Column(name = "pagenum")
        private int pagenum;
        @Column(name = "updown")
        private int updown;
        @Column(name = "is_video")
        private String is_video;
        @Column(name = "video_url")
        private String video_url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLarge_icon_url() {
            return large_icon_url;
        }

        public void setLarge_icon_url(String large_icon_url) {
            this.large_icon_url = large_icon_url;
        }

        public String getSmall_icon_url() {
            return small_icon_url;
        }

        public void setSmall_icon_url(String small_icon_url) {
            this.small_icon_url = small_icon_url;
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

        public int getPagenum() {
            return pagenum;
        }

        public void setPagenum(int pagenum) {
            this.pagenum = pagenum;
        }

        public int getUpdown() {
            return updown;
        }

        public void setUpdown(int updown) {
            this.updown = updown;
        }

        public String getList_icon_url() {
            return list_icon_url;
        }

        public void setList_icon_url(String list_icon_url) {
            this.list_icon_url = list_icon_url;
        }

        public String getIs_video() {
            return is_video;
        }

        public void setIs_video(String is_video) {
            this.is_video = is_video;
        }

        public String getVideo_url() {
            return video_url;
        }

        public void setVideo_url(String video_url) {
            this.video_url = video_url;
        }
    }
}
