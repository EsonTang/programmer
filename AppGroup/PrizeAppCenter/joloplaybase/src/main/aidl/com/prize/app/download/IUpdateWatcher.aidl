package com.prize.app.download;

import android.os.Parcelable;
import java.util.List;
import com.prize.app.net.datasource.base.AppsItemBean;
interface IUpdateWatcher{
  void update(int number,in List<String> imgs, in List<AppsItemBean> listItem);
}