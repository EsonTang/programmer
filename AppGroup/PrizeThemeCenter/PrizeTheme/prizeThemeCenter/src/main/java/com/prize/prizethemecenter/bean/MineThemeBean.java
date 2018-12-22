package com.prize.prizethemecenter.bean;

import java.util.List;

/**
 * Created by Fanghui on 2017/1/13.
 */
public class MineThemeBean {

    /**
     * Count : 1
     * pageSize : 3
     * pageIndex : 1
     * item : [{"id":"59","userid":"43334169","theme_id":"117","price":"0.10","model":"koobee A3M","buy_time":"2017-01-07 10:25:36"}]
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private String Count;
        /**
         * id : 59
         * userid : 43334169
         * theme_id : 117
         * price : 0.10
         * model : koobee A3M
         * buy_time : 2017-01-07 10:25:36
         */

        private List<ItemBean> item;

        public String getCount() {
            return Count;
        }

        public void setCount(String Count) {
            this.Count = Count;
        }

        public List<ItemBean> getItem() {
            return item;
        }

        public void setItem(List<ItemBean> item) {
            this.item = item;
        }

        public static class ItemBean {
            private String id;
            private String userid;
            private String theme_id;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getUserid() {
                return userid;
            }

            public void setUserid(String userid) {
                this.userid = userid;
            }

            public String getTheme_id() {
                return theme_id;
            }

            public void setTheme_id(String theme_id) {
                this.theme_id = theme_id;
            }
        }
    }
}
