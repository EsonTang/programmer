package com.prize.prizethemecenter.bean;

import java.util.List;

/**
 * Created by Administrator on 2016/11/7.
 */
public class SearchSimilartyData {

    /**
     * id : 39
     * name : 浮华若梦
     * ad_pictrue : http://ad-client.oss-cn-beijing.aliyuncs.com/theme/Topic/Images/1476501962.jpg
     * price : 0.00
     * download_count : 0
     * status : 1
     * tag :
     * category : 简约
     * index_name : theme_search
     */

    private List<TagBean> tag;

    public List<TagBean> getTag() {
        return tag;
    }

    public void setTag(List<TagBean> tag) {
        this.tag = tag;
    }

    public static class TagBean {
        private String id;
        private String name;
        private String ad_pictrue;
        private String price;
        private String download_count;
        private String status;
        private String label;
        private String category;
        private String index_name;

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

        public String getAd_pictrue() {
            return ad_pictrue;
        }

        public void setAd_pictrue(String ad_pictrue) {
            this.ad_pictrue = ad_pictrue;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getDownload_count() {
            return download_count;
        }

        public void setDownload_count(String download_count) {
            this.download_count = download_count;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getIndex_name() {
            return index_name;
        }

        public void setIndex_name(String index_name) {
            this.index_name = index_name;
        }
    }
}
