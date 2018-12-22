package com.prize.appcenter.bean;

import java.io.Serializable;

/**
 * 装机必备返回bean
 * @创建者 longbaoxiu
 * @创建者 2016/11/5.10:05
 * @描述
 */

public class SignInEveryDayData implements Serializable {
    private static final long serialVersionUID = 631738853073870906L;

    public DetailBean details;

    public class DetailBean implements Serializable{
        private static final long serialVersionUID = -7382084253983183067L;
        public String title;
        public String[] images;
        public String[] rules;
        public SignInSuccessBean resp;
    }
    public SignInSuccessBean resp;
}
