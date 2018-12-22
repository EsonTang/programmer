package com.prize.app.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @创建者 longbaoxiu
 * @创建者 2017/3/29.17:49
 * @描述 榜单概述请求返回
 */

public class RankOverViewResponse implements Serializable {

    public List<RankOverViewBean>  list = new ArrayList<>();
}
