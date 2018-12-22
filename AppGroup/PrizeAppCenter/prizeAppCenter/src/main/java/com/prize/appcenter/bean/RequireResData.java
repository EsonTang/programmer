package com.prize.appcenter.bean;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 装机必备返回bean
 * @创建者 longbaoxiu
 * @创建者 2016/11/5.10:05
 * @描述
 */

public class RequireResData implements Serializable {
    public String title;
    public List<RequireBean> list = new ArrayList<>();


    public class RequireBean implements  Serializable{
        public int  id;
        public String title;
        public boolean isFilter=false;
        public ArrayList<AppsItemBean> apps = new ArrayList<>();
    }
}
