package com.prize.appcenter.bean;

import com.prize.app.beans.HottestBean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * Home进入分类后需要获得的数据
 * @创建者 nieligang
 * @创建者 2016/11/15
 * @描述
 */

public class HottestData implements Serializable {

    private static final long serialVersionUID = 7335809507361837732L;
    public ArrayList<HottestBean> apps = new ArrayList<HottestBean>();
}
