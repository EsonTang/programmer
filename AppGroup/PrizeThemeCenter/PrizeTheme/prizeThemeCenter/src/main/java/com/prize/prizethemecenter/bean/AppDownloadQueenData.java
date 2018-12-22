package com.prize.prizethemecenter.bean;

import com.prize.app.util.JLog;
import com.prize.prizethemecenter.activity.DownLoadQueenActivity;
import com.prize.prizethemecenter.ui.adapter.DownloadQueenListViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 */
public class AppDownloadQueenData {
	private ArrayList<HashMap<String, Object>> downloadingData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> downloadedData = new ArrayList<HashMap<String, Object>>();

	public void setDownloadingData(
			ArrayList<HashMap<String, Object>> downloadingData) {
		this.downloadingData = downloadingData;
	}

	public void setDownloadedData(
			ArrayList<HashMap<String, Object>> downloadedData) {
		this.downloadedData = downloadedData;
	}

	public ArrayList<HashMap<String, Object>> getDownloadingData() {
		return downloadingData;
	}

	public ArrayList<HashMap<String, Object>> getDownloadedData() {
		return downloadedData;
	}

	public void removeDownloadingItemData(int position) {
		if (position < 0 || position >= downloadingData.size())
			return;
		this.downloadingData.remove(position);
	}

	/**
	 * 
	 * 已下载添加index=1的数据
	 * 
	 * @param map
	 *
	 */
	public void reSetDownloadedData(HashMap<String, Object> map) {
		JLog.i("longbaoxiu", "reSetDownloadedData-this.downloadedData.size="
				+ this.downloadedData.size());
		if (this.downloadedData.size() <= 0) {
			HashMap<String, Object> mapOne = new HashMap<String, Object>();
			mapOne.put(DownLoadQueenActivity.TYPE,
					DownloadQueenListViewAdapter.DIVIDE);
			mapOne.put(DownLoadQueenActivity.DATA, null);
			addDivideData(mapOne);
		}
		this.downloadedData.add(1, map);
	}

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
