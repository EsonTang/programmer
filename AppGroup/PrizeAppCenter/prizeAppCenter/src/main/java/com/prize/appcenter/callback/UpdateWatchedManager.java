/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p/>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/

package com.prize.appcenter.callback;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.prize.app.download.IUpdateWatcher;
import com.prize.app.net.datasource.base.AppsItemBean;

import java.util.ArrayList;
import java.util.List;

/**
 * *
 * Watched管理类
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class UpdateWatchedManager {
    // 存放观察者
    private static RemoteCallbackList<IUpdateWatcher> list = new RemoteCallbackList<IUpdateWatcher>();
    private static List<NetConnectedListener> listNetConnectedListener = new ArrayList<NetConnectedListener>();

    /**
     * 通知观察者更新需要更新的app个数
     *
     * @param number 更新的app个数，当为-1时，表示请求错误
     * @param imgs   图片地址list
     * @param data   List<AppsItemBean>
     */
    public static void notifyChange(int number, List<String> imgs,
                                    List<AppsItemBean> data) {
        if (list == null)
            return;

        try {
            final int n = list.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IUpdateWatcher listener = list.getBroadcastItem(i);
                if (listener != null) {
                    listener.update(number, imgs, data);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException illegalArgumentException) {
            illegalArgumentException.printStackTrace();
        } finally {
            try {
                list.finishBroadcast();
            } catch (IllegalArgumentException illegalArgumentException) {
                illegalArgumentException.printStackTrace();
            }

        }

    }

    /**
     * 注册观察者
     *
     * @param paramNotifyChangeObserver IUpdateWatcher
     */
    public static void registObserver(IUpdateWatcher paramNotifyChangeObserver) {
        list.register(paramNotifyChangeObserver);
    }

    /**
     * 解除注册
     *
     * @param paramNotifyChangeObserver IUpdateWatcher
     */
    public static void unregistObserver(IUpdateWatcher paramNotifyChangeObserver) {
        list.unregister(paramNotifyChangeObserver);
    }

    public static void unregistNetConnectedListener(
            NetConnectedListener Listener) {
        if (listNetConnectedListener.contains(Listener)) {
            listNetConnectedListener.remove(Listener);
        }
    }

    public static void registNetConnectedListener(NetConnectedListener Listener) {
        listNetConnectedListener.add(Listener);
    }

    public static void notifyChange() {
        int i = 0;
        try {
            while (i < listNetConnectedListener.size()) {
                listNetConnectedListener.get(i).onNetConnected();
                i++;
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }
}
