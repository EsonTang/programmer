package com.android.download;
import  com.android.download.IDownLoadCallback;
import com.android.download.DownLoadTaskInfo;
interface IDownLoad{
	void setDownLoadCallback(in IDownLoadCallback callback);
    void updateDownLoadTaskInfo(in DownLoadTaskInfo info);
    void startDownLoadTask(in DownLoadTaskInfo info);
    void removeDownLoadTask(String packageName);
}