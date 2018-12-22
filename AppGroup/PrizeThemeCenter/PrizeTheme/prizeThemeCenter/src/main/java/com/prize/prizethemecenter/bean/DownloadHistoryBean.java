package com.prize.prizethemecenter.bean;

import java.util.List;

/**
 * Created by Administrator on 2016/11/24.
 */
public class DownloadHistoryBean{


    /**
     * data : {"pageItemCount":13,"pageSize":2,"pageIndex":1,"pageCount":7,"item":[{"id":"34","name":"色彩互切","size":"525.27 KB","ad_picture":"http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Images/1476497823.jpg","download_time":"2016-11-22 17:45:33","model":"","type":0},{"id":"36","name":"夏夜初梦","size":"1.78 MB","ad_picture":"http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Images/1476497133.jpg","download_time":"2016-11-23 16:12:44","model":"","type":0}]}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * pageItemCount : 13
         * pageSize : 2
         * pageIndex : 1
         * pageCount : 7
         * item : [{"id":"34","name":"色彩互切","size":"525.27 KB","ad_picture":"http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Images/1476497823.jpg","download_time":"2016-11-22 17:45:33","model":"","type":0},{"id":"36","name":"夏夜初梦","size":"1.78 MB","ad_picture":"http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Images/1476497133.jpg","download_time":"2016-11-23 16:12:44","model":"","type":0}]
         */

        private int pageItemCount;
        private int pageSize;
        private int pageIndex;
        private int pageCount;

        public int getPageItemCount() {
            return pageItemCount;
        }

        public void setPageItemCount(int pageItemCount) {
            this.pageItemCount = pageItemCount;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
        }

        public int getPageCount() {
            return pageCount;
        }

        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }

        private List<ItemBean> item;

        public List<ItemBean> getItem() {
            return item;
        }

        public void setItem(List<ItemBean> item) {
            this.item = item;
        }

        public static class ItemBean {
            /**
             * id : 34
             * name : 色彩互切
             * size : 525.27 KB
             * ad_picture : http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Images/1476497823.jpg
             * download_time : 2016-11-22 17:45:33
             * model :
             * type : 0
             * status : 0 1 2 (0待审核，1上线，2下线)
             */

            private String id;
            private String name;
            private String size;
            private String ad_picture;
            private String download_time;
            private String model;
            private int type;
            private int status;

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getSize() {
                return size;
            }

            public void setSize(String size) {
                this.size = size;
            }

            public String getAd_picture() {
                return ad_picture;
            }

            public void setAd_picture(String ad_picture) {
                this.ad_picture = ad_picture;
            }

            public String getDownload_time() {
                return download_time;
            }

            public void setDownload_time(String download_time) {
                this.download_time = download_time;
            }

            public String getModel() {
                return model;
            }

            public void setModel(String model) {
                this.model = model;
            }

            public int getType() {
                return type;
            }

            public void setType(int type) {
                this.type = type;
            }
        }
    }
}
