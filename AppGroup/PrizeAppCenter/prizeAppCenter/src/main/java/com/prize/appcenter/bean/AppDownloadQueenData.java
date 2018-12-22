package com.prize.appcenter.bean;

import com.prize.app.util.JLog;
import com.prize.appcenter.activity.AppDownLoadQueenActivity;
import com.prize.appcenter.ui.adapter.DownloadQueenListViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 下载队列的数据
 * <p/>
 * 类名称：AppDownloadQueenData
 * <p/>
 * 创建人：huanglingjun
 * <p/>
 * 修改时间：2016年2月29日 下午5:18:37
 *
 * @version 1.0.0
 */
public class AppDownloadQueenData {
    private List<HashMap<String, Object>> downloadingData = new ArrayList<HashMap<String, Object>>();
    private List<HashMap<String, Object>> downloadedData = new ArrayList<HashMap<String, Object>>();

    public void setDownloadingData(
            List<HashMap<String, Object>> downloadingData) {
        this.downloadingData = downloadingData;
    }

//    //添加下载队列的数据
//    public void addDownloadingData(
//            List<HashMap<String, Object>> downloadingDatas) {
//        downloadingData.addAll(downloadingDatas);
//    }

    public void setDownloadedData(
            List<HashMap<String, Object>> downloadedData) {
        this.downloadedData = downloadedData;
    }

//    public void addDownloadedData(
//            List<HashMap<String, Object>> itemDatas) {
//            downloadedData.addAll(itemDatas);
////        }
//    }

    public List<HashMap<String, Object>> getDownloadingData() {
        return downloadingData;
    }

    public List<HashMap<String, Object>> getDownloadedData() {
        return downloadedData;
    }

    public void removeDownloadingItemData(int position) {
        if (position < 0 || position >= downloadingData.size())
            return;
        this.downloadingData.remove(position);
    }

    /**
     * 已下载添加index=1的数据
     *
     * @param map HashMap<String, Object>
     */
    public void reSetDownloadedData(HashMap<String, Object> map) {
        if (JLog.isDebug) {
            JLog.i("longbaoxiu", "reSetDownloadedData-this.downloadedData.size="
                    + this.downloadedData.size());
        }

        /*如果没有分割线，则重新在0号位置添加一条分割线   huangchangguo 2.1*/
        if (this.downloadedData.size() <= 0) {
            HashMap<String, Object> mapOne = new HashMap<String, Object>();
            mapOne.put(AppDownLoadQueenActivity.TYPE,
                    DownloadQueenListViewAdapter.DIVIDE);
            mapOne.put(AppDownLoadQueenActivity.DATA, null);
            addDivideData(mapOne);
        }
        /*在一号位置添加item   huangchangguo 2.1*/
        this.downloadedData.add(1, map);
        if (JLog.isDebug) {
            JLog.i("longbaoxiu", "reSetDownloadedData-this.downloadedData="
                    + this.downloadedData);
        }
    }

    //在0号位置添加一个分割线
    public void addDivideData(HashMap<String, Object> map) {
        this.downloadedData.add(0, map);
    }

    public HashMap<String, Object> get(int position) {
        if (position >= 0 && position < downloadingData.size()) {
            return downloadingData.get(position);
        } else if (position >= downloadingData.size()
                && position < downloadingData.size() + downloadedData.size()) {
            return downloadedData.get(position - downloadingData.size());
        }
        return null;
    }

    public int size() {
        return downloadingData.size() + downloadedData.size();
    }

    public int downloadingDataSize() {
        return downloadingData.size();
    }

    public int downloadedDataSize() {
        return downloadedData.size();
    }

    public void clearDownloadedData() {
        downloadedData.clear();
    }

    public void clear() {
        if (downloadedData != null && downloadingData != null) {
            downloadingData.clear();
            downloadedData.clear();
        }
    }
}
