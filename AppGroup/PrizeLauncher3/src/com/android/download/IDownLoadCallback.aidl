package com.android.download;
interface IDownLoadCallback{
void updateDownLoadState(in String pakageName,in int state); 
void removeCurrentTask(in String pakageName);
}