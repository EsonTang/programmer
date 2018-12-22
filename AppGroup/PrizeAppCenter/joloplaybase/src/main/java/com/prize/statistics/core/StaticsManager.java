package com.prize.statistics.core;

import android.content.Context;

import com.prize.statistics.model.ExposureBean;

import java.util.List;
import java.util.Properties;

/**
 * longbaoxiu
 * 2016/12/17.15:12
 *
 */

public interface StaticsManager {
    void onEventParameter(Properties p, Context mContext,boolean upLoadNow);
    void onDownEventParameter(Properties p, Context mContext);
    void onInitEvent(String eventName);
    void onInitExposureEvent(String eventName);
//    void setExposurearameter(List<ExposureBean> p,Context mContext);
    void onExposureParameter(List<ExposureBean> p, Context mContext,boolean upLoadNow);
    void onNewDown(ExposureBean p, Context mContext);
}
