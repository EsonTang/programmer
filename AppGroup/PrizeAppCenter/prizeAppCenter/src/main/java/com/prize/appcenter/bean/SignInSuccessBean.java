package com.prize.appcenter.bean;

import java.io.Serializable;

/**
 * 装机必备返回bean
 * @创建者 longbaoxiu
 * @创建者 2016/11/5.10:05
 * @描述
 */

public class SignInSuccessBean implements Serializable {
    private static final long serialVersionUID = 8843838682293339326L;
    public int condays;
    public int points;
    public int status;
    public String text;
}
