package com.prize.prizethemecenter.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/10/13.
 */
public class ThemeCommitBean implements Serializable{

    /**
     * pageCount : 7
     * pageIndex : 1
     * pageSize : 3
     * pageItemCount : 21
     * items : [{"id":"1","type":"1","correlation_id":"31","user_name":"1","icon":null,"date":"2016-07-20","content":"我是来抢沙发的","status":"0"},{"id":"3","type":"1","correlation_id":"31","user_name":"1","icon":null,"date":"2016-07-20","content":"我是来抢沙发的1","status":"0"},{"id":"4","type":"1","correlation_id":"31","user_name":"1","icon":null,"date":"2016-07-20","content":"我是来抢沙发的2","status":"0"}]
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private int pageCount;
        private String pageIndex;
        private String pageSize;
        private String pageItemCount;
        /**
         * id : 1
         * type : 1
         * correlation_id : 31
         * user_name : 1
         * icon : null
         * date : 2016-07-20
         * content : 我是来抢沙发的
         * status : 0
         */

        private List<ItemsBean> items;

        public int getPageCount() {
            return pageCount;
        }

        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }

        public String getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(String pageIndex) {
            this.pageIndex = pageIndex;
        }

        public String getPageSize() {
            return pageSize;
        }

        public void setPageSize(String pageSize) {
            this.pageSize = pageSize;
        }

        public String getPageItemCount() {
            return pageItemCount;
        }

        public void setPageItemCount(String pageItemCount) {
            this.pageItemCount = pageItemCount;
        }

        public List<ItemsBean> getItems() {
            return items;
        }

        public void setItems(List<ItemsBean> items) {
            this.items = items;
        }

        public static class ItemsBean {
            private String id;
            private String type;
            private String correlation_id;
            private String user_name;
            private String icon;
            private String date;
            private String content;
            private String status;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getCorrelation_id() {
                return correlation_id;
            }

            public void setCorrelation_id(String correlation_id) {
                this.correlation_id = correlation_id;
            }

            public String getUser_name() {
                return user_name;
            }

            public void setUser_name(String user_name) {
                this.user_name = user_name;
            }

            public String getIcon() {
                return icon;
            }

            public void setIcon(String icon) {
                this.icon = icon;
            }

            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }
        }
    }
}
