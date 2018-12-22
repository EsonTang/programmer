package com.prize.appcenter.callback;

import com.prize.statistics.model.ExposureBean;

import java.util.List;

/**
 * 横向scrollView滚动回调
 * longbaoxiu
 * 2017/6/8.10:14
 * 横向scrollView滚动回调
 */

public interface ItemStateChangeCallBack {
    /**
     *横向scrollView滚动回调
     * @param beans List<ExposureBean>
     * @param isWonderfulGame 是否是游戏界面，或者是否是精彩游戏
     */
    void OnItemSelect(List<ExposureBean> beans, boolean isWonderfulGame);
}
