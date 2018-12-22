package com.prize.appcenter.callback;

import com.prize.app.net.datasource.base.AppsItemBean;

import java.util.List;

/**
 * 回调抽屉数据返回结果
 *  longbaoxiu
 *  2017/8/23.10:52
 *
 */

public interface OnDrawerListener {
    void onDataBack(List<AppsItemBean> data, int position);
}
