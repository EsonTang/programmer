package com.android.download;
import com.prize.app.net.datasource.base.AppsItemBean;
interface IDownLoadCallback{
void updateDownLoadState(in String pakageName,in int state); 
void removeCurrentTask(in String pakageName);
void startDownLoadTask(in AppsItemBean info);
}